package com.nongxinle.controller;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatYearDayTime;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeMendian;
import static com.nongxinle.utils.GbTypeUtils.getGbPurchaseGoodsTypeForOrder;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.*;

@Controller
@RequestMapping("api/nxdistributergoodspart")
public class NxDistributerGoodsPartController {

    private static final Logger logger = LoggerFactory.getLogger(NxDistributerGoodsController.class);

    @Autowired
    private NxDistributerGoodsService dgService;
    @Autowired
    private NxDepartmentOrdersService depOrdersService;
    @Autowired
    private NxDepartmentOrdersHistoryService nxDepartmentOrdersHistoryService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxDistributerFatherGoodsService dgfService;
    @Autowired
    private NxDistributerStandardService dsService;
    @Autowired
    private NxDistributerAliasService disAliasService;
    @Autowired
    private NxAliasService nxAliasService;
    @Autowired
    private NxStandardService nxStandardService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepDisGoodsService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDisPurchaseGoodsService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDepartmentOrderHistoryService nxDepartmentOrderHistoryService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDPBService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepDisGoodsService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerFatherGoodsService gbDgfService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private NxDistributerGoodsShelfGoodsService nxDistributerGoodsShelfGoodsService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private NxJrdhSupplierService jrdhSupplierService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;
    @Autowired
    private GbDistributerPurchaseBatchService getGbDPBService;
    @Autowired
    private NxDistributerGbDistributerService nxDisGbDisService;
    @Autowired
    private GbDistributerGoodsService gbDgService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDistributerService gbDistributerService;

    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private NxDistributerGoodsShelfStockService nxDistributerGoodsShelfStockService;
    @Autowired
    private NxDistributerGoodsShelfStockReduceService nxDistributerGoodsShelfStockReduceService;
    @Autowired
    private NxDistributerGoodsShelfService nxDistributerGoodsShelfService;
    @Autowired
    private com.nongxinle.service.AsyncGoodsDownloadService asyncGoodsDownloadService;
    @Autowired
    private NxTraceReportService nxTraceReportService;




    @RequestMapping(value = "/saveNxDisGoodsAndDown", method = RequestMethod.POST)
    @ResponseBody
    public R saveNxDisGoodsAndDown(@RequestBody NxDepartmentOrdersEntity ordersEntity) {
        NxDistributerGoodsEntity goods = ordersEntity.getNxDistributerGoodsEntity();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", 1); //固定 nxDisId==1
        map.put("nxGoodsId", -1);
        map.put("goodsLevel", 2);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
        goods.setNxDgPurchaseAuto(-1);
        goods.setNxDgOutTotalWeight("0");
        goods.setNxDgBuyingPriceOne("0.1");
        goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
        goods.setNxDgWillPriceOne("0.1");
        goods.setNxDgWillPriceOneAboutPrice("0.1");
        dgService.save(goods);

        downDisGoods(goods, ordersEntity);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
        fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
        dgfService.update(fatherGoodsEntity);
        return R.ok().put("data", goods);

    }


    @RequestMapping(value = "/getGoodsSubNamesByFatherIdNx/{fatherId}")
    @ResponseBody
    public R getGoodsSubNamesByFatherIdNx(@PathVariable Integer fatherId) {


        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", fatherId);
        System.out.println("mapapap" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);
        return R.ok().put("data", fatherGoodsEntities);
    }



    @RequestMapping(value = "/getNxDistributerGoodsDetail/{disId}")
    @ResponseBody
    public R getNxDistributerGoodsDetail(@PathVariable Integer disId) {

        List<NxDistributerFatherGoodsEntity> greatGrandGoodsEntities = nxDistributerFatherGoodsService.queryDisGoodsCata(disId);
        if (greatGrandGoodsEntities.size() > 0) {
            for (NxDistributerFatherGoodsEntity greatGrandGoodsEntity : greatGrandGoodsEntities) {
                if (greatGrandGoodsEntity.getFatherGoodsEntities().size() > 0) {
                    for (NxDistributerFatherGoodsEntity grandGoodsEntity : greatGrandGoodsEntity.getFatherGoodsEntities()) {
                        System.out.println("grandGoodsEntity==" + grandGoodsEntity.getNxDfgFatherGoodsName());
                        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = grandGoodsEntity.getFatherGoodsEntities();
                        if (fatherGoodsEntities.size() > 0) {
                            for (NxDistributerFatherGoodsEntity fatherGoods : fatherGoodsEntities) {
                                Integer distributerFatherGoodsId = fatherGoods.getNxDistributerFatherGoodsId();

//                                Map<String, Object> map1 = new HashMap<>();
//                                System.out.println("grandGoodsEntity.getNxDistributerFatherGoodsId()" +fatherGoods.getNxDistributerFatherGoodsId());
//                                map.put("dgFatherId", distributerFatherGoodsId);
                                List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDgSubNameByFatherId(distributerFatherGoodsId);
                                fatherGoods.setNxDistributerGoodsEntities(distributerGoodsEntities);
                                System.out.println(fatherGoodsEntities.size() + "sisziiziziz");
                                System.out.println("fatherNameaaaa==" + fatherGoods.getNxDfgFatherGoodsName());
                            }
                        }
                    }
                }
            }

        }

        return R.ok().put("data", greatGrandGoodsEntities);
    }


