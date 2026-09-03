package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 考勤修正
 * @Date 2026-07-17 15:33:30
 * @since 2023/11/28
 **/
@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmAttendanceCorrection", domain = "c-dev-tcm-hrdev")
public class TcmAttendanceCorrection extends SuperDO implements ILogicDelete {
    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmAttendanceCorrection";
    public static final String DATE = "date";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String DRIVER_NAME = "driver_name";
    public static final String TASK_CODE = "task_code";
    public static final String KQ_ONE_DEPARTURE_TIME = "kq_one_departure_time";
    public static final String KQ_ONE_RETURN_TIME = "kq_one_return_time";
    public static final String KQ_TWO_DEPARTURE_TIME = "kq_two_departure_time";
    public static final String KQ_TWO_RETURN_TIME = "kq_two_return_time";
    public static final String PG_ONE_DEPARTURE_TIME = "pg_one_departure_time";
    public static final String PG_ONE_RETURN_TIME = "pg_one_return_time";
    public static final String PG_TWO_DEPARTURE_TIME = "pg_two_departure_time";
    public static final String PG_TWO_RETURN_TIME = "pg_two_return_time";
    public static final String PG_ONE_PARKING_SPACE = "pg_one_parking_space";
    public static final String JL_ONE_DEPARTURE_TIME = "jl_one_departure_time";
    public static final String JL_ONE_RETURN_TIME = "jl_one_return_time";
    public static final String JL_TWO_DEPARTURE_TIME = "jl_two_departure_time";
    public static final String JL_TWO_RETURN_TIME = "jl_two_return_time";
    public static final String ONE_LICENSE_PLATE = "one_license_plate";
    public static final String ONE_TIME = "one_time";
    public static final String JL_VALID_TIME = "jl_valid_time";
    public static final String TWO_LICENSE_PLATE = "two_license_plate";
    public static final String TWO_TIME = "two_time";
    public static final String OVERTIME_TIME = "overtime_time";
    public static final String LATE_DURATION = "late_duration";
    public static final String ONE_EARLY_LEAVE_DURATION = "one_early_leave_duration";
    public static final String TWO_EARLY_LEAVE_DURATION = "two_early_leave_duration";
    public static final String LATESTATUS = "lateStatus";
    public static final String EARLYLEAVESTATUS = "earlyLeaveStatus";
    public static final String OVERTIME_DURATION = "overtime_duration";
    public static final String LATE_AND_EARLY_LEAVE_DURATION = "late_and_early_leave_duration";
    public static final String OVERTIMESTATUS = "overtimeStatus";
    public static final String LATE_AND_EARLY_LEAVE_COUNT = "late_and_early_leave_count";
    public static final String MEAL_DURATION = "meal_duration";
    public static final String MEAL_COUNT = "meal_count";
    public static final String SHIFT_CHANGE_COUNT = "shift_change_count";
    public static final String VEHICLE_HANDOVER_COUNT = "vehicle_handover_count";
    public static final String LATE_NIGHT_DURATION = "late_night_duration";
    public static final String REFUEL_COUNT = "refuel_count";
    public static final String GAS_REFILL_COUNT = "gas_refill_count";
    public static final String BUS_FAULT = "bus_fault";
    public static final String CONTROL_CENTER_LOG = "control_center_log";
    public static final String ABSENCE_CATEGORY = "absence_category";
    public static final String ABSENCE_REASON = "absence_reason";
    public static final String ABSENCE_RECORD_COUNT = "absence_record_count";
    public static final String IS_NIGHT_SHIFT = "is_night_shift";
    public static final String SUBSTITUTE_SHIFT_TYPE = "substitute_shift_type";
    public static final String IS_WORKING_ON_WEEKLY_HOLIDAY = "is_working_on_weekly_holiday";
    public static final String SHIFT_CHANGE_ALLOWANCE = "shift_change_allowance";
    public static final String MINIBUS_DRIVING_HOURS = "minibus_driving_hours";
    public static final String MEDIUM_BUS_DRIVING_HOURS = "medium_bus_driving_hours";
    public static final String LARGE_BUS_DRIVING_HOURS = "large_bus_driving_hours";
    public static final String EXTRA_LARGE_BUS_DRIVING_HOURS = "extra_large_bus_driving_hours";
    public static final String MINIBUS_OVERTIME_HOURS = "minibus_overtime_hours";
    public static final String MEDIUM_BUS_OVERTIME_HOURS = "medium_bus_overtime_hours";
    public static final String LARGE_BUS_OVERTIME_HOURS = "large_bus_overtime_hours";
    public static final String EXTRA_LARGE_BUS_OVERTIME_HOURS = "extra_large_bus_overtime_hours";
    public static final String MINIBUS_EXTRA_HOURS = "minibus_extra_hours";
    public static final String MEDIUM_BUS_EXTRA_HOURS = "medium_bus_extra_hours";
    public static final String LARGE_BUS_EXTRA_HOURS = "large_bus_extra_hours";
    public static final String EXTRA_LARGE_BUS_EXTRA_HOURS = "extra_large_bus_extra_hours";
    public static final String ATTENDANCE_MODIFICATION_REMARKS = "attendance_modification_remarks";
    public static final String ANOMALY_VERIFICATION = "anomaly_verification";
    public static final String TCMBARTYPEHOURSTATUS = "tcmBarTypeHourStatus";
    public static final String IS_LESS_THAN_8_HOURS = "is_less_than_8_hours";
    public static final String IS_LATE_OR_EARLY_LEAVE = "is_late_or_early_leave";
    public static final String MINIBUS_BUS_TYPE_RPM = "minibus_bus_type_rpm";
    public static final String MEDIUM_BUS_TYPE_RPM = "medium_bus_type_rpm";
    public static final String LARGE_BUS_TYPE_RPM = "large_bus_type_rpm";
    public static final String EXTRA_LARGE_BUS_TYPE_RPM = "extra_large_bus_type_rpm";
    public static final String MINIBUS_BASE_HOURS = "minibus_base_hours";
    public static final String MEDIUM_BUS_BASE_HOURS = "medium_bus_base_hours";
    public static final String LARGE_BUS_BASE_HOURS = "large_bus_base_hours";
    public static final String EXTRA_LARGE_BUS_BASE_HOURS = "extra_large_bus_base_hours";
    public static final String BUS_TYPE_RPM_ORDER = "bus_type_rpm_order";
    public static final String BUS_TYPE_RPM = "bus_type_rpm";
    public static final String ONE_LATE_DURATION = "one_late_duration";
    public static final String TWO_LATE_DURATION = "two_late_duration";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String TCMATTENDANCEABSENCEDATARECORDLIST = "tcmAttendanceAbsenceDataRecordList";
    public static final String TCMATTENDANCECONTROLCENTERLOGLIST = "tcmAttendanceControlCenterLogList";
    public static final String TCMATTENDANCEDRIVEROVERTIMERECORDLIST = "tcmAttendanceDriverOvertimeRecordList";
    public static final String YTENANTID = "ytenantId";

