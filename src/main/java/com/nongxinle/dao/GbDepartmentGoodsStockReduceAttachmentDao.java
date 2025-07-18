package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-29 16:35
 */

import com.nongxinle.entity.GbDepartmentGoodsStockReduceAttachmentEntity;
import com.nongxinle.entity.GbDistributerPurchaseGoodsEntity;

import java.util.List;
import java.util.Map;


public interface GbDepartmentGoodsStockReduceAttachmentDao extends BaseDao<GbDepartmentGoodsStockReduceAttachmentEntity> {

    List<GbDistributerPurchaseGoodsEntity> queryStarsPurGoodsByParams(Map<String, Object> map);

    GbDepartmentGoodsStockReduceAttachmentEntity queryItemByRdId(Integer returnId);
}
