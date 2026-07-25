package com.yonyou.ucf.mdf.sample.bill.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyWorkRecordDTO {

    /** 星期 */
    private String week;

    /** 日期 */
    private String date;

    /** 出勤类型 */
    private String attendanceType;

    /** 工作时段 */
    private String workPeriod;

    /** 加班时段 */
    private String overtime;

    /** 深宵时段 */
    private String lateNight;

    /** 早退时段 */
    private String earlyLeave;

    /** 单次（车票数），null表示无数据 */
    private Integer ticketCount;

    /** 入油，null表示无数据 */
    private Integer oilCount;

    /** 加氧，null表示无数据 */
    private Integer gasCount;

    /** 上下更 */
    private String upperLowerShift;

    /** 顶更 */
    private String topShift;

    /** 夜更 */
    private String nightShift;
}
