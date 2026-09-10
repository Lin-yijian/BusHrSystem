package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAbsenceDataRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceControlCenterLog;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmControlCenterLog;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.AttendanceCalcTrigger;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ucf.mdf.sample.service.TcmControlCenterLogService;
import com.yonyou.ucf.mdf.sample.utils.AttendanceCalcImportContext;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.ImportValidateUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.basic.constant.YpdEntityStatus;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


@BillPlugin(busiObj = "tcmControlCenterLog")
@Slf4j
public class TcmControlCenterLogPlugin extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;

    @Autowired
    private ImportValidateUtil importValidateUtil;

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private YmsOidGenerator ymsOidGenerator;

    @Autowired
    private TcmAttendanceCorrectionService attendanceCorrectionService;

    @Autowired
    private TcmControlCenterLogService service;

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
        TcmControlCenterLog data = (TcmControlCenterLog) billContext.getBillDO();
        String date = data.getDate();
        boolean existFlag = service.checkDataByDate(date);
        if (!existFlag) {
            // 更新核对记录
            tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_CONTROL_CENTER_LOG,false);
        }
        calcTrigger.collectSourceAndFlush(date, data.getEmployee_id());
        String id = data.getId();
        if (StringUtils.isNotBlank(id)) {
            attendanceCorrectionService.delTcmAttendanceControlCenterLog(id);
        }
    }

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
        String tenantId = InvocationInfoProxy.getTenantid();

        TcmControlCenterLog entity = (TcmControlCenterLog) billContext.getBillDO();
        log.info(entity.toString());
        if (null == entity.getEmployee_id()){
            String staffId = util.getStaffByCardId(entity.getWaybill_employee_id(), tenantId);
            entity.setEmployee_id(staffId);
        }
        String date = entity.getDate();
        if (!AttendanceCalcImportContext.isImporting()) {
            // 保存前校驗：該日期排更數據必須已導入
            tcmAttendanceCheckRecordService.validateScheduleDataImported(date);
            tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_CONTROL_CENTER_LOG,true);
        }

    }

    @Override
    public void afterSave(YpdBillContext billContext) throws Exception {
        super.afterSave(billContext);
//        TcmControlCenterLog entity = (TcmControlCenterLog) billContext.getBillDO();
//        if (entity == null) {
//            return;
//        }
//        if (AttendanceCalcImportContext.isBulkSyncing()) {
//            return;
//        }
//        calcTrigger.collectSourceAndFlush(entity.getDate(), entity.getEmployee_id());
        Object billDo = billContext.getBillDO();
        if (billDo instanceof TcmControlCenterLog) {
            TcmControlCenterLog data = (TcmControlCenterLog) billDo;
            String id = data.getId();
            String date = data.getDate();
            String employeeId = data.getEmployee_id();
            calcTrigger.collectSourceAndFlush(date, employeeId);
            boolean isInsert = data.getEntityStatus() == YpdEntityStatus.Insert;
            if (id != null && date != null && employeeId != null && isInsert) {
                List<TcmAttendanceCorrection> correctionList = attendanceCorrectionService.getByDateAndStaffId(date, employeeId);
                List<TcmAttendanceControlCenterLog> records = new ArrayList<>();
                for (TcmAttendanceCorrection correction : correctionList) {
                    TcmAttendanceControlCenterLog record = new TcmAttendanceControlCenterLog();
                    record.setForeignerKey(correction.getId());
                    record.setEntity(id);
                    records.add(record);
                }
                attendanceCorrectionService.saveTcmAttendanceControlCenterLog(records);
            }
        }
    }


    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        AttendanceCalcImportContext.beginImport();
        List<ImportSingleData> dataList = importBatchData.getData();

        ImportValidateUtil.ValidateContext ctx = importValidateUtil.collectAndQuery(
                dataList,
                TcmControlCenterLog.CAR_PLATE_NUMBER,
                TcmControlCenterLog.ROUTE_ROUTE_CODE,
                TcmControlCenterLog.WAYBILL_EMPLOYEE_ID
        );

        List<String> dateList = new ArrayList<>();
        List<String> employeeIdList = new ArrayList<>();
        for (ImportSingleData data : dataList) {
            importValidateUtil.validateRow(data, ctx,
                    TcmControlCenterLog.DRIVER_NAME,
                    TcmControlCenterLog.WAYBILL_EMPLOYEE_ID, TcmControlCenterLog.EMPLOYEE_ID,
                    TcmControlCenterLog.CAR_PLATE_NUMBER, TcmControlCenterLog.CAR,
                    TcmControlCenterLog.ROUTE_ROUTE_CODE, TcmControlCenterLog.ROUTE
            );
            String date = (String) data.getValueByKey("date");
            String employee_id = (String) data.getValueByKey("employee_id");
            dateList.add(date);
            employeeIdList.add(employee_id);
        }
        Set<String> dateSet = new HashSet<>(dateList);
        Set<String> importedDateSet = tcmAttendanceCheckRecordService.getImportedDateSet(dateSet);
        for (ImportSingleData data : dataList) {
            String date = (String) data.getValueByKey("date");
            if (!importedDateSet.contains(date)) {
                data.addImportError("当前日期的排更数据未导入，请先导入排更数据");
            }
        }
        tcmAttendanceCheckRecordService.updateOrSaveRecords(importedDateSet, ModuleEnum.TCM_CONTROL_CENTER_LOG, true);

//        try {
//            // 插入考勤修正
//            String tenantId = InvocationInfoProxy.getTenantid();
//            Map<String, String> map = attendanceCorrectionService.getByDateListAndStaffIds(dateList, employeeIdList);
//            List<TcmAttendanceControlCenterLog> insertList = new ArrayList<>();
//            if (!map.isEmpty()) {
//                for (ImportSingleData data : dataList) {
//                    String id = ymsOidGenerator.nextStrId();
//                    data.getData().put("id", id);
//                    String key = data.getValueByKey("date") + "_" + data.getValueByKey("employee_id");
//                    String s = map.get(key);
//                    if (!StringUtils.isEmpty(s)) {
//                        TcmAttendanceControlCenterLog centerLog = new TcmAttendanceControlCenterLog();
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
//            log.error("控制中心日志导入生成修正环节报错------->{}",e.getMessage());
//        }

    }
}
