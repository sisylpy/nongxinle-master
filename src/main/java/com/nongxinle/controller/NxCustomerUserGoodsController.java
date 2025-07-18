package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.entity.NxCommunityOrdersSubEntity;
import com.nongxinle.entity.NxCustomerUserCardEntity;
import com.nongxinle.service.NxCommunityOrdersSubService;
import com.nongxinle.service.NxCustomerUserCardService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCustomerUserGoodsEntity;
import com.nongxinle.service.NxCustomerUserGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.getNowMinute;


@RestController
@RequestMapping("api/nxcustomerusergoods")
public class NxCustomerUserGoodsController {

    @Autowired
    private NxCustomerUserGoodsService nxCustomerUserGoodsService;
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;

    @Autowired
    private NxCustomerUserCardService nxCustomerUserCardService;

    @RequestMapping(value = "/userAddLoveGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveMyLove(Integer userId, Integer goodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("goodsId", goodsId);
        NxCustomerUserGoodsEntity customerUserGoodsEntity = nxCustomerUserGoodsService.queryUserGoodsByParams(map);
        customerUserGoodsEntity.setNxCugType(1);
        nxCustomerUserGoodsService.update(customerUserGoodsEntity);
        return R.ok();

    }


    @RequestMapping(value = "/addUserGoods", method = RequestMethod.POST)
    @ResponseBody
    public R addUserGoods(@RequestBody NxCustomerUserGoodsEntity userGoodsEntity) {
        Integer nxCugGoodsId = userGoodsEntity.getNxCugGoodsId();

        NxCustomerUserGoodsEntity userGoodsEntity1 = nxCustomerUserGoodsService.queryObject(nxCugGoodsId);
        userGoodsEntity1.setNxCugJoinMyTemplate(1);
        userGoodsEntity1.setNxCugOrderQuantity(userGoodsEntity.getNxCugOrderQuantity());
        userGoodsEntity1.setNxCugOrderStandard(userGoodsEntity.getNxCugOrderStandard());
        nxCustomerUserGoodsService.update(userGoodsEntity1);

        return R.ok();
    }

    @RequestMapping(value = "/getCustomerUserGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getCustomerUserGoods(@RequestBody NxCustomerUserGoodsEntity userGoodsEntity) {

        Map<String, Object> map = new HashMap<>();
        map.put("nxCugUserId", userGoodsEntity.getNxCugUserId());
        map.put("nxCugJoinMyTemplate", userGoodsEntity.getNxCugJoinMyTemplate());
        List<NxCustomerUserGoodsEntity> goodsEntities = nxCustomerUserGoodsService.queryUserGoods(map);

        return R.ok().put("data", goodsEntities);
    }


    @RequestMapping(value = "/userCancleLoveGoods/{id}")
    @ResponseBody
    public R userCancleLoveGoods(@PathVariable Integer id) {

        NxCustomerUserGoodsEntity customerUserGoodsEntity = nxCustomerUserGoodsService.queryObject(id);
        customerUserGoodsEntity.setNxCugType(0);
        nxCustomerUserGoodsService.update(customerUserGoodsEntity);
        return R.ok();
    }

    @RequestMapping(value = "/userGetLoveGoods/{userId}")
    @ResponseBody
    public R userGetLoveGoods(@PathVariable Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("type", 1);
        System.out.println("mapappapaapa" + map);
        List<NxCustomerUserGoodsEntity> nxCustomerUserGoodsEntities = nxCustomerUserGoodsService.queryUserGoods(map);
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", userId);
        mapA.put("status", -1);
        mapA.put("orderType", 0);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("userId", userId);
        mapC.put("status", -1);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("subOrders", nxCommunityOrdersSubEntities);
        mapR.put("cardList", cardEntities);
        mapR.put("goodsArr", nxCustomerUserGoodsEntities);
        return R.ok().put("data", mapR);

//        return R.ok().put("data", nxCustomerUserGoodsEntities);
    }



    @RequestMapping(value = "/customerSearchGoods", method = RequestMethod.POST)
    @ResponseBody
    public R customerSearchGoods(Integer commId, String searchStr, Integer userId, Integer serviceType) {
        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("searchStr", searchStr);
        map.put("userId", userId);
        map.put("nowMinute", getNowMinute());
        map.put("pullOff", 0);
        map.put("serviceType", serviceType);
        System.out.println("searee" + map);
        List<NxCommunityGoodsEntity> communityGoodsEntities = nxCustomerUserGoodsService.userQueryCommGoods(map);


        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", userId);
        mapA.put("status", -1);
        mapA.put("serviceType", serviceType);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("userId", userId);
        mapC.put("status", -1);
        mapC.put("type", 0);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("subOrders", nxCommunityOrdersSubEntities);
        mapR.put("cardList", cardEntities);
        mapR.put("goodsArr", communityGoodsEntities);
        return R.ok().put("data", mapR);
    }

//
//	@RequestMapping(value = "/customerUserQueryToJoin/{customerUserId}")
//	@ResponseBody
//	public R customerUserQueryToJoin(@PathVariable Integer customerUserId) {
//
//
//	    return R.ok().put("data", goodsEntities);
//	}


    /**
     * 列表
     */
    @ResponseBody
    @RequestMapping("/list")
    @RequiresPermissions("nxcustomerusergoods:list")
    public R list(Integer page, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);

        //查询列表数据
        List<NxCustomerUserGoodsEntity> nxCustomerUserGoodsList = nxCustomerUserGoodsService.queryList(map);
        int total = nxCustomerUserGoodsService.queryTotal(map);

        PageUtils pageUtil = new PageUtils(nxCustomerUserGoodsList, total, limit, page);

        return R.ok().put("page", pageUtil);
    }


    /**
     * 信息
     */
    @ResponseBody
    @RequestMapping("/info/{custUGoodsId}")
    @RequiresPermissions("nxcustomerusergoods:info")
    public R info(@PathVariable("custUGoodsId") Integer custUGoodsId) {
        NxCustomerUserGoodsEntity nxCustomerUserGoods = nxCustomerUserGoodsService.queryObject(custUGoodsId);

        return R.ok().put("nxCustomerUserGoods", nxCustomerUserGoods);
    }

    /**
     * 保存
     */
    @ResponseBody
    @RequestMapping("/save")
    @RequiresPermissions("nxcustomerusergoods:save")
    public R save(@RequestBody NxCustomerUserGoodsEntity nxCustomerUserGoods) {
        nxCustomerUserGoodsService.save(nxCustomerUserGoods);

        return R.ok();
    }

    /**
     * 修改
     */
    @ResponseBody
    @RequestMapping("/update")
    @RequiresPermissions("nxcustomerusergoods:update")
    public R update(@RequestBody NxCustomerUserGoodsEntity nxCustomerUserGoods) {
        nxCustomerUserGoodsService.update(nxCustomerUserGoods);

        return R.ok();
    }

    /**
     * 删除
     */
    @ResponseBody
    @RequestMapping("/delete")
    @RequiresPermissions("nxcustomerusergoods:delete")
    public R delete(@RequestBody Integer[] custUGoodsIds) {
        nxCustomerUserGoodsService.deleteBatch(custUGoodsIds);

        return R.ok();
    }

}
