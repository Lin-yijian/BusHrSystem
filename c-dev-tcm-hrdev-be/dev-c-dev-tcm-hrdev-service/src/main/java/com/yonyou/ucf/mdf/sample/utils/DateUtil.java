package com.yonyou.ucf.mdf.sample.utils;

import io.edap.util.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
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
    public static final String ABSOLUTE = "ABSOLUTE";
    public static final String POSITIVE_ONLY = "POSITIVE_ONLY";
    public static final String HH_MM_ZERO = "00:00";

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
     *
     * @param year  年份
     * @param month 月份（1-12）
     */
    public static String getMonthFirstDay(int year, int month) {
        return YearMonth.of(year, month).atDay(1).format(FORMATTER_YYYY_MM_DD);
    }

    /**
     * 获取指定月份的末日
     *
     * @param year  年份
     * @param month 月份（1-12）
     */
    public static String getMonthLastDay(int year, int month) {
        return YearMonth.of(year, month).atEndOfMonth().format(FORMATTER_YYYY_MM_DD);
    }

    /**
     * 获取当前日期时间字符串
     *
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
     *
     * @param targetFormat 目标日期格式（如：yyyy-MM-dd）
     * @param days         往前推的天数（必须大于0）
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
     *
     * @param targetFormat 目标日期格式（如：yyyy-MM-dd）
     * @param days         往后推的天数（必须大于0）
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
     * @param targetFormat
     * @param dateStr
     * @return
     */
    public static Date stringToDate(@NotBlank(message = "目标格式不能为空") String targetFormat,
                                    @NotBlank(message = "日期字符串不能为空") String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("日期字符串不能为空");
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(targetFormat);

            // 判断格式是否包含时间
            if (targetFormat.contains("HH") || targetFormat.contains("hh") ||
                    targetFormat.contains("mm") || targetFormat.contains("ss")) {
                // 包含时间，使用 LocalDateTime
                LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
                return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            } else {
                // 只有日期，使用 LocalDate
                LocalDate localDate = LocalDate.parse(dateStr, formatter);
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
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
     *
     * @param date1 .
     * @param date2 .
     * @return .
     */
    public static long dateDiffMinutes(Date date1, Date date2) {
        // 计算毫秒差并转换为分钟
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return TimeUnit.MILLISECONDS.toMinutes(diffInMilliseconds);
    }


    /**
     * 计算两个日期的时间差，返回小时数（保留指定小数位数）
     *
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @param scale 小数位数
     * @return 时间差（小时），返回 BigDecimal 确保精度
     */
    public static BigDecimal getTimeDifferenceInHours(Date date1, Date date2, int scale) {
        if (date1 == null || date2 == null) {
            return BigDecimal.ZERO;
        }

        long diffInMillis = date1.getTime() - date2.getTime();
        // 使用 BigDecimal 进行精确计算
        return BigDecimal.valueOf(diffInMillis)
                .divide(BigDecimal.valueOf(3600000), scale, RoundingMode.HALF_UP);
    }

    /**
     * 计算两个日期的时间差，返回小时数（支持小数）
     *
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
     * 计算两个日期的时间差，返回 HH:mm 格式的字符串（返回绝对值）
     *
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return 时间差字符串，格式为 HH:mm
     */
    public static String getTimeDifference(Date date1, Date date2) {
        return getTimeDifference(date1, date2, true, HH_MM_ZERO);
    }

    /**
     * 计算两个日期的时间差，返回 HH:mm 格式的字符串
     *
     * @param date1       第一个日期
     * @param date2       第二个日期
     * @param useAbsolute 是否返回绝对值，true=返回绝对值，false=date1小于date2时返回 HH_MM_ZERO
     * @return 时间差字符串，格式为 HH:mm
     */
    public static String getTimeDifference(Date date1, Date date2, boolean useAbsolute) {
        return getTimeDifference(date1, date2, useAbsolute, HH_MM_ZERO);
    }

    /**
     * 计算两个日期的时间差，返回 HH:mm 格式的字符串
     *
     * @param date1        第一个日期
     * @param date2        第二个日期
     * @param useAbsolute  是否返回绝对值，true=返回绝对值，false=date1小于date2时返回defaultValue
     * @param defaultValue 当 useAbsolute=false 且 date1 < date2 时返回的默认值，为null时返回 HH_MM_ZERO
     * @return 时间差字符串，格式为 HH:mm
     */
    public static String getTimeDifference(Date date1, Date date2, boolean useAbsolute, String defaultValue) {
        // 空值校验，统一使用默认值
        if (date1 == null || date2 == null) {
            return getDefaultValue(defaultValue);
        }

        long diffInMillis = date1.getTime() - date2.getTime();

        // 负数且不返回绝对值时，返回默认值
        if (diffInMillis < 0 && !useAbsolute) {
            return getDefaultValue(defaultValue);
        }

        // 取绝对值并格式化
        diffInMillis = Math.abs(diffInMillis);
        long hours = diffInMillis / (60 * 60 * 1000);
        long minutes = (diffInMillis % (60 * 60 * 1000)) / (60 * 1000);

        return String.format("%02d:%02d", hours, minutes);
    }

    /**
     * 获取默认值，若为null则返回 HH_MM_ZERO
     */
    private static String getDefaultValue(String defaultValue) {
        return defaultValue != null ? defaultValue : HH_MM_ZERO;
    }



    public static boolean checkIsZeroTime(String timeStr) {
        return StringUtil.isEmpty(timeStr) || HH_MM_ZERO.equals(timeStr) || "0:00".equals(timeStr) || "00:00:00".equals(timeStr);
    }

    /**
     * 将日期字符串归一为 yyyy-MM-dd。
     * 兼容 yyyy-MM-dd、yyyy-MM-dd HH:mm:ss、yyyy/MM/dd、yyyyMMdd。
     */
    public static String normalizeToYyyyMmDd(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        String s = dateStr.trim();
        if (s.isEmpty()) {
            return null;
        }
        s = s.replace('/', '-');
        if (s.length() >= 10 && s.charAt(4) == '-' && s.charAt(7) == '-') {
            String candidate = s.substring(0, 10);
            if (candidate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return candidate;
            }
        }
        if (s.matches("\\d{8}")) {
            return s.substring(0, 4) + "-" + s.substring(4, 6) + "-" + s.substring(6, 8);
        }
        return null;
    }

    /**
     * 查询核对日期时同时匹配 yyyy-MM-dd 与带时分秒的写法，避免查不到已有记录。
     */
    public static Set<String> dateQueryVariants(String dateStr) {
        Set<String> variants = new HashSet<>();
        if (dateStr != null) {
            String trimmed = dateStr.trim();
            if (!trimmed.isEmpty()) {
                variants.add(trimmed);
            }
        }
        String normalized = normalizeToYyyyMmDd(dateStr);
        if (normalized != null) {
            variants.add(normalized);
            variants.add(normalized + " 00:00:00");
        }
        return variants;
    }

    public static Set<String> dateQueryVariants(Collection<String> dates) {
        Set<String> variants = new HashSet<>();
        if (dates == null) {
            return variants;
        }
        for (String date : dates) {
            variants.addAll(dateQueryVariants(date));
        }
        return variants;
    }

    /**
     * 从实体字段中取出 yyyy-MM-dd。兼容 Date、yyyy-MM-dd、yyyy-MM-dd HH:mm:ss。
     */
    public static String extractDateString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return formatDate((Date) value, YYYY_MM_DD);
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(FORMATTER_YYYY_MM_DD);
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
            return null;
        }
        return normalizeToYyyyMmDd(s);
    }

    /**
     * from - to 的有符号分钟差。任一为空返回 null。
     */
    public static Integer signedDiffMinutes(Date from, Date to) {
        if (from == null || to == null) {
            return null;
        }
        return (int) TimeUnit.MILLISECONDS.toMinutes(from.getTime() - to.getTime());
    }

    /**
     * 分钟数转 HH:mm。负数按 0 处理。
     */
    public static String minutesToHhMm(int minutes) {
        if (minutes < 0) {
            minutes = 0;
        }
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }



    /**
     * 将多个时间差字符串相加
     *
     * @param times 多个时间差字符串数组，每个格式为 HH:mm
     * @return 相加后的时间差字符串，格式为 HH:mm
     */
    public static String addTimeDifferences(String... times) {
        if (times == null || times.length == 0) {
            return HH_MM_ZERO;
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

    /**
     * 计算两个时间差字符串的差值（第一个减第二个）
     *
     * @param time1 第一个时间差字符串，格式为 HH:mm（被减数）
     * @param time2 第二个时间差字符串，格式为 HH:mm（减数）
     * @return 差值字符串，格式为 HH:mm（绝对值，始终为正）
     */
    public static String subtractTimeDifferences(String time1, String time2) {

        int diffMinutes = diffByTimeString(time1, time2);

        // 转换为小时和分钟
        int diffHours = diffMinutes / 60;
        int diffMinutesRemainder = diffMinutes % 60;

        return String.format("%02d:%02d", diffHours, diffMinutesRemainder);
    }

    /**
     * 计算两个时间差字符串的差值（返回小时数）
     *
     * @param time1 第一个时间差字符串，格式为 HH:mm（被减数）
     * @param time2 第二个时间差字符串，格式为 HH:mm（减数）
     * @return 差值（小时），double类型
     */
    public static double subtractTimeDifferencesToHours(String time1, String time2) {

        int diffMinutes = diffByTimeString(time1, time2);

        // 转换为小时
        return diffMinutes / 60.0;
    }

    private static int diffByTimeString(String time1, String time2) {
        // 参数校验
        if (time1 == null || time1.isEmpty() || !time1.matches("\\d{2}:\\d{2}")) {
            time1 = HH_MM_ZERO;
        }
        if (time2 == null || time2.isEmpty() || !time2.matches("\\d{2}:\\d{2}")) {
            time2 = HH_MM_ZERO;
        }

        // 解析时间
        String[] parts1 = time1.split(":");
        String[] parts2 = time2.split(":");

        int hours1 = Integer.parseInt(parts1[0]);
        int minutes1 = Integer.parseInt(parts1[1]);
        int hours2 = Integer.parseInt(parts2[0]);
        int minutes2 = Integer.parseInt(parts2[1]);

        // 计算总分钟数
        int totalMinutes1 = hours1 * 60 + minutes1;
        int totalMinutes2 = hours2 * 60 + minutes2;

        return Math.abs(totalMinutes1 - totalMinutes2);
    }

    /**
     * 将 HH:mm 格式的字符串转换为小时数（double）
     *
     * @param timeStr HH:mm 格式的时间字符串，如 "01:30"
     * @return 小时数，如 1.5
     */
    public static double timeStringToHours(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return 0.0;
        }

        // 校验格式 HH:mm
        if (!timeStr.matches("\\d{1,2}:\\d{2}")) {
            throw new IllegalArgumentException("时间格式错误，应为 HH:mm，当前值: " + timeStr);
        }

        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        // 分钟数不能超过59
        if (minutes >= 60) {
            throw new IllegalArgumentException("分钟数不能大于59，当前值: " + minutes);
        }

        return hours + minutes / 60.0;
    }

    /**
     * 将 HH:mm 格式的字符串转换为小时数（BigDecimal，高精度）
     *
     * @param timeStr HH:mm 格式的时间字符串
     * @param scale   小数位数
     * @return 小时数（BigDecimal）
     */
    public static BigDecimal timeStringToHoursBigDecimal(String timeStr, int scale) {
        if (timeStr == null || timeStr.isEmpty()) {
            return BigDecimal.ZERO;
        }

        if (!timeStr.matches("\\d{1,2}:\\d{2}")) {
            throw new IllegalArgumentException("时间格式错误，应为 HH:mm，当前值: " + timeStr);
        }

        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        if (minutes >= 60) {
            throw new IllegalArgumentException("分钟数不能大于59，当前值: " + minutes);
        }

        BigDecimal hoursBd = BigDecimal.valueOf(hours);
        BigDecimal minutesBd = BigDecimal.valueOf(minutes);
        BigDecimal minutesInHours = minutesBd.divide(BigDecimal.valueOf(60), scale, RoundingMode.HALF_UP);

        return hoursBd.add(minutesInHours);
    }

    /**
     * 将小时数（double）转换为 HH:mm 格式的字符串
     *
     * @param hours 小时数，如 1.5
     * @return HH:mm 格式的字符串，如 "01:30"
     */
    public static String hoursToTimeString(double hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("小时数不能为负数: " + hours);
        }

        int totalMinutes = (int) Math.round(hours * 60);
        int h = totalMinutes / 60;
        int m = totalMinutes % 60;

        return String.format("%02d:%02d", h, m);
    }

    /**
     * 将小时数（double）转换为 HH:mm 格式的字符串（支持自定义舍入模式）
     *
     * @param hours        小时数，如 1.5
     * @param roundingMode 舍入模式
     * @return HH:mm 格式的字符串
     */
    public static String hoursToTimeString(double hours, RoundingMode roundingMode) {
        if (hours < 0) {
            throw new IllegalArgumentException("小时数不能为负数: " + hours);
        }

        BigDecimal hoursBd = BigDecimal.valueOf(hours);
        BigDecimal minutesBd = hoursBd.multiply(BigDecimal.valueOf(60));
        int totalMinutes = minutesBd.setScale(0, roundingMode).intValue();

        int h = totalMinutes / 60;
        int m = totalMinutes % 60;

        return String.format("%02d:%02d", h, m);
    }

    /**
     * 将小时数（BigDecimal）转换为 HH:mm 格式的字符串
     *
     * @param hours 小时数（BigDecimal）
     * @return HH:mm 格式的字符串
     */
    public static String hoursToTimeString(BigDecimal hours) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("小时数不能为负数");
        }

        BigDecimal minutesBd = hours.multiply(BigDecimal.valueOf(60));
        int totalMinutes = minutesBd.setScale(0, RoundingMode.HALF_UP).intValue();

        int h = totalMinutes / 60;
        int m = totalMinutes % 60;

        return String.format("%02d:%02d", h, m);
    }

    /**
     * 将小时数（double）转换为 HH:mm 格式的字符串（保留小数部分为分钟）
     *
     * @param hours 小时数，如 1.29611111
     * @param scale 小数位数（用于精度控制）
     * @return HH:mm 格式的字符串，如 "01:18"
     */
    public static String hoursToTimeStringPrecise(double hours, int scale) {
        if (hours < 0) {
            throw new IllegalArgumentException("小时数不能为负数: " + hours);
        }

        BigDecimal hoursBd = BigDecimal.valueOf(hours);
        BigDecimal minutesBd = hoursBd.multiply(BigDecimal.valueOf(60));
        int totalMinutes = minutesBd.setScale(scale, RoundingMode.HALF_UP).intValue();

        int h = totalMinutes / 60;
        int m = totalMinutes % 60;

        return String.format("%02d:%02d", h, m);
    }


    /**
     * 比较两个 HH:mm 格式的时间字符串大小
     *
     * @param time1 第一个时间字符串，格式为 HH:mm
     * @param time2 第二个时间字符串，格式为 HH:mm
     * @return 1: time1 > time2, 0: time1 == time2, -1: time1 < time2
     */
    public static int compareTimeStrings(String time1, String time2) {
        // 参数校验
        if (time1 == null || !time1.matches("\\d{2}:\\d{2}")) {
            time1 = HH_MM_ZERO;
        }
        if (time2 == null || !time2.matches("\\d{2}:\\d{2}")) {
            time2 = HH_MM_ZERO;
        }

        String[] parts1 = time1.split(":");
        String[] parts2 = time2.split(":");

        int hours1 = Integer.parseInt(parts1[0]);
        int minutes1 = Integer.parseInt(parts1[1]);
        int hours2 = Integer.parseInt(parts2[0]);
        int minutes2 = Integer.parseInt(parts2[1]);

        // 先比较小时
        if (hours1 > hours2) {
            return 1;
        } else if (hours1 < hours2) {
            return -1;
        } else {
            // 小时相同，比较分钟
            if (minutes1 > minutes2) {
                return 1;
            } else if (minutes1 < minutes2) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * 比较两个 HH:mm 格式的时间字符串大小（返回布尔值）
     *
     * @param time1 第一个时间字符串
     * @param time2 第二个时间字符串
     * @return true: time1 > time2
     */
    public static boolean isTimeGreater(String time1, String time2) {
        return compareTimeStrings(time1, time2) > 0;
    }

    /**
     * 检查 time1 是否大于等于 time2
     */
    public static boolean isTimeGreaterOrEqual(String time1, String time2) {
        return compareTimeStrings(time1, time2) >= 0;
    }

    /**
     * 检查 time1 是否小于 time2
     */
    public static boolean isTimeLess(String time1, String time2) {
        return compareTimeStrings(time1, time2) < 0;
    }

    /**
     * 检查 time1 是否小于等于 time2
     */
    public static boolean isTimeLessOrEqual(String time1, String time2) {
        return compareTimeStrings(time1, time2) <= 0;
    }

    /**
     * 检查两个时间是否相等
     */
    public static boolean isTimeEqual(String time1, String time2) {
        return compareTimeStrings(time1, time2) == 0;
    }

    /**
     * 获取两个时间中较大的那个
     *
     * @param time1 第一个时间字符串
     * @param time2 第二个时间字符串
     * @return 较大的时间字符串
     */
    public static String maxTime(String time1, String time2) {
        return compareTimeStrings(time1, time2) >= 0 ? time1 : time2;
    }

    /**
     * 获取两个时间中较小的那个
     *
     * @param time1 第一个时间字符串
     * @param time2 第二个时间字符串
     * @return 较小的时间字符串
     */
    public static String minTime(String time1, String time2) {
        return compareTimeStrings(time1, time2) <= 0 ? time1 : time2;
    }

    /**
     * 比较多个时间，返回最大值
     *
     * @param times 多个时间字符串
     * @return 最大的时间字符串
     */
    public static String maxTime(String... times) {
        if (times == null || times.length == 0) {
            return HH_MM_ZERO;
        }

        String max = times[0];
        for (int i = 1; i < times.length; i++) {
            if (compareTimeStrings(times[i], max) > 0) {
                max = times[i];
            }
        }
        return max;
    }

    /**
     * 比较多个时间，返回最小值
     *
     * @param times 多个时间字符串
     * @return 最小的时间字符串
     */
    public static String minTime(String... times) {
        if (times == null || times.length == 0) {
            return HH_MM_ZERO;
        }

        String min = times[0];
        for (int i = 1; i < times.length; i++) {
            if (compareTimeStrings(times[i], min) < 0) {
                min = times[i];
            }
        }
        return min;
    }

    /**
     * 检查时间是否在指定范围内（包含边界）
     *
     * @param time  要检查的时间
     * @param start 起始时间
     * @param end   结束时间
     * @return true: 在范围内
     */
    public static boolean isTimeInRange(String time, String start, String end) {
        return compareTimeStrings(time, start) >= 0 && compareTimeStrings(time, end) <= 0;
    }

    /**
     * 检查时间是否在指定范围内（不包含边界）
     *
     * @param time  要检查的时间
     * @param start 起始时间
     * @param end   结束时间
     * @return true: 在范围内
     */
    public static boolean isTimeInRangeExclusive(String time, String start, String end) {
        return compareTimeStrings(time, start) > 0 && compareTimeStrings(time, end) < 0;
    }


    /**
     * 计算两个时间的差
     *
     * @param startDate .
     * @param endDate   .
     * @return 单位（小时）
     */
    public static double getHoursDiff(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0.0;
        }
        long diffInMillis = endDate.getTime() - startDate.getTime();
        double hours = diffInMillis / (1000.0 * 60 * 60);
        return Double.parseDouble(String.format("%.2f", hours));
    }


    /**
     * 计算两个时间的差
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 时间差，格式为 HH:mm，最大值 99:59
     */
    public static String getHoursDiffAsHHmm(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return "00:00";
        }

        long diffInMillis = endDate.getTime() - startDate.getTime();
        long totalMinutes = diffInMillis / (1000 * 60);

        // 最大值限制为 99:59（5999分钟）
        long maxMinutes = 99 * 60 + 59; // 5999
        if (totalMinutes > maxMinutes) {
            totalMinutes = maxMinutes;
        }

        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        return String.format("%02d:%02d", hours, minutes);
    }

}
