package com.nongxinle.utils;

import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import com.nongxinle.service.QyGbDisCorpMsgAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;

/**
 * 企业微信消息解密工具
 * 
 * @author lpy
 * @date 2024-01-01
 */
@Component
public class MessageDecryptUtil {

    @Autowired
    private QyGbDisCorpMsgAuditService qyGbDisCorpMsgAuditService;

    /**
     * AES解密回调消息体（使用企业微信标准的AES/CBC/NoPadding模式）
     * @param encrypted 加密的消息体
     * @param encodingAesKey 加密密钥
     * @return 解密后的消息体
     */
    public String decryptCallbackMsg(String encrypted, String encodingAesKey) {
        try {
            // 将encodingAesKey进行Base64解码
            byte[] keyBytes = Base64.getDecoder().decode(encodingAesKey + "=");
            
            // 创建AES密钥
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            
            // 使用企业微信标准的AES/CBC/NoPadding模式
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            
            // 使用密钥的前16字节作为IV
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(keyBytes, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            // 解密
            byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            // 去除补位字符
            int pad = decryptedBytes[decryptedBytes.length - 1];
            if (pad < 1 || pad > 32) {
                pad = 0;
            }
            byte[] result = new byte[decryptedBytes.length - pad];
            System.arraycopy(decryptedBytes, 0, result, 0, result.length);
            
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("AES解密失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * RSA解密会话内容（支持多版本私钥）
     * @param encrypted 加密的内容
     * @param privateKeyStr 私钥字符串
     * @param publickeyVer 公钥版本号（1, 2, ...）
     * @return 解密后的内容
     */
    public String decryptMsgContent(String encrypted, String privateKeyStr, int publickeyVer) {
        try {
            System.out.println("[MessageDecryptUtil] 🔍 开始解密版本 " + publickeyVer + " 的消息");
            
            // 注册BouncyCastle提供器
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            }
            
            // 加载私钥
            PrivateKey privateKey = loadPrivateKeyFromString(privateKeyStr);
            System.out.println("[MessageDecryptUtil] ✅ 私钥加载成功");
            
            // 创建RSA解密器
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            
            // 解密
            byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            System.out.println("[MessageDecryptUtil] ✅ RSA解密成功，长度: " + decryptedBytes.length + " 字节");
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[MessageDecryptUtil] ❌ RSA解密失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * RSA解密会话内容（向后兼容）
     * @param encrypted 加密的内容
     * @param privateKeyStr 私钥字符串
     * @return 解密后的内容
     */
    public String decryptMsgContent(String encrypted, String privateKeyStr) {
        System.out.println("[MessageDecryptUtil] ⚠️ 未指定版本号，尝试使用版本2");
        return decryptMsgContent(encrypted, privateKeyStr, 2);
    }

    /**
     * 从字符串加载私钥
     * @param privateKeyStr 私钥字符串
     * @return PrivateKey对象
     */
    private PrivateKey loadPrivateKeyFromString(String privateKeyStr) throws Exception {
        try {
            // 移除PEM格式的头部和尾部
            String privateKeyContent = privateKeyStr
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            
            // Base64解码
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
            
            // 创建PKCS8KeySpec
            java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
            
            // 生成私钥
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            System.err.println("加载私钥失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 验证回调签名
     * @param msgSignature 签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param encryptedMsg 加密消息
     * @param token 验证token
     * @param encodingAesKey 加密密钥
     * @param corpId 企业ID
     * @return true验证成功，false验证失败
     */
    public boolean verifySignature(String msgSignature, String timestamp, String nonce, 
                                 String encryptedMsg, String token, String encodingAesKey, String corpId) {
        try {
            // 构建验证字符串
            String[] array = new String[]{token, timestamp, nonce, encryptedMsg};
            java.util.Arrays.sort(array);
            String str = String.join("", array);
            
            // 计算SHA1
            java.security.MessageDigest sha1 = java.security.MessageDigest.getInstance("SHA-1");
            sha1.update(str.getBytes(StandardCharsets.UTF_8));
            String signature = bytesToHex(sha1.digest());
            
            return msgSignature.equals(signature);
        } catch (Exception e) {
            System.err.println("验证签名失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 字节数组转十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * 解密回调验证字符串
     * @param echostr 加密的验证字符串
     * @param encodingAesKey 加密密钥
     * @return 解密后的验证字符串
     */
    public String decryptEchostr(String echostr, String encodingAesKey) {
        try {
            // 将encodingAesKey进行Base64解码
            byte[] keyBytes = Base64.getDecoder().decode(encodingAesKey + "=");
            
            // 创建AES密钥
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            
            // 使用企业微信标准的AES/CBC/NoPadding模式
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            
            // 使用密钥的前16字节作为IV
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(keyBytes, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            // 解密
            byte[] encryptedBytes = Base64.getDecoder().decode(echostr);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            // 去除补位字符（PKCS7）
            int pad = decryptedBytes[decryptedBytes.length - 1];
            if (pad < 1 || pad > 32) {
                pad = 0;
            }
            byte[] result = new byte[decryptedBytes.length - pad];
            System.arraycopy(decryptedBytes, 0, result, 0, result.length);
            
            // 解析企业微信的echostr格式：random(16字节) + msg_len(4字节) + msg + receiveid
            // 1. 提取网络字节序的消息长度（第16-20字节）
            byte[] networkOrder = Arrays.copyOfRange(result, 16, 20);
            
            // 2. 还原网络字节序为整数（大端字节序）
            int msgLen = 0;
            for (int i = 0; i < 4; i++) {
                msgLen <<= 8;
                msgLen |= networkOrder[i] & 0xff;
            }
            
            // 3. 提取消息内容
            String msg = new String(Arrays.copyOfRange(result, 20, 20 + msgLen), StandardCharsets.UTF_8);
            
            System.out.println("解密后的消息长度: " + msgLen);
            System.out.println("提取的真实echostr: " + msg);
            
            return msg;
        } catch (Exception e) {
            System.err.println("解密echostr失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
