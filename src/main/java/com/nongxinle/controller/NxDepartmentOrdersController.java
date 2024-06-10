package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-21 21:51
 */

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.PageUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.getWeek;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
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
    private NxRestrauntOrdersService nxRestrauntOrdersService;
    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;
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



    @RequestMapping(value = "/disUpdateBuyingPrice", method = RequestMethod.POST)
    @ResponseBody
    public R disUpdateBuyingPrice(@RequestBody NxDistributerGoodsEntity disGoods) {
        nxDistributerGoodsService.update(disGoods);

        //nxOrder
        Integer distributerGoodsId = disGoods.getNxDistributerGoodsId();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", distributerGoodsId);
        map.put("equalStatus", 0);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                if (ordersEntity.getNxDoWeight() != null) {
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


//        List<GbDepartmentOrdersEntity> ordersEntities1 = gbDepartmentOrdersService.queryGbPurGoodsForNxGoodsId(map);


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
        //2.添加规格
        if (!disGoodsEntity.getNxDgGoodsStandardname().equals(orders.getNxDoStandard())) {
            Map<String, Object> mapStand = new HashMap<>();
            mapStand.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
            mapStand.put("standardName", orders.getNxDoStandard());
            List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapStand);
            if (distributerStandardEntities.size() == 0) {
                NxDistributerStandardEntity standardEntity = new NxDistributerStandardEntity();
                standardEntity.setNxDsDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
                standardEntity.setNxDsStandardName(orders.getNxDoStandard());
                nxDistributerStandardService.save(standardEntity);
            }
        }

        aaa.setNxDistributerGoodsEntityList(new ArrayList<>());
        return R.ok().put("data", aaa);
    }

    @ResponseBody
    @RequestMapping(value = "/uploadDepOrderData1", produces = "text/html;charset=UTF-8")
    public R uploadDepOrderData1(@RequestParam("file") MultipartFile file,
                                 @RequestParam("depFatherId") Integer depFatherId,
                                 @RequestParam("depId") Integer depId,
                                 @RequestParam("disId") Integer disId,
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
                    ordersEntity.setNxDoPurchaseUserId(-1);
                    ordersEntity.setNxDepartmentOrdersId(-1);
                    ordersEntity.setNxDistributerGoodsEntityList(new ArrayList<>());

                    //
                    Map<String, Object> mapZero = new HashMap<>();
                    mapZero.put("disId", ordersEntity.getNxDoDistributerId());
                    mapZero.put("searchStr", goodsName);
                    mapZero.put("standard", ordersEntity.getNxDoStandard());
                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
                    if (distributerGoodsEntitiesZero.size() == 0) {
                        Map<String, Object> mapOne = new HashMap<>();
                        mapOne.put("disId", ordersEntity.getNxDoDistributerId());
                        mapOne.put("searchStr", goodsName);
                        List<NxDistributerGoodsEntity> distributerGoodsEntitiesOne = nxDistributerGoodsService.queryDisGoodsByName(mapOne);
                        if (distributerGoodsEntitiesOne.size() == 0) {
                            //1, 查拼音
                            String pinyinString = "";
                            for (int st = 0; st < goodsName.length(); st++) {
                                String str = goodsName.substring(st, st + 1);
                                if (str.matches("[\u4E00-\u9FFF]")) {
                                    pinyinString = hanziToPinyin(goodsName);
                                }
                            }
                            Map<String, Object> mapTwo = new HashMap<>();
                            mapTwo.put("disId", ordersEntity.getNxDoDistributerId());
                            mapTwo.put("searchPinyin", pinyinString);
                            List<NxDistributerGoodsEntity> disGoodsByNamePinyin = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
                            if (disGoodsByNamePinyin.size() == 0) {
                                //查别名
                                Map<String, Object> mapA = new HashMap<>();
                                mapA.put("disId", ordersEntity.getNxDoDistributerId());
                                mapA.put("alias", goodsName);
                                List<NxDistributerGoodsEntity> distributerGoodsEntitiesA = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
                                if (distributerGoodsEntitiesA.size() == 0) {
                                    ordersEntity.setNxDepartmentOrdersId(-1);
                                    returnList.add(ordersEntity);
                                } else if (distributerGoodsEntitiesA.size() == 1) {
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
                                    returnList.add(aaaExcel(ordersEntity, distributerGoodsEntity, goodsName));

                                } else {
                                    ordersEntity.setNxDepartmentOrdersId(-1);
                                    ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesA);
                                    returnList.add(ordersEntity);
                                }
                            } else if (disGoodsByNamePinyin.size() == 1) {

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
                                returnList.add(aaaExcel(ordersEntity, distributerGoodsEntity, goodsName));

                            } else {
                                ordersEntity.setNxDepartmentOrdersId(-1);
                                ordersEntity.setNxDistributerGoodsEntityList(disGoodsByNamePinyin);
                                returnList.add(ordersEntity);
                            }
                        } else if (distributerGoodsEntitiesOne.size() == 1) {

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
                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
                            returnList.add(aaaExcel(ordersEntity, distributerGoodsEntity, goodsName));

                        } else {
                            ordersEntity.setNxDepartmentOrdersId(-1);
                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
                            returnList.add(ordersEntity);
                        }

                    } else if (distributerGoodsEntitiesZero.size() == 1) {
                        NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesZero.get(0);
                        returnList.add(aaaExcel(ordersEntity, disGoodsEntity, goodsName));
                    } else {
                        ordersEntity.setNxDepartmentOrdersId(-1);
                        ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesZero);
                        returnList.add(ordersEntity);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("rrrrrrlisisdidi" + returnList.size());
        return R.ok().put("data", returnList);
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
                    ordersEntity.setNxDoIsAgent(disUserId);
                    ordersEntity.setNxDoStatus(-2);
                    ordersEntity.setNxDistributerGoodsEntityList(new ArrayList<>());

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
                                    returnList.add(aaaTemp(ordersEntity));
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


    private NxDepartmentOrdersEntity aaaExcel(NxDepartmentOrdersEntity ordersEntity,
                                              NxDistributerGoodsEntity disGoodsEntity, String goodsName) {
        NxDepartmentOrdersEntity order = new NxDepartmentOrdersEntity();
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDoDepartmentId(ordersEntity.getNxDoDepartmentId());
        order.setNxDoDepartmentFatherId(ordersEntity.getNxDoDepartmentFatherId());
        order.setNxDoDistributerId(ordersEntity.getNxDoDistributerId());
        order.setNxDoQuantity(ordersEntity.getNxDoQuantity());
        order.setNxDoStandard(ordersEntity.getNxDoStandard());
        order.setNxDoRemark(ordersEntity.getNxDoRemark());
        order.setNxDoStatus(0);
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
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
        order.setNxDoCostPriceLevel("0");
        order.setNxDoPurchaseGoodsId(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceUpdate());
        order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPrice());
        order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
        order.setNxDoProfitSubtotal("0");
        order.setNxDoProfitScale("0");
        order.setNxDoArriveWhatDay(getWeek(0));
        if (ordersEntity.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname())) {
            order.setNxDoWeight(ordersEntity.getNxDoQuantity());
            BigDecimal decimal = new BigDecimal(ordersEntity.getNxDoQuantity());
            BigDecimal decimal1 = new BigDecimal(disGoodsEntity.getNxDgBuyingPrice());
            BigDecimal decimal2 = decimal.multiply(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
            order.setNxDoCostSubtotal(decimal2.toString());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("depId", ordersEntity.getNxDoDepartmentId());
        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
        if (departmentDisGoodsEntity != null) {
            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
        }

        if (ordersEntity.getNxDoPurchaseUserId() != -1) {
            //临时用 purUserid 赋值 前面 order 的 id
            Integer nxDoPurchaseUserId = ordersEntity.getNxDoPurchaseUserId();
            NxDepartmentOrdersEntity beforOrder = nxDepartmentOrdersService.queryObject(nxDoPurchaseUserId);

            order.setNxDoTodayOrder(beforOrder.getNxDoTodayOrder());

            Map<String, Object> mapU = new HashMap<>();
            mapU.put("depId", ordersEntity.getNxDoDepartmentId());
            mapU.put("todayOrder", beforOrder.getNxDoTodayOrder());
            mapU.put("status", 3);
            mapU.put("orderBy", "time");
            System.out.println("maporodoerer" + mapU);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(mapU);
            if (ordersEntities.size() > 0) {
                System.out.println("orooro" + ordersEntities.size());
                for (int i = 0; i < ordersEntities.size(); i++) {
                    NxDepartmentOrdersEntity ordersEn = ordersEntities.get(i);
                    int i1 = beforOrder.getNxDoTodayOrder() + i + 2;
                    System.out.println("whisisisisisisisis====" + i1);
                    ordersEn.setNxDoTodayOrder(i1);
                    nxDepartmentOrdersService.update(ordersEn);
                }
            }

            beforOrder.setNxDoTodayOrder(beforOrder.getNxDoTodayOrder() + 1);
            nxDepartmentOrdersService.update(beforOrder);

        } else {
            Map<String, Object> mapss = new HashMap<>();
            mapss.put("depId", ordersEntity.getNxDoDepartmentId());
            mapss.put("status", 3);
            int orderOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapss);
            order.setNxDoTodayOrder(orderOrder + 1);
        }
        nxDepartmentOrdersService.save(order);
        ordersEntity.setNxDepartmentOrdersId(order.getNxDepartmentOrdersId());

        //auto
        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
            savePurGoodsAuto(order);
        }

        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDistributerGoodsEntity(distributerGoodsEntity);
        System.out.println("orderrroooroorretututu" + order);
        return order;
    }


    private NxDepartmentOrdersEntity saveOneOrder(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
        System.out.println("saveONeOrderereerereeqonenenneorere");
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDoStatus(0);
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
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
        order.setNxDoCostPriceLevel("0");
        order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
        order.setNxDoPurchaseGoodsId(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceUpdate());
        order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPrice());
        order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
        order.setNxDoProfitSubtotal("0");
        order.setNxDoProfitScale("0");
        order.setNxDoArriveWhatDay(getWeek(0));
        if (order.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname())) {
            order.setNxDoWeight(order.getNxDoQuantity());
            BigDecimal decimal = new BigDecimal(order.getNxDoQuantity());
            BigDecimal decimal1 = new BigDecimal(disGoodsEntity.getNxDgBuyingPrice());
            BigDecimal decimal2 = decimal.multiply(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
            order.setNxDoCostSubtotal(decimal2.toString());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("depId", order.getNxDoDepartmentId());
        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
        if (departmentDisGoodsEntity != null) {
            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
        }
//
//        if (order.getNxDoPurchaseUserId() != -1) {
//            //临时用 purUserid 赋值 前面 order 的 id
//            Integer nxDoPurchaseUserId = order.getNxDoPurchaseUserId();
//            NxDepartmentOrdersEntity beforOrder = nxDepartmentOrdersService.queryObject(nxDoPurchaseUserId);
//
////            order.setNxDoTodayOrder(beforOrder.getNxDoTodayOrder());
////
////            Map<String, Object> mapU = new HashMap<>();
////            mapU.put("depId", order.getNxDoDepartmentId());
////            mapU.put("todayOrder", beforOrder.getNxDoTodayOrder());
////            mapU.put("status", 3);
////            mapU.put("orderBy", "time");
////            System.out.println("maporodoerer" + mapU);
////            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(mapU);
////            if (ordersEntities.size() > 0) {
////                System.out.println("orooro" + ordersEntities.size());
////                for (int i = 0; i < ordersEntities.size(); i++) {
////                    NxDepartmentOrdersEntity ordersEn = ordersEntities.get(i);
////                    int i1 = beforOrder.getNxDoTodayOrder() + i + 2;
////                    ordersEn.setNxDoTodayOrder(i1);
////                    nxDepartmentOrdersService.update(ordersEn);
////                }
////            }
////
////            beforOrder.setNxDoTodayOrder(beforOrder.getNxDoTodayOrder() + 1);
////            nxDepartmentOrdersService.update(beforOrder);
////
////        } else {
////            Map<String, Object> mapss = new HashMap<>();
////            mapss.put("depId", order.getNxDoDepartmentId());
////            mapss.put("status", 3);
////            int orderOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapss);
////            order.setNxDoTodayOrder(orderOrder + 1);
////        }

        Map<String, Object> mapss = new HashMap<>();
        mapss.put("depId", order.getNxDoDepartmentId());
        mapss.put("status", 3);
        int orderOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapss);
        order.setNxDoTodayOrder(orderOrder + 1);
        nxDepartmentOrdersService.save(order);

        //auto
        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
            savePurGoodsAuto(order);
        }
        List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDistributerGoodsEntity(distributerGoodsEntity);
        System.out.println("orderrroooroorretututuONeorder" + order);
        return order;
    }


    private NxDepartmentOrdersEntity choiceGoodsOrder(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDoStatus(0);
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
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
        order.setNxDoCostPriceLevel("0");
        order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
        order.setNxDoPurchaseGoodsId(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceUpdate());
        order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPrice());
        order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
        order.setNxDoProfitSubtotal("0");
        order.setNxDoProfitScale("0");
        order.setNxDoArriveWhatDay(getWeek(0));
        if (order.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname())) {
            order.setNxDoWeight(order.getNxDoQuantity());
            BigDecimal decimal = new BigDecimal(order.getNxDoQuantity());
            BigDecimal decimal1 = new BigDecimal(disGoodsEntity.getNxDgBuyingPrice());
            BigDecimal decimal2 = decimal.multiply(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
            order.setNxDoCostSubtotal(decimal2.toString());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("depId", order.getNxDoDepartmentId());
        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
        if (departmentDisGoodsEntity != null) {
            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
        }

        //auto
        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
            savePurGoodsAuto(order);
        }
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDistributerGoodsEntity(distributerGoodsEntity);
        nxDepartmentOrdersService.update(order);
        return order;
    }

    private NxDepartmentOrdersEntity aaaTemp(NxDepartmentOrdersEntity order) {

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
        order.setNxDoCostPriceLevel("0");

        order.setNxDoProfitSubtotal("0");
        order.setNxDoProfitScale("0");
        order.setNxDoArriveWhatDay(getWeek(0));
        Map<String, Object> mapss = new HashMap<>();
        mapss.put("depId", order.getNxDoDepartmentId());
        mapss.put("status", 3);
        int orderOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapss);
        order.setNxDoTodayOrder(orderOrder + 1);
        nxDepartmentOrdersService.save(order);
        System.out.println("orderrroooroorretututuTempp" + order);
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


    @RequestMapping(value = "/getHaveOutCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getHaveOutCataGoods(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("equalPurStatus", 4);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("goodsType", goodsType);
        System.out.println("getHaveOutCataGoods" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        return R.ok().put("data", fatherGoodsEntities);
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

        map.put("equalPurStatus", 1);
        map.put("goodsType", goodsType);
        System.out.println("fatheridiidididi" + map);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("grandArr", greatGrandGoods);

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
        mapOk.put("equalPurStatus", 4);
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

            if (oldOrderEntity.getNxDoPrice() != null) {
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

                if (ordersEntity.getNxDoPrice() != null) {
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
        mapFin.put("equalPurStatus", 4);
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
        map.put("equalPurStatus", 4);
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
        mapFin.put("equalPurStatus", 4);
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
        mapOk.put("equalPurStatus", 4);
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
        map.put("equalPurStatus", 4);
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


    @RequestMapping(value = "/disGetWaitStockGoodsDeps", method = RequestMethod.POST)
    @ResponseBody
    public R disGetWaitStockGoodsDeps(Integer disId, String goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("goodsType", goodsType);
        System.out.println("depeppepe" + map);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(map);
        List<GbDepartmentEntity> gbDepartmentEntity = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("status", 3);
                mapDep.put("goodsType", goodsType);
                mapDep.put("depFatherId", departmentEntity.getNxDepartmentId());
                Integer count0 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                mapDep.put("purStatus", 4);
                Integer count1 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                mapDep.put("equalPurStatus", 4);
                Integer count2 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                departmentEntity.setNxDepartmentAddCount(count0);
                departmentEntity.setNxDepartmentPurOrderCount(count1);
                departmentEntity.setNxDepartmentNeedNotPurOrderCount(count2);
            }
        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDep", departmentEntities);
        mapR.put("gbDep", gbDepartmentEntity);
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
            map.put("equalPurStatus", 4);
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
                    mapDep.put("equalPurStatus", 4);
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

        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", disId);
        mapFin.put("status", 3);
        mapFin.put("goodsType", goodsType);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        mapR.put("depOrdersWait", count);

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
        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("equalPurStatus", 4);
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
        map.put("equalPurStatus", 1);
        map.put("goodsType", goodsType);
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
        mapFin.put("equalPurStatus", 4);
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
        mapOk.put("equalPurStatus", 4);
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
        mapOk.put("equalPurStatus", 4);
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


    @RequestMapping(value = "/disGetTypePrepareOrder/{disId}")
    @ResponseBody
    public R disGetTypePrepareOrder(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("purType", 0);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);
        //countOrderStatus
        fatherGoodsEntities = everyStockFatherGoodsOrderStatus(fatherGoodsEntities, disId, map);


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
        map1.put("inputType", 2);
        System.out.println("wxxxxxxx" + map1);
        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        // 打印
        map1.put("inputType", 0);
        int printCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("purType", 0);
        mapOk.put("purStatus", 4);
        //出库中
        int stockCountDo = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("purStatus", 5);
        mapOk.put("dayuPurStatus", 1);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        //订货完成
        mapOk.put("purType", 1);
        mapOk.put("inputType", 1);
        mapOk.put("batchId", 1);
        System.out.println("wxok" + mapOk);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("inputType", 2);
        System.out.println("wxok" + mapOk);
        int wxCountAutoOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        //打印完成
        mapOk.put("batchId", -1);
        mapOk.put("inputType", 0);
        mapOk.put("weightStatusEqual", 1);
        int printCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("newCount", newCount);
        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("stockCountDo", stockCountDo);
        mapR.put("wxCount", wxCount);
        mapR.put("wxCountOk", wxCountOk);
        mapR.put("wxCountAuto", wxCountAuto);
        mapR.put("wxCountAutoOk", wxCountAutoOk);
        mapR.put("printCount", printCount);
        mapR.put("printCountOk", printCountOk);

        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/disGetTypePrepareOrderHaveStockOut/{disId}")
    @ResponseBody
    public R disGetTypePrepareOrderHaveStockOut(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("equalPurStatus", 4);
        map.put("purType", 0);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);

        //countOrderStatus
        fatherGoodsEntities = everyStockFatherGoodsOrderStatus(fatherGoodsEntities, disId, map);


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
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);
        map1.put("inputType", 2);
        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);
        // 打印
        map1.put("inputType", 0);
        int printCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("purType", 0);
        mapOk.put("purStatus", 4);
        //出库中
        System.out.println("chukuchzonggnng" + mapOk);
        int stockCountDo = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        mapOk.put("dayuPurStatus", 3);
        mapOk.put("purStatus", 5);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        //订货完成
        mapOk.put("purType", 1);
        mapOk.put("inputType", 1);
        mapOk.put("weight", 1);
        mapOk.put("batchId", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("inputType", 2);
        int wxCountAutoOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        //打印完成
        mapOk.put("batchId", -1);
        mapOk.put("inputType", 0);
        mapOk.put("weightStatusEqual", 1);
        int printCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("newCount", newCount);
        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("stockCountDo", stockCountDo);
        mapR.put("wxCount", wxCount);
        mapR.put("wxCountOk", wxCountOk);
        mapR.put("wxCountAuto", wxCountAuto);
        mapR.put("wxCountAutoOk", wxCountAutoOk);
        mapR.put("printCount", printCount);
        mapR.put("printCountOk", printCountOk);

        return R.ok().put("data", mapR);

    }

    private List<NxDistributerFatherGoodsEntity> everyFatherGoodsOrderStatus(List<NxDistributerFatherGoodsEntity> fatherGoodsList,
                                                                             Integer disId) {
        for (NxDistributerFatherGoodsEntity fatherGoods : fatherGoodsList) {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", disId);
            map.put("status", 3);
            map.put("equalPurStatus", 1);
            map.put("purType", 0);
            map.put("grandId", fatherGoods.getNxDistributerFatherGoodsId());
            Integer integerNew = nxDepartmentOrdersService.queryDepOrdersAcount(map);

            map.put("equalPurStatus", 3);
            Integer integerFinish = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            map.put("equalPurStatus", 2);

            Integer integerDoing = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            System.out.println("countdkfafdafa" + integerFinish);

            fatherGoods.setNewOrderCount(integerNew);
            fatherGoods.setPurOrderCount(integerFinish);
            fatherGoods.setStockOrderCount(integerDoing);

        }

        return fatherGoodsList;
    }

    private List<NxDistributerFatherGoodsEntity> everyStockFatherGoodsOrderStatus(List<NxDistributerFatherGoodsEntity> fatherGoodsList,
                                                                                  Integer disId, Map<String, Object> map) {
        for (NxDistributerFatherGoodsEntity fatherGoods : fatherGoodsList) {
            map.put("grandId", fatherGoods.getNxDistributerFatherGoodsId());
            System.out.println("pareee" + map);
            Integer integerNew = nxDepartmentOrdersService.queryDepOrdersAcount(map);

            fatherGoods.setNewOrderCount(integerNew);
//

        }

        return fatherGoodsList;
    }

    @RequestMapping(value = "/disGetPurOnlyGoods/{disId}")
    @ResponseBody
    public R disGetPurOnlyGoods(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purchaseType", 0);
        map.put("status", 3);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerPurchaseGoodsService.queryDisPurchaseGoods(map);


        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("purStatus", 3);
        map1.put("purType", -1);
        System.out.println("dafdafdksaaaafadsdfasfasfdas" + map1);
        int count0 = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        map1.put("purStatus", 5);
        map1.put("purType", 0);
        int count = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        map1.put("purType", 1);
        System.out.println("dafdafdksa" + map1);
        int count1 = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);


        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("equalStatus", 2);
        mapOk.put("purType", 0);
        int countOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("purType", 1);
        System.out.println("dafdafdksa" + map1);
        int count1Ok = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);


        Map<String, Object> mapPurOk = new HashMap<>();
        mapPurOk.put("disId", disId);
        mapPurOk.put("equalStatus", 2);
        mapPurOk.put("type", 0);
        int count2OK = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(mapPurOk);

        Map<String, Object> mapPur = new HashMap<>();
        mapPur.put("disId", disId);
        mapPur.put("status", 3);
        mapPur.put("type", 0);
        int count2 = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(mapPur);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("newCount", count0);
        mapR.put("stockCount", count);
        mapR.put("stockCountOk", countOK);
        mapR.put("purCount", count1);
        mapR.put("purGoodsCount", count2);
        mapR.put("purGoodsCountOk", count2OK);
        mapR.put("purCountOk", count1Ok);
        return R.ok().put("data", mapR);

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
        if (depOrders.getNxDoExpcetPrice() != null) {

            BigDecimal expectPrice = new BigDecimal(depOrders.getNxDoExpcetPrice());
            BigDecimal doPrice = new BigDecimal(depOrders.getNxDoPrice());
            BigDecimal subtract = doPrice.subtract(expectPrice);
            depOrders.setNxDoPriceDifferent(subtract.toString());
        }

        System.out.println("sorderss" + depOrders);
        nxDepartmentOrdersService.update(depOrders);

        return R.ok();
    }


