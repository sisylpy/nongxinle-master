package com.nongxinle.service.impl;

import com.nongxinle.dao.GbDepartmentGoodsStockDao;
import com.nongxinle.dao.GbDepartmentOrdersDao;
import com.nongxinle.dao.GbDistributerGoodsDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.GbDistributerStandardService;
import com.nongxinle.utils.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.nongxinle.dao.GbDepartmentDisGoodsDao;
import com.nongxinle.service.GbDepartmentDisGoodsService;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@Service("gbDepartmentDisGoodsService")
public class GbDepartmentDisGoodsServiceImpl implements GbDepartmentDisGoodsService {
    @Autowired
    private GbDepartmentDisGoodsDao gbDepartmentDisGoodsDao;
    @Autowired
    private GbDepartmentOrdersDao gbDepartmentOrdersDao;
    @Autowired
    private GbDistributerGoodsDao gbDistributerGoodsDao;
    @Autowired
    private GbDepartmentGoodsStockDao gbDepartmentGoodsStockDao;
    @Autowired
    private GbDistributerStandardService gbDistributerStandardService;

    @Override
    public GbDepartmentDisGoodsEntity queryObject(Integer gbDepartmentDisGoodsId) {
        return gbDepartmentDisGoodsDao.queryObject(gbDepartmentDisGoodsId);
    }

    @Override
    public List<GbDepartmentDisGoodsEntity> queryList(Map<String, Object> map) {
        return gbDepartmentDisGoodsDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return gbDepartmentDisGoodsDao.queryTotal(map);
    }

    @Override
    public void save(GbDepartmentDisGoodsEntity gbDepartmentDisGoods) {
        gbDepartmentDisGoodsDao.save(gbDepartmentDisGoods);
    }

    @Override
    public void update(GbDepartmentDisGoodsEntity gbDepartmentDisGoods) {
        gbDepartmentDisGoodsDao.update(gbDepartmentDisGoods);
    }

    @Override
    public void delete(Integer gbDepartmentDisGoodsId) {
        gbDepartmentDisGoodsDao.delete(gbDepartmentDisGoodsId);
    }

    @Override
    public void deleteBatch(Integer[] gbDepartmentDisGoodsIds) {
        gbDepartmentDisGoodsDao.deleteBatch(gbDepartmentDisGoodsIds);
    }

    @Override
    public List<GbDepartmentDisGoodsEntity> queryGbDepDisGoodsByParams(Map<String, Object> map) {

        return gbDepartmentDisGoodsDao.queryGbDepDisGoodsByParams(map);
    }


    @Override
    public List<GbDistributerFatherGoodsEntity> depQueryDepGoodsWithOrderGb(Map<String, Object> map) {
        return gbDepartmentDisGoodsDao.depQueryDepGoodsWithOrderGb(map);
    }

    @Override
    public List<GbDepartmentDisGoodsEntity> depGetDepsGoodsGb(Map<String, Object> map) {
        return gbDepartmentDisGoodsDao.depGetDepsGoodsGb(map);
    }

    @Override
    public int queryGbDisGoodsTotal(Map<String, Object> map3) {

        return gbDepartmentDisGoodsDao.queryGbDisGoodsTotal(map3);
    }

