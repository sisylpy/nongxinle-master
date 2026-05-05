package com.nongxinle.controller;

import com.nongxinle.entity.*;
import com.nongxinle.service.NxDepartmentBillService;
import com.nongxinle.service.NxDepartmentOrderHistoryService;
import com.nongxinle.service.NxDepartmentOrdersService;


//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.*;
import com.nongxinle.service.NxDepartmentService;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RequestParam;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("api/download")
public class DownloadController {

    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;

    @Autowired
    private NxDepartmentBillService nxDepartmentBillService;

    @Autowired
    private NxDepartmentOrderHistoryService nxDepartmentOrderHistoryService;
    @Autowired
    private NxDepartmentService nxDepartmentService;


    /**
     * 下载账单Excel（供小程序 downloadBillExcel 调用）
     * 接口路径：api/download/downloadBillExcelNx?billId=xxx
     *
     * @param billId 账单ID (nxDepartmentBillId)
     */
    @RequestMapping("/downloadBillExcelNx")
    public void downloadBillExcelNx(HttpServletResponse response, @RequestParam("billId") Integer billId) throws IOException {
        log.info("[downloadBillExcelNx] 请求 billId={}", billId);
        if (billId == null) {
            log.warn("[downloadBillExcelNx] billId 为空，返回 400");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        NxDepartmentBillEntity bill = nxDepartmentBillService.queryObject(billId);
        if (bill == null) {
            log.warn("[downloadBillExcelNx] 账单不存在 billId={}", billId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 1. 构建文件名：部门名称_订单日期（与 Excel 标题一致，优先用 nxDepartmentAttrName）
        String depName = "未知部门";
        Integer depId = bill.getNxDbDepFatherId() != null ? bill.getNxDbDepFatherId() : bill.getNxDbDepId();
        if (depId != null) {
            NxDepartmentEntity dep = nxDepartmentService.queryObject(depId);
            if (dep != null) {
                depName = dep.getNxDepartmentAttrName() != null ? dep.getNxDepartmentAttrName()
                        : (dep.getNxDepartmentName() != null ? dep.getNxDepartmentName() : "未知部门");
            }
        }
        // 订单日期：nxDbDate 应为日期格式(如 2026-03-17)，若被误存为订单号则用 year-month-day 拼接
        String orderDate = resolveOrderDate(bill);
        String baseFileName = depName + "_" + orderDate + ".xlsx";
        log.info("[downloadBillExcelNx] 文件名 depName={} orderDate={} baseFileName={}", depName, orderDate, baseFileName);
        // RFC 5987: filename 用 ASCII 兜底，filename* 用 UTF-8 编码；并暴露给跨域前端
        String encodedFileName = URLEncoder.encode(baseFileName, "UTF-8").replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        // 2. 查询订单：先查当前订单表，为空则查历史订单表
        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        List<NxDepartmentOrderHistoryEntity> historyList = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(map);
        int orderCount = (historyList != null) ? historyList.size() : 0;
        log.info("[downloadBillExcelNx] 查询到订单数={} billId={} tradeNo={}", orderCount, billId, bill.getNxDbTradeNo());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("账单明细");
            int rowNum = 0;

            // 3. 账单概要
            XSSFRow titleRow = sheet.createRow(rowNum++);
            Integer nxDbDepFatherId = bill.getNxDbDepFatherId();
            NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(nxDbDepFatherId);
//            titleRow.createCell(0).setCellValue("账单明细");
            titleRow.createCell(0).setCellValue(nxDepartmentEntity.getNxDepartmentAttrName());
            titleRow.getCell(0).setCellStyle(createTitleStyle(workbook));



            rowNum++;
            XSSFRow infoRow1 = sheet.createRow(rowNum++);
            infoRow1.createCell(0).setCellValue("订单号");
            infoRow1.createCell(1).setCellValue(bill.getNxDbTradeNo() != null ? bill.getNxDbTradeNo() : "");
            XSSFRow infoRow2 = sheet.createRow(rowNum++);
            infoRow2.createCell(0).setCellValue("账单日期");
            infoRow2.createCell(1).setCellValue(bill.getNxDbDate() != null ? bill.getNxDbDate() : "");
            XSSFRow infoRow3 = sheet.createRow(rowNum++);
            infoRow3.createCell(0).setCellValue("账单金额");
            infoRow3.createCell(1).setCellValue(bill.getNxDbTotal() != null ? bill.getNxDbTotal() : "0");

            rowNum++;

            // 4. 表头
            XSSFRow headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("序号");
            headerRow.createCell(1).setCellValue("商品名称");
            headerRow.createCell(2).setCellValue("规格");
            headerRow.createCell(3).setCellValue("数量");
            headerRow.createCell(4).setCellValue("单位");
            headerRow.createCell(5).setCellValue("单价");
            headerRow.createCell(6).setCellValue("小计");

            // 5. 填充订单数据（订单内容字体 12pt）
            XSSFCellStyle dataCellStyle = createDataRowStyle(workbook);
            if (historyList != null && !historyList.isEmpty()) {
                int idx = 1;
                for (NxDepartmentOrderHistoryEntity order : historyList) {
                    XSSFRow dataRow = sheet.createRow(rowNum++);
                    fillOrderRow(dataRow, getGoodsName(order), getStandard(order), order.getNxDoQuantity(), order.getNxDoPrintStandard(), order.getNxDoPrice(),
                            order.getNxDoSubtotal(), idx++, dataCellStyle);
                }
            } else if (!historyList.isEmpty()) {
                int idx = 1;
                for (NxDepartmentOrderHistoryEntity order : historyList) {
                    XSSFRow dataRow = sheet.createRow(rowNum++);
                    fillOrderRow(dataRow,  getGoodsName(order), getStandard(order), order.getNxDoQuantity(),order.getNxDoPrintStandard(), order.getNxDoPrice(),
                            order.getNxDoSubtotal(),  idx++, dataCellStyle);
                }
            }

            // 6. 自动调整列宽
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
            log.info("[downloadBillExcelNx] 导出完成 billId={} baseFileName={} orderCount={}", billId, baseFileName, orderCount);
        } catch (Exception e) {
            log.error("[downloadBillExcelNx] 导出异常 billId={}", billId, e);
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException("导出账单Excel失败", e);
        }
    }

    /**
     * 解析订单日期：nxDbDate 应为日期格式(如 2026-03-17)，若被误存为订单号则用 year-month 拼接
     */
    private String resolveOrderDate(NxDepartmentBillEntity bill) {
        String nxDbDate = bill.getNxDbDate();
        if (nxDbDate != null && !nxDbDate.isEmpty() && nxDbDate.matches("^20\\d{2}[-/]?\\d{1,2}[-/]?\\d{1,2}.*")) {
            return nxDbDate;
        }
        String year = bill.getNxDbYear();
        String month = bill.getNxDbMonth();
        if (year != null && month != null) {
            return year + "-" + (month.length() == 1 ? "0" + month : month);
        }
        return "";
    }

    private void fillOrderRow(XSSFRow row, String goodsName, String standard,
                              String quantity, String printStandard, String price, String subtotal, int idx,
                              XSSFCellStyle cellStyle) {
        for (int i = 0; i < 7; i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
            switch (i) {
                case 0: cell.setCellValue(idx); break;
                case 1: cell.setCellValue(goodsName != null ? goodsName : ""); break;
                case 2: cell.setCellValue(standard != null ? standard : ""); break;
                case 3: cell.setCellValue(quantity != null ? quantity : ""); break;
                case 4: cell.setCellValue(printStandard != null ? printStandard : ""); break;
                case 5: cell.setCellValue(price != null ? price : ""); break;
                case 6: cell.setCellValue(subtotal != null ? subtotal : ""); break;
            }
        }
    }

    private XSSFCellStyle createDataRowStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }



    private String getGoodsName(NxDepartmentOrderHistoryEntity order) {
        if (order.getNxDistributerGoodsEntity() != null && order.getNxDistributerGoodsEntity().getNxDgGoodsName() != null) {
            return order.getNxDistributerGoodsEntity().getNxDgGoodsName();
        }
        return order.getNxDoGoodsName() != null ? order.getNxDoGoodsName() : "";
    }



    private String getStandard(NxDepartmentOrderHistoryEntity order) {
        if (order.getNxDistributerGoodsEntity() != null && order.getNxDistributerGoodsEntity().getNxDgItemsPerCarton() != null) {
            if(order.getNxDistributerGoodsEntity() != null && order.getNxDistributerGoodsEntity().getNxDgGoodsStandardWeight() != null){
                String nxDgItemsPerCarton = order.getNxDistributerGoodsEntity().getNxDgItemsPerCarton();
                String nxDgGoodsStandardname = order.getNxDistributerGoodsEntity().getNxDgGoodsStandardname();
                String nxDgGoodsStandardWeight = order.getNxDistributerGoodsEntity().getNxDgGoodsStandardWeight();
                String nxDgCartonUnit = order.getNxDistributerGoodsEntity().getNxDgCartonUnit();
                return   nxDgGoodsStandardWeight + "/" + nxDgGoodsStandardname + "*"  + nxDgItemsPerCarton +nxDgGoodsStandardname + "/" +nxDgCartonUnit ;
            }
        }else  {
            if(order.getNxDistributerGoodsEntity() != null && order.getNxDistributerGoodsEntity().getNxDgGoodsStandardname() != null){
                return order.getNxDistributerGoodsEntity().getNxDgGoodsStandardname();
            }

        }
        return  order.getNxDoPrintStandard();
    }


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
        font.setBold(true); // 加粗
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
