package com.nongxinle.community.customer.controller;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-04 19:11:55
 */

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.NxCustomerEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.community.promotion.service.NxCustomerReferralRewardService;
import com.nongxinle.community.customer.service.NxCustomerService;
import com.nongxinle.community.customer.service.NxCustomerUserService;
import com.nongxinle.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.afterWhatDay;
import static com.nongxinle.utils.DateUtils.getHowManyDaysInPeriod;


@RestController
@RequestMapping("api/nxcustomeruser")
public class NxCustomerUserController {
	@Autowired
	private NxCustomerUserService nxCustomerUserService;

	@Autowired
	private NxCustomerService nxCustomerService;
	@Autowired
	private NxCustomerReferralRewardService nxCustomerReferralRewardService;


	@RequestMapping(value = "/updateStaff", method = RequestMethod.POST)
	@ResponseBody
	public R updateStaff (Integer commId, Integer userId) {
		System.out.println("comcidididd" + commId);
		NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(userId);
		Integer nxCuCustomerId = userEntity.getNxCuCustomerId();
		NxCustomerEntity nxCustomerEntity = nxCustomerService.queryObject(nxCuCustomerId);
		nxCustomerEntity.setNxCustomerType(99);
		nxCustomerService.update(nxCustomerEntity);

		return R.ok();
	}


	@RequestMapping(value = "/updateStaff/4pqhDTY6LK.txt")
	@ResponseBody
	public String updateStaff( ) {
		return "ae662f0ace3d80f5eac9f1bcce8b71b4";
	}

	/**
	 * 地推拉新：扫普通链接二维码打开小程序 — 微信域名校验文件
	 * 配置前缀 https://.../api/nxcustomeruser/promote/
	 */
	@RequestMapping(value = "/promote/4pqhDTY6LK.txt")
	@ResponseBody
	public String promoteQrVerify() {
		return "ae662f0ace3d80f5eac9f1bcce8b71b4";
	}


	@RequestMapping(value = "/getServiceUsers/{id}")
	@ResponseBody
	public R getServiceUsers(@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("commId", id);
		map.put("type", 1);
		List<NxCustomerUserEntity> userEntities = nxCustomerUserService.queryCustomerByParams(map);

		return R.ok().put("data", userEntities);
	}


