package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverHolidayWorkRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSheduleShiftsData;

import java.util.List;
import java.util.Set;

/**
 * 源数据变更后同步考勤修正（计算前置）。
 */
public interface AttendanceCorrectionSyncService {

    /**
     * 排更 → 修正：只回写排更侧发收车/用膳等。计粮叠加在计算前 {@link #refreshCorrectionsBeforeCalc} 做一次。
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
     * 导入收尾：按人天批量回写入油加气、异常来源到修正（不触发考勤计算）。
     */
    void batchSyncFuelAndException(Set<String> empDayKeys);

    /**
     * 考勤计算前：按计算范围回写加油加气、异常来源，并按更叠加重算计粮（排更打底 → 后备 → 超时）。
     * 一段后备/超时对照排更（缺二更时用已叠上的计粮）判断写一更还是二更；
     * 来源有两段而排更只有一更时，按早晚拆出计粮二更。
     * 仅 UPDATE 内容有变化的行。
     */
    void refreshCorrectionsBeforeCalc(String dateStr, String employeeCode, boolean singleDate);
}
