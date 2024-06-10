package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 10-12 19:46
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxdistributersupplier")
public class NxDistributerSupplierController {
	@Autowired
	private NxDistributerSupplierService nxDistributerSupplierService;
	@Autowired
	private NxDistributerPurchaseGoodsService dpgService;
	@Autowired
	private NxDistributerPurchaseBatchService nxPurBatchService;
	@Autowired
	private NxJrdhUserService jrdhUserService;



}
