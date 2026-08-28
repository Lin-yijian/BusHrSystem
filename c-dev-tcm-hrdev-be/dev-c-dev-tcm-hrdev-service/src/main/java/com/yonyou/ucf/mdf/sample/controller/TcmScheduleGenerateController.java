package com.yonyou.ucf.mdf.sample.controller;

import com.yonyou.ucf.mdf.sample.service.TcmScheduleGenerateService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/waybill")
public class TcmScheduleGenerateController {

    @Autowired
    private TcmScheduleGenerateService scheduleGenerateService;

    @PostMapping("/generate-schedule")
    public ResponseEntity<?> generateSchedule(@RequestBody Map<String, String> body) {
        try {
            String date = body.get("date");
            Map<String, Object> result = scheduleGenerateService.generateSchedule(date);
            return ResponseUtil.success("生成排更成功", result);
        } catch (Exception e) {
            return ResponseUtil.error(e);
        }
    }

    /**
     * 按休假规则生成休假记录并关联考勤修正缺勤数据
     * @param body ruleId 规则ID，date 日期 yyyy-MM-dd
     */
    @PostMapping("/generate-leave-record")
    public ResponseEntity<?> generateLeaveR(@RequestBody Map<String, String> body) {
        try {
            String ruleId = body.get("ruleId");
            String date = body.get("date");
            scheduleGenerateService.generateLeaveR(ruleId, date);
            return ResponseUtil.success("生成休假記錄成功");
        } catch (Exception e) {
            return ResponseUtil.error(e);
        }
    }



}
