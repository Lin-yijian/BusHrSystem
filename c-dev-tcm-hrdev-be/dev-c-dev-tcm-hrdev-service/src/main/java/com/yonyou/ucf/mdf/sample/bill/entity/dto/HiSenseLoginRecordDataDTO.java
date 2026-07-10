package com.yonyou.ucf.mdf.sample.bill.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 海信车载机登入记录接口原始响应数据DTO
 * 用于解析海信接口返回的result数组中的每一条记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HiSenseLoginRecordDataDTO {

    /** 唯一值 */
    private String busempdutyid;

    /** 车号 */
    private String busselfid;

    /** 考勤时间 */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date actdatetime;

    /** 日期 */
    private String actdate;

    /** 操作类型：1.上班 2.下班 */
    private String operatetype;

    /** 工号 */
    private String driverid;
}