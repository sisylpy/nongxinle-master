package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 10-14 12:03
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDepartmentEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDistributerGoodsShelfStockEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfStockReduceEntity;
import com.nongxinle.service.NxDistributerGoodsShelfStockReduceService;
import com.nongxinle.service.NxDistributerGoodsShelfStockService;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;


@RestController
@RequestMapping("api/nxdistributergoodsshelfstockreduce")
public class NxDistributerGoodsShelfStockReduceController {
	@Autowired
	private NxDistributerGoodsShelfStockReduceService nxDistributerGoodsShelfStockReduceService;
	@Autowired
	private NxDistributerGoodsShelfStockService nxDistributerGoodsShelfStockService;
	@Autowired
	private NxDistributerGoodsService nxDistributerGoodsService;

    private BigDecimal resolvePriceForStock(NxDistributerGoodsShelfStockEntity stockEntity, BigDecimal fallbackWeight) {
        String priceStr = stockEntity.getNxDgssPrice();
        if (priceStr != null && !priceStr.trim().isEmpty()) {
            return new BigDecimal(priceStr);
        }

        BigDecimal weight = fallbackWeight;
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0) {
            String restWeightStr = stockEntity.getNxDgssRestWeight();
            if (restWeightStr != null && !restWeightStr.trim().isEmpty()) {
                weight = new BigDecimal(restWeightStr);
            }
        }

        String subtotalStr = stockEntity.getNxDgssRestSubtotal();
        if ((subtotalStr == null || subtotalStr.trim().isEmpty()) && stockEntity.getNxDgssSubtotal() != null && !stockEntity.getNxDgssSubtotal().trim().isEmpty()) {
            subtotalStr = stockEntity.getNxDgssSubtotal();
        }

        if (subtotalStr != null && !subtotalStr.trim().isEmpty() && weight != null && weight.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal price = new BigDecimal(subtotalStr).divide(weight, 4, BigDecimal.ROUND_HALF_UP);
            stockEntity.setNxDgssPrice(price.stripTrailingZeros().toPlainString());
            stockEntity.setNxDgssSellingPrice(price.stripTrailingZeros().toPlainString());
            return price;
        }

