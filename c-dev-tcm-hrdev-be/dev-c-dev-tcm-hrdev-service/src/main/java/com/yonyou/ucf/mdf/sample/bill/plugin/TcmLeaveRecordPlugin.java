package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceAbsenceDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmHisenseAttendanceData;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmLeaveRecord;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
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
import java.util.List;
import java.util.Map;


@BillPlugin(busiObj = "tcmLeaveRecord")
@Slf4j
public class TcmLeaveRecordPlugin extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;

    @Autowired
    private TcmAttendanceCorrectionService correctionService;

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
        Object billDo = billContext.getBillDO();
        if (billDo instanceof TcmLeaveRecord) {
            TcmLeaveRecord data = (TcmLeaveRecord) billDo;
            String id = data.getId();
            String date = data.getStart_date();
            String employeeId = data.getEmployee_id();
            if (id != null && date != null && employeeId != null) {
                List<TcmAttendanceCorrection> correctionList = correctionService.getByDateAndStaffId(date, employeeId);
                List<TcmAttendanceAbsenceDataRecord> records = new ArrayList<>();
                for (TcmAttendanceCorrection correction : correctionList) {
                    TcmAttendanceAbsenceDataRecord record = new TcmAttendanceAbsenceDataRecord();
                    record.setForeignerKey(correction.getId());
                    record.setEntity(id);
                    records.add(record);
                }
                correctionService.saveTcmAttendanceAbsenceDataRecord(records);
            }
        }

    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        List<ImportSingleData> dataList = importBatchData.getData();
        String tenantId = InvocationInfoProxy.getTenantid();

        List<String> wayBillIds  = new ArrayList<>();
        for (ImportSingleData data : dataList) {
            wayBillIds.add((String) data.getData().get("waybill_employee_id"));
        }

        Map<String, String> staffByCardIds = util.getStaffByCardIds(wayBillIds, tenantId);

        for (ImportSingleData data : dataList) {

            String waybill_employee_id = (String) data.getData().get("waybill_employee_id");
            String employee_id = staffByCardIds.get(waybill_employee_id);
            if (StringUtils.isEmpty(employee_id)){
                data.addImportError("員工不存在，澳巴工號：【"+waybill_employee_id+"】");
//                throw new RuntimeException("員工不存在，澳巴工號：【"+waybill_employee_id+"】");
            }
            data.getData().put("employee_id",employee_id);
        }

    }
}
