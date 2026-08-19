package com.yonyou.ucf.mdf.sample.aop;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSyncLog;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;

/**
 * 同步日志 AOP
 * <p>通过 {@link SyncLog} 注解触发，在目标方法执行前后组装 {@link TcmSyncLog} 并调用 billRepository.insert() 落库：
 * <ul>
 *   <li>queue_start_time：目标方法开始时间</li>
 *   <li>queue_end_time：目标方法结束时间</li>
 *   <li>task_status：目标方法报错为 2，正常为 1</li>
 *   <li>sync_status：与 task_status 逻辑一致</li>
 *   <li>date：从目标方法参数动态传入</li>
 *   <li>model：从 @SyncLog 注解传入</li>
 * </ul>
 */
@Aspect
@Component
@Slf4j
public class TcmSyncLogAspect {

    /** 任务/同步状态 - 正常 */
    private static final String STATUS_NORMAL = "1";
    /** 任务/同步状态 - 异常 */
    private static final String STATUS_ABNORMAL = "2";

    @Autowired
    private IBillRepository billRepository;

    /**
     * 自身代理，用于触发新事务落库，保证目标方法报错回滚后日志仍能记录
     */
    @Autowired
    @Lazy
    private TcmSyncLogAspect self;

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(syncLog)")
    public Object aroundSyncLog(ProceedingJoinPoint joinPoint, SyncLog syncLog) throws Throwable {
        // 记录目标方法开始时间
        Date startTime = new Date();
        String date = resolveDate(joinPoint, syncLog.dateParam());
        String model = syncLog.model().getCode();

        String taskStatus = STATUS_NORMAL;
        Object result;
        try {
            result = joinPoint.proceed();
            // 正常执行，状态为 1
            taskStatus = STATUS_NORMAL;
        } catch (Throwable e) {
            // 目标方法报错，状态为 2
            taskStatus = STATUS_ABNORMAL;
            throw e;
        } finally {
            // 记录目标方法结束时间并落库；sync_status 与 task_status 逻辑一致
            Date endTime = new Date();
            self.saveSyncLog(buildSyncLog(date, model, startTime, endTime, taskStatus));
        }
        return result;
    }

    /**
     * 组装同步日志
     */
    private TcmSyncLog buildSyncLog(String date, String model, Date startTime, Date endTime, String status) {
        TcmSyncLog syncLog = new TcmSyncLog();
        syncLog.setDate(date);
        syncLog.setModel(model);
        syncLog.setQueue_start_time(startTime);
        syncLog.setQueue_end_time(endTime);
        // sync_status 与 task_status 逻辑一致
        syncLog.setTask_status(status);
        syncLog.setSync_status(status);
        syncLog.setYtenantId(InvocationInfoProxy.getTenantid());
        return syncLog;
    }

    /**
     * 落库同步日志，使用新事务，避免目标方法事务回滚导致日志丢失
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void saveSyncLog(TcmSyncLog syncLog) {
        try {
            billRepository.insert(Collections.singletonList(syncLog));
        } catch (Exception e) {
            // 落库失败不影响目标方法
            log.error("记录同步日志失败", e);
        }
    }

    /**
     * 根据参数名从目标方法参数中解析 date 值
     */
    private String resolveDate(ProceedingJoinPoint joinPoint, String dateParam) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(signature.getMethod());
        Object[] args = joinPoint.getArgs();
        if (parameterNames == null) {
            return null;
        }
        for (int i = 0; i < parameterNames.length; i++) {
            if (dateParam.equals(parameterNames[i]) && args[i] != null) {
                return args[i].toString();
            }
        }
        return null;
    }
}
