package com.yonyou.ucf.mdf.sample.bill.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工作资料PDF渲染DTO - 封装员工信息与每日记录
 * @author aze
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkReportRenderDTO {

    /** 员工信息 */
    private EmployeeInfoDTO employeeInfo;

    /** 每日工作记录列表 */
    private List<DailyWorkRecordDTO> dailyWorkRecords;
}
