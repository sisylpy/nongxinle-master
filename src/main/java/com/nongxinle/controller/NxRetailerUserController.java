package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-22 15:35
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.NxRestrauntUserEntity;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxRetailerUserEntity;
import com.nongxinle.service.NxRetailerUserService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxretaileruser")
public class NxRetailerUserController {
	@Autowired
	private NxRetailerUserService nxRetailerUserService;


	@RequestMapping(value = "/getRetailerUsers/{retId}")
	@ResponseBody
	public R getRetailerUsers(@PathVariable Integer retId) {
	    List<NxRetailerUserEntity> userEntities =  nxRetailerUserService.queryRetUsersById(retId);
	    return R.ok().put("data", userEntities);
	}


	@RequestMapping(value = "/retailerUserRegister", method = RequestMethod.POST)
	@ResponseBody
	public R retailerUserRegister (@RequestBody NxRetailerUserEntity userEntity ) {
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

//		NxRestrauntUserEntity nxRestrauntUserEntity = res.getNxRestrauntUserEntity();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShanghuoAppID() +
				"&secret=" + myAPPIDConfig.getShanghuoScreat() + "&js_code=" + userEntity.getNxRetuCode() + "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);


		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();

		NxRetailerUserEntity retUserEntities = nxRetailerUserService.queryRetailerUserByOpenId(openid);
		if(retUserEntities != null){
			return R.error(-1,"请直接登陆");

		}else{
			//添加新用户
			userEntity.setNxRetuWxOpenId(openid);
			userEntity.setNxRetuJoinDate(formatWhatDay(0));
			nxRetailerUserService.save(userEntity);
			Integer restrauntUserId = userEntity.getNxRetailerUserId();
			Map<String, Object> stringObjectMap = nxRetailerUserService.queryRetailerAndUserInfo(restrauntUserId);
			return R.ok().put("data",stringObjectMap);
		}
	}


	@RequestMapping(value = "/retailerUserLogin/{code}")
	@ResponseBody
	public R retailerUserLogin(@PathVariable String code) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		System.out.println(code);

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShanghuoAppID()
				+ "&secret=" + myAPPIDConfig.getShanghuoScreat() + "&js_code=" + code +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);

		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		System.out.println("restuanlerlogin:::" + openId);
		if(openId != null){
			NxRetailerUserEntity retailerUserEntity = nxRetailerUserService.queryRetailerUserByOpenId(openId);
			if(retailerUserEntity != null){
				Integer retailerUserId = retailerUserEntity.getNxRetailerUserId();
				Map<String, Object> stringObjectMap = nxRetailerUserService.queryRetailerAndUserInfo(retailerUserId);
				return R.ok().put("data", stringObjectMap);
			}else{
				return R.error(-1,"请进行注册");
			}

		}else {
			return R.error(-1,"请进行注册");
		}
	}
	
}
