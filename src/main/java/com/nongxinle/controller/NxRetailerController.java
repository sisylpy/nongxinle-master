package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-22 15:25
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.NxRetailerUserService;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxRetailerService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxretailer")
public class NxRetailerController {
	@Autowired
	private NxRetailerService nxRetailerService;
	@Autowired
	private NxRetailerUserService nxRetailerUserService;



	/**
	 * PURCHASE
	 * 采购员注册
	 * @param
	 * @return 群信息
	 */
	@RequestMapping(value = "/retailerRegister", method = RequestMethod.POST)
	@ResponseBody
	public R retailerRegister (@RequestBody NxRetailerEntity retailerEntity) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String shanghuoAppID = myAPPIDConfig.getShanghuoAppID();
		String shanghuoScreat = myAPPIDConfig.getShanghuoScreat();

		NxRetailerUserEntity nxRetailerUserEntity = retailerEntity.getNxRetailerUserEntity();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + shanghuoAppID + "&secret=" + shanghuoScreat + "&js_code=" + nxRetailerUserEntity.getNxRetuCode()+ "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		NxRetailerUserEntity nxRetailerUserEntity1 = nxRetailerUserService.queryRetailerUserByOpenId(openid);
		if(nxRetailerUserEntity1 == null){
			retailerEntity.getNxRetailerUserEntity().setNxRetuWxOpenId(openid);
			Integer restailerUserId = nxRetailerService.saveNewRestailer(retailerEntity);
			if (restailerUserId != null){
				Map<String, Object> stringObjectMap = nxRetailerService.queryRetailerAndUserInfo(restailerUserId);
				return R.ok().put("data", stringObjectMap);
			}
			return R.error(-1,"注册失败");
		}else {
			return R.error(-1,"此微信号已注册过零售商用户");
		}



	}

}
