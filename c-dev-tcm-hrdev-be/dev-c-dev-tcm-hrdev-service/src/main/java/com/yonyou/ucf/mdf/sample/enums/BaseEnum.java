package com.yonyou.ucf.mdf.sample.enums;

import lombok.Getter;

/**
 * 基础枚举类
 * @author aze
 */

@Getter
public enum BaseEnum {

    /**
     * 数据来源 - 同步
     */
    SOURCE_TYPE_SYNC("1", "同步"),

    /**
     * 数据来源 - 人工
     */
    SOURCE_TYPE_MANUAL("2", "人工"),


    CHECK_STATUS_0("0","待複核"),
    CHECK_STATUS_1("1","日結完畢"),
    CHECK_STATUS_2("2","已確認"),

    AC_CHECK_STATUS_0("1","待核對"),
    AC_CHECK_STATUS_1("2","已核對"),
    AC_CHECK_STATUS_2("3","正常"),

    ERROR_CODE("1","异常"),

    NORMAL_CODE("2","正常"),

    TODO("1","待處理"),
    DONE("2","已處理"),

    /**
     * 逻辑删除 - 未删除
     */
    IS_NOT_DELETED("0", "未删除"),

    /**
     * 逻辑删除 - 已删除
     */
    IS_DELETED("1", "已删除"),

    /**
     * 同步状态 - 成功
     */
    SYNC_STATUS_SUCCESS("1", "成功"),
    /**
     * 同步状态 - 失败
     */
    SYNC_STATUS_FAILURE("2", "失败");

    private final String code;
    private final String name;

    BaseEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取Integer类型的code值
     * @return Integer类型的code
     */
    public Integer getIntCode() {
        return Integer.parseInt(this.code);
    }

}
