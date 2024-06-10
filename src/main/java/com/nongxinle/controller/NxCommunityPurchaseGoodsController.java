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
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.DateUtils.formatWhatTime;


@RestController
@RequestMapping("api/nxcommunitypurchasegoods")
public class NxCommunityPurchaseGoodsController {
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


	
}
