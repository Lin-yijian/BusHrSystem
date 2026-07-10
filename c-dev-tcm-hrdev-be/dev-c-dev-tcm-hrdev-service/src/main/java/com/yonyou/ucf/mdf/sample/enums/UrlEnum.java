package com.yonyou.ucf.mdf.sample.enums;

import lombok.Getter;

@Getter
public enum UrlEnum {

    ROUTE("TCMBaseInfo.TCMBaseInfo.TCMRoute", "c-dev-tcm-hrdev"),
    HISENSE_ATTENDANCE("unitfyEnum.TCMCaptainAttendance.tcmHisenseAttendanceData", "c-dev-tcm-hrdev"),
    CAR_INFO("TCMBaseInfo.TCMBaseInfo.TCMCar", "c-dev-tcm-hrdev");

    private final String url;
    private final String domainKey;

    UrlEnum(String url, String domainKey)
    {
        this.url = url;
        this.domainKey = domainKey;
    }

}
