package com.nongxinle.service;

import com.nongxinle.entity.NxMachineAlertRecordEntity;

import java.util.List;
import java.util.Map;

/**
 * 提醒记录Service
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface NxMachineAlertRecordService {

    NxMachineAlertRecordEntity queryObject(Long nxArId);

    List<NxMachineAlertRecordEntity> queryList(Map<String, Object> map);

    int queryTotal(Map<String, Object> map);

    void save(NxMachineAlertRecordEntity nxAlertRecord);

    void update(NxMachineAlertRecordEntity nxAlertRecord);

    void delete(Long nxArId);

    void deleteBatch(Long[] nxArIds);

    /**
     * 根据设备ID查询提醒记录列表
     */
    List<NxMachineAlertRecordEntity> queryByDeviceId(Integer deviceId, Map<String, Object> params);

    /**
     * 根据管理员ID查询提醒记录列表
     */
    List<NxMachineAlertRecordEntity> queryByManagerId(Integer managerId, Map<String, Object> params);

    /**
     * 查询未清除的提醒记录（防重复用）
     */
    NxMachineAlertRecordEntity queryUnclearedRecord(Integer deviceId, Integer alertLevel);

    /**
     * 清除设备的提醒记录（加纸后调用）
     */
    void clearRecordsByDeviceId(Integer deviceId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long recordId);

    /**
     * 查询管理员的未读消息数量
     * @param params 参数Map（必须包含managerId，可选days）
     */
    int queryUnreadCount(Map<String, Object> params);

    /**
     * 更新发送状态
     */
    void updateSendStatus(Long recordId, Integer sendStatus);
}

