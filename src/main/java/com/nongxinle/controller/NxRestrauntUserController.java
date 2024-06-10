package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 11-30 15:31
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDepartmentUserEntity;
import com.nongxinle.entity.NxRestrauntEntity;
import com.nongxinle.service.NxRestrauntService;
import com.nongxinle.utils.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxRestrauntUserEntity;
import com.nongxinle.service.NxRestrauntUserService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxrestrauntuser")
public class NxRestrauntUserController {
	@Autowired
	private NxRestrauntUserService nxRestrauntUserService;

	@Autowired
	private NxRestrauntService nxRestrauntService;


	@RequestMapping(value = "/orderUserLogin/{code}")
	@ResponseBody
	public R orderUserLogin(@PathVariable String code) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		System.out.println(code);

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getComResAppID()
				+ "&secret=" + myAPPIDConfig.getComResScreat() + "&js_code=" + code +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		System.out.println(url);
		String str = WeChatUtil.httpRequest(url, "GET", null);
		System.out.println(str);
		System.out.println("9999999999999");
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		System.out.println("restuanlerlogin:::" + openId);
		if(openId != null){
			NxRestrauntUserEntity restaurantUserEntity = nxRestrauntUserService.queryResUserByOpenId(openId);
			System.out.println(restaurantUserEntity);
			System.out.println("====---------");
			if(restaurantUserEntity != null){
				Integer restaurantUserId = restaurantUserEntity.getNxRestrauntUserId();
				Map<String, Object> stringObjectMap = nxRestrauntService.queryResAndUserInfo(restaurantUserId);
				return R.ok().put("data", stringObjectMap);
			}else{
				return R.error(-1,"请进行注册");
			}

		}else {
			return R.error(-1,"请进行注册");
		}
	}

	@RequestMapping(value = "/resManRegister", method = RequestMethod.POST)
	@ResponseBody
	public R resManRegister(@RequestBody NxRestrauntUserEntity userEntity) {
//wxApp
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

//		NxRestrauntUserEntity nxRestrauntUserEntity = res.getNxRestrauntUserEntity();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getComPurchaseAppID() +
				"&secret=" + myAPPIDConfig.getComPurchaseScreat() + "&js_code=" + userEntity.getNxRuCode() + "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);


		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();

		NxRestrauntUserEntity resUserEntities = nxRestrauntUserService.queryResUserByOpenId(openid);
		if(resUserEntities != null){
			return R.error(-1,"请直接登陆");

		}else{
			//添加新用户
			userEntity.setNxRuWxOpenId(openid);
			userEntity.setNxRuJoinDate(formatWhatDay(0));
			nxRestrauntUserService.save(userEntity);
			Integer restrauntUserId = userEntity.getNxRestrauntUserId();
			Map<String, Object> stringObjectMap = nxRestrauntService.queryResAndUserInfo(restrauntUserId);
			return R.ok().put("data",stringObjectMap);
		}
	}
	@ResponseBody
	@RequestMapping("/orderUserSave")
	public R orderUserSave(@RequestBody NxRestrauntUserEntity nxRestrauntUserEntity){


		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String resAppID = myAPPIDConfig.getComResAppID();
		String resScreat = myAPPIDConfig.getComResScreat();

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + resAppID + "&secret=" + resScreat + "&js_code=" + nxRestrauntUserEntity.getNxRuCode()+ "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();

		NxRestrauntUserEntity resUserEntities = nxRestrauntUserService.queryResUserByOpenId(openid);
		if(resUserEntities != null){
			return R.error(-1,"请直接登陆");

		}else{
			//添加新用户
			nxRestrauntUserEntity.setNxRuWxOpenId(openid);
			nxRestrauntUserEntity.setNxRuJoinDate(formatWhatDay(0));
			nxRestrauntUserService.save(nxRestrauntUserEntity);
			Integer restrauntUserId = nxRestrauntUserEntity.getNxRestrauntUserId();
//			Map<String, Object> stringObjectMap = nxDepartmentService.queryGroupAndUserInfo(nxDepartmentUserId);
			Map<String, Object> stringObjectMap = nxRestrauntService.queryResAndUserInfo(restrauntUserId);
			return R.ok().put("data",stringObjectMap);
		}
	}


	@RequestMapping(value = "/deleteResUser/{userid}")
	@ResponseBody
	public R deleteResUser(@PathVariable Integer userid) {
	    nxRestrauntUserService.delete(userid);
	    return R.ok();
	}


	@ResponseBody
	@RequestMapping("/resUserSave")
	public R resUserSave(@RequestBody NxRestrauntUserEntity nxRestrauntUserEntity){


		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String resAppID = myAPPIDConfig.getRestrauntAppID();
		String resScreat = myAPPIDConfig.getRestrauntScreat();

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + resAppID + "&secret=" + resScreat + "&js_code=" + nxRestrauntUserEntity.getNxRuCode()+ "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();

		NxRestrauntUserEntity resUserEntities = nxRestrauntUserService.queryResUserByOpenId(openid);
		if(resUserEntities != null){
			return R.error(-1,"请直接登陆");

		}else{
			//添加新用户
			nxRestrauntUserEntity.setNxRuWxOpenId(openid);
			nxRestrauntUserEntity.setNxRuJoinDate(formatWhatDay(0));
			nxRestrauntUserService.save(nxRestrauntUserEntity);
			Integer restrauntUserId = nxRestrauntUserEntity.getNxRestrauntUserId();
//			Map<String, Object> stringObjectMap = nxDepartmentService.queryGroupAndUserInfo(nxDepartmentUserId);
			Map<String, Object> stringObjectMap = nxRestrauntService.queryResAndUserInfo(restrauntUserId);
			return R.ok().put("data",stringObjectMap);
		}
	}




   @RequestMapping(value = "/getRestrauntUsers/{resId}")
   @ResponseBody
   public R getRestrauntUsers(@PathVariable Integer resId) {
       NxRestrauntEntity userEntities = nxRestrauntUserService.queryAllResUsersByResId(resId);
       return R.ok().put("data", userEntities);
   }

	@RequestMapping(value = "/getRestrauntAndDepUsers/{resId}")
	@ResponseBody
	public R getRestrauntAndDepUsers(@PathVariable Integer resId) {
		NxRestrauntEntity userEntities = nxRestrauntUserService.queryAllRestrauntAndDepUsersByResId(resId);
		return R.ok().put("data", userEntities);
	}
	@RequestMapping(value = "/getChainRestrauntAndDepUsers/{resId}")
	@ResponseBody
	public R getChainRestrauntAndDepUsers(@PathVariable Integer resId) {
		NxRestrauntEntity userEntities = nxRestrauntUserService.queryChainRestrauntAndDepUsersByResId(resId);
		return R.ok().put("data", userEntities);
	}





	@RequestMapping(value = "/getResUserInfo/{userId}")
	@ResponseBody
	public R getResUserInfo(@PathVariable Integer userId) {
//		NxRestrauntUserEntity restaurantUserEntity = nxRestrauntUserService.queryObject(userId);
		Map<String, Object> stringObjectMap = nxRestrauntService.queryResAndUserInfo(userId);
		return R.ok().put("data", stringObjectMap);
	}

	/**
	 * 修改订货用户信息
	 * @param userName 订货用户名称
	 * @param userId 用户id
	 * @return ok
	 */
	@RequestMapping(value = "/updateResUser", method = RequestMethod.POST)
	@ResponseBody
	public R updateResUser (String userName, Integer userId) {
		NxRestrauntUserEntity nxDepartmentUserEntity = nxRestrauntUserService.queryObject(userId);
		nxDepartmentUserEntity.setNxRuWxNickName(userName);
		nxRestrauntUserService.update(nxDepartmentUserEntity);

		Map<String, Object> map = new HashMap<>();

		map.put("userInfo", nxDepartmentUserEntity );
		return R.ok().put("data", map);
	}

	/**
	 * 部门用户修改用户信息
	 * @param file 用户头像
	 * @param userName 用户名
	 * @param userId 用户id
	 * @param session 图片
	 * @return ok
	 */
	@RequestMapping(value = "/updateResUserWithFile", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R updateResUserWithFile(@RequestParam("file") MultipartFile file,
								   @RequestParam("userName") String userName,
								   @RequestParam("userId") Integer userId,
								   HttpSession session) {
		//1,上传图片
		String newUploadName = "uploadImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;
		NxRestrauntUserEntity nxDepartmentUserEntity = nxRestrauntUserService.queryObject(userId);
		nxDepartmentUserEntity.setNxRuWxNickName(userName);
		nxDepartmentUserEntity.setNxRuWxAvartraUrl(filePath);
		nxDepartmentUserEntity.setNxRuUrlChange(1);
		nxRestrauntUserService.update(nxDepartmentUserEntity);

		return R.ok();
	}




	@RequestMapping(value = "/restruanteUserLogin/{code}")
	@ResponseBody
	public R restruanteUserLogin(@PathVariable String code) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		System.out.println(code);

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getRestrauntAppID()
				+ "&secret=" + myAPPIDConfig.getRestrauntScreat() + "&js_code=" + code +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		System.out.println(url);
		String str = WeChatUtil.httpRequest(url, "GET", null);
		System.out.println(str);
		System.out.println("9999999999999");
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		System.out.println("restuanlerlogin:::" + openId);
		if(openId != null){
			NxRestrauntUserEntity restaurantUserEntity = nxRestrauntUserService.queryResUserByOpenId(openId);
			System.out.println(restaurantUserEntity);
			System.out.println("====---------");
			if(restaurantUserEntity != null){
				Integer restaurantUserId = restaurantUserEntity.getNxRestrauntUserId();
				Map<String, Object> stringObjectMap = nxRestrauntService.queryResAndUserInfo(restaurantUserId);
				return R.ok().put("data", stringObjectMap);
			}else{
				return R.error(-1,"请进行注册");
			}

		}else {
			return R.error(-1,"请进行注册");
		}
	}

