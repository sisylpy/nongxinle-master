package com.nongxinle.controller;

/**
 * @author lpy
 * @date 01-14 21:23
 */

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.nongxinle.entity.NxCommunityFatherGoodsEntity;
import com.nongxinle.entity.NxRestrauntOrdersEntity;
import com.nongxinle.service.NxCommunityFatherGoodsService;
import com.nongxinle.service.NxRestrauntOrdersService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityStatisticsEntity;
import com.nongxinle.service.NxCommunityStatisticsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static org.json.XMLTokener.entity;


@RestController
@RequestMapping("api/nxcommunitystatistics")
public class NxCommunityStatisticsController {
    @Autowired
    private NxCommunityStatisticsService nxCommunityStatisticsService;
    @Autowired
    private NxRestrauntOrdersService nxRestrauntOrdersService;
    @Autowired
    private NxCommunityFatherGoodsService nxCommunityFatherGoodsService;


    @RequestMapping(value = "/comGetMonthProfitTotal/{comId}")
    @ResponseBody
    public R comGetMonthProfitTotal(@PathVariable Integer comId) {
        List<Float> list1 = new ArrayList<>();
        Integer hao = Integer.valueOf(getJustHao(0));
        for (int i = hao - 1 ; i > -1; i--) {
            System.out.println(i + "iiiiiiiiiiiiii");
            String s = formatWhatDay(-i);
            Map<String, Object> map = new HashMap<>();
            map.put("comId", comId);
            map.put("date", s);
            List<NxCommunityStatisticsEntity> entities1 = nxCommunityStatisticsService.querySt(map);
            if (entities1.size() > 0) {
                float profit = nxCommunityStatisticsService.queryStWeekProfitSum(map);
                list1.add(profit);
            } else {
                list1.add(0f);
            }
        }

        return R.ok().put("data", list1);
    }


    @RequestMapping(value = "/comGetWeekProfitTotal/{comId}")
    @ResponseBody
    public R comGetWeekProfitTotal(@PathVariable Integer comId) {
		Map<String, Object> mapData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        Date today = new Date();
        calendar.setTime(today);// 此处可换为具体某一时间
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekDay == 1) {
            weekDay = 7;
        } else {
            weekDay = weekDay - 1;
        }
		List<Float> list1 = new ArrayList<>();
		List<Float> list2 = new ArrayList<>();

        for (int i = weekDay - 1; i > -2; i--) {
            String s = formatWhatDay(-i);
            Map<String, Object> map = new HashMap<>();
            map.put("comId", comId);
            map.put("date", s);
            List<NxCommunityStatisticsEntity> entities1 = nxCommunityStatisticsService.querySt(map);
            if (entities1.size() > 0) {
                float profit = nxCommunityStatisticsService.queryStWeekProfitSum(map);
                list1.add(profit);
            } else {
                list1.add(0f);
            }
        }
		mapData.put("thisWeek", list1);

        for (int i = 6 + weekDay; i > weekDay -1; i--) {
            String s = formatWhatDay(-i);
            Map<String, Object> map = new HashMap<>();
            map.put("comId", comId);
            map.put("date", s);
            List<NxCommunityStatisticsEntity> entities1 = nxCommunityStatisticsService.querySt(map);

			if (entities1.size() > 0) {
				float profit = nxCommunityStatisticsService.queryStWeekProfitSum(map);
				list2.add(profit);
			} else {
				list2.add(0f);
			}
        }

