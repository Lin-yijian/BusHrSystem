package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMDriverSafe;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMSafety;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.ImportValidateUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 司机安全记录插件
 * @author aze
 * @date
 */
@BillPlugin(busiObj = "TCMDriverSafe")
public class TCMDriverSafePlugin extends AbstractBillPlugin {

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ImportValidateUtil importValidateUtil;

    public void processBeforeImport(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        List<ImportSingleData> dataList = importBatchData.getData();

        // 收集 + 批量查询车辆、路线、员工
        ImportValidateUtil.ValidateContext ctx = importValidateUtil.collectAndQuery(
                dataList,
                TCMDriverSafe.CAR_PLATE_NUMBER,
                TCMDriverSafe.ROUTE_ROUTE_CODE,
                TCMDriverSafe.WAYBILL_EMPLOYEE_ID
        );

        // 收集指标编码 + 批量查询指标
        Set<String> indexCodes = new HashSet<>();
        for (ImportSingleData data : dataList) {
            String code = (String) data.getValueByKey(TCMDriverSafe.INDEX_NO_INDEX_CODE);
            if (code != null && !code.isEmpty()) {
                indexCodes.add(code);
            }
        }
        Map<Object, TCMSafety> indexMap = Collections.emptyMap();
        if (!indexCodes.isEmpty()) {
            LambdaQueryCondition<TCMSafety> condition = new LambdaQueryCondition<>(new TCMSafety());
            condition.in(TCMSafety::getIndex_code, new ArrayList<>(indexCodes))
                    .eq(TCMSafety::getYtenantId, InvocationInfoProxy.getTenantid())
                    .eq(TCMSafety::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
            indexMap = billRepository.queryByClause(condition).stream()
                    .collect(Collectors.toMap(TCMSafety::getIndex_code, r -> r, (a, b) -> a));
        }

        // 逐行校验
        for (ImportSingleData data : dataList) {
            importValidateUtil.validateRow(data, ctx,
                    TCMDriverSafe.DIVER_NAME,
                    TCMDriverSafe.WAYBILL_EMPLOYEE_ID, TCMDriverSafe.EMPLOYEE_ID,
                    TCMDriverSafe.CAR_PLATE_NUMBER, TCMDriverSafe.CAR,
                    TCMDriverSafe.ROUTE_ROUTE_CODE, TCMDriverSafe.ROUTE
            );
            // 指标校验
            String indexCode = (String) data.getValueByKey(TCMDriverSafe.INDEX_NO_INDEX_CODE);
            if (indexCode != null && !indexCode.isEmpty()) {
                if (!indexMap.containsKey(indexCode)) {
                    throw new RuntimeException("指標編號【" + indexCode + "】在指標檔案中不存在");
                }
                data.getData().put(TCMDriverSafe.INDEX_NO, indexMap.get(indexCode).getId());
                data.getData().put(TCMDriverSafe.INDEX_DESC, indexMap.get(indexCode).getContent());
            }
        }
    }
}