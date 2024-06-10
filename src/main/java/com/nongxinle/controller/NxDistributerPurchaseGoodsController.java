package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-24 11:45
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.poi.util.Internal;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxCommunityTypeUtils.*;
import static com.nongxinle.utils.NxCommunityTypeUtils.getNxRestrauntOrderStatusHasFinished;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/nxdistributerpurchasegoods")
public class NxDistributerPurchaseGoodsController {
    @Autowired
    private NxDistributerPurchaseGoodsService nxDisPurcGoodsService;

    @Autowired
    private NxDistributerPurchaseBatchService nxDPBService;

    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;

    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;

    @Autowired
    private NxDistributerGoodsService nxDgService;
    @Autowired
    private NxRestrauntOrdersService nxRestrauntOrdersService;
    @Autowired
    private NxDistributerWeightService nxDistributerWeightService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;


    @RequestMapping(value = "/deleteInputType/{id}")
    @ResponseBody
    public R deleteInputType(@PathVariable Integer id) {
        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDisPurcGoodsService.queryObject(id);
        purchaseGoodsEntity.setNxDpgInputType(null);
        nxDisPurcGoodsService.update(purchaseGoodsEntity);
        return R.ok();
    }


    @RequestMapping(value = "/savePurchaseGoodsInputTypeBundle", method = RequestMethod.POST)
    @ResponseBody
    public R savePurchaseGoodsInputTypeBundle(String ids, Integer type) {
        String[] arr = ids.split(",");
        for (String id : arr) {
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDisPurcGoodsService.queryObject(Integer.valueOf(id));
            purchaseGoodsEntity.setNxDpgInputType(type);
            nxDisPurcGoodsService.update(purchaseGoodsEntity);
        }

        return R.ok();
    }


    @RequestMapping(value = "/disGetToPrintPurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetToPrintPurGoods(Integer disId, Integer fatherId) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("fatherId", fatherId);
        map4.put("status", 1);
        map4.put("weightId", -1);
        System.out.println("map44444" + map4);
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);


        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("orderStatus", 3);
        map.put("purchaseType", 1);
        map.put("batchId", 0);
        map.put("weightId", 1);
        map.put("equalStatus", 1);
        System.out.println("priririiriririiri" + map);
        Integer printOrderCount = nxDisPurcGoodsService.queryPurOrderCount(map);

        Map<String, Object> mapWN = new HashMap<>();
        mapWN.put("disId", disId);
        mapWN.put("date", formatWhatDay(0));
        mapWN.put("type", 2);
        int count = nxDistributerWeightService.queryWeightCountByParams(mapWN);
        BigDecimal trade = new BigDecimal(count).add(new BigDecimal(1));
        String s = formatDayNumber(0) + "CGD" + trade;

        Map<String, Object> map1 = new HashMap<>();
        map1.put("arr", purchaseToday);
        map1.put("printOrderCount", printOrderCount);
        map1.put("tradeNo", s);

