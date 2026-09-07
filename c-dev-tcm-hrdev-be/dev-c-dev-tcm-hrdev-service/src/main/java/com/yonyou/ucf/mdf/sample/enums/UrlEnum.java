package com.yonyou.ucf.mdf.sample.enums;

import lombok.Getter;

@Getter
public enum UrlEnum {

    // 路线管理
    ROUTE("TCMBaseInfo.TCMBaseInfo.TCMRoute", "c-dev-tcm-hrdev"),
    // 海信考勤
    HISENSE_ATTENDANCE("unitfyEnum.TCMCaptainAttendance.tcmHisenseAttendanceData", "c-dev-tcm-hrdev"),
    // 海信车载登录
    WAYBILL_LOGIN_RECORD("TCMCaptainAttendance.CJ26030001.tcmHisenseWaybillLoginRecord", "c-dev-tcm-hrdev"),
    // 员工信息
    BD_STAFF("hred.staff.Staff", "hrcloud-staff-mgr"),
    // 时间管理-工作日历-假日定义
    TS_HOLIDAY("hrtm.workingCalendar.Holiday", "yonbip-hr-tm"),
    // 车辆信息
    CAR_INFO("TCMBaseInfo.TCMBaseInfo.TCMCar", "c-dev-tcm-hrdev"),
    // 海信排更
    WAYBILL_SHEDULE_SHIFTS("unitfyEnum.TCMCaptainAttendance.tcmWaybillSheduleShiftsData", "c-dev-tcm-hrdev"),
    // 本地排更
    SHEDULE_SHIFTS("unitfyEnum.TCMCaptainAttendance.tcmSheduleShiftsData", "c-dev-tcm-hrdev"),
    // 車長假期工作記錄
    TCM_DRIVER_HOLIDAY_WORK_RECORD("TCMCaptainAttendance.CJ26030001.tcmDriverHolidayWorkRecord", "c-dev-tcm-hrdev"),
    // 車長超時工作記錄
    TCM_DRIVER_OVERTIME_RECORD("TCMCaptainAttendance.CJ26030001.tcmDriverOvertimeRecord", "c-dev-tcm-hrdev"),
    // 巴型考勤数据
    TCM_BA_TYPE_ATTENDANCE_DATA("unitfyEnum.TCMCaptainAttendance.tcmBaTypeAttendanceData", "c-dev-tcm-hrdev"),
    // 考勤修正
    ATTENDANCE_CORRECTION("unitfyEnum.TCMCaptainAttendance.tcmAttendanceCorrection", "c-dev-tcm-hrdev"),
    // 考勤計算
    ATTENDANCE_CALCULATION("unitfyEnum.TCMCaptainAttendance.tcmAttendanceCalculation", "c-dev-tcm-hrdev"),
    // 考勤统计
    ATTENDANCE_STATISTICS("unitfyEnum.TCMCaptainAttendance.tcmAttendanceStatistics", "c-dev-tcm-hrdev");

    private final String url;
    private final String domainKey;

    UrlEnum(String url, String domainKey)
    {
        this.url = url;
        this.domainKey = domainKey;
    }

}
