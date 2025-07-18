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

/**
 *@author lpy
 *@date 2020-05-22 09:11
 */


public class MyWxQingqingxiangPayConfig2 implements WXPayConfig {
    @Override
    public String getAppID() {
        return "wxb143cab0d2768fce";
    }

    @Override
    public String getMchID() {
        return "1594384761";
    }

    @Override
    public String getKey() {
        return "sisy112578sisy112578sisy112578cf";
    }
    private static final String API_KEY = "sisy112578sisy112578sisy112578cf";



    public static final String CERT_PATH = "/path/to/apiclient_cert.p12";
    // 支付回调地址
    public static final String NOTIFY_URL = "https://yourdomain.com/pay/notify";
    @Override
    public InputStream getCertStream() {
        try {
            return new FileInputStream(CERT_PATH);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("证书文件未找到", e);
        }
    }


//
//    @Override
//    public InputStream getCertStream() {
//        return null;
//    }



    @Override
    public int getHttpConnectTimeoutMs() {
        return 0;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return 0;
    }
}
