package com.yonyou.ucf.mdf.sample.bill.entity;


import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 巴型考勤数据
 * @Date 2026-07-16 14:22:49
 * @since 2023/11/28
 **/
@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmBaTypeAttendanceData", domain = "c-dev-tcm-hrdev")
public class TcmBaTypeAttendanceData extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmBaTypeAttendanceData";
    public static final String DATE = "date";
    public static final String DATA_COUNT = "data_count";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String DRIVER_NAME = "driver_name";
    public static final String TCMCAR = "tCMCar";
    public static final String PLATE_NUMBER = "plate_number";
    public static final String TCMCARTYPE = "tCMCarType";
    public static final String BUS_TRANSFER_TIMES = "bus_transfer_times";
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
    /* 数据条数 */
    private String data_count;
    /* 工号 */
    private String employee_id;
    /* Waybill工号 */
    private String waybill_employee_id;
    /* 司机姓名 */
    private String driver_name;
    /* 车辆 */
    private String tCMCar;
    /* 车牌 */
    private String plate_number;
    /* 巴型 */
    private String tCMCarType;
    /* 巴型转数 */
    private BigDecimal bus_transfer_times;
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

    public void setData_count(String data_count) {
        this.data_count = data_count;
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

    public void settCMCar(String tCMCar) {
        this.tCMCar = tCMCar;
    }

    public void setPlate_number(String plate_number) {
        this.plate_number = plate_number;
    }

    public void settCMCarType(String tCMCarType) {
        this.tCMCarType = tCMCarType;
    }

    public void setBus_transfer_times(BigDecimal bus_transfer_times) {
        this.bus_transfer_times = bus_transfer_times;
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

    public String getData_count() {
        return data_count;
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

    public String gettCMCar() {
        return tCMCar;
    }

    public String getPlate_number() {
        return plate_number;
    }

    public String gettCMCarType() {
        return tCMCarType;
    }

    public BigDecimal getBus_transfer_times() {
        return bus_transfer_times;
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

