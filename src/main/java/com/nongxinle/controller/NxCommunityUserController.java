package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 11-30 21:47
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.ParseObject;
import com.nongxinle.utils.WeChatUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxCommunityUserService;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxcommunityuser")
public class NxCommunityUserController {
	@Autowired
	private NxCommunityUserService nxCommunityUserService;
	private static final String KEY = "C5HBZ-KEIW2-JXXUJ-COLGS-FQO47-WWFAK";


	@RequestMapping(value = "/registerComAdminUser", method = RequestMethod.POST)
	@ResponseBody
	public R registerComAdminUser (@RequestBody NxCommunityUserEntity user ) {
		System.out.println("comusr===" + user);

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

		// 1, 先检查微信号是否以前注册过
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShixianLiliAppId() + "&secret=" +
				myAPPIDConfig.getShixianLiliScreat() + "&js_code=" + user.getNxCouCode() +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		Map<String, Object> map = new HashMap<>();
		map.put("openId", openid);
		map.put("roleId", 0);
		NxCommunityUserEntity communityUserEntity = nxCommunityUserService.queryComUserByOpenId(map);
		//2，如果注册过，则返回提示。
		if(communityUserEntity != null){
			return R.error(-1,"微信号已注册!");
		}else {

			user.setNxCouWxOpenId(openid);
			nxCommunityUserService.save(user);

			//3..3 返回用户id
			Integer nxCommunityUserId = user.getNxCommunityUserId();
			Map<String, Object> map1 = new HashMap<>();
			map1.put("userId", nxCommunityUserId);
			map1.put("roleId", 0);
			NxCommunityUserEntity nxCommunityUserEntity1 = nxCommunityUserService.queryComUserInfo(map1);

			return R.ok().put("data", nxCommunityUserEntity1);

		}

	}

	@RequestMapping(value = "/getComUsers/{comId}")
	@ResponseBody
	public R getComUsers(@PathVariable Integer comId) {

		List<NxCommunityUserEntity> userEntities = nxCommunityUserService.getAdmainUserByComId(comId);
		System.out.println( "user-----ssss" + userEntities );
		return R.ok().put("data", userEntities);
	}


	/**
	 * driver员工扫描
	 * 微信小程序扫描二维码校验文件
	 * @return 校验内容
	 */
	@RequestMapping(value = "/pcT8xhlNNF.txt")
	@ResponseBody
	public String driverUserRegist( ) {
		return "82e336d5278050591525a671ae9c050c";
	}


	@RequestMapping(value = "/comUserDriverSave", method = RequestMethod.POST)
	@ResponseBody
	public R comUserDriverSave (@RequestBody NxCommunityUserEntity user) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiziDriverAppID() + "&secret=" +
				myAPPIDConfig.getLiziDriverScreat() + "&js_code=" + user.getNxCouCode() +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();

