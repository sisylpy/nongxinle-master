package com.nongxinle.dao;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-09 18:47
 */

import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface NxDistributerGoodsShelfGoodsDao extends BaseDao<NxDistributerGoodsShelfGoodsEntity> {

    List<NxDistributerGoodsShelfGoodsEntity> queryShelfForGoodsByParams(Map<String, Object> map);

    int queryShelfForGoodsCount(Map<String, Object> map);

    int queryShelfGoodsCount(Map<String, Object> map);

    List<NxDistributerGoodsShelfGoodsEntity> queryShelfForGoodsWithOrders(Map<String, Object> pageParams);

    NxDistributerGoodsShelfGoodsEntity queryShlefGoodsByGoodsId(Integer nxDpgDisGoodsId);

    NxDistributerGoodsShelfGoodsEntity queryShlefGoodsByGoodsIdAndShelfId(@Param("disGoodsId") Integer disGoodsId, @Param("shelfId") Integer shelfId);

    List<NxDistributerGoodsShelfGoodsEntity> queryShelfGoodsBasic(Integer shelfId);

    void updateShelfLayer(@Param("id") Integer id, @Param("layer") Integer layer);

    /**
     * 更新商品在所有货架的重复标记
     * 如果该商品出现在2个或以上的不同货架，则标记为1（重复），否则标记为0（未重复）
     * @param disGoodsId 配送商商品ID
     */
    void updateDuplicateFlagForGoods(@Param("disGoodsId") Integer disGoodsId);

    // 盘库相关方法
    List<NxDistributerGoodsShelfGoodsEntity> queryUnInventoriedShelfGoods(Map<String, Object> map);

    Integer queryUnInventoriedShelfGoodsCount(Map<String, Object> map);

    // 溯源报告相关方法
    /**
     * 查询带有溯源报告的货架商品列表
     * @param map 查询参数（包含shelfId, status, offset, limit等）
     * @return 带有溯源报告的货架商品列表
     */
    List<NxDistributerGoodsShelfGoodsEntity> queryShelfForGoodsWithTraceReportByParams(Map<String, Object> map);

    /**
     * 查询带有溯源报告的货架商品总数
     * @param map 查询参数（包含shelfId等）
     * @return 总数
     */
    int queryShelfForGoodsWithTraceReportCount(Map<String, Object> map);

}
