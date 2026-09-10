package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmVehicleDutyRecord;
import com.yonyou.ucf.mdf.sample.service.AttendanceCalcTrigger;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
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
import java.util.List;
import java.util.Map;

@BillPlugin(busiObj = "tcmVehicleDutyRecord")
@Slf4j
public class TcmVehicleDutyRecordPlugin extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;

    @Autowired
    private TcmAttendanceCorrectionService correctionService;

    @Autowired
    private AttendanceCalcTrigger calcTrigger;

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);

    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        AttendanceCalcImportContext.beginImport();
        List<ImportSingleData> dataList = importBatchData.getData();
        String tenantId = InvocationInfoProxy.getTenantid();

        List<String> wayBillIds  = new ArrayList<>();
        for (ImportSingleData data : dataList) {
            wayBillIds.add((String) data.getData().get("waybill_employee_id"));
            if (data.getData() != null) {
                AttendanceCalcImportContext.collectDate((String) data.getData().get("date"));
            }
        }

        Map<String, String> staffByCardIds = util.getStaffByCardIds(wayBillIds, tenantId);

        for (ImportSingleData data : dataList) {

            String waybill_employee_id = (String) data.getData().get("waybill_employee_id");
            String employee_id = staffByCardIds.get(waybill_employee_id);
            if (StringUtils.isEmpty(employee_id)){
                data.addImportError("員工不存在，澳巴工號：【"+waybill_employee_id+"】");
            }
            data.getData().put("employee_id",employee_id);
        }

    }

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);
        Object billDo = billContext.getBillDO();
        if (!(billDo instanceof TcmVehicleDutyRecord)) {
            return;
        }
        TcmVehicleDutyRecord data = (TcmVehicleDutyRecord) billDo;
        if (AttendanceCalcImportContext.isBulkSyncing()) {
            return;
        }
        String date = data.getDate();
        String employeeId = data.getEmployee_id();
        Integer times = data.getVehicle_exchange_count();
        if (StringUtils.isBlank(date) || StringUtils.isBlank(employeeId) || times == null) {
            return;
        }
        List<TcmAttendanceCorrection> correctionList = correctionService.getByDateAndStaffId(date, employeeId);
        if (correctionList != null && !correctionList.isEmpty()) {
            for (TcmAttendanceCorrection correction : correctionList) {
                correction.setVehicle_handover_count(times);
            }
            correctionService.updateList(correctionList);
        } else {
            log.warn("交取车次数回写跳过：无对应修正记录, date={}, employeeId={}", date, employeeId);
        }
        calcTrigger.afterDataChanged(date, employeeId);
    }
}
