package com.nongxinle.controller;

/**
 * @author lpy
 * @date 12-01 07:19
 */

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxCommunityTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusUnPurchase;


@RestController
@RequestMapping("api/nxrestrauntorders")

public class NxRestrauntOrdersController {
    @Autowired
    private NxRestrauntOrdersService nxRestrauntOrdersService;
    @Autowired
    private NxRestrauntService nxRestrauntService;
    @Autowired
    private NxCommunityFatherGoodsService nxCommunityFatherGoodsService;
    @Autowired
    private NxCommunityGoodsService cgService;
    @Autowired
    private NxCommunityPurchaseBatchService nxCPBatchService;
    @Autowired
    private NxRestrauntBillService nxRestrauntBillService;
    @Autowired
    private NxRestrauntComGoodsService nxRestrauntComGoodsService;

    @Autowired
    private NxResComGoodsDailyService nxResComGoodsDailyService;
    @Autowired
    private NxResComGoodsWeekService nxResComGoodsWeekService;
    @Autowired
    private NxResComGoodsMonthService nxResComGoodsMonthService;
    @Autowired
    private NxCommunityPurchaseGoodsService nxComPurchaseGoodsService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxCommunitySupplierService nxCommunitySupplierService;
    @Autowired
    private NxCommunityPurchaseBatchService nxComPurchaseBatchService;

//    private static String freeZero = "49.0";
//    private static String freeOne = "99.0";
//    private static String freeTwo = "99.0";




    @RequestMapping(value = "/comGetDateCustomerOrder", method = RequestMethod.POST)
    @ResponseBody
    public R comGetDateCustomerOrder(Integer comId, String deliveryDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("comId", comId);
        map.put("date", deliveryDate);
        List<NxRestrauntBillEntity> billEntities = nxRestrauntBillService.queryResDailyBillWithOrders(map);
        return R.ok().put("data", billEntities);
    }


    @RequestMapping(value = "/disGetAccountResOrders/{id}")
    @ResponseBody
    public R disGetAccountResOrders(@PathVariable Integer id) {
        System.out.println(id + "diidiidiididi");
        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", id);
        map4.put("weight", 0);
        map4.put("equalBuyStatus", 0);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = nxRestrauntOrdersService.queryDistributerGoodsWithResOrdersByParams(map4);
        return R.ok().put("data", distributerGoodsEntities);
    }

    @RequestMapping(value = "/disGetTransferBatch/{disId}")
    @ResponseBody
    public R disGetTransferBatch(@PathVariable Integer disId) {
        System.out.println(disId + "diidididiidi");

        Map<String, Object> map2 = new HashMap<>();
        map2.put("disId", disId);
        map2.put("equalBatchStatus", 0);
        List<NxCommunityPurchaseBatchEntity> batchEntities = nxCPBatchService.queryComPurchaseBatchByParams(map2);

        return R.ok().put("data", batchEntities);
    }


    @RequestMapping(value = "/disGetTodayCommunity/{disId}")
    @ResponseBody
    public R disGetTodayCommunity(@PathVariable Integer disId) {
        //今天平台
        Map<String, Object> map = new HashMap<>();

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("equalBuyStatus", 0);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = nxRestrauntOrdersService.queryDistributerGoodsWithResOrdersByParams(map4);

        if (distributerGoodsEntities.size() > 0) {
            map.put("comArr", distributerGoodsEntities);
        } else {
            map.put("comArr", new ArrayList<>());
        }
        //purchase
        Map<String, Object> map2 = new HashMap<>();
        map2.put("disId", disId);
        map2.put("equalBatchStatus", 0);
        List<NxCommunityPurchaseBatchEntity> batchEntities = nxCPBatchService.queryComPurchaseBatchByParams(map2);
        if (batchEntities.size() > 0) {
            map.put("batchArr", batchEntities);
        } else {
            map.put("batchArr", new ArrayList<>());
        }

        return R.ok().put("data", map);

    }


    @RequestMapping(value = "/getResToPrintResOrders/{id}")
    @ResponseBody
    public R getResToPrintResOrders(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", id);
        map.put("status", 4);
        map.put("dayuStatus", 1);
        map.put("printTimes", 0);
        List<NxRestrauntOrdersEntity> ordersEntities = nxRestrauntOrdersService.queryResOrdersByParams(map);
        return R.ok().put("data", ordersEntities);
    }


    @RequestMapping(value = "/comSaveOrderPrice", method = RequestMethod.POST)
    @ResponseBody
    public R comSaveOrderPrice(@RequestBody List<NxRestrauntOrdersEntity> orders) {
        for (NxRestrauntOrdersEntity order : orders) {
            nxRestrauntOrdersService.update(order);
        }
        return R.ok();
    }

    @RequestMapping(value = "/comGetResOrdersToFill/{id}")
    @ResponseBody
    public R comGetResOrdersToFill(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", id);
        map.put("status", 7);
        map.put("printTimes", 0);
        return R.ok().put("data", nxRestrauntOrdersService.queryResOrdersByParams(map));
    }

    @RequestMapping(value = "/comGetAccountResOrdersToPrint/{id}")
    @ResponseBody
    public R comGetAccountResOrdersToPrint(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", id);
        map.put("dayuStatus", 1);
        map.put("status", 6);
        map.put("printTimes", 0);
        return R.ok().put("data", nxRestrauntOrdersService.queryResOrdersByParams(map));
    }


    @RequestMapping(value = "/resManGetResOrders/{id}")
    @ResponseBody
    public R resManGetResOrders(@PathVariable String id) {
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", id);
        map.put("status", 5);
//        map.put("printTimes", 0);
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = nxRestrauntOrdersService.queryResWithSubDepsOrder(map);
        return R.ok().put("data", fatherGoodsEntities);
    }


    @RequestMapping(value = "/chainResManGetResOrders/{resFatherId}")
    @ResponseBody
    public R chainResManGetResOrders(@PathVariable Integer resFatherId) {
        NxRestrauntEntity restrauntEntity = nxRestrauntService.queryResInfo(resFatherId);
        List<Integer> ids = new ArrayList<>();
        for (NxRestrauntEntity res : restrauntEntity.getNxRestrauntEntities()) {
            Integer nxRestrauntId = res.getNxRestrauntId();
            ids.add(nxRestrauntId);
        }
        System.out.println(ids);

        Map<String, Object> map = new HashMap<>();
        map.put("resIds", ids);
        map.put("status", 6);
        List<NxRestrauntOrdersEntity> ordersEntities = nxRestrauntOrdersService.queryResChainOrdersByParams(map);
        return R.ok().put("data", ordersEntities);
    }

    //todo
    //作为餐馆的专业版统计出现收货按钮
    @RequestMapping(value = "/receiveOrderApply/{id}")
    @ResponseBody
    public R receiveOrderApply(@PathVariable Integer id) {
        NxRestrauntOrdersEntity ordersEntity = nxRestrauntOrdersService.queryObject(id);
        ordersEntity.setNxRoStatus(6);
        System.out.println(ordersEntity.getNxRoStatus() + "statututuususus======");
        nxRestrauntOrdersService.update(ordersEntity);

        //add statics
        Integer nxRoRestrauntId = ordersEntity.getNxRoRestrauntId();
        Integer nxRoRestrauntFatherId = ordersEntity.getNxRoRestrauntFatherId();
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", nxRoRestrauntFatherId);
        map.put("resId", nxRoRestrauntId);
        map.put("date", formatWhatDay(0));
        NxResComGoodsDailyEntity daily = nxResComGoodsDailyService.queryDailyGoodsByParams(map);
        if (daily == null) {
            NxResComGoodsDailyEntity dailyEntity = new NxResComGoodsDailyEntity();
            dailyEntity.setNxRcgdDate(formatWhatDay(0));
            dailyEntity.setNxRcgdRestrauntFatherId(nxRoRestrauntFatherId);
            dailyEntity.setNxRcgdRestrauntId(nxRoRestrauntId);
            dailyEntity.setNxRcgdComGoodsId(ordersEntity.getNxRoComGoodsId());
            dailyEntity.setNxRcgdWeight(ordersEntity.getNxRoWeight());
            dailyEntity.setNxRcgdSubtotal(ordersEntity.getNxRoSubtotal());
            nxResComGoodsDailyService.save(dailyEntity);
        } else {
            daily.setNxRcgdSubtotal((new BigDecimal(daily.getNxRcgdSubtotal()).add(new BigDecimal(ordersEntity.getNxRoSubtotal()))).toString());
            daily.setNxRcgdWeight((new BigDecimal(daily.getNxRcgdWeight()).add(new BigDecimal(ordersEntity.getNxRoWeight()))).toString());
            nxResComGoodsDailyService.update(daily);
        }
        // week
        Map<String, Object> map2 = new HashMap<>();
        map2.put("resFatherId", nxRoRestrauntFatherId);
        map2.put("resId", nxRoRestrauntId);
        map2.put("week", getWeekOfYear(0));
        NxResComGoodsWeekEntity week = nxResComGoodsWeekService.queryWeekGoodsByParams(map2);
        if (week == null) {
            NxResComGoodsWeekEntity weekEntity = new NxResComGoodsWeekEntity();
            weekEntity.setNxRcgwWeek(getWeekOfYear(0).toString());
            weekEntity.setNxRcgwRestrauntFatherId(nxRoRestrauntFatherId);
            weekEntity.setNxRcgwRestrauntId(nxRoRestrauntId);
            weekEntity.setNxRcgwComGoodsId(ordersEntity.getNxRoComGoodsId());
            weekEntity.setNxRcgwWeight(ordersEntity.getNxRoWeight());
            weekEntity.setNxRcgwSubtotal(ordersEntity.getNxRoSubtotal());
            nxResComGoodsWeekService.save(weekEntity);
        } else {
            week.setNxRcgwSubtotal((new BigDecimal(week.getNxRcgwSubtotal()).add(new BigDecimal(ordersEntity.getNxRoSubtotal()))).toString());
            week.setNxRcgwWeight((new BigDecimal(week.getNxRcgwWeight()).add(new BigDecimal(ordersEntity.getNxRoWeight()))).toString());
            nxResComGoodsWeekService.update(week);
        }

        //month
        Map<String, Object> map3 = new HashMap<>();
        map3.put("resFatherId", nxRoRestrauntFatherId);
        map3.put("resId", nxRoRestrauntId);
        map3.put("month", formatWhatMonth(0));
        NxResComGoodsMonthEntity month = nxResComGoodsMonthService.queryMonthGoodsByParams(map3);
        if (month == null) {
            NxResComGoodsMonthEntity monthEntity = new NxResComGoodsMonthEntity();
            monthEntity.setNxRcgmMonth(formatWhatMonth(0));
            monthEntity.setNxRcgmRestrauntFatherId(nxRoRestrauntFatherId);
            monthEntity.setNxRcgmRestrauntId(nxRoRestrauntId);
            monthEntity.setNxRcgmComGoodsId(ordersEntity.getNxRoComGoodsId());
            monthEntity.setNxRcgmWeight(ordersEntity.getNxRoWeight());
            monthEntity.setNxRcgmSubtotal(ordersEntity.getNxRoSubtotal());
            nxResComGoodsMonthService.save(monthEntity);
        } else {
            month.setNxRcgmSubtotal((new BigDecimal(month.getNxRcgmSubtotal()).add(new BigDecimal(ordersEntity.getNxRoSubtotal()))).toString());
            month.setNxRcgmWeight((new BigDecimal(month.getNxRcgmWeight()).add(new BigDecimal(ordersEntity.getNxRoWeight()))).toString());
            nxResComGoodsMonthService.update(month);
        }

        return R.ok();
    }


