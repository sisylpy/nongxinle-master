package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.Constant;
import com.nongxinle.utils.UploadFile;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.NxCommunityTypeUtils.getNxCommunityGoodsSellTypeCoupon;
import static com.nongxinle.utils.NxCommunityTypeUtils.getNxCommunityGoodsTypeTaocan;
import static com.nongxinle.utils.PinYin4jUtils.*;


@RestController
@RequestMapping("api/nxcommunitygoods")
public class NxCommunityGoodsController {
    @Autowired
    private NxCommunityGoodsService cgService;

    @Autowired
    private NxCommunityFatherGoodsService cfgService;
    @Autowired
    private NxCommunityGoodsSetItemService nxCommunityGoodsSetItemService;
    @Autowired
    private NxCommunityGoodsSetPropertyService nxCgSetPropertyService;
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;
    @Autowired
    private NxCommunityCouponService nxCommunityCouponService;
    @Autowired
    private NxCommunityCardService nxCommunityCardService;
    @Autowired
    private NxCustomerUserCardService nxCustomerUserCardService;
    @Autowired
    private NxCommunityAdsenseService nxCommunityAdsenseService;
    @Autowired
    private NxGoodsService nxGoodsService;
//    @Autowired
//    private NxCommunityOrdersSubService nxCommunityOrdersSubService;
//    @Autowired
//    private NxGoodsService nxGoodsService;

    @Autowired
    private NxRestrauntOrdersService nxRestrauntOrdersService;
    @Autowired
    private NxCommunityPurchaseGoodsService nxCommunityPurchaseGoodsService;
    @Autowired
    private NxCommunityAliasService nxCommunityAliasService;
    @Autowired
    private NxCommunityPurchaseBatchService ncBatchService;
    @Autowired
    private NxRestrauntComGoodsService nxRestrauntComGoodsService;
    @Autowired
    private NxRestrauntService nxRestrauntService;
    @Autowired
    private NxDistributerGoodsService nxDisGoodsService;
    @Autowired
    private NxCommunityGoodsMediaService nxCommunityGoodsMediaService;
//


    private NxCommunityGoodsEntity saveCommunityGoods(NxCommunityGoodsEntity cgGoods) {

        cgGoods.setNxCgDistributerId(-1);
        cgGoods.setNxCgBuyingPriceExchangeDate(formatWhatYearDayTime(0));
        cgGoods.setNxCgGoodsPriceExchangeDate(formatWhatYearDayTime(0));
        cgGoods.setNxCgGoodsTwoPriceExchangeDate(formatWhatYearDayTime(0));
        cgGoods.setNxCgGoodsThreePriceExchangeDate(formatWhatYearDayTime(0));

        //queryGrandFatherId
        NxGoodsEntity fatherEntity = nxGoodsService.queryObject(cgGoods.getNxCgNxFatherId());
        Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
        cgGoods.setNxCgNxGrandId(grandFatherId);
        NxGoodsEntity grandEntity = nxGoodsService.queryObject(grandFatherId);
        cgGoods.setNxCgNxGrandName(grandEntity.getNxGoodsName());

        //queryGreatGrandFatherId
        Integer greatGrandFatherId = grandEntity.getNxGoodsFatherId();
        cgGoods.setNxCgNxGreatGrandId(greatGrandFatherId);
        cgGoods.setNxCgNxGreatGrandName(nxGoodsService.queryObject(greatGrandFatherId).getNxGoodsName());

        Integer communityId = cgGoods.getNxCgCommunityId();

        // 3， 查询父类
        Integer nxDgNxFatherId = cgGoods.getNxCgNxFatherId();
        Map<String, Object> map = new HashMap<>();
        map.put("comId", communityId);
        map.put("nxFatherId", nxDgNxFatherId);
        List<NxCommunityGoodsEntity> communityGoodsEntities = cgService.queryComGoodsHasNxGoodsFather(map);

        if (communityGoodsEntities.size() > 0) {
            NxCommunityGoodsEntity communityGoodsEntity = communityGoodsEntities.get(0);

            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            Integer nxDgDfgGoodsFatherId1 = communityGoodsEntity.getNxCgCfgGoodsFatherId();

            NxCommunityFatherGoodsEntity nxCommunityFatherGoodsEntity = cfgService.queryObject(nxDgDfgGoodsFatherId1);
            Integer nxDfgGoodsAmount = nxCommunityFatherGoodsEntity.getNxCfgGoodsAmount();
            nxCommunityFatherGoodsEntity.setNxCfgGoodsAmount(nxDfgGoodsAmount + 1);
            cfgService.update(nxCommunityFatherGoodsEntity);

            //2，保存disId商品
            Integer nxDgDfgGoodsFatherId = communityGoodsEntity.getNxCgCfgGoodsFatherId();
            cgGoods.setNxCgCfgGoodsFatherId(nxDgDfgGoodsFatherId);
            //配置给disDistributer

//            cgGoods = peizhiNxDistributer(cgGoods);
            //1 ，先保存disGoods
            cgService.save(cgGoods);
            //
        } else {
            //添加fatherGoods的第一个级别
            NxCommunityFatherGoodsEntity cgf = new NxCommunityFatherGoodsEntity();
            cgf.setNxCfgCommunityId(cgGoods.getNxCommunityGoodsId());
            cgf.setNxCfgFatherGoodsName(cgGoods.getNxCgNxFatherName());
            cgf.setNxCfgFatherGoodsLevel(2);
            cgf.setNxCfgGoodsAmount(1);
            cgf.setNxCfgFatherGoodsColor(cgGoods.getNxCgNxGoodsFatherColor());
            cgf.setNxCfgFatherGoodsColor(cgGoods.getNxCgNxGoodsFatherColor());
            cgf.setNxCfgNxGoodsId(cgGoods.getNxCgNxFatherId());
            cgf.setNxCfgCommunityId(cgGoods.getNxCgCommunityId());
            cgf.setNxCfgFatherGoodsImg(cgGoods.getNxCgNxFatherImg());
            cgf.setNxCfgPriceAmount(0);
            cgf.setNxCfgPriceTwoAmount(0);
            cgf.setNxCfgPriceThreeAmount(0);
            cgf.setNxCfgOrderRank(0);
            cfgService.save(cgf);
            //更新disGoods的fatherGoodsId
            Integer communityFatherGoodsId = cgf.getNxCommunityFatherGoodsId();
            cgGoods.setNxCgCfgGoodsFatherId(communityFatherGoodsId);

            cgGoods = peizhiNxDistributer(cgGoods);
            cgService.save(cgGoods);
            //继续查询是否有GrandFather
            String grandName = cgGoods.getNxCgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("comId", communityId);
            map2.put("fathersFatherName", grandName);
            List<NxCommunityFatherGoodsEntity> grandGoodsFather = cfgService.queryHasComFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                NxCommunityFatherGoodsEntity communityFatherGoodsEntity = grandGoodsFather.get(0);
                cgf.setNxCfgFathersFatherId(communityFatherGoodsEntity.getNxCommunityFatherGoodsId());
                cfgService.update(cgf);
            } else {

                //tianjiaGrand
                NxCommunityFatherGoodsEntity grand = new NxCommunityFatherGoodsEntity();
                String nxCgGrandFatherName = cgGoods.getNxCgNxGrandName();
                grand.setNxCfgFatherGoodsName(nxCgGrandFatherName);
                grand.setNxCfgCommunityId(cgGoods.getNxCgCommunityId());
                grand.setNxCfgFatherGoodsLevel(1);
                grand.setNxCfgOrderRank(0);
                grand.setNxCfgFatherGoodsColor(cgGoods.getNxCgNxGoodsFatherColor());
                grand.setNxCfgNxGoodsId(cgGoods.getNxCgNxGrandId());
                cfgService.save(grand);

                //todo
                cgf.setNxCfgFathersFatherId(grand.getNxCommunityFatherGoodsId());
                cfgService.update(cgf);


                //查询是否有greatGrand
                String greatGrandName = cgGoods.getNxCgNxGreatGrandName();
                Map<String, Object> map3 = new HashMap<>();
                map3.put("comId", communityId);
                map3.put("fathersFatherName", greatGrandName);
                List<NxCommunityFatherGoodsEntity> greatGrandGoodsFather = cfgService.queryHasComFathersFather(map3);
                if (greatGrandGoodsFather.size() > 0) {
                    NxCommunityFatherGoodsEntity NxCommunityFatherGoodsEntity = greatGrandGoodsFather.get(0);
                    Integer disFatherId = NxCommunityFatherGoodsEntity.getNxCommunityFatherGoodsId();
                    grand.setNxCfgFathersFatherId(disFatherId);
                    cfgService.update(grand);
                } else {
                    NxCommunityFatherGoodsEntity greatGrand = new NxCommunityFatherGoodsEntity();
                    String greatGrandName1 = cgGoods.getNxCgNxGreatGrandName();
                    greatGrand.setNxCfgFatherGoodsName(greatGrandName1);
                    greatGrand.setNxCfgCommunityId(cgGoods.getNxCgCommunityId());
                    greatGrand.setNxCfgFatherGoodsLevel(0);
                    greatGrand.setNxCfgOrderRank(0);
                    greatGrand.setNxCfgFatherGoodsColor(cgGoods.getNxCgNxGoodsFatherColor());
                    greatGrand.setNxCfgNxGoodsId(cgGoods.getNxCgNxGreatGrandId());
                    cfgService.save(greatGrand);
                    grand.setNxCfgFathersFatherId(greatGrand.getNxCommunityFatherGoodsId());
                    cfgService.update(grand);
                }
            }
        }


