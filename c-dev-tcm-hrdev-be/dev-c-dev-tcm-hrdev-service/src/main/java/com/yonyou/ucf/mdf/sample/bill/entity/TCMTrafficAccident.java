/*--------------------JavaPOJO for 交通意外記錄(TCMTrafficAccident)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 交通意外記錄
 * @Date 2026-07-15 16:00:58
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMBaseInfo.TCMBaseInfo.TCMTrafficAccident", domain = "c-dev-tcm-hrdev")
public class TCMTrafficAccident extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMBaseInfo.TCMBaseInfo.TCMTrafficAccident";
    public static final String ACCIDENT_NO = "accident_no";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String CAR = "car";
    public static final String ROUTE = "route";
    public static final String ACCIDENT_TYPE = "accident_type";
    public static final String DESC = "desc";
    public static final String DRIVER_NO = "driver_no";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String DRIVER_NAME = "driver_name";
    public static final String LEVEL = "level";
    public static final String DRIVING_EXPERIENCE = "driving_experience";
    public static final String LOCATION = "location";
    public static final String DIRECTION = "direction";
    public static final String RESPONSIBILITY = "responsibility";
    public static final String DRIVER_RESPONSIBILITY = "driver_responsibility";
    public static final String DAMAGED_AREA = "damaged_area";
    public static final String REPAIR_COST = "repair_cost";
    public static final String MATERIAL_COST = "material_cost";
    public static final String OTHER_COST = "other_cost";
    public static final String BUS_TYPE = "bus_type";
    public static final String OTHER_NAME = "other_name";
    public static final String OTHER_PHONE = "other_phone";
    public static final String DAMAGED_DESC = "damaged_desc";
    public static final String COMPENSATION_AMOUNT = "compensation_amount";
    public static final String PROPERTY_TYPE = "property_type";
    public static final String PROPERTY_DESC = "property_desc";
    public static final String CONTACT_DATA = "contact_data";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";
    public static final String ROUTE_ROUTE_CODE = "route_route_code";
    public static final String CAR_PLATE_NUMBER = "car_plate_number";

    /* 意外編號 */
    private String accident_no;
    /* 日期 */
    private String date;
    /* 時間 */
    private String time;
    /* 車牌號 */
    private String car;
    /* 路綫編號 */
    private String route;
    /* 意外類型 */
    private String accident_type;
    /* 描述 */
    private String desc;
    /* 工號 */
    private String driver_no;
    /* 澳巴工號 */
    private String waybill_employee_id;
    /* 人員編號 */
    private String employee_id;
    /* 司機姓名 */
    private String driver_name;
    /* 等級 */
    private String level;
    /* 駕駛執照經驗 */
    private String driving_experience;
    /* 地點 */
    private String location;
    /* 方向 */
    private String direction;
    /* 意外責任 */
    private String responsibility;
    /* 車長認爲責任誰屬 */
    private String driver_responsibility;
    /* 巴士受損部位 */
    private String damaged_area;
    /* 維修費 */
    private BigDecimal repair_cost;
    /* 材料費 */
    private BigDecimal material_cost;
    /* 第三者支付賠償 */
    private BigDecimal other_cost;
    /* 車輛種類 */
    private String bus_type;
    /* 第三者姓名 */
    private String other_name;
    /* 第三者聯係電話 */
    private String other_phone;
    /* 損毀描述 */
    private String damaged_desc;
    /* 賠償金額 */
    private BigDecimal compensation_amount;
    /* 財物類型 */
    private String property_type;
    /* 財物損毀描述 */
    private String property_desc;
    /* 聯絡資料 */
    private String contact_data;
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

    public void setAccident_no(String accident_no) {
        this.accident_no = accident_no;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setAccident_type(String accident_type) {
        this.accident_type = accident_type;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setDriver_no(String driver_no) {
        this.driver_no = driver_no;
    }

    public void setWaybill_employee_id(String waybill_employee_id) {
        this.waybill_employee_id = waybill_employee_id;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setDriving_experience(String driving_experience) {
        this.driving_experience = driving_experience;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setResponsibility(String responsibility) {
        this.responsibility = responsibility;
    }

    public void setDriver_responsibility(String driver_responsibility) {
        this.driver_responsibility = driver_responsibility;
    }

    public void setDamaged_area(String damaged_area) {
        this.damaged_area = damaged_area;
    }

    public void setRepair_cost(BigDecimal repair_cost) {
        this.repair_cost = repair_cost;
    }

    public void setMaterial_cost(BigDecimal material_cost) {
        this.material_cost = material_cost;
    }

    public void setOther_cost(BigDecimal other_cost) {
        this.other_cost = other_cost;
    }

    public void setBus_type(String bus_type) {
        this.bus_type = bus_type;
    }

    public void setOther_name(String other_name) {
        this.other_name = other_name;
    }

    public void setOther_phone(String other_phone) {
        this.other_phone = other_phone;
    }

    public void setDamaged_desc(String damaged_desc) {
        this.damaged_desc = damaged_desc;
    }

    public void setCompensation_amount(BigDecimal compensation_amount) {
        this.compensation_amount = compensation_amount;
    }

    public void setProperty_type(String property_type) {
        this.property_type = property_type;
    }

    public void setProperty_desc(String property_desc) {
        this.property_desc = property_desc;
    }

    public void setContact_data(String contact_data) {
        this.contact_data = contact_data;
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

    public String getAccident_no() {
        return accident_no;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getCar() {
        return car;
    }

    public String getRoute() {
        return route;
    }

    public String getAccident_type() {
        return accident_type;
    }

    public String getDesc() {
        return desc;
    }

    public String getDriver_no() {
        return driver_no;
    }

    public String getWaybill_employee_id() {
        return waybill_employee_id;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public String getLevel() {
        return level;
    }

    public String getDriving_experience() {
        return driving_experience;
    }

    public String getLocation() {
        return location;
    }

    public String getDirection() {
        return direction;
    }

    public String getResponsibility() {
        return responsibility;
    }

    public String getDriver_responsibility() {
        return driver_responsibility;
    }

    public String getDamaged_area() {
        return damaged_area;
    }

    public BigDecimal getRepair_cost() {
        return repair_cost;
    }

    public BigDecimal getMaterial_cost() {
        return material_cost;
    }

    public BigDecimal getOther_cost() {
        return other_cost;
    }

    public String getBus_type() {
        return bus_type;
    }

    public String getOther_name() {
        return other_name;
    }

    public String getOther_phone() {
        return other_phone;
    }

    public String getDamaged_desc() {
        return damaged_desc;
    }

    public BigDecimal getCompensation_amount() {
        return compensation_amount;
    }

    public String getProperty_type() {
        return property_type;
    }

    public String getProperty_desc() {
        return property_desc;
    }

    public String getContact_data() {
        return contact_data;
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