        return R.ok().put("data", map1);
    }


    @RequestMapping(value = "/cancleFinishPurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R cancleFinishPurGoods(@RequestBody NxDistributerPurchaseGoodsEntity purgoods) {
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getNxDepartmentOrdersEntities();

        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            orders.setNxDoStatus(getNxOrderStatusNew());
            orders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
            orders.setNxDoPurchaseUserId(-1);
            orders.setNxDoWeight(null);
            orders.setNxDoSubtotal(null);
            nxDepartmentOrdersService.update(orders);
        }
        purgoods.setNxDpgStatus(getGbPurchaseGoodsStatusNew());
        purgoods.setNxDpgBuyPrice(null);
        purgoods.setNxDpgBuyQuantity(null);
        purgoods.setNxDpgBuySubtotal(null);
        purgoods.setNxDpgStatus(0);
        purgoods.setNxDpgTime(null);
        purgoods.setNxDpgPurchaseDate(null);
        purgoods.setNxDpgPurchaseType(null);
        purgoods.setNxDpgPurUserId(null);
        purgoods.setNxDpgPurchaseDate(null);
        purgoods.setNxDpgTime(null);
        purgoods.setNxDpgBuyUserId(null);
        nxDisPurcGoodsService.update(purgoods);

        return R.ok();
    }

    @RequestMapping(value = "/disUserUpdateSelfPurGoodsOrdersCost", method = RequestMethod.POST)
    @ResponseBody
    public R disUserUpdateSelfPurGoodsOrdersCost(@RequestBody NxDistributerPurchaseGoodsEntity purGoods) {
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purGoods.getNxDepartmentOrdersEntities();
        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            nxDepartmentOrdersService.update(orders);
        }
        nxDisPurcGoodsService.update(purGoods);
        return R.ok();
    }

    @RequestMapping(value = "/disUserGetPurchaserDateBill", method = RequestMethod.POST)
    @ResponseBody
    public R disUserGetPurchaserDateBill(Integer userId, String date) {
        System.out.println("amma" + date);

        //购买采购商品batchId == -1
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("date", date);
        map.put("batchId", -1);
        map.put("payType", 0);
        System.out.println("dmapapap" + map);
        List<NxDistributerPurchaseGoodsEntity> goodsEntities = nxDisPurcGoodsService.queryPurchaseGoodsByParams(map);
        BigDecimal maileTotal = new BigDecimal(0);
        if (goodsEntities.size() > 0) {
            Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
            maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            maileTotal = new BigDecimal(0);
        }

        //现金订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("purUserId", userId);
        map1.put("date", date);
        map1.put("payType", 0);
        List<NxDistributerPurchaseBatchEntity> entitiesCash = nxDPBService.queryDisPurchaseBatch(map1);
        BigDecimal batchCashTotal = new BigDecimal(0);
        if (entitiesCash.size() > 0) {
            Double aDouble = nxDPBService.queryPurchaserCashTotal(map1);
            batchCashTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            batchCashTotal = new BigDecimal(0);
        }


        Map<String, Object> map2 = new HashMap<>();
        map2.put("purUserId", userId);
        map2.put("date", date);
        map2.put("payType", 1);
        List<NxDistributerPurchaseBatchEntity> entitiesBill = nxDPBService.queryDisPurchaseBatch(map2);
        BigDecimal batchBillTotal = new BigDecimal(0);
        if (entitiesBill.size() > 0) {
            Double aDouble = nxDPBService.queryPurchaserCashTotal(map2);
            batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            batchBillTotal = new BigDecimal(0);
        }

        BigDecimal add = maileTotal.add(batchCashTotal).add(batchBillTotal).setScale(2, BigDecimal.ROUND_HALF_UP);

        Map<String, Object> map123 = new HashMap<>();
        map123.put("total", add);
        map123.put("maileTotal", maileTotal);
        map123.put("batchCashTotal", batchCashTotal);
        map123.put("batchBillTotal", batchBillTotal);
        map123.put("maileArr", goodsEntities);
        map123.put("batchCashArr", entitiesCash);
        map123.put("batchBillArr", entitiesBill);

        return R.ok().put("data", map123);
    }


    @RequestMapping(value = "/disUserGetPurchasePurGoods/{userId}")
    @ResponseBody
    public R disUserGetPurchasePurGoods(@PathVariable Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("dayuStatus", 1);
        map.put("batchId", 0);
        List<NxDistributerPurchaseGoodsEntity> nxDistributerPurchaseGoodsEntities = nxDisPurcGoodsService.queryForDisGoods(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("month", formatWhatMonth(0));
        map1.put("arr", nxDistributerPurchaseGoodsEntities);


        List<Map<String, Object>> result = new ArrayList<>();
        result.add(map1);
//        result.add(map3);
//        result.add(map5);
        return R.ok().put("data", result);
    }


    /**
     * disUser
     *
     * @param searchStr
     * @param disId
     * @return
     */
    @RequestMapping(value = "/queryDisPurGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisPurGoodsByQuickSearch(String searchStr, String disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }

        List<NxDistributerGoodsEntity> distributerGoodsEntities = nxDgService.queryDisPurGoodsQuickSearchStr(map);

        return R.ok().put("data", distributerGoodsEntities);
    }



    @RequestMapping(value = "/purUserUpdatePurPrice", method = RequestMethod.POST)
    @ResponseBody
    public R purUserUpdatePurPrice(@RequestBody NxDistributerPurchaseGoodsEntity purgoods) {
        BigDecimal buyPrice = new BigDecimal(purgoods.getNxDpgBuyPrice());

        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getNxDepartmentOrdersEntities();
        if (nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                NxDepartmentOrdersEntity tableOrder = nxDepartmentOrdersService.queryObject(orders.getNxDepartmentOrdersId());
                tableOrder.setNxDoCostPriceUpdate(formatWhatDay(0));
                tableOrder.setNxDoCostPrice(purgoods.getNxDpgBuyPrice());
                if (tableOrder.getNxDoPrice() != null) {
                    //profit
                    BigDecimal nxDoPriceB = new BigDecimal(tableOrder.getNxDoPrice());
                    BigDecimal profitB = nxDoPriceB.subtract(buyPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal scaleB = profitB.divide(nxDoPriceB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                    tableOrder.setNxDoProfitScale(scaleB.toString());

                }
                if(tableOrder.getNxDoWeight() != null){
                    //cost
                    BigDecimal weightB = new BigDecimal(tableOrder.getNxDoWeight());
                    BigDecimal decimal = buyPrice.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                    tableOrder.setNxDoCostSubtotal(decimal.toString());
                    if(tableOrder.getNxDoPrice() != null){
                        BigDecimal nxDoPriceB = new BigDecimal(tableOrder.getNxDoPrice());
                        BigDecimal profitB = nxDoPriceB.subtract(buyPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal decimal1 = profitB.multiply(new BigDecimal(tableOrder.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                        tableOrder.setNxDoProfitSubtotal(decimal1.toString());
                    }
                }

                nxDepartmentOrdersService.update(tableOrder);

            }
        }

        nxDisPurcGoodsService.update(purgoods);

        updateDisGoodsPriceThree(purgoods);

        return R.ok();
    }
    @RequestMapping(value = "/disUserFinishPurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disUserFinishPurGoods(@RequestBody NxDistributerPurchaseGoodsEntity purgoods) {
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getNxDepartmentOrdersEntities();
        if (nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                orders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishPurchase());
                orders.setNxDoCostPriceUpdate(formatWhatDayString(0));
                nxDepartmentOrdersService.update(orders);
                if (orders.getNxDoNxRestrauntOrderId() != null && orders.getNxDoNxRestrauntOrderId() > 0) {
                    transUpdateNxRestrauntOrder(orders);
                }

                if (orders.getNxDoGbDepartmentOrderId() != null && orders.getNxDoGbDepartmentOrderId() > 0) {
                    transUpdateGbDepartmentOrder(orders);
                }
            }
        }
        purgoods.setNxDpgPurchaseDate(formatWhatDay(0));
        purgoods.setNxDpgBatchId(-1);
        purgoods.setNxDpgPayType(0);
        purgoods.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
        nxDisPurcGoodsService.update(purgoods);


        updateDisGoodsPriceThree(purgoods);

        NxDistributerGoodsEntity nxDistributerGoodsEntity = purgoods.getNxDistributerGoodsEntity();
        nxDistributerGoodsEntity.setNxDgBuyingPriceUpdate(formatWhatFullTime(0));
        nxDgService.update(nxDistributerGoodsEntity);

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


    @RequestMapping(value = "/deleteIsPurchase", method = RequestMethod.POST)
    @ResponseBody
    public R deleteIsPurchase(@RequestBody NxDistributerPurchaseGoodsEntity purgoods) {
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getNxDepartmentOrdersEntities();
        if (nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                orders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
                orders.setNxDoCostPrice(null);
                nxDepartmentOrdersService.update(orders);
                //更新restraunt订单数据
                deleteUpdateNxRestrauntOrderData(orders);
            }
        }
        purgoods.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
        purgoods.setNxDpgBuyPrice(null);
        nxDisPurcGoodsService.update(purgoods);
        return R.ok();
    }


    /**
     * jrdh app
     *
     * @param fatherId
     * @return
     */
    @RequestMapping(value = "/nxPurchaserGetPurchaseGoodsByFatherId/{fatherId}")
    @ResponseBody
    public R nxPurchaserGetPurchaseGoodsByFatherId(@PathVariable Integer fatherId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("fatherId", fatherId);
        map4.put("status", 1);
        map4.put("weightId", -1);
        map4.put("batchId", -1);
        map4.put("inputType", -1);
        System.out.println("map444" + map4);
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);
        int orderCount = 0;
        if (purchaseToday.size() > 0) {
            for (NxDistributerFatherGoodsEntity fatherGoodsEntity : purchaseToday) {
                Integer distributerFatherGoodsId = fatherGoodsEntity.getNxDistributerFatherGoodsId();
                map4.put("grandId", distributerFatherGoodsId);
                map4.put("purType", 1);
                Integer integer = nxDisPurcGoodsService.queryPurOrderCount(map4);
                fatherGoodsEntity.setPurOrderCount(integer);
                orderCount = orderCount + integer;
            }
        }
        return R.ok().put("data", purchaseToday);
    }


    @RequestMapping(value = "/getDisInputPurGoodsTx", method = RequestMethod.POST)
    @ResponseBody
    public R getDisInputPurGoodsTx(Integer disId, Integer type) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("status", 1);
        map4.put("batchId", -1);
        map4.put("equalInputType", type);
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);
        if (purchaseToday.size() > 0) {
            for (NxDistributerFatherGoodsEntity fatherGoodsEntity : purchaseToday) {
                Map<String, Object> mapF = new HashMap<>();
                mapF.put("grandId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
                mapF.put("status", 3);
                mapF.put("inputType", type);
                mapF.put("equalPurStatus", 1);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(mapF);
                fatherGoodsEntity.setNewOrderCount(integer);
            }
        }

        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("buyStatus", 3);
        map1.put("purType", -1);
        //新订单
        int newCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);
        // 出库
        map1.put("buyStatus", 5);
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
        mapOk.put("weight", 1);
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
        mapOk.put("inputType", 0);
        mapOk.put("batchId", -1);
        mapOk.put("weightStatusEqual", 1);
        int printCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

//************************************************************************************

        // map4 未发送或未打印
        map1.put("purType", 1);
        map1.put("inputType", type);
        map1.put("equalPurStatus", 1);
        System.out.println("map444undododo" + map1);
        System.out.println("type========" + type);
        Integer unDoCount = nxDepartmentOrdersService.queryDepOrdersAcount(map1);

        // map4 订货已发送
        map4.put("status", 2); //NX_DIS_PURCHASE_GOODS_IS_PURCHASE == 2 huifu
        map4.put("orderStatus", 3);
        map4.put("batchId", 1);
        Integer wxIsBatchCountUnReply = nxDisPurcGoodsService.queryPurOrderCount(map4);
        map4.put("status", 4);
        map4.put("dayuStatus", 1);
        Integer wxIsBatchCountHaveReply = nxDisPurcGoodsService.queryPurOrderCount(map4);


        //  map4 已打印
        map4.put("batchId", -1);
        map4.put("weightId", 1);
        map4.put("weightStatusEqual", 0);
        map4.put("orderStatus", 3);
        map4.put("status", 4);
        Integer isPrintCount = nxDisPurcGoodsService.queryPurOrderCount(map4);
        System.out.println("isprint444444" + map4);
        map4.put("weightStatusEqual", 1);
        Integer isPrintHaveWeightCount = nxDisPurcGoodsService.queryPurOrderCount(map4);


        Map<String, Object> map111 = new HashMap<>();

        map111.put("arr", purchaseToday);
        map111.put("newCount", newCount);
        map111.put("stockCount", stockCount);
        map111.put("stockCountOk", stockCountOK);
        map111.put("wxCount", wxCount);
        map111.put("wxCountAuto", wxCountAuto);
        map111.put("printCount", printCount);
        map111.put("wxCountOk", wxCountOk);
        map111.put("wxCountAutoOk", wxCountAutoOk);
        map111.put("printCountOk", printCountOk);

        map111.put("unDoCount", unDoCount);
        map111.put("isBatchCountUnRepaly", wxIsBatchCountUnReply);
        map111.put("isBatchCountHaveRepaly", wxIsBatchCountHaveReply);
        map111.put("isPrintCount", isPrintCount);
        map111.put("havePrintCount", isPrintHaveWeightCount);

        return R.ok().put("data", map111);
    }


    @RequestMapping(value = "/getDisInputPurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getDisInputPurGoods(Integer disId, Integer type) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("status", 1);
        map4.put("weightId", -1);
        map4.put("batchId", -1);
        map4.put("equalInputType", type);
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);

        //
        Integer wxUnBatchCount = nxDisPurcGoodsService.queryPurOrderCount(map4);

        // map4 订货已发送
        map4.put("status", 2); //NX_DIS_PURCHASE_GOODS_IS_PURCHASE == 2 huifu
        map4.put("orderStatus", 3);
        map4.put("batchId", 1);
        Integer wxIsBatchCountUnReply = nxDisPurcGoodsService.queryPurOrderCount(map4);
        map4.put("status", 4);
        map4.put("dayuStatus", 1);
        Integer wxIsBatchCountHaveReply = nxDisPurcGoodsService.queryPurOrderCount(map4);


        map4.put("batchId", -1);
        map4.put("weightId", 1);
        Integer wxIsPrintCount = nxDisPurcGoodsService.queryPurOrderCount(map4);
        map4.put("equalBuyStatus", 4);
        Integer wxIsPrintFinishCount = nxDisPurcGoodsService.queryPurOrderCount(map4);


        Map<String, Object> map41 = new HashMap<>();
        map41.put("disId", disId);
        map41.put("status", 1);
        map41.put("weightId", -1);
        map41.put("batchId", -1);
        map41.put("inputType", -1);
        System.out.println("map444aaa" + map4);
        Integer orderCount = nxDisPurcGoodsService.queryPurOrderCount(map41);


        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 4);
        map.put("equalInputType", 1);
        Integer wxOrderCount = nxDisPurcGoodsService.queryPurOrderCount(map);
        map.put("equalBuyStatus", 4);
        Integer wxOrderFinishCount = nxDisPurcGoodsService.queryPurOrderCount(map);

        Map<String, Object> mapP = new HashMap<>();
        mapP.put("disId", disId);
        mapP.put("orderStatus", 3);
        mapP.put("equalInputType", 0);
        Integer printOrderCount = nxDisPurcGoodsService.queryPurOrderCount(mapP);
        mapP.put("equalBuyStatus", 4);
        Integer printOrderFinishCount = nxDisPurcGoodsService.queryPurOrderCount(mapP);


        Map<String, Object> map111 = new HashMap<>();

        Map<String, Object> mapW = new HashMap<>();
        mapW.put("disId", disId);
        mapW.put("type", 2);
        int count = nxDistributerWeightService.queryWeightCountByParams(mapW);
        BigDecimal trade = new BigDecimal(count).add(new BigDecimal(1));
        String s = formatDayNumber(0) + "CGD" + trade;
        map111.put("arr", purchaseToday);
        map111.put("newOrderCount", orderCount);
        map111.put("wxOrderCount", wxOrderCount);
        map111.put("wxOrderFinishCount", wxOrderFinishCount);
        map111.put("printOrderCount", printOrderCount);
        map111.put("printOrderFinishCount", printOrderFinishCount);
        map111.put("wxUnBatchCount", wxUnBatchCount);
        map111.put("isBatchCountUnRepaly", wxIsBatchCountUnReply);
        map111.put("isBatchCountHaveRepaly", wxIsBatchCountHaveReply);
        map111.put("wxIsPrintCount", wxIsPrintCount);
        map111.put("wxIsPrintFinishCount", wxIsPrintFinishCount);
        map111.put("tradeNo", s);

        return R.ok().put("data", map111);
    }

    /**
     * jrdh app
     *
     * @param disId
     * @return
     */
    @RequestMapping(value = "/nxPurchaserGetPurchaseGoodsWithBatchCount/{disId}")
    @ResponseBody
    public R nxPurchaserGetPurchaseGoodsWithBatchCount(@PathVariable Integer disId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("status", 1);
        map4.put("weightId", -1);
        map4.put("batchId", -1);
        map4.put("inputType", -1);
        System.out.println("map444aaa" + map4);
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoodsGreat(map4);
        int orderCount = 0;
        if (purchaseToday.size() > 0) {
            for (NxDistributerFatherGoodsEntity greatGoodsEntity : purchaseToday) {
                int greatCount = 0;
                int goodsCount = 0;
                List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = greatGoodsEntity.getFatherGoodsEntities();
                if (fatherGoodsEntities.size() > 0) {
                    for (NxDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                        Integer distributerFatherGoodsId = fatherGoodsEntity.getNxDistributerFatherGoodsId();
                        map4.put("grandId", distributerFatherGoodsId);
                        map4.put("purType", 1);
                        System.out.println("abckckc" + map4);
                        Integer integer = nxDisPurcGoodsService.queryPurOrderCount(map4);
                        greatCount = greatCount + integer;
                        orderCount = orderCount + integer;
                        goodsCount = goodsCount + fatherGoodsEntity.getNxDistributerPurchaseGoodsEntities().size();
                    }

                }
                greatGoodsEntity.setPurOrderCount(greatCount);
                greatGoodsEntity.setStockOrderCount(goodsCount);
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("orderStatus", 3);
        map.put("equalInputType", 1);
        System.out.println("kdfjadfaklfaf" + map);
        Integer wxOrderCount = nxDisPurcGoodsService.queryPurOrderCount(map);
        map.put("equalBuyStatus", 4);
        Integer wxOrderFinishCount = nxDisPurcGoodsService.queryPurOrderCount(map);

        Map<String, Object> mapP = new HashMap<>();
        mapP.put("disId", disId);
        mapP.put("orderStatus", 3);
        mapP.put("equalInputType", 0);
        Integer printOrderCount = nxDisPurcGoodsService.queryPurOrderCount(mapP);
        mapP.put("equalBuyStatus", 4);
        Integer printOrderFinishCount = nxDisPurcGoodsService.queryPurOrderCount(mapP);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("arr", purchaseToday);
        map1.put("newOrderCount", orderCount);
        map1.put("wxOrderCount", wxOrderCount);
        map1.put("wxOrderFinishCount", wxOrderFinishCount);
        map1.put("printOrderCount", printOrderCount);
        map1.put("printOrderFinishCount", printOrderFinishCount);
        return R.ok().put("data", map1);
    }

//    @RequestMapping(value = "/jrdhGetPurchaseOrderGoodsWithBatchCount/{disId}")
//    @ResponseBody
//    public R jrdhGetPurchaseOrderGoodsWithBatchCount(@PathVariable Integer disId) {
//
//        Map<String, Object> map4 = new HashMap<>();
//        map4.put("disId", disId);
//        map4.put("status", 1);
//        map4.put("weightId", -1);
//        map4.put("purchaseType", 1);
//        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", disId);
//        map.put("orderStatus", 3);
//        map.put("purchaseType", 1);
//        map.put("equalStatus", 0);
//        map.put("weightId", -1);
//        map.put("batchId", -1);
//        System.out.println("purorder" + map);
//        Integer purOrderCount = nxDisPurcGoodsService.queryPurOrderCount(map);
//        map.put("batchId", 1);
//        Integer batchOrderCount = nxDisPurcGoodsService.queryPurOrderCount(map);
//
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("arr", purchaseToday);
//        map1.put("purOrderCount", purOrderCount);
//        map1.put("batchOrderCount", batchOrderCount);
//
//        return R.ok().put("data", map1);
//    }

    /**
     * DISTRIBUTE
     * 批发商获取进货商品列表
     *
     * @param disId 批发商id getHandPurchase
     * @return 进货商品列表
     */
    @RequestMapping(value = "/disGetPurchaseData/{disId}")
    @ResponseBody
    public R disGetPurchaseData(@PathVariable Integer disId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("fatherId", disId);
        map4.put("status", 1);
        map4.put("weightId", -1);
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);
        int orderCount = 0;
        if (purchaseToday.size() > 0) {
            for (NxDistributerFatherGoodsEntity fatherGoodsEntity : purchaseToday) {
                Integer distributerFatherGoodsId = fatherGoodsEntity.getNxDistributerFatherGoodsId();
                map4.put("grandId", distributerFatherGoodsId);
                map4.put("purType", 1);
                Integer integer = nxDisPurcGoodsService.queryPurOrderCount(map4);
                fatherGoodsEntity.setPurOrderCount(integer);
                orderCount = orderCount + integer;
            }
        }

        return R.ok().put("data", purchaseToday);
    }

    @RequestMapping(value = "/disGetIsPurchaseData/{disId}")
    @ResponseBody
    public R disGetIsPurchaseData(@PathVariable Integer disId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("equalStatus", getNxDepOrderBuyStatusIsPurchase());
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 2);
        int batchCount = nxDPBService.queryDisPurchaseBatchCount(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("purArr", purchaseToday);
        map1.put("batchCount", batchCount);
        return R.ok().put("data", map1);
    }


    /**
     * 供货商填写数量和单价
     * order 的buyStatus == 2
     *
     * @param purchaseGoodsEntity
     * @return
     */
    @RequestMapping(value = "/sellerSavePurGoodsWeight", method = RequestMethod.POST)
    @ResponseBody
    public R sellerSavePurGoodsWeight(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));

        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purchaseGoodsEntity.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            Integer nxDepartmentOrdersId = orders.getNxDepartmentOrdersId();
            NxDepartmentOrdersEntity tableOrderEntity = nxDepartmentOrdersService.queryObjectNew(nxDepartmentOrdersId);
            tableOrderEntity.setNxDoWeight(orders.getNxDoWeight());
            tableOrderEntity.setNxDoCostPrice(orders.getNxDoCostPrice());
            tableOrderEntity.setNxDoCostSubtotal(orders.getNxDoCostSubtotal());
            tableOrderEntity.setNxDoCostPriceUpdate(formatWhatDay(0));
            tableOrderEntity.setNxDoSubtotal(orders.getNxDoSubtotal());
            tableOrderEntity.setNxDoProfitSubtotal(orders.getNxDoProfitSubtotal());
            tableOrderEntity.setNxDoProfitScale(orders.getNxDoProfitScale());
            tableOrderEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusIsPurchase());
            nxDepartmentOrdersService.update(tableOrderEntity);
        }
        nxDisPurcGoodsService.update(purchaseGoodsEntity);
        return R.ok().put("data", purchaseGoodsEntity);
    }

    @RequestMapping(value = "/sellerSavePurGoodsPrice", method = RequestMethod.POST)
    @ResponseBody
    public R sellerSavePurGoodsPrice(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        Integer disGoodsId = purchaseGoodsEntity.getNxDpgDisGoodsId();
        String nxDpgBuyPrice = purchaseGoodsEntity.getNxDpgBuyPrice();

        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDgService.queryObject(disGoodsId);
        nxDistributerGoodsEntity.setNxDgBuyingPrice(nxDpgBuyPrice);
        nxDistributerGoodsEntity.setNxDgBuyingPriceUpdate(formatWhatDate(0));
        nxDgService.update(nxDistributerGoodsEntity);

//        //update PurGoods
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId",disGoodsId);
        map.put("status", 2);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);

        if(ordersEntities.size() > 0){
            for(NxDepartmentOrdersEntity ordersEntity: ordersEntities){
                BigDecimal orderWeight = new BigDecimal(ordersEntity.getNxDoWeight());

                if(ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().equals("0") && !ordersEntity.getNxDoPrice().equals("0.0")){
                    BigDecimal orderPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                    BigDecimal newSubtotal = orderWeight.multiply(orderPrice);
                    BigDecimal newCostPrice = new BigDecimal(nxDpgBuyPrice);
                    BigDecimal newScale = orderPrice.subtract(newCostPrice).divide(orderPrice, 2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal newCostSubtotal = orderWeight.multiply(newCostPrice);
                    BigDecimal newProfit = newSubtotal.subtract(newCostSubtotal);
                    ordersEntity.setNxDoCostSubtotal(newCostSubtotal.toString());
                    ordersEntity.setNxDoProfitSubtotal(newProfit.toString());
                    ordersEntity.setNxDoProfitScale(newScale.toString());
                }

                ordersEntity.setNxDoCostPrice(nxDpgBuyPrice);
                System.out.println("updateororroror");
                nxDepartmentOrdersService.update(ordersEntity);

            }
        }


        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purchaseGoodsEntity.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            Integer nxDepartmentOrdersId = orders.getNxDepartmentOrdersId();
            NxDepartmentOrdersEntity tableOrderEntity = nxDepartmentOrdersService.queryObjectNew(nxDepartmentOrdersId);
            tableOrderEntity.setNxDoWeight(orders.getNxDoWeight());
            tableOrderEntity.setNxDoCostPrice(orders.getNxDoCostPrice());
            tableOrderEntity.setNxDoCostSubtotal(orders.getNxDoCostSubtotal());
            tableOrderEntity.setNxDoCostPriceUpdate(formatWhatDay(0));
            tableOrderEntity.setNxDoSubtotal(orders.getNxDoSubtotal());
            tableOrderEntity.setNxDoProfitSubtotal(orders.getNxDoProfitSubtotal());
            tableOrderEntity.setNxDoProfitScale(orders.getNxDoProfitScale());
            tableOrderEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusIsPurchase());
            nxDepartmentOrdersService.update(tableOrderEntity);
        }
        purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
        nxDisPurcGoodsService.update(purchaseGoodsEntity);
        return R.ok().put("data", purchaseGoodsEntity);
    }


    @RequestMapping(value = "/disIsPurchaseGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disIsPurchaseGoods(@RequestBody NxDistributerPurchaseGoodsEntity purgoods) {
        System.out.println("disIsPurchaseGoodsdisIsPurchaseGoods");
        purgoods.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
        purgoods.setNxDpgPurchaseDate(formatWhatDay(0));
        purgoods.setNxDpgTime(formatWhatTime(0));
        nxDisPurcGoodsService.update(purgoods);
        NxDistributerGoodsEntity nxDistributerGoodsEntity = purgoods.getNxDistributerGoodsEntity();
        nxDgService.update(nxDistributerGoodsEntity);

        return R.ok();
    }


    @RequestMapping(value = "/savePlanPurchaseOrderBundle", method = RequestMethod.POST)
    @ResponseBody
    public R savePlanPurchaseOrderBundle(@RequestBody List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntityList) {

        for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntityList) {

            Integer nxDistributerPurchaseGoodsId = 0;
            //判断是否有已经分的
            Integer nxDpgDisGoodsId = purchaseGoodsEntity.getNxDpgDisGoodsId();
            Integer nxDpgInputType = purchaseGoodsEntity.getNxDpgInputType();
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", nxDpgDisGoodsId);
            map.put("equalInputType", nxDpgInputType);
            map.put("equalStatus", 0);
            NxDistributerPurchaseGoodsEntity havePurGoods = nxDisPurcGoodsService.queryIfHavePurGoods(map);
            if (havePurGoods != null) {
                BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgOrdersAmount());
                BigDecimal decimal1 = new BigDecimal(purchaseGoodsEntity.getNxDepartmentOrdersEntities().size());
                havePurGoods.setNxDpgOrdersAmount(Integer.valueOf(decimal.add(decimal1).toString()));
                nxDisPurcGoodsService.update(havePurGoods);
                nxDistributerPurchaseGoodsId = havePurGoods.getNxDistributerPurchaseGoodsId();

            } else {
                purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
                purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
                purchaseGoodsEntity.setNxDpgOrdersAmount(purchaseGoodsEntity.getNxDepartmentOrdersEntities().size());
                purchaseGoodsEntity.setNxDpgFinishAmount(0);
                if(purchaseGoodsEntity.getNxDpgInputType() == 0){
                    purchaseGoodsEntity.setNxDpgBuyPrice(purchaseGoodsEntity.getNxDpgExpectPrice());
                }
                nxDisPurcGoodsService.save(purchaseGoodsEntity);

                nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();

                NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDgService.queryObject(nxDpgDisGoodsId);
                nxDistributerGoodsEntity.setNxDgPurchaseAuto(nxDpgInputType);
                nxDgService.update(nxDistributerGoodsEntity);

            }

            List<NxDepartmentOrdersEntity> ordersEntities = purchaseGoodsEntity.getNxDepartmentOrdersEntities();
            if (ordersEntities.size() > 0) {
                for (NxDepartmentOrdersEntity order : ordersEntities) {
                    if(order.getPurSelected()){
                        Integer nxDepartmentOrdersId = order.getNxDepartmentOrdersId();
                        NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObjectNew(nxDepartmentOrdersId);
                        ordersEntity1.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
                        ordersEntity1.setNxDoGoodsType(nxDpgInputType);
                        nxDepartmentOrdersService.update(ordersEntity1);
                    }
                }
            }
        }

        return R.ok();
    }

    @RequestMapping(value = "/deletePlanPurchase", method = RequestMethod.POST)
    @ResponseBody
    public R deletePlanPurchase(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purchaseGoodsEntity.getNxDepartmentOrdersEntities();
        if (nxDepartmentOrdersEntities != null && nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orders.getNxDepartmentOrdersId());
                ordersEntity.setNxDoPurchaseGoodsId(-1);
                ordersEntity.setNxDoGoodsType(-1);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

//        if (purchaseGoodsEntity.getNxDpgStatus().equals(getNxDisPurchaseGoodsWithBatch())) {
//            List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurcGoodsService.queryPurchaseGoodsByBatchId(purchaseGoodsEntity.getNxDpgBatchId());
//            if (purchaseGoodsEntities.size() == 1) {
//                nxDPBService.delete(purchaseGoodsEntity.getNxDpgBatchId());
//            }
//        }

        nxDisPurcGoodsService.delete(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());

        Integer disGoodsId = purchaseGoodsEntity.getNxDpgDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDgService.queryObject(disGoodsId);
        nxDistributerGoodsEntity.setNxDgPurchaseAuto(-1);
        nxDgService.update(nxDistributerGoodsEntity);

        return R.ok();
    }

    @RequestMapping(value = "/updatePurchaseGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updatePurchaseGoods(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {

        nxDisPurcGoodsService.update(purchaseGoodsEntity);
        NxDistributerGoodsEntity nxDistributerGoodsEntity = purchaseGoodsEntity.getNxDistributerGoodsEntity();
        nxDgService.update(nxDistributerGoodsEntity);
        return R.ok().put("data", purchaseGoodsEntity);

    }

    /**
     * 修改进货商
     *
     * @param purchaseGoodsEntity 进货商品
     * @return 进货商品
     */
