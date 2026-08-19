package com.yonyou.ucf.mdf.sample.aop;

import com.yonyou.ucf.mdf.sample.enums.TaskTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 同步日志注解
 * <p>标注在需要记录同步日志的目标方法上，由 {@link TcmSyncLogAspect} 拦截触发。
 * <p>model 从注解传入；date 从目标方法参数（由 {@link #dateParam()} 指定的参数名）动态传入。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SyncLog {

    /** 所属模块，对应 TcmSyncLog.model */
    TaskTypeEnum model();

    /** 目标方法中持有日期值的参数名，默认 "date"，对应 TcmSyncLog.date */
    String dateParam() default "date";
}
