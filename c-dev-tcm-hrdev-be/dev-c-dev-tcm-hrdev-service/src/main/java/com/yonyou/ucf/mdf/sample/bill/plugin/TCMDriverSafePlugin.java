package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMDriverSafe;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMDriverService;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMSafety;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMSerivceIndex;
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

//        // 收集指标编码 + 批量查询指标
//        Set<String> indexCodes = new HashSet<>();
//        for (ImportSingleData data : dataList) {
//            String code = (String) data.getValueByKey(TCMDriverSafe.INDEX_NO_INDEX_CODE);
//            if (code != null && !code.isEmpty()) {
//                indexCodes.add(code);
//            }
//        }
//        Map<Object, TCMSafety> indexMap = Collections.emptyMap();
//        if (!indexCodes.isEmpty()) {
//            LambdaQueryCondition<TCMSafety> condition = new LambdaQueryCondition<>(new TCMSafety());
//            condition.in(TCMSafety::getIndex_code, new ArrayList<>(indexCodes))
//                    .eq(TCMSafety::getYtenantId, InvocationInfoProxy.getTenantid())
//                    .eq(TCMSafety::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
//            indexMap = billRepository.queryByClause(condition).stream()
//                    .collect(Collectors.toMap(TCMSafety::getIndex_code, r -> r, (a, b) -> a));
//        }
//
//        // 逐行校验
//        for (ImportSingleData data : dataList) {
//            importValidateUtil.validateRow(data, ctx,
//                    TCMDriverSafe.DIVER_NAME,
//                    TCMDriverSafe.WAYBILL_EMPLOYEE_ID, TCMDriverSafe.EMPLOYEE_ID,
//                    TCMDriverSafe.CAR_PLATE_NUMBER, TCMDriverSafe.CAR,
//                    TCMDriverSafe.ROUTE_ROUTE_CODE, TCMDriverSafe.ROUTE
//            );
//            // 指标校验
//            String indexCode = (String) data.getValueByKey(TCMDriverSafe.INDEX_NO_INDEX_CODE);
//            if (indexCode != null && !indexCode.isEmpty()) {
//                if (!indexMap.containsKey(indexCode)) {
//                    data.addImportError("指標編號【" + indexCode + "】在指標檔案中不存在");
////                    throw new RuntimeException("指標編號【" + indexCode + "】在指標檔案中不存在");
//                }
//                data.getData().put(TCMDriverSafe.INDEX_NO, indexMap.get(indexCode).getId());
//                data.getData().put(TCMDriverSafe.INDEX_DESC, indexMap.get(indexCode).getContent());
//            }
//        }

        // 收集指标编码 + 批量查询指标（转成 Integer，与实体 index_code 类型一致）
        Set<Integer> indexCodes = new HashSet<>();
        Map<ImportSingleData, String> invalidIndexRows = new LinkedHashMap<>();
        for (ImportSingleData data : dataList) {
            String codeStr = (String) data.getValueByKey(TCMDriverService.INDEX_NO_INDEX_CODE);
            if (codeStr != null && !codeStr.isEmpty()) {
                try {
                    indexCodes.add(Integer.parseInt(codeStr.trim()));
                } catch (NumberFormatException e) {
                    invalidIndexRows.put(data, codeStr);
                }
            }
        }
        Map<Integer, TCMSerivceIndex> indexMap = Collections.emptyMap();
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
            // 指标校验（使用 Integer key 匹配 Map）
            String indexCodeStr = (String) data.getValueByKey(TCMDriverService.INDEX_NO_INDEX_CODE);
            if (indexCodeStr != null && !indexCodeStr.isEmpty()) {
                if (invalidIndexRows.containsKey(data)) {
                    data.addImportError("指標編號【" + invalidIndexRows.get(data) + "】不是有效的整數");
                } else {
                    Integer indexCode = Integer.parseInt(indexCodeStr.trim());
                    if (!indexMap.containsKey(indexCode)) {
                        data.addImportError("指標編號【" + indexCode + "】在指標檔案中不存在");
//                        throw new RuntimeException("指標編號【" + indexCode + "】在指標檔案中不存在");
                    } else {
                        data.getData().put(TCMDriverService.INDEX_NO, indexMap.get(indexCode).getId());
                        data.getData().put(TCMDriverService.INDEX_DESC, indexMap.get(indexCode).getContent());
                    }
                }
            }
        }
    }
}