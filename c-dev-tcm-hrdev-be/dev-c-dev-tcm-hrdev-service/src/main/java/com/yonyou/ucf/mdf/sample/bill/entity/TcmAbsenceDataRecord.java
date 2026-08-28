/*--------------------JavaPOJO for 缺勤數據記錄(tcmAbsenceDataRecord)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 缺勤數據記錄
 * @Date 2026-07-14 14:58:02
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMCaptainAttendance.CJ26030001.tcmAbsenceDataRecord", domain = "c-dev-tcm-hrdev")
public class TcmAbsenceDataRecord extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMCaptainAttendance.CJ26030001.tcmAbsenceDataRecord";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String DRIVER_NAME = "driver_name";
    public static final String POSITION_NO = "position_no";
    public static final String ABSENCE_CATEGORY = "absence_category";
    public static final String IS_REST_DAY = "is_rest_day";
    public static final String AIRCRAFT_POSITION = "aircraft_position";
    public static final String REASON = "reason";
    public static final String REPLACEMENT_DRIVER = "replacement_driver";
    public static final String AFFECTED_DRIVER_NO = "affected_driver_no";
    public static final String AFFECTED_DRIVER_NAME = "affected_driver_name";
    public static final String SCHEDULED_HANDOVER_STATUS = "scheduled_handover_status";
    public static final String ACTUAL_HANDOVER_STATUS = "actual_handover_status";
    public static final String NIGHT_SHIFT_ALLOWANCE = "night_shift_allowance";
    public static final String IS_NOTIFIED = "is_notified";
    public static final String OCC_LEADER = "occ_leader";
    public static final String AFFECTED_DRIVER_PHONE = "affected_driver_phone";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
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
    /* 時間 */
    private String time;
    /* 車長工號 */
    private String employee_id;
    /* 車長姓名 */
    private String driver_name;
    /* 職務編號 */
    private String position_no;
    /* 缺勤類別 */
    private String absence_category;
    /* 是否休息日 */
    private Boolean is_rest_day;
    /* 飛機位 */
    private String aircraft_position;
    /* 原因 */
    private String reason;
    /* 頂替車長 */
    private String replacement_driver;
    /* 交接車受影響車長編號 */
    private String affected_driver_no;
    /* 交接車受影響車長姓名 */
    private String affected_driver_name;
    /* 編定交接情況 */
    private String scheduled_handover_status;
    /* 實際交接情況 */
    private String actual_handover_status;
    /* 夜更津貼 */
    private BigDecimal night_shift_allowance;
    /* 已通知 */
    private Boolean is_notified;
    /* OCC組長 */
    private String occ_leader;
    /* 受影響車長電話 */
    private String affected_driver_phone;
    /* 海信工號 */
    private String waybill_employee_id;
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

    public void setTime(String time) {
        this.time = time;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public void setPosition_no(String position_no) {
        this.position_no = position_no;
    }

    public void setAbsence_category(String absence_category) {
        this.absence_category = absence_category;
    }

    public void setIs_rest_day(Boolean is_rest_day) {
        this.is_rest_day = is_rest_day;
    }

    public void setAircraft_position(String aircraft_position) {
        this.aircraft_position = aircraft_position;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setReplacement_driver(String replacement_driver) {
        this.replacement_driver = replacement_driver;
    }

    public void setAffected_driver_no(String affected_driver_no) {
        this.affected_driver_no = affected_driver_no;
    }

    public void setAffected_driver_name(String affected_driver_name) {
        this.affected_driver_name = affected_driver_name;
    }

    public void setScheduled_handover_status(String scheduled_handover_status) {
        this.scheduled_handover_status = scheduled_handover_status;
    }

    public void setActual_handover_status(String actual_handover_status) {
        this.actual_handover_status = actual_handover_status;
    }

    public void setNight_shift_allowance(BigDecimal night_shift_allowance) {
        this.night_shift_allowance = night_shift_allowance;
    }

    public void setIs_notified(Boolean is_notified) {
        this.is_notified = is_notified;
    }

    public void setOcc_leader(String occ_leader) {
        this.occ_leader = occ_leader;
    }

    public void setAffected_driver_phone(String affected_driver_phone) {
        this.affected_driver_phone = affected_driver_phone;
    }

    public void setWaybill_employee_id(String waybill_employee_id) {
        this.waybill_employee_id = waybill_employee_id;
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

    public String getTime() {
        return time;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public String getPosition_no() {
        return position_no;
    }

    public String getAbsence_category() {
        return absence_category;
    }

    public Boolean getIs_rest_day() {
        return is_rest_day;
    }

    public String getAircraft_position() {
        return aircraft_position;
    }

    public String getReason() {
        return reason;
    }

    public String getReplacement_driver() {
        return replacement_driver;
    }

    public String getAffected_driver_no() {
        return affected_driver_no;
    }

    public String getAffected_driver_name() {
        return affected_driver_name;
    }

    public String getScheduled_handover_status() {
        return scheduled_handover_status;
    }

    public String getActual_handover_status() {
        return actual_handover_status;
    }

    public BigDecimal getNight_shift_allowance() {
        return night_shift_allowance;
    }

    public Boolean getIs_notified() {
        return is_notified;
    }

    public String getOcc_leader() {
        return occ_leader;
    }

    public String getAffected_driver_phone() {
        return affected_driver_phone;
    }

    public String getWaybill_employee_id() {
        return waybill_employee_id;
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
