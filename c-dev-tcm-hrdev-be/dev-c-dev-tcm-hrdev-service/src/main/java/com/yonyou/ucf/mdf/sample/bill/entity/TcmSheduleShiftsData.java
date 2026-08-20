/*--------------------JavaPOJO for 排更數據(tcmSheduleShiftsData)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 排更數據
 * @Date 2026-08-13 11:08:19
 * @since 2023/11/28
 **/
@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmSheduleShiftsData", domain = "c-dev-tcm-hrdev")
public class TcmSheduleShiftsData extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmSheduleShiftsData";
    public static final String DATE = "date";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String TASK_CODE = "task_code";
    public static final String DEPARTURE_TIME = "departure_time";
    public static final String FINISH_TIME = "finish_time";
    public static final String SHIFT_CHANGE_TIME1 = "shift_change_time1";
    public static final String SHIFT_CHANGE_TIME2 = "shift_change_time2";
    public static final String MEAL = "meal";
    public static final String SHIFT_CHANGE = "shift_change";
    public static final String MACAU_CAR_PARK = "macau_car_park";
    public static final String TAIPA_CAR_PARK = "taipa_car_park";
    public static final String DESIGNATED_PARKING_BAY = "designated_parking_bay";
    public static final String REMARKS = "remarks";
    public static final String ONE_EARLY_LINE = "one_early_line";
    public static final String TWO_EARLY_LINE = "two_early_line";
    public static final String PARKING_SPACE = "parking_space";
    public static final String ATTENDANCE_STATE = "attendance_state";
    public static final String TWO_DEPARTURE_TIME = "two_departure_time";
    public static final String ONE_FINISH_TIME = "one_finish_time";
    public static final String GET_OUT_CAR = "get_out_car";
    public static final String CAR = "car";
    public static final String TASK = "task";
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
    /* 澳巴工号 */
    private String waybill_employee_id;
    /* 姓名 */
    private String name;
    /* 類型 */
    private String type;
    /* 任務编号 */
    private String task_code;
    /* 一更發車時間 */
    private Date departure_time;
    /* 二更收車時間 */
    private Date finish_time;
    /* 上下更時間1 */
    private String shift_change_time1;
    /* 上下更時間2 */
    private String shift_change_time2;
    /* 用膳 */
    private Integer meal;
    /* 上下更 */
    private Integer shift_change;
    /* 澳門車場 */
    private Boolean macau_car_park;
    /* 凼仔車場 */
    private Boolean taipa_car_park;
    /* 對應更位 */
    private String designated_parking_bay;
    /* 備注 */
    private String remarks;
    /* 一更路線 */
    private String one_early_line;
    /* 二更路線 */
    private String two_early_line;
    /* 車位 */
    private String parking_space;
    /* 考勤狀態 */
    private String attendance_state;
    /* 二更發車時間 */
    private Date two_departure_time;
    /* 一更收車時間 */
    private Date one_finish_time;
    /* 交取車次數 */
    private Integer get_out_car;
    /* 車牌號 */
    private String car;
    /* 任務 */
    private String task;
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

    public void setWaybill_employee_id(String waybill_employee_id) {
        this.waybill_employee_id = waybill_employee_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTask_code(String task_code) {
        this.task_code = task_code;
    }

    public void setDeparture_time(Date departure_time) {
        this.departure_time = departure_time;
    }

    public void setFinish_time(Date finish_time) {
        this.finish_time = finish_time;
    }

    public void setShift_change_time1(String shift_change_time1) {
        this.shift_change_time1 = shift_change_time1;
    }

    public void setShift_change_time2(String shift_change_time2) {
        this.shift_change_time2 = shift_change_time2;
    }

    public void setMeal(Integer meal) {
        this.meal = meal;
    }

    public void setShift_change(Integer shift_change) {
        this.shift_change = shift_change;
    }

    public void setMacau_car_park(Boolean macau_car_park) {
        this.macau_car_park = macau_car_park;
    }

    public void setTaipa_car_park(Boolean taipa_car_park) {
        this.taipa_car_park = taipa_car_park;
    }

    public void setDesignated_parking_bay(String designated_parking_bay) {
        this.designated_parking_bay = designated_parking_bay;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setOne_early_line(String one_early_line) {
        this.one_early_line = one_early_line;
    }

    public void setTwo_early_line(String two_early_line) {
        this.two_early_line = two_early_line;
    }

    public void setParking_space(String parking_space) {
        this.parking_space = parking_space;
    }

    public void setAttendance_state(String attendance_state) {
        this.attendance_state = attendance_state;
    }

    public void setTwo_departure_time(Date two_departure_time) {
        this.two_departure_time = two_departure_time;
    }

    public void setOne_finish_time(Date one_finish_time) {
        this.one_finish_time = one_finish_time;
    }

    public void setGet_out_car(Integer get_out_car) {
        this.get_out_car = get_out_car;
    }

    public void setCar(String car) {
        this.car = car;
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

    public String getWaybill_employee_id() {
        return waybill_employee_id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getTask_code() {
        return task_code;
    }

    public Date getDeparture_time() {
        return departure_time;
    }

    public Date getFinish_time() {
        return finish_time;
    }

    public String getShift_change_time1() {
        return shift_change_time1;
    }

    public String getShift_change_time2() {
        return shift_change_time2;
    }

    public Integer getMeal() {
        return meal;
    }

    public Integer getShift_change() {
        return shift_change;
    }

    public Boolean getMacau_car_park() {
        return macau_car_park;
    }

    public Boolean getTaipa_car_park() {
        return taipa_car_park;
    }

    public String getDesignated_parking_bay() {
        return designated_parking_bay;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getOne_early_line() {
        return one_early_line;
    }

    public String getTwo_early_line() {
        return two_early_line;
    }

    public String getParking_space() {
        return parking_space;
    }

    public String getAttendance_state() {
        return attendance_state;
    }

    public Date getTwo_departure_time() {
        return two_departure_time;
    }

    public Date getOne_finish_time() {
        return one_finish_time;
    }

    public Integer getGet_out_car() {
        return get_out_car;
    }

    public String getCar() {
        return car;
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

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
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
