package com.yonyou.ucf.mdf.sample.enums.params;

import lombok.Getter;

import java.util.Arrays;

/**
 * 参数管理(TCMParamManage) - 本地排更(ROSTERING) 模块参数键枚举
 * <p>枚举项的 code 对应 TCMParamManage.key 字段；所属 module = {@link com.yonyou.ucf.mdf.sample.enums.ModuleEnum#TCM_SHEDULE_SHIFTS}.getCode() = "ROSTERING"
 * <p>checkType 用于查询参数时对 value 做格式校验，取值见 {@link ParamKey} CHECK_TYPE_* 常量
 */
@Getter
public enum TcmRosteringParamKeyEnum implements ParamKey {

    /** 默认早班时间 HH:mm */
    DEFAULT_SHIFT_MORNING("DEFAULT_SHIFT_MORNING", "默認早班時間", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 默认晚班时间 HH:mm */
    DEFAULT_SHIFT_EVENING("DEFAULT_SHIFT_EVENING", "默認晚班時間", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 排更周期（天） */
    ROSTERING_CYCLE_DAYS("ROSTERING_CYCLE_DAYS", "排更週期(天)", ParamKey.CHECK_TYPE_INT + ":1-366"),

    WORK_TIME("WORK_TIME", "標準工時", ParamKey.CHECK_TYPE_TIME_HH_MM),

    NIGHT_WORK_TIME("NIGHT_WORK_TIME", "深宵司機標準工時", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 迟到判定阈值（HH:mm） */
    LATE_THRESHOLD_MINUTES("LATE_THRESHOLD_MINUTES", "遲到判定閾值(HH:mm)", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 早退判定阈值（HH:mm） */
    EARLY_LEAVE_THRESHOLD_MINUTES("EARLY_LEAVE_THRESHOLD_MINUTES", "早退判定閾值(HH:mm)", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 兼职司机岗位编码 — 对应原 CommonUtil.PART_TIME_JOB_CODE（默认值 P0020103_NC07） */
    PART_TIME_JOB_CODE("PART_TIME_JOB_CODE", "兼職司機崗位編碼", ParamKey.CHECK_TYPE_STRING),
    /** 深宵司机枚举编号 — 对应原 CommonUtil.NIGHT_DIVER_TYPE（默认值 4） */
    NIGHT_DIVER_TYPE("NIGHT_DIVER_TYPE", "深宵司機枚舉編號", ParamKey.CHECK_TYPE_STRING),
    /** 上下更時間間隔(HH:mm) */
    SHIFT_INTERVAL_TIME("SHIFT_INTERVAL_TIME", "上下更時間間隔(HH:mm)", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 培训任务关键字，英文逗号分隔，任务编号与任一项全字段匹配，例如 培訓,培训,跟车培训,跟車培訓 */
    TASK_CODE_TRAINING("TASK_CODE_TRAINING", "培訓关键字", ParamKey.CHECK_TYPE_STRING),
    /** 培训任务計糧一更發車时间（HH:mm） */
    TRAINING_DEPARTURE_TIME("TRAINING_DEPARTURE_TIME", "培訓任務計糧一更發車時間", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 培训任务計糧一更收車时间（HH:mm） */
    TRAINING_RETURN_TIME("TRAINING_RETURN_TIME", "培訓任務計糧一更收車時間", ParamKey.CHECK_TYPE_TIME_HH_MM),
    /** 培训任务用膳次数 */
    TRAINING_MEAL_COUNT("TRAINING_MEAL_COUNT", "培訓任務用膳次數", ParamKey.CHECK_TYPE_INT + ":0-99"),
    /** 培训任务用膳时长（HH:mm） */
    TRAINING_MEAL_DURATION("TRAINING_MEAL_DURATION", "培訓任務用膳時長", ParamKey.CHECK_TYPE_TIME_HH_MM);

    private final String code;
    private final String name;
    /** 参数值格式校验类型（见 ParamKey CHECK_TYPE_* 常量） */
    private final String checkType;

    TcmRosteringParamKeyEnum(String code, String name, String checkType) {
        this.code = code;
        this.name = name;
        this.checkType = checkType;
    }

    public static TcmRosteringParamKeyEnum ofCode(String code) {
        if (code == null || code.isEmpty()) return null;
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
