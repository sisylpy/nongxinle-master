package com.nongxinle.service;

import com.nongxinle.entity.NxMachineDeviceManagerEntity;

import java.util.List;
import java.util.Map;

/**
 * 设备责任人绑定Service
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface NxMachineDeviceManagerService {

    NxMachineDeviceManagerEntity queryObject(Integer nxDmId);

    List<NxMachineDeviceManagerEntity> queryList(Map<String, Object> map);

    /**
     * 查询列表（包含管理员详细信息）
     */
    List<Map<String, Object>> queryListWithManager(Map<String, Object> map);

    int queryTotal(Map<String, Object> map);

    void save(NxMachineDeviceManagerEntity nxDeviceManager);

    void update(NxMachineDeviceManagerEntity nxDeviceManager);

    void delete(Integer nxDmId);

    void deleteBatch(Integer[] nxDmIds);

    /**
     * 根据设备ID查询责任人列表
     */
    List<NxMachineDeviceManagerEntity> queryByDeviceId(Integer deviceId);

    /**
     * 根据管理员ID查询负责的设备列表
     */
    List<NxMachineDeviceManagerEntity> queryByManagerId(Integer managerId);

    /**
     * 根据设备ID和提醒级别查询需要通知的责任人
     */
    List<NxMachineDeviceManagerEntity> queryNotifyManagers(Integer deviceId, Integer alertLevel);

    /**
     * 绑定设备责任人
     */
    void bindDeviceManager(Integer deviceId, Integer managerId, Integer notifyLevel, Integer isPrimary);

    /**
     * 解绑设备责任人
     */
    void unbindDeviceManager(Integer deviceId, Integer managerId);
}

