package com.nongxinle.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.nongxinle.utils.R;
import com.nongxinle.utils.RRException;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbPurchaseGoodsTypeForOrder;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.*;


@RestController
@RequestMapping("api/nxdepartmentorderspart")
public class NxDepartmentOrdersPartController {


    private static final Logger logger = LoggerFactory.getLogger(NxDepartmentOrdersController.class);

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
    private GbDistributerGoodsService gbDistributerGoodsService;
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
    @Autowired
    private NxOrderOcrTrainingDataService nxOrderOcrTrainingDataService;
    @Autowired
    private NxDistributerGoodsLinshiService linshiService;



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

        // 使用 queryPurchaseGoodsByParams 查询列表，避免 selectOne() 返回多条记录时报错
        List<NxDistributerPurchaseGoodsEntity> purGoodsList = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
        NxDistributerPurchaseGoodsEntity havePurGoods = null;
        if (purGoodsList != null && !purGoodsList.isEmpty()) {
            // 如果有多条记录，选择第一条（通常应该只有一条，但数据库可能有重复数据）
            havePurGoods = purGoodsList.get(0);

        }

        if (havePurGoods != null) {
            resultPurGoods = havePurGoods;
            havePurGoods.setNxDpgOrdersAmount(resultPurGoods.getNxDpgOrdersAmount() + 1);

            // 处理数量为空或空字符串的情况，使用默认值0
            String orderQtyStr = ordersEntity.getNxDoQuantity();
            if (orderQtyStr == null || orderQtyStr.trim().isEmpty()) {
                orderQtyStr = "0";
            }
            String purQtyStr = resultPurGoods.getNxDpgQuantity();
            if (purQtyStr == null || purQtyStr.trim().isEmpty()) {
                purQtyStr = "0";
            }

            BigDecimal orderQuantity = new BigDecimal(orderQtyStr);
            BigDecimal purQuantity = new BigDecimal(purQtyStr);
            BigDecimal totaoWeight = orderQuantity.add(purQuantity).setScale(1,BigDecimal.ROUND_HALF_UP);
            resultPurGoods.setNxDpgQuantity(totaoWeight.toString());
            System.out.println("ordsss" + ordersEntity.getNxDoSubtotal());
            if (ordersEntity.getNxDoSubtotal() != null && !ordersEntity.getNxDoSubtotal().equals("0")) {
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
            resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
            resultPurGoods.setNxDpgApplyDate(formatWhatYearDayTime(0));
            resultPurGoods.setNxDpgOrdersAmount(1);
            resultPurGoods.setNxDpgFinishAmount(0);
            resultPurGoods.setNxDpgPurchaseType(getGbPurchaseGoodsTypeForOrder());


            if(ordersEntity.getNxDoCostPrice() != null){
                resultPurGoods.setNxDpgExpectPrice(ordersEntity.getNxDoCostPrice());
                resultPurGoods.setNxDpgBuyPrice(ordersEntity.getNxDoCostPrice());
            }else{
                resultPurGoods.setNxDpgExpectPrice("0");
                resultPurGoods.setNxDpgBuyPrice("0");
            }

            resultPurGoods.setNxDpgDisGoodsId(doDisGoodsId);
            resultPurGoods.setNxDpgInputType(disGoods.getNxDgPurchaseAuto());
            resultPurGoods.setNxDpgQuantity(ordersEntity.getNxDoQuantity());
            resultPurGoods.setNxDpgStandard(ordersEntity.getNxDoStandard());
            System.out.println("saveoroororiid" + ordersEntity.getNxDoCostPrice());
            if (ordersEntity.getNxDoWeight() != null && !ordersEntity.getNxDoWeight().trim().isEmpty()
                    && ordersEntity.getNxDoCostPrice() != null && !ordersEntity.getNxDoCostPrice().trim().isEmpty()
            ) {
                BigDecimal totaoWeight = new BigDecimal(ordersEntity.getNxDoQuantity());
                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(ordersEntity.getNxDoCostPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
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

            List<NxDistributerPurchaseBatchEntity> entities = nxDistributerPurchaseBatchService.queryDisPurchaseBatch(mapBatch);
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
                batchEntity.setNxDpbSellUserId(supplierEntity.getNxJrdhsUserId());
                batchEntity.setNxDpbDistributerId(ordersEntity.getNxDoDistributerId());
                batchEntity.setNxDpbPurUserId(supplierEntity.getNxJrdhsNxPurUserId());
                batchEntity.setNxDpbBuyUserId(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                batchEntity.setNxDpbBuyUserOpenId(nxJrdhUserEntity.getNxJrdhWxOpenId());
                batchEntity.setNxDpbOrderIsNotice(1);
                LocalDateTime now = LocalDateTime.now();
                int hour = now.getHour();
                if( hour < 12){
                    batchEntity.setNxDpbNeedDate(formatWhatDay(0));
                }else{
                    batchEntity.setNxDpbNeedDate(formatWhatDay(1));
                }
                nxDistributerPurchaseBatchService.save(batchEntity);

                resultPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                resultPurGoods.setNxDpgPurchaseDate(formatWhatDay(0));
                resultPurGoods.setNxDpgTime(formatWhatYearDayTime(0));
                resultPurGoods.setNxDpgDistributerId(batchEntity.getNxDpbDistributerId());
                resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
                nxDistributerPurchaseGoodsService.update(resultPurGoods);


            } else {
                batchEntity = entities.get(0);
                resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
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
            pathBuilder.append("&buyUserId=").append(batchEntity.getNxDpbBuyUserId());
            pathBuilder.append("&purUserId=").append(batchEntity.getNxDpbPurUserId());
            pathBuilder.append("&fromBuyer=1");
            String path = pathBuilder.toString();
            System.out.println("paththhththt" + path);

            Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);
            if (nxJrdhUserEntity != null) {
                System.out.println("suppsleir" + path);
                WeNoticeService.nxSupplierOrderSave(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);
                ordersEntity.setNxDoPurchaseGoodsId(resultPurGoods.getNxDistributerPurchaseGoodsId());
            }

        }

        nxDepartmentOrdersService.update(ordersEntity);

    }

//    @RequestMapping(value = "/delNx/{id}")
//    @ResponseBody
//    public R delNx(@PathVariable Integer id) {
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", id);
//        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.queryDepartmentListByParams(map);
//        if (departmentEntities.size() > 0) {
//            for (NxDepartmentEntity departmentEntity : departmentEntities) {
//                Integer departmentId = departmentEntity.getNxDepartmentId();
//                map.put("depId", departmentId);
//                List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
//                if (departmentDisGoodsEntities.size() > 0) {
//                    for (NxDepartmentDisGoodsEntity disGoodsEntity : departmentDisGoodsEntities) {
//                        nxDepartmentDisGoodsService.delete(disGoodsEntity.getNxDepartmentDisGoodsId());
//                    }
//                }
//
//                List<NxDepartmentUserEntity> userEntities = nxDepartmentUserService.queryAllUsersByDepId(departmentId);
//                if (userEntities.size() > 0) {
//                    for (NxDepartmentUserEntity userEntity : userEntities) {
//                        nxDepartmentUserService.delete(userEntity.getNxDepartmentUserId());
//                    }
//                }
//
//
//                List<NxDepartmentOrdersHistoryEntity> nxDepartmentOrdersHistoryEntities = ordersHistoryService.queryDepHistoryOrdersByParams(map);
//                if (nxDepartmentOrdersHistoryEntities.size() > 0) {
//                    for (NxDepartmentOrdersHistoryEntity userEntity : nxDepartmentOrdersHistoryEntities) {
//                        historyService.delete(userEntity.getNxDepartmentOrdersHistoryId());
//                    }
//                }
//
//                nxDepartmentService.delete(departmentEntity.getNxDepartmentId());
//
//            }
//        }
//
//        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
//        if (ordersEntities.size() > 0) {
//            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
//                nxDepartmentOrdersService.delete(ordersEntity.getNxDepartmentOrdersId());
//            }
//        }
//
//        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
//        if (purchaseGoodsEntities.size() > 0) {
//            for (NxDistributerPurchaseGoodsEntity NxDistributerPurchase : purchaseGoodsEntities) {
//                nxDistributerPurchaseGoodsService.delete(NxDistributerPurchase.getNxDistributerPurchaseGoodsId());
//            }
//        }
//
//        List<NxDistributerPurchaseBatchEntity> batchEntities = nxDistributerPurchaseBatchService.queryDisPurchaseBatch(map);
//        if (batchEntities.size() > 0) {
//            for (NxDistributerPurchaseBatchEntity batchEntity : batchEntities) {
//                nxDistributerPurchaseBatchService.delete(batchEntity.getNxDistributerPurchaseBatchId());
//            }
//        }
//
//        List<NxDistributerGoodsEntity> distributerGoodsEntities = nxDistributerGoodsService.queryDisGoodsByParams(map);
//        if (distributerGoodsEntities.size() > 0) {
//            for (NxDistributerGoodsEntity batchEntity : distributerGoodsEntities) {
//
//                map.put("disGoodsId", batchEntity.getNxDistributerGoodsId());
//                List<NxDistributerAliasEntity> aliasEntities = nxDistributerAliasService.queryAliasByParmas(map);
//                if (aliasEntities.size() > 0) {
//                    for (NxDistributerAliasEntity ordersEntity : aliasEntities) {
//                        nxDistributerAliasService.delete(ordersEntity.getNxDistributerAliasId());
//                    }
//                }
//
//                List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(map);
//                if (distributerStandardEntities.size() > 0) {
//                    for (NxDistributerStandardEntity ordersEntity : distributerStandardEntities) {
//                        nxDistributerStandardService.delete(ordersEntity.getNxDistributerStandardId());
//                    }
//                }
//
//                nxDistributerGoodsService.delete(batchEntity.getNxDistributerGoodsId());
//            }
//        }
//
//        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
//        if (fatherGoodsEntities.size() > 0) {
//            for (NxDistributerFatherGoodsEntity batchEntity : fatherGoodsEntities) {
//                nxDistributerFatherGoodsService.delete(batchEntity.getNxDistributerFatherGoodsId());
//            }
//        }
//
//        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);
//        if (billEntityList.size() > 0) {
//            for (NxDepartmentBillEntity batchEntity : billEntityList) {
//                nxDepartmentBillService.delete(batchEntity.getNxDepartmentBillId());
//            }
//        }
//        nxDistributerService.delteNxDis(id);
//
//        return R.ok();
//    }


//    @ResponseBody
//    @RequestMapping("/deleteNxOrders")
//    public R deleteNxOrders(@RequestBody Integer[] nxOrdersSubIds) {
//        nxDepartmentOrdersService.deleteBatch(nxOrdersSubIds);
//        return R.ok();
//    }


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
        mapCost.put("arr", greatGrandGoodsCost);
        return R.ok().put("data", mapCost);
    }





    /**
     * 下载货架商品导入模板
     * 生成空的 Excel 模板，包含表头和示例说明
     */
    @RequestMapping(value = "/downloadShelfGoodsTemplate", method = RequestMethod.GET)
    public void downloadShelfGoodsTemplate(HttpServletResponse response) throws IOException {
        System.out.println("[downloadShelfGoodsTemplate] 请求下载模板");

        HSSFWorkbook workbook = new HSSFWorkbook();
        try {
            HSSFSheet sheet = workbook.createSheet("货架商品导入模板");
            sheet.setDefaultColumnWidth(20);

            HSSFCellStyle headerStyle = workbook.createCellStyle();
            HSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            int rowIndex = 0;
            // 提示行
            HSSFRow tipRow = sheet.createRow(rowIndex++);
            tipRow.createCell(0).setCellValue("提示：商品名称和规格为必填项，其他字段为可选项。");

            // 表头
            HSSFRow headerRow = sheet.createRow(rowIndex++);
            String[] headers = new String[]{
                    "商品名称(必填)",
                    "规格(必填)",
                    "规格重量",
                    "品牌",
                    "外包装",
                    "包装数量"
            };
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 示例行（可选，帮助用户理解格式）
            HSSFRow exampleRow = sheet.createRow(rowIndex++);
            exampleRow.createCell(0).setCellValue("示例：菌伯乐鹿茸菌");
            exampleRow.createCell(1).setCellValue("桶");
            exampleRow.createCell(2).setCellValue("5kg");
            exampleRow.createCell(3).setCellValue("菌伯乐");
            exampleRow.createCell(4).setCellValue("箱");
            exampleRow.createCell(5).setCellValue("24");

            String fileName = "货架商品导入模板.xls";
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            workbook.write(response.getOutputStream());
            response.flushBuffer();
            System.out.println("[downloadShelfGoodsTemplate] 模板生成完成，已输出响应");
        } catch (IOException e) {
            System.err.println("[downloadShelfGoodsTemplate] 生成模板异常: " + e.getMessage());
            throw new RRException("生成模板失败");
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                System.err.println("[downloadShelfGoodsTemplate] 关闭工作簿异常: " + e.getMessage());
            }
        }
    }


//    private NxDepartmentOrdersEntity aaaTemp(NxDepartmentOrdersEntity order) {
//
//        //1.查询 nxGoods 如果有完全一个的，就下载
//        // 1.1 搜索商品名称+规格完全相同
//        List<NxGoodsEntity> nxGoodsEntities = new ArrayList<>();
//        Map<String, Object> map = new HashMap<>();
//        map.put("name", order.getNxDoGoodsName());
//        map.put("level", 3);
//        List<NxGoodsEntity> nxGoodsEntitiesEx = nxGoodsService.queryNxGoodsByParams(map);
//
//        List<NxGoodsEntity> nxGoodsEntitiesA = nxAliasService.queryNxGoodsByName(map);
//
//        String pinyinString = order.getNxDoGoodsName();
//        for (int i = 0; i < order.getNxDoGoodsName().length(); i++) {
//            String str = order.getNxDoGoodsName().substring(i, i + 1);
//            if (str.matches("[\u4E00-\u9FFF]")) {
//                pinyinString = hanziToPinyin(order.getNxDoGoodsName());
//            }
//        }
//        Map<String, Object> mapSame = new HashMap<>();
//        mapSame.put("level", 3);
//        mapSame.put("searchStr", order.getNxDoGoodsName());
//        mapSame.put("searchPinyin", pinyinString);
//        List<NxGoodsEntity> nxGoodsEntitiesSame = nxGoodsService.queryQuickSearchNxGoods(mapSame);
//
//        TreeSet<NxGoodsEntity> all = new TreeSet();
//        all.addAll(nxGoodsEntitiesEx);
//        all.addAll(nxGoodsEntitiesA);
//        all.addAll(nxGoodsEntitiesSame);
//
//        // 1.2 商品名称相同
//
//        // 如果没有完全一样的，则视为临时
//        if (all.size() > 0) {
//            // 获取分销商ID，用于检查商品是否已下载
//            Integer disId = order.getNxDoDistributerId();
//
//                // 1. 收集所有分销商商品对应的系统商品ID（非null的）
//                Set<Integer> distributerGoodsNxGoodsIds = new HashSet<>();
//            List<NxDistributerGoodsEntity> distributerGoodsList = order.getNxDistributerGoodsEntityList();
//            if (distributerGoodsList != null && distributerGoodsList.size() > 0) {
//                logger.info("[aaaTemp] 已有分销商商品候选列表（数量: {}），需要去除系统商品中已出现的商品", distributerGoodsList.size());
//                for (NxDistributerGoodsEntity distributerGoods : distributerGoodsList) {
//                    if (distributerGoods.getNxDgNxGoodsId() != null) {
//                        distributerGoodsNxGoodsIds.add(distributerGoods.getNxDgNxGoodsId());
//                    }
//                }
//                logger.info("[aaaTemp] 分销商商品对应的系统商品ID集合: {}", distributerGoodsNxGoodsIds);
//            }
//
//            // 2. 过滤系统商品：只保留从未下载过的商品
//            //    如果商品已下载，需要将其对应的分销商商品加入到候选列表中
//                TreeSet<NxGoodsEntity> filteredNxGoodsSet = new TreeSet<>();
//            List<NxDistributerGoodsEntity> distributerGoodsToAdd = new ArrayList<>();
//
//                for (NxGoodsEntity nxGoods : all) {
//                if (nxGoods.getNxGoodsId() == null) {
//                    continue;
//                }
//
//                Integer nxGoodsId = nxGoods.getNxGoodsId();
//
//                // 2.1 检查是否已在候选列表中
//                boolean inCandidateList = distributerGoodsNxGoodsIds.contains(nxGoodsId);
//
//                // 2.2 检查是否已在数据库中下载过（需要分销商ID）
//                boolean alreadyDownloaded = false;
//                NxDistributerGoodsEntity downloadedGoods = null;
//                if (disId != null) {
//                    Map<String, Object> checkParams = new HashMap<>();
//                    checkParams.put("disId", disId);
//                    checkParams.put("goodsId", nxGoodsId);
//                    List<NxDistributerGoodsEntity> downloadedGoodsList = nxDistributerGoodsService.queryDisGoodsByParams(checkParams);
//                    if (downloadedGoodsList != null && !downloadedGoodsList.isEmpty()) {
//                        alreadyDownloaded = true;
//                        downloadedGoods = downloadedGoodsList.get(0);
//                        logger.info("[aaaTemp] 系统商品 nxGoodsId={} 已在数据库中下载过，对应的分销商商品ID={}",
//                                nxGoodsId, downloadedGoods.getNxDistributerGoodsId());
//                    }
//                }
//
//                // 2.3 如果已下载或已在候选列表中，不加入系统商品列表
//                if (inCandidateList || alreadyDownloaded) {
//                    // 如果已下载但不在候选列表中，需要加入到候选列表
//                    if (alreadyDownloaded && !inCandidateList && downloadedGoods != null) {
//                        // 检查是否已经在候选列表中（通过ID比较）
//                        boolean existsInList = false;
//                        if (distributerGoodsList != null) {
//                            for (NxDistributerGoodsEntity existing : distributerGoodsList) {
//                                if (existing.getNxDistributerGoodsId() != null &&
//                                    existing.getNxDistributerGoodsId().equals(downloadedGoods.getNxDistributerGoodsId())) {
//                                    existsInList = true;
//                                    break;
//                                }
//                            }
//                        }
//                        if (!existsInList) {
//                            distributerGoodsToAdd.add(downloadedGoods);
//                            logger.info("[aaaTemp] 将已下载的分销商商品（ID={}）加入到候选列表", downloadedGoods.getNxDistributerGoodsId());
//                        }
//                    }
//                } else {
//                    // 从未下载过，加入系统商品列表
//                        filteredNxGoodsSet.add(nxGoods);
//                    }
//                }
//
//            // 3. 将已下载的商品加入到候选列表（如果候选列表存在）
//            if (!distributerGoodsToAdd.isEmpty() && distributerGoodsList != null) {
//                distributerGoodsList.addAll(distributerGoodsToAdd);
//                logger.info("[aaaTemp] 已将 {} 个已下载的分销商商品加入到候选列表", distributerGoodsToAdd.size());
//            } else if (!distributerGoodsToAdd.isEmpty()) {
//                // 如果候选列表不存在，创建新的列表
//                order.setNxDistributerGoodsEntityList(distributerGoodsToAdd);
//                logger.info("[aaaTemp] 创建新的候选列表，包含 {} 个已下载的分销商商品", distributerGoodsToAdd.size());
//            }
//
//            // 4. 设置过滤后的系统商品列表
//                if (filteredNxGoodsSet.size() > 0) {
//                logger.info("[aaaTemp] 过滤后，系统商品列表数量: {}（原始数量: {}），这些商品都是从未下载过的",
//                        filteredNxGoodsSet.size(), all.size());
//                    order.setNxGoodsEntities(filteredNxGoodsSet);
//                } else {
//                logger.info("[aaaTemp] 过滤后，系统商品列表为空（所有系统商品都已下载过或已在候选列表中）");
//            }
//        }
//
//        order.setNxDoStatus(-2);
//        order.setNxDoArriveDate(formatWhatDate(0));
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
//        order.setNxDoProfitSubtotal("0");
//        order.setNxDoProfitScale("0");
//        order.setNxDoCostPrice("0.1");
//        order.setNxDoCostPriceLevel("1");
//        order.setNxDoArriveWhatDay(getWeek(0));
//        Map<String, Object> mapss = new HashMap<>();
//        mapss.put("depId", order.getNxDoDepartmentId());
//        mapss.put("status", 3);
//        int orderOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapss);
//        order.setNxDoTodayOrder(orderOrder + 1);
//        System.out.println("costrpriririrririr" + order.getNxDoCostPrice());
//        nxDepartmentOrdersService.save(order);
//
//        // 创建或关联训练数据（未找到商品的情况）
//        // 如果订单已经关联了训练数据ID，直接返回，不重复创建
//        if (order.getNxDoTrainingDataId() != null) {
//            logger.info("[aaaTemp] 订单已关联训练数据ID: {}, 订单ID: {}, 跳过创建训练数据",
//                    order.getNxDoTrainingDataId(), order.getNxDepartmentOrdersId());
//            return order;
//        }
//
//        // 查询是否已有匹配的训练数据
//        Map<String, Object> matchParams = new HashMap<>();
//        matchParams.put("departmentId", order.getNxDoDepartmentId());
//        matchParams.put("goodsName", order.getNxDoGoodsName());
//        // 不再使用 standard 和 standardWeight 作为查询条件
//
//        NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);
//
//        if (matchedTrainingData != null) {
//            // 如果找到了匹配的训练数据，关联到订单
//            order.setNxDoTrainingDataId(matchedTrainingData.getNxOtdId());
//            nxDepartmentOrdersService.update(order);
//            logger.info("[aaaTemp] 关联已有训练数据到订单，训练数据ID: {}, 订单ID: {}",
//                    matchedTrainingData.getNxOtdId(), order.getNxDepartmentOrdersId());
//        } else {
//            // 如果没有找到匹配的训练数据，创建新的训练数据
//            NxOrderOcrTrainingDataEntity trainingData = new NxOrderOcrTrainingDataEntity();
//            trainingData.setNxOtdDepartmentId(order.getNxDoDepartmentId());
//            trainingData.setNxOtdDepartmentFatherId(order.getNxDoDepartmentFatherId());
//            trainingData.setNxOtdDistributerId(order.getNxDoDistributerId());
//            trainingData.setNxOtdOriginalGoodsName(order.getNxDoGoodsName());
//            trainingData.setNxOtdOriginalQuantity(order.getNxDoQuantity());
//            trainingData.setNxOtdOriginalStandard(order.getNxDoStandard());
//            trainingData.setNxOtdOriginalStandardWeight(null); // 订单实体中没有 standardWeight 字段
//            trainingData.setNxOtdOriginalRemark(order.getNxDoRemark() != null ? order.getNxDoRemark() : "");
//            trainingData.setNxOtdIsNameManuallyAnnotated(0);
//            trainingData.setNxOtdIsQuantityManuallyAnnotated(0);
//            trainingData.setNxOtdIsStandardManuallyAnnotated(0);
//            trainingData.setNxOtdIsStandardWeightManuallyAnnotated(0);
//            trainingData.setNxOtdIsRemarkManuallyAnnotated(0);
//            // 最终确认字段不提前赋值，等待用户手动标注
//            trainingData.setNxOtdFinalGoodsName(null);
//            trainingData.setNxOtdFinalQuantity(null);
//            trainingData.setNxOtdFinalStandard(null);
//            trainingData.setNxOtdFinalStandardWeight(null);
//            trainingData.setNxOtdFinalRemark(null);
//            trainingData.setNxOtdOrderId(order.getNxDepartmentOrdersId());
//            trainingData.setNxOtdDataSource("PASTE_SEARCH");
//            trainingData.setNxOtdCreateDate(formatWhatYearDayTime(0));
//            trainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
//            trainingData.setNxOtdCreateUserId(order.getNxDoOrderUserId() != null ? order.getNxDoOrderUserId() : -1);
//
//            nxOrderOcrTrainingDataService.save(trainingData);
//            logger.info("[aaaTemp] 创建训练数据（未找到商品），训练数据ID: {}, 订单ID: {}",
//                    trainingData.getNxOtdId(), order.getNxDepartmentOrdersId());
//            // 关联训练数据ID到订单
//            order.setNxDoTrainingDataId(trainingData.getNxOtdId());
//            nxDepartmentOrdersService.update(order);
//        }
//
//        return order;
//    }
//
//
//
//    public NxDepartmentOrdersEntity saveOneOrder(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
//        System.out.println("saveONeOrderereerereeqonenenneorere");
//        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
//        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
//        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
//        // 保存订单的原始状态（如果已经是 -2 待修正，则保持，否则设置为 0）
//        Integer originalStatus = order.getNxDoStatus();
//        if (originalStatus == null || originalStatus != -2) {
//            order.setNxDoStatus(0);
//        }
//        order.setNxDoArriveDate(formatWhatDate(0));
//        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
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
//        order.setNxDoPurchaseGoodsId(disGoodsEntity.getNxDgPurchaseAuto());
//        order.setNxDoArriveWhatDay(getWeek(0));
//        order.setNxDoCostPriceLevel("1");
//
//
//        // 使用完善的价格处理逻辑（平台型/非平台型分支、货架库存查询、规格匹配等）
//        processOrderPrice(order, disGoodsEntity);
//
//        System.out.println("jieghuasu11111" + order.getNxDoPrice());
//
//        System.out.println("jieghuasu11111" + order.getNxDoPrice());
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", order.getNxDoDepartmentId());
//        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
//        map.put("standard", order.getNxDoStandard());
//        System.out.println("depmapapmmdmmdd" + map);
//        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
//        if (departmentDisGoodsEntity != null) {
//            System.out.println("prinstnan" + departmentDisGoodsEntity.getNxDdgOrderStandard());
//            // 判断是否使用了大包装单价，如果是则设置打印规格为大包装单位（支持智能匹配：件=箱）
//            boolean isUsingCartonPrice = disGoodsEntity.getNxDgCartonUnit() != null
//                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
//                    && order.getNxDoStandard() != null
//                    && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim());
//
//            if (isUsingCartonPrice) {
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
//                System.out.println("使用大包装单价，打印规格设置为: " + disGoodsEntity.getNxDgCartonUnit());
//            } else if (order.getNxDoCostPriceLevel() == null || order.getNxDoCostPriceLevel().equals("1")) {
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
//            } else {
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
//            }
//
//            order.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
//            //如果有重量和单价，则计算 subtotal
//            if(order.getNxDoWeight() != null && !order.getNxDoWeight().trim().isEmpty()
//                    && order.getNxDoPrice() != null && !order.getNxDoPrice().trim().isEmpty()){
//                try {
//                    BigDecimal weight = new BigDecimal(order.getNxDoWeight());
//                    BigDecimal price = new BigDecimal(order.getNxDoPrice());
//                    BigDecimal subtotal = weight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    order.setNxDoSubtotal(subtotal.toString());
//                } catch (NumberFormatException e) {
//                    // 如果转换失败，不设置subtotal
//                    logger.warn("[saveOneOrder] 计算subtotal失败，重量或单价格式错误: weight={}, price={}, error={}",
//                                order.getNxDoWeight(), order.getNxDoPrice(), e.getMessage());
//                }
//            }
//            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
//        }else{
//            map.put("standard", null);
//            System.out.println("depmapapapappapa" + map);
//            NxDepartmentDisGoodsEntity departmentDisGoodsEntityO = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
//            if (departmentDisGoodsEntityO != null) {
//                order.setNxDoDepDisGoodsId(departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
//            }
//        }
//
//
//        System.out.println("orderrwhwidiidic" + order.getNxDoGoodsName() + "getNxDoTodayOrder" + order.getNxDoTodayOrder());
//        if (order.getNxDoTodayOrder() == null) {
//            Map<String, Object> mapss = new HashMap<>();
//            mapss.put("depId", order.getNxDoDepartmentId());
//            mapss.put("status", 3);
//            mapss.put("todayOrder", 1);
//            int orderOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapss);
//            order.setNxDoTodayOrder(orderOrder + 1);
//        }
//
//        System.out.println("jieghuasu2222" + order.getNxDoPrice());
//        nxDepartmentOrdersService.save(order);
//
//        //auto
//        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
//            savePurGoodsAuto(order);
//        }
//        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
//        order.setNxDistributerGoodsEntity(distributerGoodsEntity);
//
//        // 找到商品后，设置为0（已保存），但如果订单状态是 -2（待修正），则保持 -2
//        if (order.getNxDoStatus() == null || (order.getNxDoStatus() != 0 && order.getNxDoStatus() != -2)) {
//            order.setNxDoStatus(0);
//        }
//
//        return order;
//    }
//
//
//    private void processOrderPrice(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
//        Integer nxDoDistributerId = order.getNxDoDistributerId();
//        NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDoDistributerId);
//        //平台型
//        if (distributerEntity.getNxDistributerBusinessTypeId() > 6) {
//            if (disGoodsEntity.getNxDgWillPriceTwo() != null
//                    && !disGoodsEntity.getNxDgWillPriceTwo().trim().isEmpty()
//                    && order.getNxDoStandard().equals(disGoodsEntity.getNxDgWillPriceTwoStandard())) {
//                System.out.println("levlellelelelel111222222222222222");
//                BigDecimal doQuantity = new BigDecimal(order.getNxDoQuantity());
//                BigDecimal cosPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceTwo());
//                BigDecimal willPrice = new BigDecimal(disGoodsEntity.getNxDgWillPriceTwo());
//                BigDecimal costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//                BigDecimal subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//                BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
//                BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
//
//                // 判断是否使用大包装单价，如果是则设置打印规格为大包装单位
//                if (disGoodsEntity.getNxDgCartonUnit() != null
//                        && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
//                        && order.getNxDoStandard() != null
//                        && order.getNxDoStandard().trim().equals(disGoodsEntity.getNxDgCartonUnit().trim())) {
//                    order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
//                    System.out.println("使用大包装单价，打印规格设置为: " + disGoodsEntity.getNxDgCartonUnit());
//                } else {
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
//                }
//                order.setNxDoPrice(disGoodsEntity.getNxDgWillPriceTwo());
//                order.setNxDoExpectPrice(disGoodsEntity.getNxDgWillPriceTwo());
//                order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceTwo());
//                order.setNxDoSubtotal(subtotal.toString());
//                order.setNxDoCostSubtotal(costSubtotal.toString());
//                order.setNxDoCostPriceLevel("2");
//                order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceTwoUpdate());
//                order.setNxDoProfitSubtotal(profit.toString());
//                order.setNxDoProfitScale(scaleB.toString());
//
//            } else {
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
//                order.setNxDoCostPriceLevel("1");
//                order.setNxDoSubtotal("0");
//                order.setNxDoCostSubtotal("0");
//                System.out.println("jimimhuaaa" + disGoodsEntity.getNxDgWillPriceOne());
//                if (disGoodsEntity.getNxDgWillPriceOne() != null && !disGoodsEntity.getNxDgWillPriceOne().trim().isEmpty()) {
//                    System.out.println("jieghuasu?????????" + order.getNxDoPrice());
//                    order.setNxDoPrice(disGoodsEntity.getNxDgWillPriceOne());
//                    order.setNxDoExpectPrice(disGoodsEntity.getNxDgWillPriceOne());
//                }
//                if (disGoodsEntity.getNxDgBuyingPriceOne() != null && !disGoodsEntity.getNxDgBuyingPriceOne().trim().isEmpty()) {
//                    order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceOneUpdate());
//                    order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceOne());
//                }
//            }
//        } else {
//            // 初始设置打印规格：如果订单规格等于大包装单位，则设置为大包装单位
//            // 支持智能匹配：件=箱，盒=箱等同义词
//            System.out.println("======== 设置打印规格(printStandard)开始 ========");
//            System.out.println("订单ID: " + order.getNxDepartmentOrdersId());
//            System.out.println("订单规格(nxDoStandard): " + order.getNxDoStandard());
//            System.out.println("商品大包装单位(nxDgCartonUnit): " + disGoodsEntity.getNxDgCartonUnit());
//            System.out.println("商品标准规格(nxDgGoodsStandardname): " + disGoodsEntity.getNxDgGoodsStandardname());
//            System.out.println("设置前 printStandard: " + order.getNxDoPrintStandard());
//
//            if (disGoodsEntity.getNxDgCartonUnit() != null
//                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
//                    && order.getNxDoStandard() != null
//                    && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim())) {
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
//                System.out.println("✅ [printStandard] 已设置为大包装单位: " + disGoodsEntity.getNxDgCartonUnit());
//            } else {
//            order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
//                System.out.println("✅ [printStandard] 已设置为商品标准规格: " + disGoodsEntity.getNxDgGoodsStandardname());
//            }
//            System.out.println("设置后 printStandard: " + order.getNxDoPrintStandard());
//            System.out.println("======== 设置打印规格(printStandard)结束 ========");
//            System.out.println("======== 从货架获取价格开始 ========");
//            System.out.println("订单ID: " + order.getNxDepartmentOrdersId());
//            System.out.println("商品ID: " + disGoodsEntity.getNxDistributerGoodsId());
//            System.out.println("商品名称: " + disGoodsEntity.getNxDgGoodsName());
//            System.out.println("商品名称12: " + disGoodsEntity.getNxDgCartonUnit());
//            System.out.println("商品名称3333: " + disGoodsEntity.getNxDgCartonUnit());
//            System.out.println("订单规格: " + order.getNxDoStandard());
//            System.out.println("订单规格ppppp: " + order.getNxDoPrintStandard());
//            System.out.println("商品规格: " + disGoodsEntity.getNxDgGoodsStandardname());
//
//            Integer nxDistributerGoodsId = disGoodsEntity.getNxDistributerGoodsId();
//            Map<String, Object> map = new HashMap<>();
//            map.put("disGoodsId", nxDistributerGoodsId);
//            map.put("restWeight", 0);
//
//            System.out.println("查询货架库存参数: " + map);
//            List<NxDistributerGoodsShelfStockEntity> stockEntities = nxDisGoodsShelfStockService.queryShelfStockListByParams(map);
//            System.out.println("查询到库存批次数量: " + stockEntities.size());
//
//            if (stockEntities.size() > 0) {
//                NxDistributerGoodsShelfStockEntity nxDistributerGoodsShelfStockEntity = stockEntities.get(stockEntities.size() - 1);
//                System.out.println("--- 最后一个库存批次信息 ---");
//                System.out.println("库存批次ID: " + nxDistributerGoodsShelfStockEntity.getNxDistributerGoodsShelfStockId());
//                System.out.println("成本价(nxDgssPrice): " + nxDistributerGoodsShelfStockEntity.getNxDgssPrice());
//                System.out.println("外包装成本价(nxDgssPriceCarton): " + nxDistributerGoodsShelfStockEntity.getNxDgssPriceCarton());
//                System.out.println("销售价(nxDgssSellingPrice): " + nxDistributerGoodsShelfStockEntity.getNxDgssSellingPrice());
//                System.out.println("外包装销售价(nxDgssSellingPriceCarton): " + nxDistributerGoodsShelfStockEntity.getNxDgssSellingPriceCarton());
//                System.out.println("剩余重量: " + nxDistributerGoodsShelfStockEntity.getNxDgssRestWeight());
//                System.out.println("订单规格: " + order.getNxDoStandard());
//                System.out.println("商品外包装单位: " + disGoodsEntity.getNxDgCartonUnit());
//
//                // 判断订单规格是否与外包装单位匹配
//                boolean useCartonPrice = false;
//                if (disGoodsEntity.getNxDgCartonUnit() != null
//                        && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
//                        && order.getNxDoStandard() != null
//                        && order.getNxDoStandard().trim().equals(disGoodsEntity.getNxDgCartonUnit().trim())) {
//                    useCartonPrice = true;
//                    System.out.println("✅ 订单规格与外包装单位匹配，将使用外包装单价");
//                }
//
//                // 根据匹配情况选择对应的单价
//                String sellingPrice = null;
//                String costPrice = null;
//
//                if (useCartonPrice) {
//                    // 使用外包装单价
//                    if (nxDistributerGoodsShelfStockEntity.getNxDgssSellingPriceCarton() != null
//                            && !nxDistributerGoodsShelfStockEntity.getNxDgssSellingPriceCarton().trim().isEmpty()) {
//                        sellingPrice = nxDistributerGoodsShelfStockEntity.getNxDgssSellingPriceCarton();
//                        System.out.println("使用外包装建议零售价: " + sellingPrice);
//                    }
//                    if (nxDistributerGoodsShelfStockEntity.getNxDgssPriceCarton() != null
//                            && !nxDistributerGoodsShelfStockEntity.getNxDgssPriceCarton().trim().isEmpty()) {
//                        costPrice = nxDistributerGoodsShelfStockEntity.getNxDgssPriceCarton();
//                        System.out.println("使用外包装采购单价: " + costPrice);
//                    }
//                } else {
//                    // 使用最小单位单价
//                    if (nxDistributerGoodsShelfStockEntity.getNxDgssSellingPrice() != null
//                            && !nxDistributerGoodsShelfStockEntity.getNxDgssSellingPrice().trim().isEmpty()) {
//                        sellingPrice = nxDistributerGoodsShelfStockEntity.getNxDgssSellingPrice();
//                        System.out.println("使用最小单位建议零售价: " + sellingPrice);
//                    }
//                    if (nxDistributerGoodsShelfStockEntity.getNxDgssPrice() != null
//                            && !nxDistributerGoodsShelfStockEntity.getNxDgssPrice().trim().isEmpty()) {
//                        costPrice = nxDistributerGoodsShelfStockEntity.getNxDgssPrice();
//                        System.out.println("使用最小单位采购单价: " + costPrice);
//                    }
//                }
//
//                // 设置订单价格
//                if (sellingPrice != null) {
//                    System.out.println("✅ 找到销售价，开始设置订单价格");
//                    order.setNxDoPrice(sellingPrice);
//                    if (costPrice != null) {
//                        order.setNxDoCostPrice(costPrice);
//                    }
//                    // 如果使用了大包装单价，设置打印规格为大包装单位
//                    if (useCartonPrice && disGoodsEntity.getNxDgCartonUnit() != null
//                            && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()) {
//                        order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
//                        System.out.println("使用大包装单价，打印规格设置为: " + disGoodsEntity.getNxDgCartonUnit());
//                    }
//                    System.out.println("订单销售价已设置为: " + order.getNxDoPrice());
//                    System.out.println("订单成本价已设置为: " + order.getNxDoCostPrice());
//                } else {
//
//                    System.out.println("⚠️ 销售价为null，不设置价格");
//                }
//            }
//
//            System.out.println("rodstntn" + order.getNxDoStandard() + "disganttt" + disGoodsEntity.getNxDgGoodsStandardname());
//            // 检查订单规格是否等于商品标准规格，或者订单规格是否匹配大包装单位（智能匹配：件=箱）
//            boolean isStandardMatch = order.getNxDoStandard() != null
//                    && order.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname());
//            boolean isCartonMatch = disGoodsEntity.getNxDgCartonUnit() != null
//                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
//                    && order.getNxDoStandard() != null
//                    && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim());
//
//            if (isStandardMatch || isCartonMatch) {
//                System.out.println("订单规格匹配: isStandardMatch=" + isStandardMatch + ", isCartonMatch=" + isCartonMatch);
//                order.setNxDoWeight(order.getNxDoQuantity());
//
//                // 检查价格是否已设置
//                if (order.getNxDoPrice() != null && !order.getNxDoPrice().trim().isEmpty()) {
//
//                    BigDecimal doQuantity = new BigDecimal(order.getNxDoQuantity());
//                    BigDecimal willPrice = new BigDecimal(order.getNxDoPrice());
//
//                    // 判断是否使用外包装单价计算（支持智能匹配：件=箱）
//                    boolean useCartonPriceForCalc = false;
//                    Integer itemsPerCartonForCalc = null;
//                    if (disGoodsEntity.getNxDgCartonUnit() != null
//                            && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
//                            && order.getNxDoStandard() != null
//                            && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim())
//                            && disGoodsEntity.getNxDgItemsPerCarton() != null
//                            && disGoodsEntity.getNxDgItemsPerCarton() > 0) {
//                        useCartonPriceForCalc = true;
//                        itemsPerCartonForCalc = disGoodsEntity.getNxDgItemsPerCarton();
//                        System.out.println("订单规格匹配外包装单位（智能匹配），计算时将数量转换为箱数: " + doQuantity + "个 ÷ " + itemsPerCartonForCalc + " = " + doQuantity.divide(new BigDecimal(itemsPerCartonForCalc), 4, BigDecimal.ROUND_HALF_UP) + "箱");
//                    }
//
//                    BigDecimal subtotal;
//                    BigDecimal costSubtotal = null;
//                    if (useCartonPriceForCalc && itemsPerCartonForCalc != null) {
//                        // 使用外包装单价：需要将数量转换为箱数
//                        BigDecimal cartonCount = doQuantity.divide(new BigDecimal(itemsPerCartonForCalc), 4, BigDecimal.ROUND_HALF_UP);
//                        subtotal = cartonCount.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//                        System.out.println("使用外包装单价计算: " + cartonCount + "箱 × " + willPrice + "元/箱 = " + subtotal + "元");
//
//                    if (order.getNxDoCostPrice() != null && !order.getNxDoCostPrice().trim().isEmpty()) {
//                        BigDecimal cosPrice = new BigDecimal(order.getNxDoCostPrice());
//                            costSubtotal = cartonCount.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//                            System.out.println("使用外包装成本价计算: " + cartonCount + "箱 × " + cosPrice + "元/箱 = " + costSubtotal + "元");
//                        }
//                    } else {
//                        // 使用最小单位单价：直接相乘
//                        subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//                        System.out.println("使用最小单位单价计算: " + doQuantity + " × " + willPrice + " = " + subtotal);
//
//                        if (order.getNxDoCostPrice() != null && !order.getNxDoCostPrice().trim().isEmpty()) {
//                            BigDecimal cosPrice = new BigDecimal(order.getNxDoCostPrice());
//                            costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//                        }
//                    }
//
//                    order.setNxDoSubtotal(subtotal.toString());
//
//                    if (costSubtotal != null) {
//                        BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
//                        BigDecimal scaleB = BigDecimal.ZERO;
//                        if (subtotal.compareTo(BigDecimal.ZERO) != 0) {
//                            scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
//                        }
//                        System.out.println("成本小计: " + costSubtotal);
//                        System.out.println("销售小计: " + subtotal);
//                        System.out.println("利润: " + profit);
//                        System.out.println("利润率: " + scaleB + "%");
//                        order.setNxDoCostSubtotal(costSubtotal.toString());
//                        order.setNxDoProfitSubtotal(profit.toString());
//                        order.setNxDoProfitScale(scaleB.toString());
//                    }
//                    System.out.println("✅ 订单金额计算完成");
//                }
//            }
//        }
//    }
//
//

