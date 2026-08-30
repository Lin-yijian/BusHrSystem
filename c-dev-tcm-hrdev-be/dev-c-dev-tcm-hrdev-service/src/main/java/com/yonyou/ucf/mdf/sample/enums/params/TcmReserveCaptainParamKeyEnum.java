package com.yonyou.ucf.mdf.sample.enums.params;

import lombok.Getter;

import java.util.Arrays;

/**
 * 参数管理(TCMParamManage) - 後備車長工作記錄(TCM_RESERVE_CAPTAIN_ATTENDANCE_RECORD) 模块参数键枚举
 * <p>枚举项的 code 对应 TCMParamManage.key 字段；所属 module = {@link com.yonyou.ucf.mdf.sample.enums.ModuleEnum#TCM_RESERVE_CAPTAIN_ATTENDANCE_RECORD}.getCode()
 * <p>
 */
@Getter
public enum TcmReserveCaptainParamKeyEnum implements ParamKey {

    /** 示例：后备车长到岗提前量（分钟） */
    RESERVE_ARRIVE_AHEAD_MINUTES("RESERVE_ARRIVE_AHEAD_MINUTES", "後備車長到崗提前量(分鐘)"),
    /** 示例：最大后备待命时长（小时） */
    RESERVE_MAX_WAIT_HOURS("RESERVE_MAX_WAIT_HOURS", "最大後備待命時長(小時)");

    private final String code;
    private final String name;

    TcmReserveCaptainParamKeyEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TcmReserveCaptainParamKeyEnum ofCode(String code) {
        if (code == null || code.isEmpty()) return null;
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
