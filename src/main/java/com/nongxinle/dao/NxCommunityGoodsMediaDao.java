package com.nongxinle.dao;

import com.nongxinle.entity.NxCommunityGoodsMediaEntity;

import java.util.List;
import java.util.Map;

/**
 * 社区商品媒体资源Dao
 *
 * @author lpy
 * @date 2026-04-12 11:51:00
 */
public interface NxCommunityGoodsMediaDao extends BaseDao<NxCommunityGoodsMediaEntity> {

    /**
     * 根据商品ID查询媒体列表（按排序）
     */
    List<NxCommunityGoodsMediaEntity> queryListByGoodsId(Map<String, Object> map);

    /**
     * 查询商品主图
     */
    NxCommunityGoodsMediaEntity queryPrimaryMediaByGoodsId(Map<String, Object> map);

    /**
     * 批量插入媒体
     */
    void saveBatch(List<NxCommunityGoodsMediaEntity> mediaList);

    /**
     * 根据媒体ID删除单张图片
     */
    int deleteByMediaId(Map<String, Object> map);

    /**
     * 根据商品ID删除所有媒体
     */
    int deleteByGoodsId(Map<String, Object> map);

    /**
     * 取消商品的所有主图状态
     */
    int clearPrimaryByGoodsId(Map<String, Object> map);

    /**
     * 更新单个图片的排序
     */
    int updateMediaSort(Map<String, Object> map);
}
