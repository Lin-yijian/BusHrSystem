package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmBusFaultDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverOvertimeRecord;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.ImportEventExtendProcessor;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@BillPlugin(busiObj = "tcmBusFaultDataRecord")
@Slf4j
public class BusFaultDataRecord extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
//        String tenantId = InvocationInfoProxy.getTenantid();

//        TcmBusFaultDataRecord entity = (TcmBusFaultDataRecord) billContext.getBillDO();
//        log.info(entity.toString());
//        if (null == entity.getDriver_no()){
//            String staffId = util.getStaffByCardId(entity.getWaybill_employee_id(), tenantId);
//            entity.setDriver_no(staffId);
//        }

    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        List<ImportSingleData> dataList = importBatchData.getData();
        List<String> ids = new ArrayList<>();

        for (ImportSingleData data : dataList) {
            TcmBusFaultDataRecord record = (TcmBusFaultDataRecord) data.getData();
            if (null == record.getWaybill_employee_id()){
                throw new RuntimeException("澳巴工号不能为空");
            }
            ids.add(record.getWaybill_employee_id());
        }
        String tenantId = InvocationInfoProxy.getTenantid();
        Map<String, String> staffMap = util.getStaffByCardIds(ids, tenantId);

        for (ImportSingleData data : dataList) {
            Map<String, Object> dataMap = data.getData();
            String id = (String) dataMap.get("waybill_employee_id");
            dataMap.put("driver_no",staffMap.get(id));
        }


    }

}
