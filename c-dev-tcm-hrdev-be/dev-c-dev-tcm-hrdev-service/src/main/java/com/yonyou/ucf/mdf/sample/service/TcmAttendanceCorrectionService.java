package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 考勤修正
 */
public interface TcmAttendanceCorrectionService {

    /**
     * 查詢考勤修正數據
     * @param date 日期 yyyy-MM-dd
     * @param staffId 員工ID
     * @return .
     */
    List<TcmAttendanceCorrection> getByDateAndStaffId(String date, String staffId);


    /**
     * 查詢考勤修正數據
     * @param date 日期 yyyy-MM-dd
     * @param staffIdList 員工ID集合
     * @return
     */
    List<TcmAttendanceCorrection> getByDateListAndStaffIds(String date, Collection<String> staffIdList);

    /**
     *
     * @param dateList 日期 yyyy-MM-dd
     * @param staffIdList 員工ID集合
     * @return
     */
    Map<String,String> getByDateListAndStaffIds(List<String> dateList, Collection<String> staffIdList);

    /**
     * 根据ID修改迟到早退状态
     * @param ids
     * @param status
     * @param type LATE、LEAVE、ALL
     */
    void updateLateStatus(List<String> ids, String status, String type);
}
