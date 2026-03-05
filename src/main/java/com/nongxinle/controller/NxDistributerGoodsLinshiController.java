package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 08-14 22:10
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsLinshiEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;
import com.nongxinle.service.NxDistributerGoodsLinshiService;
import com.nongxinle.service.NxDistributerGoodsShelfGoodsService;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/nxdistributergoodslinshi")
public class NxDistributerGoodsLinshiController {
	private static final Logger logger = LoggerFactory.getLogger(NxDistributerGoodsLinshiController.class);
	
	@Autowired
	private NxDistributerGoodsLinshiService nxDistributerGoodsLinshiService;
	
	@Autowired
	private NxDistributerGoodsShelfGoodsService nxDistributerGoodsShelfGoodsService;

	@Autowired
	private NxDistributerGoodsService nxDistributerGoodsService;


	/**
	 * 获取已替换成功的临时商品列表（status=-1）
	 */
	@RequestMapping(value = "/disGetLinshiGoodsList", method = RequestMethod.POST)
	@ResponseBody
	public R disGetLinshiGoodsList(Integer disId, Integer page, Integer limit) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		logger.info("[disGetLinshiGoodsList] 查询参数: disId={}, page={}, limit={}", disId, page, limit);
		List<NxDistributerGoodsLinshiEntity> linshiEntities = nxDistributerGoodsLinshiService.disGetLinshiGoodsList(map);

		// 如果有货架商品，增加查询nxDistributerGoodsShelfGoodsEntities
		if (linshiEntities != null && !linshiEntities.isEmpty()) {
			for (NxDistributerGoodsLinshiEntity linshiEntity : linshiEntities) {
				NxDistributerGoodsEntity nxDistributerGoodsEntity = linshiEntity.getNxDistributerGoodsEntity();
				if (nxDistributerGoodsEntity != null && nxDistributerGoodsEntity.getNxDistributerGoodsId() != null) {
					Map<String, Object> shelfGoodsMap = new HashMap<>();
					shelfGoodsMap.put("disGoodsId", nxDistributerGoodsEntity.getNxDistributerGoodsId());
					List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsEntities = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(shelfGoodsMap);
					if (shelfGoodsEntities != null && !shelfGoodsEntities.isEmpty()) {
						nxDistributerGoodsEntity.setNxDistributerGoodsShelfGoodsEntities(shelfGoodsEntities);
						logger.debug("[disGetLinshiGoodsList] 商品ID={} 查询到{}个货架商品", 
								nxDistributerGoodsEntity.getNxDistributerGoodsId(), shelfGoodsEntities.size());
					}
				}
			}
		}

		int total = nxDistributerGoodsLinshiService.disGetLinshiGoodsTotal(map);

		PageUtils pageUtil = new PageUtils(linshiEntities, total, limit, page);

