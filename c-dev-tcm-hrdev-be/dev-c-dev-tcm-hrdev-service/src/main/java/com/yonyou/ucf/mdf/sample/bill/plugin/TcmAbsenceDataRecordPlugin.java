package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAbsenceDataRecord;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@BillPlugin(busiObj = "tcmAbsenceDataRecord")
@Slf4j
public class TcmAbsenceDataRecordPlugin extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
        String tenantId = InvocationInfoProxy.getTenantid();

        TcmAbsenceDataRecord entity = (TcmAbsenceDataRecord) billContext.getBillDO();
        log.info(entity.toString());
        if (null == entity.getEmployee_id()){
            String staffId = util.getStaffByCardId(entity.getWaybill_employee_id(), tenantId);
            entity.setEmployee_id(staffId);
        }

    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        List<ImportSingleData> dataList = importBatchData.getData();
        for (ImportSingleData importSingleData : dataList) {

        }

    }
}
