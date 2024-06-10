package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import com.nongxinle.entity.NxCustomerUserGoodsEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerUserGoodsService {
	
	NxCustomerUserGoodsEntity queryObject(Integer custUGoodsId);
	
	List<NxCustomerUserGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCustomerUserGoodsEntity nxCustomerUserGoods);
	
	void update(NxCustomerUserGoodsEntity nxCustomerUserGoods);
	
	void delete(Integer custUGoodsId);
	
	void deleteBatch(Integer[] custUGoodsIds);

    List<NxCustomerUserGoodsEntity> queryUserGoods(Map<String, Object> map);
}
