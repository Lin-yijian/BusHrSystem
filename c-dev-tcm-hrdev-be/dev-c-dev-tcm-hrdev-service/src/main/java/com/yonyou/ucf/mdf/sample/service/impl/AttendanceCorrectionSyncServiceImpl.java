package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAbsenceDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmBusFaultDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmControlCenterLog;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverHolidayWorkRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverOvertimeRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmFuelGasDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmReserveCaptainAttendanceRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSheduleShiftsData;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.AttendanceCorrectionSyncService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.DateUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AttendanceCorrectionSyncServiceImpl implements AttendanceCorrectionSyncService {

    /** 上下更：一更收车与二更发车间隔 >= 3 小时计 1 次 */
    private static final double SHIFT_CHANGE_HOURS = 3.0;
    private static final String TASK_CODE_TRAINING = "培训";
    private static final String TASK_CODE_OTHER_TRAINING = "其他培训";

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private YmsOidGenerator ymsOidGenerator;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private TcmAttendanceCorrectionService attendanceCorrectionService;

    @Override
    public void syncFromSchedule(TcmSheduleShiftsData schedule) {
        if (schedule == null) {
            return;
        }
        batchSyncFromSchedules(Collections.singletonList(schedule));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSyncFromSchedules(List<TcmSheduleShiftsData> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        Set<String> dates = new HashSet<>();
        Set<String> staffIds = new HashSet<>();
        for (TcmSheduleShiftsData s : schedules) {
            if (s == null || StringUtils.isBlank(s.getDate()) || StringUtils.isBlank(s.getEmployee_id())) {
                continue;
            }
            dates.add(s.getDate());
            staffIds.add(s.getEmployee_id());
        }
        if (dates.isEmpty() || staffIds.isEmpty()) {
            return;
        }

        Map<String, Map<String, Object>> staffMap = commonUtil.getStaffMapByCardIds(staffIds, tenantId, commonUtil.FIELD_CODE_EMPLOYEE_ID);
        Map<String, TcmAttendanceCorrection> existingMap = loadCorrectionMap(dates, staffIds, tenantId);
        List<TcmAttendanceCorrection> insertList = new ArrayList<>();
        List<TcmAttendanceCorrection> updateList = new ArrayList<>();

        for (TcmSheduleShiftsData schedule : schedules) {
            if (schedule == null || StringUtils.isBlank(schedule.getDate()) || StringUtils.isBlank(schedule.getEmployee_id())) {
                continue;
            }
            String key = buildKey(schedule.getDate(), schedule.getEmployee_id(), schedule.getTask_code());
            String fallbackKey = buildKey(schedule.getDate(), schedule.getEmployee_id(), null);
            TcmAttendanceCorrection correction = existingMap.get(key);
            if (correction == null) {
                correction = existingMap.get(fallbackKey);
            }
            boolean isNew = false;
            if (correction == null) {
                correction = new TcmAttendanceCorrection();
                correction.setId(ymsOidGenerator.nextStrId());
                correction.setYtenantId(tenantId);
                correction.setDate(schedule.getDate());
                correction.setEmployee_id(schedule.getEmployee_id());
                correction.setTask_code(schedule.getTask_code());
                isNew = true;
                existingMap.put(key, correction);
                existingMap.putIfAbsent(fallbackKey, correction);
            }
            correction.setPg_one_departure_time(schedule.getDeparture_time());
            correction.setPg_one_return_time(schedule.getOne_finish_time());
            correction.setPg_two_departure_time(schedule.getTwo_departure_time());
            correction.setPg_two_return_time(schedule.getFinish_time());
            correction.setMeal_count(schedule.getMeal());
            correction.setVehicle_handover_count(schedule.getGet_out_car());
            correction.setShift_change_count(schedule.getShift_change());
            applyScheduleToCorrection(correction, schedule);
            if (isNew) {
                insertList.add(correction);
            } else if (!updateList.contains(correction)) {
                updateList.add(correction);
            }
        }
        List<TcmAttendanceCorrection> touched = new ArrayList<>();
        touched.addAll(insertList);
        touched.addAll(updateList);
        applyJlPriority(touched, dates, staffIds, tenantId, staffMap);
        if (!insertList.isEmpty()) {
            CommonUtil.fillInsertAudit(insertList);
            billRepository.insert(insertList);
        }
        if (!updateList.isEmpty()) {
            CommonUtil.fillUpdateAudit(updateList);
            billRepository.update(updateList);
        }
    }

    @Override
    public void afterScheduleDeleted(TcmSheduleShiftsData deleted) {
        if (deleted == null || StringUtils.isBlank(deleted.getDate()) || StringUtils.isBlank(deleted.getEmployee_id())) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        List<TcmSheduleShiftsData> remain = querySchedules(deleted.getDate(), deleted.getEmployee_id(), tenantId);
        if (remain.isEmpty()) {
            List<TcmAttendanceCorrection> list = attendanceCorrectionService.getByDateAndStaffId(
                    deleted.getDate(), deleted.getEmployee_id());
            if (list == null || list.isEmpty()) {
                return;
            }
            Set<String> idSet = list.stream().map(TcmAttendanceCorrection::getEmployee_id).collect(Collectors.toSet());
            Map<String, Map<String, Object>> staffMap = commonUtil.getStaffMapByCardIds(idSet, tenantId, commonUtil.FIELD_CODE_EMPLOYEE_ID);
            for (TcmAttendanceCorrection c : list) {
                c.setPg_one_departure_time(null);
                c.setPg_one_return_time(null);
                c.setPg_two_departure_time(null);
                c.setPg_two_return_time(null);
            }
            applyJlPriority(list, Collections.singleton(deleted.getDate()),
                    Collections.singleton(deleted.getEmployee_id()), tenantId, staffMap);
            CommonUtil.fillUpdateAudit(list);
            billRepository.update(list);
            return;
        }
        syncFromSchedule(remain.get(0));
    }

    @Override
    public void syncFromReserve(TcmReserveCaptainAttendanceRecord reserve) {
        if (reserve == null) {
            return;
        }
        batchSyncFromReserves(Collections.singletonList(reserve));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSyncFromReserves(List<TcmReserveCaptainAttendanceRecord> reserves) {
        if (reserves == null || reserves.isEmpty()) {
            return;
        }
        syncJlByEmployeeDays(collectEmployeeDaysFromReserves(reserves), true);
    }

    @Override
    public void syncFromOvertime(TcmDriverOvertimeRecord overtime) {
        if (overtime == null) {
            return;
        }
        batchSyncFromOvertimes(Collections.singletonList(overtime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSyncFromOvertimes(List<TcmDriverOvertimeRecord> overtimes) {
        if (overtimes == null || overtimes.isEmpty()) {
            return;
        }
        syncJlByEmployeeDays(collectEmployeeDaysFromOvertimes(overtimes), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshJlForEmployeeDay(String date, String employeeId) {
        if (StringUtils.isBlank(date) || StringUtils.isBlank(employeeId)) {
            return;
        }
        EmpDay empDay = new EmpDay(date, employeeId, null);
        syncJlByEmployeeDays(Collections.singletonList(empDay), false);
    }

    @Override
    public void syncFromHolidayWork(TcmDriverHolidayWorkRecord holiday) {
        if (holiday == null) {
            return;
        }
        batchSyncFromHolidays(Collections.singletonList(holiday));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSyncFromHolidays(List<TcmDriverHolidayWorkRecord> holidays) {
        if (holidays == null || holidays.isEmpty()) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        Set<String> dates = new HashSet<>();
        Set<String> staffIds = new HashSet<>();
        for (TcmDriverHolidayWorkRecord h : holidays) {
            if (h == null || StringUtils.isBlank(h.getDate()) || StringUtils.isBlank(h.getEmployee_id())) {
                continue;
            }
            dates.add(h.getDate());
            staffIds.add(h.getEmployee_id());
        }
        if (dates.isEmpty() || staffIds.isEmpty()) {
            return;
        }

        List<TcmAttendanceCorrection> list = queryCorrections(dates, staffIds, tenantId);
        if (list.isEmpty()) {
            log.warn("假期工作批量同步跳过：无对应修正记录, dates={}, staffSize={}", dates, staffIds.size());
            return;
        }
        Map<String, TcmDriverHolidayWorkRecord> holidayMap = new HashMap<>();
        for (TcmDriverHolidayWorkRecord h : holidays) {
            if (h != null && StringUtils.isNotBlank(h.getDate()) && StringUtils.isNotBlank(h.getEmployee_id())) {
                holidayMap.put(buildKey(h.getDate(), h.getEmployee_id(), null), h);
            }
        }
        List<TcmAttendanceCorrection> updateList = new ArrayList<>();
        for (TcmAttendanceCorrection c : list) {
            TcmDriverHolidayWorkRecord h = holidayMap.get(buildKey(c.getDate(), c.getEmployee_id(), null));
            if (h == null || h.getIs_weekend_work() == null) {
                continue;
            }
            c.setIs_working_on_weekly_holiday(h.getIs_weekend_work());
            updateList.add(c);
        }
        if (!updateList.isEmpty()) {
            CommonUtil.fillUpdateAudit(updateList);
            billRepository.update(updateList);
        }
    }

    @Override
    public void syncFromFuelGas(String date, String employeeId) {
        if (StringUtils.isBlank(date) || StringUtils.isBlank(employeeId)) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        List<TcmAttendanceCorrection> list = queryCorrections(
                DateUtil.dateQueryVariants(date), Collections.singleton(employeeId), tenantId);
        if (list.isEmpty()) {
            log.warn("入油加气同步跳过：无对应修正记录, date={}, employeeId={}", date, employeeId);
            return;
        }
        int[] counts = sumFuelCounts(queryFuels(DateUtil.dateQueryVariants(date),
                Collections.singleton(employeeId), tenantId));
        for (TcmAttendanceCorrection c : list) {
            c.setRefuel_count(counts[0]);
            c.setGas_refill_count(counts[1]);
        }
        CommonUtil.fillUpdateAudit(list);
        billRepository.update(list);
    }

    @Override
    public void syncExceptionSources(String date, String employeeId) {
        if (StringUtils.isBlank(date) || StringUtils.isBlank(employeeId)) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        List<TcmAttendanceCorrection> list = queryCorrections(
                DateUtil.dateQueryVariants(date), Collections.singleton(employeeId), tenantId);
        if (list.isEmpty()) {
            log.warn("异常来源同步跳过：无对应修正记录, date={}, employeeId={}", date, employeeId);
            return;
        }
        Set<String> dateVars = DateUtil.dateQueryVariants(date);
        Set<String> staffIds = Collections.singleton(employeeId);
        String busFault = joinTexts(collectFaultDescs(queryBusFaults(dateVars, staffIds, tenantId)));
        String controlLog = joinTexts(collectLogContents(queryControlLogs(dateVars, staffIds, tenantId)));
        List<TcmAbsenceDataRecord> absences = queryAbsences(dateVars, staffIds, tenantId);
        String absenceCategory = joinTexts(collectAbsenceCategories(absences));
        String absenceReason = joinTexts(collectAbsenceReasons(absences));
        int absenceCount = absences.size();
        for (TcmAttendanceCorrection c : list) {
            c.setBus_fault(busFault);
            c.setControl_center_log(controlLog);
            c.setAbsence_category(absenceCategory);
            c.setAbsence_reason(absenceReason);
            c.setAbsence_record_count(absenceCount);
        }
        CommonUtil.fillUpdateAudit(list);
        billRepository.update(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshCorrectionsBeforeCalc(String dateStr, String employeeCode, boolean singleDate) {
        if (StringUtils.isBlank(dateStr)) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        Set<String> staffFilter = null;
        if (StringUtils.isNotBlank(employeeCode)) {
            Map<String, String> idByCode = commonUtil.getStaffByCodes(
                    Collections.singletonList(employeeCode), tenantId);
            String staffId = idByCode.get(employeeCode);
            if (StringUtils.isBlank(staffId)) {
                log.warn("考勤计算前回写跳过：无法解析员工, code={}", employeeCode);
                return;
            }
            staffFilter = Collections.singleton(staffId);
        }
        List<TcmAttendanceCorrection> list = queryCorrectionsForCalc(dateStr, staffFilter, singleDate, tenantId);
        if (list.isEmpty()) {
            return;
        }
        long t0 = System.currentTimeMillis();
        applyFuelAndException(list, tenantId);
        Set<String> staffIds = list.stream()
                .map(TcmAttendanceCorrection::getEmployee_id)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, Map<String, Object>> staffMap = commonUtil.getStaffMapByStaffIds(staffIds, tenantId);
        for (TcmAttendanceCorrection c : list) {
            commonUtil.processDate(c, staffMap);
        }
        CommonUtil.fillUpdateAudit(list);
        billRepository.update(list);
        log.warn("[KQ_CALC] date={} step=refreshCorrectionsBeforeCalc size={} costMs={}",
                dateStr, list.size(), System.currentTimeMillis() - t0);
    }

    private void applyScheduleToCorrection(TcmAttendanceCorrection correction, TcmSheduleShiftsData schedule) {
        correction.setWaybill_employee_id(schedule.getWaybill_employee_id());
        correction.setDriver_name(schedule.getName());
        correction.setPg_one_departure_time(schedule.getDeparture_time());
        correction.setPg_one_return_time(schedule.getFinish_time());
        if (StringUtils.isNotBlank(schedule.getTask_code())) {
            correction.setTask_code(schedule.getTask_code());
        }

        Integer mealCount = schedule.getMeal() == null ? 0 : schedule.getMeal();
        correction.setMeal_count(mealCount);
        if (mealCount == 1) {
            correction.setMeal_duration("00:30");
        } else if (mealCount >= 2) {
            correction.setMeal_duration("01:00");
        } else {
            correction.setMeal_duration("00:00");
        }

        int handover = 0;
        if (Boolean.TRUE.equals(schedule.getMacau_car_park())) {
            handover++;
        }
        if (Boolean.TRUE.equals(schedule.getTaipa_car_park())) {
            handover++;
        }
        if (handover > 0) {
            correction.setVehicle_handover_count(handover);
        }
    }

    /**
     * 计粮发收车取值顺序：排更 → 后备车长 → 超时工作记录（后者覆盖前者）。海信考勤不写入计粮。
     */
    private void applyJlPriority(List<TcmAttendanceCorrection> corrections, Set<String> dates, Set<String> staffIds,
                                 String tenantId, Map<String, Map<String, Object>> staffMap) {
        if (corrections == null || corrections.isEmpty()) {
            return;
        }
        Map<String, List<TcmReserveCaptainAttendanceRecord>> reserveMap = groupReserves(
                queryReserves(dates, staffIds, tenantId));
        Map<String, List<TcmDriverOvertimeRecord>> overtimeMap = groupOvertimes(
                queryOvertimes(dates, staffIds, tenantId));
        for (TcmAttendanceCorrection c : corrections) {
            if (c == null) {
                continue;
            }
            String key = empDateKey(c.getDate(), c.getEmployee_id());
            List<TcmReserveCaptainAttendanceRecord> reserves = reserveMap.getOrDefault(key, Collections.emptyList());
            List<TcmDriverOvertimeRecord> overtimes = overtimeMap.getOrDefault(key, Collections.emptyList());
            if (!isTrainingTask(c.getTask_code())) {
                copyPgToJl(c);
            }
            if (!reserves.isEmpty()) {
                applyReserveMeta(c, reserves);
                applyReserveJl(c, reserves);
            }
            if (!overtimes.isEmpty()) {
                applyOvertimeJl(c, overtimes);
            }
            recalcShiftChange(c);
            commonUtil.processDate(c, staffMap);
        }
    }

    private void syncJlByEmployeeDays(List<EmpDay> empDays, boolean createIfMissing) {
        if (empDays == null || empDays.isEmpty()) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        Set<String> dates = new HashSet<>();
        Set<String> staffIds = new HashSet<>();
        Map<String, EmpDay> unique = new LinkedHashMap<>();
        for (EmpDay d : empDays) {
            if (d == null || StringUtils.isBlank(d.date) || StringUtils.isBlank(d.employeeId)) {
                continue;
            }
            String key = empDateKey(d.date, d.employeeId);
            EmpDay prev = unique.get(key);
            if (prev == null) {
                unique.put(key, d);
            } else if (StringUtils.isBlank(prev.hintTask) && StringUtils.isNotBlank(d.hintTask)) {
                unique.put(key, d);
            }
            dates.add(d.date);
            staffIds.add(d.employeeId);
        }
        if (dates.isEmpty() || staffIds.isEmpty()) {
            return;
        }

        Map<String, Map<String, Object>> staffMap = commonUtil.getStaffMapByCardIds(
                staffIds, tenantId, commonUtil.FIELD_CODE_EMPLOYEE_ID);
        Map<String, List<TcmAttendanceCorrection>> byEmpDate = loadCorrectionsByEmpDate(dates, staffIds, tenantId);
        List<TcmAttendanceCorrection> insertList = new ArrayList<>();
        List<TcmAttendanceCorrection> updateList = new ArrayList<>();
        List<TcmAttendanceCorrection> touched = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (EmpDay empDay : unique.values()) {
            List<TcmAttendanceCorrection> existing = byEmpDate.get(empDateKey(empDay.date, empDay.employeeId));
            TcmAttendanceCorrection correction = pickCorrection(existing, empDay.hintTask);
            boolean isNew = false;
            if (correction == null) {
                if (!createIfMissing) {
                    continue;
                }
                correction = new TcmAttendanceCorrection();
                correction.setId(ymsOidGenerator.nextStrId());
                correction.setYtenantId(tenantId);
                String normalized = DateUtil.extractDateString(empDay.date);
                correction.setDate(normalized != null ? normalized : empDay.date);
                correction.setEmployee_id(empDay.employeeId);
                if (StringUtils.isNotBlank(empDay.hintTask)) {
                    correction.setTask_code(empDay.hintTask);
                }
                isNew = true;
                byEmpDate.computeIfAbsent(empDateKey(empDay.date, empDay.employeeId), k -> new ArrayList<>())
                        .add(correction);
            }
            if (isNew) {
                insertList.add(correction);
            } else if (correction.getId() != null && seenIds.add(correction.getId()) && !updateList.contains(correction)) {
                updateList.add(correction);
            }
            if (!touched.contains(correction)) {
                touched.add(correction);
            }
        }
        applyJlPriority(touched, dates, staffIds, tenantId, staffMap);
        if (!insertList.isEmpty()) {
            CommonUtil.fillInsertAudit(insertList);
            billRepository.insert(insertList);
        }
        if (!updateList.isEmpty()) {
            CommonUtil.fillUpdateAudit(updateList);
            billRepository.update(updateList);
        }
    }

    private List<EmpDay> collectEmployeeDaysFromReserves(List<TcmReserveCaptainAttendanceRecord> reserves) {
        List<EmpDay> list = new ArrayList<>();
        for (TcmReserveCaptainAttendanceRecord r : reserves) {
            if (r == null || StringUtils.isBlank(r.getDate()) || StringUtils.isBlank(r.getDriver_no())) {
                continue;
            }
            // 匹配已有排更行：用 AM/PM 类型（如「早更后备」），不用安排任务
            list.add(new EmpDay(r.getDate(), r.getDriver_no(), r.getType()));
        }
        return list;
    }

    private List<EmpDay> collectEmployeeDaysFromOvertimes(List<TcmDriverOvertimeRecord> overtimes) {
        List<EmpDay> list = new ArrayList<>();
        for (TcmDriverOvertimeRecord r : overtimes) {
            if (r == null || StringUtils.isBlank(r.getOvertime_date()) || StringUtils.isBlank(r.getEmployee_id())) {
                continue;
            }
            list.add(new EmpDay(r.getOvertime_date(), r.getEmployee_id(), r.getTask_no()));
        }
        return list;
    }

    private Map<String, List<TcmAttendanceCorrection>> loadCorrectionsByEmpDate(
            Set<String> dates, Set<String> staffIds, String tenantId) {
        Map<String, List<TcmAttendanceCorrection>> map = new LinkedHashMap<>();
        for (TcmAttendanceCorrection c : queryCorrections(dates, staffIds, tenantId)) {
            map.computeIfAbsent(empDateKey(c.getDate(), c.getEmployee_id()), k -> new ArrayList<>()).add(c);
        }
        return map;
    }

    /**
     * 优先精确/包含匹配任务代号，其次取带排更时间的行，避免按安排任务另插一条。
     */
    private TcmAttendanceCorrection pickCorrection(List<TcmAttendanceCorrection> list, String hintTask) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        if (StringUtils.isNotBlank(hintTask)) {
            for (TcmAttendanceCorrection c : list) {
                if (hintTask.equals(c.getTask_code())) {
                    return c;
                }
            }
            for (TcmAttendanceCorrection c : list) {
                if (StringUtils.isNotBlank(c.getTask_code()) && c.getTask_code().contains(hintTask)) {
                    return c;
                }
            }
        }
        for (TcmAttendanceCorrection c : list) {
            if (hasPgTime(c)) {
                return c;
            }
        }
        return list.get(0);
    }

    private boolean hasPgTime(TcmAttendanceCorrection c) {
        return c.getPg_one_departure_time() != null || c.getPg_one_return_time() != null
                || c.getPg_two_departure_time() != null || c.getPg_two_return_time() != null;
    }

    private void copyPgToJl(TcmAttendanceCorrection c) {
        c.setJl_one_departure_time(c.getPg_one_departure_time());
        c.setJl_one_return_time(c.getPg_one_return_time());
        c.setJl_two_departure_time(c.getPg_two_departure_time());
        c.setJl_two_return_time(c.getPg_two_return_time());
    }

    private boolean isTrainingTask(String taskCode) {
        return TASK_CODE_TRAINING.equals(taskCode) || TASK_CODE_OTHER_TRAINING.equals(taskCode);
    }

    private void applyReserveMeta(TcmAttendanceCorrection correction, List<TcmReserveCaptainAttendanceRecord> reserves) {
        int mealCount = 0;
        int handover = 0;
        for (TcmReserveCaptainAttendanceRecord reserve : reserves) {
            if (StringUtils.isNotBlank(reserve.getWaybill_employee_id())) {
                correction.setWaybill_employee_id(reserve.getWaybill_employee_id());
            }
            if (StringUtils.isNotBlank(reserve.getName())) {
                correction.setDriver_name(reserve.getName());
            }
            if (StringUtils.isNotBlank(reserve.getType())) {
                correction.setSubstitute_shift_type(reserve.getType());
            }
            if (reserve.getEating_frequency() != null) {
                mealCount = Math.max(mealCount, reserve.getEating_frequency());
            }
            if (reserve.getCount() != null) {
                handover += reserve.getCount();
            }
        }
        if (mealCount > 0) {
            correction.setMeal_count(mealCount);
            if (mealCount == 1) {
                correction.setMeal_duration("00:30");
            } else if (mealCount == 2) {
                correction.setMeal_duration("01:00");
            } else if (mealCount == 3) {
                correction.setMeal_duration("01:30");
            } else if (mealCount == 4) {
                correction.setMeal_duration("02:00");
            } else {
                correction.setMeal_duration("00:00");
            }
        }
        if (handover > 0) {
            correction.setVehicle_handover_count(handover);
        }
    }

    /**
     * 1 条：发/收车 → 计粮一更；多条：最早发/收 → 一更，最晚发/收 → 二更。
     */
    private void applyReserveJl(TcmAttendanceCorrection correction, List<TcmReserveCaptainAttendanceRecord> reserves) {
        List<Date> outs = new ArrayList<>();
        List<Date> backs = new ArrayList<>();
        for (TcmReserveCaptainAttendanceRecord r : reserves) {
            Date out = combineDateAndTime(r.getDate(), r.getOut_time());
            Date back = combineDateAndTime(r.getDate(), r.getBack_time());
            if (out != null) {
                outs.add(out);
            }
            if (back != null) {
                backs.add(back);
            }
        }
        Date minOut = minDate(outs);
        Date minBack = adjustOvernight(minOut, minDate(backs));
        Date maxOut = maxDate(outs);
        Date maxBack = adjustOvernight(maxOut, maxDate(backs));
        correction.setJl_one_departure_time(minOut);
        correction.setJl_one_return_time(minBack);
        if (reserves.size() <= 1) {
            correction.setJl_two_departure_time(null);
            correction.setJl_two_return_time(null);
        } else {
            correction.setJl_two_departure_time(maxOut);
            correction.setJl_two_return_time(maxBack);
        }
    }

    /**
     * 按编定上下班窗口分组：仅一更取最早实际上班/最晚实际下班；有二更则早窗口→一更、晚窗口→二更。
     */
    private void applyOvertimeJl(TcmAttendanceCorrection correction, List<TcmDriverOvertimeRecord> records) {
        String date = DateUtil.extractDateString(correction.getDate());
        Map<String, List<TcmDriverOvertimeRecord>> groups = new LinkedHashMap<>();
        List<TcmDriverOvertimeRecord> unscheduled = new ArrayList<>();
        for (TcmDriverOvertimeRecord r : records) {
            String key = shiftWindowKey(r);
            if (key == null) {
                unscheduled.add(r);
            } else {
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }
        }
        if (groups.isEmpty() && !unscheduled.isEmpty()) {
            groups.put("_all", unscheduled);
        } else if (!unscheduled.isEmpty() && !groups.isEmpty()) {
            List<TcmDriverOvertimeRecord> first = groups.values().iterator().next();
            first.addAll(unscheduled);
        }
        if (groups.isEmpty()) {
            return;
        }
        List<List<TcmDriverOvertimeRecord>> sorted = new ArrayList<>(groups.values());
        sorted.sort(Comparator.comparing(g -> shiftSortTime(date, g), Comparator.nullsLast(Date::compareTo)));
        ShiftTimes first = extractShiftTimes(date, sorted.get(0));
        if (sorted.size() == 1) {
            correction.setJl_one_departure_time(first.departure);
            correction.setJl_one_return_time(first.returned);
            correction.setJl_two_departure_time(null);
            correction.setJl_two_return_time(null);
            return;
        }
        ShiftTimes last = extractShiftTimes(date, sorted.get(sorted.size() - 1));
        correction.setJl_one_departure_time(first.departure);
        correction.setJl_one_return_time(first.returned);
        correction.setJl_two_departure_time(last.departure);
        correction.setJl_two_return_time(last.returned);
    }

    private String shiftWindowKey(TcmDriverOvertimeRecord r) {
        String start = normalizeTimeOnly(r.getSchedule_start_time());
        String end = normalizeTimeOnly(r.getSchedule_end_time());
        if (start == null && end == null) {
            return null;
        }
        return (start == null ? "" : start) + "|" + (end == null ? "" : end);
    }

    private Date shiftSortTime(String date, List<TcmDriverOvertimeRecord> group) {
        Date minSched = null;
        Date minActual = null;
        for (TcmDriverOvertimeRecord r : group) {
            Date sched = combineDateAndTime(date != null ? date : r.getOvertime_date(), r.getSchedule_start_time());
            Date actual = combineDateAndTime(date != null ? date : r.getOvertime_date(), r.getActual_start_time());
            if (sched != null && (minSched == null || sched.before(minSched))) {
                minSched = sched;
            }
            if (actual != null && (minActual == null || actual.before(minActual))) {
                minActual = actual;
            }
        }
        return minSched != null ? minSched : minActual;
    }

    private ShiftTimes extractShiftTimes(String date, List<TcmDriverOvertimeRecord> group) {
        List<Date> starts = new ArrayList<>();
        List<Date> ends = new ArrayList<>();
        for (TcmDriverOvertimeRecord r : group) {
            String d = date != null ? date : DateUtil.extractDateString(r.getOvertime_date());
            Date start = combineDateAndTime(d, r.getActual_start_time());
            Date end = combineDateAndTime(d, r.getActual_end_time());
            if (start != null) {
                starts.add(start);
            }
            if (end != null) {
                ends.add(end);
            }
        }
        Date dep = minDate(starts);
        Date ret = maxDate(ends);
        ret = adjustOvernight(dep, ret);
        return new ShiftTimes(dep, ret);
    }

    private void recalcShiftChange(TcmAttendanceCorrection c) {
        Date oneReturn = c.getJl_one_return_time();
        Date twoDepart = c.getJl_two_departure_time();
        if (oneReturn == null || twoDepart == null) {
            c.setShift_change_count(0);
            return;
        }
        double hours = DateUtil.getHoursDiff(oneReturn, twoDepart);
        c.setShift_change_count(hours >= SHIFT_CHANGE_HOURS ? 1 : 0);
    }

    private Map<String, List<TcmReserveCaptainAttendanceRecord>> groupReserves(
            List<TcmReserveCaptainAttendanceRecord> list) {
        Map<String, List<TcmReserveCaptainAttendanceRecord>> map = new HashMap<>();
        for (TcmReserveCaptainAttendanceRecord r : list) {
            map.computeIfAbsent(empDateKey(r.getDate(), r.getDriver_no()), k -> new ArrayList<>()).add(r);
        }
        return map;
    }

    private Map<String, List<TcmDriverOvertimeRecord>> groupOvertimes(List<TcmDriverOvertimeRecord> list) {
        Map<String, List<TcmDriverOvertimeRecord>> map = new HashMap<>();
        for (TcmDriverOvertimeRecord r : list) {
            map.computeIfAbsent(empDateKey(r.getOvertime_date(), r.getEmployee_id()), k -> new ArrayList<>()).add(r);
        }
        return map;
    }

    private List<TcmReserveCaptainAttendanceRecord> queryReserves(Set<String> dates, Set<String> staffIds, String tenantId) {
        if (dates == null || dates.isEmpty() || staffIds == null || staffIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryCondition<TcmReserveCaptainAttendanceRecord> condition =
                new LambdaQueryCondition<>(new TcmReserveCaptainAttendanceRecord());
        condition.in(TcmReserveCaptainAttendanceRecord::getDate, DateUtil.dateQueryVariants(dates))
                .in(TcmReserveCaptainAttendanceRecord::getDriver_no, staffIds)
                .eq(TcmReserveCaptainAttendanceRecord::getYtenantId, tenantId)
                .eq(TcmReserveCaptainAttendanceRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmReserveCaptainAttendanceRecord> list = billRepository.queryByClause(condition);
        return list == null ? Collections.emptyList() : list;
    }

    private List<TcmDriverOvertimeRecord> queryOvertimes(Set<String> dates, Set<String> staffIds, String tenantId) {
        if (dates == null || dates.isEmpty() || staffIds == null || staffIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryCondition<TcmDriverOvertimeRecord> condition =
                new LambdaQueryCondition<>(new TcmDriverOvertimeRecord());
        condition.in(TcmDriverOvertimeRecord::getOvertime_date, DateUtil.dateQueryVariants(dates))
                .in(TcmDriverOvertimeRecord::getEmployee_id, staffIds)
                .eq(TcmDriverOvertimeRecord::getYtenantId, tenantId)
                .eq(TcmDriverOvertimeRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmDriverOvertimeRecord> list = billRepository.queryByClause(condition);
        return list == null ? Collections.emptyList() : list;
    }

    private Date minDate(List<Date> dates) {
        Date min = null;
        for (Date d : dates) {
            if (d != null && (min == null || d.before(min))) {
                min = d;
            }
        }
        return min;
    }

    private Date maxDate(List<Date> dates) {
        Date max = null;
        for (Date d : dates) {
            if (d != null && (max == null || d.after(max))) {
                max = d;
            }
        }
        return max;
    }

    private Date adjustOvernight(Date start, Date end) {
        if (start != null && end != null && !end.after(start)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(end);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            return cal.getTime();
        }
        return end;
    }

    private Map<String, TcmAttendanceCorrection> loadCorrectionMap(Set<String> dates, Set<String> staffIds, String tenantId) {
        List<TcmAttendanceCorrection> list = queryCorrections(dates, staffIds, tenantId);
        Map<String, TcmAttendanceCorrection> map = new HashMap<>();
        for (TcmAttendanceCorrection c : list) {
            String fullKey = buildKey(c.getDate(), c.getEmployee_id(), c.getTask_code());
            String empKey = buildKey(c.getDate(), c.getEmployee_id(), null);
            map.put(fullKey, c);
            map.putIfAbsent(empKey, c);
        }
        return map;
    }

    private List<TcmAttendanceCorrection> queryCorrections(Set<String> dates, Set<String> staffIds, String tenantId) {
        if (dates == null || dates.isEmpty() || staffIds == null || staffIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryCondition<TcmAttendanceCorrection> condition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        condition.in(TcmAttendanceCorrection::getDate, DateUtil.dateQueryVariants(dates))
                .in(TcmAttendanceCorrection::getEmployee_id, staffIds)
                .eq(TcmAttendanceCorrection::getYtenantId, tenantId)
                .eq(TcmAttendanceCorrection::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmAttendanceCorrection> list = billRepository.queryByClause(condition);
        return list == null ? Collections.emptyList() : list;
    }

    private List<TcmAttendanceCorrection> queryCorrectionsForCalc(String dateStr, Set<String> staffIds,
                                                                  boolean singleDate, String tenantId) {
        LambdaQueryCondition<TcmAttendanceCorrection> condition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        condition.eq(TcmAttendanceCorrection::getYtenantId, tenantId)
                .eq(TcmAttendanceCorrection::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        if (singleDate || (staffIds != null && !staffIds.isEmpty())) {
            condition.in(TcmAttendanceCorrection::getDate, DateUtil.dateQueryVariants(dateStr));
        } else {
            String[] monthRange = monthRange(dateStr);
            condition.ge(TcmAttendanceCorrection::getDate, monthRange[0])
                    .lt(TcmAttendanceCorrection::getDate, monthRange[1]);
        }
        if (staffIds != null && !staffIds.isEmpty()) {
            condition.in(TcmAttendanceCorrection::getEmployee_id, staffIds);
        }
        List<TcmAttendanceCorrection> list = billRepository.queryByClause(condition);
        return list == null ? Collections.emptyList() : list;
    }

    private void applyFuelAndException(List<TcmAttendanceCorrection> list, String tenantId) {
        Set<String> dates = new HashSet<>();
        Set<String> staffIds = new HashSet<>();
        for (TcmAttendanceCorrection c : list) {
            if (c == null) {
                continue;
            }
            String d = DateUtil.extractDateString(c.getDate());
            if (d != null) {
                dates.add(d);
            }
            if (StringUtils.isNotBlank(c.getEmployee_id())) {
                staffIds.add(c.getEmployee_id());
            }
        }
        if (dates.isEmpty() || staffIds.isEmpty()) {
            return;
        }
        Set<String> dateVars = DateUtil.dateQueryVariants(dates);
        Map<String, int[]> fuelMap = new HashMap<>();
        for (TcmFuelGasDataRecord rec : queryFuels(dateVars, staffIds, tenantId)) {
            String key = empDateKey(rec.getAttendance_date(), rec.getEmployee_id());
            int[] counts = fuelMap.computeIfAbsent(key, k -> new int[]{0, 0});
            counts[0] += nz(rec.getRefuel_count());
            counts[1] += nz(rec.getGas_count());
        }
        Map<String, List<String>> faultMap = new HashMap<>();
        for (TcmBusFaultDataRecord rec : queryBusFaults(dateVars, staffIds, tenantId)) {
            faultMap.computeIfAbsent(empDateKey(rec.getReport_date(), rec.getEmployee_id()), k -> new ArrayList<>())
                    .add(rec.getFault_detail_desc());
        }
        Map<String, List<String>> logMap = new HashMap<>();
        for (TcmControlCenterLog rec : queryControlLogs(dateVars, staffIds, tenantId)) {
            logMap.computeIfAbsent(empDateKey(rec.getDate(), rec.getEmployee_id()), k -> new ArrayList<>())
                    .add(rec.getContent());
        }
        Map<String, List<TcmAbsenceDataRecord>> absenceMap = new HashMap<>();
        for (TcmAbsenceDataRecord rec : queryAbsences(dateVars, staffIds, tenantId)) {
            absenceMap.computeIfAbsent(empDateKey(rec.getDate(), rec.getEmployee_id()), k -> new ArrayList<>())
                    .add(rec);
        }
        for (TcmAttendanceCorrection c : list) {
            String key = empDateKey(c.getDate(), c.getEmployee_id());
            int[] counts = fuelMap.getOrDefault(key, new int[]{0, 0});
            c.setRefuel_count(counts[0]);
            c.setGas_refill_count(counts[1]);
            c.setBus_fault(joinTexts(faultMap.get(key)));
            c.setControl_center_log(joinTexts(logMap.get(key)));
            List<TcmAbsenceDataRecord> absences = absenceMap.getOrDefault(key, Collections.emptyList());
            c.setAbsence_category(joinTexts(collectAbsenceCategories(absences)));
            c.setAbsence_reason(joinTexts(collectAbsenceReasons(absences)));
            c.setAbsence_record_count(absences.size());
        }
    }

    private List<TcmFuelGasDataRecord> queryFuels(Set<String> dates, Set<String> staffIds, String tenantId) {
        if (dates == null || dates.isEmpty() || staffIds == null || staffIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryCondition<TcmFuelGasDataRecord> condition = new LambdaQueryCondition<>(new TcmFuelGasDataRecord());
        condition.in(TcmFuelGasDataRecord::getAttendance_date, dates)
                .in(TcmFuelGasDataRecord::getEmployee_id, staffIds)
                .eq(TcmFuelGasDataRecord::getYtenantId, tenantId)
                .eq(TcmFuelGasDataRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmFuelGasDataRecord> list = billRepository.queryByClause(condition);
        return list == null ? Collections.emptyList() : list;
    }

    private List<TcmBusFaultDataRecord> queryBusFaults(Set<String> dates, Set<String> staffIds, String tenantId) {
        if (dates == null || dates.isEmpty() || staffIds == null || staffIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryCondition<TcmBusFaultDataRecord> condition = new LambdaQueryCondition<>(new TcmBusFaultDataRecord());
        condition.in(TcmBusFaultDataRecord::getReport_date, dates)
                .in(TcmBusFaultDataRecord::getEmployee_id, staffIds)
                .eq(TcmBusFaultDataRecord::getYtenantId, tenantId)
                .eq(TcmBusFaultDataRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmBusFaultDataRecord> list = billRepository.queryByClause(condition);
        return list == null ? Collections.emptyList() : list;
    }

    private List<TcmControlCenterLog> queryControlLogs(Set<String> dates, Set<String> staffIds, String tenantId) {
        if (dates == null || dates.isEmpty() || staffIds == null || staffIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryCondition<TcmControlCenterLog> condition = new LambdaQueryCondition<>(new TcmControlCenterLog());
        condition.in(TcmControlCenterLog::getDate, dates)
                .in(TcmControlCenterLog::getEmployee_id, staffIds)
                .eq(TcmControlCenterLog::getYtenantId, tenantId)
                .eq(TcmControlCenterLog::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmControlCenterLog> list = billRepository.queryByClause(condition);
        return list == null ? Collections.emptyList() : list;
    }

    private List<TcmAbsenceDataRecord> queryAbsences(Set<String> dates, Set<String> staffIds, String tenantId) {
        if (dates == null || dates.isEmpty() || staffIds == null || staffIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryCondition<TcmAbsenceDataRecord> condition = new LambdaQueryCondition<>(new TcmAbsenceDataRecord());
        condition.in(TcmAbsenceDataRecord::getDate, dates)
                .in(TcmAbsenceDataRecord::getEmployee_id, staffIds)
                .eq(TcmAbsenceDataRecord::getYtenantId, tenantId)
                .eq(TcmAbsenceDataRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmAbsenceDataRecord> list = billRepository.queryByClause(condition);
        return list == null ? Collections.emptyList() : list;
    }

    private int[] sumFuelCounts(List<TcmFuelGasDataRecord> records) {
        int refuel = 0;
        int gas = 0;
        for (TcmFuelGasDataRecord rec : records) {
            refuel += nz(rec.getRefuel_count());
            gas += nz(rec.getGas_count());
        }
        return new int[]{refuel, gas};
    }

    private List<String> collectFaultDescs(List<TcmBusFaultDataRecord> records) {
        List<String> texts = new ArrayList<>();
        for (TcmBusFaultDataRecord rec : records) {
            texts.add(rec.getFault_detail_desc());
        }
        return texts;
    }

    private List<String> collectLogContents(List<TcmControlCenterLog> records) {
        List<String> texts = new ArrayList<>();
        for (TcmControlCenterLog rec : records) {
            texts.add(rec.getContent());
        }
        return texts;
    }

    private List<String> collectAbsenceCategories(List<TcmAbsenceDataRecord> records) {
        List<String> texts = new ArrayList<>();
        for (TcmAbsenceDataRecord rec : records) {
            texts.add(rec.getAbsence_category());
        }
        return texts;
    }

    private List<String> collectAbsenceReasons(List<TcmAbsenceDataRecord> records) {
        List<String> texts = new ArrayList<>();
        for (TcmAbsenceDataRecord rec : records) {
            texts.add(rec.getReason());
        }
        return texts;
    }

    private String joinTexts(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return "";
        }
        return texts.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.joining("；"));
    }

    private String empDateKey(String date, String employeeId) {
        String d = DateUtil.extractDateString(date);
        if (d == null) {
            d = date != null ? date : "";
        }
        return d + "|" + (employeeId != null ? employeeId : "");
    }

    private int nz(Integer n) {
        return n == null ? 0 : n;
    }

    private String[] monthRange(String dateStr) {
        String normalized = DateUtil.extractDateString(dateStr);
        if (normalized == null) {
            return new String[]{dateStr, dateStr};
        }
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(normalized);
            java.time.LocalDate start = d.withDayOfMonth(1);
            return new String[]{start.toString(), start.plusMonths(1).toString()};
        } catch (Exception e) {
            return new String[]{normalized, normalized};
        }
    }

    private List<TcmSheduleShiftsData> querySchedules(String date, String employeeId, String tenantId) {
        LambdaQueryCondition<TcmSheduleShiftsData> condition = new LambdaQueryCondition<>(new TcmSheduleShiftsData());
        condition.eq(TcmSheduleShiftsData::getDate, date)
                .eq(TcmSheduleShiftsData::getEmployee_id, employeeId)
                .eq(TcmSheduleShiftsData::getYtenantId, tenantId)
                .eq(TcmSheduleShiftsData::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(condition);
    }

    private String buildKey(String date, String employeeId, String taskCode) {
        String d = DateUtil.extractDateString(date);
        if (d == null) {
            d = date != null ? date : "";
        }
        return d + "|"
                + (employeeId != null ? employeeId : "") + "|"
                + (taskCode != null ? taskCode : "");
    }

    private Date combineDateAndTime(String date, String time) {
        if (StringUtils.isBlank(date) || StringUtils.isBlank(time)) {
            return null;
        }
        String day = DateUtil.extractDateString(date);
        if (day == null) {
            day = date.trim();
        }
        String t = padTime(time.trim());
        try {
            if (t.contains("-") || t.contains("/")) {
                String normalized = t.replace('/', '-');
                if (normalized.length() <= 16) {
                    return DateUtil.stringToDate(DateUtil.YYYY_MM_DD_HH_MM, normalized);
                }
                return DateUtil.stringToDate(DateUtil.YYYY_MM_DD_HH_MM_SS, normalized.substring(0, 19));
            }
            if (t.length() <= 5) {
                return DateUtil.stringToDate(DateUtil.YYYY_MM_DD_HH_MM, day + " " + t);
            }
            return DateUtil.stringToDate(DateUtil.YYYY_MM_DD_HH_MM_SS, day + " " + t);
        } catch (Exception e) {
            log.warn("解析发收车时间失败 date={}, time={}", date, time, e);
            return null;
        }
    }

    /** 7:32 → 07:32，供 HH:mm 解析 */
    private String padTime(String t) {
        if (t.contains("-") || t.contains("/")) {
            return t;
        }
        String[] parts = t.split(":");
        if (parts.length < 2) {
            return t;
        }
        String h = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
        String m = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
        if (parts.length >= 3) {
            String s = parts[2].length() == 1 ? "0" + parts[2] : parts[2];
            if (s.length() > 2) {
                s = s.substring(0, 2);
            }
            return h + ":" + m + ":" + s;
        }
        return h + ":" + m;
    }

    private String normalizeTimeOnly(String time) {
        if (StringUtils.isBlank(time)) {
            return null;
        }
        Date parsed = combineDateAndTime("2000-01-01", time);
        if (parsed == null) {
            String padded = padTime(time.trim());
            return padded.length() >= 5 ? padded.substring(0, 5) : padded;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(parsed);
        return String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    private static final class EmpDay {
        final String date;
        final String employeeId;
        final String hintTask;

        EmpDay(String date, String employeeId, String hintTask) {
            this.date = date;
            this.employeeId = employeeId;
            this.hintTask = hintTask;
        }
    }

    private static final class ShiftTimes {
        final Date departure;
        final Date returned;

        ShiftTimes(Date departure, Date returned) {
            this.departure = departure;
            this.returned = returned;
        }
    }
}
