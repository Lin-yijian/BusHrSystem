package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverHolidayWorkRecord;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.service.TcmDriverHolidayWorkRecordService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.imeta.core.base.ConditionOperator;
import org.imeta.orm.schema.SimpleCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
        billRepository.insert(list);
    }


}
