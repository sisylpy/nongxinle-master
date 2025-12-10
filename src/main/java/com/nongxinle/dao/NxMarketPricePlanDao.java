package com.nongxinle.dao;

import com.nongxinle.entity.NxMarketPricePlanEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 市场价格方案DAO
 * @author lpy
 * @date 2025-01-09
 */
@Mapper
public interface NxMarketPricePlanDao {

    /**
     * 根据市场ID和类型查询价格方案
     * @param marketId 市场ID
     * @param type 方案类型（0-流量 1-设备）
     * @return 价格方案列表
     */
    List<NxMarketPricePlanEntity> queryByMarketAndType(@Param("marketId") Integer marketId, 
                                                       @Param("type") Integer type);

    /**
     * 根据条件查询价格方案
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
     * @return 影响行数
     */
    int save(NxMarketPricePlanEntity entity);

    /**
     * 更新
     * @param entity 价格方案实体
     * @return 影响行数
     */
    int update(NxMarketPricePlanEntity entity);

    /**
     * 删除
     * @param id 主键ID
     * @return 影响行数
     */
    int delete(Integer id);

    /**
     * 批量删除
     * @param ids 主键ID数组
     * @return 影响行数
     */
    int deleteBatch(Integer[] ids);
}
