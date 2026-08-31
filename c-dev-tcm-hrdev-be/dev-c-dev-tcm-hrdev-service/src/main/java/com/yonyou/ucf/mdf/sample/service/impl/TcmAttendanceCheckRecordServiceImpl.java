package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.cloud.utils.CollectionUtils;
import com.yonyou.iuap.BusinessException;
import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCheckRecord;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.DateUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import io.edap.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TcmAttendanceCheckRecordServiceImpl implements TcmAttendanceCheckRecordService {

    private final Object writeLock = new Object();

    @Autowired
    private IBillRepository billRepository;

    @Override
    public void updateOrSaveRecords(Set<String> checkDateList, ModuleEnum moduleEnum, boolean isGood) {
        Set<String> normalizedDates = normalizeDateSet(checkDateList);
        if (normalizedDates.isEmpty()) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        synchronized (writeLock) {
            List<TcmAttendanceCheckRecord> existingRecords = findRecordsByDates(normalizedDates, tenantId);
            Set<String> existingDates = existingRecords.stream()
                    .map(record -> DateUtil.normalizeToYyyyMmDd(record.getCheck_date()))
                    .filter(date -> !StringUtil.isEmpty(date))
                    .collect(Collectors.toSet());

            if (!existingRecords.isEmpty()) {
                for (TcmAttendanceCheckRecord record : existingRecords) {
                    updateRecordByModule(record, moduleEnum, isGood);
                    normalizeStoredDate(record);
                }
                CommonUtil.fillUpdateAudit(existingRecords);
                billRepository.update(existingRecords);
            }

            List<TcmAttendanceCheckRecord> newRecords = normalizedDates.stream()
                    .filter(date -> !existingDates.contains(date))
                    .map(date -> createNewRecord(date, tenantId, moduleEnum, isGood))
                    .collect(Collectors.toList());

            if (!newRecords.isEmpty()) {
                CommonUtil.fillInsertAudit(newRecords);
                billRepository.insert(newRecords);
            }
        }
    }

    @Override
    public void updateOrSaveRecord(String checkDate, ModuleEnum moduleEnum, boolean isGood) {
        String normalizedDate = DateUtil.normalizeToYyyyMmDd(checkDate);
        if (StringUtil.isEmpty(normalizedDate)) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        synchronized (writeLock) {
            List<TcmAttendanceCheckRecord> records = findRecordsByDate(normalizedDate, tenantId);
            if (records.isEmpty()) {
                TcmAttendanceCheckRecord newRecord = createNewRecord(normalizedDate, tenantId, moduleEnum, isGood);
                CommonUtil.fillInsertAudit(newRecord);
                billRepository.insert(Collections.singletonList(newRecord));
                return;
            }

            boolean needUpdate = false;
            for (TcmAttendanceCheckRecord record : records) {
                boolean alreadySame = updateRecordByModule(record, moduleEnum, isGood);
                boolean dateNormalized = normalizeStoredDate(record);
                if (!alreadySame || dateNormalized) {
                    needUpdate = true;
                }
            }
            if (needUpdate) {
                CommonUtil.fillUpdateAudit(records);
                billRepository.update(records);
            }
        }
    }

    @Override
    public void updateStatus(String checkDate, String status) {
        String tenantId = InvocationInfoProxy.getTenantid();
        synchronized (writeLock) {
            List<TcmAttendanceCheckRecord> list = findRecordsByDate(checkDate, tenantId);
            if (list.isEmpty()) {
                return;
            }
            for (TcmAttendanceCheckRecord record : list) {
                record.setCheck_status(status);
                normalizeStoredDate(record);
            }
            CommonUtil.fillUpdateAudit(list);
            billRepository.update(list);
        }
    }

    private Set<String> normalizeDateSet(Set<String> checkDateList) {
        if (CollectionUtils.isEmpty(checkDateList)) {
            return Collections.emptySet();
        }
        Set<String> normalized = new HashSet<>();
        for (String date : checkDateList) {
            String n = DateUtil.normalizeToYyyyMmDd(date);
            if (!StringUtil.isEmpty(n)) {
                normalized.add(n);
            }
        }
        return normalized;
    }

    private List<TcmAttendanceCheckRecord> findRecordsByDates(Set<String> checkDateList, String tenantId) {
        Set<String> variants = DateUtil.dateQueryVariants(checkDateList);
        if (variants.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryCondition<TcmAttendanceCheckRecord> condition = new LambdaQueryCondition<>(new TcmAttendanceCheckRecord());
        condition.in(TcmAttendanceCheckRecord::getCheck_date, variants)
                .eq(TcmAttendanceCheckRecord::getYtenantId, tenantId)
                .eq(TcmAttendanceCheckRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(condition);
    }

    private List<TcmAttendanceCheckRecord> findRecordsByDate(String checkDate, String tenantId) {
        Set<String> variants = DateUtil.dateQueryVariants(checkDate);
        if (variants.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryCondition<TcmAttendanceCheckRecord> condition = new LambdaQueryCondition<>(new TcmAttendanceCheckRecord());
        condition.in(TcmAttendanceCheckRecord::getCheck_date, variants)
                .eq(TcmAttendanceCheckRecord::getYtenantId, tenantId)
                .eq(TcmAttendanceCheckRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(condition);
    }

    /**
     * 根据模块更新对应的字段。
     * @return true 表示更新前已是目标值
     */
    private boolean updateRecordByModule(TcmAttendanceCheckRecord record, ModuleEnum moduleEnum, boolean isGood) {
        Boolean flag;
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
        return flag != null && flag == isGood;
    }

    private TcmAttendanceCheckRecord createNewRecord(String checkDate, String tenantId,
                                                     ModuleEnum moduleEnum, boolean isGood) {
        TcmAttendanceCheckRecord record = new TcmAttendanceCheckRecord();
        record.setCheck_date(checkDate);
        record.setCheck_status(BaseEnum.CHECK_STATUS_0.getCode());
        record.setYtenantId(tenantId);
        record.setDr(BaseEnum.IS_NOT_DELETED.getIntCode().shortValue());
        record.setAbsenteeism_record(false);
        record.setSchedule_data(false);
        record.setHisense_attendance(false);
        record.setBus_fault(false);
        record.setFirst_station_punch(false);
        record.setReserve_driver_absenteeism(false);
        record.setDriver_overtime(false);
        record.setControl_center_log(false);
        record.setRefueling_times(false);
        updateRecordByModule(record, moduleEnum, isGood);
        return record;
    }

    private boolean normalizeStoredDate(TcmAttendanceCheckRecord record) {
        String normalized = DateUtil.normalizeToYyyyMmDd(record.getCheck_date());
        if (normalized != null && !normalized.equals(record.getCheck_date())) {
            record.setCheck_date(normalized);
            return true;
        }
        return false;
    }
}
