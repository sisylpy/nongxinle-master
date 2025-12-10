package com.nongxinle.controller;

import com.nongxinle.entity.NxMachineMarketManagerEntity;
import com.nongxinle.service.NxMachineMarketManagerService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 打印机系统 - 授权登录Controller
 *
 * @author lpy
 * @date 2025-10-14
 */
@RestController
@RequestMapping("api/machine/auth")
public class MachineAuthController {

    @Autowired
    private NxMachineMarketManagerService nxMarketManagerService;

    /**
     * 微信授权登录
     * @param code 微信授权码
     * @return 管理员信息
     */
    @RequestMapping(value = "/wxLogin", method = RequestMethod.POST)
    public R wxLogin(String code) {
        try {
            System.out.println("机器管理员登录 code: " + code);
            
            // ========== 选择登录方式 ==========
            // 方式一：真实微信登录（需要配置 WxMiniProgramUtil 中的 APPID 和 SECRET）
            NxMachineMarketManagerEntity manager = nxMarketManagerService.wxLogin(code);
            
            /* 方式二：开发测试 - 使用固定openid模拟登录（取消注释使用）
            String testOpenid = "test_openid_" + code.substring(0, Math.min(6, code.length()));
            NxMachineMarketManagerEntity manager = nxMarketManagerService.queryByOpenid(testOpenid);
            
            if (manager == null) {
                return R.error("管理员不存在，请先在数据库添加管理员数据");
            }
            nxMarketManagerService.updateLastLoginTime(manager.getNxMmId());
            */
            
            // ========== 返回登录结果 ==========

            return R.ok()
                .put("managerId", manager.getNxMmId())
                .put("marketId", manager.getNxMmMarketId())
                .put("nickname", manager.getNxMmWxNickname());
        } catch (Exception e) {
            return R.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取管理员信息
     * @param managerId 管理员ID
     * @return 管理员信息
     */
    @RequestMapping(value = "/getInfo", method = RequestMethod.GET)
    public R getInfo(@RequestParam("managerId") Integer managerId) {
        try {
            NxMachineMarketManagerEntity manager = nxMarketManagerService.queryObject(managerId);
            if (manager == null) {
                return R.error("管理员不存在");
            }
            return R.ok().put("manager", manager);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }
}

