/*--------------------JavaPOJO for 後備車長(tcmReserveCaptainAttendanceRecord)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 後備車長
 * @Date 2026-07-28 10:01:15
 * @since 2023/11/28
 **/
@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmReserveCaptainAttendanceRecord", domain = "c-dev-tcm-hrdev")
public class TcmReserveCaptainAttendanceRecord extends SuperDO implements ILogicDelete {
    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmReserveCaptainAttendanceRecord";
    public static final String DATE = "date";
    public static final String REMARK = "remark";
    public static final String DRIVER_NO = "driver_no";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String TASK = "task";
    public static final String PICKUP_LOCATION = "pickup_location";
    public static final String OUT_TIME = "out_time";
    public static final String OUT_LOCATION = "out_location";
    public static final String BACK_TIME = "back_time";
    public static final String BACK_LOCATION = "back_location";
    public static final String DELIVERY_LOCATION = "delivery_location";
    public static final String SALARY_CALCULATION_TIME = "salary_calculation_time";
    public static final String EATING_FREQUENCY = "eating_frequency";
    public static final String COUNT = "count";
    public static final String CAR = "car";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String AUDIT_TIME = "audit_time";
    public static final String AUDITNOTE = "auditnote";
    public static final String AUDITOR = "auditor";
    public static final String VERIFYSTATE = "verifystate";
    public static final String PROCINST_ID = "procinst_id";
    public static final String BIZFLOWINSTANCEID = "bizFlowInstanceId";
    public static final String BIZFLOWMAKEBILLCODE = "bizFlowMakeBillCode";
    public static final String BIZFLOWID = "bizflowId";
    public static final String BIZFLOWNAME = "bizflowName";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String FIRSTBUSIOBJ = "firstBusiObj";
    public static final String FIRSTCHILDID = "firstChildId";
    public static final String FIRSTCODE = "firstCode";
    public static final String FIRSTID = "firstId";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String SOURCEBUSIOBJ = "sourceBusiObj";
    public static final String SOURCECHILDID = "sourceChildId";
    public static final String SOURCECODE = "sourceCode";
    public static final String SOURCEGRANDID = "sourceGrandId";
    public static final String SOURCEID = "sourceId";
    public static final String YTENANTID = "ytenantId";

    /* 日期 */
    private String date;
    /* 備注 */
    private String remark;
    /* 员工编号 */
    private String driver_no;
    /* 姓名 */
    private String name;
    /* AM/PM */
    private String type;
    /* 安排任務 */
    private String task;
    /* 接車地點 */
    private String pickup_location;
    /* 發車時間 */
    private String out_time;
    /* 發車地點 */
    private String out_location;
    /* 收車時間 */
    private String back_time;
    /* 收車地點 */
    private String back_location;
    /* 交車地點 */
    private String delivery_location;
    /* 計薪時間 */
    private String salary_calculation_time;
    /* 用膳次數 */
    private Integer eating_frequency;
    /* 交取車次數 */
    private Integer count;
    /* 車輛編號 */
    private String car;
    /* 澳巴工號 */
    private String waybill_employee_id;
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

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setDriver_no(String driver_no) {
        this.driver_no = driver_no;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setPickup_location(String pickup_location) {
        this.pickup_location = pickup_location;
    }

    public void setOut_time(String out_time) {
        this.out_time = out_time;
    }

    public void setOut_location(String out_location) {
        this.out_location = out_location;
    }

    public void setBack_time(String back_time) {
        this.back_time = back_time;
    }

    public void setBack_location(String back_location) {
        this.back_location = back_location;
    }

    public void setDelivery_location(String delivery_location) {
        this.delivery_location = delivery_location;
    }

    public void setSalary_calculation_time(String salary_calculation_time) {
        this.salary_calculation_time = salary_calculation_time;
    }

    public void setEating_frequency(Integer eating_frequency) {
        this.eating_frequency = eating_frequency;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public void setWaybill_employee_id(String waybill_employee_id) {
        this.waybill_employee_id = waybill_employee_id;
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

    public String getRemark() {
        return remark;
    }

    public String getDriver_no() {
        return driver_no;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getTask() {
        return task;
    }

    public String getPickup_location() {
        return pickup_location;
    }

    public String getOut_time() {
        return out_time;
    }

    public String getOut_location() {
        return out_location;
    }

    public String getBack_time() {
        return back_time;
    }

    public String getBack_location() {
        return back_location;
    }

    public String getDelivery_location() {
        return delivery_location;
    }

    public String getSalary_calculation_time() {
        return salary_calculation_time;
    }

    public Integer getEating_frequency() {
        return eating_frequency;
    }

    public Integer getCount() {
        return count;
    }

    public String getCar() {
        return car;
    }

    public String getWaybill_employee_id() {
        return waybill_employee_id;
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
