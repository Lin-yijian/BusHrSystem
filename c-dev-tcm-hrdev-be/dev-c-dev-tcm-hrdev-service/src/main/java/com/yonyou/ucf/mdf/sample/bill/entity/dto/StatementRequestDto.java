package com.yonyou.ucf.mdf.sample.bill.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatementRequestDto {

    private String merchantId;
    private String terminalNo;
    private String acDate;
    private String privateKey;
    private boolean prod;


}
