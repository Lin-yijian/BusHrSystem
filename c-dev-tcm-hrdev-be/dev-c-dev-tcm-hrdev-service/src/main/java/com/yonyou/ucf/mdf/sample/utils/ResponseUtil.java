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
}