    @RequestMapping(value = "/searchGoods", method = RequestMethod.POST)
    @ResponseBody
//    public R searchGoods(@RequestBody List<NxDepartmentOrdersEntity> orderList) {
//        List<NxDepartmentOrdersEntity> returnList = new ArrayList<>();
//        for (NxDepartmentOrdersEntity ordersEntity : orderList) {
//            System.out.println("zahuishsishs" + ordersEntity.getNxDoGoodsName());
//            if (ordersEntity.getNxDoStatus() == -2) {
//
//                //没有修改商品
//                if (ordersEntity.getNxDoDisGoodsId() == null) {
//
//                    String goodsName = ordersEntity.getNxDoGoodsName();
//                    Map<String, Object> mapZero = new HashMap<>();
//                    mapZero.put("disId", ordersEntity.getNxDoDistributerId());
//                    mapZero.put("searchStr", goodsName);
//                    mapZero.put("standard", ordersEntity.getNxDoStandard());
//                    System.out.println("mapzreorororor" + mapZero);
//                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
//                    System.out.println("resultteslsutltlt----zero" + distributerGoodsEntitiesZero.size());
//                    // 一级 完全相同
//                    if (distributerGoodsEntitiesZero.size() == 0) {
//                        Map<String, Object> mapOne = new HashMap<>();
//                        mapOne.put("disId", ordersEntity.getNxDoDistributerId());
//                        mapOne.put("searchStr", goodsName);
//                        List<NxDistributerGoodsEntity> distributerGoodsEntitiesOne = nxDistributerGoodsService.queryDisGoodsByName(mapOne);
//                        System.out.println("resultteslsutltlt----one" + distributerGoodsEntitiesOne.size());
//                        // 二级 相同
//                        if (distributerGoodsEntitiesOne.size() == 0) {
//                            //1, 查拼音
//                            String pinyinString = goodsName;
//// 如果包含汉字才转换，否则直接用原名
//                            if (goodsName.matches(".*[\u4E00-\u9FFF]+.*")) {
//                                pinyinString = hanziToPinyin(goodsName);
//                            }
//                            Map<String, Object> mapTwo = new HashMap<>();
//                            mapTwo.put("disId", ordersEntity.getNxDoDistributerId());
//                            mapTwo.put("searchPinyin", pinyinString);
//                            List<NxDistributerGoodsEntity> disGoodsByNamePinyin = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
//                            System.out.println("resultteslsutltlt----pinyin" + disGoodsByNamePinyin.size());
//
//                            // 三级 相同
//                            if (disGoodsByNamePinyin.size() == 0) {
//                                //查别名
//                                Map<String, Object> mapA = new HashMap<>();
//                                mapA.put("disId", ordersEntity.getNxDoDistributerId());
//                                mapA.put("alias", goodsName);
//                                List<NxDistributerGoodsEntity> distributerGoodsEntitiesA = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
//                                System.out.println("resultteslsutltlt----aila" + distributerGoodsEntitiesA.size());
//                                // 四级 相同
//                                if (distributerGoodsEntitiesA.size() == 0) {
//
//                                    //查询 depGoosName
//                                    Map<String, Object> mapDep = new HashMap<>();
//                                    mapDep.put("depId", ordersEntity.getNxDoDepartmentId());
//                                    mapDep.put("name", goodsName);
//                                    System.out.println("depserareekkekekeekke" + mapDep);
//                                    List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntityList = nxDepartmentDisGoodsService.queryDepartmentGoods(mapDep);
//                                    if (nxDepartmentDisGoodsEntityList.size() == 0) {
//
//                                        ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
//                                        returnList.add(aaaTemp(ordersEntity));
//
//
//                                    } else if (nxDepartmentDisGoodsEntityList.size() == 1) {
//                                        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsEntityList.get(0);
//                                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
//                                        returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
//                                    } else {
//                                        List<NxDistributerGoodsEntity> list = new ArrayList<>();
//                                        for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : nxDepartmentDisGoodsEntityList) {
//                                            Integer nxDdgDisGoodsId = departmentDisGoodsEntity.getNxDdgDisGoodsId();
//                                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
//                                            list.add(distributerGoodsEntity);
//                                        }
//                                        ordersEntity.setNxDistributerGoodsEntityList(list);
//                                        returnList.add(aaaTemp(ordersEntity));
//
//                                    }
//                                } else {
//
//                                    if (distributerGoodsEntitiesA.size() == 1) {
//                                        // 1.保存订单
//                                        NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesA.get(0);
//
//                                        //2.添加规格
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
//                                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
//                                        returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
//
//                                    } else {
//                                        ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesA);
//                                        returnList.add(aaaTemp(ordersEntity));
//                                    }
//                                }
//
//                            } else {
//
//                                if (disGoodsByNamePinyin.size() == 1) {
//                                    //1 保存订单
//                                    NxDistributerGoodsEntity disGoodsEntity = disGoodsByNamePinyin.get(0);
//
//                                    //2.添加规格
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
//
//                                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
//                                    returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
//
//                                } else {
//                                    ordersEntity.setNxDistributerGoodsEntityList(disGoodsByNamePinyin);
//                                    returnList.add(aaaTemp(ordersEntity));
//                                }
//                            }
//
//
//                        } else {
//
//                            if (distributerGoodsEntitiesOne.size() == 1) {
//                                //1 保存订单
//                                NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesOne.get(0);
//                                //添加规格
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
//                                System.out.println("oneoneneee=====111111111");
//
//                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
//                                returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
//
//                            } else {
//                                System.out.println("oneoneneee=====mrororoororoororooror");
//                                ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
//                                returnList.add(aaaTemp(ordersEntity));
//                            }
//                        }
//
//                    } else {
//                        if (distributerGoodsEntitiesZero.size() == 1) {
//                            System.out.println("zeooo=====111111111");
//                            NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesZero.get(0);
//                            returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
//                        } else {
//                            System.out.println("zeooo=====mrororoororoororooror");
//                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesZero);
//                            returnList.add(aaaTemp(ordersEntity));
//                        }
//                    }
//
//                } else {
//                    //已修改商品，按照商品 id 保存订单；
//                    Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
//                    NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
//                    //2.添加规格
//                    if (!disGoodsEntity.getNxDgGoodsStandardname().equals(ordersEntity.getNxDoStandard())) {
//                        Map<String, Object> mapStand = new HashMap<>();
//                        mapStand.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
//                        mapStand.put("standardName", ordersEntity.getNxDoStandard());
//                        List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapStand);
//                        if (distributerStandardEntities.size() == 0) {
//                            NxDistributerStandardEntity standardEntity = new NxDistributerStandardEntity();
//                            standardEntity.setNxDsDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
//                            standardEntity.setNxDsStandardName(ordersEntity.getNxDoStandard());
//                            nxDistributerStandardService.save(standardEntity);
//                        }
//                    }
//                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
//                    returnList.add(choiceGoodsOrder(ordersEntity, distributerGoodsEntity));
//                }
//            } else {
//                returnList.add(ordersEntity);
//            }
//        }
//
//        return R.ok().put("data", returnList);
//    }
/**
 * 将订单添加到返回列表，同时保存对应的原始订单索引
 *
 * @param returnList 返回列表
 * @param returnListOrderIndexList 返回列表对应的原始订单索引列表
 * @param order 订单实体
 * @param orderIndex 原始订单索引
 */
    private void addToReturnList(List<NxDepartmentOrdersEntity> returnList,
                                 List<Integer> returnListOrderIndexList,
                                 NxDepartmentOrdersEntity order,
                                 int orderIndex) {
        returnList.add(order);
        returnListOrderIndexList.add(orderIndex);
    }
//    private NxDepartmentOrdersEntity saveOneOrderCash(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
//        System.out.println("saveONeOrderereerereeqonenenneorere");
//        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
//        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
//        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
//        order.setNxDoArriveDate(formatWhatDate(0));
//        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
//        order.setNxDoNxGoodsId(disGoodsEntity.getNxDgNxGoodsId());
//        order.setNxDoNxGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
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
//        order.setNxDoPurchaseGoodsId(disGoodsEntity.getNxDgPurchaseAuto());
//        order.setNxDoArriveWhatDay(getWeek(0));
//        order.setNxDoCostPriceLevel("1");
//
//        // 使用完善的价格处理逻辑（平台型/非平台型分支、货架库存查询、规格匹配等）
////        processOrderPrice(order, disGoodsEntity);
//
//        nxDepartmentOrdersService.save(order);
//
//        //auto
//        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
//            savePurGoodsAuto(order);
//        }
//        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
//        order.setNxDistributerGoodsEntity(distributerGoodsEntity);
//        return order;
//    }



//    @RequestMapping(value = "/pasteSearchGoodsForShelf", method = RequestMethod.POST)
//    @ResponseBody
//    public R pasteSearchGoodsForShelf(@RequestBody JSONObject payload) {
//        Integer disId = payload.getInteger("disId");
//        Integer shelfId = payload.getInteger("shelfId");
//        Integer startSort = payload.getInteger("startSort");
//        JSONArray itemsArray = payload.getJSONArray("items");
//
//        System.out.println("[pasteSearchGoodsForShelf] disId=" + disId + ", shelfId=" + shelfId
//                + ", startSort=" + startSort + ", itemsSize=" + (itemsArray == null ? 0 : itemsArray.size()));
//        if (itemsArray != null) {
//            System.out.println("[pasteSearchGoodsForShelf] 原始 items=" + itemsArray.toJSONString());
//        }
//
//        if (disId == null || shelfId == null || itemsArray == null || itemsArray.isEmpty()) {
//            return R.error(-1, "参数错误: 缺少 disId/shelfId/items");
//        }
//
//        int currentSort = calculateShelfStartSort(shelfId, startSort);
//        List<JSONObject> resultItems = new ArrayList<>();
//
//        for (int index = 0; index < itemsArray.size(); index++) {
//            JSONObject item = itemsArray.getJSONObject(index);
//            JSONObject resultItem = new JSONObject();
//            if (item != null) {
//                resultItem.putAll(item);
//            }
//
//            String goodsName = item == null ? null : item.getString("nxDgGoodsName");
//            String standard = item == null ? null : item.getString("nxDgGoodsStandardname");
//            String standardWeight = item == null ? null : item.getString("nxDgGoodsStandardWeight");
//            System.out.println("[pasteSearchGoodsForShelf] 处理第" + (index + 1) + "条, goodsName=" + goodsName
//                    + ", targetSort=" + currentSort);
//
//            if (isBlank(goodsName)) {
//                resultItem.put("status", "failed");
//                resultItem.put("message", "缺少商品名称");
//                resultItems.add(resultItem);
//                continue;
//            }
//
//            if (isBlank(standard)) {
//                resultItem.put("status", "failed");
//                resultItem.put("message", "缺少规格 standard");
//                resultItems.add(resultItem);
//                continue;
//            }
//
//            if (item != null) {
//
//                item.put("nxDgGoodsStandardname", standard.trim());
//                item.put("nxDgGoodsStandardWeight", blankToNull(standardWeight));
//                System.out.println("woyaokkkkkk" + item);
//            }
//
//            GoodsMatchResult disMatch = matchDistributerGoods(disId, item);
//            if (disMatch.status == MatchStatus.ERROR) {
//                resultItem.put("status", "failed");
//                resultItem.put("message", disMatch.message);
//                resultItems.add(resultItem);
//                continue;
//            }
//            if (disMatch.status == MatchStatus.CONFLICT) {
//                resultItem.put("status", "conflict");
//                resultItem.put("message", disMatch.message);
//                resultItem.put("candidates", buildDisCandidateArray(disMatch.candidates));
//                resultItems.add(resultItem);
//                continue;
//            }
//
//            NxDistributerGoodsEntity disGoods = disMatch.disGoods;
//            boolean downloaded = false;
//            boolean createdTemp = false;
//
//            if (disGoods == null) {
//                GoodsMatchResult nxMatch = matchNxGoods(item);
//                if (nxMatch.status == MatchStatus.CONFLICT) {
//                    resultItem.put("status", "conflict");
//                    resultItem.put("message", nxMatch.message);
//                    resultItem.put("candidates", buildNxCandidateArray(nxMatch.nxCandidates));
//                    resultItems.add(resultItem);
//                    continue;
//                }
//                if (nxMatch.nxGoods != null) {
//                    try {
//                        disGoods = downloadNxGoods(disId, nxMatch.nxGoods, resultItem);
//                        downloaded = true;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        resultItem.put("status", "failed");
//                        resultItem.put("message", "下载系统商品失败:" + e.getMessage());
//                        resultItems.add(resultItem);
//                        continue;
//                    }
//                }
//            }
//
//            if (disGoods == null) {
//                try {
//                    disGoods = createTemporaryGoods(disId, item, resultItem);
//                    createdTemp = true;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    resultItem.put("status", "failed");
//                    resultItem.put("message", "创建临时商品失败:" + e.getMessage());
//                    resultItems.add(resultItem);
//                    continue;
//                }
//            }
//
//            if (disGoods == null || disGoods.getNxDistributerGoodsId() == null) {
//                resultItem.put("status", "failed");
//                resultItem.put("message", "未找到可用商品");
//                resultItems.add(resultItem);
//                continue;
//            }
//
//            ShelfInsertResult shelfResult;
//            try {
//                shelfResult = insertShelfGoods(disGoods.getNxDistributerGoodsId(), shelfId, currentSort);
//            } catch (Exception e) {
//                e.printStackTrace();
//                resultItem.put("status", "failed");
//                resultItem.put("message", "添加货架商品失败:" + e.getMessage());
//                resultItems.add(resultItem);
//                continue;
//            }
//
//            if (shelfResult == null || shelfResult.shelfGoods == null) {
//                resultItem.put("status", "failed");
//                resultItem.put("message", "添加货架商品失败: 未返回有效结果");
//                resultItems.add(resultItem);
//                continue;
//            }
//
//            if (!shelfResult.existed) {
//                currentSort++;
//            }
//
//            resultItem.put("disGoodsId", disGoods.getNxDistributerGoodsId());
//            resultItem.put("shelfGoodsId", shelfResult.shelfGoods.getNxDistributerGoodsShelfGoodsId());
//            if (shelfResult.existed) {
//                resultItem.put("status", "exists");
//                resultItem.put("message", "货架上已存在该商品，未重复添加");
//            } else {
//                resultItem.put("status", createdTemp ? "created_temp" : (downloaded ? "downloaded" : "success"));
//                resultItem.put("message", createdTemp ? "已创建临时商品并上架" : (downloaded ? "已下载系统商品并上架" : "已上架现有商品"));
//            }
//            resultItems.add(resultItem);
//        }
//
//        return R.ok()
//                .put("items", resultItems)
//                .put("nextSort", currentSort);
//    }
//
//
//




