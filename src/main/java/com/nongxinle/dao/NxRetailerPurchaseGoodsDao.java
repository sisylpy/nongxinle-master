package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-22 20:41
 */

import com.nongxinle.entity.NxRetailerPurchaseGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxRetailerPurchaseGoodsDao extends BaseDao<NxRetailerPurchaseGoodsEntity> {

    List<NxRetailerPurchaseGoodsEntity> queryRetShelfPurGoodsByParame(Map<String, Object> map);

}
