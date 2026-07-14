package com.yonyou.ucf.mdf.sample.bill.plugin;


import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.ImportEventExtendProcessor;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 司机服务记录插件
 * @author aze
 * @date
 */
@BillPlugin(busiObj = "TCMDriverService")
public class TCMDriverServicePlugin extends AbstractBillPlugin implements ImportEventExtendProcessor {

    @Override
    public void beforeTranslate(@NotNull ImportBatchData importBatchData, @NotNull ImportEventBO importEventBO) {
        // 获取导入数据
        List<ImportSingleData> dataList = importBatchData.getData();
        for (ImportSingleData importSingleData : dataList) {

        }

        ImportEventExtendProcessor.super.beforeTranslate(importBatchData, importEventBO);
    }

}
