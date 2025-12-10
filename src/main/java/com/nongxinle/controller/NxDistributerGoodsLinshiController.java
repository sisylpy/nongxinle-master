package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 08-14 22:10
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nongxinle.entity.NxDistributerGoodsLinshiEntity;
import com.nongxinle.service.NxDistributerGoodsLinshiService;
import com.nongxinle.service.NxDistributerGoodsShelfGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxdistributergoodslinshi")
public class NxDistributerGoodsLinshiController {
	private static final Logger logger = LoggerFactory.getLogger(NxDistributerGoodsLinshiController.class);
	
	@Autowired
	private NxDistributerGoodsLinshiService nxDistributerGoodsLinshiService;
	
	@Autowired
	private NxDistributerGoodsShelfGoodsService nxDistributerGoodsShelfGoodsService;


	@RequestMapping(value = "/disGetLinshiGoodsList", method = RequestMethod.POST)
	@ResponseBody
	public R disGetLinshiGoodsList (Integer disId,  Integer page, Integer limit ) {
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






	
}
