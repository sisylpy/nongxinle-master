package com.nongxinle.service;

/**
 * 打印机提醒核心业务Service
 * 整合打印扣减、提醒触发、加纸清除等核心功能
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface PrinterAlertService {

    /**
     * 打印时扣减纸张并触发提醒检查（默认扣减1张）
     * 核心流程：
     * 1. 扣减纸张数量（默认1张）
     * 2. 判断扣减后余量是否低于阈值
     * 3. 如低于阈值 → 触发提醒逻辑（防重复）
     * 
     * @param deviceId 设备ID
     * @return 扣减后的纸张数量，null表示扣减失败（纸张不足）
     */
    Integer printAndCheckAlert(Integer deviceId);

    /**
     * 打印时扣减纸张并触发提醒检查（批量扣减）
     * 核心流程：
     * 1. 一次性扣减指定数量的纸张
     * 2. 判断扣减后余量是否低于阈值
     * 3. 如低于阈值 → 触发提醒逻辑（防重复）
     * 
     * @param deviceId 设备ID
     * @param count 扣减数量
     * @return 扣减后的纸张数量，null表示扣减失败（纸张不足）
     */
    Integer printAndCheckAlert(Integer deviceId, Integer count);

    /**
     * 加纸并清除提醒
     * 核心流程：
     * 1. 增加纸张数量
     * 2. 记录加纸操作
     * 3. 清除该设备的所有未清除提醒记录
     * 
     * @param deviceId 设备ID
     * @param addCount 增加数量
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param refillType 加纸类型（1-正常加纸 2-初始化 3-手动校准）
     * @param remark 备注
     */
    void refillPaperAndClearAlert(Integer deviceId, Integer addCount, Integer operatorId, 
                                   String operatorName, Integer refillType, String remark);

    /**
     * 手动校准纸张数量
     * 核心流程：
     * 1. 设置纸张数量为指定值
     * 2. 记录校准操作
     * 3. 清除提醒
     * 
     * @param deviceId 设备ID
     * @param paperCount 校准后的纸张数量
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param remark 备注
     */
    void calibratePaperCount(Integer deviceId, Integer paperCount, Integer operatorId, 
                             String operatorName, String remark);

    /**
     * 检查并触发提醒
     * 
     * @param deviceId 设备ID
     * @param currentPaperCount 当前纸张数量
     */
    void checkAndTriggerAlert(Integer deviceId, Integer currentPaperCount);

    /**
     * 发送微信模板消息提醒
     * 
     * @param recordId 提醒记录ID
     * @return 是否发送成功
     */
    boolean sendWxTemplateMessage(Long recordId);
}

