package com.yonyou.ucf.mdf.sample.enums;

import lombok.Getter;

@Getter
public enum IndexType {

    SERVICE_INDEX("1", "服務獎指標"),
    SAFE_INDEX("2", "安全獎指標");

    private final String code;
    private final String name;

    IndexType(String code, String name)
    {
        this.code = code;
        this.name = name;
    }
}
