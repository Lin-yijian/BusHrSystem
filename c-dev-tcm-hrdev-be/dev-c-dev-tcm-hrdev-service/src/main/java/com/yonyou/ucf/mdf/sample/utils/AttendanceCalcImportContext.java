package com.yonyou.ucf.mdf.sample.utils;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverHolidayWorkRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSheduleShiftsData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 平台导入/删除收尾：ThreadLocal 给同事务批次用；跨线程导入/批量删除用租户级并发集合。
 */
@Slf4j
public final class AttendanceCalcImportContext {

    private static final ThreadLocal<Boolean> IMPORTING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Set<String>> DATES = ThreadLocal.withInitial(HashSet::new);
    private static final ThreadLocal<List<TcmSheduleShiftsData>> PENDING_SCHEDULES =
            ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<TcmDriverHolidayWorkRecord>> PENDING_HOLIDAYS =
            ThreadLocal.withInitial(ArrayList::new);
    /** 删除后待重算计粮的 key：日期 + TAB + 员工ID */
    private static final ThreadLocal<Set<String>> PENDING_JL_REFRESH =
            ThreadLocal.withInitial(HashSet::new);
    /** 导入后待回写入油加气/异常来源的 key：日期 + TAB + 员工ID */
    private static final ThreadLocal<Set<String>> PENDING_SOURCE_SYNC =
            ThreadLocal.withInitial(HashSet::new);
    private static final ThreadLocal<Boolean> SYNC_REGISTERED = ThreadLocal.withInitial(() -> false);
    /** 海信拉取等批量写入：afterSave 不再按条触发修正/计算。 */
    private static final ThreadLocal<Boolean> BULK_SYNC = ThreadLocal.withInitial(() -> false);

    /** 导入线程池/批量删除跨线程收集 */
    private static final ConcurrentHashMap<String, Set<String>> GLOBAL_JL_REFRESH = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Set<String>> GLOBAL_SOURCE_SYNC = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Set<String>> GLOBAL_DATES = new ConcurrentHashMap<>();
    /** 导入/批量删除：租户级合并收尾标记，工作线程也能看到 */
    private static final ConcurrentHashMap<String, Boolean> GLOBAL_IMPORTING = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> GLOBAL_MERGE = new ConcurrentHashMap<>();
    /** 最近一次导入/合并活动时间，超时后自动摘掉标记，避免整批失败后租户一直停在导入态 */
    private static final ConcurrentHashMap<String, Long> GLOBAL_MERGE_AT = new ConcurrentHashMap<>();
    private static final long MERGE_FLAG_TTL_MS = 5 * 60 * 1000L;

    private AttendanceCalcImportContext() {
    }

    private static String tenantKey() {
        String tenantId = InvocationInfoProxy.getTenantid();
        return StringUtils.isBlank(tenantId) ? "_" : tenantId;
    }

