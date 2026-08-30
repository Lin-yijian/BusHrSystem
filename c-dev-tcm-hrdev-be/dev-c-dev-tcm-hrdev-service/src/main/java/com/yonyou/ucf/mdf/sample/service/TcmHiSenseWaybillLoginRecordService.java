package com.yonyou.ucf.mdf.sample.service;

public interface TcmHiSenseWaybillLoginRecordService {


    /**
     * 根据日期同步海信车载机登入记录数据
     * @param actDate 日期（格式：yyyy-MM-dd）
     */
    void syncByActDate(String actDate);

    /**
     *【两阶段日志】使用预创建的同步日志ID执行海信车载机登入记录同步，完成后回填日志结束时间和状态。
     * @param actDate   业务日期（格式：yyyy-MM-dd）
     * @param syncLogId initSyncLog创建的日志ID（为空时回退到AOP方式记录）
     */
    void syncByActDate(String actDate, String syncLogId);
    

    /**
     * 统计数据数量
     * @param actDate 业务日期
     * @return 数量
     */
    Integer countByActDate(String actDate);


    /**
     * 根据日期删除数据
     * @param actDate 业务日期
     */
    void removeByActDate(String actDate);



    /**
     * 根据日期查询数据是否存在
     * @param actDate 日期 yyyy-MM-dd
     * @return true 存在
     * @throws Exception .
     */
    boolean checkDataByDate(String actDate);
    
}