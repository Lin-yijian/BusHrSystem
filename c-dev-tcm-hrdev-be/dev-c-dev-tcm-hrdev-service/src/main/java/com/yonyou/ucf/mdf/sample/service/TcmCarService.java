package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.TCMCar;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface TcmCarService {


    /**
     * 根据车牌号分组
     * @param busPlateNumberList 车牌号集合
     * @return k->车牌号
     */
    Map<String, TCMCar> groupCarMapByPlateNumber(Collection<String> busPlateNumberList);

    /**
     * 根据内部编号分组
     * @param busInternalNoList 内部编号集合
     * @return k->内部编号
     */
    Map<String,TCMCar> groupCarMapByInternalNo(Collection<String> busInternalNoList);


}
