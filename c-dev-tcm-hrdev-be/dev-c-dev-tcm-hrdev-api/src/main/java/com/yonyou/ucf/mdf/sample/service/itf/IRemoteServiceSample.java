package com.yonyou.ucf.mdf.sample.service.itf;

import com.yonyou.cloud.middleware.rpc.RemoteCall;

/**
 *  rpc服务注册和引用方式:
 *  1.提供者: @RemoteCall("应用名称@租户id") 注册中心中查找
 *  2.消费者: 使用Spring注入, 如:
 *         @Autowired
 *         private IRemoteServiceSample remoteServiceSample;
 *
 */
//@RemoteCall("xxx")
public interface IRemoteServiceSample {

    public Object hello();

}
