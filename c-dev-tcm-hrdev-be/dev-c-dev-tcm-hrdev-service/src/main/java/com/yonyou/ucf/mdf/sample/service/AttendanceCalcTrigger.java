package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverHolidayWorkRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSheduleShiftsData;
import com.yonyou.ucf.mdf.sample.utils.AttendanceCalcImportContext;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 考勤计算统一触发入口。
 * 手工保存：按人计算。导入/删除：提交后按人合并计算，避免整日重算和 MVCC。
 */
@Slf4j
@Component
public class AttendanceCalcTrigger {

    private static final int CALC_RETRY = 5;

    private static final ConcurrentHashMap<String, Object> DATE_LOCKS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Object> TENANT_FLUSH_LOCKS = new ConcurrentHashMap<>();

    @Autowired
    private TcmAttendanceCalculationService calculationService;

    @Autowired
    private AttendanceCorrectionSyncService correctionSyncService;

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 按员工编号计算（优先）；staffId 会解析为 code。
     */
    public void triggerByDateAndStaff(String date, String staffId) {
        if (StringUtils.isBlank(date)) {
            return;
        }
        String employeeCode = resolveEmployeeCode(staffId);
        if (StringUtils.isBlank(employeeCode)) {
            log.warn("无法解析员工编号，跳过计算 date={}, staffId={}", date, staffId);
            return;
        }
        calcDateWithLock(date, employeeCode);
    }

    private void calcDateWithLock(String date, String employeeCode) {
        if (StringUtils.isBlank(date) || StringUtils.isBlank(employeeCode)) {
            return;
        }
        String normalized = DateUtil.extractDateString(date);
        if (normalized == null) {
            normalized = date.trim();
        }
        String lockKey = InvocationInfoProxy.getTenantid() + "|" + normalized + "|" + employeeCode;
        Object lock = DATE_LOCKS.computeIfAbsent(lockKey, k -> new Object());
        synchronized (lock) {
            for (int i = 0; i < CALC_RETRY; i++) {
                try {
                    calculationService.getAndUpdateAssessReviseByDate(parseDate(normalized), employeeCode);
                    return;
                } catch (RuntimeException e) {
                    if (!isConcurrencyConflict(e) || i == CALC_RETRY - 1) {
                        log.error("考勤计算触发失败, date={}, code={}", normalized, employeeCode, e);
                        throw e;
                    }
                    log.warn("考勤计算并发冲突，重试 {}/{} date={}", i + 1, CALC_RETRY, normalized);
                    sleepQuietly(100L * (1L << i));
                }
            }
        }
    }

