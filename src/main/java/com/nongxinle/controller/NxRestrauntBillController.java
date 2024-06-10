package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 12-13 09:47
 */

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPay;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.apache.http.HttpRequest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxrestrauntbill")
public class NxRestrauntBillController {
	@Autowired
	private NxRestrauntBillService nxRestrauntBillService;
	@Autowired
	private NxRestrauntService nxRestrauntService;
	@Autowired
	private NxRestrauntOrdersService nxRestrauntOrdersService;
	@Autowired
	private NxRestrauntOrdersHistoryService nxRestrauntOrdersHistoryService;




	@RequestMapping(value = "/saveBillAndOrderProfit", method = RequestMethod.POST)
	@ResponseBody
	public R saveBillAndOrderProfit (@RequestBody NxRestrauntBillEntity bill) {
		List<NxRestrauntOrdersEntity> nxRestrauntOrdersEntities = bill.getNxRestrauntOrdersEntities();
		for (NxRestrauntOrdersEntity orders : nxRestrauntOrdersEntities) {
			orders.setNxRoStatus(8);
			nxRestrauntOrdersService.update(orders);
		}

		nxRestrauntBillService.update(bill);

		Integer restrauntId = bill.getNxRbRestrauntId();

		NxRestrauntEntity restrauntEntity = nxRestrauntService.queryObject(restrauntId);
		BigDecimal addTotal = new BigDecimal(restrauntEntity.getNxRestrauntPayTotal()).add(new BigDecimal(bill.getNxRbGoodsTotal()));
		BigDecimal profitTotal = new BigDecimal(restrauntEntity.getNxRestrauntProfitTotal()).add(new BigDecimal(bill.getNxRbProfitTotal()));
		BigDecimal subtract = profitTotal.divide(addTotal,10,BigDecimal.ROUND_HALF_DOWN).setScale(2,BigDecimal.ROUND_DOWN).multiply(new BigDecimal(100));
		restrauntEntity.setNxRestrauntPayTotal(addTotal.toString());
		restrauntEntity.setNxRestrauntProfitTotal(profitTotal.toString());
		restrauntEntity.setNxRestrauntProfitPercent(subtract.toString());
		nxRestrauntService.update(restrauntEntity);

		return R.ok();
	}

	@RequestMapping(value = "/getBillDetailProfit/{billId}") 
	@ResponseBody
	public R getBillDetailProfit(@PathVariable Integer billId) {
	   NxRestrauntBillEntity billEntity = nxRestrauntBillService.queryRestrauntBillApplys(billId);
	    return R.ok().put("data",billEntity);
	}


	@RequestMapping(value = "/comGetUnPorfitBill/{comId}")
	@ResponseBody
	public R comGetUnPorfitBill(@PathVariable Integer comId) {
		List<NxRestrauntBillEntity> billEntities = nxRestrauntBillService.queryUnProfitBill(comId);
		return R.ok().put("data", billEntities);
	}


	@RequestMapping(value = "/customerSignResBill", method = RequestMethod.POST)
    @ResponseBody
    public R customerSignResBill (@RequestBody NxRestrauntBillEntity bill) {
		//update restarunt
    	Integer restrauntId = bill.getNxRbRestrauntId();
		NxRestrauntEntity restrauntEntity = nxRestrauntService.queryObject(restrauntId);
		restrauntEntity.setNxRestrauntWorkingStatus(0);
		nxRestrauntService.update(restrauntEntity);

		Integer nxRestrauntBillId = bill.getNxRestrauntBillId();
		List<Integer> integers = nxRestrauntOrdersService.queryResOrdersIdByBillId(nxRestrauntBillId);
		for (Integer id: integers){
			NxRestrauntOrdersEntity ordersEntity = nxRestrauntOrdersService.queryObject(id);
			ordersEntity.setNxRoStatus(7);
			ordersEntity.setNxRoReceiveFullTime(formatFullTime());
			nxRestrauntOrdersService.update(ordersEntity);
		}

		//update resbill
		nxRestrauntBillService.update(bill);
        return R.ok();
    }

