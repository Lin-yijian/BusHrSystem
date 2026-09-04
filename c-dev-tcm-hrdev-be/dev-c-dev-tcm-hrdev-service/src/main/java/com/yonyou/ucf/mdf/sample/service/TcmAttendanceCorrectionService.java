package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.*;

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
     * 查询修正数据
     * @param date 日期 yyyy-MM-dd
     * @param waybillEmployeeId 海信工号
     * @return .
     */
    List<TcmAttendanceCorrection> getByDateAndWaybillEmployeeId(String date, String waybillEmployeeId);


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
    Map<String,String> getByDateListAndStaffIds(Collection<String> dateList, Collection<String> staffIdList);

    /**
     * 根据ID修改迟到早退状态
     * @param ids
     * @param status
     * @param type LATE、LEAVE、ALL
     */
    void updateLateStatus(List<String> ids, String status, String type);

    /**
     * 根据ID修改工号
     * @param id 数据ID
     * @param employeeId 工号ID
     */
    void updateEmployeeById(String id, String employeeId);

    /**
     * 迟到早退清零
     * @param ids
     */
    void clearTimeByIds(List<String> ids);


    /**
     * 是否周假上班
     * @param ids 修正数据ID
     * @param isWorkingHoliday 1 是   2 否
     */
    void isWorkingHoliday(List<String> ids,String isWorkingHoliday);

    void saveList(List<TcmAttendanceCorrection> list);

    void saveTcmAttendanceAbsenceDataRecord(List<TcmAttendanceAbsenceDataRecord> list);

    void saveTcmAttendanceControlCenterLog(List<TcmAttendanceControlCenterLog> list);

    void saveTcmAttendanceDriverOvertimeRecord(List<TcmAttendanceDriverOvertimeRecord> list);

    void delTcmAttendanceAbsenceDataRecord(String... entityId);

    void delTcmAttendanceControlCenterLog(String... entityId);

    void delTcmAttendanceDriverOvertimeRecord(String... entityId);
}
