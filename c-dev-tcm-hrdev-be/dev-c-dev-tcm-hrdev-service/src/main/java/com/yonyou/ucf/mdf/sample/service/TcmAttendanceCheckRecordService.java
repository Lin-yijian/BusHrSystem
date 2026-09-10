package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;

import java.util.Set;

public interface TcmAttendanceCheckRecordService {

    /**
     * 更新核对记录状态
     * @param checkDateList 日期 yyyy-MM-dd
     * @param moduleEnum 涉及模块
     * @param isGood true 打勾  false 打叉
     */
    void updateOrSaveRecords(Set<String> checkDateList,ModuleEnum moduleEnum, boolean isGood);


    /**
     * 更新核对记录状态
     * @param checkDate 日期 yyyy-MM-dd
     * @param moduleEnum 涉及模块
     * @param isGood true 打勾  false 打叉
     */
    void updateOrSaveRecord(String checkDate, ModuleEnum moduleEnum, boolean isGood);

    /**
     * 修改状态
     * @param checkDate 日期
     * @param status 。
     */
    void updateStatus(String checkDate, String status);

    /**
     * 校驗指定日期的排更數據是否已導入。
     * <p>查詢該日期的考勤核對記錄，若排更數據標記 schedule_data 不為 true，
     * 則拋出異常：「当前日期的排更数据未导入，请先导入排更数据。」，
     * 用於保存其他原始數據（缺勤、巴士故障、後備車長、加班、控制中心日志、加油）或同步海信數據前的前置校驗。
     *
     * @param date 日期 yyyy-MM-dd
     */
    void validateScheduleDataImported(String date);

    /**
     * 批量查詢指定日期集合中「排更數據已導入」（schedule_data = true）的日期集合。
     * <p>導入場景使用：一次查詢所有日期，避免逐行查庫；調用方對不在返回集合中的日期標記導入錯誤。
     *
     * @param dateSet 日期集合 yyyy-MM-dd
     * @return 已導入排更數據的日期集合（schedule_data = true）
     */
    Set<String> getImportedDateSet(Set<String> dateSet);

    /**
     * 校驗指定日期是否已有「排更數據(schedule_data)」以外的其他原始數據導入。
     * <p>查詢該日期的考勤核對記錄，若除 schedule_data 外的任一布爾標記字段為 true
     * （缺勤記錄、车载机登陆、海信考勤、巴士故障、後備車長、車長加班、控制中心日志、加油次數），
     * 則拋出異常：「已有其他原始数据导入，不允许删除排更数据。」，用於刪除排更數據前的前置校驗。
     *
     * @param date 日期 yyyy-MM-dd
     */
    void validateNoOtherRawDataImported(String date);
}
