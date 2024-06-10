package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 10-10 10:29
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.GbDistributerPurchaseBatchService;
import com.nongxinle.service.GbDistributerSupplierService;
import com.nongxinle.utils.*;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.GbDistributerSupplierUserService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.GbTypeUtils.*;


@RestController
@RequestMapping("api/gbdistributersupplieruser")
public class GbDistributerSupplierUserController {
	@Autowired
	private GbDistributerSupplierUserService gbDisSupplierUserService;
	@Autowired
	private GbDistributerPurchaseBatchService gbDisPurchaseBatchService;
	@Autowired
	private GbDistributerSupplierService gbDistributerSupplierService;


	/**
	 * 获取指定供货商用户
	 * @param returnSupplierId 定供货商用户id
	 * @return 定供货商用户
	 */
	@RequestMapping(value = "/getDepartmentAppointSupplierUser/{returnSupplierId}")
	@ResponseBody
	public R getDepartmentAppointSupplierUser(@PathVariable Integer returnSupplierId) {

		List<GbDistributerSupplierUserEntity> supplierEntities = gbDisSupplierUserService.querySupplierUsersBySupplierId(returnSupplierId);
		return R.ok().put("data", supplierEntities);
	}

	@RequestMapping(value = "/appointSupplierUserSave", method = RequestMethod.POST)
	@ResponseBody
	public R appointSupplierUserSave (@RequestBody GbDistributerSupplierUserEntity supplierUser) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String liancaiCaigouAppId = myAPPIDConfig.getLiancaiCaigouAppId();
		String liancaiCaigouScreat = myAPPIDConfig.getLiancaiCaigouScreat();

