package com.nongxinle.controller;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
//import com.itextpdf.layout.property.TextAlignment;
//import com.itextpdf.layout.property.UnitValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.layout.element.Paragraph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;


@RestController
@RequestMapping("/api/reportPDF")
public class RestaurantCostReportController {



//    @RequestMapping("/downloadReportPdfGb")
//    public void downloadReportPdfGb(HttpServletResponse response, Integer id, String startDate, String stopDate) {
//        System.out.println("idididid" + id);
//        response.setContentType("application/pdf");
//        response.setHeader("Content-Disposition", "attachment; filename=restaurant_report.pdf");
//
//        try {
//            System.out.println("开始加载字体...");
//            String fontPath = "fonts/Hiragino Sans GB.ttc";
//
//            InputStream fontStream = getClass().getClassLoader().getResourceAsStream(fontPath);
//            if (fontStream == null) {
//                System.err.println("字体文件未找到，路径：" + fontPath);
//                throw new IOException("字体文件未找到");
//            }
//            byte[] fontData = IOUtils.toByteArray(fontStream);
//            System.out.println("字体文件已加载，大小：" + fontData.length + " 字节");
//
//            // ✅ 加载字体
//            PdfFont font = PdfFontFactory.createTtcFont(fontData, 0, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED, true);
//            System.out.println("字体创建成功：" + font.getFontProgram().getFontNames().getFontName());
//
//            // ✅ 创建 PDF 文档
//            try (OutputStream out = response.getOutputStream();
//                 PdfWriter writer = new PdfWriter(out);
//                 PdfDocument pdf = new PdfDocument(writer);
//                 Document document = new Document(pdf, PageSize.A4)) {
//
//                // ✅ 添加标题
//                document.add(new Paragraph("餐馆原料采购成本分析报表")
//                        .setFont(font)
//                        .setFontSize(18)
//                        .setBold()
//                        .setTextAlignment(TextAlignment.CENTER));
//
//                document.add(new Paragraph("\n"));
//
//                // ✅ 生成饼状图（此处可以换成真实的图像）
//                 ImageData pieChartImage = createPieChart();
//                 document.add(new Image(pieChartImage).setAutoScale(true));
//
//                document.add(new Paragraph("\n📋 商品成本统计表").setFont(font).setBold());
//
//                // ✅ 生成表格
//                Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 2, 2, 2}))
//                        .useAllAvailableWidth();
//                table.addHeaderCell(createHeaderCell("大类", font));
//                table.addHeaderCell(createHeaderCell("商品", font));
//                table.addHeaderCell(createHeaderCell("制作成本", font));
//                table.addHeaderCell(createHeaderCell("损耗成本", font));
//                table.addHeaderCell(createHeaderCell("废弃成本", font));
//                table.addHeaderCell(createHeaderCell("平均采购单价", font));
//                table.addHeaderCell(createHeaderCell("采购总金额", font));
//
//                // ✅ 示例数据
//                table.addCell(createCell("肉类", font));
//                table.addCell(createCell("牛肉", font));
//                table.addCell(createCell("¥20,000", font));
//                table.addCell(createCell("¥2,000", font));
//                table.addCell(createCell("¥1,500", font));
//                table.addCell(createCell("¥50/kg", font));
//                table.addCell(createCell("¥23,500", font));
//
//                document.add(table);
//                document.add(new Paragraph("\n"));
//
//                // ✅ 经营优化建议
//                document.add(new Paragraph("📢 经营优化建议").setFont(font).setBold());
//                document.add(new Paragraph("✅ 优化采购计划，减少损耗和废弃成本。").setFont(font));
//                document.add(new Paragraph("✅ 供应商价格对比，争取批量折扣。").setFont(font));
//                document.add(new Paragraph("✅ 加强库存管理，减少积压。").setFont(font));
//
//                System.out.println("标题已添加");
//                System.out.println("表格已添加");
//                document.close();
//
//                // ✅ 确保输出流写入完毕
//                response.getOutputStream().flush();
//                response.getOutputStream().close();
//            }
//
//            System.out.println("PDF 生成成功！");
//        } catch (IOException e) {
//            System.err.println("生成 PDF 时发生错误：");
//            e.printStackTrace();
//        }
//    }

    public static ImageData createPieChart() {
        try {
            // ✅ 1. 创建饼状图数据
            DefaultPieDataset dataset = new DefaultPieDataset();
            dataset.setValue("肉类", 30);
            dataset.setValue("蔬菜", 25);
            dataset.setValue("调料", 15);
            dataset.setValue("海鲜", 20);
            dataset.setValue("其他", 10);

            // ✅ 2. 创建 JFreeChart 饼状图
            JFreeChart chart = ChartFactory.createPieChart("采购成本占比", dataset, true, true, false);
            PiePlot plot = (PiePlot) chart.getPlot();

            // ✅ 3. 设置图表样式
            plot.setSectionPaint("肉类", new Color(255, 99, 132));
            plot.setSectionPaint("蔬菜", new Color(54, 162, 235));
            plot.setSectionPaint("调料", new Color(255, 206, 86));
            plot.setSectionPaint("海鲜", new Color(75, 192, 192));
            plot.setSectionPaint("其他", new Color(153, 102, 255));

            plot.setBackgroundPaint(Color.WHITE);
            plot.setLabelFont(new Font("宋体", Font.BOLD, 14)); // 让文字更清晰
            chart.getTitle().setFont(new Font("宋体", Font.BOLD, 16));

            // ✅ 4. 转换为高分辨率 BufferedImage
            int width = 600;  // 增加分辨率
            int height = 400;
            BufferedImage bufferedImage = chart.createBufferedImage(width, height);

            // ✅ 5. 转换为 iText 兼容的 ImageData
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return ImageDataFactory.create(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 创建表头单元格
     */
    private Cell createHeaderCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setBold());
    }

    /**
     * 创建普通单元格
     */
    private Cell createCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font));
    }
}
