package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import com.nongxinle.service.GbDepartmentDisGoodsService;
import com.nongxinle.service.GbDepartmentOrdersService;
import com.nongxinle.service.GbDistributerGoodsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.nongxinle.dao.GbDepartmentOrdersHistoryDao;
import com.nongxinle.service.GbDepartmentOrdersHistoryService;



@Service("gbDepartmentOrdersHistoryService")
public class GbDepartmentOrdersHistoryServiceImpl implements GbDepartmentOrdersHistoryService {
	private static final Logger log = LoggerFactory.getLogger(GbDepartmentOrdersHistoryServiceImpl.class);

	@Autowired
	private GbDepartmentOrdersHistoryDao gbDepartmentOrdersHistoryDao;
	@Autowired
	private GbDepartmentOrdersService gbDepartmentOrdersService;
	@Autowired
	private GbDistributerGoodsService gbDistributerGoodsDao;
	@Autowired
	private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
	
	@Override
	public GbDepartmentOrdersHistoryEntity queryObject(Integer gbDepartmentOrdersHistoryId){
		return gbDepartmentOrdersHistoryDao.queryObject(gbDepartmentOrdersHistoryId);
	}
	
	@Override
	public List<GbDepartmentOrdersHistoryEntity> queryList(Map<String, Object> map){
		return gbDepartmentOrdersHistoryDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepartmentOrdersHistoryDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepartmentOrdersHistoryEntity gbDepartmentOrdersHistory){
		gbDepartmentOrdersHistoryDao.save(gbDepartmentOrdersHistory);
	}
	
	@Override
	public void update(GbDepartmentOrdersHistoryEntity gbDepartmentOrdersHistory){
		gbDepartmentOrdersHistoryDao.update(gbDepartmentOrdersHistory);
	}
	
