package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSyncLog;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
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
 * @author root
 */
@Service
@Slf4j
public class TcmSyncLogServiceImpl implements TcmSyncLogService {

    // =================== 状态常量 ===================
    /** 同步状态 - 未同步 */
    public static final String SYNC_STATUS_NOT_SYNCED = "0";
    /** 同步状态 - 已同步(成功) */
    public static final String SYNC_STATUS_SYNCED_SUCCESS = "1";
    /** 同步状态 - 已同步(失败) */
    public static final String SYNC_STATUS_SYNCED_FAIL = "2";

    /** 任务完成状态 - 正常 */
    public static final String TASK_STATUS_NORMAL = "1";
    /** 任务完成状态 - 异常 */
    public static final String TASK_STATUS_ABNORMAL = "2";

    /** 初始化备注 */
    private static final String REMARK_INIT = "正在开始同步...";
    /** 成功备注 */
    private static final String REMARK_SUCCESS = "同步成功";
    /** 备注最大长度（避免DB字段超长） */
    private static final int REMARK_MAX_LEN = 2000;

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private YmsOidGenerator ymsOidGenerator;

    // =================== 一、两阶段（新） ===================

    /**
     * 以 REQUIRES_NEW 独立事务创建日志，确保 syncData 主事务回滚不影响日志记录。
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

        if (ModuleEnum.getByCode(model) == null) {
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
            // 正常结束
            syncLog.setSync_status(SYNC_STATUS_SYNCED_SUCCESS);
            syncLog.setTask_status(TASK_STATUS_NORMAL);
            String remark = StringUtils.isBlank(remarkEx) ? REMARK_SUCCESS : remarkEx;
            syncLog.setRemark(truncate(remark));
        } else {
            // 异常结束
            syncLog.setSync_status(SYNC_STATUS_SYNCED_FAIL);
            syncLog.setTask_status(TASK_STATUS_ABNORMAL);
            String mergedRemark = mergeRemark(throwable, remarkEx);
            syncLog.setRemark(truncate(mergedRemark));
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

        TcmSyncLog syncLog = new TcmSyncLog();
        syncLog.setId(ymsOidGenerator.nextStrId());
        syncLog.setDate(date);
        syncLog.setModel(model);
        syncLog.setQueue_start_time(startTime);
        syncLog.setYtenantId(InvocationInfoProxy.getTenantid());
        syncLog.setSync_status(SYNC_STATUS_NOT_SYNCED);

        try {
            // 方法入参未提供实际同步动作执行点，调用方若使用此方法需自行在调用前/后完成事务状态
            syncLog.setQueue_end_time(new Date());
            syncLog.setSync_status(SYNC_STATUS_SYNCED_SUCCESS);
            syncLog.setTask_status(TASK_STATUS_NORMAL);
            syncLog.setRemark(REMARK_SUCCESS);
        } catch (Exception e) {
            syncLog.setQueue_end_time(new Date());
            syncLog.setSync_status(SYNC_STATUS_SYNCED_FAIL);
            syncLog.setTask_status(TASK_STATUS_ABNORMAL);
            syncLog.setRemark(truncate(e.getMessage()));
            throw new RuntimeException(e);
        } finally {
            syncLog.setDr(BaseEnum.IS_NOT_DELETED.getIntCode().shortValue());
            syncLog.setCreator(InvocationInfoProxy.getUserid());
            syncLog.setCreateTime(new Date());
            syncLog.setModifier(InvocationInfoProxy.getUserid());
            syncLog.setModifyTime(new Date());
        }
        billRepository.insert(Collections.singletonList(syncLog));
        return syncLog;
    }

    // =================== 内部工具 ===================

    private TcmSyncLog findById(String id, String tenantId) {
        LambdaQueryCondition<TcmSyncLog> cond = new LambdaQueryCondition<>(new TcmSyncLog());
        cond.eq(TcmSyncLog::getId, id)
                .eq(TcmSyncLog::getYtenantId, tenantId)
                .eq(TcmSyncLog::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TcmSyncLog> list = billRepository.queryByClause(cond);
        return list.isEmpty() ? null : list.get(0);
    }

    private String mergeRemark(Throwable throwable, String remarkEx) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(remarkEx)) {
            sb.append(remarkEx);
        }
        if (throwable != null) {
            String msg = throwable.getMessage();
            if (StringUtils.isBlank(msg)) {
                msg = throwable.getClass().getSimpleName();
            }
            if (sb.length() > 0) sb.append(" | ");
            sb.append(msg);

            // 附加第一行堆栈，方便排查
            StackTraceElement[] stes = throwable.getStackTrace();
            if (stes != null && stes.length > 0) {
                sb.append(" @ ").append(stes[0].toString());
            }
        }
        if (sb.length() == 0) {
            sb.append("同步失败");
        }
        return sb.toString();
    }

    private String truncate(String str) {
        if (str == null) return null;
        return str.length() <= REMARK_MAX_LEN ? str : str.substring(0, REMARK_MAX_LEN) + "...";
    }


    private String buildModuleOptions() {
        StringBuilder sb = new StringBuilder();
        for (ModuleEnum e : ModuleEnum.values()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(e.getCode()).append("(").append(e.getName()).append(")");
        }
        return sb.toString();
    }

}
