package com.yonyou.ucf.mdf.sample.bill.entity;

import com.yonyou.iuap.yms.annotation.YMSEntity;
import com.yonyou.ypd.bill.base.*;
import java.util.Date;
import com.yonyou.ypd.bill.basic.entity.SuperDO;

/**
 * @author root
 * @description 路线管理
 * @Date 2026-08-20 10:23:34
 * @since 2023/11/28
 **/
@YMSEntity(name = "TCMBaseInfo.TCMBaseInfo.TCMRoute", domain = "c-dev-tcm-hrdev")
public class TCMRoute extends SuperDO implements ICode,ILogicDelete {
    public static final String ENTITY_NAME = "TCMBaseInfo.TCMBaseInfo.TCMRoute";
    public static final String ROUTE_CODE = "route_code";
    public static final String ROUTE_DIRECTION = "route_direction";
    public static final String ROUTE_TYPE = "route_type";
    public static final String IS_NO_ROUTE = "is_no_route";
    public static final String IS_SPECIAL_ROUTE = "is_special_route";
    public static final String STATION = "station";
    public static final String CODE = "code";
    public static final String CREATETIME = "createTime";
    public static final String CREATOR = "creator";
    public static final String DR = "dr";
    public static final String ID = "id";
    public static final String MODIFIER = "modifier";
    public static final String MODIFYTIME = "modifyTime";
    public static final String PUBTS = "pubts";
    public static final String YTENANTID = "ytenantId";

    /* 路线编号 */
    private String route_code;
    /* 路线方向 */
    private String route_direction;
    /* 路线类型 */
    private String route_type;
    /* 是否N线 */
    private Boolean is_no_route;
    /* 是否特殊线路 */
    private Boolean is_special_route;
    /* 所属总站 */
    private String station;
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

    public void setRoute_code(String route_code) {
        this.route_code = route_code;
    }

    public void setRoute_direction(String route_direction) {
        this.route_direction = route_direction;
    }

    public void setRoute_type(String route_type) {
        this.route_type = route_type;
    }

    public void setIs_no_route(Boolean is_no_route) {
        this.is_no_route = is_no_route;
    }

    public void setIs_special_route(Boolean is_special_route) {
        this.is_special_route = is_special_route;
    }

    public void setStation(String station) {
        this.station = station;
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

    public String getRoute_code() {
        return route_code;
    }

    public String getRoute_direction() {
        return route_direction;
    }

    public String getRoute_type() {
        return route_type;
    }

    public Boolean getIs_no_route() {
        return is_no_route;
    }

    public Boolean getIs_special_route() {
        return is_special_route;
    }

    public String getStation() {
        return station;
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
