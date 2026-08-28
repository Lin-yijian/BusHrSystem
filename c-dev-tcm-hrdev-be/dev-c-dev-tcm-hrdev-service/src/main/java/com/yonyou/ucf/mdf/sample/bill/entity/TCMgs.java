package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 工傷記錄
 * @Date 2026-08-17 16:00:59
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMBaseInfo.TCMBaseInfo.TCMgs", domain = "c-dev-tcm-hrdev")
public class TCMgs extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMBaseInfo.TCMBaseInfo.TCMgs";
    public static final String INSETNEW9 = "insetnew9";
    public static final String GS1 = "gs1";
    public static final String GS2 = "gs2";
    public static final String INSETNEW10 = "insetnew10";
    public static final String AB02_C = "aB02_C";
    public static final String GS3 = "gs3";
    public static final String INSETNEW12 = "insetnew12";
    public static final String INSETNEW13 = "insetnew13";
    public static final String INSETNEW14 = "insetnew14";
    public static final String INSETNEW15 = "insetnew15";
    public static final String GS4 = "gs4";
    public static final String INSETNEW23 = "insetnew23";
    public static final String GS5 = "gs5";
    public static final String INSETNEW17 = "insetnew17";
    public static final String INSETNEW18 = "insetnew18";
    public static final String INSETNEW22 = "insetnew22";
    public static final String INSETNEW20 = "insetnew20";
    public static final String GS6 = "gs6";
    public static final String INSETNEW21 = "insetnew21";
    public static final String INSETNEW24 = "insetnew24";
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

    /* 人力資源部獲悉工傷意外日期 */
    private String insetnew9;
    /* 工號 */
    private String gs1;
    /* 工傷意外發生日期 */
    private String gs2;
    /* 工傷意外發生時間 */
    private String insetnew10;
    /* 是否申報勞工局及保險公司 */
    private String aB02_C;
    /* 工傷假期天數 */
    private BigDecimal gs3;
    /* 工傷假開始日 */
    private String insetnew12;
    /* 工傷完結日 */
    private String insetnew13;
    /* 不計算天數 */
    private BigDecimal insetnew14;
    /* 當作無薪假? */
    private String insetnew15;
    /* 醫療單收據總額 */
    private BigDecimal gs4;
    /* 康復証明 */
    private String insetnew23;
    /* 保險公司理賠2/3基本報酬總額 */
    private BigDecimal gs5;
    /* 保險公司理賠醫療單收據 */
    private BigDecimal insetnew17;
    /* 理賠總額 累計 */
    private BigDecimal insetnew18;
    /* 員工簽收 */
    private String insetnew22;
    /* 勞工局歸檔 */
    private String insetnew20;
    /* 工傷個案類型 */
    private String gs6;
    /* 工傷意外發生地點 */
    private String insetnew21;
    /* 受傷部位 */
    private String insetnew24;
    /* 詳情 */
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

    public void setInsetnew9(String insetnew9) {
        this.insetnew9 = insetnew9;
    }

    public void setGs1(String gs1) {
        this.gs1 = gs1;
    }

    public void setGs2(String gs2) {
        this.gs2 = gs2;
    }

    public void setInsetnew10(String insetnew10) {
        this.insetnew10 = insetnew10;
    }

    public void setaB02_C(String aB02_C) {
        this.aB02_C = aB02_C;
    }

    public void setGs3(BigDecimal gs3) {
        this.gs3 = gs3;
    }

    public void setInsetnew12(String insetnew12) {
        this.insetnew12 = insetnew12;
    }

    public void setInsetnew13(String insetnew13) {
        this.insetnew13 = insetnew13;
    }

    public void setInsetnew14(BigDecimal insetnew14) {
        this.insetnew14 = insetnew14;
    }

    public void setInsetnew15(String insetnew15) {
        this.insetnew15 = insetnew15;
    }

    public void setGs4(BigDecimal gs4) {
        this.gs4 = gs4;
    }

    public void setInsetnew23(String insetnew23) {
        this.insetnew23 = insetnew23;
    }

    public void setGs5(BigDecimal gs5) {
        this.gs5 = gs5;
    }

    public void setInsetnew17(BigDecimal insetnew17) {
        this.insetnew17 = insetnew17;
    }

    public void setInsetnew18(BigDecimal insetnew18) {
        this.insetnew18 = insetnew18;
    }

    public void setInsetnew22(String insetnew22) {
        this.insetnew22 = insetnew22;
    }

    public void setInsetnew20(String insetnew20) {
        this.insetnew20 = insetnew20;
    }

    public void setGs6(String gs6) {
        this.gs6 = gs6;
    }

    public void setInsetnew21(String insetnew21) {
        this.insetnew21 = insetnew21;
    }

    public void setInsetnew24(String insetnew24) {
        this.insetnew24 = insetnew24;
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

    public String getInsetnew9() {
        return insetnew9;
    }

    public String getGs1() {
        return gs1;
    }

    public String getGs2() {
        return gs2;
    }

    public String getInsetnew10() {
        return insetnew10;
    }

    public String getaB02_C() {
        return aB02_C;
    }

    public BigDecimal getGs3() {
        return gs3;
    }

    public String getInsetnew12() {
        return insetnew12;
    }

    public String getInsetnew13() {
        return insetnew13;
    }

    public BigDecimal getInsetnew14() {
        return insetnew14;
    }

    public String getInsetnew15() {
        return insetnew15;
    }

    public BigDecimal getGs4() {
        return gs4;
    }

    public String getInsetnew23() {
        return insetnew23;
    }

    public BigDecimal getGs5() {
        return gs5;
    }

    public BigDecimal getInsetnew17() {
        return insetnew17;
    }

    public BigDecimal getInsetnew18() {
        return insetnew18;
    }

    public String getInsetnew22() {
        return insetnew22;
    }

    public String getInsetnew20() {
        return insetnew20;
    }

    public String getGs6() {
        return gs6;
    }

    public String getInsetnew21() {
        return insetnew21;
    }

    public String getInsetnew24() {
        return insetnew24;
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
