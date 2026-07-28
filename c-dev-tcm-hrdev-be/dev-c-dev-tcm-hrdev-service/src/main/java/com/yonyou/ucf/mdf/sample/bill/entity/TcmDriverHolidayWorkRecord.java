package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 車長假期工作記錄
 * @Date 2026-07-15 15:29:48
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMCaptainAttendance.CJ26030001.tcmDriverHolidayWorkRecord", domain = "c-dev-tcm-hrdev")
public class TcmDriverHolidayWorkRecord extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMCaptainAttendance.CJ26030001.tcmDriverHolidayWorkRecord";
    public static final String DATE = "date";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String DRIVER_NO = "driver_no";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String DRIVER_NAME = "driver_name";
    public static final String DEPARTMENT = "department";
    public static final String POST = "post";
    public static final String EFFECTIVE_TOTAL_WORK_HOURS = "effective_total_work_hours";
    public static final String IS_WEEKEND_WORK = "is_weekend_work";
    public static final String WEEKEND_WORK_COMPENSATION = "weekend_work_compensation";
    public static final String IS_FORCED_LEAVE_WORK = "is_forced_leave_work";
    public static final String FORCED_LEAVE_WORK_COMPENSATION = "forced_leave_work_compensation";
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

    /* 澳巴工號 */
    private String waybill_employee_id;
    /* 姓名 */
    private String driver_name;
    /* 部門 */
    private String department;
    /* 崗位 */
    private String post;
    /* 有效總工時 */
    private String effective_total_work_hours;
    /* 是否周假上班 */
    private Boolean is_weekend_work;
    /* 周假工作補償 */
    private String weekend_work_compensation;
    /* 是否強制假上班 */
    private Boolean is_forced_leave_work;
    /* 強制假工作補償 */
    private String forced_leave_work_compensation;
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


    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public void setEffective_total_work_hours(String effective_total_work_hours) {
        this.effective_total_work_hours = effective_total_work_hours;
    }

    public void setIs_weekend_work(Boolean is_weekend_work) {
        this.is_weekend_work = is_weekend_work;
    }

    public void setWeekend_work_compensation(String weekend_work_compensation) {
        this.weekend_work_compensation = weekend_work_compensation;
    }

    public void setIs_forced_leave_work(Boolean is_forced_leave_work) {
        this.is_forced_leave_work = is_forced_leave_work;
    }

    public void setForced_leave_work_compensation(String forced_leave_work_compensation) {
        this.forced_leave_work_compensation = forced_leave_work_compensation;
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



    //新增澳巴工號set get 方法
    public void setWaybillEmployeeId(String waybillEmployeeId){this.waybill_employee_id = waybillEmployeeId;}
    public String getWaybillEmployeeId(){return waybill_employee_id;}


    public String getDate() {
        return date;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public String getDepartment() {
        return department;
    }

    public String getPost() {
        return post;
    }

    public String getEffective_total_work_hours() {
        return effective_total_work_hours;
    }

    public Boolean getIs_weekend_work() {
        return is_weekend_work;
    }

    public String getWeekend_work_compensation() {
        return weekend_work_compensation;
    }

    public Boolean getIs_forced_leave_work() {
        return is_forced_leave_work;
    }

    public String getForced_leave_work_compensation() {
        return forced_leave_work_compensation;
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

