package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.service.TencentCloudAgentService;
import com.nongxinle.utils.TencentCloudAgentConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯云智能体服务实现
 */
@Slf4j
@Service
public class TencentCloudAgentServiceImpl implements TencentCloudAgentService {

    @Override
    public String getAgentReply(String userMessage, String sessionId) {
        return getAgentReply(userMessage, sessionId, null);
    }
    
    @Override
    public String getAgentReply(String userMessage, String sessionId, Map<String, String> customVariables) {
        try {
            log.info("调用腾讯云智能体 - 消息: {}, 会话: {}, 自定义参数: {}", userMessage, sessionId, customVariables);
            
            // 临时：返回模拟回复，用于测试代码逻辑
            if (false) {  // 关闭模拟回复，使用真实API
                return getMockReply(userMessage);
            }
            
            // 验证session_id格式（2-64位，匹配^[a-zA-Z0-9_-]{2,64}$）
            if (sessionId == null || !sessionId.matches("^[a-zA-Z0-9_-]{2,64}$")) {
                log.error("session_id格式错误: {}，必须为2-64位字母数字下划线横线组合", sessionId);
                return "session_id格式错误，必须为2-64位字母数字下划线横线组合";
            }
            
            // 构建ADP HTTP SSE接口请求参数（最小必填集）
            Map<String, Object> request = new HashMap<>();
            request.put("session_id", sessionId);  // 会话ID，2-64位
            request.put("bot_app_key", TencentCloudAgentConfig.AGENT_APP_KEY);  // 发布应用的AppKey
            request.put("visitor_biz_id", "visitor_" + sessionId);  // 访客ID，标识当前用户/会话
            request.put("content", userMessage);  // 消息内容
            request.put("stream", "disable");  // 先禁用流式，便于解析
            request.put("incremental", false);  // 非增量模式
            request.put("request_id", "req_" + System.currentTimeMillis());  // 请求ID，可能必需
            
            // 添加自定义参数（如果有）
            if (customVariables != null && !customVariables.isEmpty()) {
                request.put("custom_variables", customVariables);
            }
            
            String requestBody = JSONUtil.toJsonStr(request);
            log.info("ADP HTTP SSE请求参数: {}", requestBody);
            
            // 发送ADP HTTP SSE请求
            log.info("请求URL: {}", TencentCloudAgentConfig.API_URL);
            log.info("请求体: {}", requestBody);
            log.info("使用的AppKey: {}", TencentCloudAgentConfig.AGENT_APP_KEY);
            
            String response = HttpRequest.post(TencentCloudAgentConfig.API_URL)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")  // SSE支持
                    .body(requestBody)
                    .timeout(30000)  // 30秒超时
                    .execute()
                    .body();
            
            log.info("腾讯云智能体响应: {}", response);
            
            // 解析ADP响应
            return parseADPResponse(response);
            
        } catch (Exception e) {
            log.error("调用腾讯云智能体异常", e);
            return null;
        }
    }
    
    /**
     * 临时模拟回复方法
     */
    private String getMockReply(String userMessage) {
        log.info("使用模拟回复: {}", userMessage);
        
        // 简单的模拟回复逻辑
        if (userMessage.contains("你好") || userMessage.contains("hello")) {
            return "您好！我是智能客服，请问有什么可以帮助您的？";
        } else if (userMessage.contains("价格")) {
            return "关于价格信息，请联系我们的客服人员：400-123-4567";
        } else if (userMessage.contains("配送")) {
            return "我们支持全国配送，一般3-5个工作日送达。";
        } else {
            return "感谢您的咨询，我已记录您的问题，稍后会有专人联系您。";
        }
    }

    @Override
    public boolean testConnection() {
        try {
            String reply = getAgentReply("你好", "test_session");
            return reply != null && !reply.isEmpty();
        } catch (Exception e) {
            log.error("测试智能体连接失败", e);
            return false;
        }
    }
    
    
    