    /* 日期 */
    private String date;
    /* 工號 */
    private String employee_id;
    /* 澳巴工号 */
    private String waybill_employee_id;
    /* 司機姓名 */
    private String driver_name;
    /* 任務代號 */
    private String task_code;
    /* 考勤數據一更發車時間 */
    private Date kq_one_departure_time;
    /* 考勤數據一更收車時間 */
    private Date kq_one_return_time;
    /* 考勤數據二更發車時間 */
    private Date kq_two_departure_time;
    /* 考勤數據二更收車時間 */
    private Date kq_two_return_time;
    /* 排更數據一更發車 */
    private Date pg_one_departure_time;
    /* 排更數據一更收車 */
    private Date pg_one_return_time;
    /* 排更數據二更發車 */
    private Date pg_two_departure_time;
    /* 排更數據二更收車 */
    private Date pg_two_return_time;
    /* 排更數據一更車位 */
    private String pg_one_parking_space;
    /* 計糧排更一更發車 */
    private Date jl_one_departure_time;
    /* 計糧排更一更收車 */
    private Date jl_one_return_time;
    /* 計糧排更二更發車 */
    private Date jl_two_departure_time;
    /* 計糧排更二更收車 */
    private Date jl_two_return_time;
    /* 一更車牌 */
    private String one_license_plate;
    /* 一更時長（含用膳） */
    private String one_time;
    /* 計糧有效工時（扣除用膳） */
    private String jl_valid_time;
    /* 二更車牌 */
    private String two_license_plate;
    /* 二更時長（含用膳） */
    private String two_time;
    /* 加班工時 */
    private String overtime_time;
    /* 遲到時長 */
    private String late_duration;
    /* 一更早退時長 */
    private String one_early_leave_duration;
    /* 二更早退時長 */
    private String two_early_leave_duration;
    /* 遲到狀態 */
    private String lateStatus;
    /* 早退狀態 */
    private String earlyLeaveStatus;
    /* 超時時長 */
    private String overtime_duration;
    /* 遲到早退時長（遲到早退扣款） */
    private String late_and_early_leave_duration;
    /* 超時狀態 */
    private String overtimeStatus;
    /* 遲到早退次數（勤工獎） */
    private Integer late_and_early_leave_count;
    /* 用膳時長 */
    private String meal_duration;
    /* 用膳次數 */
    private Integer meal_count;
    /* 上下更次數 */
    private Integer shift_change_count;
    /* 交取車次數 */
    private Integer vehicle_handover_count;
    /* 深宵時長 */
    private String late_night_duration;
    /* 加油次數 */
    private Integer refuel_count;
    /* 加氣次數 */
    private Integer gas_refill_count;
    /* 巴士故障 */
    private String bus_fault;
    /* 控制中心日志 */
    private String control_center_log;
    /* 缺勤类型 */
    private String absence_category;
    /* 缺勤原因 */
    private String absence_reason;
    /* 缺勤记录条数 */
    private Integer absence_record_count;
    /* 是否夜更 */
    private String is_night_shift;
    /* 頂更類型 */
    private String substitute_shift_type;
    /* 是否周假上班 */
    private String is_working_on_weekly_holiday;
    /* 上下更津貼 */
    private String shift_change_allowance;
    /* 小巴行車工時 */
    private String minibus_driving_hours;
    /* 中巴行車工時 */
    private String medium_bus_driving_hours;
    /* 大巴行車工時 */
    private String large_bus_driving_hours;
    /* 特大巴行車工時 */
    private String extra_large_bus_driving_hours;
    /* 小巴加班工時 */
    private String minibus_overtime_hours;
    /* 中巴加班工時 */
    private String medium_bus_overtime_hours;
    /* 大巴加班工時 */
    private String large_bus_overtime_hours;
    /* 特大巴加班工時 */
    private String extra_large_bus_overtime_hours;
    /* 小巴額外工時 */
    private String minibus_extra_hours;
    /* 中巴額外工時 */
    private String medium_bus_extra_hours;
    /* 大巴額外工時 */
    private String large_bus_extra_hours;
    /* 特大巴額外工時 */
    private String extra_large_bus_extra_hours;
    /* 考勤修改備註 */
    private String attendance_modification_remarks;
    /* 異常核對 */
    private String anomaly_verification;
    /* 巴型時數狀態 */
    private String tcmBarTypeHourStatus;
    /* 是否不足8小時 */
    private String is_less_than_8_hours;
    /* 是否遲到早退 */
    private String is_late_or_early_leave;
    /* 小巴巴型轉數 */
    private BigDecimal minibus_bus_type_rpm;
    /* 中巴巴型轉數 */
    private BigDecimal medium_bus_type_rpm;
    /* 大巴巴型轉數 */
    private BigDecimal large_bus_type_rpm;
    /* 特大巴巴型轉數 */
    private BigDecimal extra_large_bus_type_rpm;
    /* 小巴基本工時 */
    private String minibus_base_hours;
    /* 中巴基本工時 */
    private String medium_bus_base_hours;
    /* 大巴基本工時 */
    private String large_bus_base_hours;
    /* 特大巴基本工時 */
    private String extra_large_bus_base_hours;
    /* 巴型類型 */
    private String bus_type_rpm_order;
    /* 巴型轉數 */
    private String bus_type_rpm;
    /* 一更遲到時長 */
    private String one_late_duration;
    /* 二更遲到時長 */
    private String two_late_duration;
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
    /* 考勤修正-缺勤記錄 */
    private List<TcmAttendanceAbsenceDataRecord> tcmAttendanceAbsenceDataRecordList;
    /* 考勤修正子表-控制中心日志 */
    private List<TcmAttendanceControlCenterLog> tcmAttendanceControlCenterLogList;
    /* 考勤修正-超時工作記錄 */
    private List<TcmAttendanceDriverOvertimeRecord> tcmAttendanceDriverOvertimeRecordList;
    /* 租户id */
    private String ytenantId;


