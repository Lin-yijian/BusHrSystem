package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmReserveCaptainAttendanceRecord;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.AttendanceCalcTrigger;
import com.yonyou.ucf.mdf.sample.service.AttendanceCorrectionSyncService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmReserveCaptainAttendanceRecordService;
import com.yonyou.ucf.mdf.sample.utils.AttendanceCalcImportContext;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@BillPlugin(busiObj = "tcmReserveCaptainAttendanceRecord")
@Slf4j
public class TcmReserveCaptainAttendanceRecordPlugin extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;

    @Autowired
    private TcmReserveCaptainAttendanceRecordService service;

    @Autowired
    private TcmAttendanceCheckRecordService tcmAttendanceCheckRecordService;

    @Autowired
    private AttendanceCorrectionSyncService correctionSyncService;

    @Autowired
    private AttendanceCalcTrigger calcTrigger;

    @Override
    public void afterDelete(YpdBillContext billContext) throws Exception {
        super.afterDelete(billContext);
        TcmReserveCaptainAttendanceRecord data = (TcmReserveCaptainAttendanceRecord) billContext.getBillDO();
        String date = data.getDate();
        boolean existFlag = service.checkDataByDate(date);
        if (!existFlag) {
            tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_RESERVE_CAPTAIN_ATTENDANCE_RECORD, false);
        }
        calcTrigger.afterDataChanged(date, data.getDriver_no());
    }

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
        TcmReserveCaptainAttendanceRecord data = (TcmReserveCaptainAttendanceRecord) billContext.getBillDO();
        if (!AttendanceCalcImportContext.isImporting()) {
            tcmAttendanceCheckRecordService.updateOrSaveRecord(data.getDate(), ModuleEnum.TCM_RESERVE_CAPTAIN_ATTENDANCE_RECORD, true);
        }
    }

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);
        TcmReserveCaptainAttendanceRecord data = (TcmReserveCaptainAttendanceRecord) billContext.getBillDO();
        if (data == null) {
            return;
        }
        if (AttendanceCalcImportContext.isImporting()) {
            AttendanceCalcImportContext.collectReserve(data);
            calcTrigger.registerImportFlushAfterCommit();
            return;
        }
        correctionSyncService.syncFromReserve(data);
        calcTrigger.afterDataChanged(data.getDate(), data.getDriver_no());
    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        AttendanceCalcImportContext.beginImport();
        List<ImportSingleData> dataList = importBatchData.getData();
        String tenantId = InvocationInfoProxy.getTenantid();

        List<String> wayBillIds = new ArrayList<>();
        for (ImportSingleData data : dataList) {
            wayBillIds.add((String) data.getData().get("waybill_employee_id"));
            AttendanceCalcImportContext.collectDate((String) data.getData().get("date"));
        }

        Map<String, String> staffByCardIds = util.getStaffByCardIds(wayBillIds, tenantId);

        Set<String> dateSet = new HashSet<>();
        for (ImportSingleData data : dataList) {
            String waybill_employee_id = (String) data.getData().get("waybill_employee_id");
            String employee_id = staffByCardIds.get(waybill_employee_id);
            if (StringUtils.isEmpty(employee_id)) {
                data.addImportError("員工不存在，澳巴工號：【" + waybill_employee_id + "】");
            }
            data.getData().put("driver_no", employee_id);
            dateSet.add((String) data.getData().get("date"));
        }
        tcmAttendanceCheckRecordService.updateOrSaveRecords(dateSet, ModuleEnum.TCM_RESERVE_CAPTAIN_ATTENDANCE_RECORD, true);
    }
}
