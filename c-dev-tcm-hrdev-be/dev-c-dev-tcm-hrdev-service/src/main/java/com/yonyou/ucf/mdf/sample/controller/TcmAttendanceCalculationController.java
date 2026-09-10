package com.yonyou.ucf.mdf.sample.controller;

import com.yonyou.ucf.mdf.sample.service.TcmAttendanceCalculationService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 日考勤結果匯總控制器
 * 提供考勤修正数据同步到日考勤結果匯總的API
 *
 * @author aze
 */
@RestController
@RequestMapping("/attendance/calculation")
public class TcmAttendanceCalculationController {

    @Autowired
    private TcmAttendanceCalculationService service;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 根据日期同步修正数据到日考勤結果匯總
     * 从tcmAttendanceCorrection读取修正数据，映射后新增或更新到tcmAttendanceCalculation
     *
     * @param dateStr      日期 (yyyy-MM-dd)
     * @param employeeCode 员工代码（可选，为空则处理全部）
     * @return 包含insertCount、updateCount、correctionCount的结果
     */
    @PostMapping("/syncRevise")
    public ResponseEntity<?> syncRevise(
            @RequestParam("date") String dateStr,
            @RequestParam(value = "employeeCode", required = false) String employeeCode) {

        Date date = parseDate(dateStr);
        if (date == null) {
            return ResponseUtil.error(new IllegalArgumentException("日期格式错误，请使用 yyyy-MM-dd 格式"));
        }

        Map<String, Object> result = service.getAndUpdateAssessReviseByDate(date, employeeCode);
        return ResponseUtil.success("考勤修正数据同步完成", result);
    }

    private Date parseDate(String dateStr) {
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
}
