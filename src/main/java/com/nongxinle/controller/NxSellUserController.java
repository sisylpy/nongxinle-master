package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-29 10:21
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxselluser")
public class NxSellUserController {
	@Autowired
	private NxSellUserService nxSellUserService;
	@Autowired
	private NxRetailerPurchaseBatchService nxRetPurchaseBatchService;
	@Autowired
	private NxDistributerPurchaseBatchService nxDisPurchaseBatchService;
	@Autowired
	private NxCommunityPurchaseBatchService nxComPurchaseBatchService;



	@RequestMapping(value = "/disGetSellers/{disId}")
	@ResponseBody
	public R disGetSellers(@PathVariable Integer disId) {
	    List<NxSellUserEntity> sellUserEntities =  nxSellUserService.queryDisSellerUsersByParams(disId);
	    return R.ok().put("data", sellUserEntities);
	}


	@RequestMapping(value = "/getSupplierUsers/{supplierId}")
	@ResponseBody
	public R getSupplierUsers(@PathVariable Integer supplierId) {
		List<NxSellUserEntity>  nxSellUserEntities = nxSellUserService.querySupplierUserBySupplierId(supplierId);
	    return R.ok().put("data", nxSellUserEntities);
	}



	@RequestMapping(value = "/sellerLogin/{code}")
	@ResponseBody
	public R sellerLogin(@PathVariable String code) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

