package com.yonyou.ucf.mdf.sample.service;

public interface TcmHiSenseAttendanceService {

    /**
     * 同步海信数据
     * @param runDate 业务日期
     * @return 合并后的考勤数据列表
     */
    void syncData(String runDate);


    /**
     * 统计数据数量
     * @param runDate 业务日期
     * @return 数量
     */
    Integer countByRunDate(String runDate);


    /**
     * 根据日期删除数据
     * @param runDate 业务日期
     */
    void removeByRunDate(String runDate);



    /**
     * 根据日期查询数据是否存在
     * @param date 日期 yyyy-MM-dd
     * @return true 存在
     * @throws Exception .
     */
    boolean checkDataByDate(String date) throws Exception;

}
