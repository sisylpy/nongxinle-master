package com.nongxinle.controller;

/**
 * @author lpy
 * @date 10-28 13:40
 */

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import com.github.wxpay.sdk.WXPay;
import com.nongxinle.entity.GbDepartmentBillEntity;
import com.nongxinle.entity.GbDistributerPurchaseBatchEntity;
import com.nongxinle.service.GbDepartmentBillService;
import com.nongxinle.service.GbDistributerPurchaseBatchService;
import com.nongxinle.utils.*;
import com.sun.codemodel.internal.JForEach;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDistributerSupplierPaymentEntity;
import com.nongxinle.service.GbDistributerSupplierPaymentService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/gbdistributersupplierpayment")
public class GbDistributerSupplierPaymentController {
    @Autowired
    private GbDistributerSupplierPaymentService gbDisSupplierPaymentService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDPBService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;


    @RequestMapping(value = "/disSettleNxDepartmentBills", method = RequestMethod.POST)
    @ResponseBody
    public R disSettleNxDepartmentBills(@RequestBody GbDistributerSupplierPaymentEntity paymentEntity) {
		String gbDspPayTotal = paymentEntity.getGbDspPayTotal();
		Double aDouble = Double.parseDouble(gbDspPayTotal) * 100;
		int i = aDouble.intValue();
		String s1 = String.valueOf(i);

		//订单号
		String tradeNo = CommonUtils.generateOutTradeNo();
		//餐馆支付配置
		MyWxShixianguanliPayConfig config = new MyWxShixianguanliPayConfig();
		SortedMap<String, String> params = new TreeMap<>();
		params.put("appid", config.getAppID());
		params.put("mch_id", config.getMchID());
		params.put("nonce_str", CommonUtils.generateUUID());
		params.put("body",  "订单支付");
		params.put("out_trade_no", tradeNo);
		params.put("fee_type", "CNY");
		params.put("total_fee", s1);
		params.put("spbill_create_ip", "101.42.222.149");
		params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/gbdistributersupplierpayment/notify");
		params.put("trade_type", "JSAPI");
		params.put("openid", paymentEntity.getGbDspPayUserOpenId());

		//map转xml
		try {

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

			paymentEntity.setGbDspStatus(-1);
			paymentEntity.setGbDspWxOutTradeNo(tradeNo);
			gbDisSupplierPaymentService.save(paymentEntity);

			Integer gbDisSupplierPaymentId = paymentEntity.getGbDistributerSupplierPaymentId();
			for (GbDepartmentBillEntity departmentBillEntity : paymentEntity.getGbDepartmentBillEntities()) {
				departmentBillEntity.setGbDbWxOutTradeNo(tradeNo);
				departmentBillEntity.setGbDbGbSupplierPaymentId(gbDisSupplierPaymentId);
				departmentBillEntity.setGbDbStatus(4);
				gbDepartmentBillService.update(departmentBillEntity);
			}

			return R.ok().put("map", reMap);
		} catch (Exception e) {
			e.printStackTrace();
			return R.ok();
		}

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

            String ordersSn = notifyMap.get("out_trade_no");// 商户订单号
            GbDistributerSupplierPaymentEntity paymentEntity =  gbDisSupplierPaymentService.queryPaymentByWxTradeNo(ordersSn);

            if (notifyMap.get("result_code").equals("SUCCESS")) {

                /*
                 * 以下是自己的业务处理------仅做参考 更新order对应字段/已支付金额/状态码
                 * 更新bill支付状态
                 */
                System.out.println("===notify===回调方法已经被调！！！");

                GbDepartmentBillEntity billEntity = gbDepartmentBillService.queryDepartBillByTradeNo(ordersSn);
                billEntity.setGbDbStatus(4);
                gbDepartmentBillService.update(billEntity);
//
//                if(billEntityList.size() > 0){
//                    for (GbDepartmentBillEntity billEntity: billEntityList){
//
//                    }
//                }
                paymentEntity.setGbDspStatus(0);
                gbDisSupplierPaymentService.update(paymentEntity);

            }else{
                gbDisSupplierPaymentService.delete(paymentEntity.getGbDistributerSupplierPaymentId());
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

    @RequestMapping(value = "/disSettleSupplierBills", method = RequestMethod.POST)
    @ResponseBody
    public R disSettleSupplierBills(@RequestBody GbDistributerSupplierPaymentEntity paymentEntity) {

        gbDisSupplierPaymentService.save(paymentEntity);
        Integer gbDisSupplierPaymentId = paymentEntity.getGbDistributerSupplierPaymentId();

        BigDecimal total = new BigDecimal("0.0");

        if (paymentEntity.getGbDspSupplierId() != -1) {
            for (GbDistributerPurchaseBatchEntity batchEntity : paymentEntity.getGbDisPurchaseBatchEntities()) {
                total = total.add(new BigDecimal(batchEntity.getGbDpbSubtotal()));
                batchEntity.setGbDpbStatus(4);
                batchEntity.setGbDpbGbSupplierPaymentId(gbDisSupplierPaymentId);
                gbDPBService.update(batchEntity);
            }
        }
        if (paymentEntity.getGbDspNxDistributerId() != -1) {
            for (GbDepartmentBillEntity departmentBillEntity : paymentEntity.getGbDepartmentBillEntities()) {
                total = total.add(new BigDecimal(departmentBillEntity.getGbDbTotal()));
                departmentBillEntity.setGbDbStatus(4);
                departmentBillEntity.setGbDbGbSupplierPaymentId(gbDisSupplierPaymentId);
                gbDepartmentBillService.update(departmentBillEntity);
            }
        }


        paymentEntity.setGbDspDate(formatWhatDay(0));
        paymentEntity.setGbDspPayTotal(total.toString());
        gbDisSupplierPaymentService.update(paymentEntity);


        return R.ok();
    }


    @RequestMapping(value = "/getDistributerPayment/{disId}")
    @ResponseBody
    public R getDistributerPayment(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        List<GbDistributerSupplierPaymentEntity> paymentEntities = gbDisSupplierPaymentService.queryPaymentListByParams(map);
        return R.ok().put("data", paymentEntities);
    }


}
