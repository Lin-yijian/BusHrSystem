package com.yonyou.ucf.mdf.sample.bill.plugin;


import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@BillPlugin(busiObj = "TCMcztraining")
@Slf4j
public class TCMcztrainingPlugin extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);

    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        List<ImportSingleData> dataList = importBatchData.getData();
        String tenantId = InvocationInfoProxy.getTenantid();

        if (dataList == null || dataList.isEmpty()) {
            throw new RuntimeException("导入数据为空");
        }


         List<String> wayBillIds  = new ArrayList<>();
         List<String> codeList = new ArrayList<>();



        for (ImportSingleData data : dataList) {
            wayBillIds.add((String) data.getData().get("waybill_employee_id"));
            codeList.add((String) data.getData().get("cz01_code"));
        }


        Map<String, String> staffByCardIds = util.getStaffByCardIds(wayBillIds, tenantId);
        Map<String, String> staffByCodes = util.getStaffByCodes(codeList,tenantId);

        for (ImportSingleData data : dataList) {

            String waybillEmployeeId = (String) data.getData().get("waybill_employee_id");
            String cz01Code = (String) data.getData().get("cz01_code");
            String employeeId = staffByCardIds.get(waybillEmployeeId);

            if (StringUtils.isEmpty(employeeId)){
                employeeId = staffByCodes.get(cz01Code);
            }
            if (StringUtils.isEmpty(employeeId)){
                data.addImportError("員工不存在，澳巴工號：【"+waybillEmployeeId+"】");
//              throw new RuntimeException("員工不存在，澳巴工號：【"+waybillEmployeeId+"】");

            }

            data.getData().put("cz01",employeeId);
        }



    }
}
