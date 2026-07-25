package com.yonyou.ucf.mdf.sample.controller;

import com.yonyou.ucf.mdf.sample.bill.entity.dto.WorkReportRenderDTO;
import com.yonyou.ucf.mdf.sample.service.WorkReportPdfService;
import com.yonyou.ucf.mdf.sample.service.WorkReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 工作资料报表控制器
 * @author aze
 */
@RestController
@RequestMapping("/workReport")
public class WorkReportController {

    @Autowired
    private WorkReportService workReportService;

    @Autowired
    private WorkReportPdfService workReportPdfService;

    /**
     * 下载每日工作资料PDF报表（流式输出，支持上千条不OOM）
     * @param employeeId 员工编号
     * @param yearMonth 年月，如 2026-04（若传入 yyyy-MM-dd 则自动截取为 yyyy-MM）
     * @return 流式PDF/ZIP下载
     */
    @GetMapping("/report/pdf")
    public ResponseEntity<StreamingResponseBody> downloadReport(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String yearMonth) throws IOException {

        // 统一截取为 yyyy-MM 格式（前端可能传入 yyyy-MM-dd）
        if (yearMonth != null && yearMonth.length() >= 7) {
            yearMonth = yearMonth.substring(0, 7);
        }

        if (employeeId != null && !employeeId.isEmpty()) {
            // 指定了员工 → 单个PDF流式输出
            List<WorkReportRenderDTO> list = workReportService.getWorkReportData(employeeId, yearMonth);
            if (list.isEmpty()) {
                throw new RuntimeException("未查询到该员工" + employeeId + "在" + yearMonth + "的考勤数据");
            }
            WorkReportRenderDTO dto = list.get(0);
            String pdfName = "每日工作資料_"
                    + dto.getEmployeeInfo().getName() + "_"
                    + dto.getEmployeeInfo().getMonth() + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            setAttachmentHeader(headers, pdfName);
            return ResponseEntity.ok().headers(headers).body(
                    outputStream -> workReportPdfService.generateWorkReport(dto, outputStream));
        }

        // 未指定员工 → 一次查询全量数据，流式分组处理（内存中只保留当前员工组）
        List<Map<String, Object>> allData = workReportService.queryAllCorrectionData(yearMonth);
        if (allData.isEmpty()) {
            throw new RuntimeException("未查询到" + yearMonth + "的考勤数据");
        }

        // 流式分组：遍历按employee_id排序的数据，检测ID变化时处理上一组
        Path tempZipFile = Files.createTempFile("workreport-", ".zip");
        tempZipFile.toFile().deleteOnExit();
        try {
            Set<String> usedNames = new HashSet<>();
            int totalEmployees = 0;
            try (FileOutputStream fos = new FileOutputStream(tempZipFile.toFile());
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                String currentEmpId = null;
                List<Map<String, Object>> currentGroup = new ArrayList<>();

                for (Map<String, Object> record : allData) {
                    String empId = String.valueOf(record.get("employee_id"));
                    if (currentEmpId != null && !empId.equals(currentEmpId)) {
                        // 员工变了 → 处理上一组
                        WorkReportRenderDTO dto = workReportService.buildSingleReport(currentGroup, yearMonth);
                        writeToZip(dto, zos, usedNames);
                        currentGroup.clear();
                        totalEmployees++;
                    }
                    currentEmpId = empId;
                    currentGroup.add(record);
                }
                // 处理最后一组
                if (!currentGroup.isEmpty()) {
                    WorkReportRenderDTO dto = workReportService.buildSingleReport(currentGroup, yearMonth);
                    writeToZip(dto, zos, usedNames);
                    totalEmployees++;
                }

                if (totalEmployees == 1) {
                    // 实际只有1个员工 → 关闭ZIP，走单PDF流式输出
                    zos.close();
                    Files.deleteIfExists(tempZipFile);
                    // 重新查一次走单PDF（ZIP已关闭，无法回溯）
                    List<WorkReportRenderDTO> list = workReportService.getWorkReportData(
                            String.valueOf(allData.get(0).get("employee_id")), yearMonth);
                    WorkReportRenderDTO dto = list.get(0);
                    String pdfName = "每日工作資料_"
                            + dto.getEmployeeInfo().getName() + "_"
                            + dto.getEmployeeInfo().getMonth() + ".pdf";
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    setAttachmentHeader(headers, pdfName);
                    return ResponseEntity.ok().headers(headers).body(
                            outputStream -> workReportPdfService.generateWorkReport(dto, outputStream));
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String zipName = "每日工作資料_" + yearMonth + ".zip";
            setAttachmentHeader(headers, zipName);

            StreamingResponseBody streamBody = outputStream -> {
                byte[] buffer = new byte[8192];
                try (FileInputStream fis = new FileInputStream(tempZipFile.toFile())) {
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                } finally {
                    Files.deleteIfExists(tempZipFile);
                }
            };
            return ResponseEntity.ok().headers(headers).body(streamBody);
        } catch (Exception e) {
            Files.deleteIfExists(tempZipFile);
            throw e;
        }
    }

    /**
     * 设置 Content-Disposition 附件头（兼容所有浏览器，支持中文文件名）
     */
    private void setAttachmentHeader(HttpHeaders headers, String fileName) throws UnsupportedEncodingException {
        String encoded = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        // filename 供旧浏览器兜底，filename*=UTF-8'' 供现代浏览器（RFC 5987）
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded);
    }

    /**
     * 将单个员工的DTO写入ZIP（含重名去重）
     */
    private void writeToZip(WorkReportRenderDTO dto, ZipOutputStream zos, Set<String> usedNames) throws IOException {
        String empName = dto.getEmployeeInfo().getName();
        String month = dto.getEmployeeInfo().getMonth();
        String baseName = "每日工作資料_" + empName + "_" + month + ".pdf";
        String fileName = baseName;
        int suffix = 2;
        while (!usedNames.add(fileName)) {
            fileName = "每日工作資料_" + empName + "_" + month + "_" + suffix + ".pdf";
            suffix++;
        }
        zos.putNextEntry(new ZipEntry(fileName));
        workReportPdfService.generateWorkReport(dto, zos);
        zos.closeEntry();
    }
}
