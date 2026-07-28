package com.yonyou.ucf.mdf.sample.utils;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMCar;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMRoute;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportSingleData;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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





//    /**
//     * 第一步：从导入数据中收集车牌、路线、工号，并批量查询
//     * Internal_no  busCodeKey
//     */
//    public ImportValidateUtil.ValidateContext collectAndQuerybd(List<ImportSingleData> dataList,
//                                                              String busCodeKey,
//                                                              String routeCodeKey,
//                                                              String cardIdKey) {
//        Set<String> busCodes = new HashSet<>();
//        Set<String> routeCodes = new HashSet<>();
//        Set<String> cardIds = new HashSet<>();
//
//
//        for (ImportSingleData data : dataList) {
//            collectValue(data, busCodeKey, busCodes);
//            collectValue(data, routeCodeKey, routeCodes);
//            collectValue(data, cardIdKey, cardIds);
//        }
//
//        String tenantId = InvocationInfoProxy.getTenantid();
//        ImportValidateUtil.ValidateContext ctx = new ImportValidateUtil.ValidateContext();
//        ctx.carMap = queryCarsbd(busCodes, tenantId);
//        ctx.routeMap = queryRoutes(routeCodes, tenantId);
//        ctx.cardMap = queryCards(cardIds, tenantId);
//        return ctx;
//    }
//
//
//    /**
//     * 通用逐行校验：姓名、工号→员工ID、巴士編號(内部編號)、路线
//     */
//    public void validateRowbd(ImportSingleData data, ImportValidateUtil.ValidateContext ctx,
//                            String nameKey,
//                            String cardIdKey, String employeeIdKey,
//                            String busCodeKey, String carIdKey,
//                            String routeCodeKey, String routeIdKey) {
//        // 姓名
//        String name = (String) data.getValueByKey(nameKey);
//        if (name == null || name.isEmpty()) {
//            throw new RuntimeException("數據中姓名為空，請檢查");
//        }
//        // 工号 → 员工ID
//        String cardId = (String) data.getValueByKey(cardIdKey);
//        if (cardId != null && !cardId.isEmpty()) {
//            if (!ctx.cardMap.containsKey(cardId)) {
//                throw new RuntimeException("工號【" + cardId + "】在員工檔案中不存在");
//            }
//            data.getData().put(employeeIdKey, ctx.cardMap.get(cardId));
//        } else {
//            throw new RuntimeException("數據中澳巴工號為空，請檢查");
//        }
//        // 车辆（通過内部編號來查詢）busCodeNum
//        String busCodeNum = (String) data.getValueByKey(busCodeKey);
//        if (busCodeNum != null && !busCodeNum.isEmpty()) {
//            if (!ctx.carMap.containsKey(busCodeNum)) {
//                throw new RuntimeException("巴士編號【" + busCodeNum + "】在車輛檔案中不存在");
//            }
//            data.getData().put(carIdKey, ctx.carMap.get(busCodeNum).getId());
//        }
//        // 路线
//        String routeCode = (String) data.getValueByKey(routeCodeKey);
//        if (routeCode != null && !routeCode.isEmpty()) {
//            if (!ctx.routeMap.containsKey(routeCode)) {
//                throw new RuntimeException("路線編號【" + routeCode + "】在路線檔案中不存在");
//            }
//            data.getData().put(routeIdKey, ctx.routeMap.get(routeCode).getId());
//        }
//    }
//
//
//    public Map<String, TCMCar> queryCarsbd(Set<String> busCodes, String tenantId) {
//        if (busCodes.isEmpty()) {
//            return Collections.emptyMap();
//        }
//        LambdaQueryCondition<TCMCar> condition = new LambdaQueryCondition<>(new TCMCar());
//        condition.in(TCMCar::getInternal_no, new ArrayList<>(busCodes))
//                .eq(TCMCar::getYtenantId, tenantId)
//                .eq(TCMCar::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
//        return billRepository.queryByClause(condition).stream()
//                .collect(Collectors.toMap(TCMCar::getInternal_no, r -> r, (a, b) -> a));
//    }
//    /**
//     * 批量查询路线
//     */
//    public Map<String, TCMRoute> queryRoutes(Set<String> routeCodes, String tenantId) {
//        if (routeCodes.isEmpty()) {
//            return Collections.emptyMap();
//        }
//        LambdaQueryCondition<TCMRoute> condition = new LambdaQueryCondition<>(new TCMRoute());
//        condition.in(TCMRoute::getRoute_code, new ArrayList<>(routeCodes))
//                .eq(TCMRoute::getYtenantId, tenantId)
//                .eq(TCMRoute::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
//        return billRepository.queryByClause(condition).stream()
//                .collect(Collectors.toMap(TCMRoute::getRoute_code, r -> r, (a, b) -> a));
//    }




}
