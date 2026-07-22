package com.yonyou.ucf.mdf.sample.utils;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Validated
public class DateUtil {

    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM = "yyyy-MM";
    public static final String YYYY = "yyyy";
    public static final String HH_MM_SS = "HH:mm:ss";
    public static final String HH_MM = "HH:mm";
    public static final String YYYYMMDD_HHMMSS = "yyyyMMdd HHmmss";
    public static final String YYYYMMDD_HHMM = "yyyyMMdd HHmm";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMM = "yyyyMM";
    public static final String HHMMSS = "HHmmss";
    public static final String HHMM = "HHmm";

    /**
     * 获取当前月的月首日期字符串 yyyy-MM-dd
     */
    public static String getCurrentMonthFirstDay() {
        return YearMonth.now().atDay(1).format(FORMATTER_YYYY_MM_DD);
    }

    /**
     * 获取当前月的月末日期字符串 yyyy-MM-dd
     */
    public static String getCurrentMonthLastDay() {
        return YearMonth.now().atEndOfMonth().format(FORMATTER_YYYY_MM_DD);
    }

    /**
     * 获取指定月份的首日
     * @param year 年份
     * @param month 月份（1-12）
     */
    public static String getMonthFirstDay(int year, int month) {
        return YearMonth.of(year, month).atDay(1).format(FORMATTER_YYYY_MM_DD);
    }

    /**
     * 获取指定月份的末日
     * @param year 年份
     * @param month 月份（1-12）
     */
    public static String getMonthLastDay(int year, int month) {
        return YearMonth.of(year, month).atEndOfMonth().format(FORMATTER_YYYY_MM_DD);
    }