    @RequestMapping(value = "/pasteSearchGoodsForShelf", method = RequestMethod.POST)
    @ResponseBody
    public R pasteSearchGoodsForShelf(@RequestBody JSONObject payload) {
        Integer disId = payload.getInteger("disId");
        Integer shelfId = payload.getInteger("shelfId");
        Integer startSort = payload.getInteger("startSort");
        JSONArray itemsArray = payload.getJSONArray("items");

        System.out.println("[pasteSearchGoodsForShelf] disId=" + disId + ", shelfId=" + shelfId
                + ", startSort=" + startSort + ", itemsSize=" + (itemsArray == null ? 0 : itemsArray.size()));
        if (itemsArray != null) {
            System.out.println("[pasteSearchGoodsForShelf] 原始 items=" + itemsArray.toJSONString());
        }

        if (disId == null || shelfId == null || itemsArray == null || itemsArray.isEmpty()) {
            return R.error(-1, "参数错误: 缺少 disId/shelfId/items");
        }

        int currentSort = calculateShelfStartSort(shelfId, startSort);
        List<JSONObject> resultItems = new ArrayList<>();

        for (int index = 0; index < itemsArray.size(); index++) {
            JSONObject item = itemsArray.getJSONObject(index);
            JSONObject resultItem = new JSONObject();
            if (item != null) {
                resultItem.putAll(item);
            }

            String goodsName = item == null ? null : item.getString("nxDgGoodsName");
            String standard = item == null ? null : item.getString("nxDgGoodsStandardname");
            String standardWeight = item == null ? null : item.getString("nxDgGoodsStandardWeight");
            System.out.println("[pasteSearchGoodsForShelf] 处理第" + (index + 1) + "条, goodsName=" + goodsName
                    + ", targetSort=" + currentSort);

            if (isBlank(goodsName)) {
                resultItem.put("status", "failed");
                resultItem.put("message", "缺少商品名称");
                resultItems.add(resultItem);
                continue;
            }

            if (isBlank(standard)) {
                resultItem.put("status", "failed");
                resultItem.put("message", "缺少规格 standard");
                resultItems.add(resultItem);
                continue;
            }

            if (item != null) {

                item.put("nxDgGoodsStandardname", standard.trim());
                item.put("nxDgGoodsStandardWeight", blankToNull(standardWeight));
                System.out.println("woyaokkkkkk" + item);
            }

            GoodsMatchResult disMatch = matchDistributerGoods(disId, item);
            if (disMatch.status == MatchStatus.ERROR) {
                resultItem.put("status", "failed");
                resultItem.put("message", disMatch.message);
                resultItems.add(resultItem);
                continue;
            }
            if (disMatch.status == MatchStatus.CONFLICT) {
                resultItem.put("status", "conflict");
                resultItem.put("message", disMatch.message);
                resultItem.put("candidates", buildDisCandidateArray(disMatch.candidates));
                resultItems.add(resultItem);
                continue;
            }

            NxDistributerGoodsEntity disGoods = disMatch.disGoods;
            boolean downloaded = false;
            boolean createdTemp = false;

            if (disGoods == null) {
                GoodsMatchResult nxMatch = matchNxGoods(item);
                if (nxMatch.status == MatchStatus.CONFLICT) {
                    resultItem.put("status", "conflict");
                    resultItem.put("message", nxMatch.message);
                    resultItem.put("candidates", buildNxCandidateArray(nxMatch.nxCandidates));
                    resultItems.add(resultItem);
                    continue;
                }
                if (nxMatch.nxGoods != null) {
                    try {
                        disGoods = downloadNxGoods(disId, nxMatch.nxGoods, resultItem);
                        downloaded = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        resultItem.put("status", "failed");
                        resultItem.put("message", "下载系统商品失败:" + e.getMessage());
                        resultItems.add(resultItem);
                        continue;
                    }
                }
            }

            if (disGoods == null) {
                try {
                    disGoods = createTemporaryGoods(disId, item, resultItem);
                    createdTemp = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    resultItem.put("status", "failed");
                    resultItem.put("message", "创建临时商品失败:" + e.getMessage());
                    resultItems.add(resultItem);
                    continue;
                }
            }

            if (disGoods == null || disGoods.getNxDistributerGoodsId() == null) {
                resultItem.put("status", "failed");
                resultItem.put("message", "未找到可用商品");
                resultItems.add(resultItem);
                continue;
            }

            ShelfInsertResult shelfResult;
            try {
                shelfResult = insertShelfGoods(disGoods.getNxDistributerGoodsId(), shelfId, currentSort);
            } catch (Exception e) {
                e.printStackTrace();
                resultItem.put("status", "failed");
                resultItem.put("message", "添加货架商品失败:" + e.getMessage());
                resultItems.add(resultItem);
                continue;
            }

            if (shelfResult == null || shelfResult.shelfGoods == null) {
                resultItem.put("status", "failed");
                resultItem.put("message", "添加货架商品失败: 未返回有效结果");
                resultItems.add(resultItem);
                continue;
            }

            if (!shelfResult.existed) {
                currentSort++;
            }

            resultItem.put("disGoodsId", disGoods.getNxDistributerGoodsId());
            resultItem.put("shelfGoodsId", shelfResult.shelfGoods.getNxDistributerGoodsShelfGoodsId());
            if (shelfResult.existed) {
                resultItem.put("status", "exists");
                resultItem.put("message", "货架上已存在该商品，未重复添加");
            } else {
                resultItem.put("status", createdTemp ? "created_temp" : (downloaded ? "downloaded" : "success"));
                resultItem.put("message", createdTemp ? "已创建临时商品并上架" : (downloaded ? "已下载系统商品并上架" : "已上架现有商品"));
            }
            resultItems.add(resultItem);
        }

        return R.ok()
                .put("items", resultItems)
                .put("nextSort", currentSort);
    }

