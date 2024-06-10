package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.UploadFile;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;
import sun.tools.jconsole.JConsole;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.formatFullTime;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeMendian;
import static com.nongxinle.utils.PinYin4jUtils.*;


@RestController
@RequestMapping("api/nxgoods")
public class NxGoodsController {
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;
    @Autowired
    private NxStandardService standardService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDistributerFatherGoodsService nxDFatherGoodsService;
    @Autowired
    private NxDistributerStandardService nxDsService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerFatherGoodsService gbDisFatherGoodsService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private GbDistributerStandardService dsService;
    @Autowired
    private GbDepartmentOrdersService depOrdersService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepGoodsStockService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepDisGoodsService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;;


    @RequestMapping(value = "/updateNxGoodsSort", method = RequestMethod.POST)
    @ResponseBody
    public R updateNxGoodsSort(@RequestBody List<NxGoodsEntity> fatherGoodsEntityList) {
        for (NxGoodsEntity fatherGoods : fatherGoodsEntityList) {
            nxGoodsService.update(fatherGoods);

            //nxDisFather
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDFatherGoodsService.queryDisFathersGoodsByNxGoodsId(fatherGoods.getNxGoodsId());
            if(fatherGoodsEntities.size() > 0){
                for(NxDistributerFatherGoodsEntity distributerFatherGoodsEntity: fatherGoodsEntities){
                    distributerFatherGoodsEntity.setNxDfgFatherGoodsSort(fatherGoods.getNxGoodsSort());
                    nxDFatherGoodsService.update(distributerFatherGoodsEntity);
                }
            }


            List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.querySubNameByFatherId(fatherGoods.getNxGoodsId());
            if(nxGoodsEntities.size() > 0){
                for(NxGoodsEntity goodsEntity: nxGoodsEntities){
                    goodsEntity.setNxGoodsSort(fatherGoods.getNxGoodsSort());
                    nxGoodsService.update(goodsEntity);
                    List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = nxDistributerGoodsService.querydisGoodsByNxGoodsId(goodsEntity.getNxGoodsId());
                    if(nxDistributerGoodsEntities.size() > 0){
                        for(NxDistributerGoodsEntity distributerGoodsEntity: nxDistributerGoodsEntities){
                            distributerGoodsEntity.setNxDgGoodsSort(goodsEntity.getNxGoodsSort());
                            nxDistributerGoodsService.update(distributerGoodsEntity);
                        }
                    }

                }
            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/abcdef")
    @ResponseBody
    public R abcde() {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", 2);
        map.put("nxGreatGrandFatherId", 1);
        List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = nxDistributerGoodsService.queryDisGoodsByParams(map);

        if(nxDistributerGoodsEntities.size() > 0){
            System.out.println("dnensize" + nxDistributerGoodsEntities.size());
            for(NxDistributerGoodsEntity distributerGoodsEntity: nxDistributerGoodsEntities){
                Integer nxDgNxGoodsId = distributerGoodsEntity.getNxDgNxGoodsId();
                Map<String, Object> mapO = new HashMap<>();
                mapO.put("nxGoodsId", nxDgNxGoodsId);
                mapO.put("arriveDate", formatWhatDay(-4));
                mapO.put("depId", 1);
                System.out.println("map0" + mapO);
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(mapO);
                String price = "";
                if(ordersEntities.size() >  0){
                    price = ordersEntities.get(0).getNxDoPrice();

                }else {
                    mapO.put("depId", 2);
                    List<NxDepartmentOrdersEntity> ordersEntities2 = nxDepartmentOrdersService.queryDisOrdersByParams(mapO);
                    if(ordersEntities2.size() > 0){
                        price = ordersEntities2.get(0).getNxDoPrice();
                    }else{
                        mapO.put("depId", 3);
                        List<NxDepartmentOrdersEntity> ordersEntities3 = nxDepartmentOrdersService.queryDisOrdersByParams(mapO);
                        if(ordersEntities3.size() > 0){
                            price = ordersEntities3.get(0).getNxDoPrice();
                        } else{
                            mapO.put("depId", 4);
                            List<NxDepartmentOrdersEntity> ordersEntities4 = nxDepartmentOrdersService.queryDisOrdersByParams(mapO);
                            if(ordersEntities4.size() > 0){
                                price = ordersEntities4.get(0).getNxDoPrice();
                            }
                        }
                    }
                }
                if(price.length() > 0){
                    distributerGoodsEntity.setNxDgPriceThirdDay(price);
                }
//                else{
//                    distributerGoodsEntity.setNxDgPriceFirstDay("0.1");
//                }
                nxDistributerGoodsService.update(distributerGoodsEntity);


            }
        }

        return R.ok();
    }


    @RequestMapping(value = "/getIbookFathers")
    @ResponseBody
    public R getIbookFathers() {
        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryNxFatherGoods();
        return R.ok().put("data", nxGoodsEntities);
    }


    @RequestMapping(value = "/disGetApplyGoods/{disId}")
    @ResponseBody
    public R disGetApplyGoods(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        List<NxGoodsEntity> goodsEntities = nxGoodsService.queryNxGoodsByParams(map);

        return R.ok().put("data", goodsEntities);
    }


    /**
     * ADMIN
     * 获取nxGoods树
     *
     * @return nxGoods大类
     */
    @RequestMapping(value = "/adminGetGoodsTree")
    @ResponseBody
    public R adminGetGoodsTree() {
        List<NxGoodsEntity> entities = nxGoodsService.queryGoodsTree();

        return R.ok().put("data", entities);
    }

    @RequestMapping(value = "/adminGetNxGoodsByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R adminGetNxGoodsByFatherId(Integer limit, Integer page, Integer fatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("fatherId", fatherId);

        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryNxGoodsByParams(map);
        int total = nxGoodsService.queryTotalByFatherId(fatherId);
        PageUtils pageUtil = new PageUtils(nxGoodsEntities, total, limit, page);

        return R.ok().put("page", pageUtil);
    }

    /**
     * 删除nxGoods
     *
     * @param goodsId goodsId
     * @return ok
     */
    @RequestMapping(value = "/deleteGoods/{goodsId}")
    @ResponseBody
    public R deleteGoods(@PathVariable Integer goodsId) {


        nxGoodsService.delete(goodsId);

        return R.ok();

    }


    /**
     * 删除nxGoods
     *
     * @param goodsId goodsId
     * @return ok
     */
    @RequestMapping(value = "/deleteGoodsWithFather/{goodsId}")
    @ResponseBody
    public R deleteGoodsWithFather(@PathVariable Integer goodsId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("nxGoodsId", goodsId);
        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map4);
        if(integer == 0){
            NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(goodsId);
            Integer nxGoodsFatherId = nxGoodsEntity.getNxGoodsFatherId();
            if(nxGoodsEntity.getNxGoodsIsOldestSon() == 1){
                int total = nxGoodsService.queryTotalByFatherId(nxGoodsFatherId);
                if(total > 1){
                    return R.error(-1,"不能删除根商品");
                }else{
                    NxGoodsEntity father = nxGoodsService.queryObject(nxGoodsFatherId);
                    nxGoodsService.delete(father.getNxGoodsId());
                }
            }

            //nxDisGoods
//            List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = nxDistributerGoodsService.querydisGoodsByNxGoodsId(goodsId);
//            if (nxDistributerGoodsEntities.size() > 0) {
//                for (NxDistributerGoodsEntity nxDistributerGoodsEntity : nxDistributerGoodsEntities) {
//                    Integer nxDgDistributerId = nxDistributerGoodsEntity.getNxDgDistributerId();
//                    Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
//                    Integer distributerGoodsId = nxDistributerGoodsEntity.getNxDistributerGoodsId();
//                    canclePostDgnGoods(distributerGoodsId, nxDgDfgGoodsFatherId, nxDgDistributerId);
//                }
//            }

            nxGoodsService.delete(goodsId);

            }else {
                return R.error(-1,"有订单");

            }


            //gbDisGoods
//            List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDistributerGoodsService.querydisGoodsByNxGoodsId(goodsId);
//            if (distributerGoodsEntities.size() > 0) {
//                for (GbDistributerGoodsEntity gbDistributerGoodsEntity : distributerGoodsEntities) {
//                    Integer gbDgDistributerId = gbDistributerGoodsEntity.getGbDgDistributerId();
//                    Integer gbDgDfgGoodsFatherId = gbDistributerGoodsEntity.getGbDgDfgGoodsFatherId();
//                    Integer gbDistributerGoodsId = gbDistributerGoodsEntity.getGbDistributerGoodsId();
//                     canclePostDgnGoodsGb(gbDistributerGoodsId, gbDgDfgGoodsFatherId, gbDgDistributerId);
//                }
//            }



            return R.ok();

    }


    public void canclePostDgnGoods(Integer disGoodsId, Integer disGoodsFatherId, Integer disId) {
        //判断此商品下是否有客户


            Map<String, Object> map1 = new HashMap<>();
            map1.put("disId", disId);
            map1.put("dgFatherId", disGoodsFatherId);
            //搜索fatherId下有几个disGoods
            List<NxDistributerGoodsEntity> totalDisGoods = nxDistributerGoodsService.queryDisGoodsByParams(map1);
            //如果disGoods的父类只有一个商品
            if (totalDisGoods.size() == 1) {
                //父类Entity
                NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = nxDFatherGoodsService.queryObject(disGoodsFatherId);
                //disGoods的grandId
                Integer grandId = nxDistributerFatherGoodsEntity.getNxDfgFathersFatherId();
                Map<String, Object> mapGrand = new HashMap<>();
                mapGrand.put("fathersFatherId", grandId);
                mapGrand.put("disId", disId);
                //搜索grand有几个兄弟
                List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDFatherGoodsService.queryDisFathersGoodsByParams(mapGrand);
                if (fatherGoodsEntities.size() == 1) {
                    Integer nxDfgFathersFatherId = fatherGoodsEntities.get(0).getNxDfgFathersFatherId();
                    NxDistributerFatherGoodsEntity grandEntity = nxDFatherGoodsService.queryObject(nxDfgFathersFatherId);
                    Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("disId", disId);
                    map.put("fathersFatherId", greatGrandId);
                    List<NxDistributerFatherGoodsEntity> grandGoodsEntities = nxDFatherGoodsService.queryDisFathersGoodsByParams(map);

                    //如果grandFather也是只有一个，则删除greatGrandFather
                    if (grandGoodsEntities.size() == 1) {
                        nxDFatherGoodsService.delete(greatGrandId);
                    }
                    nxDFatherGoodsService.delete(grandId);
                }
                nxDFatherGoodsService.delete(disGoodsFatherId);
            } else {
                //父类商品数量减去1
                NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = nxDFatherGoodsService.queryObject(disGoodsFatherId);
                Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
                nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount - 1);
                nxDFatherGoodsService.update(nxDistributerFatherGoodsEntity);
            }

            //删除订货单位
            List<NxDistributerStandardEntity> standardEntities = nxDsService.queryDisStandardByDisGoodsId(disGoodsId);
            if (standardEntities.size() > 0) {
                for (NxDistributerStandardEntity disStandard : standardEntities) {
                    dsService.delete(disStandard.getNxDistributerStandardId());
                }
            }

             nxDistributerGoodsService.delete(disGoodsId);

    }


    public int canclePostDgnGoodsGb(Integer disGoodsId, Integer disGoodsFatherId, Integer disId) {
        Map<String, Object> map5 = new HashMap<>();
        map5.put("disGoodsId", disGoodsId);
        List<GbDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map5);

        List<GbDepartmentGoodsStockEntity> gbDepGoodsStockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map5);

