package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-21 21:51
 */

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.CommonUtils.generateBillTradeNo;
import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatFullTime;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbOrderStatusHasFinished;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/gbdepartmentorders")
public class GbDepartmentOrdersController {
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDistributerPurchaseBatchService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
    @Autowired
    private GbDepartmentOrdersHistoryService gbDepOrdersHistoryService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDPBService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDistributerSupplierService gbDistributerSupplierService;
    @Autowired
    private GbDepartmentUserService gbDepartmentUserService;
    @Autowired
    private GbDistributerGoodsPriceService goodsPriceService;
    @Autowired
    private GbDistributerWeightTotalService gbDisWeightTotalService;
    @Autowired
    private GbDistributerWeightGoodsService gbDisWeightGoodsService;
    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepGoodsStockReduceService;
    @Autowired
    private NxJrdhSupplierService jrdhSupplierService;
    @Autowired
    private NxDepartmentBillService nxDepartmentBillService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDPBService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;
    @Autowired
    private GbDistributerFatherGoodsService gbDistributerFatherGoodsService;
    @Autowired
    private NxDistributerUserService nxDistributerUserService;
    @Autowired
    private NxDistributerGbDistributerService nxDisGbDisService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxAliasService nxAliasService;
    @Autowired
    private NxDistributerStandardService dsService;
    @Autowired
    private NxDistributerAliasService disAliasService;
    @Autowired
    private NxStandardService nxStandardService;


    @RequestMapping(value = "/choiceGoodsForApply", method = RequestMethod.POST)
    @ResponseBody
    public R choiceGoodsForApply(@RequestBody GbDepartmentOrdersEntity orders) {
        Integer doDisGoodsId = orders.getGbDoDisGoodsId();
        if (doDisGoodsId == null) {
            return R.error("商品ID不能为空");
        }

        GbDistributerGoodsEntity disGoodsEntity = gbDistributerGoodsService.queryObject(doDisGoodsId);
        if (disGoodsEntity == null) {
            return R.error("未找到对应的分销商商品信息");
        }

        GbDistributerGoodsEntity gbDistributerGoodsEntityTotal = gbDistributerGoodsService.queryObject(disGoodsEntity.getGbDistributerGoodsId());
        if (gbDistributerGoodsEntityTotal == null) {
            return R.error("未找到对应的分销商商品总信息");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", doDisGoodsId);
        map.put("depId", orders.getGbDoDepartmentId());
        GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity = gbDepartmentDisGoodsService.queryDepGoodsItemByParams(map);

        if (gbDepartmentDisGoodsEntity == null) {
            saveDepGoodsForDepOrder(orders);
        } else {
            orders.setGbDoDepDisGoodsId(gbDepartmentDisGoodsEntity.getGbDepartmentDisGoodsId());
        }

        orders.setGbDoDisGoodsId(gbDistributerGoodsEntityTotal.getGbDistributerGoodsId());
        GbDepartmentOrdersEntity gbDepartmentOrdersEntity1 = saveOneOrderUpdate(orders, gbDistributerGoodsEntityTotal);

        System.out.println("choicicicid" + gbDepartmentOrdersEntity1);
        return R.ok().put("data", gbDepartmentOrdersEntity1);
    }


    @RequestMapping(value = "/depSearchTodayOrders", method = RequestMethod.POST)
    @ResponseBody
    public R depSearchTodayOrders(Integer depFatherId, Integer depId, String searchStr) {

        Map<String, Object> map = new HashMap<>();

        if (depFatherId != -1) {
            map.put("depFatherId", depFatherId);
        }
        if (depId != -1) {
            map.put("depId", depId);
        }


        String pinyinString = "";
        boolean hasChinese = false;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                hasChinese = true;
                break;
            }
        }
        if (hasChinese) {
            pinyinString = hanziToPinyin(searchStr);

        } else {
            // 全是拼音或英文
            pinyinString = searchStr;
            searchStr = null;
        }
        map.put("searchPinyin", pinyinString);
        map.put("searchStr", searchStr);
        map.put("status", 3);
        map.put("notEqualOrderType", 9);
        System.out.println("searchchhchc1111" + map);
        List<GbDepartmentOrdersEntity> nxDepartmentOrdersEntities = gbDepartmentOrdersService.queryDepWeightOrderSearch(map);
        System.out.println("orarrrr" + nxDepartmentOrdersEntities.size());
        return R.ok().put("data", nxDepartmentOrdersEntities);
    }


    @RequestMapping(value = "/depPasteSearchGoods", method = RequestMethod.POST)
    @ResponseBody
    public R depPasteSearchGoods(@RequestBody List<GbDepartmentOrdersEntity> orderList) {
        List<GbDepartmentOrdersEntity> returnList = new ArrayList<>();
        for (GbDepartmentOrdersEntity ordersEntity : orderList) {

            if (ordersEntity.getGbDoRemark().equals("-1")) {
                ordersEntity.setGbDoRemark(null);
            }

            //1, 查拼音
            Map<String, Object> map = new HashMap<>();
            map.put("depId", ordersEntity.getGbDoDepartmentId());
            map.put("name", ordersEntity.getGbDoGoodsName());
            map.put("orderStandard", ordersEntity.getGbDoStandard());
            System.out.println("duoggsshangpsuodepgods11111ddd11111" + map);
            List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntitiesFirst = gbDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
            if (departmentDisGoodsEntitiesFirst.size() == 1) {
                GbDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = departmentDisGoodsEntitiesFirst.get(0);
                Integer nxDdgDisGoodsId = nxDepartmentDisGoodsEntity.getGbDdgDisGoodsId();
                GbDistributerGoodsEntity distributerGoodsEntity = gbDistributerGoodsService.queryDisGoodsDetail(nxDdgDisGoodsId);
                ordersEntity.setGbDoDepDisGoodsId(nxDepartmentDisGoodsEntity.getGbDepartmentDisGoodsId());
                ordersEntity.setGbDoDisGoodsId(nxDdgDisGoodsId);
                returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
            } else {
                map.put("name", null);
                map.put("orderName", ordersEntity.getGbDoGoodsName());
                System.out.println("duoggsshangpsuodepgods222222ddd11111" + map);
                List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntitiesSecond = gbDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
                if (departmentDisGoodsEntitiesSecond.size() == 1) {
                    GbDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = departmentDisGoodsEntitiesSecond.get(0);
                    Integer nxDdgDisGoodsId = nxDepartmentDisGoodsEntity.getGbDdgDisGoodsId();
                    GbDistributerGoodsEntity distributerGoodsEntity = gbDistributerGoodsService.queryObject(nxDdgDisGoodsId);
                    System.out.println("depDigidid" + nxDdgDisGoodsId + "enndnt" + distributerGoodsEntity.getGbDistributerGoodsId());
                    ordersEntity.setGbDoDepDisGoodsId(nxDepartmentDisGoodsEntity.getGbDepartmentDisGoodsId());
                    ordersEntity.setGbDoDisGoodsId(nxDdgDisGoodsId);
                    returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));

                } else {

                    //查询 disGoods
                    String goodsName = ordersEntity.getGbDoGoodsName();
                    Map<String, Object> mapZero = new HashMap<>();
                    mapZero.put("disId", ordersEntity.getGbDoDistributerId());
                    mapZero.put("searchStr", goodsName);
                    mapZero.put("standard", ordersEntity.getGbDoStandard());
                    System.out.println("mapzreororororor111zzzizizizi" + mapZero);
                    List<GbDistributerGoodsEntity> distributerGoodsEntitiesZero = gbDistributerGoodsService.queryDisGoodsByName(mapZero);
                    if (distributerGoodsEntitiesZero.size() == 1) {
                        GbDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesZero.get(0);
                        ordersEntity.setGbDoDisGoodsId(disGoodsEntity.getGbDistributerGoodsId());
                        Map<String, Object> mapDep = new HashMap<>();
                        mapDep.put("disGoodsId", disGoodsEntity.getGbDistributerGoodsId());
                        mapDep.put("depId", ordersEntity.getGbDoDepartmentId());
                        GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity = gbDepartmentDisGoodsService.queryDepGoodsItemByParams(mapDep);
                        if (gbDepartmentDisGoodsEntity == null) {
                            saveDepGoodsForDepOrder(ordersEntity);
                        } else {
                            ordersEntity.setGbDoDepDisGoodsId(gbDepartmentDisGoodsEntity.getGbDepartmentDisGoodsId());
                        }

                        returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));

                    } else {


                        //保存临时订单
                        System.out.println("savelinshsiis");
                        returnList.add(saveTemp(ordersEntity));

                    }
                }
            }

        }


        return R.ok().put("data", returnList);
    }


    private GbDepartmentOrdersEntity saveDepGoodsForDepOrder(GbDepartmentOrdersEntity gbDepartmentOrders) {
        // add purchaseGoods
        Integer gbDoDisGoodsId = gbDepartmentOrders.getGbDoDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        //添加部门商品
        GbDepartmentDisGoodsEntity mendianDisGoodsEntity = new GbDepartmentDisGoodsEntity();
        mendianDisGoodsEntity.setGbDdgDepGoodsName(gbDistributerGoodsEntity.getGbDgGoodsName());
        mendianDisGoodsEntity.setGbDdgDisGoodsId(gbDistributerGoodsEntity.getGbDistributerGoodsId());
        mendianDisGoodsEntity.setGbDdgDisGoodsFatherId(gbDistributerGoodsEntity.getGbDgDfgGoodsFatherId());
        mendianDisGoodsEntity.setGbDdgDisGoodsGrandId(gbDistributerGoodsEntity.getGbDgDfgGoodsGrandId());
        mendianDisGoodsEntity.setGbDdgDisGoodsGreatId(gbDistributerGoodsEntity.getGbDgDfgGoodsGreatId());
        mendianDisGoodsEntity.setGbDdgDepGoodsPinyin(gbDistributerGoodsEntity.getGbDgGoodsPinyin());
        mendianDisGoodsEntity.setGbDdgDepGoodsPy(gbDistributerGoodsEntity.getGbDgGoodsPy());
        mendianDisGoodsEntity.setGbDdgDepGoodsStandardname(gbDistributerGoodsEntity.getGbDgGoodsStandardname());
        mendianDisGoodsEntity.setGbDdgDepartmentId(gbDepartmentOrders.getGbDoDepartmentId());
        mendianDisGoodsEntity.setGbDdgDepartmentFatherId(gbDepartmentOrders.getGbDoDepartmentFatherId());
        mendianDisGoodsEntity.setGbDdgGbDepartmentId(gbDistributerGoodsEntity.getGbDgGbDepartmentId());
        mendianDisGoodsEntity.setGbDdgGbDisId(gbDistributerGoodsEntity.getGbDgDistributerId());
        mendianDisGoodsEntity.setGbDdgGoodsType(gbDistributerGoodsEntity.getGbDgGoodsType());
        mendianDisGoodsEntity.setGbDdgStockTotalWeight("0.0");
        mendianDisGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        mendianDisGoodsEntity.setGbDdgShowStandardId(-1);
        mendianDisGoodsEntity.setGbDdgShowStandardName(gbDistributerGoodsEntity.getGbDgGoodsStandardname());
        mendianDisGoodsEntity.setGbDdgShowStandardScale("-1");
        mendianDisGoodsEntity.setGbDdgShowStandardWeight(null);
        mendianDisGoodsEntity.setGbDdgNxDistributerGoodsId(gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId());
        mendianDisGoodsEntity.setGbDdgNxDistributerId(-1);
        gbDepartmentDisGoodsService.save(mendianDisGoodsEntity);

        gbDepartmentOrders.setGbDoDepDisGoodsId(mendianDisGoodsEntity.getGbDepartmentDisGoodsId());

        return gbDepartmentOrders;

    }


    private GbDepartmentOrdersEntity saveOneOrderUpdate(GbDepartmentOrdersEntity gbDepartmentOrders, GbDistributerGoodsEntity gbDistributerGoodsEntity) {
        // 添加参数验证
        if (gbDepartmentOrders == null || gbDistributerGoodsEntity == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        gbDepartmentOrders.setGbDoNxGoodsId(gbDistributerGoodsEntity.getGbDgNxGoodsId());
        gbDepartmentOrders.setGbDoNxGoodsFatherId(gbDistributerGoodsEntity.getGbDgNxFatherId());
        gbDepartmentOrders.setGbDoDistributerId(gbDistributerGoodsEntity.getGbDgDistributerId());
        System.out.println("disididiididdiiddiid" + gbDistributerGoodsEntity.getGbDgDistributerId());
        gbDepartmentOrders.setGbDoToDepartmentId(gbDistributerGoodsEntity.getGbDgGbDepartmentId());
        gbDepartmentOrders.setGbDoOrderType(gbDistributerGoodsEntity.getGbDgGoodsType());
        gbDepartmentOrders.setGbDoGoodsType(gbDistributerGoodsEntity.getGbDgGoodsType());

        gbDepartmentOrders.setGbDoPurchaseGoodsId(-1);
        gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
        gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
        Integer gbDoDisGoodsFatherId = gbDistributerGoodsEntity.getGbDgDfgGoodsFatherId();
        GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(gbDoDisGoodsFatherId);
        gbDepartmentOrders.setGbDoDisGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        gbDepartmentOrders.setGbDoDisGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());

        Integer gbDfgFathersFatherId = fatherGoodsEntity.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity grandFather = gbDistributerFatherGoodsService.queryObject(gbDfgFathersFatherId);
        Integer greatFatherId = grandFather.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity greatFather = gbDistributerFatherGoodsService.queryObject(greatFatherId);
        gbDepartmentOrders.setGbDoNxGoodsGrandId(grandFather.getGbDfgNxGoodsId());
        gbDepartmentOrders.setGbDoNxGoodsGreatId(greatFather.getGbDfgNxGoodsId());
        gbDepartmentOrders.setGbDoStatus(0);
        gbDepartmentOrders.setGbDoBuyStatus(0);

        System.out.println("ossavmememememmeme" + gbDepartmentOrders.getGbDoDistributerId());
        gbDepartmentOrdersService.update(gbDepartmentOrders);

        //查询是否有采购的同一个商品
        Map<String, Object> map = new HashMap<>();
        map.put("standard", gbDepartmentOrders.getGbDoStandard());
        map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
        GbDistributerPurchaseGoodsEntity gbPurchaseGoodsEntity = new GbDistributerPurchaseGoodsEntity();
        if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null && gbDistributerGoodsEntity.getGbDgGbSupplierId() != -1) {
            map.put("supplierId", gbDistributerGoodsEntity.getGbDgGbSupplierId());
            map.put("status", 1); //有供货商的商品
        } else {
            map.put("equalStatus", 0); //没有供货商的商品
        }
//        if (gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId() != null) {
//            map.put("nxDisId", gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId());
//            map.put("status", 1); //有供货商的商品
//        } else {
//            map.put("equalStatus", 0); //没有供货商的商品
//        }

        System.out.println("putgodosmap" + map);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);

        if (purchaseGoodsEntities.size() == 0) {
            //是个新采购商品
            gbPurchaseGoodsEntity.setGbDpgPurchaseType(gbDepartmentOrders.getGbDoGoodsType());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
            gbPurchaseGoodsEntity.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
            gbPurchaseGoodsEntity.setGbDpgApplyDate(formatWhatDay(0));
            gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(1);
            gbPurchaseGoodsEntity.setGbDpgOrdersFinishAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersWeightAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersBillAmount(0);
            gbPurchaseGoodsEntity.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
            gbPurchaseGoodsEntity.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
            gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbDepartmentOrders.getGbDoQuantity());
            gbPurchaseGoodsEntity.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
            gbPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(gbDistributerGoodsEntity.getGbDgGbDepartmentId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxSupplierId(gbDistributerGoodsEntity.getGbDgGbSupplierId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDistributerGoodsEntity.getGbDgNxDistributerId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsGrandId(gbDistributerGoodsEntity.getGbDgDfgGoodsGrandId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsGreatId(gbDistributerGoodsEntity.getGbDgDfgGoodsGreatId());
            System.out.println("nxididid" + gbDepartmentOrders.getGbDoNxDistributerId());
            gbDistributerPurchaseGoodsService.save(gbPurchaseGoodsEntity);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
        } else {
            //给老采购商品添加新订单
            System.out.println("newownwgopuggog");
            gbPurchaseGoodsEntity = purchaseGoodsEntities.get(0);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
            //采购商品订单数量更新
            Integer gbDpgOrdersAmount = gbPurchaseGoodsEntity.getGbDpgOrdersAmount();
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
            BigDecimal purQuantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
            BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
            BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbPurchaseGoodsEntity.setGbDpgQuantity(add.toString());
            gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);
        }


        Integer gbDoDepartmentFatherId = gbDepartmentOrders.getGbDoDepartmentFatherId();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(gbDoDepartmentFatherId);

        if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null && gbDistributerGoodsEntity.getGbDgGbSupplierId() != -1) {
            System.out.println("mappaapagbDistributerGoodsEntity.getGbDgGbSupplierId()" + gbDistributerGoodsEntity.getGbDgGbSupplierId());

            Map<String, Object> mapData = autoAddPurchaseBatch(gbDepartmentOrders, gbDistributerGoodsEntity);

            Integer purUserId = (Integer) mapData.get("purUserId");
            gbDepartmentOrders.setGbDoPurchaseUserId(purUserId);
            gbDepartmentOrders.setGbDoBuyStatus(getGbOrderBuyStatusProcurement());


            Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDepartmentDisId);

            Integer purchaseGoodsId = gbDepartmentOrders.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);

            Map<String, TemplateData> mapNotice = new HashMap<>();
            mapNotice.put("time2", new TemplateData(formatWhatDayTime(0)));

            mapNotice.put("thing13", new TemplateData(departmentEntity.getGbDepartmentName()));
            mapNotice.put("thing8", new TemplateData(gbDistributerGoodsEntity.getGbDgGoodsName()));
            mapNotice.put("thing10", new TemplateData("订货"));
            Integer gbDoOrderUserId = gbDepartmentOrders.getGbDoOrderUserId();
            GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(gbDoOrderUserId);
            mapNotice.put("thing9", new TemplateData(gbDepartmentUserEntity.getGbDuWxNickName()));
            System.out.println("nociiciiiicicautotootototoototo" + mapNotice);
            NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDistributerGoodsEntity.getGbDgGbSupplierId());
            Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);
            Integer gbDpgBatchId = purchaseGoodsEntity.getGbDpgBatchId();

            StringBuilder pathBuilder = new StringBuilder("subPackage/pages/gbMarket/gbReceiveBatch/gbReceiveBatch");
            pathBuilder.append("?batchId=").append(gbDpgBatchId);
            pathBuilder.append("&retName=").append(gbDistributerEntity.getGbDistributerName());
            pathBuilder.append("&from=notification"); // 添加这个参数
            String path = pathBuilder.toString();
            System.out.println("Encoded URLARRRRRR: " + path);

            WeNoticeService.autoGbSuppliertixingMessageJj(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("supplierId", supplierEntity.getNxJrdhSupplierId());
            mapR.put("gbDisId", gbDepartmentOrders.getGbDoDistributerId());
            mapR.put("nxDisId", gbDistributerGoodsEntity.getGbDgNxDistributerId());
            NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDisGbDisService.queryObjectByParams(mapR);
            if (nxDistributerGbDistributerEntity != null) {
                GbDistributerPurchaseBatchEntity batchEntity = gbDPBService.queryObject(gbDpgBatchId);
                batchEntity.setGbDpbPurchaseType(5);
                batchEntity.setGbDpbNxDistributerId(nxDistributerGbDistributerEntity.getNxDgdNxDistributerId());
                gbDPBService.update(batchEntity);
                transToNxData(gbDistributerGoodsEntity, gbDepartmentOrders, supplierEntity.getNxJrdhsNxDistributerId());

            }

        }


        gbDepartmentOrdersService.update(gbDepartmentOrders);
        GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrders.getGbDepartmentOrdersId());
//        Map<String, Object> mapG = new HashMap<>();
//        mapG.put("disGoodsId", gbDistributerGoodsEntity.getGbDistributerGoodsId());
        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(gbDistributerGoodsEntity.getGbDistributerGoodsId());
//        if(goodsEntity != null){
//            gbDepartmentOrdersEntity.setGbDistributerGoodsEntity(goodsEntity);
//        }


        //
        Map<String, TemplateData> mapNotice = new HashMap<>();
        mapNotice.put("time2", new TemplateData(formatWhatDayTime(0)));
        mapNotice.put("thing13", new TemplateData(departmentEntity.getGbDepartmentName()));
        mapNotice.put("thing8", new TemplateData(gbDistributerGoodsEntity.getGbDgGoodsName()));
        mapNotice.put("thing10", new TemplateData("订货"));
        Integer gbDoOrderUserId = gbDepartmentOrders.getGbDoOrderUserId();
        GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(gbDoOrderUserId);
        mapNotice.put("thing9", new TemplateData(gbDepartmentUserEntity.getGbDuWxNickName()));
        System.out.println("nociiciiiicicautotootototoototo" + mapNotice);

        StringBuilder pathBuilderAll = new StringBuilder("subPackage/pages/gbMarket/gbReceiveBatch/gbReceiveBatch");
        pathBuilderAll.append("?batchId=").append(1);
        pathBuilderAll.append("&retName=").append("abc");
        pathBuilderAll.append("&from=notification"); // 添加这个参数
        String pathall = pathBuilderAll.toString();
        System.out.println("Encoded URLARRRRRR: " + pathall);

        WeNoticeService.autoGbSuppliertixingMessageJj("o85GY5bUj3f1lS5-tK1eFOMb5uZ8", pathall, mapNotice);


        return gbDepartmentOrders;
    }

    private GbDepartmentOrdersEntity saveOneOrder(GbDepartmentOrdersEntity gbDepartmentOrders, GbDistributerGoodsEntity gbDistributerGoodsEntity) {


        gbDepartmentOrders.setGbDoNxGoodsId(gbDistributerGoodsEntity.getGbDgNxGoodsId());
        gbDepartmentOrders.setGbDoNxGoodsFatherId(gbDistributerGoodsEntity.getGbDgNxFatherId());
        gbDepartmentOrders.setGbDoDistributerId(gbDistributerGoodsEntity.getGbDgDistributerId());
        System.out.println("disididiididdiiddiid" + gbDistributerGoodsEntity.getGbDgDistributerId());
        gbDepartmentOrders.setGbDoToDepartmentId(gbDistributerGoodsEntity.getGbDgGbDepartmentId());
        gbDepartmentOrders.setGbDoOrderType(gbDistributerGoodsEntity.getGbDgGoodsType());
        gbDepartmentOrders.setGbDoGoodsType(gbDistributerGoodsEntity.getGbDgGoodsType());


        gbDepartmentOrders.setGbDoPurchaseGoodsId(-1);
        gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
        gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
        Integer gbDoDisGoodsFatherId = gbDistributerGoodsEntity.getGbDgDfgGoodsFatherId();
        GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(gbDoDisGoodsFatherId);
        gbDepartmentOrders.setGbDoDisGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        gbDepartmentOrders.setGbDoDisGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());

        Integer gbDfgFathersFatherId = fatherGoodsEntity.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity grandFather = gbDistributerFatherGoodsService.queryObject(gbDfgFathersFatherId);
        Integer greatFatherId = grandFather.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity greatFather = gbDistributerFatherGoodsService.queryObject(greatFatherId);
        gbDepartmentOrders.setGbDoNxGoodsGrandId(grandFather.getGbDfgNxGoodsId());
        gbDepartmentOrders.setGbDoNxGoodsGreatId(greatFather.getGbDfgNxGoodsId());
        gbDepartmentOrders.setGbDoDisGoodsGreatId(grandFather.getGbDfgFathersFatherId());
        gbDepartmentOrders.setGbDoDsStandardScale("-1");
        gbDepartmentOrders.setGbDoNxDistributerId(-1);
        gbDepartmentOrders.setGbDoNxDistributerGoodsId(-1);
        gbDepartmentOrders.setGbDoPrintStandard(gbDepartmentOrders.getGbDoStandard());

        System.out.println("ossavmememememmeme" + gbDepartmentOrders.getGbDoDistributerId());
        gbDepartmentOrdersService.save(gbDepartmentOrders);

        //查询是否有采购的同一个商品
        Map<String, Object> map = new HashMap<>();
        map.put("standard", gbDepartmentOrders.getGbDoStandard());
        map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
        GbDistributerPurchaseGoodsEntity gbPurchaseGoodsEntity = new GbDistributerPurchaseGoodsEntity();
        if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null && gbDistributerGoodsEntity.getGbDgGbSupplierId() != -1) {
            map.put("supplierId", gbDistributerGoodsEntity.getGbDgGbSupplierId());
            map.put("status", 1); //有供货商的商品
        } else {
            map.put("equalStatus", 0); //没有供货商的商品
        }
        if (gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId() != null && gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId() > 0) {
            map.put("nxDisId", gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId());
            map.put("status", 1); //有供货商的商品
        } else {
            map.put("equalStatus", 0); //没有供货商的商品
        }

        System.out.println("putgodosmap" + map);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);

        if (purchaseGoodsEntities.size() == 0) {
            //是个新采购商品
            gbPurchaseGoodsEntity.setGbDpgPurchaseType(gbDepartmentOrders.getGbDoGoodsType());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
            gbPurchaseGoodsEntity.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
            gbPurchaseGoodsEntity.setGbDpgApplyDate(formatWhatDay(0));
            gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(1);
            gbPurchaseGoodsEntity.setGbDpgOrdersFinishAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersWeightAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersBillAmount(0);
            gbPurchaseGoodsEntity.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
            gbPurchaseGoodsEntity.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
            gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbDepartmentOrders.getGbDoQuantity());
            gbPurchaseGoodsEntity.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
            gbPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(gbDistributerGoodsEntity.getGbDgGbDepartmentId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxSupplierId(gbDistributerGoodsEntity.getGbDgGbSupplierId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDistributerGoodsEntity.getGbDgNxDistributerId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsGrandId(gbDistributerGoodsEntity.getGbDgDfgGoodsGrandId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsGreatId(gbDistributerGoodsEntity.getGbDgDfgGoodsGreatId());
            System.out.println("nxididid" + gbDepartmentOrders.getGbDoNxDistributerId());
            gbDistributerPurchaseGoodsService.save(gbPurchaseGoodsEntity);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
        } else {
            //给老采购商品添加新订单
            System.out.println("newownwgopuggog");
            gbPurchaseGoodsEntity = purchaseGoodsEntities.get(0);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
            //采购商品订单数量更新
            Integer gbDpgOrdersAmount = gbPurchaseGoodsEntity.getGbDpgOrdersAmount();
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
            BigDecimal purQuantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
            BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
            BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbPurchaseGoodsEntity.setGbDpgQuantity(add.toString());
            gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);
        }


        Integer gbDoDepartmentFatherId = gbDepartmentOrders.getGbDoDepartmentFatherId();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(gbDoDepartmentFatherId);

        if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null && gbDistributerGoodsEntity.getGbDgGbSupplierId() != -1) {
            System.out.println("mappaapagbDistributerGoodsEntity.getGbDgGbSupplierId()" + gbDistributerGoodsEntity.getGbDgGbSupplierId());

            Map<String, Object> mapData = autoAddPurchaseBatch(gbDepartmentOrders, gbDistributerGoodsEntity);

            Integer purUserId = (Integer) mapData.get("purUserId");
            gbDepartmentOrders.setGbDoPurchaseUserId(purUserId);
            gbDepartmentOrders.setGbDoBuyStatus(getGbOrderBuyStatusProcurement());


            Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDepartmentDisId);

            Integer purchaseGoodsId = gbDepartmentOrders.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);

            Map<String, TemplateData> mapNotice = new HashMap<>();
            mapNotice.put("time2", new TemplateData(formatWhatDayTime(0)));

            mapNotice.put("thing13", new TemplateData(departmentEntity.getGbDepartmentName()));
            mapNotice.put("thing8", new TemplateData(gbDistributerGoodsEntity.getGbDgGoodsName()));
            mapNotice.put("thing10", new TemplateData("订货"));
            Integer gbDoOrderUserId = gbDepartmentOrders.getGbDoOrderUserId();
            GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(gbDoOrderUserId);
            mapNotice.put("thing9", new TemplateData(gbDepartmentUserEntity.getGbDuWxNickName()));
            System.out.println("nociiciiiicicautotootototoototo" + mapNotice);
            NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDistributerGoodsEntity.getGbDgGbSupplierId());
            Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);
            Integer gbDpgBatchId = purchaseGoodsEntity.getGbDpgBatchId();

            StringBuilder pathBuilder = new StringBuilder("subPackage/pages/gbMarket/gbReceiveBatch/gbReceiveBatch");
            pathBuilder.append("?batchId=").append(gbDpgBatchId);

            pathBuilder.append("&retName=").append(gbDistributerEntity.getGbDistributerName());
            pathBuilder.append("&from=notification"); // 添加这个参数

            String path = pathBuilder.toString();
            System.out.println("Encoded URLARRRRRR: " + path);

            WeNoticeService.autoGbSuppliertixingMessageJj(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("supplierId", supplierEntity.getNxJrdhSupplierId());
            mapR.put("gbDisId", gbDepartmentOrders.getGbDoDistributerId());
            mapR.put("nxDisId", gbDistributerGoodsEntity.getGbDgNxDistributerId());
            NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDisGbDisService.queryObjectByParams(mapR);
            if (nxDistributerGbDistributerEntity != null) {
                GbDistributerPurchaseBatchEntity batchEntity = gbDPBService.queryObject(gbDpgBatchId);
                batchEntity.setGbDpbPurchaseType(5);
                batchEntity.setGbDpbNxDistributerId(nxDistributerGbDistributerEntity.getNxDgdNxDistributerId());
                gbDPBService.update(batchEntity);
                transToNxData(gbDistributerGoodsEntity, gbDepartmentOrders, supplierEntity.getNxJrdhsNxDistributerId());

            }

        }


        gbDepartmentOrdersService.update(gbDepartmentOrders);
        GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrders.getGbDepartmentOrdersId());
        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disGoodsId", gbDistributerGoodsEntity.getGbDistributerGoodsId());
        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryDisGoodsWithDepDisGoods(mapG);
        gbDepartmentOrdersEntity.setGbDistributerGoodsEntity(goodsEntity);


        //下单提醒李沛宜公司销售
//        Map<String, TemplateData> mapNotice = new HashMap<>();
//        mapNotice.put("time2", new TemplateData(formatWhatDayTime(0)));
//
//        mapNotice.put("thing13", new TemplateData(departmentEntity.getGbDepartmentName()));
//        mapNotice.put("thing8", new TemplateData(gbDistributerGoodsEntity.getGbDgGoodsName()));
//        mapNotice.put("thing10", new TemplateData("订货"));
//        Integer gbDoOrderUserId = gbDepartmentOrders.getGbDoOrderUserId();
//        GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(gbDoOrderUserId);
//        mapNotice.put("thing9", new TemplateData(gbDepartmentUserEntity.getGbDuWxNickName()));
//        System.out.println("nociiciiiicicautotootototoototo" + mapNotice);
//
//        StringBuilder pathBuilderAll = new StringBuilder("subPackage/pages/gbMarket/gbReceiveBatch/gbReceiveBatch");
//        pathBuilderAll.append("?batchId=").append(1);
//
//        pathBuilderAll.append("&retName=").append("abc");
//        pathBuilderAll.append("&disId=").append(1);
//        pathBuilderAll.append("&buyUserId=").append(1);
//        pathBuilderAll.append("&fromBuyer=1");
//        pathBuilderAll.append("&depId=").append("1");
//        String pathall = pathBuilderAll.toString();
//        System.out.println("Encoded URLARRRRRR: " + pathall);
//
//        WeNoticeService.autoGbSuppliertixingMessageJj("o85GY5bUj3f1lS5-tK1eFOMb5uZ8", pathall, mapNotice);


        return gbDepartmentOrders;
    }


    private GbDepartmentOrdersEntity aaaTemp(GbDepartmentOrdersEntity order) {

        //1.查询 nxGoods 如果有完全一个的，就下载
        // 1.1 搜索商品名称+规格完全相同
        System.out.println("aaaTempaaaTemp" + order);
        List<NxGoodsEntity> nxGoodsEntities = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name", order.getGbDoGoodsName());
        map.put("level", 3);
        List<NxGoodsEntity> nxGoodsEntitiesEx = nxGoodsService.queryNxGoodsByParams(map);

        List<NxGoodsEntity> nxGoodsEntitiesA = nxAliasService.queryNxGoodsByName(map);

        String pinyinString = order.getGbDoGoodsName();
        for (int i = 0; i < order.getGbDoGoodsName().length(); i++) {
            String str = order.getGbDoGoodsName().substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(order.getGbDoGoodsName());
            }
        }
        Map<String, Object> mapSame = new HashMap<>();
        mapSame.put("level", 3);
        mapSame.put("searchStr", order.getGbDoGoodsName());
        mapSame.put("searchPinyin", pinyinString);
        System.out.println("mapsammdmdmd" + mapSame);
        List<NxGoodsEntity> nxGoodsEntitiesSame = nxGoodsService.queryQuickSearchNxGoods(mapSame);

        TreeSet<NxGoodsEntity> all = new TreeSet();
        all.addAll(nxGoodsEntitiesEx);
        all.addAll(nxGoodsEntitiesA);
        all.addAll(nxGoodsEntitiesSame);


        // 1.2 商品名称相同

        // 如果没有完全一样的，则视为临时
        if (all.size() > 0) {

            // 1. 收集所有要移除的分销商商品 ID
            System.out.println("orderdg" + order.getGbDistributerGoodsEntity());
            if (order.getGbDistributerGoodsEntityList() != null && order.getGbDistributerGoodsEntityList().size() > 0) {
                Set<Integer> dgIds = order.getGbDistributerGoodsEntityList().stream()
                        .map(GbDistributerGoodsEntity::getGbDgNxGoodsId)
                        .collect(Collectors.toSet());
                all.removeIf(goods -> dgIds.contains(goods.getNxGoodsId()));

            }

            order.setNxGoodsEntities(all);
        }

        order.setGbDoStatus(-2);
        order.setGbDoArriveDate(formatWhatDate(0));
        order.setGbDoBuyStatus(getNxDepOrderBuyStatusWithPurchase());
        order.setGbDoApplyDate(formatWhatDay(0));
        order.setGbDoArriveOnlyDate(formatWhatDate(0));
        order.setGbDoArriveWeeksYear(getWeekOfYear(0));
        order.setGbDoArriveDate(formatWhatDay(0));
        order.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        order.setGbDoApplyOnlyTime(formatWhatTime(0));

        order.setGbDoArriveWhatDay(getWeek(0));
        order.setGbDoDsStandardScale("-1");
        order.setGbDoNxDistributerId(-1);
        order.setGbDoNxDistributerGoodsId(-1);
        order.setGbDoPrintStandard(order.getGbDoStandard());

        gbDepartmentOrdersService.justSave(order);

        return order;
    }

    private GbDepartmentOrdersEntity saveTemp(GbDepartmentOrdersEntity order) {


        order.setGbDoStatus(-2);
        order.setGbDoOrderType(0);
        order.setGbDoArriveDate(formatWhatDate(0));
        order.setGbDoBuyStatus(getNxDepOrderBuyStatusWithPurchase());
        order.setGbDoApplyDate(formatWhatDay(0));
        order.setGbDoArriveOnlyDate(formatWhatDate(0));
        order.setGbDoArriveWeeksYear(getWeekOfYear(0));
        order.setGbDoArriveDate(formatWhatDay(0));
        order.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        order.setGbDoApplyOnlyTime(formatWhatTime(0));

        order.setGbDoArriveWhatDay(getWeek(0));
        order.setGbDoDsStandardScale("-1");
        order.setGbDoNxDistributerId(-1);
        order.setGbDoNxDistributerGoodsId(-1);
        order.setGbDoPrintStandard(order.getGbDoStandard());

        gbDepartmentOrdersService.justSave(order);

        return order;
    }


    @RequestMapping(value = "/gbGetAppOrders")
    @ResponseBody
    public R gbGetAppOrders(Integer disId, Integer depId, Integer appDepId, Integer nxDisId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("dayuBuyStatus", -2);
        map4.put("status", 3);
//        map4.put("nxDis", 1);
        System.out.println("map4444444=====nnnnnn" + map4);

        List<NxDistributerEntity> distributerEntities = gbDepartmentOrdersService.queryGbDepNxDistributerOrder(map4);
        if (distributerEntities.size() > 0) {
            for (int i = 0; i < distributerEntities.size(); i++) {
                NxDistributerEntity distributerEntity = distributerEntities.get(i);

                Map<String, Object> map44 = new HashMap<>();
                map44.put("toDepId", appDepId);
                map44.put("buyStatus", 4);
                map44.put("dayuBuyStatus", -2);
                map44.put("status", 3);
//                map44.put("shixianId", nxDisId);
                map44.put("nxDisId", distributerEntity.getNxDistributerId());
                System.out.println("map4444444=====4444" + map44);
                List<GbDistributerFatherGoodsEntity> purchaseToday = gbDistributerPurchaseGoodsService.queryGbFatherDisPurchaseGoods(map44);
                List<GbDistributerPurchaseGoodsEntity> result = new ArrayList<>();
                if (purchaseToday.size() > 0) {
                    for (GbDistributerFatherGoodsEntity fatherGoodsEntity : purchaseToday) {
                        System.out.println("levelel00000---" + fatherGoodsEntity.getGbDfgFatherGoodsSort());
                        if (fatherGoodsEntity.getFatherGoodsEntities().size() > 0) {
                            for (GbDistributerFatherGoodsEntity fatherGoodsEntity1 : fatherGoodsEntity.getFatherGoodsEntities()) {
                                System.out.println("levelel00000---" + fatherGoodsEntity1.getGbDfgFatherGoodsSort());
                                List<GbDistributerPurchaseGoodsEntity> gbDistributerGoodsEntities = fatherGoodsEntity1.getGbDistributerPurchaseGoodsEntities();
                                result.addAll(gbDistributerGoodsEntities);
                            }
                        }
                    }
                }

                distributerEntity.setGbDistributerPurchaseGoodsEntities(result);
            }
        }


        Map<String, Object> map1 = new HashMap<>();
        map1.put("purDepId", depId);
        map1.put("purStatus", 1);
        map1.put("purchaseType", 2);
        System.out.println("fafdaafmap1map1map1" + map1);
        int count0 = 0;
        int purchaseGoodsAmount = gbDistributerPurchaseGoodsService.queryPurchaseGoodsAmount(map1);
        if (purchaseGoodsAmount > 0) {
            count0 = gbDistributerPurchaseGoodsService.queryGbPurchaseOrderAmount(map1);
        }

        map1.put("purStatus", null);
        map1.put("purGoodsEqualStatus", 1);
        System.out.println("fafdaafmap1map1map1wxwxwx" + map1);
        int purchaseGoodsAmountOne = gbDistributerPurchaseGoodsService.queryPurchaseGoodsAmount(map1);
        Integer count1 = 0;
        if (purchaseGoodsAmountOne > 0) {
            count1 = gbDistributerPurchaseGoodsService.queryGbPurchaseOrderAmount(map1);
        }

        map1.put("status", 3);
        map1.put("toDepId", appDepId);
        map1.put("orderType", 5);
        System.out.println("fafdaafmap1map1mapappppppppporderType" + map1);
        int count2 = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map1);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", distributerEntities);
        map3.put("orderAmount", count0);
        map3.put("wxAmount", count1);
        map3.put("appAmount", count2);
        map3.put("disInfo", gbDistributerService.queryDistributerInfo(disId));
        return R.ok().put("data", map3);
    }


    @RequestMapping(value = "/getJrdhGoodsOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getJrdhGoodsOrders(Integer goodsId, String startDate, String stopDate, String searchDepIds) {

        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("goodsId", goodsId);
        mapDep.put("startDate", startDate);
        mapDep.put("stopDate", stopDate);
        mapDep.put("notOutOrder", 1);
        mapDep.put("equalBuyStatus", 5);
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            List<String> idsGb = new ArrayList<>();
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    mapDep.put("depFatherIds", idsGb);
                }
            }
        }
        System.out.println("mdafdafa " + mapDep);

        List<NxJrdhSupplierEntity> supplierEntities = gbDepartmentOrdersService.querySupplierByOrdersParams(mapDep);
        return R.ok().put("data", supplierEntities);
    }


    @RequestMapping(value = "/getEveryGoodsJrdh", method = RequestMethod.POST)
    @ResponseBody
    public R getEveryGoodsJrdh(Integer goodsFatherId, String startDate, String stopDate, String searchDepIds) {

        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("goodsFatherId", goodsFatherId);
        mapDep.put("startDate", startDate);
        mapDep.put("stopDate", stopDate);
        mapDep.put("notOutOrder", 1);
        mapDep.put("equalBuyStatus", 5);
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            List<String> idsGb = new ArrayList<>();
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    mapDep.put("depFatherIds", idsGb);
                }
            }
        }

        List<GbDistributerGoodsEntity> goodsEntities = gbDepartmentOrdersService.disGetTodayGoodsOrder(mapDep);
        if (goodsEntities.size() > 0) {
            for (GbDistributerGoodsEntity goodsEntity : goodsEntities) {
                mapDep.put("goodsId", goodsEntity.getGbDistributerGoodsId());
                Double add = gbDepartmentOrdersService.queryGbOrdersSubtotal(mapDep);
                goodsEntity.setGoodsCostTotal(new BigDecimal(add).setScale(1, BigDecimal.ROUND_HALF_UP));
            }
        }

        return R.ok().put("data", goodsEntities);
    }


    @RequestMapping(value = "/disGetJrdhBills", method = RequestMethod.POST)
    @ResponseBody
    public R disGetJrdhBills(Integer disId, String startDate, String stopDate, String searchDepIds, String searchDepId) {

        System.out.println("costutuutititit");
        Map<String, Object> mapCost = new HashMap<>();
        double aDoutble = 0.0;
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("equalBuyStatus", 5);
        if (!searchDepId.equals("-1")) {
            mapDep.put("depFatherId", searchDepId);
        } else {
            if (!searchDepIds.equals("-1")) {
                String[] arrGb = searchDepIds.split(",");
                List<String> idsGb = new ArrayList<>();
                for (String idGb : arrGb) {
                    idsGb.add(idGb);
                    if (idsGb.size() > 0) {
                        mapDep.put("depFatherIds", idsGb);
                    }
                }
            }
        }


        if (howManyDaysInPeriod > 0) {
            mapDep.put("startDate", startDate);
            mapDep.put("stopDate", stopDate);
        } else {
            mapDep.put("date", startDate);
        }

        mapDep.put("notOutOrder", 1);
        System.out.println("mapdeeepee" + mapDep);
        Integer count = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(mapDep);
        if (count > 0) {
            aDoutble = gbDepartmentOrdersService.queryGbOrdersSubtotal(mapDep);
            System.out.println("abcccccc" + mapDep);
            List<GbDistributerFatherGoodsEntity> greatGrandGoods = gbDepartmentOrdersService.queryGreatGrandForJrdh(mapDep);
            for (GbDistributerFatherGoodsEntity great : greatGrandGoods) {
                System.out.println("granfa" + great.getGbDfgFatherGoodsName());
                double greatTotal = 0.0;
                List<GbDistributerFatherGoodsEntity> grandGoods = great.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grand : grandGoods) {
                    double grandTotal = 0.0;
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        mapDep.put("goodsFatherId", father.getGbDistributerFatherGoodsId());
                        Double add = gbDepartmentOrdersService.queryGbOrdersSubtotal(mapDep);
                        father.setFatherStockTotal(add);
                        father.setFatherStockTotalString(new BigDecimal(add).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        grandTotal = grandTotal + add;
                        greatTotal = greatTotal + add;
                    }
                    grand.setFatherStockTotal(grandTotal);
                    grand.setFatherStockTotalString(new BigDecimal(grandTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                }
                great.setFatherStockTotal(greatTotal);
                great.setFatherStockTotalString(new BigDecimal(greatTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
            mapCost.put("arr", greatGrandGoods);
            mapCost.put("code", "0");
            mapCost.put("total", String.format("%.1f", aDoutble));

        } else {
            mapCost.put("total", "0.0");
        }
        List<GbDepartmentEntity> deplist = new ArrayList<>();
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");

            for (String depId : arrGb) {
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(depId));
                mapDep.put("depFatherId", depId);
                mapDep.put("depFatherIds", null);
                mapDep.put("disGoodsFatherId", null);
                System.out.println("dadkafja;fjalfa;slf;alsf;as" + mapDep);
                Integer integer = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(mapDep);

//                    Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(mapDep);
                double aDoutbleDep = 0.0;
                if (integer > 0) {
                    aDoutbleDep = gbDepartmentOrdersService.queryGbOrdersSubtotal(mapDep);
                }
                departmentEntity.setDepCostGoodsTotalString(new BigDecimal(aDoutbleDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                deplist.add(departmentEntity);
            }
        }

        mapCost.put("depArr", deplist);
        return R.ok().put("data", mapCost);

    }

    @RequestMapping(value = "/delBillOrder/{id}")
    @ResponseBody
    public R delBillOrder(@PathVariable Integer id) {

        GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(id);
        Integer gbDoBillId = gbDepartmentOrdersEntity.getGbDoBillId();

        Map<String, Object> map = new HashMap<>();
        map.put("billId", gbDoBillId);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        if (ordersEntities.size() > 1) {
            GbDepartmentBillEntity gbDepartmentBillEntity = gbDepartmentBillService.queryObject(gbDoBillId);
            BigDecimal decimal = new BigDecimal(gbDepartmentBillEntity.getGbDbTotal());
            BigDecimal decimal1 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoSubtotal());
            BigDecimal decimal2 = decimal.subtract(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbDepartmentBillEntity.setGbDbTotal(decimal2.toString());
            gbDepartmentBillEntity.setGbDbOrderAmount(gbDepartmentBillEntity.getGbDbOrderAmount() - 1);
            gbDepartmentBillService.update(gbDepartmentBillEntity);
        } else {
            gbDepartmentBillService.delete(gbDoBillId);
        }

        if (gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId() != null && gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId() > 0) {
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId());
            purchaseGoodsEntity.setGbDpgOrdersBillAmount(purchaseGoodsEntity.getGbDpgOrdersBillAmount() - 1);


            gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
        }

        gbDepartmentOrdersEntity.setGbDoBillId(null);
        gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusHasFinished());
        gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        return R.ok();
    }


    @RequestMapping(value = "/getReturnOrderPurDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getReturnOrderPurDetail(@RequestBody GbDepartmentOrdersEntity ordersEntity) {
        Integer gbDoDgsrReturnId = ordersEntity.getGbDoDgsrReturnId();
        GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepGoodsStockReduceService.queryObject(gbDoDgsrReturnId);

        Integer gbDgsrGbGoodsStockId = reduceEntity.getGbDgsrGbGoodsStockId();
        GbDepartmentGoodsStockEntity stockEntity = gbDepartmentGoodsStockService.queryObject(gbDgsrGbGoodsStockId);
        Integer gbDgsGbPurGoodsId = stockEntity.getGbDgsGbPurGoodsId();

        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDgsGbPurGoodsId);

        Integer gbDpgBatchId = purchaseGoodsEntity.getGbDpgBatchId();
        GbDistributerPurchaseBatchEntity batchEntity = gbDPBService.queryBatchWithOrders(gbDpgBatchId);
        System.out.println("abakckckkckkkckc" + batchEntity);
//        purchaseGoodsEntity.setGbDisPurchaseBatchEntity(batchEntity);

//        ordersEntity.setReturnPurGoodsEntity(purchaseGoodsEntity);
        return R.ok().put("data", ordersEntity);
    }


    @RequestMapping(value = "/peisongDepGetGbOrders", method = RequestMethod.POST)
    @ResponseBody
    public R peisongDepGetGbOrders(Integer gbDisId, Integer nxDisGoodsId, String startDate, String stopDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", gbDisId);
        map.put("nxDisGoodsId", nxDisGoodsId);
        if (!startDate.equals("-1")) {
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
        }
        System.out.println("mappa" + map);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryPeisongOrdersByParams(map);

        return R.ok().put("data", ordersEntities);
    }


    @RequestMapping(value = "/depGetWeightDepDisGoods/{depId}")
    @ResponseBody
    public R depGetWeightDepDisGoods(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderDepId", depId);
        map.put("equalBuyStatus", 1);
        map.put("status", 3);
        List<GbDistributerGoodsShelfEntity> disGoodsEntities = gbDepartmentOrdersService.queryWeightGoodsByParams(map);

        return R.ok().put("data", disGoodsEntities);
    }


    @RequestMapping(value = "/getDeliveryOrders")
    @ResponseBody
    public R getDeliveryOrders(Integer toDepId, Integer orderType, Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", toDepId);
        map.put("equalStatus", 2);
        map.put("orderType", orderType);

        System.out.println("mapappaapap" + map);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryDistributerTodayDepartments(map);
        Map<String, Object> todayData = packageDisOrderByDep(departmentEntities, 0);
        List<GbDepartmentEntity> arr = (List<GbDepartmentEntity>) todayData.get("arr");

        Map<String, Object> map11 = new HashMap<>();
        map11.put("purDepId", toDepId);
        map11.put("status", 1);
        int count0 = gbDistributerPurchaseGoodsService.queryPurchaseGoodsAmount(map11);

        Map<String, Object> map12 = new HashMap<>();
        map12.put("purUserId", userId);
        map12.put("status", 2);
        map12.put("purchaseType", 2);
        Integer count1 = gbDPBService.queryDisPurchaseBatchCount(map12);

        Map<String, Object> map13 = new HashMap<>();
        map13.put("purUserId", userId);
        map13.put("equalStatus", 1);
        map13.put("batchId", -1);
        map13.put("weightId", -1);
        Integer count2 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map13);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("arr", todayData);
        map3.put("purGoodsAmount", count0);
        map3.put("haoyouAmount", count1);
        map3.put("finishAmount", count2);
        map3.put("deliveryAmount", arr.size());

        return R.ok().put("data", map3);
    }


    @RequestMapping(value = "/stockReceivePurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R stockReceivePurGoods(@RequestBody GbDepartmentOrdersEntity order) {
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Integer gbDoDepDisGoodsId = order.getGbDoDepDisGoodsId();
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        Integer gbDoStatus = ordersEntity.getGbDoStatus();
        //判断没有被别人收货
        if (gbDoStatus.equals(getGbOrderStatusHasBill())) {
            //0,修改订单上次价格涨幅
            if (departmentDisGoodsEntity.getGbDdgOrderDate() != null && !departmentDisGoodsEntity.getGbDdgOrderDate().trim().isEmpty()) {
                if (order.getGbDoPrice() != null && !order.getGbDoPrice().trim().isEmpty()) {
                    BigDecimal decimal = new BigDecimal(departmentDisGoodsEntity.getGbDdgOrderPrice());
                    BigDecimal decimal1 = new BigDecimal(order.getGbDoPrice());
                    BigDecimal subtract1 = decimal1.subtract(decimal);
                    order.setGbDoPriceDifferent(subtract1.toString());
                } else {
                    order.setGbDoPriceDifferent("0");
                }
            }


            GbDepartmentGoodsStockEntity stockEntity = new GbDepartmentGoodsStockEntity();
            stockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
            stockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
            stockEntity.setGbDgsGbPurGoodsId(order.getGbDoPurchaseGoodsId());
            stockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
            stockEntity.setGbDgsWeight(order.getGbDoWeight());
            stockEntity.setGbDgsPrice(order.getGbDoPrice());
            stockEntity.setGbDgsSubtotal(order.getGbDoSubtotal());
            stockEntity.setGbDgsRestWeight(order.getGbDoWeight());
            stockEntity.setGbDgsRestSubtotal(order.getGbDoSubtotal());
            stockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
            stockEntity.setGbDgsGbDisGoodsFatherId(order.getGbDoDisGoodsFatherId());
            stockEntity.setGbDgsGbDisGoodsGrandId(order.getGbDoDisGoodsGrandId());
            stockEntity.setGbDgsGbDisGoodsGreatId(order.getGbDoDisGoodsGreatId());
            stockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
            stockEntity.setGbDgsDate(formatWhatDay(0));
            stockEntity.setGbDgsTimeStamp(getTimeStamp());
            stockEntity.setGbDgsWeek(getWeek(0));
            stockEntity.setGbDgsMonth(formatWhatMonth(0));
            stockEntity.setGbDgsYear(formatWhatYear(0));
            stockEntity.setGbDgsFullTime(formatFullTime());
            stockEntity.setGbDgsLossWeight("0");
            stockEntity.setGbDgsLossSubtotal("0");
            stockEntity.setGbDgsReturnWeight("0");
            stockEntity.setGbDgsReturnSubtotal("0");
            stockEntity.setGbDgsProduceWeight("0");
            stockEntity.setGbDgsProduceSubtotal("0");
            stockEntity.setGbDgsWasteWeight("0");
            stockEntity.setGbDgsWasteSubtotal("0");
            String gbDdgSellingPrice = departmentDisGoodsEntity.getGbDdgSellingPrice();
            if (gbDdgSellingPrice != null && new BigDecimal(gbDdgSellingPrice).compareTo(new BigDecimal(0)) == 1) {
                stockEntity.setGbDgsAfterProfitSubtotal("0");
                stockEntity.setGbDgsBetweenPrice("0");
                stockEntity.setGbDgsCostRate("0");
                stockEntity.setGbDgsSellingSubtotal("0");
                stockEntity.setGbDgsProduceSellingSubtotal("0");
                stockEntity.setGbDgsProfitSubtotal("0");
                stockEntity.setGbDgsProfitWeight("0");
                stockEntity.setGbDgsSellingPrice(gbDdgSellingPrice);
            } else {
                stockEntity.setGbDgsSellingPrice("-1");
            }

            // showStandard
            if (departmentDisGoodsEntity.getGbDdgShowStandardId() != -1) {
                String gbDdgShowStandardScale = departmentDisGoodsEntity.getGbDdgShowStandardScale();
                BigDecimal divide = new BigDecimal(order.getGbDoWeight()).divide(new BigDecimal(gbDdgShowStandardScale), 1, BigDecimal.ROUND_HALF_UP);
                stockEntity.setGbDgsRestWeightShowStandard(divide.toString());
                stockEntity.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
            }

            //判断是否有保鲜时间参数
            if (order.getGbDoPurchaseGoodsId() != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(order.getGbDoPurchaseGoodsId());
                if (purchaseGoodsEntity.getGbDpgWasteFullTime() != null && !purchaseGoodsEntity.getGbDpgWasteFullTime().trim().isEmpty()) {
                    stockEntity.setGbDgsWasteFullTime(purchaseGoodsEntity.getGbDpgWasteFullTime());
                    String gbDpgWasteFullTime = purchaseGoodsEntity.getGbDpgWasteFullTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    // 设置日期字符串
                    // 解析日期字符串为Date对象
                    Date dateWaste = null;
                    Date dateWarn = null;
                    try {
                        if (gbDpgWasteFullTime != null && !gbDpgWasteFullTime.trim().isEmpty()) {
                            dateWaste = dateFormat.parse(gbDpgWasteFullTime);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // 获取时间戳
                    long timestampWaste = 0;
                    long timestampWarn = 0;
                    if (dateWaste != null) {
                        timestampWaste = dateWaste.getTime();
                    }
                    if (dateWarn != null) {
                        timestampWarn = dateWarn.getTime();
                    }
                    // 输出时间戳
                    stockEntity.setGbDgsWasteTimeQuantumName(String.valueOf(timestampWaste));
                }
                //判断是否价格异常商品
                if (purchaseGoodsEntity.getGbDpgDisGoodsPriceId() != null && purchaseGoodsEntity.getGbDpgDisGoodsPriceId() > 0) {
                    GbDistributerGoodsPriceEntity goodsPriceEntity = goodsPriceService.queryObject(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                    String doWeight = order.getGbDoWeight();
                    Integer gbDgpPurWhat = goodsPriceEntity.getGbDgpPurWhat();
                    String whatSubtotal = "";
                    if (gbDgpPurWhat == 1) {
                        String gbDgpGoodsHighestPrice = goodsPriceEntity.getGbDgpGoodsHighestPrice();
                        String purPrice = goodsPriceEntity.getGbDgpPurPrice();
                        BigDecimal diffPrice = new BigDecimal(purPrice).subtract(new BigDecimal(gbDgpGoodsHighestPrice));
                        BigDecimal subtotal = diffPrice.multiply(new BigDecimal(doWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                        whatSubtotal = subtotal.toString();
                    }
                    if (gbDgpPurWhat == -1) {
                        String lowestPrice = goodsPriceEntity.getGbDgpGoodsLowestPrice();
                        String purPrice = goodsPriceEntity.getGbDgpPurPrice();
                        BigDecimal diffPrice = new BigDecimal(purPrice).subtract(new BigDecimal(lowestPrice));
                        BigDecimal subtotal = diffPrice.multiply(new BigDecimal(doWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                        whatSubtotal = subtotal.toString();
                    }

                    //价控最低价的成本
                    //实际成本与最低成本的差价
                    stockEntity.setGbDgsGbPriceSubtotal(whatSubtotal); // 相差了多少成本
                    stockEntity.setGbDgsGbPriceGoodsId(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                    stockEntity.setGbDgsGbPriceSubtotalScale(goodsPriceEntity.getGbDgpPurScale());
                }

            }


            stockEntity.setGbDgsStatus(0);
            stockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
            stockEntity.setGbDgsGbGoodsStockId(-1);
            stockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
            stockEntity.setGbDgsNxDistributerId(order.getGbDoNxDistributerId());
            stockEntity.setGbDgsReceiveUserId(order.getGbDoReceiveUserId());
            stockEntity.setGbDgsInventoryDate(formatWhatDay(0));
            stockEntity.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
            stockEntity.setGbDgsInventoryMonth(formatWhatMonth(0));
            stockEntity.setGbDgsInventoryYear(formatWhatYear(0));
            stockEntity.setGbDgsStars(5);
            gbDepartmentGoodsStockService.save(stockEntity);


            orderAddDepDisGoods(order, stockEntity, gbDoDepDisGoodsId);
            updateDepGoodsDailyBusiness(stockEntity);

            //2，修改订单状态
            order.setGbDoStatus(getGbOrderStatusReceived());
            gbDepartmentOrdersService.update(order);

            //3，修改送货单收货单子数量
            if (order.getGbDoPurchaseGoodsId() != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(order.getGbDoPurchaseGoodsId());
                BigDecimal gbPgOrderAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersAmount());
                BigDecimal gbDbFinishAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersFinishAmount());
                if (gbDbFinishAmount.add(new BigDecimal(1)).compareTo(gbPgOrderAmount) == 0) {
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(purchaseGoodsEntity.getGbDpgOrdersAmount());
                    purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusWaitReceive());
                } else {
                    BigDecimal add = gbDbFinishAmount.add(new BigDecimal(1));
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(add.intValue());
                }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }

            //3，修改送货单收货单子数量
            Integer gbDoBillId = order.getGbDoBillId();
            GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryObject(gbDoBillId);
            BigDecimal gbDbOrderAmount = new BigDecimal(billEntity.getGbDbOrderAmount());
            if (gbDbOrderAmount.compareTo(new BigDecimal("1")) == 1) {
                BigDecimal subtract = gbDbOrderAmount.subtract(new BigDecimal("1"));
                billEntity.setGbDbOrderAmount(subtract.intValue());
                gbDepartmentBillService.update(billEntity);
            } else {
                billEntity.setGbDbOrderAmount(0);
                billEntity.setGbDbStatus(0);
                gbDepartmentBillService.update(billEntity);
            }
            return R.ok();
        } else {
            return R.error(-1, "已经完成收货");
        }

    }

    /**
     * 订货部门收货
     *
     * @param order
     * @return
     */
    @RequestMapping(value = "/departmentReceiveOutStock", method = RequestMethod.POST)
    @ResponseBody
    public R departmentReceiveOutStock(@RequestBody GbDepartmentOrdersEntity order) {
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Integer gbDoDepDisGoodsId = order.getGbDoDepDisGoodsId();
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        Integer gbDoStatus = ordersEntity.getGbDoStatus();
        //判断没有被别人收货
        if (gbDoStatus.equals(getGbOrderStatusHasBill())) {
            //1,修改库存数据
            if (order.getGbDoOrderType().equals(getGbOrderTypeChuKu()) || order.getGbDoOrderType().equals(getGbOrderTypeKitchen())) {
                List<GbDepartmentGoodsStockEntity> goodsStockEntityList = order.getGoodsStockEntityList();
                for (GbDepartmentGoodsStockEntity stock : goodsStockEntityList) {

                    //判断是否价格异常商品
                    if (stock.getGbDgsGbPriceGoodsId() != null && stock.getGbDgsGbPriceGoodsId() > 0) {
                        GbDistributerGoodsPriceEntity goodsPriceEntity = goodsPriceService.queryObject(stock.getGbDgsGbPriceGoodsId());
                        String subtotal = stock.getGbDgsSubtotal();
                        BigDecimal whatSubtotal = new BigDecimal(subtotal).multiply(new BigDecimal(goodsPriceEntity.getGbDgpPurScale()));
                        stock.setGbDgsGbPriceSubtotal(whatSubtotal.toString());
                        stock.setGbDgsGbPriceGoodsId(goodsPriceEntity.getGbDistributerGoodsPriceId());
                        stock.setGbDgsGbPriceSubtotalScale(goodsPriceEntity.getGbDgpPurScale());
                    }
                    stock.setGbDgsFullTime(formatFullTime());
                    stock.setGbDgsDate(formatWhatDay(0));
                    stock.setGbDgsTimeStamp(getTimeStamp());
                    stock.setGbDgsWeek(getWeek(0));
                    stock.setGbDgsMonth(formatWhatMonth(0));
                    stock.setGbDgsYear(formatWhatYear(0));
                    stock.setGbDgsInventoryFullTime(formatFullTime());
                    stock.setGbDgsInventoryDate(formatWhatDay(0));
                    stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
                    stock.setGbDgsInventoryMonth(formatWhatMonth(0));
                    stock.setGbDgsInventoryYear(formatWhatYear(0));
                    stock.setGbDgsStatus(0);
                    stock.setGbDgsStars(5);

                    // showStandard
                    if (departmentDisGoodsEntity.getGbDdgShowStandardId() != -1) {
                        String gbDdgShowStandardScale = departmentDisGoodsEntity.getGbDdgShowStandardScale();
                        BigDecimal divide = new BigDecimal(order.getGbDoWeight()).divide(new BigDecimal(gbDdgShowStandardScale), 1, BigDecimal.ROUND_HALF_UP);
                        stock.setGbDgsRestWeightShowStandard(divide.toString());
                        stock.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
                    }


                    gbDepartmentGoodsStockService.update(stock);

                    //depDisGoods
                    orderAddDepDisGoods(order, stock, gbDoDepDisGoodsId);

                    //add outStockProdeuce
                    Integer gbDoDisGoodsId = order.getGbDoDisGoodsId();
                    GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
                    if (gbDistributerGoodsEntity.getGbDgIsSelfControl() == 0) {
                        System.out.println("adddkkdkkdkdkdkdkkdkkdkdkdk");
                        addNewStockReduce(stock);
                        subtractOutGoodsDailyBusiness(stock);
                    }
                    // add departmentGoodsDaily
                    updateDepGoodsDailyBusiness(stock);
                }
            } else {
                //0,修改订单上次价格涨幅
                String gbDdgOrderDate = departmentDisGoodsEntity.getGbDdgOrderDate();

                if (gbDdgOrderDate != null && !gbDdgOrderDate.trim().isEmpty()
                        && order.getGbDoPrice() != null && !order.getGbDoPrice().trim().isEmpty()) {
                    BigDecimal decimal = new BigDecimal(departmentDisGoodsEntity.getGbDdgOrderPrice());
                    BigDecimal decimal1 = new BigDecimal(order.getGbDoPrice());
                    BigDecimal subtract1 = decimal1.subtract(decimal);
                    order.setGbDoPriceDifferent(subtract1.toString());
                } else {
                    order.setGbDoPriceDifferent("0");
                }

                GbDepartmentGoodsStockEntity stockEntity = new GbDepartmentGoodsStockEntity();
                stockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
                stockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
                stockEntity.setGbDgsGbPurGoodsId(order.getGbDoPurchaseGoodsId());
                stockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
                stockEntity.setGbDgsWeight(order.getGbDoWeight());
                stockEntity.setGbDgsPrice(order.getGbDoPrice());
                stockEntity.setGbDgsSubtotal(order.getGbDoSubtotal());
                stockEntity.setGbDgsRestWeight(order.getGbDoWeight());
                stockEntity.setGbDgsRestSubtotal(order.getGbDoSubtotal());
                stockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
                stockEntity.setGbDgsGbDisGoodsFatherId(order.getGbDoDisGoodsFatherId());
                stockEntity.setGbDgsGbDisGoodsGrandId(order.getGbDoDisGoodsGrandId());
                stockEntity.setGbDgsGbDisGoodsGreatId(order.getGbDoDisGoodsGreatId());
                stockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
                stockEntity.setGbDgsDate(formatWhatDay(0));
                stockEntity.setGbDgsTimeStamp(getTimeStamp());
                stockEntity.setGbDgsWeek(getWeek(0));
                stockEntity.setGbDgsMonth(formatWhatMonth(0));
                stockEntity.setGbDgsYear(formatWhatYear(0));
                stockEntity.setGbDgsFullTime(formatFullTime());
                stockEntity.setGbDgsLossWeight("0");
                stockEntity.setGbDgsLossSubtotal("0");
                stockEntity.setGbDgsReturnWeight("0");
                stockEntity.setGbDgsReturnSubtotal("0");
                stockEntity.setGbDgsProduceWeight("0");
                stockEntity.setGbDgsProduceSubtotal("0");
                stockEntity.setGbDgsWasteWeight("0");
                stockEntity.setGbDgsWasteSubtotal("0");
                String gbDdgSellingPrice = departmentDisGoodsEntity.getGbDdgSellingPrice();
                if (gbDdgSellingPrice != null && new BigDecimal(gbDdgSellingPrice).compareTo(new BigDecimal(0)) == 1) {
                    stockEntity.setGbDgsAfterProfitSubtotal("0");
                    stockEntity.setGbDgsBetweenPrice("0");
                    stockEntity.setGbDgsCostRate("0");
                    stockEntity.setGbDgsSellingSubtotal("0");
                    stockEntity.setGbDgsProduceSellingSubtotal("0");
                    stockEntity.setGbDgsProfitSubtotal("0");
                    stockEntity.setGbDgsProfitWeight("0");
                    stockEntity.setGbDgsSellingPrice(gbDdgSellingPrice);
                } else {
                    stockEntity.setGbDgsSellingPrice("-1");
                }

                // showStandard
                if (departmentDisGoodsEntity.getGbDdgShowStandardId() != -1) {
                    String gbDdgShowStandardScale = departmentDisGoodsEntity.getGbDdgShowStandardScale();
                    BigDecimal divide = new BigDecimal(order.getGbDoWeight()).divide(new BigDecimal(gbDdgShowStandardScale), 1, BigDecimal.ROUND_HALF_UP);
                    stockEntity.setGbDgsRestWeightShowStandard(divide.toString());
                    stockEntity.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
                }

                //判断是否有保鲜时间参数
                if (order.getGbDoPurchaseGoodsId() != -1) {
                    GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(order.getGbDoPurchaseGoodsId());
                    if (purchaseGoodsEntity.getGbDpgWasteFullTime() != null && !purchaseGoodsEntity.getGbDpgWasteFullTime().trim().isEmpty()) {
                        stockEntity.setGbDgsWasteFullTime(purchaseGoodsEntity.getGbDpgWasteFullTime());

                        String gbDpgWasteFullTime = purchaseGoodsEntity.getGbDpgWasteFullTime();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        // 设置日期字符串
                        // 解析日期字符串为Date对象
                        Date dateWaste = null;
                        Date dateWarn = null;
                        try {
                            if (gbDpgWasteFullTime != null && !gbDpgWasteFullTime.trim().isEmpty()) {
                                dateWaste = dateFormat.parse(gbDpgWasteFullTime);
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        // 获取时间戳
                        long timestampWaste = 0;
                        long timestampWarn = 0;
                        if (dateWaste != null) {
                            timestampWaste = dateWaste.getTime();
                        }
                        if (dateWarn != null) {
                            timestampWarn = dateWarn.getTime();
                        }
                        // 输出时间戳
                        System.out.println("zhelieiieieiieieiieeiie" + dateWaste + "abcc" + timestampWaste);
                        stockEntity.setGbDgsWasteTimeQuantumName(String.valueOf(timestampWaste));

                    }
                    //判断是否价格异常商品
                    if (purchaseGoodsEntity.getGbDpgDisGoodsPriceId() != null && purchaseGoodsEntity.getGbDpgDisGoodsPriceId() > 0) {
                        GbDistributerGoodsPriceEntity goodsPriceEntity = goodsPriceService.queryObject(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                        String doWeight = order.getGbDoWeight();
                        Integer gbDgpPurWhat = goodsPriceEntity.getGbDgpPurWhat();
                        String whatSubtotal = "";
                        if (gbDgpPurWhat == 1) {
                            String gbDgpGoodsHighestPrice = goodsPriceEntity.getGbDgpGoodsHighestPrice();
                            String purPrice = goodsPriceEntity.getGbDgpPurPrice();
                            BigDecimal diffPrice = new BigDecimal(purPrice).subtract(new BigDecimal(gbDgpGoodsHighestPrice));
                            BigDecimal subtotal = diffPrice.multiply(new BigDecimal(doWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                            whatSubtotal = subtotal.toString();
                        }
                        if (gbDgpPurWhat == -1) {
                            String lowestPrice = goodsPriceEntity.getGbDgpGoodsLowestPrice();
                            String purPrice = goodsPriceEntity.getGbDgpPurPrice();
                            BigDecimal diffPrice = new BigDecimal(purPrice).subtract(new BigDecimal(lowestPrice));
                            BigDecimal subtotal = diffPrice.multiply(new BigDecimal(doWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                            whatSubtotal = subtotal.toString();
                        }

                        //价控最低价的成本
                        //实际成本与最低成本的差价
                        stockEntity.setGbDgsGbPriceSubtotal(whatSubtotal); // 相差了多少成本
                        stockEntity.setGbDgsGbPriceGoodsId(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                        stockEntity.setGbDgsGbPriceSubtotalScale(goodsPriceEntity.getGbDgpPurScale());
                    }

                }


                stockEntity.setGbDgsStatus(0);
                stockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
                stockEntity.setGbDgsGbGoodsStockId(-1);
                stockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
                stockEntity.setGbDgsNxDistributerId(order.getGbDoNxDistributerId());
                stockEntity.setGbDgsReceiveUserId(order.getGbDoReceiveUserId());
                stockEntity.setGbDgsInventoryDate(formatWhatDay(0));
                stockEntity.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
                stockEntity.setGbDgsInventoryMonth(formatWhatMonth(0));
                stockEntity.setGbDgsInventoryYear(formatWhatYear(0));
                stockEntity.setGbDgsStars(5);
                gbDepartmentGoodsStockService.save(stockEntity);

                //库房收货不添加DepDaily
//                if (order.getGbDoOrderType().equals(getGbOrderTypeJiCai())
//                        || order.getGbDoOrderType().equals(getGbOrderTypeZiCai())
//                        || order.getGbDoOrderType().equals(getGbOrderTypeAppSupplier())) {
//
//                }
                updateDepGoodsDailyBusiness(stockEntity);

                orderAddDepDisGoods(order, stockEntity, gbDoDepDisGoodsId);

            }

            //2，修改订单状态
            order.setGbDoStatus(getGbOrderStatusReceived());
            gbDepartmentOrdersService.update(order);

            //3，修改送货单收货单子数量
            if (order.getGbDoPurchaseGoodsId() != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(order.getGbDoPurchaseGoodsId());
                BigDecimal gbPgOrderAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersAmount());
                BigDecimal gbDbFinishAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersFinishAmount());
                if (gbDbFinishAmount.add(new BigDecimal(1)).compareTo(gbPgOrderAmount) == 0) {
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(purchaseGoodsEntity.getGbDpgOrdersAmount());
                    purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusWaitReceive());
                } else {
                    BigDecimal add = gbDbFinishAmount.add(new BigDecimal(1));
                    purchaseGoodsEntity.setGbDpgOrdersFinishAmount(add.intValue());
                }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }

            //3，修改送货单收货单子数量
            Integer gbDoBillId = order.getGbDoBillId();
            GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryObject(gbDoBillId);
            BigDecimal gbDbOrderAmount = new BigDecimal(billEntity.getGbDbOrderAmount());
            if (gbDbOrderAmount.compareTo(new BigDecimal("1")) == 1) {
                BigDecimal subtract = gbDbOrderAmount.subtract(new BigDecimal("1"));
                billEntity.setGbDbOrderAmount(subtract.intValue());
                gbDepartmentBillService.update(billEntity);
            } else {
                billEntity.setGbDbOrderAmount(0);
                billEntity.setGbDbStatus(0);
                gbDepartmentBillService.update(billEntity);
            }
            return R.ok();
        } else {
            return R.error(-1, "已经完成收货");
        }

    }


    private void subtractOutGoodsDailyBusiness(GbDepartmentGoodsStockEntity stockEntity) {
        Integer gbDgsGbGoodsStockId = stockEntity.getGbDgsGbGoodsStockId();
        GbDepartmentGoodsStockEntity stock = gbDepartmentGoodsStockService.queryObject(gbDgsGbGoodsStockId);

        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", stock.getGbDgsGbDepDisGoodsId());
        map.put("date", formatWhatDay(0));
        System.out.println("searchchdddialldydydyydyydydy" + map);
        GbDepartmentGoodsDailyEntity depGoodsDailyItem = gbDepGoodsDailyService.queryDepGoodsDailyItem(map);
        if (depGoodsDailyItem != null) {
            BigDecimal restWeight = new BigDecimal(depGoodsDailyItem.getGbDgdRestWeight());
            BigDecimal restSubtotal = new BigDecimal(depGoodsDailyItem.getGbDgdRestSubtotal());
            BigDecimal produceWeight = new BigDecimal(depGoodsDailyItem.getGbDgdProduceWeight());
            BigDecimal produceSubtotal = new BigDecimal(depGoodsDailyItem.getGbDgdProduceSubtotal());

            BigDecimal outWeight = new BigDecimal(stockEntity.getGbDgsWeight());
            BigDecimal outSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
            BigDecimal produceAllWeight = produceWeight.add(outWeight);
            BigDecimal produceAllSubtotal = produceSubtotal.add(outSubtotal);
            BigDecimal totalRestWeight = restWeight.subtract(outWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalRestSubtotal = restSubtotal.subtract(outSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            depGoodsDailyItem.setGbDgdRestWeight(totalRestWeight.toString());
            depGoodsDailyItem.setGbDgdRestSubtotal(totalRestSubtotal.toString());
            depGoodsDailyItem.setGbDgdProduceWeight(produceAllWeight.toString());
            depGoodsDailyItem.setGbDgdProduceSubtotal(produceAllSubtotal.toString());
            gbDepGoodsDailyService.update(depGoodsDailyItem);
        }

    }

    private void addNewStockReduce(GbDepartmentGoodsStockEntity stockEntity) {
        Integer gbDgsGbGoodsStockId = stockEntity.getGbDgsGbGoodsStockId();
        GbDepartmentGoodsStockEntity stock = gbDepartmentGoodsStockService.queryObject(gbDgsGbGoodsStockId);
        GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
        reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeProduce()); //
        reduceEntity.setGbDgsrStatus(0);
        reduceEntity.setGbDgsrGbDistributerId(stock.getGbDgsGbDistributerId());
        reduceEntity.setGbDgsrGbDepartmentId(stock.getGbDgsGbDepartmentId());
        reduceEntity.setGbDgsrGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
        reduceEntity.setGbDgsrGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
        reduceEntity.setGbDgsrGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
        reduceEntity.setGbDgsrGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
        reduceEntity.setGbDgsrGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
        reduceEntity.setGbDgsrFullTime(formatFullTime());
        reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReduceWeightUserId());
        reduceEntity.setGbDgsrDate(formatWhatDay(0));
        reduceEntity.setGbDgsrStockNxDistribtuerId(stock.getGbDgsNxDistributerId());
        reduceEntity.setGbDgsrStockNxSupplierId(stock.getGbDgsNxSupplierId());
        reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
        reduceEntity.setGbDgsrMonth(formatWhatMonth(0));
        reduceEntity.setGbDgsrCostWeight(stockEntity.getGbDgsWeight());
        reduceEntity.setGbDgsrCostSubtotal(stockEntity.getGbDgsSubtotal());
        reduceEntity.setGbDgsrProduceWeight(stockEntity.getGbDgsWeight());
        reduceEntity.setGbDgsrProduceSubtotal(stockEntity.getGbDgsSubtotal());
        reduceEntity.setGbDgsrGbPurGoodsId(stock.getGbDgsGbPurGoodsId());
        reduceEntity.setGbDgsrReturnWeight("0");
        reduceEntity.setGbDgsrReturnSubtotal("0");
        reduceEntity.setGbDgsrWasteWeight("0");
        reduceEntity.setGbDgsrWasteSubtotal("0");
        reduceEntity.setGbDgsrLossWeight("0");
        reduceEntity.setGbDgsrLossSubtotal("0");
        reduceEntity.setGbDgsrGbGoodsInventoryType(1);
        gbDepGoodsStockReduceService.save(reduceEntity);

        BigDecimal myChangeWeight = new BigDecimal(stockEntity.getGbDgsWeight());
        BigDecimal myChangeSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());

        //update
        BigDecimal allWeight = new BigDecimal(stock.getGbDgsProduceWeight()).add(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
        BigDecimal allSubtotal = new BigDecimal(stock.getGbDgsProduceSubtotal()).add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
        stock.setGbDgsProduceWeight(allWeight.toString());
        stock.setGbDgsProduceSubtotal(allSubtotal.toString());
        gbDepartmentGoodsStockService.update(stock);

    }

    private void orderAddDepDisGoods(GbDepartmentOrdersEntity ordersEntity, GbDepartmentGoodsStockEntity stockEntity, Integer depDisGoodsId) {

        BigDecimal stockSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
        BigDecimal stockWeight = new BigDecimal(stockEntity.getGbDgsWeight());
        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal weight = new BigDecimal(0);
        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(stockSubtotal);
        weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(stockWeight);
        //updateOrder
        depDisGoodsEntity.setGbDdgOrderDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgOrderPrice(ordersEntity.getGbDoPrice());
        depDisGoodsEntity.setGbDdgOrderQuantity(ordersEntity.getGbDoQuantity());
        depDisGoodsEntity.setGbDdgOrderRemark(ordersEntity.getGbDoRemark());
        depDisGoodsEntity.setGbDdgOrderStandard(ordersEntity.getGbDoStandard());
        depDisGoodsEntity.setGbDdgOrderWeight(ordersEntity.getGbDoWeight());


        if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(new BigDecimal(0)) == 1) {
            BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
            BigDecimal standardWeight = weight.divide(showScale, 1, BigDecimal.ROUND_HALF_UP);
            depDisGoodsEntity.setGbDdgShowStandardWeight(standardWeight.toString());
        }

        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));
        gbDepartmentDisGoodsService.update(depDisGoodsEntity);

    }


    private void updateDepDisGoods(GbDepartmentGoodsStockEntity stockEntity, Integer depDisGoodsId, String what) {
        System.out.println("updateDepDisGoodsupdateDepDisGoods" + what);
        BigDecimal stockSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
        BigDecimal stockWeight = new BigDecimal(stockEntity.getGbDgsWeight());
        System.out.println("sotoscksubd;dldl" + stockWeight);
        System.out.println("sotoscksubd;dldl" + stockSubtotal);
        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal weight = new BigDecimal(0);
        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        System.out.println("depgoeoidididiid" + depDisGoodsEntity.getGbDepartmentDisGoodsId());
        if (what.equals("add")) {
            subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(stockSubtotal);
            weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(stockWeight);
            System.out.println("adddddd" + subTotal + "weight" + weight);
        }
        if (what.equals("subtract")) {
            subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).subtract(stockSubtotal);
            weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).subtract(stockWeight);

        }
        System.out.println("zahuishsihsis" + subTotal);
        if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(new BigDecimal(0)) == 1) {
            BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
            BigDecimal standardWeight = weight.divide(showScale, 1, BigDecimal.ROUND_HALF_UP);
            depDisGoodsEntity.setGbDdgShowStandardWeight(standardWeight.toString());
        }
        System.out.println("suttootototo-------" + subTotal + "weithht=====" + weight);
        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));

        gbDepartmentDisGoodsService.update(depDisGoodsEntity);

    }


    private void updateDepDisGoodsNoPrice(GbDepartmentGoodsStockEntity stockEntity, GbDepartmentGoodsStockEntity outStockEntity, String what) {
        BigDecimal stockWeight = new BigDecimal(stockEntity.getGbDgsWeight());
        BigDecimal fromStockPriceB = new BigDecimal(outStockEntity.getGbDgsPrice());
        BigDecimal stockSubtotal = fromStockPriceB.multiply(stockWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal weight = new BigDecimal(0);
        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(outStockEntity.getGbDgsGbDepDisGoodsId());
        if (what.equals("add")) {
            subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(stockSubtotal);
            weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(stockWeight);

        }
        if (what.equals("subtract")) {
            subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).subtract(stockSubtotal);
            weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).subtract(stockWeight);

        }
        if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(new BigDecimal(0)) == 1) {
            BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
            BigDecimal standardWeight = weight.divide(showScale, 1, BigDecimal.ROUND_HALF_UP);
            depDisGoodsEntity.setGbDdgShowStandardWeight(standardWeight.toString());
        }

        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));
        gbDepartmentDisGoodsService.update(depDisGoodsEntity);

    }


    private void updateDepGoodsDailyBusiness(GbDepartmentGoodsStockEntity stock) {

        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", stock.getGbDgsGbDepDisGoodsId());
        map.put("date", formatWhatDay(0));
        GbDepartmentGoodsDailyEntity depGoodsDailyItem = gbDepGoodsDailyService.queryDepGoodsDailyItem(map);
        if (depGoodsDailyItem != null) {
            BigDecimal weight = new BigDecimal(depGoodsDailyItem.getGbDgdWeight());
            BigDecimal total = new BigDecimal(depGoodsDailyItem.getGbDgdSubtotal());
            BigDecimal restWeight = new BigDecimal(depGoodsDailyItem.getGbDgdRestWeight());
            BigDecimal restSubtotal = new BigDecimal(depGoodsDailyItem.getGbDgdRestSubtotal());
            BigDecimal totalWeight = new BigDecimal(stock.getGbDgsWeight()).add(weight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalSubtotal = new BigDecimal(stock.getGbDgsSubtotal()).add(total).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalRestWeight = new BigDecimal(stock.getGbDgsWeight()).add(restWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalRestSubtotal = new BigDecimal(stock.getGbDgsSubtotal()).add(restSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            depGoodsDailyItem.setGbDgdWeight(totalWeight.toString());
            depGoodsDailyItem.setGbDgdSubtotal(totalSubtotal.toString());
            depGoodsDailyItem.setGbDgdRestWeight(totalRestWeight.toString());
            depGoodsDailyItem.setGbDgdRestSubtotal(totalRestSubtotal.toString());
            depGoodsDailyItem.setGbDgdSellClearHour("-1");
            depGoodsDailyItem.setGbDgdSellClearMinute("-1");
            depGoodsDailyItem.setGbDgdStatus(0);
            gbDepGoodsDailyService.update(depGoodsDailyItem);

        } else {
            GbDepartmentGoodsDailyEntity dailyEntity = new GbDepartmentGoodsDailyEntity();
            dailyEntity.setGbDgdGbDistributerId(stock.getGbDgsGbDistributerId());
            dailyEntity.setGbDgdGbDepartmentId(stock.getGbDgsGbDepartmentId());
            dailyEntity.setGbDgdGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            dailyEntity.setGbDgdGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
            dailyEntity.setGbDgdGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            dailyEntity.setGbDgdGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
            dailyEntity.setGbDgdDate(formatWhatDay(0));
            dailyEntity.setGbDgdWeek(getWeekOfYear(0).toString());
            dailyEntity.setGbDgdMonth(formatWhatMonth(0));
            dailyEntity.setGbDgdYear(formatWhatYear(0));
            dailyEntity.setGbDgdDay(getWeek(0));
            dailyEntity.setGbDgdWeight(stock.getGbDgsWeight());
            dailyEntity.setGbDgdRestWeight(stock.getGbDgsWeight());
            dailyEntity.setGbDgdRestSubtotal(stock.getGbDgsSubtotal());
            dailyEntity.setGbDgdSubtotal(stock.getGbDgsSubtotal());
            dailyEntity.setGbDgdProduceWeight("0");
            dailyEntity.setGbDgdProduceSubtotal("0");
            dailyEntity.setGbDgdLossWeight("0");
            dailyEntity.setGbDgdLossSubtotal("0");
            dailyEntity.setGbDgdReturnWeight("0");
            dailyEntity.setGbDgdReturnSubtotal("0");
            dailyEntity.setGbDgdWasteWeight("0");
            dailyEntity.setGbDgdWasteSubtotal("0");
            dailyEntity.setGbDgdSalesSubtotal("0");
            dailyEntity.setGbDgdProfitSubtotal("0");
            dailyEntity.setGbDgdAfterProfitSubtotal("0");
            dailyEntity.setGbDgdSellClearHour("-1");
            dailyEntity.setGbDgdSellClearMinute("-1");
            dailyEntity.setGbDgdLastWeight("0");
            dailyEntity.setGbDgdLastSubtotal("0");
            dailyEntity.setGbDgdLastProduceWeight("0");
            Integer gbDgdGbDisGoodsId = dailyEntity.getGbDgdGbDisGoodsId();
            GbDistributerGoodsEntity distributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDgdGbDisGoodsId);
            if (distributerGoodsEntity.getGbDgControlFresh() == 1) {
                dailyEntity.setGbDgdFreshRate("100");
            } else {
                dailyEntity.setGbDgdFreshRate("0");
            }
            dailyEntity.setGbDgdFullTime(formatFullTime());
            dailyEntity.setGbDgdStatus(0);
            gbDepGoodsDailyService.save(dailyEntity);
        }
    }

    private void addDepartmentOrderHistory(GbDepartmentOrdersEntity order) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depDisGoodsId", order.getGbDoDisGoodsId());
        map1.put("depId", order.getGbDoDepartmentId());
        List<GbDepartmentOrdersHistoryEntity> historyEntities = gbDepOrdersHistoryService.queryGbDepHistoryOrdersByParams(map1);
        String orderQuantity = "";
        String orderStandard = "";
        String orderStr = "";
        orderQuantity = order.getGbDoQuantity();
        orderStandard = order.getGbDoStandard();

        orderStr = orderQuantity + orderStandard;

        //如果有4个以内的历史记录
        if (historyEntities.size() > 0 && historyEntities.size() < 4) {

            int equalNumber = 0;
            for (GbDepartmentOrdersHistoryEntity orderHistory : historyEntities) {
                String historyStr = orderHistory.getGbDohQuantity() + orderHistory.getGbDohStandard();
                if (orderStr.equals(historyStr)) {
                    equalNumber = equalNumber + 1;
                }
            }
            if (equalNumber == 0 && historyEntities.size() < 3) {
                //添加新的
                GbDepartmentOrdersHistoryEntity historyEntity = new GbDepartmentOrdersHistoryEntity();
                historyEntity.setGbDohApplyDate(order.getGbDoApplyDate());
                historyEntity.setGbDohDepDisGoodsId(order.getGbDoDepDisGoodsId());
                historyEntity.setGbDohQuantity(orderQuantity);
                historyEntity.setGbDohStandard(orderStandard);
                historyEntity.setGbDohStandardId(order.getGbDoDsStandardId());
                historyEntity.setGbDohStandardScale(order.getGbDoDsStandardScale());
                historyEntity.setGbDohDepartmentId(order.getGbDoDepartmentId());
                historyEntity.setGbDohDepartmentFatherId(order.getGbDoDepartmentFatherId());
                historyEntity.setGbDohOrderUserId(order.getGbDoOrderUserId());
                gbDepOrdersHistoryService.save(historyEntity);
            } else if (equalNumber == 0 && historyEntities.size() == 3) {
                //删除最早一个
                GbDepartmentOrdersHistoryEntity first = historyEntities.get(0);
                Integer nxRestrauntOrdersHistoryId = first.getGbDepartmentOrdersHistoryId();
                gbDepOrdersHistoryService.delete(nxRestrauntOrdersHistoryId);
                //添加新的
                GbDepartmentOrdersHistoryEntity historyEntity = new GbDepartmentOrdersHistoryEntity();
                historyEntity.setGbDohApplyDate(order.getGbDoApplyDate());
                historyEntity.setGbDohDepDisGoodsId(order.getGbDoDepDisGoodsId());
                historyEntity.setGbDohQuantity(orderQuantity);
                historyEntity.setGbDohStandard(orderStandard);
                historyEntity.setGbDohStandardId(order.getGbDoDsStandardId());
                historyEntity.setGbDohStandardScale(order.getGbDoDsStandardScale());
                historyEntity.setGbDohDepartmentId(order.getGbDoDepartmentId());
                historyEntity.setGbDohDepartmentFatherId(order.getGbDoDepartmentFatherId());
                historyEntity.setGbDohOrderUserId(order.getGbDoOrderUserId());
                gbDepOrdersHistoryService.save(historyEntity);
            }

        } else {
            //添加新的
            GbDepartmentOrdersHistoryEntity historyEntity = new GbDepartmentOrdersHistoryEntity();
            historyEntity.setGbDohApplyDate(order.getGbDoApplyDate());
            historyEntity.setGbDohDepDisGoodsId(order.getGbDoDepDisGoodsId());
            historyEntity.setGbDohQuantity(orderQuantity);
            historyEntity.setGbDohStandard(orderStandard);
            historyEntity.setGbDohStandardId(order.getGbDoDsStandardId());
            historyEntity.setGbDohStandardScale(order.getGbDoDsStandardScale());
            historyEntity.setGbDohDepartmentId(order.getGbDoDepartmentId());
            historyEntity.setGbDohDepartmentFatherId(order.getGbDoDepartmentFatherId());
            historyEntity.setGbDohOrderUserId(order.getGbDoOrderUserId());
            gbDepOrdersHistoryService.save(historyEntity);
        }

    }


    @RequestMapping(value = "/cancelOrderOutWeight", method = RequestMethod.POST)
    @ResponseBody
    public R cancleOrderOutWeight(@RequestBody GbDepartmentOrdersEntity order) {


        //xiugaiweight
        if (order.getGbDoWeightTotalId() != null && order.getGbDoWeightTotalId() > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("weightId", order.getGbDoWeightTotalId());
            Integer orderAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
            map.put("equalStatus", 3);
            Integer orderAmountFinish = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
            if (orderAmount == orderAmountFinish) {
                GbDistributerWeightTotalEntity gbDistributerWeightTotalEntity = gbDisWeightTotalService.queryObject(order.getGbDoWeightTotalId());
                gbDistributerWeightTotalEntity.setGbGwtStatus(1);
                gbDisWeightTotalService.update(gbDistributerWeightTotalEntity);
            }
        }

        List<GbDepartmentGoodsStockEntity> goodsStockEntityList = order.getGoodsStockEntityList();
        if (goodsStockEntityList.size() > 0) {

            for (GbDepartmentGoodsStockEntity stockEntity : goodsStockEntityList) {
                //修改出库部门数据
                Integer gbDgsGbGoodsStockId = stockEntity.getGbDgsGbGoodsStockId();
                GbDepartmentGoodsStockEntity outStockEntity = gbDepartmentGoodsStockService.queryObject(gbDgsGbGoodsStockId);
                BigDecimal outStockRestWeight = new BigDecimal(outStockEntity.getGbDgsRestWeight());
                BigDecimal outStockRestSubtotal = new BigDecimal(outStockEntity.getGbDgsRestSubtotal());
                BigDecimal newWeight = new BigDecimal(stockEntity.getGbDgsWeight());
                BigDecimal newSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
                BigDecimal outTotalRestWeight = outStockRestWeight.add(newWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal outTotalRestSubtotal = outStockRestSubtotal.add(newSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                outStockEntity.setGbDgsRestWeight(outTotalRestWeight.toString());
                outStockEntity.setGbDgsRestSubtotal(outTotalRestSubtotal.toString());

                //
                if (outStockEntity.getGbDgsRestWeightShowStandard() != null && !outStockEntity.getGbDgsRestWeightShowStandard().trim().isEmpty()) {
                    Integer gbDgsGbDepDisGoodsId = outStockEntity.getGbDgsGbDepDisGoodsId();
                    GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDgsGbDepDisGoodsId);
                    BigDecimal gbDdgShowStandardScale = new BigDecimal(departmentDisGoodsEntity.getGbDdgShowStandardScale());
                    BigDecimal newShowStandardWeight = outTotalRestWeight.divide(gbDdgShowStandardScale, 1, BigDecimal.ROUND_HALF_UP);
                    outStockEntity.setGbDgsRestWeightShowStandard(newShowStandardWeight.toString());
                    outStockEntity.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
                }
                gbDepartmentGoodsStockService.update(outStockEntity);

                System.out.println("outstocenene" + outStockEntity.getGbDgsGbDepDisGoodsId());
                updateDepDisGoods(stockEntity, outStockEntity.getGbDgsGbDepDisGoodsId(), "add");


                //删除自己
                gbDepartmentGoodsStockService.delete(stockEntity.getGbDepartmentGoodsStockId());
            }
        }

        order.setGbDoStatus(getGbOrderStatusNew());
        order.setGbDoBuyStatus(getGbOrderBuyStatusNew());
        order.setGbDoWeightGoodsId(null);
        order.setGbDoWeightTotalId(null);
        gbDepartmentOrdersService.update(order);

        return R.ok();
    }




    @RequestMapping(value = "/cancelOrderOutWeightSelf", method = RequestMethod.POST)
    @ResponseBody
    public R cancelOrderOutWeightSelf(@RequestBody GbDepartmentOrdersEntity order) {


        List<GbDepartmentGoodsStockEntity> outGoodsStockEntityList = order.getGoodsStockEntityList();
        if (outGoodsStockEntityList.size() > 0) {
            for (GbDepartmentGoodsStockEntity stockEntity : outGoodsStockEntityList) {
                gbDepartmentGoodsStockService.delete(stockEntity.getGbDepartmentGoodsStockId());
            }
        }
        order.setGbDoWeight("0");
        order.setGbDoSellingPrice(null);
        order.setGbDoSellingSubtotal(null);
        order.setGbDoPrice("0");
        order.setGbDoPickUserId(null);
        order.setGbDoStatus(getGbOrderStatusNew());
        order.setGbDoBuyStatus(getGbOrderBuyStatusNew());
        order.setGbDoWeightGoodsId(null);
        order.setGbDoWeightTotalId(null);
        order.setGbDoSubtotal("0");
        gbDepartmentOrdersService.update(order);

        return R.ok();
    }

    //

    @RequestMapping(value = "/gbOrderOutWeight", method = RequestMethod.POST)
    @ResponseBody
    public R gbOrderOutWeight(Integer orderId, String weight) {

        GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(orderId);
        Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);

        System.out.println("ordstats" + gbDepartmentOrdersEntity.getGbDoStatus());
        if(gbDepartmentOrdersEntity.getGbDoStatus() == 0){

            gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusProcurement());

            Integer gbDpgOrdersWeightAmount = purchaseGoodsEntity.getGbDpgOrdersWeightAmount();
            Integer gbDpgOrdersAmount = purchaseGoodsEntity.getGbDpgOrdersAmount();
            System.out.println("gbDpgOrdersFinishAmount=" + gbDpgOrdersWeightAmount +"gbDpgOrdersAmount== " + gbDpgOrdersAmount);
            if(gbDpgOrdersAmount - gbDpgOrdersWeightAmount == 1){
                purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusWeightFinished());
            }
            purchaseGoodsEntity.setGbDpgOrdersWeightAmount(gbDpgOrdersWeightAmount + 1);
        }

        gbDepartmentOrdersEntity.setGbDoWeight(weight);

        System.out.println("priiciie" + gbDepartmentOrdersEntity.getGbDoPrice());
        if (gbDepartmentOrdersEntity.getGbDoPrice() != null && !gbDepartmentOrdersEntity.getGbDoPrice().trim().isEmpty() && !gbDepartmentOrdersEntity.getGbDoPrice().equals("0.0")) {
            BigDecimal decimal1 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoPrice());
            BigDecimal decimal2 = new BigDecimal(weight);
            BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbDepartmentOrdersEntity.setGbDoSubtotal(decimal3.toString());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());

        }else{
            gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusPrepareing());
        }
        gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

        System.out.println("aaaaaaaaaaaaaaaaaaaaa");
        Map<String, Object> map = new HashMap<>();
        map.put("purGoodsId", purchaseGoodsId);
        System.out.println("mapsusmssnnsns1111aaaa" + map);
        Integer integer = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
        if(integer > 0){
            double weightTotal = gbDepartmentOrdersService.queryOrderWeightTotalByPurGoodsId(purchaseGoodsId);
            purchaseGoodsEntity.setGbDpgBuyQuantity(String.format("%.1f", weightTotal));
             map.put("subtotal", 0);
            Integer integerHave = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
            if(integerHave > 0){
                System.out.println("queryGbOrdersSubtotalqueryGbOrdersSubtotal");
                double subtotalTotal = gbDepartmentOrdersService.queryGbOrdersSubtotal(map);
                purchaseGoodsEntity.setGbDpgBuySubtotal(String.format("%.1f", subtotalTotal));
            }
            gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

            Integer gbDpgBatchId = purchaseGoodsEntity.getGbDpgBatchId();
            Map<String, Object> mapBatch = new HashMap<>();
            mapBatch.put("batchId", gbDpgBatchId);
            Integer integer1 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapBatch);
            if(integer1 > 0){
                Double subTotal = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(mapBatch);
                GbDistributerPurchaseBatchEntity batchEntity = gbDistributerPurchaseBatchService.queryObject(gbDpgBatchId);
                batchEntity.setGbDpbSubtotal(String.format("%.1f", subTotal));
                gbDistributerPurchaseBatchService.update(batchEntity);
            }
        }

        return R.ok();
    }


    /**
     * 库房和自制公用保存订单出库数量
     *
     * @param order
     * @return
     */
    @RequestMapping(value = "/saveOrderOutWeightStock", method = RequestMethod.POST)
    @ResponseBody
    public R saveOrderOutWeightStock(@RequestBody GbDepartmentOrdersEntity order) {

        saveOutWeightStock(order);
        order.setGbDoStatus(getGbOrderStatusHasFinished());
        order.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
        gbDepartmentOrdersService.update(order);

        updateWeightOrderAmount(order);

        return R.ok();
    }

    /**
     * 库房和自制公用保存订单出库数量
     *
     * @param order
     * @return
     */
    @RequestMapping(value = "/saveOrderOutWeightStockNoPrice", method = RequestMethod.POST)
    @ResponseBody
    public R saveOrderOutWeightStockNoPrice(@RequestBody GbDepartmentOrdersEntity order) {


        if (order.getGbDoPrice() != null && !order.getGbDoPrice().trim().isEmpty()) {
            BigDecimal decimal = new BigDecimal(order.getGbDoWeight()).multiply(new BigDecimal(order.getGbDoPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
            order.setGbDoSubtotal(decimal.toString());
            order.setGbDoStatus(getGbOrderStatusHasFinished());
            order.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());

        } else {
            order.setGbDoStatus(getGbOrderStatusProcurement());
            order.setGbDoBuyStatus(getGbOrderBuyStatusPrepareing());
        }

        saveOutWeightStockNoPrice(order);

        gbDepartmentOrdersService.update(order);

        updateWeightOrderAmount(order);

        return R.ok();
    }


    @RequestMapping(value = "/saveNoPriceOrderPrice", method = RequestMethod.POST)
    @ResponseBody
    public R saveNoPriceOrderPrice(@RequestBody GbDepartmentOrdersEntity order) {

        //如果有重量
        List<GbDepartmentGoodsStockEntity> outGoodsStockEntityList = order.getGoodsStockEntityList();
        if (outGoodsStockEntityList.size() > 0) {
            BigDecimal decimal1 = new BigDecimal(order.getGbDoPrice()).multiply(new BigDecimal(order.getGbDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
            order.setGbDoSubtotal(decimal1.toString());
            order.setGbDoStatus(getGbOrderStatusHasFinished());
            order.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            BigDecimal gbDoPriceB = new BigDecimal(order.getGbDoPrice());
            for (GbDepartmentGoodsStockEntity stockEntity : outGoodsStockEntityList) {
                BigDecimal weightB = new BigDecimal(stockEntity.getGbDgsWeight());
                BigDecimal decimal = gbDoPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                stockEntity.setGbDgsSubtotal(decimal.toString());
                stockEntity.setGbDgsPrice(order.getGbDoPrice());
                stockEntity.setGbDgsRestSubtotal(decimal.toString());
                gbDepartmentGoodsStockService.update(stockEntity);
            }
        }

        gbDepartmentOrdersService.update(order);

        return R.ok();
    }


    @RequestMapping(value = "/editOrderOutWeightStock", method = RequestMethod.POST)
    @ResponseBody
    public R editOrderOutWeightStock(@RequestBody GbDepartmentOrdersEntity order) {


        //subscribe depgoods weight
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", gbDepartmentOrdersId);
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepartmentGoodsStockService.queryGoodsStockByParams(map);
        for (GbDepartmentGoodsStockEntity orderStock : stockEntities) {
            Integer gbDgsGbGoodsStockId = orderStock.getGbDgsGbGoodsStockId();
            GbDepartmentGoodsStockEntity stockEntity = gbDepartmentGoodsStockService.queryObject(gbDgsGbGoodsStockId);
            updateDepDisGoods(orderStock, stockEntity.getGbDgsGbDepDisGoodsId(), "add");
            gbDepartmentGoodsStockService.delete(orderStock.getGbDepartmentGoodsStockId());

        }


        saveOutWeightStock(order);

        gbDepartmentOrdersService.update(order);
        return R.ok();
    }

    @RequestMapping(value = "/editOrderOutWeightStockNoPrice", method = RequestMethod.POST)
    @ResponseBody
    public R editOrderOutWeightStockNoPrice(@RequestBody GbDepartmentOrdersEntity order) {


        //subscribe depgoods weight
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", gbDepartmentOrdersId);
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepartmentGoodsStockService.queryGoodsStockByParams(map);
        for (GbDepartmentGoodsStockEntity orderStock : stockEntities) {
            Integer gbDepartmentGoodsStockId = orderStock.getGbDgsGbGoodsStockId();
            GbDepartmentGoodsStockEntity outStockEntity = gbDepartmentGoodsStockService.queryObject(gbDepartmentGoodsStockId);
            updateDepDisGoodsNoPrice(orderStock, outStockEntity, "add");
            gbDepartmentGoodsStockService.delete(orderStock.getGbDepartmentGoodsStockId());
        }

        saveOutWeightStockNoPrice(order);

        gbDepartmentOrdersService.update(order);
        return R.ok();
    }


    @RequestMapping(value = "/saveOrderOutWeightSelf", method = RequestMethod.POST)
    @ResponseBody
    public R saveOrderOutWeightSelf(@RequestBody GbDepartmentOrdersEntity order) {
        order.setGbDoStatus(getGbOrderStatusHasFinished());
        order.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
        gbDepartmentOrdersService.update(order);

        updateWeightOrderAmount(order);

        saveOutWeightSelf(order);

        return R.ok();
    }

    @RequestMapping(value = "/editOrderOutWeightSelf", method = RequestMethod.POST)
    @ResponseBody
    public R editOrderOutWeightSelf(@RequestBody GbDepartmentOrdersEntity order) {

        gbDepartmentOrdersService.update(order);

        List<GbDepartmentGoodsStockEntity> goodsStockEntityList = order.getGoodsStockEntityList();
        if (goodsStockEntityList.size() > 0) {
            for (GbDepartmentGoodsStockEntity stockEntity : goodsStockEntityList) {
                stockEntity.setGbDgsWeight(order.getGbDoWeight());
                stockEntity.setGbDgsPrice(order.getGbDoPrice());
                stockEntity.setGbDgsSubtotal(order.getGbDoSubtotal());
                stockEntity.setGbDgsRestSubtotal(order.getGbDoSubtotal());
                stockEntity.setGbDgsRestWeight(order.getGbDoWeight());
                stockEntity.setGbDgsProduceSubtotal("0");
                stockEntity.setGbDgsFullTime(formatFullTime());
                stockEntity.setGbDgsDate(formatWhatDay(0));
                stockEntity.setGbDgsTimeStamp(getTimeStamp());
                stockEntity.setGbDgsWeek(getWeek(0));
                stockEntity.setGbDgsMonth(formatWhatMonth(0));
                stockEntity.setGbDgsYear(formatWhatYear(0));
                stockEntity.setGbDgsProfitSubtotal("0");
                stockEntity.setGbDgsAfterProfitSubtotal("0");
                stockEntity.setGbDgsProfitWeight("0");
                stockEntity.setGbDgsProduceSellingSubtotal("0");

                String gbDgsSellingPrice = stockEntity.getGbDgsSellingPrice();
                if (!gbDgsSellingPrice.equals("-1")) {
                    GbDepartmentGoodsStockEntity stockEntity1 = order.getSelfControlStockEntity();
                    if (stockEntity1 != null) {
                        if (order.getGbDoSellingPrice() != null && new BigDecimal(order.getGbDoSellingPrice()).compareTo(BigDecimal.ZERO) == 1) {
                            BigDecimal subtract = new BigDecimal(order.getGbDoSellingPrice()).subtract(new BigDecimal(order.getGbDoPrice()));
                            BigDecimal divide = new BigDecimal(order.getGbDoPrice()).divide(new BigDecimal(order.getGbDoSellingPrice()), 2, BigDecimal.ROUND_HALF_UP);
                            stockEntity.setGbDgsBetweenPrice(subtract.toString());
                            stockEntity.setGbDgsCostRate(divide.toString());
                            stockEntity.setGbDgsSellingPrice(order.getGbDoSellingPrice());
                            stockEntity.setGbDgsSellingSubtotal(order.getGbDoSellingSubtotal());

                        } else {
                            stockEntity.setGbDgsBetweenPrice("0");
                            stockEntity.setGbDgsCostRate("0");
                            stockEntity.setGbDgsSellingPrice("0");
                        }
                    }
                } else {
                    stockEntity.setGbDgsSellingPrice("-1");
                    stockEntity.setGbDgsSellingSubtotal("0");
                }

                gbDepartmentGoodsStockService.update(stockEntity);
            }
        }


        return R.ok();
    }

    private void updateWeightOrderAmount(GbDepartmentOrdersEntity order) {

        //weightGoods
        Integer gbDoWeightGoodsId = order.getGbDoWeightGoodsId();
        if (gbDoWeightGoodsId != null && gbDoWeightGoodsId > 0) {
            GbDistributerWeightGoodsEntity gbWeightGoodsEntity = gbDisWeightGoodsService.queryObject(gbDoWeightGoodsId);
            BigDecimal gbGwtOrderFinishCount = new BigDecimal(gbWeightGoodsEntity.getGbDwgOrderFinishAmount());
            BigDecimal add = gbGwtOrderFinishCount.add(new BigDecimal(1));
            gbWeightGoodsEntity.setGbDwgOrderFinishAmount(add.toString());
            if (add.compareTo(new BigDecimal(gbWeightGoodsEntity.getGbDwgOrderAmount())) == 0) {
                gbWeightGoodsEntity.setGbDwgStatus(1);
            }
            gbDisWeightGoodsService.update(gbWeightGoodsEntity);
        }

        // weightTotal
        Integer gbDoWeightTotalId = order.getGbDoWeightTotalId();
        if (gbDoWeightTotalId != null && gbDoWeightTotalId > 0) {
            GbDistributerWeightTotalEntity gbWeightTotalEntity = gbDisWeightTotalService.queryObject(gbDoWeightTotalId);
            BigDecimal gbGwtOrderFinishCount = new BigDecimal(gbWeightTotalEntity.getGbGwtOrderFinishCount());
            BigDecimal add = gbGwtOrderFinishCount.add(new BigDecimal(1));
            gbWeightTotalEntity.setGbGwtOrderFinishCount(add.toString());
            if (add.compareTo(new BigDecimal(gbWeightTotalEntity.getGbGwtOrderCount())) == 0) {
                gbWeightTotalEntity.setGbGwtStatus(getGbWeightTotalStatusFinished());
            }
            gbDisWeightTotalService.update(gbWeightTotalEntity);
        }
    }


    private void saveOutWeightSelf(GbDepartmentOrdersEntity order) {

        String gbDoWeight = order.getGbDoWeight();
        String gbDoPrice = order.getGbDoPrice();
        String gbDoSubtotal = order.getGbDoSubtotal();

        if (new BigDecimal(gbDoWeight).compareTo(BigDecimal.ZERO) == 1) {
            //2,添加订单出库批次
            GbDepartmentGoodsStockEntity depGoodsStockEntity = new GbDepartmentGoodsStockEntity();
            depGoodsStockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
            depGoodsStockEntity.setGbDgsGbPurGoodsId(-1);
            depGoodsStockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
            depGoodsStockEntity.setGbDgsGbDisGoodsFatherId(order.getGbDoDisGoodsFatherId());
            depGoodsStockEntity.setGbDgsGbDisGoodsGrandId(order.getGbDoDisGoodsGrandId());
            depGoodsStockEntity.setGbDgsGbDisGoodsGreatId(order.getGbDoDisGoodsGreatId());
            depGoodsStockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
            depGoodsStockEntity.setGbDgsWeight(gbDoWeight);
            depGoodsStockEntity.setGbDgsPrice(gbDoPrice);
            depGoodsStockEntity.setGbDgsSubtotal(gbDoSubtotal);
            depGoodsStockEntity.setGbDgsRestSubtotal(gbDoSubtotal);
            depGoodsStockEntity.setGbDgsRestWeight(gbDoWeight);
            depGoodsStockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
            depGoodsStockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
            depGoodsStockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
            depGoodsStockEntity.setGbDgsGbGoodsStockId(-1);
            depGoodsStockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
            depGoodsStockEntity.setGbDgsOutDate(formatWhatDay(0));
            depGoodsStockEntity.setGbDgsOutFullTime(formatFullTime());
            depGoodsStockEntity.setGbDgsOutHour(Integer.valueOf(formatWhatHour(0)));
            depGoodsStockEntity.setGbDgsStatus(-1);
            depGoodsStockEntity.setGbDgsLossWeight("0");
            depGoodsStockEntity.setGbDgsLossSubtotal("0");
            depGoodsStockEntity.setGbDgsReturnWeight("0");
            depGoodsStockEntity.setGbDgsReturnSubtotal("0");
            depGoodsStockEntity.setGbDgsProduceWeight("0");
            depGoodsStockEntity.setGbDgsProduceSubtotal("0");
            depGoodsStockEntity.setGbDgsWasteWeight("0");
            depGoodsStockEntity.setGbDgsWasteSubtotal("0");
            depGoodsStockEntity.setGbDgsWeek(getWeek(0));
            depGoodsStockEntity.setGbDgsMonth(formatWhatMonth(0));
            depGoodsStockEntity.setGbDgsYear(formatWhatYear(0));
            depGoodsStockEntity.setGbDgsProfitSubtotal("0");
            depGoodsStockEntity.setGbDgsAfterProfitSubtotal("0");
            depGoodsStockEntity.setGbDgsProduceSellingSubtotal("0");
            depGoodsStockEntity.setGbDgsProfitWeight("0");
            GbDepartmentGoodsStockEntity stockEntity = order.getSelfControlStockEntity();
            if (stockEntity != null) {
                if (stockEntity.getGbDgsWasteFullTime() != null) {
                    String gbDgsWasteFullTime = stockEntity.getGbDgsWasteFullTime();
                    int integer;
                    try {
                        integer = Integer.parseInt(gbDgsWasteFullTime);
                    } catch (NumberFormatException e) {
                        integer = 0;
                    }
                    depGoodsStockEntity.setGbDgsWarnFullTime(formatWhatFullTime(integer));
                    depGoodsStockEntity.setGbDgsWasteFullTime(formatWhatFullTime(integer));
                    String gbDpgWarnFullTime = formatWhatFullTime(integer);
                    String gbDpgWasteFullTime = formatWhatFullTime(integer);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    // 设置日期字符串
                    // 解析日期字符串为Date对象
                    Date dateWaste = null;
                    Date dateWarn = null;
                    try {
                        dateWaste = dateFormat.parse(gbDpgWasteFullTime);
                        dateWarn = dateFormat.parse(gbDpgWarnFullTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // 获取时间戳
                    long timestampWaste = dateWaste.getTime();
                    long timestampWarn = dateWarn.getTime();
                    // 输出时间戳
                    stockEntity.setGbDgsWasteTimeQuantumName(String.valueOf(timestampWaste));
                    stockEntity.setGbDgsWarnTimeQuantumName(String.valueOf(timestampWarn));


                }

                if (order.getGbDoSellingPrice() != null && new BigDecimal(order.getGbDoSellingPrice()).compareTo(BigDecimal.ZERO) == 1) {
                    BigDecimal subtract = new BigDecimal(order.getGbDoSellingPrice()).subtract(new BigDecimal(order.getGbDoPrice()));
                    BigDecimal divide = new BigDecimal(gbDoPrice).divide(new BigDecimal(order.getGbDoSellingPrice()), 2, BigDecimal.ROUND_HALF_UP);
                    depGoodsStockEntity.setGbDgsBetweenPrice(subtract.toString());
                    depGoodsStockEntity.setGbDgsCostRate(divide.toString());
                    depGoodsStockEntity.setGbDgsSellingPrice(order.getGbDoSellingPrice());
                    depGoodsStockEntity.setGbDgsSellingSubtotal(order.getGbDoSellingSubtotal());

                } else {
                    depGoodsStockEntity.setGbDgsSellingPrice("-1");
                    depGoodsStockEntity.setGbDgsSellingSubtotal("0");
                }
            }

            //weightGoods
            Integer gbDoWeightGoodsId = order.getGbDoWeightGoodsId();
            if (gbDoWeightGoodsId != null && gbDoWeightGoodsId > 0) {
                depGoodsStockEntity.setGbDgsWeightGoodsId(gbDoWeightGoodsId);
            }
            gbDepartmentGoodsStockService.save(depGoodsStockEntity);
        }

    }

    private void editOutWeight(GbDepartmentGoodsStockEntity stockEntity, GbDepartmentOrdersEntity order) {

        String gbDgsInventoryWeight = stockEntity.getGbDgsInventoryWeight();
        String gbDgsPrice = stockEntity.getGbDgsPrice();
        BigDecimal subtotal = new BigDecimal(gbDgsInventoryWeight).multiply(new BigDecimal(gbDgsPrice)).setScale(2, BigDecimal.ROUND_HALF_UP);
        gbDepartmentGoodsStockService.update(stockEntity);
        if (new BigDecimal(gbDgsInventoryWeight).compareTo(BigDecimal.ZERO) == 1) {
            //2,添加订单出库批次
            GbDepartmentGoodsStockEntity depGoodsStockEntity = new GbDepartmentGoodsStockEntity();
            depGoodsStockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
            depGoodsStockEntity.setGbDgsGbPurGoodsId(order.getGbDoPurchaseGoodsId());
            depGoodsStockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
            depGoodsStockEntity.setGbDgsGbDisGoodsFatherId(order.getGbDoDisGoodsFatherId());
            depGoodsStockEntity.setGbDgsGbDisGoodsGrandId(order.getGbDoDisGoodsGrandId());
            depGoodsStockEntity.setGbDgsGbDisGoodsGreatId(order.getGbDoDisGoodsGreatId());
            depGoodsStockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
            depGoodsStockEntity.setGbDgsWeight(gbDgsInventoryWeight);
            depGoodsStockEntity.setGbDgsPrice(gbDgsPrice);
            depGoodsStockEntity.setGbDgsSubtotal(subtotal.toString());
            depGoodsStockEntity.setGbDgsRestSubtotal(subtotal.toString());
            depGoodsStockEntity.setGbDgsRestWeight(gbDgsInventoryWeight);
            depGoodsStockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
            depGoodsStockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
            depGoodsStockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
            depGoodsStockEntity.setGbDgsGbGoodsStockId(stockEntity.getGbDepartmentGoodsStockId());
            depGoodsStockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
            depGoodsStockEntity.setGbDgsOutDate(formatWhatDay(0));
            depGoodsStockEntity.setGbDgsOutFullTime(formatFullTime());
            depGoodsStockEntity.setGbDgsOutHour(Integer.valueOf(formatWhatHour(0)));
            depGoodsStockEntity.setGbDgsStatus(-1);
            depGoodsStockEntity.setGbDgsLossWeight("0");
            depGoodsStockEntity.setGbDgsLossSubtotal("0");
            depGoodsStockEntity.setGbDgsReturnWeight("0");
            depGoodsStockEntity.setGbDgsReturnSubtotal("0");
            depGoodsStockEntity.setGbDgsProduceWeight("0");
            depGoodsStockEntity.setGbDgsProduceSubtotal("0");
            depGoodsStockEntity.setGbDgsWasteWeight("0");
            depGoodsStockEntity.setGbDgsWasteSubtotal("0");
            depGoodsStockEntity.setGbDgsProfitSubtotal("0");
            depGoodsStockEntity.setGbDgsAfterProfitSubtotal("0");
            depGoodsStockEntity.setGbDgsProduceSellingSubtotal("0");
            depGoodsStockEntity.setGbDgsProfitWeight("0");

            if (stockEntity.getGbDgsWarnFullTime() != null && !stockEntity.getGbDgsWarnFullTime().trim().isEmpty()) {
                depGoodsStockEntity.setGbDgsWarnFullTime(stockEntity.getGbDgsWarnFullTime());
                depGoodsStockEntity.setGbDgsWarnTimeQuantumName(stockEntity.getGbDgsWarnTimeQuantumName());
            }
            if (stockEntity.getGbDgsWasteFullTime() != null && !stockEntity.getGbDgsWasteFullTime().trim().isEmpty()) {
                depGoodsStockEntity.setGbDgsWasteFullTime(stockEntity.getGbDgsWasteFullTime());
                depGoodsStockEntity.setGbDgsWasteTimeQuantumName(stockEntity.getGbDgsWasteTimeQuantumName());
            }

            //sellingSubtotal
            if (order.getGbDoSellingPrice() != null && !order.getGbDoSellingPrice().trim().isEmpty()) {
                BigDecimal subtract = new BigDecimal(stockEntity.getGbDgsSellingPrice()).subtract(new BigDecimal(stockEntity.getGbDgsPrice()));
                BigDecimal divide = new BigDecimal(stockEntity.getGbDgsPrice()).divide(new BigDecimal(stockEntity.getGbDgsSellingPrice()), 2, BigDecimal.ROUND_HALF_UP);
                depGoodsStockEntity.setGbDgsSellingPrice(order.getGbDoSellingPrice());
                depGoodsStockEntity.setGbDgsBetweenPrice(subtract.toString());
                depGoodsStockEntity.setGbDgsCostRate(divide.toString());
                depGoodsStockEntity.setGbDgsSellingSubtotal(order.getGbDoSellingSubtotal());
            }
            gbDepartmentGoodsStockService.save(depGoodsStockEntity);
            updateDepDisGoods(depGoodsStockEntity, stockEntity.getGbDgsGbDepDisGoodsId(), "subtract");

        }

    }


    private void saveOutWeightStock(GbDepartmentOrdersEntity order) {
        System.out.println("ordldldlfd" + order.getOutGoodsStockEntityList().size());
        for (GbDepartmentGoodsStockEntity outStockEntity : order.getOutGoodsStockEntityList()) {
            //1，修改库房库存批次数据
            String gbDgsInventoryWeight = outStockEntity.getGbDgsInventoryWeight();
            String gbDgsPrice = outStockEntity.getGbDgsPrice();
            BigDecimal subtotal = new BigDecimal(gbDgsInventoryWeight).multiply(new BigDecimal(gbDgsPrice)).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbDepartmentGoodsStockService.update(outStockEntity);

            if (new BigDecimal(gbDgsInventoryWeight).compareTo(BigDecimal.ZERO) == 1) {
                //2,添加订单出库批次
                GbDepartmentGoodsStockEntity depGoodsStockEntity = new GbDepartmentGoodsStockEntity();
                depGoodsStockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
                depGoodsStockEntity.setGbDgsGbPurGoodsId(outStockEntity.getGbDgsGbPurGoodsId());
                depGoodsStockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
                depGoodsStockEntity.setGbDgsGbDisGoodsFatherId(order.getGbDoDisGoodsFatherId());
                depGoodsStockEntity.setGbDgsGbDisGoodsGrandId(order.getGbDoDisGoodsGrandId());
                depGoodsStockEntity.setGbDgsGbDisGoodsGreatId(order.getGbDoDisGoodsGreatId());
                depGoodsStockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
                depGoodsStockEntity.setGbDgsWeight(gbDgsInventoryWeight);
                depGoodsStockEntity.setGbDgsPrice(gbDgsPrice);
                depGoodsStockEntity.setGbDgsSubtotal(subtotal.toString());
                depGoodsStockEntity.setGbDgsRestSubtotal(subtotal.toString());
                depGoodsStockEntity.setGbDgsRestWeight(gbDgsInventoryWeight);
                depGoodsStockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
                depGoodsStockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
                depGoodsStockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
                depGoodsStockEntity.setGbDgsGbGoodsStockId(outStockEntity.getGbDepartmentGoodsStockId());
                depGoodsStockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
                depGoodsStockEntity.setGbDgsNxDistributerId(-1);
                depGoodsStockEntity.setGbDgsOutDate(formatWhatDay(0));
                depGoodsStockEntity.setGbDgsOutFullTime(formatFullTime());
                depGoodsStockEntity.setGbDgsOutHour(Integer.valueOf(formatWhatHour(0)));
                depGoodsStockEntity.setGbDgsStatus(-1);
                depGoodsStockEntity.setGbDgsLossWeight("0");
                depGoodsStockEntity.setGbDgsLossSubtotal("0");
                depGoodsStockEntity.setGbDgsReturnWeight("0");
                depGoodsStockEntity.setGbDgsReturnSubtotal("0");
                depGoodsStockEntity.setGbDgsProduceWeight("0");
                depGoodsStockEntity.setGbDgsProduceSubtotal("0");
                depGoodsStockEntity.setGbDgsWasteWeight("0");
                depGoodsStockEntity.setGbDgsWasteSubtotal("0");
                if (outStockEntity.getGbDgsWarnFullTime() != null) {
                    depGoodsStockEntity.setGbDgsWarnFullTime(outStockEntity.getGbDgsWarnFullTime());
                    depGoodsStockEntity.setGbDgsWarnTimeQuantumName(outStockEntity.getGbDgsWarnTimeQuantumName());
                }
                if (outStockEntity.getGbDgsWasteFullTime() != null) {
                    depGoodsStockEntity.setGbDgsWasteFullTime(outStockEntity.getGbDgsWasteFullTime());
                    depGoodsStockEntity.setGbDgsWasteTimeQuantumName(outStockEntity.getGbDgsWasteTimeQuantumName());
                }

                //sellingSubtotal
                if (order.getGbDoSellingPrice() != null && new BigDecimal(order.getGbDoSellingPrice()).compareTo(BigDecimal.ZERO) == 1) {
                    BigDecimal profit = new BigDecimal(order.getGbDoSellingPrice()).subtract(new BigDecimal(gbDgsPrice));
                    BigDecimal divide = new BigDecimal(gbDgsPrice).divide(new BigDecimal(order.getGbDoSellingPrice()), 2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal sellingSubtotal = new BigDecimal(order.getGbDoSellingPrice()).multiply(new BigDecimal(gbDgsInventoryWeight));
                    depGoodsStockEntity.setGbDgsBetweenPrice(profit.toString());
                    depGoodsStockEntity.setGbDgsCostRate(divide.toString());
                    depGoodsStockEntity.setGbDgsSellingPrice(order.getGbDoSellingPrice());
                    depGoodsStockEntity.setGbDgsSellingSubtotal(order.getGbDoSellingSubtotal());
                    depGoodsStockEntity.setGbDgsProfitSubtotal("0");
                    depGoodsStockEntity.setGbDgsProfitWeight("0");
                    depGoodsStockEntity.setGbDgsAfterProfitSubtotal("0");
                    depGoodsStockEntity.setGbDgsProduceSellingSubtotal("0");

                } else {
                    depGoodsStockEntity.setGbDgsSellingPrice("-1");
                }

                //weightGoods
                Integer gbDoWeightGoodsId = order.getGbDoWeightGoodsId();
                if (gbDoWeightGoodsId != null) {
                    depGoodsStockEntity.setGbDgsWeightGoodsId(gbDoWeightGoodsId);
                }

                gbDepartmentGoodsStockService.save(depGoodsStockEntity);

//                //update
                updateDepDisGoods(depGoodsStockEntity, outStockEntity.getGbDgsGbDepDisGoodsId(), "subtract");

            }
        }
    }


    private void saveOutWeightStockNoPrice(GbDepartmentOrdersEntity order) {
        System.out.println("ordldldlfd" + order.getOutGoodsStockEntityList().size());
        for (GbDepartmentGoodsStockEntity outStockEntity : order.getOutGoodsStockEntityList()) {
            //1，修改库房库存批次数据
            String gbDgsInventoryWeight = outStockEntity.getGbDgsInventoryWeight();
            gbDepartmentGoodsStockService.update(outStockEntity);

            if (new BigDecimal(gbDgsInventoryWeight).compareTo(BigDecimal.ZERO) == 1) {
                //2,添加订单出库批次
                GbDepartmentGoodsStockEntity depGoodsStockEntity = new GbDepartmentGoodsStockEntity();
                depGoodsStockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
                depGoodsStockEntity.setGbDgsGbPurGoodsId(outStockEntity.getGbDgsGbPurGoodsId());
                depGoodsStockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());
                depGoodsStockEntity.setGbDgsGbDisGoodsFatherId(order.getGbDoDisGoodsFatherId());
                depGoodsStockEntity.setGbDgsGbDisGoodsGrandId(order.getGbDoDisGoodsGrandId());
                depGoodsStockEntity.setGbDgsGbDisGoodsGreatId(order.getGbDoDisGoodsGreatId());
                depGoodsStockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
                depGoodsStockEntity.setGbDgsWeight(gbDgsInventoryWeight);
                if (order.getGbDoPrice() != null) {
                    BigDecimal gbDoPriceB = new BigDecimal(order.getGbDoPrice());
                    BigDecimal weightB = new BigDecimal(gbDgsInventoryWeight);
                    BigDecimal decimal = weightB.multiply(gbDoPriceB).setScale(1, BigDecimal.ROUND_HALF_UP);
                    depGoodsStockEntity.setGbDgsPrice(order.getGbDoPrice());
                    depGoodsStockEntity.setGbDgsSubtotal(decimal.toString());
                    depGoodsStockEntity.setGbDgsRestSubtotal(decimal.toString());


                }
                depGoodsStockEntity.setGbDgsRestWeight(gbDgsInventoryWeight);
                depGoodsStockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
                depGoodsStockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
                depGoodsStockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
                depGoodsStockEntity.setGbDgsGbGoodsStockId(outStockEntity.getGbDepartmentGoodsStockId());
                depGoodsStockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
                depGoodsStockEntity.setGbDgsNxDistributerId(-1);
                depGoodsStockEntity.setGbDgsOutDate(formatWhatDay(0));
                depGoodsStockEntity.setGbDgsOutFullTime(formatFullTime());
                depGoodsStockEntity.setGbDgsOutHour(Integer.valueOf(formatWhatHour(0)));
                depGoodsStockEntity.setGbDgsStatus(-1);
                depGoodsStockEntity.setGbDgsLossWeight("0");
                depGoodsStockEntity.setGbDgsLossSubtotal("0");
                depGoodsStockEntity.setGbDgsReturnWeight("0");
                depGoodsStockEntity.setGbDgsReturnSubtotal("0");
                depGoodsStockEntity.setGbDgsProduceWeight("0");
                depGoodsStockEntity.setGbDgsProduceSubtotal("0");
                depGoodsStockEntity.setGbDgsWasteWeight("0");
                depGoodsStockEntity.setGbDgsWasteSubtotal("0");
                if (outStockEntity.getGbDgsWarnFullTime() != null) {
                    depGoodsStockEntity.setGbDgsWarnFullTime(outStockEntity.getGbDgsWarnFullTime());
                    depGoodsStockEntity.setGbDgsWarnTimeQuantumName(outStockEntity.getGbDgsWarnTimeQuantumName());
                }
                if (outStockEntity.getGbDgsWasteFullTime() != null) {
                    depGoodsStockEntity.setGbDgsWasteFullTime(outStockEntity.getGbDgsWasteFullTime());
                    depGoodsStockEntity.setGbDgsWasteTimeQuantumName(outStockEntity.getGbDgsWasteTimeQuantumName());
                }

                //sellingSubtotal
                if (order.getGbDoSellingPrice() != null && new BigDecimal(order.getGbDoSellingPrice()).compareTo(BigDecimal.ZERO) == 1) {
//                    BigDecimal profit = new BigDecimal(order.getGbDoSellingPrice()).subtract(new BigDecimal(gbDgsPrice));
//                    BigDecimal divide = new BigDecimal(gbDgsPrice).divide(new BigDecimal(order.getGbDoSellingPrice()), 2, BigDecimal.ROUND_HALF_UP);
//                    BigDecimal sellingSubtotal = new BigDecimal(order.getGbDoSellingPrice()).multiply(new BigDecimal(gbDgsInventoryWeight));
//                    depGoodsStockEntity.setGbDgsBetweenPrice(profit.toString());
//                    depGoodsStockEntity.setGbDgsCostRate(divide.toString());
                    depGoodsStockEntity.setGbDgsSellingPrice(order.getGbDoSellingPrice());
                    depGoodsStockEntity.setGbDgsSellingSubtotal(order.getGbDoSellingSubtotal());
                    depGoodsStockEntity.setGbDgsProfitSubtotal("0");
                    depGoodsStockEntity.setGbDgsProfitWeight("0");
                    depGoodsStockEntity.setGbDgsAfterProfitSubtotal("0");
                    depGoodsStockEntity.setGbDgsProduceSellingSubtotal("0");

                } else {
                    depGoodsStockEntity.setGbDgsSellingPrice("-1");
                }

                //weightGoods
                Integer gbDoWeightGoodsId = order.getGbDoWeightGoodsId();
                if (gbDoWeightGoodsId != null) {
                    depGoodsStockEntity.setGbDgsWeightGoodsId(gbDoWeightGoodsId);
                }

                gbDepartmentGoodsStockService.save(depGoodsStockEntity);

//                //update
                updateDepDisGoodsNoPrice(depGoodsStockEntity, outStockEntity, "subtract");

            }
        }
    }


    @RequestMapping(value = "/getDisTodayOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getDisTodayOrders(Integer disId, String searchDepIds) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("equalStatus", -1);
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            List<String> idsGb = new ArrayList<>();
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map1.put("depFatherIds", idsGb);
                    map4.put("depFatherIds", idsGb);
                }
            }
        }


        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryFatherDepartment(map1);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("disId", disId);
                map.put("equalBuyStatus", 0);
                map.put("depFatherId", department.getGbDepartmentId());
                map.put("status", 1);
                System.out.println("newowowow" + map);
                Integer newAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
                department.setNewOrderAmount(newAmount.toString());
                Map<String, Object> map22 = new HashMap<>();
                map22.put("depFatherId", department.getGbDepartmentId());
                map22.put("dayuStatus", 0);
                map22.put("status", 2);
                Integer preAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map22);
                department.setPrepareOrderAmount(preAmount.toString());
            }
        }


        Integer amount = gbDepartmentBillService.queryBillsCountByParamsGb(map4);

        Map<String, Object> todayData = new HashMap<>();
        todayData.put("newAmount", amount);
        todayData.put("arr", departmentEntities);
        return R.ok().put("data", todayData);

    }

    @RequestMapping(value = "/getDisTodayFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getDisTodayFatherGoods(Integer disId, String searchDepIds, Integer status, Integer equalStatus,
                                    String startDate, String stopDate) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        if (equalStatus == -1) {
            map1.put("status", status);
        } else {
            map1.put("equalStatus", equalStatus);
        }
        map1.put("startDate", startDate);
        map1.put("stopDate", stopDate);
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            List<String> idsGb = new ArrayList<>();
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map1.put("depFatherIds", idsGb);
                }
            }
        }

        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepartmentOrdersService.queryFatherGoods(map1);

        return R.ok().put("data", fatherGoodsEntities);

    }


    @RequestMapping(value = "/getDisStockDepartment", method = RequestMethod.POST)
    @ResponseBody
    public R getDisStockDepartment(Integer toDepId, String orderType) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("notEqualDepFatherId", toDepId);
        map1.put("toDepId", toDepId);
        map1.put("equalStatus", -1);
//        map1.put("status", -1);
        map1.put("orderType", getGbOrderTypeTuihuo());
        System.out.println("mapap111111" + map1);
        List<GbDepartmentEntity> departmentEntitiesT = gbDepartmentOrdersService.queryFatherDepartment(map1);
        System.out.println("departmentEntitiesTdepartmentEntitiesT" + departmentEntitiesT.size());

        if (departmentEntitiesT.size() > 0) {
            for (GbDepartmentEntity department : departmentEntitiesT) {
                map1.put("depFatherId", department.getGbDepartmentId());
                System.out.println("mapap111111ddddddd" + map1);
                Integer newAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map1);
                department.setDepReturnGoodsTotalString(newAmount.toString());
            }
        }

        Map<String, Object> map11 = new HashMap<>();
        map11.put("toDepId", toDepId);
        map11.put("status", 3);
        map11.put("dayuBuyStatus", -1);
        map11.put("orderType", orderType);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryFatherDepartment(map11);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("toDepId", toDepId);
                map.put("equalBuyStatus", 0);
                map.put("depFatherId", department.getGbDepartmentId());
                map.put("status", 1);
                System.out.println("newowowow" + map);
                Integer newAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
                department.setNewOrderAmount(newAmount.toString());
                Map<String, Object> map22 = new HashMap<>();
                map22.put("toDepId", toDepId);
                map22.put("depFatherId", department.getGbDepartmentId());
                map22.put("buyStatus", 3);
                map22.put("status", 1);
                map22.put("dayuBuyStatus", 0);
                Integer preAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map22);
                department.setPrepareOrderAmount(preAmount.toString());
                Map<String, Object> map111 = new HashMap<>();
                map111.put("toDepId", toDepId);
                map111.put("depFatherId", department.getGbDepartmentId());
                map111.put("equalStatus", 2);
                Integer hasWeightAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map111);
                department.setHasWeightOrderAmount(hasWeightAmount.toString());
            }
        }


//        System.out.println("ddidid" + departmentEntitiesT.size());
        departmentEntitiesT.addAll(departmentEntities);


        Map<String, Object> map2 = new HashMap<>();
        map2.put("toDepId", toDepId);
        map2.put("buyStatus", 1);
        map2.put("dayuStatus", -1);
        map2.put("status", 3);
        map2.put("isSelf", 0);
        map2.put("isNotSelf", 1);
        System.out.println("map222" + map2);
        Integer amount2 = gbDepartmentOrdersService.queryTotalByParams(map2);

        map2.put("isSelf", 1);
        Integer amount3 = gbDepartmentOrdersService.queryTotalByParams(map2);

        Map<String, Object> todayData = new HashMap<>();
        todayData.put("isSelfAmount", amount3);
        todayData.put("arr", departmentEntitiesT);
        todayData.put("amount", amount2);

        return R.ok().put("data", todayData);

    }


    @RequestMapping(value = "/getDisKitchenDepartment/{toDepId}")
    @ResponseBody
    public R getDisKitchenDepartment(@PathVariable Integer toDepId) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("toDepId", toDepId);
        map1.put("status", 3);
        map1.put("orderType", getGbOrderTypeKitchen());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryFatherDepartment(map1);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("toDepId", toDepId);
                map.put("depFatherId", department.getGbDepartmentId());
                map.put("status", 1);
                map.put("equalBuyStatus", 0);
                Integer newAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
                department.setNewOrderAmount(newAmount.toString());
                Map<String, Object> map22 = new HashMap<>();
                map22.put("toDepId", toDepId);
                map22.put("depFatherId", department.getGbDepartmentId());
                map22.put("buyStatus", 3);
                map22.put("status", 1);
                map22.put("dayuBuyStatus", 0);
                Integer preAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map22);
                department.setPrepareOrderAmount(preAmount.toString());
                Map<String, Object> map111 = new HashMap<>();
                map111.put("toDepId", toDepId);
                map111.put("depFatherId", department.getGbDepartmentId());
                map111.put("equalStatus", 2);
                Integer hasWeightAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map111);
                department.setHasWeightOrderAmount(hasWeightAmount.toString());
                Double total = 0.0;
                if (hasWeightAmount > 0) {
                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("equalStatus", 2);
                    map2.put("depFatherId", department.getGbDepartmentId());
                    map2.put("toDepId", toDepId);
                    total = gbDepartmentOrdersService.queryGbOrdersSellingSubtotal(map2);
                }
                department.setUpdateSubtotal(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
        }

        Map<String, Object> map4 = new HashMap<>();
        map4.put("depId", toDepId);
        map4.put("equalStatus", -1);
        Integer amount = gbDepartmentBillService.queryBillsCountByParamsGb(map4);

        Map<String, Object> todayData = new HashMap<>();
        todayData.put("newAmount", amount);
        todayData.put("arr", departmentEntities);


        Map<String, Object> map2 = new HashMap<>();
        map2.put("depFatherId", toDepId);
        map2.put("toDepId", toDepId);
        map2.put("status", 3);
        map2.put("dayuStatus", -1);
        Integer amount2 = gbDepartmentOrdersService.queryTotalByParams(map2);
        todayData.put("applyAmount", amount2);


        return R.ok().put("data", todayData);

    }

    /**
     * 批发商给客户添加申请
     *
     * @param depFatherId 客户id
     * @return 订单
     */
    @RequestMapping(value = "/disGetDepTodayOrdersGb/{depFatherId}")
    @ResponseBody
    public R disGetDepTodayOrdersGb(@PathVariable Integer depFatherId) {
        System.out.println("zhemeeke" + depFatherId);

        //今天的数据
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("status", 3);
        map.put("orderBy", "time");
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.disQueryDisOrdersByParams(map);
        if ((ordersEntities.size() > 0)) {
            System.out.println("oreenmesiisis" + ordersEntities.size());
            return R.ok().put("data", ordersEntities);
        }
        return R.error(-1, "没有订单");
    }

    @RequestMapping(value = "/disGetToDepTodayOrdersGb/{depFatherId}")
    @ResponseBody
    public R disGetToDepTodayOrdersGb(@PathVariable Integer depFatherId) {
        System.out.println("zhemeeke" + depFatherId);

        //今天的数据
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", depFatherId);
        map.put("status", 3);
        map.put("orderBy", "time");
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.disQueryDisOrdersByParams(map);
        if ((ordersEntities.size() > 0)) {
            System.out.println("oreenmesiisis" + ordersEntities.size());
            return R.ok().put("data", ordersEntities);
        }
        return R.error(-1, "没有订单");
    }


    @RequestMapping(value = "/webGetOrderPage", method = RequestMethod.POST)
    @ResponseBody
    public R webGetOrderPage(Integer depFatherId,
                             String orderBy, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("orderBy", orderBy);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("orderbyy" + map);

        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.disQueryDisOrdersByParams(map);

        //查询列表数据

//        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("depFatherId", depFatherId);
        mapC.put("status", 3);
        System.out.println("totootoacaoaooa" + mapC);
        Integer integerC = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(mapC);
        PageUtils pageUtil = new PageUtils(ordersEntities, integerC, limit, page);


        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depFatherId);
        map3.put("date", formatWhatDay(0));
        int count = gbDepartmentBillService.queryDepartmentBillCount(map3);
        int trade = count + 1;
        String no = "";
        if (trade < 100) {
            if (count < 10) {
                no = "00" + trade;
            } else {
                no = "0" + trade;
            }
        } else {
            no = String.valueOf(count);
        }

        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depFatherId);
        String headPinyin = getHeadStringByString(gbDepartmentEntity.getGbDepartmentName(), true, null);
        String s = formatDayNumber(0) + headPinyin + no;
        double total = 0.0;

        map.put("subtotal", 0);
        Integer twoTotal = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
        if (twoTotal > 0) {
            total = gbDepartmentOrdersService.queryGbOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("tradeNo", s);
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/webGetToDepOrderPage", method = RequestMethod.POST)
    @ResponseBody
    public R webGetToDepOrderPage(Integer depFatherId,
                                  String orderBy, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("toDepId", depFatherId);
        map.put("orderBy", orderBy);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("orderbyy" + map);

        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.disQueryDisOrdersByParams(map);

        //查询列表数据
        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);


        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depFatherId);
        map3.put("date", formatWhatDay(0));
        int count = gbDepartmentBillService.queryDepartmentBillCount(map3);
        int trade = count + 1;
        String no = "";
        if (trade < 100) {
            if (count < 10) {
                no = "00" + trade;
            } else {
                no = "0" + trade;
            }
        } else {
            no = String.valueOf(count);
        }

        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depFatherId);
        String headPinyin = getHeadStringByString(gbDepartmentEntity.getGbDepartmentName(), true, null);
        String s = formatDayNumber(0) + headPinyin + no;
        double total = 0.0;

        map.put("subtotal", 0);
        Integer twoTotal = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
        if (twoTotal > 0) {
            total = gbDepartmentOrdersService.queryGbOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("tradeNo", s);
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/mendianGetDriveApplyGb/{depId}")
    @ResponseBody
    public R mendianGetDriveApplyGb(@PathVariable Integer depId) {

        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depId);
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("arr", new ArrayList<>());

        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntities) {
                //明天的数据
                Map<String, Object> map31 = new HashMap<>();
                map31.put("depId", dep.getGbDepartmentId());
                map31.put("orderBy", "time");
                map31.put("status", 3);
                map31.put("orderTypeNotEqual", getGbOrderTypeZiCai());
                List<GbDepartmentOrdersEntity> ordersEntities3 = gbDepartmentOrdersService.queryMendianDisOrdersByParams(map31);
                if (ordersEntities3.size() > 0) {
                    Map<String, Object> map4 = new HashMap<>();
                    map4.put("depName", dep.getGbDepartmentName());
                    map4.put("arr", ordersEntities3);
                    list.add(map4);
                    map.put("arr", list);
                }

            }
        } else {
            //明天的数据
            Map<String, Object> map3 = new HashMap<>();
            map3.put("depId", depId);
            map3.put("orderBy", "time");
            map3.put("status", 3);
            map3.put("orderTypeNotEqual", getGbOrderTypeZiCai());
            System.out.println("kankana333" + map3);

            List<GbDepartmentOrdersEntity> ordersEntities3 = gbDepartmentOrdersService.queryMendianDisOrdersByParams(map3);
            map.put("arr", ordersEntities3);
        }


        Map<String, Object> map4 = new HashMap<>();
        map4.put("depId", depId);
        map4.put("equalStatus", -1);
        Integer amount = gbDepartmentBillService.queryBillsCountByParamsGb(map4);
        map.put("newAmount", amount);
        return R.ok().put("data", map);

    }


    @RequestMapping(value = "/supplierGetDepApply", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGetApplyGb(Integer depFatherId, Integer supplierId) {
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depFatherId);
        Integer gbDepartmentSubAmount = departmentEntity.getGbDepartmentSubAmount();
        if (gbDepartmentSubAmount > 0) {
            List<GbDepartmentEntity> gbSubDepartments = gbDepartmentService.querySubDepartments(departmentEntity.getGbDepartmentId());
            if (gbSubDepartments.size() > 0) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (GbDepartmentEntity dep : gbSubDepartments) {
                    Map<String, Object> map3 = new HashMap<>();
                    map3.put("supplierId", supplierId);
                    map3.put("depId", dep.getGbDepartmentId());
                    map3.put("orderBy", "time");
                    map3.put("status", 4);
                    List<GbDepartmentOrdersEntity> ordersEntities3 = gbDepartmentOrdersService.queryDisOrdersByParams(map3);
                    if ((ordersEntities3.size() > 0)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("depName", dep.getGbDepartmentName());
                        map.put("depOrders", ordersEntities3);
                        result.add(map);
                    }
                }
                return R.ok().put("data", result);
            } else {
                return R.error(-1, "没有订单000");
            }

        } else {
            Map<String, Object> map3 = new HashMap<>();
            map3.put("supplierId", supplierId);
            map3.put("depFatherId", depFatherId);
            map3.put("orderBy", "time");
            map3.put("status", 4);
            List<GbDepartmentOrdersEntity> ordersEntities3 = gbDepartmentOrdersService.queryDisOrdersByParams(map3);
            if ((ordersEntities3.size() > 0)) {
                return R.ok().put("data", ordersEntities3);
            } else {
                return R.error(-1, "没有订单1234");
            }
        }
    }

    @RequestMapping(value = "/stockGetDepApplyGbWindow")
    @ResponseBody
    public R stockGetDepApplyGbWindow(Integer depFatherId, Integer toDepId, Integer orderType) {

        Map<String, Object> map3 = new HashMap<>();
        map3.put("toDepId", toDepId);
        map3.put("depFatherId", depFatherId);
        map3.put("status", 3);
        map3.put("orderType", orderType);
        System.out.println("mapd3333" + map3);
        List<GbDistributerFatherGoodsEntity> ordersEntities3 = gbDepartmentOrdersService.stockGetDepApply(map3);
        System.out.println("siziiziz" + ordersEntities3.size());
        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();

        if ((ordersEntities3.size() > 0)) {
            for (GbDistributerFatherGoodsEntity father : ordersEntities3) {
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = father.getFatherGoodsEntities();
                result.addAll(fatherGoodsEntities);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("arr", result);
            return R.ok().put("data", map);
        } else {
            return R.error(-1, "没有订单");

        }

    }


    @RequestMapping(value = "/stockGetDepApplyGb")
    @ResponseBody
    public R stockGetDepApplyGb(Integer depFatherId, Integer toDepId, Integer orderType) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("toDepId", toDepId);
        map3.put("depFatherId", depFatherId);
        map3.put("status", 3);
        map3.put("orderType", orderType);
        List<GbDistributerFatherGoodsEntity> ordersEntities3 = gbDepartmentOrdersService.stockGetDepApply(map3);
        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();

        if ((ordersEntities3.size() > 0)) {
            for (GbDistributerFatherGoodsEntity father : ordersEntities3) {
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = father.getFatherGoodsEntities();
                result.addAll(fatherGoodsEntities);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("arr", result);
//            map.put("total", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP));

            return R.ok().put("data", map);
        } else {
            return R.error(-1, "没有订单");

        }

    }


    @RequestMapping(value = "/webStockGetDepApplyGb")
    @ResponseBody
    public R webStockGetDepApplyGb(Integer depFatherId, Integer toDepId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("toDepId", toDepId);
        map3.put("depFatherId", depFatherId);
        map3.put("status", 3);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryMendianDisOrdersByParams(map3);


        double total = 0.0;
        map3.put("hasWeight", 1);
        Integer integer = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map3);
        if (integer > 0) {
            total = gbDepartmentOrdersService.queryGbOrdersSubtotal(map3);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("arr", ordersEntities);
        map.put("subtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

        return R.ok().put("data", map);

    }


    @RequestMapping(value = "/webStockGetDepApplyGbPage")
    @ResponseBody
    public R webStockGetDepApplyGbPage(Integer depFatherId, Integer toDepId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", toDepId);
        map.put("depFatherId", depFatherId);
        map.put("status", 3);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("webebeboroorooroor" + map);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryMendianDisOrdersByParams(map);
        System.out.println("ororosiisi" + ordersEntities.size());
        //查询列表数据
        double total = 0.0;
        map.put("hasWeight", 1);
        Integer integer = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(map);
        if (integer > 0) {
            total = gbDepartmentOrdersService.queryGbOrdersSubtotal(map);
        }

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("toDepId", toDepId);
        mapC.put("depFatherId", depFatherId);
        mapC.put("status", 3);
        System.out.println("totootoacaoaooa" + mapC);
        Integer integerC = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(mapC);

        PageUtils pageUtil = new PageUtils(ordersEntities, integerC, limit, page);


        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depFatherId);
        map3.put("date", formatWhatDay(0));
        int count = gbDepartmentBillService.queryDepartmentBillCount(map3);
        int trade = count + 1;
        String no = "";
        if (trade < 100) {
            if (count < 10) {
                no = "00" + trade;
            } else {
                no = "0" + trade;
            }
        } else {
            no = String.valueOf(count);
        }
        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depFatherId);
        String headPinyin = getHeadStringByString(gbDepartmentEntity.getGbDepartmentName(), true, null);
        String s = formatDayNumber(0) + headPinyin + no;

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("tradeNo", s);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/stockGetDepApplyGbIsSelfPrint")
    @ResponseBody
    public R stockGetDepApplyGbIsSelfPrint(Integer depFatherId, Integer toDepId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("toDepId", toDepId);
        map3.put("depFatherId", depFatherId);
        map3.put("orderBy", "time");
        map3.put("status", 3);
        map3.put("weightId", -1);
        List<GbDistributerFatherGoodsEntity> ordersEntities3 = gbDepartmentOrdersService.stockGetDepApply(map3);
        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();

        if ((ordersEntities3.size() > 0)) {
            for (GbDistributerFatherGoodsEntity father : ordersEntities3) {
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = father.getFatherGoodsEntities();
                result.addAll(fatherGoodsEntities);
            }

            return R.ok().put("data", result);
        } else {
            return R.error(-1, "没有订单");

        }
    }

    @RequestMapping(value = "/stockGetDepApplyGbIsSelf")
    @ResponseBody
    public R stockGetDepApplyGbIsSelf(Integer depFatherId, Integer toDepId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("toDepId", toDepId);
        map3.put("depFatherId", depFatherId);
        map3.put("orderBy", "time");
        map3.put("status", 3);
        map3.put("isSelf", 1);
        List<GbDistributerFatherGoodsEntity> ordersEntities3 = gbDepartmentOrdersService.stockGetDepApply(map3);
        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();

        if ((ordersEntities3.size() > 0)) {
            for (GbDistributerFatherGoodsEntity father : ordersEntities3) {
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = father.getFatherGoodsEntities();
                result.addAll(fatherGoodsEntities);
            }

            return R.ok().put("data", result);
        } else {
            return R.error(-1, "没有订单");

        }
    }


    @RequestMapping(value = "/depGetApplyAiByTime/{depFatherId}")
    @ResponseBody
    public R depGetApplyAiByTime(@PathVariable Integer depFatherId) {

        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(depFatherId);
        System.out.println("depdiidid" + entities.size());
        if (entities.size() > 0) {
            for (GbDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getGbDepartmentId());
                mapDep.put("depName", dep.getGbDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getGbDepartmentId());
                map1.put("orderTypeNotEqual", 9);
                System.out.println("ordereree" + map1);
                List<GbDepartmentOrdersEntity> depOrders = gbDepartmentOrdersService.queryDisOrdersListByParams(map1);
                mapDep.put("depOrders", depOrders);
                mapDep.put("depInfo", gbDepartmentService.queryDepInfoGb(dep.getGbDepartmentId()));
                System.out.println("orddddiziiziiziz" + mapDep);

                mapList.add(mapDep);
            }

            System.out.println("maplsisiisisisi" + mapList);
            mapR.put("arr", mapList);
            return R.ok().put("data", mapR);

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 3);
            map.put("depFatherId", depFatherId);
            map.put("orderTypeNotEqual", 9);
            System.out.println("abncnncnnnc" + map);
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);

            mapR.put("arr", ordersEntities);
            mapR.put("depInfo", gbDepartmentService.queryDepInfoGb(depFatherId));

            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/subDepGetApplyAiByTime/{depId}")
    @ResponseBody
    public R subDepGetApplyAiByTime(@PathVariable Integer depId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depId", depId);
        map.put("orderTypeNotEqual", 9);
        System.out.println("abncnncnnnc" + map);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);

        mapR.put("arr", ordersEntities);
        mapR.put("depInfo", gbDepartmentService.queryDepInfoGb(depId));

        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/subDepGetApplyAiFather/{depId}")
    @ResponseBody
    public R subDepGetApplyAiFather(@PathVariable Integer depId) {

        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depId", depId);
        map.put("orderTypeNotEqual", 9);
        List<GbDistributerFatherGoodsEntity> gbDistributerFatherGoodsEntities = gbDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("arr", gbDistributerFatherGoodsEntities);
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/depGetApplyAiFather/{depFatherId}")
    @ResponseBody
    public R depGetApplyAiFather(@PathVariable Integer depFatherId) {

        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(depFatherId);
        System.out.println("depdiididdepGetApplyAiFather" + entities.size());
        if (entities.size() > 0) {
            for (GbDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getGbDepartmentId());
                mapDep.put("depName", dep.getGbDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getGbDepartmentId());
                map1.put("orderTypeNotEqual", 9);
                List<GbDistributerFatherGoodsEntity> gbDistributerFatherGoodsEntities = gbDepartmentOrdersService.queryGrandGoodsOrder(map1);
                mapDep.put("depOrders", gbDistributerFatherGoodsEntities);
                System.out.println("aadddddd" + gbDistributerFatherGoodsEntities.size());
                mapList.add(mapDep);
            }
            mapR.put("arr", mapList);
            return R.ok().put("data", mapR);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 3);
            map.put("depFatherId", depFatherId);
            map.put("orderTypeNotEqual", 9);
            List<GbDistributerFatherGoodsEntity> gbDistributerFatherGoodsEntities = gbDepartmentOrdersService.queryGrandGoodsOrder(map);

            mapR.put("arr", gbDistributerFatherGoodsEntities);


            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/depGetApplyGb/{depId}")
    @ResponseBody
    public R depGetApplyGb(@PathVariable Integer depId) {
        Map<String, Object> mapresult = new HashMap<>();
        mapresult.put("bill", -1);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depId);
        map3.put("status", 3);
        List<GbDepartmentOrdersEntity> ordersEntities3 = gbDepartmentOrdersService.queryDisOrdersByParams(map3);
        mapresult.put("arr", ordersEntities3);


        Map<String, Object> map4 = new HashMap<>();
        map4.put("depId", depId);
        map4.put("equalStatus", -1);
        System.out.println("abdkdkdkdd" + map4);
        List<GbDepartmentBillEntity> billEntities1 = gbDepartmentBillService.queryBillFromWhichDepartment(map4);
        mapresult.put("receiveBills", billEntities1);


//
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("orderType", 5);
        map.put("willDate", formatWhatDay(0));
        map.put("status", 4);
        System.out.println("mapappapapapp" + map);
        List<GbDepartmentBillEntity> billEntities = gbDepartmentBillService.queryDepartmentBillList(map);
        if (billEntities.size() > 0) {
            mapresult.put("bill", billEntities.get(0));
        }

        return R.ok().put("data", mapresult);
    }


    /**
     * DISTRIBUTER
     * 批发商获取今日订货群和订单
     *
     * @param supplierId gonghuoshang商id
     * @return 订货群
     */
    @RequestMapping(value = "/supplierGetTodayOrderCustomer/{supplierId}")
    @ResponseBody
    public R supplierGetTodayOrderCustomer(@PathVariable Integer supplierId) {
        GbDistributerSupplierEntity disSupplierEntity = gbDistributerSupplierService.queryObject(supplierId);
        Integer gbDsOrderType = disSupplierEntity.getGbDsOrderType();
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("supplierId", supplierId);
        map1.put("status", 4);
        map1.put("orderType", gbDsOrderType);
        map1.put("toDepId", disSupplierEntity.getGbDsGbDepartmentId());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryDistributerTodayDepartments(map1);
        Map<String, Object> todayData = packageDisOrderByDep(departmentEntities, 0);
        return R.ok().put("data", todayData);
    }


    /**
     * DISTRIBUTER -- 老的页面格式，废弃
     * 批发商获取今日订货群和订单
     *
     * @param disId 批发商id
     * @return 订货群
     */
    @RequestMapping(value = "/disGetTodayOrderCustomerGb/{disId}")
    @ResponseBody
    public R disGetTodayOrderCustomerGb(@PathVariable Integer disId) {

        List<Map<String, Object>> returnData = new ArrayList<>();
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 4);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentOrdersService.queryDistributerTodayDepartments(map1);
        Map<String, Object> todayData = packageDisOrderByDep(departmentEntities, 0);

        if (departmentEntities.size() > 0) {
            Map<String, Object> map3 = new HashMap<>();
            map3.put("disId", disId);
            map3.put("equalBuyStatus", 0);
            map3.put("status", 3);
            map3.put("purchaseAuto", 0);
            List<GbDepartmentOrdersEntity> tomUnPurchaseApplys = gbDepartmentOrdersService.queryDisOrdersByParams(map3);
            todayData.put("buyOrders", tomUnPurchaseApplys.size());
            returnData.add(todayData);
        }
        return R.ok().put("data", returnData);
    }

    private Map<String, Object> packageDisOrderByDep(List<GbDepartmentEntity> departmentEntities, Integer which) {
        Map<String, Object> map = new HashMap<>();
        map.put("week", getWeek(which));
        map.put("hao", getJustHao(which));
        //根据部门是否有子部门组装部门订单
        //1,返回list
        List<Map<String, Object>> dataMap = new ArrayList<>();
        //2,有子部门的父部门
        TreeSet<GbDepartmentEntity> fatherDep = new TreeSet<>();
        //3，type是1的部门
        List<GbDepartmentEntity> subDepList = new ArrayList<>();

        for (GbDepartmentEntity dep : departmentEntities) {

            Integer fatherId = dep.getGbDepartmentFatherId();
            if (fatherId.equals(0)) {
                Map<String, Object> depMap = new HashMap<>();
                depMap.put("depHasSubs", 0);
                depMap.put("depId", dep.getGbDepartmentId());
                depMap.put("depName", dep.getGbDepartmentName());
                depMap.put("arrName", dep.getGbDepartmentAttrName());
                depMap.put("depOrders", dep.getGbDepartmentOrdersEntities());
                depMap.put("orderTotal", dep.getGbDepartmentOrdersEntities().size());
                depMap.put("choiceTotal", dep.getGbDepartmentOrdersEntities().size());
                dataMap.add(depMap);
            } else {
                Integer gbDepartmentFatherId = dep.getGbDepartmentFatherId();
                GbDepartmentEntity departmentEntity1 = gbDepartmentService.queryObject(gbDepartmentFatherId);
                fatherDep.add(departmentEntity1);
                subDepList.add(dep);
            }
        }

        for (GbDepartmentEntity father : fatherDep) {
            Map<String, Object> fatherMap = new HashMap<>();
            fatherMap.put("depHasSubs", 1);
            fatherMap.put("depId", father.getGbDepartmentId());
            fatherMap.put("depName", father.getGbDepartmentName());
            fatherMap.put("arrName", father.getGbDepartmentAttrName());
            int orderTotal = 0;

            List<GbDepartmentEntity> subDeps = new ArrayList<>();
            for (GbDepartmentEntity sub : subDepList) {
                if (father.getGbDepartmentId().equals(sub.getGbDepartmentFatherId())) {
                    subDeps.add(sub);
                    List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = sub.getGbDepartmentOrdersEntities();
                    int size = gbDepartmentOrdersEntities.size();
                    orderTotal = orderTotal + size;
                }

            }
            fatherMap.put("subDeps", subDeps);
            fatherMap.put("orderTotal", orderTotal);
            fatherMap.put("choiceTotal", orderTotal);
            dataMap.add(fatherMap);
        }
        map.put("arr", dataMap);
        return map;
    }


    @ResponseBody
    @RequestMapping("/chuKuDepSaveOrdersGb")
    public R chuKuDepSaveOrdersGb(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {

        // add purchaseGoods
        Integer gbDoDisGoodsId = gbDepartmentOrders.getGbDoDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        GbDistributerPurchaseGoodsEntity gbPurchaseGoodsEntity = new GbDistributerPurchaseGoodsEntity();
        //查询是否有采购的同一个商品
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
        if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null && gbDistributerGoodsEntity.getGbDgGbSupplierId() != -1) {
            map.put("status", 2); //有供货商的商品
        } else {
            map.put("equalStatus", 0); //没有供货商的商品
        }
        map.put("purDepId", gbDepartmentOrders.getGbDoToDepartmentId());
        map.put("standard", gbDepartmentOrders.getGbDoStandard());
        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);

        if (goodsEntities.size() == 0) {
            //是个新采购商品
            gbPurchaseGoodsEntity.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
            gbPurchaseGoodsEntity.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
            gbPurchaseGoodsEntity.setGbDpgApplyDate(formatWhatDay(0));
            gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(1);
            gbPurchaseGoodsEntity.setGbDpgOrdersFinishAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersWeightAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersBillAmount(0);
            gbPurchaseGoodsEntity.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
            gbPurchaseGoodsEntity.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
            gbPurchaseGoodsEntity.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
            gbPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseType(gbDepartmentOrders.getGbDoOrderType());

            System.out.println("nenenenneennenene" + gbPurchaseGoodsEntity.getGbDpgQuantity());
            gbDistributerPurchaseGoodsService.save(gbPurchaseGoodsEntity);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
        } else {
            //给老采购商品添加新订单
            gbPurchaseGoodsEntity = goodsEntities.get(0);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
            //采购商品订单数量更新
            Integer gbDpgOrdersAmount = gbPurchaseGoodsEntity.getGbDpgOrdersAmount();
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
            BigDecimal purQuantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
            BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
            BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbPurchaseGoodsEntity.setGbDpgQuantity(add.toString());
            gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);
        }


        gbDepartmentOrdersService.save(gbDepartmentOrders);

        //给autoBatch更新gbDepartmentOrderid
        if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null && gbDistributerGoodsEntity.getGbDgGbSupplierId() != -1) {
            Map<String, Object> mapData = autoAddPurchaseBatch(gbDepartmentOrders, gbDistributerGoodsEntity);
            Integer purUserId = (Integer) mapData.get("purUserId");
            gbDepartmentOrders.setGbDoPurchaseUserId(purUserId);
            gbDepartmentOrders.setGbDoBuyStatus(getGbOrderBuyStatusProcurement());
            gbDepartmentOrdersService.update(gbDepartmentOrders);
        }
        return R.ok().put("data", gbDepartmentOrdersService.queryObject(gbDepartmentOrders.getGbDepartmentOrdersId()));
    }


    @ResponseBody
    @RequestMapping("/saveOrdersGbJj")
    public R saveOrdersGbJj(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {

        System.out.println("autottototot" + gbDepartmentOrders);

        if (gbDepartmentOrders.getStockIsZero()) {
            clearDepGoodsStock(gbDepartmentOrders);
        }

        Integer gbDoDisGoodsId = gbDepartmentOrders.getGbDoDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);

        gbDepartmentOrders.setGbDoNxGoodsId(gbDistributerGoodsEntity.getGbDgNxGoodsId());
        gbDepartmentOrders.setGbDoNxGoodsFatherId(gbDistributerGoodsEntity.getGbDgNxFatherId());
        gbDepartmentOrders.setGbDoDistributerId(gbDistributerGoodsEntity.getGbDgDistributerId());
        gbDepartmentOrders.setGbDoToDepartmentId(gbDistributerGoodsEntity.getGbDgGbDepartmentId());
        gbDepartmentOrders.setGbDoOrderType(gbDistributerGoodsEntity.getGbDgGoodsType());
        gbDepartmentOrders.setGbDoGoodsType(gbDistributerGoodsEntity.getGbDgGoodsType());
        gbDepartmentOrders.setGbDoPurchaseGoodsId(-1);
        gbDepartmentOrders.setGbDoGoodsName(gbDistributerGoodsEntity.getGbDgGoodsName());
        gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
        gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
        Integer gbDoDisGoodsFatherId = gbDepartmentOrders.getGbDoDisGoodsFatherId();
        GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(gbDoDisGoodsFatherId);
        gbDepartmentOrders.setGbDoDisGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        gbDepartmentOrders.setGbDoDisGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
        Integer gbDfgFathersFatherId = fatherGoodsEntity.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity grandFather = gbDistributerFatherGoodsService.queryObject(gbDfgFathersFatherId);
        Integer greatFatherId = grandFather.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity greatFather = gbDistributerFatherGoodsService.queryObject(greatFatherId);
        gbDepartmentOrders.setGbDoDisGoodsGreatId(greatFatherId);
        gbDepartmentOrders.setGbDoNxGoodsGrandId(grandFather.getGbDfgNxGoodsId());
        gbDepartmentOrders.setGbDoNxGoodsGreatId(greatFather.getGbDfgNxGoodsId());
        gbDepartmentOrdersService.save(gbDepartmentOrders);

        //查询是否有采购的同一个商品
        Map<String, Object> map = new HashMap<>();
        map.put("standard", gbDepartmentOrders.getGbDoStandard());
        map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
        GbDistributerPurchaseGoodsEntity gbPurchaseGoodsEntity = new GbDistributerPurchaseGoodsEntity();
        if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null && gbDistributerGoodsEntity.getGbDgGbSupplierId() != -1) {
            map.put("supplierId", gbDistributerGoodsEntity.getGbDgGbSupplierId());
            map.put("status", 2); //有供货商的商品
        } else {
            map.put("equalStatus", 0); //没有供货商的商品
        }

        System.out.println("putgodosmap" + map);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);

        if (purchaseGoodsEntities.size() == 0) {
            //是个新采购商品
            gbPurchaseGoodsEntity.setGbDpgPurchaseType(gbDepartmentOrders.getGbDoGoodsType());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
            gbPurchaseGoodsEntity.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
            gbPurchaseGoodsEntity.setGbDpgApplyDate(formatWhatDay(0));
            gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(1);
            gbPurchaseGoodsEntity.setGbDpgOrdersFinishAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersWeightAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersBillAmount(0);
            gbPurchaseGoodsEntity.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
            gbPurchaseGoodsEntity.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
            gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbDepartmentOrders.getGbDoQuantity());
            gbPurchaseGoodsEntity.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
            gbPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(gbDistributerGoodsEntity.getGbDgGbDepartmentId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxSupplierId(gbDistributerGoodsEntity.getGbDgGbSupplierId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsGrandId(gbDistributerGoodsEntity.getGbDgDfgGoodsGrandId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsGreatId(gbDistributerGoodsEntity.getGbDgDfgGoodsGreatId());
            //standard Same
            if (gbDistributerGoodsEntity.getGbDgGoodsStandardname().equals(gbDepartmentOrders.getGbDoStandard())) {
                gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbDepartmentOrders.getGbDoQuantity());
            }
            gbDistributerPurchaseGoodsService.save(gbPurchaseGoodsEntity);

            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
        } else {
            //给老采购商品添加新订单
            System.out.println("newownwgopuggog");
            gbPurchaseGoodsEntity = purchaseGoodsEntities.get(0);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
            //采购商品订单数量更新
            Integer gbDpgOrdersAmount = gbPurchaseGoodsEntity.getGbDpgOrdersAmount();
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
            BigDecimal purQuantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
            BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
            BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbPurchaseGoodsEntity.setGbDpgQuantity(add.toString());
            //standard Same
            if (gbDistributerGoodsEntity.getGbDgGoodsStandardname().equals(gbDepartmentOrders.getGbDoStandard())) {
                gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbDepartmentOrders.getGbDoQuantity());
            }
            gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);
        }

        Integer gbDoDepartmentFatherId = gbDepartmentOrders.getGbDoDepartmentFatherId();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(gbDoDepartmentFatherId);

        if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null && gbDistributerGoodsEntity.getGbDgGbSupplierId() != -1) {

            Map<String, Object> mapData = autoAddPurchaseBatch(gbDepartmentOrders, gbDistributerGoodsEntity);
            Integer batchId = (Integer) mapData.get("batchId");
            Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDepartmentDisId);

            NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDistributerGoodsEntity.getGbDgGbSupplierId());

            if (supplierEntity.getNxJrdhsUserId() != null) {
                Map<String, TemplateData> mapNotice = new HashMap<>();
                mapNotice.put("time2", new TemplateData(formatWhatDayTime(0)));
                mapNotice.put("thing13", new TemplateData(departmentEntity.getGbDepartmentName()));
                mapNotice.put("thing8", new TemplateData(gbDistributerGoodsEntity.getGbDgGoodsName()));
                mapNotice.put("thing10", new TemplateData("订货"));
                Integer gbDoOrderUserId = gbDepartmentOrders.getGbDoOrderUserId();
                GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(gbDoOrderUserId);
                mapNotice.put("thing9", new TemplateData(gbDepartmentUserEntity.getGbDuWxNickName()));
                System.out.println("nociiciiiicicautotootototoototo" + mapNotice);
                Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);

                StringBuilder pathBuilder = new StringBuilder("subPackage/pages/gbMarket/gbReceiveBatch/gbReceiveBatch");
                pathBuilder.append("?batchId=").append(batchId);
                pathBuilder.append("&retName=").append(gbDistributerEntity.getGbDistributerName());
                pathBuilder.append("&from=notification"); // 添加这个参数
                String path = pathBuilder.toString();
                System.out.println("Encoded URLARRRRRRRRRR00000000saveOrdersGbJj: " + path);
                WeNoticeService.autoGbSuppliertixingMessageJj(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);
            }

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("supplierId", supplierEntity.getNxJrdhSupplierId());
            mapR.put("gbDisId", gbDepartmentOrders.getGbDoDistributerId());
            mapR.put("nxDisId", supplierEntity.getNxJrdhsNxDistributerId());
            System.out.println("nxdidisiorororooror111" + mapR);
            NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDisGbDisService.queryObjectByParams(mapR);
            if (nxDistributerGbDistributerEntity != null) {
                GbDistributerPurchaseBatchEntity batchEntity = gbDPBService.queryObject(batchId);
                batchEntity.setGbDpbPurchaseType(5);
                batchEntity.setGbDpbNxDistributerId(nxDistributerGbDistributerEntity.getNxDgdNxDistributerId());
                gbDPBService.update(batchEntity);
                transToNxData(gbDistributerGoodsEntity, gbDepartmentOrders, supplierEntity.getNxJrdhsNxDistributerId());

            }

        }


        gbDepartmentOrdersService.update(gbDepartmentOrders);
        GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrders.getGbDepartmentOrdersId());
        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disGoodsId", gbDoDisGoodsId);
        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryDisGoodsWithDepDisGoods(mapG);
        gbDepartmentOrdersEntity.setGbDistributerGoodsEntity(goodsEntity);

//        //
//        Map<String, TemplateData> mapNotice = new HashMap<>();
//        mapNotice.put("time2", new TemplateData(formatWhatDayTime(0)));
//
//        mapNotice.put("thing13", new TemplateData(departmentEntity.getGbDepartmentName()));
//        mapNotice.put("thing8", new TemplateData(gbDistributerGoodsEntity.getGbDgGoodsName()));
//        mapNotice.put("thing10", new TemplateData("订货"));
//        Integer gbDoOrderUserId = gbDepartmentOrders.getGbDoOrderUserId();
//        GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(gbDoOrderUserId);
//        mapNotice.put("thing9", new TemplateData(gbDepartmentUserEntity.getGbDuWxNickName()));
//        System.out.println("nociiciiiicicautotootototoototo" + mapNotice);
//
//        StringBuilder pathBuilderAll = new StringBuilder("subPackage/pages/gbMarket/gbReceiveBatch/gbReceiveBatch");
//        pathBuilderAll.append("?batchId=").append(1);
//
//        pathBuilderAll.append("&retName=").append("abc");
//        pathBuilderAll.append("&disId=").append(1);
//        pathBuilderAll.append("&buyUserId=").append(1);
//        pathBuilderAll.append("&fromBuyer=1");
//        pathBuilderAll.append("&depId=").append("1");
//        String pathall = pathBuilderAll.toString();
//        System.out.println("Encoded URLARRRRRR: " + pathall);
//
//        WeNoticeService.autoGbSuppliertixingMessageJj("o85GY5bUj3f1lS5-tK1eFOMb5uZ8", pathall, mapNotice);

        return R.ok().put("data", gbDepartmentOrdersEntity);
    }


    private void transToNxData(GbDistributerGoodsEntity gbDistributerGoodsEntity, GbDepartmentOrdersEntity orders, Integer nxDisId) {
        if (gbDistributerGoodsEntity.getGbDgNxGoodsId() != null) {
            //nxDisyou
            Map<String, Object> map = new HashMap<>();
            map.put("nxGoodsId", gbDistributerGoodsEntity.getGbDgNxGoodsId());
            map.put("disId", nxDisId);
            System.out.println("nxDigodamap" + map);

            NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(gbDistributerGoodsEntity.getGbDgNxGoodsId());

            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryOneGoodsAboutNxGoods(map);
            System.out.println("11111111");
            if (distributerGoodsEntity != null) {
                //设置 gbDisGoods给 nxDis
                changeGbOrderToNxOrder(orders, distributerGoodsEntity.getNxDistributerGoodsId(), nxDisId);

            } else {
                //下载 nxGoods
                NxDistributerGoodsEntity distributerGoodsEntity1 = downLoadNxGoodsForGbDisGoods(orders.getGbDoDisGoodsId(), nxGoodsEntity, nxDisId);

                ////设置 gbDisGoods给 nxDis
                changeGbOrderToNxOrder(orders, distributerGoodsEntity1.getNxDistributerGoodsId(), nxDisId);


            }

        } else {
            //linshitianjai
            //给 nxDis 添加临时 goods
            NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();
            Map<String, Object> map = new HashMap<>();
            map.put("disId", nxDisId);
            map.put("nxGoodsId", -1);
            map.put("goodsLevel", 2);
            System.out.println("linshsisssisisiisiissi" + nxDisId);
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);

            NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
            goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
            goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
            goods.setNxDgPurchaseAuto(-1);
            goods.setNxDgGoodsName(gbDistributerGoodsEntity.getGbDgGoodsName());
            String pinyin = hanziToPinyin(gbDistributerGoodsEntity.getGbDgGoodsName());
            String headPinyin = getHeadStringByString(gbDistributerGoodsEntity.getGbDgGoodsName(), false, null);
            goods.setNxDgGoodsPinyin(pinyin);
            goods.setNxDgGoodsPy(headPinyin);
            goods.setNxDgDistributerId(nxDisId);
            goods.setNxDgBuyingPriceIsGrade(0);
            goods.setNxDgBuyingPrice("0.1");
            goods.setNxDgWillPrice("0.1");
            goods.setNxDgGoodsIsHidden(0);
            goods.setNxDgGoodsStandardname(gbDistributerGoodsEntity.getGbDgGoodsStandardname());
            goods.setNxDgGoodsDetail(gbDistributerGoodsEntity.getGbDgGoodsDetail());
            goods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
            goods.setNxDgBuyingPriceOne("0.1");
            goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
            goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
            goods.setNxDgWillPriceOne("0.1");
            goods.setNxDgWillPriceOneAboutPrice("0.1");
            System.out.println("savegoogog" + goods);
            goods.setNxDgOutTotalWeight("0");
            nxDistributerGoodsService.save(goods);

            Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
            fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
            nxDistributerFatherGoodsService.update(fatherGoodsEntity);

            ////设置 gbDisGoods给 nxDis
            changeGbOrderToNxOrder(orders, goods.getNxDistributerGoodsId(), nxDisId);

        }
    }

    private void changeGbOrderToNxOrder(GbDepartmentOrdersEntity gbDepartmentOrders, Integer nxDisGoodsId, Integer nxDisId) {

        gbDepartmentOrders.setGbDoNxDistributerId(nxDisId);
        gbDepartmentOrders.setGbDoNxDistributerGoodsId(nxDisGoodsId);
        gbDepartmentOrders.setGbDoGoodsType(5);
        gbDepartmentOrders.setGbDoOrderType(5);
        gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
        gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));

        NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
        ordersEntity.setNxDoDistributerId(nxDisId);
        ordersEntity.setNxDoDisGoodsId(nxDisGoodsId);
        ordersEntity.setNxDoQuantity(gbDepartmentOrders.getGbDoQuantity());
        ordersEntity.setNxDoStandard(gbDepartmentOrders.getGbDoStandard());
        ordersEntity.setNxDoRemark(gbDepartmentOrders.getGbDoRemark());
        ordersEntity.setNxDoCostPriceLevel("1");

        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDisGoodsId);

        if (ordersEntity.getNxDoStandard().equals(nxDistributerGoodsEntity.getNxDgWillPriceOneStandard())) {
            BigDecimal willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
            BigDecimal orderWeight = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
            BigDecimal subtotal = orderWeight.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);

            gbDepartmentOrders.setGbDoPrice(nxDistributerGoodsEntity.getNxDgWillPrice());
            gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
            gbDepartmentOrders.setGbDoSubtotal(subtotal.toString());
            gbDepartmentOrders.setGbDoCostPriceLevel(1);

            Integer gbDoPurchaseGoodsId = gbDepartmentOrders.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
            purchaseGoodsEntity.setGbDpgBuyPrice(willPrice.toString());
            //todo
            purchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(nxDisId);
            gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);


            ordersEntity.setNxDoPrice(nxDistributerGoodsEntity.getNxDgWillPriceOne());
            ordersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
            ordersEntity.setNxDoSubtotal(subtotal.toString());
            ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPriceOne());
            ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceOneUpdate());
            BigDecimal buyPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPriceOne());
            BigDecimal buySutotal = buyPrice.multiply(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoCostSubtotal(buySutotal.toString());
            BigDecimal profit = subtotal.subtract(buySutotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoProfitSubtotal(profit.toString());
            BigDecimal decimal = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoProfitScale(decimal.toString());

        } else {
            ordersEntity.setNxDoCostSubtotal("0");
            ordersEntity.setNxDoProfitSubtotal("0");
            ordersEntity.setNxDoCostPrice("0");
            ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPriceOne());
            ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceOneUpdate());
            if (nxDistributerGoodsEntity.getNxDgWillPriceTwoStandard() != null && gbDepartmentOrders.getGbDoStandard().equals(nxDistributerGoodsEntity.getNxDgWillPriceTwoStandard())) {
                BigDecimal willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                BigDecimal orderWeight = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
                BigDecimal subtotal = orderWeight.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);

                ordersEntity.setNxDoPrice(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                ordersEntity.setNxDoCostPriceLevel("2");
                ordersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
                ordersEntity.setNxDoSubtotal(subtotal.toString());
                gbDepartmentOrders.setGbDoPrice(nxDistributerGoodsEntity.getNxDgWillPriceTwo());

                System.out.println("heeehere" + nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                System.out.println("heeehere" + gbDepartmentOrders.getGbDoPrice());
                gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                gbDepartmentOrders.setGbDoSubtotal(subtotal.toString());
                gbDepartmentOrders.setGbDoCostPriceLevel(2);

                Integer gbDoPurchaseGoodsId = gbDepartmentOrders.getGbDoPurchaseGoodsId();
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
                purchaseGoodsEntity.setGbDpgBuyPrice(willPrice.toString());
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

                ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPriceTwo());
                ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceTwoUpdate());
                BigDecimal buyPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPriceTwo());
                BigDecimal buySutotal = buyPrice.multiply(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoCostSubtotal(buySutotal.toString());
                BigDecimal profit = subtotal.subtract(buySutotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoProfitSubtotal(profit.toString());
                BigDecimal decimal = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoProfitScale(decimal.toString());
            }
        }


        ordersEntity.setNxDoApplyDate(formatWhatDay(0));
        ordersEntity.setNxDoArriveOnlyDate(formatWhatDay(0));
        ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
        ordersEntity.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
        ordersEntity.setNxDoArriveDate(gbDepartmentOrders.getGbDoApplyArriveDate());
        ordersEntity.setNxDoGbDistributerId(gbDepartmentOrders.getGbDoDistributerId());
        ordersEntity.setNxDoGbDepartmentId(gbDepartmentOrders.getGbDoDepartmentId());
        ordersEntity.setNxDoGbDepartmentFatherId(gbDepartmentOrders.getGbDoDepartmentFatherId());
        ordersEntity.setNxDoDepartmentId(-1);
        ordersEntity.setNxDoDepartmentFatherId(-1);
        ordersEntity.setNxDoNxCommunityId(-1);
        ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
        ordersEntity.setNxDoNxCommRestrauntId(-1);
        ordersEntity.setNxDoNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
        ordersEntity.setNxDoNxGoodsFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
        ordersEntity.setNxDoDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
        ordersEntity.setNxDoDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
        ordersEntity.setNxDoDepDisGoodsId(-1);
        ordersEntity.setNxDoArriveWhatDay(getWeek(0));
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        ordersEntity.setNxDoGoodsType(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
        ordersEntity.setNxDoIsAgent(-1);
        ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
        ordersEntity.setNxDoPurchaseUserId(-1);
        ordersEntity.setNxDoGbDepartmentOrderId(gbDepartmentOrders.getGbDepartmentOrdersId());
        if (nxDistributerGoodsEntity.getNxDgPurchaseAuto() == -1) {
            ordersEntity.setNxDoPurchaseGoodsId(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
        } else {
            savePurGoodsAutoNx(ordersEntity);
        }

        ordersEntity.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrdersService.saveForGb(ordersEntity);
        Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();

        gbDepartmentOrders.setGbDoNxDepartmentOrderId(nxDepartmentOrdersId);
        gbDepartmentOrdersService.update(gbDepartmentOrders);


    }


    private NxDistributerGoodsEntity downLoadNxGoodsForGbDisGoods(Integer gbDisGoodsId, NxGoodsEntity nxGoodsEntity, Integer nxdisId) {
        System.out.println("downLoadNxGoodsForGbDisGoods-----");
        NxDistributerGoodsEntity cgnGoods = new NxDistributerGoodsEntity();
        cgnGoods.setNxDgDistributerId(nxdisId);
        cgnGoods.setNxDgNxGoodsId(nxGoodsEntity.getNxGoodsId());
        cgnGoods.setNxDgGoodsName(nxGoodsEntity.getNxGoodsName());
        cgnGoods.setNxDgNxFatherId(nxGoodsEntity.getNxGoodsFatherId());

        Integer nxGoodsFatherId = nxGoodsEntity.getNxGoodsFatherId();
        NxGoodsEntity fatherNxGoods = nxGoodsService.queryObject(nxGoodsFatherId);

        cgnGoods.setNxDgNxFatherImg(fatherNxGoods.getNxGoodsFile());
        cgnGoods.setNxDgNxFatherName(fatherNxGoods.getNxGoodsName());
        cgnGoods.setNxDgGoodsDetail(nxGoodsEntity.getNxGoodsDetail());
        cgnGoods.setNxDgGoodsPlace(nxGoodsEntity.getNxGoodsPlace());
        cgnGoods.setNxDgGoodsBrand(nxGoodsEntity.getNxGoodsBrand());
        cgnGoods.setNxDgGoodsStandardname(nxGoodsEntity.getNxGoodsStandardname());
        cgnGoods.setNxDgGoodsStandardWeight(nxGoodsEntity.getNxGoodsStandardWeight());
        cgnGoods.setNxDgGoodsPy(nxGoodsEntity.getNxGoodsPy());
        cgnGoods.setNxDgGoodsPinyin(nxGoodsEntity.getNxGoodsPinyin());
        cgnGoods.setNxDgCartonUnit(nxGoodsEntity.getNxGoodsCartonUnit());
        cgnGoods.setNxDgItemsPerCarton(nxGoodsEntity.getNxGoodsItemsPerCarton());
        cgnGoods.setNxDgPullOff(0);
        cgnGoods.setNxDgGoodsStatus(0);
        cgnGoods.setNxDgPurchaseAuto(-1);

        NxDistributerGoodsEntity distributerGoodsEntity = saveDisGoods(cgnGoods);

        //2，保存dis规格bieming
        Integer nxDistributerGoodsId = cgnGoods.getNxDistributerGoodsId();
        //2.1
        Integer nxDgNxGoodsId = cgnGoods.getNxDgNxGoodsId();
        List<NxStandardEntity> nxStandardEntities = nxStandardService.queryGoodsStandardListByGoodId(nxDgNxGoodsId);
        if (nxStandardEntities.size() > 0) {
            for (NxStandardEntity standard : nxStandardEntities) {
                NxDistributerStandardEntity disStandards = new NxDistributerStandardEntity();
                disStandards.setNxDsDisGoodsId(nxDistributerGoodsId);
                disStandards.setNxDsStandardName(standard.getNxStandardName());
                disStandards.setNxDsStandardError(standard.getNxStandardError());
                disStandards.setNxDsStandardScale(standard.getNxStandardScale());
                disStandards.setNxDsStandardFilePath(standard.getNxStandardFilePath());
                disStandards.setNxDsStandardSort(standard.getNxStandardSort());
                dsService.save(disStandards);
            }
        }

//        2.2

        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", nxDgNxGoodsId);
        List<NxAliasEntity> aliasEntities = nxAliasService.queryNxAliasList(map);
        if (aliasEntities.size() > 0) {
            for (NxAliasEntity aliasEntity : aliasEntities) {
                NxDistributerAliasEntity disAlias = new NxDistributerAliasEntity();
                disAlias.setNxDaDisGoodsId(nxDistributerGoodsId);
                disAlias.setNxDaAliasName(aliasEntity.getNxAliasName());
                disAliasService.save(disAlias);
            }
        }


        return distributerGoodsEntity;
    }


    private NxDistributerGoodsEntity saveDisGoods(NxDistributerGoodsEntity cgnGoods) {
        Integer nxDgNxGoodsId = cgnGoods.getNxDgNxGoodsId();
        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(nxDgNxGoodsId);
        cgnGoods.setNxDgGoodsName(nxGoodsEntity.getNxGoodsName());
        cgnGoods.setNxDgNxFatherImg(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setNxDgGoodsStandardname(nxGoodsEntity.getNxGoodsStandardname());
        cgnGoods.setNxDgGoodsDetail(nxGoodsEntity.getNxGoodsDetail());
        cgnGoods.setNxDgGoodsPlace(nxGoodsEntity.getNxGoodsPlace());
        cgnGoods.setNxDgGoodsBrand(nxGoodsEntity.getNxGoodsBrand());
        cgnGoods.setNxDgGoodsStandardWeight(nxGoodsEntity.getNxGoodsStandardWeight());
        cgnGoods.setNxDgGoodsPinyin(nxGoodsEntity.getNxGoodsPinyin());
        cgnGoods.setNxDgGoodsPy(nxGoodsEntity.getNxGoodsPy());
        cgnGoods.setNxDgPullOff(0);
        cgnGoods.setNxDgGoodsStatus(1);
        cgnGoods.setNxStandardEntities(nxGoodsEntity.getNxGoodsStandardEntities());
        cgnGoods.setNxDistributerAliasEntities(nxGoodsEntity.getNxDistributerAliasEntities());
        cgnGoods.setNxDgNxFatherId(nxGoodsEntity.getNxGoodsFatherId());
        cgnGoods.setNxDgNxFatherName(nxGoodsEntity.getFatherGoods().getNxGoodsName());
        cgnGoods.setNxDgNxFatherImg(nxGoodsEntity.getFatherGoods().getNxGoodsFile());
        cgnGoods.setNxDgNxGrandName(nxGoodsEntity.getGrandGoods().getNxGoodsName());
        cgnGoods.setNxDgNxGrandId(nxGoodsEntity.getGrandGoods().getNxGoodsId());
        cgnGoods.setNxDgGoodsFile(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setNxDgGoodsFileLarge(nxGoodsEntity.getNxGoodsFileBig());
        cgnGoods.setNxDgIsOldestSon(nxGoodsEntity.getNxGoodsIsOldestSon());
        cgnGoods.setNxDgGoodsSort(nxGoodsEntity.getNxGoodsSort());
        cgnGoods.setNxDgGoodsSonsSort(nxGoodsEntity.getNxGoodsSonsSort());
        cgnGoods.setNxDgGoodsIsHidden(0);
        cgnGoods.setNxDgBuyingPriceIsGrade(0);
        cgnGoods.setNxDgPurchaseAuto(1);
        cgnGoods.setNxDgQuantityDays(nxGoodsEntity.getNxGoodsQuantityDays());

        cgnGoods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
        cgnGoods.setNxDgWillPrice("0.1");
        cgnGoods.setNxDgBuyingPrice("0.1");
        cgnGoods.setNxDgBuyingPriceOne("0.1");
        cgnGoods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        cgnGoods.setNxDgWillPriceOneStandard(cgnGoods.getNxDgGoodsStandardname());
        cgnGoods.setNxDgWillPriceOne("0.1");
        cgnGoods.setNxDgWillPriceOneAboutPrice("0.1");


        //queryGrandFatherId
        NxGoodsEntity fatherEntity = nxGoodsService.queryObject(cgnGoods.getNxDgNxFatherId());
        Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
        cgnGoods.setNxDgNxGrandId(grandFatherId);
        NxGoodsEntity grandEntity = nxGoodsService.queryObject(grandFatherId);
        cgnGoods.setNxDgNxGrandName(grandEntity.getNxGoodsName());


        //queryGreatGrandFatherId
        Integer greatGrandFatherId = grandEntity.getNxGoodsFatherId();
        if (greatGrandFatherId.equals(1)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#20afb8");
        }
        if (greatGrandFatherId.equals(2)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#f5c832");
        }
        if (greatGrandFatherId.equals(3)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#3cc36e");
        }
        if (greatGrandFatherId.equals(4)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#f09628");
        }
        if (greatGrandFatherId.equals(5)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#1ebaee");
        }
        if (greatGrandFatherId.equals(6)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#f05a32");
        }
        if (greatGrandFatherId.equals(7)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#c0a6dd");
        }
        if (greatGrandFatherId.equals(8)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#969696");
        }
        if (greatGrandFatherId.equals(9)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#318666");
        }
        if (greatGrandFatherId.equals(10)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#026bc2");
        }
        if (greatGrandFatherId.equals(11)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#06eb6d");
        }
        if (greatGrandFatherId.equals(12)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#0690eb");
        }

        cgnGoods.setNxDgNxGreatGrandId(greatGrandFatherId);
        cgnGoods.setNxDgNxGreatGrandName(nxGoodsService.queryObject(greatGrandFatherId).getNxGoodsName());

        Integer nxDgDistributerId = cgnGoods.getNxDgDistributerId();

        // 3， 查询父类
        Integer nxDgNxFatherId = cgnGoods.getNxDgNxFatherId();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDgDistributerId);
        map.put("nxFatherId", nxDgNxFatherId);
        List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = nxDistributerGoodsService.queryDisGoodsByParams(map);

        if (nxDistributerGoodsEntities.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsEntities.get(0);
            Integer nxDgDfgGoodsFatherId1 = disGoodsEntity.getNxDgDfgGoodsFatherId();
            Integer nxDgDfgGoodsGrandId = disGoodsEntity.getNxDgDfgGoodsGrandId();

            NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsFatherId1);
            Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
            nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount + 1);
            nxDistributerFatherGoodsService.update(nxDistributerFatherGoodsEntity);

            //2，保存disId商品
            Integer nxDgDfgGoodsFatherId = disGoodsEntity.getNxDgDfgGoodsFatherId();
            cgnGoods.setNxDgDfgGoodsFatherId(nxDgDfgGoodsFatherId);
            cgnGoods.setNxDgDfgGoodsGrandId(nxDgDfgGoodsGrandId);

            cgnGoods.setNxDgOutTotalWeight("0");
            //1 ，先保存disGoods
            nxDistributerGoodsService.save(cgnGoods);
            //
        } else {
            //添加fatherGoods的第一个级别
            NxDistributerFatherGoodsEntity dgf = new NxDistributerFatherGoodsEntity();
            dgf.setNxDfgDistributerId(cgnGoods.getNxDgDistributerId());
            dgf.setNxDfgFatherGoodsName(cgnGoods.getNxDgNxFatherName());
            dgf.setNxDfgFatherGoodsLevel(2);
            dgf.setNxDfgGoodsAmount(1);
            dgf.setNxDfgFatherGoodsImg(cgnGoods.getNxDgNxFatherImg());
            dgf.setNxDfgFatherGoodsColor(cgnGoods.getNxDgNxGoodsFatherColor());
            dgf.setNxDfgFatherGoodsSort(nxGoodsEntity.getFatherGoods().getNxGoodsSort());
            dgf.setNxDfgNxGoodsId(cgnGoods.getNxDgNxFatherId());
            nxDistributerFatherGoodsService.save(dgf);

            //继续查询是否有GrandFather
            String grandName = cgnGoods.getNxDgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", nxDgDistributerId);
            map2.put("fathersFatherName", grandName);
            List<NxDistributerFatherGoodsEntity> grandGoodsFather = nxDistributerFatherGoodsService.queryHasDisFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = grandGoodsFather.get(0);
                dgf.setNxDfgFathersFatherId(nxDistributerFatherGoodsEntity.getNxDistributerFatherGoodsId());
                nxDistributerFatherGoodsService.update(dgf);
                //更新disGoods的fatherGoodsId
                Integer distributerFatherGoodsId = dgf.getNxDistributerFatherGoodsId();
                Integer nxDfgFathersFatherId = dgf.getNxDfgFathersFatherId();
                cgnGoods.setNxDgDfgGoodsFatherId(distributerFatherGoodsId);
                cgnGoods.setNxDgDfgGoodsGrandId(nxDfgFathersFatherId);
                cgnGoods.setNxDgOutTotalWeight("0");
                nxDistributerGoodsService.save(cgnGoods);

            } else {
                //tianjiaGrand
                NxDistributerFatherGoodsEntity grand = new NxDistributerFatherGoodsEntity();
                String nxCgGrandFatherName = cgnGoods.getNxDgNxGrandName();
                grand.setNxDfgFatherGoodsName(nxCgGrandFatherName);
                grand.setNxDfgDistributerId(cgnGoods.getNxDgDistributerId());
                grand.setNxDfgFatherGoodsLevel(1);
                grand.setNxDfgGoodsAmount(1);
                grand.setNxDfgFatherGoodsColor(cgnGoods.getNxDgNxGoodsFatherColor());
                grand.setNxDfgNxGoodsId(cgnGoods.getNxDgNxGrandId());
                grand.setNxDfgFatherGoodsSort(nxGoodsEntity.getGrandGoods().getNxGoodsSort());
                nxDistributerFatherGoodsService.save(grand);

                dgf.setNxDfgFathersFatherId(grand.getNxDistributerFatherGoodsId());
                nxDistributerFatherGoodsService.update(dgf);
                //更新disGoods的fatherGoodsId
                Integer distributerFatherGoodsId = dgf.getNxDistributerFatherGoodsId();
                Integer nxDfgFathersFatherId = dgf.getNxDfgFathersFatherId();
                cgnGoods.setNxDgDfgGoodsFatherId(distributerFatherGoodsId);
                cgnGoods.setNxDgDfgGoodsGrandId(nxDfgFathersFatherId);
                cgnGoods.setNxDgOutTotalWeight("0");
                nxDistributerGoodsService.save(cgnGoods);
                //查询是否有greatGrand
                String greatGrandName = cgnGoods.getNxDgNxGreatGrandName();
                Map<String, Object> map3 = new HashMap<>();
                map3.put("disId", nxDgDistributerId);
                map3.put("fathersFatherName", greatGrandName);
                List<NxDistributerFatherGoodsEntity> greatGrandGoodsFather = nxDistributerFatherGoodsService.queryHasDisFathersFather(map3);
                if (greatGrandGoodsFather.size() > 0) {
                    NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = greatGrandGoodsFather.get(0);
                    Integer disFatherId = nxDistributerFatherGoodsEntity.getNxDistributerFatherGoodsId();
                    grand.setNxDfgFathersFatherId(disFatherId);
                    nxDistributerFatherGoodsService.update(grand);
                } else {
                    NxDistributerFatherGoodsEntity greatGrand = new NxDistributerFatherGoodsEntity();
                    String greatGrandName1 = cgnGoods.getNxDgNxGreatGrandName();
                    greatGrand.setNxDfgFatherGoodsName(greatGrandName1);
                    greatGrand.setNxDfgDistributerId(cgnGoods.getNxDgDistributerId());
                    greatGrand.setNxDfgFatherGoodsLevel(0);
                    greatGrand.setNxDfgFatherGoodsColor(cgnGoods.getNxDgNxGoodsFatherColor());
                    greatGrand.setNxDfgNxGoodsId(cgnGoods.getNxDgNxGreatGrandId());
                    greatGrand.setNxDfgFatherGoodsImg(grandEntity.getFatherGoods().getNxGoodsFile());
                    greatGrand.setNxDfgFatherGoodsSort(grandEntity.getFatherGoods().getNxGoodsSort());
                    greatGrand.setNxDfgGoodsAmount(1);
                    nxDistributerFatherGoodsService.save(greatGrand);
                    grand.setNxDfgFathersFatherId(greatGrand.getNxDistributerFatherGoodsId());
                    nxDistributerFatherGoodsService.update(grand);
                }
            }
        }
        return cgnGoods;
    }


    private void savePurGoodsAutoNx(NxDepartmentOrdersEntity ordersEntity) {

        NxDistributerPurchaseGoodsEntity resultPurGoods = new NxDistributerPurchaseGoodsEntity();

        //判断是否有已经分的
        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(doDisGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", doDisGoodsId);
        map.put("status", 1);
        map.put("standard", ordersEntity.getNxDoStandard());
        System.out.println("purgogogo" + map);
        NxDistributerPurchaseGoodsEntity havePurGoods = nxDistributerPurchaseGoodsService.queryIfHavePurGoods(map);
        if (havePurGoods != null) {
            resultPurGoods = havePurGoods;
            havePurGoods.setNxDpgOrdersAmount(resultPurGoods.getNxDpgOrdersAmount() + 1);
            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                if (havePurGoods.getNxDpgBuyQuantity() != null) {
                    BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgQuantity());
                    BigDecimal decimal1 = new BigDecimal(ordersEntity.getNxDoQuantity());
                    BigDecimal totaoWeight = decimal.add(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(havePurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    resultPurGoods.setNxDpgQuantity(totaoWeight.toString());
                    resultPurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
                    resultPurGoods.setNxDpgBuySubtotal(decimal2.toString());
                }

            }
//            else {
//                BigDecimal decimal = new BigDecimal(resultPurGoods.getNxDpgQuantity());
//                BigDecimal purQuantity = new BigDecimal(resultPurGoods.getNxDpgQuantity());
//                BigDecimal add = decimal.add(purQuantity);
//                resultPurGoods.setNxDpgQuantity(add.toString());
//                resultPurGoods.setNxDpgBuyQuantity(add.toString());
//            }
            nxDistributerPurchaseGoodsService.update(resultPurGoods);

        } else {
            resultPurGoods.setNxDpgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
            resultPurGoods.setNxDpgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
            resultPurGoods.setNxDpgDistributerId(disGoods.getNxDgDistributerId());
            resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
            resultPurGoods.setNxDpgApplyDate(formatWhatYearDayTime(0));
            resultPurGoods.setNxDpgOrdersAmount(1);
            resultPurGoods.setNxDpgFinishAmount(0);
            resultPurGoods.setNxDpgPurchaseType(1);
            resultPurGoods.setNxDpgExpectPrice(disGoods.getNxDgBuyingPrice());
            resultPurGoods.setNxDpgBuyPrice(disGoods.getNxDgBuyingPrice());
            resultPurGoods.setNxDpgDisGoodsId(doDisGoodsId);
            resultPurGoods.setNxDpgInputType(disGoods.getNxDgPurchaseAuto());
            resultPurGoods.setNxDpgStandard(ordersEntity.getNxDoStandard());
            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                BigDecimal totaoWeight = new BigDecimal(ordersEntity.getNxDoQuantity());
                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(resultPurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                resultPurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
                resultPurGoods.setNxDpgQuantity(totaoWeight.toString());
                resultPurGoods.setNxDpgBuySubtotal(decimal2.toString());
            }
            nxDistributerPurchaseGoodsService.save(resultPurGoods);

        }

        NxDistributerPurchaseBatchEntity batchEntity = new NxDistributerPurchaseBatchEntity();

        //给autoBatch更新gbDepartmentOrderid
        System.out.println("suplieridid" + disGoods.getNxDgGoodsName() + disGoods.getNxDgSupplierId());
        if (disGoods.getNxDgSupplierId() != null) {
            //
            Map<String, Object> mapBatch = new HashMap<>();
            Integer gbDgGbSupplierId = disGoods.getNxDgSupplierId();
            mapBatch.put("supplierId", gbDgGbSupplierId);
            mapBatch.put("status", 1);
            mapBatch.put("purchaseType", 2);

            List<NxDistributerPurchaseBatchEntity> entities = nxDPBService.queryDisPurchaseBatch(mapBatch);
            if (entities.size() == 0) {
                //
                batchEntity.setNxDpbDate(formatWhatDay(0));
                batchEntity.setNxDpbTime(formatWhatTime(0));
                batchEntity.setNxDpbMonth(formatWhatMonth(0));
                batchEntity.setNxDpbPruchaseWeek(getWeek(0));
                batchEntity.setNxDpbYear(formatWhatYear(0));
                batchEntity.setNxDpbPayFullTime(formatWhatYearDayTime(0));
                batchEntity.setNxDpbStatus(-1);
                batchEntity.setNxDpbPurchaseType(2);
                batchEntity.setNxDpbSupplierId(gbDgGbSupplierId);
                NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDgGbSupplierId);
                batchEntity.setNxDpbSellUserId(supplierEntity.getNxJrdhsUserId());
                batchEntity.setNxDpbDistributerId(ordersEntity.getNxDoDistributerId());
                batchEntity.setNxDpbPurUserId(supplierEntity.getNxJrdhsNxPurUserId());
                batchEntity.setNxDpbBuyUserId(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                batchEntity.setNxDpbBuyUserOpenId(nxJrdhUserEntity.getNxJrdhWxOpenId());
                nxDPBService.save(batchEntity);

                resultPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                resultPurGoods.setNxDpgPurchaseDate(formatWhatDay(0));
                resultPurGoods.setNxDpgTime(formatWhatYearDayTime(0));
                resultPurGoods.setNxDpgDistributerId(batchEntity.getNxDpbDistributerId());
                resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
                nxDistributerPurchaseGoodsService.update(resultPurGoods);
            } else {
                batchEntity = entities.get(0);
                resultPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                nxDistributerPurchaseGoodsService.update(resultPurGoods);
            }

            Map<String, TemplateData> mapNotice = new HashMap<>();
            mapNotice.put("thing1", new TemplateData(disGoods.getNxDgGoodsName()));
            mapNotice.put("time6", new TemplateData(formatWhatDay(0)));
            Integer nxDpbDistributerId = batchEntity.getNxDpbDistributerId();
            NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDpbDistributerId);
            mapNotice.put("thing7", new TemplateData(distributerEntity.getNxDistributerName()));
            mapNotice.put("thing8", new TemplateData(distributerEntity.getNxDistributerPhone()));
//
            StringBuilder pathBuilder = new StringBuilder("pages/txs/disOrderBatch/disOrderBatch");
            pathBuilder.append("?batchId=").append(batchEntity.getNxDistributerPurchaseBatchId());
            pathBuilder.append("&retName=").append(nxDistributerService.queryObject(batchEntity.getNxDpbDistributerId()).getNxDistributerName());
            pathBuilder.append("&disId=").append(batchEntity.getNxDpbDistributerId());
            pathBuilder.append("&buyUserId=").append(batchEntity.getNxDpbBuyUserId());
            pathBuilder.append("&purUserId=").append(batchEntity.getNxDpbPurUserId());
            pathBuilder.append("&fromBuyer=1");
            String path = pathBuilder.toString();
            System.out.println("paththhththt" + path);

            NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(disGoods.getNxDgSupplierId());
            Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);
            System.out.println("suppsleir" + path);
            WeNoticeService.nxSupplierOrderSave(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);

            ordersEntity.setNxDoPurchaseStatus(getNxDisPurchaseGoodsIsPurchase());
        }

        ordersEntity.setNxDoPurchaseGoodsId(resultPurGoods.getNxDistributerPurchaseGoodsId());
        nxDepartmentOrdersService.update(ordersEntity);

    }


    @ResponseBody
    @RequestMapping("/saveOrdersGbJjSx")
    public R saveOrdersGbJjSx(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {

        System.out.println("autottototot" + gbDepartmentOrders);

        if (gbDepartmentOrders.getStockIsZero()) {
            clearDepGoodsStock(gbDepartmentOrders);
        }

        gbDepartmentOrders.setGbDoPurchaseGoodsId(-1);
        gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
        gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
        Integer gbDoDisGoodsFatherId = gbDepartmentOrders.getGbDoDisGoodsFatherId();
        GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(gbDoDisGoodsFatherId);
        Integer gbDfgFathersFatherId = fatherGoodsEntity.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity grandFather = gbDistributerFatherGoodsService.queryObject(gbDfgFathersFatherId);
        Integer greatFatherId = grandFather.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity greatFather = gbDistributerFatherGoodsService.queryObject(greatFatherId);

        gbDepartmentOrders.setGbDoDisGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        gbDepartmentOrders.setGbDoDisGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
        gbDepartmentOrders.setGbDoDisGoodsGreatId(greatFatherId);

        gbDepartmentOrders.setGbDoNxGoodsGrandId(grandFather.getGbDfgNxGoodsId());
        gbDepartmentOrders.setGbDoNxGoodsGreatId(greatFather.getGbDfgNxGoodsId());

        gbDepartmentOrdersService.save(gbDepartmentOrders);

        //查询是否有采购的同一个商品
        Map<String, Object> map = new HashMap<>();
        map.put("nxDisId", gbDepartmentOrders.getGbDoNxDistributerId());
        map.put("standard", gbDepartmentOrders.getGbDoStandard());
        map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
        map.put("equalStatus", 0); //没有供货商的商品
        GbDistributerPurchaseGoodsEntity gbPurchaseGoodsEntity = new GbDistributerPurchaseGoodsEntity();
        Integer gbDoDisGoodsId = gbDepartmentOrders.getGbDoDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        System.out.println("putgodosmap" + map);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);

        if (purchaseGoodsEntities.size() == 0) {
            //是个新采购商品
            gbPurchaseGoodsEntity.setGbDpgPurchaseType(5);
            gbPurchaseGoodsEntity.setGbDpgPayType(0);
            gbPurchaseGoodsEntity.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
            gbPurchaseGoodsEntity.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
            gbPurchaseGoodsEntity.setGbDpgApplyDate(formatWhatDay(0));
            gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(1);
            gbPurchaseGoodsEntity.setGbDpgOrdersFinishAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersWeightAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersBillAmount(0);
            gbPurchaseGoodsEntity.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
            gbPurchaseGoodsEntity.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
            gbPurchaseGoodsEntity.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
            gbPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(gbDistributerGoodsEntity.getGbDgGbDepartmentId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxSupplierId(gbDistributerGoodsEntity.getGbDgGbSupplierId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsGrandId(gbDistributerGoodsEntity.getGbDgDfgGoodsGrandId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsGreatId(gbDistributerGoodsEntity.getGbDgDfgGoodsGreatId());
            System.out.println("nxididid" + gbDepartmentOrders.getGbDoNxDistributerId());
            gbDistributerPurchaseGoodsService.save(gbPurchaseGoodsEntity);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
        } else {
            //给老采购商品添加新订单
            System.out.println("newownwgopuggog");
            gbPurchaseGoodsEntity = purchaseGoodsEntities.get(0);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
            //采购商品订单数量更新
            Integer gbDpgOrdersAmount = gbPurchaseGoodsEntity.getGbDpgOrdersAmount();
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
            BigDecimal purQuantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
            BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
            BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbPurchaseGoodsEntity.setGbDpgQuantity(add.toString());
            gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);
        }


        NxDepartmentOrdersEntity ordersEntity = addNxOrderAuto(gbDepartmentOrders, gbDistributerGoodsEntity, gbPurchaseGoodsEntity);

        gbDepartmentOrdersService.update(gbDepartmentOrders);
        return R.ok().put("data", ordersEntity);
    }


    private NxDepartmentOrdersEntity addNxOrderAuto(GbDepartmentOrdersEntity gbDepartmentOrders, GbDistributerGoodsEntity gbDistributerGoodsEntity, GbDistributerPurchaseGoodsEntity gbPurchaseGoodsEntity) {
        Integer nxDoDistributerId = gbDepartmentOrders.getGbDoNxDistributerId();
        Integer gbDoNxDistributerGoodsId = gbDepartmentOrders.getGbDoNxDistributerGoodsId();
        Map<String, Object> map = new HashMap<>();
        map.put("nxGoodsId", gbDistributerGoodsEntity.getGbDgNxGoodsId());
        map.put("disId", gbDepartmentOrders.getGbDoNxDistributerId());
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryOneGoodsAboutNxGoods(map);


        String gbDoQuantity = gbDepartmentOrders.getGbDoQuantity();
        String gbDoStandard = gbDepartmentOrders.getGbDoStandard();
        String gbDoRemark = gbDepartmentOrders.getGbDoRemark();
        gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
        gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
        String gbDoApplyArriveDate = gbDepartmentOrders.getGbDoApplyArriveDate();
        Integer gbDoDepartmentId = gbDepartmentOrders.getGbDoDepartmentId();
        Integer gbDoDistributerId = gbDepartmentOrders.getGbDoDistributerId();
        Integer gbDoDepartmentFatherId = gbDepartmentOrders.getGbDoDepartmentFatherId();
        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
        //
        NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
        ordersEntity.setNxDoDistributerId(nxDoDistributerId);
        ordersEntity.setNxDoDisGoodsId(gbDoNxDistributerGoodsId);
        ordersEntity.setNxDoQuantity(gbDoQuantity);

        ordersEntity.setNxDoStandard(gbDoStandard);
        ordersEntity.setNxDoRemark(gbDoRemark);
        ordersEntity.setNxDoApplyDate(formatWhatDay(0));
        ordersEntity.setNxDoArriveOnlyDate(formatWhatDay(0));
        ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
        ordersEntity.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
        ordersEntity.setNxDoArriveDate(gbDoApplyArriveDate);
        ordersEntity.setNxDoGbDistributerId(gbDoDistributerId);
        ordersEntity.setNxDoGbDepartmentId(gbDoDepartmentId);
        ordersEntity.setNxDoGbDepartmentFatherId(gbDoDepartmentFatherId);
        ordersEntity.setNxDoDepartmentId(-1);
        ordersEntity.setNxDoDepartmentFatherId(-1);
        ordersEntity.setNxDoNxCommunityId(-1);
        ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
        ordersEntity.setNxDoNxCommRestrauntId(-1);
        ordersEntity.setNxDoNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
        ordersEntity.setNxDoNxGoodsFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
        ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
        ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
        ordersEntity.setNxDoArriveWhatDay(getWeek(0));
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        ordersEntity.setNxDoGoodsType(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
        ordersEntity.setNxDoIsAgent(-1);
        System.out.println("nxgenennemetowowowo" + nxDistributerGoodsEntity.getNxDgWillPriceTwo());
        System.out.println("nxgenennemetowowowo" + gbDepartmentOrders.getGbDoStandard() + "wilweiid" + nxDistributerGoodsEntity.getNxDgWillPriceTwoStandard());
        String nxDoPrice = "";
        String nxDoCostPrice = "";
        String nxDoCostUpdate = "";
        if (nxDistributerGoodsEntity.getNxDgWillPriceTwo() != null && !nxDistributerGoodsEntity.getNxDgWillPriceTwo().equals("0.1")) {
            if (gbDepartmentOrders.getGbDoStandard().equals(nxDistributerGoodsEntity.getNxDgWillPriceTwoStandard())) {
                ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight());
                ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPriceTwo());
                ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceTwoUpdate());
                ordersEntity.setNxDoCostPriceLevel("2");
                ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgWillPriceTwoStandard());
                ordersEntity.setNxDoExpectPrice(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                nxDoPrice = nxDistributerGoodsEntity.getNxDgWillPriceTwo();
                nxDoCostPrice = nxDistributerGoodsEntity.getNxDgBuyingPriceTwo();
                nxDoCostUpdate = nxDistributerGoodsEntity.getNxDgBuyingPriceTwoUpdate();
            } else {
                ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
                ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPriceOne());
                ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceOneUpdate());
                ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgWillPriceOneStandard());
                ordersEntity.setNxDoCostPriceLevel("1");
                ordersEntity.setNxDoExpectPrice(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                nxDoPrice = nxDistributerGoodsEntity.getNxDgWillPriceOne();
                nxDoCostPrice = nxDistributerGoodsEntity.getNxDgBuyingPriceOne();
                nxDoCostUpdate = nxDistributerGoodsEntity.getNxDgBuyingPriceOneUpdate();
            }
        } else {
            ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
            ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPriceOne());
            ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceOneUpdate());
            ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgWillPriceOneStandard());
            ordersEntity.setNxDoCostPriceLevel("1");
            ordersEntity.setNxDoExpectPrice(nxDistributerGoodsEntity.getNxDgWillPriceOne());
            nxDoPrice = nxDistributerGoodsEntity.getNxDgWillPriceOne();
            nxDoCostPrice = nxDistributerGoodsEntity.getNxDgBuyingPriceOne();
            nxDoCostUpdate = nxDistributerGoodsEntity.getNxDgBuyingPriceOneUpdate();
        }

        ordersEntity.setNxDoPurchaseUserId(-1);


        ordersEntity.setNxDoGbDepartmentOrderId(gbDepartmentOrders.getGbDepartmentOrdersId());
        if (nxDistributerGoodsEntity.getNxDgPurchaseAuto() == -1) {
            ordersEntity.setNxDoPurchaseGoodsId(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
        } else {
            savePurGoodsAuto(ordersEntity);
        }

        System.out.println("lveelelelel" + ordersEntity.getNxDoCostPriceLevel());
        System.out.println("lveelelelelnxDoPrice" + nxDoPrice + "cosotpri" + nxDoCostPrice);

        if (ordersEntity.getNxDoCostPriceLevel() != null && ordersEntity.getNxDoCostPriceLevel().equals("1") && !ordersEntity.getNxDoStandard().equals(nxDistributerGoodsEntity.getNxDgGoodsStandardname())) {
            ordersEntity.setNxDoCostSubtotal("0");
            ordersEntity.setNxDoProfitSubtotal("0");
            ordersEntity.setNxDoCostPrice("0");
        } else {

            BigDecimal orderWeight = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
            BigDecimal subtotal = orderWeight.multiply(new BigDecimal(nxDoPrice)).setScale(1, BigDecimal.ROUND_HALF_UP);

            ordersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
            ordersEntity.setNxDoSubtotal(subtotal.toString());
            gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
            gbDepartmentOrders.setGbDoSubtotal(subtotal.toString());

            ordersEntity.setNxDoCostPrice(nxDoCostPrice);
            ordersEntity.setNxDoCostPriceUpdate(nxDoCostUpdate);
            BigDecimal buySutotal = new BigDecimal(nxDoCostPrice).multiply(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoCostSubtotal(buySutotal.toString());
            BigDecimal profit = subtotal.subtract(buySutotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoProfitSubtotal(profit.toString());
            BigDecimal decimal = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoProfitScale(decimal.toString());

            gbPurchaseGoodsEntity.setGbDpgBuyPrice(nxDoPrice);
            gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
            gbPurchaseGoodsEntity.setGbDpgBuySubtotal(subtotal.toString());
            gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbPurchaseGoodsEntity.getGbDpgQuantity());
            gbPurchaseGoodsEntity.setGbDpgTime(formatWhatTime(0));
            gbPurchaseGoodsEntity.setGbDpgPurchaseDate(formatWhatDay(0));
            gbPurchaseGoodsEntity.setGbDpgPurchaseMonth(formatWhatMonth(0));
            gbPurchaseGoodsEntity.setGbDpgPurchaseYear(formatWhatYear(0));
            gbPurchaseGoodsEntity.setGbDpgPurchaseWeek(getWeek(0));
            gbPurchaseGoodsEntity.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));

            gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);

        }

        Map<String, Object> mapDepGoods = new HashMap<>();
        mapDepGoods.put("gbDepId", gbDepartmentOrders.getGbDoDepartmentId());
        mapDepGoods.put("disGoodsId", nxDistributerGoodsEntity.getNxDistributerGoodsId());

        System.out.println("wiwiwiwiedeppeep" + mapDepGoods);
        NxDepartmentDisGoodsEntity departmentGoods = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapDepGoods);
        if (departmentGoods != null && !departmentGoods.getNxDdgOrderPrice().equals(nxDoPrice)) {
            ordersEntity.setNxDoPrice(departmentGoods.getNxDdgOrderPrice());
            gbDepartmentOrders.setGbDoPrice(departmentGoods.getNxDdgOrderPrice());
            BigDecimal subtract = new BigDecimal(0);
            if (gbDepartmentOrders.getGbDoStandard().equals(nxDistributerGoodsEntity.getNxDgWillPriceTwoStandard())) {
                System.out.println("whwhwhwhattt" + gbDepartmentOrders.getGbDoStandard() + "wi");
                subtract = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo()).subtract(new BigDecimal(departmentGoods.getNxDdgOrderPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                nxDoCostPrice = nxDistributerGoodsEntity.getNxDgBuyingPriceTwo();
            } else {
                subtract = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne()).subtract(new BigDecimal(departmentGoods.getNxDdgOrderPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                nxDoCostPrice = nxDistributerGoodsEntity.getNxDgBuyingPriceOne();
            }
            BigDecimal orderWeight = new BigDecimal(ordersEntity.getNxDoWeight());
            BigDecimal subtotal = new BigDecimal(departmentGoods.getNxDdgOrderPrice()).multiply(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);

            ordersEntity.setNxDoSubtotal(subtotal.toString());
            gbDepartmentOrders.setGbDoSubtotal(subtotal.toString());

            ordersEntity.setNxDoPriceDifferent(subtract.toString());
            gbDepartmentOrders.setGbDoPriceDifferent(subtract.toString());

            ordersEntity.setNxDoCostPrice(nxDoCostPrice);
            ordersEntity.setNxDoCostPriceUpdate(nxDoCostUpdate);
            BigDecimal buySutotal = new BigDecimal(nxDoCostPrice).multiply(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoCostSubtotal(buySutotal.toString());
            BigDecimal profit = subtotal.subtract(buySutotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoProfitSubtotal(profit.toString());
            BigDecimal decimal = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoProfitScale(decimal.toString());
            ordersEntity.setNxDoGbDepDisGoodId(departmentGoods.getNxDepartmentDisGoodsId());
        } else {
            ordersEntity.setNxDoPrice(nxDoPrice);
            ordersEntity.setNxDoExpectPrice(nxDoPrice);
            gbDepartmentOrders.setGbDoPrice(nxDoPrice);
            ordersEntity.setNxDoPriceDifferent("0");
            gbDepartmentOrders.setGbDoPriceDifferent("0");
            ordersEntity.setNxDoGbDepDisGoodId(-1);
        }

        ordersEntity.setNxDoStatus(getNxOrderStatusNew());
        System.out.println("nxoroororo" + ordersEntity.getNxDoPrice());
        nxDepartmentOrdersService.saveForGb(ordersEntity);

        Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
        gbDepartmentOrders.setGbDoNxDepartmentOrderId(nxDepartmentOrdersId);

        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(gbDoDepartmentFatherId);
        Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDepartmentDisId);


        Map<String, TemplateData> mapNotice = new HashMap<>();
        mapNotice.put("thing1", new TemplateData(gbDistributerGoodsEntity.getGbDgGoodsName()));
        mapNotice.put("thing2", new TemplateData(gbDistributerEntity.getGbDistributerName()));

        mapNotice.put("time4", new TemplateData(formatWhatDayTime(0)));
        System.out.println("nociiciiiicicautotootototoototoRRRRR" + mapNotice);
        System.out.println("nociiciiiicicautotootototoototoRRRRR111" + ordersEntity.getNxDoDistributerId());
        Map<String, Object> mapU = new HashMap<>();
        mapU.put("disId", ordersEntity.getNxDoDistributerId());
        mapU.put("admin", 0);
        List<NxDistributerUserEntity> userEntities = nxDistributerUserService.queryRoleNxDisRoleUserList(mapU);
        if (userEntities.size() > 0) {
            for (NxDistributerUserEntity userEntity : userEntities) {
                WeNoticeService.nxDistributerReceiveGbOrders(userEntity.getNxDiuWxOpenId(), "pages/order/index/index", mapNotice);
            }
        }

        return ordersEntity;
    }


    private void clearDepGoodsStock(GbDepartmentOrdersEntity orders) {

        Integer gbDoDepDisGoodsId = orders.getGbDoDepDisGoodsId();

        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", gbDoDepDisGoodsId);
        map.put("restWeight", 0);
        System.out.println("auclearcealstock" + map);
        List<GbDepartmentGoodsStockEntity> departmentGoodsStockEntities = gbDepartmentGoodsStockService.queryGoodsStockByParams(map);
        if (departmentGoodsStockEntities.size() > 0) {
            for (GbDepartmentGoodsStockEntity stock : departmentGoodsStockEntities) {
                stock.setGbDgsMyProduceWeight(stock.getGbDgsRestWeight());
                changeDepartmentStock(stock, "produce", orders.getGbDoOrderUserId());
            }

        }
    }


    private GbDepartmentGoodsStockReduceEntity changeDepartmentStock(GbDepartmentGoodsStockEntity stock, String what, Integer doUserId) {
        GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
        System.out.println("whastttt" + what);
        BigDecimal myChangeWeight = new BigDecimal("0");
        BigDecimal myChangeSubtotal = new BigDecimal(0);
        BigDecimal newAfterProfitSubtotal = new BigDecimal(0);
        BigDecimal salesSubtotal = new BigDecimal(0);
        BigDecimal profitSubtotal = new BigDecimal((0));

        Integer gbDgsGbDisGoodsId = stock.getGbDgsGbDisGoodsId();
        GbDistributerGoodsEntity distributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDgsGbDisGoodsId);
        Integer gbDgGoodsInventoryType = distributerGoodsEntity.getGbDgGoodsInventoryType();

        //利润单价
        BigDecimal costPrice = new BigDecimal(stock.getGbDgsPrice()); //成本单价

        // 1.2 如果是制作接口
        if (what.equals("produce")) {
            //转换数据
            myChangeWeight = new BigDecimal(stock.getGbDgsMyProduceWeight()).setScale(1, BigDecimal.ROUND_HALF_UP); // 最新提交待损耗数量
            myChangeSubtotal = myChangeWeight.multiply(costPrice).setScale(2, BigDecimal.ROUND_HALF_UP); //总制作成本

            //update
            BigDecimal allWeight = new BigDecimal(stock.getGbDgsProduceWeight()).add(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
            BigDecimal allSubtotal = new BigDecimal(stock.getGbDgsProduceSubtotal()).add(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP); //总损耗数量
            stock.setGbDgsProduceWeight(allWeight.toString());
            stock.setGbDgsProduceSubtotal(allSubtotal.toString());

            if (!stock.getGbDgsSellingPrice().equals("-1")) {
                //利润
                BigDecimal gbDgsBetweenPrice = new BigDecimal(stock.getGbDgsBetweenPrice()); //生产利润单价
                BigDecimal newProfitSubtotal = gbDgsBetweenPrice.multiply(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                profitSubtotal = new BigDecimal(stock.getGbDgsProfitSubtotal()).add(newProfitSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                stock.setGbDgsProfitSubtotal(profitSubtotal.toString()); //
                //销售利润=总利润+利润
                BigDecimal stockAfterProfitSubtotal = new BigDecimal(stock.getGbDgsAfterProfitSubtotal()); //总的销售利润
                newAfterProfitSubtotal = stockAfterProfitSubtotal.add(newProfitSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                stock.setGbDgsAfterProfitSubtotal(newAfterProfitSubtotal.toString());

                BigDecimal newSellingSubtotal = new BigDecimal(stock.getGbDgsSellingPrice()).multiply(myChangeWeight);
                salesSubtotal = newSellingSubtotal.add(new BigDecimal(stock.getGbDgsProduceSellingSubtotal()));
                stock.setGbDgsProduceSellingSubtotal(salesSubtotal.toString());
                // 产生利润的数量
                BigDecimal add = new BigDecimal(stock.getGbDgsProfitWeight()).add(myChangeWeight);
                stock.setGbDgsProfitWeight(add.toString());

            }

            reduceEntity = addDepGoodsStockReduceEntity(stock, "produce", gbDgGoodsInventoryType, myChangeWeight, myChangeSubtotal, doUserId);
            subscribeDepDisGoodsTotal(myChangeWeight, myChangeSubtotal, stock.getGbDgsGbDepDisGoodsId());
            updateDepGoodsDailyEntity(stock, "produce", myChangeWeight, myChangeSubtotal);

        }

        BigDecimal restWeight = new BigDecimal(stock.getGbDgsRestWeight()); // 剩余数量
        BigDecimal newRestWeight = restWeight.subtract(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP); //最新剩余数量
        BigDecimal newRestSubtotal = newRestWeight.multiply(costPrice).setScale(1, BigDecimal.ROUND_HALF_UP); //最新剩余成本
        stock.setGbDgsRestWeight(newRestWeight.toString());
        stock.setGbDgsRestSubtotal(newRestSubtotal.toString());
        stock.setGbDgsInventoryFullTime(formatWhatFullTime(0));
        stock.setGbDgsInventoryDate(formatWhatDay(0));
        stock.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
        stock.setGbDgsInventoryMonth(formatWhatMonth(0));
        stock.setGbDgsInventoryYear(formatWhatYear(0));

        // 转换showStandardWeight
        if (stock.getGbDgsRestWeightShowStandard() != null && !stock.getGbDgsRestWeightShowStandard().trim().isEmpty()) {
            if (new BigDecimal(stock.getGbDgsRestWeightShowStandard()).compareTo(new BigDecimal(0)) == 1) {
                Integer gbDgsGbDepDisGoodsId = stock.getGbDgsGbDepDisGoodsId();
                GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDgsGbDepDisGoodsId);
                BigDecimal decimal = new BigDecimal(departmentDisGoodsEntity.getGbDdgShowStandardScale());
                BigDecimal myChangeWeightScale = myChangeWeight.divide(decimal, 1, BigDecimal.ROUND_HALF_UP);
                BigDecimal decimal1 = new BigDecimal(stock.getGbDgsRestWeightShowStandard()).subtract(myChangeWeightScale).setScale(1, BigDecimal.ROUND_HALF_UP);
                stock.setGbDgsRestWeightShowStandard(decimal1.toString());
                stock.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
            }
        }

        gbDepartmentGoodsStockService.update(stock);

        if (stock.getGbDgsWeightGoodsId() != null && !what.equals("produce")) { //更新出库制作商品业务数据
            updateWeightGoodsData(stock, what, myChangeWeight);
        }

        return reduceEntity;
    }


    private void updateDepGoodsDailyEntity(GbDepartmentGoodsStockEntity stock, String what, BigDecimal myChangeWeight,
                                           BigDecimal myChangeSubtotal) {
        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", stock.getGbDgsGbDepDisGoodsId());
        map.put("date", formatWhatDay(0));
        System.out.println("updateDepDaily" + what);
        GbDepartmentGoodsDailyEntity depGoodsDailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(map);
        if (depGoodsDailyEntity != null) {
            BigDecimal weight = new BigDecimal(0);
            BigDecimal subtotal = new BigDecimal(0);
            BigDecimal newSalesProfitSubtotal = new BigDecimal(0);
            if (what.equals("loss")) {
                weight = myChangeWeight.add(new BigDecimal(depGoodsDailyEntity.getGbDgdLossWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                subtotal = myChangeSubtotal.add(new BigDecimal(depGoodsDailyEntity.getGbDgdLossSubtotal())).setScale(1, BigDecimal.ROUND_HALF_UP);
                depGoodsDailyEntity.setGbDgdLossWeight(weight.toString());
                depGoodsDailyEntity.setGbDgdLossSubtotal(subtotal.toString());

                if (!stock.getGbDgsSellingPrice().equals("-1")) {
                    newSalesProfitSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdAfterProfitSubtotal()).subtract(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    depGoodsDailyEntity.setGbDgdAfterProfitSubtotal(newSalesProfitSubtotal.toString());
                }
            }
            if (what.equals("produce")) {
                weight = myChangeWeight.add(new BigDecimal(depGoodsDailyEntity.getGbDgdProduceWeight()));
                subtotal = myChangeSubtotal.add(new BigDecimal(depGoodsDailyEntity.getGbDgdProduceSubtotal()));
                depGoodsDailyEntity.setGbDgdProduceWeight(weight.toString());
                depGoodsDailyEntity.setGbDgdProduceSubtotal(subtotal.toString());

                if (!stock.getGbDgsSellingPrice().equals("-1")) {
                    BigDecimal profitSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdProfitSubtotal());
                    BigDecimal myChangeProfitSubtotal = new BigDecimal(stock.getGbDgsBetweenPrice()).multiply(myChangeWeight).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal newProfitSubtotal = myChangeProfitSubtotal.add(profitSubtotal);
                    depGoodsDailyEntity.setGbDgdProfitSubtotal(newProfitSubtotal.toString());

                    newSalesProfitSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdAfterProfitSubtotal()).add(myChangeProfitSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    depGoodsDailyEntity.setGbDgdAfterProfitSubtotal(newSalesProfitSubtotal.toString());
                    BigDecimal sellingPrice = new BigDecimal(stock.getGbDgsSellingPrice());
                    BigDecimal newSalesSubtotal = sellingPrice.multiply(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal salesSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdSalesSubtotal()).add(newSalesSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    depGoodsDailyEntity.setGbDgdSalesSubtotal(salesSubtotal.toString());
                }

                if (!stock.getGbDgsDate().equals(formatWhatDay(0))) { // 不是今天的批次
                    BigDecimal decimal = new BigDecimal(depGoodsDailyEntity.getGbDgdLastProduceWeight());
                    BigDecimal add = myChangeWeight.add(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    depGoodsDailyEntity.setGbDgdLastProduceWeight(add.toString());
                }

                //freshRate
                BigDecimal gbDgdProduceWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdProduceWeight());
                BigDecimal gbDgdLastWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdLastWeight());
                BigDecimal gbDgdLastProduceWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdLastProduceWeight());
                if (gbDgdLastWeight.compareTo(BigDecimal.ZERO) == 1) {
                    if (gbDgdProduceWeight.compareTo(gbDgdLastProduceWeight) == 1) {
                        BigDecimal subtract = gbDgdProduceWeight.subtract(gbDgdLastProduceWeight);
                        BigDecimal decimal = subtract.divide(gbDgdProduceWeight, 4, BigDecimal.ROUND_HALF_UP);
                        BigDecimal decimal1 = decimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        depGoodsDailyEntity.setGbDgdFreshRate(decimal1.toString());
                    } else {
                        depGoodsDailyEntity.setGbDgdFreshRate("0.0");
                    }
                } else {
                    depGoodsDailyEntity.setGbDgdFreshRate("100.00");
                }
            }

            if (what.equals("waste")) {
                weight = myChangeWeight.add(new BigDecimal(depGoodsDailyEntity.getGbDgdWasteWeight()));
                subtotal = myChangeSubtotal.add(new BigDecimal(depGoodsDailyEntity.getGbDgdWasteSubtotal()));
                depGoodsDailyEntity.setGbDgdWasteWeight(weight.toString());
                depGoodsDailyEntity.setGbDgdWasteSubtotal(subtotal.toString());
                if (!stock.getGbDgsSellingPrice().equals("-1")) {
                    newSalesProfitSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdAfterProfitSubtotal()).subtract(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    depGoodsDailyEntity.setGbDgdAfterProfitSubtotal(newSalesProfitSubtotal.toString());
                }
            }
            if (what.equals("return")) {
                weight = myChangeWeight.add(new BigDecimal(depGoodsDailyEntity.getGbDgdReturnWeight()));
                subtotal = myChangeSubtotal.add(new BigDecimal(depGoodsDailyEntity.getGbDgdReturnSubtotal()));
                depGoodsDailyEntity.setGbDgdReturnWeight(weight.toString());
                depGoodsDailyEntity.setGbDgdReturnSubtotal(subtotal.toString());
                System.out.println("returnrnrnrnrn" + depGoodsDailyEntity.getGbDgdReturnWeight());
            }

            // update restWeight
            BigDecimal newRestWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdRestWeight()).subtract(myChangeWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal newRestSubtotal = new BigDecimal(depGoodsDailyEntity.getGbDgdRestSubtotal()).subtract(myChangeSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            depGoodsDailyEntity.setGbDgdRestWeight(newRestWeight.toString());
            depGoodsDailyEntity.setGbDgdRestSubtotal(newRestSubtotal.toString());
            depGoodsDailyEntity.setGbDgdFullTime(formatFullTime());
            if (newRestWeight.compareTo(BigDecimal.ZERO) == 0) {
                Calendar calendar = Calendar.getInstance();
                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                depGoodsDailyEntity.setGbDgdSellClearHour(Integer.toString(hours));
                depGoodsDailyEntity.setGbDgdSellClearMinute(Integer.toString(minutes));
            }

            // update LastWeight
//            Integer gbDgsGbDisGoodsId = stock.getGbDgsGbDisGoodsId();
//            GbDistributerGoodsEntity distributerGoodsEntity = disGoodsService.queryObject(gbDgsGbDisGoodsId);
//            Integer gbDgControlFresh = distributerGoodsEntity.getGbDgControlFresh();

            //todo
//            if (!what.equals("produce") && gbDgControlFresh == 1) {
//                BigDecimal lastWeight = new BigDecimal(depGoodsDailyEntity.getGbDgdLastWeight());
//                if (lastWeight.compareTo(BigDecimal.ZERO) == 1) {
//                    String gbDgsDate = stock.getGbDgsDate();
//                    if (!gbDgsDate.equals(formatWhatDay(0))) { // 不是今天的批次
//                        depGoodsDailyEntity.setGbDgdFreshRate("0.0");
//                    }
//                }
//            }
            gbDepGoodsDailyService.update(depGoodsDailyEntity);
        }
    }

    private GbDepartmentGoodsStockReduceEntity addDepGoodsStockReduceEntity(GbDepartmentGoodsStockEntity stock, String what, Integer inventoryType, BigDecimal myChangeWeight,
                                                                            BigDecimal myChangeSubtotal, Integer doUserId) {

        GbDepartmentGoodsStockReduceEntity reduceEntity = new GbDepartmentGoodsStockReduceEntity();
        reduceEntity.setGbDgsrDoUserId(doUserId);
        reduceEntity.setGbDgsrGbDistributerId(stock.getGbDgsGbDistributerId());
        reduceEntity.setGbDgsrGbDepartmentId(stock.getGbDgsGbDepartmentId());
        reduceEntity.setGbDgsrGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
        reduceEntity.setGbDgsrGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
        reduceEntity.setGbDgsrGbGoodsInventoryType(inventoryType);
        reduceEntity.setGbDgsrGbDisGoodsGrandId(stock.getGbDgsGbDisGoodsGrandId());
        reduceEntity.setGbDgsrGbDisGoodsGreatId(stock.getGbDgsGbDisGoodsGreatId());
        reduceEntity.setGbDgsrGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
        reduceEntity.setGbDgsrGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
        reduceEntity.setGbDgsrGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
        reduceEntity.setGbDgsrFullTime(formatFullTime());
        reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReduceWeightUserId());
        reduceEntity.setGbDgsrDate(formatWhatDay(0));
        reduceEntity.setGbDgsrStockNxDistribtuerId(stock.getGbDgsNxDistributerId());
        reduceEntity.setGbDgsrStockNxSupplierId(stock.getGbDgsNxSupplierId());
        System.out.println("stockckkckcckkckc" + stock.getGbDgsNxSupplierId());
        reduceEntity.setGbDgsrStockNxSupplierId(stock.getGbDgsNxSupplierId());
        reduceEntity.setGbDgsrWeek(getWeekOfYear(0).toString());
        reduceEntity.setGbDgsrMonth(formatWhatMonth(0));
        reduceEntity.setGbDgsrGbPurGoodsId(stock.getGbDgsGbPurGoodsId());
        Integer gbDgsGbPurGoodsId = stock.getGbDgsGbPurGoodsId();
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDgsGbPurGoodsId);
        reduceEntity.setGbDgsrGbPurchaseDate(purchaseGoodsEntity.getGbDpgPurchaseDate());
        if (what.equals("loss")) {
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeLoss());
            reduceEntity.setGbDgsrStatus(0);
            reduceEntity.setGbDgsrLossWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrLossSubtotal(myChangeSubtotal.toString());
            reduceEntity.setGbDgsrProduceWeight("0");
            reduceEntity.setGbDgsrProduceSubtotal("0");
            reduceEntity.setGbDgsrReturnWeight("0");
            reduceEntity.setGbDgsrReturnSubtotal("0");
            reduceEntity.setGbDgsrWasteWeight("0");
            reduceEntity.setGbDgsrWasteSubtotal("0");
            reduceEntity.setGbDgsrCostWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrCostSubtotal(myChangeSubtotal.toString());

        } else if (what.equals("produce")) {
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeProduce());
            reduceEntity.setGbDgsrStatus(0);
            reduceEntity.setGbDgsrProduceWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrProduceSubtotal(myChangeSubtotal.toString());
            reduceEntity.setGbDgsrLossWeight("0");
            reduceEntity.setGbDgsrLossSubtotal("0");
            reduceEntity.setGbDgsrReturnWeight("0");
            reduceEntity.setGbDgsrReturnSubtotal("0");
            reduceEntity.setGbDgsrWasteWeight("0");
            reduceEntity.setGbDgsrWasteSubtotal("0");
            reduceEntity.setGbDgsrCostWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrCostSubtotal(myChangeSubtotal.toString());

        } else if (what.equals("return")) {
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeReturn());
            reduceEntity.setGbDgsrStatus(-1);
            reduceEntity.setGbDgsrReturnWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrReturnSubtotal(myChangeSubtotal.toString());
            reduceEntity.setGbDgsrLossWeight("0");
            reduceEntity.setGbDgsrLossSubtotal("0");
            reduceEntity.setGbDgsrProduceWeight("0");
            reduceEntity.setGbDgsrProduceSubtotal("0");
            reduceEntity.setGbDgsrWasteWeight("0");
            reduceEntity.setGbDgsrWasteSubtotal("0");
            reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReturnUserId());
            reduceEntity.setGbDgsrCostWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrCostSubtotal(myChangeSubtotal.toString());
        } else if (what.equals("waste")) {
            reduceEntity.setGbDgsrType(getGbDepartGoodsStockReduceTypeWaste());
            reduceEntity.setGbDgsrStatus(0);
            reduceEntity.setGbDgsrWasteWeight(myChangeWeight.toString());
            reduceEntity.setGbDgsrWasteSubtotal(myChangeSubtotal.toString());
            reduceEntity.setGbDgsrLossWeight("0");
            reduceEntity.setGbDgsrLossSubtotal("0");
            reduceEntity.setGbDgsrProduceWeight("0");
            reduceEntity.setGbDgsrProduceSubtotal("0");
            reduceEntity.setGbDgsrSalesSubtotal("0");
            reduceEntity.setGbDgsrReturnWeight("0");
            reduceEntity.setGbDgsrReturnSubtotal("0");
            reduceEntity.setGbDgsrDoUserId(stock.getGbDgsReturnUserId());
        }

        gbDepGoodsStockReduceService.save(reduceEntity);
        return reduceEntity;

    }


    private GbDepartmentDisGoodsEntity subscribeDepDisGoodsTotal(BigDecimal weight, BigDecimal subtotal, Integer depDisGoodsId) {
        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        BigDecimal weightB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).subtract(weight);
        BigDecimal subtotalB = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).subtract(subtotal);
        if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(new BigDecimal(0)) == 1) {
            BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
            BigDecimal showWeight = weightB.divide(showScale, 1, BigDecimal.ROUND_HALF_UP);
            depDisGoodsEntity.setGbDdgShowStandardWeight(showWeight.toString());
        }
        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subtotalB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weightB.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        if (weightB.compareTo(new BigDecimal(0)) == 0) {
            depDisGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        }

        gbDepartmentDisGoodsService.update(depDisGoodsEntity);
        return depDisGoodsEntity;
    }


    private void updateWeightGoodsData(GbDepartmentGoodsStockEntity stock, String what, BigDecimal myChangeWeight) {
        System.out.println("updateWeightGoodsDataupdateWeightGoodsData");
        Integer gbDgsWeightGoodsId = stock.getGbDgsWeightGoodsId();
        GbDistributerWeightGoodsEntity weightGoodsEntity = gbDisWeightGoodsService.queryObject(gbDgsWeightGoodsId);
        if (what.equals("loss")) {
            BigDecimal add = myChangeWeight.add(new BigDecimal(weightGoodsEntity.getGbDwgLossWeight()));
            weightGoodsEntity.setGbDwgLossWeight(add.toString());
            System.out.println("abccc" + weightGoodsEntity);
        }
        if (what.equals("waste")) {
            BigDecimal add = myChangeWeight.add(new BigDecimal(weightGoodsEntity.getGbDwgWasteWeight()));
            weightGoodsEntity.setGbDwgWasteWeight(add.toString());
        }
        if (what.equals("return")) {
            BigDecimal add = myChangeWeight.add(new BigDecimal(weightGoodsEntity.getGbDwgReturnWeight()));
            weightGoodsEntity.setGbDwgReturnWeight(add.toString());
        }
        gbDisWeightGoodsService.update(weightGoodsEntity);
    }


    @ResponseBody
    @RequestMapping("/saveOrdersGbJjAndSaveDepGoodsSx")
    public R saveOrdersGbJjAndSaveDepGoodsSx(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {

        // add purchaseGoods
        Integer gbDoDisGoodsId = gbDepartmentOrders.getGbDoDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        //添加部门商品
        GbDepartmentDisGoodsEntity mendianDisGoodsEntity = new GbDepartmentDisGoodsEntity();
        mendianDisGoodsEntity.setGbDdgDepGoodsName(gbDistributerGoodsEntity.getGbDgGoodsName());
        mendianDisGoodsEntity.setGbDdgDisGoodsId(gbDistributerGoodsEntity.getGbDistributerGoodsId());
        mendianDisGoodsEntity.setGbDdgDisGoodsFatherId(gbDistributerGoodsEntity.getGbDgDfgGoodsFatherId());
        mendianDisGoodsEntity.setGbDdgDisGoodsGrandId(gbDistributerGoodsEntity.getGbDgDfgGoodsGrandId());
        mendianDisGoodsEntity.setGbDdgDisGoodsGreatId(gbDistributerGoodsEntity.getGbDgDfgGoodsGreatId());
        mendianDisGoodsEntity.setGbDdgDepGoodsPinyin(gbDistributerGoodsEntity.getGbDgGoodsPinyin());
        mendianDisGoodsEntity.setGbDdgDepGoodsPy(gbDistributerGoodsEntity.getGbDgGoodsPy());
        mendianDisGoodsEntity.setGbDdgDepGoodsStandardname(gbDistributerGoodsEntity.getGbDgGoodsStandardname());
        mendianDisGoodsEntity.setGbDdgDepartmentId(gbDepartmentOrders.getGbDoDepartmentId());
        mendianDisGoodsEntity.setGbDdgDepartmentFatherId(gbDepartmentOrders.getGbDoDepartmentFatherId());
        mendianDisGoodsEntity.setGbDdgGbDepartmentId(gbDistributerGoodsEntity.getGbDgGbDepartmentId());
        mendianDisGoodsEntity.setGbDdgGbDisId(gbDistributerGoodsEntity.getGbDgDistributerId());
        mendianDisGoodsEntity.setGbDdgGoodsType(gbDistributerGoodsEntity.getGbDgGoodsType());
        mendianDisGoodsEntity.setGbDdgStockTotalWeight("0.0");
        mendianDisGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        mendianDisGoodsEntity.setGbDdgShowStandardId(-1);
        mendianDisGoodsEntity.setGbDdgShowStandardName(gbDistributerGoodsEntity.getGbDgGoodsStandardname());
        mendianDisGoodsEntity.setGbDdgShowStandardScale("-1");
        mendianDisGoodsEntity.setGbDdgShowStandardWeight(null);
        mendianDisGoodsEntity.setGbDdgNxDistributerGoodsId(gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId());
        mendianDisGoodsEntity.setGbDdgNxDistributerId(-1);
        gbDepartmentDisGoodsService.save(mendianDisGoodsEntity);

        gbDepartmentOrders.setGbDoDepDisGoodsId(mendianDisGoodsEntity.getGbDepartmentDisGoodsId());
        Integer gbDgDfgGoodsFatherId = gbDistributerGoodsEntity.getGbDgDfgGoodsFatherId();
        GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(gbDgDfgGoodsFatherId);
        Integer gbDfgFathersFatherId = fatherGoodsEntity.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity grandFather = gbDistributerFatherGoodsService.queryObject(gbDfgFathersFatherId);
        Integer greatFatherId = grandFather.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity greatFather = gbDistributerFatherGoodsService.queryObject(greatFatherId);


        gbDepartmentOrders.setGbDoDisGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        gbDepartmentOrders.setGbDoDisGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
        gbDepartmentOrders.setGbDoDisGoodsGreatId(greatFatherId);
        gbDepartmentOrders.setGbDoPurchaseGoodsId(-1);
        gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
        gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));


        gbDepartmentOrders.setGbDoNxGoodsGrandId(grandFather.getGbDfgNxGoodsId());
        gbDepartmentOrders.setGbDoNxGoodsGreatId(greatFather.getGbDfgNxGoodsId());

        gbDepartmentOrdersService.save(gbDepartmentOrders);

        //查询是否有采购的同一个商品
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", gbDepartmentOrders.getGbDoToDepartmentId());
        map.put("standard", gbDepartmentOrders.getGbDoStandard());
        map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
        map.put("status", 2); //有供货商的商品
        GbDistributerPurchaseGoodsEntity gbPurchaseGoodsEntity = new GbDistributerPurchaseGoodsEntity();

        System.out.println("putgodosmap" + map);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);

        if (purchaseGoodsEntities.size() == 0) {
            //是个新采购商品
            gbPurchaseGoodsEntity.setGbDpgPurchaseType(5);
            gbPurchaseGoodsEntity.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
            gbPurchaseGoodsEntity.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
            gbPurchaseGoodsEntity.setGbDpgApplyDate(formatWhatDay(0));
            gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(1);
            gbPurchaseGoodsEntity.setGbDpgOrdersFinishAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersWeightAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersBillAmount(0);
            gbPurchaseGoodsEntity.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
            gbPurchaseGoodsEntity.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
            gbPurchaseGoodsEntity.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
            gbPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
            gbDistributerPurchaseGoodsService.save(gbPurchaseGoodsEntity);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
        } else {
            //给老采购商品添加新订单
            System.out.println("newownwgopuggog");
            gbPurchaseGoodsEntity = purchaseGoodsEntities.get(0);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
            //采购商品订单数量更新
            Integer gbDpgOrdersAmount = gbPurchaseGoodsEntity.getGbDpgOrdersAmount();
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
            BigDecimal purQuantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
            BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
            BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbPurchaseGoodsEntity.setGbDpgQuantity(add.toString());
            gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);
        }


//        if (gbDistributerGoodsEntity.getGbDgNxDistributerId() != -1) {
        Integer nxDoDistributerId = gbDepartmentOrders.getGbDoNxDistributerId();
        Integer gbDoNxDistributerGoodsId = gbDepartmentOrders.getGbDoNxDistributerGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(gbDoNxDistributerGoodsId);
        String gbDoQuantity = gbDepartmentOrders.getGbDoQuantity();
        String gbDoStandard = gbDepartmentOrders.getGbDoStandard();
        String gbDoRemark = gbDepartmentOrders.getGbDoRemark();
        gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
        gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
        String gbDoApplyArriveDate = gbDepartmentOrders.getGbDoApplyArriveDate();
        Integer gbDoDepartmentId = gbDepartmentOrders.getGbDoDepartmentId();
        Integer gbDoDistributerId = gbDepartmentOrders.getGbDoDistributerId();
        Integer gbDoDepartmentFatherId = gbDepartmentOrders.getGbDoDepartmentFatherId();
        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
        //
        NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
        ordersEntity.setNxDoDistributerId(nxDoDistributerId);
        ordersEntity.setNxDoDisGoodsId(gbDoNxDistributerGoodsId);
        ordersEntity.setNxDoQuantity(gbDoQuantity);
        ordersEntity.setNxDoStandard(gbDoStandard);
        ordersEntity.setNxDoRemark(gbDoRemark);
        ordersEntity.setNxDoApplyDate(formatWhatDay(0));
        ordersEntity.setNxDoArriveOnlyDate(formatWhatDay(0));
        ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
        ordersEntity.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
        ordersEntity.setNxDoArriveDate(gbDoApplyArriveDate);
        ordersEntity.setNxDoGbDistributerId(gbDoDistributerId);
        ordersEntity.setNxDoGbDepartmentId(gbDoDepartmentId);
        ordersEntity.setNxDoGbDepartmentFatherId(gbDoDepartmentFatherId);
        ordersEntity.setNxDoDepartmentId(-1);
        ordersEntity.setNxDoDepartmentFatherId(-1);
        ordersEntity.setNxDoNxCommunityId(-1);
        ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
        ordersEntity.setNxDoNxCommRestrauntId(-1);
        ordersEntity.setNxDoNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
        ordersEntity.setNxDoNxGoodsFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
        ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
        ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
        ordersEntity.setNxDoDepDisGoodsId(-1);
        ordersEntity.setNxDoArriveWhatDay(getWeek(0));
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        ordersEntity.setNxDoGoodsType(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
        ordersEntity.setNxDoIsAgent(-1);
        ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
        ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPrice());
        ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceUpdate());
        ordersEntity.setNxDoPurchaseUserId(-1);
        ordersEntity.setNxDoGbDepartmentOrderId(gbDepartmentOrders.getGbDepartmentOrdersId());
        System.out.println("oreiidid==" + ordersEntity.getNxDoGbDepartmentOrderId());
        if (nxDistributerGoodsEntity.getNxDgPurchaseAuto() == -1) {
            ordersEntity.setNxDoPurchaseGoodsId(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
        } else {
            savePurGoodsAuto(ordersEntity);
        }

        if (ordersEntity.getNxDoStandard().equals(nxDistributerGoodsEntity.getNxDgGoodsStandardname())) {
            ordersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
            gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
            BigDecimal orderWeight = new BigDecimal(gbDepartmentOrders.getGbDoWeight());
            BigDecimal willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPrice());
            BigDecimal buyingPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPrice());
            String buyingPriceLevel = "0";
            String update = nxDistributerGoodsEntity.getNxDgBuyingPriceUpdate();
            if (nxDistributerGoodsEntity.getNxDgWillPriceOneWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOneWeight()).compareTo(BigDecimal.ZERO) == 1) {
                BigDecimal nxOneWeight = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOneWeight());
                if (orderWeight.compareTo(nxOneWeight) < 1) {
                    willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                    buyingPriceLevel = "1";
                } else {
                    if (nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight()).compareTo(BigDecimal.ZERO) == 1) {
                        BigDecimal nxTwoWeight = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight());
                        if (orderWeight.compareTo(nxTwoWeight) < 1) {
                            willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                            buyingPriceLevel = "2";
                        } else {
                            if (nxDistributerGoodsEntity.getNxDgWillPriceThreeWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceThreeWeight()).compareTo(BigDecimal.ZERO) == 1) {
                                willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceThree());
                                buyingPriceLevel = "3";
                            } else {
                                willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                                buyingPriceLevel = "2";
                            }
                        }
                    } else {
                        willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                        buyingPriceLevel = "1";
                    }

                }
            }


            BigDecimal orderSubtotal = willPrice.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(orderSubtotal.toString());
            ordersEntity.setNxDoCostPriceLevel(buyingPriceLevel);
            ordersEntity.setNxDoCostPrice(buyingPrice.toString());
            ordersEntity.setNxDoCostPriceUpdate(update);
            ordersEntity.setNxDoPrice(willPrice.toString());
            BigDecimal multiply = buyingPrice.multiply(new BigDecimal(ordersEntity.getNxDoWeight()));
            ordersEntity.setNxDoCostSubtotal(multiply.toString());
            gbDepartmentOrders.setGbDoPrice(willPrice.toString());


            //updateGbPurGoods
            gbPurchaseGoodsEntity.setGbDpgBuyPrice(willPrice.toString());
            gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbPurchaseGoodsEntity.getGbDpgQuantity());
            if (gbDepartmentOrders.getGbDoStandard().equals(gbDistributerGoodsEntity.getGbDgGoodsStandardname())) {
                System.out.println("zahduidididdi" + gbPurchaseGoodsEntity);
                BigDecimal quantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
                BigDecimal subtotal = quantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                gbPurchaseGoodsEntity.setGbDpgBuySubtotal(subtotal.toString());
            }
            gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);

            BigDecimal decimal = willPrice.multiply(new BigDecimal(gbPurchaseGoodsEntity.getGbDpgBuyQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbPurchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());


        } else {
            ordersEntity.setNxDoCostSubtotal("0");
            ordersEntity.setNxDoProfitSubtotal("0");
            ordersEntity.setNxDoCostPrice("0");
        }

        ordersEntity.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrdersService.saveForGb(ordersEntity);
        Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
        gbDepartmentOrders.setGbDoNxDepartmentOrderId(nxDepartmentOrdersId);
//        }
        gbDepartmentOrdersService.update(gbDepartmentOrders);

//        GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrders.getGbDepartmentOrdersId());
//        Map<String, Object> mapG = new HashMap<>();
//        mapG.put("disGoodsId", gbDoDisGoodsId);
//        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryDisGoodsWithDepDisGoods(mapG);
//        gbDepartmentOrdersEntity.setGbDistributerGoodsEntity(goodsEntity);
        Integer gbDoNxDepartmentOrderId = gbDepartmentOrders.getGbDoNxDepartmentOrderId();
        NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObject(nxDepartmentOrdersId);

        return R.ok().put("data", ordersEntity1);

    }

    @ResponseBody
    @RequestMapping("/saveOrdersGbJjAndSaveDepGoods")
    public R saveOrdersGbJjAndSaveDepGoods(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {


        // add purchaseGoods
        Integer gbDoDisGoodsId = gbDepartmentOrders.getGbDoDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        //添加部门商品
        GbDepartmentDisGoodsEntity mendianDisGoodsEntity = new GbDepartmentDisGoodsEntity();
        mendianDisGoodsEntity.setGbDdgDepGoodsName(gbDistributerGoodsEntity.getGbDgGoodsName());
        mendianDisGoodsEntity.setGbDdgDisGoodsId(gbDistributerGoodsEntity.getGbDistributerGoodsId());
        mendianDisGoodsEntity.setGbDdgDisGoodsFatherId(gbDistributerGoodsEntity.getGbDgDfgGoodsFatherId());
        mendianDisGoodsEntity.setGbDdgDisGoodsGrandId(gbDistributerGoodsEntity.getGbDgDfgGoodsGrandId());
        mendianDisGoodsEntity.setGbDdgDisGoodsGreatId(gbDistributerGoodsEntity.getGbDgDfgGoodsGreatId());
        mendianDisGoodsEntity.setGbDdgDepGoodsPinyin(gbDistributerGoodsEntity.getGbDgGoodsPinyin());
        mendianDisGoodsEntity.setGbDdgDepGoodsPy(gbDistributerGoodsEntity.getGbDgGoodsPy());
        mendianDisGoodsEntity.setGbDdgDepGoodsStandardname(gbDistributerGoodsEntity.getGbDgGoodsStandardname());
        mendianDisGoodsEntity.setGbDdgDepartmentId(gbDepartmentOrders.getGbDoDepartmentId());
        mendianDisGoodsEntity.setGbDdgDepartmentFatherId(gbDepartmentOrders.getGbDoDepartmentFatherId());
        mendianDisGoodsEntity.setGbDdgGbDepartmentId(gbDistributerGoodsEntity.getGbDgGbDepartmentId());
        mendianDisGoodsEntity.setGbDdgGbDisId(gbDistributerGoodsEntity.getGbDgDistributerId());
        mendianDisGoodsEntity.setGbDdgGoodsType(gbDistributerGoodsEntity.getGbDgGoodsType());
        mendianDisGoodsEntity.setGbDdgStockTotalWeight("0.0");
        mendianDisGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        mendianDisGoodsEntity.setGbDdgShowStandardId(-1);
        mendianDisGoodsEntity.setGbDdgShowStandardName(gbDistributerGoodsEntity.getGbDgGoodsStandardname());
        mendianDisGoodsEntity.setGbDdgShowStandardScale("-1");
        mendianDisGoodsEntity.setGbDdgShowStandardWeight(null);
        mendianDisGoodsEntity.setGbDdgNxDistributerGoodsId(gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId());
        mendianDisGoodsEntity.setGbDdgNxDistributerId(-1);
        gbDepartmentDisGoodsService.save(mendianDisGoodsEntity);

        gbDepartmentOrders.setGbDoDepDisGoodsId(mendianDisGoodsEntity.getGbDepartmentDisGoodsId());
        Integer gbDgDfgGoodsFatherId = gbDistributerGoodsEntity.getGbDgDfgGoodsFatherId();
        GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(gbDgDfgGoodsFatherId);
        Integer gbDfgFathersFatherId = fatherGoodsEntity.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity grandFather = gbDistributerFatherGoodsService.queryObject(gbDfgFathersFatherId);
        Integer greatFatherId = grandFather.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity greatFather = gbDistributerFatherGoodsService.queryObject(greatFatherId);
        gbDepartmentOrders.setGbDoDisGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        gbDepartmentOrders.setGbDoDisGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
        gbDepartmentOrders.setGbDoDisGoodsGreatId(greatFatherId);
        gbDepartmentOrders.setGbDoPurchaseGoodsId(-1);
        gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
        gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));

        gbDepartmentOrders.setGbDoNxGoodsGrandId(grandFather.getGbDfgNxGoodsId());
        gbDepartmentOrders.setGbDoNxGoodsGreatId(greatFather.getGbDfgNxGoodsId());

        gbDepartmentOrdersService.save(gbDepartmentOrders);

        //查询是否有采购的同一个商品
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", gbDepartmentOrders.getGbDoToDepartmentId());
        map.put("standard", gbDepartmentOrders.getGbDoStandard());
        map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
        GbDistributerPurchaseGoodsEntity gbPurchaseGoodsEntity = new GbDistributerPurchaseGoodsEntity();
        if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null && gbDistributerGoodsEntity.getGbDgGbSupplierId() != -1) {
            map.put("status", 2); //有供货商的商品

        } else {
            map.put("equalStatus", 0); //没有供货商的商品
        }

        System.out.println("putgodosmap" + map);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);

        if (purchaseGoodsEntities.size() == 0) {
            //是个新采购商品
            gbPurchaseGoodsEntity.setGbDpgPurchaseType(gbDepartmentOrders.getGbDoGoodsType());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsGrandId(gbDepartmentOrders.getGbDoDisGoodsGrandId());
            gbPurchaseGoodsEntity.setGbDpgDisGoodsGreatId(gbDepartmentOrders.getGbDoDisGoodsGreatId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxSupplierId(-1);
            gbPurchaseGoodsEntity.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
            gbPurchaseGoodsEntity.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
            gbPurchaseGoodsEntity.setGbDpgApplyDate(formatWhatDay(0));
            gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(1);
            gbPurchaseGoodsEntity.setGbDpgOrdersFinishAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersWeightAmount(0);
            gbPurchaseGoodsEntity.setGbDpgOrdersBillAmount(0);
            gbPurchaseGoodsEntity.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
            gbPurchaseGoodsEntity.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
            gbPurchaseGoodsEntity.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
            gbPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
            gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
            //standard Same
            if (gbDistributerGoodsEntity.getGbDgGoodsStandardname().equals(gbDepartmentOrders.getGbDoStandard())) {
                gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbDepartmentOrders.getGbDoQuantity());
            }
            gbDistributerPurchaseGoodsService.save(gbPurchaseGoodsEntity);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
            gbDepartmentOrdersService.update(gbDepartmentOrders);
        } else {
            //给老采购商品添加新订单
            System.out.println("newownwgopuggog");
            gbPurchaseGoodsEntity = purchaseGoodsEntities.get(0);
            Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
            //采购商品订单数量更新
            Integer gbDpgOrdersAmount = gbPurchaseGoodsEntity.getGbDpgOrdersAmount();
            gbPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
            BigDecimal purQuantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
            BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
            BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbPurchaseGoodsEntity.setGbDpgQuantity(add.toString());
            //standard Same
            if (gbDistributerGoodsEntity.getGbDgGoodsStandardname().equals(gbDepartmentOrders.getGbDoStandard())) {
                gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                gbPurchaseGoodsEntity.setGbDpgBuyQuantity(add.toString());
            }
            gbDepartmentOrders.setGbDoPurchaseGoodsId(gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId());
            gbDepartmentOrdersService.update(gbDepartmentOrders);
            gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);
        }


        Integer gbDoDepartmentFatherId = gbDepartmentOrders.getGbDoDepartmentFatherId();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(gbDoDepartmentFatherId);

        if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null && gbDistributerGoodsEntity.getGbDgGbSupplierId() != -1) {

            Map<String, Object> mapData = autoAddPurchaseBatch(gbDepartmentOrders, gbDistributerGoodsEntity);
            Integer batchId = (Integer) mapData.get("batchId");
            Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDepartmentDisId);

            NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDistributerGoodsEntity.getGbDgGbSupplierId());

            if (supplierEntity.getNxJrdhsUserId() != null) {
                Map<String, TemplateData> mapNotice = new HashMap<>();
                mapNotice.put("time2", new TemplateData(formatWhatDayTime(0)));
                mapNotice.put("thing13", new TemplateData(departmentEntity.getGbDepartmentName()));
                mapNotice.put("thing8", new TemplateData(gbDistributerGoodsEntity.getGbDgGoodsName()));
                mapNotice.put("thing10", new TemplateData("订货"));
                Integer gbDoOrderUserId = gbDepartmentOrders.getGbDoOrderUserId();
                GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(gbDoOrderUserId);
                mapNotice.put("thing9", new TemplateData(gbDepartmentUserEntity.getGbDuWxNickName()));
                System.out.println("nociiciiiicicautotootototoototo" + mapNotice);
                Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);

                StringBuilder pathBuilder = new StringBuilder("subPackage/pages/gbMarket/gbReceiveBatch/gbReceiveBatch");
                pathBuilder.append("?batchId=").append(batchId);
                pathBuilder.append("&retName=").append(gbDistributerEntity.getGbDistributerName());
                pathBuilder.append("&from=notification"); // 添加这个参数

                String path = pathBuilder.toString();
                System.out.println("Encoded URLARRRRRRRRRR00000000: " + path);
                WeNoticeService.autoGbSuppliertixingMessageJj(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);
            }

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("supplierId", supplierEntity.getNxJrdhSupplierId());
            mapR.put("gbDisId", gbDepartmentOrders.getGbDoDistributerId());
            mapR.put("nxDisId", supplierEntity.getNxJrdhsNxDistributerId());
            System.out.println("nxdidisiorororooror111" + mapR);
            NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDisGbDisService.queryObjectByParams(mapR);
            if (nxDistributerGbDistributerEntity != null) {
                GbDistributerPurchaseBatchEntity batchEntity = gbDPBService.queryObject(batchId);
                batchEntity.setGbDpbPurchaseType(5);
                batchEntity.setGbDpbNxDistributerId(nxDistributerGbDistributerEntity.getNxDgdNxDistributerId());
                gbDPBService.update(batchEntity);
                transToNxData(gbDistributerGoodsEntity, gbDepartmentOrders, supplierEntity.getNxJrdhsNxDistributerId());

            }

        }


        GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrders.getGbDepartmentOrdersId());
        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disGoodsId", gbDoDisGoodsId);
        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryDisGoodsWithDepDisGoods(mapG);
        gbDepartmentOrdersEntity.setGbDistributerGoodsEntity(goodsEntity);
        return R.ok().put("data", gbDepartmentOrdersEntity);

    }

    /**
     * ORDER,DISTRIBUTER
     * 添加订货申请
     *
     * @param gbDepartmentOrders 订货申请
     * @return ok
     */
    @ResponseBody
    @RequestMapping("/saveOrdersGb")
    public R saveOrdersGb(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {

        // add purchaseGoods
        Integer gbDoDisGoodsId = gbDepartmentOrders.getGbDoDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        Integer gbDoOrderType = gbDepartmentOrders.getGbDoOrderType();
        Integer gbDoGoodsType = gbDepartmentOrders.getGbDoGoodsType();

        gbDepartmentOrders.setGbDoPurchaseGoodsId(-1);

        //查询是否有采购的同一个商品
        Map<String, Object> map = new HashMap<>();
        map.put("purDepId", gbDepartmentOrders.getGbDoToDepartmentId());
        map.put("standard", gbDepartmentOrders.getGbDoStandard());
        map.put("purType", gbDoGoodsType);
        map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());

        GbDistributerPurchaseGoodsEntity gbPurchaseGoodsEntity = new GbDistributerPurchaseGoodsEntity();

        if (gbDoOrderType.equals(getGbOrderTypeJiCai())
                || gbDoOrderType.equals(getGbOrderTypeZiCai()) || gbDoOrderType.equals(getGbOrderTypeAppSupplier())) {
            if (gbDistributerGoodsEntity.getGbDgGbSupplierId() != null && gbDistributerGoodsEntity.getGbDgGbSupplierId() != -1) {
                map.put("status", 2); //有供货商的商品
                Map<String, Object> mapData = autoAddPurchaseBatch(gbDepartmentOrders, gbDistributerGoodsEntity);
                Integer purUserId = (Integer) mapData.get("purUserId");
                gbDepartmentOrders.setGbDoPurchaseUserId(purUserId);
                gbDepartmentOrders.setGbDoBuyStatus(getGbOrderBuyStatusProcurement());
                gbDepartmentOrdersService.update(gbDepartmentOrders);
            } else {
                map.put("equalStatus", 0); //没有供货商的商品
            }

            System.out.println("putgodosmap" + map);
            List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);

            if (goodsEntities.size() == 0) {
                //是个新采购商品
                gbPurchaseGoodsEntity.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
                gbPurchaseGoodsEntity.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
                gbPurchaseGoodsEntity.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
                gbPurchaseGoodsEntity.setGbDpgApplyDate(formatWhatDay(0));
                gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
                gbPurchaseGoodsEntity.setGbDpgOrdersAmount(1);
                gbPurchaseGoodsEntity.setGbDpgOrdersFinishAmount(0);
                gbPurchaseGoodsEntity.setGbDpgOrdersWeightAmount(0);
                gbPurchaseGoodsEntity.setGbDpgOrdersBillAmount(0);
                gbPurchaseGoodsEntity.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
                gbPurchaseGoodsEntity.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
                gbPurchaseGoodsEntity.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
                gbPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
                gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
                gbPurchaseGoodsEntity.setGbDpgPurchaseType(gbDoGoodsType);
                gbPurchaseGoodsEntity.setGbDpgBuySubtotal("0");

                System.out.println("nenenenneennenene" + gbPurchaseGoodsEntity.getGbDpgQuantity());
                gbDistributerPurchaseGoodsService.save(gbPurchaseGoodsEntity);
                Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
                gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
            } else {
                //给老采购商品添加新订单
                System.out.println("newownwgopuggog");
                gbPurchaseGoodsEntity = goodsEntities.get(0);
                Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
                gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                //采购商品订单数量更新
                Integer gbDpgOrdersAmount = gbPurchaseGoodsEntity.getGbDpgOrdersAmount();
                gbPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
                BigDecimal purQuantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
                BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
                BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                gbPurchaseGoodsEntity.setGbDpgQuantity(add.toString());
                gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);
            }

            //判断是否是配送商，自动生成配送供货商NxDistributer一个订单
            if (gbDoOrderType.equals(getGbOrderTypeAppSupplier())) {

                Integer nxDoDistributerId = gbDepartmentOrders.getGbDoNxDistributerId();
                Integer gbDoNxDistributerGoodsId = gbDepartmentOrders.getGbDoNxDistributerGoodsId();
                NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(gbDoNxDistributerGoodsId);
                String gbDoQuantity = gbDepartmentOrders.getGbDoQuantity();
                String gbDoStandard = gbDepartmentOrders.getGbDoStandard();
                String gbDoRemark = gbDepartmentOrders.getGbDoRemark();
                gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
                gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
                gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
                gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
                gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
                String gbDoApplyArriveDate = gbDepartmentOrders.getGbDoApplyArriveDate();
                Integer gbDoDepartmentId = gbDepartmentOrders.getGbDoDepartmentId();
                Integer gbDoDistributerId = gbDepartmentOrders.getGbDoDistributerId();
                Integer gbDoDepartmentFatherId = gbDepartmentOrders.getGbDoDepartmentFatherId();
                Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
                Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
                //
                NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
                ordersEntity.setNxDoDistributerId(nxDoDistributerId);
                ordersEntity.setNxDoDisGoodsId(gbDoNxDistributerGoodsId);
                ordersEntity.setNxDoQuantity(gbDoQuantity);
                ordersEntity.setNxDoStandard(gbDoStandard);
                ordersEntity.setNxDoRemark(gbDoRemark);
                ordersEntity.setNxDoApplyDate(formatWhatDay(0));
                ordersEntity.setNxDoArriveOnlyDate(formatWhatDay(0));
                ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
                ordersEntity.setNxDoApplyFullTime(formatWhatYearDayTime(0));
                ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
                ordersEntity.setNxDoArriveDate(gbDoApplyArriveDate);
                ordersEntity.setNxDoGbDistributerId(gbDoDistributerId);
                ordersEntity.setNxDoGbDepartmentId(gbDoDepartmentId);
                ordersEntity.setNxDoGbDepartmentFatherId(gbDoDepartmentFatherId);
                ordersEntity.setNxDoDepartmentId(-1);
                ordersEntity.setNxDoDepartmentFatherId(-1);
                ordersEntity.setNxDoNxCommunityId(-1);
                ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
                ordersEntity.setNxDoNxCommRestrauntId(-1);
                ordersEntity.setNxDoNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
                ordersEntity.setNxDoNxGoodsFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoDepDisGoodsId(-1);
                ordersEntity.setNxDoArriveWhatDay(getWeek(0));
                ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
                ordersEntity.setNxDoGoodsType(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
                ordersEntity.setNxDoIsAgent(-1);
                ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgGoodsStandardname());

                if (nxDistributerGoodsEntity.getNxDgPurchaseAuto() == -1) {
                    ordersEntity.setNxDoPurchaseGoodsId(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
                } else {
                    savePurGoodsAuto(ordersEntity);
                }

                if (ordersEntity.getNxDoStandard().equals(nxDistributerGoodsEntity.getNxDgGoodsStandardname())
                        && !nxDistributerGoodsEntity.getNxDgWillPrice().equals("0.1")) {
                    ordersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
                    gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                    BigDecimal orderWeight = new BigDecimal(gbDepartmentOrders.getGbDoWeight());
                    BigDecimal willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPrice());
                    BigDecimal buyingPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPrice());
                    String buyingPriceLevel = "0";
                    String update = nxDistributerGoodsEntity.getNxDgBuyingPriceUpdate();
                    if (nxDistributerGoodsEntity.getNxDgWillPriceOneWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOneWeight()).compareTo(BigDecimal.ZERO) == 1) {
                        BigDecimal nxOneWeight = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOneWeight());
                        if (orderWeight.compareTo(nxOneWeight) < 1) {
                            willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                            buyingPriceLevel = "1";
                        } else {
                            if (nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight()).compareTo(BigDecimal.ZERO) == 1) {
                                BigDecimal nxTwoWeight = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight());
                                if (orderWeight.compareTo(nxTwoWeight) < 1) {
                                    willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                                    buyingPriceLevel = "2";
                                } else {
                                    if (nxDistributerGoodsEntity.getNxDgWillPriceThreeWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceThreeWeight()).compareTo(BigDecimal.ZERO) == 1) {
                                        willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceThree());
                                        buyingPriceLevel = "3";
                                    } else {
                                        willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                                        buyingPriceLevel = "2";
                                    }
                                }
                            } else {
                                willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                                buyingPriceLevel = "1";
                            }

                        }
                    }


                    BigDecimal profitB = willPrice.subtract(buyingPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                    BigDecimal costSubtotalB = buyingPrice.multiply(new BigDecimal(gbDepartmentOrders.getGbDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal profitSubtotal = profitB.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal orderSubtotal = willPrice.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoCostSubtotal(costSubtotalB.toString());
                    ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());
                    ordersEntity.setNxDoSubtotal(orderSubtotal.toString());
                    ordersEntity.setNxDoCostPriceLevel(buyingPriceLevel);
                    ordersEntity.setNxDoCostPrice(buyingPrice.toString());
                    ordersEntity.setNxDoCostPriceUpdate(update);
                    BigDecimal multiply = buyingPrice.multiply(new BigDecimal(ordersEntity.getNxDoWeight()));
                    ordersEntity.setNxDoCostSubtotal(multiply.toString());

                    ordersEntity.setNxDoPrice(willPrice.toString());
                    gbDepartmentOrders.setGbDoPrice(willPrice.toString());

                    //profit
                    BigDecimal scaleB = profitB.divide(willPrice, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                    ordersEntity.setNxDoProfitScale(scaleB.toString());

                    //updateGbPurGoods
                    gbPurchaseGoodsEntity.setGbDpgBuyPrice(willPrice.toString());
                    gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbPurchaseGoodsEntity.getGbDpgQuantity());
                    if (gbDepartmentOrders.getGbDoStandard().equals(gbDistributerGoodsEntity.getGbDgGoodsStandardname())) {
                        System.out.println("zahduidididdi" + gbPurchaseGoodsEntity);
                        BigDecimal quantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
                        BigDecimal subtotal = quantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        gbPurchaseGoodsEntity.setGbDpgBuySubtotal(subtotal.toString());
                    }
                    gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);

                    BigDecimal decimal = willPrice.multiply(new BigDecimal(gbPurchaseGoodsEntity.getGbDpgBuyQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    gbPurchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());


                } else {
                    ordersEntity.setNxDoCostSubtotal("0");
                    ordersEntity.setNxDoProfitSubtotal("0");
                    ordersEntity.setNxDoCostPrice("0");
                }

                ordersEntity.setNxDoStatus(getNxOrderStatusNew());
                nxDepartmentOrdersService.saveForGb(ordersEntity);
                Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
                gbDepartmentOrders.setGbDoNxDepartmentOrderId(nxDepartmentOrdersId);
            }

        }

        Integer gbDoDisGoodsFatherId = gbDepartmentOrders.getGbDoDisGoodsFatherId();
        GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(gbDoDisGoodsFatherId);
        Integer gbDfgFathersFatherId = fatherGoodsEntity.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity grandFather = gbDistributerFatherGoodsService.queryObject(gbDfgFathersFatherId);
        Integer greatFatherId = grandFather.getGbDfgFathersFatherId();
        gbDepartmentOrders.setGbDoDisGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        gbDepartmentOrders.setGbDoDisGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
        gbDepartmentOrders.setGbDoDisGoodsGreatId(greatFatherId);


        GbDistributerFatherGoodsEntity greatFather = gbDistributerFatherGoodsService.queryObject(greatFatherId);
        gbDepartmentOrders.setGbDoNxGoodsGrandId(grandFather.getGbDfgNxGoodsId());
        gbDepartmentOrders.setGbDoNxGoodsGreatId(greatFather.getGbDfgNxGoodsId());


        gbDepartmentOrdersService.save(gbDepartmentOrders);

        //给NxDepartorder更新gbDepartmentOrderid
        if (gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId() != -1) {
            Integer gbDoNxDepartmentOrderId = gbDepartmentOrders.getGbDoNxDepartmentOrderId();
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
            ordersEntity.setNxDoGbDepartmentOrderId(gbDepartmentOrders.getGbDepartmentOrdersId());
            ordersEntity.setNxDoNxRestrauntOrderId(-1);
            nxDepartmentOrdersService.update(ordersEntity);
        }


        return R.ok().put("data", gbDepartmentOrdersService.queryObject(gbDepartmentOrders.getGbDepartmentOrdersId()));
    }


    private void savePurGoodsAuto(NxDepartmentOrdersEntity ordersEntity) {

        NxDistributerPurchaseGoodsEntity resultPurGoods = new NxDistributerPurchaseGoodsEntity();

        //判断是否有已经分的
        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(doDisGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", doDisGoodsId);
        map.put("status", 1);
        map.put("standard", ordersEntity.getNxDoStandard());
        System.out.println("purgogogo" + map);
        NxDistributerPurchaseGoodsEntity havePurGoods = nxDistributerPurchaseGoodsService.queryIfHavePurGoods(map);
        if (havePurGoods != null) {
            System.out.println("haovpururrorr++" + havePurGoods.getNxDpgBuyQuantity());
            resultPurGoods = havePurGoods;
            resultPurGoods.setNxDpgOrdersAmount(resultPurGoods.getNxDpgOrdersAmount() + 1);
            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                if (havePurGoods.getNxDpgBuyQuantity() != null) {
                    BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgQuantity());
                    BigDecimal decimal1 = new BigDecimal(ordersEntity.getNxDoQuantity());
                    BigDecimal totaoWeight = decimal.add(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(havePurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    resultPurGoods.setNxDpgQuantity(totaoWeight.toString());
                    resultPurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
                    resultPurGoods.setNxDpgBuySubtotal(decimal2.toString());
                }
            }
//
            nxDistributerPurchaseGoodsService.update(resultPurGoods);

        } else {
            System.out.println("newpururuururururur" + ordersEntity.getNxDoStandard());
            resultPurGoods.setNxDpgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
            resultPurGoods.setNxDpgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
            resultPurGoods.setNxDpgDistributerId(disGoods.getNxDgDistributerId());
            resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
            resultPurGoods.setNxDpgApplyDate(formatWhatYearDayTime(0));
            resultPurGoods.setNxDpgOrdersAmount(1);
            resultPurGoods.setNxDpgFinishAmount(0);
            resultPurGoods.setNxDpgPurchaseType(1);
            resultPurGoods.setNxDpgBuyPrice(disGoods.getNxDgBuyingPrice());
            resultPurGoods.setNxDpgDisGoodsId(doDisGoodsId);
            resultPurGoods.setNxDpgInputType(disGoods.getNxDgPurchaseAuto());
            resultPurGoods.setNxDpgStandard(ordersEntity.getNxDoStandard());
            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                BigDecimal orderWeight = new BigDecimal(ordersEntity.getNxDoQuantity());
                BigDecimal decimal2 = orderWeight.multiply(new BigDecimal(resultPurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                resultPurGoods.setNxDpgBuyQuantity(orderWeight.toString());
                resultPurGoods.setNxDpgQuantity(orderWeight.toString());
                resultPurGoods.setNxDpgBuySubtotal(decimal2.toString());
            }
            nxDistributerPurchaseGoodsService.save(resultPurGoods);

        }

        GbDistributerPurchaseBatchEntity batchEntity = new GbDistributerPurchaseBatchEntity();

        //给autoBatch更新gbDepartmentOrderid
        System.out.println("suplieridid" + disGoods.getNxDgGoodsName() + disGoods.getNxDgSupplierId());
        if (disGoods.getNxDgSupplierId() != null) {

            //
            Map<String, Object> mapBatch = new HashMap<>();
            Integer gbDgGbSupplierId = disGoods.getNxDgSupplierId();
            mapBatch.put("supplierId", gbDgGbSupplierId);
            mapBatch.put("status", 1);
            mapBatch.put("purchaseType", 2);

            List<GbDistributerPurchaseBatchEntity> entities = gbDPBService.queryDisPurchaseBatch(mapBatch);
            if (entities.size() == 0) {
                //
                batchEntity.setGbDpbDate(formatWhatDay(0));
                batchEntity.setGbDpbTime(formatWhatTime(0));
                batchEntity.setGbDpbPurchaseMonth(formatWhatMonth(0));
                batchEntity.setGbDpbPurchaseWeek(getWeek(0));
                batchEntity.setGbDpbPurchaseYear(formatWhatYear(0));
                batchEntity.setGbDpbPurchaseFullTime(formatWhatYearDayTime(0));
                batchEntity.setGbDpbStatus(-1);
                batchEntity.setGbDpbPurchaseType(2);
                batchEntity.setGbDpbSupplierId(gbDgGbSupplierId);
                NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDgGbSupplierId);
                batchEntity.setGbDpbSellUserId(supplierEntity.getNxJrdhsUserId());
                batchEntity.setGbDpbDistributerId(ordersEntity.getNxDoDistributerId());
                batchEntity.setGbDpbBuyUserId(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                batchEntity.setGbDpbBuyUserOpenId(nxJrdhUserEntity.getNxJrdhWxOpenId());
                batchEntity.setGbDpbPayType(1);
                gbDPBService.save(batchEntity);

                resultPurGoods.setNxDpgBatchId(batchEntity.getGbDistributerPurchaseBatchId());
                resultPurGoods.setNxDpgPurchaseDate(formatWhatDay(0));
                resultPurGoods.setNxDpgTime(formatWhatYearDayTime(0));
                resultPurGoods.setNxDpgDistributerId(batchEntity.getGbDpbDistributerId());
                resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
                nxDistributerPurchaseGoodsService.update(resultPurGoods);
            } else {
                batchEntity = entities.get(0);
                resultPurGoods.setNxDpgBatchId(batchEntity.getGbDistributerPurchaseBatchId());
                nxDistributerPurchaseGoodsService.update(resultPurGoods);
            }

            Map<String, TemplateData> mapNotice = new HashMap<>();
            mapNotice.put("thing1", new TemplateData(disGoods.getNxDgGoodsName()));
            mapNotice.put("time6", new TemplateData(formatWhatDay(0)));
            Integer nxDpbDistributerId = batchEntity.getGbDpbDistributerId();
            NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDpbDistributerId);
            mapNotice.put("thing7", new TemplateData(distributerEntity.getNxDistributerName()));
            mapNotice.put("thing8", new TemplateData(distributerEntity.getNxDistributerPhone()));
            StringBuilder pathBuilder = new StringBuilder("pages/txs/disOrderBatch/disOrderBatch");
            pathBuilder.append("?batchId=").append(batchEntity.getGbDistributerPurchaseBatchId());
            pathBuilder.append("&retName=").append(nxDistributerService.queryObject(batchEntity.getGbDpbDistributerId()).getNxDistributerName());
            pathBuilder.append("&disId=").append(batchEntity.getGbDpbDistributerId());
            pathBuilder.append("&buyUserId=").append(batchEntity.getGbDpbBuyUserId());
            pathBuilder.append("&purUserId=").append(batchEntity.getGbDpbPurUserId());
            pathBuilder.append("&fromBuyer=1");
            String path = pathBuilder.toString();
            System.out.println("paththhththt" + path);

            NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(disGoods.getNxDgSupplierId());
            Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);
            System.out.println("suppsleir" + path);
            WeNoticeService.nxSupplierOrderSave(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);

            ordersEntity.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishBuy());
        }

        ordersEntity.setNxDoPurchaseGoodsId(resultPurGoods.getNxDistributerPurchaseGoodsId());
        nxDepartmentOrdersService.update(ordersEntity);

    }

    private Map<String, Object> autoAddPurchaseBatch(GbDepartmentOrdersEntity ordersEntity, GbDistributerGoodsEntity goodsEntity) {
        Map<String, Object> mapData = new HashMap<>();
        //
        System.out.println("autotoototoba");
        Integer gbDgGbSupplierId = goodsEntity.getGbDgGbSupplierId();
        NxJrdhSupplierEntity nxJrdhSupplierEntity = jrdhSupplierService.queryObject(gbDgGbSupplierId);
        Integer nxJrdhsUserId = nxJrdhSupplierEntity.getNxJrdhsUserId();
        Integer nxJrdhsGbDepartmentId = nxJrdhSupplierEntity.getNxJrdhsGbDepartmentId();
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", gbDgGbSupplierId);
        map.put("status", 1);
        map.put("notEqualPurchaseType", 9);
        List<GbDistributerPurchaseBatchEntity> entities = gbDPBService.queryDisPurchaseBatch(map);

        if (entities.size() == 0) {

            GbDistributerPurchaseBatchEntity batchEntity = new GbDistributerPurchaseBatchEntity();
            batchEntity.setGbDpbDate(formatWhatDay(0));
            batchEntity.setGbDpbHour(formatWhatHour(0));
            batchEntity.setGbDpbMinute(formatWhatMinute(0));
            batchEntity.setGbDpbTime(formatWhatTime(0));
            batchEntity.setGbDpbPurchaseMonth(formatWhatMonth(0));
            batchEntity.setGbDpbPurchaseWeek(getWeek(0));
            batchEntity.setGbDpbPurchaseYear(formatWhatYear(0));
            batchEntity.setGbDpbPurchaseFullTime(formatWhatYearDayTime(0));
            batchEntity.setGbDpbStatus(-1);
            batchEntity.setGbDpbDistributerId(ordersEntity.getGbDoDistributerId());
            batchEntity.setGbDpbPurDepartmentId(nxJrdhsGbDepartmentId);
            batchEntity.setGbDpbNxDistributerId(nxJrdhSupplierEntity.getNxJrdhsNxDistributerId());
            if (nxJrdhSupplierEntity.getNxJrdhsNxJrdhBuyUserId() != null) {
                Integer nxJrdhsGbPurUserId = nxJrdhSupplierEntity.getNxJrdhsNxJrdhBuyUserId();
                GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(nxJrdhsGbPurUserId);
                Integer gbDuAdmin = gbDepartmentUserEntity.getGbDuAdmin();
                batchEntity.setGbDpbBuyUserId(nxJrdhsGbPurUserId);
                batchEntity.setGbDpbBuyUserOpenId(gbDepartmentUserService.queryObject(nxJrdhsGbPurUserId).getGbDuWxOpenId());
                batchEntity.setGbDpbUserAdminType(gbDuAdmin);
            } else {

                batchEntity.setGbDpbBuyUserId(-1);
                batchEntity.setGbDpbBuyUserOpenId("-1");
                batchEntity.setGbDpbUserAdminType(-1);
            }

            batchEntity.setGbDpbSupplierId(gbDgGbSupplierId);
            batchEntity.setGbDpbSellUserId(nxJrdhsUserId);
            batchEntity.setGbDpbPurchaseType(21);
            batchEntity.setGbDpbSubtotal("0");
            batchEntity.setGbDpbPayType(1);
            gbDPBService.save(batchEntity);

            Integer gbDoPurchaseGoodsId = ordersEntity.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity gbDistributerPurchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
            gbDistributerPurchaseGoodsEntity.setGbDpgBatchId(batchEntity.getGbDistributerPurchaseBatchId());
            gbDistributerPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseDate(formatWhatDay(0));
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseMonth(formatWhatMonth(0));
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseYear(formatWhatYear(0));
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseWeek(getWeek(0));
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(batchEntity.getGbDpbPurDepartmentId());
            gbDistributerPurchaseGoodsEntity.setGbDpgTime(formatWhatTime(0));
            gbDistributerPurchaseGoodsEntity.setGbDpgPurUserId(-1);
            gbDistributerPurchaseGoodsEntity.setGbDpgPurchaseNxSupplierId(gbDgGbSupplierId);

            Map<String, Object> mapItem = new HashMap<>();
            mapItem.put("disGoodsId", gbDistributerPurchaseGoodsEntity.getGbDpgDisGoodsId());
            mapItem.put("supplierId", gbDgGbSupplierId);
            mapItem.put("dayuStatus", 2);
            GbDistributerPurchaseGoodsEntity lastItem = gbDistributerPurchaseGoodsService.queryPurchaseGoodsLastItem(mapItem);
            if (lastItem != null) {

                ordersEntity.setGbDoBuyStatus(1);
                ordersEntity.setGbDoPrice(lastItem.getGbDpgBuyPrice());
                //give price
                gbDistributerPurchaseGoodsEntity.setGbDpgBuyPrice(lastItem.getGbDpgBuyPrice());
                // give subtotal
                if (gbDistributerPurchaseGoodsEntity.getGbDpgStandard().equals(goodsEntity.getGbDgGoodsStandardname())) {
                    BigDecimal multiply = new BigDecimal(ordersEntity.getGbDoQuantity()).multiply(new BigDecimal(lastItem.getGbDpgBuyPrice()));
                    ordersEntity.setGbDoSubtotal(multiply.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    ordersEntity.setGbDoWeight(ordersEntity.getGbDoQuantity());
                    BigDecimal add = new BigDecimal(gbDistributerPurchaseGoodsEntity.getGbDpgBuyQuantity()).add(new BigDecimal(ordersEntity.getGbDoQuantity()));
                    BigDecimal bigDecimal = new BigDecimal(lastItem.getGbDpgBuyPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    gbDistributerPurchaseGoodsEntity.setGbDpgBuyQuantity(add.toString());
                    gbDistributerPurchaseGoodsEntity.setGbDpgBuySubtotal(bigDecimal.toString());
                }

            }

            gbDepartmentOrdersService.update(ordersEntity);
            gbDistributerPurchaseGoodsService.update(gbDistributerPurchaseGoodsEntity);
            mapData.put("batchId", batchEntity.getGbDistributerPurchaseBatchId());
            return mapData;
        } else {


            GbDistributerPurchaseBatchEntity batchEntity = entities.get(0);
            Integer gbDoPurchaseGoodsId = ordersEntity.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity gbDistributerPurchaseGoodsEntity1 = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
            gbDistributerPurchaseGoodsEntity1.setGbDpgBatchId(batchEntity.getGbDistributerPurchaseBatchId());
            gbDistributerPurchaseGoodsEntity1.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
            gbDistributerPurchaseGoodsEntity1.setGbDpgPurchaseDate(formatWhatDay(0));
            gbDistributerPurchaseGoodsEntity1.setGbDpgPurchaseMonth(formatWhatMonth(0));
            gbDistributerPurchaseGoodsEntity1.setGbDpgPurchaseYear(formatWhatYear(0));
            gbDistributerPurchaseGoodsEntity1.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
            gbDistributerPurchaseGoodsEntity1.setGbDpgPurchaseWeek(getWeek(0));
            gbDistributerPurchaseGoodsEntity1.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            gbDistributerPurchaseGoodsEntity1.setGbDpgPurchaseDepartmentId(batchEntity.getGbDpbPurDepartmentId());
            gbDistributerPurchaseGoodsEntity1.setGbDpgTime(formatWhatTime(0));

            Map<String, Object> mapItem = new HashMap<>();
            mapItem.put("disGoodsId", gbDistributerPurchaseGoodsEntity1.getGbDpgDisGoodsId());
            mapItem.put("supplierId", gbDgGbSupplierId);
            mapItem.put("dayuStatus", 2);
            GbDistributerPurchaseGoodsEntity lastItem = gbDistributerPurchaseGoodsService.queryPurchaseGoodsLastItem(mapItem);
            if (lastItem != null) {

                ordersEntity.setGbDoBuyStatus(1);
                ordersEntity.setGbDoPrice(lastItem.getGbDpgBuyPrice());
                //give price
                gbDistributerPurchaseGoodsEntity1.setGbDpgBuyPrice(lastItem.getGbDpgBuyPrice());
                // give subtotal
                if (gbDistributerPurchaseGoodsEntity1.getGbDpgStandard().equals(goodsEntity.getGbDgGoodsStandardname())) {
                    BigDecimal multiply = new BigDecimal(ordersEntity.getGbDoQuantity()).multiply(new BigDecimal(lastItem.getGbDpgBuyPrice()));
                    ordersEntity.setGbDoWeight(ordersEntity.getGbDoQuantity());
                    ordersEntity.setGbDoSubtotal(multiply.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    BigDecimal add = new BigDecimal(gbDistributerPurchaseGoodsEntity1.getGbDpgBuyQuantity()).add(new BigDecimal(ordersEntity.getGbDoQuantity()));
                    BigDecimal bigDecimal = new BigDecimal(lastItem.getGbDpgBuyPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    gbDistributerPurchaseGoodsEntity1.setGbDpgBuyQuantity(add.toString());
                    gbDistributerPurchaseGoodsEntity1.setGbDpgBuySubtotal(bigDecimal.toString());
                }

            }

            gbDepartmentOrdersService.update(ordersEntity);

            gbDistributerPurchaseGoodsService.update(gbDistributerPurchaseGoodsEntity1);
            mapData.put("batchId", batchEntity.getGbDistributerPurchaseBatchId());
            return mapData;
        }

    }

    @RequestMapping(value = "/receiveReturnApply", method = RequestMethod.POST)
    @ResponseBody
    public R receiveReturnApply(@RequestBody GbDepartmentOrdersEntity order) {
        System.out.println("orderst" + order.getGbDoStatus());
        //
        if (order.getGbDoStatus().equals(getGbOrderStatusReceived())) {
            GbDepartmentBillEntity billEntity = new GbDepartmentBillEntity();
            String areaCode = "1" + order.getGbDoDistributerId();
            billEntity.setGbDbStatus(0);
            billEntity.setGbDbDisId(order.getGbDoDistributerId());
            billEntity.setGbDbDepId(order.getGbDoDepartmentId());
            billEntity.setGbDbDate(formatWhatDay(0));
            billEntity.setGbDbTime(formatWhatYearDayTime(0));
            billEntity.setGbDbMonth(formatWhatMonth(0));
            billEntity.setGbDbWeek(getWeek(0));
            billEntity.setGbDbTradeNo(generateBillTradeNo(areaCode));
            billEntity.setGbDbIssueDepId(order.getGbDoToDepartmentId());
            billEntity.setGbDbIssueOrderType(getGbOrderTypeTuihuo());
            billEntity.setGbDbIssueUserId(order.getGbDoReturnUserId());
            billEntity.setGbDbPrintTimes(0);
            billEntity.setGbDbTotal(order.getGbDoSubtotal());
            billEntity.setGbDbSellingTotal(order.getGbDoSellingSubtotal());
            billEntity.setGbDbOrderAmount(1);
            billEntity.setGbDbPayTotal(order.getGbDoSubtotal());
            gbDepartmentBillService.save(billEntity);

            order.setGbDoBillId(billEntity.getGbDepartmentBillId());
            order.setGbDoStatus(getGbOrderStatusReceived());

            Integer gbDoDgsrReturnId = order.getGbDoDgsrReturnId();
            GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepGoodsStockReduceService.queryObject(gbDoDgsrReturnId);
            reduceEntity.setGbDgsrStatus(0);
            gbDepGoodsStockReduceService.update(reduceEntity);

            order.setGbDoArriveWeeksYear(getWeekOfYear(0));
            order.setGbDoArriveWhatDay(getWeek(0));
            order.setGbDoArriveOnlyDate(formatWhatDate(0));
            order.setGbDoArriveDate(formatWhatDay(0));
            gbDepartmentOrdersService.update(order);

            //如果是库存商品退货，还要update退货时候添加到退货部门的stock
            GbDepartmentGoodsStockEntity stockEntity = gbDepartmentGoodsStockService.queryReturnStockItemByOrderId(order.getGbDepartmentOrdersId());
            System.out.println("stockckckkde" + stockEntity);
            if (stockEntity != null) {
                stockEntity.setGbDgsStatus(0);
                System.out.println("stoenweieie" + stockEntity.getGbDgsWeight());
                System.out.println("stoenweieie" + stockEntity.getGbDepartmentGoodsStockId());
                stockEntity.setGbDgsRestWeight(stockEntity.getGbDgsWeight());
                stockEntity.setGbDgsRestSubtotal(stockEntity.getGbDgsSubtotal());
                gbDepartmentGoodsStockService.update(stockEntity);

                Integer gbDgsrGbGoodsStockId = reduceEntity.getGbDgsrGbGoodsStockId();
                GbDepartmentGoodsStockEntity orignalStock = gbDepartmentGoodsStockService.queryObject(gbDgsrGbGoodsStockId);
                Integer gbDgsGbGoodsStockId = orignalStock.getGbDgsGbGoodsStockId();
                GbDepartmentGoodsStockEntity fromStock = gbDepartmentGoodsStockService.queryObject(gbDgsGbGoodsStockId);
                System.out.println("formssotokkdkdk" + fromStock.getGbDepartmentGoodsStockId());
                System.out.println("formssotokkdkdk" + fromStock.getGbDgsGbDepDisGoodsId());
                updateDepDisGoods(stockEntity, fromStock.getGbDgsGbDepDisGoodsId(), "add");

                updateDepGoodsDailyBusiness(stockEntity);
            }
        } else {
            gbDepartmentOrdersService.update(order);
        }
        return R.ok();
    }


    @RequestMapping(value = "/receiveReturnApplyNx", method = RequestMethod.POST)
    @ResponseBody
    public R receiveReturnApplyNx(@RequestBody NxDepartmentOrdersEntity nxOrder) {
        Integer gbDepartmentOrderId = nxOrder.getNxDoGbDepartmentOrderId();
        GbDepartmentOrdersEntity order = gbDepartmentOrdersService.queryObject(gbDepartmentOrderId);

        GbDepartmentBillEntity billEntity = new GbDepartmentBillEntity();
        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", order.getGbDoDepartmentId());
        map3.put("date", formatWhatDay(0));
        int count = gbDepartmentBillService.queryDepartmentBillCount(map3);
        int trade = count + 1;
        String no = "";
        if (trade < 100) {
            if (count < 10) {
                no = "00" + trade;
            } else {
                no = "0" + trade;
            }
        } else {
            no = String.valueOf(count);
        }
        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(order.getGbDoDepartmentId());
        String headPinyin = getHeadStringByString(gbDepartmentEntity.getGbDepartmentName(), true, null);
        String s = formatDayNumber(0) + headPinyin + no;
        billEntity.setGbDbStatus(0);
        billEntity.setGbDbDisId(order.getGbDoDistributerId());
        billEntity.setGbDbDepId(order.getGbDoDepartmentId());
        billEntity.setGbDbDate(formatWhatDay(0));
        billEntity.setGbDbTime(formatWhatYearDayTime(0));
        billEntity.setGbDbMonth(formatWhatMonth(0));
        billEntity.setGbDbWeek(getWeek(0));
        billEntity.setGbDbTradeNo(generateBillTradeNo(s));
        billEntity.setGbDbIssueDepId(order.getGbDoToDepartmentId());
        billEntity.setGbDbIssueOrderType(getGbOrderTypeTuihuo());
        billEntity.setGbDbIssueUserId(order.getGbDoReturnUserId());
        billEntity.setGbDbPrintTimes(0);
        billEntity.setGbDbTotal(order.getGbDoSubtotal());
        billEntity.setGbDbIssueNxDisId(order.getGbDoNxDistributerId());
        billEntity.setGbDbOrderAmount(1);
        billEntity.setGbDbPayTotal(order.getGbDoSubtotal());
        gbDepartmentBillService.save(billEntity);

        order.setGbDoBillId(billEntity.getGbDepartmentBillId());
        order.setGbDoStatus(getGbOrderStatusReceived());
        order.setGbDoArriveWeeksYear(getWeekOfYear(0));
        order.setGbDoArriveWhatDay(getWeek(0));
        order.setGbDoArriveOnlyDate(formatWhatDate(0));
        order.setGbDoArriveDate(formatWhatDay(0));
        gbDepartmentOrdersService.update(order);


        //
        NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
        String areaCode = "1" + order.getGbDoNxDistributerId();
        String ss = generateBillTradeNo(areaCode);
        nxDepartmentBill.setNxDbTradeNo(ss);
        nxDepartmentBill.setNxDbDisId(order.getGbDoNxDistributerId());
        nxDepartmentBill.setNxDbDepId(-1);
        nxDepartmentBill.setNxDbGbDepFatherId(-1);
        nxDepartmentBill.setNxDbTotal(order.getGbDoSubtotal());
        nxDepartmentBill.setNxDbPrintTimes(1);
        nxDepartmentBill.setNxDbGbDisId(order.getGbDoDistributerId());
        nxDepartmentBill.setNxDbGbDepId(order.getGbDoDepartmentId());

        nxDepartmentBill.setNxDbNxCommunityId(-1);
        nxDepartmentBill.setNxDbNxRestrauntId(-1);
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBillService.save(nxDepartmentBill);

        nxOrder.setNxDoStatus(3);
        nxOrder.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
        nxOrder.setNxDoReturnBillId(nxDepartmentBill.getNxDepartmentBillId());
        System.out.println("nxdretunntbiilllllssss" + nxOrder.getNxDoStatus());
        nxDepartmentOrdersService.update(nxOrder);


        Integer gbDoDgsrReturnId = order.getGbDoDgsrReturnId();
        GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepGoodsStockReduceService.queryObject(gbDoDgsrReturnId);
        reduceEntity.setGbDgsrStatus(0);
        gbDepGoodsStockReduceService.update(reduceEntity);

        return R.ok();
    }

    /**
     * ORDER
     * 修改申请
     *
     * @param gbDepartmentOrders 订货申请
     * @return ok
     */
    @ResponseBody
    @RequestMapping("/updateOrderGb")
    public R updateOrderGb(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {

        //检查修改规格
        Integer gbDepartmentOrdersId = gbDepartmentOrders.getGbDepartmentOrdersId();
        GbDepartmentOrdersEntity oldOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        String oldStandard = oldOrdersEntity.getGbDoStandard();
        String gbDoStandard1 = gbDepartmentOrders.getGbDoStandard();

        Integer oldGbDoToDepartmentId = oldOrdersEntity.getGbDoToDepartmentId();
        Integer gbDoToDepartmentId = gbDepartmentOrders.getGbDoToDepartmentId();

        //修改订单采购的单位
        if (!oldGbDoToDepartmentId.equals(gbDoToDepartmentId)) {
            Integer gbDoDepartmentId = gbDepartmentOrders.getGbDoDepartmentId();

            //新修改为自采订单
            if (gbDoToDepartmentId.equals(gbDoDepartmentId)) {
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
                map.put("equalStatus", 0);
                map.put("purDepId", gbDepartmentOrders.getGbDoToDepartmentId());
                map.put("standard", gbDepartmentOrders.getGbDoStandard());
                List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);
                if (goodsEntities.size() == 0) {
                    //是个新采购商品
                    GbDistributerPurchaseGoodsEntity newPurGoods = new GbDistributerPurchaseGoodsEntity();
                    newPurGoods.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
                    newPurGoods.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
                    newPurGoods.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
                    newPurGoods.setGbDpgApplyDate(formatWhatDay(0));
                    newPurGoods.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
                    newPurGoods.setGbDpgOrdersAmount(1);
                    newPurGoods.setGbDpgOrdersFinishAmount(0);
                    newPurGoods.setGbDpgOrdersWeightAmount(0);
                    newPurGoods.setGbDpgOrdersBillAmount(0);
                    newPurGoods.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
                    newPurGoods.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
                    newPurGoods.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
                    newPurGoods.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
                    newPurGoods.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
                    newPurGoods.setGbDpgPurchaseType(1);
                    newPurGoods.setGbDpgPurchaseWeek(getWeek(0));
                    newPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                    gbDistributerPurchaseGoodsService.save(newPurGoods);
                    Integer gbDistributerPurchaseGoodsId = newPurGoods.getGbDistributerPurchaseGoodsId();
                    gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                } else {
                    // 3， 给老采购商品添加新订单
                    GbDistributerPurchaseGoodsEntity gbDisPurGoodsEntity = goodsEntities.get(0);
                    Integer gbDistributerPurchaseGoodsId = gbDisPurGoodsEntity.getGbDistributerPurchaseGoodsId();
                    gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                    //采购商品订单数量更新
                    Integer gbDpgOrdersAmount = gbDisPurGoodsEntity.getGbDpgOrdersAmount();
                    gbDisPurGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
                    BigDecimal purQuantity = new BigDecimal(gbDisPurGoodsEntity.getGbDpgQuantity());
                    BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
                    BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                    gbDisPurGoodsEntity.setGbDpgQuantity(add.toString());
                    gbDistributerPurchaseGoodsService.update(gbDisPurGoodsEntity);
                }

            } else {
                //修改自采订单的purGoods
                Integer gbDoPurchaseGoodsId = gbDepartmentOrders.getGbDoPurchaseGoodsId();

                if (gbDoPurchaseGoodsId != -1) {
                    GbDistributerPurchaseGoodsEntity gbDistributerPurchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
                    Integer gbDpgOrdersAmount = gbDistributerPurchaseGoodsEntity.getGbDpgOrdersAmount();
                    if (gbDpgOrdersAmount > 1) {
                        gbDistributerPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount - 1);
                        gbDistributerPurchaseGoodsService.update(gbDistributerPurchaseGoodsEntity);
                    } else {
                        //订货批次是否是最后一个采购商品
                        Integer gbDpgBatchId = gbDistributerPurchaseGoodsEntity.getGbDpgBatchId();
                        Map<String, Object> mapBatch = new HashMap<>();
                        mapBatch.put("batchId", gbDpgBatchId);
                        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(mapBatch);
                        if (goodsEntities.size() == 1) {
                            gbDPBService.delete(gbDistributerPurchaseGoodsEntity.getGbDpgBatchId());
                        }
                        gbDistributerPurchaseGoodsService.delete(gbDoPurchaseGoodsId);
                    }
                }


                //将自采订单返回原采购部门订单
                //1 如果商品是集采商品，则添加 purGooods
                Integer gbDoDisGoodsId = gbDepartmentOrders.getGbDoDisGoodsId();
                GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
                if (gbDistributerGoodsEntity.getGbDgGoodsType().equals(getGbDisGoodsTypeJicai())) {
                    //查询 purGoods
                    Map<String, Object> map = new HashMap<>();
                    map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
                    map.put("equalStatus", 0);
                    map.put("purDepId", gbDepartmentOrders.getGbDoToDepartmentId());
                    map.put("standard", gbDepartmentOrders.getGbDoStandard());
                    List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);
                    if (goodsEntities.size() == 0) {
                        //是个新采购商品
                        GbDistributerPurchaseGoodsEntity newPurGoods = new GbDistributerPurchaseGoodsEntity();
                        newPurGoods.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
                        newPurGoods.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
                        newPurGoods.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
                        newPurGoods.setGbDpgApplyDate(formatWhatDay(0));
                        newPurGoods.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
                        newPurGoods.setGbDpgOrdersAmount(1);
                        newPurGoods.setGbDpgOrdersFinishAmount(0);
                        newPurGoods.setGbDpgOrdersWeightAmount(0);
                        newPurGoods.setGbDpgOrdersBillAmount(0);
                        newPurGoods.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
                        newPurGoods.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
                        newPurGoods.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
                        newPurGoods.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
                        newPurGoods.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
                        newPurGoods.setGbDpgPurchaseType(1);
                        newPurGoods.setGbDpgPurchaseWeek(getWeek(0));
                        newPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                        gbDistributerPurchaseGoodsService.save(newPurGoods);
                        Integer gbDistributerPurchaseGoodsId = newPurGoods.getGbDistributerPurchaseGoodsId();
                        gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                    } else {
                        // 3， 给老采购商品添加新订单
                        GbDistributerPurchaseGoodsEntity gbDisPurGoodsEntity = goodsEntities.get(0);
                        Integer gbDistributerPurchaseGoodsId = gbDisPurGoodsEntity.getGbDistributerPurchaseGoodsId();
                        gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                        //采购商品订单数量更新
                        Integer gbDpgOrdersAmount = gbDisPurGoodsEntity.getGbDpgOrdersAmount();
                        gbDisPurGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
                        BigDecimal purQuantity = new BigDecimal(gbDisPurGoodsEntity.getGbDpgQuantity());
                        BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
                        BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                        gbDisPurGoodsEntity.setGbDpgQuantity(add.toString());
                        gbDistributerPurchaseGoodsService.update(gbDisPurGoodsEntity);
                    }

                }


            }


        }

        if (!oldStandard.equals(gbDoStandard1)) {

            // 1，修改原来的purGoods
            Integer gbDoPurchaseGoodsId = gbDepartmentOrders.getGbDoPurchaseGoodsId();
            if (gbDoPurchaseGoodsId != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
                Integer oldOrdersAmount = purchaseGoodsEntity.getGbDpgOrdersAmount();
                if (oldOrdersAmount > 1) {
                    purchaseGoodsEntity.setGbDpgOrdersAmount(oldOrdersAmount - 1);
                    gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                } else {
                    gbDistributerPurchaseGoodsService.delete(gbDoPurchaseGoodsId);
                }
            }


            // 2，查询是否有采购的同一个商品
            //有采购商品
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", gbDepartmentOrders.getGbDoDisGoodsId());
            map.put("equalStatus", 0);
            map.put("purDepId", gbDepartmentOrders.getGbDoToDepartmentId());
            map.put("standard", gbDepartmentOrders.getGbDoStandard());
            List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);
            if (goodsEntities.size() == 0) {
                //是个新采购商品
                GbDistributerPurchaseGoodsEntity newPurGoods = new GbDistributerPurchaseGoodsEntity();
                newPurGoods.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
                newPurGoods.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
                newPurGoods.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
                newPurGoods.setGbDpgApplyDate(formatWhatDay(0));
                newPurGoods.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
                newPurGoods.setGbDpgOrdersAmount(1);
                newPurGoods.setGbDpgOrdersFinishAmount(0);
                newPurGoods.setGbDpgOrdersWeightAmount(0);
                newPurGoods.setGbDpgOrdersBillAmount(0);
                newPurGoods.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
                newPurGoods.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
                newPurGoods.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
                newPurGoods.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
                newPurGoods.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
                newPurGoods.setGbDpgPurchaseType(gbDepartmentOrders.getGbDoGoodsType());
                newPurGoods.setGbDpgPurchaseWeek(getWeek(0));
                newPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                gbDistributerPurchaseGoodsService.save(newPurGoods);
                Integer gbDistributerPurchaseGoodsId = newPurGoods.getGbDistributerPurchaseGoodsId();
                gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
            } else {
                // 3， 给老采购商品添加新订单
                GbDistributerPurchaseGoodsEntity gbDisPurGoodsEntity = goodsEntities.get(0);
                Integer gbDistributerPurchaseGoodsId = gbDisPurGoodsEntity.getGbDistributerPurchaseGoodsId();
                gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                //采购商品订单数量更新
                Integer gbDpgOrdersAmount = gbDisPurGoodsEntity.getGbDpgOrdersAmount();
                gbDisPurGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
                BigDecimal purQuantity = new BigDecimal(gbDisPurGoodsEntity.getGbDpgQuantity());
                BigDecimal orderQuantity = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
                BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                gbDisPurGoodsEntity.setGbDpgQuantity(add.toString());
                gbDistributerPurchaseGoodsService.update(gbDisPurGoodsEntity);
            }
        }

        if (gbDepartmentOrders.getGbDoNxDepartmentOrderId() != null && gbDepartmentOrders.getGbDoNxDepartmentOrderId() != -1) {
            Integer gbDoNxDepartmentOrderId = gbDepartmentOrders.getGbDoNxDepartmentOrderId();
            NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
            nxDepartmentOrdersEntity.setNxDoQuantity(gbDepartmentOrders.getGbDoQuantity());

            Integer gbDoNxDistributerGoodsId = gbDepartmentOrders.getGbDoNxDistributerGoodsId();
            NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(gbDoNxDistributerGoodsId);

            if (nxDepartmentOrdersEntity.getNxDoStandard().equals(nxDistributerGoodsEntity.getNxDgGoodsStandardname())) {
                nxDepartmentOrdersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
                gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                BigDecimal orderWeight = new BigDecimal(gbDepartmentOrders.getGbDoWeight());
                BigDecimal willPrice = new BigDecimal(0);
                BigDecimal buyingPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPrice());
                String buyingPriceLevel = "0";
                String update = nxDistributerGoodsEntity.getNxDgBuyingPriceUpdate();
                if (nxDistributerGoodsEntity.getNxDgWillPriceOneWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOneWeight()).compareTo(BigDecimal.ZERO) == 1) {
                    BigDecimal nxOneWeight = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOneWeight());
                    if (orderWeight.compareTo(nxOneWeight) < 1) {
                        willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                        buyingPriceLevel = "1";

                    } else {
                        if (nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight()).compareTo(BigDecimal.ZERO) == 1) {
                            BigDecimal nxTwoWeight = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight());
                            if (orderWeight.compareTo(nxTwoWeight) < 1) {
                                willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                                buyingPriceLevel = "2";
                            } else {
                                if (nxDistributerGoodsEntity.getNxDgWillPriceThreeWeight() != null && new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceThreeWeight()).compareTo(BigDecimal.ZERO) == 1) {
                                    willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceThree());
                                    buyingPriceLevel = "3";
                                } else {
                                    willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                                    buyingPriceLevel = "2";
                                }
                            }
                        } else {
                            willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                            buyingPriceLevel = "1";
                        }

                    }
                }


                nxDepartmentOrdersEntity.setNxDoCostPriceLevel(buyingPriceLevel);
                nxDepartmentOrdersEntity.setNxDoCostPrice(buyingPrice.toString());
                nxDepartmentOrdersEntity.setNxDoCostPriceUpdate(update);
                nxDepartmentOrdersEntity.setNxDoPrice(willPrice.toString());
                gbDepartmentOrders.setGbDoPrice(willPrice.toString());


                //profit
                BigDecimal profitB = willPrice.subtract(buyingPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profitB.divide(willPrice, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                nxDepartmentOrdersEntity.setNxDoProfitScale(scaleB.toString());

                if (nxDepartmentOrdersEntity.getNxDoStandard().equals(nxDistributerGoodsEntity.getNxDgGoodsStandardname())) {
                    nxDepartmentOrdersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
                    gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                    BigDecimal costSubtotalB = buyingPrice.multiply(new BigDecimal(gbDepartmentOrders.getGbDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal profitSubtotal = profitB.multiply(new BigDecimal(nxDepartmentOrdersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal orderSubtotal = willPrice.multiply(new BigDecimal(nxDepartmentOrdersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    nxDepartmentOrdersEntity.setNxDoCostSubtotal(costSubtotalB.toString());
                    nxDepartmentOrdersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());
                    nxDepartmentOrdersEntity.setNxDoSubtotal(orderSubtotal.toString());
                } else {
                    nxDepartmentOrdersEntity.setNxDoCostSubtotal("0");
                    nxDepartmentOrdersEntity.setNxDoProfitSubtotal("0");
                }


            }
            nxDepartmentOrdersEntity.setNxDoRemark(gbDepartmentOrders.getGbDoRemark());
            nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);
        }

        gbDepartmentOrdersService.update(gbDepartmentOrders);
        return R.ok().put("data", gbDepartmentOrders);
    }


    @ResponseBody
    @RequestMapping(value = "/updateOrderGbJjSx", method = RequestMethod.POST)
    public R updateOrderGbJjSx(Integer id, String standard, String remark, String weight) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(id);

        BigDecimal nxDoPrice = new BigDecimal(ordersEntity.getNxDoPrice());

        Integer gbDepartmentOrderId = ordersEntity.getNxDoGbDepartmentOrderId();

        System.out.println("updateeelelelleesssxxxxxxxxx" + id);
        //检查修改规格

        GbDepartmentOrdersEntity oldOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrderId);
        String oldStandard = oldOrdersEntity.getGbDoStandard();
        System.out.println("updateeelelellee" + oldStandard);

        Integer gbDoDisGoodsId = oldOrdersEntity.getGbDoDisGoodsId();
        GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        String standardname = gbDisGoodsEntity.getGbDgGoodsStandardname();
        Integer gbDoPurchaseGoodsId = oldOrdersEntity.getGbDoPurchaseGoodsId();

        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);

        if (!oldStandard.equals(standard)) {

            // 1，修改原来的purGoods
            Integer oldOrdersAmount = purchaseGoodsEntity.getGbDpgOrdersAmount();
            if (oldOrdersAmount == 1) {
                purchaseGoodsEntity.setGbDpgQuantity(weight);
                purchaseGoodsEntity.setGbDpgBuyQuantity(weight);
                purchaseGoodsEntity.setGbDpgStandard(standard);
                if (standard.equals(standardname)) {
                    BigDecimal decimal = new BigDecimal(weight).multiply(nxDoPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());
                }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

            } else {

                BigDecimal subtract = new BigDecimal(purchaseGoodsEntity.getGbDpgQuantity()).subtract(new BigDecimal(oldOrdersEntity.getGbDoQuantity()));
                purchaseGoodsEntity.setGbDpgQuantity(subtract.toString());
                purchaseGoodsEntity.setGbDpgOrdersAmount(purchaseGoodsEntity.getGbDpgOrdersAmount() - 1);
                if (standard.equals(standardname)) {
                    BigDecimal decimal = subtract.multiply(nxDoPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());
                }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

                // 2，查询是否有采购的同一个商品
                //有采购商品
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsId", oldOrdersEntity.getGbDoDisGoodsId());
                map.put("equalStatus", 0);
                map.put("standard", standard);
                List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);
                if (goodsEntities.size() == 0) {
                    //是个新采购商品
                    GbDistributerPurchaseGoodsEntity newPurGoods = new GbDistributerPurchaseGoodsEntity();
                    newPurGoods.setGbDpgDisGoodsFatherId(oldOrdersEntity.getGbDoDisGoodsFatherId());
                    newPurGoods.setGbDpgDisGoodsId(oldOrdersEntity.getGbDoDisGoodsId());
                    newPurGoods.setGbDpgDistributerId(oldOrdersEntity.getGbDoDistributerId());
                    newPurGoods.setGbDpgApplyDate(formatWhatDay(0));
                    newPurGoods.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
                    newPurGoods.setGbDpgOrdersAmount(1);
                    newPurGoods.setGbDpgOrdersFinishAmount(0);
                    newPurGoods.setGbDpgOrdersWeightAmount(0);
                    newPurGoods.setGbDpgOrdersBillAmount(0);
                    newPurGoods.setGbDpgStandard(standard);
                    newPurGoods.setGbDpgQuantity(weight);
                    newPurGoods.setGbDpgPurchaseWeek(getWeek(0));
                    newPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                    if (standard.equals(standardname)) {
                        newPurGoods.setGbDpgBuyQuantity(weight);
                        BigDecimal decimal = new BigDecimal(weight).multiply(nxDoPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        newPurGoods.setGbDpgBuySubtotal(decimal.toString());
                    }
                    gbDistributerPurchaseGoodsService.save(newPurGoods);
                    Integer gbDistributerPurchaseGoodsId = newPurGoods.getGbDistributerPurchaseGoodsId();
                    oldOrdersEntity.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                } else {
                    // 3， 给老采购商品添加新订单
                    GbDistributerPurchaseGoodsEntity gbDisPurGoodsEntity = goodsEntities.get(0);
                    Integer gbDistributerPurchaseGoodsId = gbDisPurGoodsEntity.getGbDistributerPurchaseGoodsId();
                    oldOrdersEntity.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                    //采购商品订单数量更新
                    Integer gbDpgOrdersAmount = gbDisPurGoodsEntity.getGbDpgOrdersAmount();
                    gbDisPurGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
                    BigDecimal purQuantity = new BigDecimal(gbDisPurGoodsEntity.getGbDpgQuantity());
                    BigDecimal orderQuantity = new BigDecimal(weight);
                    BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                    gbDisPurGoodsEntity.setGbDpgQuantity(add.toString());

                    if (standard.equals(standardname)) {
                        BigDecimal decimal = add.multiply(nxDoPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        purchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());
                    }
                    gbDistributerPurchaseGoodsService.update(gbDisPurGoodsEntity);
                }
            }
            // 修改 price 和 subtotal
            if (standard.equals(gbDisGoodsEntity.getGbDgGoodsStandardname())) {
                oldOrdersEntity.setGbDoWeight(weight);

            } else {
                oldOrdersEntity.setGbDoWeight("0");
                oldOrdersEntity.setGbDoSubtotal("0");
            }
        } else {
            Integer oldOrdersAmount = purchaseGoodsEntity.getGbDpgOrdersAmount();
            System.out.println("updatepururrururoldOrdersAmount=" + oldOrdersAmount);
            if (oldOrdersAmount == 1) {
                purchaseGoodsEntity.setGbDpgQuantity(weight);
                purchaseGoodsEntity.setGbDpgBuyQuantity(weight);
                purchaseGoodsEntity.setGbDpgStandard(standard);
                System.out.println("pugennenenene" + weight);
                System.out.println("pugennenenene11" + purchaseGoodsEntity.getGbDpgQuantity());
                System.out.println("pugennenenene222" + purchaseGoodsEntity.getGbDpgBuyQuantity());
                if (standard.equals(standardname)) {
                    BigDecimal decimal = new BigDecimal(weight).multiply(nxDoPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());
                }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            } else {

                BigDecimal subtract = new BigDecimal(purchaseGoodsEntity.getGbDpgQuantity()).subtract(new BigDecimal(oldOrdersEntity.getGbDoQuantity()));
                purchaseGoodsEntity.setGbDpgQuantity(subtract.toString());
                purchaseGoodsEntity.setGbDpgOrdersAmount(purchaseGoodsEntity.getGbDpgOrdersAmount() - 1);
                if (standard.equals(standardname)) {
                    BigDecimal decimal = subtract.multiply(nxDoPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());
                }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        if (oldOrdersEntity.getGbDoNxDepartmentOrderId() != null && oldOrdersEntity.getGbDoNxDepartmentOrderId() != -1) {
            updateNxOrder(oldOrdersEntity.getGbDoNxDepartmentOrderId(), weight, standard, remark);
        }

        oldOrdersEntity.setGbDoRemark(remark);
        oldOrdersEntity.setGbDoQuantity(weight);
        oldOrdersEntity.setGbDoStandard(standard);
        if (standard.equals(standardname)) {
            BigDecimal decimal = new BigDecimal(weight).multiply(nxDoPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            oldOrdersEntity.setGbDoSubtotal(decimal.toString());
        } else {
            oldOrdersEntity.setGbDoSubtotal("0");
        }
        gbDepartmentOrdersService.update(oldOrdersEntity);


        NxDepartmentOrdersEntity newNxOrder = nxDepartmentOrdersService.queryObject(id);
        return R.ok().put("data", newNxOrder);

    }


    @ResponseBody
    @RequestMapping(value = "/updateOrderGbJj", method = RequestMethod.POST)
    public R updateOrderGbJj(Integer id, String standard, String remark, String weight) {

        System.out.println("updateeelelellee" + id);
        //检查修改规格

        GbDepartmentOrdersEntity oldOrdersEntity = gbDepartmentOrdersService.queryObject(id);
        String oldStandard = oldOrdersEntity.getGbDoStandard();
        System.out.println("updateeelelellee" + oldStandard);

        Integer gbDoDisGoodsId = oldOrdersEntity.getGbDoDisGoodsId();
        GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        String standardname = gbDisGoodsEntity.getGbDgGoodsStandardname();
        Integer gbDoPurchaseGoodsId = oldOrdersEntity.getGbDoPurchaseGoodsId();

        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);

        if (!oldStandard.equals(standard)) {

            // 1，修改原来的purGoods
            Integer oldOrdersAmount = purchaseGoodsEntity.getGbDpgOrdersAmount();

            if (oldOrdersAmount == 1) { //如果新规格的采购商品只有一个订单

                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsId", oldOrdersEntity.getGbDoDisGoodsId());
                map.put("equalStatus", 0);
                map.put("standard", standard);
                List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);
                if (goodsEntities.size() == 1) {
                    GbDistributerPurchaseGoodsEntity sameStandardPurGoods = goodsEntities.get(0);
                    BigDecimal decimal = new BigDecimal(sameStandardPurGoods.getGbDpgBuyQuantity()).add(new BigDecimal(weight));
                    sameStandardPurGoods.setGbDpgQuantity(decimal.toString());
                    sameStandardPurGoods.setGbDpgBuyQuantity(decimal.toString());
                    if (standard.equals(standardname) && sameStandardPurGoods.getGbDpgBuyPrice() != null) {
                        BigDecimal decimal1 = new BigDecimal(sameStandardPurGoods.getGbDpgBuyPrice()).multiply(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                        sameStandardPurGoods.setGbDpgBuySubtotal(decimal1.toString());
                    }
                    gbDistributerPurchaseGoodsService.update(sameStandardPurGoods);
                    //删除原来的采购商品
                    gbDistributerPurchaseGoodsService.delete(purchaseGoodsEntity.getGbDistributerPurchaseGoodsId());

                } else {

                    purchaseGoodsEntity.setGbDpgBuyQuantity(weight);
                    purchaseGoodsEntity.setGbDpgQuantity(weight);
                    purchaseGoodsEntity.setGbDpgStandard(standard);
                    if (standard.equals(standardname) && purchaseGoodsEntity.getGbDpgBuyPrice() != null) {
                        BigDecimal decimal = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice()).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                        purchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());
                    }
                    gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }

            } else { //如果采购商品有多个订单

                BigDecimal subtract = new BigDecimal(purchaseGoodsEntity.getGbDpgQuantity()).subtract(new BigDecimal(oldOrdersEntity.getGbDoQuantity()));
                purchaseGoodsEntity.setGbDpgQuantity(subtract.toString());
                purchaseGoodsEntity.setGbDpgOrdersAmount(purchaseGoodsEntity.getGbDpgOrdersAmount() - 1);
                if (standard.equals(standardname) && purchaseGoodsEntity.getGbDpgBuyPrice() != null) {
                    BigDecimal decimal = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice()).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());
                }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

                // 2，查询是否有采购的同一个商品
                //有采购商品
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsId", oldOrdersEntity.getGbDoDisGoodsId());
                map.put("equalStatus", 0);
                map.put("standard", standard);
                List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);
                if (goodsEntities.size() == 0) {
                    //是个新采购商品
                    GbDistributerPurchaseGoodsEntity newPurGoods = new GbDistributerPurchaseGoodsEntity();
                    newPurGoods.setGbDpgDisGoodsFatherId(oldOrdersEntity.getGbDoDisGoodsFatherId());
                    newPurGoods.setGbDpgDisGoodsId(oldOrdersEntity.getGbDoDisGoodsId());
                    newPurGoods.setGbDpgDistributerId(oldOrdersEntity.getGbDoDistributerId());
                    newPurGoods.setGbDpgApplyDate(formatWhatDay(0));
                    newPurGoods.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
                    newPurGoods.setGbDpgOrdersAmount(1);
                    newPurGoods.setGbDpgOrdersFinishAmount(0);
                    newPurGoods.setGbDpgOrdersBillAmount(0);
                    newPurGoods.setGbDpgStandard(standard);
                    newPurGoods.setGbDpgQuantity(weight);
                    newPurGoods.setGbDpgPurchaseWeek(getWeek(0));
                    newPurGoods.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                    if (standard.equals(standardname) && purchaseGoodsEntity.getGbDpgBuyPrice() != null) {
                        BigDecimal decimal = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice()).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                        newPurGoods.setGbDpgBuySubtotal(decimal.toString());
                    }
                    gbDistributerPurchaseGoodsService.save(newPurGoods);
                    Integer gbDistributerPurchaseGoodsId = newPurGoods.getGbDistributerPurchaseGoodsId();
                    oldOrdersEntity.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);


                } else {
                    // 3， 给老采购商品添加新订单
                    GbDistributerPurchaseGoodsEntity gbDisPurGoodsEntity = goodsEntities.get(0);
                    Integer gbDistributerPurchaseGoodsId = gbDisPurGoodsEntity.getGbDistributerPurchaseGoodsId();
                    oldOrdersEntity.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
                    //采购商品订单数量更新
                    Integer gbDpgOrdersAmount = gbDisPurGoodsEntity.getGbDpgOrdersAmount();
                    gbDisPurGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount + 1);
                    BigDecimal purQuantity = new BigDecimal(gbDisPurGoodsEntity.getGbDpgQuantity());
                    BigDecimal orderQuantity = new BigDecimal(weight);
                    BigDecimal add = purQuantity.add(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                    gbDisPurGoodsEntity.setGbDpgQuantity(add.toString());
                    if (standard.equals(standardname) && purchaseGoodsEntity.getGbDpgBuyPrice() != null) {
                        BigDecimal decimal = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        gbDisPurGoodsEntity.setGbDpgBuySubtotal(decimal.toString());
                    }
                    gbDistributerPurchaseGoodsService.update(gbDisPurGoodsEntity);
                }

                //元采购商品减去
                String gbDpgBuyQuantity = purchaseGoodsEntity.getGbDpgBuyQuantity();
                BigDecimal decimal = new BigDecimal(gbDpgBuyQuantity).subtract(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                purchaseGoodsEntity.setGbDpgBuyQuantity(decimal.toString());
                purchaseGoodsEntity.setGbDpgQuantity(decimal.toString());
                purchaseGoodsEntity.setGbDpgOrdersAmount(oldOrdersAmount - 1);
                if (purchaseGoodsEntity.getGbDpgStandard().equals(standardname) && purchaseGoodsEntity.getGbDpgBuyPrice() != null) {
                    BigDecimal decimal1 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice()).multiply(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal1.toString());
                    gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }

            }


            // 修改 price 和 subtotal
            if (standard.equals(gbDisGoodsEntity.getGbDgGoodsStandardname())) {
                oldOrdersEntity.setGbDoWeight(weight);
                if (standard.equals(standardname) && oldOrdersEntity.getGbDoPrice() != null) {
                    BigDecimal decimal = new BigDecimal(oldOrdersEntity.getGbDoPrice()).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    oldOrdersEntity.setGbDoSubtotal(decimal.toString());
                }
            } else {
                oldOrdersEntity.setGbDoWeight("0");
                oldOrdersEntity.setGbDoSubtotal("0");
            }
        } else {
            System.out.println("updatepururrurur");
            Integer oldOrdersAmount = purchaseGoodsEntity.getGbDpgOrdersAmount();
            if (oldOrdersAmount == 1) {
                purchaseGoodsEntity.setGbDpgQuantity(weight);
                purchaseGoodsEntity.setGbDpgStandard(standard);
                if (standard.equals(standardname) && oldOrdersEntity.getGbDoPrice() != null && !oldOrdersEntity.getGbDoPrice().trim().isEmpty()) {
                    BigDecimal decimal = new BigDecimal(oldOrdersEntity.getGbDoPrice()).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());
                }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            } else {

                BigDecimal subtract = new BigDecimal(purchaseGoodsEntity.getGbDpgQuantity()).subtract(new BigDecimal(oldOrdersEntity.getGbDoQuantity()));
                purchaseGoodsEntity.setGbDpgQuantity(subtract.toString());
                purchaseGoodsEntity.setGbDpgOrdersAmount(purchaseGoodsEntity.getGbDpgOrdersAmount() - 1);
                if (standard.equals(standardname) && purchaseGoodsEntity.getGbDpgBuyPrice() != null) {
                    BigDecimal decimal = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice()).multiply(subtract).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());
                }
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        if (oldOrdersEntity.getGbDoNxDepartmentOrderId() != null && oldOrdersEntity.getGbDoNxDepartmentOrderId() != -1) {
            updateNxOrder(oldOrdersEntity.getGbDoNxDepartmentOrderId(), weight, standard, remark);
        }

        oldOrdersEntity.setGbDoRemark(remark);
        oldOrdersEntity.setGbDoQuantity(weight);
        oldOrdersEntity.setGbDoStandard(standard);
        System.out.println("fdstandnndndd" + standard + "snen" + standardname);
        if (standard.equals(standardname) && oldOrdersEntity.getGbDoPrice() != null && !oldOrdersEntity.getGbDoPrice().trim().isEmpty()) {
            BigDecimal decimal = new BigDecimal(oldOrdersEntity.getGbDoPrice()).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            oldOrdersEntity.setGbDoSubtotal(decimal.toString());
        }
        gbDepartmentOrdersService.update(oldOrdersEntity);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", gbDoDisGoodsId);
        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryDisGoodsWithDepDisGoods(map);
        oldOrdersEntity.setGbDistributerGoodsEntity(goodsEntity);
        return R.ok().put("data", oldOrdersEntity);
    }


    public NxDepartmentOrdersEntity updateNxOrder(Integer id, String weight, String standard, String remark) {
        System.out.println("updatedoorprupdateNxOrderupdateNxOrder" + weight);
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(id);
        String oldNxDoQuantity = ordersEntity.getNxDoQuantity();
        //自动添加重量和单价小计
        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetailWithLinshi(doDisGoodsId);
        String nxDgGoodsStandardname = distributerGoodsEntity.getNxDgGoodsStandardname();
        System.out.println("priririeieiieieieiiei" + ordersEntity.getNxDoPrice());
        if (standard.equals(nxDgGoodsStandardname)) {
            ordersEntity.setNxDoWeight(weight);
            System.out.println("priririeieiieieieiiei" + ordersEntity.getNxDoPrice());
            if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().equals("0.0")) {
                System.out.println("priririeieiieieieiiei" + ordersEntity.getNxDoPrice());
                BigDecimal decimal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoSubtotal(decimal.toString());
                //profit
                if (ordersEntity.getNxDoCostPrice().equals("0.1")) {
                    BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());
                    BigDecimal decimal2 = new BigDecimal(ordersEntity.getNxDoPrice());
                    BigDecimal subtotalB = weightB.multiply(decimal2);

                    BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                    BigDecimal decimal1 = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal profitB = subtotalB.subtract(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                    ordersEntity.setNxDoProfitScale(scaleB.toString());
                    ordersEntity.setNxDoProfitSubtotal(profitB.toString());

                    if (ordersEntity.getNxDoExpectPrice() != null) {
                        BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
                        BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                        BigDecimal subtract = doPrice.subtract(expectPrice);
                        ordersEntity.setNxDoPriceDifferent(subtract.toString());
                    }
                }
            }
        } else {
            ordersEntity.setNxDoWeight(null);
            ordersEntity.setNxDoSubtotal(null);
            ordersEntity.setNxDoCostSubtotal("0");
            ordersEntity.setNxDoProfitScale("0");
            ordersEntity.setNxDoProfitSubtotal("0");
            ordersEntity.setNxDoPriceDifferent("0");

        }

        //purGoods
        if (ordersEntity.getNxDoPurchaseGoodsId() != -1) {
            Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
            NxDistributerPurchaseGoodsEntity oldPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
            //standard changed
            System.out.println("standnnddndddddd1111" + standard + "olpurstandndd" + oldPurchaseGoodsEntity.getNxDpgStandard());
            if (!standard.equals(oldPurchaseGoodsEntity.getNxDpgStandard())) {
                if (oldPurchaseGoodsEntity.getNxDpgOrdersAmount() == 1) {

                    Map<String, Object> map = new HashMap<>();
                    map.put("disGoodsId", doDisGoodsId);
                    map.put("status", 1);
                    map.put("standard", standard);
                    System.out.println("purgogogo" + map);
                    NxDistributerPurchaseGoodsEntity havePurGoods = nxDistributerPurchaseGoodsService.queryIfHavePurGoods(map);
                    if (havePurGoods != null) {
                        BigDecimal add1 = new BigDecimal(havePurGoods.getNxDpgQuantity()).add(new BigDecimal(weight));
                        havePurGoods.setNxDpgBuyQuantity(add1.toString());
                        havePurGoods.setNxDpgQuantity(add1.toString());
                        if (standard.equals(nxDgGoodsStandardname)) {
                            BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgBuyPrice());
                            BigDecimal decimal1 = decimal.multiply(add1).setScale(1, BigDecimal.ROUND_HALF_UP);
                            havePurGoods.setNxDpgBuySubtotal(decimal1.toString());
                        }
                        nxDistributerPurchaseGoodsService.update(havePurGoods);

                        nxDistributerPurchaseGoodsService.delete(nxDoPurchaseGoodsId);

                    } else {
                        oldPurchaseGoodsEntity.setNxDpgStandard(standard);
                        oldPurchaseGoodsEntity.setNxDpgQuantity(weight);
                        oldPurchaseGoodsEntity.setNxDpgBuyQuantity(weight);
                        if (standard.equals(nxDgGoodsStandardname)) {
                            BigDecimal decimal = new BigDecimal(weight).multiply(new BigDecimal(oldPurchaseGoodsEntity.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                            oldPurchaseGoodsEntity.setNxDpgBuySubtotal(decimal.toString());
                            System.out.println("substootototot" + decimal);
                        }
                        nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);
                    }

                } else {

                    BigDecimal purQuantity = new BigDecimal(oldPurchaseGoodsEntity.getNxDpgQuantity());
                    BigDecimal newWeight = purQuantity.subtract(new BigDecimal(oldNxDoQuantity));
                    System.out.println("updidddididnewWeight" + newWeight);
                    oldPurchaseGoodsEntity.setNxDpgQuantity(newWeight.toString());
                    oldPurchaseGoodsEntity.setNxDpgBuyQuantity(newWeight.toString());
                    oldPurchaseGoodsEntity.setNxDpgOrdersAmount(oldPurchaseGoodsEntity.getNxDpgOrdersAmount() - 1);

                    if (oldPurchaseGoodsEntity.getNxDpgStandard().equals(nxDgGoodsStandardname) && oldPurchaseGoodsEntity.getNxDpgBuyPrice() != null) {
                        BigDecimal decimal = newWeight.multiply(new BigDecimal(oldPurchaseGoodsEntity.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                        oldPurchaseGoodsEntity.setNxDpgBuySubtotal(decimal.toString());
                        System.out.println("nananandndnndododooddood");
                    }
                    nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);

                    Integer nxDistributerPurchaseGoodsId = -1;
                    Map<String, Object> map = new HashMap<>();
                    map.put("disGoodsId", doDisGoodsId);
                    map.put("status", 1);
                    map.put("standard", standard);
                    System.out.println("purgogogo" + map);
                    NxDistributerPurchaseGoodsEntity havePurGoods = nxDistributerPurchaseGoodsService.queryIfHavePurGoods(map);
                    if (havePurGoods != null) {
                        BigDecimal add1 = new BigDecimal(havePurGoods.getNxDpgQuantity()).add(new BigDecimal(weight));
                        havePurGoods.setNxDpgBuyQuantity(add1.toString());
                        havePurGoods.setNxDpgQuantity(add1.toString());
                        if (standard.equals(nxDgGoodsStandardname) && havePurGoods.getNxDpgBuyPrice() != null) {
                            BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgBuyPrice());
                            BigDecimal decimal1 = decimal.multiply(add1).setScale(1, BigDecimal.ROUND_HALF_UP);
                            havePurGoods.setNxDpgBuySubtotal(decimal1.toString());
                            nxDistributerPurchaseGoodsService.update(havePurGoods);
                        }
                        nxDistributerPurchaseGoodsId = havePurGoods.getNxDistributerPurchaseGoodsId();

                    } else {
                        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
                        purchaseGoodsEntity.setNxDpgDistributerId(distributerGoodsEntity.getNxDgDistributerId());
                        purchaseGoodsEntity.setNxDpgDisGoodsFatherId(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
                        purchaseGoodsEntity.setNxDpgDisGoodsGrandId(distributerGoodsEntity.getNxDgDfgGoodsGrandId());
                        purchaseGoodsEntity.setNxDpgDistributerId(distributerGoodsEntity.getNxDgDistributerId());
                        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
                        purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
                        purchaseGoodsEntity.setNxDpgOrdersAmount(1);
                        purchaseGoodsEntity.setNxDpgFinishAmount(0);
                        purchaseGoodsEntity.setNxDpgPurchaseType(1);
                        purchaseGoodsEntity.setNxDpgExpectPrice(distributerGoodsEntity.getNxDgBuyingPrice());
                        purchaseGoodsEntity.setNxDpgBuyPrice(distributerGoodsEntity.getNxDgBuyingPrice());
                        purchaseGoodsEntity.setNxDpgDisGoodsId(doDisGoodsId);
                        purchaseGoodsEntity.setNxDpgInputType(distributerGoodsEntity.getNxDgPurchaseAuto());
                        purchaseGoodsEntity.setNxDpgStandard(standard);
                        if (standard.equals(distributerGoodsEntity.getNxDgGoodsStandardname()) && purchaseGoodsEntity.getNxDpgBuyPrice() != null) {
                            BigDecimal totaoWeight = new BigDecimal(weight);
                            purchaseGoodsEntity.setNxDpgQuantity(weight);
                            purchaseGoodsEntity.setNxDpgBuyQuantity(weight);
                            BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(purchaseGoodsEntity.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                            purchaseGoodsEntity.setNxDpgBuySubtotal(decimal2.toString());
                        }
                        purchaseGoodsEntity.setNxDpgBatchId(oldPurchaseGoodsEntity.getNxDpgBatchId());
                        nxDistributerPurchaseGoodsService.save(purchaseGoodsEntity);
                        nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
                    }
                    ordersEntity.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
                }

            } else {
                System.out.println("standnnddndddddd" + standard + "olpurstandndd" + oldPurchaseGoodsEntity.getNxDpgStandard());
                BigDecimal purQuantity = new BigDecimal(oldPurchaseGoodsEntity.getNxDpgQuantity());
                BigDecimal add = purQuantity.subtract(new BigDecimal(oldNxDoQuantity)).add(new BigDecimal(weight));
                oldPurchaseGoodsEntity.setNxDpgQuantity(add.toString());
                oldPurchaseGoodsEntity.setNxDpgBuyQuantity(add.toString());
                if (standard.equals(nxDgGoodsStandardname) && oldPurchaseGoodsEntity.getNxDpgBuyPrice() != null) {
                    BigDecimal decimal = new BigDecimal(weight).multiply(new BigDecimal(oldPurchaseGoodsEntity.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    oldPurchaseGoodsEntity.setNxDpgBuySubtotal(decimal.toString());
                    System.out.println("substootototot" + decimal);
                }
                nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);

            }
        }

        ordersEntity.setNxDoQuantity(weight);
        ordersEntity.setNxDoStandard(standard);
        ordersEntity.setNxDoRemark(remark);
        if (standard.equals(nxDgGoodsStandardname) && ordersEntity.getNxDoPrice() != null && ordersEntity.getNxDoCostPrice() != null) {
            BigDecimal decimalCost = new BigDecimal(weight).multiply(new BigDecimal(ordersEntity.getNxDoCostPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal decimal = new BigDecimal(weight).multiply(new BigDecimal(ordersEntity.getNxDoPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoCostSubtotal(decimalCost.toString());
            ordersEntity.setNxDoSubtotal(decimal.toString());
            System.out.println("substoototototoorororooroor" + decimal);
        }
        nxDepartmentOrdersService.update(ordersEntity);
        ordersEntity.setNxDistributerGoodsEntity(distributerGoodsEntity);
        return ordersEntity;
    }

    @ResponseBody
    @RequestMapping("/justUpdateOrderContent")
    public R justUpdateOrderContent(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {

        gbDepartmentOrdersService.update(gbDepartmentOrders);
        return R.ok().put("data", gbDepartmentOrders);
    }

    @ResponseBody
    @RequestMapping("/outDepDelOrder/{id}")
    public R outDepDelOrder(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", id);
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepartmentGoodsStockService.queryGoodsStockByParams(map);
        if (stockEntities.size() > 0) {
            for (GbDepartmentGoodsStockEntity stock : stockEntities) {
                gbDepartmentGoodsStockService.delete(stock.getGbDepartmentGoodsStockId());
            }
        }

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(id);
        Integer gbDoWeightTotalId = ordersEntity.getGbDoWeightTotalId();
        if (gbDoWeightTotalId != null) {
            GbDistributerWeightTotalEntity gbDistributerWeightTotalEntity = gbDisWeightTotalService.queryObject(gbDoWeightTotalId);
            BigDecimal gbGwtOrderCount = new BigDecimal(gbDistributerWeightTotalEntity.getGbGwtOrderCount());
            BigDecimal gbGwtFinishCount = new BigDecimal(gbDistributerWeightTotalEntity.getGbGwtOrderFinishCount());
            BigDecimal restCount = gbGwtOrderCount.subtract(gbGwtFinishCount);
            gbDistributerWeightTotalEntity.setGbGwtOrderCount(gbGwtOrderCount.subtract(new BigDecimal(1)).toString());
            if (restCount.compareTo(new BigDecimal(1)) == 0) {
                gbDistributerWeightTotalEntity.setGbGwtStatus(1);
            }
            gbDisWeightTotalService.update(gbDistributerWeightTotalEntity);
        }


        gbDepartmentOrdersService.delete(id);
        return R.ok();
    }


    /**
     * ORDER
     * 删除申请
     *
     * @param gbDepartmentOrdersId 订货申请id
     * @return ok
     */
    @ResponseBody
    @RequestMapping("/deleteOrderGb/{gbDepartmentOrdersId}")
    public R deleteOrderGb(@PathVariable Integer gbDepartmentOrdersId) {
        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);


        Integer gbDoDisGoodsId = ordersEntity.getGbDoDisGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        if (ordersEntity.getGbDoStatus() != -2 && ordersEntity.getGbDoPurchaseGoodsId() != -1) {
            Integer gbDoPurchaseGoodsId = ordersEntity.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity gbDistributerPurchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
            if (gbDistributerPurchaseGoodsEntity != null) {
                Integer gbDpgOrdersAmount = gbDistributerPurchaseGoodsEntity.getGbDpgOrdersAmount();
                if (gbDpgOrdersAmount > 1) {
                    gbDistributerPurchaseGoodsEntity.setGbDpgOrdersAmount(gbDpgOrdersAmount - 1);
                    BigDecimal subtract = new BigDecimal(gbDistributerPurchaseGoodsEntity.getGbDpgQuantity()).subtract(new BigDecimal(ordersEntity.getGbDoQuantity()));
                    gbDistributerPurchaseGoodsEntity.setGbDpgQuantity(subtract.toString());
                    gbDistributerPurchaseGoodsService.update(gbDistributerPurchaseGoodsEntity);
                } else {
                    //订货批次是否是最后一个采购商品
                    Integer gbDpgBatchId = gbDistributerPurchaseGoodsEntity.getGbDpgBatchId();
                    Integer oldSupplierId = gbDistributerPurchaseGoodsEntity.getGbDpgPurchaseNxSupplierId();
                    Integer gbDpgDistributerId1 = gbDistributerPurchaseGoodsEntity.getGbDpgDistributerId();
                    Map<String, Object> mapBatch = new HashMap<>();
                    mapBatch.put("batchId", gbDpgBatchId);
                    List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(mapBatch);
                    if (goodsEntities.size() == 1) {
                        gbDPBService.delete(gbDistributerPurchaseGoodsEntity.getGbDpgBatchId());
                    }
                    gbDistributerPurchaseGoodsService.delete(gbDoPurchaseGoodsId);

                    System.out.println("s删除订单提醒供货商");
                    if (oldSupplierId != -1) {
                        NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(oldSupplierId);
                        Integer jrdhsUserId = supplierEntity.getNxJrdhsUserId();
                        NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(jrdhsUserId);
                        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDpgDistributerId1);
                        System.out.println("tuuihuouonoticeeiee");
                        if (supplierEntity.getNxJrdhsUserId() != null) {
                            Map<String, TemplateData> mapNotice = new HashMap<>();
                            mapNotice.put("date7", new TemplateData(formatWhatDayTime(0)));
                            mapNotice.put("thing12", new TemplateData("删除订货" + gbDistributerGoodsEntity.getGbDgGoodsName()));
                            if (goodsEntities.size() == 1) {
                                mapNotice.put("phrase9", new TemplateData("订单取消"));
                            } else {
                                mapNotice.put("phrase9", new TemplateData("订单变更"));
                            }

                            StringBuilder pathBuilder = new StringBuilder("subPackage/pages/gbMarket/gbReceiveBatch/gbReceiveBatch");
                            pathBuilder.append("?batchId=").append(gbDpgBatchId);
                            pathBuilder.append("&retName=").append(gbDistributerEntity.getGbDistributerName());
                            pathBuilder.append("&from=notification"); // 添加这个参数
                            String path = pathBuilder.toString();
                            WeNoticeService.changeOrderSuppliertixingMessageJj(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);
                        }

                    }
                }
            }
        }

        if (ordersEntity.getGbDoNxDepartmentOrderId() != null && ordersEntity.getGbDoNxDepartmentOrderId() != -1) {
            Integer gbDoNxDepartmentOrderId = ordersEntity.getGbDoNxDepartmentOrderId();
            NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
            if (ordersEntity1 != null) {
                delNxPurGoods(ordersEntity1);
                nxDepartmentOrdersService.delete(ordersEntity1.getNxDepartmentOrdersId());
            }
        }

        gbDepartmentOrdersService.delete(gbDepartmentOrdersId);
        return R.ok();

    }

    private void delNxPurGoods(NxDepartmentOrdersEntity ordersEntity) {
        if (ordersEntity.getNxDoPurchaseGoodsId() != null && ordersEntity.getNxDoPurchaseGoodsId() != -1) {
            NxDistributerPurchaseGoodsEntity nxDistributerPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(ordersEntity.getNxDoPurchaseGoodsId());
            if (nxDistributerPurchaseGoodsEntity != null) {
                Integer nxDpgOrdersAmount = nxDistributerPurchaseGoodsEntity.getNxDpgOrdersAmount();
                System.out.println("purgoodoamaomm" + nxDpgOrdersAmount);
                if (nxDpgOrdersAmount > 1) {
                    nxDistributerPurchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
                    BigDecimal newWeight = new BigDecimal(nxDistributerPurchaseGoodsEntity.getNxDpgQuantity()).subtract(new BigDecimal(ordersEntity.getNxDoQuantity()));
                    nxDistributerPurchaseGoodsEntity.setNxDpgBuyQuantity(newWeight.toString());
                    nxDistributerPurchaseGoodsEntity.setNxDpgQuantity(newWeight.toString());
                    System.out.println("purgoodoamaomm111" + nxDistributerPurchaseGoodsEntity.getNxDpgBuyQuantity());

                    if (ordersEntity.getNxDoStandard().equals(nxDistributerPurchaseGoodsEntity.getNxDpgStandard())) {
                        System.out.println("purgoodoamaomm" + nxDpgOrdersAmount);

                        BigDecimal price = new BigDecimal(nxDistributerPurchaseGoodsEntity.getNxDpgBuyPrice());
                        BigDecimal decimal1 = newWeight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                        nxDistributerPurchaseGoodsEntity.setNxDpgBuySubtotal(decimal1.toString());
                    }
                    nxDistributerPurchaseGoodsService.update(nxDistributerPurchaseGoodsEntity);
                } else {
                    if (nxDistributerPurchaseGoodsEntity.getNxDpgBatchId() != null) {
                        Integer nxDpgBatchId = nxDistributerPurchaseGoodsEntity.getNxDpgBatchId();
                        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByBatchId(nxDpgBatchId);
                        if (purchaseGoodsEntities.size() == 1) {
                            nxDPBService.delete(nxDpgBatchId);
                        } else {
                            String nxDpgBuySubtotal = nxDistributerPurchaseGoodsEntity.getNxDpgBuySubtotal();

                            NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDPBService.queryObject(nxDpgBatchId);
                            if (nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal() != null) {
                                BigDecimal decimal = new BigDecimal(nxDistributerPurchaseBatchEntity.getNxDpbSellSubtotal());
                                BigDecimal decimal2 = new BigDecimal(nxDpgBuySubtotal);
                                BigDecimal decimal1 = decimal.subtract(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                                nxDistributerPurchaseBatchEntity.setNxDpbSellSubtotal(decimal1.toString());
                                nxDPBService.update(nxDistributerPurchaseBatchEntity);
                            }
                        }
                    }

                    nxDistributerPurchaseGoodsService.delete(nxDistributerPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
                }
            }
        }
    }


////////**************************************************

    /**
     * PURCHASE,
     * 采购员复制自采购申请
     *
     * @param depOrders 自采购申请
     * @return ok
     */
    @RequestMapping(value = "/purchaserCopyOrderContent", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserCopyOrderContent(@RequestBody List<GbDepartmentOrdersEntity> depOrders) {
        for (GbDepartmentOrdersEntity ordersEntity : depOrders) {
            ordersEntity.setGbDoOperationTime(formatWhatTime(0));
            ordersEntity.setGbDoStatus(getGbOrderStatusProcurement());
            gbDepartmentOrdersService.update(ordersEntity);
        }
        return R.ok();
    }

    /**
     * PURCHASE,
     *
     * @param depId 订货群id
     * @param disId 批发商id
     * @return 订货申请列表
     */
    @RequestMapping(value = "/purchaserGetDisOrders", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserGetDisOrders(Integer depId, Integer disId, Integer arriveDate) {

        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depId);
        map.put("disId", disId);
        map.put("orderBy", "time");
        map.put("status", 3);
        map.put("arriveDate", formatWhatDay(arriveDate));

        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
        //按每天显示订单
        Map<String, Object> stringObjectMap = countSwiperHeight((ordersEntities));

        return R.ok().put("data", stringObjectMap);
    }

    private Map<String, Object> countSwiperHeight(List<GbDepartmentOrdersEntity> ordersEntities) {
        Map<String, Object> map = new HashMap<>();
        Integer countRemark = 0;
        for (GbDepartmentOrdersEntity order : ordersEntities) {
            String nxDoRemark = order.getGbDoRemark();
            if (nxDoRemark.length() > 0) {
                countRemark = countRemark + 1;
            }
        }
        map.put("remarkCount", countRemark);
        map.put("orderArr", ordersEntities);

        return map;
    }

    /**
     * PURCHASE,
     * //
     *
     * @param depId 订货群id
     * @return 自采购商品
     */
    @RequestMapping(value = "/purchaserGetIndependentOrders", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserGetIndependentOrders(Integer depId, Integer arriveDate) {

        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depId);
        map.put("goodsType", 1);
        map.put("status", 4);
        map.put("arriveDate", formatWhatDay(arriveDate));
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
        return R.ok().put("data", ordersEntities);
    }


    /**
     * PURCHASE,
     * 采购员完成自采购申请
     *
     * @param depOrders 自采购申请
     * @return ok
     */
    @RequestMapping(value = "/purchaserFinishOrderContent", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserFinishOrderContent(@RequestBody List<GbDepartmentOrdersEntity> depOrders) {
        for (GbDepartmentOrdersEntity ordersEntity : depOrders) {
            ordersEntity.setGbDoStatus(4);
            gbDepartmentOrdersService.update(ordersEntity);
        }
        return R.ok();
    }

    /**
     * 9-11
     * DISTRIBUTER
     * 获取需要填写数量和价格的订单
     *
     * @param depFatherId 群id
     * @return 订单
     */
    @RequestMapping(value = "/typePurchasrGetToFinishDepOrders")
    @ResponseBody
    public R typePurchasrGetToFinishDepOrders(Integer depFatherId, Integer orderType, Integer toDepId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("equalStatus", 2);
        map.put("orderType", orderType);
        map.put("toDepId", toDepId);

        System.out.println("maop" + map);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(depFatherId);

        if (entities.size() > 0) {
            for (GbDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getGbDepartmentId());
                mapDep.put("depName", dep.getGbDepartmentName());

                Map<String, Object> map1 = new HashMap<>();
                map1.put("orderType", orderType);
                map1.put("equalStatus", 2);
                map1.put("toDepId", toDepId);
                map1.put("depId", dep.getGbDepartmentId());
                List<GbDepartmentOrdersEntity> depOrders = gbDepartmentOrdersService.queryDisOrdersByParams(map1);
                mapDep.put("depOrders", depOrders);
                if (depOrders.size() > 0) {
                    mapList.add(mapDep);
                }
            }

            return R.ok().put("data", mapList);
        } else {
            Map<String, Object> map3 = new HashMap<>();
            map3.put("depId", depFatherId);
            map3.put("date", formatWhatDay(0));
            int count = gbDepartmentBillService.queryDepartmentBillCount(map3);
            int trade = count + 1;
            String no = "";
            if (trade < 100) {
                if (count < 10) {
                    no = "00" + trade;
                } else {
                    no = "0" + trade;
                }
            } else {
                no = String.valueOf(count);
            }

            GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(depFatherId);
            String headPinyin = getHeadStringByString(gbDepartmentEntity.getGbDepartmentName(), true, null);
            String s = formatDayNumber(0) + headPinyin + no;
            Map<String, Object> mapR = new HashMap<>();
            mapR.put("arr", ordersEntities);
            mapR.put("tradeNo", s);
            return R.ok().put("data", mapR);
        }


    }


    /**
     * 库房，中央厨房，txsDistributer
     * 保存订单的重量
     *
     * @param depOrders 订单
     * @return ok
     */
    @RequestMapping(value = "/saveToFillWeight", method = RequestMethod.POST)
    @ResponseBody
    public R saveToFillWeight(@RequestBody List<GbDepartmentOrdersEntity> depOrders) {
        for (GbDepartmentOrdersEntity ordersEntity : depOrders) {
            ordersEntity.setGbDoStatus(getGbOrderStatusProcurement());
            gbDepartmentOrdersService.update(ordersEntity);
        }

        return R.ok();
    }

    /**
     * tsxDistributer
     *
     * @param ordersEntity
     * @return
     */
    @RequestMapping(value = "/saveToFillPriceOrderGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveToFillPriceOrderGb(@RequestBody GbDepartmentOrdersEntity ordersEntity) {
        if (ordersEntity.getGbDoPrice().equals("0.0") || ordersEntity.getGbDoPrice().equals("0.") || ordersEntity.getGbDoPrice().length() == 0) {
            ordersEntity.setGbDoPrice("-1");
            ordersEntity.setGbDoSubtotal("-1");
        }

        ordersEntity.setGbDoStatus(getGbOrderStatusProcurement());
        gbDepartmentOrdersService.update(ordersEntity);
        return R.ok();
    }


    /**
     * kufang出库
     *
     * @param ordersEntity
     * @return
     */
    @RequestMapping(value = "/saveToFillWeightOrderGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveToFillWeightOrderGb(@RequestBody GbDepartmentOrdersEntity ordersEntity) {
        if (ordersEntity.getGbDoWeight().equals("0.0") || ordersEntity.getGbDoWeight().equals("0.") || ordersEntity.getGbDoWeight().equals("0") || ordersEntity.getGbDoWeight().length() == 0) {
            ordersEntity.setGbDoWeight("-1");
            ordersEntity.setGbDoSubtotal("-1");
        }
        ordersEntity.setGbDoStatus(getGbOrderStatusProcurement());
        gbDepartmentOrdersService.update(ordersEntity);

        return R.ok();
    }


    /**
     * txsDistributer
     *
     * @param depOrders
     * @return
     */
    @RequestMapping(value = "/saveToFillPrice", method = RequestMethod.POST)
    @ResponseBody
    public R saveToFillPrice(@RequestBody List<GbDepartmentOrdersEntity> depOrders) {
        for (GbDepartmentOrdersEntity ordersEntity : depOrders) {
            ordersEntity.setGbDoStatus(getGbOrderStatusProcurement());
            gbDepartmentOrdersService.update(ordersEntity);
        }
        return R.ok();
    }


    @RequestMapping(value = "/disGetTodayGoodsOrder", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTodayGoodsOrder(Integer goodsFatherId, String searchDepIds, Integer status, Integer equalStatus,
                                   String startDate, String stopDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsFatherId", goodsFatherId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        if (equalStatus == -1) {
            map.put("status", status);
        } else {
            map.put("equalStatus", equalStatus);
        }
        if (!searchDepIds.equals("-1")) {
            String[] arrGb = searchDepIds.split(",");
            List<String> idsGb = new ArrayList<>();
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("depFatherIds", idsGb);
                }
            }
        }

        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepartmentOrdersService.disGetTodayGoodsOrder(map);

        return R.ok().put("data", distributerGoodsEntities);
    }

    @RequestMapping(value = "/depGetToPlanPurchaseGoods", method = RequestMethod.POST)
    @ResponseBody
    public R depGetToPlanPurchaseGoods(Integer depId, Integer isSelf, String orderType) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", depId);
        map.put("buyStatus", 2);
        map.put("dayuStatus", -1);
        map.put("status", 2);
        map.put("orderType", orderType);
        map.put("isSelf", isSelf);
        System.out.println("akfa" + map);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepartmentOrdersService.disGetUnPlanPurchaseApplys(map);
        Map<String, Object> map33 = new HashMap<>();
        map33.put("depId", depId);
        map33.put("equalStatus", -1);
        map33.put("isSelf", isSelf);
        int weightGoodsAcount = gbDisWeightGoodsService.queryWeightGoodsAccount(map33);
        map33.put("equalStatus", 0);
        int weightTotalAcount = gbDisWeightTotalService.queryDepWeightCountByParams(map33);


        Map<String, Object> map2 = new HashMap<>();
        map2.put("toDepId", depId);
        map2.put("buyStatus", 1);
        map2.put("isSelf", 0);
        map2.put("dayuStatus", -1);
        map2.put("status", 3);
        System.out.println("map22222" + map2);
        Integer amount2 = gbDepartmentOrdersService.queryTotalByParams(map2);

        map2.put("isSelf", 1);
        Integer amount3 = gbDepartmentOrdersService.queryTotalByParams(map2);


        Map<String, Object> todayData = new HashMap<>();
        todayData.put("isSelfAmount", amount3);
        todayData.put("amount", amount2);

        todayData.put("arr", fatherGoodsEntities);
        todayData.put("weightTotal", weightTotalAcount);
        todayData.put("weightGoodsTotal", weightGoodsAcount);

        return R.ok().put("data", todayData);
    }


    @RequestMapping(value = "/depGetToPlanPurchaseGoodsStock", method = RequestMethod.POST)
    @ResponseBody
    public R depGetToPlanPurchaseGoodsStock(Integer depId, Integer orderType) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", depId);
        map.put("buyStatus", 2);
        map.put("isSelf", 0);
        map.put("orderType", orderType);
        map.put("shelfId", 1);
        System.out.println("map=orderTypeorderTypeorderType=" + map);
        List<GbDistributerGoodsShelfEntity> shelfEntities = gbDepartmentOrdersService.disGetUnPlanPurchaseApplysStock(map);

        Map<String, Object> map33 = new HashMap<>();
        map33.put("depId", depId);
        map33.put("equalStatus", -1);
        map33.put("isSelf", 0);
        int weightGoodsAcount = gbDisWeightGoodsService.queryWeightGoodsAccount(map33);
        map33.put("equalStatus", 0);
        int i = gbDisWeightTotalService.queryDepWeightCountByParams(map33);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("toDepId", depId);
        map2.put("buyStatus", 1);
        map2.put("dayuStatus", -1);
        map2.put("status", 3);
        map2.put("isSelf", 0);
        map2.put("isNotSelf", 1);
        Integer amount2 = gbDepartmentOrdersService.queryTotalByParams(map2);

        map2.put("isSelf", 1);
        Integer amount3 = gbDepartmentOrdersService.queryTotalByParams(map2);

        Map<String, Object> todayData = new HashMap<>();
        todayData.put("isSelfAmount", amount3);
        todayData.put("amount", amount2);
        todayData.put("arr", shelfEntities);
        todayData.put("weightTotal", i);
        todayData.put("weightGoodsTotal", weightGoodsAcount);


        return R.ok().put("data", todayData);
    }

    @RequestMapping(value = "/kitchenGetToPlanPurchaseGoods/{depId}")
    @ResponseBody
    public R kitchenGetToPlanPurchaseGoods(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", depId);
        map.put("status", 3);
        map.put("orderType", getGbOrderTypeKitchen());
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepartmentOrdersService.disGetUnPlanPurchaseApplys(map);

        return R.ok().put("data", fatherGoodsEntities);
    }


}