		Map<String, Object> map1 = new HashMap<>();
		map1.put("openId", openId);
		map1.put("roleId", user.getNxCouRoleId());
		NxCommunityUserEntity nxCommunityUserEntity = nxCommunityUserService.queryComUserByOpenId(map1);
		if(nxCommunityUserEntity != null){
			return R.error(-1,"请直接登陆");
		}else{
			//添加新用户
			user.setNxCouWxOpenId(openId);
			nxCommunityUserService.save(user);
			Integer communityUserId = user.getNxCommunityUserId();
			Map<String, Object> map2 = new HashMap<>();
			map2.put("userId", communityUserId);
			map2.put("roleId", 5 );
			NxCommunityUserEntity nxCommunityUserEntity1 = nxCommunityUserService.queryComUserInfo(map2);
			return R.ok().put("data",nxCommunityUserEntity1);
		}
	}

	@RequestMapping(value = "/driverUserLogin", method = RequestMethod.POST)
	@ResponseBody
	public R driverLogin (@RequestBody NxCommunityUserEntity communityUserEntity ) {
		System.out.println(communityUserEntity);

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiziDriverAppID() + "&secret=" +
				myAPPIDConfig.getLiziDriverScreat() + "&js_code=" + communityUserEntity.getNxCouCode() +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		Map<String, Object> map = new HashMap<>();
		map.put("openId", openid);
		map.put("roleId", 5);
		System.out.println(map);
		NxCommunityUserEntity nxCommunityUserEntity = nxCommunityUserService.queryComUserByOpenId(map);

		if(nxCommunityUserEntity != null){
			Integer communityUserId = nxCommunityUserEntity.getNxCommunityUserId();
			Map<String, Object> map1 = new HashMap<>();
			map1.put("userId", communityUserId);
			map1.put("roleId", 5 );
			NxCommunityUserEntity nxCommunityUserEntity1 = nxCommunityUserService.queryComUserInfo(map1);

			System.out.println(nxCommunityUserEntity1);
			System.out.println("logingngigign");
			return R.ok().put("data", nxCommunityUserEntity1);
		}else {
			return R.error(-1,"用户不存在");
		}
	}


	@RequestMapping(value = "/getDeliverRoute", method = RequestMethod.POST)
	@ResponseBody
	public R getDeliverRoute(Integer userId, String fromLat, String fromLng) {
		System.out.println(userId);

		Map<String, Object> map4 = new HashMap<>();
		map4.put("userId", userId);
		List<NxRestrauntEntity> restrauntEntities = nxCommunityUserService.queryDeliveryRestrauntsByDriverId(map4);
		if(restrauntEntities.size() > 0){

		//获取出发点坐标
		StringBuilder stringBuilder = new StringBuilder();
		for (NxRestrauntEntity restraunt : restrauntEntities) {
			String nxRestrauntLat = restraunt.getNxRestrauntLat();
			String nxRestrauntLng = restraunt.getNxRestrauntLng();
			String item  = nxRestrauntLat + "," + nxRestrauntLng;
			stringBuilder.append(item + ";");
		}
		String substring = stringBuilder.substring(0, stringBuilder.length() - 1);
		String from = fromLat + "," + fromLng;
		String urlString = "http://apis.map.qq.com/ws/distance/v1/optimal_order?mode=driving&from="
				            + from + "&to=" + substring + "&key=" + KEY;
		// 发送请求，返回Json字符串
		String result = "";
		try {
			URL url = new URL(urlString);
			System.out.println(url);
			System.out.println("----");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			// 腾讯地图使用GET
			conn.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			// 获取地址解析结果
			System.out.println(in);
			while ((line = in.readLine()) != null) {
				result += line + "\n";
			}
			in.close();
		} catch (Exception e) {
			e.getMessage();
		}


//		// 转JSON格式
		JSONObject jsonObject = JSONObject.parseObject(result);
		String optimal_order = (String)jsonObject.getString("result");
			System.out.println(optimal_order);
			System.out.println("optimal_orderoptimal_order");

		//获取排序
		String order = JSONObject.parseObject(optimal_order).getString("optimal_order");
		System.out.println(order);
		String substring2 = order.substring(0);
		String substring3 = substring2.substring(1, substring2.length() - 1);
		String[] split = substring3.split(",");

		List<NxRestrauntEntity> treeSet = new ArrayList<>();

		String elements = JSONObject.parseObject(optimal_order).getString("elements");
		List<NxRestrauntEntity> list = new ArrayList<>();
		list = JSONObject.parseArray(elements, NxRestrauntEntity.class);

		System.out.println(list);
		System.out.println("list");

		for(int i =0; i < split.length; i++){
			Integer integer = Integer.valueOf(split[i]);

			NxRestrauntEntity nxRestrauntEntity = restrauntEntities.get(integer - 1);
			NxRestrauntEntity listEnitity = list.get(i);
			String distance = listEnitity.getDistance();
			String duration = listEnitity.getDuration();
			nxRestrauntEntity.setDistance(distance);
			nxRestrauntEntity.setDuration(duration);
			treeSet.add(nxRestrauntEntity);
		}

	    return R.ok().put("data", treeSet);
		}else{
	    	return R.error(-1,"没有订单");
		}
	}


	@RequestMapping(value = "/getComWorkingStaff/{comId}")
	@ResponseBody
	public R getComWorkingStaff(@PathVariable Integer comId) {
		List<Map<String, Object>> returnData = new ArrayList<>();
		//siji
		Map<String, Object> map = new HashMap<>();
		map.put("roleId", 5);
		map.put("comId", comId);
//		map.put("equalStatus", 1);
		List<NxCommunityUserEntity> userEntities = nxCommunityUserService.queryCommunityRoleUsers(map);

		Map<String, Object> mapDriver = new HashMap<>();
		mapDriver.put("roleId", 5 );
		mapDriver.put("arr",  userEntities);
		returnData.add(mapDriver);
		return R.ok().put("data", returnData);
	}

	@RequestMapping(value = "/getCommunityRoleUsers", method = RequestMethod.POST)
	@ResponseBody
	public R getCommunityRoleUsers (Integer roleId, Integer comId) {
		Map<String, Object> map = new HashMap<>();
		map.put("roleId", roleId);
		map.put("comId", comId);
	    List<NxCommunityUserEntity> userEntities = nxCommunityUserService.queryCommunityRoleUsers(map);
	    return R.ok().put("data", userEntities);
	}

	/**
	 * 批发商登陆
	 * @param communityUserEntity 批发商
	 * @return 批发商
	 */
	@RequestMapping(value = "/comUserLogin", method = RequestMethod.POST)
	@ResponseBody
	public R comLogin (@RequestBody NxCommunityUserEntity communityUserEntity ) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getCommunityAppID() + "&secret=" +
				myAPPIDConfig.getCommunityScreat() + "&js_code=" + communityUserEntity.getNxCouCode() +
				"&grant_type=authorization_code";
//		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShixianLiliAppId() + "&secret=" +
//				myAPPIDConfig.getShixianLiliScreat() + "&js_code=" + communityUserEntity.getNxCouCode() +
//				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		Map<String, Object> map = new HashMap<>();
		map.put("openId", openid);
		map.put("roleId", 0);
		System.out.println(map);
		NxCommunityUserEntity nxCommunityUserEntity = nxCommunityUserService.queryComUserByOpenId(map);

		if(nxCommunityUserEntity != null){
			Integer communityUserId = nxCommunityUserEntity.getNxCommunityUserId();
			Map<String, Object> map1 = new HashMap<>();
			map1.put("userId", communityUserId);
			map1.put("roleId", 0);
			NxCommunityUserEntity nxCommunityUserEntity1 = nxCommunityUserService.queryComUserInfo(map1);


			return R.ok().put("data", nxCommunityUserEntity1);
		}else {
			return R.error(-1,"用户不存在");
		}
	}



   @RequestMapping(value = "/comUserLoginAndroid/{phone}")
   @ResponseBody
   public R comUserLoginAndroid(@PathVariable String phone) {
	   System.out.println(phone + "=====");
	   NxCommunityUserEntity userEntity = nxCommunityUserService.queryUserByPhone(phone);
	   if (userEntity != null){
		   return R.ok().put("data", userEntity);
	   }else{
		   return R.error(-1, "手机号码错误");

	   }
   }
	
}
