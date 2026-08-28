/*--------------------JavaPOJO for 車長超時工作記錄(tcmDriverOvertimeRecord)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 車長超時工作記錄
 * @Date 2026-07-14 13:43:01
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMCaptainAttendance.CJ26030001.tcmDriverOvertimeRecord", domain = "c-dev-tcm-hrdev")
public class TcmDriverOvertimeRecord extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMCaptainAttendance.CJ26030001.tcmDriverOvertimeRecord";
    public static final String OCC_LEADER = "occ_leader";
    public static final String OVERTIME_DATE = "overtime_date";
    public static final String TASK_NO = "task_no";
    public static final String OVERTIME_HOURS = "overtime_hours";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String BUS_PLATE_NO = "bus_plate_no";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String SCHEDULE_END_TIME = "schedule_end_time";
    public static final String ACTUAL_END_TIME = "actual_end_time";
    public static final String VEHICLE_SWAP_ALLOWANCE = "vehicle_swap_allowance";
    public static final String OVERTIME_REASON = "overtime_reason";
    public static final String WORK_SHIFT_ALLOWANCE = "work_shift_allowance";
    public static final String ACTUAL_START_TIME = "actual_start_time";
    public static final String WORK_SHIFT_TIME = "work_shift_time";
    public static final String SCHEDULE_START_TIME = "schedule_start_time";
    public static final String NIGHT_SHIFT_ALLOWANCE = "night_shift_allowance";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* OCC組長 */
    private String occ_leader;
    /* 加班日期 */
    private String overtime_date;
    /* 任務編號 */
    private String task_no;
    /* 加班時間 */
    private String overtime_hours;
    /* 車長工號 */
    private String employee_id;
    /* 巴士車號 */
    private String bus_plate_no;
    /* 澳巴工号 */
    private String waybill_employee_id;
    /* 編定下班時間 */
    private String schedule_end_time;
    /* 實際下班時間 */
    private String actual_end_time;
    /* 交取車津貼 */
    private BigDecimal vehicle_swap_allowance;
    /* 加班原因 */
    private String overtime_reason;
    /* 上下更津貼 */
    private BigDecimal work_shift_allowance;
    /* 實際上班時間 */
    private String actual_start_time;
    /* 上下班時間 */
    private String work_shift_time;
    /* 編定上班時間 */
    private String schedule_start_time;
    /* 夜更津貼 */
    private BigDecimal night_shift_allowance;
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

    public void setOcc_leader(String occ_leader) {
        this.occ_leader = occ_leader;
    }

    public void setOvertime_date(String overtime_date) {
        this.overtime_date = overtime_date;
    }

    public void setTask_no(String task_no) {
        this.task_no = task_no;
    }

    public void setOvertime_hours(String overtime_hours) {
        this.overtime_hours = overtime_hours;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public void setBus_plate_no(String bus_plate_no) {
        this.bus_plate_no = bus_plate_no;
    }

    public void setWaybill_employee_id(String waybill_employee_id) {
        this.waybill_employee_id = waybill_employee_id;
    }

    public void setSchedule_end_time(String schedule_end_time) {
        this.schedule_end_time = schedule_end_time;
    }

    public void setActual_end_time(String actual_end_time) {
        this.actual_end_time = actual_end_time;
    }

    public void setVehicle_swap_allowance(BigDecimal vehicle_swap_allowance) {
        this.vehicle_swap_allowance = vehicle_swap_allowance;
    }

    public void setOvertime_reason(String overtime_reason) {
        this.overtime_reason = overtime_reason;
    }

    public void setWork_shift_allowance(BigDecimal work_shift_allowance) {
        this.work_shift_allowance = work_shift_allowance;
    }

    public void setActual_start_time(String actual_start_time) {
        this.actual_start_time = actual_start_time;
    }

    public void setWork_shift_time(String work_shift_time) {
        this.work_shift_time = work_shift_time;
    }

    public void setSchedule_start_time(String schedule_start_time) {
        this.schedule_start_time = schedule_start_time;
    }

    public void setNight_shift_allowance(BigDecimal night_shift_allowance) {
        this.night_shift_allowance = night_shift_allowance;
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

    public String getOcc_leader() {
        return occ_leader;
    }

    public String getOvertime_date() {
        return overtime_date;
    }

    public String getTask_no() {
        return task_no;
    }

    public String getOvertime_hours() {
        return overtime_hours;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public String getBus_plate_no() {
        return bus_plate_no;
    }

    public String getWaybill_employee_id() {
        return waybill_employee_id;
    }

    public String getSchedule_end_time() {
        return schedule_end_time;
    }

    public String getActual_end_time() {
        return actual_end_time;
    }

    public BigDecimal getVehicle_swap_allowance() {
        return vehicle_swap_allowance;
    }

    public String getOvertime_reason() {
        return overtime_reason;
    }

    public BigDecimal getWork_shift_allowance() {
        return work_shift_allowance;
    }

    public String getActual_start_time() {
        return actual_start_time;
    }

    public String getWork_shift_time() {
        return work_shift_time;
    }

    public String getSchedule_start_time() {
        return schedule_start_time;
    }

    public BigDecimal getNight_shift_allowance() {
        return night_shift_allowance;
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
