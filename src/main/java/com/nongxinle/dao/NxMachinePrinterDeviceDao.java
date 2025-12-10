package com.nongxinle.dao;

import com.nongxinle.entity.NxMachinePrinterDeviceEntity;

import java.util.List;
import java.util.Map;

/**
 * 打印设备Dao
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface NxMachinePrinterDeviceDao extends BaseDao<NxMachinePrinterDeviceEntity> {

    /**
     * 根据设备编号查询设备
     * @param deviceNo 设备编号
     * @return 设备实体
     */
    NxMachinePrinterDeviceEntity queryByDeviceNo(String deviceNo);

    /**
     * 根据市场ID查询设备列表
     * @param map 包含marketId的参数map
     * @return 设备列表
     */
    List<NxMachinePrinterDeviceEntity> queryByMarketId(Map<String, Object> map);

    /**
     * 扣减纸张数量（打印时调用）
     * @param map 包含deviceId和count的参数map
     * @return 影响行数
     */
    int reducePaperCount(Map<String, Object> map);

    /**
     * 增加纸张数量（加纸时调用）
     * @param map 包含deviceId和count的参数map
     * @return 影响行数
     */
    int addPaperCount(Map<String, Object> map);

    /**
     * 手动设置纸张数量（校准时调用）
     * @param map 包含deviceId和paperCount的参数map
     * @return 影响行数
     */
    int setPaperCount(Map<String, Object> map);

    /**
     * 更新设备状态
     * @param map 包含deviceId和status的参数map
     * @return 影响行数
     */
    int updateStatus(Map<String, Object> map);

    /**
     * 查询设备详情（包含阈值配置和责任人）
     * @param deviceId 设备ID
     * @return 设备实体（含关联数据）
     */
    NxMachinePrinterDeviceEntity queryDeviceDetail(Integer deviceId);
}

