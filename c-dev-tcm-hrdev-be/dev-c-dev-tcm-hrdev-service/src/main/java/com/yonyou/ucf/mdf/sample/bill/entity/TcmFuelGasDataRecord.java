/*--------------------JavaPOJO for 入油入汽數據記錄(tcmFuelGasDataRecord)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 入油入汽數據記錄
 * @Date 2026-07-16 14:01:27
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMCaptainAttendance.CJ26030001.tcmFuelGasDataRecord", domain = "c-dev-tcm-hrdev")
public class TcmFuelGasDataRecord extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMCaptainAttendance.CJ26030001.tcmFuelGasDataRecord";
    public static final String REFUEL_COUNT = "refuel_count";
    public static final String GAS_COUNT = "gas_count";
    public static final String DRIVER_NO = "driver_no";
    public static final String REMARK = "remark";
    public static final String ATTENDANCE_DATE = "attendance_date";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String NAME = "name";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* 入油次數 */
    private Integer refuel_count;
    /* 加氣次數 */
    private Integer gas_count;
    /* 車長編號 */
    private String driver_no;
    /* 備註 */
    private String remark;
    /* 考勤日期 */
    private String attendance_date;
    /* 澳巴工號 */
    private String waybill_employee_id;
    /* 人員編號 */
    private String employee_id;
    /* 姓名 */
    private String name;
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

    public void setRefuel_count(Integer refuel_count) {
        this.refuel_count = refuel_count;
    }

    public void setGas_count(Integer gas_count) {
        this.gas_count = gas_count;
    }

    public void setDriver_no(String driver_no) {
        this.driver_no = driver_no;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setAttendance_date(String attendance_date) {
        this.attendance_date = attendance_date;
    }

    public void setWaybill_employee_id(String waybill_employee_id) {
        this.waybill_employee_id = waybill_employee_id;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getRefuel_count() {
        return refuel_count;
    }

    public Integer getGas_count() {
        return gas_count;
    }

    public String getDriver_no() {
        return driver_no;
    }

    public String getRemark() {
        return remark;
    }

    public String getAttendance_date() {
        return attendance_date;
    }

    public String getWaybill_employee_id() {
        return waybill_employee_id;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public String getName() {
        return name;
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
    //
}
