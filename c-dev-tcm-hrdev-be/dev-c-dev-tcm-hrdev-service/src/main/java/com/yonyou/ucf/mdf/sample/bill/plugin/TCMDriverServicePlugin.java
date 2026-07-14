package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMCar;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMDriverService;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMRoute;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
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
public class TCMDriverServicePlugin extends AbstractBillPlugin implements ImportEventExtendProcessor {

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public void beforeTranslate(@NotNull ImportBatchData importBatchData, @NotNull ImportEventBO importEventBO) {
        // 获取导入数据列表
        List<ImportSingleData> dataList = importBatchData.getData();

        // 收集所有需要校验的相关信息
        Set<String> plateNumbers = new HashSet<>();
        Set<String> routeCodes = new HashSet<>();
        Set<String> cardIds = new HashSet<>();
        for (ImportSingleData data : dataList) {
            String plate = (String) data.getValueByKey(TCMDriverService.CAR_PLATE_NUMBER);
            if (plate != null && !plate.isEmpty()) {
                plateNumbers.add(plate);
            }
            String route = (String) data.getValueByKey(TCMDriverService.ROUTE_ROUTE_CODE);
            if (route != null && !route.isEmpty()) {
                routeCodes.add(route);
            }
            String cardId = (String) data.getValueByKey(TCMDriverService.WAYBILL_EMPLOYEE_ID);
            if (cardId != null && !cardId.isEmpty()) {
                cardIds.add(cardId);
            }
        }

        String tenantid = InvocationInfoProxy.getTenantid();
        // 查询车辆信息
        Map<String, TCMCar> carMap = Collections.emptyMap();
        if (!plateNumbers.isEmpty()) {
            LambdaQueryCondition<TCMCar> carCondition = new LambdaQueryCondition<>(new TCMCar());
            carCondition.in(TCMCar::getPlate_number, new ArrayList<>(plateNumbers))
                    .eq(TCMCar::getYtenantId, tenantid)
                    .eq(TCMCar::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
            List<TCMCar> carList = billRepository.queryByClause(carCondition);
            carMap = carList.stream().collect(Collectors.toMap(
                    TCMCar::getPlate_number, r -> r, (a, b) -> a));
        }

        // 查询路线信息
        Map<String, TCMRoute> routeMap = Collections.emptyMap();
        if (!routeCodes.isEmpty()) {
            LambdaQueryCondition<TCMRoute> routeCondition = new LambdaQueryCondition<>(new TCMRoute());
            routeCondition.in(TCMRoute::getRoute_code, new ArrayList<>(routeCodes))
                    .eq(TCMRoute::getYtenantId, tenantid)
                    .eq(TCMRoute::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
            List<TCMRoute> routeList = billRepository.queryByClause(routeCondition);
            routeMap = routeList.stream().collect(Collectors.toMap(
                    TCMRoute::getRoute_code, r -> r, (a, b) -> a));
        }

        // 根据澳巴工号获取员工信息
        Map<String, String> cardMap = Collections.emptyMap();
        if (!cardIds.isEmpty()) {
            cardMap = commonUtil.getStaffByCardIds(new ArrayList<>(cardIds), tenantid);
        }

        // 校验
        for (ImportSingleData data : dataList) {
            // 姓名
            String name = (String) data.getValueByKey(TCMDriverService.DIVER_NAME);
            if (name == null || name.isEmpty()) {
                throw new RuntimeException("數據中姓名為空，請檢查");
            }
            // 工号
            String cardId = (String) data.getValueByKey(TCMDriverService.WAYBILL_EMPLOYEE_ID);
            if (cardId != null && !cardId.isEmpty()) {
                if (!cardMap.containsKey(cardId)) {
                    throw new RuntimeException("工號【" + cardId + "】在員工檔案中不存在");
                } else {
                    data.getData().put(TCMDriverService.EMPLOYEE_ID, cardMap.get(cardId));
                }
            } else {
                throw new RuntimeException("數據中澳巴工號為空，請檢查");
            }
            // 车辆
            String carPlateNum = (String) data.getValueByKey(TCMDriverService.CAR_PLATE_NUMBER);
            if (carPlateNum != null && !carPlateNum.isEmpty()) {
                if (!carMap.containsKey(carPlateNum)) {
                    throw new RuntimeException("車牌號【" + carPlateNum + "】在車輛檔案中不存在");
                } else {
                    data.getData().put(TCMDriverService.CAR, carMap.get(carPlateNum).getId());
                }
            }
            // 路线
            String routeCode = (String) data.getValueByKey(TCMDriverService.ROUTE_ROUTE_CODE);
            if (routeCode != null && !routeCode.isEmpty()) {
                if (!routeMap.containsKey(routeCode)) {
                    throw new RuntimeException("路線編號【" + routeCode + "】在路線檔案中不存在");
                } else {
                    data.getData().put(TCMDriverService.ROUTE, routeMap.get(routeCode).getId());
                }
            }
        }

        ImportEventExtendProcessor.super.beforeTranslate(importBatchData, importEventBO);
    }

}