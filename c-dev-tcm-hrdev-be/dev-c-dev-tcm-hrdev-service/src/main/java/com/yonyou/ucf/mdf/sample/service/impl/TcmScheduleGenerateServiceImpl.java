package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.aop.SyncLog;
import com.yonyou.ucf.mdf.sample.bill.entity.*;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.enums.TaskTypeEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCalculationService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmScheduleGenerateService;
import com.yonyou.ucf.mdf.sample.service.TcmSyncLogService;
import com.yonyou.ucf.mdf.sample.utils.AttendanceCalcImportContext;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.DateUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class TcmScheduleGenerateServiceImpl implements TcmScheduleGenerateService {

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private YmsOidGenerator ymsOidGenerator;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    protected IBillQueryRepository queryService;

    @Autowired
    private TcmAttendanceCheckRecordService tcmAttendanceCheckRecordService;

    @Autowired
    private TcmAttendanceCalculationService calculationService;

    @Autowired
    private TcmSyncLogService tcmSyncLogService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @SyncLog(model = TaskTypeEnum.HXPG_MAKE)
    public void generateLeaveR(String ruleId,String date) {
        doGenerateLeaveRInternal(ruleId, date);
    }

    /**
     *【两阶段日志】使用预创建的 syncLogId 执行生成休假记录，完成后回填结束时间和状态。
     * <p>不使用 @SyncLog 注解，避免 AOP 再插一条日志。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateLeaveR(String ruleId, String date, String syncLogId) {
        if (StringUtils.isEmpty(syncLogId)) {
            doGenerateLeaveRInternal(ruleId, date);
            return;
        }
        Throwable captured = null;
        int newCount = 0;
        try {
            newCount = doGenerateLeaveRInternal(ruleId, date);
        } catch (Throwable t) {
            captured = t;
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            throw (Error) t;
        } finally {
            String remarkEx = (captured == null) ? ("同步成功，新增 " + newCount + " 条") : null;
            try {
                tcmSyncLogService.finishSyncLog(syncLogId, captured, remarkEx);
            } catch (Exception finishEx) {
                log.error("[generateLeaveR(syncLogId)] 回填同步日志失败 syncLogId={}", syncLogId, finishEx);
            }
        }
    }

    /**
     * 生成休假记录（主逻辑）。内部调用不触发 AOP。
     */
    private int doGenerateLeaveRInternal(String ruleId, String date) {
        TcmLeaveRule rule = queryService.findById(TcmLeaveRule.ENTITY_NAME, ruleId);
        if (rule == null) {
            throw new RuntimeException("请至少选择一条规则进行匹配");
        }
        String original = rule.getOriginal();
        String tenantId = InvocationInfoProxy.getTenantid();
        List<TcmWaybillSheduleShiftsData> dataList = queryWaybillData(date, original, tenantId);
        if (dataList.isEmpty()) {
            throw new RuntimeException("任务编码【" + original + "】下无排更数据，日期：" + date);
        }

        List<TcmLeaveRecord> list = new ArrayList<>();

        java.time.LocalDate baseLocalDate;
        try {
            baseLocalDate = java.time.LocalDate.parse(date,
                    java.time.format.DateTimeFormatter.ofPattern(DateUtil.YYYY_MM_DD));
        } catch (Exception e) {
            throw new RuntimeException("日期格式非法，应为 yyyy-MM-dd：" + date, e);
        }
        String effectiveMonth = baseLocalDate.format(
                java.time.format.DateTimeFormatter.ofPattern(DateUtil.YYYY_MM));
        String startDate = baseLocalDate.toString();
        String endDate = baseLocalDate.toString();
        String leaveType = rule.getReference();

        // 查询已存在的休假记录（start_date + employee_id）
        LambdaQueryCondition<TcmLeaveRecord> leaveCondition = new LambdaQueryCondition<>(new TcmLeaveRecord());
        leaveCondition.eq(TcmLeaveRecord::getStart_date, startDate)
                .eq(TcmLeaveRecord::getYtenantId, tenantId)
                .eq(TcmLeaveRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmLeaveRecord> existingRecords = billRepository.queryByClause(leaveCondition);
        // key = start_date + "|" + employee_id + "|" + leave_type
        Map<String, TcmLeaveRecord> existingMap = new HashMap<>();
        if (existingRecords != null) {
            for (TcmLeaveRecord existing : existingRecords) {
                String key = existing.getStart_date() + "|"
                        + existing.getEmployee_id() + "|"
                        + existing.getLeave_type();
                existingMap.putIfAbsent(key, existing);
            }
        }

        List<TcmLeaveRecord> insertList = new ArrayList<>();
        List<TcmLeaveRecord> updateList = new ArrayList<>();

        for (TcmWaybillSheduleShiftsData data : dataList) {
            TcmLeaveRecord record = new TcmLeaveRecord();
            record.setWaybill_employee_id(data.getWaybill_employee_id());
            record.setEmployee_id(data.getEmployee_id());
            record.setDriver_name(data.getDriver_name());
            record.setLeave_type(leaveType);
            record.setWaybill_shedule(data.getId());
            record.setStart_date(startDate);
            record.setEffective_month(effectiveMonth);
            record.setEnd_date(endDate);
            record.setRemark(data.getRemark());
            record.setLeave_days(1);
            record.setYtenantId(tenantId);

            // 按 start_date + employee_id + leave_type 校验是否已存在
            String key = startDate + "|" + data.getEmployee_id() + "|" + leaveType;
            TcmLeaveRecord existing = existingMap.get(key);
            if (existing != null) {
                // 已存在：用原记录 id，加入 update 集合
                record.setId(existing.getId());
                updateList.add(record);
            } else {
                // 不存在：生成新 id，加入 insert 集合
                record.setId(ymsOidGenerator.nextStrId());
                insertList.add(record);
            }
        }
        if (!insertList.isEmpty()) {
            billRepository.insert(insertList);
        }
        if (!updateList.isEmpty()) {
            billRepository.update(updateList);
        }

        // 合并 insert + update 两个集合，供后续挂考勤修正缺勤子表
        list.addAll(insertList);
        list.addAll(updateList);

        Map<String, List<TcmLeaveRecord>> leaveRecordMap = list.stream()
                .filter(r -> r.getEmployee_id() != null && !r.getEmployee_id().isEmpty())
                .collect(Collectors.groupingBy(TcmLeaveRecord::getEmployee_id));

        // 查询该日期+任务编码下的考勤修正数据，通过员工维度将休假记录挂到修正单的缺勤子表
        List<TcmAttendanceCorrection> correctionList = queryAttendanceCorrection(date, original, tenantId);
        List<TcmAttendanceAbsenceDataRecord> recordList = new ArrayList<>();
        for (TcmAttendanceCorrection correction : correctionList) {
            List<TcmLeaveRecord> leaveRecords = leaveRecordMap.get(correction.getEmployee_id());
            if (leaveRecords == null || leaveRecords.isEmpty()) {
                continue;
            }
            for (TcmLeaveRecord leaveRecord : leaveRecords) {
                String id = ymsOidGenerator.nextStrId();
                TcmAttendanceAbsenceDataRecord record = new TcmAttendanceAbsenceDataRecord();
                record.setId(id);
                record.setForeignerKey(correction.getId());
                record.setEntity(leaveRecord.getId());
                record.setYtenantId(tenantId);
                recordList.add(record);
            }
        }
        if (!recordList.isEmpty()) {
            billRepository.insert(recordList);
        }
        return list.size();
    }

    @Override
    public boolean checkDataByDate(String date) throws Exception {
        String tenantId = InvocationInfoProxy.getTenantid();
        List<TcmSheduleShiftsData> list = querySheduleShiftsData(date, tenantId);
        return !list.isEmpty();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateSchedule(String date) throws Exception {
        if (date == null || date.isEmpty()) {
            throw new RuntimeException("日期不能为空");
        }
        long totalStart = System.currentTimeMillis();
        AttendanceCalcImportContext.beginBulkSync();
        try {
            return doGenerateScheduleInternal(date);
        } finally {
            AttendanceCalcImportContext.endBulkSync();
            log.warn("[HXPG_MAKE] date={} step=total costMs={}", date, System.currentTimeMillis() - totalStart);
        }
    }

    private Map<String, Object> doGenerateScheduleInternal(String date) throws Exception {
        String tenantId = InvocationInfoProxy.getTenantid();

        List<TcmWaybillSheduleShiftsData> sourceList = queryWaybillData(date, tenantId);
        if (sourceList.isEmpty()) {
            throw new RuntimeException("该日期无排更数据");
        }

        Set<String> employeeIds = sourceList.stream().map(TcmWaybillSheduleShiftsData::getEmployee_id).collect(Collectors.toSet());
        Map<String, Map<String, Object>> staffMap = commonUtil.getStaffMapByCardIds(employeeIds, tenantId, commonUtil.FIELD_CODE_EMPLOYEE_ID);

        Map<String, List<TcmWaybillSheduleShiftsData>> groups = groupByDateAndEmployee(sourceList);

        int totalInput = sourceList.size();
        int totalGroups = groups.size();
        int singleGroups = 0;
        int mergedGroups = 0;
        int skippedGroups = 0;
        int skippedRecords = 0;
        int scheduleInserted = 0;
        int scheduleUpdated = 0;
        int scheduleFail = 0;
        int correctionInserted = 0;
        int correctionUpdated = 0;
        int correctionFail = 0;

        List<TcmSheduleShiftsData> scheduleInsertList = new ArrayList<>();
        List<TcmSheduleShiftsData> scheduleUpdateList = new ArrayList<>();
        List<TcmAttendanceCorrection> correctionInsertList = new ArrayList<>();
        List<TcmAttendanceCorrection> correctionUpdateList = new ArrayList<>();

        for (Map.Entry<String, List<TcmWaybillSheduleShiftsData>> entry : groups.entrySet()) {
            String key = entry.getKey();
            List<TcmWaybillSheduleShiftsData> rows = entry.getValue();

            if (rows.size() == 1) {
                singleGroups++;
                TcmWaybillSheduleShiftsData row = rows.get(0);

                TcmSheduleShiftsData scheduleExisting = queryExistingSchedule(
                        row.getDate(), row.getEmployee_id(), row.getTask_code(), tenantId);
                if (scheduleExisting != null) {
                    fillScheduleFields(scheduleExisting, row, true);
                    scheduleUpdateList.add(scheduleExisting);
                    scheduleUpdated++;
                } else {
                    scheduleInsertList.add(buildSchedule(row, true, tenantId));
                    scheduleInserted++;
                }

                TcmAttendanceCorrection existing = queryExistingCorrection(
                        row.getDate(), row.getEmployee_id(), row.getTask_code(), tenantId);
                if (existing != null) {
                    fillCorrectionFields(existing, row, null);
                    correctionUpdateList.add(existing);
                    correctionUpdated++;
                } else {
                    correctionInsertList.add(buildCorrection(row, null, tenantId));
                    correctionInserted++;
                }

            } else if (rows.size() == 2) {
                mergedGroups++;
                TcmWaybillSheduleShiftsData r1 = rows.get(0);
                TcmWaybillSheduleShiftsData r2 = rows.get(1);

                TcmWaybillSheduleShiftsData earlyRow;
                TcmWaybillSheduleShiftsData lateRow;
                if (r1.getDeparture_time() != null && r2.getFinish_time() != null
                        && r1.getDeparture_time().compareTo(r2.getFinish_time()) > 0) {
                    lateRow = r1;
                    earlyRow = r2;
                } else if (r2.getDeparture_time() != null && r1.getFinish_time() != null
                        && r2.getDeparture_time().compareTo(r1.getFinish_time()) > 0) {
                    lateRow = r2;
                    earlyRow = r1;
                } else {
                    scheduleFail += 2;
                    correctionFail += 2;
                    continue;
                }

                TcmSheduleShiftsData scheduleExisting = queryExistingSchedule(
                        earlyRow.getDate(), earlyRow.getEmployee_id(), earlyRow.getTask_code(), tenantId);
                if (scheduleExisting != null) {
                    fillScheduleFields(scheduleExisting, earlyRow, true);
                    fillScheduleFields(scheduleExisting, lateRow, false);
                    scheduleUpdateList.add(scheduleExisting);
                    scheduleUpdated++;
                } else {
                    TcmSheduleShiftsData data = buildSchedule(earlyRow, true, tenantId);
                    fillScheduleFields(data, lateRow, false);
                    scheduleInsertList.add(data);
                    scheduleInserted++;
                }

                TcmAttendanceCorrection existing = queryExistingCorrection(
                        earlyRow.getDate(), earlyRow.getEmployee_id(), earlyRow.getTask_code(), tenantId);
                if (existing != null) {
                    fillCorrectionFields(existing, earlyRow, lateRow);
                    correctionUpdateList.add(existing);
                    correctionUpdated++;
                } else {
                    correctionInsertList.add(buildCorrection(earlyRow, lateRow, tenantId));
                    correctionInserted++;
                }

            } else {
                skippedGroups++;
                skippedRecords += rows.size();
            }
        }

        if (!scheduleInsertList.isEmpty()) {
            billRepository.insert(scheduleInsertList);
        }
        if (!scheduleUpdateList.isEmpty()) {
            billRepository.update(scheduleUpdateList);
        }
        if (!correctionInsertList.isEmpty()) {
            processDate(correctionInsertList, staffMap);
            billRepository.insert(correctionInsertList);
        }
        if (!correctionUpdateList.isEmpty()) {
            processDate(correctionUpdateList, staffMap);
            billRepository.update(correctionUpdateList);
        }

        // 更新核对记录
        tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_SHEDULE_SHIFTS, true);

        // 計算
        calculationService.getAndUpdateAssessReviseByDate(date);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalInput", totalInput);
        result.put("totalGroups", totalGroups);
        result.put("singleGroups", singleGroups);
        result.put("mergedGroups", mergedGroups);
        result.put("skippedGroups", skippedGroups);
        result.put("skippedRecords", skippedRecords);
        result.put("scheduleInserted", scheduleInserted);
        result.put("scheduleUpdated", scheduleUpdated);
        result.put("scheduleFailed", scheduleFail);
        result.put("correctionInserted", correctionInserted);
        result.put("correctionUpdated", correctionUpdated);
        result.put("correctionFailed", correctionFail);
        return result;
    }

    /**
     * 计算考勤结果数据
     *
     * @param list     修正数据集合
     * @param staffMap
     */
    private void processDate(List<TcmAttendanceCorrection> list, Map<String, Map<String, Object>> staffMap) {
        for (TcmAttendanceCorrection entity : list) {
            commonUtil.processDate(entity,staffMap);

        }
    }


    private List<TcmWaybillSheduleShiftsData> queryWaybillData(String date, String tenantId) {
        LambdaQueryCondition<TcmWaybillSheduleShiftsData> condition = new LambdaQueryCondition<>(new TcmWaybillSheduleShiftsData());
        condition.eq(TcmWaybillSheduleShiftsData::getDate, date)
                .eq(TcmWaybillSheduleShiftsData::getYtenantId, tenantId)
                .eq(TcmWaybillSheduleShiftsData::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(condition);
    }

    private List<TcmWaybillSheduleShiftsData> queryWaybillData(String date,String takeCode, String tenantId) {
        LambdaQueryCondition<TcmWaybillSheduleShiftsData> condition = new LambdaQueryCondition<>(new TcmWaybillSheduleShiftsData());
        condition.eq(TcmWaybillSheduleShiftsData::getDate, date)
                .eq(TcmWaybillSheduleShiftsData::getTask_code, takeCode)
                .eq(TcmWaybillSheduleShiftsData::getYtenantId, tenantId)
                .eq(TcmWaybillSheduleShiftsData::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(condition);
    }

    private List<TcmAttendanceCorrection> queryAttendanceCorrection(String date,String takeCode, String tenantId) {
        LambdaQueryCondition<TcmAttendanceCorrection> condition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        condition.eq(TcmAttendanceCorrection::getDate, date)
                .eq(TcmAttendanceCorrection::getTask_code, takeCode)
                .eq(TcmAttendanceCorrection::getYtenantId, tenantId)
                .eq(TcmAttendanceCorrection::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(condition);
    }

    private List<TcmAttendanceAbsenceDataRecord> queryAttendanceAbsenceDataRecord(Collection<String> ids, String tenantId) {
        LambdaQueryCondition<TcmAttendanceAbsenceDataRecord> condition = new LambdaQueryCondition<>(new TcmAttendanceAbsenceDataRecord());
        condition.in(TcmAttendanceAbsenceDataRecord::getForeignerKey, ids)
                .eq(TcmAttendanceAbsenceDataRecord::getYtenantId, tenantId)
                .eq(TcmAttendanceAbsenceDataRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(condition);
    }

    private List<TcmSheduleShiftsData> querySheduleShiftsData(String date, String tenantId) {
        LambdaQueryCondition<TcmSheduleShiftsData> condition = new LambdaQueryCondition<>(new TcmSheduleShiftsData());
        condition.eq(TcmSheduleShiftsData::getDate, date)
                .eq(TcmSheduleShiftsData::getYtenantId, tenantId)
                .eq(TcmSheduleShiftsData::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(condition);
    }

    private Map<String, List<TcmWaybillSheduleShiftsData>> groupByDateAndEmployee(List<TcmWaybillSheduleShiftsData> list) {
        Map<String, List<TcmWaybillSheduleShiftsData>> groups = new LinkedHashMap<>();
        for (TcmWaybillSheduleShiftsData row : list) {
            String key = (row.getDate() != null ? row.getDate() : "") + "|"
                    + (row.getEmployee_id() != null ? row.getEmployee_id() : "");
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        return groups;
    }

    /**
     * 構建本地派更數據
     * @param row 還行派更
     * @param isFirst 是否第一更
     * @param tenantId
     * @return
     */
    private TcmSheduleShiftsData buildSchedule(TcmWaybillSheduleShiftsData row, boolean isFirst, String tenantId) {
        String id = ymsOidGenerator.nextStrId();
        TcmSheduleShiftsData entity = new TcmSheduleShiftsData();
        entity.setId(id);
        entity.setYtenantId(tenantId);
        entity.setDate(row.getDate());
        entity.setEmployee_id(row.getEmployee_id());
        entity.setTask_code(row.getTask_code());
        fillScheduleFields(entity, row, isFirst);
        return entity;
    }

    /**
     *
     * @param entity
     * @param row
     * @param isFirst
     */
    private void fillScheduleFields(TcmSheduleShiftsData entity, TcmWaybillSheduleShiftsData row, boolean isFirst) {
        entity.setWaybill_employee_id(row.getWaybill_employee_id());
        entity.setName(row.getDriver_name());
        String taskCode = row.getTask_code() == null ? "" : row.getTask_code();
        if (isFirst){
            // 任務為培訓或跟車培訓的，即任務=培訓/跟車培訓，要自動填寫发出時間10：00，收车时间18:30  ，用膳次數=1次 todo 待转参数
            if (Objects.equals(taskCode,"培訓") || Objects.equals(taskCode,"跟車培訓")){
                row.setDeparture_time(DateUtil.stringToDate(DateUtil.YYYY_MM_DD_HH_MM_SS,row.getDate() + " 10:00:00"));
                row.setFinish_time(DateUtil.stringToDate(DateUtil.YYYY_MM_DD_HH_MM_SS,row.getDate() + " 18:30:00"));
                row.setMeal_time(1);
            }
            entity.setDeparture_time(row.getDeparture_time());
            entity.setOne_early_line(row.getLine_str());
            entity.setOne_car(row.getCar_num());
            entity.setDesignated_parking_bay(row.getParking_space());
        }else {
            entity.setOne_finish_time(entity.getFinish_time());
            entity.setTwo_departure_time(row.getDeparture_time());
            entity.setTwo_early_line(row.getLine_str());
            entity.setTwo_car(row.getCar_num());

            String timeDifference = DateUtil.getTimeDifference(entity.getOne_finish_time(), entity.getTwo_departure_time());
            String timeOut = "03:00"; // todo 待转参数
            boolean isTimeOut = DateUtil.isTimeGreaterOrEqual(timeDifference, timeOut);
            int times = isTimeOut ? 1 : 0;
            entity.setShift_change(times);
        }
        entity.setFinish_time(row.getFinish_time());

        if (entity.getMeal() == null){
            entity.setMeal(row.getMeal_time());
        }else {
            int i = entity.getMeal() + Optional.ofNullable(row.getMeal_time()).orElse(0);
            entity.setMeal(i);
        }

        if (entity.getGet_out_car() == null){
            entity.setMeal(row.getMeal_time());
        }else {
            int i = entity.getMeal() + Optional.ofNullable(row.getMeal_time()).orElse(0);
            entity.setMeal(i);
        }

        entity.setGet_out_car(countParkingLotFields(row.getLine_str(), row.getGet_car(), row.getOut_car()));



    }


    /**
     * 判断郊区车次数
     * @param lineStr 路线名称
     * @param getCar 取车点
     * @param outCar 交车点
     * @return
     */
    private Integer countParkingLotFields(String lineStr, String getCar, String outCar) {
        // 任務為N線 交取車次數=0
        if (!StringUtils.isEmpty(lineStr)){
            boolean isNRoute = lineStr.startsWith("N");
            if (isNRoute){
                return 0;
            }
        }
        List<String> parkKeyWords = Arrays.asList("車場", "车场"); // todo 待转参数
        return (int) Stream.of(getCar, outCar)
                .filter(text -> text != null && !text.trim().isEmpty())
                .filter(text -> parkKeyWords.stream().anyMatch(text::contains))
                .count();
    }

    private TcmSheduleShiftsData queryExistingSchedule(String date, String employeeId, String taskCode, String tenantId) {
        LambdaQueryCondition<TcmSheduleShiftsData> condition = new LambdaQueryCondition<>(new TcmSheduleShiftsData());
        condition.eq(TcmSheduleShiftsData::getDate, date)
                .eq(TcmSheduleShiftsData::getEmployee_id, employeeId)
                .eq(TcmSheduleShiftsData::getTask_code, taskCode)
                .eq(TcmSheduleShiftsData::getYtenantId, tenantId)
                .eq(TcmSheduleShiftsData::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmSheduleShiftsData> list = billRepository.queryByClause(condition);
        return list.isEmpty() ? null : list.get(0);
    }

    private TcmAttendanceCorrection buildCorrection(TcmWaybillSheduleShiftsData earlyRow, TcmWaybillSheduleShiftsData lateRow, String tenantId) {
        String id = ymsOidGenerator.nextStrId();
        TcmAttendanceCorrection entity = new TcmAttendanceCorrection();
        entity.setId(id);
        entity.setYtenantId(tenantId);
        entity.setDate(earlyRow.getDate());
        entity.setEmployee_id(earlyRow.getEmployee_id());
        entity.setTask_code(earlyRow.getTask_code());
        fillCorrectionFields(entity, earlyRow, lateRow);
        return entity;
    }

    private void fillCorrectionFields(TcmAttendanceCorrection entity, TcmWaybillSheduleShiftsData earlyRow, TcmWaybillSheduleShiftsData lateRow) {
        entity.setWaybill_employee_id(earlyRow.getWaybill_employee_id());
        entity.setDriver_name(earlyRow.getDriver_name());
        entity.setPg_one_departure_time(earlyRow.getDeparture_time());
        entity.setPg_one_return_time(earlyRow.getFinish_time());
        entity.setJl_one_departure_time(earlyRow.getDeparture_time());
        entity.setJl_one_return_time(earlyRow.getFinish_time());
        entity.setShift_change_count(0);
        int mealCount = earlyRow.getMeal_time() == null ? 0 : earlyRow.getMeal_time();
        int vehicle_handover_count = countParkingLotFields(earlyRow.getLine_str(), earlyRow.getOut_car(), earlyRow.getGet_car());

        entity.setOne_license_plate(earlyRow.getCar_num());

        if (lateRow != null) {
            entity.setTwo_license_plate(lateRow.getCar_num());
            entity.setPg_two_departure_time(lateRow.getDeparture_time());
            entity.setPg_two_return_time(lateRow.getFinish_time());
            entity.setJl_two_departure_time(lateRow.getDeparture_time());
            entity.setJl_two_return_time(lateRow.getFinish_time());
            int lateMealCount = lateRow.getMeal_time() == null ? 0 : lateRow.getMeal_time();
            mealCount = mealCount + lateMealCount;

            vehicle_handover_count = vehicle_handover_count + countParkingLotFields(lateRow.getLine_str(),lateRow.getOut_car(), lateRow.getGet_car());

            // 上下更次数 上下更中间超过或等于3个小时，就算一次 一天上下更次数最多1次 todo 待转参数
            double hours = DateUtil.getHoursDiff(earlyRow.getFinish_time(), lateRow.getDeparture_time());
            if (hours >= 3.0) {
                entity.setShift_change_count(1);
            }

        } else {
            entity.setPg_two_departure_time(null);
            entity.setPg_two_return_time(null);
            entity.setJl_two_departure_time(null);
            entity.setJl_two_return_time(null);
        }
        entity.setVehicle_handover_count(vehicle_handover_count);
        // 一次用膳 30分钟
        entity.setMeal_count(mealCount);
        String mealDuration;
        if (mealCount == 1) {
            mealDuration = "00:30";
        } else if (mealCount == 2) {
            mealDuration = "01:00";
        } else if (mealCount == 3) {
            mealDuration = "01:30";
        } else if (mealCount == 4) {
            mealDuration = "02:00";
        } else {
            mealDuration = "00:00";
        }
        entity.setMeal_duration(mealDuration);
    }


    private TcmAttendanceCorrection queryExistingCorrection(String date, String employeeId, String taskCode, String tenantId) {
        LambdaQueryCondition<TcmAttendanceCorrection> condition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        condition.eq(TcmAttendanceCorrection::getDate, date)
                .eq(TcmAttendanceCorrection::getEmployee_id, employeeId)
                .eq(TcmAttendanceCorrection::getTask_code, taskCode)
                .eq(TcmAttendanceCorrection::getYtenantId, tenantId)
                .eq(TcmAttendanceCorrection::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmAttendanceCorrection> list = billRepository.queryByClause(condition);
        return list.isEmpty() ? null : list.get(0);
    }
}
