package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.enums.params.ParamKey;

import java.util.Map;
import java.util.function.Function;

/**
 * 参数管理(TCMParamManage) 服务
 * <p>
 * 业务与枚举约定：
 * <ul>
 *   <li>TCMParamManage.module 字段对应 {@link ModuleEnum#getCode()} 的值</li>
 *   <li>TCMParamManage.key    字段对应每个模块独立 *ParamKeyEnum 的 code（见 enums.params 包）</li>
 * </ul>
 *
 * @author root
 */
public interface TcmParamManageService {

    /**
     * 查询某个模块下【所有】参数，返回 Map<参数key, 参数value>
     * 若 DB 中同一个 module 下存在重复的 key，以最先查询到的那条为准（后出现的不覆盖）。
     *
     * @param module 所属模块枚举
     * @return 不可变 Map：key = TCMParamManage.key / value = TCMParamManage.value；
     *         找不到任何记录时返回空 Map（永远不为 null）
     */
    Map<String, String> queryModuleParamMap(ModuleEnum module);

    // =============================================================================================
    // 单个参数查询（带"配置不存在即抛错"校验，3 种入参形式）
    // =============================================================================================

    /**
     * 查询单个参数（Function 形式）。
     * <p>
     * 约定：通过 <b>{@code paramKeyProvider.apply("code")}</b> 获取 TCMParamManage.key，
     * 通过 <b>{@code paramKeyProvider.apply("name")}</b> 获取中文名称（用于错误提示）。
     * 最常见用法是把某个 *ParamKeyEnum 实例包装成：
     * <pre>
     *   keyEnum -> (field -> "code".equals(field) ? keyEnum.getCode() : keyEnum.getName())
     * </pre>
     * 一般业务代码直接调 {@link #queryModuleParamValue(ModuleEnum, ParamKey)} 更直观。
     * <p>
     * <b>找不到配置会抛 RuntimeException</b>，消息格式为：
     * <pre>
     * "配置信息 " + name + "[" + code + "] 不存在，请先维护"
     * </pre>
     *
     * @param module           所属模块枚举（TCMParamManage.module = module.getCode()）
     * @param paramKeyProvider "code"/"name" -> String 的提供函数，不允许为 null（否则 IllegalArgumentException）
     * @return TCMParamManage.value，永远非空（不存在直接抛异常）
     * @throws RuntimeException          若 module/code/name 为空 或 查询结果为空
     * @throws IllegalArgumentException  若 paramKeyProvider == null
     */
    String queryModuleParamValue(ModuleEnum module, Function<String, String> paramKeyProvider);

    /**
     * 查询单个参数（直接传 ParamKey 枚举值 — 推荐便捷用法）。
     * @param module   所属模块枚举
     * @param paramKey ParamKey 枚举实例（code=key, name=名称），不允许为 null
     * @return TCMParamManage.value，永远非空（不存在直接抛异常）
     * @throws RuntimeException     若 module/code/name 为空 或 查询结果为空
     */
    String queryModuleParamValue(ModuleEnum module, ParamKey paramKey);

    /**
     * 查询单个参数（仅传 key 字符串，不做不存在抛错 — 保留静默查询能力）。
     * <p>
     * 行为与此前老版本保持一致：找不到返回 null，不抛异常。
     * 适合"不强制要求配置存在 / 可走默认值"的场景。
     *
     * @param module   所属模块枚举
     * @param paramKey TCMParamManage.key
     * @return 参数 value，找不到 / 参数为空 均返回 null
     */
    String queryModuleParamValueNullable(ModuleEnum module, String paramKey);

}