	@RequestMapping(value = "/getCustomerCommerceEveryDay", method = RequestMethod.POST)
	@ResponseBody
	public R getCustomerCommerceEveryDay (Integer commerceId, String startDate, String stopDate) {

		Map<String, Object> mapR = new HashMap<>();
		List<Map<String, Object>> itemList = new ArrayList<>();
		List<String> dateList = new ArrayList<>();
		List<String> totalList = new ArrayList<>();
		Integer howManyDaysInPeriod = 0;
		if (!startDate.equals(stopDate)) {
			howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
		}
		if (howManyDaysInPeriod > 0) {

			for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
				// dateList
				String whichDay = "";
				if (i == 0) {
					whichDay = startDate;
				} else {
					whichDay = afterWhatDay(startDate, i);
				}
				Map<String, Object> map = new HashMap<>();
				map.put("date", whichDay);
				map.put("commerceId", commerceId);
				String substring = whichDay.substring(8, 10);
				dateList.add(substring);

				Integer integer = nxCustomerUserService.queryCommerceCustomerUserCount(map);
//
				totalList.add(integer.toString());
				Map<String, Object> mapItem = new HashMap<>();
				mapItem.put("day", whichDay);
				mapItem.put("value", integer);
				itemList.add(mapItem);
				mapR.put("date", dateList);
				mapR.put("list", totalList);
				mapR.put("arr", itemList);

			}

		}
		return R.ok().put("data", mapR);

	}
	@RequestMapping(value = "/getDayCustomer", method = RequestMethod.POST)
	@ResponseBody
	public R getDayCustomer(Integer commId, String date) {

		Map<String, Object> map = new HashMap<>();
//		map.put("commId", commId);
		map.put("date", date);
		System.out.println("dateeee" + map);
		List<NxCustomerUserEntity> userEntities = nxCustomerUserService.queryCustomerByParams(map);

		return R.ok().put("data", userEntities);
	}




	@RequestMapping(value = "/getCustomerEveryDay", method = RequestMethod.POST)
	@ResponseBody
	private Map<String, Object> getCustomerEveryDay(String startDate, String stopDate, Integer commId) {

		System.out.println("getfrisheeieieidydyydydydyydydyydydydyy");
		Map<String, Object> mapR = new HashMap<>();
		List<Map<String, Object>> itemList = new ArrayList<>();
		List<String> dateList = new ArrayList<>();
		List<String> totalList = new ArrayList<>();
		Integer howManyDaysInPeriod = 0;
		if (!startDate.equals(stopDate)) {
			howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
		}
		if (howManyDaysInPeriod > 0) {

			for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
				// dateList
				String whichDay = "";
				if (i == 0) {
					whichDay = startDate;
				} else {
					whichDay = afterWhatDay(startDate, i);
				}
				Map<String, Object> map = new HashMap<>();
				map.put("date", whichDay);
//				map.put("commId", commId);
				String substring = whichDay.substring(8, 10);
				dateList.add(substring);

//				String dailyFresh = "0";
				Integer integer = nxCustomerUserService.queryCustomerUserCount(map);
//				if (integer > 0) {
////					double subtotal = nxCustomerUserService.query(map);
////					dailyFresh = new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//
//				}
				totalList.add(integer.toString());
				Map<String, Object> mapItem = new HashMap<>();
				mapItem.put("day", whichDay);
				mapItem.put("value", integer);
				itemList.add(mapItem);
				mapR.put("date", dateList);
				mapR.put("list", totalList);
				mapR.put("arr", itemList);

			}

		}
		return R.ok().put("data", mapR);

	}



	@RequestMapping(value = "/commSearchCustomer", method = RequestMethod.POST)
	@ResponseBody
	public R commSearchCustomer (Integer commId, String phone) {

		Map<String, Object> map = new HashMap<>();
		map.put("commId", commId);
		map.put("phone", phone);
		System.out.println("searcusut" + map);
		List<NxCustomerUserEntity> userEntities =  nxCustomerUserService.queryCustomerByParams(map);
	    return R.ok().put("data", userEntities);
	}



	@RequestMapping(value = "/updateCustomerUser", method = RequestMethod.POST)
	@ResponseBody
	public R updateCustomerUser (@RequestBody NxCustomerUserEntity user) {
	    nxCustomerUserService.update(user);
	    return R.ok();
	}

	@RequestMapping(value = "/updateCustomerUserWithFile", method = RequestMethod.POST)
	@ResponseBody
	public R updateCustomerUserWithFile(@RequestParam("file") MultipartFile file,
									@RequestParam("userId") Integer userId,
									HttpSession session) {
		//1,上传图片
		String newUploadName = "userImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;

		NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(userId);
		String oldPath = userEntity.getNxCuWxAvatarUrl();
		if (oldPath != null && !oldPath.trim().isEmpty()) {
			String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
			File file1 = new File(oldAbsolutePath);
			if (file1.exists() && !oldPath.equals("userImage/myUrl.png")) {
				file1.delete();
			}
		}


		userEntity.setNxCuWxAvatarUrl(filePath);
		nxCustomerUserService.update(userEntity);
		return R.ok();
	}



	@RequestMapping(value = "/deleteCustomerUser/{id}")
	@ResponseBody
	public R deleteCustomerUser(@PathVariable Integer id) {
		NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(id);
		Integer nxCuCustomerId = userEntity.getNxCuCustomerId();
		nxCustomerService.delete(nxCuCustomerId);
		nxCustomerUserService.delete(id);
		return R.ok();
	}





	@RequestMapping(value = "/customerUserLogin/{code}")
	@ResponseBody
	public R customerUserLogin(@PathVariable String code) {

		System.out.println("customerUserLogincodee" + code);
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String liancaiKufangAppId = myAPPIDConfig.getQingqingxiangAppId();
		String liancaiKufangScreat = myAPPIDConfig.getQingqingxiangScreat();

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiKufangAppId + "&secret=" +
				liancaiKufangScreat + "&js_code=" + code +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		if (openId != null) {
			System.out.println("ageopddid" + openId);
			NxCustomerUserEntity userEntity = nxCustomerUserService.queryUserByOpenId(openId);
			if (userEntity != null) {
			Map<String, Object> stringObjectMap = nxCustomerUserService.queryCustomerUserInfo(userEntity.getNxCuUserId());
				nxCustomerReferralRewardService.attachLoginExtras(stringObjectMap, userEntity.getNxCuUserId(),
						userEntity.getNxCuCommunityId());
				return R.ok().put("data", stringObjectMap);
			} else {
				return R.error(-1, openId);
			}

		} else {
			return R.error(-1, openId);
		}
	}



	@RequestMapping(value = "/customerUserLoginMix", method = RequestMethod.POST)
	@ResponseBody
	public R customerUserLoginMix(String code, Integer commerceId) {

		System.out.println("customerUserLogincodee" + code);
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String liancaiKufangAppId = myAPPIDConfig.getQingqingxiangAppId();
		String liancaiKufangScreat = myAPPIDConfig.getQingqingxiangScreat();

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiKufangAppId + "&secret=" +
				liancaiKufangScreat + "&js_code=" + code +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		if (openId != null) {
			Map<String, Object> mapU = new HashMap<>();
			mapU.put("openId", openId);
			mapU.put("commerceId", commerceId);
			NxCustomerUserEntity userEntity = nxCustomerUserService.queryUserByOpenIdAndCommerceId(mapU);
			if (userEntity != null) {
				Map<String, Object> stringObjectMap = nxCustomerUserService.queryCustomerUserInfo(userEntity.getNxCuUserId());
				nxCustomerReferralRewardService.attachLoginExtras(stringObjectMap, userEntity.getNxCuUserId(),
						userEntity.getNxCuCommunityId());
				return R.ok().put("data", stringObjectMap);
			} else {
				return R.error(-1, openId);
			}

		} else {
			return R.error(-1, openId);
		}
	}

	@RequestMapping(value = "/4pqhDTY6LK.txt")
	@ResponseBody
	public String nxQingqingxiang( ) {
		return "ae662f0ace3d80f5eac9f1bcce8b71b4";
	}



	
}