    @RequestMapping(value = "/getDgCataGoodsWithSubNames/{disId}")
    @ResponseBody
    public R getDgCataGoodsWithSubNames(@PathVariable Integer disId) {

        List<NxDistributerFatherGoodsEntity> goodsEntities1 = dgfService.queryDisGoodsCata(disId);
        if (goodsEntities1.size() > 0) {
            for (NxDistributerFatherGoodsEntity greatGrandGoods : goodsEntities1) {
                for (NxDistributerFatherGoodsEntity grandGoods : greatGrandGoods.getFatherGoodsEntities()) {
                    for (NxDistributerFatherGoodsEntity fatherGoods : grandGoods.getFatherGoodsEntities()) {
                        StringBuilder builder = new StringBuilder();
                        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDgSubNameByFatherId(fatherGoods.getNxDistributerFatherGoodsId());
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            String nxGoodsName = goodsEntities.get(i).getNxDgGoodsName();
                            if (i == 0) {
                                builder.append(nxGoodsName);
                                builder.append(",");
                            } else {
                                String lastName = goodsEntities.get(i - 1).getNxDgGoodsName();
                                if (!lastName.equals(nxGoodsName)) {
                                    builder.append(nxGoodsName);
                                    builder.append(",");
                                }
                            }
                        }
                        fatherGoods.setDgGoodsSubNames(builder.toString());
                    }
                }
            }
            return R.ok().put("data", goodsEntities1);
        } else {
            return R.error(-1, "没有商品");
        }
    }

    @RequestMapping(value = "/getGoodsSubNamesByFatherId/{fatherId}")
    @ResponseBody
    public R getGoodsSubNamesByFatherId(@PathVariable Integer fatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);
        System.out.println("fathehrididiidididi" + map);
        List<NxGoodsEntity> goodsEntities1 = nxGoodsService.queryNxGoodsOrderByGoodsId(map);

        List<NxGoodsEntity> newList = new ArrayList<>();

        for (NxGoodsEntity fatherGoods : goodsEntities1) {
            StringBuilder builder = new StringBuilder();

            List<NxGoodsEntity> goodsEntities = nxGoodsService.querySubNameByFatherId(fatherGoods.getNxGoodsId());

            for (int i = 0; i < goodsEntities.size(); i++) {
                String nxGoodsName = goodsEntities.get(i).getNxGoodsName();
                if (i == 0) {
                    builder.append(nxGoodsName);
                    builder.append(",");
                } else {
                    String lastName = goodsEntities.get(i - 1).getNxGoodsName();
                    if (!lastName.equals(nxGoodsName)) {
                        builder.append(nxGoodsName);
                        builder.append(",");
                    }
                }

            }


            fatherGoods.setNxGoodsSubNames(builder.toString());
            newList.add(fatherGoods);
        }


        return R.ok().put("data", newList);
    }


    private void downDisGoods(NxDistributerGoodsEntity nxDistributerGoodsEntity, NxDepartmentOrdersEntity ordersEntity) {
        GbDistributerGoodsEntity cgnGoods = new GbDistributerGoodsEntity();
        cgnGoods.setGbDgGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
        cgnGoods.setGbDgGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
        cgnGoods.setGbDgGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
        cgnGoods.setGbDgGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
        cgnGoods.setGbDgGoodsStandardWeight(nxDistributerGoodsEntity.getNxDgGoodsStandardWeight());
        cgnGoods.setGbDgGoodsDetail(nxDistributerGoodsEntity.getNxDgGoodsDetail());
        cgnGoods.setGbDgGoodsBrand(nxDistributerGoodsEntity.getNxDgGoodsBrand());
        cgnGoods.setGbDgGoodsPlace(nxDistributerGoodsEntity.getNxDgGoodsPlace());

        cgnGoods.setGbDgDistributerId(ordersEntity.getNxDoGbDistributerId());
        cgnGoods.setGbDgGoodsStatus(0);
        cgnGoods.setGbDgGoodsIsWeight(0);
        cgnGoods.setGbDgNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
        cgnGoods.setGbDgNxFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
        cgnGoods.setGbDgNxGrandId(nxDistributerGoodsEntity.getNxDgNxGrandId());
        cgnGoods.setGbDgNxGreatGrandId(nxDistributerGoodsEntity.getNxDgNxGreatGrandId());
        cgnGoods.setGbDgPullOff(0);
        cgnGoods.setGbDgGoodsType(5);
        cgnGoods.setGbDgNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
        cgnGoods.setGbDgNxDistributerGoodsId(nxDistributerGoodsEntity.getNxDistributerGoodsId());
        cgnGoods.setGbDgGbDepartmentId(ordersEntity.getNxDoGbDepartmentId());
        cgnGoods.setGbDgControlFresh(0);
        cgnGoods.setGbDgControlPrice(0);
        cgnGoods.setGbDgGoodsInventoryType(1);
        cgnGoods.setGbDgIsFranchisePrice(0);
        cgnGoods.setGbDgIsSelfControl(0);
        gbDistributerGoodsService.save(cgnGoods);

        //添加部门商品
        GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
        disGoodsEntity.setGbDdgDepGoodsName(cgnGoods.getGbDgGoodsName());
        disGoodsEntity.setGbDdgDisGoodsId(cgnGoods.getGbDistributerGoodsId());
        disGoodsEntity.setGbDdgDisGoodsFatherId(cgnGoods.getGbDgDfgGoodsFatherId());
        disGoodsEntity.setGbDdgDisGoodsGrandId(cgnGoods.getGbDgDfgGoodsGrandId());
        disGoodsEntity.setGbDdgDisGoodsGreatId(cgnGoods.getGbDgDfgGoodsGreatId());
        disGoodsEntity.setGbDdgDepGoodsPinyin(cgnGoods.getGbDgGoodsPinyin());
        disGoodsEntity.setGbDdgDepGoodsPy(cgnGoods.getGbDgGoodsPy());
        disGoodsEntity.setGbDdgDepGoodsStandardname(cgnGoods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgDepartmentId(cgnGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgDepartmentFatherId(cgnGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDepartmentId(cgnGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDisId(cgnGoods.getGbDgDistributerId());
        disGoodsEntity.setGbDdgGoodsType(cgnGoods.getGbDgGoodsType());
        disGoodsEntity.setGbDdgStockTotalWeight("0.0");
        disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        disGoodsEntity.setGbDdgShowStandardId(-1);
        disGoodsEntity.setGbDdgShowStandardName(cgnGoods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgShowStandardScale("-1");
        disGoodsEntity.setGbDdgShowStandardWeight(null);
        disGoodsEntity.setGbDdgNxDistributerGoodsId(cgnGoods.getGbDgNxDistributerGoodsId());
        gbDepDisGoodsService.save(disGoodsEntity);


        //添加给门店
        //如果是餐饮商品，自动给门店添加部门商品
        Map<String, Object> map = new HashMap<>();
        map.put("disId", cgnGoods.getGbDgDistributerId());
        map.put("type", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntities) {
                GbDepartmentDisGoodsEntity disGoodsEntityDep = new GbDepartmentDisGoodsEntity();
                disGoodsEntityDep.setGbDdgDepGoodsName(cgnGoods.getGbDgGoodsName());
                disGoodsEntityDep.setGbDdgDisGoodsId(cgnGoods.getGbDistributerGoodsId());
                disGoodsEntityDep.setGbDdgDisGoodsFatherId(cgnGoods.getGbDgDfgGoodsFatherId());
                disGoodsEntityDep.setGbDdgDisGoodsGrandId(cgnGoods.getGbDgDfgGoodsGrandId());
                disGoodsEntityDep.setGbDdgDisGoodsGreatId(cgnGoods.getGbDgDfgGoodsGreatId());
                disGoodsEntityDep.setGbDdgDepGoodsPinyin(cgnGoods.getGbDgGoodsPinyin());
                disGoodsEntityDep.setGbDdgDepGoodsPy(cgnGoods.getGbDgGoodsPy());
                disGoodsEntityDep.setGbDdgDepGoodsStandardname(cgnGoods.getGbDgGoodsStandardname());
                disGoodsEntityDep.setGbDdgDepartmentId(dep.getGbDepartmentId());
                disGoodsEntityDep.setGbDdgDepartmentFatherId(dep.getGbDepartmentId());
                disGoodsEntityDep.setGbDdgGbDepartmentId(cgnGoods.getGbDgGbDepartmentId());
                disGoodsEntityDep.setGbDdgGbDisId(cgnGoods.getGbDgDistributerId());
                disGoodsEntityDep.setGbDdgGoodsType(cgnGoods.getGbDgGoodsType());
                disGoodsEntityDep.setGbDdgStockTotalWeight("0.0");
                disGoodsEntityDep.setGbDdgStockTotalSubtotal("0.0");
                disGoodsEntityDep.setGbDdgShowStandardId(-1);
                disGoodsEntityDep.setGbDdgShowStandardName(cgnGoods.getGbDgGoodsStandardname());
                disGoodsEntityDep.setGbDdgShowStandardScale("-1");
                disGoodsEntityDep.setGbDdgShowStandardWeight(null);
                disGoodsEntityDep.setGbDdgNxDistributerGoodsId(cgnGoods.getGbDgNxDistributerGoodsId());
                gbDepDisGoodsService.save(disGoodsEntityDep);
            }
        }
    }


    @RequestMapping(value = "/changeDisGoodsFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R changeDisGoodsFatherId(Integer disGoodsId, Integer fatherId, Integer grandId) {

        //old
        NxDistributerGoodsEntity goodsEntity = dgService.queryObject(disGoodsId);
        Integer oldfgGoodsFatherId1 = goodsEntity.getNxDgDfgGoodsFatherId();
        NxDistributerFatherGoodsEntity fatherGoodsEntity = dgfService.queryObject(oldfgGoodsFatherId1);
        fatherGoodsEntity.setNxDfgGoodsAmount(fatherGoodsEntity.getNxDfgGoodsAmount() - 1);
        dgfService.update(fatherGoodsEntity);

        //new
        NxDistributerFatherGoodsEntity fatherGoodsEntity1 = dgfService.queryObject(fatherId);
        fatherGoodsEntity1.setNxDfgGoodsAmount(fatherGoodsEntity1.getNxDfgGoodsAmount() + 1);
        dgfService.update(fatherGoodsEntity1);

        goodsEntity.setNxDgDfgGoodsFatherId(fatherId);
        goodsEntity.setNxDgDfgGoodsGrandId(grandId);
        dgService.update(goodsEntity);

        return R.ok();
    }


    @RequestMapping(value = "/saveNxDisLinshiGoodsForApply", method = RequestMethod.POST)
    @ResponseBody
    public R saveNxDisLinshiGoodsForApply(@RequestBody NxDistributerGoodsEntity goods) {


        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disId", goods.getNxDgDistributerId());
        mapG.put("searchStr", goods.getNxDgGoodsName());
        mapG.put("standard", goods.getNxDgGoodsStandardname());

        if (goods.getNxDgGoodsStandardWeight() != null && !goods.getNxDgGoodsStandardWeight().equals("-1")) {
            mapG.put("standardWeight", goods.getNxDgGoodsStandardWeight());
        } else {
            goods.setNxDgGoodsStandardWeight(null);
        }
        if (goods.getNxDgGoodsBrand() != null && !goods.getNxDgGoodsBrand().equals("-1")) {
            mapG.put("brand", goods.getNxDgGoodsBrand());
        } else {
            goods.setNxDgGoodsBrand(null);
        }
        if (goods.getNxDgGoodsPlace() != null && !goods.getNxDgGoodsPlace().equals("-1")) {
            mapG.put("place", goods.getNxDgGoodsPlace());
        } else {
            goods.setNxDgGoodsPlace(null);
        }
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisGoodsByName(mapG);
        if (distributerGoodsEntities.size() > 0) {
            return R.error(-1, "商品已存在");
        } else {
            String goodsName = goods.getNxDgGoodsName();
            Map<String, Object> map = new HashMap<>();
            map.put("disId", goods.getNxDgDistributerId());
            map.put("nxGoodsId", -1);
            map.put("goodsLevel", 2);
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
            NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
            goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
            goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
            goods.setNxDgPurchaseAuto(-1);
            goods.setNxDgWillPrice("0.1");
            goods.setNxDgBuyingPrice("0.1");
            goods.setNxDgBuyingPriceOne("0.1");
            goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
            goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
            goods.setNxDgWillPriceOne("0.1");
            goods.setNxDgWillPriceOneAboutPrice("0.1");
            goods.setNxDgGoodsIsHidden(0);
            String englishKuohao = getEnglishKuohao(goodsName);
            String pinyin = hanziToPinyin(englishKuohao);
            String headPinyin = getHeadStringByString(englishKuohao, false, null);
            goods.setNxDgGoodsPinyin(pinyin);
            goods.setNxDgGoodsPy(headPinyin);
            goods.setNxDgOutTotalWeight("0");
            dgService.save(goods);

            Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
            fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
            dgfService.update(fatherGoodsEntity);

            Integer orderId = goods.getOrderSize();
            NxDepartmentOrdersEntity ordersEntity = depOrdersService.queryObject(orderId);
            ordersEntity.setNxDoNxGoodsId(-1);
            ordersEntity.setNxDoNxGoodsFatherId(-1);
            ordersEntity.setNxDoDisGoodsId(goods.getNxDistributerGoodsId());
            ordersEntity.setNxDoDisGoodsFatherId(goods.getNxDgDfgGoodsFatherId());
            ordersEntity.setNxDoDisGoodsGrandId(goods.getNxDgDfgGoodsGrandId());
            ordersEntity.setNxDoDepDisGoodsId(-1);
            ordersEntity.setNxDoStatus(0);
            ordersEntity.setNxDoPurchaseGoodsId(-1);
            ordersEntity.setNxDoGoodsType(1);
            ordersEntity.setNxDoCostPrice("0.1");
            ordersEntity.setNxDoCostSubtotal("0.1");
            ordersEntity.setNxDoGoodsName(null);
            ordersEntity.setNxDoPrintStandard(goods.getNxDgGoodsStandardname());
            depOrdersService.update(ordersEntity);

            return R.ok().put("data", goods);
        }
    }

    @RequestMapping(value = "/queryDisFatherGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisFatherGoodsByQuickSearch(String searchStr, String disId, Integer fatherId) {

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
        System.out.println("fafas" + map);
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStrByFatherId(map);
        return R.ok().put("data", goodsEntities);
    }

    @RequestMapping(value = "/openGoodsToOrder/{id}")
    @ResponseBody
    public R openGoodsToOrder(@PathVariable Integer id) {
        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(id);
        System.out.println("whwhwhwhwhw" + distributerGoodsEntity.getNxDgGoodsIsHidden());
        if (distributerGoodsEntity.getNxDgGoodsIsHidden() == 1) {
            distributerGoodsEntity.setNxDgGoodsIsHidden(0);
        } else {
            distributerGoodsEntity.setNxDgGoodsIsHidden(1);
        }

        dgService.update(distributerGoodsEntity);
        return R.ok().put("data", distributerGoodsEntity);
    }




    @RequestMapping(value = "/exchangeNewGoodsDisWithLevel", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeNewGoodsDisWithLevel(Integer changeId, Integer toGoodsId, Integer level) {

        System.out.println("echchagnee" + changeId + "toiccic" + toGoodsId);
        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(changeId);
        NxDistributerGoodsEntity toNxGoods = dgService.queryObject(toGoodsId);
        String standard = "";
        String price = "";
        if (level == 2) {
            standard = toNxGoods.getNxDgWillPriceTwoStandard();
            price = toNxGoods.getNxDgWillPriceTwo();
        }
        //order
        Map<String, Object> mapSearch = new HashMap<>();
        mapSearch.put("disGoodsId", changeId);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapSearch);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                ordersEntity.setNxDoDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                ordersEntity.setNxDoDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                ordersEntity.setNxDoNxGoodsId(toNxGoods.getNxDgNxGoodsId());
                ordersEntity.setNxDoNxGoodsFatherId(toNxGoods.getNxDgNxFatherId());
                depOrdersService.update(ordersEntity);
            }
        }
        List<NxDepartmentOrderHistoryEntity> historyEntities = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(mapSearch);
        if (historyEntities.size() > 0) {
            for (NxDepartmentOrderHistoryEntity ordersEntity : historyEntities) {
                ordersEntity.setNxDoDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                ordersEntity.setNxDoDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                ordersEntity.setNxDoDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                ordersEntity.setNxDoNxGoodsId(toNxGoods.getNxDgNxGoodsId());
                ordersEntity.setNxDoNxGoodsFatherId(toNxGoods.getNxDgNxFatherId());
                nxDepartmentOrderHistoryService.update(ordersEntity);
            }
        }

        List<NxDepartmentOrdersHistoryEntity> linshiMapHHS = nxDepartmentOrdersHistoryService.queryDepHistoryOrdersByParams(mapSearch);
        if (linshiMapHHS.size() > 0) {
            for (NxDepartmentOrdersHistoryEntity ordersEntity : linshiMapHHS) {
                ordersEntity.setNxDohDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                nxDepartmentOrdersHistoryService.update(ordersEntity);
            }
        }

        //depGoods
        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapSearch);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity linshiDepGoodsEntity : departmentDisGoodsEntities) {
                Integer nxDdgDepartmentId = linshiDepGoodsEntity.getNxDdgDepartmentId();
                Map<String, Object> map = new HashMap<>();
                map.put("depId", nxDdgDepartmentId);
                map.put("disGoodsId", toGoodsId);
                System.out.println("depdisgoenen" + map);
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
                if (nxDepartmentDisGoodsEntity != null) {
                    System.out.println("hasdepgoods" + nxDepartmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsName(toNxGoods.getNxDgGoodsName());
                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsStandardname(standard);
                    nxDepartmentDisGoodsEntity.setNxDdgOrderStandard(standard);
                    nxDepartmentDisGoodsEntity.setNxDdgOrderPrice(price);
                    nxDepartmentDisGoodsEntity.setNxDdgOrderGoodsName(toNxGoods.getNxDgGoodsName());
                    nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);
                    nxDepartmentDisGoodsService.delete(linshiDepGoodsEntity.getNxDepartmentDisGoodsId());

                } else {
                    linshiDepGoodsEntity.setNxDdgDisGoodsId(toGoodsId);
                    linshiDepGoodsEntity.setNxDdgDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                    linshiDepGoodsEntity.setNxDdgDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                    NxDistributerFatherGoodsEntity grandEntity = nxDistributerFatherGoodsService.queryObject(toNxGoods.getNxDgDfgGoodsGrandId());
                    Integer nxDfgGreatId = grandEntity.getNxDfgFathersFatherId();
                    linshiDepGoodsEntity.setNxDdgDisGoodsGreatId(nxDfgGreatId);
                    linshiDepGoodsEntity.setNxDdgDepGoodsName(toNxGoods.getNxDgGoodsName());
                    linshiDepGoodsEntity.setNxDdgOrderGoodsName(toNxGoods.getNxDgGoodsName());
                    linshiDepGoodsEntity.setNxDdgOrderStandard(standard);
                    linshiDepGoodsEntity.setNxDdgDepGoodsStandardname(standard);
                    nxDepartmentDisGoodsService.update(linshiDepGoodsEntity);
                }
            }
        }
