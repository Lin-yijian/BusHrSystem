package com.yonyou.ucf.mdf.sample.enums;

import lombok.Getter;

@Getter
public enum TaskTypeEnum {

    /** 同步巴型轉數 */
    BXZS_SYNC("BXZS_SYNC", "同步巴型轉數"),

    /** 拉取海信車載機登錄記錄 */
    HXDL_SYNC("HXDL_SYNC", "拉取海信車載機登錄記錄"),

    /** 生成本地考勤 */
    HXKQ_MAKE("HXKQ_MAKE", "生成本地考勤"),

    /** 拉取海信考勤數據 */
    HXKQ_SYNC("HXKQ_SYNC", "拉取海信考勤數據");

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