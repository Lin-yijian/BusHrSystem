package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSheduleShiftsData;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmWaybillSheduleShiftsData;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmScheduleGenerateService;
import com.yonyou.ucf.mdf.sample.utils.DateUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TcmScheduleGenerateServiceImpl implements TcmScheduleGenerateService {

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private YmsOidGenerator ymsOidGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateSchedule(String date) throws Exception {
        if (date == null || date.isEmpty()) {
            throw new RuntimeException("日期不能为空");
        }

        String tenantId = InvocationInfoProxy.getTenantid();

        List<TcmWaybillSheduleShiftsData> sourceList = queryWaybillData(date, tenantId);
        if (sourceList.isEmpty()) {
            throw new RuntimeException("该日期无排更数据");
        }

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
                    fillScheduleFields(scheduleExisting, row, row.getDeparture_time(), row.getFinish_time());
                    scheduleUpdateList.add(scheduleExisting);
                    scheduleUpdated++;
                } else {
                    scheduleInsertList.add(buildSchedule(row, row.getDeparture_time(), row.getFinish_time(), tenantId));
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
                    fillScheduleFields(scheduleExisting, earlyRow, earlyRow.getDeparture_time(), lateRow.getFinish_time());
                    scheduleUpdateList.add(scheduleExisting);
                    scheduleUpdated++;
                } else {
                    scheduleInsertList.add(buildSchedule(earlyRow, earlyRow.getDeparture_time(), lateRow.getFinish_time(), tenantId));
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
            processDate(correctionInsertList);
            billRepository.insert(correctionInsertList);
        }
        if (!correctionUpdateList.isEmpty()) {
            processDate(correctionUpdateList);
            billRepository.update(correctionUpdateList);
        }

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
     * @param list 修正数据集合
     */
    private void processDate(List<TcmAttendanceCorrection> list){
        for (TcmAttendanceCorrection entity : list) {
            // 一更时长 = 一更计量排更收车 - 一更计量排更发车
            String oneTimeDifference = DateUtil.getTimeDifference(entity.getJl_one_departure_time(), entity.getJl_one_return_time());
            entity.setOne_time(oneTimeDifference);
            // 二更时长 = 二更计量排更收车 - 二更计量排更发车
            String twoTimeDifference = DateUtil.getTimeDifference(entity.getJl_two_departure_time(), entity.getJl_two_return_time());
            entity.setTwo_time(twoTimeDifference);
            // 一更早退时长 = 一更计量排更收车 - 考勤一更收车
            String one_early_leave_duration = DateUtil.getTimeDifference(entity.getJl_one_departure_time(), entity.getKq_one_departure_time());
            entity.setOne_early_leave_duration(one_early_leave_duration);
            // 二更早退时长 = 二更计量排更收车 - 考勤二更收车
            String two_early_leave_duration = DateUtil.getTimeDifference(entity.getJl_two_departure_time(), entity.getKq_two_departure_time());
            entity.setTwo_early_leave_duration(two_early_leave_duration);
            // 计量有效工时 = （一更计量排更收车 - 一更计量排更发车）+ （二更计量排更收车 - 二更计量排更发车）
            entity.setJl_valid_time(DateUtil.addTimeDifferences(oneTimeDifference, twoTimeDifference));
            // 加班工时 = 计量有效工时 - 08:00
            entity.setOvertime_time(DateUtil.addTimeDifferences(entity.getJl_valid_time(), "08:00"));
        }
    }



    private List<TcmWaybillSheduleShiftsData> queryWaybillData(String date, String tenantId) {
        LambdaQueryCondition<TcmWaybillSheduleShiftsData> condition = new LambdaQueryCondition<>(new TcmWaybillSheduleShiftsData());
        condition.eq(TcmWaybillSheduleShiftsData::getDate, date)
                .eq(TcmWaybillSheduleShiftsData::getYtenantId, tenantId)
                .eq(TcmWaybillSheduleShiftsData::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
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

    private TcmSheduleShiftsData buildSchedule(TcmWaybillSheduleShiftsData row, Date departureTime, Date finishTime, String tenantId) {
        String id = ymsOidGenerator.nextStrId();
        TcmSheduleShiftsData entity = new TcmSheduleShiftsData();
        entity.setId(id);
        entity.setYtenantId(tenantId);
        entity.setDate(row.getDate());
        entity.setEmployee_id(row.getEmployee_id());
        entity.setTask_code(row.getTask_code());
        fillScheduleFields(entity, row, departureTime, finishTime);
        return entity;
    }

    private void fillScheduleFields(TcmSheduleShiftsData entity, TcmWaybillSheduleShiftsData row, Date departureTime, Date finishTime) {
        entity.setWaybill_employee_id(row.getWaybill_employee_id());
        entity.setName(row.getDriver_name());
        entity.setDeparture_time(departureTime);
        entity.setFinish_time(finishTime);
        entity.setMeal(row.getMeal_time());
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

        if (lateRow != null) {
            entity.setPg_two_departure_time(lateRow.getDeparture_time());
            entity.setPg_two_return_time(lateRow.getFinish_time());
            entity.setJl_two_departure_time(lateRow.getDeparture_time());
            entity.setJl_two_return_time(lateRow.getFinish_time());
        } else {
            entity.setPg_two_departure_time(null);
            entity.setPg_two_return_time(null);
            entity.setJl_two_departure_time(null);
            entity.setJl_two_return_time(null);
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
