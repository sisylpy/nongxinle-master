package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-14 12:03
 */

import com.nongxinle.entity.NxDistributerGoodsShelfStockEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerGoodsShelfStockService {
	
	NxDistributerGoodsShelfStockEntity queryObject(Integer nxDistributerGoodsShelfStockId);
	
	List<NxDistributerGoodsShelfStockEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerGoodsShelfStockEntity nxDistributerGoodsShelfStock);
	
	void update(NxDistributerGoodsShelfStockEntity nxDistributerGoodsShelfStock);
	
	void delete(Integer nxDistributerGoodsShelfStockId);
	
	void deleteBatch(Integer[] nxDistributerGoodsShelfStockIds);
}
