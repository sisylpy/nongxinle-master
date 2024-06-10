package com.nongxinle.controller;


import com.nongxinle.entity.NxRestrauntComGoodsEntity;
import com.nongxinle.entity.NxRestrauntOrdersEntity;
import com.nongxinle.entity.NxRestrauntOrdersHistoryEntity;
import com.nongxinle.service.NxRestrauntOrdersHistoryService;
import com.nongxinle.service.NxRestrauntOrdersService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

@RestController
@RequestMapping("api/AAATest")
public class AAATest {
    @Autowired
    private NxRestrauntOrdersService nxRestrauntOrdersService;
    @Autowired
    private NxRestrauntOrdersHistoryService nxRestrauntOrdersHistoryService;

    @RequestMapping(value = "/testa/{id}")
    @ResponseBody
    public R testa(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", id);
        map.put("equalStatus", 6);
        List<NxRestrauntOrdersEntity> entities = nxRestrauntOrdersService.queryResOrdersByParams(map);
        for (NxRestrauntOrdersEntity orders : entities) {
            //update order
            orders.setNxRoStatus(7);
            nxRestrauntOrdersService.update(orders);
            //增加订货历史
            Integer nxRoResComGoodsId = orders.getNxRoResComGoodsId();
            Map<String, Object> map1 = new HashMap<>();
            map1.put("resComGoodsId", nxRoResComGoodsId);
            map1.put("resId", orders.getNxRoRestrauntFatherId());
            List<NxRestrauntOrdersHistoryEntity> historyEntities = nxRestrauntOrdersHistoryService.queryHistoryOrdersByParams(map1);

            String orderQuantity  = "";
            String orderStandard = "";
            String orderStr = "";
            if(orders.getNxRoComGoodsStandardType() == 0){
                orderQuantity = orders.getNxRoQuantity() ;
                orderStandard =  orders.getNxRoStandard();
            }else if(orders.getNxRoComGoodsStandardType() == 1){
                orderQuantity = orders.getNxRoComStandardQuantity();
                orderStandard = orders.getNxRoComStandardName();
            }
            orderStr = orderQuantity + orderStandard;

           //如果有4个以内的历史记录
            if( historyEntities.size() > 0 && historyEntities.size() < 4){
                int equalNumber = 0;
                for (NxRestrauntOrdersHistoryEntity orderHistory : historyEntities) {
                    String historyStr  = orderHistory.getNxRohQuantity() + orderHistory.getNxRohStandard();
                    if(orderStr.equals(historyStr)){
                        equalNumber = equalNumber + 1;
                    }
                }
                if(equalNumber == 0 && historyEntities.size() < 3){
                    //添加新的
                    NxRestrauntOrdersHistoryEntity historyEntity = new NxRestrauntOrdersHistoryEntity();
                    historyEntity.setNxRohApplyDate(orders.getNxRoApplyDate());
                    historyEntity.setNxRohResComGoodsId(orders.getNxRoResComGoodsId());
                    historyEntity.setNxRohQuantity(orderQuantity);
                    historyEntity.setNxRohStandard(orderStandard);
                    historyEntity.setNxRohRestrauntId(orders.getNxRoRestrauntId());
                    historyEntity.setNxRohRestrauntFatherId(orders.getNxRoRestrauntFatherId());
                    historyEntity.setNxRohSellType(orders.getNxRoComGoodsStandardType());
                    historyEntity.setNxRohOrderUserId(orders.getNxRoOrderUserId());
                    nxRestrauntOrdersHistoryService.save(historyEntity);
                }else if(equalNumber == 0 && historyEntities.size() == 3){
                    //删除最早一个
                    NxRestrauntOrdersHistoryEntity first = historyEntities.get(0);
                    Integer nxRestrauntOrdersHistoryId = first.getNxRestrauntOrdersHistoryId();
                    nxRestrauntOrdersHistoryService.delete(nxRestrauntOrdersHistoryId);
                    //添加新的
                    NxRestrauntOrdersHistoryEntity historyEntity = new NxRestrauntOrdersHistoryEntity();
                    historyEntity.setNxRohApplyDate(orders.getNxRoApplyDate());
                    historyEntity.setNxRohResComGoodsId(orders.getNxRoResComGoodsId());
                    historyEntity.setNxRohQuantity(orderQuantity);
                    historyEntity.setNxRohStandard(orderStandard);
                    historyEntity.setNxRohRestrauntId(orders.getNxRoRestrauntId());
                    historyEntity.setNxRohRestrauntFatherId(orders.getNxRoRestrauntFatherId());
                    historyEntity.setNxRohSellType(orders.getNxRoSellType());
                    historyEntity.setNxRohOrderUserId(orders.getNxRoOrderUserId());
                    nxRestrauntOrdersHistoryService.save(historyEntity);
                }

            }else{
                //添加新的
                NxRestrauntOrdersHistoryEntity historyEntity = new NxRestrauntOrdersHistoryEntity();
                historyEntity.setNxRohApplyDate(orders.getNxRoApplyDate());
                historyEntity.setNxRohResComGoodsId(orders.getNxRoResComGoodsId());
                historyEntity.setNxRohQuantity(orderQuantity);
                historyEntity.setNxRohStandard(orderStandard);
                historyEntity.setNxRohRestrauntId(orders.getNxRoRestrauntId());
                historyEntity.setNxRohRestrauntFatherId(orders.getNxRoRestrauntFatherId());
                historyEntity.setNxRohSellType(orders.getNxRoComGoodsStandardType());
                historyEntity.setNxRohOrderUserId(orders.getNxRoOrderUserId());
                nxRestrauntOrdersHistoryService.save(historyEntity);

            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/testB/{resId}")
    @ResponseBody
    public R testB(@PathVariable Integer resId) {
        int checkDay = 4;

        //查询一周内userid 订单
        String s = formatWhatDay(-checkDay);
        System.out.println( "几天之前====="+s);

        Map<String, Object> map = new HashMap<>();
        map.put("resId", resId);
        map.put("applyDate", s);
        String todayString = formatWhatDay(0) + " 00:00"; //今天时间
        SimpleDateFormat sdfm = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date todayTime = new Date();
        try {
            todayTime =  sdfm.parse(todayString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(todayTime + "todayTimememmeme=====");

        map.put("todayTime", todayString); //不包括今天订过商品

        List<NxRestrauntComGoodsEntity> todayData = new ArrayList<>();
        TreeSet<NxRestrauntComGoodsEntity>  theseDayOrderGoods  = nxRestrauntOrdersService.queryTry(map);
        System.out.println(theseDayOrderGoods.size() + "aadesize========");

        for (NxRestrauntComGoodsEntity comGoods : theseDayOrderGoods) {
            System.out.println(comGoods.getNxRcgComGoodsName() + "nenennenenneneneenenn");
            Integer nxRestrauntComGoodsId = comGoods.getNxRestrauntComGoodsId();
            //theseday orders
            Map<String, Object> map1 = new HashMap<>();
            map1.put("resComGoodsId", nxRestrauntComGoodsId );
            map1.put("applyDate", s);
            List<NxRestrauntOrdersEntity> ordersEntities = nxRestrauntOrdersService.queryResOrdersByParams(map1);

            System.out.println(nxRestrauntComGoodsId + " 的订单是几个： "+ ordersEntities.size()  );

            System.out.println(ordersEntities.size() + "<<<<>>>>>checkdkdkkdkdayyaya" + checkDay);

            //如果今天没有
            //如果check天内订过大于等于check次，说明每天订货此商品
            if(ordersEntities.size() > checkDay - 1 ){
                todayData.add(comGoods);
            }
            //如果check天内订过check-1次，说明最少2天一次商品，而且今天没订
            else if (ordersEntities.size()  == checkDay - 1){
                todayData.add(comGoods);
            }
            //如果check天内订过check-2次，说明最少2天一次商品，而且今天没订
            else if (ordersEntities.size() == checkDay - 2){
                NxRestrauntOrdersEntity ordersEntity = ordersEntities.get(ordersEntities.size() - 1);
                String nxRoApplyDate = ordersEntity.getNxRoApplyDate();
                String yesterday = formatWhatDay(-1);
                //如果昨天没定，今天也没订
                if(!nxRoApplyDate.equals(yesterday)){
                    todayData.add(comGoods);
                }
            }
            else if (ordersEntities.size() == checkDay - 3){
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
                if(orderDate.compareTo(lastDate) < 0){
                    todayData.add(comGoods);
                }
            }
        }
        System.out.println(todayData.size() + "ssiissisi");
        return R.ok();
    }
}