	@RequestMapping(value = "/comGetUnSignCustomer/{comId}")
	@ResponseBody
	public R comGetUnSignCustomer(@PathVariable Integer comId) {
		List<NxRestrauntBillEntity> restrauntEntities = nxRestrauntBillService.queryUnSignCustomer(comId);
	    return R.ok().put("data", restrauntEntities);
	}


	@RequestMapping(value = "/settleResBills", method = RequestMethod.POST)
	@ResponseBody
	public R settleResBills(@RequestBody List<NxRestrauntBillEntity> bills) {
		for (NxRestrauntBillEntity bill : bills) {
			bill.setNxRbStatus(3);
			bill.setNxRbPayTime(formatWhatTime(0));
			bill.setNxRbPayFullTime(formatWhatDay(0));
			nxRestrauntBillService.update(bill);
		}
		return R.ok();
	}

	/**
	 * 批发商获取未结账账单
	 *
	 * @param comId 批发商id
	 * @return 订单列表
	 */
	@RequestMapping(value = "/comGetUnSettleAccountBills", method = RequestMethod.POST)
	@ResponseBody
	public R comGetUnSettleAccountBills(Integer comId, Integer resId) {
		Map<String, Object> map = new HashMap<>();
		map.put("comId", comId);
		map.put("resId", resId);
		map.put("status", 3);
		List<NxRestrauntBillEntity> billEntityList = nxRestrauntBillService.queryRestrauntBillsByParams(map);
		if(billEntityList.size() > 0){
			return R.ok().put("data", billEntityList);

		}else{
			return R.error(-1, "没有订单");
		}
	}



	@RequestMapping(value = "/chainResAndComGetAccountBills", method = RequestMethod.POST)
	@ResponseBody
	public R chainResAndComGetAccountBills(Integer resFatherId, Integer comId) {
		NxRestrauntEntity restrauntEntity = nxRestrauntService.queryResInfo(resFatherId);
		List<Integer> ids = new ArrayList<>();
		for (NxRestrauntEntity res :restrauntEntity.getNxRestrauntEntities()) {
			Integer nxRestrauntId = res.getNxRestrauntId();
			ids.add(nxRestrauntId);
		}
		Map<String, Object> stringObjectMap = queryChainResAccountBillByMonth(comId, ids, 0);
		Map<String, Object> lastObjectMap = queryChainResAccountBillByMonth(comId, ids, 1);
		Map<String, Object> lastTwoObjectMap = queryChainResAccountBillByMonth(comId, ids, 2);
		List<Map<String, Object>> dataMap = new ArrayList<>();
		dataMap.add(lastTwoObjectMap);
		dataMap.add(lastObjectMap);
		dataMap.add(stringObjectMap);


		//查询总账款金额
		Map<String, Object> map = new HashMap<>();
		map.put("resIds", ids);
		map.put("equalStatus", 0);
		List<NxRestrauntBillEntity> billEntityList = nxRestrauntBillService.queryRestrauntBillsByParams(map);


		double total = 0.0;
		for (NxRestrauntBillEntity bill : billEntityList) {
			String nxDbTotal = bill.getNxRbTotal();
			Double aDouble = Double.valueOf(nxDbTotal);
			total = total + aDouble;
		}
		String formatTotal = String.format("%.2f", total);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("arr", dataMap);
		resultMap.put("total", formatTotal);

		return R.ok().put("data", resultMap);
	}


