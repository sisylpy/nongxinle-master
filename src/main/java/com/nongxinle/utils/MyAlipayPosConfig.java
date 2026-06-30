package com.nongxinle.utils;

/**
 * 支付宝 POS 扫码配置（需填入真实商户参数后启用 ALIPAY 通道）
 */
public class MyAlipayPosConfig {

    public static final String APP_ID = "";
    public static final String PRIVATE_KEY = "";
    public static final String ALIPAY_PUBLIC_KEY = "";
    public static final String GATEWAY = "https://openapi.alipay.com/gateway.do";
    public static final String NOTIFY_URL = "https://grainservice.club:8443/nongxinle/api/nxcommunitypos/payment/notify/alipay";

    public boolean isConfigured() {
        return APP_ID != null && !APP_ID.trim().isEmpty()
                && PRIVATE_KEY != null && !PRIVATE_KEY.trim().isEmpty();
    }
}
