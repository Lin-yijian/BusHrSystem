package com.yonyou.ucf.mdf.sample.bill.plugin;


import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAbsenceDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmFuelGasDataRecord;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmFuelGasDataRecordService;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.ImportDateUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
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

    @Autowired
    private TcmFuelGasDataRecordService service;

    @Autowired
    private TcmAttendanceCheckRecordService tcmAttendanceCheckRecordService;


    @Override
    public void afterDelete(YpdBillContext billContext) throws Exception {
        super.afterDelete(billContext);
        TcmFuelGasDataRecord data = (TcmFuelGasDataRecord) billContext.getBillDO();
        String date = data.getAttendance_date();
        boolean existFlag = service.checkDataByDate(date);
        if (!existFlag) {
            // 更新核对记录
            tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_ABSENCE_DATA_RECORD,false);
        }
    }


    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
        String tenantId = InvocationInfoProxy.getTenantid();

        TcmFuelGasDataRecord entity = (TcmFuelGasDataRecord) billContext.getBillDO();
        tcmAttendanceCheckRecordService.updateOrSaveRecord(entity.getAttendance_date(), ModuleEnum.TCM_ABSENCE_DATA_RECORD,true);

    }


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
