package com.nongxinle.service.impl;

import com.nongxinle.dao.GbDepartmentGoodsStockDao;
import com.nongxinle.dao.GbDistributerFatherGoodsDao;
import com.nongxinle.dao.GbDistributerPurchaseGoodsDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.GbDepartmentOrdersService;
import com.nongxinle.service.GbDistributerFatherGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerGoodsDao;
import com.nongxinle.service.GbDistributerGoodsService;

import com.nongxinle.service.GbDistributerAliasService;
import com.nongxinle.service.GbDistributerFatherGoodsService;
import com.nongxinle.service.GbDistributerStandardService;
import com.nongxinle.service.NxAliasService;
import com.nongxinle.service.NxGoodsService;
import com.nongxinle.service.NxStandardService;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@Service("gbDistributerGoodsService")
public class GbDistributerGoodsServiceImpl implements GbDistributerGoodsService {
    @Autowired
    private GbDistributerGoodsDao gbDistributerGoodsDao;
    @Autowired
    private GbDepartmentGoodsStockDao gbDepartmentGoodsStockDao;
    @Autowired
    private GbDistributerPurchaseGoodsDao gbDistributerPurchaseGoodsDao;

    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxAliasService nxAliasService;
    @Autowired
    private NxStandardService nxStandardService;
    @Autowired
    private GbDistributerFatherGoodsService gbDistributerFatherGoodsService;
    @Autowired
    private GbDistributerAliasService gbDistributerAliasService;
    @Autowired
    private GbDistributerStandardService gbDistributerStandardService;

