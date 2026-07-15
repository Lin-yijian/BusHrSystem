/*--------------------JavaPOJO for 安全獎指標(TCMSafety)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 安全獎指標
 * @Date 2026-07-15 15:35:05
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMBaseInfo.TCMBaseInfo.TCMSafety", domain = "c-dev-tcm-hrdev")
public class TCMSafety extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMBaseInfo.TCMBaseInfo.TCMSafety";
    public static final String INDEX_CODE = "index_code";
    public static final String TYPE = "type";
    public static final String CONTENT = "content";
    public static final String COMPUTER = "computer";
    public static final String PROPORTION = "proportion";
    public static final String ENABLE = "enable";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* 指標編號 */
    private String index_code;
    /* 指標類型 */
    private String type;
    /* 指標内容 */
    private String content;
    /* 計算方式 */
    private String computer;
    /* 每次獎懲比例 */
    private BigDecimal proportion;
    /* 啓停狀態 */
    private String enable;
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

    public void setIndex_code(String index_code) {
        this.index_code = index_code;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setComputer(String computer) {
        this.computer = computer;
    }

    public void setProportion(BigDecimal proportion) {
        this.proportion = proportion;
    }

    public void setEnable(String enable) {
        this.enable = enable;
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

    public String getIndex_code() {
        return index_code;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getComputer() {
        return computer;
    }

    public BigDecimal getProportion() {
        return proportion;
    }

    public String getEnable() {
        return enable;
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
