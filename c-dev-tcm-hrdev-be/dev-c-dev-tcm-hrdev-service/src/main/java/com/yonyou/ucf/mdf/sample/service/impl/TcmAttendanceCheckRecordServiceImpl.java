package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.cloud.utils.CollectionUtils;
import com.yonyou.iuap.BusinessException;
import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCheckRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmHisenseAttendanceData;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TcmAttendanceCheckRecordServiceImpl implements TcmAttendanceCheckRecordService {


    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private YmsOidGenerator ymsOidGenerator;

    @Autowired
    protected IBillQueryRepository queryService;



    @Override
    public void updateOrSaveRecord(Set<String> checkDateList, String module, boolean isGood) {

        if (CollectionUtils.isEmpty(checkDateList)) {
            throw new BusinessException("99", "日期参数不能为空");
        }

        String tenantId = InvocationInfoProxy.getTenantid();
        ModuleEnum moduleEnum = ModuleEnum.getByCode(module);

        List<TcmAttendanceCheckRecord> existingRecords = findRecordsByDates(checkDateList, tenantId);

        // 获取已存在的日期集合
        Set<String> existingDates = existingRecords.stream()
                .map(TcmAttendanceCheckRecord::getCheck_date)
                .collect(Collectors.toSet());

        // 更新现有记录
        if (!existingRecords.isEmpty()) {
            for (TcmAttendanceCheckRecord record : existingRecords) {
                updateRecordByModule(record,moduleEnum,isGood);
            }
            billRepository.update(existingRecords);
        }

        // 为不存在的日期创建新记录
        List<TcmAttendanceCheckRecord> newRecords = checkDateList.stream()
                .filter(date -> !existingDates.contains(date))
                .map(date -> createNewRecord(date, tenantId, moduleEnum, isGood))
                .collect(Collectors.toList());

        if (!newRecords.isEmpty()) {
            billRepository.insert(newRecords);
        }
    }

    /**
     * 根据日期和租户查询考勤记录
     */
    private List<TcmAttendanceCheckRecord> findRecordsByDates(Set<String> checkDateList, String tenantId) {
        LambdaQueryCondition<TcmAttendanceCheckRecord> condition = new LambdaQueryCondition<>(new TcmAttendanceCheckRecord());
        condition.in(TcmAttendanceCheckRecord::getCheck_date, checkDateList)
                .eq(TcmAttendanceCheckRecord::getYtenantId, tenantId)
                .eq(TcmAttendanceCheckRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(condition);
    }

    /**
     * 根据模块更新对应的字段
     */
    private void updateRecordByModule(TcmAttendanceCheckRecord record, ModuleEnum moduleEnum, boolean isGood){
        switch (moduleEnum) {
            case TCM_ABSENCE_DATA_RECORD:
                record.setAbsenteeism_record(isGood);
                break;
            case TCM_SHEDULE_SHIFTS:
                record.setSchedule_data(isGood);
                break;
            case TCM_HI_SENSE_ATTENDANCE:
                record.setHisense_attendance(isGood);
                break;
            case TCM_BUS_FAULT_DATA_RECORD:
                record.setBus_fault(isGood);
                break;
            case TCM_RESERVE_CAPTAIN_ATTENDANCE_RECORD:
                record.setReserve_driver_absenteeism(isGood);
                break;
            case TCM_DRIVER_OVERTIME:
                record.setDriver_overtime(isGood);
                break;
            case TCM_CONTROL_CENTER_LOG:
                record.setControl_center_log(isGood);
                break;
            case TCM_FUEL_GAS_DATA_RECORD:
                record.setRefueling_times(isGood);
                break;
            default:
                throw new BusinessException("100", "不支持的模块类型：" + moduleEnum.getCode());
        }
    }

    /**
     * 创建新的考勤记录
     */
    private TcmAttendanceCheckRecord createNewRecord(String checkDate, String tenantId,
                                                     ModuleEnum moduleEnum, boolean isGood) {
        TcmAttendanceCheckRecord record = new TcmAttendanceCheckRecord();
        // 设置默认值
        record.setCheck_date(checkDate);
        record.setYtenantId(tenantId);
        record.setDr(BaseEnum.IS_NOT_DELETED.getIntCode());
        record.setAbsenteeism_record(false);
        record.setSchedule_data(false);
        record.setHisense_attendance(false);
        record.setBus_fault(false);
        record.setReserve_driver_absenteeism(false);
        record.setDriver_overtime(false);
        record.setControl_center_log(false);
        record.setRefueling_times(false);
        updateRecordByModule(record,moduleEnum,isGood);
        return record;
    }



}
