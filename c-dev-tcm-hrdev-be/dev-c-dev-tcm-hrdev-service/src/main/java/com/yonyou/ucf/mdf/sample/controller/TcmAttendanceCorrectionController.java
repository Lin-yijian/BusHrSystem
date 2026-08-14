package com.yonyou.ucf.mdf.sample.controller;

import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance/correction")
public class TcmAttendanceCorrectionController {

    @Autowired
    private TcmAttendanceCorrectionService service;

    @PostMapping("/error/status")
    public ResponseEntity<?> errorMark(
            @RequestParam("type") String type,
            @RequestParam(value = "ids") List<String> ids) {

        service.updateLateStatus(ids, BaseEnum.AC_CHECK_STATUS_1.getCode(),type);
        return ResponseUtil.success("成功获取海信数据");
    }

    @PostMapping("/day/status")
    public ResponseEntity<?> dayMark(
            @RequestParam(value = "ids") List<String> ids) {

        service.updateLateStatus(ids, BaseEnum.AC_CHECK_STATUS_2.getCode(),"ALL");
        return ResponseUtil.success("成功获取海信数据");
    }


}
