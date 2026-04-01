package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.BusinessException;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.basic.entity.IBillDO;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;

@BillPlugin(busiObj = "TCMSafety")
@Slf4j
public class SafetyPlugin extends AbstractBillPlugin {

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);


        System.out.println("测试123");
        System.out.println(billContext.toString());
        IBillDO billDO = billContext.getBillDO();

        System.out.println(billDO.toString());
        log.info("测试打印日志");
        log.info(billDO.toString());
        log.info("------------------");
        throw new RuntimeException("测试报错");

    }
}
