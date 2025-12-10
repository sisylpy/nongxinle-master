package com.nongxinle.dto;

import java.util.List;
import java.util.Map;

/**
 * 批量更新阈值DTO
 *
 * @author lpy
 * @date 2025-10-15
 */
public class UpdateThresholdBatchDto {
    
    /**
     * 设备ID
     */
    private Integer deviceId;
    
    /**
     * 阈值配置列表
     * 每个元素包含：level, threshold, message, enable
     */
    private List<Map<String, Object>> thresholds;

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public List<Map<String, Object>> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<Map<String, Object>> thresholds) {
        this.thresholds = thresholds;
    }
}




