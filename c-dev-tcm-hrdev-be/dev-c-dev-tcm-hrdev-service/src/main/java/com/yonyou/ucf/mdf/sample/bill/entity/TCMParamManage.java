/*--------------------JavaPOJO for 參數管理(TCMParamManage)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 參數管理
 * @Date 2026-08-30 18:49:00
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMBaseInfo.TCMBaseInfo.TCMParamManage", domain = "c-dev-tcm-hrdev")
public class TCMParamManage extends SuperDO implements ICode,IEnable,ILogicDelete {
    public static final String ENTITY_NAME = "TCMBaseInfo.TCMBaseInfo.TCMParamManage";
    public static final String KEY = "key";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String MODULE = "module";
    public static final String REMARK = "remark";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DISABLETS = "disablets";
    public static final String DR = "dr";
    public static final String ENABLE = "enable";
    public static final String ENABLETS = "enablets";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* 標識 */
    private String key;
    /* 參數名稱 */
    private String name;
    /* 參數值 */
    private String value;
    /* 所屬模塊 */
    private String module;
    /* 説明 */
    private String remark;
    /* 编码 */
    private String code;
    /* 创建时间 */
    private Date createTime;
    /* 创建人 */
    private String creator;
    /* 停用时间 */
    private Date disablets;
    /* 逻辑删除 */
    private Short dr;
    /* 档案状态 */
    private String enable;
    /* 启用时间 */
    private Date enablets;
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

    public void setKey(String key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public void setDisablets(Date disablets) {
        this.disablets = disablets;
    }

    public void setDr(Short dr) {
        this.dr = dr;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public void setEnablets(Date enablets) {
        this.enablets = enablets;
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

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getModule() {
        return module;
    }

    public String getRemark() {
        return remark;
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

    public Date getDisablets() {
        return disablets;
    }

    public Short getDr() {
        return dr;
    }

    public String getEnable() {
        return enable;
    }

    public Date getEnablets() {
        return enablets;
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
