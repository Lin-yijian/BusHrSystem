package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;

import java.util.Collection;
import java.util.List;

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
}
