package com.nongxinle.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;
import java.util.TreeMap;

public class DadaSignUtils {

    /**
     * 生成达达接口签名
     * @param appSecret 达达开发者密钥
     * @param params 请求参数（无需排序，内部会自动按参数名排序）
     * @return 签名字符串（大写）
     */
    public static String generateSignature(String appSecret, Map<String, Object> params) {
        // 1. 按参数名进行字典排序
        Map<String, Object> sortedParams = new TreeMap<>(params);

        // 2. 拼接键值对
        StringBuilder paramStr = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // 处理null值，转换为空字符串
            String valueStr = (value != null) ? value.toString() : "";
            paramStr.append(key).append(valueStr);
        }

        // 3. 首尾拼接appSecret
        String fullStr = appSecret + paramStr.toString() + appSecret;

        // 4. MD5加密并转为大写
        return DigestUtils.md5Hex(fullStr).toUpperCase();
    }
}