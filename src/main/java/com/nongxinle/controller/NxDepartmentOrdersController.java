package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-21 21:51
 */

import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.UploadFile;
import org.apache.commons.io.IOUtils;


import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.usermodel.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxOrderStatusHasFinished;
import static com.nongxinle.utils.ParseObject.myRandom;
import static com.nongxinle.utils.PinYin4jUtils.*;


@RestController
@RequestMapping("api/nxdepartmentorders")
public class NxDepartmentOrdersController {
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDepartmentOrdersHistoryService nxDepartmentOrdersHistoryService;
    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDistributerPurchaseBatchService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDPBService;
    @Autowired
    private NxDistributerWeightService nxDistributerWeightService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxDepartmentBillService nxDepartmentBillService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxJrdhSupplierService jrdhSupplierService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;
    @Autowired
    private NxDistributerStandardService nxDistributerStandardService;
    @Autowired
    private NxDistributerGoodsShelfStockService nxDisGoodsShelfStockService;
    @Autowired
    private NxDistributerGoodsShelfStockReduceService nxDisGoodsShelfStockReduceService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;
    @Autowired
    private NxDepartmentUserService nxDepartmentUserService;
    @Autowired
    private NxDepartmentOrderHistoryService historyService;
    @Autowired
    private NxDistributerAliasService nxDistributerAliasService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDistributerPurchaseBatchService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxAliasService nxAliasService;
    @Autowired
    private NxDepartmentOrdersHistoryService ordersHistoryService;
    @Autowired
    private NxDistributerGoodsShelfGoodsService shelfGoodsService;
    @Autowired
    private NxDistributerGoodsShelfService nxDistributerGoodsShelfService;



    @RequestMapping(value = "/disGetLinshiOrders/{id}")
    @ResponseBody
    public R disGetLinshiOrders(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", id);
        map.put("status", -1);
        List<NxDepartmentEntity> nxDepartmentEntities = nxDepartmentOrdersService.queryDistributerTodayDepartments(map);
        return R.ok().put("data", nxDepartmentEntities);
    }

   @RequestMapping(value = "/confirmDepApplyGoods/{id}")
   @ResponseBody
   public R confirmDepApplyGoods(@PathVariable Integer id) {
       NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(id);

       nxDepartmentOrdersEntity.setNxDoStatus(0);
       nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);
       return R.ok();
   }

    @RequestMapping(value = "/editDepApplyGoods", method = RequestMethod.POST)
    @ResponseBody
    public R editDepApplyGoods (Integer orderId, Integer goodsId) {

        System.out.println("orderrid" +orderId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(orderId);
        Integer nxDoDisGoodsId = nxDepartmentOrdersEntity.getNxDoDisGoodsId();

        NxDistributerGoodsEntity lishiGoods = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);

        nxDistributerGoodsService.delete(lishiGoods.getNxDistributerGoodsId());

        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(goodsId);
        nxDepartmentOrdersEntity.setNxDoDisGoodsId(distributerGoodsEntity.getNxDistributerGoodsId());
        nxDepartmentOrdersEntity.setNxDoDisGoodsFatherId(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
        nxDepartmentOrdersEntity.setNxDoDisGoodsGrandId(distributerGoodsEntity.getNxDgDfgGoodsGrandId());
        nxDepartmentOrdersEntity.setNxDoNxGoodsId(distributerGoodsEntity.getNxDgNxGoodsId());
        nxDepartmentOrdersEntity.setNxDoNxGoodsFatherId(distributerGoodsEntity.getNxDgNxFatherId());
        nxDepartmentOrdersEntity.setNxDoStatus(0);
        nxDepartmentOrdersEntity.setNxDoGoodsType(distributerGoodsEntity.getNxDgPurchaseAuto());
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", goodsId);
        map.put("depId", nxDepartmentOrdersEntity.getNxDoDepartmentId());
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if(departmentDisGoodsEntity != null){
            String nxDdgOrderPrice = departmentDisGoodsEntity.getNxDdgOrderPrice();

            nxDepartmentOrdersEntity.setNxDoPrice(nxDdgOrderPrice);
            nxDepartmentOrdersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
            nxDepartmentOrdersEntity.setNxDoDepDisGoodsPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());

            if (distributerGoodsEntity.getNxDgWillPriceTwo() != null && nxDepartmentOrdersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgWillPriceTwoStandard())) {
                System.out.println("levlellelelelelel111222222222222222");
                BigDecimal doQuantity = new BigDecimal(nxDepartmentOrdersEntity.getNxDoQuantity());
                BigDecimal cosPrice = new BigDecimal(distributerGoodsEntity.getNxDgBuyingPriceTwo());
                BigDecimal willPrice = new BigDecimal(nxDdgOrderPrice);
                BigDecimal costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

                //
                nxDepartmentOrdersEntity.setNxDoWeight(nxDepartmentOrdersEntity.getNxDoQuantity());
                nxDepartmentOrdersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgWillPriceTwoStandard());
                nxDepartmentOrdersEntity.setNxDoExpectPrice(distributerGoodsEntity.getNxDgWillPriceTwo());
                nxDepartmentOrdersEntity.setNxDoCostPrice(distributerGoodsEntity.getNxDgBuyingPriceTwo());
                nxDepartmentOrdersEntity.setNxDoSubtotal(subtotal.toString());
                nxDepartmentOrdersEntity.setNxDoCostSubtotal(costSubtotal.toString());
                nxDepartmentOrdersEntity.setNxDoCostPriceLevel("2");
                nxDepartmentOrdersEntity.setNxDoCostPriceUpdate(distributerGoodsEntity.getNxDgBuyingPriceTwoUpdate());
                nxDepartmentOrdersEntity.setNxDoCostPrice(distributerGoodsEntity.getNxDgBuyingPriceTwo());
                nxDepartmentOrdersEntity.setNxDoProfitSubtotal(profit.toString());
                nxDepartmentOrdersEntity.setNxDoProfitScale(scaleB.toString());

            } else {
                nxDepartmentOrdersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgGoodsStandardname());
                nxDepartmentOrdersEntity.setNxDoExpectPrice(distributerGoodsEntity.getNxDgWillPriceOne());
                nxDepartmentOrdersEntity.setNxDoCostPriceLevel("1");
                nxDepartmentOrdersEntity.setNxDoCostPriceUpdate(distributerGoodsEntity.getNxDgBuyingPriceOneUpdate());
                nxDepartmentOrdersEntity.setNxDoCostPrice(distributerGoodsEntity.getNxDgBuyingPriceOne());
                nxDepartmentOrdersEntity.setNxDoWeight(nxDepartmentOrdersEntity.getNxDoQuantity());
                nxDepartmentOrdersEntity.setNxDoSubtotal("0");
                nxDepartmentOrdersEntity.setNxDoCostSubtotal("0");
                System.out.println("orderstandndnd" + nxDepartmentOrdersEntity.getNxDoStandard() + "goodsstandn" + distributerGoodsEntity.getNxDgGoodsStandardname());
                if (nxDepartmentOrdersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())
                        && !distributerGoodsEntity.getNxDgBuyingPriceOne().equals("0.1") && !distributerGoodsEntity.getNxDgBuyingPriceOne().trim().isEmpty()) {

                    BigDecimal doQuantity = new BigDecimal(nxDepartmentOrdersEntity.getNxDoQuantity());
                    BigDecimal cosPrice = new BigDecimal(distributerGoodsEntity.getNxDgBuyingPriceOne());
                    BigDecimal willPrice = new BigDecimal(nxDdgOrderPrice);
                    BigDecimal costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                    nxDepartmentOrdersEntity.setNxDoSubtotal(subtotal.toString());
                    nxDepartmentOrdersEntity.setNxDoCostSubtotal(costSubtotal.toString());
                    nxDepartmentOrdersEntity.setNxDoProfitSubtotal(profit.toString());
                    nxDepartmentOrdersEntity.setNxDoProfitScale(scaleB.toString());

                }

            }
        }

        nxDepartmentOrdersEntity.setNxDoPurchaseGoodsId(-1);
        nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);

        Integer nxDoPurchaseGoodsId = nxDepartmentOrdersEntity.getNxDoPurchaseGoodsId();
        nxDistributerPurchaseGoodsService.delete(nxDoPurchaseGoodsId);


        if(distributerGoodsEntity.getNxDgPurchaseAuto() != -1){
            savePurGoodsAuto(nxDepartmentOrdersEntity);
        }


        return R.ok();
    }


    @RequestMapping(value = "/cancleDeliveryOrder/{id}")
    @ResponseBody
    public R cancleDeliveryOrder(@PathVariable Integer id) {
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(id);
        nxDepartmentOrdersEntity.setNxDoPurchaseStatus(4);
        nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);
        return R.ok();
    }


    @RequestMapping(value = "/deliveryOrder/{id}")
    @ResponseBody
    public R deliveryOrder(@PathVariable Integer id) {
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(id);

        nxDepartmentOrdersEntity.setNxDoPurchaseStatus(5);
        nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);
        return R.ok();
    }

    @RequestMapping(value = "/getNxDisDepPdf")
    @ResponseBody
    public R getNxDisDepPdf(Integer depFatherId, String startDate, String stopDate) {


        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("depFatherId", depFatherId);
        List<NxDistributerFatherGoodsEntity> greatGrand = historyService.queryGrandGoodsOrder(map);
        double total = 0.0;
        if (greatGrand.size() > 0) {
//            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            total = historyService.queryDepOrdersSubtotal(map);

            for (NxDistributerFatherGoodsEntity grandGoods : greatGrand) {
                Integer grandId = grandGoods.getNxDistributerFatherGoodsId();
                map.put("grandId", grandId);
                System.out.println("fammamamamamamma" + map);
//                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                Integer integer = historyService.queryDepOrdersAcount(map);
                Double aDouble = 0.0;
                BigDecimal v = new BigDecimal("0.0");
                if (integer > 0) {
                    System.out.println("fammamamamamamma" + map);
                    aDouble = historyService.queryDepOrdersSubtotal(map);
                    double pie = aDouble / total;
                    v = new BigDecimal(pie).setScale(2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

                }

                Random random = new Random();
                // 生成随机的红色、绿色和蓝色分量
                // 限制 RGB 分量的范围（0 到 128），确保颜色较深
                int red = random.nextInt(128);    // 0 到 127
                int green = random.nextInt(128);  // 0 到 127
                int blue = random.nextInt(128);   // 0 到 127

                // 将RGB值转换为十六进制格式
                String hexRed = Integer.toHexString(red);
                String hexGreen = Integer.toHexString(green);
                String hexBlue = Integer.toHexString(blue);

                // 确保每个分量都是两位的十六进制数
                hexRed = hexRed.length() == 1 ? "0" + hexRed : hexRed;
                hexGreen = hexGreen.length() == 1 ? "0" + hexGreen : hexGreen;
                hexBlue = hexBlue.length() == 1 ? "0" + hexBlue : hexBlue;

                String color = "#" + hexRed + hexGreen + hexBlue;
                grandGoods.setNxDfgFatherGoodsColor(color);
                grandGoods.setFatherProfitScaleString(v.toString());
                grandGoods.setFatherSubtotalTotalString(String.format("%.1f", aDouble));

                List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = grandGoods.getNxDistributerGoodsEntities();

                if (nxDistributerGoodsEntities.size() > 0) {
                    for (NxDistributerGoodsEntity goodsEntity : nxDistributerGoodsEntities) {
                        Integer goodsId = goodsEntity.getNxDistributerGoodsId();
                        Map<String, Object> mapFather = new HashMap<>();
                        mapFather.put("startDate", startDate);
                        mapFather.put("stopDate", stopDate);
                        mapFather.put("depFatherId", depFatherId);
                        mapFather.put("disGoodsId", goodsId);
                        System.out.println("weighththmap" + mapFather);
                        double vG = historyService.queryDisGoodsOrderWeightTotal(mapFather);
//                        double vG = nxDepartmentOrdersService.queryDisGoodsOrderWeightTotal(mapFather);
//                        double vC = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapFather);
                        double vC = historyService.queryDepOrdersSubtotal(mapFather);
                        goodsEntity.setNxDgOutTotalWeight(new BigDecimal(vG).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        goodsEntity.setSellSubtotal(new BigDecimal(vC).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        double v1 = vC / vG;
                        goodsEntity.setPerPrice(new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }

                }

            }
        }

        Map<String, Object> mapres = new HashMap<>();
        mapres.put("arr", greatGrand);
        mapres.put("total", String.format("%.1f", total));
        return R.ok().put("data", mapres);
    }


    @RequestMapping("/downloadReportPdfNx")
    public void downloadReportPdfNx(HttpServletResponse response, Integer depFatherId, String startDate, String stopDate) {

        System.out.println("starr" + startDate + "stopddat" + stopDate + "doepdi" + depFatherId);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=restaurant_report.pdf");

        try {
            System.out.println("开始加载字体...");
            String fontPath = "fonts/Hiragino Sans GB.ttc";

            InputStream fontStream = getClass().getClassLoader().getResourceAsStream(fontPath);
            if (fontStream == null) {
                System.err.println("字体文件未找到，路径：" + fontPath);
                throw new IOException("字体文件未找到");
            }
            byte[] fontData = IOUtils.toByteArray(fontStream);
            System.out.println("字体文件已加载，大小：" + fontData.length + " 字节");

            // ✅ 加载字体
            PdfFont font = PdfFontFactory.createTtcFont(fontData, 0, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED, true);
            System.out.println("字体创建成功：" + font.getFontProgram().getFontNames().getFontName());

            // ✅ 创建 PDF 文档
            try (OutputStream out = response.getOutputStream();
                 PdfWriter writer = new PdfWriter(out);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf, PageSize.A4)) {

                // ✅ 添加标题
                document.add(new Paragraph("餐馆原料采购成本分析报表")
                        .setFont(font)
                        .setFontSize(18)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER));

                document.add(new Paragraph("\n"));

                // 1， 生成饼状图（此处可以换成真实的图像）
                ImageData pieChartImage = createDepPieChart(depFatherId, startDate, stopDate);
                document.add(new Image(pieChartImage).setAutoScale(true));

                // 2. 生成每个类别的详细列表
                Map<String, Object> map = new HashMap<>();
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                map.put("depFatherId", depFatherId);
                List<NxDistributerFatherGoodsEntity> greatGrand = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
                double total = 0.0;
                if (greatGrand.size() > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                }
                for (NxDistributerFatherGoodsEntity grand : greatGrand) {
                    // 📌 1. 显示大类名称（模拟小程序 `wx:for` 大类标题）
                    document.add(new Paragraph(grand.getNxDfgFatherGoodsName())
                            .setFont(font)
                            .setFontSize(16)
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER));

                    document.add(new Paragraph("\n"));

                    // 📌 2. 添加表头（模拟 `wx:for` 商品信息表头）
                    float[] columnWidths = {3, 2, 2, 2};
                    Table tableHead = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
                    tableHead.addCell(new Paragraph("商品").setFont(font));
                    tableHead.addCell(new Paragraph("数量").setFont(font));
                    tableHead.addCell(new Paragraph("总额").setFont(font));
                    tableHead.addCell(new Paragraph("均价").setFont(font));
                    document.add(tableHead);

                    List<NxDistributerGoodsEntity> goodsList = grand.getNxDistributerGoodsEntities();

                    // 📌 3. 遍历商品列表（模拟 `wx:for` 渲染 `goods`）
                    if (!goodsList.isEmpty()) {
                        for (NxDistributerGoodsEntity goods : goodsList) {
                            Table tableRow = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
                            Integer goodsId = goods.getNxDistributerGoodsId();
                            Map<String, Object> mapFather = new HashMap<>();
                            mapFather.put("startDate", startDate);
                            mapFather.put("stopDate", stopDate);
                            mapFather.put("depFatherId", depFatherId);
                            mapFather.put("disGoodsId", goodsId);
                            String goodsName = goods.getNxDgGoodsName();
                            if (goodsName == null || goodsName.isEmpty()) {
                                goodsName = "未知商品";
                            }
                            double vG = nxDepartmentOrdersService.queryDisGoodsOrderWeightTotal(mapFather);
                            double vC = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapFather);
                            double v1 = vC / vG;
                            // 添加商品名称（使用支持中文的字体）
                            tableRow.addCell(new Paragraph(goodsName).setFont(font)); // 商品名
                            tableRow.addCell(new Paragraph(new BigDecimal(vG).setScale(1, BigDecimal.ROUND_HALF_UP).toString() + goods.getNxDgGoodsStandardname()).setFont(font)); // 数量 + 单位
                            tableRow.addCell(new Paragraph(new BigDecimal(vC).setScale(1, BigDecimal.ROUND_HALF_UP).toString() + "元").setFont(font)); // 总额
                            tableRow.addCell(new Paragraph(new BigDecimal(v1).setScale(1, BigDecimal.ROUND_HALF_UP).toString() + "元/" + goods.getNxDgGoodsStandardname()).setFont(font)); // 均价

                            document.add(tableRow);
                        }
                    } else {
                        // ✅ 如果没有商品数据，显示 "暂无数据"
                        Table emptyTable = new Table(UnitValue.createPercentArray(new float[]{1})).useAllAvailableWidth();
                        emptyTable.addCell(new Paragraph("wu")); // 均价
                        document.add(emptyTable);
                    }

                    document.add(new Paragraph("\n"));
                }


                document.close();


                response.getOutputStream().flush();
                response.getOutputStream().close();
            }

            System.out.println("PDF 生成成功！");
        } catch (IOException e) {
            System.err.println("生成 PDF 时发生错误：");
            e.printStackTrace();
        }
    }

    public ImageData createDepPieChart(Integer depFatherId, String startDate, String stopDate) {
        try {
            // ✅ 1. 查询数据
            Map<String, Object> map = new HashMap<>();
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
            map.put("depFatherId", depFatherId);
            List<NxDistributerFatherGoodsEntity> greatGrand = nxDepartmentOrdersService.queryGrandGoodsOrder(map);

            // ✅ 2. 计算总成本
            double total = 0.0;
            if (greatGrand.size() > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }

            // ✅ 3. 构建饼状图数据
            DefaultPieDataset dataset = new DefaultPieDataset();
            for (NxDistributerFatherGoodsEntity grandGoods : greatGrand) {
                Integer grandId = grandGoods.getNxDistributerFatherGoodsId();
                map.put("grandId", grandId);
                double subtotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                if (subtotal > 0 && total > 0) {
                    double percentage = subtotal / total * 100;
                    if (grandGoods.getNxDfgFatherGoodsName() != null) {
                        System.out.println("grandGoodsName====" + grandGoods.getNxDfgFatherGoodsName());
                        dataset.setValue(grandGoods.getNxDfgFatherGoodsName(), percentage);
                    } else {
                        System.err.println("警告：尝试插入空键到饼图数据集中！");
                    }
                }
            }

            // ✅ 4. 生成 JFreeChart 饼状图
            JFreeChart chart = ChartFactory.createPieChart(null, dataset, false, false, false);
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setCircular(true);
            plot.setBackgroundPaint(Color.WHITE);  // 背景色
            plot.setOutlineVisible(false);  // 隐藏边框

            // 设置随机颜色
            // ✅ 5. 设置颜色
            Color[] colors = {
                    new Color(255, 99, 132), new Color(54, 162, 235),
                    new Color(255, 206, 86), new Color(75, 192, 192),
                    new Color(153, 102, 255), new Color(255, 159, 64)
            };

            int colorIndex = 0;

            // 创建颜色映射
            Map<Comparable<?>, Color> colorMap = new HashMap<>();
// 遍历数据集并存储颜色
            for (Object keyObj : dataset.getKeys()) {
                if (keyObj instanceof Comparable) {
                    Comparable<?> key = (Comparable<?>) keyObj;
                    colorMap.put(key, colors[colorIndex % colors.length]);
                    colorIndex++;
                } else {
                    System.err.println("警告：数据集中包含非 Comparable 类型：" + keyObj);
                }
            }

// 使用颜色映射设置颜色
            for (Map.Entry<Comparable<?>, Color> entry : colorMap.entrySet()) {
                plot.setSectionPaint(entry.getKey(), entry.getValue());
                // 设置数据标签格式：商品名\n金额 元 占比%
                plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}\n{1} 元 {2}",
                        new DecimalFormat("#,##0.0"), new DecimalFormat("0.0%")));
            }

            // 标签字体设置
            plot.setLabelFont(new Font("Hiragino Sans GB", Font.PLAIN, 14));


            // ✅ 6. 设置图表样式
            // 创建 BufferedImage 画布
            int width = 500, height = 500;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();

            // 设置抗锯齿
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 绘制饼图
            chart.draw(g2, new Rectangle(width, height));

            // 画一个白色的圆，实现环形效果
            int innerSize = (int) (width * 0.3);  // 内圈大小
            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Double((width - innerSize) / 2.0, (height - innerSize) / 2.0, innerSize, innerSize));

            g2.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return ImageDataFactory.create(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @RequestMapping(value = "/cancleGbOrderSx/{id}")
    @ResponseBody
    public R cancleGbOrderSx(@PathVariable Integer id) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(id);
        ordersEntity.setNxDoWeight(null);
        ordersEntity.setNxDoSubtotal("0");
        ordersEntity.setNxDoStatus(getNxOrderStatusNew());
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrdersService.update(ordersEntity);

        Integer gbDepartmentOrderId = ordersEntity.getNxDoGbDepartmentOrderId();
        GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrderId);
        gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusNew());
        gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderStatusProcurement());
        gbDepartmentOrdersEntity.setGbDoWeight("0");
        gbDepartmentOrdersEntity.setGbDoSubtotal("0");
        gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);

        Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
        purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
        purchaseGoodsEntity.setGbDpgBuySubtotal("0");
//        purchaseGoodsEntity.setGbDpgBuyQuantity("");
        gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);


        return R.ok();
    }

    @RequestMapping(value = "/cancleGbOrder/{id}")
    @ResponseBody
    public R cancleGbOrder(@PathVariable Integer id) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(id);
        ordersEntity.setNxDoWeight(null);
        ordersEntity.setNxDoSubtotal("0");
        ordersEntity.setNxDoStatus(getNxOrderStatusNew());
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrdersService.update(ordersEntity);

        Integer gbDepartmentOrderId = ordersEntity.getNxDoGbDepartmentOrderId();
        GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrderId);
        gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusNew());
        gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderStatusProcurement());
        gbDepartmentOrdersEntity.setGbDoWeight("0");
        gbDepartmentOrdersEntity.setGbDoSubtotal("0");
        gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);

        Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
        purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
        purchaseGoodsEntity.setGbDpgBuySubtotal("0");
