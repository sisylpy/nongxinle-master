package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-14 17:42
 */

import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.entity.NxCustomerUserGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxCustomerUserGoodsDao extends BaseDao<NxCustomerUserGoodsEntity> {

    NxCustomerUserGoodsEntity queryByCommunityGoodsId(Map<String, Object> map);

    List<NxCustomerUserGoodsEntity> queryUserGoods(Map<String, Object> map);

    NxCustomerUserGoodsEntity queryUserGoodsByParams(Map<String, Object> map);

    List<NxCommunityGoodsEntity> queryUserLoveGoods(Map<String, Object> map);

    List<NxCommunityGoodsEntity> userQueryCommGoods(Map<String, Object> map);

    int queryUserGoodsCount(Map<String, Object> mapUG);
}
