package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.NxDepartmentOrderHistoryService;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.NxDistributerStandardService;
import com.nongxinle.utils.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.nongxinle.dao.NxDepartmentDisGoodsDao;
import com.nongxinle.service.NxDepartmentDisGoodsService;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@Service("nxDepartmentDisGoodsService")
public class NxDepartmentDisGoodsServiceImpl implements NxDepartmentDisGoodsService {
	@Autowired
	private NxDepartmentDisGoodsDao nxDepartmentDisGoodsDao;
	@Autowired
	NxDepartmentOrdersDao nxDepartmentOrdersDao;

	@Autowired
	private NxDepartmentOrderHistoryService historyService;
	@Autowired
	private NxDistributerGoodsService nxDistributerGoodsService;
	@Autowired
	private NxDistributerStandardService nxDistributerStandardService;
	private static final Logger log = LoggerFactory.getLogger(NxDepartmentDisGoodsServiceImpl.class);



	@Override
	public List<NxDepartmentEntity> queryDepartmentsByDisGoodsId(Integer disGoodsId) {
		return nxDepartmentDisGoodsDao.queryDepartmentsByDisGoodsId(disGoodsId);
	}

	@Override
	public List<NxDistributerFatherGoodsEntity> depGetDepDisGoodsCata(Map<String, Object> map) {
		return nxDepartmentDisGoodsDao.depGetDepDisGoodsCata(map);
	}

	@Override
	public List<NxDepartmentDisGoodsEntity> queryDepGoodsByFatherId(Map<String, Object> map) {
		return nxDepartmentDisGoodsDao.queryDepGoodsByFatherId(map);
	}
	@Override
	public List<NxDepartmentDisGoodsEntity> queryAddDisDepGoods(Map<String, Object> map) {
		return nxDepartmentDisGoodsDao.queryDisDepGoods(map);
	}

    @Override
    public int queryDepGoodsTotal(Map<String, Object> map3) {
		return nxDepartmentDisGoodsDao.queryDisGoodsTotal(map3);
    }

	@Override
	public void save(NxDepartmentDisGoodsEntity nxDepartmentDisGoods){
		nxDepartmentDisGoodsDao.save(nxDepartmentDisGoods);
	}
	@Override
	public void update(NxDepartmentDisGoodsEntity nxDepartmentDisGoods){
		nxDepartmentDisGoodsDao.update(nxDepartmentDisGoods);
	}
	@Override
	public NxDepartmentDisGoodsEntity queryObject(Integer nxDepartmentDisGoodsId){
		return nxDepartmentDisGoodsDao.queryObject(nxDepartmentDisGoodsId);
	}

	@Override
	public List<NxDepartmentDisGoodsEntity> queryDepDisSearchPinyin(Map<String, Object> map) {
		return nxDepartmentDisGoodsDao.queryDepDisSearchPinyin(map);
	}



	@Override
	public TreeSet<NxDepartmentDisGoodsEntity> queryDepDisGoodsQuickSearchStr(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.queryDepDisGoodsQuickSearchStr (map);
	}

	@Override
	public void delete(Integer nxDepartmentDisGoodsId){
		nxDepartmentDisGoodsDao.delete(nxDepartmentDisGoodsId);
	}

	@Override
	public List<NxDepartmentDisGoodsEntity> queryDepDisGoodsByParams(Map<String, Object> map) {
		return nxDepartmentDisGoodsDao.queryDepDisGoodsByParams(map);
	}

    @Override
    public List<NxDistributerFatherGoodsEntity> disGetDepDisGoodsCata(Integer depFatherId) {

		return nxDepartmentDisGoodsDao.disGetDepGoodsCata(depFatherId);
    }

	@Override
	public void deleteBatch(Integer[] nxDepartmentIds){
		nxDepartmentDisGoodsDao.deleteBatch(nxDepartmentIds);
	}

    @Override
    public List<NxDistributerFatherGoodsEntity> depQueryDepGoodsWithOrder(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.depQueryDepGoodsWithOrder(map);
    }

    @Override
    public NxDepartmentEntity depFatherGetSubDepsGoods(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.depFatherGetSubDepsGoods(map);
    }

    @Override
    public List<NxDepartmentDisGoodsEntity> depGetDepsGoods(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.depGetDepsGoods(map);
    }

