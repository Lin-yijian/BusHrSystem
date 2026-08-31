package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverHolidayWorkRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmReserveCaptainAttendanceRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSheduleShiftsData;

import java.util.List;

/**
 * 源数据变更后同步考勤修正（计算前置）。
 */
public interface AttendanceCorrectionSyncService {

    /**
     * 排更 → 修正：更新排更侧发收车/用膳等，不覆盖计粮上下更(jl_*)。
     */
    void syncFromSchedule(TcmSheduleShiftsData schedule);

    /**
     * 导入收尾：批量同步排更修正。
     */
    void batchSyncFromSchedules(List<TcmSheduleShiftsData> schedules);

    /**
     * 排更删除后：若仍有同日同人排更则按剩余重装；否则仅保留非排更字段（不删修正行）。
     */
    void afterScheduleDeleted(TcmSheduleShiftsData deleted);

    /**
     * 后备车长 → 修正：发车/收车拷贝到上下更(jl_*)。
     */
    void syncFromReserve(TcmReserveCaptainAttendanceRecord reserve);

    /**
     * 导入收尾：批量同步后备修正。
     */
    void batchSyncFromReserves(List<TcmReserveCaptainAttendanceRecord> reserves);

    /**
     * 周末假期工作 → 修正：同步是否周假上班标记。
     */
    void syncFromHolidayWork(TcmDriverHolidayWorkRecord holiday);

    /**
     * 导入收尾：批量同步假期工作修正。
     */
    void batchSyncFromHolidays(List<TcmDriverHolidayWorkRecord> holidays);

    /**
     * 入油加气 → 修正：汇总当日该员工加油/加气次数，写到所有匹配修正行。
     */
    void syncFromFuelGas(String date, String employeeId);

    /**
     * 巴士故障 / 控制中心日志 / 缺勤 → 修正展示字段（异常核对用）。
     */
    void syncExceptionSources(String date, String employeeId);

    /**
     * 考勤计算前：按计算范围回写加油加气、异常来源，并重算超时时长。
     */
    void refreshCorrectionsBeforeCalc(String dateStr, String employeeCode, boolean singleDate);
}
