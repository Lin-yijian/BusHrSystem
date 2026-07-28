package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmBusFaultDataRecord;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.ImportDateUtil;
import com.yonyou.ucf.mdf.sample.utils.ImportValidateUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@BillPlugin(busiObj = "tcmBusFaultDataRecord")
@Slf4j
public class BusFaultDataRecord extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;

    @Autowired
    private ImportValidateUtil importValidateUtil;

    @Autowired
    private ImportDateUtil importDateUtil;

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        List<ImportSingleData> dataList = importBatchData.getData();

        ImportValidateUtil.ValidateContext ctx = importValidateUtil.collectAndQuery(
                dataList,
                TcmBusFaultDataRecord.CAR_PLATE_NUMBER,
                TcmBusFaultDataRecord.ROUTE_ROUTE_CODE,
                TcmBusFaultDataRecord.WAYBILL_EMPLOYEE_ID
        );

        for (ImportSingleData data : dataList) {
            importValidateUtil.validateRow(data, ctx,
                    TcmBusFaultDataRecord.CAPTAIN_NAME,
                    TcmBusFaultDataRecord.WAYBILL_EMPLOYEE_ID, TcmBusFaultDataRecord.EMPLOYEE_ID,
                    TcmBusFaultDataRecord.CAR_PLATE_NUMBER, TcmBusFaultDataRecord.CAR,
                    TcmBusFaultDataRecord.ROUTE_ROUTE_CODE, TcmBusFaultDataRecord.ROUTE
            );
        }
//
//        ImportValidateUtil.ValidateContext ctx = importDateUtil.collectAndQuerybd(
//                dataList,
//                "bus_code",
//                TcmBusFaultDataRecord.ROUTE_ROUTE_CODE,
//                TcmBusFaultDataRecord.WAYBILL_EMPLOYEE_ID
//        );
//
//        for (ImportSingleData data : dataList) {
//            importDateUtil.validateRowbd(data, ctx,
//                    TcmBusFaultDataRecord.CAPTAIN_NAME,
//                    TcmBusFaultDataRecord.WAYBILL_EMPLOYEE_ID, TcmBusFaultDataRecord.EMPLOYEE_ID,
//                    "bus_code", TcmBusFaultDataRecord.CAR,
//                    TcmBusFaultDataRecord.ROUTE_ROUTE_CODE, TcmBusFaultDataRecord.ROUTE
//            );
//        }


    }
}
