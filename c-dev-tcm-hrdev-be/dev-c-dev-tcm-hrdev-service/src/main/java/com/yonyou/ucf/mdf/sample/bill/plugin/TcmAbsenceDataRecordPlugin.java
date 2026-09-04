package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAbsenceDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceAbsenceDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceDriverOvertimeRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverOvertimeRecord;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.AttendanceCalcTrigger;
import com.yonyou.ucf.mdf.sample.service.TcmAbsenceDataRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ucf.mdf.sample.utils.AttendanceCalcImportContext;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


@BillPlugin(busiObj = "tcmAbsenceDataRecord")
@Slf4j
public class TcmAbsenceDataRecordPlugin extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private YmsOidGenerator ymsOidGenerator;

    @Autowired
    private TcmAttendanceCorrectionService attendanceCorrectionService;

    @Autowired
    private TcmAbsenceDataRecordService service;


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
        TcmAbsenceDataRecord data = (TcmAbsenceDataRecord) billContext.getBillDO();
        String date = data.getDate();
        boolean existFlag = service.checkDataByDate(date);
        if (!existFlag) {
            // 更新核对记录
            tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_ABSENCE_DATA_RECORD,false);
        }
        calcTrigger.collectSourceAndFlush(date, data.getEmployee_id());
    }

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
        String tenantId = InvocationInfoProxy.getTenantid();

        TcmAbsenceDataRecord entity = (TcmAbsenceDataRecord) billContext.getBillDO();
        log.info(entity.toString());
        if (null == entity.getEmployee_id()){
            String staffId = util.getStaffByCardId(entity.getWaybill_employee_id(), tenantId);
            entity.setEmployee_id(staffId);
        }
        if (!AttendanceCalcImportContext.isImporting()) {
            tcmAttendanceCheckRecordService.updateOrSaveRecord(entity.getDate(), ModuleEnum.TCM_ABSENCE_DATA_RECORD, true);
        }
    }

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);
        TcmAbsenceDataRecord entity = (TcmAbsenceDataRecord) billContext.getBillDO();
        if (entity == null) {
            return;
        }
        if (AttendanceCalcImportContext.isBulkSyncing()) {
            return;
        }
        calcTrigger.collectSourceAndFlush(entity.getDate(), entity.getEmployee_id());
    }

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        AttendanceCalcImportContext.beginImport();
        List<ImportSingleData> dataList = importBatchData.getData();
        String tenantId = InvocationInfoProxy.getTenantid();


        List<String> wayBillIds  = new ArrayList<>();
        for (ImportSingleData data : dataList) {
            wayBillIds.add((String) data.getData().get("waybill_employee_id"));
        }

        Map<String, String> staffByCardIds = util.getStaffByCardIds(wayBillIds, tenantId);

        Set<String> dateList = new HashSet<>();
        List<String> employeeIdList = new ArrayList<>();
        for (ImportSingleData data : dataList) {

            String waybill_employee_id = (String) data.getData().get("waybill_employee_id");
            String employee_id = staffByCardIds.get(waybill_employee_id);
            if (StringUtils.isEmpty(employee_id)){
                data.addImportError("員工不存在，澳巴工號：【"+waybill_employee_id+"】");
//                throw new RuntimeException("員工不存在，澳巴工號：【"+waybill_employee_id+"】");
            }
            data.getData().put("employee_id",employee_id);
            employeeIdList.add(employee_id);
            String overtime_date = (String) data.getData().get("date");
            dateList.add(overtime_date);
        }

        tcmAttendanceCheckRecordService.updateOrSaveRecords(dateList, ModuleEnum.TCM_ABSENCE_DATA_RECORD,true);


//        try {
//            // 插入考勤修正
//            Map<String, String> map = attendanceCorrectionService.getByDateListAndStaffIds(dateList, employeeIdList);
//            List<TcmAttendanceAbsenceDataRecord> insertList = new ArrayList<>();
//            if (!map.isEmpty()) {
//                for (ImportSingleData data : dataList) {
//                    String id = ymsOidGenerator.nextStrId();
//                    data.getData().put("id", id);
//                    String key = data.getValueByKey("date") + "_" + data.getValueByKey("employee_id");
//                    String s = map.get(key);
//                    if (!StringUtils.isEmpty(s)) {
//                        TcmAttendanceAbsenceDataRecord centerLog = new TcmAttendanceAbsenceDataRecord();
//                        centerLog.setEntity(id);
//                        centerLog.setForeignerKey(s);
//                        centerLog.setYtenantId(tenantId);
//                        insertList.add(centerLog);
//                    }
//
//                }
//                if (!insertList.isEmpty()) {
//                    billRepository.insert(insertList);
//                }
//            }
//        } catch (Exception e) {
//            log.error("缺勤记录导入生成修正环节报错------->{}",e.getMessage());
//        }

    }
}
