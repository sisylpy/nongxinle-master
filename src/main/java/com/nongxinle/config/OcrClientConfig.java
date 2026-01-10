package com.nongxinle.config;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OCR 客户端配置类
 * 将 OCR 客户端配置为单例 Bean，避免每次请求都创建新实例
 * 
 * @author lpy
 * @date 2025-12-27
 */
@Configuration
public class OcrClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(OcrClientConfig.class);

    @Value("${tencentcloud.ocr.secret.id}")
    private String secretId;

    @Value("${tencentcloud.ocr.secret.key}")
    private String secretKey;

    @Value("${tencentcloud.ocr.region:ap-beijing}")
    private String region;

    /**
     * 创建 OCR 客户端单例 Bean
     * OcrClient 是线程安全的，可以作为单例使用
     * 
     * @return OCR 客户端实例
     */
    @Bean
    public OcrClient ocrClient() {
        logger.info("[OcrClientConfig] 初始化 OCR 客户端单例，region: {}", region);
        
        // 初始化认证对象
        Credential cred = new Credential(secretId, secretKey);
        
        // 实例化 http 选项
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("ocr.tencentcloudapi.com");
        // 设置超时时间（单位：秒）
        httpProfile.setConnTimeout(60);  // 连接超时 60 秒
        httpProfile.setReadTimeout(60);   // 读取超时 60 秒
        httpProfile.setWriteTimeout(60);  // 写入超时 60 秒
        
        // 实例化 client 选项
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        
        // 创建并返回 OCR 客户端实例（单例）
        OcrClient client = new OcrClient(cred, region, clientProfile);
        logger.info("[OcrClientConfig] OCR 客户端单例初始化完成");
        
        return client;
    }
}

