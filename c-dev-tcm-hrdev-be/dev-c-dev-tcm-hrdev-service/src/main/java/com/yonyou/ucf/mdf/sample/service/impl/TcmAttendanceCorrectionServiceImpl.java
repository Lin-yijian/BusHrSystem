package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.BusinessException;
import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.SQLParameter;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceAbsenceDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.DateUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
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

    public static final String TABLE_NAME = "tcm_attendance_correction";

    @Override
    public List<TcmAttendanceCorrection> getByDateAndStaffId(String date, String staffId) {

        String tenantId = InvocationInfoProxy.getTenantid();
        LambdaQueryCondition<TcmAttendanceCorrection> routeCondition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        routeCondition.eq(TcmAttendanceCorrection::getDate, date)
                .eq(TcmAttendanceCorrection::getEmployee_id, staffId)
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

        billRepository.update(list);
    }

    @Override
    public void updateEmployeeById(String id, String employeeId) {
        commonUtil.updateBySql(TABLE_NAME,TcmAttendanceCorrection.EMPLOYEE_ID,employeeId, Collections.singleton(id));
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
    public void isWorkingHoliday(List<String> ids,boolean isWorkingHoliday) {
        commonUtil.updateBySql(TABLE_NAME,TcmAttendanceCorrection.IS_WORKING_ON_WEEKLY_HOLIDAY, String.valueOf(isWorkingHoliday), ids);
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
        billRepository.insert(list);
    }

    // 辅助方法：构建key，处理null值
    private String buildKey(String date, String employeeId) {
        return (date != null ? date : "") + "_" + (employeeId != null ? employeeId : "");
    }

}
