package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.ucf.mdf.sample.bill.entity.dto.DailyWorkRecordDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.EmployeeInfoDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.WorkReportRenderDTO;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.service.WorkReportService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QueryOrderby;
import org.imeta.orm.schema.QuerySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作资料报表服务实现（假数据版本）
 * @author aze
 */
@Service
public class WorkReportServiceImpl implements WorkReportService {

    @Autowired
    protected IBillQueryRepository queryService;

    private static final SimpleDateFormat TIME_FIELD_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
    private static final SimpleDateFormat HM_FORMAT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat YM_FORMAT = new SimpleDateFormat("yyyy-MM");
    private static final SimpleDateFormat YMD_STRICT = new SimpleDateFormat("yyyy-MM-dd");
    private static final String[] WEEK_NAMES = {"日", "一", "二", "三", "四", "五", "六"};
    static {
        YM_FORMAT.setLenient(false);
        YMD_STRICT.setLenient(false);
    }

    @Override
    public List<WorkReportRenderDTO> getWorkReportData(String employeeId, String yearMonth) {
        // 校验并标准化月份参数（yyyy-MM-dd → yyyy-MM）
        String normalizedMonth = validateAndNormalizeYearMonth(yearMonth);

        // 查询修正信息（如果指定了员工则只查该员工）
        List<Map<String, Object>> correctionData = queryCorrectionData(normalizedMonth, employeeId);

        // 分组构建
        Map<String, List<Map<String, Object>>> correctionMap = correctionData.stream().collect(Collectors.groupingBy(
                corrRecord -> String.valueOf(corrRecord.get("employee_id"))
        ));
        List<WorkReportRenderDTO> result = new ArrayList<>();
        correctionMap.forEach((empId, corrRecords) ->
                result.add(buildSingleReport(corrRecords, yearMonth)));
        return result;
    }

    @Override
    public List<Map<String, Object>> queryAllCorrectionData(String yearMonth) {
        String normalizedMonth = validateAndNormalizeYearMonth(yearMonth);
        String[] monthRange = getMonthRange(normalizedMonth);
        String selectFields = "date,employee_id,employee_id.code as employee_code,employee_id.name as employee_name,driver_name," +
                "employee_id.postId.name as post_name,employee_id.deptId.name as dept_name," +
                "jl_one_departure_time,jl_one_return_time,jl_two_departure_time,jl_two_return_time," +
                "overtime_time,late_night_duration,late_and_early_leave_duration," +
                "vehicle_handover_count,refuel_count,gas_refill_count,shift_change_allowance," +
                "substitute_shift_type,is_night_shift";
        QuerySchema querySchema = QuerySchema.create().addSelect(selectFields);
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        querySchema.appendQueryCondition(QueryCondition.name("date").egt(monthRange[0]));
        querySchema.appendQueryCondition(QueryCondition.name("date").lt(monthRange[1]));
        querySchema.addOrderBy(new QueryOrderby("employee_id", "asc"));
        querySchema.addOrderBy(new QueryOrderby("date", "asc"));
        return queryService.queryMapBySchema(
                UrlEnum.ATTENDANCE_CORRECTION.getUrl(),
                querySchema,
                UrlEnum.ATTENDANCE_CORRECTION.getDomainKey()
        );
    }

    @Override
    public WorkReportRenderDTO buildSingleReport(List<Map<String, Object>> corrRecords, String yearMonth) {
        EmployeeInfoDTO empInfo = new EmployeeInfoDTO();
        Map<String, Object> map = corrRecords.get(0);
        empInfo.setName((String) map.get("employee_name"));
        empInfo.setDepartment((String) map.get("dept_name"));
        empInfo.setCode((String) map.get("employee_code"));
        empInfo.setPosition((String) map.get("post_name"));
        empInfo.setMonth(yearMonth);

        List<DailyWorkRecordDTO> dailyRecords = new ArrayList<>();
        corrRecords.forEach(corrRecord -> {
            dailyRecords.add(createRecord(
                    getWeekDay(getStr(corrRecord, "date")),
                    getStr(corrRecord, "date"),
                    getStr(corrRecord, "attendance_type"),
                    calcWorkPeriod(
                            getStr(corrRecord, "jl_one_departure_time"),
                            getStr(corrRecord, "jl_one_return_time"),
                            getStr(corrRecord, "jl_two_departure_time"),
                            getStr(corrRecord, "jl_two_return_time")),
                    getStr(corrRecord, "overtime_time"),
                    getStr(corrRecord, "late_night_duration"),
                    getStr(corrRecord, "late_and_early_leave_duration"),
                    getInteger(corrRecord, "vehicle_handover_count"),
                    getInteger(corrRecord, "refuel_count"),
                    getInteger(corrRecord, "gas_refill_count"),
                    toYesNo(getStr(corrRecord, "shift_change_allowance")),
                    getStr(corrRecord, "substitute_shift_type"),
                    toYesNo(getStr(corrRecord, "is_night_shift"))
            ));
        });
        return new WorkReportRenderDTO(empInfo, dailyRecords);
    }

