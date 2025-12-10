package com.nongxinle.controller;

import com.nongxinle.entity.NxMachineDeviceManagerEntity;
import com.nongxinle.service.NxMachineDeviceManagerService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备责任人管理Controller
 *
 * @author lpy
 * @date 2025-10-16
 */
@RestController
@RequestMapping("api/machine/device/manager")
public class MachineDeviceManagerController {

    @Autowired
    private NxMachineDeviceManagerService deviceManagerService;

    /**
     * 查询设备的所有责任人（包含管理员详细信息）
     * @param deviceId 设备ID
     * @return 责任人列表（包含管理员姓名、电话等）
     */
    @RequestMapping(value = "/listByDevice", method = RequestMethod.GET)
    public R listByDevice(@RequestParam("deviceId") Integer deviceId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("deviceId", deviceId);
            
            // 使用带管理员信息的查询
            List<Map<String, Object>> list = deviceManagerService.queryListWithManager(params);
            
            return R.ok().put("list", list);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 为设备添加责任人
     * @param deviceId 设备ID
     * @param managerId 管理员ID
     * @param notifyLevel 通知级别（1-4，≥此级别才通知该管理员）
     * @param isPrimary 是否主要责任人（0-否 1-是，主要责任人接收所有级别的通知）
     * @return 操作结果
     */
    @RequestMapping(value = "/addManager", method = RequestMethod.POST)
    public R addManager(@RequestParam("deviceId") Integer deviceId,
                @RequestParam("managerId") Integer managerId,
                @RequestParam(value = "notifyLevel", defaultValue = "1") Integer notifyLevel,
                @RequestParam(value = "isPrimary", defaultValue = "0") Integer isPrimary) {
        try {
            // 检查是否已存在
            Map<String, Object> checkParams = new HashMap<>();
            checkParams.put("deviceId", deviceId);
            checkParams.put("managerId", managerId);
            List<NxMachineDeviceManagerEntity> existing = deviceManagerService.queryList(checkParams);
            
            if (!existing.isEmpty()) {
                return R.error("该管理员已经是此设备的责任人");
            }
            
            NxMachineDeviceManagerEntity entity = new NxMachineDeviceManagerEntity();
            entity.setNxDmDeviceId(deviceId);
            entity.setNxDmManagerId(managerId);
            entity.setNxDmNotifyLevel(notifyLevel);
            entity.setNxDmIsPrimary(isPrimary);
            entity.setNxDmEnable(1); // 默认启用
            
            deviceManagerService.save(entity);
            
            return R.ok("添加成功").put("id", entity.getNxDmId());
        } catch (Exception e) {
            return R.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新责任人配置
     * @param id 绑定ID
     * @param notifyLevel 通知级别（可选）
     * @param isPrimary 是否主要责任人（可选）
     * @param enable 是否启用（可选）
     * @return 操作结果
     */
    @RequestMapping(value = "/updateManager", method = RequestMethod.POST)
    public R updateManager(@RequestParam("id") Integer id,
                   @RequestParam(value = "notifyLevel", required = false) Integer notifyLevel,
                   @RequestParam(value = "isPrimary", required = false) Integer isPrimary,
                   @RequestParam(value = "enable", required = false) Integer enable) {
        try {
            NxMachineDeviceManagerEntity entity = deviceManagerService.queryObject(id);
            if (entity == null) {
                return R.error("记录不存在");
            }
            
            if (notifyLevel != null) {
                entity.setNxDmNotifyLevel(notifyLevel);
            }
            if (isPrimary != null) {
                entity.setNxDmIsPrimary(isPrimary);
            }
            if (enable != null) {
                entity.setNxDmEnable(enable);
            }
            
            deviceManagerService.update(entity);
            
            return R.ok("更新成功");
        } catch (Exception e) {
            return R.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除责任人
     * @param id 绑定ID
     * @return 操作结果
     */
    @RequestMapping(value = "/removeManager", method = RequestMethod.POST)
    public R removeManager(@RequestParam("id") Integer id) {
        try {
            deviceManagerService.delete(id);
            return R.ok("删除成功");
        } catch (Exception e) {
            return R.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 查询管理员负责的所有设备（包含设备详细信息）
     * @param managerId 管理员ID
     * @return 设备列表（包含设备名称、编号、位置等）
     */
    @RequestMapping(value = "/listDevicesByManager", method = RequestMethod.GET)
    public R listDevicesByManager(@RequestParam("managerId") Integer managerId) {
        try {
            // 调用已有的queryByManagerId方法，它已经包含了设备信息
            List<NxMachineDeviceManagerEntity> list = deviceManagerService.queryByManagerId(managerId);
            
            return R.ok().put("list", list);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }
}

