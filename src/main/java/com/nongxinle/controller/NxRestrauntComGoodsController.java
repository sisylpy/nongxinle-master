package com.nongxinle.controller;

/**
 * @author lpy
 * @date 12-01 08:51
 */

import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/nxrestrauntcomgoods")
public class NxRestrauntComGoodsController {
    @Autowired
    private NxRestrauntComGoodsService resComGoodsService;
    @Autowired
    private NxRestrauntService nxRestrauntService;
    @Autowired
    private NxRestrauntOrdersService nxRestrauntOrdersService;
    @Autowired
    private NxCommunityGoodsService cgService;


    
    
    @RequestMapping(value = "/resKnowExchangePrice", method = RequestMethod.POST)
    @ResponseBody
    public R resKnowExchangePrice (@RequestBody List<NxRestrauntComGoodsEntity>  resComGoodsList) {
        System.out.println("123sisy");
        for (NxRestrauntComGoodsEntity resGoods : resComGoodsList) {
            System.out.println(resGoods.getNxRcgResKnowDate());
            resGoods.setNxRcgResKnowDate(formatWhatYearDayTime(0));
            resComGoodsService.update(resGoods);
        }
        return R.ok();
    }


    @RequestMapping(value = "/cgQueryCgManagementGoods", method = RequestMethod.POST)
    @ResponseBody
    public R cgQueryCgManagementGoods (Integer resFatherId,  String searchStr   ) {

        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", resFatherId);

        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchStrPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }

        List<NxRestrauntComGoodsEntity> goodsEntities = resComGoodsService.cgQueryCgMangementGoodsQuickSearchStr(map);

