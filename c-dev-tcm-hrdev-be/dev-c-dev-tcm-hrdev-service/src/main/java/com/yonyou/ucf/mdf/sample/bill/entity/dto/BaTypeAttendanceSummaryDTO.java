package com.yonyou.ucf.mdf.sample.bill.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 巴型转数汇总记录DTO
 * 根据员工ID和车型进行归类汇总
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaTypeAttendanceSummaryDTO {

    /** 员工ID */
    private String staffId;

    /** 车型 */
    private String carType;

    /** 巴型转数累加值 */
    private BigDecimal busTransferTimes;
}