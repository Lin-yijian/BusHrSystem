/*--------------------JavaPOJO for 考勤统计(tcmAttendanceStatistics)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 考勤统计
 * @Date 2026-07-22 11:37:29
 * @since 2023/11/28
 **/
@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmAttendanceStatistics", domain = "c-dev-tcm-hrdev")
public class TcmAttendanceStatistics extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmAttendanceStatistics";
    public static final String MONTH = "month";
    public static final String NEW1 = "new1";
    public static final String POSITION = "position";
    public static final String PERSON = "person";
    public static final String EXECUTOR = "executor";
    public static final String ARCHIVESTATUS_C = "archiveStatus_C";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* 月份 */
    private String month;
    /* 月份 */
    private String new1;
    /* 岗位 */
    private String position;
    /* 人员 */
    private String person;
    /* 执行人 */
    private String executor;
    /* 压缩包状态 */
    private String archiveStatus_C;
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

    public void setMonth(String month) {
        this.month = month;
    }

    public void setNew1(String new1) {
        this.new1 = new1;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public void setArchiveStatus_C(String archiveStatus_C) {
        this.archiveStatus_C = archiveStatus_C;
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

    public String getMonth() {
        return month;
    }

    public String getNew1() {
        return new1;
    }

    public String getPosition() {
        return position;
    }

    public String getPerson() {
        return person;
    }

    public String getExecutor() {
        return executor;
    }

    public String getArchiveStatus_C() {
        return archiveStatus_C;
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
