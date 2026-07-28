package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmBusFaultDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmFuelGasDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmReserveCaptainAttendanceRecord;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@BillPlugin(busiObj = "tcmReserveCaptainAttendanceRecord")
@Slf4j
public class TcmReserveCaptainAttendanceRecordPlugin extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;

    @Autowired
    private ImportDateUtil importDateUtil;



    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);

    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        List<ImportSingleData> dataList = importBatchData.getData();
        String tenantId = InvocationInfoProxy.getTenantid();

        List<String> wayBillIds  = new ArrayList<>();
        for (ImportSingleData data : dataList) {
            wayBillIds.add((String) data.getData().get("waybill_employee_id"));
        }

        Map<String, String> staffByCardIds = util.getStaffByCardIds(wayBillIds, tenantId);

        for (ImportSingleData data : dataList) {

            String waybill_employee_id = (String) data.getData().get("waybill_employee_id");
            String employee_id = staffByCardIds.get(waybill_employee_id);
            if (StringUtils.isEmpty(employee_id)){
                throw new RuntimeException("員工不存在，澳巴工號：【"+waybill_employee_id+"】");
            }
            data.getData().put("driver_no",employee_id);
        }



        // 收集 + 批量查询员工
        // 注意：这里需要同时传入澳巴工号字段和员工编号字段
        ImportDateUtil.ValidateContext ctx = importDateUtil.collectAndQuery(
                dataList,TcmReserveCaptainAttendanceRecord.WAYBILL_EMPLOYEE_ID,TcmReserveCaptainAttendanceRecord.DRIVER_NO
        );

        // 逐行校验
        for (ImportSingleData data : dataList) {
            importDateUtil.validateRow(data, ctx,
                    TcmReserveCaptainAttendanceRecord.NAME,
                    TcmReserveCaptainAttendanceRecord.WAYBILL_EMPLOYEE_ID,
                    TcmReserveCaptainAttendanceRecord.DRIVER_NO
            );

        }



    }

}
