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

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxResComGoodsMonthEntity;
import com.nongxinle.service.NxResComGoodsMonthService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("nxrescomgoodsmonth")
public class NxResComGoodsMonthController {
	@Autowired
	private NxResComGoodsMonthService nxResComGoodsMonthService;
	

	
}
