package com.yonyou.ucf.mdf.sample.controller;

import com.yonyou.ucf.mdf.sample.service.TcmHiSenseWaybillLoginRecordService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loginRecord/hiSense")
public class TcmHiSenseWaybillLoginRecordController {

    @Autowired
    private TcmHiSenseWaybillLoginRecordService service;


    @GetMapping("/sync")
    public ResponseEntity<?> sync(@RequestParam(defaultValue = "",value = "runDate") String runDate) {

        service.syncByActDate(runDate);
        return ResponseUtil.success("成功同步海信數據");
    }


    @GetMapping("/count")
    public ResponseEntity<?> count(@RequestParam String runDate) {

        Integer count = service.countByActDate(runDate);
        return ResponseUtil.success(runDate +" 存在數據數：" + count,count);
    }
}
