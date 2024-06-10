package com.nongxinle.service;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-09 18:47
 */

import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerGoodsShelfService {
	
	NxDistributerGoodsShelfEntity queryObject(Integer nxDistributerGoodsShelfId);
	
	List<NxDistributerGoodsShelfEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerGoodsShelfEntity nxDistributerGoodsShelf);
	
	void update(NxDistributerGoodsShelfEntity nxDistributerGoodsShelf);
	
	void delete(Integer nxDistributerGoodsShelfId);
	
	void deleteBatch(Integer[] nxDistributerGoodsShelfIds);

    List<NxDistributerGoodsShelfEntity> queryShelfByParams(Map<String, Object> map);

    NxDistributerGoodsShelfEntity queryShelfGoodsByParams(Map<String, Object> map);

	List<NxDistributerFatherGoodsEntity> disGetUnPlanShelfPurchaseApplys(Map<String, Object> map);

    List<NxDistributerGoodsShelfEntity> queryShelfWithDetailByParams(Map<String, Object> map);
}