//
//    @RequestMapping(value = "/saveToFillWeightOrderPurchase", method = RequestMethod.POST)
//    @ResponseBody
//    public R saveToFillWeightOrderPurchase(@RequestBody NxDepartmentOrdersEntity ordersEntity) {
//        System.out.println("fafdkfajfs;sa");
//        if (ordersEntity.getNxDoWeight().equals("0.0") || ordersEntity.getNxDoWeight().equals("0.") || ordersEntity.getNxDoWeight().equals("0") || ordersEntity.getNxDoWeight().length() == 0) {
//            ordersEntity.setNxDoWeight("-1");
//            ordersEntity.setNxDoSubtotal("-1");
//        }
//        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishPurchase());
//        nxDepartmentOrdersService.update(ordersEntity);
//
//        if (ordersEntity.getNxDoNxRestrauntOrderId() != null && ordersEntity.getNxDoNxRestrauntOrderId() > 0) {
//            transUpdateNxRestrauntOrder(ordersEntity);
//        }
//        if (ordersEntity.getNxDoGbDepartmentOrderId() != null && ordersEntity.getNxDoGbDepartmentOrderId() > 0) {
//            transUpdateGbDepartmentOrder(ordersEntity);
//        }
//
//        if (ordersEntity.getNxDoWeightId() != null) {
//            NxDistributerWeightEntity weightEntity = nxDistributerWeightService.queryObject(ordersEntity.getNxDoWeightId());
//            Integer nxDwItemCount = weightEntity.getNxDwItemCount();
//            Integer nxDwItemFinishCount = weightEntity.getNxDwItemFinishCount();
//
//            if (nxDwItemCount - nxDwItemFinishCount > 1) {
//                weightEntity.setNxDwItemFinishCount(nxDwItemFinishCount + 1);
//            } else {
//                weightEntity.setNxDwItemFinishCount(nxDwItemCount);
//                weightEntity.setNxDwStatus(1);
//                Map<String, Object> map = new HashMap<>();
//                map.put("weightId", weightEntity.getNxDistributerWeightId());
//                List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
//
//                if (purchaseGoodsEntities.size() > 0) {
//                    for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
//                        purchaseGoodsEntity.setNxDpgStatus(getNxDepOrderBuyStatusFinishPurchase());
//                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
//                    }
//                }
//            }
//            nxDistributerWeightService.update(weightEntity);
//        }
//        return R.ok();
//    }


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
//        if (ordersEntity.getNxDoWeightId() != null) {
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
            BigDecimal decimal = new BigDecimal(nxDoPrice).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(decimal.toString());
        }

        //cost
        BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
        BigDecimal weightB = new BigDecimal(weight);
        BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxDoCostSubtotal(decimal.toString());
        nxDepartmentOrdersService.update(ordersEntity);

        return R.ok();
    }


    @RequestMapping(value = "/giveOrderPrice", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderPrice(Integer orderId, String price) {

        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoPrice(price);
        System.out.println("odididiididi==" + orderId + "priciiei===" + price);
        //try
        if (ordersEntity.getNxDoWeight() != null && new BigDecimal(ordersEntity.getNxDoWeight()).compareTo(BigDecimal.ZERO) == 1) {
            BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());
            BigDecimal subtotalB = weightB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotalB.toString());
            ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());

            //profit
            if (ordersEntity.getNxDoExpcetPrice() != null) {
                System.out.println("ddddididi" + ordersEntity.getNxDoExpcetPrice());
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitB = subtotalB.subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                ordersEntity.setNxDoProfitScale(scaleB.toString());
                ordersEntity.setNxDoProfitSubtotal(profitB.toString());

                BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpcetPrice());
                BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                BigDecimal subtract = doPrice.subtract(expectPrice);
                System.out.println("exxxxxxxx" + subtract);
                ordersEntity.setNxDoPriceDifferent(subtract.toString());

            }
        }

        nxDepartmentOrdersService.update(ordersEntity);

        if (ordersEntity.getNxDoDepartmentId() < 6 && ordersEntity.getNxDoNxGoodsId() != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("nxGoodsId", ordersEntity.getNxDoNxGoodsId());
            map.put("disId", 2);
            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryOneGoodsAboutNxGoods(map);
            if (distributerGoodsEntity.getNxDgSupplierId() == null) {
                distributerGoodsEntity.setNxDgWillPrice(ordersEntity.getNxDoPrice());
                distributerGoodsEntity.setNxDgWillPriceUpdate(formatWhatDate(0));
                nxDistributerGoodsService.update(distributerGoodsEntity);
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);

                if (ordersEntities.size() > 0) {
                    for (NxDepartmentOrdersEntity orders : ordersEntities) {
                        BigDecimal newPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                        BigDecimal different = (newPrice).subtract(new BigDecimal(orders.getNxDoExpcetPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
                        BigDecimal costPrice = new BigDecimal(orders.getNxDoCostPrice());
                        BigDecimal newSubtotal = orderWeight.multiply(newPrice);
                        BigDecimal newScale = newPrice.subtract(costPrice).divide(newPrice, 2, BigDecimal.ROUND_HALF_UP);
                        BigDecimal newCostSubtotal = orderWeight.multiply(costPrice);
                        BigDecimal newProfit = newSubtotal.subtract(newCostSubtotal);

                        orders.setNxDoPrice(ordersEntity.getNxDoPrice());
                        orders.setNxDoSubtotal(newSubtotal.toString());
                        orders.setNxDoProfitSubtotal(newProfit.toString());
                        orders.setNxDoProfitScale(newScale.toString());
                        orders.setNxDoPriceDifferent(different.toString());
                        nxDepartmentOrdersService.update(orders);

                    }
                }

            }
        }
        return R.ok();
    }

    @RequestMapping(value = "/updateOrderPrice", method = RequestMethod.POST)
    @ResponseBody
    public R updateOrderPrice(Integer orderId, String price) {

        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoPrice(price);
        //try
        if (ordersEntity.getNxDoWeight() != null && new BigDecimal(ordersEntity.getNxDoWeight()).compareTo(BigDecimal.ZERO) == 1) {
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
        for (NxDepartmentOrdersEntity ordersEntityOld : ordersEntityList) {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObjectNew(ordersEntityOld.getNxDepartmentOrdersId());
            ordersEntity.setNxDoWeight(ordersEntityOld.getNxDoWeight());
            String nxDoPrice = ordersEntity.getNxDoPrice();
            if (nxDoPrice != null) {
                BigDecimal subtotal = new BigDecimal(nxDoPrice).multiply(new BigDecimal(ordersEntityOld.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoSubtotal(subtotal.toString());
                ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
                BigDecimal nxDoPriceB = new BigDecimal(ordersEntity.getNxDoPrice());
                BigDecimal costPrice = new BigDecimal(0);
                if(ordersEntity.getNxDoCostPrice() == null){
                    //
                    Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
                    NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
                    System.out.println("whatisgoodsnameme" + nxDistributerGoodsEntity.getNxDgGoodsName());
                    BigDecimal willPrice = new BigDecimal(0);
                    BigDecimal buyingPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPrice());
                    String buyingPriceLevel = "0";
                    String update = nxDistributerGoodsEntity.getNxDgBuyingPriceUpdate();
                    if (nxDistributerGoodsEntity.getNxDgWillPriceOneWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOneWeight()).compareTo(BigDecimal.ZERO) == 1) {
                        BigDecimal nxOneWeight = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOneWeight());
                        if (new BigDecimal(ordersEntityOld.getNxDoWeight()).compareTo(nxOneWeight) < 1) {
                            willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                            buyingPriceLevel = "1";
                        } else {
                            if (nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight()).compareTo(BigDecimal.ZERO) == 1) {
                                BigDecimal nxTwoWeight = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight());
                                if (new BigDecimal(ordersEntityOld.getNxDoWeight()).compareTo(nxTwoWeight) < 1) {
                                    willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                                    buyingPriceLevel = "2";
                                } else {
                                    if (nxDistributerGoodsEntity.getNxDgWillPriceThreeWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceThreeWeight()).compareTo(BigDecimal.ZERO) == 1){
                                        willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceThree());
                                        buyingPriceLevel = "3";
                                    }else{
                                        willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                                        buyingPriceLevel = "2";
                                    }
                                }
                            }else{
                                willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                                buyingPriceLevel = "1";
                            }

                        }
                    }

                    BigDecimal  profitB = willPrice.subtract(buyingPrice).setScale(1,BigDecimal.ROUND_HALF_UP);
                    BigDecimal costSubtotalB = buyingPrice.multiply(new BigDecimal(ordersEntityOld.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal profitSubtotal = profitB.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal orderSubtotal = willPrice.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoCostSubtotal(costSubtotalB.toString());
                    ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());
                    ordersEntity.setNxDoSubtotal(orderSubtotal.toString());
                    ordersEntity.setNxDoCostPriceLevel(buyingPriceLevel);
                    ordersEntity.setNxDoCostPrice(buyingPrice.toString());
                    ordersEntity.setNxDoCostPriceUpdate(update);

                }else{
                     costPrice = new BigDecimal(ordersEntity.getNxDoCostPrice());
                }

                BigDecimal profitB = nxDoPriceB.subtract(costPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitSubtotl = profitB.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoProfitSubtotal(profitSubtotl.toString());
                BigDecimal scaleB = profitB.divide(nxDoPriceB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                ordersEntity.setNxDoProfitScale(scaleB.toString());
            }
            //
            BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
            BigDecimal weightB = new BigDecimal(ordersEntityOld.getNxDoWeight());
            BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoCostSubtotal(decimal.toString());
            ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
            nxDepartmentOrdersService.update(ordersEntity);


        }

        return R.ok();
    }


    private void transUpdateGbDepartmentOrder(NxDepartmentOrdersEntity ordersEntity) {

        //更新gbDepOrder
        Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
        if (nxDepartmentOrdersId != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
            gbDepartmentOrdersEntity.setGbDoPrice(ordersEntity.getNxDoPrice());
            gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());
            gbDepartmentOrdersEntity.setGbDoSubtotal(ordersEntity.getNxDoSubtotal());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(ordersEntity.getNxDoPurchaseStatus());
            gbDepartmentOrdersEntity.setGbDoStatus(ordersEntity.getNxDoStatus());

            //sellingData
            Integer gbDoDepDisGoodsId = gbDepartmentOrdersEntity.getGbDoDepDisGoodsId();
            GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);
            String gbDdgSellingPrice = departmentDisGoodsEntity.getGbDdgSellingPrice();
            if (gbDdgSellingPrice != null) {
                gbDepartmentOrdersEntity.setGbDoSellingPrice(gbDdgSellingPrice);
                BigDecimal multiply = new BigDecimal(gbDdgSellingPrice).multiply(new BigDecimal(ordersEntity.getNxDoWeight()));
                gbDepartmentOrdersEntity.setGbDoSellingPrice(gbDdgSellingPrice);
                gbDepartmentOrdersEntity.setGbDoSellingSubtotal(multiply.toString());
            }
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }

    }


    private void transUpdateNxRestrauntOrder(NxDepartmentOrdersEntity nxOrdersEntity) {
        Integer nxDoNxRestrauntOrderId = nxOrdersEntity.getNxDoNxRestrauntOrderId();
        if (nxDoNxRestrauntOrderId != null) {
            NxRestrauntOrdersEntity restrauntOrdersEntity = nxRestrauntOrdersService.queryObject(nxDoNxRestrauntOrderId);
            restrauntOrdersEntity.setNxRoWeight(nxOrdersEntity.getNxDoWeight());
            restrauntOrdersEntity.setNxRoCostPrice(nxOrdersEntity.getNxDoPrice());
            BigDecimal decimal = new BigDecimal(nxOrdersEntity.getNxDoPrice()).multiply(new BigDecimal(nxOrdersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
            restrauntOrdersEntity.setNxRoCostSubtotal(decimal.toString());
            restrauntOrdersEntity.setNxRoBuyStatus(nxOrdersEntity.getNxDoPurchaseStatus());
            restrauntOrdersEntity.setNxRoStatus(nxOrdersEntity.getNxDoStatus());
            nxRestrauntOrdersService.update(restrauntOrdersEntity);

            Integer comGoodsId = restrauntOrdersEntity.getNxRoComGoodsId();
            NxCommunityGoodsEntity nxCommunityGoodsEntity = nxCommunityGoodsService.queryObject(comGoodsId);
            nxCommunityGoodsEntity.setNxCgBuyingPrice(nxOrdersEntity.getNxDoPrice());
            nxCommunityGoodsService.update(nxCommunityGoodsEntity);
        }
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
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());

                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
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
            mapR.put("total", total);
            return R.ok().put("data", mapR);
        } else {
            Map<String, Object> mapR = new HashMap<>();
            double total = 0.0;
            map.put("subtotal", 0);
            System.out.println("tototototdddddddd" + map);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }
            mapR.put("subAmount", 0);
            mapR.put("arr", ordersEntities);
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapR.put("tradeNo", s);
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
                             String orderBy, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
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

    @RequestMapping(value = "/webGetSubDepOrderPage", method = RequestMethod.POST)
    @ResponseBody
    public R webGetSubDepOrderPage(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId,
                                   String orderBy, Integer limit, Integer page) {
        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
        List<Map<String, Object>> retrunList = new ArrayList<>();
        if (departmentEntities.size() > 0) {

            for (NxDepartmentEntity subDep : departmentEntities) {

                Map<String, Object> map = new HashMap<>();
                map.put("status", 3);
                map.put("depId", subDep.getNxDepartmentId());
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
                mapR.put("depName", subDep.getNxDepartmentName());

                retrunList.add(mapR);
            }

        } else {
            Map<String, Object> map = new HashMap<>();
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

            retrunList.add(mapR);
        }


        return R.ok().put("data", retrunList);
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
                map.put("dayuStatus", 1);
                System.out.println("mappfpafd" + map);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                double subtotal = 0;
                if (integer > 0) {
                    subtotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                }
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", departmentEntity.getNxDepartmentId());
                mapDep.put("depName", departmentEntity.getNxDepartmentName());
                mapDep.put("list", ordersEntities);
                mapDep.put("subtotal", new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                list.add(mapDep);
            }

            mapResult.put("arr", list);
            mapResult.put("depHasSubs", departmentEntities.size());
        } else {
            System.out.println("aodoodododoodododood" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
            mapResult.put("arr", ordersEntities);
            mapResult.put("depHasSubs", 0);
        }


        Map<String, Object> mapPPP = new HashMap<>();

        if (orderBy.equals("goodsType")) {
            Map<String, Object> mapP = new HashMap<>();
            mapP.put("status", 3);
            mapP.put("depFatherId", depFatherId);
            mapP.put("resFatherId", resFatherId);
            mapP.put("gbDepFatherId", gbDepFatherId);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(mapP);
            mapP.put("goodsType", -1); // stock
            List<NxDepartmentOrdersEntity> ordersEntitiesStock = nxDepartmentOrdersService.queryDisOrdersByParams(mapP);
            mapP.put("goodsType", 0);
            List<NxDepartmentOrdersEntity> ordersEntitiesZicai = nxDepartmentOrdersService.queryDisOrdersByParams(mapP);
            mapP.put("goodsType", 1);
            List<NxDepartmentOrdersEntity> ordersEntitiesWx = nxDepartmentOrdersService.queryDisOrdersByParams(mapP);

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
            profitTotal = nxDepartmentOrdersService.queryDepOrdersProfitSubtotal(map2);
            BigDecimal scaleBig = new BigDecimal(profitTotal).divide(new BigDecimal(total), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            scale = scaleBig.toString();
        }


        mapResult.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("profit", new BigDecimal(profitTotal).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("scale", scale);

        map.put("hasPrice", 1);
        Integer priceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        mapResult.put("priceCount", priceCount);
        mapResult.put("mapP", mapPPP);
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
            profitTotal = nxDepartmentOrdersService.queryDepOrdersProfitSubtotal(map2);
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
        map3.put("dayuStatus", -1);
        List<NxDepartmentOrdersEntity> ordersEntities3 = nxDepartmentOrdersService.queryDisOrdersByParams(map3);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", ordersEntities3);

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
        }
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/webNxDisGetTodayOrderCustomer/{disId}")
    @ResponseBody
    public R webNxDisGetTodayOrderCustomer(@PathVariable Integer disId) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        System.out.println("dapapfpa" + map1);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);

        return R.ok().put("data", departmentEntities);

    }

    @RequestMapping(value = "/disGetTodayOrderCustomer/{disId}")
    @ResponseBody
    public R disGetTodayOrderCustomer(@PathVariable Integer disId) {
        Map<String, Object> returnData = new HashMap<>();
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);
//        List<NxRestrauntEntity> restrauntEntities = nxDepartmentOrdersService.queryOrderNxRestrauntList(map1);
        List<GbDistributerEntity> distributerEntities = nxDepartmentOrdersService.queryOrderGbDistributerList(map1);
        Map<String, Object> mapData = new HashMap<>();
        List<Map<String, Object>> resultNx = new ArrayList<>();
        List<Map<String, Object>> resultRes = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            Integer zicaiFinished = 0;
            Integer chukuFinished = 0;
            Integer zicaiOrder = 0;
            Integer wxFinished = 0;
            Integer chukuOrder = 0;
            Integer wxOrder = 0;

            Integer hasPrice = 0;
            Integer hasNotPrice = 0;

            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                //new
                Map<String, Object> mapOrder = new HashMap<>();
                mapOrder.put("depFatherId", departmentEntity.getNxDepartmentId());
                mapOrder.put("status", 3);
                //出库 -chuku
                mapOrder.put("goodsType", -1);
                chukuOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
                //wx
                mapOrder.put("goodsType", 1);
                wxOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
                //zicai
                mapOrder.put("goodsType", 0);
                zicaiOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
                mapOrder.put("equalPurStatus", 4);
                zicaiFinished = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
                mapOrder.put("goodsType", -1);
                chukuFinished = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
                //wx
                mapOrder.put("goodsType", 1);
                wxFinished = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);

                // price
                Map<String, Object> map7 = new HashMap<>();
                map7.put("depFatherId", departmentEntity.getNxDepartmentId());
                map7.put("status", 3);
                map7.put("hasPrice", 1);
                hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(map7);
                map7.put("hasPrice", -1);
                hasNotPrice = nxDepartmentOrdersService.queryDepOrdersAcount(map7);

                Map<String, Object> map2 = new HashMap<>();
                map2.put("depFatherId", departmentEntity.getNxDepartmentId());
                map2.put("status", 3);
                map2.put("subtotal", 0);
                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map2);
                Double total = 0.0;
                if (twoTotal > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map2);
                }

                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("dep", departmentEntity);
                mapDep.put("chukuOrder", chukuOrder);
                mapDep.put("chukuOrderFinished", chukuFinished);
                mapDep.put("wxOrder", wxOrder);
                mapDep.put("wxOrderFinished", wxFinished);
                mapDep.put("zicaiOrder", zicaiOrder);
                mapDep.put("zicaiOrderFinished", zicaiFinished);

                mapDep.put("hasPrice", hasPrice);
                mapDep.put("hasNotPrice", hasNotPrice);

                mapDep.put("twoSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                resultNx.add(mapDep);

            }
            mapData.put("nxDep", resultNx);
        } else {
            mapData.put("nxDep", new ArrayList<>());
        }