    @Override
    public TreeSet<GbDepartmentDisGoodsEntity> queryDepDisGoodsQuickSearchStrGb(Map<String, Object> map1) {
        return gbDepartmentDisGoodsDao.queryDepDisGoodsQuickSearchStrGb(map1);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> disGetDepDisGoodsCataGb(Map<String, Object> map) {

        return gbDepartmentDisGoodsDao.disGetDepDisGoodsCataGb(map);
    }

    @Override
    public GbDepartmentDisGoodsEntity queryDepGoodsItemByParams(Map<String, Object> map1) {

        return gbDepartmentDisGoodsDao.queryDepGoodsItemByParams(map1);
    }

    @Override
    public List<GbDepartmentDisGoodsEntity> depQueryDepGoodsWithOrderDepGoods(Map<String, Object> map) {

        return gbDepartmentDisGoodsDao.depQueryDepGoodsWithOrderDepGoods(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDepTypeFatherGoods(Map<String, Object> mapD) {

        return gbDepartmentDisGoodsDao.queryDepTypeFatherGoods(mapD);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> selfMendiainGetDepDisGoodsCata(Map<String, Object> mapD) {

        return gbDepartmentDisGoodsDao.selfMendiainGetDepDisGoodsCata(mapD);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> selfMendiainGetDepDisGoodsCataWithGoods(Map<String, Object> map) {

        return gbDepartmentDisGoodsDao.selfMendiainGetDepDisGoodsCataWithGoods(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDepFatherGoodsByParams(Map<String, Object> mapG) {

        return gbDepartmentDisGoodsDao.queryDepFatherGoodsByParams(mapG);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> depQueryDepGoodsWithOrderGbNew(Map<String, Object> map) {
        return gbDepartmentDisGoodsDao.depQueryDepGoodsWithOrderGbNew(map);
    }


    @Override
    public List<GbDepartmentDisGoodsEntity> queryDepDisGoodsByParams(Map<String, Object> map) {

        return gbDepartmentDisGoodsDao.queryDepDisGoodsByParams(map);
    }

    @Override
    public List<GbDepartmentDisGoodsEntity>  queryDepartmentGoods(Map<String, Object> map) {

        return gbDepartmentDisGoodsDao.queryDepartmentGoods(map);
    }

    @Override
    public List<GbDepartmentDisGoodsEntity> depQueryDepGoodsWithOrder(Map<String, Object> map) {

        return gbDepartmentDisGoodsDao.depQueryDepGoodsWithOrder(map);
    }




//    @Override
//    public GbDepartmentDisGoodsEntity getTipText(GbDepartmentDisGoodsEntity departmentDisGoodsEntity) {
//        Integer depId = departmentDisGoodsEntity.getGbDdgDepartmentId();
//        Integer gid   = departmentDisGoodsEntity.getGbDdgDisGoodsId();
//        String unit   = departmentDisGoodsEntity.getGbDdgOrderStandard(); // 比如"件"或"斤"等
//
//        // -------- 1. 常量定义 --------
//        int windowDays = 56;       // 统计过去多少天
//        int leadTime   = 1;        // 预计提前期（天）
//        double serviceZ = 1.65;    // 95% 服务水平，对应正态分布的 Z 值
//
//        // -------- 2. 拉取这 windowDays 天内的"每日用量"：DailyUsage { date:String, qty:double } --------
//        Map<String, Object> mapForUsage = new HashMap<>();
//        mapForUsage.put("depId", depId);
//        mapForUsage.put("gid", gid);
//        mapForUsage.put("windowDays", windowDays);
//        List<DailyUsage> usages = gbDepartmentOrdersDao.selectDailyUsage(mapForUsage);
//
//        if (usages == null || usages.size() < 2) {
//            // 样本太少，至少需要 2 次非零记录才能算日均、方差
//            departmentDisGoodsEntity.setGbTipText("⚪️ 数据样本不足，无法进行补货预测");
//            // 设置默认的AI字段值
//            setAiFields(departmentDisGoodsEntity, null, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, 0.0, null, 0, 0.0, 0);
//            return departmentDisGoodsEntity;
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
//            departmentDisGoodsEntity.setGbTipText(
//                    String.format("⚪️ 过去 %d 天内未见此商品下单，无需补货", windowDays));
//            // 设置默认的AI字段值
//            setAiFields(departmentDisGoodsEntity, null, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, 0.0, null, 0, 0.0, 0);
//            return departmentDisGoodsEntity;
//        }
//
//        LocalDate today = LocalDate.now(ZoneId.systemDefault());
//        LocalDate firstDate;
//        try {
//            firstDate = LocalDate.parse(firstNonZeroDateStr);
//        } catch (DateTimeParseException e) {
//            departmentDisGoodsEntity.setGbTipText("⚠️ 解析首次下单日期失败，无法预测");
//            // 设置默认的AI字段值
//            setAiFields(departmentDisGoodsEntity, null, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, 0.0, null, 0, 0.0, 0);
//            return departmentDisGoodsEntity;
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
//        lastParams.put("depId", depId);
//        lastParams.put("gid", gid);
//        GbDepartmentOrdersEntity lo = gbDepartmentOrdersDao.selectLastOrder(lastParams);
//        if (lo == null) {
//            departmentDisGoodsEntity.setGbTipText("⚠️ 无上次订货记录，无法预测");
//            // 设置默认的AI字段值
//            setAiFields(departmentDisGoodsEntity, null, 0.0, mu, 0.0, 0.0, 0.0, 0.0, null, 0.0, null, 0, 0.0, 0);
//            return departmentDisGoodsEntity;
//        }
//
//        LocalDate lastDate;
//        try {
//            lastDate = LocalDate.parse(lo.getGbDoApplyDate());
//        } catch (DateTimeParseException e) {
//            departmentDisGoodsEntity.setGbTipText("⚠️ 上次订货日期解析失败");
//            // 设置默认的AI字段值
//            setAiFields(departmentDisGoodsEntity, null, 0.0, mu, 0.0, 0.0, 0.0, 0.0, null, 0.0, null, 0, 0.0, 0);
//            return departmentDisGoodsEntity;
//        }
//        long daysSince = ChronoUnit.DAYS.between(lastDate, today);
//        if (daysSince < 0) daysSince = 0;
//
//        double lastQty;
//        try {
//            lastQty = Double.parseDouble(lo.getGbDoQuantity());
//        } catch (NumberFormatException e) {
//            departmentDisGoodsEntity.setGbTipText("⚠️ 上次订货数量格式非法，无法预测");
//            // 设置默认的AI字段值
//            setAiFields(departmentDisGoodsEntity, null, 0.0, mu, 0.0, 0.0, 0.0, 0.0, lastDate, 0.0, lo, daysSince, 0.0, 0);
//            return departmentDisGoodsEntity;
//        }
//
//        // -------- 6.1 从库存中查询废弃的数量，把废弃的数量加入建议数量中； --------
//        Integer goodsId = departmentDisGoodsEntity.getGbDdgDisGoodsId();
//        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsDao.queryObject(goodsId);
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
//            map.put("depId", depId);
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
//                    departmentDisGoodsEntity.setGbTipText("⚠️ 无进货数据或进货量为 0，无法给出废弃信息");
//                    // 设置AI字段值
//                    setAiFields(departmentDisGoodsEntity, gbDistributerGoodsEntity, 0.0, mu, 0.0, 0.0, 0.0, 0.0, lastDate, lastQty, lo, daysSince, 0.0, 0);
//                    return departmentDisGoodsEntity;
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
//            departmentDisGoodsEntity.setGbTipText(tip);
//            // 设置AI字段值
//            setAiFields(departmentDisGoodsEntity, gbDistributerGoodsEntity, onHand_real, mu, 0.0, sigma/mu, safetyStock, reorderPoint, lastDate, lastQty, lo, daysSince, 0.0, 0);
//            return departmentDisGoodsEntity;
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
//        departmentDisGoodsEntity.setGbTipText(tipText);
//        // 设置AI字段值
//        setAiFields(departmentDisGoodsEntity, gbDistributerGoodsEntity, onHand_real, mu, 0.0, sigma/mu, safetyStock, reorderPoint, lastDate, lastQty, lo, daysSince, 0.0, adjustedOrder);
//        return departmentDisGoodsEntity;
//    }

    /**
     * 设置AI相关字段
     */
//    private void setAiFields(GbDepartmentDisGoodsEntity entity,
//                             GbDistributerGoodsEntity goods,
//                             double onHand,
//                             double mu,
//                             double recentAvg,
//                             double cv,
//                             double safetyStock,
//                             double reorderPoint,
//                             LocalDate lastDate,
//                             double lastOrderQty,
//                             GbDepartmentOrdersEntity lo,
//                             long daysSince,
//                             double tomorrowNeed,
//                             int orderQty) {
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("disGoodsId", entity.getGbDepartmentDisGoodsId());
//        map.put("depId", entity.getGbDdgDepartmentId());
//        Integer integer = gbDepartmentGoodsStockDao.queryGoodsStockCount(map);
//        if(integer > 0){
//            Double aDouble = gbDepartmentGoodsStockDao.queryDepStockWeightTotal(map);
//            System.out.println("sttoass" + aDouble);
//            entity.setGoodsStockWeightTotalString(String.format("%.2f", aDouble));
//        }else{
//            entity.setGoodsStockWeightTotalString("0");
//        }
//        entity.setAiCurrentStock(String.format("%.2f", onHand));
//        entity.setAiCurrentStockUnit(goods != null ? goods.getGbDgGoodsStandardname() : entity.getGbDdgOrderStandard());
//        entity.setAiDailyUsage(String.format("%.2f", mu));
//        entity.setAiRecentAvgUsage(String.format("%.2f", recentAvg));
//        entity.setAiUsageVariation(String.format("%.2f", cv));
//        entity.setAiSafetyStock(String.format("%.2f", safetyStock));
//        entity.setAiReorderPoint(String.format("%.2f", reorderPoint));
//        entity.setAiLastOrderDate(lastDate != null ? lastDate.toString() : "");
//        entity.setAiLastOrderQuantity(String.format("%.2f", lastOrderQty));
//        entity.setAiLastOrderUnit(lo != null ? lo.getGbDoStandard() : entity.getGbDdgOrderStandard());
//        entity.setAiDaysSinceLastOrder(String.valueOf(daysSince));
//        entity.setAiTomorrowNeed(String.format("%.2f", tomorrowNeed));
//        entity.setAiAvailableDays(String.format("%.1f", mu > 0 ? onHand / mu : 0.0));
//        entity.setAiOrderQuantity(String.valueOf(orderQty));
//        entity.setAiOrderStandard(entity.getGbDdgOrderStandard());
//
//        // 设置商品信息
////        if (goods != null) {
////            entity.setGbDistributerGoodsEntity(goods);
////        }
//    }


    @Override
    public int queryDepGoodsCount(Map<String, Object> mapC) {

        return gbDepartmentDisGoodsDao.queryDepGoodsCount(mapC);
    }

    @Override
    public List<Integer> queryOnlyDepGoodsIds(Map<String, Object> map) {

        return gbDepartmentDisGoodsDao.queryOnlyDepGoodsIds(map);
    }

    @Override
    public List<GbDepartmentDisGoodsEntity> depQueryDepGoodsWithOrderForAi(Map<String, Object> map) {

        return gbDepartmentDisGoodsDao.depQueryDepGoodsWithOrderForAi(map);
    }

    private static final Logger log = LoggerFactory.getLogger(GbDepartmentDisGoodsServiceImpl.class);

    @Override
    public PageUtils computeReorder(Integer depId, Integer page, Integer limit) {
        long totalStartTime = System.currentTimeMillis();
        log.info("开始计算部门[{}]的补货建议，页码：{}，每页数量：{}", depId, page, limit);

        // 1. 获取正在下单的商品
        long startTime = System.currentTimeMillis();
        Map<String, Object> mapOrder = new HashMap<>();
        mapOrder.put("depId", depId);
        mapOrder.put("status", 4);
        List<Integer> orderingGoodsIds = gbDepartmentOrdersDao.queryGoodsIds(mapOrder);
        log.info("获取正在下单商品耗时: {}ms", System.currentTimeMillis() - startTime);

        // 2. 参数、常量定义
        int windowDays = 56;       // 统计过去多少天
        int minTimes = 1;          // 至少订货次数阈值4 sisy
        int leadTime = 1;          // 预计提前期（天）
        double serviceZ = 1.65;    // 95% 服务水平，对应正态分布 Z 值
        log.info("计算参数设置: windowDays={}, minTimes={}, leadTime={}, serviceZ={}",
                windowDays, minTimes, leadTime, serviceZ);

        // 3. 获取常订商品总数
        Map<String, Object> freqParams = new HashMap<>();
        freqParams.put("depId", depId);
        freqParams.put("windowDays", windowDays);
        freqParams.put("minTimes", minTimes);
        int total = gbDepartmentOrdersDao.selectFrequentGoodsCount(freqParams);
        log.info("常订商品总数: {}", total);

        // 5. 分页查询，直到获取足够的数据（首屏补齐，其余正常分页）
        List<GbDepartmentDisGoodsEntity> finalResult = new ArrayList<>();
        if (page == 1) {
            int currentOffset = 0;
            int currentPage = 1;
            while (finalResult.size() < limit) {
                freqParams.put("offset", currentOffset);
                freqParams.put("limit", limit);
                log.info("[首屏补齐] 查询参数: depId={}, windowDays={}, minTimes={}, offset={}, limit={}, currentPage={}",
                        depId, windowDays, minTimes, currentOffset, limit, currentPage);
                List<Integer> goodsIds = gbDepartmentOrdersDao.selectFrequentGoodsWithPage(freqParams);
                if (goodsIds == null || goodsIds.isEmpty()) {
                    log.info("[首屏补齐] 没有更多数据，当前offset: {}, currentPage: {}", currentOffset, currentPage);
                    break;
                }
                List<GbDepartmentDisGoodsEntity> pageResult = processGoodsList(goodsIds, orderingGoodsIds, depId, windowDays, leadTime, serviceZ);
                finalResult.addAll(pageResult);
                log.info("[首屏补齐] 本次循环后累计商品数量: {}, 期望limit: {}, 当前currentPage: {}", finalResult.size(), limit, currentPage);
                if (finalResult.size() >= limit) {
                    log.info("[首屏补齐] 已获取足够数据，当前offset: {}, currentPage: {}", currentOffset, currentPage);
                    break;
                }
                currentOffset += limit;
                currentPage++;
            }
            log.info("[首屏补齐] 计算补货建议总耗时: {}ms", System.currentTimeMillis() - totalStartTime);
            log.info("[首屏补齐] 最终返回商品数量: {}, 期望limit: {}, 实际查到的最后一页currentPage: {}", finalResult.size(), limit, currentPage);
            return new PageUtils(finalResult, total, limit, currentPage);
        } else {
            int offset = (page - 1) * limit;
            freqParams.put("offset", offset);
            freqParams.put("limit", limit);
            log.info("[普通分页] 查询参数: depId={}, windowDays={}, minTimes={}, offset={}, limit={}, page={}",
                    depId, windowDays, minTimes, offset, limit, page);
            List<Integer> goodsIds = gbDepartmentOrdersDao.selectFrequentGoodsWithPage(freqParams);
            if (goodsIds != null && !goodsIds.isEmpty()) {
                finalResult.addAll(processGoodsList(goodsIds, orderingGoodsIds, depId, windowDays, leadTime, serviceZ));
            }
            log.info("[普通分页] 计算补货建议总耗时: {}ms", System.currentTimeMillis() - totalStartTime);
            log.info("[普通分页] 最终返回商品数量: {}, 期望limit: {}, 当前页page: {}", finalResult.size(), limit, page);
            return new PageUtils(finalResult, total, limit, page);
        }
    }



    private static final double EPSILON = 1e-6;             // 避免除零
    private static final int MIN_USAGE_RECORDS = 1;          // 至少需要 4 条用量数据4 sisy
    private static final int RECENT_DAYS_FOR_CV = 7;         // 用近 7 天计算波动
    private static final int ARRIVAL_HOUR = 7;               // 次日到货时间(07:00)


    public List<GbDepartmentDisGoodsEntity> processGoodsList(List<Integer> goodsIds,
                                                             List<Integer> orderingGoodsIds,
                                                             Integer depId,
                                                             int windowDays,
                                                             int leadTime,
                                                             double serviceZ) {
        List<GbDepartmentDisGoodsEntity> result = new ArrayList<GbDepartmentDisGoodsEntity>();

        for (Integer gid : goodsIds) {
            /* ---------- 1. 过滤正在下单 ---------- */
            if (orderingGoodsIds != null && orderingGoodsIds.contains(gid)) {
                log.info("SKU[{}] 正在下单，跳过", gid);
                continue;
            }

            /* ---------- 2. 基础信息 ---------- */
            GbDistributerGoodsEntity goods = gbDistributerGoodsDao.queryDisGoodsDetail(gid);
            if (goods == null) {
                log.warn("SKU[{}] 商品信息不存在", gid);
                continue;
            }
            GbDepartmentDisGoodsEntity departmentDisGoodsEntity = queryDepartmentGoods(depId, gid, goods);
            if (departmentDisGoodsEntity == null) {
                continue;
            }

            /* ---------- 3. 历史订单 ---------- */
            List<GbDepartmentOrdersEntity> orderHistory = queryHistory(depId, gid);
            GbDepartmentOrdersEntity lastOrder = queryLastOrder(depId, gid, goods);
            if (lastOrder == null) {
                log.info("SKU[{}] 在保质期内没有订货记录", gid);
                continue;
            }

            /* ---------- 4. 日用量 ---------- */
            Map<String, Object> usageParams = new HashMap<String, Object>();
            usageParams.put("depId", depId);
            usageParams.put("gid", gid);
            usageParams.put("windowDays", windowDays);
            List<DailyUsage> usages = gbDepartmentOrdersDao.selectDailyUsage(usageParams);
            if (usages == null || usages.size() < MIN_USAGE_RECORDS) {
                log.info("SKU[{}] 用量数据不足: {} 条", gid, usages == null ? 0 : usages.size());
                continue;
            }

            /* ---------- 5. 统计 ---------- */
            int n = usages.size();
            List<Double> dailyQs = new ArrayList<Double>();
            for (DailyUsage du : usages) dailyQs.add(du.getQty());

            // 打印窗口内用量明细，帮助排查日均为何偏高/低
            if (log.isDebugEnabled()) {
                log.debug("SKU[{}] 用量窗口({} 天): {}", gid, windowDays, dailyQs);
            }

            double sumQty = 0.0;
            for (Double q : dailyQs) sumQty += q;
            double mu = sumQty / n;

            if (log.isDebugEnabled()) {
                log.debug("SKU[{}] 总量: {}, 样本数: {}, 计算日均 mu: {}", gid, sumQty, n, mu);
            }

            if (mu < EPSILON) {
                log.debug("SKU[{}] mu≈0，跳过", gid);
                continue;
            }

            double varianceSum = 0.0;
            for (Double q : dailyQs) varianceSum += Math.pow(q - mu, 2);
            double sigma = Math.sqrt(varianceSum / n);

            List<Double> recentQs = dailyQs.subList(Math.max(0, n - RECENT_DAYS_FOR_CV), n);
            double recentSum = 0.0;
            for (Double q : recentQs) recentSum += q;
            double recentMu = recentSum / recentQs.size();
            double recentVarianceSum = 0.0;
            for (Double q : recentQs) recentVarianceSum += Math.pow(q - recentMu, 2);
            double recentSigma = Math.sqrt(recentVarianceSum / recentQs.size());
            double cv = Math.max(recentSigma / Math.max(recentMu, EPSILON), 0.1);

            if (log.isDebugEnabled()) {
                log.debug("SKU[{}] recentMu: {}, recentSigma: {}, CV: {}", gid, recentMu, recentSigma, cv);
            }

            /* ---------- 6. 安全库存 ---------- */
            double adjustedZ = adjustServiceZ(serviceZ, cv);
            double safetyStock = adjustedZ * sigma * Math.sqrt(leadTime);
            double reorderPoint = mu * leadTime + safetyStock;

            if (log.isDebugEnabled()) {
                log.debug("SKU[{}] safetyStock: {}, reorderPoint: {}", gid, safetyStock, reorderPoint);
            }

            /* ---------- 7. 库存估算 ---------- */
            LocalDate lastDate = LocalDate.parse(lastOrder.getGbDoApplyDate());
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            long daysSince = Math.max(0, ChronoUnit.DAYS.between(lastDate, today));

            double remainingToday = estimateRemainingDemand(LocalDateTime.now(), recentMu);
            double lastQty = parseDoubleSafe(lastOrder.getGbDoQuantity());
            double consumed = daysSince * mu;
            double onHand = lastQty - consumed - remainingToday;

            if (log.isDebugEnabled()) {
                log.debug("SKU[{}] daysSince: {}, lastQty: {}, consumed: {}, remainingToday: {}, onHand: {}", gid,
                        daysSince, lastQty, consumed, remainingToday, onHand);
            }

            /* ---------- 8. 订货决策 ---------- */
            double tomorrowNeed = recentMu;
            int orderQty = calculateOptimalOrderQuantity(orderHistory, onHand, reorderPoint, tomorrowNeed);

            if (log.isDebugEnabled()) {
                log.debug("SKU[{}] 建议订货量(orderQty): {}", gid, orderQty);
            }

            /* ---------- 9. 写入字段 ---------- */
            setAiFields(departmentDisGoodsEntity, goods, onHand, mu, recentMu, cv,
                    safetyStock, reorderPoint, lastDate, lastQty, lastOrder,
                    daysSince, tomorrowNeed, orderQty);

            departmentDisGoodsEntity.setGbDistributerGoodsEntity(goods);

            result.add(departmentDisGoodsEntity);
        }
        return result;
    }

    /* =================== 辅助方法 =================== */

    private GbDepartmentDisGoodsEntity queryDepartmentGoods(Integer depId, Integer gid, GbDistributerGoodsEntity goods) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("disGoodsId", gid);
        map.put("depId", depId);
        GbDepartmentDisGoodsEntity dg = gbDepartmentDisGoodsDao.queryDepartmentGoodsOnly(map);
        if (dg == null) log.warn("SKU[{}] 部门商品信息不存在", gid);
        return dg;
    }

    private List<GbDepartmentOrdersEntity> queryHistory(Integer depId, Integer gid) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("depId", depId);
        params.put("disGoodsId", gid);
        params.put("limit", 20);
        return gbDepartmentOrdersDao.queryDisHistoryOrdersByParamsForAi(params);
    }

    private GbDepartmentOrdersEntity queryLastOrder(Integer depId, Integer gid, GbDistributerGoodsEntity goods) {
        Map<String, Object> p = new HashMap<String, Object>();
        p.put("depId", depId);
        p.put("gid", gid);
        if (goods.getGbDgQuantityDays() != null) p.put("dayuDate", formatWhatDay(-goods.getGbDgQuantityDays()));
        return gbDepartmentOrdersDao.selectLastOrder(p);
    }

    private double adjustServiceZ(double baseZ, double cv) {
        if (cv < 0.5) return 1.0;
        if (cv < 1.0) return 1.3;
        return baseZ;
    }

    /**
     * 估计从“现在”到“下次到货”(默认次日 07:00)期间的消费量。
     */
    private double estimateRemainingDemand(LocalDateTime now, double dailyAvg) {
        int nowHour = now.getHour();
        double hoursToArrival = nowHour >= ARRIVAL_HOUR
                ? (24 - nowHour) + ARRIVAL_HOUR
                : ARRIVAL_HOUR - nowHour;
        double ratio = hoursToArrival / 24.0;
        return dailyAvg * ratio;
    }

    private double parseDoubleSafe(String s) {
        try {
            return (s == null || s.trim().isEmpty()) ? 0d : Double.parseDouble(s);
        } catch (NumberFormatException e) {
            log.warn("数值解析失败: {}", s);
            return 0d;
        }
    }

    private int calculateOptimalOrderQuantity(List<GbDepartmentOrdersEntity> orderHistory,
                                              double currentStock,
                                              double reorderPoint,
                                              double tomorrowNeed) {
        if (orderHistory == null || orderHistory.isEmpty()) {
            // 如果没有历史记录，使用基础缺口
            return (int) Math.ceil(reorderPoint - currentStock);
        }

        // 1. 计算历史订货量的统计指标
        List<Double> orderQuantities = orderHistory.stream()
                .map(order -> Double.parseDouble(order.getGbDoQuantity()))
                .collect(Collectors.toList());

        // 计算平均订货量
        double avgOrderQty = orderQuantities.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // 计算最常订的数量（众数）
        Map<Double, Long> qtyFrequency = orderQuantities.stream()
                .collect(Collectors.groupingBy(qty -> qty, Collectors.counting()));

        double modeOrderQty = qtyFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0.0);

        // 计算中位数订货量
        List<Double> sortedQuantities = new ArrayList<>(orderQuantities);
        Collections.sort(sortedQuantities);
        int mid = sortedQuantities.size() / 2;
        double medianOrderQty = sortedQuantities.size() % 2 == 0 ?
                (sortedQuantities.get(mid - 1) + sortedQuantities.get(mid)) / 2 :
                sortedQuantities.get(mid);

        // 2. 计算基础缺口
        double baseGap = reorderPoint - currentStock;

        // 3. 根据历史订货习惯调整订货量
        int orderQty;
        if (modeOrderQty > 0) {
            // 优先使用最常订的数量
            orderQty = (int) Math.ceil(modeOrderQty);
            log.info("使用最常订数量: {}", orderQty);
        } else if (avgOrderQty > 0) {
            // 如果没有最常订数量，使用平均订货量
            orderQty = (int) Math.ceil(avgOrderQty);
            log.info("使用平均订货量: {}", orderQty);
        } else {
            // 如果没有任何历史记录，使用基础缺口
            orderQty = (int) Math.ceil(baseGap);
            log.info("使用基础缺口: {}", orderQty);
        }

        // 4. 确保订货量至少能覆盖明天用量
        if (orderQty < tomorrowNeed) {
            orderQty = (int) Math.ceil(tomorrowNeed);
            log.info("调整订货量以覆盖明天用量: {}", orderQty);
        }

        // 5. 考虑商品保质期
        if (orderQty > 7 * tomorrowNeed) {  // 如果订货量超过7天用量
            orderQty = (int) Math.ceil(7 * tomorrowNeed);  // 限制在7天用量内
            log.info("考虑保质期限制，调整订货量: {}", orderQty);
        }

        return orderQty;
    }

    // 抽取设置字段的方法，避免代码重复
    private void setAiFields(GbDepartmentDisGoodsEntity entity,
                             GbDistributerGoodsEntity goods,
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

//        recentAvg
        entity.setAiCurrentStock(String.format("%.2f", onHand));
        entity.setAiCurrentStockUnit(goods.getGbDgGoodsStandardname());
        entity.setAiDailyUsage(String.format("%.2f", mu));
        entity.setAiRecentAvgUsage(String.format("%.2f", recentAvg));
        entity.setAiUsageVariation(String.format("%.2f", cv));
        entity.setAiSafetyStock(String.format("%.2f", safetyStock));
        entity.setAiReorderPoint(String.format("%.2f", reorderPoint));
        entity.setAiLastOrderDate(lastDate.toString());
        entity.setAiLastOrderQuantity(String.format("%.2f", lastOrderQty));
        entity.setAiLastOrderUnit(lo.getGbDoStandard());
        entity.setAiDaysSinceLastOrder(String.valueOf(daysSince));
        entity.setAiTomorrowNeed(String.format("%.2f", tomorrowNeed));
        entity.setAiAvailableDays(String.format("%.1f", onHand / mu));
        entity.setAiOrderQuantity(String.valueOf(orderQty));
        entity.setAiOrderStandard(entity.getGbDdgOrderStandard());

        List<GbDistributerStandardEntity> nxDistributerStandardEntities = gbDistributerStandardService.queryDisStandardByDisGoodsIdGb(goods.getGbDistributerGoodsId());
        goods.setGbDistributerStandardEntities(nxDistributerStandardEntities);

    }

//    @Override
//    public List<GbDepartmentDisGoodsEntity> computeReorder(Integer depId) {
//
//            // 1. 参数、常量定义
//            int windowDays = 56;    // 统计过去多少天
//            int minTimes = 0;     // 至少订货次数阈值
//            int leadTime = 1;     // 预计提前期（天）
//            double serviceZ = 1.65;  // 95% 服务水平
//
//            // 2. 筛出「常订」商品
//            Map<String, Object> freqParams = new HashMap<>();
//            freqParams.put("depId", depId);
//            freqParams.put("windowDays", windowDays);
//            freqParams.put("minTimes", minTimes);
//            System.out.println("freqParamsfreqParams" + freqParams);
//            List<Integer> goodsIds = gbDepartmentOrdersDao.selectFrequentGoods(freqParams);
//            if (goodsIds == null || goodsIds.isEmpty()) {
//                return Collections.emptyList();
//            }
//
//            // 3. 拉取每日用量（DailyUsage）——返回 List<DailyUsage> { date, qty }
//            Map<Integer, List<DailyUsage>> dailyMap = new HashMap<>();
//            for (Integer gid : goodsIds) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("depId", depId);
//                map.put("gid", gid);
//                map.put("windowDays", windowDays);
//                System.out.println("meyouyige " + map);
//                List<DailyUsage> usages = gbDepartmentOrdersDao.selectDailyUsage(map);
//                dailyMap.put(gid, usages != null ? usages : Collections.emptyList());
//            }
//
//            LocalDate today = LocalDate.now(ZoneId.systemDefault());
//            List<GbDepartmentDisGoodsEntity> result = new ArrayList<>();
//
//            // 4. 对每个商品，计算 μ、σ、ROP、安全库存，判断是否下单
//            for (Integer gid : goodsIds) {
//                List<DailyUsage> usages = dailyMap.get(gid);
//                if (usages.size() < 1) {
//                    // 样本太少，跳过
//                    continue;
//                }
//
//                // 4.1 计算日均 μ 和标准差 σ
//                double sum = usages.stream()
//                        .mapToDouble(DailyUsage::getQty)
//                        .sum();
//                double mu = sum / windowDays;
//                double variance = usages.stream()
//                        .mapToDouble(u -> Math.pow(u.getQty() - mu, 2))
//                        .sum() / windowDays;
//                double sigma = Math.sqrt(variance);
//
//                // 4.2 计算安全库存 & 再订货点
//                double safetyStock = serviceZ * sigma * Math.sqrt(leadTime);
//                double reorderPoint = mu * leadTime + safetyStock;
//
//                // 4.3 查询上次订货（LastOrder { goodsId, lastDate, lastQty }）
//                Map<String, Object> lastParams = new HashMap<>();
//                lastParams.put("depId", depId);
//                lastParams.put("gid", gid);
//                System.out.println("meyourkrkkrkrkrkrkkrkrkr" +lastParams);
//                GbDepartmentOrdersEntity lo = gbDepartmentOrdersDao.selectLastOrder(lastParams);
//                if (lo == null) {
//                    continue;
//                }
//
//                LocalDate lastDate;
//                try {
//                    lastDate = LocalDate.parse(lo.getGbDoApplyDate());
//                } catch (Exception e) {
////                    log.warn("日期解析失败 lo.getNxDohApplyDate()={}，跳过", lo.getGbDoApplyDate());
//                    continue;
//                }
//                long daysSince = ChronoUnit.DAYS.between(lastDate, today);
//                if (daysSince < 0) {
//                    daysSince = 0;
//                }
//
//                // 4.4 预测消耗 & 计算当前可用量
//                double consumed = daysSince * mu;
//
//                double onHand = Double.parseDouble(lo.getGbDoQuantity()) - consumed;
//
//                // 4.5 如果可用量 <= 安全库存，就要下单
//                System.out.println("goodsgetNxDgGoodsName" + gbDistributerGoodsDao.queryObject(gid).getGbDgGoodsName() +  "onHand===" + onHand + "safeStock===" + safetyStock);
//                if (onHand <= safetyStock) {
//                    int orderQty = (int) Math.ceil(reorderPoint - onHand);
//                    if (orderQty > 0) {
//                        // 构造出一个「预测要下单」实体，放订货量到 quantity 字段
//                        Map<String, Object> map = new HashMap<>();
//                        map.put("depId", depId);
//                        map.put("disGoodsId", gid);
//                        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsDao.queryDepartmentGoodsForAi(map);
//                        getTipText(departmentDisGoodsEntity);
//                        result.add(departmentDisGoodsEntity);
//                    }
//                }
//            }
//
//            return result;
//
//        }

    @Override
    public TreeSet<GbDistributerGoodsEntity> disQueryDisGoodsWithOrderForAiTree(Map<String, Object> map) {
        return gbDepartmentDisGoodsDao.disQueryDisGoodsWithOrderForAiTree(map);
    }

    @Override
    public List<Integer> queryOnlyDisGoodsIds(Map<String, Object> map) {

        return gbDepartmentDisGoodsDao.queryOnlyDisGoodsIds(map);
    }

    @Override
    public GbDepartmentDisGoodsEntity queryDepartmentGoodsForAi(Map<String, Object> mapD) {

        return gbDepartmentDisGoodsDao.queryDepartmentGoodsForAi(mapD);
    }


}


