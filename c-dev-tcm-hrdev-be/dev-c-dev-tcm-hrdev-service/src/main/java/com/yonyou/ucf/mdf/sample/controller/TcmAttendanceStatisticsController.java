package com.yonyou.ucf.mdf.sample.controller;

import com.yonyou.ucf.mdf.sample.service.TcmAttendanceStatisticsService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 考勤统计控制器
 * 提供按月生成考勤统计记录的API
 *
 * @author aze
 */
@RestController
@RequestMapping("/attendance/statistics")
public class TcmAttendanceStatisticsController {

    @Autowired
    private TcmAttendanceStatisticsService service;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 根据月份从修正表生成考勤统计记录
     * 查询当月修正数据，对在修正表中存在但统计表中不存在的员工，生成统计记录
     *
     * @param dateStr 月份中任意日期 (yyyy-MM-dd)，如 "2026-07-15"
     * @return 包含correctionData、addList、insertResult的结果
     */
    @PostMapping("/generateMonthly")
    public ResponseEntity<?> generateMonthlyStatistics(@RequestParam("date") String dateStr) {

        Date date = parseDate(dateStr);
        if (date == null) {
            return ResponseUtil.error(new IllegalArgumentException("日期格式错误，请使用 yyyy-MM-dd 格式"));
        }

        Map<String, Object> result = service.getTcmAttendanceStatisticsInfoByMonth(date);
        return ResponseUtil.success("月考勤统计记录生成完成", result);
    }

    private Date parseDate(String dateStr) {
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
}
