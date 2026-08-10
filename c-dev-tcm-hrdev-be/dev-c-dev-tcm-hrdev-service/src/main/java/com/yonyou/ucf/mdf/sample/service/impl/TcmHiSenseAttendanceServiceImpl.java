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
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.service.*;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
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

    /**
     * 海信考勤接口URL
     */
    public static final String HI_SENSE_ATTENDANCE_URL = "http://10.102.128.7:8080/sdata/rest/service/dataapi/rest/6903218280134273";

    /**
     * 一小时的毫秒数，用于判断两趟车是否需要合并
     */
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

    @Autowired
    private TcmAttendanceCheckRecordService tcmAttendanceCheckRecordService;

    @Autowired
    private CommonUtil commonUtil;

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
                .eq(TcmHisenseAttendanceData::getYtenantId, tenantId)
                .eq(TcmHisenseAttendanceData::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(carCondition);
    }


    @Override
    public void removeByRunDate(String runDate) {

        List<SimpleCondition> conditionList = new ArrayList<>();
        conditionList.add(new SimpleCondition("run_date", ConditionOperator.in, runDate));
        billRepository.batchRemove(UrlEnum.HISENSE_ATTENDANCE.getUrl(), conditionList);
    }

    @Override
    public boolean checkDataByDate(String date) throws Exception {
        List<TcmHisenseAttendanceData> list = getByRunDate(date);
        return !list.isEmpty();
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
        Map<String, String> staffIdMap = new HashMap<>();
        Map<String, String> staffPostMap = new HashMap<>();
        Map<String, TCMCar> stringTCMCarMap = tcmCarService.groupCarMapByPlateNumber(busCardIdList);
        Map<String, TCMRoute> stringTCMRouteMap = groupRouteMapByPlateNumber(routeNameList, tenantId);
        List<Map<String, Object>> staffInfoList = commonUtil.getStaffByEmployeeIds(employeeIdList, tenantId);
        if (staffInfoList != null && !staffInfoList.isEmpty()) {
            for (Map<String, Object> map : staffInfoList) {
                staffIdMap.put((String) map.get(commonUtil.FIELD_CODE_WAYBILL_EMPLOYEE_ID), (String) map.get(commonUtil.FIELD_CODE_STAFF_ID));
                staffPostMap.put((String) map.get(commonUtil.FIELD_CODE_WAYBILL_EMPLOYEE_ID), (String) map.get(commonUtil.FIELD_CODE_POST_CODE));

            }
        }

        // 收集缺失数据
        List<String> missingCarNumbers = new ArrayList<>();
        List<String> missingRouteNames = new ArrayList<>();
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

            // 检查车辆信息
            TCMCar tcmCar = stringTCMCarMap.get(hiSenseData.getBuscardid());
            if (tcmCar == null) {
                missingCarNumbers.add(hiSenseData.getBuscardid());
            }

            // 检查路线信息
            TCMRoute tcmRoute = stringTCMRouteMap.get(hiSenseData.getRoutename());
            if (tcmRoute == null) {
                missingRouteNames.add(hiSenseData.getRoutename());
            }

            // 如果车辆或路线缺失，跳过当前数据处理，继续收集
            if (tcmCar == null || tcmRoute == null) {
                continue;
            }

            String staffId = staffIdMap.get(hiSenseData.getEmployeeId());
            staffSet.add(staffId);
            data.setEmployee_id(staffId);
            data.setCar(tcmCar.getId());
            data.setRoute(tcmRoute.getId());

            // 车长假期工作
            TcmDriverHolidayWorkRecord driverHolidayWorkRecord = new TcmDriverHolidayWorkRecord();
            driverHolidayWorkRecord.setEmployee_id(data.getEmployee_id());
            driverHolidayWorkRecord.setWaybill_employee_id(data.getWaybill_employee_id());
            driverHolidayWorkRecord.setDriver_name(data.getDriver_name());
            driverHolidayWorkRecord.setDate(runDate);
            driverHolidayWorkRecord.setYtenantId(tenantId);
            driverHolidayWorkRecordList.add(driverHolidayWorkRecord);

            dataList.add(data);
        }

        // 统一校验缺失数据，如果有缺失则抛出包含所有缺失信息的异常
        if (!missingCarNumbers.isEmpty() || !missingRouteNames.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder("数据校验失败：");
            if (!missingCarNumbers.isEmpty()) {
                errorMsg.append("\n车辆信息档案缺失，请先完善档案内容，缺失车牌号：")
                        .append(String.join("、", missingCarNumbers.stream().distinct().collect(Collectors.toList())));
            }
            if (!missingRouteNames.isEmpty()) {
                errorMsg.append("\n路线信息档案缺失，请先完善档案内容，缺失路线：")
                        .append(String.join("、", missingRouteNames.stream().distinct().collect(Collectors.toList())));
            }
            throw new RuntimeException(errorMsg.toString());
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

            String staffId = staffIdMap.get(employeeId);

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
                    busTransferTimes = DateUtil.getTimeDifferenceInHours(dto.getRealarrivetime(), dto.getRealleavetime(), 8);
                }
                data.setBus_transfer_times(busTransferTimes);
                data.setDate(runDate);
                data.setOrder_no(String.valueOf(orderNo));
                baTypeAttendanceDataList.add(data);

                // 按人员分组 将排序好的巴型转数放入集合中
                String carType = tcmCar.getCar_type();
                BaTypeAttendanceSummaryDTO summaryDTO = new BaTypeAttendanceSummaryDTO(staffId, carType, orderNo, busTransferTimes);
                orderNo++;
                List<BaTypeAttendanceSummaryDTO> summary = baTypeMap.get(staffId);
                if (summary == null || summary.isEmpty()) {
                    // 数据不存在，新建记录
                    summary = new ArrayList<>();
                    summary.add(summaryDTO);
                } else {
                    // 车型已存在，累加巴型转数
                    summary.add(summaryDTO);
                }
                baTypeMap.put(staffId, summary);
            }
        }

        // 更新考勤修正数据
        assemblyCorrection(dataList, baTypeMap, runDate, staffSet, staffPostMap);

        billRepository.insert(dataList);
        // 車長假期工作記錄
        driverHolidayWorkRecordService.coverInsert(runDate, driverHolidayWorkRecordList);
        // 巴型转数
        baTypeAttendanceDataService.coverInsert(runDate, baTypeAttendanceDataList);

        // 更新核对记录
        tcmAttendanceCheckRecordService.updateOrSaveRecord(runDate, ModuleEnum.TCM_HI_SENSE_ATTENDANCE,true);

    }

    /**
     * 为 TcmAttendanceCorrection 填充符合条件的数据
     *
     * @param dataList    海信考勤数据
     * @param baTypeMap   巴型转数数据
     * @param runDate     日期
     * @param staffIdList 员工ID集合
     */
    private void assemblyCorrection(List<TcmHisenseAttendanceData> dataList, Map<String, List<BaTypeAttendanceSummaryDTO>> baTypeMap, String runDate, Collection<String> staffIdList, Map<String,String> staffPostMap) {
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

            // 是否迟到早退
            String lateDuration = entity.getLate_duration();
            String oneEarlyLeaveDuration = entity.getOne_early_leave_duration();
            String twoEarlyLeaveDuration = entity.getTwo_early_leave_duration();
            if (!StringUtil.isEmpty(lateDuration) || !DateUtil.checkIsZeroTime(oneEarlyLeaveDuration) || !DateUtil.checkIsZeroTime(twoEarlyLeaveDuration)) {
                entity.setIs_late_or_early_leave(true);
            }

            // 是否兼职司机
            String postCode = staffPostMap.get(employeeId);
            if (commonUtil.PART_TIME_JOB_CODE.equals(postCode)) {
                entity.setIs_less_than_8_hours(false);
            }
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
     * 根据巴型转数数据计算各车型的行车工时、基本工时和加班工时，并设置到修正对象中
     * 加班工时逻辑：按orderNo顺序累加行车工时，当累计超过8小时后，后续工时全部计入加班
     *
     * @param entity     考勤修正对象
     * @param baTypeList 巴型转数数据列表（按orderNo升序排列）
     * @param carTypeMap 车型信息映射（未使用，保留参数兼容性）
     */
    private void calculateTypeWorkHour(TcmAttendanceCorrection entity, List<BaTypeAttendanceSummaryDTO> baTypeList, Map<String, TCMCarType> carTypeMap) {
        // 无数据时直接返回
        if (baTypeList == null || baTypeList.isEmpty()) {
            return;
        }

        // 获取计粮有效工时（扣除用膳时间）
        String jl_valid_time = entity.getJl_valid_time();

        // 巴型类型顺序字符串（格式：carType,carType,carType）
        String bus_type_rpm_order = null;
        // 巴型转数字符串（格式：busTransferTimes,busTransferTimes）
        String bus_type_rpm = null;

        // 初始化各车型工时变量，初始值为00:00
        String minibus_base_hours = "00:00";         // 小巴基本工时
        String medium_bus_base_hours = "00:00";       // 中巴基本工时
        String large_bus_base_hours = "00:00";        // 大巴基本工时
        String extra_large_bus_base_hours = "00:00";  // 特大巴基本工时
        String minibus_driving_hours = "00:00";       // 小巴行车工时
        String medium_bus_driving_hours = "00:00";    // 中巴行车工时
        String large_bus_driving_hours = "00:00";     // 大巴行车工时
        String extra_large_bus_driving_hours = "00:00";// 特大巴行车工时
        String minibus_overtime_hours = "00:00";      // 小巴加班工时
        String medium_bus_overtime_hours = "00:00";   // 中巴加班工时
        String large_bus_overtime_hours = "00:00";    // 大巴加班工时
        String extra_large_bus_overtime_hours = "00:00";// 特大巴加班工时

        // 计算总巴型转数（用于计算单条记录的工时占比）
        BigDecimal totalTimes = baTypeList.stream()
                .map(BaTypeAttendanceSummaryDTO::getBusTransferTimes)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 用于拼接巴型类型顺序和巴型转数字符串
        StringBuilder busTypeOrderBuilder = new StringBuilder();
        StringBuilder busTypeRpmBuilder = new StringBuilder();

        // 累计已计算的行车工时（用于判断是否超过8小时）
        String countTime = "00:00";
        // 是否已开始进入加班状态
        boolean isOvertimeStarted = false;

        // 遍历巴型转数数据，按车型累加工时
        for (BaTypeAttendanceSummaryDTO dto : baTypeList) {
            TCMCarType type = carTypeMap.get(dto.getCarType());
            if (type == null){
                continue;
            }
            String typeCode = type.getType_code();
            BigDecimal busTransferTimes = dto.getBusTransferTimes(); // 巴型转数
            String runCarTime = calculate(jl_valid_time, busTransferTimes, totalTimes); // 行车工时 = 有效工时 * (目标转数/总转数)
            String baseTime = calculate("08:00", busTransferTimes, totalTimes); // 基本工时 = 8小时 * (目标转数/总转数)


            // 按orderNo顺序拼接车型编号和巴型转数
            busTypeOrderBuilder.append(typeCode).append(",");
            busTypeRpmBuilder.append(busTransferTimes).append(",");

            // 归一化车型编号（去除前导零，支持"01"、"1"等多种格式）
            String normalizedTypeCode = typeCode != null ? typeCode.replaceFirst("^0+", "") : "";

            // 车型编号（1=小巴，2=中巴，3=大巴，4=特大巴）
            // 将正常工时部分累加到行车工时和基本工时
            switch (normalizedTypeCode) {
                case "1":
                    minibus_driving_hours = DateUtil.addTimeDifferences(minibus_driving_hours, runCarTime);
                    minibus_base_hours = DateUtil.addTimeDifferences(minibus_base_hours, baseTime);
                    break;
                case "2":
                    medium_bus_driving_hours = DateUtil.addTimeDifferences(medium_bus_driving_hours, runCarTime);
                    medium_bus_base_hours = DateUtil.addTimeDifferences(medium_bus_base_hours, baseTime);
                    break;
                case "3":
                    large_bus_driving_hours = DateUtil.addTimeDifferences(large_bus_driving_hours, runCarTime);
                    large_bus_base_hours = DateUtil.addTimeDifferences(large_bus_base_hours, baseTime);
                    break;
                case "4":
                    extra_large_bus_driving_hours = DateUtil.addTimeDifferences(extra_large_bus_driving_hours, runCarTime);
                    extra_large_bus_base_hours = DateUtil.addTimeDifferences(extra_large_bus_base_hours, baseTime);
                    break;
            }

            // 判断是否已进入加班状态
            if (!isOvertimeStarted) {
                // 计算加上当前行车工时后的累计工时
                String nextCountTime = DateUtil.addTimeDifferences(countTime, runCarTime);

                // 如果累计工时超过8小时
                if (DateUtil.isTimeGreater(nextCountTime, "08:00")) {
                    // 计算当前循环中属于正常工时的部分（到8小时为止）
                    String remainingTime = DateUtil.subtractTimeDifferences("08:00", countTime);
                    // 计算当前循环中溢出的加班工时部分
                    String overflowTime = DateUtil.subtractTimeDifferences(runCarTime, remainingTime);

                    // 将溢出部分累加到加班工时
                    switch (normalizedTypeCode) {
                        case "1":
                            minibus_overtime_hours = DateUtil.addTimeDifferences(minibus_overtime_hours, overflowTime);
                            break;
                        case "2":
                            medium_bus_overtime_hours = DateUtil.addTimeDifferences(medium_bus_overtime_hours, overflowTime);
                            break;
                        case "3":
                            large_bus_overtime_hours = DateUtil.addTimeDifferences(large_bus_overtime_hours, overflowTime);
                            break;
                        case "4":
                            extra_large_bus_overtime_hours = DateUtil.addTimeDifferences(extra_large_bus_overtime_hours, overflowTime);
                            break;
                    }
                    isOvertimeStarted = true;
                }
                countTime = nextCountTime;
            } else {
                // 已超过8小时，全部算加班工时
                switch (normalizedTypeCode) {
                    case "1":
                        minibus_overtime_hours = DateUtil.addTimeDifferences(minibus_overtime_hours, runCarTime);
                        break;
                    case "2":
                        medium_bus_overtime_hours = DateUtil.addTimeDifferences(medium_bus_overtime_hours, runCarTime);
                        break;
                    case "3":
                        large_bus_overtime_hours = DateUtil.addTimeDifferences(large_bus_overtime_hours, runCarTime);
                        break;
                    case "4":
                        extra_large_bus_overtime_hours = DateUtil.addTimeDifferences(extra_large_bus_overtime_hours, runCarTime);
                        break;
                }
            }
        }

        // 移除末尾的逗号，生成最终的字符串值
        if (busTypeOrderBuilder.length() > 0) {
            bus_type_rpm_order = busTypeOrderBuilder.substring(0, busTypeOrderBuilder.length() - 1);
        }
        if (busTypeRpmBuilder.length() > 0) {
            bus_type_rpm = busTypeRpmBuilder.substring(0, busTypeRpmBuilder.length() - 1);
        }

        String status = StringUtil.isEmpty(bus_type_rpm_order) ? BaseEnum.NORMAL_CODE.getCode() : BaseEnum.ERROR_CODE.getCode();

        entity.setTcmBarTypeHourStatus(status);
        entity.setBus_type_rpm_order(bus_type_rpm_order);
        entity.setBus_type_rpm(bus_type_rpm);
        entity.setMinibus_driving_hours(minibus_driving_hours);
        entity.setMedium_bus_driving_hours(medium_bus_driving_hours);
        entity.setLarge_bus_driving_hours(large_bus_driving_hours);
        entity.setExtra_large_bus_driving_hours(extra_large_bus_driving_hours);
        entity.setMinibus_base_hours(minibus_base_hours);
        entity.setMedium_bus_base_hours(medium_bus_base_hours);
        entity.setLarge_bus_base_hours(large_bus_base_hours);
        entity.setExtra_large_bus_base_hours(extra_large_bus_base_hours);
        entity.setMinibus_overtime_hours(minibus_overtime_hours);
        entity.setMedium_bus_overtime_hours(medium_bus_overtime_hours);
        entity.setLarge_bus_overtime_hours(large_bus_overtime_hours);
        entity.setExtra_large_bus_overtime_hours(extra_large_bus_overtime_hours);
    }

    /**
     * 巴型工时计算 = 有效工时 * (目标巴型转数/总巴型转数)
     *
     * @param workHour   有效工时
     * @param targetTime 目标巴型转数
     * @param totalTimes 总巴型转数
     * @return 巴型工时
     */
    private String calculate(String workHour, BigDecimal targetTime, BigDecimal totalTimes) {
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
     *
     * @param dataList       海信考勤数据列表
     * @param targetDate     目标时间
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
     * 分组
     *
     * @param routeNameList
     * @param tenantId
     * @return
     */
    private Map<String, TCMRoute> groupRouteMapByPlateNumber(List<String> routeNameList, String tenantId) {

        LambdaQueryCondition<TCMRoute> routeCondition = new LambdaQueryCondition<>(new TCMRoute());
        routeCondition.in(TCMRoute::getRoute_code, routeNameList)
                .eq(TCMRoute::getYtenantId, tenantId)
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
    private List<HiSenseAttendanceDataDTO> getHiSenseDataByRunData(String runDate) {
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
