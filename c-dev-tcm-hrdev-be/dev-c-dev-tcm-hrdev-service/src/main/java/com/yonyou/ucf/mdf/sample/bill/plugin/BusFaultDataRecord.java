package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;

@BillPlugin(busiObj = "TCMSafety")
@Slf4j
public class BusFaultDataRecord extends AbstractBillPlugin {

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
    }
}
