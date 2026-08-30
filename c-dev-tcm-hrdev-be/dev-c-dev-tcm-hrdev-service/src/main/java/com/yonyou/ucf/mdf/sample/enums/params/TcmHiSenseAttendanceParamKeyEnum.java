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

    /** 示例：合并间隔阈值（分钟） */
    MERGE_GAP_MINUTES("MERGE_GAP_MINUTES", "合併間隔閾值(分鐘)"),
    /** 示例：最少有效考勤小时数 */
    MIN_VALID_ATTENDANCE_HOURS("MIN_VALID_ATTENDANCE_HOURS", "最少有效考勤時數");

    private final String code;
    private final String name;

    TcmHiSenseAttendanceParamKeyEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TcmHiSenseAttendanceParamKeyEnum ofCode(String code) {
        if (code == null || code.isEmpty()) return null;
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
