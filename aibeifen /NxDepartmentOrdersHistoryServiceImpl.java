package com.nongxinle.service.impl;

import com.nongxinle.controller.NxDepartmentOrdersHistoryController;
import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.dao.NxDistributerGoodsDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.nongxinle.dao.NxDepartmentOrdersHistoryDao;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@Service("nxDepartmentOrdersHistoryService")
public class NxDepartmentOrdersHistoryServiceImpl implements NxDepartmentOrdersHistoryService {

    @Autowired
    private NxDepartmentOrdersHistoryDao nxDepartmentOrdersHistoryDao;
    @Autowired
    NxDepartmentOrdersDao nxDepartmentOrdersDao;
    @Autowired
    NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDepartmentOrderHistoryService historyService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;

    @Override
    public NxDepartmentOrdersHistoryEntity queryObject(Integer nxDepartmentOrdersHistoryId) {
        return nxDepartmentOrdersHistoryDao.queryObject(nxDepartmentOrdersHistoryId);
    }

    @Override
    public List<NxDepartmentOrdersHistoryEntity> queryList(Map<String, Object> map) {
        return nxDepartmentOrdersHistoryDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxDepartmentOrdersHistoryDao.queryTotal(map);
    }

    @Override
    public void save(NxDepartmentOrdersHistoryEntity nxDepartmentOrdersHistory) {
        nxDepartmentOrdersHistoryDao.save(nxDepartmentOrdersHistory);
    }

    @Override
    public void update(NxDepartmentOrdersHistoryEntity nxDepartmentOrdersHistory) {
        nxDepartmentOrdersHistoryDao.update(nxDepartmentOrdersHistory);
    }

    @Override
    public void delete(Integer nxDepartmentOrdersHistoryId) {
        nxDepartmentOrdersHistoryDao.delete(nxDepartmentOrdersHistoryId);
    }

    @Override
    public void deleteBatch(Integer[] nxDepartmentOrdersHistoryIds) {
        nxDepartmentOrdersHistoryDao.deleteBatch(nxDepartmentOrdersHistoryIds);
    }

    @Override
    public List<NxDepartmentOrdersHistoryEntity> queryDepHistoryOrdersByParams(Map<String, Object> map1) {
        return nxDepartmentOrdersHistoryDao.queryDepHistoryOrdersByParams(map1);
    }

