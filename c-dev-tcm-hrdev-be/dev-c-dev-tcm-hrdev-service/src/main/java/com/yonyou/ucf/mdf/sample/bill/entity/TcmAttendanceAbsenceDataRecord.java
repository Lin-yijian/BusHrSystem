/*--------------------JavaPOJO for 考勤修正-缺勤記錄(tcmAttendanceAbsenceDataRecord)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 考勤修正-休假記錄
 * @Date 2026-07-17 15:32:24
 * @since 2023/11/28
 **/
@YMSEntity(name = "unitfyEnum.TCMCaptainAttendance.tcmAttendanceAbsenceDataRecord", domain = "c-dev-tcm-hrdev")
public class TcmAttendanceAbsenceDataRecord extends SuperDO implements ILogicDelete {
    public static final String ENTITY_NAME = "unitfyEnum.TCMCaptainAttendance.tcmAttendanceAbsenceDataRecord";
    public static final String ENTITY = "entity";
    public static final String FOREIGNERKEY = "foreignerKey";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* 休假記錄 */
    private String entity;
    /* 考勤修正 */
    private String foreignerKey;
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

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public void setForeignerKey(String foreignerKey) {
        this.foreignerKey = foreignerKey;
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

    public String getEntity() {
        return entity;
    }

    public String getForeignerKey() {
        return foreignerKey;
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
