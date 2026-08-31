package com.yonyou.ucf.mdf.sample.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.aop.SyncLog;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMCar;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmHisenseWaybillLoginRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseAttendanceMergedDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseLoginRecordDataDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseResponseDTO;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.ModuleEnum;
import com.yonyou.ucf.mdf.sample.enums.TaskTypeEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCheckRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmCarService;
import com.yonyou.ucf.mdf.sample.service.TcmHiSenseWaybillLoginRecordService;
import com.yonyou.ucf.mdf.sample.service.TcmSyncLogService;
import com.yonyou.ucf.mdf.sample.utils.CommonUtil;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import io.edap.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.imeta.core.base.ConditionOperator;
import org.imeta.orm.schema.SimpleCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 海信车载机登入记录同步服务实现类
 * 负责调用海信接口获取登入记录数据，并进行解析处理
 */
@Service
@Slf4j
public class TcmHiSenseWaybillLoginRecordServiceImpl implements TcmHiSenseWaybillLoginRecordService {

    /**
     * 海信车载机登入数据接口URL
     */
    public static final String HI_SENSE_LOGIN_RECORD_URL = "http://10.102.128.7:8080/sdata/rest/service/dataapi/rest/2208919096862354";

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
    private CommonUtil commonUtil;


    @Autowired
    private TcmAttendanceCheckRecordService checkRecordService;

    @Autowired
    private TcmSyncLogService tcmSyncLogService;

