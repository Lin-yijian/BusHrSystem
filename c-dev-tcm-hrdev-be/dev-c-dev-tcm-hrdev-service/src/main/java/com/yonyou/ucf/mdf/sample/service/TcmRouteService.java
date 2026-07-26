package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.TCMRoute;

import java.util.List;
import java.util.Map;

public interface TcmRouteService {

    /**
     * 获取所有路线数据
     * @return .
     */
    List<TCMRoute> listAll();

    /**
     * 获取所有路线编号
     * @return .
     */
    List<String> listAllCode();

    /**
     * 获取所有路线编号信息
     * @return k->路线编号  v-> 路线信息
     */
    Map<String,TCMRoute> getMap();
}
