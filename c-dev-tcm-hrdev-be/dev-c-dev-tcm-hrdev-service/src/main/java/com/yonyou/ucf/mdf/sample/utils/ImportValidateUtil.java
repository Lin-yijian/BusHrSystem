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

/**
 * 导入数据校验工具类 - 封装收集、批量查询、逐行校验的通用逻辑
 *
 * @author aze
 * @date
 */
@Component
public class ImportValidateUtil {

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 校验上下文：缓存批量查询结果
     */
    public static class ValidateContext {
//        public Map<String, TCMCar> carMap = Collections.emptyMap();
//        public Map<String, TCMRoute> routeMap = Collections.emptyMap();
        public Map<String, String> cardMap = Collections.emptyMap(); // cardId → employeeId



    }

    /**
     * 第一步：从导入数据中收集车牌、路线、工号，并批量查询
     */
    public ValidateContext collectAndQuery(List<ImportSingleData> dataList,
                                           String carPlateKey,
                                           String routeCodeKey,
                                           String cardIdKey) {
//        Set<String> plateNumbers = new HashSet<>();
//        Set<String> routeCodes = new HashSet<>();
        Set<String> cardIds = new HashSet<>();


//        for (ImportSingleData data : dataList) {
////            collectValue(data, carPlateKey, plateNumbers);
//            String carValue = (String) data.getValueByKey(carPlateKey);
//            if (carValue != null && !carValue.isEmpty()) {
//                plateNumbers.add(normalizePlateNumber(carValue));
//            }
//
////            collectValue(data, routeCodeKey, routeCodes);
//            String routeValue = (String) data.getValueByKey(routeCodeKey);
//            if (routeValue != null && !routeValue.isEmpty()) {
//                routeCodes.add(routeValue.toUpperCase());
//            }
//            collectValue(data, cardIdKey, cardIds);
//        }

        String tenantId = InvocationInfoProxy.getTenantid();
        ValidateContext ctx = new ValidateContext();
//        ctx.carMap = queryCars(plateNumbers, tenantId);
//        ctx.routeMap = queryRoutes(routeCodes, tenantId);
        ctx.cardMap = queryCards(cardIds, tenantId);
        return ctx;
    }

    /**
     * 批量查询车辆
     */
    public Map<String, TCMCar> queryCars(Set<String> plateNumbers, String tenantId) {
        if (plateNumbers.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryCondition<TCMCar> condition = new LambdaQueryCondition<>(new TCMCar());
        condition.in(TCMCar::getPlate_number, new ArrayList<>(plateNumbers))
                .eq(TCMCar::getYtenantId, tenantId)
                .eq(TCMCar::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(condition).stream()
                .collect(Collectors.toMap(TCMCar::getPlate_number, r -> r, (a, b) -> a));
    }

    /**
     * 批量查询路线
     */
    public Map<String, TCMRoute> queryRoutes(Set<String> routeCodes, String tenantId) {
        if (routeCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryCondition<TCMRoute> condition = new LambdaQueryCondition<>(new TCMRoute());
        condition.in(TCMRoute::getRoute_code, new ArrayList<>(routeCodes))
                .eq(TCMRoute::getYtenantId, tenantId)
                .eq(TCMRoute::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(condition).stream()
                .collect(Collectors.toMap(TCMRoute::getRoute_code, r -> r, (a, b) -> a));
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
     * 通用逐行校验：姓名、工号→员工ID、车辆、路线
     */
    public void validateRow(ImportSingleData data, ValidateContext ctx,
                            String nameKey,
                            String cardIdKey, String employeeIdKey,
                            String carPlateKey, String carIdKey,
                            String routeCodeKey, String routeIdKey) {
        // 姓名
        String name = (String) data.getValueByKey(nameKey);
        if (name == null || name.isEmpty()) {
            data.addImportError("數據中姓名為空，請檢查");
            return;
//            throw new RuntimeException("數據中姓名為空，請檢查");
        }
        // 工号 → 员工ID
        String cardId = (String) data.getValueByKey(cardIdKey);
        if (cardId != null && !cardId.isEmpty()) {
            if (!ctx.cardMap.containsKey(cardId)) {
                data.addImportError("工號【" + cardId + "】在員工檔案中不存在");
                return;
//                throw new RuntimeException("工號【" + cardId + "】在員工檔案中不存在");
            }
            data.getData().put(employeeIdKey, ctx.cardMap.get(cardId));
        } else {
            data.addImportError("數據中澳巴工號為空，請檢查");
            return;
//            throw new RuntimeException("數據中澳巴工號為空，請檢查");
        }
//        // 车辆
//        String carPlateNum = (String) data.getValueByKey(carPlateKey);
//        if (carPlateNum != null && !carPlateNum.isEmpty()) {
//            if (!ctx.carMap.containsKey(carPlateNum)) {
//                throw new RuntimeException("車牌號【" + carPlateNum + "】在車輛檔案中不存在");
//            }
//            data.getData().put(carIdKey, ctx.carMap.get(carPlateNum).getId());
//        }

//        // 车辆（支持 e3432、E3432、3432 匹配同一辆车）
//        String carPlateNum = (String) data.getValueByKey(carPlateKey);
//        if (carPlateNum != null && !carPlateNum.isEmpty()) {
//            String normalizedPlate = normalizePlateNumber(carPlateNum);
//            if (!ctx.carMap.containsKey(normalizedPlate)) {
//                data.addImportError("車牌號【" + carPlateNum + "】在車輛檔案中不存在");
//                return;
//            }
//            data.getData().put(carIdKey, ctx.carMap.get(normalizedPlate).getId());
//        }


//        // 路线
//        String routeCode = (String) data.getValueByKey(routeCodeKey);
//        if (routeCode != null && !routeCode.isEmpty()) {
//            if (!ctx.routeMap.containsKey(routeCode)) {
//                throw new RuntimeException("路線編號【" + routeCode + "】在路線檔案中不存在");
//            }
//            data.getData().put(routeIdKey, ctx.routeMap.get(routeCode).getId());
//        }

//        // 路线（支持大小写不敏感，如 8a → 8A）
//        String routeCode = (String) data.getValueByKey(routeCodeKey);
//        if (routeCode != null && !routeCode.isEmpty()) {
//            String upperRouteCode = routeCode.toUpperCase();
//            if (!ctx.routeMap.containsKey(upperRouteCode)) {
//                data.addImportError("路線編號【" + routeCode + "】在路線檔案中不存在");
//                return;
//            }
//            data.getData().put(routeIdKey, ctx.routeMap.get(upperRouteCode).getId());
//        }


    }

    private void collectValue(ImportSingleData data, String key, Set<String> target) {
        String value = (String) data.getValueByKey(key);
        if (value != null && !value.isEmpty()) {
            target.add(value);
        }
    }

    /**
     * 归一化车牌号：提取所有数字，去掉所有字母
     * 例如：e3432 → 3432, E3432 → 3432, 3432 → 3432, ab123 → 123
     */
    private String normalizePlateNumber(String plateNumber) {
        if (plateNumber == null || plateNumber.isEmpty()) {
            return plateNumber;
        }
        // 提取所有数字（去掉所有字母）
        return plateNumber.replaceAll("[^0-9]", "");
    }



}
