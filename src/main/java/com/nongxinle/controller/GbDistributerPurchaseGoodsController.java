package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-24 11:45
 */

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.sun.javafx.binding.StringFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxOrderStatusHasFinished;


@RestController
@RequestMapping("api/gbdistributerpurchasegoods")
public class GbDistributerPurchaseGoodsController {
    @Autowired
    private GbDistributerPurchaseGoodsService gbDpgService;

    @Autowired
    private GbDistributerPurchaseBatchService gbDPBService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDistributerGoodsShelfService gbDisGoodsShelfService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerGoodsPriceService gbDistributerGoodsPriceService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private GbDistributerWeightTotalService gbDistributerWeightTotalService;





    @RequestMapping(value = "/getGbPurGoods/{id}")
    @ResponseBody
    public R getGbPurGoods(@PathVariable Integer id) {

        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryPurGoodsWithOrders(id);
        return R.ok().put("data", purchaseGoodsEntity);
    }

    @RequestMapping(value = "/getDisGoodsPurList", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsPurList(Integer disGoodsId, String startDate, String stopDate, Integer depId) {

        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = new ArrayList<>();
        double doutbleCost = 0;
        double doutbleCostV = 0;
        double v = 0;
        String maxPrice = "0";
        String minPrice = "0";
        String perPrice = "0";
        int purCount = 0;

        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("disGoodsId", disGoodsId);
        map.put("equalStatus", 3);
        if (depId != -1) {
            map.put("purDepId", depId);
        }
        purCount = gbDpgService.queryGbPurchaseGoodsCount(map);
        if (purCount > 0) {
            System.out.println("caigoushushul" + map);
            doutbleCostV = gbDpgService.queryPurchaseGoodsSubTotal(map);
            doutbleCost = gbDpgService.queryPurchaseGoodsWeightTotal(map);
            v = doutbleCostV / doutbleCost;
            perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            maxPrice = gbDpgService.queryPurGoodsMaxPrice(map);
            minPrice = gbDpgService.queryPurGoodsMinPrice(map);
//            purchaseGoodsEntities = gbDpgService.queryPurchaseGoodsByParams(map);
            purchaseGoodsEntities = gbDpgService.queryPurchaseGoodsByGoodsId(map);

        }

        Map<String, Object> mapResult = new HashMap<>();

        mapResult.put("totalCost", new BigDecimal(doutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapResult.put("totalCostSubtotal", new BigDecimal(doutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapResult.put("maxPrice", maxPrice);
        mapResult.put("minPrice", minPrice);
        mapResult.put("perPrice", perPrice);
        mapResult.put("purCount", purCount);
        mapResult.put("arr", purchaseGoodsEntities);


        return R.ok().put("data", mapResult);


    }

    @RequestMapping(value = "/depGetToPrintPurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R depGetToPrintPurGoods(Integer depId, Integer userId) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("purDepId", depId);
        map4.put("buyStatus", 1);
        map4.put("weightId", -1);
        map4.put("purchaseType", 0);
        System.out.println("map4444444" + map4);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDpgService.queryDisPurchaseGoods(map4);

        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("status", 1);
        map.put("type", 2);
        int countWeight = gbDistributerWeightTotalService.queryDepWeightCountByParams(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depId);
        map3.put("date", formatWhatDay(0));
        map3.put("type", 2);
        int count = gbDistributerWeightTotalService.queryDepWeightCountByParams(map3);
        BigDecimal trade = new BigDecimal(count).add(new BigDecimal(1));
        String name = "CGD";

        String s = formatWhatDayString(0) + name + "-" + trade;

        Map<String, Object> map1 = new HashMap<>();
        map1.put("arr", fatherGoodsEntities);
        map1.put("weightTotal", countWeight);
        map1.put("tradeNo", s);


        return R.ok().put("data", map1);
    }


    @RequestMapping(value = "/getPurchasePurCash", method = RequestMethod.POST)
    @ResponseBody
    public R getPurchasePurCash(Integer userId, String date) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("finishDate", date);
        map.put("payType", 0);
        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDpgService.queryPurchaseGoodsByParams(map);

        Map<String, Object> map1 = new HashMap<>();
        if (goodsEntities.size() > 0) {
            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
            map1.put("total", aDouble);
            map1.put("arr", goodsEntities);

        } else {
            map1.put("total", 0);
            map1.put("arr", new ArrayList<>());
        }

        return R.ok().put("data", map1);
    }


    @RequestMapping(value = "/getDepPurchaserDateBill", method = RequestMethod.POST)
    @ResponseBody
    public R getDepPurchaserDateBill(Integer depId, String startDate, String stopDate) {

        Map<String, Object> mapBill = new HashMap<>();
        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depId);
        mapBill.put("issueDepId", depId);
        mapBill.put("issueType", gbDepartmentEntity.getGbDepartmentType());
        mapBill.put("startDate", startDate);
        mapBill.put("stopDate", stopDate);
        List<GbDepartmentBillEntity> billEntityList = gbDepartmentBillService.queryBillsByParamsGb(mapBill);
        Double aDouble1 = 0.0;
        if(billEntityList.size() > 0){
             aDouble1 = gbDepartmentBillService.queryGbDepBillsSubTotal(mapBill);
        }

        System.out.println("adfkafaslkfaaDouble1aDouble1" + aDouble1);

        //购买采购商品 batchId == -1
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", depId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("payType", 0);
        map.put("batchId", -1);
        Integer integer = gbDpgService.queryGbPurchaseGoodsCount(map);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDpgService.queryPurchaseGoodsByParams(map);
        BigDecimal maileTotal = new BigDecimal(0);
        if (integer > 0) {
            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
            System.out.println("mappapap" + map);
            maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }


        List<GbDistributerPurchaseBatchEntity> entitiesCash = gbDPBService.queryDisPurchaseBatch(map);
        BigDecimal batchCashTotal = new BigDecimal(0);
        if (entitiesCash.size() > 0) {
            Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
            batchCashTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            batchCashTotal = new BigDecimal(0);
        }

        map.put("payType", 1);
        List<GbDistributerPurchaseBatchEntity> entitiesBill = gbDPBService.queryDisPurchaseBatch(map);
        BigDecimal batchBillTotal = new BigDecimal(0);
        if (entitiesBill.size() > 0) {
            Double aDouble = gbDPBService.queryPurchaserCashTotal(map);
            batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            batchBillTotal = new BigDecimal(0);
        }

        BigDecimal add = maileTotal.add(batchCashTotal).add(batchBillTotal).setScale(2, BigDecimal.ROUND_HALF_UP);

        Map<String, Object> map123 = new HashMap<>();
        map123.put("billArr", billEntityList);
        map123.put("total", add);
        map123.put("maileTotal", maileTotal);
        map123.put("batchCashTotal", batchCashTotal);
        map123.put("batchBillTotal", batchBillTotal);
        map123.put("maileArr", purchaseGoodsEntities);
        map123.put("batchCashArr", entitiesCash);
        map123.put("batchBillArr", entitiesBill);
        map123.put("billTotal", new BigDecimal(aDouble1).setScale(1, BigDecimal.ROUND_HALF_UP));

        return R.ok().put("data", map123);
    }

    @RequestMapping(value = "/getPurchaserDateBill", method = RequestMethod.POST)
    @ResponseBody
    public R getPurchaserDateBill(Integer purDepId, String startDate, String stopDate) {

        //购买采购商品batchId == -1
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", purDepId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("batchId", -1);
        map.put("payType", 0);
        map.put("dayuStatus", 1);
        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDpgService.queryPurchaseGoodsByParams(map);

        BigDecimal maileTotal = new BigDecimal(0);
        if (goodsEntities.size() > 0) {
            Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
            maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            maileTotal = new BigDecimal(0);
        }


        //现金订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("purDepId", purDepId);
        map1.put("startDate", startDate);
        map1.put("stopDate", stopDate);
        map1.put("payType", 0);
        List<GbDistributerPurchaseBatchEntity> entitiesCash = gbDPBService.queryDisPurchaseBatch(map1);
        BigDecimal batchCashTotal = new BigDecimal(0);
        if (entitiesCash.size() > 0) {

            Double aDouble = gbDPBService.queryPurchaserCashTotal(map1);
            batchCashTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            batchCashTotal = new BigDecimal(0);
        }


        Map<String, Object> map2 = new HashMap<>();
        map2.put("purDepId", purDepId);
        map2.put("startDate", startDate);
        map2.put("stopDate", stopDate);
        map2.put("payType", 1);
        List<GbDistributerPurchaseBatchEntity> entitiesBill = gbDPBService.queryDisPurchaseBatch(map2);
        BigDecimal batchBillTotal = new BigDecimal(0);
        if (entitiesBill.size() > 0) {
            Double aDouble = gbDPBService.queryPurchaserCashTotal(map2);
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


    @RequestMapping(value = "/getDisGoodsPriceDayData", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsPriceDayData(Integer disGoodsId, String startDate, String stopDate, String ids) {
        System.out.println(stopDate + "stopDatewhatis");
        String[] arr = ids.split(",");
        TreeSet<String> list = new TreeSet<>();
        List<GbDepartmentEntity> departmentEntities = new ArrayList<>();
        try {
            for (String fatherID : arr) {
                GbDepartmentEntity fatherDep = gbDepartmentService.queryObject(Integer.valueOf(fatherID));
                Integer howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
                List<String> aaa = new ArrayList<>();
                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat format11 = new SimpleDateFormat("dd");
                    Date date = null;
                    date = format.parse(startDate);
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(date);
                    calendar.add(calendar.DATE, i);
                    date = calendar.getTime();
                    String format1 = format.format(date);
                    String format2 = format11.format(date);
                    list.add(format2);
                    Integer gbDepartmentId = fatherDep.getGbDepartmentId();
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("depFatherId", gbDepartmentId);
                    map1.put("date", format1);
                    map1.put("disGoodsId", disGoodsId);
                    String aDouble = gbDpgService.queryPurchaseGoodsPrice(map1);
                    if (aDouble == null) {
                        aDouble = "0.0";
                    }
                    aaa.add(aDouble);
                }
                fatherDep.setDayData(aaa);
                departmentEntities.add(fatherDep);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Map<String, Object> map3 = new HashMap<>();
        map3.put("list", list);
        map3.put("arr", departmentEntities);
        return R.ok().put("data", map3);
    }


    @RequestMapping(value = "/getDistributerGoodsPurchaseList", method = RequestMethod.POST)
    @ResponseBody
    public R getDistributerGoodsPurchaseList(String startDate, String stopDate, String ids,
                                             Integer totalPurchase, Integer lowerPurchase, Integer higherPurchase) {

        String[] arr = ids.split(",");
        List<GbDepartmentEntity> dataDeps = new ArrayList<>();
        for (String id : arr) {
            GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(id));
            // totalPurchase
            if (totalPurchase == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("purDepId", id);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                System.out.println(map);
                List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDpgService.queryPurchaseGoodsByParams(map);
                if (goodsEntities.size() > 0) {
                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                    departmentEntity.setDepPurchaseTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            // higherPurchase
            if (higherPurchase == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", id);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                map.put("purWhat", 1);
                int highestAmount = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
                if (highestAmount > 0) {
                    Double aDouble = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                    departmentEntity.setDepPurchaseHigherTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            // lowerPurchase
            if (lowerPurchase == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", id);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                map.put("purWhat", 0);
                int lowerAmout = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
                if (lowerAmout > 0) {
                    Double aDouble = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                    departmentEntity.setDepPurchaseLowerTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }

            dataDeps.add(departmentEntity);
        }

        return R.ok().put("data", dataDeps);
    }


    @RequestMapping(value = "/getDistributerGoodsPurchaseData", method = RequestMethod.POST)
    @ResponseBody
    public R getDistributerGoodsPurchaseData(String startDate, String stopDate, String ids,
                                             Integer totalPurchase, Integer lowerPurchase, Integer higherPurchase) {

        String[] arr = ids.split(",");
        List<GbDepartmentEntity> dataDeps = new ArrayList<>();
        for (String id : arr) {
            GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(id));
            // totalPurchase
            if (totalPurchase == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("purDepId", id);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDpgService.queryPurchaseGoodsByParams(map);
                if (goodsEntities.size() > 0) {
                    Double aDouble = gbDpgService.queryPurchaseGoodsSubTotal(map);
                    departmentEntity.setDepPurchaseTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            // higherPurchase
            if (higherPurchase == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", id);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                map.put("purWhat", 1);
                int highestAmount = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
                if (highestAmount > 0) {
                    Double aDouble = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                    departmentEntity.setDepPurchaseHigherTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            // lowerPurchase
            if (lowerPurchase == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", id);
                map.put("startDate", startDate);
                map.put("stopDate", stopDate);
                map.put("purWhat", 0);
                int lowerAmout = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
                if (lowerAmout > 0) {
                    Double aDouble = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                    departmentEntity.setDepPurchaseLowerTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }

            dataDeps.add(departmentEntity);
        }

        return R.ok().put("data", dataDeps);
    }


    @RequestMapping(value = "/getDistributerGoodsPurchaseManage", method = RequestMethod.POST)
    @ResponseBody
    public R getDistributerGoodsPurchaseManage(String startDate, String stopDate, Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("disId", disId);
        List<GbDistributerGoodsPriceEntity> entities = gbDistributerGoodsPriceService.queryPriceGoodsListByParams(map);


//        List<GbDepartmentEntity> dataDeps = new ArrayList<>();
//        Map<String, Object> map111 = new HashMap<>();
//        map111.put("disId", disId);
//        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map111);
//        System.out.println(departmentEntities.size() + "denpsisiisis");
//        for (GbDepartmentEntity departmentEntity : departmentEntities) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("depId", departmentEntity.getGbDepartmentId());
//                map.put("startDate", startDate);
//                map.put("stopDate", stopDate);
//                map.put("purWhat", 1);
//                int highestAmount = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
//                if (highestAmount > 0) {
//                    Double aDouble = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
//                    departmentEntity.setDepPurchaseHigherTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
//                }
//
//            // lowerPurchase
//                Map<String, Object> map1 = new HashMap<>();
//                map1.put("depId", departmentEntity.getGbDepartmentId());
//                map1.put("startDate", startDate);
//                map1.put("stopDate", stopDate);
//                map1.put("purWhat", 0);
//                int lowerAmout = gbDistributerGoodsPriceService.queryPriceWhatAmount(map1);
//                if (lowerAmout > 0) {
//                    Double aDouble = gbDistributerGoodsPriceService.queryPriceWhatTotal(map1);
//                    departmentEntity.setDepPurchaseLowerTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
//                }
//
//         if(highestAmount > 0 || lowerAmout > 0){
//             dataDeps.add(departmentEntity);
//         }
//        }
//
        return R.ok().put("data", entities);
    }


    @RequestMapping(value = "/getJicaiPurchaseAccountData")
    @ResponseBody
    public R getJicaiPurchaseAccountData(Integer depId, String startDate, String stopDate) {

        Double oneTotal = getInventoryPurchaseData(depId, startDate, stopDate, 1);
        Double oneTotal1 = getInventoryPurchaseData(depId, startDate, stopDate, 2);
        Double oneTotal2 = getInventoryPurchaseData(depId, startDate, stopDate, 3);
        Double total = oneTotal + oneTotal1 + oneTotal2;

        String format = String.format("%.2f", total);
        Map<String, Object> map = new HashMap<>();
        map.put("total", format);
        map.put("one", String.format("%.2f", oneTotal));
        map.put("two", String.format("%.2f", oneTotal1));
        map.put("three", String.format("%.2f", oneTotal2));
        return R.ok().put("data", map);
    }

    private Double getInventoryPurchaseData(Integer depId, String startDate, String stopDate, Integer type) {

        Double total = 0.0;
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("type", type);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDpgService.queryPurchaseInventoryGoodsList(map);
        if (purchaseGoodsEntities.size() > 0) {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("depId", depId);
            map1.put("startDate", startDate);
            map1.put("stopDate", stopDate);
            map1.put("type", type);
            total = gbDpgService.queryPurchaseInventoryGoodsSubTotal(map1);
        }
        return total;
    }

    @RequestMapping(value = "/getJicaiMangeData")
    @ResponseBody
    public R getJicaiMangeData(Integer depId, Integer disId) {

        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depId);
        int settleWeek = Integer.parseInt(departmentEntity.getGbDepartmentSettleWeek());
        int weekOfYear = getWeekOfYear(0) + 1;
        int start = weekOfYear - 4;
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = start; i < weekOfYear; i++) {
            Map<String, Object> weekJicaiData = getWeekJicaiData(i, depId);
            result.add(weekJicaiData);
        }
        return R.ok().put("data", result);

    }


    private Map<String, Object> getWeekJicaiData(int i, Integer depId) {

        String formatSubtotal = "0.0";
        String formathighestTotal = "0.0";
        String formatlowestTotal = "0.0";
        Map<String, Object> map5 = new HashMap<>();

        //采购总额
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", depId);
        map.put("status", 4);
        map.put("week", i);
        int i1 = gbDpgService.queryPurchaseGoodsAmount(map);
        if (i1 > 0) {
            Double subTotal = gbDpgService.queryPurchaseGoodsSubTotal(map);
            formatSubtotal = String.format("%.2f", subTotal);
        }


        //2, highest
        //价格高总额
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depId", depId);
        map1.put("status", 3);
        map1.put("purWhat", 1);
        map1.put("week", i);
        int highestAmount = gbDistributerGoodsPriceService.queryPriceWhatAmount(map1);
        if (highestAmount > 0) {
            Double highestTotal = gbDistributerGoodsPriceService.queryPriceWhatTotal(map1);
            formathighestTotal = String.format("%.2f", highestTotal);
        }

        //3. lowest
        //价格低总额
        Map<String, Object> map2 = new HashMap<>();
        map2.put("depId", depId);
        map2.put("status", 3);
        map2.put("purWhat", -1);
        map2.put("week", i);
        int lowestAmount = gbDistributerGoodsPriceService.queryPriceWhatAmount(map2);
        if (lowestAmount > 0) {
            Double lowestTotal = gbDistributerGoodsPriceService.queryPriceWhatTotal(map2);
            formatlowestTotal = String.format("%.2f", lowestTotal);
        }

        map5.put("subtotal", formatSubtotal);
        map5.put("highestTotal", formathighestTotal);
        map5.put("lowestTotal", formatlowestTotal);

        Map<String, Object> map4 = new HashMap<>();
        map4.put("week", i);
        map4.put("arr", map5);

        return map4;

    }


    @RequestMapping(value = "/getPurchasePurGoods/{userId}")
    @ResponseBody
    public R getPurchasePurGoods(@PathVariable Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("month", formatWhatMonth(0));
        map.put("dayuStatus", 1);
        map.put("batchId", -1);
        System.out.println("purgogogoogo" + map);
        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("month", formatWhatMonth(0));
        map1.put("arr", gbDistributerPurchaseGoodsEntities);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("purUserId", userId);
        map2.put("month", getLastMonth());
        map2.put("dayuStatus", 1);
        map2.put("batchId", -1);

        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities2 = gbDpgService.queryPurchaseGoodsWithDetailByParams(map2);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("month", getLastMonth());
        map3.put("arr", gbDistributerPurchaseGoodsEntities2);

        Map<String, Object> map4 = new HashMap<>();
        map4.put("purUserId", userId);
        map4.put("month", getLastTwoMonth());
        map4.put("dayuStatus", 1);
        map4.put("batchId", -1);
        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities3 = gbDpgService.queryPurchaseGoodsWithDetailByParams(map4);
        Map<String, Object> map5 = new HashMap<>();
        map5.put("month", getLastTwoMonth());
        map5.put("arr", gbDistributerPurchaseGoodsEntities3);

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(map1);
        result.add(map3);
        result.add(map5);
        return R.ok().put("data", result);
    }

    @RequestMapping(value = "/getGbDepartmentPurGoods/{depFatherId}")
    @ResponseBody
    public R getGbDepartmentPurGoods(@PathVariable Integer depFatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", depFatherId);
        map.put("month", formatWhatMonth(0));
        map.put("dayuStatus", 1);
        map.put("batchId", -1);
        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("month", formatWhatMonth(0));
        map1.put("arr", gbDistributerPurchaseGoodsEntities);

        Map<String, Object> map2 = new HashMap<>();
        map.put("purDepId", depFatherId);
        map2.put("month", getLastMonth());
        map2.put("dayuStatus", 1);

        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities2 = gbDpgService.queryPurchaseGoodsWithDetailByParams(map2);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("month", getLastMonth());
        map3.put("arr", gbDistributerPurchaseGoodsEntities2);

        Map<String, Object> map4 = new HashMap<>();
        map.put("purDepId", depFatherId);
        map4.put("month", getLastTwoMonth());
        map4.put("dayuStatus", 1);
        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities3 = gbDpgService.queryPurchaseGoodsWithDetailByParams(map4);
        Map<String, Object> map5 = new HashMap<>();
        map5.put("month", getLastTwoMonth());
        map5.put("arr", gbDistributerPurchaseGoodsEntities3);

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(map1);
        result.add(map3);
        result.add(map5);
        return R.ok().put("data", result);
    }

    @RequestMapping(value = "/mendainGetDateSelfPurchaseGoods", method = RequestMethod.POST)
    @ResponseBody
    public R mendainGetDateSelfPurchaseGoods(Integer depFatherId, String date, Integer payType) {
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", depFatherId);
        map.put("finishDate", date);
        map.put("payType", payType);

        List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);

        return R.ok().put("data", gbDistributerPurchaseGoodsEntities);
    }


    @RequestMapping(value = "/disGetHaveFinishedPurGoodsGb/{disId}")
    @ResponseBody
    public R disGetHaveFinishedPurGoodsGb(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("equalStatus", 1);
        map.put("purchaseType", 0);
        List<GbDistributerPurchaseGoodsEntity> disPurGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);
        return R.ok().put("data", disPurGoodsEntities);
    }

    @RequestMapping(value = "/purchaserGetHaveFinishedPurGoodsGb")
    @ResponseBody
    public R purchaserGetHaveFinishedPurGoodsGb(Integer userId, Integer orderType, Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("equalStatus", 1);
        map.put("batchId", -1);
        List<GbDistributerPurchaseGoodsEntity> disPurGoodsEntities = gbDpgService.queryPurchaseGoodsWithDetailByParams(map);
//
        Map<String, Object> map11 = new HashMap<>();
        map11.put("purDepId", depId);
        map11.put("status", 1);
        int count0 = gbDpgService.queryPurchaseGoodsAmount(map11);

        Map<String, Object> map12 = new HashMap<>();
        map12.put("purUserId", userId);
        map12.put("status", 2);
        Integer count1 = gbDPBService.queryDisPurchaseBatchCount(map12);

        Map<String, Object> map13 = new HashMap<>();
        map13.put("purUserId", userId);
        map13.put("equalStatus", 1);
        map13.put("batchId", -1);
        Integer count2 = gbDpgService.queryGbPurchaseGoodsCount(map13);

        Map<String, Object> map14 = new HashMap<>();
        map14.put("toDepId", depId);
        map14.put("equalStatus", 2);
        map14.put("orderType", orderType);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryDistributerTodayDepartments(map14);
        Map<String, Object> todayData = packageDisOrderByDep(departmentEntities, 0);
        List<GbDepartmentEntity> arr = (List<GbDepartmentEntity>) todayData.get("arr");

        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", disPurGoodsEntities);
        map3.put("purGoodsAmount", count0);
        map3.put("haoyouAmount", count1);
        map3.put("finishAmount", count2);
        map3.put("deliveryAmount", arr.size());

        return R.ok().put("data", map3);
    }


    @RequestMapping(value = "/getDatePurchaseGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getDatePurchaseGoods(Integer disId, String date) {
        System.out.println(date);
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purchaseDate", date);
        List<GbDistributerFatherGoodsEntity> purchase = gbDpgService.queryDisPurchaseGoods(map);
        return R.ok().put("data", purchase);
    }

    @RequestMapping(value = "/supplierGetPurchaseGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGetPurchaseGoodsGb(Integer depId, Integer supplierId) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("toDepId", depId);
        map4.put("status", 3);
        map4.put("supplierId", supplierId);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoods(map4);
        return R.ok().put("data", purchaseToday);
    }

    @RequestMapping(value = "/getPurchaseGoodsGbWithOutSupplier", method = RequestMethod.POST)
    @ResponseBody
    public R getPurchaseGoodsGbWithOutSupplier(Integer depId, Integer userId, Integer orderType) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("toDepId", depId);
        map4.put("status", 1);
        map4.put("supplierId", -1);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoods(map4);
        return R.ok().put("data", purchaseToday);
    }

    @RequestMapping(value = "/getPurchaseGoodsGb/{depId}")
    @ResponseBody
    public R getPurchaseGoodsGb(@PathVariable Integer depId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("toDepId", depId);
        map4.put("buyStatus", 1);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoods(map4);

        return R.ok().put("data", purchaseToday);
    }




    @RequestMapping(value = "/nxDisSavePurGoodsPrice", method = RequestMethod.POST)
    @ResponseBody
    public R nxDisSavePurGoodsPrice(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
        for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
            NxDepartmentOrdersEntity nxDepartmentOrdersEntity = gbDepartmentOrdersEntity.getNxDepartmentOrdersEntity();
            nxDepartmentOrdersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
            nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);
            gbDepartmentOrdersEntity.setGbDoPrice(nxDepartmentOrdersEntity.getNxDoPrice());
            gbDepartmentOrdersEntity.setGbDoWeight(nxDepartmentOrdersEntity.getNxDoWeight());
            gbDepartmentOrdersEntity.setGbDoSubtotal(nxDepartmentOrdersEntity.getNxDoSubtotal());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasFinishPurGoods());
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);

        }

        purGoods.setGbDpgStatus(2);
        purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
        purGoods.setGbDpgPurchaseType(2);
        purGoods.setGbDpgPayType(1);
        purGoods.setGbDpgTime(formatWhatTime(0));
        purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
        purGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
        purGoods.setGbDpgPurchaseYear(formatWhatYear(0));
        purGoods.setGbDpgPurchaseWeek(getWeek(0));
        purGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
        gbDpgService.update(purGoods);
        Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }

        //判断是否有保鲜时间参数
        Integer gbDoDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
            int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            purGoods.setGbDpgWarnFullTime(formatWhatFullTime(warnHour));
            purGoods.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
            gbDpgService.update(purGoods);
        }

        return R.ok();
    }


    @RequestMapping(value = "/nxDisSavePurGoodsPrice1", method = RequestMethod.POST)
    @ResponseBody
    public R nxDisSavePurGoodsPrice1(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
        BigDecimal weightTotal = new BigDecimal(0);
        for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
            NxDepartmentOrdersEntity nxDepartmentOrdersEntity = gbDepartmentOrdersEntity.getNxDepartmentOrdersEntity();
            Integer nxDoStatus = nxDepartmentOrdersEntity.getNxDoPurchaseStatus();
            if (nxDoStatus == 3) {
                weightTotal = weightTotal.add(new BigDecimal(nxDepartmentOrdersEntity.getNxDoWeight()));
                nxDepartmentOrdersEntity.setNxDoStatus(2);
            }
            nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);
        }
        purGoods.setGbDpgBatchId(-1);
        purGoods.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement()); // 1
        purGoods.setGbDpgPayType(1);
        purGoods.setGbDpgTime(formatWhatTime(0));
        purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
        purGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
        purGoods.setGbDpgPurchaseYear(formatWhatYear(0));
        purGoods.setGbDpgPurchaseWeek(getWeek(0));
        purGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
        purGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
        purGoods.setGbDpgBuyQuantity(weightTotal.toString());
        BigDecimal decimal = new BigDecimal(purGoods.getGbDpgBuyPrice()).multiply(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP);
        purGoods.setGbDpgBuySubtotal(decimal.toString());
        purGoods.setGbDpgStatus(getGbPurchaseGoodsStatusFinished()); // 2
        Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }

        gbDpgService.update(purGoods);

        return R.ok();
}

