package com.yonyou.ucf.mdf.sample.controller;

import com.yonyou.ucf.mdf.sample.service.TcmHiSenseWaybillLoginRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmSyncLogService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loginRecord/hiSense")
@Slf4j
public class TcmHiSenseWaybillLoginRecordController {

    @Autowired
    private TcmHiSenseWaybillLoginRecordService service;

    @Autowired
    private TcmSyncLogService tcmSyncLogService;


    /**
     *【新】使用两阶段同步日志的海信车载机登入记录同步接口。
     * 前端流程：
     * <ol>
     *   <li>先调 POST /api/sync-log/init（body: model=TCM_HI_SENSE_LOGIN_RECORD/对应ModuleEnum编码, date=yyyy-MM-dd）→ 得到 syncLogId</li>
     *   <li>调用本接口并传 syncLogId 参数</li>
     * </ol>
     */
    @GetMapping("/sync")
    public ResponseEntity<?> sync(@RequestParam(defaultValue = "",value = "runDate") String runDate,
                                  @RequestParam(defaultValue = "",value = "syncLogId", required = false) String syncLogId) {
        try {
            service.syncByActDate(runDate, syncLogId);
            return ResponseUtil.success("成功同步海信數據");
        } catch (Exception e) {
            if (syncLogId != null && !syncLogId.trim().isEmpty()) {
                try {
                    tcmSyncLogService.finishSyncLog(syncLogId, e, null);
                } catch (Exception finishEx) {
                    log.error("[TcmHiSenseWaybillLoginRecordController.sync] 兜底回填同步日志失败 syncLogId={}", syncLogId, finishEx);
                }
            }
            return ResponseUtil.error(e);
        }
    }


    @GetMapping("/count")
    public ResponseEntity<?> count(@RequestParam String runDate) {

        Integer count = service.countByActDate(runDate);
        return ResponseUtil.success(runDate +" 存在數據數：" + count,count);
    }
}