		String code = supplierUser.getGbDsuCode();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiCaigouAppId + "&secret=" +
				liancaiCaigouScreat + "&js_code=" + code +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		System.out.println("strrr" + str);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		Map<String, Object> map = new HashMap<>();
		map.put("openId", openId);
		GbDistributerSupplierUserEntity depUserEntity = gbDisSupplierUserService.querySupplierUserByParams(map);
		if (depUserEntity != null) {
			if(depUserEntity.getGbDsuSupplierId() != null){
				return R.error(-1, "请直接登陆");

			}else{
				depUserEntity.setGbDsuSupplierId(supplierUser.getGbDsuSupplierId());
				gbDisSupplierUserService.update(depUserEntity);
				Map<String, Object> mapAdmin = new HashMap<>();
				mapAdmin.put("openId", openId);
				GbDistributerSupplierUserEntity userEntity = gbDisSupplierUserService.querySupplierUserByParams(mapAdmin);
				return R.ok().put("data", userEntity);
			}

		} else {
			//添加新用户
			supplierUser.setGbDsuWxOpenId(openId);
			supplierUser.setGbDsuJoinDate(formatWhatDay(0));
			supplierUser.setGbDsuPrintDeviceId("-1");
			supplierUser.setGbDsuPrintBillDeviceId("-1");
			supplierUser.setGbDsuAdmin(0);
			gbDisSupplierUserService.save(supplierUser);

			//update supplier
			GbDistributerSupplierEntity gbDistributerSupplierEntity = gbDistributerSupplierService.queryObject(supplierUser.getGbDsuSupplierId());
			gbDistributerSupplierEntity.setGbDsSupplierUserId(supplierUser.getGbDistributerSupplierUserId());
			gbDistributerSupplierEntity.setGbDsPurUserId(supplierUser.getGbDsInviteUserId());
			gbDistributerSupplierService.update(gbDistributerSupplierEntity);

			Map<String, Object> mapAdmin = new HashMap<>();
			mapAdmin.put("openId", openId);
			GbDistributerSupplierUserEntity userEntity = gbDisSupplierUserService.querySupplierUserByParams(mapAdmin);

			return R.ok().put("data", userEntity);

		}
	}


	@RequestMapping(value = "/appointSupplierUserLogin/{code}")
	@ResponseBody
	public R appointSupplierUserLogin(@PathVariable String code) {
		System.out.println("apapapapapappapaapapapbbbbbbbbb");
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String liancaiCaigouAppId = myAPPIDConfig.getLiancaiCaigouAppId();
		String liancaiCaigouScreat = myAPPIDConfig.getLiancaiCaigouScreat();


		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiCaigouAppId + "&secret=" +
				liancaiCaigouScreat + "&js_code=" + code +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		if (openId != null) {
			Map<String, Object> mapAdmin = new HashMap<>();
			mapAdmin.put("openId", openId);
			mapAdmin.put("supplierId", 1);
			GbDistributerSupplierUserEntity supplierUser = gbDisSupplierUserService.querySupplierUserByParams(mapAdmin);
			System.out.println("nmdfna,fdas,fdafdaa");

			if (supplierUser != null) {
				System.out.println("okzheliiiididdidiidi");
				return R.ok().put("data", supplierUser);
			} else {
				return R.error(-1, "没有邀请");
			}
		} else {
			return R.error(-1, "没有邀请");
		}
	}



	@RequestMapping(value = "/getDepartmentSupplierUser/{returnSupplierId}")
	@ResponseBody
	public R getDepartmentSupplierUser(@PathVariable Integer returnSupplierId) {

		List<GbDistributerSupplierUserEntity> supplierEntities = gbDisSupplierUserService.querySupplierUsersBySupplierId(returnSupplierId);
		return R.ok().put("data", supplierEntities);
	}
	
	@RequestMapping(value = "/supplierUserSaveWithFile",  produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R supplierUserSaveWithFile (@RequestParam("file") MultipartFile file,
									   @RequestParam("userName") String userName,
									   @RequestParam("batchId") Integer batchId,
									   @RequestParam("code") String code,
									   @RequestParam("disId") Integer disId,
									   @RequestParam("depFatherId") Integer depFatherId,
									   @RequestParam("depId") Integer depId,
									   HttpSession session) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String appId = myAPPIDConfig.getLiancaiCaigouAppId();
		String secret = myAPPIDConfig.getLiancaiCaigouScreat();

//		String code = supplierUser.getGbDsuCode();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId + "&secret=" +
				secret + "&js_code=" + code +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);

		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		System.out.println(jsonObject);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		Map<String, Object> map = new HashMap<>();
		map.put("openId", openId);
		GbDistributerSupplierUserEntity depUserEntities = gbDisSupplierUserService.querySupplierUserByParams(map);
		if (depUserEntities != null) {
			return R.error(-1, "请直接登陆");
		} else {
			GbDistributerSupplierUserEntity supplierUser = new GbDistributerSupplierUserEntity();
			
			//添加新用户
			//1,上传图片
			String newUploadName = "uploadImage";
			String realPath = UploadFile.upload(session, newUploadName, file);

			String filename = file.getOriginalFilename();
			String filePath = newUploadName + "/" + filename;
			supplierUser.setGbDsuWxNickName(userName);
			supplierUser.setGbDsuWxAvartraUrl(filePath);
			supplierUser.setGbDsuUrlChange(1);
			supplierUser.setGbDsuWxOpenId(openId);
			supplierUser.setGbDsuJoinDate(formatWhatDay(0));
			supplierUser.setGbDsuPrintDeviceId("-1");
			supplierUser.setGbDsuPrintBillDeviceId("-1");
			supplierUser.setGbDsuDistributerId(disId);
			supplierUser.setGbDsuDepartmentId(depId);
			supplierUser.setGbDsuDepartmentFatherId(depFatherId);
			supplierUser.setGbDsuAdmin(0);
			gbDisSupplierUserService.save(supplierUser);

			Integer supplierUserId = supplierUser.getGbDistributerSupplierUserId();
			GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDisPurchaseBatchService.queryObject(batchId);
			gbDistributerPurchaseBatchEntity.setGbDpbSupplierUserId(supplierUserId);
			gbDisPurchaseBatchService.update(gbDistributerPurchaseBatchEntity);
//			GbDistributerPurchaseBatchEntity distributerPurchaseBatchEntity = gbDisPurchaseBatchService.queryBatchWithOrders(batchId);
//			return R.ok().put("data", distributerPurchaseBatchEntity);

			return R.ok();
		}
	}
	@RequestMapping(value = "/supplierUserSave", method = RequestMethod.POST)
	@ResponseBody
	public R supplierUserSave (@RequestBody GbDistributerSupplierUserEntity supplierUser) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String appId = myAPPIDConfig.getLiancaiCaigouAppId();
		String secret = myAPPIDConfig.getLiancaiCaigouScreat();

		String code = supplierUser.getGbDsuCode();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId + "&secret=" +
				secret + "&js_code=" + code +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);

		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		System.out.println(jsonObject);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		Map<String, Object> map = new HashMap<>();
		map.put("openId", openId);
		GbDistributerSupplierUserEntity depUserEntities = gbDisSupplierUserService.querySupplierUserByParams(map);
		if (depUserEntities != null) {
			return R.error(-1, "请直接登陆");
		} else {

			//添加新用户
			supplierUser.setGbDsuWxOpenId(openId);
			supplierUser.setGbDsuJoinDate(formatWhatDay(0));
			supplierUser.setGbDsuPrintDeviceId("-1");
			supplierUser.setGbDsuPrintBillDeviceId("-1");
			GbDistributerPurchaseBatchEntity batchEntity = gbDisPurchaseBatchService.queryObject(supplierUser.getUserRegisterBatchId());
			supplierUser.setGbDsuDistributerId(batchEntity.getGbDpbDistributerId());
			supplierUser.setGbDsuDepartmentId(batchEntity.getGbDpbPurDepartmentId());
			supplierUser.setGbDsuDepartmentFatherId(batchEntity.getGbDpbPurDepartmentId());
			supplierUser.setGbDsuAdmin(0);
			gbDisSupplierUserService.save(supplierUser);

			Integer supplierUserId = supplierUser.getGbDistributerSupplierUserId();
			Integer sellerSellBatchId = supplierUser.getUserRegisterBatchId();
			GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDisPurchaseBatchService.queryObject(sellerSellBatchId);
			gbDistributerPurchaseBatchEntity.setGbDpbSupplierUserId(supplierUserId);
			gbDisPurchaseBatchService.update(gbDistributerPurchaseBatchEntity);
			GbDistributerPurchaseBatchEntity distributerPurchaseBatchEntity = gbDisPurchaseBatchService.queryBatchWithOrders(sellerSellBatchId);
			return R.ok().put("data", distributerPurchaseBatchEntity);

		}
	}

	@RequestMapping(value = "/supplierUserLogin/{code}")
	@ResponseBody
	public R supplierUserLogin(@PathVariable String code) {
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String liancaiCaigouAppId = myAPPIDConfig.getLiancaiCaigouAppId();
		String liancaiCaigouScreat = myAPPIDConfig.getLiancaiCaigouScreat();

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiCaigouAppId + "&secret=" +
				liancaiCaigouScreat + "&js_code=" + code +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		if (openId != null) {
			Map<String, Object> mapAdmin = new HashMap<>();
			mapAdmin.put("openId", openId);
			System.out.println("mapdppdpdpd" + mapAdmin);
			GbDistributerSupplierUserEntity supplierUser = gbDisSupplierUserService.querySupplierUserByParams(mapAdmin);
			if (supplierUser != null) {
				return R.ok().put("data", supplierUser);
			} else {
				return R.error(-1, "没有邀请");
			}

		} else {
			return R.error(-1, "没有邀请");
		}
	}









}
