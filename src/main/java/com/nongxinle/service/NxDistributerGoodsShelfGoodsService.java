package com.nongxinle.service;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-09 18:47
 */

import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerGoodsShelfGoodsService {
	
	NxDistributerGoodsShelfGoodsEntity queryObject(Integer nxDistributerGoodsShelfGoodsId);
	
	List<NxDistributerGoodsShelfGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerGoodsShelfGoodsEntity nxDistributerGoodsShelfGoods);
	
	void update(NxDistributerGoodsShelfGoodsEntity nxDistributerGoodsShelfGoods);
	
	void delete(Integer nxDistributerGoodsShelfGoodsId);
	
	void deleteBatch(Integer[] nxDistributerGoodsShelfGoodsIds);

    List<NxDistributerGoodsShelfGoodsEntity> queryShelfGoodsByParams(Map<String, Object> map);
}
