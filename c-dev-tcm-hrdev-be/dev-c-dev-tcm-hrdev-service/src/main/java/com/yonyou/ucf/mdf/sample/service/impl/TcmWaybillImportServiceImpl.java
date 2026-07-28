package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMCar;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMRoute;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmHisenseAttendanceData;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmWaybillSheduleShiftsData;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.WaybillExcelRecordDTO;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmCarService;
import com.yonyou.ucf.mdf.sample.service.TcmRouteService;
import com.yonyou.ucf.mdf.sample.service.TcmWaybillImportService;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TcmWaybillImportServiceImpl implements TcmWaybillImportService {

    @Autowired
    private TcmRouteService routeService;

    @Autowired
    private TcmCarService carService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private IBillRepository billRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importWaybillExcel(MultipartFile file) throws Exception {

        Map<String, TCMRoute> routeMap = routeService.getMap();
        if (routeMap == null || routeMap.isEmpty()) {
            throw new RuntimeException("路綫數據缺失，請先維護路綫信息！");
        }
        Set<String> sheetNameList = routeMap.keySet();

        List<WaybillExcelRecordDTO> records = parseWaybillExcel(file, sheetNameList);

        return saveData(records,routeMap);

    }


    private Map<String, Object> saveData(List<WaybillExcelRecordDTO> records, Map<String, TCMRoute> routeMap){

        String tenantId = InvocationInfoProxy.getTenantid();

        Set<String> carNumberSet = new HashSet<>();
        Set<String> waybillEmployeeSet = new HashSet<>();

        for (WaybillExcelRecordDTO record : records) {
            carNumberSet.add(record.getCarNumber());
            waybillEmployeeSet.add(record.getEmployeeId());

        }
        Map<String, TCMCar> carMap = carService.groupCarMapByInternalNo(carNumberSet);
        Map<String, String> staffMap = commonUtil.getStaffByCardIds(waybillEmployeeSet, tenantId);

        String acDate = "1949-10-01";

        List<TcmWaybillSheduleShiftsData> dataList = new ArrayList<>();
        List<TcmWaybillSheduleShiftsData> insertList = new ArrayList<>();
        List<TcmWaybillSheduleShiftsData> updateList = new ArrayList<>();
        for (WaybillExcelRecordDTO record : records) {
            String str = record.getRoute();
            String routeCode = str.endsWith("(標準)")
                    ? str.substring(0, str.length() - "(標準)".length())
                    : str;
            // todo 添加去重校验
            TcmWaybillSheduleShiftsData data = changeData(record);
            TCMRoute tcmRoute = routeMap.get(routeCode);
            if (tcmRoute != null) {
                data.setLine(tcmRoute.getId());
            }
            boolean flag = str.matches("(?i)^N.*");
            if (flag) {
                data.setShift("NM");
            }
            TCMCar tcmCar = carMap.get(record.getCarNumber());
            if (tcmCar != null) {
                data.setCar(tcmCar.getId());
            }
            data.setEmployee_id(staffMap.get(record.getEmployeeId()));
            data.setYtenantId(tenantId);
            dataList.add(data);
            acDate = record.getDate();

        }

        LambdaQueryCondition<TcmWaybillSheduleShiftsData> carCondition = new LambdaQueryCondition<>(new TcmWaybillSheduleShiftsData());
        carCondition.eq(TcmWaybillSheduleShiftsData::getYtenantId, tenantId)
                .eq(TcmWaybillSheduleShiftsData::getDate,acDate)
                .eq(TcmWaybillSheduleShiftsData::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmWaybillSheduleShiftsData> exitDataList = billRepository.queryByClause(carCondition);

        // 按照 date+employee_id+car 组合字段分组
        Map<String, TcmWaybillSheduleShiftsData> exitDataMap = exitDataList.stream()
                .collect(Collectors.toMap(
                        this::buildDataKey,
                        data -> data,
                        (existing, replacement) -> existing
                ));

        // 遍历 dataList，区分新增和更新
        for (TcmWaybillSheduleShiftsData data : dataList) {
            String errorRemark = "";
            // 記錄员工或车辆未匹配到的数据
            if (data.getEmployee_id() == null) {
                errorRemark = "海信工號匹配失敗 ";
            }
            if (data.getCar() == null){
                errorRemark = errorRemark + " 車輛編號匹配失敗";
            }

            String key = buildDataKey(data);
            TcmWaybillSheduleShiftsData existingData = exitDataMap.get(key);
            data.setError_remark(errorRemark);

            if (existingData != null) {
                // 数据存在，更新：将新数据的值赋值到旧数据中，保留旧数据的 id
                copyDataValues(data, existingData);
                updateList.add(existingData);
            } else {
                // 数据不存在，新增
                insertList.add(data);
            }
        }

        if (!insertList.isEmpty()){
            billRepository.insert(insertList);
        }
        if (!updateList.isEmpty()){
            billRepository.update(updateList);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", "200");
        result.put("message", "解析成功");
        result.put("saveCount", insertList.size());
        result.put("updateCount", updateList.size());
        result.put("total", records.size());
        result.put("data", records);
        return result;
    }


    private TcmWaybillSheduleShiftsData changeData(WaybillExcelRecordDTO record){
        TcmWaybillSheduleShiftsData data = new TcmWaybillSheduleShiftsData();
        data.setDate(record.getDate());
        data.setTask_code(record.getTask());
        data.setWaybill_employee_id(record.getEmployeeId());
        data.setDriver_name(record.getEmployeeName());
        data.setDeparture_time(com.yonyou.ucf.mdf.sample.utils.DateUtil.stringToDate("yyyy-MM-dd HH:mm:ss",record.getRunTime()));
        data.setFinish_time(com.yonyou.ucf.mdf.sample.utils.DateUtil.stringToDate("yyyy-MM-dd HH:mm:ss",record.getBackTime()));
        data.setLine_str(record.getRoute());
        try {
            data.setMeal_time(record.getDiningNum() != null && !record.getDiningNum().isEmpty() 
                    ? Integer.valueOf(record.getDiningNum()) : 0);
        } catch (NumberFormatException e) {
            data.setMeal_time(0);
        }
        data.setCar_num(record.getCarNumber());
        data.setGet_car(record.getTakeCarLocation());
        data.setOut_car(record.getGiveCarLocation());
        data.setRun_car_location(record.getRunCarLocation());
        data.setBack_car_location(record.getBackCarLocation());
        data.setRemark(record.getRemark());
        data.setShift(record.getShift());
        return data;
    }

    /**
     * 构建数据主键：date+employee_id+car+shift
     */
    private String buildDataKey(TcmWaybillSheduleShiftsData data) {
        return (data.getDate() != null ? data.getDate() : "") + "|" +
               (data.getWaybill_employee_id() != null ? data.getWaybill_employee_id() : "") + "|" +
               (data.getCar_num() != null ? data.getCar_num() : "") + "|" +
               (data.getShift() != null ? data.getShift() : "");
    }

    /**
     * 将新数据的值复制到旧数据中，保留旧数据的 id
     */
    private void copyDataValues(TcmWaybillSheduleShiftsData source, TcmWaybillSheduleShiftsData target) {
        target.setDate(source.getDate());
        target.setTask_code(source.getTask_code());
        target.setWaybill_employee_id(source.getWaybill_employee_id());
        target.setDriver_name(source.getDriver_name());
        target.setCar(source.getCar());
        target.setCar_num(source.getCar_num());
        target.setDeparture_time(source.getDeparture_time());
        target.setFinish_time(source.getFinish_time());
        target.setLine(source.getLine());
        target.setParking_space(source.getParking_space());
        target.setMeal_time(source.getMeal_time());
        target.setGet_car(source.getGet_car());
        target.setOut_car(source.getOut_car());
        target.setRun_car_location(source.getRun_car_location());
        target.setBack_car_location(source.getBack_car_location());
        target.setRemark(source.getRemark());
        target.setShift(source.getShift());
        target.setLine_str(source.getLine_str());
        target.setCode(source.getCode());
    }
    /**
     * 解析派车单Excel文件
     * @param file Excel文件
     * @param sheetNameSet 需要处理的sheet名称集合
     * @return 解析后的派车单记录列表
     */
    private List<WaybillExcelRecordDTO> parseWaybillExcel(MultipartFile file, Set<String> sheetNameSet) throws Exception {
        List<WaybillExcelRecordDTO> records = new ArrayList<>();
        String baseDate = null;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            // 从Data sheet获取日期数据（B1单元格）
            Sheet dataSheet = workbook.getSheet("Data");
            if (dataSheet != null) {
                Row firstRow = dataSheet.getRow(0);
                if (firstRow != null) {
                    Cell dateCell = firstRow.getCell(1);
                    baseDate = getCellDateValue(dateCell);
                }
            }

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String sheetName = workbook.getSheetName(i).trim();
                String routeCode = sheetName.endsWith("(標準)")
                        ? sheetName.substring(0, sheetName.length() - "(標準)".length())
                        : sheetName;
                if (!sheetNameSet.contains(routeCode)) {
                    continue;
                }

                Sheet sheet = workbook.getSheetAt(i);
                if (sheet == null) {
                    continue;
                }

                List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();

                for (int rowIdx = 4; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (row == null) {
                        continue;
                    }

                    // 解析早更数据（C列到U列）
                    WaybillExcelRecordDTO amRecord = parseAMRecord(sheet, rowIdx, sheetName, mergedRegions);
                    if (amRecord != null) {
                        records.add(amRecord);
                    }

                    // 解析晚更数据（V列到AN列）
                    WaybillExcelRecordDTO pmRecord = parsePMRecord(sheet, rowIdx, sheetName, mergedRegions);
                    if (pmRecord != null) {
                        records.add(pmRecord);
                    }
                }
            }
        }

        // 设置日期字段并处理时间格式
        processDateTime(records, baseDate);

        return records;
    }

    /**
     * 设置日期字段并处理时间格式
     * runTime = date + " " + runTime
     * 如果 backTime >= runTime，则 backTime = date + " " + backTime
     * 否则 backTime = (date+1天) + " " + backTime
     * 最终格式为 yyyy-MM-dd HH:mm:ss
     */
    private void processDateTime(List<WaybillExcelRecordDTO> records, String baseDate) {
        if (baseDate == null || baseDate.isEmpty()) {
            baseDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }

        for (WaybillExcelRecordDTO record : records) {
            record.setDate(baseDate);

            String runTime = record.getRunTime();
            String backTime = record.getBackTime();

            if (runTime != null && !runTime.isEmpty()) {
                record.setRunTime(baseDate + " " + runTime + ":00");
            }

            if (backTime != null && !backTime.isEmpty()) {
                if (runTime != null && !runTime.isEmpty()) {
                    if (isTimeGreaterOrEqual(backTime, runTime)) {
                        record.setBackTime(baseDate + " " + backTime + ":00");
                    } else {
                        record.setBackTime(addOneDay(baseDate) + " " + backTime + ":00");
                    }
                } else {
                    record.setBackTime(baseDate + " " + backTime + ":00");
                }
            }
        }
    }

    /**
     * 比较两个时间的大小（格式 HH:mm）
     * @return true 如果 time1 >= time2
     */
    private boolean isTimeGreaterOrEqual(String time1, String time2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date t1 = sdf.parse(time1);
            Date t2 = sdf.parse(time2);
            return t1.compareTo(t2) >= 0;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 日期加一天
     * @param dateStr 格式 yyyy-MM-dd
     * @return 加一天后的日期字符串
     */
    private String addOneDay(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            return dateStr;
        }
    }

    /**
     * 解析早更数据（C列到U列）
     */
    private WaybillExcelRecordDTO parseAMRecord(Sheet sheet, int rowIdx, String route, List<CellRangeAddress> mergedRegions) {
        String task = getCellValueWithMerged(sheet, rowIdx, 5, mergedRegions);
        if (task == null || task.isEmpty()) {
            return null;
        }

        WaybillExcelRecordDTO record = new WaybillExcelRecordDTO();
        record.setRoute(route);
        record.setShift("AM");
        record.setTakeCarLocation(getCellValueWithMerged(sheet, rowIdx, 2, mergedRegions));
        record.setTakeOil(getCellValueWithMerged(sheet, rowIdx, 3, mergedRegions));
        record.setCarNumber(getCellValueWithMerged(sheet, rowIdx, 4, mergedRegions));
        record.setTask(task);
        record.setRunTime(normalizeTime(getCellValueWithMerged(sheet, rowIdx, 6, mergedRegions)));
        record.setRunCarLocation(getCellValueWithMerged(sheet, rowIdx, 7, mergedRegions));
        record.setEmployeeId(getCellValueWithMerged(sheet, rowIdx, 12, mergedRegions));
        record.setEmployeeName(getCellValueWithMerged(sheet, rowIdx, 13, mergedRegions));
        record.setBackTime(normalizeTime(getCellValueWithMerged(sheet, rowIdx, 14, mergedRegions)));
        record.setBackCarLocation(getCellValueWithMerged(sheet, rowIdx, 15, mergedRegions));
        record.setBackOil(getCellValueWithMerged(sheet, rowIdx, 16, mergedRegions));
        record.setGiveCarLocation(getCellValueWithMerged(sheet, rowIdx, 17, mergedRegions));
        record.setToPM(getCellValueWithMerged(sheet, rowIdx, 18, mergedRegions));
        record.setDiningNum(getCellValueWithMerged(sheet, rowIdx, 19, mergedRegions));
        record.setRemark(getCellValueWithMerged(sheet, rowIdx, 20, mergedRegions));

        return record;
    }

    /**
     * 解析晚更数据（V列到AN列）
     */
    private WaybillExcelRecordDTO parsePMRecord(Sheet sheet, int rowIdx, String route, List<CellRangeAddress> mergedRegions) {
        String task = getCellValueWithMerged(sheet, rowIdx, 25, mergedRegions);
        if (task == null || task.isEmpty()) {
            return null;
        }

        WaybillExcelRecordDTO record = new WaybillExcelRecordDTO();
        record.setRoute(route);
        record.setShift("PM");
        record.setToAM(getCellValueWithMerged(sheet, rowIdx, 21, mergedRegions));
        record.setTakeCarLocation(getCellValueWithMerged(sheet, rowIdx, 22, mergedRegions));
        record.setTakeOil(getCellValueWithMerged(sheet, rowIdx, 23, mergedRegions));
        record.setCarNumber(getCellValueWithMerged(sheet, rowIdx, 24, mergedRegions));
        record.setTask(task);
        record.setRunTime(normalizeTime(getCellValueWithMerged(sheet, rowIdx, 26, mergedRegions)));
        record.setRunCarLocation(getCellValueWithMerged(sheet, rowIdx, 27, mergedRegions));
        record.setEmployeeId(getCellValueWithMerged(sheet, rowIdx, 32, mergedRegions));
        record.setEmployeeName(getCellValueWithMerged(sheet, rowIdx, 33, mergedRegions));
        record.setBackTime(normalizeTime(getCellValueWithMerged(sheet, rowIdx, 34, mergedRegions)));
        record.setBackCarLocation(getCellValueWithMerged(sheet, rowIdx, 35, mergedRegions));
        record.setBackOil(getCellValueWithMerged(sheet, rowIdx, 36, mergedRegions));
        record.setGiveCarLocation(getCellValueWithMerged(sheet, rowIdx, 37, mergedRegions));
        record.setDiningNum(getCellValueWithMerged(sheet, rowIdx, 38, mergedRegions));
        record.setRemark(getCellValueWithMerged(sheet, rowIdx, 39, mergedRegions));

        return record;
    }

    /**
     * 获取单元格值，处理合并单元格情况
     * 如果当前单元格为空且在合并区域内，返回合并区域左上角单元格的值
     */
    private String getCellValueWithMerged(Sheet sheet, int rowIdx, int colIdx, List<CellRangeAddress> mergedRegions) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            return null;
        }

        Cell cell = row.getCell(colIdx);
        String value = getCellValue(cell);

        if (value != null && !value.isEmpty()) {
            return value;
        }

        for (CellRangeAddress region : mergedRegions) {
            if (rowIdx >= region.getFirstRow() && rowIdx <= region.getLastRow()
                    && colIdx >= region.getFirstColumn() && colIdx <= region.getLastColumn()) {
                Row firstRow = sheet.getRow(region.getFirstRow());
                if (firstRow != null) {
                    Cell firstCell = firstRow.getCell(region.getFirstColumn());
                    return getCellValue(firstCell);
                }
            }
        }

        return null;
    }

    /**
     * 获取单元格值（时间字段使用 HH:mm 格式）
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        return String.valueOf((long) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        double value = cell.getNumericCellValue();
                        if (value == Math.floor(value)) {
                            return String.valueOf((long) value);
                        } else {
                            return String.valueOf(value);
                        }
                    } catch (Exception e2) {
                        return null;
                    }
                }
            default:
                return null;
        }
    }

    /**
     * 获取单元格日期值（使用 yyyy-MM-dd 格式）
     */
    private String getCellDateValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        return String.valueOf((long) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        if (DateUtil.isCellDateFormatted(cell)) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            return sdf.format(cell.getDateCellValue());
                        }
                        double value = cell.getNumericCellValue();
                        if (value == Math.floor(value)) {
                            return String.valueOf((long) value);
                        } else {
                            return String.valueOf(value);
                        }
                    } catch (Exception e2) {
                        return null;
                    }
                }
            default:
                return null;
        }
    }

    /**
     * 标准化时间格式
     * 将 m:s 格式转换为 HH:mm（如 7:19 → 07:19，7:9 → 07:09）
     */
    private String normalizeTime(String time) {
        if (time == null || time.isEmpty()) {
            return null;
        }

        String[] parts = time.split(":");
        if (parts.length != 2) {
            return time;
        }

        String hour = parts[0];
        String minute = parts[1];

        if (hour.length() == 1) {
            hour = "0" + hour;
        }

        if (minute.length() == 1) {
            minute = "0" + minute;
        }

        return hour + ":" + minute;
    }
}
