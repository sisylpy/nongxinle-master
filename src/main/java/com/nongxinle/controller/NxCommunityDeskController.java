package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 04-07 09:33
 */

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.nongxinle.entity.GbDepartmentBillEntity;
import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.entity.NxCommunityOrdersSubEntity;
import com.nongxinle.service.NxCommunityOrdersService;
import com.nongxinle.service.NxCommunityOrdersSubService;
import com.nongxinle.utils.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityDeskEntity;
import com.nongxinle.service.NxCommunityDeskService;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("api/nxcommunitydesk")
public class NxCommunityDeskController {
	@Autowired
	private NxCommunityDeskService nxCommunityDeskService;
	@Autowired
	private NxCommunityOrdersSubService nxCommunityOrdersSubService;
	@Autowired
	private NxCommunityOrdersService nxCommunityOrdersService;


	@ResponseBody
	@RequestMapping(value = "/deskToSettle", method = RequestMethod.POST)
	public R deskToSettle(Integer deskId) {

		Map<String, Object> map = new HashMap<>();
		map.put("deskId", deskId);
		map.put("status", 3);
		System.out.println("subsbsbssutt" + map);
		double nxRbTotal = nxCommunityOrdersSubService.queryHuaxianTotal(map);
		String s2 = String.valueOf(nxRbTotal);
		System.out.println("dfdafkdaksfas" + s2);

		MyWxQingqingxiangPayConfig config = new MyWxQingqingxiangPayConfig();
//		Double aDouble = Double.parseDouble(s2) * 100;
		Double aDouble = Double.parseDouble("0.01") * 100;
		int i = aDouble.intValue();
		String s1 = String.valueOf(i);
		String tradeNo = CommonUtils.generateOutTradeNo();
		SortedMap<String, String> params = new TreeMap<>();
		params.put("appid", config.getAppID());
		params.put("mch_id", config.getMchID());
		params.put("nonce_str", CommonUtils.generateUUID());
		params.put("body", "订单支付");
		params.put("out_trade_no", tradeNo);
		params.put("fee_type", "CNY");
		params.put("total_fee", s1);
		params.put("spbill_create_ip", "101.42.222.149");
		params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxcommunitydesk/notify");
		params.put("trade_type", "NATIVE");
		params.put("product_id", "PROD_001");

		try {
			WXPay wxpay = new WXPay(config);
			Map<String, String> resp = wxpay.unifiedOrder(params);
			System.out.println("微信返回参数：" + resp);

			if ("SUCCESS".equals(resp.get("return_code")) && "SUCCESS".equals(resp.get("result_code"))) {
				String codeUrl = resp.get("code_url");

				Map<String, Object> mapOR = new HashMap<>();
				mapOR.put("deskId", deskId);
				mapOR.put("status", 0 );
				List<NxCommunityOrdersEntity> ordersEntities = nxCommunityOrdersService.queryCustomerOrder(mapOR);
				if(ordersEntities.size() > 0){
					for(NxCommunityOrdersEntity ordersEntity: ordersEntities){
						ordersEntity.setNxCoWxOutTradeNo(tradeNo);
						nxCommunityOrdersService.update(ordersEntity);
					}
				}

				System.out.println("comdmdmddmddmlsls" + codeUrl);
				return R.ok().put("code_url", codeUrl);
			} else {
				return R.error("微信下单失败：" + resp.get("return_msg"));
			}

		} catch (Exception e) {
			e.printStackTrace();
			return R.error("异常：" + e.getMessage());
		}
//		return R.ok();
	}



	@RequestMapping("/notify")
	public String payNotify(HttpServletRequest request) {
		try {
			// 读取回调数据
			InputStream inStream = request.getInputStream();
			ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = inStream.read(buffer)) != -1) {
				outSteam.write(buffer, 0, len);
			}
			String notifyXml = new String(outSteam.toByteArray(), "utf-8");

			// 解析XML
			Map<String, String> notifyData = WXPayUtil.xmlToMap(notifyXml);

			// 验证签名
			MyWxQingqingxiangPayConfig config = new MyWxQingqingxiangPayConfig();

			if (WXPayUtil.isSignatureValid(notifyData, config.getKey())) {
				// 处理业务逻辑
				String orderNo = notifyData.get("out_trade_no");
				String transactionId = notifyData.get("transaction_id");

				// TODO: 更新订单状态为已支付

				Map<String, Object> mapP = new HashMap<>();
				mapP.put("trade_no",orderNo);
				List<NxCommunityOrdersEntity> ordersEntities = nxCommunityOrdersService.queryCustomerOrder(mapP);
				if(ordersEntities.size() > 0){
					for(NxCommunityOrdersEntity ordersEntity : ordersEntities){
						ordersEntity.setNxCoStatus(1);
						nxCommunityOrdersService.update(ordersEntity);

						Integer nxCoDeskId = ordersEntity.getNxCoDeskId();
						NxCommunityDeskEntity deskEntity = nxCommunityDeskService.queryObject(nxCoDeskId);
						deskEntity.setNxCdStatus(0);
						nxCommunityDeskService.update(deskEntity);

						Map<String, Object> map = new HashMap<>();
						map.put("orderId", ordersEntity.getNxCommunityOrdersId());
						List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
						if(subEntities.size() > 0){
							for(NxCommunityOrdersSubEntity subEntity: subEntities){
								subEntity.setNxCosStatus(3);
								nxCommunityOrdersSubService.update(subEntity);
							}
						}
					}

				}

				// 返回成功响应
				Map<String, String> returnData = new HashMap<>();
				returnData.put("return_code", "SUCCESS");
				returnData.put("return_msg", "OK");
				return WXPayUtil.mapToXml(returnData);
			} else {
				throw new RuntimeException("签名验证失败");
			}
		} catch (Exception e) {
			// 返回失败响应
			Map<String, String> returnData = new HashMap<>();
			returnData.put("return_code", "FAIL");
			returnData.put("return_msg", e.getMessage());
			try {
				return WXPayUtil.mapToXml(returnData);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		return "1";
	}


	@RequestMapping(value = "/getDeskInfo/{id}")
	@ResponseBody
	public R getDeskInfo(@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("deskId", id);
		map.put("status", 3);
		System.out.println("mapsppspspds" + map);
	    NxCommunityDeskEntity deskEntity =  nxCommunityDeskService.queryDeskWithOrders(map);
	    return R.ok().put("data", deskEntity);
	}


	@RequestMapping(value = "/communityGetDesk/{id}")
	@ResponseBody
	public R communityGetDesk(@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("commId", id);
		map.put("type", 0);
	    List<NxCommunityDeskEntity> deskEntities = nxCommunityDeskService.queryComDeskByParams(map);
		map.put("type", 1);
		List<NxCommunityDeskEntity> bj = nxCommunityDeskService.queryComDeskByParams(map);
		Map<String, Object> mapR = new HashMap<>();
		mapR.put("tables",deskEntities);
		mapR.put("bjs", bj);
	    return R.ok().put("data", mapR);
	}





	

	
}
