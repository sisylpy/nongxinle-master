package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-03-22 18:07:28
 */

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxorderssub")
public class NxCommunityOrdersSubController {
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;
    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;
    @Autowired
    private NxCommunitySplicingOrdersService nxCommSplicingOrdersService;




    @RequestMapping(value = "/getCommGoodsOfSubs/{fatherId}")
    @ResponseBody
    public R getCommGoodsOfSubs(@PathVariable Integer fatherId) {
        System.out.println(fatherId + "fatherId");
        Map<String, Object> mapS = new HashMap<>();
        mapS.put("fatherId", fatherId);
          List<NxCommunityOrdersSubEntity> subEntities =  nxCommunityOrdersSubService.querySubOrdersByParams(mapS);

          TreeSet<NxCommunityGoodsEntity> commGoodsEntityTreeSet = new TreeSet<>();
        for (NxCommunityOrdersSubEntity sub : subEntities) {
            NxCommunityGoodsEntity nxCommunityGoodsEntity = sub.getNxCommunityGoodsEntity();
            commGoodsEntityTreeSet.add(nxCommunityGoodsEntity);
        }

        List<Map<String, Object>> data = new ArrayList<>();

        for (NxCommunityGoodsEntity commGoods : commGoodsEntityTreeSet) {
            Map<String, Object> map = new HashMap<>();

            List<NxCommunityOrdersSubEntity> subs = new ArrayList<>();
            map.put("goodsName", commGoods.getNxCgGoodsName());
            for (NxCommunityOrdersSubEntity subEntity: subEntities){
                if(commGoods.getNxCommunityGoodsId().equals(subEntity.getNxCosCommunityGoodsId())){
                    subs.add(subEntity);
                }
            }
            map.put("orders", subs);

            data.add(map);

        }


        return R.ok().put("data", data);
    }





    /**
     * 获取订单详细
     *
     * @param orderIds 订单ids
     * @return 订单
     */
    @RequestMapping(value = "/getOrderCate/{orderIds}")
    @ResponseBody
    public R getOrderCate(@PathVariable String orderIds) {
        System.out.println(orderIds + "idsiddssss");
        String[] split = orderIds.split(",");

        TreeSet<NxCommunityFatherGoodsEntity> commFatherGoodsTreeSet = new TreeSet<>();

        List<NxCommunityOrdersSubEntity> subEntities = new ArrayList<>();
        for (String s : split) {

            if(!s.equals(',')){
                System.out.println(s + "::::s::::");
                Integer integer = Integer.valueOf(s);
                if(integer != null){
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderId",integer );
                    List<NxCommunityOrdersSubEntity> ordersEntity = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                    subEntities.addAll(ordersEntity);
                    for (NxCommunityOrdersSubEntity subEntity: ordersEntity) {
                        System.out.println(subEntity.getNxCommunityOrdersSubId() + "idididididiididi");
                        NxCommunityFatherGoodsEntity nxCommunityFatherGoodsEntity = subEntity.getNxCommunityFatherGoodsEntity();
                        System.out.println("goodssssss");
                        commFatherGoodsTreeSet.add(nxCommunityFatherGoodsEntity);

                    }
                }
            }

            System.out.println(commFatherGoodsTreeSet);
            System.out.println(subEntities.size() + "sisisisiziziziiziz");

        }
        List<Map<String, Object>> data = new ArrayList<>();

        for (NxCommunityFatherGoodsEntity father : commFatherGoodsTreeSet) {
            Map<String, Object> map = new HashMap<>();
            Integer total = 0;
            map.put("fatherId", father.getNxCommunityFatherGoodsId());
            map.put("fatherName", father.getNxCfgFatherGoodsName());
            map.put("img", father.getNxCfgFatherGoodsImg());
            Integer nxCommunityFatherGoodsId = father.getNxCommunityFatherGoodsId();
            for (NxCommunityOrdersSubEntity sub : subEntities) {
                if(sub.getNxCosCommunityGoodsFatherId().equals(nxCommunityFatherGoodsId)){
                    total = total + 1 ;
                    map.put("total", total);
                }

            }
            data.add(map);
        }
        return R.ok().put("data", data);
    }



    @RequestMapping(value = "/getOutGoodsOfGoodsType", method = RequestMethod.POST)
    @ResponseBody
    public R getOutGoodsOfGoodsType (@RequestParam Integer page, @RequestParam Integer limit,
                                     @RequestParam Integer goodsType, @RequestParam Integer nxDistributerId) {

        System.out.println(goodsType);
        Map<String, Object> map = new HashMap<>();

        map.put("nxOsGoodsType", goodsType);
        map.put("nxOsSubDistributerId", nxDistributerId);
        map.put("nxOsStatus", 0);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);

