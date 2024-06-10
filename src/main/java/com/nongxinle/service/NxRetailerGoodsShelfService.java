package com.nongxinle.service;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-22 15:25
 */

import com.nongxinle.entity.NxRetailerGoodsShelfEntity;

import java.util.List;
import java.util.Map;

public interface NxRetailerGoodsShelfService {
	
	NxRetailerGoodsShelfEntity queryObject(Integer nxRetailerGoodsShelfId);
	
	List<NxRetailerGoodsShelfEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxRetailerGoodsShelfEntity nxRetailerGoodsShelf);
	
	void update(NxRetailerGoodsShelfEntity nxRetailerGoodsShelf);
	
	void delete(Integer nxRetailerGoodsShelfId);
	
	void deleteBatch(Integer[] nxRetailerGoodsShelfIds);

    List<NxRetailerGoodsShelfEntity> queryRetShelfByParams(Map<String, Object> map);

	List<NxRetailerGoodsShelfEntity> queryRetShelfWithPurGoodsByParams(Map<String, Object> map);

}
