package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMCar;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmCarService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import io.edap.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TcmCarServiceImpl implements TcmCarService {


    @Autowired
    private IBillRepository billRepository;


    @Override
    public Map<String, TCMCar> groupCarMapByPlateNumber(Collection<String> busPlateNumberList) {
        return groupCarMapByField(
                busPlateNumberList,
                TCMCar::getPlate_number,
                "车牌号",
                (condition, list) -> condition.in(TCMCar::getPlate_number, list)
        );
    }

    @Override
    public Map<String, TCMCar> groupCarMapByInternalNo(Collection<String> busInternalNoList) {
        return groupCarMapByField(
                busInternalNoList,
                TCMCar::getInternal_no,
                "内部编号",
                (condition, list) -> condition.in(TCMCar::getInternal_no, list)
        );
    }

    /**
     * 通用车辆信息查询分组方法（支持动态查询条件）
     * @param valueList 查询值列表
     * @param fieldGetter 字段getter方法引用（用于Map的key）
     * @param fieldName 字段中文名（用于错误提示）
     * @param conditionBuilder 查询条件构建器
     * @return 字段值->车辆实体 的Map
     */
    private Map<String, TCMCar> groupCarMapByField(
            Collection<String> valueList,
            Function<TCMCar, String> fieldGetter,
            String fieldName,
            BiConsumer<LambdaQueryCondition<TCMCar>, Collection<String>> conditionBuilder) {

        if (valueList == null || valueList.isEmpty()) {
            return new HashMap<>();
        }

        String tenantId = InvocationInfoProxy.getTenantid();

        LambdaQueryCondition<TCMCar> carCondition = new LambdaQueryCondition<>(new TCMCar());
        conditionBuilder.accept(carCondition, valueList);  // 动态注入查询条件
        carCondition.eq(TCMCar::getYtenantId, tenantId)
                .eq(TCMCar::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());

        List<TCMCar> list = billRepository.queryByClause(carCondition);

        if (list.isEmpty()) {
            if (StringUtil.isEmpty(fieldName)){
                return new HashMap<>();
            }
            throw new RuntimeException(String.format("车辆信息档案缺失，请先完善档案内容，%s：%s",
                    fieldName, String.join(", ", valueList)));
        }

        return list.stream().collect(Collectors.toMap(
                fieldGetter,
                item -> item,
                (existing, replacement) -> existing
        ));
    }
}
