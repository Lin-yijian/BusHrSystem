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
}
