package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class TcmAttendanceCorrectionServiceImpl implements TcmAttendanceCorrectionService {


    @Autowired
    private IBillRepository billRepository;
    
    @Override
    public List<TcmAttendanceCorrection> getByDateAndStaffId(String date, String staffId) {

        String tenantId = InvocationInfoProxy.getTenantid();
        LambdaQueryCondition<TcmAttendanceCorrection> routeCondition = new LambdaQueryCondition<>(new TcmAttendanceCorrection());
        routeCondition.eq(TcmAttendanceCorrection::getDate, date)
                .eq(TcmAttendanceCorrection::getEmployee_id,staffId)
                .eq(TcmAttendanceCorrection::getYtenantId,tenantId)
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
                .in(TcmAttendanceCorrection::getEmployee_id,staffIdList)
                .eq(TcmAttendanceCorrection::getYtenantId,tenantId)
                .eq(TcmAttendanceCorrection::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(routeCondition);
    }


}
