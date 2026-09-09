package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.BusinessException;
import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.SQLParameter;
import com.yonyou.iuap.yms.param.condtition.SFunction;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.*;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ucf.mdf.sample.service.TcmDriverHolidayWorkRecordService;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.DateUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TcmAttendanceCorrectionServiceImpl implements TcmAttendanceCorrectionService {


    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private TcmDriverHolidayWorkRecordService driverHolidayWorkRecordService;

    @Autowired
    private YmsOidGenerator ymsOidGenerator;

    public static final String TABLE_NAME = "tcm_attendance_correction";

    @Override
    public List<TcmAttendanceCorrection> getByDateAndStaffId(String date, String staffId) {
        return getByDateAndField(date, staffId, TcmAttendanceCorrection::getEmployee_id);
    }


    @Override
    public List<TcmAttendanceCorrection> getByDateAndWaybillEmployeeId(String date, String waybillEmployeeId) {
        return getByDateAndField(date, waybillEmployeeId, TcmAttendanceCorrection::getWaybill_employee_id);
    }

    /**
     * 查询
     * @param date 。
     * @param fieldValue 。
     * @param fieldGetter 。
     * @return 。
     */
    private List<TcmAttendanceCorrection> getByDateAndField(String date, String fieldValue,
                                                            SFunction<TcmAttendanceCorrection, String> fieldGetter) {
        if (StringUtils.isBlank(fieldValue) || StringUtils.isBlank(date)) {
            return Collections.emptyList();
        }

        String tenantId = InvocationInfoProxy.getTenantid();
        LambdaQueryCondition<TcmAttendanceCorrection> routeCondition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        routeCondition.eq(TcmAttendanceCorrection::getDate, date)
                .eq(fieldGetter, fieldValue)
                .eq(TcmAttendanceCorrection::getYtenantId, tenantId)
                .eq(TcmAttendanceCorrection::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(routeCondition);
    }

    @Override
    public List<TcmAttendanceCorrection> getByDateListAndStaffIds(String date, Collection<String> staffIdList) {

        if (staffIdList == null) {
            return Collections.emptyList();
        }


        String tenantId = InvocationInfoProxy.getTenantid();
        LambdaQueryCondition<TcmAttendanceCorrection> routeCondition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        routeCondition.eq(TcmAttendanceCorrection::getDate, date)
                .in(TcmAttendanceCorrection::getEmployee_id, staffIdList)
                .eq(TcmAttendanceCorrection::getYtenantId, tenantId)
                .eq(TcmAttendanceCorrection::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(routeCondition);
    }

    @Override
    public Map<String, String> getByDateListAndStaffIds(Collection<String> dateList, Collection<String> staffIdList) {
        if (staffIdList == null || dateList == null || staffIdList.isEmpty() || dateList.isEmpty()) {
            return Collections.emptyMap();
        }

        String tenantId = InvocationInfoProxy.getTenantid();
        LambdaQueryCondition<TcmAttendanceCorrection> routeCondition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        routeCondition.in(TcmAttendanceCorrection::getDate, dateList)
                .in(TcmAttendanceCorrection::getEmployee_id, staffIdList)
                .eq(TcmAttendanceCorrection::getYtenantId, tenantId)
                .eq(TcmAttendanceCorrection::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());

        List<TcmAttendanceCorrection> list = billRepository.queryByClause(routeCondition);

        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }

        return list.stream()
                .collect(Collectors.toMap(
                        item -> buildKey(item.getDate(), item.getEmployee_id()),
                        item -> item.getId() != null ? item.getId() : "",
                        (existing, replacement) -> replacement
                ));
    }


    @Override
    public void updateLateStatus(List<String> ids, String status, String type) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("99", "请先选择数据");
        }

        if (!Arrays.asList("LATE", "LEAVE", "ALL").contains(type)) {
            throw new BusinessException("99", "无效的操作类型");
        }

        String tenantId = InvocationInfoProxy.getTenantid();
        LambdaQueryCondition<TcmAttendanceCorrection> condition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        condition.in(TcmAttendanceCorrection::getId, ids)
                .eq(TcmAttendanceCorrection::getYtenantId, tenantId)
                .eq(TcmAttendanceCorrection::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());

        List<TcmAttendanceCorrection> list = billRepository.queryByClause(condition);

        if (list.isEmpty()) {
            throw new BusinessException("99", "未找到对应的数据");
        }

        String approvedStatus = BaseEnum.AC_CHECK_STATUS_0.getCode();
        String normalStatus = BaseEnum.NORMAL_CODE.getCode();

        list.forEach(correction -> {

            if ("LATE".equals(type)) {
                correction.setLateStatus(status);
            } else if ("LEAVE".equals(type)) {
                correction.setEarlyLeaveStatus(status);
            } else if ("ALL".equals(type)) {
                correction.setLateStatus(status);
                correction.setEarlyLeaveStatus(status);
            }

            if (!approvedStatus.equals(correction.getLateStatus())
                    && !approvedStatus.equals(correction.getEarlyLeaveStatus())) {
                correction.setAnomaly_verification(normalStatus);
            }
        });

        CommonUtil.fillUpdateAudit(list);
        billRepository.update(list);
    }

    @Override
    public void updateEmployeeById(String id, String employeeId) {
        commonUtil.updateBySql(TABLE_NAME,TcmAttendanceCorrection.EMPLOYEE_ID, employeeId, TcmAttendanceCorrection.ID, Collections.singleton(id));
    }


    @Override
    public void clearTimeByIds(List<String> ids) {
        Map<String,Object> map = new HashMap<>();
        map.put(TcmAttendanceCorrection.ONE_LATE_DURATION, DateUtil.HH_MM_ZERO);
        map.put(TcmAttendanceCorrection.TWO_LATE_DURATION, DateUtil.HH_MM_ZERO);
        map.put(TcmAttendanceCorrection.LATE_DURATION, DateUtil.HH_MM_ZERO);
        map.put(TcmAttendanceCorrection.ONE_EARLY_LEAVE_DURATION, DateUtil.HH_MM_ZERO);
        map.put(TcmAttendanceCorrection.TWO_EARLY_LEAVE_DURATION, DateUtil.HH_MM_ZERO);
        commonUtil.updateBySql(TABLE_NAME,map, ids);
    }

    @Override
    public void isWorkingHoliday(List<String> ids, String isWorkingHoliday, String date) {
        isWorkingHoliday = StringUtils.isBlank(isWorkingHoliday) ? BaseEnum.NO.getCode() : isWorkingHoliday;


        Map<String,Object> map = new HashMap<>();
        map.put(TcmAttendanceCorrection.IS_WORKING_ON_WEEKLY_HOLIDAY, isWorkingHoliday);
        if (BaseEnum.YES.getCode().equals(isWorkingHoliday)) {
            map.put(TcmAttendanceCorrection.IS_FORCED_LEAVE_WORK, BaseEnum.NO.getCode());
        }
        commonUtil.updateBySql(TABLE_NAME,map, ids);

        String tenantId = InvocationInfoProxy.getTenantid();
        LambdaQueryCondition<TcmAttendanceCorrection> routeCondition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        routeCondition.in(TcmAttendanceCorrection::getId, ids)
                .eq(TcmAttendanceCorrection::getYtenantId, tenantId)
                .eq(TcmAttendanceCorrection::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmAttendanceCorrection> list = billRepository.queryByClause(routeCondition);
        Set<String> employeeIds = list.stream().map(TcmAttendanceCorrection::getEmployee_id).collect(Collectors.toSet());

        boolean flag = !Objects.equals(BaseEnum.YES.getCode(), isWorkingHoliday);
        driverHolidayWorkRecordService.changeWorkStatus(date,employeeIds,flag,"1");

    }

    @Override
    public void sheduleShiftsChangeCorrection(TcmSheduleShiftsData data) {
        if (null == data || StringUtils.isBlank(data.getDate()) || StringUtils.isBlank(data.getEmployee_id())) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();

        // 以 日期 + 員工 + 任務 查找已有修正記錄：有則在原記錄上更新排更(pg)/計糧(jl)時間（保留考勤 kq 等欄位），無則新增
        LambdaQueryCondition<TcmAttendanceCorrection> condition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        condition.eq(TcmAttendanceCorrection::getDate, data.getDate())
                .eq(TcmAttendanceCorrection::getEmployee_id, data.getEmployee_id())
                .eq(TcmAttendanceCorrection::getTask_code, data.getTask_code())
                .eq(TcmAttendanceCorrection::getYtenantId, tenantId)
                .eq(TcmAttendanceCorrection::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmAttendanceCorrection> existList = billRepository.queryByClause(condition);
        boolean isNew = existList.isEmpty();
        TcmAttendanceCorrection c = isNew ? new TcmAttendanceCorrection() : existList.get(0);
        if (isNew) {
            c.setId(ymsOidGenerator.nextStrId());
            c.setYtenantId(tenantId);
            c.setDate(data.getDate());
            c.setEmployee_id(data.getEmployee_id());
            c.setTask_code(data.getTask_code());
        }

        // 基礎信息
        c.setWaybill_employee_id(data.getWaybill_employee_id());
        c.setDriver_name(data.getName());
        // 排更(pg)時間：一更發車/收車、二更發車/收車
        c.setPg_one_departure_time(data.getDeparture_time());
        c.setPg_one_return_time(data.getOne_finish_time());
        c.setPg_two_departure_time(data.getTwo_departure_time());
        c.setPg_two_return_time(data.getFinish_time());
        // 計糧(jl)時間默認取排更時間（後續可由後備車長/加班等邏輯覆蓋）
        c.setJl_one_departure_time(data.getDeparture_time());
        c.setJl_one_return_time(data.getOne_finish_time());
        c.setJl_two_departure_time(data.getTwo_departure_time());
        c.setJl_two_return_time(data.getFinish_time());

        // 員工信息（key=員工工號 code；processDate 內按 employee_id 取用工類型等）
        Set<String> idSet = Collections.singleton(data.getEmployee_id());
        Map<String, Map<String, Object>> staffMap = commonUtil.getStaffMapByCardIds(idSet, tenantId, commonUtil.FIELD_CODE_EMPLOYEE_ID);
        // 重新計算有效工時、遲到早退、夜更、異常核對等
        commonUtil.processDate(c, staffMap);

        // 新增/更新落庫
        if (isNew) {
            CommonUtil.fillInsertAudit(c);
            billRepository.insert(Collections.singletonList(c));
        } else {
            CommonUtil.fillUpdateAudit(c);
            billRepository.update(Collections.singletonList(c));
        }
    }

    @Override
    public void updateList(List<TcmAttendanceCorrection> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        CommonUtil.fillUpdateAudit(list);
        billRepository.update(list);
    }

    @Override
    public void saveTcmAttendanceAbsenceDataRecord(List<TcmAttendanceAbsenceDataRecord> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        for (TcmAttendanceAbsenceDataRecord record : list) {
            record.setYtenantId(tenantId);
        }
        CommonUtil.fillInsertAudit(list);
        billRepository.insert(list);
    }


    @Override
    public void saveTcmAttendanceControlCenterLog(List<TcmAttendanceControlCenterLog> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        for (TcmAttendanceControlCenterLog record : list) {
            record.setYtenantId(tenantId);
        }
        CommonUtil.fillInsertAudit(list);
        billRepository.insert(list);
    }

    @Override
    public void saveTcmAttendanceDriverOvertimeRecord(List<TcmAttendanceDriverOvertimeRecord> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        for (TcmAttendanceDriverOvertimeRecord record : list) {
            record.setYtenantId(tenantId);
        }
        CommonUtil.fillInsertAudit(list);
        billRepository.insert(list);
    }

    @Override
    public void delTcmAttendanceAbsenceDataRecord(String... entityId) {
        List<String> ids = new ArrayList<>(Arrays.asList(entityId));
        commonUtil.updateBySql("tcm_attendance_absence_data_record",TcmAttendanceAbsenceDataRecord.DR, "1", TcmAttendanceAbsenceDataRecord.ENTITY, ids);
    }

    @Override
    public void delTcmAttendanceControlCenterLog(String... entityId) {
        List<String> ids = new ArrayList<>(Arrays.asList(entityId));
        commonUtil.updateBySql("tcm_attendance_control_center_log",TcmAttendanceControlCenterLog.DR, "1", TcmAttendanceControlCenterLog.ENTITY, ids);
    }

    @Override
    public void delTcmAttendanceDriverOvertimeRecord(String... entityId) {
        List<String> ids = new ArrayList<>(Arrays.asList(entityId));
        commonUtil.updateBySql("tcm_attendance_driver_overtime_record",TcmAttendanceDriverOvertimeRecord.DR, "1", TcmAttendanceDriverOvertimeRecord.ENTITY, ids);
    }

    // 辅助方法：构建key，处理null值
    private String buildKey(String date, String employeeId) {
        return (date != null ? date : "") + "_" + (employeeId != null ? employeeId : "");
    }

}
