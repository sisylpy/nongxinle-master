package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-25 22:52
 */

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.github.wxpay.sdk.WXPay;
import com.nongxinle.entity.*;
import com.nongxinle.entity.NxGoodsPriceEntity;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.CommonUtils.generateBillTradeNo;
import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbOrderTypeTuihuo;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPayListRecord;


@RestController
@RequestMapping("api/gbdistributerpurchasebatch")
public class GbDistributerPurchaseBatchController {
    @Autowired
    private GbDistributerPurchaseBatchService gbDPBService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDPGService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private NxJrdhSupplierService nxJrdhSupplierService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerGoodsPriceService gbDistributerGoodsPriceService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepartmentStockReduceService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
    @Autowired
    private GbDistributerGoodsPriceService goodsPriceService;
    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;
    @Autowired
    private NxDepartmentBillService nxDepartmentBillService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDPBService;
    @Autowired
    private GbDistributerPayListService payListService;
    @Autowired
    private NxDistributerGbDistributerService nxDisGbDisService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxDistributerPayListService nxPayListService;
    @Autowired
    private NxDistributerGoodsService dgService;
    @Autowired
    private NxGoodsPriceService nxGoodsPriceService;
    @Autowired
    private GbDepartmentUserService gbDepartmentUserService;
    @Autowired
    private NxDepartmentOrderHistoryService historyService;

