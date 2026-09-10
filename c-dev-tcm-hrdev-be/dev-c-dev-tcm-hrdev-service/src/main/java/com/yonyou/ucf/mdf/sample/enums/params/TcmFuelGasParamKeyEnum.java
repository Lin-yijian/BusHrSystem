package com.yonyou.ucf.mdf.sample.enums.params;

import lombok.Getter;

import java.util.Arrays;

/**
 * 参数管理(TCMParamManage) - 入油入氣(TCM_FUEL_GAS_DATA_RECORD) 模块参数键枚举
 * <p>枚举项的 code 对应 TCMParamManage.key 字段；所属 module = {@link com.yonyou.ucf.mdf.sample.enums.ModuleEnum#TCM_FUEL_GAS_DATA_RECORD}.getCode()
 * <p>
 */
@Getter
public enum TcmFuelGasParamKeyEnum implements ParamKey {

    /** 异常油耗阈值（升/百公里） */
    ABNORMAL_FUEL_CONSUMPTION("ABNORMAL_FUEL_CONSUMPTION", "異常油耗閾值(升/百公里)", ParamKey.CHECK_TYPE_DECIMAL + ":0-100"),
    /** LNG 气压阈值（MPa） */
    LNG_PRESSURE_THRESHOLD("LNG_PRESSURE_THRESHOLD", "LNG氣壓閾值(MPa)", ParamKey.CHECK_TYPE_DECIMAL + ":0-10");

    private final String code;
    private final String name;
    /** 参数值格式校验类型（见 ParamKey CHECK_TYPE_* 常量） */
    private final String checkType;

    TcmFuelGasParamKeyEnum(String code, String name, String checkType) {
        this.code = code;
        this.name = name;
        this.checkType = checkType;
    }

    public static TcmFuelGasParamKeyEnum ofCode(String code) {
        if (code == null || code.isEmpty()) return null;
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
