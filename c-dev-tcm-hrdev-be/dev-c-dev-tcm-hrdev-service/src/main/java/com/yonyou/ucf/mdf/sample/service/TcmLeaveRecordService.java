package com.yonyou.ucf.mdf.sample.service;

public interface TcmLeaveRecordService {

    boolean checkExist(String date, String employeeId, String type);

    void delByEmployeeIdAndDate(String date, String employeeId);
}
