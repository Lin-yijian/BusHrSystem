package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmSyncLog;

/**
 * 同步日志服务
 * @author root
 */
public interface TcmSyncLogService {

    /**
     * 动态接收日期、模块类型，记录方法开始与结束的日期时间并落库
     * @param date 日期 yyyy-MM-dd
     * @param model 所属模块类型
     * @return 落库后的同步日志实体
     */
    TcmSyncLog recordSyncLog(String date, String model);

    /**
     *【初始化阶段】在同步动作触发前，预创建一条同步日志
     * <p>字段默认值：
     * <ul>
     *   <li>sync_status = 0 (未同步)</li>
     *   <li>task_status = 2 (异常/未完成)</li>
     *   <li>queue_start_time = 当前时间</li>
     *   <li>remark = 正在开始同步...</li>
     * </ul>
     *
     * @param date  业务日期 yyyy-MM-dd
     * @param model 所属模块编码（见 ModuleEnum）
     * @return 已落库的日志实体（含ID）
     */
    TcmSyncLog initSyncLog(String date, String model);

    /**
     *【完成阶段】同步动作结束后，回填之前预创建的那条同步日志
     * <p>规则：
     * <ul>
     *   <li>throwable == null：sync_status=1 task_status=1 remark=同步成功</li>
     *   <li>throwable != null：sync_status=2 task_status=2 remark=异常信息摘要</li>
     *   <li>queue_end_time = 当前时间</li>
     * </ul>
     *
     * @param syncLogId  initSyncLog返回的日志ID
     * @param throwable  同步动作抛出的异常（成功时传null）
     * @param remarkEx   附加备注（可空）；异常时如果throwable有信息会自动拼接
     * @return 更新后的日志实体，找不到ID时返回null
     */
    TcmSyncLog finishSyncLog(String syncLogId, Throwable throwable, String remarkEx);

}
