package com.nongxinle.controller;

import com.nongxinle.utils.R;
import com.nongxinle.utils.WeworkFinanceSdkUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * SDK测试控制器
 * 用于测试企业微信会话存档SDK的集成情况
 * 
 * @author system
 * @date 2024-10-19
 */
@Controller
@RequestMapping("/api/sdk")
public class SdkTestController {

    @Autowired
    private WeworkFinanceSdkUtil weworkFinanceSdkUtil;

    /**
     * 测试SDK初始化
     */
    @RequestMapping(value = "/test-init", method = RequestMethod.GET)
    @ResponseBody
    public R testSdkInit(@RequestParam(defaultValue = "ww9778dea409045fe6") String corpId,
                        @RequestParam(defaultValue = "QYb4xvJnu47bCClSC5PgOqJQrpM4iRy_BJv0BpEiBZ4") String secret) {
        try {
            System.out.println("=== 测试SDK初始化 ===");
            System.out.println("企业ID: " + corpId);
            System.out.println("Secret: " + secret.substring(0, 10) + "...");
            
            boolean result = weworkFinanceSdkUtil.init(corpId, secret);
            
            if (result) {
                System.out.println("✅ SDK初始化成功");
                return R.ok().put("data", "SDK初始化成功");
            } else {
                System.out.println("❌ SDK初始化失败");
                return R.error("SDK初始化失败");
            }
            
        } catch (Exception e) {
            System.err.println("SDK初始化异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("SDK初始化异常: " + e.getMessage());
        }
    }

    /**
     * 测试native库加载
     */
    @RequestMapping(value = "/test-library", method = RequestMethod.GET)
    @ResponseBody
    public R testLibraryLoad() {
        try {
            System.out.println("=== 测试native库加载 ===");
            
            // 检查库文件是否存在
            String projectPath = System.getProperty("user.dir");
            String libPath = projectPath + "/lib/finance-sdk/native/linux/libWeWorkFinanceSdk_Java.so";
            
            java.io.File libFile = new java.io.File(libPath);
            if (!libFile.exists()) {
                return R.error("Native库文件不存在: " + libPath);
            }
            
            // 尝试加载库
            try {
                System.load(libPath);
                System.out.println("✅ Native库加载成功");
                return R.ok().put("data", "Native库加载成功");
            } catch (UnsatisfiedLinkError e) {
                System.err.println("❌ Native库加载失败: " + e.getMessage());
                return R.error("Native库加载失败: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("测试异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("测试异常: " + e.getMessage());
        }
    }

    /**
     * 测试SDK基本信息
     */
    @RequestMapping(value = "/test-info", method = RequestMethod.GET)
    @ResponseBody
    public R testSdkInfo() {
        try {
            System.out.println("=== 测试SDK基本信息 ===");
            
            java.util.Map<String, Object> info = new java.util.HashMap<>();
            info.put("osName", System.getProperty("os.name"));
            info.put("osArch", System.getProperty("os.arch"));
            info.put("javaVersion", System.getProperty("java.version"));
            info.put("userDir", System.getProperty("user.dir"));
            
            String libPath = System.getProperty("user.dir") + "/lib/finance-sdk/native/linux/libWeWorkFinanceSdk_Java.so";
            java.io.File libFile = new java.io.File(libPath);
            info.put("libPath", libPath);
            info.put("libExists", libFile.exists());
            info.put("libSize", libFile.exists() ? libFile.length() : 0);
            
            System.out.println("系统信息: " + info);
            
            return R.ok().put("data", info);
            
        } catch (Exception e) {
            System.err.println("获取信息异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取信息异常: " + e.getMessage());
        }
    }
}
