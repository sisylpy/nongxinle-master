package com.nongxinle.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯云智能体配置
 */
public class TencentCloudAgentConfig {
    
    // 腾讯云智能体开发平台(ADP) HTTP SSE接口地址
    public static final String API_URL = "https://wss.lke.cloud.tencent.com/v1/qbot/chat/sse";
    
    // ========== 智能体 AppKey 配置 ==========
    // 默认智能体（商品分类智能体）
    public static final String AGENT_APP_KEY = "jvXlnvfcCQYMWsbMgMGQZicRbATsavDUOxIsBUhSqrZFkEbXpPxIPNOLUmBeXDyhCZhdHsmbjFZrvVBRMggbJOEHdgeNcdtXbLXpRoWMPbjryEflBdhWYntoxkYcBloQ";
    
    // 多个智能体的 AppKey 映射（可以根据需要添加）
    // Key: 智能体名称/标识，Value: AppKey
    private static final Map<String, String> AGENT_APP_KEYS = new HashMap<String, String>() {{
        put("goods_classify", "jvXlnvfcCQYMWsbMgMGQZicRbATsavDUOxIsBUhSqrZFkEbXpPxIPNOLUmBeXDyhCZhdHsmbjFZrvVBRMggbJOEHdgeNcdtXbLXpRoWMPbjryEflBdhWYntoxkYcBloQ");  // 商品分类智能体
        // 在这里添加其他智能体的 AppKey
        // put("other_agent", "其他智能体的AppKey");
    }};
    
    /**
     * 根据智能体名称获取 AppKey
     * @param agentName 智能体名称（例如 "goods_classify"）
     * @return AppKey，如果不存在则返回默认的 AppKey
     */
    public static String getAppKey(String agentName) {
        if (agentName != null && !agentName.isEmpty() && AGENT_APP_KEYS.containsKey(agentName)) {
            return AGENT_APP_KEYS.get(agentName);
        }
        return AGENT_APP_KEY;  // 返回默认 AppKey
    }
    
    /**
     * 添加或更新智能体的 AppKey
     * @param agentName 智能体名称
     * @param appKey AppKey
     */
    public static void setAppKey(String agentName, String appKey) {
        AGENT_APP_KEYS.put(agentName, appKey);
    }
    
    /**
     * 获取所有已配置的智能体名称
     * @return 智能体名称列表
     */
    public static Map<String, String> getAllAgents() {
        return new HashMap<>(AGENT_APP_KEYS);
    }
}
