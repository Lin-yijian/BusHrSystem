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
import io.edap.util.StringUtil;
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
    public void updateOrSaveRecords(Set<String> checkDateList, ModuleEnum moduleEnum, boolean isGood) {

        if (CollectionUtils.isEmpty(checkDateList)) {
            throw new BusinessException("99", "日期参数不能为空");
        }

        String tenantId = InvocationInfoProxy.getTenantid();

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

    @Override
    public void updateOrSaveRecord(String checkDate, ModuleEnum moduleEnum, boolean isGood) {
        if (StringUtil.isEmpty(checkDate)) {
            throw new BusinessException("99", "日期参数不能为空");
        }
        String tenantId = InvocationInfoProxy.getTenantid();
//        ModuleEnum moduleEnum = ModuleEnum.getByCode(module);


        LambdaQueryCondition<TcmAttendanceCheckRecord> condition = new LambdaQueryCondition<>(new TcmAttendanceCheckRecord());
        condition.eq(TcmAttendanceCheckRecord::getCheck_date, checkDate)
                .eq(TcmAttendanceCheckRecord::getYtenantId, tenantId)
                .eq(TcmAttendanceCheckRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmAttendanceCheckRecord> records = billRepository.queryByClause(condition);

        if (records.isEmpty()) {
            TcmAttendanceCheckRecord newRecord = createNewRecord(checkDate, tenantId, moduleEnum, isGood);
            records.add(newRecord);
            billRepository.insert(records);
            return;
        }

        boolean flag = false;
        for (TcmAttendanceCheckRecord record : records) {
            flag = updateRecordByModule(record, moduleEnum, isGood);
        }
        if (!flag){
            billRepository.update(records);
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
    private boolean updateRecordByModule(TcmAttendanceCheckRecord record, ModuleEnum moduleEnum, boolean isGood){
        Boolean flag = null;
        switch (moduleEnum) {
            case TCM_ABSENCE_DATA_RECORD:
                flag = record.getAbsenteeism_record();
                record.setAbsenteeism_record(isGood);
                break;
            case TCM_SHEDULE_SHIFTS:
                flag = record.getSchedule_data();
                record.setSchedule_data(isGood);
                break;
            case TCM_HI_SENSE_ATTENDANCE:
                flag = record.getHisense_attendance();
                record.setHisense_attendance(isGood);
                break;
            case TCM_BUS_FAULT_DATA_RECORD:
                flag = record.getBus_fault();
                record.setBus_fault(isGood);
                break;
            case TCM_RESERVE_CAPTAIN_ATTENDANCE_RECORD:
                flag = record.getReserve_driver_absenteeism();
                record.setReserve_driver_absenteeism(isGood);
                break;
            case TCM_DRIVER_OVERTIME:
                flag = record.getDriver_overtime();
                record.setDriver_overtime(isGood);
                break;
            case TCM_CONTROL_CENTER_LOG:
                flag = record.getControl_center_log();
                record.setControl_center_log(isGood);
                break;
            case TCM_FUEL_GAS_DATA_RECORD:
                flag = record.getRefueling_times();
                record.setRefueling_times(isGood);
                break;
            case TCM_ATTENDANCE_CHECK_RECORD:
                flag = record.getFirst_station_punch();
                record.setFirst_station_punch(isGood);
                break;
            default:
                throw new BusinessException("100", "不支持的模块类型：" + moduleEnum.getCode());
        }

        return flag == isGood;

    }

    /**
     * 创建新的考勤记录
     */
    private TcmAttendanceCheckRecord createNewRecord(String checkDate, String tenantId,
                                                     ModuleEnum moduleEnum, boolean isGood) {
        TcmAttendanceCheckRecord record = new TcmAttendanceCheckRecord();
        // 设置默认值
        record.setCheck_date(checkDate);
        record.setCheck_status(BaseEnum.CHECK_STATUS_0.getCode());
        record.setYtenantId(tenantId);
        record.setDr(BaseEnum.IS_NOT_DELETED.getIntCode());
        record.setAbsenteeism_record(false);
        record.setSchedule_data(false);
        record.setHisense_attendance(false);
        record.setBus_fault(false);
        record.setFirst_station_punch(false);
        record.setReserve_driver_absenteeism(false);
        record.setDriver_overtime(false);
        record.setControl_center_log(false);
        record.setRefueling_times(false);
        updateRecordByModule(record,moduleEnum,isGood);
        return record;
    }



}
