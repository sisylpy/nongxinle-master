package com.nongxinle.controller;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-09 18:47
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.NxDistributerGoodsShelfGoodsService;
import com.nongxinle.service.NxDistributerService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxDistributerGoodsShelfService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("api/nxdistributergoodsshelf")
public class NxDistributerGoodsShelfController {
	private static final Logger logger = LoggerFactory.getLogger(NxDistributerGoodsShelfController.class);
	@Autowired
	private NxDistributerGoodsShelfService nxDisGoodsShelfService;
	@Autowired
	private NxDistributerGoodsShelfGoodsService nxDisGoodsShelfGoodsService;
	@Autowired
	private NxDistributerService nxDistributerService;
	@Autowired
	private NxDistributerGoodsService dgService;



	@RequestMapping(value = "/disGetShelfListByType", method = RequestMethod.POST)
	@ResponseBody
	public R disGetShelfListByType(Integer disId, Integer shelfGoodsType) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("shelfGoodsType", shelfGoodsType);
		// shelfGoodsType: 99=全部 1=多货架商品 2=有库存 3=临期商品 4=库存不足

		List<NxDistributerGoodsShelfEntity> nxDistributerGoodsShelfEntities = nxDisGoodsShelfService.queryShelfList(map);
		// 有库存/多货架/临期/库存不足 时不查询非货架商品，直接返回0
		int total = (shelfGoodsType != null && shelfGoodsType != 99) ? 0 : dgService.queryDisUnshelfGoodsTotal(map);
		Map<String, Object> mapRe = new HashMap<>();
		mapRe.put("shelfArr", nxDistributerGoodsShelfEntities);
		mapRe.put("unShelfTotalCount", total);

