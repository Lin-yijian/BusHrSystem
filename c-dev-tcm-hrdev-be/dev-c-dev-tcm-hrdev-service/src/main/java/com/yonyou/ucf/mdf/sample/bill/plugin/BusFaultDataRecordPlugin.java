package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmBusFaultDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverOvertimeRecord;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.AttendanceCorrectionSyncService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmBusFaultDataRecordService;
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
public class BusFaultDataRecordPlugin extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;

    @Autowired
    private ImportValidateUtil importValidateUtil;

    @Autowired
    private ImportDateUtil importDateUtil;

    @Autowired
    private TcmBusFaultDataRecordService service;



    @Autowired
    private TcmAttendanceCheckRecordService tcmAttendanceCheckRecordService;

    @Autowired
    private AttendanceCorrectionSyncService correctionSyncService;


    @Override
    public void afterDelete(YpdBillContext billContext) throws Exception {
        super.afterDelete(billContext);
        TcmBusFaultDataRecord data = (TcmBusFaultDataRecord) billContext.getBillDO();
        String date = data.getReport_date();
        boolean existFlag = service.checkDataByDate(date);
        if (!existFlag) {
            // 更新核对记录
            tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_BUS_FAULT_DATA_RECORD,false);
        }
        correctionSyncService.syncExceptionSources(date, data.getEmployee_id());
    }

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);

        TcmBusFaultDataRecord data = (TcmBusFaultDataRecord) billContext.getBillDO();
        String date = data.getReport_date();
        tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_BUS_FAULT_DATA_RECORD,true);

    }

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);
        TcmBusFaultDataRecord data = (TcmBusFaultDataRecord) billContext.getBillDO();
        if (data == null) {
            return;
        }
        correctionSyncService.syncExceptionSources(data.getReport_date(), data.getEmployee_id());
    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        List<ImportSingleData> dataList = importBatchData.getData();

        ImportValidateUtil.ValidateContext ctx = importValidateUtil.collectAndQuery(
                dataList,
                TcmBusFaultDataRecord.CAR_PLATE_NUMBER,
                TcmBusFaultDataRecord.ROUTE_ROUTE_CODE,
                TcmBusFaultDataRecord.WAYBILL_EMPLOYEE_ID
        );

        Set<String> dateSet = new HashSet<>();
        for (ImportSingleData data : dataList) {
            importValidateUtil.validateRow(data, ctx,
                    TcmBusFaultDataRecord.CAPTAIN_NAME,
                    TcmBusFaultDataRecord.WAYBILL_EMPLOYEE_ID, TcmBusFaultDataRecord.EMPLOYEE_ID,
                    TcmBusFaultDataRecord.CAR_PLATE_NUMBER, TcmBusFaultDataRecord.CAR,
                    TcmBusFaultDataRecord.ROUTE_ROUTE_CODE, TcmBusFaultDataRecord.ROUTE
            );
            dateSet.add((String) data.getData().get(TcmBusFaultDataRecord.REPORT_DATE));
        }
        tcmAttendanceCheckRecordService.updateOrSaveRecords(dateSet, ModuleEnum.TCM_BUS_FAULT_DATA_RECORD, true);
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
