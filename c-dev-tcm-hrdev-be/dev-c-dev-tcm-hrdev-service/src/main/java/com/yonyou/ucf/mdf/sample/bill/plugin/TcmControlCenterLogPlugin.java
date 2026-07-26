package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceControlCenterLog;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmControlCenterLog;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCorrectionService;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.ImportValidateUtil;
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

    }


    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
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

        try {
            // 插入考勤修正
            String tenantId = InvocationInfoProxy.getTenantid();
            Map<String, String> map = attendanceCorrectionService.getByDateListAndStaffIds(dateList, employeeIdList);
            List<TcmAttendanceControlCenterLog> insertList = new ArrayList<>();
            if (!map.isEmpty()) {
                for (ImportSingleData data : dataList) {
                    String id = ymsOidGenerator.nextStrId();
                    data.getData().put("id", id);
                    String key = data.getValueByKey("date") + "_" + data.getValueByKey("employee_id");
                    String s = map.get(key);
                    if (!StringUtils.isEmpty(s)) {
                        TcmAttendanceControlCenterLog centerLog = new TcmAttendanceControlCenterLog();
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
            log.error("控制中心日志导入生成修正环节报错------->{}",e.getMessage());
        }

    }
}
