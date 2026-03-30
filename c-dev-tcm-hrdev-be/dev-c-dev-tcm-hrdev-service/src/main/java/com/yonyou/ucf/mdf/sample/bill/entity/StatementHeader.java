package com.yonyou.ucf.mdf.sample.bill.entity;

import lombok.Data;

@Data
public class StatementHeader {

    /**
     * MOP成功訂單筆數
     */
    private Integer mopSuccessCount;

    /**
     * MOP成功交易金額(分)
     */
    private Long mopSuccessAmount;

    /**
     * HKD成功訂單筆數
     */
    private Integer hkdSuccessCount;

    /**
     * HKD成功交易金額(分)
     */
    private Long hkdSuccessAmount;

    /**
     * USD成功訂單筆數
     */
    private Integer usdSuccessCount;

    /**
     * USD成功交易金額(分)
     */
    private Long usdSuccessAmount;

}