    @RequestMapping(value = "/nxDisFinishPurchaseGoodsBatchGb")
    @ResponseBody
    public R nxDisFinishPurchaseGoodsBatchGb(@RequestBody GbDistributerPurchaseBatchEntity batchEntity) {

        List<GbDistributerPurchaseGoodsEntity> nxDPBEntities = batchEntity.getGbDPGEntities();
        int orderTotal = 0;
        for (GbDistributerPurchaseGoodsEntity purGoods : nxDPBEntities) {
            purGoods.setGbDpgStatus(getGbPurchaseGoodsStatusFinished());
            purGoods.setGbDpgPurchaseNxDistributerId(batchEntity.getGbDpbDistributerId());
            gbDPGService.update(purGoods);

            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", purGoods.getGbDistributerPurchaseGoodsId());
            List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
            if (gbDepartmentOrdersEntities.size() > 0) {
                for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
                    orderTotal = orderTotal + 1;

                    Integer gbDoNxDepartmentOrderId = gbDepartmentOrdersEntity.getGbDoNxDepartmentOrderId();

                    NxDepartmentOrdersEntity orders = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
                    orders.setNxDoStatus(2);
                    orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
                    nxDepartmentOrdersService.update(orders);

//                    gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusReceived());
//                    gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusUnPayFinish());
                    gbDepartmentOrdersEntity.setGbDoArriveWeeksYear(getWeekOfYear(0));
                    gbDepartmentOrdersEntity.setGbDoArriveWhatDay(getWeek(0));
                    gbDepartmentOrdersEntity.setGbDoArriveOnlyDate(formatWhatDate(0));
                    gbDepartmentOrdersEntity.setGbDoArriveDate(formatWhatDay(0));
                    gbDepartmentOrdersEntity.setGbDoWeight(orders.getNxDoWeight());
                    gbDepartmentOrdersEntity.setGbDoSubtotal(orders.getNxDoSubtotal());
                    gbDepartmentOrdersEntity.setGbDoPrice(orders.getNxDoPrice());
                    gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);


                    Integer doDisGoodsId = orders.getNxDoDisGoodsId();
                    NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
                    BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
                    BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
                    BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                    distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
                    dgService.update(distributerGoodsEntity);

                    Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
                    if (nxDoPurchaseGoodsId != -1) {
                        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                        Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                        Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                        if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                        } else {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                        }

                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                }

            }

//            BigDecimal decimal1 = new BigDecimal(orderTotal);
//            Map<String, Object> mapNG = new HashMap<>();
//            mapNG.put("gbDisId", batchEntity.getGbDpbDistributerId());
//            mapNG.put("nxDisId", batchEntity.getGbDpbNxDistributerId());
//            NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDisGbDisService.queryObjectByParams(mapNG);
//            //如果不是批发商邀请的饭馆
//            if(nxDistributerGbDistributerEntity.getNxDgdFromNxDisId() != null && !nxDistributerGbDistributerEntity.getNxDgdFromNxDisId().equals(batchEntity.getGbDpbNxDistributerId())){
//                NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(batchEntity.getGbDpbNxDistributerId());
//                NxDistributerPayListEntity payListEntity = new NxDistributerPayListEntity();
//                payListEntity.setNxNdplNxDisId(batchEntity.getGbDpbNxDistributerId());
//                payListEntity.setNxNdplNxDepartmentId(-1);
//                payListEntity.setNxNdplNxDepartmentFatherId(-1);
//                payListEntity.setNxNdplPaySubtotal(Integer.valueOf(orderTotal).toString());
//                payListEntity.setNxNdplPayTime(formatFullTime());
//                payListEntity.setNxNdplPayDate(formatWhatDay(0));
//                payListEntity.setNxNdplPayMonth(formatWhatMonth(0));
//                payListEntity.setNxNdplPayYear(formatWhatYear(0));
//                payListEntity.setNxNdplStatus(0);
//                payListEntity.setNxNdplType(getNxDisPayListGb());
//                payListEntity.setNxNdplRestPoints(Integer.valueOf(nxDistributerEntity.getNxDistributerBuyQuantity()));
//                nxPayListService.save(payListEntity);
//
//                BigDecimal decimal0 = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());
//                BigDecimal restPoints = decimal0.subtract(decimal1).setScale(0,BigDecimal.ROUND_HALF_UP);
//                nxDistributerEntity.setNxDistributerBuyQuantity(restPoints.toString());
//                nxDistributerService.update(nxDistributerEntity);
//            }

        }

        batchEntity.setGbDpbStatus(getGbDisPurchaseBatchSellerReply());
        batchEntity.setGbDpbSellerReplyFullTime(formatFullTime());
        gbDPBService.update(batchEntity);
        return R.ok();
    }


    @RequestMapping(value = "/finishPayPurchaseBatchGb", method = RequestMethod.POST)
    @ResponseBody
    public R finishPayPurchaseBatchGb(@RequestBody List<GbDistributerPurchaseBatchEntity> purList) {
        for (GbDistributerPurchaseBatchEntity batchEntity : purList) {

            Map<String, Object> map = new HashMap<>();
            map.put("batchId", batchEntity.getGbDistributerPurchaseBatchId());
            List<GbDistributerPurchaseGoodsEntity> distributerPurchaseGoodsEntities = gbDPGService.queryPurchaseGoodsByParams(map);
            for (GbDistributerPurchaseGoodsEntity purGoods : distributerPurchaseGoodsEntities) {
                purGoods.setGbDpgStatus(getNxDisPurchaseGoodsFinishPay());
                gbDPGService.update(purGoods);
            }
            batchEntity.setGbDpbFinishFullTime(formatFullTime());
            batchEntity.setGbDpbStatus(getGbDisPurchaseBatchDisUserFinishPay());
            gbDPBService.update(batchEntity);
        }
        return R.ok();
    }


    @RequestMapping(value = "/disCheckUnPayBillsGb", method = RequestMethod.POST)
    @ResponseBody
    public R disCheckUnPayBillsGb(Integer disId, Integer supplierId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("supplierId", supplierId);
        map.put("equalStatus", getNxDisPurchaseBatchDisUserFinishPay());
        map.put("payType", 1);
        List<GbDistributerPurchaseBatchEntity> batchEntities = gbDPBService.queryDisPurchaseBatch(map);
        int i = gbDPBService.queryDisPurchaseBatchCount(map);
        Double decimal = 0.0;
        if (i > 0) {
            decimal = gbDPBService.querySupplierUnSettleSubtotal(map);
        }
        Map<String, Object> map2 = new HashMap<>();
        map2.put("arr", batchEntities);
        map2.put("total", new BigDecimal(decimal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

        return R.ok().put("data", map2);

    }


    @RequestMapping(value = "/disGetDinghuoByDate", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDinghuoByDate(Integer disId, String searchDepIds, Integer type,
                                 String startDate, String stopDate, String searchDepId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purchaseType", type);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
//        if(!searchDepId.equals("-1")){
//            map.put("purDepId", searchDepId);
//        }else{
//            if (!searchDepIds.equals("-1")) {
//                String[] arrGb = searchDepIds.split(",");
//                List<String> idsGb = new ArrayList<>();
//                for (String idGb : arrGb) {
//                    idsGb.add(idGb);
//                    if (idsGb.size() > 0) {
//                        map.put("purDepIds", idsGb);
//                    }
//                }
//            }
//        }
        System.out.println("mappapapap" + map);


        Map<String, Object> mapR = new HashMap<>();

        if (type == 2) {
            map.put("payType", 0);
            List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();
            System.out.println("pururugoogogogooodod" + map);
            map.put("subtoal", 0);
            List<GbDistributerFatherGoodsEntity> greatGrandPurGoodsDetial = gbDPGService.queryGreatGrandPurGoodsDetail(map);
            if (greatGrandPurGoodsDetial.size() > 0) {
                for (GbDistributerFatherGoodsEntity greatGrand : greatGrandPurGoodsDetial) {
                    List<GbDistributerFatherGoodsEntity> grandFatherGoodsEntities = greatGrand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity grand : grandFatherGoodsEntities) {
                        result.addAll(grand.getFatherGoodsEntities());
                    }
                }
            }
            mapR.put("arr", result);
        } else if (type == 21) {
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("disId", disId);
            mapS.put("startDate", startDate);
            mapS.put("dayuStatus", 2);
            System.out.println("dayussst" + mapS);
            List<NxJrdhSupplierEntity> supplierEntities = gbDPBService.querySupplierList(mapS);
            mapR.put("arr", supplierEntities);
        } else if (type == 5) {
            Map<String, Object> map4 = new HashMap<>();
            map4.put("disId", disId);
            map4.put("dayStatus", 2);
            map4.put("startDate", startDate);
            map4.put("stopDate", stopDate);
            System.out.println("map4444444=====aaa" + map4);

            List<NxDistributerEntity> distributerEntities = gbDepartmentBillService.queryNxDistributer(map4);
            List<Map<String, Object>> result = new ArrayList<>();
//            if (distributerEntities.size() > 0) {
//                for (int i = 0; i < distributerEntities.size(); i++) {
//
//                    NxDistributerEntity distributerEntity = distributerEntities.get(i);
//                    Map<String, Object> mapItem = new HashMap<>();
//                    mapItem.put("nxDis", distributerEntity);
//                    Map<String, Object> mapB = new HashMap<>();
//
//                    mapB.put("startDate", startDate);
//                    mapB.put("stopDate", stopDate);
//                    mapB.put("disId", distributerEntity.getNxDistributerId());
//                    mapB.put("gbDisId", disId);
//                    List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(mapB);
//                    mapItem.put("arr", billEntityList);
//
//                    result.add(mapItem);
//
//                }
//            }
            mapR.put("arr", distributerEntities);
        }

        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/userGetDinghuoByDate", method = RequestMethod.POST)
    @ResponseBody
    public R userGetDinghuoByDate(Integer userId, Integer type, String date) {

        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("payType", type);
        map.put("date", date);

        System.out.println("gbgbbgbgb");
        List<GbDistributerPurchaseBatchEntity> batchEntities = gbDPBService.queryDisPurchaseBatch(map);
        return R.ok().put("data", batchEntities);
    }


    @RequestMapping(value = "/getPurchaserPurBill", method = RequestMethod.POST)
    @ResponseBody
    public R getPurchaserPurBill(Integer userId, String date) {
        System.out.println(date);
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("date", date);
        List<GbDistributerPurchaseBatchEntity> entities = gbDPBService.queryDisPurchaseBatch(map);

        return R.ok().put("data", entities);
    }


    @RequestMapping(value = "/getPaymentDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getPaymentDetail(Integer payId, Integer nxDisId) {
        Map<String, Object> mapR = new HashMap<>();
        List<GbDepartmentBillEntity> billEntities = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("payId", payId);
        if (nxDisId == -1) {
            List<GbDistributerPurchaseBatchEntity> purchaseBatch = gbDPBService.queryDisPurchaseBatch(map);
            mapR.put("arr", purchaseBatch);
        } else {
            billEntities = gbDepartmentBillService.queryBillsByParamsGb(map);
            mapR.put("arr", billEntities);
        }

        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/getDistributerAccounting/{disId}")
    @ResponseBody
    public R getDistributerAccounting(@PathVariable Integer disId) {

        BigDecimal mapResult = new BigDecimal(0);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("disId", disId);
        map2.put("equalStatus", 3);
        List<GbDepartmentEntity> departmentEntities = gbDPBService.queryDistributerAccountingData(map2);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntities) {
                Integer gbDepartmentId = dep.getGbDepartmentId();
                Map<String, Object> map3 = new HashMap<>();
                map3.put("purDepId", gbDepartmentId);
                map3.put("equalStatus", 3);
                map3.put("subDayu", 1);
                Double aDouble = gbDPBService.querySupplierUnSettleSubtotal(map3);
                BigDecimal result = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);

                //tuihuo
                Map<String, Object> map333 = new HashMap<>();
                map333.put("purDepId", gbDepartmentId);
                map333.put("equalStatus", 3);
                map333.put("subDayu", -1);
                int returnlist = gbDPBService.queryReturnList(map333);
                System.out.println(returnlist + "reuturlist");
                if (returnlist > 0) {
                    Map<String, Object> map33 = new HashMap<>();
                    map33.put("purDepId", gbDepartmentId);
                    map33.put("equalStatus", 3);
                    map33.put("subDayu", -1);
                    Double aDouble3 = gbDPBService.querySupplierUnSettleSubtotal(map33);
                    BigDecimal returnResult = new BigDecimal(aDouble3);
                    BigDecimal subtract = result.subtract(returnResult.abs()).setScale(2, BigDecimal.ROUND_HALF_UP);
                    dep.setUnPayTotal(subtract.toString());
                    mapResult = mapResult.add(subtract);
                    System.out.println(mapResult + "mapresitutuutu");
                } else {
                    dep.setUnPayTotal(result.toString());
                    mapResult = mapResult.add(result);
                    System.out.println(mapResult + "mapresitutuutu5555555");
                }
            }

//            Map<String, Object> map1 = new HashMap<>();
//            map1.put("disId", disId);
//            map1.put("equalStatus", 3);
//            Double aDouble = gbDPBService.querySupplierUnSettleSubtotal(map1);
//            BigDecimal mapResult = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);

            Map<String, Object> map = new HashMap<>();
            map.put("deps", departmentEntities);
            map.put("subtotal", mapResult);
            return R.ok().put("data", map);
        } else {
            return R.error(-1, "没有数据");
        }
    }


    @RequestMapping(value = "/depGetUnSettleSupplierBills/{depId}")
    @ResponseBody
    public R depGetUnSettleSupplierBills(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", depId);
        map.put("equalStatus", 3);
        map.put("payType", 1);

        List<GbDistributerPurchaseBatchEntity> billEntityList = gbDPBService.queryDisPurchaseBatch(map);
        return R.ok().put("data", billEntityList);
    }


    @RequestMapping(value = "/disGetUnSettleSupplierAccountBills/{supplierId}")
    @ResponseBody
    public R disGetUnSettleSupplierAccountBills(@PathVariable Integer supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", supplierId);
        map.put("equalStatus", 3);
        List<GbDistributerPurchaseBatchEntity> billEntityList = gbDPBService.queryDisPurchaseBatch(map);
        if (billEntityList.size() > 0) {
            return R.ok().put("data", billEntityList);
        } else {
            return R.error(-1, "没有订单");
        }
    }

    @RequestMapping(value = "/purchaserEditBatch/{batchId}")
    @ResponseBody
    public R purchaserEditBatch(@PathVariable Integer batchId) {
        GbDistributerPurchaseBatchEntity gbDisPurBatchEntity = gbDPBService.queryObject(batchId);
        if (gbDisPurBatchEntity.getGbDpbStatus() == 2) {

            List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDPGService.queryPurchaseGoodsByBatchId(batchId);

            if (purchaseGoodsEntities.size() > 0) {
                for (GbDistributerPurchaseGoodsEntity purGoods : purchaseGoodsEntities) {
                    purGoods.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
                    gbDPGService.update(purGoods);

                    //
                    Integer purchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("purGoodsId", purchaseGoodsId);
                    List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
                    if (ordersEntities.size() > 0) {
                        for (GbDepartmentOrdersEntity order : ordersEntities) {
                            order.setGbDoStatus(getGbOrderBuyStatusProcurement());
                            gbDepartmentOrdersService.update(order);
                        }
                    }
                }


                //
            }

            gbDisPurBatchEntity.setGbDpbStatus(1);
            gbDPBService.update(gbDisPurBatchEntity);
        }
        return R.ok();
    }

    @RequestMapping(value = "/supplierEditBatchGb/{batchId}")
    @ResponseBody
    public R supplierEditBatchGb(@PathVariable Integer batchId) {
        GbDistributerPurchaseBatchEntity gbDisPurBatchEntity = gbDPBService.queryObject(batchId);
        if (gbDisPurBatchEntity.getGbDpbStatus() == 2) {
            gbDisPurBatchEntity.setGbDpbStatus(0);
            gbDPBService.update(gbDisPurBatchEntity);

            List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDPGService.queryPurchaseGoodsByBatchId(batchId);
            if (purchaseGoodsEntities.size() > 0) {
                for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                    purchaseGoodsEntity.setGbDpgStatus(1);
                    gbDPGService.update(purchaseGoodsEntity);
                }
            }
        }
        GbDistributerPurchaseBatchEntity entity = gbDPBService.queryBatchWithOrders(batchId);
        return R.ok().put("data", entity);
    }

    @RequestMapping(value = "/supplierGetPrintBatchGb/{batchId}")
    @ResponseBody
    public R supplierGetPrintBatchGb(@PathVariable Integer batchId) {
        Map<String, Object> map = new HashMap<>();

        System.out.println("supplierGetPrintBatchGbsupplierGetPrintBatchGb");
        map.put("batchId", batchId);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);

        return R.ok().put("data", ordersEntities);
    }

    @RequestMapping(value = "/supplierPrintBatchGb/{batchId}")
    @ResponseBody
    public R supplierPrintBatchGb(@PathVariable Integer batchId) {
        GbDistributerPurchaseBatchEntity gbDisPurBatchEntity = gbDPBService.queryObject(batchId);
        if (gbDisPurBatchEntity.getGbDpbStatus() == 2) {
            gbDisPurBatchEntity.setGbDpbStatus(3);
            gbDPBService.update(gbDisPurBatchEntity);
        }
        GbDistributerPurchaseBatchEntity entity = gbDPBService.queryBatchWithOrders(batchId);
        return R.ok().put("data", entity);
    }

    @RequestMapping(value = "/getGbDepartmentPurBatch/{depFatherId}")
    @ResponseBody
    public R getGbDepartmentPurBatch(@PathVariable Integer depFatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", depFatherId);
        map.put("month", formatWhatMonth(0));
        map.put("dayuStatus", 1);
        List<GbDistributerPurchaseBatchEntity> purchaseBatch = gbDPBService.queryDisPurchaseBatch(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("month", formatWhatMonth(0));
        map1.put("arr", purchaseBatch);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("purDepId", depFatherId);
        map2.put("month", getLastMonth());
        map2.put("dayuStatus", 1);

        List<GbDistributerPurchaseBatchEntity> purchaseBatch2 = gbDPBService.queryDisPurchaseBatch(map2);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("month", getLastMonth());
        map3.put("arr", purchaseBatch2);

        Map<String, Object> map4 = new HashMap<>();
        map4.put("purDepId", depFatherId);
        map4.put("month", getLastTwoMonth());
        map4.put("dayuStatus", 1);
        List<GbDistributerPurchaseBatchEntity> purchaseBatch3 = gbDPBService.queryDisPurchaseBatch(map4);
        Map<String, Object> map5 = new HashMap<>();
        map5.put("month", getLastTwoMonth());
        map5.put("arr", purchaseBatch3);

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(map1);
        result.add(map3);
        result.add(map5);
        return R.ok().put("data", result);
    }


    @RequestMapping(value = "/getPurchaserPurBatch/{userId}")
    @ResponseBody
    public R getPurchaserPurBatch(@PathVariable Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("dayuStatus", 1);
        map.put("status", 5);

        List<GbDistributerPurchaseBatchEntity> purchaseBatch = gbDPBService.queryDisPurchaseBatch(map);
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("month", formatWhatMonth(0));
//        map1.put("arr", purchaseBatch);
//
//        Map<String, Object> map2 = new HashMap<>();
//        map2.put("purUserId", userId);
//        map2.put("month", getLastMonth());
//        map2.put("dayuStatus", 1);
//
//        List<GbDistributerPurchaseBatchEntity> purchaseBatch2 = gbDPBService.queryDisPurchaseBatch(map2);
//        Map<String, Object> map3 = new HashMap<>();
//        map3.put("month", getLastMonth());
//        map3.put("arr", purchaseBatch2);
//
//        Map<String, Object> map4 = new HashMap<>();
//        map4.put("purUserId", userId);
//        map4.put("month", getLastTwoMonth());
//        map4.put("dayuStatus", 1);
//        List<GbDistributerPurchaseBatchEntity> purchaseBatch3 = gbDPBService.queryDisPurchaseBatch(map4);
//        Map<String, Object> map5 = new HashMap<>();
//        map5.put("month", getLastTwoMonth());
//        map5.put("arr", purchaseBatch3);

//        List<Map<String, Object>> result = new ArrayList<>();
//        result.add(map1);
//        result.add(map3);
//        result.add(map5);

        return R.ok().put("data", purchaseBatch);
    }


    @RequestMapping(value = "/sellerDistributerPurchaseBatchsGb", method = RequestMethod.POST)
    @ResponseBody
    public R sellerDistributerPurchaseBatchsGb(Integer disId, Integer supplierId) {

        Double resultUnPay = 0.0;
        Double resultPay = 0.0;
        Double resultUnPay1 = 0.0;
        Double resultPay1 = 0.0;
        Double resultUnPay2 = 0.0;
        Double resultPay2 = 0.0;
        Double resultPayTotal = 0.0;
        //第一个月
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("supplierId", supplierId);
        map.put("month", formatWhatMonth(0));
        map.put("year", formatWhatYear(0));
//        List<GbDistributerPurchaseBatchEntity> batchEntities = gbDPBService.queryDisPurchaseBatch(map);
        List<GbDistributerPurchaseBatchEntity> batchEntities = gbDPBService.queryDisPurchaseBatchInfo(map);
        System.out.println("bathchssisiis" + batchEntities.size());
        map.put("dayuStatus", 1);
        map.put("status", 4);
        map.put("payType", 1);
        System.out.println("onenennecoun" + map);
        int i = gbDPBService.queryDisPurchaseBatchCount(map);
        System.out.println("onenennecoun" + map);

        if (i > 0) {
            resultUnPay = gbDPBService.queryPurchaserCashTotal(map);
        }

        Map<String, Object> mapPay = new HashMap<>();
        mapPay.put("disId", disId);
        mapPay.put("supplierId", supplierId);
        mapPay.put("month", formatWhatMonth(0));
        mapPay.put("year", formatWhatYear(0));
        mapPay.put("dayuStatus", 1);
        int iUnpay = gbDPBService.queryDisPurchaseBatchCount(mapPay);
        if (iUnpay > 0) {
            resultPay = gbDPBService.queryPurchaserCashTotal(mapPay);
        }

        //第二个月
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("supplierId", supplierId);
        map1.put("month", getLastMonth());
        map1.put("year", formatWhatYear(0));
        List<GbDistributerPurchaseBatchEntity> batchEntities1 = gbDPBService.queryDisPurchaseBatchInfo(map1);
        map1.put("dayuStatus", 1);
        map1.put("status", 4);
        map1.put("payType", 1);
        int i1 = gbDPBService.queryDisPurchaseBatchCount(map1);
        if (i1 > 0) {
            resultUnPay1 = gbDPBService.queryPurchaserCashTotal(map1);
        }

        Map<String, Object> mapPay1 = new HashMap<>();
        mapPay1.put("disId", disId);
        mapPay1.put("supplierId", supplierId);
        mapPay1.put("month", formatWhatMonth(0));
        mapPay1.put("year", formatWhatYear(0));
        mapPay1.put("dayuStatus", 1);
        int iUnpay1 = gbDPBService.queryDisPurchaseBatchCount(mapPay1);
        if (iUnpay1 > 0) {
            resultPay1 = gbDPBService.queryPurchaserCashTotal(mapPay1);
        }
        //第三个月
        Map<String, Object> map2 = new HashMap<>();
        map2.put("disId", disId);
        map2.put("supplierId", supplierId);
        map2.put("month", getLastTwoMonth());
        map2.put("year", formatWhatYear(0));
        List<GbDistributerPurchaseBatchEntity> batchEntities2 = gbDPBService.queryDisPurchaseBatchInfo(map2);
        map2.put("dayuStatus", 1);
        map2.put("status", 4);
        map2.put("payType", 1);
        System.out.println("isisisiisisisisisiisisi" + map2);
        int i2 = gbDPBService.queryDisPurchaseBatchCount(map2);
        if (i2 > 0) {
            resultUnPay2 = gbDPBService.queryPurchaserCashTotal(map2);
        }

        Map<String, Object> mapPay2 = new HashMap<>();
        mapPay2.put("disId", disId);
        mapPay2.put("supplierId", supplierId);
        mapPay2.put("month", formatWhatMonth(0));
        mapPay2.put("year", formatWhatYear(0));
        mapPay2.put("dayuStatus", 1);
        System.out.println("mapp222222" + map2);
        int iUnpay2 = gbDPBService.queryDisPurchaseBatchCount(mapPay2);
        if (iUnpay2 > 0) {
            resultPay2 = gbDPBService.queryPurchaserCashTotal(mapPay2);
        }

        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", batchEntities);
        map3.put("month", formatWhatMonth(0));
        map3.put("payTotal", String.format("%.1f", resultPay));
        map3.put("unPayTotal", String.format("%.1f", resultUnPay));
        Map<String, Object> map4 = new HashMap<>();
        map4.put("arr", batchEntities1);
        map4.put("month", getLastMonth());
        map4.put("payTotal", String.format("%.1f", resultPay1));
        map4.put("unPayTotal", String.format("%.1f", resultUnPay1));
        Map<String, Object> map5 = new HashMap<>();
        map5.put("arr", batchEntities2);
        map5.put("month", getLastTwoMonth());
        map5.put("payTotal", String.format("%.1f", resultPay2));
        map5.put("unPayTotal", String.format("%.1f", resultUnPay2));

        List<Map<String, Object>> resultData = new ArrayList<>();
        resultData.add(map3);
        resultData.add(map4);
        resultData.add(map5);


        Map<String, Object> mapPayTotal = new HashMap<>();
        mapPayTotal.put("disId", disId);
        mapPayTotal.put("supplierId", supplierId);
        mapPayTotal.put("dayuStatus", 1);
        mapPayTotal.put("payType", 1);
        int iUnpayTotal = gbDPBService.queryDisPurchaseBatchCount(mapPayTotal);
        if (iUnpayTotal > 0) {
            resultPayTotal = gbDPBService.queryPurchaserCashTotal(mapPayTotal);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("resultPayTotal", new BigDecimal(resultPayTotal).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("arr", resultData);
        mapR.put("disInfo", gbDistributerService.queryObject(disId));

//        Map<String, Object> mapSupplier = new HashMap<>();
//        mapSupplier.put("gbDisId", disId);
//        mapSupplier.put("supplierId",supplierId );
        NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.queryObject(supplierId);
        mapR.put("supplierInfo", supplierEntity);

        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/disGetGbSupplierBillsWithStatus", method = RequestMethod.POST)
    @ResponseBody
    public R disGetGbSupplierBillsWithStatus(Integer supplierId, Integer status, String startDate, String stopDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", supplierId);
        map.put("dayuStatus", status);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        List<GbDistributerPurchaseBatchEntity> purchaseBatch = gbDPBService.queryDisPurchaseBatchInfo(map);

        map.put("status", 4);
//        map.put("payType", 1);
        Integer integer = gbDPBService.queryDisPurchaseBatchCount(map);
        Double aDouble = 0.0;
        System.out.println("sttussamp" + map);
        if (integer > 0) {
            aDouble = gbDPBService.querySupplierUnSettleSubtotal(map);
        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("total", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("arr", purchaseBatch);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/disGetGbSupplierBills/{supplierId}")
    @ResponseBody
    public R disGetGbSupplierBills(@PathVariable Integer supplierId) {

        BigDecimal listTotal = new BigDecimal("0.0");
        double unSettleSubtotal = 0.0;

        //第一个月账单
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", supplierId);
        map.put("month", formatWhatMonth(0));
        map.put("dayuStatus", 1);
        String totalDec1 = "0";
        List<GbDistributerPurchaseBatchEntity> purchaseBatch = gbDPBService.queryDisPurchaseBatch(map);
        BigDecimal bigDecimal = new BigDecimal(purchaseBatch.size());
        listTotal = listTotal.add(bigDecimal); //账单数量

        Map<String, Object> map41 = new HashMap<>();
        map41.put("supplierId", supplierId);
        map41.put("month", formatWhatMonth(0));
        map41.put("dayuStatus", 1);
        map41.put("status", 4);
//        map41.put("payType", 1);
        System.out.println("41mapapapap" + map41);
        Integer integer = gbDPBService.queryDisPurchaseBatchCount(map41);
        if (integer > 0) {
            Double total1 = gbDPBService.querySupplierUnSettleSubtotal(map41);
            unSettleSubtotal = unSettleSubtotal + total1; //未结账款总额
            totalDec1 = String.format("%.2f", total1);
        }
        Map<String, Object> map1 = new HashMap<>();
        map1.put("month", formatWhatMonth(0));
        map1.put("arr", purchaseBatch);
        map1.put("total", totalDec1);


        //第二个月账单
        Map<String, Object> map2 = new HashMap<>();
        map2.put("supplierId", supplierId);
        map2.put("month", getLastMonth());
        map2.put("dayuStatus", 1);
        List<GbDistributerPurchaseBatchEntity> purchaseBatch2 = gbDPBService.queryDisPurchaseBatch(map2);
        BigDecimal bigDecimal2 = new BigDecimal(purchaseBatch2.size());
        listTotal = listTotal.add(bigDecimal2); //账单数量

        String totalDec2 = "0";
        Map<String, Object> map42 = new HashMap<>();
        map42.put("supplierId", supplierId);
        map42.put("month", getLastMonth());
        map42.put("dayuStatus", 1);
        map42.put("status", 4);
//        map42.put("payType", 1);
        Integer integer1 = gbDPBService.queryDisPurchaseBatchCount(map42);
        if (integer1 > 0) {
            Double total2 = gbDPBService.querySupplierUnSettleSubtotal(map42);
            unSettleSubtotal = unSettleSubtotal + total2; //未结账款总额
            totalDec2 = String.format("%.2f", total2);
        }

        Map<String, Object> map3 = new HashMap<>();
        map3.put("month", getLastMonth());
        map3.put("arr", purchaseBatch2);
        map3.put("total", totalDec2);

        //第三个月账单
        Map<String, Object> map4 = new HashMap<>();
        map4.put("supplierId", supplierId);
        map4.put("month", getLastTwoMonth());
        map4.put("dayuStatus", 1);
        List<GbDistributerPurchaseBatchEntity> purchaseBatch3 = gbDPBService.queryDisPurchaseBatch(map4);
        BigDecimal bigDecimal3 = new BigDecimal(purchaseBatch3.size());
        listTotal = listTotal.add(bigDecimal3);

        String totalDec3 = "0";
        Map<String, Object> map43 = new HashMap<>();
        map43.put("supplierId", supplierId);
        map43.put("month", getLastTwoMonth());
        map43.put("dayuStatus", 1);
        map43.put("status", 4);
//        map43.put("payType", 1);
        Integer integer2 = gbDPBService.queryDisPurchaseBatchCount(map43);

        if (integer2 > 0) {
            Double total3 = gbDPBService.querySupplierUnSettleSubtotal(map43);
            unSettleSubtotal = unSettleSubtotal + total3; //未结账款总额
            totalDec3 = String.format("%.2f", total3);
        }

        Map<String, Object> map5 = new HashMap<>();
        map5.put("month", getLastTwoMonth());
        map5.put("arr", purchaseBatch3);
        map5.put("total", totalDec3);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("unSettleSubtotal", unSettleSubtotal);
        map111.put("listTotal", listTotal);

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(map1);
        result.add(map3);
        result.add(map5);
        result.add(map111);
        return R.ok().put("data", result);

    }


    @RequestMapping(value = "/finishReturnToSuppler", method = RequestMethod.POST)
    @ResponseBody
    public R finishReturnToSuppler(@RequestBody GbDistributerPurchaseBatchEntity batchEntity) {

        //1，保存供应商退货单
        if (batchEntity.getGbDpbPayType() == 0) {
            batchEntity.setGbDpbStatus(4); //如果是现金 status == 4 完成结账状态
        } else {
            batchEntity.setGbDpbStatus(3); //如果是记账，status == 2， 开票完成后，status == 3
        }
        batchEntity.setGbDpbDate(formatWhatDay(0));
        batchEntity.setGbDpbHour(formatWhatHour(0));
        batchEntity.setGbDpbMinute(formatWhatMinute(0));
        batchEntity.setGbDpbTime(formatWhatTime(0));
        batchEntity.setGbDpbPurchaseMonth(formatWhatMonth(0));
        batchEntity.setGbDpbPurchaseWeek(getWeek(0));
        batchEntity.setGbDpbPurchaseYear(formatWhatYear(0));
        batchEntity.setGbDpbPurchaseFullTime(formatWhatYearDayTime(0));
        batchEntity.setGbDpbFinishFullTime(formatWhatYearDayTime(0));
        batchEntity.setGbDpbPaySubtotal(batchEntity.getGbDpbSubtotal());

        gbDPBService.save(batchEntity);


        //3，保存部门退货单
        GbDepartmentBillEntity billEntity = new GbDepartmentBillEntity();
        billEntity.setGbDbOrderAmount(1);
        billEntity.setGbDbIssueUserId(batchEntity.getGbDpbPurUserId());
        billEntity.setGbDbStatus(0);
        billEntity.setGbDbDate(formatWhatDay(0));
        billEntity.setGbDbMonth(formatWhatMonth(0));
        billEntity.setGbDbWeek(getWeek(0));
        billEntity.setGbDbTime(formatFullTime());
        billEntity.setGbDbTotal(batchEntity.getGbDpbSubtotal());
        billEntity.setGbDbDepId(batchEntity.getGbDPGEntities().get(0).getGbDepartmentOrdersEntities().get(0).getGbDoDepartmentFatherId());
        billEntity.setGbDbDisId(batchEntity.getGbDpbDistributerId());
        billEntity.setGbDbIssueDepId(batchEntity.getGbDpbPurDepartmentId());
        billEntity.setGbDbIssueOrderType(getGbOrderTypeTuihuo());
        String areaCode = "1" + batchEntity.getGbDpbDistributerId();
        billEntity.setGbDbTradeNo(generateBillTradeNo(areaCode));
        gbDepartmentBillService.save(billEntity);


        //2， 修改订单和采购商品状态
        for (GbDistributerPurchaseGoodsEntity purGoods : batchEntity.getGbDPGEntities()) {
            purGoods.setGbDpgBatchId(batchEntity.getGbDistributerPurchaseBatchId());
            purGoods.setGbDpgStatus(3);
            purGoods.setGbDpgPayType(batchEntity.getGbDpbPayType());
            purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
            purGoods.setGbDpgPurchaseYear(formatWhatYear(0));
            purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
            purGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
            purGoods.setGbDpgPurchaseYear(formatWhatYear(0));
            purGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
            purGoods.setGbDpgPurchaseWeek(getWeek(0));
            purGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            purGoods.setGbDpgTime(formatWhatTime(0));
            gbDPGService.update(purGoods);

            Integer gbDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", gbDistributerPurchaseGoodsId);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);

            for (GbDepartmentOrdersEntity orders : ordersEntities) {

                Integer gbDoDgsrReturnId = orders.getGbDoDgsrReturnId();
                GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentStockReduceService.queryObject(gbDoDgsrReturnId);
                reduceEntity.setGbDgsrStatus(0);
                gbDepartmentStockReduceService.update(reduceEntity);
                orders.setGbDoArriveWeeksYear(getWeekOfYear(0));
                orders.setGbDoArriveWhatDay(getWeek(0));
                orders.setGbDoArriveOnlyDate(formatWhatDate(0));
                orders.setGbDoArriveDate(formatWhatDay(0));
                orders.setGbDoStatus(4);
                orders.setGbDoBuyStatus(3);
                orders.setGbDoBillId(billEntity.getGbDepartmentBillId());
                gbDepartmentOrdersService.update(orders);
            }
        }

        return R.ok();
    }


    @RequestMapping(value = "/finishSharePurGoodsBatchReturn/{batchId}")
    @ResponseBody
    public R finishSharePurGoodsBatchReturn(@PathVariable Integer batchId) {
        System.out.println("baucicic" + batchId);
        GbDistributerPurchaseBatchEntity batch = gbDPBService.queryObject(batchId);
        Map<String, Object> mapP = new HashMap<>();
        mapP.put("batchId", batchId);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDPGService.queryPurchaseGoodsByParams(mapP);

        for (GbDistributerPurchaseGoodsEntity purGoods : purchaseGoodsEntities) {

            purGoods.setGbDpgStatus(getGbPurchaseGoodsStatusReceive());
            gbDPGService.update(purGoods);

            Integer gbDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", gbDistributerPurchaseGoodsId);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
            for (GbDepartmentOrdersEntity orders : ordersEntities) {
                Integer gbDoDgsrReturnId = orders.getGbDoDgsrReturnId();
                GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentStockReduceService.queryObject(gbDoDgsrReturnId);
                reduceEntity.setGbDgsrStatus(2);
                gbDepartmentStockReduceService.update(reduceEntity);
                orders.setGbDoStatus(getGbOrderStatusReceived()); //wancheng
                System.out.println("bactpey");
                if (batch.getGbDpbPayType() == 0) {
                    orders.setGbDoBuyStatus(getGbOrderBuyStatusHavePayFinish());
                } else {
                    orders.setGbDoBuyStatus(getGbOrderBuyStatusUnPayFinish());
                }
                gbDepartmentOrdersService.update(orders);
            }
        }

        batch.setGbDpbFinishFullTime(formatFullTime());
        if (batch.getGbDpbPayType() == 0) {
            batch.setGbDpbStatus(4); //如果是现金 status == 4 完成结账状态
        } else {
            batch.setGbDpbStatus(2); //如果是记账，status == 2， 开票完成后，status == 3
            Integer gbDpbSupplierId = batch.getGbDpbSupplierId();
            NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.queryObject(gbDpbSupplierId);
            Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
            System.out.println("dafdafas" + supplierEntity);
            if (nxJrdhsUserId == null) {
                System.out.println("dafafads" + nxJrdhsUserId);
                supplierEntity.setNxJrdhsUserId(batch.getGbDpbSellUserId());
                nxJrdhSupplierService.update(supplierEntity);
            }
        }
        gbDPBService.update(batch);

        return R.ok();


    }


    @RequestMapping(value = "/nxDisPrintGbPurBatch/{id}", method = RequestMethod.GET)
    @ResponseBody
    public R nxDisPrintGbPurBatch(@PathVariable Integer id) {
        System.out.println("nxDisPrintGbPurBatchnxDisPrintGbPurBatch");
        GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDPBService.queryObject(id);
        Integer gbDpbStatus = gbDistributerPurchaseBatchEntity.getGbDpbStatus();
        int orderCount = 0;
        if (gbDpbStatus == 1) {
            List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDPGService.queryPurchaseGoodsByBatchId(id);
            for (GbDistributerPurchaseGoodsEntity purGoods : purchaseGoodsEntities) {
//                purGoods.setGbDpgPayType(1);
//                purGoods.setGbDpgStatus();
//                purGoods.setGbDpgPurchaseNxSupplierId(gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());
//                gbDPGService.update(purGoods);
                orderCount += purGoods.getGbDpgOrdersAmount();
                Integer gbDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", gbDistributerPurchaseGoodsId);
                List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);

                for (GbDepartmentOrdersEntity orders : ordersEntities) {
                    if (orders.getGbDoOrderType().equals(getGbOrderTypeTuihuo())) {
                        Integer gbDoDgsrReturnId = orders.getGbDoDgsrReturnId();
                        GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentStockReduceService.queryObject(gbDoDgsrReturnId);
                        reduceEntity.setGbDgsrStatus(2);
                        gbDepartmentStockReduceService.update(reduceEntity);
                    } else {
//                        saveDepStockDataByPurchase(orders, gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());
                    }

                    orders.setGbDoStatus(getGbOrderStatusHasFinished()); //wancheng
                    orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
                    gbDepartmentOrdersService.update(orders);

                    Integer gbDoNxDepartmentOrderId = orders.getGbDoNxDepartmentOrderId();
                    NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
                    ordersEntity.setNxDoStatus(3);
                    nxDepartmentOrdersService.update(ordersEntity);

                }
            }

            gbDistributerPurchaseBatchEntity.setGbDpbFinishFullTime(formatFullTime());
            gbDistributerPurchaseBatchEntity.setGbDpbStatus(getGbDisPurchaseBatchDisUserWaitReceive());
//            gbDistributerPurchaseBatchEntity.setGbDpbStatus(());
            gbDPBService.update(gbDistributerPurchaseBatchEntity);

//            Integer gbDpbDistributerId = gbDistributerPurchaseBatchEntity.getGbDpbDistributerId();
//            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDpbDistributerId);

//            GbDistributerPayListEntity payListEntity = new GbDistributerPayListEntity();
//            payListEntity.setGbNdplPaySubtotal(Integer.valueOf(orderCount).toString());
//            payListEntity.setGbNdplPayTime(formatFullTime());
//            payListEntity.setGbNdplPayDate(formatWhatDay(0));
//            payListEntity.setGbNdplPayMonth(formatWhatMonth(0));
//            payListEntity.setGbNdplPayYear(formatWhatYear(0));
//            payListEntity.setGbNdplStatus(0);
//            payListEntity.setGbNdplType(0);
//            payListEntity.setGbNdplRestPoints(Integer.valueOf(gbDistributerEntity.getGbDistributerBuyQuantity()));
//            payListEntity.setGbNdplGbDisId(gbDistributerEntity.getGbDistributerId());
//            payListEntity.setGbNdplNxSupplierId(gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());
//            payListEntity.setGbNdplGbPbId(gbDistributerPurchaseBatchEntity.getGbDistributerPurchaseBatchId());
//            payListService.save(payListEntity);

//            BigDecimal decimal = new BigDecimal(gbDistributerEntity.getGbDistributerBuyQuantity());
            BigDecimal decimal1 = new BigDecimal(orderCount);
//            BigDecimal add = decimal.subtract(decimal1).setScale(0, BigDecimal.ROUND_HALF_UP);
//            gbDistributerEntity.setGbDistributerBuyQuantity(add.toString());
//            gbDistributerService.update(gbDistributerEntity);

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("supplierId", gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());
            mapR.put("gbDisId", gbDistributerPurchaseBatchEntity.getGbDpbDistributerId());
            mapR.put("nxDisId", gbDistributerPurchaseBatchEntity.getGbDpbNxDistributerId());
            NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDisGbDisService.queryObjectByParams(mapR);
            if (nxDistributerGbDistributerEntity.getNxDgdFromNxDisId() != null && !nxDistributerGbDistributerEntity.getNxDgdFromNxDisId().equals(gbDistributerPurchaseBatchEntity.getGbDpbNxDistributerId())) {
                NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(gbDistributerPurchaseBatchEntity.getGbDpbNxDistributerId());

                NxDistributerPayListEntity nxPayListEntity = new NxDistributerPayListEntity();
                nxPayListEntity.setNxNdplNxDisId(gbDistributerPurchaseBatchEntity.getGbDpbNxDistributerId());
                nxPayListEntity.setNxNdplNxDepartmentId(-1);
                nxPayListEntity.setNxNdplNxDepartmentFatherId(-1);
                nxPayListEntity.setNxNdplPaySubtotal(Integer.valueOf(orderCount).toString());
                nxPayListEntity.setNxNdplPayTime(formatFullTime());
                nxPayListEntity.setNxNdplPayDate(formatWhatDay(0));
                nxPayListEntity.setNxNdplPayMonth(formatWhatMonth(0));
                nxPayListEntity.setNxNdplPayYear(formatWhatYear(0));
                nxPayListEntity.setNxNdplStatus(0);
                nxPayListEntity.setNxNdplType(getNxDisPayListPrinter());
                nxPayListEntity.setNxNdplRestPoints(Integer.valueOf(nxDistributerEntity.getNxDistributerBuyQuantity()));
                nxPayListService.save(nxPayListEntity);

                BigDecimal decimal0 = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());
                BigDecimal restPoints = decimal0.subtract(decimal1).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxDistributerEntity.setNxDistributerBuyQuantity(restPoints.toString());
                nxDistributerService.update(nxDistributerEntity);
            }

            return R.ok();

        } else {
            return R.error(-1, "请刷新数据。");
        }
    }


    @RequestMapping(value = "/finishSharePurGoodsBatch")
    @ResponseBody
    public R finishSharePurGoodsBatch(@RequestBody GbDistributerPurchaseBatchEntity batch) {
        Integer gbDistributerPurchaseBatchId = batch.getGbDistributerPurchaseBatchId();
        GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDPBService.queryObject(gbDistributerPurchaseBatchId);
        Integer gbDpbStatus = gbDistributerPurchaseBatchEntity.getGbDpbStatus();
        int orderCount = 0;
        if (gbDpbStatus == 2) {
            for (GbDistributerPurchaseGoodsEntity purGoods : batch.getGbDPGEntities()) {
                purGoods.setGbDpgPayType(batch.getGbDpbPayType());
                purGoods.setGbDpgStatus(3);
                purGoods.setGbDpgPurchaseType(21);
                purGoods.setGbDpgPurchaseNxSupplierId(gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());
                gbDPGService.update(purGoods);
                orderCount += purGoods.getGbDpgOrdersAmount();
                Integer gbDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", gbDistributerPurchaseGoodsId);
                List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);

                for (GbDepartmentOrdersEntity orders : ordersEntities) {
                    BigDecimal gbDoWeight = new BigDecimal(orders.getGbDoWeight());
                    BigDecimal gbDoPrice = new BigDecimal(orders.getGbDoPrice());
                    BigDecimal gbDoSubtotal = new BigDecimal(orders.getGbDoSubtotal());

                    if (gbDoWeight.compareTo(BigDecimal.ZERO) == 1 && gbDoPrice.compareTo(BigDecimal.ZERO) == 1 &&
                            gbDoSubtotal.compareTo(BigDecimal.ZERO) == 1) {

                        if (orders.getGbDoOrderType().equals(getGbOrderTypeTuihuo())) {
                            Integer gbDoDgsrReturnId = orders.getGbDoDgsrReturnId();
                            GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentStockReduceService.queryObject(gbDoDgsrReturnId);
                            reduceEntity.setGbDgsrStatus(2);
                            gbDepartmentStockReduceService.update(reduceEntity);
                        } else {
                            saveDepStockDataByPurchase(orders, gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());
                        }

                        orders.setGbDoStatus(getGbOrderStatusReceived()); //wancheng
                        if (batch.getGbDpbPayType() == 0) {
                            orders.setGbDoBuyStatus(getGbOrderBuyStatusHavePayFinish());
                        } else {
                            orders.setGbDoBuyStatus(getGbOrderBuyStatusUnPayFinish());
                        }

                        gbDepartmentOrdersService.update(orders);

                    }


                }
            }

            batch.setGbDpbFinishFullTime(formatFullTime());
            if (batch.getGbDpbPayType() == 0) {
                batch.setGbDpbStatus(4); //如果是现金 status == 4 完成结账状态
            } else {
                batch.setGbDpbStatus(3); //如果是记账，status == 2， 开票完成后，status == 3
//                Integer gbDpbSupplierId = batch.getGbDpbSupplierId();
//                NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.queryObject(gbDpbSupplierId);
//                Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
//                System.out.println("dafdafas" + supplierEntity);
//                if (nxJrdhsUserId == null) {
//                    System.out.println("dafafads" + nxJrdhsUserId);
//                    supplierEntity.setNxJrdhsUserId(batch.getGbDpbSellUserId());
//                    nxJrdhSupplierService.update(supplierEntity);
//                }
            }
            gbDPBService.update(batch);

            Integer gbDpbDistributerId = batch.getGbDpbDistributerId();
            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDpbDistributerId);

            GbDistributerPayListEntity payListEntity = new GbDistributerPayListEntity();
            payListEntity.setGbNdplPaySubtotal(Integer.valueOf(orderCount).toString());
            payListEntity.setGbNdplPayTime(formatFullTime());
            payListEntity.setGbNdplPayDate(formatWhatDay(0));
            payListEntity.setGbNdplPayMonth(formatWhatMonth(0));
            payListEntity.setGbNdplPayYear(formatWhatYear(0));
            payListEntity.setGbNdplStatus(0);
            payListEntity.setGbNdplType(0);
            payListEntity.setGbNdplRestPoints(Integer.valueOf(gbDistributerEntity.getGbDistributerBuyQuantity()));
            payListEntity.setGbNdplGbDisId(gbDistributerEntity.getGbDistributerId());
            payListEntity.setGbNdplNxSupplierId(gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());
            payListEntity.setGbNdplGbPbId(gbDistributerPurchaseBatchEntity.getGbDistributerPurchaseBatchId());
            payListService.save(payListEntity);

            BigDecimal decimal = new BigDecimal(gbDistributerEntity.getGbDistributerBuyQuantity());
            System.out.println("decimdall========" + decimal);
            BigDecimal decimal1 = new BigDecimal(orderCount);
            System.out.println("nxdkkddkdk00" + gbDistributerEntity.getGbDistributerBuyQuantity());
            BigDecimal add = decimal.subtract(decimal1).setScale(0, BigDecimal.ROUND_HALF_UP);
            gbDistributerEntity.setGbDistributerBuyQuantity(add.toString());
            System.out.println("nxdkkddkdk11" + gbDistributerEntity.getGbDistributerBuyQuantity());
            gbDistributerService.update(gbDistributerEntity);

            return R.ok();

        } else {
            return R.error(-1, "请刷新数据。");
        }
    }

    @ResponseBody
    @RequestMapping(value = "/finishShixianBill", method = RequestMethod.POST)
    public R finishShixianBill(@RequestBody GbDepartmentBillEntity billEntity) {
        System.out.println("billl" + billEntity);
        //转换总金额
        String nxRbTotal = billEntity.getGbDbPayTotal();
        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);

        //订单号
        String tradeNo = CommonUtils.generateOutTradeNo();
        //餐馆支付配置
        MyWxLaoduShixianPayConfig config = new MyWxLaoduShixianPayConfig();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/gbdistributerpurchasebatch/notifyShixian");
        params.put("trade_type", "JSAPI");
        params.put("openid", billEntity.getGbUserOpenId());

        //map转xml
        try {

            WXPay wxpay = new WXPay(config);
            long time = System.currentTimeMillis();
            String tString = String.valueOf(time / 1000);
            Map<String, String> resp = wxpay.unifiedOrder(params);
            System.out.println(resp);
            SortedMap<String, String> reMap = new TreeMap<>();
            reMap.put("appId", config.getAppID());
            reMap.put("nonceStr", resp.get("nonce_str"));
            reMap.put("package", "prepay_id=" + resp.get("prepay_id"));
            reMap.put("signType", "MD5");
            reMap.put("timeStamp", tString);
            String s = WxPayUtils.creatSign(reMap, config.getKey());
            reMap.put("paySign", s);


            GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryDepUserByOpenId(billEntity.getGbUserOpenId());
            billEntity.setGbDbIssueUserId(gbDepartmentUserEntity.getGbDepartmentUserId());
            billEntity.setGbDbWxOutTradeNo(tradeNo);
            gbDepartmentBillService.update(billEntity);

            //setAutoGoods

            return R.ok().put("map", reMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok();
    }

    @RequestMapping("/notifyShixian")
    public String callBackShixian(HttpServletRequest request, HttpServletResponse response) {
        // System.out.println("微信支付成功,微信发送的callback信息,请注意修改订单信息");
        InputStream is = null;
        try {

            is = request.getInputStream();// 获取请求的流信息(这里是微信发的xml格式所有只能使用流来读)
            String xml = WxPayUtils.InputStream2String(is);
            Map<String, String> notifyMap = WxPayUtils.xmlToMap(xml);// 将微信发的xml转map
            System.out.println("微信返回给回调函数的信息为：" + xml);
            if (notifyMap.get("result_code").equals("SUCCESS")) {
                /*
                 * 以下是自己的业务处理------仅做参考 更新order对应字段/已支付金额/状态码
                 * 更新bill支付状态
                 */
                System.out.println("===notify===回调方法已经被调！！！");
                String ordersSn = notifyMap.get("out_trade_no");// 商户订单号
                GbDepartmentBillEntity gbDepartmentBillEntity = gbDepartmentBillService.queryDepartBillByTradeNo(ordersSn);
                Integer gbDbSetAutoGoods = gbDepartmentBillEntity.getGbDbSetAutoGoods();

                Map<String, Object> map = new HashMap<>();
                map.put("billId", gbDepartmentBillEntity.getGbDepartmentBillId());
                List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
                for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : ordersEntities) {
                    gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusReceived()); //wancheng
                    gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHavePayFinish());
                    gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
                    if (gbDbSetAutoGoods == 1) {
                        Integer gbDoDisGoodsId = gbDepartmentOrdersEntity.getGbDoDisGoodsId();
                        GbDistributerGoodsEntity distributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
                        distributerGoodsEntity.setGbDgGoodsType(5);
                        distributerGoodsEntity.setGbDgNxDistributerId(gbDepartmentOrdersEntity.getGbDoNxDistributerId());
                        distributerGoodsEntity.setGbDgNxDistributerGoodsId(gbDepartmentOrdersEntity.getGbDoNxDistributerGoodsId());
                        gbDistributerGoodsService.update(distributerGoodsEntity);

                    }

                    saveDepStockDataByShixian(gbDepartmentOrdersEntity);
                }

                gbDepartmentBillEntity.setGbDbStatus(4);
                gbDepartmentBillService.update(gbDepartmentBillEntity);

                NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryItemByGbDepBillId(gbDepartmentBillEntity.getGbDepartmentBillId());
                billEntity.setNxDbStatus(4);
                nxDepartmentBillService.update(billEntity);
            }

            // 告诉微信服务器收到信息了，不要在调用回调action了========这里很重要回复微信服务器信息用流发送一个xml即可
            response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

//    @RequestMapping(value = "/finishShixianBill", method = RequestMethod.POST)
//    @ResponseBody
//    public R finishShixianBill(@RequestBody GbDepartmentBillEntity billEntity) {
//        if (billEntity.getGbDbStatus() == -2) {
//            for (GbDepartmentOrdersEntity orders : billEntity.getGbDepartmentOrdersEntities()) {
//                BigDecimal gbDoWeight = new BigDecimal(orders.getGbDoWeight());
//                BigDecimal gbDoPrice = new BigDecimal(orders.getGbDoPrice());
//                BigDecimal gbDoSubtotal = new BigDecimal(orders.getGbDoSubtotal());
//
//                if (gbDoWeight.compareTo(BigDecimal.ZERO) == 1 && gbDoPrice.compareTo(BigDecimal.ZERO) == 1 &&
//                        gbDoSubtotal.compareTo(BigDecimal.ZERO) == 1) {
//                    saveDepStockDataByShixian(orders);
//                    orders.setGbDoStatus(getGbOrderStatusReceived()); //wancheng
//                    orders.setGbDoBuyStatus(getGbOrderBuyStatusHavePayFinish());
//                    gbDepartmentOrdersService.update(orders);
//
//                }
//            }
//            return R.ok();
//
//        } else {
//            return R.error(-1, "请刷新数据。");
//        }
//
//    }

    @RequestMapping(value = "/finishSharePurGoodsBatchIsAuto", method = RequestMethod.POST)
    @ResponseBody
    public R finishSharePurGoodsBatchIsAuto(Integer batchId, Boolean isAuto, Integer payType) {
        GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDPBService.queryObject(batchId);
        Integer gbDpbStatus = gbDistributerPurchaseBatchEntity.getGbDpbStatus();
        int orderCount = 0;

        if (gbDpbStatus == 2) {
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("batchId", batchId);
            List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDPGService.queryPurchaseGoodsByParams(mapB);
            for (GbDistributerPurchaseGoodsEntity purGoods : purchaseGoodsEntities) {
                purGoods.setGbDpgPayType(payType);
                purGoods.setGbDpgStatus(3);
                purGoods.setGbDpgPurchaseType(21);
                gbDPGService.update(purGoods);

                orderCount += purGoods.getGbDpgOrdersAmount();

                Integer gbDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", gbDistributerPurchaseGoodsId);
                List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);

                Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
                if (isAuto) {
                    GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
                    goodsEntity.setGbDgGbSupplierId(gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());
                    goodsEntity.setGbDgGoodsType(21);
                    goodsEntity.setGbDgNxDistributerId(gbDistributerPurchaseBatchEntity.getGbDpbNxDistributerId());
                    goodsEntity.setGbDgNxDistributerGoodsId(ordersEntities.get(0).getGbDoNxDistributerGoodsId());
                    gbDistributerGoodsService.update(goodsEntity);
                }

                for (GbDepartmentOrdersEntity orders : ordersEntities) {
                    BigDecimal gbDoWeight = new BigDecimal(orders.getGbDoWeight());
                    BigDecimal gbDoPrice = new BigDecimal(orders.getGbDoPrice());
                    BigDecimal gbDoSubtotal = new BigDecimal(orders.getGbDoSubtotal());

                    if (gbDoWeight.compareTo(BigDecimal.ZERO) == 1 && gbDoPrice.compareTo(BigDecimal.ZERO) == 1 &&
                            gbDoSubtotal.compareTo(BigDecimal.ZERO) == 1) {

                        saveDepStockDataByPurchase(orders, gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());


                        orders.setGbDoStatus(getGbOrderStatusReceived()); //wancheng
                        if (payType == 0) {
                            orders.setGbDoBuyStatus(getGbOrderBuyStatusHavePayFinish());
                        } else {
                            orders.setGbDoBuyStatus(getGbOrderBuyStatusUnPayFinish());
                        }

                        gbDepartmentOrdersService.update(orders);

                        //update nxGoodsPrice
                        System.out.println("nxgoididiidid" + orders.getGbDoNxGoodsId());
                        if (orders.getGbDoNxGoodsId() != null) {
                            Integer gbDpbSupplierId = gbDistributerPurchaseBatchEntity.getGbDpbSupplierId();

                            NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.queryObject(gbDpbSupplierId);
                            Integer nxDistributerSysMarketId = supplierEntity.getNxJrdhsSysMarketId();
                            Integer nxDistributerSysCityId = supplierEntity.getNxJrdhsSysCityId();

                            Map<String, Object> mapG = new HashMap<>();
                            mapG.put("nxGoodsId", orders.getGbDoNxGoodsId());
                            mapG.put("cityId", nxDistributerSysCityId);
                            mapG.put("marketId", nxDistributerSysMarketId);
                            mapG.put("date", formatWhatDay(0));
                            System.out.println("mapGGGg" + mapG);
                            NxGoodsPriceEntity goodsPriceEntity = nxGoodsPriceService.queryPriceGoodsByParams(mapG);
                            if (goodsPriceEntity == null) {
                                NxGoodsPriceEntity nxGoodsPriceEntity = new NxGoodsPriceEntity();
                                nxGoodsPriceEntity.setNxGpNxGoodsId(orders.getGbDoNxGoodsId());
                                nxGoodsPriceEntity.setNxGpDate(formatWhatDay(0));
                                nxGoodsPriceEntity.setNxGpSysMarketId(nxDistributerSysMarketId);
                                nxGoodsPriceEntity.setNxGpSysCityId(nxDistributerSysCityId);
                                nxGoodsPriceEntity.setNxGpLowestNxDistributerId(-1);
                                nxGoodsPriceEntity.setNxGpLowestJrdhSupplierId(gbDpbSupplierId);
                                nxGoodsPriceEntity.setNxGpLowestPrice(orders.getGbDoPrice());
                                nxGoodsPriceEntity.setNxGpLowestWeight(orders.getGbDoWeight());
                                nxGoodsPriceEntity.setNxGpHighestNxDistributerId(-1);
                                nxGoodsPriceEntity.setNxGpHighestJrdhSupplierId(gbDpbSupplierId);
                                nxGoodsPriceEntity.setNxGpHighestWeight(orders.getGbDoWeight());
                                nxGoodsPriceEntity.setNxGpHighestPrice(orders.getGbDoPrice());

                                nxGoodsPriceService.save(nxGoodsPriceEntity);

                            } else {
                                BigDecimal orderPirce = new BigDecimal(orders.getGbDoPrice());
                                BigDecimal lowestPrice = new BigDecimal(goodsPriceEntity.getNxGpLowestPrice());
                                BigDecimal highestPrice = new BigDecimal(goodsPriceEntity.getNxGpHighestPrice());
                                if (orderPirce.compareTo(lowestPrice) < 0) {
                                    goodsPriceEntity.setNxGpLowestPrice(orders.getGbDoPrice());
                                    goodsPriceEntity.setNxGpLowestWeight(orders.getGbDoWeight());
                                    goodsPriceEntity.setNxGpLowestNxDistributerId(-1);
                                    goodsPriceEntity.setNxGpLowestJrdhSupplierId(gbDpbSupplierId);
                                    nxGoodsPriceService.update(goodsPriceEntity);
                                } else if (orderPirce.compareTo(highestPrice) > 0) {
                                    goodsPriceEntity.setNxGpHighestPrice(orders.getGbDoPrice());
                                    goodsPriceEntity.setNxGpHighestWeight(orders.getGbDoWeight());
                                    goodsPriceEntity.setNxGpHighestNxDistributerId(-1);
                                    goodsPriceEntity.setNxGpHighestJrdhSupplierId(gbDpbSupplierId);
                                    nxGoodsPriceService.update(goodsPriceEntity);
                                }
                            }
                        }

                    }

                }
            }

            gbDistributerPurchaseBatchEntity.setGbDpbFinishFullTime(formatFullTime());
            if (payType == 0) {
                gbDistributerPurchaseBatchEntity.setGbDpbStatus(4); //如果是现金 status == 4 完成结账状态
            } else {
                gbDistributerPurchaseBatchEntity.setGbDpbStatus(3); //如果是记账，status == 2， 开票完成后，status == 3
                Integer gbDpbSupplierId = gbDistributerPurchaseBatchEntity.getGbDpbSupplierId();
                NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.queryObject(gbDpbSupplierId);
                Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
                System.out.println("dafdafas" + supplierEntity);
                if (nxJrdhsUserId == null) {
                    System.out.println("dafafads" + nxJrdhsUserId);
                    supplierEntity.setNxJrdhsUserId(gbDistributerPurchaseBatchEntity.getGbDpbSellUserId());
                    nxJrdhSupplierService.update(supplierEntity);
                }
            }
            gbDistributerPurchaseBatchEntity.setGbDpbPayType(payType);

            gbDPBService.update(gbDistributerPurchaseBatchEntity);


            Integer gbDpbDistributerId = gbDistributerPurchaseBatchEntity.getGbDpbDistributerId();
            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDpbDistributerId);

            GbDistributerPayListEntity payListEntity = new GbDistributerPayListEntity();
            payListEntity.setGbNdplPaySubtotal(Integer.valueOf(orderCount).toString());
            payListEntity.setGbNdplPayTime(formatFullTime());
            payListEntity.setGbNdplPayDate(formatWhatDay(0));
            payListEntity.setGbNdplPayMonth(formatWhatMonth(0));
            payListEntity.setGbNdplPayYear(formatWhatYear(0));

            payListEntity.setGbNdplStatus(0);
            payListEntity.setGbNdplType(0);
            payListEntity.setGbNdplRestPoints(Integer.valueOf(gbDistributerEntity.getGbDistributerBuyQuantity()));
            payListEntity.setGbNdplGbDisId(gbDistributerEntity.getGbDistributerId());
            payListEntity.setGbNdplNxSupplierId(gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());
            payListEntity.setGbNdplGbPbId(gbDistributerPurchaseBatchEntity.getGbDistributerPurchaseBatchId());
            payListService.save(payListEntity);

            BigDecimal decimal = new BigDecimal(gbDistributerEntity.getGbDistributerBuyQuantity());
            System.out.println("decimdall========" + decimal);
            BigDecimal decimal1 = new BigDecimal(orderCount);
            System.out.println("nxdkkddkdk00" + gbDistributerEntity.getGbDistributerBuyQuantity());
            BigDecimal add = decimal.subtract(decimal1).setScale(0, BigDecimal.ROUND_HALF_UP);
            gbDistributerEntity.setGbDistributerBuyQuantity(add.toString());
            System.out.println("nxdkkddkdk11" + gbDistributerEntity.getGbDistributerBuyQuantity());
            gbDistributerService.update(gbDistributerEntity);

            return R.ok();

        } else {
            return R.error(-1, "请刷新数据。");
        }

    }

    public R saveDepStockDataByShixian(@RequestBody GbDepartmentOrdersEntity order) {
        System.out.println("upddodididufidfuaisf");
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Integer gbDoDepDisGoodsId = order.getGbDoDepDisGoodsId();
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        Integer gbDoStatus = ordersEntity.getGbDoStatus();
        //判断没有被别人收货
        System.out.println("eeeeeeststttttttt" + gbDoStatus);
//        if (gbDoStatus.equals(getGbOrderStatusHasBill())) {
        //0,修改订单上次价格涨幅
        if (departmentDisGoodsEntity.getGbDdgOrderDate() != null && !departmentDisGoodsEntity.getGbDdgOrderDate().trim().isEmpty()) {
            if (order.getGbDoPrice() != null && !order.getGbDoPrice().trim().isEmpty()) {
                BigDecimal decimal = new BigDecimal(departmentDisGoodsEntity.getGbDdgOrderPrice());
                BigDecimal decimal1 = new BigDecimal(order.getGbDoPrice());
                BigDecimal subtract1 = decimal1.subtract(decimal);
                order.setGbDoPriceDifferent(subtract1.toString());
            } else {
                order.setGbDoPriceDifferent("0");
            }
        }

        GbDepartmentGoodsStockEntity stockEntity = new GbDepartmentGoodsStockEntity();
        stockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
        stockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
        stockEntity.setGbDgsGbPurGoodsId(order.getGbDoPurchaseGoodsId());
        stockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());

        Integer gbDoNxDepartmentOrderId = ordersEntity.getGbDoNxDepartmentOrderId();
        NxDepartmentOrderHistoryEntity nxDepartmentOrderHistoryEntity = historyService.queryObject(gbDoNxDepartmentOrderId);
        System.out.println("wiajiaiaooaoaoaoo" + nxDepartmentOrderHistoryEntity.getNxDoCostPriceLevel());

        if (nxDepartmentOrderHistoryEntity.getNxDoCostPriceLevel().equals("2")) {
            Integer nxDoDisGoodsId = nxDepartmentOrderHistoryEntity.getNxDoDisGoodsId();
            NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxDoDisGoodsId);
            BigDecimal bigDecimal = new BigDecimal(order.getGbDoWeight()).multiply(new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
            stockEntity.setGbDgsWeight(bigDecimal.toString());
            stockEntity.setGbDgsRestWeight(bigDecimal.toString());
            stockEntity.setGbDgsPrice(nxDistributerGoodsEntity.getNxDgWillPriceTwoAboutPrice());

        } else {
            stockEntity.setGbDgsWeight(order.getGbDoWeight());
            stockEntity.setGbDgsRestWeight(order.getGbDoWeight());
            stockEntity.setGbDgsPrice(order.getGbDoPrice());
        }
        stockEntity.setGbDgsSubtotal(order.getGbDoSubtotal());
        stockEntity.setGbDgsRestSubtotal(order.getGbDoSubtotal());
        stockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
        stockEntity.setGbDgsNxSupplierId(-1);

        Integer gbDoDisGoodsId = order.getGbDoDisGoodsId();
        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        stockEntity.setGbDgsGbDisGoodsFatherId(goodsEntity.getGbDgDfgGoodsFatherId());
        stockEntity.setGbDgsGbDisGoodsGrandId(goodsEntity.getGbDgDfgGoodsGrandId());
        stockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
        stockEntity.setGbDgsDate(formatWhatDay(0));
        stockEntity.setGbDgsTimeStamp(getTimeStamp());
        stockEntity.setGbDgsWeek(getWeek(0));
        stockEntity.setGbDgsMonth(formatWhatMonth(0));
        stockEntity.setGbDgsYear(formatWhatYear(0));
        stockEntity.setGbDgsFullTime(formatFullTime());
        stockEntity.setGbDgsLossWeight("0");
        stockEntity.setGbDgsLossSubtotal("0");
        stockEntity.setGbDgsReturnWeight("0");
        stockEntity.setGbDgsReturnSubtotal("0");
        stockEntity.setGbDgsProduceWeight("0");
        stockEntity.setGbDgsProduceSubtotal("0");
        stockEntity.setGbDgsWasteWeight("0");
        stockEntity.setGbDgsWasteSubtotal("0");
        String gbDdgSellingPrice = departmentDisGoodsEntity.getGbDdgSellingPrice();
        if (gbDdgSellingPrice != null && new BigDecimal(gbDdgSellingPrice).compareTo(new BigDecimal(0)) == 1) {
            stockEntity.setGbDgsAfterProfitSubtotal("0");
            stockEntity.setGbDgsBetweenPrice("0");
            stockEntity.setGbDgsCostRate("0");
            stockEntity.setGbDgsSellingSubtotal("0");
            stockEntity.setGbDgsProduceSellingSubtotal("0");
            stockEntity.setGbDgsProfitSubtotal("0");
            stockEntity.setGbDgsProfitWeight("0");
            stockEntity.setGbDgsSellingPrice(gbDdgSellingPrice);
        } else {
            stockEntity.setGbDgsSellingPrice("-1");
        }

        // showStandard
        if (departmentDisGoodsEntity.getGbDdgShowStandardId() != -1) {
            String gbDdgShowStandardScale = departmentDisGoodsEntity.getGbDdgShowStandardScale();
            BigDecimal divide = new BigDecimal(order.getGbDoWeight()).divide(new BigDecimal(gbDdgShowStandardScale), 1, BigDecimal.ROUND_HALF_UP);
            stockEntity.setGbDgsRestWeightShowStandard(divide.toString());
            stockEntity.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
        }

        //判断是否有保鲜时间参数
        if (order.getGbDoPurchaseGoodsId() != -1) {
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDPGService.queryObject(order.getGbDoPurchaseGoodsId());
            if (purchaseGoodsEntity.getGbDpgWarnFullTime() != null && !purchaseGoodsEntity.getGbDpgWarnFullTime().trim().isEmpty()
                    && purchaseGoodsEntity.getGbDpgWasteFullTime() != null && !purchaseGoodsEntity.getGbDpgWasteFullTime().trim().isEmpty()) {
                stockEntity.setGbDgsWarnFullTime(purchaseGoodsEntity.getGbDpgWarnFullTime());
                stockEntity.setGbDgsWasteFullTime(purchaseGoodsEntity.getGbDpgWasteFullTime());
                String gbDpgWarnFullTime = purchaseGoodsEntity.getGbDpgWarnFullTime();
                String gbDpgWasteFullTime = purchaseGoodsEntity.getGbDpgWasteFullTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                // 设置日期字符串
                // 解析日期字符串为Date对象
                Date dateWaste = null;
                Date dateWarn = null;
                try {
                    dateWaste = dateFormat.parse(gbDpgWasteFullTime);
                    dateWarn = dateFormat.parse(gbDpgWarnFullTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // 获取时间戳
                long timestampWaste = dateWaste.getTime();
                long timestampWarn = dateWarn.getTime();
                // 输出时间戳
                stockEntity.setGbDgsWasteTimeQuantumName(String.valueOf(timestampWaste));
                stockEntity.setGbDgsWarnTimeQuantumName(String.valueOf(timestampWarn));
            }
            //判断是否价格异常商品
            if (purchaseGoodsEntity.getGbDpgDisGoodsPriceId() != null) {
                GbDistributerGoodsPriceEntity goodsPriceEntity = goodsPriceService.queryObject(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                String doWeight = order.getGbDoWeight();
                Integer gbDgpPurWhat = goodsPriceEntity.getGbDgpPurWhat();
                String whatSubtotal = "";
                if (gbDgpPurWhat == 1) {
                    String gbDgpGoodsHighestPrice = goodsPriceEntity.getGbDgpGoodsHighestPrice();
                    String purPrice = goodsPriceEntity.getGbDgpPurPrice();
                    BigDecimal diffPrice = new BigDecimal(purPrice).subtract(new BigDecimal(gbDgpGoodsHighestPrice));
                    BigDecimal subtotal = diffPrice.multiply(new BigDecimal(doWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    whatSubtotal = subtotal.toString();
                }
                if (gbDgpPurWhat == -1) {
                    String lowestPrice = goodsPriceEntity.getGbDgpGoodsLowestPrice();
                    String purPrice = goodsPriceEntity.getGbDgpPurPrice();
                    BigDecimal diffPrice = new BigDecimal(purPrice).subtract(new BigDecimal(lowestPrice));
                    BigDecimal subtotal = diffPrice.multiply(new BigDecimal(doWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    whatSubtotal = subtotal.toString();
                }

                //价控最低价的成本
                //实际成本与最低成本的差价
                stockEntity.setGbDgsGbPriceSubtotal(whatSubtotal); // 相差了多少成本
                stockEntity.setGbDgsGbPriceGoodsId(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                stockEntity.setGbDgsGbPriceSubtotalScale(goodsPriceEntity.getGbDgpPurScale());
            }

        }


        stockEntity.setGbDgsStatus(0);
        stockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
        stockEntity.setGbDgsGbGoodsStockId(-1);
        stockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
        stockEntity.setGbDgsNxDistributerId(order.getGbDoNxDistributerId());
        stockEntity.setGbDgsReceiveUserId(order.getGbDoReceiveUserId());
        stockEntity.setGbDgsInventoryDate(formatWhatDay(0));
        stockEntity.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
        stockEntity.setGbDgsInventoryMonth(formatWhatMonth(0));
        stockEntity.setGbDgsInventoryYear(formatWhatYear(0));
        stockEntity.setGbDgsStars(5);
        gbDepartmentGoodsStockService.save(stockEntity);


        orderAddDepDisGoods(order, stockEntity, gbDoDepDisGoodsId);
        updateDepGoodsDailyBusiness(stockEntity);

        //2，修改订单状态
        order.setGbDoStatus(getGbOrderStatusReceived());
        gbDepartmentOrdersService.update(order);


        return R.ok();

    }

    public R saveDepStockDataByPurchase(@RequestBody GbDepartmentOrdersEntity order, Integer supplierId) {
        System.out.println("upddodididufidfuaisf");
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Integer gbDoDepDisGoodsId = order.getGbDoDepDisGoodsId();
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        Integer gbDoStatus = ordersEntity.getGbDoStatus();
        //判断没有被别人收货
        System.out.println("eeeeeeststttttttt" + gbDoStatus);
//        if (gbDoStatus.equals(getGbOrderStatusHasBill())) {
        //0,修改订单上次价格涨幅
        if (departmentDisGoodsEntity.getGbDdgOrderDate() != null && !departmentDisGoodsEntity.getGbDdgOrderDate().trim().isEmpty()) {
            if (order.getGbDoPrice() != null && !order.getGbDoPrice().trim().isEmpty()) {
                BigDecimal decimal = new BigDecimal(departmentDisGoodsEntity.getGbDdgOrderPrice());
                BigDecimal decimal1 = new BigDecimal(order.getGbDoPrice());
                BigDecimal subtract1 = decimal1.subtract(decimal);
                order.setGbDoPriceDifferent(subtract1.toString());
            } else {
                order.setGbDoPriceDifferent("0");
            }
        }


        GbDepartmentGoodsStockEntity stockEntity = new GbDepartmentGoodsStockEntity();
        stockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
        stockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
        stockEntity.setGbDgsGbPurGoodsId(order.getGbDoPurchaseGoodsId());
        stockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
        stockEntity.setGbDgsWeight(order.getGbDoWeight());
        stockEntity.setGbDgsPrice(order.getGbDoPrice());
        stockEntity.setGbDgsSubtotal(order.getGbDoSubtotal());
        stockEntity.setGbDgsRestWeight(order.getGbDoWeight());
        stockEntity.setGbDgsRestSubtotal(order.getGbDoSubtotal());
        stockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
        stockEntity.setGbDgsNxSupplierId(supplierId);

        Integer gbDoDisGoodsId = order.getGbDoDisGoodsId();
        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        stockEntity.setGbDgsGbDisGoodsFatherId(goodsEntity.getGbDgDfgGoodsFatherId());
        stockEntity.setGbDgsGbDisGoodsGrandId(goodsEntity.getGbDgDfgGoodsGrandId());
        stockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
        stockEntity.setGbDgsDate(formatWhatDay(0));
        stockEntity.setGbDgsTimeStamp(getTimeStamp());
        stockEntity.setGbDgsWeek(getWeek(0));
        stockEntity.setGbDgsMonth(formatWhatMonth(0));
        stockEntity.setGbDgsYear(formatWhatYear(0));
        stockEntity.setGbDgsFullTime(formatFullTime());
        stockEntity.setGbDgsLossWeight("0");
        stockEntity.setGbDgsLossSubtotal("0");
        stockEntity.setGbDgsReturnWeight("0");
        stockEntity.setGbDgsReturnSubtotal("0");
        stockEntity.setGbDgsProduceWeight("0");
        stockEntity.setGbDgsProduceSubtotal("0");
        stockEntity.setGbDgsWasteWeight("0");
        stockEntity.setGbDgsWasteSubtotal("0");
        String gbDdgSellingPrice = departmentDisGoodsEntity.getGbDdgSellingPrice();
        if (gbDdgSellingPrice != null && new BigDecimal(gbDdgSellingPrice).compareTo(new BigDecimal(0)) == 1) {
            stockEntity.setGbDgsAfterProfitSubtotal("0");
            stockEntity.setGbDgsBetweenPrice("0");
            stockEntity.setGbDgsCostRate("0");
            stockEntity.setGbDgsSellingSubtotal("0");
            stockEntity.setGbDgsProduceSellingSubtotal("0");
            stockEntity.setGbDgsProfitSubtotal("0");
            stockEntity.setGbDgsProfitWeight("0");
            stockEntity.setGbDgsSellingPrice(gbDdgSellingPrice);
        } else {
            stockEntity.setGbDgsSellingPrice("-1");
        }

        // showStandard
        if (departmentDisGoodsEntity.getGbDdgShowStandardId() != -1) {
            String gbDdgShowStandardScale = departmentDisGoodsEntity.getGbDdgShowStandardScale();
            BigDecimal divide = new BigDecimal(order.getGbDoWeight()).divide(new BigDecimal(gbDdgShowStandardScale), 1, BigDecimal.ROUND_HALF_UP);
            stockEntity.setGbDgsRestWeightShowStandard(divide.toString());
            stockEntity.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
        }

        //判断是否有保鲜时间参数
        if (order.getGbDoPurchaseGoodsId() != -1) {
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDPGService.queryObject(order.getGbDoPurchaseGoodsId());
            System.out.println("gbDpgWasteFullTimegbDpgWasteFullTime11" + purchaseGoodsEntity.getGbDpgWarnFullTime());
            System.out.println("gbDpgWarnFullTimegbDpgWarnFullTime1111" + purchaseGoodsEntity.getGbDpgWasteFullTime());
            String warnFullTime = purchaseGoodsEntity.getGbDpgWarnFullTime();
            String gbDpgWasteFullTime1 = purchaseGoodsEntity.getGbDpgWasteFullTime();
            if ( warnFullTime!= null && !warnFullTime.trim().isEmpty() && gbDpgWasteFullTime1 != null && !gbDpgWasteFullTime1.trim().isEmpty()) {
                stockEntity.setGbDgsWarnFullTime(purchaseGoodsEntity.getGbDpgWarnFullTime());
                stockEntity.setGbDgsWasteFullTime(purchaseGoodsEntity.getGbDpgWasteFullTime());
                String gbDpgWarnFullTime = purchaseGoodsEntity.getGbDpgWarnFullTime();
                String gbDpgWasteFullTime = purchaseGoodsEntity.getGbDpgWasteFullTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                // 设置日期字符串
                // 解析日期字符串为Date对象
                Date dateWaste = null;
                Date dateWarn = null;

                try {
                    dateWaste = dateFormat.parse(gbDpgWasteFullTime);
                    dateWarn = dateFormat.parse(gbDpgWarnFullTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // 获取时间戳
                long timestampWaste = dateWaste.getTime();
                long timestampWarn = dateWarn.getTime();
                // 输出时间戳

                System.out.println("gbDpgWasteFullTimegbDpgWasteFullTime22222" + purchaseGoodsEntity.getGbDpgWarnFullTime());
                System.out.println("gbDpgWarnFullTimegbDpgWarnFullTime22222" + purchaseGoodsEntity.getGbDpgWasteFullTime());

                stockEntity.setGbDgsWasteTimeQuantumName(String.valueOf(timestampWaste));
                stockEntity.setGbDgsWarnTimeQuantumName(String.valueOf(timestampWarn));
            }
            //判断是否价格异常商品
            if (purchaseGoodsEntity.getGbDpgDisGoodsPriceId() != null) {
                GbDistributerGoodsPriceEntity goodsPriceEntity = goodsPriceService.queryObject(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                String doWeight = order.getGbDoWeight();
                Integer gbDgpPurWhat = goodsPriceEntity.getGbDgpPurWhat();
                String whatSubtotal = "";
                if (gbDgpPurWhat == 1) {
                    String gbDgpGoodsHighestPrice = goodsPriceEntity.getGbDgpGoodsHighestPrice();
                    String purPrice = goodsPriceEntity.getGbDgpPurPrice();
                    BigDecimal diffPrice = new BigDecimal(purPrice).subtract(new BigDecimal(gbDgpGoodsHighestPrice));
                    BigDecimal subtotal = diffPrice.multiply(new BigDecimal(doWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    whatSubtotal = subtotal.toString();
                }
                if (gbDgpPurWhat == -1) {
                    String lowestPrice = goodsPriceEntity.getGbDgpGoodsLowestPrice();
                    String purPrice = goodsPriceEntity.getGbDgpPurPrice();
                    BigDecimal diffPrice = new BigDecimal(purPrice).subtract(new BigDecimal(lowestPrice));
                    BigDecimal subtotal = diffPrice.multiply(new BigDecimal(doWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    whatSubtotal = subtotal.toString();
                }

                //价控最低价的成本
                //实际成本与最低成本的差价
                stockEntity.setGbDgsGbPriceSubtotal(whatSubtotal); // 相差了多少成本
                stockEntity.setGbDgsGbPriceGoodsId(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                stockEntity.setGbDgsGbPriceSubtotalScale(goodsPriceEntity.getGbDgpPurScale());
            }

        }


        stockEntity.setGbDgsStatus(0);
        stockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
        stockEntity.setGbDgsGbGoodsStockId(-1);
        stockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
        stockEntity.setGbDgsNxDistributerId(order.getGbDoNxDistributerId());
        stockEntity.setGbDgsReceiveUserId(order.getGbDoReceiveUserId());
        stockEntity.setGbDgsInventoryDate(formatWhatDay(0));
        stockEntity.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
        stockEntity.setGbDgsInventoryMonth(formatWhatMonth(0));
        stockEntity.setGbDgsInventoryYear(formatWhatYear(0));
        stockEntity.setGbDgsStars(5);
        gbDepartmentGoodsStockService.save(stockEntity);


        orderAddDepDisGoods(order, stockEntity, gbDoDepDisGoodsId);
        updateDepGoodsDailyBusiness(stockEntity);

        //2，修改订单状态
        order.setGbDoStatus(getGbOrderStatusReceived());
        gbDepartmentOrdersService.update(order);

        //3，修改送货单收货单子数量
        if (order.getGbDoPurchaseGoodsId() != -1) {
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDPGService.queryObject(order.getGbDoPurchaseGoodsId());
            BigDecimal gbPgOrderAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersAmount());
            BigDecimal gbDbFinishAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersFinishAmount());
            if (gbDbFinishAmount.add(new BigDecimal(1)).compareTo(gbPgOrderAmount) == 0) {
                purchaseGoodsEntity.setGbDpgOrdersFinishAmount(purchaseGoodsEntity.getGbDpgOrdersAmount());
                purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusReceive());
            } else {
                BigDecimal add = gbDbFinishAmount.add(new BigDecimal(1));
                purchaseGoodsEntity.setGbDpgOrdersFinishAmount(add.intValue());
            }
            gbDPGService.update(purchaseGoodsEntity);
        }

        //3，修改送货单收货单子数量
//            Integer gbDoBillId = order.getGbDoBillId();
//            GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryObject(gbDoBillId);
//            BigDecimal gbDbOrderAmount = new BigDecimal(billEntity.getGbDbOrderAmount());
//            if (gbDbOrderAmount.compareTo(new BigDecimal("1")) == 1) {
//                BigDecimal subtract = gbDbOrderAmount.subtract(new BigDecimal("1"));
//                billEntity.setGbDbOrderAmount(subtract.intValue());
//                gbDepartmentBillService.update(billEntity);
//            } else {
//                billEntity.setGbDbOrderAmount(0);
//                billEntity.setGbDbStatus(0);
//                gbDepartmentBillService.update(billEntity);
//            }
        return R.ok();
//        } else {
//            return R.error(-1, "已经完成收货");
//        }

    }

    private void orderAddDepDisGoods(GbDepartmentOrdersEntity ordersEntity, GbDepartmentGoodsStockEntity stockEntity, Integer depDisGoodsId) {

        BigDecimal stockSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
        BigDecimal stockWeight = new BigDecimal(stockEntity.getGbDgsWeight());
        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal weight = new BigDecimal(0);
        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(stockSubtotal);
        weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(stockWeight);
        //updateOrder
        depDisGoodsEntity.setGbDdgOrderDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgOrderPrice(ordersEntity.getGbDoPrice());
        depDisGoodsEntity.setGbDdgOrderQuantity(ordersEntity.getGbDoQuantity());
        depDisGoodsEntity.setGbDdgOrderRemark(ordersEntity.getGbDoRemark());
        depDisGoodsEntity.setGbDdgOrderStandard(ordersEntity.getGbDoStandard());
        depDisGoodsEntity.setGbDdgOrderWeight(ordersEntity.getGbDoWeight());
        depDisGoodsEntity.setGbDdgPrintStandard(ordersEntity.getGbDoPrintStandard());

        if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(new BigDecimal(0)) == 1) {
            BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
            BigDecimal standardWeight = weight.divide(showScale, 1, BigDecimal.ROUND_HALF_UP);
            depDisGoodsEntity.setGbDdgShowStandardWeight(standardWeight.toString());
        }

        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));
        gbDepartmentDisGoodsService.update(depDisGoodsEntity);

    }

    private void updateDepGoodsDailyBusiness(GbDepartmentGoodsStockEntity stock) {

        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", stock.getGbDgsGbDepDisGoodsId());
        map.put("date", formatWhatDay(0));
        GbDepartmentGoodsDailyEntity depGoodsDailyItem = gbDepGoodsDailyService.queryDepGoodsDailyItem(map);
        if (depGoodsDailyItem != null) {
            BigDecimal weight = new BigDecimal(depGoodsDailyItem.getGbDgdWeight());
            BigDecimal total = new BigDecimal(depGoodsDailyItem.getGbDgdSubtotal());
            BigDecimal restWeight = new BigDecimal(depGoodsDailyItem.getGbDgdRestWeight());
            BigDecimal totalWeight = new BigDecimal(stock.getGbDgsWeight()).add(weight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalSubtotal = new BigDecimal(stock.getGbDgsSubtotal()).add(total).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalRestWeight = new BigDecimal(stock.getGbDgsWeight()).add(restWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            depGoodsDailyItem.setGbDgdWeight(totalWeight.toString());
            depGoodsDailyItem.setGbDgdSubtotal(totalSubtotal.toString());
            depGoodsDailyItem.setGbDgdRestWeight(totalRestWeight.toString());
            depGoodsDailyItem.setGbDgdSellClearHour("-1");
            depGoodsDailyItem.setGbDgdSellClearMinute("-1");
            depGoodsDailyItem.setGbDgdStatus(0);
            gbDepGoodsDailyService.update(depGoodsDailyItem);

        } else {
            GbDepartmentGoodsDailyEntity dailyEntity = new GbDepartmentGoodsDailyEntity();
            dailyEntity.setGbDgdGbDistributerId(stock.getGbDgsGbDistributerId());
            dailyEntity.setGbDgdGbDepartmentId(stock.getGbDgsGbDepartmentId());
            dailyEntity.setGbDgdGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            dailyEntity.setGbDgdGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
            dailyEntity.setGbDgdGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            dailyEntity.setGbDgdGbDisGoodsGrandId(stock.getGbDgsGbDisGoodsGrandId());
            dailyEntity.setGbDgdGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
            dailyEntity.setGbDgdDate(formatWhatDay(0));
            dailyEntity.setGbDgdWeek(getWeekOfYear(0).toString());
            dailyEntity.setGbDgdMonth(formatWhatMonth(0));
            dailyEntity.setGbDgdYear(formatWhatYear(0));
            dailyEntity.setGbDgdDay(getWeek(0));
            dailyEntity.setGbDgdWeight(stock.getGbDgsWeight());
            dailyEntity.setGbDgdRestWeight(stock.getGbDgsWeight());
            dailyEntity.setGbDgdSubtotal(stock.getGbDgsSubtotal());
            dailyEntity.setGbDgdProduceWeight("0");
            dailyEntity.setGbDgdProduceSubtotal("0");
            dailyEntity.setGbDgdLossWeight("0");
            dailyEntity.setGbDgdLossSubtotal("0");
            dailyEntity.setGbDgdReturnWeight("0");
            dailyEntity.setGbDgdReturnSubtotal("0");
            dailyEntity.setGbDgdWasteWeight("0");
            dailyEntity.setGbDgdWasteSubtotal("0");
            dailyEntity.setGbDgdSalesSubtotal("0");
            dailyEntity.setGbDgdProfitSubtotal("0");
            dailyEntity.setGbDgdAfterProfitSubtotal("0");
            dailyEntity.setGbDgdSellClearHour("-1");
            dailyEntity.setGbDgdSellClearMinute("-1");
            dailyEntity.setGbDgdLastWeight("0");
            dailyEntity.setGbDgdLastProduceWeight("0");
            Integer gbDgdGbDisGoodsId = dailyEntity.getGbDgdGbDisGoodsId();
            GbDistributerGoodsEntity distributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDgdGbDisGoodsId);
            if (distributerGoodsEntity.getGbDgControlFresh() == 1) {
                dailyEntity.setGbDgdFreshRate("100");
            } else {
                dailyEntity.setGbDgdFreshRate("0");
            }
            dailyEntity.setGbDgdFullTime(formatFullTime());
            dailyEntity.setGbDgdStatus(0);
            gbDepGoodsDailyService.save(dailyEntity);
        }
    }


    private GbDistributerPurchaseGoodsEntity checkPurGoodsPrice(GbDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        System.out.println("checkkckGoododopriiddcheckPurGoodsPrice" + purchaseGoodsEntity.getGbDpgDisGoodsId());
        Integer gbDpgDisGoodsId = purchaseGoodsEntity.getGbDpgDisGoodsId();
        BigDecimal buyPrice = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
        Integer gbDpgDisGoodsPriceId = purchaseGoodsEntity.getGbDpgDisGoodsPriceId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);

        BigDecimal weight = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
        BigDecimal goodsHighest = new BigDecimal(gbDistributerGoodsEntity.getGbDgGoodsHighestPrice());
        BigDecimal goodsLowest = new BigDecimal(gbDistributerGoodsEntity.getGbDgGoodsLowestPrice());
        String priceTotal = buyPrice.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString();

        if (buyPrice.compareTo(goodsHighest) == 1 && purchaseGoodsEntity.getGbDpgBuyQuantity() != null && !purchaseGoodsEntity.getGbDpgBuyQuantity().trim().isEmpty()) { //高于最高价

            BigDecimal higherWhatPrice = buyPrice.subtract(goodsHighest);

            BigDecimal highertotal = higherWhatPrice.multiply(weight).setScale(2, BigDecimal.ROUND_HALF_UP); //高出的单价部分 * 重量
            BigDecimal multiply = higherWhatPrice.divide(goodsHighest, 4, BigDecimal.ROUND_HALF_DOWN); // 高出的单价部门 除以 正常价格
            BigDecimal highestTotal = goodsHighest.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP);

            if (gbDpgDisGoodsPriceId != null) {
                GbDistributerGoodsPriceEntity goodsPriceEntity = gbDistributerGoodsPriceService.queryObject(gbDpgDisGoodsPriceId);
                goodsPriceEntity.setGbDgpPurWhat(1);
                goodsPriceEntity.setGbDgpPurPrice(purchaseGoodsEntity.getGbDpgBuyPrice());
                goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
                goodsPriceEntity.setGbDgpPurScale(multiply.toString());
                goodsPriceEntity.setGbDgpPurWhatTotal(highertotal.toString());
                goodsPriceEntity.setGbDgpGoodsHighestTotal(highestTotal.toString());
                goodsPriceEntity.setGbDgpPurTotal(priceTotal);
                goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
                goodsPriceEntity.setGbDgpGoodsLowestTotal("0");
                goodsPriceEntity.setGbDgpPurDepartmentId(purchaseGoodsEntity.getGbDpgPurchaseDepartmentId());
                goodsPriceEntity.setGbDgpPurNxDistributerId(purchaseGoodsEntity.getGbDpgPurchaseNxDistributerId());
                goodsPriceEntity.setGbDgpGoodsPrice(gbDistributerGoodsEntity.getGbDgGoodsPrice());
                goodsPriceEntity.setGbDgpGoodsLowestPrice(gbDistributerGoodsEntity.getGbDgGoodsLowestPrice());
                goodsPriceEntity.setGbDgpGoodsHighestPrice(gbDistributerGoodsEntity.getGbDgGoodsHighestPrice());
                gbDistributerGoodsPriceService.update(goodsPriceEntity);
            } else {
                GbDistributerGoodsPriceEntity goodsPriceEntity = new GbDistributerGoodsPriceEntity();
                goodsPriceEntity.setGbDgpGoodsPrice(gbDistributerGoodsEntity.getGbDgGoodsPrice());
                goodsPriceEntity.setGbDgpGoodsLowestPrice(gbDistributerGoodsEntity.getGbDgGoodsLowestPrice());
                goodsPriceEntity.setGbDgpGoodsHighestPrice(gbDistributerGoodsEntity.getGbDgGoodsHighestPrice());
                goodsPriceEntity.setGbDgpDistributerGoodsId(gbDistributerGoodsEntity.getGbDistributerGoodsId());
                goodsPriceEntity.setGbDgpDistributerId(gbDistributerGoodsEntity.getGbDgDistributerId());
                goodsPriceEntity.setGbDgpDfgGoodsFatherId(gbDistributerGoodsEntity.getGbDgDfgGoodsFatherId());
                goodsPriceEntity.setGbDgpPurDate(formatWhatDay(0));
                goodsPriceEntity.setGbDgpPurGoodsId(purchaseGoodsEntity.getGbDistributerPurchaseGoodsId());
                goodsPriceEntity.setGbDgpPurUserId(purchaseGoodsEntity.getGbDpgPurUserId());
                goodsPriceEntity.setGbDgpPurWhat(1);
                goodsPriceEntity.setGbDgpPurPrice(purchaseGoodsEntity.getGbDpgBuyPrice());
                goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
                goodsPriceEntity.setGbDgpPurScale(multiply.toString());
                goodsPriceEntity.setGbDgpPurWhatTotal(highertotal.toString());
                goodsPriceEntity.setGbDgpGoodsHighestTotal(highestTotal.toString());
                goodsPriceEntity.setGbDgpGoodsLowestTotal("0");
                goodsPriceEntity.setGbDgpPurTotal(priceTotal);
                goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
                goodsPriceEntity.setGbDgpPurDepartmentId(purchaseGoodsEntity.getGbDpgPurchaseDepartmentId());
                goodsPriceEntity.setGbDgpStatus(0);
                goodsPriceEntity.setGbDgpWeek(getWeekOfYear(0).toString());
                goodsPriceEntity.setGbDgpMonth(formatWhatMonth(0));
                goodsPriceEntity.setGbDgpYear(formatWhatYear(0));
                goodsPriceEntity.setGbDgpPurNxDistributerId(gbDistributerGoodsEntity.getGbDgNxDistributerId());
                System.out.println("fdhfehfrekfe" + gbDistributerGoodsEntity.getGbDgNxDistributerId());
                gbDistributerGoodsPriceService.save(goodsPriceEntity);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(goodsPriceEntity.getGbDistributerGoodsPriceId());
            }
            gbDPGService.update(purchaseGoodsEntity);
        }

        if (buyPrice.compareTo(goodsLowest) == -1 && purchaseGoodsEntity.getGbDpgBuyQuantity() != null && !purchaseGoodsEntity.getGbDpgBuyQuantity().trim().isEmpty()) { //低于最低价
            BigDecimal lowerWhatPrice = goodsLowest.subtract(buyPrice);
            BigDecimal lowerTotal = lowerWhatPrice.multiply(weight).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal multiply = lowerWhatPrice.divide(goodsLowest, 4, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal lowestTotal = goodsLowest.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP);

            if (gbDpgDisGoodsPriceId != null) {
                GbDistributerGoodsPriceEntity goodsPriceEntity = gbDistributerGoodsPriceService.queryObject(gbDpgDisGoodsPriceId);
                goodsPriceEntity.setGbDgpPurWhat(-1);
                goodsPriceEntity.setGbDgpPurPrice(purchaseGoodsEntity.getGbDpgBuyPrice());
                goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
                goodsPriceEntity.setGbDgpPurScale(multiply.toString());
                goodsPriceEntity.setGbDgpPurWhatTotal(lowerTotal.toString());
                goodsPriceEntity.setGbDgpGoodsLowestTotal(lowestTotal.toString());
                goodsPriceEntity.setGbDgpGoodsHighestTotal("0");
                goodsPriceEntity.setGbDgpPurTotal(priceTotal);
                goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
                goodsPriceEntity.setGbDgpPurDepartmentId(purchaseGoodsEntity.getGbDpgPurchaseDepartmentId());
                goodsPriceEntity.setGbDgpPurNxDistributerId(purchaseGoodsEntity.getGbDpgPurchaseNxDistributerId());
                goodsPriceEntity.setGbDgpGoodsPrice(gbDistributerGoodsEntity.getGbDgGoodsPrice());
                goodsPriceEntity.setGbDgpGoodsLowestPrice(gbDistributerGoodsEntity.getGbDgGoodsLowestPrice());
                goodsPriceEntity.setGbDgpGoodsHighestPrice(gbDistributerGoodsEntity.getGbDgGoodsHighestPrice());

                gbDistributerGoodsPriceService.update(goodsPriceEntity);
            } else {
                GbDistributerGoodsPriceEntity goodsPriceEntity = new GbDistributerGoodsPriceEntity();
                goodsPriceEntity.setGbDgpGoodsPrice(gbDistributerGoodsEntity.getGbDgGoodsPrice());
                goodsPriceEntity.setGbDgpGoodsLowestPrice(gbDistributerGoodsEntity.getGbDgGoodsLowestPrice());
                goodsPriceEntity.setGbDgpGoodsHighestPrice(gbDistributerGoodsEntity.getGbDgGoodsHighestPrice());
                goodsPriceEntity.setGbDgpDistributerGoodsId(gbDistributerGoodsEntity.getGbDistributerGoodsId());
                goodsPriceEntity.setGbDgpDistributerId(gbDistributerGoodsEntity.getGbDgDistributerId());
                goodsPriceEntity.setGbDgpDfgGoodsFatherId(gbDistributerGoodsEntity.getGbDgDfgGoodsFatherId());
                goodsPriceEntity.setGbDgpPurDate(formatWhatDay(0));
                goodsPriceEntity.setGbDgpPurGoodsId(purchaseGoodsEntity.getGbDistributerPurchaseGoodsId());
                goodsPriceEntity.setGbDgpPurUserId(purchaseGoodsEntity.getGbDpgPurUserId());
                goodsPriceEntity.setGbDgpPurWhat(-1);
                goodsPriceEntity.setGbDgpPurPrice(purchaseGoodsEntity.getGbDpgBuyPrice());
                goodsPriceEntity.setGbDgpPurScale(multiply.toString());
                goodsPriceEntity.setGbDgpPurWhatTotal(lowerTotal.toString());
                goodsPriceEntity.setGbDgpGoodsLowestTotal(lowestTotal.toString());
                goodsPriceEntity.setGbDgpPurTotal(priceTotal);
                goodsPriceEntity.setGbDgpPurWeight(purchaseGoodsEntity.getGbDpgBuyQuantity());
                goodsPriceEntity.setGbDgpGoodsHighestTotal("0");
                goodsPriceEntity.setGbDgpPurDepartmentId(purchaseGoodsEntity.getGbDpgPurchaseDepartmentId());
                goodsPriceEntity.setGbDgpStatus(0);
                goodsPriceEntity.setGbDgpWeek(getWeekOfYear(0).toString());
                goodsPriceEntity.setGbDgpMonth(formatWhatMonth(0));
                goodsPriceEntity.setGbDgpYear(formatWhatYear(0));
                goodsPriceEntity.setGbDgpPurNxDistributerId(gbDistributerGoodsEntity.getGbDgNxDistributerId());
                gbDistributerGoodsPriceService.save(goodsPriceEntity);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(goodsPriceEntity.getGbDistributerGoodsPriceId());
            }

            gbDPGService.update(purchaseGoodsEntity);
        }

        if (buyPrice.compareTo(goodsHighest) == -1 && buyPrice.compareTo(goodsLowest) == 1) {

            if (gbDpgDisGoodsPriceId != null) {

                gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);

                purchaseGoodsEntity.setGbDpgBuyPriceReason(null);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(null);
                gbDPGService.update(purchaseGoodsEntity);

            }
        }
        if (buyPrice.compareTo(goodsHighest) == 0 || buyPrice.compareTo(goodsLowest) == 0) {
            if (gbDpgDisGoodsPriceId != null) {
                gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);
                purchaseGoodsEntity.setGbDpgBuyPriceReason(null);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(null);
                gbDPGService.update(purchaseGoodsEntity);
            }
        }

        return purchaseGoodsEntity;

    }


//    @RequestMapping(value = "/getGbDisPurchaseGoodsBatch/{batchId}")
//    @ResponseBody
//    public R getGbDisPurchaseGoodsBatch(@PathVariable Integer batchId) {
//
//        GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDPBService.queryBatchWithOrders(batchId);
//
//        if (gbDistributerPurchaseBatchEntity != null) {
//            return R.ok().put("data", gbDistributerPurchaseBatchEntity);
//        } else {
//            return R.error(-1, "没有订单");
//        }
//    }


//    @RequestMapping(value = "/saveGbDisPurBatchBuyUserId", method = RequestMethod.POST)
//    @ResponseBody
//    public R saveGbDisPurBatchBuyUserId(Integer batchId, Integer buyUserId) {
//        GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDPBService.queryObject(batchId);
//        gbDistributerPurchaseBatchEntity.setGbDpbBuyUserId(buyUserId);
//        gbDPBService.update(gbDistributerPurchaseBatchEntity);
//        return R.ok();
//    }


    @RequestMapping(value = "/sellerGetPurchaseBatch", method = RequestMethod.POST)
    @ResponseBody
    public R sellerGetPurchaseBatch(Integer userId, Integer disId) {
        System.out.println("selleieid" + userId);
        System.out.println("dididididididi" + disId);
        Map<String, Object> map = new HashMap<>();
        map.put("sellerId", userId);
        map.put("month", formatWhatMonth(0));
        System.out.println("paumondthhthth" + map);
        List<GbDistributerPurchaseBatchEntity> purchaseBatch = gbDPBService.queryDisPurchaseBatch(map);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("month", formatWhatMonth(0));
        map1.put("arr", purchaseBatch);

        //lastMonth
        Map<String, Object> map2 = new HashMap<>();
        map2.put("sellerId", userId);
        map2.put("month", getLastMonth());
        List<GbDistributerPurchaseBatchEntity> purchaseBatch1 = gbDPBService.queryDisPurchaseBatch(map2);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("month", getLastMonth());
        map3.put("arr", purchaseBatch1);

        //lastTwoMonth
        Map<String, Object> map4 = new HashMap<>();
        map4.put("sellerId", userId);
        map4.put("month", getLastTwoMonth());
        List<GbDistributerPurchaseBatchEntity> purchaseBatch2 = gbDPBService.queryDisPurchaseBatch(map4);

        Map<String, Object> map5 = new HashMap<>();
        map5.put("month", getLastTwoMonth());
        map5.put("arr", purchaseBatch2);

        Map<String, Object> map6 = new HashMap<>();
        map6.put("sellerId", userId);
        map6.put("status", 4);
        map6.put("dayuStatus", 1);
        Integer integer = gbDPBService.queryDisPurchaseBatchCount(map6);
        BigDecimal subtotal = new BigDecimal(0);
        if (integer > 0) {
            Double aDouble = gbDPBService.querySupplierUnSettleSubtotal(map6);
            subtotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(map1);
        result.add(map3);
        result.add(map5);
        Map<String, Object> map111 = new HashMap<>();
        map111.put("month", result);
        map111.put("unSettle", subtotal);
        map111.put("gbDis", gbDistributerService.queryObject(disId));
        return R.ok().put("data", map111);
    }

    @RequestMapping(value = "/supplierGetPurchaseBatch/{supplierId}")
    @ResponseBody
    public R supplierGetPurchaseBatch(@PathVariable Integer supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", supplierId);
        map.put("month", formatWhatMonth(0));
        List<GbDistributerPurchaseBatchEntity> purchaseBatch = gbDPBService.queryDisPurchaseBatch(map);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("month", formatWhatMonth(0));
        map1.put("arr", purchaseBatch);

        //lastMonth
        Map<String, Object> map2 = new HashMap<>();
        map2.put("supplierId", supplierId);
        map2.put("month", getLastMonth());
        List<GbDistributerPurchaseBatchEntity> purchaseBatch1 = gbDPBService.queryDisPurchaseBatch(map2);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("month", getLastMonth());
        map3.put("arr", purchaseBatch1);

        //lastTwoMonth
        Map<String, Object> map4 = new HashMap<>();
        map4.put("supplierId", supplierId);
        map4.put("month", getLastTwoMonth());
        List<GbDistributerPurchaseBatchEntity> purchaseBatch2 = gbDPBService.queryDisPurchaseBatch(map4);

        Map<String, Object> map5 = new HashMap<>();
        map5.put("month", getLastTwoMonth());
        map5.put("arr", purchaseBatch2);

        Map<String, Object> map6 = new HashMap<>();
        map6.put("supplierId", supplierId);
        map6.put("status", 4);
        map6.put("dayuStatus", 1);
        Integer integer = gbDPBService.queryDisPurchaseBatchCount(map6);
        BigDecimal subtotal = new BigDecimal(0);
        if (integer > 0) {
            Double aDouble = gbDPBService.querySupplierUnSettleSubtotal(map6);
            subtotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(map1);
        result.add(map3);
        result.add(map5);
        Map<String, Object> map111 = new HashMap<>();
        map111.put("month", result);
        map111.put("unSettle", subtotal);
        return R.ok().put("data", map111);
    }

    @RequestMapping(value = "/sellUserOpenDisBatchGb/{batchId}")
    @ResponseBody
    public R sellUserOpenDisBatchGb(@PathVariable Integer batchId) {
        GbDistributerPurchaseBatchEntity batch = gbDPBService.queryObject(batchId);
        batch.setGbDpbStatus(0);
        gbDPBService.update(batch);
        Integer gbDistributerPurchaseBatchId = batch.getGbDistributerPurchaseBatchId();
        GbDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = gbDPBService.queryBatchWithOrders(gbDistributerPurchaseBatchId);
        return R.ok().put("data", nxDistributerPurchaseBatchEntity);
    }

    @RequestMapping(value = "/sellUserReadDisBatchGb", method = RequestMethod.POST)
    @ResponseBody
    public R sellUserReadDisBatchGb(@RequestBody GbDistributerPurchaseBatchEntity batch) {

        System.out.println("sellUserReadDisBatchGbsellUserReadDisBatchGbsellUserReadDisBatchGb");
        batch.setGbDpbStatus(getGbDisPurchaseBatchHaveRead());
        gbDPBService.update(batch);

        Integer batchId = batch.getGbDistributerPurchaseBatchId();
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDPGService.queryPurchaseGoodsByBatchId(batchId);
        if (purchaseGoodsEntities.size() > 0) {
            for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setGbDpgPurchaseNxSupplierId(batch.getGbDpbSupplierId());
                gbDPGService.update(purchaseGoodsEntity);
            }
        }

        GbDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = gbDPBService.queryBatchWithOrders(batchId);
        return R.ok().put("data", nxDistributerPurchaseBatchEntity);

    }

    @RequestMapping(value = "/sellerReceiveReturnBill")
    @ResponseBody
    public R sellerReceiveReturnBill(@RequestBody GbDistributerPurchaseBatchEntity batchEntity) {

        BigDecimal tuihuo = new BigDecimal(0);
        List<GbDistributerPurchaseGoodsEntity> nxDPBEntities = batchEntity.getGbDPGEntities();
        for (GbDistributerPurchaseGoodsEntity purGoods : nxDPBEntities) {
            String gbDpgBuySubtotal = purGoods.getGbDpgBuySubtotal();
            tuihuo = tuihuo.add(new BigDecimal(gbDpgBuySubtotal));
            purGoods.setGbDpgStatus(3);
            gbDPGService.update(purGoods);

            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", purGoods.getGbDistributerPurchaseGoodsId());
            List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
            if (gbDepartmentOrdersEntities.size() > 0) {
                for (GbDepartmentOrdersEntity ordersEntity : gbDepartmentOrdersEntities) {
                    ordersEntity.setGbDoStatus(4);
                    ordersEntity.setGbDoBuyStatus(6);
                    gbDepartmentOrdersService.update(ordersEntity);
                    Integer gbDoDgsrReturnId = ordersEntity.getGbDoDgsrReturnId();
                    GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentStockReduceService.queryObject(gbDoDgsrReturnId);
                    reduceEntity.setGbDgsrStatus(0);
                    gbDepartmentStockReduceService.update(reduceEntity);
                }
            }
        }

        batchEntity.setGbDpbSubtotal(tuihuo.toString());
        batchEntity.setGbDpbStatus(3);
        batchEntity.setGbDpbSellerReplyFullTime(formatFullTime());
        batchEntity.setGbDpbFinishFullTime(formatFullTime());
        gbDPBService.update(batchEntity);
        return R.ok();
    }


    @RequestMapping(value = "/sellerFinishPurchaseGoodsBatchGb")
    @ResponseBody
    public R sellerFinishPurchaseGoodsBatchGb(@RequestBody GbDistributerPurchaseBatchEntity batchEntity) {

        List<GbDistributerPurchaseGoodsEntity> nxDPBEntities = batchEntity.getGbDPGEntities();
        for (GbDistributerPurchaseGoodsEntity purGoods : nxDPBEntities) {
            purGoods.setGbDpgStatus(2);
            gbDPGService.update(purGoods);
        }
        batchEntity.setGbDpbStatus(2);
        batchEntity.setGbDpbSellerReplyFullTime(formatFullTime());
        gbDPBService.update(batchEntity);

//
//        if (batchEntity.getGbDpbPayType() == 0) {
//
//
//        } else {
//            finishBatchBySeller(batchEntity);
//        }
        return R.ok();
    }


    private void finishBatchBySeller(GbDistributerPurchaseBatchEntity batch) {
        Integer gbDistributerPurchaseBatchId = batch.getGbDistributerPurchaseBatchId();
        GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDPBService.queryObject(gbDistributerPurchaseBatchId);
//        Integer gbDpbStatus = gbDistributerPurchaseBatchEntity.getGbDpbStatus();
        int orderCount = 0;
//        if (gbDpbStatus == 2) {
        for (GbDistributerPurchaseGoodsEntity purGoods : batch.getGbDPGEntities()) {
            purGoods.setGbDpgPayType(batch.getGbDpbPayType());
            purGoods.setGbDpgStatus(3);
            purGoods.setGbDpgPurchaseType(21);
            purGoods.setGbDpgPurchaseNxSupplierId(gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());
            gbDPGService.update(purGoods);
            orderCount += purGoods.getGbDpgOrdersAmount();
            Integer gbDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", gbDistributerPurchaseGoodsId);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);

            for (GbDepartmentOrdersEntity orders : ordersEntities) {
                BigDecimal gbDoWeight = new BigDecimal(orders.getGbDoWeight());
                BigDecimal gbDoPrice = new BigDecimal(orders.getGbDoPrice());
                BigDecimal gbDoSubtotal = new BigDecimal(orders.getGbDoSubtotal());

                if (gbDoWeight.compareTo(BigDecimal.ZERO) == 1 && gbDoPrice.compareTo(BigDecimal.ZERO) == 1 &&
                        gbDoSubtotal.compareTo(BigDecimal.ZERO) == 1) {

                    if (orders.getGbDoOrderType().equals(getGbOrderTypeTuihuo())) {
                        Integer gbDoDgsrReturnId = orders.getGbDoDgsrReturnId();
                        GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentStockReduceService.queryObject(gbDoDgsrReturnId);
                        reduceEntity.setGbDgsrStatus(2);
                        gbDepartmentStockReduceService.update(reduceEntity);
                    } else {
                        saveDepStockDataByPurchase(orders, gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());
                    }

                    orders.setGbDoStatus(getGbOrderStatusReceived()); //wancheng
                    orders.setGbDoBuyStatus(getGbOrderBuyStatusUnPayFinish());

                    gbDepartmentOrdersService.update(orders);

                }

            }
        }

        batch.setGbDpbFinishFullTime(formatFullTime());
        batch.setGbDpbStatus(3); //如果是记账，status == 2， 开票完成后，status == 3

        gbDPBService.update(batch);

        Integer gbDpbDistributerId = batch.getGbDpbDistributerId();
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDpbDistributerId);

        GbDistributerPayListEntity payListEntity = new GbDistributerPayListEntity();
        payListEntity.setGbNdplPaySubtotal(Integer.valueOf(orderCount).toString());
        payListEntity.setGbNdplPayTime(formatFullTime());
        payListEntity.setGbNdplPayDate(formatWhatDay(0));
        payListEntity.setGbNdplPayMonth(formatWhatMonth(0));
        payListEntity.setGbNdplPayYear(formatWhatYear(0));
        payListEntity.setGbNdplStatus(0);
        payListEntity.setGbNdplType(0);
        payListEntity.setGbNdplRestPoints(Integer.valueOf(gbDistributerEntity.getGbDistributerBuyQuantity()));
        payListEntity.setGbNdplGbDisId(gbDistributerEntity.getGbDistributerId());
        payListEntity.setGbNdplNxSupplierId(gbDistributerPurchaseBatchEntity.getGbDpbSupplierId());
        payListEntity.setGbNdplGbPbId(gbDistributerPurchaseBatchEntity.getGbDistributerPurchaseBatchId());
        payListService.save(payListEntity);

        BigDecimal decimal = new BigDecimal(gbDistributerEntity.getGbDistributerBuyQuantity());
        BigDecimal decimal1 = new BigDecimal(orderCount);
        BigDecimal add = decimal.subtract(decimal1).setScale(0, BigDecimal.ROUND_HALF_UP);
        gbDistributerEntity.setGbDistributerBuyQuantity(add.toString());
        gbDistributerService.update(gbDistributerEntity);

//        }


    }

    @RequestMapping(value = "/updatePurchaseBatch")
    @ResponseBody
    public R updatePurchaseBatch(@RequestBody GbDistributerPurchaseBatchEntity batchEntity) {

        gbDPBService.update(batchEntity);
        return R.ok();
    }


    @RequestMapping(value = "/updateBatchBuyerIdGb", method = RequestMethod.POST)
    @ResponseBody
    public R updateBatchBuyerIdGb(Integer buyerId, Integer batchId, String openId) {
        GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDPBService.queryObject(batchId);
        gbDistributerPurchaseBatchEntity.setGbDpbBuyUserId(buyerId);
        gbDistributerPurchaseBatchEntity.setGbDpbStatus(getGbDisPurchaseBatchUnRead());
        gbDistributerPurchaseBatchEntity.setGbDpbBuyUserOpenId(openId);
        gbDPBService.update(gbDistributerPurchaseBatchEntity);
        return R.ok();
    }


    /**
     * 采购员获取
     *
     * @param disId 不是采购部门
     * @return 采购批次
     */
    @RequestMapping(value = "/disGetBuyingGoodsGb/{disId}")
    @ResponseBody
    public R disGetBuyingGoodsGb(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 2);
        List<GbDistributerPurchaseBatchEntity> batchEntities = gbDPBService.queryDisPurchaseBatch(map);
        if (batchEntities.size() > 0) {
            return R.ok().put("data", batchEntities);

        } else {
            return R.error(-1, "没有订货");
        }
    }

    @RequestMapping(value = "/purUserGetBuyingGoods/{userId}")
    @ResponseBody
    public R purUserGetBuyingGoods(@PathVariable Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("status", 2);
        List<GbDistributerPurchaseBatchEntity> batchEntities = gbDPBService.queryDisPurchaseBatch(map);
        return R.ok().put("data", batchEntities);
    }

    @RequestMapping(value = "/jingjingGetBuyingGoodsGb")
    @ResponseBody
    public R jingjingGetBuyingGoodsGb(Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("priceDate", formatWhatDay(0));
        map.put("cityId", gbDistributerService.queryObject(disId).getGbDistributerSysCityId());
        System.out.println("abbdbdbd" + map);
        List<GbDistributerPurchaseBatchEntity> batchEntities = gbDPBService.queryDisPurchaseBatch(map);
        System.out.println("siisisisisisi" + batchEntities.size());
        if (batchEntities.size() > 0) {
            for (GbDistributerPurchaseBatchEntity batchEntity : batchEntities) {
                if (batchEntity.getGbDPGEntities().size() == 0) {
                    gbDPBService.delete(batchEntity.getGbDistributerPurchaseBatchId());
                }
            }
        }

        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 1);
        map1.put("orderType", 2);
        map1.put("equalBuyStatus", 0);
        System.out.println("mapp111" + map1);
        int purCount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map1);
        map1.put("orderType", null);
        map1.put("notEqualOrderType", 5);
        map1.put("status", 4);
        map1.put("equalBuyStatus", null);
        map1.put("dayuBuyStatus", 0);
        map1.put("buyStatus", 5);
        System.out.println("wxsoroorrororoddddd" + map1);
        int purCountOne = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map1);
        map1.put("status", 4);
        map1.put("dayuBuyStatus", null);
        map1.put("notEqualOrderType", 2);
        map1.put("orderType", 5);
        System.out.println("fafdaafmap1map1mapappppppppporderTypeONeonenenne5555" + map1);
        int count2 = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map1);

        Map<String, Object> map3 = new HashMap<>();

        map3.put("arr", batchEntities);
        map3.put("orderAmount", purCount);
        map3.put("wxAmount", purCountOne);
        map3.put("appAmount", count2);
        map3.put("disInfo", gbDistributerService.queryDistributerInfo(disId));
        return R.ok().put("data", map3);

    }

    @RequestMapping(value = "/purchaserGetBuyingGoodsGb")
    @ResponseBody
    public R purchaserGetBuyingGoodsGb(Integer userId, Integer disId, Integer userAdmin, Integer depId, Integer orderType) {
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("status", 3);
        List<GbDistributerPurchaseBatchEntity> batchEntities = gbDPBService.queryDisPurchaseBatch(map);

        Map<String, Object> map11 = new HashMap<>();
        map11.put("purDepId", depId);
        map11.put("status", 1);
        int count0 = gbDPGService.queryPurchaseGoodsAmount(map11);

        Map<String, Object> map12 = new HashMap<>();
        map12.put("purUserId", userId);
        map12.put("status", 2);
        Integer count1 = gbDPBService.queryDisPurchaseBatchCount(map12);

        Map<String, Object> map13 = new HashMap<>();
        map13.put("purUserId", userId);
        map13.put("equalStatus", 1);
        map13.put("batchId", -1);
        map13.put("weightId", -1);
        Integer count2 = gbDPGService.queryGbPurchaseGoodsCount(map13);

        //
        Map<String, Object> map14 = new HashMap<>();
        map14.put("toDepId", depId);
        map14.put("equalStatus", 2);
        map14.put("orderType", orderType);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryDistributerTodayDepartments(map14);
        Map<String, Object> todayData = packageDisOrderByDep(departmentEntities, 0);
        List<GbDepartmentEntity> arr = (List<GbDepartmentEntity>) todayData.get("arr");

        Map<String, Object> map1 = new HashMap<>();
        map1.put("batchArr", batchEntities);
        map1.put("purGoodsAmount", count0);
        map1.put("haoyouAmount", count1);
        map1.put("finishAmount", count2);
        map1.put("deliveryAmount", arr.size());
        return R.ok().put("data", map1);

    }


    private Map<String, Object> packageDisOrderByDep(List<GbDepartmentEntity> departmentEntities, Integer which) {
        Map<String, Object> map = new HashMap<>();
        map.put("week", getWeek(which));
        map.put("hao", getJustHao(which));
        //根据部门是否有子部门组装部门订单
        //1,返回list
        List<Map<String, Object>> dataMap = new ArrayList<>();
        //2,有子部门的父部门
        TreeSet<GbDepartmentEntity> fatherDep = new TreeSet<>();
        //3，type是1的部门
        List<GbDepartmentEntity> subDepList = new ArrayList<>();


        for (GbDepartmentEntity dep : departmentEntities) {

            Integer fatherId = dep.getGbDepartmentFatherId();
            if (fatherId.equals(0)) {
                Map<String, Object> depMap = new HashMap<>();
                depMap.put("depHasSubs", 0);
                depMap.put("depId", dep.getGbDepartmentId());
                depMap.put("depName", dep.getGbDepartmentName());
                depMap.put("arrName", dep.getGbDepartmentAttrName());
                depMap.put("depOrders", dep.getGbDepartmentOrdersEntities());
                depMap.put("orderTotal", dep.getGbDepartmentOrdersEntities().size());
                depMap.put("choiceTotal", dep.getGbDepartmentOrdersEntities().size());
                dataMap.add(depMap);
            } else {
                Integer gbDepartmentFatherId = dep.getGbDepartmentFatherId();
                GbDepartmentEntity departmentEntity1 = gbDepartmentService.queryObject(gbDepartmentFatherId);
                fatherDep.add(departmentEntity1);
                subDepList.add(dep);
            }
        }

        for (GbDepartmentEntity father : fatherDep) {
            Map<String, Object> fatherMap = new HashMap<>();
            fatherMap.put("depHasSubs", 1);
            fatherMap.put("depId", father.getGbDepartmentId());
            fatherMap.put("depName", father.getGbDepartmentName());
            fatherMap.put("arrName", father.getGbDepartmentAttrName());

            List<GbDepartmentEntity> subDeps = new ArrayList<>();
            for (GbDepartmentEntity sub : subDepList) {
                if (father.getGbDepartmentId().equals(sub.getGbDepartmentFatherId())) {
                    subDeps.add(sub);
                }
            }
            fatherMap.put("subDeps", subDeps);
            dataMap.add(fatherMap);
        }
        map.put("arr", dataMap);
        return map;
    }

    /**
     * @param depId 门店和库房和中央厨房都可以
     * @return 采购批次
     */
    @RequestMapping(value = "/getDepartmentBuyingGoodsGb/{depId}")
    @ResponseBody
    public R getDepartmentBuyingGoodsGb(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", depId);
        map.put("status", 2);
        List<GbDistributerPurchaseBatchEntity> purchaseBatch = gbDPBService.queryDepartmentPurchaseBatch(map);
        return R.ok().put("data", purchaseBatch);
    }


    /**
     * 批发商获取进货商品列表
     *
     * @param batchId
     * @return
     */
    @RequestMapping(value = "/getDisPurchaseGoodsBatchGb/{batchId}")
    @ResponseBody
    public R getDisPurchaseGoodsBatchGb(@PathVariable Integer batchId) {
        System.out.println("bababbtidid" + batchId);
        GbDistributerPurchaseBatchEntity entity = gbDPBService.queryBatchWithOrders(batchId);
        if (entity != null) {
            return R.ok().put("data", entity);
        } else {
            return R.error(-1, "没有订单");
        }
    }


    /**
     * 采购员删除订货批次->采购商品->"订单"
     *
     * @param id 订单id
     * @return ok
     */
    @RequestMapping(value = "/deleteDisPurBatchGbOrder/{id}")
    @ResponseBody
    public R deleteDisPurBatchGbOrder(@PathVariable Integer id) {

        GbDepartmentOrdersEntity orders = gbDepartmentOrdersService.queryObject(id);
        System.out.println("stastst" + orders.getGbDoStatus());
        if (orders.getGbDoBuyStatus() < 3) {
            Integer gbDoPurchaseGoodsId = orders.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity purGoods = gbDPGService.queryObject(gbDoPurchaseGoodsId);

            //如果只有一个采购商品，删除采购批次
            Integer gbDpgBatchId = purGoods.getGbDpgBatchId();
            Map<String, Object> map1 = new HashMap<>();
            map1.put("batchId", gbDpgBatchId);
            List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDPGService.queryPurchaseGoodsByParams(map1);
            if (goodsEntities.size() == 1) {
                Integer purchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", purchaseGoodsId);
                List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
                if (ordersEntities.size() == 1) {
                    gbDPBService.delete(gbDpgBatchId);
                }
            }


            Integer gbDpgOrdersAmount = purGoods.getGbDpgOrdersAmount();
            if (gbDpgOrdersAmount == 1) {
                Integer gbDpgDisGoodsPriceId = purGoods.getGbDpgDisGoodsPriceId();
                if (gbDpgDisGoodsPriceId != null) {
                    gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);
                }
                purGoods.setGbDpgPurUserId(null);
                purGoods.setGbDpgStatus(0);
                purGoods.setGbDpgTime(null);
                purGoods.setGbDpgBuySubtotal(null);
                purGoods.setGbDpgBuyPrice(null);
                purGoods.setGbDpgBuyQuantity(null);
                purGoods.setGbDpgBuyScalePrice(null);
                purGoods.setGbDpgBuyScaleQuantity(null);
                purGoods.setGbDpgPurchaseFullTime(null);
                purGoods.setGbDpgPurchaseMonth(null);
                purGoods.setGbDpgPurchaseYear(null);
                purGoods.setGbDpgPurchaseWeek(null);
                purGoods.setGbDpgPurchaseWeekYear(null);
                purGoods.setGbDpgBatchId(null);
                gbDPGService.update(purGoods);
            } else {

                BigDecimal purQuantity = new BigDecimal(purGoods.getGbDpgQuantity());
                BigDecimal orderQuantity = new BigDecimal(orders.getGbDoQuantity());
                BigDecimal result = purQuantity.subtract(orderQuantity).setScale(2, BigDecimal.ROUND_HALF_UP);
                purGoods.setGbDpgQuantity(result.toString());
                purGoods.setGbDpgOrdersAmount(purGoods.getGbDpgOrdersAmount() - 1);
                gbDPGService.update(purGoods);

                //新添加一个采购商品
                GbDistributerPurchaseGoodsEntity disGoods = new GbDistributerPurchaseGoodsEntity();
                disGoods.setGbDpgDisGoodsFatherId(orders.getGbDoDisGoodsFatherId());
                disGoods.setGbDpgDisGoodsId(orders.getGbDoDisGoodsId());
                disGoods.setGbDpgDistributerId(orders.getGbDoDistributerId());
                disGoods.setGbDpgApplyDate(formatWhatDay(0));
                disGoods.setGbDpgStatus(0);
                disGoods.setGbDpgOrdersAmount(1);
                disGoods.setGbDpgOrdersFinishAmount(0);
                disGoods.setGbDpgOrdersBillAmount(0);
                disGoods.setGbDpgStandard(orders.getGbDoStandard());
                disGoods.setGbDpgQuantity(orders.getGbDoQuantity());
                disGoods.setGbDpgBuyScale(orders.getGbDoDsStandardScale());
                disGoods.setGbDpgPurchaseDepartmentId(orders.getGbDoDepartmentFatherId());
                disGoods.setGbDpgPurchaseNxDistributerId(orders.getGbDoNxDistributerId());
                disGoods.setGbDpgPurchaseType(1);
                disGoods.setGbDpgPurchaseWeek(getWeek(0));
                disGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                disGoods.setGbDpgIsCheck(0);
                gbDPGService.save(disGoods);
                Integer gbDistributerPurchaseGoodsId = disGoods.getGbDistributerPurchaseGoodsId();
                orders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);

            }
            orders.setGbDoBuyStatus(getGbOrderBuyStatusNew());
            orders.setGbDoWeight("0.0");
            orders.setGbDoPrice("0.0");
            orders.setGbDoSubtotal("0.0");
            orders.setGbDoScalePrice("0.0");
            orders.setGbDoScaleWeight("0.0");
            orders.setGbDoStatus(getGbOrderStatusNew());
            orders.setGbDoPurchaseUserId(null);
            gbDepartmentOrdersService.update(orders);

            return R.ok();

        } else {
            System.out.println("wieeieieb请刷新数据请刷新数据请刷新数据");
            return R.error(-1, "请刷新数据");
        }


    }


    /**
     * 删除订货批次->"采购商品"
     *
     * @param id 采购商品id
     * @return ok
     */
    @RequestMapping(value = "/deleteDisPurBatchGbItem/{id}")
    @ResponseBody
    public R deleteDisPurBatchGbItem(@PathVariable Integer id) {
        GbDistributerPurchaseGoodsEntity purGoods = gbDPGService.queryObject(id);
        if (purGoods.getGbDpgStatus() == 1) {
            Integer gbDpgBatchId = purGoods.getGbDpgBatchId();
            Map<String, Object> map1 = new HashMap<>();
            map1.put("batchId", gbDpgBatchId);
            List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDPGService.queryPurchaseGoodsByParams(map1);
            if (goodsEntities.size() == 1) {
                gbDPBService.delete(gbDpgBatchId);
            }
            Integer gbDpgDisGoodsPriceId = purGoods.getGbDpgDisGoodsPriceId();
            if (gbDpgDisGoodsPriceId != null) {
                gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);
            }
            purGoods.setGbDpgPurchaseType(2);
            purGoods.setGbDpgDisGoodsPriceId(null);
            purGoods.setGbDpgBatchId(null);
            purGoods.setGbDpgPurUserId(null);
            purGoods.setGbDpgStatus(0);
            purGoods.setGbDpgTime(null);
            purGoods.setGbDpgBuySubtotal(null);
            purGoods.setGbDpgBuyPrice(null);
            purGoods.setGbDpgBuyQuantity(null);
            purGoods.setGbDpgBuyScalePrice(null);
            purGoods.setGbDpgBuyScaleQuantity(null);
            purGoods.setGbDpgPurchaseFullTime(null);
            purGoods.setGbDpgPurchaseMonth(null);
            purGoods.setGbDpgPurchaseYear(null);
            purGoods.setGbDpgPurchaseWeek(null);
            purGoods.setGbDpgPurchaseWeekYear(null);
            purGoods.setGbDpgPurchaseNxDistributerId(-1);
            purGoods.setGbDpgPurchaseNxSupplierId(-1);
            purGoods.setGbDpgWasteFullTime(null);
            purGoods.setGbDpgWarnFullTime(null);
            System.out.println("purururruururur" + purGoods.getGbDistributerPurchaseGoodsId());
            gbDPGService.update(purGoods);

            Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
            GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
            goodsEntity.setGbDgGbSupplierId(-1);
            goodsEntity.setGbDgNxDistributerId(-1);
            goodsEntity.setGbDgNxDistributerGoodsId(-1);
            goodsEntity.setGbDgGoodsType(2);
            gbDistributerGoodsService.update(goodsEntity);

            Integer nxDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", nxDistributerPurchaseGoodsId);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
            for (GbDepartmentOrdersEntity orders : ordersEntities) {

                if (orders.getGbDoNxDepartmentOrderId() != null && orders.getGbDoNxDepartmentOrderId() != -1) {
                    System.out.println("nxnxnxnxxxoxoxxoxoxoxo" + orders.getGbDoNxDepartmentOrderId());
                    Integer gbDoNxDepartmentOrderId = orders.getGbDoNxDepartmentOrderId();
                    NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
                    if (ordersEntity1 != null) {
                        System.out.println("nxnxnxnxxxoxoxxoxoxoxo" + ordersEntity1.getNxDepartmentOrdersId());
                        delNxPurGoods(ordersEntity1);
                        nxDepartmentOrdersService.delete(ordersEntity1.getNxDepartmentOrdersId());
                    }
                }

                orders.setGbDoBuyStatus(getGbOrderBuyStatusNew());
                orders.setGbDoWeight("0.0");
                orders.setGbDoPrice("0.0");
                orders.setGbDoSubtotal("0.0");
                orders.setGbDoScalePrice("0.0");
                orders.setGbDoScaleWeight("0.0");
                orders.setGbDoStatus(getGbOrderStatusNew());
                orders.setGbDoPurchaseUserId(null);
                orders.setGbDoOrderType(2);
                orders.setGbDoGoodsType(2);
                orders.setGbDoNxDistributerGoodsId(-1);
                orders.setGbDoNxDistributerId(-1);
                orders.setGbDoNxDepartmentOrderId(null);
                gbDepartmentOrdersService.update(orders);

            }
            return R.ok();

        } else {
            return R.error(-1, "请刷新数据");
        }
    }


    private void delNxPurGoods(NxDepartmentOrdersEntity ordersEntity) {
//
//                && ordersEntity.getNxDoPurchaseStatus() < getNxDepOrderBuyStatusFinishPurchase()

        if (ordersEntity.getNxDoPurchaseGoodsId() != null && ordersEntity.getNxDoPurchaseGoodsId() != -1) {
            NxDistributerPurchaseGoodsEntity nxDistributerPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(ordersEntity.getNxDoPurchaseGoodsId());
            if (nxDistributerPurchaseGoodsEntity != null) {
                Integer nxDpgOrdersAmount = nxDistributerPurchaseGoodsEntity.getNxDpgOrdersAmount();
                System.out.println("purgoodoamaomm" + nxDpgOrdersAmount);
                if (nxDpgOrdersAmount > 1) {
                    nxDistributerPurchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
                    BigDecimal newWeight = new BigDecimal(nxDistributerPurchaseGoodsEntity.getNxDpgQuantity()).subtract(new BigDecimal(ordersEntity.getNxDoQuantity()));
                    nxDistributerPurchaseGoodsEntity.setNxDpgBuyQuantity(newWeight.toString());
                    nxDistributerPurchaseGoodsEntity.setNxDpgQuantity(newWeight.toString());
                    System.out.println("purgoodoamaomm111" + nxDistributerPurchaseGoodsEntity.getNxDpgBuyQuantity());

                    if (ordersEntity.getNxDoStandard().equals(nxDistributerPurchaseGoodsEntity.getNxDpgStandard())) {
                        System.out.println("purgoodoamaomm" + nxDpgOrdersAmount);

                        BigDecimal price = new BigDecimal(nxDistributerPurchaseGoodsEntity.getNxDpgBuyPrice());
                        BigDecimal decimal1 = newWeight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                        nxDistributerPurchaseGoodsEntity.setNxDpgBuySubtotal(decimal1.toString());
                    }
                    nxDistributerPurchaseGoodsService.update(nxDistributerPurchaseGoodsEntity);
                } else {
//                    if (nxDistributerPurchaseGoodsEntity.getNxDpgStatus() == getNxDisPurchaseGoodsUnBuy() || nxDistributerPurchaseGoodsEntity.getNxDpgStatus() == getNxDisPurchaseGoodsWithBatch()) {
                    Integer nxDpgBatchId = nxDistributerPurchaseGoodsEntity.getNxDpgBatchId();
                    List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByBatchId(nxDpgBatchId);
                    if (purchaseGoodsEntities.size() == 1) {
                        nxDPBService.delete(nxDpgBatchId);
                    } else {
                        String nxDpgBuySubtotal = nxDistributerPurchaseGoodsEntity.getNxDpgBuySubtotal();
                        NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryObject(nxDpgBatchId);
                        if (nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal() != null && !nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal().trim().isEmpty()) {
                            BigDecimal decimal = new BigDecimal(nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal());
                            BigDecimal decimal2 = new BigDecimal(nxDpgBuySubtotal);
                            BigDecimal decimal1 = decimal.subtract(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                            nxDistributerPurchaseBatchEntity.setNxDpbSellSubtotal(decimal1.toString());
                            nxDPBService.update(nxDistributerPurchaseBatchEntity);
                        }
                    }
//                    }

                    nxDistributerPurchaseGoodsService.delete(nxDistributerPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
                }
            }
        }
    }

    /**
     * 采购员删除采购批次
     *
     * @param batchId
     * @return
     */
    @RequestMapping(value = "/deleteDisBatchGb/{batchId}")
    @ResponseBody
    public R deleteDisBatchGb(@PathVariable Integer batchId) {

        GbDistributerPurchaseBatchEntity batchEntity = gbDPBService.queryBatchWithOrders(batchId);
        for (GbDistributerPurchaseGoodsEntity purGoods : batchEntity.getGbDPGEntities()) {
            Integer gbDpgDisGoodsPriceId = purGoods.getGbDpgDisGoodsPriceId();
            if (gbDpgDisGoodsPriceId != null) {
                gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);
            }
            purGoods.setGbDpgPurchaseDate(null);
            purGoods.setGbDpgPurchaseMonth(null);
            purGoods.setGbDpgPurchaseYear(null);
            purGoods.setGbDpgPurchaseFullTime(null);
            purGoods.setGbDpgPurchaseWeek(null);
            purGoods.setGbDpgPurchaseWeekYear(null);
            purGoods.setGbDpgPurchaseDepartmentId(null);
            purGoods.setGbDpgTime(null);
            purGoods.setGbDpgBatchId(null);
            purGoods.setGbDpgBuyPrice(null);
            purGoods.setGbDpgStatus(0);
            purGoods.setGbDpgPurchaseType(2);
            purGoods.setGbDpgDisGoodsPriceId(null);
            purGoods.setGbDpgBuySubtotal(null);
            purGoods.setGbDpgBuyPrice(null);
            purGoods.setGbDpgBuyQuantity(null);
            purGoods.setGbDpgBuyScalePrice(null);
            purGoods.setGbDpgBuyScaleQuantity(null);
            purGoods.setGbDpgPurUserId(null);
            gbDPGService.update(purGoods);

            Integer gbDoDisGoodsId = purGoods.getGbDpgDisGoodsId();
            GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
            goodsEntity.setGbDgGbSupplierId(-1);
            goodsEntity.setGbDgNxDistributerId(-1);
            goodsEntity.setGbDgNxDistributerGoodsId(-1);
            gbDistributerGoodsService.update(goodsEntity);

            Integer gbDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", gbDistributerPurchaseGoodsId);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
            for (GbDepartmentOrdersEntity orders : ordersEntities) {
                orders.setGbDoBuyStatus(0);
                orders.setGbDoStatus(getGbOrderStatusNew());
                orders.setGbDoWeight("0");
                orders.setGbDoSubtotal("0");
                orders.setGbDoPrice(null);
                orders.setGbDoScalePrice(null);
                orders.setGbDoScaleWeight(null);
                orders.setGbDoPurchaseUserId(null);
                gbDepartmentOrdersService.update(orders);


            }
        }

        gbDPBService.delete(batchId);
        return R.ok();
    }

    /**
     * 采购员分享进货商品
     *
     * @param
     * @return ok  shareGbPurchaseGoodsStatus
     */
    @RequestMapping(value = "/saveDisPurGoodsBatchGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveDisPurGoodsBatchGb(@RequestBody GbDistributerPurchaseBatchEntity batchEntity) {
        try {
            batchEntity.setGbDpbDate(formatWhatDay(0));
            batchEntity.setGbDpbHour(formatWhatHour(0));
            batchEntity.setGbDpbMinute(formatWhatMinute(0));
            batchEntity.setGbDpbTime(formatWhatTime(0));
            batchEntity.setGbDpbPurchaseMonth(formatWhatMonth(0));
            batchEntity.setGbDpbPurchaseWeek(getWeek(0));
            batchEntity.setGbDpbPurchaseYear(formatWhatYear(0));
            batchEntity.setGbDpbPurchaseFullTime(formatWhatYearDayTime(0));
            batchEntity.setGbDpbStatus(getGbDisPurchaseBatchUnRead());
            gbDPBService.save(batchEntity);
            System.out.println("savvbabba" + batchEntity);

        for (GbDistributerPurchaseGoodsEntity gbPurGoods : batchEntity.getGbDPGEntities()) {
            Integer gbDpgDisGoodsId = gbPurGoods.getGbDpgDisGoodsId();
            GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
            List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = gbPurGoods.getGbDepartmentOrdersEntities();
            List<GbDepartmentOrdersEntity> unChoiceOrderList = new ArrayList<>();
            BigDecimal buytotal = new BigDecimal(0);
            for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                Boolean hasChoice = orders.getIsNotice();
                if (hasChoice) {
                    orders.setGbDoBuyStatus(getGbOrderBuyStatusProcurement());
                    orders.setGbDoPurchaseUserId(batchEntity.getGbDpbPurUserId());
                    buytotal = buytotal.add(new BigDecimal(orders.getGbDoQuantity()));
                    gbDepartmentOrdersService.update(orders);
                } else {
                    unChoiceOrderList.add(orders);
                }
            }

            Integer newLength = nxDepartmentOrdersEntities.size() - unChoiceOrderList.size();
            gbPurGoods.setGbDpgOrdersAmount(newLength);
            gbPurGoods.setGbDpgPurchaseType(2);
            gbPurGoods.setGbDpgBatchId(batchEntity.getGbDistributerPurchaseBatchId());
            gbPurGoods.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
            gbPurGoods.setGbDpgPurchaseDate(formatWhatDay(0));
            gbPurGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
            gbPurGoods.setGbDpgPurchaseYear(formatWhatYear(0));
            gbPurGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
            gbPurGoods.setGbDpgPurchaseWeek(getWeek(0));
            gbPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            gbPurGoods.setGbDpgTime(formatWhatTime(0));
            gbPurGoods.setGbDpgQuantity(buytotal.toString());
            gbPurGoods.setGbDpgDisGoodsGrandId(gbDistributerGoodsEntity.getGbDgDfgGoodsGrandId());
            gbDPGService.update(gbPurGoods);

            if (unChoiceOrderList.size() > 0) {
                GbDistributerPurchaseGoodsEntity disGoods = new GbDistributerPurchaseGoodsEntity();
                disGoods.setGbDpgDisGoodsFatherId(unChoiceOrderList.get(0).getGbDoDisGoodsFatherId());
                disGoods.setGbDpgDisGoodsId(unChoiceOrderList.get(0).getGbDoDisGoodsId());
                disGoods.setGbDpgDistributerId(unChoiceOrderList.get(0).getGbDoDistributerId());
                disGoods.setGbDpgApplyDate(formatWhatDay(0));
                disGoods.setGbDpgStatus(0);
                disGoods.setGbDpgTime(formatWhatTime(0));
                disGoods.setGbDpgOrdersAmount(unChoiceOrderList.size());
                disGoods.setGbDpgOrdersFinishAmount(0);
                disGoods.setGbDpgOrdersBillAmount(0);
                disGoods.setGbDpgPurchaseWeek(getWeek(0));
                disGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                disGoods.setGbDpgIsCheck(0);
                disGoods.setGbDpgPurchaseDepartmentId(unChoiceOrderList.get(0).getGbDoToDepartmentId());
                disGoods.setGbDpgPurchaseType(2);
                disGoods.setGbDpgPurchaseNxSupplierId(-1);
                disGoods.setGbDpgPurchaseNxDistributerId(-1);
                disGoods.setGbDpgDisGoodsGrandId(gbDistributerGoodsEntity.getGbDgDfgGoodsGrandId());

                gbDPGService.save(disGoods);
                BigDecimal unPurQuantity = new BigDecimal(0);
                for (GbDepartmentOrdersEntity unChoiceOrder : unChoiceOrderList) {
                    Integer gbDistributerPurchaseGoodsId = disGoods.getGbDistributerPurchaseGoodsId();
                    unChoiceOrder.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                    gbDepartmentOrdersService.update(unChoiceOrder);
                    BigDecimal orderQuantity = new BigDecimal(unChoiceOrder.getGbDoQuantity());
                    unPurQuantity = unPurQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);

                }
                disGoods.setGbDpgQuantity(unPurQuantity.toString());
                disGoods.setGbDpgStandard(unChoiceOrderList.get(0).getGbDoStandard());
                gbDPGService.update(disGoods);
            }
        }

            GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDPBService.queryBatchWithOrders(batchEntity.getGbDistributerPurchaseBatchId());
            return R.ok().put("data", gbDistributerPurchaseBatchEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("保存失败：" + e.getMessage());
        }
    }


    @RequestMapping(value = "/saveDisPurGoodsBatchGbSx", method = RequestMethod.POST)
    @ResponseBody
    public R saveDisPurGoodsBatchGbSx(@RequestBody GbDistributerPurchaseBatchEntity batchEntity) {
        batchEntity.setGbDpbDate(formatWhatDay(0));
        batchEntity.setGbDpbHour(formatWhatHour(0));
        batchEntity.setGbDpbMinute(formatWhatMinute(0));
        batchEntity.setGbDpbTime(formatWhatTime(0));
        batchEntity.setGbDpbPurchaseMonth(formatWhatMonth(0));
        batchEntity.setGbDpbPurchaseWeek(getWeek(0));
        batchEntity.setGbDpbPurchaseYear(formatWhatYear(0));
        batchEntity.setGbDpbPurchaseFullTime(formatWhatYearDayTime(0));
        batchEntity.setGbDpbStatus(getGbDisPurchaseBatchUnSend());
        gbDPBService.save(batchEntity);

        for (GbDistributerPurchaseGoodsEntity gbPurGoods : batchEntity.getGbDPGEntities()) {
            Integer gbDpgDisGoodsId = gbPurGoods.getGbDpgDisGoodsId();
            GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
            List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = gbPurGoods.getGbDepartmentOrdersEntities();
            List<GbDepartmentOrdersEntity> unChoiceOrderList = new ArrayList<>();
            BigDecimal buytotal = new BigDecimal(0);
            for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                Boolean hasChoice = orders.getIsNotice();
                if (hasChoice) {
                    orders.setGbDoBuyStatus(getGbOrderBuyStatusProcurement());
                    orders.setGbDoPurchaseUserId(batchEntity.getGbDpbPurUserId());
                    buytotal = buytotal.add(new BigDecimal(orders.getGbDoQuantity()));
                    gbDepartmentOrdersService.update(orders);
                } else {
                    unChoiceOrderList.add(orders);
                }
            }

            Integer newLength = nxDepartmentOrdersEntities.size() - unChoiceOrderList.size();
            gbPurGoods.setGbDpgOrdersAmount(newLength);
            gbPurGoods.setGbDpgPurchaseType(2);
            gbPurGoods.setGbDpgBatchId(batchEntity.getGbDistributerPurchaseBatchId());
            gbPurGoods.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
            gbPurGoods.setGbDpgPurchaseDate(formatWhatDay(0));
            gbPurGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
            gbPurGoods.setGbDpgPurchaseYear(formatWhatYear(0));
            gbPurGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
            gbPurGoods.setGbDpgPurchaseWeek(getWeek(0));
            gbPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            gbPurGoods.setGbDpgTime(formatWhatTime(0));
            gbPurGoods.setGbDpgQuantity(buytotal.toString());
            gbPurGoods.setGbDpgDisGoodsGrandId(gbDistributerGoodsEntity.getGbDgDfgGoodsGrandId());
            gbDPGService.update(gbPurGoods);

            if (unChoiceOrderList.size() > 0) {
                GbDistributerPurchaseGoodsEntity disGoods = new GbDistributerPurchaseGoodsEntity();
                disGoods.setGbDpgDisGoodsFatherId(unChoiceOrderList.get(0).getGbDoDisGoodsFatherId());
                disGoods.setGbDpgDisGoodsId(unChoiceOrderList.get(0).getGbDoDisGoodsId());
                disGoods.setGbDpgDistributerId(unChoiceOrderList.get(0).getGbDoDistributerId());
                disGoods.setGbDpgApplyDate(formatWhatDay(0));
                disGoods.setGbDpgStatus(0);
                disGoods.setGbDpgTime(formatWhatTime(0));
                disGoods.setGbDpgOrdersAmount(unChoiceOrderList.size());
                disGoods.setGbDpgOrdersFinishAmount(0);
                disGoods.setGbDpgOrdersBillAmount(0);
                disGoods.setGbDpgPurchaseWeek(getWeek(0));
                disGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                disGoods.setGbDpgIsCheck(0);
                disGoods.setGbDpgPurchaseDepartmentId(unChoiceOrderList.get(0).getGbDoToDepartmentId());
                disGoods.setGbDpgPurchaseType(2);
                disGoods.setGbDpgPurchaseNxSupplierId(-1);
                disGoods.setGbDpgPurchaseNxDistributerId(-1);
                disGoods.setGbDpgDisGoodsGrandId(gbDistributerGoodsEntity.getGbDgDfgGoodsGrandId());

                gbDPGService.save(disGoods);
                BigDecimal unPurQuantity = new BigDecimal(0);
                for (GbDepartmentOrdersEntity unChoiceOrder : unChoiceOrderList) {
                    Integer gbDistributerPurchaseGoodsId = disGoods.getGbDistributerPurchaseGoodsId();
                    unChoiceOrder.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                    gbDepartmentOrdersService.update(unChoiceOrder);
                    BigDecimal orderQuantity = new BigDecimal(unChoiceOrder.getGbDoQuantity());
                    unPurQuantity = unPurQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);

                }
                disGoods.setGbDpgQuantity(unPurQuantity.toString());
                disGoods.setGbDpgStandard(unChoiceOrderList.get(0).getGbDoStandard());
                gbDPGService.update(disGoods);
            }
        }

        GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDPBService.queryBatchWithOrders(batchEntity.getGbDistributerPurchaseBatchId());
        return R.ok().put("data", gbDistributerPurchaseBatchEntity);
    }

    /**
     * 删除订货批次
     * @param batchId
     * @return
     */
