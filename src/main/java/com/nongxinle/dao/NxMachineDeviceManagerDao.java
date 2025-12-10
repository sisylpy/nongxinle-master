package com.nongxinle.dao;

import com.nongxinle.entity.NxMachineDeviceManagerEntity;

import java.util.List;
import java.util.Map;

/**
 * 设备责任人绑定Dao
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface NxMachineDeviceManagerDao extends BaseDao<NxMachineDeviceManagerEntity> {

    /**
     * 根据设备ID查询责任人列表
     * @param deviceId 设备ID
     * @return 责任人绑定列表
     */
    List<NxMachineDeviceManagerEntity> queryByDeviceId(Integer deviceId);

    /**
     * 根据管理员ID查询负责的设备列表
     * @param managerId 管理员ID
     * @return 责任人绑定列表
     */
    List<NxMachineDeviceManagerEntity> queryByManagerId(Integer managerId);

    /**
     * 查询列表（包含管理员详细信息）
     * @param map 查询参数
     * @return 责任人列表（包含管理员姓名、电话等）
     */
    List<Map<String, Object>> queryListWithManager(Map<String, Object> map);

    /**
     * 根据设备ID和提醒级别查询需要通知的责任人
     * @param map 包含deviceId和alertLevel的参数map
     * @return 需要通知的责任人列表
     */
    List<NxMachineDeviceManagerEntity> queryNotifyManagers(Map<String, Object> map);

    /**
     * 检查设备和管理员的绑定关系是否存在
     * @param map 包含deviceId和managerId的参数map
     * @return 绑定实体（如果存在）
     */
    NxMachineDeviceManagerEntity queryByDeviceAndManager(Map<String, Object> map);
}

