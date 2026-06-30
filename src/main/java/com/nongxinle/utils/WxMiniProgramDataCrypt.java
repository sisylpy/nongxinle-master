package com.nongxinle.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 微信小程序 encryptedData 解密（用于 getGroupEnterInfo 获取 openGId）。
 */
public final class WxMiniProgramDataCrypt {
    private WxMiniProgramDataCrypt() {
    }

    public static String decryptToJson(String sessionKey, String encryptedData, String iv) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            byte[] dataBytes = Base64.getDecoder().decode(encryptedData);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
            byte[] decrypted = cipher.doFinal(dataBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("解密小程序群进入信息失败", e);
        }
    }

    public static String extractOpenGId(String sessionKey, String encryptedData, String iv) {
        if (isBlank(sessionKey) || isBlank(encryptedData) || isBlank(iv)) {
            return null;
        }
        String json = decryptToJson(sessionKey, encryptedData, iv);
        JSONObject object = JSON.parseObject(json);
        if (object == null) {
            return null;
        }
        return firstNonBlank(object.getString("opengid"), object.getString("openGId"));
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }
}
