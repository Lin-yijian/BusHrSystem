package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCalculation;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.service.AttendanceCorrectionSyncService;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCalculationService;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ucf.mdf.sample.utils.DateUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import lombok.extern.slf4j.Slf4j;
import org.imeta.core.base.ConditionOperator;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QuerySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日考勤結果匯總服务实现
 * @author aze
 */
@Slf4j
@Service
public class TcmAttendanceCalculationServiceImpl implements TcmAttendanceCalculationService {

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    protected IBillQueryRepository queryService;

    @Autowired
    private AttendanceCorrectionSyncService correctionSyncService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> getAndUpdateAssessReviseByDate(String dateStr) {
        // 单日全量（生成排更/海信拉取/导入收尾）
        return getAndUpdateAssessReviseByDateInternal(dateStr, null, true);
    }

    /**
     * 根据日期和员工代码获取并更新考勤计算结果
     * employeeCode 为空时按「日期所在月」范围同步（兼容原手工接口）
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> getAndUpdateAssessReviseByDate(Date date, String employeeCode) {
        String dateStr = formatDate(date);
        boolean singleDate = employeeCode != null && !employeeCode.isEmpty();
        return getAndUpdateAssessReviseByDateInternal(dateStr, employeeCode, singleDate);
    }

    /**
     * @param singleDate true=仅该日；false=该日所在整月（employeeCode 必须为空）
     */
    private Map<String, Object> getAndUpdateAssessReviseByDateInternal(String dateStr, String employeeCode, boolean singleDate) {
        long t0 = System.currentTimeMillis();
        String tenantId = InvocationInfoProxy.getTenantid();

        long step = t0;
        correctionSyncService.refreshCorrectionsBeforeCalc(dateStr, employeeCode, singleDate);
        log.warn("[KQ_CALC] date={} employee={} step=refreshCorrections costMs={}",
                dateStr, employeeCode, System.currentTimeMillis() - step);

        step = System.currentTimeMillis();
        List<Map<String, Object>> correctionData = queryCorrectionData(dateStr, employeeCode, tenantId, singleDate);
        List<TcmAttendanceCalculation> calData = queryCalculationData(dateStr, employeeCode, tenantId, singleDate);
        log.warn("[KQ_CALC] date={} employee={} step=query corr={} calc={} costMs={}",
                dateStr, employeeCode,
                correctionData != null ? correctionData.size() : 0,
                calData != null ? calData.size() : 0,
                System.currentTimeMillis() - step);

        // 构建计算数据Map，key为employee_id
        Map<String, TcmAttendanceCalculation> calMap = new HashMap<>();
        if (calData != null && !calData.isEmpty()) {
            for (TcmAttendanceCalculation rec : calData) {
                String empId = rec.getEmployee_id();
                if (empId != null) {
                    calMap.put(empId, rec);
                }
            }
        }

        // 遍历修正数据，映射到计算表
        List<TcmAttendanceCalculation> newCalList = new ArrayList<>();
        List<TcmAttendanceCalculation> updateCalList = new ArrayList<>();

        if (correctionData != null && !correctionData.isEmpty()) {
            for (Map<String, Object> corrRecord : correctionData) {
                String empCode = getStringValue(corrRecord, "employee_code");
                if (empCode == null || empCode.isEmpty()) {
                    continue;
                }

                TcmAttendanceCalculation existing = calMap.get(empCode);
                TcmAttendanceCalculation newCalRecord = new TcmAttendanceCalculation();

                // 映射字段：修正表 → 计算表
                newCalRecord.setDate(getStringValue(corrRecord, "date"));
                newCalRecord.setEmployee_id(empCode);
                newCalRecord.setName(getStringValue(corrRecord, "driver_name"));
                newCalRecord.setTask_code(getStringValue(corrRecord, "task_code"));
                newCalRecord.setOvertime_hours(getStringValue(corrRecord, "overtime_time"));
                newCalRecord.setLate_early_leave_duration(getStringValue(corrRecord, "late_and_early_leave_duration"));
                newCalRecord.setMeal_count(getIntegerValue(corrRecord, "meal_count"));
                newCalRecord.setMeal_duration(getStringValue(corrRecord, "meal_duration"));
                newCalRecord.setVehicle_handover_count(getIntegerValue(corrRecord, "vehicle_handover_count"));
                newCalRecord.setRefuel_count(getIntegerValue(corrRecord, "refuel_count"));
                newCalRecord.setGas_refill_count(getIntegerValue(corrRecord, "gas_refill_count"));
                newCalRecord.setShift_allowance(getBooleanValue(corrRecord, "shift_change_allowance"));
                newCalRecord.setNight_shift_allowance(getBooleanValue(corrRecord, "is_night_shift"));
                newCalRecord.setLate_night_duration(getStringValue(corrRecord, "late_night_duration"));
                newCalRecord.setSubstitute_shift_type(getStringValue(corrRecord, "substitute_shift_type"));
                newCalRecord.setLate_and_early_leave_count(getIntegerValue(corrRecord, "late_and_early_leave_count"));
                newCalRecord.setIs_working_on_weekly_holiday(getBooleanValue(corrRecord, "is_working_on_weekly_holiday"));
                newCalRecord.setMinibus_driving_hours(getStringValue(corrRecord, "minibus_driving_hours"));
                newCalRecord.setMedium_bus_driving_hours(getStringValue(corrRecord, "medium_bus_driving_hours"));
                newCalRecord.setLarge_bus_driving_hours(getStringValue(corrRecord, "large_bus_driving_hours"));
                newCalRecord.setExtra_large_bus_driving_hours(getStringValue(corrRecord, "extra_large_bus_driving_hours"));

                // 计算标准工时
                String stdHoursStr = calcStandardWorkHours(corrRecord);
                newCalRecord.setStandard_work_hours(stdHoursStr);

                // 计算总工时 = 标准工时(秒) + 加班时长(秒)
                int stdSeconds = timeToSeconds(stdHoursStr);
                int overtimeSeconds = timeToSeconds(newCalRecord.getOvertime_hours());
                int totalSeconds = stdSeconds + overtimeSeconds;
                newCalRecord.setTotal_work_hours(secondsToTime(totalSeconds));

                // 处理计算表特有字段
                if (existing != null) {
                    // 保留原有特殊字段和乐观锁版本，避免批量删除并发 UPDATE 触发 PolarDB MVCC
                    newCalRecord.setDepartment_summary(existing.getDepartment_summary());
                    newCalRecord.setAttendance_type(existing.getAttendance_type());
                    newCalRecord.setWork_time(existing.getWork_time());
                    newCalRecord.setLate_early_leave_period(existing.getLate_early_leave_period());
                    newCalRecord.setId(existing.getId());
                    newCalRecord.setYtenantId(existing.getYtenantId());
                    newCalRecord.setDr(existing.getDr());
                    newCalRecord.setCreator(existing.getCreator());
                    newCalRecord.setCreateTime(existing.getCreateTime());
                    newCalRecord.setPubts(existing.getPubts());
                    updateCalList.add(newCalRecord);
                } else {
                    // 新记录设置租户ID
                    newCalRecord.setYtenantId(tenantId);
                    newCalList.add(newCalRecord);
                }
            }
        }

        // 批量新增和更新
        Map<String, Object> result = new HashMap<>();
        if (!newCalList.isEmpty()) {
            CommonUtil.fillInsertAudit(newCalList);
            billRepository.insert(newCalList);
            result.put("insertCount", newCalList.size());
        }
        if (!updateCalList.isEmpty()) {
            CommonUtil.fillUpdateAudit(updateCalList);
            billRepository.update(updateCalList);
            result.put("updateCount", updateCalList.size());
        }
        result.put("correctionCount", correctionData != null ? correctionData.size() : 0);
        log.warn("[KQ_CALC] date={} employee={} step=total insert={} update={} costMs={}",
                dateStr, employeeCode,
                newCalList.size(), updateCalList.size(),
                System.currentTimeMillis() - t0);
        return result;
    }

