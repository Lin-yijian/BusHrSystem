package com.yonyou.ucf.mdf.sample.enums.params;

import lombok.Getter;

import java.util.Arrays;

/**
 * 参数管理(TCMParamManage) - 本地排更(ROSTERING) 模块参数键枚举
 * <p>枚举项的 code 对应 TCMParamManage.key 字段；所属 module = {@link com.yonyou.ucf.mdf.sample.enums.ModuleEnum#TCM_SHEDULE_SHIFTS}.getCode() = "ROSTERING"
 * <p>
 */
@Getter
public enum TcmRosteringParamKeyEnum implements ParamKey {

    /** 示例：默认早班时间 HH:mm */
    DEFAULT_SHIFT_MORNING("DEFAULT_SHIFT_MORNING", "默認早班時間"),
    /** 示例：默认晚班时间 HH:mm */
    DEFAULT_SHIFT_EVENING("DEFAULT_SHIFT_EVENING", "默認晚班時間"),
    /** 示例：排更周期（天） */
    ROSTERING_CYCLE_DAYS("ROSTERING_CYCLE_DAYS", "排更週期(天)");

    private final String code;
    private final String name;

    TcmRosteringParamKeyEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TcmRosteringParamKeyEnum ofCode(String code) {
        if (code == null || code.isEmpty()) return null;
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
