package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ucf.mdf.sample.bill.entity.TCMParamManage;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.enums.params.ParamKey;
import com.yonyou.ucf.mdf.sample.enums.params.TcmAbsenceDataRecordParamKeyEnum;
import com.yonyou.ucf.mdf.sample.enums.params.TcmAttendanceCheckRecordParamKeyEnum;
import com.yonyou.ucf.mdf.sample.enums.params.TcmAttendanceVerificationParamKeyEnum;
import com.yonyou.ucf.mdf.sample.enums.params.TcmBusFaultParamKeyEnum;
import com.yonyou.ucf.mdf.sample.enums.params.TcmControlCenterLogParamKeyEnum;
import com.yonyou.ucf.mdf.sample.enums.params.TcmDriverOvertimeParamKeyEnum;
import com.yonyou.ucf.mdf.sample.enums.params.TcmFuelGasParamKeyEnum;
import com.yonyou.ucf.mdf.sample.enums.params.TcmHiSenseAttendanceParamKeyEnum;
import com.yonyou.ucf.mdf.sample.enums.params.TcmReserveCaptainParamKeyEnum;
import com.yonyou.ucf.mdf.sample.enums.params.TcmRosteringParamKeyEnum;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 参数管理(TCMParamManage) 单据插件：保存前按已定义规则校验参数。
 * <p>对应关系：
 * <ul>
 *   <li>TCMParamManage.module = {@link ModuleEnum#getCode()}</li>
 *   <li>TCMParamManage.key    = 各模块 *ParamKeyEnum 的 code（{@link ParamKey#getCode()}）</li>
 *   <li>TCMParamManage.value  = 按 {@link ParamKey#getCheckType()} 做格式/范围校验</li>
 * </ul>
 */
@BillPlugin(busiObj = "TCMParamManage")
@Slf4j
public class TCMParamManagePlugin extends AbstractBillPlugin {

    /** module code → 该模块 ParamKey 枚举的 ofCode 查找函数 */
    private static final Map<String, Function<String, ? extends ParamKey>> PARAM_KEY_LOOKUP = new HashMap<>();

    static {
        PARAM_KEY_LOOKUP.put(ModuleEnum.TCM_DRIVER_OVERTIME.getCode(), TcmDriverOvertimeParamKeyEnum::ofCode);
        PARAM_KEY_LOOKUP.put(ModuleEnum.TCM_ABSENCE_DATA_RECORD.getCode(), TcmAbsenceDataRecordParamKeyEnum::ofCode);
        PARAM_KEY_LOOKUP.put(ModuleEnum.TCM_RESERVE_CAPTAIN_ATTENDANCE_RECORD.getCode(), TcmReserveCaptainParamKeyEnum::ofCode);
        PARAM_KEY_LOOKUP.put(ModuleEnum.TCM_BUS_FAULT_DATA_RECORD.getCode(), TcmBusFaultParamKeyEnum::ofCode);
        PARAM_KEY_LOOKUP.put(ModuleEnum.TCM_CONTROL_CENTER_LOG.getCode(), TcmControlCenterLogParamKeyEnum::ofCode);
        PARAM_KEY_LOOKUP.put(ModuleEnum.TCM_FUEL_GAS_DATA_RECORD.getCode(), TcmFuelGasParamKeyEnum::ofCode);
        PARAM_KEY_LOOKUP.put(ModuleEnum.TCM_HI_SENSE_ATTENDANCE.getCode(), TcmHiSenseAttendanceParamKeyEnum::ofCode);
        PARAM_KEY_LOOKUP.put(ModuleEnum.TCM_ATTENDANCE_CHECK_RECORD.getCode(), TcmAttendanceCheckRecordParamKeyEnum::ofCode);
        PARAM_KEY_LOOKUP.put(ModuleEnum.TCM_SHEDULE_SHIFTS.getCode(), TcmRosteringParamKeyEnum::ofCode);
        PARAM_KEY_LOOKUP.put(ModuleEnum.TCM_ATTENDANCE_VERIFICATION.getCode(), TcmAttendanceVerificationParamKeyEnum::ofCode);
    }

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
        Object billDo = billContext.getBillDO();
        if (billDo instanceof TCMParamManage) {
            validateParam((TCMParamManage) billDo);
        }
    }

    /**
     * 保存前校验：
     * <ol>
     *   <li>module/key 必填</li>
     *   <li>module 必须是已登记的 ModuleEnum code</li>
     *   <li>key 必须命中该模块 *ParamKeyEnum 的枚举常量</li>
     *   <li>value 按枚举 checkType 做格式/范围校验（{@link ParamKey#checkValueForSave}，
     *       保存场景专用提示语）：空值抛"參數【name】(code) 的參數值不能為空，請填寫後再保存"；
     *       格式非法抛"參數【name】(code) 的參數值格式錯誤：value（原因），請修正後再保存"</li>
     * </ol>
     */
    private void validateParam(TCMParamManage data) {
        String module = data.getModule();
        String key = data.getKey();

        if (StringUtils.isBlank(module)) {
            throw new RuntimeException("所屬模塊(module)不能為空，請選擇後再保存");
        }
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("參數鍵(key)不能為空，請填寫後再保存");
        }

        Function<String, ? extends ParamKey> lookup = PARAM_KEY_LOOKUP.get(module);
        if (lookup == null) {
            throw new RuntimeException("未登記的參數模塊(module)=" + module + "，請在 TCMParamManagePlugin 中補錄對應 ParamKey 枚舉映射");
        }

        ParamKey keyEnum = lookup.apply(key);
        if (keyEnum == null) {
            throw new RuntimeException("參數鍵(key)=" + key + " 不屬於模塊 " + ModuleEnum.getNameByCode(module)
                    + "，請按對應 *ParamKeyEnum 枚舉維護後再保存");
        }

        // checkType 格式/范围校验（含空值拦截），保存场景提示语
        ParamKey.checkValueForSave(keyEnum, data.getValue());
    }
}
