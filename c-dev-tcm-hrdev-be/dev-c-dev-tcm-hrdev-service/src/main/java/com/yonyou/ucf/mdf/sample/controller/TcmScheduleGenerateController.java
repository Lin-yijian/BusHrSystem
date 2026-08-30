package com.yonyou.ucf.mdf.sample.controller;

import com.yonyou.ucf.mdf.sample.service.TcmScheduleGenerateService;
import com.yonyou.ucf.mdf.sample.service.TcmSyncLogService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/waybill")
@Slf4j
public class TcmScheduleGenerateController {

    @Autowired
    private TcmScheduleGenerateService scheduleGenerateService;

    @Autowired
    private TcmSyncLogService tcmSyncLogService;

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
     *【新】使用两阶段同步日志生成休假记录。
     * body 支持字段：
     *   - ruleId : 必填，休假规则ID
     *   - date   : 必填，业务日期 yyyy-MM-dd
     *   - syncLogId : 可选，调 POST /api/sync-log/init 预先创建日志后返回的ID。若有则回填队列结束时间/状态；若无则走旧AOP方式。
     */
    @PostMapping("/generate-leave-record")
    public ResponseEntity<?> generateLeaveR(@RequestBody Map<String, String> body) {
        String ruleId = body.get("ruleId");
        String date = body.get("date");
        String syncLogId = body.get("syncLogId");
        try {
            scheduleGenerateService.generateLeaveR(ruleId, date, syncLogId);
            return ResponseUtil.success("生成休假記錄成功");
        } catch (Exception e) {
            if (syncLogId != null && !syncLogId.trim().isEmpty()) {
                try {
                    tcmSyncLogService.finishSyncLog(syncLogId, e, null);
                } catch (Exception finishEx) {
                    log.error("[TcmScheduleGenerateController.generateLeaveR] 兜底回填同步日志失败 syncLogId={}", syncLogId, finishEx);
                }
            }
            return ResponseUtil.error(e);
        }
    }



}
