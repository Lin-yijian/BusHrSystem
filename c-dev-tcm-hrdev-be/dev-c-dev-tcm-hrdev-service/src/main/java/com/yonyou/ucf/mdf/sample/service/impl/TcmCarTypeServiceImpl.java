package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMCarType;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmCarTypeService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TcmCarTypeServiceImpl implements TcmCarTypeService {

    @Autowired
    private IBillRepository billRepository;

    @Override
    public List<TCMCarType> selectAll() {

        String tenantId = InvocationInfoProxy.getTenantid();
        LambdaQueryCondition<TCMCarType> carCondition = new LambdaQueryCondition<>(new TCMCarType());
        carCondition.eq(TCMCarType::getYtenantId, tenantId)
                .eq(TCMCarType::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(carCondition);
    }
}
