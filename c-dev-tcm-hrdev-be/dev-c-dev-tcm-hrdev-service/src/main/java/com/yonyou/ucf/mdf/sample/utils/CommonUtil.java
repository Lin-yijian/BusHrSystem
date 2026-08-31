package com.yonyou.ucf.mdf.sample.utils;

import com.yonyou.iuap.yms.param.SQLParameter;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.enums.params.TcmRosteringParamKeyEnum;
import com.yonyou.ucf.mdf.sample.service.TcmParamManageService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QuerySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CommonUtil {

    @Autowired
    protected IBillQueryRepository queryService;

    @Autowired
    protected IBillRepository billRepository;

    @Autowired
    private TcmParamManageService tcmParamManageService;

    public final String FIELD_CODE_WAYBILL_EMPLOYEE_ID = "waybill_employeeId";
    public final String FIELD_CODE_EMPLOYEE_ID = "code";
    public final String FIELD_CODE_POST_CODE = "postCode";
    public final String FIELD_CODE_STAFF_ID = "id";
    public final String FIELD_CODE_STAFF_AB_09 = "diverType";


    /**
     * 实时查询指定参数 key 的 value（每次走 DB，不缓存），若不存在或空白则抛 RuntimeException。
     *
     * @param keyEnum 参数键枚举（含 name、code）
     * @return 非空 value
     */
    private String requireRosterParam(TcmRosteringParamKeyEnum keyEnum) {
        String code = keyEnum.getCode();
        String displayName = keyEnum.getName();
        String value = tcmParamManageService.queryModuleParamValueNullable(
                ModuleEnum.TCM_SHEDULE_SHIFTS, code);
        if (StringUtils.isBlank(value)) {
            throw new RuntimeException(
                    "配置缺失：" + displayName + "[" + code + "]，請至【參數管理】菜單補錄");
        }
        return value;
    }

    /** @return 兼职司机岗位编码 — TCMParamManage.key = {@link TcmRosteringParamKeyEnum#PART_TIME_JOB_CODE} */
    public String getPartTimeJobCode() {
        return requireRosterParam(TcmRosteringParamKeyEnum.PART_TIME_JOB_CODE);
    }

    /** @return 深宵司机枚举编号 — TCMParamManage.key = {@link TcmRosteringParamKeyEnum#NIGHT_DIVER_TYPE} */
    public String getNightDriverType() {
        return requireRosterParam(TcmRosteringParamKeyEnum.NIGHT_DIVER_TYPE);
    }

    /** @return 非深宵司机标准工时 — TCMParamManage.key = {@link TcmRosteringParamKeyEnum#WORK_TIME} */
    public String getWorkTime() {
        return requireRosterParam(TcmRosteringParamKeyEnum.WORK_TIME);
    }

    /** @return 深宵司机标准工时 — TCMParamManage.key = {@link TcmRosteringParamKeyEnum#NIGHT_WORK_TIME} */
    public String getNightWorkTime() {
        return requireRosterParam(TcmRosteringParamKeyEnum.NIGHT_WORK_TIME);
    }


    /** @return 遲到判定閾值 — TCMParamManage.key = {@link TcmRosteringParamKeyEnum#NIGHT_WORK_TIME} */
    public String getLateMin() {
        return requireRosterParam(TcmRosteringParamKeyEnum.LATE_THRESHOLD_MINUTES);
    }


    /**
     * 根据澳巴工号获取员工信息
     * @param ids 澳巴工号集合
     * @param tenantId 租户ID
     * @return k->澳巴工号   v->员工信息ID
     */
    public Map<String,String> getStaffByCardIds(Collection<String> ids, String tenantId) {

        List<Map<String, Object>> mapList = getStaffByEmployeeIds(ids,tenantId);

        if (mapList == null || mapList.isEmpty()) {
            return new HashMap<>();
        }

        Map<String,String> finalMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            finalMap.put((String) map.get(FIELD_CODE_WAYBILL_EMPLOYEE_ID),(String) map.get(FIELD_CODE_STAFF_ID));
        }

        return finalMap;

    }

    /**
     * 根据澳巴工号获取员工信息
     * @param ids 澳巴工号集合
     * @param tenantId 租户ID
     * @param tenantId 租户ID
     * @param fieldKeyName 键字段名
     * @param fieldValueName 值字段名
     * @return k->自定义内容（键字段名）   v->自定义内容（值字段名）
     */
    public Map<String,String> getStaffMapByCardIds(Collection<String> ids, String tenantId, String fieldKeyName, String fieldValueName) {

        List<Map<String, Object>> mapList = getStaffByEmployeeIds(ids,tenantId);

        if (mapList == null || mapList.isEmpty()) {
            return new HashMap<>();
        }

        Map<String,String> finalMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            finalMap.put((String) map.get(fieldKeyName),(String) map.get(fieldValueName));
        }

        return finalMap;

    }


    /**
     * 根据澳巴工号获取员工信息
     * @param ids 澳巴工号集合
     * @param tenantId 租户ID
     * @param tenantId 租户ID
     * @param fieldKeyName 键字段名
     * @return k->自定义内容（键字段名）   v->信息对象
     */
    public Map<String,Map<String, Object>> getStaffMapByCardIds(Collection<String> ids, String tenantId, String fieldKeyName) {

        List<Map<String, Object>> mapList = getStaffByEmployeeIds(ids,tenantId);

        if (mapList == null || mapList.isEmpty()) {
            return new HashMap<>();
        }

        Map<String,Map<String, Object>> finalMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            finalMap.put((String) map.get(fieldKeyName),map);
        }

        return finalMap;

    }


    /**
     * 根据澳巴工号查询员工信息
     * @param ids 澳巴工号集合
     * @param tenantId 租户ID
     * @return .
     */
    public List<Map<String, Object>> getStaffByEmployeeIds(Collection<String> ids, String tenantId) {
        QuerySchema querySchema = QuerySchema.create().addSelect("id, staffDefines.ab_01 as waybill_employeeId, staffDefines.ab_09 as diverType, postId, postId.code as postCode,code");
        querySchema.appendQueryCondition(QueryCondition.name("staffDefines.ab_01").in(ids));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());

        return mapList;

    }




    /**
     * 根据澳巴工号获取员工信息
     * @param id 澳巴工号
     * @param tenantId 租户ID
     * @return 员工信息ID
     */
    public String getStaffByCardId(String id, String tenantId) {
        QuerySchema querySchema = QuerySchema.create().addSelect("id, staffDefines.ab_01 as waybill_employeeId,code");
        querySchema.appendQueryCondition(QueryCondition.name("staffDefines.ab_01").eq(id));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());

        if (mapList == null || mapList.isEmpty()) {
            throw new RuntimeException("工號匹配失敗，請先錄入該工號人員信息，澳巴工號："+id);
        }

        return (String) mapList.get(0).get(FIELD_CODE_STAFF_ID);

    }



    /**
     * 根据员工主键ID获取员工编号 code
     */
    public String getStaffCodeById(String staffId, String tenantId) {
        if (staffId == null || staffId.isEmpty()) {
            return null;
        }
        QuerySchema querySchema = QuerySchema.create().addSelect("id, code");
        querySchema.appendQueryCondition(QueryCondition.name("id").eq(staffId));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(
                UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());
        if (mapList == null || mapList.isEmpty()) {
            return null;
        }
        return (String) mapList.get(0).get(FIELD_CODE_EMPLOYEE_ID);
    }

    /**
     * 根据员工主键ID批量获取员工编号
     * @return k->staffId  v->code
     */
    public Map<String, String> getStaffCodesByIds(Collection<String> staffIds, String tenantId) {
        Map<String, String> result = new HashMap<>();
        if (staffIds == null || staffIds.isEmpty()) {
            return result;
        }
        QuerySchema querySchema = QuerySchema.create().addSelect("id, code");
        querySchema.appendQueryCondition(QueryCondition.name("id").in(staffIds));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(
                UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());
        if (mapList == null || mapList.isEmpty()) {
            return result;
        }
        for (Map<String, Object> map : mapList) {
            String id = (String) map.get(FIELD_CODE_STAFF_ID);
            String code = (String) map.get(FIELD_CODE_EMPLOYEE_ID);
            if (id != null && code != null) {
                result.put(id, code);
            }
        }
        return result;
    }

    /**
     * 按员工主键 ID 批量查询员工信息。key = staff.id，供修正表 employee_id 匹配。
     */
    public Map<String, Map<String, Object>> getStaffMapByStaffIds(Collection<String> ids, String tenantId) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return result;
        }
        QuerySchema querySchema = QuerySchema.create()
                .addSelect("id, staffDefines.ab_01 as waybill_employeeId, staffDefines.ab_09 as diverType, postId, postId.code as postCode,code");
        querySchema.appendQueryCondition(QueryCondition.name("id").in(ids));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(
                UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());
        if (mapList == null || mapList.isEmpty()) {
            return result;
        }
        for (Map<String, Object> map : mapList) {
            String id = (String) map.get(FIELD_CODE_STAFF_ID);
            if (id != null) {
                result.put(id, map);
            }
        }
        return result;
    }

    /**
     * 根据员工编号获取员工信息（批量）
     * @param codes 员工编号集合
     * @param tenantId 租户ID
     * @return k->员工编号   v->员工信息ID
     */
    public Map<String,String> getStaffByCodes(List<String> codes, String tenantId) {
        if (codes == null || codes.isEmpty()) {
            return new HashMap<>();
        }

        QuerySchema querySchema = QuerySchema.create().addSelect("id, code");
        querySchema.appendQueryCondition(QueryCondition.name("code").in(codes));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());

        if (mapList == null || mapList.isEmpty()) {
            return new HashMap<>();
        }

        Map<String,String> finalMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            finalMap.put((String) map.get(FIELD_CODE_EMPLOYEE_ID), (String) map.get(FIELD_CODE_STAFF_ID));
        }

        return finalMap;
    }



    public void processDate(TcmAttendanceCorrection entity, Map<String, Map<String, Object>> staffMap){

        // 初始化状态
        if (StringUtils.isEmpty(entity.getTcmBarTypeHourStatus())){
            entity.setTcmBarTypeHourStatus(BaseEnum.AC_CHECK_STATUS_0.getCode());
        }
        if (StringUtils.isEmpty(entity.getLateStatus())){
            entity.setLateStatus(BaseEnum.AC_CHECK_STATUS_2.getCode());
        }
        if (StringUtils.isEmpty(entity.getOvertimeStatus())){
            entity.setOvertimeStatus(BaseEnum.AC_CHECK_STATUS_2.getCode());
        }
        if (StringUtils.isEmpty(entity.getAnomaly_verification())){
            entity.setAnomaly_verification(BaseEnum.NORMAL_CODE.getCode());
        }

        calculateCorrection(entity,staffMap);
    }



    private void calculateCorrection(TcmAttendanceCorrection entity, Map<String, Map<String, Object>> staffMap) {
        // 一更时长 = 一更计量排更收车 - 一更计量排更发车    二更时长 = 二更计量排更收车 - 二更计量排更发车
        String oneTimeDifference = DateUtil.getTimeDifference(entity.getJl_one_return_time(), entity.getJl_one_departure_time());
        entity.setOne_time(oneTimeDifference);

        String twoTimeDifference = DateUtil.getTimeDifference(entity.getJl_two_return_time(), entity.getJl_two_departure_time());
        entity.setTwo_time(twoTimeDifference);

        // 计量有效工时 = （一更计量排更收车 - 一更计量排更发车）+ （二更计量排更收车 - 二更计量排更发车）
        entity.setJl_valid_time(DateUtil.addTimeDifferences(oneTimeDifference, twoTimeDifference));

        // 一更早退时长 = 一更计量排更收车 - 考勤一更收车     二更早退时长 = 二更计量排更收车 - 考勤二更收车
        String oneEarlyLeaveDuration = DateUtil.getTimeDifference(entity.getJl_one_return_time(), entity.getKq_one_return_time(), false);
        entity.setOne_early_leave_duration(oneEarlyLeaveDuration);

        String twoEarlyLeaveDuration = DateUtil.getTimeDifference(entity.getJl_two_return_time(), entity.getKq_two_return_time(), false);
        entity.setTwo_early_leave_duration(twoEarlyLeaveDuration);

        // 计算迟到时长 一更遲到時長 =  考勤一更發車 - 計糧一更發車    二更遲到時長 =  考勤二更發車 - 計糧二更發車
        String oneLateDuration = DateUtil.getTimeDifference(entity.getKq_one_departure_time(), entity.getJl_one_departure_time(), false);
        entity.setOne_late_duration(oneLateDuration);

        String twoLateDuration = DateUtil.getTimeDifference(entity.getKq_two_departure_time(), entity.getJl_two_departure_time(), false);
        entity.setTwo_late_duration(twoLateDuration);
        entity.setLate_duration(DateUtil.addTimeDifferences(oneLateDuration, twoLateDuration));

        // 计算加班时长（根据员工类型）
        calculateOvertimeTime(entity, staffMap);

        // 计算超时时长（计粮 vs 考勤发收车）
        calculateOvertimeDuration(entity);

        // 计算迟到早退状态
        calculateLateEarlyStatus(entity);

        // 计算异常核对状态
        calculateAnomalyVerification(entity);
    }


    /**
     * 判断迟到是否超过阈值（10分钟）
     */
    private boolean isLateExceedsThreshold(TcmAttendanceCorrection entity) {
        String threshold = getLateMin();
        String oneLateDuration = entity.getOne_late_duration();
        String twoLateDuration = entity.getTwo_late_duration();

        return DateUtil.isTimeGreater(oneLateDuration, threshold)
                || DateUtil.isTimeGreater(twoLateDuration, threshold);
    }

    /**
     * 判断考勤数据是否完整（排更和考勤数据必须成对出现）
     */
    private boolean isAttendanceDataIncomplete(TcmAttendanceCorrection entity) {
        boolean oneDepartureMatch = (entity.getKq_one_departure_time() != null) == (entity.getPg_one_departure_time() != null);
        boolean oneReturnMatch = (entity.getKq_one_return_time() != null) == (entity.getPg_one_return_time() != null);
        boolean twoDepartureMatch = (entity.getKq_two_departure_time() != null) == (entity.getPg_two_departure_time() != null);
        boolean twoReturnMatch = (entity.getKq_two_return_time() != null) == (entity.getPg_two_return_time() != null);

        return !oneDepartureMatch || !oneReturnMatch || !twoDepartureMatch || !twoReturnMatch;
    }

    /**
     * 计算异常核对状态
     */
    private void calculateAnomalyVerification(TcmAttendanceCorrection entity) {
        // 迟到状态判断
        if (isLateExceedsThreshold(entity)) {
            entity.setLateStatus(BaseEnum.AC_CHECK_STATUS_0.getCode());
        }

        // 数据不完整判断
        if (isAttendanceDataIncomplete(entity)) {
            entity.setLateStatus(BaseEnum.AC_CHECK_STATUS_0.getCode());
            entity.setOvertimeStatus(BaseEnum.AC_CHECK_STATUS_0.getCode());
        }

        // 存在任何异常状态，设置异常核对标记
        if (BaseEnum.AC_CHECK_STATUS_0.getCode().equals(entity.getOvertimeStatus())
                || BaseEnum.AC_CHECK_STATUS_0.getCode().equals(entity.getLateStatus())) {
            entity.setAnomaly_verification(BaseEnum.ERROR_CODE.getCode());
        }
    }


    /**
     * 计算迟到早退状态
     */
    private void calculateLateEarlyStatus(TcmAttendanceCorrection entity) {
        String lateDuration = entity.getLate_duration();
        String oneEarlyLeaveDuration = entity.getOne_early_leave_duration();
        String twoEarlyLeaveDuration = entity.getTwo_early_leave_duration();

        boolean hasLate = !DateUtil.checkIsZeroTime(lateDuration);
        boolean hasEarlyLeave = !DateUtil.checkIsZeroTime(oneEarlyLeaveDuration) || !DateUtil.checkIsZeroTime(twoEarlyLeaveDuration);

        entity.setIs_late_or_early_leave(hasLate || hasEarlyLeave);
    }


    /**
     * 计算加班时长
     */
    private void calculateOvertimeTime(TcmAttendanceCorrection entity, Map<String, Map<String, Object>> staffMap) {
        Map<String, Object> staff = staffMap.get(entity.getEmployee_id());
        if (staff == null) {
            log.warn("未找到员工信息, employeeId: {}", entity.getEmployee_id());
            return;
        }

        String validTime = entity.getJl_valid_time();
        String postCode = (String) staff.get(FIELD_CODE_POST_CODE);
        String staffType = (String) staff.get(FIELD_CODE_STAFF_AB_09);

        // 兼职司机：标准工时不足标记为false，加班时长为0
        if (getPartTimeJobCode().equals(postCode)) {
            entity.setIs_less_than_8_hours(false);
            entity.setOvertime_time(DateUtil.HH_MM_ZERO);
            return;
        }

        // 确定标准工时（按员工类型：深宵司机 / 非深宵司机，从参数管理动态取）
        String standardWorkTime = getNightDriverType().equals(staffType) ? getNightWorkTime() : getWorkTime();

        // 计算加班时长
        if (DateUtil.isTimeLessOrEqual(validTime, standardWorkTime)) {
            entity.setOvertime_time(DateUtil.HH_MM_ZERO);
        } else {
            entity.setOvertime_time(DateUtil.subtractTimeDifferences(validTime, standardWorkTime));
        }
    }

    /**
     * 超时时长：计粮发收车 vs 考勤发收车。
     * A 两边都是一更或都是两更时按分钟规则累加；B 更次不一致时不计算，超时状态=待核对。
     */
    private void calculateOvertimeDuration(TcmAttendanceCorrection entity) {
        boolean kqOne = hasShiftData(entity.getKq_one_departure_time(), entity.getKq_one_return_time());
        boolean kqTwo = hasShiftData(entity.getKq_two_departure_time(), entity.getKq_two_return_time());
        boolean jlOne = hasShiftData(entity.getJl_one_departure_time(), entity.getJl_one_return_time());
        boolean jlTwo = hasShiftData(entity.getJl_two_departure_time(), entity.getJl_two_return_time());

        boolean kqOneOnly = kqOne && !kqTwo;
        boolean jlOneOnly = jlOne && !jlTwo;
        boolean mismatch = (kqOneOnly && jlTwo) || (kqTwo && jlOneOnly);
        if (mismatch) {
            entity.setOvertime_duration(DateUtil.HH_MM_ZERO);
            entity.setOvertimeStatus(BaseEnum.AC_CHECK_STATUS_0.getCode());
            return;
        }

        boolean bothOne = kqOneOnly && jlOneOnly;
        boolean bothTwo = kqTwo && jlTwo;
        if (!bothOne && !bothTwo) {
            entity.setOvertime_duration(DateUtil.HH_MM_ZERO);
            return;
        }

        int total = contribDeparture(entity.getJl_one_departure_time(), entity.getKq_one_departure_time())
                + contribReturn(entity.getJl_one_return_time(), entity.getKq_one_return_time())
                + contribDeparture(entity.getJl_two_departure_time(), entity.getKq_two_departure_time())
                + contribReturn(entity.getJl_two_return_time(), entity.getKq_two_return_time());
        if (total < 0) {
            total = 0;
        }
        entity.setOvertime_duration(DateUtil.minutesToHhMm(total));
        if (total >= 10) {
            entity.setOvertimeStatus(BaseEnum.AC_CHECK_STATUS_0.getCode());
        } else {
            entity.setOvertimeStatus(BaseEnum.AC_CHECK_STATUS_2.getCode());
        }
    }

    private boolean hasShiftData(Date departure, Date ret) {
        return departure != null || ret != null;
    }

    /** 发车：X=计粮-考勤；X>=0 取 X；-10&lt;X&lt;0 取 0；X&lt;=-10 取 X */
    private int contribDeparture(Date jl, Date kq) {
        Integer x = DateUtil.signedDiffMinutes(jl, kq);
        if (x == null) {
            return 0;
        }
        if (x >= 0) {
            return x;
        }
        if (x > -10) {
            return 0;
        }
        return x;
    }

    /** 收车：X=计粮-考勤；X&lt;0 取 |X|；X>=30 取 -X；0&lt;X&lt;30 取 0 */
    private int contribReturn(Date jl, Date kq) {
        Integer x = DateUtil.signedDiffMinutes(jl, kq);
        if (x == null) {
            return 0;
        }
        if (x < 0) {
            return -x;
        }
        if (x >= 30) {
            return -x;
        }
        return 0;
    }

    /**
     * 批量更新指定表中某个字段的值
     *
     * @param tableName  数据库表名
     * @param fieldName  要更新的字段名
     * @param fieldValue 要设置的新值
     * @param ids        需要更新的记录ID集合
     * @return 影响的行数
     */
    public int updateBySql(String tableName, String fieldName, String fieldValue, Collection<String> ids) {
        // 动态生成占位符：?, ?, ?
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "UPDATE " + tableName + " SET " + fieldName + " = ? WHERE id IN (" + placeholders + ")";

        SQLParameter sqlParameter = new SQLParameter();
        sqlParameter.addParam(fieldValue);
        for (String id : ids) {
            sqlParameter.addParam(id);
        }

        return billRepository.update(sql, sqlParameter);
    }

    /**
     * 批量更新指定表中多个字段的值
     *
     * @param tableName  数据库表名
     * @param fieldMap   要更新的字段名和值的映射关系 (key: 字段名, value: 字段值)
     * @param ids        需要更新的记录ID集合
     * @return 影响的行数
     */
    public int updateBySql(String tableName, Map<String, Object> fieldMap, Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        if (fieldMap == null || fieldMap.isEmpty()) {
            throw new IllegalArgumentException("fieldMap cannot be empty");
        }

        // 构建 SET 子句: field1 = ?, field2 = ?, field3 = ?
        String setClause = fieldMap.keySet().stream()
                .map(field -> field + " = ?")
                .collect(Collectors.joining(", "));

        // 动态生成 ID 占位符：?, ?, ?
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE id IN (" + placeholders + ")";

        // 设置参数：先设置所有字段值，再设置所有ID值
        SQLParameter sqlParameter = new SQLParameter();
        for (Object value : fieldMap.values()) {
            sqlParameter.addParam(value);
        }
        for (String id : ids) {
            sqlParameter.addParam(id);
        }

        return billRepository.update(sql, sqlParameter);
    }



    public String extractDigits(String input) {
        if (input == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
