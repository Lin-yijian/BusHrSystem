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
import org.imeta.core.base.ConditionOperator;
import org.imeta.orm.schema.SimpleCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 海信车载机登入记录同步服务实现类
 * 负责调用海信接口获取登入记录数据，并进行解析处理
 */
@Service
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

        List<TcmHisenseWaybillLoginRecord> list = new ArrayList<>();
        for (HiSenseLoginRecordDataDTO dto : hiSenseDataList) {
            String id = ymsOidGenerator.nextStrId();
            TcmHisenseWaybillLoginRecord data = new TcmHisenseWaybillLoginRecord();
            data.setId(id);
            data.setYtenantId(tenantId);
            data.setDate(dto.getActdate());
            data.setHisense_waybill_no(dto.getDriverid());
            data.setDriver_no(dto.getDriverid());
            data.setOperate_type(dto.getOperatetype());
            data.setTime(dto.getActdatetime());

            TCMCar tcmCar = stringTCMCarMap.get(dto.getBusselfid());
            if (tcmCar == null) {
                throw new RuntimeException("車輛信息缺失，請先完善檔案内容，内部編號：" + dto.getBusselfid());
            }

            data.setCar(tcmCar.getId());
            list.add(data);
        }

        billRepository.insert(list);


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
            String body = "{\"actdate\":\"" + actDate + "\", \"pageNum\":1, \"pageSize\":9999}";
            String result = restTemplate.postForObject(HI_SENSE_LOGIN_RECORD_URL, body, String.class);

            HiSenseResponseDTO<HiSenseLoginRecordDataDTO> response = objectMapper.readValue(result,
                    objectMapper.getTypeFactory().constructParametricType(HiSenseResponseDTO.class, HiSenseLoginRecordDataDTO.class));

            if (response == null || response.getResult() == null) {
                return Collections.emptyList();
            }

            return response.getResult();

        } catch (Exception e) {
            throw new RuntimeException("同步海信車載機登錄記錄數據失敗", e);
        }
    }


}