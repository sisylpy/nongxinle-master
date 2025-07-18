package com.nongxinle.controller;

import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.service.NxDepartmentOrdersService;


//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("api/download")
public class DownloadController {

    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;


    @RequestMapping("/downloadReportExcelNx")
    public void downloadReportExcelNx(HttpServletResponse response, Integer depFatherId, String startDate, String stopDate
    ) throws IOException {
        // 1. 设置响应头
        String fileName = URLEncoder.encode("采购成本分析.xlsx", "UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // 2. 创建Excel工作簿
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("采购分析");
            int rowNum = 0;

            // 3. 添加标题
            XSSFRow titleRow = sheet.createRow(rowNum++);
            titleRow.createCell(0).setCellValue("餐馆原料采购成本分析报表");
            titleRow.getCell(0).setCellStyle(createTitleStyle(workbook));

            // 4. 查询数据
            Map<String, Object> params = new HashMap<>();
            params.put("depFatherId", depFatherId);
            params.put("startDate", startDate);
            params.put("stopDate", stopDate);

            List<NxDistributerFatherGoodsEntity> dataList = nxDepartmentOrdersService.queryGrandGoodsOrder(params);

            // 5. 填充数据
            for (NxDistributerFatherGoodsEntity category : dataList) {
                // 添加分类标题
                XSSFRow categoryRow = sheet.createRow(rowNum++);
//                categoryRow.createCell(0).setCellValue(category.getNxDfgFatherGoodsName());
//                XSSFCell categoryCell = categoryRow.createCell(0);
//                categoryCell.setCellValue(category.getNxDfgFatherGoodsName());

                XSSFCell categoryCell = categoryRow.createCell(0);
                categoryCell.setCellValue(category.getNxDfgFatherGoodsName());

// 应用样式
                XSSFCellStyle categoryStyle = createCategoryStyle(workbook);
                categoryCell.setCellStyle(categoryStyle);

// 合并单元格（如果需要跨列居中）
                sheet.addMergedRegion(new CellRangeAddress(
                        rowNum-1, // 起始行（0-based）
                        rowNum-1, // 结束行
                        0,        // 起始列
                        4        // 结束列（假设合并前4列）
                ));



                // 添加表头
                XSSFRow headerRow = sheet.createRow(rowNum++);
                headerRow.createCell(0).setCellValue("商品名称");
                headerRow.createCell(1).setCellValue("单位");
                headerRow.createCell(2).setCellValue("均价");
                headerRow.createCell(3).setCellValue("数量");
                headerRow.createCell(4).setCellValue("金额");

                // 添加商品数据
                for (NxDistributerGoodsEntity goods : category.getNxDistributerGoodsEntities()) {
                    params.put("disGoodsId", goods.getNxDistributerGoodsId());
                    double quantity = nxDepartmentOrdersService.queryDisGoodsOrderWeightTotal(params);
                    double amount = nxDepartmentOrdersService.queryDepOrdersSubtotal(params);
                    double price =  amount / quantity;

                    XSSFRow dataRow = sheet.createRow(rowNum++);
                    dataRow.createCell(0).setCellValue(goods.getNxDgGoodsName());
                    dataRow.createCell(1).setCellValue(goods.getNxDgGoodsStandardname());
                    dataRow.createCell(2).setCellValue(String.format("%.1f", price) +"元/" + goods.getNxDgGoodsStandardname());
                    dataRow.createCell(3).setCellValue(quantity);
                    dataRow.createCell(4).setCellValue(amount);
                }
            }

            // 6. 自动调整列宽
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // 7. 写入响应流
            workbook.write(response.getOutputStream());
        }
    }

    private XSSFCellStyle createCategoryStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();

        // 创建字体
        XSSFFont font = workbook.createFont();
        font.setBold(true);                  // 加粗
        font.setFontHeightInPoints((short)14); // 14号字体
        font.setColor(IndexedColors.DARK_BLUE.getIndex()); // 深蓝色字体

