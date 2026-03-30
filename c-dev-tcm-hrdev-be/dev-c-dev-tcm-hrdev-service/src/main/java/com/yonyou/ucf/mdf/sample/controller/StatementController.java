package com.yonyou.ucf.mdf.sample.controller;

import com.yonyou.ucf.mdf.sample.bill.entity.StatementDetail;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.StatementRequestDto;
import com.yonyou.ucf.mdf.sample.service.impl.StatementService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statement")
public class StatementController {

    @Autowired
    private StatementService statementService;

    /**
     * 下载并解析对账文件
     */
    @PostMapping("/download")
    public ResponseEntity<?> downloadStatement(@RequestBody StatementRequestDto request) {
        try {
            List<StatementDetail> details = statementService.downloadStatement(
                    request.getMerchantId(),
                    request.getTerminalNo(),
                    request.getAcDate(),
                    // 从配置或密钥管理服务获取
                    request.getPrivateKey(),
                    request.isProd()
            );

            Map<String, Object> result = new HashMap<>();
            result.put("code", "000000");
            result.put("message", "对账成功");
            result.put("totalCount", details.size());
            result.put("data", details);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}