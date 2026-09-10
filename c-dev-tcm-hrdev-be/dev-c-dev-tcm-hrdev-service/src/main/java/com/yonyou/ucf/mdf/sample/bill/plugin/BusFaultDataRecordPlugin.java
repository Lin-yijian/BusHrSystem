package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ucf.mdf.sample.bill.entity.*;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.AttendanceCalcTrigger;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ucf.mdf.sample.service.TcmBusFaultDataRecordService;
import com.yonyou.ucf.mdf.sample.utils.AttendanceCalcImportContext;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.ImportDateUtil;
import com.yonyou.ucf.mdf.sample.utils.ImportValidateUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.basic.constant.YpdEntityStatus;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private TcmAttendanceCorrectionService attendanceCorrectionService;

    @Autowired
    private AttendanceCalcTrigger calcTrigger;


    @Override
    public void beforeDelete(YpdBillContext billContext) throws Exception {
        super.beforeDelete(billContext);
        AttendanceCalcImportContext.beginMergeFlush();
    }

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
        calcTrigger.collectSourceAndFlush(date, data.getEmployee_id());

        String id = data.getId();
        if (StringUtils.isNotBlank(id)) {
            attendanceCorrectionService.delTcmAttendanceBusFaultData(id);
        }
    }

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);

        TcmBusFaultDataRecord data = (TcmBusFaultDataRecord) billContext.getBillDO();
        String date = data.getReport_date();
        if (!AttendanceCalcImportContext.isImporting()) {
            // 保存前校驗：該日期排更數據必須已導入
            tcmAttendanceCheckRecordService.validateScheduleDataImported(date);
            tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_BUS_FAULT_DATA_RECORD,true);
        }

    }

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);

        Object billDo = billContext.getBillDO();
        if (billDo instanceof TcmBusFaultDataRecord) {
            TcmBusFaultDataRecord data = (TcmBusFaultDataRecord) billDo;
            if (AttendanceCalcImportContext.isBulkSyncing()) {
                return;
            }
            String id = data.getId();
            String date = data.getReport_date();
            String employeeId = data.getEmployee_id();
            calcTrigger.collectSourceAndFlush(date, employeeId);
            boolean isInsert = data.getEntityStatus() == YpdEntityStatus.Insert;
            if (id != null && date != null && employeeId != null && isInsert) {
                List<TcmAttendanceCorrection> correctionList = attendanceCorrectionService.getByDateAndStaffId(date, employeeId);
                List<TcmAttendanceBusFaultData> records = new ArrayList<>();
                for (TcmAttendanceCorrection correction : correctionList) {
                    TcmAttendanceBusFaultData record = new TcmAttendanceBusFaultData();
                    record.setForeignerKey(correction.getId());
                    record.setEntity(id);
                    records.add(record);
                }
                attendanceCorrectionService.saveTcmAttendanceBusFaultData(records);
            }
        }
    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        AttendanceCalcImportContext.beginImport();
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
            String reportDate = (String) data.getData().get(TcmBusFaultDataRecord.REPORT_DATE);
            dateSet.add(reportDate);
        }
        Set<String> importedDateSet = tcmAttendanceCheckRecordService.getImportedDateSet(dateSet);
        for (ImportSingleData data : dataList) {
            String reportDate = (String) data.getData().get(TcmBusFaultDataRecord.REPORT_DATE);
            if (!importedDateSet.contains(reportDate)) {
                data.addImportError("当前日期的排更数据未导入，请先导入排更数据");
            }
        }
        tcmAttendanceCheckRecordService.updateOrSaveRecords(importedDateSet, ModuleEnum.TCM_BUS_FAULT_DATA_RECORD, true);



    }
}