    public String getOne_late_duration() {
        return one_late_duration;
    }

    public void setOne_late_duration(String one_late_duration) {
        this.one_late_duration = one_late_duration;
    }

    public String getTwo_late_duration() {
        return two_late_duration;
    }

    public void setTwo_late_duration(String two_late_duration) {
        this.two_late_duration = two_late_duration;
    }

    public String getBus_type_rpm_order() {
        return bus_type_rpm_order;
    }

    public void setBus_type_rpm_order(String bus_type_rpm_order) {
        this.bus_type_rpm_order = bus_type_rpm_order;
    }

    public String getBus_type_rpm() {
        return bus_type_rpm;
    }

    public void setBus_type_rpm(String bus_type_rpm) {
        this.bus_type_rpm = bus_type_rpm;
    }

    public String getMinibus_base_hours() {
        return minibus_base_hours;
    }

    public void setMinibus_base_hours(String minibus_base_hours) {
        this.minibus_base_hours = minibus_base_hours;
    }

    public String getMedium_bus_base_hours() {
        return medium_bus_base_hours;
    }

    public void setMedium_bus_base_hours(String medium_bus_base_hours) {
        this.medium_bus_base_hours = medium_bus_base_hours;
    }

    public String getLarge_bus_base_hours() {
        return large_bus_base_hours;
    }

