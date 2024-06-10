package com.nongxinle.controller;

/**
 * @author lpy
 * @date 02-18 14:22
 */

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import com.nongxinle.service.GbDepartmentGoodsStockReduceDailyService;
import com.nongxinle.service.GbDistributerGoodsService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDepInventoryGoodsDailyTotalEntity;
import com.nongxinle.service.GbDepInventoryGoodsDailyTotalService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;


@RestController
@RequestMapping("api/gbdepinventorygoodsdailytotal")
public class GbDepInventoryGoodsDailyTotalController {
    @Autowired
    private GbDepInventoryGoodsDailyTotalService gbDepGoodsDailyTotalService;
    @Autowired
    private GbDistributerGoodsService distributerGoodsService;
    @Autowired
    private GbDepartmentGoodsStockReduceDailyService gbDepGoodsStockReduceDailyService;


}