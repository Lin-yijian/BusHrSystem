package com.yonyou.ucf.mdf.sample.enums.params;

import lombok.Getter;

import java.util.Arrays;

/**
 * 参数管理(TCMParamManage) - 考勤核對(TCM_ATTENDANCE_VERIFICATION) 模块参数键枚举
 * <p>枚举项的 code 对应 TCMParamManage.key 字段；所属 module = {@link com.yonyou.ucf.mdf.sample.enums.ModuleEnum#TCM_ATTENDANCE_VERIFICATION}.getCode() = "TCM_ATTENDANCE_VERIFICATION"
 * <p>checkType 用于查询参数时对 value 做格式校验，取值见 {@link ParamKey} CHECK_TYPE_* 常量
 */
@Getter
public enum TcmAttendanceVerificationParamKeyEnum implements ParamKey {

    /** 夜更判定窗口起點（HH:mm），如 17:00 */
    NIGHT_SHIFT_WINDOW_START("NIGHT_SHIFT_WINDOW_START", "夜更判定窗口起點(HH:mm)", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 夜更判定窗口終點（HH:mm），如 20:00 */
    NIGHT_SHIFT_WINDOW_END("NIGHT_SHIFT_WINDOW_END", "夜更判定窗口終點(HH:mm)", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 夜更判定：計量有效工時門檻（HH:mm），如 08:00 */
    NIGHT_SHIFT_MIN_VALID_TIME("NIGHT_SHIFT_MIN_VALID_TIME", "夜更判定最低有效工時(HH:mm)", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 勤工獎：單更遲到計次門檻（HH:mm 時長），遲到時長 >= 此值算一次，如 00:10 */
    LATE_COUNT_THRESHOLD("LATE_COUNT_THRESHOLD", "遲到計次門檻(HH:mm)", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 勤工獎：單更早退計次門檻（HH:mm 時長），早退時長 >= 此值視為該更早退，如 00:30 */
    EARLY_LEAVE_SINGLE_THRESHOLD("EARLY_LEAVE_SINGLE_THRESHOLD", "單更早退計次門檻(HH:mm)", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 勤工獎：兩更早退合計門檻（HH:mm 時長），兩更均不足單更門檻但合計 >= 此值算一次，如 00:40 */
    EARLY_LEAVE_COMBINED_THRESHOLD("EARLY_LEAVE_COMBINED_THRESHOLD", "早退合計門檻(HH:mm)", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 勤工獎：深宵車長早退判定有效工時門檻（HH:mm），如 06:00 */
    NIGHT_EARLY_LEAVE_STANDARD_TIME("NIGHT_EARLY_LEAVE_STANDARD_TIME", "深宵車長早退判定工時門檻(HH:mm)", ParamKey.CHECK_TYPE_TIME_HH_MM);

    private final String code;
    private final String name;
    /** 参数值格式校验类型（见 ParamKey CHECK_TYPE_* 常量） */
    private final String checkType;

    TcmAttendanceVerificationParamKeyEnum(String code, String name, String checkType) {
        this.code = code;
        this.name = name;
        this.checkType = checkType;
    }

    public static TcmAttendanceVerificationParamKeyEnum ofCode(String code) {
        if (code == null || code.isEmpty()) return null;
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
