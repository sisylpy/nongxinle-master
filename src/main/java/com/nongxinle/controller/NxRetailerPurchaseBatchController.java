package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-22 20:41
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.NxRetailerPurchaseGoodsService;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxRetailerPurchaseBatchService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatTime;


@RestController
@RequestMapping("api/nxretailerpurchasebatch")
public class NxRetailerPurchaseBatchController {
	@Autowired
	private NxRetailerPurchaseBatchService nxRetPurchaseBatchService;
	@Autowired
	private NxRetailerPurchaseGoodsService nxRetPurchaseGoodsService;
	
	
	@RequestMapping(value = "/buyUserGetOpenId/{code}")
	@ResponseBody
	public R buyUserGetOpenId(@PathVariable String code ) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		System.out.println(code);

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
		System.out.println(jsonObject);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		System.out.println("restuanlerlogin:::" + openId);

		if (openId != null) {

				return R.ok().put("data", openId);

		} else {
			return R.error(-1, "请进行注册");
		}
	    
	}

	@RequestMapping(value = "/saveRetPurBatchBuyUserId", method = RequestMethod.POST)
	@ResponseBody
	public R saveRetPurBatchBuyUserId (Integer batchId, Integer buyUserId) {
		NxRetailerPurchaseBatchEntity nxRetailerPurchaseBatchEntity = nxRetPurchaseBatchService.queryObject(batchId);
		nxRetailerPurchaseBatchEntity.setNxRpbBuyUserId(buyUserId);
		nxRetPurchaseBatchService.update(nxRetailerPurchaseBatchEntity);
		return R.ok();
	}

	@RequestMapping(value = "/updateBatchDetail", method = RequestMethod.POST)
	@ResponseBody
	public R updateBatchDetail (@RequestBody NxRetailerPurchaseBatchEntity batch) {
	    nxRetPurchaseBatchService.update(batch);
	    return R.ok();
	}


	@RequestMapping(value = "/sellUserReadRetBatch", method = RequestMethod.POST)
	@ResponseBody
	public R sellUserReadRetBatch (@RequestBody NxRetailerPurchaseBatchEntity batch) {
		batch.setNxRpbStatus(1);
	    nxRetPurchaseBatchService.update(batch);
		Integer nxRetailerPurchaseBatchId = batch.getNxRetailerPurchaseBatchId();
		Map<String, Object> map = new HashMap<>();
		map.put("batchId", nxRetailerPurchaseBatchId);
		NxRetailerPurchaseBatchEntity nxRetailerPurchaseBatchEntity = nxRetPurchaseBatchService.queryRetPurBatchDetail(map);
		return R.ok().put("data", nxRetailerPurchaseBatchEntity);
	}



	@RequestMapping(value = "/copySucessRetPurBatch/{batchId}")
	@ResponseBody
	public R copySucessRetPurBatch(@PathVariable Integer batchId) {
		NxRetailerPurchaseBatchEntity nxRetBatchEntity = nxRetPurchaseBatchService.queryObject(batchId);
		nxRetBatchEntity.setNxRpbStatus(1);
		nxRetBatchEntity.setNxRpbType(2);
		nxRetPurchaseBatchService.update(nxRetBatchEntity);
	    return R.ok();
	}

	@RequestMapping(value = "/shareSucessRetPurBatch/{batchId}")
	@ResponseBody
	public R shareSucessRetPurBatch(@PathVariable Integer batchId) {
		NxRetailerPurchaseBatchEntity nxRetBatchEntity = nxRetPurchaseBatchService.queryObject(batchId);
		nxRetBatchEntity.setNxRpbStatus(1);
		nxRetBatchEntity.setNxRpbType(1);
		nxRetPurchaseBatchService.update(nxRetBatchEntity);

		return R.ok();
	}

	@RequestMapping(value = "/delteRetPurBatch/{batchid}")
	@ResponseBody
	public R delteRetPurBatch(@PathVariable Integer batchid) {
		Map<String, Object> map = new HashMap<>();
		map.put("batchId", batchid);
		NxRetailerPurchaseBatchEntity nxRetailerPurchaseBatchEntity = nxRetPurchaseBatchService.queryRetPurBatchDetail(map);
		List<NxRetailerPurchaseGoodsEntity> nxRetPurchaseGoodsEntityList = nxRetailerPurchaseBatchEntity.getNxRetPurchaseGoodsEntityList();
		System.out.println(nxRetPurchaseGoodsEntityList.size() + "siziiziziziz");
		for (NxRetailerPurchaseGoodsEntity purGoods : nxRetPurchaseGoodsEntityList) {
			System.out.println(purGoods.getNxRpgBatchId() + " ---------");
			purGoods.setNxRpgBatchId(null);
			purGoods.setNxRpgStatus(0);
			System.out.println(purGoods.getNxRpgBatchId() + "======");
			nxRetPurchaseGoodsService.update(purGoods);
		}

		nxRetPurchaseBatchService.delete(batchid);
	    return R.ok();
	}




	@RequestMapping(value = "/fillCostRetPurchaseGoodsBatch")
	@ResponseBody
	public R fillCostRetPurchaseGoodsBatch(@RequestBody NxRetailerPurchaseBatchEntity batchEntity) {

		List<NxRetailerPurchaseGoodsEntity> nxRPBEntities = batchEntity.getNxRetPurchaseGoodsEntityList();
		for (NxRetailerPurchaseGoodsEntity purGoods : nxRPBEntities) {
			purGoods.setNxRpgStatus(2);
			nxRetPurchaseGoodsService.update(purGoods);
		}
		batchEntity.setNxRpbStatus(2);
		nxRetPurchaseBatchService.update(batchEntity);

		return R.ok();
	}





	@RequestMapping(value = "/getRetPurchaseGoodsBatch/{batchId}")
	@ResponseBody
	public R getRetPurchaseGoodsBatch(@PathVariable Integer batchId) {
		Map<String, Object> map = new HashMap<>();
		map.put("batchId", batchId);
		NxRetailerPurchaseBatchEntity purchaseBatchEntities = nxRetPurchaseBatchService.queryRetPurBatchDetail(map);
		if(purchaseBatchEntities != null){
			return R.ok().put("data", purchaseBatchEntities);
		}else{
			return R.error(-1, "没有订单");
		}
	}


	@RequestMapping(value = "/finishRetPurchaseBatchGoods", method = RequestMethod.POST)
	@ResponseBody
	public R finishRetPurchaseBatchGoods (@RequestBody NxRetailerPurchaseBatchEntity batch) {
		List<NxRetailerPurchaseGoodsEntity> nxRetPurchaseGoodsEntityList = batch.getNxRetPurchaseGoodsEntityList();
		for (NxRetailerPurchaseGoodsEntity purGoods : nxRetPurchaseGoodsEntityList) {
			purGoods.setNxRpgStatus(3);
			purGoods.setNxRpgPurchaseDate(formatWhatDate(0));
			nxRetPurchaseGoodsService.update(purGoods);
		}
		batch.setNxRpbStatus(4);
		nxRetPurchaseBatchService.update(batch);

	    return R.ok();
	}



	@RequestMapping(value = "/retGetBuyingGoods/{redId}")
	@ResponseBody
	public R retGetBuyingGoods(@PathVariable Integer redId) {
		Map<String, Object> map = new HashMap<>();
		map.put("retId", redId);
		map.put("status", 5);
		List<NxRetailerPurchaseBatchEntity> purchaseBatchEntities = nxRetPurchaseBatchService.queryRetPurBatchByParams(map);

		return R.ok().put("data", purchaseBatchEntities);
	}



	/**
	 * 分享进货商品
	 * @param
	 * @return ok
	 */
	@RequestMapping(value = "/shareRetPurchaseGoodsStatus", method = RequestMethod.POST)
	@ResponseBody
	public R shareRetPurchaseGoodsStatus(@RequestBody NxRetailerPurchaseBatchEntity batchEntity) {

		String s = purchaseRetGoodsBatch(batchEntity, 3);
		return R.ok().put("data", s);
	}

	/**
	 * 复制进货商品
	 * @return ok
	 */
	@RequestMapping(value = "/copyRetPruchaseGoodsStatus", method = RequestMethod.POST)
	@ResponseBody
	public R copyRetPruchaseGoodsStatus(@RequestBody NxRetailerPurchaseBatchEntity batchEntity) {
		String s = purchaseRetGoodsBatch(batchEntity, 1);
		return R.ok().put("data", s);
	}

	private String purchaseRetGoodsBatch(NxRetailerPurchaseBatchEntity batchEntity, Integer type) {
		batchEntity.setNxRpbType(type);
		batchEntity.setNxRpbDate(formatWhatDate(0));
		batchEntity.setNxRpbHour(formatWhatHour(0));
		batchEntity.setNxRpbMinute(formatWhatMinute(0));
		batchEntity.setNxRpbTime(formatWhatTime(0));
		batchEntity.setNxRpbStatus(0);
		nxRetPurchaseBatchService.save(batchEntity);
		for (NxRetailerPurchaseGoodsEntity retPurGoods : batchEntity.getNxRetPurchaseGoodsEntityList()) {
			retPurGoods.setNxRpgBatchId(batchEntity.getNxRetailerPurchaseBatchId());
			retPurGoods.setNxRpgStatus(1);
			retPurGoods.setNxRpgTime(formatWhatDayTime(0));
			System.out.println(retPurGoods.getNxRpgBatchId());
			nxRetPurchaseGoodsService.update(retPurGoods);

		}
		return batchEntity.getNxRetailerPurchaseBatchId().toString();

	}


	

	
}
