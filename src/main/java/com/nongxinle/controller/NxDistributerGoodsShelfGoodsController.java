package com.nongxinle.controller;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-09 18:47
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;
import com.nongxinle.service.NxDistributerGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxDistributerGoodsShelfGoodsService;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/nxdistributergoodsshelfgoods")
public class NxDistributerGoodsShelfGoodsController {
    @Autowired
    private NxDistributerGoodsShelfGoodsService nxDisGoodsShelfGoodsService;
    @Autowired
    private NxDistributerGoodsService dgService;


    @RequestMapping(value = "/queryDisShelfGoods", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisShelfGoods(String searchStr, String disId) {
        System.out.println(searchStr);
        Map<String, Object> map = new HashMap<>();
        String pinyinString = searchStr;
        map.put("disId", disId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchStr", searchStr);
        map.put("searchPinyin", pinyinString);

        List<NxDistributerGoodsShelfGoodsEntity> goodsEntities = dgService.queryDisShelfGoods(map);
//        map.put("searchPinyin", pinyinString);
//        map.put("searchStr", null);
//
        List<NxDistributerGoodsShelfGoodsEntity> goodsEntitiesPiyin = dgService.queryDisShelfGoods(map);
//        goodsEntitiesPiyin.removeAll(goodsEntities);
//        List<NxDistributerGoodsShelfGoodsEntity> all = new ArrayList<>();
//        all.addAll(goodsEntities);
//        all.addAll(goodsEntitiesPiyin);
        return R.ok().put("data", goodsEntities);
    }


    @RequestMapping(value = "/addShelfGoods", method = RequestMethod.POST)
    @ResponseBody
    public R addShelfGoods(@RequestBody List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsList) {

        NxDistributerGoodsShelfGoodsEntity nxDistributerGoodsShelfGoodsEntity = shelfGoodsList.get(0);
        Integer nxDgsgSort = nxDistributerGoodsShelfGoodsEntity.getNxDgsgSort();
        Integer nxDgsgShelfId = nxDistributerGoodsShelfGoodsEntity.getNxDgsgShelfId();

        Map<String, Object> map = new HashMap<>();
        map.put("dayuSort", nxDgsgSort - 1);
        map.put("shelfId", nxDgsgShelfId);
        System.out.println("kkmap====" + map);
        List<NxDistributerGoodsShelfGoodsEntity> nxDistGoodsShelfEntities = nxDisGoodsShelfGoodsService.queryShelfForGoodsByParams(map);
        System.out.println(nxDistGoodsShelfEntities.size());
        for (int i = 0; i < nxDistGoodsShelfEntities.size(); i++) {
            int size = shelfGoodsList.size();
            nxDistGoodsShelfEntities.get(i).setNxDgsgSort(size + i + nxDgsgSort);
            nxDisGoodsShelfGoodsService.update(nxDistGoodsShelfEntities.get(i));
        }

        for (NxDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsList) {
            nxDisGoodsShelfGoodsService.save(shelfGoods);
        }

        return R.ok();
    }

    @RequestMapping(value = "/updateShelfGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updateShelfGoods(@RequestBody NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity) {
        nxDisGoodsShelfGoodsService.update(shelfGoodsEntity);
        return R.ok();
    }


    @RequestMapping(value = "/updateShelfGoodsSort", method = RequestMethod.POST)
    @ResponseBody
    public R updateShelfGoodsSort(@RequestBody List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsList) {
        for (NxDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsList) {
            nxDisGoodsShelfGoodsService.update(shelfGoods);
        }
        return R.ok();
    }

    @RequestMapping(value = "/deleteShelfGoods/{id}")
    @ResponseBody
    public R deleteShelfGoods(@PathVariable Integer id) {
//		NxDistributerGoodsShelfGoodsEntity nxDisGoodsShelfGoodsEntity = nxDisGoodsShelfGoodsService.queryObject(id);
//		Integer nxDgsgDisGoodsId = nxDisGoodsShelfGoodsEntity.getNxDgsgDisGoodsId();
//		Map<String, Object> map = new HashMap<>();
//		map.put("disGoodsId", nxDgsgDisGoodsId);
//		map.put("status", 4);
//		List<NxDistributerPurchaseGoodsEntity> disPurGoodsEntities = nxDisPurchaseGoodsService.queryForDisGoods(map);
//        if(disPurGoodsEntities.size() > 0){
//        	return R.error(-1, "有订货");
//		}else{
//			nxDisGoodsShelfGoodsService.delete(id);
//			return R.ok();
//		}


        NxDistributerGoodsShelfGoodsEntity nxDisGoodsShelfGoods = nxDisGoodsShelfGoodsService.queryObject(id);
        Integer nxDgsgSort = nxDisGoodsShelfGoods.getNxDgsgSort();
        Integer nxDgsgShelfId = nxDisGoodsShelfGoods.getNxDgsgShelfId();
        Map<String, Object> map = new HashMap<>();
        map.put("dayuSort", nxDgsgSort - 1);
        map.put("shelfId", nxDgsgShelfId);
        List<NxDistributerGoodsShelfGoodsEntity> nxDistGoodsShelfEntities = nxDisGoodsShelfGoodsService.queryShelfForGoodsByParams(map);
        System.out.println(nxDistGoodsShelfEntities.size());
        for (int i = 0; i < nxDistGoodsShelfEntities.size(); i++) {
            NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity = nxDistGoodsShelfEntities.get(i);
            shelfGoodsEntity.setNxDgsgSort(shelfGoodsEntity.getNxDgsgSort() - 1);
            nxDisGoodsShelfGoodsService.update(shelfGoodsEntity);
        }
        nxDisGoodsShelfGoodsService.delete(id);
        return R.ok();

    }

}
