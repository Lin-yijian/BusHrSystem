package com.yonyou.ucf.mdf.sample.service;

import java.util.Set;

public interface TcmAttendanceCheckRecordService {

    /**
     * 更新核对记录状态
     * @param checkDateList 日期
     * @param module 涉及模块
     * @param isGood true 打勾  false 打叉
     */
    void updateOrSaveRecord(Set<String> checkDateList,String module, boolean isGood);
}
