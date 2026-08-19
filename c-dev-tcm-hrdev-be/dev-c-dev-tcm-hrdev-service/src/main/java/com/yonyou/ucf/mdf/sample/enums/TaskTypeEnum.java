package com.yonyou.ucf.mdf.sample.enums;

import lombok.Getter;

@Getter
public enum TaskTypeEnum {

    /** 休假記錄 */
    LEAVE_RECORD("1", "休假記錄"),

    /** 考勤统计 */
    ATTENDANCE_STATISTICS("2", "考勤统计"),

    /** 排更數據 */
    SCHEDULE_DATA("3", "排更數據"),

    /** 巴型同步任务 */
    BUS_TYPE_SYNC_TASK("4", "巴型同步任务"),

    /** 巴型转数数据 */
    BUS_TYPE_TRANSFER_DATA("5", "巴型转数数据"),

    /** 車載系統登錄記錄 */
    VEHICLE_SYSTEM_LOGIN("6", "車載系統登錄記錄"),

    /** 考勤計算 */
    ATTENDANCE_CALCULATION("7", "考勤計算"),

    /** 海信考勤数据 */
    HISENSE_ATTENDANCE_DATA("8", "海信考勤数据"),

    /** 入油加氣 */
    FUEL_GAS("9", "入油加氣"),

    /** 車長超時工作紀錄 */
    DRIVER_OVERTIME_RECORD("10", "車長超時工作紀錄"),

    /** 車長假期工作記錄 */
    DRIVER_HOLIDAY_WORK_RECORD("11", "車長假期工作記錄"),

    /** 考勤修正 */
    ATTENDANCE_CORRECTION("12", "考勤修正"),

    /** 海信排更數據 */
    HISENSE_SCHEDULE_DATA("13", "海信排更數據"),

    /** 考勤匯總 */
    ATTENDANCE_SUMMARY("14", "考勤匯總"),

    /** 後備車長考勤記錄 */
    BACKUP_DRIVER_ATTENDANCE("15", "後備車長考勤記錄"),

    /** 考勤結果 */
    ATTENDANCE_RESULT("16", "考勤結果"),

    /** 排更同步任务 */
    SCHEDULE_SYNC_TASK("17", "排更同步任务"),

    /** 巴型工時數據 */
    BUS_TYPE_WORK_HOURS("18", "巴型工時數據"),

    /** 巴士故障數據記錄 */
    BUS_FAULT_RECORD("19", "巴士故障數據記錄"),

    /** 考勤核對記錄 */
    ATTENDANCE_CHECK_RECORD("20", "考勤核對記錄"),

    /** 控制中心日誌 */
    CONTROL_CENTER_LOG("21", "控制中心日誌"),

    /** 交車取車次記錄 */
    VEHICLE_HANDOVER_RECORD("22", "交車取車次記錄"),

    /** 缺勤數據記錄 */
    ABSENCE_RECORD("23", "缺勤數據記錄"),

    /** 考勤日結確認 */
    ATTENDANCE_DAILY_CONFIRM("24", "考勤日結確認"),

    /** 考勤異常核對 */
    ATTENDANCE_ABNORMAL_CHECK("25", "考勤異常核對"),

    /** 周假額外工作時數 */
    WEEKLY_HOLIDAY_OVERTIME("26", "周假額外工作時數"),

    /** 考勤修改備註 */
    ATTENDANCE_MODIFY_REMARK("27", "考勤修改備註");

    private final String code;
    private final String desc;

    TaskTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static TaskTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (TaskTypeEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取描述
     */
    public static String getDescByCode(String code) {
        TaskTypeEnum e = getByCode(code);
        return e != null ? e.desc : null;
    }

    /**
     * 校验 code 是否存在
     */
    public static boolean isValidCode(String code) {
        return getByCode(code) != null;
    }
}