package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-20 21:43
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.service.GbDepartmentService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbRouteDepEntity;
import com.nongxinle.service.GbRouteDepService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/gbroutedep")
public class GbRouteDepController {

	@Autowired
	private GbRouteDepService gbRouteDepService;
	@Autowired
	private GbDepartmentService gbDepartmentService;





	

	
}