        Map<String, Object> mapDisGoods = new HashMap<>();
        mapDisGoods.put("disGoodsId", disGoodsId);
        mapDisGoods.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(mapDisGoods);

        if (ordersEntities.size() > 0 || gbDepGoodsStockEntities.size() > 0 || departmentDisGoodsEntities.size() > 0) {
            return 1;
        } else {

            GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = gbDisFatherGoodsService.queryObject(disGoodsFatherId);
            Integer gbDfgGoodsAmount = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
            gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount - 1);
            gbDisFatherGoodsService.update(gbDistributerFatherGoodsEntity);


            Map<String, Object> map1 = new HashMap<>();
            map1.put("disId", disId);
            map1.put("dgFatherId", disGoodsFatherId);
            //搜索fatherId下有几个disGoods
            List<GbDistributerGoodsEntity> totalDisGoods = gbDistributerGoodsService.queryDisGoodsByParams(map1);
            //如果disGoods的父类只有一个商品
            if (totalDisGoods.size() == 1) {
                //父类Entity
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity0 = gbDisFatherGoodsService.queryObject(disGoodsFatherId);
                //disGoods的grandId
                Integer grandId = gbDistributerFatherGoodsEntity0.getGbDfgFathersFatherId();
                Map<String, Object> mapGrand = new HashMap<>();
                mapGrand.put("fathersFatherId", grandId);
                //搜索grand有几个兄弟
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDisFatherGoodsService.queryDisFathersGoodsByParamsGb(mapGrand);
                if (fatherGoodsEntities.size() == 1) {
                    Integer gbDfgFathersFatherId = fatherGoodsEntities.get(0).getGbDfgFathersFatherId();
                    GbDistributerFatherGoodsEntity grandEntity = gbDisFatherGoodsService.queryObject(gbDfgFathersFatherId);
                    Integer greatGrandId = grandEntity.getGbDfgFathersFatherId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("fathersFatherId", greatGrandId);
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = gbDisFatherGoodsService.queryDisFathersGoodsByParamsGb(map);

                    //如果grandFather也是只有一个，则删除greatGrandFather
                    if (grandGoodsEntities.size() == 1) {
                        gbDisFatherGoodsService.delete(greatGrandId);
                    }
                    gbDisFatherGoodsService.delete(grandId);
                }
                gbDisFatherGoodsService.delete(disGoodsFatherId);
            } else {
                //父类商品数量减去1
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity1 = gbDisFatherGoodsService.queryObject(disGoodsFatherId);
                Integer gbDfgGoodsAmount1 = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
                gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount1 - 1);
                gbDisFatherGoodsService.update(gbDistributerFatherGoodsEntity1);
            }

            //删除订货单位
            List<GbDistributerStandardEntity> standardEntities = dsService.queryDisStandardByDisGoodsIdGb(disGoodsId);
            if (standardEntities.size() > 0) {
                for (GbDistributerStandardEntity disStandard : standardEntities) {
                    gbDisFatherGoodsService.delete(disStandard.getGbDistributerStandardId());
                }
            }
            int i = gbDistributerGoodsService.delete(disGoodsId);
            if (i == 1) {
                return 0;
            } else {
                return 1;
            }

        }

    }

    @RequestMapping(value = "/delteNxFather/{id}")
    @ResponseBody
    public R delteNxFather(@PathVariable Integer id) {
        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.querySubNameByFatherId(id);
        if(nxGoodsEntities.size() > 0){
            return R.error(-1,"有子商品");
        }else{
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDFatherGoodsService.queryDisFathersGoodsByNxGoodsId(id);
            if(fatherGoodsEntities.size() > 0){
                for(NxDistributerFatherGoodsEntity fatherGoodsEntity: fatherGoodsEntities){
                    nxDFatherGoodsService.delete(fatherGoodsEntity.getNxDistributerFatherGoodsId());
                }
            }
            nxGoodsService.delete(id);
            return R.ok(); 
        }

    }


    /**
     * 查询nxGoods
     *
     * @param nxGoodsId id
     * @return nxGoods
     */
    @RequestMapping(value = "/getNxGoodsInfo/{nxGoodsId}")
    @ResponseBody
    public R getNxGoodsInfo(@PathVariable Integer nxGoodsId) {
        return R.ok().put("data", nxGoodsService.queryObject(nxGoodsId));
    }

    /**
     * 更新商品
     *
     * @param goods nxGoods
     * @return ok
     */
    @RequestMapping(value = "/editNxGoods", method = RequestMethod.POST)
    @ResponseBody
    public R editNxGoods(@RequestBody NxGoodsEntity goods) {

        String goodsName = goods.getNxGoodsName();
        String englishKuohao = getEnglishKuohao(goodsName);

        String nxGoodsDetail = goods.getNxGoodsDetail();
        String nxGoodsBrand = goods.getNxGoodsBrand();
        Integer nxGoodsFatherId = goods.getNxGoodsFatherId();
        String standard = goods.getNxGoodsStandardname();
        String standardWeight = goods.getNxGoodsStandardWeight();
        String place = goods.getNxGoodsPlace();

        Map<String, Object> map = new HashMap<>();
        map.put("goodsName", englishKuohao);
        map.put("goodsDetail", nxGoodsDetail);
        map.put("goodsBrand", nxGoodsBrand);
        map.put("fatherId", nxGoodsFatherId);
        map.put("goodsStandard", standard);
        map.put("standardWeight", standardWeight);
        map.put("place", place);
        map.put("sort", goods.getNxGoodsSort());
        System.out.println("smamdmdmmddmmdm" + map);
        List<NxGoodsEntity> goodsEntities = nxGoodsService.queryIfHasSameGoods(map);
        if (goodsEntities.size() > 0) {
            return R.error(-1, "已有相同商品");
        } else {
            String pinyin = hanziToPinyin(goodsName);
            String headPinyin = getHeadStringByString(goodsName, false, null);
            goods.setNxGoodsName(englishKuohao);
            goods.setNxGoodsPinyin(pinyin);
            goods.setNxGoodsPy(headPinyin);
            nxGoodsService.update(goods);
            if (goods.getNxGoodsIsOldestSon() == 1) {
                NxGoodsEntity fatherGoodsEntity = nxGoodsService.queryObject(nxGoodsFatherId);
                fatherGoodsEntity.setNxGoodsName(englishKuohao);
                fatherGoodsEntity.setNxGoodsPinyin(pinyin);
                fatherGoodsEntity.setNxGoodsPy(headPinyin);
                nxGoodsService.update(fatherGoodsEntity);

                List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByNxGoodsId(nxGoodsFatherId);
                if(fatherGoodsEntities.size() > 0){
                    for(NxDistributerFatherGoodsEntity fatherGoodsEntity1: fatherGoodsEntities){
                        fatherGoodsEntity1.setNxDfgFatherGoodsName(englishKuohao);
                        nxDistributerFatherGoodsService.update(fatherGoodsEntity1);
                    }
                }
            }

            // update NxDisGoods
            List<NxDistributerGoodsEntity> distributerGoodsEntities = nxDistributerGoodsService.querydisGoodsByNxGoodsId(goods.getNxGoodsId());
            if (distributerGoodsEntities.size() > 0) {
                for (NxDistributerGoodsEntity disGoods : distributerGoodsEntities) {
                    disGoods.setNxDgGoodsName(goodsName);
                    disGoods.setNxDgGoodsPinyin(pinyin);
                    disGoods.setNxDgGoodsPy(headPinyin);
                    disGoods.setNxDgGoodsStandardname(goods.getNxGoodsStandardname());
                    disGoods.setNxDgGoodsStandardWeight(goods.getNxGoodsStandardWeight());
                    disGoods.setNxDgGoodsDetail(goods.getNxGoodsDetail());
                    disGoods.setNxDgGoodsPlace(goods.getNxGoodsPlace());
                    disGoods.setNxDgGoodsBrand(goods.getNxGoodsBrand());
                    disGoods.setNxDgGoodsSort(goods.getNxGoodsSort());
                    disGoods.setNxDgGoodsSonsSort(goods.getNxGoodsSonsSort());
                    disGoods.setNxDgGoodsIsHidden(goods.getNxGoodsIsHidden());
                    nxDistributerGoodsService.update(disGoods);
                    Map<String, Object> mapDG = new HashMap<>();
                    mapDG.put("disGoodsId", disGoods.getNxDistributerGoodsId());
                    List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapDG);
                    if(departmentDisGoodsEntities.size() > 0){
                        for(NxDepartmentDisGoodsEntity departmentDisGoodsEntity: departmentDisGoodsEntities){
                            departmentDisGoodsEntity.setNxDdgDepGoodsName(goodsName);
                            departmentDisGoodsEntity.setNxDdgDepGoodsPinyin(pinyin);
                            departmentDisGoodsEntity.setNxDdgDepGoodsPy(headPinyin);
                            departmentDisGoodsEntity.setNxDdgDepGoodsStandardname(goods.getNxGoodsStandardname());
                            departmentDisGoodsEntity.setNxDdgDepGoodsDetail(goods.getNxGoodsDetail());
                            departmentDisGoodsEntity.setNxDdgDepGoodsBrand(goods.getNxGoodsBrand());
                            departmentDisGoodsEntity.setNxDdgDepGoodsPlace(goods.getNxGoodsPlace());
                            nxDepartmentDisGoodsService.update(departmentDisGoodsEntity);
                        }
                    }
                }
            }

            //update GbDisGoods
            List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDistributerGoodsService.querydisGoodsByNxGoodsId(goods.getNxGoodsId());
            if (gbDistributerGoodsEntities.size() > 0) {
                for (GbDistributerGoodsEntity gbDistributerGoodsEntity : gbDistributerGoodsEntities) {
                    gbDistributerGoodsEntity.setGbDgGoodsName(englishKuohao);
                    gbDistributerGoodsEntity.setGbDgGoodsPinyin(pinyin);
                    gbDistributerGoodsEntity.setGbDgGoodsPy(headPinyin);
                    gbDistributerGoodsEntity.setGbDgGoodsStandardname(goods.getNxGoodsStandardname());
                    gbDistributerGoodsEntity.setGbDgGoodsStandardWeight(goods.getNxGoodsStandardWeight());
                    gbDistributerGoodsEntity.setGbDgGoodsDetail(goods.getNxGoodsDetail());
                    gbDistributerGoodsEntity.setGbDgGoodsPlace(goods.getNxGoodsPlace());
                    gbDistributerGoodsEntity.setGbDgGoodsBrand(goods.getNxGoodsBrand());
                    gbDistributerGoodsEntity.setGbDgGoodsSort(goods.getNxGoodsSort());
                    gbDistributerGoodsEntity.setGbDgGoodsSonsSort(goods.getNxGoodsSonsSort());
                    gbDistributerGoodsEntity.setGbDgGoodsIsHidden(goods.getNxGoodsIsHidden());
                    gbDistributerGoodsService.update(gbDistributerGoodsEntity);

                    Map<String, Object> mapDepG = new HashMap<>();
                    mapDepG.put("disGoodsId", gbDistributerGoodsEntity.getGbDistributerGoodsId());
                    List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(mapDepG);
                    if(departmentDisGoodsEntities.size() > 0){
                        for(GbDepartmentDisGoodsEntity departmentDisGoodsEntity: departmentDisGoodsEntities){
                            departmentDisGoodsEntity.setGbDdgDepGoodsName(englishKuohao);
                            departmentDisGoodsEntity.setGbDdgDepGoodsPinyin(pinyin);
                            departmentDisGoodsEntity.setGbDdgDepGoodsPy(headPinyin);
                            departmentDisGoodsEntity.setGbDdgShowStandardName(goods.getNxGoodsStandardname());
                            departmentDisGoodsEntity.setGbDdgShowStandardWeight(goods.getNxGoodsStandardWeight());
                            departmentDisGoodsEntity.setGbDdgDepGoodsDetail(goods.getNxGoodsDetail());
                            departmentDisGoodsEntity.setGbDdgDepGoodsPlace(goods.getNxGoodsPlace());
                            departmentDisGoodsEntity.setGbDdgDepGoodsBrand(goods.getNxGoodsBrand());
                            gbDepDisGoodsService.update(departmentDisGoodsEntity);
                        }
                    }
                }
            }
            return R.ok();
        }
    }




    /**
     * 保存商品
     *
     * @param goods nxGoods
     * @return ok
     */
    @RequestMapping(value = "/saveNxGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveNxGoods(@RequestBody NxGoodsEntity goods) {

        String goodsName = goods.getNxGoodsName();
        String nxGoodsDetail = goods.getNxGoodsDetail();
        String nxGoodsBrand = goods.getNxGoodsBrand();
        Integer nxGoodsFatherId = goods.getNxGoodsFatherId();
        String standard = goods.getNxGoodsStandardname();
        String standardWeight = goods.getNxGoodsStandardWeight();
        String englishKuohao = getEnglishKuohao(goodsName);
        String pinyin = hanziToPinyin(englishKuohao);

        Map<String, Object> map = new HashMap<>();
        map.put("goodsPinyin", pinyin);
        map.put("goodsDetail", nxGoodsDetail);
        map.put("goodsBrand", nxGoodsBrand);
        map.put("fatherId", nxGoodsFatherId);
        map.put("goodsStandard", standard);
        map.put("standardWeight", standardWeight);
        map.put("level", 3);
        List<NxGoodsEntity> goodsEntities = nxGoodsService.queryIfHasSameGoods(map);

        if (goodsEntities.size() > 0) {
            Map<String, Object> mapR = new HashMap<>();
            mapR.put("haveSame", 3);
            mapR.put("arr", goodsEntities);
            return R.ok().put("data", mapR);
        } else {

            String headPinyin = getHeadStringByString(englishKuohao, false, null);
            goods.setNxGoodsName(englishKuohao);
            goods.setNxGoodsPinyin(pinyin);
            goods.setNxGoodsPy(headPinyin);
            goods.setNxGoodsLevel(3);
            goods.setNxGoodsFile("goodsImage/logo.jpg");
            nxGoodsService.save(goods);

            //addDisGoodsForDistributer
//            List<NxDistributerEntity> distributerEntities = nxDistributerService.queryAllTypeOne();
//            if (distributerEntities.size() > 0) {
//                for (NxDistributerEntity distributerEntity : distributerEntities) {
//                    Integer nxDistributerId = distributerEntity.getNxDistributerId();
//
//                    //disGoods
//                    NxDistributerGoodsEntity addNxGoods = new NxDistributerGoodsEntity();
//                    addNxGoods.setNxDgDistributerId(nxDistributerId);
//                    addNxGoods.setNxDgNxGoodsId(goods.getNxGoodsId());
//                    addNxGoods.setNxDgNxFatherId(goods.getNxGoodsFatherId());
//                    addNxGoods.setNxDgNxGrandId(goods.getNxGoodsGrandId());
//                    addNxGoods.setNxDgGoodsFile(goods.getNxGoodsFile());
//                    addNxGoods.setNxDgNxFatherImg(goods.getNxGoodsFile());
//                    addNxGoods.setNxDgBuyingPrice("0.1");
//                    addNxGoods.setNxDgWillPrice("0.1");
//                    addNxGoods.setNxDgPriceFirstDay("0.1");
//                    addNxGoods.setNxDgPriceSecondDay("0.1");
//                    addNxGoods.setNxDgPriceThirdDay("0.1");
//                    addNxGoods.setNxDgBuyingPriceIsGrade(0);
//                    addNxGoods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
//                    addNxGoods.setNxDgNxFatherImg(goods.getNxGoodsFile());
//                    System.out.println("faididi" + addNxGoods.getNxDgNxFatherId());
//                    saveDisGoods(addNxGoods);
//                }
//
//            }
            Map<String, Object> mapR = new HashMap<>();
            mapR.put("haveSame", -1);
            mapR.put("arr", nxGoodsService.queryObject(goods.getNxGoodsId()));
            return R.ok().put("data", mapR);
        }
    }


    /**
     * 保存商品
     *
     * @param goods nxGoods
     * @return ok
     */
    @RequestMapping(value = "/saveNxGoodsWithFather", method = RequestMethod.POST)
    @ResponseBody
    public R saveNxGoodsWithFather(@RequestBody NxGoodsEntity goods) {

        String goodsName = goods.getNxGoodsName();
        String nxGoodsBrand = goods.getNxGoodsBrand();
        Integer nxGoodsFatherId = goods.getNxGoodsFatherId();
        String standard = goods.getNxGoodsStandardname();
        String standardWeight = goods.getNxGoodsStandardWeight();
        String nxGoodsPlace = goods.getNxGoodsPlace();
        String englishKuohao = getEnglishKuohao(goodsName);
        String pinyin = hanziToPinyin(englishKuohao);

        Map<String, Object> map = new HashMap<>();
        map.put("goodsPinyin", pinyin);
        map.put("goodsBrand", nxGoodsBrand);
        map.put("goodsPlace", nxGoodsPlace);
        map.put("goodsStandard", standard);
        map.put("standardWeight", standardWeight);
        map.put("fatherId", nxGoodsFatherId);
        map.put("level", 3);
        System.out.println("222mapa" + map);
        List<NxGoodsEntity> goodsEntities = nxGoodsService.queryIfHasSameGoods(map);
        if (goodsEntities.size() > 0) {
            Map<String, Object> mapR = new HashMap<>();
            mapR.put("level", 3);
            mapR.put("haveSame", 3);
            mapR.put("arr", goodsEntities);
            return R.ok().put("data", mapR);
        } else {
            Map<String, Object> mapNameFther = new HashMap<>();
            mapNameFther.put("goodsPinyin", pinyin);
            mapNameFther.put("fatherId", nxGoodsFatherId);
            mapNameFther.put("level", 2);
            List<NxGoodsEntity> goodsEntitiesNameFther = nxGoodsService.queryIfHasSameGoods(mapNameFther);
            if (goodsEntitiesNameFther.size() > 0) {
                Map<String, Object> mapR = new HashMap<>();
                mapR.put("haveSame", 2);
                mapR.put("arr", goodsEntitiesNameFther);
                return R.ok().put("data", mapR);
            } else {
                Map<String, Object> mapName = new HashMap<>();
                mapName.put("goodsPinyin", pinyin);
                mapName.put("notEqualFatherId", nxGoodsFatherId);
                mapName.put("level", 2);
                List<NxGoodsEntity> goodsEntitiesName = nxGoodsService.queryIfHasSameGoods(mapName);
                if (goodsEntitiesName.size() > 0) {
                    Map<String, Object> mapR = new HashMap<>();
                    mapR.put("haveSame", 1);
                    mapR.put("arr", goodsEntitiesName);
                    return R.ok().put("data", mapR);
                } else {
                    NxGoodsEntity fatherGoods = new NxGoodsEntity();
                    fatherGoods.setNxGoodsName(goodsName);
                    fatherGoods.setNxGoodsFatherId(goods.getNxGoodsGrandId());
                    fatherGoods.setNxGoodsSort(goods.getNxGoodsSort());
                    fatherGoods.setNxGoodsFile("goodsImage/logo.jpg");
                    fatherGoods.setNxGoodsLevel(2);
                    int i = nxGoodsService.querySecondLevelMaxId();
                    fatherGoods.setNxGoodsId(i + 1);
                    nxGoodsService.save(fatherGoods);


                    String headPinyin = getHeadStringByString(englishKuohao, false, null);
                    goods.setNxGoodsName(englishKuohao);
                    goods.setNxGoodsPinyin(pinyin);
                    goods.setNxGoodsPy(headPinyin);
                    goods.setNxGoodsLevel(3);
                    goods.setNxGoodsFile("goodsImage/logo.jpg");
                    goods.setNxGoodsFatherId(fatherGoods.getNxGoodsId());
                    goods.setNxGoodsIsOldestSon(1);
                    nxGoodsService.save(goods);

                    //addDisGoodsForDistributer
//                    List<NxDistributerEntity> distributerEntities = nxDistributerService.queryAllTypeOne();
//                    if (distributerEntities.size() > 0) {
//                        for (NxDistributerEntity distributerEntity : distributerEntities) {
//                            Integer nxDistributerId = distributerEntity.getNxDistributerId();
//
//                            //disGoods
//                            NxDistributerGoodsEntity addNxGoods = new NxDistributerGoodsEntity();
//                            addNxGoods.setNxDgDistributerId(nxDistributerId);
//                            addNxGoods.setNxDgNxGoodsId(goods.getNxGoodsId());
//                            addNxGoods.setNxDgNxFatherId(goods.getNxGoodsFatherId());
//                            addNxGoods.setNxDgNxGrandId(goods.getNxGoodsGrandId());
//                            addNxGoods.setNxDgGoodsFile(goods.getNxGoodsFile());
//                            addNxGoods.setNxDgNxFatherImg(goods.getNxGoodsFile());
//                            addNxGoods.setNxDgBuyingPrice("0.1");
//                            addNxGoods.setNxDgWillPrice("0.1");
//                            addNxGoods.setNxDgPriceFirstDay("0.1");
//                            addNxGoods.setNxDgPriceSecondDay("0.1");
//                            addNxGoods.setNxDgPriceThirdDay("0.1");
//                            addNxGoods.setNxDgBuyingPriceIsGrade(0);
//                            addNxGoods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
//                            addNxGoods.setNxDgNxFatherImg(goods.getNxGoodsFile());
//                            addNxGoods.setNxDgGoodsIsHidden(0);
//                            saveDisGoods(addNxGoods);
//                        }
//                    }
                    Map<String, Object> mapR = new HashMap<>();
                    mapR.put("haveSame", 0);
                    mapR.put("arr", nxGoodsService.queryObject(goods.getNxGoodsId()));
                    return R.ok().put("data", mapR);
                }
            }
        }
    }

    private NxDistributerGoodsEntity saveDisGoods(NxDistributerGoodsEntity cgnGoods) {
        Integer nxDgNxGoodsId = cgnGoods.getNxDgNxGoodsId();
        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(nxDgNxGoodsId);
        cgnGoods.setNxDgGoodsName(nxGoodsEntity.getNxGoodsName());
        cgnGoods.setNxDgGoodsStandardname(nxGoodsEntity.getNxGoodsStandardname());
        cgnGoods.setNxDgGoodsDetail(nxGoodsEntity.getNxGoodsDetail());
        cgnGoods.setNxDgGoodsPlace(nxGoodsEntity.getNxGoodsPlace());
        cgnGoods.setNxDgGoodsBrand(nxGoodsEntity.getNxGoodsBrand());
        cgnGoods.setNxDgGoodsStandardWeight(nxGoodsEntity.getNxGoodsStandardWeight());
        cgnGoods.setNxDgGoodsPinyin(nxGoodsEntity.getNxGoodsPinyin());
        cgnGoods.setNxDgGoodsPy(nxGoodsEntity.getNxGoodsPy());
        cgnGoods.setNxDgPullOff(0);
        cgnGoods.setNxDgGoodsStatus(0);
        cgnGoods.setNxStandardEntities(nxGoodsEntity.getNxGoodsStandardEntities());
        cgnGoods.setNxDistributerAliasEntities(nxGoodsEntity.getNxDistributerAliasEntities());
        cgnGoods.setNxDgNxFatherId(nxGoodsEntity.getNxGoodsFatherId());
        cgnGoods.setNxDgNxFatherName(nxGoodsEntity.getFatherGoods().getNxGoodsName());
        cgnGoods.setNxDgNxFatherImg(nxGoodsEntity.getFatherGoods().getNxGoodsFile());
        cgnGoods.setNxDgNxGrandName(nxGoodsEntity.getGrandGoods().getNxGoodsName());
        cgnGoods.setNxDgNxGrandId(nxGoodsEntity.getGrandGoods().getNxGoodsId());
        cgnGoods.setNxDgGoodsFile(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setNxDgNxFatherImg(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setNxDgGoodsSort(nxGoodsEntity.getNxGoodsSort());
        cgnGoods.setNxDgGoodsSonsSort(nxGoodsEntity.getNxGoodsSonsSort());
        cgnGoods.setNxDgPurchaseAuto(-1);
        cgnGoods.setNxDgIsOldestSon(nxGoodsEntity.getNxGoodsIsOldestSon());

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

            NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsEntities.get(0);

            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            Integer nxDgDfgGoodsFatherId1 = disGoodsEntity.getNxDgDfgGoodsFatherId();
            Integer nxDgDfgGoodsGrandId = disGoodsEntity.getNxDgDfgGoodsGrandId();

            NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = nxDFatherGoodsService.queryObject(nxDgDfgGoodsFatherId1);
            Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
            nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount + 1);
            nxDFatherGoodsService.update(nxDistributerFatherGoodsEntity);

            //2，保存disId商品
            Integer nxDgDfgGoodsFatherId = disGoodsEntity.getNxDgDfgGoodsFatherId();
            cgnGoods.setNxDgDfgGoodsFatherId(nxDgDfgGoodsFatherId);
            cgnGoods.setNxDgDfgGoodsGrandId(nxDgDfgGoodsGrandId);
            cgnGoods.setNxDgGoodsIsHidden(0);

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
            dgf.setNxDfgNxGoodsId(cgnGoods.getNxDgNxFatherId());
            dgf.setNxDfgFatherGoodsSort(nxGoodsEntity.getNxGoodsSort());
            nxDFatherGoodsService.save(dgf);
            //继续查询是否有GrandFather
            String grandName = cgnGoods.getNxDgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", nxDgDistributerId);
            map2.put("fathersFatherName", grandName);
            List<NxDistributerFatherGoodsEntity> grandGoodsFather = nxDFatherGoodsService.queryHasDisFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = grandGoodsFather.get(0);
                dgf.setNxDfgFathersFatherId(nxDistributerFatherGoodsEntity.getNxDistributerFatherGoodsId());
                nxDFatherGoodsService.update(dgf);
                //更新disGoods的fatherGoodsId
                Integer distributerFatherGoodsId = dgf.getNxDistributerFatherGoodsId();
                Integer nxDfgFathersFatherId = dgf.getNxDfgFathersFatherId();
                cgnGoods.setNxDgDfgGoodsFatherId(distributerFatherGoodsId);
                cgnGoods.setNxDgDfgGoodsGrandId(nxDfgFathersFatherId);
                nxDistributerGoodsService.save(cgnGoods);

            } else {
                //tianjiaGrand
                NxDistributerFatherGoodsEntity grand = new NxDistributerFatherGoodsEntity();
                String nxCgGrandFatherName = cgnGoods.getNxDgNxGrandName();
                grand.setNxDfgFatherGoodsName(nxCgGrandFatherName);
                grand.setNxDfgDistributerId(cgnGoods.getNxDgDistributerId());
                grand.setNxDfgFatherGoodsLevel(1);
                grand.setNxDfgFatherGoodsColor(cgnGoods.getNxDgNxGoodsFatherColor());
                grand.setNxDfgNxGoodsId(cgnGoods.getNxDgNxGrandId());
                nxDFatherGoodsService.save(grand);

                dgf.setNxDfgFathersFatherId(grand.getNxDistributerFatherGoodsId());
                nxDFatherGoodsService.update(dgf);
                //更新disGoods的fatherGoodsId
                Integer distributerFatherGoodsId = dgf.getNxDistributerFatherGoodsId();
                Integer nxDfgFathersFatherId = dgf.getNxDfgFathersFatherId();
                cgnGoods.setNxDgDfgGoodsFatherId(distributerFatherGoodsId);
                cgnGoods.setNxDgDfgGoodsGrandId(nxDfgFathersFatherId);
                nxDistributerGoodsService.save(cgnGoods);
                //查询是否有greatGrand
                String greatGrandName = cgnGoods.getNxDgNxGreatGrandName();
                Map<String, Object> map3 = new HashMap<>();
                map3.put("disId", nxDgDistributerId);
                map3.put("fathersFatherName", greatGrandName);
                List<NxDistributerFatherGoodsEntity> greatGrandGoodsFather = nxDFatherGoodsService.queryHasDisFathersFather(map3);
                if (greatGrandGoodsFather.size() > 0) {
                    NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = greatGrandGoodsFather.get(0);
                    Integer disFatherId = nxDistributerFatherGoodsEntity.getNxDistributerFatherGoodsId();
                    grand.setNxDfgFathersFatherId(disFatherId);
                    nxDFatherGoodsService.update(grand);
                } else {
                    NxDistributerFatherGoodsEntity greatGrand = new NxDistributerFatherGoodsEntity();
                    String greatGrandName1 = cgnGoods.getNxDgNxGreatGrandName();
                    greatGrand.setNxDfgFatherGoodsName(greatGrandName1);
                    greatGrand.setNxDfgDistributerId(cgnGoods.getNxDgDistributerId());
                    greatGrand.setNxDfgFatherGoodsLevel(0);
                    greatGrand.setNxDfgFatherGoodsColor(cgnGoods.getNxDgNxGoodsFatherColor());
                    greatGrand.setNxDfgNxGoodsId(cgnGoods.getNxDgNxGreatGrandId());
                    nxDFatherGoodsService.save(greatGrand);
                    grand.setNxDfgFathersFatherId(greatGrand.getNxDistributerFatherGoodsId());
                    nxDFatherGoodsService.update(grand);
                }
            }
        }
        return cgnGoods;
    }


    /**
     * 搜索ibook
     *
     * @param searchStr 搜索词
     * @return 搜索结果
     */
    @RequestMapping(value = "/queryGoodsByQuickSearch/{searchStr}")
    @ResponseBody
    public R queryGoodsByQuickSearch(@PathVariable String searchStr) {
        Map<String, Object> map = new HashMap<>();
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
        List<NxGoodsEntity> goodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);
        return R.ok().put("data", goodsEntities);
    }

    /**
     * 在类别下搜索商品
     *
     * @param fatherId  父级id
     * @param searchStr 搜索词
     * @return 批发商商品
     */
    @RequestMapping(value = "/queryCategoryGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryCategoryGoodsByQuickSearch(Integer fatherId, String searchStr) {
        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);
        map.put("searchStr", searchStr);
        List<NxGoodsEntity> goodsEntities = nxGoodsService.queryQuickSearchNxCategoryGoods(map);
        return R.ok().put("data", goodsEntities);
    }

    @RequestMapping(value = "/queryCategoryGoodsWithNxDisByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryCategoryGoodsWithNxDisByQuickSearch(Integer fatherId, String searchStr,
                                                      Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);
        map.put("searchStr", searchStr);
        map.put("disId", disId);
        System.out.println("mapSearchchdhdhdh" + map);
        List<NxGoodsEntity> goodsEntities = nxGoodsService.queryQuickSearchNxCategoryGoodsWithNxDis(map);
        return R.ok().put("data", goodsEntities);
    }

    @RequestMapping(value = "/queryAllGoodsWithNxDisByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryAllGoodsWithNxDisByQuickSearch(String searchStr, Integer type) {

        List<NxGoodsEntity> goodsEntities = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();

        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
                map.put("searchStr", null);

            }
        }

