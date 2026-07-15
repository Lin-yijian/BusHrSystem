package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ucf.mdf.sample.bill.entity.TCMTrafficAccident;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.ImportValidateUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 交通意外記錄插件
 * @author aze
 * @date
 */
@BillPlugin(busiObj = "TCMTrafficAccident")
public class TCMTrafficAccidentPlugin extends AbstractBillPlugin {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ImportValidateUtil importValidateUtil;

    public void processBeforeImport(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        List<ImportSingleData> dataList = importBatchData.getData();

        ImportValidateUtil.ValidateContext ctx = importValidateUtil.collectAndQuery(
                dataList,
                TCMTrafficAccident.CAR_PLATE_NUMBER,
                TCMTrafficAccident.ROUTE_ROUTE_CODE,
                TCMTrafficAccident.WAYBILL_EMPLOYEE_ID
        );

        for (ImportSingleData data : dataList) {
            importValidateUtil.validateRow(data, ctx,
                    TCMTrafficAccident.DRIVER_NAME,
                    TCMTrafficAccident.WAYBILL_EMPLOYEE_ID, TCMTrafficAccident.EMPLOYEE_ID,
                    TCMTrafficAccident.CAR_PLATE_NUMBER, TCMTrafficAccident.CAR,
                    TCMTrafficAccident.ROUTE_ROUTE_CODE, TCMTrafficAccident.ROUTE
            );
        }
    }
}