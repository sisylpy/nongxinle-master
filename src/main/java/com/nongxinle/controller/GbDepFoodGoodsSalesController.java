package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 03-27 21:55
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDepFoodSalesEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDepFoodGoodsSalesEntity;
import com.nongxinle.service.GbDepFoodGoodsSalesService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("gbdepfoodgoodssales")
public class GbDepFoodGoodsSalesController {
	@Autowired
	private GbDepFoodGoodsSalesService gbDepFoodGoodsSalesService;
	


	
}
