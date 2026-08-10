package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmBusFaultDataRecord;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmBusFaultDataRecordService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import io.edap.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TcmBusFaultDataRecordServiceImpl implements TcmBusFaultDataRecordService {


    @Autowired
    private IBillRepository billRepository;


    @Override
    public boolean checkDataByDate(String date) {
        return !getListByDate(date).isEmpty();
    }


    private List<TcmBusFaultDataRecord> getListByDate(String date) {
        if (StringUtil.isEmpty(date)) {
            throw new RuntimeException("日期參數不能爲空");
        }

        String tenantId = InvocationInfoProxy.getTenantid();

        LambdaQueryCondition<TcmBusFaultDataRecord> q = new LambdaQueryCondition<>(new TcmBusFaultDataRecord());
        q.eq(TcmBusFaultDataRecord::getReport_date, date)
                .eq(TcmBusFaultDataRecord::getYtenantId, tenantId)
                .eq(TcmBusFaultDataRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(q);
    }
    
    
    
}
