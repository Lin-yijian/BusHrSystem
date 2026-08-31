package com.yonyou.ucf.mdf.sample.enums.params;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * 参数键的统一抽取契约。
 * <p>
 * 每个模块独立的 *ParamKeyEnum（例如 {@link TcmDriverOvertimeParamKeyEnum}）都实现此接口，
 * 从而允许上游（例如 {@code TcmParamManageService#queryParamValue}）
 * 用统一强类型接收任意 ParamKey 枚举值，并提取：
 * <ul>
 *   <li>{@link #getCode()} — 对应 TCMParamManage.key 字段，作为查询 key</li>
 *   <li>{@link #getName()} — 对应 TCMParamManage.name 字段，用于异常提示的可读名称</li>
 *   <li>{@link #getCheckType()} — 参数值格式校验类型，查询参数时按此校验（见下方常量说明）</li>
 * </ul>
 */
public interface ParamKey {

    // =============================================================================================
    // checkType 校验类型常量
    // 支持范围后缀 ":min-max"（闭区间），例如 INT:0-1440、DECIMAL:0-100
    // =============================================================================================

    /** 字符串：非空即可，不做格式校验 */
    String CHECK_TYPE_STRING = "STRING";
    /** 时间/时长：HH:mm（小时 0-99，分钟 00-59），例如 08:00、06:30、48:00 */
    String CHECK_TYPE_TIME_HH_MM = "TIME_HH_MM";
    /** 日期：yyyy-MM-dd */
    String CHECK_TYPE_DATE = "DATE";
    /** 日期时间：yyyy-MM-dd HH:mm */
    String CHECK_TYPE_DATETIME = "DATETIME";
    /** 整数，可带范围：INT 或 INT:min-max */
    String CHECK_TYPE_INT = "INT";
    /** 数字（整数或小数），可带范围：DECIMAL 或 DECIMAL:min-max */
    String CHECK_TYPE_DECIMAL = "DECIMAL";

    /** HH:mm 时长格式（小时 0-99，分钟 00-59） */
    Pattern TIME_HH_MM_PATTERN = Pattern.compile("^\\d{1,2}:[0-5]\\d$");
    /** yyyy-MM-dd 日期格式（严格校验，如 02-30 会判非法） */
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    /** yyyy-MM-dd HH:mm 日期时间格式 */
    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm");

    /**
     * @return 对应 TCMParamManage.key 的值，非空
     */
    String getCode();

    /**
     * @return 对应 TCMParamManage.name 的值（参数名称 / 中文说明），用于错误提示
     */
    String getName();

    /**
     * @return 参数值格式校验类型（见本接口 CHECK_TYPE_* 常量；null/空/STRING 均表示仅要求非空）
     */
    String getCheckType();

    /**
     * 统一的参数值格式校验入口（查询参数后调用）。
     * <p>按 {@link #getCheckType()} 校验 value 的格式与数字范围：
     * <ul>
     *   <li>null/空白 → 抛"配置非法 ... =空"</li>
     *   <li>TIME_HH_MM → 必须匹配 HH:mm（小时 0-99、分钟 00-59）</li>
     *   <li>DATE → 必须是合法 yyyy-MM-dd 日期</li>
     *   <li>DATETIME → 必须是合法 yyyy-MM-dd HH:mm</li>
     *   <li>INT[:min-max] → 必须是整数且在闭区间 [min,max] 内</li>
     *   <li>DECIMAL[:min-max] → 必须是数字且在闭区间 [min,max] 内</li>
     *   <li>STRING / 空 → 非空即可</li>
     * </ul>
     *
     * @param key   参数键枚举（用于异常提示 name[code]）
     * @param value 待校验的参数值
     * @return 校验通过后的去空白值
     * @throws RuntimeException 格式非法时抛出："配置非法：name[code]=value，請至【參數管理】菜單修正"
     */
    static String checkValue(ParamKey key, String value) {
        String name = key.getName();
        String code = key.getCode();
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("配置非法：" + name + "[" + code + "]=空，請至【參數管理】菜單修正");
        }
        String v = value.trim();
        String type = key.getCheckType() == null ? "" : key.getCheckType().trim();
        try {
            if (type.isEmpty() || CHECK_TYPE_STRING.equals(type)) {
                return v;
            }
            if (CHECK_TYPE_TIME_HH_MM.equals(type)) {
                if (!TIME_HH_MM_PATTERN.matcher(v).matches()) {
                    throw new IllegalArgumentException("需 HH:mm 格式（例如 01:00）");
                }
                return v;
            }
            if (CHECK_TYPE_DATE.equals(type)) {
                LocalDate.parse(v, DATE_FORMATTER);
                return v;
            }
            if (CHECK_TYPE_DATETIME.equals(type)) {
                LocalDateTime.parse(v, DATETIME_FORMATTER);
                return v;
            }
            if (type.startsWith(CHECK_TYPE_INT)) {
                long num = Long.parseLong(v);
                checkRange(type, num);
                return v;
            }
            if (type.startsWith(CHECK_TYPE_DECIMAL)) {
                BigDecimal num = new BigDecimal(v);
                checkRange(type, num);
                return v;
            }
            // checkType 本身维护错误
            throw new IllegalArgumentException("checkType 未定义：" + type);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("配置非法")) {
                throw e;
            }
            throw new RuntimeException("配置非法：" + name + "[" + code + "]=" + value
                    + "（" + e.getMessage() + "），請至【參數管理】菜單修正");
        }
    }

    /**
     * 校验 ":min-max" 范围（闭区间）；无范围后缀直接通过。
     * <p>注意：接口中不支持 private 方法（Java 8 兼容），故为 public，仅供 {@link #checkValue} 内部使用。
     */
    static void checkRange(String type, Number num) {
        int idx = type.indexOf(':');
        if (idx < 0) {
            return;
        }
        String[] range = type.substring(idx + 1).split("-", 2);
        BigDecimal n = new BigDecimal(num.toString());
        BigDecimal min = new BigDecimal(range[0].trim());
        BigDecimal max = new BigDecimal(range[1].trim());
        if (n.compareTo(min) < 0 || n.compareTo(max) > 0) {
            throw new IllegalArgumentException("超出范围 [" + min.stripTrailingZeros().toPlainString()
                    + "-" + max.stripTrailingZeros().toPlainString() + "]");
        }
    }
}
