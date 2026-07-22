package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.TCMCarType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface TcmCarTypeService {

    /**
     * 获取所有车型信息
     * @return all
     */
    List<TCMCarType> selectAll();

    /**
     * 根据指定字段分组数据
     * @param fieldGetter 分组依据
     * @return 。
     */
    Map<String,TCMCarType> mapByFieldGetter(Function<TCMCarType, String> fieldGetter);
}
