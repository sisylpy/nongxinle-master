package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 02-23 21:44
 */

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.GbDepartmentGoodsStockReduceEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import com.nongxinle.service.GbDepartmentGoodsStockRecordService;
import com.nongxinle.service.GbDepartmentGoodsStockReduceService;
import com.nongxinle.service.GbDistributerGoodsService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDepartmentGoodsStockReduceDailyEntity;
import com.nongxinle.service.GbDepartmentGoodsStockReduceDailyService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.DateUtils.getHowManyDaysInPeriod;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartGoodsStockReduceTypeReturn;


@RestController
@RequestMapping("api/gbdepartmentgoodsstockreducedaily")
public class GbDepartmentGoodsStockReduceDailyController {
	@Autowired
	private GbDepartmentGoodsStockReduceDailyService gbDepGoodsStockReduceDailyService;
	@Autowired
	private GbDepartmentGoodsStockReduceService gbDepartmentStockReduceService;
	@Autowired
	private GbDistributerGoodsService distributerGoodsService;







	
}