//    @RequestMapping(value = "/delteDisPurBatchGb/{batchId}")
//    @ResponseBody
//    public R delteDisPurBatchGb(@PathVariable Integer batchId) {
//        GbDistributerPurchaseBatchEntity batchEntity = gbDPBService.queryBatchWithOrders(batchId);
//        if(batchEntity.getGbDpbStatus() == 1){
//            for (GbDistributerPurchaseGoodsEntity purGoods : batchEntity.getGbDPGEntities()) {
//                purGoods.setGbDpgBatchId(null);
//                purGoods.setGbDpgStatus(0);
//                purGoods.setGbDpgBuySubtotal("0.0");
//                purGoods.setGbDpgBuyPrice("0.0");
//                purGoods.setGbDpgBuyQuantity("0.0");
//                purGoods.setGbDpgBuyScalePrice("0.0");
//                purGoods.setGbDpgBuyScaleQuantity("0.0");
//                gbDPGService.update(purGoods);
//
//                Integer nxDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
//                Map<String, Object> map = new HashMap<>();
//                map.put("purGoodsId", nxDistributerPurchaseGoodsId);
//                List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
//                for (GbDepartmentOrdersEntity orders : ordersEntities) {
//                    orders.setGbDoBuyStatus(getGbOrderBuyStatusNew());
//                    orders.setGbDoWeight("0.0");
//                    orders.setGbDoPrice("0.0");
//                    orders.setGbDoSubtotal("0.0");
//                    orders.setGbDoScalePrice("0.0");
//                    orders.setGbDoScaleWeight("0.0");
//                    orders.setGbDoStatus(getGbOrderStatusNew());
//                    orders.setGbDoPurchaseUserId(null);
//                    gbDepartmentOrdersService.update(orders);
//                }
//            }
//            gbDPBService.delete(batchId);
//            return R.ok();
//        }else{
//            return R.error(-1,"请刷新数据");
//        }
//    }
}