    /**
     * 查询当月有修正数据的所有员工ID（轻量查询）
     */
    @Override
    public List<String> queryDistinctEmployeeIds(String yearMonth) {
        String normalizedMonth = validateAndNormalizeYearMonth(yearMonth);
        String[] monthRange = getMonthRange(normalizedMonth);
        QuerySchema querySchema = QuerySchema.create().addSelect("employee_id");
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        querySchema.appendQueryCondition(QueryCondition.name("date").egt(monthRange[0]));
        querySchema.appendQueryCondition(QueryCondition.name("date").lt(monthRange[1]));
        List<Map<String, Object>> idList = queryService.queryMapBySchema(
                UrlEnum.ATTENDANCE_CORRECTION.getUrl(),
                querySchema,
                UrlEnum.ATTENDANCE_CORRECTION.getDomainKey()
        );
        return idList.stream()
                .map(m -> String.valueOf(m.get("employee_id")))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     *
     * @param yearMonth
     * @param employeeId
     * @return
     */
    private List<Map<String, Object>> queryCorrectionData(String yearMonth, String employeeId) {
        // 查詢字段
        String selectFields = "date,employee_id,employee_id.code as employee_code,employee_id.name as employee_name,driver_name," +
                "employee_id.postId.name as post_name,employee_id.deptId.name as dept_name," +
                "jl_one_departure_time,jl_one_return_time,jl_two_departure_time,jl_two_return_time," +
                "overtime_time,late_night_duration,late_and_early_leave_duration," +
                "vehicle_handover_count,refuel_count,gas_refill_count,shift_change_allowance," +
                "substitute_shift_type,is_night_shift";
        QuerySchema querySchema = QuerySchema.create().addSelect(selectFields);
        // 查詢條件
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        String[] monthRange = getMonthRange(yearMonth);
        querySchema.appendQueryCondition(QueryCondition.name("date").egt(monthRange[0]));
        querySchema.appendQueryCondition(QueryCondition.name("date").lt(monthRange[1]));
        if (employeeId != null && !employeeId.isEmpty()) {
            querySchema.appendQueryCondition(QueryCondition.name("employee_id").eq(employeeId));
        }
        querySchema.addOrderBy(new QueryOrderby("date", "asc"));
        return queryService.queryMapBySchema(
                UrlEnum.ATTENDANCE_CORRECTION.getUrl(),
                querySchema,
                UrlEnum.ATTENDANCE_CORRECTION.getDomainKey()
        );
    }

    /**
     * 根据 yyyy-MM-dd 日期计算星期几（中文）
     * @param dateStr 如 "2026-04-01"
     * @return 如 "三"，解析失败返回 ""
     */
    private String getWeekDay(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(DATE_FORMAT.parse(dateStr));
            return WEEK_NAMES[cal.get(Calendar.DAY_OF_WEEK) - 1];
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * 从Map安全获取String值（null → ""）
     */
    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    /**
     * 从Map安全获取int值（null/非数字 → 0）
     */
    private int getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 0;
    }

    /**
     * 从Map安全获取Integer值（null/非数字 → null，有数据才返回数值）
     */
    private Integer getInteger(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return null;
    }

    /**
     * 布尔字符串转中文：true→是，false→否，null/空→""
     */
    private String toYesNo(String boolStr) {
        if (boolStr == null || boolStr.isEmpty()) {
            return "";
        }
        return "true".equalsIgnoreCase(boolStr) ? "是" : "否";
    }

    /**
     * 计算工作時數：(jl_one_return_time - jl_one_departure_time) + (jl_two_return_time - jl_two_departure_time)
     * 每对必须两个值都存在才计入，否则该对返回0
     * @return "HH:mm" 格式时长，无有效数据返回 ""
     */
    private String calcWorkPeriod(String depart1, String return1, String depart2, String return2) {
        int totalMinutes = calcPairMinutes(depart1, return1) + calcPairMinutes(depart2, return2);
        return totalMinutes > 0 ? String.format("%02d:%02d", totalMinutes / 60, totalMinutes % 60) : "";
    }

    private int calcPairMinutes(String departure, String returnTime) {
        if (departure == null || departure.isEmpty() || returnTime == null || returnTime.isEmpty()) {
            return 0;
        }
        try {
            Calendar depCal = Calendar.getInstance();
            depCal.setTime(TIME_FIELD_FORMAT.parse(departure));
            int depMin = depCal.get(Calendar.HOUR_OF_DAY) * 60 + depCal.get(Calendar.MINUTE);

            Calendar retCal = Calendar.getInstance();
            retCal.setTime(TIME_FIELD_FORMAT.parse(returnTime));
            int retMin = retCal.get(Calendar.HOUR_OF_DAY) * 60 + retCal.get(Calendar.MINUTE);

            return Math.max(0, retMin - depMin);
        } catch (ParseException e) {
            return 0;
        }
    }

    private DailyWorkRecordDTO createRecord(String week, String date, String attendanceType,
            String workPeriod, String overtime, String lateNight, String earlyLeave,
            Integer ticketCount, Integer oilCount, Integer gasCount,
            String upperLowerShift, String topShift, String nightShift) {
        DailyWorkRecordDTO r = new DailyWorkRecordDTO();
        r.setWeek(week);
        r.setDate(date);
        r.setAttendanceType(attendanceType);
        r.setWorkPeriod(workPeriod);
        r.setOvertime(overtime);
        r.setLateNight(lateNight);
        r.setEarlyLeave(earlyLeave);
        r.setTicketCount(ticketCount);
        r.setOilCount(oilCount);
        r.setGasCount(gasCount);
        r.setUpperLowerShift(upperLowerShift);
        r.setTopShift(topShift);
        r.setNightShift(nightShift);
        return r;
    }

    /**
     * 校验并标准化月份参数：yyyy-MM-dd → yyyy-MM，非日期格式报错
     */
    private String validateAndNormalizeYearMonth(String yearMonth) {
        if (yearMonth == null || yearMonth.trim().isEmpty()) {
            throw new IllegalArgumentException("月份参数不能为空");
        }
        yearMonth = yearMonth.trim();
        // 尝试 yyyy-MM 格式
        try {
            YM_FORMAT.parse(yearMonth);
            return yearMonth;
        } catch (ParseException ignored) {
        }
        // 尝试 yyyy-MM-dd 格式
        try {
            YMD_STRICT.parse(yearMonth);
            return yearMonth.substring(0, 7);
        } catch (ParseException ignored) {
        }
        throw new IllegalArgumentException(
                "月份参数格式错误，请使用 yyyy-MM 或 yyyy-MM-dd 格式，当前值: " + yearMonth);
    }

    /**
     * 根据日期计算当月范围，返回 [当月第一天, 下月第一天]
     * 支持 yyyy-MM-dd 和 yyyy-MM 两种输入
     */
    private String[] getMonthRange(String dateStr) {
        try {
            Calendar cal = Calendar.getInstance();
            // 尝试 yyyy-MM-dd 解析，失败则回退到 yyyy-MM
            try {
                cal.setTime(DATE_FORMAT.parse(dateStr));
            } catch (ParseException e) {
                cal.setTime(YM_FORMAT.parse(dateStr));
            }
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String startStr = DATE_FORMAT.format(cal.getTime());
            cal.add(Calendar.MONTH, 1);
            String endStr = DATE_FORMAT.format(cal.getTime());
            return new String[]{startStr, endStr};
        } catch (ParseException e) {
            return new String[]{dateStr, dateStr};
        }
    }
}