        return R.ok().put("data", goodsEntities);
    }
    
    @RequestMapping(value = "/orderUserQueryResComGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R orderUserQueryResComGoodsByQuickSearch(Integer resId, Integer userId, String searchStr) {

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> map1 = new HashMap<>();

        map.put("userId", userId);
        map1.put("userId", userId);
        map.put("resFatherId", resId);
        map1.put("resFatherId", resId);


        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchStrPinyin", pinyin);
                map1.put("searchStr", searchStr);
                map1.put("searchStrPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
                map1.put("searchPinyin", searchStr);
            }
        }

        List<NxRestrauntComGoodsEntity> goodsEntities = resComGoodsService.orderUserQueryResComGoodsQuickSearchStr(map);
        List<NxRestrauntComGoodsEntity> userEntities = resComGoodsService.ordreUserQueryHistoryGoodsQuickSearchStr(map1);
        Map<String, Object> map3 = new HashMap<>();
        if (goodsEntities.size() > 0) {
            map3.put("resGoods", goodsEntities);
        } else {
            map3.put("resGoods", new ArrayList<>());
        }

        if (userEntities.size() > 0) {
            map3.put("userGoods", userEntities);
        } else {
            map3.put("userGoods", new ArrayList<>());
        }
        return R.ok().put("data", map3);
    }


    @RequestMapping(value = "/orderUserGetGoods/{userId}")
    @ResponseBody
    public R orderUserGetGoods(@PathVariable Integer userId) {

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = resComGoodsService.queryOrderUserGoods(map);
        return R.ok().put("data", fatherGoodsEntities);
    }

    @RequestMapping(value = "/orderUserGetResGoods", method = RequestMethod.POST)
    @ResponseBody
    public R orderUserGetResGoods(Integer resId, Integer userId) {

        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", resId);
        map.put("userId", userId);
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = resComGoodsService.queryOrderResGoods(map);
        return R.ok().put("data", fatherGoodsEntities);

    }

    @RequestMapping(value = "/cgGetCgGoods/{resId}")
    @ResponseBody
    public R cgGetCgGoods(@PathVariable Integer resId) {
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", resId);
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = resComGoodsService.queryCgManagedGoods(map);
        return R.ok().put("data", fatherGoodsEntities);
    }


    //comData小程序中 客户管理中
    @RequestMapping(value = "/resGetResGoods/{resId}")
    @ResponseBody
    public R resGetResGoods(@PathVariable Integer resId) {
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", resId);
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = resComGoodsService.queryResManagedGoods(map);
        return R.ok().put("data", fatherGoodsEntities);
    }


    @RequestMapping(value = "/chainManEditResComGoods", method = RequestMethod.POST)
    @ResponseBody
    public R chainManEditResComGoods(@RequestBody NxCommunityGoodsEntity comGoods) {

        List<NxRestrauntEntity> nxRestrauntEntities = comGoods.getNxRestrauntEntities();
        for (NxRestrauntEntity res : nxRestrauntEntities) {
            if (res.getIsSelected()) {
                Integer nxRestrauntFatherId = res.getNxRestrauntId();
                Integer nxCommunityGoodsId = comGoods.getNxCommunityGoodsId();
                Map<String, Object> map = new HashMap<>();
                map.put("resFatherId", nxRestrauntFatherId);
                map.put("comGoodsId", nxCommunityGoodsId);
                List<NxRestrauntComGoodsEntity> nxRestrauntComGoodsEntities = resComGoodsService.queryResComGoodsByParams(map);
                if (nxRestrauntComGoodsEntities.size() == 0) {
                    NxRestrauntComGoodsEntity resComGoods = new NxRestrauntComGoodsEntity();
                    resComGoods.setNxRcgRestrauntFatherId(res.getNxRestrauntId());
                    resComGoods.setNxRcgComGoodsId(nxCommunityGoodsId);
                    resComGoods.setNxRcgComGoodsFatherId(comGoods.getNxCgCfgGoodsFatherId());
                    resComGoods.setNxRcgComGoodsName(comGoods.getNxCgGoodsName());
                    resComGoods.setNxRcgComGoodsPinyin(comGoods.getNxCgGoodsPinyin());
                    resComGoods.setNxRcgComGoodsPy(comGoods.getNxCgGoodsPy());
                    resComGoods.setNxRcgComGoodsStandardname(comGoods.getNxCgGoodsStandardname());
                    resComGoods.setNxRcgComGoodsDetail(comGoods.getNxCgGoodsDetail());
                    resComGoods.setNxRcgComGoodsBrand(comGoods.getNxCgGoodsBrand());
                    resComGoods.setNxRcgComGoodsPlace(comGoods.getNxCgGoodsPlace());
                    resComGoodsService.save(resComGoods);
                }
            }else{
                Integer nxRestrauntFatherId = res.getNxRestrauntId();
                Integer nxCommunityGoodsId = comGoods.getNxCommunityGoodsId();
                Map<String, Object> map = new HashMap<>();
                map.put("resFatherId", nxRestrauntFatherId);
                map.put("comGoodsId", nxCommunityGoodsId);
                List<NxRestrauntComGoodsEntity> nxRestrauntComGoodsEntities = resComGoodsService.queryResComGoodsByParams(map);
                System.out.println("isNOtselelelelnototntototo");
                System.out.println(nxRestrauntComGoodsEntities);
                if(nxRestrauntComGoodsEntities.size() > 0){
                    for (NxRestrauntComGoodsEntity com:nxRestrauntComGoodsEntities) {
                        resComGoodsService.delete(com.getNxRestrauntComGoodsId());
                    }
                }
            }
        }
        return R.ok();
    }

    @RequestMapping(value = "/chainManAddComGoods", method = RequestMethod.POST)
    @ResponseBody
    public R chainManAddComGoods(@RequestBody List<NxRestrauntComGoodsEntity> comGoodsEntities) {
        for (NxRestrauntComGoodsEntity comGoods : comGoodsEntities) {
            comGoods.setNxRcgResKnowDate(formatWhatYearDayTime(0));
            resComGoodsService.save(comGoods);
        }
        return R.ok();
    }

    @RequestMapping(value = "/resManAddComGoods", method = RequestMethod.POST)
    @ResponseBody
    public R resManAddComGoods(@RequestBody NxRestrauntComGoodsEntity resComGoods) {

        resComGoods.setNxRcgResKnowDate(formatWhatYearDayTime(0));
        resComGoodsService.save(resComGoods);
        return R.ok().put("data", resComGoods);
    }

    @RequestMapping(value = "/cancleResComGoods/{resComGoodsId}")
    @ResponseBody
    public R cancleResComGoods(@PathVariable Integer resComGoodsId) {

        //是否有订单
        NxRestrauntComGoodsEntity nxRestrauntComGoodsEntity = resComGoodsService.queryObject(resComGoodsId);
        Integer nxRcgRestrauntFatherId = nxRestrauntComGoodsEntity.getNxRcgRestrauntFatherId();
        Integer nxRcgComGoodsId = nxRestrauntComGoodsEntity.getNxRcgComGoodsId();
        Map<String, Object> map = new HashMap<>();
        map.put("resFatherId", nxRcgRestrauntFatherId);
        map.put("comGoodsId", nxRcgComGoodsId);
        map.put("status", 5);
        List<NxRestrauntOrdersEntity> ordersEntities = nxRestrauntOrdersService.queryResOrdersByParams(map);
        if (ordersEntities.size() == 0) {
            //删除店铺商品
            //删除部门个人商品
            Map<String, Object> map1 = new HashMap<>();
            map1.put("comGoodsId", nxRcgComGoodsId);
            map1.put("resFatherId", nxRcgRestrauntFatherId);
            resComGoodsService.deleteResComGoods(map1);

            return R.ok();

        } else {
            return R.error(-1, "此商品下有订单");
        }
    }



    @RequestMapping(value = "/resChainGetResChainGoods/{resId}")
    @ResponseBody
    public R resChainGetResChainGoods(@PathVariable Integer resId) {
        List<NxRestrauntEntity> restrauntEntities = nxRestrauntService.queryChainRestrauntsByResId(resId);
        List<Integer> ids = new ArrayList<>();
        for (NxRestrauntEntity res : restrauntEntities) {
            Integer nxRestrauntId = res.getNxRestrauntId();
            ids.add(nxRestrauntId);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("ids", ids);
//        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities =  resComGoodsService.queryChainResGoodsByParams(map);
        List<NxRestrauntEntity> fatherGoodsEntities =  resComGoodsService.queryChainResGoodsByParams(map);

        return R.ok().put("data", fatherGoodsEntities);
    }
    @RequestMapping(value = "/chainManGetComGoods", method = RequestMethod.POST)
    @ResponseBody
    public R chainManGetComGoods(Integer limit, Integer page, String ids, Integer fatherId,
                                 Integer serviceLevel) {
        String[] arr = ids.split(",");
        System.out.println(Arrays.toString(arr));
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("cgFatherId", fatherId);
        map.put("serviceLevel", serviceLevel);
        map.put("ids", arr);

        List<NxCommunityGoodsEntity> goodsEntities1 = cgService.queryChainComResGoodsByParams(map);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("fatherId", fatherId);
        map3.put("serviceLevel", serviceLevel);

        int total = cgService.queryTotalByFatherId(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }
    @RequestMapping(value = "/chainManQueryComResGoodsInFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R chainManQueryComResGoodsInFatherId(String ids, Integer fatherId,Integer serviceLevel
     , String searchStr) {
        String[] arr = ids.split(",");
        System.out.println(Arrays.toString(arr));
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchStrPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }

        map.put("cgFatherId", fatherId);
        map.put("serviceLevel", serviceLevel);
        map.put("ids", arr);
        List<NxCommunityGoodsEntity> goodsEntities1 = cgService.cgQueryCgMangementGoodsQuickSearchStr(map);
        return R.ok().put("data", goodsEntities1);
    }


    @RequestMapping(value = "/resManGetComGoods", method = RequestMethod.POST)
    @ResponseBody
    public R resManGetComGoods(Integer limit, Integer page, Integer resId, Integer fatherId,
                               Integer serviceLevel) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("cgFatherId", fatherId);
        map.put("serviceLevel", serviceLevel);
        map.put("resFatherId", resId);
        List<NxCommunityGoodsEntity> goodsEntities1 = cgService.queryComResGoodsByParams(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("fatherId", fatherId);
        map3.put("serviceLevel", serviceLevel);
        map3.put("resFatherId", resId);

        int total = cgService.queryTotalByFatherId(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/resGetHistoryGoods/{resId}")
    @ResponseBody
    public R resGetHistoryGoods(@PathVariable Integer resId) {

        NxRestrauntEntity nxRestrauntEntity = nxRestrauntService.queryObject(resId);
        nxRestrauntEntity.setNxRestrauntAddCount(nxRestrauntEntity.getNxRestrauntAddCount() + 1);
        nxRestrauntService.update(nxRestrauntEntity);

        Integer serviceLevel = nxRestrauntEntity.getNxRestrauntServiceLevel();
        Map<String, Object> map = new HashMap<>();
        map.put("resId", resId);
        map.put("level", serviceLevel);
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = resComGoodsService.queryHistoryGoods(map);
        return R.ok().put("data", fatherGoodsEntities);

    }






    //todo nouseful
    @RequestMapping(value = "/resGetResComGoodsCata/{resId}")
    @ResponseBody
    public R resGetResComGoodsCata(@PathVariable Integer resId) {

        NxRestrauntEntity nxRestrauntEntity = nxRestrauntService.queryObject(resId);
        Integer nxRestrauntServiceLevel = nxRestrauntEntity.getNxRestrauntServiceLevel();
        Map<String, Object> map = new HashMap<>();
        map.put("resId", resId);
        map.put("level", nxRestrauntServiceLevel);

        System.out.println(map);
        System.out.println("heereemapd");

        List<NxCommunityFatherGoodsEntity> disGoodsEntities = resComGoodsService.resGetResComGoodsCata(map);
        return R.ok().put("data", disGoodsEntities);
    }

    @RequestMapping(value = "/updateResComGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updateResComGoods (@RequestBody NxRestrauntComGoodsEntity resComGoods) {
        System.out.println(resComGoods.getNxRcgResContractOrderQuantity() + "currentTarget");
        resComGoodsService.update(resComGoods);
        return R.ok();
    }


}
