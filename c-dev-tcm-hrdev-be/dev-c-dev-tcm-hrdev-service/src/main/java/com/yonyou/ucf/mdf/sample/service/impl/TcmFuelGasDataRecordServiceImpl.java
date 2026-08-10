package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmFuelGasDataRecord;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmFuelGasDataRecordService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import io.edap.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TcmFuelGasDataRecordServiceImpl implements TcmFuelGasDataRecordService {


    @Autowired
    private IBillRepository billRepository;


    @Override
    public boolean checkDataByDate(String date) {
        return !getListByDate(date).isEmpty();
    }


    private List<TcmFuelGasDataRecord> getListByDate(String date) {
        if (StringUtil.isEmpty(date)) {
            throw new RuntimeException("日期參數不能爲空");
        }

        String tenantId = InvocationInfoProxy.getTenantid();

        LambdaQueryCondition<TcmFuelGasDataRecord> q = new LambdaQueryCondition<>(new TcmFuelGasDataRecord());
        q.eq(TcmFuelGasDataRecord::getAttendance_date, date)
                .eq(TcmFuelGasDataRecord::getYtenantId, tenantId)
                .eq(TcmFuelGasDataRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(q);
    }
    
    
    
}
