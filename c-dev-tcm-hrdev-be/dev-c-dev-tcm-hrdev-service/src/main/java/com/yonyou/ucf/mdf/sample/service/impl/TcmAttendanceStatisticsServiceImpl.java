package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceStatistics;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceStatisticsService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.imeta.core.base.ConditionOperator;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QuerySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 考勤统计服务实现类
 * @author aze
 */
@Service
public class TcmAttendanceStatisticsServiceImpl implements TcmAttendanceStatisticsService {

    @Autowired
    private IBillRepository billRepository;

    @Autowired
    protected IBillQueryRepository queryService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 根据月份获取考勤统计信息
     * 1. 计算当月日期范围（当月第一天 ~ 下月第一天）
     * 2. 查询当月所有修正数据，按employee_id分组
     * 3. 查询当月已有统计记录，按person分组建Map
     * 4. 对在修正表中存在但统计表中不存在的员工，生成新的统计记录
     * 5. 批量插入统计记录
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> getTcmAttendanceStatisticsInfoByMonth(Date date) {
        // 计算当月日期范围
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        String startStr = String.format("%04d-%02d-01", year, month + 1);
        // 下月第一天
        int nextMonth = month + 1;
        int nextYear = year;
        if (nextMonth == 12) {
            nextYear = year + 1;
            nextMonth = 0;
        }
        String endStr = String.format("%04d-%02d-01", nextYear, nextMonth + 1);

        String tenantId = InvocationInfoProxy.getTenantid();

        // 查询修正数据
        List<Map<String, Object>> correctionData = queryMonthCorrectionData(startStr, endStr);

        // 按employee_id分组
        Map<String, List<Map<String, Object>>> correctionMap = new LinkedHashMap<>();
        if (correctionData != null && !correctionData.isEmpty()) {
            for (Map<String, Object> cor : correctionData) {
                String empId = getStringValue(cor, "employee_id");
                if (empId != null && !empId.isEmpty()) {
                    correctionMap.computeIfAbsent(empId, k -> new ArrayList<>()).add(cor);
                }
            }
        }

        // 查询统计表已有数据
        List<Map<String, Object>> stanData = queryMonthStatisticsData(startStr, endStr);
        Map<String, Map<String, Object>> staMap = new HashMap<>();
        if (stanData != null && !stanData.isEmpty()) {
            for (Map<String, Object> sta : stanData) {
                String perId = getStringValue(sta, "person");
                if (perId != null && !perId.isEmpty()) {
                    staMap.put(perId, sta);
                }
            }
        }

        // 生成需要新增的统计记录
        List<TcmAttendanceStatistics> addList = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : correctionMap.entrySet()) {
            String empId = entry.getKey();
            // 如果该员工在统计表中不存在，则生成一条统计记录
            if (!staMap.containsKey(empId)) {
                List<Map<String, Object>> corrections = entry.getValue();
                if (corrections != null && !corrections.isEmpty()) {
                    Map<String, Object> cor = corrections.get(0);

                    TcmAttendanceStatistics newStat = new TcmAttendanceStatistics();
                    newStat.setNew1(getStringValue(cor, "date"));
                    newStat.setPosition(getStringValue(cor, "postName"));
                    newStat.setPerson(getStringValue(cor, "driver_name"));
                    newStat.setExecutor(null);
                    newStat.setArchiveStatus_C("2");
                    newStat.setYtenantId(tenantId);

                    addList.add(newStat);
                }
            }
        }

        // 批量插入
        List<TcmAttendanceStatistics> insertResult = null;
        if (!addList.isEmpty()) {
            billRepository.insert(addList);
            insertResult = addList;
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("correctionData", correctionData);
        result.put("addList", addList);
        result.put("insertResult", insertResult);
        return result;
    }

    /**
     * 查询当月修正数据（含 postName）
     */
    private List<Map<String, Object>> queryMonthCorrectionData(String startStr, String endStr) {
        String selectFields = "date,employee_id,employee_id.code as employee_code," +
                "employee_id.postId.name as postName,driver_name,task_code," +
                "jl_valid_time,overtime_time,late_and_early_leave_duration,meal_count,meal_duration," +
                "vehicle_handover_count,refuel_count,gas_refill_count,shift_change_allowance," +
                "is_night_shift,late_night_duration,substitute_shift_type,late_and_early_leave_count," +
                "is_working_on_weekly_holiday,minibus_driving_hours,medium_bus_driving_hours," +
                "large_bus_driving_hours,extra_large_bus_driving_hours," +
                "pg_one_departure_time,pg_one_return_time,pg_two_departure_time,pg_two_return_time";

        QuerySchema querySchema = QuerySchema.create().addSelect(selectFields);
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        querySchema.appendQueryCondition(new QueryCondition("date", ConditionOperator.egt, startStr));
        querySchema.appendQueryCondition(new QueryCondition("date", ConditionOperator.lt, endStr));

        return queryService.queryMapBySchema(
                UrlEnum.ATTENDANCE_CORRECTION.getUrl(),
                querySchema,
                UrlEnum.ATTENDANCE_CORRECTION.getDomainKey()
        );
    }

    /**
     * 查询当月统计表数据
     */
    private List<Map<String, Object>> queryMonthStatisticsData(String startStr, String endStr) {
        String selectFields = "new1,position,person,executor,archiveStatus_C";

        QuerySchema querySchema = QuerySchema.create().addSelect(selectFields);
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        querySchema.appendQueryCondition(new QueryCondition("new1", ConditionOperator.egt, startStr));
        querySchema.appendQueryCondition(new QueryCondition("new1", ConditionOperator.lt, endStr));

        return queryService.queryMapBySchema(
                UrlEnum.ATTENDANCE_STATISTICS.getUrl(),
                querySchema,
                UrlEnum.ATTENDANCE_STATISTICS.getDomainKey()
        );
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
}
