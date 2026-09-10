package com.yonyou.ucf.mdf.sample.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ModuleEnum {

    TCM_DRIVER_OVERTIME("TCM_DRIVER_OVERTIME", "車長超時工作"),
    TCM_ABSENCE_DATA_RECORD("TCM_ABSENCE_DATA_RECORD", "缺勤記錄"),
    TCM_RESERVE_CAPTAIN_ATTENDANCE_RECORD("TCM_RESERVE_CAPTAIN_ATTENDANCE_RECORD", "後備車長工作記錄"),
    TCM_BUS_FAULT_DATA_RECORD("TCM_BUS_FAULT_DATA_RECORD", "巴士故障"),
    TCM_CONTROL_CENTER_LOG("TCM_CONTROL_CENTER_LOG", "控制中心日誌"),
    TCM_FUEL_GAS_DATA_RECORD("TCM_FUEL_GAS_DATA_RECORD", "入油入氣"),
    TCM_HI_SENSE_ATTENDANCE("TCM_HI_SENSE_ATTENDANCE", "海信考勤"),
    TCM_ATTENDANCE_CHECK_RECORD("TCM_ATTENDANCE_CHECK_RECORD", "海信車載登陸"),
    TCM_SHEDULE_SHIFTS("ROSTERING", "本地排更"),
    TCM_ATTENDANCE_VERIFICATION("TCM_ATTENDANCE_VERIFICATION", "考勤核對");

    private final String code;
    private final String name;

    ModuleEnum(String code, String name)
    {
        this.code = code;
        this.name = name;
    }


    public static String getNameByCode(String code) {
        return Arrays.stream(ModuleEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().map(ModuleEnum::getName).orElse(null);
    }


    public static ModuleEnum getByCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (ModuleEnum moduleEnum : ModuleEnum.values()) {
            if (moduleEnum.getCode().equals(code)) {
                return moduleEnum;
            }
        }
        return null;
    }


}