    /**
     * 临时商品修改后重新查询系统商品
     * 当临时商品修改了名称、规格或规格重量后，调用此接口重新查询系统商品
     * 如果找到系统商品，则下载并更新临时商品
     */
//    @RequestMapping(value = "/reSearchNxGoodsForTempGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R reSearchNxGoodsForTempGoods(@RequestBody JSONObject payload) {
//        Integer disGoodsId = payload.getInteger("disGoodsId");
//        String goodsName = payload.getString("nxDgGoodsName");
//        String standard = payload.getString("nxDgGoodsStandardname");
//        String standardWeight = payload.getString("nxDgGoodsStandardWeight");
//        String brand = payload.getString("nxDgGoodsBrand");
//        String place = payload.getString("nxDgGoodsPlace");
//
//        System.out.println("[reSearchNxGoodsForTempGoods] disGoodsId=" + disGoodsId + ", goodsName=" + goodsName
//                + ", standard=" + standard + ", standardWeight=" + standardWeight);
//
//        if (disGoodsId == null) {
//            return R.error(-1, "参数错误: 缺少 disGoodsId");
//        }
//
//        // 查询临时商品
//        NxDistributerGoodsEntity tempGoods = nxDistributerGoodsService.queryObject(disGoodsId);
//        if (tempGoods == null) {
//            return R.error(-1, "商品不存在");
//        }
//
//        // 验证是否为临时商品（临时商品的 nxDgNxGoodsId 为 null）
//        if (tempGoods.getNxDgNxGoodsId() != null) {
//            return R.error(-1, "该商品不是临时商品，无需重新查询");
//        }
//
//        // 使用传入的参数或临时商品的当前信息
//        String searchName = isBlank(goodsName) ? tempGoods.getNxDgGoodsName() : goodsName;
//        String searchStandard = isBlank(standard) ? tempGoods.getNxDgGoodsStandardname() : standard;
//        String searchWeight = isBlank(standardWeight) ? tempGoods.getNxDgGoodsStandardWeight() : standardWeight;
//        String searchBrand = isBlank(brand) ? tempGoods.getNxDgGoodsBrand() : brand;
//        String searchPlace = isBlank(place) ? tempGoods.getNxDgGoodsPlace() : place;
//
//        // 如果名称或规格为空，更新临时商品的其他字段后返回
//        if (isBlank(searchName)) {
//            // 更新其他字段
//            if (!isBlank(searchStandard)) {
//                tempGoods.setNxDgGoodsStandardname(searchStandard);
//            }
//            if (!isBlank(searchWeight)) {
//                tempGoods.setNxDgGoodsStandardWeight(searchWeight);
//            }
//            if (!isBlank(searchBrand)) {
//                tempGoods.setNxDgGoodsBrand(searchBrand);
//            }
//            if (!isBlank(searchPlace)) {
//                tempGoods.setNxDgGoodsPlace(searchPlace);
//            }
//            nxDistributerGoodsService.update(tempGoods);
//            return R.ok()
//                    .put("disGoodsId", tempGoods.getNxDistributerGoodsId())
//                    .put("message", "商品名称不能为空，已更新临时商品的其他字段");
//        }
//        if (isBlank(searchStandard)) {
//            // 更新其他字段
//            if (!isBlank(searchName)) {
//                tempGoods.setNxDgGoodsName(searchName);
//            }
//            if (!isBlank(searchWeight)) {
//                tempGoods.setNxDgGoodsStandardWeight(searchWeight);
//            }
//            if (!isBlank(searchBrand)) {
//                tempGoods.setNxDgGoodsBrand(searchBrand);
//            }
//            if (!isBlank(searchPlace)) {
//                tempGoods.setNxDgGoodsPlace(searchPlace);
//            }
//            nxDistributerGoodsService.update(tempGoods);
//            return R.ok()
//                    .put("disGoodsId", tempGoods.getNxDistributerGoodsId())
//                    .put("message", "商品规格不能为空，已更新临时商品的其他字段");
//        }
//
//        // 构建查询参数
//        JSONObject searchItem = new JSONObject();
//        searchItem.put("nxDgGoodsName", searchName);
//        searchItem.put("nxDgGoodsStandardname", searchStandard);
//        searchItem.put("nxDgGoodsStandardWeight", searchWeight);
//        searchItem.put("nxDgGoodsBrand", searchBrand);
//        searchItem.put("nxDgGoodsPlace", searchPlace);
//
//        // 查询系统商品
//        GoodsMatchResult nxMatch = matchNxGoods(searchItem);
//        if (nxMatch.status == MatchStatus.CONFLICT) {
//            // 匹配到多条，保存为临时商品，名称后添加"系统多条"
//            String newGoodsName = searchName + "系统多条";
//            tempGoods.setNxDgGoodsName(newGoodsName);
//            if (!isBlank(searchStandard)) {
//                tempGoods.setNxDgGoodsStandardname(searchStandard);
//            }
//            if (!isBlank(searchWeight)) {
//                tempGoods.setNxDgGoodsStandardWeight(searchWeight);
//            }
//            if (!isBlank(searchBrand)) {
//                tempGoods.setNxDgGoodsBrand(searchBrand);
//            }
//            if (!isBlank(searchPlace)) {
//                tempGoods.setNxDgGoodsPlace(searchPlace);
//            }
//            nxDistributerGoodsService.update(tempGoods);
//            return R.ok()
//                    .put("disGoodsId", tempGoods.getNxDistributerGoodsId())
//                    .put("candidates", buildNxCandidateArray(nxMatch.nxCandidates))
//                    .put("message", "系统商品匹配多条，已保存为临时商品（名称已添加'系统多条'）");
//        }
//        if (nxMatch.status == MatchStatus.ERROR) {
//            // 匹配错误，保存为临时商品
//            if (!isBlank(searchName)) {
//                tempGoods.setNxDgGoodsName(searchName);
//            }
//            if (!isBlank(searchStandard)) {
//                tempGoods.setNxDgGoodsStandardname(searchStandard);
//            }
//            if (!isBlank(searchWeight)) {
//                tempGoods.setNxDgGoodsStandardWeight(searchWeight);
//            }
//            if (!isBlank(searchBrand)) {
//                tempGoods.setNxDgGoodsBrand(searchBrand);
//            }
//            if (!isBlank(searchPlace)) {
//                tempGoods.setNxDgGoodsPlace(searchPlace);
//            }
//            nxDistributerGoodsService.update(tempGoods);
//            return R.ok()
//                    .put("disGoodsId", tempGoods.getNxDistributerGoodsId())
//                    .put("message", "系统商品匹配错误，已保存为临时商品: " + nxMatch.message);
//        }
//        if (nxMatch.nxGoods == null) {
//            // 未找到匹配的系统商品，保存为临时商品
//            if (!isBlank(searchName)) {
//                tempGoods.setNxDgGoodsName(searchName);
//            }
//            if (!isBlank(searchStandard)) {
//                tempGoods.setNxDgGoodsStandardname(searchStandard);
//            }
//            if (!isBlank(searchWeight)) {
//                tempGoods.setNxDgGoodsStandardWeight(searchWeight);
//            }
//            if (!isBlank(searchBrand)) {
//                tempGoods.setNxDgGoodsBrand(searchBrand);
//            }
//            if (!isBlank(searchPlace)) {
//                tempGoods.setNxDgGoodsPlace(searchPlace);
//            }
//            nxDistributerGoodsService.update(tempGoods);
//            return R.ok()
//                    .put("disGoodsId", tempGoods.getNxDistributerGoodsId())
//                    .put("message", "未找到匹配的系统商品，已保存为临时商品");
//        }
//
//        // 找到系统商品，检查是否已下载
//        Integer disId = tempGoods.getNxDgDistributerId();
//        Map<String, Object> checkParams = new HashMap<>();
//        checkParams.put("disId", disId);
//        checkParams.put("goodsId", nxMatch.nxGoods.getNxGoodsId());
//        List<NxDistributerGoodsEntity> exists = nxDistributerGoodsService.queryDisGoodsByParams(checkParams);
//
//        NxGoodsEntity detail = nxGoodsService.queryObject(nxMatch.nxGoods.getNxGoodsId());
//
//        if (exists != null && !exists.isEmpty()) {
//            // 系统商品已下载，直接更新临时商品为系统商品
//            System.out.println("[reSearchNxGoodsForTempGoods] 系统商品已下载，直接更新临时商品");
//            updateTempGoodsToSystemGoods(tempGoods, detail);
//            nxDistributerGoodsService.update(tempGoods);
//            // 复制规格和别名（如果还没有的话）
//            copyStandardsAndAlias(tempGoods, detail);
//            return R.ok().put("disGoodsId", tempGoods.getNxDistributerGoodsId())
//                    .put("nxGoodsId", nxMatch.nxGoods.getNxGoodsId())
//                    .put("message", "已更新临时商品为系统商品（系统商品已存在）");
//        } else {
//            // 系统商品未下载，先下载系统商品
//            try {
//                JSONObject resultItem = new JSONObject();
//                NxDistributerGoodsEntity downloadedGoods = downloadNxGoods(disId, nxMatch.nxGoods, resultItem);
//                System.out.println("[reSearchNxGoodsForTempGoods] 下载系统商品成功，disGoodsId=" + downloadedGoods.getNxDistributerGoodsId());
//
//                // 如果下载的商品就是临时商品本身（理论上不会发生，因为临时商品没有 nxDgNxGoodsId）
//                // 但 downloadNxGoods 会检查是否已存在，如果临时商品已存在同名商品，可能会返回临时商品
//                // 所以我们需要更新临时商品
//                if (downloadedGoods.getNxDistributerGoodsId().equals(disGoodsId)) {
//                    // 临时商品就是下载的商品，已经更新好了
//                    return R.ok().put("disGoodsId", tempGoods.getNxDistributerGoodsId())
//                            .put("nxGoodsId", nxMatch.nxGoods.getNxGoodsId())
//                            .put("message", "已更新临时商品为系统商品");
//                } else {
//                    // 下载的商品是新商品，更新临时商品关联到系统商品
//                    updateTempGoodsToSystemGoods(tempGoods, detail);
//                    nxDistributerGoodsService.update(tempGoods);
//                    // 复制规格和别名
//                    copyStandardsAndAlias(tempGoods, detail);
//                    return R.ok().put("disGoodsId", tempGoods.getNxDistributerGoodsId())
//                            .put("nxGoodsId", nxMatch.nxGoods.getNxGoodsId())
//                            .put("message", "已更新临时商品为系统商品");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                // 下载失败，保存为临时商品
//                if (!isBlank(searchName)) {
//                    tempGoods.setNxDgGoodsName(searchName);
//                }
//                if (!isBlank(searchStandard)) {
//                    tempGoods.setNxDgGoodsStandardname(searchStandard);
//                }
//                if (!isBlank(searchWeight)) {
//                    tempGoods.setNxDgGoodsStandardWeight(searchWeight);
//                }
//                if (!isBlank(searchBrand)) {
//                    tempGoods.setNxDgGoodsBrand(searchBrand);
//                }
//                if (!isBlank(searchPlace)) {
//                    tempGoods.setNxDgGoodsPlace(searchPlace);
//                }
//                nxDistributerGoodsService.update(tempGoods);
//                return R.ok()
//                        .put("disGoodsId", tempGoods.getNxDistributerGoodsId())
//                        .put("message", "下载系统商品失败，已保存为临时商品: " + e.getMessage());
//            }
//        }
//    }