//        map.put("disId", disId);
        System.out.println("qsdafdfadaa" + map);
        if (type == 0) {
            goodsEntities = nxGoodsService.queryQuickSearchFatherGoods(map);
        }
        if (type == 1) {
            goodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);
        }
        return R.ok().put("data", goodsEntities);
    }

    /**
     * ok
     * 获取ibook书皮数据
     *
     * @return 封皮数据
     */
    @RequestMapping(value = "/getiBookCover")
    @ResponseBody
    public R getiBookCover() {

        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.getiBookCoverData();

        for (NxGoodsEntity goods : nxGoodsEntities) {
            if (goods.getNxGoodsId().equals(1)) {
                goods.setColor("#20afb8");
            }
            if (goods.getNxGoodsId().equals(2)) {
                goods.setColor("#f5c832");
            }
            if (goods.getNxGoodsId().equals(3)) {
                goods.setColor("#3cc36e");
            }
            if (goods.getNxGoodsId().equals(4)) {
                goods.setColor("#1ebaee");
            }
            if (goods.getNxGoodsId().equals(5)) {
                goods.setColor("#f09628");
            }
            if (goods.getNxGoodsId().equals(6)) {
                goods.setColor("#f05a32");
            }
            if (goods.getNxGoodsId().equals(7)) {
                goods.setColor("#20afb8");
            }
            if (goods.getNxGoodsId().equals(8)) {
                goods.setColor("#969696");
            }
        }

        return R.ok().put("data", nxGoodsEntities);
    }


    /**
     * ibook大类列表
     *
     * @param fatherId 父级id
     * @return 商品列表
     */
    @RequestMapping(value = "/getGoodsSubNamesByFatherId/{fatherId}")
    @ResponseBody
    public R getGoodsSubNamesByFatherId(@PathVariable Integer fatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);
        System.out.println("dmafdasfksafaf a" + map);
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


    @RequestMapping(value = "/adminGetTypeGoodsCata/{type}")
    @ResponseBody
    public R adminGetTypeGoodsCata(@PathVariable Integer type) {
        List<NxGoodsEntity> goodsEntities = nxGoodsService.queryGoodsCataByType(type);
        return R.ok().put("data", goodsEntities);
    }


    @RequestMapping(value = "/getAddCommunityGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getAddCommunityGoods(Integer limit, Integer page, Integer fatherId, Integer communityId) {
        System.out.println("hen");
        Map<String, Object> map1 = new HashMap<>();
        map1.put("offset", (page - 1) * limit);
        map1.put("limit", limit);
        map1.put("fatherId", fatherId);
        List<NxGoodsEntity> nxGoodsEntities1 = nxGoodsService.queryNxGoodsByParams(map1);

        List<NxGoodsEntity> goodsEntities = new ArrayList<>();

        for (NxGoodsEntity goods : nxGoodsEntities1) {
            Map<String, Object> map = new HashMap<>();
            map.put("communityId", communityId);
            map.put("goodsId", goods.getNxGoodsId());
            List<NxCommunityGoodsEntity> dgGoods = nxCommunityGoodsService.queryCommunityDownloadGoods(map);
            Integer nxGoodsId = goods.getNxGoodsId();
            List<NxStandardEntity> standardEntities = standardService.queryGoodsStandardListByGoodId(nxGoodsId);
            goods.setNxGoodsStandardEntities(standardEntities);

            if (dgGoods.size() > 0) {
                goods.setIsDownload(1);
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


    /**
     * ok
     * sign获取电子牌商品
     * @param limit 最大
     * @param page 第几页
     * @param fatherId 上级id
     * @return 商品
     * todo 暂停disGoods的概念
     */
//    @RequestMapping(value = "/getIbookGoodsByFatherId", method = RequestMethod.POST)
//    @ResponseBody
//    public R getGoodsByFatherId(Integer limit, Integer page, Integer fatherId, Integer disId) {
//
//        List<NxGoodsEntity> nxGoodsEntities1 = queryByFatherIdwithLimit(limit, page, fatherId);
//
//        List<NxGoodsEntity> goodsEntities = new ArrayList<>();
//
//        for (NxGoodsEntity goods : nxGoodsEntities1) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("disId", disId);
//            map.put("goodsId", goods.getNxGoodsId());
//           List<NxCommunityGoodsEntity> dgGoods = nxCommunityGoodsService.queryDisDownloadGoods(map);
//            Integer nxGoodsId = goods.getNxGoodsId();
//            List<NxStandardEntity> standardEntities =  standardService.queryGoodsStandardListByGoodId(nxGoodsId);
//            goods.setNxGoodsStandardEntities(standardEntities);
//
//            if(dgGoods.size() > 0) {
//               goods.setIsDownload(1);
//               goodsEntities.add(goods);
//           }else {
//               goods.setIsDownload(0);
//               goodsEntities.add(goods);
//           }
//        }
//
//        int total = nxGoodsService.queryTotalByFatherId(fatherId);
//        PageUtils pageUtil = new PageUtils(goodsEntities, total, limit, page);
//
//        return R.ok().put("page", pageUtil);
//
//    }


    /**
     * todo testPage
     *
     * @param fatherId
     * @return
     */
    @RequestMapping(value = "/getiBookFatherGoodsAndInitPage/{fatherId}")
    @ResponseBody
    public R nxGoodsCateList(@PathVariable Integer fatherId) {
        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.getAllFatherGoods(fatherId);

        NxGoodsEntity nxGoodsEntity = nxGoodsEntities.get(0);
        Integer nxGoodsId = nxGoodsEntity.getNxGoodsId();
        List<NxGoodsEntity> nxGoodsEntities1 = queryListByGoodsId(nxGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("fatherList", nxGoodsEntities);
        map.put("initGoods", nxGoodsEntities1);
        return R.ok().put("data", map);
    }


    @RequestMapping(value = "/getFatherGoods/{fatherId}")
    @ResponseBody
    public R getFatherGoods(@PathVariable Integer fatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);

        //查询列表数据
        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryListWithFatherId(map);
        return R.ok().put("data", nxGoodsEntities);

    }

    public List<NxGoodsEntity> queryListByGoodsId(Integer fatherId) {
        Map<String, Object> map = new HashMap<>();
//        map.put("offset", 0);
//        map.put("limit", 20);
        map.put("fatherId", fatherId);

        //查询列表数据
        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryListWithFatherId(map);
        return nxGoodsEntities;

    }

    /**
     * todo testData
     *
     * @return
     */
//    @ResponseBody
//    @RequestMapping(value = "/getCatalogue", method = RequestMethod.POST)
//    public R getCatalogue(Integer page, Integer limit) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("offset", (page - 1) * limit);
//        map.put("limit", limit);
//        List<NxGoodsEntity> nxGoodsEntities1 = nxGoodsService.queryList(map);
//        for (NxGoodsEntity goods : nxGoodsEntities1) {
//            List<NxGoodsEntity> nxGoodsEntityList = goods.getNxGoodsEntityList();
//            for (NxGoodsEntity goods2: nxGoodsEntityList) {
//                String nxGoodsFile = goods2.getNxGoodsFile();
//                System.out.println(nxGoodsFile + "------");
//            }
//        }
//
//
//        return R.ok().put("data", nxGoodsEntities1);
//
//    }
    @RequestMapping("/downloadAllExcel")
    @ResponseBody
    public void downloadAllExcel(HttpServletResponse response, HttpServletRequest request) {

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("fatherId", 0);
            List<NxGoodsEntity> greatGrandEntities = nxGoodsService.queryNxGoodsByParams(map);
            HSSFWorkbook wb = new HSSFWorkbook();
            BigDecimal total = new BigDecimal(0);

            for (NxGoodsEntity grandGoods : greatGrandEntities) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("fatherId", grandGoods.getNxGoodsId());
                List<NxGoodsEntity> grandEntities = nxGoodsService.queryNxGoodsByParams(map1);
                for (NxGoodsEntity fatherGoods : grandEntities) {

                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("fatherId", fatherGoods.getNxGoodsId());
                    List<NxGoodsEntity> fatherEntities = nxGoodsService.queryNxGoodsByParams(map2);

                    for (int m = 0; m < fatherEntities.size(); m++) {
                        Map<String, Object> map3 = new HashMap<>();
                        map3.put("fatherId", fatherEntities.get(m).getNxGoodsId());
                        List<NxGoodsEntity> goodsEntities = nxGoodsService.queryNxGoodsByParams(map3);

                        String nxGoodsName = fatherEntities.get(m).getNxGoodsName();
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


                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            NxGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(i + 2);
                            goodsRow.createCell(0).setCellValue(i + 1);
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getNxGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getNxGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getNxGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getNxGoodsPlace());
                            goodsRow.createCell(5).setCellValue(ckGoodsEntity.getNxGoodsDetail());
                        }
                    }


                }

            }

            String fileName = new String("导出商品.xls".getBytes("utf-8"), "iso8859-1");
            response.setHeader("content-Disposition", "attachment; filename =" + fileName);
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 导出excel
     *
     * @param response
     */
    @RequestMapping("/downloadExcel")
    @ResponseBody
    public void downloadExcel(HttpServletResponse response, HttpServletRequest request) {

        String fatherId = request.getParameter("fatherId");
        System.out.println("fatherIdfatherId" + fatherId);

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("fatherId", fatherId);
            List<NxGoodsEntity> ckGoodsEntities = nxGoodsService.queryNxGoodsByParams(map);
            HSSFWorkbook wb = new HSSFWorkbook();
            NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(Integer.valueOf(fatherId));
            String nxGoodsName = nxGoodsEntity.getNxGoodsName();
            HSSFSheet sheet = wb.createSheet(nxGoodsName);

            //设置表头
            HSSFRow row = sheet.createRow(0);

            row.createCell(0).setCellValue("商品id");
            row.createCell(1).setCellValue("商品名称");
            row.createCell(2).setCellValue("父级id");
            row.createCell(3).setCellValue("规格");
            row.createCell(12).setCellValue("商品排序");

            //设置表体
            HSSFRow goodsRow = null;
            for (int i = 0; i < ckGoodsEntities.size(); i++) {
                NxGoodsEntity ckGoodsEntity = ckGoodsEntities.get(i);
                goodsRow = sheet.createRow(i + 1);
                goodsRow.createCell(0).setCellValue(ckGoodsEntity.getNxGoodsId());
                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getNxGoodsName());
                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getNxGoodsFatherId());
                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getNxGoodsStandardname());
