package com.yonyou.ucf.mdf.sample.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yonyou.iuap.context.InvocationInfoProxy;
import com.yonyou.iuap.yms.id.generator.YmsOidGenerator;
import com.yonyou.iuap.yms.param.condtition.query.LambdaQueryCondition;
import com.yonyou.ucf.mdf.sample.bill.entity.TCMCar;
import com.yonyou.ucf.mdf.sample.bill.entity.TcmHisenseWaybillLoginRecord;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseAttendanceMergedDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseLoginRecordDataDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.HiSenseResponseDTO;
import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ucf.mdf.sample.enums.UrlEnum;
import com.yonyou.ucf.mdf.sample.service.TcmCarService;
import com.yonyou.ucf.mdf.sample.service.TcmHiSenseWaybillLoginRecordService;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillQueryRepository;
import com.yonyou.ypd.bill.infrastructure.service.api.IBillRepository;
import io.edap.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.imeta.core.base.ConditionOperator;
import org.imeta.orm.schema.SimpleCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    /**
     * 构造函数，初始化ObjectMapper并配置反序列化特性
     */
    public TcmHiSenseWaybillLoginRecordServiceImpl() {
        this.objectMapper = new ObjectMapper();
        // 忽略未知属性，避免接口返回新增字段导致反序列化失败
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @Override
    public void syncByActDate(String actDate) {
        List<HiSenseLoginRecordDataDTO> hiSenseDataList = getHiSenseDataByActDate(actDate);
        if (hiSenseDataList.isEmpty()) {
            throw new RuntimeException("當前日期數據爲空，請稍後再試");
        }

        removeByActDate(actDate);

        List<String> busSelfIdList = new ArrayList<>();
        for (HiSenseLoginRecordDataDTO hiSenseData : hiSenseDataList) {
            busSelfIdList.add(hiSenseData.getBusselfid());
        }

        Map<String, TCMCar> stringTCMCarMap = tcmCarService.groupCarMapByInternalNo(busSelfIdList);

        String tenantId = InvocationInfoProxy.getTenantid();

        // 用于存储缺失车辆信息的记录
        List<HiSenseLoginRecordDataDTO> missingCarDataList = new ArrayList<>();
        List<TcmHisenseWaybillLoginRecord> list = new ArrayList<>();

        for (HiSenseLoginRecordDataDTO dto : hiSenseDataList) {
            TCMCar tcmCar = stringTCMCarMap.get(dto.getBusselfid());
            if (tcmCar == null) {
                missingCarDataList.add(dto);
                continue;
            }

            String id = ymsOidGenerator.nextStrId();
            TcmHisenseWaybillLoginRecord data = new TcmHisenseWaybillLoginRecord();
            data.setId(id);
            data.setYtenantId(tenantId);
            data.setDate(dto.getActdate());
            data.setWaybill_employee_id(dto.getDriverid());
            data.setDriver_no(dto.getDriverid());
            data.setOperate_type(dto.getOperatetype());
            data.setTime(dto.getActdatetime());
            data.setCar(tcmCar.getId());
            list.add(data);
        }

        // 如果有缺失车辆信息的记录，统一报错
        if (!missingCarDataList.isEmpty()) {
            List<String> missingBusselfids = missingCarDataList.stream()
                    .map(HiSenseLoginRecordDataDTO::getBusselfid)
                    .distinct()
                    .collect(Collectors.toList());

            String errorMsg = String.format(
                    "車輛信息缺失，請先完善檔案内容。缺失的內部編號共 %d 個：%s",
                    missingBusselfids.size(),
                    String.join("、", missingBusselfids)
            );

            throw new RuntimeException(errorMsg);
        }

        if (!list.isEmpty()) {
            billRepository.insert(list);
            log.info("成功同步海信数据，日期: {}, 记录数: {}", actDate, list.size());
        } else {
            log.warn("没有可同步的数据，日期: {}", actDate);
        }
    }

    @Override
    public Integer countByActDate(String actDate) {
        return getByActDate(actDate).size();
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

            int pageNum = 1;
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