    @Override
    public List<GbDepartmentEntity> queryGbDepartmentsByDisGoodsId(Integer disGoodsId) {

		return nxDepartmentDisGoodsDao.queryGbDepartmentsByDisGoodsId(disGoodsId);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryDepDisGoodsWithOrders(Map<String, Object> mapDep) {

		return nxDepartmentDisGoodsDao.queryDepDisGoodsWithOrders(mapDep);
    }

    @Override
    public List<NxDepartmentDisGoodsEntity> queryDepartmentGoods(Map<String, Object> mapDep) {

		return nxDepartmentDisGoodsDao.queryDepartmentGoods(mapDep);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryGbDisGbDepGoods(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.queryGbDisGbDepGoods(map);
    }

    @Override
    public List<NxDepartmentDisGoodsEntity> queryDepDisGoodsOrders(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.queryDepDisGoodsOrders(map);
    }

    @Override
    public int queryDepGoodsCount(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.queryDepGoodsCount(map);
    }

    @Override
    public List<NxDepartmentDisGoodsEntity> queryWenti() {

		return nxDepartmentDisGoodsDao.queryWenti();
    }

    @Override
    public List<Integer> queryOnlyDepGoodsIds(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.queryOnlyDepGoodsIds(map);
    }

    @Override
    public List<NxDepartmentDisGoodsEntity> queryDepDisGoodsOrdersForAi(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.queryDepDisGoodsOrdersForAi(map);
    }

    @Override
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
		if (goods != null) {
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
				departmentDisGoodsEntity.setNxTipText(goods.getNxDgQuantityDays() + "日内没有订货");
				return departmentDisGoodsEntity;

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

			if (usages == null || usages.size() < 3) {
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


		} else {
			departmentDisGoodsEntity.setNxTipText("没有查到数据");
		}

		return departmentDisGoodsEntity;
	}

	@Override
	public PageUtils computeReorder(Integer depId, Integer page, Integer limit) {
		log.info("收到前端请求参数: depId={}, page={}, limit={}", depId, page, limit);
		long totalStartTime = System.currentTimeMillis();
		log.info("开始计算部门[{}]的补货建议，页码：{}，每页数量：{}", depId, page, limit);

		// 1. 获取正在下单的商品
		long startTime = System.currentTimeMillis();
		Map<String, Object> mapOrder = new HashMap<>();
		mapOrder.put("depId", depId);
		mapOrder.put("purStatus", 4);
		List<Integer> orderingGoodsIds = nxDepartmentOrdersDao.queryGoodsIds(mapOrder);
		log.info("获取正在下单商品耗时: {}ms", System.currentTimeMillis() - startTime);

		// 2. 参数、常量定义
		int windowDays = 56;       // 统计过去多少天
		int minTimes = 4;          // 至少订货次数阈值
		int leadTime = 1;          // 预计提前期（天）
		double serviceZ = 1.65;    // 95% 服务水平，对应正态分布 Z 值
		log.info("计算参数设置: windowDays={}, minTimes={}, leadTime={}, serviceZ={}",
				windowDays, minTimes, leadTime, serviceZ);

		// 3. 获取常订商品总数
		Map<String, Object> freqParams = new HashMap<>();
		freqParams.put("depId", depId);
		freqParams.put("windowDays", windowDays);
		freqParams.put("minTimes", minTimes);
		int total = historyService.selectFrequentGoodsCount(freqParams);
		log.info("常订商品总数: {}", total);

		// 5. 分页查询，直到获取足够的数据（首屏补齐，其余正常分页）
		List<NxDepartmentDisGoodsEntity> finalResult = new ArrayList<>();
		if (page == 1) {
			int currentOffset = 0;
			int currentPage = 1;
			while (finalResult.size() < limit) {
				freqParams.put("offset", currentOffset);
				freqParams.put("limit", limit);
				log.info("[首屏补齐] 查询参数: depId={}, windowDays={}, minTimes={}, offset={}, limit={}, currentPage={}",
						depId, windowDays, minTimes, currentOffset, limit, currentPage);
				List<Integer> goodsIds = historyService.selectFrequentGoodsWithPage(freqParams);
				if (goodsIds == null || goodsIds.isEmpty()) {
					log.info("[首屏补齐] 没有更多数据，当前offset: {}, currentPage: {}", currentOffset, currentPage);
					break;
				}
				List<NxDepartmentDisGoodsEntity> pageResult = processGoodsList(goodsIds, orderingGoodsIds, depId, windowDays, leadTime, serviceZ);
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
			List<Integer> goodsIds = historyService.selectFrequentGoodsWithPage(freqParams);
			if (goodsIds != null && !goodsIds.isEmpty()) {
				finalResult.addAll(processGoodsList(goodsIds, orderingGoodsIds, depId, windowDays, leadTime, serviceZ));
			}
			log.info("[普通分页] 计算补货建议总耗时: {}ms", System.currentTimeMillis() - totalStartTime);
			log.info("[普通分页] 最终返回商品数量: {}, 期望limit: {}, 当前页page: {}", finalResult.size(), limit, page);
			return new PageUtils(finalResult, total, limit, page);
		}
	}

    @Override
    public NxDepartmentDisGoodsEntity queryDepartmentGoodsOnly(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.queryDepartmentGoodsOnly(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStrWithDepOrders(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.queryDisGoodsQuickSearchStrWithDepOrders(map);
    }

	@Override
	public List<NxDepartmentDisGoodsEntity> queryByDisGoodsIdsAndDep(Map<String, Object> map) {
		return nxDepartmentDisGoodsDao.queryByDisGoodsIdsAndDep(map);
	}

    private static final double EPSILON = 1e-6;             // 避免除零
	private static final int MIN_USAGE_RECORDS = 4;          // 至少需要 4 条用量数据
	private static final int RECENT_DAYS_FOR_CV = 7;         // 用近 7 天计算波动
	private static final int ARRIVAL_HOUR = 7;               // 次日到货时间(07:00)

	// 依赖注入省略...




	public List<NxDepartmentDisGoodsEntity> processGoodsList(List<Integer> goodsIds,
															 List<Integer> orderingGoodsIds,
															 Integer depId,
															 int windowDays,
															 int leadTime,
															 double serviceZ) {
		List<NxDepartmentDisGoodsEntity> result = new ArrayList<NxDepartmentDisGoodsEntity>();

		for (Integer gid : goodsIds) {
			/* ---------- 1. 过滤正在下单 ---------- */
			if (orderingGoodsIds != null && orderingGoodsIds.contains(gid)) {
				log.info("SKU[{}] 正在下单，跳过", gid);
				continue;
			}

			/* ---------- 2. 基础信息 ---------- */
			NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(gid);
			if (goods == null) {
				log.warn("SKU[{}] 商品信息不存在", gid);
				continue;
			}
			NxDepartmentDisGoodsEntity departmentDisGoodsEntity = queryDepartmentGoods(depId, gid, goods);
			if (departmentDisGoodsEntity == null) {
				continue;
			}

			/* ---------- 3. 历史订单 ---------- */
			List<NxDepartmentOrderHistoryEntity> orderHistory = queryHistory(depId, gid);
			NxDepartmentOrderHistoryEntity lastOrder = queryLastOrder(depId, gid, goods);
			if (lastOrder == null) {
				log.info("SKU[{}] 在保质期内没有订货记录", gid);
				continue;
			}

			/* ---------- 4. 日用量 ---------- */
			Map<String, Object> usageParams = new HashMap<String, Object>();
			usageParams.put("depId", depId);
			usageParams.put("gid", gid);
			usageParams.put("windowDays", windowDays);
			List<DailyUsage> usages = historyService.selectDailyUsage(usageParams);
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
			LocalDate lastDate = LocalDate.parse(lastOrder.getNxDoApplyDate());
			LocalDate today = LocalDate.now(ZoneId.systemDefault());
			long daysSince = Math.max(0, ChronoUnit.DAYS.between(lastDate, today));

			double remainingToday = estimateRemainingDemand(LocalDateTime.now(), recentMu);
			double lastQty = parseDoubleSafe(lastOrder.getNxDoQuantity());
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

			departmentDisGoodsEntity.setNxDistributerGoodsEntity(goods);

			result.add(departmentDisGoodsEntity);
		}
		return result;
	}

	/* =================== 辅助方法 =================== */

	private NxDepartmentDisGoodsEntity queryDepartmentGoods(Integer depId, Integer gid, NxDistributerGoodsEntity goods) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("disGoodsId", gid);
		map.put("depId", depId);
//		NxDepartmentDisGoodsEntity dg = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
		NxDepartmentDisGoodsEntity dg = nxDepartmentDisGoodsDao.queryDepartmentGoodsOnly(map);
		if (dg == null) log.warn("SKU[{}] 部门商品信息不存在", gid);
		return dg;
	}

	private List<NxDepartmentOrderHistoryEntity> queryHistory(Integer depId, Integer gid) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("depId", depId);
		params.put("disGoodsId", gid);
		params.put("limit", 20);
		return historyService.queryDisHistoryOrdersByParamsForAi(params);
	}

	private NxDepartmentOrderHistoryEntity queryLastOrder(Integer depId, Integer gid, NxDistributerGoodsEntity goods) {
		Map<String, Object> p = new HashMap<String, Object>();
		p.put("depId", depId);
		p.put("gid", gid);
		if (goods.getNxDgQuantityDays() != null) p.put("dayuDate", formatWhatDay(-goods.getNxDgQuantityDays()));
		return historyService.selectLastOrder(p);
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

		List<NxDistributerStandardEntity> nxDistributerStandardEntities = nxDistributerStandardService.queryDisStandardByDisGoodsId(goods.getNxDistributerGoodsId());
		goods.setNxDistributerStandardEntities(nxDistributerStandardEntities);
//		entity.setNxDistributerGoodsEntity(goods);
	}



}