//        purchaseGoodsEntity.setGbDpgBuyQuantity(null);
        gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

        Integer gbDpgBatchId = purchaseGoodsEntity.getGbDpgBatchId();
        GbDistributerPurchaseBatchEntity batchEntity = gbDistributerPurchaseBatchService.queryObject(gbDpgBatchId);
        batchEntity.setGbDpbStatus(getGbDisPurchaseBatchSellerReply());
        batchEntity.setGbDpbSubtotal("0");
        gbDistributerPurchaseBatchService.update(batchEntity);

        return R.ok();
    }


    @RequestMapping(value = "/delNx/{id}")
    @ResponseBody
    public R delNx(@PathVariable Integer id) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", id);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.queryDepartmentListByParams(map);
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                Integer departmentId = departmentEntity.getNxDepartmentId();
                map.put("depId", departmentId);
                List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
                if (departmentDisGoodsEntities.size() > 0) {
                    for (NxDepartmentDisGoodsEntity disGoodsEntity : departmentDisGoodsEntities) {
                        nxDepartmentDisGoodsService.delete(disGoodsEntity.getNxDepartmentDisGoodsId());
                    }
                }

                List<NxDepartmentUserEntity> userEntities = nxDepartmentUserService.queryAllUsersByDepId(departmentId);
                if (userEntities.size() > 0) {
                    for (NxDepartmentUserEntity userEntity : userEntities) {
                        nxDepartmentUserService.delete(userEntity.getNxDepartmentUserId());
                    }
                }


                List<NxDepartmentOrdersHistoryEntity> nxDepartmentOrdersHistoryEntities = ordersHistoryService.queryDepHistoryOrdersByParams(map);
                if (nxDepartmentOrdersHistoryEntities.size() > 0) {
                    for (NxDepartmentOrdersHistoryEntity userEntity : nxDepartmentOrdersHistoryEntities) {
                        historyService.delete(userEntity.getNxDepartmentOrdersHistoryId());
                    }
                }

                nxDepartmentService.delete(departmentEntity.getNxDepartmentId());

            }
        }

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                nxDepartmentOrdersService.delete(ordersEntity.getNxDepartmentOrdersId());
            }
        }

        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity NxDistributerPurchase : purchaseGoodsEntities) {
                nxDistributerPurchaseGoodsService.delete(NxDistributerPurchase.getNxDistributerPurchaseGoodsId());
            }
        }

        List<NxDistributerPurchaseBatchEntity> batchEntities = nxDistributerPurchaseBatchService.queryDisPurchaseBatch(map);
        if (batchEntities.size() > 0) {
            for (NxDistributerPurchaseBatchEntity batchEntity : batchEntities) {
                nxDistributerPurchaseBatchService.delete(batchEntity.getNxDistributerPurchaseBatchId());
            }
        }

        List<NxDistributerGoodsEntity> distributerGoodsEntities = nxDistributerGoodsService.queryDisGoodsByParams(map);
        if (distributerGoodsEntities.size() > 0) {
            for (NxDistributerGoodsEntity batchEntity : distributerGoodsEntities) {

                map.put("disGoodsId", batchEntity.getNxDistributerGoodsId());
                List<NxDistributerAliasEntity> aliasEntities = nxDistributerAliasService.queryAliasByParmas(map);
                if (aliasEntities.size() > 0) {
                    for (NxDistributerAliasEntity ordersEntity : aliasEntities) {
                        nxDistributerAliasService.delete(ordersEntity.getNxDistributerAliasId());
                    }
                }

                List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(map);
                if (distributerStandardEntities.size() > 0) {
                    for (NxDistributerStandardEntity ordersEntity : distributerStandardEntities) {
                        nxDistributerStandardService.delete(ordersEntity.getNxDistributerStandardId());
                    }
                }

                nxDistributerGoodsService.delete(batchEntity.getNxDistributerGoodsId());
            }
        }

        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        if (fatherGoodsEntities.size() > 0) {
            for (NxDistributerFatherGoodsEntity batchEntity : fatherGoodsEntities) {
                nxDistributerFatherGoodsService.delete(batchEntity.getNxDistributerFatherGoodsId());
            }
        }

        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);
        if (billEntityList.size() > 0) {
            for (NxDepartmentBillEntity batchEntity : billEntityList) {
                nxDepartmentBillService.delete(batchEntity.getNxDepartmentBillId());
            }
        }
        nxDistributerService.delteNxDis(id);

        return R.ok();
    }


    @ResponseBody
    @RequestMapping("/deleteNxOrders")
    public R deleteNxOrders(@RequestBody Integer[] nxOrdersSubIds) {
        nxDepartmentOrdersService.deleteBatch(nxOrdersSubIds);
        return R.ok();
    }

    @RequestMapping(value = "/disUpdateBuyingPrice", method = RequestMethod.POST)
    @ResponseBody
    public R disUpdateBuyingPrice(@RequestBody NxDistributerGoodsEntity disGoods) {

        nxDistributerGoodsService.update(disGoods);

        //nxOrder
        Integer distributerGoodsId = disGoods.getNxDistributerGoodsId();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", distributerGoodsId);
        map.put("equalStatus", 0);
        map.put("settleType", 0);
        System.out.println("senet");
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                if (ordersEntity.getNxDoWeight() != null && !ordersEntity.getNxDoWeight().trim().isEmpty()) {
                    BigDecimal orderWeight = new BigDecimal(ordersEntity.getNxDoWeight());
                    BigDecimal willPrice = new BigDecimal(0);
                    BigDecimal buyingPrice = new BigDecimal(disGoods.getNxDgBuyingPrice());
                    String buyingPriceLevel = "0";
                    String update = disGoods.getNxDgBuyingPriceUpdate();
                    if (disGoods.getNxDgWillPriceOneWeight() != null && new BigDecimal(disGoods.getNxDgWillPriceOneWeight()).compareTo(BigDecimal.ZERO) == 1) {
                        BigDecimal nxOneWeight = new BigDecimal(disGoods.getNxDgWillPriceOneWeight());
                        if (orderWeight.compareTo(nxOneWeight) < 1) {
                            willPrice = new BigDecimal(disGoods.getNxDgWillPriceOne());
                            buyingPriceLevel = "1";
                        } else {
                            if (disGoods.getNxDgWillPriceTwoWeight() != null && new BigDecimal(disGoods.getNxDgWillPriceTwoWeight()).compareTo(BigDecimal.ZERO) == 1) {
                                BigDecimal nxTwoWeight = new BigDecimal(disGoods.getNxDgWillPriceTwoWeight());
                                if (orderWeight.compareTo(nxTwoWeight) < 1) {
                                    willPrice = new BigDecimal(disGoods.getNxDgWillPriceTwo());
                                    buyingPriceLevel = "2";
                                } else {
                                    if (disGoods.getNxDgWillPriceThreeWeight() != null && new BigDecimal(disGoods.getNxDgWillPriceThreeWeight()).compareTo(BigDecimal.ZERO) == 1) {
                                        willPrice = new BigDecimal(disGoods.getNxDgWillPriceThree());
                                        buyingPriceLevel = "3";
                                    } else {
                                        willPrice = new BigDecimal(disGoods.getNxDgWillPriceTwo());
                                        buyingPriceLevel = "2";
                                    }
                                }
                            } else {
                                willPrice = new BigDecimal(disGoods.getNxDgWillPriceOne());
                                buyingPriceLevel = "1";
                            }

                        }
                    } else {
                        willPrice = new BigDecimal(disGoods.getNxDgWillPrice());
                    }

                    BigDecimal profitB = willPrice.subtract(buyingPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoCostPriceLevel(buyingPriceLevel);
                    ordersEntity.setNxDoCostPrice(buyingPrice.toString());
                    ordersEntity.setNxDoCostPriceUpdate(update);
                    ordersEntity.setNxDoPrice(willPrice.toString());

                    //profit
                    BigDecimal scaleB = profitB.divide(willPrice, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                    ordersEntity.setNxDoProfitScale(scaleB.toString());

                    if (ordersEntity.getNxDoStandard().equals(disGoods.getNxDgGoodsStandardname())) {
                        BigDecimal costSubtotalB = buyingPrice.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal profitSubtotal = profitB.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal orderSubtotal = willPrice.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                        ordersEntity.setNxDoCostSubtotal(costSubtotalB.toString());
                        ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());
                        ordersEntity.setNxDoSubtotal(orderSubtotal.toString());

                    }
                } else {
                    System.out.println("updatepriceeieeiieeiie");
                    ordersEntity.setNxDoPrice(disGoods.getNxDgWillPrice());
                }

                nxDepartmentOrdersService.update(ordersEntity);

            }
        }


        //nxPurGoods
        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgBuyPrice(disGoods.getNxDgBuyingPrice());
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        return R.ok();
    }

    @RequestMapping(value = "/disGetProfitTotalByGoodsFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R disGetProfitTotalByGoodsFatherId(Integer fatherId, String startDate, String stopDate) {
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        Map<String, Object> map1222 = new HashMap<>();
        map1222.put("grandId", fatherId);
        map1222.put("depFatherId", -1);
        if (howManyDaysInPeriod > 0) {
            map1222.put("startDate", startDate);
//            map1222.put("stopDate", stopDate);
        } else {
            map1222.put("date", startDate);
        }


        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryFatherGoodsByParams(map1222);

        if (fatherGoodsEntities.size() > 0) {
            for (NxDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {

                map1222.put("fatherId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
                System.out.println("faheriid" + map1222);
                Double grandDouble = nxDepartmentOrdersService.queryDepOrdersProfitSubtotal(map1222);

                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1222);
                double total = nxDepartmentOrdersService.queryCostSubtotal(map1222);
                double scaleTotal = nxDepartmentOrdersService.queryDepOrdersProfitScale(map1222);
                BigDecimal averScale = new BigDecimal(scaleTotal).divide(new BigDecimal(integer), 2, BigDecimal.ROUND_HALF_UP);

                fatherGoodsEntity.setFatherProfitTotal(grandDouble);
                fatherGoodsEntity.setFatherProfitScaleString(averScale.toString());
                fatherGoodsEntity.setFatherSubtotalTotalString(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                fatherGoodsEntity.setFatherProfitTotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
        }
        return R.ok().put("data", fatherGoodsEntities);
    }

    @RequestMapping(value = "/disGetProfitTotal", method = RequestMethod.POST)
    @ResponseBody
    public R disGetProfitTotal(Integer disId, String startDate, String stopDate) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }


        Map<String, Object> map1222 = new HashMap<>();
        map1222.put("disId", disId);
        map1222.put("depFatherId", -1);
        if (howManyDaysInPeriod > 0) {
            map1222.put("startDate", startDate);
//            map1222.put("stopDate", stopDate);
        } else {
            map1222.put("date", startDate);
        }

        System.out.println("arrr" + map1222);
        Integer count122 = nxDepartmentOrdersService.queryDepOrdersAcount(map1222);
        List<NxDistributerFatherGoodsEntity> greatGrandGoodsCost = new ArrayList<>();
        if (count122 > 0) {
            System.out.println("coun122222" + count122);


            double greatGrandDouble = 0.0;
            greatGrandGoodsCost = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map1222);
            for (NxDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsCost) {
                System.out.println("greattttttgreatGrandFather" + greatGrandFather.getNxDfgFatherGoodsName());

                List<NxDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (NxDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    System.out.println("greatttttt" + grandFather.getNxDfgFatherGoodsName());
                    Integer nxDistributerFatherGoodsId = grandFather.getNxDistributerFatherGoodsId();
                    map1222.put("grandId", nxDistributerFatherGoodsId);
                    Double grandDouble = nxDepartmentOrdersService.queryDepOrdersProfitSubtotal(map1222);
                    greatGrandDouble = greatGrandDouble + grandDouble;

                    grandFather.setFatherProfitTotal(grandDouble);
                    grandFather.setFatherProfitTotalString(new BigDecimal(grandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                greatGrandFather.setFatherGoodsEntities(grandGoodsEntities);
                greatGrandFather.setFatherProfitTotal(greatGrandDouble);
                greatGrandFather.setFatherProfitTotalString(new BigDecimal(greatGrandDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            }

        } else {
            return R.error(-1, "meiou");
        }

        Map<String, Object> mapCost = new HashMap<>();
//        mapCost.put("date", dateList);
//        mapCost.put("list", list);
//        mapCost.put("total", String.format("%.1f", aDoutble));
//        mapCost.put("salesTotal", String.format("%.1f", saleDouble));
//        mapCost.put("lossTotal", String.format("%.1f", lossDouble));
//        mapCost.put("wasteTotal", String.format("%.1f", wasteDouble));

        mapCost.put("arr", greatGrandGoodsCost);

//        mapCost.put("code", "0");
        return R.ok().put("data", mapCost);
    }


    @RequestMapping(value = "/choiceGoodsForApply", method = RequestMethod.POST)
    @ResponseBody
    public R choiceGoodsForApply(@RequestBody NxDepartmentOrdersEntity orders) {
        Integer doDisGoodsId = orders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
        NxDepartmentOrdersEntity aaa = choiceGoodsOrder(orders, disGoodsEntity);

        aaa.setNxDistributerGoodsEntityList(new ArrayList<NxDistributerGoodsEntity>());
        return R.ok().put("data", aaa);
    }

    @ResponseBody
    @RequestMapping(value = "/uploadDepOrderData", produces = "text/html;charset=UTF-8")
    public R uploadDepOrderData(@RequestParam("file") MultipartFile file,
                                @RequestParam("depFatherId") Integer depFatherId,
                                @RequestParam("depId") Integer depId,
                                @RequestParam("disId") Integer disId,
                                @RequestParam("disUserId") Integer disUserId,
                                HttpSession session) {

        System.out.println(file.getName());
        System.out.println(file.getName() + "depId====" + depId);
        List<NxDepartmentOrdersEntity> returnList = new ArrayList<>();
        HSSFWorkbook wb = null;
        try {
            wb = new HSSFWorkbook(file.getInputStream());
            HSSFSheet sheet = null;
            for (int j = 0; j < wb.getNumberOfSheets(); j++) {
                sheet = wb.getSheetAt(j);
                int lastRowNum = sheet.getLastRowNum();
                Row goodsRow = null;
                for (int i = 1; i <= lastRowNum; i++) {
                    goodsRow = sheet.getRow(i);
                    String goodsName = (String) getCellValue(goodsRow.getCell(1));
                    String quantity = (String) getCellValue(goodsRow.getCell(2));
                    String standard = (String) getCellValue(goodsRow.getCell(3));
                    String remark = (String) getCellValue(goodsRow.getCell(4));

                    NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
                    ordersEntity.setNxDoDepartmentId(depId);
                    ordersEntity.setNxDoDepartmentFatherId(depFatherId);
                    ordersEntity.setNxDoGoodsName(goodsName);
                    ordersEntity.setNxDoQuantity(quantity);
                    ordersEntity.setNxDoStandard(standard);
                    ordersEntity.setNxDoRemark(remark);
                    ordersEntity.setNxDoDistributerId(disId);
                    ordersEntity.setNxDoIsAgent(1);
                    ordersEntity.setNxDoStatus(-2);
                    ordersEntity.setNxDistributerGoodsEntityList(new ArrayList<NxDistributerGoodsEntity>());

                    //
//                    String goodsName = ordersEntity.getNxDoGoodsName();
                    Map<String, Object> mapZero = new HashMap<>();
                    mapZero.put("disId", ordersEntity.getNxDoDistributerId());
                    mapZero.put("searchStr", goodsName);
                    mapZero.put("standard", ordersEntity.getNxDoStandard());
                    System.out.println("mapzreororororor" + mapZero);
                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
                    System.out.println("resultteslsutltlt----zero" + distributerGoodsEntitiesZero.size());
                    // 一级 完全相同
                    if (distributerGoodsEntitiesZero.size() == 0) {
                        Map<String, Object> mapOne = new HashMap<>();
                        mapOne.put("disId", ordersEntity.getNxDoDistributerId());
                        mapOne.put("searchStr", goodsName);
                        List<NxDistributerGoodsEntity> distributerGoodsEntitiesOne = nxDistributerGoodsService.queryDisGoodsByName(mapOne);
                        System.out.println("resultteslsutltlt----one" + distributerGoodsEntitiesOne.size());
                        // 二级 相同
                        if (distributerGoodsEntitiesOne.size() == 0) {
                            //1, 查拼音
                            String pinyinString = "";
                            for (int p = 0; p < goodsName.length(); p++) {
                                String str = goodsName.substring(p, p + 1);
                                if (str.matches("[\u4E00-\u9FFF]")) {
                                    pinyinString = hanziToPinyin(goodsName);
                                }
                            }
                            Map<String, Object> mapTwo = new HashMap<>();
                            mapTwo.put("disId", ordersEntity.getNxDoDistributerId());
                            mapTwo.put("searchPinyin", pinyinString);
                            List<NxDistributerGoodsEntity> disGoodsByNamePinyin = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
                            System.out.println("resultteslsutltlt----pinyin" + disGoodsByNamePinyin.size());

                            // 三级 相同
                            if (disGoodsByNamePinyin.size() == 0) {
                                //查别名
                                Map<String, Object> mapA = new HashMap<>();
                                mapA.put("disId", ordersEntity.getNxDoDistributerId());
                                mapA.put("alias", goodsName);
                                List<NxDistributerGoodsEntity> distributerGoodsEntitiesA = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
                                System.out.println("resultteslsutltlt----aila" + distributerGoodsEntitiesA.size());
                                // 四级 相同
                                if (distributerGoodsEntitiesA.size() == 0) {
                                    returnList.add(aaaTemp(ordersEntity));
                                } else {

                                    if (distributerGoodsEntitiesA.size() == 1) {
                                        // 1.保存订单
                                        NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesA.get(0);

                                        //2.添加规格
//                                        if (!disGoodsEntity.getNxDgGoodsStandardname().equals(ordersEntity.getNxDoStandard())) {
//                                            Map<String, Object> mapStand = new HashMap<>();
//                                            mapStand.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
//                                            mapStand.put("standardName", ordersEntity.getNxDoStandard());
//                                            List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapStand);
//                                            if (distributerStandardEntities.size() == 0) {
//                                                NxDistributerStandardEntity standardEntity = new NxDistributerStandardEntity();
//                                                standardEntity.setNxDsDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
//                                                standardEntity.setNxDsStandardName(ordersEntity.getNxDoStandard());
//                                                nxDistributerStandardService.save(standardEntity);
//                                            }
//                                        }
                                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                                        returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));

                                    } else {
                                        ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesA);
                                        returnList.add(aaaTemp(ordersEntity));
                                    }
                                }

                            } else {

                                if (disGoodsByNamePinyin.size() == 1) {
                                    //1 保存订单
                                    NxDistributerGoodsEntity disGoodsEntity = disGoodsByNamePinyin.get(0);

                                    //2.添加规格
//                                    if (!disGoodsEntity.getNxDgGoodsStandardname().equals(ordersEntity.getNxDoStandard())) {
//                                        Map<String, Object> mapStand = new HashMap<>();
//                                        mapStand.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
//                                        mapStand.put("standardName", ordersEntity.getNxDoStandard());
//                                        List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapStand);
//                                        if (distributerStandardEntities.size() == 0) {
//                                            NxDistributerStandardEntity standardEntity = new NxDistributerStandardEntity();
//                                            standardEntity.setNxDsDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
//                                            standardEntity.setNxDsStandardName(ordersEntity.getNxDoStandard());
//                                            nxDistributerStandardService.save(standardEntity);
//                                        }
//                                    }

                                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                                    returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));

                                } else {
                                    ordersEntity.setNxDistributerGoodsEntityList(disGoodsByNamePinyin);
                                    returnList.add(aaaTemp(ordersEntity));
                                }
                            }


                        } else {

                            if (distributerGoodsEntitiesOne.size() == 1) {
                                //1 保存订单
                                NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesOne.get(0);
                                //添加规格
//                                Map<String, Object> mapStand = new HashMap<>();
//                                mapStand.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
//                                mapStand.put("standardName", ordersEntity.getNxDoStandard());
//                                List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapStand);
//                                if (distributerStandardEntities.size() == 0) {
//                                    NxDistributerStandardEntity standardEntity = new NxDistributerStandardEntity();
//                                    standardEntity.setNxDsDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
//                                    standardEntity.setNxDsStandardName(ordersEntity.getNxDoStandard());
//                                    nxDistributerStandardService.save(standardEntity);
//                                }
                                System.out.println("oneoneneee=====111111111");

                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                                returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));

                            } else {
                                System.out.println("oneoneneee=====mrororoororoororooror");
                                ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
                                returnList.add(aaaTemp(ordersEntity));
                            }
                        }

                    } else {
                        if (distributerGoodsEntitiesZero.size() == 1) {
                            System.out.println("zeooo=====111111111");
                            NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesZero.get(0);
                            returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
                        } else {
                            System.out.println("zeooo=====mrororoororoororooror");
                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesZero);
                            returnList.add(aaaTemp(ordersEntity));
                        }
                    }

//                    //////

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("rrrrrrlisisdidi" + returnList.size());
        return R.ok().put("data", returnList);
    }


    private Object getCellValue(Cell cell) {
        System.out.println(cell.getCellType() + "typepepep???????");
        String value = "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getRichStringCellValue().getString();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
//                    double numericCellValue = cell.getNumericCellValue();
//                    String s = String.valueOf(numericCellValue);
//                    int i1 = Integer.parseInt(s.replace(".0", ""));
//                    return i1;
                    DecimalFormat df = new DecimalFormat("#.#");
                    value = df.format(cell.getNumericCellValue());
                    return value;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
        }

        return cell;

    }


    @RequestMapping(value = "/pasteSearchGoods", method = RequestMethod.POST)
    @ResponseBody
    public R pasteSearchGoods(@RequestBody List<NxDepartmentOrdersEntity> orderList) {
        List<NxDepartmentOrdersEntity> returnList = new ArrayList<>();
        for (NxDepartmentOrdersEntity ordersEntity : orderList) {

            if (ordersEntity.getNxDoRemark().equals("-1")) {
                ordersEntity.setNxDoRemark(null);
            }

            String goodsName = ordersEntity.getNxDoGoodsName();
            Map<String, Object> mapZero = new HashMap<>();
            mapZero.put("disId", ordersEntity.getNxDoDistributerId());
            mapZero.put("searchStr", goodsName);
            mapZero.put("standard", ordersEntity.getNxDoStandard());
            mapZero.put("depId", ordersEntity.getNxDoDepartmentId());
            System.out.println("mapzreororororor111zzzizizizi" + mapZero);
            List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
            System.out.println("resultteslsutltlt----zero" + distributerGoodsEntitiesZero.size());
            // 一级 没有修改商品
            if (distributerGoodsEntitiesZero.size() == 0) {
                Map<String, Object> mapOne = new HashMap<>();
                mapOne.put("disId", ordersEntity.getNxDoDistributerId());
                mapOne.put("searchStr", goodsName);
                mapOne.put("depId", ordersEntity.getNxDoDepartmentId());
                List<NxDistributerGoodsEntity> distributerGoodsEntitiesOne = nxDistributerGoodsService.queryDisGoodsByLikeName(mapOne);
                System.out.println("distributerGoodsEntitiesOnezieeee==" + distributerGoodsEntitiesOne.size());
                // 二级 只有商品名称相同
                // 二级 没有相同的，则查询拼音完全一样的
                if (distributerGoodsEntitiesOne.size() == 0) {
                    //1, 查拼音
                    String pinyinString = "";
                    for (int i = 0; i < goodsName.length(); i++) {
                        String str = goodsName.substring(i, i + 1);
                        if (str.matches("[\u4E00-\u9FFF]")) {
                            pinyinString = hanziToPinyin(goodsName);
                        }
                    }
                    Map<String, Object> mapTwo = new HashMap<>();
                    mapTwo.put("disId", ordersEntity.getNxDoDistributerId());
                    mapTwo.put("searchPinyin", pinyinString);
                    mapTwo.put("standard", ordersEntity.getNxDoStandard());
                    List<NxDistributerGoodsEntity> disGoodsByNamePinyin = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
                    System.out.println("resultteslsutltlt----pinyin" + disGoodsByNamePinyin.size());

                    // 三级 查询拼音完全一样 + 规格完全一样
                    // 三级 没有拼音完全一样的
                    if (disGoodsByNamePinyin.size() == 0) {
                        mapTwo.put("standard", null);
                        List<NxDistributerGoodsEntity> disGoodsByNamePinyinJust = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
                        System.out.println("resultteslsutltlt----pinyin" + disGoodsByNamePinyinJust.size());
                        if (disGoodsByNamePinyinJust.size() == 0) {
                            List<NxDistributerGoodsEntity> disGoodsByNamePinyinLike = nxDistributerGoodsService.queryDisGoodsByNameLikePinyin(mapTwo);
                            if (disGoodsByNamePinyinLike.size() == 0) {
                                //查别名
                                Map<String, Object> mapA = new HashMap<>();
                                mapA.put("disId", ordersEntity.getNxDoDistributerId());
                                mapA.put("alias", goodsName);
                                List<NxDistributerGoodsEntity> distributerGoodsEntitiesA = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
                                System.out.println("resultteslsutltlt----aila" + distributerGoodsEntitiesA.size());
                                // 四级 相同
                                if (distributerGoodsEntitiesA.size() == 0) {
                                    System.out.println("xinznealallapainapaainai" + mapTwo);

                                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesALike = nxDistributerGoodsService.queryDisGoodsByAliasLike(mapTwo);

                                    if (distributerGoodsEntitiesALike.size() == 0) {
                                        //查询 depGoosName
                                        Map<String, Object> mapDep = new HashMap<>();
                                        mapDep.put("depId", ordersEntity.getNxDoDepartmentId());
                                        mapDep.put("name", goodsName);
                                        System.out.println("depserareekkekekeekke" + mapDep);
                                        List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntityList = nxDepartmentDisGoodsService.queryDepartmentGoods(mapDep);
                                        if (nxDepartmentDisGoodsEntityList.size() == 0) {

                                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
                                            returnList.add(aaaTemp(ordersEntity));


                                        } else if (nxDepartmentDisGoodsEntityList.size() == 1) {
                                            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsEntityList.get(0);
                                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                                            returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
                                        } else {
                                            List<NxDistributerGoodsEntity> list = new ArrayList<>();
                                            for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : nxDepartmentDisGoodsEntityList) {
                                                Integer nxDdgDisGoodsId = departmentDisGoodsEntity.getNxDdgDisGoodsId();

                                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
                                                list.add(distributerGoodsEntity);
                                            }
                                            ordersEntity.setNxDistributerGoodsEntityList(list);
                                            returnList.add(aaaTemp(ordersEntity));

                                        }
//                                        if (departmentDisGoodsEntity != null) {
//                                            Integer nxDdgDisGoodsId = departmentDisGoodsEntity.getNxDdgDisGoodsId();
//                                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
//                                            returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
//                                        } else {
//                                            ///
//                                            returnList.add(aaaTemp(ordersEntity));
//                                        }

                                    } else {
                                        if (distributerGoodsEntitiesALike.size() == 1) {
                                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesALike);
                                            returnList.add(aaaTemp(ordersEntity));

                                        } else {
                                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesALike);
                                            returnList.add(aaaTemp(ordersEntity));
                                        }
                                    }

                                } else {
                                    if (distributerGoodsEntitiesA.size() == 1) {
                                        // 1.保存订单
                                        NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesA.get(0);

                                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                                        returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));

                                    } else {
                                        ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesA);
                                        returnList.add(aaaTemp(ordersEntity));
                                    }
                                }
                            } else {
                                if (disGoodsByNamePinyinLike.size() == 1) {
                                    NxDistributerGoodsEntity disGoodsEntity = disGoodsByNamePinyinLike.get(0);
                                    returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
                                } else {
                                    ordersEntity.setNxDistributerGoodsEntityList(disGoodsByNamePinyinLike);
                                    returnList.add(aaaTemp(ordersEntity));
                                }
                            }


                        } else {
                            if (disGoodsByNamePinyinJust.size() == 1) {
                                NxDistributerGoodsEntity disGoodsEntity = disGoodsByNamePinyinJust.get(0);
                                returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
                            } else {
                                ordersEntity.setNxDistributerGoodsEntityList(disGoodsByNamePinyinJust);
                                returnList.add(aaaTemp(ordersEntity));
                            }
                        }
                    } else {

                        // 三级 如果有拼音完全一样的
                        if (disGoodsByNamePinyin.size() == 1) {
                            //1 保存订单
                            NxDistributerGoodsEntity disGoodsEntity = disGoodsByNamePinyin.get(0);
                            returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
                        } else {
                            System.out.println("pinyinssouocduoo" + disGoodsByNamePinyin);
                            ordersEntity.setNxDistributerGoodsEntityList(disGoodsByNamePinyin);
                            returnList.add(aaaTemp(ordersEntity));
                        }
                    }

                } else {

                    // 二级 有相同的
                    // 二级 1， 只有一个商品名称相同的，那么就添加订货规格
                    if (distributerGoodsEntitiesOne.size() == 1) {
                        //1 保存订单
                        NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesOne.get(0);
                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                        returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));

                    } else {
                        // 二级 商品名称相同
                        //查询 depGoods 是否有
                        Map<String, Object> map = new HashMap<>();
                        map.put("depId", ordersEntity.getNxDoDepartmentId());
                        map.put("name", ordersEntity.getNxDoGoodsName());
                        System.out.println("chaxunxdepGoods" + map);
                        List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntityList = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
                        if (nxDepartmentDisGoodsEntityList.size() == 0) {

                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
                            returnList.add(aaaTemp(ordersEntity));


                        } else if (nxDepartmentDisGoodsEntityList.size() == 1) {
                            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsEntityList.get(0);
                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                            returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
                        } else {
                            List<NxDistributerGoodsEntity> list = new ArrayList<>();
                            for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : nxDepartmentDisGoodsEntityList) {
                                Integer nxDdgDisGoodsId = departmentDisGoodsEntity.getNxDdgDisGoodsId();

                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
                                list.add(distributerGoodsEntity);
                            }
                            ordersEntity.setNxDistributerGoodsEntityList(list);
                            returnList.add(aaaTemp(ordersEntity));

                        }
                    }
                }
            }

            // 一级 nxDis已经有商品名称+规格的商品
            // 一级 1，如果只有一个，那么就选择这个
            else {
                if (distributerGoodsEntitiesZero.size() == 1) {
                    System.out.println("zeooo=====111111111");
                    NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesZero.get(0);

                    returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
                } else {
                    // 一级 2，如果有多个，就提示商品列表，让配送商自己选择
                    System.out.println("zeooo=====mrororoororoororooror");
                    //查询 depGoods 是否有
                    Map<String, Object> map = new HashMap<>();
                    map.put("depId", ordersEntity.getNxDoDepartmentId());
                    map.put("name", ordersEntity.getNxDoGoodsName());
                    System.out.println("chaxunxdepGoods" + map);
                    List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
                    if (nxDepartmentDisGoodsEntities.size() == 1) {
                        NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsEntities.get(0);
                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(nxDepartmentDisGoodsEntity.getNxDdgDisGoodsId());
                        returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
                    } else {
                        ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesZero);
                        returnList.add(aaaTemp(ordersEntity));
                    }

                }
            }

        }


        return R.ok().put("data", returnList);
    }


    @RequestMapping(value = "/depPasteSearchGoods", method = RequestMethod.POST)
    @ResponseBody
    public R depPasteSearchGoods(@RequestBody List<NxDepartmentOrdersEntity> orderList) {
        List<NxDepartmentOrdersEntity> returnList = new ArrayList<>();
        for (NxDepartmentOrdersEntity ordersEntity : orderList) {

            if (ordersEntity.getNxDoRemark().equals("-1")) {
                ordersEntity.setNxDoRemark(null);
            }

            String goodsName = ordersEntity.getNxDoGoodsName();
            Map<String, Object> mapZero = new HashMap<>();
            mapZero.put("disId", ordersEntity.getNxDoDistributerId());
            mapZero.put("searchStr", goodsName);
            mapZero.put("standard", ordersEntity.getNxDoStandard());
            mapZero.put("nxGoodsId", 1);
            System.out.println("商品名称+规格搜索" + mapZero + "ordername--------------------" + ordersEntity.getNxDoGoodsName());
            List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
            System.out.println("resultteslsutltlt----zero" + distributerGoodsEntitiesZero.size());
            // 一级 没有修改商品
            if (distributerGoodsEntitiesZero.size() == 0) {
                //别名搜索
                Map<String, Object> mapAlias = new HashMap<>();
                mapAlias.put("disId", ordersEntity.getNxDoDistributerId());
                mapAlias.put("alias", goodsName);
                System.out.println("别名搜索" + mapAlias);
                List<NxDistributerGoodsEntity> distributerGoodsEntitiesOne = nxDistributerGoodsService.queryDisGoodsByAlias(mapAlias);
                if (distributerGoodsEntitiesOne.size() == 0) {
                    mapZero.put("standard", null);
                    System.out.println("仅商品名称搜索" + mapZero);
                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesTwo = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
                    if (distributerGoodsEntitiesTwo.size() == 0) {
                        System.out.println("商品名称拼音相同" + mapZero);
                        //1, 查拼音
                    String pinyinString = "";
                    for (int i = 0; i < goodsName.length(); i++) {
                        String str = goodsName.substring(i, i + 1);
                        if (str.matches("[\u4E00-\u9FFF]")) {
                            pinyinString = hanziToPinyin(goodsName);
                        }
                    }
                    Map<String, Object> mapTwo = new HashMap<>();
                    mapTwo.put("disId", ordersEntity.getNxDoDistributerId());
                    mapTwo.put("searchPinyin", pinyinString);
                    mapTwo.put("nxGoodsId", 1);
                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesThree = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
                        if (distributerGoodsEntitiesThree.size() == 0) {
                            //1, 查拼音
                            Map<String, Object> map = new HashMap<>();
                            map.put("depId", ordersEntity.getNxDoDepartmentId());
                            map.put("name", ordersEntity.getNxDoGoodsName());

                            List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
                            if (departmentDisGoodsEntities.size() == 1) {
                                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = departmentDisGoodsEntities.get(0);
                                Integer nxDdgDisGoodsId = nxDepartmentDisGoodsEntity.getNxDdgDisGoodsId();
                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(nxDdgDisGoodsId);
                                returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
                            } else {

                                returnList.add(saveLinshiGoodsForPasteOrder(ordersEntity));
                            }
                        } else if (distributerGoodsEntitiesThree.size() == 1) {
                            NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesThree.get(0);
                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                            returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));

                        } else {
                            System.out.println("多个distributerGoodsEntitiesThree拼音相同");
                            returnList.add(saveLinshiGoodsForPasteOrder(ordersEntity));
                        }

                    } else if (distributerGoodsEntitiesTwo.size() == 1) {
                        //1 保存订单
                        NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesTwo.get(0);
                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                        returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
                    } else {
                        System.out.println("多个distributerGoodsEntitiesTwo相同");

                        //1, 查拼音
                        Map<String, Object> map = new HashMap<>();
                        map.put("depId", ordersEntity.getNxDoDepartmentId());
                        map.put("name", ordersEntity.getNxDoGoodsName());
                        System.out.println("duoggsshangpsuodepgods" + map);
                        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
                        if (departmentDisGoodsEntities.size() == 1) {
                            NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = departmentDisGoodsEntities.get(0);
                            Integer nxDdgDisGoodsId = nxDepartmentDisGoodsEntity.getNxDdgDisGoodsId();
                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(nxDdgDisGoodsId);
                            returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
                        } else {

                            returnList.add(saveLinshiGoodsForPasteOrder(ordersEntity));
                        }
                    }

                } else if (distributerGoodsEntitiesOne.size() == 1) {
                    System.out.println("zeooo=====111111111");
                    NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesOne.get(0);
                    returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
                } else {
                    System.out.println("多个distributerGoodsEntitiesOne相同");
                    returnList.add(saveLinshiGoodsForPasteOrder(ordersEntity));
                }

            } else {
                if (distributerGoodsEntitiesZero.size() == 1) {
                    System.out.println("zeooo=====111111111");
                    NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesZero.get(0);
                    returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
                } else {
                    System.out.println("多个mapZero结果商品名称+规格搜索");
                    returnList.add(saveLinshiGoodsForPasteOrder(ordersEntity));
                }
            }

        }

        return R.ok().put("data", returnList);
    }
