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
import java.util.function.Function;

/**
 * 参数管理(TCMParamManage) 服务实现
 *
 * @author root
 */
@Service
@Slf4j
public class TcmParamManageServiceImpl implements TcmParamManageService {

    /** 逻辑删除标识：0 = 未删除 */
    private static final Short DR_NOT_DELETED = (short) 0;

    /** Function 入参标记：提取 "code"（对应 TCMParamManage.key） */
    public static final String FK_CODE = "code";
    /** Function 入参标记：提取 "name"（对应 参数中文名称，用于错误提示） */
    public static final String FK_NAME = "name";

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
    public String queryModuleParamValue(ModuleEnum module, Function<String, String> paramKeyProvider) {
        if (paramKeyProvider == null) {
            throw new IllegalArgumentException("参数paramKeyProvider(Function<String,String>)不能为空");
        }
        String code = paramKeyProvider.apply(FK_CODE);
        String name = paramKeyProvider.apply(FK_NAME);

        if (module == null || StringUtils.isBlank(module.getCode())) {
            throw new RuntimeException("参数模块(ModuleEnum)不能为空");
        }
        if (StringUtils.isBlank(code)) {
            String displayName = StringUtils.defaultIfBlank(name, "UNKNOWN");
            String displayCode = StringUtils.defaultIfBlank(code, "");
            throw new RuntimeException("配置缺失：" + displayName + "[" + displayCode + "]，請至【參數管理】菜單補錄");
        }

        // 查询
        String value = queryNullableByCode(module.getCode(), code);

        if (StringUtils.isBlank(value)) {
            // 不存在或空值：统一按用户指定格式抛错
            String displayName = StringUtils.defaultIfBlank(name, code);
            throw new RuntimeException("配置缺失：" + displayName + "[" + code + "]，請至【參數管理】帶單補錄");
        }
        return value;
    }

    @Override
    public String queryModuleParamValue(ModuleEnum module, ParamKey paramKey) {
        if (paramKey == null) {
            throw new IllegalArgumentException("参数paramKey(ParamKey)不能为空");
        }
        // 统一包装成 Function，复用"主版"的校验与异常格式（避免两套逻辑漂移）
        Function<String, String> provider = field -> {
            if (FK_CODE.equals(field)) return paramKey.getCode();
            if (FK_NAME.equals(field)) return paramKey.getName();
            return null;
        };
        return queryModuleParamValue(module, provider);
    }

    @Override
    public String queryModuleParamValueNullable(ModuleEnum module, String paramKey) {
        if (module == null || StringUtils.isBlank(module.getCode()) || StringUtils.isBlank(paramKey)) {
            return null;
        }
        return queryNullableByCode(module.getCode(), paramKey);
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
