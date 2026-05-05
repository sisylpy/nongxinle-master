package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 08-14 22:10
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsLinshiEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;
import com.nongxinle.entity.NxGoodsEntity;
import com.nongxinle.service.NxDistributerGoodsLinshiService;
import com.nongxinle.service.NxDistributerGoodsShelfGoodsService;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.NxGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;
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

	@Autowired
	private NxGoodsService nxGoodsService;


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

	/** 不传 status 时合并查询的最大单源条数，避免内存过大 */
	private static final int SEARCH_ALL_MAX_PER_SOURCE = 500;

	/**
	 * 根据搜索词查询临时商品（支持分页）
	 * @param searchGoodsName 搜索关键词（商品名称，支持拼音）
	 * @param disId 配送商ID
	 * @param status 可选，0=未处理，1=推荐，2=申请添加，-1=已替换；不传则查全部（status0+linshi合并后分页）
	 * @param page 页码
	 * @param limit 每页条数
	 */
	@RequestMapping(value = "/searchLinshiGoods", method = RequestMethod.POST)
	@ResponseBody
	public R searchLinshiGoods(String searchGoodsName, Integer disId,
	                           @RequestParam(required = false) Integer status,
	                           @RequestParam(defaultValue = "1") Integer page,
	                           @RequestParam(defaultValue = "10") Integer limit) {
		if (disId == null) {
			return R.error("disId 必填");
		}
		String searchName = searchGoodsName != null ? searchGoodsName.trim() : null;
		String searchPinyin = null;
		if (searchName != null && !searchName.isEmpty() && searchName.matches(".*[\u4E00-\u9FFF]+.*")) {
			searchPinyin = hanziToPinyin(searchName);
		}
		logger.info("[searchLinshiGoods] 查询参数: disId={}, searchGoodsName={}, status={}, page={}, limit={}",
				disId, searchGoodsName, status, page, limit);

		if (status != null && status == 0) {
			// status=0：未处理的临时商品在 nx_distributer_goods 表（nx_dg_nx_goods_id is null），排除已有linshi记录的
			Map<String, Object> map = new HashMap<>();
			map.put("disId", disId);
			map.put("searchGoodsName", searchName);
			map.put("searchPinyin", searchPinyin);
			map.put("offset", (page - 1) * limit);
			map.put("limit", limit);
			List<Integer> excludeIds = nxDistributerGoodsLinshiService.queryFromGoodsIdsByDisId(disId);
			map.put("excludeFromGoodsIds", excludeIds != null ? excludeIds : new ArrayList<>());
			List<NxDistributerGoodsEntity> goodsEntities = nxDistributerGoodsService.queryLinshiGoodsBySearch(map);
			fillShelfGoods(goodsEntities);
			int total = nxDistributerGoodsService.queryLinshiGoodsBySearchTotal(map);
			PageUtils pageUtil = new PageUtils(goodsEntities, total, limit, page);
			return R.ok().put("page", pageUtil);
		}

		if (status != null && status != 0) {
			// status=1,2,-1：仅从 linshi 表查
			Map<String, Object> map = new HashMap<>();
			map.put("disId", disId);
			map.put("searchGoodsName", searchName);
			map.put("searchPinyin", searchPinyin);
			map.put("status", status);
			map.put("offset", (page - 1) * limit);
			map.put("limit", limit);
			List<NxDistributerGoodsLinshiEntity> linshiEntities = nxDistributerGoodsLinshiService.searchLinshiGoodsList(map);
			fillShelfGoodsForLinshi(linshiEntities);
			fillRecommendGoodsForLinshi(linshiEntities, disId, status);
			int total = nxDistributerGoodsLinshiService.searchLinshiGoodsTotal(map);
			PageUtils pageUtil = new PageUtils(linshiEntities, total, limit, page);
			return R.ok().put("page", pageUtil);
		}

		// status 未传：合并 status=0 与 linshi(1,2,-1)，统一为 NxDistributerGoodsLinshiEntity 结构后分页
		Map<String, Object> map0 = new HashMap<>();
		map0.put("disId", disId);
		map0.put("searchGoodsName", searchName);
		map0.put("searchPinyin", searchPinyin);
		map0.put("offset", 0);
		map0.put("limit", SEARCH_ALL_MAX_PER_SOURCE);
		List<Integer> excludeIds = nxDistributerGoodsLinshiService.queryFromGoodsIdsByDisId(disId);
		map0.put("excludeFromGoodsIds", excludeIds != null ? excludeIds : new ArrayList<>());
		List<NxDistributerGoodsEntity> status0List = nxDistributerGoodsService.queryLinshiGoodsBySearch(map0);
		fillShelfGoods(status0List);
		int total0 = nxDistributerGoodsService.queryLinshiGoodsBySearchTotal(map0);

		Map<String, Object> mapLinshi = new HashMap<>();
		mapLinshi.put("disId", disId);
		mapLinshi.put("searchGoodsName", searchName);
		mapLinshi.put("searchPinyin", searchPinyin);
		mapLinshi.put("status", null);
		mapLinshi.put("offset", 0);
		mapLinshi.put("limit", SEARCH_ALL_MAX_PER_SOURCE);
		List<NxDistributerGoodsLinshiEntity> linshiList = nxDistributerGoodsLinshiService.searchLinshiGoodsList(mapLinshi);
		fillShelfGoodsForLinshi(linshiList);
		fillRecommendGoodsForLinshi(linshiList, disId, null);

		List<NxDistributerGoodsLinshiEntity> merged = new ArrayList<>();
		List<NxDistributerGoodsEntity> list0 = status0List != null ? status0List : new ArrayList<>();
		for (NxDistributerGoodsEntity g : list0) {
			NxDistributerGoodsLinshiEntity wrap = new NxDistributerGoodsLinshiEntity();
			wrap.setNxDgGoodsLsStatus(0);
			wrap.setNxDistributerGoodsEntity(g);
			wrap.setNxDgFromNxDisGoodsId(g.getNxDistributerGoodsId());
			merged.add(wrap);
		}
		if (linshiList != null) {
			merged.addAll(linshiList);
		}
		merged.sort((a, b) -> {
			String na = getGoodsName(a);
			String nb = getGoodsName(b);
			return (na != null ? na : "").compareTo(nb != null ? nb : "");
		});
		int totalLinshi = nxDistributerGoodsLinshiService.searchLinshiGoodsTotal(mapLinshi);
		int total = total0 + totalLinshi;
		int from = (page - 1) * limit;
		int to = Math.min(from + limit, merged.size());
		List<NxDistributerGoodsLinshiEntity> pageList = from < merged.size() ? merged.subList(from, to) : new ArrayList<>();
		PageUtils pageUtil = new PageUtils(pageList, total, limit, page);
		return R.ok().put("page", pageUtil);
	}

	private void fillShelfGoods(List<NxDistributerGoodsEntity> list) {
		if (list == null || list.isEmpty()) return;
		for (NxDistributerGoodsEntity g : list) {
			if (g.getNxDistributerGoodsId() != null) {
				Map<String, Object> m = new HashMap<>();
				m.put("disGoodsId", g.getNxDistributerGoodsId());
				List<NxDistributerGoodsShelfGoodsEntity> shelf = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(m);
				if (shelf != null && !shelf.isEmpty()) g.setNxDistributerGoodsShelfGoodsEntities(shelf);
			}
		}
	}

	private void fillShelfGoodsForLinshi(List<NxDistributerGoodsLinshiEntity> list) {
		if (list == null || list.isEmpty()) return;
		for (NxDistributerGoodsLinshiEntity le : list) {
			NxDistributerGoodsEntity g = le.getNxDistributerGoodsEntity();
			if (g != null && g.getNxDistributerGoodsId() != null) {
				Map<String, Object> m = new HashMap<>();
				m.put("disGoodsId", g.getNxDistributerGoodsId());
				List<NxDistributerGoodsShelfGoodsEntity> shelf = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(m);
				if (shelf != null && !shelf.isEmpty()) g.setNxDistributerGoodsShelfGoodsEntities(shelf);
			}
		}
	}

	private String getGoodsName(NxDistributerGoodsLinshiEntity le) {
		if (le.getNxDistributerGoodsEntity() != null) {
			return le.getNxDistributerGoodsEntity().getNxDgGoodsName();
		}
		return le.getNxDgGoodsLsName();
	}

	/**
	 * 为 status=1/2 的 linshi 填充推荐商品 nxGoodsList（与 getDisLinshiGoods 一致）
	 */
	private void fillRecommendGoodsForLinshi(List<NxDistributerGoodsLinshiEntity> list, Integer disId, Integer status) {
		if (list == null || list.isEmpty() || disId == null) return;
		for (NxDistributerGoodsLinshiEntity linshi : list) {
			Integer ls = linshi.getNxDgGoodsLsStatus();
			if ((status == null || status == 1 || status == 2) && (ls == 1 || ls == 2)) {
				String idsStr = linshi.getNxDgRecommendNxGoodsIds();
				if (idsStr == null || idsStr.trim().isEmpty()) continue;
				NxDistributerGoodsEntity goods = linshi.getNxDistributerGoodsEntity();
				if (goods == null) continue;
				List<NxGoodsEntity> nxGoodsList = new ArrayList<>();
				for (String idStr : idsStr.split(",")) {
					try {
						Integer nxGoodsId = Integer.parseInt(idStr.trim());
						NxGoodsEntity ng = nxGoodsService.queryObject(nxGoodsId);
						if (ng != null) {
							Map<String, Object> disMap = new HashMap<>();
							disMap.put("disId", disId);
							disMap.put("nxGoodsId", nxGoodsId);
							List<NxDistributerGoodsEntity> downloadedList = nxDistributerGoodsService.queryDisGoodsByParams(disMap);
							if (downloadedList != null && !downloadedList.isEmpty()) {
								ng.setNxDistributerGoodsEntity(downloadedList.get(0));
							}
							nxGoodsList.add(ng);
						}
					} catch (NumberFormatException ignored) {
					}
				}
				goods.setNxGoodsList(nxGoodsList);
			}
		}
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

	/**
	 * 回退替换：将已替换成功的临时商品（status=-1）改回申请添加新商品状态（status=2）
	 * 用于 disSaveLinshiToNxGoods 替换错了时，让用户可重新选择正确的正式商品
	 * @param linshiId 临时商品申请记录ID（nx_distributer_goods_ls_id），或
	 * @param nxGoodsId 已替换成的正式商品ID（nx_dg_to_nx_dis_goods_id），二选一
	 */
	@RequestMapping(value = "/revertReplaceToLinshi", method = RequestMethod.POST)
	@ResponseBody
	public R revertReplaceToLinshi(@RequestParam(required = false) Integer linshiId,
	                                @RequestParam(required = false) Integer nxGoodsId) {
		if (linshiId == null && nxGoodsId == null) {
			return R.error("参数错误：linshiId 或 nxGoodsId 必填其一");
		}
		NxDistributerGoodsLinshiEntity linshi;
		if (linshiId != null) {
			linshi = nxDistributerGoodsLinshiService.queryObject(linshiId);
		} else {
			linshi = nxDistributerGoodsLinshiService.queryLinshiByToGoodsId(nxGoodsId);
		}
		if (linshi == null) {
			return R.error("未找到对应的临时商品申请记录");
		}
		if (linshi.getNxDgGoodsLsStatus() != null && linshi.getNxDgGoodsLsStatus() != -1) {
			return R.error("当前记录状态不是已替换成功，无法回退。当前状态: " + linshi.getNxDgGoodsLsStatus());
		}
		nxDistributerGoodsLinshiService.updateRevertToLinshi(linshi.getNxDistributerGoodsLsId());
		linshi.setNxDgGoodsLsStatus(2);
		linshi.setNxDgToNxDisGoodsId(null);
		logger.info("[revertReplaceToLinshi] 回退成功，linshiId={}, 已改为申请添加新商品状态", linshi.getNxDistributerGoodsLsId());
		return R.ok().put("data", linshi);
	}

}
