package com.yonyou.ucf.mdf.sample.enums.params;

/**
 * 参数键的统一抽取契约。
 * <p>
 * 每个模块独立的 *ParamKeyEnum（例如 {@link TcmDriverOvertimeParamKeyEnum}）都实现此接口，
 * 从而允许上游（例如 {@code TcmParamManageService#queryModuleParamValue}）
 * 用统一强类型接收任意 ParamKey 枚举值，并提取：
 * <ul>
 *   <li>{@link #getCode()} — 对应 TCMParamManage.key 字段，作为查询 key</li>
 *   <li>{@link #getName()} — 对应 TCMParamManage.name 字段，用于异常提示的可读名称</li>
 * </ul>
 */
public interface ParamKey {

    /**
     * @return 对应 TCMParamManage.key 的值，非空
     */
    String getCode();

    /**
     * @return 对应 TCMParamManage.name 的值（参数名称 / 中文说明），用于错误提示
     */
    String getName();
}
