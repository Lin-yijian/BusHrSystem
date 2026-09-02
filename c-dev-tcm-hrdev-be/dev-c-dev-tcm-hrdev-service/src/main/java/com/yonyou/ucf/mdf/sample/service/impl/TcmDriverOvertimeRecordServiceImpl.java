package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.service.TcmDriverOvertimeRecordService;
import com.yonyou.ucf.mdf.sample.utils.DateUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import io.edap.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.imeta.core.base.ConditionOperator;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QuerySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class TcmDriverOvertimeRecordServiceImpl implements TcmDriverOvertimeRecordService {

    @Autowired
    private IBillQueryRepository queryService;

    @Override
    public boolean checkDataByDate(String date) {
        if (StringUtil.isEmpty(date)) {
            throw new RuntimeException("日期參數不能爲空");
        }
        QuerySchema querySchema = QuerySchema.create().addSelect("id");
        querySchema.appendQueryCondition(new QueryCondition<>("overtime_date", ConditionOperator.in, DateUtil.dateQueryVariants(date)));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> list = queryService.queryMapBySchema(
                UrlEnum.TCM_DRIVER_OVERTIME_RECORD.getUrl(), querySchema, UrlEnum.TCM_DRIVER_OVERTIME_RECORD.getDomainKey());
        return list != null && !list.isEmpty();
    }

    @Override
    public Set<String> findDatesWithData(Collection<String> dates) {
        if (dates == null || dates.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> variants = DateUtil.dateQueryVariants(dates);
        if (variants.isEmpty()) {
            return Collections.emptySet();
        }
        QuerySchema querySchema = QuerySchema.create().addSelect("overtime_date");
        querySchema.appendQueryCondition(new QueryCondition<>("overtime_date", ConditionOperator.in, variants));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> list = queryService.queryMapBySchema(
                UrlEnum.TCM_DRIVER_OVERTIME_RECORD.getUrl(), querySchema, UrlEnum.TCM_DRIVER_OVERTIME_RECORD.getDomainKey());
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>();
        for (Map<String, Object> row : list) {
            Object date = row.get("overtime_date");
            if (date == null) {
                continue;
            }
            String normalized = DateUtil.normalizeToYyyyMmDd(String.valueOf(date));
            if (normalized != null) {
                result.add(normalized);
            }
        }
        return result;
    }
}
