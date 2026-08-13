package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverHolidayWorkRecord;
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

@Slf4j
@Service
public class AttendanceCorrectionSyncServiceImpl implements AttendanceCorrectionSyncService {

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
            commonUtil.processDate(correction);
            if (isNew) {
                insertList.add(correction);
            } else if (!updateList.contains(correction)) {
                updateList.add(correction);
            }
        }
        if (!insertList.isEmpty()) {
            billRepository.insert(insertList);
        }
        if (!updateList.isEmpty()) {
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
            for (TcmAttendanceCorrection c : list) {
                c.setPg_one_departure_time(null);
                c.setPg_one_return_time(null);
                c.setPg_two_departure_time(null);
                c.setPg_two_return_time(null);
                commonUtil.processDate(c);
            }
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
        String tenantId = InvocationInfoProxy.getTenantid();
        Set<String> dates = new HashSet<>();
        Set<String> staffIds = new HashSet<>();
        for (TcmReserveCaptainAttendanceRecord r : reserves) {
            if (r == null || StringUtils.isBlank(r.getDate()) || StringUtils.isBlank(r.getDriver_no())) {
                continue;
            }
            dates.add(r.getDate());
            staffIds.add(r.getDriver_no());
        }
        if (dates.isEmpty() || staffIds.isEmpty()) {
            return;
        }

        Map<String, TcmAttendanceCorrection> existingMap = loadCorrectionMap(dates, staffIds, tenantId);
        List<TcmAttendanceCorrection> insertList = new ArrayList<>();
        List<TcmAttendanceCorrection> updateList = new ArrayList<>();

        for (TcmReserveCaptainAttendanceRecord reserve : reserves) {
            if (reserve == null || StringUtils.isBlank(reserve.getDate()) || StringUtils.isBlank(reserve.getDriver_no())) {
                continue;
            }
            String key = buildKey(reserve.getDate(), reserve.getDriver_no(), reserve.getTask());
            TcmAttendanceCorrection correction = existingMap.get(key);
            boolean isNew = false;
            if (correction == null) {
                correction = new TcmAttendanceCorrection();
                correction.setId(ymsOidGenerator.nextStrId());
                correction.setYtenantId(tenantId);
                correction.setDate(reserve.getDate());
                correction.setEmployee_id(reserve.getDriver_no());
                correction.setTask_code(reserve.getTask());
                isNew = true;
                existingMap.put(key, correction);
            }

            applyReserveToCorrection(correction, reserve);
            commonUtil.processDate(correction);
            if (isNew) {
                insertList.add(correction);
            } else if (!updateList.contains(correction)) {
                updateList.add(correction);
            }
        }
        if (!insertList.isEmpty()) {
            billRepository.insert(insertList);
        }
        if (!updateList.isEmpty()) {
            billRepository.update(updateList);
        }
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
            billRepository.update(updateList);
        }
    }

    private void applyScheduleToCorrection(TcmAttendanceCorrection correction, TcmSheduleShiftsData schedule) {
        correction.setWaybill_employee_id(schedule.getWaybill_employee_id());
        correction.setDriver_name(schedule.getName());
        // 仅更新排更侧(pg_*)，不覆盖计粮上下更(jl_*)
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

    private void applyReserveToCorrection(TcmAttendanceCorrection correction, TcmReserveCaptainAttendanceRecord reserve) {
        if (StringUtils.isNotBlank(reserve.getWaybill_employee_id())) {
            correction.setWaybill_employee_id(reserve.getWaybill_employee_id());
        }
        if (StringUtils.isNotBlank(reserve.getName())) {
            correction.setDriver_name(reserve.getName());
        }
        Date outTime = combineDateAndTime(reserve.getDate(), reserve.getOut_time());
        Date backTime = combineDateAndTime(reserve.getDate(), reserve.getBack_time());
        if (isPmShift(reserve.getType())) {
            correction.setJl_two_departure_time(outTime);
            correction.setJl_two_return_time(backTime);
        } else {
            correction.setJl_one_departure_time(outTime);
            correction.setJl_one_return_time(backTime);
        }
        if (StringUtils.isNotBlank(reserve.getType())) {
            correction.setSubstitute_shift_type(reserve.getType());
        }
        if (reserve.getEating_frequency() != null) {
            int mealCount = reserve.getEating_frequency();
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
        if (reserve.getCount() != null) {
            correction.setVehicle_handover_count(reserve.getCount());
        }
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
        LambdaQueryCondition<TcmAttendanceCorrection> condition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        condition.in(TcmAttendanceCorrection::getDate, dates)
                .in(TcmAttendanceCorrection::getEmployee_id, staffIds)
                .eq(TcmAttendanceCorrection::getYtenantId, tenantId)
                .eq(TcmAttendanceCorrection::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmAttendanceCorrection> list = billRepository.queryByClause(condition);
        return list == null ? Collections.emptyList() : list;
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
        return (date != null ? date : "") + "|"
                + (employeeId != null ? employeeId : "") + "|"
                + (taskCode != null ? taskCode : "");
    }

    private boolean isPmShift(String type) {
        if (StringUtils.isBlank(type)) {
            return false;
        }
        String t = type.trim().toUpperCase();
        return t.contains("晚") || t.contains("PM") || t.contains("P.M");
    }

    private Date combineDateAndTime(String date, String time) {
        if (StringUtils.isBlank(date) || StringUtils.isBlank(time)) {
            return null;
        }
        String t = time.trim();
        try {
            if (t.length() == 5) {
                return DateUtil.stringToDate(DateUtil.YYYY_MM_DD_HH_MM, date + " " + t);
            }
            if (t.length() == 8) {
                return DateUtil.stringToDate(DateUtil.YYYY_MM_DD_HH_MM_SS, date + " " + t);
            }
            if (t.contains("-") || t.contains("/")) {
                return DateUtil.stringToDate(DateUtil.YYYY_MM_DD_HH_MM, t.replace('/', '-'));
            }
            return DateUtil.stringToDate(DateUtil.YYYY_MM_DD_HH_MM, date + " " + t);
        } catch (Exception e) {
            log.warn("解析后备车长时间失败 date={}, time={}", date, time, e);
            return null;
        }
    }
}
