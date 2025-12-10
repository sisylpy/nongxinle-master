package com.nongxinle.utils;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 企业微信Token管理器
 * 解决服务器重启后Token丢失的问题
 */
@Component
public class WxTokenManager {
    
    // Token缓存，避免频繁读取文件
    private static final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();
    
    // Token过期时间缓存（毫秒）
    private static final ConcurrentHashMap<String, Long> tokenExpireCache = new ConcurrentHashMap<>();
    
    // Token有效期（企业微信Token有效期7200秒）
    private static final long TOKEN_EXPIRE_TIME = 7200 * 1000;
    
    /**
     * 保存Token到文件和缓存
     */
    public void saveWxProperty(String key, String value) {
        System.out.println("保存Token: " + key + " = " + value);
        
        // 1. 保存到文件
        try {
            PropertiesConfiguration configuration = new PropertiesConfiguration("wx.properties");
            configuration.setProperty(key, value);
            configuration.save();
            System.out.println("Token已保存到文件: " + key);
        } catch (ConfigurationException e) {
            System.err.println("保存Token到文件失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 2. 保存到内存缓存
        tokenCache.put(key, value);
        tokenExpireCache.put(key, System.currentTimeMillis() + TOKEN_EXPIRE_TIME);
        
        System.out.println("Token已保存到缓存: " + key + ", 过期时间: " + tokenExpireCache.get(key));
    }
    
    /**
     * 获取Token（优先从缓存，缓存失效时从文件读取）
     */
    public String getWxProperty(String key) {
        // 1. 检查缓存是否有效
        if (tokenCache.containsKey(key)) {
            Long expireTime = tokenExpireCache.get(key);
            if (expireTime != null && System.currentTimeMillis() < expireTime) {
                System.out.println("从缓存获取Token: " + key + " = " + tokenCache.get(key));
                return tokenCache.get(key);
            } else {
                // 缓存过期，清除
                System.out.println("Token缓存已过期，清除: " + key);
                tokenCache.remove(key);
                tokenExpireCache.remove(key);
            }
        }
        
        // 2. 从文件读取
        String value = readFromFile(key);
        if (value != null && !"-1".equals(value)) {
            // 3. 更新缓存
            tokenCache.put(key, value);
            tokenExpireCache.put(key, System.currentTimeMillis() + TOKEN_EXPIRE_TIME);
            System.out.println("从文件读取Token并更新缓存: " + key + " = " + value);
        }
        
        return value;
    }
    
    /**
     * 从文件读取Token
     */
    private String readFromFile(String key) {
        Properties pps = new Properties();
        InputStream is = null;
        try {
            is = WxTokenManager.class.getClassLoader().getResourceAsStream("wx.properties");
            if (is == null) {
                System.err.println("无法找到wx.properties文件");
                return "-1";
            }
            
            pps.load(is);
            String value = pps.getProperty(key);
            System.out.println("从文件读取: " + key + " = " + value);
            return value;
            
        } catch (IOException e) {
            System.err.println("读取Token文件失败: " + e.getMessage());
            e.printStackTrace();
            return "-1";
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 清除指定Token的缓存
     */
    public void clearTokenCache(String key) {
        tokenCache.remove(key);
        tokenExpireCache.remove(key);
        System.out.println("已清除Token缓存: " + key);
    }
    
    /**
     * 清除所有Token缓存
     */
    public void clearAllTokenCache() {
        tokenCache.clear();
        tokenExpireCache.clear();
        System.out.println("已清除所有Token缓存");
    }
    
    /**
     * 检查Token是否有效
     */
    public boolean isTokenValid(String key) {
        String token = getWxProperty(key);
        return token != null && !"-1".equals(token) && !token.isEmpty();
    }
    
    /**
     * 获取缓存状态信息
     */
    public String getCacheStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Token缓存状态:\n");
        sb.append("缓存数量: ").append(tokenCache.size()).append("\n");
        
        for (String key : tokenCache.keySet()) {
            Long expireTime = tokenExpireCache.get(key);
            boolean isValid = expireTime != null && System.currentTimeMillis() < expireTime;
            sb.append(key).append(": ").append(isValid ? "有效" : "过期").append("\n");
        }
        
        return sb.toString();
    }
}
