package com.nongxinle.utils;

import com.github.wxpay.sdk.WXPayConfig;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态微信支付配置类
 * 根据nxDisId返回不同的支付配置
 * @author lpy
 * @date 2024-01-01
 */
public class DynamicWxPayConfig implements WXPayConfig {
    
    private final Integer nxDisId;
    
    // 支付配置映射表
    private static final Map<Integer, PayConfig> PAY_CONFIG_MAP = new HashMap<>();
    
    static {
        // 初始化支付配置
        // nxDisId = 56 的配置
        PAY_CONFIG_MAP.put(56, new PayConfig(
            "wx58ba279bc3d04c4a",  // appId
            "1694659996",          // mchId
            "5d020040fed265d26050f6da30578295"  // key
        ));
        
        // nxDisId = 111 的配置（示例）
        PAY_CONFIG_MAP.put(111, new PayConfig(
            "wx1111111111111111",  // appId
            "1111111111",          // mchId
            "11111111111111111111111111111111"  // key
        ));
        
        // 可以继续添加更多支付商家配置
        // PAY_CONFIG_MAP.put(222, new PayConfig("appId", "mchId", "key"));
    }
    
    public DynamicWxPayConfig(Integer nxDisId) {
        this.nxDisId = nxDisId;
    }
    
    @Override
    public String getAppID() {
        PayConfig config = PAY_CONFIG_MAP.get(nxDisId);
        if (config == null) {
            throw new RuntimeException("未找到nxDisId=" + nxDisId + "的支付配置");
        }
        return config.getAppId();
    }
    
    @Override
    public String getMchID() {
        PayConfig config = PAY_CONFIG_MAP.get(nxDisId);
        if (config == null) {
            throw new RuntimeException("未找到nxDisId=" + nxDisId + "的支付配置");
        }
        return config.getMchId();
    }
    
    @Override
    public String getKey() {
        PayConfig config = PAY_CONFIG_MAP.get(nxDisId);
        if (config == null) {
            throw new RuntimeException("未找到nxDisId=" + nxDisId + "的支付配置");
        }
        return config.getKey();
    }
    
    @Override
    public InputStream getCertStream() {
        return null;
    }
    
    @Override
    public int getHttpConnectTimeoutMs() {
        return 0;
    }
    
    @Override
    public int getHttpReadTimeoutMs() {
        return 0;
    }
    
    /**
     * 支付配置内部类
     */
    private static class PayConfig {
        private final String appId;
        private final String mchId;
        private final String key;
        
        public PayConfig(String appId, String mchId, String key) {
            this.appId = appId;
            this.mchId = mchId;
            this.key = key;
        }
        
        public String getAppId() {
            return appId;
        }
        
        public String getMchId() {
            return mchId;
        }
        
        public String getKey() {
            return key;
        }
    }
    
    /**
     * 添加新的支付配置
     * @param nxDisId 商家ID
     * @param appId 微信AppID
     * @param mchId 微信商户号
     * @param key 微信支付密钥
     */
    public static void addPayConfig(Integer nxDisId, String appId, String mchId, String key) {
        PAY_CONFIG_MAP.put(nxDisId, new PayConfig(appId, mchId, key));
    }
    
    /**
     * 检查是否存在指定nxDisId的配置
     * @param nxDisId 商家ID
     * @return 是否存在配置
     */
    public static boolean hasConfig(Integer nxDisId) {
        return PAY_CONFIG_MAP.containsKey(nxDisId);
    }
} 