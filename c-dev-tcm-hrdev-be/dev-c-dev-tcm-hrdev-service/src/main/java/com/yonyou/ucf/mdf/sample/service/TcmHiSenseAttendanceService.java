package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseAttendanceMergedDTO;

import java.util.List;

public interface TcmHiSenseAttendanceService {

    /**
     * 同步海信数据
     * @param runDate 业务日期
     * @return 合并后的考勤数据列表
     */
    void syncData(String runDate);
}