    @RequestMapping(value = "/nxDisGetPurchaseGoodsGb/{depId}")
    @ResponseBody
    public R nxDisGetPurchaseGoodsGb(@PathVariable Integer depId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("toDepId", depId);
        map4.put("purStatus", 4);
        System.out.println("abckckckc" + map4);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoodsForNxDis(map4);

        return R.ok().put("data", purchaseToday);
    }


    /**
     * DISTRIBUTE
     * 批发商获取进货商品列表
     *
     * @param
     * @return 进货商品列表
     */
    @RequestMapping(value = "/getPurchaseGoodsGbWithTabCount")
    @ResponseBody
    public R getPurchaseGoodsGbWithTabCount(Integer depId, Integer userId, Integer orderType) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("toDepId", depId);
        map4.put("buyStatus", 1);
        map4.put("dayuBuyStatus", -2);
        map4.put("status", 3);
        System.out.println("map4444444====="+ map4);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoods(map4);

        //peisongshang
        map4.put("nxDis", 1);
        List<NxDistributerEntity> distributerEntities = gbDepartmentOrdersService.queryGbDepNxDistributerOrder(map4);


        Map<String, Object> map1 = new HashMap<>();
        map1.put("purDepId", depId);
        map1.put("status", 1);
        System.out.println("fafdaafmap1map1map1" + map1);
        int count0 = gbDpgService.queryPurchaseGoodsAmount(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("purUserId", userId);
        map2.put("status", 2);
        Integer count1 = gbDPBService.queryDisPurchaseBatchCount(map2);

        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("equalStatus", 1);
        map.put("batchId", -1);
        Integer count2 = gbDpgService.queryGbPurchaseGoodsCount(map);

        Map<String, Object> map14 = new HashMap<>();
        map14.put("toDepId", depId);
        map14.put("equalStatus", 2);
        map14.put("orderType", orderType);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryDistributerTodayDepartments(map14);
        Map<String, Object> todayData = packageDisOrderByDep(departmentEntities, 0);
        List<GbDepartmentEntity> arr = (List<GbDepartmentEntity>) todayData.get("arr");

        Map<String, Object> mapw = new HashMap<>();
        mapw.put("userId", userId);
        mapw.put("status", 1);
        mapw.put("type", 2);
        int count = gbDistributerWeightTotalService.queryDepWeightCountByParams(mapw);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", purchaseToday);
        map3.put("nxDis", distributerEntities);
        map3.put("purGoodsAmount", count0);
        map3.put("haoyouAmount", count1);
        map3.put("finishAmount", count2);
        map3.put("deliveryAmount", arr.size());
        map3.put("printTotal", count);


        return R.ok().put("data", map3);
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


    @RequestMapping(value = "/mendianGetSelfPurchaseGoods/{depFatherId}")
    @ResponseBody
    public R mendianGetSelfPurchaseGoods(@PathVariable Integer depFatherId) {

        Map<String, Object> map1 = new HashMap<>();

        map1.put("fatherDepId", depFatherId);
        map1.put("toDepId", depFatherId);
//        map1.put("orderType", getGbOrderTypeZiCai());
        map1.put("status", 3);
        List<GbDistributerFatherGoodsEntity> purchaseToday = gbDpgService.queryDisPurchaseGoods(map1);
        Map<String, Object> map4 = new HashMap<>();
        map4.put("depId", depFatherId);
        map4.put("equalStatus", -1);
        Integer amount = gbDepartmentBillService.queryBillsCountByParamsGb(map4);
        Map<String, Object> map = new HashMap<>();
        map.put("arr", purchaseToday);
        map.put("newAmount", amount);
        return R.ok().put("data", map);
    }

    /**
     * DISTRIBUTE
     * 批发商获取进货商品列表
     *
     * @param
     * @return 进货商品列表
     */
    @RequestMapping(value = "/getStockPurchaseGoodsGb/{depId}")
    @ResponseBody
    public R getStockPurchaseGoodsGb(@PathVariable Integer depId) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("depFatherId", depId);
        map4.put("status", 4);
        map4.put("toDepId", depId);
        System.out.println("roroorrr444444" + map4);
        //
        List<GbDistributerGoodsShelfEntity> shelfEntities = gbDisGoodsShelfService.queryStockOrdersByParams(map4);
//        List<GbDistributerGoodsShelfEntity> shelfEntities = gbDisGoodsShelfService.queryShelfGoodsWithPurOrder(map4);
        List<GbDistributerGoodsShelfGoodsEntity> shelfGoodsEntities = new ArrayList<>();
        List<GbDistributerGoodsShelfEntity> shelfEntityList = new ArrayList<>();
        GbDistributerGoodsShelfEntity shelfGoodsEntity = new GbDistributerGoodsShelfEntity();
        for (GbDistributerGoodsShelfEntity shelf : shelfEntities) {
            Integer gbDistributerGoodsShelfId = shelf.getGbDistributerGoodsShelfId();
            if (gbDistributerGoodsShelfId == null) {
                shelfGoodsEntities.add(shelf.getGbDisGoodsShelfGoodsEntities().get(0));
            } else {
                shelfEntityList.add(shelf);
            }
        }
        if (shelfGoodsEntities.size() > 0) {
            shelfGoodsEntity.setGbDisGoodsShelfGoodsEntities(shelfGoodsEntities);
            shelfEntityList.add(shelfGoodsEntity);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("status", 1);
        map.put("type", 2);
        int count = gbDistributerWeightTotalService.queryDepWeightCountByParams(map);


        Map<String, Object> map1 = new HashMap<>();
        map1.put("arr", shelfEntityList);
        map1.put("weightTotal", count);


        return R.ok().put("data", map1);
    }

    @RequestMapping(value = "/getStockPurchaseGoodsGb0/{depId}")
    @ResponseBody
    public R getStockPurchaseGoodsGb0(@PathVariable Integer depId) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("depFatherId", depId);
        map4.put("status", 3);
        map4.put("toDepId", depId);
        System.out.println("roroorrr444444" + map4);
        List<GbDistributerGoodsShelfEntity> shelfEntities = gbDisGoodsShelfService.queryStockOrdersByParams(map4);
        List<GbDistributerGoodsShelfGoodsEntity> shelfGoodsEntities = new ArrayList<>();
        List<GbDistributerGoodsShelfEntity> shelfEntityList = new ArrayList<>();
        GbDistributerGoodsShelfEntity shelfGoodsEntity = new GbDistributerGoodsShelfEntity();
        for (GbDistributerGoodsShelfEntity shelf : shelfEntities) {
            Integer gbDistributerGoodsShelfId = shelf.getGbDistributerGoodsShelfId();
            if (gbDistributerGoodsShelfId == null) {
                shelfGoodsEntities.add(shelf.getGbDisGoodsShelfGoodsEntities().get(0));
            } else {
                shelfEntityList.add(shelf);
            }
        }
        if (shelfGoodsEntities.size() > 0) {
            shelfGoodsEntity.setGbDisGoodsShelfGoodsEntities(shelfGoodsEntities);
            shelfEntityList.add(shelfGoodsEntity);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("equalStatus", -1);
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryBillsByParamsGb(map);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("depId", depId);
        mapR.put("status", 3);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(mapR);
        System.out.println("fdajaslfidasdniasfsalliangiweiwewieiei================");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("toDepId", depId);
        map2.put("buyStatus", 1);
        map2.put("dayuStatus", -1);
        map2.put("status", 3);
        map2.put("isNotSelf", 1);
        Integer amount2 = gbDepartmentOrdersService.queryTotalByParams(map2);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("arr", shelfEntityList);
        map1.put("billTotal", billEntities.size());
        map1.put("orderTotal", ordersEntities.size());
        map1.put("amount", amount2);


        return R.ok().put("data", map1);
    }


    @RequestMapping(value = "/cancleFinishPurGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R cancleFinishPurGoodsGb(@RequestBody GbDistributerPurchaseGoodsEntity purgoods) {
        List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getGbDepartmentOrdersEntities();

        for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            orders.setGbDoStatus(getGbOrderStatusNew());
            orders.setGbDoBuyStatus(getGbOrderBuyStatusNew());
            orders.setGbDoPurchaseUserId(-1);
            orders.setGbDoPrice(null);
            orders.setGbDoWeight(null);
            orders.setGbDoScalePrice(null);
            orders.setGbDoScaleWeight(null);
            orders.setGbDoSubtotal(null);
            orders.setGbDoWeightGoodsId(null);
            gbDepartmentOrdersService.update(orders);

        }


// check goodsPrice

        Integer gbDpgDisGoodsPriceId = purgoods.getGbDpgDisGoodsPriceId();
        if (gbDpgDisGoodsPriceId != null) {
            System.out.println("dellelele" + purgoods.getGbDpgDisGoodsPriceId());
            gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);
        }
        purgoods.setGbDpgDisGoodsPriceId(null);
        purgoods.setGbDpgBatchId(-1);
        purgoods.setGbDpgBuyPrice(null);
        purgoods.setGbDpgBuyQuantity(null);
        purgoods.setGbDpgBuySubtotal(null);
        purgoods.setGbDpgBuyPriceReason(null);
        purgoods.setGbDpgStatus(0);
        purgoods.setGbDpgTime(null);
        purgoods.setGbDpgPurchaseDate(null);
        purgoods.setGbDpgPurchaseType(null);
        purgoods.setGbDpgPurchaseMonth(null);
        purgoods.setGbDpgPurchaseYear(null);
        purgoods.setGbDpgPurchaseWeek(null);
        purgoods.setGbDpgPurchaseWeekYear(null);
        purgoods.setGbDpgPurchaseFullTime(null);
        purgoods.setGbDpgBuyScaleQuantity(null);
        purgoods.setGbDpgBuyScalePrice(null);
        purgoods.setGbDpgWeightId(null);
        purgoods.setGbDpgPurchaseType(null);
        purgoods.setGbDpgPurUserId(null);
        gbDpgService.update(purgoods);


