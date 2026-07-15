/*--------------------JavaPOJO for 控制中心日誌(tcmControlCenterLog)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 控制中心日誌
 * @Date 2026-07-15 17:37:18
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMCaptainAttendance.CJ26030001.tcmControlCenterLog", domain = "c-dev-tcm-hrdev")
public class TcmControlCenterLog extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMCaptainAttendance.CJ26030001.tcmControlCenterLog";
    public static final String DATE = "date";
    public static final String CAR = "car";
    public static final String VEHICLE_NO = "vehicle_no";
    public static final String DEPART_TIME = "depart_time";
    public static final String DRIVER_NO = "driver_no";
    public static final String RETURN_TIME = "return_time";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String TYPE = "type";
    public static final String CONTENT = "content";
    public static final String OCC = "occ";
    public static final String DRIVER_NAME = "driver_name";
    public static final String ROUTE = "route";
    public static final String TRAVEL_DIRECTION = "travel_direction";
    public static final String LOCATION = "location";
    public static final String TIME = "time";
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
    public static final String ROUTE_ROUTE_CODE = "route_route_code";
    public static final String CAR_PLATE_NUMBER = "car_plate_number";

    /* 日期 */
    private String date;
    /* 車牌號 */
    private String car;
    /* 車號 */
    private String vehicle_no;
    /* 發車時間 */
    private String depart_time;
    /* 車長編號 */
    private String driver_no;
    /* 收車時間 */
    private String return_time;
    /* 車長工號 */
    private String employee_id;
    /* 類型 */
    private String type;
    /* 內容 */
    private String content;
    /* OCC */
    private String occ;
    /* 車長姓名 */
    private String driver_name;
    /* 路線 */
    private String route;
    /* 行駛方向 */
    private String travel_direction;
    /* 地點 */
    private String location;
    /* 時間 */
    private String time;
    /* 澳巴工號 */
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

    public void setCar(String car) {
        this.car = car;
    }

    public void setVehicle_no(String vehicle_no) {
        this.vehicle_no = vehicle_no;
    }

    public void setDepart_time(String depart_time) {
        this.depart_time = depart_time;
    }

    public void setDriver_no(String driver_no) {
        this.driver_no = driver_no;
    }

    public void setReturn_time(String return_time) {
        this.return_time = return_time;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setOcc(String occ) {
        this.occ = occ;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setTravel_direction(String travel_direction) {
        this.travel_direction = travel_direction;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTime(String time) {
        this.time = time;
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

    public String getCar() {
        return car;
    }

    public String getVehicle_no() {
        return vehicle_no;
    }

    public String getDepart_time() {
        return depart_time;
    }

    public String getDriver_no() {
        return driver_no;
    }

    public String getReturn_time() {
        return return_time;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getOcc() {
        return occ;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public String getRoute() {
        return route;
    }

    public String getTravel_direction() {
        return travel_direction;
    }

    public String getLocation() {
        return location;
    }

    public String getTime() {
        return time;
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
