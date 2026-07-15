/*--------------------JavaPOJO for 巴士故障數據記錄(tcmBusFaultDataRecord)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 巴士故障數據記錄
 * @Date 2026-07-15 17:02:27
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMCaptainAttendance.CJ26030001.tcmBusFaultDataRecord", domain = "c-dev-tcm-hrdev")
public class TcmBusFaultDataRecord extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMCaptainAttendance.CJ26030001.tcmBusFaultDataRecord";
    public static final String PARKING_LOCATION = "parking_location";
    public static final String CAPTAIN_NAME = "captain_name";
    public static final String FAULT_TYPE = "fault_type";
    public static final String PROCESS_FINISH_TIME = "process_finish_time";
    public static final String BUS_ARRANGE = "bus_arrange";
    public static final String IS_CHANGE_BUS_MIDWAY = "is_change_bus_midway";
    public static final String DRIVER_NO = "driver_no";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String FOLLOW_UP_STATUS = "follow_up_status";
    public static final String OCC_GROUP_LEADER = "occ_group_leader";
    public static final String ROUTE = "route";
    public static final String REPORT_TIME = "report_time";
    public static final String FAULT_CONSUME_TIME = "fault_consume_time";
    public static final String BUS_CODE = "bus_code";
    public static final String FAULT_DETAIL_DESC = "fault_detail_desc";
    public static final String CAR = "car";
    public static final String DEPART_TIME = "depart_time";
    public static final String RETURN_TIME = "return_time";
    public static final String REPORT_DATE = "report_date";
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

    /* 停車位置 */
    private String parking_location;
    /* 車長姓名 */
    private String captain_name;
    /* 故障類型 */
    private String fault_type;
    /* 處理完成時間 */
    private String process_finish_time;
    /* 巴士安排 */
    private String bus_arrange;
    /* 是否中途換車 */
    private Boolean is_change_bus_midway;
    /* 車長編號 */
    private String driver_no;
    /* 澳巴工號 */
    private String waybill_employee_id;
    /* 人員編號 */
    private String employee_id;
    /* 跟進情況 */
    private String follow_up_status;
    /* OCC組長 */
    private String occ_group_leader;
    /* 路線 */
    private String route;
    /* 報告時間 */
    private String report_time;
    /* 故障耗時 */
    private String fault_consume_time;
    /* 巴士編號 */
    private String bus_code;
    /* 故障詳情描述 */
    private String fault_detail_desc;
    /* 車牌號 */
    private String car;
    /* 發車時間 */
    private String depart_time;
    /* 收車時間 */
    private String return_time;
    /* 報告日期 */
    private String report_date;
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

    public void setParking_location(String parking_location) {
        this.parking_location = parking_location;
    }

    public void setCaptain_name(String captain_name) {
        this.captain_name = captain_name;
    }

    public void setFault_type(String fault_type) {
        this.fault_type = fault_type;
    }

    public void setProcess_finish_time(String process_finish_time) {
        this.process_finish_time = process_finish_time;
    }

    public void setBus_arrange(String bus_arrange) {
        this.bus_arrange = bus_arrange;
    }

    public void setIs_change_bus_midway(Boolean is_change_bus_midway) {
        this.is_change_bus_midway = is_change_bus_midway;
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

    public void setFollow_up_status(String follow_up_status) {
        this.follow_up_status = follow_up_status;
    }

    public void setOcc_group_leader(String occ_group_leader) {
        this.occ_group_leader = occ_group_leader;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setReport_time(String report_time) {
        this.report_time = report_time;
    }

    public void setFault_consume_time(String fault_consume_time) {
        this.fault_consume_time = fault_consume_time;
    }

    public void setBus_code(String bus_code) {
        this.bus_code = bus_code;
    }

    public void setFault_detail_desc(String fault_detail_desc) {
        this.fault_detail_desc = fault_detail_desc;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public void setDepart_time(String depart_time) {
        this.depart_time = depart_time;
    }

    public void setReturn_time(String return_time) {
        this.return_time = return_time;
    }

    public void setReport_date(String report_date) {
        this.report_date = report_date;
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

    public String getParking_location() {
        return parking_location;
    }

    public String getCaptain_name() {
        return captain_name;
    }

    public String getFault_type() {
        return fault_type;
    }

    public String getProcess_finish_time() {
        return process_finish_time;
    }

    public String getBus_arrange() {
        return bus_arrange;
    }

    public Boolean getIs_change_bus_midway() {
        return is_change_bus_midway;
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

    public String getFollow_up_status() {
        return follow_up_status;
    }

    public String getOcc_group_leader() {
        return occ_group_leader;
    }

    public String getRoute() {
        return route;
    }

    public String getReport_time() {
        return report_time;
    }

    public String getFault_consume_time() {
        return fault_consume_time;
    }

    public String getBus_code() {
        return bus_code;
    }

    public String getFault_detail_desc() {
        return fault_detail_desc;
    }

    public String getCar() {
        return car;
    }

    public String getDepart_time() {
        return depart_time;
    }

    public String getReturn_time() {
        return return_time;
    }

    public String getReport_date() {
        return report_date;
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
