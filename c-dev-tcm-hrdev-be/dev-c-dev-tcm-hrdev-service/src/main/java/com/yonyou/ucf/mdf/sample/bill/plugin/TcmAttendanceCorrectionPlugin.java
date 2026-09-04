package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverHolidayWorkRecord;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.service.TcmDriverHolidayWorkRecordService;
import com.yonyou.ucf.mdf.sample.utils.AttendanceCalcImportContext;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.BillQueryContext;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QuerySchema;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@BillPlugin(busiObj = "tcmAttendanceCorrection")
@Slf4j
public class TcmAttendanceCorrectionPlugin extends AbstractBillPlugin {


    @Autowired
    private TcmDriverHolidayWorkRecordService service;

    @Override
    public void beforeBillListQuery(BillQueryContext billContext, QuerySchema querySchema) throws Exception {
        super.beforeBillListQuery(billContext, querySchema);
//        if ("bocInfoCash".equals(billContext.getBaseBillContext().getServiceCode())){
//            querySchema.appendQueryCondition(QueryCondition.name("terminal_id").is_null());
//        }
    }


    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);

        Object billDo = billContext.getBillDO();
        if (billDo instanceof TcmAttendanceCorrection) {
            TcmAttendanceCorrection data = (TcmAttendanceCorrection) billContext.getBillDO();
            if (data == null) {
                return;
            }
            String date = data.getDate();
            String waybillEmployeeId = data.getWaybill_employee_id();
            String isWeekendWork = data.getIs_working_on_weekly_holiday();
            boolean isWeekend = isWeekendWork != null && isWeekendWork.equals(BaseEnum.YES.getCode());
            List<TcmDriverHolidayWorkRecord> recordList = service.getByDateAndWaybillEmployeeId(date, waybillEmployeeId);
            for (TcmDriverHolidayWorkRecord record : recordList) {
                record.setIs_weekend_work(isWeekend);
            }

        }

    }


}
