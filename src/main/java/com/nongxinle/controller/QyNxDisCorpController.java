package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 07-15 15:52
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.QyNxDisCorpEntity;
import com.nongxinle.service.QyNxDisCorpService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("qynxdiscorp")
public class QyNxDisCorpController {
	@Autowired
	private QyNxDisCorpService qyNxDisCorpService;
	




	
}