        List<NxCommunityOrdersEntity> ordersEntityList = nxCommunityOrdersSubService.queryOutGoodsByType(map);
        int total = nxCommunityOrdersSubService.queryTotal(map);

        PageUtils pageUtil = new PageUtils(ordersEntityList, total, limit, page);


        return R.ok().put("page", pageUtil);
    }



//
//    @RequestMapping(value = "cust/customerGetSubOrders", method = RequestMethod.POST)
//    @ResponseBody
//    public R customerGetSubOrders(Integer nxOrdersUserId, Integer nxOtOrderTemplateId) {
//        Map<String, Object> map = new HashMap<>();
////        map.put("offset", (page - 1) * limit);
////        map.put("limit", limit);
//        map.put("nxOrdersUserId", nxOrdersUserId);
//        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByCustomerUserId(map);
//
//        //查询订单中的批发商商品
//        TreeSet<NxCommunityOrdersSubEntity> treeSet = new TreeSet<>();
//        for (NxCommunityOrdersSubEntity subEntity : subEntities) {
//            treeSet.add(subEntity);
//        }
//
//        //查询订单模版中的商品
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("nxOtOrderTemplateId", nxOtOrderTemplateId);
//        List<NxOrderTemplateItemEntity> itemEntities = nxOrderTemplateItemService.queryUserItem(map1);
//
//        //给已经添加的模版商品加标记
//        TreeSet<NxCommunityOrdersSubEntity> nxOrdersSubEntities = new TreeSet<>();
//        for (NxCommunityOrdersSubEntity sub : treeSet) {
//            sub.setHasItem(false);
//            for (NxOrderTemplateItemEntity item : itemEntities) {
//                if (sub.getNxCosCommunityGoodsId().equals(item.getNxOtDisGoodsId())) {
//                    sub.setHasItem(true);
//                    nxOrdersSubEntities.add(sub);
//                } else {
//                    nxOrdersSubEntities.add(sub);
//                }
//            }
//        }
//        //筛选不是模版商品的订单数据
//        List<NxCommunityOrdersSubEntity> data = new ArrayList<>();
//        for (NxCommunityOrdersSubEntity subEntity : nxOrdersSubEntities) {
//            if (!subEntity.getHasItem()) {
//                data.add(subEntity);
//            }
//        }
//        return R.ok().put("data", data);
//    }

