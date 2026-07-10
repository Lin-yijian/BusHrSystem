package com.yonyou.ucf.mdf.sample.bill.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 海信考勤接口响应DTO
 * 用于解析海信接口返回的完整JSON结构
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HiSenseAttendanceResponseDTO {

    /** 响应码 */
    private Integer code;

    /** 确定值 */
    private Integer definite;

    /** 描述信息 */
    private String description;

    /** 日志记录 */
    private Object logRecord;

    /** 响应消息 */
    private String message;

    /** 编排结果 */
    private Object orchResult;

    /** 考勤数据列表 */
    private List<HiSenseAttendanceDataDTO> result;

    /** HTTP状态码 */
    private Integer status;

    /** 总记录数 */
    private Integer totalCount;

    /** 追踪ID */
    private String traceId;
}