        stockEntity.setNxDgssPrice("0");
        if (stockEntity.getNxDgssSellingPrice() == null || stockEntity.getNxDgssSellingPrice().trim().isEmpty()) {
            stockEntity.setNxDgssSellingPrice("0");
        }
        return BigDecimal.ZERO;
    }


	/**
	 * 损耗
	 * @param disId 批发商ID
	 * @param disGoodsId 商品ID
	 * @param weight 损耗重量
	 * @param userId 操作用户ID
	 * @return
	 */
	@RequestMapping(value = "/addUse", method = RequestMethod.POST)
	@ResponseBody
	public R addUse(Integer disId, Integer disGoodsId, String weight, Integer userId) {
		System.out.println("======== 添加损耗记录开始 ========");
		System.out.println("批发商ID: " + disId + ", 商品ID: " + disGoodsId + ", 损耗重量: " + weight + ", 用户ID: " + userId);

		try {
			// 查询库存批次（FIFO排序）
			Map<String, Object> map = new HashMap<>();
			map.put("disGoodsId", disGoodsId);
			map.put("restWeight", 0);
			List<NxDistributerGoodsShelfStockEntity> stockEntities = nxDistributerGoodsShelfStockService.queryShelfStockListByParams(map);

			if (stockEntities.size() == 0) {
				System.out.println("⚠️ 没有找到库存批次");
				return R.error("没有库存可以损耗");
			}

			BigDecimal needWeight = new BigDecimal(weight);
			BigDecimal totalCost = BigDecimal.ZERO;
			BigDecimal remainingWeight = needWeight;

			System.out.println("开始FIFO扣减库存 - 损耗重量: " + needWeight);

			for (NxDistributerGoodsShelfStockEntity stockEntity : stockEntities) {
				if (remainingWeight.compareTo(BigDecimal.ZERO) <= 0) {
					break;
				}

				BigDecimal stockRestWeight = new BigDecimal(stockEntity.getNxDgssRestWeight());
				String priceStr = stockEntity.getNxDgssPrice();
				if (priceStr == null || priceStr.trim().isEmpty()) {
					priceStr = resolvePriceForStock(stockEntity, stockRestWeight).stripTrailingZeros().toPlainString();
				}
				BigDecimal stockPrice = new BigDecimal(priceStr);

				if (stockRestWeight.compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}

				BigDecimal deductWeight;
				if (remainingWeight.compareTo(stockRestWeight) <= 0) {
					deductWeight = remainingWeight;
				} else {
					deductWeight = stockRestWeight;
				}

				BigDecimal batchCost = deductWeight.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
				totalCost = totalCost.add(batchCost);

				// 更新库存
				BigDecimal newRestWeight = stockRestWeight.subtract(deductWeight);
				stockEntity.setNxDgssRestWeight(newRestWeight.toString());
				BigDecimal newRestSubtotal = newRestWeight.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
				stockEntity.setNxDgssRestSubtotal(newRestSubtotal.toString());
				nxDistributerGoodsShelfStockService.update(stockEntity);

				// 创建损耗记录
				NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
				reduceEntity.setNxDgssrNxDistributerId(disId);
				reduceEntity.setNxDgssrNxDisGoodsId(disGoodsId);
				reduceEntity.setNxDgssrNxDisGoodsFatherId(stockEntity.getNxDgssNxDisGoodsFatherId());
				reduceEntity.setNxDgssrNxStockId(stockEntity.getNxDistributerGoodsShelfStockId());
				reduceEntity.setNxDgssrDate(formatWhatDay(0));
				reduceEntity.setNxDgssrWeek(getWeek(0));
				reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
				reduceEntity.setNxDgssrFullTime(formatWhatYearDayTime(0));
				reduceEntity.setNxDgssrType(1); // 1=使用
				reduceEntity.setNxDgssrDoUserId(userId);
				reduceEntity.setNxDgssrCostWeight(deductWeight.toString());
				reduceEntity.setNxDgssrCostSubtotal(batchCost.toString());
				reduceEntity.setNxDgssrLossWeight(deductWeight.toString());
				reduceEntity.setNxDgssrLossSubtotal(batchCost.toString());
				reduceEntity.setNxDgssrNxPurGoodsId(stockEntity.getNxDgssNxPurGoodsId());
				reduceEntity.setNxDgssrStatus(0);
				nxDistributerGoodsShelfStockReduceService.save(reduceEntity);

				System.out.println("扣减批次 " + stockEntity.getNxDistributerGoodsShelfStockId() +
						" - 损耗重量: " + deductWeight + ", 损耗成本: " + batchCost);

				remainingWeight = remainingWeight.subtract(deductWeight);
			}

			if (remainingWeight.compareTo(BigDecimal.ZERO) > 0) {
				System.out.println("⚠️ 库存不足，还需要: " + remainingWeight);
				return R.error("库存不足，还差" + remainingWeight + "无法损耗");
			}

			Map<String, Object> result = new HashMap<>();
			result.put("totalWeight", weight);
			result.put("totalCost", totalCost.toString());

			System.out.println("======== 添加损耗记录完成 ========");
			return R.ok().put("data", result);

		} catch (Exception e) {
			e.printStackTrace();
			return R.error("添加损耗记录失败：" + e.getMessage());
		}
	}

	/**
	 * 损耗
	 * @param disId 批发商ID
	 * @param disGoodsId 商品ID
	 * @param weight 损耗重量
	 * @param userId 操作用户ID
	 * @return
	 */
	@RequestMapping(value = "/addLoss", method = RequestMethod.POST)
	@ResponseBody
	public R addLoss(Integer disId, Integer disGoodsId, String weight, Integer userId) {
		System.out.println("======== 添加损耗记录开始 ========");
		System.out.println("批发商ID: " + disId + ", 商品ID: " + disGoodsId + ", 损耗重量: " + weight + ", 用户ID: " + userId);

		try {
			// 查询库存批次（FIFO排序）
			Map<String, Object> map = new HashMap<>();
			map.put("disGoodsId", disGoodsId);
			map.put("restWeight", 0);
			List<NxDistributerGoodsShelfStockEntity> stockEntities = nxDistributerGoodsShelfStockService.queryShelfStockListByParams(map);
			
			if (stockEntities.size() == 0) {
				System.out.println("⚠️ 没有找到库存批次");
				return R.error("没有库存可以损耗");
			}

			BigDecimal needWeight = new BigDecimal(weight);
			BigDecimal totalCost = BigDecimal.ZERO;
			BigDecimal remainingWeight = needWeight;
			
			System.out.println("开始FIFO扣减库存 - 损耗重量: " + needWeight);

			for (NxDistributerGoodsShelfStockEntity stockEntity : stockEntities) {
				if (remainingWeight.compareTo(BigDecimal.ZERO) <= 0) {
					break;
				}

				BigDecimal stockRestWeight = new BigDecimal(stockEntity.getNxDgssRestWeight());
				String priceStr = stockEntity.getNxDgssPrice();
				if (priceStr == null || priceStr.trim().isEmpty()) {
					priceStr = resolvePriceForStock(stockEntity, stockRestWeight).stripTrailingZeros().toPlainString();
				}
				BigDecimal stockPrice = new BigDecimal(priceStr);

				if (stockRestWeight.compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}

				BigDecimal deductWeight;
				if (remainingWeight.compareTo(stockRestWeight) <= 0) {
					deductWeight = remainingWeight;
				} else {
					deductWeight = stockRestWeight;
				}

				BigDecimal batchCost = deductWeight.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
				totalCost = totalCost.add(batchCost);

				// 更新库存
				BigDecimal newRestWeight = stockRestWeight.subtract(deductWeight);
				stockEntity.setNxDgssRestWeight(newRestWeight.toString());
				BigDecimal newRestSubtotal = newRestWeight.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
				stockEntity.setNxDgssRestSubtotal(newRestSubtotal.toString());
				nxDistributerGoodsShelfStockService.update(stockEntity);

				// 创建损耗记录
				NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
				reduceEntity.setNxDgssrNxDistributerId(disId);
				reduceEntity.setNxDgssrNxDisGoodsId(disGoodsId);
				reduceEntity.setNxDgssrNxDisGoodsFatherId(stockEntity.getNxDgssNxDisGoodsFatherId());
				reduceEntity.setNxDgssrNxStockId(stockEntity.getNxDistributerGoodsShelfStockId());
				reduceEntity.setNxDgssrDate(formatWhatDay(0));
				reduceEntity.setNxDgssrWeek(getWeek(0));
				reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
				reduceEntity.setNxDgssrFullTime(formatWhatYearDayTime(0));
				reduceEntity.setNxDgssrType(3); // 3=损耗
				reduceEntity.setNxDgssrDoUserId(userId);
				reduceEntity.setNxDgssrCostWeight(deductWeight.toString());
				reduceEntity.setNxDgssrCostSubtotal(batchCost.toString());
				reduceEntity.setNxDgssrLossWeight(deductWeight.toString());
				reduceEntity.setNxDgssrLossSubtotal(batchCost.toString());
				reduceEntity.setNxDgssrNxPurGoodsId(stockEntity.getNxDgssNxPurGoodsId());
				reduceEntity.setNxDgssrStatus(0);
				nxDistributerGoodsShelfStockReduceService.save(reduceEntity);

				System.out.println("扣减批次 " + stockEntity.getNxDistributerGoodsShelfStockId() + 
								 " - 损耗重量: " + deductWeight + ", 损耗成本: " + batchCost);

				remainingWeight = remainingWeight.subtract(deductWeight);
			}

			if (remainingWeight.compareTo(BigDecimal.ZERO) > 0) {
				System.out.println("⚠️ 库存不足，还需要: " + remainingWeight);
				return R.error("库存不足，还差" + remainingWeight + "无法损耗");
			}

			Map<String, Object> result = new HashMap<>();
			result.put("totalWeight", weight);
			result.put("totalCost", totalCost.toString());
			
			System.out.println("======== 添加损耗记录完成 ========");
			return R.ok().put("data", result);

		} catch (Exception e) {
			e.printStackTrace();
			return R.error("添加损耗记录失败：" + e.getMessage());
		}
	}

	/**
	 * 退货
	 * @param disId 批发商ID
	 * @param disGoodsId 商品ID
	 * @param weight 退货重量
	 * @param userId 操作用户ID
	 * @return
	 */
	@RequestMapping(value = "/addReturn", method = RequestMethod.POST)
	@ResponseBody
	public R addReturn(Integer disId, Integer disGoodsId, String weight, Integer userId) {
		System.out.println("======== 添加退货记录开始 ========");
		System.out.println("批发商ID: " + disId + ", 商品ID: " + disGoodsId + ", 退货重量: " + weight + ", 用户ID: " + userId);

		try {
			// 查询库存批次（FIFO排序）
			Map<String, Object> map = new HashMap<>();
			map.put("disGoodsId", disGoodsId);
			map.put("restWeight", 0);
			List<NxDistributerGoodsShelfStockEntity> stockEntities = nxDistributerGoodsShelfStockService.queryShelfStockListByParams(map);
			
			if (stockEntities.size() == 0) {
				System.out.println("⚠️ 没有找到库存批次");
				return R.error("没有库存可以退货");
			}

			BigDecimal needWeight = new BigDecimal(weight);
			BigDecimal totalCost = BigDecimal.ZERO;
			BigDecimal remainingWeight = needWeight;
			
			System.out.println("开始FIFO扣减库存 - 退货重量: " + needWeight);

			for (NxDistributerGoodsShelfStockEntity stockEntity : stockEntities) {
				if (remainingWeight.compareTo(BigDecimal.ZERO) <= 0) {
					break;
				}

				BigDecimal stockRestWeight = new BigDecimal(stockEntity.getNxDgssRestWeight());
				String priceStr = stockEntity.getNxDgssPrice();
				if (priceStr == null || priceStr.trim().isEmpty()) {
					priceStr = "0";
					stockEntity.setNxDgssPrice("0");
					if (stockEntity.getNxDgssSellingPrice() == null || stockEntity.getNxDgssSellingPrice().trim().isEmpty()) {
						stockEntity.setNxDgssSellingPrice("0");
					}
				}
				BigDecimal stockPrice = new BigDecimal(priceStr);

				if (stockRestWeight.compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}

				BigDecimal deductWeight;
				if (remainingWeight.compareTo(stockRestWeight) <= 0) {
					deductWeight = remainingWeight;
				} else {
					deductWeight = stockRestWeight;
				}

				BigDecimal batchCost = deductWeight.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
				totalCost = totalCost.add(batchCost);

				// 更新库存
				BigDecimal newRestWeight = stockRestWeight.subtract(deductWeight);
				stockEntity.setNxDgssRestWeight(newRestWeight.toString());
				BigDecimal newRestSubtotal = newRestWeight.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
				stockEntity.setNxDgssRestSubtotal(newRestSubtotal.toString());
				nxDistributerGoodsShelfStockService.update(stockEntity);

				// 创建退货记录
				NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
				reduceEntity.setNxDgssrNxDistributerId(disId);
				reduceEntity.setNxDgssrNxDisGoodsId(disGoodsId);
				reduceEntity.setNxDgssrNxDisGoodsFatherId(stockEntity.getNxDgssNxDisGoodsFatherId());
				reduceEntity.setNxDgssrNxStockId(stockEntity.getNxDistributerGoodsShelfStockId());
				reduceEntity.setNxDgssrDate(formatWhatDay(0));
				reduceEntity.setNxDgssrWeek(getWeek(0));
				reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
				reduceEntity.setNxDgssrFullTime(formatWhatYearDayTime(0));
				reduceEntity.setNxDgssrType(4); // 4=退货
				reduceEntity.setNxDgssrDoUserId(userId);
				reduceEntity.setNxDgssrCostWeight(deductWeight.toString());
				reduceEntity.setNxDgssrCostSubtotal(batchCost.toString());
				reduceEntity.setNxDgssrReturnWeight(deductWeight.toString());
				reduceEntity.setNxDgssrReturnSubtotal(batchCost.toString());
				reduceEntity.setNxDgssrNxPurGoodsId(stockEntity.getNxDgssNxPurGoodsId());
				reduceEntity.setNxDgssrStatus(0);
				nxDistributerGoodsShelfStockReduceService.save(reduceEntity);

				System.out.println("扣减批次 " + stockEntity.getNxDistributerGoodsShelfStockId() + 
								 " - 退货重量: " + deductWeight + ", 退货成本: " + batchCost);

				remainingWeight = remainingWeight.subtract(deductWeight);
			}

			if (remainingWeight.compareTo(BigDecimal.ZERO) > 0) {
				System.out.println("⚠️ 库存不足，还需要: " + remainingWeight);
				return R.error("库存不足，还差" + remainingWeight + "无法退货");
			}

			Map<String, Object> result = new HashMap<>();
			result.put("totalWeight", weight);
			result.put("totalCost", totalCost.toString());
			
			System.out.println("======== 添加退货记录完成 ========");
			return R.ok().put("data", result);

		} catch (Exception e) {
			e.printStackTrace();
			return R.error("添加退货记录失败：" + e.getMessage());
		}
	}

	/**
	 * 保存/更新盘库记录（盘库损失）
	 * @param stockId 批次ID（可选，为空时需提供disGoodsId和disId）
	 * @param waste 盘库损耗数量（必填）
	 * @param inventoryType 盘库类型：1=日盘库，2=周盘库，3=月盘库（必填）
	 * @param inventoryDate 日盘库：日期，格式 "2025-01-15"
	 * @param inventoryWeek 周盘库：周数，格式 "3"（当前年第几周）
	 * @param inventoryMonth 月盘库：月份，格式 "01"
	 * @param operatorId 操作员ID（必填）
	 * @param disGoodsId 配送商品ID（当stockId为空时必填）
	 * @param disId 配送商ID（当stockId为空时必填）
	 * @return
	 */
	@RequestMapping(value = "/saveInventoryRecord", method = RequestMethod.POST)
	@ResponseBody
	public R saveInventoryRecord(Integer stockId, String waste, String weight, Integer inventoryType,
	                             String inventoryDate, String inventoryWeek, String inventoryMonth,
	                             Integer operatorId, Integer disGoodsId, Integer disId, Integer userId) {
		// 兼容 weight 参数名（前端可能传 weight）
		if ((waste == null || waste.trim().isEmpty()) && weight != null && !weight.trim().isEmpty()) {
			waste = weight;
		}
		// 兼容 userId 参数名（前端可能传 userId）
		if (operatorId == null && userId != null) {
			operatorId = userId;
		}
		
		System.out.println("======== 保存盘库记录开始 ========");
		System.out.println("批次ID: " + stockId + ", 损耗数量: " + waste + ", 盘库类型: " + inventoryType + ", 操作员ID: " + operatorId);

		try {
			// 1. 参数校验
			if (waste == null || waste.trim().isEmpty()) {
				return R.error("损耗数量不能为空");
			}
			if (inventoryType == null || (inventoryType != 1 && inventoryType != 2 && inventoryType != 3)) {
				return R.error("盘库类型无效，必须是 1(日)、2(周) 或 3(月)");
			}
			if (operatorId == null) {
				return R.error("操作员ID不能为空");
			}
			
			// 如果stockId为空，必须提供disGoodsId和disId
			if (stockId == null) {
				if (disGoodsId == null) {
					return R.error("批次ID为空时，配送商品ID不能为空");
				}
				if (disId == null) {
					return R.error("批次ID为空时，配送商ID不能为空");
				}
			}

			// 2. 校验周期参数
			String date = null;
			String week = null;
			String month = null;
			if (inventoryType == 1) {
				if (inventoryDate == null || inventoryDate.trim().isEmpty()) {
					return R.error("日盘库必须提供盘库日期");
				}
				date = inventoryDate.trim();
			} else if (inventoryType == 2) {
				if (inventoryWeek == null || inventoryWeek.trim().isEmpty()) {
					return R.error("周盘库必须提供盘库周数");
				}
				week = inventoryWeek.trim();
			} else if (inventoryType == 3) {
				if (inventoryMonth == null || inventoryMonth.trim().isEmpty()) {
					return R.error("月盘库必须提供盘库月份");
				}
				month = inventoryMonth.trim();
			}

			// 3. 计算总损耗数量
			BigDecimal totalWasteWeight = new BigDecimal(waste);
			if (totalWasteWeight.compareTo(BigDecimal.ZERO) < 0) {
				return R.error("损耗数量不能为负数");
			}

			// 4. 准备周期字段
			String recordDate = date != null ? date : formatWhatDay(0);
			String recordWeek = week != null ? week : String.valueOf(getWeekOfYear(0));
			String recordMonth = month != null ? month : formatWhatMonth(0);
			String recordFullTime = formatWhatYearDayTime(0);

			// 5. 处理单个批次或批量批次
			List<Map<String, Object>> recordResults = new ArrayList<>();
			BigDecimal remainingWaste = totalWasteWeight;

			if (stockId != null) {
				// 单个批次处理
				Map<String, Object> result = saveInventoryRecordForStock(stockId, remainingWaste, inventoryType,
						date, week, month, operatorId, recordDate, recordWeek, recordMonth, recordFullTime);
				recordResults.add(result);
			} else {
				// 批量批次处理：查询未盘库的批次列表，按最早批次先减去
				Map<String, Object> queryMap = new HashMap<>();
				queryMap.put("disId", disId);
				queryMap.put("disGoodsId", disGoodsId);
				queryMap.put("inventoryType", inventoryType);
				if (date != null) {
					queryMap.put("inventoryDate", date);
				}
				if (week != null) {
					queryMap.put("inventoryWeek", week);
				}
				if (month != null) {
					queryMap.put("inventoryMonth", month);
				}

				// 查询未盘库的批次列表（按日期升序，最早的在前）
				List<NxDistributerGoodsShelfStockEntity> stockList = nxDistributerGoodsShelfStockService.queryStockByDisGoodsIdAndDate(queryMap);
				
				// 过滤出未盘库的批次
				List<NxDistributerGoodsShelfStockEntity> unInventoriedStocks = new ArrayList<>();
				for (NxDistributerGoodsShelfStockEntity stock : stockList) {
					// 检查该批次在当前周期是否已有盘库记录
					Map<String, Object> checkMap = new HashMap<>();
					checkMap.put("stockId", stock.getNxDistributerGoodsShelfStockId());
					checkMap.put("inventoryType", inventoryType);
					if (date != null) {
						checkMap.put("inventoryDate", date);
					}
					if (week != null) {
						checkMap.put("inventoryWeek", week);
					}
					if (month != null) {
						checkMap.put("inventoryMonth", month);
					}
					
					NxDistributerGoodsShelfStockReduceEntity existingRecord = 
						nxDistributerGoodsShelfStockReduceService.queryStockInventoryRecord(checkMap);
					
					if (existingRecord == null) {
						unInventoriedStocks.add(stock);
					}
				}

				if (unInventoriedStocks.isEmpty()) {
					return R.error("未找到未盘库的库存批次");
				}

				// 按最早批次先减去损耗
				for (NxDistributerGoodsShelfStockEntity stock : unInventoriedStocks) {
					if (remainingWaste.compareTo(BigDecimal.ZERO) <= 0) {
						break; // 损耗已全部分摊完毕
					}

					// 计算当前批次可分摊的损耗数量（不超过剩余库存）
					BigDecimal restWeight = new BigDecimal(stock.getNxDgssRestWeight());
					BigDecimal wasteForThisStock = remainingWaste.min(restWeight);

					Map<String, Object> result = saveInventoryRecordForStock(
							stock.getNxDistributerGoodsShelfStockId(),
							wasteForThisStock,
							inventoryType,
							date, week, month,
							operatorId,
							recordDate, recordWeek, recordMonth, recordFullTime);
					recordResults.add(result);

					remainingWaste = remainingWaste.subtract(wasteForThisStock);
				}

				if (remainingWaste.compareTo(BigDecimal.ZERO) > 0) {
					return R.error("损耗数量(" + totalWasteWeight + ")超过所有未盘库批次的剩余库存总和");
				}
			}

			// 6. 返回结果
			Map<String, Object> result = new HashMap<>();
			result.put("records", recordResults);
			result.put("totalWasteWeight", totalWasteWeight.toString());
			result.put("recordCount", recordResults.size());
			
			System.out.println("======== 保存盘库记录完成 ========");
			return R.ok().put("data", result);

		} catch (NumberFormatException e) {
			e.printStackTrace();
			return R.error("损耗数量格式错误：" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return R.error("保存盘库记录失败：" + e.getMessage());
		}
	}

	/**
	 * 为单个批次保存盘库记录
	 */
	private Map<String, Object> saveInventoryRecordForStock(Integer stockId, BigDecimal wasteWeight,
	                                                         Integer inventoryType,
	                                                         String date, String week, String month,
	                                                         Integer operatorId,
	                                                         String recordDate, String recordWeek, String recordMonth, String recordFullTime) {
		// 1. 查询批次信息
		NxDistributerGoodsShelfStockEntity stockEntity = nxDistributerGoodsShelfStockService.queryObject(stockId);
		if (stockEntity == null) {
			throw new RuntimeException("批次不存在: " + stockId);
		}

		// 2. 计算损耗金额
		String priceStr = stockEntity.getNxDgssPrice();
		if (priceStr == null || priceStr.trim().isEmpty()) {
			priceStr = "0";
		}
		BigDecimal stockPrice = new BigDecimal(priceStr);
		BigDecimal wasteSubtotal = wasteWeight.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);

		// 3. 查询当前周期是否已有盘库记录
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("stockId", stockId);
		queryMap.put("inventoryType", inventoryType);
		if (date != null) {
			queryMap.put("inventoryDate", date);
		}
		if (week != null) {
			queryMap.put("inventoryWeek", week);
		}
		if (month != null) {
			queryMap.put("inventoryMonth", month);
		}

		NxDistributerGoodsShelfStockReduceEntity existingRecord = 
			nxDistributerGoodsShelfStockReduceService.queryStockInventoryRecord(queryMap);

		// 4. 创建或更新盘库记录
		NxDistributerGoodsShelfStockReduceEntity reduceEntity;
		boolean isNewRecord = false; // 标记是否为新创建记录
		if (existingRecord != null) {
			// 更新已有记录（累加损耗）
			reduceEntity = existingRecord;
			BigDecimal existingWaste = new BigDecimal(reduceEntity.getNxDgssrWasteWeight());
			BigDecimal existingSubtotal = new BigDecimal(reduceEntity.getNxDgssrWasteSubtotal());
			
			BigDecimal newWaste = existingWaste.add(wasteWeight);
			BigDecimal newSubtotal = existingSubtotal.add(wasteSubtotal);
			
			reduceEntity.setNxDgssrWasteWeight(newWaste.toString());
			reduceEntity.setNxDgssrWasteSubtotal(newSubtotal.toString());
			System.out.println("更新已有盘库记录，记录ID: " + reduceEntity.getNxDistributerGoodsShelfStockReduceId() + ", 新增损耗: " + wasteWeight);
			// 更新已有记录时，库存已经在创建记录时扣减过了，不需要再次扣减
		} else {
			isNewRecord = true; // 标记为新创建记录
			// 创建新记录
			reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
			reduceEntity.setNxDgssrNxDistributerId(stockEntity.getNxDgssNxDistributerId());
			reduceEntity.setNxDgssrNxDisGoodsId(stockEntity.getNxDgssNxDisGoodsId());
			reduceEntity.setNxDgssrNxDisGoodsFatherId(stockEntity.getNxDgssNxDisGoodsFatherId());
			reduceEntity.setNxDgssrNxStockId(stockId);
			reduceEntity.setNxDgssrDate(recordDate);
			reduceEntity.setNxDgssrWeek(recordWeek);
			reduceEntity.setNxDgssrMonth(recordMonth);
			reduceEntity.setNxDgssrFullTime(recordFullTime);
			reduceEntity.setNxDgssrType(2); // 2=盘库损失
			reduceEntity.setNxDgssrDoUserId(operatorId);
			reduceEntity.setNxDgssrWasteWeight(wasteWeight.toString());
			reduceEntity.setNxDgssrWasteSubtotal(wasteSubtotal.toString());
			reduceEntity.setNxDgssrCostWeight(wasteWeight.toString());
			reduceEntity.setNxDgssrCostSubtotal(wasteSubtotal.toString());
			reduceEntity.setNxDgssrProduceWeight("0");
			reduceEntity.setNxDgssrProduceSubtotal("0");
			reduceEntity.setNxDgssrLossWeight("0");
			reduceEntity.setNxDgssrLossSubtotal("0");
			reduceEntity.setNxDgssrNxPurGoodsId(stockEntity.getNxDgssNxPurGoodsId());
			reduceEntity.setNxDgssrGoodsInventoryType(inventoryType); // 盘库类型
			reduceEntity.setNxDgssrStatus(0);
			System.out.println("创建新盘库记录");
		}

		// 5. 保存或更新
		if (existingRecord != null) {
			nxDistributerGoodsShelfStockReduceService.update(reduceEntity);
			System.out.println("盘库记录已更新 - 批次ID: " + stockId + ", 损耗数量: " + wasteWeight + ", 损耗金额: " + wasteSubtotal);
		} else {
			nxDistributerGoodsShelfStockReduceService.save(reduceEntity);
			System.out.println("盘库记录已创建 - 批次ID: " + stockId + ", 损耗数量: " + wasteWeight + ", 损耗金额: " + wasteSubtotal);
		}

		// 6. 更新库存批次剩余数量（只在新创建记录时扣减，更新已有记录时已经扣减过了）
		if (isNewRecord) {
			BigDecimal currentRestWeight = new BigDecimal(stockEntity.getNxDgssRestWeight());
			BigDecimal newRestWeight = currentRestWeight.subtract(wasteWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
			if (newRestWeight.compareTo(BigDecimal.ZERO) < 0) {
				newRestWeight = BigDecimal.ZERO;
			}
			stockEntity.setNxDgssRestWeight(newRestWeight.toString());
			BigDecimal newRestSubtotal = newRestWeight.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
			stockEntity.setNxDgssRestSubtotal(newRestSubtotal.toString());
			// 更新盘库相关字段
			stockEntity.setNxDgssInventoryDate(recordDate);
			stockEntity.setNxDgssInventoryFullTime(recordFullTime);
			if (week != null) {
				stockEntity.setNxDgssInventoryWeek(week);
			}
			if (month != null) {
				stockEntity.setNxDgssInventoryMonth(month);
			}
			nxDistributerGoodsShelfStockService.update(stockEntity);
			System.out.println("库存批次已更新 - 批次ID: " + stockId + ", 原剩余数量: " + currentRestWeight + ", 扣减数量: " + wasteWeight + ", 新剩余数量: " + newRestWeight);
		} else {
			System.out.println("更新已有盘库记录，库存已在创建记录时扣减，无需再次扣减");
		}

		// 7. 返回结果
		Map<String, Object> result = new HashMap<>();
		result.put("recordId", reduceEntity.getNxDistributerGoodsShelfStockReduceId());
		result.put("stockId", stockId);
		result.put("wasteWeight", wasteWeight.toString());
		result.put("wasteSubtotal", wasteSubtotal.toString());
		result.put("isUpdate", existingRecord != null);

		return result;
	}

	/**
	 * 获取成本商品统计信息
	 */
	@RequestMapping(value = "/getNxCostGoodsStatistics", method = RequestMethod.POST)
	@ResponseBody
	public R getNxCostGoodsStatistics(@RequestParam String supplierIds,
	                                  @RequestParam String purUserIds,
	                                  @RequestParam Integer disId,
	                                  @RequestParam String startDate,
	                                  @RequestParam String stopDate) {

		try {
			// 构建查询参数
			Map<String, Object> map = new HashMap<>();

			if (!purUserIds.equals("-1")) {
				String[] arrNx = purUserIds.split(",");
				List<String> idsNx = new ArrayList<>();
				for (String idNx : arrNx) {
					idsNx.add(idNx);
					if (idsNx.size() > 0) {
						map.put("purUserIds", idsNx);
					}
				}
			}
			if (!supplierIds.equals("-1")) {
				String[] arrNx = supplierIds.split(",");
				List<String> idsNx = new ArrayList<>();
				for (String idNx : arrNx) {
					idsNx.add(idNx);
					if (idsNx.size() > 0) {
						map.put("supplierIds", idsNx);
					}
				}
			}

			map.put("disId", disId);
			map.put("startDate", startDate);
			map.put("stopDate", stopDate);
			Integer integer = nxDistributerGoodsShelfStockReduceService.queryReduceTypeCount(map);
			String costTotal = "";
			if (integer > 0) {
				// 销售成本（type = 0, 1）
				List<Integer> saleTypes = new ArrayList<>();
				saleTypes.add(0);
				saleTypes.add(1);
				map.put("types", saleTypes);
				Double produceTotal = nxDistributerGoodsShelfStockReduceService.queryReduceCostSubtotal(map);
				
				// 损耗成本（type = 2, 3）：包括浪费(type=2)和损耗(type=3)
				map.remove("types");
				List<Integer> lossTypes = new ArrayList<>();
				lossTypes.add(2);
				lossTypes.add(3);
				map.put("types", lossTypes);
				// 浪费成本（type = 2）
				map.remove("types");
				map.put("equalType", 2);
				Double wasteTotal = nxDistributerGoodsShelfStockReduceService.queryReduceWasteSubtotal(map);
				// 损耗成本（type = 3）
				map.put("equalType", 3);
				Double lossTotal = nxDistributerGoodsShelfStockReduceService.queryReduceLossSubtotal(map);
				// 总损耗成本 = 浪费成本 + 损耗成本
				Double totalLoss = (wasteTotal != null ? wasteTotal : 0.0) + (lossTotal != null ? lossTotal : 0.0);
				
				// 退货成本（type = 4）
				map.remove("equalType");
				Double returnTotal = nxDistributerGoodsShelfStockReduceService.queryReduceReturnTotal(map);
				
				double v = (produceTotal != null ? produceTotal : 0.0) + 
				           totalLoss + 
				           (returnTotal != null ? returnTotal : 0.0);
				costTotal = String.format("%.1f", v);
			}

			//按销售成本TOP（type = 0, 1）
			System.out.println("tootititittiitProduce" + map);
			List<NxDistributerGoodsEntity> topGoodsProduce = nxDistributerGoodsShelfStockReduceService.queryStockProduceSubtotalTopTimes(map);

			//按损耗成本TOP（type = 2, 3）：包括浪费和损耗
			List<NxDistributerGoodsEntity> topGoodsLoss = nxDistributerGoodsShelfStockReduceService.queryStockLossSubtotalTopTimes(map);

			//按日支出
			System.out.println("tootititittiitDDDD" + map);
			List<Map<String, Object>> topDayCost = nxDistributerGoodsShelfStockReduceService.queryNxPurchaseGoodsTopDay(map);

			// 构建返回数据
			Map<String, Object> result = new HashMap<>();

			result.put("costTotal", costTotal);
			result.put("topGoodsProduce", topGoodsProduce);
			result.put("topGoodsLoss", topGoodsLoss);
			result.put("topDayCost", topDayCost);
			return R.ok().put("data", result);

		} catch (Exception e) {
			e.printStackTrace();
			return R.error(-1, "没有数据");
		}
	}

	/**
	 * 获取商品出货数据（带日期数据）
	 */
	@RequestMapping(value = "/getGoodsReduceWithDayData", method = RequestMethod.POST)
	@ResponseBody
	public R getGoodsReduceWithDayData(@RequestParam String startDate,
	                                   @RequestParam String stopDate,
	                                   @RequestParam Integer disGoodsId,
	                                   @RequestParam(required = false, defaultValue = "-1") String searchDepId) {
		Integer howManyDaysInPeriod = 0;
		if (!startDate.equals(stopDate)) {
			howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
		}
		Map<String, Object> mapResult = new HashMap<>();
		List<String> dateList = new ArrayList<>();
		List<Map<String, Object>> producelist = new ArrayList<>();
		List<Map<String, Object>> losslist = new ArrayList<>();
		List<Map<String, Object>> wastelist = new ArrayList<>();
		List<Map<String, Object>> listItem = new ArrayList<>();

		Map<String, Object> map0 = new HashMap<>();
		if (howManyDaysInPeriod > 0) {
			map0.put("startDate", startDate);
			map0.put("stopDate", stopDate);
		} else {
			map0.put("date", startDate);
		}
		map0.put("disGoodsId", disGoodsId);

		if (searchDepId != null && !searchDepId.equals("-1")) {
			map0.put("depId", searchDepId);
		}

		Integer integer1 = nxDistributerGoodsShelfStockReduceService.queryReduceTypeCount(map0);
		if (integer1 == 0) {
			return R.error(-1, "没有数据");
		}

		// 统计总金额
		Double weightTotalTL = nxDistributerGoodsShelfStockReduceService.queryReduceLossSubtotal(map0);
		Double weightTotalTW = nxDistributerGoodsShelfStockReduceService.queryReduceWasteSubtotal(map0);
		Double weightTotalS = nxDistributerGoodsShelfStockReduceService.queryReduceProduceSubtotal(map0);

		// 统计总重量
		Double weightTotalTLW = nxDistributerGoodsShelfStockReduceService.queryReduceLossWeightTotal(map0);
		Double weightTotalTWW = nxDistributerGoodsShelfStockReduceService.queryReduceWasteWeightTotal(map0);
		Double weightTotalSW = nxDistributerGoodsShelfStockReduceService.queryReduceProduceWeightTotal(map0);

		double weightTotalP = weightTotalS + weightTotalTL + weightTotalTW;
		double weightTotalPW = weightTotalSW + weightTotalTLW + weightTotalTWW;

		if (howManyDaysInPeriod > 0) {
			// 按天统计
			for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
				// dateList
				String whichDay = "";
				if (i == 0) {
					whichDay = startDate;
				} else {
					whichDay = afterWhatDay(startDate, i);
				}
				String substring = whichDay.substring(8, 10);
				dateList.add(substring);

				Map<String, Object> map = new HashMap<>();
				map.put("date", whichDay);
				map.put("disGoodsId", disGoodsId);
				if (searchDepId != null && !searchDepId.equals("-1")) {
					map.put("depId", searchDepId);
				}

				Integer integer = nxDistributerGoodsShelfStockReduceService.queryReduceTypeCount(map);
				Double costTotal = 0.0;
				Double produceSubtotal = 0.0;
				Double lossSubtotal = 0.0;
				Double wasteSubtotal = 0.0;
				if (integer > 0) {
					System.out.println("kaankanank25hao" + map);
					lossSubtotal = nxDistributerGoodsShelfStockReduceService.queryReduceLossSubtotal(map);
					wasteSubtotal = nxDistributerGoodsShelfStockReduceService.queryReduceWasteSubtotal(map);
					produceSubtotal = nxDistributerGoodsShelfStockReduceService.queryReduceProduceSubtotal(map);
					costTotal = produceSubtotal + lossSubtotal + wasteSubtotal;
				}
				// 销售
				Map<String, Object> mapPro = new HashMap<>();
				mapPro.put("date", whichDay);
				mapPro.put("value", String.format("%.1f", produceSubtotal));
				producelist.add(mapPro);

				// 损耗
				Map<String, Object> mapLoss = new HashMap<>();
				mapLoss.put("date", whichDay);
				mapLoss.put("value", String.format("%.1f", lossSubtotal));
				losslist.add(mapLoss);

				// 废弃
				Map<String, Object> mapWaste = new HashMap<>();
				mapWaste.put("date", whichDay);
				mapWaste.put("value", String.format("%.1f", wasteSubtotal));
				wastelist.add(mapWaste);

				if (costTotal > 0) {
					map.put("depId", null);
					System.out.println("kanakndninteterere" + map);
					Integer integerD = nxDistributerGoodsShelfStockReduceService.queryReduceTypeCount(map);
					Map<String, Object> mapReduce = new HashMap<>();
					mapReduce.put("date", whichDay);
					if (integerD > 0) {
						List<NxDepartmentEntity> departmentEntities = nxDistributerGoodsShelfStockReduceService.queryReduceDepartment(map);
						if (departmentEntities.size() > 0) {
							// 为每个门店补充详细数据
							List<NxDepartmentEntity> depReduceData = getDepReduceDataAll(departmentEntities, map);
							mapReduce.put("arr", depReduceData);
						} else {
							mapReduce.put("arr", new ArrayList<>());
						}
						listItem.add(mapReduce);
					}
				}
			}
		}

		mapResult.put("itemList", listItem);
		mapResult.put("produceList", producelist);
		mapResult.put("lossList", losslist);
		mapResult.put("wasteList", wastelist);
		mapResult.put("oneTotal", new BigDecimal(weightTotalP).setScale(1, BigDecimal.ROUND_HALF_UP));
		mapResult.put("oneTotalWeight", new BigDecimal(weightTotalPW).setScale(1, BigDecimal.ROUND_HALF_UP));
		mapResult.put("salesTotal", new BigDecimal(weightTotalS).setScale(1, BigDecimal.ROUND_HALF_UP));
		mapResult.put("salesTotalWeight", new BigDecimal(weightTotalSW).setScale(1, BigDecimal.ROUND_HALF_UP));
		mapResult.put("lossTotal", new BigDecimal(weightTotalTL).setScale(1, BigDecimal.ROUND_HALF_UP));
		mapResult.put("lossTotalWeight", new BigDecimal(weightTotalTLW).setScale(1, BigDecimal.ROUND_HALF_UP));
		mapResult.put("wasteTotal", new BigDecimal(weightTotalTW).setScale(1, BigDecimal.ROUND_HALF_UP));
		mapResult.put("wasteTotalWeight", new BigDecimal(weightTotalTWW).setScale(1, BigDecimal.ROUND_HALF_UP));
		mapResult.put("date", dateList);

		// ========== 添加总体AI分析 ==========
		Map<String, Object> overallAiAnalysis = generateOverallAiAnalysis(
			weightTotalS, weightTotalTL, weightTotalTW, disGoodsId, howManyDaysInPeriod, weightTotalPW, weightTotalP);
		if (overallAiAnalysis != null) {
			mapResult.put("aiAnalysis", overallAiAnalysis);
		}

		return R.ok().put("data", mapResult);
	}

	/**
	 * 为门店列表补充详细数据
	 */
	private List<NxDepartmentEntity> getDepReduceDataAll(List<NxDepartmentEntity> departmentEntities, Map<String, Object> map) {
		for (NxDepartmentEntity departmentEntity : departmentEntities) {
			Map<String, Object> depMap = new HashMap<>(map);
			depMap.put("depId", departmentEntity.getNxDepartmentId());
			
			// 查询该门店的出货记录
			List<NxDistributerGoodsShelfStockReduceEntity> reduceEntities = nxDistributerGoodsShelfStockReduceService.queryReduceListByParams(depMap);
			// 这里可以将出货记录设置到门店实体中，如果 NxDepartmentEntity 有对应的字段
			
			// 统计该门店的总成本
			Double produceTotal = nxDistributerGoodsShelfStockReduceService.queryReduceProduceSubtotal(depMap);
			Double wasteTotal = nxDistributerGoodsShelfStockReduceService.queryReduceWasteSubtotal(depMap);
			Double lossTotal = nxDistributerGoodsShelfStockReduceService.queryReduceLossSubtotal(depMap);
			Double returnTotal = nxDistributerGoodsShelfStockReduceService.queryReduceReturnTotal(depMap);
			double v = (produceTotal != null ? produceTotal : 0.0) + 
			           (wasteTotal != null ? wasteTotal : 0.0) + 
			           (lossTotal != null ? lossTotal : 0.0) + 
			           (returnTotal != null ? returnTotal : 0.0);
			
			// 统计该门店的总重量
			Double produceTotalWeight = nxDistributerGoodsShelfStockReduceService.queryReduceProduceWeightTotal(depMap);
			Double wasteTotalWeight = nxDistributerGoodsShelfStockReduceService.queryReduceWasteWeightTotal(depMap);
			Double lossTotalWeight = nxDistributerGoodsShelfStockReduceService.queryReduceLossWeightTotal(depMap);
			double vW = (produceTotalWeight != null ? produceTotalWeight : 0.0) + 
			            (wasteTotalWeight != null ? wasteTotalWeight : 0.0) + 
			            (lossTotalWeight != null ? lossTotalWeight : 0.0);
			
			// 设置总成本和总重量（如果实体有对应字段，可以使用 setter 方法）
			// 这里可以根据 NxDepartmentEntity 的实际字段来设置
		}
		return departmentEntities;
	}

	/**
	 * 生成总体AI分析 - 基于整个统计周期的数据
	 */
	private Map<String, Object> generateOverallAiAnalysis(Double totalProduce, Double totalLoss, Double totalWaste,
	                                                     Integer disGoodsId, Integer howManyDaysInPeriod, double weightTotalPW,
	                                                     double weightTotalP) {
		try {
			Map<String, Object> aiResult = new HashMap<>();
			
			// 1. 基础数据计算
			double totalUsage = (totalProduce != null ? totalProduce : 0.0) + 
			                    (totalLoss != null ? totalLoss : 0.0) + 
			                    (totalWaste != null ? totalWaste : 0.0); // 实际总使用量
			double aWeight = howManyDaysInPeriod > 0 ? weightTotalPW / howManyDaysInPeriod : 0.0; //平均用量
			double aSubtotal = howManyDaysInPeriod > 0 ? weightTotalP / howManyDaysInPeriod : 0.0; //平均成本

			// 2. 效率分析
			double lossRate = totalUsage > 0 ? ((totalLoss != null ? totalLoss : 0.0) / totalUsage) * 100 : 0;
			double wasteRate = totalUsage > 0 ? ((totalWaste != null ? totalWaste : 0.0) / totalUsage) * 100 : 0;
			double totalWasteRate = totalUsage > 0 ? ((totalWaste != null ? totalWaste : 0.0) / totalUsage) * 100 : 0;

			String type = "normal";
			List<String> suggestions = new ArrayList<>();

			// 3. 获取商品信息（NX项目没有controlFresh字段，简化逻辑）
			try {
				NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(disGoodsId);
				if (goods != null) {
					// NX项目可以在这里添加其他商品属性判断
					// 例如：根据商品类型、分类等设置type
				}
			} catch (Exception e) {
				System.err.println("查询商品信息出错: " + e.getMessage());
			}

			// 4. 问题预警
			List<String> warnings = new ArrayList<>();
			if (wasteRate > 0) {
				warnings.add("⚠️ 统计周期内存在废弃情况，需要关注商品质量或存储条件");
			}
			if (lossRate > 10) {
				warnings.add("⚠️ 平均损耗率较高(" + String.format("%.1f", lossRate) + "%)，建议检查操作流程");
			}
			if (totalWasteRate > 15) {
				warnings.add("⚠️ 总浪费率过高(" + String.format("%.1f", totalWasteRate) + "%)，需要优化管理");
			}

			// 5. 管理建议
			if (wasteRate > 0) {
				suggestions.add("💡 建议检查存储条件，优化商品管理流程");
			}
			if (totalWasteRate < 5) {
				suggestions.add("✅ 商品管理良好，浪费率控制在合理范围内");
			}
			if (lossRate < 5 && wasteRate == 0) {
				suggestions.add("🎉 商品管理优秀，损耗和废弃都控制得很好");
			}
			if (lossRate > 5) {
				suggestions.add("💡 建议加强员工培训，减少操作损耗");
			}

			// 6. 查询库存剩余重量和金额
			Map<String, Object> map = new HashMap<>();
			map.put("disGoodsId", disGoodsId);
			Double weightTotal = nxDistributerGoodsShelfStockService.queryShelfStockRestWeightTotal(map);
			Double aDouble = nxDistributerGoodsShelfStockService.queryShelfStockRestTotal(map);

			// 7. 构建返回结果
			aiResult.put("suggestions", suggestions);
			aiResult.put("type", type);
			aiResult.put("averageSubtotal", new BigDecimal(aSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP));
			aiResult.put("averageWeight", new BigDecimal(aWeight).setScale(1, BigDecimal.ROUND_HALF_UP));
			aiResult.put("lossRate", new BigDecimal(lossRate).setScale(1, BigDecimal.ROUND_HALF_UP));
			aiResult.put("wasteRate", new BigDecimal(wasteRate).setScale(1, BigDecimal.ROUND_HALF_UP));
			aiResult.put("totalWasteRate", new BigDecimal(totalWasteRate).setScale(1, BigDecimal.ROUND_HALF_UP));
			aiResult.put("stockWeight", new BigDecimal(weightTotal != null ? weightTotal : 0.0).setScale(1, BigDecimal.ROUND_HALF_UP));
			aiResult.put("stockSubtotal", new BigDecimal(aDouble != null ? aDouble : 0.0).setScale(1, BigDecimal.ROUND_HALF_UP));
			aiResult.put("totalCostWeight", new BigDecimal(weightTotalPW).setScale(1, BigDecimal.ROUND_HALF_UP));
			aiResult.put("totalCostSubtotal", new BigDecimal(weightTotalP).setScale(1, BigDecimal.ROUND_HALF_UP));
			if (!warnings.isEmpty()) {
				aiResult.put("warnings", warnings);
			}

			return aiResult;
			
		} catch (Exception e) {
			// 如果AI分析出错，返回null，不影响原有功能
			System.err.println("总体AI分析出错: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
}
