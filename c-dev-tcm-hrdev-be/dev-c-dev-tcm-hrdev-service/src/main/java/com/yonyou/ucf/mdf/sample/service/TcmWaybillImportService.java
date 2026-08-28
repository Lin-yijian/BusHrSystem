package com.yonyou.ucf.mdf.sample.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface TcmWaybillImportService {

    Map<String, Object> importWaybillExcel(MultipartFile file) throws Exception;
}
