package com.yonyou.ucf.mdf.sample.enums.params;

import lombok.Getter;

import java.util.Arrays;

/**
 * 参数管理(TCMParamManage) - 車長超時工作(TCM_DRIVER_OVERTIME) 模块参数键枚举
 * <p>枚举项的 code 对应 TCMParamManage.key 字段；所属 module = {@link com.yonyou.ucf.mdf.sample.enums.ModuleEnum#TCM_DRIVER_OVERTIME}.getCode()
 * <p>
 */
@Getter
public enum TcmDriverOvertimeParamKeyEnum implements ParamKey {

    /** 超时阈值（小时） */
    OVERTIME_THRESHOLD_HOURS("OVERTIME_THRESHOLD_HOURS", "超時閾值(小時)", ParamKey.CHECK_TYPE_DECIMAL + ":0-24"),
    /** 加班费倍率 */
    OVERTIME_PAY_RATE("OVERTIME_PAY_RATE", "加班費倍率", ParamKey.CHECK_TYPE_DECIMAL + ":0-100");

    /** 对应 TCMParamManage.key */
    private final String code;
    /** 对应 TCMParamManage.name（参数名称/说明） */
    private final String name;
    /** 参数值格式校验类型（见 ParamKey CHECK_TYPE_* 常量） */
    private final String checkType;

    TcmDriverOvertimeParamKeyEnum(String code, String name, String checkType) {
        this.code = code;
        this.name = name;
        this.checkType = checkType;
    }

    /**
     * 根据 code 查找对应枚举常量
     * @param code TCMParamManage.key
     * @return 枚举项，找不到返回 null
     */
    public static TcmDriverOvertimeParamKeyEnum ofCode(String code) {
        if (code == null || code.isEmpty()) return null;
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
