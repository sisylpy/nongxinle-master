package com.nongxinle.service;

import com.nongxinle.entity.NxDistributerGoodsEntity;

/**
 * 临时商品合并到正式商品服务
 */
public interface NxDistributerGoodsMergeService {

    /**
     * 将临时商品合并到正式商品
     * @param lsGoodsId 临时商品ID
     * @param nxGoodsId 目标正式商品ID（配送商商品ID）
     * @return 合并后的正式商品实体
     */
    NxDistributerGoodsEntity mergeLinshiToNxGoods(Integer lsGoodsId, Integer nxGoodsId);
}
