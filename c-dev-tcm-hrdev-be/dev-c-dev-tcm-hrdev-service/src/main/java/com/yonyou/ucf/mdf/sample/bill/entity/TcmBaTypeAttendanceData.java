/*--------------------JavaPOJO for 巴型考勤数据(tcmBaTypeAttendanceData)--------------------*/
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
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String BUS_TRANSFER_TIMES = "bus_transfer_times";
    public static final String ORDER_NO = "order_no";
    public static final String CAR = "car";
    public static final String ROUTE_NAME = "route_name";
    public static final String SEGMENT_ID = "segment_id";
    public static final String SEGMENT_NAME = "segment_name";
    public static final String START_STATION_NAME = "start_station_name";
    public static final String END_STATION_NAME = "end_station_name";
    public static final String BUSSELF_ID = "busself_id";
    public static final String BUSCARD_ID = "buscard_id";
    public static final String DRIVER_NAME = "driver_name";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String LEAVE_TIME = "leave_time";
    public static final String ARRIVE_TIME = "arrive_time";
    public static final String REAL_LEAVE_TIME = "real_leave_time";
    public static final String REAL_ARRIVE_TIME = "real_arrive_time";
    public static final String IS_CONFIRM = "is_confirm";
    public static final String FAKE_FLAG = "fake_flag";
    public static final String TROUBLE_FLAG = "trouble_flag";
    public static final String REC_STATE = "rec_state";
    public static final String BUS_TYPE = "bus_type";
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
    /* 巴型轉數 */
    private BigDecimal bus_transfer_times;
    /* 序號 */
    private String order_no;
    /* 車輛 */
    private String car;
    /* 线路名 */
    private String route_name;
    /* 方向ID */
    private String segment_id;
    /* 方向名称 */
    private String segment_name;
    /* 发车站点 */
    private String start_station_name;
    /* 到达站点 */
    private String end_station_name;
    /* 车号 */
    private String busself_id;
    /* 车牌 */
    private String buscard_id;
    /* 驾驶员 */
    private String driver_name;
    /* 澳巴工號 */
    private String waybill_employee_id;
    /* 调度发车时间 */
    private Date leave_time;
    /* 调度到达时间 */
    private Date arrive_time;
    /* 实际发车时间 */
    private Date real_leave_time;
    /* 实际到达时间 */
    private Date real_arrive_time;
    /* 是否已确认 */
    private String is_confirm;
    /* 烂班标识 */
    private String fake_flag;
    /* 故障标识 */
    private String trouble_flag;
    /* 车次执行状态（暂无用） */
    private String rec_state;
    /* 巴型类型 */
    private String bus_type;
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

    public String getBus_type() {
        return bus_type;
    }

    public void setBus_type(String bus_type) {
        this.bus_type = bus_type;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public void setBus_transfer_times(BigDecimal bus_transfer_times) {
        this.bus_transfer_times = bus_transfer_times;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public void setRoute_name(String route_name) {
        this.route_name = route_name;
    }

    public void setSegment_id(String segment_id) {
        this.segment_id = segment_id;
    }

    public void setSegment_name(String segment_name) {
        this.segment_name = segment_name;
    }

    public void setStart_station_name(String start_station_name) {
        this.start_station_name = start_station_name;
    }

    public void setEnd_station_name(String end_station_name) {
        this.end_station_name = end_station_name;
    }

    public void setBusself_id(String busself_id) {
        this.busself_id = busself_id;
    }

    public void setBuscard_id(String buscard_id) {
        this.buscard_id = buscard_id;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public void setWaybill_employee_id(String waybill_employee_id) {
        this.waybill_employee_id = waybill_employee_id;
    }

    public void setLeave_time(Date leave_time) {
        this.leave_time = leave_time;
    }

    public void setArrive_time(Date arrive_time) {
        this.arrive_time = arrive_time;
    }

    public void setReal_leave_time(Date real_leave_time) {
        this.real_leave_time = real_leave_time;
    }

    public void setReal_arrive_time(Date real_arrive_time) {
        this.real_arrive_time = real_arrive_time;
    }

    public void setIs_confirm(String is_confirm) {
        this.is_confirm = is_confirm;
    }

    public void setFake_flag(String fake_flag) {
        this.fake_flag = fake_flag;
    }

    public void setTrouble_flag(String trouble_flag) {
        this.trouble_flag = trouble_flag;
    }

    public void setRec_state(String rec_state) {
        this.rec_state = rec_state;
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

    public String getEmployee_id() {
        return employee_id;
    }

    public BigDecimal getBus_transfer_times() {
        return bus_transfer_times;
    }

    public String getOrder_no() {
        return order_no;
    }

    public String getCar() {
        return car;
    }

    public String getRoute_name() {
        return route_name;
    }

    public String getSegment_id() {
        return segment_id;
    }

    public String getSegment_name() {
        return segment_name;
    }

    public String getStart_station_name() {
        return start_station_name;
    }

    public String getEnd_station_name() {
        return end_station_name;
    }

    public String getBusself_id() {
        return busself_id;
    }

    public String getBuscard_id() {
        return buscard_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public String getWaybill_employee_id() {
        return waybill_employee_id;
    }

    public Date getLeave_time() {
        return leave_time;
    }

    public Date getArrive_time() {
        return arrive_time;
    }

    public Date getReal_leave_time() {
        return real_leave_time;
    }

    public Date getReal_arrive_time() {
        return real_arrive_time;
    }

    public String getIs_confirm() {
        return is_confirm;
    }

    public String getFake_flag() {
        return fake_flag;
    }

    public String getTrouble_flag() {
        return trouble_flag;
    }

    public String getRec_state() {
        return rec_state;
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
