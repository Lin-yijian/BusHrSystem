package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.enums.params.ParamKey;

import java.util.Map;

/**
 * 参数管理(TCMParamManage) 服务
 * <p>
 * 业务与枚举约定：
 * <ul>
 *   <li>TCMParamManage.module 字段对应 {@link ModuleEnum#getCode()} 的值</li>
 *   <li>TCMParamManage.key    字段对应每个模块独立 *ParamKeyEnum 的 code（见 enums.params 包）</li>
 *   <li>每个 *ParamKeyEnum 的 checkType 决定查询结果值的格式校验规则（见 {@link ParamKey} CHECK_TYPE_* 常量）</li>
 * </ul>
 *
 * @author root
 */
public interface TcmParamManageService {

    /**
     * 【批量】查询某个模块下【所有】参数，返回 Map<参数key, 参数value>。
     * <p>适用于循环外一次性取整模块参数的高性能场景（单条 SQL）。
     * 注意：此方法【不做】checkType 格式校验（Map 的 key 无强类型枚举对应），
     * 业务侧取值后请配合 {@link ParamKey#checkValue} 校验。
     * <p>若 DB 中同一个 module 下存在重复的 key，以最先查询到的那条为准（后出现的不覆盖）。
     *
     * @param module 所属模块枚举
     * @return 不可变 Map：key = TCMParamManage.key / value = TCMParamManage.value；
     *         找不到任何记录时返回空 Map（永远不为 null）
     */
    Map<String, String> queryModuleParamMap(ModuleEnum module);

    /**
     * 【统一单值查询入口】按模块 + ParamKey 枚举查询参数值，并按枚举的 checkType 做格式校验。
     * <p>
     * 三段式保障：
     * <ol>
     *   <li>不存在/空白 → 抛 {@code "配置缺失：name[code]，請至【參數管理】菜單補錄"}</li>
     *   <li>格式非法（按 {@link ParamKey#getCheckType()} 校验）→ 抛
     *       {@code "配置非法：name[code]=value，請至【參數管理】菜單修正"}</li>
     *   <li>校验通过 → 返回去空白后的 value</li>
     * </ol>
     *
     * @param module   所属模块枚举（TCMParamManage.module = module.getCode()），不允许为空
     * @param paramKey ParamKey 枚举实例（code=key, name=名称, checkType=校验类型），不允许为空
     * @return 校验通过的参数 value（去空白），永远非空
     * @throws RuntimeException         缺失或格式非法
     * @throws IllegalArgumentException paramKey / module 为空
     */
    String queryParamValue(ModuleEnum module, ParamKey paramKey);

}
