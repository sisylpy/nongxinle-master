package com.nongxinle.controller;

/**
 * @author lpy
 * @date 11-20 12:33
 */

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import javafx.scene.layout.BackgroundImage;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import sun.tools.jconsole.inspector.XObject;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.changeStringToTime;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartGoodsStockReduceTypeProduce;


@RestController
@RequestMapping("api/gbdepartmentgoodsstockreduce")
public class GbDepartmentGoodsStockReduceController {
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepartmentStockReduceService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepGoodsStockService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDepartmentGoodsStockRecordService gbDepGoodsStockRecordService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private GbDepFoodGoodsSalesService gbDepFoodGoodsSalesService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;


    @RequestMapping(value = "/getWhichDayReduce", method = RequestMethod.POST)
    @ResponseBody
    public R getWhichDayReduce(String searchDepIds, Integer disGoodsId, String  date, String type) {
        Map<String, Object> map = new HashMap<>();
        if(!searchDepIds.equals("-1")){
            String[] arrGb = searchDepIds.split(",");
            List<String> idsGb = new ArrayList<>();
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("depFatherIds", idsGb);
                }
            }
        }
        map.put("disGoodsId", disGoodsId);
        map.put("date", date);
        map.put("inventoryType", type);


        System.out.println("abcccc" + map);

        List<GbDepartmentEntity> departmentEntities = gbDepartmentStockReduceService.queryReduceDepartment(map);

        for(GbDepartmentEntity gbDepartmentEntity: departmentEntities){

            Double aDouble = gbDepartmentStockReduceService.queryReduceProduceTotal(map);
            gbDepartmentEntity.setDepProduceGoodsTotalString(new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP).toString());




        }



        return R.ok().put("data", departmentEntities);
    }


    @RequestMapping(value = "/getDisGoodsPurListForCost", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsPurListForCost(String searchDepIds, Integer disGoodsId, String  startDate,
                                       String stopDate) {
        Map<String, Object> map = new HashMap<>();
        if(!searchDepIds.equals("-1")){
            String[] arrGb = searchDepIds.split(",");
            List<String> idsGb = new ArrayList<>();
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("depFatherIds", idsGb);
                }
            }
        }
        map.put("disGoodsId", disGoodsId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        System.out.println("mapappapappapapaap" + map);
        List<GbDistributerPurchaseGoodsEntity> salesEntities = gbDepartmentStockReduceService.queryPurGoodsForCost(map);


        List<String> dateList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        if(salesEntities.size() > 0){
            for(GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : salesEntities){
                String gbDpgPurchaseDate = purchaseGoodsEntity.getGbDpgPurchaseDate();
                String gbDpgBuyQuantity = purchaseGoodsEntity.getGbDpgBuyPrice();
                String substring = gbDpgPurchaseDate.substring(8, 10);
                dateList.add(substring);
                list.add(gbDpgBuyQuantity);
            }
        }

        map.put("equalStatus", 3);
        double doutbleCostV = 0.0;
        double doutbleCost = 0.0;
        String perPrice = "0.0";
        double v = 0.0;
        String maxPrice = "0";
        String minPrice = "0";
        double v1 = 0.0;
        int perDay = 0;
        double perBuy = 0;
        int purCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
        if (purCount > 0) {
            System.out.println("caigoushushul" + map);
            doutbleCostV = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
            doutbleCost = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(map);
            v = doutbleCostV / doutbleCost;
            perPrice = new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            Integer howManyDaysInPeriod1 = getHowManyDaysInPeriod(stopDate, startDate);
            perDay = howManyDaysInPeriod1 / purCount;
            perBuy = doutbleCost / purCount;

        }
        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("arr",salesEntities );
        mapResult.put("totalWeight", new BigDecimal(doutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("totalSubtotal", new BigDecimal(doutbleCostV).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("perPrice", perPrice);
        mapResult.put("purCount", purCount);
        mapResult.put("list", list);
        mapResult.put("purWeight", new BigDecimal(perBuy).setScale(1,BigDecimal.ROUND_HALF_UP));
        mapResult.put("perDay", perDay);




        mapResult.put("date", dateList);

        return R.ok().put("data", mapResult);
    }



    @RequestMapping(value = "/deleteReduceItem/{id}")
    @ResponseBody
    public R deleteReduceItem(@PathVariable Integer id) {
        //reduceItem
        GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentStockReduceService.queryObject(id);
        Integer gbDgsrType = reduceEntity.getGbDgsrType();

        //stockItem
        Integer gbDgsrGbGoodsStockId = reduceEntity.getGbDgsrGbGoodsStockId();
        GbDepartmentGoodsStockEntity stockEntity = gbDepGoodsStockService.queryObject(gbDgsrGbGoodsStockId);
        BigDecimal sRestWeight = new BigDecimal(stockEntity.getGbDgsRestWeight());
        BigDecimal sRestSubtotal = new BigDecimal(stockEntity.getGbDgsRestSubtotal());
        BigDecimal newRestWeight = new BigDecimal(0);
        BigDecimal newRestSubtotal = new BigDecimal(0);
        BigDecimal reduceBusinessWeight = new BigDecimal(0);
        BigDecimal reduceBusinessSubtotal = new BigDecimal(0);
        if (gbDgsrType.equals(getGbDepartGoodsStockReduceTypeProduce())) {
            reduceBusinessWeight = new BigDecimal(reduceEntity.getGbDgsrProduceWeight());
            reduceBusinessSubtotal = new BigDecimal(reduceEntity.getGbDgsrProduceSubtotal());
            BigDecimal gbDgsProduceWeight = new BigDecimal(stockEntity.getGbDgsProduceWeight());
            BigDecimal gbDgsProduceSubtotal = new BigDecimal(stockEntity.getGbDgsProduceSubtotal());
            BigDecimal newProduceWeight = gbDgsProduceWeight.subtract(reduceBusinessWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal newProduceSubtotal = gbDgsProduceSubtotal.subtract(reduceBusinessSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            stockEntity.setGbDgsProduceWeight(newProduceWeight.toString());
            stockEntity.setGbDgsProduceSubtotal(newProduceSubtotal.toString());

        }
        if (gbDgsrType.equals(getGbDepartGoodsStockReduceTypeLoss())) {
            reduceBusinessWeight = new BigDecimal(reduceEntity.getGbDgsrLossWeight());
            reduceBusinessSubtotal = new BigDecimal(reduceEntity.getGbDgsrLossSubtotal());
            BigDecimal gbDgsLossWeight = new BigDecimal(stockEntity.getGbDgsLossWeight());
            BigDecimal gbDgsLossSubtotal = new BigDecimal(stockEntity.getGbDgsLossSubtotal());
            BigDecimal newLossWeight = gbDgsLossWeight.subtract(reduceBusinessWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal newLossSubtotal = gbDgsLossSubtotal.subtract(reduceBusinessSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            stockEntity.setGbDgsLossWeight(newLossWeight.toString());
            stockEntity.setGbDgsLossSubtotal(newLossSubtotal.toString());
        }
        if (gbDgsrType.equals(getGbDepartGoodsStockReduceTypeWaste())) {
            reduceBusinessWeight = new BigDecimal(reduceEntity.getGbDgsrWasteWeight());
            reduceBusinessSubtotal = new BigDecimal(reduceEntity.getGbDgsrWasteSubtotal());
            stockEntity.setGbDgsWasteWeight("0");
            stockEntity.setGbDgsWasteSubtotal("0");
        }
        if (gbDgsrType.equals(getGbDepartGoodsStockReduceTypeReturn())) {
            reduceBusinessWeight = new BigDecimal(reduceEntity.getGbDgsrReturnWeight());
            reduceBusinessSubtotal = new BigDecimal(reduceEntity.getGbDgsrReturnSubtotal());
            BigDecimal gbDgsReturnWeight = new BigDecimal(stockEntity.getGbDgsReturnWeight());
            BigDecimal gbDgsReturnSubtotal = new BigDecimal(stockEntity.getGbDgsReturnSubtotal());
            BigDecimal newReturnWeight = gbDgsReturnWeight.subtract(reduceBusinessWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal newReturnSubtotal = gbDgsReturnSubtotal.subtract(reduceBusinessSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            stockEntity.setGbDgsReturnWeight(newReturnWeight.toString());
            stockEntity.setGbDgsReturnSubtotal(newReturnSubtotal.toString());

            GbDepartmentOrdersEntity ordersEntity =    gbDepartmentOrdersService.queryReturnOrderByReduceId(reduceEntity.getGbDepartmentGoodsStockReduceId());

            Integer purchaseGoodsId = ordersEntity.getGbDoPurchaseGoodsId();
            gbDistributerPurchaseGoodsService.delete(purchaseGoodsId);

            //nxOrder
            if(stockEntity.getGbDgsNxDistributerId() != null && stockEntity.getGbDgsNxDistributerId() != -1){
                Integer gbDoNxDepartmentOrderId = ordersEntity.getGbDoNxDepartmentOrderId();
                nxDepartmentOrdersService.delete(gbDoNxDepartmentOrderId);
            }
            
            gbDepartmentOrdersService.delete(ordersEntity.getGbDepartmentOrdersId());

            if(stockEntity.getGbDgsGbGoodsStockId() != -1){
              GbDepartmentGoodsStockEntity stockEntityReturn =   gbDepGoodsStockService.queryReturnStockItemByOrderId(ordersEntity.getGbDepartmentOrdersId());
              gbDepGoodsStockService.delete(stockEntityReturn.getGbDepartmentGoodsStockId());

           }


        }

        newRestWeight = sRestWeight.add(reduceBusinessWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
        newRestSubtotal = sRestSubtotal.add(reduceBusinessSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
        stockEntity.setGbDgsRestWeight(newRestWeight.toString());
        stockEntity.setGbDgsRestSubtotal(newRestSubtotal.toString());
        gbDepGoodsStockService.update(stockEntity);
        //修改depGoods数量
        addDepDisGoodsTotal(reduceBusinessWeight, reduceBusinessSubtotal, stockEntity.getGbDgsGbDepDisGoodsId());
        //改变daily数据
        String betweentPrice ="0";
        if(stockEntity.getGbDgsBetweenPrice() != null){
            betweentPrice = stockEntity.getGbDgsBetweenPrice();
        }

        changeDepGoodsStockReduceDailyEntity(reduceEntity, gbDgsrType, reduceBusinessWeight, reduceBusinessSubtotal, betweentPrice, stockEntity.getGbDgsSellingPrice());
        //删除redude条目
        gbDepartmentStockReduceService.delete(reduceEntity.getGbDepartmentGoodsStockReduceId());


        return R.ok();
    }


    private void changeDepGoodsStockReduceDailyEntity(GbDepartmentGoodsStockReduceEntity reduceEntity, Integer what, BigDecimal myChangeWeight, BigDecimal myChangeSubtotal, String profitPrice, String sellingPrice) {

        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", reduceEntity.getGbDgsrGbDepDisGoodsId());
        map.put("date", reduceEntity.getGbDgsrDate());
        GbDepartmentGoodsDailyEntity depGoodsDailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(map);
        if (depGoodsDailyEntity != null) {
            if (what.equals(getGbDepartGoodsStockReduceTypeLoss())) {
                BigDecimal lossWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdLossWeight()).subtract(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal lossSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdLossSubtotal()).subtract(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                depGoodsDailyEntity.setGbDgdLossWeight(lossWeight.toString());
                depGoodsDailyEntity.setGbDgdLossSubtotal(lossSubtotal.toString());
            }
            if (what.equals(getGbDepartGoodsStockReduceTypeProduce())) {
                BigDecimal produceWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdProduceWeight()).subtract(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal produceSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdProduceSubtotal()).subtract(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                depGoodsDailyEntity.setGbDgdProduceWeight(produceWeight.toString());
                depGoodsDailyEntity.setGbDgdProduceSubtotal(produceSubtotal.toString());

                //profitSubtotal
                BigDecimal newProfitSubtotal = produceWeight.multiply(new BigDecimal(profitPrice));
                depGoodsDailyEntity.setGbDgdProfitSubtotal(newProfitSubtotal.toString());
                //salesSubtotal
                BigDecimal newSalesSubtotal = produceWeight.multiply(new BigDecimal(sellingPrice));
                depGoodsDailyEntity.setGbDgdSalesSubtotal(newSalesSubtotal.toString());

                //freshRate
                BigDecimal gbDgdProduceWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdProduceWeight());
                BigDecimal gbDgdLastWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdLastWeight());
                if (gbDgdLastWeight.compareTo(BigDecimal.ZERO) == 1) {
                    if (gbDgdProduceWeight.compareTo(gbDgdLastWeight) == 1) {
                        BigDecimal subtract = gbDgdProduceWeight.subtract(gbDgdLastWeight);
                        BigDecimal decimal = subtract.divide(gbDgdProduceWeight, 2, BigDecimal.ROUND_HALF_UP);
                        depGoodsDailyEntity.setGbDgdFreshRate(decimal.toString());
                    }else if (gbDgdProduceWeight.compareTo(gbDgdLastWeight) == 0) {
                        depGoodsDailyEntity.setGbDgdFreshRate("50.0");
                    } else {
                        depGoodsDailyEntity.setGbDgdFreshRate("0.0");
                    }
                } else {
                    depGoodsDailyEntity.setGbDgdFreshRate("100.00");
                }

            }
            if (what.equals(getGbDepartGoodsStockReduceTypeWaste())) {
                BigDecimal wasteWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdWasteWeight()).subtract(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal wasteSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdWasteSubtotal()).subtract(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                depGoodsDailyEntity.setGbDgdWasteWeight(wasteWeight.toString());
                depGoodsDailyEntity.setGbDgdWasteSubtotal(wasteSubtotal.toString());
            }
            if (what.equals(getGbDepartGoodsStockReduceTypeReturn())) {
                BigDecimal returnWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdReturnWeight()).subtract(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal returnSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdReturnSubtotal()).subtract(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                depGoodsDailyEntity.setGbDgdReturnWeight(returnWeight.toString());
                depGoodsDailyEntity.setGbDgdReturnSubtotal(returnSubtotal.toString());
            }

            depGoodsDailyEntity.setGbDgdFullTime(formatFullTime());
            //restWeight
            BigDecimal newRestWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdRestWeight()).add(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            depGoodsDailyEntity.setGbDgdRestWeight(newRestWeight.toString());

            gbDepGoodsDailyService.update(depGoodsDailyEntity);


        }


    }

    private GbDepartmentDisGoodsEntity addDepDisGoodsTotal(BigDecimal weight, BigDecimal subtotal, Integer depDisGoodsId) {

        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        BigDecimal weightB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(weight);
        BigDecimal subtotalB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(subtotal);
        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subtotalB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weightB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

        gbDepartmentDisGoodsService.update(depDisGoodsEntity);
        return depDisGoodsEntity;
    }

    @RequestMapping(value = "/getGoodsTotalList", method = RequestMethod.POST)
    @ResponseBody
    public R getGoodsTotalList(String startDate, String stopDate, String disGoodsId, Integer inventoryType) {
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("disGoodsId", disGoodsId);
        System.out.println("tytytit" + map);
        if (inventoryType.equals(getDISGoodsInventroyDaily())) {
            List<GbDepartmentGoodsDailyEntity> dailyTotalEntities = new ArrayList<>();
            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);

            if (integer > 0) {
                dailyTotalEntities = gbDepGoodsDailyService.queryDepGoodsDailyListByParams(map);
            }
            return R.ok().put("data", dailyTotalEntities);

        } else {
            return R.ok();
        }

    }

    @RequestMapping(value = "/getGoodsReduceList", method = RequestMethod.POST)
    @ResponseBody
    public R getGoodsReduceList(String startDate, String stopDate, String disGoodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("notDayuStopDate", stopDate);
        map.put("disGoodsId", disGoodsId);
        List<GbDepartmentGoodsStockReduceEntity> reduceEntities = new ArrayList<>();
        Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integer > 0) {
            reduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(map);
        }

        return R.ok().put("data", reduceEntities);
    }


    @RequestMapping(value = "/getGoodsReduceListProfit", method = RequestMethod.POST)
    @ResponseBody
    public R getGoodsReduceListByType(String startDate, String stopDate, String disGoodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("notDayuStopDate", stopDate);
        map.put("disGoodsId", disGoodsId);
        map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());

        List<GbDepartmentGoodsStockReduceEntity> reduceEntities = new ArrayList<>();
        Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integer > 0) {
            reduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(map);
        }

        return R.ok().put("data", reduceEntities);
    }


    @RequestMapping(value = "/disGetReduceGoodsAll", method = RequestMethod.POST)
    @ResponseBody
    public R disGetReduceGoodsAll(String startDate, String stopDate, String disGoodsFatherId, String type, Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("notDayuStopDate", stopDate);
        map.put("disGoodsFatherId", disGoodsFatherId);
        TreeSet<GbDistributerGoodsEntity> aaa = gbDepartmentStockReduceService.queryGoodsStockRecordTreeByParams(map);
        for (GbDistributerGoodsEntity goodsEntity : aaa) {
            BigDecimal costTotal = new BigDecimal(0);
            BigDecimal costWeight = new BigDecimal(0);

            //1 produceTotal
            Map<String, Object> map1 = new HashMap<>();
            map1.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
            map1.put("startDate", startDate);
//            map1.put("inventoryType", getGbDepartGoodsStockReduceTypeProduce());
            map1.put("notDayuStopDate", stopDate);
            Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
            if (integer > 0) {
                Double aDouble = gbDepartmentStockReduceService.queryReduceCostSubtotal(map1);
                costTotal = costTotal.add(new BigDecimal(aDouble));
//                Double aDoubleWeight = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(map1);
                Double aDoubleWeight = gbDepartmentStockReduceService.queryReduceCostWeightTotal(map1);
                costWeight = costWeight.add(new BigDecimal(aDoubleWeight));
            }
            //2 wasteTotal

//            map1.put("inventoryType", getGbDepartGoodsStockReduceTypeWaste());
//            Integer integerW = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
//            if (integerW > 0) {
//                Double aDoubleW = gbDepartmentStockReduceService.queryReduceWasteTotal(map1);
//                costTotal = costTotal.add(new BigDecimal(aDoubleW));
//                Double aDoubleWeight = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(map1);
//                costWeight = costWeight.add(new BigDecimal(aDoubleWeight));
//            }

            //3 lossTotal
//            map1.put("inventoryType", getGbDepartGoodsStockReduceTypeLoss());
//            Integer integerl = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
//            if (integerl > 0) {
//                Double aDouble = gbDepartmentStockReduceService.queryReduceLossTotal(map1);
//                costTotal = costTotal.add(new BigDecimal(aDouble));
//                Double aDoubleWeight = gbDepartmentStockReduceService.queryReduceLossWeightTotal(map1);
//                costWeight = costWeight.add(new BigDecimal(aDoubleWeight));
//            }

            goodsEntity.setGoodsCostTotal(costTotal.doubleValue());
            goodsEntity.setGoodsCostTotalString(costTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            goodsEntity.setGoodsWeightTotal(costWeight.doubleValue());
            goodsEntity.setGoodsWeightTotalString(costWeight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        }

        return R.ok().put("data", aaa);
    }


    @RequestMapping(value = "/getEveryGoodsFatherMangement", method = RequestMethod.POST)
    @ResponseBody
    public R getEveryGoodsFatherMangement(String startDate, String stopDate, String disGoodsFatherId, Integer type, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("disGoodsFatherId", disGoodsFatherId);
        TreeSet<GbDistributerGoodsEntity> aaa = gbDepartmentStockReduceService.queryGoodsStockRecordTreeByParams(map);

        BigDecimal decimalDay = new BigDecimal(1);
        if (!startDate.equals(stopDate)) {
            decimalDay = new BigDecimal(Integer.parseInt(getHowManyDaysInPeriod(stopDate, startDate).toString()));
        }


        BigDecimal decimalWeek = decimalDay.divide(new BigDecimal(7), 2, BigDecimal.ROUND_HALF_UP);

//
        BigDecimal decimalMonth = new BigDecimal(1);
        if (!startDate.equals(stopDate)) {
            decimalMonth = new BigDecimal(Integer.toString(getMonthDiff(stopDate, startDate)));
        }

        System.out.println("afdbfafdasd" + aaa.size());
        for (GbDistributerGoodsEntity goodsEntity : aaa) {
            //1 求总wasteTotal
            Map<String, Object> map1 = new HashMap<>();
            map1.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
            map1.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
            map1.put("startDate", startDate);
            map1.put("stopDate", stopDate);
            System.out.println("map111111aaa" + map1);
            BigDecimal totalEveryWeight = new BigDecimal(0);
            BigDecimal totalEvery = new BigDecimal(0);
            BigDecimal everyProduceTotal = new BigDecimal(0);
            BigDecimal everyProduceWeight = new BigDecimal(0);
            Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
            if (integer > 0) {
                Double aDouble = gbDepartmentStockReduceService.queryReduceProduceTotal(map1);
                Double aDoubleWeight = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(map1);
                if (type == 1) {
                    everyProduceTotal = new BigDecimal(aDouble).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                    everyProduceWeight = new BigDecimal(aDoubleWeight).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                }
                if (type == 2) {
                    everyProduceTotal = new BigDecimal(aDouble).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);
                    everyProduceWeight = new BigDecimal(aDoubleWeight).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);

                }
                if (type == 3) {
                    everyProduceTotal = new BigDecimal(aDouble).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);
                    everyProduceWeight = new BigDecimal(aDoubleWeight).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);

                }

                goodsEntity.setGoodsEveryProduceTotal(everyProduceTotal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryProduceTotalString(everyProduceTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                goodsEntity.setGoodsEveryProduceWeightTotal(everyProduceWeight.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryProduceWeightTotalString(everyProduceWeight.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }

            map1.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
            Integer integer1 = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
            BigDecimal everyWasteTotal = new BigDecimal(0);
            BigDecimal everyWasteWeight = new BigDecimal(0);
            if (integer1 > 0) {

                Double aDouble = gbDepartmentStockReduceService.queryReduceWasteTotal(map1);
                Double aDoubleWeight = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(map1);

                if (type == 1) {
                    everyWasteTotal = new BigDecimal(aDouble).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                    everyWasteWeight = new BigDecimal(aDoubleWeight).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                }
                if (type == 2) {
                    everyWasteTotal = new BigDecimal(aDouble).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);
                    everyWasteWeight = new BigDecimal(aDoubleWeight).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);
                }

                if (type == 3) {
                    everyWasteTotal = new BigDecimal(aDouble).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);
                    everyWasteWeight = new BigDecimal(aDoubleWeight).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);
                }


                goodsEntity.setGoodsEveryWasteTotal(everyWasteTotal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryWasteTotalString(everyWasteTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                goodsEntity.setGoodsEveryWasteWeightTotal(everyWasteWeight.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryWasteWeightTotalString(everyWasteWeight.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }

            map1.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
            Integer integer2 = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
            BigDecimal everyLossTotal = new BigDecimal(0);
            BigDecimal everyLossWeight = new BigDecimal(0);
            if (integer2 > 0) {
                Double aDouble = gbDepartmentStockReduceService.queryReduceLossTotal(map1);
                Double aDoubleWeight = gbDepartmentStockReduceService.queryReduceLossWeightTotal(map1);


                if (type == 1) {
                    everyLossTotal = new BigDecimal(aDouble).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                    everyLossWeight = new BigDecimal(aDoubleWeight).divide(decimalDay, 2, BigDecimal.ROUND_HALF_UP);
                }
                if (type == 2) {
                    everyLossTotal = new BigDecimal(aDouble).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);
                    everyLossWeight = new BigDecimal(aDoubleWeight).divide(decimalWeek, 2, BigDecimal.ROUND_HALF_UP);
                }
                if (type == 3) {
                    everyLossTotal = new BigDecimal(aDouble).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);
                    everyLossWeight = new BigDecimal(aDoubleWeight).divide(decimalMonth, 2, BigDecimal.ROUND_HALF_UP);
                }

                goodsEntity.setGoodsEveryLossTotal(everyLossTotal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryLossTotalString(everyLossTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                goodsEntity.setGoodsEveryLossWeightTotal(everyLossWeight.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                goodsEntity.setGoodsEveryLossWeightTotalString(everyLossWeight.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }

            totalEveryWeight = totalEveryWeight.add(everyProduceWeight).add(everyLossWeight).add(everyWasteWeight);

            BigDecimal divide = everyProduceWeight.divide(totalEveryWeight, 1, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

            goodsEntity.setEveryDayWeightString(totalEveryWeight.toString());
            goodsEntity.setAverageManyTotal(divide.toString());

        }


        TreeSet<GbDistributerGoodsEntity> abc = abcProduceEvery(aaa);

        return R.ok().put("data", abc);
    }

    @RequestMapping(value = "/getDistributerGoodsFatherMangement", method = RequestMethod.POST)
    @ResponseBody
    public R getDistributerGoodsFatherMangement(String startDate, String stopDate, String disGoodsFatherId, String type, Integer disId) {
        Map<String, Object> map = new HashMap<>();
//        map.put("status", 2);
        map.put("startDate", startDate);
        map.put("notDayuStopDate", stopDate);
        map.put("disGoodsFatherId", disGoodsFatherId);
        if (type.equals("produce")) {
            map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
        }
        if (type.equals("waste")) {
            map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        }
        if (type.equals("loss")) {
            map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
        }
        TreeSet<GbDistributerGoodsEntity> aaa = gbDepartmentStockReduceService.queryGoodsStockRecordTreeByParams(map);

        for (GbDistributerGoodsEntity goodsEntity : aaa) {
            //1 求总wasteTotal
            Map<String, Object> map1 = new HashMap<>();
            map1.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
            if (type.equals("produce")) {
                map1.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
                if (integer > 0) {
                    Double aDouble = gbDepartmentStockReduceService.queryReduceProduceTotal(map1);
                    Double aDoubleWeight = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(map1);
                    goodsEntity.setGoodsProduceTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    goodsEntity.setGoodsProduceTotalString(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsProduceWeightTotal(new BigDecimal(aDoubleWeight).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    goodsEntity.setGoodsProduceWeightTotalString(new BigDecimal(aDoubleWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }
            if (type.equals("waste")) {
                map1.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
                if (integer > 0) {
                    Double aDouble = gbDepartmentStockReduceService.queryReduceWasteTotal(map1);
                    Double aDoubleWeight = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(map1);
                    goodsEntity.setGoodsWasteTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    goodsEntity.setGoodsWasteTotalString(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsWasteWeightTotal(new BigDecimal(aDoubleWeight).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    goodsEntity.setGoodsWasteWeightTotalString(new BigDecimal(aDoubleWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

            if (type.equals("loss")) {
                map1.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
                if (integer > 0) {
                    Double aDouble = gbDepartmentStockReduceService.queryReduceLossTotal(map1);
                    Double aDoubleWeight = gbDepartmentStockReduceService.queryReduceLossWeightTotal(map1);
                    goodsEntity.setGoodsLossTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    goodsEntity.setGoodsLossTotalString(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    goodsEntity.setGoodsLossWeightTotal(new BigDecimal(aDoubleWeight).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    goodsEntity.setGoodsLossWeightTotalString(new BigDecimal(aDoubleWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }


            //2, 求和
            Map<String, Object> mapDis = new HashMap<>();
            mapDis.put("disGoodsFatherId", disGoodsFatherId);
            mapDis.put("type", getGbDepartmentTypeMendian());
            List<GbDepartmentEntity> departmentEntities1 = gbDepartmentService.queryDepByDepType(mapDis);

            for (GbDepartmentEntity departmentEntity : departmentEntities1) {
                Map<String, Object> map3 = new HashMap<>();
                map3.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                map3.put("status", 2);
                map3.put("depFatherId", departmentEntity.getGbDepartmentId());
                if (type.equals("produce")) {
                    map3.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                }
                if (type.equals("waste")) {
                    map3.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                }
                if (type.equals("loss")) {
                    map3.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                }
                List<GbDepartmentGoodsStockReduceEntity> stockreduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(map3);
                if (stockreduceEntities.size() > 0) {
                    if (type.equals("produce")) {
                        Double aDouble2 = gbDepartmentStockReduceService.queryReduceProduceTotal(map3);
                        Double aDouble2Weight = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(map3);
                        departmentEntity.setDepProduceGoodsTotal(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                        departmentEntity.setDepProduceGoodsTotalString(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        departmentEntity.setDepProduceGoodsWeightTotal(new BigDecimal(aDouble2Weight).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                        departmentEntity.setDepProduceGoodsWeightTotalString(new BigDecimal(aDouble2Weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("waste")) {
                        Double aDouble2 = gbDepartmentStockReduceService.queryReduceWasteTotal(map3);
                        Double aDouble2Weight = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(map3);
                        departmentEntity.setDepWasteGoodsTotal(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                        departmentEntity.setDepWasteGoodsTotalString(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        departmentEntity.setDepWasteGoodsWeightTotal(new BigDecimal(aDouble2Weight).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                        departmentEntity.setDepWasteGoodsWeightTotalString(new BigDecimal(aDouble2Weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }

                    if (type.equals("loss")) {
                        Double aDouble2 = gbDepartmentStockReduceService.queryReduceLossTotal(map3);
                        Double aDouble2Weight = gbDepartmentStockReduceService.queryReduceLossWeightTotal(map3);
                        departmentEntity.setDepLossGoodsTotal(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                        departmentEntity.setDepLossGoodsTotalString(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        departmentEntity.setDepLossGoodsWeightTotal(new BigDecimal(aDouble2Weight).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                        departmentEntity.setDepLossGoodsWeightTotalString(new BigDecimal(aDouble2Weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }

                }
            }
            goodsEntity.setWasteDepartmentEntities(departmentEntities1);
        }
        Map<String, Object> mapG = new HashMap<>();
        mapG.put("status", 2);
        mapG.put("disId", disId);
        mapG.put("startDate", startDate);
        mapG.put("stopDate", stopDate);
        mapG.put("equalType", getGbDepartGoodsStockReduceTypeWaste());

        Integer integer2 = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapG);
        BigDecimal decimal = new BigDecimal(0);
        if (integer2 > 0) {
            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(mapG);
            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(mapG);
            if (produceSubtotal > 0) {
                decimal = new BigDecimal(wasteSubtotal).divide(new BigDecimal(produceSubtotal), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }
        if (type.equals("produce")) {
            TreeSet<GbDistributerGoodsEntity> abc = abcProduce(aaa);

            return R.ok().put("data", abc);
        }
        if (type.equals("waste")) {
            TreeSet<GbDistributerGoodsEntity> abc = abcWaste(aaa);
            return R.ok().put("data", abc);
        }
        if (type.equals("loss")) {
            TreeSet<GbDistributerGoodsEntity> abc = abcLoss(aaa);
            System.out.println("abclossss" + abc.size());

            return R.ok().put("data", abc);
        }
        return R.error(-1, "获取失败");
    }



    @RequestMapping(value = "/getDistributerGoodsFreshMangement", method = RequestMethod.POST)
    @ResponseBody
    public R getDistributerGoodsFreshMangement(String startDate, String stopDate, Integer disId, String type,Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 2);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("disId", disId);
        map.put("depType", getGbDepartmentTypeMendian());
        if (type.equals("produce")) {
            map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
        }
        if (type.equals("waste")) {
            map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        }
        if (type.equals("loss")) {
            map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
        }
        if(depId != -1){
            map.put("depId", depId);
        }

        TreeSet<GbDistributerGoodsEntity> aaa = gbDepartmentStockReduceService.queryGoodsStockRecordTreeByParams(map);
        for (GbDistributerGoodsEntity goodsEntity : aaa) {
            //1 求总wasteTotal
            Map<String, Object> map1 = new HashMap<>();
            map1.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
            map1.put("depType", getGbDepartmentTypeMendian());
            if(depId != -1){
                map1.put("depId", depId);
            }
            Double aDouble = 0.0;
            if (type.equals("produce")) {
                map1.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
                if (integer > 0) {
                     aDouble = gbDepartmentStockReduceService.queryReduceProduceTotal(map1);
                    goodsEntity.setGoodsProduceTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    goodsEntity.setGoodsProduceTotalString(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }
            if (type.equals("waste")) {
                map1.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
                if (integer > 0) {
                    aDouble = gbDepartmentStockReduceService.queryReduceWasteTotal(map1);
                    goodsEntity.setGoodsWasteTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    goodsEntity.setGoodsWasteTotalString(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

            if (type.equals("loss")) {
                map1.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
                if (integer > 0) {
                     aDouble = gbDepartmentStockReduceService.queryReduceLossTotal(map1);
                    goodsEntity.setGoodsLossTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    goodsEntity.setGoodsLossTotalString(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }


            //2, 求和
            List<GbDepartmentEntity> departmentEntities1 = new ArrayList<>();
            Map<String, Object> mapDis = new HashMap<>();
            mapDis.put("disId", disId);
            mapDis.put("type", getGbDepartmentTypeMendian());

            if(depId != -1){
                GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depId);
                departmentEntities1.add(gbDepartmentEntity);
            }else{
                departmentEntities1  = gbDepartmentService.queryDepByDepType(mapDis);
            }

            List<GbDepartmentEntity> resultDep = new ArrayList<>();

            for (GbDepartmentEntity departmentEntity : departmentEntities1) {
                Map<String, Object> map3 = new HashMap<>();
                map3.put("disGoodsId", goodsEntity.getGbDistributerGoodsId());
                map3.put("status", 2);
                map3.put("depFatherId", departmentEntity.getGbDepartmentId());
                if (type.equals("produce")) {
                    map3.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                }
                if (type.equals("waste")) {
                    map3.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                }
                if (type.equals("loss")) {
                    map3.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                }
                System.out.println("map3" + map3);
                Double aDouble2 = 0.0;
                List<GbDepartmentGoodsStockReduceEntity> stockreduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(map3);
                if (stockreduceEntities.size() > 0) {
                    if (type.equals("produce")) {
                         aDouble2 = gbDepartmentStockReduceService.queryReduceProduceTotal(map3);
                        departmentEntity.setDepProduceGoodsTotal(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                        departmentEntity.setDepProduceGoodsTotalString(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (type.equals("waste")) {
                        aDouble2 = gbDepartmentStockReduceService.queryReduceWasteTotal(map3);
                        departmentEntity.setDepWasteGoodsTotal(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                        departmentEntity.setDepWasteGoodsTotalString(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }

                    if (type.equals("loss")) {
                         aDouble2 = gbDepartmentStockReduceService.queryReduceLossTotal(map3);
                        departmentEntity.setDepLossGoodsTotal(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                        departmentEntity.setDepLossGoodsTotalString(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }

                }else{
                    departmentEntity.setDepProduceGoodsTotal(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    departmentEntity.setDepProduceGoodsTotalString(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    departmentEntity.setDepWasteGoodsTotal(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    departmentEntity.setDepWasteGoodsTotalString(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    departmentEntity.setDepLossGoodsTotal(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    departmentEntity.setDepLossGoodsTotalString(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }
            }
            goodsEntity.setWasteDepartmentEntities(departmentEntities1);
        }
        if (type.equals("produce")) {
            TreeSet<GbDistributerGoodsEntity> abc = abcProduce(aaa);
            return R.ok().put("data", abc);
        }
        if (type.equals("waste")) {
            TreeSet<GbDistributerGoodsEntity> abc = abcWaste(aaa);
            return R.ok().put("data", abc);
        }
        if (type.equals("loss")) {
            TreeSet<GbDistributerGoodsEntity> abc = abcLoss(aaa);
            return R.ok().put("data", abc);
        }
        return R.error(-1, "获取失败");
    }




//    @RequestMapping(value = "/getDistributerGoodsFreshData", method = RequestMethod.POST)
//    @ResponseBody
//    public R getDistributerGoodsFreshData(String startDate, String stopDate, String ids) {
//        System.out.println("startDate" + startDate);
//        System.out.println("kfIds" + ids);
//        String[] arr = ids.split(",");
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("status", 2);
//        map.put("startDate", startDate);
//        map.put("stopDate", stopDate);
//        TreeSet<GbDistributerGoodsEntity> aaa = gbDepGoodsStockService.queryStockGoodsWasteLevel(map);
//
//        for (GbDistributerGoodsEntity disGoods : aaa) {
//            //1 求总wasteTotal
//            Map<String, Object> map1 = new HashMap<>();
//            map1.put("disGoodsId", disGoods.getGbDistributerGoodsId());
//            map1.put("status", 2);
//            Double aDouble = gbDepGoodsStockService.queryDepGoodsWasteTotal(map1);
//            disGoods.setGoodsWasteTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
//
//            //2, 求和
//            List<GbDepartmentEntity> departmentEntities = new ArrayList<>();
//            for (String aa : arr) {
//                GbDepartmentEntity dep = gbDepartmentService.queryObject(Integer.valueOf(aa));
//                System.out.println(aa);
//                Map<String, Object> map3 = new HashMap<>();
//                map3.put("disGoodsId", disGoods.getGbDistributerGoodsId());
//                map3.put("status", 2);
//                map3.put("depFatherId", aa);
//                List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryDepGoodsWasteList(map3);
//                if (stockEntities.size() > 0) {
//                    Double aDouble2 = gbDepGoodsStockService.queryDepGoodsWasteTotal(map3);
//                    dep.setDepWasteGoodsTotal(new BigDecimal(aDouble2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
//                    departmentEntities.add(dep);
//                }
//            }
//
//            disGoods.setWasteDepartmentEntities(departmentEntities);
//        }
//
//        TreeSet<GbDistributerGoodsEntity> abc = abcWaste(aaa);
//        return R.ok().put("data", abc);
//    }


    private TreeSet<GbDistributerGoodsEntity> abcWaste(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;
                if (o2.getGoodsWasteTotal() - o1.getGoodsWasteTotal() < 0) {
                    result = 1;
                } else if (o2.getGoodsWasteTotal() - o1.getGoodsWasteTotal() > 0) {
                    result = -1;
                } else {
                    result = 1;
                }

                return result;
            }
        });
//        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
//            @Override
//            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
//                int result;
//                if (o2.getGoodsWasteTotal() - o1.getGoodsWasteTotal() < 0) {
//                    result = -1;
//                } else if (o2.getGoodsWasteTotal() - o1.getGoodsWasteTotal() > 0) {
//                    result = 1;
//                } else {
//                    result = 1;
//                }
//
//                return result;
//            }
//        });

        ts.addAll(goodsEntities);

        return ts;

    }

    private TreeSet<GbDistributerGoodsEntity> abcLoss(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;
                if (o2.getGoodsLossTotal() - o1.getGoodsLossTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsLossTotal() - o1.getGoodsLossTotal() > 0) {
                    result = 1;
                } else {
                    result = 1;
                }
                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }


    private TreeSet<GbDistributerGoodsEntity> abcProfitEvery(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;
                if (o2.getGoodsEveryProfitTotal() - o1.getGoodsEveryProfitTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsEveryProfitTotal() - o1.getGoodsEveryProfitTotal() > 0) {
                    result = 1;
                } else {
                    result = 1;
                }
                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }

    private TreeSet<GbDistributerGoodsEntity> abcProduceEvery(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;
                if (o2.getGoodsEveryProduceTotal() - o1.getGoodsEveryProduceTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsEveryProduceTotal() - o1.getGoodsEveryProduceTotal() > 0) {
                    result = 1;
                } else {
                    result = 1;
                }
                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }


    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherProduce(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;
                if (o2.getFatherProduceTotal() - o1.getFatherProduceTotal() < 0) {
                    result = -1;
                } else if (o2.getFatherProduceTotal() - o1.getFatherProduceTotal() > 0) {
                    result = 1;
                } else {
                    result = 1;
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;
    }


    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherWaste(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;
                if (o2.getFatherWasteTotal() - o1.getFatherWasteTotal() < 0) {
                    result = -1;
                } else if (o2.getFatherWasteTotal() - o1.getFatherWasteTotal() > 0) {
                    result = 1;
                } else {
                    result = 1;
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }

    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherLoss(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;
                if (o2.getFatherLossTotal() - o1.getFatherLossTotal() < 0) {
                    result = -1;
                } else if (o2.getFatherLossTotal() - o1.getFatherLossTotal() > 0) {
                    result = 1;
                } else {
                    result = 1;
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }


    private TreeSet<GbDistributerGoodsEntity> abcProduce(TreeSet<GbDistributerGoodsEntity> goodsEntities) {

        TreeSet<GbDistributerGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerGoodsEntity>() {
            @Override
            public int compare(GbDistributerGoodsEntity o1, GbDistributerGoodsEntity o2) {
                int result;
                if (o2.getGoodsProduceTotal() - o1.getGoodsProduceTotal() < 0) {
                    result = -1;
                } else if (o2.getGoodsProduceTotal() - o1.getGoodsProduceTotal() > 0) {
                    result = 1;
                } else {
                    result = 1;
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }

    @RequestMapping(value = "/getMendianCostStatics")
    @ResponseBody
    public R getMendianCostStatics(Integer disId, String startDate, String stopDate) {

        List<Map<String, Object>> depList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("type", 3);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
        for (GbDepartmentEntity dep : departmentEntities) {
            Map<String, Object> mapDep = new HashMap<>();
            mapDep.put("dep", dep);

            Map<String, Object> map1 = new HashMap<>();
            map1.put("depFatherId", dep.getGbDepartmentId());
            map1.put("startDate", startDate);
            map1.put("stopDate", stopDate);
            map1.put("disId", disId);

            map1.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
            Integer aDoubleCostCount = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
            if (aDoubleCostCount > 0) {
                Double aDoubleCost = gbDepartmentStockReduceService.queryReduceProduceTotal(map1);
                mapDep.put("produce", aDoubleCost.toString());
            } else {
                mapDep.put("produce", "0");
            }

            Map<String, Object> map2 = new HashMap<>();
            map2.put("depFatherId", dep.getGbDepartmentId());
            map2.put("startDate", startDate);
            map2.put("stopDate", stopDate);
            map2.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
            Integer aDoubleCostLoss = gbDepartmentStockReduceService.queryReduceTypeCount(map2);
            if (aDoubleCostLoss > 0) {
                Double aDoubleLoss = gbDepartmentStockReduceService.queryReduceLossTotal(map2);
                mapDep.put("loss", aDoubleLoss.toString());
            } else {
                mapDep.put("loss", "0");
            }

            Map<String, Object> map3 = new HashMap<>();
            map3.put("depFatherId", dep.getGbDepartmentId());
            map3.put("startDate", startDate);
            map3.put("stopDate", stopDate);
            map3.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
            Integer aDoubleCostWaste = gbDepartmentStockReduceService.queryReduceTypeCount(map3);
            if (aDoubleCostWaste > 0) {
                Double aDoubleWaste = gbDepartmentStockReduceService.queryReduceWasteTotal(map3);
                mapDep.put("waste", aDoubleWaste.toString());
            } else {
                mapDep.put("waste", "0");
            }
            Map<String, Object> map4 = new HashMap<>();
            map4.put("depFatherId", dep.getGbDepartmentId());
            map4.put("startDate", startDate);
            map4.put("stopDate", stopDate);
            map4.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
            Integer aDoubleCostReturn = gbDepartmentStockReduceService.queryReduceTypeCount(map4);
            if (aDoubleCostReturn > 0) {
                Double aDoubleReturn = gbDepartmentStockReduceService.queryReduceReturnTotal(map4);

                mapDep.put("return", aDoubleReturn.toString());
            } else {
                mapDep.put("return", "0");
            }
            depList.add(mapDep);

        }

        return R.ok().put("data", depList);
    }


    @RequestMapping(value = "/getDepCostStatistics")
    @ResponseBody
    public R getDepCostStatistics(Integer disId, String startDate, String stopDate) {

        List<Map<String, Object>> grandGoodsList = new ArrayList<>();

        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(disId);
        List<GbDepartmentEntity> mendianDepartmentList = gbDistributerEntity.getMendianDepartmentList();

        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("disId", disId);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepartmentStockReduceService.queryReduceFatherGoods(map);

        for (GbDistributerFatherGoodsEntity fatherGoods : fatherGoodsEntities) {
            Map<String, Object> mapGrand = new HashMap<>();
            mapGrand.put("grandName", fatherGoods.getGbDfgFatherGoodsName());
            List<Map<String, Object>> fatherGoodsList = new ArrayList<>();

            for (GbDistributerFatherGoodsEntity father : fatherGoods.getFatherGoodsEntities()) {
                Map<String, Object> mapFather = new HashMap<>();

                mapFather.put("fatherName", father.getGbDfgFatherGoodsName());

                List<Map<String, Object>> depList = new ArrayList<>();

                Map<String, Object> map1 = new HashMap<>();
                map1.put("fatherGoodsId", father.getGbDistributerFatherGoodsId());
                for (GbDepartmentEntity dep : mendianDepartmentList) {
                    Map<String, Object> mapDep = new HashMap<>();
                    mapDep.put("depName", dep.getGbDepartmentName());
                    map1.put("depFatherId", dep.getGbDepartmentId());
                    map1.put("equalType", 1);
                    Integer aDoubleCostCount = gbDepartmentStockReduceService.queryReduceTypeCount(map1);
                    if (aDoubleCostCount > 0) {
                        Double aDoubleCost = gbDepartmentStockReduceService.queryReduceProduceTotal(map1);
                        mapDep.put("produce", aDoubleCost.toString());
                    } else {
                        mapDep.put("produce", "0");
                    }

                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("fatherGoodsId", father.getGbDistributerFatherGoodsId());
                    map2.put("depFatherId", dep.getGbDepartmentId());
                    map2.put("equalType", 3);
                    Integer aDoubleCostLoss = gbDepartmentStockReduceService.queryReduceTypeCount(map2);
                    if (aDoubleCostLoss > 0) {
                        Double aDoubleLoss = gbDepartmentStockReduceService.queryReduceLossTotal(map2);
                        mapDep.put("loss", aDoubleLoss.toString());
                    } else {
                        mapDep.put("loss", "0");
                    }

                    Map<String, Object> map3 = new HashMap<>();
                    map3.put("fatherGoodsId", father.getGbDistributerFatherGoodsId());
                    map3.put("depFatherId", dep.getGbDepartmentId());
                    map3.put("equalType", 2);
                    Integer aDoubleCostWaste = gbDepartmentStockReduceService.queryReduceTypeCount(map3);
                    if (aDoubleCostWaste > 0) {
                        Double aDoubleWaste = gbDepartmentStockReduceService.queryReduceWasteTotal(map3);
                        mapDep.put("waste", aDoubleWaste.toString());
                    } else {
                        mapDep.put("waste", "0");
                    }
                    Map<String, Object> map4 = new HashMap<>();
                    map4.put("fatherGoodsId", father.getGbDistributerFatherGoodsId());
                    map4.put("depFatherId", dep.getGbDepartmentId());
                    map4.put("equalType", 4);
                    Integer aDoubleCostReturn = gbDepartmentStockReduceService.queryReduceTypeCount(map4);
                    if (aDoubleCostReturn > 0) {
                        Double aDoubleReturn = gbDepartmentStockReduceService.queryReduceReturnTotal(map4);

                        mapDep.put("return", aDoubleReturn.toString());
                    } else {
                        mapDep.put("return", "0");
                    }

                    depList.add(mapDep);
                }
                mapFather.put("fatherArr", depList);

                fatherGoodsList.add(mapFather);

            }
            mapGrand.put("grandArr", fatherGoodsList);
            grandGoodsList.add(mapGrand);

        }

        return R.ok().put("data", grandGoodsList);
    }


    @RequestMapping(value = "/getDepSettleGoodsCostDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getDepSettleGoodsCostDetail(Integer depId, Integer disGoodsId, Integer month) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("month", month);
        map.put("businessDisGoodsId", disGoodsId);
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockWithReduceList(map);
//        List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(map);

        List<GbDepFoodSalesEntity> salesEntities = gbDepFoodGoodsSalesService.queryDepFoodsWithGoods(map);

        Map<String, Object> result = new HashMap<>();
        result.put("reduceArr", stockEntities);
        result.put("foodArr", salesEntities);
        return R.ok().put("data", result);
    }







    @RequestMapping(value = "/getDistributerGoodsCostData", method = RequestMethod.POST)
    @ResponseBody
    public R getDistributerGoodsCostData(String startDate, String stopDate, String ids,
                                         Integer costUse, Integer costWaste, Integer costLoss) {

        String[] arr = ids.split(",");
        List<GbDepartmentEntity> dataDeps = new ArrayList<>();
        for (String id : arr) {
            GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(id));
            // cost
            if (costUse == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", id);
//                map.put("startDate", startDate);
//                map.put("notDayuStopDate", stopDate);
                map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                System.out.println(map);
                List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(map);
                if (reduceEntities.size() > 0) {
                    Double aDouble = gbDepartmentStockReduceService.queryReduceProduceTotal(map);
                    departmentEntity.setDepCostUseStockTotal(new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            // cost
            if (costWaste == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", id);
//                map.put("startDate", startDate);
//                map.put("stopDate", stopDate);
//                map.put("notDayuStopDate", stopDate);
                map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(map);
                if (reduceEntities.size() > 0) {
                    Double aDouble = gbDepartmentStockReduceService.queryReduceWasteTotal(map);
                    departmentEntity.setDepCostWasteStockTotal(new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            // loss
            if (costLoss == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", id);
//                map.put("startDate", startDate);
//                map.put("stopDate", stopDate);
//                map.put("notDayuStopDate", stopDate);
                map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepartmentStockReduceService.queryStockReduceListByParams(map);
                if (reduceEntities.size() > 0) {
                    Double aDouble = gbDepartmentStockReduceService.queryReduceLossTotal(map);
                    departmentEntity.setDepCostLossStockTotal(new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }

            dataDeps.add(departmentEntity);
        }


        return R.ok().put("data", dataDeps);
    }

//
//    @RequestMapping(value = "/disGetStockReduceWorkDetail", method = RequestMethod.POST)
//    @ResponseBody
//    public R disGetStockReduceWorkDetail(Integer depFatherId, String startDate, String stopDate, Integer disGoodsId, String type) {
//
//        Map<String, Object> map = new HashMap<>();
////        map.put("depFatherId", depFatherId);
//        map.put("startDate", startDate);
//        map.put("stopDate", stopDate);
//        map.put("disGoodsId", disGoodsId);
//
//        if (type.equals("produce")) {
//            map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
//        } else if (type.equals("waste")) {
//            map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
//        } else if (type.equals("loss")) {
//            map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
//        } else if (type.equals("return")) {
//            map.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
//        }
//        GbDistributerGoodsEntity entities = gbDepartmentStockReduceService.queryReduceGoodsTypeWorkByParams(map);
//        return R.ok().put("data", entities);
//    }

    @RequestMapping(value = "/depGetStockReduceDetail", method = RequestMethod.POST)
    @ResponseBody
    public R depGetStockReduceDetail(Integer depFatherId, String type, Integer depId) {
        //cost查询
        Map<String, Object> map = new HashMap<>();
        if (depFatherId != null) {
            map.put("depFatherId", depFatherId);
        }
        if (depId != null) {
            map.put("depId", depId);
        }
        if (type.equals("cost")) {
            map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
        } else if (type.equals("waste")) {
            map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        } else if (type.equals("loss")) {
            map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
        } else if (type.equals("return")) {
            map.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
        }
        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDepartmentStockReduceService.queryReduceGoodsTypeByParams(map);

        return R.ok().put("data", gbDistributerGoodsEntities);
    }


    @RequestMapping(value = "/disGetReduceTotal", method = RequestMethod.POST)
    @ResponseBody
    public R disGetReduceTotal(Integer disId, String startDate, String stopDate) {
        Map<String, Object> map123 = new HashMap<>();
        Double costDoutble = 0.0;
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsCost = new TreeSet<>();
        TreeSet<GbDistributerFatherGoodsEntity> resultGrandFatherList = new TreeSet<>();
        Map<String, Object> map1222 = new HashMap<>();
        map1222.put("disId", disId);
        map1222.put("startDate", startDate);
        map1222.put("notDayuStopDate", stopDate);
        Integer count122 = gbDepartmentStockReduceService.queryReduceTypeCount(map1222);
        if (count122 > 0) {
            costDoutble = gbDepartmentStockReduceService.queryReduceCostSubtotal(map1222);
            greatGrandGoodsCost = gbDepartmentStockReduceService.queryReduceGoodsFatherTypeByParams(map1222);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsCost) {
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandDouble = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map1222.put("fatherGoodsId", gbDistributerFatherGoodsId);
                        Double fatherDouble = gbDepartmentStockReduceService.queryReduceCostSubtotal(map1222);
                        grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                        father.setFatherProduceTotal(fatherDouble);
                        father.setFatherProduceTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
//                    grandFather.setFatherGoodsEntities(abcFatherProduce(fatherGoodsEntities));
                    grandFather.setFatherProduceTotal(grandDouble.doubleValue());
                    grandFather.setFatherProduceTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    //return data
                    resultGrandFatherList.add(grandFather);
                }
//                    greatGrandFather.setFatherGoodsEntities(abcFatherProduce(grandGoodsEntities));
//                    greatGrandFather.setFatherProduceTotal(greatGrandTotal.doubleValue());
//                    greatGrandFather.setFatherProduceTotalString(greatGrandTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            }

        }

        Map<String, Object> mapCost = new HashMap<>();
        mapCost.put("total", String.format("%.1f", costDoutble));
        mapCost.put("arr", abcFatherProduce(resultGrandFatherList));
        map123.put("cost", mapCost);
        return R.ok().put("data", map123);

    }


    @RequestMapping(value = "/disGetReduceTotal0000", method = RequestMethod.POST)
    @ResponseBody
    public R disGetReduceTotal0000(Integer disId, String startDate, String stopDate, String type, Integer inventoryType) {
        System.out.println("type=" + type);
        Map<String, Object> map123 = new HashMap<>();
        //进货查询
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", disId);
//        map.put("inventoryType", inventoryType);
//        map.put("dayuStatus", -1);
//        map.put("startDate", startDate);
//        map.put("notDayuStopDate", stopDate);
//        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
//        Double stockDouble1 = 0.0;
//        if (integer > 0) {
//            stockDouble = gbDepGoodsStockService.queryDepGoodsSubtotal(map);
//        }

        if (type.equals("produce")) {
            //cost查询
            Double costDoutble = 0.0;
            TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsCost = new TreeSet<>();
            TreeSet<GbDistributerFatherGoodsEntity> resultGrandFatherList = new TreeSet<>();
            Map<String, Object> map1222 = new HashMap<>();
            map1222.put("disId", disId);
            map1222.put("startDate", startDate);
            map1222.put("notDayuStopDate", stopDate);
            map1222.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
            map1222.put("inventoryType", inventoryType);
            System.out.println("prororoororor" + map1222);
            Integer count122 = gbDepartmentStockReduceService.queryReduceTypeCount(map1222);
            System.out.println("999999999999999" + count122);
            if (count122 > 0) {
                costDoutble = gbDepartmentStockReduceService.queryReduceProduceTotal(map1222);
                System.out.println("cododododododdd" + costDoutble);
                greatGrandGoodsCost = gbDepartmentStockReduceService.queryReduceGoodsFatherTypeByParams(map1222);
                for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsCost) {
//                    BigDecimal greatGrandTotal = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                        BigDecimal grandDouble = new BigDecimal(0);
                        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                        for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                            Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                            map1222.put("fatherGoodsId", gbDistributerFatherGoodsId);
                            Double fatherDouble = gbDepartmentStockReduceService.queryReduceProduceTotal(map1222);
                            grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                            father.setFatherProduceTotal(fatherDouble);
                            father.setFatherProduceTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
//                        grandFather.setFatherGoodsEntities(abcFatherProduce(fatherGoodsEntities));
                        grandFather.setFatherProduceTotal(grandDouble.doubleValue());
                        grandFather.setFatherProduceTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        //return data
                        resultGrandFatherList.add(grandFather);
                    }
//                    greatGrandFather.setFatherGoodsEntities(abcFatherProduce(grandGoodsEntities));
//                    greatGrandFather.setFatherProduceTotal(greatGrandTotal.doubleValue());
//                    greatGrandFather.setFatherProduceTotalString(greatGrandTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }

            }

//            Double costPer = 0.0;
//            if (stockDouble > 0) {
//                costPer = costDoutble / stockDouble;
//            }
//            Double showCost = costPer * 100;
            Map<String, Object> mapCost = new HashMap<>();
            mapCost.put("total", String.format("%.1f", costDoutble));
//            mapCost.put("percent", String.format("%.1f", costPer));
//            mapCost.put("showPercent", String.format("%.1f", showCost));
            mapCost.put("arr", abcFatherProduce(resultGrandFatherList));

            map123.put("cost", mapCost);

        }

        if (type.equals("waste")) {

            //cost查询
            Double costDoutble = 0.0;
            TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsCost = new TreeSet<>();
            TreeSet<GbDistributerFatherGoodsEntity> resultGrandFatherList = new TreeSet<>();
            Map<String, Object> map1222 = new HashMap<>();
            map1222.put("disId", disId);
            map1222.put("startDate", startDate);
            map1222.put("notDayuStopDate", stopDate);
            map1222.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
            map1222.put("inventoryType", inventoryType);
            System.out.println("wastestqwwwwwwwastestqwwwwwwwastestqwwwwww" + map1222);
            Integer count122 = gbDepartmentStockReduceService.queryReduceTypeCount(map1222);
            if (count122 > 0) {
                costDoutble = gbDepartmentStockReduceService.queryReduceWasteTotal(map1222);
                greatGrandGoodsCost = gbDepartmentStockReduceService.queryReduceGoodsFatherTypeByParams(map1222);
                for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsCost) {
                    BigDecimal greatGrandTotal = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                        BigDecimal grandDouble = new BigDecimal(0);
                        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                        for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                            Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                            map1222.put("fatherGoodsId", gbDistributerFatherGoodsId);
                            System.out.println("aaaa" + map1222);
                            Double fatherDouble = gbDepartmentStockReduceService.queryReduceWasteTotal(map1222);
                            grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                            greatGrandTotal = greatGrandTotal.add(new BigDecimal(fatherDouble));
                            father.setFatherWasteTotal(fatherDouble);
                            father.setFatherWasteTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
//                        grandFather.setFatherGoodsEntities(abcFatherWaste(fatherGoodsEntities));
                        grandFather.setFatherWasteTotal(grandDouble.doubleValue());
                        grandFather.setFatherWasteTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        //return data
                        resultGrandFatherList.add(grandFather);
                    }
//                    greatGrandFather.setFatherWasteTotal(greatGrandTotal.doubleValue());
//                    greatGrandFather.setFatherWasteTotalString(greatGrandTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }
            }

//            Double costPer = 0.0;
//            if (stockDouble > 0) {
//                costPer = costDoutble / stockDouble;
//            }
//            Double showCost = costPer * 100;
            Map<String, Object> mapCost = new HashMap<>();
            mapCost.put("total", String.format("%.1f", costDoutble));
//            mapCost.put("percent", String.format("%.1f", costPer));
//            mapCost.put("showPercent", String.format("%.1f", showCost));
            mapCost.put("arr", abcFatherWaste(resultGrandFatherList));

            map123.put("cost", mapCost);

        }
        if (type.equals("loss")) {

            //cost查询
            Double costDoutble = 0.0;
            TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsCost = new TreeSet<>();
            TreeSet<GbDistributerFatherGoodsEntity> resultGrandFatherList = new TreeSet<>();

            Map<String, Object> map1222 = new HashMap<>();
            map1222.put("disId", disId);
            map1222.put("startDate", startDate);
            map1222.put("notDayuStopDate", stopDate);
            map1222.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
            map1222.put("inventoryType", inventoryType);
            System.out.println("loss" + map1222);
            Integer count122 = gbDepartmentStockReduceService.queryReduceTypeCount(map1222);
            if (count122 > 0) {
                costDoutble = gbDepartmentStockReduceService.queryReduceLossTotal(map1222);
                System.out.println("lsosoossindidille" + costDoutble);

                greatGrandGoodsCost = gbDepartmentStockReduceService.queryReduceGoodsFatherTypeByParams(map1222);
                for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsCost) {
                    BigDecimal greatGrandTotal = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                        BigDecimal grandDouble = new BigDecimal(0);
                        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                        for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                            Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                            map1222.put("fatherGoodsId", gbDistributerFatherGoodsId);
                            Double fatherDouble = gbDepartmentStockReduceService.queryReduceLossTotal(map1222);
                            grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                            greatGrandTotal = greatGrandTotal.add(new BigDecimal(fatherDouble));
                            father.setFatherLossTotal(fatherDouble);
                            father.setFatherLossTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        }
//                        grandFather.setFatherGoodsEntities(abcFatherLoss(fatherGoodsEntities));
                        grandFather.setFatherLossTotal(grandDouble.doubleValue());
                        grandFather.setFatherLossTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        //return data
                        resultGrandFatherList.add(grandFather);
                    }
//                    greatGrandFather.setFatherLossTotal(greatGrandTotal.doubleValue());
//                    greatGrandFather.setFatherLossTotalString(greatGrandTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

//            Double costPer = 0.0;
//            if (stockDouble > 0) {
//                costPer = costDoutble / stockDouble;
//            }
//            Double showCost = costPer * 100;
            Map<String, Object> mapCost = new HashMap<>();
            mapCost.put("total", String.format("%.1f", costDoutble));
//            mapCost.put("percent", String.format("%.1f", costPer));
//            mapCost.put("showPercent", String.format("%.1f", showCost));
            mapCost.put("arr", abcFatherLoss(resultGrandFatherList));

            map123.put("cost", mapCost);

        }

        map123.put("threeTotal", getThreeCostTotal(disId, startDate, stopDate, inventoryType));

        return R.ok().put("data", map123);

    }


    private Map<String, Object> getThreeCostTotal(Integer disId, String startDate, String stopDate, Integer inventoryType) {


//        produce
        Double costDoutble = 0.0;
        Map<String, Object> map1222 = new HashMap<>();
        map1222.put("disId", disId);
        map1222.put("startDate", startDate);
        map1222.put("notDayuStopDate", stopDate);
        map1222.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
        map1222.put("inventoryType", inventoryType);
        Integer count122 = gbDepartmentStockReduceService.queryReduceTypeCount(map1222);
        if (count122 > 0) {
            costDoutble = gbDepartmentStockReduceService.queryReduceProduceTotal(map1222);
        }
        //  produce
        Double wasteDoutble = 0.0;
        map1222.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        Integer count1223 = gbDepartmentStockReduceService.queryReduceTypeCount(map1222);
        if (count1223 > 0) {
            wasteDoutble = gbDepartmentStockReduceService.queryReduceWasteTotal(map1222);
        }
        // loss
        Double lossDoutble = 0.0;
        map1222.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
        Integer loss122 = gbDepartmentStockReduceService.queryReduceTypeCount(map1222);
        if (loss122 > 0) {
            lossDoutble = gbDepartmentStockReduceService.queryReduceLossTotal(map1222);
        }

        Map<String, Object> mapCost = new HashMap<>();
        mapCost.put("ctotal", String.format("%.1f", costDoutble));
        mapCost.put("wtotal", String.format("%.1f", wasteDoutble));
        mapCost.put("ltotal", String.format("%.1f", lossDoutble));
        mapCost.put("ptotal", String.format("%.1f", lossDoutble));

        return mapCost;

    }

    @RequestMapping(value = "/depGetReduceTotal", method = RequestMethod.POST)
    @ResponseBody
    public R depGetReduceTotal(Integer depId, String settleId) {
        Map<String, Object> map123 = new HashMap<>();
        //进货查询
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depId);
        map.put("dayuStatus", -1);
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        Double stockDouble = 0.0;
        if (integer > 0) {
            stockDouble = gbDepGoodsStockService.queryDepGoodsSubtotal(map);
        }

        //剩余查询
        Map<String, Object> map3 = new HashMap<>();
        map3.put("depFatherId", depId);
        map3.put("dayuStatus", -1);
        Integer integer3 = gbDepGoodsStockService.queryGoodsStockCount(map3);
        Double restDouble = 0.0;
        if (integer3 > 0) {
            restDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map3);
        }


        Map<String, Object> map1 = new HashMap<>();
        map1.put("fromDepId", depId);
        map1.put("dayuStatus", -1);
        Integer integer2 = gbDepGoodsStockService.queryGoodsStockCount(map1);
        Double outDouble = 0.0;
        if (integer2 > 0) {
            outDouble = gbDepGoodsStockService.queryDepGoodsSubtotal(map1);
        }

        //cost查询
        Double costDoutble = 0.0;
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsCost = new TreeSet<>();
        Map<String, Object> map1222 = new HashMap<>();
        map1222.put("depFatherId", depId);
        map1222.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
        Integer count122 = gbDepartmentStockReduceService.queryReduceTypeCount(map1222);
        if (count122 > 0) {
            costDoutble = gbDepartmentStockReduceService.queryReduceProduceTotal(map1222);
            greatGrandGoodsCost = gbDepartmentStockReduceService.queryReduceGoodsFatherTypeByParams(map1222);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsCost) {
                BigDecimal greatGrandTotal = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandDouble = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map1222.put("fatherGoodsId", gbDistributerFatherGoodsId);
                        Double fatherDouble = gbDepartmentStockReduceService.queryReduceProduceTotal(map1222);
                        grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                        greatGrandTotal = greatGrandTotal.add(new BigDecimal(fatherDouble));
                        father.setFatherProduceTotal(fatherDouble);
                        father.setFatherProduceTotalString(new BigDecimal(fatherDouble).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    grandFather.setFatherProduceTotal(grandDouble.doubleValue());
                    grandFather.setFatherProduceTotalString(grandDouble.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                }
                greatGrandFather.setFatherProduceTotal(greatGrandTotal.doubleValue());
                greatGrandFather.setFatherProduceTotalString(greatGrandTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        }

        double costPer = 0.0;
        if (stockDouble > 0) {
            costPer = costDoutble / stockDouble;
        }
        Double showCost = costPer * 100;
        Map<String, Object> mapCost = new HashMap<>();
        mapCost.put("total", String.format("%.2f", costDoutble));
        mapCost.put("percent", String.format("%.2f", costPer));
        mapCost.put("showPercent", String.format("%.2f", showCost));
        mapCost.put("arr", greatGrandGoodsCost);


        //waste查询
        Double wasteDoutble = 0.0;
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsWaste = new TreeSet<>();
        Map<String, Object> map122 = new HashMap<>();
        map122.put("depFatherId", depId);
        map122.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        Integer count12 = gbDepartmentStockReduceService.queryReduceTypeCount(map122);
        if (count12 > 0) {
            wasteDoutble = gbDepartmentStockReduceService.queryReduceWasteTotal(map122);
            greatGrandGoodsWaste = gbDepartmentStockReduceService.queryReduceGoodsFatherTypeByParams(map122);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsWaste) {
                BigDecimal greatGrandTotal = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandDouble = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map122.put("fatherGoodsId", gbDistributerFatherGoodsId);
                        Double fatherDouble = gbDepartmentStockReduceService.queryReduceWasteTotal(map122);
                        grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                        greatGrandTotal = greatGrandTotal.add(new BigDecimal(fatherDouble));
                        father.setFatherWasteTotal(fatherDouble);
                        father.setFatherWasteTotalString(new BigDecimal(fatherDouble).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    grandFather.setFatherWasteTotal(grandDouble.doubleValue());
                    grandFather.setFatherWasteTotalString(grandDouble.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                }
                greatGrandFather.setFatherWasteTotal(greatGrandTotal.doubleValue());
                greatGrandFather.setFatherWasteTotalString(greatGrandTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        }
        double wastePer = 0.0;
        if (stockDouble > 0) {
            wastePer = wasteDoutble / stockDouble;
        }
        Double showWaste = wastePer * 100;
        Map<String, Object> mapWaste = new HashMap<>();
        mapWaste.put("total", String.format("%.2f", wasteDoutble));
        mapWaste.put("percent", String.format("%.2f", wastePer));
        mapWaste.put("showPercent", String.format("%.2f", showWaste));
        mapWaste.put("arr", greatGrandGoodsWaste);

        //loss查询
        Double lossDoutble = 0.0;
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsLoss = new TreeSet<>();
        Map<String, Object> map121 = new HashMap<>();
        map121.put("depFatherId", depId);
        map121.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
        Integer count1 = gbDepartmentStockReduceService.queryReduceTypeCount(map121);
        if (count1 > 0) {
            lossDoutble = gbDepartmentStockReduceService.queryReduceLossTotal(map121);
            System.out.println("iszhdhhdhdhdhhdfkdjakdjfsalk");
            greatGrandGoodsLoss = gbDepartmentStockReduceService.queryReduceGoodsFatherTypeByParams(map121);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsLoss) {
                BigDecimal greatGrandTotal = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandDouble = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map121.put("fatherGoodsId", gbDistributerFatherGoodsId);
                        Double fatherDouble = gbDepartmentStockReduceService.queryReduceLossTotal(map121);
                        grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                        greatGrandTotal = greatGrandTotal.add(new BigDecimal(fatherDouble));
                        father.setFatherLossTotal(fatherDouble);
                        father.setFatherLossTotalString(new BigDecimal(fatherDouble).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    grandFather.setFatherLossTotal(grandDouble.doubleValue());
                    grandFather.setFatherLossTotalString(grandDouble.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                }
                greatGrandFather.setFatherLossTotal(greatGrandTotal.doubleValue());
                greatGrandFather.setFatherLossTotalString(greatGrandTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        }

        Double lossPer = 0.0;
        if (stockDouble > 0) {
            lossPer = lossDoutble / stockDouble;
        }

        Double showLoss = lossPer * 100;

        Map<String, Object> mapLoss = new HashMap<>();
        mapLoss.put("total", String.format("%.2f", lossDoutble));
        mapLoss.put("percent", String.format("%.2f", lossPer));
        mapLoss.put("showPercent", String.format("%.2f", showLoss));
        mapLoss.put("arr", greatGrandGoodsLoss);


        //return查询
        Double returnDoutble = 0.0;
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsReturn = new TreeSet<>();
        Map<String, Object> map1213 = new HashMap<>();
        map1213.put("depFatherId", depId);
        map1213.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
        Integer count13 = gbDepartmentStockReduceService.queryReduceTypeCount(map1213);
        if (count13 > 0) {
            returnDoutble = gbDepartmentStockReduceService.queryReduceReturnTotal(map1213);
            greatGrandGoodsReturn = gbDepartmentStockReduceService.queryReduceGoodsFatherTypeByParams(map1213);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsReturn) {
                BigDecimal greatGrandTotal = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandDouble = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map1213.put("fatherGoodsId", gbDistributerFatherGoodsId);
                        Double fatherDouble = gbDepartmentStockReduceService.queryReduceReturnTotal(map1213);
                        grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                        greatGrandTotal = greatGrandTotal.add(new BigDecimal(fatherDouble));
                        father.setFatherReturnTotal(fatherDouble);
                        father.setFatherReturnTotalString(new BigDecimal(fatherDouble).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    grandFather.setFatherReturnTotal(grandDouble.doubleValue());
                    grandFather.setFatherReturnTotalString(grandDouble.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                }
                greatGrandFather.setFatherReturnTotal(greatGrandTotal.doubleValue());
                greatGrandFather.setFatherReturnTotalString(greatGrandTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        }
        double returnPer = 0.0;
        if (stockDouble > 0) {
            returnPer = returnDoutble / stockDouble;
        }
        Double showReturn = returnPer * 100;

        Map<String, Object> mapReturn = new HashMap<>();
        mapReturn.put("total", String.format("%.2f", returnDoutble));
        mapReturn.put("percent", String.format("%.2f", returnPer));
        mapReturn.put("showPercent", String.format("%.2f", showReturn));
        mapReturn.put("arr", greatGrandGoodsReturn);

        //判断如果是库房，增加库房独有的统计内容：
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depId);
        if (departmentEntity.getGbDepartmentType().equals(getGbDepartmentTypeKufang())) {
            //chuku
            //new Stock out金额-
            Double outTotal = 0.0;
            Map<String, Object> map145 = new HashMap<>();
            map145.put("fromDepId", depId);
            map145.put("equalStatus", 0);
            Integer stockAmount55 = gbDepGoodsStockService.queryGoodsStockCount(map145);
            if (stockAmount55 > 0) {
                //出库金额
                Map<String, Object> map42 = new HashMap<>();
                map42.put("fromDepId", depId);
                map42.put("equalStatus", 0);
                Double outNewTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map42);
                outTotal = outTotal + outNewTotal;

            }

            //old Stock out金额 goods_record table status == 0
            Map<String, Object> map1451 = new HashMap<>();
            map1451.put("fromDepId", depId);
            map1451.put("equalStatus", 0);
            Integer stockPriceAmount55 = gbDepGoodsStockRecordService.queryGoodsStockRecordCount(map1451);
            if (stockPriceAmount55 > 0) {
                //出库金额
                Map<String, Object> map42 = new HashMap<>();
                map42.put("fromDepId", depId);
                map42.put("equalStatus", 0);
                Double outOldTotal = gbDepGoodsStockRecordService.queryGoodsStockRecordSubtotal(map42);
                outTotal = outOldTotal + outTotal;
            }

            Double stockPer = 0.0;
            if (stockDouble > 0) {
                stockPer = outTotal / stockDouble;
            }

            Double showOut = stockPer * 100;
            Map<String, Object> mapStock = new HashMap<>();
            mapStock.put("total", String.format("%.2f", outTotal));
            mapStock.put("percent", String.format("%.2f", stockPer));
            mapStock.put("showPercent", String.format("%.2f", showOut));

            //wait out
            Double waitingTotal = 0.0;
            Map<String, Object> map1446 = new HashMap<>();
            map1446.put("fromDepId", depId);
            map1446.put("status", 0);
            map1446.put("depFatherIdNotEqual", depId);
            Integer stockAmount16 = gbDepGoodsStockService.queryGoodsStockCount(map1446);
            if (stockAmount16 > 0) {
                //正在出库，门店未收货的金额
                Map<String, Object> map4 = new HashMap<>();
                map4.put("fromDepId", depId);
                map4.put("status", 0);
                map4.put("depFatherIdNotEqual", depId);
                waitingTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map4);
            }
            Double waitStockPer = 0.0;
            if (stockDouble > 0) {
                waitStockPer = waitingTotal / stockDouble;
            }
            Double showWaiting = waitStockPer * 100;
            Map<String, Object> mapWaitStock = new HashMap<>();
            mapWaitStock.put("total", String.format("%.2f", waitingTotal));
            mapWaitStock.put("percent", String.format("%.2f", waitStockPer));
            mapWaitStock.put("showPercent", String.format("%.2f", showWaiting));

            map123.put("waitOutDouble", new BigDecimal(waitingTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            map123.put("stock", mapStock);
            map123.put("waitStock", mapWaitStock);
        }

        map123.put("cost", mapCost);
        map123.put("waste", mapWaste);
        map123.put("loss", mapLoss);
        map123.put("returnS", mapReturn);
        map123.put("stockDouble", new BigDecimal(stockDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        map123.put("restDouble", new BigDecimal(restDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        map123.put("outDouble", new BigDecimal(outDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//        map123.put("lastDouble", lastDouble);
        return R.ok().put("data", map123);

    }


    @RequestMapping(value = "/subDepGetReduceTotal", method = RequestMethod.POST)
    @ResponseBody
    public R subDepGetReduceTotal(Integer depId, String settleId) {
        Map<String, Object> map123 = new HashMap<>();
        //进货查询
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("dayuStatus", -1);
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        Double stockDouble = 0.0;
        if (integer > 0) {
            stockDouble = gbDepGoodsStockService.queryDepGoodsSubtotal(map);
        }

        //剩余查询
        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depId);
        map3.put("dayuStatus", -1);
        Integer integer3 = gbDepGoodsStockService.queryGoodsStockCount(map3);
        Double restDouble = 0.0;
        if (integer3 > 0) {
            restDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map3);
        }


//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("fromDepId", depId);
////        map1.put("fromSettleId", -1);
//        map1.put("dayuStatus", -1);
//        Integer integer2 = gbDepGoodsStockService.queryGoodsStockCount(map1);
//        Double outDouble = 0.0;
//        if (integer2 > 0) {
//            outDouble = gbDepGoodsStockService.queryDepGoodsSubtotal(map1);
//        }


//        String lastDouble = "";
//        if (settleId.equals("-1")) {
//            lastDouble = "0.0";
//        } else {
//            GbDepartmentSettleEntity settleEntity = gbDepartmentSettleService.queryTotalBySettleId(settleId);
//            lastDouble = settleEntity.getGbDsRestTotal();
//        }


        //cost查询
        Double costDoutble = 0.0;
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsCost = new TreeSet<>();
        Map<String, Object> map1222 = new HashMap<>();
        map1222.put("depId", depId);
        map1222.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
        Integer count122 = gbDepartmentStockReduceService.queryReduceTypeCount(map1222);
        if (count122 > 0) {
            costDoutble = gbDepartmentStockReduceService.queryReduceProduceTotal(map1222);
            greatGrandGoodsCost = gbDepartmentStockReduceService.queryReduceGoodsFatherTypeByParams(map1222);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsCost) {
                BigDecimal greatGrandTotal = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandDouble = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map1222.put("fatherGoodsId", gbDistributerFatherGoodsId);
                        Double fatherDouble = gbDepartmentStockReduceService.queryReduceProduceTotal(map1222);
                        grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                        greatGrandTotal = greatGrandTotal.add(new BigDecimal(fatherDouble));
                        father.setFatherProduceTotal(fatherDouble);
                        father.setFatherProduceTotalString(new BigDecimal(fatherDouble).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    grandFather.setFatherProduceTotal(grandDouble.doubleValue());
                    grandFather.setFatherProduceTotalString(grandDouble.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                }
                greatGrandFather.setFatherProduceTotal(greatGrandTotal.doubleValue());
                greatGrandFather.setFatherProduceTotalString(greatGrandTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        }


        Double costPer = costDoutble / stockDouble;
        Double showCost = costPer * 100;
        Map<String, Object> mapCost = new HashMap<>();
        mapCost.put("total", String.format("%.2f", costDoutble));
        mapCost.put("percent", String.format("%.2f", costPer));
        mapCost.put("showPercent", String.format("%.2f", showCost));
        mapCost.put("arr", greatGrandGoodsCost);


        //waste查询
        Double wasteDoutble = 0.0;
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsWaste = new TreeSet<>();
        Map<String, Object> map122 = new HashMap<>();
        map122.put("depId", depId);
        map122.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        System.out.println("waste=============" + map122);
        Integer count12 = gbDepartmentStockReduceService.queryReduceTypeCount(map122);
        if (count12 > 0) {
            wasteDoutble = gbDepartmentStockReduceService.queryReduceWasteTotal(map122);
            greatGrandGoodsWaste = gbDepartmentStockReduceService.queryReduceGoodsFatherTypeByParams(map122);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsWaste) {
                BigDecimal greatGrandTotal = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandDouble = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map122.put("fatherGoodsId", gbDistributerFatherGoodsId);
                        Double fatherDouble = gbDepartmentStockReduceService.queryReduceWasteTotal(map122);
                        grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                        greatGrandTotal = greatGrandTotal.add(new BigDecimal(fatherDouble));
                        father.setFatherProduceTotal(fatherDouble);
                        father.setFatherProduceTotalString(new BigDecimal(fatherDouble).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    grandFather.setFatherProduceTotal(grandDouble.doubleValue());
                    grandFather.setFatherProduceTotalString(grandDouble.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                }
                greatGrandFather.setFatherProduceTotal(greatGrandTotal.doubleValue());
                greatGrandFather.setFatherProduceTotalString(greatGrandTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        }
        Double wastePer = wasteDoutble / stockDouble;
        Double showWaste = wastePer * 100;
        Map<String, Object> mapWaste = new HashMap<>();
        mapWaste.put("total", String.format("%.2f", wasteDoutble));
        mapWaste.put("percent", String.format("%.2f", wastePer));
        mapWaste.put("showPercent", String.format("%.2f", showWaste));
        mapWaste.put("arr", greatGrandGoodsWaste);

        //loss查询
        Double lossDoutble = 0.0;
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsLoss = new TreeSet<>();
        Map<String, Object> map121 = new HashMap<>();
        map121.put("depId", depId);
        map121.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
        Integer count1 = gbDepartmentStockReduceService.queryReduceTypeCount(map121);
        if (count1 > 0) {
            lossDoutble = gbDepartmentStockReduceService.queryReduceLossTotal(map121);
            System.out.println("beginkdaiisidididiidididid");
            greatGrandGoodsLoss = gbDepartmentStockReduceService.queryReduceGoodsFatherTypeByParams(map121);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsLoss) {
                BigDecimal greatGrandTotal = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandDouble = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map121.put("fatherGoodsId", gbDistributerFatherGoodsId);
                        Double fatherDouble = gbDepartmentStockReduceService.queryReduceLossTotal(map121);
                        grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                        greatGrandTotal = greatGrandTotal.add(new BigDecimal(fatherDouble));
                        father.setFatherProduceTotal(fatherDouble);
                        father.setFatherProduceTotalString(new BigDecimal(fatherDouble).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    grandFather.setFatherProduceTotal(grandDouble.doubleValue());
                    grandFather.setFatherProduceTotalString(grandDouble.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                }
                greatGrandFather.setFatherProduceTotal(greatGrandTotal.doubleValue());
                greatGrandFather.setFatherProduceTotalString(greatGrandTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        }
        Double lossPer = lossDoutble / stockDouble;
        Double showLoss = lossPer * 100;

        Map<String, Object> mapLoss = new HashMap<>();
        mapLoss.put("total", String.format("%.2f", lossDoutble));
        mapLoss.put("percent", String.format("%.2f", lossPer));
        mapLoss.put("showPercent", String.format("%.2f", showLoss));
        mapLoss.put("arr", greatGrandGoodsLoss);


        //return查询
        Double returnDoutble = 0.0;
        TreeSet<GbDistributerFatherGoodsEntity> greatGrandGoodsReturn = new TreeSet<>();
        Map<String, Object> map1213 = new HashMap<>();
        map1213.put("depId", depId);
        map1213.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
        Integer count13 = gbDepartmentStockReduceService.queryReduceTypeCount(map1213);
        if (count13 > 0) {
            returnDoutble = gbDepartmentStockReduceService.queryReduceReturnTotal(map1213);
            greatGrandGoodsReturn = gbDepartmentStockReduceService.queryReduceGoodsFatherTypeByParams(map1213);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoodsReturn) {
                BigDecimal greatGrandTotal = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandDouble = new BigDecimal(0);
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map1213.put("fatherGoodsId", gbDistributerFatherGoodsId);
                        Double fatherDouble = gbDepartmentStockReduceService.queryReduceReturnTotal(map1213);
                        grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
                        greatGrandTotal = greatGrandTotal.add(new BigDecimal(fatherDouble));
                        father.setFatherReturnTotal(fatherDouble);
                        father.setFatherReturnTotalString(new BigDecimal(fatherDouble).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    grandFather.setFatherReturnTotal(grandDouble.doubleValue());
                    grandFather.setFatherReturnTotalString(grandDouble.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                }
                greatGrandFather.setFatherReturnTotal(greatGrandTotal.doubleValue());
                greatGrandFather.setFatherReturnTotalString(greatGrandTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        }
        Double returnPer = returnDoutble / stockDouble;
        Double showReturn = returnPer * 100;

        Map<String, Object> mapReturn = new HashMap<>();
        mapReturn.put("total", String.format("%.2f", returnDoutble));
        mapReturn.put("percent", String.format("%.2f", returnPer));
        mapReturn.put("showPercent", String.format("%.2f", showReturn));
        mapReturn.put("arr", greatGrandGoodsReturn);

        //判断如果是库房，增加库房独有的统计内容：
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depId);
        if (departmentEntity.getGbDepartmentType().equals(getGbDepartmentTypeKufang())) {
            //chuku
            //new Stock out金额-
            Double outTotal = 0.0;
            Map<String, Object> map145 = new HashMap<>();
            map145.put("fromDepId", depId);
            map145.put("equalStatus", 0);
            Integer stockAmount55 = gbDepGoodsStockService.queryGoodsStockCount(map145);
            if (stockAmount55 > 0) {
                //出库金额
                Map<String, Object> map42 = new HashMap<>();
                map42.put("fromDepId", depId);
                map42.put("equalStatus", 0);
                Double outNewTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map42);
                outTotal = outTotal + outNewTotal;

            }

            //old Stock out金额 goods_record table status == 0
            Map<String, Object> map1451 = new HashMap<>();
            map1451.put("fromDepId", depId);
            map1451.put("equalStatus", 0);
            Integer stockPriceAmount55 = gbDepGoodsStockRecordService.queryGoodsStockRecordCount(map1451);
            if (stockPriceAmount55 > 0) {
                //出库金额
                Map<String, Object> map42 = new HashMap<>();
                map42.put("fromDepId", depId);
                map42.put("equalStatus", 0);
                Double outOldTotal = gbDepGoodsStockRecordService.queryGoodsStockRecordSubtotal(map42);
                outTotal = outOldTotal + outTotal;
            }
            Double stockPer = outTotal / stockDouble;
            Double showOut = stockPer * 100;
            Map<String, Object> mapStock = new HashMap<>();
            mapStock.put("total", String.format("%.2f", outTotal));
            mapStock.put("percent", String.format("%.2f", stockPer));
            mapStock.put("showPercent", String.format("%.2f", showOut));

            //wait out
            Double waitingTotal = 0.0;
            Map<String, Object> map1446 = new HashMap<>();
            map1446.put("fromDepId", depId);
            map1446.put("status", 0);
            map1446.put("depFatherIdNotEqual", depId);
            Integer stockAmount16 = gbDepGoodsStockService.queryGoodsStockCount(map1446);
            if (stockAmount16 > 0) {
                //正在出库，门店未收货的金额
                Map<String, Object> map4 = new HashMap<>();
                map4.put("fromDepId", depId);
                map4.put("status", 0);
                map4.put("depFatherIdNotEqual", depId);
                System.out.println("meiqiwizuozuozu");
                waitingTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map4);
            }
            Double waitStockPer = waitingTotal / stockDouble;
            Double showWaiting = waitStockPer * 100;
            Map<String, Object> mapWaitStock = new HashMap<>();
            mapWaitStock.put("total", String.format("%.2f", waitingTotal));
            mapWaitStock.put("percent", String.format("%.2f", waitStockPer));
            mapWaitStock.put("showPercent", String.format("%.2f", showWaiting));

            map123.put("waitOutDouble", new BigDecimal(waitingTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            map123.put("stock", mapStock);
            map123.put("waitStock", mapWaitStock);
        }

        map123.put("cost", mapCost);
        map123.put("waste", mapWaste);
        map123.put("loss", mapLoss);
        map123.put("returnS", mapReturn);

        BigDecimal add = new BigDecimal(costDoutble).add(new BigDecimal(lossDoutble)).add(new BigDecimal(wasteDoutble)).add(new BigDecimal(returnDoutble));
        String s = add.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        map123.put("total", s);

        map123.put("restDouble", new BigDecimal(restDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        return R.ok().put("data", map123);

    }


}