    private boolean isConcurrencyConflict(Throwable e) {
        while (e != null) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("mvcc") || msg.contains("MVCC")
                    || msg.contains("乐观锁") || msg.contains("pubts"))) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    private void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private Date parseDate(String date) {
        String normalized = DateUtil.extractDateString(date);
        if (normalized == null) {
            normalized = date;
        }
        return DateUtil.stringToDate(DateUtil.YYYY_MM_DD, normalized);
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
            if (StringUtils.isNotBlank(staffId)) {
                AttendanceCalcImportContext.collectJlRefresh(date, staffId);
            }
            registerImportFlushAfterCommit();
            return;
        }
        if (StringUtils.isBlank(staffId)) {
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
        registerFlushAfterCommit();
    }

    /**
     * 删除/导入：提交后在当前线程收尾（租户上下文仍在），按人合并计算。
     */
    public void registerFlushAfterCommit() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            flushImportPending();
            return;
        }
        if (!AttendanceCalcImportContext.markSyncRegistered()) {
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
        String tenant = StringUtils.defaultIfBlank(InvocationInfoProxy.getTenantid(), "_");
        Object lock = TENANT_FLUSH_LOCKS.computeIfAbsent(tenant, k -> new Object());
        synchronized (lock) {
            try {
                FlushResult result = drainAndProcess();
                if (!result.ok()) {
                    AttendanceCalcImportContext.requeueJlRefresh(result.failedJlKeys);
                    log.error("导入/删除后考勤收尾未全部成功，失败人天可到【考勤计算】补齐. empDays={}",
                            result.failedJlKeys.size());
                }
            } catch (Exception e) {
                log.error("导入/删除后考勤收尾失败 tenant={}", tenant, e);
            } finally {
                AttendanceCalcImportContext.clear();
            }
        }
    }

    private FlushResult drainAndProcess() {
        FlushResult result = new FlushResult();
        while (true) {
            List<TcmSheduleShiftsData> schedules = AttendanceCalcImportContext.snapshotSchedules();
            List<TcmDriverHolidayWorkRecord> holidays = AttendanceCalcImportContext.snapshotHolidays();
            Set<String> empDayKeys = keysFromSchedules(schedules);
            empDayKeys.addAll(keysFromHolidays(holidays));
            correctionSyncService.batchSyncFromSchedules(schedules);
            correctionSyncService.batchSyncFromHolidays(holidays);
            Set<String> jlKeys = AttendanceCalcImportContext.snapshotJlRefresh();
            jlKeys.addAll(empDayKeys);
            AttendanceCalcImportContext.drainDates();
            if (jlKeys.isEmpty() && AttendanceCalcImportContext.isQueueEmpty()) {
                return result;
            }
            refreshAndCalcByEmployee(jlKeys, result);
            if (AttendanceCalcImportContext.isQueueEmpty()) {
                return result;
            }
        }
    }

    private Set<String> keysFromSchedules(List<TcmSheduleShiftsData> schedules) {
        Set<String> keys = new HashSet<>();
        if (schedules == null) {
            return keys;
        }
        for (TcmSheduleShiftsData s : schedules) {
            if (s == null || StringUtils.isBlank(s.getDate()) || StringUtils.isBlank(s.getEmployee_id())) {
                continue;
            }
            String normalized = DateUtil.extractDateString(s.getDate());
            String date = normalized != null ? normalized : s.getDate().trim();
            keys.add(date + "\t" + s.getEmployee_id().trim());
        }
        return keys;
    }

    private Set<String> keysFromHolidays(List<TcmDriverHolidayWorkRecord> holidays) {
        Set<String> keys = new HashSet<>();
        if (holidays == null) {
            return keys;
        }
        for (TcmDriverHolidayWorkRecord h : holidays) {
            if (h == null || StringUtils.isBlank(h.getDate()) || StringUtils.isBlank(h.getEmployee_id())) {
                continue;
            }
            String normalized = DateUtil.extractDateString(h.getDate());
            String date = normalized != null ? normalized : h.getDate().trim();
            keys.add(date + "\t" + h.getEmployee_id().trim());
        }
        return keys;
    }

    /**
     * 必须同时有日期和员工才重算计粮和考勤。
     */
    private void refreshAndCalcByEmployee(Set<String> jlKeys, FlushResult result) {
        if (jlKeys == null || jlKeys.isEmpty()) {
            return;
        }
        Map<String, Set<String>> staffByDate = new HashMap<>();
        Set<String> staffIds = new HashSet<>();
        for (String key : jlKeys) {
            int tab = key.indexOf('\t');
            if (tab <= 0 || tab >= key.length() - 1) {
                continue;
            }
            String date = key.substring(0, tab);
            String staffId = key.substring(tab + 1);
            if (StringUtils.isBlank(date) || StringUtils.isBlank(staffId)) {
                continue;
            }
            staffByDate.computeIfAbsent(date, k -> new HashSet<>()).add(staffId);
            staffIds.add(staffId);
        }
        if (staffIds.isEmpty()) {
            return;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        Map<String, String> codeById = commonUtil.getStaffCodesByIds(staffIds, tenantId);
        for (Map.Entry<String, Set<String>> e : staffByDate.entrySet()) {
            String date = e.getKey();
            for (String staffId : e.getValue()) {
                String key = date + "\t" + staffId;
                try {
                    String code = codeById.get(staffId);
                    if (StringUtils.isBlank(code)) {
                        log.warn("无法解析员工编号，跳过计算 date={} staffId={}", date, staffId);
                        continue;
                    }
                    calcDateWithLock(date, code);
                } catch (Exception ex) {
                    log.error("按人考勤计算失败, date={} staffId={}", date, staffId, ex);
                    result.failEmpDay(key);
                }
            }
        }
    }

    private static final class FlushResult {
        private final Set<String> failedJlKeys = new HashSet<>();

        boolean ok() {
            return failedJlKeys.isEmpty();
        }

        void failEmpDay(String key) {
            failedJlKeys.add(key);
        }
    }
}
