package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 住院記錄
 * @Date 2026-07-16 15:31:15
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMBaseInfo.TCMBaseInfo.TCMzy01", domain = "c-dev-tcm-hrdev")
public class TCMzy01 extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMBaseInfo.TCMBaseInfo.TCMzy01";
    public static final String ZY1 = "zy1";
    public static final String ZY2 = "zy2";
    public static final String ZY3 = "zy3";
    public static final String ZY4 = "zy4";
    public static final String ZY5 = "zy5";
    public static final String ZY6 = "zy6";
    public static final String ZY7 = "zy7";
    public static final String ZY8 = "zy8";
    public static final String ZY9 = "zy9";
    public static final String ZY10 = "zy10";
    public static final String ZY11 = "zy11";
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
    private String zy1;
    /* 住院日期 */
    private String zy2;
    /* 表格提交日期 */
    private String zy3;
    /* 申請賠償金額 */
    private BigDecimal zy4;
    /* 診所/醫院 */
    private String zy5;
    /* 病症 */
    private String zy6;
    /* 出院日期 */
    private String zy7;
    /* 索償結果 */
    private String zy8;
    /* 賠償日期 */
    private String zy9;
    /* 賠償金額 */
    private BigDecimal zy10;
    /* 備注 */
    private String zy11;
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

    public void setZy1(String zy1) {
        this.zy1 = zy1;
    }

    public void setZy2(String zy2) {
        this.zy2 = zy2;
    }

    public void setZy3(String zy3) {
        this.zy3 = zy3;
    }

    public void setZy4(BigDecimal zy4) {
        this.zy4 = zy4;
    }

    public void setZy5(String zy5) {
        this.zy5 = zy5;
    }

    public void setZy6(String zy6) {
        this.zy6 = zy6;
    }

    public void setZy7(String zy7) {
        this.zy7 = zy7;
    }

    public void setZy8(String zy8) {
        this.zy8 = zy8;
    }

    public void setZy9(String zy9) {
        this.zy9 = zy9;
    }

    public void setZy10(BigDecimal zy10) {
        this.zy10 = zy10;
    }

    public void setZy11(String zy11) {
        this.zy11 = zy11;
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

    public String getZy1() {
        return zy1;
    }

    public String getZy2() {
        return zy2;
    }

    public String getZy3() {
        return zy3;
    }

    public BigDecimal getZy4() {
        return zy4;
    }

    public String getZy5() {
        return zy5;
    }

    public String getZy6() {
        return zy6;
    }

    public String getZy7() {
        return zy7;
    }

    public String getZy8() {
        return zy8;
    }

    public String getZy9() {
        return zy9;
    }

    public BigDecimal getZy10() {
        return zy10;
    }

    public String getZy11() {
        return zy11;
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
