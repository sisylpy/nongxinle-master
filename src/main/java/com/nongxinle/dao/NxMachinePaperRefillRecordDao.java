package com.nongxinle.dao;

import com.nongxinle.entity.NxMachinePaperRefillRecordEntity;

import java.util.List;
import java.util.Map;

/**
 * 加纸记录Dao
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface NxMachinePaperRefillRecordDao extends BaseDao<NxMachinePaperRefillRecordEntity> {

    /**
     * 根据设备ID查询加纸记录列表
     * @param map 包含deviceId的参数map（支持分页）
     * @return 加纸记录列表
     */
    List<NxMachinePaperRefillRecordEntity> queryByDeviceId(Map<String, Object> map);

    /**
     * 根据操作人ID查询加纸记录列表
     * @param map 包含operatorId的参数map
     * @return 加纸记录列表
     */
    List<NxMachinePaperRefillRecordEntity> queryByOperatorId(Map<String, Object> map);

    /**
     * 查询设备的最近一次加纸记录
     * @param deviceId 设备ID
     * @return 最近一次加纸记录
     */
    NxMachinePaperRefillRecordEntity queryLatestByDeviceId(Integer deviceId);

    /**
     * 统计设备的加纸次数
     * @param map 包含deviceId和时间范围的参数map
     * @return 加纸次数
     */
    int countByDeviceId(Map<String, Object> map);

    /**
     * 统计设备的纸张消耗量
     * @param map 包含deviceId和时间范围的参数map
     * @return 纸张消耗总量
     */
    Integer sumPaperConsumption(Map<String, Object> map);

    /**
     * 根据市场ID查询换纸记录（关联设备表和管理员表）
     * @param map 包含marketId、startDate、stopDate、offset、limit的参数map
     * @return 换纸记录列表（Map格式，包含设备和操作人信息）
     */
    List<Map<String, Object>> queryByMarketIdWithDetails(Map<String, Object> map);

    /**
     * 根据市场ID查询设备换纸统计（按设备分组）
     * @param map 包含marketId、startDate、stopDate、offset、limit的参数map
     * @return 设备列表（Map格式，包含设备信息和统计数据）
     */
    List<Map<String, Object>> queryDeviceStatsByMarketId(Map<String, Object> map);

    /**
     * 统计市场的换纸记录总数
     * @param map 包含marketId、startDate、endDate的参数map
     * @return 记录总数
     */
    int countByMarketId(Map<String, Object> map);

    /**
     * 统计市场的设备总数（有换纸记录的设备）
     * @param map 包含marketId、startDate、stopDate的参数map
     * @return 设备总数
     */
    int countDevicesByMarketId(Map<String, Object> map);

    /**
     * 根据设备ID查询换纸记录（关联设备表和管理员表）
     * @param map 包含deviceId、startDate、stopDate、offset、limit的参数map
     * @return 换纸记录列表（Map格式，包含设备和操作人信息）
     */
    List<Map<String, Object>> queryByDeviceIdWithDetails(Map<String, Object> map);

    /**
     * 统计设备的换纸记录总数
     * @param map 包含deviceId、startDate、stopDate的参数map
     * @return 记录总数
     */
    int countByDeviceIdWithDetails(Map<String, Object> map);
}

