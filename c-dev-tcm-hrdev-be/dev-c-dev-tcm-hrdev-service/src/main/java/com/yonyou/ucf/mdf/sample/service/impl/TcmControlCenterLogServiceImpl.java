package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmControlCenterLog;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmControlCenterLogService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import io.edap.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TcmControlCenterLogServiceImpl implements TcmControlCenterLogService {

    @Autowired
    private IBillRepository billRepository;


    @Override
    public boolean checkDataByDate(String date) {
        return !getListByDate(date).isEmpty();
    }


    private List<TcmControlCenterLog> getListByDate(String date) {
        if (StringUtil.isEmpty(date)) {
            throw new RuntimeException("日期參數不能爲空");
        }

        String tenantId = InvocationInfoProxy.getTenantid();

        LambdaQueryCondition<TcmControlCenterLog> q = new LambdaQueryCondition<>(new TcmControlCenterLog());
        q.eq(TcmControlCenterLog::getDate, date)
                .eq(TcmControlCenterLog::getYtenantId, tenantId)
                .eq(TcmControlCenterLog::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(q);
    }
    
    
}
