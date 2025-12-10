package com.nongxinle.service;

import com.nongxinle.entity.NxMachineAlertThresholdEntity;

import java.util.List;
import java.util.Map;

/**
 * 提醒阈值配置Service
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface NxMachineAlertThresholdService {

    NxMachineAlertThresholdEntity queryObject(Integer nxAtId);

    List<NxMachineAlertThresholdEntity> queryList(Map<String, Object> map);

    int queryTotal(Map<String, Object> map);

    void save(NxMachineAlertThresholdEntity nxAlertThreshold);

    void update(NxMachineAlertThresholdEntity nxAlertThreshold);

    void delete(Integer nxAtId);

    void deleteBatch(Integer[] nxAtIds);

    /**
     * 根据设备ID查询阈值配置列表
     */
    List<NxMachineAlertThresholdEntity> queryByDeviceId(Integer deviceId);

    /**
     * 查询触发的阈值配置
     * @param deviceId 设备ID
     * @param paperCount 当前纸张数量
     * @return 触发的阈值配置列表
     */
    List<NxMachineAlertThresholdEntity> queryTriggeredThresholds(Integer deviceId, Integer paperCount);

    /**
     * 初始化设备的默认阈值配置
     * @param deviceId 设备ID
     */
    void initDefaultThresholds(Integer deviceId);
}

