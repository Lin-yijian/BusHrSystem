package com.yonyou.ucf.mdf.sample.utils;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {
    public ResponseUtil() {
    }

    public static ResponseEntity<?> success(String message) {
        Map<String, Object> response = new HashMap();
        response.put("code", "200");
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<?> success(String message, Object data) {
        Map<String, Object> response = new HashMap();
        response.put("code", "200");
        response.put("message", message);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<?> error(Exception e) {
        e.printStackTrace();
        Map<String, Object> errorResponse = new HashMap();
        errorResponse.put("code", "500");
        errorResponse.put("message", "操作失败: " + e.getMessage());
        return ResponseEntity.status(500).body(errorResponse);
    }

    /**
     * 接收自定义错误信息的方法
     * @param errorMessage 自定义错误信息
     * @return ResponseEntity 包含错误信息的响应
     */
    public static ResponseEntity<?> error(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap();
        errorResponse.put("code", "500");
        errorResponse.put("message", errorMessage);
        return ResponseEntity.status(500).body(errorResponse);
    }

    /**
     * 接收自定义错误信息和状态码的方法
     * @param errorMessage 自定义错误信息
     * @param statusCode HTTP状态码
     * @return ResponseEntity 包含错误信息的响应
     */
    public static ResponseEntity<?> error(String errorMessage, int statusCode) {
        Map<String, Object> errorResponse = new HashMap();
        errorResponse.put("code", String.valueOf(statusCode));
        errorResponse.put("message", errorMessage);
        return ResponseEntity.status(statusCode).body(errorResponse);
    }

    /**
     * 接收自定义错误信息和额外数据的方法
     * @param errorMessage 自定义错误信息
     * @param data 额外的错误数据
     * @return ResponseEntity 包含错误信息和数据的响应
     */
    public static ResponseEntity<?> error(String errorMessage, Object data) {
        Map<String, Object> errorResponse = new HashMap();
        errorResponse.put("code", "500");
        errorResponse.put("message", errorMessage);
        errorResponse.put("data", data);
        return ResponseEntity.status(500).body(errorResponse);
    }
}
