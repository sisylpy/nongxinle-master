package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-02 17:14
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.QyGbDisCorpUserEntity;
import com.nongxinle.service.QyGbDisCorpUserService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/qygbdiscorpuser")
public class QyGbDisCorpUserController {
	@Autowired
	private QyGbDisCorpUserService qyGbDisCorpUserService;
	

	
}
