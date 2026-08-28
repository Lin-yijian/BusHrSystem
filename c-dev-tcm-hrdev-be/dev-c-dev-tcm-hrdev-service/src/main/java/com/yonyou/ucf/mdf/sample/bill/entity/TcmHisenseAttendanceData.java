package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 海信考勤数据
 * @Date 2026-07-09 16:39:12
 * @since 2023/11/28
 **/
@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmHisenseAttendanceData", domain = "c-dev-tcm-hrdev")
public class TcmHisenseAttendanceData extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmHisenseAttendanceData";
    public static final String RUN_DATE = "run_date";
    public static final String COUNT = "count";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String DRIVER_NAME = "driver_name";
    public static final String CAR = "car";
    public static final String REAL_LEAVE_TIME = "real_leave_time";
    public static final String REAL_ARRIVE_TIME = "real_arrive_time";
    public static final String ROUTE = "route";
    public static final String SEGMENT_ID = "segment_id";
    public static final String SEGMENT_NAME = "segment_name";
    public static final String START_STATION_NAME = "start_station_name";
    public static final String END_STATION_NAME = "end_station_name";
    public static final String FAKE_FLAG = "fake_flag";
    public static final String TROUBLE_FLAG = "trouble_flag";
    public static final String HANDLE_STATUS = "handle_status";
    public static final String CAR_STR = "car_str";
    public static final String INTERNAL_NO = "internal_no";
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
    private String run_date;
    /* 數據條數 */
    private Integer count;
    /* 工號 */
    private String employee_id;
    /* 海信工號 */
    private String waybill_employee_id;
    /* 司機名稱 */
    private String driver_name;
    /* 車牌 */
    private String car;
    /* 發車時間 */
    private Date real_leave_time;
    /* 收車時間 */
    private Date real_arrive_time;
    /* 路綫 */
    private String route;
    /* 方向編號 */
    private String segment_id;
    /* 方向名稱 */
    private String segment_name;
    /* 發車站點 */
    private String start_station_name;
    /* 到達站點 */
    private String end_station_name;
    /* 爛班標識 */
    private String fake_flag;
    /* 故障標識 */
    private String trouble_flag;
    /* 狀態 */
    private String handle_status;
    /* 車牌（原） */
    private String car_str;
    /* 内部編號（原） */
    private String internal_no;
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

    public void setRun_date(String run_date) {
        this.run_date = run_date;
    }

    public void setCount(Integer count) {
        this.count = count;
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

    public void setCar(String car) {
        this.car = car;
    }

    public void setReal_leave_time(Date real_leave_time) {
        this.real_leave_time = real_leave_time;
    }

    public void setReal_arrive_time(Date real_arrive_time) {
        this.real_arrive_time = real_arrive_time;
    }

    public void setRoute(String route) {
        this.route = route;
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

    public void setFake_flag(String fake_flag) {
        this.fake_flag = fake_flag;
    }

    public void setTrouble_flag(String trouble_flag) {
        this.trouble_flag = trouble_flag;
    }

    public void setHandle_status(String handle_status) {
        this.handle_status = handle_status;
    }

    public void setCar_str(String car_str) {
        this.car_str = car_str;
    }

    public void setInternal_no(String internal_no) {
        this.internal_no = internal_no;
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

    public String getRun_date() {
        return run_date;
    }

    public Integer getCount() {
        return count;
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

    public String getCar() {
        return car;
    }

    public Date getReal_leave_time() {
        return real_leave_time;
    }

    public Date getReal_arrive_time() {
        return real_arrive_time;
    }

    public String getRoute() {
        return route;
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

    public String getFake_flag() {
        return fake_flag;
    }

    public String getTrouble_flag() {
        return trouble_flag;
    }

    public String getHandle_status() {
        return handle_status;
    }

    public String getCar_str() {
        return car_str;
    }

    public String getInternal_no() {
        return internal_no;
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
