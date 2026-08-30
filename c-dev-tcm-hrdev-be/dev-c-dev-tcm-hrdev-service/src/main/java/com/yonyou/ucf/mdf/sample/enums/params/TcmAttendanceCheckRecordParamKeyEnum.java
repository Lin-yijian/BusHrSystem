package com.yonyou.ucf.mdf.sample.enums.params;

import lombok.Getter;

import java.util.Arrays;

/**
 * 参数管理(TCMParamManage) - 海信車載登陸(TCM_ATTENDANCE_CHECK_RECORD) 模块参数键枚举
 * <p>枚举项的 code 对应 TCMParamManage.key 字段；所属 module = {@link com.yonyou.ucf.mdf.sample.enums.ModuleEnum#TCM_ATTENDANCE_CHECK_RECORD}.getCode()
 * <p>
 */
@Getter
public enum TcmAttendanceCheckRecordParamKeyEnum implements ParamKey {

    /** 离线超时阈值（分钟） */
    OFFLINE_TIMEOUT_MINUTES("OFFLINE_TIMEOUT_MINUTES", "離線超時閾值(分鐘)"),
    /** 非法登入门禁告警开关 */
    ILLEGAL_LOGIN_ALERT_ENABLE("ILLEGAL_LOGIN_ALERT_ENABLE", "非法登入告警開關");

    private final String code;
    private final String name;

    TcmAttendanceCheckRecordParamKeyEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TcmAttendanceCheckRecordParamKeyEnum ofCode(String code) {
        if (code == null || code.isEmpty()) return null;
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
