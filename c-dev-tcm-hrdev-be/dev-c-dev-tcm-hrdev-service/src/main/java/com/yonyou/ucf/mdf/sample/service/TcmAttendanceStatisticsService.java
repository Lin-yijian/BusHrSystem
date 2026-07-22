package com.yonyou.ucf.mdf.sample.service;

import java.util.Date;
import java.util.Map;

/**
 * 考勤统计服务
 * @author aze
 */
public interface TcmAttendanceStatisticsService {

    /**
     * 根据月份获取考勤统计信息
     * 查询当月修正数据，对没有统计记录的人员生成新的统计记录
     *
     * @param date 月份中的任意日期
     * @return 包含correctionData、addList、insertResult的结果Map
     */
    Map<String, Object> getTcmAttendanceStatisticsInfoByMonth(Date date);

}
