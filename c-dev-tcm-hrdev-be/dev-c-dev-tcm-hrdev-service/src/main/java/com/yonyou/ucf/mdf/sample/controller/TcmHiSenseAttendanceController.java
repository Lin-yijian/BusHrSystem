package com.yonyou.ucf.mdf.sample.controller;


import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseAttendanceMergedDTO;
import com.yonyou.ucf.mdf.sample.service.TcmHiSenseAttendanceService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/attendance/hiSense")
public class TcmHiSenseAttendanceController {

    @Autowired
    private TcmHiSenseAttendanceService service;

    @GetMapping("/sync")
    public ResponseEntity<?> sync(@RequestParam(defaultValue = "",value = "runDate") String runDate) {

        service.syncData(runDate);
        return ResponseUtil.success("成功获取海信数据");
    }


    @GetMapping("/generate")
    public ResponseEntity<?> generate(@RequestParam(defaultValue = "",value = "runDate") String runDate) {

        service.generate(runDate);
        return ResponseUtil.success("成功生成本地考勤");
    }


    @GetMapping("/count")
    public ResponseEntity<?> count(@RequestParam String runDate) {

        Integer count = service.countByRunDate(runDate);
        return ResponseUtil.success(runDate +" 存在数据数：" + count,count);
    }
    
    
}
