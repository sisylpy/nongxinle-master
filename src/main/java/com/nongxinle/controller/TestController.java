package com.nongxinle.controller;

import com.nongxinle.service.QyWxCustomerGroupService;
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
 * 测试控制器 - 用于测试企业微信会话存档功能
 * 
 * @author lpy
 * @date 2025-01-11
 */
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private QyWxCustomerGroupService qyWxCustomerGroupService;
    
    @Autowired
    private QyGbDisCorpMsgAuditService qyGbDisCorpMsgAuditService;

    /**
     * 测试获取客户群列表
     * 访问地址: http://localhost:8080/api/test/customer-groups?corpId=ww9778dea409045fe6
     */
    @RequestMapping(value = "/customer-groups", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> testCustomerGroups(@RequestParam String corpId) {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("=== 测试获取客户群列表 ===");
            System.out.println("企业ID: " + corpId);
            
            // 调用服务获取客户群列表
//            Map<String, Object> groups = qyWxCustomerGroupService.getCustomerGroupList(corpId);
//
//            result.put("success", true);
//            result.put("message", "获取客户群列表成功");
//            result.put("corpId", corpId);
//            result.put("data", groups);
//
//            System.out.println("获取客户群列表结果: " + groups);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取客户群列表失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            System.err.println("获取客户群列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }


}
