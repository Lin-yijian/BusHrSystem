package com.yonyou.ucf.mdf.sample.bill.plugin;


import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAbsenceDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmFuelGasDataRecord;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.AttendanceCalcTrigger;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmFuelGasDataRecordService;
import com.yonyou.ucf.mdf.sample.utils.AttendanceCalcImportContext;
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
        TcmFuelGasDataRecord data = (TcmFuelGasDataRecord) billContext.getBillDO();
        String date = data.getAttendance_date();
        boolean existFlag = service.checkDataByDate(date);
        if (!existFlag) {
            // 更新核对记录
            tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_FUEL_GAS_DATA_RECORD,false);
        }
        calcTrigger.collectSourceAndFlush(date, data.getEmployee_id());
    }


    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
        String tenantId = InvocationInfoProxy.getTenantid();

        TcmFuelGasDataRecord entity = (TcmFuelGasDataRecord) billContext.getBillDO();
        if (!AttendanceCalcImportContext.isImporting()) {
            // 保存前校驗：該日期排更數據必須已導入
            tcmAttendanceCheckRecordService.validateScheduleDataImported(entity.getAttendance_date());
            tcmAttendanceCheckRecordService.updateOrSaveRecord(entity.getAttendance_date(), ModuleEnum.TCM_FUEL_GAS_DATA_RECORD,true);
        }

    }

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);
        TcmFuelGasDataRecord entity = (TcmFuelGasDataRecord) billContext.getBillDO();
        if (entity == null) {
            return;
        }
        if (AttendanceCalcImportContext.isBulkSyncing()) {
            return;
        }
        calcTrigger.collectSourceAndFlush(entity.getAttendance_date(), entity.getEmployee_id());
    }


    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        AttendanceCalcImportContext.beginImport();
        List<ImportSingleData> dataList = importBatchData.getData();



        // 收集 + 批量查询员工
        // 注意：这里需要同时传入澳巴工号字段和员工编号字段
        ImportDateUtil.ValidateContext ctx = importDateUtil.collectAndQuery(
                dataList,TcmFuelGasDataRecord.WAYBILL_EMPLOYEE_ID,TcmFuelGasDataRecord.EMPLOYEE_ID
        );

        // 逐行校验
        Set<String> dateSet = new HashSet<>();
        for (ImportSingleData data : dataList) {
            importDateUtil.validateRow(data, ctx,
                    TcmFuelGasDataRecord.NAME,
                    TcmFuelGasDataRecord.WAYBILL_EMPLOYEE_ID,
                    TcmFuelGasDataRecord.EMPLOYEE_ID
                    );
            String attendanceDate = (String) data.getData().get(TcmFuelGasDataRecord.ATTENDANCE_DATE);
            dateSet.add(attendanceDate);
        }
        Set<String> importedDateSet = tcmAttendanceCheckRecordService.getImportedDateSet(dateSet);
        for (ImportSingleData data : dataList) {
            String attendanceDate = (String) data.getData().get(TcmFuelGasDataRecord.ATTENDANCE_DATE);
            if (!importedDateSet.contains(attendanceDate)) {
                data.addImportError("当前日期的排更数据未导入，请先导入排更数据");
            }
        }
        tcmAttendanceCheckRecordService.updateOrSaveRecords(importedDateSet, ModuleEnum.TCM_FUEL_GAS_DATA_RECORD, true);

    }
}
