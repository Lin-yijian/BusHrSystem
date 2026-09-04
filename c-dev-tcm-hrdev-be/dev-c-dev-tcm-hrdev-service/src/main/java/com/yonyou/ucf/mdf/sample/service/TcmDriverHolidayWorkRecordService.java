package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverHolidayWorkRecord;

import java.util.Collection;
import java.util.List;

public interface TcmDriverHolidayWorkRecordService {

    /**
     * 根据日期+工号删除数据
     * @param date 日期 yyyy-MM-dd
     * @param employeeIds BIP工号
     */
    void removeByDateAndEmployeeIds(String date, Collection<String> employeeIds);


    /**
     * 覆盖新增 删除日期+工号集合的数据，再进行新增
     * @param date 日期
     * @param list 集合数据
     */
    void coverInsert(String date, List<TcmDriverHolidayWorkRecord> list);


    /**
     *
     * @param date
     * @param waybillEmployeeId
     * @return
     */
    List<TcmDriverHolidayWorkRecord> getByDateAndWaybillEmployeeId(String date, String waybillEmployeeId);


    void saveList(List<TcmDriverHolidayWorkRecord> list);

}