		return R.ok().put("page", pageUtil);
	}

	/**
	 * 添加推荐商品（创建或更新linshi）
	 * @param lsGoodsId 临时商品ID
	 * @param nxGoodsIds 推荐的nxGoodsId列表，支持：1) 逗号分隔字符串 "101,102,103"  2) 表单 nxGoodsIds=101&nxGoodsIds=102
	 */
	@RequestMapping(value = "/addRecommendGoods", method = RequestMethod.POST)
	@ResponseBody
	public R addRecommendGoods(Integer lsGoodsId, @RequestParam(value = "nxGoodsIds", required = false) List<Integer> nxGoodsIds,
	                          @RequestParam(value = "nxGoodsIdsStr", required = false) String nxGoodsIdsStr) {
		List<Integer> ids = nxGoodsIds;
		if (ids == null || ids.isEmpty()) {
			if (nxGoodsIdsStr != null && !nxGoodsIdsStr.trim().isEmpty()) {
				try {
					ids = Arrays.stream(nxGoodsIdsStr.split(",")).map(String::trim).filter(s -> !s.isEmpty())
							.map(Integer::parseInt).collect(Collectors.toList());
				} catch (NumberFormatException e) {
					return R.error("nxGoodsIds格式错误，应为数字，逗号分隔");
				}
			}
		}
		if (lsGoodsId == null || ids == null || ids.isEmpty()) {
			return R.error("参数错误：lsGoodsId和nxGoodsIds必填");
		}
		String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
		NxDistributerGoodsLinshiEntity linshi = nxDistributerGoodsLinshiService.queryLinshiByFromGoodsId(lsGoodsId);
		if (linshi == null) {
			NxDistributerGoodsEntity tempGoods = nxDistributerGoodsService.queryObject(lsGoodsId);
			if (tempGoods == null) {
				return R.error("临时商品不存在");
			}
			linshi = new NxDistributerGoodsLinshiEntity();
			linshi.setNxDgFromNxDisGoodsId(lsGoodsId);
			linshi.setNxDgDistributerLsId(tempGoods.getNxDgDistributerId());
			linshi.setNxDgGoodsLsName(tempGoods.getNxDgGoodsName());
			linshi.setNxDgGoodsLsStandardname(tempGoods.getNxDgGoodsStandardname());
			linshi.setNxDgDfgGoodsFatherLsId(tempGoods.getNxDgDfgGoodsFatherId());
			linshi.setNxDgGoodsLsDetail(tempGoods.getNxDgGoodsDetail());
			linshi.setNxDgGoodsLsFile(tempGoods.getNxDgGoodsFile());
			linshi.setNxDgGoodsLsFileLarge(tempGoods.getNxDgGoodsFileLarge());
			linshi.setNxDgApplyDate(tempGoods.getNxDgBuyingPriceUpdate());
			linshi.setNxDgGoodsLsStatus(1);
			linshi.setNxDgRecommendNxGoodsIds(idsStr);
			nxDistributerGoodsLinshiService.save(linshi);
		} else {
			LinkedHashSet<Integer> set = new LinkedHashSet<>(ids);
			linshi.setNxDgRecommendNxGoodsIds(set.stream().map(String::valueOf).collect(Collectors.joining(",")));
			linshi.setNxDgGoodsLsStatus(1);
			nxDistributerGoodsLinshiService.update(linshi);
		}
		return R.ok().put("data", linshi);
	}

	/**
	 * 移除推荐商品
	 * @param lsGoodsId 临时商品ID
	 * @param nxGoodsId 要移除的nxGoodsId
	 */
	@RequestMapping(value = "/removeRecommendGoods", method = RequestMethod.POST)
	@ResponseBody
	public R removeRecommendGoods(Integer lsGoodsId, Integer nxGoodsId) {
		if (lsGoodsId == null || nxGoodsId == null) {
			return R.error("参数错误：lsGoodsId和nxGoodsId必填");
		}
		NxDistributerGoodsLinshiEntity linshi = nxDistributerGoodsLinshiService.queryLinshiByFromGoodsId(lsGoodsId);
		if (linshi == null) {
			return R.error("未找到对应的推荐记录");
		}
		String ids = linshi.getNxDgRecommendNxGoodsIds();
		if (ids == null || ids.trim().isEmpty()) {
			return R.ok().put("data", linshi);
		}
		List<Integer> list;
		try {
			list = Arrays.stream(ids.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.map(Integer::parseInt)
					.filter(id -> !nxGoodsId.equals(id))
					.collect(Collectors.toList());
		} catch (NumberFormatException e) {
			return R.error("推荐商品ID格式错误");
		}
		if (list.isEmpty()) {
			linshi.setNxDgRecommendNxGoodsIds(null);
			linshi.setNxDgGoodsLsStatus(0);
			nxDistributerGoodsLinshiService.update(linshi);
		} else {
			linshi.setNxDgRecommendNxGoodsIds(list.stream().map(String::valueOf).collect(Collectors.joining(",")));
			nxDistributerGoodsLinshiService.update(linshi);
		}
		return R.ok().put("data", linshi);
	}

	/**
	 * 申请添加新商品（推荐的都不对，用户表明态度）
	 * @param lsGoodsId 临时商品ID
	 */
	@RequestMapping(value = "/applyAddNewGoods", method = RequestMethod.POST)
	@ResponseBody
	public R applyAddNewGoods(Integer lsGoodsId) {
		if (lsGoodsId == null) {
			return R.error("参数错误：lsGoodsId必填");
		}
		NxDistributerGoodsLinshiEntity linshi = nxDistributerGoodsLinshiService.queryLinshiByFromGoodsId(lsGoodsId);
		if (linshi == null) {
			NxDistributerGoodsEntity tempGoods = nxDistributerGoodsService.queryObject(lsGoodsId);
			if (tempGoods == null) {
				return R.error("临时商品不存在");
			}
			linshi = new NxDistributerGoodsLinshiEntity();
			linshi.setNxDgFromNxDisGoodsId(lsGoodsId);
			linshi.setNxDgDistributerLsId(tempGoods.getNxDgDistributerId());
			linshi.setNxDgGoodsLsName(tempGoods.getNxDgGoodsName());
			linshi.setNxDgGoodsLsStandardname(tempGoods.getNxDgGoodsStandardname());
			linshi.setNxDgDfgGoodsFatherLsId(tempGoods.getNxDgDfgGoodsFatherId());
			linshi.setNxDgGoodsLsDetail(tempGoods.getNxDgGoodsDetail());
			linshi.setNxDgGoodsLsFile(tempGoods.getNxDgGoodsFile());
			linshi.setNxDgGoodsLsFileLarge(tempGoods.getNxDgGoodsFileLarge());
			linshi.setNxDgApplyDate(tempGoods.getNxDgBuyingPriceUpdate());
			linshi.setNxDgGoodsLsStatus(2);
			nxDistributerGoodsLinshiService.save(linshi);
		} else {
			linshi.setNxDgGoodsLsStatus(2);
			nxDistributerGoodsLinshiService.update(linshi);
		}
		return R.ok().put("data", linshi);
	}

}
