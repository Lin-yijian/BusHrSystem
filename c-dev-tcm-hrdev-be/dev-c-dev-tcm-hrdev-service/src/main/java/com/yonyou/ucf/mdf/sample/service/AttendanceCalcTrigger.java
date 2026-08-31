package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.utils.AttendanceCalcImportContext;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Date;

/**
 * 考勤计算统一触发入口。
 * 手工保存：按人计算；导入：afterCommit 批量同步修正后按日计算。
 */
@Slf4j
@Component
public class AttendanceCalcTrigger {

    @Autowired
    private TcmAttendanceCalculationService calculationService;

    @Autowired
    private AttendanceCorrectionSyncService correctionSyncService;

    @Autowired
    private CommonUtil commonUtil;

    public void triggerByDate(String date) {
        if (StringUtils.isBlank(date)) {
            return;
        }
        try {
            calculationService.getAndUpdateAssessReviseByDate(date);
        } catch (Exception e) {
            log.error("考勤计算触发失败(按日), date={}", date, e);
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
    }

    /**
     * 按员工编号计算（优先）；staffId 会解析为 code。
     */
    public void triggerByDateAndStaff(String date, String staffId) {
        if (StringUtils.isBlank(date)) {
            return;
        }
        String employeeCode = resolveEmployeeCode(staffId);
        if (StringUtils.isBlank(employeeCode)) {
            log.warn("无法解析员工编号，回退按日计算 date={}, staffId={}", date, staffId);
            triggerByDate(date);
            return;
        }
        try {
            calculationService.getAndUpdateAssessReviseByDate(parseDate(date), employeeCode);
        } catch (Exception e) {
            log.error("考勤计算触发失败(按人), date={}, code={}", date, employeeCode, e);
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
    }

    private Date parseDate(String date) {
        return com.yonyou.ucf.mdf.sample.utils.DateUtil.stringToDate(
                com.yonyou.ucf.mdf.sample.utils.DateUtil.YYYY_MM_DD, date);
    }

    private String resolveEmployeeCode(String staffId) {
        if (StringUtils.isBlank(staffId)) {
            return null;
        }
        try {
            return commonUtil.getStaffCodeById(staffId, InvocationInfoProxy.getTenantid());
        } catch (Exception e) {
            log.warn("解析员工编号失败 staffId={}", staffId, e);
            return null;
        }
    }

    /**
     * 页面保存后：立即按人计算。
     * 导入中：只收集日期并注册收尾（实体收集由各 Plugin 负责）。
     */
    public void afterDataChanged(String date, String staffId) {
        if (StringUtils.isBlank(date)) {
            return;
        }
        if (AttendanceCalcImportContext.isBulkSyncing()) {
            return;
        }
        if (AttendanceCalcImportContext.isImporting()) {
            AttendanceCalcImportContext.collectDate(date);
            registerImportFlushAfterCommit();
            return;
        }
        triggerByDateAndStaff(date, staffId);
    }

    /**
     * 兼容仅有日期的场景（如删除后无可靠员工）。
     */
    public void afterDataChanged(String date) {
        afterDataChanged(date, null);
    }

    public void registerImportFlushAfterCommit() {
        if (!AttendanceCalcImportContext.isImporting()) {
            return;
        }
        if (!AttendanceCalcImportContext.markSyncRegistered()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            flushImportPending();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                flushImportPending();
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    AttendanceCalcImportContext.clear();
                }
            }
        });
    }

    private void flushImportPending() {
        try {
            correctionSyncService.batchSyncFromSchedules(AttendanceCalcImportContext.snapshotSchedules());
            correctionSyncService.batchSyncFromReserves(AttendanceCalcImportContext.snapshotReserves());
            correctionSyncService.batchSyncFromHolidays(AttendanceCalcImportContext.snapshotHolidays());
            for (String date : AttendanceCalcImportContext.snapshotDates()) {
                try {
                    calculationService.getAndUpdateAssessReviseByDate(date);
                } catch (Exception e) {
                    log.error("导入后考勤计算失败, date={}", date, e);
                }
            }
        } finally {
            AttendanceCalcImportContext.clear();
        }
    }
}
