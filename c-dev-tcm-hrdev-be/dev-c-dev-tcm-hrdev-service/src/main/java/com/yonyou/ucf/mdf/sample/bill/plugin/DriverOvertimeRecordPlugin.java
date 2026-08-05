package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceControlCenterLog;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceDriverOvertimeRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmDriverOvertimeRecord;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ucf.mdf.sample.service.TcmDriverOvertimeRecordService;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.ImportEventExtendProcessor;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.YpdBillContext;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@BillPlugin(busiObj = "tcmDriverOvertimeRecord")
@Slf4j
public class DriverOvertimeRecordPlugin extends AbstractBillPlugin {

    @Autowired
    private CommonUtil util;


    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private YmsOidGenerator ymsOidGenerator;

    @Autowired
    private TcmAttendanceCorrectionService attendanceCorrectionService;

    @Autowired
    private TcmDriverOvertimeRecordService service;

    @Autowired
    private TcmAttendanceCheckRecordService tcmAttendanceCheckRecordService;



    @Override
    public void afterDelete(YpdBillContext billContext) throws Exception {
        super.afterDelete(billContext);
        TcmDriverOvertimeRecord data = (TcmDriverOvertimeRecord) billContext.getBillDO();
        String date = data.getOvertime_date();
        boolean existFlag = service.checkDataByDate(date);
        if (!existFlag) {
            // 更新核对记录
            tcmAttendanceCheckRecordService.updateOrSaveRecord(date, ModuleEnum.TCM_DRIVER_OVERTIME,false);
        }
    }

    @Override
    public void beforeSave(YpdBillContext billContext) throws Exception {
        super.beforeSave(billContext);
        String tenantId = InvocationInfoProxy.getTenantid();

        TcmDriverOvertimeRecord entity = (TcmDriverOvertimeRecord) billContext.getBillDO();
        log.info(entity.toString());
        if (null == entity.getEmployee_id()){
            String staffId = util.getStaffByCardId(entity.getWaybill_employee_id(), tenantId);
            entity.setEmployee_id(staffId);
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

        List<String> dateList = new ArrayList<>();
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
            String overtime_date = (String) data.getData().get("overtime_date");
            dateList.add(overtime_date);

        }

        try {
            // 插入考勤修正
            Map<String, String> map = attendanceCorrectionService.getByDateListAndStaffIds(dateList, employeeIdList);
            List<TcmAttendanceDriverOvertimeRecord> insertList = new ArrayList<>();
            if (!map.isEmpty()) {
                for (ImportSingleData data : dataList) {
                    String id = ymsOidGenerator.nextStrId();
                    data.getData().put("id", id);
                    String key = data.getValueByKey("overtime_date") + "_" + data.getValueByKey("employee_id");
                    String s = map.get(key);
                    if (!StringUtils.isEmpty(s)) {
                        TcmAttendanceDriverOvertimeRecord centerLog = new TcmAttendanceDriverOvertimeRecord();
                        centerLog.setEntity(id);
                        centerLog.setForeignerKey(s);
                        centerLog.setYtenantId(tenantId);
                        insertList.add(centerLog);
                    }

                }
                if (!insertList.isEmpty()) {
                    billRepository.insert(insertList);
                }
            }
        } catch (Exception e) {
            log.error("车长超时工作导入生成修正环节报错------->{}",e.getMessage());
        }

    }
}
