package com.yonyou.ucf.mdf.sample.bill.plugin;


import com.yonyou.ucf.mdf.sample.bill.entity.TcmFuelGasDataRecord;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.ImportDateUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@BillPlugin(busiObj = "tcmFuelGasDataRecord")
public class TcmFuelGasDataRecordPlugin extends AbstractBillPlugin {
    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ImportDateUtil importDateUtil;


    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        List<ImportSingleData> dataList = importBatchData.getData();



        // 收集 + 批量查询员工
        // 注意：这里需要同时传入澳巴工号字段和员工编号字段
        ImportDateUtil.ValidateContext ctx = importDateUtil.collectAndQuery(
                dataList,TcmFuelGasDataRecord.WAYBILL_EMPLOYEE_ID,TcmFuelGasDataRecord.EMPLOYEE_ID
        );

        // 逐行校验
        for (ImportSingleData data : dataList) {
            importDateUtil.validateRow(data, ctx,
                    TcmFuelGasDataRecord.NAME,
                    TcmFuelGasDataRecord.WAYBILL_EMPLOYEE_ID,
                    TcmFuelGasDataRecord.EMPLOYEE_ID
                    );

        }

    }
}
