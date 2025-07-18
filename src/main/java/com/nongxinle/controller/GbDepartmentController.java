package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-18 21:32
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/gbdepartment")
public class GbDepartmentController {
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDistributerUserService gbDistributerUserService;
    @Autowired
    private GbDepartmentUserService gbDepartmentUserService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private NxDistributerGbDistributerService nxDistributerGbDistributerService;
    @Autowired
    private SysCityMarketService sysCityMarketService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private NxJrdhSupplierService nxJrdhSupplierService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDPBService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxDepartmentBillService nxDepartmentBillService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;


    @RequestMapping(value = "/saveSubDepartment", method = RequestMethod.POST)
    @ResponseBody
    public R saveSubDepartment(@RequestBody GbDepartmentEntity subDeps) {

        Integer gbDepartmentFatherId = subDeps.getGbDepartmentFatherId();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(gbDepartmentFatherId);

        subDeps.setGbDepartmentSettleFullTime(formatFullTime());
        subDeps.setGbDepartmentSettleDate(formatWhatDay(0));
        subDeps.setGbDepartmentSettleMonth(formatWhatMonth(0));
        subDeps.setGbDepartmentSettleWeek(getWeekOfYear(0).toString());
        subDeps.setGbDepartmentSettleYear(formatWhatYear(0));
        subDeps.setGbDepartmentSettleTimes("0");
        subDeps.setGbDepartmentSubAmount(0);
        subDeps.setGbDepartmentIsGroupDep(0);
        subDeps.setGbDepartmentAttrName(subDeps.getGbDepartmentName());
        Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDepartmentDisId);
        subDeps.setGbDepartmentPrintName(gbDistributerEntity.getGbDistributerPrintName());
        String gbDepartmentName = subDeps.getGbDepartmentName();
        String headPinyin = getHeadStringByString(gbDepartmentName, false, null);
        subDeps.setGbDepartmentNamePy(headPinyin);
        subDeps.setGbDepartmentType(departmentEntity.getGbDepartmentType());
        subDeps.setGbDepartmentDisId(departmentEntity.getGbDepartmentDisId());
        subDeps.setGbDepartmentFatherId(departmentEntity.getGbDepartmentId());
        subDeps.setGbDepartmentDepSettleId(-1);
        subDeps.setGbDepartmentLevel(1);
        gbDepartmentService.save(subDeps);