        return R.ok();
    }


    /**
     * 采购员完成采购
     *
     * @param purgoods
     * @return
     */
    @RequestMapping(value = "/markGbPurGoodsFinish", method = RequestMethod.POST)
    @ResponseBody
    public R markGbPurGoodsFinish(@RequestBody GbDistributerPurchaseGoodsEntity purgoods) {

        List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getGbDepartmentOrdersEntities();
        List<GbDepartmentOrdersEntity> unChoiceOrderList = new ArrayList<>();
        int finishOrder = 0;
        BigDecimal purQuantity = new BigDecimal(0);
        for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            Boolean hasChoice = orders.getHasChoice();
            if (hasChoice) {
                finishOrder = finishOrder + 1;
                purQuantity = purQuantity.add(new BigDecimal(orders.getGbDoQuantity()).setScale(1, BigDecimal.ROUND_HALF_UP));
                orders.setGbDoStatus(getGbOrderStatusProcurement());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusProcurement());
                orders.setGbDoPurchaseUserId(purgoods.getGbDpgPurUserId());
                gbDepartmentOrdersService.update(orders);

            } else {
                unChoiceOrderList.add(orders);
            }
        }

        purgoods.setGbDpgQuantity(purQuantity.toString());
        purgoods.setGbDpgOrdersAmount(finishOrder);
        purgoods.setGbDpgBatchId(-1);
        purgoods.setGbDpgStatus(1);
        purgoods.setGbDpgPayType(0);
        purgoods.setGbDpgTime(formatWhatTime(0));
        purgoods.setGbDpgPurchaseDate(formatWhatDay(0));
        purgoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
        purgoods.setGbDpgPurchaseYear(formatWhatYear(0));
        purgoods.setGbDpgPurchaseWeek(getWeek(0));
        purgoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
        purgoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
        Integer gbDoDisGoodsId = purgoods.getGbDpgDisGoodsId();

        //判断是否有保鲜时间参数
        GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
            int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            purgoods.setGbDpgWarnFullTime(formatWhatFullTime(warnHour));
            purgoods.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
        }
        gbDpgService.update(purgoods);

        //给没有选择订单新生成一个采购商品
        if (unChoiceOrderList.size() > 0) {
            GbDistributerPurchaseGoodsEntity newPurGoods = new GbDistributerPurchaseGoodsEntity();
            newPurGoods.setGbDpgDisGoodsFatherId(unChoiceOrderList.get(0).getGbDoDisGoodsFatherId());
            newPurGoods.setGbDpgDisGoodsId(unChoiceOrderList.get(0).getGbDoDisGoodsId());
            newPurGoods.setGbDpgDistributerId(unChoiceOrderList.get(0).getGbDoDistributerId());
            newPurGoods.setGbDpgApplyDate(formatWhatDay(0));
            newPurGoods.setGbDpgStatus(0);
            newPurGoods.setGbDpgOrdersAmount(unChoiceOrderList.size());
            newPurGoods.setGbDpgOrdersFinishAmount(0);
            newPurGoods.setGbDpgPurchaseWeek(getWeek(0));
            newPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            newPurGoods.setGbDpgIsCheck(0);
            newPurGoods.setGbDpgPurchaseDepartmentId(unChoiceOrderList.get(0).getGbDoToDepartmentId());
            newPurGoods.setGbDpgPurchaseNxDistributerId(unChoiceOrderList.get(0).getGbDoNxDistributerId());
            gbDpgService.save(newPurGoods);
            BigDecimal unPurQuantity = new BigDecimal(0);
            for (GbDepartmentOrdersEntity unChoiceOrder : unChoiceOrderList) {
                Integer gbDistributerPurchaseGoodsId = newPurGoods.getGbDistributerPurchaseGoodsId();
                unChoiceOrder.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                gbDepartmentOrdersService.update(unChoiceOrder);
                BigDecimal orderQuantity = new BigDecimal(unChoiceOrder.getGbDoQuantity());
                unPurQuantity = unPurQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);

            }
            newPurGoods.setGbDpgQuantity(unPurQuantity.toString());
            newPurGoods.setGbDpgStandard(unChoiceOrderList.get(0).getGbDoStandard());
            gbDpgService.update(newPurGoods);
        }

        return R.ok();
    }


    @RequestMapping(value = "/updateStockPurGoodsPrice", method = RequestMethod.POST)
    @ResponseBody
    public R updateStockPurGoodsPrice(Integer id, String price) {
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(id);
        purchaseGoodsEntity.setGbDpgBuyPrice(price);
        if (purchaseGoodsEntity.getGbDpgBuyQuantity() != null) {
            BigDecimal quantityB = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
            BigDecimal multiply = quantityB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
            purchaseGoodsEntity.setGbDpgBuySubtotal(multiply.toString());
            purchaseGoodsEntity.setGbDpgStatus(3);
        }
        gbDpgService.update(purchaseGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("purGoodsId", id);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        for (GbDepartmentOrdersEntity orders : ordersEntities) {
            orders.setGbDoPrice(price);
            if (orders.getGbDoWeight() != null) {
                BigDecimal weightB = new BigDecimal(orders.getGbDoWeight());
                BigDecimal multiply = weightB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
                orders.setGbDoSubtotal(multiply.toString());
                orders.setGbDoStatus(getGbOrderStatusHasFinished());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            }
            gbDepartmentOrdersService.update(orders);

        }

        Integer gbDpgDisGoodsId = purchaseGoodsEntity.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purchaseGoodsEntity);
        }

        return R.ok();
    }

    @RequestMapping(value = "/giveStockPurGoodsPrice", method = RequestMethod.POST)
    @ResponseBody
    public R giveStockPurGoodsPrice(Integer id, String price) {
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(id);
        purchaseGoodsEntity.setGbDpgBuyPrice(price);
        if (purchaseGoodsEntity.getGbDpgBuyQuantity() != null) {
            BigDecimal quantityB = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
            BigDecimal multiply = quantityB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
            purchaseGoodsEntity.setGbDpgBuySubtotal(multiply.toString());
            purchaseGoodsEntity.setGbDpgStatus(3);
        } else {
            purchaseGoodsEntity.setGbDpgStatus(2);
        }
        gbDpgService.update(purchaseGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("purGoodsId", id);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        for (GbDepartmentOrdersEntity orders : ordersEntities) {
            orders.setGbDoPrice(price);
            if (orders.getGbDoWeight() != null) {
                BigDecimal weightB = new BigDecimal(orders.getGbDoWeight());
                BigDecimal multiply = weightB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
                orders.setGbDoSubtotal(multiply.toString());
                orders.setGbDoStatus(getGbOrderStatusHasBill());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());

                Integer gbDoWeightTotalId = orders.getGbDoWeightTotalId();
                System.out.println("weitototoidiidd" + gbDoWeightTotalId);
                if (gbDoWeightTotalId != null) {
                    GbDistributerWeightTotalEntity gbWeightTotalEntity = gbDistributerWeightTotalService.queryObject(gbDoWeightTotalId);
                    BigDecimal gbGwtOrderFinishCount = new BigDecimal(gbWeightTotalEntity.getGbGwtOrderFinishCount());
                    BigDecimal add = gbGwtOrderFinishCount.add(new BigDecimal(1));
                    gbWeightTotalEntity.setGbGwtOrderFinishCount(add.toString());
                    if (add.compareTo(new BigDecimal(gbWeightTotalEntity.getGbGwtOrderCount())) == 0) {
                        gbWeightTotalEntity.setGbGwtStatus(getGbWeightTotalStatusFinished());
                    }
                    gbDistributerWeightTotalService.update(gbWeightTotalEntity);
                }

            } else {
                orders.setGbDoStatus(getGbOrderStatusProcurement());
                orders.setGbDoBuyStatus(getGbOrderStatusProcurement());
            }

            orders.setGbDoPurchaseUserId(purchaseGoodsEntity.getGbDpgPurUserId());
            gbDepartmentOrdersService.update(orders);

        }


        Integer gbDpgDisGoodsId = purchaseGoodsEntity.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purchaseGoodsEntity);
        }

        return R.ok();
    }


    @RequestMapping(value = "/giveStockPurGoodsWeight", method = RequestMethod.POST)
    @ResponseBody
    public R giveStockPurGoodsWeight(Integer id, String weight) {
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(id);
        purchaseGoodsEntity.setGbDpgBuyQuantity(weight);
        if (purchaseGoodsEntity.getGbDpgBuyPrice() != null) {
            BigDecimal priceB = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
            BigDecimal multiply = priceB.multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            purchaseGoodsEntity.setGbDpgBuySubtotal(multiply.toString());
            purchaseGoodsEntity.setGbDpgStatus(3);
        } else {
            purchaseGoodsEntity.setGbDpgStatus(2);
        }
        gbDpgService.update(purchaseGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("purGoodsId", id);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        for (GbDepartmentOrdersEntity orders : ordersEntities) {
            orders.setGbDoWeight(weight);
            if (orders.getGbDoPrice() != null) {
                BigDecimal priceB = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                BigDecimal multiply = priceB.multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                orders.setGbDoSubtotal(multiply.toString());
                orders.setGbDoStatus(getGbOrderStatusHasBill());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());

                Integer gbDoWeightTotalId = orders.getGbDoWeightTotalId();
                System.out.println("weitototoidiidd" + gbDoWeightTotalId);
                if (gbDoWeightTotalId != null) {
                    GbDistributerWeightTotalEntity gbWeightTotalEntity = gbDistributerWeightTotalService.queryObject(gbDoWeightTotalId);
                    BigDecimal gbGwtOrderFinishCount = new BigDecimal(gbWeightTotalEntity.getGbGwtOrderFinishCount());
                    BigDecimal add = gbGwtOrderFinishCount.add(new BigDecimal(1));
                    gbWeightTotalEntity.setGbGwtOrderFinishCount(add.toString());
                    if (add.compareTo(new BigDecimal(gbWeightTotalEntity.getGbGwtOrderCount())) == 0) {
                        gbWeightTotalEntity.setGbGwtStatus(getGbWeightTotalStatusFinished());
                    }
                    gbDistributerWeightTotalService.update(gbWeightTotalEntity);
                }
            } else {
                orders.setGbDoStatus(getGbOrderStatusProcurement());
                orders.setGbDoBuyStatus(getGbOrderStatusProcurement());
            }

            orders.setGbDoPurchaseUserId(purchaseGoodsEntity.getGbDpgPurUserId());
            gbDepartmentOrdersService.update(orders);


        }

        return R.ok();
    }

    @RequestMapping(value = "/updateStockPurGoodsWeight", method = RequestMethod.POST)
    @ResponseBody
    public R updateStockPurGoodsWeight(Integer id, String weight) {
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(id);
        purchaseGoodsEntity.setGbDpgBuyQuantity(weight);
        if (purchaseGoodsEntity.getGbDpgBuyPrice() != null) {
            BigDecimal priceB = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
            BigDecimal multiply = priceB.multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            purchaseGoodsEntity.setGbDpgBuySubtotal(multiply.toString());
            purchaseGoodsEntity.setGbDpgStatus(3);
        }

        gbDpgService.update(purchaseGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("purGoodsId", id);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        for (GbDepartmentOrdersEntity orders : ordersEntities) {
            orders.setGbDoWeight(weight);
            if (orders.getGbDoPrice() != null) {
                BigDecimal priceB = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                BigDecimal multiply = priceB.multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                orders.setGbDoSubtotal(multiply.toString());
                orders.setGbDoStatus(getGbOrderStatusHasFinished());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            }
            gbDepartmentOrdersService.update(orders);
        }

        return R.ok();
    }

    /**
     * 采购员记录"完成采购"
     *
     * @param purGoods
     * @return
     */
    @RequestMapping(value = "/saveSelfPurGoodsOrdersCost", method = RequestMethod.POST)
    @ResponseBody
    public R saveSelfPurGoodsOrdersCost(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {

        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
        for (GbDepartmentOrdersEntity orders : gbDepartmentOrdersEntities) {
            orders.setGbDoStatus(getGbOrderStatusHasFinished());
            orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            orders.setGbDoPurchaseUserId(purGoods.getGbDpgPurUserId());
            gbDepartmentOrdersService.update(orders);

            Integer gbDoWeightTotalId = orders.getGbDoWeightTotalId();
            System.out.println("weitototoidiidd" + gbDoWeightTotalId);
            if (gbDoWeightTotalId != null) {
                GbDistributerWeightTotalEntity gbWeightTotalEntity = gbDistributerWeightTotalService.queryObject(gbDoWeightTotalId);
                BigDecimal gbGwtOrderFinishCount = new BigDecimal(gbWeightTotalEntity.getGbGwtOrderFinishCount());
                BigDecimal add = gbGwtOrderFinishCount.add(new BigDecimal(1));
                gbWeightTotalEntity.setGbGwtOrderFinishCount(add.toString());
                if (add.compareTo(new BigDecimal(gbWeightTotalEntity.getGbGwtOrderCount())) == 0) {
                    gbWeightTotalEntity.setGbGwtStatus(getGbWeightTotalStatusFinished());
                }
                gbDistributerWeightTotalService.update(gbWeightTotalEntity);
            }
        }

        purGoods.setGbDpgStatus(2);
        gbDpgService.update(purGoods);
        Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);
        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }

        return R.ok();
    }


    /**
     * 采购员直接完成自采购商品，没有记录完成采购一步。
     * 暂时没有用到
     *
     * @param purGoods
     * @return
     */
    @RequestMapping(value = "/finishSelfPurGoodsOrdersCost", method = RequestMethod.POST)
    @ResponseBody
    public R finishSelfPurGoodsOrdersCost(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        System.out.println("dpgitititmemmmedpgitititmemmmedpgitititmemmme");
        List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
        List<GbDepartmentOrdersEntity> unChoiceOrderList = new ArrayList<>();
        int finishOrder = 0;
        for (GbDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            Boolean hasChoice = orders.getHasChoice();
            if (hasChoice) {
                finishOrder = finishOrder + 1;
                orders.setGbDoStatus(getGbOrderStatusHasFinished());
                orders.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
                orders.setGbDoPurchaseUserId(purGoods.getGbDpgPurUserId());
                gbDepartmentOrdersService.update(orders);

            } else {
                unChoiceOrderList.add(orders);
            }

        }
        purGoods.setGbDpgOrdersAmount(finishOrder);
        purGoods.setGbDpgBatchId(-1);
        purGoods.setGbDpgStatus(2);
        purGoods.setGbDpgPayType(0);
        purGoods.setGbDpgTime(formatWhatTime(0));
        purGoods.setGbDpgPurchaseDate(formatWhatDay(0));
        purGoods.setGbDpgPurchaseMonth(formatWhatMonth(0));
        purGoods.setGbDpgPurchaseYear(formatWhatYear(0));
        purGoods.setGbDpgPurchaseWeek(getWeek(0));
        purGoods.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
        purGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
        Integer gbDpgDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);

        if (gbDistributerGoodsEntity.getGbDgControlPrice() != null && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }
        //判断是否有保鲜时间参数
        Integer gbDoDisGoodsId = purGoods.getGbDpgDisGoodsId();
        GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
            int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
            int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
            purGoods.setGbDpgWarnFullTime(formatWhatFullTime(warnHour));
            purGoods.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
        }
        gbDpgService.update(purGoods);