    /**
     * 查询修正数据
     * @param singleDate true 仅该日；false 且无 employeeCode 时按月
     */
    private List<Map<String, Object>> queryCorrectionData(String dateStr, String employeeCode, String tenantId, boolean singleDate) {
        String selectFields = "date,employee_id,employee_id.code as employee_code,driver_name,task_code," +
                "jl_valid_time,overtime_time,late_and_early_leave_duration,meal_count,meal_duration," +
                "vehicle_handover_count,refuel_count,gas_refill_count,shift_change_allowance," +
                "is_night_shift,late_night_duration,substitute_shift_type,late_and_early_leave_count," +
                "is_working_on_weekly_holiday,minibus_driving_hours,medium_bus_driving_hours," +
                "large_bus_driving_hours,extra_large_bus_driving_hours," +
                "pg_one_departure_time,pg_one_return_time,pg_two_departure_time,pg_two_return_time";

        QuerySchema querySchema = QuerySchema.create().addSelect(selectFields);
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        querySchema.appendQueryCondition(QueryCondition.name("ytenantId").eq(tenantId));
        if (employeeCode != null && !employeeCode.isEmpty()) {
            querySchema.appendQueryCondition(new QueryCondition<>("date", ConditionOperator.in, DateUtil.dateQueryVariants(dateStr)));
            querySchema.appendQueryCondition(QueryCondition.name("employee_id.code").eq(employeeCode));
        } else if (singleDate) {
            querySchema.appendQueryCondition(new QueryCondition<>("date", ConditionOperator.in, DateUtil.dateQueryVariants(dateStr)));
        } else {
            String[] monthRange = getMonthRange(dateStr);
            querySchema.appendQueryCondition(QueryCondition.name("date").egt(monthRange[0]));
            querySchema.appendQueryCondition(QueryCondition.name("date").lt(monthRange[1]));
        }

        return queryService.queryMapBySchema(
                UrlEnum.ATTENDANCE_CORRECTION.getUrl(),
                querySchema,
                UrlEnum.ATTENDANCE_CORRECTION.getDomainKey()
        );
    }

