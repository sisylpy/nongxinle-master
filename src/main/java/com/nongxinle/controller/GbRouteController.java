package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.service.GbDepartmentService;
import com.nongxinle.service.GbRouteDepService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbRouteEntity;
import com.nongxinle.service.GbRouteService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/gbroute")
public class GbRouteController {
	@Autowired
	private GbRouteService gbRouteService;
	@Autowired
	private GbDepartmentService gbDepartmentService;



	@RequestMapping(value = "/getDisRoutes/{disId}")
	@ResponseBody
	public R getDisRoutes(@PathVariable Integer disId) {
		System.out.println("dididi" + disId);
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
	    List<GbRouteEntity> routeEntities = gbRouteService.getDisRoutesByDisId(map);
	    return R.ok().put("data", routeEntities);
	}


	@RequestMapping(value = "/saveNewLine", method = RequestMethod.POST)
	@ResponseBody
	public R saveNewLine (@RequestBody GbRouteEntity  routeEntity ) {
		System.out.println(routeEntity + "enenne");


		gbRouteService.save(routeEntity);

		Integer gbRouteId = routeEntity.getGbRouteId();

		List<GbDepartmentEntity> departmentEntities = routeEntity.getDepartmentEntities();
		for (GbDepartmentEntity dep : departmentEntities) {
			dep.setGbDepartmentRouteId(gbRouteId);
			gbDepartmentService.update(dep);
		}

		return R.ok();
	}




	
}