	@RequestMapping(value = "/resAndComGetAccountBills", method = RequestMethod.POST)
	@ResponseBody
	public R resAndComGetAccountBills(Integer resFatherId, Integer comId) {

		Map<String, Object> stringObjectMap = queryResAccountBillByMonth(comId, resFatherId, 0);
		Map<String, Object> lastObjectMap = queryResAccountBillByMonth(comId, resFatherId, 1);
		Map<String, Object> lastTwoObjectMap = queryResAccountBillByMonth(comId, resFatherId, 2);
		List<Map<String, Object>> dataMap = new ArrayList<>();
		dataMap.add(lastTwoObjectMap);
		dataMap.add(lastObjectMap);
		dataMap.add(stringObjectMap);


		//查询总账款金额
		Map<String, Object> map = new HashMap<>();
		map.put("resId", resFatherId);
//		map.put("equalStatus", 0);
		map.put("status", 3);

		List<NxRestrauntBillEntity> billEntityList = nxRestrauntBillService.queryRestrauntBillsByParams(map);

		double total = 0.0;
		String formatTotal = "0.0";
		for (NxRestrauntBillEntity bill : billEntityList) {
			String nxDbTotal = bill.getNxRbTotal();
			Double aDouble = Double.valueOf(nxDbTotal);
			total = total + aDouble;
		}

		formatTotal = String.format("%.2f", total);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("arr", dataMap);
		resultMap.put("total", formatTotal);

		return R.ok().put("data", resultMap);
	}



	private Map<String, Object> queryChainResAccountBillByMonth(Integer comId, List<Integer> ids, int which) {

		LocalDate today = LocalDate.now();
		today = today.minusMonths(which);
		DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM");
		String format = formatters.format(today);


		//本月的账单
		Map<String, Object> map = new HashMap<>();
		map.put("comId", comId);
		map.put("resIds", ids);
		map.put("month", format);
		map.put("status", 3);
		List<NxRestrauntBillEntity> billEntityList = nxRestrauntBillService.queryRestrauntBillsByParams(map);

		//本月的未结账单数量
		Map<String, Object> map1 = new HashMap<>();
		map1.put("comId", comId);
		map1.put("resIds", ids);
//		map1.put("equalStatus", 0);
		map1.put("status", 3);
		map1.put("month", format);
		int whichMonthUnSettleTotal = nxRestrauntBillService.queryTotalRestrauntBillByParams(map1);
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("arr", billEntityList);
		dataMap.put("unSettleTotal", whichMonthUnSettleTotal);
		dataMap.put("month", format);

		return dataMap;
	}


	private Map<String, Object> queryResAccountBillByMonth(Integer comId, Integer resFatherId, int which) {

		LocalDate today = LocalDate.now();
		today = today.minusMonths(which);
		DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM");
		String format = formatters.format(today);
		//本月的账单
		Map<String, Object> map = new HashMap<>();
		map.put("comId", comId);
		map.put("resId", resFatherId);
		map.put("month", format);
//		map.put("status", 3);
		List<NxRestrauntBillEntity> billEntityList = nxRestrauntBillService.queryRestrauntBillsByParams(map);

		//本月的未结账单数量
		Map<String, Object> map1 = new HashMap<>();
		map1.put("comId", comId);
		map1.put("resId", resFatherId);
		map1.put("status", 3);
		map1.put("month", format);
		int whichMonthUnSettleTotal = nxRestrauntBillService.queryTotalRestrauntBillByParams(map1);
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("arr", billEntityList);
		dataMap.put("unSettleTotal", whichMonthUnSettleTotal);
		dataMap.put("month", format);

		return dataMap;
	}


