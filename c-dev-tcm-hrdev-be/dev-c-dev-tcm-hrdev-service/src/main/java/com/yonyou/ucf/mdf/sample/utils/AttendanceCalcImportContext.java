package com.yonyou.ucf.mdf.sample.utils;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverHolidayWorkRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverOvertimeRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmReserveCaptainAttendanceRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSheduleShiftsData;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 平台导入期间标记：afterSave 只收集，afterCommit 批量同步修正并按日计算。
 */
public final class AttendanceCalcImportContext {

    private static final ThreadLocal<Boolean> IMPORTING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Set<String>> DATES = ThreadLocal.withInitial(HashSet::new);
    private static final ThreadLocal<List<TcmSheduleShiftsData>> PENDING_SCHEDULES =
            ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<TcmReserveCaptainAttendanceRecord>> PENDING_RESERVES =
            ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<TcmDriverOvertimeRecord>> PENDING_OVERTIMES =
            ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<TcmDriverHolidayWorkRecord>> PENDING_HOLIDAYS =
            ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<Boolean> SYNC_REGISTERED = ThreadLocal.withInitial(() -> false);
    /** 海信拉取等批量写入：afterSave 不再按条触发修正/计算。 */
    private static final ThreadLocal<Boolean> BULK_SYNC = ThreadLocal.withInitial(() -> false);

    private AttendanceCalcImportContext() {
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
        PENDING_RESERVES.get().clear();
        PENDING_OVERTIMES.get().clear();
        PENDING_HOLIDAYS.get().clear();
        SYNC_REGISTERED.set(false);
    }

    public static boolean isImporting() {
        return Boolean.TRUE.equals(IMPORTING.get());
    }

    public static void collectDate(String date) {
        if (StringUtils.isNotBlank(date)) {
            DATES.get().add(date.trim());
        }
    }

    public static void collectSchedule(TcmSheduleShiftsData schedule) {
        if (schedule != null) {
            PENDING_SCHEDULES.get().add(schedule);
            collectDate(schedule.getDate());
        }
    }

    public static void collectReserve(TcmReserveCaptainAttendanceRecord reserve) {
        if (reserve != null) {
            PENDING_RESERVES.get().add(reserve);
            collectDate(reserve.getDate());
        }
    }

    public static void collectOvertime(TcmDriverOvertimeRecord overtime) {
        if (overtime != null) {
            PENDING_OVERTIMES.get().add(overtime);
            collectDate(overtime.getOvertime_date());
        }
    }

    public static void collectHoliday(TcmDriverHolidayWorkRecord holiday) {
        if (holiday != null) {
            PENDING_HOLIDAYS.get().add(holiday);
            collectDate(holiday.getDate());
        }
    }

    public static Set<String> snapshotDates() {
        return new HashSet<>(DATES.get());
    }

    public static List<TcmSheduleShiftsData> snapshotSchedules() {
        return new ArrayList<>(PENDING_SCHEDULES.get());
    }

    public static List<TcmReserveCaptainAttendanceRecord> snapshotReserves() {
        return new ArrayList<>(PENDING_RESERVES.get());
    }

    public static List<TcmDriverOvertimeRecord> snapshotOvertimes() {
        return new ArrayList<>(PENDING_OVERTIMES.get());
    }

    public static List<TcmDriverHolidayWorkRecord> snapshotHolidays() {
        return new ArrayList<>(PENDING_HOLIDAYS.get());
    }

    /**
     * @return true 表示首次注册，可挂 afterCommit
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
        PENDING_RESERVES.remove();
        PENDING_OVERTIMES.remove();
        PENDING_HOLIDAYS.remove();
        SYNC_REGISTERED.remove();
        BULK_SYNC.remove();
    }
}