    public void setLarge_bus_base_hours(String large_bus_base_hours) {
        this.large_bus_base_hours = large_bus_base_hours;
    }

    public String getExtra_large_bus_base_hours() {
        return extra_large_bus_base_hours;
    }

    public void setExtra_large_bus_base_hours(String extra_large_bus_base_hours) {
        this.extra_large_bus_base_hours = extra_large_bus_base_hours;
    }

    public BigDecimal getMinibus_bus_type_rpm() {
        return minibus_bus_type_rpm;
    }

    public void setMinibus_bus_type_rpm(BigDecimal minibus_bus_type_rpm) {
        this.minibus_bus_type_rpm = minibus_bus_type_rpm;
    }

    public BigDecimal getMedium_bus_type_rpm() {
        return medium_bus_type_rpm;
    }

    public void setMedium_bus_type_rpm(BigDecimal medium_bus_type_rpm) {
        this.medium_bus_type_rpm = medium_bus_type_rpm;
    }

    public BigDecimal getLarge_bus_type_rpm() {
        return large_bus_type_rpm;
    }

    public void setLarge_bus_type_rpm(BigDecimal large_bus_type_rpm) {
        this.large_bus_type_rpm = large_bus_type_rpm;
    }

    public BigDecimal getExtra_large_bus_type_rpm() {
        return extra_large_bus_type_rpm;
    }

