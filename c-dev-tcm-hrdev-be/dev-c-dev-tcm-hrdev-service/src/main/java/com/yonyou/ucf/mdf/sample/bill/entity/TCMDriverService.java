/*--------------------JavaPOJO for 司機服務記錄(TCMDriverService)--------------------*/
package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.math.BigDecimal;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 司機服務記錄
 * @Date 2026-07-14 11:42:56
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMBaseInfo.TCMBaseInfo.TCMDriverService", domain = "c-dev-tcm-hrdev")
public class TCMDriverService extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMBaseInfo.TCMBaseInfo.TCMDriverService";
    public static final String DATE = "date";
    public static final String DRIVER_NO = "driver_no";
    public static final String CONTENT = "content";
    public static final String ROUTE = "route";
    public static final String ROUTE_ROUTE_CODE = "route_route_code";
    public static final String DIVER_NAME = "diver_name";
    public static final String CAR = "car";
    public static final String CAR_PLATE_NUMBER = "car_plate_number";
    public static final String INDEX_NO = "index_no";
    public static final String TIME = "time";
    public static final String REWARD_RIO = "reward_rio";
    public static final String INDEX_DESC = "index_desc";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* 日期 */
    private String date;
    /* 工號 */
    private String driver_no;
    /* 内容 */
    private String content;
    /* 路綫編號 */
    private String route;
    /* 姓名 */
    private String diver_name;
    /* 車牌號 */
    private String car;
    /* 指標編號 */
    private String index_no;
    /* 時間 */
    private String time;
    /* 獎懲比例 */
    private BigDecimal reward_rio;
    /* 指標説明 */
    private String index_desc;
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

    public void setDate(String date) {
        this.date = date;
    }

    public void setDriver_no(String driver_no) {
        this.driver_no = driver_no;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setDiver_name(String diver_name) {
        this.diver_name = diver_name;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public void setIndex_no(String index_no) {
        this.index_no = index_no;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setReward_rio(BigDecimal reward_rio) {
        this.reward_rio = reward_rio;
    }

    public void setIndex_desc(String index_desc) {
        this.index_desc = index_desc;
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

    public String getDate() {
        return date;
    }

    public String getDriver_no() {
        return driver_no;
    }

    public String getContent() {
        return content;
    }

    public String getRoute() {
        return route;
    }

    public String getDiver_name() {
        return diver_name;
    }

    public String getCar() {
        return car;
    }

    public String getIndex_no() {
        return index_no;
    }

    public String getTime() {
        return time;
    }

    public BigDecimal getReward_rio() {
        return reward_rio;
    }

    public String getIndex_desc() {
        return index_desc;
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
