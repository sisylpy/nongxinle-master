package com.nongxinle.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单测试控制器
 */
@RequestMapping("api/hello")
public class HelloController {

    /**
     * 简单测试接口
     * 访问地址: http://localhost:8080/nongxinle_master_war_exploded/api/hello/test
     */
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Hello World!");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}


