package com.nongxinle;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class TestPrivateKey {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("  测试私钥解析和可用性");
        System.out.println("============================================================");

        try {
            // 从数据库读取的私钥（模拟）
            String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCN9HEOv7AtXTeV\n" +
                "/87C3nLrlbl4dtSHThNxkAcTL/J44ahvOXegEY8DZSot8t6BRaquWf3l5Fo2byee\n" +
                "sKQN3JXFFg0axX7KkJrlXW7zLoFmUSjJa7APCOp/+GyNN1qZT2xHE3daimT51MCD\n" +
                "8v2N7u+Rc6pR5Q64epXo1hc3VKhWD4/4gCamDoQW9eyIJ+411qrCgJcwI7dXrZ51\n" +
                "9B1mhQL/mZQ9Jdct9dRmrhquhFTFsFRDxnArFCu7U/cq4pIZkWlkQ+61n5DRt1du\n" +
                "hJ4j8iBCSmlugAtcNNk5X/CJd2o7gj58+Geuh39uuJ7TRe/WgObETxZl8hL7yKv+\n" +
                "dfYZsr2nAgMBAAECggEAA5h5dKIuWDfQPKBqIE16I7V7KS35XCQCIhI/26CPR7iP\n" +
                "h6GxAgDv2mopk1hfgm69UAU/hR8vmcle+4oF3cQElyVj15XBDGh1Q/IF6Wr4JBtS\n" +
                "KSftIbRcHDmypjEMvm1glLxNfaGbxSg6aB0zhvDSaKDBY6mloc6ZFXi2xdQDH4rj\n" +
                "apxbLVpUxahZQug29++qswp6fdYTHhlit7Nd9FQ0E9UAWLBp7EWuJUyBXNreYzjx\n" +
                "OSucgqUCiZdjT9RGmOxiysr2lA3Cy4JFefYr4nTC0FQybZj020Q9hXUYeo5JsJsR\n" +
                "14rtrHAeW8skL0EZWG0q0A5a5xuBOHsAIfNR1t5VYQKBgQD0C6b7t2p8e5JKlxcW\n" +
                "Tnksr8hGom9EgElospwCRGrJuFagO8bpYNzxvxeZaxT5kYX8iKjUCZ2S9KWmgui3\n" +
                "o+KSw2/h4rmZw18U8IGQ4w35RgVgKw6bhU9BFydwrDeM68wPOAMITXfCG37xQ8Jt\n" +
                "TBmG+kxZE3MtTTOjW7w87y5NpQKBgQCU6JC37mh7lG5JOj7RtjnssRLCAjScJQoF\n" +
                "etlklluMjM0am3hvPM+43XaiXU7HczBxUETUiNzQgodQKPcC/O8JVoiUr689zMrY\n" +
                "Wuef+J5AG+HphsbNkaN3y3SnZcRz/2bZHzX0wkAKcnRIpEoMe9QPaF2UYim4CTpC\n" +
                "eXrV0kdUWwKBgHVG3Dn+zHeB0q1xqjCYCXdGChKXoKzkkWJ04q/cPQ0vPsHFuDMS\n" +
                "z1qFIEwjv3KnUiIncipjbIvgcXJxWnBVm143+R4uHE8eKiUf5sO/uUlfMtoiFuT5\n" +
                "zTpUbCmjORbJgciWfC6TO8fV+szqaRapCWNqCSKNTD4q6XPJc848ip3ZAoGAOG9j\n" +
                "2bHLe3qnyaz/fe94SJJbr5eyZLNM9wK2PX8Wt0/ts/XUUSRU/ZVBjwuZ/dFUPL3V\n" +
                "OE+ekI/HxMOhykoeNgXLwb5LxjpFbnYVbvWE1Hs8xYrE8cgOkipTzdWt/OCBFCzW\n" +
                "ACtWph72n0lUnyKXHjfJr6D8erlUkTkFKuCFFB0CgYAgUaSMcExh9ILAwB4RP9TJ\n" +
                "DmZUlKjnJC8HNGMbrTmFX55l6KYOpg5gPvj7xufZCM5E8N8EDGqylwZ6U6TNqNw+\n" +
                "UuApGxxezpclHIXMjUMpEgX9LQOUL+GFTJ+nAdQzgW4579gJ9pRSGGXC7lRggPRX\n" +
                "xnTqi96ITX+pxyz8LUk4QA==\n" +
                "-----END PRIVATE KEY-----";

            System.out.println("🔍 私钥信息：");
            System.out.println("- 长度: " + privateKeyPem.length() + " 字符");
            System.out.println("- 开头: " + privateKeyPem.substring(0, 30));
            System.out.println("- 结尾: " + privateKeyPem.substring(privateKeyPem.length() - 30));

            // 解析私钥
            String base64Content = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----\n", "")
                .replace("\n-----END PRIVATE KEY-----", "")
                .replace("\n", "");

            byte[] derBytes = Base64.getDecoder().decode(base64Content);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(derBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            System.out.println("\n✅ 私钥解析成功！");
            System.out.println("- 算法: " + privateKey.getAlgorithm());
            System.out.println("- 格式: " + privateKey.getFormat());
            System.out.println("- 编码长度: " + privateKey.getEncoded().length + " 字节");

            System.out.println("\n🎉 私钥测试完成，可以用于解密！");

        } catch (Exception e) {
            System.err.println("❌ 私钥测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
