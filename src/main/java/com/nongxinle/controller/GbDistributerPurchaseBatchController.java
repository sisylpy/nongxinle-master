package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-25 22:52
 */

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.CommonUtils.generateBillTradeNo;
import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPurchaseBatchUnRead;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPurchaseGoodsWithBatch;


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


    @RequestMapping(value = "/getPurchaserPurBill", method = RequestMethod.POST)
    @ResponseBody
    public R getPurchaserPurBill (Integer userId, String date) {
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
        if(nxDisId == -1){
            List<GbDistributerPurchaseBatchEntity> purchaseBatch = gbDPBService.queryDisPurchaseBatch(map);
            mapR.put("arr", purchaseBatch);
        }else{
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
                 int returnlist =  gbDPBService.queryReturnList(map333);
                System.out.println(returnlist + "reuturlist");
                if(returnlist > 0){
                    Map<String, Object> map33 = new HashMap<>();
                    map33.put("purDepId", gbDepartmentId);
                    map33.put("equalStatus", 3);
                    map33.put("subDayu", -1);
                    Double aDouble3 = gbDPBService.querySupplierUnSettleSubtotal(map33);
                    BigDecimal returnResult = new BigDecimal(aDouble3);
                    BigDecimal subtract = result.subtract(returnResult.abs()).setScale(2,BigDecimal.ROUND_HALF_UP);
                    dep.setUnPayTotal(subtract.toString());
                    mapResult = mapResult.add(subtract);
                    System.out.println(mapResult + "mapresitutuutu");
                }else{
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

            if(purchaseGoodsEntities.size() > 0){
                for (GbDistributerPurchaseGoodsEntity purGoods:purchaseGoodsEntities) {
                    purGoods.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
                    gbDPGService.update(purGoods);

                    //
                    Integer purchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("purGoodsId", purchaseGoodsId);
                    List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
                    if(ordersEntities.size() > 0){
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
        if (gbDisPurBatchEntity.getGbDpbStatus() == 1) {
            gbDisPurBatchEntity.setGbDpbStatus(0);
            gbDPBService.update(gbDisPurBatchEntity);
        }
        GbDistributerPurchaseBatchEntity entity = gbDPBService.queryBatchWithOrders(batchId);
        return R.ok().put("data", entity);
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
        map41.put("payType", 1);
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
        map42.put("payType", 1);
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
        map43.put("status", 4 );
        map43.put("payType", 1);
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
            batchEntity.setGbDpbStatus(3); //如果是现金 status == 4 完成结账状态

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
        billEntity.setGbDbIssueOrderType(7);
        String areaCode = "1" + batchEntity.getGbDpbDistributerId();
        billEntity.setGbDbTradeNo(generateBillTradeNo(areaCode));
        gbDepartmentBillService.save(billEntity);


        //2， 修改订单和采购商品状态
        for (GbDistributerPurchaseGoodsEntity purGoods : batchEntity.getGbDPGEntities()) {
            purGoods.setGbDpgBatchId(batchEntity.getGbDistributerPurchaseBatchId());
            purGoods.setGbDpgStatus(2);
            purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
            gbDPGService.update(purGoods);

            Integer gbDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", gbDistributerPurchaseGoodsId);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);

            for (GbDepartmentOrdersEntity orders : ordersEntities) {
                orders.setGbDoStatus(4);
                orders.setGbDoBillId(billEntity.getGbDepartmentBillId());
                gbDepartmentOrdersService.update(orders);
            }
        }

        return R.ok();
    }


    @RequestMapping(value = "/finishSharePurGoodsBatch")
    @ResponseBody
    public R finishSharePurGoodsBatch(@RequestBody GbDistributerPurchaseBatchEntity batch) {
        Integer gbDistributerPurchaseBatchId = batch.getGbDistributerPurchaseBatchId();
        GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDPBService.queryObject(gbDistributerPurchaseBatchId);
        Integer gbDpbStatus = gbDistributerPurchaseBatchEntity.getGbDpbStatus();
        if (gbDpbStatus == 1) {
            for (GbDistributerPurchaseGoodsEntity purGoods : batch.getGbDPGEntities()) {
                purGoods.setGbDpgPayType(batch.getGbDpbPayType());
                purGoods.setGbDpgStatus(2);
                gbDPGService.update(purGoods);

                Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);

        if (purGoods.getGbDpgBuyPrice() != null
                && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
                 checkPurGoodsPrice(purGoods);

        }

                Integer gbDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", gbDistributerPurchaseGoodsId);
                List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);

                for (GbDepartmentOrdersEntity orders : ordersEntities) {
                    BigDecimal gbDoWeight = new BigDecimal(orders.getGbDoWeight());
                    BigDecimal gbDoPrice = new BigDecimal(orders.getGbDoPrice());
                    BigDecimal gbDoSubtotal = new BigDecimal(orders.getGbDoSubtotal());

                    if(gbDoWeight.compareTo(BigDecimal.ZERO) ==  1 && gbDoPrice.compareTo(BigDecimal.ZERO) == 1 &&
                        gbDoSubtotal.compareTo(BigDecimal.ZERO) == 1){
                        orders.setGbDoStatus(getGbOrderStatusHasFinished());
                        orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
                        gbDepartmentOrdersService.update(orders);
                    }

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
                if(nxJrdhsUserId == null){
                    System.out.println("dafafads" + nxJrdhsUserId);
                    supplierEntity.setNxJrdhsUserId(batch.getGbDpbSellUserId());
                    nxJrdhSupplierService.update(supplierEntity);
                }
            }
            gbDPBService.update(batch);
            return R.ok();

        } else {
            return R.error(-1, "请刷新数据。");
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

        if (buyPrice.compareTo(goodsHighest) == 1 && purchaseGoodsEntity.getGbDpgBuyQuantity() != null) { //高于最高价

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

        if (buyPrice.compareTo(goodsLowest) == -1 && purchaseGoodsEntity.getGbDpgBuyQuantity() != null) { //低于最低价
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
        if(integer > 0){
            Double  aDouble = gbDPBService.querySupplierUnSettleSubtotal(map6);
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
        if(integer > 0){
           Double  aDouble = gbDPBService.querySupplierUnSettleSubtotal(map6);
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
    public R sellUserReadDisBatchGb(String openId,  Integer sellUserId, Integer batchId) {
        GbDistributerPurchaseBatchEntity batchEntity = gbDPBService.queryObject(batchId);
        System.out.println("baciciciciici" + batchEntity.getGbDpbSupplierId());

        Map<String, Object> map = new HashMap<>();
        map.put("batchId", batchId);
        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDPGService.queryPurchaseGoodsByParams(map);
        for (GbDistributerPurchaseGoodsEntity purGoods : goodsEntities) {
            Integer gbDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
            Map<String, Object> mapOrder = new HashMap<>();
            mapOrder.put("purGoodsId", gbDistributerPurchaseGoodsId);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(mapOrder);
            for (GbDepartmentOrdersEntity order : ordersEntities) {
                order.setGbDoBuyStatus(getGbOrderBuyStatusPrepareing());
                gbDepartmentOrdersService.update(order);
            }
        }

        System.out.println("openeidid" + openId);
        batchEntity.setGbDpbSellUserOpenId(openId);
        batchEntity.setGbDpbSellUserId(sellUserId);
        batchEntity.setGbDpbStatus(getGbDisPurchaseBatchHaveRead());
        gbDPBService.update(batchEntity);

        GbDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = gbDPBService.queryBatchWithOrders(batchId);
        return R.ok().put("data", nxDistributerPurchaseBatchEntity);
    }


    @RequestMapping(value = "/sellerFinishPurchaseGoodsBatchGb")
    @ResponseBody
    public R sellerFinishPurchaseGoodsBatchGb(@RequestBody GbDistributerPurchaseBatchEntity batchEntity) {

        List<GbDistributerPurchaseGoodsEntity> nxDPBEntities = batchEntity.getGbDPGEntities();
        for (GbDistributerPurchaseGoodsEntity purGoods : nxDPBEntities) {
            purGoods.setGbDpgStatus(1);
            gbDPGService.update(purGoods);
        }
        batchEntity.setGbDpbStatus(1);
        batchEntity.setGbDpbSellerReplyFullTime(formatFullTime());
        gbDPBService.update(batchEntity);
        return R.ok();
    }

    @RequestMapping(value = "/updatePurchaseBatch")
    @ResponseBody
    public R updatePurchaseBatch(@RequestBody GbDistributerPurchaseBatchEntity batchEntity) {

        gbDPBService.update(batchEntity);
        return R.ok();
    }


    @RequestMapping(value = "/updateBatchBuyerIdGb", method = RequestMethod.POST)
    @ResponseBody
    public R updateBatchBuyerIdGb (Integer buyerId, Integer batchId, String openId) {
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

    @RequestMapping(value = "/purchaserGetBuyingGoodsGb")
    @ResponseBody
    public R purchaserGetBuyingGoodsGb(Integer userId, Integer disId, Integer userAdmin, Integer depId, Integer orderType) {
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("status", 2);
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
        GbDistributerPurchaseBatchEntity entity = gbDPBService.queryBatchWithOrders(batchId);
        if (entity != null) {
            return R.ok().put("data", entity);
        } else {
            return R.error(-1, "没有订单");
        }
    }


    /**
     * 采购员删除订货批次->采购商品->"订单"
     * @param id 订单id
     * @return ok
     */
    @RequestMapping(value = "/deleteDisPurBatchGbOrder/{id}")
    @ResponseBody
    public R deleteDisPurBatchGbOrder(@PathVariable Integer id) {

        GbDepartmentOrdersEntity orders = gbDepartmentOrdersService.queryObject(id);
        System.out.println("stastst" + orders.getGbDoStatus());
        if(orders.getGbDoBuyStatus()  < 3){
            Integer gbDoPurchaseGoodsId = orders.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity purGoods = gbDPGService.queryObject(gbDoPurchaseGoodsId);

            //如果只有一个采购商品，删除采购批次
            Integer gbDpgBatchId = purGoods.getGbDpgBatchId();
            Map<String, Object> map1 = new HashMap<>();
            map1.put("batchId", gbDpgBatchId);
            List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDPGService.queryPurchaseGoodsByParams(map1);
            if(goodsEntities.size() == 1){
                Integer purchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", purchaseGoodsId);
                List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
                if(ordersEntities.size() == 1){
                    gbDPBService.delete(gbDpgBatchId);
                }
            }



            Integer gbDpgOrdersAmount = purGoods.getGbDpgOrdersAmount();
            if(gbDpgOrdersAmount == 1){
                Integer gbDpgDisGoodsPriceId = purGoods.getGbDpgDisGoodsPriceId();
                if(gbDpgDisGoodsPriceId != null){
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
            }else{

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

        }else{
            System.out.println("wieeieieb请刷新数据请刷新数据请刷新数据");
            return R.error(-1, "请刷新数据");
        }


    }


    /**
     * 删除订货批次->"采购商品"
     * @param id 采购商品id
     * @return ok
     */
    @RequestMapping(value = "/deleteDisPurBatchGbItem/{id}")
    @ResponseBody
    public R deleteDisPurBatchGbItem(@PathVariable Integer id) {
        GbDistributerPurchaseGoodsEntity purGoods = gbDPGService.queryObject(id);
        if(purGoods.getGbDpgStatus() == 1){
            Integer gbDpgBatchId = purGoods.getGbDpgBatchId();
            Map<String, Object> map1 = new HashMap<>();
            map1.put("batchId", gbDpgBatchId);
            List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDPGService.queryPurchaseGoodsByParams(map1);
            if(goodsEntities.size() == 1){
                gbDPBService.delete(gbDpgBatchId);
            }
            Integer gbDpgDisGoodsPriceId = purGoods.getGbDpgDisGoodsPriceId();
            if(gbDpgDisGoodsPriceId != null){
                gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);
            }
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
            gbDPGService.update(purGoods);

            Integer nxDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", nxDistributerPurchaseGoodsId);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
            for (GbDepartmentOrdersEntity orders : ordersEntities) {
                orders.setGbDoBuyStatus(getGbOrderBuyStatusNew());
                orders.setGbDoWeight("0.0");
                orders.setGbDoPrice("0.0");
                orders.setGbDoSubtotal("0.0");
                orders.setGbDoScalePrice("0.0");
                orders.setGbDoScaleWeight("0.0");
                orders.setGbDoStatus(getGbOrderStatusNew());
                orders.setGbDoPurchaseUserId(null);
                gbDepartmentOrdersService.update(orders);

            }
            return R.ok();

        }else{
            return R.error(-1,"请刷新数据");
        }
    }

    /**
     * 采购员删除采购批次
     * @param batchId
     * @return
     */
    @RequestMapping(value = "/deleteDisBatchGb/{batchId}")
    @ResponseBody
    public R deleteDisBatchGb(@PathVariable Integer batchId) {

        GbDistributerPurchaseBatchEntity batchEntity = gbDPBService.queryBatchWithOrders(batchId);
        for (GbDistributerPurchaseGoodsEntity purGoods : batchEntity.getGbDPGEntities()) {
            Integer gbDpgDisGoodsPriceId = purGoods.getGbDpgDisGoodsPriceId();
            if(gbDpgDisGoodsPriceId != null){
                gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);
            }
            purGoods.setGbDpgPurchaseDate(null);
            purGoods.setGbDpgPurchaseMonth(null);
            purGoods.setGbDpgPurchaseYear(null);
            purGoods.setGbDpgPurchaseFullTime(null);
            purGoods.setGbDpgPurchaseWeek(null);
            purGoods.setGbDpgPurchaseWeekYear(null);
            purGoods.setGbDpgPurchaseDepartmentId(null);
            purGoods.setGbDpgPurchaseNxDistributerId(null);
            purGoods.setGbDpgTime(null);
            purGoods.setGbDpgBatchId(null);
            purGoods.setGbDpgBuyPrice(null);
            purGoods.setGbDpgStatus(0);
            purGoods.setGbDpgDisGoodsPriceId(null);
            purGoods.setGbDpgBuySubtotal(null);
            purGoods.setGbDpgBuyPrice(null);
            purGoods.setGbDpgBuyQuantity(null);
            purGoods.setGbDpgBuyScalePrice(null);
            purGoods.setGbDpgBuyScaleQuantity(null);
            purGoods.setGbDpgPurUserId(null);
            gbDPGService.update(purGoods);

            Integer gbDistributerPurchaseGoodsId = purGoods.getGbDistributerPurchaseGoodsId();
            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", gbDistributerPurchaseGoodsId);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
            for (GbDepartmentOrdersEntity orders : ordersEntities) {
                orders.setGbDoBuyStatus(0);
                orders.setGbDoStatus(getGbOrderStatusNew());
                orders.setGbDoWeight(null);
                orders.setGbDoPrice(null);
                orders.setGbDoScalePrice(null);
                orders.setGbDoScaleWeight(null);
                orders.setGbDoSubtotal(null);
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
            List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = gbPurGoods.getGbDepartmentOrdersEntities();
            List<GbDepartmentOrdersEntity> unChoiceOrderList = new ArrayList<>();

            for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                Boolean hasChoice = orders.getHasChoice();
                if (hasChoice) {
                    orders.setGbDoBuyStatus(getGbOrderBuyStatusProcurement());
                    orders.setGbDoPurchaseUserId(batchEntity.getGbDpbPurUserId());
                    gbDepartmentOrdersService.update(orders);
                } else {
                    unChoiceOrderList.add(orders);
                }
            }

            Integer newLength = nxDepartmentOrdersEntities.size() - unChoiceOrderList.size();
            gbPurGoods.setGbDpgOrdersAmount(newLength);
            gbPurGoods.setGbDpgBatchId(batchEntity.getGbDistributerPurchaseBatchId());
            gbPurGoods.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
            gbPurGoods.setGbDpgPurchaseDate(formatWhatDay(0));
            gbPurGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
            gbPurGoods.setGbDpgPurchaseYear(formatWhatYear(0));
            gbPurGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
            gbPurGoods.setGbDpgPurchaseWeek(getWeek(0));
            gbPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            gbPurGoods.setGbDpgPurchaseDepartmentId(batchEntity.getGbDpbPurDepartmentId());
            gbPurGoods.setGbDpgPurchaseNxDistributerId(batchEntity.getGbDpbDistributerId());
            gbPurGoods.setGbDpgTime(formatWhatTime(0));
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
                disGoods.setGbDpgPurchaseWeek(getWeek(0));
                disGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                disGoods.setGbDpgIsCheck(0);
                gbDPGService.save(disGoods);

                for (GbDepartmentOrdersEntity unChoiceOrder : unChoiceOrderList) {
                    Integer gbDistributerPurchaseGoodsId = disGoods.getGbDistributerPurchaseGoodsId();
                    unChoiceOrder.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                    gbDepartmentOrdersService.update(unChoiceOrder);
                }
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