//                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getNxGoodsSort());

            }

            String fileName = new String("导出商品.xls".getBytes("utf-8"), "iso8859-1");
            response.setHeader("content-Disposition", "attachment; filename =" + fileName);
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 下载导入商品的Excel模版
     *
     * @param response no
     * @param session  获取保存文件路径
     */
    @RequestMapping(value = "/downloadExcelTMP", method = RequestMethod.GET)
    @ResponseBody
    public void downloadExcelTMP(HttpServletResponse response, HttpSession session) {

        FileInputStream is = null;

        try {
            String fileName = new String("商品模版.xls".getBytes("utf-8"), "iso8859-1");
            response.setHeader("content-Disposition", "attachment; filename =" + fileName);

            ServletContext servletContext = session.getServletContext();
            String realPath = servletContext.getRealPath("statics/goodsTML.xls");

            is = new FileInputStream(realPath);

            IOUtils.copy(is, response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * ok
     * 导入商品
     *
     * @param file    xls文件
     * @param session http
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/uploadExcel", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R uploadExcel(@RequestParam("file") MultipartFile file,
                         @RequestParam("name") String name,
                         HttpSession session) throws Exception {
        System.out.println("updlsosoosooso" + name);
        System.out.println(file.getName());
        HSSFWorkbook wb = new HSSFWorkbook(file.getInputStream());

        HSSFSheet sheet = null;
        for (int j = 0; j < wb.getNumberOfSheets(); j++) {
            sheet = wb.getSheetAt(j);
            int lastRowNum = sheet.getLastRowNum();
            Row goodsRow = null;
            for (int i = 1; i <= lastRowNum; i++) {
                goodsRow = sheet.getRow(i);
                NxGoodsEntity goods = new NxGoodsEntity();
                String goodsName = (String) getCellValue(goodsRow.getCell(1));
                String pinyin = hanziToPinyin(goodsName);
                String headPinyin = getHeadStringByString(goodsName, false, null);
                goods.setNxGoodsPinyin(pinyin);
                goods.setNxGoodsPy(headPinyin);
                goods.setNxGoodsSort((Integer) getCellValue(goodsRow.getCell(0)));
                goods.setNxGoodsName((String) getCellValue(goodsRow.getCell(1)));
                goods.setNxGoodsFatherId((Integer) getCellValue(goodsRow.getCell(2)));
                goods.setNxGoodsStandardname((String) getCellValue(goodsRow.getCell(3)));
                goods.setNxGoodsPlace((String) getCellValue(goodsRow.getCell(4)));
                goods.setNxGoodsBrand((String) getCellValue(goodsRow.getCell(5)));
                nxGoodsService.save(goods);
            }
        }

        return R.ok();

    }


    private Object getCellValue(Cell cell) {

        switch (cell.getCellType()) {
            case STRING:
                return cell.getRichStringCellValue().getString();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double numericCellValue = cell.getNumericCellValue();
                    String s = String.valueOf(numericCellValue);
                    int i1 = Integer.parseInt(s.replace(".0", ""));
                    return i1;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
        }

        return cell;

    }


    /**
     * wait
     * ============
     */

    /**
     * 信息
     */
    @ResponseBody
    @RequestMapping(value = "/info/{nxGoodsId}")
    public R info(@PathVariable Integer nxGoodsId) {
        NxGoodsEntity nxGoods = nxGoodsService.queryObject(nxGoodsId);
        return R.ok().put("data", nxGoods);
    }

    /**
     * 保存
     */
    @ResponseBody
    @RequestMapping(value = "/save", produces = "text/html;charset=UTF-8")
    public R save(@RequestParam("file") MultipartFile file,
                  @RequestParam("nxGoodsName") String goodsName,
                  @RequestParam("nxGoodsStandardName") String nxGoodsStandardName,
                  @RequestParam("nxGoodsId") Integer nxGoodsId,
                  @RequestParam("nxGoodsFatherId") Integer nxGoodsFatherId,
                  HttpSession session) {


        //1,上传图片
        String newUploadName = "goodsImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;

        if (nxGoodsId.equals(-1)) {
            NxGoodsEntity goodsEntity = new NxGoodsEntity();
            goodsEntity.setNxGoodsFile(filePath);
            goodsEntity.setNxGoodsName(goodsName);
            goodsEntity.setNxGoodsStandardname(nxGoodsStandardName);
            goodsEntity.setNxGoodsFatherId(nxGoodsFatherId);
            nxGoodsService.save(goodsEntity);
        } else {

        }

        return R.ok();
    }


    /**
     * 保存
     */
    @ResponseBody
    @RequestMapping(value = "/saveNxFather", produces = "text/html;charset=UTF-8")
    public R saveNxFather(@RequestParam("file") MultipartFile file,
                          @RequestParam("goodsName") String goodsName,
                          @RequestParam("fatherId") Integer fatherId,
                          HttpSession session) {

        //1,上传图片
        String newUploadName = "goodsImage";
        String englishKuohao = getEnglishKuohao(goodsName);
        String headByString = hanziToPinyin(englishKuohao);
        String lastFileName = getXiegang(englishKuohao) + formatFullTime();

        String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
        String filename = file.getOriginalFilename();

        String filePath = newUploadName + "/" + lastFileName + ".jpg";
        NxGoodsEntity goodsEntity = new NxGoodsEntity();
        goodsEntity.setNxGoodsFile(filePath);
        goodsEntity.setNxGoodsName(englishKuohao);
        goodsEntity.setNxGoodsFatherId(fatherId);

        nxGoodsService.save(goodsEntity);
        return R.ok();
    }

    @ResponseBody
    @RequestMapping(value = "/saveNxFatherNew", method = RequestMethod.POST)
    public R saveNxFatherNew(String goodsName, Integer fatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("goodsName", goodsName);
        List<NxGoodsEntity> goodsEntities = nxGoodsService.queryIfHasSameGoods(map);
        if (goodsEntities.size() > 0) {
            return R.error(-1, "已有相同商品");
        } else {
            int maxid = nxGoodsService.querySecondLevelMaxId();
            NxGoodsEntity goodsEntity = new NxGoodsEntity();
            goodsEntity.setNxGoodsId(maxid + 1);
            goodsEntity.setNxGoodsFatherId(fatherId);
            goodsEntity.setNxGoodsName(goodsName);
            goodsEntity.setNxGoodsLevel(2);
            nxGoodsService.save(goodsEntity);
            return R.ok().put("data", goodsEntity);
        }

    }

    /**
     * 保存
     */
    @ResponseBody
    @RequestMapping(value = "/saveFather", produces = "text/html;charset=UTF-8")
    public R saveFather(@RequestParam("file") MultipartFile file,
                        @RequestParam("goodsName") String goodsName,
                        @RequestParam("fatherId") Integer fatherId,
                        @RequestParam("sort") Integer sort,
                        @RequestParam("id") Integer id,
                        HttpSession session) {

        //1,上传图片
        String newUploadName = "goodsImage";
        String headByString = hanziToPinyin(goodsName);
        String englishKuohao = getEnglishKuohao(goodsName);
        String lastFileName = getXiegang(englishKuohao) + formatFullTime();

        String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + lastFileName + ".jpg";
        System.out.println("headByString" + headByString);

        NxGoodsEntity goodsEntity = new NxGoodsEntity();
        goodsEntity.setNxGoodsFile(filePath);
        goodsEntity.setNxGoodsName(englishKuohao);
        goodsEntity.setNxGoodsFatherId(fatherId);
        goodsEntity.setNxGoodsSort(sort);
        goodsEntity.setNxGoodsId(id);
        nxGoodsService.save(goodsEntity);


        return R.ok();
    }


    /**
     * 保存
     */
    @ResponseBody
    @RequestMapping(value = "/updateFather", produces = "text/html;charset=UTF-8")
    public R updateFather(@RequestParam("file") MultipartFile file,
                          @RequestParam("goodsName") String goodsName,
                          @RequestParam("id") Integer id,
                          HttpSession session) {

        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(id);
        String gbDistributerFoodImg = nxGoodsEntity.getNxGoodsFile();
        if (nxGoodsEntity.getNxGoodsFile() != null) {
            ServletContext servletContext = session.getServletContext();
            String realPath1 = servletContext.getRealPath(gbDistributerFoodImg);
            File file1 = new File(realPath1);
            if (file1.exists()) {
                file1.delete();
            }
        }


        //1,上传图片
        String newUploadName = "goodsImage";
        String englishKuohao = getEnglishKuohao(goodsName);
        String lastFileName = goodsName + formatFullTime();
        String pinyin = hanziToPinyin(englishKuohao);
        String headPinyin = getHeadStringByString(englishKuohao, false, null);
        nxGoodsEntity.setNxGoodsPinyin(pinyin);
        nxGoodsEntity.setNxGoodsPy(headPinyin);
        String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + lastFileName + ".jpg";
        nxGoodsEntity.setNxGoodsFile(filePath);
        nxGoodsEntity.setNxGoodsName(englishKuohao);
        nxGoodsService.update(nxGoodsEntity);


        if (nxGoodsEntity.getNxGoodsLevel() < 3) {
            //NxGoods
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDFatherGoodsService.queryDisFathersGoodsByNxGoodsId(nxGoodsEntity.getNxGoodsId());
            if (fatherGoodsEntities.size() > 0) {
                for (NxDistributerFatherGoodsEntity nxFather : fatherGoodsEntities) {
                    nxFather.setNxDfgFatherGoodsImg(filePath);
                    nxFather.setNxDfgFatherGoodsName(englishKuohao);
                    nxDFatherGoodsService.update(nxFather);
                }
            }

            //GbGoods
            List<GbDistributerFatherGoodsEntity> gbFtherGoodsEntities = gbDisFatherGoodsService.queryDisFathersGoodsByNxGoodsId(nxGoodsEntity.getNxGoodsId());
            System.out.println("gbgbgbbgbfatehrhhrherhe" + gbFtherGoodsEntities.size());
            if (gbFtherGoodsEntities.size() > 0) {
                for (GbDistributerFatherGoodsEntity fatherGoodsEntity1 : gbFtherGoodsEntities) {
                    fatherGoodsEntity1.setGbDfgFatherGoodsImg(filePath);
                    fatherGoodsEntity1.setGbDfgFatherGoodsName(englishKuohao);
                    gbDisFatherGoodsService.update(fatherGoodsEntity1);
                }
            }

        } else {

            // update NxDisGoods
            List<NxDistributerGoodsEntity> distributerGoodsEntities = nxDistributerGoodsService.querydisGoodsByNxGoodsId(nxGoodsEntity.getNxGoodsId());
            if (distributerGoodsEntities.size() > 0) {
                for (NxDistributerGoodsEntity disGoods : distributerGoodsEntities) {
                    disGoods.setNxDgGoodsFile(filePath);
                    disGoods.setNxDgGoodsName(goodsName);
                    disGoods.setNxDgGoodsPinyin(pinyin);
                    disGoods.setNxDgGoodsPy(headPinyin);
                    nxDistributerGoodsService.update(disGoods);
                    Map<String, Object> map = new HashMap<>();
                    map.put("disGoodsId", disGoods.getNxDistributerGoodsId());
                    List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);

                    if(departmentDisGoodsEntities.size() > 0){
                        for(NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity: departmentDisGoodsEntities){
                            nxDepartmentDisGoodsEntity.setNxDdgDepGoodsName(englishKuohao);
                            nxDepartmentDisGoodsEntity.setNxDdgDepGoodsPinyin(pinyin);
                            nxDepartmentDisGoodsEntity.setNxDdgDepGoodsPy(headPinyin);
                            nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);
                        }
                    }
                }
            }

            //update GbDisGoods
            List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDistributerGoodsService.querydisGoodsByNxGoodsId(nxGoodsEntity.getNxGoodsId());
            if (gbDistributerGoodsEntities.size() > 0) {
                for (GbDistributerGoodsEntity gbDistributerGoodsEntity : gbDistributerGoodsEntities) {
                    gbDistributerGoodsEntity.setGbDgNxFatherImg(filePath);
                    gbDistributerGoodsEntity.setGbDgGoodsName(englishKuohao);
                    gbDistributerGoodsEntity.setGbDgGoodsPinyin(pinyin);
                    gbDistributerGoodsEntity.setGbDgGoodsPy(headPinyin);
                    gbDistributerGoodsService.update(gbDistributerGoodsEntity);

                    Map<String, Object> map = new HashMap<>();
                    map.put("disGoodsId",gbDistributerGoodsEntity.getGbDistributerGoodsId() );
                    List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);

                    if(departmentDisGoodsEntities.size() > 0){
                        for(GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity: departmentDisGoodsEntities){
                            gbDepartmentDisGoodsEntity.setGbDdgDepGoodsName(englishKuohao);
                            gbDepartmentDisGoodsEntity.setGbDdgDepGoodsPinyin(pinyin);
                            gbDepartmentDisGoodsEntity.setGbDdgDepGoodsPy(headPinyin);
                            gbDepDisGoodsService.update(gbDepartmentDisGoodsEntity);
                        }
                    }
                }
            }
        }

        return R.ok();
    }

    @ResponseBody
    @RequestMapping(value = "/updateFatherBig", produces = "text/html;charset=UTF-8")
    public R updateFatherBig(@RequestParam("file") MultipartFile file,
                          @RequestParam("goodsName") String goodsName,
                          @RequestParam("id") Integer id,
                          HttpSession session) {

        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(id);
        String gbDistributerFoodImg = nxGoodsEntity.getNxGoodsFileBig();
        if (nxGoodsEntity.getNxGoodsFileBig() != null) {
            ServletContext servletContext = session.getServletContext();
            String realPath1 = servletContext.getRealPath(gbDistributerFoodImg);
            File file1 = new File(realPath1);
            if (file1.exists()) {
                file1.delete();
            }
        }


        //1,上传图片
        String newUploadName = "goodsImage";
        String englishKuohao = getEnglishKuohao(goodsName);
        String lastFileName = goodsName + formatFullTime() + "large";
        String pinyin = hanziToPinyin(englishKuohao);
        String headPinyin = getHeadStringByString(englishKuohao, false, null);
        nxGoodsEntity.setNxGoodsPinyin(pinyin);
        nxGoodsEntity.setNxGoodsPy(headPinyin);
        String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + lastFileName + ".jpg";
        nxGoodsEntity.setNxGoodsFileBig(filePath);
        nxGoodsEntity.setNxGoodsName(englishKuohao);
        nxGoodsService.update(nxGoodsEntity);


        if (nxGoodsEntity.getNxGoodsLevel() < 3) {
            //NxGoods
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDFatherGoodsService.queryDisFathersGoodsByNxGoodsId(nxGoodsEntity.getNxGoodsId());
            if (fatherGoodsEntities.size() > 0) {
                for (NxDistributerFatherGoodsEntity nxFather : fatherGoodsEntities) {
                    nxFather.setNxDfgFatherGoodsImgLarge(filePath);
                    nxFather.setNxDfgFatherGoodsName(englishKuohao);
                    nxDFatherGoodsService.update(nxFather);
                }
            }

            //GbGoods
            List<GbDistributerFatherGoodsEntity> gbFtherGoodsEntities = gbDisFatherGoodsService.queryDisFathersGoodsByNxGoodsId(nxGoodsEntity.getNxGoodsId());
            System.out.println("gbgbgbbgbfatehrhhrherhe" + gbFtherGoodsEntities.size());
            if (gbFtherGoodsEntities.size() > 0) {
                for (GbDistributerFatherGoodsEntity fatherGoodsEntity1 : gbFtherGoodsEntities) {
                    fatherGoodsEntity1.setGbDfgFatherGoodsImgLarge(filePath);
                    fatherGoodsEntity1.setGbDfgFatherGoodsName(englishKuohao);
                    gbDisFatherGoodsService.update(fatherGoodsEntity1);
                }
            }

        } else {

            // update NxDisGoods
            List<NxDistributerGoodsEntity> distributerGoodsEntities = nxDistributerGoodsService.querydisGoodsByNxGoodsId(nxGoodsEntity.getNxGoodsId());
            if (distributerGoodsEntities.size() > 0) {
                for (NxDistributerGoodsEntity disGoods : distributerGoodsEntities) {
                    disGoods.setNxDgGoodsFileLarge(filePath);
                    disGoods.setNxDgGoodsName(goodsName);
                    disGoods.setNxDgGoodsPinyin(pinyin);
                    disGoods.setNxDgGoodsPy(headPinyin);
                    nxDistributerGoodsService.update(disGoods);
                    Map<String, Object> map = new HashMap<>();
                    map.put("disGoodsId", disGoods.getNxDistributerGoodsId());
                    List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);

                    if(departmentDisGoodsEntities.size() > 0){
                        for(NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity: departmentDisGoodsEntities){
                            nxDepartmentDisGoodsEntity.setNxDdgDepGoodsName(englishKuohao);
                            nxDepartmentDisGoodsEntity.setNxDdgDepGoodsPinyin(pinyin);
                            nxDepartmentDisGoodsEntity.setNxDdgDepGoodsPy(headPinyin);
                            nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);
                        }
                    }
                }
            }

            //update GbDisGoods
            List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDistributerGoodsService.querydisGoodsByNxGoodsId(nxGoodsEntity.getNxGoodsId());
            if (gbDistributerGoodsEntities.size() > 0) {
                for (GbDistributerGoodsEntity gbDistributerGoodsEntity : gbDistributerGoodsEntities) {
                    gbDistributerGoodsEntity.setGbDgNxFatherImgLarge(filePath);
                    gbDistributerGoodsEntity.setGbDgGoodsName(englishKuohao);
                    gbDistributerGoodsEntity.setGbDgGoodsPinyin(pinyin);
                    gbDistributerGoodsEntity.setGbDgGoodsPy(headPinyin);
                    gbDistributerGoodsService.update(gbDistributerGoodsEntity);

                    Map<String, Object> map = new HashMap<>();
                    map.put("disGoodsId",gbDistributerGoodsEntity.getGbDistributerGoodsId() );
                    List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);

                    if(departmentDisGoodsEntities.size() > 0){
                        for(GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity: departmentDisGoodsEntities){
                            gbDepartmentDisGoodsEntity.setGbDdgDepGoodsName(englishKuohao);
                            gbDepartmentDisGoodsEntity.setGbDdgDepGoodsPinyin(pinyin);
                            gbDepartmentDisGoodsEntity.setGbDdgDepGoodsPy(headPinyin);
                            gbDepDisGoodsService.update(gbDepartmentDisGoodsEntity);
                        }
                    }
                }
            }


        }

        return R.ok();
    }
    /**
     * 保存
     */
    @ResponseBody
    @RequestMapping(value = "/updateFatherName", method = RequestMethod.POST)
    public R updateFatherName(@RequestBody NxGoodsEntity goods) {
        String englishKuohao = getEnglishKuohao(goods.getNxGoodsName());
        goods.setNxGoodsName(englishKuohao);
        nxGoodsService.update(goods);

        return R.ok();
    }

    @RequestMapping(value = "/deleteNxGoods/{goodsId}")
    @ResponseBody
    public R deleteNxGoods(@PathVariable Integer goodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", goodsId);
        List<NxGoodsEntity> gbDistributerGoodsEntities = nxGoodsService.queryNxGoodsByParams(map);
        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.querySubNameByFatherId(goodsId);
        if (gbDistributerGoodsEntities.size() > 0 || nxGoodsEntities.size() > 0) {
            return R.error(-1, "有商品不能删除");
        } else {
            nxGoodsService.delete(goodsId);
            return R.ok();
        }
    }


}
