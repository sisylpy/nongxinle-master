package com.nongxinle.controller;

import com.nongxinle.dto.UpdateThresholdBatchDto;
import com.nongxinle.entity.NxMachineAlertThresholdEntity;
import com.nongxinle.service.NxMachineAlertThresholdService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 打印机系统 - 阈值管理Controller
 *
 * @author lpy
 * @date 2025-10-14
 */
@RestController
@RequestMapping("api/machine/threshold")
public class MachineThresholdController {

    @Autowired
    private NxMachineAlertThresholdService nxAlertThresholdService;

    /**
     * 查询设备的所有阈值配置
     * @param deviceId 设备ID
     * @return 阈值列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public R list(@RequestParam("deviceId") Integer deviceId) {
        try {
            List<NxMachineAlertThresholdEntity> list = nxAlertThresholdService.queryByDeviceId(deviceId);
            return R.ok().put("list", list);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 更新单个阈值配置
     * @param thresholdId 阈值ID
     * @param threshold 阈值数值
     * @param message 提醒消息（可选）
     * @param enable 是否启用（可选）
     * @return 操作结果
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public R update(@RequestParam("thresholdId") Integer thresholdId,
                   @RequestParam("threshold") Integer threshold,
                   @RequestParam(value = "message", required = false) String message,
                   @RequestParam(value = "enable", required = false) Integer enable) {
        try {
            NxMachineAlertThresholdEntity entity = nxAlertThresholdService.queryObject(thresholdId);
            if (entity == null) {
                return R.error("阈值配置不存在");
            }

            // 更新字段
            entity.setNxAtThreshold(threshold);
            if (message != null && !message.isEmpty()) {
                entity.setNxAtMessage(message);
            }
            if (enable != null) {
                entity.setNxAtEnable(enable);
            }

            nxAlertThresholdService.update(entity);
            return R.ok("更新成功");
        } catch (Exception e) {
            return R.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新设备的阈值配置
     * @param dto 请求DTO，包含deviceId和thresholds
     *            格式：{"deviceId":1, "thresholds":[{"level":1,"threshold":800,"message":"...","enable":1}, ...]}
     * @return 操作结果
     */
    @RequestMapping(value = "/updateBatch", method = RequestMethod.POST)
    public R updateBatch(@RequestBody UpdateThresholdBatchDto dto) {
        try {
            Integer deviceId = dto.getDeviceId();
            List<Map<String, Object>> thresholds = dto.getThresholds();
            
            if (deviceId == null) {
                return R.error("设备ID不能为空");
            }
            
            if (thresholds == null || thresholds.isEmpty()) {
                return R.error("阈值配置不能为空");
            }
            
            // 查询当前设备的所有阈值
            List<NxMachineAlertThresholdEntity> existingThresholds = 
                nxAlertThresholdService.queryByDeviceId(deviceId);

            if (existingThresholds.isEmpty()) {
                return R.error("设备阈值配置不存在");
            }

            // 批量更新
            for (Map<String, Object> item : thresholds) {
                Integer level = (Integer) item.get("level");
                Integer threshold = (Integer) item.get("threshold");
                String message = (String) item.get("message");
                Integer enable = (Integer) item.get("enable");

                // 查找对应级别的配置
                for (NxMachineAlertThresholdEntity entity : existingThresholds) {
                    if (entity.getNxAtLevel().equals(level)) {
                        entity.setNxAtThreshold(threshold);
                        if (message != null && !message.isEmpty()) {
                            entity.setNxAtMessage(message);
                        }
                        if (enable != null) {
                            entity.setNxAtEnable(enable);
                        }
                        nxAlertThresholdService.update(entity);
                        break;
                    }
                }
            }

            return R.ok("批量更新成功");
        } catch (Exception e) {
            return R.error("批量更新失败：" + e.getMessage());
        }
    }
}