		return R.ok().put("data", mapRe);
	}

	@RequestMapping(value = "/disGetShelfList/{disId}")
	@ResponseBody
	public R disGetShelfList(@PathVariable  Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);

		List<NxDistributerGoodsShelfEntity> nxDistributerGoodsShelfEntities = nxDisGoodsShelfService.queryShelfList(map);
		// 有库存/多货架/临期/库存不足 时不查询非货架商品，直接返回0
		int total = dgService.queryDisUnshelfGoodsTotal(map);
		Map<String, Object> mapRe = new HashMap<>();
		mapRe.put("shelfArr", nxDistributerGoodsShelfEntities);
		mapRe.put("unShelfTotalCount", total);

		return R.ok().put("data", mapRe);
	}


	@RequestMapping(value = "/disGetToPlanPurchaseShelfGoods", method = RequestMethod.POST)
	@ResponseBody
	public R disGetToPlanPurchaseShelfGoods(Integer disId, Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("purStatus", 2);
		map.put("shelfId", shelfId);
		List<NxDistributerFatherGoodsEntity> shelfGoodsEntities = nxDisGoodsShelfService.disGetUnPlanShelfPurchaseApplys(map);

		return R.ok().put("data", shelfGoodsEntities);
	}

	@RequestMapping(value = "/updateShelfName", method = RequestMethod.POST)
	@ResponseBody
	public R updateShelfName (Integer shelfId, String shelfName) {

		NxDistributerGoodsShelfEntity shelfEntity = nxDisGoodsShelfService.queryObject(shelfId);
		shelfEntity.setNxDistributerGoodsShelfName(shelfName);
		nxDisGoodsShelfService.update(shelfEntity);
		return R.ok().put("data", shelfEntity);
	}

	@RequestMapping(value = "/updateShelfSort", method = RequestMethod.POST)
	@ResponseBody
	public R updateShelfSort (@RequestBody List<NxDistributerGoodsShelfEntity> shelfList) {

		for (int i = 0; i < shelfList.size(); i++){
			NxDistributerGoodsShelfEntity shelfEntity = shelfList.get(i);
			shelfEntity.setNxDistributerGoodsShelfSort(i + 1);
			nxDisGoodsShelfService.update(shelfEntity);
		}
		return R.ok();
	}


	@RequestMapping(value = "/getShelfGoods", method = RequestMethod.POST)
	@ResponseBody
	public R getShelfGoods(
			@RequestParam(required = false) Integer shelfId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer limit,
			@RequestParam(required = false) Integer shelfGoodsType,
			@RequestParam(value = "shelfGoodsQuerySort", required = false) Integer shelfGoodsQuerySort) {
		// 设置默认分页参数
		if (page == null || page < 1) {
			page = 1;
		}
		if (limit == null || limit < 1) {
			limit = 15;
		}

		// shelfGoodsQuerySort: 0 默认 1 库存金额降序 2 升序 3 到期剩余天数降序 4 升序（临期优先）；非法或未传按 0
		int sort = (shelfGoodsQuerySort == null || shelfGoodsQuerySort < 0 || shelfGoodsQuerySort > 4) ? 0 : shelfGoodsQuerySort;

		// 计算偏移量
		int offset = (page - 1) * limit;

		// 构建查询参数
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);
		map.put("status", 3);
		map.put("offset", offset);
		map.put("limit", limit);
		map.put("shelfGoodsType", shelfGoodsType);
		map.put("shelfGoodsQuerySort", sort);
		// shelfGoodsType: 99=全部 1=多货架商品 2=有库存 3=无库存 4=库存不足
		
		logger.info("[getShelfGoods] 查询参数: shelfId={}, page={}, limit={}, shelfGoodsType={}, shelfGoodsQuerySort={}", shelfId, page, limit, shelfGoodsType, sort);

		// 查询总数
		int total = nxDisGoodsShelfGoodsService.queryShelfForGoodsCount(map);
		// 查询分页数据
		List<NxDistributerGoodsShelfGoodsEntity> nxDistributerGoodsShelfGoodsEntities = nxDisGoodsShelfGoodsService.queryShelfForGoodsByParams(map);

		if (sort == 3 || sort == 4) {
			logger.info("[getShelfGoods] 临期排序 SQL 方向: sort={}, desc模式(晚到期在前)={}, totalCount={}, 本页条数={}",
					sort, sort == 3, total, nxDistributerGoodsShelfGoodsEntities.size());
			for (int i = 0; i < nxDistributerGoodsShelfGoodsEntities.size(); i++) {
				NxDistributerGoodsShelfGoodsEntity e = nxDistributerGoodsShelfGoodsEntities.get(i);
				Integer rowStockId = e.getShelfGoodsListRowStockId();
				String expiry = "";
				if (e.getNxDisGoodsShelfStockEntities() != null) {
					for (NxDistributerGoodsShelfStockEntity st : e.getNxDisGoodsShelfStockEntities()) {
						if (rowStockId != null && rowStockId.equals(st.getNxDistributerGoodsShelfStockId())) {
							expiry = st.getNxDgssExpiryDate() == null ? "" : st.getNxDgssExpiryDate();
							break;
						}
					}
				}
				logger.info("[getShelfGoods] 临期序#{} shelfGoodsId={} rowStockId={} restDaysUntil={} nxDgssExpiryDate={}",
						i + 1, e.getNxDistributerGoodsShelfGoodsId(), rowStockId, e.getShelfGoodsRestDaysUntil(), expiry);
			}
		}

		// 使用 PageUtils 返回分页结果
		PageUtils pageUtil = new PageUtils(nxDistributerGoodsShelfGoodsEntities, total, limit, page);
		return R.ok().put("page", pageUtil);
	}

	/**
	 * 查询货架商品列表（包含溯源报告信息）
	 * 返回所有货架商品，有溯源报告的库存批次会包含溯源信息（nxDgssTraceReportId不为null），
	 * 没有溯源报告的库存批次的nxDgssTraceReportId为null
	 * 这样前端可以显示所有商品，并且可以识别哪些商品有溯源报告，哪些没有
	 * 同时支持后续通过接口删除或更新溯源报告
	 */
	@RequestMapping(value = "/getShelfGoodsWithTraceReport/{shelfId}")
	@ResponseBody
	public R getShelfGoodsWithTraceReport(@PathVariable Integer shelfId, Integer page, Integer limit) {
		// 设置默认分页参数
		if (page == null || page < 1) {
			page = 1;
		}
		if (limit == null || limit < 1) {
			limit = 15;
		}

		// 计算偏移量
		int offset = (page - 1) * limit;

		// 构建查询参数
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);
		map.put("status", 3);
		map.put("offset", offset);
		map.put("limit", limit);

		logger.info("[getShelfGoodsWithTraceReport] 查询参数: shelfId={}, page={}, limit={}", shelfId, page, limit);

		// 查询总数（返回所有货架商品）
		int total = nxDisGoodsShelfGoodsService.queryShelfForGoodsWithTraceReportCount(map);
		// 查询分页数据（返回所有货架商品，包含溯源报告信息）
		List<NxDistributerGoodsShelfGoodsEntity> nxDistributerGoodsShelfGoodsEntities = nxDisGoodsShelfGoodsService.queryShelfForGoodsWithTraceReportByParams(map);

		// 使用 PageUtils 返回分页结果
		PageUtils pageUtil = new PageUtils(nxDistributerGoodsShelfGoodsEntities, total, limit, page);
		return R.ok().put("page", pageUtil);
	}


	
	@RequestMapping(value = "/disGetShelfs/{disId}")
	@ResponseBody
	public R disGetShelfs(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		List<NxDistributerGoodsShelfEntity> shelfEntities = nxDisGoodsShelfService.queryShelfByParams(map);
	    return R.ok().put("data", shelfEntities);
	}

