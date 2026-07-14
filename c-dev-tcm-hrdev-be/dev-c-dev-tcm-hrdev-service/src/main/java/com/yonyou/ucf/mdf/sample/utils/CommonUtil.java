package com.yonyou.ucf.mdf.sample.utils;

import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QuerySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommonUtil {

    @Autowired
    protected IBillQueryRepository queryService;


    public Map<String,String> getStaffByCardIds(List<String> ids, String tenantId) {
        QuerySchema querySchema = QuerySchema.create().addSelect("id, staffDefines.ab_01 as cardId,code");
        querySchema.appendQueryCondition(QueryCondition.name("staffDefines.ab_01").in(ids));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());

        if (mapList == null || mapList.isEmpty()) {
            return new HashMap<>();
        }

        Map<String,String> finalMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            finalMap.put((String) map.get("cardId"),(String) map.get("id"));
        }

        return finalMap;

    }

    public String getStaffByCardId(String id, String tenantId) {
        QuerySchema querySchema = QuerySchema.create().addSelect("id, staffDefines.ab_01 as cardId,code");
        querySchema.appendQueryCondition(QueryCondition.name("staffDefines.ab_01").eq(id));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());

        if (mapList == null || mapList.isEmpty()) {
            throw new RuntimeException("工號匹配失敗，請先錄入該工號人員信息，澳巴工號："+id);
        }

        return (String) mapList.get(0).get("id");

    }
}
