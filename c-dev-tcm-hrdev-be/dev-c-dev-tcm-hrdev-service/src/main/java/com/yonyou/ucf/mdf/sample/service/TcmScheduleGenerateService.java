package com.yonyou.ucf.mdf.sample.service;

import java.util.Map;

public interface TcmScheduleGenerateService {

    /**
     * 生成本地排更数据
     * @param date 目标日期
     * @return .
     * @throws Exception .
     */
    Map<String, Object> generateSchedule(String date) throws Exception;


    /**
     * 根据日期查询数据是否存在
     * @param date 日期 yyyy-MM-dd
     * @return true 存在
     * @throws Exception .
     */
    boolean checkDataByDate(String date) throws Exception;

    /**
     * 生成休假記錄
     * @param ruleId
     * @param date
     */
    void generateLeaveR(String ruleId,String date);
}