		String maimaiAppID = myAPPIDConfig.getJinriDinghuoAppId();
		String maimaiScreat = myAPPIDConfig.getJinriDinghuoScreat();

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
				maimaiScreat + "&js_code=" + code +
				"&grant_type=authorization_code";

		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		System.out.println(str);

		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		if (openId != null) {
			Map<String, Object> map = new HashMap<>();
			map.put("openId", openId);
//			map.put("disId", disId);
			NxSellUserEntity nxSellUserEntity = nxSellUserService.queryDisSellerUserByParmas(map);
			if (nxSellUserEntity != null) {
				Integer sellUserId = nxSellUserEntity.getNxSellUserId();
				NxSellUserEntity nxSellUserEntity1 = nxSellUserService.queryObject(sellUserId);
				return R.ok().put("data", nxSellUserEntity1);
			} else {
				return R.error(-1, "请进行注册");
			}
		} else {
			return R.error(-1, "请进行注册");
		}
	}

	@RequestMapping(value = "/disSellerLogin", method = RequestMethod.POST)
	@ResponseBody
	public R disSellerLogin(String code, Integer disId) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

		String maimaiAppID = myAPPIDConfig.getJinriDinghuoAppId();
		String maimaiScreat = myAPPIDConfig.getJinriDinghuoScreat();

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
				maimaiScreat + "&js_code=" + code +
				"&grant_type=authorization_code";

		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		System.out.println(str);

		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		if (openId != null) {
			Map<String, Object> map = new HashMap<>();
			map.put("openId", openId);
			map.put("disId", disId);
			NxSellUserEntity nxSellUserEntity = nxSellUserService.queryDisSellerUserByParmas(map);
			if (nxSellUserEntity != null) {
				Integer sellUserId = nxSellUserEntity.getNxSellUserId();
				NxSellUserEntity nxSellUserEntity1 = nxSellUserService.queryObject(sellUserId);
				return R.ok().put("data", nxSellUserEntity1);
			} else {
				return R.error(-1, "请进行注册");
			}
		} else {
			return R.error(-1, "请进行注册");
		}
	}


	@RequestMapping(value = "/gbSupplerUserRegister", method = RequestMethod.POST)
	@ResponseBody
	public R gbSupplerUserRegister (@RequestBody NxSellUserEntity sellUserEntity  ) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getJinriDinghuoAppId() +
				"&secret=" + myAPPIDConfig.getJinriDinghuoScreat() + "&js_code=" + sellUserEntity.getNxSuCode() + "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);

		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		System.out.println(jsonObject);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		NxSellUserEntity nxSellUserEntities = nxSellUserService.querySellerUserByOpenId(openid);
		if(nxSellUserEntities != null){
			return R.error(-1, "用户已存在，请直接登陆");
		}else {
			//添加新用户
			sellUserEntity.setNxSuWxOpenId(openid);
			sellUserEntity.setNxSuJoinDate(formatWhatDay(0));
			nxSellUserService.save(sellUserEntity);
		}

	    return R.ok();
	}


	@RequestMapping(value = "/registerSeller", method = RequestMethod.POST)
	@ResponseBody
	public R registerSeller(@RequestBody NxSellUserEntity sellUserEntity) {
//wxApp
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getJinriDinghuoAppId() +
				"&secret=" + myAPPIDConfig.getJinriDinghuoScreat() + "&js_code=" + sellUserEntity.getNxSuCode() + "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);

		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		System.out.println(jsonObject);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();

		//添加新用户
		sellUserEntity.setNxSuWxOpenId(openid);
		sellUserEntity.setNxSuJoinDate(formatWhatDay(0));
		nxSellUserService.save(sellUserEntity);

		if(sellUserEntity.getNxSellerType().equals("ret")){
			//改batch selluser
			Integer sellUserId = sellUserEntity.getNxSellUserId();
			Integer sellerSellBatchId = sellUserEntity.getNxSellerSellBatchId();
			Map<String, Object> map = new HashMap<>();
			map.put("batchId", sellerSellBatchId);
			NxRetailerPurchaseBatchEntity nxRetailerPurchaseBatchEntity = nxRetPurchaseBatchService.queryObject(sellerSellBatchId);
			nxRetailerPurchaseBatchEntity.setNxRpbSellUserId(sellUserId);
			nxRetPurchaseBatchService.update(nxRetailerPurchaseBatchEntity);

			NxRetailerPurchaseBatchEntity purchaseBatchEntities = nxRetPurchaseBatchService.queryRetPurBatchDetail(map);

			return R.ok().put("data", purchaseBatchEntities);
		}

//		if(sellUserEntity.getNxSellerType().equals("gbDis")){
//			//改batch selluser
//			Integer sellUserId = sellUserEntity.getNxSellUserId();
//			Integer sellerSellBatchId = sellUserEntity.getNxSellerSellBatchId();
//			Map<String, Object> map = new HashMap<>();
//			map.put("batchId", sellerSellBatchId);
//			GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDisPurchaseBatchService.queryObject(sellerSellBatchId);
//			gbDistributerPurchaseBatchEntity.setGbDpbSellUserId(sellUserId);
//			gbDisPurchaseBatchService.update(gbDistributerPurchaseBatchEntity);
//
//			GbDistributerPurchaseBatchEntity distributerPurchaseBatchEntity = gbDisPurchaseBatchService.queryBatchWithOrders(sellerSellBatchId);
//
//			return R.ok().put("data", distributerPurchaseBatchEntity);
//		}

		if(sellUserEntity.getNxSellerType().equals("dis")){
			//改batch selluser
			Integer sellUserId = sellUserEntity.getNxSellUserId();
			Integer sellerSellBatchId = sellUserEntity.getNxSellerSellBatchId();
			Map<String, Object> map = new HashMap<>();
			map.put("batchId", sellerSellBatchId);
			NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDisPurchaseBatchService.queryObject(sellerSellBatchId);
			nxDistributerPurchaseBatchEntity.setNxDpbSellUserId(sellUserId);
			nxDisPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);
			NxDistributerPurchaseBatchEntity distributerPurchaseBatchEntity = nxDisPurchaseBatchService.queryBatchWithOrders(sellerSellBatchId);
			return R.ok().put("data", distributerPurchaseBatchEntity);
		}
		if(sellUserEntity.getNxSellerType().equals("com")){
			//改batch selluser
			Integer sellUserId = sellUserEntity.getNxSellUserId();
			Integer sellerSellBatchId = sellUserEntity.getNxSellerSellBatchId();
			Map<String, Object> map = new HashMap<>();
			map.put("batchId", sellerSellBatchId);
			NxCommunityPurchaseBatchEntity nxCommunityPurchaseBatchEntity = nxComPurchaseBatchService.queryObject(sellerSellBatchId);
			nxCommunityPurchaseBatchEntity.setNxCpbSellUserId(sellUserId);
			nxComPurchaseBatchService.update(nxCommunityPurchaseBatchEntity);
			NxCommunityPurchaseBatchEntity communityPurchaseBatchEntity = nxComPurchaseBatchService.queryBatchDetail(map);

			return R.ok().put("data", communityPurchaseBatchEntity);
		}

		return R.error(-1,"chonxixncaozuo");

	}

	
}
