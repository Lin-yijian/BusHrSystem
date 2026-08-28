package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 車載系統登錄記錄
 * @Date 2026-07-10 15:33:22
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMCaptainAttendance.CJ26030001.tcmHisenseWaybillLoginRecord", domain = "c-dev-tcm-hrdev")
public class TcmHisenseWaybillLoginRecord extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMCaptainAttendance.CJ26030001.tcmHisenseWaybillLoginRecord";
    public static final String DATE = "date";
    public static final String DRIVER_NAME = "driver_name";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String CAR = "car";
    public static final String VEHICLE_NO = "vehicle_no";
    public static final String DRIVER_NO = "driver_no";
    public static final String TIME = "time";
    public static final String OPERATE_TYPE = "operate_type";
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
    /* 姓名 */
    private String driver_name;
    /* 海信/Waybill工號 */
    private String waybill_employee_id;
    /* 車輛 */
    private String car;
    /* 車輛 */
    private String vehicle_no;
    /* 工號 */
    private String driver_no;
    /* 時間 */
    private Date time;
    /* 類型 */
    private String operate_type;
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

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }


    public void setCar(String car) {
        this.car = car;
    }

    public void setVehicle_no(String vehicle_no) {
        this.vehicle_no = vehicle_no;
    }

    public void setDriver_no(String driver_no) {
        this.driver_no = driver_no;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setOperate_type(String operate_type) {
        this.operate_type = operate_type;
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

    public String getWaybill_employee_id() {
        return waybill_employee_id;
    }

    public void setWaybill_employee_id(String waybill_employee_id) {
        this.waybill_employee_id = waybill_employee_id;
    }

    public String getDate() {
        return date;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public String getCar() {
        return car;
    }

    public String getVehicle_no() {
        return vehicle_no;
    }

    public String getDriver_no() {
        return driver_no;
    }

    public Date getTime() {
        return time;
    }

    public String getOperate_type() {
        return operate_type;
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

