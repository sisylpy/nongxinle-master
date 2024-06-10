package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-18 21:32
 */

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.PageUtilsMap;
import com.sun.xml.internal.xsom.XSUnionSimpleType;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeMendian;
import static com.nongxinle.utils.PinYin4jUtils.*;


@RestController
@RequestMapping("api/gbdistributergoods")
public class GbDistributerGoodsController {

    @Autowired
    private GbDistributerGoodsService gbDgService;
    @Autowired
    private GbDepartmentOrdersService depOrdersService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private GbDistributerFatherGoodsService dgfService;
    @Autowired
    private GbDistributerStandardService dsService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepDisGoodsService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepGoodsStockService;
    @Autowired
    private GbDistributerGoodsShelfGoodsService gbDistributerGoodsShelfGoodsService;
    @Autowired
    private GbDistributerSupplierService gbDistributerSupplierService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepGoodsStockReduceService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDisPurchaseGoodsService;
    @Autowired
    private NxDistributerStandardService nxDistributerStandardService;



    @RequestMapping(value = "/depGetGbAppointSupplierGoods/{supplierId}")
    @ResponseBody
    public R depGetGbAppointSupplierGoods(@PathVariable Integer supplierId) {

        Map<String, Object> mapSup = new HashMap<>();
        mapSup.put("supplierId", supplierId);
        mapSup.put("admin", 0);

        GbDistributerSupplierEntity distributerSupplierEntity = gbDistributerSupplierService.queryAppointSupplierBySupplierId(mapSup);
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", supplierId);
        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryGoodsByParamsGb(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("supplier", distributerSupplierEntity);
        map1.put("goodsArr", goodsEntities);
        return R.ok().put("data", map1);
    }


    @RequestMapping(value = "/purchaserGetGoodsList", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserGetGoodsList(Integer fatherId, Integer inventoryType,
                                   Integer limit, Integer page,
                                   Integer toDepId, Integer type) {
        Map<String, Object> map = new HashMap<>();
        map.put("dgFatherId", fatherId);
        if (inventoryType != -1) {
            map.put("inventoryType", inventoryType);
        }
        if (toDepId != -1) {
            map.put("toDepId", toDepId);
        }
        if (type != -1) {
            map.put("type", 3);
        }
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println(map);
        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDgService.queryPurchaserDisGoodsByParams(map);
        int total = gbDgService.queryDisGoodsCount(map);
        PageUtils pageUtil = new PageUtils(gbDistributerGoodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/purchaserGetGoodsFatherWithSub", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserGetGoodsFatherWithSub(Integer disId, Integer toDepId, Integer type) {

        Map<String, Object> map = new HashMap<>();
        if (type != -1) {
            map.put("type", 3);
        }
        if (toDepId != -1) {
            map.put("toDepId", toDepId);
        }
        if (disId != -1) {
            map.put("disId", disId);
        }
        System.out.println(map);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDgService.queryDisFatherGoodsByParams(map);


        if (fatherGoodsEntities.size() > 0) {
            List<GbDistributerFatherGoodsEntity> newList = new ArrayList<>();
            for (GbDistributerFatherGoodsEntity greatGrandGoods : fatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grandGoods : greatGrandGoods.getFatherGoodsEntities()) {
                    for (GbDistributerFatherGoodsEntity fatherGoods : grandGoods.getFatherGoodsEntities()) {
                        StringBuilder builder = new StringBuilder();
                        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryDgSubNameByFatherIdGb(fatherGoods.getGbDistributerFatherGoodsId());
                        for (GbDistributerGoodsEntity goods : goodsEntities) {
                            String nxGoodsName = goods.getGbDgGoodsName();
                            builder.append(nxGoodsName);
                            builder.append(',');
                        }
                        fatherGoods.setGbDgGoodsSubNames(builder.toString());
                        newList.add(fatherGoods);
                    }
                }
            }

            return R.ok().put("data", fatherGoodsEntities);
        } else {
            return R.error(-1, "没有商品");
        }
    }


    @RequestMapping(value = "/purchaserGetGoodsFather", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserGetGoodsFather(Integer disId, Integer toDepId, Integer type) {

        Map<String, Object> map = new HashMap<>();
        if (type != -1) {
            map.put("type", 3);
        }
        if (toDepId != -1) {
            map.put("toDepId", toDepId);
        }
        if (disId != -1) {
            map.put("disId", disId);
        }
        System.out.println(map);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDgService.queryDisFatherGoodsByParams(map);
        return R.ok().put("data", fatherGoodsEntities);
    }

//    @RequestMapping(value = "/getMendianFatherGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R getMendianFatherGoods(Integer disId) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("type", 3);
//        map.put("disId", disId);
//        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDgService.queryDisFatherGoodsByParams(map);
//        return R.ok().put("data", fatherGoodsEntities);
//    }


    @RequestMapping(value = "/getToDepartmentFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getToDepartmentFatherGoods(Integer toDepId, String controlString, Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", toDepId);
        map.put("disId", disId);
        if (controlString.equals("price")) {
            map.put("price", "1");
        }
        if (controlString.equals("fresh")) {
            map.put("fresh", "1");
        }

        if (controlString.equals("isNotSelf")) {
            map.put("isSelf", "0");
        }

        if (controlString.equals("isSelf")) {
            map.put("isSelf", "1");
        }

        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDgService.queryDisFatherGoodsByParams(map);

        return R.ok().put("data", fatherGoodsEntities);
    }


    @RequestMapping(value = "/getToDepartmentGoodsList", method = RequestMethod.POST)
    @ResponseBody
    public R getToDepartmentGoodsList(Integer toDepId, Integer fatherId, String goodsType,
                                      Integer limit, Integer page, Integer nxDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", toDepId);
        map.put("dgFatherId", fatherId);
        map.put("nxDisId", nxDisId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        if (goodsType.equals("price")) {
            map.put("price", 1);
        }
        if (goodsType.equals("fresh")) {
            map.put("fresh", 1);
        }
        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDgService.queryDisGoodsByParams(map);
        int total = gbDgService.queryDisGoodsCount(map);
        PageUtils pageUtil = new PageUtils(gbDistributerGoodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/getToDepartmentGoodsListWithShelf", method = RequestMethod.POST)
    @ResponseBody
    public R getToDepartmentGoodsListWithShelf(Integer toDepId, Integer fatherId, String goodsType,
                                               Integer limit, Integer page, String inventoryType, String controlString) {
        Map<String, Object> map = new HashMap<>();
        System.out.println("afdaf" + inventoryType);

        map.put("toDepId", toDepId);
        map.put("dgFatherId", fatherId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        if (!inventoryType.equals("-1")) {
            map.put("inventoryType", inventoryType);
        }
        if (goodsType.equals("jicai")) {
            map.put("type", getGbDisGoodsTypeJicai());
        }

        if (goodsType.equals("chuku")) {
            map.put("type", getGbDisGoodsTypeChuku());
        }
        if (goodsType.equals("zicai")) {
            map.put("type", getGbDisGoodsTypeZicai());
        }
        if (controlString.equals("price")) {
            map.put("price", "1");
        }
        if (controlString.equals("fresh")) {
            map.put("fresh", "1");
        }

        if (controlString.equals("isNotSelf")) {
            map.put("isSelf", "0");
        }

        if (controlString.equals("isSelf")) {
            map.put("isSelf", "1");
        }

        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDgService.queryDisShelfGoodsWithParams(map);
        int total = gbDgService.queryDisGoodsCount(map);
        PageUtils pageUtil = new PageUtils(gbDistributerGoodsEntities, total, limit, page);


        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/getMendianGoodsList", method = RequestMethod.POST)
    @ResponseBody
    public R getMendianGoodsList(Integer fatherId, String inventoryType,
                                 Integer limit, Integer page, Integer depFatherId) {
        Map<String, Object> map = new HashMap<>();
//        map.put("type", 3);
        map.put("disGoodsFatherId", fatherId);
        map.put("inventoryType", inventoryType);
        map.put("depFatherId", depFatherId);
        System.out.println("inenetytyyty==" + map);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        List<GbDepartmentDisGoodsEntity> gbDistributerGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
        int total = gbDgService.queryDisGoodsCount(map);
        PageUtils pageUtil = new PageUtils(gbDistributerGoodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


//    @RequestMapping(value = "/getDisStockOrders", method = RequestMethod.POST)
//    @ResponseBody
//    public R getDisStockOrders(String[] depIds, String[] fatherIds, Integer toDepId, Integer isWeight) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("depIds", depIds);
//        map.put("toDepId", toDepId);
//        map.put("isWeight", isWeight);
//        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDgService.queryDisGoodsByIds(map);
//        return R.ok().put("data", gbDistributerGoodsEntities);
//    }


    @RequestMapping(value = "/disUpdateDisGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R disUpdateDisGoodsGb(@RequestBody GbDistributerGoodsEntity gbGoods) {

        //old
        Integer gbDistributerGoodsId = gbGoods.getGbDistributerGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDgService.queryObject(gbDistributerGoodsId);
        Integer oldDepartmentId = gbDistributerGoodsEntity.getGbDgGbDepartmentId();

        Integer nowDepartmentId = gbGoods.getGbDgGbDepartmentId();
        GbDistributerGoodsEntity oldGoodsEntity = gbDgService.queryObject(gbDistributerGoodsId);
        Integer oldGoodsType = oldGoodsEntity.getGbDgGoodsType();
        Integer gbDgGoodsType = gbGoods.getGbDgGoodsType();
        String gbDgSellingPrice = oldGoodsEntity.getGbDgSellingPrice();
        String gbDgSellingPrice1 = gbGoods.getGbDgSellingPrice();
        String gbDgSelfPrice = oldGoodsEntity.getGbDgSelfPrice();
        String gbDgSelfPrice1 = gbGoods.getGbDgSelfPrice();
        Integer gbDgIsSelfControl = oldGoodsEntity.getGbDgIsSelfControl();
        Integer gbDgIsSelfControl1 = gbGoods.getGbDgIsSelfControl();

        // 修改商品采购方式
        if (!gbDgGoodsType.equals(oldGoodsType) || !oldDepartmentId.equals(nowDepartmentId)) {
            //查询是否有未完成订单
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", gbDistributerGoodsId);
            map.put("status", 4);
            List<GbDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map);
            if (ordersEntities.size() > 0) {
                return R.error(-1, "有未完成订单");
            }

            //查询是否有库存
            Map<String, Object> map1 = new HashMap<>();
            map1.put("stockDepId", oldDepartmentId);
            map1.put("disGoodsId", gbDistributerGoodsId);
            List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map1);
            if (stockEntities.size() > 0) {
                return R.error(-1, "有库存,不能改为非库存商品.");
            } else {
                //删除货架商品
                if (oldGoodsType.equals(getGbDisGoodsTypeChuku())) {
                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("stockDepId", oldGoodsEntity.getGbDgGbDepartmentId());
                    map2.put("disGoodsId", gbDistributerGoodsId);
                    List<GbDistributerGoodsShelfGoodsEntity> shelfGoodsEntities = gbDistributerGoodsShelfGoodsService.queryShelfGoodsByParams(map2);
                    System.out.println(shelfGoodsEntities.size() + "shelfgoods");
                    if (shelfGoodsEntities.size() > 0) {
                        for (GbDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsEntities) {
                            gbDistributerGoodsShelfGoodsService.delete(shelfGoods.getGbDistributerGoodsShelfGoodsId());
                        }
                    }
                }

            }
        }

//
//
//        if(!gbDgSellingPrice.equals(gbDgSellingPrice1) || !gbDgSelfPrice.equals(gbDgSelfPrice1)){
//            System.out.println("selfprir---" + gbDgSellingPrice1 + "newpr---" +gbDgSellingPrice);
//            System.out.println("selfprigbDgSelfPrice1r===" + gbDgSelfPrice1 + "newprgbDgSelfPrice===" +gbDgSelfPrice);
//            //查询是否有未完成订单
//            Map<String, Object> map = new HashMap<>();
//            map.put("disGoodsId", gbDistributerGoodsId);
//            map.put("status", 4);
//            List<GbDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map);
//            if (ordersEntities.size() > 0) {
//                return R.error(-1, "有未完成订单ddd");
//            }
//        }


        // 修改商品盘库方式
//        if (!gbDgGoodsInventoryType.equals(oldGoodsInventoryType)) {
//            // month盘库改其他盘库
//            if (oldGoodsInventoryType.equals(getDISGoodsInventroyMonth())) {
//                changeInventoryMonthData(gbGoods);
//            }
//            // week盘库改其他盘库
//            if (oldGoodsInventoryType.equals(getDISGoodsInventroyWeek())) {
//                changeInventoryWeekData(gbGoods);
//            }
//            // daily盘库改其他盘库
//            if (oldGoodsInventoryType.equals(getDISGoodsInventroyDaily())) {
//                changeInventoryDailyData(gbGoods);
//            }
//        }

        if (gbGoods.getGbDgGoodsType().equals(getGbDisGoodsTypeChuku()) || gbGoods.getGbDgGoodsType().equals(getGbDisGoodsTypeKitchen())) {
            //对比 old-ToDepId 和新 todepId 是否一样
            if (!gbDistributerGoodsEntity.getGbDgGbDepartmentId().equals(gbGoods.getGbDgGbDepartmentId())) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("depFatherId", gbGoods.getGbDgGbDepartmentId());
                map1.put("disGoodsId", gbGoods.getGbDistributerGoodsId());
                List<GbDepartmentDisGoodsEntity> newDepartmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map1);
                System.out.println("dkajsfkaslfjas;lfjsa" + newDepartmentDisGoodsEntities.size());
                if (newDepartmentDisGoodsEntities.size() == 0) {
                    //添加部门商品
                    GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
                    disGoodsEntity.setGbDdgDepGoodsName(gbGoods.getGbDgGoodsName());
                    disGoodsEntity.setGbDdgDisGoodsId(gbGoods.getGbDistributerGoodsId());
                    disGoodsEntity.setGbDdgDisGoodsFatherId(gbGoods.getGbDgDfgGoodsFatherId());
                    disGoodsEntity.setGbDdgDepGoodsPinyin(gbGoods.getGbDgGoodsPinyin());
                    disGoodsEntity.setGbDdgDepGoodsPy(gbGoods.getGbDgGoodsPy());
                    disGoodsEntity.setGbDdgDepGoodsStandardname(gbGoods.getGbDgGoodsStandardname());
                    disGoodsEntity.setGbDdgDepartmentId(gbGoods.getGbDgGbDepartmentId());
                    disGoodsEntity.setGbDdgDepartmentFatherId(gbGoods.getGbDgGbDepartmentId());
                    disGoodsEntity.setGbDdgGbDepartmentId(gbGoods.getGbDgGbDepartmentId());
                    disGoodsEntity.setGbDdgGbDisId(gbGoods.getGbDgDistributerId());
                    disGoodsEntity.setGbDdgGoodsType(gbGoods.getGbDgGoodsType());
                    disGoodsEntity.setGbDdgStockTotalWeight("0.0");
                    disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
                    disGoodsEntity.setGbDdgShowStandardId(-1);
                    disGoodsEntity.setGbDdgShowStandardName(gbGoods.getGbDgGoodsStandardname());
                    disGoodsEntity.setGbDdgShowStandardScale("-1");
                    disGoodsEntity.setGbDdgShowStandardWeight(null);
                    gbDepDisGoodsService.save(disGoodsEntity);

                }
            }
        }


        //gengxin
        //判断是否是nxDistributer商品
        Integer nxGoodsId = gbGoods.getGbDgNxGoodsId();
        System.out.println("nxgoidisisisi" + gbGoods.getGbDgNxGoodsId());
        if (nxGoodsId != -1) {
            System.out.println("-1--1-1-1--1--1-1-1-1--1-1--1-1-1-");
            Integer gbDgNxGoodsId = gbGoods.getGbDgNxGoodsId();
            Integer gbDgNxDistributerId = gbGoods.getGbDgNxDistributerId();
            Map<String, Object> map = new HashMap<>();
            map.put("disId", gbDgNxDistributerId);
            map.put("goodsId", gbDgNxGoodsId);
            List<NxDistributerGoodsEntity> distributerGoodsEntities1 = nxDistributerGoodsService.queryDisGoodsByParams(map);
            if (distributerGoodsEntities1.size() == 0) {
//                return R.error(-1, "供货商没有这个商品");
            } else {
                Integer distributerGoodsId = distributerGoodsEntities1.get(0).getNxDistributerGoodsId();
                gbGoods.setGbDgNxDistributerGoodsId(distributerGoodsId);

            }
        } else {

            gbGoods.setGbDgNxDistributerId(-1);
            gbGoods.setGbDgNxDistributerGoodsId(-1);
        }
        String goodsName = gbGoods.getGbDgGoodsName();
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        gbGoods.setGbDgGoodsPinyin(pinyin);
        gbGoods.setGbDgGoodsPy(headPinyin);

        gbDgService.update(gbGoods);

//		//修改供货商
//		if(disGoods.getGbDgGoodsType().equals(getGbDisGoodsTypeGonghuoshang())){
//			Map<String, Object> map = new HashMap<>();
//			map.put("nxDisGoodsId", disGoods.getGbDgNxDistributerGoodsId());
//			map.put("status", 6);
//			map.put("equalBuyStatus", 0);
//			System.out.println(map);
//			System.out.println("duoshaola");
//			List<GbDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map);
//			if(ordersEntities.size() > 0){
//				for (GbDepartmentOrdersEntity orders : ordersEntities) {
//					orders.setGbDoNxDistributerGoodsId(disGoods.getGbDistributerGoodsId());
//					orders.setGbDoNxDistributerId(disGoods.getGbDgDistributerId());
//					depOrdersService.update(orders);
//				}
//			}
//		}
//
//		//修改供货商
//		if(disGoods.getGbDgGoodsType() == 1 || disGoods.getGbDgGoodsType() == 4 ){
//			Map<String, Object> map = new HashMap<>();
//			map.put("nxDisId", disGoods.getGbDgNxDistributerId());
//			map.put("nxDisGoodsId", disGoods.getGbDgNxDistributerGoodsId());
//			map.put("status", 6);
//			map.put("equalBuyStatus", 0);
//			List<GbDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map);
//			if(ordersEntities.size() > 0){
//				for (GbDepartmentOrdersEntity orders : ordersEntities) {
//					orders.setGbDoNxDistributerGoodsId(-1);
//					orders.setGbDoNxDistributerId(-1);
//					depOrdersService.update(orders);
//				}
//			}
//		}

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", gbDistributerGoodsId);
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
        System.out.println("changedepdisgooodss" + departmentDisGoodsEntities.size());
        if (departmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity disGoodsEntity : departmentDisGoodsEntities) {
                disGoodsEntity.setGbDdgDepGoodsName(gbGoods.getGbDgGoodsName());
                disGoodsEntity.setGbDdgDepGoodsPinyin(gbGoods.getGbDgGoodsPinyin());
                disGoodsEntity.setGbDdgDepGoodsPy(gbGoods.getGbDgGoodsPy());
                disGoodsEntity.setGbDdgDepGoodsStandardname(gbGoods.getGbDgGoodsStandardname());
                disGoodsEntity.setGbDdgGbDepartmentId(gbGoods.getGbDgGbDepartmentId());
                disGoodsEntity.setGbDdgGbDisId(gbGoods.getGbDgDistributerId());
                disGoodsEntity.setGbDdgGoodsType(gbGoods.getGbDgGoodsType());
                disGoodsEntity.setGbDdgShowStandardName(gbGoods.getGbDgGoodsStandardname());
                gbDepDisGoodsService.update(disGoodsEntity);
            }
        }

        return R.ok();
    }


    /**
     * 批发商商品列表
     * Integer inventoryType,
     *
     * @param fatherId 父类id
     * @return 批发商商品列表
     */
    @RequestMapping(value = "/disGetDisTypeGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDisTypeGoodsListByFatherId(Integer fatherId, Integer inventoryType, String controlString,
                                              Integer limit, Integer page, String goodsType) {

        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("dgFatherId", fatherId);
        map.put("inventoryType", inventoryType);
        if (goodsType.equals("jicai")) {
            map.put("type", getGbDisGoodsTypeJicai());
        }
        if (goodsType.equals("kitchen")) {
            map.put("type", getGbDisGoodsTypeKitchen());
        }

        if (goodsType.equals("chuku")) {
            map.put("type", getGbDisGoodsTypeChuku());
        }
        if (goodsType.equals("zicai")) {
            map.put("type", getGbDisGoodsTypeZicai());
        }
        if (controlString.equals("price")) {
            map.put("price", "1");
        }
        if (controlString.equals("fresh")) {
            map.put("fresh", "1");
        }

        if (controlString.equals("isNotSelf")) {
            map.put("isSelf", "0");
            map.put("type", getGbDisGoodsTypeChuku());
        }

        if (controlString.equals("isSelf")) {
            map.put("isSelf", "1");
        }
        if (goodsType.equals("appSupplier")) {
            map.put("type", getGbDisGoodsTypeAppSupplier());
        }
        List<GbDistributerGoodsEntity> goodsEntities1;
        goodsEntities1 = gbDgService.queryDisGoodsByParams(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("fatherId", fatherId);
        map3.put("inventoryType", inventoryType);
        if (goodsType.equals("jicai")) {
            map3.put("type", getGbDisGoodsTypeJicai());
        }

        if (goodsType.equals("chuku")) {
            map3.put("type", getGbDisGoodsTypeChuku());
        }
        if (goodsType.equals("zicai")) {
            map3.put("type", getGbDisGoodsTypeZicai());
        }
        int total = gbDgService.queryGbGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);

        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/getDgCataGoodsWithSubNamesGb/{disId}")
    @ResponseBody
    public R getDgCataGoodsWithSubNamesGb(@PathVariable Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        List<GbDistributerFatherGoodsEntity> goodsEntities1 = dgfService.queryDisGoodsCataWithGoods(map);

        if (goodsEntities1.size() > 0) {
            List<GbDistributerFatherGoodsEntity> newList = new ArrayList<>();
            for (GbDistributerFatherGoodsEntity greatGrandGoods : goodsEntities1) {
                for (GbDistributerFatherGoodsEntity grandGoods : greatGrandGoods.getFatherGoodsEntities()) {
                    System.out.println("grandGoodsgrandGoodssororororor=========" + grandGoods.getGbDfgFatherGoodsSort());

                    for (GbDistributerFatherGoodsEntity fatherGoods : grandGoods.getFatherGoodsEntities()) {
                        System.out.println("fathsororororor=========" + fatherGoods.getGbDfgFatherGoodsSort());
                        StringBuilder builder = new StringBuilder();
                        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryDgSubNameByFatherIdGb(fatherGoods.getGbDistributerFatherGoodsId());
                        for (GbDistributerGoodsEntity goods : goodsEntities) {
                            String nxGoodsName = goods.getGbDgGoodsName();
                            builder.append(nxGoodsName);
                            builder.append(',');
                        }
                        fatherGoods.setGbDgGoodsSubNames(builder.toString());
                        newList.add(fatherGoods);
                    }
                }
            }

            return R.ok().put("data", goodsEntities1);
        } else {
            return R.error(-1, "没有商品");
        }
    }



    @RequestMapping(value = "/postDgnGoodsForNx", method = RequestMethod.POST)
    @ResponseBody
    public R postDgnGoodsForNx(Integer disGoodsId, Integer gbDisId, Integer depId) {

        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(disGoodsId);
        Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();

        //判断是否已经下载
        Map<String, Object> map7 = new HashMap<>();
        map7.put("disId", gbDisId);
        map7.put("goodsId", nxDgNxGoodsId);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDgService.queryDisGoodsByParams(map7);

        if (distributerGoodsEntities.size() > 0) {
            return R.error(-1, "已经下载");
        } else {
            GbDistributerGoodsEntity cgnGoods = new GbDistributerGoodsEntity();
            cgnGoods.setGbDgGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
            cgnGoods.setGbDgGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
            cgnGoods.setGbDgGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
            cgnGoods.setGbDgGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
            cgnGoods.setGbDgGoodsStandardWeight(nxDistributerGoodsEntity.getNxDgGoodsStandardWeight());
            cgnGoods.setGbDgGoodsDetail(nxDistributerGoodsEntity.getNxDgGoodsDetail());
            cgnGoods.setGbDgGoodsBrand(nxDistributerGoodsEntity.getNxDgGoodsBrand());
            cgnGoods.setGbDgGoodsPlace(nxDistributerGoodsEntity.getNxDgGoodsPlace());
            cgnGoods.setGbDgGoodsSort(nxDistributerGoodsEntity.getNxDgGoodsSort());

            cgnGoods.setGbDgDistributerId(gbDisId);
            cgnGoods.setGbDgGoodsStatus(0);
            cgnGoods.setGbDgGoodsIsWeight(0);
            cgnGoods.setGbDgNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
            cgnGoods.setGbDgNxFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
            cgnGoods.setGbDgNxGrandId(nxDistributerGoodsEntity.getNxDgNxGrandId());
            cgnGoods.setGbDgNxGreatGrandId(nxDistributerGoodsEntity.getNxDgNxGreatGrandId());
            cgnGoods.setGbDgPullOff(0);
            cgnGoods.setGbDgGoodsType(5);
            cgnGoods.setGbDgNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
            cgnGoods.setGbDgNxDistributerGoodsId(disGoodsId);
            cgnGoods.setGbDgGbDepartmentId(depId);
            cgnGoods.setGbDgControlFresh(0);
            cgnGoods.setGbDgControlPrice(0);
            cgnGoods.setGbDgGoodsInventoryType(1);
            cgnGoods.setGbDgIsFranchisePrice(0);
            cgnGoods.setGbDgIsSelfControl(0);
            GbDistributerGoodsEntity disGoods = saveDisGoodsForNx(cgnGoods);

            //添加部门商品
            GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
            disGoodsEntity.setGbDdgDepGoodsName(disGoods.getGbDgGoodsName());
            disGoodsEntity.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
            disGoodsEntity.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
            disGoodsEntity.setGbDdgDepGoodsPinyin(disGoods.getGbDgGoodsPinyin());
            disGoodsEntity.setGbDdgDepGoodsPy(disGoods.getGbDgGoodsPy());
            disGoodsEntity.setGbDdgDepGoodsStandardname(disGoods.getGbDgGoodsStandardname());
            disGoodsEntity.setGbDdgDepartmentId(disGoods.getGbDgGbDepartmentId());
            disGoodsEntity.setGbDdgDepartmentFatherId(disGoods.getGbDgGbDepartmentId());
            disGoodsEntity.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
            disGoodsEntity.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
            disGoodsEntity.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
            disGoodsEntity.setGbDdgStockTotalWeight("0.0");
            disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
            disGoodsEntity.setGbDdgShowStandardId(-1);
            disGoodsEntity.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
            disGoodsEntity.setGbDdgShowStandardScale("-1");
            disGoodsEntity.setGbDdgShowStandardWeight(null);
            disGoodsEntity.setGbDdgNxDistributerGoodsId(disGoodsId);
            gbDepDisGoodsService.save(disGoodsEntity);

            //2.2
            Integer nxCgGoodsId = disGoods.getGbDistributerGoodsId();

//            List<NxAliasEntity> aliasEntities = cgnGoods.getNxAliasEntities();
//            if (aliasEntities.size() > 0) {
//                for (NxAliasEntity aliasEntity : aliasEntities) {
//                    GbDistributerAliasEntity disAlias = new GbDistributerAliasEntity();
//                    disAlias.setGbDaDisGoodsId(nxCgGoodsId);
//                    disAlias.setGbDaAliasName(aliasEntity.getNxAliasName());
//                    disAliasService.save(disAlias);
//                }
//            }

            Integer gbDgNxDistributerGoodsId = cgnGoods.getGbDgNxDistributerGoodsId();
            List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByDisGoodsId(gbDgNxDistributerGoodsId);
            if(distributerStandardEntities.size() > 0){
                for(NxDistributerStandardEntity standardEntity: distributerStandardEntities){
                    GbDistributerStandardEntity distributerStandardEntity = new GbDistributerStandardEntity();
                    distributerStandardEntity.setGbDsDisGoodsId(nxCgGoodsId);
                    distributerStandardEntity.setGbDsStandardName(standardEntity.getNxDsStandardName());
                    dsService.save(distributerStandardEntity);
                }
            }


            //添加给门店
            //如果是餐饮商品，自动给门店添加部门商品
            if (disGoods.getGbDgGoodsType() < 20) {
                Map<String, Object> map = new HashMap<>();
                map.put("disId", disGoods.getGbDgDistributerId());
                map.put("type", getGbDepartmentTypeMendian());
                List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
                if (departmentEntities.size() > 0) {
                    for (GbDepartmentEntity dep : departmentEntities) {
                        GbDepartmentDisGoodsEntity disGoodsEntityDep = new GbDepartmentDisGoodsEntity();
                        disGoodsEntityDep.setGbDdgDepGoodsName(disGoods.getGbDgGoodsName());
                        disGoodsEntityDep.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
                        disGoodsEntityDep.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
                        disGoodsEntityDep.setGbDdgDepGoodsPinyin(disGoods.getGbDgGoodsPinyin());
                        disGoodsEntityDep.setGbDdgDepGoodsPy(disGoods.getGbDgGoodsPy());
                        disGoodsEntityDep.setGbDdgDepGoodsStandardname(disGoods.getGbDgGoodsStandardname());
                        disGoodsEntityDep.setGbDdgDepartmentId(dep.getGbDepartmentId());
                        disGoodsEntityDep.setGbDdgDepartmentFatherId(dep.getGbDepartmentId());
                        disGoodsEntityDep.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
                        disGoodsEntityDep.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
                        disGoodsEntityDep.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
                        disGoodsEntityDep.setGbDdgStockTotalWeight("0.0");
                        disGoodsEntityDep.setGbDdgStockTotalSubtotal("0.0");
                        disGoodsEntityDep.setGbDdgShowStandardId(-1);
                        disGoodsEntityDep.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
                        disGoodsEntityDep.setGbDdgShowStandardScale("-1");
                        disGoodsEntityDep.setGbDdgShowStandardWeight(null);
                        disGoodsEntityDep.setGbDdgNxDistributerGoodsId(disGoodsId);
                        gbDepDisGoodsService.save(disGoodsEntityDep);
                    }
                }
            }

            return R.ok().put("data", disGoods);
        }
    }


    //
//	/**
//	 * 添加批发商商品
//	 * @param cgnGoods 批发商商品
//	 * @return ok
//	 */
//    @RequestMapping(value = "/postDgnGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R postDgnGoods(@RequestBody GbDistributerGoodsEntity cgnGoods) {
//
//        Integer nxDgNxGoodsId = cgnGoods.getGbDgNxGoodsId();
//
//        //判断是否已经下载
//        Integer nxDgDistributerId1 = cgnGoods.getGbDgDistributerId();
//        Map<String, Object> map7 = new HashMap<>();
//        map7.put("disId", nxDgDistributerId1);
//        map7.put("goodsId", nxDgNxGoodsId);
//        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDgService.queryDisGoodsByParams(map7);
//
//        if (distributerGoodsEntities.size() > 0) {
//            return R.error(-1, "已经下载");
//        } else {
//            cgnGoods.setGbDgControlFresh(0);
//            cgnGoods.setGbDgControlPrice(0);
//            Integer gbDgNxDistributerId = cgnGoods.getGbDgNxDistributerId();
//            if (gbDgNxDistributerId != -1) {
//                Integer gbDgNxGoodsId = cgnGoods.getGbDgNxGoodsId();
//                Map<String, Object> map = new HashMap<>();
//                map.put("disId", gbDgNxDistributerId);
//                map.put("goodsId", gbDgNxGoodsId);
//                List<NxDistributerGoodsEntity> distributerGoodsEntities1 = nxDistributerGoodsService.queryDisGoodsByParams(map);
//                if (distributerGoodsEntities1.size() == 0) {
////                    return R.error(-1, "供货商没有这个商品");
//                } else {
//                    Integer distributerGoodsId = distributerGoodsEntities1.get(0).getNxDistributerGoodsId();
//
//                    cgnGoods.setGbDgNxDistributerGoodsId(distributerGoodsId);
//                }
//            }
//
//
//            GbDistributerGoodsEntity disGoods = saveDisGoods(cgnGoods);
//
//            //addStockDepDisGoods
//            Integer gbDgGoodsType = disGoods.getGbDgGoodsType();
//            System.out.println("goodstype" + gbDgGoodsType);
//            if (gbDgGoodsType.equals(getGbDisGoodsTypeChuku()) || gbDgGoodsType.equals(getGbDisGoodsTypeKitchen()) || gbDgGoodsType.equals(getGbDisGoodsTypeAppSupplier())) {
////添加部门商品
//                GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
//                disGoodsEntity.setGbDdgDepGoodsName(disGoods.getGbDgGoodsName());
//                disGoodsEntity.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
//                disGoodsEntity.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
//                disGoodsEntity.setGbDdgDepGoodsPinyin(disGoods.getGbDgGoodsPinyin());
//                disGoodsEntity.setGbDdgDepGoodsPy(disGoods.getGbDgGoodsPy());
//                disGoodsEntity.setGbDdgDepGoodsStandardname(disGoods.getGbDgGoodsStandardname());
//                disGoodsEntity.setGbDdgDepartmentId(disGoods.getGbDgGbDepartmentId());
//                disGoodsEntity.setGbDdgDepartmentFatherId(disGoods.getGbDgGbDepartmentId());
//                disGoodsEntity.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
//                disGoodsEntity.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
//                disGoodsEntity.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
//                disGoodsEntity.setGbDdgStockTotalWeight("0.0");
//                disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
//                disGoodsEntity.setGbDdgShowStandardId(-1);
//                disGoodsEntity.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
//                disGoodsEntity.setGbDdgShowStandardScale("-1");
//                disGoodsEntity.setGbDdgShowStandardWeight(null);
//                disGoodsEntity.setGbDdgNxDistributerGoodsId(nxDgNxGoodsId);
//                gbDepDisGoodsService.save(disGoodsEntity);
//            }
//
//            //2.2
//            Integer nxCgGoodsId = cgnGoods.getGbDistributerGoodsId();
//            List<NxAliasEntity> aliasEntities = cgnGoods.getNxAliasEntities();
//            if (aliasEntities.size() > 0) {
//                for (NxAliasEntity aliasEntity : aliasEntities) {
//                    GbDistributerAliasEntity disAlias = new GbDistributerAliasEntity();
//                    disAlias.setGbDaDisGoodsId(nxCgGoodsId);
//                    disAlias.setGbDaAliasName(aliasEntity.getNxAliasName());
//                    disAliasService.save(disAlias);
//                }
//            }
//
//            //添加给门店
//            //如果是餐饮商品，自动给门店添加部门商品
//            if (disGoods.getGbDgGoodsType() < 20) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("disId", disGoods.getGbDgDistributerId());
//                map.put("type", getGbDepartmentTypeMendian());
//                List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
//                if (departmentEntities.size() > 0) {
//                    for (GbDepartmentEntity dep : departmentEntities) {
//                        GbDepartmentDisGoodsEntity disGoodsEntityDep = new GbDepartmentDisGoodsEntity();
//                        disGoodsEntityDep.setGbDdgDepGoodsName(disGoods.getGbDgGoodsName());
//                        disGoodsEntityDep.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
//                        disGoodsEntityDep.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
//                        disGoodsEntityDep.setGbDdgDepGoodsPinyin(disGoods.getGbDgGoodsPinyin());
//                        disGoodsEntityDep.setGbDdgDepGoodsPy(disGoods.getGbDgGoodsPy());
//                        disGoodsEntityDep.setGbDdgDepGoodsStandardname(disGoods.getGbDgGoodsStandardname());
//                        disGoodsEntityDep.setGbDdgDepartmentId(dep.getGbDepartmentId());
//                        disGoodsEntityDep.setGbDdgDepartmentFatherId(dep.getGbDepartmentId());
//                        disGoodsEntityDep.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
//                        disGoodsEntityDep.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
//                        disGoodsEntityDep.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
//                        disGoodsEntityDep.setGbDdgStockTotalWeight("0.0");
//                        disGoodsEntityDep.setGbDdgStockTotalSubtotal("0.0");
//                        disGoodsEntityDep.setGbDdgShowStandardId(-1);
//                        disGoodsEntityDep.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
//                        disGoodsEntityDep.setGbDdgShowStandardScale("-1");
//                        disGoodsEntityDep.setGbDdgShowStandardWeight(null);
//                        gbDepDisGoodsService.save(disGoodsEntityDep);
//                    }
//                }
//            }
//
//            return R.ok().put("data", disGoods);
//        }
//    }


    private GbDistributerGoodsEntity saveDisGoodsForNx(GbDistributerGoodsEntity cgnGoods) {


        Integer gbDgDistributerId = cgnGoods.getGbDgDistributerId();
        Integer gbDgNxGrandId = cgnGoods.getGbDgNxGrandId(); //nxGrand 是gb的father 101

        //queryGrandFatherId
        NxGoodsEntity fatherEntity = nxGoodsService.queryObject(gbDgNxGrandId); //叶花菜
        Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
        NxGoodsEntity grandEntity = nxGoodsService.queryObject(grandFatherId);
        cgnGoods.setGbDgNxGrandName(grandEntity.getNxGoodsName());
        cgnGoods.setGbDgNxFatherName(fatherEntity.getNxGoodsName());

        Map<String, Object> map = new HashMap<>();
        map.put("color", "#187e6e");
        map.put("disId", gbDgDistributerId);;
        GbDistributerFatherGoodsEntity greatGrand =  dgfService.queryAppFatherGoods(map);
        cgnGoods.setGbDgNxGreatGrandId(greatGrand.getGbDistributerFatherGoodsId());
        cgnGoods.setGbDgNxGreatGrandName(greatGrand.getGbDfgFatherGoodsName());

        // 3， 查询父类
        Map<String, Object> map11 = new HashMap<>();
        map11.put("nxGoodsId", fatherEntity.getNxGoodsId());
        map11.put("disId", gbDgDistributerId);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities1 = dgfService.queryDisFathersGoodsByParamsGb(map11);

        if (fatherGoodsEntities1.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities1.get(0);
            Integer nxDfgGoodsAmount = fatherGoodsEntity.getGbDfgGoodsAmount();
            fatherGoodsEntity.setGbDfgGoodsAmount(nxDfgGoodsAmount + 1);
            dgfService.update(fatherGoodsEntity);

            //2，保存disId商品
            cgnGoods.setGbDgDfgGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
            //1 ，先保存disGoods
            gbDgService.save(cgnGoods);
            //
        } else {
            //添加fatherGoods的第一个级别 是nxGrand
            GbDistributerFatherGoodsEntity dgf = new GbDistributerFatherGoodsEntity();
            dgf.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
            dgf.setGbDfgFatherGoodsName(fatherEntity.getNxGoodsName());
            dgf.setGbDfgFatherGoodsLevel(2);
            dgf.setGbDfgGoodsAmount(1);
            dgf.setGbDfgPriceAmount(0);
            dgf.setGbDfgPriceTwoAmount(0);
            dgf.setGbDfgPriceThreeAmount(0);
            dgf.setGbDfgNxGoodsId(cgnGoods.getGbDgNxGrandId());
            dgf.setGbDfgFatherGoodsImg(fatherEntity.getNxGoodsFile());
            dgf.setGbDfgFatherGoodsSort(fatherEntity.getNxGoodsSort());
            dgfService.save(dgf);
            //更新disGoods的fatherGoodsId
            Integer distributerFatherGoodsId = dgf.getGbDistributerFatherGoodsId();
            cgnGoods.setGbDgDfgGoodsFatherId(distributerFatherGoodsId);
            gbDgService.save(cgnGoods);
            //继续查询是否有GrandFather
            String fatherName = cgnGoods.getGbDgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", gbDgDistributerId);
            map2.put("fathersFatherName", fatherName);
            map2.put("goodsLevel", 1);
            List<GbDistributerFatherGoodsEntity> grandGoodsFather = dgfService.queryHasDisFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = grandGoodsFather.get(0);
                dgf.setGbDfgFathersFatherId(gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId());
                dgf.setGbDfgGoodsAmount(gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount() + 1);
                dgfService.update(dgf);
            } else {
                //tianjiaGrand
                GbDistributerFatherGoodsEntity grand = new GbDistributerFatherGoodsEntity();
                grand.setGbDfgFatherGoodsName(grandEntity.getNxGoodsName());
                grand.setGbDfgDistributerId(gbDgDistributerId);
                grand.setGbDfgFatherGoodsLevel(1);
                grand.setGbDfgGoodsAmount(1);
                grand.setGbDfgFatherGoodsSort(grandEntity.getNxGoodsSort());
                grand.setGbDfgFatherGoodsColor(grandEntity.getColor());
                grand.setGbDfgFatherGoodsImg(grandEntity.getNxGoodsFile());
                grand.setGbDfgNxGoodsId(grandEntity.getNxGoodsId());
                Map<String, Object> mapApp = new HashMap<>();
                mapApp.put("color", "#187e6e");
                mapApp.put("disId", gbDgDistributerId);
                GbDistributerFatherGoodsEntity app = dgfService.queryAppFatherGoods(mapApp);
                grand.setGbDfgFathersFatherId(app.getGbDistributerFatherGoodsId());
                dgfService.save(grand);

                dgf.setGbDfgFathersFatherId(grand.getGbDistributerFatherGoodsId());
                dgfService.update(dgf);

            }
        }


        return cgnGoods;
    }

    private GbDistributerGoodsEntity saveDisGoods(GbDistributerGoodsEntity cgnGoods) {

        //queryGrandFatherId
        NxGoodsEntity fatherEntity = nxGoodsService.queryObject(cgnGoods.getGbDgNxFatherId());
        Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
        cgnGoods.setGbDgNxGrandId(grandFatherId);
        NxGoodsEntity grandEntity = nxGoodsService.queryObject(grandFatherId);
        cgnGoods.setGbDgNxGrandName(grandEntity.getNxGoodsName());

        //queryGreatGrandFatherId
        Integer greatGrandFatherId = grandEntity.getNxGoodsFatherId();
        cgnGoods.setGbDgNxGreatGrandId(greatGrandFatherId);
        cgnGoods.setGbDgNxGreatGrandName(nxGoodsService.queryObject(greatGrandFatherId).getNxGoodsName());

        Integer nxDgDistributerId = cgnGoods.getGbDgDistributerId();

        // 3， 查询父类
        Integer nxDgNxFatherId = cgnGoods.getGbDgNxFatherId();
        Map<String, Object> map11 = new HashMap<>();
        map11.put("nxGoodsId", nxDgNxFatherId);
        map11.put("disId", nxDgDistributerId);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities1 = dgfService.queryDisFathersGoodsByParamsGb(map11);

        if (fatherGoodsEntities1.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities1.get(0);
            Integer nxDfgGoodsAmount = fatherGoodsEntity.getGbDfgGoodsAmount();
            fatherGoodsEntity.setGbDfgGoodsAmount(nxDfgGoodsAmount + 1);
            dgfService.update(fatherGoodsEntity);

            //2，保存disId商品
            cgnGoods.setGbDgDfgGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
            //1 ，先保存disGoods

            gbDgService.save(cgnGoods);
            //
        } else {
            //添加fatherGoods的第一个级别
            GbDistributerFatherGoodsEntity dgf = new GbDistributerFatherGoodsEntity();
            dgf.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
            dgf.setGbDfgFatherGoodsName(cgnGoods.getGbDgNxFatherName());
            dgf.setGbDfgFatherGoodsLevel(2);
            dgf.setGbDfgGoodsAmount(1);
            dgf.setGbDfgPriceAmount(0);
            dgf.setGbDfgPriceTwoAmount(0);
            dgf.setGbDfgPriceThreeAmount(0);
            dgf.setGbDfgFatherGoodsColor(cgnGoods.getGbDgNxGoodsFatherColor());
            dgf.setGbDfgNxGoodsId(cgnGoods.getGbDgNxFatherId());
            dgf.setGbDfgFatherGoodsImg(cgnGoods.getGbDgNxFatherImg());
            dgf.setGbDfgFatherGoodsSort(cgnGoods.getGbDgGoodsSort());
            dgfService.save(dgf);
            //更新disGoods的fatherGoodsId
            Integer distributerFatherGoodsId = dgf.getGbDistributerFatherGoodsId();
            cgnGoods.setGbDgDfgGoodsFatherId(distributerFatherGoodsId);
            gbDgService.save(cgnGoods);
            //继续查询是否有GrandFather
            String grandName = cgnGoods.getGbDgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", nxDgDistributerId);
            map2.put("fathersFatherName", grandName);
            map2.put("goodsLevel", 1);
            List<GbDistributerFatherGoodsEntity> grandGoodsFather = dgfService.queryHasDisFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = grandGoodsFather.get(0);
                dgf.setGbDfgFathersFatherId(gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId());
                dgfService.update(dgf);
            } else {
                //tianjiaGrand
                GbDistributerFatherGoodsEntity grand = new GbDistributerFatherGoodsEntity();
                String nxCgGrandFatherName = cgnGoods.getGbDgNxGrandName();
                grand.setGbDfgFatherGoodsName(nxCgGrandFatherName);
                grand.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
                grand.setGbDfgFatherGoodsLevel(1);
                grand.setGbDfgFatherGoodsColor(cgnGoods.getGbDgNxGoodsFatherColor());
                grand.setGbDfgNxGoodsId(cgnGoods.getGbDgNxGrandId());
                grand.setGbDfgFatherGoodsSort(grand.getGbDfgFatherGoodsSort());
                dgfService.save(grand);
                dgf.setGbDfgFathersFatherId(grand.getGbDistributerFatherGoodsId());

                dgfService.update(dgf);


                //查询是否有greatGrand
                String greatGrandName = cgnGoods.getGbDgNxGreatGrandName();
                Map<String, Object> map3 = new HashMap<>();
                map3.put("disId", nxDgDistributerId);
                map3.put("fathersFatherName", greatGrandName);
                map3.put("goodsLevel", 0);
                List<GbDistributerFatherGoodsEntity> greatGrandGoodsFather = dgfService.queryHasDisFathersFather(map3);
                if (greatGrandGoodsFather.size() > 0) {
                    GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = greatGrandGoodsFather.get(0);
                    Integer disFatherId = gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId();
                    grand.setGbDfgFathersFatherId(disFatherId);
                    Map<String, Object> map = new HashMap<>();
                    map.put("fathersFatherId", disFatherId);
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map);
                    grand.setGbDfgFatherGoodsSort(grandGoodsEntities.size() + 1);
                    dgfService.update(grand);
                } else {
                    GbDistributerFatherGoodsEntity greatGrand = new GbDistributerFatherGoodsEntity();
                    String greatGrandName1 = cgnGoods.getGbDgNxGreatGrandName();
                    greatGrand.setGbDfgFatherGoodsName(greatGrandName1);
                    greatGrand.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
                    greatGrand.setGbDfgFatherGoodsLevel(0);
                    greatGrand.setGbDfgFathersFatherId(0);
                    greatGrand.setGbDfgFatherGoodsColor(cgnGoods.getGbDgNxGoodsFatherColor());
                    greatGrand.setGbDfgNxGoodsId(cgnGoods.getGbDgNxGreatGrandId());
                    Map<String, Object> mapGreatGrand = new HashMap<>();
                    mapGreatGrand.put("fathersFatherId", 0);
                    mapGreatGrand.put("disId", nxDgDistributerId);
                    System.out.println(mapGreatGrand);
                    System.out.println("mapgreagtgrandndnndd");
                    List<GbDistributerFatherGoodsEntity> greatGrandFatherGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(mapGreatGrand);
                    greatGrand.setGbDfgFatherGoodsSort(greatGrandFatherGoodsEntities.size() + 1);
                    dgfService.save(greatGrand);
                    grand.setGbDfgFathersFatherId(greatGrand.getGbDistributerFatherGoodsId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("fathersFatherId", greatGrand.getGbDistributerFatherGoodsId());
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map);
                    grand.setGbDfgFatherGoodsSort(grandGoodsEntities.size() + 1);
                    dgfService.update(grand);
                }
            }
        }


        return cgnGoods;
    }


    //
//
//
//
//	/**
//	 * 批发商商品列表
//	 * @param fatherId 父类id
//	 * @return 批发商商品列表
//	 */
    @RequestMapping(value = "/disGetDisGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDisGoodsListByFatherId(Integer fatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("dgFatherId", fatherId);
        map.put("disId", disId);
        List<GbDistributerGoodsEntity> goodsEntities1 = gbDgService.queryDisGoodsByParams(map);
        return R.ok().put("data", goodsEntities1);
    }

    //
//	/**
//	 * ibook获取含有批发商信息的商品列表
//	 * @param limit 每页商品数量
//	 * @param page 第几页
//	 * @param fatherId 商品父级id
//	 * @param disId 批发商id
//	 * @return ibook商品列表
//	 */
    @RequestMapping(value = "/disGetIbookGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetIbookGoods(Integer limit, Integer page, Integer fatherId, Integer disId) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("offset", (page - 1) * limit);
        map1.put("limit", limit);
        map1.put("fatherId", fatherId);
        List<NxGoodsEntity> nxGoodsEntities1 = nxGoodsService.queryNxGoodsByParams(map1);

        List<NxGoodsEntity> goodsEntities = new ArrayList<>();

        for (NxGoodsEntity goods : nxGoodsEntities1) {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", disId);
            map.put("goodsId", goods.getNxGoodsId());
            List<GbDistributerGoodsEntity> dgGoods = gbDgService.queryAddDistributerNxGoods(map);

            if (dgGoods.size() > 0) {
                goods.setIsDownload(1);
                goods.setGbDistributerGoodsEntity(dgGoods.get(0));
                goodsEntities.add(goods);
            } else {
                goods.setIsDownload(0);
                goodsEntities.add(goods);
            }
        }

        int total = nxGoodsService.queryTotalByFatherId(fatherId);
        PageUtils pageUtil = new PageUtils(goodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/depGetGbDisGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R depGetGbDisGoodsDetail(Integer disGoodsId, Integer depId) {

        //商品信息

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disGoodsId", disGoodsId);
        map4.put("depId", depId);
        GbDistributerGoodsEntity disGoods = gbDgService.queryDisGoodsWithDepDisGoods(map4);

        //3ri订单
        List<Map<String, Object>> orderList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disGoods.getGbDgDistributerId());
        map1.put("disGoodsId", disGoodsId);
        map1.put("orderType", disGoods.getGbDgGoodsType());
        map1.put("applyDate", formatWhatDay(0));
        map1.put("toDepId", depId);
        List<GbDepartmentOrdersEntity> departmentOrdersEntities = depOrdersService.queryOrdersForDisGoods(map1);
        Map<String, Object> mapone = new HashMap<>();
        mapone.put("date", formatWhatDayString(0));
        mapone.put("order", departmentOrdersEntities);
        orderList.add(mapone);

        map1.put("applyDate", formatWhatDay(-1));
        List<GbDepartmentOrdersEntity> departmentOrdersEntities2 = depOrdersService.queryOrdersForDisGoods(map1);
        Map<String, Object> maptwo = new HashMap<>();
        maptwo.put("date", formatWhatDayString(-1));
        maptwo.put("order", departmentOrdersEntities2);
        orderList.add(maptwo);

        map1.put("applyDate", formatWhatDay(-2));
        List<GbDepartmentOrdersEntity> departmentOrdersEntities3 = depOrdersService.queryOrdersForDisGoods(map1);
        Map<String, Object> mapthree = new HashMap<>();
        mapthree.put("date", formatWhatDayString(-2));
        mapthree.put("order", departmentOrdersEntities3);
        orderList.add(mapthree);


        //进货
        //进货
        Map<String, Object> map2 = new HashMap<>();
        map2.put("disGoodsId", disGoodsId);
        map2.put("startDate", formatWhatDay(-2));

        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDisPurchaseGoodsService.queryPurchaseGoodsByParams(map2);


        //客户
        Map<String, Object> map41 = new HashMap<>();
        map41.put("disGoodsId", disGoodsId);
        map41.put("depType", getGbDepartmentTypeMendian());
        System.out.println("41141" + map41);
        TreeSet<GbDepartmentEntity> departmentEntities = gbDepGoodsStockService.queryDepGoodsTreeDepartments(map41);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                double depDoutbleRest = 0;
                double depDoutbleRestV = 0;
                Map<String, Object> mapDepStock = new HashMap<>();
                mapDepStock.put("disGoodsId", disGoodsId);
                mapDepStock.put("depFatherId", department.getGbDepartmentId());
                Integer integer = gbDepGoodsStockService.queryGoodsStockCount(mapDepStock);
                if (integer > 0) {
                    depDoutbleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDepStock);
                    depDoutbleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(mapDepStock);
                }
                department.setDepRestGoodsTotalString(new BigDecimal(depDoutbleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepRestGoodsWeightTotalString(new BigDecimal(depDoutbleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("orderArr", orderList);
        map.put("purchaseArr", goodsEntities);
        map.put("goodsInfo", disGoods);
        map.put("depGoodArr", departmentEntities);
        return R.ok().put("data", map);
    }

    //
//	/**
//	 * 批发商商品详细
//	 * @param disGoodsId 批发商商品id
//	 * @return 含有客户的商品
//	 */
    @RequestMapping(value = "/gbDisGetGoodsDetail/{disGoodsId}")
    @ResponseBody
    public R gbDisGetGoodsDetail(@PathVariable Integer disGoodsId) {

        //商品信息
        GbDistributerGoodsEntity disGoods = gbDgService.queryGbDisGoodsDetail(disGoodsId);

        //3ri订单
        List<Map<String, Object>> orderList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disGoods.getGbDgDistributerId());
        map1.put("disGoodsId", disGoodsId);
        map1.put("orderType", disGoods.getGbDgGoodsType());
        map1.put("date", formatWhatDay(0));
        List<GbDepartmentOrdersEntity> departmentOrdersEntities = depOrdersService.queryOrdersForDisGoods(map1);
        Map<String, Object> mapone = new HashMap<>();
        mapone.put("date", formatWhatDayString(0));
        mapone.put("order", departmentOrdersEntities);
        orderList.add(mapone);

        map1.put("date", formatWhatDay(-1));
        List<GbDepartmentOrdersEntity> departmentOrdersEntities2 = depOrdersService.queryOrdersForDisGoods(map1);
        Map<String, Object> maptwo = new HashMap<>();
        maptwo.put("date", formatWhatDayString(-1));
        maptwo.put("order", departmentOrdersEntities2);
        orderList.add(maptwo);

        map1.put("date", formatWhatDay(-2));
        List<GbDepartmentOrdersEntity> departmentOrdersEntities3 = depOrdersService.queryOrdersForDisGoods(map1);
        Map<String, Object> mapthree = new HashMap<>();
        mapthree.put("date", formatWhatDayString(-2));
        mapthree.put("order", departmentOrdersEntities3);
        orderList.add(mapthree);


        //进货
        Map<String, Object> map2 = new HashMap<>();
        map2.put("disGoodsId", disGoodsId);
        map2.put("startDate", formatWhatDay(-2));
        map2.put("equalStatus", 3);
        System.out.println("purgooddd" + map2);
        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDisPurchaseGoodsService.queryPurchaseGoodsByParams(map2);

        //客户
        Map<String, Object> map41 = new HashMap<>();
        map41.put("disGoodsId", disGoodsId);
        map41.put("depType", getGbDepartmentTypeMendian());
        System.out.println("41141" + map41);
        TreeSet<GbDepartmentEntity> departmentEntities = gbDepGoodsStockService.queryDepGoodsTreeDepartments(map41);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                double depDoutbleRest = 0;
                double depDoutbleRestV = 0;
                Map<String, Object> mapDepStock = new HashMap<>();
                mapDepStock.put("disGoodsId", disGoodsId);
                mapDepStock.put("depFatherId", department.getGbDepartmentId());
                Integer integer = gbDepGoodsStockService.queryGoodsStockCount(mapDepStock);
                if (integer > 0) {
                    depDoutbleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDepStock);
                    depDoutbleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(mapDepStock);
                }
                department.setDepRestGoodsTotalString(new BigDecimal(depDoutbleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepRestGoodsWeightTotalString(new BigDecimal(depDoutbleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
        }

        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disGoodsId", disGoodsId);
        mapDep.put("depFatherId", disGoods.getGbDgGbDepartmentId());
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepDisGoodsService.queryDepGoodsItemByParams(mapDep);

        Map<String, Object> map = new HashMap<>();
        map.put("orderArr", orderList);
        map.put("purchaseArr", goodsEntities);
        map.put("goodsInfo", disGoods);
        map.put("depGoodArr", departmentEntities);
        System.out.println("depgppd" + departmentDisGoodsEntity);
        map.put("depGoods", departmentDisGoodsEntity);
        return R.ok().put("data", map);
    }

    /**
     * @param searchStr 搜索字符串
     * @param disId     批发商id
     * @return 搜索结果
     */
    @RequestMapping(value = "/queryDisDepGoodsByQuickSearchGb", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisDepGoodsByQuickSearchGb(String searchStr, String disId, String depId, Integer isSelf) {

        Map<String, Object> map = new HashMap<>();

        map.put("disId", disId);
        map.put("depId", depId);
        map.put("isSelf", isSelf);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }

        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryGbDisUnShlefGoodsQuickSearchStr(map);

        return R.ok().put("data", goodsEntities);
    }

    @RequestMapping(value = "/queryIsSelfGoodsByQuickSearchGb", method = RequestMethod.POST)
    @ResponseBody
    public R queryIsSelfGoodsByQuickSearchGb(String searchStr, String disId, String depId, Integer isSelf) {

        Map<String, Object> map = new HashMap<>();

        map.put("disId", disId);
        map.put("depId", depId);
        map.put("isSelf", isSelf);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }

        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryGbDisUnShlefGoodsQuickSearchStr(map);

        return R.ok().put("data", goodsEntities);
    }

    /**
     * @param searchStr 搜索字符串
     * @param disId     批发商id
     * @return 搜索结果
     */
    @RequestMapping(value = "/queryDisGoodsByQuickSearchGb", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisGoodsByQuickSearchGb(String searchStr, String disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }
        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryGbDisGoodsQuickSearchStr(map);
        return R.ok().put("data", goodsEntities);
    }

    @RequestMapping(value = "/queryNxAppGoodsQuickSearchWithDepOrder", method = RequestMethod.POST)
    @ResponseBody
    public R queryNxAppGoodsQuickSearchWithDepOrder(String searchStr, Integer nxDisId, Integer gbDisId,Integer depId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", gbDisId);
        map.put("nxDisId", nxDisId);
        map.put("depId", depId);

        Map<String, Object> mapNx = new HashMap<>();
        mapNx.put("disId", nxDisId);
        map.put("searchStr", searchStr);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
                mapNx.put("searchStr", searchStr);
                mapNx.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
                mapNx.put("searchPinyin", searchStr);
            }
        }
        System.out.println("depserardkddkdkdk" + map);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDgService.queryDisGoodsQuickSearchStrWithDepOrdersGb(map);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("disArr", distributerGoodsEntities);
        if (distributerGoodsEntities.size() > 0) {
            if (distributerGoodsEntities.size() < 10) {
                List<NxDistributerGoodsEntity> nxDisGoodsEntities = nxDistributerGoodsService.queryDisGoodsQuickSearchStr(mapNx);
                if (nxDisGoodsEntities.size() > 0) {
                    for (GbDistributerGoodsEntity disGoods : distributerGoodsEntities) {
                        Integer nxDgNxGoodsId = disGoods.getGbDgNxDistributerGoodsId();
                        for (int i = 0; i < nxDisGoodsEntities.size(); i++) {
                            Integer nxGoodsId = nxDisGoodsEntities.get(i).getNxDistributerGoodsId();
                            if (nxDgNxGoodsId.equals(nxGoodsId)) {
                                nxDisGoodsEntities.remove(i);
                            }
                        }
                    }
                    map3.put("nxDisArr", nxDisGoodsEntities);
                } else {
                    map3.put("nxDisArr", new ArrayList<>());
                }
            }
        } else {
            map3.put("disArr", new ArrayList<>());
            List<NxDistributerGoodsEntity> nxDisGoodsEntities = nxDistributerGoodsService.queryDisGoodsQuickSearchStr(mapNx);
            if (nxDisGoodsEntities.size() > 0) {
                map3.put("nxDisArr", nxDisGoodsEntities);
            } else {
                map3.put("nxDisArr", new ArrayList<>());
            }

        }

        return R.ok().put("data", map3);
    }

    @RequestMapping(value = "/queryNxAppGoodsQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryNxAppGoodsQuickSearch(String searchStr, Integer nxDisId, Integer gbDisId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", gbDisId);
        map.put("nxDisId", nxDisId);

        Map<String, Object> mapNx = new HashMap<>();
        mapNx.put("disId", nxDisId);
        map.put("searchStr", searchStr);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
                mapNx.put("searchStr", searchStr);
                mapNx.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
                mapNx.put("searchPinyin", searchStr);
            }
        }
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDgService.queryGbDisGoodsQuickSearchStr(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disArr", distributerGoodsEntities);


        if (distributerGoodsEntities.size() > 0) {
            if (distributerGoodsEntities.size() < 10) {
                List<NxDistributerGoodsEntity> nxDisGoodsEntities = nxDistributerGoodsService.queryDisGoodsQuickSearchStr(mapNx);
                if (nxDisGoodsEntities.size() > 0) {
                    for (GbDistributerGoodsEntity disGoods : distributerGoodsEntities) {
                        Integer nxDgNxGoodsId = disGoods.getGbDgNxDistributerGoodsId();
                        for (int i = 0; i < nxDisGoodsEntities.size(); i++) {
                            Integer nxGoodsId = nxDisGoodsEntities.get(i).getNxDistributerGoodsId();
                            if (nxDgNxGoodsId.equals(nxGoodsId)) {
                                nxDisGoodsEntities.remove(i);
                            }
                        }
                    }
                    map3.put("nxDisArr", nxDisGoodsEntities);
                } else {
                    map3.put("nxDisArr", new ArrayList<>());
                }
            }
        } else {
            map3.put("disArr", new ArrayList<>());
            List<NxDistributerGoodsEntity> nxDisGoodsEntities = nxDistributerGoodsService.queryDisGoodsQuickSearchStr(mapNx);
            if (nxDisGoodsEntities.size() > 0) {
                map3.put("nxDisArr", nxDisGoodsEntities);
            } else {
                map3.put("nxDisArr", new ArrayList<>());
            }

        }

        return R.ok().put("data", map3);
    }

    /**
     * @param searchStr 搜索字符串
     * @param disId     批发商id
     * @return 搜索结果
     */
    @RequestMapping(value = "/queryDisFatherGoodsByQuickSearchGb", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisFatherGoodsByQuickSearchGb(String searchStr, String disId, Integer fatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("fatherId", fatherId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }
        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryGbDisGoodsQuickSearchStr(map);
        return R.ok().put("data", goodsEntities);
    }

//    @RequestMapping(value = "/queryStockGoodsByQuickSearchGb", method = RequestMethod.POST)
//    @ResponseBody
//    public R queryStockGoodsByQuickSearchGb(String searchStr, String disId, String depId) {
//
//        Map<String, Object> map = new HashMap<>();
//        Map<String, Object> map1 = new HashMap<>();
//        map.put("disId", disId);
//        map.put("toDepId", depId);
//        for (int i = 0; i < searchStr.length(); i++) {
//            String str = searchStr.substring(i, i + 1);
//            if (str.matches("[\u4E00-\u9FFF]")) {
//                String pinyin = hanziToPinyin(searchStr);
//                map.put("searchStr", searchStr);
//                map.put("searchPinyin", pinyin);
//            } else {
//                map.put("searchPinyin", searchStr);
//            }
//        }
//        System.out.println("mapappaa" + map);
//
////        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryDisGoodsQuickSearchStrWithDepOrdersGb(map);
//        TreeSet<GbDepartmentDisGoodsEntity> goodsEntities = gbDepDisGoodsService.queryDepDisGoodsQuickSearchStrGb(map);
//
//        return R.ok().put("data", goodsEntities);
//    }

    @RequestMapping(value = "/queryDepartmentGoodsByQuickSearchGb", method = RequestMethod.POST)
    @ResponseBody
    public R queryDepartmentGoodsByQuickSearchGb(String searchStr, String isSelf, String depId) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depId", depId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map1.put("searchStr", searchStr);
                map1.put("searchPinyin", pinyin);
            } else {
                map1.put("searchPinyin", searchStr);
            }
        }

        System.out.println("mpa11111111" + map1);

        TreeSet<GbDepartmentDisGoodsEntity> disGoodsEntityTreeSet = gbDepDisGoodsService.queryDepDisGoodsQuickSearchStrGb(map1);


        return R.ok().put("data", disGoodsEntityTreeSet);
    }