//
//    @RequestMapping(value = "/getToPurchaseGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R getToPurchaseGoods(Integer disId, Integer status) {
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", disId);
//        map.put("status", status);
//        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.queryToPurchaseGoods(map);
//
//        List<Map<String, Object>> resultData = new ArrayList<>();
//        System.out.println("fhakfdaslkfjaskl" + subEntities.size());
//
//        if (subEntities.size() > 0) {
//
//            TreeSet<NxCommunityFatherGoodsEntity> fatherGoodsList = new TreeSet<>();
//            //获取父级商品
//            for (NxCommunityOrdersSubEntity subEntity : subEntities) {
//                Integer nxOsGoodsFatherId = subEntity.getNxCosCommunityGoodsFatherId();
//                NxCommunityFatherGoodsEntity fatherGoods = nxCommunityFatherGoodsService.queryObject(nxOsGoodsFatherId);
//                fatherGoodsList.add(fatherGoods);
//            }
//            System.out.println(fatherGoodsList + "fatherGoodsList");
//
//
//            // 一，父级商品子商品组装
//            for (NxCommunityFatherGoodsEntity fatherGoods : fatherGoodsList) {
//                // 父级商品和子商品和子订单
//                Map<String, Object> mapFather = new HashMap<>();
//                mapFather.put("fatherName", fatherGoods.getNxCfgFatherGoodsName());
//                mapFather.put("show", false);
//                List<Map<String, Object>> goodsList = new ArrayList<>();
//                TreeSet<NxCommunityGoodsEntity> commGoodsEntityTreeSet = new TreeSet<>();
//
//                // 二，获取配送商商品列表
//                for (NxCommunityOrdersSubEntity subEntity : subEntities) {
//
//                    System.out.println("kankan:" + subEntity.getNxCommunityGoodsEntity());
//                    System.out.println("father" + fatherGoods.getNxCommunityFatherGoodsId());
////                    System.out.println("subFather" + subEntity.getNxOsGoodsFatherId());
//                    // 组装子商品和子订单
//                    if (fatherGoods.getNxCommunityFatherGoodsId().equals(subEntity.getNxCosCommunityGoodsFatherId())) {
//                        commGoodsEntityTreeSet.add(subEntity.getNxCommunityGoodsEntity());
//                    }
//                }
//
//                System.out.println("trees:" + commGoodsEntityTreeSet);
//
//                // 组装商品的子订单
//                for (NxCommunityGoodsEntity commGoods : commGoodsEntityTreeSet) {
//                    Map<String, Object> mapSub = new HashMap<>();
//
//                    mapSub.put("goodsName", commGoods.getNxCgGoodsName());
//                    mapSub.put("standardName", commGoods.getNxCgGoodsStandardname());
//                    mapSub.put("purchase", commGoods.getNxCgPurchaseQuantity());
//                    mapSub.put("disGoodsId", commGoods.getNxCommunityGoodsId());
//                    mapSub.put("show", true);
//                    Map<String, Object> subMap = new HashMap<>();
//                    subMap.put("disGoodsId", commGoods.getNxCommunityGoodsId());
//                    subMap.put("status", status);
//                    List<NxCommunityOrdersSubEntity> subEntities1 = nxCommunityOrdersSubService.querySubsByGoodsId(subMap);
//                    mapSub.put("subList", subEntities1);
//                    goodsList.add(mapSub);
//
//                }
//
//                mapFather.put("goodsList", goodsList);
//
//                resultData.add(mapFather);
//
//            }
//
//        }
//
//        return R.ok().put("data", resultData);
//
//    }


    /**
     * 列表
     */
    @ResponseBody
    @RequestMapping("/list")
    @RequiresPermissions("nxorderssub:list")
    public R list(Integer page, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);

        //查询列表数据
        List<NxCommunityOrdersSubEntity> nxOrdersSubList = nxCommunityOrdersSubService.queryList(map);
        int total = nxCommunityOrdersSubService.queryTotal(map);

        PageUtils pageUtil = new PageUtils(nxOrdersSubList, total, limit, page);

        return R.ok().put("page", pageUtil);
    }


    /**
     * 信息
     */
    @ResponseBody
    @RequestMapping("/info/{nxOrdersSubId}")
    @RequiresPermissions("nxorderssub:info")
    public R info(@PathVariable("nxOrdersSubId") Integer nxOrdersSubId) {
        NxCommunityOrdersSubEntity nxOrdersSub = nxCommunityOrdersSubService.queryObject(nxOrdersSubId);

        return R.ok().put("nxOrdersSub", nxOrdersSub);
    }


    @RequestMapping(value = "/updateSplicingOrder/{id}")
    @ResponseBody
    public R updateSplicingOrder(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("dayuType", 3);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        BigDecimal total = new BigDecimal(0);
        BigDecimal youhuiTotal = new BigDecimal(0);
        if(subEntities.size() > 0){
            for(NxCommunityOrdersSubEntity subEntity: subEntities){
                BigDecimal subtotal = new BigDecimal(subEntity.getNxCosSubtotal());
                total = total.add(subtotal);
                if(!subEntity.getNxCosHuaxianDifferentPrice().equals("0")){
                    BigDecimal diffTotal = new BigDecimal(subEntity.getNxCosQuantity()).multiply(new BigDecimal(subEntity.getNxCosHuaxianDifferentPrice()));
                    BigDecimal dif = diffTotal.setScale(1, BigDecimal.ROUND_HALF_UP);
                    youhuiTotal = youhuiTotal.add(dif);
                }
            }
        }
        NxCommunitySplicingOrdersEntity splicingOrdersEntity = nxCommSplicingOrdersService.queryObject(id);
        splicingOrdersEntity.setNxCsoTotal(total.toString());
        splicingOrdersEntity.setNxCsoYouhuiTotal(youhuiTotal.toString());
        nxCommSplicingOrdersService.update(splicingOrdersEntity);

        return R.ok();
    }
    @ResponseBody
    @RequestMapping("/saveSubOrderPindan")
    public R saveSubOrderPindan(@RequestBody NxCommunityOrdersSubEntity nxOrdersSub) {
        nxCommunityOrdersSubService.save(nxOrdersSub);
        Integer nxCosOrdersId = nxOrdersSub.getNxCosOrdersId();

        NxCommunitySplicingOrdersEntity splicingOrdersEntity = nxCommSplicingOrdersService.queryObject(nxCosOrdersId);
        BigDecimal decimal = new BigDecimal(splicingOrdersEntity.getNxCsoTotal());
        BigDecimal decimal1 = new BigDecimal(nxOrdersSub.getNxCosSubtotal());
        BigDecimal total = decimal.add(decimal1).setScale(1,BigDecimal.ROUND_HALF_UP);
        splicingOrdersEntity.setNxCsoTotal(total.toString());

        if(!nxOrdersSub.getNxCosHuaxianDifferentPrice().equals("0")){
            BigDecimal diffTotal = new BigDecimal(nxOrdersSub.getNxCosQuantity()).multiply(new BigDecimal(nxOrdersSub.getNxCosHuaxianDifferentPrice()));
            BigDecimal decimal2 = diffTotal.setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal add = new BigDecimal(splicingOrdersEntity.getNxCsoYouhuiTotal()).add(decimal2);
            splicingOrdersEntity.setNxCsoYouhuiTotal(add.toString());
        }

        splicingOrdersEntity.setNxCsoStatus(1);
        
        nxCommSplicingOrdersService.update(splicingOrdersEntity);


        return R.ok().put("data", nxOrdersSub);
    }

    /**
     * 保存
     */
    @ResponseBody
    @RequestMapping("/saveSubOrder")
    public R saveSubOrder(@RequestBody NxCommunityOrdersSubEntity nxOrdersSub) {
        nxCommunityOrdersSubService.save(nxOrdersSub);
        NxCommunityGoodsEntity nxCommunityGoodsEntity = nxCommunityGoodsService.queryObject(nxOrdersSub.getNxCosCommunityGoodsId());
        nxOrdersSub.setNxCommunityGoodsEntity(nxCommunityGoodsEntity);
        return R.ok().put("data", nxOrdersSub);
    }

    /**
     * 修改
     */
    @ResponseBody
    @RequestMapping("/updateSubOrder")
    public R updateSubOrder(@RequestBody NxCommunityOrdersSubEntity nxOrdersSub) {
        System.out.println("upddididiidididius" + nxOrdersSub);
        nxCommunityOrdersSubService.update(nxOrdersSub);
        NxCommunityGoodsEntity nxCommunityGoodsEntity = nxCommunityGoodsService.queryObject(nxOrdersSub.getNxCosCommunityGoodsId());
        nxOrdersSub.setNxCommunityGoodsEntity(nxCommunityGoodsEntity);
        return R.ok().put("data", nxOrdersSub);
    }

    @ResponseBody
    @RequestMapping("/updateSubOrderPindan")
    public R updateSubOrderPindan(@RequestBody NxCommunityOrdersSubEntity nxOrdersSub) {


        System.out.println("upddididiidididius" + nxOrdersSub);
        nxCommunityOrdersSubService.update(nxOrdersSub);
        Integer nxCosOrdersId = nxOrdersSub.getNxCosOrdersId();

        Map<String, Object> map = new HashMap<>();
        map.put("id", nxCosOrdersId);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        BigDecimal total = new BigDecimal(0);
        BigDecimal youhuiTotal = new BigDecimal(0);
        if(subEntities.size() > 0){
            for(NxCommunityOrdersSubEntity subEntity: subEntities){
                BigDecimal subtotal = new BigDecimal(subEntity.getNxCosSubtotal());
                total = total.add(subtotal);
                if(!subEntity.getNxCosHuaxianDifferentPrice().equals("0")){
                    BigDecimal diffTotal = new BigDecimal(subEntity.getNxCosQuantity()).multiply(new BigDecimal(subEntity.getNxCosHuaxianDifferentPrice()));
                    BigDecimal dif = diffTotal.setScale(1, BigDecimal.ROUND_HALF_UP);
                    youhuiTotal = youhuiTotal.add(dif);
                }
            }
        }
        NxCommunitySplicingOrdersEntity splicingOrdersEntity = nxCommSplicingOrdersService.queryObject(nxCosOrdersId);
        splicingOrdersEntity.setNxCsoTotal(total.toString());
        splicingOrdersEntity.setNxCsoYouhuiTotal(youhuiTotal.toString());
        nxCommSplicingOrdersService.update(splicingOrdersEntity);


        NxCommunityGoodsEntity nxCommunityGoodsEntity = nxCommunityGoodsService.queryObject(nxOrdersSub.getNxCosCommunityGoodsId());
        nxOrdersSub.setNxCommunityGoodsEntity(nxCommunityGoodsEntity);
        return R.ok().put("data", nxOrdersSub);
    }
    /**
     * 删除
     */
    @ResponseBody
    @RequestMapping("/deleteSubOrders")
    public R deleteSubOrders(@RequestBody Integer[] nxOrdersSubIds) {
        nxCommunityOrdersSubService.deleteBatch(nxOrdersSubIds);

        return R.ok();
    }

}
