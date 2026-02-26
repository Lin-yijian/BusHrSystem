package com.yonyou.ucf.mdf.sample.enums;

import lombok.Getter;

@Getter
public enum UrlEnum {

    CAR_INFO("XSDcar.XSDcar.csd_car_info", "developplatform"),
    CAR_INFO_HISTORY("XSDcar.XSDcar.csd_car_info_history", "developplatform");

    private final String url;
    private final String domainKey;

    UrlEnum(String url, String domainKey)
    {
        this.url = url;
        this.domainKey = domainKey;
    }

}