    public void setExtra_large_bus_type_rpm(BigDecimal extra_large_bus_type_rpm) {
        this.extra_large_bus_type_rpm = extra_large_bus_type_rpm;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public void setWaybill_employee_id(String waybill_employee_id) {
        this.waybill_employee_id = waybill_employee_id;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public void setTask_code(String task_code) {
        this.task_code = task_code;
    }

    public void setKq_one_departure_time(Date kq_one_departure_time) {
        this.kq_one_departure_time = kq_one_departure_time;
    }

    public void setKq_one_return_time(Date kq_one_return_time) {
        this.kq_one_return_time = kq_one_return_time;
    }

    public void setKq_two_departure_time(Date kq_two_departure_time) {
        this.kq_two_departure_time = kq_two_departure_time;
    }

    public void setKq_two_return_time(Date kq_two_return_time) {
        this.kq_two_return_time = kq_two_return_time;
    }

    public void setPg_one_departure_time(Date pg_one_departure_time) {
        this.pg_one_departure_time = pg_one_departure_time;
    }

    public void setPg_one_return_time(Date pg_one_return_time) {
        this.pg_one_return_time = pg_one_return_time;
    }

    public void setPg_two_departure_time(Date pg_two_departure_time) {
        this.pg_two_departure_time = pg_two_departure_time;
    }

    public void setPg_two_return_time(Date pg_two_return_time) {
        this.pg_two_return_time = pg_two_return_time;
    }

    public void setPg_one_parking_space(String pg_one_parking_space) {
        this.pg_one_parking_space = pg_one_parking_space;
    }

    public void setJl_one_departure_time(Date jl_one_departure_time) {
        this.jl_one_departure_time = jl_one_departure_time;
    }

    public void setJl_one_return_time(Date jl_one_return_time) {
        this.jl_one_return_time = jl_one_return_time;
    }

    public void setJl_two_departure_time(Date jl_two_departure_time) {
        this.jl_two_departure_time = jl_two_departure_time;
    }

    public void setJl_two_return_time(Date jl_two_return_time) {
        this.jl_two_return_time = jl_two_return_time;
    }

    public void setOne_license_plate(String one_license_plate) {
        this.one_license_plate = one_license_plate;
    }

    public void setOne_time(String one_time) {
        this.one_time = one_time;
    }

    public void setJl_valid_time(String jl_valid_time) {
        this.jl_valid_time = jl_valid_time;
    }

    public void setTwo_license_plate(String two_license_plate) {
        this.two_license_plate = two_license_plate;
    }

    public void setTwo_time(String two_time) {
        this.two_time = two_time;
    }

    public void setOvertime_time(String overtime_time) {
        this.overtime_time = overtime_time;
    }

    public void setLate_duration(String late_duration) {
        this.late_duration = late_duration;
    }

    public void setOne_early_leave_duration(String one_early_leave_duration) {
        this.one_early_leave_duration = one_early_leave_duration;
    }

    public void setTwo_early_leave_duration(String two_early_leave_duration) {
        this.two_early_leave_duration = two_early_leave_duration;
    }

    public void setLateStatus(String lateStatus) {
        this.lateStatus = lateStatus;
    }

    public void setEarlyLeaveStatus(String earlyLeaveStatus) {
        this.earlyLeaveStatus = earlyLeaveStatus;
    }

    public void setOvertime_duration(String overtime_duration) {
        this.overtime_duration = overtime_duration;
    }

    public void setLate_and_early_leave_duration(String late_and_early_leave_duration) {
        this.late_and_early_leave_duration = late_and_early_leave_duration;
    }

    public void setOvertimeStatus(String overtimeStatus) {
        this.overtimeStatus = overtimeStatus;
    }

    public void setLate_and_early_leave_count(Integer late_and_early_leave_count) {
        this.late_and_early_leave_count = late_and_early_leave_count;
    }

    public void setMeal_duration(String meal_duration) {
        this.meal_duration = meal_duration;
    }

    public void setMeal_count(Integer meal_count) {
        this.meal_count = meal_count;
    }

    public void setShift_change_count(Integer shift_change_count) {
        this.shift_change_count = shift_change_count;
    }

    public void setVehicle_handover_count(Integer vehicle_handover_count) {
        this.vehicle_handover_count = vehicle_handover_count;
    }

    public void setLate_night_duration(String late_night_duration) {
        this.late_night_duration = late_night_duration;
    }

    public void setRefuel_count(Integer refuel_count) {
        this.refuel_count = refuel_count;
    }

    public void setGas_refill_count(Integer gas_refill_count) {
        this.gas_refill_count = gas_refill_count;
    }

    public void setBus_fault(String bus_fault) {
        this.bus_fault = bus_fault;
    }

    public void setControl_center_log(String control_center_log) {
        this.control_center_log = control_center_log;
    }

    public void setAbsence_category(String absence_category) {
        this.absence_category = absence_category;
    }

    public void setAbsence_reason(String absence_reason) {
        this.absence_reason = absence_reason;
    }

    public void setAbsence_record_count(Integer absence_record_count) {
        this.absence_record_count = absence_record_count;
    }

    public void setIs_night_shift(String is_night_shift) {
        this.is_night_shift = is_night_shift;
    }

    public void setSubstitute_shift_type(String substitute_shift_type) {
        this.substitute_shift_type = substitute_shift_type;
    }

    public void setIs_working_on_weekly_holiday(String is_working_on_weekly_holiday) {
        this.is_working_on_weekly_holiday = is_working_on_weekly_holiday;
    }

    public void setShift_change_allowance(String shift_change_allowance) {
        this.shift_change_allowance = shift_change_allowance;
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

    public void setMinibus_overtime_hours(String minibus_overtime_hours) {
        this.minibus_overtime_hours = minibus_overtime_hours;
    }

    public void setMedium_bus_overtime_hours(String medium_bus_overtime_hours) {
        this.medium_bus_overtime_hours = medium_bus_overtime_hours;
    }

    public void setLarge_bus_overtime_hours(String large_bus_overtime_hours) {
        this.large_bus_overtime_hours = large_bus_overtime_hours;
    }

    public void setExtra_large_bus_overtime_hours(String extra_large_bus_overtime_hours) {
        this.extra_large_bus_overtime_hours = extra_large_bus_overtime_hours;
    }

    public void setMinibus_extra_hours(String minibus_extra_hours) {
        this.minibus_extra_hours = minibus_extra_hours;
    }

    public void setMedium_bus_extra_hours(String medium_bus_extra_hours) {
        this.medium_bus_extra_hours = medium_bus_extra_hours;
    }

    public void setLarge_bus_extra_hours(String large_bus_extra_hours) {
        this.large_bus_extra_hours = large_bus_extra_hours;
    }

    public void setExtra_large_bus_extra_hours(String extra_large_bus_extra_hours) {
        this.extra_large_bus_extra_hours = extra_large_bus_extra_hours;
    }

    public void setAttendance_modification_remarks(String attendance_modification_remarks) {
        this.attendance_modification_remarks = attendance_modification_remarks;
    }


    public void setAnomaly_verification(String anomaly_verification) {
        this.anomaly_verification = anomaly_verification;
    }

    public void setTcmBarTypeHourStatus(String tcmBarTypeHourStatus) {
        this.tcmBarTypeHourStatus = tcmBarTypeHourStatus;
    }

    public void setIs_less_than_8_hours(String is_less_than_8_hours) {
        this.is_less_than_8_hours = is_less_than_8_hours;
    }

    public void setIs_late_or_early_leave(String is_late_or_early_leave) {
        this.is_late_or_early_leave = is_late_or_early_leave;
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

    public void setTcmAttendanceAbsenceDataRecordList(List<TcmAttendanceAbsenceDataRecord> tcmAttendanceAbsenceDataRecordList) {
        this.tcmAttendanceAbsenceDataRecordList = tcmAttendanceAbsenceDataRecordList;
    }

    public void setTcmAttendanceControlCenterLogList(List<TcmAttendanceControlCenterLog> tcmAttendanceControlCenterLogList) {
        this.tcmAttendanceControlCenterLogList = tcmAttendanceControlCenterLogList;
    }

    public void setTcmAttendanceDriverOvertimeRecordList(List<TcmAttendanceDriverOvertimeRecord> tcmAttendanceDriverOvertimeRecordList) {
        this.tcmAttendanceDriverOvertimeRecordList = tcmAttendanceDriverOvertimeRecordList;
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

    public String getWaybill_employee_id() {
        return waybill_employee_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public String getTask_code() {
        return task_code;
    }

    public Date getKq_one_departure_time() {
        return kq_one_departure_time;
    }

    public Date getKq_one_return_time() {
        return kq_one_return_time;
    }

    public Date getKq_two_departure_time() {
        return kq_two_departure_time;
    }

    public Date getKq_two_return_time() {
        return kq_two_return_time;
    }

    public Date getPg_one_departure_time() {
        return pg_one_departure_time;
    }

    public Date getPg_one_return_time() {
        return pg_one_return_time;
    }

    public Date getPg_two_departure_time() {
        return pg_two_departure_time;
    }

    public Date getPg_two_return_time() {
        return pg_two_return_time;
    }

    public String getPg_one_parking_space() {
        return pg_one_parking_space;
    }

    public Date getJl_one_departure_time() {
        return jl_one_departure_time;
    }

    public Date getJl_one_return_time() {
        return jl_one_return_time;
    }

    public Date getJl_two_departure_time() {
        return jl_two_departure_time;
    }

    public Date getJl_two_return_time() {
        return jl_two_return_time;
    }

    public String getOne_license_plate() {
        return one_license_plate;
    }

    public String getOne_time() {
        return one_time;
    }

    public String getJl_valid_time() {
        return jl_valid_time;
    }

    public String getTwo_license_plate() {
        return two_license_plate;
    }

    public String getTwo_time() {
        return two_time;
    }

    public String getOvertime_time() {
        return overtime_time;
    }

    public String getLate_duration() {
        return late_duration;
    }

    public String getOne_early_leave_duration() {
        return one_early_leave_duration;
    }

    public String getTwo_early_leave_duration() {
        return two_early_leave_duration;
    }

    public String getLateStatus() {
        return lateStatus;
    }

    public String getEarlyLeaveStatus() {
        return earlyLeaveStatus;
    }

    public String getOvertime_duration() {
        return overtime_duration;
    }

    public String getLate_and_early_leave_duration() {
        return late_and_early_leave_duration;
    }

    public String getOvertimeStatus() {
        return overtimeStatus;
    }

    public Integer getLate_and_early_leave_count() {
        return late_and_early_leave_count;
    }

    public String getMeal_duration() {
        return meal_duration;
    }

    public Integer getMeal_count() {
        return meal_count;
    }

    public Integer getShift_change_count() {
        return shift_change_count;
    }

    public Integer getVehicle_handover_count() {
        return vehicle_handover_count;
    }

    public String getLate_night_duration() {
        return late_night_duration;
    }

    public Integer getRefuel_count() {
        return refuel_count;
    }

    public Integer getGas_refill_count() {
        return gas_refill_count;
    }

    public String getBus_fault() {
        return bus_fault;
    }

    public String getControl_center_log() {
        return control_center_log;
    }

    public String getAbsence_category() {
        return absence_category;
    }

    public String getAbsence_reason() {
        return absence_reason;
    }

    public Integer getAbsence_record_count() {
        return absence_record_count;
    }

    public String getIs_night_shift() {
        return is_night_shift;
    }

    public String getSubstitute_shift_type() {
        return substitute_shift_type;
    }

    public String getIs_working_on_weekly_holiday() {
        return is_working_on_weekly_holiday;
    }

    public String getShift_change_allowance() {
        return shift_change_allowance;
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

    public String getMinibus_overtime_hours() {
        return minibus_overtime_hours;
    }

    public String getMedium_bus_overtime_hours() {
        return medium_bus_overtime_hours;
    }

    public String getLarge_bus_overtime_hours() {
        return large_bus_overtime_hours;
    }

    public String getExtra_large_bus_overtime_hours() {
        return extra_large_bus_overtime_hours;
    }

    public String getMinibus_extra_hours() {
        return minibus_extra_hours;
    }

    public String getMedium_bus_extra_hours() {
        return medium_bus_extra_hours;
    }

    public String getLarge_bus_extra_hours() {
        return large_bus_extra_hours;
    }

    public String getExtra_large_bus_extra_hours() {
        return extra_large_bus_extra_hours;
    }

    public String getAttendance_modification_remarks() {
        return attendance_modification_remarks;
    }

    public String getAnomaly_verification() {
        return anomaly_verification;
    }

    public String getTcmBarTypeHourStatus() {
        return tcmBarTypeHourStatus;
    }

    public String getIs_less_than_8_hours() {
        return is_less_than_8_hours;
    }

    public String getIs_late_or_early_leave() {
        return is_late_or_early_leave;
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

    public List<TcmAttendanceAbsenceDataRecord> getTcmAttendanceAbsenceDataRecordList() {
        return tcmAttendanceAbsenceDataRecordList;
    }

    public List<TcmAttendanceControlCenterLog> getTcmAttendanceControlCenterLogList() {
        return tcmAttendanceControlCenterLogList;
    }

    public List<TcmAttendanceDriverOvertimeRecord> getTcmAttendanceDriverOvertimeRecordList() {
        return tcmAttendanceDriverOvertimeRecordList;
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
