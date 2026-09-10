package com.yonyou.ucf.mdf.sample.controller;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmSyncLog;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.enums.TaskTypeEnum;
import com.yonyou.ucf.mdf.sample.service.TcmSyncLogService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 同步日志控制器
 * <p>
 * 配合两阶段式同步流程：
 * <ol>
 *   <li>前端先调用 {@code POST /api/sync-log/init} 创建日志（状态=未同步，任务状态=异常，备注=正在开始同步...）</li>
 *   <li>前端拿到返回的 {@code syncLogId} 后，调用具体同步接口（如海信考勤同步 {@code /attendance/hiSense/sync}）并把 {@code syncLogId} 一起传入</li>
 *   <li>同步动作在业务控制层内部调用 {@link TcmSyncLogService#finishSyncLog} 回填结束时间、状态与备注</li>
 * </ol>
 * 两阶段均使用 REQUIRES_NEW 事务，保证即使同步业务事务回滚，日志记录仍可完整保留。
 */
@RestController
@RequestMapping("/api/sync-log")
@Slf4j
public class TcmSyncLogController {

    @Autowired
    private TcmSyncLogService tcmSyncLogService;

    /**
     * 初始化同步日志
     * @param body model=所属模块（ModuleEnum.code，如 TCM_HI_SENSE_ATTENDANCE），date=业务日期 yyyy-MM-dd
     */
    @PostMapping("/init")
    public ResponseEntity<?> initSyncLog(@RequestBody Map<String, String> body) {
        try {
            String model = StringUtils.trimToNull(body.get("model"));
            String date = StringUtils.trimToNull(body.get("date"));

            TcmSyncLog syncLog = tcmSyncLogService.initSyncLog(date, model);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("syncLogId", syncLog.getId());
            payload.put("date", syncLog.getDate());
            payload.put("model", syncLog.getModel());
            payload.put("modelName", TaskTypeEnum.getDescByCode(model));
            payload.put("queueStartTime", syncLog.getQueue_start_time());
            payload.put("syncStatus", syncLog.getSync_status());
            payload.put("taskStatus", syncLog.getTask_status());
            payload.put("remark", syncLog.getRemark());
            return ResponseUtil.success("同步日志初始化成功", payload);
        } catch (Exception e) {
            log.error("[TcmSyncLogController.initSyncLog] error", e);
            return ResponseUtil.error(e);
        }
    }

    /**
     * 主动回填同步日志完成状态（兜底接口：业务控制层已在 try/finally 中自动调用，一般不需前端手动调用）
     * @param body syncLogId=日志ID，status=success|fail，remark=自定义备注
     */
    @PostMapping("/finish")
    public ResponseEntity<?> finishSyncLog(@RequestBody Map<String, String> body) {
        try {
            String syncLogId = StringUtils.trimToNull(body.get("syncLogId"));
            String status = StringUtils.trimToEmpty(body.get("status"));
            String remark = body.get("remark");
            if (syncLogId == null) {
                return ResponseUtil.error("syncLogId不能为空");
            }
            Throwable t = "fail".equalsIgnoreCase(status) ? new RuntimeException(remark == null ? "调用方手动标记失败" : remark) : null;
            TcmSyncLog result = tcmSyncLogService.finishSyncLog(syncLogId, t, null);
            if (result == null) {
                return ResponseUtil.error("未找到对应的同步日志：syncLogId=" + syncLogId);
            }
            return ResponseUtil.success("同步日志回填完成");
        } catch (Exception e) {
            log.error("[TcmSyncLogController.finishSyncLog] error", e);
            return ResponseUtil.error(e);
        }
    }

    /**
     * 获取可选模块列表，前端构建下拉框
     */
    @GetMapping("/module-options")
    public ResponseEntity<?> moduleOptions() {
        Map<String, Object>[] arr = new Map[ModuleEnum.values().length];
        int i = 0;
        for (ModuleEnum e : ModuleEnum.values()) {
            Map<String, Object> m = new HashMap<>();
            m.put("code", e.getCode());
            m.put("name", e.getName());
            arr[i++] = m;
        }
        return ResponseUtil.success("模块可选值", arr);
    }
}
