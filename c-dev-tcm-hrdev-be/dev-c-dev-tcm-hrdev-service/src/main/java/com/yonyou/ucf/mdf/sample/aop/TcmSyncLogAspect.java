package com.yonyou.ucf.mdf.sample.aop;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSyncLog;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 切点：标注了 @SyncLog 的方法
     */
    @Pointcut("@annotation(com.yonyou.ucf.mdf.sample.aop.SyncLog)")
    public void syncLogPointcut() {
    }

    /**
     * 独立 Helper，避免在切面类上使用 @Transactional 导致代理失效；
     * 也避免了"切面自注入"的循环依赖陷阱。
     */
    @Autowired
    private TcmSyncLogPersister syncLogPersister;

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("syncLogPointcut()")
    public Object aroundSyncLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 优先从目标类实现方法取 @SyncLog（接口代理 + JDK代理 时签名方法可能取到的是接口上没有的注解）
        SyncLog syncLog = resolveSyncLogAnnotation(joinPoint);
        if (syncLog == null) {
            if (log.isDebugEnabled()) {
                log.debug("TcmSyncLogAspect.aroundSyncLog: @SyncLog not resolved, skip. signature={}", joinPoint.getSignature());
            }
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String targetClass = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getMethod().getName();
        String fullMethodName = targetClass + "." + methodName;

        // 记录目标方法开始时间
        Date startTime = new Date();
        String date = resolveDate(joinPoint, syncLog.dateParam());
        String model = syncLog.model().getCode();
        String businessDesc = syncLog.model().getDesc();

        // 1. 方法进入时打印结构化日志（参考 MethodLogAspect 的框线风格）
        logEnter(fullMethodName, businessDesc, date, model, signature.getParameterNames(), joinPoint.getArgs());

        String taskStatus = STATUS_NORMAL;
        Throwable error = null;
        Object result;
        try {
            result = joinPoint.proceed();
            taskStatus = STATUS_NORMAL;
            return result;
        } catch (Throwable e) {
            taskStatus = STATUS_ABNORMAL;
            error = e;
            throw e;
        } finally {
            Date endTime = new Date();
            long costMs = endTime.getTime() - startTime.getTime();
            TcmSyncLog entity = buildSyncLog(date, model, startTime, endTime, taskStatus);

            // 2. 无论正常/异常，先打印"方法结束"结构化日志（含报错、耗时）
            logExit(fullMethodName, businessDesc, date, model, taskStatus, costMs, error,
                    signature.getParameterNames(), joinPoint.getArgs());

            // 3. 落库（新事务 REQUIRES_NEW，且外层 try/catch 保护绝不影响目标方法抛出的异常）
            try {
                syncLogPersister.saveSyncLog(entity);
            } catch (Exception ex) {
                log.error("【同步日志AOP触发】落库失败. method={}, status={}, date={}, model={}",
                        fullMethodName, taskStatus, date, model, ex);
            }
        }
    }

    /**
     * 方法进入时打印结构化日志（对齐 MethodLogAspect 的框线风格）
     */
    private void logEnter(String fullMethodName, String businessDesc, String date, String model,
                          String[] paramNames, Object[] args) {
        StringBuilder sb = new StringBuilder()
                .append("\n╔═══════════════════════════════════════════════════════════════════")
                .append("\n║ 【同步日志AOP·开始】").append(fullMethodName)
                .append(businessDesc != null && !businessDesc.isEmpty() ? " - " + businessDesc : "")
                .append("\n║ 【调用时间】").append(LocalDateTime.now().format(FORMATTER))
                .append("\n║ 【业务日期】").append(date == null ? "(未解析到)" : date)
                .append("\n║ 【所属模块】").append(model);

        if (args != null && args.length > 0) {
            sb.append("\n║ 【参数个数】").append(args.length)
                    .append("\n║ 【参数详情】");
            for (int i = 0; i < args.length; i++) {
                String paramName = (paramNames != null && i < paramNames.length) ? paramNames[i] : "arg" + i;
                sb.append("\n║   └─ ").append(paramName)
                        .append(" (").append(getParamType(args[i])).append(")")
                        .append(" = ").append(safeToString(args[i]));
            }
        } else {
            sb.append("\n║ 【参数详情】无参数");
        }
        sb.append("\n╚═══════════════════════════════════════════════════════════════════");
        log.info(sb.toString());
    }

    /**
     * 方法退出时打印结构化日志（正常 + 异常 + 耗时）
     */
    private void logExit(String fullMethodName, String businessDesc, String date, String model,
                         String taskStatus, long costMs, Throwable error,
                         String[] paramNames, Object[] args) {
        StringBuilder sb = new StringBuilder()
                .append("\n┌─────────────────────────────────────────────────────────────")
                .append("\n│ 【同步日志AOP·结束】").append(fullMethodName)
                .append(businessDesc != null && !businessDesc.isEmpty() ? " - " + businessDesc : "")
                .append("\n│ 【执行耗时】").append(costMs).append(" ms")
                .append("\n│ 【执行状态】").append(STATUS_NORMAL.equals(taskStatus) ? "成功" : "失败")
                .append(" (task_status=").append(taskStatus).append(")")
                .append("\n│ 【业务日期】").append(date == null ? "(未解析到)" : date)
                .append("\n│ 【所属模块】").append(model);

        if (error != null) {
            sb.append("\n│ 【异常类型】").append(error.getClass().getSimpleName())
                    .append("\n│ 【异常信息】").append(error.getMessage());
            StackTraceElement[] stackTrace = error.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                sb.append("\n│ 【堆栈信息】")
                        .append("\n│   ").append(stackTrace[0]);
                if (stackTrace.length > 1) {
                    sb.append("\n│   ... 省略 ").append(stackTrace.length - 1).append(" 行");
                }
            }
            if (args != null && args.length > 0) {
                sb.append("\n│ 【异常时参数】");
                for (int i = 0; i < args.length; i++) {
                    String paramName = (paramNames != null && i < paramNames.length) ? paramNames[i] : "arg" + i;
                    sb.append("\n│   └─ ").append(paramName)
                            .append(" (").append(getParamType(args[i])).append(")")
                            .append(" = ").append(safeToString(args[i]));
                }
            }
        }
        sb.append("\n└─────────────────────────────────────────────────────────────");

        if (error != null) {
            // 异常路径用 error 级别，便于捞日志
            log.error(sb.toString(), error);
        } else {
            log.info(sb.toString());
        }
    }

    /**
     * 从目标方法上解析 @SyncLog 注解：优先目标类实现方法，其次回退签名方法。
     * 避免 JDK 接口代理 / 桥接方法导致注解不可见。
     */
    private SyncLog resolveSyncLogAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method signatureMethod = signature.getMethod();
        SyncLog onSignature = signatureMethod.getAnnotation(SyncLog.class);
        if (onSignature != null) {
            return onSignature;
        }
        try {
            Method targetMethod = joinPoint.getTarget().getClass()
                    .getMethod(signatureMethod.getName(), signatureMethod.getParameterTypes());
            SyncLog onTarget = targetMethod.getAnnotation(SyncLog.class);
            if (onTarget != null) {
                return onTarget;
            }
        } catch (NoSuchMethodException ex) {
            // 忽略，回退 null
        }
        return null;
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

    /**
     * 参数类型（参考 MethodLogAspect 的简洁写法）
     */
    private String getParamType(Object param) {
        if (param == null) return "null";
        if (param instanceof java.util.Collection)
            return "Collection(size=" + ((java.util.Collection<?>) param).size() + ")";
        if (param instanceof java.util.Map)
            return "Map(size=" + ((java.util.Map<?, ?>) param).size() + ")";
        if (param.getClass().isArray())
            return "Array(length=" + java.lang.reflect.Array.getLength(param) + ")";
        return param.getClass().getSimpleName();
    }

    /**
     * 安全 toString（避免实体 toString 抛异常时中断日志输出）
     */
    private String safeToString(Object obj) {
        if (obj == null) return "null";
        try {
            return obj.toString();
        } catch (Exception e) {
            return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
        }
    }

    /**
     * 同步日志落库 Helper Bean。
     * 拆分到独立组件上使用 REQUIRES_NEW 事务，确保：
     * 1. 目标方法事务回滚时，日志事务独立提交；
     * 2. 目标方法抛错后，finally 内调用的落库不会被卷入外层事务。
     */
    @Component
    static class TcmSyncLogPersister {

        @Autowired
        private IBillRepository billRepository;

        @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
        public void saveSyncLog(TcmSyncLog syncLog) {
            billRepository.insert(Collections.singletonList(syncLog));
        }
    }
}
