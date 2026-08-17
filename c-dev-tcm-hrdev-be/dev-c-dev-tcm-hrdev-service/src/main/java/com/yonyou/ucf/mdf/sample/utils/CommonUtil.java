package com.yonyou.ucf.mdf.sample.utils;

import com.yonyou.iuap.yms.param.SQLParameter;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import org.apache.commons.lang3.StringUtils;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QuerySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CommonUtil {

    @Autowired
    protected IBillQueryRepository queryService;

    @Autowired
    protected IBillRepository billRepository;

    public final String FIELD_CODE_WAYBILL_EMPLOYEE_ID = "waybill_employeeId";
    public final String FIELD_CODE_EMPLOYEE_ID = "code";
    public final String FIELD_CODE_POST_CODE = "postCode";
    public final String FIELD_CODE_STAFF_ID = "id";


    /** 兼职司机岗位编码 */
    public final String PART_TIME_JOB_CODE = "P0020103_NC07";

    public static final String WORK_TIME = "08:00";


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
        QuerySchema querySchema = QuerySchema.create().addSelect("id, staffDefines.ab_01 as waybill_employeeId, postId, postId.code as postCode,code");
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



    public void processDate(TcmAttendanceCorrection entity){

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

        calculateCorrection(entity);
    }


    private void calculateCorrection(TcmAttendanceCorrection entity){
        // 一更时长 = 一更计量排更收车 - 一更计量排更发车
        String oneTimeDifference = DateUtil.getTimeDifference(entity.getJl_one_return_time(), entity.getJl_one_departure_time());
        entity.setOne_time(oneTimeDifference);
        // 二更时长 = 二更计量排更收车 - 二更计量排更发车
        String twoTimeDifference = DateUtil.getTimeDifference(entity.getJl_two_return_time(), entity.getJl_two_departure_time());
        entity.setTwo_time(twoTimeDifference);
        // 一更早退时长 = 一更计量排更收车 - 考勤一更收车
        String one_early_leave_duration = DateUtil.getTimeDifference(entity.getJl_one_return_time(), entity.getKq_one_return_time(), false);
        entity.setOne_early_leave_duration(one_early_leave_duration);
        // 二更早退时长 = 二更计量排更收车 - 考勤二更收车
        String two_early_leave_duration = DateUtil.getTimeDifference(entity.getJl_two_return_time(), entity.getKq_two_return_time(), false);
        entity.setTwo_early_leave_duration(two_early_leave_duration);
        // 计量有效工时 = （一更计量排更收车 - 一更计量排更发车）+ （二更计量排更收车 - 二更计量排更发车）
        entity.setJl_valid_time(DateUtil.addTimeDifferences(oneTimeDifference, twoTimeDifference));
        // 加班工时 = 计量有效工时 - 08:00
        if (DateUtil.isTimeLessOrEqual(entity.getJl_valid_time(), WORK_TIME)) {
            entity.setOvertime_time("00:00");
        }else {
            entity.setOvertime_time(DateUtil.subtractTimeDifferences(entity.getJl_valid_time(), WORK_TIME));
        }
        // 一更遲到時長 =  考勤一更發車 - 計糧一更發車
        String oneLateDuration = DateUtil.getTimeDifference(entity.getKq_one_departure_time(), entity.getJl_one_departure_time(), false);
        entity.setOne_late_duration(oneLateDuration);
        // 二更遲到時長 =  考勤二更發車 - 計糧二更發車
        String twoLateDuration = DateUtil.getTimeDifference(entity.getKq_two_departure_time(), entity.getJl_two_departure_time(), false);
        entity.setTwo_late_duration(twoLateDuration);
        // 迟到总时长
        String lateDuration = DateUtil.addTimeDifferences(oneTimeDifference, twoTimeDifference);
        entity.setLate_duration(lateDuration);

        // 迟到状态 1、一更遲到時長或二更遲到時長>10分鐘，則待核對  2、遲到時長計算的參數不完整（比如排更有兩更、考勤只有一更時；或排更有一更、考勤有兩更就是參數不完整）
        String lateTime = "00:10"; // todo 待转参数
        boolean oneD = (entity.getKq_one_departure_time() != null) == (entity.getPg_one_departure_time() != null);
        boolean oneR = (entity.getKq_one_return_time() != null) == (entity.getPg_one_return_time() != null);
        boolean twoD = (entity.getKq_two_departure_time() != null) == (entity.getPg_two_departure_time() != null);
        boolean twoR = (entity.getKq_two_return_time() != null) == (entity.getPg_two_return_time() != null);
        if (DateUtil.isTimeGreater(oneLateDuration, lateTime) || DateUtil.isTimeGreater(twoLateDuration, lateTime)) {
            entity.setLateStatus(BaseEnum.AC_CHECK_STATUS_0.getCode());
        }

        if (!oneD || !oneR || !twoD || !twoR){
            entity.setLateStatus(BaseEnum.AC_CHECK_STATUS_0.getCode());
            entity.setOvertimeStatus(BaseEnum.AC_CHECK_STATUS_0.getCode());
        }
        // 异常核对状态变更
        if (BaseEnum.AC_CHECK_STATUS_0.getCode().equals(entity.getOvertimeStatus()) || BaseEnum.AC_CHECK_STATUS_0.getCode().equals(entity.getLateStatus())) {
            entity.setAnomaly_verification(BaseEnum.ERROR_CODE.getCode());
        }

        // 是否迟到早退
        if (!DateUtil.checkIsZeroTime(lateDuration) || !DateUtil.checkIsZeroTime(one_early_leave_duration) || !DateUtil.checkIsZeroTime(two_early_leave_duration)) {
            entity.setIs_late_or_early_leave(true);
        }
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
