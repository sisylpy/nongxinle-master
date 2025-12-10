package com.nongxinle.service;

import com.nongxinle.entity.NxMachinePrintRecordEntity;

import java.util.List;
import java.util.Map;

/**
 * 自助打印记录Service
 * 
 * @author lpy
 * @date 2025-10-15
 */
public interface NxMachinePrintRecordService {
    
    /**
     * 根据ID查询
     */
    NxMachinePrintRecordEntity queryObject(Integer nxPrId);
    
    /**
     * 条件查询列表
     */
    List<NxMachinePrintRecordEntity> queryList(Map<String, Object> map);
    
    /**
     * 条件查询列表（包含配送商名称）
     */
    List<Map<String, Object>> queryListWithDistributer(Map<String, Object> map);
    
    /**
     * 条件查询总数
     */
    int queryTotal(Map<String, Object> map);
    
    /**
     * 保存打印记录
     */
    void save(NxMachinePrintRecordEntity printRecord);
    
    /**
     * 更新打印记录
     */
    void update(NxMachinePrintRecordEntity printRecord);
    
    /**
     * 删除打印记录
     */
    void delete(Integer nxPrId);
    
    /**
     * 批量删除
     */
    void deleteBatch(Integer[] nxPrIds);
    
    /**
     * 根据单据ID查询打印记录
     */
    List<NxMachinePrintRecordEntity> queryByBillId(Integer billId);
    
    /**
     * 查询打印统计（按日期）
     * @param params 查询参数（marketId, deviceId, startDate, stopDate）
     */
    List<Map<String, Object>> queryDailyStats(Map<String, Object> params);
    
    /**
     * 查询设备打印统计
     * @param params 查询参数（marketId, startDate, stopDate）
     */
    List<Map<String, Object>> queryDeviceStats(Map<String, Object> params);
    
    /**
     * 查询配送商打印统计
     * @param params 查询参数（marketId, startDate, stopDate）
     */
    List<Map<String, Object>> queryDistributerStats(Map<String, Object> params);
    
    /**
     * 记录单据打印（核心方法）
     * @param billId 单据ID
     * @param deviceId 打印机设备ID
     * @param marketId 市场ID
     * @param distributerId 配送商ID
     * @param paperCount 打印纸张数量
     * @param billTotal 单据金额（可选，指订单金额）
     * @param billTradeNo 单据流水号（可选）
     * @param operatorId 操作人ID（可选）
     * @param operatorName 操作人姓名（可选）
     * @return 包含打印记录ID、打印费用、纸张数量等信息的Map
     */
    Map<String, Object> recordPrint(Integer billId, Integer deviceId, Integer marketId, Integer distributerId,
                                   Integer paperCount, Double billTotal, String billTradeNo, 
                                   Integer operatorId, String operatorName);
}