//
//
//    @RequestMapping(value = "/depPasteSearchGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R depPasteSearchGoods(@RequestBody List<NxDepartmentOrdersEntity> orderList) {
//        List<NxDepartmentOrdersEntity> returnList = new ArrayList<>();
//        for (NxDepartmentOrdersEntity ordersEntity : orderList) {
//
//            if (ordersEntity.getNxDoRemark().equals("-1")) {
//                ordersEntity.setNxDoRemark(null);
//            }
//
//            String goodsName = ordersEntity.getNxDoGoodsName();
//            Map<String, Object> mapZero = new HashMap<>();
//            mapZero.put("disId", ordersEntity.getNxDoDistributerId());
//            mapZero.put("searchStr", goodsName);
//            mapZero.put("standard", ordersEntity.getNxDoStandard());
//            mapZero.put("depId", ordersEntity.getNxDoDepartmentId());
//            System.out.println("mapzreororororor111zzzizizizi" + mapZero);
//            List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
//            System.out.println("resultteslsutltlt----zero" + distributerGoodsEntitiesZero.size());
//            // 一级 没有修改商品
//            if (distributerGoodsEntitiesZero.size() == 0) {
//                Map<String, Object> mapOne = new HashMap<>();
//                mapOne.put("disId", ordersEntity.getNxDoDistributerId());
//                mapOne.put("searchStr", goodsName);
//                mapOne.put("depId", ordersEntity.getNxDoDepartmentId());
//                List<NxDistributerGoodsEntity> distributerGoodsEntitiesOne = nxDistributerGoodsService.queryDisGoodsByLikeName(mapOne);
//                // 二级 只有商品名称相同
//                // 二级 没有相同的，则查询拼音完全一样的
//                if (distributerGoodsEntitiesOne.size() == 0) {
//                    //1, 查拼音
//                    String pinyinString = "";
//                    for (int i = 0; i < goodsName.length(); i++) {
//                        String str = goodsName.substring(i, i + 1);
//                        if (str.matches("[\u4E00-\u9FFF]")) {
//                            pinyinString = hanziToPinyin(goodsName);
//                        }
//                    }
//                    Map<String, Object> mapTwo = new HashMap<>();
//                    mapTwo.put("disId", ordersEntity.getNxDoDistributerId());
//                    mapTwo.put("searchPinyin", pinyinString);
//                    mapTwo.put("standard", ordersEntity.getNxDoStandard());
//                    List<NxDistributerGoodsEntity> disGoodsByNamePinyin = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
//                    System.out.println("resultteslsutltlt----pinyin" + disGoodsByNamePinyin.size());
//
//                    // 三级 查询拼音完全一样 + 规格完全一样
//                    // 三级 没有拼音完全一样的
//                    if (disGoodsByNamePinyin.size() == 0) {
//
//                        mapTwo.put("standard", null);
//                        List<NxDistributerGoodsEntity> disGoodsByNamePinyinJust = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
//                        System.out.println("resultteslsutltlt----pinyin" + disGoodsByNamePinyinJust.size());
//                        if (disGoodsByNamePinyinJust.size() == 0) {
//                            //查别名
//                            Map<String, Object> mapA = new HashMap<>();
//                            mapA.put("disId", ordersEntity.getNxDoDistributerId());
//                            mapA.put("alias", goodsName);
//                            List<NxDistributerGoodsEntity> distributerGoodsEntitiesA = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
//                            System.out.println("resultteslsutltlt----aila" + distributerGoodsEntitiesA.size());
//                            // 四级 相同
//                            if (distributerGoodsEntitiesA.size() == 0) {
//                                //查询 depGoosName
//                                Map<String, Object> mapDep = new HashMap<>();
//                                mapDep.put("depId", ordersEntity.getNxDoDepartmentId());
//                                mapDep.put("name", goodsName);
//                                System.out.println("depserareekkekekeekke" + mapDep);
////                                NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoods(mapDep);
////                                if (departmentDisGoodsEntity != null) {
////                                    Integer nxDdgDisGoodsId = departmentDisGoodsEntity.getNxDdgDisGoodsId();
////                                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
////                                    returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
////                                } else {
////                                    returnList.add(bbbTemp(ordersEntity));
////                                }
//                                List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntityList = nxDepartmentDisGoodsService.queryDepartmentGoods(mapDep);
//                                if (nxDepartmentDisGoodsEntityList.size() == 0) {
//
//                                    ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
//                                    returnList.add(aaaTemp(ordersEntity));
//
//
//                                } else if (nxDepartmentDisGoodsEntityList.size() == 1) {
//                                    NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsEntityList.get(0);
//                                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
//                                    returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
//                                } else {
//                                    List<NxDistributerGoodsEntity> list = new ArrayList<>();
//                                    for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : nxDepartmentDisGoodsEntityList) {
//                                        Integer nxDdgDisGoodsId = departmentDisGoodsEntity.getNxDdgDisGoodsId();
//
//                                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
//                                        list.add(distributerGoodsEntity);
//                                    }
//                                    ordersEntity.setNxDistributerGoodsEntityList(list);
//                                    returnList.add(aaaTemp(ordersEntity));
//
//                                }
//                            } else {
//                                if (distributerGoodsEntitiesA.size() == 1) {
//                                    // 1.保存订单
//                                    NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesA.get(0);
//
//                                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
//                                    returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
//
//                                } else {
//                                    ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesA);
//                                    ///
//                                    returnList.add(bbbTemp(ordersEntity));
//                                }
//                            }
//                        } else {
//                            if (disGoodsByNamePinyinJust.size() == 1) {
//                                NxDistributerGoodsEntity disGoodsEntity = disGoodsByNamePinyinJust.get(0);
//                                returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
//                            } else {
//                                ordersEntity.setNxDistributerGoodsEntityList(disGoodsByNamePinyinJust);
//                                returnList.add(bbbTemp(ordersEntity));
//
//
//                            }
//                        }
//                    } else {
//
//                        // 三级 如果有拼音完全一样的
//                        if (disGoodsByNamePinyin.size() == 1) {
//                            //1 保存订单
//                            NxDistributerGoodsEntity disGoodsEntity = disGoodsByNamePinyin.get(0);
//                            returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
//                        } else {
//                            System.out.println("pinyinssouocduoo" + disGoodsByNamePinyin);
//                            ordersEntity.setNxDistributerGoodsEntityList(disGoodsByNamePinyin);
//                            returnList.add(bbbTemp(ordersEntity));
//
//                        }
//                    }
//
//                } else {
//
//                    // 二级 有相同的
//                    // 二级 1， 只有一个商品名称相同的，那么就添加订货规格
//                    if (distributerGoodsEntitiesOne.size() == 1) {
//                        //1 保存订单
//                        NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesOne.get(0);
//                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
//                        returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
//
//                    } else {
//                        // 二级 商品名称相同
//                        //查询 depGoods 是否有
//                        Map<String, Object> map = new HashMap<>();
//                        map.put("depId", ordersEntity.getNxDoDepartmentId());
//                        map.put("name", ordersEntity.getNxDoGoodsName());
//                        System.out.println("chaxunxdepGoods" + map);
//                        List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntityList = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
//                        if (nxDepartmentDisGoodsEntityList.size() == 0) {
//                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
//                            returnList.add(aaaTemp(ordersEntity));
//
//                        } else if (nxDepartmentDisGoodsEntityList.size() == 1) {
//                            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsEntityList.get(0);
//                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
//                            returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
//                        } else {
//                            List<NxDistributerGoodsEntity> list = new ArrayList<>();
//                            for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : nxDepartmentDisGoodsEntityList) {
//                                Integer nxDdgDisGoodsId = departmentDisGoodsEntity.getNxDdgDisGoodsId();
//
//                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
//                                list.add(distributerGoodsEntity);
//                            }
//                            ordersEntity.setNxDistributerGoodsEntityList(list);
//                            returnList.add(aaaTemp(ordersEntity));
//
//                        }
//                    }
//                }
//            }
//
//            // 一级 nxDis已经有商品名称+规格的商品
//            // 一级 1，如果只有一个，那么就选择这个
//            else {
//                if (distributerGoodsEntitiesZero.size() == 1) {
//                    System.out.println("zeooo=====111111111");
//                    NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesZero.get(0);
//
//                    returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
//                } else {
//
//
//                    saveLinshiGoodsForPasteOrder(ordersEntity);
//
//
////
////                    // 一级 2，如果有多个，就提示商品列表，让配送商自己选择
////                    System.out.println("zeooo=====mrororoororoororooror");
////                    //查询 depGoods 是否有
////                    Map<String, Object> map = new HashMap<>();
////                    map.put("depId", ordersEntity.getNxDoDepartmentId());
////                    map.put("name", ordersEntity.getNxDoGoodsName());
////                    System.out.println("chaxunxdepGoods" + map);
////                    List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
////                    if (nxDepartmentDisGoodsEntities.size() == 1) {
////                        NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsEntities.get(0);
////                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(nxDepartmentDisGoodsEntity.getNxDdgDisGoodsId());
////                        returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
////                    } else {
////                        ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesZero);
////                        ///
////                        returnList.add(bbbTemp(ordersEntity));
////
////                    }
//
//                }
//            }
//
//        }
//
//
//        return R.ok().put("data", returnList);
//    }


    private NxDepartmentOrdersEntity saveLinshiGoodsForPasteOrder(NxDepartmentOrdersEntity ordersEntity) {

        String goodsName = ordersEntity.getNxDoGoodsName();
        Integer disId = ordersEntity.getNxDoDistributerId();


        NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("nxGoodsId", -1);
        map.put("goodsLevel", 2);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
        goods.setNxDgPurchaseAuto(1);
        goods.setNxDgDistributerId(disId);
        goods.setNxDgGoodsFile("logo.jpg");
        goods.setNxDgGoodsFileLarge("");
        goods.setNxDgGoodsName(goodsName);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        goods.setNxDgGoodsPinyin(pinyin);
        goods.setNxDgGoodsPy(headPinyin);
        goods.setNxDgDistributerId(disId);
        goods.setNxDgBuyingPriceIsGrade(0);
        goods.setNxDgBuyingPrice("0.1");
        goods.setNxDgWillPrice("0.1");
        goods.setNxDgBuyingPriceOne("0.1");
        goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        goods.setNxDgWillPriceOneWeight(goods.getNxDgGoodsStandardname());
        goods.setNxDgWillPriceOne("0.1");
        goods.setNxDgWillPriceOneAboutPrice("0.1");
        goods.setNxDgGoodsIsHidden(0);
        goods.setNxDgGoodsStandardname(ordersEntity.getNxDoStandard());
        goods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
        System.out.println("savegoogog" + goods);
        goods.setNxDgOutTotalWeight("0");
        goods.setNxDgPullOff(0);
        goods.setNxDgGoodsStatus(0);
        nxDistributerGoodsService.save(goods);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
        fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
        nxDistributerFatherGoodsService.update(fatherGoodsEntity);

        ordersEntity.setNxDoDisGoodsId(goods.getNxDistributerGoodsId());
        ordersEntity.setNxDoDisGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        ordersEntity.setNxDoDisGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
        ordersEntity.setNxDoStatus(-2);
        ordersEntity.setNxDoArriveDate(formatWhatDate(0));
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        ordersEntity.setNxDoApplyDate(formatWhatDay(0));
        ordersEntity.setNxDoArriveOnlyDate(formatWhatDate(0));
        ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
        ordersEntity.setNxDoArriveDate(formatWhatDay(0));
        ordersEntity.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
        ordersEntity.setNxDoGbDistributerId(-1);
        ordersEntity.setNxDoGbDepartmentFatherId(-1);
        ordersEntity.setNxDoGbDepartmentId(-1);
        ordersEntity.setNxDoNxCommunityId(-1);
        ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
        ordersEntity.setNxDoNxCommRestrauntId(-1);
        ordersEntity.setNxDoProfitSubtotal("0");
        ordersEntity.setNxDoProfitScale("0");
        ordersEntity.setNxDoCostPriceLevel("1");
        ordersEntity.setNxDoCostPrice("0.1");
        ordersEntity.setNxDoPrice("0.1");
        ordersEntity.setNxDoPurchaseGoodsId(-1);
        ordersEntity.setNxDoArriveWhatDay(getWeek(0));
        ordersEntity.setNxDoPrintStandard(goods.getNxDgGoodsStandardname());
        ordersEntity.setNxDoExpectPrice(goods.getNxDgWillPriceOne());
        ordersEntity.setNxDoCostPriceUpdate(formatWhatDay(0));
        ordersEntity.setNxDoDepDisGoodsId(-1);
        ordersEntity.setNxDoWeight(ordersEntity.getNxDoQuantity());
        ordersEntity.setNxDoGoodsType(-1);
        ordersEntity.setNxDoProfitSubtotal("0.0");
        ordersEntity.setNxDoProfitScale("0");
        BigDecimal bigDecimal = new BigDecimal(ordersEntity.getNxDoQuantity()).multiply(new BigDecimal(0.1)).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxDoSubtotal(bigDecimal.toString());
        ordersEntity.setNxDoCostSubtotal(bigDecimal.toString());

        Map<String, Object> mapss = new HashMap<>();
        mapss.put("depId", ordersEntity.getNxDoDepartmentId());
        mapss.put("status", 3);
        int orderOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapss);
        ordersEntity.setNxDoTodayOrder(orderOrder + 1);
        System.out.println("savellinshssigogogo" + ordersEntity.getNxDoTodayOrder());
        nxDepartmentOrdersService.save(ordersEntity);

        savePurGoodsAuto(ordersEntity);
        return ordersEntity;
    }

    private NxDepartmentOrdersEntity aaaTemp(NxDepartmentOrdersEntity order) {

        //1.查询 nxGoods 如果有完全一个的，就下载
        // 1.1 搜索商品名称+规格完全相同
        List<NxGoodsEntity> nxGoodsEntities = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name", order.getNxDoGoodsName());
        map.put("level", 3);
        List<NxGoodsEntity> nxGoodsEntitiesEx = nxGoodsService.queryNxGoodsByParams(map);

        List<NxGoodsEntity> nxGoodsEntitiesA = nxAliasService.queryNxGoodsByName(map);

        String pinyinString = order.getNxDoGoodsName();
        for (int i = 0; i < order.getNxDoGoodsName().length(); i++) {
            String str = order.getNxDoGoodsName().substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(order.getNxDoGoodsName());
            }
        }
        Map<String, Object> mapSame = new HashMap<>();
        mapSame.put("level", 3);
        mapSame.put("searchStr", order.getNxDoGoodsName());
        mapSame.put("searchPinyin", pinyinString);
        List<NxGoodsEntity> nxGoodsEntitiesSame = nxGoodsService.queryQuickSearchNxGoods(mapSame);

        TreeSet<NxGoodsEntity> all = new TreeSet();
        all.addAll(nxGoodsEntitiesEx);
        all.addAll(nxGoodsEntitiesA);
        all.addAll(nxGoodsEntitiesSame);

        // 1.2 商品名称相同

        // 如果没有完全一样的，则视为临时
        if (all.size() > 0) {
            // 1. 收集所有要移除的分销商商品 ID
            if (order.getNxDistributerGoodsEntityList() != null && order.getNxDistributerGoodsEntityList().size() > 0) {
                Set<Integer> dgIds = order.getNxDistributerGoodsEntityList().stream()
                        .map(NxDistributerGoodsEntity::getNxDgNxGoodsId)
                        .collect(Collectors.toSet());
                all.removeIf(goods -> dgIds.contains(goods.getNxGoodsId()));

            }
            order.setNxGoodsEntities(all);
        }

        order.setNxDoStatus(-2);
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        order.setNxDoApplyDate(formatWhatDay(0));
        order.setNxDoArriveOnlyDate(formatWhatDate(0));
        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
        order.setNxDoArriveDate(formatWhatDay(0));
        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        order.setNxDoApplyOnlyTime(formatWhatTime(0));
        order.setNxDoGbDistributerId(-1);
        order.setNxDoGbDepartmentFatherId(-1);
        order.setNxDoGbDepartmentId(-1);
        order.setNxDoNxCommunityId(-1);
        order.setNxDoNxCommRestrauntFatherId(-1);
        order.setNxDoNxCommRestrauntId(-1);
        order.setNxDoProfitSubtotal("0");
        order.setNxDoProfitScale("0");
        order.setNxDoCostPrice("0.1");
        order.setNxDoCostPriceLevel("1");

        order.setNxDoArriveWhatDay(getWeek(0));
        Map<String, Object> mapss = new HashMap<>();
        mapss.put("depId", order.getNxDoDepartmentId());
        mapss.put("status", 3);
        int orderOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapss);
        order.setNxDoTodayOrder(orderOrder + 1);
        System.out.println("costrpriririrririr" + order.getNxDoCostPrice());
        nxDepartmentOrdersService.save(order);



        return order;
    }

    private NxDepartmentOrdersEntity bbbTemp(NxDepartmentOrdersEntity order) {

        order.setNxDoStatus(-2);
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        order.setNxDoApplyDate(formatWhatDay(0));
        order.setNxDoArriveOnlyDate(formatWhatDate(0));
        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
        order.setNxDoArriveDate(formatWhatDay(0));
        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        order.setNxDoApplyOnlyTime(formatWhatTime(0));
        order.setNxDoGbDistributerId(-1);
        order.setNxDoGbDepartmentFatherId(-1);
        order.setNxDoGbDepartmentId(-1);
        order.setNxDoNxCommunityId(-1);
        order.setNxDoNxCommRestrauntFatherId(-1);
        order.setNxDoNxCommRestrauntId(-1);
        order.setNxDoProfitSubtotal("0");
        order.setNxDoProfitScale("0");

        order.setNxDoArriveWhatDay(getWeek(0));
        Map<String, Object> mapss = new HashMap<>();
        mapss.put("depId", order.getNxDoDepartmentId());
        mapss.put("status", 3);
        int orderOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapss);
        order.setNxDoTodayOrder(orderOrder + 1);

        nxDepartmentOrdersService.save(order);

        return order;
    }

    @RequestMapping(value = "/searchGoods", method = RequestMethod.POST)
    @ResponseBody
    public R searchGoods(@RequestBody List<NxDepartmentOrdersEntity> orderList) {
        List<NxDepartmentOrdersEntity> returnList = new ArrayList<>();
        for (NxDepartmentOrdersEntity ordersEntity : orderList) {
            System.out.println("zahuishsishs" + ordersEntity.getNxDoGoodsName());
            if (ordersEntity.getNxDoStatus() == -2) {

                //没有修改商品
                if (ordersEntity.getNxDoDisGoodsId() == null) {

                    String goodsName = ordersEntity.getNxDoGoodsName();
                    Map<String, Object> mapZero = new HashMap<>();
                    mapZero.put("disId", ordersEntity.getNxDoDistributerId());
                    mapZero.put("searchStr", goodsName);
                    mapZero.put("standard", ordersEntity.getNxDoStandard());
                    System.out.println("mapzreororororor" + mapZero);
                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
                    System.out.println("resultteslsutltlt----zero" + distributerGoodsEntitiesZero.size());
                    // 一级 完全相同
                    if (distributerGoodsEntitiesZero.size() == 0) {
                        Map<String, Object> mapOne = new HashMap<>();
                        mapOne.put("disId", ordersEntity.getNxDoDistributerId());
                        mapOne.put("searchStr", goodsName);
                        List<NxDistributerGoodsEntity> distributerGoodsEntitiesOne = nxDistributerGoodsService.queryDisGoodsByName(mapOne);
                        System.out.println("resultteslsutltlt----one" + distributerGoodsEntitiesOne.size());
                        // 二级 相同
                        if (distributerGoodsEntitiesOne.size() == 0) {
                            //1, 查拼音
                            String pinyinString = "";
                            for (int i = 0; i < goodsName.length(); i++) {
                                String str = goodsName.substring(i, i + 1);
                                if (str.matches("[\u4E00-\u9FFF]")) {
                                    pinyinString = hanziToPinyin(goodsName);
                                }
                            }
                            Map<String, Object> mapTwo = new HashMap<>();
                            mapTwo.put("disId", ordersEntity.getNxDoDistributerId());
                            mapTwo.put("searchPinyin", pinyinString);
                            List<NxDistributerGoodsEntity> disGoodsByNamePinyin = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
                            System.out.println("resultteslsutltlt----pinyin" + disGoodsByNamePinyin.size());

                            // 三级 相同
                            if (disGoodsByNamePinyin.size() == 0) {
                                //查别名
                                Map<String, Object> mapA = new HashMap<>();
                                mapA.put("disId", ordersEntity.getNxDoDistributerId());
                                mapA.put("alias", goodsName);
                                List<NxDistributerGoodsEntity> distributerGoodsEntitiesA = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
                                System.out.println("resultteslsutltlt----aila" + distributerGoodsEntitiesA.size());
                                // 四级 相同
                                if (distributerGoodsEntitiesA.size() == 0) {

                                    //查询 depGoosName
                                    Map<String, Object> mapDep = new HashMap<>();
                                    mapDep.put("depId", ordersEntity.getNxDoDepartmentId());
                                    mapDep.put("name", goodsName);
                                    System.out.println("depserareekkekekeekke" + mapDep);
                                    List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntityList = nxDepartmentDisGoodsService.queryDepartmentGoods(mapDep);
                                    if (nxDepartmentDisGoodsEntityList.size() == 0) {

                                        ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
                                        returnList.add(aaaTemp(ordersEntity));


                                    } else if (nxDepartmentDisGoodsEntityList.size() == 1) {
                                        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsEntityList.get(0);
                                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                                        returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
                                    } else {
                                        List<NxDistributerGoodsEntity> list = new ArrayList<>();
                                        for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : nxDepartmentDisGoodsEntityList) {
                                            Integer nxDdgDisGoodsId = departmentDisGoodsEntity.getNxDdgDisGoodsId();

                                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
                                            list.add(distributerGoodsEntity);
                                        }
                                        ordersEntity.setNxDistributerGoodsEntityList(list);
                                        returnList.add(aaaTemp(ordersEntity));

                                    }
//                                    NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoods(mapDep);
//                                    if (departmentDisGoodsEntity != null) {
//                                        Integer nxDdgDisGoodsId = departmentDisGoodsEntity.getNxDdgDisGoodsId();
//                                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
//                                        returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
//                                    } else {
//                                        ///
//                                        returnList.add(aaaTemp(ordersEntity));
//                                    }

                                } else {

                                    if (distributerGoodsEntitiesA.size() == 1) {
                                        // 1.保存订单
                                        NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesA.get(0);

                                        //2.添加规格
                                        if (!disGoodsEntity.getNxDgGoodsStandardname().equals(ordersEntity.getNxDoStandard())) {
                                            Map<String, Object> mapStand = new HashMap<>();
                                            mapStand.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
                                            mapStand.put("standardName", ordersEntity.getNxDoStandard());
                                            List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapStand);
                                            if (distributerStandardEntities.size() == 0) {
                                                NxDistributerStandardEntity standardEntity = new NxDistributerStandardEntity();
                                                standardEntity.setNxDsDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
                                                standardEntity.setNxDsStandardName(ordersEntity.getNxDoStandard());
                                                nxDistributerStandardService.save(standardEntity);
                                            }
                                        }
                                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                                        returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));

                                    } else {
                                        ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesA);
                                        returnList.add(aaaTemp(ordersEntity));
                                    }
                                }

                            } else {

                                if (disGoodsByNamePinyin.size() == 1) {
                                    //1 保存订单
                                    NxDistributerGoodsEntity disGoodsEntity = disGoodsByNamePinyin.get(0);

                                    //2.添加规格
                                    if (!disGoodsEntity.getNxDgGoodsStandardname().equals(ordersEntity.getNxDoStandard())) {
                                        Map<String, Object> mapStand = new HashMap<>();
                                        mapStand.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
                                        mapStand.put("standardName", ordersEntity.getNxDoStandard());
                                        List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapStand);
                                        if (distributerStandardEntities.size() == 0) {
                                            NxDistributerStandardEntity standardEntity = new NxDistributerStandardEntity();
                                            standardEntity.setNxDsDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
                                            standardEntity.setNxDsStandardName(ordersEntity.getNxDoStandard());
                                            nxDistributerStandardService.save(standardEntity);
                                        }
                                    }

                                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                                    returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));

                                } else {
                                    ordersEntity.setNxDistributerGoodsEntityList(disGoodsByNamePinyin);
                                    returnList.add(aaaTemp(ordersEntity));
                                }
                            }


                        } else {

                            if (distributerGoodsEntitiesOne.size() == 1) {
                                //1 保存订单
                                NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesOne.get(0);
                                //添加规格
                                Map<String, Object> mapStand = new HashMap<>();
                                mapStand.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
                                mapStand.put("standardName", ordersEntity.getNxDoStandard());
                                List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapStand);
                                if (distributerStandardEntities.size() == 0) {
                                    NxDistributerStandardEntity standardEntity = new NxDistributerStandardEntity();
                                    standardEntity.setNxDsDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
                                    standardEntity.setNxDsStandardName(ordersEntity.getNxDoStandard());
                                    nxDistributerStandardService.save(standardEntity);
                                }
                                System.out.println("oneoneneee=====111111111");

                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                                returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));

                            } else {
                                System.out.println("oneoneneee=====mrororoororoororooror");
                                ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
                                returnList.add(aaaTemp(ordersEntity));
                            }
                        }

                    } else {
                        if (distributerGoodsEntitiesZero.size() == 1) {
                            System.out.println("zeooo=====111111111");
                            NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesZero.get(0);
                            returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
                        } else {
                            System.out.println("zeooo=====mrororoororoororooror");
                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesZero);
                            returnList.add(aaaTemp(ordersEntity));
                        }
                    }

                } else {
                    //已修改商品，按照商品 id 保存订单；
                    Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
                    NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
                    //2.添加规格
                    if (!disGoodsEntity.getNxDgGoodsStandardname().equals(ordersEntity.getNxDoStandard())) {
                        Map<String, Object> mapStand = new HashMap<>();
                        mapStand.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
                        mapStand.put("standardName", ordersEntity.getNxDoStandard());
                        List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapStand);
                        if (distributerStandardEntities.size() == 0) {
                            NxDistributerStandardEntity standardEntity = new NxDistributerStandardEntity();
                            standardEntity.setNxDsDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
                            standardEntity.setNxDsStandardName(ordersEntity.getNxDoStandard());
                            nxDistributerStandardService.save(standardEntity);
                        }
                    }

                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                    returnList.add(choiceGoodsOrder(ordersEntity, distributerGoodsEntity));
                }
            } else {
                returnList.add(ordersEntity);
            }
        }

        return R.ok().put("data", returnList);
    }


    private NxDepartmentOrdersEntity saveOneOrder(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
        System.out.println("saveONeOrderereerereeqonenenneorere");
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDoStatus(0);
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoNxGoodsId(disGoodsEntity.getNxDgNxGoodsId());
        order.setNxDoNxGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        order.setNxDoApplyDate(formatWhatDay(0));
        order.setNxDoArriveOnlyDate(formatWhatDate(0));
        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
        order.setNxDoArriveDate(formatWhatDay(0));
        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        order.setNxDoApplyOnlyTime(formatWhatTime(0));
        order.setNxDoGbDistributerId(-1);
        order.setNxDoGbDepartmentFatherId(-1);
        order.setNxDoGbDepartmentId(-1);
        order.setNxDoNxCommunityId(-1);
        order.setNxDoNxCommRestrauntFatherId(-1);
        order.setNxDoNxCommRestrauntId(-1);
        order.setNxDoPurchaseGoodsId(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoArriveWhatDay(getWeek(0));

        System.out.println("levlellelelelelel111222222222222222" + order.getNxDoStandard() + disGoodsEntity.getNxDgWillPriceTwoStandard());

        if (disGoodsEntity.getNxDgWillPriceTwo() != null && order.getNxDoStandard().equals(disGoodsEntity.getNxDgWillPriceTwoStandard())) {
            System.out.println("levlellelelelelel111222222222222222");
            BigDecimal doQuantity = new BigDecimal(order.getNxDoQuantity());
            BigDecimal cosPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceTwo());
            BigDecimal willPrice = new BigDecimal(disGoodsEntity.getNxDgWillPriceTwo());
            BigDecimal costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

            //
            order.setNxDoWeight(order.getNxDoQuantity());
            order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
            order.setNxDoPrice(disGoodsEntity.getNxDgWillPriceTwo());
            order.setNxDoExpectPrice(disGoodsEntity.getNxDgWillPriceTwo());
            order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceTwo());
            order.setNxDoSubtotal(subtotal.toString());
            order.setNxDoCostSubtotal(costSubtotal.toString());
            order.setNxDoCostPriceLevel("2");
            order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceTwoUpdate());
            order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceTwo());
            order.setNxDoProfitSubtotal(profit.toString());
            order.setNxDoProfitScale(scaleB.toString());

        } else {
            order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
            order.setNxDoPrice(disGoodsEntity.getNxDgWillPriceOne());
            order.setNxDoExpectPrice(disGoodsEntity.getNxDgWillPriceOne());
            order.setNxDoCostPriceLevel("1");
            order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceOneUpdate());
            order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceOne());
            order.setNxDoWeight(order.getNxDoQuantity());
            order.setNxDoSubtotal("0");
            order.setNxDoCostSubtotal("0");
            System.out.println("orderstandndnd" + order.getNxDoStandard() + "goodsstandn" + disGoodsEntity.getNxDgGoodsStandardname());
            if (order.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname())
                    && !disGoodsEntity.getNxDgBuyingPriceOne().equals("0.1") && !disGoodsEntity.getNxDgBuyingPriceOne().trim().isEmpty()) {

                BigDecimal doQuantity = new BigDecimal(order.getNxDoQuantity());
                BigDecimal cosPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceOne());
                BigDecimal willPrice = new BigDecimal(disGoodsEntity.getNxDgWillPriceOne());
                BigDecimal costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                order.setNxDoSubtotal(subtotal.toString());
                order.setNxDoCostSubtotal(costSubtotal.toString());
                order.setNxDoProfitSubtotal(profit.toString());
                order.setNxDoProfitScale(scaleB.toString());

            }

        }


        Map<String, Object> map = new HashMap<>();
        map.put("depId", order.getNxDoDepartmentId());
        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
        map.put("standard", order.getNxDoStandard());
        System.out.println("depmapapmmdmmdd" + map);
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if (departmentDisGoodsEntity != null) {

            System.out.println("prinstnan" + departmentDisGoodsEntity.getNxDdgOrderStandard());
            if (order.getNxDoCostPriceLevel().equals("1")) {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
            } else {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
            }

            order.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
            BigDecimal costPrice = new BigDecimal(0);
            if (order.getNxDoCostPriceLevel().equals("1")) {
                costPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceOne());
                order.setNxDoCostPrice(costPrice.toString());
                order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceOneUpdate());
                if (order.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname())) {
                    BigDecimal price = new BigDecimal(departmentDisGoodsEntity.getNxDdgOrderPrice());
                    BigDecimal subtotal = price.multiply(new BigDecimal(order.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal costDecimal = costPrice.multiply(new BigDecimal(order.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal profit = subtotal.subtract(costDecimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

                    order.setNxDoSubtotal(subtotal.toString());
                    order.setNxDoCostSubtotal(costDecimal.toString());
                    order.setNxDoProfitSubtotal(profit.toString());
                    order.setNxDoProfitScale(scaleB.toString());
                }
            } else {
                BigDecimal price = new BigDecimal(departmentDisGoodsEntity.getNxDdgOrderPrice());
                costPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceTwo());
                BigDecimal costDecimal = costPrice.multiply(new BigDecimal(order.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal subtotal = price.multiply(new BigDecimal(order.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profit = subtotal.subtract(costDecimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

                order.setNxDoCostPrice(costPrice.toString());
                order.setNxDoSubtotal(subtotal.toString());
                order.setNxDoCostSubtotal(costDecimal.toString());
                order.setNxDoProfitSubtotal(profit.toString());
                order.setNxDoProfitScale(scaleB.toString());
                order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceTwoUpdate());
            }

            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());

        }


        System.out.println("orderrwhwidiidic" + order.getNxDoGoodsName() + "getNxDoTodayOrder" + order.getNxDoTodayOrder());
        if (order.getNxDoTodayOrder() == null) {
            Map<String, Object> mapss = new HashMap<>();
            mapss.put("depId", order.getNxDoDepartmentId());
            mapss.put("status", 3);
            mapss.put("todayOrder", 1);
            int orderOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapss);
            order.setNxDoTodayOrder(orderOrder + 1);
        }

        map.put("standard", null);
        System.out.println("depmapapapappapa" + map);
        NxDepartmentDisGoodsEntity departmentDisGoodsEntityO = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if (departmentDisGoodsEntityO != null) {
            order.setNxDoDepDisGoodsId(departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());

        }
        nxDepartmentOrdersService.save(order);

        //auto
        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
            savePurGoodsAuto(order);
        }
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDistributerGoodsEntity(distributerGoodsEntity);


//
        return order;
    }


    private NxDepartmentOrdersEntity updateOneOrderForChoice(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
        System.out.println("saveONeOrderereerereeqonenenneorere");
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDoStatus(0);
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoNxGoodsId(disGoodsEntity.getNxDgNxGoodsId());
        order.setNxDoNxGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        order.setNxDoApplyDate(formatWhatDay(0));
        order.setNxDoArriveOnlyDate(formatWhatDate(0));
        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
        order.setNxDoArriveDate(formatWhatDay(0));
        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        order.setNxDoApplyOnlyTime(formatWhatTime(0));
        order.setNxDoGbDistributerId(-1);
        order.setNxDoGbDepartmentFatherId(-1);
        order.setNxDoGbDepartmentId(-1);
        order.setNxDoNxCommunityId(-1);
        order.setNxDoNxCommRestrauntFatherId(-1);
        order.setNxDoNxCommRestrauntId(-1);
        order.setNxDoPurchaseGoodsId(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoArriveWhatDay(getWeek(0));

        if (disGoodsEntity.getNxDgWillPriceTwo() != null && order.getNxDoStandard().equals(disGoodsEntity.getNxDgWillPriceTwoStandard())) {

            BigDecimal doQuantity = new BigDecimal(order.getNxDoQuantity());
            BigDecimal cosPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceTwo());
            BigDecimal willPrice = new BigDecimal(disGoodsEntity.getNxDgWillPriceTwo());
            BigDecimal costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

            //
            order.setNxDoWeight(order.getNxDoQuantity());
            order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
            order.setNxDoPrice(disGoodsEntity.getNxDgWillPriceTwo());
            order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceTwo());
            order.setNxDoSubtotal(subtotal.toString());
            order.setNxDoCostSubtotal(costSubtotal.toString());
            order.setNxDoCostPriceLevel("2");
            order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceTwoUpdate());
            order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceTwo());
            order.setNxDoProfitSubtotal(profit.toString());
            order.setNxDoProfitScale(scaleB.toString());

        } else {
            order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
            order.setNxDoPrice(disGoodsEntity.getNxDgWillPriceOne());
            order.setNxDoCostPriceLevel("1");
            order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceOneUpdate());
            order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceOne());

            if (order.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname())) {

                BigDecimal doQuantity = new BigDecimal(order.getNxDoQuantity());
                BigDecimal cosPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceOne());
                BigDecimal willPrice = new BigDecimal(disGoodsEntity.getNxDgWillPriceOne());
                BigDecimal costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

                order.setNxDoWeight(order.getNxDoQuantity());
                order.setNxDoSubtotal(subtotal.toString());
                order.setNxDoCostSubtotal(costSubtotal.toString());
                order.setNxDoProfitSubtotal(profit.toString());
                order.setNxDoProfitScale(scaleB.toString());

            }
        }


        Map<String, Object> map = new HashMap<>();
        map.put("depId", order.getNxDoDepartmentId());
        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
        map.put("standard", order.getNxDoStandard());
        System.out.println("depmapapmmdmmdd" + map);
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if (departmentDisGoodsEntity != null) {
            System.out.println("prinstnan" + departmentDisGoodsEntity.getNxDdgOrderStandard());
            order.setNxDoPrintStandard(departmentDisGoodsEntity.getNxDdgOrderStandard());
            order.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
            BigDecimal costPrice = new BigDecimal(0);
            if (order.getNxDoCostPriceLevel().equals("1")) {
                costPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceOne());
                order.setNxDoCostPrice(costPrice.toString());
                order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceOneUpdate());
                if (order.getNxDoStandard().equals(departmentDisGoodsEntity.getNxDdgOrderStandard())) {
                    BigDecimal price = new BigDecimal(departmentDisGoodsEntity.getNxDdgOrderPrice());
                    BigDecimal subtotal = price.multiply(new BigDecimal(order.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal costDecimal = costPrice.multiply(new BigDecimal(order.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal profit = subtotal.subtract(costDecimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

                    order.setNxDoSubtotal(subtotal.toString());
                    order.setNxDoCostSubtotal(costDecimal.toString());
                    order.setNxDoProfitSubtotal(profit.toString());
                    order.setNxDoProfitScale(scaleB.toString());
                }
            } else {
                BigDecimal price = new BigDecimal(departmentDisGoodsEntity.getNxDdgOrderPrice());
                costPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceTwo());
                BigDecimal costDecimal = costPrice.multiply(new BigDecimal(order.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal subtotal = price.multiply(new BigDecimal(order.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profit = subtotal.subtract(costDecimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

                order.setNxDoCostPrice(costPrice.toString());
                order.setNxDoSubtotal(subtotal.toString());
                order.setNxDoCostSubtotal(costDecimal.toString());
                order.setNxDoProfitSubtotal(profit.toString());
                order.setNxDoProfitScale(scaleB.toString());
                order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceTwoUpdate());
            }

            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());

        }

        nxDepartmentOrdersService.update(order);

        //auto
        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
            savePurGoodsAuto(order);
        }
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDistributerGoodsEntity(distributerGoodsEntity);
        return order;
    }


    private NxDepartmentOrdersEntity choiceGoodsOrder(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDoStatus(0);
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoNxGoodsId(disGoodsEntity.getNxDgNxGoodsId());
        order.setNxDoNxGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        order.setNxDoApplyDate(formatWhatDay(0));
        order.setNxDoArriveOnlyDate(formatWhatDate(0));
        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
        order.setNxDoArriveDate(formatWhatDay(0));
        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        order.setNxDoApplyOnlyTime(formatWhatTime(0));
        order.setNxDoGbDistributerId(-1);
        order.setNxDoGbDepartmentFatherId(-1);
        order.setNxDoGbDepartmentId(-1);
        order.setNxDoNxCommunityId(-1);
        order.setNxDoNxCommRestrauntFatherId(-1);
        order.setNxDoNxCommRestrauntId(-1);
        order.setNxDoPurchaseGoodsId(disGoodsEntity.getNxDgPurchaseAuto());

        order.setNxDoArriveWhatDay(getWeek(0));
//        if (disGoodsEntity.getNxDgWillPriceTwo() != null) {
//            if (order.getNxDoStandard().equals(disGoodsEntity.getNxDgWillPriceTwoStandard())) {
//                order.setNxDoWeight(order.getNxDoQuantity());
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
//                order.setNxDoCostPriceLevel("2");
//
//                BigDecimal quantity = new BigDecimal(order.getNxDoQuantity());
//                BigDecimal willPrice = new BigDecimal(disGoodsEntity.getNxDgWillPriceTwo());
//                BigDecimal costPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceTwo());
//                BigDecimal subtotal = quantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//                BigDecimal costSubtotal = quantity.multiply(costPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//                order.setNxDoCostSubtotal(costSubtotal.toString());
//                order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceTwoUpdate());
//                order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceTwo());
//                order.setNxDoSubtotal(subtotal.toString());
//
//                BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
//                order.setNxDoProfitSubtotal(profit.toString());
//                BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
//                order.setNxDoProfitScale(scaleB.toString());
//                order.setNxDoProfitSubtotal(profit.toString());
//            } else {
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
//                order.setNxDoCostPriceLevel("1");
//            }
//        } else {
//            if (order.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname())) {
//                order.setNxDoWeight(order.getNxDoQuantity());
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
//                order.setNxDoCostPriceLevel("1");
//
//                BigDecimal quantity = new BigDecimal(order.getNxDoQuantity());
//                BigDecimal willPrice = new BigDecimal(disGoodsEntity.getNxDgWillPriceOne());
//                BigDecimal costPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceOne());
//                BigDecimal subtotal = quantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//                BigDecimal costSubtotal = quantity.multiply(costPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//                order.setNxDoCostSubtotal(costSubtotal.toString());
//                order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceOneUpdate());
//                order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceOne());
//                order.setNxDoSubtotal(subtotal.toString());
//
//                BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
//                order.setNxDoProfitSubtotal(profit.toString());
//                BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
//                order.setNxDoProfitScale(scaleB.toString());
//                order.setNxDoProfitSubtotal(profit.toString());
//
//            } else {
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
//                order.setNxDoCostPriceLevel("1");
//            }
//        }

//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", order.getNxDoDepartmentId());
//        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
//        System.out.println("whdepididig" + map);
//        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
//        if (departmentDisGoodsEntity != null) {
//
//            order.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
//            if (order.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname())) {
//                BigDecimal decimal = new BigDecimal(departmentDisGoodsEntity.getNxDdgOrderPrice()).multiply(new BigDecimal(order.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                order.setNxDoSubtotal(decimal.toString());
//            }
//            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
//
//            departmentDisGoodsEntity.setNxDdgOrderGoodsName(order.getNxDoGoodsName());
//            nxDepartmentDisGoodsService.update(departmentDisGoodsEntity);
//
//        }

        Integer nxDoDisGoodsId = order.getNxDoDisGoodsId();

        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);

        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = updateOneOrderForChoice(order, nxDistributerGoodsEntity);

        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
        return order;
    }


    @RequestMapping("/downloadApplysExcel")
    @ResponseBody
    public void downloadApplysExcel(HttpServletResponse response, HttpServletRequest request) {
        String id = request.getParameter("id");
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            System.out.println(id);

            wb = toCreatNxDisControlGoodsPriceForm(id);


            String fileName = new String("导出商品.xls".getBytes("utf-8"), "iso8859-1");
            response.setHeader("content-Disposition", "attachment; filename =" + fileName);
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private HSSFWorkbook toCreatNxDisControlGoodsPriceForm(String id) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("depId", id);
        map.put("equalStatus", 0);
        System.out.println("mapamapapapExcelleleel" + map);
        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(Integer.valueOf(id));

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);

        if (ordersEntities.size() > 0) {
            HSSFSheet sheet = wb.createSheet(departmentEntity.getNxDepartmentName());
            //设置表头
            HSSFRow row1 = sheet.createRow(0);
            row1.createCell(0).setCellValue("序号");
            row1.createCell(1).setCellValue("商品名称");
            row1.createCell(2).setCellValue("规格");
            row1.createCell(3).setCellValue("订货");
            row1.createCell(4).setCellValue("备注");
            //设置表体
            HSSFRow goodsRow = null;
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                goodsRow.createCell(1).setCellValue(ordersEntity.getNxDistributerGoodsEntity().getNxDgGoodsName());
                goodsRow.createCell(2).setCellValue(ordersEntity.getNxDistributerGoodsEntity().getNxDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ordersEntity.getNxDoQuantity() + ordersEntity.getNxDoStandard());
                goodsRow.createCell(4).setCellValue(ordersEntity.getNxDoRemark());
            }
        }

        return wb;
    }

    @RequestMapping(value = "/getHaveNotOutCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getHaveNotOutCataGoods(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("goodsType", goodsType);
        System.out.println("mappapapapap" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        return R.ok().put("data", fatherGoodsEntities);
    }

    @RequestMapping(value = "/stokerHaveNotOutCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R stokerHaveNotOutCataGoods(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        System.out.println("mappapapapap" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        Integer notInteger = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("purStatus", null);
        map.put("dayuPurStatus", 3);
        Integer haveInteger = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("haveCount", haveInteger);
        mapR.put("notCount", notInteger);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/stockerGetHaveOutCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetHaveOutCataGoods(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("dayuPurStatus", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        System.out.println("getHaveOutCataGoods" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);

        Integer haveInteger = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("purStatus", 4);
        map.put("equalPurStatus", null);
        Integer notInteger = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("haveCount", haveInteger);
        mapR.put("notCount", notInteger);
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/getHaveOutCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getHaveOutCataGoods(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("dayuPurStatus", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("goodsType", goodsType);
        System.out.println("getHaveOutCataGoods" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        return R.ok().put("data", fatherGoodsEntities);
    }


    @RequestMapping(value = "/pickerGetToStockGoodsWithDepIdsKf", method = RequestMethod.POST)
    @ResponseBody
    public R pickerGetToStockGoodsWithDepIdsKf(String nxDepIds, String gbDepIds, Integer disId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();

        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }

        map.put("disId", disId);
        map.put("purStatus", 2);
        map.put("goodsType", goodsType);
        System.out.println("fatheridiidididi" + map);
        map.put("shelfId", 1);
        System.out.println("shelflfllfflfff111" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        map.put("shelfId", 0);
        System.out.println("shelflfllfflfff000" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities2 = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        List<NxDistributerGoodsShelfGoodsEntity> emptyList = new ArrayList<>();
        NxDistributerGoodsShelfEntity emptyShelf = new NxDistributerGoodsShelfEntity();
        if (shelfEntities2.size() > 0) {
            for (NxDistributerGoodsShelfEntity emptyEnity : shelfEntities2) {
                emptyList.addAll(emptyEnity.getNxDisGoodsShelfGoodsEntities());
            }
            emptyShelf.setNxDisGoodsShelfGoodsEntities(emptyList);
            shelfEntities.add(emptyShelf);
        }


        return R.ok().put("data", shelfEntities);

    }


    @RequestMapping(value = "/disGetToStockGoodsWithDepIdsKf", method = RequestMethod.POST)
    @ResponseBody
    public R disGetToStockGoodsWithDepIdsKf(String nxDepIds, String gbDepIds, Integer nxDisId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();

        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }

        map.put("disId", nxDisId);
        map.put("purStatus", 2);
        map.put("goodsType", goodsType);
        System.out.println("fatheridiidididi" + map);
        map.put("shelfId", 1);
        System.out.println("shelflfllfflfff111" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        map.put("shelfId", 0);
        System.out.println("shelflfllfflfff000" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities2 = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        List<NxDistributerGoodsShelfGoodsEntity> emptyList = new ArrayList<>();
        NxDistributerGoodsShelfEntity emptyShelf = new NxDistributerGoodsShelfEntity();
        if (shelfEntities2.size() > 0) {
            for (NxDistributerGoodsShelfEntity emptyEnity : shelfEntities2) {
                emptyList.addAll(emptyEnity.getNxDisGoodsShelfGoodsEntities());
            }
            emptyShelf.setNxDisGoodsShelfGoodsEntities(emptyList);
            shelfEntities.add(emptyShelf);
        }


        mapR.put("shelfArr", shelfEntities);


        System.out.println("zahuishsshisisis" + map);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        mapR.put("depOrdersWait", count);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", nxDisId);
        mapFin.put("status", 3);
        mapFin.put("goodsType", goodsType);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", nxDisId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 2);
        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", nxDisId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        int wxCountAutoOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 0);
        int zicaiCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCount);
        mapR.put("wxCountOk", wxCountOk);
        mapR.put("wxCountAuto", wxCountAuto);
        mapR.put("wxCountAutoOk", wxCountAutoOk);
        mapR.put("zicaiCount", zicaiCount);
        mapR.put("zicaiCountOk", zicaiCountOk);
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/stockerGetToStockGoodsWithDepIdsKf", method = RequestMethod.POST)
    @ResponseBody
    @Cacheable(value = "stockGoods", key = "#nxDisId + '_' + #nxDepIds + '_' + #gbDepIds")
    public R stockerGetToStockGoodsWithDepIdsKf(String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("purStatus", 4);
        // 处理部门ID
        List<String> idsGb = parseDepIds(gbDepIds);
        List<String> idsNx = parseDepIds(nxDepIds);

        if (!idsGb.isEmpty()) {
            params.put("gbDepIds", idsGb);
        }
        if (!idsNx.isEmpty()) {
            params.put("nxDepIds", idsNx);
        }

        // 获取所有数据
        Map<String, Object> result = nxDepartmentOrdersService.queryStockGoodsData(params);

        return R.ok().put("data", result);
    }


    private List<String> parseDepIds(String depIds) {
        if ("0".equals(depIds)) {
            return Collections.emptyList();
        }
        return Arrays.asList(depIds.split(","));
    }

    @RequestMapping(value = "/stokerGetToStockGoodsWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R stokerGetToStockGoodsWithDepIds(String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDisId);
        map.put("status", 3);

        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }
        map.put("purStatus", 4);

        Integer countDep = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        System.out.println("fatheridiidididi" + map);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("grandArr", greatGrandGoods);

        System.out.println("zahuishsshisisis" + map);
        mapR.put("depOrdersWait", count);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", nxDisId);
        mapFin.put("status", 3);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        mapR.put("depOrdersWait", count);
        mapR.put("idDepOrdersWait", countDep);

        mapFin.put("purStatus", 4);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapFin);
        mapFin.put("purStatus", null);
        mapFin.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapFin);
        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/disGetToStockGoodsWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R disGetToStockGoodsWithDepIds(String nxDepIds, String gbDepIds, Integer nxDisId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();

        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }

        map.put("disId", nxDisId);
        map.put("goodsType", goodsType);
        System.out.println("fatheridiidididiwwgggggg" + map);
        map.put("purStatus", 4);
        Integer preOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("grandArr", greatGrandGoods);
        mapR.put("depOrdersWait", preOrders);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", nxDisId);
        mapFin.put("status", 3);
        mapFin.put("goodsType", goodsType);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", nxDisId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 2);
        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", nxDisId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        int wxCountAutoOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 0);
        int zicaiCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCount);
        mapR.put("wxCountOk", wxCountOk);
        mapR.put("preOrders", preOrders);
        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/disGetToStockNxDepGoodsFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetToStockNxDepGoodsFatherGoods(Integer fatherId, String nxDepIds, String gbDepIds, Integer disId, Integer goodsType) {

        Map<String, Object> map = new HashMap<>();


        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }
        Integer orderCount = 0;
        map.put("status", 3);
        map.put("fathersFatherId", fatherId);
        map.put("purStatus", 4);
        map.put("goodsType", goodsType);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);
        map.put("purStatus", 4);
        orderCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("status", 3);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapDep);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);


        Map<String, Object> mapR = new HashMap<>();
        mapR.put("grandArr", fatherGoodsEntities);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        mapR.put("orderCount", orderCount);

        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/disOutOrdersWithWeightFinish", method = RequestMethod.POST)
    @ResponseBody
    public R disOutOrdersWithWeightFinish(@RequestBody List<NxDepartmentOrdersEntity> ordersEntities) {

        for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
            Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
            NxDepartmentOrdersEntity oldOrderEntity = nxDepartmentOrdersService.queryObject(nxDepartmentOrdersId);

            oldOrderEntity.setNxDoWeight(ordersEntity.getNxDoWeight());
            BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());

            if (oldOrderEntity.getNxDoPrice() != null && !oldOrderEntity.getNxDoPrice().trim().isEmpty()) {
                BigDecimal priceB = new BigDecimal(oldOrderEntity.getNxDoPrice());
                BigDecimal decimal1 = priceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                oldOrderEntity.setNxDoStatus(getNxOrderStatusHasFinished());
                oldOrderEntity.setNxDoSubtotal(decimal1.toString());

            }

            BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
            BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
            oldOrderEntity.setNxDoCostSubtotal(decimal.toString());
            oldOrderEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
            nxDepartmentOrdersService.update(oldOrderEntity);
        }

        return R.ok();
    }

    @RequestMapping(value = "/disOutOrdersFinish/{ids}")
    @ResponseBody
    public R disOutOrdersFinish(@PathVariable String ids) {

        String[] arr = ids.split(",");
        if (arr.length > 0) {
            for (String id : arr) {
                NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(Integer.valueOf(id));
                ordersEntity.setNxDoWeight(ordersEntity.getNxDoQuantity());
                BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());

                if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                    BigDecimal priceB = new BigDecimal(ordersEntity.getNxDoPrice());
                    BigDecimal decimal1 = priceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoSubtotal(decimal1.toString());
                    ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
                }
                //cost
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoCostSubtotal(decimal.toString());
                ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
                nxDepartmentOrdersService.update(ordersEntity);

            }
        }


        return R.ok();
    }


    @RequestMapping(value = "/disGetStockGoodsOrdersFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoodsOrdersFatherGoods(Integer fatherId, Integer disId, Integer goodsType) {

        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", fatherId);
        map.put("equalPurStatus", 1);
        map.put("goodsType", goodsType);
        Map<String, Object> mapR = new HashMap<>();
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);
        mapR.put("grandArr", fatherGoodsEntities);
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("status", 3);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapDep);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/disGetOutGoodsOrdersFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetOutGoodsOrdersFatherGoods(Integer fatherId, Integer disId) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("fathersFatherId", fatherId);
        map.put("purStatus", 4);
        map.put("dayuPurStatus", 1);
        map.put("purType", 1);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryDisGoodsForTodayOrders(map);
        if (fatherGoodsEntities.size() > 0) {
            mapR.put("grandArr", fatherGoodsEntities);

        } else {
            mapR.put("grandArr", new ArrayList<>());
        }
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("purStatus", 4);
        mapDep.put("status", 3);
        mapDep.put("purType", 1);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapDep);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", disId);
        mapFin.put("status", 3);
        mapFin.put("dayuPurStatus", 3);
        mapFin.put("purType", 1);
        List<NxDepartmentEntity> departmentEntitiesFinish = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntitiesFinish = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);


        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        mapR.put("finishDepNx", departmentEntitiesFinish);
        mapR.put("finishDepGb", gbDepartmentEntitiesFinish);
        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/disGetHaveOutGoodsOrdersFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetHaveOutGoodsOrdersFatherGoods(Integer fatherId, Integer disId) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("fathersFatherId", fatherId);
        map.put("dayuPurStatus", 3);
        map.put("purType", 1);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryDisGoodsForTodayOrders(map);

        return R.ok().put("data", fatherGoodsEntities);

    }

    @RequestMapping(value = "/disGetToOutGoods/{disId}")
    @ResponseBody
    public R disGetToOutGoods(@PathVariable Integer disId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("dayuPurStatus", 1);
        map.put("purType", 1);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        List<NxDistributerFatherGoodsEntity> distributerGoodsEntities = new ArrayList<>();
        if (greatGrandGoods.size() > 0) {
            NxDistributerFatherGoodsEntity greatGrandsEntity = greatGrandGoods.get(0);
            Integer greatGarndGoodsId = greatGrandsEntity.getNxDistributerFatherGoodsId();
            map.put("fathersFatherId", greatGarndGoodsId);
            distributerGoodsEntities = nxDepartmentOrdersService.queryDisGoodsForTodayOrders(map);
            mapR.put("cataArr", greatGrandGoods);

        } else {
            mapR.put("cataArr", new ArrayList<>());
        }
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(map);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", disId);
        mapFin.put("status", 3);
        mapFin.put("dayuPurStatus", 3);
        mapFin.put("purType", 1);
        List<NxDepartmentEntity> departmentEntitiesFinish = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntitiesFinish = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);

        mapR.put("grandArr", distributerGoodsEntities);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        mapR.put("finishDepNx", departmentEntitiesFinish);
        mapR.put("finishDepGb", gbDepartmentEntitiesFinish);
        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("purStatus", 5);
        map111.put("purType", 0);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("purType", 1);
        map111.put("dayuPurStatus", 1);
        int purCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);


        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("purType", 0);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("purType", 1);
        int purCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("purCount", purCount);
        mapR.put("purCountOk", purCountOk);

        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/disGetHaveOutGoods/{disId}")
    @ResponseBody
    public R disGetHaveOutGoods(@PathVariable Integer disId) {
        System.out.println("didiiididdiid" + disId);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("dayuPurStatus", 3);
        map.put("purType", 1);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        List<NxDistributerFatherGoodsEntity> distributerGoodsEntities = new ArrayList<>();
        if (greatGrandGoods.size() > 0) {
            NxDistributerFatherGoodsEntity greatGrandsEntity = greatGrandGoods.get(0);
            Integer greatGarndGoodsId = greatGrandsEntity.getNxDistributerFatherGoodsId();
            map.put("fathersFatherId", greatGarndGoodsId);
            System.out.println("mapgranidididiidi" + map);
            distributerGoodsEntities = nxDepartmentOrdersService.queryDisGoodsForTodayOrders(map);
        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr", greatGrandGoods);
        mapR.put("grandArr", distributerGoodsEntities);
        System.out.println("mapapprpr" + mapR);

        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/stockerGetWaitStockGoodsDeps", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetWaitStockGoodsDeps(Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purStatus", 4);
        System.out.println("mapappapa" + map);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        // 1. 获取部门列表
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartmentSimple(map);
        List<GbDepartmentEntity> gbDepartmentEntityList = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        // 2. 批量获取统计数据
        if (!departmentEntities.isEmpty()) {
            List<Integer> depIds = departmentEntities.stream()
                    .map(NxDepartmentEntity::getNxDepartmentId)
                    .collect(Collectors.toList());

            // 一次性查询所有部门的统计数据
            Map<Integer, Map<String, Integer>> depStats = nxDepartmentOrdersService.batchQueryDepStats(depIds);

            // 设置统计数据
            for (NxDepartmentEntity dept : departmentEntities) {
                Map<String, Integer> stats = depStats.get(dept.getNxDepartmentId());
                if (stats != null) {
                    dept.setNxDepartmentAddCount(stats.get("count0"));
                    dept.setNxDepartmentPurOrderCount(stats.get("count1"));
                    dept.setNxDepartmentNeedNotPurOrderCount(stats.get("count2"));
                }
            }
        }

        // 3. 同样处理 GbDepartment
        if (!gbDepartmentEntityList.isEmpty()) {
            List<Integer> gbDepIds = gbDepartmentEntityList.stream()
                    .map(GbDepartmentEntity::getGbDepartmentId)
                    .collect(Collectors.toList());

            System.out.println("depididiisisississigbDepIds" + gbDepIds);

            Map<Integer, Map<String, Integer>> gbDepStats = nxDepartmentOrdersService.batchQueryGbDepStats(gbDepIds);

            for (GbDepartmentEntity dept : gbDepartmentEntityList) {
                Map<String, Integer> stats = gbDepStats.get(dept.getGbDepartmentId());
                if (stats != null) {
                    dept.setGbDepartmentAddCount(stats.get("count0"));
                    dept.setGbDepartmentPurOrderCount(stats.get("count1"));
                    dept.setGbDepartmentNeedNotPurOrderCount(stats.get("count2"));
                }
            }
        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDep", departmentEntities);
        mapR.put("gbDep", gbDepartmentEntityList);
        mapR.put("depOrdersWait", count);


        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/stockerGetFinishStockGoodsDeps", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetFinishStockGoodsDeps(Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
//        map.put("euqalPurStatus", 4);
        List<NxDepartmentEntity> resultNx = new ArrayList<>();

        // 1. 获取部门列表
        System.out.println("dkfkjakfdas;fkasd;stockerGetFinishStockGoodsDepsstockerGetFinishStockGoodsDepsfas" + map);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartmentSimple(map);
        List<GbDepartmentEntity> gbDepartmentEntityList = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        // 2. 批量获取统计数据
        if (!departmentEntities.isEmpty()) {
            List<Integer> depIds = departmentEntities.stream()
                    .map(NxDepartmentEntity::getNxDepartmentId)
                    .collect(Collectors.toList());

            // 一次性查询所有部门的统计数据
            Map<Integer, Map<String, Integer>> depStats = nxDepartmentOrdersService.batchQueryDepStats(depIds);

            // 设置统计数据
            for (NxDepartmentEntity dept : departmentEntities) {
                Map<String, Integer> stats = depStats.get(dept.getNxDepartmentId());
                if (stats != null) {

                    dept.setNxDepartmentAddCount(stats.get("count0"));
                    dept.setNxDepartmentPurOrderCount(stats.get("count1"));
                    dept.setNxDepartmentNeedNotPurOrderCount(stats.get("count2"));
//                    if (stats.get("count1") == 0) {
//                        resultNx.add(dept);
//                    }
                }
            }
        }
        List<GbDepartmentEntity> resultGb = new ArrayList<>();

        // 3. 同样处理 GbDepartment
        if (!gbDepartmentEntityList.isEmpty()) {
            List<Integer> gbDepIds = gbDepartmentEntityList.stream()
                    .map(GbDepartmentEntity::getGbDepartmentId)
                    .collect(Collectors.toList());

            System.out.println("depididiisisississigbDepIds" + gbDepIds);

            Map<Integer, Map<String, Integer>> gbDepStats = nxDepartmentOrdersService.batchQueryGbDepStats(gbDepIds);

            for (GbDepartmentEntity dept : gbDepartmentEntityList) {
                Map<String, Integer> stats = gbDepStats.get(dept.getGbDepartmentId());
                if (stats != null) {
                    dept.setGbDepartmentAddCount(stats.get("count0"));
                    dept.setGbDepartmentPurOrderCount(stats.get("count1"));
                    dept.setGbDepartmentNeedNotPurOrderCount(stats.get("count2"));
//                    if (stats.get("count1") == 0) {
//                        resultGb.add(dept);
//                    }
                }
            }
        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDep", departmentEntities);
        mapR.put("gbDep", resultGb);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/disGetWaitStockGoodsDeps", method = RequestMethod.POST)
    @ResponseBody
    public R disGetWaitStockGoodsDeps(Integer disId, String goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("goodsType", goodsType);
        System.out.println("depeppepe" + map);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(map);
        List<GbDepartmentEntity> gbDepartmentEntityList = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("status", 3);
                mapDep.put("goodsType", goodsType);
                mapDep.put("depFatherId", departmentEntity.getNxDepartmentId());
                Integer count0 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                mapDep.put("purStatus", 4);
                Integer count1 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                mapDep.put("dayuPurStatus", 3);
                Integer count2 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                departmentEntity.setNxDepartmentAddCount(count0);
                departmentEntity.setNxDepartmentPurOrderCount(count1);
                departmentEntity.setNxDepartmentNeedNotPurOrderCount(count2);
            }
        }

        if (gbDepartmentEntityList.size() > 0) {
            for (GbDepartmentEntity gbDepartmentEntity : gbDepartmentEntityList) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("status", 3);
                mapDep.put("goodsType", goodsType);
                mapDep.put("gbDepFatherId", gbDepartmentEntity.getGbDepartmentId());
                Integer count0 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                mapDep.put("purStatus", 4);
                System.out.println("nenennen" + mapDep);
                Integer count1 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                mapDep.put("dayuPurStatus", 3);
                Integer count2 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                gbDepartmentEntity.setGbDepartmentAddCount(count0);
                gbDepartmentEntity.setGbDepartmentPurOrderCount(count1);
                gbDepartmentEntity.setGbDepartmentNeedNotPurOrderCount(count2);
            }
        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDep", departmentEntities);
        mapR.put("gbDep", gbDepartmentEntityList);
        System.out.println("返回data" + mapR);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/disGetWaitOutGoodsDeps", method = RequestMethod.POST)
    @ResponseBody
    public R disGetWaitOutGoodsDeps(Integer disId, String type) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purType", 1);
        if (type.equals("wait")) {
            map.put("dayuPurStatus", 1);
            map.put("purStatus", 4);
        }
        if (type.equals("finish")) {
            map.put("dayuPurStatus", 3);
        }
        System.out.println("akankana" + map);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(map);
        List<GbDepartmentEntity> gbDepartmentEntity = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        if (type.equals("finish")) {
            if (departmentEntities.size() > 0) {
                for (NxDepartmentEntity departmentEntity : departmentEntities) {
                    Map<String, Object> mapDep = new HashMap<>();
                    mapDep.put("status", 3);
                    mapDep.put("purType", 1);
                    mapDep.put("depFatherId", departmentEntity.getNxDepartmentId());
                    Integer count0 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                    mapDep.put("equalPurStatus", 3);
                    Integer count1 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                    mapDep.put("dayuPurStatus", 3);
                    Integer count2 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);

                    departmentEntity.setNxDepartmentAddCount(count0);
                    departmentEntity.setNxDepartmentPurOrderCount(count1);
                    departmentEntity.setNxDepartmentNeedNotPurOrderCount(count2);

                }
            }
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDep", departmentEntities);
        mapR.put("gbDep", gbDepartmentEntity);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/getNotWeightOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getNotWeightOrders(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depFatherId", depFatherId);
        map1.put("gbDepFatherId", gbDepFatherId);
        map1.put("resFatherId", resFatherId);
        map1.put("purGoodsId", -1);
        map1.put("status", 3);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map1);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("depFatherId", depFatherId);
        mapR.put("gbDepFatherId", gbDepFatherId);
        mapR.put("resFatherId", resFatherId);
        mapR.put("equalStatus", 0);
        mapR.put("weightId", 1);
        mapR.put("purGoodsId", -1);
        mapR.put("weightType", 1);
        System.out.println("laisiisisisiisis" + mapR);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(mapR);

        Map<String, Object> map = new HashMap<>();
        map.put("equalStatus", 0);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        map.put("purGoodsId", -1);
        map.put("weightId", 0);
        Integer count1 = nxDepartmentOrdersService.queryDepOrdersAcount(map);


        Map<String, Object> map11 = new HashMap<>();
        map11.put("arr", ordersEntities);
        map11.put("weightCount", count);
        map11.put("toPrintCount", count1);

        return R.ok().put("data", map11);
    }


    @RequestMapping(value = "/getBillReturnApplys/{billId}")
    @ResponseBody
    public R getBillReturnApplys(@PathVariable Integer billId) {
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryReturnOrdersByBillId(billId);
        return R.ok().put("data", ordersEntities);
    }


    /**
     * 批发商给客户添加申请
     *
     * @param depFatherId 客户id
     * @return 订单
     */
    @RequestMapping(value = "/disGetDepTodayOrders/{depFatherId}")
    @ResponseBody
    public R disGetDepTodayOrders(@PathVariable Integer depFatherId) {
        Map<String, Object> mapR = new HashMap<>();
        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
        if (departmentEntities.size() > 0) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", departmentEntity.getNxDepartmentId());
                List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = nxDepartmentOrdersHistoryService.queryDepTodayOrder(map);
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depName", departmentEntity.getNxDepartmentName());
                mapDep.put("depId", departmentEntity.getNxDepartmentId());
                mapDep.put("list", nxDistributerGoodsEntities);
                list.add(mapDep);
            }
            mapR.put("arr", list);
            mapR.put("subDep", departmentEntities.size());

        } else {
            //今天的数据
            Map<String, Object> map = new HashMap<>();
            map.put("depId", depFatherId);
            List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = nxDepartmentOrdersHistoryService.queryDepTodayOrder(map);
            mapR.put("arr", nxDistributerGoodsEntities);
            mapR.put("subDep", 0);
        }


        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/disGetToPlanPurchaseGoodsSearch", method = RequestMethod.POST)
    @ResponseBody
    public R disGetToPlanPurchaseGoodsSearch(Integer disId, String searchStr) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        String pinyinString = searchStr;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchPinyin", pinyinString);
        map.put("searchStr", searchStr);
        System.out.println("idiiddiisisisidididididiids" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetUnPlanPurchaseApplysSearch(map);

        return R.ok().put("data", fatherGoodsEntities);
    }


    /**
     * 批发商获取未进货的订单
     *
     * @param disId 批发商id
     * @return 批发商父类商品
     */
    @RequestMapping(value = "/disGetToPlanPurchaseGoods/{disId}")
    @ResponseBody
    public R disGetToPlanPurchaseGoods(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("equalPurStatus", 0);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetUnPlanPurchaseApplysNew(map);

        if (fatherGoodsEntities.size() > 0) {
            for (NxDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                Map<String, Object> mapF = new HashMap<>();
                mapF.put("grandId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
                mapF.put("status", 3);
                mapF.put("equalPurStatus", 0);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(mapF);
                fatherGoodsEntity.setNewOrderCount(integer);
            }
        }

        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("purStatus", 3);
        map1.put("purType", -1);
        //新订单
        int newCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        // 出库
        map1.put("purStatus", 5);
        map1.put("purType", 0);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);


        // 订货
        map1.put("purType", 1);
        map1.put("inputType", 1);
        System.out.println("wxxxxxxx" + map1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        // 打印
        map1.put("inputType", 0);
        int printCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("purType", 0);
        mapOk.put("status", 3);
        mapOk.put("weight", 1);
        //出库完成
        System.out.println("mapoikkstocooutokookpk" + mapOk);
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        //订货完成
        mapOk.put("purType", 1);
        mapOk.put("inputType", 1);
        mapOk.put("weight", 1);
        mapOk.put("batchId", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        //打印
        mapOk.put("inputType", 0);
        mapOk.put("batchId", -1);
        mapOk.put("weightStatusEqual", 1);
        int printCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);


//        //////************************************
        // map4 未发送或未打印
        Map<String, Object> map4 = new HashMap<>();

        map4.put("disId", disId);
        map4.put("status", 1);
        map4.put("weightId", -1);
        map4.put("batchId", -1);
        map4.put("equalInputType", 0);
        System.out.println("map444undododo" + map4);

        map1.put("purType", 1);
        map1.put("inputType", 1);
        map1.put("equalStatus", 0);
        Integer unDoCount = nxDepartmentOrdersService.queryDepOrdersAcount(map1);


        // map4 订货已发送
        map4.put("status", 2); //NX_DIS_PURCHASE_GOODS_IS_PURCHASE == 2 huifu
        map4.put("orderStatus", 3);
        map4.put("batchId", 1);
        map4.put("dayuPurStatus", 1);
        map4.put("purStatus", 3);
        Integer wxIsBatchCountUnReply = nxDistributerPurchaseGoodsService.queryPurOrderCount(map4);
        map4.put("status", 4);
        map4.put("dayuStatus", 1);
        Integer wxIsBatchCountHaveReply = nxDistributerPurchaseGoodsService.queryPurOrderCount(map4);


        //  map4 已打印
        map4.put("batchId", -1);
        map4.put("weightId", 1);
        map4.put("weightStatusEqual", 0);
        map4.put("orderStatus", 3);
        map4.put("status", 4);
        Integer isPrintCount = nxDistributerPurchaseGoodsService.queryPurOrderCount(map4);
        System.out.println("isprint444444" + map4);
        map4.put("weightStatusEqual", 1);
        Integer isPrintHaveWeightCount = nxDistributerPurchaseGoodsService.queryPurOrderCount(map4);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("newCount", newCount);
        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCount);
        mapR.put("printCount", printCount);
        mapR.put("wxCountOk", wxCountOk);
        mapR.put("printCountOk", printCountOk);

        mapR.put("unDoCount", unDoCount);
        mapR.put("isBatchCountUnRepaly", wxIsBatchCountUnReply);
        mapR.put("isBatchCountHaveRepaly", wxIsBatchCountHaveReply);
        mapR.put("isPrintCount", isPrintCount);
        mapR.put("havePrintCount", isPrintHaveWeightCount);

        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/disGetStockGoodsToPrint", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoodsToPrint(Integer disId, Integer goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("goodsType", goodsType);
        map.put("weightId", 0);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryDisGetPrintOrderGreatGrandGoods(map);
        return R.ok().put("data", fatherGoodsEntities);
    }

    @RequestMapping(value = "/disGetStockDepartmentToPrint", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockDepartmentToPrint(Integer disId, Integer goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("goodsType", goodsType);
        map.put("weightId", 0);
//        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryDistributerFatherGoodsTodayDepartments(map);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryDistributerTodayDepartments(map);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryOrderGbDepartmentList(map);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDepArr", departmentEntities);
        mapR.put("gbDepArr", gbDepartmentEntities);
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/disGetStockGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoods(Integer disId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purStatus", 4);
        map.put("goodsType", goodsType);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("grandArr", greatGrandGoods);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", disId);
        mapFin.put("status", 3);
        mapFin.put("goodsType", goodsType);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("purStatus", 4);
        Integer preOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        map3.put("status", 3);
        Integer buyOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Map<String, Object> map3Ok = new HashMap<>();
        map3Ok.put("disId", disId);
        map3Ok.put("equalStatus", 2);
        Integer buyOrdersOk = nxDepartmentOrdersService.queryDepOrdersAcount(map3Ok);
        mapR.put("buyOrders", buyOrders);
        mapR.put("buyOrdersOk", buyOrdersOk);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        System.out.println("mapokk" + mapOk);
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        Map<String, Object> mapB = new HashMap<>();
        mapB.put("disId", disId);
        mapB.put("settleType", 0);
        mapB.put("equalStatus", 0);
        int unPayCount = nxDepartmentBillService.queryBillsCount(mapB);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCount);
        mapR.put("wxCountOk", wxCountOk);
        mapR.put("unPayCount", unPayCount);
        mapR.put("preOrders", preOrders);
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/stockerGetStockGoodsUnWeigth", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetStockGoodsUnWeigth(Integer disId) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);

        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(map);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        System.out.println("righthtmap" + map);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        mapR.put("depOrdersWait", count);
        map.put("equalPurStatus", 0);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("grandArr", greatGrandGoods);

        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map);
        map.put("purStatus", null);
        map.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map);
        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/stockerGetStockGoods", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetStockGoods(Integer disId) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        mapR.put("depOrdersWait", count);
        map.put("purStatus", 4);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("grandArr", greatGrandGoods);

        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map);
        map.put("purStatus", null);
        map.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map);
        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/disGetStockGoodsKf", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoodsKf(Integer disId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purStatus", 4);
        map.put("goodsType", goodsType);
        map.put("shelfId", 1);
        System.out.println("shelflfllfflfff" + map);

        List<NxDistributerGoodsShelfEntity> shelfEntities = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        System.out.println("shellld");
        map.put("shelfId", 0);
        System.out.println("shelflfllfflfff" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities2 = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        List<NxDistributerGoodsShelfGoodsEntity> emptyList = new ArrayList<>();
        NxDistributerGoodsShelfEntity emptyShelf = new NxDistributerGoodsShelfEntity();
        if (shelfEntities2.size() > 0) {
            for (NxDistributerGoodsShelfEntity emptyEnity : shelfEntities2) {
                emptyList.addAll(emptyEnity.getNxDisGoodsShelfGoodsEntities());
            }
            emptyShelf.setNxDisGoodsShelfGoodsEntities(emptyList);
            shelfEntities.add(emptyShelf);
        }


        System.out.println("shelrlrlrlr" + shelfEntities.size());
        mapR.put("shelfArr", shelfEntities);


        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", disId);
        mapFin.put("status", 3);
        mapFin.put("goodsType", goodsType);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("purStatus", 4);
        Integer preOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        map3.put("status", 3);
        Integer buyOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Map<String, Object> map3Ok = new HashMap<>();
        map3Ok.put("disId", disId);
        map3Ok.put("equalStatus", 2);
        Integer buyOrdersOk = nxDepartmentOrdersService.queryDepOrdersAcount(map3Ok);
        mapR.put("buyOrders", buyOrders);
        mapR.put("buyOrdersOk", buyOrdersOk);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 2);
        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        int wxCountPur = wxCount + wxCountAuto;

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        System.out.println("mapokk" + mapOk);
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        int wxCountOkAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        int wxCountPurOk = wxCountOk + wxCountOkAuto;


        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCountPur);
        mapR.put("wxCountOk", wxCountPurOk);
        mapR.put("preOrders", preOrders);
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/pickerGetStockGoodsKf", method = RequestMethod.POST)
    @ResponseBody
    public R pickerGetStockGoodsKf(Integer disId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purStatus", 4);
        map.put("goodsType", goodsType);
        map.put("shelfId", 1);
        System.out.println("shelflfllfflfffpickerGetStockGoodsKf" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        map.put("shelfId", 0);
        System.out.println("shelflfllfflfffpickerGetStockGoodsKf" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities2 = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        List<NxDistributerGoodsShelfGoodsEntity> emptyList = new ArrayList<>();
        NxDistributerGoodsShelfEntity emptyShelf = new NxDistributerGoodsShelfEntity();
        if (shelfEntities2.size() > 0) {
            for (NxDistributerGoodsShelfEntity emptyEnity : shelfEntities2) {
                emptyList.addAll(emptyEnity.getNxDisGoodsShelfGoodsEntities());
            }
            emptyShelf.setNxDisGoodsShelfGoodsEntities(emptyList);
            shelfEntities.add(emptyShelf);
        }


        System.out.println("dataArr=====" + shelfEntities);
        for (NxDistributerGoodsShelfEntity shelfEntity : shelfEntities) {
            List<NxDistributerGoodsShelfGoodsEntity> nxDisGoodsShelfGoodsEntities = shelfEntity.getNxDisGoodsShelfGoodsEntities();
            for (NxDistributerGoodsShelfGoodsEntity shelfGoodss : nxDisGoodsShelfGoodsEntities) {

            }
        }

        return R.ok().put("data", shelfEntities);

    }


    @RequestMapping("/disGetStockGoodsKfPage")
    @ResponseBody
    public R disGetStockGoodsKfPage(Integer disId, Integer page, Integer limit) {
        if (page == null || page < 1) page = 1;
        if (limit == null || limit < 1) limit = 15;

        // 1. 计算 offset
        int offset = (page - 1) * limit;

        // 2. 先做 count 查询——获取该分销商、该状态下的商品总数
        Map<String, Object> countParams = new HashMap<>();
        countParams.put("disId", disId);
//        countParams.put("status",   3);
//        countParams.put("purStatus",4);
        System.out.println("countntntntttaaaaa" + countParams);

        int total = shelfGoodsService.queryShelfGoodsCount(countParams);


        // 3. 再做带 offset/limit 的分页查询，直接返回当前页数据
        Map<String, Object> pageParams = new HashMap<>();
        pageParams.put("disId", disId);
//        pageParams.put("status",   3);
//        pageParams.put("purStatus",4);
        pageParams.put("offset", offset);
        pageParams.put("limit", limit);
        System.out.println("pagemmaididididi" + pageParams);
        List<NxDistributerGoodsShelfGoodsEntity> pageList =
                shelfGoodsService.queryShelfForGoodsWithOrders(pageParams);
        System.out.println("printpageListsize" + pageList.size());

        // 4. 构造 PageUtils 结果
        PageUtils pageUtil = new PageUtils(pageList, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping("/stockerGetStockGoodsKfPage")
    @ResponseBody
    public R stockerGetStockGoodsKfPage(Integer disId, Integer page, Integer limit) {
        if (page == null || page < 1) page = 1;
        if (limit == null || limit < 1) limit = 15;

        // 1. 计算 offset
        int offset = (page - 1) * limit;

        // 2. 先做 count 查询——获取该分销商、该状态下的商品总数
        Map<String, Object> countParams = new HashMap<>();
        countParams.put("disId", disId);
        countParams.put("status", 3);
        countParams.put("purStatus", 4);
        int total = shelfGoodsService.queryShelfGoodsCount(countParams);

        // 3. 再做带 offset/limit 的分页查询，直接返回当前页数据
        Map<String, Object> pageParams = new HashMap<>();
        pageParams.put("disId", disId);
        pageParams.put("status", 3);
        pageParams.put("purStatus", 4);
        pageParams.put("offset", offset);
        pageParams.put("limit", limit);
        System.out.println("pagemmaididididi" + pageParams);
        List<NxDistributerGoodsShelfGoodsEntity> pageList =
                shelfGoodsService.queryShelfForGoodsWithOrders(pageParams);
        // 4. 构造 PageUtils 结果
        PageUtils pageUtil = new PageUtils(pageList, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/stockerGetShelfList/{disId}")
    @ResponseBody
    public R stockerGetShelfList(@PathVariable Integer disId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", disId);
        params.put("purStatus", 4);
        System.out.println("mapososososaaaa" + params);
        List<NxDistributerGoodsShelfEntity> allShelves = nxDistributerGoodsShelfService.queryStockShelf(params);

        return R.ok().put("data", allShelves);
    }

    @RequestMapping(value = "/disGetShelfList/{disId}")
    @ResponseBody
    public R disGetShelfList(@PathVariable Integer disId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", disId);
        System.out.println("mapososososaaaa" + params);
        List<NxDistributerGoodsShelfEntity> allShelves = nxDistributerGoodsShelfService.queryShelfList(params);

        return R.ok().put("data", allShelves);
    }


    @RequestMapping(value = "/disGetStockGoodsWeb", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoodsWeb(Integer disId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purStatus", 4);
        map.put("goodsType", goodsType);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);

        if (greatGrandGoods.size() > 0) {

            for (NxDistributerFatherGoodsEntity greatGrand : greatGrandGoods) {

                List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = greatGrand.getNxDistributerGoodsEntities();
                if (nxDistributerGoodsEntities.size() > 0) {
                    for (NxDistributerGoodsEntity distributerGoodsEntity : nxDistributerGoodsEntities) {
                        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = distributerGoodsEntity.getNxDepartmentOrdersEntities();
                        if (nxDepartmentOrdersEntities.size() > 0) {
                            String content = "";
                            for (NxDepartmentOrdersEntity ordersEntity : nxDepartmentOrdersEntities) {
                                String depName = ordersEntity.getNxDepartmentEntity().getNxDepartmentName();
                                String order = ordersEntity.getNxDoQuantity() + ordersEntity.getNxDoStandard();
                                content = content + depName + " " + order + ", ";
                                System.out.println("contnennen" + content);
                                distributerGoodsEntity.setOrderContent(content);
                                distributerGoodsEntity.setOrderSize(content.length());
                            }
                        }
                    }
                }
            }
        }

        return R.ok().put("data", greatGrandGoods);
    }


    @RequestMapping(value = "/disGetStockGoodsNew", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoodsNew(Integer disId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
//        map.put("equalPurStatus", 1);
//        map.put("goodsType", goodsType);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        List<NxDistributerFatherGoodsEntity> distributerGoodsEntities = new ArrayList<>();
        if (greatGrandGoods.size() > 0) {
            NxDistributerFatherGoodsEntity greatGrandsEntity = greatGrandGoods.get(0);
            Integer greatGarndGoodsId = greatGrandsEntity.getNxDistributerFatherGoodsId();
            map.put("fathersFatherId", greatGarndGoodsId);
            distributerGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);

            mapR.put("cataArr", greatGrandGoods);
            mapR.put("grandArr", distributerGoodsEntities);

        } else {
            mapR.put("cataArr", new ArrayList<>());
            mapR.put("grandArr", new ArrayList<>());
        }

        Map<String, Object> mapW = new HashMap<>();
        mapW.put("disId", disId);
        mapW.put("weightType", 3); //出库单 == 3
        mapW.put("status", 2);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(mapW);

        mapW.put("hasWeight", 1);
        Integer count1 = nxDepartmentOrdersService.queryDepOrdersAcount(mapW);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", disId);
        mapFin.put("status", 3);
        mapFin.put("dayuPurStatus", 3);
        mapFin.put("purType", 0);
        List<NxDepartmentEntity> departmentEntitiesFinish = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntitiesFinish = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);

        Map<String, Object> mapD = new HashMap<>();
        mapD.put("disId", disId);
        mapD.put("equalPurStatus", 1);
        mapD.put("purType", 0);
        mapD.put("weightId", 0);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapD);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapD);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        mapR.put("finishDepNx", departmentEntitiesFinish);
        mapR.put("finishDepGb", gbDepartmentEntitiesFinish);

        mapR.put("totalWeightCount", count);
        mapR.put("haveFinishCount", count1);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("purStatus", 5);
        map111.put("purType", 0);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("purType", 1);
        map111.put("dayuPurStatus", 1);
        int purCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("purType", 0);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("purType", 1);
        int purCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("purCount", purCount);
        mapR.put("purCountOk", purCountOk);

        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/disGetStockGoodsNewWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoodsNewWithDepIds(String nxDepIds, String gbDepIds, Integer nxDisId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();

        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }

        map.put("equalPurStatus", 1);
        map.put("goodsType", goodsType);
//        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
//        mapR.put("grandArr", greatGrandGoods);
        List<NxDistributerFatherGoodsEntity> distributerGoodsEntities = new ArrayList<>();
        if (greatGrandGoods.size() > 0) {
            NxDistributerFatherGoodsEntity greatGrandsEntity = greatGrandGoods.get(0);
            Integer greatGarndGoodsId = greatGrandsEntity.getNxDistributerFatherGoodsId();
            map.put("fathersFatherId", greatGarndGoodsId);
//            distributerGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);
            distributerGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);

            mapR.put("cataArr", greatGrandGoods);
            mapR.put("grandArr", distributerGoodsEntities);

        } else {
            mapR.put("cataArr", new ArrayList<>());
            mapR.put("grandArr", new ArrayList<>());
        }


        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        mapR.put("depOrdersWait", count);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", nxDisId);
        mapFin.put("status", 3);
        mapFin.put("goodsType", goodsType);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", nxDisId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", nxDisId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 0);
        int zicaiCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCount);
        mapR.put("wxCountOk", wxCountOk);
        mapR.put("zicaiCount", zicaiCount);
        mapR.put("zicaiCountOk", zicaiCountOk);
        return R.ok().put("data", mapR);


    }



    @RequestMapping(value = "/depGetDepDisGoodsCata")
    @ResponseBody
    public R depGetDepDisGoodsCata(Integer depId, Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("disId", disId);
        map.put("notLinshi", 1);
        System.out.println("cattaktktktkktk");
        List<NxDistributerFatherGoodsEntity> disGoodsEntities = nxDepartmentDisGoodsService.depGetDepDisGoodsCata(map);// 1. 获取总数

        List<Integer > departmentDisGoodsEntities =   nxDepartmentDisGoodsService.queryOnlyDepGoodsIds(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr",disGoodsEntities);
        mapR.put("depGoodsArr", departmentDisGoodsEntities);

        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/disGetTypePrepareOrderPage")
    @ResponseBody
    public R disGetTypePrepareOrderPage(Integer limit, Integer page, Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("purType", 0);

        // 1. 获取总数
        int total = nxDepartmentDisGoodsService.queryDepGoodsCount(map);

        // 2. 获取当前页数据
        map.put("purStatus", 5);
        map.put("limit", limit);
        map.put("offset", (page - 1) * limit);
        System.out.println("");
        List<NxDistributerFatherGoodsEntity> currentPageList = nxDepartmentOrdersService.queryGrandGoodsOrder(map);

        // 5. 返回分页数据
        PageUtils pageUtil = new PageUtils(currentPageList, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/disGetTypePrepareOrderCata/{disId}")
    @ResponseBody
    public R disGetTypePrepareOrderCata(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("purType", 0);
//        System.out.println("aaoaoaooaoaaooa" + map);
//        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);
//        //countOrderStatus
//        fatherGoodsEntities = everyStockFatherGoodsOrderStatus(fatherGoodsEntities, disId, map);


        System.out.println("cattaktktktkktk");
        List<NxDistributerFatherGoodsEntity> disGoodsEntities = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        // 1. 获取总数

        List<Integer > disGoodsIds =   nxDepartmentOrdersService.queryOnlyNxGoodsIds(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr",disGoodsEntities);
        mapR.put("depGoodsArr", disGoodsIds);


        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("status", 3);
        map3.put("purStatus", 4);
        Integer preOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Integer buyOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Map<String, Object> map3Ok = new HashMap<>();
        map3Ok.put("disId", disId);
        map3Ok.put("equalStatus", 2);
        Integer buyOrdersOk = nxDepartmentOrdersService.queryDepOrdersAcount(map3Ok);
        mapR.put("buyOrders", buyOrders);
        mapR.put("buyOrdersOk", buyOrdersOk);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 2);
        map111.put("purStatus", 4);

        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        int wxCountPur = wxCount + wxCountAuto;

        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        System.out.println("mapokk" + mapOk);
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        //订货完成
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        System.out.println("wxcoucncncncnccn" + mapOk);
        int wxCountOkAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        int wxCountPurOk = wxCountOk + wxCountOkAuto;
        Map<String, Object> mapB = new HashMap<>();
        mapB.put("disId", disId);
        mapB.put("settleType", 0);
        mapB.put("equalStatus", 0);
        int unPayCount = nxDepartmentBillService.queryBillsCount(mapB);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCountPur);
        mapR.put("wxCountOk", wxCountPurOk);
        mapR.put("unPayCount", unPayCount);
        mapR.put("preOrders", preOrders);

        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/disGetTypePrepareOrder/{disId}")
    @ResponseBody
    public R disGetTypePrepareOrder(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("purType", 0);
        System.out.println("aaoaoaooaoaaooa" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);
        //countOrderStatus
        fatherGoodsEntities = everyStockFatherGoodsOrderStatus(fatherGoodsEntities, disId, map);


        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("status", 3);
        map3.put("purStatus", 4);
        Integer preOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Integer buyOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Map<String, Object> map3Ok = new HashMap<>();
        map3Ok.put("disId", disId);
        map3Ok.put("equalStatus", 2);
        Integer buyOrdersOk = nxDepartmentOrdersService.queryDepOrdersAcount(map3Ok);
        mapR.put("buyOrders", buyOrders);
        mapR.put("buyOrdersOk", buyOrdersOk);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 2);
        map111.put("purStatus", 4);

        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        int wxCountPur = wxCount + wxCountAuto;

        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        System.out.println("mapokk" + mapOk);
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        //订货完成
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        System.out.println("wxcoucncncncnccn" + mapOk);
        int wxCountOkAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        int wxCountPurOk = wxCountOk + wxCountOkAuto;
        Map<String, Object> mapB = new HashMap<>();
        mapB.put("disId", disId);
        mapB.put("settleType", 0);
        mapB.put("equalStatus", 0);
        int unPayCount = nxDepartmentBillService.queryBillsCount(mapB);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCountPur);
        mapR.put("wxCountOk", wxCountPurOk);
        mapR.put("unPayCount", unPayCount);
        mapR.put("preOrders", preOrders);

        mapR.put("arr", fatherGoodsEntities);
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/disGetTypePrepareOrderHaveStockOut/{disId}")
    @ResponseBody
    public R disGetTypePrepareOrderHaveStockOut(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("dayuPurStatus", 3);
        map.put("purType", 0);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);

        //countOrderStatus
        fatherGoodsEntities = everyStockFatherGoodsOrderStatus(fatherGoodsEntities, disId, map);


        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("purStatus", 3);
        map1.put("purType", -1);

        // 出库
        map1.put("purStatus", 5);
        map1.put("purType", 0);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        // 订货
        map1.put("purType", 1);
        map1.put("inputType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("stockCount", stockCount);
        mapR.put("wxCount", wxCount);
        return R.ok().put("data", mapR);

    }


    private List<NxDistributerFatherGoodsEntity> everyStockFatherGoodsOrderStatus(List<NxDistributerFatherGoodsEntity> fatherGoodsList,
                                                                                  Integer disId, Map<String, Object> map) {
        for (NxDistributerFatherGoodsEntity fatherGoods : fatherGoodsList) {
            map.put("grandId", fatherGoods.getNxDistributerFatherGoodsId());
            map.put("disId", disId);
            System.out.println("pareee" + map);
            Integer integerNew = nxDepartmentOrdersService.queryDepOrdersAcount(map);

            fatherGoods.setNewOrderCount(integerNew);
//

        }

        return fatherGoodsList;
    }


    /**
     * 9-11
     * DISTRIBUTER
     * 保存订单的数量
     *
     * @param depOrders 订单
     * @return ok
     */
    @RequestMapping(value = "/saveToFillWeightAndPrice", method = RequestMethod.POST)
    @ResponseBody
    public R saveToFillWeightAndPrice(@RequestBody NxDepartmentOrdersEntity depOrders) {

        if (depOrders.getNxDoSubtotal() != null && new BigDecimal(depOrders.getNxDoSubtotal()).compareTo(BigDecimal.ZERO) == 1) {
            depOrders.setNxDoStatus(2);
        }
        //profit
        if (depOrders.getNxDoExpectPrice() != null && !depOrders.getNxDoExpectPrice().trim().isEmpty()) {

            BigDecimal expectPrice = new BigDecimal(depOrders.getNxDoExpectPrice());
            BigDecimal doPrice = new BigDecimal(depOrders.getNxDoPrice());
            BigDecimal subtract = doPrice.subtract(expectPrice);
            depOrders.setNxDoPriceDifferent(subtract.toString());
        }

        System.out.println("sorderss" + depOrders);
        nxDepartmentOrdersService.update(depOrders);

        return R.ok();
    }


    @RequestMapping(value = "/giveOrderWeightForPrint", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderWeightForPrint(Integer orderId, String weight) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoWeight(weight);
        String nxDoPrice = ordersEntity.getNxDoPrice();
        if (nxDoPrice != null) {
            BigDecimal decimal = new BigDecimal(nxDoPrice).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(decimal.toString());
        }
        //cost
        BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
        BigDecimal weightB = new BigDecimal(weight);
        BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxDoCostSubtotal(decimal.toString());
        nxDepartmentOrdersService.update(ordersEntity);
        //weight
        if (ordersEntity.getNxDoWeightId() != null) {
            NxDistributerWeightEntity weightEntity = nxDistributerWeightService.queryObject(ordersEntity.getNxDoWeightId());
            Integer nxDwItemCount = weightEntity.getNxDwItemCount();
            Integer nxDwItemFinishCount = weightEntity.getNxDwItemFinishCount();
            if (nxDwItemCount - nxDwItemFinishCount > 1) {
                weightEntity.setNxDwItemFinishCount(nxDwItemFinishCount + 1);
            } else {
                weightEntity.setNxDwItemFinishCount(nxDwItemCount);
                weightEntity.setNxDwStatus(1);
            }
            nxDistributerWeightService.update(weightEntity);
        }
        return R.ok();
    }

    @RequestMapping(value = "/giveOrderWeightForStockPrintAndFinish", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderWeightForStockPrintAndFinish(Integer orderId, String weight) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoWeight(weight);
        String nxDoPrice = ordersEntity.getNxDoPrice();
        if (nxDoPrice != null) {
            BigDecimal decimal = new BigDecimal(nxDoPrice).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(decimal.toString());
            ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
        }
        //cost
        BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
        BigDecimal weightB = new BigDecimal(weight);
        BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxDoCostSubtotal(decimal.toString());

//        ordersEntity.setNxDoStatus(getNxOrderStatusProcurement());
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
        nxDepartmentOrdersService.update(ordersEntity);
        //weight
        if (ordersEntity.getNxDoWeightId() != null) {
            NxDistributerWeightEntity weightEntity = nxDistributerWeightService.queryObject(ordersEntity.getNxDoWeightId());
            Integer nxDwItemCount = weightEntity.getNxDwItemCount();
            Integer nxDwItemFinishCount = weightEntity.getNxDwItemFinishCount();
            if (nxDwItemCount - nxDwItemFinishCount > 1) {
                weightEntity.setNxDwItemFinishCount(nxDwItemFinishCount + 1);
            } else {
                weightEntity.setNxDwItemFinishCount(nxDwItemCount);
                weightEntity.setNxDwStatus(1);
            }
            nxDistributerWeightService.update(weightEntity);
        }
        return R.ok();
    }

    @RequestMapping(value = "/giveOrderWeight", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderWeight(Integer orderId, String weight) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoWeight(weight);
        String nxDoPrice = ordersEntity.getNxDoPrice();
        if (nxDoPrice != null) {
            BigDecimal decimal = new BigDecimal(nxDoPrice).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(decimal.toString());
        }
        //cost
        BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
        BigDecimal weightB = new BigDecimal(weight);
        BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxDoCostSubtotal(decimal.toString());
        nxDepartmentOrdersService.update(ordersEntity);
        //weight
//        if (ordersEntity.getNxDoWeightId() != null && !getNxDoWeightId().trim().isEmpty()) {
//            NxDistributerWeightEntity weightEntity = nxDistributerWeightService.queryObject(ordersEntity.getNxDoWeightId());
//            Integer nxDwItemCount = weightEntity.getNxDwItemCount();
//            Integer nxDwItemFinishCount = weightEntity.getNxDwItemFinishCount();
//            if (nxDwItemCount - nxDwItemFinishCount > 1) {
//                weightEntity.setNxDwItemFinishCount(nxDwItemFinishCount + 1);
//            } else {
//                weightEntity.setNxDwItemFinishCount(nxDwItemCount);
//                weightEntity.setNxDwStatus(1);
//            }
//            nxDistributerWeightService.update(weightEntity);
//        }
        return R.ok();
    }


    @RequestMapping(value = "/updateOrderWeight", method = RequestMethod.POST)
    @ResponseBody
    public R updateOrderWeight(Integer orderId, String weight) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoWeight(weight);
        String nxDoPrice = ordersEntity.getNxDoPrice();
        if (nxDoPrice != null) {
            BigDecimal subtotalB = new BigDecimal(nxDoPrice).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotalB.toString());
            if (ordersEntity.getNxDoCostSubtotal() != null) {
                BigDecimal weightB = new BigDecimal(weight);
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitB = subtotalB.subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                ordersEntity.setNxDoProfitScale(scaleB.toString());
                ordersEntity.setNxDoProfitSubtotal(profitB.toString());
            }

        }
        if (ordersEntity.getNxDoExpectPrice() != null && !ordersEntity.getNxDoExpectPrice().trim().isEmpty()) {
            BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
            BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
            BigDecimal subtract = doPrice.subtract(expectPrice);
            System.out.println("exxxxxxxx" + subtract);
            ordersEntity.setNxDoPriceDifferent(subtract.toString());

        }

        nxDepartmentOrdersService.update(ordersEntity);

        return R.ok();
    }

    @RequestMapping(value = "/updateOrderWeightGb", method = RequestMethod.POST)
    @ResponseBody
    public R updateOrderWeightGb(Integer orderId, String weight) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoWeight(weight);
        String nxDoPrice = ordersEntity.getNxDoPrice();
        if (nxDoPrice != null) {
            BigDecimal subtotalB = new BigDecimal(nxDoPrice).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotalB.toString());
            if (ordersEntity.getNxDoCostSubtotal() != null) {
                BigDecimal weightB = new BigDecimal(weight);
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitB = subtotalB.subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                ordersEntity.setNxDoProfitScale(scaleB.toString());
                ordersEntity.setNxDoProfitSubtotal(profitB.toString());
            }
        }

        nxDepartmentOrdersService.update(ordersEntity);

        System.out.println("xnordidiididid" + ordersEntity.getNxDoGbDepartmentOrderId());
        if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(ordersEntity.getNxDepartmentOrdersId());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusProcurement());
            gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());

            if (gbDepartmentOrdersEntity.getGbDoPrice() != null && !gbDepartmentOrdersEntity.getGbDoPrice().equals("0.0")) {
                BigDecimal decimal1 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoPrice());
                BigDecimal decimal2 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoWeight());
                BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                gbDepartmentOrdersEntity.setGbDoSubtotal(decimal3.toString());
            }
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
            System.out.println("xnordidiididid" + gbDepartmentOrdersEntity.getGbDepartmentOrdersId());


            Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();

            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", purchaseGoodsId);
            List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
            BigDecimal purWeight = new BigDecimal(0);
            if (gbDepartmentOrdersEntities.size() > 0) {
                for (GbDepartmentOrdersEntity gbOrder : gbDepartmentOrdersEntities) {
                    if (gbOrder.getGbDoWeight() != null) {
                        purWeight = purWeight.add(new BigDecimal(gbOrder.getGbDoWeight()));
                    }
                }
            }
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
            purchaseGoodsEntity.setGbDpgBuyQuantity(purWeight.toString());
            if (purchaseGoodsEntity.getGbDpgBuyPrice() != null) {
                BigDecimal decimal1 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                BigDecimal decimal2 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
                BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                purchaseGoodsEntity.setGbDpgBuySubtotal(decimal3.toString());
            }
