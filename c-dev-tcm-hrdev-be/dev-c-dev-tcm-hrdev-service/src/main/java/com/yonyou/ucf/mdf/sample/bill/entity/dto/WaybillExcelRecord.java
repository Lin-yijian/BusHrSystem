package com.yonyou.ucf.mdf.sample.bill.entity.dto;

import java.util.Date;

/**
 * Excel解析中间DTO - 排班记录
 */
public class WaybillExcelRecord {

    private String date;
    private int dataCount;
    private String taskId;
    private String hisunId;
    private String plate;
    private String routeSheet;
    private Date departTime;
    private Date returnTime;
    private int mealCount;
    private String location;
    private String parkingSpace;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getDataCount() { return dataCount; }
    public void setDataCount(int dataCount) { this.dataCount = dataCount; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getHisunId() { return hisunId; }
    public void setHisunId(String hisunId) { this.hisunId = hisunId; }

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public String getRouteSheet() { return routeSheet; }
    public void setRouteSheet(String routeSheet) { this.routeSheet = routeSheet; }

    public Date getDepartTime() { return departTime; }
    public void setDepartTime(Date departTime) { this.departTime = departTime; }

    public Date getReturnTime() { return returnTime; }
    public void setReturnTime(Date returnTime) { this.returnTime = returnTime; }

    public int getMealCount() { return mealCount; }
    public void setMealCount(int mealCount) { this.mealCount = mealCount; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getParkingSpace() { return parkingSpace; }
    public void setParkingSpace(String parkingSpace) { this.parkingSpace = parkingSpace; }
}
