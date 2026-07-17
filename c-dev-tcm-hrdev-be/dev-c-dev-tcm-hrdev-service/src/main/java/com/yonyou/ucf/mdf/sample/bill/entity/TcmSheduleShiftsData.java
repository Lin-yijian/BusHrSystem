package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.ICode;
import com.yonyou.ypd.bill.base.ILogicDelete;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

import java.util.Date;

@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmSheduleShiftsData", domain = "c-dev-tcm-hrdev")
public class TcmSheduleShiftsData extends SuperDO implements ICode, ILogicDelete {

    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmSheduleShiftsData";
    public static final String DATE = "date";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String NAME = "name";
    public static final String DEPARTURE_TIME = "departure_time";
    public static final String FINISH_TIME = "finish_time";
    public static final String MEAL = "meal";
    public static final String TASK_CODE = "task_code";

    private String date;
    private String employee_id;
    private String waybill_employee_id;
    private String name;
    private Date departure_time;
    private Date finish_time;
    private Integer meal;
    private String task_code;
    private String code;
    private Date createTime;
    private String creator;
    private Short dr;
    private String id;
    private String modifier;
    private Date modifyTime;
    private Date pubts;
    private String ytenantId;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getEmployee_id() { return employee_id; }
    public void setEmployee_id(String employee_id) { this.employee_id = employee_id; }

    public String getWaybill_employee_id() { return waybill_employee_id; }
    public void setWaybill_employee_id(String waybill_employee_id) { this.waybill_employee_id = waybill_employee_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Date getDeparture_time() { return departure_time; }
    public void setDeparture_time(Date departure_time) { this.departure_time = departure_time; }

    public Date getFinish_time() { return finish_time; }
    public void setFinish_time(Date finish_time) { this.finish_time = finish_time; }

    public Integer getMeal() { return meal; }
    public void setMeal(Integer meal) { this.meal = meal; }

    public String getTask_code() { return task_code; }
    public void setTask_code(String task_code) { this.task_code = task_code; }

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
        if (super.getFullName() != null && super.getFullName().contains("#PT#.")) {
            return super.getFullName();
        }
        return ENTITY_NAME;
    }
}