    /**
     * 查询计算数据
     */
    private List<TcmAttendanceCalculation> queryCalculationData(String dateStr, String employeeCode, String tenantId, boolean singleDate) {
        LambdaQueryCondition<TcmAttendanceCalculation> condition =
                new LambdaQueryCondition<>(new TcmAttendanceCalculation());
        condition.eq(TcmAttendanceCalculation::getDr, BaseEnum.IS_NOT_DELETED.getIntCode())
                .eq(TcmAttendanceCalculation::getYtenantId, tenantId);
        if (employeeCode != null && !employeeCode.isEmpty()) {
            condition.in(TcmAttendanceCalculation::getDate, DateUtil.dateQueryVariants(dateStr))
                    .eq(TcmAttendanceCalculation::getEmployee_id, employeeCode);
        } else if (singleDate) {
            condition.in(TcmAttendanceCalculation::getDate, DateUtil.dateQueryVariants(dateStr));
        } else {
            String[] monthRange = getMonthRange(dateStr);
            condition.ge(TcmAttendanceCalculation::getDate, monthRange[0])
                    .lt(TcmAttendanceCalculation::getDate, monthRange[1]);
        }
        return billRepository.queryByClause(condition);
    }

    /**
     * 格式化Date为yyyy-MM-dd字符串
     */
    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return DATE_FORMAT.format(date);
    }

    /**
     * 根据日期计算当月范围，返回 [当月第一天, 下月第一天]
     * 如输入 2026-04-13 → [2026-04-01, 2026-05-01]
     * 如输入 2026-12-25 → [2026-12-01, 2027-01-01]
     */
    private String[] getMonthRange(String dateStr) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(DATE_FORMAT.parse(dateStr));
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String startStr = DATE_FORMAT.format(cal.getTime());
            cal.add(Calendar.MONTH, 1);
            String endStr = DATE_FORMAT.format(cal.getTime());
            return new String[]{startStr, endStr};
        } catch (ParseException e) {
            return new String[]{dateStr, dateStr};
        }
    }

    /**
     * 解析时间字符串为毫秒时间戳
     */
    private long parseTime(String str) {
        if (str == null || str.isEmpty()) {
            return 0L;
        }
        try {
            // 优先解析日期时间格式，避免日期格式在宽松模式下截断时间部分
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(str).getTime();
        } catch (ParseException e) {
            // 再尝试解析纯日期格式
            try {
                return DATE_FORMAT.parse(str).getTime();
            } catch (ParseException ex) {
                return 0L;
            }
        }
    }

    /**
     * 将HH:MM:SS格式转为秒数
     */
    private int timeToSeconds(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        String[] parts = str.split(":");
        if (parts.length == 3) {
            return Integer.parseInt(parts[0]) * 3600
                    + Integer.parseInt(parts[1]) * 60
                    + Integer.parseInt(parts[2]);
        }
        return 0;
    }

    /**
     * 将秒数转为HH:MM:SS格式
     */
    private String secondsToTime(int totalSeconds) {
        if (totalSeconds < 0) {
            totalSeconds = 0;
        }
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * 计算标准工时
     * 第一段：pg_one_departure_time → pg_one_return_time
     * 第二段：pg_two_departure_time → pg_two_return_time
     */
    private String calcStandardWorkHours(Map<String, Object> record) {
        long totalMs = 0L;

        // 第一段
        long d1 = getTimeMs(record, "pg_one_departure_time");
        long r1 = getTimeMs(record, "pg_one_return_time");
        if (d1 > 0 && r1 > 0 && r1 > d1) {
            totalMs += (r1 - d1);
        }

        // 第二段
        long d2 = getTimeMs(record, "pg_two_departure_time");
        long r2 = getTimeMs(record, "pg_two_return_time");
        if (d2 > 0 && r2 > 0 && r2 > d2) {
            totalMs += (r2 - d2);
        }

        int totalSeconds = (int) (totalMs / 1000);
        return secondsToTime(totalSeconds);
    }

    /**
     * 从Map中提取时间字段的毫秒值
     * 兼容 Date 对象和字符串格式
     */
    private long getTimeMs(Map<String, Object> record, String key) {
        Object value = record.get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        return parseTime(value.toString());
    }

    /**
     * 从Map中获取String值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * 从Map中获取Integer值
     */
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从Map中获取Boolean值
     */
    private Boolean getBooleanValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String str = value.toString();
        return "true".equalsIgnoreCase(str) || "1".equals(str);
    }
}
