package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 08-14 22:10
 */

import com.nongxinle.entity.NxDistributerGoodsLinshiEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerGoodsLinshiDao extends BaseDao<NxDistributerGoodsLinshiEntity> {

	List<NxDistributerGoodsLinshiEntity> disGetLinshiGoodsList(Map<String, Object> map);

	int disGetLinshiGoodsTotal(Map<String, Object> map);

	NxDistributerGoodsLinshiEntity queryLinshiByFromGoodsId(Integer fromGoodsId);

	List<NxDistributerGoodsLinshiEntity> queryLinshiListByStatus(Map<String, Object> map);

	List<Integer> queryFromGoodsIdsByDisId(Integer disId);

}