//
//        if (restrauntEntities.size() > 0) {
//            Integer zicaiFinished = 0;
//            Integer chukuFinished = 0;
//            Integer zicaiOrder = 0;
//            Integer wxFinished = 0;
//            Integer chukuOrder = 0;
//            Integer wxOrder = 0;
//
//            Integer hasPrice = 0;
//            Integer hasNotPrice = 0;
//
//            for (NxRestrauntEntity restrauntEntity : restrauntEntities) {
//                //new
//                Map<String, Object> mapOrder = new HashMap<>();
//                mapOrder.put("resFatherId", restrauntEntity.getNxRestrauntId());
//                mapOrder.put("status", 3);
//                //出库 -chuku
//                mapOrder.put("goodsType", -1);
//                chukuOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
//                //wx
//                mapOrder.put("goodsType", 1);
//                wxOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
//                //zicai
//                mapOrder.put("goodsType", 0);
//                zicaiOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
//                mapOrder.put("equalPurStatus", 4);
//                zicaiFinished = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
//                mapOrder.put("goodsType", -1);
//                chukuFinished = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
//                //wx
//                mapOrder.put("goodsType", 1);
//                wxFinished = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
//
//
//                // price
//                Map<String, Object> map7 = new HashMap<>();
//                map7.put("resFatherId", restrauntEntity.getNxRestrauntId());
//                map7.put("status", 3);
//                map7.put("hasPrice", 1);
//                hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(map7);
//                map7.put("hasPrice", -1);
//                hasNotPrice = nxDepartmentOrdersService.queryDepOrdersAcount(map7);
//
//                Map<String, Object> map2 = new HashMap<>();
//                map2.put("resFatherId", restrauntEntity.getNxRestrauntId());
//                map2.put("status", 3);
//                map2.put("subtotal", 0);
//                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map2);
//                Double total = 0.0;
//                if (twoTotal > 0) {
//                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map2);
//                }
//
//                Map<String, Object> mapDep = new HashMap<>();
//                mapDep.put("dep", restrauntEntity);
//                mapDep.put("chukuOrder", chukuOrder);
//                mapDep.put("chukuOrderFinished", chukuFinished);
//                mapDep.put("wxOrder", wxOrder);
//                mapDep.put("wxOrderFinished", wxFinished);
//                mapDep.put("zicaiOrder", zicaiOrder);
//                mapDep.put("zicaiOrderFinished", zicaiFinished);
//
//                mapDep.put("hasPrice", hasPrice);
//                mapDep.put("hasNotPrice", hasNotPrice);
//
//                mapDep.put("twoSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                resultRes.add(mapDep);
//
//            }
//            mapData.put("commRes", resultRes);
//        } else {
//            mapData.put("commRes", new ArrayList<>());
//        }

        List<Map<String, Object>> gbList = new ArrayList<>();
        if (distributerEntities.size() > 0) {

            for (GbDistributerEntity gbDis : distributerEntities) {
                List<Map<String, Object>> gbDisDepArrMap = new ArrayList<>();

                Map<String, Object> mapDis = new HashMap<>();
                mapDis.put("dis", gbDis);
                mapDis.put("gbDisId", gbDis.getGbDistributerId());
                mapDis.put("status", 3);
                List<GbDepartmentEntity> gbpartmentEntities = nxDepartmentOrdersService.queryOrderGbDepartmentList(mapDis);

                Integer zicaiFinished = 0;
                Integer chukuFinished = 0;
                Integer zicaiOrder = 0;
                Integer wxFinished = 0;
                Integer chukuOrder = 0;
                Integer wxOrder = 0;

                Integer hasPrice = 0;
                Integer hasNotPrice = 0;

                for (GbDepartmentEntity gbDepartmentEntity : gbpartmentEntities) {
                    //new
                    Map<String, Object> mapOrder = new HashMap<>();
                    mapOrder.put("gbDepFatherId", gbDepartmentEntity.getGbDepartmentId());
                    mapOrder.put("status", 3);
                    //出库 -chuku
                    mapOrder.put("goodsType", -1);
                    chukuOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
                    //wx
                    mapOrder.put("goodsType", 1);
                    wxOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
                    //zicai
                    mapOrder.put("goodsType", 0);
                    zicaiOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
                    mapOrder.put("equalPurStatus", 4);
                    zicaiFinished = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
                    mapOrder.put("goodsType", -1);
                    chukuFinished = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);
                    //wx
                    mapOrder.put("goodsType", 1);
                    wxFinished = nxDepartmentOrdersService.queryDepOrdersAcount(mapOrder);

                    // price
                    Map<String, Object> map7 = new HashMap<>();
                    map7.put("gbDepFatherId", gbDepartmentEntity.getGbDepartmentId());
                    map7.put("status", 3);
                    map7.put("hasPrice", 1);
                    hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(map7);
                    map7.put("hasPrice", -1);
                    hasNotPrice = nxDepartmentOrdersService.queryDepOrdersAcount(map7);

                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("gbDepFatherId", gbDepartmentEntity.getGbDepartmentId());
                    map2.put("status", 3);
                    map2.put("subtotal", 0);
                    Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map2);
                    Double total = 0.0;
                    if (twoTotal > 0) {
                        total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map2);
                    }

                    Map<String, Object> mapDep = new HashMap<>();
                    mapDep.put("gbDep", gbDepartmentEntity);
                    mapDep.put("chukuOrder", chukuOrder);
                    mapDep.put("chukuOrderFinished", chukuFinished);
                    mapDep.put("wxOrder", wxOrder);
                    mapDep.put("wxOrderFinished", wxFinished);
                    mapDep.put("zicaiOrder", zicaiOrder);
                    mapDep.put("zicaiOrderFinished", zicaiFinished);

                    mapDep.put("hasPrice", hasPrice);
                    mapDep.put("hasNotPrice", hasNotPrice);
                    mapDep.put("twoSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    gbDisDepArrMap.add(mapDep);
                }

                mapDis.put("arr", gbDisDepArrMap);
                gbList.add(mapDis);
            }

            mapData.put("gbDisArr", gbList);

        } else {

            mapData.put("gbDisArr", new ArrayList<>());
        }


        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("status", 3);
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
        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("equalPurStatus", 4);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        int wxCountAutoOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 0);
        int zicaiCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        returnData.put("stockCount", stockCount);
        returnData.put("stockCountOk", stockCountOK);
        returnData.put("wxCount", wxCount);
        returnData.put("wxCountOk", wxCountOk);
        returnData.put("wxCountAuto", wxCountAuto);
        returnData.put("wxCountAutoOk", wxCountAutoOk);

        returnData.put("zicaiCount", zicaiCount);
        returnData.put("zicaiCountOk", zicaiCountOk);

        returnData.put("week", getWeek(0));
        returnData.put("hao", getJustHao(0));
        returnData.put("deps", mapData);
        return R.ok().put("data", returnData);

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


    /**
     * ORDER
     * 修改申请
     *
     * @param
     * @return ok
     */
    @ResponseBody
    @RequestMapping(value = "/updateOrder", method = RequestMethod.POST)
    public R updateOrder(Integer id, String weight, String standard, String remark) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(id);
        ordersEntity.setNxDoQuantity(weight);
        ordersEntity.setNxDoStandard(standard);
        ordersEntity.setNxDoRemark(remark);
        //自动添加重量和单价小计
        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetailWithLinshi(doDisGoodsId);
        String nxDgGoodsStandardname = distributerGoodsEntity.getNxDgGoodsStandardname();

        if (ordersEntity.getNxDoWeight() != null && standard.equals(nxDgGoodsStandardname)) {
            ordersEntity.setNxDoWeight(weight);
            if (ordersEntity.getNxDoSubtotal() != null && !ordersEntity.getNxDoSubtotal().equals("0.0")) {
                BigDecimal decimal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoSubtotal(decimal.toString());
                //profit
                if (ordersEntity.getNxDoExpcetPrice() != null) {
                    System.out.println("ddddididi" + ordersEntity.getNxDoExpcetPrice());
                    BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());
                    BigDecimal decimal2 = new BigDecimal(ordersEntity.getNxDoPrice());
                    BigDecimal subtotalB = weightB.multiply(decimal2);

                    BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                    BigDecimal decimal1 = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal profitB = subtotalB.subtract(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                    ordersEntity.setNxDoProfitScale(scaleB.toString());
                    ordersEntity.setNxDoProfitSubtotal(profitB.toString());

                    BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpcetPrice());
                    BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                    BigDecimal subtract = doPrice.subtract(expectPrice);
                    ordersEntity.setNxDoPriceDifferent(subtract.toString());

                }
            }
        } else {
            ordersEntity.setNxDoWeight(null);
            ordersEntity.setNxDoSubtotal(null);
            ordersEntity.setNxDoCostSubtotal(null);
            ordersEntity.setNxDoProfitScale(null);
            ordersEntity.setNxDoProfitSubtotal(null);
            ordersEntity.setNxDoPriceDifferent(null);

        }

        nxDepartmentOrdersService.update(ordersEntity);
        ordersEntity.setNxDistributerGoodsEntity(distributerGoodsEntity);
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
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObjectNew(nxDepartmentOrdersId);
        if (ordersEntity.getNxDoPurchaseGoodsId() != null && ordersEntity.getNxDoPurchaseGoodsId() != -1
                && ordersEntity.getNxDoPurchaseStatus() < getNxDepOrderBuyStatusFinishPurchase()) {
            NxDistributerPurchaseGoodsEntity nxDistributerPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(ordersEntity.getNxDoPurchaseGoodsId());
            Integer nxDpgOrdersAmount = nxDistributerPurchaseGoodsEntity.getNxDpgOrdersAmount();
            if (nxDpgOrdersAmount > 1) {
                nxDistributerPurchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
                nxDistributerPurchaseGoodsService.update(nxDistributerPurchaseGoodsEntity);
            } else {
                if (nxDistributerPurchaseGoodsEntity.getNxDpgStatus() == getNxDisPurchaseGoodsWithBatch()) {
                    Integer nxDpgBatchId = nxDistributerPurchaseGoodsEntity.getNxDpgBatchId();
                    List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByBatchId(nxDpgBatchId);
                    if (purchaseGoodsEntities.size() == 1) {
                        nxDPBService.delete(nxDpgBatchId);
                    }
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
        nxDepartmentOrders.setNxDoCostPriceLevel("0");
        nxDepartmentOrders.setNxDoPriceDifferent("0.0");

        //auto
        if (nxDepartmentOrders.getNxDoPurchaseGoodsId() != -1) {
            savePurGoodsAuto(nxDepartmentOrders);
        }

        Integer integer = checkDepDisGoods(nxDepartmentOrders);
        nxDepartmentOrders.setNxDoDepDisGoodsId(integer);

        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("status", 3);
        int order = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        nxDepartmentOrders.setNxDoTodayOrder(order + 1);
        nxDepartmentOrdersService.save(nxDepartmentOrders);

        return R.ok().put("data", nxDepartmentOrdersService.queryObject(nxDepartmentOrders.getNxDepartmentOrdersId()));
    }


    private Integer checkDepDisGoods(NxDepartmentOrdersEntity nxDepartmentOrders) {

        System.out.println("chehchcchchhchchcdididigogood");

        Integer depDisGoodsId = 0;
        //判断是否是部门商品
        Integer doDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        Integer nxDoDepartmentId1 = nxDepartmentOrders.getNxDoDepartmentId();
        //查询部门还是订货群是否添加过此商品
        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDoDepartmentId1);
        map.put("disGoodsId", doDisGoodsId);
        List<NxDepartmentDisGoodsEntity> disGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
        if (disGoodsEntities.size() == 0) {
            //添加部门商品
            NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
            String nxDgGoodsName = nxDistributerGoodsEntity.getNxDgGoodsName();
            NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
            disGoodsEntity.setNxDdgDepGoodsName(nxDgGoodsName);
            disGoodsEntity.setNxDdgDisGoodsId(doDisGoodsId);
            disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
            disGoodsEntity.setNxDdgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
            disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
            disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
            disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
            disGoodsEntity.setNxDdgDepartmentId(nxDoDepartmentId1);
            disGoodsEntity.setNxDdgDepartmentFatherId(nxDepartmentOrders.getNxDoDepartmentFatherId());
            //orderData
            disGoodsEntity.setNxDdgOrderPrice(nxDepartmentOrders.getNxDoPrice());
            disGoodsEntity.setNxDdgOrderCostPrice(nxDepartmentOrders.getNxDoCostPrice());
            disGoodsEntity.setNxDdgOrderQuantity(nxDepartmentOrders.getNxDoQuantity());
            disGoodsEntity.setNxDdgOrderRemark(nxDepartmentOrders.getNxDoRemark());
            disGoodsEntity.setNxDdgOrderStandard(nxDepartmentOrders.getNxDoStandard());
            disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
            nxDepartmentDisGoodsService.save(disGoodsEntity);
            depDisGoodsId = disGoodsEntity.getNxDepartmentDisGoodsId();

        } else {

            depDisGoodsId = disGoodsEntities.get(0).getNxDepartmentDisGoodsId();
            NxDepartmentDisGoodsEntity disGoodsEntity = nxDepartmentDisGoodsService.queryObject(depDisGoodsId);
            disGoodsEntity.setNxDdgOrderPrice(nxDepartmentOrders.getNxDoPrice());
            disGoodsEntity.setNxDdgOrderCostPrice(nxDepartmentOrders.getNxDoCostPrice());
            disGoodsEntity.setNxDdgOrderQuantity(nxDepartmentOrders.getNxDoQuantity());
            disGoodsEntity.setNxDdgOrderRemark(nxDepartmentOrders.getNxDoRemark());
            disGoodsEntity.setNxDdgOrderStandard(nxDepartmentOrders.getNxDoStandard());
            disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDepartmentOrders.getPurchaseGoodsEntity();
            if (purchaseGoodsEntity != null) {
                if (purchaseGoodsEntity.getNxDpgSellUserId() != null) {
                    disGoodsEntity.setNxDdgOrderSellerUserId(purchaseGoodsEntity.getNxDpgSellUserId());
                }
                disGoodsEntity.setNxDdgOrderBuyerUserId(purchaseGoodsEntity.getNxDpgBuyUserId());
            }
            nxDepartmentDisGoodsService.update(disGoodsEntity);

        }
        return depDisGoodsId;
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
        nxDepartmentOrders.setNxDoCostPriceLevel("0");


        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("status", 3);
        System.out.println("ordierireirei" + map);
        int order = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        nxDepartmentOrders.setNxDoTodayOrder(order + 1);
        nxDepartmentOrdersService.save(nxDepartmentOrders);

        //auto
        if (nxDepartmentOrders.getNxDoPurchaseGoodsId() != -1) {
            savePurGoodsAuto(nxDepartmentOrders);
        }

        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(nxDepartmentOrders.getNxDepartmentOrdersId());
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(ordersEntity.getNxDoDisGoodsId());
        ordersEntity.setNxDistributerGoodsEntity(distributerGoodsEntity);
        return R.ok().put("data", ordersEntity);
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
        nxDepartmentOrders.setNxDoCostPriceLevel("0");
        nxDepartmentOrders.setNxDoTodayOrder(beforOrder.getNxDoTodayOrder());
        nxDepartmentOrdersService.save(nxDepartmentOrders);

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
                System.out.println("whisisisisisisisis====" + i1);
                ordersEntity.setNxDoTodayOrder(i1);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

        beforOrder.setNxDoTodayOrder(beforOrder.getNxDoTodayOrder() + 1);
        nxDepartmentOrdersService.update(beforOrder);

        //auto
        if (nxDepartmentOrders.getNxDoPurchaseGoodsId() != -1) {
            savePurGoodsAuto(nxDepartmentOrders);
        }
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(nxDepartmentOrders.getNxDepartmentOrdersId());
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(ordersEntity.getNxDoDisGoodsId());
        ordersEntity.setNxDistributerGoodsEntity(distributerGoodsEntity);
        return R.ok().put("data", ordersEntity);
    }

    private void savePurGoodsAuto(NxDepartmentOrdersEntity ordersEntity) {


        Integer nxDistributerPurchaseGoodsId = 0;
        //判断是否有已经分的

        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(doDisGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", doDisGoodsId);
        map.put("status", 2);
        System.out.println("purgogogo" + map);
        NxDistributerPurchaseGoodsEntity havePurGoods = nxDistributerPurchaseGoodsService.queryIfHavePurGoods(map);
        if (havePurGoods != null) {
            havePurGoods.setNxDpgOrdersAmount(havePurGoods.getNxDpgOrdersAmount() + 1);
            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                if (havePurGoods.getNxDpgBuyQuantity() != null) {
                    BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgQuantity());
                    BigDecimal decimal1 = new BigDecimal(ordersEntity.getNxDoQuantity());
                    BigDecimal totaoWeight = decimal.add(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(havePurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    havePurGoods.setNxDpgQuantity(totaoWeight.toString());
                    havePurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
                    havePurGoods.setNxDpgBuySubtotal(decimal2.toString());
                }

            }

            nxDistributerPurchaseGoodsService.update(havePurGoods);
            nxDistributerPurchaseGoodsId = havePurGoods.getNxDistributerPurchaseGoodsId();

        } else {

            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
            purchaseGoodsEntity.setNxDpgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
            purchaseGoodsEntity.setNxDpgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
            purchaseGoodsEntity.setNxDpgDistributerId(disGoods.getNxDgDistributerId());
            purchaseGoodsEntity.setNxDpgApplyDate(formatWhatDay(0));
            purchaseGoodsEntity.setNxDpgCostLevel(disGoods.getNxDgBuyingPriceIsGrade());
            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
            purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
            purchaseGoodsEntity.setNxDpgOrdersAmount(1);
            purchaseGoodsEntity.setNxDpgFinishAmount(0);
            purchaseGoodsEntity.setNxDpgPurchaseType(1);
            purchaseGoodsEntity.setNxDpgExpectPrice(disGoods.getNxDgBuyingPrice());
            purchaseGoodsEntity.setNxDpgBuyPrice(disGoods.getNxDgBuyingPrice());
            purchaseGoodsEntity.setNxDpgDisGoodsId(doDisGoodsId);
            purchaseGoodsEntity.setNxDpgInputType(disGoods.getNxDgPurchaseAuto());
            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                purchaseGoodsEntity.setNxDpgQuantity(ordersEntity.getNxDoQuantity());
                BigDecimal totaoWeight = new BigDecimal(ordersEntity.getNxDoQuantity());
                purchaseGoodsEntity.setNxDpgStandard(ordersEntity.getNxDoStandard());
                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(purchaseGoodsEntity.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                purchaseGoodsEntity.setNxDpgQuantity(totaoWeight.toString());
                purchaseGoodsEntity.setNxDpgBuyQuantity(totaoWeight.toString());
                purchaseGoodsEntity.setNxDpgBuySubtotal(decimal2.toString());
            }
            nxDistributerPurchaseGoodsService.save(purchaseGoodsEntity);
            nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();

            //给autoBatch更新gbDepartmentOrderid
            if (disGoods.getNxDgSupplierId() != null) {
                //
                Map<String, Object> mapBatch = new HashMap<>();
                Integer gbDgGbSupplierId = disGoods.getNxDgSupplierId();
                mapBatch.put("supplierId", gbDgGbSupplierId);
                mapBatch.put("status", 1);
                mapBatch.put("purchaseType", 2);
                List<NxDistributerPurchaseBatchEntity> entities = nxDPBService.queryDisPurchaseBatch(mapBatch);

                if (entities.size() == 0) {
                    //
                    NxDistributerPurchaseBatchEntity batchEntity = new NxDistributerPurchaseBatchEntity();
                    batchEntity.setNxDpbDate(formatWhatDay(0));
                    batchEntity.setNxDpbTime(formatWhatTime(0));
                    batchEntity.setNxDpbMonth(formatWhatMonth(0));
                    batchEntity.setNxDpbPruchaseWeek(getWeek(0));
                    batchEntity.setNxDpbYear(formatWhatYear(0));
                    batchEntity.setNxDpbPayFullTime(formatWhatYearDayTime(0));
                    batchEntity.setNxDpbStatus(-1);
                    batchEntity.setNxDpbPurchaseType(2);
                    batchEntity.setNxDpbSupplierId(gbDgGbSupplierId);
                    NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDgGbSupplierId);
                    batchEntity.setNxDpbSellUserId(supplierEntity.getNxJrdhsUserId());
                    batchEntity.setNxDpbDistributerId(ordersEntity.getNxDoDistributerId());
                    batchEntity.setNxDpbPurUserId(supplierEntity.getNxJrdhsNxPurUserId());
                    batchEntity.setNxDpbBuyUserId(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                    NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                    batchEntity.setNxDpbBuyUserOpenId(nxJrdhUserEntity.getNxJrdhWxOpenId());
                    nxDPBService.save(batchEntity);

                    purchaseGoodsEntity.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                    purchaseGoodsEntity.setNxDpgStatus(getGbPurchaseGoodsStatusProcurement());
                    purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
                    purchaseGoodsEntity.setNxDpgTime(formatWhatYearDayTime(0));
                    purchaseGoodsEntity.setNxDpgDistributerId(batchEntity.getNxDpbDistributerId());
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                } else {
                    NxDistributerPurchaseBatchEntity batchEntity = entities.get(0);
                    purchaseGoodsEntity.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                    purchaseGoodsEntity.setNxDpgStatus(getGbPurchaseGoodsStatusProcurement());
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            }

        }
        ordersEntity.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
        nxDepartmentOrdersService.update(ordersEntity);


    }


}
