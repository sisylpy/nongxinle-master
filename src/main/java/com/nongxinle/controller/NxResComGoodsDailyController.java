package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 03-28 17:06
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.entity.NxRestrauntComGoodsEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxResComGoodsDailyEntity;
import com.nongxinle.service.NxResComGoodsDailyService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxrescomgoodsdaily")
public class NxResComGoodsDailyController {
	@Autowired
	private NxResComGoodsDailyService nxResComGoodsDailyService;

	@RequestMapping(value = "/resGetStatisticsDaily", method = RequestMethod.POST)
	@ResponseBody
	public R resGetStatisticsDaily(Integer resId, String date) {
		Map<String, Object> map = new HashMap<>();
		map.put("resFatherId", resId);
		map.put("date", date);
		System.out.println(map + "kkkmap===");
		List<NxCommunityGoodsEntity> dailyEntity = nxResComGoodsDailyService.queryResDailyStatistics(map);
		System.out.println(dailyEntity + "made");
		return R.ok().put("data", dailyEntity);
	}





	
}