//

        //purGoods
        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(mapSearch);

        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        List<NxDistributerGoodsShelfGoodsEntity> entities = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(mapSearch);
        if (entities.size() > 0) {
            for (NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity : entities) {
                nxDistributerGoodsShelfGoodsService.delete(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
            }
        }


        Map<String, Object> mapGb = new HashMap<>();
        mapGb.put("nxDisGoodsId", changeId);
        System.out.println("gbdidiididid" + mapGb);
        List<GbDistributerGoodsEntity> goodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapGb);
        if (goodsEntities.size() > 0) {
            for (GbDistributerGoodsEntity gbDistributerGoodsEntity : goodsEntities) {
                gbDistributerGoodsEntity.setGbDgNxDistributerGoodsId(toGoodsId);
                gbDistributerGoodsService.update(gbDistributerGoodsEntity);
            }
        }


        List<GbDepartmentOrdersEntity> gbOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(mapGb);
        if (gbOrdersEntities.size() > 0) {
            for (GbDepartmentOrdersEntity ordersEntity : gbOrdersEntities) {
                ordersEntity.setGbDoNxDistributerGoodsId(toGoodsId);
                gbDepartmentOrdersService.update(ordersEntity);
            }
        }


        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("nxGoodsId", changeId);
        List<GbDepartmentDisGoodsEntity> gbDepartmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapDep);
        if (gbDepartmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity disGoodsEntity : gbDepartmentDisGoodsEntities) {
                gbDepartmentDisGoodsService.delete(disGoodsEntity.getGbDepartmentDisGoodsId());
            }
        }


        Map<String, Object> map1 = new HashMap<>();
        map1.put("dgFatherId", distributerGoodsEntity.getNxDgDfgGoodsFatherId());
        //搜索fatherId下有几个disGoods
        List<NxDistributerGoodsEntity> totalDisGoods = dgService.queryDisGoodsByParams(map1);
        //如果disGoods的父类只有一个商品
        if (totalDisGoods.size() == 1) {
            //父类Entity
            NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
            //disGoods的grandId
            Integer grandId = nxDistributerFatherGoodsEntity.getNxDfgFathersFatherId();
            Map<String, Object> mapGrand = new HashMap<>();
            mapGrand.put("fathersFatherId", grandId);
            //搜索grand有几个兄弟
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(mapGrand);
            if (fatherGoodsEntities.size() == 1) {
                Integer nxDfgFathersFatherId = fatherGoodsEntities.get(0).getNxDfgFathersFatherId();
                NxDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(nxDfgFathersFatherId);
                Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
                Map<String, Object> map = new HashMap<>();
                map.put("fathersFatherId", greatGrandId);
                List<NxDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);

                //如果grandFather也是只有一个，则删除greatGrandFather
                if (grandGoodsEntities.size() == 1) {
                    dgfService.delete(greatGrandId);
                }
                dgfService.delete(grandId);
            }
            dgfService.delete(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
        } else {
            //父类商品数量减去1
            NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
            Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
            nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount - 1);
            dgfService.update(nxDistributerFatherGoodsEntity);
        }

        dgService.delete(changeId);

        return R.ok();
    }




    /**
     * 配送商添加兄弟商品，暂时没用
     * @param
     * @return
     * @date 2026-01-10
     */
    @RequestMapping(value = "/disChangeLinshiToSub", method = RequestMethod.POST)
    @ResponseBody
    public R disChangeLinshiToSub(Integer lsGoodsId, Integer nxGoodsId, Integer disId) {


        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxGoodsId);

        String goodsName = nxDistributerGoodsEntity.getNxDgGoodsName();
        String nxGoodsDetail = nxDistributerGoodsEntity.getNxDgGoodsDetail();
        String nxGoodsBrand = nxDistributerGoodsEntity.getNxDgGoodsBrand();
        String nxDgGoodsStandardname = nxDistributerGoodsEntity.getNxDgGoodsStandardname();
        String nxDgGoodsStandardWeight = nxDistributerGoodsEntity.getNxDgGoodsStandardWeight();

        Map<String, Object> mapS = new HashMap<>();
        mapS.put("goodsName", goodsName);
        mapS.put("goodsStandard", nxDgGoodsStandardname);
        mapS.put("goodsDetail", nxGoodsDetail);
        mapS.put("goodsBrand", nxGoodsBrand);
        mapS.put("standardWeight", nxDgGoodsStandardWeight);
        mapS.put("disId", nxDistributerGoodsEntity.getNxDgDistributerId());
        mapS.put("fatherId", nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
        System.out.println("mapaidididpdppdpdpdpdpdpdpdp" + mapS);
        List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = dgService.queryIfHasSameDisGoods(mapS);
        if (nxDistributerGoodsEntities.size() > 1) {

            return R.error(-1, "已有相同商品");

        } else {
            Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
            Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
            Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();
            Integer nxDgNxFatherId = nxDistributerGoodsEntity.getNxDgNxFatherId();

            NxDistributerGoodsEntity linshiGoods = dgService.queryObject(lsGoodsId);
            linshiGoods.setNxDgDfgGoodsFatherId(nxDgDfgGoodsFatherId);
            linshiGoods.setNxDgDfgGoodsGrandId(nxDgDfgGoodsGrandId);
            linshiGoods.setNxDgNxFatherId(nxDgNxFatherId);
            linshiGoods.setNxDgNxGoodsId(null);
            linshiGoods.setNxDgIsOldestSon(0);
            linshiGoods.setNxDgGoodsStatus(1);
            linshiGoods.setNxDgNxGrandId(nxDistributerGoodsEntity.getNxDgNxGrandId());
            linshiGoods.setNxDgNxGreatGrandId(nxDistributerGoodsEntity.getNxDgNxGreatGrandId());
            linshiGoods.setNxDgGoodsSort(nxDistributerGoodsEntity.getNxDgGoodsSort());
            Map<String, Object> map = new HashMap<>();
            map.put("fatherId", nxDgDfgGoodsFatherId);
            int wxCountAuto = dgService.queryDisGoodsTotal(map);
            linshiGoods.setNxDgGoodsSonsSort(wxCountAuto + 1);
            dgService.update(linshiGoods);

            //update FatherGoods
            NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsFatherId);
            fatherGoodsEntity.setNxDfgGoodsAmount(fatherGoodsEntity.getNxDfgGoodsAmount() + 1);
            nxDistributerFatherGoodsService.update(fatherGoodsEntity);

            Map<String, Object> linshiMap = new HashMap<>();
            linshiMap.put("disGoodsId", lsGoodsId);
            List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(linshiMap);
            if (ordersEntities.size() > 0) {
                for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                    ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                    ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                    depOrdersService.update(ordersEntity);
                }
            }

            Map<String, Object> linshiMapH = new HashMap<>();
            linshiMapH.put("disGoodsId", lsGoodsId);
            List<NxDepartmentOrderHistoryEntity> ordersEntitiesH = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(linshiMapH);
            if (ordersEntitiesH.size() > 0) {
                for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesH) {
                    ordersEntity.setNxDoDisGoodsId(nxGoodsId);
                    ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                    ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                    nxDepartmentOrderHistoryService.update(ordersEntity);
                }
            }
            List<NxDepartmentOrdersHistoryEntity> linshiMapHHS = nxDepartmentOrdersHistoryService.queryDepHistoryOrdersByParams(linshiMapH);
            if (linshiMapHHS.size() > 0) {
                for (NxDepartmentOrdersHistoryEntity ordersEntity : linshiMapHHS) {
                    ordersEntity.setNxDohDisGoodsId(nxGoodsId);
                    nxDepartmentOrdersHistoryService.update(ordersEntity);
                }
            }

            List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(linshiMap);
            if (departmentDisGoodsEntities.size() > 0) {
                for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                    depGoodsEntity.setNxDdgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    depGoodsEntity.setNxDdgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    NxDistributerFatherGoodsEntity grand = nxDistributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                    Integer greatFatherId = grand.getNxDfgFathersFatherId();
                    depGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);

                    nxDepartmentDisGoodsService.update(depGoodsEntity);
                }
            }


            List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(linshiMap);
            if (purchaseGoodsEntities.size() > 0) {
                for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                    purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            }

            return R.ok();

        }

    }


    @RequestMapping(value = "/moveGoods", method = RequestMethod.POST)
    @ResponseBody
    public R moveGoods(Integer toFatherId, Integer moveId) {
        // 0， 新 nxGoods
        NxGoodsEntity fatherGoods = nxGoodsService.queryObject(toFatherId);

        NxGoodsEntity moveGoods = nxGoodsService.queryObject(moveId);

        Integer nxGoodsFatherId = moveGoods.getNxGoodsFatherId();
        int total = nxGoodsService.queryTotalByFatherId(nxGoodsFatherId);

        if (total == 1) {
            NxGoodsEntity father = nxGoodsService.queryObject(nxGoodsFatherId);
            nxGoodsService.delete(father.getNxGoodsId());
        }

        moveGoods.setNxGoodsFatherId(fatherGoods.getNxGoodsId());
        moveGoods.setNxGoodsGrandId(fatherGoods.getNxGoodsFatherId());
        moveGoods.setNxGoodsGreatGrandId(fatherGoods.getNxGoodsGrandId());
        System.out.println("firstmovveieieiieiieiei" + moveGoods);
        nxGoodsService.update(moveGoods);


        System.out.println("stopeoneeeoeoeo");
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.querydisGoodsByNxGoodsId(moveId);

        if (distributerGoodsEntities.size() > 0) {

            for (NxDistributerGoodsEntity delNxGoods : distributerGoodsEntities) {

                NxDistributerGoodsEntity toNxDisGoods = saveDisGoods(delNxGoods);

                Map<String, Object> mapSearch = new HashMap<>();
                mapSearch.put("disGoodsId", delNxGoods.getNxDistributerGoodsId());
                System.out.println("mapseredisGoodsId1111" + mapSearch);
                List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapSearch);
                if (ordersEntities.size() > 0) {
                    for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                        ordersEntity.setNxDoDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                        ordersEntity.setNxDoDisGoodsGrandId(toNxDisGoods.getNxDgDfgGoodsGrandId());
                        ordersEntity.setNxDoDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                        ordersEntity.setNxDoNxGoodsFatherId(fatherGoods.getNxGoodsId());
                        depOrdersService.update(ordersEntity);

                    }
                }

                List<NxDepartmentOrderHistoryEntity> ordersEntitiesHis = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(mapSearch);
                if (ordersEntitiesHis.size() > 0) {
                    for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesHis) {
                        ordersEntity.setNxDoDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                        ordersEntity.setNxDoDisGoodsGrandId(toNxDisGoods.getNxDgDfgGoodsGrandId());
                        ordersEntity.setNxDoDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                        ordersEntity.setNxDoNxGoodsFatherId(fatherGoods.getNxGoodsId());
                        nxDepartmentOrderHistoryService.update(ordersEntity);
                    }
                }

                //depGoods
                System.out.println("drpGHooddsossooss" + mapSearch);
                List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapSearch);
                if (departmentDisGoodsEntities.size() > 0) {
                    for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("depId", depGoodsEntity.getNxDdgDepartmentId());
                        map.put("disGoodsId", toNxDisGoods.getNxDistributerGoodsId());
                        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
                        if (departmentDisGoodsEntity != null) {

                            Map<String, Object> mapDepOrder = new HashMap<>();
                            mapDepOrder.put("depDisGoodsId", depGoodsEntity.getNxDepartmentDisGoodsId());
                            System.out.println("mapseredisGoodsId22222" + mapSearch);
                            List<NxDepartmentOrdersEntity> ordersEntitiesDep = depOrdersService.queryDisOrdersByParams(mapDepOrder);
                            if (ordersEntitiesDep.size() > 0) {
                                for (NxDepartmentOrdersEntity ordersEntity : ordersEntitiesDep) {
                                    ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                                    depOrdersService.update(ordersEntity);
                                }
                            }

                            System.out.println("mapseredisGoodsI3333333" + mapSearch);
                            List<NxDepartmentOrderHistoryEntity> ordersEntitiesDepHis = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(mapDepOrder);
                            if (ordersEntitiesDep.size() > 0) {
                                for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesDepHis) {
                                    ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                                    nxDepartmentOrderHistoryService.update(ordersEntity);
                                }
                            }

                            nxDepartmentDisGoodsService.delete(depGoodsEntity.getNxDepartmentDisGoodsId());

                        } else {
                            depGoodsEntity.setNxDdgDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                            depGoodsEntity.setNxDdgDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                            nxDepartmentDisGoodsService.update(depGoodsEntity);
                        }

                    }

                    //purGoods
                    System.out.println("purgodosssss" + mapSearch);
                    List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(mapSearch);
                    if (purchaseGoodsEntities.size() > 0) {
                        for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                            purchaseGoodsEntity.setNxDpgDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                            purchaseGoodsEntity.setNxDpgDisGoodsGrandId(toNxDisGoods.getNxDgDfgGoodsGrandId());
                            purchaseGoodsEntity.setNxDpgDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                            nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
                        }
                    }


                    //gbDisGoods
                    Map<String, Object> mapG = new HashMap<>();
                    mapG.put("nxDisGoodsId", delNxGoods.getNxDistributerGoodsId());
                    List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapG);
                    if (gbDistributerGoodsEntities.size() > 0) {
                        for (GbDistributerGoodsEntity gbDistributerGoodsEntity : gbDistributerGoodsEntities) {
                            gbDistributerGoodsEntity.setGbDgNxDistributerGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                            gbDistributerGoodsService.update(gbDistributerGoodsEntity);
                        }
                    }

                    // gbDepOrders
                    List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(mapG);
                    if (gbDepartmentOrdersEntities.size() > 0) {
                        for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
                            gbDepartmentOrdersEntity.setGbDoNxDistributerGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
                        }
                    }
                }


                //lastt
                dgService.delete(delNxGoods.getNxDistributerGoodsId());
            }

        }

        return R.ok();
    }


    @RequestMapping(value = "/updateFatherId")
    @ResponseBody
    public R updateFatherId() {

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryWenti();
        System.out.println("deiisissisissiis" + departmentDisGoodsEntities.size());
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                if (nxDistributerGoodsEntity != null) {

                    System.out.println("depgoods111" + nxDistributerGoodsEntity.getNxDgGoodsName());
                    System.out.println("depgoods111ffffff" + nxDistributerGoodsEntity.getNxDgNxFatherId());
                    NxGoodsEntity nxFather = nxGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgNxFatherId());
                    NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
                    if (nxFather != null && fatherGoodsEntity == null) {
                        NxDistributerFatherGoodsEntity goodsEntity = new NxDistributerFatherGoodsEntity();

                        goodsEntity.setNxDistributerFatherGoodsId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
                        goodsEntity.setNxDfgFatherGoodsName(nxFather.getNxGoodsName());
                        goodsEntity.setNxDfgFathersFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                        goodsEntity.setNxDfgDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
                        NxDistributerFatherGoodsEntity grandEnttitty = nxDistributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());

                        if (grandEnttitty != null && grandEnttitty.getNxDfgFatherGoodsSort() != null) {

                            Integer nxDgNxGreatGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
                            int sort = nxDistributerFatherGoodsService.queryMaxSortByFatherId(nxDgNxGreatGrandId);
                            goodsEntity.setNxDfgFatherGoodsSort(sort + 1);
                            goodsEntity.setNxDfgFatherGoodsLevel(2);
                            goodsEntity.setNxDfgGoodsAmount(1);
                            NxDistributerFatherGoodsEntity grand = dgfService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                            goodsEntity.setNxDfgFatherGoodsColor(grand.getNxDfgFatherGoodsColor());
                            goodsEntity.setNxDfgFatherGoodsImg("goodsImage/logo.jpg");
                            goodsEntity.setNxDfgNxGoodsId(nxFather.getNxGoodsId());

                            nxDistributerFatherGoodsService.save(goodsEntity);
                        }

                    }
                }


            }
        }

        return R.ok();
    }

    private NxDistributerGoodsEntity saveDisGoods(NxDistributerGoodsEntity cgnGoods) {
        Integer nxDgNxGoodsId = cgnGoods.getNxDgNxGoodsId();
        System.out.println("nxgoidsisissi" + cgnGoods.getNxDgNxGoodsId());
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
        cgnGoods.setNxDgPurchaseAuto(-1);
        cgnGoods.setNxDgQuantityDays(nxGoodsEntity.getNxGoodsQuantityDays());
        cgnGoods.setNxDgItemsPerCarton(nxGoodsEntity.getNxGoodsItemsPerCarton());
        cgnGoods.setNxDgCartonUnit(nxGoodsEntity.getNxGoodsCartonUnit());

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
        Integer nxDgNxGreatGrandId = cgnGoods.getNxDgNxGreatGrandId();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDgDistributerId);
        map.put("nxFatherId", nxDgNxFatherId);
        // 添加曾祖父ID限制，避免不同曾祖父分类下的同名商品混淆（如"蔬菜"下的"菌菇"和"蔬菜调理"下的"香菇"）
        if (nxDgNxGreatGrandId != null) {
            map.put("nxGreatGrandFatherId", nxDgNxGreatGrandId);
        }
        List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = dgService.queryDisGoodsByParams(map);

        if (nxDistributerGoodsEntities.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsEntities.get(0);
            Integer nxDgDfgGoodsFatherId1 = disGoodsEntity.getNxDgDfgGoodsFatherId();
            Integer nxDgDfgGoodsGrandId = disGoodsEntity.getNxDgDfgGoodsGrandId();

            // 检查父级商品是否存在，如果不存在则抛出异常
            if (nxDgDfgGoodsFatherId1 != null) {
                NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(nxDgDfgGoodsFatherId1);
                if (nxDistributerFatherGoodsEntity == null) {
                    throw new RuntimeException("父级商品不存在，ID: " + nxDgDfgGoodsFatherId1 + ", 配送商品ID: " + disGoodsEntity.getNxDistributerGoodsId());
                }
                Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
                nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount + 1);
                dgfService.update(nxDistributerFatherGoodsEntity);
            } else {
                throw new RuntimeException("配送商品没有关联父级商品ID，配送商品ID: " + disGoodsEntity.getNxDistributerGoodsId());
            }

            //2，保存disId商品
            Integer nxDgDfgGoodsFatherId = disGoodsEntity.getNxDgDfgGoodsFatherId();
            cgnGoods.setNxDgDfgGoodsFatherId(nxDgDfgGoodsFatherId);
            cgnGoods.setNxDgDfgGoodsGrandId(nxDgDfgGoodsGrandId);

            cgnGoods.setNxDgOutTotalWeight("0");
            //1 ，先保存disGoods
            dgService.save(cgnGoods);
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
            dgfService.save(dgf);

            //继续查询是否有GrandFather
            String grandName = cgnGoods.getNxDgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", nxDgDistributerId);
            map2.put("fathersFatherName", grandName);
            map2.put("fatherGoodsLevel", 1); // grand是level 1
            map2.put("nxGoodsId", cgnGoods.getNxDgNxGrandId()); // 添加nxGoodsId限制，避免同名但不同分类的grand混淆
            List<NxDistributerFatherGoodsEntity> grandGoodsFather = dgfService.queryHasDisFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = grandGoodsFather.get(0);
                dgf.setNxDfgFathersFatherId(nxDistributerFatherGoodsEntity.getNxDistributerFatherGoodsId());
                dgfService.update(dgf);
                //更新disGoods的fatherGoodsId
                Integer distributerFatherGoodsId = dgf.getNxDistributerFatherGoodsId();
                Integer nxDfgFathersFatherId = dgf.getNxDfgFathersFatherId();
                cgnGoods.setNxDgDfgGoodsFatherId(distributerFatherGoodsId);
                cgnGoods.setNxDgDfgGoodsGrandId(nxDfgFathersFatherId);
                cgnGoods.setNxDgOutTotalWeight("0");
                dgService.save(cgnGoods);

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
                dgfService.save(grand);

                dgf.setNxDfgFathersFatherId(grand.getNxDistributerFatherGoodsId());
                dgfService.update(dgf);
                //更新disGoods的fatherGoodsId
                Integer distributerFatherGoodsId = dgf.getNxDistributerFatherGoodsId();
                Integer nxDfgFathersFatherId = dgf.getNxDfgFathersFatherId();
                cgnGoods.setNxDgDfgGoodsFatherId(distributerFatherGoodsId);
                cgnGoods.setNxDgDfgGoodsGrandId(nxDfgFathersFatherId);
                cgnGoods.setNxDgOutTotalWeight("0");
                dgService.save(cgnGoods);
                //查询是否有greatGrand
                String greatGrandName = cgnGoods.getNxDgNxGreatGrandName();
                Map<String, Object> map3 = new HashMap<>();
                map3.put("disId", nxDgDistributerId);
                map3.put("fathersFatherName", greatGrandName);
                map3.put("fatherGoodsLevel", 0); // greatGrand是level 0
                map3.put("nxGoodsId", cgnGoods.getNxDgNxGreatGrandId()); // 添加nxGoodsId限制，避免同名但不同分类的greatGrand混淆
                List<NxDistributerFatherGoodsEntity> greatGrandGoodsFather = dgfService.queryHasDisFathersFather(map3);
                if (greatGrandGoodsFather.size() > 0) {
                    NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = greatGrandGoodsFather.get(0);
                    Integer disFatherId = nxDistributerFatherGoodsEntity.getNxDistributerFatherGoodsId();
                    grand.setNxDfgFathersFatherId(disFatherId);
                    dgfService.update(grand);
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
                    dgfService.save(greatGrand);
                    grand.setNxDfgFathersFatherId(greatGrand.getNxDistributerFatherGoodsId());
                    dgfService.update(grand);
                }
            }
        }
        return cgnGoods;
    }

    //todo test
    @RequestMapping(value = "/exchangeNewGoods", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeNewGoods(Integer deleteId, Integer toGoodsId) {

        // 0， 新 nxGoods
        NxGoodsEntity toGoods = nxGoodsService.queryObject(toGoodsId);
        // 1,获取所有要删除 nxgoodsId 的 nxDisttributerGoods
        System.out.println("stopeoneeeoeoeo");
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.querydisGoodsByNxGoodsId(deleteId);

        if (distributerGoodsEntities.size() > 0) {

            for (NxDistributerGoodsEntity delNxGoods : distributerGoodsEntities) {

                Map<String, Object> mapDepGoods = new HashMap<>();
                mapDepGoods.put("nxGoodsId", toGoodsId);
                mapDepGoods.put("disId", delNxGoods.getNxDgDistributerId());
                System.out.println("queyrdidididiiididiid" + mapDepGoods);
                NxDistributerGoodsEntity toNxDisGoods = dgService.queryOneGoodsAboutNxGoods(mapDepGoods);
                //转移的商品如果有
                if (toNxDisGoods != null) {

                    toNxDisGoods.setNxDgPurchaseAuto(delNxGoods.getNxDgPurchaseAuto());
                    toNxDisGoods.setNxDgGoodsFile(toGoods.getNxGoodsFile());
                    toNxDisGoods.setNxDgGoodsFileLarge(toGoods.getNxGoodsFileBig());
                    toNxDisGoods.setNxDgCartonUnit(toGoods.getNxGoodsCartonUnit());
                    toNxDisGoods.setNxDgItemsPerCarton(toGoods.getNxGoodsItemsPerCarton());

                    if (delNxGoods.getNxDgWillPriceOne() != null) {
                        toNxDisGoods.setNxDgWillPriceOne(delNxGoods.getNxDgWillPrice());
                        toNxDisGoods.setNxDgWillPriceOneStandard(delNxGoods.getNxDgWillPriceOneStandard());
                        toNxDisGoods.setNxDgBuyingPriceOneUpdate(delNxGoods.getNxDgBuyingPriceOneUpdate());
                        if (delNxGoods.getNxDgWillPriceTwo() != null) {
                            toNxDisGoods.setNxDgWillPriceTwo(delNxGoods.getNxDgWillPrice());
                            toNxDisGoods.setNxDgWillPriceTwoStandard(delNxGoods.getNxDgWillPriceTwoStandard());
                            toNxDisGoods.setNxDgWillPriceTwoAboutPrice(delNxGoods.getNxDgWillPriceTwoAboutPrice());
                            toNxDisGoods.setNxDgWillPriceTwoWeight(delNxGoods.getNxDgWillPriceTwoWeight());
                            toNxDisGoods.setNxDgBuyingPriceTwoUpdate(delNxGoods.getNxDgBuyingPriceTwoUpdate());
                        }

                        if (delNxGoods.getNxDgSupplierId() != null) {
                            toNxDisGoods.setNxDgSupplierId(delNxGoods.getNxDgSupplierId());
                        }
                        dgService.update(toNxDisGoods);
                    }

                    Map<String, Object> mapSearch = new HashMap<>();
                    mapSearch.put("disGoodsId", delNxGoods.getNxDistributerGoodsId());
                    System.out.println("mapseredisGoodsId1111" + mapSearch);
                    List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapSearch);
                    if (ordersEntities.size() > 0) {
                        for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                            ordersEntity.setNxDoDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                            ordersEntity.setNxDoDisGoodsGrandId(toNxDisGoods.getNxDgDfgGoodsGrandId());
                            ordersEntity.setNxDoDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                            ordersEntity.setNxDoNxGoodsId(toGoods.getNxGoodsId());
                            ordersEntity.setNxDoNxGoodsFatherId(toGoods.getNxGoodsFatherId());
                            depOrdersService.update(ordersEntity);

                        }
                    }

                    List<NxDepartmentOrderHistoryEntity> ordersEntitiesHis = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(mapSearch);
                    if (ordersEntitiesHis.size() > 0) {
                        for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesHis) {
                            ordersEntity.setNxDoDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                            ordersEntity.setNxDoDisGoodsGrandId(toNxDisGoods.getNxDgDfgGoodsGrandId());
                            ordersEntity.setNxDoDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                            ordersEntity.setNxDoNxGoodsId(toGoods.getNxGoodsId());
                            ordersEntity.setNxDoNxGoodsFatherId(toGoods.getNxGoodsFatherId());
                            nxDepartmentOrderHistoryService.update(ordersEntity);
                        }
                    }

                    //depGoods
                    System.out.println("drpGHooddsossooss" + mapSearch);
                    List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapSearch);
                    if (departmentDisGoodsEntities.size() > 0) {
                        for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("depId", depGoodsEntity.getNxDdgDepartmentId());
                            map.put("disGoodsId", toNxDisGoods.getNxDistributerGoodsId());
                            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
                            if (departmentDisGoodsEntity != null) {

                                Map<String, Object> mapDepOrder = new HashMap<>();
                                mapDepOrder.put("depDisGoodsId", depGoodsEntity.getNxDepartmentDisGoodsId());
                                System.out.println("mapseredisGoodsId22222" + mapSearch);
                                List<NxDepartmentOrdersEntity> ordersEntitiesDep = depOrdersService.queryDisOrdersByParams(mapDepOrder);
                                if (ordersEntitiesDep.size() > 0) {
                                    for (NxDepartmentOrdersEntity ordersEntity : ordersEntitiesDep) {
                                        ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                                        depOrdersService.update(ordersEntity);
                                    }
                                }

                                System.out.println("mapseredisGoodsI3333333" + mapSearch);
                                List<NxDepartmentOrderHistoryEntity> ordersEntitiesDepHis = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(mapDepOrder);
                                if (ordersEntitiesDep.size() > 0) {
                                    for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesDepHis) {
                                        ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                                        nxDepartmentOrderHistoryService.update(ordersEntity);
                                    }
                                }

                                nxDepartmentDisGoodsService.delete(depGoodsEntity.getNxDepartmentDisGoodsId());

                            } else {
                                depGoodsEntity.setNxDdgDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                                depGoodsEntity.setNxDdgDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                                nxDepartmentDisGoodsService.update(depGoodsEntity);
                            }

                        }

                        //purGoods
                        System.out.println("purgodosssss" + mapSearch);
                        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(mapSearch);
                        if (purchaseGoodsEntities.size() > 0) {
                            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                                purchaseGoodsEntity.setNxDpgDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(toNxDisGoods.getNxDgDfgGoodsGrandId());
                                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
                            }
                        }


                        //gbDisGoods
                        Map<String, Object> mapG = new HashMap<>();
                        mapG.put("nxDisGoodsId", delNxGoods.getNxDistributerGoodsId());
                        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapG);
                        if (gbDistributerGoodsEntities.size() > 0) {
                            for (GbDistributerGoodsEntity gbDistributerGoodsEntity : gbDistributerGoodsEntities) {
                                gbDistributerGoodsEntity.setGbDgNxDistributerGoodsId(toGoodsId);
                                gbDistributerGoodsService.update(gbDistributerGoodsEntity);
                            }
                        }

                        // gbDepOrders
                        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(mapG);
                        if (gbDepartmentOrdersEntities.size() > 0) {
                            for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
                                gbDepartmentOrdersEntity.setGbDoNxDistributerGoodsId(toGoodsId);
                                gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
                            }
                        }
                    }


                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("dgFatherId", delNxGoods.getNxDgDfgGoodsFatherId());
                    //搜索fatherId下有几个disGoods
                    List<NxDistributerGoodsEntity> totalDisGoods = dgService.queryDisGoodsByParams(map1);
                    //如果disGoods的父类只有一个商品
                    System.out.println("totalDisGoodstotalDisGoodstotalDisGoods" + totalDisGoods.size());
                    if (totalDisGoods.size() == 1) {
//                    //父类Entity
                        NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(delNxGoods.getNxDgDfgGoodsFatherId());
                        //disGoods的grandId
                        Integer grandId = nxDistributerFatherGoodsEntity.getNxDfgFathersFatherId();
                        Map<String, Object> mapGrand = new HashMap<>();
                        mapGrand.put("fathersFatherId", grandId);
                        //搜索grand有几个兄弟
                        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(mapGrand);
                        if (fatherGoodsEntities.size() == 1) {
                            Integer nxDfgFathersFatherId = fatherGoodsEntities.get(0).getNxDfgFathersFatherId();
                            NxDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(nxDfgFathersFatherId);
                            Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
                            Map<String, Object> map = new HashMap<>();
                            map.put("fathersFatherId", greatGrandId);
                            List<NxDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);

                            //如果grandFather也是只有一个，则删除greatGrandFather
                            if (grandGoodsEntities.size() == 1) {
                                dgfService.delete(greatGrandId);
                            }
                            dgfService.delete(grandId);
                        }

                        dgfService.delete(nxDistributerFatherGoodsEntity.getNxDistributerFatherGoodsId());

                    } else {
                        //父类商品数量减去1
                        NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(delNxGoods.getNxDgDfgGoodsFatherId());
                        Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
                        nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount - 1);
                        dgfService.update(nxDistributerFatherGoodsEntity);
                    }

                    dgService.delete(delNxGoods.getNxDistributerGoodsId());

                } else {

                    System.out.println("updatneExchangeee");
                    delNxGoods.setNxDgNxGoodsId(toGoodsId);
                    delNxGoods.setNxDgNxFatherId(toGoods.getNxGoodsFatherId());
                    delNxGoods.setNxDgNxGrandId(toGoods.getNxGoodsGrandId());
                    delNxGoods.setNxDgGoodsName(toGoods.getNxGoodsName());
                    delNxGoods.setNxDgGoodsBrand(toGoods.getNxGoodsBrand());
                    delNxGoods.setPerPrice(toGoods.getNxGoodsPlace());
                    delNxGoods.setNxDgGoodsDetail(toGoods.getNxGoodsDetail());
                    delNxGoods.setNxDgGoodsStandardname(toGoods.getNxGoodsStandardname());
                    delNxGoods.setNxDgGoodsStandardWeight(toGoods.getNxGoodsStandardWeight());
                    System.out.println("upadtttetteeeeee" + delNxGoods);
                    delNxGoods.setNxDgGoodsFile(toGoods.getNxGoodsFile());
                    delNxGoods.setNxDgGoodsFileLarge(toGoods.getNxGoodsFileBig());
                    delNxGoods.setNxDgCartonUnit(toGoods.getNxGoodsCartonUnit());
                    delNxGoods.setNxDgItemsPerCarton(toGoods.getNxGoodsItemsPerCarton());
                    dgService.update(delNxGoods);

                    Integer nxDgDfgGoodsFatherId = delNxGoods.getNxDgDfgGoodsFatherId();
                    NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsFatherId);
                    fatherGoodsEntity.setNxDfgNxGoodsId(toGoods.getNxGoodsFatherId());
                    nxDistributerFatherGoodsService.update(fatherGoodsEntity);

                    Integer nxDgDfgGoodsGrandId = delNxGoods.getNxDgDfgGoodsGrandId();
                    NxDistributerFatherGoodsEntity grandGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsGrandId);
                    grandGoodsEntity.setNxDfgNxGoodsId(toGoods.getNxGoodsGrandId());
                    nxDistributerFatherGoodsService.update(grandGoodsEntity);


                    Integer nxDfgGreatId = grandGoodsEntity.getNxDfgFathersFatherId();

                    NxDistributerFatherGoodsEntity greatGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDfgGreatId);
                    grandGoodsEntity.setNxDfgNxGoodsId(toGoods.getNxGoodsGreatGrandId());
                    nxDistributerFatherGoodsService.update(greatGoodsEntity);


                    Map<String, Object> mapSearch = new HashMap<>();
                    mapSearch.put("disGoodsId", delNxGoods.getNxDistributerGoodsId());
                    System.out.println("mapseredisGoodsId" + mapSearch);
                    List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapSearch);
                    if (ordersEntities.size() > 0) {
                        for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                            ordersEntity.setNxDoNxGoodsId(toGoods.getNxGoodsId());
                            ordersEntity.setNxDoNxGoodsFatherId(toGoods.getNxGoodsFatherId());
                            ordersEntity.setNxDoDepDisGoodsId(-1);
                            depOrdersService.update(ordersEntity);
                        }
                    }

                    //depGoods
                    System.out.println("drpGHooddsossooss" + mapSearch);
                    List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapSearch);
                    if (departmentDisGoodsEntities.size() > 0) {
                        for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {

                            depGoodsEntity.setNxDdgDepGoodsName(toGoods.getNxGoodsName());
                            depGoodsEntity.setNxDdgDepGoodsStandardname(toGoods.getNxGoodsStandardname());
                            nxDepartmentDisGoodsService.update(depGoodsEntity);
                        }
                    }

                }
            }

        }

        NxGoodsEntity delGoodsEntity = nxGoodsService.queryObject(deleteId);
        Integer nxGoodsFatherId = delGoodsEntity.getNxGoodsFatherId();
        int total = nxGoodsService.queryTotalByFatherId(nxGoodsFatherId);

        if (delGoodsEntity.getNxGoodsIsOldestSon() == 1) {
            if (total > 1) {
                return R.error(-1, "不能删除根商品");
            }
        }

        if (total == 1) {
            NxGoodsEntity father = nxGoodsService.queryObject(nxGoodsFatherId);
            nxGoodsService.delete(father.getNxGoodsId());
        }

        nxGoodsService.delete(deleteId);

        return R.ok();
    }




}
