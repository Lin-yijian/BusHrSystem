package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMRoute;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmRouteService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TcmRouteServiceImpl implements TcmRouteService {


    @Autowired
    private IBillRepository billRepository;


    @Override
    public List<TCMRoute> listAll() {
        String tenantId = InvocationInfoProxy.getTenantid();

        LambdaQueryCondition<TCMRoute> carCondition = new LambdaQueryCondition<>(new TCMRoute());
        carCondition.eq(TCMRoute::getYtenantId, tenantId).eq(TCMRoute::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(carCondition);
    }

    @Override
    public List<String> listAllCode() {
        List<TCMRoute> list = listAll();
        return list.stream().map(TCMRoute::getRoute_code).collect(Collectors.toList());
    }

    @Override
    public Map<String, TCMRoute> getMap() {
        List<TCMRoute> list = listAll();
        if (list.isEmpty()) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.toMap(
                TCMRoute::getRoute_code,
                item -> item,
                (existing, replacement) -> existing
        ));
    }


}