    private static String normalizeDate(String date) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        String normalized = DateUtil.extractDateString(date);
        return normalized != null ? normalized : date.trim();
    }

    public static void beginBulkSync() {
        BULK_SYNC.set(true);
    }

    public static boolean isBulkSyncing() {
        return Boolean.TRUE.equals(BULK_SYNC.get());
    }

    public static void endBulkSync() {
        BULK_SYNC.remove();
    }

    public static void beginImport() {
        IMPORTING.set(true);
        DATES.get().clear();
        PENDING_SCHEDULES.get().clear();
        PENDING_HOLIDAYS.get().clear();
        PENDING_JL_REFRESH.get().clear();
        PENDING_SOURCE_SYNC.get().clear();
        SYNC_REGISTERED.set(false);
        GLOBAL_IMPORTING.put(tenantKey(), Boolean.TRUE);
        GLOBAL_MERGE.put(tenantKey(), Boolean.TRUE);
        touchMergeFlag();
    }

    /** 批量删除入口：后续提交只收集人天，合并后再算。 */
    public static void beginMergeFlush() {
        GLOBAL_MERGE.put(tenantKey(), Boolean.TRUE);
        touchMergeFlag();
    }

    public static void endMergeFlush() {
        String tk = tenantKey();
        GLOBAL_MERGE.remove(tk);
        GLOBAL_IMPORTING.remove(tk);
        GLOBAL_MERGE_AT.remove(tk);
    }

    public static boolean isImporting() {
        expireStaleFlagsIfNeeded();
        return Boolean.TRUE.equals(IMPORTING.get())
                || Boolean.TRUE.equals(GLOBAL_IMPORTING.get(tenantKey()));
    }

    /** 导入或批量删除已显式打开合并收尾。 */
    public static boolean isMergeFlush() {
        expireStaleFlagsIfNeeded();
        return Boolean.TRUE.equals(IMPORTING.get())
                || Boolean.TRUE.equals(GLOBAL_IMPORTING.get(tenantKey()))
                || Boolean.TRUE.equals(GLOBAL_MERGE.get(tenantKey()));
    }

    private static void touchMergeFlag() {
        String tk = tenantKey();
        if (GLOBAL_IMPORTING.containsKey(tk) || GLOBAL_MERGE.containsKey(tk)) {
            GLOBAL_MERGE_AT.put(tk, System.currentTimeMillis());
        }
    }

    private static void expireStaleFlagsIfNeeded() {
        String tk = tenantKey();
        Long at = GLOBAL_MERGE_AT.get(tk);
        if (at == null) {
            return;
        }
        if (System.currentTimeMillis() - at <= MERGE_FLAG_TTL_MS) {
            return;
        }
        log.warn("导入/合并标记超时，自动清理 tenant={}", tk);
        endMergeFlush();
    }

    public static void collectDate(String date) {
        String trimmed = normalizeDate(date);
        if (trimmed == null) {
            return;
        }
        DATES.get().add(trimmed);
        GLOBAL_DATES.computeIfAbsent(tenantKey(), k -> ConcurrentHashMap.newKeySet()).add(trimmed);
        touchMergeFlag();
    }

    public static void collectSchedule(TcmSheduleShiftsData schedule) {
        if (schedule != null) {
            PENDING_SCHEDULES.get().add(schedule);
            collectDate(schedule.getDate());
        }
    }

    public static void collectHoliday(TcmDriverHolidayWorkRecord holiday) {
        if (holiday != null) {
            PENDING_HOLIDAYS.get().add(holiday);
            collectDate(holiday.getDate());
        }
    }

    /**
     * 删除/导入后：提交后再按人重算计粮。
     * @param shareGlobally true 写入跨线程队列，供导入/批量删除合并收尾
     */
    public static void collectJlRefresh(String date, String employeeId) {
        collectJlRefresh(date, employeeId, true);
    }

    public static void collectJlRefresh(String date, String employeeId, boolean shareGlobally) {
        String trimmedDate = normalizeDate(date);
        if (trimmedDate != null) {
            DATES.get().add(trimmedDate);
            if (shareGlobally) {
                GLOBAL_DATES.computeIfAbsent(tenantKey(), k -> ConcurrentHashMap.newKeySet()).add(trimmedDate);
            }
        }
        if (trimmedDate == null || StringUtils.isBlank(employeeId)) {
            return;
        }
        String key = trimmedDate + "\t" + employeeId.trim();
        PENDING_JL_REFRESH.get().add(key);
        if (shareGlobally) {
            GLOBAL_JL_REFRESH.computeIfAbsent(tenantKey(), k -> ConcurrentHashMap.newKeySet()).add(key);
        }
        touchMergeFlag();
    }

    /**
     * 导入后：提交后再按人天回写入油加气/异常来源，避免并行 afterSave 更新同一修正行触发乐观锁。
     */
    public static void collectSourceSync(String date, String employeeId) {
        collectSourceSync(date, employeeId, true);
    }

    public static void collectSourceSync(String date, String employeeId, boolean shareGlobally) {
        String trimmedDate = normalizeDate(date);
        if (trimmedDate != null) {
            DATES.get().add(trimmedDate);
            if (shareGlobally) {
                GLOBAL_DATES.computeIfAbsent(tenantKey(), k -> ConcurrentHashMap.newKeySet()).add(trimmedDate);
            }
        }
        if (trimmedDate == null || StringUtils.isBlank(employeeId)) {
            return;
        }
        String key = trimmedDate + "\t" + employeeId.trim();
        PENDING_SOURCE_SYNC.get().add(key);
        if (shareGlobally) {
            GLOBAL_SOURCE_SYNC.computeIfAbsent(tenantKey(), k -> ConcurrentHashMap.newKeySet()).add(key);
        }
        touchMergeFlag();
    }

    /** 收尾失败时把待回写人天放回队列。 */
    public static void requeueSourceSync(Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            if (StringUtils.isBlank(key)) {
                continue;
            }
            int tab = key.indexOf('\t');
            if (tab <= 0 || tab >= key.length() - 1) {
                collectDate(key);
                continue;
            }
            collectSourceSync(key.substring(0, tab), key.substring(tab + 1));
        }
    }

    /** 无 Spring 事务时：把本线程队列并入租户队列，再延迟收尾。 */
    public static void promoteLocalToGlobal() {
        String tk = tenantKey();
        copyToGlobal(PENDING_JL_REFRESH.get(), GLOBAL_JL_REFRESH, tk);
        copyToGlobal(PENDING_SOURCE_SYNC.get(), GLOBAL_SOURCE_SYNC, tk);
        copyToGlobal(DATES.get(), GLOBAL_DATES, tk);
        touchMergeFlag();
    }

    private static void copyToGlobal(Set<String> local, ConcurrentHashMap<String, Set<String>> global, String tk) {
        if (local == null || local.isEmpty()) {
            return;
        }
        global.computeIfAbsent(tk, k -> ConcurrentHashMap.newKeySet()).addAll(local);
    }

    /** 收尾失败时把待算人天放回队列，供延迟重试。 */
    public static void requeueJlRefresh(Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            if (StringUtils.isBlank(key)) {
                continue;
            }
            int tab = key.indexOf('\t');
            if (tab <= 0 || tab >= key.length() - 1) {
                collectDate(key);
                continue;
            }
            collectJlRefresh(key.substring(0, tab), key.substring(tab + 1));
        }
    }

    /** 丢掉仅日期队列，避免无员工时误判还有待处理。 */
    public static void drainDates() {
        drainLocalDates();
        GLOBAL_DATES.remove(tenantKey());
    }

    public static void drainLocalDates() {
        DATES.get().clear();
    }

    public static List<TcmSheduleShiftsData> snapshotSchedules() {
        List<TcmSheduleShiftsData> list = new ArrayList<>(PENDING_SCHEDULES.get());
        PENDING_SCHEDULES.get().clear();
        return list;
    }

    public static List<TcmDriverHolidayWorkRecord> snapshotHolidays() {
        List<TcmDriverHolidayWorkRecord> list = new ArrayList<>(PENDING_HOLIDAYS.get());
        PENDING_HOLIDAYS.get().clear();
        return list;
    }

    /** 只取当前线程待算人天，不碰跨线程队列。 */
    public static Set<String> snapshotJlRefresh() {
        Set<String> keys = new HashSet<>(PENDING_JL_REFRESH.get());
        PENDING_JL_REFRESH.get().clear();
        return keys;
    }

    /** 取出并清空本租户跨线程待算人天。 */
    public static Set<String> snapshotGlobalJlRefresh() {
        Set<String> global = GLOBAL_JL_REFRESH.remove(tenantKey());
        if (global == null || global.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(global);
    }

    public static Set<String> snapshotSourceSync() {
        Set<String> keys = new HashSet<>(PENDING_SOURCE_SYNC.get());
        PENDING_SOURCE_SYNC.get().clear();
        return keys;
    }

    public static Set<String> snapshotGlobalSourceSync() {
        Set<String> global = GLOBAL_SOURCE_SYNC.remove(tenantKey());
        if (global == null || global.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(global);
    }

    public static boolean isLocalQueueEmpty() {
        return DATES.get().isEmpty() && PENDING_JL_REFRESH.get().isEmpty()
                && PENDING_SOURCE_SYNC.get().isEmpty()
                && PENDING_SCHEDULES.get().isEmpty() && PENDING_HOLIDAYS.get().isEmpty();
    }

    public static boolean isQueueEmpty() {
        if (!isLocalQueueEmpty()) {
            return false;
        }
        Set<String> dates = GLOBAL_DATES.get(tenantKey());
        if (dates != null && !dates.isEmpty()) {
            return false;
        }
        Set<String> jl = GLOBAL_JL_REFRESH.get(tenantKey());
        if (jl != null && !jl.isEmpty()) {
            return false;
        }
        Set<String> source = GLOBAL_SOURCE_SYNC.get(tenantKey());
        return source == null || source.isEmpty();
    }

    /**
     * @return true 表示本线程首次注册，可挂 afterCommit
     */
    public static boolean markSyncRegistered() {
        if (Boolean.TRUE.equals(SYNC_REGISTERED.get())) {
            return false;
        }
        SYNC_REGISTERED.set(true);
        return true;
    }

    public static void clear() {
        IMPORTING.remove();
        DATES.remove();
        PENDING_SCHEDULES.remove();
        PENDING_HOLIDAYS.remove();
        PENDING_JL_REFRESH.remove();
        PENDING_SOURCE_SYNC.remove();
        SYNC_REGISTERED.remove();
        BULK_SYNC.remove();
    }
}
