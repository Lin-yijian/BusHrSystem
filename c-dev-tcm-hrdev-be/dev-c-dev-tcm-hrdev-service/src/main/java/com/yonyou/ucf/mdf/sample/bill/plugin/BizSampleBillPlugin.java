package com.yonyou.ucf.mdf.sample.bill.plugin;


import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;

/**
 * 业务单据插件样例
 * @author wuzunqian
 * @date 2021/6/18 9:32 AM
 */
//@BillPlugin(busiObj = "xxx")
public class BizSampleBillPlugin extends AbstractBillPlugin {

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);
    }

}
