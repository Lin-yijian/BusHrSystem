/*--------------------JavaPOJO for 休假記錄(tcmLeaveRecord)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 休假記錄
 * @Date 2026-08-27 12:54:15
 * @since 2023/11/28
 **/
@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmLeaveRecord", domain = "c-dev-tcm-hrdev")
public class TcmLeaveRecord extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmLeaveRecord";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String DRIVER_NAME = "driver_name";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String LEAVE_TYPE = "leave_type";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String REMARK = "remark";
    public static final String LEAVE_DAYS = "leave_days";
    public static final String EFFECTIVE_MONTH = "effective_month";
    public static final String WAYBILL_SHEDULE = "waybill_shedule";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* 工號 */
    private String employee_id;
    /* 姓名 */
    private String driver_name;
    /* 海信/Waybill工號 */
    private String waybill_employee_id;
    /* 休假類型 */
    private String leave_type;
    /* 開始日期 */
    private String start_date;
    /* 結束日期 */
    private String end_date;
    /* 請假説明 */
    private String remark;
    /* 請假天數 */
    private Integer leave_days;
    /* 生效月份 */
    private String effective_month;
    /* waybill信息 */
    private String waybill_shedule;
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

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public void setWaybill_employee_id(String waybill_employee_id) {
        this.waybill_employee_id = waybill_employee_id;
    }

    public void setLeave_type(String leave_type) {
        this.leave_type = leave_type;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setLeave_days(Integer leave_days) {
        this.leave_days = leave_days;
    }

    public void setEffective_month(String effective_month) {
        this.effective_month = effective_month;
    }

    public void setWaybill_shedule(String waybill_shedule) {
        this.waybill_shedule = waybill_shedule;
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

    public String getEmployee_id() {
        return employee_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public String getWaybill_employee_id() {
        return waybill_employee_id;
    }

    public String getLeave_type() {
        return leave_type;
    }

    public String getStart_date() {
        return start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public String getRemark() {
        return remark;
    }

    public Integer getLeave_days() {
        return leave_days;
    }

    public String getEffective_month() {
        return effective_month;
    }

    public String getWaybill_shedule() {
        return waybill_shedule;
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
