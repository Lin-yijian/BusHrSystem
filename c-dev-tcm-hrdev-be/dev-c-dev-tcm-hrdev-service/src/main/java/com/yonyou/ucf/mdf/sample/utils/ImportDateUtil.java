package com.yonyou.ucf.mdf.sample.utils;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class ImportDateUtil {

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 校验上下文：缓存批量查询结果
     */
    public static class ValidateContext {

        public Map<String, String> cardMap = Collections.emptyMap(); // cardId → employeeId
    }

    /**
     * 第一步：从导入数据中收集工号，并批量查询
     */
    public ImportDateUtil.ValidateContext collectAndQuery(List<ImportSingleData> dataList,String cardIdKey) {

        Set<String> cardIds = new HashSet<>();


        for (ImportSingleData data : dataList) {

            collectValue(data, cardIdKey, cardIds);
        }

        String tenantId = InvocationInfoProxy.getTenantid();
        ImportDateUtil.ValidateContext ctx = new ImportDateUtil.ValidateContext();

        ctx.cardMap = queryCards(cardIds, tenantId);
        return ctx;
    }


    /**
     * 批量查询员工（澳巴工号 → staffId）
     */
    public Map<String, String> queryCards(Set<String> cardIds, String tenantId) {
        if (cardIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commonUtil.getStaffByCardIds(new ArrayList<>(cardIds), tenantId);
    }

    /**
     * 通用逐行校验：姓名、工号→员工ID
     */
    public void validateRow(ImportSingleData data, ImportDateUtil.ValidateContext ctx,
                            String nameKey, String cardIdKey, String employeeIdKey) {
        // 姓名
        String name = (String) data.getValueByKey(nameKey);
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("數據中姓名為空，請檢查");
        }
        // 工号 → 员工ID
        String cardId = (String) data.getValueByKey(cardIdKey);
        if (cardId != null && !cardId.isEmpty()) {
            if (!ctx.cardMap.containsKey(cardId)) {
                throw new RuntimeException("工號【" + cardId + "】在員工檔案中不存在");
            }
            data.getData().put(employeeIdKey, ctx.cardMap.get(cardId));
        } else {
            throw new RuntimeException("數據中澳巴工號為空，請檢查");
        }

    }

    private void collectValue(ImportSingleData data, String key, Set<String> target) {
        String value = (String) data.getValueByKey(key);
        if (value != null && !value.isEmpty()) {
            target.add(value);
        }
    }
    //
}
