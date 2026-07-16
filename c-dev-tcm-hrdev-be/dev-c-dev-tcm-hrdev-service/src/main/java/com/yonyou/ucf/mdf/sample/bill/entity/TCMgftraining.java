package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 公费培训
 * @Date 2026-07-16 15:19:02
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMBaseInfo.TCMBaseInfo.TCMgftraining", domain = "c-dev-tcm-hrdev")
public class TCMgftraining extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMBaseInfo.TCMBaseInfo.TCMgftraining";
    public static final String GF01 = "gf01";
    public static final String GF02 = "gf02";
    public static final String GF05 = "gf05";
    public static final String GF06 = "gf06";
    public static final String GF07 = "gf07";
    public static final String GF08 = "gf08";
    public static final String GF09 = "gf09";
    public static final String GF10 = "gf10";
    public static final String GF11 = "gf11";
    public static final String GF12 = "gf12";
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

    /* 培訓名稱 */
    private String gf01;
    /* 工號 */
    private String gf02;
    /* 培訓合同生效日期 */
    private String gf05;
    /* 預付費用 */
    private BigDecimal gf06;
    /* 已退回培訓費用 */
    private BigDecimal gf07;
    /* 考試結果 */
    private String gf08;
    /* 是否違約 */
    private String gf09;
    /* 服務期限 */
    private String gf10;
    /* 員工當前服務届滿日期 */
    private String gf11;
    /* 本培訓届滿日期 */
    private String gf12;
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

    public void setGf01(String gf01) {
        this.gf01 = gf01;
    }

    public void setGf02(String gf02) {
        this.gf02 = gf02;
    }

    public void setGf05(String gf05) {
        this.gf05 = gf05;
    }

    public void setGf06(BigDecimal gf06) {
        this.gf06 = gf06;
    }

    public void setGf07(BigDecimal gf07) {
        this.gf07 = gf07;
    }

    public void setGf08(String gf08) {
        this.gf08 = gf08;
    }

    public void setGf09(String gf09) {
        this.gf09 = gf09;
    }

    public void setGf10(String gf10) {
        this.gf10 = gf10;
    }

    public void setGf11(String gf11) {
        this.gf11 = gf11;
    }

    public void setGf12(String gf12) {
        this.gf12 = gf12;
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

    public String getGf01() {
        return gf01;
    }

    public String getGf02() {
        return gf02;
    }

    public String getGf05() {
        return gf05;
    }

    public BigDecimal getGf06() {
        return gf06;
    }

    public BigDecimal getGf07() {
        return gf07;
    }

    public String getGf08() {
        return gf08;
    }

    public String getGf09() {
        return gf09;
    }

    public String getGf10() {
        return gf10;
    }

    public String getGf11() {
        return gf11;
    }

    public String getGf12() {
        return gf12;
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

