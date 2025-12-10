package com.nongxinle.controller;

import com.nongxinle.entity.NxMachinePrinterDeviceEntity;
import com.nongxinle.service.NxMachineMarketManagerService;
import com.nongxinle.service.NxMachinePrinterDeviceService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 打印软件专用Controller
 * 提供打印软件所需的登录和配置接口
 *
 * @author lpy
 * @date 2025-10-15
 */
@RestController
@RequestMapping("api/machine/printSoftware")
public class MachinePrintSoftwareController {

    @Autowired
    private NxMachineMarketManagerService nxMarketManagerService;

    @Autowired
    private NxMachinePrinterDeviceService nxPrinterDeviceService;

    /**
     * 打印软件登录（通过手机号）
     * @param phone 手机号
     * @return 登录结果（包含管理员信息和市场信息）
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public R login(@RequestParam("phone") String phone) {
        try {
            // 验证手机号格式
            if (phone == null || phone.trim().isEmpty()) {
                return R.error("手机号不能为空");
            }
            
            // 简单的手机号格式验证
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return R.error("手机号格式不正确");
            }
            
            // 调用登录服务
            Map<String, Object> result = nxMarketManagerService.printSoftwareLogin(phone);
            
            Boolean success = (Boolean) result.get("success");
            if (success) {
                return R.ok()
                    .put("managerId", result.get("managerId"))
                    .put("managerName", result.get("managerName"))
                    .put("phone", result.get("phone"))
                    .put("marketId", result.get("marketId"))
                    .put("marketName", result.get("marketName"));
            } else {
                return R.error((String) result.get("message"));
            }
            
        } catch (Exception e) {
            return R.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取管理员所属市场的设备列表
     * @param marketId 市场ID
     * @return 设备列表
     */
    @RequestMapping(value = "/devices", method = RequestMethod.GET)
    public R getDevices(@RequestParam("marketId") Integer marketId) {
        try {
            List<NxMachinePrinterDeviceEntity> list = nxPrinterDeviceService.queryByMarketId(marketId);
            
            // 过滤出正常状态的设备（可选）
            // list = list.stream()
            //     .filter(d -> d.getNxPdStatus() == 1)
            //     .collect(Collectors.toList());
            
            return R.ok().put("list", list);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 验证手机号是否存在
     * @param phone 手机号
     * @return 验证结果
     */
    @RequestMapping(value = "/checkPhone", method = RequestMethod.GET)
    public R checkPhone(@RequestParam("phone") String phone) {
        try {
            Map<String, Object> result = nxMarketManagerService.printSoftwareLogin(phone);
            Boolean success = (Boolean) result.get("success");
            
            if (success) {
                return R.ok("手机号已注册")
                    .put("marketName", result.get("marketName"));
            } else {
                return R.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return R.error("验证失败：" + e.getMessage());
        }
    }
}