//        //给没有选择订单新生成一个采购商品
        if (unChoiceOrderList.size() > 0) {
            GbDistributerPurchaseGoodsEntity disGoods = new GbDistributerPurchaseGoodsEntity();
            disGoods.setGbDpgDisGoodsFatherId(unChoiceOrderList.get(0).getGbDoDisGoodsFatherId());
            disGoods.setGbDpgDisGoodsId(unChoiceOrderList.get(0).getGbDoDisGoodsId());
            disGoods.setGbDpgDistributerId(unChoiceOrderList.get(0).getGbDoDistributerId());
            disGoods.setGbDpgApplyDate(formatWhatDay(0));
            disGoods.setGbDpgStatus(0);
            disGoods.setGbDpgQuantity("0");
            disGoods.setGbDpgStandard(unChoiceOrderList.get(0).getGbDoStandard());
            disGoods.setGbDpgOrdersAmount(unChoiceOrderList.size());
            disGoods.setGbDpgOrdersFinishAmount(0);
            disGoods.setGbDpgPurchaseWeek(getWeek(0));
            disGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            disGoods.setGbDpgIsCheck(0);
            disGoods.setGbDpgPurchaseType(1);
            gbDpgService.save(disGoods);
            for (GbDepartmentOrdersEntity unChoiceOrder : unChoiceOrderList) {
                Integer gbDistributerPurchaseGoodsId = disGoods.getGbDistributerPurchaseGoodsId();
                unChoiceOrder.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                gbDepartmentOrdersService.update(unChoiceOrder);

                BigDecimal purQuantity = new BigDecimal(disGoods.getGbDpgQuantity());
                BigDecimal orderQuantity = new BigDecimal(unChoiceOrder.getGbDoQuantity());
                BigDecimal add = purQuantity.add(orderQuantity).setScale(2, BigDecimal.ROUND_HALF_UP);
                disGoods.setGbDpgQuantity(add.toString());
                gbDpgService.update(disGoods);
            }

        }
        return R.ok();
    }


    @RequestMapping(value = "/updateSelfPurGoodsOrdersCost", method = RequestMethod.POST)
    @ResponseBody
    public R updateSelfPurGoodsOrdersCost(@RequestBody GbDistributerPurchaseGoodsEntity purGoods) {
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purGoods.getGbDepartmentOrdersEntities();
        for (GbDepartmentOrdersEntity orders : gbDepartmentOrdersEntities) {
            if (orders.getGbDoStatus() < 4) {
                Integer gbDoBillId = orders.getGbDoBillId();
                if (gbDoBillId != null) {
                    GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryObject(gbDoBillId);
                    BigDecimal billtotal = new BigDecimal(billEntity.getGbDbTotal());
                    Integer gbDepartmentOrdersId = orders.getGbDepartmentOrdersId();
                    GbDepartmentOrdersEntity orignalOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
                    BigDecimal oldSubtotal = new BigDecimal(orignalOrdersEntity.getGbDoSubtotal());
                    BigDecimal nowSubtotal = new BigDecimal(orders.getGbDoSubtotal());
                    BigDecimal changeTotal = oldSubtotal.subtract(nowSubtotal);
                    if (changeTotal.compareTo(BigDecimal.ZERO) == -1) { //小于0
                        BigDecimal add = billtotal.add(changeTotal.abs());
                        billEntity.setGbDbTotal(add.toString());
                        gbDepartmentBillService.update(billEntity);
                    }
                    if (changeTotal.compareTo(BigDecimal.ZERO) == 1) { //大于0
                        BigDecimal add = billtotal.subtract(changeTotal);
                        billEntity.setGbDbTotal(add.toString());
                        gbDepartmentBillService.update(billEntity);
                    }

                }
                gbDepartmentOrdersService.update(orders);
            }
        }

        if (gbDistributerGoodsService.queryObject(purGoods.getGbDpgDisGoodsId()).getGbDgControlPrice() == 1) {
            checkPurGoodsPrice(purGoods);
        }
        System.out.println(purGoods);
        gbDpgService.update(purGoods);
        return R.ok();
    }

    /**
     * 修改
     */
    @ResponseBody
    @RequestMapping("/sellerUpdatePurGoods")
    public R sellerUpdatePurGoods(@RequestBody GbDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        //如果供货商填写了重量，则判断商品是否有保鲜时间参数
        if (purchaseGoodsEntity.getGbDpgBuyQuantity() != null) {
            Integer gbDoDisGoodsId = purchaseGoodsEntity.getGbDpgDisGoodsId();
            GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
            if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
                int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
                int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
                BigDecimal[] warnHourBig = new BigDecimal(warnHour).divideAndRemainder(new BigDecimal(24));
//                BigDecimal decimal = warnHourBig[1];

//                Integer gbDpgDistributerId = purchaseGoodsEntity.getGbDpgDistributerId();
//                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDpgDistributerId);
//                Integer gbDistributerTimeQuantum = gbDistributerEntity.getGbDistributerTimeQuantum();
//
//                if(gbDistributerTimeQuantum == 2){
////                    compareLunchTime(warnHour);
//
//                }

                purchaseGoodsEntity.setGbDpgWarnFullTime(formatWhatFullTime(warnHour));
                purchaseGoodsEntity.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
            }
        }

        gbDpgService.update(purchaseGoodsEntity);
        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purchaseGoodsEntity.getGbDepartmentOrdersEntities();
        for (GbDepartmentOrdersEntity orders : gbDepartmentOrdersEntities) {
            orders.setGbDoStatus(getGbOrderStatusProcurement());
            orders.setGbDoBuyStatus(1);
            gbDepartmentOrdersService.update(orders);
        }

