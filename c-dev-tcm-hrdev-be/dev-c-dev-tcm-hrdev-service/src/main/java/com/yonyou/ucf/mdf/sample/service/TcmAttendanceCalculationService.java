package com.yonyou.ucf.mdf.sample.service;

import java.util.Date;
import java.util.Map;

/**
 * 考勤计算服务
 * @author aze
 */
public interface TcmAttendanceCalculationService {

    /**
     * 根据日期和员工代码获取并更新考勤计算结果
     * @param date
     * @param employeeCode
     * @return
     */
    Map<String, Object> getAndUpdateAssessReviseByDate(Date date, String employeeCode);


    /**
     * 根据日期和员工代码获取并更新考勤计算结果
     * @param dateStr yyyy-MM-dd
     * @return .
     */
    Map<String, Object> getAndUpdateAssessReviseByDate(String dateStr);

}
