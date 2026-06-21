package com.nongxinle.service.platform;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayUtil;
import com.nongxinle.entity.SysCityMarketEntity;
import com.nongxinle.service.SysCityMarketService;
import com.nongxinle.utils.CommonUtils;
import com.nongxinle.utils.WxPayUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 平台市场微信支付 JSAPI 下单（按市场 sysCmPayConfigClass 选商户配置）。
 */
public final class PlatformWechatPayHelper {

    private static final String DEFAULT_PAY_CONFIG_CLASS = "MyWxJJCGPayConfig";

    private PlatformWechatPayHelper() {
    }

    public static WXPayConfig resolvePayConfig(Integer marketId, SysCityMarketService marketService) {
        String className = DEFAULT_PAY_CONFIG_CLASS;
        if (marketId != null && marketService != null) {
            SysCityMarketEntity market = marketService.queryObject(marketId);
            if (market != null && StringUtils.isNotBlank(market.getSysCmPayConfigClass())) {
                className = market.getSysCmPayConfigClass().trim();
            }
        }
        return instantiatePayConfig(className);
    }

    public static Map<String, String> createJsapiPrepay(WXPayConfig config, String outTradeNo,
                                                        BigDecimal amountYuan, String openId,
                                                        String notifyUrl, String body) throws Exception {
        validatePayConfig(config);
        if (StringUtils.isBlank(notifyUrl)) {
            throw new IllegalStateException("未配置 platform.checkout.wechat.notify-url");
        }
        if (StringUtils.isBlank(openId)) {
            throw new IllegalArgumentException("openId 不能为空");
        }
        int totalFee = amountYuan.multiply(new BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
        if (totalFee <= 0) {
            throw new IllegalArgumentException("支付金额必须大于 0");
        }

        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", StringUtils.isBlank(body) ? "京采市场采购单" : body);
        params.put("out_trade_no", outTradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", String.valueOf(totalFee));
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("notify_url", notifyUrl.trim());
        params.put("trade_type", "JSAPI");
        params.put("openid", openId.trim());

        WXPay wxpay = new WXPay(config);
        Map<String, String> resp = wxpay.unifiedOrder(params);
        if (!"SUCCESS".equals(resp.get("return_code"))) {
            throw new IllegalStateException("微信下单通信失败: " + resp.get("return_msg"));
        }
        if (!"SUCCESS".equals(resp.get("result_code"))) {
            throw new IllegalStateException("微信下单失败: " + resp.get("err_code_des"));
        }

        long time = System.currentTimeMillis();
        String timeStamp = String.valueOf(time / 1000);
        SortedMap<String, String> clientParams = new TreeMap<>();
        clientParams.put("appId", config.getAppID());
        clientParams.put("nonceStr", resp.get("nonce_str"));
        clientParams.put("package", "prepay_id=" + resp.get("prepay_id"));
        clientParams.put("signType", "MD5");
        clientParams.put("timeStamp", timeStamp);
        String paySign = WxPayUtils.creatSign(clientParams, config.getKey());
        clientParams.put("paySign", paySign);

        Map<String, String> result = new HashMap<>(clientParams);
        result.put("prepayId", resp.get("prepay_id"));
        return result;
    }

    public static Map<String, String> resignJsapiParams(WXPayConfig config, String prepayId, String nonceStr) {
        long time = System.currentTimeMillis();
        String timeStamp = String.valueOf(time / 1000);
        SortedMap<String, String> clientParams = new TreeMap<>();
        clientParams.put("appId", config.getAppID());
        clientParams.put("nonceStr", nonceStr);
        clientParams.put("package", "prepay_id=" + prepayId);
        clientParams.put("signType", "MD5");
        clientParams.put("timeStamp", timeStamp);
        String paySign = WxPayUtils.creatSign(clientParams, config.getKey());
        clientParams.put("paySign", paySign);
        return new HashMap<>(clientParams);
    }

    public static boolean verifyNotifySignature(Map<String, String> notifyData, WXPayConfig config) throws Exception {
        return WXPayUtil.isSignatureValid(notifyData, config.getKey());
    }

    private static void validatePayConfig(WXPayConfig config) {
        if (config == null) {
            throw new IllegalStateException("微信支付配置不存在");
        }
        if (StringUtils.isBlank(config.getAppID())) {
            throw new IllegalStateException("微信支付未配置 appId");
        }
        if (StringUtils.isBlank(config.getMchID())) {
            throw new IllegalStateException("微信支付未配置 mchId");
        }
        if (StringUtils.isBlank(config.getKey())) {
            throw new IllegalStateException("微信支付未配置 API_KEY");
        }
    }

    private static WXPayConfig instantiatePayConfig(String className) {
        try {
            Class<?> clazz = Class.forName("com.nongxinle.utils." + className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (!(instance instanceof WXPayConfig)) {
                throw new IllegalStateException("支付配置类未实现 WXPayConfig: " + className);
            }
            return (WXPayConfig) instance;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("无法加载支付配置: " + className, ex);
        }
    }
}
