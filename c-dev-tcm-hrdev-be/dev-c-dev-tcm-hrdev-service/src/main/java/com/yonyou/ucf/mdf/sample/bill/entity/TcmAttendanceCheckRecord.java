/*--------------------JavaPOJO for 考勤核對記錄(tcmAttendanceCheckRecord)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 考勤核對記錄
 * @Date 2026-08-05 11:57:06
 * @since 2023/11/28
 **/
@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmAttendanceCheckRecord", domain = "c-dev-tcm-hrdev")
public class TcmAttendanceCheckRecord extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmAttendanceCheckRecord";
    public static final String ABSENTEEISM_RECORD = "absenteeism_record";
    public static final String SCHEDULE_DATA = "schedule_data";
    public static final String FIRST_STATION_PUNCH = "first_station_punch";
    public static final String HISENSE_ATTENDANCE = "hisense_attendance";
    public static final String BUS_FAULT = "bus_fault";
    public static final String CHECK_STATUS = "check_status";
    public static final String RESERVE_DRIVER_ABSENTEEISM = "reserve_driver_absenteeism";
    public static final String CHECK_DATE = "check_date";
    public static final String DRIVER_OVERTIME = "driver_overtime";
    public static final String CONTROL_CENTER_LOG = "control_center_log";
    public static final String REFUELING_TIMES = "refueling_times";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* 缺勤記錄 */
    private Boolean absenteeism_record;
    /* 排更數據 */
    private Boolean schedule_data;
    /* 頭站打卡 */
    private Boolean first_station_punch;
    /* 海信考勤 */
    private Boolean hisense_attendance;
    /* 巴士故障 */
    private Boolean bus_fault;
    /* 核對狀態 */
    private String check_status;
    /* 後備車長工作記錄 */
    private Boolean reserve_driver_absenteeism;
    /* 核對日期 */
    private String check_date;
    /* 車長加班記錄 */
    private Boolean driver_overtime;
    /* 控制中心日志 */
    private Boolean control_center_log;
    /* 加油次數 */
    private Boolean refueling_times;
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

    public void setAbsenteeism_record(Boolean absenteeism_record) {
        this.absenteeism_record = absenteeism_record;
    }

    public void setSchedule_data(Boolean schedule_data) {
        this.schedule_data = schedule_data;
    }

    public void setFirst_station_punch(Boolean first_station_punch) {
        this.first_station_punch = first_station_punch;
    }

    public void setHisense_attendance(Boolean hisense_attendance) {
        this.hisense_attendance = hisense_attendance;
    }

    public void setBus_fault(Boolean bus_fault) {
        this.bus_fault = bus_fault;
    }

    public void setCheck_status(String check_status) {
        this.check_status = check_status;
    }

    public void setReserve_driver_absenteeism(Boolean reserve_driver_absenteeism) {
        this.reserve_driver_absenteeism = reserve_driver_absenteeism;
    }

    public void setCheck_date(String check_date) {
        this.check_date = check_date;
    }

    public void setDriver_overtime(Boolean driver_overtime) {
        this.driver_overtime = driver_overtime;
    }

    public void setControl_center_log(Boolean control_center_log) {
        this.control_center_log = control_center_log;
    }

    public void setRefueling_times(Boolean refueling_times) {
        this.refueling_times = refueling_times;
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

    public Boolean getAbsenteeism_record() {
        return absenteeism_record;
    }

    public Boolean getSchedule_data() {
        return schedule_data;
    }

    public Boolean getFirst_station_punch() {
        return first_station_punch;
    }

    public Boolean getHisense_attendance() {
        return hisense_attendance;
    }

    public Boolean getBus_fault() {
        return bus_fault;
    }

    public String getCheck_status() {
        return check_status;
    }

    public Boolean getReserve_driver_absenteeism() {
        return reserve_driver_absenteeism;
    }

    public String getCheck_date() {
        return check_date;
    }

    public Boolean getDriver_overtime() {
        return driver_overtime;
    }

    public Boolean getControl_center_log() {
        return control_center_log;
    }

    public Boolean getRefueling_times() {
        return refueling_times;
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
