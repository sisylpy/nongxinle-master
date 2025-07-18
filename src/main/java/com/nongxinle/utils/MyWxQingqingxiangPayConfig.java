/**
 * com.nongxinle.utils class
 *
 * @Author: peiyi li
 * @Date: 2020-05-22 09:11
 */

package com.nongxinle.utils;

import com.github.wxpay.sdk.WXPayConfig;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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


    // V2 使用的 p12 证书路径
    private static final String CERT_PATH = "/Users/lpy/cert/apiclient_cert.p12";

    // V3 使用的私钥路径（pem 格式）
    private static final String PRIVATE_KEY_PATH = "/Users/lpy/cert/apiclient_key.pem";

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
        try (PEMParser pemParser = new PEMParser(new FileReader(PRIVATE_KEY_PATH))) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            Object object = pemParser.readObject();
            if (object instanceof PrivateKeyInfo) {
                return converter.getPrivateKey((PrivateKeyInfo) object);
            } else {
                throw new RuntimeException("私钥文件格式不正确");
            }
        } catch (Exception e) {
            throw new RuntimeException("读取私钥失败", e);
        }
    }

    // 你也可以缓存这个私钥，不每次都加载，可以优化
}