//            purchaseGoodsEntity.setGbDpgStatus(getNxDisPurchaseGoodsIsPurchase());
            System.out.println("purpprur" + purchaseGoodsEntity);
            gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

        }

        return R.ok();
    }


    @RequestMapping(value = "/giveOrderPrice", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderPrice(Integer orderId, String price) {

        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoPrice(price);
        System.out.println("odididiididi==" + orderId + "priciiei===" + price);
        //try
        if (ordersEntity.getNxDoWeight() != null && !ordersEntity.getNxDoWeight().trim().isEmpty() && new BigDecimal(ordersEntity.getNxDoWeight()).compareTo(BigDecimal.ZERO) == 1) {
            BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());

            BigDecimal subtotalB = weightB.multiply(new BigDecimal(price)).setScale(2, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotalB.toString());
            ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());

            //profit
            if (ordersEntity.getNxDoExpectPrice() != null && !ordersEntity.getNxDoExpectPrice().trim().isEmpty() && !ordersEntity.getNxDoExpectPrice().isEmpty()) {
                BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
                BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                BigDecimal subtract = doPrice.subtract(expectPrice);
                System.out.println("exxxxxxxx" + subtract);
                ordersEntity.setNxDoPriceDifferent(subtract.toString());

            }
            if (ordersEntity.getNxDoCostSubtotal() != null && !ordersEntity.getNxDoCostSubtotal().isEmpty()) {
//
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitB = subtotalB.subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                System.out.println("subsbsbbssbbsbbbbb" + subtotalB);
                BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                ordersEntity.setNxDoProfitScale(scaleB.toString());
                ordersEntity.setNxDoProfitSubtotal(profitB.toString());
            }
        }

        nxDepartmentOrdersService.update(ordersEntity);


        return R.ok();
    }

    @RequestMapping(value = "/updateOrderPrice", method = RequestMethod.POST)
    @ResponseBody
    public R updateOrderPrice(Integer orderId, String price) {

        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoPrice(price);
        //try
        if (ordersEntity.getNxDoWeight() != null && !ordersEntity.getNxDoWeight().trim().isEmpty() && new BigDecimal(ordersEntity.getNxDoWeight()).compareTo(BigDecimal.ZERO) == 1) {
            BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());
            BigDecimal subtotalB = weightB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotalB.toString());
            ordersEntity.setNxDoStatus(2);


            //profit
            if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitB = subtotalB.subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                ordersEntity.setNxDoProfitScale(scaleB.toString());
                ordersEntity.setNxDoProfitSubtotal(profitB.toString());
            }
        }
        nxDepartmentOrdersService.update(ordersEntity);
        return R.ok();
    }


    @RequestMapping(value = "/giveOrderWeightListForStockAndFinish", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderWeightListForStockAndFinish(@RequestBody List<NxDepartmentOrdersEntity> ordersEntityList) {
        System.out.println("giveOrderWeightListForStockAndFinishgiveOrderWeightListForStockAndFinish" + ordersEntityList);
        for (NxDepartmentOrdersEntity ordersEntityOld : ordersEntityList) {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObjectNew(ordersEntityOld.getNxDepartmentOrdersId());
            ordersEntity.setNxDoWeight(ordersEntityOld.getNxDoWeight());
            ordersEntity.setNxDoPickUserId(ordersEntityOld.getNxDoPickUserId());
            if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                BigDecimal subtotal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(ordersEntityOld.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoSubtotal(subtotal.toString());
                ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
                BigDecimal nxDoPriceB = new BigDecimal(ordersEntity.getNxDoPrice());
                BigDecimal costPrice = new BigDecimal(0);
                if (ordersEntity.getNxDoCostPrice() == null) {
                    //
                    Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
                    NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
                    System.out.println("whatisgoodsnameme" + nxDistributerGoodsEntity.getNxDgGoodsName());
                    BigDecimal willPrice = new BigDecimal(0);
                    BigDecimal buyingPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPrice());
                    String buyingPriceLevel = "0";
                    String update = nxDistributerGoodsEntity.getNxDgBuyingPriceUpdate();
                    BigDecimal profitB = willPrice.subtract(buyingPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal costSubtotalB = buyingPrice.multiply(new BigDecimal(ordersEntityOld.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal profitSubtotal = profitB.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal orderSubtotal = willPrice.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoCostSubtotal(costSubtotalB.toString());
                    ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());
                    ordersEntity.setNxDoSubtotal(orderSubtotal.toString());
                    ordersEntity.setNxDoCostPriceLevel(buyingPriceLevel);
                    ordersEntity.setNxDoCostPrice(buyingPrice.toString());
                    ordersEntity.setNxDoCostPriceUpdate(update);

                } else {
                    costPrice = new BigDecimal(ordersEntity.getNxDoCostPrice());
                }

                BigDecimal profitB = nxDoPriceB.subtract(costPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitSubtotl = profitB.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoProfitSubtotal(profitSubtotl.toString());
                BigDecimal scaleB = profitB.divide(nxDoPriceB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                ordersEntity.setNxDoProfitScale(scaleB.toString());
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal weightB = new BigDecimal(ordersEntityOld.getNxDoWeight());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoCostSubtotal(decimal.toString());
            }

            ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
            nxDepartmentOrdersService.update(ordersEntity);


            System.out.println("xnordidiididid" + ordersEntity.getNxDoGbDepartmentOrderId());
            if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(ordersEntity.getNxDepartmentOrdersId());
                gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusPrepareing());
                gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusProcurement());
                gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());

                if (gbDepartmentOrdersEntity.getGbDoPrice() != null && !gbDepartmentOrdersEntity.getGbDoPrice().equals("0.0") && gbDepartmentOrdersEntity.getGbDoWeight() != null) {
                    BigDecimal decimal1 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoPrice());
                    BigDecimal decimal2 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoWeight());
                    BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                    gbDepartmentOrdersEntity.setGbDoSubtotal(decimal3.toString());
                }
                gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
                System.out.println("xnordidiididid" + gbDepartmentOrdersEntity.getGbDepartmentOrdersId());


                Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();

                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", purchaseGoodsId);
                List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
                BigDecimal purWeight = new BigDecimal(0);
                if (gbDepartmentOrdersEntities.size() > 0) {
                    for (GbDepartmentOrdersEntity gbOrder : gbDepartmentOrdersEntities) {
                        if (gbOrder.getGbDoWeight() != null) {
                            purWeight = purWeight.add(new BigDecimal(gbOrder.getGbDoWeight()));
                        }
                    }
                }
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
                purchaseGoodsEntity.setGbDpgBuyQuantity(purWeight.toString());
                if (purchaseGoodsEntity.getGbDpgBuyPrice() != null) {
                    BigDecimal decimal1 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                    BigDecimal decimal2 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
                    BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal3.toString());
                }
                purchaseGoodsEntity.setGbDpgStatus(getNxDisPurchaseGoodsIsPurchase());
                System.out.println("purpprur" + purchaseGoodsEntity);
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

            }

        }

        return R.ok();
    }

    @RequestMapping(value = "/giveOrderWeightListForStock", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderWeightListForStock(@RequestBody NxDepartmentOrdersEntity ordersEntity) {
        List<NxDistributerGoodsShelfStockEntity> outStockDisGoodsShelfStockEntities = ordersEntity.getOutStockDisGoodsShelfStockEntities();
        BigDecimal costSubtotal = new BigDecimal(0);
        for (NxDistributerGoodsShelfStockEntity stockEntity : outStockDisGoodsShelfStockEntities) {
            String nxDgssInventoryWeight = stockEntity.getNxDgssInventoryWeight();
            String inventeryPrice = stockEntity.getNxDgssPrice();
            costSubtotal = costSubtotal.add(new BigDecimal(inventeryPrice).multiply(new BigDecimal(nxDgssInventoryWeight)).setScale(1, BigDecimal.ROUND_HALF_UP));
            System.out.println("restseee" + stockEntity.getNxDgssRestWeight() + "sub==" + stockEntity.getNxDgssRestSubtotal());
            nxDisGoodsShelfStockService.update(stockEntity);

            NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
            reduceEntity.setNxDgssrDate(formatWhatDate(0));
            reduceEntity.setNxDgssrNxDepOrderId(ordersEntity.getNxDepartmentOrdersId());
            reduceEntity.setNxDgssrCostWeight(stockEntity.getNxDgssInventoryWeight());
            reduceEntity.setNxDgssrCostSubtotal(costSubtotal.toString());
            reduceEntity.setNxDgssrNxDistributerId(ordersEntity.getNxDoDistributerId());
            reduceEntity.setNxDgssrNxDisGoodsId(stockEntity.getNxDgssNxDisGoodsId());
            reduceEntity.setNxDgssrGoodsInventoryType(0);
            reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
            reduceEntity.setNxDgssrFullTime(formatFullTime());
            reduceEntity.setNxDgssrNxStockId(stockEntity.getNxDistributerGoodsShelfStockId());
            reduceEntity.setNxDgssrType(0);
            reduceEntity.setNxDgssrNxPurGoodsId(stockEntity.getNxDgssNxPurGoodsId());
            reduceEntity.setNxDgssrStatus(0);
            reduceEntity.setNxDgssrNxDisGoodsFatherId(stockEntity.getNxDgssNxDisGoodsFatherId());

            nxDisGoodsShelfStockReduceService.save(reduceEntity);
        }

        ordersEntity.setNxDoCostSubtotal(costSubtotal.toString());
        BigDecimal perPrice = costSubtotal.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxDoCostPrice(perPrice.toString());
        if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty() && new BigDecimal(ordersEntity.getNxDoPrice()).compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal subtotal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotal.toString());
            BigDecimal profitSubtotal = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());
            BigDecimal multiply = profitSubtotal.divide(subtotal).setScale(2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            ordersEntity.setNxDoProfitScale(multiply.toString());

        } else {
            ordersEntity.setNxDoSubtotal("0");
            ordersEntity.setNxDoProfitSubtotal("0");
            ordersEntity.setNxDoProfitScale("0");
        }

        System.out.println("orderreeeequant" + ordersEntity.getNxDoWeight() + "sutl" + ordersEntity.getNxDoSubtotal());
        ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());

        nxDepartmentOrdersService.update(ordersEntity);

        return R.ok();
    }


    @RequestMapping(value = "/disGetToWeightOrders", method = RequestMethod.POST)
    @ResponseBody
    public R disGetToWeightOrders(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("equalStatus", 0);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        map.put("purGoodsId", -1);
        map.put("weightId", 0);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntitiesWeightun = nxDepartmentOrdersService.queryDepOrdersOrderFatherGoods(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("date", formatWhatDay(0));
        map3.put("type", 1);
        int count = nxDistributerWeightService.queryWeightCountByParams(map3);
        BigDecimal trade = new BigDecimal(count).add(new BigDecimal(1));
        String s = formatDayNumber(0) + "JHD" + trade;

        Map<String, Object> map11 = new HashMap<>();
        map11.put("tradeNo", s);
        map11.put("arr", fatherGoodsEntitiesWeightun);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("depFatherId", depFatherId);
        mapR.put("equalStatus", 0);
        int count1 = nxDistributerWeightService.queryWeightCountByParams(mapR);
        map11.put("weightCount", count1);
        return R.ok().put("data", map11);
    }


    @RequestMapping(value = "/getHaveWeightDepOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getHaveWeightDepOrders(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);

        return R.ok().put("data", ordersEntities);
    }

    /**
     * 9-11
     * DISTRIBUTER
     * 获取需要填写数量和价格的订单
     *
     * @param depFatherId 群id
     * @return 订单
     */
    @RequestMapping(value = "/getToFillDepOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getToFillDepOrders(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {
        System.out.println("getToFillDepOrdersgetToFillDepOrders" + depFatherId + gbDepFatherId + resFatherId + disId);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        System.out.println("abncnncnnnc" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        System.out.println("disnamme" + nxDistributerEntity.getNxDistributerName());
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        List<Map<String, Object>> mapList = new ArrayList<>();

        if (depFatherId != -1) {
            List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
            System.out.println("depdiidid" + entities.size());
            if (entities.size() > 0) {
                for (NxDepartmentEntity dep : entities) {
                    Map<String, Object> mapDep = new HashMap<>();
                    mapDep.put("disId", disId);
                    mapDep.put("depId", dep.getNxDepartmentId());
                    mapDep.put("depName", dep.getNxDepartmentName());
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("disId", disId);
                    map1.put("status", 3);
                    map1.put("depId", dep.getNxDepartmentId());
                    map1.put("orderBy", "time");
                    List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                    mapDep.put("depOrders", depOrders);
                    map1.put("subtotal", 0);
                    System.out.println("map111aaa" + map1);
                    Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                    Double sutotal = 0.0;
                    if (integer > 0) {
                        sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                    }
                    mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                    mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                    mapList.add(mapDep);
                }

                Map<String, Object> mapR = new HashMap<>();
                mapR.put("subAmount", entities.size());
                mapR.put("arr", mapList);
                mapR.put("tradeNo", s);
                double total = 0.0;
                map.put("subtotal", 0);
                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                if (twoTotal > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                }

                mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
                mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

                return R.ok().put("data", mapR);
            } else {
                Map<String, Object> mapR = new HashMap<>();
                double total = 0.0;
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("disId", disId);
                mapS.put("status", 3);
                mapS.put("depFatherId", depFatherId);
                mapS.put("gbDepFatherId", gbDepFatherId);
                mapS.put("resFatherId", resFatherId);
                mapS.put("subtotal", 0);
                System.out.println("tototototdddddddd" + mapS);
                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
                if (twoTotal > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapS);
                }
                mapR.put("subAmount", 0);
                mapR.put("arr", ordersEntities);
                mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
                mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapR.put("tradeNo", s);

                return R.ok().put("data", mapR);
            }


        } else if (gbDepFatherId != -1) {
            List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(gbDepFatherId);
            System.out.println("depdiididgbDepFatherIdgbDepFatherId" + entities.size());
            if (entities.size() > 0) {
                for (GbDepartmentEntity dep : entities) {
                    Map<String, Object> mapDep = new HashMap<>();
                    mapDep.put("disId", disId);
                    mapDep.put("gbDepId", dep.getGbDepartmentId());
                    mapDep.put("depName", dep.getGbDepartmentName());
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("disId", disId);
                    map1.put("status", 3);
                    map1.put("gbDepId", dep.getGbDepartmentId());
                    map1.put("orderBy", "time");
                    List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                    mapDep.put("depOrders", depOrders);
                    map1.put("subtotal", 0);
                    System.out.println("map111aaa" + map1);
                    Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                    Double sutotal = 0.0;
                    if (integer > 0) {
                        sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                    }
                    mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                    mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                    mapList.add(mapDep);
                }

                Map<String, Object> mapR = new HashMap<>();
                mapR.put("subAmount", entities.size());
                mapR.put("arr", mapList);
                mapR.put("tradeNo", s);
                double total = 0.0;
                map.put("subtotal", 0);
                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                if (twoTotal > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                }

                mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
                mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

                return R.ok().put("data", mapR);
            } else {
                Map<String, Object> mapR = new HashMap<>();
                double total = 0.0;
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("disId", disId);
                mapS.put("status", 3);
                mapS.put("depFatherId", depFatherId);
                mapS.put("gbDepFatherId", gbDepFatherId);
                mapS.put("resFatherId", resFatherId);
                mapS.put("subtotal", 0);
                System.out.println("tototototdddddddd" + mapS);
                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
                if (twoTotal > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapS);
                }
                mapR.put("subAmount", 0);
                mapR.put("arr", ordersEntities);
                mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
                mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapR.put("tradeNo", s);

                return R.ok().put("data", mapR);
            }
        }
        return R.ok();
    }

    @RequestMapping(value = "/phoneGetToFillDepOrders", method = RequestMethod.POST)
    @ResponseBody
    public R phoneGetToFillDepOrders(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        System.out.println("abncnncnnnc" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);

        map.put("hasPrice", 1);
        int hasPriceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("hasPrice", null);
        map.put("hasWeight", 1);
        int hasWeightCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        map.put("status", null);
        map.put("equalStatus", 2);
        map.put("dayuPurStatus", 3);
        System.out.println("elqlqlqlql" + map);
        int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        System.out.println("disnamme" + nxDistributerEntity.getNxDistributerName());
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        System.out.println("depdiidid" + entities.size());
        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                mapDep.put("depOrders", depOrders);
                map1.put("subtotal", 0);
                System.out.println("map111aaa" + map1);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                Double sutotal = 0.0;
                if (integer > 0) {
                    sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                }
                mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                mapList.add(mapDep);
            }

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("subAmount", entities.size());
            mapR.put("arr", mapList);
            mapR.put("tradeNo", s);
            double total = 0.0;
            map.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }

            mapR.put("totalCount", ordersEntities.size());
            mapR.put("finishCount", finishCount);
            mapR.put("hasPriceCount", hasPriceCount);
            mapR.put("hasWeightCount", hasWeightCount);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

            return R.ok().put("data", mapR);
        } else {
            Map<String, Object> mapR = new HashMap<>();
            double total = 0.0;
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("status", 3);
            mapS.put("depFatherId", depFatherId);
            mapS.put("gbDepFatherId", gbDepFatherId);
            mapS.put("resFatherId", resFatherId);
            mapS.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapS);
            }
            mapR.put("subAmount", 0);
            mapR.put("arr", ordersEntities);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapR.put("tradeNo", s);
            mapR.put("finishCount", finishCount);
            mapR.put("hasPriceCount", hasPriceCount);
            mapR.put("hasWeightCount", hasWeightCount);
            mapR.put("totalCount", ordersEntities.size());
            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/phoneGetToFillDepOrdersGb", method = RequestMethod.POST)
    @ResponseBody
    public R phoneGetToFillDepOrdersGb(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        System.out.println("abncnncnnnc" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderGb(map);

        map.put("hasPrice", 1);
        System.out.println("hasprice=" + map);
        int hasPriceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("hasPrice", null);
        map.put("hasWeight", 1);
        int hasWeightCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        map.put("dayuPurStatus", 3);
        map.put("hasPrice", 1);
        System.out.println("finfidiididididid" + map);
        int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        System.out.println("disnamme" + nxDistributerEntity.getNxDistributerName());
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(gbDepFatherId);
        System.out.println("depdiidid" + entities.size());
        if (entities.size() > 0) {
            for (GbDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("gbDepId", dep.getGbDepartmentId());
                mapDep.put("depName", dep.getGbDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("gbDepId", dep.getGbDepartmentId());
                map1.put("orderBy", "time");
                map1.put("disId", disId);
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDepWeightOrderGb(map1);
                mapDep.put("depOrders", depOrders);
                map1.put("subtotal", 0);
                System.out.println("map111aaa" + map1);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                Double sutotal = 0.0;
                if (integer > 0) {
                    sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                }
                mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                mapList.add(mapDep);
            }

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("subAmount", entities.size());
            mapR.put("arr", mapList);
            mapR.put("tradeNo", s);
            double total = 0.0;
            map.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }

            mapR.put("totalCount", ordersEntities.size());
            mapR.put("finishCount", finishCount);
            mapR.put("hasPriceCount", hasPriceCount);
            mapR.put("hasWeightCount", hasWeightCount);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

            return R.ok().put("data", mapR);
        } else {
            Map<String, Object> mapR = new HashMap<>();
            double total = 0.0;
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("status", 3);
            mapS.put("depFatherId", depFatherId);
            mapS.put("gbDepFatherId", gbDepFatherId);
            mapS.put("resFatherId", resFatherId);
            mapS.put("subtotal", 0);
            System.out.println("tototototdddddddd" + mapS);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapS);
            }
            mapR.put("subAmount", 0);
            mapR.put("arr", ordersEntities);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapR.put("tradeNo", s);
            mapR.put("finishCount", finishCount);
            mapR.put("hasPriceCount", hasPriceCount);
            mapR.put("hasWeightCount", hasWeightCount);
            mapR.put("totalCount", ordersEntities.size());
            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/printerGetPrintOrders", method = RequestMethod.POST)
    @ResponseBody
    public R printerGetPrintOrders(Integer depFatherId, Integer depId, Integer gbDepFatherId, Integer gbDepId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("equalStatus", 2);
        map.put("depFatherId", depFatherId);
        if (!depFatherId.equals(depId)) {
            map.put("depId", depId);
        }

        map.put("gbDepFatherId", gbDepFatherId);
        if (!gbDepFatherId.equals(gbDepId)) {
            map.put("gbDepId", gbDepId);
        }
        map.put("resFatherId", resFatherId);
        map.put("orderBy", "time");
        System.out.println("mapps" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.disQueryDisOrdersByParams(map);
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        double total = 0.0;
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", ordersEntities);
        mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("tradeNo", s);
        mapR.put("distributer", nxDistributerEntity);

        System.out.println("rmamammam" + mapR);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/getToFillDepOrdersReturn", method = RequestMethod.POST)
    @ResponseBody
    public R getToFillDepOrdersReturn(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("returnStatus", 1);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("returnStatus", 1);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                mapDep.put("depOrders", depOrders);
                System.out.println("map111aaa" + map1);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                Double sutotal = 0.0;
                if (integer > 0) {
                    sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                }
                mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                mapList.add(mapDep);
            }

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("subAmount", entities.size());
            mapR.put("arr", mapList);
            mapR.put("tradeNo", s);
            double total = 0.0;
//            map.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }

            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
            return R.ok().put("data", mapR);
        } else {
            Map<String, Object> mapR = new HashMap<>();
            double total = 0.0;
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("returnStatus", 1);
            mapS.put("depFatherId", depFatherId);
            mapS.put("gbDepFatherId", gbDepFatherId);
            mapS.put("resFatherId", resFatherId);
//            mapS.put("subtotal", 0);
            System.out.println("tototototdddddddd" + mapS);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapS);
            }
            mapR.put("subAmount", 0);
            mapR.put("arr", ordersEntities);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapR.put("tradeNo", s);
            System.out.println("ababdbbdbdbdbdbbdb====" + mapR);
            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/getOrderPageSearch", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPageSearch(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, String searchStr) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        String pinyinString = searchStr;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchPinyin", pinyinString);
        map.put("searchStr", searchStr);
        System.out.println("arrserchas" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderSearch(map);

        return R.ok().put("data", ordersEntities);
    }

    @RequestMapping(value = "/webGetOrderPage", method = RequestMethod.POST)
    @ResponseBody
    public R webGetOrderPage(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId,
                             Integer limit, Integer page, Integer nxDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDisId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
        System.out.println("orsisisisis" + ordersEntities.size());
        //查询列表数据
        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
        double total = 0.0;

        map.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/webGetOrderPageReturn", method = RequestMethod.POST)
    @ResponseBody
    public R webGetOrderPageReturn(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId,
                                   String orderBy, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("returnStatus", 1);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
        System.out.println("orsisisisis" + ordersEntities.size());
        //查询列表数据
        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
        double total = 0.0;

        map.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/webGetSubDepOrderPageSubDep", method = RequestMethod.POST)
    @ResponseBody
    public R webGetSubDepOrderPageSubDep(Integer depFatherId, Integer depId, Integer gbDepId, Integer resFatherId,
                                         String orderBy, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depId", depId);
        map.put("gbDepId", gbDepId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
        System.out.println("orsisisisis" + ordersEntities.size());
        //查询列表数据
        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
        double total = 0.0;

        map.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/webGetSubDepOrderPageSubDepReturn", method = RequestMethod.POST)
    @ResponseBody
    public R webGetSubDepOrderPageSubDepReturn(Integer depFatherId, Integer depId, Integer gbDepFatherId, Integer resFatherId,
                                               String orderBy, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("returnStatus", 1);
        map.put("depId", depId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
        System.out.println("orsisisisis" + ordersEntities.size());
        //查询列表数据
        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
        double total = 0.0;

        map.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/webGetSubDepOrderPageReturn", method = RequestMethod.POST)
    @ResponseBody
    public R webGetSubDepOrderPageReturn(Integer depFatherId, Integer depId, Integer gbDepFatherId, Integer resFatherId,
                                         Integer limit, Integer page) {
        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
        List<Map<String, Object>> retrunList = new ArrayList<>();
        if (departmentEntities.size() > 0) {

            for (NxDepartmentEntity subDep : departmentEntities) {

                Map<String, Object> map = new HashMap<>();
                map.put("returnStatus", 1);
                map.put("depId", subDep.getNxDepartmentId());
                map.put("resFatherId", resFatherId);
                map.put("gbDepFatherId", gbDepFatherId);
                map.put("offset", (page - 1) * limit);
                map.put("limit", limit);
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
                System.out.println("orsisisisis" + ordersEntities.size());

                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                System.out.println("wokkkk" + integer);

                double total = 0.0;
                map.put("offset", null);
                map.put("limit", null);


                map.put("subtotal", 0);
                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                if (twoTotal > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                }
                //查询列表数据

                PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);

                Map<String, Object> mapR = new HashMap<>();
                mapR.put("page", pageUtil);
                mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapR.put("depName", subDep.getNxDepartmentName());

                retrunList.add(mapR);
            }

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("returnStatus", 1);
            map.put("depFatherId", depFatherId);
            map.put("resFatherId", resFatherId);
            map.put("gbDepFatherId", gbDepFatherId);
            map.put("offset", (page - 1) * limit);
            map.put("limit", limit);

            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
            System.out.println("orsisisisis" + ordersEntities.size());
            //查询列表数据
            Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

            PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
            double total = 0.0;
            map.put("offset", null);
            map.put("limit", null);
            map.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }
            Map<String, Object> mapR = new HashMap<>();
            mapR.put("page", pageUtil);
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

            retrunList.add(mapR);
        }

        return R.ok().put("data", retrunList);
    }

    @RequestMapping(value = "/webGetSubDepOrderPage", method = RequestMethod.POST)
    @ResponseBody
    public R webGetSubDepOrderPage(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId,
                                   Integer limit, Integer page, Integer disId) {

        int depAmount = 0;
        List<Map<String, Object>> retrunList = new ArrayList<>();

        if (depFatherId != -1) {
            List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
            if (departmentEntities.size() > 0) {
                depAmount = departmentEntities.size();
                for (NxDepartmentEntity subDep : departmentEntities) {

                    Map<String, Object> map = new HashMap<>();
                    map.put("disId", disId);
                    map.put("status", 3);
                    map.put("depId", subDep.getNxDepartmentId());
                    map.put("resFatherId", resFatherId);
                    map.put("gbDepFatherId", gbDepFatherId);
                    map.put("offset", (page - 1) * limit);
                    map.put("limit", limit);
                    List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
                    System.out.println("orsisisisis" + ordersEntities.size());

                    Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                    System.out.println("wokkkk" + integer);

                    double total = 0.0;
                    map.put("offset", null);
                    map.put("limit", null);


                    map.put("subtotal", 0);
                    Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                    if (twoTotal > 0) {
                        total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                    }
                    //查询列表数据

                    PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);

                    Map<String, Object> mapR = new HashMap<>();
                    mapR.put("page", pageUtil);
                    mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
                    mapR.put("depName", subDep.getNxDepartmentName());

                    retrunList.add(mapR);
                }
            }
        } else {
            if (gbDepFatherId != -1) {
                List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(gbDepFatherId);
                if (departmentEntities.size() > 0) {
                    depAmount = departmentEntities.size();
                    for (GbDepartmentEntity subDep : departmentEntities) {

                        Map<String, Object> map = new HashMap<>();
                        map.put("disId", disId);
                        map.put("status", 3);
                        map.put("depFatherId", depFatherId);
                        map.put("resFatherId", resFatherId);
                        map.put("gbDepId", subDep.getGbDepartmentId());
                        map.put("offset", (page - 1) * limit);
                        map.put("limit", limit);
                        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
                        System.out.println("orsisisisis" + ordersEntities.size());

                        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                        System.out.println("wokkkkGGGGGGG" + integer);

                        double total = 0.0;
                        map.put("offset", null);
                        map.put("limit", null);


                        map.put("subtotal", 0);
                        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                        if (twoTotal > 0) {
                            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                        }
                        //查询列表数据

                        PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
                        Map<String, Object> mapR = new HashMap<>();
                        mapR.put("page", pageUtil);
                        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
                        mapR.put("depName", subDep.getGbDepartmentName());
                        retrunList.add(mapR);
                    }
                }
            }
        }


        if (depAmount == 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", disId);
            map.put("status", 3);
            map.put("depFatherId", depFatherId);
            map.put("resFatherId", resFatherId);
            map.put("gbDepFatherId", gbDepFatherId);
            map.put("offset", (page - 1) * limit);
            map.put("limit", limit);

            System.out.println("orroormriappap" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
            System.out.println("orsisisisis" + ordersEntities.size());
            //查询列表数据
            Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

            PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
            double total = 0.0;
            map.put("offset", null);
            map.put("limit", null);
            map.put("subtotal", 0);
            System.out.println("sussusososossoso" + map);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }
            Map<String, Object> mapR = new HashMap<>();
            mapR.put("page", pageUtil);
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

            retrunList.add(mapR);
        }

        return R.ok().put("data", retrunList);
    }

    @RequestMapping(value = "/getOrderPageToOutWeight", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPageToOutWeight(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, String orderBy) {
        Map<String, Object> mapResult = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("orderBy", orderBy);

        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
        List<Map<String, Object>> list = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                map.put("depId", departmentEntity.getNxDepartmentId());
                map.put("purchaseStatus", 4);
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", departmentEntity.getNxDepartmentId());
                mapDep.put("depName", departmentEntity.getNxDepartmentName());
                mapDep.put("list", ordersEntities);
                list.add(mapDep);
            }

            mapResult.put("arr", list);
            mapResult.put("depHasSubs", departmentEntities.size());
        } else {
            map.put("purchaseStatus", 4);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
            mapResult.put("arr", ordersEntities);
            mapResult.put("depHasSubs", 0);
        }

        mapResult.put("subDep", departmentEntities.size());
        return R.ok().put("data", mapResult);
    }

    @RequestMapping(value = "/getOrderPageToOutWeightGb", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPageToOutWeightGb(Integer disId, Integer gbDepFatherId, Integer resFatherId, String orderBy) {
        Map<String, Object> mapResult = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("disId", disId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("orderBy", orderBy);

        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(gbDepFatherId);
        List<Map<String, Object>> list = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity departmentEntity : departmentEntities) {
                map.put("disId", disId);
                map.put("gbDepId", departmentEntity.getGbDepartmentId());
                map.put("purchaseStatus", 4);
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderGb(map);
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("gbDepId", departmentEntity.getGbDepartmentId());
                mapDep.put("depName", departmentEntity.getGbDepartmentName());
                mapDep.put("list", ordersEntities);
                list.add(mapDep);
            }

            mapResult.put("arr", list);
            mapResult.put("depHasSubs", departmentEntities.size());
        } else {
            map.put("purchaseStatus", 4);
            System.out.println("aodoodododoodododood0000000" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderGb(map);
            mapResult.put("arr", ordersEntities);
            mapResult.put("depHasSubs", 0);
        }

        mapResult.put("subDep", departmentEntities.size());
        return R.ok().put("data", mapResult);
    }

    @RequestMapping(value = "/getOrderPage", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPage(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, String orderBy) {
        Map<String, Object> mapResult = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("orderBy", orderBy);

        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
        List<Map<String, Object>> list = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                map.put("depId", departmentEntity.getNxDepartmentId());
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", departmentEntity.getNxDepartmentId());
                mapDep.put("depName", departmentEntity.getNxDepartmentName());
                mapDep.put("list", ordersEntities);
                list.add(mapDep);
            }
            mapResult.put("arr", list);
            mapResult.put("depHasSubs", departmentEntities.size());
        } else {

            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
            mapResult.put("arr", ordersEntities);
            mapResult.put("depHasSubs", 0);
        }

        mapResult.put("subDep", departmentEntities.size());
        return R.ok().put("data", mapResult);
    }

    @RequestMapping(value = "/getOrderPagePicture", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPagePicture(Integer depFatherId, String orderBy) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        List<NxDepartmentOrdersEntity> ordersEntities = new ArrayList<>();
        if (orderBy.equals("time") || orderBy.equals("sort")) {
            map.put("orderBy", orderBy);
            ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderGb(map);
        }

        return R.ok().put("data", ordersEntities);
    }


    @RequestMapping(value = "/getOrderPageGb", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPageGb(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, String orderBy) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        List<NxDepartmentOrdersEntity> ordersEntities = new ArrayList<>();
        if (orderBy.equals("time") || orderBy.equals("sort")) {
            map.put("orderBy", orderBy);
            System.out.println("oririir" + map);
            ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderGb(map);
        }


        Map<String, Object> mapPPP = new HashMap<>();

        if (orderBy.equals("orderPrice")) {
            Map<String, Object> mapP = new HashMap<>();
            mapP.put("status", 3);
            mapP.put("depFatherId", depFatherId);
            mapP.put("resFatherId", resFatherId);
            mapP.put("gbDepFatherId", gbDepFatherId);
            ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderGb(mapP);

            mapP.put("purType", 0); // stock
            List<NxDepartmentOrdersEntity> ordersEntitiesStock = nxDepartmentOrdersService.queryDepWeightOrderGb(mapP);
            mapP.put("purType", 1);
            mapP.put("inputType", 0);
            List<NxDepartmentOrdersEntity> ordersEntitiesZicai = nxDepartmentOrdersService.queryDepWeightOrderGb(mapP);
            mapP.put("inputType", 1);
            List<NxDepartmentOrdersEntity> ordersEntitiesWx = nxDepartmentOrdersService.queryDepWeightOrderGb(mapP);

            mapPPP.put("stock", ordersEntitiesStock);
            mapPPP.put("zicai", ordersEntitiesZicai);
            mapPPP.put("wx", ordersEntitiesWx);

        }


        Map<String, Object> map2 = new HashMap<>();
        map2.put("status", 3);
        map2.put("depFatherId", depFatherId);
        map2.put("resFatherId", resFatherId);
        map2.put("gbDepFatherId", gbDepFatherId);
        map2.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map2);
        Double total = 0.0;
        Double profitTotal = 0.0;
        String scale = "";
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map2);
//            profitTotal = nxDepartmentOrdersService.queryDepOrdersProfitSubtotal(map2);
            BigDecimal scaleBig = new BigDecimal(profitTotal).divide(new BigDecimal(total), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            scale = scaleBig.toString();
        }

        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("arr", ordersEntities);
        mapResult.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("profit", new BigDecimal(profitTotal).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("scale", scale);


        map.put("hasPrice", 1);
        Integer priceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        mapResult.put("priceCount", priceCount);
        mapResult.put("mapP", mapPPP);

        return R.ok().put("data", mapResult);
    }

    @RequestMapping(value = "/depGetApplyDesk/{depId}")
    @ResponseBody
    public R depGetApplyDesk(@PathVariable Integer depId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depId);
        map3.put("orderBy", "time");
        map3.put("status", 3);
        map3.put("dayuStatus", -1);
        List<NxDepartmentOrdersEntity> ordersEntities3 = nxDepartmentOrdersService.queryDisOrdersByParams(map3);

        return R.ok().put("data", ordersEntities3);
    }


    @RequestMapping(value = "/depGetApplyAi/{depFatherId}")
    @ResponseBody
    public R depGetApplyAi(@PathVariable Integer depFatherId) {

        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        System.out.println("depdiidid" + entities.size());
        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                mapDep.put("depOrders", depOrders);

                Map<String, Object> mapD = new HashMap<>();
                mapD.put("depId", dep.getNxDepartmentId());
                int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);
                mapDep.put("depGoodsCount", i);
                System.out.println("newwnnwenddepdinofoffo" + depFatherId);
                mapDep.put("depInfo", nxDepartmentService.queryDepInfoAll(dep.getNxDepartmentId()));

                mapList.add(mapDep);
            }

            System.out.println("Arekrjekjkre" + mapR);
            mapR.put("arr", mapList);

            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depFatherId);

            if (departmentEntity.getNxDepartmentSettleType() == 0) {
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("depFatherId", depFatherId);
                mapB.put("status", 1);
                System.out.println("debebiiii" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
                if (billEntityList.size() > 0) {
                    mapR.put("bill", billEntityList.get(0));
                } else {
                    mapR.put("bill", -1);
                }
            } else {
                mapR.put("bill", -1);
            }
            return R.ok().put("data", mapR);

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 3);
            map.put("depFatherId", depFatherId);
            System.out.println("abncnncnnnc" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
            Map<String, Object> mapD = new HashMap<>();
            mapD.put("depId", depFatherId);
            int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);

            mapR.put("depGoodsCount", i);
            mapR.put("arr", ordersEntities);
            mapR.put("depInfo", nxDepartmentService.queryDepInfoAll(depFatherId));

            System.out.println("whwhwhwwwwkw");
            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depFatherId);

            if (departmentEntity.getNxDepartmentSettleType() == 0) {
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("depFatherId", depFatherId);
                mapB.put("status", 1);
                System.out.println("debebiiii" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
                if (billEntityList.size() > 0) {
                    mapR.put("bill", billEntityList.get(0));
                } else {
                    mapR.put("bill", -1);
                }
            } else {
                mapR.put("bill", -1);
            }
            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/depGetApplyAiByTime/{depFatherId}")
    @ResponseBody
    public R depGetApplyAiByTime(@PathVariable Integer depFatherId) {

        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        System.out.println("depdiidid" + entities.size());
        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                mapDep.put("depOrders", depOrders);

                Map<String, Object> mapD = new HashMap<>();
                mapD.put("depId", dep.getNxDepartmentId());
                int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);
                mapDep.put("depGoodsCount", i);
                System.out.println("newwnnwenddepdinofoffo" + depFatherId);
                mapDep.put("depInfo", nxDepartmentService.queryDepInfoAll(dep.getNxDepartmentId()));

                mapList.add(mapDep);
            }

            System.out.println("Arekrjekjkre" + mapR);
            mapR.put("arr", mapList);

            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depFatherId);

            if (departmentEntity.getNxDepartmentSettleType() == 0) {
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("depFatherId", depFatherId);
                mapB.put("status", 1);
                System.out.println("debebiiii" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
                if (billEntityList.size() > 0) {
                    mapR.put("bill", billEntityList.get(0));
                } else {
                    mapR.put("bill", -1);
                }
            } else {
                mapR.put("bill", -1);
            }
            return R.ok().put("data", mapR);

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 3);
            map.put("depFatherId", depFatherId);
            System.out.println("abncnncnnnc" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
            Map<String, Object> mapD = new HashMap<>();
            mapD.put("depId", depFatherId);
            int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);

            mapR.put("depGoodsCount", i);
            mapR.put("arr", ordersEntities);
            mapR.put("depInfo", nxDepartmentService.queryDepInfoAll(depFatherId));

            System.out.println("whwhwhwwwwkw");
            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depFatherId);

            if (departmentEntity.getNxDepartmentSettleType() == 0) {
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("depFatherId", depFatherId);
                mapB.put("status", 1);
                System.out.println("debebiiii" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
                if (billEntityList.size() > 0) {
                    mapR.put("bill", billEntityList.get(0));
                } else {
                    mapR.put("bill", -1);
                }
            } else {
                mapR.put("bill", -1);
            }
            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/depSearchTodayOrders", method = RequestMethod.POST)
    @ResponseBody
    public R depSearchTodayOrders(Integer depFatherId, Integer depId, String searchStr) {

//        Map<String, Object> map = new HashMap<>();
//
//        if(depFatherId != -1){
//            map.put("depFatherId", depFatherId);
//        }
//        if(depId != -1){
//            map.put("depId", depId);
//        }
//
//        map.put("searchStr", searchStr);
//        String pinyinString ="";
//        for (int i = 0; i < searchStr.length(); i++) {
//            String str = searchStr.substring(i, i + 1);
//            if (str.matches("[\u4E00-\u9FFF]")) {
//                pinyinString = hanziToPinyin(searchStr);
//            }
//        }
//        map.put("searchPinyin", pinyinString);
        Map<String, Object> map = new HashMap<>();

        if (depFatherId != -1) {
            map.put("depFatherId", depFatherId);
        }
        if (depId != -1) {
            map.put("depId", depId);
        }


        String pinyinString = "";
        boolean hasChinese = false;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                hasChinese = true;
                break;
            }
        }
        if (hasChinese) {
            pinyinString = hanziToPinyin(searchStr);

        } else {
            // 全是拼音或英文
            pinyinString = searchStr;
            searchStr = null;
        }
        map.put("searchPinyin", pinyinString);
        map.put("searchStr", searchStr);

        System.out.println("searchchhchc1111" + map);
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentOrdersService.queryDepWeightOrderSearch(map);
        System.out.println("orarrrr" + nxDepartmentOrdersEntities.size());
        return R.ok().put("data", nxDepartmentOrdersEntities);
    }

    @RequestMapping(value = "/depGetApplyAiFather/{depFatherId}")
    @ResponseBody
    public R depGetApplyAiFather(@PathVariable Integer depFatherId) {

        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        System.out.println("depdiidid" + entities.size());
        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDistributerFatherGoodsEntity> nxDistributerFatherGoodsEntities = nxDepartmentOrdersService.queryGrandGoodsOrder(map1);
                mapDep.put("depOrders", nxDistributerFatherGoodsEntities);

                Map<String, Object> mapD = new HashMap<>();
                mapD.put("depId", dep.getNxDepartmentId());
                int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);
                mapDep.put("depGoodsCount", i);
                System.out.println("newwnnwenddepdinofoffo" + depFatherId);
                mapDep.put("depInfo", nxDepartmentService.queryDepInfoAll(dep.getNxDepartmentId()));

                mapList.add(mapDep);
            }

            System.out.println("Arekrjekjkre" + mapR);
            mapR.put("arr", mapList);

            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depFatherId);

            if (departmentEntity.getNxDepartmentSettleType() == 0) {
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("depFatherId", depFatherId);
                mapB.put("status", 1);
                System.out.println("debebiiii" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
                if (billEntityList.size() > 0) {
                    mapR.put("bill", billEntityList.get(0));
                } else {
                    mapR.put("bill", -1);
                }
            } else {
                mapR.put("bill", -1);
            }
            return R.ok().put("data", mapR);

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 3);
            map.put("depFatherId", depFatherId);
