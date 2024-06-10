package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-02-24 17:06:57
 */

import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxcommunityfathergoods")
public class NxCommunityFatherGoodsController {
    @Autowired
    private NxCommunityFatherGoodsService cfgService;
    @Autowired
    private NxCommunityAdsenseService nxCommunityAdsenseService;
    @Autowired
    private NxCommunityGoodsService cgService;
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;





    @RequestMapping(value = "/saveFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveFatherGoods(@RequestBody NxCommunityFatherGoodsEntity fatherGoods) {
        Map<String, Object> map5 = new HashMap<>();
        map5.put("level", 1);
        map5.put("fathersFatherId", fatherGoods.getNxCfgFathersFatherId());
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = cfgService.queryComFathersGoodsByParams(map5);
        if (fatherGoodsEntities.size() > 0) {
            fatherGoods.setNxCfgFatherGoodsSort(fatherGoodsEntities.size() + 1);
        } else {
            fatherGoods.setNxCfgFatherGoodsSort(1);
        }
        fatherGoods.setNxCfgNxGoodsId(-1);
        fatherGoods.setNxCfgGoodsAmount(0);
        cfgService.save(fatherGoods);
        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/saveFatherGoodsNxNew", method = RequestMethod.POST)
    public R saveFatherGoodsNxNew(String goodsName, Integer fatherId) {
        NxCommunityFatherGoodsEntity fatherGoodsEntity = cfgService.queryObject(fatherId);
        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", fatherId);
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = cfgService.queryComFathersGoodsByParams(map);
        NxCommunityFatherGoodsEntity goodsEntity = new NxCommunityFatherGoodsEntity();
        goodsEntity.setNxCfgFatherGoodsName(goodsName);
        goodsEntity.setNxCfgFathersFatherId(fatherId);
        goodsEntity.setNxCfgFatherGoodsSort(fatherGoodsEntities.size() + 1);
        goodsEntity.setNxCfgFatherGoodsLevel(2);
        goodsEntity.setNxCfgGoodsAmount(0);
        goodsEntity.setNxCfgNxGoodsId(-1);
        goodsEntity.setNxCfgCommunityId(fatherGoodsEntity.getNxCfgCommunityId());
        cfgService.save(goodsEntity);


        fatherGoodsEntity.setNxCfgGoodsAmount(fatherGoodsEntity.getNxCfgGoodsAmount() + 1);
        cfgService.update(fatherGoodsEntity);


        return R.ok();
    }



    @RequestMapping(value = "/updateFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updateFatherGoods(@RequestBody NxCommunityFatherGoodsEntity fatherGoods) {
        cfgService.update(fatherGoods);

        return R.ok();
    }



    //
    @RequestMapping(value = "/getFatherWithGoodsPindan", method = RequestMethod.POST)
    @ResponseBody
    public R getFatherWithGoodsPindan(Integer commId, Integer orderUserId) {

        System.out.println("idcommdmmdmdmdmdm" + commId);
        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("level", 2);
        if(orderUserId != -1){
            map.put("orderUserId", orderUserId);
            map.put("xiaoyuStatus", 2);
            map.put("dayuType", 3);

        }

        System.out.println("typtptprmapappapa" + map);
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities =  cfgService.queryFatherWithGoodsPindan(map);

//
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("xiaoyuStatus", 2);
        mapA.put("dayuType", 3);
        System.out.println("fafaslfasfasaaaaaa" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        List<NxCommunityAdsenseEntity> adsenseEntities = nxCommunityAdsenseService.queryAdsenseByNxCommunityId(commId);

        Map<String, Object> mapR = new HashMap<>();

        mapR.put("adsense", adsenseEntities);
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("subOrders", nxCommunityOrdersSubEntities);

        return R.ok().put("data", mapR);
    }
    //
    @RequestMapping(value = "/getFatherWithGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getFatherWithGoods(Integer commId, Integer orderUserId) {

        System.out.println("idcommdmmdmdmdmdm" + commId);
        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("level", 2);
        if(orderUserId != -1){
            map.put("orderUserId", orderUserId);
            map.put("xiaoyuStatus", 2);
            map.put("xiaoyuType", 4);
        }
        System.out.println("tytytytytytyyt" + map);

        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities =  cfgService.queryFatherWithGoods(map);

//
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("xiaoyuStatus", 2);
        mapA.put("xiaoyuType", 4);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        List<NxCommunityAdsenseEntity> adsenseEntities = nxCommunityAdsenseService.queryAdsenseByNxCommunityId(commId);

        Map<String, Object> mapR = new HashMap<>();

        mapR.put("adsense", adsenseEntities);
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("subOrders", nxCommunityOrdersSubEntities);

        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/getRankFatherGoods/{comId}")
    @ResponseBody
    public R getRankFatherGoods(@PathVariable Integer comId) {
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = cfgService.queryRankFatherGoods(comId);
        return R.ok().put("data", fatherGoodsEntities);
    }

    /**
     * 获取批发商商品的父类列表
     * @param comId 批发商id
     * @return 批发商父类列表
     */
    @RequestMapping(value = "/resGetComGoodsCata", method = RequestMethod.POST)
    @ResponseBody
    public R resGetComGoodsCata(Integer comId, Integer level) {
        System.out.println(comId + level + "abc");
        Map<String, Object> map = new HashMap<>();
        map.put("comId", comId);
        map.put("level", level);
        List<NxCommunityFatherGoodsEntity> comGoodsCata = cfgService.queryComGoodsCata(map);
        if (comGoodsCata.size() > 0) {
//            List<NxCommunityFatherGoodsEntity> newList = new ArrayList<>();
            for (NxCommunityFatherGoodsEntity greatGrandGoods : comGoodsCata) {
                for (NxCommunityFatherGoodsEntity grandGoods : greatGrandGoods.getFatherGoodsEntities()) {
					for (NxCommunityFatherGoodsEntity fatherGoods : grandGoods.getFatherGoodsEntities()) {

						StringBuilder builder = new StringBuilder();
						Map<String, Object> map1 = new HashMap<>();
						Integer communityFatherGoodsId = fatherGoods.getNxCommunityFatherGoodsId();
						map1.put("fatherId", communityFatherGoodsId);
						map1.put("serviceLevel", level);
						List<NxCommunityGoodsEntity> goodsEntities = cgService.queryCgSubNameByFatherId(map1);

						for (NxCommunityGoodsEntity goods : goodsEntities) {
							String nxGoodsName = goods.getNxCgGoodsName();
							builder.append(nxGoodsName);
							builder.append(',');
						}
						System.out.println(builder + "buildld");
						fatherGoods.setCgGoodsSubNames(builder.toString());
//						newList.add(fatherGoods);
					}
                }
            }
            return R.ok().put("data", comGoodsCata);
        }
        return R.ok().put("data", new ArrayList<>());
    }
    /**
     * 获取批发商商品的父类列表
     * @param comId 批发商id
     * @return 批发商父类列表
     */
    @RequestMapping(value = "/customerGetComGoodsCata/{comId}")
    @ResponseBody
    public R customerGetComGoodsCata(@PathVariable Integer comId) {
        System.out.println("fdfajdajfda???");

        Map<String, Object> map = new HashMap<>();
        map.put("comId", comId);
        List<NxCommunityFatherGoodsEntity> comGoodsCata = cfgService.queryComGoodsCata(map);
        if (comGoodsCata.size() > 0) {

            List<NxCommunityFatherGoodsEntity> newList = new ArrayList<>();

            for (NxCommunityFatherGoodsEntity greatGrandGoods : comGoodsCata) {
                for (NxCommunityFatherGoodsEntity grandGoods : greatGrandGoods.getFatherGoodsEntities()) {
                    for (NxCommunityFatherGoodsEntity fatherGoods : grandGoods.getFatherGoodsEntities()) {

                        StringBuilder builder = new StringBuilder();
                        Map<String, Object> map1 = new HashMap<>();
                        Integer communityFatherGoodsId = fatherGoods.getNxCommunityFatherGoodsId();
                        map1.put("fatherId", communityFatherGoodsId);
                        List<NxCommunityGoodsEntity> goodsEntities = cgService.queryCgSubNameByFatherId(map1);

                        for (NxCommunityGoodsEntity goods : goodsEntities) {
                            String nxGoodsName = goods.getNxCgGoodsName();
                            builder.append(nxGoodsName);
                            builder.append(',');
                        }
                        fatherGoods.setCgGoodsSubNames(builder.toString());
                        newList.add(fatherGoods);
                    }

                }

            }


            return R.ok().put("data", comGoodsCata);
        }
        return R.ok().put("data", new ArrayList<>());
    }



    @RequestMapping(value = "/comGetFatherGoodsByFatherId/{fatherId}")
    @ResponseBody
    public R comGetFatherGoodsByFatherId(@PathVariable Integer fatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", fatherId);
        System.out.println("whwhwhhw" + map);
        List<NxCommunityFatherGoodsEntity> comGoodsCata = cfgService.queryComFathersGoodsByParams(map);
        if (comGoodsCata.size() > 0) {
            return R.ok().put("data", comGoodsCata);
        }
        List<NxCommunityFatherGoodsEntity> zero = new ArrayList<>();
        return R.ok().put("data", zero);
    }



    @RequestMapping(value = "/comGetComGoodsCata/{comId}")
    @ResponseBody
    public R comGetComGoodsCata(@PathVariable Integer comId) {
        Map<String, Object> map = new HashMap<>();
        map.put("comId", comId);
        System.out.println("whwhwhhw" + map);
        List<NxCommunityFatherGoodsEntity> comGoodsCata = cfgService.queryComGoodsCata(map);
        if (comGoodsCata.size() > 0) {
            return R.ok().put("data", comGoodsCata);
        }
        List<NxCommunityFatherGoodsEntity> zero = new ArrayList<>();
        return R.ok().put("data", zero);
    }




    @RequestMapping(value = "/getCgCateList/{communityId}")
    @ResponseBody
    public R getGoodsCateList(@PathVariable Integer communityId) {
        List<NxCommunityFatherGoodsEntity> entities = cfgService.queryCataListByCommunityId(communityId);
        return R.ok().put("data", entities);
    }


    /**
     * 保存
     */
    @ResponseBody
    @RequestMapping("/save")
    @RequiresPermissions("nxdistributerfathergoods:save")
    public R save(@RequestBody NxCommunityFatherGoodsEntity nxDistributerFatherGoods) {
        cfgService.save(nxDistributerFatherGoods);

        return R.ok();
    }


}
