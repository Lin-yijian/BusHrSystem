package com.yonyou.ucf.mdf.sample.utils;

import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QuerySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommonUtil {

    @Autowired
    protected IBillQueryRepository queryService;

    public final String FIELD_CODE_WAYBILL_EMPLOYEE_ID = "waybill_employeeId";
    public final String FIELD_CODE_EMPLOYEE_ID = "code";
    public final String FIELD_CODE_POST_CODE = "postCode";
    public final String FIELD_CODE_STAFF_ID = "id";


    /** 兼职司机岗位编码 */
    public final String PART_TIME_JOB_CODE = "P0020103_NC07";


    /**
     * 根据澳巴工号获取员工信息
     * @param ids 澳巴工号集合
     * @param tenantId 租户ID
     * @return k->澳巴工号   v->员工信息ID
     */
    public Map<String,String> getStaffByCardIds(Collection<String> ids, String tenantId) {

        List<Map<String, Object>> mapList = getStaffByEmployeeIds(ids,tenantId);

        if (mapList == null || mapList.isEmpty()) {
            return new HashMap<>();
        }

        Map<String,String> finalMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            finalMap.put((String) map.get(FIELD_CODE_WAYBILL_EMPLOYEE_ID),(String) map.get(FIELD_CODE_STAFF_ID));
        }

        return finalMap;

    }

    /**
     * 根据澳巴工号获取员工信息
     * @param ids 澳巴工号集合
     * @param tenantId 租户ID
     * @param tenantId 租户ID
     * @param fieldKeyName 键字段名
     * @param fieldValueName 值字段名
     * @return k->自定义内容（键字段名）   v->自定义内容（值字段名）
     */
    public Map<String,String> getStaffMapByCardIds(Collection<String> ids, String tenantId, String fieldKeyName, String fieldValueName) {

        List<Map<String, Object>> mapList = getStaffByEmployeeIds(ids,tenantId);

        if (mapList == null || mapList.isEmpty()) {
            return new HashMap<>();
        }

        Map<String,String> finalMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            finalMap.put((String) map.get(fieldKeyName),(String) map.get(fieldValueName));
        }

        return finalMap;

    }


    /**
     * 根据澳巴工号获取员工信息
     * @param ids 澳巴工号集合
     * @param tenantId 租户ID
     * @param tenantId 租户ID
     * @param fieldKeyName 键字段名
     * @return k->自定义内容（键字段名）   v->信息对象
     */
    public Map<String,Map<String, Object>> getStaffMapByCardIds(Collection<String> ids, String tenantId, String fieldKeyName) {

        List<Map<String, Object>> mapList = getStaffByEmployeeIds(ids,tenantId);

        if (mapList == null || mapList.isEmpty()) {
            return new HashMap<>();
        }

        Map<String,Map<String, Object>> finalMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            finalMap.put((String) map.get(fieldKeyName),map);
        }

        return finalMap;

    }


    /**
     * 根据澳巴工号查询员工信息
     * @param ids 澳巴工号集合
     * @param tenantId 租户ID
     * @return .
     */
    public List<Map<String, Object>> getStaffByEmployeeIds(Collection<String> ids, String tenantId) {
        QuerySchema querySchema = QuerySchema.create().addSelect("id, staffDefines.ab_01 as waybill_employeeId, postId, postId.code as postCode,code");
        querySchema.appendQueryCondition(QueryCondition.name("staffDefines.ab_01").in(ids));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());

        return mapList;

    }




    /**
     * 根据澳巴工号获取员工信息
     * @param id 澳巴工号
     * @param tenantId 租户ID
     * @return 员工信息ID
     */
    public String getStaffByCardId(String id, String tenantId) {
        QuerySchema querySchema = QuerySchema.create().addSelect("id, staffDefines.ab_01 as waybill_employeeId,code");
        querySchema.appendQueryCondition(QueryCondition.name("staffDefines.ab_01").eq(id));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());

        if (mapList == null || mapList.isEmpty()) {
            throw new RuntimeException("工號匹配失敗，請先錄入該工號人員信息，澳巴工號："+id);
        }

        return (String) mapList.get(0).get(FIELD_CODE_STAFF_ID);

    }



    /**
     * 根据员工编号获取员工信息（批量）
     * @param codes 员工编号集合
     * @param tenantId 租户ID
     * @return k->员工编号   v->员工信息ID
     */
    public Map<String,String> getStaffByCodes(List<String> codes, String tenantId) {
        if (codes == null || codes.isEmpty()) {
            return new HashMap<>();
        }

        QuerySchema querySchema = QuerySchema.create().addSelect("id, code");
        querySchema.appendQueryCondition(QueryCondition.name("code").in(codes));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());

        if (mapList == null || mapList.isEmpty()) {
            return new HashMap<>();
        }

        Map<String,String> finalMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            finalMap.put((String) map.get(FIELD_CODE_EMPLOYEE_ID), (String) map.get(FIELD_CODE_STAFF_ID));
        }

        return finalMap;
    }



}
