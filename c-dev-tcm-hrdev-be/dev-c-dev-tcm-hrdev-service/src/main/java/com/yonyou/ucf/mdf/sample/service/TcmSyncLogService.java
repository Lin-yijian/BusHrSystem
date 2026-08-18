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

}