        departmentEntity.setGbDepartmentSubAmount(departmentEntity.getGbDepartmentSubAmount() + 1);
        gbDepartmentService.update(departmentEntity);
        return R.ok().put("data", gbDistributerService.queryDistributerInfo(departmentEntity.getGbDepartmentDisId()));

    }


    @RequestMapping(value = "/changeSingleMendian", method = RequestMethod.POST)
    @ResponseBody
    public R changeSingleMendian(@RequestBody GbDepartmentEntity departmentEntity) {

        Integer gbDepartmentId = departmentEntity.getGbDepartmentId();
        Map<String, Object> map = new HashMap<>();
        map.put("depId", gbDepartmentId);
        map.put("status", 3);
        System.out.println("akdakfdlfl;saffdaf;a" + map);
        Integer integer = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
        if(integer > 0){
            return R.error(-1,"有未完成订单");
        }else{
            List<GbDepartmentEntity> gbDepartmentEntityList = departmentEntity.getGbDepartmentEntityList();
            if (gbDepartmentEntityList.size() > 0) {
                for (GbDepartmentEntity subDeps : gbDepartmentEntityList) {
                    subDeps.setGbDepartmentSettleFullTime(formatFullTime());
                    subDeps.setGbDepartmentSettleDate(formatWhatDay(0));
                    subDeps.setGbDepartmentSettleMonth(formatWhatMonth(0));
                    subDeps.setGbDepartmentSettleWeek(getWeekOfYear(0).toString());
                    subDeps.setGbDepartmentSettleYear(formatWhatYear(0));
                    subDeps.setGbDepartmentSettleTimes("0");
                    subDeps.setGbDepartmentSubAmount(0);
                    subDeps.setGbDepartmentIsGroupDep(0);
                    subDeps.setGbDepartmentAttrName(subDeps.getGbDepartmentName());
                    subDeps.setGbDepartmentPrintName("ApplyHalfPanel");
                    String gbDepartmentName = subDeps.getGbDepartmentName();
                    String headPinyin = getHeadStringByString(gbDepartmentName, false, null);
                    subDeps.setGbDepartmentNamePy(headPinyin);
                    subDeps.setGbDepartmentType(departmentEntity.getGbDepartmentType());
                    subDeps.setGbDepartmentDisId(departmentEntity.getGbDepartmentDisId());
                    subDeps.setGbDepartmentFatherId(departmentEntity.getGbDepartmentId());
                    subDeps.setGbDepartmentDepSettleId(-1);
                    subDeps.setGbDepartmentLevel(1);
                    gbDepartmentService.save(subDeps);
                }
                departmentEntity.setGbDepartmentSubAmount(departmentEntity.getGbDepartmentEntityList().size());
                gbDepartmentService.update(departmentEntity);
            }

            


            return R.ok().put("data", gbDistributerService.queryDistributerInfo(departmentEntity.getGbDepartmentDisId()));
        }


    }


    @RequestMapping(value = "/purUserSaveMendain", method = RequestMethod.POST)
    @ResponseBody
    public R purUserSaveMendain(@RequestBody GbDepartmentEntity depart) {
        depart.setGbDepartmentSubAmount(depart.getGbDepartmentEntityList().size());
        String gbDepartmentName = depart.getGbDepartmentName();
        String headPinyin = getHeadStringByString(gbDepartmentName, false, null);
        depart.setGbDepartmentNamePy(headPinyin);
        depart.setGbDepartmentAttrName(depart.getGbDepartmentName());
        depart.setGbDepartmentPrintName("ApplyHalfPanel");
        depart.setGbDepartmentPrintSet(0);
        GbDepartmentEntity departmentEntity = gbDepartmentService.saveNewDepartmentGb(depart);
        Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(gbDepartmentDisId);

        return R.ok().put("data", gbDistributerEntity);
    }


    @RequestMapping(value = "/peisongGetYishangCata/{depId}")
    @ResponseBody
    public R peisongGetYishangCata(@PathVariable Integer depId) {

        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        List<SysCityMarketEntity> marketEntities = sysCityMarketService.queryMarketNxDisByParams(map);
        return R.ok().put("data", marketEntities);
    }


    @RequestMapping(value = "/peisongDepGetNxDistributer/{depId}")
    @ResponseBody
    public R peisongDepGetNxDistributer(@PathVariable Integer depId) {
        List<NxDistributerGbDistributerEntity> distributerEntities = nxDistributerGbDistributerService.queryGbDistributerNxDistribtuer(depId);

        if(distributerEntities.size() > 0){
            for(NxDistributerGbDistributerEntity distributerGbDistributerEntity: distributerEntities){
                Integer nxDgdNxDistributerId = distributerGbDistributerEntity.getNxDgdNxDistributerId();

                NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDgdNxDistributerId);

                Map<String, Object> map = new HashMap<>();
                map.put("disId", distributerEntity.getNxDistributerId());
                map.put("gbDisId", distributerGbDistributerEntity.getNxDgdGbDistributerId());
                map.put("equalStatus", 0);
                int i = nxDepartmentBillService.queryBillsCount(map);
                double total = 0.0;
                if(i > 0){
                    total = nxDepartmentBillService.queryBillCostSubtotalByParams(map);
                }

                distributerEntity.setTotal(String.format("%.1f", total));
                distributerEntity.setBillCount(i);
                distributerGbDistributerEntity.setNxDistributerEntity(distributerEntity);
            }
        }

        return R.ok().put("data", distributerEntities);
    }


    @RequestMapping(value = "/peisongDepDeleteNxDistributer", method = RequestMethod.POST)
    @ResponseBody
    public R peisongDepDeleteNxDistributer(Integer depId, Integer nxDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("nxDisId", nxDisId);
        NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDistributerGbDistributerService.queryObjectByParams(map);
        if (nxDistributerGbDistributerEntity != null) {
            nxDistributerGbDistributerService.delete(nxDistributerGbDistributerEntity.getNxDistributerGbDistributerId());
        }

        return R.ok();
    }

    @RequestMapping(value = "/peisongDepGetNxDistributerGoods", method = RequestMethod.POST)
    @ResponseBody
    public R peisongDepGetNxDistributerGoods(Integer depId, Integer nxGoodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("nxGoodsId", nxGoodsId);
        List<NxDistributerEntity> distributerEntities = nxDistributerGbDistributerService.queryGbDistributerNxDistribtuerGoods(map);
        return R.ok().put("data", distributerEntities);

    }


    //	@RequestMapping(value = "/getDisStockOrdersGoods", method = RequestMethod.POST)
