package com.nongxinle.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nongxinle.entity.SysUserEntity;
import com.nongxinle.utils.ShiroUtils;

/**
 * Controller公共组件
 * 
 */
public abstract class AbstractController {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	protected SysUserEntity getUser() {

		return ShiroUtils.getUserEntity();
	}

	protected Long getUserId() {

		Long userId = getUser().getUserId();
		System.out.println("getUserId=====" + userId);
		return userId;
	}
}
