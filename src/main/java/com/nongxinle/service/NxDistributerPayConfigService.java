package com.nongxinle.service;

import com.nongxinle.entity.NxDistributerPayConfigEntity;

/**
 * 商家支付配置服务接口
 * @author lpy
 * @date 2024-01-01
 */
public interface NxDistributerPayConfigService {
    
    /**
     * 根据商家ID查询支付配置
     * @param nxDisId 商家ID
     * @return 支付配置
     */
    NxDistributerPayConfigEntity queryByNxDisId(Integer nxDisId);
    
    /**
     * 保存支付配置
     * @param payConfig 支付配置
     * @return 操作结果
     */
    int save(NxDistributerPayConfigEntity payConfig);
    
    /**
     * 更新支付配置
     * @param payConfig 支付配置
     * @return 操作结果
     */
    int update(NxDistributerPayConfigEntity payConfig);
    
    /**
     * 删除支付配置
     * @param nxDpcId 配置ID
     * @return 操作结果
     */
    int delete(Integer nxDpcId);
} 