//    @RequestMapping(value = "/updatePlanPurchase", method = RequestMethod.POST)
//    @ResponseBody
//    public R updatePlanPurchase(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
//
//        Integer nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
//        List<NxDepartmentOrdersEntity> ordersEntities = purchaseGoodsEntity.getNxDepartmentOrdersEntities();
//        if (ordersEntities != null && ordersEntities.size() > 0) {
//            for (NxDepartmentOrdersEntity order : ordersEntities) {
//                order.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
//                order.setNxDoPurchaseStatus(1);
//                nxDepartmentOrdersService.update(order);
//            }
//            purchaseGoodsEntity.setNxDpgOrdersAmount(purchaseGoodsEntity.getNxDpgOrdersAmount() + ordersEntities.size());
//        }
//        nxDisPurcGoodsService.update(purchaseGoodsEntity);
//        return R.ok().put("data", purchaseGoodsEntity);
//
//    }

    @RequestMapping(value = "/savePlanPurchaseGoods", method = RequestMethod.POST)
    @ResponseBody
    public R savePlanPurchaseGoods(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {

        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purchaseGoodsEntity.getNxDepartmentOrdersEntities();
        if (nxDepartmentOrdersEntities != null && nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : nxDepartmentOrdersEntities) {
                if (ordersEntity.getPurSelected()) {
                    ordersEntity.setNxDoPurchaseGoodsId(-1);
                    ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishPurchase());
                    ordersEntity.setNxDoCostPriceLevel(purchaseGoodsEntity.getNxDpgCostLevel().toString());
                    ordersEntity.setNxDoCostPriceUpdate(formatWhatDay(0));
                    nxDepartmentOrdersService.update(ordersEntity);
                }
            }
        }
        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
        purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
        purchaseGoodsEntity.setNxDpgFinishAmount(0);
        nxDisPurcGoodsService.save(purchaseGoodsEntity);
        return R.ok().put("data", purchaseGoodsEntity);
    }




    /**
     * txs
     * 老板添加进货商品
     * order的buyStatus == 1
     *
     * @param purchaseGoodsEntity 批发商商品
     * @return ok
     */
    @RequestMapping(value = "/savePlanPurchaseOrder", method = RequestMethod.POST)
    @ResponseBody
    public R savePlanPurchaseOrder(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {

        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
        purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
        purchaseGoodsEntity.setNxDpgOrdersAmount(purchaseGoodsEntity.getNxDepartmentOrdersEntities().size());
        purchaseGoodsEntity.setNxDpgFinishAmount(0);
        nxDisPurcGoodsService.save(purchaseGoodsEntity);

        Integer nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
        List<NxDepartmentOrdersEntity> ordersEntities = purchaseGoodsEntity.getNxDepartmentOrdersEntities();
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity order : ordersEntities) {
                Integer nxDepartmentOrdersId = order.getNxDepartmentOrdersId();
                NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObject(nxDepartmentOrdersId);
                ordersEntity1.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
                ordersEntity1.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
                ordersEntity1.setNxDoCostPriceUpdate(formatWhatDayString(0));
                System.out.println("purododdododo" + formatWhatDayString(0));
                ordersEntity1.setNxDoCostPriceLevel(order.getNxDoCostPriceLevel());
                ordersEntity1.setNxDoCostPrice(order.getNxDoCostPrice());
                nxDepartmentOrdersService.update(ordersEntity1);
                if (order.getNxDoDepartmentId() == -1 && order.getNxDoGbDepartmentFatherId() > 0) {
                    GbDepartmentOrdersEntity gbOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(order.getNxDepartmentOrdersId());
                    gbOrdersEntity.setGbDoBuyStatus(1);
                    gbDepartmentOrdersService.update(gbOrdersEntity);
                }
                if (order.getNxDoDepartmentId() == -1 && order.getNxDoNxCommRestrauntFatherId() > 0) {
                    NxRestrauntOrdersEntity resOrdersEntity = nxRestrauntOrdersService.queryNxRestrauntOrderByNxOrderId(order.getNxDepartmentOrdersId());
                    resOrdersEntity.setNxRoBuyStatus(1);
                    nxRestrauntOrdersService.update(resOrdersEntity);
                }
            }
        }

        return R.ok().put("data", purchaseGoodsEntity);
    }

    private void updateDisGoodsPriceThree(NxDistributerPurchaseGoodsEntity purgoods) {
        Integer nxDpgDisGoodsId = purgoods.getNxDpgDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDgService.queryObject(nxDpgDisGoodsId);
        String nxDpgBuyPrice = purgoods.getNxDpgBuyPrice();
        if (nxDistributerGoodsEntity.getNxDgBuyingPriceIsGrade() == 0) {
            nxDistributerGoodsEntity.setNxDgBuyingPrice(nxDpgBuyPrice);
            nxDistributerGoodsEntity.setNxDgBuyingPriceUpdate(formatWhatDay(0));
            nxDgService.update(nxDistributerGoodsEntity);
        }
        if (nxDistributerGoodsEntity.getNxDgBuyingPriceIsGrade() == 1) {
            Integer level = purgoods.getNxDpgCostLevel();
            if (level == 1) {
                nxDistributerGoodsEntity.setNxDgBuyingPriceOne(nxDpgBuyPrice);
                nxDistributerGoodsEntity.setNxDgBuyingPriceOneUpdate(formatWhatDayString(0));
                nxDgService.update(nxDistributerGoodsEntity);
            }
            if (level == 2) {
                nxDistributerGoodsEntity.setNxDgBuyingPriceTwo(nxDpgBuyPrice);
                nxDistributerGoodsEntity.setNxDgBuyingPriceTwoUpdate(formatWhatDayString(0));
                nxDgService.update(nxDistributerGoodsEntity);
            }
            if (level == 3) {
                nxDistributerGoodsEntity.setNxDgBuyingPriceThree(nxDpgBuyPrice);
                nxDistributerGoodsEntity.setNxDgBuyingPriceThreeUpdate(formatWhatDayString(0));
                nxDgService.update(nxDistributerGoodsEntity);
            }

        }
//        System.out.println("updatethreepricieieiieieiieeieiei");
//        Integer nxDpgDisGoodsId = purgoods.getNxDpgDisGoodsId();
//        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDgService.queryObject(nxDpgDisGoodsId);
//        String nxDpgBuyPrice = purgoods.getNxDpgBuyPrice();
//        String nxDgPriceProfitOne = nxDistributerGoodsEntity.getNxDgPriceProfitOne();
//        String nxDgPriceProfitTwo = nxDistributerGoodsEntity.getNxDgPriceProfitTwo();
//        String nxDgPriceProfitThree = nxDistributerGoodsEntity.getNxDgPriceProfitThree();
//
//        BigDecimal subtractOne = new BigDecimal(100).subtract(new BigDecimal(nxDgPriceProfitOne));
//        BigDecimal nxDgPriceProfitOnePrice = new BigDecimal(nxDpgBuyPrice).divide(subtractOne, 3, BigDecimal.ROUND_HALF_UP)
//                .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//
//        BigDecimal subtractTwo = new BigDecimal(100).subtract(new BigDecimal(nxDgPriceProfitTwo));
//        BigDecimal nxDgPriceProfitTwoPrice = new BigDecimal(nxDpgBuyPrice).divide(subtractTwo, 3, BigDecimal.ROUND_HALF_UP)
//                .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//        BigDecimal subtractThree = new BigDecimal(100).subtract(new BigDecimal(nxDgPriceProfitThree));
//        BigDecimal nxDgPriceProfitThreePrice = new BigDecimal(nxDpgBuyPrice).divide(subtractThree, 3, BigDecimal.ROUND_HALF_UP)
//                .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//        nxDistributerGoodsEntity.setNxDgBuyingPrice(nxDpgBuyPrice);
//        nxDistributerGoodsEntity.setNxDgBuyingPriceUpdate(formatFullTime());
//        nxDistributerGoodsEntity.setNxDgPriceProfitOnePrice(nxDgPriceProfitOnePrice.toString());
//        nxDistributerGoodsEntity.setNxDgPriceProfitTwoPrice(nxDgPriceProfitTwoPrice.toString());
//        nxDistributerGoodsEntity.setNxDgPriceProfitThreePrice(nxDgPriceProfitThreePrice.toString());
//
//        nxDgService.update(nxDistributerGoodsEntity);

    }

    private void deleteUpdateNxRestrauntOrderData(NxDepartmentOrdersEntity orders) {
        Integer nxDoNxCommRestrauntId = orders.getNxDoNxCommRestrauntId();
        if (nxDoNxCommRestrauntId != -1) {
            NxRestrauntOrdersEntity resOrdersEntity = nxRestrauntOrdersService.queryObject(orders.getNxDoNxRestrauntOrderId());
            resOrdersEntity.setNxRoWeight(null);
            resOrdersEntity.setNxRoBuyStatus(getNxDepOrderBuyStatusUnPurchase());
            resOrdersEntity.setNxRoCostPrice(null);
            resOrdersEntity.setNxRoCostSubtotal(null);
            nxRestrauntOrdersService.update(resOrdersEntity);
        }
    }
