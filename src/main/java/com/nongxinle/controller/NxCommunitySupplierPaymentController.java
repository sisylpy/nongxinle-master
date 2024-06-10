package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 10-15 18:45
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDistributerPurchaseBatchEntity;
import com.nongxinle.entity.NxCommunityPurchaseBatchEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunitySupplierPaymentEntity;
import com.nongxinle.service.NxCommunitySupplierPaymentService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.getLastTwoMonth;


@RestController
@RequestMapping("api/nxcommunitysupplierpayment")
public class NxCommunitySupplierPaymentController {
	@Autowired
	private NxCommunitySupplierPaymentService nxCommunitySupplierPaymentService;


	
}
