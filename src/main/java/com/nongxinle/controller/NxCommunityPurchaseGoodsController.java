package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 12-02 20:50
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.NxCommunityPurchaseBatchService;
import com.nongxinle.service.NxRestrauntOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxCommunityPurchaseGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.DateUtils.formatWhatTime;


@RestController
@RequestMapping("api/nxcommunitypurchasegoods")
public class NxCommunityPurchaseGoodsController {
	private static final Logger logger = LoggerFactory.getLogger(NxCommunityPurchaseGoodsController.class);

	@Autowired
	private NxCommunityPurchaseGoodsService nxComPurchaseGoodsService;


	@RequestMapping(value = "/comGetPurGoods/{id}")
	@ResponseBody
	public R comGetPurGoods(@PathVariable Integer id) {
		Map<String, Object> map4 = new HashMap<>();
		map4.put("comId", id);
		map4.put("buyStatus", 1);
		List<NxCommunityFatherGoodsEntity> purchaseToday = nxComPurchaseGoodsService.queryComPurchaseGoods(map4);
		return R.ok().put("data", purchaseToday);
	}


	/**
	 * 获取社区采购分类目录（简化版）
	 * 按状态统计每个分类下的采购商品数量
	 */
	@RequestMapping(value = "/commGetTypePrepareOutCata", method = RequestMethod.POST)
	@ResponseBody
	public R commGetTypePrepareOutCata(Integer commId, Integer equalStatus) {
		logger.info("[commGetTypePrepareOutCata] 开始查询，参数: commId={}, equalStatus={}", commId, equalStatus);
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("commId", commId);
			if (equalStatus != null) {
				map.put("equalStatus", equalStatus);
			}
			logger.info("[commGetTypePrepareOutCata] 查询条件 map: {}", map);

			List<NxCommunityFatherGoodsEntity> fatherList = nxComPurchaseGoodsService.queryCommFatherGoodsSimple(map);
			logger.info("[commGetTypePrepareOutCata] 查询结果数量: {}", fatherList != null ? fatherList.size() : 0);

			logger.info("[commGetTypePrepareOutCata] 返回数据完成");
			return R.ok().put("data", fatherList);
		} catch (Exception e) {
			logger.error("[commGetTypePrepareOutCata] 查询异常: ", e);
			return R.error("查询失败: " + e.getMessage());
		}
	}

	/**
	 * 分页获取社区采购商品列表（简化版）
	 * 按状态筛选，返回商品和父分类信息
	 */
	@RequestMapping(value = "/commGetTypePreparePurGoodsPage", method = RequestMethod.POST)
	@ResponseBody
	public R commGetTypePreparePurGoodsPage(Integer commId,
											Integer equalStatus,
											Integer page,
											Integer limit) {
		logger.info("[commGetTypePreparePurGoodsPage] 开始查询，参数: commId={}, equalStatus={}, dayuStatus={}, page={}, limit={}",
				commId, equalStatus, page, limit);

		Map<String, Object> map = new HashMap<>();
		map.put("commId", commId);
		if (equalStatus != null) {
			map.put("equalStatus", equalStatus);
		}

		if (page != null && limit != null) {
			map.put("offset", (page - 1) * limit);
			map.put("limit", limit);
		}

		logger.info("[commGetTypePreparePurGoodsPage] 查询条件 map: {}", map);

		List<NxCommunityPurchaseGoodsEntity> purchaseGoodsList = nxComPurchaseGoodsService.queryCommPurchaseGoodsSimple(map);
		logger.info("[commGetTypePreparePurGoodsPage] 查询结果数量: {}", purchaseGoodsList != null ? purchaseGoodsList.size() : 0);

		// 查询总数
		Map<String, Object> mapCount = new HashMap<>();
		mapCount.put("commId", commId);
		if (equalStatus != null) {
			mapCount.put("equalStatus", equalStatus);
		}

		Integer total = nxComPurchaseGoodsService.queryCommPurchaseGoodsCount(mapCount);
		logger.info("[commGetTypePreparePurGoodsPage] 采购商品总数: {}", total);

		PageUtils pageUtil = new PageUtils(purchaseGoodsList, total, limit, page);
		logger.info("[commGetTypePreparePurGoodsPage] 返回数据完成");
		return R.ok().put("page", pageUtil);
	}

	
}
