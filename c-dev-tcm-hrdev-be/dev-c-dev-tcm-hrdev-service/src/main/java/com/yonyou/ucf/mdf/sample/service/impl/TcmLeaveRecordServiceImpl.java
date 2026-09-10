package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmLeaveRecord;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmLeaveRecordService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class TcmLeaveRecordServiceImpl implements TcmLeaveRecordService {

    @Autowired
    private IBillRepository billRepository;


    @Override
    public boolean checkExist(String date, String employeeId, String type) {

        String tenantId = InvocationInfoProxy.getTenantid();

        LambdaQueryCondition<TcmLeaveRecord> leaveCondition = new LambdaQueryCondition<>(new TcmLeaveRecord());
        leaveCondition.eq(TcmLeaveRecord::getStart_date, date)
                .eq(TcmLeaveRecord::getEmployee_id, employeeId)
                .eq(TcmLeaveRecord::getLeave_type,type)
                .eq(TcmLeaveRecord::getYtenantId, tenantId)
                .eq(TcmLeaveRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmLeaveRecord> list = billRepository.queryByClause(leaveCondition);
        return !list.isEmpty();
    }

    @Override
    public void delByEmployeeIdAndDate(String date, String employeeId) {
        String tenantId = InvocationInfoProxy.getTenantid();

        LambdaQueryCondition<TcmLeaveRecord> leaveCondition = new LambdaQueryCondition<>(new TcmLeaveRecord());
        leaveCondition.eq(TcmLeaveRecord::getStart_date, date)
                .eq(TcmLeaveRecord::getEmployee_id, employeeId)
                .eq(TcmLeaveRecord::getYtenantId, tenantId)
                .eq(TcmLeaveRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmLeaveRecord> list = billRepository.queryByClause(leaveCondition);
        for (TcmLeaveRecord record : list) {
            record.setDr(BaseEnum.IS_DELETED.getIntCode());
        }
        if (!list.isEmpty()) {
            billRepository.update(list);
        }

    }


}
