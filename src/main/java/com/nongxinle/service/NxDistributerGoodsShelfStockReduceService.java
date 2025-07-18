package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-14 12:03
 */

import com.nongxinle.entity.NxDistributerGoodsShelfStockReduceEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerGoodsShelfStockReduceService {
	
	NxDistributerGoodsShelfStockReduceEntity queryObject(Integer nxDistributerGoodsShelfStockReduceId);
	
	List<NxDistributerGoodsShelfStockReduceEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerGoodsShelfStockReduceEntity nxDistributerGoodsShelfStockReduce);
	
	void update(NxDistributerGoodsShelfStockReduceEntity nxDistributerGoodsShelfStockReduce);
	
	void delete(Integer nxDistributerGoodsShelfStockReduceId);
	
	void deleteBatch(Integer[] nxDistributerGoodsShelfStockReduceIds);

    List<NxDistributerGoodsShelfStockReduceEntity> queryReduceListByParams(Map<String, Object> map);
}