    @Override
    public GbDistributerGoodsEntity queryObject(Integer gbDistributerGoodsId) {
        return gbDistributerGoodsDao.queryObject(gbDistributerGoodsId);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryList(Map<String, Object> map) {
        return gbDistributerGoodsDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return gbDistributerGoodsDao.queryTotal(map);
    }

    @Override
    public void save(GbDistributerGoodsEntity gbDistributerGoods) {
        gbDistributerGoodsDao.save(gbDistributerGoods);
    }

    @Override
    public void update(GbDistributerGoodsEntity gbDistributerGoods) {
        gbDistributerGoodsDao.update(gbDistributerGoods);
    }

    @Override
    public int delete(Integer gbDistributerGoodsId) {
        gbDistributerGoodsDao.delete(gbDistributerGoodsId);
        return 1;
    }

    @Override
    public void deleteBatch(Integer[] gbDistributerGoodsIds) {
        gbDistributerGoodsDao.deleteBatch(gbDistributerGoodsIds);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryGoodsByParamsGb(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryGoodsByParamsGb(map);
    }

    @Override
    public int queryGbGoodsTotal(Map<String, Object> map3) {

        return gbDistributerGoodsDao.queryGbGoodsTotal(map3);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryDisGoodsByParams(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryAddDistributerNxGoods(Map<String, Object> map) {
        return gbDistributerGoodsDao.queryAddDistributerNxGoods(map);
    }


    @Override
    public GbDistributerGoodsEntity queryGbDisGoodsDetail(Integer disGoodsId) {

        return gbDistributerGoodsDao.queryGbDisGoodsDetail(disGoodsId);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDgSubNameByFatherIdGb(Integer gbDistributerFatherGoodsId) {
        return gbDistributerGoodsDao.queryDgSubNameByFatherIdGb(gbDistributerFatherGoodsId);
    }


    @Override
    public List<GbDistributerGoodsEntity> queryGbDisGoodsQuickSearchStr(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryGbDisGoodsQuickSearchStr(map);
    }

    @Override
    public int queryGbStockGoodsTotal(Map<String, Object> map3) {

        return gbDistributerGoodsDao.queryGbStockGoodsTotal(map3);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDisFatherGoodsByParams(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryDisFatherGoodsByParams(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisShelfGoodsWithParams(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryDisShelfGoodsWithParams(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryGbDisUnShlefGoodsQuickSearchStr(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryGbDisUnShlefGoodsQuickSearchStr(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> querySubNameByFatherId(Integer gbDistributerFatherGoodsId) {

        return gbDistributerGoodsDao.querySubNameByFatherId(gbDistributerFatherGoodsId);
    }


    @Override
    public int queryDisGoodsCount(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryDisGoodsCount(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryPurchaserDisGoodsByParams(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryPurchaserDisGoodsByParams(map);
    }

    @Override
    public GbDistributerGoodsEntity queryDisGoodsWithDepDisGoods(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryDisGoodsWithDepDisGoods(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryUpdateGoodsByParams(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryUpdateGoodsByParams(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsWithShelfGoods(Integer depId) {

        return gbDistributerGoodsDao.queryDisGoodsWithShelfGoods(depId);
    }

    @Override
    public List<GbDistributerGoodsEntity> querydisGoodsByNxGoodsId(Integer nxGoodsId) {

        return gbDistributerGoodsDao.querydisGoodsByNxGoodsId(nxGoodsId);
    }

    @Override
    public List<GbDistributerEntity> queryGbDisByNxGoodsId(Integer nxGoodsId) {

        return gbDistributerGoodsDao.queryGbDisByNxGoodsId(nxGoodsId);
    }

    @Override
    public GbDistributerGoodsEntity queryGbGoodsByNxGoodsId(Integer lsGoodsId) {

        return gbDistributerGoodsDao.queryGbGoodsByNxGoodsId(lsGoodsId);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsQuickSearchStrWithDepOrdersGb(Map<String, Object> map) {
        return gbDistributerGoodsDao.queryDisGoodsQuickSearchStrWithDepOrdersGb(map);
    }

    @Override
    public List<GbDepartmentEntity> queryOutDepsByFatherId(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryOutDepsByFatherId(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> querySupplierGoodsByGreatIdGb(Map<String, Object> map) {

        return gbDistributerGoodsDao.querySupplierGoodsByGreatIdGb(map);
    }

    @Override
    public int queryLinshiGoodsAcount(Integer gbDistributerId) {

        return gbDistributerGoodsDao.queryLinshiGoodsAcount(gbDistributerId);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsByName(Map<String, Object> mapZero) {

        return gbDistributerGoodsDao.queryDisGoodsByName(mapZero);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsByLikeName(Map<String, Object> mapOne) {

        return gbDistributerGoodsDao.queryDisGoodsByLikeName(mapOne);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsByNamePinyin(Map<String, Object> mapTwo) {

        return gbDistributerGoodsDao.queryDisGoodsByNamePinyin(mapTwo);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsByAlias(Map<String, Object> mapA) {
        return gbDistributerGoodsDao.queryDisGoodsByAlias(mapA);
    }

    @Override
    public GbDistributerGoodsEntity getTipText(GbDistributerGoodsEntity gbDistributerGoodsEntity) {




        Integer gid = gbDistributerGoodsEntity.getGbDistributerGoodsId();
        Integer disId = gbDistributerGoodsEntity.getGbDgDistributerId(); // 比如"件"或"斤"等
        Map<String, Object> mapResult = new HashMap<>();

//
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", gid);

        Integer integerS = gbDepartmentGoodsStockDao.queryGoodsStockCount(map);
        if (integerS > 0) {
            System.out.println("sttoass" + map);
            Double aDouble = gbDepartmentGoodsStockDao.queryDepGoodsRestWeightTotal(map);
            Double aDoubleS = gbDepartmentGoodsStockDao.queryDepGoodsRestTotal(map);
            System.out.println("jeiguogoguogg" + aDouble);
            gbDistributerGoodsEntity.setGoodsStockWeightTotalString(String.format("%.1f", aDouble));
            gbDistributerGoodsEntity.setGoodsStockTotalString(String.format("%.1f", aDoubleS));
        } else {
            gbDistributerGoodsEntity.setGoodsStockWeightTotalString("0");
            gbDistributerGoodsEntity.setGoodsStockTotalString("0");

        }

        LocalDate today = LocalDate.now();

        //
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        map.put("startDate", firstDayOfMonth);
        map.put("stopDate", formatWhatDay(0));
        System.out.println("mapmannd" + map);
        Integer integer = gbDepartmentGoodsStockDao.queryGoodsStockCount(map);
        System.out.println("inidididid" + integer);
        Double aDouble = 0.0;
        if (integer > 0) {
            mapResult.put("code", 0);
            System.out.println("inidididid" + integer);
//             - 本月总成本：
            aDouble = gbDepartmentGoodsStockDao.queryDepGoodsSubtotal(map);
            mapResult.put("thisMonthSubtotal", String.format("%.1f", aDouble));


            Integer gbDgDfgGoodsGrandId = gbDistributerGoodsEntity.getGbDgDfgGoodsGrandId();
            GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(gbDgDfgGoodsGrandId);
            map.put("disGoodsId", null);
            map.put("disGoodsGrandId", gbDgDfgGoodsGrandId);
            System.out.println("granddndndnndnddn" + map);
            Double grandDouble = gbDepartmentGoodsStockDao.queryDepGoodsSubtotal(map);
            //- 占叶花菜类总成本：25%
            BigDecimal grandPercent = BigDecimal.valueOf(aDouble)
                    .divide(BigDecimal.valueOf(grandDouble), 4, BigDecimal.ROUND_HALF_UP)  // 先保留4位小数避免精度丢失
                    .multiply(BigDecimal.valueOf(100))                                      // 转换为百分比
                    .setScale(2, BigDecimal.ROUND_HALF_UP);                                  // 最后保留2位小数
            mapResult.put("grandPercent", grandPercent);
            mapResult.put("grandName", fatherGoodsEntity.getGbDfgFatherGoodsName());

//            占全店总采购成本：8%
            map.put("disGoodsGrandId", null);
            map.put("disId", disId);
            System.out.println("sttorororosbsuus" + map);
            Double storeDouble = gbDepartmentGoodsStockDao.queryDepGoodsSubtotal(map);

            System.out.println("stororos" + storeDouble);
            BigDecimal storePercent = BigDecimal.valueOf(aDouble)
                    .divide(BigDecimal.valueOf(storeDouble), 4, BigDecimal.ROUND_HALF_UP)  // 先保留4位小数避免精度丢失
                    .multiply(BigDecimal.valueOf(100))                                      // 转换为百分比
                    .setScale(2, BigDecimal.ROUND_HALF_UP);                                  // 最后保留2位小数
            mapResult.put("storePercent", storePercent);


           //- 当前采购价：2.0元/斤
            map.put("disGoodsId", gid);
            map.put("startDate", null);
            map.put("stopDate", null);
            System.out.println("puriciieiieieei" + map);
           GbDistributerPurchaseGoodsEntity purchaseGoodsEntity =  gbDistributerPurchaseGoodsDao.queryLastestItem(map);
            mapResult.put("purPrice", purchaseGoodsEntity.getGbDpgBuyPrice());


            //- 最近30天均价：1.7元/斤（↑+17%）


            map.put("startDate", formatWhatDay(-30));
            Double subTotal = gbDistributerPurchaseGoodsDao.queryPurchaseGoodsSubTotal(map);
            Double  weightTotal = gbDistributerPurchaseGoodsDao.queryPurchaseGoodsWeightTotal(map);
            BigDecimal bigDecimal = new BigDecimal(subTotal).divide(new BigDecimal(weightTotal), 2, BigDecimal.ROUND_HALF_UP).setScale(1, BigDecimal.ROUND_HALF_UP);
            mapResult.put("thirtyPurPrice", bigDecimal.toString());


            //- 历史损耗率：28%

            System.out.println("lsihissusnshs" + map);
            Double aDouble1 = gbDepartmentGoodsStockDao.queryDepStockLossWeightTotal(map);
            Double aDouble2 = gbDepartmentGoodsStockDao.queryDepStockWeightTotal(map);
            BigDecimal lossPercent = BigDecimal.valueOf(aDouble1)
                    .divide(BigDecimal.valueOf(aDouble2), 4, BigDecimal.ROUND_HALF_UP)  // 先保留4位小数避免精度丢失
                    .multiply(BigDecimal.valueOf(100))                                      // 转换为百分比
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            mapResult.put("lossPercent", lossPercent.toString());


        } else {
            mapResult.put("code", -1);
        }


        gbDistributerGoodsEntity.setGoodsData(mapResult);


        return gbDistributerGoodsEntity;
    }

    @Override
    public List<Integer> queryOnlyDisGoodsIds(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryOnlyDisGoodsIds(map);
    }

    @Override
    public GbDistributerGoodsEntity queryDisGoodsDetail(Integer nxDdgDisGoodsId) {

        return gbDistributerGoodsDao.queryDisGoodsDetail(nxDdgDisGoodsId);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsByNameLikePinyin(Map<String, Object> mapTwo) {

        return gbDistributerGoodsDao.queryDisGoodsByNameLikePinyin(mapTwo);

    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsByAliasLike(Map<String, Object> mapTwo) {

        return gbDistributerGoodsDao.queryDisGoodsByAliasLike(mapTwo);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsQuickSearchStrWithDepOrders(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryDisGoodsQuickSearchStrWithDepOrders(map);
    }

    @Override
    public void getStockTotal(GbDistributerGoodsEntity distributerGoodsEntity) {

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());

        Integer integerS = gbDepartmentGoodsStockDao.queryGoodsStockCount(map);
        if (integerS > 0) {
            System.out.println("sttoass" + map);
            Double aDouble = gbDepartmentGoodsStockDao.queryDepGoodsRestWeightTotal(map);
            Double aDoubleS = gbDepartmentGoodsStockDao.queryDepGoodsRestTotal(map);
            System.out.println("jeiguogoguogg" + aDouble);
            distributerGoodsEntity.setGoodsStockWeightTotalString(String.format("%.1f", aDouble));
            distributerGoodsEntity.setGoodsStockTotalString(String.format("%.1f", aDoubleS));
        } else {
            distributerGoodsEntity.setGoodsStockWeightTotalString("0");
            distributerGoodsEntity.setGoodsStockTotalString("0");

        }

    }


    /**
     * 设置AI相关字段
     */
    private void setAiFields(GbDistributerGoodsEntity entity,
                             double onHand,
                             double mu,
                             double recentAvg,
                             double cv,
                             double safetyStock,
                             double reorderPoint,
                             LocalDate lastDate,
                             double lastOrderQty,
                             GbDepartmentOrdersEntity lo,
                             long daysSince,
                             double tomorrowNeed,
                             int orderQty) {

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", entity.getGbDistributerGoodsId());
        Integer integer = gbDepartmentGoodsStockDao.queryGoodsStockCount(map);
        if (integer > 0) {
            Double aDouble = gbDepartmentGoodsStockDao.queryDepStockWeightTotal(map);
            System.out.println("sttoass" + aDouble);
            entity.setGoodsStockWeightTotalString(String.format("%.2f", aDouble));
        } else {
            entity.setGoodsStockWeightTotalString("0");
        }

        entity.setAiCurrentStock(String.format("%.2f", onHand));
        entity.setAiCurrentStockUnit(entity.getGbDgGoodsStandardname());
        entity.setAiDailyUsage(String.format("%.2f", mu));
        entity.setAiRecentAvgUsage(String.format("%.2f", recentAvg));
        entity.setAiUsageVariation(String.format("%.2f", cv));
        entity.setAiSafetyStock(String.format("%.2f", safetyStock));
        entity.setAiReorderPoint(String.format("%.2f", reorderPoint));
        entity.setAiLastOrderDate(lastDate != null ? lastDate.toString() : "");
        entity.setAiLastOrderQuantity(String.format("%.2f", lastOrderQty));
        entity.setAiLastOrderUnit(lo != null ? lo.getGbDoStandard() : entity.getGbDgGoodsStandardname());
        entity.setAiDaysSinceLastOrder(String.valueOf(daysSince));
        entity.setAiTomorrowNeed(String.format("%.2f", tomorrowNeed));
        entity.setAiAvailableDays(String.format("%.1f", mu > 0 ? onHand / mu : 0.0));
        entity.setAiOrderQuantity(String.valueOf(orderQty));
        entity.setAiOrderStandard(entity.getGbDgGoodsStandardname());


    }




    @Override
    public GbDistributerGoodsEntity postDgnGbGoods(Integer gbDisId, Integer depId, Integer nxGoodsId) {

        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(nxGoodsId);
        GbDistributerGoodsEntity cgnGoods = new GbDistributerGoodsEntity();
        cgnGoods.setGbDgGoodsName(nxGoodsEntity.getNxGoodsName());
        cgnGoods.setGbDgGoodsStandardname(nxGoodsEntity.getNxGoodsStandardname());
        cgnGoods.setGbDgGoodsPy(nxGoodsEntity.getNxGoodsPy());
        cgnGoods.setGbDgGoodsPinyin(nxGoodsEntity.getNxGoodsPinyin());
        cgnGoods.setGbDgGoodsStandardWeight(nxGoodsEntity.getNxGoodsStandardWeight());
        cgnGoods.setGbDgGoodsDetail(nxGoodsEntity.getNxGoodsDetail());
        cgnGoods.setGbDgGoodsBrand(nxGoodsEntity.getNxGoodsBrand());
        cgnGoods.setGbDgGoodsPlace(nxGoodsEntity.getNxGoodsPlace());
        cgnGoods.setGbDgGoodsSort(nxGoodsEntity.getNxGoodsSort());
        cgnGoods.setGbDgGoodsSonsSort(nxGoodsEntity.getNxGoodsSonsSort());
        cgnGoods.setGbDgNxFatherImgLarge(nxGoodsEntity.getNxGoodsFileBig());
        cgnGoods.setGbDgNxFatherImg(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setGbDgNxGoodsFatherColor(nxGoodsEntity.getColor());
        cgnGoods.setGbDgDistributerId(gbDisId);
        cgnGoods.setGbDgGoodsStatus(0);
        cgnGoods.setGbDgGoodsIsWeight(0);
        cgnGoods.setGbDgGoodsIsHidden(0);
        cgnGoods.setGbDgNxGoodsId(nxGoodsEntity.getNxGoodsId());
        cgnGoods.setGbDgNxFatherId(nxGoodsEntity.getNxGoodsFatherId());
        cgnGoods.setGbDgNxGrandId(nxGoodsEntity.getNxGoodsGrandId());
        cgnGoods.setGbDgPullOff(0);
        cgnGoods.setGbDgGoodsType(2);
        cgnGoods.setGbDgGbSupplierId(-1);
        cgnGoods.setGbDgNxDistributerId(-1);
        cgnGoods.setGbDgNxDistributerGoodsId(-1);
        cgnGoods.setGbDgNxDistributerGoodsPrice("0.1");
        cgnGoods.setGbDgGbDepartmentId(depId);
        cgnGoods.setGbDgControlFresh(0);
        cgnGoods.setGbDgControlPrice(0);
        cgnGoods.setGbDgGoodsInventoryType(1);
        cgnGoods.setGbDgIsFranchisePrice(0);
        cgnGoods.setGbDgIsSelfControl(0);
        cgnGoods.setGbDgNxFatherImg(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setGbDgNxFatherImgLarge(nxGoodsEntity.getNxGoodsFileBig());

        GbDistributerGoodsEntity disGoods = saveDisGoods(cgnGoods);

        //2.2
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", nxGoodsId);
        List<NxAliasEntity> aliasEntities = nxAliasService.queryNxAliasList(map);
        if (aliasEntities.size() > 0) {
            for (NxAliasEntity aliasEntity : aliasEntities) {
                GbDistributerAliasEntity disAlias = new GbDistributerAliasEntity();
                disAlias.setGbDaDisGoodsId(disGoods.getGbDistributerGoodsId());
                disAlias.setGbDaAliasName(aliasEntity.getNxAliasName());
                gbDistributerAliasService.save(disAlias);
            }
        }

        List<NxStandardEntity> nxStandardEntities = nxStandardService.queryGoodsStandardListByGoodId(nxGoodsId);
        if (nxStandardEntities.size() > 0) {
            for (NxStandardEntity standardEntity : nxStandardEntities) {
                GbDistributerStandardEntity distributerStandardEntity = new GbDistributerStandardEntity();
                distributerStandardEntity.setGbDsDisGoodsId(disGoods.getGbDistributerGoodsId());
                distributerStandardEntity.setGbDsStandardName(standardEntity.getNxStandardName());
                gbDistributerStandardService.save(distributerStandardEntity);
            }
        }

        return disGoods;
    }
    private GbDistributerGoodsEntity saveDisGoods(GbDistributerGoodsEntity cgnGoods) {

        Integer nxDgNxGoodsId = cgnGoods.getGbDgNxGoodsId();
        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(nxDgNxGoodsId);

        cgnGoods.setGbDgGoodsName(nxGoodsEntity.getNxGoodsName());
        cgnGoods.setGbDgNxFatherImg(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setGbDgGoodsStandardname(nxGoodsEntity.getNxGoodsStandardname());
        cgnGoods.setGbDgGoodsDetail(nxGoodsEntity.getNxGoodsDetail());
        cgnGoods.setGbDgGoodsPlace(nxGoodsEntity.getNxGoodsPlace());
        cgnGoods.setGbDgGoodsBrand(nxGoodsEntity.getNxGoodsBrand());
        cgnGoods.setGbDgGoodsStandardWeight(nxGoodsEntity.getNxGoodsStandardWeight());
        cgnGoods.setGbDgGoodsPinyin(nxGoodsEntity.getNxGoodsPinyin());
        cgnGoods.setGbDgGoodsPy(nxGoodsEntity.getNxGoodsPy());
        cgnGoods.setGbDgPullOff(0);
        cgnGoods.setGbDgGoodsStatus(1);
        cgnGoods.setNxStandardEntities(nxGoodsEntity.getNxGoodsStandardEntities());
        cgnGoods.setGbDgNxFatherId(nxGoodsEntity.getNxGoodsFatherId());
        cgnGoods.setGbDgNxFatherName(nxGoodsEntity.getFatherGoods().getNxGoodsName());
        cgnGoods.setGbDgNxFatherImg(nxGoodsEntity.getFatherGoods().getNxGoodsFile());
        cgnGoods.setGbDgNxGrandName(nxGoodsEntity.getGrandGoods().getNxGoodsName());
        cgnGoods.setGbDgNxGrandId(nxGoodsEntity.getGrandGoods().getNxGoodsId());
        cgnGoods.setGbDgNxFatherImg(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setGbDgNxFatherImgLarge(nxGoodsEntity.getNxGoodsFileBig());
        cgnGoods.setGbDgGoodsSort(nxGoodsEntity.getNxGoodsSort());
        cgnGoods.setGbDgGoodsSonsSort(nxGoodsEntity.getNxGoodsSonsSort());
        cgnGoods.setGbDgIsFranchisePrice(0);
        cgnGoods.setGbDgIsSelfControl(0);
        cgnGoods.setGbDgGoodsIsHidden(0);
        cgnGoods.setGbDgGoodsType(2);
        cgnGoods.setGbDgGoodsIsWeight(0);
        cgnGoods.setGbDgNxDistributerId(-1);
        cgnGoods.setGbDgNxDistributerGoodsId(-1);
        cgnGoods.setGbDgGoodsInventoryType(1);
        cgnGoods.setGbDgGbSupplierId(-1);
        cgnGoods.setGbDgNxDistributerGoodsPrice("0.1");
        cgnGoods.setGbDgQuantityDays(nxGoodsEntity.getNxGoodsQuantityDays());


        //queryGrandFatherId
        NxGoodsEntity fatherEntity = nxGoodsService.queryObject(cgnGoods.getGbDgNxFatherId());
        Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
        cgnGoods.setGbDgNxGrandId(grandFatherId);
        NxGoodsEntity grandEntity = nxGoodsService.queryObject(grandFatherId);
        cgnGoods.setGbDgNxGrandName(grandEntity.getNxGoodsName());

        //queryGreatGrandFatherId
        Integer nxGreatGrandFatherId = grandEntity.getNxGoodsFatherId();
        if (nxGreatGrandFatherId.equals(1)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#20afb8");
        }
        if (nxGreatGrandFatherId.equals(2)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#f5c832");
        }
        if (nxGreatGrandFatherId.equals(3)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#3cc36e");
        }
        if (nxGreatGrandFatherId.equals(4)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#f09628");
        }
        if (nxGreatGrandFatherId.equals(5)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#1ebaee");
        }
        if (nxGreatGrandFatherId.equals(6)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#f05a32");
        }
        if (nxGreatGrandFatherId.equals(7)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#c0a6dd");
        }
        if (nxGreatGrandFatherId.equals(8)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#969696");
        }
        if (nxGreatGrandFatherId.equals(9)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#318666");
        }
        if (nxGreatGrandFatherId.equals(10)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#026bc2");
        }
        if (nxGreatGrandFatherId.equals(11)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#06eb6d");
        }
        if (nxGreatGrandFatherId.equals(12)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#0690eb");
        }

        cgnGoods.setGbDgNxGreatGrandId(nxGreatGrandFatherId);
        cgnGoods.setGbDgNxGreatGrandName(nxGoodsService.queryObject(nxGreatGrandFatherId).getNxGoodsName());

        Integer GbDgDistributerId = cgnGoods.getGbDgDistributerId();

        // 3， 查询父类
        Integer GbDgNxFatherId = cgnGoods.getGbDgNxFatherId();
        Map<String, Object> map11 = new HashMap<>();
        map11.put("disId", GbDgDistributerId);
        map11.put("nxFatherId", GbDgNxFatherId);
        System.out.println("faehrhrhehehemap" + map11);
        List<GbDistributerGoodsEntity> nxDistributerGoodsEntities = this.queryDisGoodsByParams(map11);

        if (nxDistributerGoodsEntities.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            GbDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsEntities.get(0);
            Integer nxDgDfgGoodsFatherId1 = disGoodsEntity.getGbDgDfgGoodsFatherId();
            Integer nxDgDfgGoodsGrandId = disGoodsEntity.getGbDgDfgGoodsGrandId();
            Integer nxDgDfgGoodsGreatId = disGoodsEntity.getGbDgDfgGoodsGreatId();

            GbDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(nxDgDfgGoodsFatherId1);
            Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
            nxDistributerFatherGoodsEntity.setGbDfgGoodsAmount(nxDfgGoodsAmount + 1);
            gbDistributerFatherGoodsService.update(nxDistributerFatherGoodsEntity);

            //2，保存disId商品
            Integer nxDgDfgGoodsFatherId = disGoodsEntity.getGbDgDfgGoodsFatherId();
            cgnGoods.setGbDgDfgGoodsFatherId(nxDgDfgGoodsFatherId);
            cgnGoods.setGbDgDfgGoodsGrandId(nxDgDfgGoodsGrandId);
            cgnGoods.setGbDgDfgGoodsGreatId(nxDgDfgGoodsGreatId);

            System.out.println("cngnndg" + cgnGoods.getGbDgDfgGoodsGrandId());
            //1 ，先保存disGoods
            this.save(cgnGoods);
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
            dgf.setGbDfgFatherGoodsImgLarge(cgnGoods.getGbDgNxFatherImgLarge());
            dgf.setGbDfgFatherGoodsSort(nxGoodsEntity.getFatherGoods().getNxGoodsSort());
            dgf.setGbDfgNxGoodsId(cgnGoods.getGbDgNxFatherId());
            gbDistributerFatherGoodsService.save(dgf);
            //更新disGoods的fatherGoodsId
            Integer distributerFatherGoodsId = dgf.getGbDistributerFatherGoodsId();
            cgnGoods.setGbDgDfgGoodsFatherId(distributerFatherGoodsId);

            //todo
//            cgnGoods.setGbDgDfgGoodsGrandId(dgf.getGbDfgFathersFatherId());
////
//            GbDistributerFatherGoodsEntity grandFather = gbDistributerFatherGoodsService.queryObject(dgf.getGbDfgFathersFatherId());
//            cgnGoods.setGbDgDfgGoodsGreatId(grandFather.getGbDfgFathersFatherId());

            System.out.println("zizin" + dgf.getGbDfgFathersFatherId());
            this.save(cgnGoods);
            //继续查询是否有GrandFather

            String grandName = cgnGoods.getGbDgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", GbDgDistributerId);
            map2.put("fathersFatherName", grandName);
            List<GbDistributerFatherGoodsEntity> grandGoodsFather = gbDistributerFatherGoodsService.queryHasDisFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = grandGoodsFather.get(0);
                Integer nxDfgGoodsAmount = dgf.getGbDfgGoodsAmount();
                dgf.setGbDfgGoodsAmount(nxDfgGoodsAmount + 1);
                dgf.setGbDfgFathersFatherId(gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId());
                gbDistributerFatherGoodsService.update(dgf);

                Integer nxDfgFathersFatherId = gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId();
                cgnGoods.setGbDgDfgGoodsGrandId(nxDfgFathersFatherId);
                GbDistributerFatherGoodsEntity great = gbDistributerFatherGoodsService.queryObject(nxDfgFathersFatherId);
                cgnGoods.setGbDgDfgGoodsGreatId(great.getGbDfgFathersFatherId());
                this.update(cgnGoods);



            } else {
                //tianjiaGrand
                GbDistributerFatherGoodsEntity grand = new GbDistributerFatherGoodsEntity();
                String nxCgGrandFatherName = cgnGoods.getGbDgNxGrandName();
                grand.setGbDfgFatherGoodsName(nxCgGrandFatherName);
                grand.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
                grand.setGbDfgFatherGoodsLevel(1);
                grand.setGbDfgGoodsAmount(1);
                grand.setGbDfgFatherGoodsColor(cgnGoods.getGbDgNxGoodsFatherColor());
                grand.setGbDfgNxGoodsId(cgnGoods.getGbDgNxGrandId());
                NxGoodsEntity nxGrand = nxGoodsService.queryObject(cgnGoods.getGbDgNxGrandId());
                grand.setGbDfgFatherGoodsImg(nxGrand.getNxGoodsFile());
                grand.setGbDfgFatherGoodsImgLarge(nxGrand.getNxGoodsFileBig());
                System.out.println("nxgoodsnxgoods====" + nxGrand.getNxGoodsId() + "sort==" + nxGrand.getNxGoodsSort());
                grand.setGbDfgFatherGoodsSort(nxGrand.getNxGoodsSort());
                gbDistributerFatherGoodsService.save(grand);

                dgf.setGbDfgFathersFatherId(grand.getGbDistributerFatherGoodsId());
                gbDistributerFatherGoodsService.update(dgf);

                cgnGoods.setGbDgDfgGoodsGrandId(grand.getGbDistributerFatherGoodsId());
                this.update(cgnGoods);

                //查询是否有greatGrand
                Map<String, Object> map3 = new HashMap<>();
                map3.put("disId", GbDgDistributerId);
                String greatGrandName = cgnGoods.getGbDgNxGreatGrandName();
                map3.put("fathersFatherName", greatGrandName);
                List<GbDistributerFatherGoodsEntity> greatGrandGoodsFather = gbDistributerFatherGoodsService.queryHasDisFathersFather(map3);

                if (greatGrandGoodsFather.size() > 0) {
                    GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = greatGrandGoodsFather.get(0);
                    Integer disFatherId = gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId();
                    grand.setGbDfgFathersFatherId(disFatherId);
                    Integer gbDfgGoodsAmount = grand.getGbDfgGoodsAmount();
                    grand.setGbDfgGoodsAmount(gbDfgGoodsAmount + 1);
                    gbDistributerFatherGoodsService.update(grand);

                    cgnGoods.setGbDgDfgGoodsGreatId(disFatherId);
                    this.update(cgnGoods);

                } else {
                    GbDistributerFatherGoodsEntity greatGrand = new GbDistributerFatherGoodsEntity();
                    NxGoodsEntity greatGrandEntity = nxGoodsService.queryObject(cgnGoods.getGbDgNxGreatGrandId());
                    String greatGrandName1 = cgnGoods.getGbDgNxGreatGrandName();
                    greatGrand.setGbDfgFatherGoodsName(greatGrandName1);
                    greatGrand.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
                    greatGrand.setGbDfgFatherGoodsImg(greatGrandEntity.getNxGoodsFile());
                    greatGrand.setGbDfgFatherGoodsImgLarge(greatGrandEntity.getNxGoodsFileBig());
                    greatGrand.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
                    greatGrand.setGbDfgFatherGoodsLevel(0);
                    greatGrand.setGbDfgFathersFatherId(0);
                    greatGrand.setGbDfgFatherGoodsColor(cgnGoods.getGbDgNxGoodsFatherColor());
                    greatGrand.setGbDfgNxGoodsId(cgnGoods.getGbDgNxGreatGrandId());
                    greatGrand.setGbDfgFatherGoodsSort(greatGrandEntity.getNxGoodsSort());
                    greatGrand.setGbDfgGoodsAmount(1);
                    gbDistributerFatherGoodsService.save(greatGrand);

                    grand.setGbDfgFathersFatherId(greatGrand.getGbDistributerFatherGoodsId());
                    gbDistributerFatherGoodsService.update(grand);

                    cgnGoods.setGbDgDfgGoodsGreatId(greatGrand.getGbDistributerFatherGoodsId());
                    this.update(cgnGoods);
                }
            }
        }


        return cgnGoods;
    }

}
