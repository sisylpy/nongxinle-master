package com.nongxinle.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.UUID;

public class WxV3UtilBei {

    public static String signMessage(String message, PrivateKey privateKey) throws Exception {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(sign.sign());
    }


    public static PrivateKey loadPrivateKey(String filePath) throws Exception {
        // 注册 BouncyCastle 提供器，如果还未注册
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
            System.out.println("✅ BouncyCastle provider registered.");
        }
        System.out.println("🔍 尝试读取 PEM 文件2222: " + filePath);
        // 以下原有代码继续…
        try (PEMParser pemParser = new PEMParser(new FileReader(filePath))) {
            Object object = pemParser.readObject();
            System.out.println("📦 读取到的 PEM 对象类型: " + (object != null ? object.getClass().getName() : "null"));
            if (object == null) {
                throw new IllegalArgumentException("❌ PEM 文件为空或格式错误，未读取到任何对象");
            }
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            if (object instanceof PEMKeyPair) {
                System.out.println("✅ PEM 对象是 PEMKeyPair");
                KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
                return kp.getPrivate();
            } else if (object instanceof PrivateKeyInfo) {
                System.out.println("✅ PEM 对象是 PrivateKeyInfo");
                return converter.getPrivateKey((PrivateKeyInfo) object);
            } else {
                System.out.println("⚠️ 未识别 PEM 类型: " + object.getClass());
                throw new IllegalArgumentException("不支持的 PEM 类型: " + object.getClass());
            }
        } catch (Exception e) {
            System.out.println("❗ 加载私钥失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }




    public static String sendPostWithCert(String url, String body) throws Exception {
        System.out.println("🚀 开始发送 POST 请求到微信 V3 接口");

        // 1. 加载商户私钥（用于签名）
//        String privateKeyPath = "/Users/lpy/cert/apiclient_key.pem";
        String privateKeyPath = "/opt/nongxinle-cert/apiclient_key.pem";
        System.out.println("🔐 加载私钥路径: " + privateKeyPath);
        PrivateKey privateKey = loadPrivateKey(privateKeyPath);
        System.out.println("✅ 私钥加载成功: " + privateKey);

        // 2. 构建签名用参数
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String serialNo = "72AA3DE129952C831F2799C4F25841B1F798F8B8"; // 替换成你实际的证书序列号
        URI uri = new URI(url);
        String canonicalUrl = uri.getPath();  // 只取路径部分用于签名

        String message = "POST\n" +
                canonicalUrl + "\n" +
                timestamp + "\n" +
                nonceStr + "\n" +
                body + "\n";

        System.out.println("🧾 签名字符串如下：\n" + message);

        String signature = signMessage(message, privateKey);
        System.out.println("🖋️ 生成签名成功: " + signature);

        // 3. 构建 HTTP 请求
        HttpPost post = new HttpPost(uri);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Accept", "application/json");
        post.setHeader("User-Agent", "Java-WxPay-Client");
        post.setHeader("Authorization", String.format(
                "WECHATPAY2-SHA256-RSA2048 mchid=\"%s\",nonce_str=\"%s\",timestamp=\"%s\",serial_no=\"%s\",signature=\"%s\"",
                "1594384761", nonceStr, timestamp, serialNo, signature));
        post.setEntity(new StringEntity(body, "UTF-8"));

        System.out.println("📦 请求头与 body 已构建完成");

        // 4. 加载微信 V3 客户端证书（.p12）
        System.out.println("📥 开始加载微信客户端证书");
//        String p12Path = "/Users/lpy/cert/apiclient_cert.p12";
        String p12Path = "/opt/nongxinle-cert/apiclient_cert.p12";

        String mchId = "1594384761"; // 证书密码即商户号
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream instream = new FileInputStream(p12Path)) {
            keyStore.load(instream, mchId.toCharArray());
        }
        System.out.println("✅ 客户端证书加载成功");

        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, mchId.toCharArray())
                .build();

        // 5. 发送 HTTPS 请求
        System.out.println("🚚 准备发送请求...");
        try (CloseableHttpClient client = HttpClients.custom()
                .setSSLContext(sslContext)
                .build()) {
            HttpResponse response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

            System.out.println("✅ 响应状态码: " + statusCode);
            System.out.println("📄 响应体内容:\n" + responseBody);

            return responseBody;
        } catch (Exception e) {
            System.out.println("❌ 请求失败: " + e.getMessage());
            e.printStackTrace();
            return "请求失败";
        }
    }




}

