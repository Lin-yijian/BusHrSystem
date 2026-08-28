package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAbsenceDataRecord;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAbsenceDataRecordService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import io.edap.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TcmAbsenceDataRecordServiceImpl implements TcmAbsenceDataRecordService {


    @Autowired
    private IBillRepository billRepository;


    @Override
    public boolean checkDataByDate(String date) {
        return !getListByDate(date).isEmpty();
    }


    private List<TcmAbsenceDataRecord> getListByDate(String date) {
        if (StringUtil.isEmpty(date)) {
            throw new RuntimeException("日期參數不能爲空");
        }

        String tenantId = InvocationInfoProxy.getTenantid();

        LambdaQueryCondition<TcmAbsenceDataRecord> carCondition = new LambdaQueryCondition<>(new TcmAbsenceDataRecord());
        carCondition.eq(TcmAbsenceDataRecord::getDate, date)
                .eq(TcmAbsenceDataRecord::getYtenantId, tenantId)
                .eq(TcmAbsenceDataRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(carCondition);
    }
}