//        Integer gbDpgDisGoodsId = purchaseGoodsEntity.getGbDpgDisGoodsId();
//        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDpgDisGoodsId);

//        if (purchaseGoodsEntity.getGbDpgBuyPrice() != null
//                && gbDistributerGoodsEntity.getGbDgControlPrice() == 1) {
//            GbDistributerPurchaseGoodsEntity gbDistributerPurchaseGoodsEntity = checkPurGoodsPrice(purchaseGoodsEntity);
//            return R.ok().put("data", gbDistributerPurchaseGoodsEntity);
//
//        } else {
//            return R.ok().put("data", purchaseGoodsEntity);
//        }
        return R.ok().put("data", purchaseGoodsEntity);

    }

    private GbDistributerPurchaseGoodsEntity checkPurGoodsPrice(GbDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        System.out.println("checkkckGoododopriidd" + purchaseGoodsEntity.getGbDpgDisGoodsId());
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
                goodsPriceEntity.setGbDgpPurNxDistributerId(gbDistributerGoodsEntity.getGbDgNxDistributerId());
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
                gbDistributerGoodsPriceService.save(goodsPriceEntity);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(goodsPriceEntity.getGbDistributerGoodsPriceId());
            }
            gbDpgService.update(purchaseGoodsEntity);
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
                goodsPriceEntity.setGbDgpPurNxDistributerId(gbDistributerGoodsEntity.getGbDgNxDistributerId());
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

            gbDpgService.update(purchaseGoodsEntity);
        }

        if (buyPrice.compareTo(goodsHighest) == -1 && buyPrice.compareTo(goodsLowest) == 1) {

            if (gbDpgDisGoodsPriceId != null) {

                gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);

                purchaseGoodsEntity.setGbDpgBuyPriceReason(null);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(null);
                gbDpgService.update(purchaseGoodsEntity);

            }
        }
        if (buyPrice.compareTo(goodsHighest) == 0 || buyPrice.compareTo(goodsLowest) == 0) {
            if (gbDpgDisGoodsPriceId != null) {

                gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);

                purchaseGoodsEntity.setGbDpgBuyPriceReason(null);
                purchaseGoodsEntity.setGbDpgDisGoodsPriceId(null);
                gbDpgService.update(purchaseGoodsEntity);


            }
        }


        return purchaseGoodsEntity;

    }

    @RequestMapping(value = "/checkPurchaseGoodsStatus/{id}")
    @ResponseBody
    public R checkPurchaseGoodsStatus(@PathVariable Integer id) {

        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDpgService.queryObject(id);

        return R.ok().put("data", purchaseGoodsEntity.getGbDpgStatus());
    }

}