	@RequestMapping(value = "/comPrintSalesResOrders", method = RequestMethod.POST)
	@ResponseBody
	public R comPrintSalesResOrders (@RequestBody NxRestrauntBillEntity restrauntBillEntity  ) {
		String tradeNo = CommonUtils.generateOutTradeNo();

		restrauntBillEntity.setNxRbStatus(1);
		restrauntBillEntity.setNxRbProduceTime(formatWhatDayTime(0));
		restrauntBillEntity.setNxRbMonth(formatWhatMonth(0));
		restrauntBillEntity.setNxRbDate(formatWhatDate(0));
		restrauntBillEntity.setNxRbTradeNo(tradeNo);
		restrauntBillEntity.setNxRbApplyPayTime(formatWhatDayTime(0));
		restrauntBillEntity.setNxRbPayFullTime(formatWhatDay(0));
		restrauntBillEntity.setNxRbDeliveryDate(formatWhatDay(0));
		restrauntBillEntity.setNxRbDay(getWeek(0));
		nxRestrauntBillService.save(restrauntBillEntity);
		List<NxRestrauntOrdersEntity> ordersEntities = restrauntBillEntity.getNxRestrauntOrdersEntities();
		for (NxRestrauntOrdersEntity orders : ordersEntities) {
			orders.setNxRoStatus(7);
			orders.setNxRoPrintTimes(1);
			orders.setNxRoDeliveryDate(formatWhatDay(0));
			orders.setNxRoBillId(restrauntBillEntity.getNxRestrauntBillId());
			nxRestrauntOrdersService.update(orders);
		}
		Integer nxRbRestrauntId = restrauntBillEntity.getNxRbRestrauntId();
		NxRestrauntEntity nxRestrauntEntity = nxRestrauntService.queryObject(nxRbRestrauntId);
		nxRestrauntEntity.setNxRestrauntWorkingStatus(0);
		nxRestrauntService.update(nxRestrauntEntity);

		return R.ok();
	}



	@RequestMapping(value = "/comPrintAccountResOrders", method = RequestMethod.POST)
	@ResponseBody
	public R comPrintAccountResOrders (@RequestBody  NxRestrauntBillEntity restrauntBillEntity ) {

		String tradeNo = CommonUtils.generateOutTradeNo();

		restrauntBillEntity.setNxRbStatus(0);
		restrauntBillEntity.setNxRbTradeNo(tradeNo);
		restrauntBillEntity.setNxRbProduceTime(formatWhatDayTime(0));
		restrauntBillEntity.setNxRbDay(getWeek(0));
		restrauntBillEntity.setNxRbMonth(formatWhatMonth(0));
		restrauntBillEntity.setNxRbDate(formatWhatDate(0));
		restrauntBillEntity.setNxRbWeek(getWeekOfYear(0).toString());
		nxRestrauntBillService.save(restrauntBillEntity);
		List<NxRestrauntOrdersEntity> ordersEntities = restrauntBillEntity.getNxRestrauntOrdersEntities();
		for (NxRestrauntOrdersEntity orders : ordersEntities) {
			orders.setNxRoPrintTimes(1);
			orders.setNxRoBillId(restrauntBillEntity.getNxRestrauntBillId());
			nxRestrauntOrdersService.update(orders);
		}
		return R.ok();
	}


	@RequestMapping(value = "/getRestrauntBillApplys/{billId}")
	@ResponseBody
	public R getRestrauntBillApplys(@PathVariable Integer billId) {
        NxRestrauntBillEntity billEntity =  nxRestrauntBillService.queryRestrauntBillApplys(billId);
	    return R.ok().put("data", billEntity);
	}




	//
	@RequestMapping(value = "/restrauntAndComGetAccountBills", method = RequestMethod.POST)
	@ResponseBody
	public R restrauntAndComGetAccountBills(Integer resFatherId, Integer comId) {
		Map<String, Object> lastTwoObjectMap = queryAccountBillByMonth(comId, resFatherId, 2);
		Map<String, Object> lastObjectMap = queryAccountBillByMonth(comId, resFatherId, 1);
		Map<String, Object> stringObjectMap = queryAccountBillByMonth(comId, resFatherId, 0);
		List<Map<String, Object>> dataMap = new ArrayList<>();
		dataMap.add(lastTwoObjectMap);
		dataMap.add(lastObjectMap);
		dataMap.add(stringObjectMap);

		//查询总账款金额
		Map<String, Object> map = new HashMap<>();
		map.put("resId", resFatherId);
		map.put("status", 3);
		List<NxRestrauntBillEntity> billEntityList = nxRestrauntBillService.queryRestrauntBillsByParams(map);
		double total = 0.0;
		for (NxRestrauntBillEntity bill : billEntityList) {
			String nxDbTotal = bill.getNxRbTotal();
			Double aDouble = Double.valueOf(nxDbTotal);
			total = total + aDouble;
		}
		String formatTotal = String.format("%.2f", total);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("arr", dataMap);
		resultMap.put("total", formatTotal);
		return R.ok().put("data", resultMap);
	}