    /**
     * 构造函数，初始化ObjectMapper并配置反序列化特性
     */
    public TcmHiSenseWaybillLoginRecordServiceImpl() {
        this.objectMapper = new ObjectMapper();
        // 忽略未知属性，避免接口返回新增字段导致反序列化失败
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @SyncLog(model = TaskTypeEnum.HXDL_SYNC,dateParam = "actDate")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncByActDate(String actDate) {
        doSyncByActDateInternal(actDate);
    }

    /**
     *【两阶段日志】使用预创建的 syncLogId 执行海信车载机登入记录同步，完成后回填日志。
     * <p>不使用 @SyncLog 注解，避免 AOP 再插一条日志。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncByActDate(String actDate, String syncLogId) {
        if (StringUtil.isEmpty(syncLogId)) {
            doSyncByActDateInternal(actDate);
            return;
        }
        Throwable captured = null;
        int newCount = 0;
        try {
            newCount = doSyncByActDateInternal(actDate);
        } catch (Throwable t) {
            captured = t;
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            throw (Error) t;
        } finally {
            String remarkEx = (captured == null) ? ("同步成功，新增 " + newCount + " 条") : null;
            try {
                tcmSyncLogService.finishSyncLog(syncLogId, captured, remarkEx);
            } catch (Exception finishEx) {
                log.error("[syncByActDate(syncLogId)] 回填同步日志失败 syncLogId={}", syncLogId, finishEx);
            }
        }
    }

    /**
     * 车载机登入记录同步主逻辑（内部私有，不携带 @SyncLog 避免 AOP 切入）。
     */
    private int doSyncByActDateInternal(String actDate) {
        List<HiSenseLoginRecordDataDTO> hiSenseDataList = getHiSenseDataByActDate(actDate);
        if (hiSenseDataList.isEmpty()) {
            throw new RuntimeException("當前日期數據爲空，請稍後再試");
        }

        log.info("【海信登陆记录接口】业务日期：【{}】， 实际返回数：【{}】", actDate, hiSenseDataList.size());

        removeByActDate(actDate);

        String tenantId = InvocationInfoProxy.getTenantid();
        Set<String> busSelfIdSet = new HashSet<>();
        Set<String> weIdSet = new HashSet<>();
        for (HiSenseLoginRecordDataDTO h : hiSenseDataList) {
            busSelfIdSet.add(h.getBusselfid());
            weIdSet.add(h.getDriverid());
        }

        Map<String, TCMCar> stringTCMCarMap = tcmCarService.groupCarMapByInternalNo(busSelfIdSet);
        Map<String, String> staffMap = commonUtil.getStaffByCardIds(weIdSet, tenantId);


        // 用于存储缺失车辆信息的记录
        Set<String> missingCarSet = new HashSet<>();
        List<TcmHisenseWaybillLoginRecord> list = new ArrayList<>();

        for (HiSenseLoginRecordDataDTO dto : hiSenseDataList) {
            String busSelfId = dto.getBusselfid();
            TCMCar tcmCar = stringTCMCarMap.get(busSelfId);
            if (tcmCar == null) {
                // 再次以纯数字匹配
                String onlyNumber = commonUtil.extractDigits(busSelfId);
                tcmCar = stringTCMCarMap.get(onlyNumber);
                if (tcmCar == null) {
                    missingCarSet.add(busSelfId);
                    continue;
                }
            }

            TcmHisenseWaybillLoginRecord data = new TcmHisenseWaybillLoginRecord();
            data.setYtenantId(tenantId);
            data.setDate(dto.getActdate());
            data.setWaybill_employee_id(dto.getDriverid());
            data.setDriver_no(staffMap.get(dto.getDriverid()));
            data.setOperate_type(dto.getOperatetype());
            data.setTime(dto.getActdatetime());
            data.setCar(tcmCar.getId());
            list.add(data);
        }

        // 如果有缺失车辆信息的记录，统一报错
        if (!missingCarSet.isEmpty()) {

            String errorMsg = String.format(
                    "車輛信息缺失，請先完善檔案内容。缺失的內部編號共 %d 個：%s",
                    missingCarSet.size(),
                    String.join("、", missingCarSet)
            );

            throw new RuntimeException(errorMsg);
        }

        if (!list.isEmpty()) {
            billRepository.insert(list);
            checkRecordService.updateOrSaveRecord(actDate, ModuleEnum.TCM_ATTENDANCE_CHECK_RECORD,true);
            log.info("成功同步海信数据，日期: {}, 记录数: {}", actDate, list.size());
        } else {
            log.warn("没有可同步的数据，日期: {}", actDate);
        }
        return list.size();
    }

    @Override
    public Integer countByActDate(String actDate) {
        return getByActDate(actDate).size();
    }

    @Override
    public boolean checkDataByDate(String actDate){
        return !getByActDate(actDate).isEmpty();
    }

    @Override
    public void removeByActDate(String actDate) {
        List<SimpleCondition> conditionList = new ArrayList<>();
        conditionList.add(new SimpleCondition("date", ConditionOperator.in, actDate));
        billRepository.batchRemove(UrlEnum.WAYBILL_LOGIN_RECORD.getUrl(), conditionList);
    }

    private List<TcmHisenseWaybillLoginRecord> getByActDate(String actDate) {

        if (StringUtil.isEmpty(actDate)) {
            throw new RuntimeException("日期參數不能爲空");
        }

        String tenantId = InvocationInfoProxy.getTenantid();
        LambdaQueryCondition<TcmHisenseWaybillLoginRecord> carCondition = new LambdaQueryCondition<>(new TcmHisenseWaybillLoginRecord());
        carCondition.eq(TcmHisenseWaybillLoginRecord::getDate, actDate)
                .eq(TcmHisenseWaybillLoginRecord::getYtenantId, tenantId)
                .eq(TcmHisenseWaybillLoginRecord::getDr, BaseEnum.IS_NOT_DELETED.getIntCode());
        return billRepository.queryByClause(carCondition);
    }

    /**
     * 调用海信接口获取原始数据，解析后返回登录记录列表
     *
     * @param actDate 日期（格式：yyyy-MM-dd）
     * @return 登入记录数据列表
     */
    private List<HiSenseLoginRecordDataDTO> getHiSenseDataByActDate(String actDate) {


        if (StringUtil.isEmpty(actDate)) {
            throw new RuntimeException("日期參數不能爲空");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            List<HiSenseLoginRecordDataDTO> allData = new ArrayList<>();

            int pageNum = 0;
            int pageSize = 500;
            boolean hasMoreData = true;

            while (hasMoreData) {
                // 构建请求体
                String body = String.format(
                        "{\"actdate\":\"%s\", \"pageNum\":%d, \"pageSize\":%d}",
                        actDate, pageNum, pageSize
                );

//                log.info("请求海信数据，日期: {}, 第{}页, 每页{}条", actDate, pageNum, pageSize);

                String result = restTemplate.postForObject(HI_SENSE_LOGIN_RECORD_URL, body, String.class);

                HiSenseResponseDTO<HiSenseLoginRecordDataDTO> response = objectMapper.readValue(result,
                        objectMapper.getTypeFactory().constructParametricType(
                                HiSenseResponseDTO.class,
                                HiSenseLoginRecordDataDTO.class
                        ));

                if (response == null || response.getResult() == null || response.getResult().isEmpty()) {
                    // 没有更多数据了
                    hasMoreData = false;
//                    log.info("第{}页无数据，停止获取", pageNum);
                } else {
                    // 添加当前页数据
                    allData.addAll(response.getResult());
//                    log.info("第{}页获取到{}条数据，累计{}条",
//                            pageNum, response.getResult().size(), allData.size());

                    // 如果返回的数据少于pageSize，说明是最后一页
                    if (response.getResult().size() < pageSize) {
                        hasMoreData = false;
//                        log.info("最后一页数据，停止获取");
                    } else {
                        pageNum++; // 继续获取下一页
                    }
                }
            }

//            log.info("获取海信数据完成，日期: {}, 总记录数: {}", actDate, allData.size());
            return allData;

        } catch (Exception e) {
            throw new RuntimeException("同步海信車載機登錄記錄數據失敗", e);
        }
    }


}