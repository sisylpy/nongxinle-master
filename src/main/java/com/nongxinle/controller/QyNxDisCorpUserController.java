package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 07-16 12:25
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.QyNxDisCropUserService;


@RestController
@RequestMapping("qynxdiscropuser")
public class QyNxDisCorpUserController {
	@Autowired
	private QyNxDisCropUserService qyNxDisCropUserService;
	



	
}