//
//    private void updateNxRestrauntOrderData(NxDepartmentOrdersEntity orders) {
//        Integer nxDoNxCommRestrauntId = orders.getNxDoNxCommRestrauntId();
//        if (nxDoNxCommRestrauntId != -1) {
//            NxRestrauntOrdersEntity resOrdersEntity = nxRestrauntOrdersService.queryObject(orders.getNxDoNxRestrauntOrderId());
//            resOrdersEntity.setNxRoWeight(orders.getNxDoWeight());
//            resOrdersEntity.setNxRoBuyStatus(getNxDepOrderBuyStatusFinishPurchase());
//            String nxRoPrice = resOrdersEntity.getNxRoPrice();
//            BigDecimal decimal = new BigDecimal(nxRoPrice).multiply(new BigDecimal(orders.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
//            resOrdersEntity.setNxRoSubtotal(decimal.toString());
//            BigDecimal decimal1 = new BigDecimal(resOrdersEntity.getNxRoCostPrice()).multiply(new BigDecimal(orders.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
//            resOrdersEntity.setNxRoCostSubtotal(decimal1.toString());
//            nxRestrauntOrdersService.update(resOrdersEntity);
//
//        }
//    }


    private Map<String, Object> disGetHaveFinishedPurGoods(Integer disId, Integer which) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("finishDate", formatWhatDay(which));
        List<NxDistributerPurchaseGoodsEntity> distributerPurchaseGoodsEntities = nxDisPurcGoodsService.queryPurchaseGoodsWithDetailByParams(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("date", formatWhatDay(which));
        map1.put("arr", distributerPurchaseGoodsEntities);
        return map1;
    }

    /**
     * DISTRIBUTE
     * 批发商获取进货商品列表
     *
     * @param disId 批发商id
     * @return 进货商品列表
     */
