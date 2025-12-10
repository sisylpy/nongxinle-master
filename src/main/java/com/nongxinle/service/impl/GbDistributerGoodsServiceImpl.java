package com.nongxinle.service.impl;

import com.nongxinle.dao.GbDepartmentGoodsStockDao;
import com.nongxinle.dao.GbDistributerFatherGoodsDao;
import com.nongxinle.dao.GbDistributerPurchaseGoodsDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.GbDepartmentOrdersService;
import com.nongxinle.service.GbDistributerFatherGoodsService;
import com.nongxinle.service.GbDistributerFoodService;
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

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@Service("gbDistributerGoodsService")
public class GbDistributerGoodsServiceImpl implements GbDistributerGoodsService {
    @Autowired
    private GbDistributerGoodsDao gbDistributerGoodsDao;
    @Autowired
    private GbDepartmentGoodsStockDao gbDepartmentGoodsStockDao;
    @Autowired
    private GbDistributerFatherGoodsDao gbDistributerFatherGoodsService;
    @Autowired
    private GbDistributerPurchaseGoodsDao gbDistributerPurchaseGoodsDao;

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


}