//            Map<String, Object> mapD = new HashMap<>();
//            mapD.put("depId", depFatherId);
//            int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);
//
//            mapR.put("depGoodsCount", i);
            List<NxDistributerFatherGoodsEntity> nxDistributerFatherGoodsEntities = nxDepartmentOrdersService.queryGrandGoodsOrder(map);

            mapR.put("arr", nxDistributerFatherGoodsEntities);
            mapR.put("depInfo", nxDepartmentService.queryDepInfoAll(depFatherId));

            System.out.println("whwhwhwwwwkw");
            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depFatherId);

            if (departmentEntity.getNxDepartmentSettleType() == 0) {
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("depFatherId", depFatherId);
                mapB.put("status", 1);
                System.out.println("debebiiii" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
                if (billEntityList.size() > 0) {
                    mapR.put("bill", billEntityList.get(0));
                } else {
                    mapR.put("bill", -1);
                }
            } else {
                mapR.put("bill", -1);
            }
            return R.ok().put("data", mapR);
        }
    }


    @RequestMapping(value = "/subDepGetApplyAi/{depId}")
    @ResponseBody
    public R subDepGetApplyAi(@PathVariable Integer depId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depId", depId);
        System.out.println("abncnncnnncSubsusb" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
        Map<String, Object> mapD = new HashMap<>();
        mapD.put("depId", depId);
        int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);

        mapR.put("depGoodsCount", i);
        mapR.put("arr", ordersEntities);
        mapR.put("depInfo", nxDepartmentService.queryDepInfoAll(depId));

        System.out.println("whwhwhwwwwkw");
        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);

        if (departmentEntity.getNxDepartmentSettleType() == 0) {
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("depFatherId", departmentEntity.getNxDepartmentFatherId());
            mapB.put("status", 1);
            System.out.println("debebiiii" + mapB);
            List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
            if (billEntityList.size() > 0) {
                mapR.put("bill", billEntityList.get(0));
            } else {
                mapR.put("bill", -1);
            }
        } else {
            mapR.put("bill", -1);
        }
        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/subDepGetApplyAiByTime/{depId}")
    @ResponseBody
    public R subDepGetApplyAiByTime(@PathVariable Integer depId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depId", depId);
        System.out.println("abncnncnnncSubsusb" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
        Map<String, Object> mapD = new HashMap<>();
        mapD.put("depId", depId);
        int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);

        mapR.put("depGoodsCount", i);
        mapR.put("arr", ordersEntities);
        mapR.put("depInfo", nxDepartmentService.queryDepInfoAll(depId));

        System.out.println("whwhwhwwwwkw");
        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);

        if (departmentEntity.getNxDepartmentSettleType() == 0) {
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("depFatherId", departmentEntity.getNxDepartmentFatherId());
            mapB.put("status", 1);
            System.out.println("debebiiii" + mapB);
            List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
            if (billEntityList.size() > 0) {
                mapR.put("bill", billEntityList.get(0));
            } else {
                mapR.put("bill", -1);
            }
        } else {
            mapR.put("bill", -1);
        }
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/subDepGetApplyAiFather/{depId}")
    @ResponseBody
    public R subDepGetApplyAiFather(@PathVariable Integer depId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depId", depId);

        List<NxDistributerFatherGoodsEntity> nxDistributerFatherGoodsEntities = nxDepartmentOrdersService.queryGrandGoodsOrder(map);

        mapR.put("arr", nxDistributerFatherGoodsEntities);
        mapR.put("depInfo", nxDepartmentService.queryDepInfoAll(depId));

        System.out.println("whwhwhwwwwkw");
        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);

        if (departmentEntity.getNxDepartmentSettleType() == 0) {
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("depFatherId", departmentEntity.getNxDepartmentFatherId());
            mapB.put("status", 1);
            System.out.println("debebiiii" + mapB);
            List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
            if (billEntityList.size() > 0) {
                mapR.put("bill", billEntityList.get(0));
            } else {
                mapR.put("bill", -1);
            }
        } else {
            mapR.put("bill", -1);
        }
        return R.ok().put("data", mapR);

    }


    /**
     * dh
     *
     * @param depId
     * @return
     */
    @RequestMapping(value = "/depGetApply/{depId}")
    @ResponseBody
    public R depGetApply(@PathVariable Integer depId) {

        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depId);
        map3.put("orderBy", "time");
        map3.put("status", 3);
