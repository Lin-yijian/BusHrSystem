package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.ICode;
import com.yonyou.ypd.bill.base.ILogicDelete;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

import java.util.Date;

/**
 * 海信排更-运单排班数据
 */
@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmWaybillSheduleShiftsData", domain = "c-dev-tcm-hrdev")
public class TcmWaybillSheduleShiftsData extends SuperDO implements ICode, ILogicDelete {

    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmWaybillSheduleShiftsData";
    public static final String DATE = "date";
    public static final String DATA_COUNT = "data_count";
    public static final String TASK_CODE = "task_code";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String DRIVER_NAME = "driver_name";
    public static final String CAR = "car";
    public static final String CAR_NUM = "car_num";
    public static final String DEPARTURE_TIME = "departure_time";
    public static final String FINISH_TIME = "finish_time";
    public static final String LINE = "line";
    public static final String PARKING_SPACE = "parking_space";
    public static final String MEAL_TIME = "meal_time";
    public static final String MACAU_CAR_PARK = "macau_car_park";
    public static final String GET_CAR = "get_car";
    public static final String OUT_CAR = "out_car";
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
    /* 數據條數 */
    private Integer data_count;
    /* 任务编号 */
    private String task_code;
    /* 工號 */
    private String employee_id;
    /* 海信/Waybill工號 */
    private String waybill_employee_id;
    /* 司機姓名 */
    private String driver_name;
    /* 車輛 */
    private String car;
    /* 車辆编号 */
    private String car_num;
    /* 發車時間 */
    private Date departure_time;
    /* 收車時間 */
    private Date finish_time;
    /* 路綫 */
    private String line;
    /* 車位 */
    private String parking_space;
    /* 用膳次數 */
    private Integer meal_time;
    /* 澳門車場 */
    private String macau_car_park;
    /* 取車點 */
    private String get_car;
    /* 交車點 */
    private String out_car;
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

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public String getGet_car() {
        return get_car;
    }

    public void setGet_car(String get_car) {
        this.get_car = get_car;
    }

    public String getOut_car() {
        return out_car;
    }

    public void setOut_car(String out_car) {
        this.out_car = out_car;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Integer getData_count() { return data_count; }
    public void setData_count(Integer data_count) { this.data_count = data_count; }

    public String getTask_code() { return task_code; }
    public void setTask_code(String task_code) { this.task_code = task_code; }

    public String getEmployee_id() { return employee_id; }
    public void setEmployee_id(String employee_id) { this.employee_id = employee_id; }

    public String getWaybill_employee_id() { return waybill_employee_id; }
    public void setWaybill_employee_id(String waybill_employee_id) { this.waybill_employee_id = waybill_employee_id; }

    public String getDriver_name() { return driver_name; }
    public void setDriver_name(String driver_name) { this.driver_name = driver_name; }

    public String getCar_num() { return car_num; }
    public void setCar_num(String car_num) { this.car_num = car_num; }

    public Date getDeparture_time() { return departure_time; }
    public void setDeparture_time(Date departure_time) { this.departure_time = departure_time; }

    public Date getFinish_time() { return finish_time; }
    public void setFinish_time(Date finish_time) { this.finish_time = finish_time; }

    public String getLine() { return line; }
    public void setLine(String line) { this.line = line; }

    public String getParking_space() { return parking_space; }
    public void setParking_space(String parking_space) { this.parking_space = parking_space; }

    public Integer getMeal_time() { return meal_time; }
    public void setMeal_time(Integer meal_time) { this.meal_time = meal_time; }

    public String getMacau_car_park() { return macau_car_park; }
    public void setMacau_car_park(String macau_car_park) { this.macau_car_park = macau_car_park; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }

    public Short getDr() { return dr; }
    public void setDr(Short dr) { this.dr = dr; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getModifier() { return modifier; }
    public void setModifier(String modifier) { this.modifier = modifier; }

    public Date getModifyTime() { return modifyTime; }
    public void setModifyTime(Date modifyTime) { this.modifyTime = modifyTime; }

    public Date getPubts() { return pubts; }
    public void setPubts(Date pubts) { this.pubts = pubts; }

    public String getYtenantId() { return ytenantId; }
    public void setYtenantId(String ytenantId) { this.ytenantId = ytenantId; }

    public String getFullName() {
        if (super.getFullName() != null && super.getFullName().indexOf("#PT#.") != -1) {
            return super.getFullName();
        } else {
            return ENTITY_NAME;
        }
    }
}