    /**
     * 更新临时商品为系统商品
     */
    private void updateTempGoodsToSystemGoods(NxDistributerGoodsEntity tempGoods, NxGoodsEntity detail) {
        tempGoods.setNxDgNxGoodsId(detail.getNxGoodsId());
        tempGoods.setNxDgGoodsName(detail.getNxGoodsName());
        tempGoods.setNxDgGoodsStandardname(detail.getNxGoodsStandardname());
        tempGoods.setNxDgGoodsStandardWeight(detail.getNxGoodsStandardWeight());
        tempGoods.setNxDgGoodsBrand(detail.getNxGoodsBrand());
        tempGoods.setNxDgGoodsPlace(detail.getNxGoodsPlace());
        tempGoods.setNxDgGoodsDetail(detail.getNxGoodsDetail());
        tempGoods.setNxDgGoodsFile(detail.getNxGoodsFile());
        tempGoods.setNxDgGoodsFileLarge(detail.getNxGoodsFileBig());
        tempGoods.setNxDgGoodsPinyin(detail.getNxGoodsPinyin());
        tempGoods.setNxDgGoodsPy(detail.getNxGoodsPy());
        tempGoods.setNxDgGoodsStatus(1);
        tempGoods.setNxDgQuantityDays(detail.getNxGoodsQuantityDays());
        tempGoods.setNxDgGoodsSort(detail.getNxGoodsSort());
        tempGoods.setNxDgGoodsSonsSort(detail.getNxGoodsSonsSort());

        if (detail.getFatherGoods() != null) {
            tempGoods.setNxDgNxFatherId(detail.getFatherGoods().getNxGoodsId());
            tempGoods.setNxDgNxFatherName(detail.getFatherGoods().getNxGoodsName());
            tempGoods.setNxDgNxFatherImg(detail.getFatherGoods().getNxGoodsFile());
        }
        if (detail.getGrandGoods() != null) {
            tempGoods.setNxDgNxGrandId(detail.getGrandGoods().getNxGoodsId());
            tempGoods.setNxDgNxGrandName(detail.getGrandGoods().getNxGoodsName());
        }
        if (detail.getGreatGrandGoods() != null) {
            tempGoods.setNxDgNxGreatGrandId(detail.getGreatGrandGoods().getNxGoodsId());
            tempGoods.setNxDgNxGreatGrandName(detail.getGreatGrandGoods().getNxGoodsName());
            tempGoods.setNxDgNxGoodsFatherColor(detail.getGreatGrandGoods().getColor());
        }

        // 更新层级关系
        assignDistributerHierarchy(tempGoods, detail);
    }