		mapData.put("lastWeek", list2);
        return R.ok().put("data", mapData);
    }


    @RequestMapping(value = "/comGetStatistics/{comId}")
    @ResponseBody
    public R comGetStatistics(@PathVariable Integer comId) {

        Map<String, Object> map = new HashMap<>();
        map.put("comId", comId);
        map.put("equalStatus", 7);
        map.put("profit", 1);

        List<NxRestrauntOrdersEntity> entities = nxRestrauntOrdersService.queryResOrdersByParams(map);
        System.out.println("kdfjaksfjads;lkfjdasfas" + entities.size());
        if (entities.size() > 0) {
            for (NxRestrauntOrdersEntity orders : entities) {
                Integer nxCommunityGoodsId = orders.getNxCommunityGoodsEntity().getNxCommunityGoodsId();
                String deliveryDate = orders.getNxRoDeliveryDate();
                Map<String, Object> map1 = new HashMap<>();
                map1.put("comGoodsId", nxCommunityGoodsId);
                map1.put("deliveryDate", deliveryDate);
                List<NxCommunityStatisticsEntity> statisticsGoodsList = nxCommunityStatisticsService.queryStatisticsGoods(map1);
                //1,如果没有当天统计商品
                if (statisticsGoodsList.size() == 0) {
                    addNew(orders);
                } else if (statisticsGoodsList.size() == 1) { //如果只有一个当天统计商品
                    //2,如果订单金额和已有统计
                    NxCommunityStatisticsEntity statisticsGoods = statisticsGoodsList.get(0);
                    toCompare(orders, statisticsGoods);
                } else { //3,如果有多个当天统计商品
                    for (NxCommunityStatisticsEntity st : statisticsGoodsList) {
                        toCompare(orders, st);
                    }
                }
                orders.setNxRoStatus(8);
                nxRestrauntOrdersService.update(orders);
            }
            //显示统计
            List<Map<String, Object>> weekStatistics = getWeekStatistics(comId);
            return R.ok().put("data", weekStatistics);
        } else {
            //显示统计
            List<Map<String, Object>> weekStatistics = getWeekStatistics(comId);
            return R.ok().put("data", weekStatistics);
        }
    }

    private List<Map<String, Object>> getWeekStatistics(Integer comId) {
        List<Map<String, Object>> resultData = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date today = new Date();
        calendar.setTime(today);// 此处可换为具体某一时间
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekDay == 1) {
            weekDay = 7;
        } else {
            weekDay = weekDay - 1;
        }

        for (int i = 0; i < weekDay; i++) {
            String s = formatWhatDay(-i);
            System.out.println(s + "dtttttttt");
            Map<String, Object> map = new HashMap<>();
            map.put("comId", comId);
            map.put("date", s);
            List<NxCommunityStatisticsEntity> entities1 = nxCommunityStatisticsService.querySt(map);
            if (entities1.size() > 0) {
                Map<String, Object> mapData = new HashMap<>();
                mapData.put("week", getWeek(-i));
                mapData.put("date", formatWhatDate(-i));
                mapData.put("arr", entities1);
                Float total = 0.0f;
                for (NxCommunityStatisticsEntity sat : entities1) {
                    Float nxCsComGoodsProfit = sat.getNxCsComGoodsProfit();
                    total = total + nxCsComGoodsProfit;
                }
                mapData.put("total", total);

                resultData.add(mapData);
            }
        }
        return resultData;

    }


    private void addNew(NxRestrauntOrdersEntity orders) {
        NxCommunityStatisticsEntity entity = new NxCommunityStatisticsEntity();
        entity.setNxCsComGoodsId(orders.getNxCommunityGoodsEntity().getNxCommunityGoodsId());
        Integer nxCgCfgGoodsFatherId = orders.getNxCommunityGoodsEntity().getNxCgCfgGoodsFatherId();
        entity.setNxCsComFGoodsId(nxCgCfgGoodsFatherId);
        //father
        NxCommunityFatherGoodsEntity fatherGoodsEntity = nxCommunityFatherGoodsService.queryObject(nxCgCfgGoodsFatherId);
        Integer grandFatherId = fatherGoodsEntity.getNxCfgFathersFatherId();
        //grand
        NxCommunityFatherGoodsEntity fatherGoodsEntity1 = nxCommunityFatherGoodsService.queryObject(grandFatherId);
        Integer nxCfgFathersFatherId = fatherGoodsEntity1.getNxCfgFathersFatherId();
        entity.setNxCsComGfGoodsId(grandFatherId);
        entity.setNxCsComGgfGoodsId(nxCfgFathersFatherId);
        entity.setNxCsComId(orders.getNxRoCommunityId());
        System.out.println("weighthht????" + orders.getNxRoWeight());
        System.out.println("pricie!!!!!" + orders.getNxRoPrice());
        System.out.println("----------------->>>>>>>>>>>");
        float orderWeight = Float.parseFloat(orders.getNxRoWeight());
        float orderProfit = Float.parseFloat(orders.getNxRoProfit());
        float orderPrice = Float.parseFloat(orders.getNxRoPrice());
        new BigDecimal(orders.getNxRoPrice());
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");//注意月份是MM
            String deliveryDate = orders.getNxRoDeliveryDate();
            Date date = simpleDateFormat.parse(deliveryDate);
            entity.setNxCsOrderDate(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        entity.setNxCsComId(orders.getNxRoCommunityId());
        entity.setNxCsComGoodsWeight(orderWeight);
        entity.setNxCsComGoodsProfit(orderProfit);
        entity.setNxCsPurchasePrice(orderPrice);
        entity.setNxCsOrderQuantity(1);
        nxCommunityStatisticsService.save(entity);



    }

    private void toCompare(NxRestrauntOrdersEntity orders, NxCommunityStatisticsEntity statisticsGoods) {
//        float v3 = Float.parseFloat(orders.getNxRoPrice());
        float v3 = Float.parseFloat(orders.getNxRoPrice());
        if (v3 != statisticsGoods.getNxCsPurchasePrice()) {
            addNew(orders);
        } else {
            //profit
            float csProfit = statisticsGoods.getNxCsComGoodsProfit();
            float orderProfit = Float.parseFloat(orders.getNxRoProfit());
            float v = csProfit + orderProfit;
            BigDecimal b = new BigDecimal(v);
            float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            statisticsGoods.setNxCsComGoodsProfit(f1);

            //weight
            Float csWeight = statisticsGoods.getNxCsComGoodsWeight();
            float orderWeight = Float.parseFloat(orders.getNxRoWeight());
            float v1 = csWeight + orderWeight;
            BigDecimal bigDecimal = new BigDecimal(v1);
            float v2 = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            statisticsGoods.setNxCsComGoodsWeight(v2);
            statisticsGoods.setNxCsOrderQuantity(statisticsGoods.getNxCsOrderQuantity() + 1);
            nxCommunityStatisticsService.update(statisticsGoods);
        }
    }


}
