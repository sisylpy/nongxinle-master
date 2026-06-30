package com.nongxinle.controller;

import com.nongxinle.service.QyWxCustomerGroupService;
import com.nongxinle.service.QyGbDisCorpMsgAuditService;
import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import com.nongxinle.service.impl.CustomerGroupDiscoveryService;
import com.nongxinle.utils.R;
import com.nongxinle.utils.QyWxTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 企业微信客户群管理控制器
 * 
 * @author lpy
 * @date 2024-01-01
 */
@RestController
@RequestMapping("api/qywx/customergroup")
public class QyWxCustomerGroupController {

    @Autowired
    private QyWxCustomerGroupService customerGroupService;
    
    @Autowired
    private QyGbDisCorpMsgAuditService qyGbDisCorpMsgAuditService;
    
    @Autowired
    private CustomerGroupDiscoveryService customerGroupDiscoveryService;
    
    @Autowired
    private QyWxTokenUtil qyWxTokenUtil;



    /**
     * 获取企业微信客户群列表
     * @param corpId 企业ID
     * @param statusFilter 群状态过滤
     * @param offset 分页偏移
     * @param limit 每页数量
     * @return 客户群列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public R getGroupList(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId,
                          @RequestParam(required = false) Integer statusFilter,
                          @RequestParam(defaultValue = "0") Integer offset,
                          @RequestParam(defaultValue = "100") Integer limit) {
        try {
            System.out.println("获取客户群列表 - corpId: " + corpId);
            List<Map<String, Object>> groupList = customerGroupService.getCustomerGroupList(
                corpId, statusFilter, offset, limit);
            return R.ok().put("list", groupList).put("total", groupList.size());
        } catch (Exception e) {
            System.err.println("获取客户群列表失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取客户群列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户群详情
     * @param corpId 企业ID
     * @param chatId 客户群ID
     * @return 客户群详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public R getGroupDetail(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId,
                           @RequestParam String chatId) {
        try {
            System.out.println("获取客户群详情 - corpId: " + corpId + ", chatId: " + chatId);
            Map<String, Object> detail = customerGroupService.getCustomerGroupDetail(corpId, chatId);
            return R.ok().put("detail", detail);
        } catch (Exception e) {
            System.err.println("获取客户群详情失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取客户群详情失败: " + e.getMessage());
        }
    }

    /**
     * 添加监控的客户群
     * @param corpId 企业ID
     * @param chatId 客户群ID
     * @param chatName 客户群名称
     * @return 操作结果
     */
    @RequestMapping(value = "/monitor/add", method = RequestMethod.POST)
    @ResponseBody
    public R addMonitoredGroup(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId,
                              @RequestParam String chatId,
                              @RequestParam String chatName) {
        try {
            System.out.println("添加监控群 - corpId: " + corpId + ", chatId: " + chatId);
            boolean success = customerGroupService.saveMonitoredGroup(corpId, chatId, chatName);
            if (success) {
                return R.ok("添加成功");
            } else {
                return R.error("添加失败");
            }
        } catch (Exception e) {
            System.err.println("添加监控群失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("添加失败: " + e.getMessage());
        }
    }

    /**
     * 获取已监控的客户群列表
     * @param corpId 企业ID
     * @return 已监控的客户群列表
     */
    @RequestMapping(value = "/monitor/list", method = RequestMethod.GET)
    @ResponseBody
    public R getMonitoredGroups(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId) {
        try {
            System.out.println("获取已监控群列表 - corpId: " + corpId);
            List<Map<String, Object>> list = customerGroupService.getMonitoredGroups(corpId);
            return R.ok().put("list", list).put("total", list.size());
        } catch (Exception e) {
            System.err.println("获取已监控群列表失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 移除监控的客户群
     * @param corpId 企业ID
     * @param chatId 客户群ID
     * @return 操作结果
     */
    @RequestMapping(value = "/monitor/remove", method = RequestMethod.POST)
    @ResponseBody
    public R removeMonitoredGroup(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId,
                                 @RequestParam String chatId) {
        try {
            System.out.println("移除监控群 - corpId: " + corpId + ", chatId: " + chatId);
            boolean success = customerGroupService.removeMonitoredGroup(corpId, chatId);
            if (success) {
                return R.ok("移除成功");
            } else {
                return R.error("移除失败");
            }
        } catch (Exception e) {
            System.err.println("移除监控群失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("移除失败: " + e.getMessage());
        }
    }

    /**
     * 测试接口 - 检查配置
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/qywx/customergroup/test
     */
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public R testConfig(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId) {
        try {
            System.out.println("=== 测试配置 ===");
            System.out.println("企业ID: " + corpId);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("corpId", corpId);
            result.put("message", "测试成功");
            result.put("timestamp", System.currentTimeMillis());
            
            return R.ok().put("data", result);
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试数据库配置
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/qywx/customergroup/db-test
     */
    @RequestMapping(value = "/db-test", method = RequestMethod.GET)
    @ResponseBody
    public R testDatabase(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId) {
        try {
            System.out.println("=== 测试数据库配置 ===");
            System.out.println("企业ID: " + corpId);
            
            // 从数据库获取配置
            QyGbDisCorpMsgAuditEntity config = qyGbDisCorpMsgAuditService.queryByCorpId(corpId);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("corpId", corpId);
            
            if (config == null) {
                result.put("message", "未找到企业配置");
                result.put("configExists", false);
                return R.error("未找到企业配置").put("data", result);
            }
            
            result.put("message", "配置获取成功");
            result.put("configExists", true);
            result.put("token", config.getQyGbDisCorpMsgAuditToken());
            result.put("secretConfigured", !(config.getQyGbDisCorpMsgAuditSecret() == null || 
                config.getQyGbDisCorpMsgAuditSecret().equals("您的会话存档应用Secret") ||
                config.getQyGbDisCorpMsgAuditSecret().equals("YOUR_SECRET_HERE")));
            result.put("privateKeyConfigured", config.getQyGbDisCorpMsgAuditPrivateKey() != null);
            result.put("status", config.getQyGbDisCorpMsgAuditStatus());
            result.put("timestamp", System.currentTimeMillis());
            
            return R.ok().put("data", result);
        } catch (Exception e) {
            System.err.println("测试数据库失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("测试数据库失败: " + e.getMessage());
        }
    }

    /**
     * 测试获取access_token（已禁用：防止泄露 access_token）
     */
    @RequestMapping(value = "/token-test", method = RequestMethod.GET)
    @ResponseBody
    public R testAccessToken(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId) {
        return R.error("此接口已禁用，不可返回 access_token");
    }

    /**
     * 测试获取用户信息
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/qywx/customergroup/user-test?userId=testuser
     */
    @RequestMapping(value = "/user-test", method = RequestMethod.GET)
    @ResponseBody
    public R testGetUser(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId,
                        @RequestParam(defaultValue = "testuser") String userId) {
        try {
            System.out.println("=== 测试获取用户信息 ===");
            System.out.println("企业ID: " + corpId);
            System.out.println("用户ID: " + userId);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("corpId", corpId);
            result.put("userId", userId);
            
            // 获取access_token
            String accessToken = customerGroupService.getAccessToken(corpId);
            
            if (accessToken == null || accessToken.isEmpty()) {
                result.put("message", "获取access_token失败，无法调用用户信息接口");
                result.put("accessToken", null);
                result.put("success", false);
                result.put("timestamp", System.currentTimeMillis());
                return R.error("获取access_token失败").put("data", result);
            }
            
            // 调用企业微信API获取用户信息
            String url = "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=" + accessToken + "&userid=" + userId;
            System.out.println("请求用户信息，corpId=" + corpId + ", userId=" + userId);
            
            String response = com.nongxinle.utils.WeChatUtil.httpRequest(url, "GET", null);
            System.out.println("用户信息响应: " + response);
            
            result.put("message", "调用用户信息接口成功");
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());
            
            return R.ok().put("data", result);
            
        } catch (Exception e) {
            System.err.println("测试获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("测试获取用户信息失败: " + e.getMessage()).put("error", e.getClass().getSimpleName());
        }
    }

    /**
     * 获取用户列表
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/qywx/customergroup/users?departmentId=2
     * https://grainservice.club:8443/nongxinle/api/qywx/customergroup/users?departmentId=2
     */
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ResponseBody
    public R getUserList(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId,
                        @RequestParam(defaultValue = "1") Integer departmentId,
                        @RequestParam(defaultValue = "0") Integer fetchChild) {
        try {
            System.out.println("=== 获取用户列表 ===");
            System.out.println("企业ID: " + corpId);
            System.out.println("部门ID: " + departmentId);
            System.out.println("是否递归获取子部门: " + fetchChild);
            
            // 获取access_token
            String accessToken = customerGroupService.getAccessToken(corpId);
            
            if (accessToken == null || accessToken.isEmpty()) {
                return R.error("获取access_token失败");
            }
            
            // 调用企业微信API获取用户列表
            String url = "https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token=" + accessToken 
                        + "&department_id=" + departmentId + "&fetch_child=" + fetchChild;
            System.out.println("请求用户列表，departmentId=" + departmentId);
            
            String response = com.nongxinle.utils.WeChatUtil.httpRequest(url, "GET", null);
            System.out.println("用户列表响应: " + response);
            
            return R.ok().put("data", response);
            
        } catch (Exception e) {
            System.err.println("获取用户列表失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取部门列表
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/qywx/customergroup/departments
     */
    @RequestMapping(value = "/departments", method = RequestMethod.GET)
    @ResponseBody
    public R getDepartmentList(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId) {
        try {
            System.out.println("=== 获取部门列表 ===");
            System.out.println("企业ID: " + corpId);
            
            // 获取access_token
            String accessToken = customerGroupService.getAccessToken(corpId);
            
            if (accessToken == null || accessToken.isEmpty()) {
                return R.error("获取access_token失败");
            }
            
            // 调用企业微信API获取部门列表
            String url = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token=" + accessToken;
            System.out.println("请求部门列表，corpId=" + corpId);
            
            String response = com.nongxinle.utils.WeChatUtil.httpRequest(url, "GET", null);
            System.out.println("部门列表响应: " + response);
            
            return R.ok().put("data", response);
            
        } catch (Exception e) {
            System.err.println("获取部门列表失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取部门列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查服务器出口IP
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/qywx/customergroup/check-ip
     */
    @RequestMapping(value = "/check-ip", method = RequestMethod.GET)
    @ResponseBody
    public R checkServerIp() {
        try {
            System.out.println("=== 检查服务器出口IP ===");
            
            // 调用一个返回IP的API
            String url = "https://api.ipify.org?format=json";
            String response = com.nongxinle.utils.WeChatUtil.httpRequest(url, "GET", null);
            System.out.println("IP检查响应: " + response);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("ipCheckResponse", response);
            result.put("message", "请查看服务器的实际出口IP");
            
            return R.ok().put("data", result);
            
        } catch (Exception e) {
            System.err.println("检查IP失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("检查IP失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发群聊发现任务
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/qywx/customergroup/discover-groups
     */
    @RequestMapping(value = "/discover-groups", method = RequestMethod.POST)
    @ResponseBody
    public R discoverGroups(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId) {
        try {
            System.out.println("=== 手动触发群聊发现任务 ===");
            System.out.println("企业ID: " + corpId);
            
            // 调用群聊发现服务
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("corpId", corpId);
            result.put("message", "群聊发现任务已触发");
            result.put("discoveredCount", 0);
            result.put("status", "processing");
            result.put("timestamp", System.currentTimeMillis());
            
            // 调用发现服务
            try {
                int discoveredCount = customerGroupDiscoveryService.discoverGroupsFromMsgAudit(corpId);
                result.put("discoveredCount", discoveredCount);
                result.put("status", "success");
                result.put("message", "群聊发现完成");
            } catch (Exception e) {
                System.err.println("群聊发现服务调用异常: " + e.getMessage());
                result.put("status", "error");
                result.put("error", e.getMessage());
                result.put("message", "群聊发现失败");
            }
            
            return R.ok().put("data", result);
        } catch (Exception e) {
            System.err.println("触发群聊发现任务异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("触发失败: " + e.getMessage());
        }
    }

    /**
     * 简单测试群聊发现功能
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/qywx/customergroup/test-discovery
     */
    @RequestMapping(value = "/test-discovery", method = RequestMethod.GET)
    @ResponseBody
    public R testDiscovery(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId) {
        try {
            System.out.println("=== 测试群聊发现功能 ===");
            System.out.println("企业ID: " + corpId);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("corpId", corpId);
            result.put("message", "群聊发现功能测试");
            result.put("status", "ready");
            result.put("timestamp", System.currentTimeMillis());
            
            // 检查配置
            QyGbDisCorpMsgAuditEntity config = qyGbDisCorpMsgAuditService.queryByCorpId(corpId);
            if (config != null) {
                result.put("configStatus", "已配置");
                result.put("secretConfigured", config.getQyGbDisCorpMsgAuditSecret() != null);
                result.put("privateKeyConfigured", config.getQyGbDisCorpMsgAuditPrivateKey() != null);
            } else {
                result.put("configStatus", "未配置");
            }
            
            return R.ok().put("data", result);
        } catch (Exception e) {
            System.err.println("测试群聊发现功能异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 最简单的群聊发现测试
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/qywx/customergroup/simple-test
     */
    @RequestMapping(value = "/simple-test", method = RequestMethod.GET)
    @ResponseBody
    public R simpleTest() {
        try {
            System.out.println("=== 最简单的群聊发现测试 ===");
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("message", "最简单的测试接口");
            result.put("status", "success");
            result.put("timestamp", System.currentTimeMillis());
            
            return R.ok().put("data", result);
        } catch (Exception e) {
            System.err.println("简单测试异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取已发现的群聊列表（通过会话存档发现的）
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/qywx/customergroup/discovered-groups
     */
    @RequestMapping(value = "/discovered-groups", method = RequestMethod.GET)
    @ResponseBody
    public R getDiscoveredGroups(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId) {
        try {
            System.out.println("=== 获取已发现的群聊列表 ===");
            System.out.println("企业ID: " + corpId);
            
            // 提示：群聊信息在日志中查看
            System.out.println("提示：调用 /discover-groups 接口后，在日志中查看发现的群聊信息");
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("corpId", corpId);
            result.put("message", "请查看日志中的群聊发现信息");
            result.put("note", "群聊信息已输出到catalina.out日志文件");
            result.put("timestamp", System.currentTimeMillis());
            
            return R.ok().put("data", result);
        } catch (Exception e) {
            System.err.println("获取已发现群聊列表异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 测试会话存档功能
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/qywx/customergroup/test-msgaudit
     */
    @RequestMapping(value = "/test-msgaudit", method = RequestMethod.GET)
    @ResponseBody
    public R testMsgAudit(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId) {
        try {
            System.out.println("=== 测试会话存档功能 ===");
            System.out.println("企业ID: " + corpId);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("corpId", corpId);
            result.put("message", "会话存档功能测试");
            result.put("status", "ready");
            result.put("timestamp", System.currentTimeMillis());
            
            // 检查配置
            QyGbDisCorpMsgAuditEntity config = qyGbDisCorpMsgAuditService.queryByCorpId(corpId);
            if (config != null) {
                result.put("configStatus", "已配置");
                result.put("secretConfigured", config.getQyGbDisCorpMsgAuditSecret() != null);
                result.put("privateKeyConfigured", config.getQyGbDisCorpMsgAuditPrivateKey() != null);
            } else {
                result.put("configStatus", "未配置");
            }
            
            result.put("instructions", "请在外部客户群中发送消息来测试会话存档功能");
            result.put("callbackUrl", "https://grainservice.club:8443/nongxinle/api/qywx/msgaudit/callback");
            
            return R.ok().put("data", result);
        } catch (Exception e) {
            System.err.println("测试会话存档功能异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试会话存档Secret配置（已禁用：防止泄露 secret/token/aesKey）
     * 如需临时开启调试，请修改 application.properties: ygt.wecom.test-endpoints.enabled=true
     */
    @RequestMapping(value = "/secret-test", method = RequestMethod.GET)
    @ResponseBody
    public R testSecretConfig(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId) {
        return R.error("此接口已禁用，不可返回 secret/token/aesKey");
    }

    /**
     * 测试发送消息到群
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/qywx/customergroup/test-send-message
     */
    @RequestMapping(value = "/test-send-message", method = RequestMethod.POST)
    @ResponseBody
    public R testSendMessage(@RequestParam String roomId, 
                            @RequestParam(defaultValue = "这是一条测试消息") String content,
                            @RequestParam(defaultValue = "ww9778dea409045fe6") String corpId) {
        try {
            System.out.println("=== 测试发送消息到群 ===");
            System.out.println("群ID: " + roomId);
            System.out.println("消息内容: " + content);
            System.out.println("企业ID: " + corpId);
            
            // 1. 获取access_token
            String accessToken = qyWxTokenUtil.getAccessToken(corpId);
            if (accessToken == null || accessToken.isEmpty()) {
                return R.error("获取access_token失败");
            }
            System.out.println("获取access_token成功");
            
            // 2. 构建发送消息的API请求
            String url = "https://qyapi.weixin.qq.com/cgi-bin/appchat/send?access_token=" + accessToken;
            
            // 3. 构建消息体
            Map<String, Object> params = new java.util.HashMap<>();
            params.put("chatid", roomId);
            params.put("msgtype", "text");
            
            Map<String, String> textContent = new java.util.HashMap<>();
            textContent.put("content", content);
            params.put("text", textContent);
            
            String jsonParams = com.alibaba.fastjson.JSON.toJSONString(params);
            System.out.println("📤 发送请求: " + jsonParams);
            
            // 4. 发送请求
            String response = com.nongxinle.utils.WeChatUtil.httpRequest(url, "POST", jsonParams);
            System.out.println("📥 响应结果: " + response);
            
            // 5. 解析响应
            com.alibaba.fastjson.JSONObject responseJson = com.alibaba.fastjson.JSON.parseObject(response);
            int errcode = responseJson.getIntValue("errcode");
            String errmsg = responseJson.getString("errmsg");
            
            if (errcode == 0) {
                System.out.println("✅ 消息发送成功！");
                return R.ok("消息发送成功")
                    .put("roomId", roomId)
                    .put("content", content)
                    .put("response", response);
            } else {
                System.err.println("❌ 消息发送失败: errcode=" + errcode + ", errmsg=" + errmsg);
                return R.error("消息发送失败: " + errmsg)
                    .put("errcode", errcode)
                    .put("errmsg", errmsg);
            }
            
        } catch (Exception e) {
            System.err.println("发送消息异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("发送消息异常: " + e.getMessage());
        }
    }
}

