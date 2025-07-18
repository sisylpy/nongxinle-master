package com.nongxinle.service.impl;

import com.nongxinle.dao.GbDepartmentGoodsStockDao;
import com.nongxinle.dao.GbDistributerFatherGoodsDao;
import com.nongxinle.dao.GbDistributerPurchaseGoodsDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.GbDepartmentOrdersService;
import com.nongxinle.service.GbDistributerFatherGoodsService;
import com.nongxinle.service.GbDistributerFoodService;
import com.sun.tools.internal.xjc.reader.dtd.bindinfo.BIAttribute;
import javafx.scene.layout.BackgroundImage;
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

//    @Override
//    public GbDistributerGoodsEntity getTipText(GbDistributerGoodsEntity gbDistributerGoodsEntity) {
//
//        Integer gid   = gbDistributerGoodsEntity.getGbDistributerGoodsId();
//        String unit   = gbDistributerGoodsEntity.getGbDgGoodsStandardname(); // 比如"件"或"斤"等
//
//        // -------- 1. 常量定义 --------
//        int windowDays = 56;       // 统计过去多少天
//        int leadTime   = 1;        // 预计提前期（天）
//        double serviceZ = 1.65;    // 95% 服务水平，对应正态分布的 Z 值
//
//        // -------- 2. 拉取这 windowDays 天内的"每日用量"：DailyUsage { date:String, qty:double } --------
//        Map<String, Object> mapForUsage = new HashMap<>();
//        mapForUsage.put("gid", gid);
//        mapForUsage.put("windowDays", windowDays);
//        System.out.println("disgooostewx"+ mapForUsage);
//        List<DailyUsage> usages = gbDepartmentOrdersDao.selectDailyUsage(mapForUsage);
//
//        if (usages == null || usages.size() < 2) {
//            // 样本太少，至少需要 2 次非零记录才能算日均、方差
//            gbDistributerGoodsEntity.setGbTipText("⚪️ 数据样本不足，无法进行补货预测");
//            // 设置默认的AI字段值
//            setAiFields(gbDistributerGoodsEntity, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, 0.0, null, 0, 0.0, 0);
//            return gbDistributerGoodsEntity;
//        }
//
//        // -------- 3. 找到 firstDate（第一笔非零下单日期）& 统计 daysSpan --------
//        String firstNonZeroDateStr = null;
//        for (DailyUsage du : usages) {
//            if (du.getQty() > 0) {
//                firstNonZeroDateStr = du.getDate();
//                break;
//            }
//        }
//        if (firstNonZeroDateStr == null) {
//            gbDistributerGoodsEntity.setGbTipText(
//                    String.format("⚪️ 过去 %d 天内未见此商品下单，无需补货", windowDays));
//            // 设置默认的AI字段值
//            setAiFields(gbDistributerGoodsEntity, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, 0.0, null, 0, 0.0, 0);
//            return gbDistributerGoodsEntity;
//        }
//
//        LocalDate today = LocalDate.now(ZoneId.systemDefault());
//        LocalDate firstDate;
//        try {
//            firstDate = LocalDate.parse(firstNonZeroDateStr);
//        } catch (DateTimeParseException e) {
//            gbDistributerGoodsEntity.setGbTipText("⚠️ 解析首次下单日期失败，无法预测");
//            // 设置默认的AI字段值
//            setAiFields(gbDistributerGoodsEntity, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, 0.0, null, 0, 0.0, 0);
//            return gbDistributerGoodsEntity;
//        }
//        long daysSpan = ChronoUnit.DAYS.between(firstDate, today) + 1;
//        if (daysSpan <= 0) daysSpan = 1;
//
//        // -------- 4. 计算这段区间里日均 μ 和方差 σ²，再开方得到 σ --------
//        double sumQty = usages.stream()
//                .filter(du -> {
//                    try {
//                        LocalDate d = LocalDate.parse(du.getDate());
//                        return !d.isBefore(firstDate) && !d.isAfter(today);
//                    } catch (DateTimeParseException ex) {
//                        return false;
//                    }
//                })
//                .mapToDouble(DailyUsage::getQty)
//                .sum();
//        double mu = sumQty / (double) daysSpan;
//
//        double variance = usages.stream()
//                .filter(du -> {
//                    try {
//                        LocalDate d = LocalDate.parse(du.getDate());
//                        return !d.isBefore(firstDate) && !d.isAfter(today);
//                    } catch (DateTimeParseException ex) {
//                        return false;
//                    }
//                })
//                .mapToDouble(u -> Math.pow(u.getQty() - mu, 2))
//                .sum() / (double) daysSpan;
//        double sigma = Math.sqrt(variance);
//
//        // -------- 5. 查"上次订货"记录 --------
//        Map<String, Object> lastParams = new HashMap<>();
//        lastParams.put("gid", gid);
//        GbDepartmentOrdersEntity lo = gbDepartmentOrdersDao.selectLastOrder(lastParams);
//        if (lo == null) {
//            gbDistributerGoodsEntity.setGbTipText("⚠️ 无上次订货记录，无法预测");
//            // 设置默认的AI字段值
//            setAiFields(gbDistributerGoodsEntity, 0.0, mu, 0.0, 0.0, 0.0, 0.0, null, 0.0, null, 0, 0.0, 0);
//            return gbDistributerGoodsEntity;
//        }
//
//        LocalDate lastDate;
//        try {
//            lastDate = LocalDate.parse(lo.getGbDoApplyDate());
//        } catch (DateTimeParseException e) {
//            gbDistributerGoodsEntity.setGbTipText("⚠️ 上次订货日期解析失败");
//            // 设置默认的AI字段值
//            setAiFields(gbDistributerGoodsEntity, 0.0, mu, 0.0, 0.0, 0.0, 0.0, null, 0.0, null, 0, 0.0, 0);
//            return gbDistributerGoodsEntity;
//        }
//        long daysSince = ChronoUnit.DAYS.between(lastDate, today);
//        if (daysSince < 0) daysSince = 0;
//
//        double lastQty;
//        try {
//            lastQty = Double.parseDouble(lo.getGbDoQuantity());
//        } catch (NumberFormatException e) {
//            gbDistributerGoodsEntity.setGbTipText("⚠️ 上次订货数量格式非法，无法预测");
//            // 设置默认的AI字段值
//            setAiFields(gbDistributerGoodsEntity,  0.0, mu, 0.0, 0.0, 0.0, 0.0, lastDate, 0.0, lo, daysSince, 0.0, 0);
//            return gbDistributerGoodsEntity;
//        }
//
//        // -------- 6.1 从库存中查询废弃的数量，把废弃的数量加入建议数量中； --------
//
//        boolean isFreshGoods = false;
//        String wasteSuggestion = "";
//        double totalRate = 0.0;
//        double middleRate = 0.0;
//        double lateRate   = 0.0;
//
//        // 只有"需保鲜"的商品才计算废弃
//        System.out.println("godososoosos" + gbDistributerGoodsEntity.getGbDgGoodsName() + gbDistributerGoodsEntity.getGbDgControlFresh());
//        if (gbDistributerGoodsEntity.getGbDgControlFresh() != null
//                && gbDistributerGoodsEntity.getGbDgControlFresh() == 1) {
//
//            isFreshGoods = true;
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("disGoodsId", gid);
//            map.put("startDate", formatWhatDay(-windowDays));
//            System.out.println("waststooneeieiieiie" + map);
//
//            // 6.1.1 统计过去 windowDays 的 "废弃总量"
//            Double totalWaste = gbDepartmentGoodsStockDao.queryDepGoodsWasteWeightTotal(map);
//            if (totalWaste == null) totalWaste = 0.0;
//
//            if (totalWaste > 0) {
//                // 6.1.2 查询"中期"的废弃情况（过去 windowDays/2 天里）
//                map.put("startDate", formatWhatDay(-windowDays / 2));
//                Double middleWaste = gbDepartmentGoodsStockDao.queryDepGoodsWasteWeightTotal(map);
//                if (middleWaste == null) middleWaste = 0.0;
//
//                // 6.1.3 查询"中期"总进货量
//                Double middlePurchase = gbDepartmentGoodsStockDao.queryDepStockWeightTotal(map);
//                if (middlePurchase == null || middlePurchase < 0.0001) {
//                    // 如果没有进货数据，直接提示库存异常
//                    gbDistributerGoodsEntity.setGbTipText("⚠️ 无进货数据或进货量为 0，无法给出废弃信息");
//                    // 设置AI字段值
//                    setAiFields(gbDistributerGoodsEntity, 0.0, mu, 0.0, 0.0, 0.0, 0.0, lastDate, lastQty, lo, daysSince, 0.0, 0);
//                    return gbDistributerGoodsEntity;
//                }
//
//                // 6.1.4 计算"中期废弃率" = 中期废弃 / 中期进货
//                middleRate = middleWaste / middlePurchase; // 例如 0.15 表示 15%
//
//                if (middleWaste > 0) {
//                    // 6.1.5 查询"中期废弃次数"
//                    int middleStockTimes = gbDepartmentGoodsStockDao.queryGoodsStockCount(map);
//
//                    map.put("waste", 0);
//                    int middleWasteTimes = gbDepartmentGoodsStockDao.queryGoodsStockCount(map);
//                    // 废弃频率 = 废弃次数 / (windowDays/2)
//                    System.out.println("coumiidididididmiddleWasteTimes" + middleWasteTimes + "middleStockTimes" + middleStockTimes);
//                    double middleWasteTimesScale = (double) middleWasteTimes / middleStockTimes;
//                    int recentlyTimes = middleStockTimes / 2;
//
//                    System.out.println("middleAvgTimesmiddleAvgTimes" + middleWasteTimesScale);
//                    // 高频废弃：如果中期废弃率太高
//                    if (middleWasteTimesScale > 0.7) {
//                        wasteSuggestion = "⚠️ 废弃次数很高，建议扣量补货或优先检查保鲜条件；";
//                    }
//                    // 中频废弃：废弃率适中
//                    else if (middleWasteTimesScale > 0.3) {
//                        wasteSuggestion = "⚠️ 曾出现废弃，";
//                        // 6.1.6 查询"最近 middleAvgTimes/2 天"内是否又有新的废弃
//                        map.put("r", recentlyTimes);
//                        System.out.println("tmapapamama" + map);
//                        double latestWeight   = gbDepartmentGoodsStockDao.queryGoodsLatestWeight(map);
//                        double latestWasteWeight = gbDepartmentGoodsStockDao.queryGoodsLatestWasteWeight(map);
//                        System.out.println("lastetweidkdkdk" + latestWeight +"latestWasteWeightlatestWasteWeight"  +latestWasteWeight);
//                        if (latestWeight < 0.0001) {
//                            latestWeight = 1.0; // 防止除零
//                        }
//
//                        lateRate = latestWasteWeight / latestWeight; // e.g. 0.15 表示 15%
//                        System.out.println("middleRatemiddleRate=" + middleRate + "lateRatelateRate"  +lateRate);
//                        if (latestWasteWeight == 0) {
//                            wasteSuggestion = String.format(
//                                    "%s但过去 %d次暂无废弃，保持现有补货节奏。",
//                                    wasteSuggestion, recentlyTimes
//                            );
//                        } else if (lateRate > middleRate) {
//                            wasteSuggestion = String.format(
//                                    "%s过去%d次仍有废弃，建议继续减少批量；",
//                                    wasteSuggestion, recentlyTimes
//                            );
//                        } else {
//                            wasteSuggestion = String.format(
//                                    "%s过去 %d次有废弃，但数量下降，继续观察；",
//                                    wasteSuggestion, recentlyTimes
//                            );
//                        }
//                    }
//                    // 低频废弃：废弃出现过，但很少
//                    else {
//                        wasteSuggestion = "✅ 废弃率较低，保持现有补货量即可。";
//                    }
//                } else {
//                    // totalWaste == 0：过去 windowDays 内无任何废弃
//                    wasteSuggestion = "✅ 最近没有废弃，您的补货策略很好，继续保持。";
//                }
//            } else {
//                // totalWaste == 0
//                wasteSuggestion = "✅ 过去 56 天内一直无废弃，您的进货量非常合理。";
//            }
//        }
//
//
//        // -------- 7. 账面可用 onHand_raw = lastQty – daysSince × μ --------
//        double onHand_raw = lastQty - daysSince * mu;
//        if (onHand_raw < 0) onHand_raw = 0;
//
//        // -------- 8. 再订货点 reorderPoint = μ × leadTime + safetyStock --------
//        double safetyStock  = serviceZ * sigma * Math.sqrt(leadTime);
//        double reorderPoint = mu * (double) leadTime + safetyStock;
//
//        // -------- 9. 真实可用 onHand_real = onHand_raw  (不再扣除 totalWaste) --------
//        double onHand_real = onHand_raw;
//
//        // -------- 10. 如果 onHand_real ≥ reorderPoint，则无需补货 --------
//        if (onHand_real >= reorderPoint) {
//            String tip = String.format(
//                    "✅ 当前账面可用 %.2f%s，高于再订货点 %.2f%s，无需补货。\n%s",
//                    onHand_real, unit,
//                    reorderPoint, unit,
//                    isFreshGoods ? wasteSuggestion : ""  // 如果是易腐商品，则附上废弃小贴士
//            );
//            gbDistributerGoodsEntity.setGbTipText(tip);
//            // 设置AI字段值
//            setAiFields(gbDistributerGoodsEntity, onHand_real, mu, 0.0, sigma/mu, safetyStock, reorderPoint, lastDate, lastQty, lo, daysSince, 0.0, 0);
//            return gbDistributerGoodsEntity;
//        }
//
//        // -------- 11. 计算基础缺口 baseGap = reorderPoint – onHand_real --------
//        double baseGap = reorderPoint - onHand_real;
//        if (baseGap < 0) baseGap = 0;
//        int baseGapInt = (int) Math.ceil(baseGap);
//
//        // -------- 12. 按废弃率"放大补货"，但我们此处只留下 wasteSuggestion，具体补货量按业务决定 --------
//        // 由于你要"尽量避免废弃"，所以可以在 baseGapInt 基础上酌情扣减一部分，或者仅把 wasteSuggestion 作为调节提醒
//        // 这里示例：直接把基础补货量做为建议，废弃小贴士已在 wasteSuggestion 中
//        int adjustedOrder = baseGapInt;
//
//        // -------- 13. 最终建议 & 小贴士文本输出 --------
//        String tipText = String.format(
//                "🏷️ 建议补货：%d%s\n" +
//                        "    • 平均消耗：%.2f%s/天\n" +
//                        "    • 波动 σ：≈%.2f\n" +
//                        "    • 安全库存：≈%.2f%s\n" +
//                        "    • 再订货点：%.2f%s\n" +
//                        "    • 上次订货：%s（%d 天前），那次进了 %.2f%s，账面可用 %.2f%s\n" +
//                        "    • %s\n",
//                adjustedOrder, unit,    // 建议补货数量
//                mu, unit,               // 日均消耗
//                sigma,                  // 波动 σ
//                safetyStock, unit,      // 安全库存
//                reorderPoint, unit,     // 再订货点
//                lastDate.toString(),    // 上次订货日期
//                daysSince,
//                lastQty, unit,          // 上次订货量
//                onHand_raw, unit,       // 账面可用
//                (isFreshGoods ? wasteSuggestion : "")  // 废弃小贴士
//        );
//
//        gbDistributerGoodsEntity.setGbTipText(tipText);
//        // 设置AI字段值
//        setAiFields(gbDistributerGoodsEntity, onHand_real, mu, 0.0, sigma/mu, safetyStock, reorderPoint, lastDate, lastQty, lo, daysSince, 0.0, adjustedOrder);
//        return gbDistributerGoodsEntity;
//    }

    @Override
    public List<Integer> queryOnlyDisGoodsIds(Map<String, Object> map) {

        return gbDistributerGoodsDao.queryOnlyDisGoodsIds(map);
    }

    @Override
    public GbDistributerGoodsEntity queryDisGoodsDetail(Integer nxDdgDisGoodsId) {

        return gbDistributerGoodsDao.queryDisGoodsDetail(nxDdgDisGoodsId);
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