    @Override
    public int queryOrderTimes(Map<String, Object> map) {

        return nxDepartmentOrdersHistoryDao.queryOrderTimes(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDepTodayOrder(Map<String, Object> map) {

        return nxDepartmentOrdersHistoryDao.queryDepTodayOrder(map);
    }

    @Override
    public List<Integer> queryRecentOrders(NxDepartmentEntity departmentId) {


        return nxDepartmentOrdersHistoryDao.queryRecentOrders(departmentId);
    }

    @Override
    public int queryDepOrderCount(Map<String, Object> map) {

        return nxDepartmentOrdersHistoryDao.queryDepOrderCount(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> mapGD) {

        return nxDepartmentOrdersHistoryDao.queryDisGoodsByParams(mapGD);
    }

    @Override
    public List<Map<String, Object>> queryLogs(Map<String, Object> mapLog) {

        return nxDepartmentOrdersHistoryDao.queryLogs(mapLog);
    }

    private static final Logger log = LoggerFactory.getLogger(NxDepartmentOrdersHistoryServiceImpl.class);



    @Override
    public PageUtils computeReorder
            (Integer depId, Integer page, Integer limit) {
        long totalStartTime = System.currentTimeMillis();
        log.info("开始计算部门[{}]的补货建议，页码：{}，每页数量：{}", depId, page, limit);

        // 1. 获取正在下单的商品
        long startTime = System.currentTimeMillis();
        Map<String, Object> mapOrder = new HashMap<>();
        mapOrder.put("depId", depId);
        mapOrder.put("status", 3);
        List<Integer> orderingGoodsIds = nxDepartmentOrdersDao.queryGoodsIds(mapOrder);
        log.info("获取正在下单商品耗时: {}ms", System.currentTimeMillis() - startTime);

        // 2. 参数、常量定义
        int windowDays = 56;       // 统计过去多少天
        int minTimes = 6;          // 至少订货次数阈值
        int leadTime = 1;          // 预计提前期（天）
        double serviceZ = 1.65;    // 95% 服务水平，对应正态分布 Z 值
        log.info("计算参数设置: windowDays={}, minTimes={}, leadTime={}, serviceZ={}",
                windowDays, minTimes, leadTime, serviceZ);

        // 3. 筛出「常订」商品，并直接进行分页
        startTime = System.currentTimeMillis();
        Map<String, Object> freqParams = new HashMap<>();
        freqParams.put("depId", depId);
        freqParams.put("windowDays", windowDays);
        freqParams.put("minTimes", minTimes);
        freqParams.put("page", page);
        freqParams.put("limit", limit);

        // 修改为持续查询直到凑够数据或没有更多数据
        List<Integer> allGoodsIds = new ArrayList<>();
        List<NxDepartmentDisGoodsEntity> finalResult = new ArrayList<>();  // 存储满足补货条件的商品
        int offset = (page - 1) * limit;
        int fetchSize = limit * 2; // 每次取大一点，加快凑齐速度
        int innerPage = 0;
        boolean hasMore = true;
        long queryStartTime = System.currentTimeMillis();
        int totalQueries = 0;
        int totalItemsFetched = 0;

        log.info("开始持续查询商品，目标数量: {}, 每次查询数量: {}, 起始offset: {}", limit, fetchSize, offset);

        while (finalResult.size() < limit && hasMore) {  // 使用finalResult.size()判断
            long queryIterationStart = System.currentTimeMillis();
            totalQueries++;

            freqParams.put("offset", offset + innerPage * fetchSize);
            freqParams.put("limit", fetchSize);
            log.info("执行第{}次查询: offset={}, limit={}, 当前已获取商品数: {}, 当前满足条件的商品数: {}",
                    totalQueries,
                    offset + innerPage * fetchSize,
                    fetchSize,
                    allGoodsIds.size(),
                    finalResult.size());

            List<Integer> goodsIds = historyService.selectFrequentGoodsWithPage(freqParams);
            long queryTime = System.currentTimeMillis() - queryIterationStart;

            if (goodsIds == null || goodsIds.isEmpty()) {
                log.info("第{}次查询没有返回数据，耗时: {}ms，停止查询", totalQueries, queryTime);
                hasMore = false;
                break;
            }

            totalItemsFetched += goodsIds.size();
            log.info("第{}次查询结果: 获取到{}个商品, 耗时: {}ms, 当前累计查询: {}, 当前满足条件的商品数: {}/{}",
                    totalQueries,
                    goodsIds.size(),
                    queryTime,
                    allGoodsIds.size() + goodsIds.size(),
                    finalResult.size(),
                    limit);

            allGoodsIds.addAll(goodsIds);

            // 处理当前批次查询到的商品
            for (Integer gid : goodsIds) {
                if (orderingGoodsIds != null && orderingGoodsIds.contains(gid)) {
                    log.info("商品[{}]正在下单中，跳过", gid);
                    continue;
                }

                // 处理商品并添加到finalResult
                NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(gid);
                if (goods == null) {
                    log.warn("商品[{}]信息不存在", gid);
                    continue;
                }

                // 获取部门商品信息
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsId", gid);
                map.put("depId", depId);
                NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
                if (departmentDisGoodsEntity == null) {
                    log.warn("商品[{}]部门商品信息不存在", goods.getNxDgGoodsName());
                    continue;
                }

                // 获取历史订货记录
                Map<String, Object> orderHistoryParams = new HashMap<>();
                orderHistoryParams.put("depId", depId);
                orderHistoryParams.put("disGoodsId", gid);
                orderHistoryParams.put("limit", 20);
                List<NxDepartmentOrderHistoryEntity> orderHistory = historyService.queryDisHistoryOrdersByParamsForAi(orderHistoryParams);

                // 获取最近一次订货记录
                Map<String, Object> lastParams = new HashMap<>();
                lastParams.put("depId", depId);
                lastParams.put("gid", gid);
                if (goods.getNxDgQuantityDays() != null) {
                    lastParams.put("dayuDate", formatWhatDay(-goods.getNxDgQuantityDays()));
                }
                NxDepartmentOrderHistoryEntity lo = historyService.selectLastOrder(lastParams);
                if (lo == null) {
                    log.info("商品[{}]在保质期内没有订货记录", goods.getNxDgGoodsName());
                    continue;
                }

                // 获取每日用量数据
                Map<String, Object> usageParams = new HashMap<>();
                usageParams.put("depId", depId);
                usageParams.put("gid", gid);
                usageParams.put("windowDays", windowDays);
                List<DailyUsage> usages = historyService.selectDailyUsage(usageParams);
                if (usages == null || usages.size() < 5) {
                    log.info("商品[{}]历史用量数据不足: {}条记录", goods.getNxDgGoodsName(), usages != null ? usages.size() : 0);
                    continue;
                }

                // 计算用量统计指标
                List<Double> dailyQuantities = usages.stream()
                        .map(DailyUsage::getQty)
                        .collect(Collectors.toList());
                double sum = dailyQuantities.stream().mapToDouble(Double::doubleValue).sum();
                double mu = sum / windowDays;  // 日均用量

                // 计算用量波动性
                double variance = dailyQuantities.stream()
                        .mapToDouble(qty -> Math.pow(qty - mu, 2))
                        .sum() / windowDays;
                double sigma = Math.sqrt(variance);
                double cv = sigma / mu;  // 变异系数，反映用量波动程度

                // 使用最近7天的数据重新计算CV，以更好地反映近期趋势
                List<Double> recentQuantities = dailyQuantities.subList(Math.max(0, dailyQuantities.size() - 7), dailyQuantities.size());
                double recentMu = recentQuantities.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(mu);
                double recentVariance = recentQuantities.stream()
                        .mapToDouble(qty -> Math.pow(qty - recentMu, 2))
                        .sum() / recentQuantities.size();
                double recentSigma = Math.sqrt(recentVariance);
                double recentCv = recentSigma / recentMu;

                // 使用最近7天的CV值，但确保不会过低
                cv = Math.max(recentCv, 0.1);  // 设置最小CV为0.1，避免过度降低安全库存

                // 计算最近7天的用量趋势
                double recentAvg = recentQuantities.stream()
                        .filter(qty -> qty > 0) // 排除0值
                        .filter(qty -> qty <= mu * 3) // 排除异常值（超过平均值的3倍）
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(mu); // 如果没有有效值，使用长期平均值

                // 添加日志记录
                log.info("商品[{}]最近7天用量分析:", goods.getNxDgGoodsName());
                log.info("- 原始数据: {}", recentQuantities);
                log.info("- 过滤后数据: {}", recentQuantities.stream()
                        .filter(qty -> qty > 0)
                        .filter(qty -> qty <= mu * 3)
                        .collect(Collectors.toList()));
                log.info("- 计算得到的近期平均值: {}", String.format("%.2f", recentAvg));
                log.info("- 近期用量波动性(CV): {}", String.format("%.2f", recentCv));

                // 根据用量波动性调整安全库存系数
                double adjustedServiceZ = serviceZ;
                if (cv < 0.5) {  // 用量非常稳定
                    adjustedServiceZ = 1.0;  // 降低安全库存
                    log.info("商品[{}]用量非常稳定(CV={}), 使用较低的安全库存系数: {}",
                            goods.getNxDgGoodsName(), String.format("%.2f", cv), String.format("%.2f", adjustedServiceZ));
                } else if (cv < 1.0) {  // 用量相对稳定
                    adjustedServiceZ = 1.3;
                    log.info("商品[{}]用量相对稳定(CV={}), 使用中等安全库存系数: {}",
                            goods.getNxDgGoodsName(), String.format("%.2f", cv), String.format("%.2f", adjustedServiceZ));
                } else {
                    log.info("商品[{}]用量波动较大(CV={}), 使用标准安全库存系数: {}",
                            goods.getNxDgGoodsName(), String.format("%.2f", cv), String.format("%.2f", adjustedServiceZ));
                }

                double safetyStock = adjustedServiceZ * sigma * Math.sqrt(leadTime);
                double reorderPoint = mu * leadTime + safetyStock;

                log.info("商品[{}]用量分析:", goods.getNxDgGoodsName());
                log.info("- 长期日均用量: {}", String.format("%.2f", mu));
                log.info("- 最近7天平均用量: {}", String.format("%.2f", recentAvg));
                log.info("- 长期用量波动性(CV): {}", String.format("%.2f", sigma / mu));
                log.info("- 近期用量波动性(CV): {}", String.format("%.2f", recentCv));
                log.info("- 最终使用CV: {}", String.format("%.2f", cv));
                log.info("- 安全库存系数: {}", String.format("%.2f", adjustedServiceZ));
                log.info("- 安全库存: {}", String.format("%.2f", safetyStock));
                log.info("- 再订货点: {}", String.format("%.2f", reorderPoint));

                // 8.6 计算当前库存
                LocalDate lastDate = LocalDate.parse(lo.getNxDoApplyDate());
                LocalDate today = LocalDate.now(ZoneId.systemDefault());

                long daysSince = Math.max(0, ChronoUnit.DAYS.between(lastDate, today));

                // 根据当前时间决定是否计算今天的消耗
                LocalDateTime now = LocalDateTime.now();
                double todayConsumed = 0.0;

                // 获取最近一次订货的时间
                LocalDateTime lastOrderTime = null;
                try {
                    lastOrderTime = LocalDateTime.parse(lo.getNxDoApplyDate() + " " + lo.getNxDoApplyOnlyTime());
                } catch (Exception e) {
                    log.warn("商品[{}]时间解析失败: {}", goods.getNxDgGoodsName(), lo.getNxDoApplyOnlyTime());
                }

                // 判断是否计算今天消耗的逻辑
                if (now.getHour() >= 12) {  // 如果是下午
                    // 使用最近7天平均用量作为今天的消耗估算
                    todayConsumed = recentAvg;
                    log.info("当前时间: {}, 已过中午，估算今天消耗: {}",
                            now.format(DateTimeFormatter.ofPattern("HH:mm")),
                            String.format("%.2f", todayConsumed));
                } else if (now.getHour() < 4) {  // 如果是凌晨0-4点
                    // 检查是否是昨天的订单
                    if (lastOrderTime != null && lastOrderTime.getHour() >= 0 && lastOrderTime.getHour() < 4) {
                        // 如果最近一次订货是在凌晨0-4点，且是昨天的订单，则计入昨天的消耗
                        if (lastDate.equals(today.minusDays(1))) {
                            todayConsumed = recentAvg;
                            log.info("当前时间: {}, 凌晨时段，且最近一次订货是昨天凌晨，计入昨天消耗: {}",
                                    now.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    String.format("%.2f", todayConsumed));
                        }
                    }
                } else {
                    log.info("当前时间: {}, 未过中午，不计入今天消耗",
                            now.format(DateTimeFormatter.ofPattern("HH:mm")));
                }

                // 计算当前实际库存
                double consumed = daysSince * mu;
                double lastOrderQty = Double.parseDouble(lo.getNxDoQuantity());
                double onHand = lastOrderQty - consumed - todayConsumed;  // 减去今天的消耗

                // 预测明天的需求
                double tomorrowNeed = 0.7 * recentAvg + 0.3 * mu;  // 更重视近期趋势
                double totalNeed = onHand + tomorrowNeed;  // 当前库存 + 明天用量

                log.info("商品[{}]库存分析:", goods.getNxDgGoodsName());
                log.info("- 上次订货日期: {}, 距离今天: {}天", lastDate, daysSince);
                log.info("- 预计消耗: {}, 今天消耗: {}, 上次订货量: {}, 当前库存: {}",
                        String.format("%.2f", consumed),
                        String.format("%.2f", todayConsumed),
                        String.format("%.2f", lastOrderQty),
                        String.format("%.2f", onHand));
                log.info("- 明天预期用量: {}, 总需求(库存+明天): {}",
                        String.format("%.2f", tomorrowNeed),
                        String.format("%.2f", totalNeed));

                if (onHand < reorderPoint) {  // 当前库存低于再订货点时补货
                    // 计算最佳订货量
                    int orderQty = calculateOptimalOrderQuantity(orderHistory, onHand, reorderPoint, tomorrowNeed);

                    if (orderQty > 0) {
                        // 设置所有计算结果到字段
                        setAiFields(departmentDisGoodsEntity, goods, onHand, mu, recentAvg, cv, safetyStock,
                                reorderPoint, lastDate, lastOrderQty, lo, daysSince, tomorrowNeed, orderQty);

                        log.info("商品[{}]需要补货: {}个", goods.getNxDgGoodsName(), orderQty);
                        log.info("- 补货原因: 当前库存({}) < 再订货点({})",
                                String.format("%.2f", onHand),
                                String.format("%.2f", reorderPoint));
                        log.info("- 补货建议: 考虑到用量波动性(CV={})和安全库存系数({})",
                                String.format("%.2f", cv),
                                String.format("%.2f", adjustedServiceZ));

                        // 确保设置了补货建议文本
                        String tipText = String.format("建议补货%d个，当前库存%.1f，再订货点%.1f",
                                orderQty, onHand, reorderPoint);
                        departmentDisGoodsEntity.setNxTipText(tipText);

                        finalResult.add(departmentDisGoodsEntity);
                    }
                } else {
                    // 库存充足的情况，也设置这些字段
                    setAiFields(departmentDisGoodsEntity, goods, onHand, mu, recentAvg, cv, safetyStock,
                            reorderPoint, lastDate, lastOrderQty, lo, daysSince, tomorrowNeed, 0); // 订货量为0

                    log.info("商品[{}]库存充足: 当前库存({}) >= 再订货点({})",
                            goods.getNxDgGoodsName(),
                            String.format("%.2f", onHand),
                            String.format("%.2f", reorderPoint));
                    log.info("- 库存状态: 考虑到用量波动性(CV={})和安全库存系数({})",
                            String.format("%.2f", cv),
                            String.format("%.2f", adjustedServiceZ));

                    // 设置库存充足的建议文本
                    String tipText = String.format("库存充足，当前库存%.1f，再订货点%.1f",
                            onHand, reorderPoint);
                    departmentDisGoodsEntity.setNxTipText(tipText);

                    finalResult.add(departmentDisGoodsEntity);
                }
            }

            // 判断是否需要继续查询下一页
            if (finalResult.size() < limit) {
                if (goodsIds.size() < fetchSize) {
                    log.info("当前满足条件的商品数{}，未达到目标数量{}，但数据已查完，停止查询",
                            finalResult.size(), limit);
                    hasMore = false;
                } else {
                    log.info("当前满足条件的商品数{}，未达到目标数量{}，继续查询下一页",
                            finalResult.size(), limit);
                }
            } else {
                log.info("已达到目标数量{}，停止查询", limit);
                hasMore = false;
            }

            innerPage++;
        }

        long totalQueryTime = System.currentTimeMillis() - queryStartTime;
        log.info("持续查询完成: 执行{}次查询, 总耗时: {}ms, 查询商品总数: {}, 满足条件的商品数: {}, 平均每次查询耗时: {}ms",
                totalQueries,
                totalQueryTime,
                allGoodsIds.size(),
                finalResult.size(),
                totalQueries > 0 ? totalQueryTime / totalQueries : 0);

        // 获取总数
        int total = historyService.selectFrequentGoodsCount(freqParams);
        log.info("筛选常订商品耗时: {}ms, 总共获取到{}个商品, 数据库总记录数: {}",
                System.currentTimeMillis() - startTime,
                finalResult.size(),
                total);

        if (finalResult.isEmpty()) {
            log.info("没有找到常订商品");
            return new PageUtils(new ArrayList<>(), 0, limit, page);
        }

        // 确保最终处理的商品数量不超过limit
        int finalGoodsCount = Math.min(limit, finalResult.size());
        List<NxDepartmentDisGoodsEntity> limitedResult = finalResult.subList(0, finalGoodsCount);
        log.info("最终处理商品数量限制: 查询到{}个商品，限制为{}个，实际处理{}个",
                finalResult.size(),
                limit,
                finalGoodsCount);

        // 预加载所有需要的商品信息
        startTime = System.currentTimeMillis();
        Map<Integer, NxDistributerGoodsEntity> goodsMap = new HashMap<>();
        for (NxDepartmentDisGoodsEntity goods : limitedResult) {
            Integer gid = goods.getNxDdgDisGoodsId();
            NxDistributerGoodsEntity goodsEntity = nxDistributerGoodsService.queryObject(gid);
            if (goodsEntity != null) {
                goodsMap.put(gid, goodsEntity);
                log.info("加载商品信息: ID={}, 名称={}", gid, goodsEntity.getNxDgGoodsName());
            } else {
                log.warn("商品[{}]信息不存在", gid);
            }
        }

        // 预加载所有需要的父类信息
        startTime = System.currentTimeMillis();
        Set<Integer> fatherIds = new HashSet<>();
        for (NxDistributerGoodsEntity goods : goodsMap.values()) {
            if (goods.getNxDgDfgGoodsFatherId() != null) {
                fatherIds.add(goods.getNxDgDfgGoodsFatherId());
            }
        }
        log.info("需要加载的父类ID数量: {}", fatherIds.size());

        Map<Integer, NxDistributerFatherGoodsEntity> fatherGoodsMap = new HashMap<>();
        if (!fatherIds.isEmpty()) {
            List<NxDistributerFatherGoodsEntity> fatherGoods = nxDistributerFatherGoodsService.queryListByIds(new ArrayList<>(fatherIds));
            fatherGoods.forEach(father -> {
                fatherGoodsMap.put(father.getNxDistributerFatherGoodsId(), father);
                log.info("加载父类信息: ID={}, 名称={}",
                        father.getNxDistributerFatherGoodsId(),
                        father.getNxDfgFatherGoodsName());
            });
        }
        log.info("加载父类信息耗时: {}ms, 父类数量: {}", System.currentTimeMillis() - startTime, fatherGoodsMap.size());

        // 预加载所有需要的祖父类信息
        startTime = System.currentTimeMillis();
        Set<Integer> grandIds = new HashSet<>();
        for (NxDistributerFatherGoodsEntity father : fatherGoodsMap.values()) {
            if (father.getNxDfgFathersFatherId() != null) {
                grandIds.add(father.getNxDfgFathersFatherId());
            }
        }
        log.info("需要加载的祖父类ID数量: {}", grandIds.size());

        Map<Integer, NxDistributerFatherGoodsEntity> grandGoodsMap = new HashMap<>();
        if (!grandIds.isEmpty()) {
            List<NxDistributerFatherGoodsEntity> grandGoods = nxDistributerFatherGoodsService.queryListByIds(new ArrayList<>(grandIds));
            grandGoods.forEach(grand -> {
                grandGoodsMap.put(grand.getNxDistributerFatherGoodsId(), grand);
                log.info("加载祖父类信息: ID={}, 名称={}",
                        grand.getNxDistributerFatherGoodsId(),
                        grand.getNxDfgFatherGoodsName());
            });
        }
        log.info("加载祖父类信息耗时: {}ms, 祖父类数量: {}", System.currentTimeMillis() - startTime, grandGoodsMap.size());

        // 预加载所有需要的曾祖父类信息
        startTime = System.currentTimeMillis();
        Set<Integer> greatGrandIds = new HashSet<>();
        for (NxDistributerFatherGoodsEntity grand : grandGoodsMap.values()) {
            if (grand.getNxDfgFathersFatherId() != null) {
                greatGrandIds.add(grand.getNxDfgFathersFatherId());
            }
        }
        log.info("需要加载的曾祖父类ID数量: {}", greatGrandIds.size());

        Map<Integer, NxDistributerFatherGoodsEntity> greatGrandGoodsMap = new HashMap<>();
        if (!greatGrandIds.isEmpty()) {
            List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDistributerFatherGoodsService.queryListByIds(new ArrayList<>(greatGrandIds));
            greatGrandGoods.forEach(greatGrand -> {
                greatGrandGoodsMap.put(greatGrand.getNxDistributerFatherGoodsId(), greatGrand);
                log.info("加载曾祖父类信息: ID={}, 名称={}",
                        greatGrand.getNxDistributerFatherGoodsId(),
                        greatGrand.getNxDfgFatherGoodsName());
            });
        }
        log.info("加载曾祖父类信息耗时: {}ms, 曾祖父类数量: {}", System.currentTimeMillis() - startTime, greatGrandGoodsMap.size());

        // 预计算分类信息用于排序
        Map<Integer, CategoryInfo> categoryInfoMap = new HashMap<>();
        for (NxDepartmentDisGoodsEntity goods : limitedResult) {
            Integer goodsId = goods.getNxDdgDisGoodsId();
            NxDistributerGoodsEntity goodsEntity = goodsMap.get(goodsId);
            if (goodsEntity != null) {
                Integer fatherId = goodsEntity.getNxDgDfgGoodsFatherId();
                NxDistributerFatherGoodsEntity fatherEntity = fatherGoodsMap.get(fatherId);
                if (fatherEntity != null) {
                    Integer grandId = fatherEntity.getNxDfgFathersFatherId();
                    NxDistributerFatherGoodsEntity grandEntity = grandGoodsMap.get(grandId);
                    if (grandEntity != null) {
                        Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
                        NxDistributerFatherGoodsEntity greatGrandEntity = greatGrandGoodsMap.get(greatGrandId);

                        categoryInfoMap.put(goodsId, new CategoryInfo(
                                fatherEntity.getNxDfgFatherGoodsSort(),
                                grandEntity.getNxDfgFatherGoodsSort(),
                                greatGrandEntity != null ? greatGrandEntity.getNxDfgFatherGoodsSort() : 0
                        ));
                    }
                }
            }
        }

        // 排序
        limitedResult.sort((a, b) -> {
            CategoryInfo infoA = categoryInfoMap.get(a.getNxDdgDisGoodsId());
            CategoryInfo infoB = categoryInfoMap.get(b.getNxDdgDisGoodsId());
            if (infoA == null || infoB == null) return 0;

            // 首先比较曾祖父类别的sort
            int greatCompare = Integer.compare(
                    infoA.getGreatSort() != null ? infoA.getGreatSort() : 0,
                    infoB.getGreatSort() != null ? infoB.getGreatSort() : 0
            );
            if (greatCompare != 0) return greatCompare;

            // 然后比较祖父类别的sort
            int grandCompare = Integer.compare(
                    infoA.getGrandSort() != null ? infoA.getGrandSort() : 0,
                    infoB.getGrandSort() != null ? infoB.getGrandSort() : 0
            );
            if (grandCompare != 0) return grandCompare;

            // 最后比较父类的sort
            return Integer.compare(
                    infoA.getFatherSort() != null ? infoA.getFatherSort() : 0,
                    infoB.getFatherSort() != null ? infoB.getFatherSort() : 0
            );
        });

        log.info("总耗时: {}ms", System.currentTimeMillis() - totalStartTime);
        PageUtils pageUtil = new PageUtils(limitedResult, total, limit, page);

        return pageUtil;
    }


//    @Override
//    public PageUtils computeReorderDuoqingiu
//            (Integer depId, Integer page, Integer limit) {
//        long totalStartTime = System.currentTimeMillis();
//        log.info("开始计算部门[{}]的补货建议，页码：{}，每页数量：{}", depId, page, limit);
//
//        // 1. 获取正在下单的商品
//        long startTime = System.currentTimeMillis();
//        Map<String, Object> mapOrder = new HashMap<>();
//        mapOrder.put("depId", depId);
//        mapOrder.put("status", 3);
//        List<Integer> orderingGoodsIds = nxDepartmentOrdersDao.queryGoodsIds(mapOrder);
//        log.info("获取正在下单商品耗时: {}ms", System.currentTimeMillis() - startTime);
//
//        // 2. 参数、常量定义
//        int windowDays = 56;       // 统计过去多少天
//        int minTimes = 6;          // 至少订货次数阈值
//        int leadTime = 1;          // 预计提前期（天）
//        double serviceZ = 1.65;    // 95% 服务水平，对应正态分布 Z 值
//        log.info("计算参数设置: windowDays={}, minTimes={}, leadTime={}, serviceZ={}",
//                windowDays, minTimes, leadTime, serviceZ);
//
//        // 3. 筛出「常订」商品，并直接进行分页
//        startTime = System.currentTimeMillis();
//        Map<String, Object> freqParams = new HashMap<>();
//        freqParams.put("depId", depId);
//        freqParams.put("windowDays", windowDays);
//        freqParams.put("minTimes", minTimes);
//        freqParams.put("page", page);
//        freqParams.put("limit", limit);
//
//        // 修改为持续查询直到凑够数据或没有更多数据
//        List<Integer> allGoodsIds = new ArrayList<>();
//        List<NxDepartmentDisGoodsEntity> finalResult = new ArrayList<>();  // 存储满足补货条件的商品
//        int offset = (page - 1) * limit;
//        int fetchSize = limit * 2; // 每次取大一点，加快凑齐速度
//        int innerPage = 0;
//        boolean hasMore = true;
//        long queryStartTime = System.currentTimeMillis();
//        int totalQueries = 0;
//        int totalItemsFetched = 0;
//
//        log.info("开始持续查询商品，目标数量: {}, 每次查询数量: {}, 起始offset: {}", limit, fetchSize, offset);
//
//        while (finalResult.size() < limit && hasMore) {  // 使用finalResult.size()判断
//            long queryIterationStart = System.currentTimeMillis();
//            totalQueries++;
//
//            freqParams.put("offset", offset + innerPage * fetchSize);
//            freqParams.put("limit", fetchSize);
//            log.info("执行第{}次查询: offset={}, limit={}, 当前已获取商品数: {}, 当前满足条件的商品数: {}",
//                    totalQueries,
//                    offset + innerPage * fetchSize,
//                    fetchSize,
//                    allGoodsIds.size(),
//                    finalResult.size());
//
//            List<Integer> goodsIds = historyService.selectFrequentGoodsWithPage(freqParams);
//            long queryTime = System.currentTimeMillis() - queryIterationStart;
//
//            if (goodsIds == null || goodsIds.isEmpty()) {
//                log.info("第{}次查询没有返回数据，耗时: {}ms，停止查询", totalQueries, queryTime);
//                hasMore = false;
//                break;
//            }
//
//            totalItemsFetched += goodsIds.size();
//            log.info("第{}次查询结果: 获取到{}个商品, 耗时: {}ms, 当前累计查询: {}, 当前满足条件的商品数: {}/{}",
//                    totalQueries,
//                    goodsIds.size(),
//                    queryTime,
//                    allGoodsIds.size() + goodsIds.size(),
//                    finalResult.size(),
//                    limit);
//
//            allGoodsIds.addAll(goodsIds);
//
//            // 处理当前批次查询到的商品
//            for (Integer gid : goodsIds) {
//                if (orderingGoodsIds != null && orderingGoodsIds.contains(gid)) {
//                    log.info("商品[{}]正在下单中，跳过", gid);
//                    continue;
//                }
//
//                // 处理商品并添加到finalResult
//                NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(gid);
//                if (goods == null) {
//                    log.warn("商品[{}]信息不存在", gid);
//                    continue;
//                }
//
//                // 获取部门商品信息
//                Map<String, Object> map = new HashMap<>();
//                map.put("disGoodsId", gid);
//                map.put("depId", depId);
//                NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
//                if (departmentDisGoodsEntity == null) {
//                    log.warn("商品[{}]部门商品信息不存在", goods.getNxDgGoodsName());
//                    continue;
//                }
//
//                // 获取历史订货记录
//                Map<String, Object> orderHistoryParams = new HashMap<>();
//                orderHistoryParams.put("depId", depId);
//                orderHistoryParams.put("disGoodsId", gid);
//                orderHistoryParams.put("limit", 20);
//                List<NxDepartmentOrderHistoryEntity> orderHistory = historyService.queryDisHistoryOrdersByParamsForAi(orderHistoryParams);
//
//                // 获取最近一次订货记录
//                Map<String, Object> lastParams = new HashMap<>();
//                lastParams.put("depId", depId);
//                lastParams.put("gid", gid);
//                if (goods.getNxDgQuantityDays() != null) {
//                    lastParams.put("dayuDate", formatWhatDay(-goods.getNxDgQuantityDays()));
//                }
//                NxDepartmentOrderHistoryEntity lo = historyService.selectLastOrder(lastParams);
//                if (lo == null) {
//                    log.info("商品[{}]在保质期内没有订货记录", goods.getNxDgGoodsName());
//                    continue;
//                }
//
//                // 获取每日用量数据
//                Map<String, Object> usageParams = new HashMap<>();
//                usageParams.put("depId", depId);
//                usageParams.put("gid", gid);
//                usageParams.put("windowDays", windowDays);
//                List<DailyUsage> usages = historyService.selectDailyUsage(usageParams);
//                if (usages == null || usages.size() < 5) {
//                    log.info("商品[{}]历史用量数据不足: {}条记录", goods.getNxDgGoodsName(), usages != null ? usages.size() : 0);
//                    continue;
//                }
//
//                // 计算用量统计指标
//                List<Double> dailyQuantities = usages.stream()
//                        .map(DailyUsage::getQty)
//                        .collect(Collectors.toList());
//                double sum = dailyQuantities.stream().mapToDouble(Double::doubleValue).sum();
//                double mu = sum / windowDays;  // 日均用量
//
//                // 计算用量波动性
//                double variance = dailyQuantities.stream()
//                        .mapToDouble(qty -> Math.pow(qty - mu, 2))
//                        .sum() / windowDays;
//                double sigma = Math.sqrt(variance);
//                double cv = sigma / mu;  // 变异系数，反映用量波动程度
//
//                // 使用最近7天的数据重新计算CV，以更好地反映近期趋势
//                List<Double> recentQuantities = dailyQuantities.subList(Math.max(0, dailyQuantities.size() - 7), dailyQuantities.size());
//                double recentMu = recentQuantities.stream()
//                        .mapToDouble(Double::doubleValue)
//                        .average()
//                        .orElse(mu);
//                double recentVariance = recentQuantities.stream()
//                        .mapToDouble(qty -> Math.pow(qty - recentMu, 2))
//                        .sum() / recentQuantities.size();
//                double recentSigma = Math.sqrt(recentVariance);
//                double recentCv = recentSigma / recentMu;
//
//                // 使用最近7天的CV值，但确保不会过低
//                cv = Math.max(recentCv, 0.1);  // 设置最小CV为0.1，避免过度降低安全库存
//
//                // 计算最近7天的用量趋势
//                double recentAvg = recentQuantities.stream()
//                        .filter(qty -> qty > 0) // 排除0值
//                        .filter(qty -> qty <= mu * 3) // 排除异常值（超过平均值的3倍）
//                        .mapToDouble(Double::doubleValue)
//                        .average()
//                        .orElse(mu); // 如果没有有效值，使用长期平均值
//
//                // 添加日志记录
//                log.info("商品[{}]最近7天用量分析:", goods.getNxDgGoodsName());
//                log.info("- 原始数据: {}", recentQuantities);
//                log.info("- 过滤后数据: {}", recentQuantities.stream()
//                        .filter(qty -> qty > 0)
//                        .filter(qty -> qty <= mu * 3)
//                        .collect(Collectors.toList()));
//                log.info("- 计算得到的近期平均值: {}", String.format("%.2f", recentAvg));
//                log.info("- 近期用量波动性(CV): {}", String.format("%.2f", recentCv));
//
//                // 根据用量波动性调整安全库存系数
//                double adjustedServiceZ = serviceZ;
//                if (cv < 0.5) {  // 用量非常稳定
//                    adjustedServiceZ = 1.0;  // 降低安全库存
//                    log.info("商品[{}]用量非常稳定(CV={}), 使用较低的安全库存系数: {}",
//                            goods.getNxDgGoodsName(), String.format("%.2f", cv), String.format("%.2f", adjustedServiceZ));
//                } else if (cv < 1.0) {  // 用量相对稳定
//                    adjustedServiceZ = 1.3;
//                    log.info("商品[{}]用量相对稳定(CV={}), 使用中等安全库存系数: {}",
//                            goods.getNxDgGoodsName(), String.format("%.2f", cv), String.format("%.2f", adjustedServiceZ));
//                } else {
//                    log.info("商品[{}]用量波动较大(CV={}), 使用标准安全库存系数: {}",
//                            goods.getNxDgGoodsName(), String.format("%.2f", cv), String.format("%.2f", adjustedServiceZ));
//                }
//
//                double safetyStock = adjustedServiceZ * sigma * Math.sqrt(leadTime);
//                double reorderPoint = mu * leadTime + safetyStock;
//
//                log.info("商品[{}]用量分析:", goods.getNxDgGoodsName());
//                log.info("- 长期日均用量: {}", String.format("%.2f", mu));
//                log.info("- 最近7天平均用量: {}", String.format("%.2f", recentAvg));
//                log.info("- 长期用量波动性(CV): {}", String.format("%.2f", sigma / mu));
//                log.info("- 近期用量波动性(CV): {}", String.format("%.2f", recentCv));
//                log.info("- 最终使用CV: {}", String.format("%.2f", cv));
//                log.info("- 安全库存系数: {}", String.format("%.2f", adjustedServiceZ));
//                log.info("- 安全库存: {}", String.format("%.2f", safetyStock));
//                log.info("- 再订货点: {}", String.format("%.2f", reorderPoint));
//
//                // 8.6 计算当前库存
//                LocalDate lastDate = LocalDate.parse(lo.getNxDoApplyDate());
//                LocalDate today = LocalDate.now(ZoneId.systemDefault());
//
//                long daysSince = Math.max(0, ChronoUnit.DAYS.between(lastDate, today));
//
//                // 根据当前时间决定是否计算今天的消耗
//                LocalDateTime now = LocalDateTime.now();
//                double todayConsumed = 0.0;
//
//                // 获取最近一次订货的时间
//                LocalDateTime lastOrderTime = null;
//                try {
//                    lastOrderTime = LocalDateTime.parse(lo.getNxDoApplyDate() + " " + lo.getNxDoApplyOnlyTime());
//                } catch (Exception e) {
//                    log.warn("商品[{}]时间解析失败: {}", goods.getNxDgGoodsName(), lo.getNxDoApplyOnlyTime());
//                }
//
//                // 判断是否计算今天消耗的逻辑
//                if (now.getHour() >= 12) {  // 如果是下午
//                    // 使用最近7天平均用量作为今天的消耗估算
//                    todayConsumed = recentAvg;
//                    log.info("当前时间: {}, 已过中午，估算今天消耗: {}",
//                            now.format(DateTimeFormatter.ofPattern("HH:mm")),
//                            String.format("%.2f", todayConsumed));
//                } else if (now.getHour() < 4) {  // 如果是凌晨0-4点
//                    // 检查是否是昨天的订单
//                    if (lastOrderTime != null && lastOrderTime.getHour() >= 0 && lastOrderTime.getHour() < 4) {
//                        // 如果最近一次订货是在凌晨0-4点，且是昨天的订单，则计入昨天的消耗
//                        if (lastDate.equals(today.minusDays(1))) {
//                            todayConsumed = recentAvg;
//                            log.info("当前时间: {}, 凌晨时段，且最近一次订货是昨天凌晨，计入昨天消耗: {}",
//                                    now.format(DateTimeFormatter.ofPattern("HH:mm")),
//                                    String.format("%.2f", todayConsumed));
//                        }
//                    }
//                } else {
//                    log.info("当前时间: {}, 未过中午，不计入今天消耗",
//                            now.format(DateTimeFormatter.ofPattern("HH:mm")));
//                }
//
//                // 计算当前实际库存
//                double consumed = daysSince * mu;
//                double lastOrderQty = Double.parseDouble(lo.getNxDoQuantity());
//                double onHand = lastOrderQty - consumed - todayConsumed;  // 减去今天的消耗
//
//                // 预测明天的需求
//                double tomorrowNeed = 0.7 * recentAvg + 0.3 * mu;  // 更重视近期趋势
//                double totalNeed = onHand + tomorrowNeed;  // 当前库存 + 明天用量
//
//                log.info("商品[{}]库存分析:", goods.getNxDgGoodsName());
//                log.info("- 上次订货日期: {}, 距离今天: {}天", lastDate, daysSince);
//                log.info("- 预计消耗: {}, 今天消耗: {}, 上次订货量: {}, 当前库存: {}",
//                        String.format("%.2f", consumed),
//                        String.format("%.2f", todayConsumed),
//                        String.format("%.2f", lastOrderQty),
//                        String.format("%.2f", onHand));
//                log.info("- 明天预期用量: {}, 总需求(库存+明天): {}",
//                        String.format("%.2f", tomorrowNeed),
//                        String.format("%.2f", totalNeed));
//
//                if (onHand < reorderPoint) {  // 当前库存低于再订货点时补货
//                    // 计算最佳订货量
//                    int orderQty = calculateOptimalOrderQuantity(orderHistory, onHand, reorderPoint, tomorrowNeed);
//
//                    if (orderQty > 0) {
//                        // 设置所有计算结果到字段
//                        setAiFields(departmentDisGoodsEntity, goods, onHand, mu, recentAvg, cv, safetyStock,
//                                reorderPoint, lastDate, lastOrderQty, lo, daysSince, tomorrowNeed, orderQty);
//
//                        log.info("商品[{}]需要补货: {}个", goods.getNxDgGoodsName(), orderQty);
//                        log.info("- 补货原因: 当前库存({}) < 再订货点({})",
//                                String.format("%.2f", onHand),
//                                String.format("%.2f", reorderPoint));
//                        log.info("- 补货建议: 考虑到用量波动性(CV={})和安全库存系数({})",
//                                String.format("%.2f", cv),
//                                String.format("%.2f", adjustedServiceZ));
//
//                        // 确保设置了补货建议文本
//                        String tipText = String.format("建议补货%d个，当前库存%.1f，再订货点%.1f",
//                                orderQty, onHand, reorderPoint);
//                        departmentDisGoodsEntity.setNxTipText(tipText);
//
//                        finalResult.add(departmentDisGoodsEntity);
//                    }
//                } else {
//                    // 库存充足的情况，也设置这些字段
//                    setAiFields(departmentDisGoodsEntity, goods, onHand, mu, recentAvg, cv, safetyStock,
//                            reorderPoint, lastDate, lastOrderQty, lo, daysSince, tomorrowNeed, 0); // 订货量为0
//
//                    log.info("商品[{}]库存充足: 当前库存({}) >= 再订货点({})",
//                            goods.getNxDgGoodsName(),
//                            String.format("%.2f", onHand),
//                            String.format("%.2f", reorderPoint));
//                    log.info("- 库存状态: 考虑到用量波动性(CV={})和安全库存系数({})",
//                            String.format("%.2f", cv),
//                            String.format("%.2f", adjustedServiceZ));
//
//                    // 设置库存充足的建议文本
//                    String tipText = String.format("库存充足，当前库存%.1f，再订货点%.1f",
//                            onHand, reorderPoint);
//                    departmentDisGoodsEntity.setNxTipText(tipText);
//
//                    finalResult.add(departmentDisGoodsEntity);
//                }
//            }
//
//            // 判断是否需要继续查询下一页
//            if (finalResult.size() < limit) {
//                if (goodsIds.size() < fetchSize) {
//                    log.info("当前满足条件的商品数{}，未达到目标数量{}，但数据已查完，停止查询",
//                            finalResult.size(), limit);
//                    hasMore = false;
//                } else {
//                    log.info("当前满足条件的商品数{}，未达到目标数量{}，继续查询下一页",
//                            finalResult.size(), limit);
//                }
//            } else {
//                log.info("已达到目标数量{}，停止查询", limit);
//                hasMore = false;
//            }
//
//            innerPage++;
//        }
//
//        long totalQueryTime = System.currentTimeMillis() - queryStartTime;
//        log.info("持续查询完成: 执行{}次查询, 总耗时: {}ms, 查询商品总数: {}, 满足条件的商品数: {}, 平均每次查询耗时: {}ms",
//                totalQueries,
//                totalQueryTime,
//                allGoodsIds.size(),
//                finalResult.size(),
//                totalQueries > 0 ? totalQueryTime / totalQueries : 0);
//
//        // 获取总数
//        int total = historyService.selectFrequentGoodsCount(freqParams);
//        log.info("筛选常订商品耗时: {}ms, 总共获取到{}个商品, 数据库总记录数: {}",
//                System.currentTimeMillis() - startTime,
//                finalResult.size(),
//                total);
//
//        if (finalResult.isEmpty()) {
//            log.info("没有找到常订商品");
//            return new PageUtils(new ArrayList<>(), 0, limit, page);
//        }
//
//        // 确保最终处理的商品数量不超过limit
//        int finalGoodsCount = Math.min(limit, finalResult.size());
//        List<NxDepartmentDisGoodsEntity> limitedResult = finalResult.subList(0, finalGoodsCount);
//        log.info("最终处理商品数量限制: 查询到{}个商品，限制为{}个，实际处理{}个",
//                finalResult.size(),
//                limit,
//                finalGoodsCount);
//
//        // 预加载所有需要的商品信息
//        startTime = System.currentTimeMillis();
//        Map<Integer, NxDistributerGoodsEntity> goodsMap = new HashMap<>();
//        for (NxDepartmentDisGoodsEntity goods : limitedResult) {
//            Integer gid = goods.getNxDdgDisGoodsId();
//            NxDistributerGoodsEntity goodsEntity = nxDistributerGoodsService.queryObject(gid);
//            if (goodsEntity != null) {
//                goodsMap.put(gid, goodsEntity);
//                log.info("加载商品信息: ID={}, 名称={}", gid, goodsEntity.getNxDgGoodsName());
//            } else {
//                log.warn("商品[{}]信息不存在", gid);
//            }
//        }
//
//        // 预加载所有需要的父类信息
//        startTime = System.currentTimeMillis();
//        Set<Integer> fatherIds = new HashSet<>();
//        for (NxDistributerGoodsEntity goods : goodsMap.values()) {
//            if (goods.getNxDgDfgGoodsFatherId() != null) {
//                fatherIds.add(goods.getNxDgDfgGoodsFatherId());
//            }
//        }
//        log.info("需要加载的父类ID数量: {}", fatherIds.size());
//
//        Map<Integer, NxDistributerFatherGoodsEntity> fatherGoodsMap = new HashMap<>();
//        if (!fatherIds.isEmpty()) {
//            List<NxDistributerFatherGoodsEntity> fatherGoods = nxDistributerFatherGoodsService.queryListByIds(new ArrayList<>(fatherIds));
//            fatherGoods.forEach(father -> {
//                fatherGoodsMap.put(father.getNxDistributerFatherGoodsId(), father);
//                log.info("加载父类信息: ID={}, 名称={}",
//                        father.getNxDistributerFatherGoodsId(),
//                        father.getNxDfgFatherGoodsName());
//            });
//        }
//        log.info("加载父类信息耗时: {}ms, 父类数量: {}", System.currentTimeMillis() - startTime, fatherGoodsMap.size());
//
//        // 预加载所有需要的祖父类信息
//        startTime = System.currentTimeMillis();
//        Set<Integer> grandIds = new HashSet<>();
//        for (NxDistributerFatherGoodsEntity father : fatherGoodsMap.values()) {
//            if (father.getNxDfgFathersFatherId() != null) {
//                grandIds.add(father.getNxDfgFathersFatherId());
//            }
//        }
//        log.info("需要加载的祖父类ID数量: {}", grandIds.size());
//
//        Map<Integer, NxDistributerFatherGoodsEntity> grandGoodsMap = new HashMap<>();
//        if (!grandIds.isEmpty()) {
//            List<NxDistributerFatherGoodsEntity> grandGoods = nxDistributerFatherGoodsService.queryListByIds(new ArrayList<>(grandIds));
//            grandGoods.forEach(grand -> {
//                grandGoodsMap.put(grand.getNxDistributerFatherGoodsId(), grand);
//                log.info("加载祖父类信息: ID={}, 名称={}",
//                        grand.getNxDistributerFatherGoodsId(),
//                        grand.getNxDfgFatherGoodsName());
//            });
//        }
//        log.info("加载祖父类信息耗时: {}ms, 祖父类数量: {}", System.currentTimeMillis() - startTime, grandGoodsMap.size());
//
//        // 预加载所有需要的曾祖父类信息
//        startTime = System.currentTimeMillis();
//        Set<Integer> greatGrandIds = new HashSet<>();
//        for (NxDistributerFatherGoodsEntity grand : grandGoodsMap.values()) {
//            if (grand.getNxDfgFathersFatherId() != null) {
//                greatGrandIds.add(grand.getNxDfgFathersFatherId());
//            }
//        }
//        log.info("需要加载的曾祖父类ID数量: {}", greatGrandIds.size());
//
//        Map<Integer, NxDistributerFatherGoodsEntity> greatGrandGoodsMap = new HashMap<>();
//        if (!greatGrandIds.isEmpty()) {
//            List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDistributerFatherGoodsService.queryListByIds(new ArrayList<>(greatGrandIds));
//            greatGrandGoods.forEach(greatGrand -> {
//                greatGrandGoodsMap.put(greatGrand.getNxDistributerFatherGoodsId(), greatGrand);
//                log.info("加载曾祖父类信息: ID={}, 名称={}",
//                        greatGrand.getNxDistributerFatherGoodsId(),
//                        greatGrand.getNxDfgFatherGoodsName());
//            });
//        }
//        log.info("加载曾祖父类信息耗时: {}ms, 曾祖父类数量: {}", System.currentTimeMillis() - startTime, greatGrandGoodsMap.size());
//
//        // 预计算分类信息用于排序
//        Map<Integer, CategoryInfo> categoryInfoMap = new HashMap<>();
//        for (NxDepartmentDisGoodsEntity goods : limitedResult) {
//            Integer goodsId = goods.getNxDdgDisGoodsId();
//            NxDistributerGoodsEntity goodsEntity = goodsMap.get(goodsId);
//            if (goodsEntity != null) {
//                Integer fatherId = goodsEntity.getNxDgDfgGoodsFatherId();
//                NxDistributerFatherGoodsEntity fatherEntity = fatherGoodsMap.get(fatherId);
//                if (fatherEntity != null) {
//                    Integer grandId = fatherEntity.getNxDfgFathersFatherId();
//                    NxDistributerFatherGoodsEntity grandEntity = grandGoodsMap.get(grandId);
//                    if (grandEntity != null) {
//                        Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
//                        NxDistributerFatherGoodsEntity greatGrandEntity = greatGrandGoodsMap.get(greatGrandId);
//
//                        categoryInfoMap.put(goodsId, new CategoryInfo(
//                                fatherEntity.getNxDfgFatherGoodsSort(),
//                                grandEntity.getNxDfgFatherGoodsSort(),
//                                greatGrandEntity != null ? greatGrandEntity.getNxDfgFatherGoodsSort() : 0
//                        ));
//                    }
//                }
//            }
//        }
//
//        // 排序
//        limitedResult.sort((a, b) -> {
//            CategoryInfo infoA = categoryInfoMap.get(a.getNxDdgDisGoodsId());
//            CategoryInfo infoB = categoryInfoMap.get(b.getNxDdgDisGoodsId());
//            if (infoA == null || infoB == null) return 0;
//
//            // 首先比较曾祖父类别的sort
//            int greatCompare = Integer.compare(
//                    infoA.getGreatSort() != null ? infoA.getGreatSort() : 0,
//                    infoB.getGreatSort() != null ? infoB.getGreatSort() : 0
//            );
//            if (greatCompare != 0) return greatCompare;
//
//            // 然后比较祖父类别的sort
//            int grandCompare = Integer.compare(
//                    infoA.getGrandSort() != null ? infoA.getGrandSort() : 0,
//                    infoB.getGrandSort() != null ? infoB.getGrandSort() : 0
//            );
//            if (grandCompare != 0) return grandCompare;
//
//            // 最后比较父类的sort
//            return Integer.compare(
//                    infoA.getFatherSort() != null ? infoA.getFatherSort() : 0,
//                    infoB.getFatherSort() != null ? infoB.getFatherSort() : 0
//            );
//        });
//
//        log.info("总耗时: {}ms", System.currentTimeMillis() - totalStartTime);
//        PageUtils pageUtil = new PageUtils(limitedResult, total, limit, page);
//
//        return pageUtil;
//    }



//    @Override
//    public PageUtils computeReorderOk(Integer depId, Integer page, Integer limit) {
//        long totalStartTime = System.currentTimeMillis();
//        log.info("开始计算部门[{}]的补货建议，页码：{}，每页数量：{}", depId, page, limit);
//
//        // 获取正在下单商品
//        List<Integer> orderingGoodsIds = getOrderingGoodsIds(depId);
//        log.info("正在下单商品ID列表: {}", orderingGoodsIds);
//
//        // 参数设置
//        int windowDays = 56, minTimes = 6, leadTime = 1;
//        double serviceZ = 1.65;
//
//        // 查询常订商品总数
//        Map<String, Object> countParams = new HashMap<>();
//        countParams.put("depId", depId);
//        countParams.put("windowDays", windowDays);
//        countParams.put("minTimes", minTimes);
//        int total = historyService.selectFrequentGoodsCount(countParams);
//        log.info("常订商品总数: {}", total);
//
//        List<NxDepartmentDisGoodsEntity> result = new ArrayList<>();
//        int fetchSize = limit * 2;
//        int currentOffset = 0;
//        int round = 0;
//
//        while (result.size() < limit && currentOffset < total) {
//            round++;
//            log.info("补货计算第{}轮分页，当前offset: {}，当前结果数量: {}", round, currentOffset, result.size());
//
//            Map<String, Object> freqParams = new HashMap<>();
//            freqParams.put("depId", depId);
//            freqParams.put("windowDays", windowDays);
//            freqParams.put("minTimes", minTimes);
//            freqParams.put("offset", currentOffset);
//            freqParams.put("limit", fetchSize);
//
//            List<Integer> goodsIds = historyService.selectFrequentGoodsWithPage(freqParams);
//            log.info("分页获取常订商品数量: {}", goodsIds != null ? goodsIds.size() : 0);
//            if (goodsIds == null || goodsIds.isEmpty()) break;
//
//            for (Integer gid : goodsIds) {
//                if (orderingGoodsIds.contains(gid)) {
//                    log.debug("商品[{}]正在下单中，跳过", gid);
//                    continue;
//                }
//
//                NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(gid);
//                if (goods == null) {
//                    log.warn("商品[{}]信息不存在，跳过", gid);
//                    continue;
//                }
//
//                boolean alreadyInResult = result.stream()
//                        .anyMatch(r -> r.getNxDdgDisGoodsId().equals(gid));
//                if (alreadyInResult) continue;
//
//                NxDepartmentDisGoodsEntity reorderResult = computeReorderForGoods(goods, depId, windowDays, leadTime, serviceZ);
//                if (reorderResult != null) {
//                    result.add(reorderResult);
//                    log.info("商品[{}]加入补货列表，当前总数: {}", gid, result.size());
//                }
//
//                if (result.size() >= limit) break;
//            }
//
//            currentOffset += goodsIds.size();
//        }
//
//        log.info("补货分页循环完成，最终补货商品数: {}", result.size());
//
//        // 加载商品 → 分类 → 父类 → 祖父类 → 曾祖父类信息（用于排序）
//        Map<Integer, NxDistributerGoodsEntity> goodsMap = result.stream()
//                .collect(Collectors.toMap(NxDepartmentDisGoodsEntity::getNxDdgDisGoodsId, NxDepartmentDisGoodsEntity::getNxDistributerGoodsEntity));
//
//        Set<Integer> fatherIds = goodsMap.values().stream()
//                .map(NxDistributerGoodsEntity::getNxDgDfgGoodsFatherId)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toSet());
//        log.info("需要加载的父类ID数量: {}", fatherIds.size());
//
//        Map<Integer, NxDistributerFatherGoodsEntity> fatherGoodsMap = new HashMap<>();
//        if (!fatherIds.isEmpty()) {
//            List<NxDistributerFatherGoodsEntity> fatherGoods = nxDistributerFatherGoodsService.queryListByIds(new ArrayList<>(fatherIds));
//            for (NxDistributerFatherGoodsEntity fg : fatherGoods) {
//                fatherGoodsMap.put(fg.getNxDistributerFatherGoodsId(), fg);
//            }
//        }
//
//        Set<Integer> grandIds = fatherGoodsMap.values().stream()
//                .map(NxDistributerFatherGoodsEntity::getNxDfgFathersFatherId)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toSet());
//
//        Map<Integer, NxDistributerFatherGoodsEntity> grandGoodsMap = new HashMap<>();
//        if (!grandIds.isEmpty()) {
//            List<NxDistributerFatherGoodsEntity> grandGoods = nxDistributerFatherGoodsService.queryListByIds(new ArrayList<>(grandIds));
//            for (NxDistributerFatherGoodsEntity gg : grandGoods) {
//                grandGoodsMap.put(gg.getNxDistributerFatherGoodsId(), gg);
//            }
//        }
//
//        Set<Integer> greatGrandIds = grandGoodsMap.values().stream()
//                .map(NxDistributerFatherGoodsEntity::getNxDfgFathersFatherId)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toSet());
//
//        Map<Integer, NxDistributerFatherGoodsEntity> greatGrandGoodsMap = new HashMap<>();
//        if (!greatGrandIds.isEmpty()) {
//            List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDistributerFatherGoodsService.queryListByIds(new ArrayList<>(greatGrandIds));
//            for (NxDistributerFatherGoodsEntity ggg : greatGrandGoods) {
//                greatGrandGoodsMap.put(ggg.getNxDistributerFatherGoodsId(), ggg);
//            }
//        }
//
//        Map<Integer, CategoryInfo> categoryInfoMap = new HashMap<>();
//        for (NxDepartmentDisGoodsEntity goods : result) {
//            Integer goodsId = goods.getNxDdgDisGoodsId();
//            NxDistributerGoodsEntity g = goodsMap.get(goodsId);
//            if (g != null) {
//                Integer fatherId = g.getNxDgDfgGoodsFatherId();
//                NxDistributerFatherGoodsEntity father = fatherGoodsMap.get(fatherId);
//                if (father != null) {
//                    Integer grandId = father.getNxDfgFathersFatherId();
//                    NxDistributerFatherGoodsEntity grand = grandGoodsMap.get(grandId);
//                    if (grand != null) {
//                        Integer greatId = grand.getNxDfgFathersFatherId();
//                        NxDistributerFatherGoodsEntity great = greatGrandGoodsMap.get(greatId);
//                        categoryInfoMap.put(goodsId, new CategoryInfo(
//                                father.getNxDfgFatherGoodsSort(),
//                                grand.getNxDfgFatherGoodsSort(),
//                                great != null ? great.getNxDfgFatherGoodsSort() : 0
//                        ));
//                    }
//                }
//            }
//        }
//        log.info("商品分类信息构建完成，映射数量: {}", categoryInfoMap.size());
//
//        result.sort((a, b) -> {
//            CategoryInfo infoA = categoryInfoMap.get(a.getNxDdgDisGoodsId());
//            CategoryInfo infoB = categoryInfoMap.get(b.getNxDdgDisGoodsId());
//            if (infoA == null || infoB == null) return 0;
//            int cmpGreat = Integer.compare(infoA.getGreatSort(), infoB.getGreatSort());
//            if (cmpGreat != 0) return cmpGreat;
//            int cmpGrand = Integer.compare(infoA.getGrandSort(), infoB.getGrandSort());
//            if (cmpGrand != 0) return cmpGrand;
//            return Integer.compare(infoA.getFatherSort(), infoB.getFatherSort());
//        });
//        log.info("排序完成，最终可分页商品数量: {}", result.size());
//
//        List<NxDepartmentDisGoodsEntity> pagedResult = result.stream()
//                .skip((long) (page - 1) * limit)
//                .limit(limit)
//                .collect(Collectors.toList());
//        log.info("最终分页返回{}条记录，处理总耗时: {}ms", pagedResult.size(), System.currentTimeMillis() - totalStartTime);
//
//        return new PageUtils(pagedResult, total, limit, page);
//    }




//    @Override
//    public PageUtils computeReorder(Integer depId, Integer page, Integer limit) {
//        long totalStartTime = System.currentTimeMillis();
//        log.info("开始计算部门[{}]的补货建议，页码：{}，每页数量：{}", depId, page, limit);
//
//        // 1. 获取正在下单的商品
//        long startTime = System.currentTimeMillis();
//        Map<String, Object> mapOrder = new HashMap<>();
//        mapOrder.put("depId", depId);
//        mapOrder.put("status", 3);
//        List<Integer> orderingGoodsIds = nxDepartmentOrdersDao.queryGoodsIds(mapOrder);
//        log.info("获取正在下单商品耗时: {}ms", System.currentTimeMillis() - startTime);
//
//        // 2. 参数、常量定义
//        int windowDays = 56;       // 统计过去多少天
//        int minTimes = 6;          // 至少订货次数阈值
//        int leadTime = 1;          // 预计提前期（天）
//        double serviceZ = 1.65;    // 95% 服务水平，对应正态分布 Z 值
//
//        // 3. 筛出「常订」商品，并直接进行分页
//        startTime = System.currentTimeMillis();
//        Map<String, Object> freqParams = new HashMap<>();
//        freqParams.put("depId", depId);
//        freqParams.put("windowDays", windowDays);
//        freqParams.put("minTimes", minTimes);
//        freqParams.put("page", page);
//        freqParams.put("limit", limit);
//        freqParams.put("offset", (page - 1) * limit);  // 修改这里，使用 offset 作为参数名
//
//        // 修改为分页查询
//        List<Integer> goodsIds = historyService.selectFrequentGoodsWithPage(freqParams);
//        // 获取总数
//        int total = historyService.selectFrequentGoodsCount(freqParams);
//        log.info("筛选常订商品耗时: {}ms", System.currentTimeMillis() - startTime);
//
//        if (goodsIds == null || goodsIds.isEmpty()) {
//            log.info("没有找到常订商品");
//            return new PageUtils(new ArrayList<>(), 0, limit, page);
//        }
//        log.info("常订商品数量: {}", goodsIds.size());
//
//        // 4. 预加载所有需要的商品信息
//        startTime = System.currentTimeMillis();
//        Map<Integer, NxDistributerGoodsEntity> goodsMap = new HashMap<>();
//        for (Integer gid : goodsIds) {
//            if (orderingGoodsIds != null && orderingGoodsIds.contains(gid)) {
//                log.info("商品[{}]正在下单中，跳过", gid);
//                continue;
//            }
//
//            NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(gid);
//            if (goods != null) {
//                goodsMap.put(gid, goods);
//                log.info("加载商品信息: ID={}, 名称={}", gid, goods.getNxDgGoodsName());
//            } else {
//                log.warn("商品[{}]信息不存在", gid);
//            }
//        }
//
//        log.info("加载商品信息耗时: {}ms, 商品数量: {}", System.currentTimeMillis() - startTime, goodsMap.size());
//
//        // 5. 预加载所有需要的父类信息
//        startTime = System.currentTimeMillis();
//        Set<Integer> fatherIds = new HashSet<>();
//        for (NxDistributerGoodsEntity goods : goodsMap.values()) {
//            if (goods.getNxDgDfgGoodsFatherId() != null) {
//                fatherIds.add(goods.getNxDgDfgGoodsFatherId());
//            }
//        }
//        log.info("需要加载的父类ID数量: {}", fatherIds.size());
//
//        Map<Integer, NxDistributerFatherGoodsEntity> fatherGoodsMap = new HashMap<>();
//        if (!fatherIds.isEmpty()) {
//            List<NxDistributerFatherGoodsEntity> fatherGoods = nxDistributerFatherGoodsService.queryListByIds(new ArrayList<>(fatherIds));
//            fatherGoods.forEach(father -> {
//                fatherGoodsMap.put(father.getNxDistributerFatherGoodsId(), father);
//                log.info("加载父类信息: ID={}, 名称={}",
//                        father.getNxDistributerFatherGoodsId(),
//                        father.getNxDfgFatherGoodsName());
//            });
//        }
//        log.info("加载父类信息耗时: {}ms, 父类数量: {}", System.currentTimeMillis() - startTime, fatherGoodsMap.size());
//
//        // 6. 预加载所有需要的祖父类信息
//        startTime = System.currentTimeMillis();
//        Set<Integer> grandIds = new HashSet<>();
//        for (NxDistributerFatherGoodsEntity father : fatherGoodsMap.values()) {
//            if (father.getNxDfgFathersFatherId() != null) {
//                grandIds.add(father.getNxDfgFathersFatherId());
//            }
//        }
//        log.info("需要加载的祖父类ID数量: {}", grandIds.size());
//
//        Map<Integer, NxDistributerFatherGoodsEntity> grandGoodsMap = new HashMap<>();
//        if (!grandIds.isEmpty()) {
//            List<NxDistributerFatherGoodsEntity> grandGoods = nxDistributerFatherGoodsService.queryListByIds(new ArrayList<>(grandIds));
//            grandGoods.forEach(grand -> {
//                grandGoodsMap.put(grand.getNxDistributerFatherGoodsId(), grand);
//                log.info("加载祖父类信息: ID={}, 名称={}",
//                        grand.getNxDistributerFatherGoodsId(),
//                        grand.getNxDfgFatherGoodsName());
//            });
//        }
//        log.info("加载祖父类信息耗时: {}ms, 祖父类数量: {}", System.currentTimeMillis() - startTime, grandGoodsMap.size());
//
//        // 7. 预加载所有需要的曾祖父类信息
//        startTime = System.currentTimeMillis();
//        Set<Integer> greatGrandIds = new HashSet<>();
//        for (NxDistributerFatherGoodsEntity grand : grandGoodsMap.values()) {
//            if (grand.getNxDfgFathersFatherId() != null) {
//                greatGrandIds.add(grand.getNxDfgFathersFatherId());
//            }
//        }
//        log.info("需要加载的曾祖父类ID数量: {}", greatGrandIds.size());
//
//        Map<Integer, NxDistributerFatherGoodsEntity> greatGrandGoodsMap = new HashMap<>();
//        if (!greatGrandIds.isEmpty()) {
//            List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDistributerFatherGoodsService.queryListByIds(new ArrayList<>(greatGrandIds));
//            greatGrandGoods.forEach(greatGrand -> {
//                greatGrandGoodsMap.put(greatGrand.getNxDistributerFatherGoodsId(), greatGrand);
//                log.info("加载曾祖父类信息: ID={}, 名称={}",
//                        greatGrand.getNxDistributerFatherGoodsId(),
//                        greatGrand.getNxDfgFatherGoodsName());
//            });
//        }
//        log.info("加载曾祖父类信息耗时: {}ms, 曾祖父类数量: {}", System.currentTimeMillis() - startTime, greatGrandGoodsMap.size());
//
//        // 8. 计算补货建议
//        startTime = System.currentTimeMillis();
//        List<NxDepartmentDisGoodsEntity> result = new ArrayList<>();
//        LocalDate today = LocalDate.now(ZoneId.systemDefault());
//        log.info("开始计算补货建议，当前日期: {}", today);
//
//        for (Map.Entry<Integer, NxDistributerGoodsEntity> entry : goodsMap.entrySet()) {
//            long itemStartTime = System.currentTimeMillis();
//            Integer gid = entry.getKey();
//            NxDistributerGoodsEntity goods = entry.getValue();
//            log.info("开始处理商品[{}]的补货建议", goods.getNxDgGoodsName());
//
//            // 8.1 获取历史订货记录
//            Map<String, Object> orderHistoryParams = new HashMap<>();
//            orderHistoryParams.put("depId", depId);
//            orderHistoryParams.put("disGoodsId", gid);
//            orderHistoryParams.put("limit", 20);
//            List<NxDepartmentOrderHistoryEntity> orderHistory = historyService.queryDisHistoryOrdersByParamsForAi(orderHistoryParams);
//            log.info("商品[{}]历史订货记录数量: {}", goods.getNxDgGoodsName(),
//                    orderHistory != null ? orderHistory.size() : 0);
//
//            // 8.2 分析历史订货模式
//            double avgOrderQty = 0.0;
//            double modeOrderQty = 0.0;  // 最常订的数量
//            double medianOrderQty = 0.0;  // 中位数订货量
//            int orderFrequency = 0;  // 平均订货间隔（天）
//
//            if (orderHistory != null && !orderHistory.isEmpty()) {
//                // 计算平均订货量
//                avgOrderQty = orderHistory.stream()
//                        .mapToDouble(order -> Double.parseDouble(order.getNxDoQuantity()))
//                        .average()
//                        .orElse(0.0);
//
//                // 计算最常订的数量（众数）
//                Map<Double, Long> qtyFrequency = orderHistory.stream()
//                        .map(order -> Double.parseDouble(order.getNxDoQuantity()))
//                        .collect(Collectors.groupingBy(qty -> qty, Collectors.counting()));
//
//                modeOrderQty = qtyFrequency.entrySet().stream()
//                        .max(Map.Entry.comparingByValue())
//                        .map(Map.Entry::getKey)
//                        .orElse(0.0);
//
//                // 计算中位数订货量
//                List<Double> sortedQuantities = orderHistory.stream()
//                        .map(order -> Double.parseDouble(order.getNxDoQuantity()))
//                        .sorted()
//                        .collect(Collectors.toList());
//                int mid = sortedQuantities.size() / 2;
//                medianOrderQty = sortedQuantities.size() % 2 == 0 ?
//                        (sortedQuantities.get(mid - 1) + sortedQuantities.get(mid)) / 2 :
//                        sortedQuantities.get(mid);
//
//                // 计算平均订货间隔
//                List<LocalDate> orderDates = orderHistory.stream()
//                        .map(order -> LocalDate.parse(order.getNxDoApplyDate()))
//                        .sorted()
//                        .collect(Collectors.toList());
//
//                if (orderDates.size() > 1) {
//                    long totalDays = ChronoUnit.DAYS.between(orderDates.get(0), orderDates.get(orderDates.size() - 1));
//                    orderFrequency = (int) (totalDays / (orderDates.size() - 1));
//                }
//
//                log.info("商品[{}]历史订货分析:", goods.getNxDgGoodsName());
//                log.info("- 最近{}次订货记录", orderHistory.size());
//                log.info("- 平均订货量: {}", String.format("%.2f", avgOrderQty));
//                log.info("- 最常订数量: {}", String.format("%.2f", modeOrderQty));
//                log.info("- 中位数订货量: {}", String.format("%.2f", medianOrderQty));
//                log.info("- 平均订货间隔: {}天", orderFrequency);
//            }
//
//            // 8.3 获取最近一次订货记录
//            Map<String, Object> lastParams = new HashMap<>();
//            lastParams.put("depId", depId);
//            lastParams.put("gid", gid);
//            if (goods.getNxDgQuantityDays() != null) {
//                lastParams.put("dayuDate", formatWhatDay(-goods.getNxDgQuantityDays()));
//                log.info("商品[{}]有保质期限制: {}天, 查询起始日期: {}",
//                        goods.getNxDgGoodsName(),
//                        goods.getNxDgQuantityDays(),
//                        formatWhatDay(-goods.getNxDgQuantityDays()));
//            }
//
//            System.out.println("mappapap" + lastParams);
//
//            NxDepartmentOrderHistoryEntity lo = historyService.selectLastOrder(lastParams);
//            if (lo == null) {
//                log.info("商品[{}]在保质期内没有订货记录", goods.getNxDgGoodsName());
//                continue;
//            }
//            log.info("商品[{}]最近一次订货日期: {}, 订货数量: {}",
//                    goods.getNxDgGoodsName(),
//                    lo.getNxDoApplyDate(),
//                    lo.getNxDoQuantity());
//
//            // 8.4 获取每日用量数据
//            Map<String, Object> usageParams = new HashMap<>();
//            usageParams.put("depId", depId);
//            usageParams.put("gid", gid);
//            usageParams.put("windowDays", windowDays);
//            List<DailyUsage> usages = historyService.selectDailyUsage(usageParams);
//
//            if (usages == null || usages.size() < 5) {
//                log.info("商品[{}]历史用量数据不足: {}条记录",
//                        goods.getNxDgGoodsName(),
//                        usages != null ? usages.size() : 0);
//                continue;
//            }
//            log.info("商品[{}]历史用量数据充足: {}条记录",
//                    goods.getNxDgGoodsName(),
//                    usages.size());
//
//            // 8.5 计算用量统计指标
//            List<Double> dailyQuantities = usages.stream()
//                    .map(DailyUsage::getQty)
//                    .collect(Collectors.toList());
//
//            double sum = dailyQuantities.stream().mapToDouble(Double::doubleValue).sum();
//            double mu = sum / windowDays;  // 日均用量
//
//            // 计算最近7天的用量趋势
//            List<Double> recentQuantities = dailyQuantities.subList(Math.max(0, dailyQuantities.size() - 7), dailyQuantities.size());
//            double recentAvg = recentQuantities.stream()
//                    .filter(qty -> qty > 0) // 排除0值
//                    .filter(qty -> qty <= mu * 3) // 排除异常值（超过平均值的3倍）
//                    .mapToDouble(Double::doubleValue)
//                    .average()
//                    .orElse(mu); // 如果没有有效值，使用长期平均值
//            // 计算用量波动性
//            double variance = dailyQuantities.stream()
//                    .mapToDouble(qty -> Math.pow(qty - mu, 2))
//                    .sum() / windowDays;
//            double sigma = Math.sqrt(variance);
//            double cv = sigma / mu;  // 变异系数，反映用量波动程度
//
//            double safetyStock = serviceZ * sigma * Math.sqrt(leadTime);
//            double reorderPoint = mu * leadTime + safetyStock;
//
//            log.info("商品[{}]用量分析:", goods.getNxDgGoodsName());
//            log.info("- 长期日均用量: {}", String.format("%.2f", mu));
//            log.info("- 最近7天平均用量: {}", String.format("%.2f", recentAvg));
//            log.info("- 用量波动性(CV): {}", String.format("%.2f", cv));
//            log.info("- 安全库存: {}", String.format("%.2f", safetyStock));
//            log.info("- 再订货点: {}", String.format("%.2f", reorderPoint));
//
//            // 8.6 计算当前库存
//            LocalDate lastDate = LocalDate.parse(lo.getNxDoApplyDate());
//            long daysSince = Math.max(0, ChronoUnit.DAYS.between(lastDate, today));
//
//            // 根据当前时间决定是否计算今天的消耗
//            LocalDateTime now = LocalDateTime.now();
//            double todayConsumed = 0.0;
//
//            // 获取最近一次订货的时间
//            LocalDateTime lastOrderTime = null;
//            try {
//                lastOrderTime = LocalDateTime.parse(lo.getNxDoApplyDate() + " " + lo.getNxDoApplyOnlyTime());
//            } catch (Exception e) {
//                log.warn("商品[{}]时间解析失败: {}", goods.getNxDgGoodsName(), lo.getNxDoApplyOnlyTime());
//            }
//
//            // 判断是否计算今天消耗的逻辑
//            if (now.getHour() >= 12) {  // 如果是下午
//                // 使用最近7天平均用量作为今天的消耗估算
//                todayConsumed = recentAvg;
//                log.info("当前时间: {}, 已过中午，估算今天消耗: {}",
//                        now.format(DateTimeFormatter.ofPattern("HH:mm")),
//                        String.format("%.2f", todayConsumed));
//            } else if (now.getHour() < 4) {  // 如果是凌晨0-4点
//                // 检查是否是昨天的订单
//                if (lastOrderTime != null && lastOrderTime.getHour() >= 0 && lastOrderTime.getHour() < 4) {
//                    // 如果最近一次订货是在凌晨0-4点，且是昨天的订单，则计入昨天的消耗
//                    if (lastDate.equals(today.minusDays(1))) {
//                        todayConsumed = recentAvg;
//                        log.info("当前时间: {}, 凌晨时段，且最近一次订货是昨天凌晨，计入昨天消耗: {}",
//                                now.format(DateTimeFormatter.ofPattern("HH:mm")),
//                                String.format("%.2f", todayConsumed));
//                    }
//                }
//            } else {
//                log.info("当前时间: {}, 未过中午，不计入今天消耗",
//                        now.format(DateTimeFormatter.ofPattern("HH:mm")));
//            }
//
//            // 计算当前实际库存
//            double consumed = daysSince * mu;
//            double lastOrderQty = Double.parseDouble(lo.getNxDoQuantity());
//            double onHand = lastOrderQty - consumed - todayConsumed;  // 减去今天的消耗
//
//            // 预测明天的需求
//            double tomorrowNeed = 0.7 * recentAvg + 0.3 * mu;  // 更重视近期趋势
//            double totalNeed = onHand + tomorrowNeed;  // 当前库存 + 明天用量
//
//            log.info("商品[{}]库存分析:", goods.getNxDgGoodsName());
//            log.info("- 上次订货日期: {}, 距离今天: {}天", lastDate, daysSince);
//            log.info("- 预计消耗: {}, 今天消耗: {}, 上次订货量: {}, 当前库存: {}",
//                    String.format("%.2f", consumed),
//                    String.format("%.2f", todayConsumed),
//                    String.format("%.2f", lastOrderQty),
//                    String.format("%.2f", onHand));
//            log.info("- 明天预期用量: {}, 总需求(库存+明天): {}",
//                    String.format("%.2f", tomorrowNeed),
//                    String.format("%.2f", totalNeed));
//
//            if (onHand < reorderPoint) {  // 当前库存低于再订货点时补货
//                // 计算最佳订货量
//                int orderQty = calculateOptimalOrderQuantity(orderHistory, onHand, reorderPoint, tomorrowNeed);
//
//                if (orderQty > 0) {
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("disGoodsId", gid);
//                    map.put("depId", depId);
//                    NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
//                    if (nxDepartmentDisGoodsEntity == null) {
//                        log.warn("商品[{}]部门商品信息不存在", goods.getNxDgGoodsName());
//                        continue;
//                    }
//
//                    // 保存所有计算结果到字段
//                    nxDepartmentDisGoodsEntity.setAiCurrentStock(String.format("%.2f", onHand));
//                    nxDepartmentDisGoodsEntity.setAiCurrentStockUnit(goods.getNxDgGoodsStandardname());
//                    nxDepartmentDisGoodsEntity.setAiDailyUsage(String.format("%.2f", mu));
//                    nxDepartmentDisGoodsEntity.setAiRecentAvgUsage(String.format("%.2f", recentAvg));
//                    nxDepartmentDisGoodsEntity.setAiUsageVariation(String.format("%.2f", cv));
//                    nxDepartmentDisGoodsEntity.setAiSafetyStock(String.format("%.2f", safetyStock));
//                    nxDepartmentDisGoodsEntity.setAiReorderPoint(String.format("%.2f", reorderPoint));
//                    nxDepartmentDisGoodsEntity.setAiLastOrderDate(lastDate.toString());
//                    nxDepartmentDisGoodsEntity.setAiLastOrderQuantity(String.format("%.2f", lastOrderQty));
//                    nxDepartmentDisGoodsEntity.setAiLastOrderUnit(lo.getNxDoStandard());
//                    nxDepartmentDisGoodsEntity.setAiDaysSinceLastOrder(String.valueOf(daysSince));
//                    nxDepartmentDisGoodsEntity.setAiTomorrowNeed(String.format("%.2f", tomorrowNeed));
//                    nxDepartmentDisGoodsEntity.setAiAvailableDays(String.format("%.1f", onHand / mu));
//                    nxDepartmentDisGoodsEntity.setAiOrderQuantity(String.valueOf(orderQty));
//                    nxDepartmentDisGoodsEntity.setAiOrderStandard(nxDepartmentDisGoodsEntity.getNxDdgOrderStandard());
//
//                    nxDepartmentDisGoodsEntity.setNxDistributerGoodsEntity(goods);
//
//                    result.add(nxDepartmentDisGoodsEntity);
//                    log.info("商品[{}]需要补货: {}个", goods.getNxDgGoodsName(), orderQty);
//                    log.info("- 补货原因: 当前库存({}) < 再订货点({})",
//                            String.format("%.2f", onHand),
//                            String.format("%.2f", reorderPoint));
//                }
//            } else {
//                log.info("商品[{}]无需补货", goods.getNxDgGoodsName());
//                log.info("- 原因: 当前库存({}) >= 再订货点({})",
//                        String.format("%.2f", onHand),
//                        String.format("%.2f", reorderPoint));
//            }
//
//            log.info("处理商品[{}]耗时: {}ms", goods.getNxDgGoodsName(), System.currentTimeMillis() - itemStartTime);
//        }
//
//        log.info("计算补货建议总耗时: {}ms, 结果数量: {}", System.currentTimeMillis() - startTime, result.size());
//
//        // 9. 预计算分类信息用于排序
//        startTime = System.currentTimeMillis();
//        Map<Integer, CategoryInfo> categoryInfoMap = new HashMap<>();
//        log.info("开始预计算分类信息用于排序");
//        for (NxDepartmentDisGoodsEntity goods : result) {
//            Integer goodsId = goods.getNxDdgDisGoodsId();
//            NxDistributerGoodsEntity goodsEntity = goodsMap.get(goodsId);
//            if (goodsEntity != null) {
//                Integer fatherId = goodsEntity.getNxDgDfgGoodsFatherId();
//                NxDistributerFatherGoodsEntity fatherEntity = fatherGoodsMap.get(fatherId);
//                if (fatherEntity != null) {
//                    Integer grandId = fatherEntity.getNxDfgFathersFatherId();
//                    NxDistributerFatherGoodsEntity grandEntity = grandGoodsMap.get(grandId);
//                    if (grandEntity != null) {
//                        Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
//                        NxDistributerFatherGoodsEntity greatGrandEntity = greatGrandGoodsMap.get(greatGrandId);
//
//                        categoryInfoMap.put(goodsId, new CategoryInfo(
//                                fatherEntity.getNxDfgFatherGoodsSort(),
//                                grandEntity.getNxDfgFatherGoodsSort(),
//                                greatGrandEntity != null ? greatGrandEntity.getNxDfgFatherGoodsSort() : 0
//                        ));
//                    }
//                }
//            }
//        }
//        log.info("预计算分类信息耗时: {}ms", System.currentTimeMillis() - startTime);
//
//        // 10. 排序
//        startTime = System.currentTimeMillis();
//        result.sort((a, b) -> {
//            CategoryInfo infoA = categoryInfoMap.get(a.getNxDdgDisGoodsId());
//            CategoryInfo infoB = categoryInfoMap.get(b.getNxDdgDisGoodsId());
//            if (infoA == null || infoB == null) return 0;
//
//            // 首先比较曾祖父类别的sort
//            int greatCompare = Integer.compare(
//                    infoA.getGreatSort() != null ? infoA.getGreatSort() : 0,
//                    infoB.getGreatSort() != null ? infoB.getGreatSort() : 0
//            );
//            if (greatCompare != 0) return greatCompare;
//
//            // 然后比较祖父类别的sort
//            int grandCompare = Integer.compare(
//                    infoA.getGrandSort() != null ? infoA.getGrandSort() : 0,
//                    infoB.getGrandSort() != null ? infoB.getGrandSort() : 0
//            );
//            if (grandCompare != 0) return grandCompare;
//
//            // 最后比较父类的sort
//            return Integer.compare(
//                    infoA.getFatherSort() != null ? infoA.getFatherSort() : 0,
//                    infoB.getFatherSort() != null ? infoB.getFatherSort() : 0
//            );
//        });
//        log.info("排序耗时: {}ms", System.currentTimeMillis() - startTime);
//
//
//        // 11. 分页处理
//        startTime = System.currentTimeMillis();
//        log.info("分页处理耗时: {}ms", System.currentTimeMillis() - startTime);
//
//        log.info("总耗时: {}ms", System.currentTimeMillis() - totalStartTime);
//        PageUtils pageUtil = new PageUtils(result, total, limit, page);
//
//        return pageUtil;
//    }



//    public void computeReorder1(Integer depId, Integer page, Integer limit) {
//
//        List<NxDepartmentDisGoodsEntity> result = new ArrayList<>();
//        int currentPage = page;
//        int currentLimit = limit;
//
//        long totalStartTime = System.currentTimeMillis();
//        log.info("开始计算部门[{}]的补货建议，页码：{}，每页数量：{}", depId, page, limit);
//
//        // 1. 获取正在下单的商品
//        long startTime = System.currentTimeMillis();
//        Map<String, Object> mapOrder = new HashMap<>();
//        mapOrder.put("depId", depId);
//        mapOrder.put("status", 3);
//        List<Integer> orderingGoodsIds = nxDepartmentOrdersDao.queryGoodsIds(mapOrder);
//        log.info("获取正在下单商品耗时: {}ms", System.currentTimeMillis() - startTime);
//
//        // 2. 参数、常量定义
//        int windowDays = 56;       // 统计过去多少天
//        int minTimes = 6;          // 至少订货次数阈值
//        int leadTime = 1;          // 预计提前期（天）
//        double serviceZ = 1.65;    // 95% 服务水平，对应正态分布 Z 值
//
//        // 3. 筛出「常订」商品，并直接进行分页
//        startTime = System.currentTimeMillis();
//        Map<String, Object> freqParams = new HashMap<>();
//        freqParams.put("depId", depId);
//        freqParams.put("windowDays", windowDays);
//        freqParams.put("minTimes", minTimes);
//        freqParams.put("page", page);
//        freqParams.put("limit", limit);
//        freqParams.put("offset", (page - 1) * limit);  // 修改这里，使用 offset 作为参数名
//
//
//        while (result.size() < limit) {
//            // 使用原有的分页参数
//            freqParams.put("page", currentPage);
//            freqParams.put("limit", currentLimit);
//            freqParams.put("offset", (currentPage - 1) * currentLimit);
//
//            List<Integer> goodsIds = historyService.selectFrequentGoodsWithPage(freqParams);
//
//            int total = historyService.selectFrequentGoodsCount(freqParams);
//            log.info("筛选常订商品耗时: {}ms", System.currentTimeMillis() - startTime);
//
//            if (goodsIds == null || goodsIds.isEmpty()) {
//                log.info("没有找到常订商品");
////                return new PageUtils(new ArrayList<>(), 0, limit, page);
//
//            }
//            log.info("常订商品数量: {}", goodsIds.size());
//
//            // 4. 预加载所有需要的商品信息
//            startTime = System.currentTimeMillis();
//            // 处理商品逻辑保持不变
//            for (Integer gid : goodsIds) {
//                // ... 原有的商品处理逻辑 ...
//
//                if (result.size() >= limit) {
//                    break;
//                }
//            }
//
//            currentPage++;
//
//            if (goodsIds.size() < currentLimit) {
//                break; // 数据已经查完，不必继续了
//            }
//
//
//        }
//    }

    // 用于存储分类信息的内部类
    private static class CategoryInfo {
        private final Integer fatherSort;
        private final Integer grandSort;
        private final Integer greatSort;

        public CategoryInfo(Integer fatherSort, Integer grandSort, Integer greatSort) {
            this.fatherSort = fatherSort;
            this.grandSort = grandSort;
            this.greatSort = greatSort;
        }

        public Integer getFatherSort() {
            return fatherSort;
        }

        public Integer getGrandSort() {
            return grandSort;
        }

        public Integer getGreatSort() {
            return greatSort;
        }
    }

    //    /**
//     * 计算最佳订货量
//     * @param orderHistory 历史订货记录
//     * @param currentStock 当前库存
//     * @param reorderPoint 再订货点
//     * @param tomorrowNeed 明天预期用量
//     * @return 建议订货量
//     */

    private int calculateOptimalOrderQuantity(List<NxDepartmentOrderHistoryEntity> orderHistory,
                                              double currentStock,
                                              double reorderPoint,
                                              double tomorrowNeed) {
        if (orderHistory == null || orderHistory.isEmpty()) {
            // 如果没有历史记录，使用基础缺口
            return (int) Math.ceil(reorderPoint - currentStock);
        }

        // 1. 计算历史订货量的统计指标
        List<Double> orderQuantities = orderHistory.stream()
                .map(order -> Double.parseDouble(order.getNxDoQuantity()))
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

    public NxDepartmentDisGoodsEntity getTipText(NxDepartmentDisGoodsEntity departmentDisGoodsEntity) {

        // 2. 参数、常量定义
        int windowDays = 56;       // 统计过去多少天
        int minTimes = 6;          // 至少订货次数阈值
        int leadTime = 1;          // 预计提前期（天）
        double serviceZ = 1.65;    // 95% 服务水平，对应正态分布 Z 值
        Integer depId = departmentDisGoodsEntity.getNxDdgDepartmentId();

        long itemStartTime = System.currentTimeMillis();
        Integer gid = departmentDisGoodsEntity.getNxDdgDisGoodsId();

        NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(gid);
        if(goods != null){
            log.info("开始处理商品[{}]的补货建议", goods.getNxDgGoodsName());

            // 8.1 获取历史订货记录
            Map<String, Object> orderHistoryParams = new HashMap<>();
            orderHistoryParams.put("depId", depId);
            orderHistoryParams.put("disGoodsId", gid);
            orderHistoryParams.put("limit", 20);
            List<NxDepartmentOrderHistoryEntity> orderHistory = historyService.queryDisHistoryOrdersByParamsForAi(orderHistoryParams);
            log.info("商品[{}]历史订货记录数量: {}", goods.getNxDgGoodsName(),
                    orderHistory != null ? orderHistory.size() : 0);

            // 8.2 分析历史订货模式
            double avgOrderQty = 0.0;
            double modeOrderQty = 0.0;  // 最常订的数量
            double medianOrderQty = 0.0;  // 中位数订货量
            int orderFrequency = 0;  // 平均订货间隔（天）

            if (orderHistory != null && !orderHistory.isEmpty()) {
                // 计算平均订货量
                avgOrderQty = orderHistory.stream()
                        .mapToDouble(order -> Double.parseDouble(order.getNxDoQuantity()))
                        .average()
                        .orElse(0.0);

                // 计算最常订的数量（众数）
                Map<Double, Long> qtyFrequency = orderHistory.stream()
                        .map(order -> Double.parseDouble(order.getNxDoQuantity()))
                        .collect(Collectors.groupingBy(qty -> qty, Collectors.counting()));

                modeOrderQty = qtyFrequency.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(0.0);

                // 计算中位数订货量
                List<Double> sortedQuantities = orderHistory.stream()
                        .map(order -> Double.parseDouble(order.getNxDoQuantity()))
                        .sorted()
                        .collect(Collectors.toList());
                int mid = sortedQuantities.size() / 2;
                medianOrderQty = sortedQuantities.size() % 2 == 0 ?
                        (sortedQuantities.get(mid - 1) + sortedQuantities.get(mid)) / 2 :
                        sortedQuantities.get(mid);

                // 计算平均订货间隔
                List<LocalDate> orderDates = orderHistory.stream()
                        .map(order -> LocalDate.parse(order.getNxDoApplyDate()))
                        .sorted()
                        .collect(Collectors.toList());

                if (orderDates.size() > 1) {
                    long totalDays = ChronoUnit.DAYS.between(orderDates.get(0), orderDates.get(orderDates.size() - 1));
                    orderFrequency = (int) (totalDays / (orderDates.size() - 1));
                }

                log.info("商品[{}]历史订货分析:", goods.getNxDgGoodsName());
                log.info("- 最近{}次订货记录", orderHistory.size());
                log.info("- 平均订货量: {}", String.format("%.2f", avgOrderQty));
                log.info("- 最常订数量: {}", String.format("%.2f", modeOrderQty));
                log.info("- 中位数订货量: {}", String.format("%.2f", medianOrderQty));
                log.info("- 平均订货间隔: {}天", orderFrequency);
            }

            // 8.3 获取最近一次订货记录
            Map<String, Object> lastParams = new HashMap<>();
            lastParams.put("depId", depId);
            lastParams.put("gid", gid);
            if (goods.getNxDgQuantityDays() != null) {
                lastParams.put("dayuDate", formatWhatDay(-goods.getNxDgQuantityDays()));
                log.info("商品[{}]有保质期限制: {}天, 查询起始日期: {}",
                        goods.getNxDgGoodsName(),
                        goods.getNxDgQuantityDays(),
                        formatWhatDay(-goods.getNxDgQuantityDays()));
            }

            System.out.println("mappapap" + lastParams);

            NxDepartmentOrderHistoryEntity lo = historyService.selectLastOrder(lastParams);

            if (lo == null) {
                log.info("商品[{}]在保质期内没有订货记录", goods.getNxDgGoodsName());
//            continue;
                departmentDisGoodsEntity.setNxTipText("商品在"+ goods.getNxDgQuantityDays() +"日没有订货记录");
                return  departmentDisGoodsEntity;

            }
            log.info("商品[{}]最近一次订货日期: {}, 订货数量: {}",
                    goods.getNxDgGoodsName(),
                    lo.getNxDoApplyDate(),
                    lo.getNxDoQuantity());

            // 8.4 获取每日用量数据
            Map<String, Object> usageParams = new HashMap<>();
            usageParams.put("depId", depId);
            usageParams.put("gid", gid);
            usageParams.put("windowDays", windowDays);
            List<DailyUsage> usages = historyService.selectDailyUsage(usageParams);

            if (usages == null || usages.size() < 5) {
                log.info("商品[{}]历史用量数据不足: {}条记录",
                        goods.getNxDgGoodsName(),
                        usages != null ? usages.size() : 0);
                departmentDisGoodsEntity.setNxTipText("商品历史用量数据不足");

//            continue;
            }
            log.info("商品[{}]历史用量数据充足: {}条记录",
                    goods.getNxDgGoodsName(),
                    usages.size());

            // 8.5 计算用量统计指标
            List<Double> dailyQuantities = usages.stream()
                    .map(DailyUsage::getQty)
                    .collect(Collectors.toList());

            double sum = dailyQuantities.stream().mapToDouble(Double::doubleValue).sum();
            double mu = sum / windowDays;  // 日均用量

            // 计算最近7天的用量趋势
            List<Double> recentQuantities = dailyQuantities.subList(Math.max(0, dailyQuantities.size() - 7), dailyQuantities.size());
            double recentAvg = recentQuantities.stream()
                    .filter(qty -> qty > 0) // 排除0值
                    .filter(qty -> qty <= mu * 3) // 排除异常值（超过平均值的3倍）
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(mu); // 如果没有有效值，使用长期平均值

// 添加日志记录
            log.info("商品[{}]最近7天用量分析:", goods.getNxDgGoodsName());
            log.info("- 原始数据: {}", recentQuantities);
            log.info("- 过滤后数据: {}", recentQuantities.stream()
                    .filter(qty -> qty > 0)
                    .filter(qty -> qty <= mu * 3)
                    .collect(Collectors.toList()));
            log.info("- 计算得到的近期平均值: {}", String.format("%.2f", recentAvg));
            // 计算用量波动性
            double variance = dailyQuantities.stream()
                    .mapToDouble(qty -> Math.pow(qty - mu, 2))
                    .sum() / windowDays;
            double sigma = Math.sqrt(variance);
            double cv = sigma / mu;  // 变异系数，反映用量波动程度

            double safetyStock = serviceZ * sigma * Math.sqrt(leadTime);
            double reorderPoint = mu * leadTime + safetyStock;

            log.info("商品[{}]用量分析:", goods.getNxDgGoodsName());
            log.info("- 长期日均用量: {}", String.format("%.2f", mu));
            log.info("- 最近7天平均用量: {}", String.format("%.2f", recentAvg));
            log.info("- 用量波动性(CV): {}", String.format("%.2f", cv));
            log.info("- 安全库存: {}", String.format("%.2f", safetyStock));
            log.info("- 再订货点: {}", String.format("%.2f", reorderPoint));

            // 8.6 计算当前库存
            LocalDate lastDate = LocalDate.parse(lo.getNxDoApplyDate());
            LocalDate today = LocalDate.now(ZoneId.systemDefault());

            long daysSince = Math.max(0, ChronoUnit.DAYS.between(lastDate, today));

            // 根据当前时间决定是否计算今天的消耗
            LocalDateTime now = LocalDateTime.now();
            double todayConsumed = 0.0;

            // 获取最近一次订货的时间
            LocalDateTime lastOrderTime = null;
            try {
                lastOrderTime = LocalDateTime.parse(lo.getNxDoApplyDate() + " " + lo.getNxDoApplyOnlyTime());
            } catch (Exception e) {
                log.warn("商品[{}]时间解析失败: {}", goods.getNxDgGoodsName(), lo.getNxDoApplyOnlyTime());
            }

            // 判断是否计算今天消耗的逻辑
            if (now.getHour() >= 12) {  // 如果是下午
                // 使用最近7天平均用量作为今天的消耗估算
                todayConsumed = recentAvg;
                log.info("当前时间: {}, 已过中午，估算今天消耗: {}",
                        now.format(DateTimeFormatter.ofPattern("HH:mm")),
                        String.format("%.2f", todayConsumed));
            } else if (now.getHour() < 4) {  // 如果是凌晨0-4点
                // 检查是否是昨天的订单
                if (lastOrderTime != null && lastOrderTime.getHour() >= 0 && lastOrderTime.getHour() < 4) {
                    // 如果最近一次订货是在凌晨0-4点，且是昨天的订单，则计入昨天的消耗
                    if (lastDate.equals(today.minusDays(1))) {
                        todayConsumed = recentAvg;
                        log.info("当前时间: {}, 凌晨时段，且最近一次订货是昨天凌晨，计入昨天消耗: {}",
                                now.format(DateTimeFormatter.ofPattern("HH:mm")),
                                String.format("%.2f", todayConsumed));
                    }
                }
            } else {
                log.info("当前时间: {}, 未过中午，不计入今天消耗",
                        now.format(DateTimeFormatter.ofPattern("HH:mm")));
            }

            // 计算当前实际库存
            double consumed = daysSince * mu;
            double lastOrderQty = Double.parseDouble(lo.getNxDoQuantity());
            double onHand = lastOrderQty - consumed - todayConsumed;  // 减去今天的消耗

            // 预测明天的需求
            double tomorrowNeed = 0.7 * recentAvg + 0.3 * mu;  // 更重视近期趋势
            double totalNeed = onHand + tomorrowNeed;  // 当前库存 + 明天用量

            log.info("商品[{}]库存分析:", goods.getNxDgGoodsName());
            log.info("- 上次订货日期: {}, 距离今天: {}天", lastDate, daysSince);
            log.info("- 预计消耗: {}, 今天消耗: {}, 上次订货量: {}, 当前库存: {}",
                    String.format("%.2f", consumed),
                    String.format("%.2f", todayConsumed),
                    String.format("%.2f", lastOrderQty),
                    String.format("%.2f", onHand));
            log.info("- 明天预期用量: {}, 总需求(库存+明天): {}",
                    String.format("%.2f", tomorrowNeed),
                    String.format("%.2f", totalNeed));



            if (onHand < reorderPoint) {  // 当前库存低于再订货点时补货
                // 计算最佳订货量
                int orderQty = calculateOptimalOrderQuantity(orderHistory, onHand, reorderPoint, tomorrowNeed);

                if (orderQty > 0) {
                    // 设置所有计算结果到字段
                    setAiFields(departmentDisGoodsEntity, goods, onHand, mu, recentAvg, cv, safetyStock,
                            reorderPoint, lastDate, lastOrderQty, lo, daysSince, tomorrowNeed, orderQty);

                    log.info("商品[{}]需要补货: {}个", goods.getNxDgGoodsName(), orderQty);
                    log.info("- 补货原因: 当前库存({}) < 再订货点({})",
                            String.format("%.2f", onHand),
                            String.format("%.2f", reorderPoint));
                }
            } else {
                // 库存充足的情况，也设置这些字段
                setAiFields(departmentDisGoodsEntity, goods, onHand, mu, recentAvg, cv, safetyStock,
                        reorderPoint, lastDate, lastOrderQty, lo, daysSince, tomorrowNeed, 0); // 订货量为0

                log.info("商品[{}]库存充足: 当前库存({}) >= 再订货点({})",
                        goods.getNxDgGoodsName(),
                        String.format("%.2f", onHand),
                        String.format("%.2f", reorderPoint));
            }


        }else{
            departmentDisGoodsEntity.setNxTipText("没有查到数据");
        }

        return departmentDisGoodsEntity;
    }



    // 抽取设置字段的方法，避免代码重复
    private void setAiFields(NxDepartmentDisGoodsEntity entity,
                             NxDistributerGoodsEntity goods,
                             double onHand,
                             double mu,
                             double recentAvg,
                             double cv,
                             double safetyStock,
                             double reorderPoint,
                             LocalDate lastDate,
                             double lastOrderQty,
                             NxDepartmentOrderHistoryEntity lo,
                             long daysSince,
                             double tomorrowNeed,
                             int orderQty) {

//        recentAvg
        entity.setAiCurrentStock(String.format("%.2f", onHand));
        entity.setAiCurrentStockUnit(goods.getNxDgGoodsStandardname());
        entity.setAiDailyUsage(String.format("%.2f", mu));
        entity.setAiRecentAvgUsage(String.format("%.2f", recentAvg));
        entity.setAiUsageVariation(String.format("%.2f", cv));
        entity.setAiSafetyStock(String.format("%.2f", safetyStock));
        entity.setAiReorderPoint(String.format("%.2f", reorderPoint));
        entity.setAiLastOrderDate(lastDate.toString());
        entity.setAiLastOrderQuantity(String.format("%.2f", lastOrderQty));
        entity.setAiLastOrderUnit(lo.getNxDoStandard());
        entity.setAiDaysSinceLastOrder(String.valueOf(daysSince));
        entity.setAiTomorrowNeed(String.format("%.2f", tomorrowNeed));
        entity.setAiAvailableDays(String.format("%.1f", onHand / mu));
        entity.setAiOrderQuantity(String.valueOf(orderQty));
        entity.setAiOrderStandard(entity.getNxDdgOrderStandard());
        entity.setNxDistributerGoodsEntity(goods);
    }

}