//
//	@RequestMapping(value = "/disGetShelfsWithDetail/{disId}")
//	@ResponseBody
//	public R disGetShelfsWithDetail(@PathVariable Integer disId) {
//		Map<String, Object> map = new HashMap<>();
//		map.put("disId", disId);
//		List<NxDistributerGoodsShelfEntity> shelfEntities = nxDisGoodsShelfService.queryShelfWithDetailByParams(map);
//		return R.ok().put("data", shelfEntities);
//	}

	@RequestMapping(value = "/updateShelf", method = RequestMethod.POST)
	@ResponseBody
	public R updateShelf (@RequestBody NxDistributerGoodsShelfEntity shelfEntity ) {
//		for (int i = 0; i < shelfList.size(); i++){
//			NxDistributerGoodsShelfEntity shelfEntity = shelfList.get(i);
//			shelfEntity.setNxDistributerGoodsShelfSort(i + 1);
//			nxDisGoodsShelfService.update(shelfEntity);
//		}
		nxDisGoodsShelfService.update(shelfEntity);



		return R.ok();
	}

	@RequestMapping(value = "/saveNewShelf", method = RequestMethod.POST)
	@ResponseBody
	public R saveNewShelf (@RequestBody NxDistributerGoodsShelfEntity shelf) {
		shelf.setGoodsCount(0);
	    nxDisGoodsShelfService.save(shelf);
		Integer nxDistributerGoodsShelfDisId = shelf.getNxDistributerGoodsShelfDisId();
		NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDistributerGoodsShelfDisId);
		nxDistributerEntity.setNxDistributerShelfQuantity(nxDistributerEntity.getNxDistributerShelfQuantity() + 1);
		nxDistributerService.update(nxDistributerEntity);
		return R.ok().put("data", shelf);
	}

	@RequestMapping(value = "/deleteShelf/{shelfId}")
	@ResponseBody
	public R deleteShelf(@PathVariable Integer shelfId) {
		Map<String, Object> map = new HashMap<>();
		map.put("shelfId", shelfId);

		List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsEntities =  nxDisGoodsShelfGoodsService.queryShelfForGoodsByParams(map);
		if(shelfGoodsEntities.size() > 0){
			return R.error(-1,"先删除商品");
		}else{
			NxDistributerGoodsShelfEntity shelfEntity = nxDisGoodsShelfService.queryObject(shelfId);
			Integer nxDistributerGoodsShelfDisId = shelfEntity.getNxDistributerGoodsShelfDisId();
			NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDistributerGoodsShelfDisId);
			nxDistributerEntity.setNxDistributerShelfQuantity(nxDistributerEntity.getNxDistributerShelfQuantity() - 1);
			nxDistributerService.update(nxDistributerEntity);

			nxDisGoodsShelfService.delete(shelfId);

			return R.ok();
		}

	}

	@RequestMapping(value = "/setShelfUser", method = RequestMethod.POST)
	@ResponseBody
	public R setShelfUser(Integer shelfId, Integer userId) {
		NxDistributerGoodsShelfEntity shelfEntity = nxDisGoodsShelfService.queryObject(shelfId);
		shelfEntity.setNxDistributerGoodsShelfUserId(userId);
		nxDisGoodsShelfService.update(shelfEntity);
		return R.ok().put("data", shelfEntity);
	}

	@RequestMapping(value = "/getShelfsByUserId/{userId}")
	@ResponseBody
	public R getShelfsByUserId(@PathVariable Integer userId) {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", userId);
		List<NxDistributerGoodsShelfEntity> shelfEntities = nxDisGoodsShelfService.queryShelfByParams(map);
		return R.ok().put("data", shelfEntities);
	}


	






	
}
