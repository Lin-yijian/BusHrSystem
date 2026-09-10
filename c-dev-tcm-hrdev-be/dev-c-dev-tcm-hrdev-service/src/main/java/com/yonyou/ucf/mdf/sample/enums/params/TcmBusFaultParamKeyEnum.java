package com.yonyou.ucf.mdf.sample.enums.params;

import lombok.Getter;

import java.util.Arrays;

/**
 * 参数管理(TCMParamManage) - 巴士故障(TCM_BUS_FAULT_DATA_RECORD) 模块参数键枚举
 * <p>枚举项的 code 对应 TCMParamManage.key 字段；所属 module = {@link com.yonyou.ucf.mdf.sample.enums.ModuleEnum#TCM_BUS_FAULT_DATA_RECORD}.getCode()
 * <p>
 */
@Getter
public enum TcmBusFaultParamKeyEnum implements ParamKey {

    /** 故障自动上报启用 */
    AUTO_REPORT_ENABLE("AUTO_REPORT_ENABLE", "故障自動上報啟用", ParamKey.CHECK_TYPE_STRING),
    /** 严重故障码列表（逗号分隔） */
    SERIOUS_FAULT_CODES("SERIOUS_FAULT_CODES", "嚴重故障碼列表", ParamKey.CHECK_TYPE_STRING);

    private final String code;
    private final String name;
    /** 参数值格式校验类型（见 ParamKey CHECK_TYPE_* 常量） */
    private final String checkType;

    TcmBusFaultParamKeyEnum(String code, String name, String checkType) {
        this.code = code;
        this.name = name;
        this.checkType = checkType;
    }

    public static TcmBusFaultParamKeyEnum ofCode(String code) {
        if (code == null || code.isEmpty()) return null;
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
