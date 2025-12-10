package com.nongxinle.dao;

import com.nongxinle.entity.NxMachinePrintRecordEntity;

import java.util.List;
import java.util.Map;

/**
 * 自助打印记录Dao
 * 
 * @author lpy
 * @date 2025-10-15
 */
public interface NxMachinePrintRecordDao {
    
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
     */
    List<Map<String, Object>> queryDailyStats(Map<String, Object> params);
    
    /**
     * 查询设备打印统计
     */
    List<Map<String, Object>> queryDeviceStats(Map<String, Object> params);
    
    /**
     * 查询配送商打印统计
     */
    List<Map<String, Object>> queryDistributerStats(Map<String, Object> params);
}

