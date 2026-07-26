package com.yonyou.ucf.mdf.sample.bill.entity.dto;

import lombok.Data;

@Data
public class WaybillExcelRecordDTO {

    /** 取車地點 */
    private String takeCarLocation;
    /** 交車地點 */
    private String giveCarLocation;
    /** 編定車號 */
    private String carNumber;
    /** 任務 */
    private String task;
    /** 發車時間 */
    private String runTime;
    /** 收車時間 */
    private String backTime;
    /** 發車地點 */
    private String runCarLocation;
    /** 收車地點 */
    private String backCarLocation;
    /** 工號 */
    private String employeeId;
    /** 姓名 */
    private String employeeName;
    /** 對應下更 */
    private String toPM;
    /** 對應上更 */
    private String toAM;
    /** 用膳次數 */
    private String diningNum;
    /** 備註 */
    private String remark;
    /** 路线 */
    private String route;
    /** 班次 */
    private String shift;
    /** 取車加油 */
    private String takeOil;
    /** 收車加油 */
    private String backOil;
    /** 日期 */
    private String date;




}
