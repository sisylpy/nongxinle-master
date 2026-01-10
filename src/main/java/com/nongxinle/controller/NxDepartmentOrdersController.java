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
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.dto.PasteSearchGoodsResponseDTO;
import com.nongxinle.dto.DistributerGoodsCandidateDTO;
import com.nongxinle.dto.NxGoodsCandidateDTO;
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
import com.nongxinle.utils.RRException;
import org.apache.commons.io.IOUtils;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.net.URLEncoder;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbPurchaseGoodsTypeForOrder;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.ParseObject.myRandom;
import static com.nongxinle.utils.PinYin4jUtils.*;


@RestController
@RequestMapping("api/nxdepartmentorders")
public class NxDepartmentOrdersController {
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


    @RequestMapping(value = "/updateOrderPrint", method = RequestMethod.POST)
    @ResponseBody
    public R updateOrderPrint (Integer orderId, String printName) {
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(orderId);
        nxDepartmentOrdersEntity.setNxDoPrintStandard(printName);
        nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);
        return R.ok();
    }


    @RequestMapping(value = "/disGetLinshiOrders/{id}")
    @ResponseBody
    public R disGetLinshiOrders(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", id);
        map.put("status", -1);
        map.put("agent", 1);
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

    //
    @RequestMapping(value = "/exchangeDepApplyGoods", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeDepApplyGoods(Integer orderId, Integer goodsId) {
        System.out.println("orderrid" + orderId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(orderId);
        Map<String, Object> mapOD = new HashMap<>();
        mapOD.put("disGoodsId", nxDepartmentOrdersEntity.getNxDoDisGoodsId());
        mapOD.put("depId", nxDepartmentOrdersEntity.getNxDoDepartmentId());
        NxDepartmentDisGoodsEntity orderDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapOD);
        if (orderDepartmentDisGoodsEntity != null) {
            nxDepartmentDisGoodsService.delete(orderDepartmentDisGoodsEntity.getNxDepartmentDisGoodsId());
        }

        NxDistributerGoodsEntity exChangeGoodsEntity = nxDistributerGoodsService.queryObject(goodsId);

        Integer nxDoPurchaseGoodsId = nxDepartmentOrdersEntity.getNxDoPurchaseGoodsId();

        if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
            if (purchaseGoodsEntity != null) {
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                if (nxDpgOrdersAmount != null && nxDpgOrdersAmount == 1) {
                    if (purchaseGoodsEntity.getNxDpgBatchId() != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("batchId", purchaseGoodsEntity.getNxDpgBatchId());
                        List<NxDistributerPurchaseGoodsEntity> nxDistributerPurchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
                        if (nxDistributerPurchaseGoodsEntities.size() == 1) {
                            nxDistributerPurchaseBatchService.delete(purchaseGoodsEntity.getNxDpgBatchId());
                        }
                    } else {
                        purchaseGoodsEntity.setNxDpgDisGoodsId(goodsId);
                        purchaseGoodsEntity.setNxDpgDisGoodsFatherId(exChangeGoodsEntity.getGbDisGoodsFatherId());
                        purchaseGoodsEntity.setNxDpgDisGoodsGrandId(exChangeGoodsEntity.getNxDgDfgGoodsGrandId());
                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }

                } else if (nxDpgOrdersAmount != null && nxDpgOrdersAmount > 1) {
                    purchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            } else {
                System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
            }
        }

        nxDepartmentOrdersEntity.setNxDoDisGoodsId(exChangeGoodsEntity.getNxDistributerGoodsId());
        nxDepartmentOrdersEntity.setNxDoDisGoodsFatherId(exChangeGoodsEntity.getNxDgDfgGoodsFatherId());
        nxDepartmentOrdersEntity.setNxDoDisGoodsGrandId(exChangeGoodsEntity.getNxDgDfgGoodsGrandId());
        nxDepartmentOrdersEntity.setNxDoNxGoodsId(exChangeGoodsEntity.getNxDgNxGoodsId());
        nxDepartmentOrdersEntity.setNxDoNxGoodsFatherId(exChangeGoodsEntity.getNxDgNxFatherId());
        nxDepartmentOrdersEntity.setNxDoGoodsType(exChangeGoodsEntity.getNxDgPurchaseAuto());
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", goodsId);
        map.put("depId", nxDepartmentOrdersEntity.getNxDoDepartmentId());
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if (departmentDisGoodsEntity != null) {
            String nxDdgOrderPrice = departmentDisGoodsEntity.getNxDdgOrderPrice();
            nxDepartmentOrdersEntity.setNxDoPrice(nxDdgOrderPrice);
            nxDepartmentOrdersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
            nxDepartmentOrdersEntity.setNxDoDepDisGoodsPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
        }

        if (exChangeGoodsEntity.getNxDgWillPriceTwo() != null && nxDepartmentOrdersEntity.getNxDoStandard().equals(exChangeGoodsEntity.getNxDgWillPriceTwoStandard())) {
            System.out.println("levlellelelelelel111222222222222222");
            BigDecimal doQuantity = new BigDecimal(nxDepartmentOrdersEntity.getNxDoQuantity());
            BigDecimal cosPrice = new BigDecimal(exChangeGoodsEntity.getNxDgBuyingPriceTwo());
            BigDecimal willPrice = new BigDecimal(0);
            if (departmentDisGoodsEntity != null) {
                willPrice = new BigDecimal(nxDepartmentOrdersEntity.getNxDoPrice());
            } else {
                willPrice = new BigDecimal(exChangeGoodsEntity.getNxDgWillPriceTwo());
            }

            BigDecimal costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

            //
            nxDepartmentOrdersEntity.setNxDoPrintStandard(exChangeGoodsEntity.getNxDgWillPriceTwoStandard());
            nxDepartmentOrdersEntity.setNxDoExpectPrice(exChangeGoodsEntity.getNxDgWillPriceTwo());
            nxDepartmentOrdersEntity.setNxDoPrice(exChangeGoodsEntity.getNxDgWillPriceTwo());
            nxDepartmentOrdersEntity.setNxDoSubtotal(subtotal.toString());
            nxDepartmentOrdersEntity.setNxDoCostSubtotal(costSubtotal.toString());
            nxDepartmentOrdersEntity.setNxDoCostPriceLevel("2");
            nxDepartmentOrdersEntity.setNxDoCostPriceUpdate(exChangeGoodsEntity.getNxDgBuyingPriceTwoUpdate());
            nxDepartmentOrdersEntity.setNxDoCostPrice(exChangeGoodsEntity.getNxDgBuyingPriceTwo());
            nxDepartmentOrdersEntity.setNxDoProfitSubtotal(profit.toString());
            nxDepartmentOrdersEntity.setNxDoProfitScale(scaleB.toString());

        } else {
            nxDepartmentOrdersEntity.setNxDoPrintStandard(exChangeGoodsEntity.getNxDgGoodsStandardname());
            BigDecimal willPrice = new BigDecimal(0);
            if (departmentDisGoodsEntity != null) {
                willPrice = new BigDecimal(nxDepartmentOrdersEntity.getNxDoPrice());
            } else {
                willPrice = new BigDecimal(exChangeGoodsEntity.getNxDgWillPriceOne());
            }
            nxDepartmentOrdersEntity.setNxDoPrice(willPrice.toString());
            nxDepartmentOrdersEntity.setNxDoExpectPrice(exChangeGoodsEntity.getNxDgWillPriceOne());
            nxDepartmentOrdersEntity.setNxDoCostPriceLevel("1");
            nxDepartmentOrdersEntity.setNxDoCostPriceUpdate(exChangeGoodsEntity.getNxDgBuyingPriceOneUpdate());
            nxDepartmentOrdersEntity.setNxDoCostPrice(exChangeGoodsEntity.getNxDgBuyingPriceOne());
            System.out.println("orderstandndnd" + nxDepartmentOrdersEntity.getNxDoStandard() + "goodsstandn" + exChangeGoodsEntity.getNxDgGoodsStandardname());
            if (nxDepartmentOrdersEntity.getNxDoStandard().equals(exChangeGoodsEntity.getNxDgGoodsStandardname())
                    && !exChangeGoodsEntity.getNxDgBuyingPriceOne().equals("0.1") && !exChangeGoodsEntity.getNxDgBuyingPriceOne().trim().isEmpty()) {
                BigDecimal doQuantity = new BigDecimal(nxDepartmentOrdersEntity.getNxDoQuantity());
                BigDecimal cosPrice = new BigDecimal(exChangeGoodsEntity.getNxDgBuyingPriceOne());
                BigDecimal costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                nxDepartmentOrdersEntity.setNxDoSubtotal(subtotal.toString());
                nxDepartmentOrdersEntity.setNxDoCostSubtotal(costSubtotal.toString());
                nxDepartmentOrdersEntity.setNxDoProfitSubtotal(profit.toString());
                nxDepartmentOrdersEntity.setNxDoProfitScale(scaleB.toString());

            } else {
                nxDepartmentOrdersEntity.setNxDoSubtotal(null);
            }

        }

        nxDepartmentOrdersEntity.setNxDoPurchaseGoodsId(-1);
        nxDepartmentOrdersEntity.setNxDoStatus(0);
        nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);

        if (exChangeGoodsEntity.getNxDgPurchaseAuto() != -1) {
            savePurGoodsAuto(nxDepartmentOrdersEntity);
        }

        return R.ok();
    }

    @RequestMapping(value = "/editDepApplyGoods", method = RequestMethod.POST)
    @ResponseBody
    public R editDepApplyGoods(Integer orderId, Integer goodsId) {

        System.out.println("orderrid" + orderId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(orderId);
        Integer nxDoDisGoodsId = nxDepartmentOrdersEntity.getNxDoDisGoodsId();

        NxDistributerGoodsEntity lishiGoods = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        Integer nxDoPurchaseGoodsIldLinshi = nxDepartmentOrdersEntity.getNxDoPurchaseGoodsId();
        NxDistributerPurchaseGoodsEntity linshiPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsIldLinshi);

        System.out.println("purgidid" + linshiPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
        NxDistributerGoodsEntity toNxdisGoodsEnitity = nxDistributerGoodsService.queryObject(goodsId);
        Integer nxDpgOrdersAmount = linshiPurchaseGoodsEntity.getNxDpgOrdersAmount();
        if (nxDpgOrdersAmount == 1) {
            if (linshiPurchaseGoodsEntity.getNxDpgBatchId() == null) {
                nxDistributerPurchaseGoodsService.delete(linshiPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
            } else {
                System.out.println("updateeeeeee" + linshiPurchaseGoodsEntity);
                linshiPurchaseGoodsEntity.setNxDpgDisGoodsId(goodsId);
                linshiPurchaseGoodsEntity.setNxDpgDisGoodsFatherId(toNxdisGoodsEnitity.getGbDisGoodsFatherId());
                linshiPurchaseGoodsEntity.setNxDpgDisGoodsGrandId(toNxdisGoodsEnitity.getNxDgDfgGoodsGrandId());
                nxDistributerPurchaseGoodsService.update(linshiPurchaseGoodsEntity);
            }
        } else {
            linshiPurchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
            linshiPurchaseGoodsEntity.setNxDpgDisGoodsId(goodsId);
            linshiPurchaseGoodsEntity.setNxDpgDisGoodsFatherId(toNxdisGoodsEnitity.getGbDisGoodsFatherId());
            linshiPurchaseGoodsEntity.setNxDpgDisGoodsGrandId(toNxdisGoodsEnitity.getNxDgDfgGoodsGrandId());
            nxDistributerPurchaseGoodsService.update(linshiPurchaseGoodsEntity);
        }

        nxDistributerGoodsService.delete(lishiGoods.getNxDistributerGoodsId());


        Integer nxDoPurchaseGoodsId = nxDepartmentOrdersEntity.getNxDoPurchaseGoodsId();
        nxDistributerPurchaseGoodsService.delete(nxDoPurchaseGoodsId);

        nxDepartmentOrdersEntity.setNxDoDisGoodsId(toNxdisGoodsEnitity.getNxDistributerGoodsId());
        nxDepartmentOrdersEntity.setNxDoDisGoodsFatherId(toNxdisGoodsEnitity.getNxDgDfgGoodsFatherId());
        nxDepartmentOrdersEntity.setNxDoDisGoodsGrandId(toNxdisGoodsEnitity.getNxDgDfgGoodsGrandId());
        nxDepartmentOrdersEntity.setNxDoNxGoodsId(toNxdisGoodsEnitity.getNxDgNxGoodsId());
        nxDepartmentOrdersEntity.setNxDoNxGoodsFatherId(toNxdisGoodsEnitity.getNxDgNxFatherId());
        nxDepartmentOrdersEntity.setNxDoStatus(0);
        nxDepartmentOrdersEntity.setNxDoGoodsType(toNxdisGoodsEnitity.getNxDgPurchaseAuto());

        processOrderPrice(nxDepartmentOrdersEntity, toNxdisGoodsEnitity);
        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrdersEntity.getNxDoDepartmentId());
        map.put("disGoodsId", toNxdisGoodsEnitity.getNxDistributerGoodsId());
        map.put("standard", nxDepartmentOrdersEntity.getNxDoStandard());
        System.out.println("depmapapmmdmmdd" + map);
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if (departmentDisGoodsEntity != null) {
            System.out.println("prinstnan" + departmentDisGoodsEntity.getNxDdgOrderStandard());
            // 判断是否使用了大包装单价，如果是则设置打印规格为大包装单位（支持智能匹配：件=箱）
            boolean isUsingCartonPrice = toNxdisGoodsEnitity.getNxDgCartonUnit() != null
                    && !toNxdisGoodsEnitity.getNxDgCartonUnit().trim().isEmpty()
                    && nxDepartmentOrdersEntity.getNxDoStandard() != null
                    && isStandardMatch(nxDepartmentOrdersEntity.getNxDoStandard().trim(), toNxdisGoodsEnitity.getNxDgCartonUnit().trim());

            if (isUsingCartonPrice) {
                nxDepartmentOrdersEntity.setNxDoPrintStandard(toNxdisGoodsEnitity.getNxDgCartonUnit());
                System.out.println("使用大包装单价，打印规格设置为: " + toNxdisGoodsEnitity.getNxDgCartonUnit());
            } else if (nxDepartmentOrdersEntity.getNxDoCostPriceLevel() == null || nxDepartmentOrdersEntity.getNxDoCostPriceLevel().equals("1")) {
                nxDepartmentOrdersEntity.setNxDoPrintStandard(toNxdisGoodsEnitity.getNxDgGoodsStandardname());
            } else {
                nxDepartmentOrdersEntity.setNxDoPrintStandard(toNxdisGoodsEnitity.getNxDgWillPriceTwoStandard());
            }

            nxDepartmentOrdersEntity.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
            //如果有重量和单价，则计算 subtotal
            if(nxDepartmentOrdersEntity.getNxDoWeight() != null && !nxDepartmentOrdersEntity.getNxDoWeight().trim().isEmpty()
                    && nxDepartmentOrdersEntity.getNxDoPrice() != null && !nxDepartmentOrdersEntity.getNxDoPrice().trim().isEmpty()){
                try {
                    BigDecimal weight = new BigDecimal(nxDepartmentOrdersEntity.getNxDoWeight());
                    BigDecimal price = new BigDecimal(nxDepartmentOrdersEntity.getNxDoPrice());
                    BigDecimal subtotal = weight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                    nxDepartmentOrdersEntity.setNxDoSubtotal(subtotal.toString());
                } catch (NumberFormatException e) {
                    // 如果转换失败，不设置subtotal
                    logger.warn("[saveOneOrder] 计算subtotal失败，重量或单价格式错误: weight={}, price={}, error={}",
                            nxDepartmentOrdersEntity.getNxDoWeight(), nxDepartmentOrdersEntity.getNxDoPrice(), e.getMessage());
                }
            }
            nxDepartmentOrdersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
        }else{
            map.put("standard", null);
            System.out.println("depmapapapappapa" + map);
            NxDepartmentDisGoodsEntity departmentDisGoodsEntityO = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
            if (departmentDisGoodsEntityO != null) {
                nxDepartmentOrdersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
            }
        }
//        Map<String, Object> map = new HashMap<>();
//        map.put("disGoodsId", goodsId);
//        map.put("depId", nxDepartmentOrdersEntity.getNxDoDepartmentId());
//        map.put("standard", nxDepartmentOrdersEntity.getNxDoStandard());
//        System.out.println("depto");
//        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
//        if (departmentDisGoodsEntity != null) {
//            String nxDdgOrderPrice = departmentDisGoodsEntity.getNxDdgOrderPrice();
//            nxDepartmentOrdersEntity.setNxDoPrice(nxDdgOrderPrice);
//            nxDepartmentOrdersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
//            nxDepartmentOrdersEntity.setNxDoDepDisGoodsPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
//                BigDecimal doQuantity = new BigDecimal(nxDepartmentOrdersEntity.getNxDoQuantity());
//                BigDecimal willPrice = new BigDecimal(nxDdgOrderPrice);
//                BigDecimal subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    nxDepartmentOrdersEntity.setNxDoSubtotal(subtotal.toString());
//        }
//
//        if(nxDepartmentOrdersEntity.getNxDoStandard().equals(toNxdisGoodsEnitity.getNxDgGoodsStandardname())){
//            nxDepartmentOrdersEntity.setNxDoWeight(nxDepartmentOrdersEntity.getNxDoQuantity());
//        }
        nxDepartmentOrdersEntity.setNxDoPurchaseGoodsId(-1);
        nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);

        if (toNxdisGoodsEnitity.getNxDgPurchaseAuto() != -1) {
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
        logger.info("[choiceGoodsForApply] 开始处理商品选择申请，订单ID: {}, 商品名称: {}, 分销商商品ID: {}", 
                orders.getNxDepartmentOrdersId(), orders.getNxDoGoodsName(), orders.getNxDoDisGoodsId());
        
        // 保存原始订单的JSON字符串和原始商品名称（用于转换为精简字段）
        String originalOrderJson = JSONObject.toJSONString(orders);
        String originalGoodsName = orders.getNxDoGoodsName();
        
        Integer doDisGoodsId = orders.getNxDoDisGoodsId();
        if (doDisGoodsId == null) {
            logger.warn("[choiceGoodsForApply] 分销商商品ID为空，无法处理");
            return R.error(-1, "分销商商品ID不能为空");
        }
        
        NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
        if (disGoodsEntity == null) {
            logger.warn("[choiceGoodsForApply] 未找到分销商商品，ID: {}", doDisGoodsId);
            return R.error(-1, "未找到指定的分销商商品");
        }
        
        logger.info("[choiceGoodsForApply] 查询到分销商商品，商品ID: {}, 商品名称: {}, 规格: {}", 
                disGoodsEntity.getNxDistributerGoodsId(), disGoodsEntity.getNxDgGoodsName(), 
                disGoodsEntity.getNxDgGoodsStandardname());
        
        NxDepartmentOrdersEntity aaa = choiceGoodsOrder(orders, disGoodsEntity);
        logger.info("[choiceGoodsForApply] 商品选择订单处理完成，订单ID: {}, 状态: {}", 
                aaa.getNxDepartmentOrdersId(), aaa.getNxDoStatus());

        // 更新训练数据（如果订单有关联的训练数据）
        // 从数据库重新查询订单，确保获取最新的训练数据ID
        if (aaa.getNxDepartmentOrdersId() != null) {
            NxDepartmentOrdersEntity savedOrder = nxDepartmentOrdersService.queryObject(aaa.getNxDepartmentOrdersId());
            if (savedOrder != null && savedOrder.getNxDoTrainingDataId() != null) {
                logger.info("[choiceGoodsForApply] 订单关联了训练数据，训练数据ID: {}", savedOrder.getNxDoTrainingDataId());
                NxOrderOcrTrainingDataEntity trainingData = nxOrderOcrTrainingDataService.queryObject(savedOrder.getNxDoTrainingDataId());
            
            if (trainingData != null) {
                // 手动标注标志设为 2（自动识别是 1，手动识别是 2）
                trainingData.setNxOtdDisGoodsId(aaa.getNxDoDisGoodsId());
                trainingData.setNxOtdOrderId(aaa.getNxDepartmentOrdersId());
                
                // 填充最终确认字段（手动标注）
                trainingData.setNxOtdFinalGoodsName(aaa.getNxDoGoodsName());
                trainingData.setNxOtdFinalQuantity(aaa.getNxDoQuantity());
                trainingData.setNxOtdFinalStandard(aaa.getNxDoStandard());
                trainingData.setNxOtdFinalRemark(aaa.getNxDoRemark() != null ? aaa.getNxDoRemark() : "");
                
                // 手动标注标志设为 2
                trainingData.setNxOtdIsNameManuallyAnnotated(2);
                trainingData.setNxOtdIsQuantityManuallyAnnotated(2);
                trainingData.setNxOtdIsStandardManuallyAnnotated(2);
                trainingData.setNxOtdIsStandardWeightManuallyAnnotated(2);
                trainingData.setNxOtdIsRemarkManuallyAnnotated(2);
                
                // 更新标准重量（如果有的话，从原始数据中获取）
                if (trainingData.getNxOtdOriginalStandardWeight() != null) {
                    trainingData.setNxOtdFinalStandardWeight(trainingData.getNxOtdOriginalStandardWeight());
                }
                
                trainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
                
                // 更新训练数据
                nxOrderOcrTrainingDataService.update(trainingData);
                
                logger.info("[choiceGoodsForApply] 训练数据已更新（手动标注），训练数据ID: {}, 订单ID: {}, 商品ID: {}", 
                        trainingData.getNxOtdId(), aaa.getNxDepartmentOrdersId(), aaa.getNxDoDisGoodsId());
            } else {
                logger.warn("[choiceGoodsForApply] 未找到训练数据，训练数据ID: {}", savedOrder.getNxDoTrainingDataId());
            }
            }
        }

        aaa.setNxDistributerGoodsEntityList(new ArrayList<NxDistributerGoodsEntity>());
        
        // 转换为精简字段的DTO，与pasteSearchGoods保持一致
        PasteSearchGoodsResponseDTO responseDTO = convertToResponseDTO(aaa, originalOrderJson, originalGoodsName);
        logger.info("[choiceGoodsForApply] 处理完成，返回精简字段的订单数据");
        return R.ok().put("data", responseDTO);
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
                            String pinyinString = goodsName;
// 如果包含汉字才转换，否则直接用原名
                            if (goodsName.matches(".*[\u4E00-\u9FFF]+.*")) {
                                pinyinString = hanziToPinyin(goodsName);
                            }
                            Map<String, Object> mapTwo = new HashMap<>();
                            mapTwo.put("disId", ordersEntity.getNxDoDistributerId());
                            mapTwo.put("searchPinyin", pinyinString);
                            List<NxDistributerGoodsEntity> disGoodsByNamePinyin = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
                            System.out.println("resultteslsutltlt----pinyin11111" + disGoodsByNamePinyin.size());

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

    /**
     * Excel 导入货架商品
     * 从 Excel 文件读取商品信息，匹配或创建商品后添加到货架
     */
    @RequestMapping(value = "/importShelfGoodsFromExcel", method = RequestMethod.POST)
    @ResponseBody
    public R importShelfGoodsFromExcel(@RequestParam("file") MultipartFile file,
                                       @RequestParam("disId") Integer disId,
                                       @RequestParam("shelfId") Integer shelfId,
                                       @RequestParam(value = "startSort", required = false) Integer startSort) {
        System.out.println("[importShelfGoodsFromExcel] 请求导入货架商品 disId=" + disId
                + ", shelfId=" + shelfId + ", startSort=" + startSort
                + ", fileName=" + (file == null ? "null" : file.getOriginalFilename()));

        if (disId == null || shelfId == null) {
            return R.error(-1, "参数错误: 缺少 disId/shelfId");
        }
        if (file == null || file.isEmpty()) {
            return R.error(-1, "文件不能为空");
        }

        DataFormatter formatter = new DataFormatter();
        List<Map<String, Object>> failItems = new ArrayList<>();
        List<JSONObject> resultItems = new ArrayList<>();
        int total = 0;
        int success = 0;

        // 初始化排序号
        int currentSort = calculateShelfStartSort(shelfId, startSort);

        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(file.getInputStream());
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                if (sheet == null) {
                    continue;
                }
                int lastRowNum = sheet.getLastRowNum();
                // 从第3行开始读取数据（第1行是提示，第2行是表头，第3行可能是示例）
                for (int rowIndex = 2; rowIndex <= lastRowNum; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        continue;
                    }

                    String goodsName = getCellString(row, 0, formatter);
                    String standard = getCellString(row, 1, formatter);
                    // 如果商品名称和规格都为空，跳过这一行
                    if (isBlank(goodsName) && isBlank(standard)) {
                        continue;
                    }

                    total++;
                    JSONObject resultItem = new JSONObject();
                    resultItem.put("row", rowIndex + 1);
                    resultItem.put("goodsName", goodsName);
                    resultItem.put("standard", standard);

                    try {
                        // 验证必填项
                        if (isBlank(goodsName)) {
                            throw new IllegalArgumentException("缺少商品名称");
                        }
                        if (isBlank(standard)) {
                            throw new IllegalArgumentException("缺少规格");
                        }

                        // 构建商品信息 JSONObject
                        JSONObject item = new JSONObject();
                        item.put("nxDgGoodsName", goodsName.trim());
                        item.put("nxDgGoodsStandardname", standard.trim());
                        
                        String standardWeight = getCellString(row, 2, formatter);
                        if (!isBlank(standardWeight)) {
                            item.put("nxDgGoodsStandardWeight", standardWeight.trim());
                        }
                        
                        String brand = getCellString(row, 3, formatter);
                        if (!isBlank(brand)) {
                            item.put("nxDgGoodsBrand", brand.trim());
                        }
                        
                        String cartonUnit = getCellString(row, 4, formatter);
                        if (!isBlank(cartonUnit)) {
                            item.put("nxDgCartonUnit", cartonUnit.trim());
                        }
                        
                        String itemsPerCarton = getCellString(row, 5, formatter);
                        if (!isBlank(itemsPerCarton)) {
                            try {
                                Integer itemsPerCartonInt = Integer.parseInt(itemsPerCarton.trim());
                                item.put("nxDgItemsPerCarton", itemsPerCartonInt);
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("包装数量格式错误，应为整数");
                            }
                        }

                        resultItem.put("targetSort", currentSort);

                        GoodsMatchResult disMatch = matchDistributerGoods(disId, item);
                        if (disMatch.status == MatchStatus.ERROR) {
                            throw new IllegalArgumentException(disMatch.message);
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
                                disGoods = downloadNxGoods(disId, nxMatch.nxGoods, resultItem);
                                downloaded = true;
                            }
                        }

                        if (disGoods == null) {
                            disGoods = createTemporaryGoods(disId, item, resultItem);
                            createdTemp = true;
                        }

                        if (disGoods == null || disGoods.getNxDistributerGoodsId() == null) {
                            throw new IllegalArgumentException("未找到可用商品");
                        }

                        ShelfInsertResult shelfResult = insertShelfGoods(disGoods.getNxDistributerGoodsId(), shelfId, currentSort);
                        if (shelfResult == null || shelfResult.shelfGoods == null) {
                            throw new IllegalArgumentException("添加货架商品失败: 未返回有效结果");
                        }

                        if (!shelfResult.existed) {
                            // 只有成功添加新商品时才递增排序号
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
                        success++;

                    } catch (Exception rowEx) {
                        Map<String, Object> fail = new HashMap<>();
                        fail.put("row", rowIndex + 1);
                        fail.put("goodsName", goodsName);
                        fail.put("standard", standard);
                        fail.put("message", rowEx.getMessage());
                        failItems.add(fail);
                        resultItem.put("status", "failed");
                        resultItem.put("message", rowEx.getMessage());
                        resultItems.add(resultItem);
                        System.err.println("[importShelfGoodsFromExcel] 行导入失败 row=" + (rowIndex + 1)
                                + ", 原因=" + rowEx.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[importShelfGoodsFromExcel] 读取文件失败: " + e.getMessage());
            return R.error(-1, "文件读取失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[importShelfGoodsFromExcel] 解析文件失败: " + e.getMessage());
            return R.error(-1, "文件格式不支持，请使用模板导出的 Excel");
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    System.err.println("[importShelfGoodsFromExcel] 关闭工作簿异常: " + e.getMessage());
                }
            }
        }

        System.out.println("[importShelfGoodsFromExcel] 导入完成 total=" + total + ", success=" + success + ", failed=" + failItems.size());
        return R.ok()
                .put("total", total)
                .put("success", success)
                .put("failed", failItems.size())
                .put("items", resultItems)
                .put("failItems", failItems);
    }

    /**
     * 从 Excel 行中获取字符串值
     */
    private String getCellString(Row row, int cellIndex, DataFormatter formatter) {
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell);
        return value == null ? null : value.trim();
    }

    /**
     * 临时商品修改后重新查询系统商品
     * 当临时商品修改了名称、规格或规格重量后，调用此接口重新查询系统商品
     * 如果找到系统商品，则下载并更新临时商品
     */
    @RequestMapping(value = "/reSearchNxGoodsForTempGoods", method = RequestMethod.POST)
    @ResponseBody
    public R reSearchNxGoodsForTempGoods(@RequestBody JSONObject payload) {
        Integer disGoodsId = payload.getInteger("disGoodsId");
        String goodsName = payload.getString("nxDgGoodsName");
        String standard = payload.getString("nxDgGoodsStandardname");
        String standardWeight = payload.getString("nxDgGoodsStandardWeight");
        String brand = payload.getString("nxDgGoodsBrand");
        String place = payload.getString("nxDgGoodsPlace");

        System.out.println("[reSearchNxGoodsForTempGoods] disGoodsId=" + disGoodsId + ", goodsName=" + goodsName
                + ", standard=" + standard + ", standardWeight=" + standardWeight);

        if (disGoodsId == null) {
            return R.error(-1, "参数错误: 缺少 disGoodsId");
        }

        // 查询临时商品
        NxDistributerGoodsEntity tempGoods = nxDistributerGoodsService.queryObject(disGoodsId);
        if (tempGoods == null) {
            return R.error(-1, "商品不存在");
        }

        // 验证是否为临时商品（临时商品的 nxDgNxGoodsId 为 null）
        if (tempGoods.getNxDgNxGoodsId() != null) {
            return R.error(-1, "该商品不是临时商品，无需重新查询");
        }

        // 使用传入的参数或临时商品的当前信息
        String searchName = isBlank(goodsName) ? tempGoods.getNxDgGoodsName() : goodsName;
        String searchStandard = isBlank(standard) ? tempGoods.getNxDgGoodsStandardname() : standard;
        String searchWeight = isBlank(standardWeight) ? tempGoods.getNxDgGoodsStandardWeight() : standardWeight;
        String searchBrand = isBlank(brand) ? tempGoods.getNxDgGoodsBrand() : brand;
        String searchPlace = isBlank(place) ? tempGoods.getNxDgGoodsPlace() : place;

        // 如果名称或规格为空，更新临时商品的其他字段后返回
        if (isBlank(searchName)) {
            // 更新其他字段
            if (!isBlank(searchStandard)) {
                tempGoods.setNxDgGoodsStandardname(searchStandard);
            }
            if (!isBlank(searchWeight)) {
                tempGoods.setNxDgGoodsStandardWeight(searchWeight);
            }
            if (!isBlank(searchBrand)) {
                tempGoods.setNxDgGoodsBrand(searchBrand);
            }
            if (!isBlank(searchPlace)) {
                tempGoods.setNxDgGoodsPlace(searchPlace);
            }
            nxDistributerGoodsService.update(tempGoods);
            return R.ok()
                    .put("disGoodsId", tempGoods.getNxDistributerGoodsId())
                    .put("message", "商品名称不能为空，已更新临时商品的其他字段");
        }
        if (isBlank(searchStandard)) {
            // 更新其他字段
            if (!isBlank(searchName)) {
                tempGoods.setNxDgGoodsName(searchName);
            }
            if (!isBlank(searchWeight)) {
                tempGoods.setNxDgGoodsStandardWeight(searchWeight);
            }
            if (!isBlank(searchBrand)) {
                tempGoods.setNxDgGoodsBrand(searchBrand);
            }
            if (!isBlank(searchPlace)) {
                tempGoods.setNxDgGoodsPlace(searchPlace);
            }
            nxDistributerGoodsService.update(tempGoods);
            return R.ok()
                    .put("disGoodsId", tempGoods.getNxDistributerGoodsId())
                    .put("message", "商品规格不能为空，已更新临时商品的其他字段");
        }

        // 构建查询参数
        JSONObject searchItem = new JSONObject();
        searchItem.put("nxDgGoodsName", searchName);
        searchItem.put("nxDgGoodsStandardname", searchStandard);
        searchItem.put("nxDgGoodsStandardWeight", searchWeight);
        searchItem.put("nxDgGoodsBrand", searchBrand);
        searchItem.put("nxDgGoodsPlace", searchPlace);

        // 查询系统商品
        GoodsMatchResult nxMatch = matchNxGoods(searchItem);
        if (nxMatch.status == MatchStatus.CONFLICT) {
            // 匹配到多条，保存为临时商品，名称后添加"系统多条"
            String newGoodsName = searchName + "系统多条";
            tempGoods.setNxDgGoodsName(newGoodsName);
            if (!isBlank(searchStandard)) {
                tempGoods.setNxDgGoodsStandardname(searchStandard);
            }
            if (!isBlank(searchWeight)) {
                tempGoods.setNxDgGoodsStandardWeight(searchWeight);
            }
            if (!isBlank(searchBrand)) {
                tempGoods.setNxDgGoodsBrand(searchBrand);
            }
            if (!isBlank(searchPlace)) {
                tempGoods.setNxDgGoodsPlace(searchPlace);
            }
            nxDistributerGoodsService.update(tempGoods);
            return R.ok()
                    .put("disGoodsId", tempGoods.getNxDistributerGoodsId())
                    .put("candidates", buildNxCandidateArray(nxMatch.nxCandidates))
                    .put("message", "系统商品匹配多条，已保存为临时商品（名称已添加'系统多条'）");
        }
        if (nxMatch.status == MatchStatus.ERROR) {
            // 匹配错误，保存为临时商品
            if (!isBlank(searchName)) {
                tempGoods.setNxDgGoodsName(searchName);
            }
            if (!isBlank(searchStandard)) {
                tempGoods.setNxDgGoodsStandardname(searchStandard);
            }
            if (!isBlank(searchWeight)) {
                tempGoods.setNxDgGoodsStandardWeight(searchWeight);
            }
            if (!isBlank(searchBrand)) {
                tempGoods.setNxDgGoodsBrand(searchBrand);
            }
            if (!isBlank(searchPlace)) {
                tempGoods.setNxDgGoodsPlace(searchPlace);
            }
            nxDistributerGoodsService.update(tempGoods);
            return R.ok()
                    .put("disGoodsId", tempGoods.getNxDistributerGoodsId())
                    .put("message", "系统商品匹配错误，已保存为临时商品: " + nxMatch.message);
        }
        if (nxMatch.nxGoods == null) {
            // 未找到匹配的系统商品，保存为临时商品
            if (!isBlank(searchName)) {
                tempGoods.setNxDgGoodsName(searchName);
            }
            if (!isBlank(searchStandard)) {
                tempGoods.setNxDgGoodsStandardname(searchStandard);
            }
            if (!isBlank(searchWeight)) {
                tempGoods.setNxDgGoodsStandardWeight(searchWeight);
            }
            if (!isBlank(searchBrand)) {
                tempGoods.setNxDgGoodsBrand(searchBrand);
            }
            if (!isBlank(searchPlace)) {
                tempGoods.setNxDgGoodsPlace(searchPlace);
            }
            nxDistributerGoodsService.update(tempGoods);
            return R.ok()
                    .put("disGoodsId", tempGoods.getNxDistributerGoodsId())
                    .put("message", "未找到匹配的系统商品，已保存为临时商品");
        }

        // 找到系统商品，检查是否已下载
        Integer disId = tempGoods.getNxDgDistributerId();
        Map<String, Object> checkParams = new HashMap<>();
        checkParams.put("disId", disId);
        checkParams.put("goodsId", nxMatch.nxGoods.getNxGoodsId());
        List<NxDistributerGoodsEntity> exists = nxDistributerGoodsService.queryDisGoodsByParams(checkParams);
        
        NxGoodsEntity detail = nxGoodsService.queryObject(nxMatch.nxGoods.getNxGoodsId());
        
        if (exists != null && !exists.isEmpty()) {
            // 系统商品已下载，直接更新临时商品为系统商品
            System.out.println("[reSearchNxGoodsForTempGoods] 系统商品已下载，直接更新临时商品");
            updateTempGoodsToSystemGoods(tempGoods, detail);
            nxDistributerGoodsService.update(tempGoods);
            // 复制规格和别名（如果还没有的话）
            copyStandardsAndAlias(tempGoods, detail);
            return R.ok().put("disGoodsId", tempGoods.getNxDistributerGoodsId())
                    .put("nxGoodsId", nxMatch.nxGoods.getNxGoodsId())
                    .put("message", "已更新临时商品为系统商品（系统商品已存在）");
        } else {
            // 系统商品未下载，先下载系统商品
            try {
                JSONObject resultItem = new JSONObject();
                NxDistributerGoodsEntity downloadedGoods = downloadNxGoods(disId, nxMatch.nxGoods, resultItem);
                System.out.println("[reSearchNxGoodsForTempGoods] 下载系统商品成功，disGoodsId=" + downloadedGoods.getNxDistributerGoodsId());
                
                // 如果下载的商品就是临时商品本身（理论上不会发生，因为临时商品没有 nxDgNxGoodsId）
                // 但 downloadNxGoods 会检查是否已存在，如果临时商品已存在同名商品，可能会返回临时商品
                // 所以我们需要更新临时商品
                if (downloadedGoods.getNxDistributerGoodsId().equals(disGoodsId)) {
                    // 临时商品就是下载的商品，已经更新好了
                    return R.ok().put("disGoodsId", tempGoods.getNxDistributerGoodsId())
                            .put("nxGoodsId", nxMatch.nxGoods.getNxGoodsId())
                            .put("message", "已更新临时商品为系统商品");
                } else {
                    // 下载的商品是新商品，更新临时商品关联到系统商品
                    updateTempGoodsToSystemGoods(tempGoods, detail);
                    nxDistributerGoodsService.update(tempGoods);
                    // 复制规格和别名
                    copyStandardsAndAlias(tempGoods, detail);
                    return R.ok().put("disGoodsId", tempGoods.getNxDistributerGoodsId())
                            .put("nxGoodsId", nxMatch.nxGoods.getNxGoodsId())
                            .put("message", "已更新临时商品为系统商品");
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 下载失败，保存为临时商品
                if (!isBlank(searchName)) {
                    tempGoods.setNxDgGoodsName(searchName);
                }
                if (!isBlank(searchStandard)) {
                    tempGoods.setNxDgGoodsStandardname(searchStandard);
                }
                if (!isBlank(searchWeight)) {
                    tempGoods.setNxDgGoodsStandardWeight(searchWeight);
                }
                if (!isBlank(searchBrand)) {
                    tempGoods.setNxDgGoodsBrand(searchBrand);
                }
                if (!isBlank(searchPlace)) {
                    tempGoods.setNxDgGoodsPlace(searchPlace);
                }
                nxDistributerGoodsService.update(tempGoods);
                return R.ok()
                        .put("disGoodsId", tempGoods.getNxDistributerGoodsId())
                        .put("message", "下载系统商品失败，已保存为临时商品: " + e.getMessage());
            }
        }
    }

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
            if (itemsPerCartonObj instanceof Integer) {
                goods.setNxDgItemsPerCarton((Integer) itemsPerCartonObj);
            } else if (itemsPerCartonObj instanceof String) {
                String itemsPerCartonStr = ((String) itemsPerCartonObj).trim();
                if (!itemsPerCartonStr.isEmpty()) {
                    try {
                        goods.setNxDgItemsPerCarton(Integer.parseInt(itemsPerCartonStr));
                    } catch (NumberFormatException e) {
                        // 忽略格式错误，不设置该字段
                    }
                }
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
            map.put("name", ordersEntity.getNxDoGoodsName());
            System.out.println("部门商品搜索" + map);
            List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
            if (departmentDisGoodsEntities.size() == 1) {
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = departmentDisGoodsEntities.get(0);
                Integer nxDdgDisGoodsId = nxDepartmentDisGoodsEntity.getNxDdgDisGoodsId();
                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(nxDdgDisGoodsId);
                returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
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
                                returnList.add(saveLinshiGoodsForPasteOrder(ordersEntity));
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
                            returnList.add(saveLinshiGoodsForPasteOrder(ordersEntity));
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

        }

        return R.ok().put("data", returnList);
    }


    private NxDepartmentOrdersEntity saveLinshiGoodsForPasteOrder(NxDepartmentOrdersEntity ordersEntity) {

        String goodsName = ordersEntity.getNxDoGoodsName();
        String goodsStandard = ordersEntity.getNxDoStandard();
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
        goods.setNxDgPurchaseAuto(-1);
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
        goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
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
        ordersEntity.setNxDoPrintStandard(ordersEntity.getNxDoStandard());
        ordersEntity.setNxDoExpectPrice("0.1");
        ordersEntity.setNxDoCostPriceUpdate(formatWhatDay(0));
        ordersEntity.setNxDoDepDisGoodsId(-1);
        ordersEntity.setNxDoGoodsType(1);
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
            // 获取分销商ID，用于检查商品是否已下载
            Integer disId = order.getNxDoDistributerId();
                
                // 1. 收集所有分销商商品对应的系统商品ID（非null的）
                Set<Integer> distributerGoodsNxGoodsIds = new HashSet<>();
            List<NxDistributerGoodsEntity> distributerGoodsList = order.getNxDistributerGoodsEntityList();
            if (distributerGoodsList != null && distributerGoodsList.size() > 0) {
                logger.info("[aaaTemp] 已有分销商商品候选列表（数量: {}），需要去除系统商品中已出现的商品", distributerGoodsList.size());
                for (NxDistributerGoodsEntity distributerGoods : distributerGoodsList) {
                    if (distributerGoods.getNxDgNxGoodsId() != null) {
                        distributerGoodsNxGoodsIds.add(distributerGoods.getNxDgNxGoodsId());
                    }
                }
                logger.info("[aaaTemp] 分销商商品对应的系统商品ID集合: {}", distributerGoodsNxGoodsIds);
            }
                
            // 2. 过滤系统商品：只保留从未下载过的商品
            //    如果商品已下载，需要将其对应的分销商商品加入到候选列表中
                TreeSet<NxGoodsEntity> filteredNxGoodsSet = new TreeSet<>();
            List<NxDistributerGoodsEntity> distributerGoodsToAdd = new ArrayList<>();
            
                for (NxGoodsEntity nxGoods : all) {
                if (nxGoods.getNxGoodsId() == null) {
                    continue;
                }
                
                Integer nxGoodsId = nxGoods.getNxGoodsId();
                
                // 2.1 检查是否已在候选列表中
                boolean inCandidateList = distributerGoodsNxGoodsIds.contains(nxGoodsId);
                
                // 2.2 检查是否已在数据库中下载过（需要分销商ID）
                boolean alreadyDownloaded = false;
                NxDistributerGoodsEntity downloadedGoods = null;
                if (disId != null) {
                    Map<String, Object> checkParams = new HashMap<>();
                    checkParams.put("disId", disId);
                    checkParams.put("goodsId", nxGoodsId);
                    List<NxDistributerGoodsEntity> downloadedGoodsList = nxDistributerGoodsService.queryDisGoodsByParams(checkParams);
                    if (downloadedGoodsList != null && !downloadedGoodsList.isEmpty()) {
                        alreadyDownloaded = true;
                        downloadedGoods = downloadedGoodsList.get(0);
                        logger.info("[aaaTemp] 系统商品 nxGoodsId={} 已在数据库中下载过，对应的分销商商品ID={}", 
                                nxGoodsId, downloadedGoods.getNxDistributerGoodsId());
                    }
                }
                
                // 2.3 如果已下载或已在候选列表中，不加入系统商品列表
                if (inCandidateList || alreadyDownloaded) {
                    // 如果已下载但不在候选列表中，需要加入到候选列表
                    if (alreadyDownloaded && !inCandidateList && downloadedGoods != null) {
                        // 检查是否已经在候选列表中（通过ID比较）
                        boolean existsInList = false;
                        if (distributerGoodsList != null) {
                            for (NxDistributerGoodsEntity existing : distributerGoodsList) {
                                if (existing.getNxDistributerGoodsId() != null && 
                                    existing.getNxDistributerGoodsId().equals(downloadedGoods.getNxDistributerGoodsId())) {
                                    existsInList = true;
                                    break;
                                }
                            }
                        }
                        if (!existsInList) {
                            distributerGoodsToAdd.add(downloadedGoods);
                            logger.info("[aaaTemp] 将已下载的分销商商品（ID={}）加入到候选列表", downloadedGoods.getNxDistributerGoodsId());
                        }
                    }
                } else {
                    // 从未下载过，加入系统商品列表
                        filteredNxGoodsSet.add(nxGoods);
                    }
                }
                
            // 3. 将已下载的商品加入到候选列表（如果候选列表存在）
            if (!distributerGoodsToAdd.isEmpty() && distributerGoodsList != null) {
                distributerGoodsList.addAll(distributerGoodsToAdd);
                logger.info("[aaaTemp] 已将 {} 个已下载的分销商商品加入到候选列表", distributerGoodsToAdd.size());
            } else if (!distributerGoodsToAdd.isEmpty()) {
                // 如果候选列表不存在，创建新的列表
                order.setNxDistributerGoodsEntityList(distributerGoodsToAdd);
                logger.info("[aaaTemp] 创建新的候选列表，包含 {} 个已下载的分销商商品", distributerGoodsToAdd.size());
            }
            
            // 4. 设置过滤后的系统商品列表
                if (filteredNxGoodsSet.size() > 0) {
                logger.info("[aaaTemp] 过滤后，系统商品列表数量: {}（原始数量: {}），这些商品都是从未下载过的", 
                        filteredNxGoodsSet.size(), all.size());
                    order.setNxGoodsEntities(filteredNxGoodsSet);
                } else {
                logger.info("[aaaTemp] 过滤后，系统商品列表为空（所有系统商品都已下载过或已在候选列表中）");
            }
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

        // 创建或关联训练数据（未找到商品的情况）
        // 如果订单已经关联了训练数据ID，直接返回，不重复创建
        if (order.getNxDoTrainingDataId() != null) {
            logger.info("[aaaTemp] 订单已关联训练数据ID: {}, 订单ID: {}, 跳过创建训练数据", 
                    order.getNxDoTrainingDataId(), order.getNxDepartmentOrdersId());
            return order;
        }

        // 查询是否已有匹配的训练数据
        Map<String, Object> matchParams = new HashMap<>();
        matchParams.put("departmentId", order.getNxDoDepartmentId());
        matchParams.put("goodsName", order.getNxDoGoodsName());
        // 不再使用 standard 和 standardWeight 作为查询条件

        NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);

        if (matchedTrainingData != null) {
            // 如果找到了匹配的训练数据，关联到订单
            order.setNxDoTrainingDataId(matchedTrainingData.getNxOtdId());
            nxDepartmentOrdersService.update(order);
            logger.info("[aaaTemp] 关联已有训练数据到订单，训练数据ID: {}, 订单ID: {}", 
                    matchedTrainingData.getNxOtdId(), order.getNxDepartmentOrdersId());
        } else {
            // 如果没有找到匹配的训练数据，创建新的训练数据
            NxOrderOcrTrainingDataEntity trainingData = new NxOrderOcrTrainingDataEntity();
            trainingData.setNxOtdDepartmentId(order.getNxDoDepartmentId());
            trainingData.setNxOtdDepartmentFatherId(order.getNxDoDepartmentFatherId());
            trainingData.setNxOtdDistributerId(order.getNxDoDistributerId());
            trainingData.setNxOtdOriginalGoodsName(order.getNxDoGoodsName());
            trainingData.setNxOtdOriginalQuantity(order.getNxDoQuantity());
            trainingData.setNxOtdOriginalStandard(order.getNxDoStandard());
            trainingData.setNxOtdOriginalStandardWeight(null); // 订单实体中没有 standardWeight 字段
            trainingData.setNxOtdOriginalRemark(order.getNxDoRemark() != null ? order.getNxDoRemark() : "");
            trainingData.setNxOtdIsNameManuallyAnnotated(0);
            trainingData.setNxOtdIsQuantityManuallyAnnotated(0);
            trainingData.setNxOtdIsStandardManuallyAnnotated(0);
            trainingData.setNxOtdIsStandardWeightManuallyAnnotated(0);
            trainingData.setNxOtdIsRemarkManuallyAnnotated(0);
            // 最终确认字段不提前赋值，等待用户手动标注
            trainingData.setNxOtdFinalGoodsName(null);
            trainingData.setNxOtdFinalQuantity(null);
            trainingData.setNxOtdFinalStandard(null);
            trainingData.setNxOtdFinalStandardWeight(null);
            trainingData.setNxOtdFinalRemark(null);
            trainingData.setNxOtdOrderId(order.getNxDepartmentOrdersId());
            trainingData.setNxOtdDataSource("PASTE_SEARCH");
            trainingData.setNxOtdCreateDate(formatWhatYearDayTime(0));
            trainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
            trainingData.setNxOtdCreateUserId(order.getNxDoOrderUserId() != null ? order.getNxDoOrderUserId() : -1);

            nxOrderOcrTrainingDataService.save(trainingData);
            logger.info("[aaaTemp] 创建训练数据（未找到商品），训练数据ID: {}, 订单ID: {}", 
                    trainingData.getNxOtdId(), order.getNxDepartmentOrdersId());
            // 关联训练数据ID到订单
            order.setNxDoTrainingDataId(trainingData.getNxOtdId());
            nxDepartmentOrdersService.update(order);
        }

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
                    System.out.println("mapzreorororor" + mapZero);
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
                            String pinyinString = goodsName;
// 如果包含汉字才转换，否则直接用原名
                            if (goodsName.matches(".*[\u4E00-\u9FFF]+.*")) {
                                pinyinString = hanziToPinyin(goodsName);
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

    private NxDepartmentOrdersEntity saveOneOrderCash(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
        System.out.println("saveONeOrderereerereeqonenenneorere");
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoNxGoodsId(disGoodsEntity.getNxDgNxGoodsId());
        order.setNxDoNxGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
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
        order.setNxDoCostPriceLevel("1");

        // 使用完善的价格处理逻辑（平台型/非平台型分支、货架库存查询、规格匹配等）
//        processOrderPrice(order, disGoodsEntity);

        nxDepartmentOrdersService.save(order);

        //auto
        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
            savePurGoodsAuto(order);
        }
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDistributerGoodsEntity(distributerGoodsEntity);
        return order;
    }

    public NxDepartmentOrdersEntity saveOneOrder(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
        System.out.println("saveONeOrderereerereeqonenenneorere");
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
        // 保存订单的原始状态（如果已经是 -2 待修正，则保持，否则设置为 0）
        Integer originalStatus = order.getNxDoStatus();
        if (originalStatus == null || originalStatus != -2) {
            order.setNxDoStatus(0);
        }
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
        order.setNxDoCostPriceLevel("1");


        // 使用完善的价格处理逻辑（平台型/非平台型分支、货架库存查询、规格匹配等）
        processOrderPrice(order, disGoodsEntity);

        System.out.println("jieghuasu11111" + order.getNxDoPrice());

        System.out.println("jieghuasu11111" + order.getNxDoPrice());

        Map<String, Object> map = new HashMap<>();
        map.put("depId", order.getNxDoDepartmentId());
        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
        map.put("standard", order.getNxDoStandard());
        System.out.println("depmapapmmdmmdd" + map);
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if (departmentDisGoodsEntity != null) {
            System.out.println("prinstnan" + departmentDisGoodsEntity.getNxDdgOrderStandard());
            // 判断是否使用了大包装单价，如果是则设置打印规格为大包装单位（支持智能匹配：件=箱）
            boolean isUsingCartonPrice = disGoodsEntity.getNxDgCartonUnit() != null
                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                    && order.getNxDoStandard() != null
                    && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim());

            if (isUsingCartonPrice) {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
                System.out.println("使用大包装单价，打印规格设置为: " + disGoodsEntity.getNxDgCartonUnit());
            } else if (order.getNxDoCostPriceLevel() == null || order.getNxDoCostPriceLevel().equals("1")) {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
            } else {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
            }

            order.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
            //如果有重量和单价，则计算 subtotal
            if(order.getNxDoWeight() != null && !order.getNxDoWeight().trim().isEmpty()
                    && order.getNxDoPrice() != null && !order.getNxDoPrice().trim().isEmpty()){
                try {
                    BigDecimal weight = new BigDecimal(order.getNxDoWeight());
                    BigDecimal price = new BigDecimal(order.getNxDoPrice());
                    BigDecimal subtotal = weight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                    order.setNxDoSubtotal(subtotal.toString());
                } catch (NumberFormatException e) {
                    // 如果转换失败，不设置subtotal
                    logger.warn("[saveOneOrder] 计算subtotal失败，重量或单价格式错误: weight={}, price={}, error={}",
                                order.getNxDoWeight(), order.getNxDoPrice(), e.getMessage());
                }
            }
            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
        }else{
            map.put("standard", null);
            System.out.println("depmapapapappapa" + map);
            NxDepartmentDisGoodsEntity departmentDisGoodsEntityO = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
            if (departmentDisGoodsEntityO != null) {
                order.setNxDoDepDisGoodsId(departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
            }
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

        System.out.println("jieghuasu2222" + order.getNxDoPrice());
        nxDepartmentOrdersService.save(order);

        //auto
        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
            savePurGoodsAuto(order);
        }
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDistributerGoodsEntity(distributerGoodsEntity);
        
        // 确保返回的订单状态正确
        logger.info("[saveOneOrder] 返回订单: 商品名称={}, 订单ID={}, 状态={}, 分销商商品ID={}", 
                order.getNxDoGoodsName(), order.getNxDepartmentOrdersId(), 
                order.getNxDoStatus(), order.getNxDoDisGoodsId());
        
        // 找到商品后，设置为0（已保存），但如果订单状态是 -2（待修正），则保持 -2
        if (order.getNxDoStatus() == null || (order.getNxDoStatus() != 0 && order.getNxDoStatus() != -2)) {
            order.setNxDoStatus(0);
        }
        
        return order;
    }

    /**
     * 创建或更新训练数据（当订单保存成功后）
     * 如果订单没有关联训练数据ID，则创建新的训练数据；如果已关联，则更新训练数据
     * 
     * @param order 订单实体（已保存，包含订单ID）
     * @param originalGoodsName 原始商品名称（可选，如果为null则使用订单中的商品名称）
     * @param originalQuantity 原始数量（可选，如果为null则使用订单中的数量）
     * @param originalStandard 原始规格（可选，如果为null则使用订单中的规格）
     * @param originalRemark 原始备注（可选，如果为null则使用订单中的备注）
     */
    private void createOrUpdateTrainingDataAfterOrderSaved(NxDepartmentOrdersEntity order, 
            String originalGoodsName, String originalQuantity, String originalStandard, String originalRemark) {
        try {
            // 如果订单对象中没有 trainingDataId，从数据库中重新查询订单（确保获取最新的数据）
            Integer trainingDataId = order.getNxDoTrainingDataId();
            if (trainingDataId == null && order.getNxDepartmentOrdersId() != null) {
                NxDepartmentOrdersEntity savedOrder = nxDepartmentOrdersService.queryObjectNew(order.getNxDepartmentOrdersId());
                if (savedOrder != null && savedOrder.getNxDoTrainingDataId() != null) {
                    trainingDataId = savedOrder.getNxDoTrainingDataId();
                    order.setNxDoTrainingDataId(trainingDataId);
                    logger.info("[createOrUpdateTrainingDataAfterOrderSaved] 从数据库查询到训练数据ID: {}, 订单ID: {}", 
                            trainingDataId, order.getNxDepartmentOrdersId());
                }
            }
            
            // 如果订单已经关联了训练数据ID，则更新训练数据
            if (trainingDataId != null) {
                NxOrderOcrTrainingDataEntity trainingData = nxOrderOcrTrainingDataService.queryObject(trainingDataId);
                if (trainingData != null) {
                    // 更新训练数据：自动识别标志=1，填充商品ID和final字段
                    if (trainingData.getNxOtdDisGoodsId() == null && order.getNxDoDisGoodsId() != null) {
                        trainingData.setNxOtdDisGoodsId(order.getNxDoDisGoodsId());
                        trainingData.setNxOtdOrderId(order.getNxDepartmentOrdersId());
                        trainingData.setNxOtdFinalGoodsName(order.getNxDoGoodsName());
                        trainingData.setNxOtdFinalQuantity(order.getNxDoQuantity());
                        trainingData.setNxOtdFinalStandard(order.getNxDoStandard());
                        trainingData.setNxOtdFinalRemark(order.getNxDoRemark() != null ? order.getNxDoRemark() : "");
                        // 自动识别标志设为 1
                        trainingData.setNxOtdIsNameManuallyAnnotated(1);
                        trainingData.setNxOtdIsQuantityManuallyAnnotated(1);
                        trainingData.setNxOtdIsStandardManuallyAnnotated(1);
                        trainingData.setNxOtdIsRemarkManuallyAnnotated(1);
                        trainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
                        nxOrderOcrTrainingDataService.update(trainingData);
                        logger.info("[createOrUpdateTrainingDataAfterOrderSaved] 更新训练数据（自动识别），训练数据ID: {}, 订单ID: {}, 商品ID: {}", 
                                trainingData.getNxOtdId(), order.getNxDepartmentOrdersId(), order.getNxDoDisGoodsId());
                    }
                }
                return;
            }

            // 如果订单没有关联训练数据ID，查询是否已有匹配的训练数据
            Map<String, Object> matchParams = new HashMap<>();
            matchParams.put("departmentId", order.getNxDoDepartmentId());
            matchParams.put("goodsName", originalGoodsName != null ? originalGoodsName : order.getNxDoGoodsName());
            // 不再使用 standard 和 standardWeight 作为查询条件

            NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);

            if (matchedTrainingData != null) {
                // 如果找到了匹配的训练数据，先关联到订单（避免重复创建）
                logger.info("[createOrUpdateTrainingDataAfterOrderSaved] 找到匹配的训练数据，训练数据ID: {}, 订单ID: {}", 
                        matchedTrainingData.getNxOtdId(), order.getNxDepartmentOrdersId());
                
                // 关联训练数据ID到订单（无论是否有商品ID都要关联，避免重复创建）
                order.setNxDoTrainingDataId(matchedTrainingData.getNxOtdId());
                nxDepartmentOrdersService.update(order);
                
                // 如果训练数据没有商品ID，且有商品ID，则更新训练数据
                if (matchedTrainingData.getNxOtdDisGoodsId() == null && order.getNxDoDisGoodsId() != null) {
                    matchedTrainingData.setNxOtdDisGoodsId(order.getNxDoDisGoodsId());
                    matchedTrainingData.setNxOtdOrderId(order.getNxDepartmentOrdersId());
                    matchedTrainingData.setNxOtdFinalGoodsName(order.getNxDoGoodsName());
                    matchedTrainingData.setNxOtdFinalQuantity(order.getNxDoQuantity());
                    matchedTrainingData.setNxOtdFinalStandard(order.getNxDoStandard());
                    matchedTrainingData.setNxOtdFinalRemark(order.getNxDoRemark() != null ? order.getNxDoRemark() : "");
                    // 自动识别标志设为 1
                    matchedTrainingData.setNxOtdIsNameManuallyAnnotated(1);
                    matchedTrainingData.setNxOtdIsQuantityManuallyAnnotated(1);
                    matchedTrainingData.setNxOtdIsStandardManuallyAnnotated(1);
                    matchedTrainingData.setNxOtdIsRemarkManuallyAnnotated(1);
                    matchedTrainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
                    nxOrderOcrTrainingDataService.update(matchedTrainingData);
                    logger.info("[createOrUpdateTrainingDataAfterOrderSaved] 更新匹配的训练数据（自动识别），训练数据ID: {}, 订单ID: {}, 商品ID: {}", 
                            matchedTrainingData.getNxOtdId(), order.getNxDepartmentOrdersId(), order.getNxDoDisGoodsId());
                } else {
                    logger.info("[createOrUpdateTrainingDataAfterOrderSaved] 匹配的训练数据已有商品ID或订单无商品ID，仅关联到订单，训练数据ID: {}, 订单ID: {}", 
                            matchedTrainingData.getNxOtdId(), order.getNxDepartmentOrdersId());
                }
                return; // 找到匹配的训练数据，关联后直接返回，不创建新的
            } else {
                // 如果没有找到匹配的训练数据，再次尝试通过订单关联的训练数据ID查询（避免重复创建）
                // 这种情况可能发生在：训练数据使用 rawName 创建，但查询时使用了 name（纠错后的名称）
                if (order.getNxDoTrainingDataId() != null) {
                    NxOrderOcrTrainingDataEntity existingTrainingData = nxOrderOcrTrainingDataService.queryObject(order.getNxDoTrainingDataId());
                    if (existingTrainingData != null) {
                        logger.info("[createOrUpdateTrainingDataAfterOrderSaved] 通过订单关联的训练数据ID找到训练数据，训练数据ID: {}, 订单ID: {}", 
                                existingTrainingData.getNxOtdId(), order.getNxDepartmentOrdersId());
                        // 如果训练数据没有商品ID，且有商品ID，则更新训练数据
                        if (existingTrainingData.getNxOtdDisGoodsId() == null && order.getNxDoDisGoodsId() != null) {
                            existingTrainingData.setNxOtdDisGoodsId(order.getNxDoDisGoodsId());
                            existingTrainingData.setNxOtdOrderId(order.getNxDepartmentOrdersId());
                            existingTrainingData.setNxOtdFinalGoodsName(order.getNxDoGoodsName());
                            existingTrainingData.setNxOtdFinalQuantity(order.getNxDoQuantity());
                            existingTrainingData.setNxOtdFinalStandard(order.getNxDoStandard());
                            existingTrainingData.setNxOtdFinalRemark(order.getNxDoRemark() != null ? order.getNxDoRemark() : "");
                            existingTrainingData.setNxOtdIsNameManuallyAnnotated(1);
                            existingTrainingData.setNxOtdIsQuantityManuallyAnnotated(1);
                            existingTrainingData.setNxOtdIsStandardManuallyAnnotated(1);
                            existingTrainingData.setNxOtdIsRemarkManuallyAnnotated(1);
                            existingTrainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
                            nxOrderOcrTrainingDataService.update(existingTrainingData);
                            logger.info("[createOrUpdateTrainingDataAfterOrderSaved] 更新已关联的训练数据（自动识别），训练数据ID: {}, 订单ID: {}, 商品ID: {}", 
                                    existingTrainingData.getNxOtdId(), order.getNxDepartmentOrdersId(), order.getNxDoDisGoodsId());
                        }
                        return; // 找到已关联的训练数据，直接返回，不创建新的
                    }
                }
                
                // 确实没有找到匹配的训练数据，创建新的训练数据
                logger.info("[createOrUpdateTrainingDataAfterOrderSaved] 未找到匹配的训练数据，创建新的训练数据，订单ID: {}, 商品名称: {}", 
                        order.getNxDepartmentOrdersId(), originalGoodsName != null ? originalGoodsName : order.getNxDoGoodsName());
                
                NxOrderOcrTrainingDataEntity trainingData = new NxOrderOcrTrainingDataEntity();
                trainingData.setNxOtdDepartmentId(order.getNxDoDepartmentId());
                trainingData.setNxOtdDepartmentFatherId(order.getNxDoDepartmentFatherId());
                trainingData.setNxOtdDistributerId(order.getNxDoDistributerId());
                trainingData.setNxOtdOriginalGoodsName(originalGoodsName != null ? originalGoodsName : order.getNxDoGoodsName());
                trainingData.setNxOtdOriginalQuantity(originalQuantity != null ? originalQuantity : order.getNxDoQuantity());
                trainingData.setNxOtdOriginalStandard(originalStandard != null ? originalStandard : order.getNxDoStandard());
                trainingData.setNxOtdOriginalStandardWeight(null); // 订单实体中没有 standardWeight 字段
                trainingData.setNxOtdOriginalRemark(originalRemark != null ? originalRemark : (order.getNxDoRemark() != null ? order.getNxDoRemark() : ""));
                trainingData.setNxOtdDisGoodsId(order.getNxDoDisGoodsId());
                trainingData.setNxOtdOrderId(order.getNxDepartmentOrdersId());
                trainingData.setNxOtdFinalGoodsName(order.getNxDoGoodsName());
                trainingData.setNxOtdFinalQuantity(order.getNxDoQuantity());
                trainingData.setNxOtdFinalStandard(order.getNxDoStandard());
                trainingData.setNxOtdFinalRemark(order.getNxDoRemark() != null ? order.getNxDoRemark() : "");
                // 自动识别标志设为 1
                trainingData.setNxOtdIsNameManuallyAnnotated(1);
                trainingData.setNxOtdIsQuantityManuallyAnnotated(1);
                trainingData.setNxOtdIsStandardManuallyAnnotated(1);
                trainingData.setNxOtdIsStandardWeightManuallyAnnotated(1);
                trainingData.setNxOtdIsRemarkManuallyAnnotated(1);
                trainingData.setNxOtdDataSource("PASTE_SEARCH");
                trainingData.setNxOtdCreateDate(formatWhatYearDayTime(0));
                trainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
                trainingData.setNxOtdCreateUserId(order.getNxDoOrderUserId() != null ? order.getNxDoOrderUserId() : -1);

                nxOrderOcrTrainingDataService.save(trainingData);
                logger.info("[createOrUpdateTrainingDataAfterOrderSaved] 创建训练数据（自动识别），训练数据ID: {}, 订单ID: {}, 商品ID: {}", 
                        trainingData.getNxOtdId(), order.getNxDepartmentOrdersId(), order.getNxDoDisGoodsId());
                // 关联训练数据ID到订单
                order.setNxDoTrainingDataId(trainingData.getNxOtdId());
                nxDepartmentOrdersService.update(order);
            }
        } catch (Exception e) {
            logger.error("[createOrUpdateTrainingDataAfterOrderSaved] 创建或更新训练数据失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响订单保存流程
        }
    }

    /**
     * 处理订单价格逻辑（平台型/非平台型分支、货架库存查询、规格匹配等）
     * 从 saveOneOrder 方法中提取的价格处理逻辑，供多个方法复用
     * 
     * @param order 订单实体
     * @param disGoodsEntity 分销商商品实体
     */
    private void processOrderPrice(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
        Integer nxDoDistributerId = order.getNxDoDistributerId();
        NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDoDistributerId);
        //平台型
        if (distributerEntity.getNxDistributerBusinessTypeId() > 6) {
            if (disGoodsEntity.getNxDgWillPriceTwo() != null
                    && !disGoodsEntity.getNxDgWillPriceTwo().trim().isEmpty()
                    && order.getNxDoStandard().equals(disGoodsEntity.getNxDgWillPriceTwoStandard())) {
                System.out.println("levlellelelelel111222222222222222");
                BigDecimal doQuantity = new BigDecimal(order.getNxDoQuantity());
                BigDecimal cosPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceTwo());
                BigDecimal willPrice = new BigDecimal(disGoodsEntity.getNxDgWillPriceTwo());
                BigDecimal costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

                // 判断是否使用大包装单价，如果是则设置打印规格为大包装单位
                if (disGoodsEntity.getNxDgCartonUnit() != null 
                        && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                        && order.getNxDoStandard() != null
                        && order.getNxDoStandard().trim().equals(disGoodsEntity.getNxDgCartonUnit().trim())) {
                    order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
                    System.out.println("使用大包装单价，打印规格设置为: " + disGoodsEntity.getNxDgCartonUnit());
                } else {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
                }
                order.setNxDoPrice(disGoodsEntity.getNxDgWillPriceTwo());
                order.setNxDoExpectPrice(disGoodsEntity.getNxDgWillPriceTwo());
                order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceTwo());
                order.setNxDoSubtotal(subtotal.toString());
                order.setNxDoCostSubtotal(costSubtotal.toString());
                order.setNxDoCostPriceLevel("2");
                order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceTwoUpdate());
                order.setNxDoProfitSubtotal(profit.toString());
                order.setNxDoProfitScale(scaleB.toString());

            } else {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
                order.setNxDoCostPriceLevel("1");
                order.setNxDoSubtotal("0");
                order.setNxDoCostSubtotal("0");
                System.out.println("jimimhuaaa" + disGoodsEntity.getNxDgWillPriceOne());
                if (disGoodsEntity.getNxDgWillPriceOne() != null && !disGoodsEntity.getNxDgWillPriceOne().trim().isEmpty()) {
                    System.out.println("jieghuasu?????????" + order.getNxDoPrice());
                    order.setNxDoPrice(disGoodsEntity.getNxDgWillPriceOne());
                    order.setNxDoExpectPrice(disGoodsEntity.getNxDgWillPriceOne());
                }
                if (disGoodsEntity.getNxDgBuyingPriceOne() != null && !disGoodsEntity.getNxDgBuyingPriceOne().trim().isEmpty()) {
                    order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceOneUpdate());
                    order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceOne());
                }
            }
        } else {
            // 初始设置打印规格：如果订单规格等于大包装单位，则设置为大包装单位
            // 支持智能匹配：件=箱，盒=箱等同义词
            System.out.println("======== 设置打印规格(printStandard)开始 ========");
            System.out.println("订单ID: " + order.getNxDepartmentOrdersId());
            System.out.println("订单规格(nxDoStandard): " + order.getNxDoStandard());
            System.out.println("商品大包装单位(nxDgCartonUnit): " + disGoodsEntity.getNxDgCartonUnit());
            System.out.println("商品标准规格(nxDgGoodsStandardname): " + disGoodsEntity.getNxDgGoodsStandardname());
            System.out.println("设置前 printStandard: " + order.getNxDoPrintStandard());
            
            if (disGoodsEntity.getNxDgCartonUnit() != null 
                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                    && order.getNxDoStandard() != null
                    && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim())) {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
                System.out.println("✅ [printStandard] 已设置为大包装单位: " + disGoodsEntity.getNxDgCartonUnit());
            } else {
            order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
                System.out.println("✅ [printStandard] 已设置为商品标准规格: " + disGoodsEntity.getNxDgGoodsStandardname());
            }
            System.out.println("设置后 printStandard: " + order.getNxDoPrintStandard());
            System.out.println("======== 设置打印规格(printStandard)结束 ========");
            System.out.println("======== 从货架获取价格开始 ========");
            System.out.println("订单ID: " + order.getNxDepartmentOrdersId());
            System.out.println("商品ID: " + disGoodsEntity.getNxDistributerGoodsId());
            System.out.println("商品名称: " + disGoodsEntity.getNxDgGoodsName());
            System.out.println("商品名称12: " + disGoodsEntity.getNxDgCartonUnit());
            System.out.println("商品名称3333: " + disGoodsEntity.getNxDgCartonUnit());
            System.out.println("订单规格: " + order.getNxDoStandard());
            System.out.println("订单规格ppppp: " + order.getNxDoPrintStandard());
            System.out.println("商品规格: " + disGoodsEntity.getNxDgGoodsStandardname());

            Integer nxDistributerGoodsId = disGoodsEntity.getNxDistributerGoodsId();
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", nxDistributerGoodsId);
            map.put("restWeight", 0);

            System.out.println("查询货架库存参数: " + map);
            List<NxDistributerGoodsShelfStockEntity> stockEntities = nxDisGoodsShelfStockService.queryShelfStockListByParams(map);
            System.out.println("查询到库存批次数量: " + stockEntities.size());

            if (stockEntities.size() > 0) {
                NxDistributerGoodsShelfStockEntity nxDistributerGoodsShelfStockEntity = stockEntities.get(stockEntities.size() - 1);
                System.out.println("--- 最后一个库存批次信息 ---");
                System.out.println("库存批次ID: " + nxDistributerGoodsShelfStockEntity.getNxDistributerGoodsShelfStockId());
                System.out.println("成本价(nxDgssPrice): " + nxDistributerGoodsShelfStockEntity.getNxDgssPrice());
                System.out.println("外包装成本价(nxDgssPriceCarton): " + nxDistributerGoodsShelfStockEntity.getNxDgssPriceCarton());
                System.out.println("销售价(nxDgssSellingPrice): " + nxDistributerGoodsShelfStockEntity.getNxDgssSellingPrice());
                System.out.println("外包装销售价(nxDgssSellingPriceCarton): " + nxDistributerGoodsShelfStockEntity.getNxDgssSellingPriceCarton());
                System.out.println("剩余重量: " + nxDistributerGoodsShelfStockEntity.getNxDgssRestWeight());
                System.out.println("订单规格: " + order.getNxDoStandard());
                System.out.println("商品外包装单位: " + disGoodsEntity.getNxDgCartonUnit());

                // 判断订单规格是否与外包装单位匹配
                boolean useCartonPrice = false;
                if (disGoodsEntity.getNxDgCartonUnit() != null 
                        && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                        && order.getNxDoStandard() != null
                        && order.getNxDoStandard().trim().equals(disGoodsEntity.getNxDgCartonUnit().trim())) {
                    useCartonPrice = true;
                    System.out.println("✅ 订单规格与外包装单位匹配，将使用外包装单价");
                }

                // 根据匹配情况选择对应的单价
                String sellingPrice = null;
                String costPrice = null;
                
                if (useCartonPrice) {
                    // 使用外包装单价
                    if (nxDistributerGoodsShelfStockEntity.getNxDgssSellingPriceCarton() != null 
                            && !nxDistributerGoodsShelfStockEntity.getNxDgssSellingPriceCarton().trim().isEmpty()) {
                        sellingPrice = nxDistributerGoodsShelfStockEntity.getNxDgssSellingPriceCarton();
                        System.out.println("使用外包装建议零售价: " + sellingPrice);
                    }
                    if (nxDistributerGoodsShelfStockEntity.getNxDgssPriceCarton() != null 
                            && !nxDistributerGoodsShelfStockEntity.getNxDgssPriceCarton().trim().isEmpty()) {
                        costPrice = nxDistributerGoodsShelfStockEntity.getNxDgssPriceCarton();
                        System.out.println("使用外包装采购单价: " + costPrice);
                    }
                } else {
                    // 使用最小单位单价
                    if (nxDistributerGoodsShelfStockEntity.getNxDgssSellingPrice() != null 
                            && !nxDistributerGoodsShelfStockEntity.getNxDgssSellingPrice().trim().isEmpty()) {
                        sellingPrice = nxDistributerGoodsShelfStockEntity.getNxDgssSellingPrice();
                        System.out.println("使用最小单位建议零售价: " + sellingPrice);
                    }
                    if (nxDistributerGoodsShelfStockEntity.getNxDgssPrice() != null 
                            && !nxDistributerGoodsShelfStockEntity.getNxDgssPrice().trim().isEmpty()) {
                        costPrice = nxDistributerGoodsShelfStockEntity.getNxDgssPrice();
                        System.out.println("使用最小单位采购单价: " + costPrice);
                    }
                }

                // 设置订单价格
                if (sellingPrice != null) {
                    System.out.println("✅ 找到销售价，开始设置订单价格");
                    order.setNxDoPrice(sellingPrice);
                    if (costPrice != null) {
                        order.setNxDoCostPrice(costPrice);
                    }
                    // 如果使用了大包装单价，设置打印规格为大包装单位
                    if (useCartonPrice && disGoodsEntity.getNxDgCartonUnit() != null 
                            && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()) {
                        order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
                        System.out.println("使用大包装单价，打印规格设置为: " + disGoodsEntity.getNxDgCartonUnit());
                    }
                    System.out.println("订单销售价已设置为: " + order.getNxDoPrice());
                    System.out.println("订单成本价已设置为: " + order.getNxDoCostPrice());
                } else {

                    System.out.println("⚠️ 销售价为null，不设置价格");
                }
            }

            System.out.println("rodstntn" + order.getNxDoStandard() + "disganttt" + disGoodsEntity.getNxDgGoodsStandardname());
            // 检查订单规格是否等于商品标准规格，或者订单规格是否匹配大包装单位（智能匹配：件=箱）
            boolean isStandardMatch = order.getNxDoStandard() != null 
                    && order.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname());
            boolean isCartonMatch = disGoodsEntity.getNxDgCartonUnit() != null 
                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                    && order.getNxDoStandard() != null
                    && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim());
            
            if (isStandardMatch || isCartonMatch) {
                System.out.println("订单规格匹配: isStandardMatch=" + isStandardMatch + ", isCartonMatch=" + isCartonMatch);
                order.setNxDoWeight(order.getNxDoQuantity());

                // 检查价格是否已设置
                if (order.getNxDoPrice() != null && !order.getNxDoPrice().trim().isEmpty()) {

                    BigDecimal doQuantity = new BigDecimal(order.getNxDoQuantity());
                    BigDecimal willPrice = new BigDecimal(order.getNxDoPrice());

                    // 判断是否使用外包装单价计算（支持智能匹配：件=箱）
                    boolean useCartonPriceForCalc = false;
                    Integer itemsPerCartonForCalc = null;
                    if (disGoodsEntity.getNxDgCartonUnit() != null 
                            && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                            && order.getNxDoStandard() != null
                            && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim())
                            && disGoodsEntity.getNxDgItemsPerCarton() != null 
                            && disGoodsEntity.getNxDgItemsPerCarton() > 0) {
                        useCartonPriceForCalc = true;
                        itemsPerCartonForCalc = disGoodsEntity.getNxDgItemsPerCarton();
                        System.out.println("订单规格匹配外包装单位（智能匹配），计算时将数量转换为箱数: " + doQuantity + "个 ÷ " + itemsPerCartonForCalc + " = " + doQuantity.divide(new BigDecimal(itemsPerCartonForCalc), 4, BigDecimal.ROUND_HALF_UP) + "箱");
                    }

                    BigDecimal subtotal;
                    BigDecimal costSubtotal = null;
                    if (useCartonPriceForCalc && itemsPerCartonForCalc != null) {
                        // 使用外包装单价：需要将数量转换为箱数
                        BigDecimal cartonCount = doQuantity.divide(new BigDecimal(itemsPerCartonForCalc), 4, BigDecimal.ROUND_HALF_UP);
                        subtotal = cartonCount.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        System.out.println("使用外包装单价计算: " + cartonCount + "箱 × " + willPrice + "元/箱 = " + subtotal + "元");

                    if (order.getNxDoCostPrice() != null && !order.getNxDoCostPrice().trim().isEmpty()) {
                        BigDecimal cosPrice = new BigDecimal(order.getNxDoCostPrice());
                            costSubtotal = cartonCount.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                            System.out.println("使用外包装成本价计算: " + cartonCount + "箱 × " + cosPrice + "元/箱 = " + costSubtotal + "元");
                        }
                    } else {
                        // 使用最小单位单价：直接相乘
                        subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        System.out.println("使用最小单位单价计算: " + doQuantity + " × " + willPrice + " = " + subtotal);
                        
                        if (order.getNxDoCostPrice() != null && !order.getNxDoCostPrice().trim().isEmpty()) {
                            BigDecimal cosPrice = new BigDecimal(order.getNxDoCostPrice());
                            costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        }
                    }
                    
                    order.setNxDoSubtotal(subtotal.toString());

                    if (costSubtotal != null) {
                        BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal scaleB = BigDecimal.ZERO;
                        if (subtotal.compareTo(BigDecimal.ZERO) != 0) {
                            scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                        }
                        System.out.println("成本小计: " + costSubtotal);
                        System.out.println("销售小计: " + subtotal);
                        System.out.println("利润: " + profit);
                        System.out.println("利润率: " + scaleB + "%");
                        order.setNxDoCostSubtotal(costSubtotal.toString());
                        order.setNxDoProfitSubtotal(profit.toString());
                        order.setNxDoProfitScale(scaleB.toString());
                    }
                    System.out.println("✅ 订单金额计算完成");
                }
            }
        }
    }

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

    /**
     * 将 NxDepartmentOrdersEntity 转换为 PasteSearchGoodsResponseDTO
     * 只包含前端需要的字段，减少数据传输量
     * 
     * @param order 订单实体
     * @param originalOrderJson 原始订单的JSON字符串（保留参数以保持方法签名兼容性）
     * @param originalGoodsName 原始商品名称（客户录入的原始内容，不可修改）
     * @return DTO对象
     */
    private PasteSearchGoodsResponseDTO convertToResponseDTO(NxDepartmentOrdersEntity order, String originalOrderJson, String originalGoodsName) {
        PasteSearchGoodsResponseDTO dto = new PasteSearchGoodsResponseDTO();
        
        dto.setNxDepartmentOrdersId(order.getNxDepartmentOrdersId());
        dto.setNxDoGoodsName(order.getNxDoGoodsName());
        // 设置原始商品名称（与商品名称相同）
        dto.setNxDoGoodsNameOriginal(order.getNxDoGoodsName());
        dto.setNxDoQuantity(order.getNxDoQuantity());
        // 规格默认值为"斤"
        dto.setNxDoStandard(order.getNxDoStandard() != null && !order.getNxDoStandard().trim().isEmpty() 
                ? order.getNxDoStandard() : "斤");
        dto.setNxDoRemark(order.getNxDoRemark());
        // 是否有备注：remark不为null且trim后不为空
        dto.setNxDoAddRemark(order.getNxDoRemark() != null && !order.getNxDoRemark().trim().isEmpty());
        dto.setNxDoStatus(order.getNxDoStatus());
        dto.setNxDoDepartmentId(order.getNxDoDepartmentId());
        dto.setNxDoDepartmentFatherId(order.getNxDoDepartmentFatherId());
        dto.setNxDoDisGoodsId(order.getNxDoDisGoodsId());
        // 默认值：0
        dto.setNxDoStandardWarn(0);
        dto.setGoodsNameWarn(0);
        dto.setNxDoDistributerId(order.getNxDoDistributerId());
        // 采购用户ID默认值为-1
        dto.setNxDoPurchaseUserId(order.getNxDoPurchaseUserId() != null ? order.getNxDoPurchaseUserId() : -1);
        dto.setNxDoOrderUserId(order.getNxDoOrderUserId());
        // 是否代理默认值为-1
        dto.setNxDoIsAgent(order.getNxDoIsAgent() != null ? order.getNxDoIsAgent() : -1);
        // 今日订单序号
        dto.setNxDoTodayOrder(order.getNxDoTodayOrder());
        
        // 处理候选商品列表
        // 分销商商品候选列表（优先显示）
        if (order.getNxDistributerGoodsEntityList() != null && !order.getNxDistributerGoodsEntityList().isEmpty()) {
            logger.info("[convertToResponseDTO] 转换分销商商品候选列表，数量: {}", order.getNxDistributerGoodsEntityList().size());
            List<DistributerGoodsCandidateDTO> distributerGoodsList = new ArrayList<>();
            for (NxDistributerGoodsEntity goods : order.getNxDistributerGoodsEntityList()) {
                DistributerGoodsCandidateDTO candidateDTO = new DistributerGoodsCandidateDTO();
                candidateDTO.setNxDistributerGoodsId(goods.getNxDistributerGoodsId());
                candidateDTO.setNxDgGoodsName(goods.getNxDgGoodsName());
                candidateDTO.setNxDgGoodsStandardname(goods.getNxDgGoodsStandardname());
                candidateDTO.setNxDgGoodsStandardWeight(goods.getNxDgGoodsStandardWeight());
                candidateDTO.setNxDgCartonUnit(goods.getNxDgCartonUnit());
                candidateDTO.setNxDgGoodsBrand(goods.getNxDgGoodsBrand());
                candidateDTO.setNxDgGoodsFile(goods.getNxDgGoodsFile());
                candidateDTO.setNxDgNxGoodsId(goods.getNxDgNxGoodsId());
                distributerGoodsList.add(candidateDTO);
            }
            dto.setNxDistributerGoodsEntityList(distributerGoodsList);
            logger.info("[convertToResponseDTO] 分销商商品候选列表转换完成，返回数量: {}", distributerGoodsList.size());
            // 验证DTO中是否成功设置了候选列表
            if (dto.getNxDistributerGoodsEntityList() != null) {
                logger.info("[convertToResponseDTO] DTO中分销商商品候选列表已设置，数量: {}", dto.getNxDistributerGoodsEntityList().size());
            } else {
                logger.error("[convertToResponseDTO] DTO中分销商商品候选列表为null！");
            }
        }
        
        // 系统商品候选列表（即使有分销商商品候选列表，也要显示系统商品，但已去除重复的）
        if (order.getNxGoodsEntities() != null && !order.getNxGoodsEntities().isEmpty()) {
            logger.info("[convertToResponseDTO] 转换系统商品候选列表，数量: {}", order.getNxGoodsEntities().size());
            List<NxGoodsCandidateDTO> nxGoodsList = new ArrayList<>();
            for (NxGoodsEntity goods : order.getNxGoodsEntities()) {
                NxGoodsCandidateDTO candidateDTO = new NxGoodsCandidateDTO();
                candidateDTO.setNxGoodsId(goods.getNxGoodsId());
                candidateDTO.setNxGoodsName(goods.getNxGoodsName());
                candidateDTO.setNxGoodsStandardname(goods.getNxGoodsStandardname());
                candidateDTO.setNxGoodsStandardWeight(goods.getNxGoodsStandardWeight());
                candidateDTO.setNxGoodsCartonUnit(goods.getNxGoodsCartonUnit());
                candidateDTO.setNxGoodsBrand(goods.getNxGoodsBrand());
                candidateDTO.setNxGoodsFile(goods.getNxGoodsFile());
                candidateDTO.setNxGoodsFatherId(goods.getNxGoodsFatherId());
                nxGoodsList.add(candidateDTO);
            }
            dto.setNxGoodsEntities(nxGoodsList);
            logger.info("[convertToResponseDTO] 系统商品候选列表转换完成，返回数量: {}", nxGoodsList.size());
        }
        
        return dto;
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
        //todo
        order.setNxDoPurchaseGoodsId(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoArriveWhatDay(getWeek(0));
        order.setNxDoCostPriceLevel("1");

        // 使用完善的价格处理逻辑（平台型/非平台型分支、货架库存查询、规格匹配等）
        processOrderPrice(order, disGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("depId", order.getNxDoDepartmentId());
        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
        map.put("standard", order.getNxDoStandard());
        System.out.println("depmapapmmdmmdd" + map);
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if (departmentDisGoodsEntity != null) {
            System.out.println("prinstnan" + departmentDisGoodsEntity.getNxDdgOrderStandard());
            // 判断是否使用了大包装单价，如果是则设置打印规格为大包装单位（支持智能匹配：件=箱）
            boolean isUsingCartonPrice = disGoodsEntity.getNxDgCartonUnit() != null
                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                    && order.getNxDoStandard() != null
                    && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim());

            if (isUsingCartonPrice) {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
                System.out.println("使用大包装单价，打印规格设置为: " + disGoodsEntity.getNxDgCartonUnit());
            } else if (order.getNxDoCostPriceLevel() == null || order.getNxDoCostPriceLevel().equals("1")) {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
            } else {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
            }

            order.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
            //如果有重量和单价，则计算 subtotal
            if(order.getNxDoWeight() != null && !order.getNxDoWeight().trim().isEmpty()
                    && order.getNxDoPrice() != null && !order.getNxDoPrice().trim().isEmpty()){
                try {
                    BigDecimal weight = new BigDecimal(order.getNxDoWeight());
                    BigDecimal price = new BigDecimal(order.getNxDoPrice());
                    BigDecimal subtotal = weight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                    order.setNxDoSubtotal(subtotal.toString());
                } catch (NumberFormatException e) {
                    // 如果转换失败，不设置subtotal
                    logger.warn("[saveOneOrder] 计算subtotal失败，重量或单价格式错误: weight={}, price={}, error={}",
                            order.getNxDoWeight(), order.getNxDoPrice(), e.getMessage());
                }
            }
            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
        }else{
            map.put("standard", null);
            System.out.println("depmapapapappapa" + map);
            NxDepartmentDisGoodsEntity departmentDisGoodsEntityO = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
            if (departmentDisGoodsEntityO != null) {
                order.setNxDoDepDisGoodsId(departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
            }
        }
//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", order.getNxDoDepartmentId());
//        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
//        map.put("standard", order.getNxDoStandard());
//        System.out.println("depmapapmmdmmdd" + map);
//        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
//        if (departmentDisGoodsEntity != null) {
//            System.out.println("prinstnan" + departmentDisGoodsEntity.getNxDdgOrderStandard());
//            order.setNxDoPrintStandard(departmentDisGoodsEntity.getNxDdgOrderStandard());
//            order.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
//            BigDecimal costPrice = new BigDecimal(0);
//            if (order.getNxDoCostPriceLevel().equals("1")) {
//                costPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceOne());
//                order.setNxDoCostPrice(costPrice.toString());
//                order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceOneUpdate());
//                if (order.getNxDoStandard().equals(departmentDisGoodsEntity.getNxDdgOrderStandard())) {
//                    BigDecimal price = new BigDecimal(departmentDisGoodsEntity.getNxDdgOrderPrice());
//                    BigDecimal subtotal = price.multiply(new BigDecimal(order.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    BigDecimal costDecimal = costPrice.multiply(new BigDecimal(order.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    BigDecimal profit = subtotal.subtract(costDecimal).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
//
//                    order.setNxDoSubtotal(subtotal.toString());
//                    order.setNxDoCostSubtotal(costDecimal.toString());
//                    order.setNxDoProfitSubtotal(profit.toString());
//                    order.setNxDoProfitScale(scaleB.toString());
//                }
//            } else {
//                BigDecimal price = new BigDecimal(departmentDisGoodsEntity.getNxDdgOrderPrice());
//                costPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceTwo());
//                BigDecimal costDecimal = costPrice.multiply(new BigDecimal(order.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                BigDecimal subtotal = price.multiply(new BigDecimal(order.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                BigDecimal profit = subtotal.subtract(costDecimal).setScale(1, BigDecimal.ROUND_HALF_UP);
//                BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
//
//                order.setNxDoCostPrice(costPrice.toString());
//                order.setNxDoSubtotal(subtotal.toString());
//                order.setNxDoCostSubtotal(costDecimal.toString());
//                order.setNxDoProfitSubtotal(profit.toString());
//                order.setNxDoProfitScale(scaleB.toString());
//                order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceTwoUpdate());
//            }
//
//            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
//
//        }

        nxDepartmentOrdersService.update(order);

        //auto
        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
            savePurGoodsAuto(order);
        }
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDistributerGoodsEntity(distributerGoodsEntity);
        
        // 确保返回的订单状态正确
        logger.info("[saveOneOrder] 返回订单: 商品名称={}, 订单ID={}, 状态={}, 分销商商品ID={}", 
                order.getNxDoGoodsName(), order.getNxDepartmentOrdersId(), 
                order.getNxDoStatus(), order.getNxDoDisGoodsId());
        
        // 找到商品后，设置为0（已保存），但如果订单状态是 -2（待修正），则保持 -2
        if (order.getNxDoStatus() == null || (order.getNxDoStatus() != 0 && order.getNxDoStatus() != -2)) {
            order.setNxDoStatus(0);
        }
        
        return order;
    }


    private NxDepartmentOrdersEntity choiceGoodsOrder(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
        logger.info("choiceGoodsOrderNw" , order.getNxDoGoodsName());
        logger.info("choiceGoodsOrderNw" , order.getNxDoStandard());
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

        Integer nxDoDisGoodsId = order.getNxDoDisGoodsId();

        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);

        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = updateOneOrderForChoice(order, nxDistributerGoodsEntity);

        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
        return order;
    }
    // not good
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


    // not good
    @RequestMapping(value = "/pickerGetToStockGoodsWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R pickerGetToStockGoodsWithDepIds(String nxDepIds, String gbDepIds, Integer nxDisId) {
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


    //not good
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

    // not good
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
    public R stokerHaveNotOutCataGoods(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        if(supplierId != null && supplierId != -1){
            map.put("nxSupplierId", supplierId);
        }
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
    public R stockerGetHaveOutCataGoods(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer supplierId, Integer batchSupplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("dayuPurStatus", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        if(supplierId != null && supplierId != -1){
            map.put("nxSupplierId", supplierId);
        }


        System.out.println("getHaveOutCataGoodsstockerGetHaveOutCataGoods" + map);
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

    /**
     * 获取货架列表（仅基本信息，不包含商品详情）
     * @param nxDepIds 部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param gbDepIds GB部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param nxDisId 分销商ID
     * @return 包含货架列表、部门列表、统计数据的Map
     */
    @RequestMapping(value = "/stockerGetShelfListWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetShelfListWithDepIds(String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("status", 3);
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

        // 获取货架列表和统计数据
        Map<String, Object> result = nxDepartmentOrdersService.queryShelfListWithDepIds(params);

        return R.ok().put("data", result);
    }

    /**
     * 获取指定货架的商品详情（包含订单信息）
     * @param shelfId 货架ID
     * @param nxDepIds 部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param gbDepIds GB部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param nxDisId 分销商ID
     * @return 完整的货架对象（包含商品列表和订单）
     */
    @RequestMapping(value = "/stockerGetShelfGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetShelfGoodsDetail(Integer shelfId, String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("status", 3);
        params.put("purStatus", 4);
        params.put("shelfId", shelfId);
        
        // 处理部门ID
        List<String> idsGb = parseDepIds(gbDepIds);
        List<String> idsNx = parseDepIds(nxDepIds);

        if (!idsGb.isEmpty()) {
            params.put("gbDepIds", idsGb);
        }
        if (!idsNx.isEmpty()) {
            params.put("nxDepIds", idsNx);
        }

        // 使用超简化版查询，字段扁平化，大幅减少数据传输量
        ShelfDetailSimpleDTO shelf = nxDepartmentOrdersService.queryShelfGoodsDetailUltraSimple(params);

        // 打印日志，检查 nxDepartmentAttrName 字段
        if (shelf != null && shelf.getGoodsList() != null) {
            logger.info("========= stockerGetShelfGoodsDetail 返回数据检查 =========");
            logger.info("货架ID: {}, 货架名称: {}", shelf.getNxDistributerGoodsShelfId(), shelf.getNxDistributerGoodsShelfName());
            logger.info("商品数量: {}", shelf.getGoodsList().size());
            
            int orderCount = 0;
            int hasAttrNameCount = 0;
            int nullAttrNameCount = 0;
            
            for (ShelfGoodsShelfGoodsSimpleDTO shelfGoods : shelf.getGoodsList()) {
                if (shelfGoods.getGoods() != null && shelfGoods.getGoods().getOrders() != null) {
                    for (ShelfOrderSimpleDTO order : shelfGoods.getGoods().getOrders()) {
                        orderCount++;
                        if (order.getNxDepartmentAttrName() != null && !order.getNxDepartmentAttrName().isEmpty()) {
                            hasAttrNameCount++;
                            logger.info("订单ID: {}, nxDepartmentAttrName: {}, depName: {}", 
                                    order.getNxDepartmentOrdersId(), 
                                    order.getNxDepartmentAttrName(), 
                                    order.getDepName());
                        } else {
                            nullAttrNameCount++;
                            logger.warn("订单ID: {}, nxDepartmentAttrName 为 NULL 或空, depName: {}", 
                                    order.getNxDepartmentOrdersId(), 
                                    order.getDepName());
                        }
                    }
                }
            }
            
            logger.info("总订单数: {}, 有 nxDepartmentAttrName 的订单数: {}, nxDepartmentAttrName 为 NULL 的订单数: {}", 
                    orderCount, hasAttrNameCount, nullAttrNameCount);
            logger.info("========= stockerGetShelfGoodsDetail 数据检查完成 =========");
        } else {
            logger.warn("stockerGetShelfGoodsDetail 返回的 shelf 为 NULL 或 goodsList 为 NULL");
        }

        return R.ok().put("data", shelf);
    }

    /**
     * 获取统计数据（不包含货架和商品数据）
     * @param nxDepIds 部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param gbDepIds GB部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param nxDisId 分销商ID
     * @return 包含部门列表和统计数据的Map
     */
    @RequestMapping(value = "/stockerGetShelfStatistics", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetShelfStatistics(String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("status", 3);
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

        // 获取统计数据
        Map<String, Object> result = nxDepartmentOrdersService.queryShelfStatistics(params);

        return R.ok().put("data", result);
    }

    /**
     * 获取类别列表（曾祖父级别，仅基本信息，不包含商品详情）
     * @param nxDepIds 部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param gbDepIds GB部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param nxDisId 分销商ID
     * @return 包含类别列表、部门列表和统计数据的Map
     */
    @RequestMapping(value = "/stockerGetCategoryListWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetCategoryListWithDepIds(String nxDepIds, String gbDepIds, Integer nxDisId, Integer supplierId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("status", 3);
        params.put("purStatus", 4);
        if(supplierId != null && supplierId != -1){
            params.put("nxSupplierId", supplierId);
        }
        
        // 处理部门ID
        List<String> idsGb = parseDepIds(gbDepIds);
        List<String> idsNx = parseDepIds(nxDepIds);

        if (!idsGb.isEmpty()) {
            params.put("gbDepIds", idsGb);
        }
        if (!idsNx.isEmpty()) {
            params.put("nxDepIds", idsNx);
        }

        // 获取类别列表和统计数据
        Map<String, Object> result = nxDepartmentOrdersService.queryCategoryListWithDepIds(params);

        return R.ok().put("data", result);
    }

    /**
     * 获取指定类别的商品详情（包含订单信息）
     * @param categoryId 类别ID（Integer类型，必填）- 曾祖父级别ID
     * @param nxDepIds 部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param gbDepIds GB部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param nxDisId 分销商ID
     * @return 完整的类别对象（包含商品列表和订单）
     */
    @RequestMapping(value = "/stockerGetCategoryGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetCategoryGoodsDetail(Integer categoryId, String nxDepIds, String gbDepIds, Integer nxDisId, Integer supplierId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("status", 3);
        params.put("purStatus", 4);
        params.put("categoryId", categoryId);
        if(supplierId != null && supplierId != -1){
            params.put("nxSupplierId", supplierId);
        }
        // 处理部门ID
        List<String> idsGb = parseDepIds(gbDepIds);
        List<String> idsNx = parseDepIds(nxDepIds);

        if (!idsGb.isEmpty()) {
            params.put("gbDepIds", idsGb);
        }
        if (!idsNx.isEmpty()) {
            params.put("nxDepIds", idsNx);
        }

        // 使用超简化版查询，字段扁平化，大幅减少数据传输量
        CategoryDetailSimpleDTO category = nxDepartmentOrdersService.queryCategoryGoodsDetailUltraSimple(params);

        return R.ok().put("data", category);
    }

    /**
     * 获取统计数据（不包含类别和商品数据）
     * @param nxDepIds 部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param gbDepIds GB部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param nxDisId 分销商ID
     * @return 包含部门列表和统计数据的Map
     */
    @RequestMapping(value = "/stockerGetCategoryStatistics", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetCategoryStatistics(String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("status", 3);
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

        // 获取统计数据
        Map<String, Object> result = nxDepartmentOrdersService.queryCategoryStatistics(params);

        return R.ok().put("data", result);
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
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapDep);

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

        // 过滤掉 null 元素
        if (departmentEntities != null) {
            departmentEntities = departmentEntities.stream()
                    .filter(dept -> dept != null && dept.getNxDepartmentId() != null)
                    .collect(Collectors.toList());
        }
        if (gbDepartmentEntityList != null) {
            gbDepartmentEntityList = gbDepartmentEntityList.stream()
                    .filter(dept -> dept != null && dept.getGbDepartmentId() != null)
                    .collect(Collectors.toList());
        }

        // 2. 批量获取统计数据
        if (departmentEntities != null && !departmentEntities.isEmpty()) {
            List<Integer> depIds = departmentEntities.stream()
                    .map(NxDepartmentEntity::getNxDepartmentId)
                    .collect(Collectors.toList());

            // 一次性查询所有部门的统计数据
            Map<Integer, Map<String, Integer>> depStats = nxDepartmentOrdersService.batchQueryDepStats(depIds, null);

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
        if (gbDepartmentEntityList != null && !gbDepartmentEntityList.isEmpty()) {
            List<Integer> gbDepIds = gbDepartmentEntityList.stream()
                    .map(GbDepartmentEntity::getGbDepartmentId)
                    .collect(Collectors.toList());

            System.out.println("depididiisisississigbDepIds" + gbDepIds);

            Map<Integer, Map<String, Integer>> gbDepStats = nxDepartmentOrdersService.batchQueryGbDepStats(gbDepIds, null);

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

    @RequestMapping(value = "/supplierGetWaitStockGoodsDeps", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGetWaitStockGoodsDeps(Integer disId, Integer supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purStatus", 4);
        map.put("nxSupplierId", supplierId);
        System.out.println("mapappapasuppleerr" + map);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        // 1. 获取部门列表
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartmentSimple(map);
        List<GbDepartmentEntity> gbDepartmentEntityList = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        // 过滤掉 null 元素
        if (departmentEntities != null) {
            departmentEntities = departmentEntities.stream()
                    .filter(dept -> dept != null && dept.getNxDepartmentId() != null)
                    .collect(Collectors.toList());
        }
        if (gbDepartmentEntityList != null) {
            gbDepartmentEntityList = gbDepartmentEntityList.stream()
                    .filter(dept -> dept != null && dept.getGbDepartmentId() != null)
                    .collect(Collectors.toList());
        }

        // 2. 批量获取统计数据
        if (departmentEntities != null && !departmentEntities.isEmpty()) {
            List<Integer> depIds = departmentEntities.stream()
                    .map(NxDepartmentEntity::getNxDepartmentId)
                    .collect(Collectors.toList());

            // 一次性查询所有部门的统计数据
            Map<Integer, Map<String, Integer>> depStats = nxDepartmentOrdersService.batchQueryDepStats(depIds, map);

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
        if (gbDepartmentEntityList != null && !gbDepartmentEntityList.isEmpty()) {
            List<Integer> gbDepIds = gbDepartmentEntityList.stream()
                    .map(GbDepartmentEntity::getGbDepartmentId)
                    .collect(Collectors.toList());

            System.out.println("depididiisisississigbDepIds" + gbDepIds);

            Map<Integer, Map<String, Integer>> gbDepStats = nxDepartmentOrdersService.batchQueryGbDepStats(gbDepIds, map);

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
    public R stockerGetFinishStockGoodsDeps(Integer disId, Integer supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        if(supplierId != null && supplierId != -1){
            map.put("nxSupplierId", supplierId);
        }

//        map.put("euqalPurStatus", 4);
        List<NxDepartmentEntity> resultNx = new ArrayList<>();

        // 1. 获取部门列表
        System.out.println("dkfkjakfdas;fkasd;stockerGetFinishStockGoodsDepsstockerGetFinishStockGoodsDepsfas" + map);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartmentSimple(map);
        List<GbDepartmentEntity> gbDepartmentEntityList = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        // 2. 批量获取统计数据
        if (!departmentEntities.isEmpty()) {
            List<Integer> depIds = departmentEntities.stream()
                    .filter(Objects::nonNull)  // 过滤掉 null 元素
                    .map(NxDepartmentEntity::getNxDepartmentId)
                    .filter(Objects::nonNull)  // 过滤掉 null ID
                    .collect(Collectors.toList());

            // 一次性查询所有部门的统计数据
            Map<Integer, Map<String, Integer>> depStats = nxDepartmentOrdersService.batchQueryDepStats(depIds, null);

            // 设置统计数据
            for (NxDepartmentEntity dept : departmentEntities) {
                if (dept == null || dept.getNxDepartmentId() == null) {
                    continue;  // 跳过 null 元素或 null ID
                }
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
                    .filter(Objects::nonNull)  // 过滤掉 null 元素
                    .map(GbDepartmentEntity::getGbDepartmentId)
                    .filter(Objects::nonNull)  // 过滤掉 null ID
                    .collect(Collectors.toList());

            System.out.println("depididiisisississigbDepIds" + gbDepIds);

            Map<Integer, Map<String, Integer>> gbDepStats = nxDepartmentOrdersService.batchQueryGbDepStats(gbDepIds, null);

            for (GbDepartmentEntity dept : gbDepartmentEntityList) {
                if (dept == null || dept.getGbDepartmentId() == null) {
                    continue;  // 跳过 null 元素或 null ID
                }
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

        // 过滤掉 null 元素，确保返回的列表不包含 null
        List<NxDepartmentEntity> filteredNxDep = departmentEntities.stream()
                .filter(Objects::nonNull)
                .filter(dept -> dept.getNxDepartmentId() != null)
                .collect(Collectors.toList());
        
        List<GbDepartmentEntity> filteredGbDep = gbDepartmentEntityList.stream()
                .filter(Objects::nonNull)
                .filter(dept -> dept.getGbDepartmentId() != null)
                .collect(Collectors.toList());
        
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDep", filteredNxDep);
        mapR.put("gbDep", filteredGbDep);
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
     * 根据tradeNo查询账单信息和订单列表（包含溯源报告信息）
     * 如果是货架商品，从库存批次关联溯源报告；如果不是货架商品，从采购商品关联溯源报告
     * 
     * @param tradeNo 账单交易号（nx_DB_trade_no）
     * @return 账单信息和订单列表，每个订单包含下单用户、下单时间、溯源报告等信息
     */
    @RequestMapping(value = "/getOrdersByTradeNoWithTraceReport/{tradeNo}")
    @ResponseBody
    public R getOrdersByTradeNoWithTraceReport(@PathVariable String tradeNo, HttpServletRequest request) {
        System.out.println("[getOrdersByTradeNoWithTraceReport] 开始查询账单和订单溯源报告，tradeNo: " + tradeNo);
        
        if (tradeNo == null || tradeNo.trim().isEmpty()) {
            return R.error("tradeNo 不能为空");
        }
        
        // 通过 tradeNo 查询账单信息
        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryDepartBillByJustTradeNo(tradeNo);
        if (billEntity == null) {
            return R.error("账单不存在，tradeNo: " + tradeNo);
        }
        
        Integer departmentBillId = billEntity.getNxDepartmentBillId();
        if (departmentBillId == null) {
            return R.error("账单ID为空，tradeNo: " + tradeNo);
        }
        
        // 查询历史订单列表（包含溯源报告信息）
        Map<String, Object> map = new HashMap<>();
        map.put("billId", departmentBillId);
        
        List<NxDepartmentOrderHistoryEntity> ordersEntities = historyService.queryOrdersByBillIdWithTraceReport(map);
        
        System.out.println("[getOrdersByTradeNoWithTraceReport] 查询完成，tradeNo: " + tradeNo + 
            ", 账单ID: " + departmentBillId + ", 订单数量: " + ordersEntities.size());
        
        // 构建基础URL（用于文件下载）
        String baseUrl = buildBaseUrl(request);
        
        // 处理溯源报告的下载地址
        for (NxDepartmentOrderHistoryEntity order : ordersEntities) {
            NxTraceReportEntity traceReport = order.getNxTraceReportEntity();
            if (traceReport != null) {
                // 处理单个文件路径
                String filePath = traceReport.getNxTrFilePath();
                if (filePath != null && !filePath.trim().isEmpty()) {
                    String downloadUrl = buildDownloadUrl(baseUrl, filePath);
                    traceReport.setNxTrFilePath(downloadUrl); // 将相对路径替换为完整URL
                }
                
                // 处理多个文件路径（JSON格式）
                String filePaths = traceReport.getNxTrFilePaths();
                if (filePaths != null && !filePaths.trim().isEmpty()) {
                    try {
                        // 解析JSON数组（fastjson 1.2.x 版本使用 parseArray）
                        JSONArray filePathsArray = JSONArray.parseArray(filePaths);
                        JSONArray downloadUrlsArray = new JSONArray();
                        for (int i = 0; i < filePathsArray.size(); i++) {
                            String path = filePathsArray.getString(i);
                            if (path != null && !path.trim().isEmpty()) {
                                String downloadUrl = buildDownloadUrl(baseUrl, path);
                                downloadUrlsArray.add(downloadUrl);
                            }
                        }
                        traceReport.setNxTrFilePaths(downloadUrlsArray.toJSONString());
                    } catch (Exception e) {
                        logger.warn("[getOrdersByTradeNoWithTraceReport] 解析文件路径JSON失败: {}, 错误: {}", filePaths, e.getMessage());
                        // 如果解析失败，保持原值
                    }
                }
            }
            
            System.out.println("[getOrdersByTradeNoWithTraceReport] 订单ID: " + order.getNxDepartmentOrdersId() + 
                ", 商品名称: " + order.getNxDoGoodsName() +
                ", 下单时间: " + order.getNxDoTodayOrder() +
                ", 下单用户: " + (order.getNxDepartmentUserEntity() != null ?
                    order.getNxDepartmentUserEntity().getNxDuWxNickName() : "null") +
                ", 溯源报告: " + (traceReport != null ? "存在(ID:" + traceReport.getNxTraceReportId() + ")" : "null"));
            if (traceReport != null) {
                System.out.println("[getOrdersByTradeNoWithTraceReport]   溯源报告详情 - 供应商: " + traceReport.getNxTrSupplierName() + 
                    ", 采购日期: " + traceReport.getNxTrPurchaseDate() +
                    ", 有效期: " + traceReport.getNxTrValidStartDate() + " ~ " + traceReport.getNxTrValidEndDate() +
                    ", 文件路径: " + traceReport.getNxTrFilePath());
            }
        }
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("bill", billEntity);
        result.put("orders", ordersEntities);
        
        return R.ok().put("data", result);
    }

    /**
     * 构建基础URL（协议 + 域名 + 端口 + 上下文路径）
     */
    private String buildBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme(); // http 或 https
        String serverName = request.getServerName(); // 服务器名称或IP
        int serverPort = request.getServerPort(); // 端口号
        String contextPath = request.getContextPath(); // 上下文路径，如 /nongxinle_master_war_exploded
        
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);
        
        // 如果是标准端口（80或443），不需要添加端口号
        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }
        
        baseUrl.append(contextPath);
        
        return baseUrl.toString();
    }

    /**
     * 构建文件下载URL
     * @param baseUrl 基础URL（如 http://192.168.0.101:8080/nongxinle_master_war_exploded）
     * @param filePath 文件相对路径（如 traceReports/xxx.pdf）
     * @return 完整的下载URL
     */
    private String buildDownloadUrl(String baseUrl, String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }
        
        // 如果已经是完整URL，直接返回
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            return filePath;
        }
        
        // 确保路径以 / 开头
        String normalizedPath = filePath.startsWith("/") ? filePath : "/" + filePath;
        
        // 构建完整URL
        return baseUrl + normalizedPath;
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

        List<Integer> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryOnlyDepGoodsIds(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr", disGoodsEntities);
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

        List<Integer> disGoodsIds = nxDepartmentOrdersService.queryOnlyNxGoodsIds(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr", disGoodsEntities);
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


    @RequestMapping(value = "/disGetTypePrepareOutPage", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTypePrepareOutPage(Integer disId,
                                      Integer page,
                                      Integer limit) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("purType", 0);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        logger.info("[disGetTypePrepareOutPage] 查询条件 map: {}", map);
        logger.info("[disGetTypePrepareOutPage] 使用超简化版查询，只返回前端需要的字段");
        
        // 使用超简化版查询，只返回前端需要的字段，大幅减少数据传输量
        List<com.nongxinle.entity.OutGoodsSimpleDTO> outGoodsSimpleList = nxDepartmentOrdersService.queryOutGoodsWithOrdersUltraSimple(map);
        logger.info("[disGetTypePrepareOutPage] 查询结果数量: {}", outGoodsSimpleList != null ? outGoodsSimpleList.size() : 0);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("disId", disId);
        mapCount.put("status", 3);
        mapCount.put("purStatus", 4);
        mapCount.put("purType", 0);
        mapCount.put("type", -1);
        Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(mapCount);

        PageUtils pageUtil = new PageUtils(outGoodsSimpleList, integer, limit, page);

        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/disGetTypePrepareOutByDep", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTypePrepareOutByDep(Integer disId, Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depFatherId", depId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("purType", 0);
        logger.info("[disGetTypePrepareOutByDep] 查询参数: {}", map);
        logger.info("[disGetTypePrepareOutByDep] 使用超简化版查询，只返回前端需要的字段");
        
        // 使用超简化版查询，只返回前端需要的字段，大幅减少数据传输量
        List<com.nongxinle.entity.OutGoodsSimpleDTO> outGoodsSimpleList = nxDepartmentOrdersService.disGetNxGoodsApplyUltraSimple(map);
        logger.info("[disGetTypePrepareOutByDep] 查询结果数量: {}", outGoodsSimpleList != null ? outGoodsSimpleList.size() : 0);
        return R.ok().put("data", outGoodsSimpleList);
    }





    @RequestMapping(value = "/disGetTypePrepareOutCata", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTypePrepareOutCata(Integer disId, Integer purType) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
//        map.put("status", 3);
        map.put("purStatus", 2);
        map.put("orderGoodsType", purType);
        

        // 使用超简化版查询，只返回曾祖父对象基本信息，不包含嵌套对象，大幅减少数据传输量
        System.out.println("ormapapa" + map);
        List<com.nongxinle.entity.GreatGrandFatherGoodsSimpleDTO> greatGrandList = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoodsUltraSimple(map);
        logger.info("[disGetTypePrepareOutCata] 查询结果数量: {}", greatGrandList != null ? greatGrandList.size() : 0);

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("purStatus", 4);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        map111.put("batchId", 0);
        int unPurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("batchId", 1);
        int puringCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        mapR.put("stockCount", stockCount);
        mapR.put("unPurCount", unPurCount);
        mapR.put("puringCount", puringCount);
        mapR.put("arr", greatGrandList);
        System.out.println("grnalisiisis" + greatGrandList.size());
        logger.info("[disGetTypePrepareOutCata] 返回数据完成（使用简化DTO，数据传输量大幅减少）");
        return R.ok().put("data", mapR);

    }

    /**
     * 按部门查询准备出库/采购的部门列表
     * 只返回部门列表，不包含商品分类
     * 按部门订单数量排序
     * purType: 0-出库商品(purchaseAuto=-1), 1-采购商品(purchaseAuto=1)
     */
    @RequestMapping(value = "/disGetTypePrepareOutDepCata", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTypePrepareOutDepCata(Integer disId, Integer purType) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("orderGoodsType", purType);
        if (purType == 1) {
            map.put("batchId", 0);
        }
        
        // 1. 查询有订单的部门列表
        System.out.println("查询部门map" +  map);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartmentSimple(map);
        
        // 2. 统计每个部门的订单数量，并按订单数量排序
        List<Map<String, Object>> departmentList = new ArrayList<>();
        for (NxDepartmentEntity department : departmentEntities) {
            Map<String, Object> depMap = new HashMap<>();
            depMap.put("depId", department.getNxDepartmentId());
            depMap.put("depName", department.getNxDepartmentName());
            depMap.put("depFatherId", department.getNxDepartmentFatherId());
            depMap.put("depAttrName", department.getNxDepartmentAttrName());
            depMap.put("depOrderCode", department.getNxDepartmentOrderCode());
            depMap.put("depPinyin", department.getNxDepartmentPinyin());

            // 统计该部门的订单数量
            Map<String, Object> countMap = new HashMap<>();
            countMap.put("disId", disId);
            countMap.put("status", 3);
            countMap.put("purStatus", 4);
            countMap.put("depFatherId", department.getNxDepartmentId());
            countMap.put("orderGoodsType", purType);

            // 根据 purType 设置 batchId，与主查询保持一致
            if (purType == 1) {
                countMap.put("batchId", 0); // 采购商品：只统计未批次的订单
            }
            System.out.println("orercount" + countMap);
            Integer orderCount = nxDepartmentOrdersService.queryDepOrdersAcount(countMap);
            depMap.put("orderCount", orderCount != null ? orderCount : 0);
            
            departmentList.add(depMap);
        }
        
        // 3. 按部门名称拼音排序
        departmentList.sort((a, b) -> {
            String pinyinA = (String) a.get("depPinyin");
            String pinyinB = (String) b.get("depPinyin");
            if (pinyinA == null && pinyinB == null) {
                return 0;
            }
            if (pinyinA == null) {
                return 1; // null值排在后面
            }
            if (pinyinB == null) {
                return -1; // null值排在后面
            }
            return pinyinA.compareTo(pinyinB); // 升序排序
        });
        
        // 4. 全局统计
        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("purStatus", 4);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        map111.put("batchId", 0);
        int unPurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("batchId", 1);
        int puringCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("stockCount", stockCount);
        mapR.put("unPurCount", unPurCount);
        mapR.put("puringCount", puringCount);
        mapR.put("arr", departmentList);
        return R.ok().put("data", mapR);
    }

    /**
     * 根据部门ID查询该部门的商品分类、类别下的商品和订单
     * 返回商品分类列表，每个分类下包含商品和订单信息
     * purType: 0-出库商品(purchaseAuto=-1), 1-采购商品(purchaseAuto=1)
     */
    @RequestMapping(value = "/disGetTypePrepareOutDepGoodsPage", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTypePrepareOutDepGoodsPage(Integer disId, Integer depId, Integer purType) {
        logger.info("[disGetTypePrepareOutDepGoodsPage] 开始查询，参数: disId={}, depId={}, purType={}", disId, depId, purType);
        
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depFatherId", depId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("purType", purType);
        
        // 参考 disGetTypePreparePurGoodsPage 接口的逻辑
        // 不设置 purchaseAuto，查询所有有采购商品的订单（包括 purchaseAuto=-1 但被添加了采购商品的）
        // 只根据 purType 设置 batchId
        if (purType != null && purType == 1) {
            // 采购商品：查询未批次的采购商品
            map.put("batchId", 0);
        } else {
            // 出库商品：查询所有有采购商品的订单（包括已批次和未批次的）
            // 不设置 batchId，让SQL不进行过滤
        }
        
        logger.info("[disGetTypePrepareOutDepGoodsPage] 查询条件 map: {}", map);
        logger.info("[disGetTypePrepareOutDepGoodsPage] 注意：未设置 purchaseAuto，将查询所有有采购商品的订单");
        
        // 查询该部门下的商品分类（按大类sort排序），每个分类下包含商品和订单
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);
        logger.info("[disGetTypePrepareOutDepGoodsPage] 查询结果分类数量: {}", fatherGoodsEntities != null ? fatherGoodsEntities.size() : 0);
        
//        if (fatherGoodsEntities != null && !fatherGoodsEntities.isEmpty()) {
//            for (int i = 0; i < fatherGoodsEntities.size(); i++) {
//                NxDistributerFatherGoodsEntity fatherGoods = fatherGoodsEntities.get(i);
//                logger.info("[disGetTypePrepareOutDepGoodsPage] 分类[{}]: id={}, name={}, 采购商品数量={}",
//                    i,
//                    fatherGoods.getNxDistributerFatherGoodsId(),
//                    fatherGoods.getNxDfgFatherGoodsName(),
//                    fatherGoods.getNxDistributerPurchaseGoodsEntities() != null ? fatherGoods.getNxDistributerPurchaseGoodsEntities().size() : 0);
//
//                if (fatherGoods.getNxDistributerPurchaseGoodsEntities() != null && !fatherGoods.getNxDistributerPurchaseGoodsEntities().isEmpty()) {
//                    for (int j = 0; j < fatherGoods.getNxDistributerPurchaseGoodsEntities().size(); j++) {
//                        NxDistributerPurchaseGoodsEntity purGoods = fatherGoods.getNxDistributerPurchaseGoodsEntities().get(j);
//                        if (purGoods.getNxDistributerGoodsEntity() != null) {
//                            NxDistributerGoodsEntity goods = purGoods.getNxDistributerGoodsEntity();
//                            logger.info("[disGetTypePrepareOutDepGoodsPage]   采购商品[{}]: id={}, name={}, purchaseAuto={}, 订单数量={}",
//                                j,
//                                goods.getNxDistributerGoodsId(),
//                                goods.getNxDgGoodsName(),
//                                goods.getNxDgPurchaseAuto(),
//                                purGoods.getNxDepartmentOrdersEntities() != null ? purGoods.getNxDepartmentOrdersEntities().size() : 0);
//                        }
//                    }
//                }
//            }
//        } else {
//            logger.warn("[disGetTypePrepareOutDepGoodsPage] 查询结果为空！");
//        }
        
        // 统计订单状态
        fatherGoodsEntities = everyStockFatherGoodsOrderStatus(fatherGoodsEntities, disId, map);
        logger.info("[disGetTypePrepareOutDepGoodsPage] 统计订单状态后分类数量: {}", fatherGoodsEntities != null ? fatherGoodsEntities.size() : 0);
        
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        logger.info("[disGetTypePrepareOutDepGoodsPage] 返回数据完成");
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
        if (fatherGoodsList == null || fatherGoodsList.isEmpty()) {
            return fatherGoodsList;
        }

        // 提取所有父级商品ID
        List<Integer> grandIds = fatherGoodsList.stream()
                .map(NxDistributerFatherGoodsEntity::getNxDistributerFatherGoodsId)
                .collect(Collectors.toList());

        // 构建查询参数
        Map<String, Object> queryParams = new HashMap<>(map);
        queryParams.put("disId", disId);
        queryParams.put("purStatus", 4);
        System.out.println("ididisisis" + grandIds);

        // 批量查询订单数量
        Map<Integer, Integer> orderCountMap = nxDepartmentOrdersService.batchQueryFatherGoodsOrderCount(grandIds, queryParams);

        // 设置订单数量
        for (NxDistributerFatherGoodsEntity fatherGoods : fatherGoodsList) {
            Integer orderCount = orderCountMap.get(fatherGoods.getNxDistributerFatherGoodsId());
            fatherGoods.setNewOrderCount(orderCount != null ? orderCount : 0);
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
            BigDecimal subtotalB = weightB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
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
            System.out.println("cosrprice" + ordersEntity.getNxDoCostPrice());
            if (ordersEntity.getNxDoCostPrice() != null && !ordersEntity.getNxDoCostPrice().isEmpty()) {
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
        List<Map<String, Object>> shortages = new ArrayList<>();
        for (NxDepartmentOrdersEntity ordersEntityOld : ordersEntityList) {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObjectNew(ordersEntityOld.getNxDepartmentOrdersId());
            ordersEntity.setNxDoPickUserId(ordersEntityOld.getNxDoPickUserId());
            System.out.println("oslsllslsls" + ordersEntityOld.getNxDoWeight());
            if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                BigDecimal subtotal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(ordersEntityOld.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoSubtotal(subtotal.toString());
                ordersEntity.setNxDoWeight(ordersEntityOld.getNxDoWeight());
                ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
                //查询是否有库存（按日期排序，实现FIFO先进先出）
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsId", ordersEntity.getNxDoDisGoodsId());
                map.put("restWeight", 0);
                List<NxDistributerGoodsShelfStockEntity> stockEntities = nxDisGoodsShelfStockService.queryShelfStockListByParams(map);

                if (stockEntities.size() > 0) {
                    // 需要出货的重量
                    BigDecimal needWeight = new BigDecimal(ordersEntityOld.getNxDoWeight());
                    BigDecimal totalCost = BigDecimal.ZERO; // 总成本
                    BigDecimal remainingWeight = needWeight; // 剩余需要扣减的重量

                    System.out.println("开始FIFO扣减库存 - 订单重量: " + needWeight);

                    // 查询商品信息，判断出库单位
                    NxDistributerGoodsEntity disGoodsForOut = nxDistributerGoodsService.queryObject(ordersEntity.getNxDoDisGoodsId());
                    boolean useCartonPrice = false;
                    Integer itemsPerCartonForOut = null;
                    if (disGoodsForOut != null 
                            && disGoodsForOut.getNxDgCartonUnit() != null 
                            && !disGoodsForOut.getNxDgCartonUnit().trim().isEmpty()
                            && ordersEntity.getNxDoStandard() != null
                            && isStandardMatch(ordersEntity.getNxDoStandard(), disGoodsForOut.getNxDgCartonUnit())) {
                        // 订单规格与外包装单位匹配（支持"箱"和"件"等同义词），使用外包装单价
                        useCartonPrice = true;
                        itemsPerCartonForOut = disGoodsForOut.getNxDgItemsPerCarton();
                        System.out.println("出库单位与外包装单位匹配，使用外包装单价: " + disGoodsForOut.getNxDgCartonUnit() 
                                + ", 每箱" + itemsPerCartonForOut + "个");
                    }

                    for (NxDistributerGoodsShelfStockEntity stockEntity : stockEntities) {
                        if (remainingWeight.compareTo(BigDecimal.ZERO) <= 0) {
                            break; // 已经扣减完毕
                        }

                        BigDecimal stockRestWeight = new BigDecimal(stockEntity.getNxDgssRestWeight());

                        if (stockRestWeight.compareTo(BigDecimal.ZERO) <= 0) {
                            continue; // 跳过没有库存的批次
                        }

                        // 计算本批次可以扣减的重量（最小单位）
                        BigDecimal deductWeight;
                        if (remainingWeight.compareTo(stockRestWeight) <= 0) {
                            // 本批次够用
                            deductWeight = remainingWeight;
                        } else {
                            // 本批次不够，全部扣减
                            deductWeight = stockRestWeight;
                        }

                        // 根据出库单位选择对应的单价和计算方式
                        BigDecimal stockPrice;
                        BigDecimal deductWeightForCalc; // 用于成本计算的扣减数量
                        if (useCartonPrice && stockEntity.getNxDgssPriceCarton() != null 
                                && !stockEntity.getNxDgssPriceCarton().trim().isEmpty()
                                && itemsPerCartonForOut != null && itemsPerCartonForOut > 0) {
                            // 使用外包装单价，需要将最小单位数量转换为箱数
                            deductWeightForCalc = deductWeight.divide(new BigDecimal(itemsPerCartonForOut), 4, BigDecimal.ROUND_HALF_UP);
                            stockPrice = new BigDecimal(stockEntity.getNxDgssPriceCarton());
                            System.out.println("批次ID: " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                    ", 剩余: " + stockRestWeight + ", 扣减: " + deductWeight + "个(" + deductWeightForCalc + "箱)"
                                    + ", 使用外包装单价: " + stockPrice + "元/箱");
                        } else {
                            // 使用最小单位单价
                            String priceStr = stockEntity.getNxDgssPrice();
                            stockPrice = (priceStr == null || priceStr.trim().isEmpty())
                                    ? BigDecimal.ZERO
                                    : new BigDecimal(priceStr);
                            deductWeightForCalc = deductWeight;
                            System.out.println("批次ID: " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                    ", 剩余: " + stockRestWeight + ", 扣减: " + deductWeight
                                    + ", 使用最小单位单价: " + stockPrice);
                        }

                        // 计算本批次的成本（使用对应的单价和数量）
                        BigDecimal batchCost = deductWeightForCalc.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        totalCost = totalCost.add(batchCost);

                        // 更新库存批次的剩余数量
                        BigDecimal newRestWeight = stockRestWeight.subtract(deductWeight);
                        stockEntity.setNxDgssRestWeight(newRestWeight.toString());

                        // 更新库存批次的剩余成本（使用最小单位单价计算）
                        String priceStrForRest = stockEntity.getNxDgssPrice();
                        BigDecimal stockPriceForRest = (priceStrForRest == null || priceStrForRest.trim().isEmpty())
                                ? BigDecimal.ZERO
                                : new BigDecimal(priceStrForRest);
                        BigDecimal newRestSubtotal = newRestWeight.multiply(stockPriceForRest).setScale(1, BigDecimal.ROUND_HALF_UP);
                        stockEntity.setNxDgssRestSubtotal(newRestSubtotal.toString());

                        // 保存库存批次更新
                        nxDisGoodsShelfStockService.update(stockEntity);

                        // 创建库存扣减记录
                        NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
                        reduceEntity.setNxDgssrNxDistributerId(ordersEntity.getNxDoDistributerId());
                        reduceEntity.setNxDgssrNxDisGoodsId(ordersEntity.getNxDoDisGoodsId());
                        reduceEntity.setNxDgssrNxDisGoodsFatherId(stockEntity.getNxDgssNxDisGoodsFatherId());
                        reduceEntity.setNxDgssrNxStockId(stockEntity.getNxDistributerGoodsShelfStockId());
                        reduceEntity.setNxDgssrDate(formatWhatDay(0));
                        reduceEntity.setNxDgssrWeek(getWeek(0));
                        reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
                        reduceEntity.setNxDgssrFullTime(formatWhatYearDayTime(0));
                        reduceEntity.setNxDgssrType(0); // 0=销售扣减
                        reduceEntity.setNxDgssrDoUserId(ordersEntity.getNxDoPickUserId());
                        reduceEntity.setNxDgssrCostWeight(deductWeight.toString());
                        reduceEntity.setNxDgssrCostSubtotal(batchCost.toString());
                        reduceEntity.setNxDgssrNxPurGoodsId(stockEntity.getNxDgssNxPurGoodsId());
                        reduceEntity.setNxDgssrNxDepOrderId(ordersEntity.getNxDepartmentOrdersId());
                        reduceEntity.setNxDgssrStatus(0);
                        reduceEntity.setNxDgssrProduceWeight(deductWeight.toString());
                        reduceEntity.setNxDgssrProduceSubtotal(batchCost.toString());
                        nxDisGoodsShelfStockReduceService.save(reduceEntity);

                        System.out.println("扣减批次 " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                " - 扣减重量: " + deductWeight + ", 本批次成本: " + batchCost +
                                ", 新剩余: " + newRestWeight + ", 已保存扣减记录");

                        // 减少剩余需要扣减的重量
                        remainingWeight = remainingWeight.subtract(deductWeight);
                    }

                    // 计算加权平均成本价（保留1位小数）
                    // 如果使用外包装单价，needWeight应该是箱数，avgCostPrice是每箱的平均成本价
                    BigDecimal avgCostPrice = totalCost.divide(needWeight, 1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoCostPrice(avgCostPrice.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                    ordersEntity.setNxDoCostSubtotal(totalCost.toString());

                    // 计算利润
                    BigDecimal sellPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                    BigDecimal profit = sellPrice.subtract(avgCostPrice);
                    BigDecimal profitSubtotal = profit.multiply(needWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());

                    System.out.println("FIFO扣减完成 - 总成本: " + totalCost +
                            ", 平均成本价: " + avgCostPrice +
                            ", 利润小计: " + profitSubtotal);

                    if (remainingWeight.compareTo(BigDecimal.ZERO) > 0) {
                        System.out.println("警告：库存不足，还需要: " + remainingWeight);
                        Map<String, Object> shortage = new HashMap<>();
                        shortage.put("orderId", ordersEntity.getNxDepartmentOrdersId());
                        shortage.put("shortage", remainingWeight);
                        shortages.add(shortage);
                    }
                }
            }

            ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
            nxDepartmentOrdersService.update(ordersEntity);


            Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
            if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                if (purchaseGoodsEntity != null) {
                    Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                    Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                    System.out.println("purGoodsss" + nxDpgFinishAmount);
                    if (nxDpgOrdersAmount != null && nxDpgFinishAmount != null) {
                        if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
//查询商品如有有采购批次nxDistributerPurchaseBatch
                            // 并且该批次下的全部采购商品nxDistributerGoods的 nxDpgStatus == getNxDisPurchaseGoodsFinishBuy 的数量
                            //是所有采购商品数量减去 1 ，那么就认为该采购批次也是完成。
                            if(purchaseGoodsEntity.getNxDpgBatchId() != null && purchaseGoodsEntity.getNxDpgBatchId() != -1){
                                Map<String, Object> map = new HashMap<>();
                                map.put("batchId",purchaseGoodsEntity.getNxDpgBatchId() );
                                int count = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                                map.put("equalStatus",getNxDisPurchaseGoodsFinishBuy());
                                int finishCount = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                                if(count - finishCount == 1){
                                    NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDistributerPurchaseBatchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());
                                    nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
                                    nxDistributerPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);
                                }
                            }
                        } else {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                        }
                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                } else {
                    System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
                }
            }


            System.out.println("xnordidiididid" + ordersEntity.getNxDoGbDepartmentOrderId());
            if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(ordersEntity.getNxDepartmentOrdersId());
                gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
                gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusProcurement());
                gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());

                if (gbDepartmentOrdersEntity.getGbDoPrice() != null && !gbDepartmentOrdersEntity.getGbDoPrice().trim().isEmpty() && !gbDepartmentOrdersEntity.getGbDoPrice().equals("0.0") && gbDepartmentOrdersEntity.getGbDoWeight() != null) {
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
                if (purchaseGoodsEntity.getGbDpgBuyPrice() != null && !purchaseGoodsEntity.getGbDpgBuyPrice().trim().isEmpty()) {
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


    @RequestMapping(value = "/pickerGiveOrderWeight", method = RequestMethod.POST)
    @ResponseBody
    public R pickerGiveOrderWeight(Integer orderId, String orderWeight, Integer pickerUserId) {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObjectNew(orderId);

            ordersEntity.setNxDoPickUserId(pickerUserId);

            ordersEntity.setNxDoWeight(orderWeight);
            if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                BigDecimal subtotal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(orderWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoSubtotal(subtotal.toString());
                ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
            }
             ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
            nxDepartmentOrdersService.update(ordersEntity);

            Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
            if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                if (purchaseGoodsEntity != null) {
                    Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                    Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                    System.out.println("purGoodsss" + nxDpgFinishAmount);
                    if (nxDpgOrdersAmount != null && nxDpgFinishAmount != null) {
                        if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
//查询商品如有有采购批次nxDistributerPurchaseBatch
                            // 并且该批次下的全部采购商品nxDistributerGoods的 nxDpgStatus == getNxDisPurchaseGoodsFinishBuy 的数量
                            //是所有采购商品数量减去 1 ，那么就认为该采购批次也是完成。
                            if(purchaseGoodsEntity.getNxDpgBatchId() != null && purchaseGoodsEntity.getNxDpgBatchId() != -1){
                                Map<String, Object> map = new HashMap<>();
                                map.put("batchId",purchaseGoodsEntity.getNxDpgBatchId() );
                                int count = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                                map.put("equalStatus",getNxDisPurchaseGoodsFinishBuy());
                                int finishCount = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                                if(count - finishCount == 1){
                                    NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDistributerPurchaseBatchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());
                                    nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
                                    nxDistributerPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);
                                }
                            }
                        } else {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                        }
                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                } else {
                    System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
                }
            }


            System.out.println("xnordidiididid" + ordersEntity.getNxDoGbDepartmentOrderId());
            if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(ordersEntity.getNxDepartmentOrdersId());
                gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
                gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusProcurement());
                gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());

                if (gbDepartmentOrdersEntity.getGbDoPrice() != null && !gbDepartmentOrdersEntity.getGbDoPrice().trim().isEmpty() && !gbDepartmentOrdersEntity.getGbDoPrice().equals("0.0") && gbDepartmentOrdersEntity.getGbDoWeight() != null) {
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
                if (purchaseGoodsEntity.getGbDpgBuyPrice() != null && !purchaseGoodsEntity.getGbDpgBuyPrice().trim().isEmpty()) {
                    BigDecimal decimal1 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                    BigDecimal decimal2 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
                    BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal3.toString());
                }
                purchaseGoodsEntity.setGbDpgStatus(getNxDisPurchaseGoodsIsPurchase());
                System.out.println("purpprur" + purchaseGoodsEntity);
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

        }

        return R.ok();
    }



    @RequestMapping(value = "/pickerGiveOrderWeightUpdateShelfStock", method = RequestMethod.POST)
    @ResponseBody
    public R pickerGiveOrderWeightUpdateShelfStock(Integer orderId, String orderWeight, Integer pickerUserId, Integer shelfId) {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObjectNew(orderId);
            ordersEntity.setNxDoPickUserId(pickerUserId);

            // 无论价格是否为空，都要设置正确的重量
            ordersEntity.setNxDoWeight(orderWeight);

            if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                BigDecimal subtotal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(orderWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoSubtotal(subtotal.toString());
                ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
                System.out.println("subsbbsbsbbsbs" + subtotal.toString());
                System.out.println("subsbbsbsbbsbs" + ordersEntity.getNxDoCostPrice());

                //查询是否有库存（按日期排序，实现FIFO先进先出）
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsId", ordersEntity.getNxDoDisGoodsId());
                map.put("restWeight", 0);
                // 如果订单有出库货架ID，则只查询该货架的库存
                if (shelfId != -1) {
                    map.put("shelfId", shelfId);
                    System.out.println("🔍 查询指定货架的库存 - 货架ID: " + shelfId);
                }
                List<NxDistributerGoodsShelfStockEntity> stockEntities = nxDisGoodsShelfStockService.queryShelfStockListByParams(map);

                if (stockEntities.size() > 0) {
                    // 查询商品信息，判断出库单位（需要先判断，才能正确转换数量）
                    NxDistributerGoodsEntity disGoodsForOut = nxDistributerGoodsService.queryObject(ordersEntity.getNxDoDisGoodsId());
                    boolean useCartonPrice = false;
                    Integer itemsPerCartonForOut = null;
                    if (disGoodsForOut != null
                            && disGoodsForOut.getNxDgCartonUnit() != null
                            && !disGoodsForOut.getNxDgCartonUnit().trim().isEmpty()
                            && ordersEntity.getNxDoPrintStandard() != null
                            && ordersEntity.getNxDoPrintStandard().trim().equals(disGoodsForOut.getNxDgCartonUnit().trim())) {
                        // 订单规格与外包装单位匹配，使用外包装单价
                        useCartonPrice = true;
                        itemsPerCartonForOut = disGoodsForOut.getNxDgItemsPerCarton();
                        System.out.println("✅ 出库单位与外包装单位匹配，使用外包装单价: " + disGoodsForOut.getNxDgCartonUnit()
                                + ", 每箱" + itemsPerCartonForOut + "个");
                    } else {
                        System.out.println("⚠️ 出库单位不匹配 - 订单规格: " + ordersEntity.getNxDoStandard()
                                + ", 商品外包装单位: " + (disGoodsForOut != null ? disGoodsForOut.getNxDgCartonUnit() : "null"));
                    }

                    // 需要出货的重量（使用确定的重量值）
                    BigDecimal needWeightRaw = new BigDecimal(orderWeight);
                    BigDecimal needWeight; // 转换为最小单位后的重量
                    BigDecimal totalCost = BigDecimal.ZERO; // 总成本

                    System.out.println("🔍 调试信息 - 前台提交的重量: " + needWeightRaw + ", 订单规格: " + ordersEntity.getNxDoStandard());
                    System.out.println("🔍 调试信息 - useCartonPrice: " + useCartonPrice + ", itemsPerCartonForOut: " + itemsPerCartonForOut);

                    // 如果使用大包装单位，需要将箱数转换为最小单位数量
                    if (useCartonPrice && itemsPerCartonForOut != null && itemsPerCartonForOut > 0) {
                        // 前台提交的是箱数，需要转换为最小单位数量
                        needWeight = needWeightRaw.multiply(new BigDecimal(itemsPerCartonForOut));
                        System.out.println("✅ 开始FIFO扣减库存 - 订单箱数: " + needWeightRaw + "箱, 转换为最小单位: " + needWeight + "个");
                    } else {
                        // 前台提交的已经是最小单位数量
                        needWeight = needWeightRaw;
                        System.out.println("✅ 开始FIFO扣减库存 - 订单重量: " + needWeight + "个（未使用大包装单位）");
                    }

                    BigDecimal remainingWeight = needWeight; // 剩余需要扣减的重量（最小单位）
                    System.out.println("🔍 调试信息 - remainingWeight（剩余需要扣减）: " + remainingWeight + "个");

                    for (NxDistributerGoodsShelfStockEntity stockEntity : stockEntities) {
                        if (remainingWeight.compareTo(BigDecimal.ZERO) <= 0) {
                            break; // 已经扣减完毕
                        }

                        BigDecimal stockRestWeight = new BigDecimal(stockEntity.getNxDgssRestWeight());

                        // 计算本批次可以扣减的重量（最小单位）
                        BigDecimal deductWeight;
                        if (remainingWeight.compareTo(stockRestWeight) <= 0) {
                            // 本批次够用
                            deductWeight = remainingWeight;
                        } else {
                            // 本批次不够，全部扣减
                            deductWeight = stockRestWeight;
                        }

                        // 根据出库单位选择对应的单价和计算方式
                        BigDecimal stockPrice;
                        BigDecimal deductWeightForCalc; // 用于成本计算的扣减数量
                        if (useCartonPrice && stockEntity.getNxDgssPriceCarton() != null
                                && !stockEntity.getNxDgssPriceCarton().trim().isEmpty()
                                && itemsPerCartonForOut != null && itemsPerCartonForOut > 0) {
                            // 使用外包装单价，需要将最小单位数量转换为箱数
                            deductWeightForCalc = deductWeight.divide(new BigDecimal(itemsPerCartonForOut), 4, BigDecimal.ROUND_HALF_UP);
                            stockPrice = new BigDecimal(stockEntity.getNxDgssPriceCarton());
                            System.out.println("批次ID: " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                    ", 剩余: " + stockRestWeight + ", 扣减: " + deductWeight + "个(" + deductWeightForCalc + "箱)"
                                    + ", 使用外包装单价: " + stockPrice + "元/箱");
                        } else {
                            // 使用最小单位单价
                            String priceStr = stockEntity.getNxDgssPrice();
                            stockPrice = (priceStr == null || priceStr.trim().isEmpty())
                                    ? BigDecimal.ZERO
                                    : new BigDecimal(priceStr);
                            deductWeightForCalc = deductWeight;
                            System.out.println("批次ID: " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                    ", 剩余: " + stockRestWeight + ", 扣减: " + deductWeight
                                    + ", 使用最小单位单价: " + stockPrice);
                        }

                        if (stockRestWeight.compareTo(BigDecimal.ZERO) <= 0) {
                            continue; // 跳过没有库存的批次
                        }

                        // 计算本批次的成本（使用对应的单价和数量）
                        BigDecimal batchCost = deductWeightForCalc.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        totalCost = totalCost.add(batchCost);

                        // 更新库存批次的剩余数量
                        BigDecimal newRestWeight = stockRestWeight.subtract(deductWeight);
                        stockEntity.setNxDgssRestWeight(newRestWeight.toString());

                        // 更新库存批次的剩余成本（使用最小单位单价计算）
                        String priceStrForRest = stockEntity.getNxDgssPrice();
                        BigDecimal stockPriceForRest = (priceStrForRest == null || priceStrForRest.trim().isEmpty())
                                ? BigDecimal.ZERO
                                : new BigDecimal(priceStrForRest);
                        BigDecimal newRestSubtotal = newRestWeight.multiply(stockPriceForRest).setScale(1, BigDecimal.ROUND_HALF_UP);
                        stockEntity.setNxDgssRestSubtotal(newRestSubtotal.toString());

                        // 保存库存批次更新
                        nxDisGoodsShelfStockService.update(stockEntity);

                        // 创建库存扣减记录
                        NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
                        reduceEntity.setNxDgssrNxDistributerId(ordersEntity.getNxDoDistributerId());
                        reduceEntity.setNxDgssrNxDisGoodsId(ordersEntity.getNxDoDisGoodsId());
                        reduceEntity.setNxDgssrNxDisGoodsFatherId(stockEntity.getNxDgssNxDisGoodsFatherId());
                        reduceEntity.setNxDgssrNxStockId(stockEntity.getNxDistributerGoodsShelfStockId());
                        reduceEntity.setNxDgssrDate(formatWhatDay(0));
                        reduceEntity.setNxDgssrWeek(getWeek(0));
                        reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
                        reduceEntity.setNxDgssrFullTime(formatWhatYearDayTime(0));
                        reduceEntity.setNxDgssrType(0); // 0=销售扣减
                        reduceEntity.setNxDgssrDoUserId(ordersEntity.getNxDoPickUserId());
                        reduceEntity.setNxDgssrCostWeight(deductWeight.toString());
                        reduceEntity.setNxDgssrCostSubtotal(batchCost.toString());
                        reduceEntity.setNxDgssrNxPurGoodsId(stockEntity.getNxDgssNxPurGoodsId());
                        reduceEntity.setNxDgssrNxDepOrderId(ordersEntity.getNxDepartmentOrdersId());
                        reduceEntity.setNxDgssrStatus(0);
                        reduceEntity.setNxDgssrProduceWeight(deductWeight.toString());
                        reduceEntity.setNxDgssrProduceSubtotal(batchCost.toString());
                        nxDisGoodsShelfStockReduceService.save(reduceEntity);

                        System.out.println("扣减批次 " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                " - 扣减重量: " + deductWeight + ", 本批次成本: " + batchCost +
                                ", 新剩余: " + newRestWeight + ", 已保存扣减记录");

                        // 减少剩余需要扣减的重量
                        remainingWeight = remainingWeight.subtract(deductWeight);
                    }

                    // 计算加权平均成本价（保留1位小数）
                    // 如果使用外包装单价，needWeight应该是箱数，avgCostPrice是每箱的平均成本价
                    BigDecimal avgCostPrice = totalCost.divide(needWeight, 1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoCostPrice(avgCostPrice.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                    ordersEntity.setNxDoCostSubtotal(totalCost.toString());

                    // 计算利润
                    BigDecimal sellPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                    BigDecimal profit = sellPrice.subtract(avgCostPrice);
                    BigDecimal profitSubtotal = profit.multiply(needWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());

                }
            }

            ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
            nxDepartmentOrdersService.update(ordersEntity);


            Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
            if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                if (purchaseGoodsEntity != null) {
                    Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                    Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                    System.out.println("purGoodsss" + nxDpgFinishAmount);
                    if (nxDpgOrdersAmount != null && nxDpgFinishAmount != null) {
                        if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
//查询商品如有有采购批次nxDistributerPurchaseBatch
                            // 并且该批次下的全部采购商品nxDistributerGoods的 nxDpgStatus == getNxDisPurchaseGoodsFinishBuy 的数量
                            //是所有采购商品数量减去 1 ，那么就认为该采购批次也是完成。
                            if(purchaseGoodsEntity.getNxDpgBatchId() != null && purchaseGoodsEntity.getNxDpgBatchId() != -1){
                                Map<String, Object> map = new HashMap<>();
                                map.put("batchId",purchaseGoodsEntity.getNxDpgBatchId() );
                                int count = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                                map.put("equalStatus",getNxDisPurchaseGoodsFinishBuy());
                                int finishCount = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                                if(count - finishCount == 1){
                                    NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDistributerPurchaseBatchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());
                                    nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
                                    nxDistributerPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);
                                }
                            }
                        } else {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                        }
                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                } else {
                    System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
                }
            }


            System.out.println("xnordidiididid" + ordersEntity.getNxDoGbDepartmentOrderId());
            if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(ordersEntity.getNxDepartmentOrdersId());
                gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
                gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusProcurement());
                gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());

                if (gbDepartmentOrdersEntity.getGbDoPrice() != null && !gbDepartmentOrdersEntity.getGbDoPrice().trim().isEmpty() && !gbDepartmentOrdersEntity.getGbDoPrice().equals("0.0") && gbDepartmentOrdersEntity.getGbDoWeight() != null) {
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
                if (purchaseGoodsEntity.getGbDpgBuyPrice() != null && !purchaseGoodsEntity.getGbDpgBuyPrice().trim().isEmpty()) {
                    BigDecimal decimal1 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                    BigDecimal decimal2 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
                    BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal3.toString());
                }
                purchaseGoodsEntity.setGbDpgStatus(getNxDisPurchaseGoodsIsPurchase());
                System.out.println("purpprur" + purchaseGoodsEntity);
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

            }

        return R.ok();
    }



    @RequestMapping(value = "/giveOrderWeightListForStockShelfGoods", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderWeightListForStockShelfGoods(@RequestBody List<NxDepartmentOrdersEntity> ordersEntityList) {
        System.out.println("giveOrderWeightListForStockAndFinishgiveOrderWeightListForStockAndFinish" + ordersEntityList);
        List<Map<String, Object>> shortages = new ArrayList<>();
        for (NxDepartmentOrdersEntity ordersEntityOld : ordersEntityList) {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObjectNew(ordersEntityOld.getNxDepartmentOrdersId());
            ordersEntity.setNxDoPickUserId(ordersEntityOld.getNxDoPickUserId());
            
            // 确定使用的重量：优先使用前台传入的重量，如果为空则使用订单数量
            String weightToUse = null;
            if (ordersEntityOld.getNxDoWeight() != null && !ordersEntityOld.getNxDoWeight().trim().isEmpty()) {
                weightToUse = ordersEntityOld.getNxDoWeight();
                logger.info("[giveOrderWeightListForStockShelfGoods] 使用前台传入的重量: {}", weightToUse);
            } else if (ordersEntity.getNxDoQuantity() != null && !ordersEntity.getNxDoQuantity().trim().isEmpty()) {
                // 如果前台没有传入重量，使用订单数量作为重量
                weightToUse = ordersEntity.getNxDoQuantity();
                logger.warn("[giveOrderWeightListForStockShelfGoods] 前台未传入重量，使用订单数量作为重量: {}", weightToUse);
            } else {
                // 如果都没有，使用数据库中的重量（但记录警告）
                weightToUse = ordersEntity.getNxDoWeight();
                logger.warn("[giveOrderWeightListForStockShelfGoods] 前台未传入重量且订单数量为空，使用数据库中的重量: {} (订单ID: {})", 
                        weightToUse, ordersEntity.getNxDepartmentOrdersId());
            }
            
            // 记录调试信息
            logger.info("[giveOrderWeightListForStockShelfGoods] 订单ID: {}, 订单数量: {}, 数据库重量: {}, 前台传入重量: {}, 最终使用重量: {}", 
                    ordersEntity.getNxDepartmentOrdersId(), 
                    ordersEntity.getNxDoQuantity(), 
                    ordersEntity.getNxDoWeight(), 
                    ordersEntityOld.getNxDoWeight(), 
                    weightToUse);
            
            // 无论价格是否为空，都要设置正确的重量
            ordersEntity.setNxDoWeight(weightToUse);
            
            if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                BigDecimal subtotal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(weightToUse)).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoSubtotal(subtotal.toString());
                ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
                System.out.println("subsbbsbsbbsbs" + subtotal.toString());
                System.out.println("subsbbsbsbbsbs" + ordersEntity.getNxDoCostPrice());

                //查询是否有库存（按日期排序，实现FIFO先进先出）
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsId", ordersEntity.getNxDoDisGoodsId());
                map.put("restWeight", 0);
                // 如果订单有出库货架ID，则只查询该货架的库存
                if (ordersEntityOld.getOutShelfId() != null) {
                    map.put("shelfId", ordersEntityOld.getOutShelfId());
                    System.out.println("🔍 查询指定货架的库存 - 货架ID: " + ordersEntityOld.getOutShelfId());
                }
                List<NxDistributerGoodsShelfStockEntity> stockEntities = nxDisGoodsShelfStockService.queryShelfStockListByParams(map);

                if (stockEntities.size() > 0) {
                    // 查询商品信息，判断出库单位（需要先判断，才能正确转换数量）
                    NxDistributerGoodsEntity disGoodsForOut = nxDistributerGoodsService.queryObject(ordersEntity.getNxDoDisGoodsId());
                    boolean useCartonPrice = false;
                    Integer itemsPerCartonForOut = null;
                    if (disGoodsForOut != null 
                            && disGoodsForOut.getNxDgCartonUnit() != null 
                            && !disGoodsForOut.getNxDgCartonUnit().trim().isEmpty()
                            && ordersEntity.getNxDoPrintStandard() != null
                            && ordersEntity.getNxDoPrintStandard().trim().equals(disGoodsForOut.getNxDgCartonUnit().trim())) {
                        // 订单规格与外包装单位匹配，使用外包装单价
                        useCartonPrice = true;
                        itemsPerCartonForOut = disGoodsForOut.getNxDgItemsPerCarton();
                        System.out.println("✅ 出库单位与外包装单位匹配，使用外包装单价: " + disGoodsForOut.getNxDgCartonUnit() 
                                + ", 每箱" + itemsPerCartonForOut + "个");
                    } else {
                        System.out.println("⚠️ 出库单位不匹配 - 订单规格: " + ordersEntity.getNxDoStandard() 
                                + ", 商品外包装单位: " + (disGoodsForOut != null ? disGoodsForOut.getNxDgCartonUnit() : "null"));
                    }

                    // 需要出货的重量（使用确定的重量值）
                    BigDecimal needWeightRaw = new BigDecimal(weightToUse);
                    BigDecimal needWeight; // 转换为最小单位后的重量
                    BigDecimal totalCost = BigDecimal.ZERO; // 总成本
                    
                    System.out.println("🔍 调试信息 - 前台提交的重量: " + needWeightRaw + ", 订单规格: " + ordersEntity.getNxDoStandard());
                    System.out.println("🔍 调试信息 - useCartonPrice: " + useCartonPrice + ", itemsPerCartonForOut: " + itemsPerCartonForOut);
                    
                    // 如果使用大包装单位，需要将箱数转换为最小单位数量
                    if (useCartonPrice && itemsPerCartonForOut != null && itemsPerCartonForOut > 0) {
                        // 前台提交的是箱数，需要转换为最小单位数量
                        needWeight = needWeightRaw.multiply(new BigDecimal(itemsPerCartonForOut));
                        System.out.println("✅ 开始FIFO扣减库存 - 订单箱数: " + needWeightRaw + "箱, 转换为最小单位: " + needWeight + "个");
                    } else {
                        // 前台提交的已经是最小单位数量
                        needWeight = needWeightRaw;
                        System.out.println("✅ 开始FIFO扣减库存 - 订单重量: " + needWeight + "个（未使用大包装单位）");
                    }
                    
                    BigDecimal remainingWeight = needWeight; // 剩余需要扣减的重量（最小单位）
                    System.out.println("🔍 调试信息 - remainingWeight（剩余需要扣减）: " + remainingWeight + "个");

                    for (NxDistributerGoodsShelfStockEntity stockEntity : stockEntities) {
                        if (remainingWeight.compareTo(BigDecimal.ZERO) <= 0) {
                            break; // 已经扣减完毕
                        }

                        BigDecimal stockRestWeight = new BigDecimal(stockEntity.getNxDgssRestWeight());
                        
                        // 计算本批次可以扣减的重量（最小单位）
                        BigDecimal deductWeight;
                        if (remainingWeight.compareTo(stockRestWeight) <= 0) {
                            // 本批次够用
                            deductWeight = remainingWeight;
                        } else {
                            // 本批次不够，全部扣减
                            deductWeight = stockRestWeight;
                        }

                        // 根据出库单位选择对应的单价和计算方式
                        BigDecimal stockPrice;
                        BigDecimal deductWeightForCalc; // 用于成本计算的扣减数量
                        if (useCartonPrice && stockEntity.getNxDgssPriceCarton() != null 
                                && !stockEntity.getNxDgssPriceCarton().trim().isEmpty()
                                && itemsPerCartonForOut != null && itemsPerCartonForOut > 0) {
                            // 使用外包装单价，需要将最小单位数量转换为箱数
                            deductWeightForCalc = deductWeight.divide(new BigDecimal(itemsPerCartonForOut), 4, BigDecimal.ROUND_HALF_UP);
                            stockPrice = new BigDecimal(stockEntity.getNxDgssPriceCarton());
                            System.out.println("批次ID: " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                    ", 剩余: " + stockRestWeight + ", 扣减: " + deductWeight + "个(" + deductWeightForCalc + "箱)"
                                    + ", 使用外包装单价: " + stockPrice + "元/箱");
                        } else {
                            // 使用最小单位单价
                            String priceStr = stockEntity.getNxDgssPrice();
                            stockPrice = (priceStr == null || priceStr.trim().isEmpty())
                                    ? BigDecimal.ZERO
                                    : new BigDecimal(priceStr);
                            deductWeightForCalc = deductWeight;
                            System.out.println("批次ID: " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                    ", 剩余: " + stockRestWeight + ", 扣减: " + deductWeight
                                    + ", 使用最小单位单价: " + stockPrice);
                        }

                        if (stockRestWeight.compareTo(BigDecimal.ZERO) <= 0) {
                            continue; // 跳过没有库存的批次
                        }

                        // 计算本批次的成本（使用对应的单价和数量）
                        BigDecimal batchCost = deductWeightForCalc.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        totalCost = totalCost.add(batchCost);

                        // 更新库存批次的剩余数量
                        BigDecimal newRestWeight = stockRestWeight.subtract(deductWeight);
                        stockEntity.setNxDgssRestWeight(newRestWeight.toString());

                        // 更新库存批次的剩余成本（使用最小单位单价计算）
                        String priceStrForRest = stockEntity.getNxDgssPrice();
                        BigDecimal stockPriceForRest = (priceStrForRest == null || priceStrForRest.trim().isEmpty())
                                ? BigDecimal.ZERO
                                : new BigDecimal(priceStrForRest);
                        BigDecimal newRestSubtotal = newRestWeight.multiply(stockPriceForRest).setScale(1, BigDecimal.ROUND_HALF_UP);
                        stockEntity.setNxDgssRestSubtotal(newRestSubtotal.toString());

                        // 保存库存批次更新
                        nxDisGoodsShelfStockService.update(stockEntity);

                        // 创建库存扣减记录
                        NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
                        reduceEntity.setNxDgssrNxDistributerId(ordersEntity.getNxDoDistributerId());
                        reduceEntity.setNxDgssrNxDisGoodsId(ordersEntity.getNxDoDisGoodsId());
                        reduceEntity.setNxDgssrNxDisGoodsFatherId(stockEntity.getNxDgssNxDisGoodsFatherId());
                        reduceEntity.setNxDgssrNxStockId(stockEntity.getNxDistributerGoodsShelfStockId());
                        reduceEntity.setNxDgssrDate(formatWhatDay(0));
                        reduceEntity.setNxDgssrWeek(getWeek(0));
                        reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
                        reduceEntity.setNxDgssrFullTime(formatWhatYearDayTime(0));
                        reduceEntity.setNxDgssrType(0); // 0=销售扣减
                        reduceEntity.setNxDgssrDoUserId(ordersEntity.getNxDoPickUserId());
                        reduceEntity.setNxDgssrCostWeight(deductWeight.toString());
                        reduceEntity.setNxDgssrCostSubtotal(batchCost.toString());
                        reduceEntity.setNxDgssrNxPurGoodsId(stockEntity.getNxDgssNxPurGoodsId());
                        reduceEntity.setNxDgssrNxDepOrderId(ordersEntity.getNxDepartmentOrdersId());
                        reduceEntity.setNxDgssrStatus(0);
                        reduceEntity.setNxDgssrProduceWeight(deductWeight.toString());
                        reduceEntity.setNxDgssrProduceSubtotal(batchCost.toString());
                        nxDisGoodsShelfStockReduceService.save(reduceEntity);

                        System.out.println("扣减批次 " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                " - 扣减重量: " + deductWeight + ", 本批次成本: " + batchCost +
                                ", 新剩余: " + newRestWeight + ", 已保存扣减记录");

                        // 减少剩余需要扣减的重量
                        remainingWeight = remainingWeight.subtract(deductWeight);
                    }

                    // 计算加权平均成本价（保留1位小数）
                    // 如果使用外包装单价，needWeight应该是箱数，avgCostPrice是每箱的平均成本价
                    BigDecimal avgCostPrice = totalCost.divide(needWeight, 1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoCostPrice(avgCostPrice.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                    ordersEntity.setNxDoCostSubtotal(totalCost.toString());

                    // 计算利润
                    BigDecimal sellPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                    BigDecimal profit = sellPrice.subtract(avgCostPrice);
                    BigDecimal profitSubtotal = profit.multiply(needWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());

                    System.out.println("FIFO扣减完成 - 总成本: " + totalCost +
                            ", 平均成本价: " + avgCostPrice +
                            ", 利润小计: " + profitSubtotal);

                    if (remainingWeight.compareTo(BigDecimal.ZERO) > 0) {
                        System.out.println("警告：库存不足，还需要: " + remainingWeight);
                        Map<String, Object> shortage = new HashMap<>();
                        shortage.put("orderId", ordersEntity.getNxDepartmentOrdersId());
                        shortage.put("shortage", remainingWeight);
                        shortages.add(shortage);
                    }
                }
            }

            ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
            nxDepartmentOrdersService.update(ordersEntity);

            Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
            if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                if (purchaseGoodsEntity != null) {
                    Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                    Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                    System.out.println("purGoodsss" + nxDpgFinishAmount);
                    if (nxDpgOrdersAmount != null && nxDpgFinishAmount != null) {
                        if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                            //查询商品如有有采购批次nxDistributerPurchaseBatch
                            // 并且该批次下的全部采购商品nxDistributerGoods的 nxDpgStatus == getNxDisPurchaseGoodsFinishBuy 的数量
                            //是所有采购商品数量减去 1 ，那么就认为该采购批次也是完成。
                            System.out.println("gengixathciid" + purchaseGoodsEntity.getNxDpgBatchId());
                            if(purchaseGoodsEntity.getNxDpgBatchId() != null && purchaseGoodsEntity.getNxDpgBatchId() != -1){
                                Map<String, Object> map = new HashMap<>();
                                map.put("batchId",purchaseGoodsEntity.getNxDpgBatchId() );
                                int count = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                                map.put("equalStatus",getNxDisPurchaseGoodsFinishBuy());
                                int finishCount = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                                System.out.println("fincoundfdafdafadfat" + finishCount);
                                if(count - finishCount == 1){
                                    NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDistributerPurchaseBatchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());
                                    nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
                                    nxDistributerPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);
                                }
                            }

                        } else {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                        }
                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                } else {
                    System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
                }
            }


            System.out.println("xnordidiididid" + ordersEntity.getNxDoGbDepartmentOrderId());
            if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(ordersEntity.getNxDepartmentOrdersId());
                gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
                gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusProcurement());
                gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());

                if (gbDepartmentOrdersEntity.getGbDoPrice() != null && !gbDepartmentOrdersEntity.getGbDoPrice().trim().isEmpty() && !gbDepartmentOrdersEntity.getGbDoPrice().equals("0.0") && gbDepartmentOrdersEntity.getGbDoWeight() != null) {
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
                if (purchaseGoodsEntity.getGbDpgBuyPrice() != null && !purchaseGoodsEntity.getGbDpgBuyPrice().trim().isEmpty()) {
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

        return R.ok().put("shortages", shortages);
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
            reduceEntity.setNxDgssrDate(formatWhatDay(0));
            reduceEntity.setNxDgssrNxDepOrderId(ordersEntity.getNxDepartmentOrdersId());
            reduceEntity.setNxDgssrCostWeight(stockEntity.getNxDgssInventoryWeight());
            reduceEntity.setNxDgssrCostSubtotal(costSubtotal.toString());
            reduceEntity.setNxDgssrNxDistributerId(ordersEntity.getNxDoDistributerId());
            reduceEntity.setNxDgssrNxDisGoodsId(stockEntity.getNxDgssNxDisGoodsId());
            reduceEntity.setNxDgssrGoodsInventoryType(0);
            reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
            reduceEntity.setNxDgssrFullTime(formatFullTime());
            reduceEntity.setNxDgssrNxStockId(stockEntity.getNxDistributerGoodsShelfStockId());
            reduceEntity.setNxDgssrType(0);// 0=销售扣减
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
            ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
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

    @RequestMapping(value = "/phoneGetToFillDepOrdersSunla", method = RequestMethod.POST)
    @ResponseBody
    public R phoneGetToFillDepOrdersSunla(Integer depFatherId,Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("dayuStatus", -1);
        map.put("purType", -1);
        map.put("depFatherId", depFatherId);
        System.out.println("abncnncnnnc" + map);
        List<NxDepartmentOrdersEntity> ordersEntitiesFirst = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
        map.put("purType", -2);
        List<NxDepartmentOrdersEntity> ordersEntitiesTwo = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
        Map<String, Object> mapR = new HashMap<>();

        mapR.put("oneArr", ordersEntitiesFirst);
        mapR.put("twoArr", ordersEntitiesTwo );

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

            mapR.put("subAmount", entities.size());
            mapR.put("arr", mapList);

            return R.ok().put("data", mapR);
        } else {

            return R.ok().put("data", mapR);
        }
    }

    /**
     * 获取需要填写数量和价格的订单（带Kg转换）
     * 借鉴 phoneGetToFillDepOrders 接口，添加Kg单价转换功能
     * 如果商品规格是"斤"，则将单价转换为Kg单价（1斤=0.5Kg，所以1Kg=2斤，Kg单价=斤单价×2）
     */
    @RequestMapping(value = "/phoneGetToFillDepOrdersWithKg", method = RequestMethod.POST)
    @ResponseBody
    public R phoneGetToFillDepOrdersWithKg(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        System.out.println("phoneGetToFillDepOrdersWithKg - abncnncnnnc" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
        // 转换订单的Kg单价和重量（直接修改 nxDoPrice 和 nxDoWeight）
        // 只转换商品规格是"斤"的订单，返回被转换的订单列表
        List<NxDepartmentOrdersEntity> convertedOrders = convertOrdersPriceToKg(ordersEntities);
        // 批量保存转换后的订单到数据库（只保存被转换的订单）
        saveOrders(convertedOrders);

        map.put("hasPrice", 1);
        int hasPriceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("hasPrice", null);
        map.put("hasWeight", 1);
        int hasWeightCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        map.put("status", null);
        map.put("equalStatus", 2);
        map.put("dayuPurStatus", 3);
        System.out.println("phoneGetToFillDepOrdersWithKg - elqlqlqlql" + map);
        int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        System.out.println("phoneGetToFillDepOrdersWithKg - disnamme" + nxDistributerEntity.getNxDistributerName());
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        System.out.println("phoneGetToFillDepOrdersWithKg - depdiidid" + entities.size());
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
                // 转换订单的Kg单价和重量（直接修改 nxDoPrice 和 nxDoWeight）
                // 只转换商品规格是"斤"的订单，返回被转换的订单列表
                List<NxDepartmentOrdersEntity> convertedDepOrders = convertOrdersPriceToKg(depOrders);
                // 批量保存转换后的订单到数据库（只保存被转换的订单）
                saveOrders(convertedDepOrders);
                mapDep.put("depOrders", depOrders);
                map1.put("subtotal", 0);
                System.out.println("phoneGetToFillDepOrdersWithKg - map111aaa" + map1);
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


    

    /**
     * 将订单的单价和重量从公斤转换为斤
     * 直接修改 nxDoPrice 和 nxDoWeight 字段
     * @param depFatherId 部门父ID
     * @param gbDepFatherId GB部门父ID
     * @param resFatherId 餐厅父ID
     * @param disId 批发商ID
     * @return
     */
    @RequestMapping(value = "/phoneGetToFillDepOrdersWithJin", method = RequestMethod.POST)
    @ResponseBody
    public R phoneGetToFillDepOrdersWithJin(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        System.out.println("phoneGetToFillDepOrdersWithJin - abncnncnnnc" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
        // 转换订单的斤单价和重量（直接修改 nxDoPrice 和 nxDoWeight）
        // 只转换商品规格是"Kg"的订单，返回被转换的订单列表
        List<NxDepartmentOrdersEntity> convertedOrders = convertOrdersPriceToJin(ordersEntities);
        // 批量保存转换后的订单到数据库（只保存被转换的订单）
        saveOrders(convertedOrders);

        map.put("hasPrice", 1);
        int hasPriceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("hasPrice", null);
        map.put("hasWeight", 1);
        int hasWeightCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        map.put("status", null);
        map.put("equalStatus", 2);
        map.put("dayuPurStatus", 3);
        System.out.println("phoneGetToFillDepOrdersWithJin - elqlqlqlql" + map);
        int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        System.out.println("phoneGetToFillDepOrdersWithJin - disnamme" + nxDistributerEntity.getNxDistributerName());
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        System.out.println("phoneGetToFillDepOrdersWithJin - depdiidid" + entities.size());
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
                // 转换订单的斤单价和重量（直接修改 nxDoPrice 和 nxDoWeight）
                // 只转换商品规格是"Kg"的订单，返回被转换的订单列表
                List<NxDepartmentOrdersEntity> convertedDepOrders = convertOrdersPriceToJin(depOrders);
                // 批量保存转换后的订单到数据库（只保存被转换的订单）
                saveOrders(convertedDepOrders);
                mapDep.put("depOrders", depOrders);
                map1.put("subtotal", 0);
                System.out.println("phoneGetToFillDepOrdersWithJin - map111aaa" + map1);
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
            mapS.put("disId", disId);
            mapS.put("depFatherId", depFatherId);
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
        System.out.println("finfidiidididid" + map);
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

    /**
     * 根据depFatherId查询订单列表（包含溯源报告信息）
     * 如果是货架商品，从库存批次关联溯源报告；如果不是货架商品，从采购商品关联溯源报告
     */
    @RequestMapping(value = "/getOrderPageWithTraceReport", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPageWithTraceReport(Integer depFatherId) {
        System.out.println("[getOrderPageWithTraceReport] 开始查询订单溯源报告，depFatherId: " + depFatherId);
        Map<String, Object> mapResult = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);

        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
        List<Map<String, Object>> list = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                map.put("depId", departmentEntity.getNxDepartmentId());
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepOrdersWithTraceReport(map);
                System.out.println("[getOrderPageWithTraceReport] 部门ID: " + departmentEntity.getNxDepartmentId() + 
                    ", 部门名称: " + departmentEntity.getNxDepartmentName() + ", 订单数量: " + ordersEntities.size());
                
                // 打印每个订单的溯源报告信息
                for (NxDepartmentOrdersEntity order : ordersEntities) {
                    NxTraceReportEntity traceReport = order.getNxTraceReportEntity();
                    NxDistributerGoodsShelfStockEntity shelfStock = order.getShelfStockEntity();
                    // 查询商品信息，检查商品表中的溯源报告ID
                    if (order.getNxDoDisGoodsId() != null) {
                        NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(order.getNxDoDisGoodsId());
                        System.out.println("[getOrderPageWithTraceReport] 订单ID: " + order.getNxDepartmentOrdersId() + 
                            ", 商品ID: " + order.getNxDoDisGoodsId() + 
                            ", 商品名称: " + order.getNxDoGoodsName() +
                            ", 商品表中的溯源报告ID: " + (goods != null ? goods.getNxDgTraceReportId() : "null") +
                            ", 订单对象中的溯源报告: " + (traceReport != null ? "存在(ID:" + traceReport.getNxTraceReportId() + ")" : "null"));
                    } else {
                        System.out.println("[getOrderPageWithTraceReport] 订单ID: " + order.getNxDepartmentOrdersId() + 
                            ", 商品名称: " + order.getNxDoGoodsName() +
                            ", 订单对象中的溯源报告: " + (traceReport != null ? "存在(ID:" + traceReport.getNxTraceReportId() + ")" : "null"));
                    }
                    if (traceReport != null) {
                        System.out.println("[getOrderPageWithTraceReport]   溯源报告详情 - 供应商: " + traceReport.getNxTrSupplierName() + 
                            ", 采购日期: " + traceReport.getNxTrPurchaseDate());
                    }
                }
//                for (NxDepartmentOrdersEntity order : ordersEntities) {
//                    NxTraceReportEntity traceReport = order.getNxTraceReportEntity();
//                    NxDistributerGoodsShelfStockEntity shelfStock = order.getShelfStockEntity();
//                    System.out.println("[getOrderPageWithTraceReport] 订单ID: " + order.getNxDepartmentOrdersId() +
//                        ", 商品ID: " + order.getNxDoDisGoodsId() +
//                        ", 商品名称: " + order.getNxDoGoodsName() +
//                        ", 采购商品ID: " + order.getNxDoPurchaseGoodsId() +
//                        ", 溯源报告: " + (traceReport != null ? "存在(ID:" + traceReport.getNxTraceReportId() + ")" : "null") +
//                        ", 货架库存: " + (shelfStock != null ? "存在(ID:" + shelfStock.getNxDistributerGoodsShelfStockId() + ",剩余重量:" + shelfStock.getNxDgssRestWeight() + ")" : "null"));
//                    if (traceReport != null) {
//                        System.out.println("[getOrderPageWithTraceReport]   溯源报告详情 - 供应商: " + traceReport.getNxTrSupplierName() +
//                            ", 采购日期: " + traceReport.getNxTrPurchaseDate() +
//                            ", 有效期: " + traceReport.getNxTrValidStartDate() + " ~ " + traceReport.getNxTrValidEndDate());
//                    }
//                    if (shelfStock != null && shelfStock.getNxDgssTraceReportId() != null) {
//                        System.out.println("[getOrderPageWithTraceReport]   货架库存溯源报告ID: " + shelfStock.getNxDgssTraceReportId());
//                    }
//                }
                
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", departmentEntity.getNxDepartmentId());
                mapDep.put("depName", departmentEntity.getNxDepartmentName());
                mapDep.put("list", ordersEntities);
                list.add(mapDep);
            }
            mapResult.put("arr", list);
            mapResult.put("depHasSubs", departmentEntities.size());
        } else {
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepOrdersWithTraceReport(map);
            System.out.println("[getOrderPageWithTraceReport] 无子部门，订单数量: " + ordersEntities.size());
            
            // 打印每个订单的溯源报告信息
            for (NxDepartmentOrdersEntity order : ordersEntities) {
                NxTraceReportEntity traceReport = order.getNxTraceReportEntity();
                NxDistributerGoodsShelfStockEntity shelfStock = order.getShelfStockEntity();
                // 查询商品信息，检查商品表中的溯源报告ID
                if (order.getNxDoDisGoodsId() != null) {
                    NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(order.getNxDoDisGoodsId());
                    System.out.println("[getOrderPageWithTraceReport] 订单ID: " + order.getNxDepartmentOrdersId() + 
                        ", 商品ID: " + order.getNxDoDisGoodsId() + 
                        ", 商品名称: " + order.getNxDoGoodsName() +
                        ", 商品表中的溯源报告ID: " + (goods != null ? goods.getNxDgTraceReportId() : "null") +
                        ", 订单对象中的溯源报告: " + (traceReport != null ? "存在(ID:" + traceReport.getNxTraceReportId() + ")" : "null"));
                } else {
                    System.out.println("[getOrderPageWithTraceReport] 订单ID: " + order.getNxDepartmentOrdersId() + 
                        ", 商品名称: " + order.getNxDoGoodsName() +
                        ", 订单对象中的溯源报告: " + (traceReport != null ? "存在(ID:" + traceReport.getNxTraceReportId() + ")" : "null"));
                }
                if (traceReport != null) {
                    System.out.println("[getOrderPageWithTraceReport]   溯源报告详情 - 供应商: " + traceReport.getNxTrSupplierName() + 
                        ", 采购日期: " + traceReport.getNxTrPurchaseDate());
                }
            }
            mapResult.put("arr", ordersEntities);
            mapResult.put("depHasSubs", 0);
        }

        mapResult.put("subDep", departmentEntities.size());
        System.out.println("[getOrderPageWithTraceReport] 查询完成");
        return R.ok().put("data", mapResult);
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


    @RequestMapping(value = "/depGetApplyAiByTimeSunla/{depFatherId}")
    @ResponseBody
    public R depGetApplyAiByTimeSunla(@PathVariable Integer depFatherId) {

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
                map1.put("dayuStatus", -1);
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

            return R.ok().put("data", mapR);

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 3);
            map.put("dayuStatus", -1);
            map.put("depFatherId", depFatherId);
            map.put("isPurType", true);
            System.out.println("abncnncnnncaaaa" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
            mapR.put("arr", ordersEntities);
            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/depSearchTodayOrders", method = RequestMethod.POST)
    @ResponseBody
    public R depSearchTodayOrders(Integer depFatherId, Integer depId, String searchStr) {

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
        map.put("status", 3);

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


    private static final String KEY = "C5HBZ-KEIW2-JXXUJ-COLGS-FQO47-WWFAK";

    // 修复后的代码片段，替换原有的路线优化逻辑
// 在 NxDepartmentOrdersController.java 的 disGetTodayOrderCustomerRoute 方法中
// 修复后的代码片段，替换原有的路线优化逻辑
// 在 NxDepartmentOrdersController.java 的 disGetTodayOrderCustomerRoute 方法中

    @RequestMapping(value = "/disGetTodayOrderCustomerRoute", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTodayOrderCustomerRoute(Integer disId, String fromLat, String fromLng) {
        try {
            //今天订货
            Map<String, Object> map1 = new HashMap<>();
            map1.put("disId", disId);
            map1.put("status", 3);
            List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);
            System.out.println("disGetTodayOrderCustomerRoute 参数: disId=" + disId + ", fromLat=" + fromLat + ", fromLng=" + fromLng);

            if (departmentEntities.size() == 0) {
                return R.error(-1, "没有订单");
            }

            // 过滤有效坐标的客户
            List<NxDepartmentEntity> validCustomers = new ArrayList<>();
            StringBuilder stringBuilder = new StringBuilder();

            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                map1.put("depFatherId", departmentEntity.getNxDepartmentId());
                System.out.println("counddd" + map1);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                departmentEntity.setNxDepartmentOrderTotal(integer);

                String nxDepartmentLat = departmentEntity.getNxDepartmentLat();
                String nxDepartmentLng = departmentEntity.getNxDepartmentLng();

                // 检查坐标是否有效
                if (isValidCoordinate(nxDepartmentLat, nxDepartmentLng)) {
                    validCustomers.add(departmentEntity);
                    String item = nxDepartmentLat + "," + nxDepartmentLng;
                    stringBuilder.append(item + ";");
                } else {
                    System.out.println("跳过无效坐标客户: " + departmentEntity.getNxDepartmentName() +
                            " 坐标: (" + nxDepartmentLat + ", " + nxDepartmentLng + ")");
                }
            }

            System.out.println("有效坐标客户数量: " + validCustomers.size());

            if (validCustomers.isEmpty()) {
                System.out.println("没有有效的客户坐标，返回原始数据");
                return R.ok().put("data", departmentEntities);
            }

            // 移除最后一个分号
            String coordinates = stringBuilder.toString();
            if (coordinates.endsWith(";")) {
                coordinates = coordinates.substring(0, coordinates.length() - 1);
            }

            String from = fromLat + "," + fromLng;
            String urlString = "http://apis.map.qq.com/ws/distance/v1/optimal_order?mode=driving&from="
                    + from + "&to=" + coordinates + "&key=" + KEY;

            System.out.println("腾讯地图API请求URL: " + urlString);

            // 发送请求，返回Json字符串
            String result = "";
            try {
                URL url = new URL(urlString);
                System.out.println(url);
                System.out.println("----");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                // 获取地址解析结果
                System.out.println(in);
                while ((line = in.readLine()) != null) {
                    result += line + "\n";
                }
                in.close();

                System.out.println("腾讯地图API响应: " + result);

                // 转JSON格式
                JSONObject jsonObject = JSONObject.parseObject(result);
                if (jsonObject == null) {
                    System.err.println("API响应解析失败，返回有效客户数据");
                    return R.ok().put("data", validCustomers);
                }

                String optimal_order = jsonObject.getString("result");
                if (optimal_order == null) {
                    System.err.println("API响应中没有result字段，返回有效客户数据");
                    return R.ok().put("data", validCustomers);
                }

                System.out.println(optimal_order);
                System.out.println("optimal_orderoptimal_order");

                //获取排序
                JSONObject optimalOrderJson = JSONObject.parseObject(optimal_order);
                if (optimalOrderJson == null) {
                    System.err.println("optimal_order解析失败，返回有效客户数据");
                    return R.ok().put("data", validCustomers);
                }

                String order = optimalOrderJson.getString("optimal_order");
                if (order == null || order.isEmpty()) {
                    System.err.println("optimal_order字段为空，返回有效客户数据");
                    return R.ok().put("data", validCustomers);
                }

                System.out.println(order);
                String substring2 = order.substring(0);
                String substring3 = substring2.substring(1, substring2.length() - 1);
                String[] split = substring3.split(",");

                List<NxDepartmentEntity> treeSet = new ArrayList<>();

                String elements = optimalOrderJson.getString("elements");
                if (elements == null) {
                    System.err.println("elements字段为空，返回有效客户数据");
                    return R.ok().put("data", validCustomers);
                }

                List<NxDepartmentEntity> list = new ArrayList<>();
                try {
                    list = JSONObject.parseArray(elements, NxDepartmentEntity.class);
                } catch (Exception e) {
                    System.err.println("解析elements失败: " + e.getMessage());
                    return R.ok().put("data", validCustomers);
                }

                System.out.println(list);
                System.out.println("list");

                // 使用validCustomers而不是departmentEntities
                System.out.println("开始处理排序结果，split长度: " + split.length);
                for (int i = 0; i < split.length; i++) {
                    try {
                        Integer integer = Integer.valueOf(split[i]);
                        System.out.println("处理第" + (i + 1) + "个索引: " + integer);

                        // 确保索引在有效范围内
                        if (integer > 0 && integer <= validCustomers.size()) {
                            NxDepartmentEntity nxRestrauntEntity = validCustomers.get(integer - 1);
                            System.out.println("获取客户: " + nxRestrauntEntity.getNxDepartmentName());

                            if (i < list.size()) {
                                NxDepartmentEntity listEnitity = list.get(i);
                                String distance = listEnitity.getDistance();
                                String duration = listEnitity.getDuration();
                                nxRestrauntEntity.setDistance(distance);
                                nxRestrauntEntity.setDuration(duration);
                                System.out.println("设置距离: " + distance + ", 时间: " + duration);
                            }
                            treeSet.add(nxRestrauntEntity);
                        } else {
                            System.err.println("索引超出范围: " + integer + ", 有效客户数量: " + validCustomers.size());
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("解析索引失败: " + split[i] + ", 错误: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("处理第" + (i + 1) + "个索引时出错: " + e.getMessage());
                    }
                }

                System.out.println("最终返回客户数量: " + treeSet.size());

                return R.ok().put("data", treeSet);

            } catch (Exception e) {
                System.err.println("调用腾讯地图API失败: " + e.getMessage());
                e.printStackTrace();
                // API调用失败时返回有效客户数据
                return R.ok().put("data", validCustomers);
            }

        } catch (Exception e) {
            System.err.println("disGetTodayOrderCustomerRoute 异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取客户数据失败: " + e.getMessage());
        }
    }


    @RequestMapping(value = "/disGetDriversOptimalRoute", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDriversOptimalRoute(@RequestBody Map<String, Object> paramMap) {
        System.out.println("disGetDriversOptimalRoute" + paramMap);
        try {
            List<Map<String, Object>> driverRoutes = (List<Map<String, Object>>) paramMap.get("driverRoutes");
            List<Map<String, Object>> resultList = new ArrayList<>();

            for (Map<String, Object> driverRoute : driverRoutes) {
                Integer driverId = (Integer) driverRoute.get("driverId");
                String fromLat = (String) driverRoute.get("fromLat");
                String fromLng = (String) driverRoute.get("fromLng");

                List<Map<String, Object>> customers = (List<Map<String, Object>>) driverRoute.get("customers");
                if (customers == null || customers.isEmpty()) continue;

                // 拼接 to 坐标
                StringBuilder sb = new StringBuilder();
                for (Map<String, Object> customer : customers) {
                    String lat = (String) customer.get("lat");
                    String lng = (String) customer.get("lng");
                    sb.append(lat).append(",").append(lng).append(";");
                }
                String coordinates = sb.toString();
                if (coordinates.endsWith(";")) coordinates = coordinates.substring(0, coordinates.length() - 1);

                String from = fromLat + "," + fromLng;
                String urlString = "http://apis.map.qq.com/ws/distance/v1/optimal_order?mode=driving&from="
                        + from + "&to=" + coordinates + "&key=" + KEY;

                System.out.println("apiaiaiaiiai" + urlString);
                // 请求腾讯API
                String result = "";
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String line;
                    while ((line = in.readLine()) != null) {
                        result += line + "\n";
                    }
                    in.close();

                    // 解析API结果
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    String optimal_order = jsonObject.getString("result");
                    JSONObject optimalOrderJson = JSONObject.parseObject(optimal_order);

                    // 最优顺序索引
                    String order = optimalOrderJson.getString("optimal_order");
                    String substring3 = order.substring(1, order.length() - 1);
                    String[] split = substring3.split(",");

                    // 每一段距离/时长
                    String elements = optimalOrderJson.getString("elements");
                    List<NxDepartmentEntity> sortedCustomers = new ArrayList<>();
                    List<NxDepartmentEntity> elementsList = JSONObject.parseArray(elements, NxDepartmentEntity.class);

                    for (int i = 0; i < split.length; i++) {
                        int idx = Integer.parseInt(split[i]) - 1;
                        if (idx >= 0 && idx < customers.size()) {
                            Map<String, Object> customerMap = customers.get(idx);
                            NxDepartmentEntity nxEntity = new NxDepartmentEntity();
                            // 复制你关心的字段
                            nxEntity.setNxDepartmentId((Integer) customerMap.get("customerId"));
                            nxEntity.setNxDepartmentLat((String) customerMap.get("lat"));
                            nxEntity.setNxDepartmentLng((String) customerMap.get("lng"));
                            if (i < elementsList.size()) {
                                NxDepartmentEntity ele = elementsList.get(i);
                                nxEntity.setDistance(ele.getDistance());
                                nxEntity.setDuration(ele.getDuration());
                            }
                            sortedCustomers.add(nxEntity);
                        }
                    }

                    Map<String, Object> driverResult = new HashMap<>();
                    driverResult.put("driverId", driverId);
                    driverResult.put("sortedCustomers", sortedCustomers);
                    resultList.add(driverResult);

                } catch (Exception e) {
                    // 一个司机失败也能返回别的司机的结果
                    e.printStackTrace();
                }

                // 腾讯API限流，建议加sleep
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }

            return R.ok().put("data", resultList);

        } catch (Exception e) {
            e.printStackTrace();
            return R.error("系统异常: " + e.getMessage());
        }
    }


    @RequestMapping(value = "/disGetCustomerDistanceMatrix", method = RequestMethod.POST)
    @ResponseBody
    public R disGetCustomerDistanceMatrix(Integer disId, String fromLat, String fromLng) {
        try {
            // 今天订货
            Map<String, Object> map1 = new HashMap<>();
            map1.put("disId", disId);
            map1.put("status", 3);
            List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);

            if (departmentEntities.isEmpty()) {
                return R.error(-1, "没有订单");
            }

            // 过滤有效坐标客户
            List<NxDepartmentEntity> validCustomers = new ArrayList<>();
            StringBuilder toBuilder = new StringBuilder();

            System.out.println("departmentEntitiesdepartmentEntities" + departmentEntities.size());
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                String lat = departmentEntity.getNxDepartmentLat();
                String lng = departmentEntity.getNxDepartmentLng();
                if (isValidCoordinate(lat, lng)) {
                    validCustomers.add(departmentEntity);
                    toBuilder.append(lat).append(",").append(lng).append(";");
                }
            }

            if (validCustomers.isEmpty()) {
                return R.ok().put("data", departmentEntities);
            }

            // 移除最后一个分号
            String to = toBuilder.toString();
            if (to.endsWith(";")) {
                to = to.substring(0, to.length() - 1);
            }

            // 腾讯 distance matrix接口（from只能1个，to多个）
            String from = fromLat + "," + fromLng;
            String urlString = "https://apis.map.qq.com/ws/distance/v1/matrix?mode=driving&from=" + from
                    + "&to=" + to + "&key=" + KEY;

            System.out.println("请求腾讯Matrix接口URL: " + urlString);

            String result = "";
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                while ((line = in.readLine()) != null) {
                    result += line + "\n";
                }
                in.close();
            } catch (Exception e) {
                System.err.println("腾讯matrix接口请求失败: " + e.getMessage());
                return R.ok().put("data", validCustomers);
            }

            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject == null || jsonObject.getInteger("status") != 0) {
                System.err.println("腾讯matrix接口响应解析失败: " + result);
                return R.ok().put("data", validCustomers);
            }

            // 解析距离/时长
            JSONObject resObj = jsonObject.getJSONObject("result");
            JSONArray rows = resObj.getJSONArray("rows");
            if (rows == null || rows.size() == 0) {
                System.err.println("腾讯matrix rows为空: " + result);
                return R.ok().put("data", validCustomers);
            }

            // 只取第一行（单起点对多终点）
            JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");

            for (int i = 0; i < validCustomers.size(); i++) {
                NxDepartmentEntity customer = validCustomers.get(i);
                if (i < elements.size()) {
                    JSONObject element = elements.getJSONObject(i);
                    System.out.println("sttieonenoeneoneoeneoenoneo" + element.getString("duration") + element.getString("distance"));
                    customer.setDistance(element.getString("distance")); // 单位: 米
                    customer.setDuration(element.getString("duration")); // 单位: 秒
                }
            }

            return R.ok().put("data", validCustomers);

        } catch (Exception e) {
            System.err.println("disGetCustomerDistanceMatrix 异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取客户距离数据失败: " + e.getMessage());
        }
    }

    private boolean isValidCoordinate(String lat, String lng) {
        if (lat == null || lng == null || lat.trim().isEmpty() || lng.trim().isEmpty()) {
            return false;
        }

        try {
            double latValue = Double.parseDouble(lat);
            double lngValue = Double.parseDouble(lng);

            // 检查坐标范围（中国大致范围）
            return latValue >= 18.0 && latValue <= 54.0 &&
                    lngValue >= 73.0 && lngValue <= 135.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

//    @RequestMapping(value = "/disGetTodayOrderCustomerRoute", method = RequestMethod.POST)
//    @ResponseBody
//    public R disGetTodayOrderCustomerRoute(Integer disId, String fromLat, String fromLng) {
//        //今天订货
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("disId", disId);
//        map1.put("status", 3);
//        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);
//        System.out.println("disGetTodayOrderCustomerRoutedisGetTodayOrderCustomerRoute" + map1);
//        StringBuilder stringBuilder = new StringBuilder();
//        List<NxDepartmentEntity> validCustomers = new ArrayList<>();
//        if (departmentEntities.size() > 0) {
//
//            for (NxDepartmentEntity departmentEntity : departmentEntities) {
//
//                String nxDepartmentLat = departmentEntity.getNxDepartmentLat();
//                String nxDepartmentLng = departmentEntity.getNxDepartmentLng();
//
//                // 检查坐标是否有效
//                if (isValidCoordinate(nxDepartmentLat, nxDepartmentLng)) {
//                    validCustomers.add(departmentEntity);
//                    String item = nxDepartmentLat + "," + nxDepartmentLng;
//                    stringBuilder.append(item + ";");
//                } else {
//                    System.out.println("跳过无效坐标客户: " + departmentEntity.getNxDepartmentName() +
//                            " 坐标: (" + nxDepartmentLat + ", " + nxDepartmentLng + ")");
//                }
//                String nxRestrauntLat = departmentEntity.getNxDepartmentLat();
//                String nxRestrauntLng = departmentEntity.getNxDepartmentLng();
//                String item = nxRestrauntLat + "," + nxRestrauntLng;
//                stringBuilder.append(item + ";");
//            }
//            String substring = stringBuilder.substring(0, stringBuilder.length() - 1);
//            String from = fromLat + "," + fromLng;
//            String urlString = "http://apis.map.qq.com/ws/distance/v1/optimal_order?mode=driving&from="
//                    + from + "&to=" + substring + "&key=" + KEY;
//            // 发送请求，返回Json字符串
//            String result = "";
//            try {
//                URL url = new URL(urlString);
//                System.out.println(url);
//                System.out.println("----");
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setDoOutput(true);
//                // 腾讯地图使用GET
//                conn.setRequestMethod("GET");
//                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//                String line;
//                // 获取地址解析结果
//                System.out.println(in);
//                while ((line = in.readLine()) != null) {
//                    result += line + "\n";
//                }
//                in.close();
//            } catch (Exception e) {
//                e.getMessage();
//            }
//
//
////		// 转JSON格式
//            JSONObject jsonObject = JSONObject.parseObject(result);
//            String optimal_order = (String) jsonObject.getString("result");
//            System.out.println(optimal_order);
//            System.out.println("optimal_orderoptimal_order");
//
//            //获取排序
//            String order = JSONObject.parseObject(optimal_order).getString("optimal_order");
//            System.out.println(order);
//            String substring2 = order.substring(0);
//            String substring3 = substring2.substring(1, substring2.length() - 1);
//            String[] split = substring3.split(",");
//
//            List<NxDepartmentEntity> treeSet = new ArrayList<>();
//
//            String elements = JSONObject.parseObject(optimal_order).getString("elements");
//            List<NxDepartmentEntity> list = new ArrayList<>();
//            list = JSONObject.parseArray(elements, NxDepartmentEntity.class);
//
//            System.out.println(list);
//            System.out.println("list");
//
//            for (int i = 0; i < split.length; i++) {
//                Integer integer = Integer.valueOf(split[i]);
//
//                NxDepartmentEntity nxRestrauntEntity = departmentEntities.get(integer - 1);
//                NxDepartmentEntity listEnitity = list.get(i);
//                String distance = listEnitity.getDistance();
//                String duration = listEnitity.getDuration();
//                nxRestrauntEntity.setDistance(distance);
//                nxRestrauntEntity.setDuration(duration);
//                treeSet.add(nxRestrauntEntity);
//            }
//
//            return R.ok().put("data", treeSet);
//        }else{
//            return R.error(-1,"没有订单");
//        }
//
//
//    }
//
//
//    private boolean isValidCoordinate(String lat, String lng) {
//        if (lat == null || lng == null || lat.trim().isEmpty() || lng.trim().isEmpty()) {
//            return false;
//        }
//
//        try {
//            double latValue = Double.parseDouble(lat);
//            double lngValue = Double.parseDouble(lng);
//
//            // 检查坐标范围（中国大致范围）
//            return latValue >= 18.0 && latValue <= 54.0 &&
//                    lngValue >= 73.0 && lngValue <= 135.0;
//        } catch (NumberFormatException e) {
//            return false;
//        }
//    }


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
            System.out.println("depsisiisisisiiis" + departmentEntities.size());
            // 提取所有部门ID，过滤掉null值
            List<Integer> depFatherIds = departmentEntities.stream()
                    .filter(dept -> dept != null)
                    .map(dept -> dept.getNxDepartmentId())
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            // 批量查询所有部门的统计数据
            Map<Integer, Map<String, Object>> statsMap = nxDepartmentOrdersService.batchQueryDepartmentOrderStats(depFatherIds);

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


        System.out.println("gbDisArrapp" + mapData);


//        //GbData
//        Map<String, Object> mapPB = new HashMap<>();
//        mapPB.put("nxDisId", disId);
//        mapPB.put("status", 3);
//        System.out.println("enennenenenne" + mapPB);
//        List<GbDistributerPurchaseBatchEntity> entities = gbDistributerPurchaseBatchService.queryDisPurchaseBatch(mapPB);
//
//        if (entities.size() > 0) {
//
//            for (GbDistributerPurchaseBatchEntity batchEntity : entities) {
//
//                Map<String, Object> mapDis = new HashMap<>();
//                mapDis.put("nxDisId", disId);
//                mapDis.put("disId", batchEntity.getGbDpbDistributerId());
//                mapDis.put("status", 3);
//                System.out.println("whwhwhwhhwhwhwhpururur000" + mapDis);
//                Integer goodsCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapDis);
//                mapDis.put("hasPrice", -1);
//                System.out.println("whwhwhwhhwhwhwhpururur111" + mapDis);
//                Integer hasNotPrice = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapDis);
//                mapDis.put("hasPrice", 1);
//                System.out.println("whwhwhwhhwhwhwhpururur222" + mapDis);
//                Integer hasPriceCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapDis);
//
//            }
//
//            mapData.put("gbDisArr", entities);
//
//        } else {
//            mapData.put("gbDisArr", new ArrayList<>());
//        }


        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("purStatus", 4);
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
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("equalStatus", -1);
        System.out.println("rututtntnmaappa" + map);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryReturnBill(map);
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
        returnData.put("returnList", billEntityList);
        returnData.put("unDoTotal", unDoTotal);
        returnData.put("linshiTotal", wxCountAuto1);
        return R.ok().put("data", returnData);

    }
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



//    @RequestMapping(value = "/disGetTodayOrderCustomer/{disId}")
//    @ResponseBody
//    public R disGetTodayOrderCustomer(@PathVariable Integer disId) {
//        Map<String, Object> returnData = new HashMap<>();
//        //今天订货
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("disId", disId);
//        map1.put("status", 3);
//        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);
//        System.out.println("mapapapapapddiidididi" + map1);
//        List<GbDistributerEntity> distributerEntitiesAA = nxDepartmentOrdersService.queryOrderGbDistributerList(map1);
//        System.out.println("gbbgbgbgbbgbbgbgb" + distributerEntitiesAA.size());
//        Map<String, Object> mapData = new HashMap<>();
//        List<Map<String, Object>> resultNx = new ArrayList<>();
//        if (departmentEntities.size() > 0) {
//            Integer hasPrice = 0;
//            Integer hasWeight = 0;
//            Integer unDo = 0;
//
//            System.out.println("depsisiisisisiiis" + departmentEntities.size());
//            for (NxDepartmentEntity departmentEntity : departmentEntities) {
//                Map<String, Object> map7 = new HashMap<>();
//                map7.put("depFatherId", departmentEntity.getNxDepartmentId());
//                map7.put("status", -1);
//                unDo = nxDepartmentOrdersService.queryDepOrdersAcount(map7);
//
//                map7.put("status", 3);
//                map7.put("hasPrice", 1);
//                System.out.println("kkkkkkkkkkkkkk" + map7);
//                hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(map7);
//
//                // weigth
//                Map<String, Object> map8 = new HashMap<>();
//                map8.put("depFatherId", departmentEntity.getNxDepartmentId());
//                map8.put("status", 3);
//                map8.put("dayuPurStatus", 3);
//                hasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(map8);
//
//                Map<String, Object> map2 = new HashMap<>();
//                map2.put("depFatherId", departmentEntity.getNxDepartmentId());
//                map2.put("status", 3);
//                Integer totalCount = nxDepartmentOrdersService.queryDepOrdersAcount(map2);
//                map2.put("status", null);
//                map2.put("dayuPurStatus", 3);
//                map2.put("equalStatus", 2);
//                map2.put("subtotal", 0);
//                map2.put("hasPrice", 1);
//                System.out.println("fiisisiisisimap" + map2);
//                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map2);
//                Double total = 0.0;
//                if (twoTotal > 0) {
//                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map2);
//                }
//
//
//
//                Map<String, Object> mapDep = new HashMap<>();
//                mapDep.put("dep", departmentEntity);
//                mapDep.put("totalCount", totalCount);
//                mapDep.put("finishCount", twoTotal);
//                mapDep.put("hasPrice", hasPrice);
//                mapDep.put("hasWeight", hasWeight);
//                mapDep.put("unDo", unDo);
//
//
//
//                mapDep.put("twoSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                resultNx.add(mapDep);
//
//            }
//            mapData.put("nxDep", resultNx);
//        } else {
//            mapData.put("nxDep", new ArrayList<>());
//        }
//
//        List<Map<String, Object>> gbList = new ArrayList<>();
//
//        if (distributerEntitiesAA.size() > 0) {
//
//            for (GbDistributerEntity gbDis : distributerEntitiesAA) {
//                if (gbDis.getGbDistributerBusinessType() > 0) {
//                    List<Map<String, Object>> gbDisDepArrMap = new ArrayList<>();
//
//                    Map<String, Object> mapDis = new HashMap<>();
//                    mapDis.put("dis", gbDis);
//                    mapDis.put("gbDisId", gbDis.getGbDistributerId());
//                    Map<String, Object> mapOrderDep = new HashMap<>();
//                    mapOrderDep.put("gbDisId", gbDis.getGbDistributerId());
//                    mapOrderDep.put("disId", disId);
//                    System.out.println("gbdidisiisisismap==" + mapOrderDep);
//                    List<GbDepartmentEntity> gbpartmentEntities = nxDepartmentOrdersService.queryOrderGbDepartmentList(mapOrderDep);
//
//                    Integer newOrder = 0;
//                    Integer hasNotWeight = 0;
//                    Integer jinhuoOrder = 0;
//                    Integer jinhuoHasWeight = 0;
//                    Integer jinhuoFinished = 0;
//                    Integer chukuOrder = 0;
//                    Integer chukuHasWeight = 0;
//
//                    Integer hasPrice = 0;
//                    Integer hasNotPrice = 0;
//
//                    System.out.println("gbeenesisi" + gbpartmentEntities.size());
//                    for (GbDepartmentEntity gbDepartmentEntity : gbpartmentEntities) {
//                        //new
//                        Map<String, Object> mapOrder = new HashMap<>();
//                        mapOrder.put("gbDepId", gbDepartmentEntity.getGbDepartmentId());
//                        mapOrder.put("status", 3);
////                    mapOrder.put("purGoodsId", 0);
//                        System.out.println("gbnNewworororooror" + mapOrder);
//                        newOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
//
//                        if (newOrder > 0) {
//                            //采购进货-jinhuo
//                            mapOrder.put("purGoodsId", 1);
//                            jinhuoOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
//                            //出库 -chuku
//                            mapOrder.put("purGoodsId", -1);
//                            chukuOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
//
//                            // --weight
//                            Map<String, Object> mapWeight = new HashMap<>();
//                            mapWeight.put("gbDepId", gbDepartmentEntity.getGbDepartmentId());
//                            mapWeight.put("hasWeight", -1);
//                            mapWeight.put("status", 3);
//
//                            System.out.println("jinhuowhawwwrrrrrrggggg" + mapWeight);
//                            hasNotWeight = nxDepartmentOrdersService.queryDepOrdersAcount(mapWeight);
//
//                            mapWeight.put("hasWeight", 1);
//                            mapWeight.put("purGoodsId", 1);
//                            jinhuoHasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(mapWeight);
//                            mapWeight.put("purGoodsId", -1);
//                            chukuHasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(mapWeight);
//
//                            // price
//                            Map<String, Object> map7 = new HashMap<>();
//                            map7.put("gbDepId", gbDepartmentEntity.getGbDepartmentId());
//                            map7.put("status", 3);
//                            map7.put("hasPrice", 1);
//                            hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(map7);
//                            map7.put("hasPrice", -1);
//                            hasNotPrice = nxDepartmentOrdersService.queryDepOrdersAcount(map7);
//
//                            Map<String, Object> map2 = new HashMap<>();
//                            map2.put("gbDepId", gbDepartmentEntity.getGbDepartmentId());
//                            map2.put("equalStatus", 2);
//                            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map2);
//                            Double total = 0.0;
//                            if (twoTotal > 0) {
//                                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map2);
//                            }
//
//
//                            Map<String, Object> mapDep = new HashMap<>();
//                            mapDep.put("gbDep", gbDepartmentEntity);
//                            mapDep.put("newOrder", newOrder);
//                            mapDep.put("hasNotWeight", hasNotWeight);
//                            mapDep.put("jinhuoOrder", jinhuoOrder);
//                            mapDep.put("jinhuoHasWeight", jinhuoHasWeight);
//                            mapDep.put("jinhuoFinished", jinhuoFinished);
//
//                            mapDep.put("chukuOrder", chukuOrder);
//                            mapDep.put("chukuHasWeight", chukuHasWeight);
//                            mapDep.put("chukuFinished", jinhuoFinished);
//
//                            mapDep.put("hasPrice", hasPrice);
//                            mapDep.put("hasNotPrice", hasNotPrice);
//
//                            mapDep.put("twoSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                            gbDisDepArrMap.add(mapDep);
//
//                        }
//
//                    }
//
//                    mapDis.put("arr", gbDisDepArrMap);
//                    gbList.add(mapDis);
//                }
//            }
//
//            mapData.put("gbDisArrApp", gbList);
//
//        } else {
//            mapData.put("gbDisArrApp", new ArrayList<>());
//        }
//
//
//        System.out.println("gbDisArrapp" + mapData);
//
//
//        //GbData
//        Map<String, Object> mapPB = new HashMap<>();
//        mapPB.put("nxDisId", disId);
//        mapPB.put("status", 3);
//        System.out.println("enennenenenne" + mapPB);
//        List<GbDistributerPurchaseBatchEntity> entities = gbDistributerPurchaseBatchService.queryDisPurchaseBatch(mapPB);
//
//        if (entities.size() > 0) {
//
//            for (GbDistributerPurchaseBatchEntity batchEntity : entities) {
//
//                Map<String, Object> mapDis = new HashMap<>();
//                mapDis.put("nxDisId", disId);
//                mapDis.put("disId", batchEntity.getGbDpbDistributerId());
//                mapDis.put("status", 3);
//                System.out.println("whwhwhwhhwhwhwhpururur000" + mapDis);
//                Integer goodsCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapDis);
//                mapDis.put("hasPrice", -1);
//                System.out.println("whwhwhwhhwhwhwhpururur111" + mapDis);
//                Integer hasNotPrice = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapDis);
//                mapDis.put("hasPrice", 1);
//                System.out.println("whwhwhwhhwhwhwhpururur222" + mapDis);
//                Integer hasPriceCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapDis);
//
//            }
//
//            mapData.put("gbDisArr", entities);
//
//        } else {
//            mapData.put("gbDisArr", new ArrayList<>());
//        }
//
//        Map<String, Object> map3 = new HashMap<>();
//        map3.put("disId", disId);
//        map3.put("status", 3);
//        map3.put("purStatus", 4);
//        Integer preOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
//        Integer buyOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
//        Map<String, Object> map3Ok = new HashMap<>();
//        map3Ok.put("disId", disId);
//        map3Ok.put("equalStatus", 2);
//        Integer buyOrdersOk = nxDepartmentOrdersService.queryDepOrdersAcount(map3Ok);
//        returnData.put("buyOrders", buyOrders);
//        returnData.put("buyOrdersOk", buyOrdersOk);
//
//        Map<String, Object> map111 = new HashMap<>();
//        map111.put("disId", disId);
//        map111.put("status", 3);
//        // 出库
//        map111.put("goodsType", -1);
//        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
//
//        map111.put("goodsType", 1);
//        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
//        map111.put("goodsType", 2);
//        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
//        int wxCountPur = wxCount + wxCountAuto;
//
//        map111.put("goodsType", 0);
//        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
//
//        //ok
//        Map<String, Object> mapOk = new HashMap<>();
//        mapOk.put("disId", disId);
//        mapOk.put("status", 3);
//        mapOk.put("goodsType", -1);
//        mapOk.put("dayuPurStatus", 3);
//        //出库完成
//        System.out.println("mapokk" + mapOk);
//        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
//        //订货完成
//        mapOk.put("goodsType", 1);
//        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
//        mapOk.put("goodsType", 2);
//        int wxCountOkAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
//        int wxCountPurOk = wxCountOk + wxCountOkAuto;
//        Map<String, Object> mapB = new HashMap<>();
//        mapB.put("disId", disId);
//        mapB.put("settleType", 0);
//        mapB.put("equalStatus", 0);
//        int unPayCount = nxDepartmentBillService.queryBillsCount(mapB);
//
//        //return
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", disId);
//        map.put("equalStatus", -1);
//        System.out.println("rututtntnmaappa" + map);
//        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryReturnBill(map);
//
//
//        Map<String, Object> mapGb = new HashMap<>();
//        mapGb.put("disId", disId);
//        mapGb.put("gbDepFatherIdNotEqual", -1);
//        mapGb.put("status", 3);
//        System.out.println("usnbdidiid" + mapGb);
//        int i = nxDepartmentBillService.queryBillsCount(mapGb);
//
//
//        Map<String, Object> map7 = new HashMap<>();
//        map7.put("disId", disId);
//        map7.put("status", -1);
//        int unDoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map7);
//
//
//        returnData.put("stockCount", stockCount);
//        returnData.put("stockCountOk", stockCountOK);
//        returnData.put("wxCount", wxCountPur);
//        returnData.put("wxCountOk", wxCountPurOk);
//        returnData.put("unPayCount", unPayCount);
//        returnData.put("preOrders", preOrders);
//
//
//        List<NxGoodsEntity> books = nxGoodsService.queryNumberGoods();
//        returnData.put("deps", mapData);
//        returnData.put("disInfo", nxDistributerService.queryObject(disId));
//        returnData.put("books", books);
//        returnData.put("returnList", billEntityList);
//        returnData.put("unPayGbBills", i);
//        returnData.put("unDoTotal", unDoTotal);
//        return R.ok().put("data", returnData);
//
//    }

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
//                    if (gbDis.getGbDistributerBusinessType() > 0) {
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
//                    }
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

        Map<String, Object> mapG = new HashMap<>();
        mapG.put("batchId", id);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(mapG);

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
//        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByBatchId(id);
        Map<String, Object> mapG = new HashMap<>();
        mapG.put("batchId", id);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(mapG);
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
        if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
            if (purchaseGoodsEntity != null) {
                Integer nxDpgBatchId = purchaseGoodsEntity.getNxDpgBatchId();
                if (nxDpgBatchId == null) {
                    BigDecimal orderCount = new BigDecimal(0);
                    if (purchaseGoodsEntity.getNxDpgOrdersAmount() != null && purchaseGoodsEntity.getNxDpgOrdersAmount() > 0) {
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
            } else {
                System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
            }
        }

        ordersEntity.setNxDoPurchaseGoodsId(-1);
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        ordersEntity.setNxDoStatus(getNxOrderStatusNew());
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


        Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
        if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
            if (purchaseGoodsEntity != null) {
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                System.out.println("purGoodsss" + nxDpgFinishAmount);
                if (nxDpgFinishAmount != null && nxDpgFinishAmount > 0) {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount - 1);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

                    //查询采购批次如果是订货批次purchaseType= 2，那么就修改订货批次为getNxDisPurchaseBatchSellerReply。
                    if(purchaseGoodsEntity.getNxDpgBatchId() != null && purchaseGoodsEntity.getNxDpgBatchId() != -1){

                            NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDistributerPurchaseBatchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());
                            if(nxDistributerPurchaseBatchEntity.getNxDpbStatus() == getNxDisPurchaseBatchDisUserFinish())
                            nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchSellerReply());
                        nxDistributerPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);
                    }
                }
            } else {
                System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
            }
        }
        if (ordersEntity1.getNxDoGbDepartmentOrderId() != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(ordersEntity.getNxDoGbDepartmentOrderId());
            gbDepartmentOrdersEntity.setGbDoStatus(0);
            gbDepartmentOrdersEntity.setGbDoBuyStatus(0);
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }


        //查询该订单的库存扣减记录，恢复库存并删除扣减记录
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", ordersEntity.getNxDepartmentOrdersId());
        List<NxDistributerGoodsShelfStockReduceEntity> reduceEntities = nxDisGoodsShelfStockReduceService.queryReduceListByParams(map);
        if (reduceEntities.size() > 0) {
            System.out.println("开始恢复库存 - 找到 " + reduceEntities.size() + " 条扣减记录");
            for (NxDistributerGoodsShelfStockReduceEntity reduceEntity : reduceEntities) {
                Integer nxDgssrNxStockId = reduceEntity.getNxDgssrNxStockId();
                NxDistributerGoodsShelfStockEntity stockEntity = nxDisGoodsShelfStockService.queryObject(nxDgssrNxStockId);

                // 恢复库存数量和成本
                BigDecimal restWeight = new BigDecimal(stockEntity.getNxDgssRestWeight()).add(new BigDecimal(reduceEntity.getNxDgssrCostWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal restSubtotal = new BigDecimal(stockEntity.getNxDgssRestSubtotal()).add(new BigDecimal(reduceEntity.getNxDgssrCostSubtotal())).setScale(1, BigDecimal.ROUND_HALF_UP);
                stockEntity.setNxDgssRestWeight(restWeight.toString());
                stockEntity.setNxDgssRestSubtotal(restSubtotal.toString());
                nxDisGoodsShelfStockService.update(stockEntity);

                System.out.println("恢复批次 " + nxDgssrNxStockId + " - 恢复重量: " + reduceEntity.getNxDgssrCostWeight() +
                        ", 新剩余: " + restWeight);

                // 删除扣减记录
                nxDisGoodsShelfStockReduceService.delete(reduceEntity.getNxDistributerGoodsShelfStockReduceId());
            }
            System.out.println("库存恢复完成");
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
        
        // 检查规格是否变化：如果新规格和旧规格不一样，单价和小计设置为null
        String oldStandard = ordersEntity.getNxDoStandard();
        boolean standardChanged = false;
        if (oldStandard != null && standard != null && !oldStandard.trim().equals(standard.trim())) {
            System.out.println("⚠️ 规格已变化: 旧规格[" + oldStandard + "] -> 新规格[" + standard + "]，单价和小计将设置为null");
            standardChanged = true;
            ordersEntity.setNxDoPrice(null);
            ordersEntity.setNxDoSubtotal(null);
        } else if (oldStandard == null && standard != null) {
            System.out.println("⚠️ 规格从null变为[" + standard + "]，单价和小计将设置为null");
            standardChanged = true;
            ordersEntity.setNxDoPrice(null);
            ordersEntity.setNxDoSubtotal(null);
        } else if (oldStandard != null && standard == null) {
            System.out.println("⚠️ 规格从[" + oldStandard + "]变为null，单价和小计将设置为null");
            standardChanged = true;
            ordersEntity.setNxDoPrice(null);
            ordersEntity.setNxDoSubtotal(null);
        } else {
            System.out.println("✅ 规格未变化: [" + oldStandard + "] = [" + standard + "]");
        }

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

            // 如果规格变化了，不重新设置价格，保持为null
            // 必须确保部门商品的规格(ddgOrderStandard)和订单规格(standard)一致，才能给单价赋值
            if (nxDepartmentDisGoodsEntity != null && !standardChanged) {
                String departmentStandard = nxDepartmentDisGoodsEntity.getNxDdgOrderStandard();
                // 检查部门商品规格和订单规格是否一致
                boolean standardMatch = (departmentStandard == null && standard == null) 
                        || (departmentStandard != null && standard != null 
                            && departmentStandard.trim().equals(standard.trim()));
                if (standardMatch) {
                    ordersEntity.setNxDoPrice(nxDepartmentDisGoodsEntity.getNxDdgOrderPrice());
                    System.out.println("✅ 部门商品规格匹配，设置价格: " + nxDepartmentDisGoodsEntity.getNxDdgOrderPrice());
                } else {
                    System.out.println("⚠️ 部门商品规格不匹配，跳过价格设置。部门商品规格: [" + departmentStandard 
                            + "], 订单规格: [" + standard + "]");
                }
            } else if (standardChanged) {
                System.out.println("⚠️ 规格已变化，跳过价格设置，保持nxDoPrice为null");
            }

            ordersEntity.setNxDoCostPrice(distributerGoodsEntity.getNxDgBuyingPriceOne());
            ordersEntity.setNxDoCostPriceUpdate(distributerGoodsEntity.getNxDgBuyingPriceOneUpdate());

            System.out.println("priririeieiieieieiiei0000nnnn" + ordersEntity.getNxDoPrice());

            // 参考 saveOneOrder 的逻辑：检查订单规格是否等于商品标准规格，或者订单规格是否匹配大包装单位（智能匹配：件=箱）
            boolean isStandardMatch = standard != null && standard.equals(nxDgGoodsStandardname);
            boolean isCartonMatch = distributerGoodsEntity.getNxDgCartonUnit() != null 
                    && !distributerGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                    && standard != null
                    && isStandardMatch(standard.trim(), distributerGoodsEntity.getNxDgCartonUnit().trim());
            
            if (isStandardMatch || isCartonMatch) {
                System.out.println("订单规格匹配: isStandardMatch=" + isStandardMatch + ", isCartonMatch=" + isCartonMatch);
                ordersEntity.setNxDoWeight(weight);
                // 检查成本价格是否为有效数字
                String costPrice = ordersEntity.getNxDoCostPrice();
                if (costPrice == null || costPrice.trim().isEmpty()) {
                    costPrice = "0";
                }
                try {
                    // 参考 saveOneOrder 的逻辑：判断是否使用外包装单价计算（支持智能匹配：件=箱）
                    BigDecimal doQuantity = new BigDecimal(weight);
                    boolean useCartonPriceForCalc = false;
                    Integer itemsPerCartonForCalc = null;
                    if (distributerGoodsEntity.getNxDgCartonUnit() != null 
                            && !distributerGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                            && standard != null
                            && isStandardMatch(standard.trim(), distributerGoodsEntity.getNxDgCartonUnit().trim())
                            && distributerGoodsEntity.getNxDgItemsPerCarton() != null 
                            && distributerGoodsEntity.getNxDgItemsPerCarton() > 0) {
                        useCartonPriceForCalc = true;
                        itemsPerCartonForCalc = distributerGoodsEntity.getNxDgItemsPerCarton();
                        System.out.println("订单规格匹配外包装单位（智能匹配），计算时将数量转换为箱数: " + doQuantity + "个 ÷ " + itemsPerCartonForCalc + " = " + doQuantity.divide(new BigDecimal(itemsPerCartonForCalc), 4, BigDecimal.ROUND_HALF_UP) + "箱");
                    }
                    
                    // 计算成本小计
                    BigDecimal costSubtotalCalc = null;
                    if (useCartonPriceForCalc && itemsPerCartonForCalc != null) {
                        // 使用外包装成本价：需要将数量转换为箱数
                        BigDecimal cartonCount = doQuantity.divide(new BigDecimal(itemsPerCartonForCalc), 4, BigDecimal.ROUND_HALF_UP);
                        BigDecimal costPriceBD = new BigDecimal(costPrice);
                        costSubtotalCalc = cartonCount.multiply(costPriceBD).setScale(1, BigDecimal.ROUND_HALF_UP);
                        System.out.println("======== 成本小计计算（外包装） ========");
                        System.out.println("原始数量: " + doQuantity + "个");
                        System.out.println("每箱数量: " + itemsPerCartonForCalc + "个/箱");
                        System.out.println("箱数: " + cartonCount + "箱");
                        System.out.println("成本单价: " + costPriceBD + "元/箱");
                        System.out.println("成本小计 = " + cartonCount + "箱 × " + costPriceBD + "元/箱 = " + costSubtotalCalc + "元");
                        System.out.println("=====================================");
                    } else {
                        // 使用最小单位成本价：直接相乘
                        BigDecimal costPriceBD = new BigDecimal(costPrice);
                        costSubtotalCalc = doQuantity.multiply(costPriceBD).setScale(1, BigDecimal.ROUND_HALF_UP);
                        System.out.println("======== 成本小计计算（最小单位） ========");
                        System.out.println("数量: " + doQuantity);
                        System.out.println("成本单价: " + costPriceBD);
                        System.out.println("成本小计 = " + doQuantity + " × " + costPriceBD + " = " + costSubtotalCalc);
                        System.out.println("=====================================");
                    }
                    ordersEntity.setNxDoCostSubtotal(costSubtotalCalc.toString());

                    System.out.println("priririeieiieieieiiei1111" + ordersEntity.getNxDoPrice());
                    if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                        System.out.println("priririeieiieieieiiei2222" + ordersEntity.getNxDoPrice());
                        
                        // 计算销售小计
                        BigDecimal willPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                        BigDecimal subtotal;
                        if (useCartonPriceForCalc && itemsPerCartonForCalc != null) {
                            // 使用外包装单价：需要将数量转换为箱数
                            BigDecimal cartonCount = doQuantity.divide(new BigDecimal(itemsPerCartonForCalc), 4, BigDecimal.ROUND_HALF_UP);
                            subtotal = cartonCount.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                            System.out.println("======== 销售小计计算（外包装） ========");
                            System.out.println("原始数量: " + doQuantity + "个");
                            System.out.println("每箱数量: " + itemsPerCartonForCalc + "个/箱");
                            System.out.println("箱数: " + cartonCount + "箱");
                            System.out.println("销售单价: " + willPrice + "元/箱");
                            System.out.println("销售小计 = " + cartonCount + "箱 × " + willPrice + "元/箱 = " + subtotal + "元");
                            System.out.println("=====================================");
                        } else {
                            // 使用最小单位单价：直接相乘
                            subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                            System.out.println("======== 销售小计计算（最小单位） ========");
                            System.out.println("数量: " + doQuantity);
                            System.out.println("销售单价: " + willPrice);
                            System.out.println("销售小计 = " + doQuantity + " × " + willPrice + " = " + subtotal);
                            System.out.println("=====================================");
                        }
                        
                        ordersEntity.setNxDoSubtotal(subtotal.toString());
                        
                        //profit - 参考 saveOneOrder 的逻辑，使用已计算好的 subtotal 和 costSubtotal
                        if (costSubtotalCalc != null && subtotal != null) {
                            BigDecimal profitB = subtotal.subtract(costSubtotalCalc).setScale(1, BigDecimal.ROUND_HALF_UP);
                            BigDecimal scaleB = BigDecimal.ZERO;
                            if (subtotal.compareTo(BigDecimal.ZERO) != 0) {
                                scaleB = profitB.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                            }
                            System.out.println("成本小计: " + costSubtotalCalc);
                            System.out.println("销售小计: " + subtotal);
                            System.out.println("利润: " + profitB);
                            System.out.println("利润率: " + scaleB + "%");
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
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format for cost price: " + costPrice + ", weight: " + weight);
                    ordersEntity.setNxDoCostSubtotal("0");
                    ordersEntity.setNxDoProfitScale("0");
                    ordersEntity.setNxDoProfitSubtotal("0");
                    ordersEntity.setNxDoPriceDifferent("0");
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
            // 检查价格是否为有效数字
            String willPriceTwo = distributerGoodsEntity.getNxDgWillPriceTwo();
            if (willPriceTwo == null || willPriceTwo.trim().isEmpty()) {
                willPriceTwo = "0";
            }

            try {
                BigDecimal bigDecimal = new BigDecimal(weight).multiply(new BigDecimal(willPriceTwo)).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoWeight(weight);

                Integer nxDoDepartmentId = ordersEntity.getNxDoDepartmentId();
                Map<String, Object> map = new HashMap<>();
                map.put("depId", nxDoDepartmentId);
                map.put("standard", ordersEntity.getNxDoStandard());
                map.put("disGoodsId", distributerGoodsEntity.getNxDistributerGoodsId());
                System.out.println("zahusimdkd" + map);
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
                // 如果规格变化了，不重新设置价格，保持为null
                // 必须确保部门商品的规格(ddgOrderStandard)和订单规格(standard)一致，才能给单价赋值
                if (nxDepartmentDisGoodsEntity != null && !standardChanged) {
                    String departmentStandard = nxDepartmentDisGoodsEntity.getNxDdgOrderStandard();
                    // 检查部门商品规格和订单规格是否一致
                    boolean standardMatch = (departmentStandard == null && standard == null) 
                            || (departmentStandard != null && standard != null 
                                && departmentStandard.trim().equals(standard.trim()));
                    if (standardMatch) {
                        ordersEntity.setNxDoPrice(nxDepartmentDisGoodsEntity.getNxDdgOrderPrice());
                        System.out.println("✅ 部门商品规格匹配，设置价格: " + nxDepartmentDisGoodsEntity.getNxDdgOrderPrice());
                    } else {
                        System.out.println("⚠️ 部门商品规格不匹配，跳过价格设置。部门商品规格: [" + departmentStandard 
                                + "], 订单规格: [" + standard + "]");
                    }
                } else if (standardChanged) {
                    System.out.println("⚠️ 规格已变化，跳过价格设置，保持nxDoPrice为null");
                }
                // 如果规格变化了，小计也设置为null
                if (!standardChanged) {
                ordersEntity.setNxDoSubtotal(bigDecimal.toString());
                } else {
                    ordersEntity.setNxDoSubtotal(null);
                }
                ordersEntity.setNxDoCostPrice(distributerGoodsEntity.getNxDgBuyingPriceTwo());
                ordersEntity.setNxDoCostPriceUpdate(distributerGoodsEntity.getNxDgBuyingPriceTwoUpdate());

                // 检查成本价格是否为有效数字
                String costPriceTwo = ordersEntity.getNxDoCostPrice();
                if (costPriceTwo == null || costPriceTwo.trim().isEmpty()) {
                    costPriceTwo = "0";
                }
                BigDecimal decimal3 = new BigDecimal(weight).multiply(new BigDecimal(costPriceTwo)).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoCostSubtotal(decimal3.toString());
                BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());

                if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                    BigDecimal decimal2 = new BigDecimal(ordersEntity.getNxDoPrice());
                    BigDecimal subtotalB = weightB.multiply(decimal2);

                    BigDecimal nxDoCostPriceB = new BigDecimal(costPriceTwo);  // 使用已验证的 costPriceTwo
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
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format for price or cost: willPriceTwo=" + willPriceTwo + ", costPriceTwo=" + ", weight=" + weight);
                ordersEntity.setNxDoSubtotal("0");
                ordersEntity.setNxDoCostSubtotal("0");
                ordersEntity.setNxDoProfitScale("0");
                ordersEntity.setNxDoProfitSubtotal("0");
                ordersEntity.setNxDoPriceDifferent("0");
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
                    BigDecimal add = purQuantity.subtract(new BigDecimal(ordersEntity.getNxDoQuantity())).setScale(1,BigDecimal.ROUND_HALF_UP);
                    System.out.println("updidddidid" + add);
                    oldPurchaseGoodsEntity.setNxDpgQuantity(add.toString());
                    oldPurchaseGoodsEntity.setNxDpgOrdersAmount(oldPurchaseGoodsEntity.getNxDpgOrdersAmount() - 1);
                    NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
                    if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                        BigDecimal totaoWeight = new BigDecimal(weight);
                        oldPurchaseGoodsEntity.setNxDpgBuyQuantity(add.toString());
                        // Check if buy price is not null or empty before calculating
                        String buyPrice = oldPurchaseGoodsEntity.getNxDpgBuyPrice();
                        if (buyPrice != null && !buyPrice.trim().isEmpty()) {
                            BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(buyPrice)).setScale(1, BigDecimal.ROUND_HALF_UP);
                            purchaseGoodsEntity.setNxDpgBuyQuantity(weight);
                            purchaseGoodsEntity.setNxDpgBuySubtotal(decimal2.toString());
                        } else {
                            purchaseGoodsEntity.setNxDpgBuyQuantity(weight);
                            purchaseGoodsEntity.setNxDpgBuySubtotal("0");
                        }
                    }

                    nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);

                    purchaseGoodsEntity.setNxDpgDistributerId(distributerGoodsEntity.getNxDgDistributerId());
                    purchaseGoodsEntity.setNxDpgDisGoodsFatherId(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
                    purchaseGoodsEntity.setNxDpgDisGoodsGrandId(distributerGoodsEntity.getNxDgDfgGoodsGrandId());
                    purchaseGoodsEntity.setNxDpgDistributerId(distributerGoodsEntity.getNxDgDistributerId());
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
                    purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
                    purchaseGoodsEntity.setNxDpgOrdersAmount(1);
                    purchaseGoodsEntity.setNxDpgFinishAmount(0);
                    purchaseGoodsEntity.setNxDpgPurchaseType(getGbPurchaseGoodsTypeForOrder());
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
                BigDecimal add = purQuantity.subtract(new BigDecimal(oldNxDoQuantity)).add(new BigDecimal(weight)).setScale(1,BigDecimal.ROUND_HALF_UP);
                oldPurchaseGoodsEntity.setNxDpgQuantity(add.toString());
                oldPurchaseGoodsEntity.setNxDpgBuyQuantity(add.toString());
                if (standard.equals(nxDgGoodsStandardname)) {
                    System.out.println("hehehehrhereer===" + nxDgGoodsStandardname);
                    // Check if buy price is not null or empty before calculating
                    String buyPrice = oldPurchaseGoodsEntity.getNxDpgBuyPrice();
                    if (buyPrice != null && !buyPrice.trim().isEmpty()) {
                        BigDecimal decimal = new BigDecimal(buyPrice).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        oldPurchaseGoodsEntity.setNxDpgBuySubtotal(decimal.toString());
                    } else {
                        // If buy price is empty, set subtotal to 0
                        oldPurchaseGoodsEntity.setNxDpgBuySubtotal("0");
                    }
                }
                nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);

            }
        }

        ordersEntity.setNxDoQuantity(weight);
        ordersEntity.setNxDoStandard(standard);
        ordersEntity.setNxDoRemark(remark);
        
        // 智能匹配打印规格：如果订单规格匹配大包装单位，则设置为大包装单位
        // 支持同义词匹配：件=箱，盒=箱等
        System.out.println("======== 修改订单-设置打印规格(printStandard)开始 ========");
        System.out.println("订单ID: " + ordersEntity.getNxDepartmentOrdersId());
        System.out.println("订单规格(standard): " + standard);
        System.out.println("前端传入打印规格(printStandard参数): " + printStandard);
        System.out.println("商品大包装单位(nxDgCartonUnit): " + distributerGoodsEntity.getNxDgCartonUnit());
        System.out.println("商品标准规格(nxDgGoodsStandardname): " + distributerGoodsEntity.getNxDgGoodsStandardname());
        System.out.println("设置前 printStandard: " + ordersEntity.getNxDoPrintStandard());
        
        if (distributerGoodsEntity.getNxDgCartonUnit() != null 
                && !distributerGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                && standard != null
                && isStandardMatch(standard.trim(), distributerGoodsEntity.getNxDgCartonUnit().trim())) {
            ordersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgCartonUnit());
            System.out.println("✅ [printStandard] 已设置为大包装单位: " + distributerGoodsEntity.getNxDgCartonUnit());
        } else if (printStandard != null && !printStandard.trim().isEmpty()) {
            // 如果前端传入了打印规格，使用前端传入的值
        ordersEntity.setNxDoPrintStandard(printStandard);
            System.out.println("✅ [printStandard] 已设置为前端传入值: " + printStandard);
        } else {
            // 否则使用商品的标准规格名称
            ordersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgGoodsStandardname());
            System.out.println("✅ [printStandard] 已设置为商品标准规格: " + distributerGoodsEntity.getNxDgGoodsStandardname());
        }
        System.out.println("设置后 printStandard: " + ordersEntity.getNxDoPrintStandard());
        System.out.println("======== 修改订单-设置打印规格(printStandard)结束 ========");

        // 参考 saveOneOrder 的逻辑：从部门商品中查找价格（如果前面没有设置价格，或者规格变化后需要重新查找）
        System.out.println("======== 从部门商品查找价格开始 ========");
        Map<String, Object> depGoodsMap = new HashMap<>();
        depGoodsMap.put("depId", ordersEntity.getNxDoDepartmentId());
        depGoodsMap.put("disGoodsId", distributerGoodsEntity.getNxDistributerGoodsId());
        depGoodsMap.put("standard", standard);  // 使用新的规格
        System.out.println("查询部门商品参数: " + depGoodsMap);
        
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(depGoodsMap);
        if (departmentDisGoodsEntity != null) {
            System.out.println("找到部门商品，规格: " + departmentDisGoodsEntity.getNxDdgOrderStandard());
            System.out.println("部门商品价格: " + departmentDisGoodsEntity.getNxDdgOrderPrice());
            
            // 如果订单价格为null（规格变化后），或者需要更新价格，则从部门商品获取
            // 必须确保部门商品的规格(ddgOrderStandard)和订单规格(standard)一致，才能给单价赋值
            if (ordersEntity.getNxDoPrice() == null || standardChanged) {
                String departmentStandard = departmentDisGoodsEntity.getNxDdgOrderStandard();
                // 检查部门商品规格和订单规格是否一致
                boolean standardMatch = (departmentStandard == null && standard == null) 
                        || (departmentStandard != null && standard != null 
                            && departmentStandard.trim().equals(standard.trim()));
                if (standardMatch) {
                    ordersEntity.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
                    System.out.println("✅ 部门商品规格匹配，从部门商品设置价格: " + departmentDisGoodsEntity.getNxDdgOrderPrice());
                } else {
                    System.out.println("⚠️ 部门商品规格不匹配，跳过价格设置。部门商品规格: [" + departmentStandard 
                            + "], 订单规格: [" + standard + "]");
                }
            }
            
            // 判断是否使用了大包装单价，如果是则设置打印规格为大包装单位（支持智能匹配：件=箱）
            boolean isUsingCartonPrice = distributerGoodsEntity.getNxDgCartonUnit() != null 
                    && !distributerGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                    && standard != null
                    && isStandardMatch(standard.trim(), distributerGoodsEntity.getNxDgCartonUnit().trim());
            
            if (isUsingCartonPrice) {
                ordersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgCartonUnit());
                System.out.println("使用大包装单价，打印规格设置为: " + distributerGoodsEntity.getNxDgCartonUnit());
            } else if (ordersEntity.getNxDoCostPriceLevel() == null || ordersEntity.getNxDoCostPriceLevel().equals("1")) {
                ordersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgGoodsStandardname());
            } else {
                ordersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgWillPriceTwoStandard());
            }
            
            // 如果有重量和单价，则重新计算 subtotal（参考 saveOneOrder 的逻辑）
            if (ordersEntity.getNxDoWeight() != null && !ordersEntity.getNxDoWeight().trim().isEmpty() 
                    && ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                try {
                    BigDecimal weightBD = new BigDecimal(ordersEntity.getNxDoWeight());
                    BigDecimal priceBD = new BigDecimal(ordersEntity.getNxDoPrice());
                    BigDecimal subtotal = weightBD.multiply(priceBD).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoSubtotal(subtotal.toString());
                    System.out.println("重新计算小计: " + weightBD + " × " + priceBD + " = " + subtotal);
                } catch (NumberFormatException e) {
                    System.err.println("计算subtotal失败，重量或单价格式错误: weight=" + ordersEntity.getNxDoWeight() 
                            + ", price=" + ordersEntity.getNxDoPrice() + ", error=" + e.getMessage());
                }
            }
            
            ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
            System.out.println("✅ 部门商品ID已设置: " + departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
        } else {
            // 如果没有找到对应规格的部门商品，尝试查找标准为null的部门商品
            depGoodsMap.put("standard", null);
            System.out.println("未找到对应规格的部门商品，尝试查找标准为null的部门商品: " + depGoodsMap);
            NxDepartmentDisGoodsEntity departmentDisGoodsEntityO = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(depGoodsMap);
            if (departmentDisGoodsEntityO != null) {
                ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
                System.out.println("✅ 找到标准为null的部门商品，ID: " + departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
            } else {
                System.out.println("⚠️ 未找到部门商品");
            }
        }
        System.out.println("======== 从部门商品查找价格结束 ========");

        ordersEntity.setNxDoCostPriceLevel(priceLevel);
        nxDepartmentOrdersService.update(ordersEntity);

        ordersEntity.setNxDistributerGoodsEntity(distributerGoodsEntity);
        return R.ok().put("data", ordersEntity);
    }


    @ResponseBody
    @RequestMapping(value = "/updateOrderReturn", method = RequestMethod.POST)
    public R updateOrderReturn(Integer id, String weight, String subtotal) {

        NxDepartmentOrderHistoryEntity orders = historyService.queryObject(id);

        orders.setNxDoReturnWeight(weight);
        orders.setNxDoReturnSubtotal(subtotal);
        orders.setNxDoReturnStatus(0);
        historyService.update(orders);
        return R.ok().put("data", orders);
    }

    /**
     * ORDER
     * 删除申请
     *
     * @param nxDepartmentOrdersId 订货申请id
     * @return ok
     */
    @ResponseBody
    /**
     * 删除单个订单的内部逻辑
     * 
     * @param nxDepartmentOrdersId 订单ID
     * @return 是否删除成功
     */
    private boolean deleteOrderInternal(Integer nxDepartmentOrdersId) {
        try {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObjectNew(nxDepartmentOrdersId);
            if (ordersEntity == null) {
                logger.warn("[deleteOrderInternal] 订单不存在，订单ID: {}", nxDepartmentOrdersId);
                return false;
            }

            // 删除训练数据
            if (ordersEntity.getNxDoTrainingDataId() != null) {
                logger.info("[deleteOrderInternal] 删除训练数据，训练数据ID: {}", ordersEntity.getNxDoTrainingDataId());
                NxOrderOcrTrainingDataEntity nxOrderOcrTrainingDataEntity = nxOrderOcrTrainingDataService.queryObject(ordersEntity.getNxDoTrainingDataId());
                if(nxOrderOcrTrainingDataEntity != null){
                    if(nxOrderOcrTrainingDataEntity.getNxOtdOrderId() == null){
                        nxOrderOcrTrainingDataService.delete(ordersEntity.getNxDoTrainingDataId());
                    }else{
                        Integer nxOtdOrderId = nxOrderOcrTrainingDataEntity.getNxOtdOrderId();
                        logger.info("[deleteOrderInternal] 删除训练数据shcnh" + nxOtdOrderId + "grid" + nxDepartmentOrdersId);
                        if(nxOtdOrderId.equals(nxDepartmentOrdersId)){
                            nxOrderOcrTrainingDataService.delete(ordersEntity.getNxDoTrainingDataId());
                        }
                    }
                }
            }

            // 处理采购商品
            if (ordersEntity.getNxDoPurchaseGoodsId() != null && ordersEntity.getNxDoPurchaseGoodsId() != -1) {
                NxDistributerPurchaseGoodsEntity nxDistributerPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(ordersEntity.getNxDoPurchaseGoodsId());
                if (nxDistributerPurchaseGoodsEntity != null) {
                    Integer nxDpgOrdersAmount = nxDistributerPurchaseGoodsEntity.getNxDpgOrdersAmount();
                    if (nxDpgOrdersAmount > 1) {
                        nxDistributerPurchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
                        if (nxDistributerPurchaseGoodsEntity.getNxDpgStandard().equals(ordersEntity.getNxDoStandard())) {
                            if (nxDistributerPurchaseGoodsEntity.getNxDpgBuyQuantity() != null && ordersEntity.getNxDoQuantity() != null) {
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
                            nxDistributerPurchaseBatchService.delete(nxDpgBatchId);
                        }
                        nxDistributerPurchaseGoodsService.delete(nxDistributerPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
                    }

                    // 更新同部门同状态订单的todayOrder
                    Map<String, Object> map = new HashMap<>();
                    map.put("depId", ordersEntity.getNxDoDepartmentId());
                    map.put("status", 3);
                    map.put("todayOrder", ordersEntity.getNxDoTodayOrder());
                    List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
                    if (ordersEntities.size() > 0) {
                        Integer currentTodayOrder = ordersEntity.getNxDoTodayOrder() != null ? ordersEntity.getNxDoTodayOrder() : 0;
                        for (int i = 0; i < ordersEntities.size(); i++) {
                            NxDepartmentOrdersEntity ordersEntity1 = ordersEntities.get(i);
                            ordersEntity1.setNxDoTodayOrder(currentTodayOrder + i);
                            nxDepartmentOrdersService.update(ordersEntity1);
                        }
                    }
                }
            } else {
                // 没有采购商品的情况，更新todayOrder
                Map<String, Object> map = new HashMap<>();
                map.put("depId", ordersEntity.getNxDoDepartmentId());
                map.put("status", 3);
                map.put("todayOrder", ordersEntity.getNxDoTodayOrder());
                map.put("orderBy", "time");
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
                if (ordersEntities.size() > 0) {
                    Integer currentTodayOrder = ordersEntity.getNxDoTodayOrder() != null ? ordersEntity.getNxDoTodayOrder() : 0;
                    for (int i = 0; i < ordersEntities.size(); i++) {
                        NxDepartmentOrdersEntity ordersEntity1 = ordersEntities.get(i);
                        ordersEntity1.setNxDoTodayOrder(currentTodayOrder + i);
                        nxDepartmentOrdersService.update(ordersEntity1);
                    }
                }
            }

            // 删除订单
            nxDepartmentOrdersService.delete(nxDepartmentOrdersId);
            return true;
        } catch (Exception e) {
            logger.error("[deleteOrderInternal] 删除订单失败，订单ID: {}, 错误: {}", nxDepartmentOrdersId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除单个订单
     * 
     * @param nxDepartmentOrdersId 订单ID
     * @return 删除结果
     */
    @RequestMapping("/delete/{nxDepartmentOrdersId}")
    public R delete(@PathVariable Integer nxDepartmentOrdersId) {
        boolean success = deleteOrderInternal(nxDepartmentOrdersId);
        if (success) {
            return R.ok();
        } else {
            return R.error("删除订单失败，订单ID: " + nxDepartmentOrdersId);
        }
    }

    /**
     * 批量删除订单
     * 
     * @param request 请求体，包含订单ID列表，格式：{"orderIds": [1, 2, 3, ...]}
     * @return 删除结果，包含成功和失败的统计信息
     */
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.POST)
    @ResponseBody
    public R deleteBatch(@RequestBody Map<String, Object> request) {
        try {
            // 获取订单ID列表
            Object orderIdsObj = request.get("orderIds");
            if (orderIdsObj == null) {
                return R.error("订单ID列表不能为空");
            }

            List<Integer> orderIds = new ArrayList<>();
            if (orderIdsObj instanceof List) {
                List<?> list = (List<?>) orderIdsObj;
                for (Object item : list) {
                    if (item instanceof Number) {
                        orderIds.add(((Number) item).intValue());
                    } else if (item instanceof String) {
                        try {
                            orderIds.add(Integer.parseInt((String) item));
                        } catch (NumberFormatException e) {
                            logger.warn("[deleteBatch] 无效的订单ID格式: {}", item);
                        }
                    }
                }
            } else if (orderIdsObj instanceof Integer[]) {
                Integer[] array = (Integer[]) orderIdsObj;
                orderIds = Arrays.asList(array);
            } else {
                return R.error("订单ID列表格式不正确，应为数组格式");
            }

            if (orderIds.isEmpty()) {
                return R.error("订单ID列表为空");
            }

            logger.info("[deleteBatch] 开始批量删除订单，订单数量: {}", orderIds.size());

            // 执行批量删除
            List<Integer> successIds = new ArrayList<>();
            List<Integer> failedIds = new ArrayList<>();
            Map<Integer, String> failedReasons = new HashMap<>();

            for (Integer orderId : orderIds) {
                boolean success = deleteOrderInternal(orderId);
                if (success) {
                    successIds.add(orderId);
                } else {
                    failedIds.add(orderId);
                    failedReasons.put(orderId, "删除失败");
                }
            }

            logger.info("[deleteBatch] 批量删除完成，成功: {} 个，失败: {} 个", successIds.size(), failedIds.size());

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("total", orderIds.size());
            result.put("successCount", successIds.size());
            result.put("failedCount", failedIds.size());
            result.put("successIds", successIds);
            result.put("failedIds", failedIds);
            result.put("failedReasons", failedReasons);
            return R.ok().put("data", result).put("msg", "批量删除成功");
//            if (failedIds.isEmpty()) {
//                return R.ok().put("data", result).put("msg", "批量删除成功");
//            } else if (successIds.isEmpty()) {
//                return R.error("批量删除失败").put("data", result);
//            } else {
//                return R.ok().put("data", result).put("msg", "部分删除成功，部分删除失败");
//            }
        } catch (Exception e) {
            logger.error("[deleteBatch] 批量删除订单异常: {}", e.getMessage(), e);
            return R.error("批量删除订单异常: " + e.getMessage());
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
    @RequestMapping("/saveCashSun")
    public R saveCashSun(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {
        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusGouwu());
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
        map.put("isPurType", true);
        int order = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        nxDepartmentOrders.setNxDoTodayOrder(order + 1);

        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = saveOneOrderCash(nxDepartmentOrders, nxDistributerGoodsEntity);

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
        // 获取beforOrder的todayOrder值，如果为null则使用0
        Integer beforTodayOrder = beforOrder.getNxDoTodayOrder() != null ? beforOrder.getNxDoTodayOrder() : 0;
        nxDepartmentOrders.setNxDoTodayOrder(beforTodayOrder);
        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = saveOneOrder(nxDepartmentOrders, nxDistributerGoodsEntity);
//


        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("todayOrder", beforTodayOrder);
        map.put("status", 3);
        map.put("orderBy", "time");
        System.out.println("maporodoerer" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            System.out.println("orooro" + ordersEntities.size());
            for (int i = 0; i < ordersEntities.size(); i++) {
                NxDepartmentOrdersEntity ordersEntity = ordersEntities.get(i);
                int i1 = beforTodayOrder + i + 2;
                ordersEntity.setNxDoTodayOrder(i1);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

        beforOrder.setNxDoTodayOrder(beforTodayOrder + 1);
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
        // 获取beforOrder的todayOrder值，如果为null则使用0
        Integer beforTodayOrder = beforOrder.getNxDoTodayOrder() != null ? beforOrder.getNxDoTodayOrder() : 0;
        System.out.println("befororrordidd" + beforTodayOrder);
        nxDepartmentOrders.setNxDoTodayOrder(beforTodayOrder);

        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = saveOneOrder(nxDepartmentOrders, nxDistributerGoodsEntity);


        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("todayOrder", beforTodayOrder);
        map.put("status", 3);
        map.put("orderBy", "time");
        System.out.println("maporodoerer" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            System.out.println("orooro" + ordersEntities.size());
            for (int i = 0; i < ordersEntities.size(); i++) {
                NxDepartmentOrdersEntity ordersEntity = ordersEntities.get(i);
                int i1 = beforTodayOrder + i + 2;
                System.out.println("whisisisisisisisis====" + i1);
                ordersEntity.setNxDoTodayOrder(i1);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

        beforOrder.setNxDoTodayOrder(beforTodayOrder + 1);
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
        
        // 使用 queryPurchaseGoodsByParams 查询列表，避免 selectOne() 返回多条记录时报错
        List<NxDistributerPurchaseGoodsEntity> purGoodsList = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
        NxDistributerPurchaseGoodsEntity havePurGoods = null;
        if (purGoodsList != null && !purGoodsList.isEmpty()) {
            // 如果有多条记录，选择第一条（通常应该只有一条，但数据库可能有重复数据）
            havePurGoods = purGoodsList.get(0);
            if (purGoodsList.size() > 1) {
                logger.warn("[savePurGoodsAuto] 查询到多条采购商品记录，商品ID={}, 规格={}, 记录数={}，使用第一条记录", 
                        doDisGoodsId, ordersEntity.getNxDoStandard(), purGoodsList.size());
            }
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
                ordersEntity.setNxDoPurchaseStatus(getNxDisPurchaseGoodsIsPurchase());
                ordersEntity.setNxDoPurchaseGoodsId(resultPurGoods.getNxDistributerPurchaseGoodsId());
            }

        }

        nxDepartmentOrdersService.update(ordersEntity);

    }


    @RequestMapping(value = "/disSaveLinshiToNxGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disSaveLinshiToNxGoods(Integer lsGoodsId, Integer nxGoodsId) {

        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxGoodsId);
        NxDistributerGoodsEntity linshiGoods = nxDistributerGoodsService.queryObject(lsGoodsId);

        Map<String, Object> mapPur = new HashMap<>();
        mapPur.put("disGoodsId", lsGoodsId);
        List<NxDistributerPurchaseGoodsEntity> nxDistributerPurchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(mapPur);
        if (nxDistributerPurchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : nxDistributerPurchaseGoodsEntities) {
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                System.out.println("oneonenenennennene" + nxDpgOrdersAmount);
                if (nxDpgOrdersAmount == 1) {
                    if (purchaseGoodsEntity.getNxDpgBatchId() == null) {
                        nxDistributerPurchaseGoodsService.delete(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
                    } else {
                        System.out.println("updateeeeeee" + purchaseGoodsEntity);
                        purchaseGoodsEntity.setNxDpgDisGoodsId(nxGoodsId);
                        purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDistributerGoodsEntity.getGbDisGoodsFatherId());
                        purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                } else {
                    purchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
                    purchaseGoodsEntity.setNxDpgDisGoodsId(nxGoodsId);
                    purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDistributerGoodsEntity.getGbDisGoodsFatherId());
                    purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

                    if (nxDistributerGoodsEntity.getNxDgBuyingPriceOne().equals("0.1")) {
                        nxDistributerGoodsEntity.setNxDgBuyingPriceOne(purchaseGoodsEntity.getNxDpgBuyPrice());
                        nxDistributerGoodsEntity.setNxDgBuyingPrice(purchaseGoodsEntity.getNxDpgBuyPrice());
                        nxDistributerGoodsEntity.setNxDgBuyingPriceUpdate(formatWhatDay(0));
                        nxDistributerGoodsService.update(nxDistributerGoodsEntity);
                    }
                }
            }
        }

        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();

        Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();
        Integer nxDgNxFatherId = nxDistributerGoodsEntity.getNxDgNxFatherId();

        Map<String, Object> linshiMap = new HashMap<>();
        linshiMap.put("disGoodsId", lsGoodsId);
        System.out.println("linsihsimappsa " + linshiMap);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(linshiMap);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsId(nxGoodsId);
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                if (ordersEntity.getNxDoStatus() == -2) {
                    ordersEntity.setNxDoStatus(0);
                }
                nxDepartmentOrdersService.update(ordersEntity);

                if (nxDistributerGoodsEntity.getNxDgPurchaseAuto() != -1) {
                    savePurGoodsAuto(ordersEntity);
                }
            }
        }

        Map<String, Object> linshiMapH = new HashMap<>();
        linshiMapH.put("disGoodsId", lsGoodsId);
        System.out.println("linsihsimappslinshiMapHlinshiMapHa " + linshiMapH);
        List<NxDepartmentOrderHistoryEntity> ordersEntitiesH = historyService.queryDisHistoryOrdersByParams(linshiMapH);
        if (ordersEntitiesH.size() > 0) {
            for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesH) {
                ordersEntity.setNxDoDisGoodsId(nxGoodsId);
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                historyService.update(ordersEntity);

                if (nxDistributerGoodsEntity.getNxDgWillPriceOne().equals("0.1")) {
                    nxDistributerGoodsEntity.setNxDgWillPriceOne(ordersEntity.getNxDoPrice());
                    nxDistributerGoodsService.update(nxDistributerGoodsEntity);
                }

            }
        }

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(linshiMap);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity linshiDepGoodsEntity : departmentDisGoodsEntities) {
                Integer nxDdgDepartmentId = linshiDepGoodsEntity.getNxDdgDepartmentId();
                Map<String, Object> map = new HashMap<>();
                map.put("depId", nxDdgDepartmentId);
                map.put("disGoodsId", nxGoodsId);
                System.out.println("depdisgoenen" + map);
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
                if (nxDepartmentDisGoodsEntity != null) {
                    System.out.println("hasdepgoods" + nxDepartmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsStandardname(linshiGoods.getNxDgGoodsStandardname());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderStandard(linshiGoods.getNxDgGoodsStandardname());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderPrice(linshiDepGoodsEntity.getNxDdgOrderPrice());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderGoodsName(linshiGoods.getNxDgGoodsName());
                    nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);
                    nxDepartmentDisGoodsService.delete(linshiDepGoodsEntity.getNxDepartmentDisGoodsId());

                } else {
                    linshiDepGoodsEntity.setNxDdgDisGoodsId(nxGoodsId);
                    linshiDepGoodsEntity.setNxDdgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    linshiDepGoodsEntity.setNxDdgDisGoodsGrandId(nxDgDfgGoodsGrandId);

                    NxDistributerFatherGoodsEntity grandEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsGrandId);
                    Integer nxDfgGreatId = grandEntity.getNxDfgFathersFatherId();
                    linshiDepGoodsEntity.setNxDdgDisGoodsGreatId(nxDfgGreatId);

                    linshiDepGoodsEntity.setNxDdgDepGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
                    linshiDepGoodsEntity.setNxDdgOrderGoodsName(linshiGoods.getNxDgGoodsName());
                    linshiDepGoodsEntity.setNxDdgOrderStandard(linshiGoods.getNxDgGoodsStandardname());
                    linshiDepGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
                    nxDepartmentDisGoodsService.update(linshiDepGoodsEntity);
                }
            }
        }

        Map<String, Object> mapNx = new HashMap<>();
        mapNx.put("disGoodsId", nxGoodsId);
        int same = 0;
        List<NxDistributerStandardEntity> nxStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapNx);
        if (nxStandardEntities.size() > 0) {
            for (NxDistributerStandardEntity nxDistributerStandardEntity : nxStandardEntities) {
                if (linshiGoods.getNxDgGoodsStandardname().equals(nxDistributerStandardEntity.getNxDsStandardName())) {
                    same = same + 1;
                }
            }
            if (nxDistributerGoodsEntity.getNxDgGoodsStandardname().equals(linshiGoods.getNxDgGoodsStandardname())) {
                same = same + 1;
            }
        }
        String nxDgGoodsStandardname = nxDistributerGoodsEntity.getNxDgGoodsStandardname();
        String nxDgGoodsStandardname1 = linshiGoods.getNxDgGoodsStandardname();
        if (nxDgGoodsStandardname.equals(nxDgGoodsStandardname1)) {
            same = same + 1;
        }
        if (same == 0) {
            NxDistributerStandardEntity nxDistributerStandardEntity = new NxDistributerStandardEntity();
            nxDistributerStandardEntity.setNxDsStandardName(linshiGoods.getNxDgGoodsStandardname());
            nxDistributerStandardEntity.setNxDsDisGoodsId(nxGoodsId);
            nxDistributerStandardEntity.setNxDsStandardSort(nxStandardEntities.size());
            nxDistributerStandardService.save(nxDistributerStandardEntity);
        }

        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(linshiMap);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsId(nxDgNxGoodsId);
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

            }
        }


        // 1. 更新库存商品的商品ID（如果库存商品有 lsGoodsId 的，要换成新的 nxGoodsId）
        Map<String, Object> stockMap = new HashMap<>();
        stockMap.put("disGoodsId", lsGoodsId);
        List<NxDistributerGoodsShelfStockEntity> stockList = nxDisGoodsShelfStockService.queryShelfStockListByParams(stockMap);
        if (stockList != null && !stockList.isEmpty()) {
            System.out.println("[disSaveLinshiToNxGoods] 找到库存批次 " + stockList.size() + " 个，更新商品ID从 " + lsGoodsId + " 到 " + nxGoodsId);
            
            // 查询新的 nxGoodsId 是否有货架商品
            Map<String, Object> nxGoodsShelfMap = new HashMap<>();
            nxGoodsShelfMap.put("disGoodsId", nxGoodsId);
            List<NxDistributerGoodsShelfGoodsEntity> nxGoodsShelfGoodsList = shelfGoodsService.queryShelfForGoodsByParams(nxGoodsShelfMap);
            NxDistributerGoodsShelfGoodsEntity nxGoodsShelfGoods = null;
            if (nxGoodsShelfGoodsList != null && !nxGoodsShelfGoodsList.isEmpty()) {
                // 优先使用货架ID=1的货架商品，如果没有则使用第一个
                nxGoodsShelfGoods = nxGoodsShelfGoodsList.stream()
                    .filter(sg -> sg.getNxDgsgShelfId() != null && sg.getNxDgsgShelfId() == 1)
                    .findFirst()
                    .orElse(nxGoodsShelfGoodsList.get(0));
                System.out.println("[disSaveLinshiToNxGoods] 新商品 " + nxGoodsId + " 有货架商品，货架商品ID: " + nxGoodsShelfGoods.getNxDistributerGoodsShelfGoodsId());
            }
            
            for (NxDistributerGoodsShelfStockEntity stock : stockList) {
                stock.setNxDgssNxDisGoodsId(nxGoodsId);
                stock.setNxDgssNxDisGoodsFatherId(nxDgDfgGoodsFatherId);
                
                // 如果新商品有货架商品，更新库存的货架商品ID
                if (nxGoodsShelfGoods != null && nxGoodsShelfGoods.getNxDistributerGoodsShelfGoodsId() != null) {
                    stock.setNxDgssNxShelfGoodsId(nxGoodsShelfGoods.getNxDistributerGoodsShelfGoodsId());
                    System.out.println("[disSaveLinshiToNxGoods] 更新库存批次 " + stock.getNxDistributerGoodsShelfStockId() + " 的货架商品ID为 " + nxGoodsShelfGoods.getNxDistributerGoodsShelfGoodsId());
                }
                
                nxDisGoodsShelfStockService.update(stock);
                System.out.println("[disSaveLinshiToNxGoods] 更新库存批次 " + stock.getNxDistributerGoodsShelfStockId() + " 的商品ID为 " + nxGoodsId);
            }
        }

        // 2. 更新货架商品的商品ID（如果有货架商品，也要更新货架商品的ID）
        System.out.println("dshelflfl" + linshiMap);
        List<NxDistributerGoodsShelfGoodsEntity> entities = shelfGoodsService.queryShelfForGoodsByParams(linshiMap);
        System.out.println("shellfgoo" + entities.size());
        if (entities.size() > 0) {
            for (NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity : entities) {
                shelfGoodsEntity.setNxDgsgDisGoodsId(nxGoodsId);
                shelfGoodsService.update(shelfGoodsEntity);
                System.out.println("[disSaveLinshiToNxGoods] 更新货架商品 " + shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId() + " 的商品ID为 " + nxGoodsId);
            }
        }


        Map<String, Object> mapGb = new HashMap<>();
        mapGb.put("nxDisGoodsId", lsGoodsId);
        System.out.println("gbdidiididid" + mapGb);
        List<GbDistributerGoodsEntity> goodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapGb);
        if (goodsEntities.size() > 0) {
            for (GbDistributerGoodsEntity distributerGoodsEntity : goodsEntities) {
                distributerGoodsEntity.setGbDgNxDistributerGoodsId(nxGoodsId);
                gbDistributerGoodsService.update(distributerGoodsEntity);
            }
        }


        List<GbDepartmentOrdersEntity> gbOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(mapGb);
        if (gbOrdersEntities.size() > 0) {
            for (GbDepartmentOrdersEntity ordersEntity : gbOrdersEntities) {
                ordersEntity.setGbDoNxDistributerGoodsId(nxGoodsId);
                gbDepartmentOrdersService.update(ordersEntity);
            }
        }


        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("nxGoodsId", lsGoodsId);
        List<GbDepartmentDisGoodsEntity> gbDepartmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapDep);
        if (gbDepartmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity disGoodsEntity : gbDepartmentDisGoodsEntities) {
                gbDepartmentDisGoodsService.delete(disGoodsEntity.getGbDepartmentDisGoodsId());
            }
        }

        NxDistributerGoodsLinshiEntity linshiEntity = new NxDistributerGoodsLinshiEntity();
        linshiEntity.setNxDgDistributerLsId(linshiGoods.getNxDgDistributerId());
        linshiEntity.setNxDgGoodsLsName(linshiGoods.getNxDgGoodsName());
        linshiEntity.setNxDgGoodsLsStandardname(linshiGoods.getNxDgGoodsStandardname());
        linshiEntity.setNxDgDfgGoodsFatherLsId(linshiGoods.getNxDgDfgGoodsFatherId());
        linshiEntity.setNxDgGoodsLsDetail(linshiGoods.getNxDgGoodsDetail());
        linshiEntity.setNxDgGoodsLsStatus(-1);
        linshiEntity.setNxDgToNxDisGoodsId(nxGoodsId);
        linshiEntity.setNxDgApplyDate(linshiGoods.getNxDgBuyingPriceUpdate());
        linshiEntity.setNxDgGoodsLsFile(linshiGoods.getNxDgGoodsFile());
        linshiEntity.setNxDgGoodsLsFileLarge(linshiGoods.getNxDgGoodsFileLarge());
        linshiService.save(linshiEntity);

        nxDistributerGoodsService.delete(lsGoodsId);

        return R.ok().put("data",nxDistributerGoodsEntity);
    }


    @RequestMapping(value = "/disSaveLinshiToAlias", method = RequestMethod.POST)
    @ResponseBody
    public R disSaveLinshiToAlias(Integer lsGoodsId, Integer nxGoodsId, Integer disId) {

        NxDistributerAliasEntity aliasEntity = new NxDistributerAliasEntity();

        Map<String, Object> mapPur = new HashMap<>();
        mapPur.put("disGoodsId", lsGoodsId);
        List<NxDistributerPurchaseGoodsEntity> nxDistributerPurchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(mapPur);
        if (nxDistributerPurchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : nxDistributerPurchaseGoodsEntities) {
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                if (nxDpgOrdersAmount == 1 && purchaseGoodsEntity.getNxDpgStatus() == 0 && purchaseGoodsEntity.getNxDpgBatchId() == null) {
                    nxDistributerPurchaseGoodsService.delete(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
                } else {
                    purchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            }
        }

        NxDistributerGoodsEntity linshiGoods = nxDistributerGoodsService.queryObject(lsGoodsId);
        aliasEntity.setNxDaAliasName(linshiGoods.getNxDgGoodsName());
        String pinyin = hanziToPinyin(linshiGoods.getNxDgGoodsName());
        String headPinyin = getHeadStringByString(linshiGoods.getNxDgGoodsName(), false, null);
        aliasEntity.setNxDaAliasPy(headPinyin);
        aliasEntity.setNxDaAliasPinyin(pinyin);
        aliasEntity.setNxDaDisGoodsId(nxGoodsId);
        nxDistributerAliasService.save(aliasEntity);

        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxGoodsId);
        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
        Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();
        Integer nxDgNxFatherId = nxDistributerGoodsEntity.getNxDgNxFatherId();

        Map<String, Object> linshiMap = new HashMap<>();
        linshiMap.put("disGoodsId", lsGoodsId);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(linshiMap);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsId(nxGoodsId);
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                if (ordersEntity.getNxDoStatus() == -2) {
                    ordersEntity.setNxDoStatus(0);
                }
                nxDepartmentOrdersService.update(ordersEntity);
                if (nxDistributerGoodsEntity.getNxDgPurchaseAuto() != -1) {
                    savePurGoodsAuto(ordersEntity);
                }
            }
        }

        Map<String, Object> linshiMapH = new HashMap<>();
        linshiMapH.put("disGoodsId", lsGoodsId);
        List<NxDepartmentOrderHistoryEntity> ordersEntitiesH = historyService.queryDisHistoryOrdersByParams(linshiMapH);
        if (ordersEntitiesH.size() > 0) {
            for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesH) {
                ordersEntity.setNxDoDisGoodsId(nxGoodsId);
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                historyService.update(ordersEntity);
            }
        }

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(linshiMap);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity linshiDepGoodsEntity : departmentDisGoodsEntities) {
                Integer nxDdgDepartmentId = linshiDepGoodsEntity.getNxDdgDepartmentId();
                Map<String, Object> map = new HashMap<>();
                map.put("depId", nxDdgDepartmentId);
                map.put("disGoodsId", nxGoodsId);
                System.out.println("depdisgoenen" + map);
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
                if (nxDepartmentDisGoodsEntity != null) {

                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsStandardname(linshiGoods.getNxDgGoodsStandardname());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderStandard(linshiGoods.getNxDgGoodsStandardname());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderPrice(linshiDepGoodsEntity.getNxDdgOrderPrice());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderGoodsName(linshiGoods.getNxDgGoodsName());


                    nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);
                    nxDepartmentDisGoodsService.delete(linshiDepGoodsEntity.getNxDepartmentDisGoodsId());

                } else {
                    linshiDepGoodsEntity.setNxDdgDisGoodsId(nxGoodsId);
                    linshiDepGoodsEntity.setNxDdgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    linshiDepGoodsEntity.setNxDdgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    NxDistributerFatherGoodsEntity grandEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsGrandId);
                    Integer nxDfgGreatId = grandEntity.getNxDfgFathersFatherId();
                    linshiDepGoodsEntity.setNxDdgDisGoodsGreatId(nxDfgGreatId);
                    nxDepartmentDisGoodsService.update(linshiDepGoodsEntity);
                }
            }
        }


        Map<String, Object> mapNx = new HashMap<>();
        mapNx.put("disGoodsId", nxGoodsId);
        int same = 0;
        List<NxDistributerStandardEntity> nxStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapNx);
        if (nxStandardEntities.size() > 0) {
            for (NxDistributerStandardEntity nxDistributerStandardEntity : nxStandardEntities) {
                if (linshiGoods.getNxDgGoodsStandardname().equals(nxDistributerStandardEntity.getNxDsStandardName())) {
                    same = same + 1;
                }
            }
            if (nxDistributerGoodsEntity.getNxDgGoodsStandardname().equals(linshiGoods.getNxDgGoodsStandardname())) {
                same = same + 1;
            }
        }

        String nxDgGoodsStandardname = nxDistributerGoodsEntity.getNxDgGoodsStandardname();
        String nxDgGoodsStandardname1 = linshiGoods.getNxDgGoodsStandardname();
        if (nxDgGoodsStandardname.equals(nxDgGoodsStandardname1)) {
            same = same + 1;
        }
        if (same == 0) {
            NxDistributerStandardEntity nxDistributerStandardEntity = new NxDistributerStandardEntity();
            nxDistributerStandardEntity.setNxDsStandardName(linshiGoods.getNxDgGoodsStandardname());
            nxDistributerStandardEntity.setNxDsDisGoodsId(nxGoodsId);
            nxDistributerStandardEntity.setNxDsStandardSort(nxStandardEntities.size());
            nxDistributerStandardService.save(nxDistributerStandardEntity);
        }

        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(linshiMap);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsId(nxDgNxGoodsId);
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }


        List<NxDistributerGoodsShelfGoodsEntity> entities = shelfGoodsService.queryShelfForGoodsByParams(linshiMap);
        if (entities.size() > 0) {
            for (NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity : entities) {
//                shelfGoodsEntity.setNxDgsgDisGoodsId(nxGoodsId);
                shelfGoodsService.delete(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
            }
        }

        Map<String, Object> mapGb = new HashMap<>();
        mapGb.put("nxDisGoodsId", lsGoodsId);
        List<GbDistributerGoodsEntity> goodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapGb);
        if (goodsEntities.size() > 0) {
            for (GbDistributerGoodsEntity distributerGoodsEntity : goodsEntities) {
                distributerGoodsEntity.setGbDgNxDistributerGoodsId(nxGoodsId);
                gbDistributerGoodsService.update(distributerGoodsEntity);
            }
        }


        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("nxGoodsId", lsGoodsId);
        List<GbDepartmentDisGoodsEntity> gbDepartmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapDep);
        if (gbDepartmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity disGoodsEntity : gbDepartmentDisGoodsEntities) {
                gbDepartmentDisGoodsService.delete(disGoodsEntity.getGbDepartmentDisGoodsId());
            }
        }

        nxDistributerGoodsService.delete(lsGoodsId);

        return R.ok();
    }

    /**
     * 转换订单的Kg单价和重量（直接修改 nxDoPrice 和 nxDoWeight）
     * 如果商品规格是"斤"，则将单价和重量转换为Kg
     * 单价转换：1斤=0.5Kg，所以1Kg=2斤，Kg单价=斤单价×2
     * 重量转换：1斤=0.5Kg，所以Kg重量=斤重量/2
     * 转换后会更新订单规格为"Kg"，避免重复转换
     * @param order 订单实体
     * @return 是否进行了转换
     */
    private boolean convertOrderPriceToKg(NxDepartmentOrdersEntity order) {
        if (order == null) {
            System.out.println("========= convertOrderPriceToKg 开始 =========");
            System.out.println("❌ 订单对象为 null，无法转换");
            return false;
        }
        
        System.out.println("========= convertOrderPriceToKg 开始 =========");
        System.out.println("订单ID: " + order.getNxDepartmentOrdersId());
        
        // 判断商品的规格是否为"斤"，而不是订单的规格
        NxDistributerGoodsEntity distributerGoodsEntity = order.getNxDistributerGoodsEntity();
        if (distributerGoodsEntity == null) {
            System.out.println("❌ 商品信息为空，无法判断是否需要转换");
            System.out.println("========= convertOrderPriceToKg 结束 =========");
            return false;
        }
        
        String goodsStandard = distributerGoodsEntity.getNxDgGoodsStandardname();
        System.out.println("商品规格(nxDgGoodsStandardname): " + goodsStandard);
        System.out.println("判断依据: 使用商品规格(nxDgGoodsStandardname)来判断是否需要转换");
        
        if (goodsStandard == null || goodsStandard.trim().isEmpty()) {
            System.out.println("❌ 商品规格为空，无需转换");
            System.out.println("========= convertOrderPriceToKg 结束 =========");
            return false;
        }
        
        String goodsStandardTrim = goodsStandard.trim();
        System.out.println("商品规格（去除空格后）: [" + goodsStandardTrim + "]");
        
        // 检查商品规格是否包含"斤"（支持"斤"、"市斤"等）
        boolean goodsContainsJin = goodsStandardTrim.contains("斤");
        System.out.println("商品规格是否包含'斤': " + goodsContainsJin);
        
        if (!goodsContainsJin) {
            // 商品规格不是"斤"，无需转换
            System.out.println("✅ 商品规格不是'斤'，无需转换（商品规格: " + goodsStandardTrim + "）");
            System.out.println("========= convertOrderPriceToKg 结束 =========");
            return false;
        }
        
        // 检查订单规格，如果已经是"Kg"，说明已经转换过，避免重复转换
        String printStandard = order.getNxDoPrintStandard();
        System.out.println("订单打印规格(nxDoPrintStandard): " + printStandard);
        
        if (printStandard != null && !printStandard.trim().isEmpty()) {
            String orderStandardTrim = printStandard.trim().toLowerCase();
            boolean orderContainsKg = orderStandardTrim.contains("kg");
            System.out.println("订单规格是否包含'Kg': " + orderContainsKg);
            
            if (orderContainsKg) {
                System.out.println("✅ 订单规格已经是'Kg'，说明之前已经转换过，避免重复转换");
                System.out.println("========= convertOrderPriceToKg 结束 =========");
                return false;
            }
        }
        
        System.out.println("✅ 商品规格是'斤'，且订单规格不是'Kg'，需要转换为'Kg'");
        
        // 订单规格是"斤"，需要转换
        String price = order.getNxDoPrice();
        String weight = order.getNxDoWeight();
        System.out.println("转换前 - 单价(nxDoPrice): " + price);
        System.out.println("转换前 - 重量(nxDoWeight): " + weight);
        
        boolean hasConversion = false;
        
        // 转换单价（直接修改 nxDoPrice）
        if (price != null && !price.trim().isEmpty()) {
            try {
                BigDecimal priceDecimal = new BigDecimal(price.trim());
                System.out.println("单价转换计算: " + priceDecimal + " × 2 = ?");
                // 1斤 = 0.5Kg，所以1Kg = 2斤，Kg单价 = 斤单价 × 2
                BigDecimal priceKg = priceDecimal.multiply(new BigDecimal("2")).setScale(1, BigDecimal.ROUND_HALF_UP);
                System.out.println("单价转换结果: " + priceDecimal + " × 2 = " + priceKg);
                order.setNxDoPrice(priceKg.toString());
                hasConversion = true;
                System.out.println("✅ 单价已转换: " + price + "元/斤 -> " + priceKg + "元/公斤");
            } catch (NumberFormatException e) {
                System.out.println("❌ 单价格式错误，无法转换: " + price + ", 错误: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ 单价为空，跳过单价转换");
        }
        
        // 转换重量（直接修改 nxDoWeight）
        if (weight != null && !weight.trim().isEmpty()) {
            try {
                BigDecimal weightDecimal = new BigDecimal(weight.trim());
                System.out.println("重量转换计算: " + weightDecimal + " ÷ 2 = ?");
                // 1斤 = 0.5Kg，所以Kg重量 = 斤重量 / 2
                BigDecimal weightKg = weightDecimal.divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP);
                System.out.println("重量转换结果: " + weightDecimal + " ÷ 2 = " + weightKg);
                order.setNxDoWeight(weightKg.toString());
                hasConversion = true;
                System.out.println("✅ 重量已转换: " + weight + "斤 -> " + weightKg + "公斤");
            } catch (NumberFormatException e) {
                System.out.println("❌ 重量格式错误，无法转换: " + weight + ", 错误: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ 重量为空，跳过重量转换");
        }
        
        // 如果进行了转换，更新订单规格为"Kg"，避免重复转换
        if (hasConversion) {
            // 将订单规格从"斤"改为"Kg"
            String oldStandard = printStandard;
            String newStandard = printStandard.replace("斤", "Kg").replace("市斤", "Kg");
            order.setNxDoPrintStandard(newStandard);
            System.out.println("✅ 订单规格已更新: [" + oldStandard + "] -> [" + newStandard + "]");
            System.out.println("说明: 更新订单规格后，下次调用时订单规格已经是'Kg'，不会再转换，避免重复转换");
        } else {
            System.out.println("⚠️ 没有进行任何转换（单价和重量都为空或格式错误）");
        }
        
        System.out.println("========= convertOrderPriceToKg 结束 =========");
        return hasConversion;
    }
    
    /**
     * 转换订单的斤单价和重量（直接修改 nxDoPrice 和 nxDoWeight）
     * 如果商品规格是"Kg"或"kg"，则将单价和重量转换为斤
     * 单价转换：1Kg = 2斤，所以斤单价 = Kg单价 / 2
     * 重量转换：1Kg = 2斤，所以斤重量 = Kg重量 × 2
     * 转换后会更新订单规格为"斤"，避免重复转换
     * @param order 订单实体
     * @return 是否进行了转换
     */
    /**
     * 将订单从公斤转换为斤（只处理商品规格是"斤"的订单）
     * 修改3个字段：nxDoPrice（单价）、nxDoWeight（重量）、nxDoPrintStandard（打印规格）
     * 注意：nxDoStandard（客户订货规格）不能修改
     */
    private boolean convertOrderPriceToJin(NxDepartmentOrdersEntity order) {
        if (order == null) {
            System.out.println("========= convertOrderPriceToJin 开始 =========");
            System.out.println("❌ 订单对象为 null，无法转换");
            return false;
        }
        
        System.out.println("========= convertOrderPriceToJin 开始 =========");
        System.out.println("订单ID: " + order.getNxDepartmentOrdersId());
        
        // 判断商品的规格是否为"斤"
        NxDistributerGoodsEntity distributerGoodsEntity = order.getNxDistributerGoodsEntity();
        if (distributerGoodsEntity == null) {
            System.out.println("❌ 商品信息为空，无法判断是否需要转换");
            System.out.println("========= convertOrderPriceToJin 结束 =========");
            return false;
        }
        
        String goodsStandard = distributerGoodsEntity.getNxDgGoodsStandardname();
        System.out.println("商品规格(nxDgGoodsStandardname): " + goodsStandard);
        System.out.println("判断依据: 只处理商品规格是'斤'的订单");
        
        if (goodsStandard == null || goodsStandard.trim().isEmpty()) {
            System.out.println("❌ 商品规格为空，无需转换");
            System.out.println("========= convertOrderPriceToJin 结束 =========");
            return false;
        }
        
        String goodsStandardTrim = goodsStandard.trim();
        System.out.println("商品规格（去除空格后）: [" + goodsStandardTrim + "]");
        
        // 检查商品规格是否包含"斤"
        boolean goodsContainsJin = goodsStandardTrim.contains("斤");
        System.out.println("商品规格是否包含'斤': " + goodsContainsJin);
        
        if (!goodsContainsJin) {
            // 商品规格不是"斤"，无需转换
            System.out.println("✅ 商品规格不是'斤'，无需转换（商品规格: " + goodsStandardTrim + "）");
            System.out.println("========= convertOrderPriceToJin 结束 =========");
            return false;
        }
        
        // 检查订单打印规格，如果已经是"斤"，说明已经转换过，避免重复转换
        String printStandard = order.getNxDoPrintStandard();
        System.out.println("订单打印规格(nxDoPrintStandard): " + printStandard);
        System.out.println("注意: nxDoStandard（客户订货规格）不修改，值为: " + order.getNxDoStandard());
        
        if (printStandard != null && !printStandard.trim().isEmpty()) {
            String printStandardTrim = printStandard.trim();
            boolean printContainsJin = printStandardTrim.contains("斤");
            System.out.println("订单打印规格是否包含'斤': " + printContainsJin);
            
            if (printContainsJin) {
                System.out.println("✅ 订单打印规格已经是'斤'，说明之前已经转换过，避免重复转换");
                System.out.println("========= convertOrderPriceToJin 结束 =========");
                return false;
            }
        }
        
        System.out.println("✅ 商品规格是'斤'，且订单打印规格不是'斤'，需要从公斤转换为'斤'");
        
        // 从公斤转换为斤：修改 nxDoPrice、nxDoWeight、nxDoPrintStandard
        String price = order.getNxDoPrice();
        String weight = order.getNxDoWeight();
        System.out.println("转换前 - 单价(nxDoPrice): " + price);
        System.out.println("转换前 - 重量(nxDoWeight): " + weight);
        
        boolean hasConversion = false;
        
        // 转换单价：从公斤单价改为斤单价（除以2，因为1Kg = 2斤）
        if (price != null && !price.trim().isEmpty()) {
            try {
                BigDecimal priceDecimal = new BigDecimal(price.trim());
                System.out.println("单价转换计算: " + priceDecimal + " ÷ 2 = ?");
                // 1Kg = 2斤，所以斤单价 = Kg单价 / 2
                BigDecimal priceJin = priceDecimal.divide(new BigDecimal("2"), 1, BigDecimal.ROUND_HALF_UP);
                System.out.println("单价转换结果: " + priceDecimal + " ÷ 2 = " + priceJin);
                order.setNxDoPrice(priceJin.toString());
                hasConversion = true;
                System.out.println("✅ 单价已转换: " + price + "元/公斤 -> " + priceJin + "元/斤");
            } catch (NumberFormatException e) {
                System.out.println("❌ 单价格式错误，无法转换: " + price + ", 错误: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ 单价为空，跳过单价转换");
        }
        
        // 转换重量：从公斤重量改为斤重量（乘以2，因为1Kg = 2斤）
        if (weight != null && !weight.trim().isEmpty()) {
            try {
                BigDecimal weightDecimal = new BigDecimal(weight.trim());
                System.out.println("重量转换计算: " + weightDecimal + " × 2 = ?");
                // 1Kg = 2斤，所以斤重量 = Kg重量 × 2
                BigDecimal weightJin = weightDecimal.multiply(new BigDecimal("2")).setScale(1, BigDecimal.ROUND_HALF_UP);
                System.out.println("重量转换结果: " + weightDecimal + " × 2 = " + weightJin);
                order.setNxDoWeight(weightJin.toString());
                hasConversion = true;
                System.out.println("✅ 重量已转换: " + weight + "公斤 -> " + weightJin + "斤");
            } catch (NumberFormatException e) {
                System.out.println("❌ 重量格式错误，无法转换: " + weight + ", 错误: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ 重量为空，跳过重量转换");
        }
        
        // 如果进行了转换，更新订单打印规格为"斤"，避免重复转换
        if (hasConversion) {
            // 将订单打印规格从"Kg"改为"斤"
            String oldPrintStandard = printStandard != null ? printStandard : "";
            String newPrintStandard = oldPrintStandard.replaceAll("(?i)kg", "斤");
            order.setNxDoPrintStandard(newPrintStandard);
            System.out.println("✅ 订单打印规格已更新: [" + oldPrintStandard + "] -> [" + newPrintStandard + "]");
            System.out.println("说明: 更新订单打印规格后，下次调用时订单打印规格已经是'斤'，不会再转换，避免重复转换");
            System.out.println("注意: nxDoStandard（客户订货规格）保持不变，值为: " + order.getNxDoStandard());
        } else {
            System.out.println("⚠️ 没有进行任何转换（单价和重量都为空或格式错误）");
        }
        
        System.out.println("========= convertOrderPriceToJin 结束 =========");
        return hasConversion;
    }
    
    /**
     * 批量转换订单列表的Kg单价
     * @param orders 订单列表
     */
    /**
     * 批量转换订单列表的Kg单价和重量
     * @param orders 订单列表
     * @return 被转换的订单列表（只包含真正被转换的订单）
     */
    private List<NxDepartmentOrdersEntity> convertOrdersPriceToKg(List<NxDepartmentOrdersEntity> orders) {
        List<NxDepartmentOrdersEntity> convertedOrders = new ArrayList<>();
        if (orders == null || orders.isEmpty()) {
            return convertedOrders;
        }
        System.out.println("========= 批量转换开始（斤转公斤），订单总数: " + orders.size() + " =========");
        int convertedCount = 0;
        for (NxDepartmentOrdersEntity order : orders) {
            boolean converted = convertOrderPriceToKg(order);
            if (converted) {
                convertedOrders.add(order);
                convertedCount++;
            }
        }
        System.out.println("========= 批量转换结束，实际转换数量: " + convertedCount + "/" + orders.size() + " =========");
        return convertedOrders;
    }
    
    /**
     * 批量转换订单列表的斤单价和重量
     * @param orders 订单列表
     * @return 被转换的订单列表（只包含真正被转换的订单）
     */
    private List<NxDepartmentOrdersEntity> convertOrdersPriceToJin(List<NxDepartmentOrdersEntity> orders) {
        List<NxDepartmentOrdersEntity> convertedOrders = new ArrayList<>();
        if (orders == null || orders.isEmpty()) {
            return convertedOrders;
        }
        System.out.println("========= 批量转换开始（公斤转斤），订单总数: " + orders.size() + " =========");
        int convertedCount = 0;
        for (NxDepartmentOrdersEntity order : orders) {
            boolean converted = convertOrderPriceToJin(order);
            if (converted) {
                convertedOrders.add(order);
                convertedCount++;
            }
        }
        System.out.println("========= 批量转换结束，实际转换数量: " + convertedCount + "/" + orders.size() + " =========");
        return convertedOrders;
    }
    
    /**
     * 批量保存订单到数据库（只保存被转换的订单）
     * @param orders 订单列表（只包含被转换的订单）
     */
    private void saveOrders(List<NxDepartmentOrdersEntity> orders) {
        if (orders == null || orders.isEmpty()) {
            System.out.println("没有需要保存的订单（没有订单被转换）");
            return;
        }
        System.out.println("========= 开始保存订单到数据库，数量: " + orders.size() + " =========");
        for (NxDepartmentOrdersEntity order : orders) {
            try {
                nxDepartmentOrdersService.update(order);
                System.out.println("✅ 订单ID: " + order.getNxDepartmentOrdersId() + 
                                 " 已保存到数据库");
            } catch (Exception e) {
                System.out.println("❌ 保存订单ID: " + order.getNxDepartmentOrdersId() + 
                                 " 失败: " + e.getMessage());
            }
        }
        System.out.println("========= 订单保存完成 =========");
    }

    /**
     * 智能匹配单位规格
     * 支持同义词匹配，例如：件=箱，盒=箱等
     * @param orderStandard 订单规格（客户输入的规格，如"件"）
     * @param cartonUnit 大包装单位（商品的外包装单位，如"箱"）
     * @return 是否匹配
     */
    private boolean isStandardMatch(String orderStandard, String cartonUnit) {
        System.out.println("======== 智能匹配单位规格开始 ========");
        System.out.println("订单规格: [" + orderStandard + "]");
        System.out.println("包装单位: [" + cartonUnit + "]");
        
        if (orderStandard == null || cartonUnit == null) {
            System.out.println("❌ 匹配失败: 订单规格或包装单位为null");
            return false;
        }
        
        // 去除空格并转为小写进行比较
        String orderStd = orderStandard.trim();
        String carton = cartonUnit.trim();
        
        System.out.println("去除空格后 - 订单规格: [" + orderStd + "], 包装单位: [" + carton + "]");
        
        // 1. 完全匹配
        if (orderStd.equals(carton)) {
            System.out.println("✅ 完全匹配成功: [" + orderStd + "] == [" + carton + "]");
            return true;
        }
        
        // 2. 同义词映射：定义常见的单位同义词
        // 件 = 箱（订货"件"视为大包装"箱"）
        Map<String, Set<String>> synonymMap = new HashMap<>();
        synonymMap.put("箱", new HashSet<>(Arrays.asList("件")));
        synonymMap.put("件", new HashSet<>(Arrays.asList("箱")));

        System.out.println("开始检查同义词匹配...");
        
        // 3. 检查同义词匹配
        // 如果订单规格的同义词组包含包装单位，或者包装单位的同义词组包含订单规格，则认为匹配
        Set<String> orderSynonyms = synonymMap.get(orderStd);
        Set<String> cartonSynonyms = synonymMap.get(carton);
        
        if (orderSynonyms != null && orderSynonyms.contains(carton)) {
            System.out.println("✅ 智能匹配成功: 订单规格[" + orderStd + "] 的同义词组包含包装单位[" + carton + "]");
            System.out.println("订单规格[" + orderStd + "] 的同义词组: " + orderSynonyms);
            return true;
        }
        
        if (cartonSynonyms != null && cartonSynonyms.contains(orderStd)) {
            System.out.println("✅ 智能匹配成功: 包装单位[" + carton + "] 的同义词组包含订单规格[" + orderStd + "]");
            System.out.println("包装单位[" + carton + "] 的同义词组: " + cartonSynonyms);
            return true;
        }
        
        System.out.println("❌ 匹配失败: 订单规格[" + orderStd + "] 与 包装单位[" + carton + "] 不匹配");
        System.out.println("======== 智能匹配单位规格结束 ========");
        return false;
    }

}