    @RequestMapping(value = "/queryDepartmentIsSelfGoodsByQuickSearchGb", method = RequestMethod.POST)
    @ResponseBody
    public R queryDepartmentIsSelfGoodsByQuickSearchGb(String searchStr, Integer isSelf, String depId) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depId", depId);
        map1.put("isSelf", isSelf);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map1.put("searchStr", searchStr);
                map1.put("searchPinyin", pinyin);
            } else {
                map1.put("searchPinyin", searchStr);
            }
        }

        TreeSet<GbDepartmentDisGoodsEntity> disGoodsEntityTreeSet = gbDepDisGoodsService.queryDepDisGoodsQuickSearchStrGb(map1);

        return R.ok().put("data", disGoodsEntityTreeSet);
    }

    @RequestMapping(value = "/getStockDepartmentGoodsPage")
    @ResponseBody
    public R getStockDepartmentGoodsPage(Integer depId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("toDepId", depId);
        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDgService.queryDisGoodsByParams(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("toDepId", depId);
        int total = gbDgService.queryGbStockGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(gbDistributerGoodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/getGbGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R getGbGoodsListByFatherId(Integer fatherId, Integer limit, Integer page) {

        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("gbFatherId", fatherId);
        List<GbDistributerGoodsEntity> goodsEntities1 = gbDgService.queryGoodsByParamsGb(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("fatherId", fatherId);
        int total = gbDgService.queryGbGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/changeDisGoodsFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R changeDisGoodsFatherId(Integer disGoodsId, Integer fatherId) {

        //old
        GbDistributerGoodsEntity goodsEntity = gbDgService.queryObject(disGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);

        //depDisGoods
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
        if (departmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity depDisGoods : departmentDisGoodsEntities) {
                depDisGoods.setGbDdgDisGoodsFatherId(fatherId);
                gbDepDisGoodsService.update(depDisGoods);
            }
        }

        //depStock
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);
        if (stockEntities.size() > 0) {
            for (GbDepartmentGoodsStockEntity stockEntity : stockEntities) {
                stockEntity.setGbDgsGbDisGoodsFatherId(fatherId);
                gbDepGoodsStockService.update(stockEntity);
            }
        }

        //depDaily
        List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListByParams(map);
        if (departmentGoodsDailyEntities.size() > 0) {
            for (GbDepartmentGoodsDailyEntity dailyEntity : departmentGoodsDailyEntities) {
                dailyEntity.setGbDgdGbDisGoodsFatherId(fatherId);
                gbDepGoodsDailyService.update(dailyEntity);
            }
        }
        //depReduce
        List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepGoodsStockReduceService.queryStockReduceListByParams(map);
        if (reduceEntities.size() > 0) {
            for (GbDepartmentGoodsStockReduceEntity reduceEntity : reduceEntities) {
                reduceEntity.setGbDgsrGbDisGoodsFatherId(fatherId);
                gbDepGoodsStockReduceService.update(reduceEntity);
            }
        }


        Integer oldfgGoodsFatherId1 = goodsEntity.getGbDgDfgGoodsFatherId();
        GbDistributerFatherGoodsEntity fatherGoodsEntity = dgfService.queryObject(oldfgGoodsFatherId1);
        fatherGoodsEntity.setGbDfgGoodsAmount(fatherGoodsEntity.getGbDfgGoodsAmount() - 1);
        dgfService.update(fatherGoodsEntity);
        if(fatherGoodsEntity.getGbDfgGoodsAmount() == 0){
            Integer grandId = fatherGoodsEntity.getGbDfgFathersFatherId();
            GbDistributerFatherGoodsEntity grandFather = dgfService.queryObject(grandId);
            if(grandFather.getGbDfgGoodsAmount() == 0){
                Integer greatGrandId = grandFather.getGbDfgFathersFatherId();
                GbDistributerFatherGoodsEntity greatGrandFather = dgfService.queryObject(greatGrandId);
                if(greatGrandFather.getGbDfgGoodsAmount() == 0){
                    dgfService.delete(greatGrandId);
                }
                dgfService.delete(grandId);
            }
            dgfService.delete(oldfgGoodsFatherId1);
        }



        //new
        GbDistributerFatherGoodsEntity fatherGoodsEntity1 = dgfService.queryObject(fatherId);
        fatherGoodsEntity1.setGbDfgGoodsAmount(fatherGoodsEntity1.getGbDfgGoodsAmount() + 1);
        dgfService.update(fatherGoodsEntity1);

        goodsEntity.setGbDgDfgGoodsFatherId(fatherId);
        gbDgService.update(goodsEntity);

        return R.ok();
    }


    @RequestMapping(value = "/changeGbGoodsFresh", method = RequestMethod.POST)
    @ResponseBody
    public R changeGbGoodsFresh(@RequestBody GbDistributerGoodsEntity gbGoods) {
        Integer gbDistributerGoodsId = gbGoods.getGbDistributerGoodsId();

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", gbDistributerGoodsId);
        map.put("date", formatWhatDay(0));
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);

        if (integer > 0) {
            List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListByParams(map);
            for (GbDepartmentGoodsDailyEntity dailyEntity : departmentGoodsDailyEntities) {
                BigDecimal lastWeight = new BigDecimal(dailyEntity.getGbDgdLastWeight());
                BigDecimal todayWeight = new BigDecimal(dailyEntity.getGbDgdWeight());
                BigDecimal produceWeight = new BigDecimal(dailyEntity.getGbDgdProduceWeight());
                if (lastWeight.compareTo(BigDecimal.ZERO) == 0) {//没有剩余都是新鲜的
                    dailyEntity.setGbDgdFreshRate("100");
                } else {
                    if (todayWeight.compareTo(BigDecimal.ZERO) == 0) {//没有进货都是旧的
                        dailyEntity.setGbDgdFreshRate("0");
                    } else {
                        //有进新货，对比produce
                        //1, 没有销售
                        if (produceWeight.compareTo(BigDecimal.ZERO) == 0) {
                            dailyEntity.setGbDgdFreshRate("0");
                        } else {
                            //如果销售大于剩余量
                            if (produceWeight.compareTo(lastWeight) == 1) {
                                //销售有新货数量 12 有1斤是新的 8.33
                                BigDecimal subtract = produceWeight.subtract(lastWeight);
                                BigDecimal decimal = subtract.divide(produceWeight, 4, BigDecimal.ROUND_HALF_UP);
                                BigDecimal decimal1 = decimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                dailyEntity.setGbDgdFreshRate(decimal1.toString());
                            } else {
                                //销售的数量是剩余数量
                                dailyEntity.setGbDgdFreshRate("0");
                            }
                        }
                    }
                }
                gbDepGoodsDailyService.update(dailyEntity);
            }
        }

        gbDgService.update(gbGoods);

        return R.ok();
    }

    @RequestMapping(value = "/updateGbGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updateGbGoods(@RequestBody GbDistributerGoodsEntity gbGoods) {
        //修改部门商品
        String goodsName = gbGoods.getGbDgGoodsName();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", gbGoods.getGbDgDistributerId());
        map.put("goodsName", goodsName);
        map.put("standardName", gbGoods.getGbDgGoodsStandardname());
        map.put("detail", gbGoods.getGbDgGoodsDetail());
        map.put("brand", gbGoods.getGbDgGoodsBrand());
        map.put("place", gbGoods.getGbDgGoodsPlace());
        map.put("notEqualDisGoodsId", gbGoods.getGbDistributerGoodsId());
        System.out.println("mappa" + map);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDgService.queryUpdateGoodsByParams(map);
        if (distributerGoodsEntities.size() > 1) {
            return R.error(-1, "存在相同商品");
        } else {
            String pinyin = hanziToPinyin(goodsName);
            String headPinyin = getHeadStringByString(goodsName, false, null);
            gbGoods.setGbDgGoodsPinyin(pinyin);
            gbGoods.setGbDgGoodsPy(headPinyin);
            gbDgService.update(gbGoods);
            return R.ok().put("data", gbGoods);
        }
    }


    @RequestMapping(value = "/saveGbGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveGbGoods(@RequestBody GbDistributerGoodsEntity goods) {

        Integer gbDgNxDistributerId = goods.getGbDgNxDistributerId();
        if (gbDgNxDistributerId != -1) {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", gbDgNxDistributerId);
            map.put("goodsName", goods.getGbDgGoodsName());
            map.put("goodsStandard", goods.getGbDgGoodsStandardname());
            List<NxDistributerGoodsEntity> distributerGoodsEntities = nxDistributerGoodsService.queryIfHasSameDisGoods(map);
            if (distributerGoodsEntities.size() == 0) {

//                return R.error(-1, "供货商没有这个商品");
            } else {
                Integer distributerGoodsId = distributerGoodsEntities.get(0).getNxDistributerGoodsId();
                goods.setGbDgNxDistributerGoodsId(distributerGoodsId);
            }
        } else {
            goods.setGbDgNxDistributerId(-1);
            goods.setGbDgNxDistributerGoodsId(-1);
        }

        String goodsName = goods.getGbDgGoodsName();
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        goods.setGbDgGoodsPinyin(pinyin);
        goods.setGbDgGoodsPy(headPinyin);
        goods.setGbDgControlFresh(0);
        goods.setGbDgControlPrice(0);


        gbDgService.save(goods);


        //addDepDisGoods
        //添加部门商品
        GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
        disGoodsEntity.setGbDdgDepGoodsName(goods.getGbDgGoodsName());
        disGoodsEntity.setGbDdgDisGoodsId(goods.getGbDistributerGoodsId());
        disGoodsEntity.setGbDdgDisGoodsFatherId(goods.getGbDgDfgGoodsFatherId());
        disGoodsEntity.setGbDdgDepGoodsPinyin(goods.getGbDgGoodsPinyin());
        disGoodsEntity.setGbDdgDepGoodsPy(goods.getGbDgGoodsPy());
        disGoodsEntity.setGbDdgDepGoodsStandardname(goods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgDepartmentId(goods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgDepartmentFatherId(goods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDepartmentId(goods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDisId(goods.getGbDgDistributerId());
        disGoodsEntity.setGbDdgGoodsType(goods.getGbDgGoodsType());
        disGoodsEntity.setGbDdgStockTotalWeight("0.0");
        disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        disGoodsEntity.setGbDdgShowStandardId(-1);
        disGoodsEntity.setGbDdgShowStandardName(goods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgShowStandardScale("-1");
        disGoodsEntity.setGbDdgShowStandardWeight(null);
        gbDepDisGoodsService.save(disGoodsEntity);

        //如果是餐饮商品，自动给门店添加部门商品
        Integer gbDgDistributerId = goods.getGbDgDistributerId();
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDgDistributerId);

        if (gbDistributerEntity.getGbDistributerBusinessType() < 20) {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", goods.getGbDgDistributerId());
            map.put("type", getGbDepartmentTypeMendian());
            List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
            if (departmentEntities.size() > 0) {
                for (GbDepartmentEntity dep : departmentEntities) {
                    GbDepartmentDisGoodsEntity disGoodsEntityDep = new GbDepartmentDisGoodsEntity();
                    disGoodsEntityDep.setGbDdgDepGoodsName(goods.getGbDgGoodsName());
                    disGoodsEntityDep.setGbDdgDisGoodsId(goods.getGbDistributerGoodsId());
                    disGoodsEntityDep.setGbDdgDisGoodsFatherId(goods.getGbDgDfgGoodsFatherId());
                    disGoodsEntityDep.setGbDdgDepGoodsPinyin(goods.getGbDgGoodsPinyin());
                    disGoodsEntityDep.setGbDdgDepGoodsPy(goods.getGbDgGoodsPy());
                    disGoodsEntityDep.setGbDdgDepGoodsStandardname(goods.getGbDgGoodsStandardname());
                    disGoodsEntityDep.setGbDdgDepartmentId(dep.getGbDepartmentId());
                    disGoodsEntityDep.setGbDdgDepartmentFatherId(dep.getGbDepartmentId());
                    disGoodsEntityDep.setGbDdgGbDepartmentId(goods.getGbDgGbDepartmentId());
                    disGoodsEntity.setGbDdgGbDisId(goods.getGbDgDistributerId());
                    disGoodsEntityDep.setGbDdgGoodsType(goods.getGbDgGoodsType());
                    disGoodsEntityDep.setGbDdgStockTotalWeight("0.0");
                    disGoodsEntityDep.setGbDdgStockTotalSubtotal("0.0");
                    disGoodsEntityDep.setGbDdgShowStandardId(-1);
                    disGoodsEntityDep.setGbDdgShowStandardName(goods.getGbDgGoodsStandardname());
                    disGoodsEntityDep.setGbDdgShowStandardScale("-1");
                    disGoodsEntityDep.setGbDdgGbDisId(goods.getGbDgDistributerId());
                    gbDepDisGoodsService.save(disGoodsEntityDep);
                }
            }
        }

        Integer nxDgDfgGoodsFatherId1 = goods.getGbDgDfgGoodsFatherId();

        GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = dgfService.queryObject(nxDgDfgGoodsFatherId1);
        Integer gbDfgGoodsAmount = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
        gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount + 1);
        dgfService.update(gbDistributerFatherGoodsEntity);
        return R.ok().put("data", goods);
    }

    @RequestMapping(value = "/deleteGbGoods/{goodsId}")
    @ResponseBody
    public R deleteGbGoods(@PathVariable Integer goodsId) {
        gbDgService.delete(goodsId);
        return R.ok();
    }

    @RequestMapping(value = "/getGoodsSubNamesByFatherIdGb/{fatherId}")
    @ResponseBody
    public R getGoodsSubNamesByFatherIdGb(@PathVariable Integer fatherId) {


        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.querySubFatherGoods(fatherId);

        List<GbDistributerFatherGoodsEntity> newList = new ArrayList<>();

        for (GbDistributerFatherGoodsEntity fatherGoods : fatherGoodsEntities) {
            StringBuilder builder = new StringBuilder();

            List<GbDistributerGoodsEntity> goodsEntities = gbDgService.querySubNameByFatherId(fatherGoods.getGbDistributerFatherGoodsId());
            for (GbDistributerGoodsEntity goods : goodsEntities) {
                String nxGoodsName = goods.getGbDgGoodsName();
                builder.append(nxGoodsName);
                builder.append(',');
            }
            fatherGoods.setGbDgGoodsSubNames(builder.toString());
            newList.add(fatherGoods);
        }

        return R.ok().put("data", newList);
    }

    @RequestMapping(value = "/canclePostDgnGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R canclePostDgnGoodsGb(Integer disGoodsId, Integer disGoodsFatherId, Integer disId) {
        Map<String, Object> map5 = new HashMap<>();
        map5.put("disGoodsId", disGoodsId);
        Integer orderAmount = depOrdersService.queryGbDepartmentOrderAmount(map5);

        Integer stockCount = gbDepGoodsStockService.queryGoodsStockCount(map5);

        Map<String, Object> mapDisGoods = new HashMap<>();
        mapDisGoods.put("disGoodsId", disGoodsId);
        mapDisGoods.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(mapDisGoods);

        if (orderAmount > 0 || stockCount > 0 || departmentDisGoodsEntities.size() > 0) {
            return R.error(-1, "此商品在使用中");
        } else {

            GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = dgfService.queryObject(disGoodsFatherId);
            Integer gbDfgGoodsAmount = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
            gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount - 1);
            dgfService.update(gbDistributerFatherGoodsEntity);

            Map<String, Object> map1 = new HashMap<>();
            map1.put("disId", disId);
            map1.put("dgFatherId", disGoodsFatherId);
            //搜索fatherId下有几个disGoods
            List<GbDistributerGoodsEntity> totalDisGoods = gbDgService.queryDisGoodsByParams(map1);
            //如果disGoods的父类只有一个商品
            if (totalDisGoods.size() == 1) {
                //父类Entity
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity0 = dgfService.queryObject(disGoodsFatherId);
                //disGoods的grandId
                Integer grandId = gbDistributerFatherGoodsEntity0.getGbDfgFathersFatherId();
                Map<String, Object> mapGrand = new HashMap<>();
                mapGrand.put("fathersFatherId", grandId);
                //搜索grand有几个兄弟
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(mapGrand);
                if (fatherGoodsEntities.size() == 1) {
                    Integer gbDfgFathersFatherId = fatherGoodsEntities.get(0).getGbDfgFathersFatherId();
                    GbDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(gbDfgFathersFatherId);
                    Integer greatGrandId = grandEntity.getGbDfgFathersFatherId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("fathersFatherId", greatGrandId);
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map);

                    //如果grandFather也是只有一个，则删除greatGrandFather
                    if (grandGoodsEntities.size() == 1) {
                        dgfService.delete(greatGrandId);
                    }
                    dgfService.delete(grandId);
                }
                dgfService.delete(disGoodsFatherId);
            } else {
                //父类商品数量减去1
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity1 = dgfService.queryObject(disGoodsFatherId);
                Integer gbDfgGoodsAmount1 = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
                gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount1 - 1);
                dgfService.update(gbDistributerFatherGoodsEntity1);
            }

            //删除订货单位
            List<GbDistributerStandardEntity> standardEntities = dsService.queryDisStandardByDisGoodsIdGb(disGoodsId);
            if (standardEntities.size() > 0) {
                for (GbDistributerStandardEntity disStandard : standardEntities) {
                    dsService.delete(disStandard.getGbDistributerStandardId());
                }
            }

            int i = gbDgService.delete(disGoodsId);

            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", disGoodsId);
            List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities1 = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
            if(departmentDisGoodsEntities1.size() > 0){
                for(GbDepartmentDisGoodsEntity disGoodsEntity: departmentDisGoodsEntities1){
                    gbDepDisGoodsService.delete(disGoodsEntity.getGbDepartmentDisGoodsId());
                }
            }

            if (i == 1) {
                return R.ok();
            } else {
                return R.error(-1, "删除失败");
            }

        }

    }


    //
    @RequestMapping("/downloadExcelGbDep")
    @ResponseBody
    public void downloadExcelGbDep(HttpServletResponse response, HttpServletRequest request) {
        String depId = request.getParameter("depId");
        String fatherName = request.getParameter("depName");
        System.out.println("deelleleleleelleel" + depId);
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            BigDecimal total = new BigDecimal(0);
            Map<String, Object> map1 = new HashMap<>();
            map1.put("toDepId", depId);
            System.out.println(map1);
            List<GbDistributerFatherGoodsEntity> greatGrandEntities = gbDgService.queryDisFatherGoodsByParams(map1);
            for (GbDistributerFatherGoodsEntity great : greatGrandEntities) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("fathersFatherId", great.getGbDistributerFatherGoodsId() );
//                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map);

                for (GbDistributerFatherGoodsEntity grand : great.getFatherGoodsEntities()) {

//                    Map<String, Object> map2 = new HashMap<>();
//                    map2.put("fathersFatherId", grand.getGbDistributerFatherGoodsId() );
//                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map2);

//                    for (GbDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
//                    TreeSet<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();

//                    for (int m = 0; m < fatherGoodsEntities.size(); m++) {
//                        String nxGoodsName = fatherGoodsEntities.get(m).getGbDfgFatherGoodsName();
//                        System.out.println("nxGoodsName" + nxGoodsName);
//                        String replace = nxGoodsName.replace("/", "-");
//                        total = total.add(new BigDecimal(1));
//
//                        HSSFSheet sheet = wb.createSheet(total + " " + replace);
////                        //设置表头
////                        HSSFRow row = sheet.createRow(0);
////                        row.createCell(0).setCellValue(total + " " + replace);
//
//                        //设置表头
//                        HSSFRow row1 = sheet.createRow(0);
//
//                        row1.createCell(0).setCellValue("序号");
//                        row1.createCell(1).setCellValue("goodsId");
//                        row1.createCell(2).setCellValue("goodsFatherId");
//                        row1.createCell(3).setCellValue("商品名称");
//                        row1.createCell(4).setCellValue("规格");
//                        row1.createCell(5).setCellValue("品牌");
//                        row1.createCell(6).setCellValue("产地");
//                        row1.createCell(7).setCellValue("库存");
//                        row1.createCell(8).setCellValue("单价");
//
//
//                        Map<String, Object> map21 = new HashMap<>();
//                        map21.put("dgFatherId", fatherGoodsEntities.get(m).getGbDistributerFatherGoodsId());
//                        map21.put("toDepId", depId);
//                        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryDisGoodsByParams(map21);
//                        //设置表体
//                        HSSFRow goodsRow = null;
//                        for (int i = 0; i < goodsEntities.size(); i++) {
//                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
//                            goodsRow = sheet.createRow(i + 2);
//                            goodsRow.createCell(0).setCellValue(i + 1);
//                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDistributerGoodsId());
//                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgDfgGoodsFatherId());
//                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsName());
//                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
//                            goodsRow.createCell(5).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
//                            goodsRow.createCell(6).setCellValue(ckGoodsEntity.getGbDgGoodsPlace());
//                        }
//
//                    }

//                    }
                }


            }


            String fileName = new String("导出商品.xls".getBytes("utf-8"), "iso8859-1");
            response.setHeader("content-Disposition", "attachment; filename =" + fileName);
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @RequestMapping("/downloadExcelGb")
    @ResponseBody
    public void downloadExcelGb(HttpServletResponse response, HttpServletRequest request) {
        String fatherId = request.getParameter("fatherId");
        String fatherName = request.getParameter("fatherName");
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            BigDecimal total = new BigDecimal(0);
            Map<String, Object> map1 = new HashMap<>();
            map1.put("fathersFatherId", fatherId);
            List<GbDistributerFatherGoodsEntity> grandEntities = dgfService.queryDisFathersGoodsByParamsGb(map1);
            for (int m = 0; m < grandEntities.size(); m++) {
                String nxGoodsName = grandEntities.get(m).getGbDfgFatherGoodsName();
                String replace = nxGoodsName.replace("/", "-");
                total = total.add(new BigDecimal(1));

                HSSFSheet sheet = wb.createSheet(total + " " + replace);
                //设置表头
                HSSFRow row = sheet.createRow(0);
                row.createCell(0).setCellValue(total + " " + replace);

                //设置表头
                HSSFRow row1 = sheet.createRow(1);

                row1.createCell(0).setCellValue("序号");
                row1.createCell(1).setCellValue("商品名称");
                row1.createCell(2).setCellValue("规格");
                row1.createCell(3).setCellValue("品牌");
                row1.createCell(4).setCellValue("产地");
                row1.createCell(5).setCellValue("介绍");


                Map<String, Object> map2 = new HashMap<>();
                map2.put("dgFatherId", grandEntities.get(m).getGbDistributerFatherGoodsId());
                List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryDisGoodsByParams(map2);
                //设置表体
                HSSFRow goodsRow = null;
                for (int i = 0; i < goodsEntities.size(); i++) {
                    GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                    goodsRow = sheet.createRow(i + 2);
                    goodsRow.createCell(0).setCellValue(i + 1);
                    goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                    goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                    goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                    goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsPlace());
                    goodsRow.createCell(5).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                }


            }

            String fileName = new String("导出商品.xls".getBytes("utf-8"), "iso8859-1");
            response.setHeader("content-Disposition", "attachment; filename =" + fileName);
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


//    private void changeInventoryDailyData(GbDistributerGoodsEntity gbGoods) {
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("settleId", -1);
//        map1.put("disGoodsId", gbGoods.getGbDistributerGoodsId());
//        List<GbDepInventoryGoodsDailyEntity> dailyEntities = gbDepInventoryGoodsDailyService.queryDailyStockListByParams(map1);
//        if (dailyEntities.size() > 0) {
//            for (GbDepInventoryGoodsDailyEntity dailyEntity : dailyEntities) {
//                //update Week, Month
//                Map<String, Object> map2 = new HashMap<>();
//                map2.put("depId", dailyEntity.getGbIgdDepartmentId());
//                Integer newGoodsInventoryType = gbGoods.getGbDgGoodsInventoryType();
//                if (newGoodsInventoryType.equals(getDISGoodsInventroyWeek())) {
//                    // 1, 添加新weekGoods
//                    GbDepInventoryGoodsWeekEntity weekGoodsEntity = new GbDepInventoryGoodsWeekEntity();
//                    weekGoodsEntity.setGbIgwReturnWeight(dailyEntity.getGbIgdReturnWeight());
//                    weekGoodsEntity.setGbIgwReturnSubtotal(dailyEntity.getGbIgdReturnSubtotal());
//                    weekGoodsEntity.setGbIgwStatus(dailyEntity.getGbIgdStatus());
//                    weekGoodsEntity.setGbIgwWeek(getWeekOfYear(0).toString());
//                    weekGoodsEntity.setGbIgwYear(formatWhatYear(0));
//                    weekGoodsEntity.setGbIgwLossWeight(dailyEntity.getGbIgdLossWeight());
//                    weekGoodsEntity.setGbIgwLossSubtotal(dailyEntity.getGbIgdLossSubtotal());
//                    weekGoodsEntity.setGbIgwWasteSubtotal(dailyEntity.getGbIgdWasteSubtotal());
//                    weekGoodsEntity.setGbIgwWasteWeight(dailyEntity.getGbIgdWasteWeight());
//                    weekGoodsEntity.setGbIgwProduceWeight(dailyEntity.getGbIgdProduceWeight());
//                    weekGoodsEntity.setGbIgwProduceSubtotal(dailyEntity.getGbIgdProduceSubtotal());
//                    weekGoodsEntity.setGbIgwReturnWeight(dailyEntity.getGbIgdReturnWeight());
//                    weekGoodsEntity.setGbIgwReturnSubtotal(dailyEntity.getGbIgdReturnSubtotal());
//
//                    weekGoodsEntity.setGbIgwDepartmentFatherId(dailyEntity.getGbIgdDepartmentFatherId());
//                    weekGoodsEntity.setGbIgwDepartmentId(dailyEntity.getGbIgdDepartmentId());
//                    weekGoodsEntity.setGbIgwDisGoodsFatherId(dailyEntity.getGbIgdDisGoodsFatherId());
//                    weekGoodsEntity.setGbIgwDisGoodsId(dailyEntity.getGbIgdDisGoodsId());
//                    weekGoodsEntity.setGbIgwDistributerId(dailyEntity.getGbIgdDistributerId());
//                    weekGoodsEntity.setGbIgwGbDepStockId(dailyEntity.getGbIgdGbDepStockId());
//                    weekGoodsEntity.setGbIgwFullTime(dailyEntity.getGbIgdFullTime());
//                    weekGoodsEntity.setGbIgwDepDisGoodsId(dailyEntity.getGbIgdDepDisGoodsId());
//                    gbDepInventoryGoodsWeekService.save(weekGoodsEntity);
//
//
//                    GbDepInventoryWeekEntity inventoryWeekEntity = gbDepInventoryWeekService.queryInventoryWeek(map2);
//
//                    BigDecimal wasteTotal = new BigDecimal(inventoryWeekEntity.getGbDiwWasteTotal()).add(new BigDecimal(dailyEntity.getGbIgdWasteSubtotal()));
//                    BigDecimal returnTotal = new BigDecimal(inventoryWeekEntity.getGbDiwReturnTotal()).add(new BigDecimal(dailyEntity.getGbIgdReturnSubtotal()));
//                    BigDecimal lossTotal = new BigDecimal(inventoryWeekEntity.getGbDiwLossTotal()).add(new BigDecimal(dailyEntity.getGbIgdLossSubtotal()));
//                    BigDecimal subTotal = new BigDecimal(inventoryWeekEntity.getGbDiwProduceTotal()).add(new BigDecimal(dailyEntity.getGbIgdProduceSubtotal()));
//                    inventoryWeekEntity.setGbDiwWasteTotal(wasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryWeekEntity.setGbDiwReturnTotal(returnTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryWeekEntity.setGbDiwLossTotal(lossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryWeekEntity.setGbDiwProduceTotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    gbDepInventoryWeekService.update(inventoryWeekEntity);
//
//                }
//                if (newGoodsInventoryType.equals(getDISGoodsInventroyMonth())) {
//                    // 1, 添加新weekGoods
//                    GbDepInventoryGoodsMonthEntity monthGoodsEntity = new GbDepInventoryGoodsMonthEntity();
//                    monthGoodsEntity.setGbIgmReturnSubtotal(dailyEntity.getGbIgdReturnSubtotal());
//                    monthGoodsEntity.setGbIgmReturnSubtotal(dailyEntity.getGbIgdReturnSubtotal());
//                    monthGoodsEntity.setGbIgmMonth(formatWhatMonth(0));
//                    monthGoodsEntity.setGbIgmStatus(dailyEntity.getGbIgdStatus());
//                    monthGoodsEntity.setGbIgmLossSubtotal(dailyEntity.getGbIgdLossSubtotal());
//                    monthGoodsEntity.setGbIgmLossSubtotal(dailyEntity.getGbIgdLossSubtotal());
//                    monthGoodsEntity.setGbIgmWasteSubtotal(dailyEntity.getGbIgdWasteSubtotal());
//                    monthGoodsEntity.setGbIgmWasteWeight(dailyEntity.getGbIgdWasteWeight());
//                    monthGoodsEntity.setGbIgmReturnWeight(dailyEntity.getGbIgdReturnWeight());
//                    monthGoodsEntity.setGbIgmReturnSubtotal(dailyEntity.getGbIgdReturnSubtotal());
//                    monthGoodsEntity.setGbIgmProduceWeight(dailyEntity.getGbIgdProduceWeight());
//                    monthGoodsEntity.setGbIgmProduceSubtotal(dailyEntity.getGbIgdProduceSubtotal());
//                    monthGoodsEntity.setGbIgmDepartmentFatherId(dailyEntity.getGbIgdDepartmentFatherId());
//                    monthGoodsEntity.setGbIgmDepartmentId(dailyEntity.getGbIgdDepartmentId());
//                    monthGoodsEntity.setGbIgmDisGoodsFatherId(dailyEntity.getGbIgdDisGoodsFatherId());
//                    monthGoodsEntity.setGbIgmDisGoodsId(dailyEntity.getGbIgdDisGoodsId());
//                    monthGoodsEntity.setGbIgmDistributerId(dailyEntity.getGbIgdDistributerId());
//                    monthGoodsEntity.setGbIgmGbDepStockId(dailyEntity.getGbIgdGbDepStockId());
//                    monthGoodsEntity.setGbIgmFullTime(dailyEntity.getGbIgdFullTime());
//                    gbDepInventoryGoodsMonthService.save(monthGoodsEntity);
//
//
//                    GbDepInventoryMonthEntity inventoryMonthEntity = gbDepInventoryMonthService.queryInventoryMonth(map2);
//                    System.out.println(inventoryMonthEntity.getGbImWasteTotal() + "111111111111");
//                    System.out.println(dailyEntity.getGbIgdWasteSubtotal() + "222222222222");
//
//                    BigDecimal wasteTotal = new BigDecimal(inventoryMonthEntity.getGbImWasteTotal()).add(new BigDecimal(dailyEntity.getGbIgdWasteSubtotal()));
//                    BigDecimal returnTotal = new BigDecimal(inventoryMonthEntity.getGbImReturnTotal()).add(new BigDecimal(dailyEntity.getGbIgdReturnSubtotal()));
//                    BigDecimal lossTotal = new BigDecimal(inventoryMonthEntity.getGbImLossTotal()).add(new BigDecimal(dailyEntity.getGbIgdLossSubtotal()));
//                    BigDecimal subTotal = new BigDecimal(inventoryMonthEntity.getGbImProduceTotal()).add(new BigDecimal(dailyEntity.getGbIgdProduceSubtotal()));
//                    inventoryMonthEntity.setGbImWasteTotal(wasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryMonthEntity.setGbImReturnTotal(returnTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryMonthEntity.setGbImLossTotal(lossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryMonthEntity.setGbImProduceTotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    gbDepInventoryMonthService.update(inventoryMonthEntity);
//
//
//                }
//
//                GbDepInventoryDailyEntity inventoryDailyEntity = gbDepInventoryDailyService.queryInventoryDaily(map2);
//                BigDecimal subtotal = new BigDecimal(inventoryDailyEntity.getGbIdProduceTotal()).subtract(new BigDecimal(dailyEntity.getGbIgdProduceSubtotal()));
//                BigDecimal loss = new BigDecimal(inventoryDailyEntity.getGbIdLossTotal()).subtract(new BigDecimal(dailyEntity.getGbIgdLossSubtotal()));
//                BigDecimal returnTotal1 = new BigDecimal(inventoryDailyEntity.getGbIdReturnTotal()).subtract(new BigDecimal(dailyEntity.getGbIgdReturnSubtotal()));
//                BigDecimal waste = new BigDecimal(inventoryDailyEntity.getGbIdWasteTotal()).subtract(new BigDecimal(dailyEntity.getGbIgdWasteSubtotal()));
//                inventoryDailyEntity.setGbIdProduceTotal(subtotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryDailyEntity.setGbIdLossTotal(loss.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryDailyEntity.setGbIdReturnTotal(returnTotal1.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryDailyEntity.setGbIdWasteTotal(waste.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                gbDepInventoryDailyService.update(inventoryDailyEntity);
//
//                // 3, delete weekGoods
//                gbDepInventoryGoodsDailyService.delete(dailyEntity.getGbInventoryGoodsDailyId());
//            }
//        }
//
//    }
//
//
//    private void changeInventoryWeekData(GbDistributerGoodsEntity gbGoods) {
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("settleId", -1);
//        map1.put("disGoodsId", gbGoods.getGbDistributerGoodsId());
//        List<GbDepInventoryGoodsWeekEntity> weekEntities = gbDepInventoryGoodsWeekService.queryWeekStockListByParams(map1);
//        if (weekEntities.size() > 0) {
//            for (GbDepInventoryGoodsWeekEntity weekEntity : weekEntities) {
//                //update Week, Daily
//                Map<String, Object> map2 = new HashMap<>();
//                map2.put("depId", weekEntity.getGbIgwDepartmentId());
//                Integer newGoodsInventoryType = gbGoods.getGbDgGoodsInventoryType();
//                if (newGoodsInventoryType.equals(getDISGoodsInventroyDaily())) {
//                    // 1, 添加新weekGoods
//                    GbDepInventoryGoodsDailyEntity dailyGoodsEntity = new GbDepInventoryGoodsDailyEntity();
//                    dailyGoodsEntity.setGbIgdReturnWeight(weekEntity.getGbIgwReturnWeight());
//                    dailyGoodsEntity.setGbIgdReturnSubtotal(weekEntity.getGbIgwReturnSubtotal());
//                    dailyGoodsEntity.setGbIgdStatus(weekEntity.getGbIgwStatus());
//                    dailyGoodsEntity.setGbIgdLossWeight(weekEntity.getGbIgwLossWeight());
//                    dailyGoodsEntity.setGbIgdLossSubtotal(weekEntity.getGbIgwLossSubtotal());
//                    dailyGoodsEntity.setGbIgdWasteSubtotal(weekEntity.getGbIgwWasteSubtotal());
//                    dailyGoodsEntity.setGbIgdWasteWeight(weekEntity.getGbIgwWasteWeight());
//                    dailyGoodsEntity.setGbIgdProduceWeight(weekEntity.getGbIgwProduceWeight());
//                    dailyGoodsEntity.setGbIgdProduceSubtotal(weekEntity.getGbIgwProduceSubtotal());
//                    dailyGoodsEntity.setGbIgdReturnWeight(weekEntity.getGbIgwReturnWeight());
//                    dailyGoodsEntity.setGbIgdReturnSubtotal(weekEntity.getGbIgwReturnSubtotal());
//                    dailyGoodsEntity.setGbIgdDepartmentFatherId(weekEntity.getGbIgwDepartmentFatherId());
//                    dailyGoodsEntity.setGbIgdDepartmentId(weekEntity.getGbIgwDepartmentId());
//                    dailyGoodsEntity.setGbIgdDisGoodsFatherId(weekEntity.getGbIgwDisGoodsFatherId());
//                    dailyGoodsEntity.setGbIgdDisGoodsId(weekEntity.getGbIgwDisGoodsId());
//                    dailyGoodsEntity.setGbIgdDistributerId(weekEntity.getGbIgwDistributerId());
//                    dailyGoodsEntity.setGbIgdGbDepStockId(weekEntity.getGbIgwGbDepStockId());
//                    dailyGoodsEntity.setGbIgdFullTime(weekEntity.getGbIgwFullTime());
//                    dailyGoodsEntity.setGbIgdDepDisGoodsId(weekEntity.getGbIgwDepDisGoodsId());
//                    gbDepInventoryGoodsDailyService.save(dailyGoodsEntity);
//
//
//                    GbDepInventoryDailyEntity inventoryDailyEntity = gbDepInventoryDailyService.queryInventoryDaily(map2);
//                    BigDecimal wasteTotal = new BigDecimal(inventoryDailyEntity.getGbIdWasteTotal()).add(new BigDecimal(weekEntity.getGbIgwWasteSubtotal()));
//                    BigDecimal returnTotal = new BigDecimal(inventoryDailyEntity.getGbIdReturnTotal()).add(new BigDecimal(weekEntity.getGbIgwReturnSubtotal()));
//                    BigDecimal lossTotal = new BigDecimal(inventoryDailyEntity.getGbIdLossTotal()).add(new BigDecimal(weekEntity.getGbIgwLossSubtotal()));
//                    BigDecimal subTotal = new BigDecimal(inventoryDailyEntity.getGbIdProduceTotal()).add(new BigDecimal(weekEntity.getGbIgwSubtotal()));
//                    inventoryDailyEntity.setGbIdWasteTotal(wasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryDailyEntity.setGbIdReturnTotal(returnTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryDailyEntity.setGbIdLossTotal(lossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryDailyEntity.setGbIdProduceTotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    gbDepInventoryDailyService.update(inventoryDailyEntity);
//
//                }
//                if (newGoodsInventoryType.equals(getDISGoodsInventroyMonth())) {
//                    // 1, 添加新weekGoods
//                    GbDepInventoryGoodsMonthEntity monthGoodsEntity = new GbDepInventoryGoodsMonthEntity();
//                    monthGoodsEntity.setGbIgmReturnWeight(weekEntity.getGbIgwReturnWeight());
//                    monthGoodsEntity.setGbIgmReturnSubtotal(weekEntity.getGbIgwReturnSubtotal());
//                    monthGoodsEntity.setGbIgmMonth(formatWhatMonth(0));
//                    monthGoodsEntity.setGbIgmYear(weekEntity.getGbIgwYear());
//                    monthGoodsEntity.setGbIgmStatus(weekEntity.getGbIgwStatus());
//                    monthGoodsEntity.setGbIgmLossWeight(weekEntity.getGbIgwLossWeight());
//                    monthGoodsEntity.setGbIgmLossSubtotal(weekEntity.getGbIgwLossSubtotal());
//                    monthGoodsEntity.setGbIgmWasteSubtotal(weekEntity.getGbIgwWasteSubtotal());
//                    monthGoodsEntity.setGbIgmWasteWeight(weekEntity.getGbIgwWasteWeight());
//                    monthGoodsEntity.setGbIgmProduceWeight(weekEntity.getGbIgwWeight());
//                    monthGoodsEntity.setGbIgmProduceSubtotal(weekEntity.getGbIgwSubtotal());
//                    monthGoodsEntity.setGbIgmDepartmentFatherId(weekEntity.getGbIgwDepartmentFatherId());
//                    monthGoodsEntity.setGbIgmDepartmentId(weekEntity.getGbIgwDepartmentId());
//                    monthGoodsEntity.setGbIgmDisGoodsFatherId(weekEntity.getGbIgwDisGoodsFatherId());
//                    monthGoodsEntity.setGbIgmDisGoodsId(weekEntity.getGbIgwDisGoodsId());
//                    monthGoodsEntity.setGbIgmDistributerId(weekEntity.getGbIgwDistributerId());
//                    monthGoodsEntity.setGbIgmGbDepStockId(weekEntity.getGbIgwGbDepStockId());
//                    monthGoodsEntity.setGbIgmFullTime(weekEntity.getGbIgwFullTime());
//                    gbDepInventoryGoodsMonthService.save(monthGoodsEntity);
//
//
//                    GbDepInventoryMonthEntity inventoryMonthEntity = gbDepInventoryMonthService.queryInventoryMonth(map2);
//                    BigDecimal wasteTotal = new BigDecimal(inventoryMonthEntity.getGbImWasteTotal()).add(new BigDecimal(weekEntity.getGbIgwSubtotal()));
//                    BigDecimal returnTotal = new BigDecimal(inventoryMonthEntity.getGbImReturnTotal()).add(new BigDecimal(weekEntity.getGbIgwReturnSubtotal()));
//                    BigDecimal lossTotal = new BigDecimal(inventoryMonthEntity.getGbImLossTotal()).add(new BigDecimal(weekEntity.getGbIgwLossSubtotal()));
//                    BigDecimal subTotal = new BigDecimal(inventoryMonthEntity.getGbImProduceTotal()).add(new BigDecimal(weekEntity.getGbIgwSubtotal()));
//                    inventoryMonthEntity.setGbImWasteTotal(wasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryMonthEntity.setGbImReturnTotal(returnTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryMonthEntity.setGbImLossTotal(lossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryMonthEntity.setGbImProduceTotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    gbDepInventoryMonthService.update(inventoryMonthEntity);
//
//
//                }
//
//                GbDepInventoryWeekEntity inventoryWeekEntity = gbDepInventoryWeekService.queryInventoryWeek(map2);
//                BigDecimal subtotal = new BigDecimal(inventoryWeekEntity.getGbDiwProduceTotal()).subtract(new BigDecimal(weekEntity.getGbIgwSubtotal()));
//                BigDecimal loss = new BigDecimal(inventoryWeekEntity.getGbDiwLossTotal()).subtract(new BigDecimal(weekEntity.getGbIgwLossSubtotal()));
//                BigDecimal returnTotal1 = new BigDecimal(inventoryWeekEntity.getGbDiwReturnTotal()).subtract(new BigDecimal(weekEntity.getGbIgwReturnSubtotal()));
//                BigDecimal waste = new BigDecimal(inventoryWeekEntity.getGbDiwWasteTotal()).subtract(new BigDecimal(weekEntity.getGbIgwWasteSubtotal()));
//                inventoryWeekEntity.setGbDiwProduceTotal(subtotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryWeekEntity.setGbDiwLossTotal(loss.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryWeekEntity.setGbDiwReturnTotal(returnTotal1.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryWeekEntity.setGbDiwWasteTotal(waste.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                gbDepInventoryWeekService.update(inventoryWeekEntity);
//
//                // 3, delete weekGoods
//                gbDepInventoryGoodsWeekService.delete(weekEntity.getGbInventoryGoodsWeekId());
//            }
//        }
//
//    }
//
//
//    private void changeInventoryMonthData(GbDistributerGoodsEntity gbGoods) {
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("settleId", -1);
//        map1.put("disGoodsId", gbGoods.getGbDistributerGoodsId());
//        List<GbDepInventoryGoodsMonthEntity> monthEntities = gbDepInventoryGoodsMonthService.queryMonthStockListByParams(map1);
//        if (monthEntities.size() > 0) {
//            for (GbDepInventoryGoodsMonthEntity month : monthEntities) {
//                //update Week, Daily
//                Map<String, Object> map2 = new HashMap<>();
//                map2.put("depId", month.getGbIgmDepartmentId());
//                Integer newGoodsInventoryType = gbGoods.getGbDgGoodsInventoryType();
//                if (newGoodsInventoryType.equals(getDISGoodsInventroyDaily())) {
//                    // 1, 添加新weekGoods
//                    GbDepInventoryGoodsDailyEntity dailyGoodsEntity = new GbDepInventoryGoodsDailyEntity();
//                    dailyGoodsEntity.setGbIgdReturnSubtotal(month.getGbIgmReturnSubtotal());
//                    dailyGoodsEntity.setGbIgdReturnSubtotal(month.getGbIgmReturnSubtotal());
//                    dailyGoodsEntity.setGbIgdStatus(month.getGbIgmStatus());
//                    dailyGoodsEntity.setGbIgdLossSubtotal(month.getGbIgmLossSubtotal());
//                    dailyGoodsEntity.setGbIgdLossSubtotal(month.getGbIgmLossSubtotal());
//                    dailyGoodsEntity.setGbIgdWasteSubtotal(month.getGbIgmWasteSubtotal());
//                    dailyGoodsEntity.setGbIgdWasteWeight(month.getGbIgmWasteWeight());
//                    dailyGoodsEntity.setGbIgdProduceWeight(month.getGbIgmProduceWeight());
//                    dailyGoodsEntity.setGbIgdProduceSubtotal(month.getGbIgmProduceSubtotal());
//                    dailyGoodsEntity.setGbIgdDepartmentFatherId(month.getGbIgmDepartmentFatherId());
//                    dailyGoodsEntity.setGbIgdDepartmentId(month.getGbIgmDepartmentId());
//                    dailyGoodsEntity.setGbIgdDisGoodsFatherId(month.getGbIgmDisGoodsFatherId());
//                    dailyGoodsEntity.setGbIgdDisGoodsId(month.getGbIgmDisGoodsId());
//                    dailyGoodsEntity.setGbIgdDistributerId(month.getGbIgmDistributerId());
//                    dailyGoodsEntity.setGbIgdGbDepStockId(month.getGbIgmGbDepStockId());
//                    dailyGoodsEntity.setGbIgdFullTime(month.getGbIgmFullTime());
//                    gbDepInventoryGoodsDailyService.save(dailyGoodsEntity);
//
//                    //
//                    GbDepInventoryDailyEntity inventoryDailyEntity = gbDepInventoryDailyService.queryInventoryDaily(map2);
//                    if (inventoryDailyEntity == null) {
//                        GbDepInventoryDailyEntity dailyEntity1 = new GbDepInventoryDailyEntity();
//                        dailyEntity1.setGbIdDate(formatWhatDay(0));
//                        dailyEntity1.setGbIdProduceTotal(month.getGbIgmSubtotal());
//                        dailyEntity1.setGbIdDepartmentId(month.getGbIgmDepartmentId());
//                        dailyEntity1.setGbIdDepartmentFatherId(month.getGbIgmDepartmentFatherId());
//                        dailyEntity1.setGbIdDistributerId(month.getGbIgmDistributerId());
//                        dailyEntity1.setGbIdWeek(getWeek(0));
//                        dailyEntity1.setGbIdYear(formatWhatYear(0));
//                        dailyEntity1.setGbIdProduceTotal("0");
//                        dailyEntity1.setGbIdWasteTotal("0");
//                        dailyEntity1.setGbIdLossTotal("0");
//                        dailyEntity1.setGbIdReturnTotal("0");
//                        dailyEntity1.setGbIdStatus(0);
//                        gbDepInventoryDailyService.save(dailyEntity1);
//
//                    } else {
//                        BigDecimal wasteTotal = new BigDecimal(inventoryDailyEntity.getGbIdWasteTotal()).add(new BigDecimal(month.getGbIgmWasteSubtotal()));
//                        BigDecimal returnTotal = new BigDecimal(inventoryDailyEntity.getGbIdReturnTotal()).add(new BigDecimal(month.getGbIgmReturnSubtotal()));
//                        BigDecimal lossTotal = new BigDecimal(inventoryDailyEntity.getGbIdLossTotal()).add(new BigDecimal(month.getGbIgmLossSubtotal()));
//                        BigDecimal subTotal = new BigDecimal(inventoryDailyEntity.getGbIdProduceTotal()).add(new BigDecimal(month.getGbIgmSubtotal()));
//                        inventoryDailyEntity.setGbIdWasteTotal(wasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        inventoryDailyEntity.setGbIdReturnTotal(returnTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        inventoryDailyEntity.setGbIdLossTotal(lossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        inventoryDailyEntity.setGbIdProduceTotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        gbDepInventoryDailyService.update(inventoryDailyEntity);
//                    }
//                }
//
//                if (newGoodsInventoryType.equals(getDISGoodsInventroyWeek())) {
//                    // 1, 添加新weekGoods
//                    GbDepInventoryGoodsWeekEntity weekGoodsEntity = new GbDepInventoryGoodsWeekEntity();
//                    weekGoodsEntity.setGbIgwReturnSubtotal(month.getGbIgmReturnSubtotal());
//                    weekGoodsEntity.setGbIgwReturnWeight(month.getGbIgmReturnWeight());
//                    weekGoodsEntity.setGbIgwWeek(getWeekOfYear(0).toString());
//                    weekGoodsEntity.setGbIgwYear(month.getGbIgmYear());
//                    weekGoodsEntity.setGbIgwStatus(month.getGbIgmStatus());
//                    weekGoodsEntity.setGbIgwLossWeight(month.getGbIgmLossWeight());
//                    weekGoodsEntity.setGbIgwLossSubtotal(month.getGbIgmLossSubtotal());
//                    weekGoodsEntity.setGbIgwWasteSubtotal(month.getGbIgmWasteSubtotal());
//                    weekGoodsEntity.setGbIgwWasteWeight(month.getGbIgmWasteWeight());
//                    weekGoodsEntity.setGbIgwProduceWeight(month.getGbIgmProduceWeight());
//                    weekGoodsEntity.setGbIgwProduceSubtotal(month.getGbIgmProduceSubtotal());
//                    weekGoodsEntity.setGbIgwDepartmentFatherId(month.getGbIgmDepartmentFatherId());
//                    weekGoodsEntity.setGbIgwDepartmentId(month.getGbIgmDepartmentId());
//                    weekGoodsEntity.setGbIgwDisGoodsFatherId(month.getGbIgmDisGoodsFatherId());
//                    weekGoodsEntity.setGbIgwDisGoodsId(month.getGbIgmDisGoodsId());
//                    weekGoodsEntity.setGbIgwDistributerId(month.getGbIgmDistributerId());
//                    weekGoodsEntity.setGbIgwGbDepStockId(month.getGbIgmGbDepStockId());
//                    weekGoodsEntity.setGbIgwFullTime(month.getGbIgmFullTime());
//                    weekGoodsEntity.setGbIgwDepDisGoodsId(month.getGbIgmDepDisGoodsId());
//                    gbDepInventoryGoodsWeekService.save(weekGoodsEntity);
//
//
//                    GbDepInventoryWeekEntity inventoryWeekEntity = gbDepInventoryWeekService.queryInventoryWeek(map2);
//                    if (inventoryWeekEntity == null) {
//                        GbDepInventoryWeekEntity weekEntity1 = new GbDepInventoryWeekEntity();
//                        weekEntity1.setGbDiwProduceTotal(month.getGbIgmWasteSubtotal());
//                        weekEntity1.setGbDiwDepartmentId(month.getGbIgmDepartmentId());
//                        weekEntity1.setGbDiwDepartmentFatherId(month.getGbIgmDepartmentFatherId());
//                        weekEntity1.setGbDiwDistributerId(month.getGbIgmDistributerId());
//                        weekEntity1.setGbDiwWeek(getWeekOfYear(0).toString());
//                        weekEntity1.setGbDiwYear(formatWhatYear(0));
//                        weekEntity1.setGbDiwWasteTotal("0");
//                        weekEntity1.setGbDiwLossTotal("0");
//                        weekEntity1.setGbDiwReturnTotal("0");
//                        weekEntity1.setGbDiwProduceTotal("0");
//                        weekEntity1.setGbDiwStatus(0);
//                        gbDepInventoryWeekService.save(weekEntity1);
//                    } else {
//                        BigDecimal wasteTotal = new BigDecimal(inventoryWeekEntity.getGbDiwWasteTotal()).add(new BigDecimal(month.getGbIgmWasteSubtotal()));
//                        BigDecimal returnTotal = new BigDecimal(inventoryWeekEntity.getGbDiwReturnTotal()).add(new BigDecimal(month.getGbIgmReturnSubtotal()));
//                        BigDecimal lossTotal = new BigDecimal(inventoryWeekEntity.getGbDiwLossTotal()).add(new BigDecimal(month.getGbIgmLossSubtotal()));
//                        BigDecimal subTotal = new BigDecimal(inventoryWeekEntity.getGbDiwProduceTotal()).add(new BigDecimal(month.getGbIgmSubtotal()));
//                        inventoryWeekEntity.setGbDiwWasteTotal(wasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        inventoryWeekEntity.setGbDiwReturnTotal(returnTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        inventoryWeekEntity.setGbDiwLossTotal(lossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        inventoryWeekEntity.setGbDiwProduceTotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        gbDepInventoryWeekService.update(inventoryWeekEntity);
//                    }
//                }
//
//                GbDepInventoryMonthEntity inventoryMonthEntity = gbDepInventoryMonthService.queryInventoryMonth(map2);
//                BigDecimal subtotal = new BigDecimal(inventoryMonthEntity.getGbImProduceTotal()).subtract(new BigDecimal(month.getGbIgmSubtotal()));
//                BigDecimal loss = new BigDecimal(inventoryMonthEntity.getGbImLossTotal()).subtract(new BigDecimal(month.getGbIgmLossSubtotal()));
//                BigDecimal returnTotal1 = new BigDecimal(inventoryMonthEntity.getGbImReturnTotal()).subtract(new BigDecimal(month.getGbIgmReturnSubtotal()));
//                BigDecimal waste = new BigDecimal(inventoryMonthEntity.getGbImWasteTotal()).subtract(new BigDecimal(month.getGbIgmWasteSubtotal()));
//                inventoryMonthEntity.setGbImProduceTotal(subtotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryMonthEntity.setGbImLossTotal(loss.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryMonthEntity.setGbImReturnTotal(returnTotal1.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryMonthEntity.setGbImWasteTotal(waste.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                gbDepInventoryMonthService.update(inventoryMonthEntity);
//
//                // 3, delete month
//                gbDepInventoryGoodsMonthService.delete(month.getGbInventoryGoodsMonthId());
//            }
//        }
//
//    }


}





