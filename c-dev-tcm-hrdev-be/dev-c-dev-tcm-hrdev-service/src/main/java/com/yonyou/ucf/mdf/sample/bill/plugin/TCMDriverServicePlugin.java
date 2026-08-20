package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMCar;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMDriverService;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMRoute;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMSerivceIndex;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.ImportValidateUtil;
import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.ImportEventExtendProcessor;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 司机服务记录插件
 * @author aze
 * @date
 */
@BillPlugin(busiObj = "TCMDriverService")
public class TCMDriverServicePlugin extends AbstractBillPlugin {

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ImportValidateUtil importValidateUtil;

    public void beforeTranslate(ImportBatchData importBatchData, ImportEventBO importEventBO) {
        List<ImportSingleData> dataList = importBatchData.getData();

        // 收集 + 批量查询车辆、路线、员工
        ImportValidateUtil.ValidateContext ctx = importValidateUtil.collectAndQuery(
                dataList,
                TCMDriverService.CAR_PLATE_NUMBER,
                TCMDriverService.ROUTE_ROUTE_CODE,
                TCMDriverService.WAYBILL_EMPLOYEE_ID
        );

        // 收集指标编码 + 批量查询指标
        Set<String> indexCodes = new HashSet<>();
        for (ImportSingleData data : dataList) {
            String code = (String) data.getValueByKey(TCMDriverService.INDEX_NO_INDEX_CODE);
            if (code != null && !code.isEmpty()) {
                indexCodes.add(code);
            }
        }
        Map<Object, TCMSerivceIndex> indexMap = Collections.emptyMap();
        if (!indexCodes.isEmpty()) {
            LambdaQueryCondition<TCMSerivceIndex> condition = new LambdaQueryCondition<>(new TCMSerivceIndex());
            condition.in(TCMSerivceIndex::getIndex_code, new ArrayList<>(indexCodes))
                    .eq(TCMSerivceIndex::getYtenantId, InvocationInfoProxy.getTenantid())
                    .eq(TCMSerivceIndex::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
            indexMap = billRepository.queryByClause(condition).stream()
                    .collect(Collectors.toMap(TCMSerivceIndex::getIndex_code, r -> r, (a, b) -> a));
        }

        // 逐行校验
        for (ImportSingleData data : dataList) {
            importValidateUtil.validateRow(data, ctx,
                    TCMDriverService.DIVER_NAME,
                    TCMDriverService.WAYBILL_EMPLOYEE_ID, TCMDriverService.EMPLOYEE_ID,
                    TCMDriverService.CAR_PLATE_NUMBER, TCMDriverService.CAR,
                    TCMDriverService.ROUTE_ROUTE_CODE, TCMDriverService.ROUTE
            );
            // 指标校验
            String indexCode = (String) data.getValueByKey(TCMDriverService.INDEX_NO_INDEX_CODE);
            if (indexCode != null && !indexCode.isEmpty()) {
                if (!indexMap.containsKey(indexCode)) {
                    data.addImportError("指標編號在指標檔案中不存在");
//                    throw new RuntimeException("指標編號【" + indexCode + "】在指標檔案中不存在");
                } else {
                    data.getData().put(TCMDriverService.INDEX_NO, indexMap.get(indexCode).getId());
                    data.getData().put(TCMDriverService.INDEX_DESC, indexMap.get(indexCode).getContent());
                }
            }
        }
    }
}