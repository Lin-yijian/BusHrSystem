package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 其他培訓
 * @Date 2026-07-16 15:34:25
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMBaseInfo.TCMBaseInfo.TCMqttraining", domain = "c-dev-tcm-hrdev")
public class TCMqttraining extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMBaseInfo.TCMBaseInfo.TCMqttraining";
    public static final String QT01 = "qt01";
    public static final String QT02 = "qt02";
    public static final String QT03 = "qt03";
    public static final String QT04 = "qt04";
    public static final String QT05 = "qt05";
    public static final String QT06 = "qt06";
    public static final String QT07 = "qt07";
    public static final String QT08 = "qt08";
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
    private String qt01;
    /* 培訓日期 */
    private String qt02;
    /* 培訓時間 */
    private String qt03;
    /* 缺勤狀態 */
    private String qt04;
    /* 培訓名稱 */
    private String qt05;
    /* 津貼金額 */
    private BigDecimal qt06;
    /* 發放月份 */
    private String qt07;
    /* 備注 */
    private String qt08;
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

    public void setQt01(String qt01) {
        this.qt01 = qt01;
    }

    public void setQt02(String qt02) {
        this.qt02 = qt02;
    }

    public void setQt03(String qt03) {
        this.qt03 = qt03;
    }

    public void setQt04(String qt04) {
        this.qt04 = qt04;
    }

    public void setQt05(String qt05) {
        this.qt05 = qt05;
    }

    public void setQt06(BigDecimal qt06) {
        this.qt06 = qt06;
    }

    public void setQt07(String qt07) {
        this.qt07 = qt07;
    }

    public void setQt08(String qt08) {
        this.qt08 = qt08;
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

    public String getQt01() {
        return qt01;
    }

    public String getQt02() {
        return qt02;
    }

    public String getQt03() {
        return qt03;
    }

    public String getQt04() {
        return qt04;
    }

    public String getQt05() {
        return qt05;
    }

    public BigDecimal getQt06() {
        return qt06;
    }

    public String getQt07() {
        return qt07;
    }

    public String getQt08() {
        return qt08;
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

