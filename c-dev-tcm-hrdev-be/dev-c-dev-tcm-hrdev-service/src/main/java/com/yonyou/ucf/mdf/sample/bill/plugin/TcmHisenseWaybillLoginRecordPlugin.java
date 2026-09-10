package com.yonyou.ucf.mdf.sample.bill.plugin;


import com.yonyou.ucf.mdf.sample.bill.entity.TcmHisenseAttendanceData;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmHisenseWaybillLoginRecord;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ucf.mdf.sample.service.TcmHiSenseWaybillLoginRecordService;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@BillPlugin(busiObj = "tcmHisenseWaybillLoginRecord")
@Slf4j
public class TcmHisenseWaybillLoginRecordPlugin extends AbstractBillPlugin {

    @Autowired
    private TcmAttendanceCheckRecordService tcmAttendanceCheckRecordService;

    @Autowired
    private TcmHiSenseWaybillLoginRecordService service;

    @Autowired
    private TcmAttendanceCorrectionService attendanceCorrectionService;


    @Override
    public void afterDelete(YpdBillContext billContext) throws Exception {
        super.afterDelete(billContext);

        Object billDo = billContext.getBillDO();
        if (billDo instanceof TcmHisenseWaybillLoginRecord) {
            TcmHisenseWaybillLoginRecord data = (TcmHisenseWaybillLoginRecord) billDo;
            String date = data.getDate();
            if (date != null) {
                boolean existFlag = service.checkDataByDate(date);
                if (!existFlag) {
                    tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_ATTENDANCE_CHECK_RECORD, false);
                }
            }
            String id = data.getId();
            if (StringUtils.isNotBlank(id)) {
                attendanceCorrectionService.delTcmAttendanceHisenseWaybillLogin(id);
            }
        }
    }


}
