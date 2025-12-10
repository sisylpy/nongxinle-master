package com.nongxinle.service;

import com.nongxinle.entity.NxMarketPricePlanEntity;
import com.nongxinle.utils.PageUtils;

import java.util.List;
import java.util.Map;

/**
 * 市场价格方案Service
 * @author lpy
 * @date 2025-01-09
 */
public interface NxMarketPricePlanService {

    /**
     * 根据市场ID和类型查询价格方案
     * @param marketId 市场ID
     * @param type 方案类型（0-流量 1-设备）
     * @return 价格方案列表
     */
    List<NxMarketPricePlanEntity> queryByMarketAndType(Integer marketId, Integer type);

    /**
     * 根据条件查询价格方案列表
     * @param map 查询条件
     * @return 价格方案列表
     */
    List<NxMarketPricePlanEntity> queryList(Map<String, Object> map);

    /**
     * 查询总数
     * @param map 查询条件
     * @return 总数
     */
    int queryTotal(Map<String, Object> map);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return 价格方案
     */
    NxMarketPricePlanEntity queryObject(Integer id);

    /**
     * 保存
     * @param entity 价格方案实体
     */
    void save(NxMarketPricePlanEntity entity);

    /**
     * 更新
     * @param entity 价格方案实体
     */
    void update(NxMarketPricePlanEntity entity);

    /**
     * 删除
     * @param id 主键ID
     */
    void delete(Integer id);

    /**
     * 批量删除
     * @param ids 主键ID数组
     */
    void deleteBatch(Integer[] ids);

    /**
     * 分页查询
     * @param params 查询参数
     * @return 分页结果
     */
    PageUtils queryPage(Map<String, Object> params);
}