    /**
     * 解析SSE响应（工作流模式）
     */
    private String parseADPResponse(String sseResponse) {
        try {
            if (sseResponse == null || sseResponse.trim().isEmpty()) {
                return null;
            }

            log.debug("收到ADP HTTP SSE响应: {}", sseResponse);
            
            // 检查是否返回HTML（错误网关）
            if (sseResponse.trim().startsWith("<html>") || sseResponse.trim().startsWith("<!DOCTYPE")) {
                log.error("网关错误：返回HTML页面，域名/路径可能错误");
                return "网关错误：请检查接口地址配置";
            }

            // 解析SSE格式：event: / data: 逐行解析
            String[] lines = sseResponse.split("\n");
            String workflowResult = null;
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                
                // 处理错误事件
                if (line.startsWith("event:error")) {
                    if (i + 1 < lines.length) {
                        String dataLine = lines[i + 1].trim();
                        if (dataLine.startsWith("data:")) {
                            String jsonData = dataLine.substring(5).trim();
                            try {
                                JSONObject errorData = JSON.parseObject(jsonData);
                                String errorCode = errorData.getString("code");
                                String errorMsg = errorData.getString("message");
                                log.error("ADP SSE返回错误: code={}, message={}", errorCode, errorMsg);
                                return null;
        } catch (Exception e) {
                                log.error("解析错误事件失败: {}", jsonData, e);
                            }
                        }
                    }
                }
                
                // 处理回复事件（工作流模式）
                if (line.startsWith("event:reply")) {
                    if (i + 1 < lines.length) {
                        String dataLine = lines[i + 1].trim();
                        if (dataLine.startsWith("data:")) {
                            String jsonData = dataLine.substring(5).trim();
                            try {
                                JSONObject replyData = JSON.parseObject(jsonData);
                                JSONObject payload = replyData.getJSONObject("payload");
                                
                                // 忽略回声（is_from_self=true）
                                if (payload != null && payload.getBooleanValue("is_from_self")) {
                                    log.debug("忽略回声消息");
                                    continue;
                                }
                                
                                // 只处理 is_final=true 的消息
                                if (payload != null && payload.getBooleanValue("is_final")) {
                                    // 优先读取 work_flow.current_node.Output
                                    JSONObject workFlow = payload.getJSONObject("work_flow");
                                    if (workFlow != null) {
                                        JSONObject currentNode = workFlow.getJSONObject("current_node");
                                        if (currentNode != null) {
                                            String output = currentNode.getString("Output");
                                            if (output != null && !output.isEmpty() && !output.equals("{}")) {
                                                workflowResult = output;
                                                log.debug("从work_flow.current_node.Output获取结果: {}", output);
                                            }
                                        }
                                    }
                                    
                                    // 如果 work_flow.current_node.Output 为空，尝试读取 content
                                    if (workflowResult == null || workflowResult.isEmpty() || workflowResult.equals("{}")) {
                                        String content = payload.getString("content");
                                        if (content != null && !content.isEmpty()) {
                                            workflowResult = content;
                                            log.debug("从content获取结果: {}", content);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("解析回复事件失败: {}", jsonData, e);
                            }
                        }
                    }
                }
                
                // 处理 token_stat 事件，从调试信息中提取工作流节点输出
                if (line.startsWith("event:token_stat")) {
                    if (i + 1 < lines.length) {
                        String dataLine = lines[i + 1].trim();
                        if (dataLine.startsWith("data:")) {
                            String jsonData = dataLine.substring(5).trim();
                            try {
                                JSONObject tokenData = JSON.parseObject(jsonData);
                                JSONObject payload = tokenData.getJSONObject("payload");
                                if (payload != null) {
                                    JSONArray procedures = payload.getJSONArray("procedures");
                                    if (procedures != null && procedures.size() > 0) {
                                        JSONObject procedure = procedures.getJSONObject(0);
                                        JSONObject debugging = procedure.getJSONObject("debugging");
                                        if (debugging != null) {
                                            JSONObject wf = debugging.getJSONObject("work_flow");
                                            if (wf != null) {
                                                JSONArray runNodes = wf.getJSONArray("run_nodes");
                                                if (runNodes != null) {
                                                    // 查找参数提取节点的 output
                                                    for (int j = 0; j < runNodes.size(); j++) {
                                                        JSONObject node = runNodes.getJSONObject(j);
                                                        int nodeType = node.getIntValue("node_type");
                                                        String output = node.getString("output");
                                                        // node_type=2 是参数提取节点
                                                        if (nodeType == 2 && output != null && !output.isEmpty() && !output.equals("{}")) {
                                                            workflowResult = output;
                                                            log.debug("从token_stat的参数提取节点获取结果: {}", output);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.debug("解析token_stat事件时出错，忽略: {}", e.getMessage());
                            }
                        }
                    }
                }
            }

            if (workflowResult != null && !workflowResult.isEmpty()) {
                log.info("解析Agent结果: {}", workflowResult);
                return workflowResult;
            }

            log.warn("ADP HTTP SSE响应中没有找到工作流结果");
            return null;
            
        } catch (Exception e) {
            log.error("解析ADP HTTP SSE响应异常", e);
            return null;
        }
    }
    
}
