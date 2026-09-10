package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMParamManage;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.enums.params.ParamKey;
import com.yonyou.ucf.mdf.sample.service.TcmParamManageService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数管理(TCMParamManage) 服务实现
 * <p>
 * 统一查询入口 {@link #queryParamValue}：查询 + 缺失抛错 + checkType 格式校验一条龙。
 *
 * @author root
 */
@Service
@Slf4j
public class TcmParamManageServiceImpl implements TcmParamManageService {

    /** 逻辑删除标识：0 = 未删除 */
    private static final Short DR_NOT_DELETED = (short) 0;

    @Autowired
    private IBillRepository billRepository;

    @Override
    public Map<String, String> queryModuleParamMap(ModuleEnum module) {

        if (module == null) {
            return Collections.emptyMap();
        }
        String moduleCode = module.getCode();
        if (StringUtils.isBlank(moduleCode)) {
            return Collections.emptyMap();
        }

        String tenantId = StringUtils.defaultString(InvocationInfoProxy.getTenantid());
        LambdaQueryCondition<TCMParamManage> cond = new LambdaQueryCondition<>(new TCMParamManage());
        cond.eq(TCMParamManage::getModule, moduleCode)
                .eq(TCMParamManage::getDr, DR_NOT_DELETED);
        if (StringUtils.isNotBlank(tenantId)) {
            cond.eq(TCMParamManage::getYtenantId, tenantId);
        }
        List<TCMParamManage> list = billRepository.queryByClause(cond);
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new LinkedHashMap<>(list.size());
        for (TCMParamManage p : list) {
            if (p == null) continue;
            String k = p.getKey();
            if (k == null) continue;
            result.putIfAbsent(k, p.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public String queryParamValue(ModuleEnum module, ParamKey paramKey) {
        if (paramKey == null) {
            throw new IllegalArgumentException("参数paramKey(ParamKey)不能为空");
        }
        if (module == null || StringUtils.isBlank(module.getCode())) {
            throw new RuntimeException("参数模块(ModuleEnum)不能为空");
        }
        String code = paramKey.getCode();
        String name = paramKey.getName();
        if (StringUtils.isBlank(code)) {
            throw new RuntimeException("配置缺失：" + StringUtils.defaultIfBlank(name, "UNKNOWN") + "[" + code
                    + "]，請至【參數管理】菜單補錄");
        }

        // 1. 查询（实时走 DB，不缓存）
        String value = queryNullableByCode(module.getCode(), code);

        // 2. 缺失/空白 → 统一抛错
        if (StringUtils.isBlank(value)) {
            throw new RuntimeException("配置缺失：" + StringUtils.defaultIfBlank(name, code) + "[" + code
                    + "]，請至【參數管理】菜單補錄");
        }

        // 3. 按枚举 checkType 做格式校验（非法直接抛错）
        return ParamKey.checkValue(paramKey, value);
    }

    private String queryNullableByCode(String moduleCode, String paramKey) {
        String tenantId = StringUtils.defaultString(InvocationInfoProxy.getTenantid());
        LambdaQueryCondition<TCMParamManage> cond = new LambdaQueryCondition<>(new TCMParamManage());
        cond.eq(TCMParamManage::getModule, moduleCode)
                .eq(TCMParamManage::getKey, paramKey)
                .eq(TCMParamManage::getDr, DR_NOT_DELETED);
        if (StringUtils.isNotBlank(tenantId)) {
            cond.eq(TCMParamManage::getYtenantId, tenantId);
        }
        List<TCMParamManage> list = billRepository.queryByClause(cond);
        if (list == null || list.isEmpty()) return null;
        TCMParamManage first = list.get(0);
        return first == null ? null : first.getValue();
    }
}