//    @RequestMapping(value = "/getPurchaseGoods/{disId}")
//    @ResponseBody
//    public R getPurchaseGoods(@PathVariable Integer disId) {
//
//        Map<String, Object> map4 = new HashMap<>();
//        map4.put("disId", disId);
//        map4.put("status", 1);
//        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);
//        return R.ok().put("data", purchaseToday);
//    }


//    @ResponseBody
//    @RequestMapping("/sellerUpdatePurGoods")
//    public R sellerUpdatePurGoods(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
//        nxDisPurcGoodsService.update(purchaseGoodsEntity);
//        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purchaseGoodsEntity.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
//        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
//            nxDepartmentOrdersService.update(orders);
//        }
//        return R.ok().put("data", purchaseGoodsEntity);
//    }

//    @RequestMapping(value = "/disGoodsGetPurchaseGoods/{disGoodsId}")
//    @ResponseBody
//    public R disGoodsGetPurchaseGoods(@PathVariable Integer disGoodsId) {
//        Map<String, Object> map2 = new HashMap<>();
//        map2.put("disGoodsId", disGoodsId);
//        List<NxDistributerPurchaseGoodsEntity> entities = nxDisPurcGoodsService.queryPurchaseGoodsByParams(map2);
//        return R.ok().put("data", entities);
//    }


