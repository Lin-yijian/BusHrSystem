package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmControlCenterLog;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmSheduleShiftsData;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.*;
import com.yonyou.ucf.mdf.sample.utils.AttendanceCalcImportContext;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.basic.constant.YpdEntityStatus;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Autowired
    private TcmAttendanceCorrectionService attendanceCorrectionService;;

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);


        Object billDo = billContext.getBillDO();
        if (billDo instanceof TcmSheduleShiftsData) {
            TcmSheduleShiftsData data = (TcmSheduleShiftsData) billDo;

            if (AttendanceCalcImportContext.isBulkSyncing()) {
                return;
            }
            if (!AttendanceCalcImportContext.isImporting()) {
                tcmAttendanceCheckRecordService.updateOrSaveRecord(data.getDate(), ModuleEnum.TCM_SHEDULE_SHIFTS, true);
                attendanceCorrectionService.sheduleShiftsChangeCorrection(data);
            }
            AttendanceCalcImportContext.collectSchedule(data);
            calcTrigger.registerFlushAfterCommit();
        }
    }

    @Override
    public void afterDelete(YpdBillContext billContext) throws Exception {
        super.afterDelete(billContext);
        TcmSheduleShiftsData data = (TcmSheduleShiftsData) billContext.getBillDO();
        if (data == null) {
            return;
        }
        String date = data.getDate();
        // 刪除排更前置校驗：該日期若已有其他原始數據導入（考勤核對記錄中除排更外的標記為 true），則阻止刪除
        tcmAttendanceCheckRecordService.validateNoOtherRawDataImported(date);
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
        Set<String> dateSet = new HashSet<>();
        for (ImportSingleData data : dataList) {
            if (data.getData() != null) {
                String date = (String) data.getData().get("date");
                AttendanceCalcImportContext.collectDate(date);
                dateSet.add(date);
            }
        }
        tcmAttendanceCheckRecordService.updateOrSaveRecords(dateSet, ModuleEnum.TCM_SHEDULE_SHIFTS, true);
    }
}