    private int calculateShelfStartSort(Integer shelfId, Integer startSort) {
        if (startSort != null && startSort > 0) {
            return startSort;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("shelfId", shelfId);
        List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsEntities = shelfGoodsService.queryShelfForGoodsByParams(map);
        if (shelfGoodsEntities == null || shelfGoodsEntities.isEmpty()) {
            return 1;
        }
        NxDistributerGoodsShelfGoodsEntity last = shelfGoodsEntities.get(shelfGoodsEntities.size() - 1);
        return last.getNxDgsgSort() == null ? 1 : last.getNxDgsgSort() + 1;
    }

    private GoodsMatchResult matchDistributerGoods(Integer disId, JSONObject item) {
        GoodsMatchResult result = new GoodsMatchResult();
        String name = item.getString("nxDgGoodsName");
        String standard = item.getString("nxDgGoodsStandardname");
        String weight = item.getString("nxDgGoodsStandardWeight");
        String brand = item.getString("nxDgGoodsBrand");

        // 用于判断是否为同一商品的4个字段：商品名称、规格、规格重量、品牌
        String matchName = isBlank(name) ? null : name.trim();
        String matchStandard = isBlank(standard) ? null : standard.trim();
        String matchWeight = isBlank(weight) ? null : weight.trim();
        String matchBrand = isBlank(brand) ? null : brand.trim();

        Map<String, Object> params = new HashMap<>();
        params.put("disId", disId);
        params.put("searchStr", name);
        // 规格是必填字段，完全匹配
        if (!isBlank(standard)) {
            params.put("standard", standard.trim());
        }
        if (!isBlank(weight)) {
            params.put("standardWeight", weight.trim());
        }
        if (!isBlank(brand)) {
            params.put("brand", brand.trim());
        }

        System.out.println("[matchDisGoods] 精准查询参数=" + params);
        List<NxDistributerGoodsEntity> exactList = nxDistributerGoodsService.queryDisGoodsByName(params);
        System.out.println("[matchDisGoods] 精准查询结果条数=" + (exactList == null ? 0 : exactList.size()));
        // 使用pickDisGoods进行严格匹配（名称、规格、规格重量、品牌都要匹配）
        NxDistributerGoodsEntity pick = pickDisGoods(exactList, matchName, matchStandard, matchWeight, matchBrand);
        if (pick != null) {
            result.status = MatchStatus.FOUND;
            result.disGoods = pick;
            result.message = "名称+规格匹配";
            return result;
        }
        // 如果经过严格匹配后有多条，才返回CONFLICT
        List<NxDistributerGoodsEntity> filteredExactList = filterDisGoodsStrict(exactList, matchName, matchStandard, matchWeight, matchBrand);
        if (filteredExactList != null && filteredExactList.size() > 1) {
            result.status = MatchStatus.CONFLICT;
            result.message = "名称+规格匹配多条";
            result.candidates = filteredExactList;
            return result;
        }

        String pinyin = hanziToPinyin(name);
        Map<String, Object> pinyinParams = new HashMap<>();
        pinyinParams.put("disId", disId);
        pinyinParams.put("searchPinyin", pinyin);
        // 规格是必填字段，完全匹配
        if (!isBlank(standard)) {
            pinyinParams.put("standard", standard.trim());
        }
        // 规格重量和品牌也要一起查询，如果不同，则视为新商品
        if (!isBlank(weight)) {
            pinyinParams.put("standardWeight", weight.trim());
        }
        if (!isBlank(brand)) {
            pinyinParams.put("brand", brand.trim());
        }
        System.out.println("[matchDisGoods] 拼音查询参数=" + pinyinParams);
        List<NxDistributerGoodsEntity> pinyinList = nxDistributerGoodsService.queryDisGoodsByNamePinyin(pinyinParams);
        System.out.println("[matchDisGoods] 拼音查询结果条数=" + (pinyinList == null ? 0 : pinyinList.size()));
        // 使用pickDisGoods进行严格匹配（名称、规格、规格重量、品牌都要匹配）
        pick = pickDisGoods(pinyinList, matchName, matchStandard, matchWeight, matchBrand);
        if (pick != null) {
            result.status = MatchStatus.FOUND;
            result.disGoods = pick;
            result.message = "拼音匹配";
            return result;
        }
        // 如果经过严格匹配后有多条，才返回CONFLICT
        List<NxDistributerGoodsEntity> filteredPinyinList = filterDisGoodsStrict(pinyinList, matchName, matchStandard, matchWeight, matchBrand);
        if (filteredPinyinList != null && filteredPinyinList.size() > 1) {
            result.status = MatchStatus.CONFLICT;
            result.message = "拼音匹配多条";
            result.candidates = filteredPinyinList;
            return result;
        }

        Map<String, Object> aliasParams = new HashMap<>();
        aliasParams.put("disId", disId);
        aliasParams.put("alias", name);
        System.out.println("[matchDisGoods] 别名查询参数=" + aliasParams);
        List<NxDistributerGoodsEntity> aliasList = nxDistributerGoodsService.queryDisGoodsByAlias(aliasParams);
        System.out.println("[matchDisGoods] 别名查询结果条数=" + (aliasList == null ? 0 : aliasList.size()));
        // 使用pickDisGoods进行严格匹配（名称、规格、规格重量、品牌都要匹配）
        pick = pickDisGoods(aliasList, matchName, matchStandard, matchWeight, matchBrand);
        if (pick != null) {
            result.status = MatchStatus.FOUND;
            result.disGoods = pick;
            result.message = "配送商别名匹配";
            return result;
        }
        // 如果经过严格匹配后有多条，才返回CONFLICT
        List<NxDistributerGoodsEntity> filteredAliasList = filterDisGoodsStrict(aliasList, matchName, matchStandard, matchWeight, matchBrand);
        if (filteredAliasList != null && filteredAliasList.size() > 1) {
            result.status = MatchStatus.CONFLICT;
            result.message = "配送商别名匹配多条";
            result.candidates = filteredAliasList;
            return result;
        }

        result.status = MatchStatus.NOT_FOUND;
        result.message = "配送商商品未命中";
        return result;
    }

    private GoodsMatchResult matchNxGoods(JSONObject item) {
        GoodsMatchResult result = new GoodsMatchResult();
        String name = item.getString("nxDgGoodsName");
        String standard = item.getString("nxDgGoodsStandardname");
        String weight = item.getString("nxDgGoodsStandardWeight");
        String brand = item.getString("nxDgGoodsBrand");
        String place = item.getString("nxDgGoodsPlace");

        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("level", 3);
        if (!isBlank(standard)) {
            params.put("standard", standard.trim());
        }
        if (!isBlank(weight)) {
            params.put("standardWeight", weight.trim());
        }
        if (!isBlank(brand)) {
            params.put("brand", brand.trim());
        }


        System.out.println("[matchNxGoods] 精准查询参数=" + params);
        List<NxGoodsEntity> list = nxGoodsService.queryNxGoodsByParams(params);
        System.out.println("[matchNxGoods] 精准查询结果条数=" + (list == null ? 0 : list.size()));
        if (list == null || list.isEmpty()) {
            Map<String, Object> fallbackParams = new HashMap<>(params);
            fallbackParams.remove("level");
            System.out.println("[matchNxGoods] 精准查询(无level)参数=" + fallbackParams);
            list = nxGoodsService.queryNxGoodsByParams(fallbackParams);
            System.out.println("[matchNxGoods] 精准查询(无level)结果条数=" + (list == null ? 0 : list.size()));
        }

        NxGoodsEntity pick = pickNxGoods(list, standard, weight, brand);
        if (pick != null) {
            result.status = MatchStatus.FOUND;
            result.nxGoods = pick;
            result.message = "系统商品命中";
            return result;
        }
        if (list != null && list.size() > 1) {
            result.status = MatchStatus.CONFLICT;
            result.message = "系统商品匹配多条";
            result.nxCandidates = list;
            return result;
        }

        Map<String, Object> aliasParams = new HashMap<>();
        aliasParams.put("name", name);
        System.out.println("[matchNxGoods] 别名查询参数=" + aliasParams);
        List<NxGoodsEntity> aliasList = nxAliasService.queryNxGoodsByName(aliasParams);
        System.out.println("[matchNxGoods] 别名查询结果条数=" + (aliasList == null ? 0 : aliasList.size()));
        pick = pickNxGoods(aliasList, standard, weight, brand);
        if (pick != null) {
            result.status = MatchStatus.FOUND;
            result.nxGoods = pick;
            result.message = "系统别名命中";
            return result;
        }
        if (aliasList != null && aliasList.size() > 1) {
            result.status = MatchStatus.CONFLICT;
            result.message = "系统别名匹配多条";
            result.nxCandidates = aliasList;
            return result;
        }

        result.status = MatchStatus.NOT_FOUND;
        result.message = "系统商品未命中";
        return result;
    }

    private NxDistributerGoodsEntity downloadNxGoods(Integer disId, NxGoodsEntity nxGoods, JSONObject resultItem) {
        System.out.println("[pasteSearchGoodsForShelf] 下载系统商品 nxGoodsId=" + nxGoods.getNxGoodsId());

        Map<String, Object> checkParams = new HashMap<>();
        checkParams.put("disId", disId);
        checkParams.put("goodsId", nxGoods.getNxGoodsId());
        List<NxDistributerGoodsEntity> exists = nxDistributerGoodsService.queryDisGoodsByParams(checkParams);
        if (exists != null && !exists.isEmpty()) {
            resultItem.put("message", "商品已下载，直接使用");
            return exists.get(0);
        }

        NxGoodsEntity detail = nxGoodsService.queryObject(nxGoods.getNxGoodsId());
        NxDistributerGoodsEntity goods = buildBaseDisGoods(detail, disId);
        assignDistributerHierarchy(goods, detail);
        persistDisGoods(goods, detail);
        copyStandardsAndAlias(goods, detail);

        resultItem.put("downloadFromNxGoodsId", detail.getNxGoodsId());
        return goods;
    }

    private NxDistributerGoodsEntity buildBaseDisGoods(NxGoodsEntity detail, Integer disId) {
        NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();
        goods.setNxDgDistributerId(disId);
        goods.setNxDgNxGoodsId(detail.getNxGoodsId());
        goods.setNxDgGoodsName(detail.getNxGoodsName());
        goods.setNxDgGoodsStandardname(detail.getNxGoodsStandardname());
        goods.setNxDgGoodsStandardWeight(detail.getNxGoodsStandardWeight());
        goods.setNxDgGoodsBrand(detail.getNxGoodsBrand());
        goods.setNxDgGoodsPlace(detail.getNxGoodsPlace());
        goods.setNxDgGoodsDetail(detail.getNxGoodsDetail());
        goods.setNxDgGoodsFile(detail.getNxGoodsFile());
        goods.setNxDgGoodsFileLarge(detail.getNxGoodsFileBig());
        goods.setNxDgGoodsPinyin(detail.getNxGoodsPinyin());
        goods.setNxDgGoodsPy(detail.getNxGoodsPy());
        goods.setNxDgGoodsStatus(1);
        goods.setNxDgPullOff(0);
        goods.setNxDgGoodsIsHidden(0);
        goods.setNxDgPurchaseAuto(-1);
        goods.setNxDgBuyingPrice("0.1");
        goods.setNxDgWillPrice("0.1");
        goods.setNxDgBuyingPriceOne("0.1");
        goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
        goods.setNxDgWillPriceOne("0.1");
        goods.setNxDgWillPriceOneAboutPrice("0.1");
        goods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
        goods.setNxDgQuantityDays(detail.getNxGoodsQuantityDays());
        goods.setNxDgGoodsSort(detail.getNxGoodsSort());
        goods.setNxDgGoodsSonsSort(detail.getNxGoodsSonsSort());
        goods.setNxDgOutTotalWeight("0");
        goods.setNxDgCartonUnit(detail.getNxGoodsCartonUnit());
        goods.setNxDgItemsPerCarton(detail.getNxGoodsItemsPerCarton());

        if (detail.getFatherGoods() != null) {
            goods.setNxDgNxFatherId(detail.getFatherGoods().getNxGoodsId());
            goods.setNxDgNxFatherName(detail.getFatherGoods().getNxGoodsName());
            goods.setNxDgNxFatherImg(detail.getFatherGoods().getNxGoodsFile());
        }
        if (detail.getGrandGoods() != null) {
            goods.setNxDgNxGrandId(detail.getGrandGoods().getNxGoodsId());
            goods.setNxDgNxGrandName(detail.getGrandGoods().getNxGoodsName());
        }
        if (detail.getGreatGrandGoods() != null) {
            goods.setNxDgNxGreatGrandId(detail.getGreatGrandGoods().getNxGoodsId());
            goods.setNxDgNxGreatGrandName(detail.getGreatGrandGoods().getNxGoodsName());
            goods.setNxDgNxGoodsFatherColor(detail.getGreatGrandGoods().getColor());
        }
        return goods;
    }

    private void assignDistributerHierarchy(NxDistributerGoodsEntity goods, NxGoodsEntity detail) {
        Integer disId = goods.getNxDgDistributerId();
        Integer nxFatherId = goods.getNxDgNxFatherId();

        if (nxFatherId == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", disId);
            map.put("nxGoodsId", -1);
            map.put("goodsLevel", 2);
            List<NxDistributerFatherGoodsEntity> fallback = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
            if (fallback == null || fallback.isEmpty()) {
                throw new RuntimeException("未找到配送商默认父级类目");
            }
            NxDistributerFatherGoodsEntity entity = fallback.get(0);
            goods.setNxDgDfgGoodsFatherId(entity.getNxDistributerFatherGoodsId());
            goods.setNxDgDfgGoodsGrandId(entity.getNxDfgFathersFatherId());
            entity.setNxDfgGoodsAmount((entity.getNxDfgGoodsAmount() == null ? 0 : entity.getNxDfgGoodsAmount()) + 1);
            nxDistributerFatherGoodsService.update(entity);
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("nxFatherId", nxFatherId);
        List<NxDistributerGoodsEntity> sameFatherGoods = nxDistributerGoodsService.queryDisGoodsByParams(map);

        if (sameFatherGoods != null && !sameFatherGoods.isEmpty()) {
            NxDistributerGoodsEntity sample = sameFatherGoods.get(0);
            Integer fatherId = sample.getNxDgDfgGoodsFatherId();
            NxDistributerFatherGoodsEntity father = nxDistributerFatherGoodsService.queryObject(fatherId);
            if (father != null) {
                father.setNxDfgGoodsAmount((father.getNxDfgGoodsAmount() == null ? 0 : father.getNxDfgGoodsAmount()) + 1);
                nxDistributerFatherGoodsService.update(father);
            }
            goods.setNxDgDfgGoodsFatherId(fatherId);
            goods.setNxDgDfgGoodsGrandId(sample.getNxDgDfgGoodsGrandId());
        } else {
            createHierarchyForDisGoods(goods, detail);
        }
    }

    private void createHierarchyForDisGoods(NxDistributerGoodsEntity goods, NxGoodsEntity detail) {
        Integer disId = goods.getNxDgDistributerId();
        NxDistributerFatherGoodsEntity father = new NxDistributerFatherGoodsEntity();
        father.setNxDfgDistributerId(disId);
        father.setNxDfgFatherGoodsName(goods.getNxDgNxFatherName());
        father.setNxDfgFatherGoodsLevel(2);
        father.setNxDfgGoodsAmount(1);
        father.setNxDfgFatherGoodsImg(goods.getNxDgNxFatherImg());
        father.setNxDfgFatherGoodsColor(goods.getNxDgNxGoodsFatherColor());
        father.setNxDfgFatherGoodsSort(detail.getFatherGoods() == null ? 0 : detail.getFatherGoods().getNxGoodsSort());
        father.setNxDfgNxGoodsId(goods.getNxDgNxFatherId());
        nxDistributerFatherGoodsService.save(father);

        NxDistributerFatherGoodsEntity grand = ensureFatherHierarchy(disId, goods.getNxDgNxGrandName(),
                goods.getNxDgNxGrandId(), goods.getNxDgNxGoodsFatherColor(),
                detail.getGrandGoods() == null ? 0 : detail.getGrandGoods().getNxGoodsSort(), father);

        ensureGreatGrandHierarchy(disId, goods, detail, grand);

        goods.setNxDgDfgGoodsFatherId(father.getNxDistributerFatherGoodsId());
        goods.setNxDgDfgGoodsGrandId(grand.getNxDistributerFatherGoodsId());
    }
    private NxDistributerFatherGoodsEntity ensureFatherHierarchy(Integer disId, String name, Integer nxGoodsId,
                                                                 String color, Integer sort,
                                                                 NxDistributerFatherGoodsEntity father) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("fathersFatherName", name);
        List<NxDistributerFatherGoodsEntity> existed = nxDistributerFatherGoodsService.queryHasDisFathersFather(map);
        if (existed != null && !existed.isEmpty()) {
            NxDistributerFatherGoodsEntity grand = existed.get(0);
            father.setNxDfgFathersFatherId(grand.getNxDistributerFatherGoodsId());
            nxDistributerFatherGoodsService.update(father);
            return grand;
        }

        NxDistributerFatherGoodsEntity grand = new NxDistributerFatherGoodsEntity();
        grand.setNxDfgFatherGoodsName(name);
        grand.setNxDfgDistributerId(disId);
        grand.setNxDfgFatherGoodsLevel(1);
        grand.setNxDfgGoodsAmount(1);
        grand.setNxDfgFatherGoodsColor(color);
        grand.setNxDfgNxGoodsId(nxGoodsId);
        grand.setNxDfgFatherGoodsSort(sort);
        nxDistributerFatherGoodsService.save(grand);

        father.setNxDfgFathersFatherId(grand.getNxDistributerFatherGoodsId());
        nxDistributerFatherGoodsService.update(father);
        return grand;
    }

    private void ensureGreatGrandHierarchy(Integer disId, NxDistributerGoodsEntity goods,
                                           NxGoodsEntity detail, NxDistributerFatherGoodsEntity grand) {
        String greatName = goods.getNxDgNxGreatGrandName();
        if (greatName == null) {
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("fathersFatherName", greatName);
        List<NxDistributerFatherGoodsEntity> existed = nxDistributerFatherGoodsService.queryHasDisFathersFather(map);
        if (existed != null && !existed.isEmpty()) {
            NxDistributerFatherGoodsEntity great = existed.get(0);
            grand.setNxDfgFathersFatherId(great.getNxDistributerFatherGoodsId());
            nxDistributerFatherGoodsService.update(grand);
            return;
        }

        NxDistributerFatherGoodsEntity great = new NxDistributerFatherGoodsEntity();
        great.setNxDfgFatherGoodsName(greatName);
        great.setNxDfgDistributerId(disId);
        great.setNxDfgFatherGoodsLevel(0);
        great.setNxDfgFatherGoodsColor(goods.getNxDgNxGoodsFatherColor());
        great.setNxDfgNxGoodsId(goods.getNxDgNxGreatGrandId());
        if (detail.getGreatGrandGoods() != null && detail.getGreatGrandGoods().getFatherGoods() != null) {
            great.setNxDfgFatherGoodsImg(detail.getGreatGrandGoods().getFatherGoods().getNxGoodsFile());
            great.setNxDfgFatherGoodsSort(detail.getGreatGrandGoods().getFatherGoods().getNxGoodsSort());
        }
        great.setNxDfgGoodsAmount(1);
        nxDistributerFatherGoodsService.save(great);

        grand.setNxDfgFathersFatherId(great.getNxDistributerFatherGoodsId());
        nxDistributerFatherGoodsService.update(grand);
    }

    private void persistDisGoods(NxDistributerGoodsEntity goods, NxGoodsEntity detail) {
        nxDistributerGoodsService.save(goods);
    }

    private void copyStandardsAndAlias(NxDistributerGoodsEntity goods, NxGoodsEntity detail) {
        if (detail.getNxGoodsStandardEntities() != null) {
            for (NxStandardEntity standardEntity : detail.getNxGoodsStandardEntities()) {
                NxDistributerStandardEntity disStandard = new NxDistributerStandardEntity();
                disStandard.setNxDsDisGoodsId(goods.getNxDistributerGoodsId());
                disStandard.setNxDsStandardName(standardEntity.getNxStandardName());
                disStandard.setNxDsStandardError(standardEntity.getNxStandardError());
                disStandard.setNxDsStandardScale(standardEntity.getNxStandardScale());
                disStandard.setNxDsStandardFilePath(standardEntity.getNxStandardFilePath());
                disStandard.setNxDsStandardSort(standardEntity.getNxStandardSort());
                nxDistributerStandardService.save(disStandard);
            }
        }
        if (detail.getNxAliasEntities() != null) {
            for (NxAliasEntity aliasEntity : detail.getNxAliasEntities()) {
                NxDistributerAliasEntity disAlias = new NxDistributerAliasEntity();
                disAlias.setNxDaDisGoodsId(goods.getNxDistributerGoodsId());
                disAlias.setNxDaAliasName(aliasEntity.getNxAliasName());
                nxDistributerAliasService.save(disAlias);
            }
        }
    }

    private NxDistributerGoodsEntity createTemporaryGoods(Integer disId, JSONObject item, JSONObject resultItem) {
        String goodsName = item.getString("nxDgGoodsName");
        System.out.println("[pasteSearchGoodsForShelf] 创建临时商品 " + goodsName);

        // 检查是否已存在相同商品（需要匹配：名称、规格、规格重量、品牌）
        Map<String, Object> checkParams = new HashMap<>();
        checkParams.put("disId", disId);
        checkParams.put("searchStr", goodsName);
        checkParams.put("standard", item.getString("nxDgGoodsStandardname"));
        String standardWeight = blankToNull(item.getString("nxDgGoodsStandardWeight"));
        String brand = blankToNull(item.getString("nxDgGoodsBrand"));
        if (standardWeight != null) {
            checkParams.put("standardWeight", standardWeight);
        }
        if (brand != null) {
            checkParams.put("brand", brand);
        }
        List<NxDistributerGoodsEntity> existing = nxDistributerGoodsService.queryDisGoodsByName(checkParams);
        // 使用pickDisGoods的逻辑进行严格匹配
        NxDistributerGoodsEntity matched = pickDisGoods(existing, goodsName,
                item.getString("nxDgGoodsStandardname"), standardWeight, brand);
        if (matched != null) {
            System.out.println("[pasteSearchGoodsForShelf] 配送商已存在相同商品，商品ID=" + matched.getNxDistributerGoodsId() +
                    ", 名称=" + matched.getNxDgGoodsName() + ", 规格=" + matched.getNxDgGoodsStandardname() +
                    ", 规格重量=" + matched.getNxDgGoodsStandardWeight() + ", 品牌=" + matched.getNxDgGoodsBrand());
            throw new RuntimeException("配送商已存在相同商品");
        }

        NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();
        goods.setNxDgDistributerId(disId);
        goods.setNxDgGoodsName(goodsName);
        goods.setNxDgGoodsStandardname(item.getString("nxDgGoodsStandardname"));
        goods.setNxDgGoodsStandardWeight(blankToNull(item.getString("nxDgGoodsStandardWeight")));
        goods.setNxDgGoodsBrand(blankToNull(item.getString("nxDgGoodsBrand")));
        goods.setNxDgGoodsPlace(blankToNull(item.getString("nxDgGoodsPlace")));
        // 外包装相关字段
        goods.setNxDgCartonUnit(blankToNull(item.getString("nxDgCartonUnit")));
        Object itemsPerCartonObj = item.get("nxDgItemsPerCarton");
        if (itemsPerCartonObj != null) {
            String normalized = com.nongxinle.utils.CommonUtils.normalizeItemsPerCartonString(itemsPerCartonObj);
            if (normalized != null) {
                goods.setNxDgItemsPerCarton(normalized);
            }
        }
        goods.setNxDgGoodsStatus(0);
        goods.setNxDgPullOff(0);
        goods.setNxDgGoodsIsHidden(0);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("nxGoodsId", -1);
        map.put("goodsLevel", 2);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        if (fatherGoodsEntities == null || fatherGoodsEntities.isEmpty()) {
            throw new RuntimeException("未找到可用的配送商父级类目");
        }
        NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());

        goods.setNxDgPurchaseAuto(-1);
        goods.setNxDgWillPrice("0.1");
        goods.setNxDgBuyingPrice("0.1");
        goods.setNxDgBuyingPriceOne("0.1");
        goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
        goods.setNxDgWillPriceOne("0.1");
        goods.setNxDgWillPriceOneAboutPrice("0.1");
        goods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
        goods.setNxDgOutTotalWeight("0");

        String englishKuohao = getEnglishKuohao(goodsName);
        String pinyin = hanziToPinyin(englishKuohao);
        String headPinyin = getHeadStringByString(englishKuohao, false, null);
        goods.setNxDgGoodsPinyin(pinyin);
        goods.setNxDgGoodsPy(headPinyin);

        nxDistributerGoodsService.save(goods);

        Integer amount = fatherGoodsEntity.getNxDfgGoodsAmount();
        fatherGoodsEntity.setNxDfgGoodsAmount((amount == null ? 0 : amount) + 1);
        nxDistributerFatherGoodsService.update(fatherGoodsEntity);

        resultItem.put("createdTemp", true);
        return goods;
    }
    private ShelfInsertResult insertShelfGoods(Integer disGoodsId, Integer shelfId, int targetSort) {
        ShelfInsertResult result = new ShelfInsertResult();

        Map<String, Object> checkMap = new HashMap<>();
        checkMap.put("disGoodsId", disGoodsId);
        checkMap.put("shelfId", shelfId);
        List<NxDistributerGoodsShelfGoodsEntity> existed = shelfGoodsService.queryShelfForGoodsByParams(checkMap);
        if (existed != null && !existed.isEmpty()) {
            System.out.println("[pasteSearchGoodsForShelf] 货架已存在商品 disGoodsId=" + disGoodsId);
            result.existed = true;
            result.shelfGoods = existed.get(0);
            return result;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("dayuSort", targetSort - 1);
        map.put("shelfId", shelfId);
        List<NxDistributerGoodsShelfGoodsEntity> needAdjust = shelfGoodsService.queryShelfForGoodsByParams(map);
        if (needAdjust != null) {
            for (int i = 0; i < needAdjust.size(); i++) {
                NxDistributerGoodsShelfGoodsEntity entity = needAdjust.get(i);
                entity.setNxDgsgSort(targetSort + i + 1);
                shelfGoodsService.update(entity);
            }
        }

        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(disGoodsId);
        if (disGoods != null) {
            disGoods.setNxDgPurchaseAuto(-1);
            nxDistributerGoodsService.update(disGoods);
        }

        NxDistributerGoodsShelfGoodsEntity shelfGoods = new NxDistributerGoodsShelfGoodsEntity();
        shelfGoods.setNxDgsgShelfId(shelfId);
        shelfGoods.setNxDgsgDisGoodsId(disGoodsId);
        shelfGoods.setNxDgsgSort(targetSort);
        shelfGoodsService.save(shelfGoods);
        result.shelfGoods = shelfGoods;
        result.existed = false;

        // 更新该商品的重复标记（新添加后需要重新计算）
        shelfGoodsService.updateDuplicateFlagForGoods(disGoodsId);

        return result;
    }

    private static class ShelfInsertResult {
        NxDistributerGoodsShelfGoodsEntity shelfGoods;
        boolean existed;
    }

    private JSONArray buildDisCandidateArray(List<NxDistributerGoodsEntity> candidates) {
        JSONArray array = new JSONArray();
        if (candidates != null) {
            for (NxDistributerGoodsEntity candidate : candidates) {
                JSONObject obj = new JSONObject();
                obj.put("disGoodsId", candidate.getNxDistributerGoodsId());
                obj.put("name", candidate.getNxDgGoodsName());
                obj.put("standard", candidate.getNxDgGoodsStandardname());
                obj.put("brand", candidate.getNxDgGoodsBrand());
                obj.put("place", candidate.getNxDgGoodsPlace());
                array.add(obj);
            }
        }
        return array;
    }

    private JSONArray buildNxCandidateArray(List<NxGoodsEntity> candidates) {
        JSONArray array = new JSONArray();
        if (candidates != null) {
            for (NxGoodsEntity candidate : candidates) {
                JSONObject obj = new JSONObject();
                obj.put("nxGoodsId", candidate.getNxGoodsId());
                obj.put("name", candidate.getNxGoodsName());
                obj.put("standard", candidate.getNxGoodsStandardname());
                obj.put("brand", candidate.getNxGoodsBrand());
                obj.put("place", candidate.getNxGoodsPlace());
                array.add(obj);
            }
        }
        return array;
    }

    /**
     * 从商品列表中挑选匹配的配送商品
     * 判断规则：
     * 1. 商品名称（必填）：必须相同
     * 2. 规格（必填）：必须相同
     * 3. 规格重量（可选）：如果有值，必须相同；如果查询的商品为空，数据库商品也必须为空
     * 4. 品牌（可选）：如果有值，必须相同；如果查询的商品为空，数据库商品也必须为空
     * 只要有一个字段不匹配，就视为不同的商品
     */
    private NxDistributerGoodsEntity pickDisGoods(List<NxDistributerGoodsEntity> list,
                                                  String name, String standard, String weight, String brand) {
        List<NxDistributerGoodsEntity> filtered = filterDisGoodsStrict(list, name, standard, weight, brand);
        return filtered != null && filtered.size() == 1 ? filtered.get(0) : null;
    }

    /**
     * 从商品列表中过滤出严格匹配的配送商品列表
     * 判断规则同pickDisGoods，但返回所有匹配的商品列表
     */
    private List<NxDistributerGoodsEntity> filterDisGoodsStrict(List<NxDistributerGoodsEntity> list,
                                                                String name, String standard, String weight, String brand) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        // 商品名称和规格是必填的，必须相同
        // 规格重量和品牌：如果有值，必须相同；如果查询的商品为空，数据库商品也必须为空
        List<NxDistributerGoodsEntity> filtered = list.stream()
                .filter(item -> {
                    // 商品名称必须相同（必填，不应为空）
                    if (isBlank(name) || isBlank(item.getNxDgGoodsName())) {
                        return false;
                    }
                    return name.trim().equals(item.getNxDgGoodsName().trim());
                })
                .filter(item -> {
                    // 规格必须相同（必填，不应为空）
                    if (isBlank(standard) || isBlank(item.getNxDgGoodsStandardname())) {
                        return false;
                    }
                    return standard.trim().equals(item.getNxDgGoodsStandardname().trim());
                })
                .filter(item -> matchStringStrict(item.getNxDgGoodsStandardWeight(), weight))  // 规格重量：有则相同，无则都无
                .filter(item -> matchStringStrict(item.getNxDgGoodsBrand(), brand))  // 品牌：有则相同，无则都无
                .collect(Collectors.toList());
        return filtered.isEmpty() ? null : filtered;
    }

    private NxGoodsEntity pickNxGoods(List<NxGoodsEntity> list,
                                      String standard, String weight, String brand) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        List<NxGoodsEntity> filtered = list.stream()
                .filter(item -> matchString(item.getNxGoodsStandardname(), standard))
                .filter(item -> matchString(item.getNxGoodsStandardWeight(), weight))
                .filter(item -> matchString(item.getNxGoodsBrand(), brand))
                .collect(Collectors.toList());
        return filtered.size() == 1 ? filtered.get(0) : null;
    }

