package com.nongxinle.controller;

/**
 * @author lpy
 * @date 10-03 09:29
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDistributerGoodsPriceEntity;
import com.nongxinle.service.GbDistributerGoodsPriceService;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/gbdistributergoodsprice")
public class GbDistributerGoodsPriceController {
    @Autowired
    private GbDistributerGoodsPriceService gbDisGoodsPriceService;


    @RequestMapping(value = "/getMendianPriceTypeStatics", method = RequestMethod.POST)
    @ResponseBody
    public R getMendianPriceTypeStatics(Integer disId, String startDate, String stopDate, Integer type, String ids) {
        Map<String, Object> map0 = new HashMap<>();
        map0.put("startDate", startDate);
//        map0.put("stopDate", stopDate);
        map0.put("notDayuStopDate", stopDate);
        map0.put("disId", disId);
        map0.put("inventoryType", type);
//        map0.put("mdIds", ids);

        System.out.println("mappdp" + map0);

        TreeSet<GbDistributerFatherGoodsEntity>    greatGrandGoods = gbDisGoodsPriceService.queryTreePriceGoodsList(map0);
        if (greatGrandGoods.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoods) {
                BigDecimal greatGrandHighestTotal = new BigDecimal(0);
                BigDecimal greatGrandHighestTotalNormal = new BigDecimal(0);
                BigDecimal greatGrandHighestTotalScale = new BigDecimal(0);
                BigDecimal greatGrandHighestWhatTotal = new BigDecimal(0);
                BigDecimal greatGrandLowestTotal = new BigDecimal(0);
                BigDecimal greatGrandLowestTotalNormal = new BigDecimal(0);
                BigDecimal greatGrandLowestTotalScale = new BigDecimal(0);
                BigDecimal greatGrandLowestWhatTotal = new BigDecimal(0);

                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    BigDecimal grandHighestTotal = new BigDecimal(0);
                    BigDecimal grandHighestTotalNormal = new BigDecimal(0);
                    BigDecimal grandHighestTotalScale = new BigDecimal(0);
                    BigDecimal grandHighestWhatTotal = new BigDecimal(0);
                    BigDecimal grandLowestTotal = new BigDecimal(0);
                    BigDecimal grandLowestTotalNormal = new BigDecimal(0);
                    BigDecimal grandLowestTotalScale = new BigDecimal(0);
                    BigDecimal grandLowestWhatTotal = new BigDecimal(0);

                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {

                        // 高于单价的总额
                        BigDecimal highestTotal = new BigDecimal(0);
                        BigDecimal highestTotalNormal = new BigDecimal(0);
                        BigDecimal highestTotalScale = new BigDecimal(0);
                        BigDecimal highestWhatTotal = new BigDecimal(0);
                        map0.put("purWhat", 1);
                        map0.put("fatherGoodsId", father.getGbDistributerFatherGoodsId());
                        Integer count1 = gbDisGoodsPriceService.queryDisPriceGoodsCount(map0);
                        if (count1 > 0) {
                            Double aDouble2HighestWhat = gbDisGoodsPriceService.queryPurTotal(map0);
                            Double aDouble2HighestNormal = gbDisGoodsPriceService.queryPriceHighestTotal(map0);
                            Double aDouble2WhatTotal = gbDisGoodsPriceService.queryPriceWhatTotal(map0);
                            highestTotal = new BigDecimal(aDouble2HighestWhat).setScale(1, BigDecimal.ROUND_HALF_UP);
                            highestTotalNormal = new BigDecimal(aDouble2HighestNormal).setScale(1, BigDecimal.ROUND_HALF_UP);
                            highestWhatTotal = new BigDecimal(aDouble2WhatTotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                            highestTotalScale = (highestTotal.subtract(highestTotalNormal)).divide(highestTotalNormal, 2, BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP);
                            highestTotalScale = highestTotalScale.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                        }

                        // 低于单价的总额
                        BigDecimal lowestTotal = new BigDecimal(0);
                        BigDecimal lowestTotalNormal = new BigDecimal(0);
                        BigDecimal lowestTotalScale = new BigDecimal(0);
                        BigDecimal lowestWhatTotal = new BigDecimal(0);
                        map0.put("purWhat", -1);
                        Integer count2 = gbDisGoodsPriceService.queryDisPriceGoodsCount(map0);
                        if (count2 > 0) {
                            Double aDouble3LowestWhat = gbDisGoodsPriceService.queryPurTotal(map0);
                            Double aDouble3LowestNormal = gbDisGoodsPriceService.queryPriceLowestTotal(map0);
                            Double aDouble3WhatTotal = gbDisGoodsPriceService.queryPriceWhatTotal(map0);
                            lowestTotal = new BigDecimal(aDouble3LowestWhat).setScale(1, BigDecimal.ROUND_HALF_UP);
                            lowestTotalNormal = new BigDecimal(aDouble3LowestNormal).setScale(1, BigDecimal.ROUND_HALF_UP);
                            lowestWhatTotal = new BigDecimal(aDouble3WhatTotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                            lowestTotalScale = (lowestTotalNormal.subtract(lowestTotal)).divide(lowestTotalNormal, 4, BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP);
                            lowestTotalScale = lowestTotalScale.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                        }
                        father.setGoodsPriceHighestTotal(highestTotal.toString());
                        father.setGoodsPriceHighestTotalNormal(highestTotalNormal.toString());
                        father.setGoodsPriceHighestTotalScale(highestTotalScale.toString());
                        father.setGoodsPriceHighestWhatTotal(highestWhatTotal.toString());
                        father.setGoodsPriceLowestTotal(lowestTotal.toString());
                        father.setGoodsPriceLowestTotalNormal(lowestTotalNormal.toString());
                        father.setGoodsPriceLowestTotalScale(lowestTotalScale.toString());
                        father.setGoodsPriceLowestWhatTotal(lowestWhatTotal.toString());
                        BigDecimal subtract = highestWhatTotal.subtract(lowestWhatTotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                        int i = highestWhatTotal.compareTo(lowestWhatTotal);
                        father.setGoodsPriceWhat(Integer.toString(i));
                        father.setGoodsPriceWhatTotal(subtract.toString());


                        grandHighestTotal = grandHighestTotal.add(highestTotal);
                        grandHighestTotalNormal = grandHighestTotalNormal.add(highestTotalNormal);
                        grandHighestTotalScale = grandHighestTotalScale.add(highestTotalScale);
                        grandHighestWhatTotal = grandHighestWhatTotal.add(highestWhatTotal);

                        grandLowestTotal = grandLowestTotal.add(lowestTotal);
                        grandLowestTotalNormal = grandLowestTotalNormal.add(lowestTotalNormal);
                        grandLowestTotalScale = grandLowestTotalScale.add(lowestTotalScale);
                        grandLowestWhatTotal = grandLowestWhatTotal.add(lowestWhatTotal);


                        greatGrandHighestTotal = greatGrandHighestTotal.add(highestTotal);
                        greatGrandHighestTotalNormal = greatGrandHighestTotalNormal.add(highestTotalNormal);
                        greatGrandHighestTotalScale = greatGrandHighestTotalScale.add(highestTotalScale);
                        greatGrandHighestWhatTotal = greatGrandHighestWhatTotal.add(highestWhatTotal);

                        greatGrandLowestTotal = greatGrandLowestTotal.add(lowestTotal);
                        greatGrandLowestTotalNormal = greatGrandLowestTotalNormal.add(lowestTotalNormal);
                        greatGrandLowestTotalScale = greatGrandLowestTotalScale.add(lowestTotalScale);
                        greatGrandLowestWhatTotal = greatGrandLowestWhatTotal.add(lowestWhatTotal);


                    }

                    grandFather.setGoodsPriceHighestTotal(grandHighestTotal.toString());
                    grandFather.setGoodsPriceHighestTotalNormal(grandHighestTotalNormal.toString());
                    grandFather.setGoodsPriceHighestTotalScale(grandHighestTotalScale.toString());
                    grandFather.setGoodsPriceHighestWhatTotal(grandHighestWhatTotal.toString());
                    grandFather.setGoodsPriceLowestTotal(grandLowestTotal.toString());
                    grandFather.setGoodsPriceLowestTotalNormal(grandLowestTotalNormal.toString());
                    grandFather.setGoodsPriceLowestTotalScale(grandLowestTotalScale.toString());
                    grandFather.setGoodsPriceLowestWhatTotal(grandLowestWhatTotal.toString());
                    BigDecimal subtract = grandHighestWhatTotal.subtract(grandLowestWhatTotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    int i = grandHighestWhatTotal.compareTo(grandLowestWhatTotal);
                    grandFather.setGoodsPriceWhat(Integer.toString(i));
                    grandFather.setGoodsPriceWhatTotal(subtract.toString());
                }

                greatGrandFather.setGoodsPriceHighestTotal(greatGrandHighestTotal.toString());
                greatGrandFather.setGoodsPriceHighestTotalNormal(greatGrandHighestTotalNormal.toString());
                greatGrandFather.setGoodsPriceHighestTotalScale(greatGrandHighestTotalScale.toString());
                greatGrandFather.setGoodsPriceHighestWhatTotal(greatGrandHighestWhatTotal.toString());
                greatGrandFather.setGoodsPriceLowestTotal(greatGrandLowestTotal.toString());
                greatGrandFather.setGoodsPriceLowestTotalNormal(greatGrandLowestTotalNormal.toString());
                greatGrandFather.setGoodsPriceLowestTotalScale(greatGrandLowestTotalScale.toString());
                greatGrandFather.setGoodsPriceLowestWhatTotal(greatGrandLowestWhatTotal.toString());
                BigDecimal subtract = greatGrandLowestWhatTotal.subtract(greatGrandLowestWhatTotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                int i = greatGrandLowestWhatTotal.compareTo(greatGrandLowestWhatTotal);
                greatGrandFather.setGoodsPriceWhat(Integer.toString(i));
                greatGrandFather.setGoodsPriceWhatTotal(subtract.toString());

            }
        }


        return R.ok().put("data", greatGrandGoods);
    }


    @RequestMapping(value = "/getGbDisPurGoodPriceDaily/{disId}")
    @ResponseBody
    public R getGbDisPurGoodPriceDaily(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        List<GbDistributerGoodsPriceEntity> entities = gbDisGoodsPriceService.queryPriceGoodsListByParams(map);
        return R.ok().put("data", entities);
    }

//    @RequestMapping(value = "/getGbDepPurGoodPriceDaily/{depId}")
//    @ResponseBody
//    public R getGbDepPurGoodPrice(@PathVariable Integer depId) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", depId);
//        List<GbDistributerGoodsPriceEntity> entities = gbDisGoodsPriceService.queryPriceGoodsListByParams(map);
//        return R.ok().put("data", entities);
//    }


}