//    @RequestMapping(value = "/getDatePurchaseGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R getDatePurchaseGoods(Integer disId, String date) {
//        System.out.println(date);
//        Map<String, Object> map = new HashMap<>();
//
//        map.put("disId", disId);
//        map.put("purchaseDate", date);
//        List<NxDistributerFatherGoodsEntity> purchase = nxDisPurcGoodsService.queryDisPurchaseGoods(map);
//
//        return R.ok().put("data", purchase);
//    }

//    @RequestMapping(value = "/finishPurAutoGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R finishPurAutoGoods(@RequestBody NxDistributerPurchaseGoodsEntity purgoods) {
//        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getNxDepartmentOrdersEntities();
//        int size = nxDepartmentOrdersEntities.size();
//        int changeCount = 0;
//        if (size > 0) {
//            for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
//                if (orders.getNxDoWeight() != null && orders.getNxDoWeight().length() > 0) {
//                    orders.setNxDoStatus(1);
//                    orders.setNxDoPurchaseStatus(1);
//                    orders.setNxDoPurchaseGoodsId(-1);
//                    nxDepartmentOrdersService.update(orders);
//                    changeCount = changeCount + 1;
//                }
//            }
//            if (size == changeCount) {
//                purgoods.setNxDpgPurchaseType(0); // dis完成采购，采购方式==0
//                purgoods.setNxDpgStatus(3);
//                purgoods.setNxDpgTime(formatWhatTime(0));
//                purgoods.setNxDpgPurchaseDate(formatWhatDay(0));
//                nxDisPurcGoodsService.update(purgoods);
//            } else {
//                purgoods.setNxDpgOrdersAmount(size - changeCount);
//                nxDisPurcGoodsService.update(purgoods);
//            }
//        }
//        return R.ok();
//    }


//    @RequestMapping(value = "/disGetHaveFinishedPurGoods/{disId}")
//    @ResponseBody
//    public R disGetHaveFinishedPurGoods(@PathVariable Integer disId) {
//        Map<String, Object> stringObjectMap = disGetHaveFinishedPurGoods(disId, 0);
//        Map<String, Object> stringObjectMap1 = disGetHaveFinishedPurGoods(disId, -1);
//        Map<String, Object> stringObjectMap2 = disGetHaveFinishedPurGoods(disId, -2);
//        List<Map<String, Object>> result = new ArrayList<>();
//        result.add(stringObjectMap);
//        result.add(stringObjectMap1);
//        result.add(stringObjectMap2);
//        return R.ok().put("data", result);
//    }


//    @RequestMapping(value = "/queryDisPurGoodsDetail/{goodsId}")
//    @ResponseBody
//    public R queryDisPurGoodsDetail(@PathVariable Integer goodsId) {
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("goodsId", goodsId);
//        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurcGoodsService.queryPurchaseGoodsWithDetailByParams(map);
//        return R.ok().put("data", purchaseGoodsEntities);
//    }

}
