package com.nongxinle.controller;

import com.nongxinle.service.QyGbDisCorpMsgAuditService;
import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单测试控制器
 */
@RequestMapping("/api/test")
public class SimpleTestController {

    @Autowired
    private QyGbDisCorpMsgAuditService qyGbDisCorpMsgAuditService;

    /**
     * 测试数据库配置
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/test/config?corpId=ww9778dea409045fe6
     */
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> testConfig(@RequestParam String corpId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从数据库获取配置
            QyGbDisCorpMsgAuditEntity config = qyGbDisCorpMsgAuditService.queryByCorpId(corpId);
            
            if (config == null) {
                result.put("success", false);
                result.put("message", "未找到企业配置");
                return result;
            }
            
            result.put("success", true);
            result.put("corpId", config.getQyGbDisQyCorpId());
            result.put("token", config.getQyGbDisCorpMsgAuditToken());
            result.put("secret", config.getQyGbDisCorpMsgAuditSecret());
            result.put("privateKey", config.getQyGbDisCorpMsgAuditPrivateKey() != null ? "已配置" : "未配置");
            result.put("status", config.getQyGbDisCorpMsgAuditStatus());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "测试失败: " + e.getMessage());
        }
        
        return result;
    }
}