//        map3.put("purStatus", 4);
        map3.put("dayuStatus", -1);
        List<NxDepartmentOrdersEntity> ordersEntities3 = nxDepartmentOrdersService.queryDisOrdersByParams(map3);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", ordersEntities3);
        mapR.put("depInfo", nxDepartmentService.queryObject(depId));

        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);

        if (departmentEntity.getNxDepartmentSettleType() == 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("depId", depId);
            map.put("status", 1);
            List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(map);
            if (billEntityList.size() > 0) {
                mapR.put("bill", billEntityList.get(0));
            } else {
                mapR.put("bill", -1);
            }
        } else {
            mapR.put("bill", -1);
        }
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/printerNxDisGetTodayOrderCustomer/{disId}")
    @ResponseBody
    public R printerNxDisGetTodayOrderCustomer(@PathVariable Integer disId) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("equalStatus", 2);
        System.out.println("dapapfpa" + map1);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);
        List<NxDepartmentEntity> resultData = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                map1.put("status", null);
                map1.put("equalStatus", 2);
                map1.put("depFatherId", departmentEntity.getNxDepartmentId());
                Integer finish = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                map1.put("equalStatus", null);
                map1.put("status", 3);
//                System.out.println("dkdfajdlfalfdz" +  map1);

                Integer total = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                System.out.println("dkdfajdlfalfdzininnininin" + total + "finsi=" + finish);
                if (total == finish) {
                    System.out.println("dkdfajdlfalfdzininnininin" + total + "finsi=" + finish);
                    resultData.add(departmentEntity);
                }
            }
        }
        return R.ok().put("data", resultData);

    }

    @RequestMapping(value = "/webNxDisGetTodayOrderCustomer/{disId}")
    @ResponseBody
    public R webNxDisGetTodayOrderCustomer(@PathVariable String disId) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("equalStatus", 2);
        map1.put("dayuPurStatus", 3);
        System.out.println("dapapfpa" + map1);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);
        System.out.println("eneneeneisisisisisiss" + departmentEntities.size());
        List<NxDepartmentEntity> result = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                Integer departmentId = departmentEntity.getNxDepartmentId();
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depFatherId", departmentId);
                mapDep.put("status", 3);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                mapDep.put("equalStatus", 2);
                mapDep.put("dayuPurStatus", 3);
                System.out.println("depmapappaaforoofofofo" + mapDep);
                Integer integerFinish = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                System.out.println("reususlslt" + departmentEntity.getNxDepartmentAttrName() + "d" + integer + "fins" + integerFinish);

                if (integer.equals(integerFinish) && integer > 0) {

                    if (departmentEntity.getNxDepartmentEntities().size() > 0) {
                        System.out.println("susususuuususuususokkokkkk" + departmentEntity.getNxDepartmentName());
                        List<NxDepartmentEntity> subList = new ArrayList<>();
                        for (NxDepartmentEntity sub : departmentEntity.getNxDepartmentEntities()) {

                            mapDep.put("depFatherId", departmentId);
                            mapDep.put("depId", sub.getNxDepartmentId());
                            mapDep.put("equalStatus", 2);
                            mapDep.put("dayuPurStatus", 3);
                            System.out.println("depmapappaasusuusussuusu" + mapDep);
                            System.out.println("depmapappaasusuusussuusu" + sub.getNxDepartmentName());
                            Integer integerFinishsub = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                            if (integerFinishsub > 0) {
                                subList.add(sub);
                                System.out.println("sulisiis.elel" + subList.size());
                            }
                        }
                        departmentEntity.setNxDepartmentEntities(subList);
                        result.add(departmentEntity);

                    } else {
                        result.add(departmentEntity);
                    }

                }
            }
        }


        System.out.println("gbbgbgbgbgbgbgbgbbgbgbbgnaaa" + map1);
