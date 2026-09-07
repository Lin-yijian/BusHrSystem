package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverHolidayWorkRecord;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.AttendanceCalcTrigger;
import com.yonyou.ucf.mdf.sample.service.AttendanceCorrectionSyncService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ucf.mdf.sample.utils.AttendanceCalcImportContext;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.ImportDateUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.basic.constant.YpdEntityStatus;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@BillPlugin(busiObj = "tcmDriverHolidayWorkRecord")
public class TcmDriverHolidayWorkRecordPlugin extends AbstractBillPlugin {
    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ImportDateUtil importDateUtil;

    @Autowired
    private AttendanceCorrectionSyncService correctionSyncService;

    @Autowired
    private TcmAttendanceCorrectionService correctionService;

    @Autowired
    private AttendanceCalcTrigger calcTrigger;

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);

        Object billDo = billContext.getBillDO();
        if (billDo instanceof TcmDriverHolidayWorkRecord) {
            TcmDriverHolidayWorkRecord data = (TcmDriverHolidayWorkRecord) billContext.getBillDO();
            if (data == null) {
                return;
            }
            String date = data.getDate();
            String employeeId = data.getEmployee_id();
            Boolean isWeekendWork = data.getIs_weekend_work();
            String isWeekendWorkStr = isWeekendWork ? BaseEnum.YES.getCode() : BaseEnum.NO.getCode();
            boolean isUpdate = data.getEntityStatus() == YpdEntityStatus.Update;
            if (isUpdate){
                List<TcmAttendanceCorrection> correctionList = correctionService.getByDateAndStaffId(date, employeeId);
                for (TcmAttendanceCorrection correction : correctionList) {
                    correction.setIs_working_on_weekly_holiday(isWeekendWorkStr);
                    if (isWeekendWork) {
                        correction.setIs_forced_leave_work(BaseEnum.NO.getCode());
                    }
                }
                correctionService.saveList(correctionList);
            }
            if (AttendanceCalcImportContext.isBulkSyncing()) {
                return;
            }
            if (AttendanceCalcImportContext.isImporting()) {
                AttendanceCalcImportContext.collectHoliday(data);
                calcTrigger.registerImportFlushAfterCommit();
                return;
            }
            correctionSyncService.syncFromHolidayWork(data);
            calcTrigger.afterDataChanged(data.getDate(), data.getEmployee_id());
        }

    }

    @Override
    public void afterDelete(YpdBillContext billContext) throws Exception {
        super.afterDelete(billContext);
        TcmDriverHolidayWorkRecord data = (TcmDriverHolidayWorkRecord) billContext.getBillDO();
        if (data != null) {
            calcTrigger.afterDataChanged(data.getDate(), data.getEmployee_id());
        }
    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        AttendanceCalcImportContext.beginImport();
        List<ImportSingleData> dataList = importBatchData.getData();

        // 收集 + 批量查询员工
        ImportDateUtil.ValidateContext ctx = importDateUtil.collectAndQuery(
                dataList, TcmDriverHolidayWorkRecord.WAYBILL_EMPLOYEE_ID, TcmDriverHolidayWorkRecord.EMPLOYEE_ID
        );

        // 逐行校验
        for (ImportSingleData data : dataList) {
            importDateUtil.validateRow(data, ctx,
                    TcmDriverHolidayWorkRecord.DRIVER_NAME,
                    TcmDriverHolidayWorkRecord.WAYBILL_EMPLOYEE_ID,
                    TcmDriverHolidayWorkRecord.EMPLOYEE_ID
            );
            if (data.getData() != null) {
                AttendanceCalcImportContext.collectDate((String) data.getData().get("date"));
            }
        }
    }
}
