package com.nongxinle;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;

public class GenerateCleanKey {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("  企业微信会话存档 - 生成干净的私钥");
        System.out.println("============================================================");

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey privateKey = pair.getPrivate();

            // 生成PEM格式私钥
            String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(privateKey.getEncoded()) +
                "\n-----END PRIVATE KEY-----";

            System.out.println("\n🔑 完整的PEM私钥：");
            System.out.println("============================================================");
            System.out.println(privateKeyPem);
            System.out.println("============================================================");

            // 生成单行Base64（去掉PEM头尾和换行）
            String base64Content = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----\n", "")
                .replace("\n-----END PRIVATE KEY-----", "")
                .replace("\n", "");

            System.out.println("\n📝 单行Base64内容：");
            System.out.println("============================================================");
            System.out.println(base64Content);
            System.out.println("============================================================");

            System.out.println("\n📋 Base64长度: " + base64Content.length() + " 字符");

            System.out.println("\n✅ 私钥生成完成！");
            System.out.println("\n📋 操作步骤：");
            System.out.println("1. 复制上面的单行Base64内容");
            System.out.println("2. 在MySQL中执行UPDATE语句");
            System.out.println("3. 验证更新结果");

        } catch (Exception e) {
            System.err.println("❌ 生成私钥失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