//	@ResponseBody
//	public R getDisStockOrdersGoods (Integer disId, Integer goodsType, Integer depId) {
//
//		Map<String, Object> map = new HashMap<>();
////		map.put("disId", disId);
////		map.put("goodsType", goodsType);
//		map.put("orderType",getGbOrderTypeChuKu() );
//		map.put("depId", depId);
//		map.put("status", 3);
//		List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentService.queryApplyOutStockGoodsDeps(map);
//
//	    return R.ok().put("data", gbDepartmentEntities);
//	}
    @RequestMapping(value = "/getDepInfoGb/{depId}")
    @ResponseBody
    public R getDepInfoGb(@PathVariable Integer depId) {
        System.out.println(depId + "idiid");
        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryDepInfoGb(depId);
        return R.ok().put("data", gbDepartmentEntity);
    }

    @RequestMapping(value = "/getSubDepartmentsGb/{depId}")
    @ResponseBody
    public R getSubDepartmentsGb(@PathVariable Integer depId) {
        System.out.println(depId);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depId);
        return R.ok().put("data", departmentEntities);
    }

    @RequestMapping(value = "/getDisDepartmentGbMendian/{disId}")
    @ResponseBody
    public R getDisDepartmentGbMendian(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("depType", getGbDepartmentTypeJiameng());
        List<GbDepartmentEntity> gbDepartmentEntities1 = gbDepartmentService.queryGroupDepsByDisId(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("zhiyingArr", gbDepartmentEntities);
        map2.put("jiamengArr", gbDepartmentEntities1);
        return R.ok().put("data", map2);
    }

    @RequestMapping(value = "/getDisDepartmentGbMendianJing/{disId}")
    @ResponseBody
    public R getDisDepartmentGbMendianJing(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);


        return R.ok().put("data", gbDepartmentEntities);
    }

    @RequestMapping(value = "/getDisDepartmentGbMendianWithBill/{disId}")
    @ResponseBody
    public R getDisDepartmentGbMendianWithBill(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depType", getGbDepartmentTypeMendian());
        System.out.println("havbidididiid" + map);
        List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentService.queryGroupDepsByDisIdWithUnPayBill(map);

        map.put("depType", getGbDepartmentTypeJiameng());
        List<GbDepartmentEntity> gbDepartmentEntities1 = gbDepartmentService.queryGroupDepsByDisIdWithUnPayBill(map);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("zhiyingArr", gbDepartmentEntities);
        map2.put("jiamengArr", gbDepartmentEntities1);
        return R.ok().put("data", map2);
    }


    @RequestMapping(value = "/getDisDepartmentGb")
    @ResponseBody
    public R getDisDepartmentGb(Integer disId, Integer type) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depType", type);
        List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);
        return R.ok().put("data", gbDepartmentEntities);
    }

    @RequestMapping(value = "/saveGbDepartment", method = RequestMethod.POST)
    @ResponseBody
    public R saveGbDepartment(@RequestBody GbDepartmentEntity department) {

        System.out.println("nudlldld" + department.getFatherGoodsIds());
        String gbDepartmentName = department.getGbDepartmentName();
        String headPinyin = getHeadStringByString(gbDepartmentName, false, null);
        department.setGbDepartmentNamePy(headPinyin);
        department.setGbDepartmentPrintSet(0);
        department.setGbDepartmentLevel(0);
        Integer gbDepartmentDisId = department.getGbDepartmentDisId();
        GbDistributerEntity gbDistributerEntity1 = gbDistributerService.queryObject(gbDepartmentDisId);
        department.setGbDepartmentPrintName(gbDistributerEntity1.getGbDistributerPrintName());
        if (department.getGbDepartmentType().equals(getGbDepartmentTypeMendian()) || department.getGbDepartmentType().equals(getGbDepartmentTypeJiameng())) {
            if (department.getCankaoDepId() > 0) {
                gbDepartmentService.saveNewDepartmentGbWithDepGoods(department, department.getCankaoDepId());
            } else {
                gbDepartmentService.saveNewDepartmentGb(department);
            }
        } else {
            gbDepartmentService.saveNewDepartmentGb(department);
        }

        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(department.getGbDepartmentDisId());
        return R.ok().put("data", gbDistributerEntity);
    }

    /**
     * PURCHASE
     * 采购员注册
     *
     * @return 群信息
     */
    @RequestMapping(value = "/depManRegisterNewChainDepartmentGb", method = RequestMethod.POST)
    @ResponseBody
    public R depManRegisterNewChainDepartmentGb(@RequestBody GbDepartmentEntity gbDepartmentEntity) {
//wxApp
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        GbDistributerUserEntity distributerUserEntity = gbDepartmentEntity.getGbDistributerUserEntity();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongAppID() +
                "&secret=" + myAPPIDConfig.getTexiansongScreat() + "&js_code=" + distributerUserEntity.getGbDiuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        GbDistributerUserEntity gbDistributerUserEntity = gbDistributerUserService.queryDisUserByOpenIdGb(openid);

        if (gbDistributerUserEntity == null) {
            gbDepartmentEntity.getGbDistributerUserEntity().setGbDiuWxOpenId(openid);
            Integer resUserId = gbDepartmentService.saveNewChainDepartmentGb(gbDepartmentEntity);

            if (resUserId != null) {
                Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(resUserId);
                return R.ok().put("data", stringObjectMap);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }
    }


    @RequestMapping(value = "/queryUnLineStore/{disId}")
    @ResponseBody
    public R queryUnLineStore(@PathVariable Integer disId) {
        List<GbDepartmentEntity> unLineDeps = gbDepartmentService.queryUnLineDepsByDisId(disId);
        return R.ok().put("data", unLineDeps);
    }

    @RequestMapping(value = "/getGroupJicai/{disId}")
    @ResponseBody
    public R getGroupJicai(@PathVariable Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depType", getGbDepartmentTypeJicai());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);
        return R.ok().put("data", departmentEntities);
    }

    @RequestMapping(value = "/getGbDisPurchaseDepartment/{disId}")
    @ResponseBody
    public R getGbDisPurchaseDepartment(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depType", getGbDepartmentTypeKufang());
        List<GbDepartmentEntity> kufangDpartmentEntities = countJrdhData(gbDepartmentService.queryGroupDepsByDisId(map));

        map.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> mendianDepartmentEntities = countJrdhData(gbDepartmentService.queryGroupDepsByDisId(map));

        map.put("depType", getGbDepartmentTypeKitchen());
        List<GbDepartmentEntity> kitchenDepartmentEntities = countJrdhData(gbDepartmentService.queryGroupDepsByDisId(map));


        map.put("depType", getGbDepartmentTypeJicai());
        List<GbDepartmentEntity> purDepartmentEntities = countJrdhData(gbDepartmentService.queryGroupDepsByDisId(map));

        Map<String, Object> map5 = new HashMap<>();
        map5.put("stock", kufangDpartmentEntities);
        map5.put("mendian", mendianDepartmentEntities);
        map5.put("kitchen", kitchenDepartmentEntities);
        map5.put("purchase", purDepartmentEntities);
        return R.ok().put("data", map5);
    }


    private List<GbDepartmentEntity> countJrdhData(List<GbDepartmentEntity> entityList) {
        if (entityList.size() > 0) {
            for (GbDepartmentEntity departmentEntity : entityList) {

                Integer gbDepartmentId = departmentEntity.getGbDepartmentId();
                Map<String, Object> map = new HashMap<>();
                map.put("gbDepId", gbDepartmentId);
                map.put("equalStatus", 0);
                System.out.println("docuccocuocuc" + map);
                int count = nxJrdhSupplierService.queryJrdhSupplierCount(map);
                if (count > 0) {
                    Map<String, Object> mapB = new HashMap<>();
                    mapB.put("purDepId", gbDepartmentId);
                    mapB.put("payType", 1);
                    mapB.put("status", 4);
                    System.out.println("mapbdbbdbbbbbbbbbb" + mapB);
                    Integer integer = gbDPBService.queryDisPurchaseBatchCount(mapB);
                    if (integer > 0) {
                        Double aDouble = gbDPBService.querySupplierUnSettleSubtotal(mapB);
                        departmentEntity.setDepCostGoodsTotalString(Integer.valueOf(count).toString());
                        departmentEntity.setDepStockSubtotalString(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    } else {
                        departmentEntity.setDepCostGoodsTotalString("0");
                        departmentEntity.setDepStockSubtotalString("0");
                    }
                } else {
                    departmentEntity.setDepCostGoodsTotalString("0");
                    departmentEntity.setDepStockSubtotalString("0");
                }
            }
        }

        return entityList;
    }

    @RequestMapping(value = "/getGbDisTypeDepartment/{disId}")
    @ResponseBody
    public R getGbDisTypeDepartment(@PathVariable Integer disId) {
        Map<String, Object> map0 = new HashMap<>();
        map0.put("disId", disId);
        map0.put("depType", getGbDepartmentTypeKufang());
        List<GbDepartmentEntity> departmentEntitiesK = gbDepartmentService.queryGroupDepsByDisId(map0);


        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depType", getGbDepartmentTypeJicai());
        List<GbDepartmentEntity> departmentEntitiesJ = gbDepartmentService.queryGroupDepsByDisId(map);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntitiesM = gbDepartmentService.queryGroupDepsByDisId(map1);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("jicai", departmentEntitiesJ);
        map2.put("stock", departmentEntitiesK);
        map2.put("mendian", departmentEntitiesM);
        return R.ok().put("data", map2);
    }


    @RequestMapping(value = "/getGroupStockRooms/{disId}")
    @ResponseBody
    public R getGroupStockRooms(@PathVariable Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depType", 2);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);
        return R.ok().put("data", departmentEntities);
    }

    @RequestMapping(value = "/getGroupTypeDeps")
    @ResponseBody
    public R getGroupTypeDeps(Integer disId, Integer type) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depType", type);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);
        return R.ok().put("data", departmentEntities);
    }

    @RequestMapping(value = "/getGroupDeps/{disId}")
    @ResponseBody
    public R getGroupDeps(@PathVariable Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depType", 1);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);
        return R.ok().put("data", departmentEntities);
    }


    @RequestMapping(value = "/updateGroupNameGb", method = RequestMethod.POST)
    @ResponseBody
    public R updateGroupNameGb(@RequestBody GbDepartmentEntity departmentEntity) {

        departmentEntity.setGbDepartmentAttrName(departmentEntity.getGbDepartmentName());
        departmentEntity.setGbDepartmentPrintName("ApplyHalfPanel");
        String gbDepartmentName = departmentEntity.getGbDepartmentName();
        String headPinyin = getHeadStringByString(gbDepartmentName, false, null);
        departmentEntity.setGbDepartmentNamePy(headPinyin);
        gbDepartmentService.update(departmentEntity);

        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(departmentEntity.getGbDepartmentDisId());
        return R.ok().put("data", gbDistributerEntity);
    }


    @RequestMapping(value = "/deleteDepartmentSetGoods/{depId}")
    @ResponseBody
    public R deleteDepartmentSetGoods(@PathVariable Integer depId) {
        //1,yonghu
        List<GbDepartmentUserEntity> gbDepartmentUserEntities = gbDepartmentUserService.queryAllUsersByDepId(depId);
        //2,
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", depId);
        List<GbDistributerGoodsEntity> gbDisGoodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(map);

        if (gbDepartmentUserEntities.size() > 0) {
            return R.error(-1, "已经有用户，不能删除。");
        } else if (gbDisGoodsEntities.size() > 0) {
            return R.error(-1, "有商品设置采购部门，不能删除。");
        } else {
            List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depId);
            if (departmentEntities.size() > 0) {
                for (GbDepartmentEntity dep : departmentEntities) {
                    gbDepartmentService.delete(dep.getGbDepartmentId());
                }
            }
            GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depId);
            Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(gbDepartmentDisId);

            gbDepartmentService.delete(depId);

            return R.ok().put("data", gbDistributerEntity);
        }
    }


    @RequestMapping(value = "/deleteMendian/{depId}")
    @ResponseBody
    public R deleteMendian(@PathVariable Integer depId) {
        List<GbDepartmentUserEntity> gbDepartmentUserEntities = gbDepartmentUserService.queryAllUsersByDepId(depId);
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depId);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        if (gbDepartmentUserEntities.size() > 0 || ordersEntities.size() > 0) {
            return R.error(-1, "有部门相关数据，暂无法删除。");
        } else {
            List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depId);
            if (departmentEntities.size() > 0) {
                for (GbDepartmentEntity dep : departmentEntities) {
                    gbDepartmentService.delete(dep.getGbDepartmentId());
                }
            }

            List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map);
            if (departmentDisGoodsEntities.size() > 0) {
                for (GbDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                    gbDepartmentDisGoodsService.delete(departmentDisGoodsEntity.getGbDepartmentDisGoodsId());
                }
            }

            GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depId);

            Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();

            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(gbDepartmentDisId);

            gbDepartmentService.delete(depId);

            return R.ok().put("data", gbDistributerEntity);
        }
    }


    @RequestMapping(value = "/deleteDepartment/{depId}")
    @ResponseBody
    public R deleteDepartment(@PathVariable Integer depId) {
        List<GbDepartmentUserEntity> gbDepartmentUserEntities = gbDepartmentUserService.queryAllUsersByDepId(depId);
        System.out.println("depusueureeee" + gbDepartmentUserEntities);
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        System.out.println("mapapa" + map);

        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depId);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        List<GbDepartmentGoodsStockEntity> departmentGoodsStockEntities = gbDepartmentGoodsStockService.queryGoodsStockByParams(map);
        if (gbDepartmentUserEntities.size() > 0 || ordersEntities.size() > 0 || departmentGoodsStockEntities.size() > 0) {
            return R.error(-1, "有部门相关数据，暂无法删除。");
        } else {

            List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map);
            if (departmentDisGoodsEntities.size() > 0) {
                for (GbDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                    gbDepartmentDisGoodsService.delete(departmentDisGoodsEntity.getGbDepartmentDisGoodsId());
                }
            }

            Integer gbDepartmentFatherId = departmentEntity.getGbDepartmentFatherId();
            GbDepartmentEntity fatherDep = gbDepartmentService.queryObject(gbDepartmentFatherId);
            fatherDep.setGbDepartmentSubAmount(fatherDep.getGbDepartmentSubAmount() - 1);
            gbDepartmentService.update(fatherDep);

            Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(gbDepartmentDisId);
            gbDepartmentService.delete(depId);

            return R.ok().put("data", gbDistributerEntity);
        }
    }


}
