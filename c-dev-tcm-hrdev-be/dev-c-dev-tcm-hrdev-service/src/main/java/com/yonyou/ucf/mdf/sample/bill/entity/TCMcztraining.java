package com.yonyou.ucf.mdf.sample.bill.entity;


import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 車長入職培訓
 * @Date 2026-07-16 15:36:07
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMBaseInfo.TCMBaseInfo.TCMcztraining", domain = "c-dev-tcm-hrdev")
public class TCMcztraining extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMBaseInfo.TCMBaseInfo.TCMcztraining";
    public static final String CZ01 = "cz01";
    public static final String CZ02 = "cz02";
    public static final String CZ03 = "cz03";
    public static final String CZ04 = "cz04";
    public static final String CZ05 = "cz05";
    public static final String CZ06 = "cz06";
    public static final String CZ07 = "cz07";
    public static final String CZ08 = "cz08";
    public static final String CZ09 = "cz09";
    public static final String CZ10 = "cz10";
    public static final String WAYBILL_EMPLOYEE_ID = "waybill_employee_id";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* 員工信息 */
    private String cz01;
    /* 培訓日期 */
    private String cz02;
    /* 培訓開始時間 */
    private String cz03;
    /* 培訓結束時間 */
    private String cz04;
    /* 用膳時長 */
    private BigDecimal cz05;
    /* 培訓有效時長 */
    private BigDecimal cz06;
    /* 培訓津貼 */
    private BigDecimal cz07;
    /* 津貼時薪 */
    private BigDecimal cz08;
    /* 發放月份 */
    private String cz09;
    /* 備注 */
    private String cz10;
    /* 澳巴工號 */
    private String waybill_employee_id;
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

    public void setCz01(String cz01) {
        this.cz01 = cz01;
    }

    public void setCz02(String cz02) {
        this.cz02 = cz02;
    }

    public void setCz03(String cz03) {
        this.cz03 = cz03;
    }

    public void setCz04(String cz04) {
        this.cz04 = cz04;
    }

    public void setCz05(BigDecimal cz05) {
        this.cz05 = cz05;
    }

    public void setCz06(BigDecimal cz06) {
        this.cz06 = cz06;
    }

    public void setCz07(BigDecimal cz07) {
        this.cz07 = cz07;
    }

    public void setCz08(BigDecimal cz08) {
        this.cz08 = cz08;
    }

    public void setCz09(String cz09) {
        this.cz09 = cz09;
    }

    public void setCz10(String cz10) {
        this.cz10 = cz10;
    }

    public void setWaybill_employee_id(String waybill_employee_id) {
        this.waybill_employee_id = waybill_employee_id;
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

    public String getCz01() {
        return cz01;
    }

    public String getCz02() {
        return cz02;
    }

    public String getCz03() {
        return cz03;
    }

    public String getCz04() {
        return cz04;
    }

    public BigDecimal getCz05() {
        return cz05;
    }

    public BigDecimal getCz06() {
        return cz06;
    }

    public BigDecimal getCz07() {
        return cz07;
    }

    public BigDecimal getCz08() {
        return cz08;
    }

    public String getCz09() {
        return cz09;
    }

    public String getCz10() {
        return cz10;
    }

    public String getWaybill_employee_id() {
        return waybill_employee_id;
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