    @RequestMapping(value = "/orderGetResApply/{resId}")
    @ResponseBody
    public R orderGetResApply(@PathVariable Integer resId) {

        //今天的数据
        Map<String, Object> map = new HashMap<>();
        map.put("resId", resId);
        map.put("orderBy", "time");
        map.put("status", 6);

        return R.ok().put("data", nxRestrauntOrdersService.queryResOrdersByParams(map));
    }


    @RequestMapping(value = "/changeOrderToUnWeight/{orderid}")
    @ResponseBody
    public R changeOrderToUnWeight(@PathVariable Integer orderid) {
        NxRestrauntOrdersEntity ordersEntity = nxRestrauntOrdersService.queryObject(orderid);
        ordersEntity.setNxRoStatus(0);
        ordersEntity.setNxRoWeight("-1");
        ordersEntity.setNxRoSubtotal("-1");
        ordersEntity.setNxRoCostSubtotal("-1");
        nxRestrauntOrdersService.update(ordersEntity);
        return R.ok();
    }


    /**
     * community
     * 区域商获取正在进货商品
     *
     * @param comId 区域id
     * @return 进货批次
     */

    /**
     * community
     * 区域商获取未称重商品
     *
     * @param comId 区域商id
     * @return 区域商品
     */
    @RequestMapping(value = "/comGetToWeighCustomerList/{comId}")
    @ResponseBody
    public R comGetToWeighCustomerList(@PathVariable Integer comId) {
        Map<String, Object> map = new HashMap<>();
        map.put("comId", comId);
        map.put("status", 2);
//        map.put("equalBuyStatus", 0);

        TreeSet<NxRestrauntEntity> restrauntEntities = nxRestrauntOrdersService.queryTodayComRestrauant(map);

        return R.ok().put("data", restrauntEntities);
    }

    /**
     * community
     * 区域商获取未称重商品
     *
     * @param comId 区域商id
     * @return 区域商品
     */
    @RequestMapping(value = "/comGetToWeighGoodsList/{comId}")
    @ResponseBody
    public R comGetToWeighGoodsList(@PathVariable Integer comId) {
        Map<String, Object> map = new HashMap<>();
        map.put("comId", comId);
        map.put("status", 2);
        map.put("order", "goods");
        List<NxCommunityFatherGoodsEntity> fatherGoods = nxRestrauntOrdersService.queryTodayComGoodsType(map);

        return R.ok().put("data", fatherGoods);
    }


    @RequestMapping(value = "/comGetResDeliveryGoods", method = RequestMethod.POST)
    @ResponseBody
    public R comGetResDeliveryGoods(Integer resId) {
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", resId);
        map.put("equalStatus", 2);
        map.put("orderByGoods", 1);
        List<NxRestrauntOrdersEntity> nxRestrauntOrdersEntities = nxRestrauntOrdersService.queryComDeliveryResOrdersByParams(map);
        return R.ok().put("data", nxRestrauntOrdersEntities);
    }


    /**
     * 获取未填写成本的订单
     *
     * @param comId 社区id
     * @param type  类别
     * @return
     */
    @RequestMapping(value = "/comGetUnCostOrders", method = RequestMethod.POST)
    @ResponseBody
    public R comGetUnCostOrders(Integer comId, Integer type) {

        Map<String, Object> map = new HashMap<>();
        map.put("comId", comId);
        map.put("type", type);
        map.put("cost", 0);
        map.put("dayuStatus", 1);
        map.put("status", 8);
        TreeSet<NxRestrauntOrdersEntity> ordersTreeSet = nxRestrauntOrdersService.queryUnCostDate(map);
        List<Map<String, Object>> result = new ArrayList<>();
        if (ordersTreeSet.size() > 0) {
            for (NxRestrauntOrdersEntity orders : ordersTreeSet) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("comId", comId);
                map1.put("type", type);
                map1.put("dayuStatus", 1);
                map1.put("status", 8);
                map1.put("cost", 0);
                map1.put("applyDate", orders.getNxRoApplyDate());
                List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = nxRestrauntOrdersService.queryResOrdersByComStockGoodsType(map1);
                Map<String, Object> map3 = new HashMap<>();
                map3.put("date", orders.getNxRoApplyDate());
                map3.put("arr", fatherGoodsEntities);
                result.add(map3);
            }

            return R.ok().put("data", result);
        } else {
            return R.error(-1, "meiyou dingdan");
        }

    }


    @RequestMapping(value = "/comUpdateOrderWeight", method = RequestMethod.POST)
    @ResponseBody
    public R comUpdateOrderWeight(@RequestBody List<NxRestrauntOrdersEntity> resOrders) {
        for (NxRestrauntOrdersEntity ordersEntity : resOrders) {
            nxRestrauntOrdersService.update(ordersEntity);
        }
        return R.ok();
    }

    @RequestMapping(value = "/disSaveOrderWeight", method = RequestMethod.POST)
    @ResponseBody
    public R disSaveOrderWeight(@RequestBody List<NxRestrauntOrdersEntity> resOrders) {
        for (NxRestrauntOrdersEntity ordersEntity : resOrders) {
            ordersEntity.setNxRoStatus(2);
//            NxRestrauntOrdersEntity ordersEntity1 = countOrderCostSubtotal(ordersEntity);
            nxRestrauntOrdersService.update(ordersEntity);
        }
        return R.ok();
    }


    @RequestMapping(value = "/comSaveOrderWeight", method = RequestMethod.POST)
    @ResponseBody
    public R comSaveOrderWeight(@RequestBody List<NxRestrauntOrdersEntity> resOrders) {
        for (NxRestrauntOrdersEntity ordersEntity : resOrders) {
            ordersEntity.setNxRoStatus(2);
//            NxRestrauntOrdersEntity ordersEntity1 = countOrderCostSubtotal(ordersEntity);
            nxRestrauntOrdersService.update(ordersEntity);
        }
        return R.ok();
    }

