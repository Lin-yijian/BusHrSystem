package com.yonyou.ucf.mdf.sample.utils;

import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.param.SQLParameter;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmAttendanceCorrection;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.enums.params.ParamKey;
import com.yonyou.ucf.mdf.sample.enums.params.TcmAttendanceVerificationParamKeyEnum;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    // =========================================================================================
    // 审计字段统一填充工具（creator/createTime/modifier/modifyTime）
    // insert 前调用 fillInsertAudit：填充 创建人+创建时间+修改人+修改时间
    // update 前调用 fillUpdateAudit：只填充 修改人+修改时间（不覆盖原创建人/创建时间）
    // 兼容所有实体（实体各自声明审计字段与 setter，通过反射+缓存调用）
    // =========================================================================================

    /** setter 反射缓存：Class -> (字段名 -> Method) */
    private static final Map<Class<?>, Map<String, Method>> AUDIT_SETTER_CACHE = new ConcurrentHashMap<>();

    /** 为单个对象填充 insert 审计字段（creator/createTime/modifier/modifyTime） */
    public static void fillInsertAudit(Object bean) {
        fillAudit(bean, true);
    }

    /** 为集合中每个对象填充 insert 审计字段 */
    public static void fillInsertAudit(Collection<?> beans) {
        if (beans == null) {
            return;
        }
        beans.forEach(b -> fillAudit(b, true));
    }

    /** 为单个对象填充 update 审计字段（modifier/modifyTime） */
    public static void fillUpdateAudit(Object bean) {
        fillAudit(bean, false);
    }

    /** 为集合中每个对象填充 update 审计字段 */
    public static void fillUpdateAudit(Collection<?> beans) {
        if (beans == null) {
            return;
        }
        beans.forEach(b -> fillAudit(b, false));
    }

    private static void fillAudit(Object bean, boolean insert) {
        if (bean == null) {
            return;
        }
        try {
            String userId = InvocationInfoProxy.getUserid();
            Date now = new Date();
            if (insert) {
                invokeAuditSetter(bean, "creator", userId);
                invokeAuditSetter(bean, "createTime", now);
            }
            invokeAuditSetter(bean, "modifier", userId);
            invokeAuditSetter(bean, "modifyTime", now);
        } catch (Exception e) {
            log.warn("[fillAudit] 填充审计字段失败 class={}", bean.getClass().getName(), e);
        }
    }

    private static void invokeAuditSetter(Object bean, String field, Object value) {
        Map<String, Method> setters = AUDIT_SETTER_CACHE.computeIfAbsent(bean.getClass(), k -> new ConcurrentHashMap<>());
        Method setter = setters.get(field);
        if (setter == null) {
            try {
                setter = bean.getClass().getMethod("set" + Character.toUpperCase(field.charAt(0)) + field.substring(1));
            } catch (NoSuchMethodException e) {
                // 实体没有该审计字段则跳过
                return;
            }
            setter.setAccessible(true);
            setters.put(field, setter);
        }
        try {
            setter.invoke(bean, value);
        } catch (Exception e) {
            log.warn("[fillAudit] 调用 setter 失败 class={} field={}", bean.getClass().getName(), field, e);
        }
    }

    public final String FIELD_CODE_WAYBILL_EMPLOYEE_ID = "waybill_employeeId";
    public final String FIELD_CODE_EMPLOYEE_ID = "code";
    public final String FIELD_CODE_POST_CODE = "postCode";
    public final String FIELD_CODE_STAFF_ID = "id";
    public final String FIELD_CODE_STAFF_AB_09 = "diverType";

    /** 当前事务内排更参数快照：一次任务只查整表一次，事务结束后丢弃。 */
    private final ThreadLocal<Map<String, String>> rosterParamSnapshot = new ThreadLocal<>();
    /** 当前事务内考勤核对参数快照：一次任务只查整表一次，事务结束后丢弃。 */
    private final ThreadLocal<Map<String, String>> verificationParamSnapshot = new ThreadLocal<>();

    /**
     * 参数读取统一入口（各模块共用）。
     * 有事务时整表查一次后复用（避免循环 N+1，事务结束自动丢弃快照）；无事务走
     * {@link TcmParamManageService#queryParamValue} 实时单键查询（自带缺失抛错 + checkType 格式校验）。
     * 每次任务（新事务）都重新查库，参数变更在下一次任务即生效。
     */
    private <K extends ParamKey> String requireParam(ModuleEnum module, K keyEnum,
                                                     ThreadLocal<Map<String, String>> snapshotHolder) {
        // 1. 事务内快照：整表一次，循环复用
        Map<String, String> snapshot = snapshotHolder.get();
        if (snapshot == null && TransactionSynchronizationManager.isSynchronizationActive()) {
            Map<String, String> loaded = tcmParamManageService.queryModuleParamMap(module);
            snapshot = (loaded != null) ? new HashMap<>(loaded) : new HashMap<>();
            snapshotHolder.set(snapshot);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    snapshotHolder.remove();
                }
            });
        }
        if (snapshot != null) {
            String value = snapshot.get(keyEnum.getCode());
            if (StringUtils.isBlank(value)) {
                throw new RuntimeException(
                        "配置缺失：" + keyEnum.getName() + "[" + keyEnum.getCode() + "]，請至【參數管理】菜單補錄");
            }
            // 按枚举 checkType 做格式校验（非法抛"配置非法：..."）
            return ParamKey.checkValue(keyEnum, value);
        }
        // 2. 无事务：统一入口实时单键查询
        return tcmParamManageService.queryParamValue(module, keyEnum);
    }

    /** 读取排更(ROSTERING)模块参数 */
    private String requireRosterParam(TcmRosteringParamKeyEnum keyEnum) {
        return requireParam(ModuleEnum.TCM_SHEDULE_SHIFTS, keyEnum, rosterParamSnapshot);
    }

    /** 读取考勤核對(TCM_ATTENDANCE_VERIFICATION)模块参数 */
    private String requireVerificationParam(TcmAttendanceVerificationParamKeyEnum keyEnum) {
        return requireParam(ModuleEnum.TCM_ATTENDANCE_VERIFICATION, keyEnum, verificationParamSnapshot);
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

        List<Map<String, Object>> mapList = getStaffByWayBillEmployeeIds(ids,tenantId);

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

        List<Map<String, Object>> mapList = getStaffByWayBillEmployeeIds(ids,tenantId);

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
     * 根据BIP工号获取员工信息
     * @param ids BIP工号集合
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
     * .
     * @param ids 工号集合
     * @param fieldName 查询字段
     * @param tenantId 租户ID
     * @return .
     */
    public List<Map<String, Object>> getStaff(Collection<String> ids,String fieldName, String tenantId) {
        QuerySchema querySchema = QuerySchema.create().addSelect("id, staffDefines.ab_01 as waybill_employeeId, staffDefines.ab_09 as diverType, postId, postId.code as postCode,code");
        querySchema.appendQueryCondition(QueryCondition.name(fieldName).in(ids));
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));

        return queryService.queryMapBySchema(UrlEnum.BD_STAFF.getUrl(), querySchema, UrlEnum.BD_STAFF.getDomainKey());

    }


    /**
     * 根据澳巴工号查询员工信息
     * @param ids 澳巴工号集合
     * @param tenantId 租户ID
     * @return .
     */
    public List<Map<String, Object>> getStaffByWayBillEmployeeIds(Collection<String> ids, String tenantId) {

        return getStaff(ids,"staffDefines.ab_01",tenantId);

    }

    /**
     * 根据BIP工号查询员工信息
     * @param ids BIP工号集合
     * @param tenantId 租户ID
     * @return .
     */
    public List<Map<String, Object>> getStaffByEmployeeIds(Collection<String> ids, String tenantId) {

        return getStaff(ids,"id",tenantId);

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
     * 检查日期是否处于强制假期范围内
     * @param date  yyyy-MM-dd
     * @param tenantId .
     * @return true 表示该日期为强制假期（假期上班）；无数据或不在任何区间内返回 false
     */
    public boolean checkHoliday(String date, String tenantId,String workingCalendarName) {
        if (StringUtils.isEmpty(date)) {
            return false;
        }

        QuerySchema querySchema = QuerySchema.create().addSelect("id, begintime, endtime, holidyname, ispreset ");
        querySchema.appendQueryCondition(QueryCondition.name("ytenant").eq(tenantId));
        querySchema.appendQueryCondition(QueryCondition.name("parentid.workingcalendarname").eq("澳巴-強制性假")); // todo 待转参数
//        querySchema.appendQueryCondition(QueryCondition.name("dr").eq(BaseEnum.IS_NOT_DELETED.getIntCode()));
        List<Map<String, Object>> mapList = queryService.queryMapBySchema(
                UrlEnum.TS_HOLIDAY.getUrl(), querySchema, UrlEnum.TS_HOLIDAY.getDomainKey());
        if (mapList == null || mapList.isEmpty()) {
            return false;
        }

        for (Map<String, Object> map : mapList) {
            // 日期区间判断（date 为 yyyy-MM-dd，begintime/endtime 为 yyyy-MM-dd HH:mm:ss），按日期粒度闭区间比较
            if (DateUtil.isDateInRange(date, map.get("begintime"), map.get("endtime"))) {
                log.info("【假期判斷】日期【{}】命中假期【{}】", date, map.get("holidyname"));
                return true;
            }
        }
        return false;
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
        entity.setIs_night_shift(BaseEnum.NO.getCode());

        calculateCorrection(entity,staffMap);
    }



    private void calculateCorrection(TcmAttendanceCorrection entity, Map<String, Map<String, Object>> staffMap) {
        // 一更时长 = 一更计量排更收车 - 一更计量排更发车    二更时长 = 二更计量排更收车 - 二更计量排更发车
        String oneTimeDifference = DateUtil.getTimeDifference(entity.getJl_one_return_time(), entity.getJl_one_departure_time());
        entity.setOne_time(oneTimeDifference);

        String twoTimeDifference = DateUtil.getTimeDifference(entity.getJl_two_return_time(), entity.getJl_two_departure_time());
        entity.setTwo_time(twoTimeDifference);

        // 计量有效工时 = （一更计量排更收车 - 一更计量排更发车）+ （二更计量排更收车 - 二更计量排更发车）- 用膳时长
        entity.setJl_valid_time(DateUtil.subtractTimeDifferences(
                DateUtil.addTimeDifferences(oneTimeDifference, twoTimeDifference),
                entity.getMeal_duration()));

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

        // 计算深宵时长（计粮发收车与每日 00:00-06:00 窗口的重叠时长；深宵司机不计）
        calculateLateNightDuration(entity, staffMap);

        // 计算超时时长（计粮 vs 考勤发收车）
        calculateOvertimeDuration(entity);

        // 计算遲到早退次數（勤工獎）= 遲到次數 + 早退次數
        calculateLateAndEarlyLeaveCount(entity, staffMap);

        // 计算是否夜更（计粮发收车完整覆盖 17:00-20:00 且 有效工时>=08:00）
        calculateNightShift(entity);

        // 计算迟到早退状态
        calculateLateEarlyStatus(entity);

        // 计算异常核对状态
        calculateAnomalyVerification(entity);
    }

    /**
     * 计算是否夜更（is_night_shift），两个条件须同时满足（判定窗口/工时门槛均取参数管理「考勤核對」模块配置）：
     * <ol>
     *   <li>（計糧一更發車 至 計糧一更收車）或（計糧二更發車 至 計糧二更收車）<b>完整覆蓋</b> 夜更判定窗口（默認 17:00-20:00）；</li>
     *   <li>計量有效工時（jl_valid_time）&gt;= 門檻（默認 08:00）。</li>
     * </ol>
     * 满足 → is_night_shift = {@link BaseEnum#YES}("1")；否则 = {@link BaseEnum#NO}("2")。
     */
    public void calculateNightShift(TcmAttendanceCorrection entity) {
        if (entity == null) {
            return;
        }
        String windowStart = requireVerificationParam(TcmAttendanceVerificationParamKeyEnum.NIGHT_SHIFT_WINDOW_START);
        String windowEnd = requireVerificationParam(TcmAttendanceVerificationParamKeyEnum.NIGHT_SHIFT_WINDOW_END);
        String minValidTime = requireVerificationParam(TcmAttendanceVerificationParamKeyEnum.NIGHT_SHIFT_MIN_VALID_TIME);

        boolean coversNightWindow =
                coversNightWindow(entity.getJl_one_departure_time(), entity.getJl_one_return_time(), windowStart, windowEnd)
                || coversNightWindow(entity.getJl_two_departure_time(), entity.getJl_two_return_time(), windowStart, windowEnd);
        // jl_valid_time 为 null 时按 00:00 处理，比较结果为 false
        boolean validTimeReached = DateUtil.isTimeGreaterOrEqual(entity.getJl_valid_time(), minValidTime);

        entity.setIs_night_shift((coversNightWindow && validTimeReached)
                ? BaseEnum.YES.getCode()
                : BaseEnum.NO.getCode());
    }

    /**
     * 判断单个更次区间 [departure, returnTime] 是否完整覆盖 [windowStartHhmm, windowEndHhmm]：
     * 发车时间 &lt;= 窗口起点 且 收车时间 &gt;= 窗口终点（边界包含；窗口日期以发车日期为准，兼容跨零点收车）。
     *
     * @param windowStartHhmm 窗口起点 HH:mm（checkType TIME_HH_MM 已保证格式合法）
     * @param windowEndHhmm   窗口终点 HH:mm
     */
    private boolean coversNightWindow(Date departure, Date returnTime, String windowStartHhmm, String windowEndHhmm) {
        if (departure == null || returnTime == null) {
            return false;
        }
        int[] start = parseHhMm(windowStartHhmm);
        int[] end = parseHhMm(windowEndHhmm);

        Calendar cal = Calendar.getInstance();
        cal.setTime(departure);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.set(Calendar.HOUR_OF_DAY, start[0]);
        cal.set(Calendar.MINUTE, start[1]);
        Date windowStart = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, end[0]);
        cal.set(Calendar.MINUTE, end[1]);
        Date windowEnd = cal.getTime();

        // 發車 <= 窗口起點 且 收車 >= 窗口終點
        return !departure.after(windowStart) && !returnTime.before(windowEnd);
    }

    /** 解析 HH:mm 为 [小时, 分钟]；checkType TIME_HH_MM 已在取值时保证格式合法 */
    private int[] parseHhMm(String hhmm) {
        String[] parts = hhmm.split(":");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
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

        String is_late_or_early_leave = (hasLate || hasEarlyLeave) ? BaseEnum.YES.getCode() : BaseEnum.NO.getCode();
        entity.setIs_late_or_early_leave(is_late_or_early_leave);
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
            entity.setIs_less_than_8_hours(BaseEnum.NO.getCode());
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

    /** 深宵窗口：每日凌晨 00:00 起至 06:00（含頭不含尾） */
    private static final int LATE_NIGHT_WINDOW_END_HOUR = 6;

    /**
     * 计算深宵時長（late_night_duration）。
     * <p>計糧一更/二更的【發車-收車】區間與每日凌晨 00:00-06:00 窗口的重疊時長累加（一更、二更分別計算後合計）：
     * <ul>
     *   <li>發車在窗口內、收車不在（如 04:00 發車、08:00 收車）→ 發車至 06:00 計入；</li>
     *   <li>發車、收車都在窗口內（如 02:00-05:00）→ 發車至收車全額計入；</li>
     *   <li>跨日（如二更 23:00 發車、次日 05:00 收車）→ 次日 00:00 至收車計入；</li>
     *   <li>窗口按日展開逐段求交，跨多日也能正確累加。</li>
     * </ul>
     * 司機類型為深宵司機時不計算深宵時長，固定 00:00。
     */
    private void calculateLateNightDuration(TcmAttendanceCorrection entity, Map<String, Map<String, Object>> staffMap) {
        Map<String, Object> staff = staffMap == null ? null : staffMap.get(entity.getEmployee_id());
        if (staff == null) {
            log.warn("未找到员工信息, 深宵時長按0處理, employeeId: {}", entity.getEmployee_id());
            entity.setLate_night_duration(DateUtil.HH_MM_ZERO);
            return;
        }
        String staffType = (String) staff.get(FIELD_CODE_STAFF_AB_09);
        // 深宵司機不計算深宵時長
        if (getNightDriverType().equals(staffType)) {
            entity.setLate_night_duration(DateUtil.HH_MM_ZERO);
            return;
        }
        long overlapMillis = 0L;
        overlapMillis += lateNightOverlapMillis(entity.getJl_one_departure_time(), entity.getJl_one_return_time());
        overlapMillis += lateNightOverlapMillis(entity.getJl_two_departure_time(), entity.getJl_two_return_time());
        int minutes = (int) (overlapMillis / 60000L);
        entity.setLate_night_duration(DateUtil.minutesToHhMm(minutes));
    }

    /**
     * 區間 [departure, returnTime] 與每日凌晨 00:00-06:00 窗口的重疊毫秒數。
     * 從發車當日 00:00 起逐日構造窗口，與工作區間求交集後累加，天然兼容跨日（含跨兩晚）場景。
     */
    private long lateNightOverlapMillis(Date departure, Date returnTime) {
        if (departure == null || returnTime == null || !returnTime.after(departure)) {
            return 0L;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(departure);
        // 對齊到發車當日 00:00:00.000
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long overlap = 0L;
        // 窗口起點不晚於收車時間才可能有交集
        while (!cal.getTime().after(returnTime)) {
            long windowStart = cal.getTimeInMillis();
            cal.add(Calendar.HOUR_OF_DAY, LATE_NIGHT_WINDOW_END_HOUR);
            long windowEnd = cal.getTimeInMillis();

            // 區間求交：[max(發車, 窗口起), min(收車, 窗口終))
            long interStart = Math.max(departure.getTime(), windowStart);
            long interEnd = Math.min(returnTime.getTime(), windowEnd);
            if (interEnd > interStart) {
                overlap += interEnd - interStart;
            }
            // 推進到次日 00:00（06:00 + 18h）
            cal.add(Calendar.HOUR_OF_DAY, 24 - LATE_NIGHT_WINDOW_END_HOUR);
        }
        return overlap;
    }

    /**
     * 計算遲到早退次數（勤工獎）：late_and_early_leave_count = 遲到次數 + 早退次數。
     * <p>閾值均取參數管理「考勤核對」模塊（時間參數為 HH:mm 字符串）：
     * <ul>
     *   <li>遲到次數：一更/二更各自的遲到時長 &gt;= LATE_COUNT_THRESHOLD（默認 00:10），該更算一次（最多 2 次）；</li>
     *   <li>有效總工時 &gt;= 標準工時（深宵車長 &gt;= NIGHT_EARLY_LEAVE_STANDARD_TIME 默認 06:00，其餘 &gt;= 標準工時參數）→ 不計早退；</li>
     *   <li>有效總工時 &lt; 標準工時：一、二更早退時長均 &gt;= EARLY_LEAVE_SINGLE_THRESHOLD（默認 00:30）算 2 次；
     *       一個達標、一個未達標算 1 次；均未達標但合計 &gt;= EARLY_LEAVE_COMBINED_THRESHOLD（默認 00:40）算 1 次；否則 0 次。</li>
     * </ul>
     */
    private void calculateLateAndEarlyLeaveCount(TcmAttendanceCorrection entity, Map<String, Map<String, Object>> staffMap) {
        // 閾值參數（HH:mm）：實時查詢，缺失/非法由參數服務統一拋錯
        int lateThreshold = hhmmToMinutes(requireVerificationParam(TcmAttendanceVerificationParamKeyEnum.LATE_COUNT_THRESHOLD));
        int singleEarlyThreshold = hhmmToMinutes(requireVerificationParam(TcmAttendanceVerificationParamKeyEnum.EARLY_LEAVE_SINGLE_THRESHOLD));
        int combinedEarlyThreshold = hhmmToMinutes(requireVerificationParam(TcmAttendanceVerificationParamKeyEnum.EARLY_LEAVE_COMBINED_THRESHOLD));
        String nightStandardTime = requireVerificationParam(TcmAttendanceVerificationParamKeyEnum.NIGHT_EARLY_LEAVE_STANDARD_TIME);

        // 遲到次數：逐更判定，單更遲到時長 >= 門檻算一次
        int lateCount = 0;
        if (hhmmToMinutes(entity.getOne_late_duration()) >= lateThreshold) {
            lateCount++;
        }
        if (hhmmToMinutes(entity.getTwo_late_duration()) >= lateThreshold) {
            lateCount++;
        }

        // 早退次數
        int earlyLeaveCount = 0;
        String staffType = staffMap == null ? null
                : (staffMap.get(entity.getEmployee_id()) == null ? null
                : (String) staffMap.get(entity.getEmployee_id()).get(FIELD_CODE_STAFF_AB_09));
        boolean nightDriver = getNightDriverType().equals(staffType);
        // 深宵車長早退判定工時門檻取參數（默認 06:00），其餘用標準工時參數
        String standardTime = nightDriver ? nightStandardTime : getWorkTime();
        String validTime = entity.getJl_valid_time();
        // 有效總工時 >= 標準工時 → 不計早退
        boolean reachStandard = DateUtil.isTimeGreaterOrEqual(validTime, standardTime);
        if (!reachStandard) {
            int d1 = hhmmToMinutes(entity.getOne_early_leave_duration());
            int d2 = hhmmToMinutes(entity.getTwo_early_leave_duration());
            boolean e1 = d1 >= singleEarlyThreshold;
            boolean e2 = d2 >= singleEarlyThreshold;
            if (e1 && e2) {
                earlyLeaveCount = 2;                       // 兩更均達單更門檻
            } else if (e1 || e2) {
                earlyLeaveCount = 1;                       // 僅一更達單更門檻
            } else if (d1 + d2 >= combinedEarlyThreshold) {
                earlyLeaveCount = 1;                       // 兩更均未達標但合計達門檻
            }
        }
        entity.setLate_and_early_leave_count(lateCount + earlyLeaveCount);
    }

    /** HH:mm 时长字符串转分钟数；null/空白/非法按 0 处理 */
    private int hhmmToMinutes(String hhmm) {
        if (StringUtils.isBlank(hhmm)) {
            return 0;
        }
        try {
            String[] parts = hhmm.trim().split(":");
            int hours = Integer.parseInt(parts[0].trim());
            int minutes = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
            return hours * 60 + minutes;
        } catch (Exception e) {
            log.warn("無法解析時長字符串，按0處理: {}", hhmm);
            return 0;
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
     * 批量更新指定表中某个字段的值
     *
     * @param tableName  数据库表名
     * @param queryField    查询字段名
     * @param fieldName  要更新的字段名
     * @param fieldValue 要设置的新值
     * @param ids        需要更新的记录ID集合
     * @return 影响的行数
     */
    public int updateBySql(String tableName, String fieldName, String fieldValue, String queryField,Collection<String> ids) {
        // 参数校验
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        // 动态生成占位符：?, ?, ?
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "UPDATE " + tableName + " SET " + fieldName + " = ? WHERE " + queryField + " IN (" + placeholders + ")";

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
