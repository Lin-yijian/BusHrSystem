package com.yonyou.ucf.mdf.sample.service;

import java.util.Collection;
import java.util.Set;

public interface TcmDriverOvertimeRecordService {


    /**
     * 根据日期查询数据是否存在
     * @param date 日期 yyyy-MM-dd
     * @return true 存在
     */
    boolean checkDataByDate(String date);

    /**
     * 批量查询哪些日期仍存在数据（删除收尾用，一次查询避免按日逐条查库）。
     *
     * @param dates 日期集合
     * @return 仍存在数据的日期（归一为 yyyy-MM-dd）
     */
    Set<String> findDatesWithData(Collection<String> dates);
}
