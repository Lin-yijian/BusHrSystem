package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 工傷記錄
 * @Date 2026-07-16 15:28:36
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMBaseInfo.TCMBaseInfo.TCMgs", domain = "c-dev-tcm-hrdev")
public class TCMgs extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMBaseInfo.TCMBaseInfo.TCMgs";
    public static final String GS1 = "gs1";
    public static final String GS2 = "gs2";
    public static final String GS3 = "gs3";
    public static final String GS4 = "gs4";
    public static final String GS5 = "gs5";
    public static final String GS6 = "gs6";
    public static final String GS7 = "gs7";
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

    /* 工號 */
    private String gs1;
    /* 工傷意外發生日期 */
    private String gs2;
    /* 工傷假期天數 */
    private BigDecimal gs3;
    /* 醫療單收據總額 */
    private BigDecimal gs4;
    /* 保險公司理賠2/3基本報酬總額 */
    private BigDecimal gs5;
    /* 工傷個案類型 */
    private String gs6;
    /* 備注 */
    private String gs7;
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

    public void setGs1(String gs1) {
        this.gs1 = gs1;
    }

    public void setGs2(String gs2) {
        this.gs2 = gs2;
    }

    public void setGs3(BigDecimal gs3) {
        this.gs3 = gs3;
    }

    public void setGs4(BigDecimal gs4) {
        this.gs4 = gs4;
    }

    public void setGs5(BigDecimal gs5) {
        this.gs5 = gs5;
    }

    public void setGs6(String gs6) {
        this.gs6 = gs6;
    }

    public void setGs7(String gs7) {
        this.gs7 = gs7;
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

    public String getGs1() {
        return gs1;
    }

    public String getGs2() {
        return gs2;
    }

    public BigDecimal getGs3() {
        return gs3;
    }

    public BigDecimal getGs4() {
        return gs4;
    }

    public BigDecimal getGs5() {
        return gs5;
    }

    public String getGs6() {
        return gs6;
    }

    public String getGs7() {
        return gs7;
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

