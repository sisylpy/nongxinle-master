package com.nongxinle.utils;

import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 支付宝当面付预下单（precreate），未配置商户密钥时由调用方返回错误。
 */
public class PosAlipayNativeUtil {

    public static Map<String, String> precreate(String outTradeNo, BigDecimal amountYuan, String subject) throws Exception {
        MyAlipayPosConfig config = new MyAlipayPosConfig();
        if (!config.isConfigured()) {
            throw new IllegalStateException("支付宝商户未配置，请在 MyAlipayPosConfig 中填写 APP_ID 与密钥");
        }

        JSONObject biz = new JSONObject();
        biz.put("out_trade_no", outTradeNo);
        biz.put("total_amount", amountYuan.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
        biz.put("subject", subject);

        Map<String, String> params = new TreeMap<>();
        params.put("app_id", MyAlipayPosConfig.APP_ID);
        params.put("method", "alipay.trade.precreate");
        params.put("format", "JSON");
        params.put("charset", "utf-8");
        params.put("sign_type", "RSA2");
        params.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        params.put("version", "1.0");
        params.put("notify_url", MyAlipayPosConfig.NOTIFY_URL);
        params.put("biz_content", biz.toJSONString());

        String signContent = buildSignContent(params);
        String sign = PosAlipaySignUtil.rsa2Sign(signContent, MyAlipayPosConfig.PRIVATE_KEY);
        params.put("sign", sign);

        String response = PosAlipaySignUtil.httpPost(MyAlipayPosConfig.GATEWAY, params);
        JSONObject root = JSONObject.parseObject(response);
        JSONObject resp = root.getJSONObject("alipay_trade_precreate_response");
        if (resp == null) {
            throw new RuntimeException("支付宝返回异常: " + response);
        }
        if (!"10000".equals(resp.getString("code"))) {
            throw new RuntimeException("支付宝下单失败: " + resp.getString("sub_msg"));
        }
        Map<String, String> result = new HashMap<>();
        result.put("qr_code", resp.getString("qr_code"));
        result.put("out_trade_no", outTradeNo);
        return result;
    }

    public static boolean verifyNotify(Map<String, String> params) {
        if (!new MyAlipayPosConfig().isConfigured()) {
            return false;
        }
        TreeMap<String, String> sorted = new TreeMap<>(params);
        String sign = sorted.remove("sign");
        sorted.remove("sign_type");
        if (sign == null) {
            return false;
        }
        String content = buildSignContent(sorted);
        try {
            return PosAlipaySignUtil.rsa2Verify(content, sign, MyAlipayPosConfig.ALIPAY_PUBLIC_KEY);
        } catch (Exception e) {
            return false;
        }
    }

    private static String buildSignContent(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (e.getValue() == null || e.getValue().trim().isEmpty()) {
                continue;
            }
            if (!first) {
                sb.append("&");
            }
            sb.append(e.getKey()).append("=").append(e.getValue());
            first = false;
        }
        return sb.toString();
    }
}
