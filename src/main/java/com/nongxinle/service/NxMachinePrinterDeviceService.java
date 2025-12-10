package com.nongxinle.service;

import com.nongxinle.entity.NxMachinePrinterDeviceEntity;

import java.util.List;
import java.util.Map;

/**
 * 打印设备Service
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface NxMachinePrinterDeviceService {

    NxMachinePrinterDeviceEntity queryObject(Integer nxPdId);

    List<NxMachinePrinterDeviceEntity> queryList(Map<String, Object> map);

    int queryTotal(Map<String, Object> map);

    void save(NxMachinePrinterDeviceEntity nxPrinterDevice);

    void update(NxMachinePrinterDeviceEntity nxPrinterDevice);

    void delete(Integer nxPdId);

    void deleteBatch(Integer[] nxPdIds);

    /**
     * 根据设备编号查询设备
     */
    NxMachinePrinterDeviceEntity queryByDeviceNo(String deviceNo);

    /**
     * 根据市场ID查询设备列表
     */
    List<NxMachinePrinterDeviceEntity> queryByMarketId(Integer marketId);

    /**
     * 扣减纸张数量（打印时调用）
     * @param deviceId 设备ID
     * @param count 扣减数量（默认1张）
     * @return 扣减后的纸张数量
     */
    Integer reducePaperCount(Integer deviceId, Integer count);

    /**
     * 增加纸张数量（加纸时调用）
     * @param deviceId 设备ID
     * @param count 增加数量
     * @return 增加后的纸张数量
     */
    Integer addPaperCount(Integer deviceId, Integer count);

    /**
     * 手动设置纸张数量（校准时调用）
     */
    void setPaperCount(Integer deviceId, Integer paperCount);

    /**
     * 更新设备状态
     */
    void updateStatus(Integer deviceId, Integer status);

    /**
     * 查询设备详情（包含阈值配置和责任人）
     */
    NxMachinePrinterDeviceEntity queryDeviceDetail(Integer deviceId);

    /**
     * 生成设备编号
     */
    String generateDeviceNo();
}

