package com.yonyou.ucf.mdf.sample.service;

public interface TcmFuelGasDataRecordService {

    /**
     * 根据日期查询数据是否存在
     * @param date 日期 yyyy-MM-dd
     * @return true 存在
     */
    boolean checkDataByDate(String date);



}