//
@RequestMapping(value = "/updateResManWithFile", produces = "text/html;charset=UTF-8")
@ResponseBody
public R updateResManWithFile(@RequestParam("file") MultipartFile file,
							  @RequestParam("userName") String userName,
							  @RequestParam("resName") String resName,
							  @RequestParam("userId") Integer userId,
							  @RequestParam("resId") Integer resId,
							  HttpSession session) {
	//1,上传图片
	String newUploadName = "uploadImage";
	String realPath = UploadFile.upload(session, newUploadName, file);

	String filename = file.getOriginalFilename();
	String filePath = newUploadName + "/" + filename;
	NxRestrauntUserEntity nxRestrauntUserEntity = nxRestrauntUserService.queryObject(userId);
	nxRestrauntUserEntity.setNxRuWxNickName(userName);
	nxRestrauntUserEntity.setNxRuWxAvartraUrl(filePath);
	nxRestrauntUserEntity.setNxRuUrlChange(1);
	nxRestrauntUserService.update(nxRestrauntUserEntity);

	NxRestrauntEntity nxRestrauntEntity = nxRestrauntService.queryObject(resId);
	nxRestrauntEntity.setNxRestrauntName(resName);
	nxRestrauntService.update(nxRestrauntEntity);


	return R.ok();

}
	@RequestMapping(value = "/updateResMan", method = RequestMethod.POST)
	@ResponseBody
	public R updateResMan (String userName, String resName, Integer userId,  Integer resId) {
		NxRestrauntUserEntity nxRestrauntUserEntity = nxRestrauntUserService.queryObject(userId);
		nxRestrauntUserEntity.setNxRuWxNickName(userName);
		nxRestrauntUserService.update(nxRestrauntUserEntity);

		NxRestrauntEntity nxDepartmentEntity = nxRestrauntService.queryObject(resId);
		nxDepartmentEntity.setNxRestrauntName(resName);
		nxRestrauntService.update(nxDepartmentEntity);
		return R.ok();
	}
//
	@RequestMapping(value = "/chainResManLogin/{code}")
	@ResponseBody
	public R chainResManLogin(@PathVariable String code) {
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		System.out.println(code);

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getComPurchaseAppID()
				+ "&secret=" + myAPPIDConfig.getComPurchaseScreat() + "&js_code=" + code +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);

		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		if(openId != null){
			NxRestrauntUserEntity restaurantUserEntity = nxRestrauntUserService.queryResUserByOpenId(openId);

			if(restaurantUserEntity != null){
				Integer restaurantUserId = restaurantUserEntity.getNxRestrauntUserId();
				Map<String, Object> stringObjectMap = nxRestrauntService.queryResAndUserInfo(restaurantUserId);
				return R.ok().put("data", stringObjectMap);
			}else{
				return R.error(-1,"请进行注册");
			}

		}else {
			return R.error(-1,"请进行注册");
		}
	}



	

	
}
