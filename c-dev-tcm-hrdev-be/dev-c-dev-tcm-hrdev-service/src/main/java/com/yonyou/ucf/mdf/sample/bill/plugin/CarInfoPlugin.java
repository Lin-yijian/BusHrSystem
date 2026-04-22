package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.BusinessException;
import com.yonyou.ucf.mdd.api.interfaces.rpc.IComOperateApi;
import com.yonyou.ucf.mdd.ext.support.utils.RemoteInvockTools;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMCar;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMCarHistory;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.basic.entity.IBillDO;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import com.yonyou.ypd.bill.utils.IrisRpcUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@BillPlugin(busiObj = "TCMSafety")
@Slf4j
public class CarInfoPlugin extends AbstractBillPlugin {


    @Autowired
    private IBillRepository billRepository;


    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
    }

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);

        log.info(billContext.toString());
        TCMCar car = (TCMCar) billContext.getBillDO();
        log.info(car.toString());
        if (car.getId() != null){
            TCMCarHistory history = changeVo(car);
            billRepository.insertBillDO(history);
            log.info("成功插入歷史記錄");
        }


    }

    private TCMCarHistory changeVo(TCMCar car){
        TCMCarHistory history = new TCMCarHistory();
        history.setCar_type(car.getCar_type());
        history.setInternal_no(car.getInternal_no());
        history.setPlate_number(car.getPlate_number());
        history.setRun_type(car.getRun_type());
        history.setSource(car.getSource());
        history.setParent_id(car.getId());
        return history;
    }
}