	@RequestMapping(value = "/restrauntAndComGetSalesBills", method = RequestMethod.POST)
	@ResponseBody
	public R restrauntAndComGetSalesBills(Integer resFatherId, Integer comId) {

		Map<String, Object> lastTwoObjectMap = querySalesBillByMonth(comId, resFatherId, 2);
		Map<String, Object> lastObjectMap = querySalesBillByMonth(comId, resFatherId, 1);
		Map<String, Object> stringObjectMap = querySalesBillByMonth(comId, resFatherId, 0);
		List<Map<String, Object>> dataMap = new ArrayList<>();
		dataMap.add(lastTwoObjectMap);
		dataMap.add(lastObjectMap);
		dataMap.add(stringObjectMap);
		return R.ok().put("data", dataMap);

	}
	private Map<String, Object> queryAccountBillByMonth(Integer comId, Integer resFatherId, int which) {

		LocalDate today = LocalDate.now();
		today = today.minusMonths(which);
		DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM");
		String format = formatters.format(today);
		//本月的账单
		Map<String, Object> map = new HashMap<>();
		map.put("comId", comId);
		map.put("resId", resFatherId);
		map.put("month", format);
		map.put("status", 4);
		List<NxRestrauntBillEntity> billEntityList = nxRestrauntBillService.queryRestrauntBillsByParams(map);

		//本月的未结账单数量
		Map<String, Object> map1 = new HashMap<>();
		map1.put("comId", comId);
		map1.put("resId", resFatherId);
		map1.put("status", 3);
//		map1.put("equalStatus", 0);
		map1.put("month", format);
		int whichMonthUnSettleTotal = nxRestrauntBillService.queryTotalRestrauntBillByParams(map1);
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("arr", billEntityList);
		dataMap.put("unSettleTotal", whichMonthUnSettleTotal);
		dataMap.put("month", format);

		return dataMap;
	}


	private Map<String, Object> querySalesBillByMonth(Integer comId, Integer resFatherId, int which) {

		LocalDate today = LocalDate.now();
		today = today.minusMonths(which);
		DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM");
		String format = formatters.format(today);
		//本月的账单
		Map<String, Object> map = new HashMap<>();
		map.put("comId", comId);
		map.put("resId", resFatherId);
		map.put("month", format);
//		map.put("equalStatus", 0);
		List<NxRestrauntBillEntity> billEntityList = nxRestrauntBillService.queryRestrauntBillsByParams(map);

		BigDecimal total = new BigDecimal(0);
		for (NxRestrauntBillEntity bill : billEntityList) {
			String nxRbGoodsTotal = bill.getNxRbGoodsTotal();
			total = total.add(new BigDecimal(nxRbGoodsTotal));

		}
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("arr", billEntityList);
		dataMap.put("month", format);
		dataMap.put("total", total);

		return dataMap;
	}


