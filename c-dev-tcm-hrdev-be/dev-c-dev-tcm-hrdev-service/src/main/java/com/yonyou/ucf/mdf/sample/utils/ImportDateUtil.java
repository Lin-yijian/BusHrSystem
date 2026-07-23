package com.yonyou.ucf.mdf.sample.utils;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
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
        public Map<String, String> employeeCodeMap = Collections.emptyMap(); // 员工编号 → employeeId
    }

    /**
     * 第一步：从导入数据中收集工号，并批量查询
     */
    public ImportDateUtil.ValidateContext collectAndQuery(List<ImportSingleData> dataList,String cardIdKey,String employeeCodeKey) {

        Set<String> cardIds = new HashSet<>();
        Set<String> employeeCodes = new HashSet<>();


        for (ImportSingleData data : dataList) {

            collectValue(data, cardIdKey, cardIds);
            collectValue(data, employeeCodeKey, employeeCodes);
        }

        String tenantId = InvocationInfoProxy.getTenantid();
        ImportDateUtil.ValidateContext ctx = new ImportDateUtil.ValidateContext();

        ctx.cardMap = queryCards(cardIds, tenantId);
        ctx.employeeCodeMap = queryEmployeeCodes(employeeCodes,tenantId);
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
     * 批量查询员工（员工编号 → employeeId）
     */
    public Map<String, String> queryEmployeeCodes(Set<String> employeeCodes, String tenantId) {
        if (employeeCodes.isEmpty()) {
            return Collections.emptyMap();
        }

        return commonUtil.getStaffByCodes(new ArrayList<>(employeeCodes), tenantId);
    }

    /**
     * 通用逐行校验：姓名、工号→员工ID
     */
    public void validateRow(ImportSingleData data, ImportDateUtil.ValidateContext ctx,
                            String nameKey, String cardIdKey, String employeeIdKey) {
        // 姓名
        String name = (String) data.getValueByKey(nameKey);
        if (name == null || name.isEmpty()) {
//            throw new RuntimeException("數據中姓名為空，請檢查");
        }

        String employeeId = null;
        // 工号 → 员工ID
        String cardId = (String) data.getValueByKey(cardIdKey);
        //员工编号
        String employeeCode = (String) data.getValueByKey(employeeIdKey);


        if (cardId != null && !cardId.isEmpty()) {
            if (!ctx.cardMap.containsKey(cardId)) {
                throw new RuntimeException("工號【" + cardId + "】在員工檔案中不存在");
            }
            employeeId = ctx.cardMap.get(cardId);
        }
        // 如果没有澳巴工号，尝试使用员工编号查询
        else if (employeeCode != null && !employeeCode.isEmpty()) {
            if (!ctx.employeeCodeMap.containsKey(employeeCode)) {
                throw new RuntimeException("員工編號【" + employeeCode + "】在員工檔案中不存在");
            }
            employeeId = ctx.employeeCodeMap.get(employeeCode);
        }
        // 两者都为空，抛出异常
        else {
            throw new RuntimeException("數據中澳巴工號和員工編號都為空，請至少填寫一項");
        }

        // 如果查询到了员工ID，将其设置到数据中（这里的employeeIdKey是员工编码字段）
        if (employeeId != null) {
            data.getData().put(employeeIdKey, employeeId);
        }

    }

    private void collectValue(ImportSingleData data, String key, Set<String> target) {
        String value = (String) data.getValueByKey(key);
        if (value != null && !value.isEmpty()) {
            target.add(value);
        }
    }

}
