package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 02-18 14:22
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDepInventoryGoodsMonthTotalEntity;
import com.nongxinle.service.GbDepInventoryGoodsMonthTotalService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/gbdepinventorygoodsmonthtotal")
public class GbDepInventoryGoodsMonthTotalController {
	@Autowired
	private GbDepInventoryGoodsMonthTotalService gbDepInventoryGoodsMonthTotalService;

	
}
