package com.yonyou.ucf.mdf.sample.enums.params;

import lombok.Getter;

import java.util.Arrays;

/**
 * 参数管理(TCMParamManage) - 控制中心日誌(TCM_CONTROL_CENTER_LOG) 模块参数键枚举
 * <p>枚举项的 code 对应 TCMParamManage.key 字段；所属 module = {@link com.yonyou.ucf.mdf.sample.enums.ModuleEnum#TCM_CONTROL_CENTER_LOG}.getCode()
 * <p>
 */
@Getter
public enum TcmControlCenterLogParamKeyEnum implements ParamKey {

    /** 日志保留天数 */
    LOG_RETENTION_DAYS("LOG_RETENTION_DAYS", "日誌保留天數", ParamKey.CHECK_TYPE_INT + ":1-3650"),
    /** 告警通知方式 */
    ALERT_NOTIFY_CHANNEL("ALERT_NOTIFY_CHANNEL", "告警通知方式", ParamKey.CHECK_TYPE_STRING);

    private final String code;
    private final String name;
    /** 参数值格式校验类型（见 ParamKey CHECK_TYPE_* 常量） */
    private final String checkType;

    TcmControlCenterLogParamKeyEnum(String code, String name, String checkType) {
        this.code = code;
        this.name = name;
        this.checkType = checkType;
    }

    public static TcmControlCenterLogParamKeyEnum ofCode(String code) {
        if (code == null || code.isEmpty()) return null;
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
