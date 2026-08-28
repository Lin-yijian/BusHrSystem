/*--------------------JavaPOJO for 考勤計算(tcmAttendanceCalculation)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 考勤計算
 * @Date 2026-07-21 19:31:33
 * @since 2023/11/28
 **/
@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmAttendanceCalculation", domain = "c-dev-tcm-hrdev")
public class TcmAttendanceCalculation extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmAttendanceCalculation";
    public static final String DATE = "date";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String EMPLOYEE_ID_CODE = "employee_id.code";
    public static final String NAME = "name";
    public static final String TASK_CODE = "task_code";
    public static final String DEPARTMENT_SUMMARY = "department_summary";
    public static final String ATTENDANCE_TYPE = "attendance_type";
    public static final String WORK_TIME = "work_time";
    public static final String LATE_EARLY_LEAVE_PERIOD = "late_early_leave_period";
    public static final String TOTAL_WORK_HOURS = "total_work_hours";
    public static final String OVERTIME_HOURS = "overtime_hours";
    public static final String STANDARD_WORK_HOURS = "standard_work_hours";
    public static final String LATE_EARLY_LEAVE_DURATION = "late_early_leave_duration";
    public static final String MEAL_COUNT = "meal_count";
    public static final String MEAL_DURATION = "meal_duration";
    public static final String VEHICLE_HANDOVER_COUNT = "vehicle_handover_count";
    public static final String REFUEL_COUNT = "refuel_count";
    public static final String GAS_REFILL_COUNT = "gas_refill_count";
    public static final String SHIFT_ALLOWANCE = "shift_allowance";
    public static final String NIGHT_SHIFT_ALLOWANCE = "night_shift_allowance";
    public static final String LATE_NIGHT_DURATION = "late_night_duration";
    public static final String SUBSTITUTE_SHIFT_TYPE = "substitute_shift_type";
    public static final String LATE_AND_EARLY_LEAVE_COUNT = "late_and_early_leave_count";
    public static final String IS_WORKING_ON_WEEKLY_HOLIDAY = "is_working_on_weekly_holiday";
    public static final String MINIBUS_DRIVING_HOURS = "minibus_driving_hours";
    public static final String MEDIUM_BUS_DRIVING_HOURS = "medium_bus_driving_hours";
    public static final String LARGE_BUS_DRIVING_HOURS = "large_bus_driving_hours";
    public static final String EXTRA_LARGE_BUS_DRIVING_HOURS = "extra_large_bus_driving_hours";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* 日期 */
    private String date;
    /* 工號 */
    private String employee_id;
    /* 姓名 */
    private String name;
    /* 任務代號 */
    private String task_code;
    /* 所屬匯總 */
    private String department_summary;
    /* 出勤類型 */
    private String attendance_type;
    /* 工作時間 */
    private String work_time;
    /* 遲到早退時段 */
    private String late_early_leave_period;
    /* 總工時 */
    private String total_work_hours;
    /* 加班時長 */
    private String overtime_hours;
    /* 標準工時 */
    private String standard_work_hours;
    /* 遲到早退時長 */
    private String late_early_leave_duration;
    /* 用膳次數 */
    private Integer meal_count;
    /* 用膳時長 */
    private String meal_duration;
    /* 交取車次數 */
    private Integer vehicle_handover_count;
    /* 加油次數 */
    private Integer refuel_count;
    /* 加氣次數 */
    private Integer gas_refill_count;
    /* 上下更津貼 */
    private Boolean shift_allowance;
    /* 夜更津貼 */
    private Boolean night_shift_allowance;
    /* 深宵時長 */
    private String late_night_duration;
    /* 頂更類型 */
    private String substitute_shift_type;
    /* 遲到早退次數 */
    private Integer late_and_early_leave_count;
    /* 是否周假上班 */
    private Boolean is_working_on_weekly_holiday;
    /* 小巴基本工時 */
    private String minibus_driving_hours;
    /* 中巴基本工時 */
    private String medium_bus_driving_hours;
    /* 大巴基本工時 */
    private String large_bus_driving_hours;
    /* 特大巴行車工時 */
    private String extra_large_bus_driving_hours;
    /* 编码 */
    private String code;
    /* 创建时间 */
    private Date createTime;
    /* 创建人 */
    private String creator;
    /* 逻辑删除 */
    private Short dr;
    /* id */
    private String id;
    /* 修改人 */
    private String modifier;
    /* 修改时间 */
    private Date modifyTime;
    /* pubts */
    private Date pubts;
    /* 租户id */
    private String ytenantId;

    public void setDate(String date) {
        this.date = date;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTask_code(String task_code) {
        this.task_code = task_code;
    }

    public void setDepartment_summary(String department_summary) {
        this.department_summary = department_summary;
    }

    public void setAttendance_type(String attendance_type) {
        this.attendance_type = attendance_type;
    }

    public void setWork_time(String work_time) {
        this.work_time = work_time;
    }

    public void setLate_early_leave_period(String late_early_leave_period) {
        this.late_early_leave_period = late_early_leave_period;
    }

    public void setTotal_work_hours(String total_work_hours) {
        this.total_work_hours = total_work_hours;
    }

    public void setOvertime_hours(String overtime_hours) {
        this.overtime_hours = overtime_hours;
    }

    public void setStandard_work_hours(String standard_work_hours) {
        this.standard_work_hours = standard_work_hours;
    }

    public void setLate_early_leave_duration(String late_early_leave_duration) {
        this.late_early_leave_duration = late_early_leave_duration;
    }

    public void setMeal_count(Integer meal_count) {
        this.meal_count = meal_count;
    }

    public void setMeal_duration(String meal_duration) {
        this.meal_duration = meal_duration;
    }

    public void setVehicle_handover_count(Integer vehicle_handover_count) {
        this.vehicle_handover_count = vehicle_handover_count;
    }

    public void setRefuel_count(Integer refuel_count) {
        this.refuel_count = refuel_count;
    }

    public void setGas_refill_count(Integer gas_refill_count) {
        this.gas_refill_count = gas_refill_count;
    }

    public void setShift_allowance(Boolean shift_allowance) {
        this.shift_allowance = shift_allowance;
    }

    public void setNight_shift_allowance(Boolean night_shift_allowance) {
        this.night_shift_allowance = night_shift_allowance;
    }

    public void setLate_night_duration(String late_night_duration) {
        this.late_night_duration = late_night_duration;
    }

    public void setSubstitute_shift_type(String substitute_shift_type) {
        this.substitute_shift_type = substitute_shift_type;
    }

    public void setLate_and_early_leave_count(Integer late_and_early_leave_count) {
        this.late_and_early_leave_count = late_and_early_leave_count;
    }

    public void setIs_working_on_weekly_holiday(Boolean is_working_on_weekly_holiday) {
        this.is_working_on_weekly_holiday = is_working_on_weekly_holiday;
    }

    public void setMinibus_driving_hours(String minibus_driving_hours) {
        this.minibus_driving_hours = minibus_driving_hours;
    }

    public void setMedium_bus_driving_hours(String medium_bus_driving_hours) {
        this.medium_bus_driving_hours = medium_bus_driving_hours;
    }

    public void setLarge_bus_driving_hours(String large_bus_driving_hours) {
        this.large_bus_driving_hours = large_bus_driving_hours;
    }

    public void setExtra_large_bus_driving_hours(String extra_large_bus_driving_hours) {
        this.extra_large_bus_driving_hours = extra_large_bus_driving_hours;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setDr(Short dr) {
        this.dr = dr;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public void setPubts(Date pubts) {
        this.pubts = pubts;
    }

    public void setYtenantId(String ytenantId) {
        this.ytenantId = ytenantId;
    }

    public String getDate() {
        return date;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public String getName() {
        return name;
    }

    public String getTask_code() {
        return task_code;
    }

    public String getDepartment_summary() {
        return department_summary;
    }

    public String getAttendance_type() {
        return attendance_type;
    }

    public String getWork_time() {
        return work_time;
    }

    public String getLate_early_leave_period() {
        return late_early_leave_period;
    }

    public String getTotal_work_hours() {
        return total_work_hours;
    }

    public String getOvertime_hours() {
        return overtime_hours;
    }

    public String getStandard_work_hours() {
        return standard_work_hours;
    }

    public String getLate_early_leave_duration() {
        return late_early_leave_duration;
    }

    public Integer getMeal_count() {
        return meal_count;
    }

    public String getMeal_duration() {
        return meal_duration;
    }

    public Integer getVehicle_handover_count() {
        return vehicle_handover_count;
    }

    public Integer getRefuel_count() {
        return refuel_count;
    }

    public Integer getGas_refill_count() {
        return gas_refill_count;
    }

    public Boolean getShift_allowance() {
        return shift_allowance;
    }

    public Boolean getNight_shift_allowance() {
        return night_shift_allowance;
    }

    public String getLate_night_duration() {
        return late_night_duration;
    }

    public String getSubstitute_shift_type() {
        return substitute_shift_type;
    }

    public Integer getLate_and_early_leave_count() {
        return late_and_early_leave_count;
    }

    public Boolean getIs_working_on_weekly_holiday() {
        return is_working_on_weekly_holiday;
    }

    public String getMinibus_driving_hours() {
        return minibus_driving_hours;
    }

    public String getMedium_bus_driving_hours() {
        return medium_bus_driving_hours;
    }

    public String getLarge_bus_driving_hours() {
        return large_bus_driving_hours;
    }

    public String getExtra_large_bus_driving_hours() {
        return extra_large_bus_driving_hours;
    }

    public String getCode() {
        return code;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public String getCreator() {
        return creator;
    }

    public Short getDr() {
        return dr;
    }

    public String getId() {
        return id;
    }

    public String getModifier() {
        return modifier;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public Date getPubts() {
        return pubts;
    }

    public String getYtenantId() {
        return ytenantId;
    }

    public String getFullName() {
        if (super.getFullName() != null && super.getFullName().indexOf("#PT#.") != -1) {
            return super.getFullName();
        } else {
            return ENTITY_NAME;
        }
    }
}
