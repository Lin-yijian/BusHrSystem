package com.yonyou.ucf.mdf.sample.bill.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 海信考勤接口原始响应数据DTO
 * 用于解析海信接口返回的result数组中的每一条记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HiSenseAttendanceDataDTO {

    /** 车号 */
    private String busselfid;

    /** 实际发车时间 */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date realleavetime;

    /** 方向名称 */
    private String segmentname;

    /** 到达站点 */
    private String endstationname;

    /** 实际到达时间 */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date realarrivetime;

    /** 驾驶员|工号（格式：姓名|工号，接口原始字段） */
    private String drivername;

    /** 驾驶员姓名（由 drivername 按 "|" 拆分得到，非接口字段，拉取后统一回填） */
    private String driverName;

    /** 驾驶员工号（由 drivername 按 "|" 拆分得到，非接口字段，拉取后统一回填） */
    private String employeeId;

    /** 调度到达时间 */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date arrivetime;

    /** 烂班标识 */
    private String fakeflag;

    /** 车牌 */
    private String buscardid;

    /** 运行日期 */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date rundate;

    /** 车次执行状态（暂无用） */
    private String recstate;

    /** 方向ID */
    private String segmentid;

    /** 调度发车时间 */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date leavetime;

    /** 发车站点 */
    private String startstationname;

    /** 故障标识 */
    private String troubleflag;

    /** 线路名 */
    private String routename;

    /** 是否已确认 */
    private String isconfirm;

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDrivername() {
        return drivername;
    }

    public void setDrivername(String drivername) {
        this.drivername = drivername;
    }
}
