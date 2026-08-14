package com.yonyou.ucf.mdf.sample.controller;

import com.yonyou.ucf.mdf.sample.bill.entity.dto.TcmAttendanceCorrectionBo;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/attendance/correction")
public class TcmAttendanceCorrectionController {

    @Autowired
    private TcmAttendanceCorrectionService service;

    @Autowired
    private TcmAttendanceCheckRecordService checkRecordService;

    @PutMapping("/error/status")
    public ResponseEntity<?> errorMark(@RequestBody TcmAttendanceCorrectionBo bo) {

        service.updateLateStatus(bo.getIds(), BaseEnum.AC_CHECK_STATUS_1.getCode(), bo.getType());
        return ResponseUtil.success("標記成功");
    }

    @PutMapping("/day/status")
    public ResponseEntity<?> dayMark(@RequestBody TcmAttendanceCorrectionBo bo) {

        service.updateLateStatus(bo.getIds(), BaseEnum.AC_CHECK_STATUS_2.getCode(),"ALL");
        return ResponseUtil.success("標記成功");
    }


    @PutMapping("/employee")
    public ResponseEntity<?> updateEmployeeById(@RequestBody TcmAttendanceCorrectionBo bo) {

        service.updateEmployeeById(bo.getId(), bo.getEmployeeId());
        return ResponseUtil.success("工號修改成功");
    }


    @PutMapping("/clearTime")
    public ResponseEntity<?> clearTimeByIds(@RequestBody TcmAttendanceCorrectionBo bo) {

        service.clearTimeByIds(bo.getIds());
        return ResponseUtil.success("已成功清零");
    }


    @PutMapping("/isWorkingHoliday")
    public ResponseEntity<?> isWorkingHoliday(@RequestBody TcmAttendanceCorrectionBo bo) {

        service.isWorkingHoliday(bo.getIds(),true);
        return ResponseUtil.success("標記成功");
    }

    @PutMapping("/dayEnd")
    public ResponseEntity<?> dayEnd(@RequestBody TcmAttendanceCorrectionBo bo) {

        checkRecordService.updateStatus(bo.getDate(),BaseEnum.CHECK_STATUS_1.getCode());
        return ResponseUtil.success("日结成功");
    }

    @PutMapping("/dayEnd/cancel")
    public ResponseEntity<?> dayEndCancel(@RequestBody TcmAttendanceCorrectionBo bo) {

        checkRecordService.updateStatus(bo.getDate(),BaseEnum.CHECK_STATUS_0.getCode());
        return ResponseUtil.success("日结取消成功");

    }


}
