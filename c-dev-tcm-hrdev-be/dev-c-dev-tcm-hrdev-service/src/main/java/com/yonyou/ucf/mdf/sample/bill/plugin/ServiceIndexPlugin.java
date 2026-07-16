package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ucf.mdf.sample.bill.entity.TCMCarHistory;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMSerivceIndex;
import com.yonyou.ucf.mdf.sample.enums.IndexType;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;

@BillPlugin(busiObj = "TCMSerivceIndex")
@Slf4j
public class ServiceIndexPlugin extends AbstractBillPlugin {


    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
        TCMSerivceIndex entity = (TCMSerivceIndex) billContext.getBillDO();
        log.info(entity.toString());
        if (null == entity.getId()){
            if (null == entity.getType()) {
                // 类型为空则默认为服务奖指标
                entity.setType(IndexType.SERVICE_INDEX.getCode());
            }
        }


    }
}
