/**
 * com.nongxinle.utils class
 *
 * @Author: peiyi li
 * @Date: 2020-05-22 09:11
 */

package com.nongxinle.utils;

import com.github.wxpay.sdk.WXPayConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.PrivateKey;

/**
 *@author lpy
 *@date 2020-05-22 09:11
 */

public class MyWxQingqingxiangPayConfig implements WXPayConfig {

    private static final String APP_ID = "wxb143cab0d2768fce";
    private static final String MCH_ID = "1594384761";
    private static final String API_KEY = "sisy112578sisy112578sisy112578cf";


    // 与 WxV3Util.sendPostWithCert 保持一致，避免服务器上 JSAPI 下单成功但二次签名读不到私钥
    private static final String CERT_PATH = "/opt/nongxinle-cert/apiclient_cert.p12";

    // V3 使用的私钥路径（pem 格式）
    private static final String PRIVATE_KEY_PATH = "/opt/nongxinle-cert/apiclient_key.pem";

    @Override
    public String getAppID() {
        return APP_ID;
    }

    @Override
    public String getMchID() {
        return MCH_ID;
    }

    @Override
    public String getKey() {
        return API_KEY;
    }

    @Override
    public InputStream getCertStream() {
        try {
            return new FileInputStream(CERT_PATH); // 这个是 V2 用的 p12，可以保留
        } catch (FileNotFoundException e) {
            throw new RuntimeException("证书文件未找到", e);
        }
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return 0;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return 0;
    }

    // =============== V3 专用 ===============
    public PrivateKey getPrivateKey() {
        try {
            return WxV3Util.loadPrivateKey(PRIVATE_KEY_PATH);
        } catch (Exception e) {
            throw new RuntimeException("读取私钥失败", e);
        }
    }

    // 你也可以缓存这个私钥，不每次都加载，可以优化
}

