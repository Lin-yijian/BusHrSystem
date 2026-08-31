package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSyncLog;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.TaskTypeEnum;
import com.yonyou.ucf.mdf.sample.service.TcmSyncLogService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 同步日志服务实现
 * <p>
 * 提供 3 种使用方式：
 * <ol>
 *   <li>{@link #initSyncLog} + {@link #finishSyncLog}：前端两阶段式（建日志→触发动作→回填）</li>
 *   <li>{@link #recordSyncLog}：遗留单阶段接口（方法内同步动作后落库）</li>
 *   <li>AOP 方式：{@code @SyncLog} 注解，见 {@code TcmSyncLogAspect}</li>
 * </ol>
 *
 * <p><b>字段拆分规则（异常场景）：</b>
 * <ul>
 *   <li>remark：面向用户/列表展示的简短描述 — "同步失败：{异常类名}: {异常消息}"</li>
 *   <li>error_info：面向开发定位问题的完整堆栈（含 cause 链路，截断 8000 字符）</li>
 * </ul>
 *
 * @author root
 */
@Service
@Slf4j
public class TcmSyncLogServiceImpl implements TcmSyncLogService {

    // =================== 状态常量（与 AOP / 前端约定保持一致） ===================
    /** 同步状态 - 未同步 */
    public static final String SYNC_STATUS_NOT_SYNCED = "1";
    /** 同步状态 - 已同步(成功) */
    public static final String SYNC_STATUS_SYNCED_SUCCESS = "2";
    /** 同步状态 - 已同步(失败) */
    public static final String SYNC_STATUS_SYNCED_FAIL = "3";

    /** 任务完成状态 - 正常 */
    public static final String TASK_STATUS_NORMAL = "2";
    /** 任务完成状态 - 异常 */
    public static final String TASK_STATUS_ABNORMAL = "1";

    /** 初始化备注 */
    private static final String REMARK_INIT = "正在开始同步...";
    /** 成功备注 */
    private static final String REMARK_SUCCESS = "同步成功";
    /** 失败备注前缀 */
    private static final String REMARK_FAIL_PREFIX = "同步失败";
    /** remark 最大长度（避免 DB VARCHAR 字段超长） */
    private static final int REMARK_MAX_LEN = 2000;
    /** error_info 完整堆栈最大长度（DB字段建议 TEXT/LONGTEXT；此处截断防极端OOM） */
    private static final int ERROR_INFO_MAX_LEN = 8000;

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private YmsOidGenerator ymsOidGenerator;

    // =================== 一、两阶段（新） ===================

    /**
     * 以 REQUIRES_NEW 独立事务创建日志，确保 syncData 主事务回滚不影响日志记录。
     * 默认：sync_status=未同步(0), task_status=异常(2), remark=正在开始同步..., error_info 清空
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public TcmSyncLog initSyncLog(String date, String model) {

        if (StringUtils.isBlank(date)) {
            throw new IllegalArgumentException("同步日志初始化失败：业务日期不能为空");
        }
        if (StringUtils.isBlank(model)) {
            throw new IllegalArgumentException("同步日志初始化失败：所属模块不能为空");
        }

        if (TaskTypeEnum.getByCode(model) == null) {
            throw new IllegalArgumentException("模块编码无效：" + model + "，可选值：" + buildModuleOptions());
        }

        Date now = new Date();
        String tenantId = InvocationInfoProxy.getTenantid();
        String userId = InvocationInfoProxy.getUserid();

        TcmSyncLog syncLog = new TcmSyncLog();
        syncLog.setId(ymsOidGenerator.nextStrId());
        syncLog.setDate(date);
        syncLog.setModel(model);
        syncLog.setSync_status(SYNC_STATUS_NOT_SYNCED);
        syncLog.setTask_status(TASK_STATUS_ABNORMAL);
        syncLog.setQueue_start_time(now);
        syncLog.setRemark(REMARK_INIT);
        syncLog.setError_info(null);
        syncLog.setDr(BaseEnum.IS_NOT_DELETED.getIntCode().shortValue());
        syncLog.setYtenantId(tenantId);
        syncLog.setCreator(userId);
        syncLog.setCreateTime(now);
        syncLog.setModifier(userId);
        syncLog.setModifyTime(now);

        billRepository.insert(Collections.singletonList(syncLog));
        log.info("[TcmSyncLog] init log saved. id={}, model={}, date={}, start={}",
                syncLog.getId(), model, date, now);
        return syncLog;
    }

    /**
     * 以 REQUIRES_NEW 独立事务回填日志结束信息，确保 syncData 主事务回滚不影响日志记录。
     *
     * <p><b>异常字段拆分：</b>
     * <ul>
     *   <li>remark = 简短失败描述（列表直接展示）</li>
     *   <li>error_info = 完整堆栈（点进去再看）</li>
     * </ul>
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public TcmSyncLog finishSyncLog(String syncLogId, Throwable throwable, String remarkEx) {
        if (StringUtils.isBlank(syncLogId)) {
            log.warn("[TcmSyncLog] finishSyncLog: syncLogId is blank, skip.");
            return null;
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        TcmSyncLog syncLog = findById(syncLogId, tenantId);
        if (syncLog == null) {
            log.warn("[TcmSyncLog] finishSyncLog: log not found. id={}", syncLogId);
            return null;
        }

        Date now = new Date();
        String userId = InvocationInfoProxy.getUserid();
        syncLog.setQueue_end_time(now);
        syncLog.setModifier(userId);
        syncLog.setModifyTime(now);

        if (throwable == null) {
            // ========== 正常结束 ==========
            syncLog.setSync_status(SYNC_STATUS_SYNCED_SUCCESS);
            syncLog.setTask_status(TASK_STATUS_NORMAL);
            String remark = StringUtils.isBlank(remarkEx) ? REMARK_SUCCESS : remarkEx;
            syncLog.setRemark(truncate(remark, REMARK_MAX_LEN));
            syncLog.setError_info(null);
        } else {
            // ========== 异常结束：拆分 简述(remark) + 堆栈(error_info) ==========
            syncLog.setSync_status(SYNC_STATUS_SYNCED_FAIL);
            syncLog.setTask_status(TASK_STATUS_ABNORMAL);
            syncLog.setRemark(buildErrorRemark(throwable, remarkEx));
            syncLog.setError_info(buildFullStackTrace(throwable));
        }

        billRepository.update(Collections.singletonList(syncLog));
        log.info("[TcmSyncLog] finish log saved. id={}, task_status={}, sync_status={}, cost_ms={}",
                syncLog.getId(),
                syncLog.getTask_status(),
                syncLog.getSync_status(),
                (syncLog.getQueue_end_time() == null || syncLog.getQueue_start_time() == null) ? -1 :
                        (syncLog.getQueue_end_time().getTime() - syncLog.getQueue_start_time().getTime()));
        return syncLog;
    }

    // =================== 二、遗留：单阶段 ===================

    @Override
    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    public TcmSyncLog recordSyncLog(String date, String model) {
        Date startTime = new Date();
        Date now = new Date();
        String userId = InvocationInfoProxy.getUserid();
        String tenantId = InvocationInfoProxy.getTenantid();

        TcmSyncLog syncLog = new TcmSyncLog();
        syncLog.setId(ymsOidGenerator.nextStrId());
        syncLog.setDate(date);
        syncLog.setModel(model);
        syncLog.setQueue_start_time(startTime);
        syncLog.setYtenantId(tenantId);
        syncLog.setSync_status(SYNC_STATUS_NOT_SYNCED);

        try {
            syncLog.setQueue_end_time(new Date());
            syncLog.setSync_status(SYNC_STATUS_SYNCED_SUCCESS);
            syncLog.setTask_status(TASK_STATUS_NORMAL);
            syncLog.setRemark(REMARK_SUCCESS);
            syncLog.setError_info(null);
        } catch (Exception e) {
            syncLog.setQueue_end_time(new Date());
            syncLog.setSync_status(SYNC_STATUS_SYNCED_FAIL);
            syncLog.setTask_status(TASK_STATUS_ABNORMAL);
            syncLog.setRemark(buildErrorRemark(e, null));
            syncLog.setError_info(buildFullStackTrace(e));
            throw new RuntimeException(e);
        } finally {
            syncLog.setDr(BaseEnum.IS_NOT_DELETED.getIntCode().shortValue());
            syncLog.setCreator(userId);
            syncLog.setCreateTime(now);
            syncLog.setModifier(userId);
            syncLog.setModifyTime(now);
        }
        billRepository.insert(Collections.singletonList(syncLog));
        return syncLog;
    }

    // =================== 内部工具：异常信息拆分 ===================

    /**
     * 构造简短失败描述（放到 remark 字段，给列表展示）。
     * 格式：{remarkEx前缀 | }同步失败：{异常类名}: {异常message}
     * <p>该方法已 public static，AOP 等其它组件可直接复用，保证异常描述格式一致。
     */
    public static String buildErrorRemark(Throwable throwable, String remarkEx) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(remarkEx)) {
            sb.append(remarkEx).append(" | ");
        }
        sb.append(REMARK_FAIL_PREFIX);
        if (throwable != null) {
            String msg = throwable.getMessage();
            String simpleName = throwable.getClass().getSimpleName();
            if (StringUtils.isBlank(msg)) {
                sb.append("：").append(simpleName);
            } else {
                sb.append("：").append(simpleName).append(": ").append(firstLineOf(msg));
            }
        }
        return truncate(sb.toString(), REMARK_MAX_LEN);
    }

    /**
     * 构造完整堆栈信息（放到 error_info 字段，便于排查；含 cause 链路）。
     * <p>public static，供 AOP 等跨包调用复用。
     */
    public static String buildFullStackTrace(Throwable throwable) {
        if (throwable == null) return null;
        StringWriter sw = new StringWriter(512);
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
        }
        return truncate(sw.toString(), ERROR_INFO_MAX_LEN);
    }

    // =================== 内部工具：杂项 ===================

    private static String firstLineOf(String msg) {
        if (msg == null) return null;
        int idx = msg.indexOf('\n');
        if (idx < 0) idx = msg.indexOf('\r');
        return idx < 0 ? msg : msg.substring(0, idx);
    }

    private static String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() <= maxLen ? str : str.substring(0, maxLen) + "...";
    }

    /** remark 字段长度限制（public 供跨包共享） */
    public static int remarkMaxLen() { return REMARK_MAX_LEN; }
    /** error_info 字段长度限制（public 供跨包共享） */
    public static int errorInfoMaxLen() { return ERROR_INFO_MAX_LEN; }
    /** 成功备注（public 供跨包共享） */
    public static String remarkSuccess() { return REMARK_SUCCESS; }

    private TcmSyncLog findById(String id, String tenantId) {
        LambdaQueryCondition<TcmSyncLog> cond = new LambdaQueryCondition<>(new TcmSyncLog());
        cond.eq(TcmSyncLog::getId, id)
                .eq(TcmSyncLog::getYtenantId, tenantId)
                .eq(TcmSyncLog::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmSyncLog> list = billRepository.queryByClause(cond);
        return list.isEmpty() ? null : list.get(0);
    }

    private String buildModuleOptions() {
        StringBuilder sb = new StringBuilder();
        for (TaskTypeEnum e : TaskTypeEnum.values()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(e.getCode()).append("(").append(e.getDesc()).append(")");
        }
        return sb.toString();
    }

}
