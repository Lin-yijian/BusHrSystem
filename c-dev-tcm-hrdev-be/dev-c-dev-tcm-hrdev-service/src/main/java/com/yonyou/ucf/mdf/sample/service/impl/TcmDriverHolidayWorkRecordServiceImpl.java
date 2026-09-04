package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.SFunction;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceDriverOvertimeRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverHolidayWorkRecord;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.service.TcmDriverHolidayWorkRecordService;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.apache.commons.lang3.StringUtils;
import org.imeta.core.base.ConditionOperator;
import org.imeta.orm.schema.SimpleCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TcmDriverHolidayWorkRecordServiceImpl implements TcmDriverHolidayWorkRecordService {

    @Autowired
    private IBillRepository billRepository;


    @Override
    public void removeByDateAndEmployeeIds(String date, Collection<String> employeeIds) {
        List<SimpleCondition> conditionList = new ArrayList<>();
        conditionList.add(new SimpleCondition("date", ConditionOperator.eq, date));
        conditionList.add(new SimpleCondition("employee_id", ConditionOperator.in, employeeIds));
        billRepository.batchRemove(UrlEnum.TCM_DRIVER_HOLIDAY_WORK_RECORD.getUrl(), conditionList);
    }

    @Override
    public void coverInsert(String date, List<TcmDriverHolidayWorkRecord> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<String> ids = list.stream().map(TcmDriverHolidayWorkRecord::getEmployee_id).collect(Collectors.toSet());
        removeByDateAndEmployeeIds(date, ids);
        CommonUtil.fillInsertAudit(list);
        billRepository.insert(list);
    }

    @Override
    public List<TcmDriverHolidayWorkRecord> getByDateAndWaybillEmployeeId(String date, String waybillEmployeeId) {
        return getByDateAndField(date, waybillEmployeeId, TcmDriverHolidayWorkRecord::getWaybill_employee_id);
    }


    @Override
    public void saveList(List<TcmDriverHolidayWorkRecord> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        for (TcmDriverHolidayWorkRecord record : list) {
            record.setYtenantId(tenantId);
        }
        CommonUtil.fillInsertAudit(list);
        billRepository.insert(list);
    }

    /**
     * 查询
     * @param date 。
     * @param fieldValue 。
     * @param fieldGetter 。
     * @return 。
     */
    private List<TcmDriverHolidayWorkRecord> getByDateAndField(String date, String fieldValue,
                                                            SFunction<TcmDriverHolidayWorkRecord, String> fieldGetter) {
        if (StringUtils.isBlank(fieldValue) || StringUtils.isBlank(date)) {
            return Collections.emptyList();
        }

        String tenantId = InvocationInfoProxy.getTenantid();
        LambdaQueryCondition<TcmDriverHolidayWorkRecord> routeCondition = new LambdaQueryCondition<>(new TcmDriverHolidayWorkRecord());
        routeCondition.eq(TcmDriverHolidayWorkRecord::getDate, date)
                .eq(fieldGetter, fieldValue)
                .eq(TcmDriverHolidayWorkRecord::getYtenantId, tenantId)
                .eq(TcmDriverHolidayWorkRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(routeCondition);
    }
}
