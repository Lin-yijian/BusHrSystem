package com.yonyou.ucf.mdf.sample.service;

import com.yonyou.ucf.mdf.sample.bill.entity.dto.WorkReportRenderDTO;

import java.util.List;
import java.util.Map;

/**
 * 工作资料报表服务
 * @author aze
 */
public interface WorkReportService {

    /**
     * 获取工作资料报表渲染数据（员工信息+每日记录）
     * @param employeeId 员工编号
     * @param yearMonth 年月，如 2026-04
     * @return 渲染数据DTO列表
     */
    List<WorkReportRenderDTO> getWorkReportData(String employeeId, String yearMonth);

    /**
     * 查询当月所有修正数据（按employee_id, date排序），供流式分组处理
     * @param yearMonth 年月，如 2026-04
     * @return 原始Map列表
     */
    List<Map<String, Object>> queryAllCorrectionData(String yearMonth);

    /**
     * 根据单个员工的修正记录构建渲染DTO
     * @param empRecords 单个员工的修正记录列表
     * @param yearMonth 年月
     * @return 渲染DTO
     */
    WorkReportRenderDTO buildSingleReport(List<Map<String, Object>> empRecords, String yearMonth);

    /**
     * 查询当月有修正数据的所有员工ID（轻量查询，只返回ID列表）
     * @param yearMonth 年月，如 2026-04
     * @return 员工ID列表
     */
    List<String> queryDistinctEmployeeIds(String yearMonth);
}
