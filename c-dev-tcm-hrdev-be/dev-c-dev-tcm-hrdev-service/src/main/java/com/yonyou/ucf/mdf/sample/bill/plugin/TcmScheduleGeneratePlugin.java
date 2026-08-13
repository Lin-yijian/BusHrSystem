package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmSheduleShiftsData;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.*;
import com.yonyou.ucf.mdf.sample.utils.AttendanceCalcImportContext;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@BillPlugin(busiObj = "tcmSheduleShiftsData")
@Slf4j
public class TcmScheduleGeneratePlugin extends AbstractBillPlugin {

    @Autowired
    private TcmScheduleGenerateService service;

    @Autowired
    private TcmAttendanceCheckRecordService tcmAttendanceCheckRecordService;

    @Autowired
    private AttendanceCorrectionSyncService correctionSyncService;

    @Autowired
    private AttendanceCalcTrigger calcTrigger;

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);
        TcmSheduleShiftsData data = (TcmSheduleShiftsData) billContext.getBillDO();
        if (data == null) {
            return;
        }
        if (AttendanceCalcImportContext.isImporting()) {
            AttendanceCalcImportContext.collectSchedule(data);
            tcmAttendanceCheckRecordService.updateOrSaveRecord(data.getDate(), ModuleEnum.TCM_SHEDULE_SHIFTS, true);
            calcTrigger.registerImportFlushAfterCommit();
            return;
        }
        correctionSyncService.syncFromSchedule(data);
        tcmAttendanceCheckRecordService.updateOrSaveRecord(data.getDate(), ModuleEnum.TCM_SHEDULE_SHIFTS, true);
        calcTrigger.afterDataChanged(data.getDate(), data.getEmployee_id());
    }

    @Override
    public void afterDelete(YpdBillContext billContext) throws Exception {
        super.afterDelete(billContext);
        TcmSheduleShiftsData data = (TcmSheduleShiftsData) billContext.getBillDO();
        if (data == null) {
            return;
        }
        String date = data.getDate();
        boolean existFlag = service.checkDataByDate(date);
        if (!existFlag) {
            tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_SHEDULE_SHIFTS, false);
        }
        correctionSyncService.afterScheduleDeleted(data);
        calcTrigger.afterDataChanged(date, data.getEmployee_id());
    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        AttendanceCalcImportContext.beginImport();
        List<ImportSingleData> dataList = importBatchData.getData();
        if (dataList == null) {
            return;
        }
        for (ImportSingleData data : dataList) {
            if (data.getData() != null) {
                AttendanceCalcImportContext.collectDate((String) data.getData().get("date"));
            }
        }
    }
}
