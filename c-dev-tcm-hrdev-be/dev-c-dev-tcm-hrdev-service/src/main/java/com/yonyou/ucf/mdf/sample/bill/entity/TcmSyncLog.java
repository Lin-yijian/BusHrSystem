/*--------------------JavaPOJO for 同步日志(tcmSyncLog)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 同步日志
 * @Date 2026-08-30 18:28:29
 * @since 2023/11/28
 **/
@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmSyncLog", domain = "c-dev-tcm-hrdev")
public class TcmSyncLog extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmSyncLog";
    public static final String DATE = "date";
    public static final String MODEL = "model";
    public static final String SYNC_STATUS = "sync_status";
    public static final String TASK_STATUS = "task_status";
    public static final String QUEUE_START_TIME = "queue_start_time";
    public static final String QUEUE_END_TIME = "queue_end_time";
    public static final String REMARK = "remark";
    public static final String ERROR_INFO = "error_info";
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
    /* 所屬模塊 */
    private String model;
    /* 同步狀態 */
    private String sync_status;
    /* 任務完成狀態 */
    private String task_status;
    /* 隊列開始時間 */
    private Date queue_start_time;
    /* 隊列結束時間 */
    private Date queue_end_time;
    /* 備注 */
    private String remark;
    /* 异常信息 */
    private String error_info;
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

    public void setModel(String model) {
        this.model = model;
    }

    public void setSync_status(String sync_status) {
        this.sync_status = sync_status;
    }

    public void setTask_status(String task_status) {
        this.task_status = task_status;
    }

    public void setQueue_start_time(Date queue_start_time) {
        this.queue_start_time = queue_start_time;
    }

    public void setQueue_end_time(Date queue_end_time) {
        this.queue_end_time = queue_end_time;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setError_info(String error_info) {
        this.error_info = error_info;
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

    public String getModel() {
        return model;
    }

    public String getSync_status() {
        return sync_status;
    }

    public String getTask_status() {
        return task_status;
    }

    public Date getQueue_start_time() {
        return queue_start_time;
    }

    public Date getQueue_end_time() {
        return queue_end_time;
    }

    public String getRemark() {
        return remark;
    }

    public String getError_info() {
        return error_info;
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