        // 设置样式
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER); // 水平居中
        style.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中

        // 设置背景色（可选）
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return style;
    }
    private XSSFCellStyle createTitleStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short)16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }




    @RequestMapping("/downloadReportExcelNx1")
    public void downloadReportExcelNx1(HttpServletResponse response, Integer depFatherId, String startDate, String stopDate) {
        System.out.println("开始生成 Excel 报表...");

        // 1. **创建 Excel 文件**
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("餐馆原料采购成本分析");

        int rowNum = 0;

        // 2. **设置标题**
        XSSFRow titleRow = sheet.createRow(rowNum++);
        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("餐馆原料采购成本分析报表"); // 设置标题
        titleCell.setCellStyle(getTitleCellStyle1(workbook)); // 设置标题样式
        System.out.println("shezhititall");

        // 3. **空一行**
        rowNum++;

//        // 4. **生成饼状图（可选）**
//        ByteArrayOutputStream pieChartStream = new ByteArrayOutputStream();
//        BufferedImage pieChartImage = createDepPieChartBuffered(depFatherId, startDate, stopDate);
//        try {
//            ImageIO.write(pieChartImage, "png", pieChartStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        insertImageToExcel(sheet, workbook, pieChartStream.toByteArray()); // 插入图片

        // 5. **生成数据表**
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("depFatherId", depFatherId);
        List<NxDistributerFatherGoodsEntity> greatGrand = nxDepartmentOrdersService.queryGrandGoodsOrder(map);

        double total = 0.0;
        if (!greatGrand.isEmpty()) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }

        for (NxDistributerFatherGoodsEntity grand : greatGrand) {
            // **写入大类标题**
            XSSFRow categoryRow = sheet.createRow(rowNum++);
            XSSFCell categoryCell = categoryRow.createCell(0); // 创建单元格
            categoryCell.setCellValue(grand.getNxDfgFatherGoodsName()); // 设置大类名称

            // **写入表头**
            XSSFRow headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("商品");
            headerRow.createCell(1).setCellValue("数量");
            headerRow.createCell(2).setCellValue("总额");
            headerRow.createCell(3).setCellValue("均价");

            System.out.println("granddddds" + grand.getNxDistributerGoodsEntities().size());
            List<NxDistributerGoodsEntity> goodsList = grand.getNxDistributerGoodsEntities();
            if (!goodsList.isEmpty()) {
                for (NxDistributerGoodsEntity goods : goodsList) {
                    map.put("goodsId", goods.getNxDistributerGoodsId()); // 添加商品ID过滤
                    System.out.println("goodsso" + map);
                    double weight = nxDepartmentOrdersService.queryDisGoodsOrderWeightTotal(map);
                    double subtotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                    System.out.println("Total weight: " + weight + ", Subtotal: " + subtotal);

                    XSSFRow row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(goods.getNxDgGoodsName());
                    row.createCell(1).setCellValue(weight);
                    row.createCell(2).setCellValue(subtotal);
                    row.createCell(3).setCellValue(weight > 0 ? subtotal / weight : 0); // 避免除零
//
                }
            }
        }

        // 6. **写入 Excel 并响应下载**
        try {
            String fileName = new String("导出商品.xls".getBytes("utf-8"), "iso8859-1");
            response.setHeader("content-Disposition", "attachment; filename =" + fileName);
            workbook.write(response.getOutputStream());

            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private XSSFCellStyle getTitleCellStyle1(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont(); // ✅ 明确指定 XSSFFont 类型
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font); // ✅ 传入 XSSFFont
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void insertImageToExcel(XSSFSheet sheet, XSSFWorkbook workbook, byte[] imageBytes) {
        int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
        CreationHelper helper = workbook.getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(0);
        anchor.setRow1(2);
        Picture pict = drawing.createPicture(anchor, pictureIdx);
        pict.resize(3, 5);
    }
    private BufferedImage createDepPieChartBuffered(Integer depFatherId, String startDate, String stopDate) {
        // 1. 查询数据
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("depFatherId", depFatherId);
        List<NxDistributerFatherGoodsEntity> greatGrand = nxDepartmentOrdersService.queryGrandGoodsOrder(map);

        double total = 0.0;
        if (!greatGrand.isEmpty()) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }

        // 2. 构建饼状图数据
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (NxDistributerFatherGoodsEntity grandGoods : greatGrand) {
            Integer grandId = grandGoods.getNxDistributerFatherGoodsId();
            map.put("grandId", grandId);
            double subtotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            if (subtotal > 0 && total > 0) {
                dataset.setValue(grandGoods.getNxDfgFatherGoodsName(), subtotal);
            }
        }

        // 3. 生成 JFreeChart 饼状图
        JFreeChart chart = ChartFactory.createPieChart(null, dataset, false, false, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setCircular(true);
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new java.awt.Font("Hiragino Sans GB", Font.PLAIN, 14));

        // 4. 渲染成 BufferedImage
        int width = 500, height = 500;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        chart.draw(g2, new Rectangle(width, height));
        g2.dispose();
        return image;
    }


    @RequestMapping(value = "/wx/downLoadImg/{value}")
    public ResponseEntity downLoadImg (@PathVariable String value, HttpSession session) throws Exception {

        System.out.println("nihao");

        //1,获取文件路径
        ServletContext servletContext = session.getServletContext();

        String realPath = servletContext.getRealPath("uploadImage/r.jpg");


        //2,把文件读取程序当中
        InputStream io = new FileInputStream(realPath);
        byte[] body = new byte[io.available()];
        io.read(body);
      
        //3,创建相应头
        HttpHeaders httpHeaders = new HttpHeaders();
        System.out.println(httpHeaders);

        httpHeaders.add("Content-Disposition","attachment; filename=" +  value +".jpg");
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);

        return responseEntity;
    }



}
