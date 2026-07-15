package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmControlCenterLog;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
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


@BillPlugin(busiObj = "tcmControlCenterLog")
@Slf4j
public class TcmControlCenterLogPlugin extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;

    @Autowired
    private ImportValidateUtil importValidateUtil;

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
        String tenantId = InvocationInfoProxy.getTenantid();

        TcmControlCenterLog entity = (TcmControlCenterLog) billContext.getBillDO();
        log.info(entity.toString());
        if (null == entity.getEmployee_id()){
            String staffId = util.getStaffByCardId(entity.getWaybill_employee_id(), tenantId);
            entity.setEmployee_id(staffId);
        }

    }


    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        List<ImportSingleData> dataList = importBatchData.getData();

        ImportValidateUtil.ValidateContext ctx = importValidateUtil.collectAndQuery(
                dataList,
                TcmControlCenterLog.CAR_PLATE_NUMBER,
                TcmControlCenterLog.ROUTE_ROUTE_CODE,
                TcmControlCenterLog.WAYBILL_EMPLOYEE_ID
        );

        for (ImportSingleData data : dataList) {
            importValidateUtil.validateRow(data, ctx,
                    TcmControlCenterLog.DRIVER_NAME,
                    TcmControlCenterLog.WAYBILL_EMPLOYEE_ID, TcmControlCenterLog.EMPLOYEE_ID,
                    TcmControlCenterLog.CAR_PLATE_NUMBER, TcmControlCenterLog.CAR,
                    TcmControlCenterLog.ROUTE_ROUTE_CODE, TcmControlCenterLog.ROUTE
            );
        }
    }
}
