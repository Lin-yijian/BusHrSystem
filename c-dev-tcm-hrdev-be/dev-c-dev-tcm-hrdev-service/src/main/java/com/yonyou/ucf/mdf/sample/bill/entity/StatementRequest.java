package com.yonyou.ucf.mdf.sample.bill.entity;


import lombok.Data;

@Data
public class StatementRequest {

    /**
     * 商戶系統請求流水號
     */
    private String requestId;

    /**
     * 接口類型：Statement
     */
    private String service;

    /**
     * 接口版本：4.0
     */
    private String version;

    /**
     * 用戶瀏覽器IP地址
     */
    private String ipAddress;

    /**
     * 簽名類型：RSA2
     */
    private String signType;

    /**
     * 合作商戶編號
     */
    private String merchantId;

    /**
     * 終端號
     */
    private String terminalNo;

    /**
     * 對賬日期 (yyyyMMdd)
     */
    private String acDate;

    /**
     * 簽名串
     */
    private String merchantSign;
}
