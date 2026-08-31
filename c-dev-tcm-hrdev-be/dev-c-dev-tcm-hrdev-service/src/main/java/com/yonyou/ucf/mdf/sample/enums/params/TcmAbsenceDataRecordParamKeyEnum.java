package com.yonyou.ucf.mdf.sample.enums.params;

import lombok.Getter;

import java.util.Arrays;

/**
 * 参数管理(TCMParamManage) - 缺勤記錄(TCM_ABSENCE_DATA_RECORD) 模块参数键枚举
 * <p>枚举项的 code 对应 TCMParamManage.key 字段；所属 module = {@link com.yonyou.ucf.mdf.sample.enums.ModuleEnum#TCM_ABSENCE_DATA_RECORD}.getCode()
 * <p>
 */
@Getter
public enum TcmAbsenceDataRecordParamKeyEnum implements ParamKey {

    /** 迟到判定阈值（分钟） */
    LATE_THRESHOLD_MINUTES("LATE_THRESHOLD_MINUTES", "遲到判定閾值(分鐘)", ParamKey.CHECK_TYPE_INT + ":0-1440"),
    /** 早退判定阈值（分钟） */
    EARLY_LEAVE_THRESHOLD_MINUTES("EARLY_LEAVE_THRESHOLD_MINUTES", "早退判定閾值(分鐘)", ParamKey.CHECK_TYPE_INT + ":0-1440");

    private final String code;
    private final String name;
    /** 参数值格式校验类型（见 ParamKey CHECK_TYPE_* 常量） */
    private final String checkType;

    TcmAbsenceDataRecordParamKeyEnum(String code, String name, String checkType) {
        this.code = code;
        this.name = name;
        this.checkType = checkType;
    }

    public static TcmAbsenceDataRecordParamKeyEnum ofCode(String code) {
        if (code == null || code.isEmpty()) return null;
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
