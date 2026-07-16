package com.yonyou.ucf.mdf.sample.controller;

import com.yonyou.ucf.mdf.sample.service.TcmWaybillImportService;
import com.yonyou.ucf.mdf.sample.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/waybill")
public class TcmWaybillImportController {

    @Autowired
    private TcmWaybillImportService waybillImportService;

    @PostMapping("/import")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = waybillImportService.importWaybillExcel(file);
            return ResponseUtil.success("导入完成", result);
        } catch (Exception e) {
            return ResponseUtil.error(e);
        }
    }
}
