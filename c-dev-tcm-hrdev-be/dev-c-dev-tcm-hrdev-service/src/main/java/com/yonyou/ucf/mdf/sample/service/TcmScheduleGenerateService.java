package com.yonyou.ucf.mdf.sample.service;

import java.util.Map;

public interface TcmScheduleGenerateService {

    Map<String, Object> generateSchedule(String date) throws Exception;
}
