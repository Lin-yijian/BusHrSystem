package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.ICode;
import com.yonyou.ypd.bill.base.ILogicDelete;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

import java.util.Date;

@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmAttendanceCorrection", domain = "c-dev-tcm-hrdev")
public class TcmAttendanceCorrection extends SuperDO implements ICode, ILogicDelete {

    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmAttendanceCorrection";

    private String date;
    private String employee_id;
    private String driver_name;
    private Date pg_one_departure_time;
    private Date pg_one_return_time;
    private Date pg_two_departure_time;
    private Date pg_two_return_time;
    private Date jl_one_departure_time;
    private Date jl_one_return_time;
    private Date jl_two_departure_time;
    private Date jl_two_return_time;
    private String one_license_plate;
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

    public String getDriver_name() { return driver_name; }
    public void setDriver_name(String driver_name) { this.driver_name = driver_name; }

    public Date getPg_one_departure_time() { return pg_one_departure_time; }
    public void setPg_one_departure_time(Date pg_one_departure_time) { this.pg_one_departure_time = pg_one_departure_time; }

    public Date getPg_one_return_time() { return pg_one_return_time; }
    public void setPg_one_return_time(Date pg_one_return_time) { this.pg_one_return_time = pg_one_return_time; }

    public Date getPg_two_departure_time() { return pg_two_departure_time; }
    public void setPg_two_departure_time(Date pg_two_departure_time) { this.pg_two_departure_time = pg_two_departure_time; }

    public Date getPg_two_return_time() { return pg_two_return_time; }
    public void setPg_two_return_time(Date pg_two_return_time) { this.pg_two_return_time = pg_two_return_time; }

    public Date getJl_one_departure_time() { return jl_one_departure_time; }
    public void setJl_one_departure_time(Date jl_one_departure_time) { this.jl_one_departure_time = jl_one_departure_time; }

    public Date getJl_one_return_time() { return jl_one_return_time; }
    public void setJl_one_return_time(Date jl_one_return_time) { this.jl_one_return_time = jl_one_return_time; }

    public Date getJl_two_departure_time() { return jl_two_departure_time; }
    public void setJl_two_departure_time(Date jl_two_departure_time) { this.jl_two_departure_time = jl_two_departure_time; }

    public Date getJl_two_return_time() { return jl_two_return_time; }
    public void setJl_two_return_time(Date jl_two_return_time) { this.jl_two_return_time = jl_two_return_time; }

    public String getOne_license_plate() { return one_license_plate; }
    public void setOne_license_plate(String one_license_plate) { this.one_license_plate = one_license_plate; }

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