//    private NxRestrauntOrdersEntity countOrderCostSubtotal(NxRestrauntOrdersEntity ordersEntity) {
//
//        if (ordersEntity.getNxRoCostPrice() != null) {
//            String nxRoCostPrice = ordersEntity.getNxRoCostPrice();
//            String nxRoWeight = ordersEntity.getNxRoWeight();
//            BigDecimal add = new BigDecimal(nxRoCostPrice).multiply(new BigDecimal(nxRoWeight));
//            String s = add.setScale(1, BigDecimal.ROUND_HALF_DOWN).toString();
//            ordersEntity.setNxRoCostSubtotal(s);
//
//        }
//
//        return ordersEntity;
//    }

    /**
     * android
     * 2
     *
     * @param fatherId
     * @param type
     * @return
     */
    @RequestMapping(value = "/weighingGetGoods", method = RequestMethod.POST)
    @ResponseBody
    public R aaaweighingGetGoods(Integer fatherId, Integer type) {
        Map<String, Object> map2 = new HashMap<>();
        map2.put("fatherId", fatherId);
        map2.put("type", type);
        map2.put("status", 1);
        List<NxCommunityGoodsEntity> comGoodsEntities = nxRestrauntOrdersService.queryOrderGoodsByFatherId(map2);

        if (comGoodsEntities.size() > 0) {
            return R.ok().put("data", comGoodsEntities);
        } else {
            return R.error(-1, "暂时没有商品");
        }

    }

    /**
     * android 获取称重订单
     *
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/weighingGetOrders/{goodsId}")
    @ResponseBody
    public R aaaweighingGetOrders(@PathVariable Integer goodsId) {

        Map<String, Object> map2 = new HashMap<>();
        map2.put("comGoodsId", goodsId);
        map2.put("status", 1);
        List<NxRestrauntOrdersEntity> comGoodsEntities = nxRestrauntOrdersService.queryResOrdersByParams(map2);

        if (comGoodsEntities.size() > 0) {
            return R.ok().put("data", comGoodsEntities);
        } else {
            return R.error(-1, "暂时没有商品");
        }
    }

    /**
     * android
     * 获取未称重商品类别
     *
     * @param comId
     * @param type
     * @return
     */
    @RequestMapping(value = "/weighingGetOrderGoodsType", method = RequestMethod.POST)
    @ResponseBody
    public R aaaaweighingGetOrderGoodsType(Integer comId, Integer type) {

        Map<String, Object> map1 = new HashMap<>();
        map1.put("comId", comId);
        map1.put("type", type);
        map1.put("status", 1);
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = nxRestrauntOrdersService.queryTodayComGoodsType(map1);
        if (fatherGoodsEntities.size() > 0) {
            return R.ok().put("data", fatherGoodsEntities);
        } else {
            return R.error(-1, "暂时没有订货");
        }

//
//        if (type == 1) {
//            Map<String, Object> map1 = new HashMap<>();
//            map1.put("comId", comId);
//            map1.put("type", type);
//            map1.put("status", 1);
//            List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = nxRestrauntOrdersService.queryTodayComGoodsType(map1);
//            if (fatherGoodsEntities.size() > 0) {
//                return R.ok().put("data", fatherGoodsEntities);
//            } else {
//                return R.error(-1, "暂时没有订货");
//            }
//        } else if (type == 2) {
//            Map<String, Object> map2 = new HashMap<>();
//            map2.put("comId", comId);
//            map2.put("type", type);
//            map2.put("status", 1);
//            List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = nxRestrauntOrdersService.queryTodayComGoodsType(map2);
//            if (fatherGoodsEntities.size() > 0) {
//                return R.ok().put("data", fatherGoodsEntities);
//            } else {
//                return R.error(-1, "暂时没有订货");
//
//            }
//        } else if (type == 3) {
//            Map<String, Object> map2 = new HashMap<>();
//            map2.put("comId", comId);
//            map2.put("type", type);
//            map2.put("status", 1);
//            List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = nxRestrauntOrdersService.queryTodayComGoodsType(map2);
//            if (fatherGoodsEntities.size() > 0) {
//                return R.ok().put("data", fatherGoodsEntities);
//            } else {
//                return R.error(-1, "暂时没有订货");
//
//            }
//        }
//        return R.error(-1, "请重新查询");
    }


    /**
     * android 称重
     *
     * @param orderId
     * @param orderWeight
     * @return
     */
    @RequestMapping(value = "/saveOrderWeight", method = RequestMethod.POST)
    @ResponseBody
    public R androidSaveOrderWeight(Integer orderId, String orderWeight) {
        NxRestrauntOrdersEntity ordersEntity = nxRestrauntOrdersService.queryObject(orderId);
        String nxRoPrice = ordersEntity.getNxRoPrice();
        float a = Float.parseFloat(nxRoPrice);
        float b = Float.parseFloat(orderWeight);
        float result = a * b;
        NumberFormat formatter = new DecimalFormat("0.0");
        String formmatedFloatValue = formatter.format(result);
        ordersEntity.setNxRoSubtotal(formmatedFloatValue);
        ordersEntity.setNxRoWeight(orderWeight);
        System.out.println("priririri===" + ordersEntity.getNxRoCostPrice());
//        NxRestrauntOrdersEntity ordersEntity1 = countOrderCostSubtotal(ordersEntity);
        ordersEntity.setNxRoStatus(2);
        nxRestrauntOrdersService.update(ordersEntity);
        return R.ok();
    }


    /**
     * community
     * 社区获取未进货商品
     *
     * @param comId 社区ID
     * @param type  商品类别
     * @return
     */
    @RequestMapping(value = "/comGetTodayUnBuyGoodsType", method = RequestMethod.POST)
    @ResponseBody
    public R comGetTodayUnBuyGoodsType(Integer comId, Integer type) {

        Map<String, Object> map2 = new HashMap<>();
        map2.put("comId", comId);
        map2.put("type", type);
        map2.put("status", 3);

        if (type == 4) {
            List<NxDistributerEntity> DistributerEntities = nxRestrauntOrdersService.queryResOrdersByComDistributerGoodsType(map2);
            Map<String, Object> resultMap = getApplyComGoodsStatusTotal(comId, type);
            resultMap.put("arr", DistributerEntities);
            return R.ok().put("data", resultMap);
        } else {
            List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = nxRestrauntOrdersService.queryResOrdersByComStockGoodsType(map2);
            Map<String, Object> resultMap = getApplyComGoodsStatusTotal(comId, type);
            resultMap.put("arr", fatherGoodsEntities);
            return R.ok().put("data", resultMap);
        }

    }

    private Map<String, Object> getApplyComGoodsStatusTotal(Integer comId, Integer type) {
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("comId", comId);
//        map1.put("equalBuyStatus", 1);
//        map1.put("type", type);
//        int totalGoods = nxRestrauntOrdersService.queryOrderComGoodsTodayTotal(map1);
//
//        Map<String, Object> map2 = new HashMap<>();
//        map2.put("comId", comId);
//        map2.put("equalBuyStatus", 1);
//        map2.put("type", type);
//        int totalPurGoods = nxRestrauntOrdersService.queryOrderComGoodsTodayTotal(map2);
        Map<String, Object> map = new HashMap<>();
        map.put("comId", comId);
        map.put("type", type);
        map.put("status", 3);
        List<NxCommunityPurchaseBatchEntity> batchEntities = nxCPBatchService.queryComPurchaseBatchByParams(map);


        Map<String, Object> resultMap = new HashMap<>();
//        resultMap.put("goodsTotal", totalGoods);
        resultMap.put("goodsPurchase", batchEntities.size());
        return resultMap;

    }


    @RequestMapping(value = "/comGetUnPayCustomer/{comId}")
    @ResponseBody
    public R comGetUnPayCustomer(@PathVariable Integer comId) {
        Map<String, Object> map2 = new HashMap<>();
        map2.put("comId", comId);
        map2.put("dayuStatus", 4);
        map2.put("status", 7);
        map2.put("settleType", 0);
        TreeSet<NxRestrauntEntity> unPayRes = nxRestrauntOrdersService.queryCommunityTodayRestruants(map2);
        return R.ok().put("data", unPayRes);
    }

    @RequestMapping(value = "/comGetUnPrintCustomer/{comId}")
    @ResponseBody
    public R comGetUnPrintCustomer(@PathVariable Integer comId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("comId", comId);
        map3.put("dayuStatus", 2);
        map3.put("status", 7);
        map3.put("printTimes", 0);
        map3.put("settleType", 1);
        TreeSet<NxRestrauntEntity> unPrintRes = nxRestrauntOrdersService.queryCommunityTodayRestruants(map3);

        return R.ok().put("data", unPrintRes);
    }

    @RequestMapping(value = "/comGetWarnRestraunts/{comId}")
    @ResponseBody
    public R comGetWarnRestraunts(@PathVariable Integer comId) {
        System.out.println(comId + "=====");
        Map<String, Object> map = new HashMap<>();
        map.put("comId", comId);
        map.put("status", 1);
        TreeSet<NxRestrauntEntity> departmentEntities = nxRestrauntOrdersService.queryCommunityTodayRestruants(map);
        List<NxRestrauntEntity> resultRes = new ArrayList<>();
        for (NxRestrauntEntity res : departmentEntities) {
            if (res.getNxRestrauntWorkingStatus() == 0) {
                resultRes.add(res);
            }
        }
        if (resultRes.size() > 0) {
            return R.ok().put("data", resultRes);
        } else {
            return R.error(-1, "没有订单");
        }
    }

    @RequestMapping(value = "/comReceiveWarnRestraunt/{resId}")
    @ResponseBody
    public R comReceiveWarnRestraunt(@PathVariable Integer resId) {
        NxRestrauntEntity restrauntEntity = nxRestrauntService.queryObject(resId);

        restrauntEntity.setNxRestrauntWorkingStatus(1);
        nxRestrauntService.update(restrauntEntity);
        return R.ok();
    }

    /**
     * community
     * 区域获取今日订货餐馆订单
     *
     * @param comId 社区id
     * @return 订货餐馆
     */
    @RequestMapping(value = "/comGetTodayOrderRestruant/{comId}")
    @ResponseBody
    public R comGetTodayOrderCustomer(@PathVariable Integer comId) {

        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("comId", comId);
        map1.put("status", 5);
        TreeSet<NxRestrauntEntity> departmentEntities = nxRestrauntOrdersService.queryCommunityTodayRestruants(map1);

        //待支付现金客户
        Map<String, Object> map2 = new HashMap<>();
        map2.put("comId", comId);
        map2.put("dayuStatus", 4);
        map2.put("status", 7);
        map2.put("settleType", 0);
        TreeSet<NxRestrauntEntity> unPayRes = nxRestrauntOrdersService.queryCommunityTodayRestruants(map2);
        // 待打印收货记账客户
        Map<String, Object> map3 = new HashMap<>();
        map3.put("comId", comId);
        map3.put("dayuStatus", 3);
        map3.put("status", 7);
        map3.put("printTimes", 0);
        map3.put("settleType", 1);
        TreeSet<NxRestrauntEntity> unPrintRes = nxRestrauntOrdersService.queryCommunityTodayRestruants(map3);
        //待签字客户
//        Integer totalUnsign =  nxRestrauntBillService.queryUnSignCustomerCount(comId);
        List<NxRestrauntBillEntity> restrauntEntities = nxRestrauntBillService.queryUnSignCustomer(comId);

        //resutlData:
        Map<String, Object> map = new HashMap<>();
        map.put("arr", departmentEntities);
        map.put("totalUnsign", restrauntEntities.size());
        map.put("unPrint", unPrintRes.size());
        map.put("unPay", unPayRes.size());
        return R.ok().put("data", map);

    }

    @RequestMapping(value = "/resUserGetCleverGoods/{resId}")
    @ResponseBody
    public R resUserGetCleverGoods(@PathVariable Integer resId) {
        int checkDay = 4;

        //查询一周内userid 订单
        String s = formatWhatDay(-checkDay);
        System.out.println("几天之前=====" + s);

        Map<String, Object> map = new HashMap<>();
        map.put("resId", resId);
        map.put("applyDate", s);
        String todayString = formatWhatDay(0) + " 00:00"; //今天时间
        SimpleDateFormat sdfm = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date todayTime = new Date();
        try {
            todayTime = sdfm.parse(todayString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        map.put("todayTime", todayString); //不包括今天订过商品
        List<NxRestrauntComGoodsEntity> todayData = new ArrayList<>();
        TreeSet<NxRestrauntComGoodsEntity> theseDayOrderGoods = nxRestrauntOrdersService.queryTry(map);

        for (NxRestrauntComGoodsEntity comGoods : theseDayOrderGoods) {
            Integer nxRestrauntComGoodsId = comGoods.getNxRestrauntComGoodsId();
            //theseday orders
            Map<String, Object> map1 = new HashMap<>();
            map1.put("resComGoodsId", nxRestrauntComGoodsId);
            map1.put("applyDate", s);
            List<NxRestrauntOrdersEntity> ordersEntities = nxRestrauntOrdersService.queryResOrdersByParams(map1);

            System.out.println(nxRestrauntComGoodsId + " 的订单是几个： " + ordersEntities.size());
            //如果今天没有
            //如果check天内订过大于等于check次，说明每天订货此商品
            if (ordersEntities.size() > checkDay - 1) {
                todayData.add(comGoods);
            }
            //如果check天内订过check-1次，说明最少2天一次商品，而且今天没订
            else if (ordersEntities.size() == checkDay - 1) {
                todayData.add(comGoods);
            }
            //如果check天内订过check-2次，说明最少2天一次商品，而且今天没订
            else if (ordersEntities.size() == checkDay - 2) {
                NxRestrauntOrdersEntity ordersEntity = ordersEntities.get(ordersEntities.size() - 1);
                String nxRoApplyDate = ordersEntity.getNxRoApplyDate();
                String yesterday = formatWhatDay(-1);
                //如果昨天没定，今天也没订
                if (!nxRoApplyDate.equals(yesterday)) {
                    todayData.add(comGoods);
                }
            } else if (ordersEntities.size() == checkDay - 3) {
                NxRestrauntOrdersEntity ordersEntity = ordersEntities.get(0);
                String nxRoApplyDate = ordersEntity.getNxRoApplyDate();
                String lastDay = formatWhatDay(-2);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date lastDate = new Date();
                Date orderDate = new Date();
                try {
                    lastDate = sdf.parse(lastDay);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                try {
                    orderDate = sdf.parse(nxRoApplyDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (orderDate.compareTo(lastDate) < 0) {
                    todayData.add(comGoods);
                }
            }
        }

        return R.ok().put("data", todayData);
    }


    /**
     * 餐馆获取订单
     *
     * @param resId 餐馆id
     * @return 订单
     */
    @RequestMapping(value = "/resGetApply/{resId}")
    @ResponseBody
    public R resGetApply(@PathVariable Integer resId) {

//        Map<String, Object> map = new HashMap<>();
//        map.put("dayuApplyDate", formatWhatDay(-3));
//        map.put("resFatherId", resId);
//        List<NxRestrauntOrdersEntity> ordersEntities = nxRestrauntOrdersService.queryResOrdersByParams(map);
        NxRestrauntEntity nxRestrauntEntity = nxRestrauntService.queryObject(resId);
        Integer serviceLevel = nxRestrauntEntity.getNxRestrauntServiceLevel();
//        Map<String, Object> map7 = new HashMap<>();
//        map7.put("serviceLevel", serviceLevel);
//        map7.put("exchangeDate", formatWhatDate(0));
//        map7.put("resFatherId", resId);
//        List<NxCommunityGoodsEntity> communityGoodsEntities = cgService.resQueryComExchangePriceGoodsByDate(map7);
//

//        if (ordersEntities.size() == 0) {
//            return R.error(-2, "过期");
//        }
//        if (restrauntEntity.getNxRestrauntWorkingStatus() == -99) {
//            return R.error(-3, "tingye");
//        } else {


        Map<String, Object> mapData = new HashMap<>();
        List<NxRestrauntOrdersEntity> ordersEntitiesZero = getResApplysByRank(0, resId);
        List<NxRestrauntOrdersEntity> ordersEntitiesOne = getResApplysByRank(1, resId);
        List<NxRestrauntOrdersEntity> ordersEntitiesTwo = getResApplysByRank(2, resId);
        String totalExpectZero = "0.0";
        String totalExpectOne = "0.0";
        String totalExpectTwo = "0.0";
        String yunfeiZero = "0.0";
        String yunfeiOne = "0.0";
        String yunfeiTwo = "0.0";
        if (ordersEntitiesZero.size() > 0) {
            Map<String, Object> stringObjectMap = resGetTotalOrdersZero(ordersEntitiesZero);
            stringObjectMap.put("arr", ordersEntitiesZero);
            mapData.put("zero", stringObjectMap);
            totalExpectZero = (String) stringObjectMap.get("totalExpect");
            yunfeiZero = (String) stringObjectMap.get("yunfei");
        } else {
            mapData.put("zero", -1);
        }
        if (ordersEntitiesOne.size() > 0) {
            Map<String, Object> stringObjectMapOne = resGetTotalOrdersOne(ordersEntitiesOne);
            stringObjectMapOne.put("arr", ordersEntitiesOne);
            mapData.put("one", stringObjectMapOne);
            totalExpectOne = (String) stringObjectMapOne.get("totalExpect");
            yunfeiOne = (String) stringObjectMapOne.get("yunfei");
        } else {
            mapData.put("one", -1);
        }
        if (ordersEntitiesTwo.size() > 0) {
            Map<String, Object> stringObjectMapTwo = resGetTotalOrdersTwo(ordersEntitiesTwo, new BigDecimal(totalExpectZero));
            stringObjectMapTwo.put("arr", ordersEntitiesTwo);
            mapData.put("two", stringObjectMapTwo);
            totalExpectTwo = (String) stringObjectMapTwo.get("totalExpect");
            yunfeiTwo = (String) stringObjectMapTwo.get("yunfei");
        } else {
            mapData.put("two", -1);
        }

        //totalGoods, totalYunfei,total
        BigDecimal add = new BigDecimal(totalExpectZero).add(new BigDecimal(totalExpectOne)).add(new BigDecimal(totalExpectTwo));
        mapData.put("totalGoods", add.toString());
        BigDecimal totalYunfei = getMinBig(new BigDecimal(yunfeiZero), new BigDecimal(yunfeiOne), new BigDecimal(yunfeiTwo));
        mapData.put("totalYunfei", totalYunfei.toString());
        if (!totalYunfei.toString().equals("-1")) {
            BigDecimal add1 = add.add(totalYunfei);
            mapData.put("total", add1.toString());

        } else {
            mapData.put("total", add.toString());
        }

        //yunfeiZeroTishi
        if (!yunfeiZero.equals("0.0")) {
            BigDecimal bigDecimal = countTishiYunfeiZero(yunfeiZero, totalExpectZero);
            mapData.put("tishiZero", bigDecimal.toString());
        } else {
            mapData.put("tishiZero", -1);
        }
        //yunfeiOneTishi
        if (!yunfeiOne.equals("0.0")) {
            BigDecimal bigDecimal = countTishiYunfeiOne(yunfeiOne, totalExpectOne);
            mapData.put("tishiOne", bigDecimal.toString());
        } else {
            mapData.put("tishiOne", -1);
        }
        //yunfeiTwoTishi
        if (!yunfeiTwo.equals("0.0")) {
            BigDecimal bigDecimal = countTishiYunfeiTwo(yunfeiTwo, totalExpectTwo);
            mapData.put("tishiTwo", bigDecimal.toString());
        } else {
            mapData.put("tishiTwo", -1);
        }

        //获取餐馆是否结账
        if (nxRestrauntEntity.getNxRestrauntWorkingStatus() > 3) {
            Map<String, Object> stringObjectMap = resGetToPayApply(resId);
            mapData.put("payMap", stringObjectMap);
        } else {
            mapData.put("payMap", -1);
        }

        //查询是否有未结账单
        Map<String, Object> map6 = new HashMap<>();
        map6.put("resId", resId);
        map6.put("equalStatus", 0);
        NxRestrauntBillEntity unPayRestrauntBill = nxRestrauntBillService.queryUnPayRestrauntBill(map6);
        mapData.put("bill", unPayRestrauntBill);
        //查询价格变动商品
        List<NxRestrauntComGoodsEntity> resComGoodsWithComGoods = nxRestrauntComGoodsService.queryResComGoodsWithComGoods(resId);
        List<NxRestrauntComGoodsEntity> resultComGoods = new ArrayList<>();
        for (NxRestrauntComGoodsEntity resComGoods : resComGoodsWithComGoods) {
            SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm");

            String nxRcgResKnowDate = resComGoods.getNxRcgResKnowDate();
            String nxCgGoodsPriceExchangeDate = resComGoods.getNxCommunityGoodsEntity().getNxCgGoodsPriceExchangeDate();
            String nxCgGoodsTwoPriceExchangeDate = resComGoods.getNxCommunityGoodsEntity().getNxCgGoodsTwoPriceExchangeDate();
            String nxCgGoodsThreePriceExchangeDate = resComGoods.getNxCommunityGoodsEntity().getNxCgGoodsThreePriceExchangeDate();
            Date parse = null;
            try {
                parse = format.parse(nxRcgResKnowDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date priceParse = null;
            try {
                priceParse = format.parse(nxCgGoodsPriceExchangeDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Date priceTwoParse = null;
            try {
                priceTwoParse = format.parse(nxCgGoodsTwoPriceExchangeDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date priceThreeParse = null;
            try {
                priceThreeParse = format.parse(nxCgGoodsThreePriceExchangeDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (nxRestrauntEntity.getNxRestrauntServiceLevel() == 1) {
//                System.out.println(parse+ "parse=====" + priceParse);
                if (priceParse.compareTo(parse) > 0) {
                    BigDecimal subtract = new BigDecimal(resComGoods.getNxCommunityGoodsEntity().getNxCgGoodsPrice()).subtract(new BigDecimal(resComGoods.getNxRcgOrderPrice()));
                    resComGoods.setNxRcgComGoodsAbcExchange(subtract.abs().toString());
                    System.out.println("subtractsubtract" + subtract);
                    if (subtract.compareTo(BigDecimal.ZERO) > 0) {
                        resComGoods.setNxRcgComGoodsUpOrDown(1);
                        resultComGoods.add(resComGoods);

                    } else if (subtract.compareTo(BigDecimal.ZERO) == 0) {
                        resComGoods.setNxRcgComGoodsUpOrDown(0);
                    } else {
                        resComGoods.setNxRcgComGoodsUpOrDown(-1);
                        resultComGoods.add(resComGoods);

                    }
                    System.out.println(subtract + "------" + resComGoods.getNxRcgComGoodsUpOrDown());

                }
            }

            if (nxRestrauntEntity.getNxRestrauntServiceLevel() == 2) {
                if (priceTwoParse.compareTo(parse) > 0) {
                    resultComGoods.add(resComGoods);
                }
            }

            if (nxRestrauntEntity.getNxRestrauntServiceLevel() == 3) {
                if (priceThreeParse.compareTo(parse) > 0) {
                    resultComGoods.add(resComGoods);
                }
            }
        }

        mapData.put("exchange", resultComGoods);
        mapData.put("workingStatus", nxRestrauntEntity.getNxRestrauntWorkingStatus());

        if (nxRestrauntEntity.getNxRestrauntWorkingStatus() < 4 && ordersEntitiesZero.size() == 0 && ordersEntitiesOne.size() == 0 && ordersEntitiesTwo.size() == 0) {
            if (resultComGoods.size() > 0) {
                Map<String, Object> map9 = new HashMap<>();
                map9.put("exchange", resultComGoods);
                return R.ok().put("data", map9);
            } else {
                return R.error(-1, "无订单");
            }
        } else {

            return R.ok().put("data", mapData);
        }
//        }
    }


    public Map<String, Object> resGetToPayApply(Integer resId) {
        Map<String, Object> mapData = new HashMap<>();
        List<NxRestrauntOrdersEntity> ordersEntitiesZero = getResToPayApplysByRank(0, resId);
        List<NxRestrauntOrdersEntity> ordersEntitiesOne = getResToPayApplysByRank(1, resId);
        List<NxRestrauntOrdersEntity> ordersEntitiesTwo = getResToPayApplysByRank(2, resId);
        System.out.println(ordersEntitiesZero);
        System.out.println("hahhahahahahha------------------------------------");
        String totalExpectZero = "0.0";
        String totalExpectOne = "0.0";
        String totalExpectTwo = "0.0";
        String yunfeiZero = "0.0";
        String yunfeiOne = "0.0";
        String yunfeiTwo = "0.0";
        if (ordersEntitiesZero.size() > 0) {
            Map<String, Object> stringObjectMap = resGetTotalOrdersZeroPay(ordersEntitiesZero);
            stringObjectMap.put("arr", ordersEntitiesZero);
            mapData.put("zero", stringObjectMap);
            totalExpectZero = (String) stringObjectMap.get("totalSubtotal");
            yunfeiZero = (String) stringObjectMap.get("yunfei");
        } else {
            mapData.put("zero", -1);
        }
        if (ordersEntitiesOne.size() > 0) {
            Map<String, Object> stringObjectMapOne = resGetTotalOrdersOnePay(ordersEntitiesOne);
            stringObjectMapOne.put("arr", ordersEntitiesOne);
            mapData.put("one", stringObjectMapOne);
            totalExpectOne = (String) stringObjectMapOne.get("totalSubtotal");
            yunfeiOne = (String) stringObjectMapOne.get("yunfei");
        } else {
            mapData.put("one", -1);
        }
        if (ordersEntitiesTwo.size() > 0) {
            Map<String, Object> stringObjectMapTwo = resGetTotalOrdersTwoPay(ordersEntitiesTwo, new BigDecimal(totalExpectZero));
            stringObjectMapTwo.put("arr", ordersEntitiesTwo);
            mapData.put("two", stringObjectMapTwo);
            totalExpectTwo = (String) stringObjectMapTwo.get("totalSubtotal");
            yunfeiTwo = (String) stringObjectMapTwo.get("yunfei");
        } else {
            mapData.put("two", -1);
        }

        //totalGoods, totalYunfei,total
        BigDecimal add = new BigDecimal(totalExpectZero).add(new BigDecimal(totalExpectOne)).add(new BigDecimal(totalExpectTwo));
        mapData.put("totalGoods", add.toString());
        BigDecimal totalYunfei = getMinBig(new BigDecimal(yunfeiZero), new BigDecimal(yunfeiOne), new BigDecimal(yunfeiTwo));
        System.out.println("mininmini===" + totalYunfei);
        mapData.put("totalYunfei", totalYunfei.toString());
        if (!totalYunfei.toString().equals("-1")) {
            BigDecimal add1 = add.add(totalYunfei);
            mapData.put("total", add1.toString());

        } else {
            mapData.put("total", add.toString());
        }

        //yunfeiZeroTishi
        if (!yunfeiZero.equals("0.0")) {
            BigDecimal bigDecimal = countTishiYunfeiZero(yunfeiZero, totalExpectZero);
            mapData.put("tishiZero", bigDecimal.toString());
        } else {
            mapData.put("tishiZero", -1);
        }
        //yunfeiOneTishi
        if (!yunfeiOne.equals("0.0")) {
            BigDecimal bigDecimal = countTishiYunfeiOne(yunfeiOne, totalExpectOne);
            mapData.put("tishiOne", bigDecimal.toString());
        } else {
            mapData.put("tishiOne", -1);
        }
        //yunfeiTwoTishi
        if (!yunfeiTwo.equals("0.0")) {
            BigDecimal bigDecimal = countTishiYunfeiTwo(yunfeiTwo, totalExpectTwo);
            mapData.put("tishiTwo", bigDecimal.toString());
        } else {
            mapData.put("tishiTwo", -1);
        }
        return mapData;
    }

    private BigDecimal getMinBig(BigDecimal rd, BigDecimal sl, BigDecimal sj) {

        List<BigDecimal> array = new ArrayList<>();
        BigDecimal zero = new BigDecimal("0.0");
        if (rd.compareTo(zero) != 0) {
            array.add(rd);
        }
        if (sl.compareTo(zero) != 0) {
            array.add(sl);
        }
        if (sj.compareTo(zero) != 0) {
            array.add(sj);
        }
        if (array.size() > 0) {
            BigDecimal min = array.get(0);
            //获取最小值
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).compareTo(min) == (-1)) {
                    min = array.get(i);
                }
            }
            return min;
        } else {
            return new BigDecimal("0.0");
        }
    }

    private BigDecimal countTishiYunfeiZero(String yunfei, String totalExpectZero) {
        BigDecimal subtract = new BigDecimal("99.0").subtract(new BigDecimal(totalExpectZero));
        return subtract;
    }

    private BigDecimal countTishiYunfeiOne(String yunfei, String totalExpectOne) {

        if (yunfei.equals("20.0")) {
            BigDecimal subtract = new BigDecimal("99.0").subtract(new BigDecimal(totalExpectOne));
            return subtract;
        } else {
            return new BigDecimal("0.0");

        }

    }

    private BigDecimal countTishiYunfeiTwo(String yunfei, String totalExpectTwo) {
        if (yunfei.equals("16.0")) {
            BigDecimal subtract = new BigDecimal("99.0").subtract(new BigDecimal(totalExpectTwo));
            return subtract;
        } else {
            return new BigDecimal("0.0");

        }
    }

    private List<NxRestrauntOrdersEntity> getResToPayApplysByRank(Integer rank, Integer resId) {
        //今天的数据
        Map<String, Object> map = new HashMap<>();
        map.put("resId", resId);
        map.put("orderBy", "time");
        map.put("dayuStatus", 4);
        map.put("status", 7);
        map.put("rank", rank);
        return nxRestrauntOrdersService.queryResOrdersByParams(map);
    }


    private List<NxRestrauntOrdersEntity> getResApplysByRank(Integer rank, Integer resId) {
        //今天的数据
        Map<String, Object> map = new HashMap<>();
        map.put("resId", resId);
        map.put("orderBy", "time");
        //todo
        map.put("status", 5);
        map.put("rank", rank);
        System.out.println("levelelelelelle" + map);
        return nxRestrauntOrdersService.queryResOrdersByParams(map);
    }

    private Map<String, Object> resGetTotalOrdersTwo(List<NxRestrauntOrdersEntity> ordersEntities, BigDecimal zeroTotal) {
        Map<String, Object> map = new HashMap<>();
        //1.预期One-订单金额
        BigDecimal totalExpect = new BigDecimal(0);
        for (NxRestrauntOrdersEntity ordersEntity : ordersEntities) {
            String nxRoExpectPrice = ordersEntity.getNxRoExpectPrice();
            if (nxRoExpectPrice != null) {
                BigDecimal subtotal = new BigDecimal(nxRoExpectPrice);
                totalExpect = totalExpect.add(subtotal);
            }
        }
        map.put("totalExpect", totalExpect.toString());

        //todo 有运费的时候打开这里
        //2，订单级别运费
//        BigDecimal mintotalExpect = new BigDecimal(99);
//
//        if (totalExpect.compareTo(mintotalExpect) < 0) {
//            BigDecimal subtract = mintotalExpect.subtract(totalExpect);
//            map.put("cha", subtract.toString());
//            map.put("yunfei", "16.0");
//            //小于99 大于49
//        } else {
//            map.put("cha", 0);
//            map.put("yunfei", "-1");
//        }
//
        //todo 有运费的时候注销这里
        map.put("cha", 0);
        map.put("yunfei", "-1");

        return map;
    }

    private Map<String, Object> resGetTotalOrdersTwoPay(List<NxRestrauntOrdersEntity> ordersEntities, BigDecimal zeroTotal) {
        Map<String, Object> map = new HashMap<>();
        //1.预期One-订单金额
        BigDecimal totalSubtotal = new BigDecimal(0);
        for (NxRestrauntOrdersEntity ordersEntity : ordersEntities) {
            String nxRoSubtotal = ordersEntity.getNxRoSubtotal();
            if (nxRoSubtotal != null) {
                BigDecimal subtotal = new BigDecimal(nxRoSubtotal);
                totalSubtotal = totalSubtotal.add(subtotal);
            }
        }
        map.put("totalSubtotal", totalSubtotal.toString());

        //2，订单级别运费
        // todo 有运费的时候打开这里
//        BigDecimal mintotalExpect = new BigDecimal(99);
//
//        if (totalSubtotal.compareTo(mintotalExpect) < 0) {
//            BigDecimal subtract = mintotalExpect.subtract(totalSubtotal);
//            map.put("cha", subtract.toString());
//            map.put("yunfei", "16.0");
//            //小于99 大于49
//        } else {
//            map.put("cha", 0);
//            map.put("yunfei", "-1");
//        }

        //todo 有运费的时候注销这里
        map.put("cha", 0);
        map.put("yunfei", "-1");
        return map;
    }

    private Map<String, Object> resGetTotalOrdersOne(List<NxRestrauntOrdersEntity> ordersEntities) {
        Map<String, Object> map = new HashMap<>();

        //1.预期订单金额
        BigDecimal totalExpect = new BigDecimal(0);
        for (NxRestrauntOrdersEntity ordersEntity : ordersEntities) {
            String nxRoExpectPrice = ordersEntity.getNxRoExpectPrice();
            if (nxRoExpectPrice != null) {
                BigDecimal subtotal = new BigDecimal(nxRoExpectPrice);
                totalExpect = totalExpect.add(subtotal);
            }
        }
        map.put("totalExpect", totalExpect.toString());
        //2，订单级别运费
        //最低
        // todo 有运费的时候，打开这里
//        BigDecimal mintotalExpect = new BigDecimal(99);
//        if (totalExpect.compareTo(mintotalExpect) < 0) {
//            BigDecimal subtract = mintotalExpect.subtract(totalExpect);
//            map.put("cha", subtract.toString());
//            map.put("yunfei", "20.0");
//            //小于99 大于49
//        } else {
//            map.put("cha", 0);
//            map.put("yunfei", "-1");
//        }

        //todo 有运费的时候注销这里
        map.put("cha", 0);
        map.put("yunfei", "-1");
        return map;
    }

    private Map<String, Object> resGetTotalOrdersOnePay(List<NxRestrauntOrdersEntity> ordersEntities) {
        Map<String, Object> map = new HashMap<>();

        //1.预期订单金额
        BigDecimal totalSubtotal = new BigDecimal(0);
        for (NxRestrauntOrdersEntity ordersEntity : ordersEntities) {
            String nxRoSubtotal = ordersEntity.getNxRoSubtotal();
            if (nxRoSubtotal != null) {
                BigDecimal subtotal = new BigDecimal(nxRoSubtotal);
                totalSubtotal = totalSubtotal.add(subtotal);
            }
        }
        map.put("totalSubtotal", totalSubtotal.toString());
        //2，订单级别运费
        // todo 有运费的时候，打开这里
        //最低
//        BigDecimal mintotalExpect = new BigDecimal(99);
//
//        //小于49
//        if (totalSubtotal.compareTo(mintotalExpect) < 0) {
//            BigDecimal subtract = mintotalExpect.subtract(totalSubtotal);
//            map.put("cha", subtract.toString());
//            map.put("yunfei", "20.0");
//            //小于99 大于49
//        } else {
//            map.put("cha", 0);
//            map.put("yunfei", "-1");
//        }

        //todo 有运费的时候注销这里
        map.put("cha", 0);
        map.put("yunfei", "-1");
        return map;
    }


    private Map<String, Object> resGetTotalOrdersZero(List<NxRestrauntOrdersEntity> ordersEntities) {
        Map<String, Object> map = new HashMap<>();

        //1.预期订单金额
        BigDecimal totalExpect = new BigDecimal(0);
        for (NxRestrauntOrdersEntity ordersEntity : ordersEntities) {
            String nxRoExpectPrice = ordersEntity.getNxRoExpectPrice();
            if (nxRoExpectPrice != null) {
                BigDecimal subtotal = new BigDecimal(nxRoExpectPrice);
                totalExpect = totalExpect.add(subtotal);
            }
        }
        map.put("totalExpect", totalExpect.toString());
        //2，订单级别运费
        //todo 有运费的时候打开这里
//        BigDecimal mintotalExpect = new BigDecimal(49);
//        BigDecimal middletotalExpect = new BigDecimal(99);

//        //小于49
//        if (totalExpect.compareTo(mintotalExpect) < 0) {
//            BigDecimal subtract = mintotalExpect.subtract(totalExpect);
//            map.put("cha", subtract.toString());
//            map.put("yunfei", "12.0");
//            //小于99 大于49
//        } else {
////
//
//            if (totalExpect.compareTo(middletotalExpect) < 0) {
//                BigDecimal subtract = middletotalExpect.subtract(totalExpect);
//                map.put("cha", subtract.toString());
//                map.put("yunfei", "10.0");
//                //xiao于199 大于99
//            } else {
//                map.put("cha", 0);
//                map.put("yunfei", "-1");
//
//            }
//        }
        //todo 有运费的时候打开上面的代码，注销这2行代码
        map.put("cha", 0);
        map.put("yunfei", "-1");


        return map;
    }

    private Map<String, Object> resGetTotalOrdersZeroPay(List<NxRestrauntOrdersEntity> ordersEntities) {
        Map<String, Object> map = new HashMap<>();

        //1.预期订单金额
        BigDecimal totalSubtotal = new BigDecimal(0);
        for (NxRestrauntOrdersEntity ordersEntity : ordersEntities) {
            String nxRoSubtotal = ordersEntity.getNxRoSubtotal();
            if (nxRoSubtotal != null) {
                BigDecimal subtotal = new BigDecimal(nxRoSubtotal);
                totalSubtotal = totalSubtotal.add(subtotal);
            }
        }
        map.put("totalSubtotal", totalSubtotal.toString());
        //2，订单级别运费
        //最低
        BigDecimal mintotalExpect = new BigDecimal(49);
        BigDecimal middletotalExpect = new BigDecimal(99);

        //小于49
        //todo 有运费的时候打开这里
//        if (totalSubtotal.compareTo(mintotalExpect) < 0) {
//            BigDecimal subtract = mintotalExpect.subtract(totalSubtotal);
//            map.put("cha", subtract.toString());
//            map.put("yunfei", "12.0");
//            //小于99 大于49
//        } else {
//            if (totalSubtotal.compareTo(middletotalExpect) < 0) {
//                BigDecimal subtract = middletotalExpect.subtract(totalSubtotal);
//                map.put("cha", subtract.toString());
//                map.put("yunfei", "10.0");
//                //xiao于199 大于99
//            } else {
//                map.put("cha", 0);
//                map.put("yunfei", "-1");
//            }
//        }
        //todo 有运费的时候注销这里
        map.put("cha", 0);
        map.put("yunfei", "-1");

        return map;
    }


    /**
     * Community
     * quyu获取今日订货群和订单
     *
     * @param comId quyuid
     * @return 订货群
     */
    @RequestMapping(value = "/comGetToDeliveryRestraunt/{comId}")
    @ResponseBody
    public R comGetToDeliveryRestraunt(@PathVariable Integer comId) {

        Map<String, Object> map1 = new HashMap<>();
        map1.put("comId", comId);
        map1.put("equalStatus", 2);
        TreeSet<NxRestrauntEntity> restaurantEntities1 = nxRestrauntOrdersService.queryDeliveryResByParams(map1);

        return R.ok().put("data", restaurantEntities1);
    }

    @RequestMapping(value = "/updateOrderList", method = RequestMethod.POST)
    @ResponseBody
    public R updateOrderList(@RequestBody List<NxRestrauntOrdersEntity> ordersEntities) {
        for (NxRestrauntOrdersEntity orders : ordersEntities) {
            System.out.println(orders.getNxRoProfit() + "bfofoofoffofoof====");
            nxRestrauntOrdersService.update(orders);
        }
        return R.ok();
    }

    @RequestMapping(value = "/updateOrder", method = RequestMethod.POST)
    @ResponseBody
    public R updateOrder(@RequestBody NxRestrauntOrdersEntity nxRestrauntOrdersEntity) {
        nxRestrauntOrdersService.update(nxRestrauntOrdersEntity);
        Integer nxRoComDistributerOrderId = nxRestrauntOrdersEntity.getNxRoComDistributerOrderId();
        System.out.println("nxRoComDistributerOrderIdnxRoComDistributerOrderId" + nxRoComDistributerOrderId);
        if (nxRoComDistributerOrderId != null) {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(nxRoComDistributerOrderId);
            ordersEntity.setNxDoQuantity(nxRestrauntOrdersEntity.getNxRoQuantity());
            ordersEntity.setNxDoStandard(nxRestrauntOrdersEntity.getNxRoStandard());
            ordersEntity.setNxDoRemark(nxRestrauntOrdersEntity.getNxRoRemark());
            nxDepartmentOrdersService.update(ordersEntity);
        }
        return R.ok();
    }

    @RequestMapping(value = "/deleteResOrder/{id}")
    @ResponseBody
    public R deleteResOrder(@PathVariable Integer id) {
        NxRestrauntOrdersEntity ordersEntity = nxRestrauntOrdersService.queryObject(id);
        nxRestrauntOrdersService.delete(id);

        Integer nxRoRestrauntFatherId = ordersEntity.getNxRoRestrauntFatherId();
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", nxRoRestrauntFatherId);
        List<NxRestrauntOrdersEntity> ordersEntities = nxRestrauntOrdersService.queryResOrdersByParams(map);
        if(ordersEntities.size() == 0){
            NxRestrauntEntity restrauntEntity = nxRestrauntService.queryObject(nxRoRestrauntFatherId);
            restrauntEntity.setNxRestrauntWorkingStatus(0);
            nxRestrauntService.update(restrauntEntity);
        }
        Integer nxRoComDistributerOrderId = ordersEntity.getNxRoComDistributerOrderId();
        System.out.println("orrrorororididididid" + nxRoComDistributerOrderId);
        if(nxRoComDistributerOrderId != null){
            nxDepartmentOrdersService.delete(nxRoComDistributerOrderId);

        }


        return R.ok();
    }


    @ResponseBody
    @RequestMapping("/saveResOrder")
    public R saveResOrder(@RequestBody NxRestrauntOrdersEntity nxRestrauntOrdersEntity) {
        Integer nxDepOrderId = 0;
        //rank
        Integer nxRoComGoodsFatherId = nxRestrauntOrdersEntity.getNxRoComGoodsFatherId();
        NxCommunityFatherGoodsEntity fatherGoodsEntity = nxCommunityFatherGoodsService.queryObject(nxRoComGoodsFatherId);
        NxCommunityFatherGoodsEntity grandGoodsEntity = nxCommunityFatherGoodsService.queryObject(fatherGoodsEntity.getNxCfgFathersFatherId());
        Integer nxCfgOrderRank = grandGoodsEntity.getNxCfgOrderRank();
        nxRestrauntOrdersEntity.setNxRoOrderRank(nxCfgOrderRank);
        //printTimes
        nxRestrauntOrdersEntity.setNxRoPrintTimes(0);

        //add purchaseGoods
        Integer comGoodsId = nxRestrauntOrdersEntity.getNxRoComGoodsId();
        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryObject(comGoodsId);
//
        Integer nxCgGoodsType = communityGoodsEntity.getNxCgGoodsType();
        nxRestrauntOrdersEntity.setNxRoPurchaseGoodsId(-1);
        //需要采购的订单
        if (nxCgGoodsType.equals(getNXCOMMUNITYGOODSTYPERICAI()) || nxCgGoodsType.equals(getNXCOMMUNITYGOODSTYPESUPPLIER())) {
            //查询是否有采购的同一个商品
            Map<String, Object> map = new HashMap<>();
            map.put("comGoodsId", comGoodsId);
            map.put("equalStatus", 0);
            List<NxCommunityPurchaseGoodsEntity> goodsEntities = nxComPurchaseGoodsService.queryPurchaseForComGoods(map);
            if (goodsEntities.size() == 0) {
                //是个新采购商品
                NxCommunityPurchaseGoodsEntity comPurchaseGoodsEntity = new NxCommunityPurchaseGoodsEntity();
                comPurchaseGoodsEntity.setNxCpgComGoodsFatherId(nxRestrauntOrdersEntity.getNxRoComGoodsFatherId());
                comPurchaseGoodsEntity.setNxCpgComGoodsId(nxRestrauntOrdersEntity.getNxRoComGoodsId());
                comPurchaseGoodsEntity.setNxCpgCommunityId(nxRestrauntOrdersEntity.getNxRoCommunityId());
                comPurchaseGoodsEntity.setNxCpgApplyDate(formatWhatDay(0));
                comPurchaseGoodsEntity.setNxCpgStatus(0);
                comPurchaseGoodsEntity.setNxCpgOrdersAmount(1);
                comPurchaseGoodsEntity.setNxCpgStandard(nxRestrauntOrdersEntity.getNxRoStandard());
                comPurchaseGoodsEntity.setNxCpgQuantity(nxRestrauntOrdersEntity.getNxRoQuantity());
                comPurchaseGoodsEntity.setNxCpgPurchaseType(getGbOrderTypeJiCai());
                nxComPurchaseGoodsService.save(comPurchaseGoodsEntity);
                Integer gbDistributerPurchaseGoodsId = comPurchaseGoodsEntity.getNxCommunityPurchaseGoodsId();
                nxRestrauntOrdersEntity.setNxRoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                nxRestrauntOrdersEntity.setNxRoBuyStatus(0);

            } else {
                //给老采购商品添加新订单
                NxCommunityPurchaseGoodsEntity gbDisPurGoodsEntity = goodsEntities.get(0);
                Integer gbDistributerPurchaseGoodsId = gbDisPurGoodsEntity.getNxCommunityPurchaseGoodsId();
                nxRestrauntOrdersEntity.setNxRoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                nxRestrauntOrdersEntity.setNxRoBuyStatus(0);
                //采购商品订单数量更新
                Integer gbDpgOrdersAmount = gbDisPurGoodsEntity.getNxCpgOrdersAmount();
                gbDisPurGoodsEntity.setNxCpgOrdersAmount(gbDpgOrdersAmount + 1);
                BigDecimal purQuantity = new BigDecimal(gbDisPurGoodsEntity.getNxCpgQuantity());
                BigDecimal orderQuantity = new BigDecimal(nxRestrauntOrdersEntity.getNxRoQuantity());
                BigDecimal add = purQuantity.add(orderQuantity).setScale(2, BigDecimal.ROUND_HALF_UP);
                gbDisPurGoodsEntity.setNxCpgQuantity(add.toString());
                nxComPurchaseGoodsService.update(gbDisPurGoodsEntity);
            }
            System.out.println("zidongddidid" +  nxCgGoodsType);
            //判断是否是自动订货商品，代替采购员发送purchaseBatch

            if (nxCgGoodsType.equals(getNXCOMMUNITYGOODSTYPESUPPLIER())) {
                Integer nxCgDistributerId = communityGoodsEntity.getNxCgDistributerId();
                Integer nxCgDistributerGoodsId = communityGoodsEntity.getNxCgDistributerGoodsId();
                Map<String, Object> mapData = autoAddPurchaseBatch(nxRestrauntOrdersEntity, communityGoodsEntity);
                Integer purUserId = (Integer) mapData.get("purUserId");
                nxRestrauntOrdersEntity.setNxRoPurchaseUserId(purUserId);
                nxRestrauntOrdersEntity.setNxRoComDistributerId(nxCgDistributerId);
                nxRestrauntOrdersEntity.setNxRoComDistributerGoodsId(nxCgDistributerGoodsId);
                nxRestrauntOrdersEntity.setNxRoCostPrice(communityGoodsEntity.getNxCgBuyingPrice());
            }

        }


        //判断是否是配送商，自动生成配送供货商NxDistributer一个订单
        if (nxCgGoodsType.equals(getNXCOMMUNITYGOODSTYPENXDISTRIBUTER())) {
            System.out.println("nxdisorreerer==" + nxCgGoodsType);
            Integer nxDoDistributerId = nxRestrauntOrdersEntity.getNxRoCommunityId();
            Integer gbDoNxDistributerGoodsId = nxRestrauntOrdersEntity.getNxRoComDistributerGoodsId();
            String gbDoQuantity = nxRestrauntOrdersEntity.getNxRoQuantity();
            String gbDoStandard = nxRestrauntOrdersEntity.getNxRoStandard();
            String gbDoRemark = nxRestrauntOrdersEntity.getNxRoRemark();
            String gbDoApplyDate = nxRestrauntOrdersEntity.getNxRoApplyDate();
            //
            String gbDoArriveOnlyDate = nxRestrauntOrdersEntity.getNxRoArriveOnlyDate();
            Integer gbDoArriveWeeksYear = nxRestrauntOrdersEntity.getNxRoArriveWeeksYear();
            String gbDoApplyFullTime = nxRestrauntOrdersEntity.getNxRoApplyFullTime();
            String gbDoApplyArriveDate = nxRestrauntOrdersEntity.getNxRoArriveDate();
            Integer nxRoRestrauntId = nxRestrauntOrdersEntity.getNxRoRestrauntId();
            Integer nxRoCommunityId = nxRestrauntOrdersEntity.getNxRoCommunityId();
            Integer nxRoRestrauntFatherId = nxRestrauntOrdersEntity.getNxRoRestrauntFatherId();
            NxDistributerGoodsEntity nxDistributerGoodsEntity1 = nxDistributerGoodsService.queryObject(gbDoNxDistributerGoodsId);
            Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity1.getNxDgDfgGoodsFatherId();
            //
            NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
            ordersEntity.setNxDoDistributerId(nxDoDistributerId);
            ordersEntity.setNxDoDisGoodsId(gbDoNxDistributerGoodsId);
            ordersEntity.setNxDoQuantity(gbDoQuantity);
            ordersEntity.setNxDoStandard(gbDoStandard);
            ordersEntity.setNxDoRemark(gbDoRemark);
            ordersEntity.setNxDoApplyDate(gbDoApplyDate);
            ordersEntity.setNxDoArriveOnlyDate(gbDoArriveOnlyDate);
            ordersEntity.setNxDoArriveWeeksYear(gbDoArriveWeeksYear);
            ordersEntity.setNxDoApplyFullTime(gbDoApplyFullTime);
            ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
            ordersEntity.setNxDoArriveDate(gbDoApplyArriveDate);
            ordersEntity.setNxDoNxCommunityId(nxRoCommunityId);
            ordersEntity.setNxDoNxCommRestrauntId(nxRoRestrauntId);
            ordersEntity.setNxDoNxCommRestrauntFatherId(nxRoRestrauntFatherId);
            ordersEntity.setNxDoDepartmentId(-1);
            ordersEntity.setNxDoDepartmentFatherId(-1);
            ordersEntity.setNxDoGbDepartmentId(-1);
            ordersEntity.setNxDoGbDepartmentFatherId(-1);
            ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
            ordersEntity.setNxDoDepDisGoodsId(-1);
            ordersEntity.setNxDoArriveWhatDay(getWeek(0));
            ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
            ordersEntity.setNxDoPrice(communityGoodsEntity.getNxCgBuyingPrice());

            nxDepartmentOrdersService.save(ordersEntity);
            nxDepOrderId = ordersEntity.getNxDepartmentOrdersId();
            Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
            nxRestrauntOrdersEntity.setNxRoComDistributerOrderId(nxDepartmentOrdersId);
            nxRestrauntOrdersEntity.setNxRoCostPrice(communityGoodsEntity.getNxCgBuyingPrice());

        }

        nxRestrauntOrdersService.save(nxRestrauntOrdersEntity);



        if (nxCgGoodsType.equals(getNXCOMMUNITYGOODSTYPENXDISTRIBUTER())) {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(nxDepOrderId);
            ordersEntity.setNxDoNxRestrauntOrderId(nxRestrauntOrdersEntity.getNxRestrauntOrdersId());
            ordersEntity.setNxDoGbDepartmentOrderId(-1);
            nxDepartmentOrdersService.update(ordersEntity);
        }
        return R.ok().put("data", nxRestrauntOrdersService.queryObject(nxRestrauntOrdersEntity.getNxRestrauntOrdersId()));
    }

    private Map<String, Object> autoAddPurchaseBatch(NxRestrauntOrdersEntity ordersEntity, NxCommunityGoodsEntity goodsEntity) {
        Map<String, Object> mapData = new HashMap<>();

        //
        Integer gbDgGbSupplierId = goodsEntity.getNxCgCommunitySupplierId();
        NxCommunitySupplierEntity communitySupplierEntity = nxCommunitySupplierService.queryObject(gbDgGbSupplierId);
        Integer gbDsSupplierUserId = communitySupplierEntity.getNxCsJrdhSupplierUserId();
        Integer gbDsPurUserId = communitySupplierEntity.getNxCsJrdhPurUserId();
        Integer gbDsGbDepartmentId1 = communitySupplierEntity.getNxCsNxCommunityId();
        System.out.println("autotottototootot" + gbDsPurUserId + "gbDgGbSupplierIdgbDgGbSupplierId=" + gbDgGbSupplierId);

        Map<String, Object> map = new HashMap<>();
        mapData.put("purUserId", gbDsPurUserId);

        map.put("supplierId", gbDgGbSupplierId);
        map.put("status", 1);
        List<NxCommunityPurchaseBatchEntity> entities = nxComPurchaseBatchService.queryComPurchaseBatchByParams(map);

        if (entities.size() == 0) {
            //
            NxCommunityPurchaseBatchEntity batchEntity = new NxCommunityPurchaseBatchEntity();
            batchEntity.setNxCpbDate(formatWhatDay(0));
            batchEntity.setNxCpbHour(formatWhatHour(0));
            batchEntity.setNxCpbMinute(formatWhatMinute(0));
            batchEntity.setNxCpbTime(formatWhatTime(0));
            batchEntity.setNxCpbStatus(-1);
            batchEntity.setNxCpbPurUserId(gbDsPurUserId);
            batchEntity.setNxCpbComSupplierId(communitySupplierEntity.getNxCommunitySupplierId());
            batchEntity.setNxCpbCommunityId(goodsEntity.getNxCgCommunityId());
            batchEntity.setNxCpbPurchaseType(goodsEntity.getNxCgGoodsType());
            nxComPurchaseBatchService.save(batchEntity);

            Integer gbDoPurchaseGoodsId = ordersEntity.getNxRoPurchaseGoodsId();
            NxCommunityPurchaseGoodsEntity gbDistributerPurchaseGoodsEntity = nxComPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
            gbDistributerPurchaseGoodsEntity.setNxCpgBatchId(batchEntity.getNxCommunityPurchaseBatchId());
            gbDistributerPurchaseGoodsEntity.setNxCpgStatus(1);
            gbDistributerPurchaseGoodsEntity.setNxCpgPurchaseDate(formatWhatDay(0));
            gbDistributerPurchaseGoodsEntity.setNxCpgCommunityId(batchEntity.getNxCpbCommunityId());
            gbDistributerPurchaseGoodsEntity.setNxCpgTime(formatWhatTime(0));
            nxComPurchaseGoodsService.update(gbDistributerPurchaseGoodsEntity);
            mapData.put("batchId", gbDistributerPurchaseGoodsEntity.getNxCommunityPurchaseGoodsId());
            return mapData;
        } else {
            NxCommunityPurchaseBatchEntity batchEntity = entities.get(0);
            Integer gbDoPurchaseGoodsId = ordersEntity.getNxRoPurchaseGoodsId();
            NxCommunityPurchaseGoodsEntity gbDistributerPurchaseGoodsEntity1 = nxComPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
            gbDistributerPurchaseGoodsEntity1.setNxCpgBatchId(batchEntity.getNxCommunityPurchaseBatchId());
            nxComPurchaseGoodsService.update(gbDistributerPurchaseGoodsEntity1);
            mapData.put("batchId", batchEntity.getNxCommunityPurchaseBatchId());
            return mapData;

        }

    }


}