//        List<GbDepartmentEntity> gbDepartmentEntityList = nxDepartmentOrdersService.queryqueryOrderGbDepartmentList(map1);
//        System.out.println("eneneeneisisisisisissGGGGGGGGGG" + gbDepartmentEntityList.size());
//        List<GbDepartmentEntity> resultGb = new ArrayList<>();
//        if (gbDepartmentEntityList.size() > 0) {
//            for (GbDepartmentEntity departmentEntity : gbDepartmentEntityList) {
//                Integer departmentId = departmentEntity.getGbDepartmentId();
//                Map<String, Object> mapDep = new HashMap<>();
//                mapDep.put("gbDepFatherId", departmentId);
//                mapDep.put("status", 3);
//                System.out.println("whsnsussususlsllslslslss" + mapDep);
//                Integer integer = nxDepartmentOrdersService.queryTotalByParams(mapDep);
//                mapDep.put("equalStatus", 2);
//                mapDep.put("status", null);
//                mapDep.put("dayuPurStatus", 3);
//                System.out.println("depmapappaaforoofofofoGGGGGGG" + mapDep);
//                Integer integerFinish = nxDepartmentOrdersService.queryTotalByParams(mapDep);
//                System.out.println("reususlslt" + departmentEntity.getGbDepartmentAttrName() + "d" + integer + "fins" + integerFinish);
//
//                if (integer.equals(integerFinish) && integer > 0) {
//
//                    if (departmentEntity.getGbDepartmentEntityList().size() > 0) {
//                        System.out.println("susususuuususuususokkokkkk" + departmentEntity.getGbDepartmentName());
//                        List<GbDepartmentEntity> subList = new ArrayList<>();
//                        for (GbDepartmentEntity sub : departmentEntity.getGbDepartmentEntityList()) {
//
//                            mapDep.put("gbDepFatherId", departmentId);
//                            mapDep.put("gbDepId", sub.getGbDepartmentId());
//                            mapDep.put("equalStatus", 2);
//                            mapDep.put("dayuPurStatus", 3);
//                            System.out.println("depmapappaasusuusussuusu" + mapDep);
//                            System.out.println("depmapappaasusuusussuusu" + sub.getGbDepartmentName());
//                            Integer integerFinishsub = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
//                            if (integerFinishsub > 0) {
//                                subList.add(sub);
//                                System.out.println("sulisiis.elel" + subList.size());
//                            }
//                        }
//                        departmentEntity.setGbDepartmentEntityList(subList);
//                        resultGb.add(departmentEntity);
//
//                    } else {
//                        resultGb.add(departmentEntity);
//                    }
//
//                }
//            }
//        }


        //gbBatch
        //GbData
        Map<String, Object> mapPB = new HashMap<>();
        mapPB.put("nxDisId", disId);
        mapPB.put("equalStatus", 1);
        System.out.println("enennenenenne" + mapPB);
        List<GbDistributerPurchaseBatchEntity> entities = gbDistributerPurchaseBatchService.queryDisPurchaseBatch(mapPB);

        Map<String, Object> map = new HashMap<>();
        map.put("nxArr", result);
        map.put("gbArr", new ArrayList<>());
        map.put("gbBatchArr", entities);

        return R.ok().put("data", map);

    }


    @RequestMapping(value = "/webNxDisGetTodayReturnCustomer/{disId}")
    @ResponseBody
    public R webNxDisGetTodayReturnCustomer(@PathVariable Integer disId) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("returnStatus", 1);
        System.out.println("dapapfpa" + map1);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);

        return R.ok().put("data", departmentEntities);

    }


    //
    @RequestMapping(value = "/disGetTodayOrderCustomer/{disId}")
    @ResponseBody
    public R disGetTodayOrderCustomer(@PathVariable Integer disId) {
        Map<String, Object> returnData = new HashMap<>();
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);
        System.out.println("mapapapapapddiidididi" + map1);
        List<GbDistributerEntity> distributerEntitiesAA = nxDepartmentOrdersService.queryOrderGbDistributerList(map1);
        System.out.println("gbbgbgbgbbgbbgbgb" + distributerEntitiesAA.size());
        Map<String, Object> mapData = new HashMap<>();
        List<Map<String, Object>> resultNx = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            Integer hasPrice = 0;
            Integer hasWeight = 0;
            Integer unDo = 0;

            System.out.println("depsisiisisisiiis" + departmentEntities.size());
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                Map<String, Object> map7 = new HashMap<>();
                map7.put("depFatherId", departmentEntity.getNxDepartmentId());
                map7.put("status", -1);
                unDo = nxDepartmentOrdersService.queryDepOrdersAcount(map7);

                map7.put("status", 3);
                map7.put("hasPrice", 1);
                System.out.println("kkkkkkkkkkkkkk" + map7);
                hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(map7);

                // weigth
                Map<String, Object> map8 = new HashMap<>();
                map8.put("depFatherId", departmentEntity.getNxDepartmentId());
                map8.put("status", 3);
                map8.put("dayuPurStatus", 3);
                hasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(map8);

                Map<String, Object> map2 = new HashMap<>();
                map2.put("depFatherId", departmentEntity.getNxDepartmentId());
                map2.put("status", 3);
                Integer totalCount = nxDepartmentOrdersService.queryDepOrdersAcount(map2);
                map2.put("status", null);
                map2.put("dayuPurStatus", 3);
                map2.put("equalStatus", 2);
                map2.put("subtotal", 0);
                map2.put("hasPrice", 1);
                System.out.println("fiisisiisisimap" + map2);
                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map2);
                Double total = 0.0;
                if (twoTotal > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map2);
                }



                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("dep", departmentEntity);
                mapDep.put("totalCount", totalCount);
                mapDep.put("finishCount", twoTotal);
                mapDep.put("hasPrice", hasPrice);
                mapDep.put("hasWeight", hasWeight);
                mapDep.put("unDo", unDo);



                mapDep.put("twoSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                resultNx.add(mapDep);

            }
            mapData.put("nxDep", resultNx);
        } else {
            mapData.put("nxDep", new ArrayList<>());
        }

        List<Map<String, Object>> gbList = new ArrayList<>();

        if (distributerEntitiesAA.size() > 0) {

            for (GbDistributerEntity gbDis : distributerEntitiesAA) {
                if (gbDis.getGbDistributerBusinessType() > 0) {
                    List<Map<String, Object>> gbDisDepArrMap = new ArrayList<>();

                    Map<String, Object> mapDis = new HashMap<>();
                    mapDis.put("dis", gbDis);
                    mapDis.put("gbDisId", gbDis.getGbDistributerId());
                    Map<String, Object> mapOrderDep = new HashMap<>();
                    mapOrderDep.put("gbDisId", gbDis.getGbDistributerId());
                    mapOrderDep.put("disId", disId);
                    System.out.println("gbdidisiisisismap==" + mapOrderDep);
                    List<GbDepartmentEntity> gbpartmentEntities = nxDepartmentOrdersService.queryOrderGbDepartmentList(mapOrderDep);

                    Integer newOrder = 0;
                    Integer hasNotWeight = 0;
                    Integer jinhuoOrder = 0;
                    Integer jinhuoHasWeight = 0;
                    Integer jinhuoFinished = 0;
                    Integer chukuOrder = 0;
                    Integer chukuHasWeight = 0;

                    Integer hasPrice = 0;
                    Integer hasNotPrice = 0;

                    System.out.println("gbeenesisi" + gbpartmentEntities.size());
                    for (GbDepartmentEntity gbDepartmentEntity : gbpartmentEntities) {
                        //new
                        Map<String, Object> mapOrder = new HashMap<>();
                        mapOrder.put("gbDepId", gbDepartmentEntity.getGbDepartmentId());
                        mapOrder.put("status", 3);
//                    mapOrder.put("purGoodsId", 0);
                        System.out.println("gbnNewworororooror" + mapOrder);
                        newOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);

                        if (newOrder > 0) {
                            //采购进货-jinhuo
                            mapOrder.put("purGoodsId", 1);
                            jinhuoOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
                            //出库 -chuku
                            mapOrder.put("purGoodsId", -1);
                            chukuOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);

                            // --weight
                            Map<String, Object> mapWeight = new HashMap<>();
                            mapWeight.put("gbDepId", gbDepartmentEntity.getGbDepartmentId());
                            mapWeight.put("hasWeight", -1);
                            mapWeight.put("status", 3);

                            System.out.println("jinhuowhawwwrrrrrrggggg" + mapWeight);
                            hasNotWeight = nxDepartmentOrdersService.queryDepOrdersAcount(mapWeight);

                            mapWeight.put("hasWeight", 1);
                            mapWeight.put("purGoodsId", 1);
                            jinhuoHasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(mapWeight);
                            mapWeight.put("purGoodsId", -1);
                            chukuHasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(mapWeight);

                            // price
                            Map<String, Object> map7 = new HashMap<>();
                            map7.put("gbDepId", gbDepartmentEntity.getGbDepartmentId());
                            map7.put("status", 3);
                            map7.put("hasPrice", 1);
                            hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(map7);
                            map7.put("hasPrice", -1);
                            hasNotPrice = nxDepartmentOrdersService.queryDepOrdersAcount(map7);

                            Map<String, Object> map2 = new HashMap<>();
                            map2.put("gbDepId", gbDepartmentEntity.getGbDepartmentId());
                            map2.put("equalStatus", 2);
                            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map2);
                            Double total = 0.0;
                            if (twoTotal > 0) {
                                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map2);
                            }


                            Map<String, Object> mapDep = new HashMap<>();
                            mapDep.put("gbDep", gbDepartmentEntity);
                            mapDep.put("newOrder", newOrder);
                            mapDep.put("hasNotWeight", hasNotWeight);
                            mapDep.put("jinhuoOrder", jinhuoOrder);
                            mapDep.put("jinhuoHasWeight", jinhuoHasWeight);
                            mapDep.put("jinhuoFinished", jinhuoFinished);

                            mapDep.put("chukuOrder", chukuOrder);
                            mapDep.put("chukuHasWeight", chukuHasWeight);
                            mapDep.put("chukuFinished", jinhuoFinished);

                            mapDep.put("hasPrice", hasPrice);
                            mapDep.put("hasNotPrice", hasNotPrice);

                            mapDep.put("twoSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            gbDisDepArrMap.add(mapDep);

                        }

                    }

                    mapDis.put("arr", gbDisDepArrMap);
                    gbList.add(mapDis);
                }
            }

            mapData.put("gbDisArrApp", gbList);

        } else {
            mapData.put("gbDisArrApp", new ArrayList<>());
        }


        System.out.println("gbDisArrapp" + mapData);


        //GbData
        Map<String, Object> mapPB = new HashMap<>();
        mapPB.put("nxDisId", disId);
        mapPB.put("status", 3);
        System.out.println("enennenenenne" + mapPB);
        List<GbDistributerPurchaseBatchEntity> entities = gbDistributerPurchaseBatchService.queryDisPurchaseBatch(mapPB);

        if (entities.size() > 0) {

            for (GbDistributerPurchaseBatchEntity batchEntity : entities) {

                Map<String, Object> mapDis = new HashMap<>();
                mapDis.put("nxDisId", disId);
                mapDis.put("disId", batchEntity.getGbDpbDistributerId());
                mapDis.put("status", 3);
                System.out.println("whwhwhwhhwhwhwhpururur000" + mapDis);
                Integer goodsCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapDis);
                mapDis.put("hasPrice", -1);
                System.out.println("whwhwhwhhwhwhwhpururur111" + mapDis);
                Integer hasNotPrice = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapDis);
                mapDis.put("hasPrice", 1);
                System.out.println("whwhwhwhhwhwhwhpururur222" + mapDis);
                Integer hasPriceCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapDis);

            }

            mapData.put("gbDisArr", entities);

        } else {
            mapData.put("gbDisArr", new ArrayList<>());
        }

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("status", 3);
        map3.put("purStatus", 4);
        Integer preOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Integer buyOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Map<String, Object> map3Ok = new HashMap<>();
        map3Ok.put("disId", disId);
        map3Ok.put("equalStatus", 2);
        Integer buyOrdersOk = nxDepartmentOrdersService.queryDepOrdersAcount(map3Ok);
        returnData.put("buyOrders", buyOrders);
        returnData.put("buyOrdersOk", buyOrdersOk);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 2);
        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        int wxCountPur = wxCount + wxCountAuto;

        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        System.out.println("mapokk" + mapOk);
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        //订货完成
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        int wxCountOkAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        int wxCountPurOk = wxCountOk + wxCountOkAuto;
        Map<String, Object> mapB = new HashMap<>();
        mapB.put("disId", disId);
        mapB.put("settleType", 0);
        mapB.put("equalStatus", 0);
        int unPayCount = nxDepartmentBillService.queryBillsCount(mapB);

        //return
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("equalStatus", -1);
        System.out.println("rututtntnmaappa" + map);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryReturnBill(map);


        Map<String, Object> mapGb = new HashMap<>();
        mapGb.put("disId", disId);
        mapGb.put("gbDepFatherIdNotEqual", -1);
        mapGb.put("status", 3);
        System.out.println("usnbdidiid" + mapGb);
        int i = nxDepartmentBillService.queryBillsCount(mapGb);


        Map<String, Object> map7 = new HashMap<>();
        map7.put("disId", disId);
        map7.put("status", -1);
        int unDoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map7);


        returnData.put("stockCount", stockCount);
        returnData.put("stockCountOk", stockCountOK);
        returnData.put("wxCount", wxCountPur);
        returnData.put("wxCountOk", wxCountPurOk);
        returnData.put("unPayCount", unPayCount);
        returnData.put("preOrders", preOrders);


        List<NxGoodsEntity> books = nxGoodsService.queryNumberGoods();
        returnData.put("deps", mapData);
        returnData.put("disInfo", nxDistributerService.queryObject(disId));
        returnData.put("books", books);
        returnData.put("returnList", billEntityList);
        returnData.put("unPayGbBills", i);
        returnData.put("unDoTotal", unDoTotal);
        return R.ok().put("data", returnData);

    }


    /**
     * 获取今日订单客户信息
     */
    @RequestMapping(value = "/disGetTodayOrderCustomer2/{disId}")
    @ResponseBody
    public R disGetTodayOrderCustomer2(@PathVariable Integer disId) {
        try {
            Map<String, Object> returnData = new HashMap<>();
            Map<String, Object> mapData = new HashMap<>();

            // 1. 获取部门数据
            Map<String, Object> params = new HashMap<>();
            params.put("disId", disId);
            params.put("stasts", 3);
            List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(params);
            List<GbDistributerEntity> distributerEntitiesAA = nxDepartmentOrdersService.queryOrderGbDistributerList(params);

            // 2. 处理部门订单统计
            List<Map<String, Object>> resultNx = new ArrayList<>();
            if (!departmentEntities.isEmpty()) {
                for (NxDepartmentEntity department : departmentEntities) {
                    Map<String, Object> depStats = new HashMap<>();
                    depStats.put("dep", department);

                    // 获取价格统计
                    Map<String, Object> priceParams = new HashMap<>();
                    priceParams.put("depFatherId", department.getNxDepartmentId());
                    priceParams.put("status", 3);
                    priceParams.put("hasPrice", 1);
                    Integer hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(priceParams);

                    // 获取重量统计
                    Map<String, Object> weightParams = new HashMap<>();
                    weightParams.put("depFatherId", department.getNxDepartmentId());
                    weightParams.put("status", 3);
                    weightParams.put("dayuPurStatus", 3);
                    Integer hasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(weightParams);

                    // 获取总数统计
                    Map<String, Object> totalParams = new HashMap<>();
                    totalParams.put("depFatherId", department.getNxDepartmentId());
                    totalParams.put("status", 3);
                    Integer totalCount = nxDepartmentOrdersService.queryDepOrdersAcount(totalParams);

                    // 获取完成订单统计
                    totalParams.put("status", null);
                    totalParams.put("dayuPurStatus", 3);
                    totalParams.put("equalStatus", 2);
                    totalParams.put("subtotal", 0);
                    Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(totalParams);

                    // 计算总金额
                    Double total = 0.0;
                    if (twoTotal > 0) {
                        total = nxDepartmentOrdersService.queryDepOrdersSubtotal(totalParams);
                    }

                    // 组装部门统计数据
                    depStats.put("totalCount", totalCount);
                    depStats.put("finishCount", twoTotal);
                    depStats.put("hasPrice", hasPrice);
                    depStats.put("hasWeight", hasWeight);
                    depStats.put("twoSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    resultNx.add(depStats);
                }
                mapData.put("nxDep", resultNx);
            } else {
                mapData.put("nxDep", new ArrayList<>());
            }

            // 3. 处理国标部门订单统计
            List<Map<String, Object>> gbList = new ArrayList<>();
            if (!distributerEntitiesAA.isEmpty()) {
                for (GbDistributerEntity gbDis : distributerEntitiesAA) {
                    if (gbDis.getGbDistributerBusinessType() > 0) {
                        Map<String, Object> gbDisStats = new HashMap<>();
                        gbDisStats.put("dis", gbDis);
                        gbDisStats.put("gbDisId", gbDis.getGbDistributerId());

                        // 获取国标部门列表
                        Map<String, Object> gbDepParams = new HashMap<>();
                        gbDepParams.put("gbDisId", gbDis.getGbDistributerId());
                        gbDepParams.put("disId", disId);
                        List<GbDepartmentEntity> gbDepartments = nxDepartmentOrdersService.queryOrderGbDepartmentList(gbDepParams);

                        List<Map<String, Object>> gbDisDepArrMap = new ArrayList<>();
                        for (GbDepartmentEntity gbDep : gbDepartments) {
                            Map<String, Object> gbDepStats = processGbDepartmentStats(gbDep);
                            if (gbDepStats != null) {
                                gbDisDepArrMap.add(gbDepStats);
                            }
                        }

                        gbDisStats.put("arr", gbDisDepArrMap);
                        gbList.add(gbDisStats);
                    }
                }
                mapData.put("gbDisArrApp", gbList);
            } else {
                mapData.put("gbDisArrApp", new ArrayList<>());
            }

            // 4. 处理采购批次统计
            Map<String, Object> batchParams = new HashMap<>();
            batchParams.put("nxDisId", disId);
            batchParams.put("status", 3);
            List<GbDistributerPurchaseBatchEntity> batchEntities = gbDistributerPurchaseBatchService.queryDisPurchaseBatch(batchParams);
            mapData.put("gbDisArr", batchEntities.isEmpty() ? new ArrayList<>() : batchEntities);

            // 5. 获取订单统计
            Map<String, Object> orderStats = nxDepartmentOrdersService.getOrderStats(disId);
            returnData.putAll(orderStats);

            // 6. 获取账单统计
            Map<String, Object> billStats = nxDepartmentBillService.getBillStats(disId);
            returnData.putAll(billStats);

            // 7. 获取其他数据
            returnData.put("deps", mapData);
            returnData.put("disInfo", nxDistributerService.queryObject(disId));
            returnData.put("books", nxGoodsService.queryNumberGoods());

            return R.ok().put("data", returnData);

        } catch (Exception e) {
            System.out.println("获取今日订单客户信息失败" + e);
            return R.error("获取今日订单客户信息失败: " + e.getMessage());
        }
    }

    /**
     * 处理国标部门统计数据
     */
    private Map<String, Object> processGbDepartmentStats(GbDepartmentEntity gbDep) {
        Map<String, Object> mapOrder = new HashMap<>();
        mapOrder.put("gbDepId", gbDep.getGbDepartmentId());
        mapOrder.put("status", 3);

        Integer newOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
        if (newOrder <= 0) {
            return null;
        }

        Map<String, Object> gbDepStats = new HashMap<>();
        gbDepStats.put("gbDep", gbDep);
        gbDepStats.put("newOrder", newOrder);

        // 获取进货订单统计
        mapOrder.put("purGoodsId", 1);
        Integer jinhuoOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
        gbDepStats.put("jinhuoOrder", jinhuoOrder);

        // 获取出库订单统计
        mapOrder.put("purGoodsId", -1);
        Integer chukuOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
        gbDepStats.put("chukuOrder", chukuOrder);

        // 获取重量统计
        Map<String, Object> weightParams = new HashMap<>();
        weightParams.put("gbDepId", gbDep.getGbDepartmentId());
        weightParams.put("status", 3);

        weightParams.put("hasWeight", -1);
        Integer hasNotWeight = nxDepartmentOrdersService.queryDepOrdersAcount(weightParams);
        gbDepStats.put("hasNotWeight", hasNotWeight);

        weightParams.put("hasWeight", 1);
        weightParams.put("purGoodsId", 1);
        Integer jinhuoHasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(weightParams);
        gbDepStats.put("jinhuoHasWeight", jinhuoHasWeight);

        weightParams.put("purGoodsId", -1);
        Integer chukuHasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(weightParams);
        gbDepStats.put("chukuHasWeight", chukuHasWeight);

        // 获取价格统计
        Map<String, Object> priceParams = new HashMap<>();
        priceParams.put("gbDepId", gbDep.getGbDepartmentId());
        priceParams.put("status", 3);

        priceParams.put("hasPrice", 1);
        Integer hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(priceParams);
        gbDepStats.put("hasPrice", hasPrice);

        priceParams.put("hasPrice", -1);
        Integer hasNotPrice = nxDepartmentOrdersService.queryDepOrdersAcount(priceParams);
        gbDepStats.put("hasNotPrice", hasNotPrice);

        // 获取完成订单统计
        Map<String, Object> finishParams = new HashMap<>();
        finishParams.put("gbDepId", gbDep.getGbDepartmentId());
        finishParams.put("equalStatus", 2);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(finishParams);

        // 计算总金额
        Double total = 0.0;
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(finishParams);
        }
        gbDepStats.put("twoSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

        return gbDepStats;
    }


    @RequestMapping(value = "/getBooks")
    @ResponseBody
    public R getBooks() {
        List<NxGoodsEntity> books = nxGoodsService.queryNumberGoods();

        System.out.println("getBooksgetBooksgetBooks");
        return R.ok().put("data", books);
    }


    @RequestMapping(value = "/nxDisGetGbBatchOrders/{id}")
    @ResponseBody
    public R nxDisGetGbBatchOrders(@PathVariable Integer id) {
        GbDistributerPurchaseBatchEntity batchEntity = gbDistributerPurchaseBatchService.queryObject(id);

        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByBatchId(id);
        List<NxDepartmentOrdersEntity> list = new ArrayList<>();

        if (purchaseGoodsEntities.size() > 0) {
            for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                Integer purchaseGoodsId = purchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", purchaseGoodsId);
                List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
                if (gbDepartmentOrdersEntities.size() > 0) {
                    for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
                        Integer gbDoNxDepartmentOrderId = gbDepartmentOrdersEntity.getGbDoNxDepartmentOrderId();
                        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
                        list.add(ordersEntity);
                    }
                }
            }
        }
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(batchEntity.getGbDpbNxDistributerId());
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        Map<String, Object> map = new HashMap<>();
        map.put("batchId", id);
        Double aDouble = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", list);
        mapR.put("totalHanzi", convertDoubleToChineseCurrency(aDouble));
        mapR.put("total", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("tradeNo", s);
        mapR.put("distributer", nxDistributerEntity);

        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/nxDisGetGbBatchOrdersUnOut/{id}")
    @ResponseBody
    public R nxDisGetGbBatchOrdersUnOut(@PathVariable Integer id) {
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByBatchId(id);
        List<NxDepartmentOrdersEntity> list = new ArrayList<>();

        if (purchaseGoodsEntities.size() > 0) {
            for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                Integer purchaseGoodsId = purchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", purchaseGoodsId);
                List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
                if (gbDepartmentOrdersEntities.size() > 0) {
                    for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
                        Integer gbDoNxDepartmentOrderId = gbDepartmentOrdersEntity.getGbDoNxDepartmentOrderId();
                        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
                        if (ordersEntity.getNxDoPurchaseStatus() < 4) {
                            list.add(ordersEntity);
                        }

                    }
                }
            }
        }
        return R.ok().put("data", list);
    }

    @RequestMapping(value = "/disInitOrderStatus", method = RequestMethod.POST)
    @ResponseBody
    public R disInitOrderStatus(@RequestBody NxDepartmentOrdersEntity ordersEntity) {

        Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
        if (nxDoPurchaseGoodsId != -1) {
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
            Integer nxDpgBatchId = purchaseGoodsEntity.getNxDpgBatchId();
            if (nxDpgBatchId == null) {
                BigDecimal orderCount = new BigDecimal(0);
                if (purchaseGoodsEntity.getNxDpgOrdersAmount() > 0) {
                    orderCount = new BigDecimal(purchaseGoodsEntity.getNxDpgOrdersAmount()).subtract(new BigDecimal(1));
                }
                if (orderCount.compareTo(BigDecimal.ZERO) == 0) {
                    nxDistributerPurchaseGoodsService.delete(nxDoPurchaseGoodsId);
                } else {
                    purchaseGoodsEntity.setNxDpgOrdersAmount(Integer.valueOf(orderCount.toString()));
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            } else {
                return R.error(-1, "请采购员先删除对外订货");
            }
        }

        ordersEntity.setNxDoPurchaseGoodsId(-1);
        ordersEntity.setNxDoGoodsType(-1);
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        ordersEntity.setNxDoStatus(getNxOrderStatusNew());
        ordersEntity.setNxDoWeightId(null);
        nxDepartmentOrdersService.update(ordersEntity);

        if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(ordersEntity.getNxDoGbDepartmentOrderId());
            gbDepartmentOrdersEntity.setGbDoStatus(0);
            gbDepartmentOrdersEntity.setGbDoBuyStatus(0);
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }


        return R.ok();
    }

    @RequestMapping(value = "/cancelOutOrder", method = RequestMethod.POST)
    @ResponseBody
    public R cancelOutOrder(@RequestBody NxDepartmentOrdersEntity ordersEntity) {
        Integer nxDepartmentOrderId = ordersEntity.getNxDepartmentOrdersId();
        NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObject(nxDepartmentOrderId);

        ordersEntity1.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        ordersEntity1.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrdersService.update(ordersEntity1);

        if (ordersEntity1.getNxDoGbDepartmentOrderId() != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(ordersEntity.getNxDoGbDepartmentOrderId());
            gbDepartmentOrdersEntity.setGbDoStatus(0);
            gbDepartmentOrdersEntity.setGbDoBuyStatus(0);
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }

        return R.ok();
    }

    @RequestMapping(value = "/cancelOutOrderAdmin", method = RequestMethod.POST)
    @ResponseBody
    public R cancelOutOrderAdmin(@RequestBody NxDepartmentOrdersEntity ordersEntity) {
        Integer nxDepartmentOrderId = ordersEntity.getNxDepartmentOrdersId();
        NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObject(nxDepartmentOrderId);

        ordersEntity1.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        ordersEntity1.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrdersService.update(ordersEntity1);


        Map<String, Object> map = new HashMap<>();
        map.put("orderId", ordersEntity.getNxDepartmentOrdersId());
        List<NxDistributerGoodsShelfStockReduceEntity> reduceEntities = nxDisGoodsShelfStockReduceService.queryReduceListByParams(map);
        if (reduceEntities.size() > 0) {
            for (NxDistributerGoodsShelfStockReduceEntity reduceEntity : reduceEntities) {
                Integer nxDgssrNxStockId = reduceEntity.getNxDgssrNxStockId();
                NxDistributerGoodsShelfStockEntity stockEntity = nxDisGoodsShelfStockService.queryObject(nxDgssrNxStockId);

                BigDecimal restWeight = new BigDecimal(stockEntity.getNxDgssRestWeight()).add(new BigDecimal(reduceEntity.getNxDgssrCostWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal restSubtotal = new BigDecimal(stockEntity.getNxDgssRestSubtotal()).add(new BigDecimal(reduceEntity.getNxDgssrCostSubtotal())).setScale(1, BigDecimal.ROUND_HALF_UP);
                stockEntity.setNxDgssRestWeight(restWeight.toString());
                stockEntity.setNxDgssRestSubtotal(restSubtotal.toString());
                nxDisGoodsShelfStockService.update(stockEntity);

                nxDisGoodsShelfStockReduceService.delete(reduceEntity.getNxDistributerGoodsShelfStockReduceId());
            }
        }

        if (ordersEntity1.getNxDoGbDepartmentOrderId() != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(ordersEntity.getNxDoGbDepartmentOrderId());
            gbDepartmentOrdersEntity.setGbDoStatus(0);
            gbDepartmentOrdersEntity.setGbDoBuyStatus(0);
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }

        return R.ok();
    }

    /**
     * ORDER
     * 修改申请
     *
     * @param
     * @return ok
     */
    @ResponseBody
    @RequestMapping(value = "/updateOrder", method = RequestMethod.POST)
    public R updateOrder(Integer id, String weight, String standard, String remark, String printStandard, String priceLevel) {
        System.out.println("updatedoorpr" + weight);


        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(id);

        System.out.println("depdisididiidididdiid" + ordersEntity.getNxDoDepDisGoodsId());
        String oldNxDoQuantity = ordersEntity.getNxDoQuantity();

        //自动添加重量和单价小计
        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetailWithLinshi(doDisGoodsId);
        String nxDgGoodsStandardname = distributerGoodsEntity.getNxDgGoodsStandardname();

        if (priceLevel.equals("1")) {

            Integer nxDoDepartmentId = ordersEntity.getNxDoDepartmentId();
            Map<String, Object> map = new HashMap<>();
            map.put("depId", nxDoDepartmentId);
            map.put("standard", ordersEntity.getNxDoStandard());
            map.put("disGoodsId", distributerGoodsEntity.getNxDistributerGoodsId());
            System.out.println("zahusimdkd" + map);
            NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);

            if (nxDepartmentDisGoodsEntity != null) {
                ordersEntity.setNxDoPrice(nxDepartmentDisGoodsEntity.getNxDdgOrderPrice());
            }

            ordersEntity.setNxDoCostPrice(distributerGoodsEntity.getNxDgBuyingPriceOne());
            ordersEntity.setNxDoCostPriceUpdate(distributerGoodsEntity.getNxDgBuyingPriceOneUpdate());

            System.out.println("priririeieiieieieiiei0000nnnn" + ordersEntity.getNxDoPrice());

            if (standard.equals(nxDgGoodsStandardname)) {
                ordersEntity.setNxDoWeight(weight);
                BigDecimal decimal3 = new BigDecimal(weight).multiply(new BigDecimal(ordersEntity.getNxDoCostPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoCostSubtotal(decimal3.toString());

                System.out.println("priririeieiieieieiiei1111" + ordersEntity.getNxDoPrice());
                if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                    System.out.println("priririeieiieieieiiei2222" + ordersEntity.getNxDoPrice());
                    BigDecimal decimal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoSubtotal(decimal.toString());
                    //profit
                    if (ordersEntity.getNxDoCostPrice() != null && !ordersEntity.getNxDoCostPrice().trim().isEmpty()) {
                        BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());
                        BigDecimal decimal2 = new BigDecimal(ordersEntity.getNxDoPrice());
                        BigDecimal subtotalB = weightB.multiply(decimal2);

                        BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                        BigDecimal decimal1 = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal profitB = subtotalB.subtract(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                        ordersEntity.setNxDoProfitScale(scaleB.toString());
                        ordersEntity.setNxDoProfitSubtotal(profitB.toString());

                        if (ordersEntity.getNxDoExpectPrice() != null && !ordersEntity.getNxDoExpectPrice().trim().isEmpty()) {
                            BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
                            BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                            BigDecimal subtract = doPrice.subtract(expectPrice);
                            ordersEntity.setNxDoPriceDifferent(subtract.toString());
                        }
                    }
                }
            } else {
                ordersEntity.setNxDoWeight(null);
                ordersEntity.setNxDoSubtotal(null);
                ordersEntity.setNxDoCostSubtotal("0");
                ordersEntity.setNxDoProfitScale("0");
                ordersEntity.setNxDoProfitSubtotal("0");
                ordersEntity.setNxDoPriceDifferent("0");

            }
        } else {
            BigDecimal bigDecimal = new BigDecimal(weight).multiply(new BigDecimal(distributerGoodsEntity.getNxDgWillPriceTwo())).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoWeight(weight);

            Integer nxDoDepartmentId = ordersEntity.getNxDoDepartmentId();
            Map<String, Object> map = new HashMap<>();
            map.put("depId", nxDoDepartmentId);
            map.put("standard", ordersEntity.getNxDoStandard());
            map.put("disGoodsId", distributerGoodsEntity.getNxDistributerGoodsId());
            System.out.println("zahusimdkd" + map);
            NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
            if (nxDepartmentDisGoodsEntity != null) {
                ordersEntity.setNxDoPrice(nxDepartmentDisGoodsEntity.getNxDdgOrderPrice());
            }
            ordersEntity.setNxDoSubtotal(bigDecimal.toString());
            ordersEntity.setNxDoCostPrice(distributerGoodsEntity.getNxDgBuyingPriceTwo());
            ordersEntity.setNxDoCostPriceUpdate(distributerGoodsEntity.getNxDgBuyingPriceTwoUpdate());
            BigDecimal decimal3 = new BigDecimal(weight).multiply(new BigDecimal(ordersEntity.getNxDoCostPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoCostSubtotal(decimal3.toString());
            BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());
            BigDecimal decimal2 = new BigDecimal(ordersEntity.getNxDoPrice());
            BigDecimal subtotalB = weightB.multiply(decimal2);

            BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
            BigDecimal decimal1 = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal profitB = subtotalB.subtract(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            ordersEntity.setNxDoProfitScale(scaleB.toString());
            ordersEntity.setNxDoProfitSubtotal(profitB.toString());

            if (ordersEntity.getNxDoExpectPrice() != null && !ordersEntity.getNxDoExpectPrice().trim().isEmpty()) {
                BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
                BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                BigDecimal subtract = doPrice.subtract(expectPrice);
                ordersEntity.setNxDoPriceDifferent(subtract.toString());
            }
        }


        //purGoods
        if (ordersEntity.getNxDoPurchaseGoodsId() != -1) {
            Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();

            NxDistributerPurchaseGoodsEntity oldPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
            //standard changed
            if (!standard.equals(oldPurchaseGoodsEntity.getNxDpgStandard())) {
                if (oldPurchaseGoodsEntity.getNxDpgOrdersAmount() == 1) {
                    oldPurchaseGoodsEntity.setNxDpgStandard(standard);
                    oldPurchaseGoodsEntity.setNxDpgQuantity(weight);
                    nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);
                } else {

                    BigDecimal purQuantity = new BigDecimal(oldPurchaseGoodsEntity.getNxDpgQuantity());
                    BigDecimal add = purQuantity.subtract(new BigDecimal(ordersEntity.getNxDoQuantity()));
                    System.out.println("updidddidid" + add);
                    oldPurchaseGoodsEntity.setNxDpgQuantity(add.toString());
                    oldPurchaseGoodsEntity.setNxDpgOrdersAmount(oldPurchaseGoodsEntity.getNxDpgOrdersAmount() - 1);
                    NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
                    if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                        BigDecimal totaoWeight = new BigDecimal(weight);
                        oldPurchaseGoodsEntity.setNxDpgBuyQuantity(add.toString());
                        BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(purchaseGoodsEntity.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                        purchaseGoodsEntity.setNxDpgBuyQuantity(weight);
                        purchaseGoodsEntity.setNxDpgBuySubtotal(decimal2.toString());
                    }

                    nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);

                    purchaseGoodsEntity.setNxDpgDistributerId(distributerGoodsEntity.getNxDgDistributerId());
                    purchaseGoodsEntity.setNxDpgDisGoodsFatherId(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
                    purchaseGoodsEntity.setNxDpgDisGoodsGrandId(distributerGoodsEntity.getNxDgDfgGoodsGrandId());
                    purchaseGoodsEntity.setNxDpgDistributerId(distributerGoodsEntity.getNxDgDistributerId());
                    purchaseGoodsEntity.setNxDpgApplyDate(formatWhatDay(0));
//                    purchaseGoodsEntity.setNxDpgCostLevel(distributerGoodsEntity.getNxDgBuyingPriceIsGrade());
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
                    purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
                    purchaseGoodsEntity.setNxDpgOrdersAmount(1);
                    purchaseGoodsEntity.setNxDpgFinishAmount(0);
                    purchaseGoodsEntity.setNxDpgPurchaseType(1);
                    purchaseGoodsEntity.setNxDpgExpectPrice(distributerGoodsEntity.getNxDgBuyingPrice());
                    purchaseGoodsEntity.setNxDpgBuyPrice(distributerGoodsEntity.getNxDgBuyingPrice());
                    purchaseGoodsEntity.setNxDpgDisGoodsId(doDisGoodsId);
                    purchaseGoodsEntity.setNxDpgInputType(distributerGoodsEntity.getNxDgPurchaseAuto());
                    purchaseGoodsEntity.setNxDpgStandard(standard);
                    purchaseGoodsEntity.setNxDpgQuantity(ordersEntity.getNxDoQuantity());

                    nxDistributerPurchaseGoodsService.save(purchaseGoodsEntity);
                    Integer nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
                    ordersEntity.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
                }

            } else {
                System.out.println("hehehehrhereer===" + nxDgGoodsStandardname);
                BigDecimal purQuantity = new BigDecimal(oldPurchaseGoodsEntity.getNxDpgQuantity());
                BigDecimal add = purQuantity.subtract(new BigDecimal(oldNxDoQuantity)).add(new BigDecimal(weight));
                oldPurchaseGoodsEntity.setNxDpgQuantity(add.toString());
                oldPurchaseGoodsEntity.setNxDpgBuyQuantity(add.toString());
                if (standard.equals(nxDgGoodsStandardname)) {
                    System.out.println("hehehehrhereer===" + nxDgGoodsStandardname);
                    BigDecimal decimal = new BigDecimal(oldPurchaseGoodsEntity.getNxDpgBuyPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    oldPurchaseGoodsEntity.setNxDpgBuySubtotal(decimal.toString());
                }
                nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);

            }
        }

        ordersEntity.setNxDoQuantity(weight);
        ordersEntity.setNxDoStandard(standard);
        ordersEntity.setNxDoRemark(remark);
        ordersEntity.setNxDoPrintStandard(printStandard);

        ordersEntity.setNxDoCostPriceLevel(priceLevel);
        nxDepartmentOrdersService.update(ordersEntity);

        ordersEntity.setNxDistributerGoodsEntity(distributerGoodsEntity);
        return R.ok().put("data", ordersEntity);
    }


    @ResponseBody
    @RequestMapping(value = "/updateOrderReturn", method = RequestMethod.POST)
    public R updateOrderReturn(Integer id, String weight, String subtotal) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(id);
        ordersEntity.setNxDoReturnWeight(weight);
        ordersEntity.setNxDoReturnSubtotal(subtotal);
        ordersEntity.setNxDoReturnStatus(0);
        nxDepartmentOrdersService.update(ordersEntity);
        return R.ok().put("data", ordersEntity);
    }

    /**
     * ORDER
     * 删除申请
     *
     * @param nxDepartmentOrdersId 订货申请id
     * @return ok
     */
    @ResponseBody
    @RequestMapping("/delete/{nxDepartmentOrdersId}")
    public R delete(@PathVariable Integer nxDepartmentOrdersId) {
//
//                && ordersEntity.getNxDoPurchaseStatus() < getNxDepOrderBuyStatusFinishPurchase()
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObjectNew(nxDepartmentOrdersId);
        if (ordersEntity.getNxDoPurchaseGoodsId() != null && ordersEntity.getNxDoPurchaseGoodsId() != -1) {
            NxDistributerPurchaseGoodsEntity nxDistributerPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(ordersEntity.getNxDoPurchaseGoodsId());
            Integer nxDpgOrdersAmount = nxDistributerPurchaseGoodsEntity.getNxDpgOrdersAmount();
            if (nxDpgOrdersAmount > 1) {
                nxDistributerPurchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
                if (nxDistributerPurchaseGoodsEntity.getNxDpgStandard().equals(ordersEntity.getNxDoStandard())) {
                    if(nxDistributerPurchaseGoodsEntity.getNxDpgBuyQuantity() != null && ordersEntity.getNxDoQuantity() != null){
                        BigDecimal decimal = new BigDecimal(nxDistributerPurchaseGoodsEntity.getNxDpgBuyQuantity()).subtract(new BigDecimal(ordersEntity.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal multiply = decimal.multiply(new BigDecimal(nxDistributerPurchaseGoodsEntity.getNxDpgBuyPrice()));
                        nxDistributerPurchaseGoodsEntity.setNxDpgBuySubtotal(multiply.toString());
                        nxDistributerPurchaseGoodsEntity.setNxDpgBuyQuantity(decimal.toString());
                        nxDistributerPurchaseGoodsEntity.setNxDpgQuantity(decimal.toString());
                    }

                }
                nxDistributerPurchaseGoodsService.update(nxDistributerPurchaseGoodsEntity);
            } else {
                Integer nxDpgBatchId = nxDistributerPurchaseGoodsEntity.getNxDpgBatchId();
                List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByBatchId(nxDpgBatchId);
                if (purchaseGoodsEntities.size() == 1) {
                    nxDPBService.delete(nxDpgBatchId);
                }
                nxDistributerPurchaseGoodsService.delete(nxDistributerPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
            }

            Map<String, Object> map = new HashMap<>();
            map.put("depId", ordersEntity.getNxDoDepartmentId());
            map.put("status", 3);
            map.put("todayOrder", ordersEntity.getNxDoTodayOrder());
            System.out.println("ordierireirei" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
            if (ordersEntities.size() > 0) {
                for (int i = 0; i < ordersEntities.size(); i++) {
                    NxDepartmentOrdersEntity ordersEntity1 = ordersEntities.get(i);
                    ordersEntity1.setNxDoTodayOrder(ordersEntity.getNxDoTodayOrder() + i);
                    nxDepartmentOrdersService.update(ordersEntity1);
                }
            }
            nxDepartmentOrdersService.delete(nxDepartmentOrdersId);
            return R.ok();
        } else {

            Map<String, Object> map = new HashMap<>();
            map.put("depId", ordersEntity.getNxDoDepartmentId());
            map.put("status", 3);
            map.put("todayOrder", ordersEntity.getNxDoTodayOrder());
            map.put("orderBy", "time");
            System.out.println("ordierireirei" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
            if (ordersEntities.size() > 0) {
                for (int i = 0; i < ordersEntities.size(); i++) {
                    NxDepartmentOrdersEntity ordersEntity1 = ordersEntities.get(i);
                    ordersEntity1.setNxDoTodayOrder(ordersEntity.getNxDoTodayOrder() + i);
                    nxDepartmentOrdersService.update(ordersEntity1);
                }
            }
            nxDepartmentOrdersService.delete(nxDepartmentOrdersId);
            return R.ok();
        }
    }


    @ResponseBody
    @RequestMapping("/saveCash")
    public R saveCash(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {
        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
        nxDepartmentOrders.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxDepartmentOrders.setNxDoGbDistributerId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
        nxDepartmentOrders.setNxDoNxCommunityId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
        nxDepartmentOrders.setNxDoPriceDifferent("0.0");

        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("status", 3);
        int order = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        nxDepartmentOrders.setNxDoTodayOrder(order + 1);

        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = saveOneOrder(nxDepartmentOrders, nxDistributerGoodsEntity);

        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
        return R.ok().put("data", nxDepartmentOrdersEntity);

    }


    @ResponseBody
    @RequestMapping("/saveCashBefore")
    public R saveCashBefore(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {
        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
        //临时用 purUserid 赋值 前面 order 的 id
        Integer nxDoPurchaseUserId = nxDepartmentOrders.getNxDoPurchaseUserId();
        NxDepartmentOrdersEntity beforOrder = nxDepartmentOrdersService.queryObject(nxDoPurchaseUserId);
        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxDepartmentOrders.setNxDoGbDistributerId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
        nxDepartmentOrders.setNxDoNxCommunityId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
        nxDepartmentOrders.setNxDoPriceDifferent("0.0");
        nxDepartmentOrders.setNxDoTodayOrder(beforOrder.getNxDoTodayOrder());
        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = saveOneOrder(nxDepartmentOrders, nxDistributerGoodsEntity);


//


        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("todayOrder", beforOrder.getNxDoTodayOrder());
        map.put("status", 3);
        map.put("orderBy", "time");
        System.out.println("maporodoerer" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            System.out.println("orooro" + ordersEntities.size());
            for (int i = 0; i < ordersEntities.size(); i++) {
                NxDepartmentOrdersEntity ordersEntity = ordersEntities.get(i);
                int i1 = beforOrder.getNxDoTodayOrder() + i + 2;
                ordersEntity.setNxDoTodayOrder(i1);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

        beforOrder.setNxDoTodayOrder(beforOrder.getNxDoTodayOrder() + 1);
        nxDepartmentOrdersService.update(beforOrder);

        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
        return R.ok().put("data", nxDepartmentOrdersEntity);
    }


    /**
     * ORDER,DISTRIBUTER
     * 添加订货申请
     *
     * @param nxDepartmentOrders 订货申请
     * @return ok
     */
    @ResponseBody
    @RequestMapping("/save")
    public R save(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {
        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
        nxDepartmentOrders.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxDepartmentOrders.setNxDoGbDistributerId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
        nxDepartmentOrders.setNxDoNxCommunityId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("status", 3);
        System.out.println("ordierireirei" + map);
        int order = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        nxDepartmentOrders.setNxDoTodayOrder(order + 1);

        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = saveOneOrder(nxDepartmentOrders, nxDistributerGoodsEntity);
        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);


        return R.ok().put("data", nxDepartmentOrdersEntity);
    }

    @ResponseBody
    @RequestMapping("/nxDepSaveApply")
    public R nxDepSaveApply(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {

        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
        nxDepartmentOrders.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxDepartmentOrders.setNxDoGbDistributerId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
        nxDepartmentOrders.setNxDoNxCommunityId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("status", 3);
        System.out.println("ordierireirei" + map);
        int order = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        nxDepartmentOrders.setNxDoTodayOrder(order + 1);

        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = saveOneOrder(nxDepartmentOrders, nxDistributerGoodsEntity);
        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
        return R.ok().put("data", nxDepartmentOrdersEntity);
    }

    /**
     * ORDER,DISTRIBUTER
     * 添加订货申请
     *
     * @param nxDepartmentOrders 订货申请
     * @return ok
     */
    @ResponseBody
    @RequestMapping("/saveBefore")
    public R saveBefore(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {
        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
        //临时用 purUserid 赋值 前面 order 的 id
        Integer nxDoPurchaseUserId = nxDepartmentOrders.getNxDoPurchaseUserId();
        NxDepartmentOrdersEntity beforOrder = nxDepartmentOrdersService.queryObject(nxDoPurchaseUserId);
        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxDepartmentOrders.setNxDoGbDistributerId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
        nxDepartmentOrders.setNxDoNxCommunityId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
        System.out.println("befororrordidd" + beforOrder.getNxDoTodayOrder());
        nxDepartmentOrders.setNxDoTodayOrder(beforOrder.getNxDoTodayOrder());

        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = saveOneOrder(nxDepartmentOrders, nxDistributerGoodsEntity);


        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("todayOrder", beforOrder.getNxDoTodayOrder());
        map.put("status", 3);
        map.put("orderBy", "time");
        System.out.println("maporodoerer" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            System.out.println("orooro" + ordersEntities.size());
            for (int i = 0; i < ordersEntities.size(); i++) {
                NxDepartmentOrdersEntity ordersEntity = ordersEntities.get(i);
                int i1 = beforOrder.getNxDoTodayOrder() + i + 2;
                System.out.println("whisisisisisisisis====" + i1);
                ordersEntity.setNxDoTodayOrder(i1);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

        beforOrder.setNxDoTodayOrder(beforOrder.getNxDoTodayOrder() + 1);
        nxDepartmentOrdersService.update(beforOrder);

        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
        return R.ok().put("data", nxDepartmentOrdersEntity);
    }

    private void savePurGoodsAuto(NxDepartmentOrdersEntity ordersEntity) {

        NxDistributerPurchaseGoodsEntity resultPurGoods = new NxDistributerPurchaseGoodsEntity();

        //判断是否有已经分的
        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(doDisGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", doDisGoodsId);
        map.put("status", 1);
        map.put("standard", ordersEntity.getNxDoStandard());
        System.out.println("purgogogo" + map);
        NxDistributerPurchaseGoodsEntity havePurGoods = nxDistributerPurchaseGoodsService.queryIfHavePurGoods(map);
        if (havePurGoods != null) {
            resultPurGoods = havePurGoods;
            havePurGoods.setNxDpgOrdersAmount(resultPurGoods.getNxDpgOrdersAmount() + 1);

            BigDecimal orderQuantity = new BigDecimal(ordersEntity.getNxDoQuantity());
            BigDecimal purQuantity = new BigDecimal(resultPurGoods.getNxDpgQuantity());
            BigDecimal totaoWeight = orderQuantity.add(purQuantity);
            resultPurGoods.setNxDpgQuantity(totaoWeight.toString());

            if (ordersEntity.getNxDoSubtotal() != null) {
                resultPurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(havePurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                resultPurGoods.setNxDpgBuySubtotal(decimal2.toString());
            }
            nxDistributerPurchaseGoodsService.update(resultPurGoods);
            ordersEntity.setNxDoPurchaseGoodsId(resultPurGoods.getNxDistributerPurchaseGoodsId());


        } else {
            resultPurGoods.setNxDpgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
            resultPurGoods.setNxDpgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
            resultPurGoods.setNxDpgDistributerId(disGoods.getNxDgDistributerId());
            resultPurGoods.setNxDpgApplyDate(formatWhatDay(0));
            resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
            resultPurGoods.setNxDpgApplyDate(formatWhatYearDayTime(0));
            resultPurGoods.setNxDpgOrdersAmount(1);
            resultPurGoods.setNxDpgFinishAmount(0);
            resultPurGoods.setNxDpgPurchaseType(1);

            resultPurGoods.setNxDpgExpectPrice(ordersEntity.getNxDoCostPrice());
            resultPurGoods.setNxDpgBuyPrice(ordersEntity.getNxDoCostPrice());
            resultPurGoods.setNxDpgDisGoodsId(doDisGoodsId);
            resultPurGoods.setNxDpgInputType(disGoods.getNxDgPurchaseAuto());
            resultPurGoods.setNxDpgQuantity(ordersEntity.getNxDoQuantity());
            resultPurGoods.setNxDpgStandard(ordersEntity.getNxDoStandard());
            if (ordersEntity.getNxDoWeight() != null && !ordersEntity.getNxDoWeight().trim().isEmpty()) {
                BigDecimal totaoWeight = new BigDecimal(ordersEntity.getNxDoQuantity());
                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(resultPurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                // 采购商品数据
                resultPurGoods.setNxDpgBuyQuantity(ordersEntity.getNxDoQuantity());
                resultPurGoods.setNxDpgBuySubtotal(decimal2.toString());

            }
            nxDistributerPurchaseGoodsService.save(resultPurGoods);
            ordersEntity.setNxDoPurchaseGoodsId(resultPurGoods.getNxDistributerPurchaseGoodsId());

        }

        System.out.println("updaidiididii");
        nxDepartmentOrdersService.update(ordersEntity);
        NxDistributerPurchaseBatchEntity batchEntity = new NxDistributerPurchaseBatchEntity();

        //给autoBatch更新gbDepartmentOrderid
        System.out.println("suplieridid" + disGoods.getNxDgGoodsName() + disGoods.getNxDgSupplierId());
        NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(disGoods.getNxDgSupplierId());

        if (disGoods.getNxDgSupplierId() != null && supplierEntity.getNxJrdhsUserId() != null && supplierEntity.getNxJrdhsUserId() != -1) {
            //
            Map<String, Object> mapBatch = new HashMap<>();
            Integer gbDgGbSupplierId = disGoods.getNxDgSupplierId();
            mapBatch.put("supplierId", gbDgGbSupplierId);
            mapBatch.put("status", 1);
            mapBatch.put("purchaseType", 2);

            List<NxDistributerPurchaseBatchEntity> entities = nxDPBService.queryDisPurchaseBatch(mapBatch);
            if (entities.size() == 0) {
                //
                batchEntity.setNxDpbDate(formatWhatDay(0));
                batchEntity.setNxDpbTime(formatWhatTime(0));
                batchEntity.setNxDpbMonth(formatWhatMonth(0));
                batchEntity.setNxDpbPruchaseWeek(getWeek(0));
                batchEntity.setNxDpbYear(formatWhatYear(0));
                batchEntity.setNxDpbPayFullTime(formatWhatYearDayTime(0));
                batchEntity.setNxDpbStatus(-1);
                batchEntity.setNxDpbPurchaseType(2);
                batchEntity.setNxDpbSupplierId(gbDgGbSupplierId);
//                NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDgGbSupplierId);
                batchEntity.setNxDpbSellUserId(supplierEntity.getNxJrdhsUserId());
                batchEntity.setNxDpbDistributerId(ordersEntity.getNxDoDistributerId());
                batchEntity.setNxDpbPurUserId(supplierEntity.getNxJrdhsNxPurUserId());
                batchEntity.setNxDpbBuyUserId(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                batchEntity.setNxDpbBuyUserOpenId(nxJrdhUserEntity.getNxJrdhWxOpenId());
                nxDPBService.save(batchEntity);

                resultPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                resultPurGoods.setNxDpgPurchaseDate(formatWhatDay(0));
                resultPurGoods.setNxDpgTime(formatWhatYearDayTime(0));
                resultPurGoods.setNxDpgDistributerId(batchEntity.getNxDpbDistributerId());
                resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
                nxDistributerPurchaseGoodsService.update(resultPurGoods);


            } else {
                batchEntity = entities.get(0);
                resultPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                nxDistributerPurchaseGoodsService.update(resultPurGoods);
            }

            Map<String, TemplateData> mapNotice = new HashMap<>();
            mapNotice.put("thing1", new TemplateData(disGoods.getNxDgGoodsName()));
            mapNotice.put("time6", new TemplateData(formatWhatDay(0)));
            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(ordersEntity.getNxDoDepartmentId());
            mapNotice.put("thing7", new TemplateData(departmentEntity.getNxDepartmentName()));
            mapNotice.put("thing8", new TemplateData(departmentEntity.getNxDepartmentAttrName()));
            StringBuilder pathBuilder = new StringBuilder("pages/txs/disOrderBatch/disOrderBatch");
            pathBuilder.append("?batchId=").append(batchEntity.getNxDistributerPurchaseBatchId());
            pathBuilder.append("&retName=").append(nxDistributerService.queryObject(batchEntity.getNxDpbDistributerId()).getNxDistributerName());
            pathBuilder.append("&disId=").append(batchEntity.getNxDpbDistributerId());
            pathBuilder.append("&buyerUserId=").append(batchEntity.getNxDpbBuyUserId());
            pathBuilder.append("&purUserId=").append(batchEntity.getNxDpbPurUserId());
            pathBuilder.append("&fromBuyer=1");
            String path = pathBuilder.toString();
            System.out.println("paththhththt" + path);

            Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);
            System.out.println("suppsleir" + path);
            WeNoticeService.nxSupplierOrderSave(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);
            ordersEntity.setNxDoPurchaseStatus(getNxDisPurchaseGoodsIsPurchase());
            ordersEntity.setNxDoPurchaseGoodsId(resultPurGoods.getNxDistributerPurchaseGoodsId());
        }

        nxDepartmentOrdersService.update(ordersEntity);

    }


}


//
//    private NxDepartmentOrdersEntity aaaExcel(NxDepartmentOrdersEntity ordersEntity,
//                                              NxDistributerGoodsEntity disGoodsEntity, String goodsName) {
//        NxDepartmentOrdersEntity order = new NxDepartmentOrdersEntity();
//        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
//        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
//        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
//        order.setNxDoDepartmentId(ordersEntity.getNxDoDepartmentId());
//        order.setNxDoDepartmentFatherId(ordersEntity.getNxDoDepartmentFatherId());
//        order.setNxDoDistributerId(ordersEntity.getNxDoDistributerId());
//        order.setNxDoQuantity(ordersEntity.getNxDoQuantity());
//        order.setNxDoStandard(ordersEntity.getNxDoStandard());
//        order.setNxDoRemark(ordersEntity.getNxDoRemark());
//        order.setNxDoStatus(0);
//        order.setNxDoArriveDate(formatWhatDate(0));
//        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
//        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
//        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
//        order.setNxDoNxGoodsId(disGoodsEntity.getNxDgNxGoodsId());
//        order.setNxDoNxGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
//        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
//        order.setNxDoApplyDate(formatWhatDay(0));
//        order.setNxDoArriveOnlyDate(formatWhatDate(0));
//        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
//        order.setNxDoArriveDate(formatWhatDay(0));
//        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
//        order.setNxDoApplyOnlyTime(formatWhatTime(0));
//        order.setNxDoGbDistributerId(-1);
//        order.setNxDoGbDepartmentFatherId(-1);
//        order.setNxDoGbDepartmentId(-1);
//        order.setNxDoNxCommunityId(-1);
//        order.setNxDoNxCommRestrauntFatherId(-1);
//        order.setNxDoNxCommRestrauntId(-1);
////        order.setNxDoCostPriceLevel("0");
//        order.setNxDoPurchaseGoodsId(disGoodsEntity.getNxDgPurchaseAuto());
//        order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceUpdate());
//        order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPrice());
//        order.setNxDoPrintStandard(ordersEntity.getNxDoPrintStandard());
//        order.setNxDoProfitSubtotal("0");
//        order.setNxDoProfitScale("0");
//        order.setNxDoArriveWhatDay(getWeek(0));
//        if (ordersEntity.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname())) {
//            order.setNxDoWeight(ordersEntity.getNxDoQuantity());
//            BigDecimal decimal = new BigDecimal(ordersEntity.getNxDoQuantity());
//            BigDecimal decimal1 = new BigDecimal(disGoodsEntity.getNxDgBuyingPrice());
//            BigDecimal decimal2 = decimal.multiply(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
//            order.setNxDoCostSubtotal(decimal2.toString());
//        }
//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", ordersEntity.getNxDoDepartmentId());
//        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
//        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
//        if (departmentDisGoodsEntity != null) {
//            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
//            order.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
//            if (order.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname())) {
//                BigDecimal decimal = new BigDecimal(departmentDisGoodsEntity.getNxDdgOrderPrice()).multiply(new BigDecimal(order.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                order.setNxDoSubtotal(decimal.toString());
//            }
//        }
//
//        if (ordersEntity.getNxDoPurchaseUserId() != -1) {
//            //临时用 purUserid 赋值 前面 order 的 id
//            Integer nxDoPurchaseUserId = ordersEntity.getNxDoPurchaseUserId();
//            NxDepartmentOrdersEntity beforOrder = nxDepartmentOrdersService.queryObject(nxDoPurchaseUserId);
//
//            order.setNxDoTodayOrder(beforOrder.getNxDoTodayOrder());
//
//            Map<String, Object> mapU = new HashMap<>();
//            mapU.put("depId", ordersEntity.getNxDoDepartmentId());
//            mapU.put("todayOrder", beforOrder.getNxDoTodayOrder());
//            mapU.put("status", 3);
//            mapU.put("orderBy", "time");
//            System.out.println("maporodoerer" + mapU);
//            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(mapU);
//            if (ordersEntities.size() > 0) {
//                System.out.println("orooro" + ordersEntities.size());
//                for (int i = 0; i < ordersEntities.size(); i++) {
//                    NxDepartmentOrdersEntity ordersEn = ordersEntities.get(i);
//                    int i1 = beforOrder.getNxDoTodayOrder() + i + 2;
//                    System.out.println("whisisisisisisisis====" + i1);
//                    ordersEn.setNxDoTodayOrder(i1);
//                    nxDepartmentOrdersService.update(ordersEn);
//                }
//            }
//
//            beforOrder.setNxDoTodayOrder(beforOrder.getNxDoTodayOrder() + 1);
//            nxDepartmentOrdersService.update(beforOrder);
//
//        } else {
//            Map<String, Object> mapss = new HashMap<>();
//            mapss.put("depId", ordersEntity.getNxDoDepartmentId());
//            mapss.put("status", 3);
//            int orderOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapss);
//            order.setNxDoTodayOrder(orderOrder + 1);
//        }
//        nxDepartmentOrdersService.save(order);
//        ordersEntity.setNxDepartmentOrdersId(order.getNxDepartmentOrdersId());
//
//        //auto
//        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
//            savePurGoodsAuto(order);
//        }
//
//        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
//        order.setNxDistributerGoodsEntity(distributerGoodsEntity);
//        System.out.println("orderrroooroorretututu" + order);
//        return order;
//    }