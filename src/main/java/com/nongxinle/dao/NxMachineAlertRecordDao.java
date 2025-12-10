package com.nongxinle.dao;

import com.nongxinle.entity.NxMachineAlertRecordEntity;

import java.util.List;
import java.util.Map;

/**
 * 提醒记录Dao
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface NxMachineAlertRecordDao extends BaseDao<NxMachineAlertRecordEntity> {

    /**
     * 根据设备ID查询提醒记录列表
     * @param map 包含deviceId的参数map
     * @return 提醒记录列表
     */
    List<NxMachineAlertRecordEntity> queryByDeviceId(Map<String, Object> map);

    /**
     * 根据管理员ID查询提醒记录列表
     * @param map 包含managerId的参数map
     * @return 提醒记录列表
     */
    List<NxMachineAlertRecordEntity> queryByManagerId(Map<String, Object> map);

    /**
     * 查询未清除的提醒记录（防重复用）
     * @param map 包含deviceId、alertLevel和isCleared=0的参数map
     * @return 未清除的提醒记录
     */
    NxMachineAlertRecordEntity queryUnclearedRecord(Map<String, Object> map);

    /**
     * 清除设备的提醒记录（加纸后调用）
     * @param deviceId 设备ID
     * @return 影响行数
     */
    int clearRecordsByDeviceId(Integer deviceId);

    /**
     * 标记消息为已读
     * @param recordId 记录ID
     * @return 影响行数
     */
    int markAsRead(Long recordId);

    /**
     * 查询管理员的未读消息数量
     * @param params 包含managerId（必需）和days（可选）的参数map
     * @return 未读消息数量
     */
    int queryUnreadCount(Map<String, Object> params);

    /**
     * 更新发送状态
     * @param map 包含recordId和sendStatus的参数map
     * @return 影响行数
     */
    int updateSendStatus(Map<String, Object> map);
}

