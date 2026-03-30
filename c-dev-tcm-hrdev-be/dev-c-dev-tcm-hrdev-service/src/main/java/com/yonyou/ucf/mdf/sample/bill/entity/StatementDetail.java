package com.yonyou.ucf.mdf.sample.bill.entity;

import lombok.Data;

@Data
public class StatementDetail {

    /**
     * 訂單日期
     */
    private String orderDate;

    /**
     * 訂單類型 (P-消費 R-退款 C-撤銷 T-TIPS A-預授權完成 M-MOTO消費 K-MOTO退款 I-分期消費 J-分期退款 N-未明)
     */
    private String orderType;

    /**
     * 訂單時間
     */
    private String orderTime;

    /**
     * 商戶訂單編號
     */
    private String merchantOrderNo;

    /**
     * 商戶號
     */
    private String merchantNo;

    /**
     * 門店號
     */
    private String storeNo;

    /**
     * 終端號
     */
    private String terminalNo;

    /**
     * 支付方式
     */
    private String payChannel;

    /**
     * 訂單狀態
     */
    private String orderStatus;

    /**
     * 交易金額(分)
     */
    private Long transactionAmount;

    /**
     * 交易手續費(分)
     */
    private Long fee;

    /**
     * 第三方訂單號
     */
    private String thirdOrderNo;

    /**
     * 交易接入渠道
     */
    private String accessChannel;

    /**
     * 訂單金額(分)
     */
    private Long orderAmount;

    /**
     * 活動優惠金額(分)
     */
    private Long discountAmount;

    /**
     * 營銷活動編號
     */
    private String marketingId;

    /**
     * 中銀訂單號
     */
    private String bocOrderNo;

    /**
     * 原交易訂單號
     */
    private String originalOrderNo;

    /**
     * 幣別
     */
    private String currency;

    /**
     * 卡號
     */
    private String cardNumber;

    /**
     * 授權號
     */
    private String authCode;

    /**
     * 批次號
     */
    private String batchNo;

    /**
     * 終端流水號
     */
    private String terminalSerialNo;

    /**
     * 交易方式 (I-插卡、S-刷卡、T-拍卡、F-降級、M-手輸、Q-掃碼、G-GATEWAY PAYMENT)
     */
    private String transactionMethod;

    /**
     * 分期期數
     */
    private String installmentPeriod;

    /**
     * 屬性 (錢包或卡屬性)
     */
    private String attribute;

    /**
     * 訂單描述
     */
    private String orderDesc;
}
