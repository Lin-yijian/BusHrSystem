package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.BusinessException;
import com.yonyou.ucf.mdd.api.interfaces.rpc.IComOperateApi;
import com.yonyou.ucf.mdd.ext.support.utils.RemoteInvockTools;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.basic.entity.IBillDO;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import com.yonyou.ypd.bill.utils.IrisRpcUtil;

@BillPlugin(busiObj = "XSDVehicle")
public class CarInfoPlugin extends AbstractBillPlugin {


    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);

        System.out.println("测试123");
        System.out.println(billContext.toString());
        IBillDO billDO = billContext.getBillDO();

        System.out.println(billDO.toString());

        throw new BusinessException("100","报错了");

//        IComOperateApi comOperateApi =  IrisRpcUtil.getIrisReference(UrlEnum.CAR_INFO_HISTORY.getDomainKey(), IComOperateApi.class);
//        comOperateApi.insert(UrlEnum.CAR_INFO_HISTORY.getUrl(), bill);
//                comOperateApi.update(DomainEnum.AGREEMENT.getUri(), updateInfo);
    }
}