    private boolean matchString(String actual, String expected) {
        if (isBlank(expected)) {
            return true;
        }
        return actual != null && actual.trim().equals(expected.trim());
    }

    /**
     * 严格匹配字符串：用于判断商品的可选字段（规格重量、品牌）是否为同一个
     * 规则：
     * - 如果查询的商品字段有值，数据库商品字段也必须有相同值才匹配
     * - 如果查询的商品字段为空，数据库商品字段也必须为空才匹配
     * - 如果查询的商品字段有值但数据库商品字段为空，或不匹配，返回false
     *
     * @param actual 数据库中的实际值
     * @param expected 查询时传入的期望值
     * @return true表示匹配，false表示不匹配
     */
    private boolean matchStringStrict(String actual, String expected) {
        // 如果查询的商品字段为空，数据库商品字段也必须为空才匹配
        if (isBlank(expected)) {
            return isBlank(actual);
        }
        // 如果查询的商品字段有值，数据库商品字段也必须有相同值才匹配
        if (isBlank(actual)) {
            return false;  // 查询有值但数据库为空，不匹配
        }
        return actual.trim().equals(expected.trim());  // 都有值，必须内容相同
    }

    private String blankToNull(String source) {
        return isBlank(source) ? null : source.trim();
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private enum MatchStatus {
        FOUND,
        CONFLICT,
        NOT_FOUND,
        ERROR
    }

    private static class GoodsMatchResult {
        MatchStatus status = MatchStatus.NOT_FOUND;
        NxDistributerGoodsEntity disGoods;
        List<NxDistributerGoodsEntity> candidates;
        NxGoodsEntity nxGoods;
        List<NxGoodsEntity> nxCandidates;
        String message;
    }



    @RequestMapping(value = "/depPasteSearchGoods", method = RequestMethod.POST)
    @ResponseBody
    public R depPasteSearchGoods(@RequestBody List<NxDepartmentOrdersEntity> orderList) {
        List<NxDepartmentOrdersEntity> returnList = new ArrayList<>();
        for (NxDepartmentOrdersEntity ordersEntity : orderList) {
            if (ordersEntity.getNxDoRemark().equals("-1")) {
                ordersEntity.setNxDoRemark(null);
            }
            // 第一条件：部门商品搜索
            Map<String, Object> map = new HashMap<>();
            map.put("depId", ordersEntity.getNxDoDepartmentId());
            map.put("disId", ordersEntity.getNxDoDistributerId());
            map.put("name", ordersEntity.getNxDoGoodsName());
            System.out.println("部门商品搜索" + map);
            List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
            if (departmentDisGoodsEntities.size() == 1) {
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = departmentDisGoodsEntities.get(0);
                Integer nxDdgDisGoodsId = nxDepartmentDisGoodsEntity.getNxDdgDisGoodsId();
                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(nxDdgDisGoodsId);
                ordersEntity.setNxDoStatus(0);
                returnList.add(nxDepartmentOrdersService.saveOrderWithGoods(ordersEntity, distributerGoodsEntity));
            } else {
                // 第二条件：商品名称+规格搜索
                Map<String, Object> mapZero = new HashMap<>();
                String goodsName = ordersEntity.getNxDoGoodsName();
                mapZero.put("disId", ordersEntity.getNxDoDistributerId());
                mapZero.put("searchStr", goodsName);
                mapZero.put("standard", ordersEntity.getNxDoStandard());
                mapZero.put("nxGoodsId", 1);
                System.out.println("商品名称+规格搜索brand" + mapZero + "ordername--------------------" + ordersEntity.getNxDoGoodsName());
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
                            String pinyinString = goodsName;
// 如果包含汉字才转换，否则直接用原名
                            if (goodsName.matches(".*[\u4E00-\u9FFF]+.*")) {
                                pinyinString = hanziToPinyin(goodsName);
                            }
                            Map<String, Object> mapTwo = new HashMap<>();
                            mapTwo.put("disId", ordersEntity.getNxDoDistributerId());
                            mapTwo.put("searchPinyin", pinyinString);
                            mapTwo.put("nxGoodsId", 1);
                            List<NxDistributerGoodsEntity> distributerGoodsEntitiesThree = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
                            if (distributerGoodsEntitiesThree.size() == 0) {
                                returnList.add(nxDepartmentOrdersService.depSaveLinshiGoodsForPasteOrder(ordersEntity));
                            } else if (distributerGoodsEntitiesThree.size() == 1) {
                                NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesThree.get(0);
                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                                ordersEntity.setNxDoStatus(0);
                                returnList.add(nxDepartmentOrdersService.saveOrderWithGoods(ordersEntity, distributerGoodsEntity));

                            } else {
                                System.out.println("多个distributerGoodsEntitiesThree拼音相同");
                                returnList.add(nxDepartmentOrdersService.depSaveLinshiGoodsForPasteOrder(ordersEntity));
                            }

                        } else if (distributerGoodsEntitiesTwo.size() == 1) {
                            //1 保存订单
                            NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesTwo.get(0);
                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                            ordersEntity.setNxDoStatus(0);
                            returnList.add(nxDepartmentOrdersService.saveOrderWithGoods(ordersEntity, distributerGoodsEntity));
                        } else {
                            System.out.println("多个distributerGoodsEntitiesTwo相同");
                            returnList.add(nxDepartmentOrdersService.depSaveLinshiGoodsForPasteOrder(ordersEntity));
                        }

                    } else if (distributerGoodsEntitiesOne.size() == 1) {
                        System.out.println("zeooo=====111111111");
                        NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesOne.get(0);
                        ordersEntity.setNxDoStatus(0);
                        returnList.add(nxDepartmentOrdersService.saveOrderWithGoods(ordersEntity, disGoodsEntity));
                    } else {
                        System.out.println("多个distributerGoodsEntitiesOne相同");
                        returnList.add(nxDepartmentOrdersService.depSaveLinshiGoodsForPasteOrder(ordersEntity));
                    }

                } else {
                    if (distributerGoodsEntitiesZero.size() == 1) {
                        System.out.println("zeooo=====111111111");
                        NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesZero.get(0);
                        ordersEntity.setNxDoStatus(0);
                        returnList.add(nxDepartmentOrdersService.saveOrderWithGoods(ordersEntity, disGoodsEntity));
                    } else {
                        System.out.println("多个mapZero结果商品名称+规格搜索");
                        returnList.add(nxDepartmentOrdersService.depSaveLinshiGoodsForPasteOrder(ordersEntity));
                    }
                }
            }

        }

        return R.ok().put("data", returnList);
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
//                        goodsEntity.setSellSubtotal(new BigDecimal(vC).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
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

    private static final String DEPRECATED_ROUTE_DISPATCH_MSG =
            "旧路线派单已下线，请使用 GET api/nxdisroutedispatch/dispatch/sandbox/today、"
                    + "POST api/nxdisroutedispatch/sandbox/stops/confirm 及 tasks/{id}/assign";

    /** @deprecated 旧主链已摘除，不再执行 Controller 内贪心路线算法 */
    @RequestMapping(value = "/disGetTodayOrderCustomerRoute", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTodayOrderCustomerRoute(Integer disId, String fromLat, String fromLng) {
        return R.error(-1, DEPRECATED_ROUTE_DISPATCH_MSG);
    }


    /** @deprecated 旧主链已摘除 */
    @RequestMapping(value = "/disGetDriversOptimalRoute", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDriversOptimalRoute(@RequestBody Map<String, Object> paramMap) {
        return R.error(-1, DEPRECATED_ROUTE_DISPATCH_MSG);
    }


    /** @deprecated 旧主链已摘除 */
    @RequestMapping(value = "/disGetCustomerDistanceMatrix", method = RequestMethod.POST)
    @ResponseBody
    public R disGetCustomerDistanceMatrix(Integer disId, String fromLat, String fromLng) {
        return R.error(-1, DEPRECATED_ROUTE_DISPATCH_MSG);
    }

    /** REMOVED: greedyRouteByMatrix / queryMatrixLegs / requestTencentMapJson — 路线算法已迁至 RouteEngineRegistry */

//    @ResponseBody
//    @RequestMapping("/saveCashSun")
//    public R saveCashSun(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {
//        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusGouwu());
//        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
//        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
//        nxDepartmentOrders.setNxDoApplyFullTime(formatWhatYearDayTime(0));
//        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
//        nxDepartmentOrders.setNxDoGbDistributerId(-1);
//        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
//        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
//        nxDepartmentOrders.setNxDoNxCommunityId(-1);
//        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
//        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
//        nxDepartmentOrders.setNxDoPriceDifferent("0.0");
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
//        map.put("status", 3);
//        map.put("isPurType", true);
//        int order = nxDepartmentOrdersService.queryDepOrdersAcount(map);
//        nxDepartmentOrders.setNxDoTodayOrder(order + 1);
//
//        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
//        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
//        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = saveOneOrderCash(nxDepartmentOrders, nxDistributerGoodsEntity);
//
//        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
//        return R.ok().put("data", nxDepartmentOrdersEntity);
//
//    }

    @RequestMapping(value = "/disGetTodayOrderCustomerSunla/{disId}")
    @ResponseBody
    public R disGetTodayOrderCustomerSunla(@PathVariable Integer disId) {
        Map<String, Object> returnData = new HashMap<>();
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("dayuStatus", -1);
        System.out.println("mapapapapapddiidididi" + map1);

        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);
        List<GbDistributerEntity> distributerEntitiesAA = nxDepartmentOrdersService.queryOrderGbDistributerList(map1);
        System.out.println("gbbgbgbgbbgbbgbgb" + distributerEntitiesAA.size());
        Map<String, Object> mapData = new HashMap<>();
        List<Map<String, Object>> resultNx = new ArrayList<>();
        if (departmentEntities.size() > 0) {


            System.out.println("depsisiisisisiiis" + departmentEntities.size());
            // 提取所有部门ID，过滤掉null值
            List<Integer> depFatherIds = departmentEntities.stream()
                    .filter(dept -> dept != null)
                    .map(dept -> dept.getNxDepartmentId())
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            // 批量查询所有部门的统计数据
            Map<Integer, Map<String, Object>> statsMap = nxDepartmentOrdersService.batchQueryDepartmentOrderStatsSunla(depFatherIds);

            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                // 跳过null的部门实体
                if (departmentEntity == null) {
                    continue;
                }
                Integer depId = departmentEntity.getNxDepartmentId();
                // 跳过部门ID为null的情况
                if (depId == null) {
                    continue;
                }
                Map<String, Object> stats = statsMap.get(depId);

                if (stats != null) {
                    Integer unDo = (Integer) stats.get("unDo");
                    Integer hasPrice = (Integer) stats.get("hasPrice");
                    Integer hasWeight = (Integer) stats.get("hasWeight");
                    Integer totalCount = (Integer) stats.get("totalCount");
                    Integer finishCount = (Integer) stats.get("finishCount");
                    Object totalSubtotal = stats.get("totalSubtotal");

                    Double total = 0.0;
                    if (totalSubtotal != null) {
                        total = ((Number) totalSubtotal).doubleValue();
                    }

                    Map<String, Object> mapDep = new HashMap<>();
                    mapDep.put("dep", departmentEntity);
                    mapDep.put("totalCount", totalCount);
                    mapDep.put("finishCount", finishCount);
                    mapDep.put("hasPrice", hasPrice);
                    mapDep.put("hasWeight", hasWeight);
                    mapDep.put("unDo", unDo);
                    mapDep.put("twoSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    resultNx.add(mapDep);
                }
            }
            mapData.put("nxDep", resultNx);
        } else {
            mapData.put("nxDep", new ArrayList<>());
        }

        List<Map<String, Object>> gbList = new ArrayList<>();

        if (distributerEntitiesAA.size() > 0) {
            // 提取所有分销商ID和部门ID
            List<Integer> gbDisIds = new ArrayList<>();
            List<Integer> gbDepIds = new ArrayList<>();
            Map<Integer, List<GbDepartmentEntity>> gbDisToDepMap = new HashMap<>();

            for (GbDistributerEntity gbDis : distributerEntitiesAA) {
                gbDisIds.add(gbDis.getGbDistributerId());

                Map<String, Object> mapOrderDep = new HashMap<>();
                mapOrderDep.put("gbDisId", gbDis.getGbDistributerId());
                mapOrderDep.put("disId", disId);
                System.out.println("查询部门参数: " + mapOrderDep);
                List<GbDepartmentEntity> gbpartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapOrderDep);
                System.out.println("商家 " + gbDis.getGbDistributerId() + " 关联的部门数量: " + gbpartmentEntities.size());
                if (gbpartmentEntities.size() > 0) {
                    System.out.println("部门列表: " + gbpartmentEntities.stream().map(GbDepartmentEntity::getGbDepartmentName).collect(Collectors.toList()));
                }
                gbDisToDepMap.put(gbDis.getGbDistributerId(), gbpartmentEntities);
                for (GbDepartmentEntity gbDep : gbpartmentEntities) {
                    gbDepIds.add(gbDep.getGbDepartmentId());
                }
            }

            // 批量查询所有统计数据
            System.out.println("=== 批量查询统计数据 ===");
            System.out.println("商家ID列表: " + gbDisIds);
            System.out.println("部门ID列表: " + gbDepIds);
            Map<String, Map<String, Object>> statsMap = nxDepartmentOrdersService.batchQueryGbDistributerDepartmentStats(gbDisIds, gbDepIds);
            System.out.println("查询到的统计数据Map大小: " + statsMap.size());
            System.out.println("统计数据Map的key列表: " + statsMap.keySet());
            System.out.println("统计数据Map内容: " + statsMap);
            System.out.println("=== 批量查询完成 ===\n");

            for (GbDistributerEntity gbDis : distributerEntitiesAA) {
                List<Map<String, Object>> gbDisDepArrMap = new ArrayList<>();

                Map<String, Object> mapDis = new HashMap<>();
                mapDis.put("dis", gbDis);
                mapDis.put("gbDisId", gbDis.getGbDistributerId());

                List<GbDepartmentEntity> gbpartmentEntities = gbDisToDepMap.get(gbDis.getGbDistributerId());

                System.out.println("=== 开始处理商家: " + gbDis.getGbDistributerId() + " ===");
                System.out.println("商家名称: " + gbDis.getGbDistributerName());
                System.out.println("关联部门数量: " + (gbpartmentEntities != null ? gbpartmentEntities.size() : "null"));

                for (GbDepartmentEntity gbDepartmentEntity : gbpartmentEntities) {
                    String key = gbDis.getGbDistributerId() + "_" + gbDepartmentEntity.getGbDepartmentId();
                    Map<String, Object> stats = statsMap.get(key);

                    System.out.println("--- 处理部门: " + gbDepartmentEntity.getGbDepartmentId() + " ---");
                    System.out.println("部门名称: " + gbDepartmentEntity.getGbDepartmentName());
                    System.out.println("查询key: " + key);
                    System.out.println("统计数据: " + stats);

                    if (stats != null) {
                        Integer newOrder = (Integer) stats.get("newOrder");
                        System.out.println("newOrder值: " + newOrder);

                        if (newOrder > 0) {
                            Integer jinhuoOrder = (Integer) stats.get("jinhuoOrder");
                            Integer chukuOrder = (Integer) stats.get("chukuOrder");
                            Integer hasNotWeight = (Integer) stats.get("hasNotWeight");
                            Integer jinhuoHasWeight = (Integer) stats.get("jinhuoHasWeight");
                            Integer chukuHasWeight = (Integer) stats.get("chukuHasWeight");
                            Integer hasPrice = (Integer) stats.get("hasPrice");
                            Integer hasNotPrice = (Integer) stats.get("hasNotPrice");
                            Integer twoTotal = (Integer) stats.get("twoTotal");
                            Object totalObj = stats.get("total");

                            Double total = 0.0;
                            if (totalObj != null) {
                                total = ((Number) totalObj).doubleValue();
                            }

                            Integer jinhuoFinished = 0; // 这个字段在原来的代码中似乎没有计算

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
                            System.out.println("*** 添加部门数据到arr ***");
                        } else {
                            System.out.println("*** newOrder <= 0，跳过该部门 ***");
                        }
                    } else {
                        System.out.println("*** stats为null，跳过该部门 ***");
                    }
                }

                System.out.println("最终arr大小: " + gbDisDepArrMap.size());
                System.out.println("arr内容: " + gbDisDepArrMap);
                mapDis.put("arr", gbDisDepArrMap);
                gbList.add(mapDis);
                System.out.println("=== 商家处理完成 ===\n");
            }

            mapData.put("gbDisArrApp", gbList);

        } else {
            mapData.put("gbDisArrApp", new ArrayList<>());
        }



        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("purStatus", 4);
        map111.put("dayuStatus", -1);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        map111.put("batchId", 0);
        int unPurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("batchId", 1);
        int puringCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);


        Map<String, Object> mapB = new HashMap<>();
        mapB.put("disId", disId);
        mapB.put("settleType", 0);
        mapB.put("equalStatus", 0);
        int unPayCount = nxDepartmentBillService.queryBillsCount(mapB);

        //return
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", disId);
//        map.put("equalStatus", -1);
//        System.out.println("rututtntnmaappa" + map);
//        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryReturnBill(map);
//        Map<String, Object> mapGb = new HashMap<>();
//        mapGb.put("disId", disId);
//        mapGb.put("gbDepFatherIdNotEqual", -1);
//        mapGb.put("status", 3);
//        System.out.println("usnbdidiid" + mapGb);
//        int i = nxDepartmentBillService.queryBillsCount(mapGb);


        Map<String, Object> map7 = new HashMap<>();
        map7.put("disId", disId);
        map7.put("status", -1);
        map7.put("agent", 1);
        map7.put("dayuStatus", -1);
        int unDoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map7);

        Map<String, Object> mapLinshi = new HashMap<>();
        mapLinshi.put("disId", disId);
        mapLinshi.put("isLinshi", 1);
        System.out.println("linsshsisiss" + mapLinshi);
        int wxCountAuto1 = nxDistributerGoodsService.queryDisGoodsTotal(mapLinshi);

        returnData.put("stockCount", stockCount);
        returnData.put("unPurCount", unPurCount);
        returnData.put("puringCount", puringCount);
        returnData.put("unPayCount", unPayCount);
        returnData.put("deps", mapData);
        returnData.put("disInfo", nxDistributerService.queryDistributerInfo(disId));
//        returnData.put("returnList", billEntityList);
        returnData.put("unDoTotal", unDoTotal);
        returnData.put("linshiTotal", wxCountAuto1);
        return R.ok().put("data", returnData);

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




}
