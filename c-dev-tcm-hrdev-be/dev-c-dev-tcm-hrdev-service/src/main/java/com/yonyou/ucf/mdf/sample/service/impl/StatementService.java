package com.yonyou.ucf.mdf.sample.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yonyou.iuap.ymc.telemetry.internal.okhttp3.*;
import com.yonyou.ucf.mdf.sample.bill.entity.StatementDetail;
import com.yonyou.ucf.mdf.sample.bill.entity.StatementHeader;
import com.yonyou.ucf.mdf.sample.bill.entity.StatementRequest;
import com.yonyou.ucf.mdf.sample.utils.RSA2Util;
import com.yonyou.ucf.mdf.sample.utils.SignUtil;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StatementService {

    private static final String UAT_URL = "https://cuaam.bocmacau.com/mercweb/rsa/api_ol";
    private static final String PROD_URL = "https://aam.bocmacau.com/mercweb/rsa/api_ol";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient httpClient = new OkHttpClient();

    /**
     * 下载对账文件
     * @param merchantId 商戶編號
     * @param terminalNo 終端號
     * @param acDate 對賬日期 (yyyyMMdd)
     * @param privateKey 商戶私鑰
     * @param isProd 是否生产环境
     * @return 对账文件内容列表
     */
    public List<StatementDetail> downloadStatement(String merchantId, String terminalNo,
                                                   String acDate, String privateKey,
                                                   boolean isProd) throws Exception {
        // 1. 构建请求参数
        StatementRequest request = buildRequest(merchantId, terminalNo, acDate);

        // 2. 生成签名
        String signContent = SignUtil.buildSignContent(request);
        String sign = RSA2Util.sign(signContent, null);
        request.setMerchantSign(sign);

        // 3. 发送请求
        String url = isProd ? PROD_URL : UAT_URL;
        String jsonRequest = objectMapper.writeValueAsString(request);

        Request httpRequest = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求失败: " + response.code());
            }

            // 4. 解析响应文件内容
            return parseStatementFile(response.body().bytes());
        }
    }

    /**
     * 构建请求参数
     */
    private StatementRequest buildRequest(String merchantId, String terminalNo, String acDate) {
        StatementRequest request = new StatementRequest();
        request.setRequestId(generateRequestId());
        request.setService("Statement");
        request.setVersion("4.0");
        request.setSignType("RSA2");
        request.setMerchantId(merchantId);
        request.setTerminalNo(terminalNo);
        request.setAcDate(acDate);
        return request;
    }

    /**
     * 生成请求流水号
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    /**
     * 解析对账文件
     * 文件格式：头信息 + 交易明細，数据间用"|"分隔
     */
    private List<StatementDetail> parseStatementFile(byte[] fileContent) throws Exception {
        List<StatementDetail> details = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new java.io.ByteArrayInputStream(fileContent), StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                // 文件结束标识
                if ("END_FLG".equals(line.trim())) {
                    break;
                }

                if (isHeader) {
                    // 解析头信息
                    parseHeader(line);
                    isHeader = false;
                } else {
                    // 解析明细
                    StatementDetail detail = parseDetail(line);
                    if (detail != null) {
                        details.add(detail);
                    }
                }
            }
        }

        return details;
    }

    /**
     * 解析文件头信息
     * 格式：成功訂單筆數(MOP)|成功交易金額(MOP)|成功訂單筆數(HKD)|成功交易金額(HKD)|成功訂單筆數(USD)|成功交易金額(USD)
     */
    private StatementHeader parseHeader(String headerLine) {
        String[] fields = headerLine.split("\\|");
        if (fields.length >= 6) {
            StatementHeader header = new StatementHeader();
            header.setMopSuccessCount(parseInt(fields[0]));
            header.setMopSuccessAmount(parseLong(fields[1]));
            header.setHkdSuccessCount(parseInt(fields[2]));
            header.setHkdSuccessAmount(parseLong(fields[3]));
            header.setUsdSuccessCount(parseInt(fields[4]));
            header.setUsdSuccessAmount(parseLong(fields[5]));
            return header;
        }
        return null;
    }

    /**
     * 解析明细行
     * 格式：訂單日期|訂單類型|訂單時間|商戶訂單編號|商戶號|門店號|終端號|支付方式|訂單狀態|交易金額|交易手續費|
     *       第三方訂單號|交易接入渠道|訂單金額|活動優惠金額|營銷活動編號|中銀訂單號|原交易訂單號|幣別|卡號|授權號|
     *       批次號|終端流水號|交易方式|分期期數|屬性|訂單描述
     */
    private StatementDetail parseDetail(String detailLine) {
        String[] fields = detailLine.split("\\|");
        if (fields.length < 27) {
            return null;
        }

        StatementDetail detail = new StatementDetail();
        detail.setOrderDate(fields[0]);
        detail.setOrderType(fields[1]);
        detail.setOrderTime(fields[2]);
        detail.setMerchantOrderNo(fields[3]);
        detail.setMerchantNo(fields[4]);
        detail.setStoreNo(fields[5]);
        detail.setTerminalNo(fields[6]);
        detail.setPayChannel(fields[7]);
        detail.setOrderStatus(fields[8]);
        detail.setTransactionAmount(parseLong(fields[9]));
        detail.setFee(parseLong(fields[10]));
        detail.setThirdOrderNo(fields[11]);
        detail.setAccessChannel(fields[12]);
        detail.setOrderAmount(parseLong(fields[13]));
        detail.setDiscountAmount(parseLong(fields[14]));
        detail.setMarketingId(fields[15]);
        detail.setBocOrderNo(fields[16]);
        detail.setOriginalOrderNo(fields[17]);
        detail.setCurrency(fields[18]);
        detail.setCardNumber(fields[19]);
        detail.setAuthCode(fields[20]);
        detail.setBatchNo(fields[21]);
        detail.setTerminalSerialNo(fields[22]);
        detail.setTransactionMethod(fields[23]);
        detail.setInstallmentPeriod(fields[24]);
        detail.setAttribute(fields[25]);

        // 订单描述可能有多个字段拼接
        if (fields.length > 26) {
            StringBuilder desc = new StringBuilder();
            for (int i = 26; i < fields.length; i++) {
                if (desc.length() > 0) desc.append("|");
                desc.append(fields[i]);
            }
            detail.setOrderDesc(desc.toString());
        }

        return detail;
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
