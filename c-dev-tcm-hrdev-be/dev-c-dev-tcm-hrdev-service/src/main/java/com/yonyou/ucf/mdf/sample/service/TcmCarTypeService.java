package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.TCMCarType;

import java.util.List;

public interface TcmCarTypeService {

    /**
     * 获取所有车型信息
     * @return all
     */
    List<TCMCarType> selectAll();
}
