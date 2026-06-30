package com.nongxinle.community.customer.controller;

/**
 * 
 *
 * @author lpy
 * @date 11-30 21:47
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.utils.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.community.customer.service.NxCommunityUserService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxcommunityuser")
public class NxCommunityUserController {
	@Autowired
	private NxCommunityUserService nxCommunityUserService;

	private static final String KEY = "C5HBZ-KEIW2-JXXUJ-COLGS-FQO47-WWFAK";



	@RequestMapping(value = "/getDeliverRoute", method = RequestMethod.POST)
	@ResponseBody
	public R getDeliverRoute(Integer userId, String fromLat, String fromLng) {
		return R.error(-1, "餐饮配送路线功能已下线");
	}

	@RequestMapping(value = "/updateCommUserWithFile", method = RequestMethod.POST)
	@ResponseBody
	public R updateCommUserWithFile(@RequestParam("file") MultipartFile file,
								  @RequestParam("id") Integer id,
								  HttpSession session) {
		//1,上传图片
		String newUploadName = "userImage";
		String realPath = UploadFile.upload(session, newUploadName, file);
		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;

		NxCommunityUserEntity communityCardEntity = nxCommunityUserService.queryObject(id);
		String oldPath = communityCardEntity.getNxCouWxAvartraUrl();
		if (oldPath != null && !oldPath.trim().isEmpty()) {
			String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
			File file1 = new File(oldAbsolutePath);
			if (file1.exists()) {
				file1.delete();
			}
		}

		communityCardEntity.setNxCouWxAvartraUrl(filePath);
		communityCardEntity.setNxCouUrlIsChange(1);
		nxCommunityUserService.update(communityCardEntity);

		return R.ok();
	}



	@RequestMapping(value = "/updateCommunityUser", method = RequestMethod.POST)
	@ResponseBody
	public R updateCommunityUser (@RequestBody NxCommunityUserEntity customerUser) {
		nxCommunityUserService.update(customerUser);
		return R.ok().put("data", customerUser);
	}


	@RequestMapping(value = "/deleteCommunityUser/{id}")
	@ResponseBody
	public R deleteCommunityUser(@PathVariable Integer id) {
	    nxCommunityUserService.delete(id);
	    return R.ok();
	}



	@RequestMapping(value = "/registerComAdminUser", method = RequestMethod.POST)
	@ResponseBody
	public R registerComAdminUser (@RequestBody NxCommunityUserEntity user ) {
		System.out.println("comusr===" + user);

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

		// 1, 先检查微信号是否以前注册过
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getCommunityAppID() + "&secret=" +
				myAPPIDConfig.getCommunityScreat() + "&js_code=" + user.getNxCouCode() +
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
			user.setNxCouDeviceId("-1");
			user.setNxCouUrlIsChange(0);

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
			user.setNxCouDeviceId("-1");
			user.setNxCouUrlIsChange(0);
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






	/**
	 * 批发商登陆
	 * @param communityUserEntity 批发商
	 * @return 批发商
	 */
	@RequestMapping(value = "/comUserLogin", method = RequestMethod.POST)
	@ResponseBody
	public R comUserLogin (@RequestBody NxCommunityUserEntity communityUserEntity ) {
		System.out.println("afdafaskfjaslkdfj;alsf;alsjf;lasflasjflalogigngignigig");

		try {
			// 检查请求参数
			if (communityUserEntity == null) {
				System.out.println("请求参数为空");
				return R.error(-1, "请求参数不能为空");
			}
			
			String nxCouCode = communityUserEntity.getNxCouCode();
			if (nxCouCode == null || nxCouCode.trim().isEmpty()) {
				System.out.println("微信code为空");
				return R.error(-1, "微信登录code不能为空");
			}
			
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
			String appId = myAPPIDConfig.getCommunityAppID();
			String secret = myAPPIDConfig.getCommunityScreat();
			
			if (appId == null || secret == null) {
				System.out.println("微信配置信息不完整");
				return R.error(-1, "微信配置信息不完整");
			}
			
			String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId + "&secret=" +
					secret + "&js_code=" + nxCouCode + "&grant_type=authorization_code";
			
			System.out.println("requestUrl-====" + url);
			
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
			
			if (str == null || str.trim().isEmpty()) {
				System.out.println("微信API返回空响应");
				return R.error(-1, "微信API返回空响应");
			}
			
			System.out.println("微信API响应: " + str);
			
		// 转成Json对象 获取openid
			JSONObject jsonObject = null;
			try {
				jsonObject = JSONObject.parseObject(str);
			} catch (Exception e) {
				System.out.println("解析微信API响应失败: " + str);
				return R.error(-1, "解析微信API响应失败");
			}
			
			if (jsonObject == null) {
				System.out.println("微信API响应解析后为空");
				return R.error(-1, "微信API响应解析失败");
			}

			// 检查微信API返回的错误
			if (jsonObject.containsKey("errcode")) {
				Integer errcode = jsonObject.getInteger("errcode");
				String errmsg = jsonObject.getString("errmsg");
				System.out.println("微信接口返回错误: errcode=" + errcode + ", errmsg=" + errmsg);
				return R.error(-1, "微信登录失败: " + errmsg + " (错误码: " + errcode + ")");
			}

			// 检查是否有openid
			if (!jsonObject.containsKey("openid") || jsonObject.get("openid") == null) {
				System.out.println("微信接口未返回openid，返回数据: " + jsonObject);
				return R.error(-1, "获取openid失败，请检查微信配置");
			}

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		Map<String, Object> map = new HashMap<>();
		map.put("openId", openid);
//		map.put("roleId", 0);
		System.out.println(map);
		NxCommunityUserEntity nxCommunityUserEntity = nxCommunityUserService.queryComUserInfo(map);

		if(nxCommunityUserEntity != null){
			Integer communityUserId = nxCommunityUserEntity.getNxCommunityUserId();
				if (communityUserId == null) {
					System.out.println("用户ID为空");
					return R.error(-1, "用户ID为空");
				}
				
			Map<String, Object> map1 = new HashMap<>();
			map1.put("userId", communityUserId);
//			map1.put("roleId", 0);
			System.out.println("ammmda11111" + map1);
			NxCommunityUserEntity nxCommunityUserEntity1 = nxCommunityUserService.queryComUserInfo(map1);
				if (nxCommunityUserEntity1 == null) {
					return R.error(-1, "查询用户信息失败");
				}
				if (nxCommunityUserEntity1.getNxCommunityEntity() != null) {
			System.out.println("diididididiidi" + nxCommunityUserEntity1.getNxCommunityEntity().getNxCommunityName());
					NxCommunityEntity communityEntity = nxCommunityUserEntity1.getNxCommunityEntity();
					
					// 打破循环引用，避免JSON序列化错误
					// 1. NxCommunityEntity中包含NxCommunityUserEntity，会造成循环引用
					communityEntity.setNxCommunityUserEntity(null);
					
					// 2. NxECommerceCommunityEntity可能包含循环引用
					if (communityEntity.getNxECommerceCommunityEntity() != null) {
						communityEntity.getNxECommerceCommunityEntity().setNxCommunityEntity(null);
						// NxECommerceEntity也可能包含循环引用
						if (communityEntity.getNxECommerceCommunityEntity().getNxECommerceEntity() != null) {
							communityEntity.getNxECommerceCommunityEntity().getNxECommerceEntity().setNxECommerceCommunityEntity(null);
							communityEntity.getNxECommerceCommunityEntity().getNxECommerceEntity().setNxECommerceCommunityEntities(null);
						}
					}
					
					// 3. NxECommerceEntity可能包含循环引用
					if (communityEntity.getNxECommerceEntity() != null) {
						communityEntity.getNxECommerceEntity().setNxECommerceCommunityEntity(null);
						communityEntity.getNxECommerceEntity().setNxECommerceCommunityEntities(null);
					}
				}
				System.out.println("准备返回用户数据");
			return R.ok().put("data", nxCommunityUserEntity1);
		}else {
			return R.error(-1,"用户不存在");
			}
		} catch (Exception e) {
			System.out.println("========== comUserLogin 发生异常 ==========");
			System.out.println("异常类型: " + e.getClass().getName());
			System.out.println("异常信息: " + e.getMessage());
			e.printStackTrace();
			return R.error(-1, "登录失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
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
