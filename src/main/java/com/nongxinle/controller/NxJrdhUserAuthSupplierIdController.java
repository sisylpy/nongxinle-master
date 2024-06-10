package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 09-11 14:46
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxJrdhUserAuthSupplierIdEntity;
import com.nongxinle.service.NxJrdhUserAuthSupplierIdService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("nxjrdhuserauthsupplierid")
public class NxJrdhUserAuthSupplierIdController {
	@Autowired
	private NxJrdhUserAuthSupplierIdService nxJrdhUserAuthSupplierIdService;
	



	
}