        return cgGoods;
    }


    private NxCommunityGoodsEntity peizhiNxDistributer(NxCommunityGoodsEntity cgGoodsEntity){
        //
//        Integer nxCgNxGoodsId = cgGoodsEntity.getNxCgNxGoodsId();
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", 1);
//        map.put("nxGoodsId", nxCgNxGoodsId);
//        NxDistributerGoodsEntity disGoodsEntity =   nxDisGoodsService.queryNxDisGoodsByNxGoodsId(map);
////        cgGoodsEntity.setNxCgBuyingPrice(disGoodsEntity.getNxDgBuyingPrice());
////        cgGoodsEntity.setNxCgGoodsPrice(disGoodsEntity.getNxDgPriceProfitOnePrice());
////        cgGoodsEntity.setNxCgGoodsTwoPrice(disGoodsEntity.getNxDgPriceProfitTwoPrice());
////        cgGoodsEntity.setNxCgGoodsThreePrice(disGoodsEntity.getNxDgPriceProfitThreePrice());
//        cgGoodsEntity.setNxCgDistributerGoodsId(disGoodsEntity.getNxDistributerGoodsId());
//        cgGoodsEntity.setNxCgDistributerId(1);
        return cgGoodsEntity;
    }
    @ResponseBody
    @RequestMapping("/comSaveCommunityGoods")
    public R comSaveCommunityGoods(@RequestBody NxCommunityGoodsEntity nxCommunityGoodsEntity) {

        System.out.println("nandaooadoddodododo");

        String goodsName = nxCommunityGoodsEntity.getNxCgGoodsName();
        String nxGoodsDetail = nxCommunityGoodsEntity.getNxCgGoodsDetail();
        String nxGoodsBrand = nxCommunityGoodsEntity.getNxCgGoodsBrand();
        String nxCgGoodsStandardname = nxCommunityGoodsEntity.getNxCgGoodsStandardname();

        Map<String, Object> map = new HashMap<>();
        map.put("goodsName", goodsName);
        map.put("goodsDetail", nxGoodsDetail);
        map.put("goodsBrand", nxGoodsBrand);
        map.put("goodsStandard", nxCgGoodsStandardname);
        List<NxGoodsEntity> goodsEntities = nxGoodsService.queryIfHasSameGoods(map);
        if (goodsEntities.size() > 0) {
            return R.error(-1, "已有相同商品");

        } else {
            System.out.println("jinlaikakakak");

            //保存nxgoods
            NxGoodsEntity nxGoodsEntity = new NxGoodsEntity();
            nxGoodsEntity.setNxGoodsName(goodsName);
            nxGoodsEntity.setNxGoodsDetail(nxGoodsDetail);
            nxGoodsEntity.setNxGoodsBrand(nxGoodsBrand);
            String pinyin = hanziToPinyin(goodsName);
            String headPinyin = getHeadStringByString(goodsName, false, null);
            nxGoodsEntity.setNxGoodsPinyin(pinyin);
            nxGoodsEntity.setNxGoodsPy(headPinyin);
            nxGoodsEntity.setNxGoodsFatherId(nxCommunityGoodsEntity.getNxCgNxFatherId());
            nxGoodsEntity.setNxGoodsStandardname(nxCommunityGoodsEntity.getNxCgGoodsStandardname());
            nxGoodsEntity.setNxGoodsBrand(nxCommunityGoodsEntity.getNxCgGoodsBrand());
            nxGoodsEntity.setNxGoodsPlace(nxCommunityGoodsEntity.getNxCgGoodsPlace());
            nxGoodsEntity.setNxGoodsStandardWeight(nxCommunityGoodsEntity.getNxCgGoodsStandardWeight());
            System.out.println(nxCommunityGoodsEntity.getNxCgGoodsStandardWeight() + "wweeee");
            System.out.println("shshshhshshshs========");
            nxGoodsService.save(nxGoodsEntity);

            //保存comGoods
            Integer nxGoodsId = nxGoodsEntity.getNxGoodsId();
            nxCommunityGoodsEntity.setNxCgNxGoodsId(nxGoodsId);
            nxCommunityGoodsEntity.setNxCgGoodsPinyin(pinyin);
            nxCommunityGoodsEntity.setNxCgGoodsPy(headPinyin);
            nxCommunityGoodsEntity.setNxCgNxGoodsId(nxGoodsId);

            NxCommunityGoodsEntity communityGoodsEntity = saveCommunityGoods(nxCommunityGoodsEntity);

            updatePriceAmount(nxCommunityGoodsEntity);
            return R.ok().put("data", communityGoodsEntity.getNxCommunityGoodsId());
        }
    }

    private void updatePriceAmount(NxCommunityGoodsEntity cgGoods) {
        Integer nxCgCfgGoodsFatherId = cgGoods.getNxCgCfgGoodsFatherId();
        NxCommunityFatherGoodsEntity fatherGoodsEntity = cfgService.queryObject(nxCgCfgGoodsFatherId);

        String nxCgGoodsPrice = cgGoods.getNxCgGoodsPrice();
        String nxCgGoodsTwoPrice = cgGoods.getNxCgGoodsTwoPrice();
        String nxCgGoodsThreePrice = cgGoods.getNxCgGoodsThreePrice();
        if (!nxCgGoodsPrice.equals("null") && !nxCgGoodsPrice.equals("0")) {
            fatherGoodsEntity.setNxCfgPriceAmount(fatherGoodsEntity.getNxCfgPriceAmount() + 1);
        }
        System.out.println(cgGoods.getNxCgGoodsTwoPrice() + "getTwoPricieiciieieiieeii");
        if (!nxCgGoodsTwoPrice.equals("null") && !nxCgGoodsTwoPrice.equals("0")) {
            fatherGoodsEntity.setNxCfgPriceTwoAmount(fatherGoodsEntity.getNxCfgPriceTwoAmount() + 1);
        }
        if (!nxCgGoodsThreePrice.equals("null") && !nxCgGoodsThreePrice.equals("0")) {
            fatherGoodsEntity.setNxCfgPriceThreeAmount(fatherGoodsEntity.getNxCfgPriceThreeAmount() + 1);
        }
        cfgService.update(fatherGoodsEntity);

    }

    @RequestMapping(value = "/comGetIbookGoods", method = RequestMethod.POST)
    @ResponseBody
    public R comGetIbookGoods(Integer limit, Integer page, Integer fatherId, Integer comId) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("offset", (page - 1) * limit);
        map1.put("limit", limit);
        map1.put("fatherId", fatherId);
        List<NxGoodsEntity> nxGoodsEntities1 = nxGoodsService.queryNxGoodsByParams(map1);

        List<NxGoodsEntity> goodsEntities = new ArrayList<>();

        for (NxGoodsEntity goods : nxGoodsEntities1) {
            Map<String, Object> map = new HashMap<>();
            map.put("comId", comId);
            map.put("goodsId", goods.getNxGoodsId());
            List<NxCommunityGoodsEntity> dgGoods = cgService.queryAddCommunityNxGoods(map);

            if (dgGoods.size() > 0) {
                goods.setIsDownload(1);
                goods.setNxCommunityGoodsEntity(dgGoods.get(0));
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

//    @RequestMapping(value = "/queryGoodsWithPinyin", method = RequestMethod.POST)
//    @ResponseBody
//    public R queryGoodsWithPinyin(@RequestBody NxCommunityGoodsEntity goodsEntity) {
//        System.out.println("haiiahfiai");
//        System.out.println(goodsEntity);
//        System.out.println(goodsEntity.getNxCgGoodsPinyin());
//        Integer nxCgCommunityId = goodsEntity.getNxCgCommunityId();
//        Map<String, Object> map = new HashMap<>();
//        map.put("nxCgCommunityId", nxCgCommunityId);
//        map.put("pinyin", goodsEntity.getNxCgGoodsPinyin());
//        List<NxCommunityGoodsEntity> entities = cgService.queryCommunityGoodsWithPinyin(map);
//        return R.ok().put("data", entities);
//    }


    @RequestMapping(value = "/comGetComGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R comGetComGoodsListByFatherId(Integer fatherId, Integer type,
                                          Integer limit, Integer page) {

        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("cgFatherId", fatherId);
        map.put("type", type);
        List<NxCommunityGoodsEntity>  goodsEntities1 ;
        if(type.equals(4)){
            goodsEntities1 = cgService.queryComGoodsWithSupplierByParams(map);
        }else {
            goodsEntities1 = cgService.queryComGoodsByParams(map);
        }


        Map<String, Object> map3 = new HashMap<>();
        map3.put("fatherId", fatherId);
        map3.put("type", type);
        int total = cgService.queryTotalByFatherId(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @ResponseBody
    @RequestMapping(value = "/delComGoods", method = RequestMethod.POST)
    public R delComGoods(Integer id, HttpSession session) {

        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryObject(id);

        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", id);
        System.out.println("ididiid" + map);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByCustomerUserId(map);
        if(subEntities.size() == 0){
            NxCommunityGoodsEntity nxCommunityGoodsEntity = cgService.queryObject(id);
            List<NxCommunityGoodsSetItemEntity> entities =  nxCommunityGoodsSetItemService.queryCgGoodsSetListByParams(map);
            if(entities.size() > 0){
                for(int i = 0; i < entities.size(); i++){
                    nxCommunityGoodsSetItemService.delete(entities.get(i).getNxCommunityGoodsSetItemId());
                }
            }

            Map<String, Object> mapP = new HashMap<>();
            mapP.put("goodsId", id);
            List<NxCommunityGoodsSetPropertyEntity> nxCgSetPropertyEntities =   nxCgSetPropertyService.queryCgGoodsPropertyListByParams(mapP);
            if(nxCgSetPropertyEntities.size() > 0){
                for(int i = 0; i < nxCgSetPropertyEntities.size(); i++){
                    nxCgSetPropertyService.delete(nxCgSetPropertyEntities.get(i).getNxCommunityGoodsSetPropertyId());
                }
            }

            Integer NxCgDfgGoodsFatherId = nxCommunityGoodsEntity.getNxCgCfgGoodsFatherId();
            NxCommunityFatherGoodsEntity fatherGoodsEntity = cfgService.queryObject(NxCgDfgGoodsFatherId);
            fatherGoodsEntity.setNxCfgGoodsAmount(fatherGoodsEntity.getNxCfgGoodsAmount() - 1);
            cfgService.update(fatherGoodsEntity);
            String oldPath = communityGoodsEntity.getNxCgNxGoodsFilePath();
            if (oldPath != null && !oldPath.trim().isEmpty()) {
                // 旧文件绝对路径
                // oldPath 类似 "goodsImage/oldName.jpg"
                String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
                File oldFile = new File(oldAbsolutePath);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
//
            cgService.delete(id);


            return R.ok();
        }else{
            return R.error(-1,"有订单不能删除");
        }
    }



    @RequestMapping(value = "/updateAdsendse", method = RequestMethod.POST)
    @ResponseBody
    public R updateAdsendse (@RequestBody NxCommunityGoodsEntity nxCommunityGoodsEntity  ) {

        String cgStartTime = nxCommunityGoodsEntity.getNxCgAdsenseStartTime();
        String startHour = cgStartTime.substring(0, 2);
        String startMinute = cgStartTime.substring(3, 5);
        BigDecimal hourMinuteStart = new BigDecimal(startHour).multiply(new BigDecimal(60));
        BigDecimal decimalStart = hourMinuteStart.add(new BigDecimal(startMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
        nxCommunityGoodsEntity.setNxCgAdsenseStartTimeZone(decimalStart.toString());

        String cgStopTime = nxCommunityGoodsEntity.getNxCgAdsenseStopTime();
        String stopHour = cgStopTime.substring(0, 2);
        String stopMinute = cgStopTime.substring(3, 5);
        BigDecimal hourMinuteStop = new BigDecimal(stopHour).multiply(new BigDecimal(60));
        BigDecimal decimalStop = hourMinuteStop.add(new BigDecimal(stopMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
        nxCommunityGoodsEntity.setNxCgAdsenseStopTimeZone(decimalStop.toString());

        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", nxCommunityGoodsEntity.getNxCommunityGoodsId());
        NxCommunityAdsenseEntity adsenseEntity = nxCommunityAdsenseService.queryGoodsAdsenseByParams(map);

        String cgStartTimeAd = nxCommunityGoodsEntity.getNxCgAdsenseStartTime();
        String startHourAd = cgStartTimeAd.substring(0, 2);
        String startMinuteAd = cgStartTimeAd.substring(3, 5);
        BigDecimal hourMinuteStartAd = new BigDecimal(startHourAd).multiply(new BigDecimal(60));
        BigDecimal decimalStartAd = hourMinuteStartAd.add(new BigDecimal(startMinuteAd)).setScale(0, BigDecimal.ROUND_HALF_UP);
        nxCommunityGoodsEntity.setNxCgAdsenseStartTimeZone(decimalStartAd.toString());

        String cgStopTimeAd = nxCommunityGoodsEntity.getNxCgAdsenseStopTime();
        String stopHourAd = cgStopTimeAd.substring(0, 2);
        String stopMinuteAd = cgStopTimeAd.substring(3, 5);
        BigDecimal hourMinuteStopAd = new BigDecimal(stopHourAd).multiply(new BigDecimal(60));
        BigDecimal decimalStopAd = hourMinuteStopAd.add(new BigDecimal(stopMinuteAd)).setScale(0, BigDecimal.ROUND_HALF_UP);
        nxCommunityGoodsEntity.setNxCgAdsenseStopTimeZone(decimalStopAd.toString());

        adsenseEntity.setNxCaStartTimeZone(decimalStartAd.toString());
        adsenseEntity.setNxCaStopTimeZone(decimalStopAd.toString());
        adsenseEntity.setNxCaStartTime(nxCommunityGoodsEntity.getNxCgAdsenseStartTime());
        adsenseEntity.setNxCaStopTime(nxCommunityGoodsEntity.getNxCgAdsenseStopTime());

        nxCommunityAdsenseService.update(adsenseEntity);




            cgService.update(nxCommunityGoodsEntity);



        return R.ok();
    }


    @RequestMapping(value = "/updateComGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updateComGoods (@RequestBody NxCommunityGoodsEntity nxCommunityGoodsEntity) {

            BigDecimal goodsPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsPrice());
            BigDecimal fractionalPart = goodsPrice.subtract(goodsPrice.setScale(0, RoundingMode.DOWN)).multiply(new BigDecimal(10)).setScale(0,BigDecimal.ROUND_HALF_UP);
            BigDecimal integerPart = goodsPrice.setScale(0, RoundingMode.DOWN);
            nxCommunityGoodsEntity.setNxCgGoodsPriceInteger(integerPart.toString());
            nxCommunityGoodsEntity.setNxCgGoodsPriceDecimal(fractionalPart.toString());

            if(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice() != null && nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice().length() > 0){
                BigDecimal huaxianPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
                BigDecimal difDec = huaxianPrice.subtract(goodsPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgGoodsHuaxianPriceDifferent(difDec.toString());

                System.out.println("indiddiidDDD"+ nxCommunityGoodsEntity.getNxCgGoodsPriceDecimal());
                if(nxCommunityGoodsEntity.getNxCgGoodsType() == 2){
                    nxCommunityGoodsEntity.setNxCgBuyingPrice(nxCommunityGoodsEntity.getNxCgGoodsPrice());
                    nxCommunityGoodsEntity.setNxCgBuyingPriceExchange(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
                }
            }else{
                nxCommunityGoodsEntity.setNxCgGoodsHuaxianPriceDifferent(null);
                nxCommunityGoodsEntity.setNxCgGoodsHuaxianPrice(null);
                nxCommunityGoodsEntity.setNxCgGoodsHuaxianQuantity(null);

            }


            if(nxCommunityGoodsEntity.getNxCgSellType() == 1){
                String cgStartTime = nxCommunityGoodsEntity.getNxCgStartTime();
                String startHour = cgStartTime.substring(0, 2);
                String startMinute = cgStartTime.substring(3, 5);
                BigDecimal hourMinuteStart = new BigDecimal(startHour).multiply(new BigDecimal(60));
                BigDecimal decimalStart = hourMinuteStart.add(new BigDecimal(startMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgStartTimeZone(decimalStart.toString());

                String cgStopTime = nxCommunityGoodsEntity.getNxCgStopTime();
                String stopHour = cgStopTime.substring(0, 2);
                String stopMinute = cgStopTime.substring(3, 5);
                BigDecimal hourMinuteStop = new BigDecimal(stopHour).multiply(new BigDecimal(60));
                BigDecimal decimalStop = hourMinuteStop.add(new BigDecimal(stopMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgStopTimeZone(decimalStop.toString());

            }else{

                BigDecimal multiply = new BigDecimal(24).multiply(new BigDecimal(60));
                nxCommunityGoodsEntity.setNxCgStopTimeZone(multiply.toString());
                nxCommunityGoodsEntity.setNxCgStartTimeZone("0");
                nxCommunityGoodsEntity.setNxCgStartTime("00:00");
                nxCommunityGoodsEntity.setNxCgStopTime("23:59");

            }

            if(nxCommunityGoodsEntity.getNxCgIsOpenAdsense() == 1){
                String cgStartTime = nxCommunityGoodsEntity.getNxCgAdsenseStartTime();
                String startHour = cgStartTime.substring(0, 2);
                String startMinute = cgStartTime.substring(3, 5);
                BigDecimal hourMinuteStart = new BigDecimal(startHour).multiply(new BigDecimal(60));
                BigDecimal decimalStart = hourMinuteStart.add(new BigDecimal(startMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgAdsenseStartTimeZone(decimalStart.toString());

                String cgStopTime = nxCommunityGoodsEntity.getNxCgAdsenseStopTime();
                String stopHour = cgStopTime.substring(0, 2);
                String stopMinute = cgStopTime.substring(3, 5);
                BigDecimal hourMinuteStop = new BigDecimal(stopHour).multiply(new BigDecimal(60));
                BigDecimal decimalStop = hourMinuteStop.add(new BigDecimal(stopMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgAdsenseStopTimeZone(decimalStop.toString());

                Map<String, Object> map = new HashMap<>();
                map.put("goodsId", nxCommunityGoodsEntity.getNxCommunityGoodsId());
                NxCommunityAdsenseEntity adsenseEntity = nxCommunityAdsenseService.queryGoodsAdsenseByParams(map);

                String cgStartTimeAd = nxCommunityGoodsEntity.getNxCgAdsenseStartTime();
                String startHourAd = cgStartTimeAd.substring(0, 2);
                String startMinuteAd = cgStartTimeAd.substring(3, 5);
                BigDecimal hourMinuteStartAd = new BigDecimal(startHourAd).multiply(new BigDecimal(60));
                BigDecimal decimalStartAd = hourMinuteStartAd.add(new BigDecimal(startMinuteAd)).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgAdsenseStartTimeZone(decimalStartAd.toString());

                String cgStopTimeAd = nxCommunityGoodsEntity.getNxCgAdsenseStopTime();
                String stopHourAd = cgStopTimeAd.substring(0, 2);
                String stopMinuteAd = cgStopTimeAd.substring(3, 5);
                BigDecimal hourMinuteStopAd = new BigDecimal(stopHourAd).multiply(new BigDecimal(60));
                BigDecimal decimalStopAd = hourMinuteStopAd.add(new BigDecimal(stopMinuteAd)).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgAdsenseStopTimeZone(decimalStopAd.toString());

                adsenseEntity.setNxCaStartTimeZone(decimalStartAd.toString());
                adsenseEntity.setNxCaStopTimeZone(decimalStopAd.toString());
                adsenseEntity.setNxCaStartTime(nxCommunityGoodsEntity.getNxCgAdsenseStartTime());
                adsenseEntity.setNxCaStopTime(nxCommunityGoodsEntity.getNxCgAdsenseStopTime());
                adsenseEntity.setNxCommunityAdsenseName(nxCommunityGoodsEntity.getNxCgGoodsName());

                Integer nxCgGoodsType = nxCommunityGoodsEntity.getNxCgGoodsType();
                Integer nxCgCardId = nxCommunityGoodsEntity.getNxCgCardId();

                String path = "";
                String url = "?nxCommunityGoodsId=" + nxCommunityGoodsEntity.getNxCommunityGoodsId() + "&from=index&orderType=0&spId=-1&pindanId=-1";
                if(nxCgGoodsType == 0){
                    path = "zeroGoodsPage/zeroGoodsPage";
                }else if(nxCgGoodsType == 1){
                    if(nxCgCardId != null){
                        path = "oneGoodsCardPage/oneGoodsCardPage";

                    }else{
                        path = "oneGoodsPage/oneGoodsPage";
                    }


                }else if(nxCgGoodsType == 2){
                    if(nxCgCardId != null){
                        path = "twoGoodsCardPage/twoGoodsCardPage";

                    }else{
                        path = "twoGoodsPage/twoGoodsPage";
                    }
                }
                else if(nxCgGoodsType == 3){
                    if(nxCgCardId != null){
                        path = "threeGoodsCardPage/threeGoodsCardPage";

                    }else{
                        path = "threeGoodsPage/threeGoodsPage";
                    }

                }
                adsenseEntity.setNxCaClickTo(path + url);
                nxCommunityAdsenseService.update(adsenseEntity);
            }

            cgService.update(nxCommunityGoodsEntity);

            if(nxCommunityGoodsEntity.getNxCgCardId() != null){
                Integer nxCgCardId = nxCommunityGoodsEntity.getNxCgCardId();
                NxCommunityCardEntity nxCommunityCardEntity = nxCommunityCardService.queryObject(nxCgCardId);
                nxCommunityGoodsEntity.setNxCommunityCardEntity(nxCommunityCardEntity);
            }



            return R.ok().put("data", nxCommunityGoodsEntity);

    }




    @ResponseBody
    @RequestMapping("/comSaveComCouponGoods")
    public R comSaveComCouponGoods(@RequestBody NxCommunityGoodsEntity nxCommunityGoodsEntity) {

        String goodsName = nxCommunityGoodsEntity.getNxCgGoodsName();

        nxCommunityGoodsEntity.setNxCgGoodsStatus(0);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        nxCommunityGoodsEntity.setNxCgGoodsPy(headPinyin);
        nxCommunityGoodsEntity.setNxCgGoodsPinyin(pinyin);
        nxCommunityGoodsEntity.setNxCgNxGoodsId(-1);
        nxCommunityGoodsEntity.setNxCgNxFatherId(-1);
        nxCommunityGoodsEntity.setNxCgNxGrandId(-1);
        nxCommunityGoodsEntity.setNxCgNxGreatGrandId(-1);
        nxCommunityGoodsEntity.setNxCgSellType(getNxCommunityGoodsSellTypeCoupon());
        nxCommunityGoodsEntity.setNxCgIsOpenAdsense(0);

        if(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice() != null){
            BigDecimal huaxianPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
            BigDecimal goodsPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsPrice());
            BigDecimal difDec = huaxianPrice.subtract(goodsPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            nxCommunityGoodsEntity.setNxCgGoodsHuaxianPriceDifferent(difDec.toString());
            BigDecimal fractionalPart = goodsPrice.subtract(goodsPrice.setScale(0, RoundingMode.DOWN)).multiply(new BigDecimal(10)).setScale(0,BigDecimal.ROUND_HALF_UP);
            BigDecimal integerPart = goodsPrice.setScale(0, RoundingMode.DOWN);
            nxCommunityGoodsEntity.setNxCgGoodsPriceInteger(integerPart.toString());
            nxCommunityGoodsEntity.setNxCgGoodsPriceDecimal(fractionalPart.toString());
        }

        if(nxCommunityGoodsEntity.getNxCgGoodsType() == 2){
            nxCommunityGoodsEntity.setNxCgBuyingPrice(nxCommunityGoodsEntity.getNxCgGoodsPrice());
            nxCommunityGoodsEntity.setNxCgBuyingPriceExchange(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
        }

        cgService.save(nxCommunityGoodsEntity);

        List<NxCommunityGoodsSetItemEntity> itemSubEntities = nxCommunityGoodsEntity.getNxCommunityGoodsSetItemEntities();
        if(itemSubEntities.size() > 0){
            for(int i = 0; i < itemSubEntities.size(); i++){
                NxCommunityGoodsSetItemEntity itemEntity = itemSubEntities.get(i);
                itemEntity.setNxCgsiItemSort(i+1);
                itemEntity.setNxCgsiItemCgGoodsId(nxCommunityGoodsEntity.getNxCommunityGoodsId());
                nxCommunityGoodsSetItemService.save(itemEntity);
            }
        }

        List<NxCommunityGoodsSetPropertyEntity> nxCgSetPropertyEntities = nxCommunityGoodsEntity.getNxCommunityGoodsSetPropertyEntities();

        if(nxCgSetPropertyEntities.size() > 0){
            for(int i = 0; i < nxCgSetPropertyEntities.size(); i++){
                NxCommunityGoodsSetPropertyEntity itemEntity = nxCgSetPropertyEntities.get(i);
                itemEntity.setNxCgspSort(i+1);
                itemEntity.setNxCgspCgGoodsId(nxCommunityGoodsEntity.getNxCommunityGoodsId());
                nxCgSetPropertyService.save(itemEntity);
            }
        }

       NxCommunityCouponEntity goodsCoupon = new NxCommunityCouponEntity();
       goodsCoupon.setNxCpStartDate(nxCommunityGoodsEntity.getCouponStartDate());
       goodsCoupon.setNxCpStartTime(nxCommunityGoodsEntity.getCouponStartTime());
       goodsCoupon.setNxCpStopDate(nxCommunityGoodsEntity.getCouponStopDate());
       goodsCoupon.setNxCpStopTime(nxCommunityGoodsEntity.getCouponStopTime());
        goodsCoupon.setNxCpCgGoodsId(nxCommunityGoodsEntity.getNxCommunityGoodsId());
        goodsCoupon.setNxCommunityCouponName(nxCommunityGoodsEntity.getNxCgGoodsName());
        goodsCoupon.setNxCpCommunityId(nxCommunityGoodsEntity.getNxCgCommunityId());
        goodsCoupon.setNxCpDownCount(0);
        goodsCoupon.setNxCpUseCount(0);
        goodsCoupon.setNxCpPrice(nxCommunityGoodsEntity.getNxCgGoodsPrice());
        goodsCoupon.setNxCpOriginalPrice(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());

        String startDate =  nxCommunityGoodsEntity.getCouponStartDate();
        String stopDate =  nxCommunityGoodsEntity.getCouponStopDate();
        String couponStartTime = nxCommunityGoodsEntity.getCouponStartTime();
        String couponStopTime = nxCommunityGoodsEntity.getCouponStopTime();

        String replaceStart = couponStartTime.replace(":", "-");
        String replaceStop = couponStopTime.replace(":", "-");
        String start = startDate + "-" + replaceStart;
        String  stop = stopDate + "-" + replaceStop;

        String[] splitStart = start.split("-");
        int year = Integer.parseInt(splitStart[0]);
        int month = Integer.parseInt(splitStart[1]);
        int day = Integer.parseInt(splitStart[2]);
        int hour = Integer.parseInt(splitStart[3]);
        int minute = Integer.parseInt(splitStart[4]);
        int haomiao = Integer.parseInt(splitStart[5]);
        LocalDateTime beginTime = LocalDateTime.of(year, month, day, hour, minute, haomiao);

        String[] splitStop = stop.split("-");
        int yearS = Integer.parseInt(splitStop[0]);
        int monthS = Integer.parseInt(splitStop[1]);
        int dayS = Integer.parseInt(splitStop[2]);
        int hourS = Integer.parseInt(splitStop[3]);
        int minuteS = Integer.parseInt(splitStop[4]);
        int haomiaoS = Integer.parseInt(splitStop[5]);
        LocalDateTime stopTime = LocalDateTime.of(yearS, monthS, dayS, hourS, minuteS, haomiaoS);
        System.out.println("adafasd" + beginTime + "stttt" + stopTime);

        goodsCoupon.setNxCpStartTimeZone(beginTime);
        goodsCoupon.setNxCpStopTimeZone(stopTime);
        goodsCoupon.setNxCpStatus(1);
        goodsCoupon.setNxCpType(0);
        goodsCoupon.setNxCpQuantity(nxCommunityGoodsEntity.getNxCgGoodsHuaxianQuantity());

        nxCommunityCouponService.save(goodsCoupon);
        goodsCoupon.setNxCommunityGoodsEntity(nxCommunityGoodsEntity);
        return R.ok().put("data", goodsCoupon);

    }


    @ResponseBody
    @RequestMapping("/comSaveComGoods")
    public R comSaveComGoods(@RequestBody NxCommunityGoodsEntity nxCommunityGoodsEntity) {

        String goodsName = nxCommunityGoodsEntity.getNxCgGoodsName();

        nxCommunityGoodsEntity.setNxCgGoodsStatus(0);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        nxCommunityGoodsEntity.setNxCgGoodsPy(headPinyin);
        nxCommunityGoodsEntity.setNxCgGoodsPinyin(pinyin);
        nxCommunityGoodsEntity.setNxCgNxGoodsId(-1);
        nxCommunityGoodsEntity.setNxCgNxFatherId(-1);
        nxCommunityGoodsEntity.setNxCgNxGrandId(-1);
        nxCommunityGoodsEntity.setNxCgNxGreatGrandId(-1);
        nxCommunityGoodsEntity.setNxCgIsOpenAdsense(0);
        nxCommunityGoodsEntity.setNxCgPromotionType(0);

        BigDecimal goodsPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsPrice());
        BigDecimal fractionalPart = goodsPrice.subtract(goodsPrice.setScale(0, RoundingMode.DOWN)).multiply(new BigDecimal(10)).setScale(0,BigDecimal.ROUND_HALF_UP);
        BigDecimal integerPart = goodsPrice.setScale(0, RoundingMode.DOWN);
        nxCommunityGoodsEntity.setNxCgGoodsPriceInteger(integerPart.toString());
        nxCommunityGoodsEntity.setNxCgGoodsPriceDecimal(fractionalPart.toString());
        if(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice() != null ){
            BigDecimal huaxianPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
            BigDecimal difDec = huaxianPrice.subtract(goodsPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            nxCommunityGoodsEntity.setNxCgGoodsHuaxianPriceDifferent(difDec.toString());
        }

        if(nxCommunityGoodsEntity.getNxCgGoodsType() == 2){
            nxCommunityGoodsEntity.setNxCgBuyingPrice(nxCommunityGoodsEntity.getNxCgGoodsPrice());
            nxCommunityGoodsEntity.setNxCgBuyingPriceExchange(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
        }

        if(nxCommunityGoodsEntity.getNxCgSellType() == 1){
            String cgStartTime = nxCommunityGoodsEntity.getNxCgStartTime();
            String startHour = cgStartTime.substring(0, 2);
            String startMinute = cgStartTime.substring(3, 5);
            BigDecimal hourMinuteStart = new BigDecimal(startHour).multiply(new BigDecimal(60));
            BigDecimal decimalStart = hourMinuteStart.add(new BigDecimal(startMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
            nxCommunityGoodsEntity.setNxCgStartTimeZone(decimalStart.toString());
            String cgStopTime = nxCommunityGoodsEntity.getNxCgStopTime();
            String stopHour = cgStopTime.substring(0, 2);
            String stopMinute = cgStopTime.substring(3, 5);
            BigDecimal hourMinuteStop = new BigDecimal(stopHour).multiply(new BigDecimal(60));
            BigDecimal decimalStop = hourMinuteStop.add(new BigDecimal(stopMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
            nxCommunityGoodsEntity.setNxCgStopTimeZone(decimalStop.toString());

        }else{

            BigDecimal multiply = new BigDecimal(24).multiply(new BigDecimal(60));
            nxCommunityGoodsEntity.setNxCgStopTimeZone(multiply.toString());
            nxCommunityGoodsEntity.setNxCgStartTimeZone("0");
            nxCommunityGoodsEntity.setNxCgStartTime("00:00");
            nxCommunityGoodsEntity.setNxCgStopTime("23:59");

        }


        cgService.save(nxCommunityGoodsEntity);

        List<NxCommunityGoodsSetItemEntity> itemSubEntities = nxCommunityGoodsEntity.getNxCommunityGoodsSetItemEntities();
        if(itemSubEntities.size() > 0){
            for(int i = 0; i < itemSubEntities.size(); i++){
                NxCommunityGoodsSetItemEntity itemEntity = itemSubEntities.get(i);
                itemEntity.setNxCgsiItemSort(i+1);
                itemEntity.setNxCgsiItemStatus(0);
                itemEntity.setNxCgsiItemCgGoodsId(nxCommunityGoodsEntity.getNxCommunityGoodsId());
                nxCommunityGoodsSetItemService.save(itemEntity);
            }
        }

        List<NxCommunityGoodsSetPropertyEntity> nxCgSetPropertyEntities = nxCommunityGoodsEntity.getNxCommunityGoodsSetPropertyEntities();

        if(nxCgSetPropertyEntities.size() > 0){
            for(int i = 0; i < nxCgSetPropertyEntities.size(); i++){
                NxCommunityGoodsSetPropertyEntity itemEntity = nxCgSetPropertyEntities.get(i);
                itemEntity.setNxCgspSort(i+1);
                itemEntity.setNxCgspCgGoodsId(nxCommunityGoodsEntity.getNxCommunityGoodsId());
                nxCgSetPropertyService.save(itemEntity);
            }
        }

        Integer NxCgDfgGoodsFatherId = nxCommunityGoodsEntity.getNxCgCfgGoodsFatherId();
        NxCommunityFatherGoodsEntity fatherGoodsEntity = cfgService.queryObject(NxCgDfgGoodsFatherId);
        fatherGoodsEntity.setNxCfgGoodsAmount(fatherGoodsEntity.getNxCfgGoodsAmount() + 1);
        cfgService.update(fatherGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("goodsId",nxCommunityGoodsEntity.getNxCommunityGoodsId());
        NxCommunityGoodsEntity newCgGoods = cgService.queryComGoodsDetail(map);

        return R.ok().put("data", newCgGoods);

    }


    @RequestMapping(value = "/commGetComAppointSupplierGoods/{supplierId}")
    @ResponseBody
    public R commGetComAppointSupplierGoods(@PathVariable Integer supplierId) {

        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", supplierId);
        List<NxCommunityGoodsEntity> goodsEntities = cgService.queryComGoodsByParams(map);

        return R.ok().put("data", goodsEntities);
    }






    /**
     * @param searchStr 搜索字符串
     * @param comId     批发商id
     * @return 搜索结果
     */
    @RequestMapping(value = "/queryComGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryComGoodsByQuickSearch(String searchStr, String comId) {

        System.out.println(searchStr);
        Map<String, Object> map = new HashMap<>();
        map.put("commId", comId);

        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchStrPinyin", pinyin);
            } else {
                map.put("searchStr", searchStr);
                map.put("searchPinyin", searchStr);
            }
        }

        System.out.println("duopimknidnifndanfisadf" + map);
        List<NxCommunityGoodsEntity> goodsEntities = cgService.queryComGoodsQuickSearchStr(map);
        if (goodsEntities.size() > 0) {
            return R.ok().put("data", goodsEntities);
        }
        return R.error(-1, "没有商品");
    }



    @RequestMapping(value = "/comGetGoodsDetail/{comGoodsId}")
    @ResponseBody
    public R comGetGoodsDetail(@PathVariable Integer comGoodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", comGoodsId);
        System.out.println("deetailalalallala" + map);
        NxCommunityGoodsEntity comGoods = cgService.queryComGoodsDetail(map);
        return R.ok().put("data", comGoods);
    }





    @RequestMapping(value = "/getCommunityGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getCommunityGoodsDetail(Integer goodsId, Integer orderUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderUserId", orderUserId);
        map.put("goodsId", goodsId);
        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryComGoodsDetail(map);
        Map<String, Object> mapC = new HashMap<>();
        mapC.put("userId", orderUserId);
        mapC.put("goodsId", goodsId);
        mapC.put("stopTime", formatWhatDay(0));
        mapC.put("status", 1);
        System.out.println("cccckckkckkckc" + mapC);
        NxCustomerUserCardEntity card = nxCustomerUserCardService.queryUserGoodsCard(mapC);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("goods",communityGoodsEntity);
        mapR.put("card", card);

        //添加商品 Goodsmedia 的列表
        List<NxCommunityGoodsMediaEntity> mediaList = nxCommunityGoodsMediaService.getMediaListByGoodsId(goodsId);
        mapR.put("mediaList", mediaList);
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/getRemarkCommunityGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getRemarkCommunityGoodsDetail(Integer goodsId, Integer orderUserId) {

        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goodsId);
        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryRemarkComGoodsDetail(map);
       if(communityGoodsEntity.getNxCgGoodsType().equals(getNxCommunityGoodsTypeTaocan())){
           List<NxCommunityGoodsSetPropertyEntity> nxCommunityGoodsSetPropertyEntities = communityGoodsEntity.getNxCommunityGoodsSetPropertyEntities();
           for(NxCommunityGoodsSetPropertyEntity propertyEntity: nxCommunityGoodsSetPropertyEntities){
               List<NxCommunityGoodsSetItemEntity> nxCommunityGoodsSetItemEntities = propertyEntity.getNxCommunityGoodsSetItemEntities();
               if(nxCommunityGoodsSetItemEntities.size() > 0){
                   List<NxCommunityGoodsSetItemEntity> updateItemList = new ArrayList<>();
                   for(NxCommunityGoodsSetItemEntity itemEntity: nxCommunityGoodsSetItemEntities){
                       if(itemEntity.getNxCommunityGoodsEntity() != null){
                           NxCommunityGoodsEntity nxCommunityGoodsEntity = itemEntity.getNxCommunityGoodsEntity();
                           Integer nxCommunityGoodsId = nxCommunityGoodsEntity.getNxCommunityGoodsId();

                           Map<String, Object> mapG = new HashMap<>();
                           mapG.put("goodsId", nxCommunityGoodsId);
                           NxCommunityGoodsEntity itemGoods = cgService.queryRemarkComGoodsDetail(mapG);
                           String remark = "";
                           System.out.println("ziahsuisissiissi" + itemGoods.getNxCommunityGoodsSetItemEntities() + "aaa");
                           if(itemGoods.getNxCommunityGoodsSetItemEntities().size() > 0){
                               for(NxCommunityGoodsSetItemEntity item: itemGoods.getNxCommunityGoodsSetItemEntities()){
                                   if(new BigDecimal(item.getNxCgsiItemQuantity()).compareTo(BigDecimal.ZERO) > 0){
                                       remark = remark + item.getNxCgsiItemName() + item.getNxCgsiItemQuantity() + "+";
                                   }
                               }
                               if(remark.length() > 0){
                                   itemGoods.setNxCgGoodsDetail(remark.substring(0, remark.length() - 1));
                               }else {
                                   itemGoods.setNxCgGoodsDetail("");
                               }
                           }
                           itemEntity.setNxCommunityGoodsEntity(itemGoods);
                       }

                   }
               }
           }
       }
        Map<String, Object> mapC = new HashMap<>();
        mapC.put("userId", orderUserId);
        mapC.put("goodsId", goodsId);
        mapC.put("stopTime", formatWhatDay(0));
        mapC.put("status", 1);
        System.out.println("cccckckkckkckc" + mapC);
        NxCustomerUserCardEntity card = nxCustomerUserCardService.queryUserGoodsCard(mapC);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("goods",communityGoodsEntity);
        mapR.put("card", card);
        return R.ok().put("data", mapR);
    }





    @RequestMapping(value = "/updateComGoodsWithFile", method = RequestMethod.POST)
    @ResponseBody
    public R updateComGoodsWithFile(@RequestParam("file") MultipartFile file,
                                    @RequestParam("goodsId") Integer goodsId,
                                    HttpSession session) {
//        //1,上传图片
//        String newUploadName = "images/goodsImage";
//        String realPath = UploadFile.upload(session, newUploadName, file);
//
//        String filename = file.getOriginalFilename();
//        String filePath = newUploadName + "/" + filename;
//
//        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryObject(goodsId);
//        if (communityGoodsEntity.getNxCgNxGoodsFilePath() != null) {
////            ServletContext servletContext = session.getServletContext();
////            String realPath1 = servletContext.getRealPath(communityGoodsEntity.getNxCgNxGoodsFilePath());
//            String EXTERNAL_DATA_DIR = "/opt/tomcat/latest/app-data/images/";
//
//            String realPathOld = EXTERNAL_DATA_DIR + communityGoodsEntity.getNxCgNxGoodsFilePath();
//
//            File file1 = new File(realPathOld);
//            if (file1.exists()) {
//                System.out.println("filslslslls" + realPathOld);
//                file1.delete();
//            }
//        }


        // 1) 定义子目录，如 "goodsImage"
        String subDir = "goodsImage";

        // 2) 上传文件 -> /opt/tomcat/latest/app-data/images/goodsImage/xxx.jpg
        UploadFile.upload(session, subDir, file);

        // 3) 构造数据库要保存的相对路径 (goodsImage/xxx.jpg)
        String filename = file.getOriginalFilename();
        String filePath = subDir + "/" + filename;

        // 4) 删除旧文件（若已有旧路径）
        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryObject(goodsId);
        String oldPath = communityGoodsEntity.getNxCgNxGoodsFilePath();
        if (oldPath != null && !oldPath.trim().isEmpty()) {
            // 旧文件绝对路径
            // oldPath 类似 "goodsImage/oldName.jpg"
            String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
            File oldFile = new File(oldAbsolutePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }

        // 5) 更新数据库记录
        communityGoodsEntity.setNxCgNxGoodsFilePath(filePath);
        cgService.update(communityGoodsEntity);


        return R.ok();
    }


    @RequestMapping(value = "/updateComGoodsWithFileTop", method = RequestMethod.POST)
    @ResponseBody
    public R updateComGoodsWithFileTop(@RequestParam("file") MultipartFile file,
                                    @RequestParam("goodsId") Integer goodsId,
                                    HttpSession session) {

//        session.getServletContext();
        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryObject(goodsId);
        String oldPath = communityGoodsEntity.getNxCgNxGoodsTopFilePath();
        if (oldPath != null && !oldPath.trim().isEmpty()) {
            String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
            File file1 = new File(oldAbsolutePath);
            if (file1.exists()) {
                file1.delete();
            }
        }

        //1,上传图片
        String newUploadName = "goodsImage";
        String realPath = UploadFile.upload(session, newUploadName, file);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;
        communityGoodsEntity.setNxCgNxGoodsTopFilePath(filePath);
        cgService.update(communityGoodsEntity);

        if(communityGoodsEntity.getNxCgIsOpenAdsense() == 1){
            Map<String, Object> map = new HashMap<>();
            map.put("goodsId", goodsId);
            NxCommunityAdsenseEntity communityAdsenseEntity = nxCommunityAdsenseService.queryGoodsAdsenseByParams(map);
            communityAdsenseEntity.setNxCaFilePath(filePath);
            nxCommunityAdsenseService.update(communityAdsenseEntity);
        }


        return R.ok();
    }

    /**
     * 上传社区商品介绍视频（与顶部图接口类似，文件保存在 goodsVideo/ 下）
     */
    @RequestMapping(value = "/updateComGoodsWithIntroVideo", method = RequestMethod.POST)
    @ResponseBody
    public R updateComGoodsWithIntroVideo(@RequestParam("file") MultipartFile file,
                                          @RequestParam("goodsId") Integer goodsId,
                                          HttpSession session) {
        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryObject(goodsId);
        String oldPath = communityGoodsEntity.getNxCgGoodsIntroVideo();
        if (oldPath != null && !oldPath.trim().isEmpty()) {
            String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
            File oldFile = new File(oldAbsolutePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }

        String subDir = "goodsVideo";
        UploadFile.upload(session, subDir, file);
        String filename = file.getOriginalFilename();
        String filePath = subDir + "/" + filename;
        communityGoodsEntity.setNxCgGoodsIntroVideo(filePath);
        cgService.update(communityGoodsEntity);

        return R.ok();
    }

    @RequestMapping(value = "/getCommunityGoodsByFatherId/{fatherId}")
    @ResponseBody
    public R getCommunityGoodsByFatherId(@PathVariable Integer fatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);
        System.out.println("getCommunityGoodsByFatherIdssss" + map);

        //查询列表数据
        List<NxCommunityGoodsEntity> dgGoodsLit = cgService.queryCommunityGoods(map);

        return R.ok().put("data", dgGoodsLit);
    }




    /**
     * 信息
     */
    @ResponseBody
    @RequestMapping("/info/{cgGoodsId}")
//    @RequiresPermissions("nxCommunityGoodsEntity:info")
    public R info(@PathVariable("cgGoodsId") Integer cgGoodsId) {
        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryObject(cgGoodsId);

        return R.ok().put("data", communityGoodsEntity);
    }

    /**
     * 获取商品的图片列表
     */
    @RequestMapping(value = "/getGoodsMediaList/{goodsId}")
    @ResponseBody
    public R getGoodsMediaList(@PathVariable("goodsId") Integer goodsId) {
        List<NxCommunityGoodsMediaEntity> mediaList = nxCommunityGoodsMediaService.getMediaListByGoodsId(goodsId);
        return R.ok().put("data", mediaList);
    }

    /**
     * 获取商品主图
     */
    @RequestMapping(value = "/getGoodsPrimaryMedia/{goodsId}")
    @ResponseBody
    public R getGoodsPrimaryMedia(@PathVariable("goodsId") Integer goodsId) {
        NxCommunityGoodsMediaEntity primaryMedia = nxCommunityGoodsMediaService.getPrimaryMedia(goodsId);
        return R.ok().put("data", primaryMedia);
    }


}
