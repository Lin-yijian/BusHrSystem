package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.ucf.mdf.sample.service.TcmHiSenseAttendanceService;
import io.edap.util.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TcmHiSenseAttendanceServiceImpl implements TcmHiSenseAttendanceService {

    public static final String HI_SENSE_ATTENDANCE_URL = "http://10.102.128.7:8080/sdata/rest/service/dataapi/rest/6903218280134273";

    @Override
    public void syncData(String runData) {

        if (StringUtil.isEmpty(runData)){
            throw new RuntimeException("业务日期不能为空");
        }

        RestTemplate restTemplate = new RestTemplate();
        String body = "{\"rundate\":\"" + runData + "\"}";
        String result = restTemplate.postForObject(HI_SENSE_ATTENDANCE_URL, body, String.class);


    }



}
