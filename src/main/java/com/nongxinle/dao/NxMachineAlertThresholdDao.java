package com.nongxinle.dao;

import com.nongxinle.entity.NxMachineAlertThresholdEntity;

import java.util.List;
import java.util.Map;

/**
 * 提醒阈值配置Dao
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface NxMachineAlertThresholdDao extends BaseDao<NxMachineAlertThresholdEntity> {

    /**
     * 根据设备ID查询阈值配置列表
     * @param deviceId 设备ID
     * @return 阈值配置列表
     */
    List<NxMachineAlertThresholdEntity> queryByDeviceId(Integer deviceId);

    /**
     * 根据设备ID和级别查询阈值配置
     * @param map 包含deviceId和level的参数map
     * @return 阈值配置实体
     */
    NxMachineAlertThresholdEntity queryByDeviceIdAndLevel(Map<String, Object> map);

    /**
     * 查询触发的阈值配置（纸张数量小于等于阈值的配置）
     * @param map 包含deviceId和paperCount的参数map
     * @return 触发的阈值配置列表
     */
    List<NxMachineAlertThresholdEntity> queryTriggeredThresholds(Map<String, Object> map);

    /**
     * 批量初始化设备的默认阈值配置
     * @param list 阈值配置列表
     */
    void saveBatchThresholds(List<NxMachineAlertThresholdEntity> list);
}