	@Override
	public void delete(Integer gbDepartmentOrdersHistoryId){
		gbDepartmentOrdersHistoryDao.delete(gbDepartmentOrdersHistoryId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepartmentOrdersHistoryIds){
		gbDepartmentOrdersHistoryDao.deleteBatch(gbDepartmentOrdersHistoryIds);
	}

    @Override
    public List<GbDepartmentOrdersHistoryEntity> queryGbDepHistoryOrdersByParams(Map<String, Object> map1) {
        return gbDepartmentOrdersHistoryDao.queryGbDepHistoryOrdersByParams(map1);
    }

//    @Override
//    public List<GbDepartmentOrdersHistoryEntity> queryDepHistoryOrdersByParamsGb(Map<String, Object> map1) {
//
//		return gbDepartmentOrdersHistoryDao.queryDepHistoryOrdersByParamsGb(map1);
//    }

//	@Override
//	public List<GbDepartmentOrdersHistoryEntity> computeReorder(Integer depId) {
//		// 1. 参数、常量定义
//		int windowDays = 56;    // 统计过去多少天
//		int minTimes = 8;     // 至少订货次数阈值
//		int leadTime = 1;     // 预计提前期（天）
//		double serviceZ = 1.65;  // 95% 服务水平
//
//		// 2. 筛出「常订」商品
//		Map<String, Object> freqParams = new HashMap<>();
//		freqParams.put("depId", depId);
//		freqParams.put("windowDays", windowDays);
//		freqParams.put("minTimes", minTimes);
//		List<Integer> goodsIds = gbDepartmentOrdersHistoryDao.selectFrequentGoods(freqParams);
//		if (goodsIds == null || goodsIds.isEmpty()) {
//			return Collections.emptyList();
//		}
//
//		// 3. 拉取每日用量（DailyUsage）——返回 List<DailyUsage> { date, qty }
//		Map<Integer, List<DailyUsage>> dailyMap = new HashMap<>();
//		for (Integer gid : goodsIds) {
//			Map<String, Object> map = new HashMap<>();
//			map.put("depId", depId);
//			map.put("gid", gid);
//			map.put("windowDays", windowDays);
//			System.out.println("meyouyige " + map);
//			List<DailyUsage> usages = gbDepartmentOrdersHistoryDao.selectDailyUsage(map);
//			dailyMap.put(gid, usages != null ? usages : Collections.emptyList());
//		}
//
//		LocalDate today = LocalDate.now(ZoneId.systemDefault());
//		List<GbDepartmentOrdersHistoryEntity> result = new ArrayList<>();
//
//		// 4. 对每个商品，计算 μ、σ、ROP、安全库存，判断是否下单
//		for (Integer gid : goodsIds) {
//			List<DailyUsage> usages = dailyMap.get(gid);
//			if (usages.size() < 5) {
//				// 样本太少，跳过
//				continue;
//			}
//
//			// 4.1 计算日均 μ 和标准差 σ
//			double sum = usages.stream()
//					.mapToDouble(DailyUsage::getQty)
//					.sum();
//			double mu = sum / windowDays;
//			double variance = usages.stream()
//					.mapToDouble(u -> Math.pow(u.getQty() - mu, 2))
//					.sum() / windowDays;
//			double sigma = Math.sqrt(variance);
//
//			// 4.2 计算安全库存 & 再订货点
//			double safetyStock = serviceZ * sigma * Math.sqrt(leadTime);
//			double reorderPoint = mu * leadTime + safetyStock;
//
//			// 4.3 查询上次订货（LastOrder { goodsId, lastDate, lastQty }）
//			Map<String, Object> lastParams = new HashMap<>();
//			lastParams.put("depId", depId);
//			lastParams.put("gid", gid);
//			System.out.println("meyourkrkkrkrkrkrkkrkrkr" +lastParams);
//			GbDepartmentOrdersHistoryEntity lo = gbDepartmentOrdersHistoryDao.selectLastOrder(lastParams);
//			if (lo == null) {
//				continue;
//			}
//
//
////             解析上次订货日期
//			LocalDate lastDate;
//			try {
//				lastDate = LocalDate.parse(lo.getGbDohApplyDate());
//			} catch (Exception e) {
//				log.warn("日期解析失败 lo.getNxDohApplyDate()={}，跳过", lo.getGbDohApplyDate());
//				continue;
//			}
//			long daysSince = ChronoUnit.DAYS.between(lastDate, today);
//			if (daysSince < 0) {
//				daysSince = 0;
//			}
//
//			// 4.4 预测消耗 & 计算当前可用量
//			double consumed = daysSince * mu;
//
//			double onHand = Double.parseDouble(lo.getGbDohQuantity()) - consumed;
//
//			// 4.5 如果可用量 <= 安全库存，就要下单
//			System.out.println("goodsgetNxDgGoodsName" + nxDistributerGoodsDao.queryObject(gid).getGbDgGoodsName() +  "onHand===" + onHand + "safeStock===" + safetyStock);
//			if (onHand <= safetyStock) {
//				int orderQty = (int) Math.ceil(reorderPoint - onHand);
//				if (orderQty > 0) {
//					// 构造出一个「预测要下单」实体，放订货量到 quantity 字段
//					GbDepartmentOrdersHistoryEntity out = new GbDepartmentOrdersHistoryEntity();
//					out.setGbDohDepartmentId(depId);
//					out.setGbDohDisGoodsId(gid);
//					out.setGbDohQuantity(String.valueOf(orderQty));
//
//					// 填充商品详情
//					out.setGbDistributerGoodsEntity(
//							nxDistributerGoodsDao.queryObject(gid)
//					);
//					// 填充这次商品的历史明细（可选）
//					Map<String, Object> histParams = new HashMap<>();
//					histParams.put("depId", depId);
//					histParams.put("disGoodsId", gid);
//					List<GbDepartmentOrdersEntity> historyList =
//							gbDepartmentOrdersService.queryDisOrdersByParams(histParams);
//					out.setGbDepartmentOrdersEntities(historyList);
//					out.setGbDohStandard(lo.getGbDohStandard());
//
//					result.add(out);
//				}
//			}
//		}
//
//		return result;
//
//	}







//	@Override
//	public List<GbDepartmentOrdersHistoryEntity> computeReorder(Integer depId) {
//		// 1. 参数、常量定义
//		int windowDays = 56;    // 统计过去多少天
//		int minTimes = 8;     // 至少订货次数阈值
//		int leadTime = 1;     // 预计提前期（天）
//		double serviceZ = 1.65;  // 95% 服务水平
//
//		// 2. 筛出「常订」商品
//		Map<String, Object> freqParams = new HashMap<>();
//		freqParams.put("depId", depId);
//		freqParams.put("windowDays", windowDays);
//		freqParams.put("minTimes", minTimes);
//		List<Integer> goodsIds = gbDepartmentOrdersHistoryDao.selectFrequentGoods(freqParams);
//		if (goodsIds == null || goodsIds.isEmpty()) {
//			return Collections.emptyList();
//		}
//
//		// 3. 拉取每日用量（DailyUsage）——返回 List<DailyUsage> { date, qty }
//		Map<Integer, List<DailyUsage>> dailyMap = new HashMap<>();
//		for (Integer gid : goodsIds) {
//			Map<String, Object> map = new HashMap<>();
//			map.put("depId", depId);
//			map.put("gid", gid);
//			map.put("windowDays", windowDays);
//			System.out.println("meyouyige " + map);
//			List<DailyUsage> usages = gbDepartmentOrdersHistoryDao.selectDailyUsage(map);
//			dailyMap.put(gid, usages != null ? usages : Collections.emptyList());
//		}
//
//		LocalDate today = LocalDate.now(ZoneId.systemDefault());
//		List<GbDepartmentOrdersHistoryEntity> result = new ArrayList<>();
//
//		// 4. 对每个商品，计算 μ、σ、ROP、安全库存，判断是否下单
//		for (Integer gid : goodsIds) {
//			List<DailyUsage> usages = dailyMap.get(gid);
//			if (usages.size() < 5) {
//				// 样本太少，跳过
//				continue;
//			}
//
//			// 4.1 计算日均 μ 和标准差 σ
//			double sum = usages.stream()
//					.mapToDouble(DailyUsage::getQty)
//					.sum();
//			double mu = sum / windowDays;
//			double variance = usages.stream()
//					.mapToDouble(u -> Math.pow(u.getQty() - mu, 2))
//					.sum() / windowDays;
//			double sigma = Math.sqrt(variance);
//
//			// 4.2 计算安全库存 & 再订货点
//			double safetyStock = serviceZ * sigma * Math.sqrt(leadTime);
//			double reorderPoint = mu * leadTime + safetyStock;
//
//			// 4.3 查询上次订货（LastOrder { goodsId, lastDate, lastQty }）
//			Map<String, Object> lastParams = new HashMap<>();
//			lastParams.put("depId", depId);
//			lastParams.put("gid", gid);
//			System.out.println("meyourkrkkrkrkrkrkkrkrkr" +lastParams);
//			GbDepartmentOrdersHistoryEntity lo = gbDepartmentOrdersHistoryDao.selectLastOrder(lastParams);
//			if (lo == null) {
//				continue;
//			}
//
//
////             解析上次订货日期
//			LocalDate lastDate;
//			try {
//				lastDate = LocalDate.parse(lo.getGbDohApplyDate());
//			} catch (Exception e) {
//				log.warn("日期解析失败 lo.getNxDohApplyDate()={}，跳过", lo.getGbDohApplyDate());
//				continue;
//			}
//			long daysSince = ChronoUnit.DAYS.between(lastDate, today);
//			if (daysSince < 0) {
//				daysSince = 0;
//			}
//
//			// 4.4 预测消耗 & 计算当前可用量
//			double consumed = daysSince * mu;
//
//			double onHand = Double.parseDouble(lo.getGbDohQuantity()) - consumed;
//
//			// 4.5 如果可用量 <= 安全库存，就要下单
//			System.out.println("goodsgetNxDgGoodsName" + nxDistributerGoodsDao.queryObject(gid).getGbDgGoodsName() +  "onHand===" + onHand + "safeStock===" + safetyStock);
//			if (onHand <= safetyStock) {
//				int orderQty = (int) Math.ceil(reorderPoint - onHand);
//				if (orderQty > 0) {
//					// 构造出一个「预测要下单」实体，放订货量到 quantity 字段
//					GbDepartmentOrdersHistoryEntity out = new GbDepartmentOrdersHistoryEntity();
//					out.setGbDohDepartmentId(depId);
//					out.setGbDohDisGoodsId(gid);
//					out.setGbDohQuantity(String.valueOf(orderQty));
//
//					// 填充商品详情
//					out.setGbDistributerGoodsEntity(
//							nxDistributerGoodsDao.queryObject(gid)
//					);
//					// 填充这次商品的历史明细（可选）
//					Map<String, Object> histParams = new HashMap<>();
//					histParams.put("depId", depId);
//					histParams.put("disGoodsId", gid);
//					List<GbDepartmentOrdersEntity> historyList =
//							gbDepartmentOrdersService.queryDisOrdersByParams(histParams);
//					out.setGbDepartmentOrdersEntities(historyList);
//					out.setGbDohStandard(lo.getGbDohStandard());
//
//					result.add(out);
//				}
//			}
//		}
//
//		return result;
//
//	}

//	@Override
//	public GbDepartmentOrdersHistoryEntity selectLastOrder(Map<String, Object> params) {
//		return gbDepartmentOrdersHistoryDao.selectLastOrder(params);
//	}

}
