package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmHisenseAttendanceData;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.AttendanceCalcTrigger;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmHiSenseAttendanceService;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@BillPlugin(busiObj = "tcmHisenseAttendanceData")
@Slf4j
public class TcmHisenseAttendanceDataPlugin extends AbstractBillPlugin {

    @Autowired
    private TcmAttendanceCheckRecordService tcmAttendanceCheckRecordService;

    @Autowired
    private TcmHiSenseAttendanceService service;

    @Autowired
    private AttendanceCalcTrigger calcTrigger;

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);

        Object billDo = billContext.getBillDO();
        if (billDo instanceof TcmHisenseAttendanceData) {
            TcmHisenseAttendanceData data = (TcmHisenseAttendanceData) billDo;
            String date = data.getRun_date();
            if (date != null) {
                service.refreshCorrectionFromLocalData(date, data.getEmployee_id());
                calcTrigger.afterDataChanged(date, data.getEmployee_id());
            }
        }

    }

    @Override
    public void afterDelete(YpdBillContext billContext) throws Exception {
        super.afterDelete(billContext);
        TcmHisenseAttendanceData data = (TcmHisenseAttendanceData) billContext.getBillDO();
        String date = data.getRun_date();
        boolean existFlag = service.checkDataByDate(date);
        if (!existFlag) {
            tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_HI_SENSE_ATTENDANCE, false);
        }
//        service.refreshCorrectionFromLocalData(date, data.getEmployee_id());
//        calcTrigger.afterDataChanged(date, data.getEmployee_id());
    }
}
