package com.yonyou.ucf.mdf.sample.enums;

import lombok.Getter;

@Getter
public enum UrlEnum {

    ROUTE("TCMBaseInfo.TCMBaseInfo.TCMRoute", "c-dev-tcm-hrdev"),
    HISENSE_ATTENDANCE("unitfyEnum.TCMCaptainAttendance.tcmHisenseAttendanceData", "c-dev-tcm-hrdev"),
    WAYBILL_LOGIN_RECORD("TCMCaptainAttendance.CJ26030001.tcmHisenseWaybillLoginRecord", "c-dev-tcm-hrdev"),
    BD_STAFF("hred.staff.Staff", "hrcloud-staff-mgr"),
    CAR_INFO("TCMBaseInfo.TCMBaseInfo.TCMCar", "c-dev-tcm-hrdev"),
    WAYBILL_SHEDULE_SHIFTS("unitfyEnum.TCMCaptainAttendance.tcmWaybillSheduleShiftsData", "c-dev-tcm-hrdev");

    private final String url;
    private final String domainKey;

    UrlEnum(String url, String domainKey)
    {
        this.url = url;
        this.domainKey = domainKey;
    }

}
