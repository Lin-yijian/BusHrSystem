package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 车辆信息
 * @Date 2026-04-22 13:56:11
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMBaseInfo.TCMBaseInfo.TCMCar", domain = "c-dev-tcm-hrdev")
public class TCMCar extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMBaseInfo.TCMBaseInfo.TCMCar";
    public static final String INTERNAL_NO = "Internal_no";
    public static final String PLATE_NUMBER = "plate_number";
    public static final String CAR_TYPE = "car_type";
    public static final String RUN_TYPE = "run_type";
    public static final String SOURCE = "source";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* 内部編號 */
    private String Internal_no;
    /* 車牌號 */
    private String plate_number;
    /* 車型 */
    private String car_type;
    /* 運作類型 */
    private String run_type;
    /* 數據類型 */
    private String source;
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

    public void setInternal_no(String Internal_no) {
        this.Internal_no = Internal_no;
    }

    public void setPlate_number(String plate_number) {
        this.plate_number = plate_number;
    }

    public void setCar_type(String car_type) {
        this.car_type = car_type;
    }

    public void setRun_type(String run_type) {
        this.run_type = run_type;
    }

    public void setSource(String source) {
        this.source = source;
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

    public String getInternal_no() {
        return Internal_no;
    }

    public String getPlate_number() {
        return plate_number;
    }

    public String getCar_type() {
        return car_type;
    }

    public String getRun_type() {
        return run_type;
    }

    public String getSource() {
        return source;
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

