package com.yonyou.ucf.mdf.sample.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMCar;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMRoute;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmHisenseAttendanceData;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseAttendanceDataDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseAttendanceMergedDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseAttendanceResponseDTO;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.service.TcmHiSenseAttendanceService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import io.edap.util.StringUtil;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QuerySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 海信考勤数据同步服务实现类
 * 负责调用海信接口获取考勤数据，并按照业务规则进行合并处理
 */
@Service
public class TcmHiSenseAttendanceServiceImpl implements TcmHiSenseAttendanceService {

    /** 海信考勤接口URL */
    public static final String HI_SENSE_ATTENDANCE_URL = "http://10.102.128.7:8080/sdata/rest/service/dataapi/rest/6903218280134273";

    /** 一小时的毫秒数，用于判断两趟车是否需要合并 */
    private static final long ONE_HOUR_IN_MILLIS = 60 * 60 * 1000L;


    private final ObjectMapper objectMapper;

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    private YmsOidGenerator ymsOidGenerator;

    @Autowired
    protected IBillQueryRepository queryService;

    /**
     * 构造函数，初始化ObjectMapper并配置反序列化特性
     */
    public TcmHiSenseAttendanceServiceImpl() {
        this.objectMapper = new ObjectMapper();
        // 忽略未知属性，避免接口返回新增字段导致反序列化失败
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @Override
    public void syncData(String runDate) {
        List<HiSenseAttendanceMergedDTO> hiSenseDataList = getHiSenseDataByRunData(runDate);
        if (hiSenseDataList.isEmpty()) {
            throw new RuntimeException("当前日期数据为空，请稍后再试");
        }

        List<TcmHisenseAttendanceData> dataList = new ArrayList<>();

        List<String> routeNameList = new ArrayList<>();
        List<String> busCardIdList = new ArrayList<>();
        for (HiSenseAttendanceMergedDTO hiSenseData : hiSenseDataList) {
            routeNameList.add(hiSenseData.getRoutename());
            busCardIdList.add(hiSenseData.getBuscardid());
        }

        String tenantId = InvocationInfoProxy.getTenantid();

        Map<String, TCMCar> stringTCMCarMap = groupCarMapByPlateNumber(busCardIdList, tenantId);

        Map<String, TCMRoute> stringTCMRouteMap = groupRouteMapByPlateNumber(routeNameList, tenantId);


        for (HiSenseAttendanceMergedDTO hiSenseData : hiSenseDataList) {
            TcmHisenseAttendanceData data = new TcmHisenseAttendanceData();
            data.setFake_flag(hiSenseData.getFakeflag());
            data.setTrouble_flag(hiSenseData.getTroubleflag());
            data.setReal_arrive_time(hiSenseData.getRealarrivetime());
            data.setReal_leave_time(hiSenseData.getRealleavetime());
            data.setSegment_id(hiSenseData.getSegmentid());
            data.setSegment_name(hiSenseData.getSegmentname());
            data.setStart_station_name(hiSenseData.getStartstationname());
            data.setEnd_station_name(hiSenseData.getEndstationname());
            data.setDriver_name(hiSenseData.getDriverName());
            data.setWaybill_employee_id(hiSenseData.getEmployeeId());
            data.setRun_date(runDate);

            TCMCar tcmCar = stringTCMCarMap.get(hiSenseData.getBuscardid());
            if (tcmCar == null) {
                throw new RuntimeException("车辆信息档案缺失，请先完善档案内容，车牌号：" + hiSenseData.getBuscardid());
            }

            TCMRoute tcmRoute = stringTCMRouteMap.get(hiSenseData.getRoutename());
            if (tcmRoute == null) {
                throw new RuntimeException("路线信息档案缺失，请先完善档案内容，路线：" + hiSenseData.getRoutename());
            }

            data.setCar(tcmCar.getId());
            data.setRoute(tcmRoute.getId());
            dataList.add(data);

        }

        billRepository.insert(dataList);


    }

    /**
     * 分组
     * @param busCardIdList
     * @param tenantId
     * @return
     */
    private Map<String,TCMCar> groupCarMapByPlateNumber(List<String> busCardIdList,String tenantId) {

        LambdaQueryCondition<TCMCar> carCondition = new LambdaQueryCondition<>(new TCMCar());
        carCondition.in(TCMCar::getPlate_number, busCardIdList)
                .eq(TCMCar::getYtenantId,tenantId)
                .eq(TCMCar::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TCMCar> list = billRepository.queryByClause(carCondition);

        if (list.isEmpty()) {
            throw new RuntimeException("车辆信息档案缺失，请先完善档案内容，车牌号：" + busCardIdList);
        }

        return list.stream().collect(Collectors.toMap(
                TCMCar::getPlate_number,
                item -> item,
                (existing, replacement) -> existing
        ));
    }

    /**
     * 分组
     * @param routeNameList
     * @param tenantId
     * @return
     */
    private Map<String,TCMRoute> groupRouteMapByPlateNumber(List<String> routeNameList,String tenantId) {

        LambdaQueryCondition<TCMRoute> routeCondition = new LambdaQueryCondition<>(new TCMRoute());
        routeCondition.in(TCMRoute::getRoute_code, routeNameList)
                .eq(TCMRoute::getYtenantId,tenantId)
                .eq(TCMRoute::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        List<TCMRoute> list = billRepository.queryByClause(routeCondition);

        if (list.isEmpty()) {
            throw new RuntimeException("路线信息档案缺失，请先完善档案内容，路线：" + routeNameList);
        }

        return list.stream().collect(Collectors.toMap(
                TCMRoute::getRoute_code,
                item -> item,
                (existing, replacement) -> existing
        ));
    }

    /**
     * 同步海信考勤数据
     * 调用海信接口获取原始数据，解析后按照业务规则合并，返回合并后的结果
     *
     * @param runDate 业务日期
     * @return 合并后的考勤数据列表
     */
    private  List<HiSenseAttendanceMergedDTO> getHiSenseDataByRunData(String runDate) {
        // 参数校验：业务日期不能为空
        if (StringUtil.isEmpty(runDate)) {
            throw new RuntimeException("业务日期不能为空");
        }

        // 使用RestTemplate调用海信接口
        RestTemplate restTemplate = new RestTemplate();
        String body = "{\"rundate\":\"" + runDate + "\"}";
        String result = restTemplate.postForObject(HI_SENSE_ATTENDANCE_URL, body, String.class);

        try {
            // 解析JSON响应为HiSenseAttendanceResponseDTO对象
            HiSenseAttendanceResponseDTO response = objectMapper.readValue(result, HiSenseAttendanceResponseDTO.class);
            List<HiSenseAttendanceDataDTO> attendanceDataList = response.getResult();

            return mergeAttendanceData(attendanceDataList);

        } catch (Exception e) {
            throw new RuntimeException("解析海信考勤数据失败", e);
        }
    }

    /**
     * 合并考勤数据
     * 步骤：1.按工号+车牌+线路+方向+日期分组 2.组内按实际发车时间排序 3.判断相邻记录是否合并
     *
     * @param dataList 原始考勤数据列表
     * @return 合并后的考勤数据列表
     */
    private List<HiSenseAttendanceMergedDTO> mergeAttendanceData(List<HiSenseAttendanceDataDTO> dataList) {
        // 空数据处理
        if (dataList == null || dataList.isEmpty()) {
            return Collections.emptyList();
        }

        // 按工号、车牌、线路、方向、运行日期分组
        // 确保不会跨天、跨线路、跨方向合并
        Map<String, List<HiSenseAttendanceDataDTO>> groupedMap = dataList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(this::buildGroupKey));

        List<HiSenseAttendanceMergedDTO> mergedList = new ArrayList<>();

        // 遍历每个分组，进行组内排序和合并
        for (List<HiSenseAttendanceDataDTO> group : groupedMap.values()) {
            // 按实际发车时间排序，null值排在最后
            group.sort(Comparator.comparing(HiSenseAttendanceDataDTO::getRealleavetime, Comparator.nullsLast(Date::compareTo)));
            // 在组内执行合并逻辑
            mergedList.addAll(mergeWithinGroup(group));
        }

        return mergedList;
    }

    /**
     * 构建分组键
     * 分组维度：工号 + 车牌 + 线路 + 方向 + 运行日期
     *
     * @param data 考勤数据
     * @return 分组键字符串
     */
    private String buildGroupKey(HiSenseAttendanceDataDTO data) {
        String employeeId = parseEmployeeId(data.getDrivername());
        String buscardid = data.getBuscardid();
        String routename = data.getRoutename();
        String segmentid = data.getSegmentid();
        String rundate = data.getRundate() != null ? String.valueOf(data.getRundate().getTime()) : "";
        return employeeId + "|" + buscardid + "|" + routename + "|" + segmentid + "|" + rundate;
    }

    /**
     * 从drivername中解析工号
     * drivername格式：姓名|工号，如"陸嘉華|5531"
     *
     * @param drivername 驾驶员信息（格式：姓名|工号）
     * @return 工号，解析失败返回空字符串
     */
    private String parseEmployeeId(String drivername) {
        if (StringUtil.isEmpty(drivername)) {
            return "";
        }
        String[] parts = drivername.split("\\|");
        if (parts.length >= 2) {
            return parts[1].trim();
        }
        return "";
    }

    /**
     * 从drivername中解析驾驶员姓名
     * drivername格式：姓名|工号，如"陸嘉華|5531"
     *
     * @param drivername 驾驶员信息（格式：姓名|工号）
     * @return 驾驶员姓名
     */
    private String parseDriverName(String drivername) {
        if (StringUtil.isEmpty(drivername)) {
            return "";
        }
        String[] parts = drivername.split("\\|");
        return parts[0].trim();
    }

    /**
     * 在分组内执行合并逻辑
     * 遍历排序后的记录，判断相邻记录是否需要合并
     * 合并条件：上一条的实际到达时间与下一条的实际发车时间间隔不超过1小时
     *
     * @param sortedGroup 已按实际发车时间排序的分组数据
     * @return 合并后的结果列表
     */
    private List<HiSenseAttendanceMergedDTO> mergeWithinGroup(List<HiSenseAttendanceDataDTO> sortedGroup) {
        List<HiSenseAttendanceMergedDTO> mergedList = new ArrayList<>();
        if (sortedGroup.isEmpty()) {
            return mergedList;
        }

        // 当前正在合并的组
        List<HiSenseAttendanceDataDTO> currentMergeGroup = new ArrayList<>();
        currentMergeGroup.add(sortedGroup.get(0));

        // 遍历剩余记录，判断是否合并到当前组
        for (int i = 1; i < sortedGroup.size(); i++) {
            HiSenseAttendanceDataDTO prev = currentMergeGroup.get(currentMergeGroup.size() - 1);
            HiSenseAttendanceDataDTO curr = sortedGroup.get(i);

            // 判断是否满足合并条件
            if (shouldMerge(prev, curr)) {
                // 满足条件，合并到当前组
                currentMergeGroup.add(curr);
            } else {
                // 不满足条件，将当前组合并成一条记录，然后开启新组
                mergedList.add(mergeGroup(currentMergeGroup));
                currentMergeGroup = new ArrayList<>();
                currentMergeGroup.add(curr);
            }
        }

        // 处理最后一个合并组
        if (!currentMergeGroup.isEmpty()) {
            mergedList.add(mergeGroup(currentMergeGroup));
        }

        return mergedList;
    }

    /**
     * 判断两条记录是否需要合并
     * 合并条件：上一条的实际到达时间与下一条的实际发车时间间隔不超过1小时，且时间顺序正确（非负数）
     *
     * @param prev 上一条记录
     * @param curr 当前记录
     * @return true表示需要合并，false表示不需要合并
     */
    private boolean shouldMerge(HiSenseAttendanceDataDTO prev, HiSenseAttendanceDataDTO curr) {
        Date prevArrive = prev.getRealarrivetime();
        Date currLeave = curr.getRealleavetime();

        // 任一时间为空则不合并
        if (prevArrive == null || currLeave == null) {
            return false;
        }

        // 计算时间间隔（毫秒）
        long gap = currLeave.getTime() - prevArrive.getTime();
        // 间隔 >= 0（时间顺序正确）且 <= 1小时（60*60*1000毫秒）则合并
        return gap >= 0 && gap <= ONE_HOUR_IN_MILLIS;
    }

    /**
     * 将一组连续的记录合并为一条
     * 合并规则：
     * - 调度发车时间：取第一条
     * - 调度到达时间：取最后一条
     * - 实际发车时间：取第一条
     * - 实际到达时间：取最后一条
     * - 其他字段：取第一条
     *
     * @param group 需要合并的记录组
     * @return 合并后的考勤数据
     */
    private HiSenseAttendanceMergedDTO mergeGroup(List<HiSenseAttendanceDataDTO> group) {
        // 取第一条和最后一条记录用于合并
        HiSenseAttendanceDataDTO first = group.get(0);
        HiSenseAttendanceDataDTO last = group.get(group.size() - 1);

        HiSenseAttendanceMergedDTO merged = new HiSenseAttendanceMergedDTO();

        // 基本信息取第一条
        merged.setRundate(first.getRundate());
        merged.setRoutename(first.getRoutename());
        merged.setSegmentid(first.getSegmentid());
        merged.setSegmentname(first.getSegmentname());
        merged.setStartstationname(first.getStartstationname());
        // 到达站点取最后一条（可能与第一条不同）
        merged.setEndstationname(last.getEndstationname());
        merged.setBusselfid(first.getBusselfid());
        merged.setBuscardid(first.getBuscardid());
        // 驾驶员信息拆分
        merged.setDriverName(parseDriverName(first.getDrivername()));
        merged.setEmployeeId(parseEmployeeId(first.getDrivername()));
        // 时间字段：发车时间取第一条，到达时间取最后一条
        merged.setLeavetime(first.getLeavetime());
        merged.setArrivetime(last.getArrivetime());
        merged.setRealleavetime(first.getRealleavetime());
        merged.setRealarrivetime(last.getRealarrivetime());
        // 其他状态字段取第一条
        merged.setIsconfirm(first.getIsconfirm());
        merged.setFakeflag(first.getFakeflag());
        merged.setTroubleflag(first.getTroubleflag());
        merged.setRecstate(first.getRecstate());

        return merged;
    }
}