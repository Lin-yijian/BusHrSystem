package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMRoute;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmWaybillSheduleShiftsData;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.WaybillExcelRecord;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.service.TcmWaybillImportService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.apache.poi.ss.usermodel.*;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QuerySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TcmWaybillImportServiceImpl implements TcmWaybillImportService {

    private static final Set<String> EXCLUDE_SHEETS = new HashSet<>(Arrays.asList(
            "Data", "車號", "首頁", "中途插入", "包車", "Sheet1"
    ));

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private IBillQueryRepository queryService;

    @Autowired
    private YmsOidGenerator ymsOidGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importWaybillExcel(MultipartFile file) throws Exception {
        List<WaybillExcelRecord> records = parseExcel(file);
        if (records.isEmpty()) {
            throw new RuntimeException("未解析到有效排班数据");
        }
        return batchInsert(records);
    }

    private List<WaybillExcelRecord> parseExcel(MultipartFile file) throws Exception {
        List<WaybillExcelRecord> allRecords = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            Sheet dataSheet = workbook.getSheet("Data");
            String baseDate = getCellString(dataSheet, colToIndex("B"), 0, formatter, evaluator);
            if (baseDate == null || baseDate.isEmpty()) {
                baseDate = "2026-04-13";
            }

            Map<String, String> hisunMap = new LinkedHashMap<>();
            if (dataSheet != null) {
                for (int r = 2; r <= dataSheet.getLastRowNum(); r++) {
                    String t = getCellString(dataSheet, colToIndex("A"), r, formatter, evaluator);
                    String h = getCellString(dataSheet, colToIndex("B"), r, formatter, evaluator);
                    if (t != null && !t.isEmpty() && h != null && !h.isEmpty()) {
                        hisunMap.put(t, h);
                    }
                }
            }

            int globalSeq = 0;

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String name = workbook.getSheetName(i).trim();
                if (EXCLUDE_SHEETS.contains(name)) {
                    continue;
                }
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet == null) continue;

                boolean isN = name.startsWith("N");
                int emptyCount = 0;

                for (int r = 4; r <= sheet.getLastRowNum(); r++) {
                    String amTask = getCellString(sheet, colToIndex("F"), r, formatter, evaluator);
                    if (amTask != null && !amTask.isEmpty()) {
                        emptyCount = 0;
                        globalSeq++;
                        allRecords.add(makeRecord(baseDate, globalSeq, name, amTask, hisunMap,
                                sheet, r, "AM", formatter, evaluator));
                    } else {
                        emptyCount++;
                    }

                    if (!isN) {
                        String pmTask = getCellString(sheet, colToIndex("Z"), r, formatter, evaluator);
                        if (pmTask != null && !pmTask.isEmpty()) {
                            emptyCount = 0;
                            globalSeq++;
                            allRecords.add(makeRecord(baseDate, globalSeq, name, pmTask, hisunMap,
                                    sheet, r, "PM", formatter, evaluator));
                        }
                    }

                    if (emptyCount >= 3) break;
                }
            }
        }

        return allRecords;
    }

    private WaybillExcelRecord makeRecord(String baseDate, int globalSeq, String routeSheet,
                                           String taskId, Map<String, String> hisunMap,
                                           Sheet sheet, int row, String shiftType,
                                           DataFormatter formatter, FormulaEvaluator evaluator) {
        String carCol = "AM".equals(shiftType) ? "E" : "Y";
        String depCol = "AM".equals(shiftType) ? "G" : "AA";
        String retCol = "AM".equals(shiftType) ? "O" : "AI";
        String locCol = "AM".equals(shiftType) ? "C" : "W";
        String getCarCol = "AM".equals(shiftType) ? "C" : "W";
        String outCarCol = "AM".equals(shiftType) ? "R" : "AL";
        String mealCol = "AM".equals(shiftType) ? "T" : "AM";

        WaybillExcelRecord record = new WaybillExcelRecord();
        record.setDate(baseDate);
        record.setDataCount(globalSeq);
        record.setTaskId(taskId);
        record.setHisunId(lookupHisun(taskId, hisunMap));
        record.setPlate(getCellString(sheet, colToIndex(carCol), row, formatter, evaluator));
        record.setRouteSheet(routeSheet);
        record.setDepartTime(buildDateTime(baseDate, getCellString(sheet, colToIndex(depCol), row, formatter, evaluator)));
        record.setReturnTime(buildDateTime(baseDate, getCellString(sheet, colToIndex(retCol), row, formatter, evaluator)));

        String mealStr = getCellString(sheet, colToIndex(mealCol), row, formatter, evaluator);
        try {
            record.setMealCount(mealStr != null && !mealStr.isEmpty() ? Integer.parseInt(mealStr) : 0);
        } catch (NumberFormatException e) {
            record.setMealCount(0);
        }

        String loc = getCellString(sheet, colToIndex(locCol), row, formatter, evaluator);
        record.setLocation(loc != null ? loc.replaceAll("[\\r\\n]+", "") : "");

        String getCar = getCellString(sheet, colToIndex(getCarCol), row, formatter, evaluator);
        String outCar = getCellString(sheet, colToIndex(outCarCol), row, formatter, evaluator);

        record.setGetCar(getCar != null ? getCar.replaceAll("[\\r\\n]+", "") : "");
        record.setOutCar(outCar != null ? outCar.replaceAll("[\\r\\n]+", "") : "");


        record.setParkingSpace("");

        return record;
    }

    private String lookupHisun(String taskId, Map<String, String> hisunMap) {
        String val = hisunMap.get(taskId);
        if (val != null) return val;
        if (taskId != null && taskId.length() >= 3) {
            String altKey = taskId.substring(0, 2) + "更" + taskId.substring(2);
            val = hisunMap.get(altKey);
            if (val != null) return val;
        }
        return "";
    }

    private Date buildDateTime(String dateStr, String timeStr) {
        if (timeStr == null || timeStr.isEmpty() || "undefined".equals(timeStr) || "null".equals(timeStr)) {
            return null;
        }
        try {
            String[] parts = timeStr.split(":");
            String h = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
            String m = parts.length > 1 ? parts[1] : "00";
            String s = parts.length > 2 ? parts[2] : "00";
//            return new SimpleDateFormat(DATE_TIME_PATTERN).parse(dateStr + " " + h + ":" + m + ":" + s);
            return new SimpleDateFormat(DATE_TIME_PATTERN).parse(dateStr + " " + h + ":" + m + ":" + s);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> batchInsert(List<WaybillExcelRecord> records) {
        String tenantId = InvocationInfoProxy.getTenantid();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        List<String> hisunIds = records.stream()
                .map(WaybillExcelRecord::getHisunId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        Map<String, Map<String, Object>> staffMap = batchQueryStaffByHisunIds(hisunIds, tenantId);

        List<String> routeCodes = records.stream()
                .map(WaybillExcelRecord::getRouteSheet)
                .filter(code -> code != null && !code.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> routeMap = batchQueryRoutesByCodes(routeCodes, tenantId);

        List<TcmWaybillSheduleShiftsData> dataList = new ArrayList<>();

        for (WaybillExcelRecord record : records) {
            String hisunId = record.getHisunId() != null ? record.getHisunId() : "";
            Map<String, Object> staff = staffMap.get(hisunId);
            if (hisunId.isEmpty() || staff == null) {
                failCount++;
                if (errors.size() < 5) errors.add("Staff未找到: hisunId=" + hisunId);
                continue;
            }

            String routeId = routeMap.get(record.getRouteSheet());

            String id = ymsOidGenerator.nextStrId();
            TcmWaybillSheduleShiftsData entity = new TcmWaybillSheduleShiftsData();
            entity.setId(id);
            entity.setYtenantId(tenantId);
            entity.setDate(record.getDate());
            entity.setTask_code(record.getTaskId());
            entity.setWaybill_employee_id(hisunId);
            entity.setDriver_name(staff != null ? (String) staff.get("name") : null);
            entity.setEmployee_id(staff != null ? (String) staff.get("id") : null);
            entity.setCar_num(record.getPlate());
            entity.setDeparture_time(record.getDepartTime());
            entity.setFinish_time(record.getReturnTime());
            entity.setLine(routeId);
            entity.setParking_space(record.getParkingSpace());
            entity.setMeal_time(record.getMealCount());
            entity.setMacau_car_park(record.getLocation());
            entity.setGet_car(record.getGetCar());
            entity.setOut_car(record.getOutCar());

            dataList.add(entity);
        }

        if (!dataList.isEmpty()) {
            billRepository.insert(dataList);
            successCount = dataList.size();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("total", records.size());
        result.put("errors", errors);
        return result;
    }

    private Map<String, Map<String, Object>> batchQueryStaffByHisunIds(List<String> hisunIds, String tenantId) {
        if (hisunIds.isEmpty()) return Collections.emptyMap();

        QuerySchema querySchema = QuerySchema.create()
                .addSelect("id, code, name, staffDefines.ab_01 as cardId");
        querySchema.appendQueryCondition(QueryCondition.name("staffDefines.ab_01").in(hisunIds));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));

        List<Map<String, Object>> mapList = queryService.queryMapBySchema(
                UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());

        if (mapList == null || mapList.isEmpty()) return Collections.emptyMap();

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (Map<String, Object> row : mapList) {
            String cardId = (String) row.get("cardId");
            if (cardId != null) {
                result.put(cardId, row);
            }
        }
        return result;
    }

    private Map<String, String> batchQueryRoutesByCodes(List<String> routeCodes, String tenantId) {
        if (routeCodes.isEmpty()) return Collections.emptyMap();

        LambdaQueryCondition<TCMRoute> condition = new LambdaQueryCondition<>(new TCMRoute());
        condition.in(TCMRoute::getRoute_code, routeCodes)
                .eq(TCMRoute::getYtenantId, tenantId)
                .eq(TCMRoute::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TCMRoute> list = billRepository.queryByClause(condition);

        Map<String, String> result = new LinkedHashMap<>();
        for (TCMRoute route : list) {
            if (route.getRoute_code() != null) {
                result.put(route.getRoute_code(), route.getId());
            }
        }
        return result;
    }

    private static String getCellString(Sheet sheet, int colIdx, int rowIdx, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (sheet == null) return null;
        Row row = sheet.getRow(rowIdx);
        if (row == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        try {
            String val = formatter.formatCellValue(cell, evaluator).trim();
            return val.isEmpty() ? null : val;
        } catch (Exception e) {
            String val = formatter.formatCellValue(cell).trim();
            return val.isEmpty() ? null : val;
        }
    }

    private static int colToIndex(String col) {
        int result = 0;
        for (int i = 0; i < col.length(); i++) {
            result = result * 26 + (col.charAt(i) - 'A' + 1);
        }
        return result - 1;
    }


}
