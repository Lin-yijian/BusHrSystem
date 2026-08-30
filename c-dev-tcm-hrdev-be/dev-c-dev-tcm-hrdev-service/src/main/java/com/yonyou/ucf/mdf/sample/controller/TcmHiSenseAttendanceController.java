package com.yonyou.ucf.mdf.sample.controller;


import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseAttendanceMergedDTO;
import com.yonyou.ucf.mdf.sample.service.TcmHiSenseAttendanceService;
import com.yonyou.ucf.mdf.sample.service.TcmSyncLogService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/attendance/hiSense")
@Slf4j
public class TcmHiSenseAttendanceController {

    @Autowired
    private TcmHiSenseAttendanceService service;

    @Autowired
    private TcmSyncLogService tcmSyncLogService;

    /**
     *【新】使用两阶段同步日志的海信考勤同步接口。
     * 前端流程：
     * <ol>
     *   <li>先调 POST /api/sync-log/init（body: model=TCM_HI_SENSE_ATTENDANCE, date=yyyy-MM-dd）→ 得到 syncLogId</li>
     *   <li>调用本接口并传 syncLogId 参数</li>
     * </ol>
     * 执行完后，syncData(service)会以 REQUIRES_NEW 独立事务回填结束时间/状态/备注。
     * 即使 syncData 主事务最终因异常回滚，日志记录仍保留可查。
     */
    @GetMapping("/sync")
    public ResponseEntity<?> sync(@RequestParam(defaultValue = "",value = "runDate") String runDate,
                                  @RequestParam(defaultValue = "",value = "syncLogId", required = false) String syncLogId) {
        try {
            service.syncData(runDate, syncLogId);
            return ResponseUtil.success("成功获取海信数据");
        } catch (Exception e) {
            // 如果前端没传 syncLogId（老调用方式），仍兜底尝试完成阶段，但会找不到日志，行为退化为仅响应报错
            if (syncLogId != null && !syncLogId.trim().isEmpty()) {
                try {
                    tcmSyncLogService.finishSyncLog(syncLogId, e, null);
                } catch (Exception finishEx) {
                    log.error("[TcmHiSenseAttendanceController.sync] 兜底回填同步日志失败 syncLogId={}", syncLogId, finishEx);
                }
            }
            return ResponseUtil.error(e);
        }
    }


    @GetMapping("/syncBaType")
    public ResponseEntity<?> syncBaType(@RequestParam(defaultValue = "",value = "runDate") String runDate,
                                        @RequestParam(defaultValue = "",value = "syncLogId", required = false) String syncLogId) {
        try {
            service.syncBaType(runDate, syncLogId);
            return ResponseUtil.success("成功获取海信数据");
        } catch (Exception e) {
            if (syncLogId != null && !syncLogId.trim().isEmpty()) {
                try {
                    tcmSyncLogService.finishSyncLog(syncLogId, e, null);
                } catch (Exception finishEx) {
                    log.error("[TcmHiSenseAttendanceController.syncBaType] 兜底回填同步日志失败 syncLogId={}", syncLogId, finishEx);
                }
            }
            return ResponseUtil.error(e);
        }
    }


    @GetMapping("/generate")
    public ResponseEntity<?> generate(@RequestParam(defaultValue = "",value = "runDate") String runDate,
                                      @RequestParam(defaultValue = "",value = "syncLogId", required = false) String syncLogId) {
        try {
            service.generate(runDate, syncLogId);
            return ResponseUtil.success("成功生成本地考勤");
        } catch (Exception e) {
            if (syncLogId != null && !syncLogId.trim().isEmpty()) {
                try {
                    tcmSyncLogService.finishSyncLog(syncLogId, e, null);
                } catch (Exception finishEx) {
                    log.error("[TcmHiSenseAttendanceController.generate] 兜底回填同步日志失败 syncLogId={}", syncLogId, finishEx);
                }
            }
            return ResponseUtil.error(e);
        }
    }


    @GetMapping("/count")
    public ResponseEntity<?> count(@RequestParam String runDate) {

        Integer count = service.countByRunDate(runDate);
        return ResponseUtil.success(runDate +" 存在数据数：" + count,count);
    }
    
    
}
