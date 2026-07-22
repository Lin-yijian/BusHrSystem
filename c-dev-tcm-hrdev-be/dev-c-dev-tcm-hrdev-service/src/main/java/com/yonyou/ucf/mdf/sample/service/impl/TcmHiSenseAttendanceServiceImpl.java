package com.yonyou.ucf.mdf.sample.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.*;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.BaTypeAttendanceSummaryDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseAttendanceDataDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseAttendanceMergedDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseResponseDTO;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.service.*;
import com.yonyou.ucf.mdf.sample.utils.DateUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import io.edap.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.imeta.core.base.ConditionOperator;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QuerySchema;
import org.imeta.orm.schema.SimpleCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 海信考勤数据同步服务实现类
 * 负责调用海信接口获取考勤数据，并按照业务规则进行合并处理
 */
@Service
@Slf4j
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

    @Autowired
    private TcmCarService tcmCarService;

    @Autowired
    private TcmAttendanceCorrectionService attendanceCorrectionService;

    @Autowired
    private TcmCarTypeService carTypeService;

    @Autowired
    private TcmBaTypeAttendanceDataService baTypeAttendanceDataService;

    @Autowired
    private TcmDriverHolidayWorkRecordService driverHolidayWorkRecordService;

    /**
     * 构造函数，初始化ObjectMapper并配置反序列化特性
     */
    public TcmHiSenseAttendanceServiceImpl() {
        this.objectMapper = new ObjectMapper();
        // 忽略未知属性，避免接口返回新增字段导致反序列化失败
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Integer countByRunDate(String runDate) {
        return getByRunDate(runDate).size();
    }

    private List<TcmHisenseAttendanceData> getByRunDate(String runDate) {

        if (StringUtil.isEmpty(runDate)) {
            throw new RuntimeException("日期參數不能爲空");
        }

        String tenantId = InvocationInfoProxy.getTenantid();

        LambdaQueryCondition<TcmHisenseAttendanceData> carCondition = new LambdaQueryCondition<>(new TcmHisenseAttendanceData());
        carCondition.eq(TcmHisenseAttendanceData::getRun_date, runDate)
                .eq(TcmHisenseAttendanceData::getYtenantId,tenantId)
                .eq(TcmHisenseAttendanceData::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(carCondition);
    }


    @Override
    public void removeByRunDate(String runDate) {

        List<SimpleCondition> conditionList = new ArrayList<>();
        conditionList.add(new SimpleCondition("run_date", ConditionOperator.in, runDate));
        billRepository.batchRemove(UrlEnum.HISENSE_ATTENDANCE.getUrl(), conditionList);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void syncData(String runDate) {
        List<HiSenseAttendanceDataDTO> hiSenseDTOList = getHiSenseDataByRunData(runDate);
        if (hiSenseDTOList.isEmpty()) {
            throw new RuntimeException("當前日期數據爲空，請稍後再試");
        }


        List<HiSenseAttendanceMergedDTO> hiSenseDataList = mergeAttendanceData(hiSenseDTOList);
        removeByRunDate(runDate);

        List<TcmHisenseAttendanceData> dataList = new ArrayList<>();
        List<TcmDriverHolidayWorkRecord> driverHolidayWorkRecordList = new ArrayList<>();
        List<TcmBaTypeAttendanceData> baTypeAttendanceDataList = new ArrayList<>();

        List<String> routeNameList = new ArrayList<>();
        List<String> busCardIdList = new ArrayList<>();
        List<String> employeeIdList = new ArrayList<>();
        for (HiSenseAttendanceMergedDTO hiSenseData : hiSenseDataList) {
            routeNameList.add(hiSenseData.getRoutename());
            busCardIdList.add(hiSenseData.getBuscardid());
            employeeIdList.add(hiSenseData.getEmployeeId());

        }

        // 基礎信息
        String tenantId = InvocationInfoProxy.getTenantid();
        Map<String, String> staffMap = getStaffByCardIds(employeeIdList, tenantId);
        Map<String, TCMCar> stringTCMCarMap = tcmCarService.groupCarMapByPlateNumber(busCardIdList);
        Map<String, TCMRoute> stringTCMRouteMap = groupRouteMapByPlateNumber(routeNameList, tenantId);

        // 收集條件
        Set<String> staffSet = new HashSet<>();

        for (HiSenseAttendanceMergedDTO hiSenseData : hiSenseDataList) {
            String id = ymsOidGenerator.nextStrId();
            TcmHisenseAttendanceData data = new TcmHisenseAttendanceData();
            data.setId(id);
            data.setYtenantId(tenantId);
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
            data.setCount(hiSenseData.getCount());

            TCMCar tcmCar = stringTCMCarMap.get(hiSenseData.getBuscardid());
            if (tcmCar == null) {
                throw new RuntimeException("车辆信息档案缺失，请先完善档案内容，车牌号：" + hiSenseData.getBuscardid());
            }

            TCMRoute tcmRoute = stringTCMRouteMap.get(hiSenseData.getRoutename());
            if (tcmRoute == null) {
                throw new RuntimeException("路线信息档案缺失，请先完善档案内容，路线：" + hiSenseData.getRoutename());
            }

            String staffId = staffMap.get(hiSenseData.getEmployeeId());
            staffSet.add(staffId);
            data.setEmployee_id(staffId);

            data.setCar(tcmCar.getId());
            data.setRoute(tcmRoute.getId());

            // 车长假期工作
            TcmDriverHolidayWorkRecord driverHolidayWorkRecord = new TcmDriverHolidayWorkRecord();
            driverHolidayWorkRecord.setEmployee_id(data.getEmployee_id());
            driverHolidayWorkRecord.setDriver_no(data.getWaybill_employee_id());
            driverHolidayWorkRecord.setDriver_name(data.getDriver_name());
            driverHolidayWorkRecord.setDate(runDate);
            driverHolidayWorkRecord.setYtenantId(tenantId);
            driverHolidayWorkRecordList.add(driverHolidayWorkRecord);

            dataList.add(data);

        }


        // 巴型转数：按员工工号分组处理
        Map<String, List<HiSenseAttendanceDataDTO>> groupedByEmployee = hiSenseDTOList.stream()
                .filter(dto -> !StringUtil.isEmpty(parseEmployeeId(dto.getDrivername())))
                .collect(Collectors.groupingBy(dto -> parseEmployeeId(dto.getDrivername())));

        Map<String, List<BaTypeAttendanceSummaryDTO>> baTypeMap = new HashMap<>();

        for (Map.Entry<String, List<HiSenseAttendanceDataDTO>> entry : groupedByEmployee.entrySet()) {
            String employeeId = entry.getKey();
            List<HiSenseAttendanceDataDTO> dtoList = entry.getValue();

            // 按实际发车时间升序排序
            dtoList.sort(Comparator.comparing(HiSenseAttendanceDataDTO::getRealleavetime, Comparator.nullsLast(Date::compareTo)));

            String staffId = staffMap.get(employeeId);

            // 用于暂存当前员工各车型的汇总值（同一车型累加）
            Map<String, BaTypeAttendanceSummaryDTO> carTypeSummaryMap = new HashMap<>();

            // 循环处理每条数据，设置序号
            int orderNo = 1;
            for (HiSenseAttendanceDataDTO dto : dtoList) {
                TcmBaTypeAttendanceData data = new TcmBaTypeAttendanceData();

                data.setYtenantId(tenantId);
                data.setEmployee_id(staffId);
                TCMCar tcmCar = stringTCMCarMap.get(dto.getBuscardid());
                data.setCar(tcmCar.getId());
                // 空值检查：如果发车时间或到达时间为空，巴型转数设为0
                BigDecimal busTransferTimes = BigDecimal.ZERO;
                if (dto.getRealleavetime() != null && dto.getRealarrivetime() != null) {
                    double hours = DateUtil.getTimeDifferenceInHours(dto.getRealarrivetime(), dto.getRealleavetime(), 8);
                    busTransferTimes = new BigDecimal(hours);
                }
                data.setBus_transfer_times(busTransferTimes);
                data.setDate(runDate);
                data.setOrder_no(String.valueOf(orderNo++));
                baTypeAttendanceDataList.add(data);

                // 按车型归类汇总巴型转数
                String carType = tcmCar.getCar_type();
                BaTypeAttendanceSummaryDTO summary = carTypeSummaryMap.get(carType);
                if (summary == null) {
                    // 车型不存在，新建记录
                    summary = new BaTypeAttendanceSummaryDTO(staffId, carType, busTransferTimes);
                    carTypeSummaryMap.put(carType, summary);
                } else {
                    // 车型已存在，累加巴型转数
                    summary.setBusTransferTimes(summary.getBusTransferTimes().add(busTransferTimes));
                }
            }

            // 将当前员工的汇总数据存入缓存
            List<BaTypeAttendanceSummaryDTO> staffSummary = new ArrayList<>(carTypeSummaryMap.values());
            baTypeMap.put(staffId, staffSummary);
        }

        // 更新考勤修正数据
        assemblyCorrection(dataList, baTypeMap, runDate, staffSet);


        billRepository.insert(dataList);
        // 車長假期工作記錄
        driverHolidayWorkRecordService.coverInsert(runDate,driverHolidayWorkRecordList);
//        billRepository.insert(driverHolidayWorkRecordList);
        // 巴型转数
        baTypeAttendanceDataService.coverInsert(runDate,baTypeAttendanceDataList);
//        billRepository.insert(baTypeAttendanceDataList);


    }

    /**
     * 为 TcmAttendanceCorrection 填充符合条件的数据
     * @param dataList 海信考勤数据
     * @param baTypeMap 巴型转数数据
     * @param runDate 日期
     * @param staffIdList 员工ID集合
     */
    private void assemblyCorrection(List<TcmHisenseAttendanceData> dataList, Map<String, List<BaTypeAttendanceSummaryDTO>> baTypeMap, String runDate, Collection<String> staffIdList){
        List<TcmAttendanceCorrection> correctionList = attendanceCorrectionService.getByDateListAndStaffIds(runDate, staffIdList);
        if (correctionList.isEmpty()) {
            return;
        }

        // 按员工ID分组考勤数据，预处理
        Map<String, List<TcmHisenseAttendanceData>> attendanceGroupMap = dataList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(TcmHisenseAttendanceData::getEmployee_id));

        // 预加载车型映射，避免重复查询
        Map<String, TCMCarType> carTypeMap = carTypeService.mapByFieldGetter(TCMCarType::getId);

        // 批量处理每条修正记录
        for (TcmAttendanceCorrection entity : correctionList) {
            String employeeId = entity.getEmployee_id();
            List<TcmHisenseAttendanceData> attendanceDataList = attendanceGroupMap.get(employeeId);

            // 无考勤数据时跳过
            if (attendanceDataList == null || attendanceDataList.isEmpty()) {
                continue;
            }

            // 创建可变列表用于跟踪已匹配的数据
            List<TcmHisenseAttendanceData> availableDataList = new ArrayList<>(attendanceDataList);

            // 步骤1和步骤2：处理一更数据（两条可共享同一条记录）
            processFirstShift(entity, availableDataList);

            // 步骤3和步骤4：处理二更数据（与一更数据隔离）
            processSecondShift(entity, availableDataList);

            // 巴型工时计算
            calculateTypeWorkHour(entity, baTypeMap.get(employeeId), carTypeMap);
        }

        // 批量更新修正数据
        billRepository.update(correctionList);
    }

    /**
     * 处理一更数据
     * kq_one_departure_time 和 kq_one_return_time 可以来自同一条记录
     */
    private void processFirstShift(TcmAttendanceCorrection entity, List<TcmHisenseAttendanceData> availableDataList) {
        // kq_one_departure_time：取数据集中 real_leave_time 的最小值
        TcmHisenseAttendanceData minLeaveTimeData = availableDataList.stream()
                .filter(d -> d.getReal_leave_time() != null)
                .min(Comparator.comparing(TcmHisenseAttendanceData::getReal_leave_time))
                .orElse(null);
        if (minLeaveTimeData != null) {
            entity.setKq_one_departure_time(minLeaveTimeData.getReal_leave_time());
            entity.setOne_license_plate(minLeaveTimeData.getCar());
        } else {
            entity.setKq_one_departure_time(null);
            entity.setOne_license_plate(null);
        }

        // kq_one_return_time：取数据集中 real_arrive_time 与 pg_one_return_time 最接近的值
        Date pgOneReturnTime = entity.getPg_one_return_time();
        TcmHisenseAttendanceData closestArriveData = findClosestData(availableDataList, pgOneReturnTime, true);
        if (closestArriveData != null) {
            entity.setKq_one_return_time(closestArriveData.getReal_arrive_time());
        } else {
            entity.setKq_one_return_time(null);
        }

        // 统一移除第一步和第二步匹配到的数据
        if (minLeaveTimeData != null) {
            availableDataList.remove(minLeaveTimeData);
        }
        if (closestArriveData != null && closestArriveData != minLeaveTimeData) {
            availableDataList.remove(closestArriveData);
        }
    }

    /**
     * 处理二更数据
     * kq_two_departure_time 和 kq_two_return_time 与一更数据隔离
     */
    private void processSecondShift(TcmAttendanceCorrection entity, List<TcmHisenseAttendanceData> availableDataList) {
        if (availableDataList.isEmpty()) {
            entity.setKq_two_departure_time(null);
            entity.setTwo_license_plate(null);
            entity.setKq_two_return_time(null);
            return;
        }

        //  kq_two_departure_time：取剩余数据集中 real_leave_time 与 pg_two_departure_time 最接近的值
        Date pgTwoDepartureTime = entity.getPg_two_departure_time();
        TcmHisenseAttendanceData closestLeaveData = findClosestData(availableDataList, pgTwoDepartureTime, false);
        if (closestLeaveData != null) {
            entity.setKq_two_departure_time(closestLeaveData.getReal_leave_time());
            entity.setTwo_license_plate(closestLeaveData.getCar());
//            availableDataList.remove(closestLeaveData);
        } else {
            entity.setKq_two_departure_time(null);
            entity.setTwo_license_plate(null);
        }

        // kq_two_return_time：取剩余数据集中 real_arrive_time 的最大值
        Date maxArriveTime = availableDataList.stream()
                .map(TcmHisenseAttendanceData::getReal_arrive_time)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(null);
        entity.setKq_two_return_time(maxArriveTime);
    }

    /**
     * 巴型工时计算赋值
     * @param entity 修正对象
     * @param baTypeList 巴型转数数据
     * @param carTypeMap 车型信息
     */
    private void calculateTypeWorkHour(TcmAttendanceCorrection entity, List<BaTypeAttendanceSummaryDTO> baTypeList, Map<String, TCMCarType> carTypeMap){
        if (baTypeList == null || baTypeList.isEmpty()) {
            return;
        }

        BigDecimal totalTimes = baTypeList.stream()
                .map(BaTypeAttendanceSummaryDTO::getBusTransferTimes)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 处理除零情况
        if (totalTimes.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        String jlValidTime = entity.getJl_valid_time();
        for (BaTypeAttendanceSummaryDTO dto : baTypeList) {
            TCMCarType carType = carTypeMap.get(dto.getCarType());
            if (carType == null) {
                continue;
            }

            String typeCode = carType.getType_code();
            BigDecimal busTransferTimes = dto.getBusTransferTimes();
            String runCarTime = calculate(jlValidTime, busTransferTimes, totalTimes);
            String baseTime = calculate("08:00", busTransferTimes, totalTimes);
            String overTime = DateUtil.subtractTimeDifferences(runCarTime, baseTime);

            // 暂时以编号区分巴型
            switch (typeCode){
                case "01":
                    entity.setMinibus_bus_type_rpm(busTransferTimes);
                    entity.setMinibus_driving_hours(runCarTime);
                    entity.setMinibus_base_hours(baseTime);
                    entity.setMinibus_overtime_hours(overTime);
                    break;
                case "02":
                    entity.setMedium_bus_type_rpm(busTransferTimes);
                    entity.setMedium_bus_driving_hours(runCarTime);
                    entity.setMedium_bus_base_hours(baseTime);
                    entity.setMedium_bus_overtime_hours(overTime);
                    break;
                case "03":
                    entity.setLarge_bus_type_rpm(busTransferTimes);
                    entity.setLarge_bus_driving_hours(runCarTime);
                    entity.setLarge_bus_base_hours(baseTime);
                    entity.setLarge_bus_overtime_hours(overTime);
                    break;
                case "04":
                    entity.setExtra_large_bus_type_rpm(busTransferTimes);
                    entity.setExtra_large_bus_driving_hours(runCarTime);
                    entity.setExtra_large_bus_base_hours(baseTime);
                    entity.setExtra_large_bus_overtime_hours(overTime);
                    break;
            }
        }
    }

    /**
     * 巴型工时计算 = 有效工时 * (目标巴型转数/总巴型转数)
     * @param workHour 有效工时
     * @param targetTime 目标巴型转数
     * @param totalTimes 总巴型转数
     * @return 巴型工时
     */
    private String calculate(String workHour, BigDecimal targetTime, BigDecimal totalTimes){
        // 空值和零值检查
        if (StringUtil.isEmpty(workHour) || targetTime == null || totalTimes == null || totalTimes.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("巴型工时计算参数异常，返回默认值00:00。workHour={}, targetTime={}, totalTimes={}", 
                    workHour, targetTime, totalTimes);
            return "00:00";
        }

        BigDecimal workHourBigDecimal = DateUtil.timeStringToHoursBigDecimal(workHour, 8);
        BigDecimal m2 = targetTime.divide(totalTimes, 8, RoundingMode.HALF_UP);
        BigDecimal multiply = workHourBigDecimal.multiply(m2);
        return DateUtil.hoursToTimeString(multiply);
    }



    /**
     * 在海信考勤数据列表中查找与目标时间最接近的数据对象
     * @param dataList 海信考勤数据列表
     * @param targetDate 目标时间
     * @param findArriveTime true表示查找real_arrive_time，false表示查找real_leave_time
     * @return 最接近的数据对象，如果列表为空或目标时间为空则返回null
     */
    private TcmHisenseAttendanceData findClosestData(List<TcmHisenseAttendanceData> dataList, Date targetDate, boolean findArriveTime) {
        if (targetDate == null || dataList == null || dataList.isEmpty()) {
            return null;
        }

        long targetTime = targetDate.getTime();
        TcmHisenseAttendanceData closestData = null;
        long minDiff = Long.MAX_VALUE;

        for (TcmHisenseAttendanceData data : dataList) {
            Date dateToCompare = findArriveTime ? data.getReal_arrive_time() : data.getReal_leave_time();
            if (dateToCompare != null) {
                long diff = Math.abs(dateToCompare.getTime() - targetTime);
                if (diff < minDiff) {
                    minDiff = diff;
                    closestData = data;
                }
            }
        }

        return closestData;
    }


    /**
     * 查询BIP员工信息
     * @param ids
     * @param tenantId
     * @return
     */
    private Map<String,String> getStaffByCardIds(List<String> ids,String tenantId) {
        QuerySchema querySchema = QuerySchema.create().addSelect("id, staffDefines.ab_01 as cardId,code");
        querySchema.appendQueryCondition(QueryCondition.name("staffDefines.ab_01").in(ids));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());

        if (mapList == null || mapList.isEmpty()) {
            return new HashMap<>();
        }

        Map<String,String> finalMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            finalMap.put((String) map.get("cardId"),(String) map.get("id"));
        }

        return finalMap;

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
    private  List<HiSenseAttendanceDataDTO> getHiSenseDataByRunData(String runDate) {
        // 参数校验：业务日期不能为空
        if (StringUtil.isEmpty(runDate)) {
            throw new RuntimeException("业务日期不能为空");
        }

        // 使用RestTemplate调用海信接口
        RestTemplate restTemplate = new RestTemplate();
        String body = "{\"rundate\":\"" + runDate + "\"}";
        String result = restTemplate.postForObject(HI_SENSE_ATTENDANCE_URL, body, String.class);

        try {
            // 解析JSON响应为HiSenseResponseDTO对象（使用泛型）
            HiSenseResponseDTO<HiSenseAttendanceDataDTO> response = objectMapper.readValue(result,
                    objectMapper.getTypeFactory().constructParametricType(HiSenseResponseDTO.class, HiSenseAttendanceDataDTO.class));
            return response.getResult();

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

        // 记录合并数量（当前合并组包含的原始记录条数）
        merged.setCount(group.size());

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