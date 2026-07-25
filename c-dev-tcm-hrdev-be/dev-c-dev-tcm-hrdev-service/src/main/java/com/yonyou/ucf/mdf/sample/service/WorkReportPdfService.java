package com.yonyou.ucf.mdf.sample.service;

import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.DailyWorkRecordDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.EmployeeInfoDTO;
import com.yonyou.ucf.mdf.sample.bill.entity.dto.WorkReportRenderDTO;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 工作资料PDF报表生成服务
 * @author aze
 */
@Service
public class WorkReportPdfService {

    /** 缓存的字体程序（文档无关，可安全跨PdfDocument复用），全局只加载一次避免OOM */
    private volatile FontProgram cachedFontProgram;
    /** 缓存的字体临时文件路径，用于服务销毁时清理 */
    private Path cachedFontTempFile;

    @PostConstruct
    public void init() throws IOException {
        InputStream fontStream = getClass().getResourceAsStream("/fonts/simsun.ttc");
        if (fontStream == null) {
            throw new IOException("未找到中文字体文件 /fonts/simsun.ttc，无法进行PDF渲染生成");
        }
        // 将字体拷贝到临时文件（只做一次）
        cachedFontTempFile = Files.createTempFile("simsun-", ".ttc");
        Files.copy(fontStream, cachedFontTempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        fontStream.close();
        // 加载字体程序并缓存（FontProgram与文档无关，可安全复用）
        cachedFontProgram = FontProgramFactory.createFont(cachedFontTempFile.toString() + ",0");
    }

    @PreDestroy
    public void destroy() throws IOException {
        if (cachedFontTempFile != null) {
            Files.deleteIfExists(cachedFontTempFile);
        }
    }

    /**
     * 生成每日工作资料PDF，直接写入指定OutputStream（不关闭流，调用方负责关闭）
     * @param renderData 渲染数据
     * @param os 目标输出流
     */
    public void generateWorkReport(WorkReportRenderDTO renderData, OutputStream os) throws IOException {
        PdfWriter writer = new PdfWriter(os);
        writer.setCloseStream(false);
        renderPdf(renderData, writer);
    }

    /**
     * 生成每日工作资料PDF
     * @param renderData 渲染数据（员工信息+每日记录）
     * @return PDF字节数组
     */
    public byte[] generateWorkReport(WorkReportRenderDTO renderData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        renderPdf(renderData, writer);
        return baos.toByteArray();
    }

    /**
     * PDF渲染核心逻辑（与输出流无关，可复用）
     */
    private void renderPdf(WorkReportRenderDTO renderData, PdfWriter writer) throws IOException {
        EmployeeInfoDTO emp = renderData.getEmployeeInfo();
        List<DailyWorkRecordDTO> records = renderData.getDailyWorkRecords();

        PdfDocument pdfDoc = new PdfDocument(writer);

        // 纵向A4
        pdfDoc.setDefaultPageSize(PageSize.A4);
        Document document = new Document(pdfDoc);

        // 从缓存的FontProgram创建PdfFont（每个PdfDocument需要独立的PdfFont实例）
        PdfFont chineseFont = PdfFontFactory.createFont(cachedFontProgram);

        // 大标题 =======
        document.add(new Paragraph("每日工作資料")
                .setFont(chineseFont)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold());
        document.add(new Paragraph(" "));

        // 员工信息表格
        Table infoTable = new Table(UnitValue.createPercentArray(
                new float[]{100f/6, 100f/6, 100f/6, 100f/6, 100f/6, 100f/6}));
        infoTable.setWidth(UnitValue.createPercentValue(100));
        infoTable.setFontSize(7);
        // 第一行
        Cell nameCell = new Cell(1, 3)
                .add(new Paragraph("員工姓名：" + emp.getName()).setFont(chineseFont))
                .setPadding(3);
        Cell deptCell = new Cell(1, 3)
                .add(new Paragraph("部門：" + emp.getDepartment()).setFont(chineseFont))
                .setPadding(3);
        infoTable.addCell(nameCell);
        infoTable.addCell(deptCell);
        // 第二行
        Cell idCell = new Cell(1, 2)
                .add(new Paragraph("員工编号：" + emp.getCode()).setFont(chineseFont))
                .setPadding(3);
        Cell posCell = new Cell(1, 2)
                .add(new Paragraph("崗位：" + emp.getPosition()).setFont(chineseFont))
                .setPadding(3);
        Cell monthCell = new Cell(1, 2)
                .add(new Paragraph("月份：" + emp.getMonth()).setFont(chineseFont))
                .setPadding(3);
        infoTable.addCell(idCell);
        infoTable.addCell(posCell);
        infoTable.addCell(monthCell);
        document.add(infoTable);
        document.add(new Paragraph(" "));

        // 小标题
        document.add(new Paragraph("每日明细").setFont(chineseFont).setBold().setFontSize(7));

        // 动态数据表格
        float[] columnWidths = {4.55f, 9.09f, 9.09f, 9.09f, 9.09f, 9.09f, 9.09f,
                6.82f, 6.82f, 6.82f, 6.82f, 6.82f, 6.82f};
        Table dataTable = new Table(UnitValue.createPercentArray(columnWidths));
        dataTable.setWidth(UnitValue.createPercentValue(100));
        dataTable.setFontSize(7);
        String[] headers = {"星期", "日期", "出勤類型", "工作時數", "加班時數",
                "深宵時數", "遲到早退時長", "收取車次數", "入油次數", "加氣次數", "上下更", "頂更", "夜更"};
        for (String h : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(h).setFont(chineseFont).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            // 使用 addHeaderCell 使表头在每页重复
            dataTable.addHeaderCell(headerCell);
        }
        for (DailyWorkRecordDTO r : records) {
            dataTable.addCell(createDataCell(r.getWeek(), chineseFont));
            dataTable.addCell(createDataCell(r.getDate(), chineseFont));
            dataTable.addCell(createDataCell(r.getAttendanceType(), chineseFont));
            dataTable.addCell(createDataCell(r.getWorkPeriod(), chineseFont));
            dataTable.addCell(createDataCell(r.getOvertime(), chineseFont));
            dataTable.addCell(createDataCell(r.getLateNight(), chineseFont));
            dataTable.addCell(createDataCell(r.getEarlyLeave(), chineseFont));
            dataTable.addCell(createDataCell(intToStr(r.getTicketCount()), chineseFont));
            dataTable.addCell(createDataCell(intToStr(r.getOilCount()), chineseFont));
            dataTable.addCell(createDataCell(intToStr(r.getGasCount()), chineseFont));
            dataTable.addCell(createDataCell(r.getUpperLowerShift(), chineseFont));
            dataTable.addCell(createDataCell(r.getTopShift(), chineseFont));
            dataTable.addCell(createDataCell(r.getNightShift(), chineseFont));
        }
        document.add(dataTable);

        document.close();
    }

    private Cell createInfoCell(String content, PdfFont font) {
        return new Cell()
                .add(new Paragraph(content).setFont(font))
                .setBorder(Border.NO_BORDER)
                .setPadding(3);
    }

    private Cell createDataCell(String content, PdfFont font) {
        return new Cell()
                .add(new Paragraph(content).setFont(font))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
    }

    /** Integer→显示字符串：null→""，有值→String.valueOf */
    private String intToStr(Integer val) {
        return val != null ? String.valueOf(val) : "";
    }
}
