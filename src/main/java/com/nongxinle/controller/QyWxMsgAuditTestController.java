package com.nongxinle.controller;

import com.nongxinle.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 企业微信会话存档回调接口（测试版）
 * 
 * @author lpy
 * @date 2024-01-01
 */
public class QyWxMsgAuditTestController {

    /**
     * 简单的回调验证接口（GET）
     * 先测试基本连通性
     */
    @RequestMapping(value = "/test/callback", method = RequestMethod.GET)
    @ResponseBody
    public String testCallback(@RequestParam(name = "msg_signature") String msgSignature,
                              @RequestParam(name = "timestamp") String timestamp,
                              @RequestParam(name = "nonce") String nonce,
                              @RequestParam(name = "echostr") String echostr) {
        try {
            System.out.println("=== 测试回调验证 ===");
            System.out.println("msg_signature: " + msgSignature);
            System.out.println("timestamp: " + timestamp);
            System.out.println("nonce: " + nonce);
            System.out.println("echostr: " + echostr);
            
            // 暂时直接返回echostr，不进行解密验证
            System.out.println("=== 测试回调验证完成 ===");
            return echostr;
        } catch (Exception e) {
            System.err.println("测试回调验证失败: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * 测试接口
     */
    @RequestMapping(value = "/test/ping", method = RequestMethod.GET)
    @ResponseBody
    public R testPing() {
        return R.ok().put("message", "企业微信会话存档测试接口正常");
    }
}


