package com.nongxinle.controller;

import com.nongxinle.service.TencentCloudAgentService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 腾讯云智能体测试控制器
 */
@Controller
@RequestMapping("/api/tencent-agent")
public class TencentAgentTestController {
    
    @Autowired
    private TencentCloudAgentService tencentCloudAgentService;
    
    /**
     * 测试智能体连接
     */
    @RequestMapping(value = "/test-connection", method = RequestMethod.GET)
    @ResponseBody
    public R testConnection() {
        try {
            boolean connected = tencentCloudAgentService.testConnection();
            if (connected) {
                return R.ok().put("message", "智能体连接成功");
            } else {
                return R.error("智能体连接失败");
            }
        } catch (Exception e) {
            return R.error("测试异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试智能体聊天（GET方法，用于浏览器测试）
     */
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public R testChatGet(@RequestParam(defaultValue = "你好") String message, 
                        @RequestParam(defaultValue = "test_session") String sessionId) {
        try {
            String reply = tencentCloudAgentService.getAgentReply(message, sessionId);
            if (reply != null) {
                return R.ok()
                    .put("message", "智能体回复成功")
                    .put("reply", reply)
                    .put("userMessage", message)
                    .put("sessionId", sessionId)
                    .put("timestamp", System.currentTimeMillis());
            } else {
                return R.error("智能体没有返回回复");
            }
        } catch (Exception e) {
            return R.error("聊天测试异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试智能体回复（POST方法）
     */
    @RequestMapping(value = "/chat", method = RequestMethod.POST)
    @ResponseBody
    public R testChat(@RequestParam String message, 
                     @RequestParam(defaultValue = "test_session") String sessionId) {
        try {
            String reply = tencentCloudAgentService.getAgentReply(message, sessionId);
            if (reply != null) {
                return R.ok()
                    .put("message", "智能体回复成功")
                    .put("userMessage", message)
                    .put("agentReply", reply);
            } else {
                return R.error("智能体回复失败");
            }
        } catch (Exception e) {
            return R.error("聊天测试异常: " + e.getMessage());
        }
    }
    
    
    
    
}
