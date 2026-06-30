package com.nongxinle.community.catalog.service;

import com.nongxinle.entity.NxCommunityGoodsMediaEntity;

import java.util.List;
import java.util.Map;

/**
 * 社区商品媒体资源Service
 *
 * @author lpy
 * @date 2026-04-12 11:51:00
 */
public interface NxCommunityGoodsMediaService {

    /**
     * 根据商品ID查询媒体列表
     */
    List<NxCommunityGoodsMediaEntity> getMediaListByGoodsId(Integer goodsId);

    /**
     * 获取商品主图
     */
    NxCommunityGoodsMediaEntity getPrimaryMedia(Integer goodsId);

    /**
     * 保存商品媒体（批量）
     */
    void saveMediaBatch(List<NxCommunityGoodsMediaEntity> mediaList);

    /**
     * 删除商品的所有媒体
     */
    void removeByGoodsId(Integer goodsId);

    /**
     * 删除单张图片
     */
    void removeMediaById(Integer mediaId);

    /**
     * 保存单张图片
     */
    void saveMedia(NxCommunityGoodsMediaEntity media);

    /**
     * 取消商品的所有主图状态（设为非主图）
     */
    void clearPrimaryByGoodsId(Integer goodsId);

    /**
     * 重新排序商品的所有图片
     */
    void updateMediaSort(Integer goodsId, Integer mediaId, Integer newSort);
}
