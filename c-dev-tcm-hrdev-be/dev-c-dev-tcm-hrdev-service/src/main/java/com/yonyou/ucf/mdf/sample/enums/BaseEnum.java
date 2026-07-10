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

    /**
     * 數據核銷狀態
     */
    WRITE_OFF("1", "已核銷"),
    WRITE_OFF_HALF("2", "部分核銷"),
    WRITE_OFF_TODO("3", "未核銷"),

    /**
     * 反核销状态
     */
    BACK_WRITE_OFF_ZERO("0", "正常"),
    BACK_WRITE_OFF_ONE("1", "已回滚核銷"),

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
