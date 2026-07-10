package com.yonyou.ucf.mdf.sample.bill.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 合并后的海信考勤数据DTO
 * 同一工号、同一辆车、同线路、同方向的连续车次会根据时间间隔进行合并
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HiSenseAttendanceMergedDTO {

    /** 合并数量（记录合并了多少条原始数据） */
    private Integer count;

    /** 运行日期 */
    private Date rundate;

    /** 线路名 */
    private String routename;

    /** 方向ID */
    private String segmentid;

    /** 方向名称 */
    private String segmentname;

    /** 发车站点（取第一条记录） */
    private String startstationname;

    /** 到达站点（取最后一条记录） */
    private String endstationname;

    /** 车号 */
    private String busselfid;

    /** 车牌 */
    private String buscardid;

    /** 驾驶员姓名（从drivername中拆分） */
    private String driverName;

    /** 工号（从drivername中拆分） */
    private String employeeId;

    /** 调度发车时间（取第一条记录） */
    private Date leavetime;

    /** 调度到达时间（取最后一条记录） */
    private Date arrivetime;

    /** 实际发车时间（取第一条记录） */
    private Date realleavetime;

    /** 实际到达时间（取最后一条记录） */
    private Date realarrivetime;

    /** 是否已确认 */
    private String isconfirm;

    /** 烂班标识 */
    private String fakeflag;

    /** 故障标识 */
    private String troubleflag;

    /** 车次执行状态（暂无用） */
    private String recstate;
}