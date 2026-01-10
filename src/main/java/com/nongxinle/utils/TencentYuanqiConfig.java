package com.nongxinle.utils;

/**
 * 腾讯元器配置
 * 使用方案A：调用"分类助手"智能体（Bearer Token + assistant_id）
 */
public class TencentYuanqiConfig {
    
    // 腾讯元器 API 地址（官方文档：https://open.hunyuan.tencent.com/openapi/v1/agent/chat/completions）
    public static final String API_URL = "https://open.hunyuan.tencent.com/openapi/v1/agent/chat/completions";
    
    // 如果 DNS 解析失败，可以使用 IP 地址（需要配置 hosts 或使用下面的 IP_URL）
    // 腾讯元器网关 IP（北京/华南地区常用）
    public static final String API_IP = "119.147.183.109";
    public static final String API_URL_BY_IP = "https://119.147.183.109/v1/agent/chat/completions";
    
    // 是否使用 IP 地址直接访问（如果 DNS 解析失败，设置为 true）
    // 注意：如果修改了 hosts 文件，应该设置为 false，使用域名访问
    public static final boolean USE_IP_DIRECTLY = false;  // 使用域名，通过 hosts 文件解析
    
    // API Token（Bearer Token，在腾讯元器平台个人中心获取）
    public static final String API_TOKEN = "aJLlElSVTGb1O1LyuvoivQ7WpyZbnQli";
    
    // 智能体ID（assistant_id，你的"分类助手"智能体的ID）
    public static final String ASSISTANT_ID = "thA2fXtHhul6";
}