    /**
     * 获取当前日期时间字符串
     * @param targetFormat 目标格式，支持DateTimeFormatter的所有合法模式
     * @return 格式化后的日期时间字符串
     * @throws IllegalArgumentException 当格式字符串为null、空白或非法时抛出
     */
    public static String getCurrentDateTime(@NotBlank(message = "目标格式不能为空") String targetFormat) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(targetFormat);
            return LocalDateTime.now().format(formatter);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("日期格式非法: " + targetFormat, e);
        }
    }

    /**
     * 获取当前日期前第N天的日期
     * @param targetFormat 目标日期格式（如：yyyy-MM-dd）
     * @param days 往前推的天数（必须大于0）
     * @return 格式化后的日期字符串
     * @throws IllegalArgumentException 当格式字符串为null、空白或非法时抛出
     */
    public static String getDateBeforeNow(@NotBlank(message = "目标格式不能为空") String targetFormat,
                                          @Min(value = 1, message = "天数必须大于0") int days) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(targetFormat);
            LocalDate targetDate = LocalDate.now().minusDays(days);
            return targetDate.format(formatter);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("日期格式非法: " + targetFormat, e);
        }
    }

    /**
     * 获取当前日期后第N天的日期
     * @param targetFormat 目标日期格式（如：yyyy-MM-dd）
     * @param days 往后推的天数（必须大于0）
     * @return 格式化后的日期字符串
     */
    public static String getDateAfterNow(@NotBlank(message = "目标格式不能为空") String targetFormat,
                                         @Min(value = 1, message = "天数必须大于0") int days) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(targetFormat);
            LocalDate targetDate = LocalDate.now().plusDays(days);
            return targetDate.format(formatter);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("日期格式非法: " + targetFormat, e);
        }
    }


    /**
     * 格式化时间字符串
     * <p>将一种格式的时间字符串转换为另一种格式的时间字符串</p>
     * <p>示例：将 "14:30:00" (HH:mm:ss) 转换为 "02:30 PM" (hh:mm a)</p>
     *
     * @param sourceFormat 源时间格式，如 "HH:mm:ss"
     * @param targetFormat 目标时间格式，如 "hh:mm a"
     * @param timeStr      需要转换的时间字符串，如 "14:30:00"
     * @return 转换后的时间字符串，如 "02:30 PM"
     * @throws IllegalArgumentException 如果时间字符串为null、空字符串或格式不匹配
     * @throws DateTimeParseException   如果时间字符串无法按源格式解析
     */
    public static String convertTimeFormat(@NotBlank(message = "源格式不能为空") String sourceFormat,
                             @NotBlank(message = "目标格式不能为空") String targetFormat,
                             @NotBlank(message = "时间字符串不能为空") String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("时间字符串不能为空");
        }
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(sourceFormat);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(targetFormat);
            LocalTime time = LocalTime.parse(timeStr, inputFormatter);
            return time.format(outputFormatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    String.format("无效的时间格式，期望格式: %s，实际输入: %s", sourceFormat, timeStr), e);
        }
    }


    /**
     * 将日期字符串转换为 Date 对象
     * <p>转换后的 Date 对象时间部分为当天 00:00:00（系统默认时区）</p>
     * <p>示例：stringToDate("yyyy-MM-dd", "2026-05-21") → Date 对象（2026-05-21 00:00:00）</p>
     *
     * @param targetFormat 日期字符串的格式，如 "yyyy-MM-dd"
     * @param dateStr      需要转换的日期字符串，如 "2026-05-21"
     * @return 转换后的 Date 对象（时间为当天 00:00:00）
     * @throws IllegalArgumentException 如果日期字符串为null、空字符串或格式不匹配
     * @throws DateTimeParseException   如果日期字符串无法按指定格式解析
     * @see ZoneId#systemDefault() 使用系统默认时区
     */
    public static Date stringToDate(@NotBlank(message = "目标格式不能为空") String targetFormat,
                             @NotBlank(message = "日期字符串不能为空") String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("日期字符串不能为空");
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(targetFormat);
            LocalDate localDate = LocalDate.parse(dateStr, formatter);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    String.format("无效的日期格式，期望格式: %s，实际输入: %s", targetFormat, dateStr), e);
        }
    }

    /**
     * 字符串转字符串的通用方法（日期类型）
     * 支持任意日期格式之间的转换
     *
     * @param sourceFormat 源日期格式
     * @param targetFormat 目标日期格式
     * @param dateStr      日期字符串
     * @return 转换后的日期字符串
     * @throws IllegalArgumentException 参数无效时抛出
     */
    public static String convertDateFormat(@NotBlank String sourceFormat,
                                           @NotBlank String targetFormat,
                                           @NotBlank String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("日期字符串不能为空");
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(sourceFormat));
            return date.format(DateTimeFormatter.ofPattern(targetFormat));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    String.format("日期转换失败，源格式: %s，目标格式: %s，输入值: %s",
                            sourceFormat, targetFormat, dateStr), e);
        }
    }

    /**
     * 格式化 Date 对象为字符串
     *
     * @param date    日期对象，为null时返回null
     * @param pattern 格式模式，如 "yyyy-MM-dd HH:mm:ss"
     * @return 格式化后的日期字符串
     * @throws IllegalArgumentException 当pattern为空或null时抛出
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty");
        }

        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(formatter);
    }


    /**
     * 计算相差的分钟数据
     * @param date1 .
     * @param date2 .
     * @return .
     */
    public static long dateDiffMinutes(Date date1, Date date2){
        // 计算毫秒差并转换为分钟
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return TimeUnit.MILLISECONDS.toMinutes(diffInMilliseconds);
    }


    /**
     * 计算两个日期的时间差，返回小时数（保留指定小数位数）
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @param scale 小数位数
     * @return 时间差（小时）
     */
    public static double getTimeDifferenceInHours(Date date1, Date date2, int scale) {
        double hours = getTimeDifferenceInHours(date1, date2);

        // 四舍五入到指定小数位
        double factor = Math.pow(10, scale);
        return Math.round(hours * factor) / factor;
    }

    /**
     * 计算两个日期的时间差，返回小时数（支持小数）
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return 时间差（小时），保留完整精度
     */
    public static double getTimeDifferenceInHours(Date date1, Date date2) {
        // 参数校验
        if (date1 == null || date2 == null) {
            return 0.0;
        }

        // 计算毫秒差
        long diffInMillis = Math.abs(date1.getTime() - date2.getTime());

        // 转换为小时（1小时 = 3600000毫秒）
        return (double) diffInMillis / (1000.0 * 60.0 * 60.0);
    }


    /**
     * 计算两个日期的时间差，返回 HH:mm 格式的字符串
     * @param date1 第一个日期（被减数）
     * @param date2 第二个日期（减数）
     * @return 时间差字符串，格式为 HH:mm
     */
    public static String getTimeDifference(Date date1, Date date2) {

        if (date1 == null || date2 == null) {
            return "00:00";
        }

        long diffInMillis = Math.abs(date1.getTime() - date2.getTime());

        long hours = diffInMillis / (60 * 60 * 1000);
        long minutes = (diffInMillis % (60 * 60 * 1000)) / (60 * 1000);

        return String.format("%02d:%02d", hours, minutes);
    }



    /**
     * 将多个时间差字符串相加
     * @param times 多个时间差字符串数组，每个格式为 HH:mm
     * @return 相加后的时间差字符串，格式为 HH:mm
     */
    public static String addTimeDifferences(String... times) {
        if (times == null || times.length == 0) {
            return "00:00";
        }

        int totalMinutes = 0;

        for (String time : times) {
            if (time == null || time.isEmpty() || !time.matches("\\d{2}:\\d{2}")) {
                continue;
            }

            String[] parts = time.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            totalMinutes += hours * 60 + minutes;
        }

        int totalHours = totalMinutes / 60;
        int totalMinutesRemainder = totalMinutes % 60;

        return String.format("%02d:%02d", totalHours, totalMinutesRemainder);
    }


}
