package com.yonyou.ucf.mdf.sample.utils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 签名参数拼接工具类
 */
public class SignUtil {

    /**
     * 拼接待签名字符串
     * 规则：参数名ASCII码从小到大排序，将对应的value拼接成待签名串（value为空的域不参与）
     */
    public static String buildSignContent(Object obj) throws Exception {
        Map<String, Object> map = new TreeMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = field.get(obj);

            // 跳过签名域和空值
            if ("merchantSign".equals(fieldName) || "serverSign".equals(fieldName)) {
                continue;
            }

            if (value != null && !"".equals(value.toString())) {
                map.put(fieldName, value);
            }
        }

        // 按ASCII码顺序拼接
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getValue());
        }

        return sb.toString();
    }

    /**
     * 拼接响应待签名字符串
     */
    public static String buildResponseSignContent(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if ("serverSign".equals(key) || "merchantSign".equals(key)) {
                continue;
            }
            String value = params.get(key);
            if (value != null && !value.isEmpty()) {
                sb.append(value);
            }
        }

        return sb.toString();
    }


}
