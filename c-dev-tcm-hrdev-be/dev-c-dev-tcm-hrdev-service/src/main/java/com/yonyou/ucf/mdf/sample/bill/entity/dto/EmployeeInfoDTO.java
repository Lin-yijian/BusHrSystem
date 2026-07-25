package com.yonyou.ucf.mdf.sample.bill.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 员工信息DTO - 每日工作资料PDF报表用
 * @author aze
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeInfoDTO {

    /** 员工姓名 */
    private String name;

    /** 部门 */
    private String department;

    /** 员工编号 */
    private String code;

    /** 岗位 */
    private String position;

    /** 月份，如 2026-04 */
    private String month;
}
