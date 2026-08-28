package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSyncLog;
import com.yonyou.ucf.mdf.sample.service.TcmSyncLogService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;

/**
 * 同步日志服务实现
 * @author root
 */
@Service
public class TcmSyncLogServiceImpl implements TcmSyncLogService {

    @Autowired
    private IBillRepository billRepository;

    /**
     * 同步状态 - 未同步
     */
    private static final String SYNC_STATUS_NOT_SYNCED = "1";
    /**
     * 同步状态 - 已同步
     */
    private static final String SYNC_STATUS_SYNCED = "2";
    /**
     * 任务完成状态 - 正常
     */
    private static final String TASK_STATUS_NORMAL = "1";
    /**
     * 任务完成状态 - 异常
     */
    private static final String TASK_STATUS_ABNORMAL = "2";

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TcmSyncLog recordSyncLog(String date, String model) {
        // 记录方法开始时间
        Date startTime = new Date();

        TcmSyncLog syncLog = new TcmSyncLog();
        syncLog.setDate(date);
        syncLog.setModel(model);
        syncLog.setQueue_start_time(startTime);
        syncLog.setYtenantId(InvocationInfoProxy.getTenantid());
        // 方法开始时为未同步
        syncLog.setSync_status(SYNC_STATUS_NOT_SYNCED);

        try {
            // TODO 在此执行实际同步业务逻辑

            // 同步业务执行成功，记录方法结束时间并标记为已同步
            syncLog.setQueue_end_time(new Date());
            syncLog.setSync_status(SYNC_STATUS_SYNCED);
            syncLog.setTask_status(TASK_STATUS_NORMAL);
        } catch (Exception e) {
            // 异常时记录结束时间，状态保持未同步，任务标记为异常
            syncLog.setQueue_end_time(new Date());
            syncLog.setSync_status(SYNC_STATUS_NOT_SYNCED);
            syncLog.setTask_status(TASK_STATUS_ABNORMAL);
            throw new RuntimeException(e);
        }

        // 通过 service 层调用仓储落库
        billRepository.insert(Collections.singletonList(syncLog));
        return syncLog;
    }

}
