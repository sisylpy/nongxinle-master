package com.nongxinle.service;

import java.util.Map;

/**
 * 腾讯云智能体服务接口
 */
public interface TencentCloudAgentService {
    
    /**
     * 调用腾讯云智能体获取回复
     * @param userMessage 用户消息
     * @param sessionId 会话ID（使用群ID）
     * @return 智能体回复内容
     */
    String getAgentReply(String userMessage, String sessionId);
    
    /**
     * 调用腾讯云智能体获取回复（支持自定义参数）
     * @param userMessage 用户消息
     * @param sessionId 会话ID
     * @param customVariables 自定义参数（如 distributor_id）
     * @return 智能体回复内容
     */
    String getAgentReply(String userMessage, String sessionId, Map<String, String> customVariables);
    
    /**
     * 测试智能体连接
     * @return 是否连接成功
     */
    boolean testConnection();
}
