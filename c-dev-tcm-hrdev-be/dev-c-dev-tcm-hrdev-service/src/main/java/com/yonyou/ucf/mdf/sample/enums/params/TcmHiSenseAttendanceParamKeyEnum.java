package com.yonyou.ucf.mdf.sample.enums.params;

import lombok.Getter;

import java.util.Arrays;

/**
 * 参数管理(TCMParamManage) - 海信考勤(TCM_HI_SENSE_ATTENDANCE) 模块参数键枚举
 * <p>枚举项的 code 对应 TCMParamManage.key 字段；所属 module = {@link com.yonyou.ucf.mdf.sample.enums.ModuleEnum#TCM_HI_SENSE_ATTENDANCE}.getCode()
 * <p>
 */
@Getter
public enum TcmHiSenseAttendanceParamKeyEnum implements ParamKey {

    /** 合并间隔阈值（HH:mm 时长格式，例如 01:00 表示 1 小时内允许合并） */
    MERGE_GAP_DURATION("MERGE_GAP_DURATION", "合併間隔閾值(HH:mm)", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 最少有效考勤小时数 */
    MIN_VALID_ATTENDANCE_HOURS("MIN_VALID_ATTENDANCE_HOURS", "最少有效考勤時數", ParamKey.CHECK_TYPE_DECIMAL + ":0-24");

    private final String code;
    private final String name;
    /** 参数值格式校验类型（见 ParamKey CHECK_TYPE_* 常量） */
    private final String checkType;

    TcmHiSenseAttendanceParamKeyEnum(String code, String name, String checkType) {
        this.code = code;
        this.name = name;
        this.checkType = checkType;
    }

    public static TcmHiSenseAttendanceParamKeyEnum ofCode(String code) {
        if (code == null || code.isEmpty()) return null;
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
