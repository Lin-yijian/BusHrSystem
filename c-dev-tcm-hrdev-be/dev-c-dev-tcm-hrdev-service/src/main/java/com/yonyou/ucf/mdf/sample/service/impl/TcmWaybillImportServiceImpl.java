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
import org.apache.commons.lang3.StringUtils;
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

//        Map<String, TCMRoute> routeMap = routeService.getMap();
//        if (routeMap == null || routeMap.isEmpty()) {
//            throw new RuntimeException("路綫數據缺失，請先維護路綫信息！");
//        }
        List<WaybillExcelRecordDTO> records = parseWaybillExcel(file);
        return saveData(records);

    }


    private Map<String, Object> saveData(List<WaybillExcelRecordDTO> records){

        String tenantId = InvocationInfoProxy.getTenantid();
        Set<String> waybillEmployeeSet = new HashSet<>();
        for (WaybillExcelRecordDTO record : records) {
            waybillEmployeeSet.add(record.getEmployeeId());

        }
        Map<String, String> staffMap = commonUtil.getStaffByCardIds(waybillEmployeeSet, tenantId);
        String acDate = "1949-10-01";

        List<TcmWaybillSheduleShiftsData> dataList = new ArrayList<>();
        List<TcmWaybillSheduleShiftsData> insertList = new ArrayList<>();
        List<TcmWaybillSheduleShiftsData> updateList = new ArrayList<>();
        for (WaybillExcelRecordDTO record : records) {
            TcmWaybillSheduleShiftsData data = changeData(record);
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
                errorRemark = "海信工號匹配失敗；";
            }
            if (data.getCar_num() == null){
                errorRemark = errorRemark + "車輛編號缺失；";
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
        if (!StringUtils.isEmpty(record.getRunTime())){
            data.setDeparture_time(com.yonyou.ucf.mdf.sample.utils.DateUtil.stringToDate("yyyy-MM-dd HH:mm:ss",record.getRunTime()));
        }
        if (!StringUtils.isEmpty(record.getBackTime())){
            data.setFinish_time(com.yonyou.ucf.mdf.sample.utils.DateUtil.stringToDate("yyyy-MM-dd HH:mm:ss",record.getBackTime()));
        }

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
        // 對應更位：早更取「對應下更」(toPM)，晚更取「對應上更」(toAM)
        if ("PM".equals(record.getShift())) {
            data.setParking_space(record.getToAM());
        } else {
            data.setParking_space(record.getToPM());
        }
        return data;
    }

    /**
     * 构建数据主键：date+employee_id+car+shift
     */
    private String buildDataKey(TcmWaybillSheduleShiftsData data) {
        return (data.getDate() != null ? data.getDate() : "") + "|" +
               (data.getTask_code() != null ? data.getTask_code() : "") + "|" +
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
//        target.setCar(source.getCar());
        target.setCar_num(source.getCar_num());
        target.setDeparture_time(source.getDeparture_time());
        target.setFinish_time(source.getFinish_time());
//        target.setLine(source.getLine());
        target.setParking_space(source.getParking_space());
        target.setMeal_time(source.getMeal_time());
        target.setGet_car(source.getGet_car());
        target.setOut_car(source.getOut_car());
        target.setRun_car_location(source.getRun_car_location());
        target.setBack_car_location(source.getBack_car_location());
        target.setRemark(source.getRemark());
        target.setShift(source.getShift());
        target.setLine_str(source.getLine_str());
        target.setError_remark(source.getError_remark());
//        target.setCode(source.getCode());
    }
    /**
     * 解析派车单Excel文件
     * 1. 先解析Data工作表：B1为日期，A2/B2/C2为标题行，数据从第3行开始，解析为aList
//     * 2. 循环除Data外的工作表：判断第二行（C2-U2合并格子）是否含有"線"，若是则解析正式数据为bList
     * 2. 循环除Data外的工作表：判断第二行（C2-U2合并格子）是否含有"線"，若是则解析正式数据为bList；包車sheet跳过不解析
     * 3. 比对：bList中存在与aList相同task的数据时，从aList中删除
     * 4. 合并剩余aList和bList为cList返回
     * @param file Excel文件
     * @return 解析后的派车单记录列表
     */
    private List<WaybillExcelRecordDTO> parseWaybillExcel(MultipartFile file) throws Exception {
        List<WaybillExcelRecordDTO> aList = new ArrayList<>();
        List<WaybillExcelRecordDTO> bList = new ArrayList<>();
        String baseDate = null;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            // 解析Data工作表：B1为日期，A2/B2/C2为标题行（任務/工號/姓名），数据从第3行开始
            Sheet dataSheet = workbook.getSheet("Data");
            if (dataSheet != null) {
                Row firstRow = dataSheet.getRow(0);
                if (firstRow != null) {
                    Cell dateCell = firstRow.getCell(1);
                    baseDate = getCellDateValue(dateCell);
                }

                // 从第3行（索引2）开始读取A/B/C列数据
                for (int rowIdx = 2; rowIdx <= dataSheet.getLastRowNum(); rowIdx++) {
                    Row row = dataSheet.getRow(rowIdx);
                    if (row == null) {
                        continue;
                    }

                    String task = getCellValue(row.getCell(0));        // A列：任務
                    String employeeId = getCellValue(row.getCell(1));    // B列：工號
                    String employeeName = getCellValue(row.getCell(2)); // C列：姓名

                    if (task == null || task.isEmpty()) {
                        continue;
                    }

                    WaybillExcelRecordDTO record = new WaybillExcelRecordDTO();
                    record.setTask(task);
                    record.setEmployeeId(employeeId);
                    record.setEmployeeName(employeeName);
                    aList.add(record);
                }
            }

            // 循环除了Data之外的工作表
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String sheetName = workbook.getSheetName(i).trim();
                if ("Data".equals(sheetName)) {
                    continue;
                }

                Sheet sheet = workbook.getSheetAt(i);
                if (sheet == null) {
                    continue;
                }

                List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();

//                // 判断第二行数据是否含有"線"或等于"包車"（第二行处于C2-U2合并的格子中）
//                String routeValue = getCellValueWithMerged(sheet, 1, 2, mergedRegions);
//                if (routeValue == null || (!routeValue.contains("線") && !"包車".equals(routeValue))) {
//                    continue;
//                }
                // 判断第二行数据是否含有"線"（第二行处于C2-U2合并的格子中）；包車sheet不解析導入BIP
                String routeValue = getCellValueWithMerged(sheet, 1, 2, mergedRegions);
                if (routeValue == null || !routeValue.contains("線")) {
                    continue;
                }
                // N开头路线：只有C-U范围数据，shift设为NM，无晚更
                boolean isNRoute = routeValue.startsWith("N");

                // 解析正式数据（早更+晚更），逻辑与原方法一致
                for (int rowIdx = 4; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (row == null) {
                        continue;
                    }

                    // 解析早更数据（C列到U列）
                    WaybillExcelRecordDTO amRecord = parseAMRecord(sheet, rowIdx, routeValue, mergedRegions);
                    if (amRecord != null) {
                        // N开头路线：shift覆盖为NM
                        if (isNRoute) {
                            amRecord.setShift("NM");
                        }
                        bList.add(amRecord);
                    }

                    // N开头路线只有C-U范围数据，跳过晚更解析
                    if (!isNRoute) {
                        // 解析晚更数据（V列到AN列）
                        WaybillExcelRecordDTO pmRecord = parsePMRecord(sheet, rowIdx, routeValue, mergedRegions);
                        if (pmRecord != null) {
                            bList.add(pmRecord);
                        }
                    }
                }
            }
        }

        // 比对aList和bList：bList中存在与aList相同task的数据时，先将aList中的employeeId和employeeName复制到bList对应对象，再从aList中删除
        Iterator<WaybillExcelRecordDTO> aIter = aList.iterator();
        while (aIter.hasNext()) {
            WaybillExcelRecordDTO aRecord = aIter.next();
            if (aRecord.getTask() == null) {
                continue;
            }
            boolean matched = false;
            // 在bList中查找相同task的记录
            for (WaybillExcelRecordDTO bRecord : bList) {
                if (aRecord.getTask().equals(bRecord.getTask())) {
                    // 将aList中的employeeId和employeeName复制到bList对应对象
                    bRecord.setEmployeeId(aRecord.getEmployeeId());
                    bRecord.setEmployeeName(aRecord.getEmployeeName());
                    matched = true;
                }else if (aRecord.getEmployeeId().equals(bRecord.getEmployeeId())){
                    matched = true;
                }
            }
            // 仅在bList中找到匹配时才从aList中删除
            if (matched) {
                aIter.remove();
            }
        }

        // 合并剩余aList和bList为cList
        List<WaybillExcelRecordDTO> cList = new ArrayList<>();
        cList.addAll(aList);
        cList.addAll(bList);

        // 设置日期字段并处理时间格式
        processDateTime(cList, baseDate);

        return cList;
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
