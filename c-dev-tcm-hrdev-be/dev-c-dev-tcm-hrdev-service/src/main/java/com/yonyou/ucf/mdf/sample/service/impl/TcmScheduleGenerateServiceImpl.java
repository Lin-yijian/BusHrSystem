package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.aop.SyncLog;
import com.yonyou.ucf.mdf.sample.bill.entity.*;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.enums.TaskTypeEnum;
import com.yonyou.ucf.mdf.sample.enums.params.ParamKey;
import com.yonyou.ucf.mdf.sample.enums.params.TcmRosteringParamKeyEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCalculationService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmParamManageService;
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

    @Autowired
    private TcmParamManageService tcmParamManageService;


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
            CommonUtil.fillInsertAudit(insertList);
            billRepository.insert(insertList);
        }
        if (!updateList.isEmpty()) {
            CommonUtil.fillUpdateAudit(updateList);
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
            CommonUtil.fillInsertAudit(recordList);
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

    /**
     *【两阶段日志】使用预创建的同步日志ID生成排更，finally 中以 REQUIRES_NEW 回填日志，
     * 即使主事务回滚日志仍保留。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateSchedule(String date, String syncLogId) throws Exception {
        if (date == null || date.isEmpty()) {
            throw new RuntimeException("日期不能为空");
        }
        long totalStart = System.currentTimeMillis();
        AttendanceCalcImportContext.beginBulkSync();
        Throwable captured = null;
        Map<String, Object> result = null;
        try {
            result = doGenerateScheduleInternal(date);
            return result;
        } catch (Throwable t) {
            captured = t;
            if (t instanceof Exception) throw (Exception) t;
            throw (Error) t;
        } finally {
            AttendanceCalcImportContext.endBulkSync();
            String remarkEx = (captured == null) ? buildScheduleSuccessRemark(result) : null;
            try {
                tcmSyncLogService.finishSyncLog(syncLogId, captured, remarkEx);
            } catch (Exception finishEx) {
                log.error("[generateSchedule(syncLogId)] 回填同步日志失败 syncLogId={}", syncLogId, finishEx);
            }
            log.warn("[HXPG_MAKE] date={} step=total costMs={}", date, System.currentTimeMillis() - totalStart);
        }
    }

    /** 成功备注：同步成功，排更新增 X 条、更新 Y 条，修正新增 M 条、更新 N 条，假期工作新增 P 条、更新 Q 条 */
    private String buildScheduleSuccessRemark(Map<String, Object> result) {
        if (result == null) {
            return TcmSyncLogServiceImpl.remarkSuccess();
        }
        return "同步成功，本地排更新增 " + result.getOrDefault("scheduleInserted", 0)
                + " 条、更新 " + result.getOrDefault("scheduleUpdated", 0)
                + " 条，修正新增 " + result.getOrDefault("correctionInserted", 0)
                + " 条、更新 " + result.getOrDefault("correctionUpdated", 0)
                + " 条，假期工作新增 " + result.getOrDefault("holidayInserted", 0)
                + " 条、更新 " + result.getOrDefault("holidayUpdated", 0) + " 条";
    }

    private Map<String, Object> doGenerateScheduleInternal(String date) throws Exception {
        String tenantId = InvocationInfoProxy.getTenantid();

        List<TcmWaybillSheduleShiftsData> sourceList = queryWaybillData(date, tenantId);
        if (sourceList.isEmpty()) {
            throw new RuntimeException("该日期无排更数据");
        }

        Set<String> employeeIds = sourceList.stream().map(TcmWaybillSheduleShiftsData::getEmployee_id).collect(Collectors.toSet());
        Map<String, Map<String, Object>> staffMap = commonUtil.getStaffMapByCardIds(employeeIds, tenantId, commonUtil.FIELD_CODE_EMPLOYEE_ID);

        // 循环外一次性查询 ROSTERING 模块全部参数（含培训任务固定值），避免逐条查库
        Map<String, String> rosterParams = tcmParamManageService.queryModuleParamMap(ModuleEnum.TCM_SHEDULE_SHIFTS);
        TrainingParamConfig trainingConfig = buildTrainingParamConfig(rosterParams);

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
        int holidayInserted = 0;
        int holidayUpdated = 0;

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
                    fillScheduleFields(scheduleExisting, row, true, trainingConfig);
                    scheduleUpdateList.add(scheduleExisting);
                    scheduleUpdated++;
                } else {
                    scheduleInsertList.add(buildSchedule(row, true, tenantId, trainingConfig));
                    scheduleInserted++;
                }

                TcmAttendanceCorrection existing = queryExistingCorrection(
                        row.getDate(), row.getEmployee_id(), row.getTask_code(), tenantId);
                if (existing != null) {
                    fillCorrectionFields(existing, row, null, trainingConfig);
                    correctionUpdateList.add(existing);
                    correctionUpdated++;
                } else {
                    correctionInsertList.add(buildCorrection(row, null, tenantId, trainingConfig));
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
                    fillScheduleFields(scheduleExisting, earlyRow, true, trainingConfig);
                    fillScheduleFields(scheduleExisting, lateRow, false, trainingConfig);
                    scheduleUpdateList.add(scheduleExisting);
                    scheduleUpdated++;
                } else {
                    TcmSheduleShiftsData data = buildSchedule(earlyRow, true, tenantId, trainingConfig);
                    fillScheduleFields(data, lateRow, false, trainingConfig);
                    scheduleInsertList.add(data);
                    scheduleInserted++;
                }

                TcmAttendanceCorrection existing = queryExistingCorrection(
                        earlyRow.getDate(), earlyRow.getEmployee_id(), earlyRow.getTask_code(), tenantId);
                if (existing != null) {
                    fillCorrectionFields(existing, earlyRow, lateRow, trainingConfig);
                    correctionUpdateList.add(existing);
                    correctionUpdated++;
                } else {
                    correctionInsertList.add(buildCorrection(earlyRow, lateRow, tenantId, trainingConfig));
                    correctionInserted++;
                }

            } else {
                skippedGroups++;
                skippedRecords += rows.size();
            }
        }

        if (!scheduleInsertList.isEmpty()) {
            CommonUtil.fillInsertAudit(scheduleInsertList);
            billRepository.insert(scheduleInsertList);
        }
        if (!scheduleUpdateList.isEmpty()) {
            CommonUtil.fillUpdateAudit(scheduleUpdateList);
            billRepository.update(scheduleUpdateList);
        }
        if (!correctionInsertList.isEmpty()) {
            processDate(correctionInsertList, staffMap);
            CommonUtil.fillInsertAudit(correctionInsertList);
            billRepository.insert(correctionInsertList);
        }
        if (!correctionUpdateList.isEmpty()) {
            processDate(correctionUpdateList, staffMap);
            CommonUtil.fillUpdateAudit(correctionUpdateList);
            billRepository.update(correctionUpdateList);
        }

        // 按照生成修正后的数据对应生成车长假期工作记录（须在 processDate 之后，取已计算的有效计量工时）
        List<TcmAttendanceCorrection> mergedCorrections = new ArrayList<>(correctionInsertList);
        mergedCorrections.addAll(correctionUpdateList);
        int[] holidayCounts = upsertDriverHolidayWorkRecords(date, mergedCorrections, tenantId);
        holidayInserted = holidayCounts[0];
        holidayUpdated = holidayCounts[1];

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
        result.put("holidayInserted", holidayInserted);
        result.put("holidayUpdated", holidayUpdated);
        return result;
    }

    /**
     * 按照生成修正后的数据对应生成車長假期工作記錄（TcmDriverHolidayWorkRecord）。
     * <ul>
     *   <li>日期、工號、姓名、澳巴工號從修正數據複製；</li>
     *   <li>is_weekend_work 默認 true、weekend_work_compensation 默認 "1"、is_forced_leave_work 默認 false；</li>
     *   <li>有效總工時 = 有效計量工時(jl_valid_time) - 用膳時長(meal_duration)，均為 HH:mm 字符串；</li>
     *   <li>以 date+employee_id 為唯一鍵：當日已存在則更新（複製字段與工時同步刷新），否則新增。</li>
     * </ul>
     *
     * @param date        排更日期（yyyy-MM-dd）
     * @param corrections 本輪生成的修正數據（新增 + 更新合併）
     * @param tenantId    租戶 id
     * @return int[]{新增數, 更新數}
     */
    private int[] upsertDriverHolidayWorkRecords(String date, List<TcmAttendanceCorrection> corrections, String tenantId) {
        if (corrections == null || corrections.isEmpty()) {
            return new int[]{0, 0};
        }
        // 同一員工去重（按 employee_id，先出現者優先）
        Map<String, TcmAttendanceCorrection> correctionMap = new LinkedHashMap<>();
        for (TcmAttendanceCorrection correction : corrections) {
            if (correction != null && correction.getEmployee_id() != null) {
                correctionMap.putIfAbsent(correction.getEmployee_id(), correction);
            }
        }
        if (correctionMap.isEmpty()) {
            return new int[]{0, 0};
        }

        // 一次查詢當日已有假期工作記錄，按 employee_id 建索引，避免逐條查庫
        LambdaQueryCondition<TcmDriverHolidayWorkRecord> condition = new LambdaQueryCondition<>(new TcmDriverHolidayWorkRecord());
        condition.eq(TcmDriverHolidayWorkRecord::getDate, date)
                .eq(TcmDriverHolidayWorkRecord::getYtenantId, tenantId)
                .eq(TcmDriverHolidayWorkRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        Map<String, TcmDriverHolidayWorkRecord> existingMap = new LinkedHashMap<>();
        for (TcmDriverHolidayWorkRecord record : billRepository.queryByClause(condition)) {
            if (record.getEmployee_id() != null) {
                existingMap.putIfAbsent(record.getEmployee_id(), record);
            }
        }

        List<TcmDriverHolidayWorkRecord> insertList = new ArrayList<>();
        List<TcmDriverHolidayWorkRecord> updateList = new ArrayList<>();
        for (TcmAttendanceCorrection correction : correctionMap.values()) {
            TcmDriverHolidayWorkRecord existing = existingMap.get(correction.getEmployee_id());
            boolean isNew = existing == null;
            TcmDriverHolidayWorkRecord record = isNew ? new TcmDriverHolidayWorkRecord() : existing;
            if (isNew) {
                record.setId(ymsOidGenerator.nextStrId());
                record.setYtenantId(tenantId);
            }
            // 複製基礎字段
            record.setDate(correction.getDate());
            record.setEmployee_id(correction.getEmployee_id());
            record.setDriver_name(correction.getDriver_name());
            record.setWaybill_employee_id(correction.getWaybill_employee_id());
            // 默認值
            record.setIs_weekend_work(Boolean.TRUE);
            record.setWeekend_work_compensation("1");
            record.setIs_forced_leave_work(Boolean.FALSE);
            // 有效總工時 = 有效計量工時 - 用膳時長（subtractTimeDifferences 對空值/非法值按 00:00 處理）
            record.setEffective_total_work_hours(DateUtil.subtractTimeDifferences(
                    correction.getJl_valid_time(), correction.getMeal_duration()));
            if (isNew) {
                insertList.add(record);
            } else {
                updateList.add(record);
            }
        }
        if (!insertList.isEmpty()) {
            CommonUtil.fillInsertAudit(insertList);
            billRepository.insert(insertList);
        }
        if (!updateList.isEmpty()) {
            CommonUtil.fillUpdateAudit(updateList);
            billRepository.update(updateList);
        }
        return new int[]{insertList.size(), updateList.size()};
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
    private TcmSheduleShiftsData buildSchedule(TcmWaybillSheduleShiftsData row, boolean isFirst, String tenantId,
                                                TrainingParamConfig trainingConfig) {
        String id = ymsOidGenerator.nextStrId();
        TcmSheduleShiftsData entity = new TcmSheduleShiftsData();
        entity.setId(id);
        entity.setYtenantId(tenantId);
        entity.setDate(row.getDate());
        entity.setEmployee_id(row.getEmployee_id());
        entity.setTask_code(row.getTask_code());
        fillScheduleFields(entity, row, isFirst, trainingConfig);
        return entity;
    }

    /**
     *
     * @param entity
     * @param row
     * @param isFirst
     */
    private void fillScheduleFields(TcmSheduleShiftsData entity, TcmWaybillSheduleShiftsData row, boolean isFirst,
                                    TrainingParamConfig trainingConfig) {
        entity.setWaybill_employee_id(row.getWaybill_employee_id());
        entity.setName(row.getDriver_name());
        String taskCode = row.getTask_code() == null ? "" : row.getTask_code();
        if (isFirst){
            // 任務為培訓的，按參數管理配置自動填寫發車/收車時間、用膳次數
            if (taskCode.contains(trainingConfig.taskCodeTraining)){
                row.setDeparture_time(combineDateAndTime(row.getDate(), trainingConfig.departureTime));
                row.setFinish_time(combineDateAndTime(row.getDate(), trainingConfig.returnTime));
                row.setMeal_time(trainingConfig.mealCount);
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
            String timeOut = trainingConfig.shiftIntervalTime;
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

    private TcmAttendanceCorrection buildCorrection(TcmWaybillSheduleShiftsData earlyRow, TcmWaybillSheduleShiftsData lateRow, String tenantId, TrainingParamConfig trainingConfig) {
        String id = ymsOidGenerator.nextStrId();
        TcmAttendanceCorrection entity = new TcmAttendanceCorrection();
        entity.setId(id);
        entity.setYtenantId(tenantId);
        entity.setDate(earlyRow.getDate());
        entity.setEmployee_id(earlyRow.getEmployee_id());
        entity.setTask_code(earlyRow.getTask_code());
        fillCorrectionFields(entity, earlyRow, lateRow, trainingConfig);
        return entity;
    }

    private void fillCorrectionFields(TcmAttendanceCorrection entity, TcmWaybillSheduleShiftsData earlyRow, TcmWaybillSheduleShiftsData lateRow, TrainingParamConfig trainingConfig) {
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

        // 培训/其他培训：計糧排更一更發車/收車、用膳次数、用膳时长 取参数管理(ROSTERING)配置
        String taskCode = earlyRow.getTask_code();
        if (taskCode.contains(trainingConfig.taskCodeTraining)) {
            entity.setJl_one_departure_time(combineDateAndTime(earlyRow.getDate(), trainingConfig.departureTime));
            entity.setJl_one_return_time(combineDateAndTime(earlyRow.getDate(), trainingConfig.returnTime));
            entity.setJl_two_departure_time(null);
            entity.setJl_two_return_time(null);
            entity.setMeal_count(trainingConfig.mealCount);
            entity.setMeal_duration(trainingConfig.mealDuration);
        }
    }

    /**
     * 培训任务固定值配置（来源：TCMParamManage / ROSTERING 模块，循环外一次性解析）
     */
    private static class TrainingParamConfig {
        /** 計糧排更一更發車时间 HH:mm */
        final String departureTime;
        /** 計糧排更一更收車时间 HH:mm */
        final String returnTime;
        /** 用膳次数 */
        final int mealCount;
        /** 用膳时长 HH:mm */
        final String mealDuration;
        /** 培训关键字 */
        final String taskCodeTraining;
        /** 上下更時間間隔 HH:mm */
        final String shiftIntervalTime;

        TrainingParamConfig(String departureTime, String returnTime, int mealCount, String mealDuration,
                            String taskCodeTraining, String shiftIntervalTime) {
            this.departureTime = departureTime;
            this.returnTime = returnTime;
            this.mealCount = mealCount;
            this.mealDuration = mealDuration;
            this.taskCodeTraining = taskCodeTraining;
            this.shiftIntervalTime = shiftIntervalTime;
        }
    }

    /**
     * 循环外一次性解析培训参数配置；缺配置/格式非法直接抛错（fail-fast，避免写一半数据）。
     */
    private TrainingParamConfig buildTrainingParamConfig(Map<String, String> params) {
        String departureTime = requireRosterParam(params, TcmRosteringParamKeyEnum.TRAINING_DEPARTURE_TIME);
        String returnTime = requireRosterParam(params, TcmRosteringParamKeyEnum.TRAINING_RETURN_TIME);
        // checkType=INT:0-99 已保证是范围内整数，直接解析
        int mealCount = Integer.parseInt(requireRosterParam(params, TcmRosteringParamKeyEnum.TRAINING_MEAL_COUNT));
        String mealDuration = requireRosterParam(params, TcmRosteringParamKeyEnum.TRAINING_MEAL_DURATION);
        String taskCodeTraining = requireRosterParam(params, TcmRosteringParamKeyEnum.TASK_CODE_TRAINING);
        String shiftIntervalTime = requireRosterParam(params, TcmRosteringParamKeyEnum.SHIFT_INTERVAL_TIME);
        return new TrainingParamConfig(departureTime, returnTime, mealCount, mealDuration,
                taskCodeTraining, shiftIntervalTime);
    }

    /**
     * 从模块参数Map中取值；缺失抛出统一提示，并按枚举 checkType 做格式校验
     */
    private String requireRosterParam(Map<String, String> params, TcmRosteringParamKeyEnum keyEnum) {
        String code = keyEnum.getCode();
        String value = params == null ? null : params.get(code);
        if (StringUtils.isBlank(value)) {
            throw new RuntimeException("配置缺失：" + keyEnum.getName() + "[" + code + "]，請至【參數管理】菜單補錄");
        }
        // 按枚举 checkType 做格式校验（非法抛"配置非法：..."）
        return ParamKey.checkValue(keyEnum, value);
    }

    /** 将日期(yyyy-MM-dd)与时间(HH:mm)合并为 Date；解析失败返回 null */
    private Date combineDateAndTime(String date, String time) {
        if (StringUtils.isBlank(date) || StringUtils.isBlank(time)) {
            return null;
        }
        try {
            return DateUtil.stringToDate(DateUtil.YYYY_MM_DD_HH_MM, date.trim() + " " + time.trim());
        } catch (Exception e) {
            log.warn("[HXPG_MAKE] 合并日期时间失败 date={} time={}", date, time, e);
            return null;
        }
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