	@ResponseBody
	@RequestMapping("/restrauntCashPay")
	public R restrauntCashPay(@RequestBody NxRestrauntBillEntity billEntity, HttpServletRequest request) {

		//转换总金额
		String nxRbTotal = billEntity.getNxRbTotal();
		Double aDouble = Double.parseDouble(nxRbTotal) * 100;
		int i = aDouble.intValue();
		String s1 = String.valueOf(i);

		//订单号

		String tradeNo = CommonUtils.generateOutTradeNo();
		//餐馆支付配置
		MyWxRestrauntPayConfig config = new MyWxRestrauntPayConfig();
		SortedMap<String, String> params = new TreeMap<>();
		params.put("appid", config.getAppID());
		params.put("mch_id", config.getMchID());
		params.put("nonce_str", CommonUtils.generateUUID());
		params.put("body", billEntity.getNxRbRestrauntName() + "订单支付");
		params.put("out_trade_no", tradeNo);
		params.put("fee_type", "CNY");
		params.put("total_fee", s1);
		params.put("spbill_create_ip", "123.12.12.123");
		params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxrestrauntbill/notify");
		params.put("trade_type", "JSAPI");
		params.put("openid", billEntity.getNxRbUserOpenId());

		//map转xml
		try {

			Integer restrauntId = billEntity.getNxRbRestrauntId();
			Map<String, Object> map = new HashMap<>();
			map.put("resId",restrauntId);
			map.put("equalStatus", 0 );
			NxRestrauntBillEntity unPayRestrauntBill = nxRestrauntBillService.queryUnPayRestrauntBill(map);
			if(unPayRestrauntBill == null ){
					billEntity.setNxRbStatus(0);
				    billEntity.setNxRbTradeNo(tradeNo);
				    billEntity.setNxRbProduceTime(formatWhatDayTime(0));
				    billEntity.setNxRbApplyPayTime(formatWhatDayTime(0));
					billEntity.setNxRbPayFullTime(formatWhatDay(0));
					billEntity.setNxRbDate(formatWhatDay(0));
					billEntity.setNxRbWeek(getWeek(0));
					billEntity.setNxRbMonth(formatWhatMonth(0));
					nxRestrauntBillService.save(billEntity);
				//update order status= 5
				Map<String, Object> map2 = new HashMap<>();
				map2.put("resFatherId", restrauntId);
				map2.put("equalStatus", 5);
				List<NxRestrauntOrdersEntity> entities = nxRestrauntOrdersService.queryResOrdersByParams(map2);
				for (NxRestrauntOrdersEntity orders : entities) {
					orders.setNxRoStatus(6);
					orders.setNxRoBillId(billEntity.getNxRestrauntBillId());
					nxRestrauntOrdersService.update(orders);
				}

				billEntity.setNxRbDate(entities.get(0).getNxRoArriveOnlyDate());
				billEntity.setNxRbWeek(entities.get(0).getNxRoArriveWeeksYear().toString());
				billEntity.setNxRbDay(entities.get(0).getNxRoArriveWhatDay());
				billEntity.setNxRbDeliveryDate(entities.get(0).getNxRoDeliveryDate());
                nxRestrauntBillService.update(billEntity);

				Integer nxRbRestrauntId = billEntity.getNxRbRestrauntId();
				NxRestrauntEntity nxRestrauntEntity = nxRestrauntService.queryObject(nxRbRestrauntId);
				nxRestrauntEntity.setNxRestrauntWorkingStatus(4);
				nxRestrauntService.update(nxRestrauntEntity);
			}
			else{
				//
				billEntity.setNxRbProduceTime(formatWhatDayTime(0));
				billEntity.setNxRbMonth(formatWhatMonth(0));
				billEntity.setNxRbDate(formatWhatDate(0));
				billEntity.setNxRbDay(getWeek(0));
				billEntity.setNxRbTradeNo(tradeNo);
				billEntity.setNxRbPayFullTime(formatWhatDay(0));
				billEntity.setNxRbApplyPayTime(formatWhatDayTime(0));
				nxRestrauntBillService.update(billEntity);
				Map<String, Object> map2 = new HashMap<>();
				map2.put("resFatherId", restrauntId);
				map2.put("equalStatus", 5);
				List<NxRestrauntOrdersEntity> entities = nxRestrauntOrdersService.queryResOrdersByParams(map2);
				for (NxRestrauntOrdersEntity orders : entities) {
					orders.setNxRoStatus(6);
					orders.setNxRoBillId(billEntity.getNxRestrauntBillId());
					nxRestrauntOrdersService.update(orders);
				}
			}


			WXPay wxpay = new WXPay(config);
			long time = System.currentTimeMillis();
			String tString = String.valueOf(time/1000);
			Map<String, String> resp = wxpay.unifiedOrder(params);
			System.out.println(resp);
			SortedMap<String, String> reMap = new TreeMap<>();
			reMap.put("appId", config.getAppID());
			reMap.put("nonceStr", resp.get("nonce_str"));
			reMap.put("package", "prepay_id=" + resp.get("prepay_id"));
			reMap.put("signType", "MD5");
			reMap.put("timeStamp", tString);
			String s = WxPayUtils.creatSign(reMap, config.getKey());
			reMap.put("paySign", s);

			return R.ok().put("map", reMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return R.ok();
	}



	/**
	 * @Title: callBack
	 * @Description: 支付完成的回调函数
	 * @param:
	 * @return:
	 */
	@RequestMapping("/notify")
	public String callBack(HttpServletRequest request, HttpServletResponse response) {
		// System.out.println("微信支付成功,微信发送的callback信息,请注意修改订单信息");
		InputStream is = null;
		try {

			is = request.getInputStream();// 获取请求的流信息(这里是微信发的xml格式所有只能使用流来读)
			String xml = WxPayUtils.InputStream2String(is);
			Map<String, String> notifyMap = WxPayUtils.xmlToMap(xml);// 将微信发的xml转map

			System.out.println("微信返回给回调函数的信息为："+xml);

			if (notifyMap.get("result_code").equals("SUCCESS")) {

				/*
				 * 以下是自己的业务处理------仅做参考 更新order对应字段/已支付金额/状态码
				 */
				System.out.println("===notify===回调方法已经被调！！！");

				//更新bill支付状态
				String ordersSn = notifyMap.get("out_trade_no");// 商户订单号
				NxRestrauntBillEntity billEntity = nxRestrauntBillService.queryRestrauntBillByTradeNo(ordersSn);
				billEntity.setNxRbStatus(1);
				billEntity.setNxRbPayTime(formatWhatDayTime(0));
				nxRestrauntBillService.update(billEntity);
//				String amountpaid = notifyMap.get("total_fee");// 实际支付的订单金额:单位 分
//				BigDecimal amountPay = (new BigDecimal(amountpaid).divide(new BigDecimal("100"))).setScale(2);// 将分转换成元-实际支付金额:元
//

				//更新餐馆状态
				Integer nxRbRestrauntId = billEntity.getNxRbRestrauntId();
				NxRestrauntEntity nxRestrauntEntity = nxRestrauntService.queryObject(nxRbRestrauntId);
				nxRestrauntEntity.setNxRestrauntWorkingStatus(0);
				nxRestrauntService.update(nxRestrauntEntity);

				//update order status= 5
				Map<String, Object> map = new HashMap<>();
				map.put("resFatherId", nxRbRestrauntId);
				map.put("equalStatus", 6);
				List<NxRestrauntOrdersEntity> entities = nxRestrauntOrdersService.queryResOrdersByParams(map);
				for (NxRestrauntOrdersEntity orders : entities) {
					//update order
					orders.setNxRoStatus(7);
					nxRestrauntOrdersService.update(orders);
					//增加订货历史
					Integer nxRoResComGoodsId = orders.getNxRoResComGoodsId();
					Map<String, Object> map1 = new HashMap<>();
					map1.put("resComGoodsId", nxRoResComGoodsId);
					map1.put("resId", orders.getNxRoRestrauntFatherId());
					List<NxRestrauntOrdersHistoryEntity> historyEntities = nxRestrauntOrdersHistoryService.queryHistoryOrdersByParams(map1);

					String orderQuantity  = "";
					String orderStandard = "";
					String orderStr = "";
					if(orders.getNxRoComGoodsStandardType() == 0){
						orderQuantity = orders.getNxRoQuantity() ;
						orderStandard =  orders.getNxRoStandard();
					}else if(orders.getNxRoComGoodsStandardType() == 1){
						orderQuantity = orders.getNxRoComStandardQuantity();
						orderStandard = orders.getNxRoComStandardName();
					}
					orderStr = orderQuantity + orderStandard;

					//如果有4个以内的历史记录
					if( historyEntities.size() > 0 && historyEntities.size() < 4){


						int equalNumber = 0;
						for (NxRestrauntOrdersHistoryEntity orderHistory : historyEntities) {
							String historyStr  = orderHistory.getNxRohQuantity() + orderHistory.getNxRohStandard();
							if(orderStr.equals(historyStr)){
								equalNumber = equalNumber + 1;
							}
						}
						if(equalNumber == 0 && historyEntities.size() < 3){
							//添加新的
							NxRestrauntOrdersHistoryEntity historyEntity = new NxRestrauntOrdersHistoryEntity();
							historyEntity.setNxRohApplyDate(orders.getNxRoApplyDate());
							historyEntity.setNxRohResComGoodsId(orders.getNxRoResComGoodsId());
							historyEntity.setNxRohQuantity(orderQuantity);
							historyEntity.setNxRohStandard(orderStandard);
							historyEntity.setNxRohRestrauntId(orders.getNxRoRestrauntId());
							historyEntity.setNxRohRestrauntFatherId(orders.getNxRoRestrauntFatherId());
							historyEntity.setNxRohSellType(orders.getNxRoComGoodsStandardType());
							historyEntity.setNxRohOrderUserId(orders.getNxRoOrderUserId());
							nxRestrauntOrdersHistoryService.save(historyEntity);
						}else if(equalNumber == 0 && historyEntities.size() == 3){
							//删除最早一个
							NxRestrauntOrdersHistoryEntity first = historyEntities.get(0);
							Integer nxRestrauntOrdersHistoryId = first.getNxRestrauntOrdersHistoryId();
							nxRestrauntOrdersHistoryService.delete(nxRestrauntOrdersHistoryId);
							//添加新的
							NxRestrauntOrdersHistoryEntity historyEntity = new NxRestrauntOrdersHistoryEntity();
							historyEntity.setNxRohApplyDate(orders.getNxRoApplyDate());
							historyEntity.setNxRohResComGoodsId(orders.getNxRoResComGoodsId());
							historyEntity.setNxRohQuantity(orderQuantity);
							historyEntity.setNxRohStandard(orderStandard);
							historyEntity.setNxRohRestrauntId(orders.getNxRoRestrauntId());
							historyEntity.setNxRohRestrauntFatherId(orders.getNxRoRestrauntFatherId());
							historyEntity.setNxRohSellType(orders.getNxRoSellType());
							historyEntity.setNxRohOrderUserId(orders.getNxRoOrderUserId());
							nxRestrauntOrdersHistoryService.save(historyEntity);
						}

					}else{
						//添加新的
						NxRestrauntOrdersHistoryEntity historyEntity = new NxRestrauntOrdersHistoryEntity();
						historyEntity.setNxRohApplyDate(orders.getNxRoApplyDate());
						historyEntity.setNxRohResComGoodsId(orders.getNxRoResComGoodsId());
						historyEntity.setNxRohQuantity(orderQuantity);
						historyEntity.setNxRohStandard(orderStandard);
						historyEntity.setNxRohRestrauntId(orders.getNxRoRestrauntId());
						historyEntity.setNxRohRestrauntFatherId(orders.getNxRoRestrauntFatherId());
						historyEntity.setNxRohSellType(orders.getNxRoComGoodsStandardType());
						historyEntity.setNxRohOrderUserId(orders.getNxRoOrderUserId());
						nxRestrauntOrdersHistoryService.save(historyEntity);

					}
				}
			}

			// 告诉微信服务器收到信息了，不要在调用回调action了========这里很重要回复微信服务器信息用流发送一个xml即可
			response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}




	
}
