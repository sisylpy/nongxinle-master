package com.nongxinle.service;

import com.nongxinle.entity.NxMachinePaperRefillRecordEntity;

import java.util.List;
import java.util.Map;

/**
 * 加纸记录Service
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface NxMachinePaperRefillRecordService {

    NxMachinePaperRefillRecordEntity queryObject(Long nxPrrId);

    List<NxMachinePaperRefillRecordEntity> queryList(Map<String, Object> map);

    int queryTotal(Map<String, Object> map);

    void save(NxMachinePaperRefillRecordEntity nxPaperRefillRecord);

    void update(NxMachinePaperRefillRecordEntity nxPaperRefillRecord);

    void delete(Long nxPrrId);

    void deleteBatch(Long[] nxPrrIds);

    /**
     * 根据设备ID查询加纸记录列表
     */
    List<NxMachinePaperRefillRecordEntity> queryByDeviceId(Integer deviceId, Map<String, Object> params);

    /**
     * 根据操作人ID查询加纸记录列表
     */
    List<NxMachinePaperRefillRecordEntity> queryByOperatorId(Integer operatorId);

    /**
     * 查询设备的最近一次加纸记录
     */
    NxMachinePaperRefillRecordEntity queryLatestByDeviceId(Integer deviceId);

    /**
     * 执行加纸操作（包含记录和更新设备纸张数量）
     * @param deviceId 设备ID
     * @param addCount 增加数量
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param refillType 加纸类型（1-正常加纸 2-初始化 3-手动校准）
     * @param remark 备注
     * @deprecated 已废弃，请使用 refillPaperReplace 或 calibratePaper
     */
    @Deprecated
    void refillPaper(Integer deviceId, Integer addCount, Integer operatorId, String operatorName, 
                     Integer refillType, String remark);

    /**
     * 正常加纸/更换新纸（旧纸作废）
     * @param deviceId 设备ID
     * @param newPaperCount 新纸数量（直接设置，不累加）
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param remark 备注
     * @return 作废的旧纸数量
     */
    Integer refillPaperReplace(Integer deviceId, Integer newPaperCount, Integer operatorId, 
                               String operatorName, String remark);

    /**
     * 手动校准（只能减少，校准作废）
     * @param deviceId 设备ID
     * @param paperCount 校准后的纸张数量（必须 <= 当前数量）
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param remark 备注
     * @return 校准作废的数量
     * @throws IllegalArgumentException 如果校准值大于当前数量
     */
    Integer calibratePaper(Integer deviceId, Integer paperCount, Integer operatorId, 
                          String operatorName, String remark) throws IllegalArgumentException;

    /**
     * 根据市场ID查询换纸记录（关联设备表和管理员表）
     * @param params 包含marketId、startDate、stopDate、offset、limit的参数
     * @return 换纸记录列表（Map格式，包含设备和操作人信息）
     */
    List<Map<String, Object>> queryByMarketIdWithDetails(Map<String, Object> params);

    /**
     * 根据市场ID查询设备换纸统计（按设备分组）
     * @param params 包含marketId、startDate、stopDate、offset、limit的参数
     * @return 设备列表（Map格式，包含设备信息和统计数据）
     */
    List<Map<String, Object>> queryDeviceStatsByMarketId(Map<String, Object> params);

    /**
     * 统计市场的换纸记录总数
     * @param params 包含marketId、startDate、endDate的参数
     * @return 记录总数
     */
    int countByMarketId(Map<String, Object> params);

    /**
     * 统计市场的设备总数（有换纸记录的设备）
     * @param params 包含marketId、startDate、stopDate的参数
     * @return 设备总数
     */
    int countDevicesByMarketId(Map<String, Object> params);

    /**
     * 根据设备ID查询换纸记录（关联设备表和管理员表）
     * @param params 包含deviceId、startDate、stopDate、offset、limit的参数
     * @return 换纸记录列表（Map格式，包含设备和操作人信息）
     */
    List<Map<String, Object>> queryByDeviceIdWithDetails(Map<String, Object> params);

    /**
     * 统计设备的换纸记录总数
     * @param params 包含deviceId、startDate、stopDate的参数
     * @return 记录总数
     */
    int countByDeviceIdWithDetails(Map<String, Object> params);
}

