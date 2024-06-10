package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-21 12:08
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;

public interface NxDistributerWeightService {
	
	NxDistributerWeightEntity queryObject(Integer nxDistributerWeightId);
	
	List<NxDistributerWeightEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerWeightEntity nxDistributerWeight);
	
	void update(NxDistributerWeightEntity nxDistributerWeight);
	
	void delete(Integer nxDistributerWeightId);
	
	void deleteBatch(Integer[] nxDistributerWeightIds);

    List<NxDistributerWeightEntity> queryWeightListByParams(Map<String, Object> map);

	NxDistributerWeightEntity queryWeightOrdersById(Integer id);

    int queryWeightCountByParams(Map<String, Object> map);


	List<NxDistributerPurchaseGoodsEntity> queryWeightGoodsById(Integer id);

    List<NxDistributerFatherGoodsEntity> queryFatherGoodsStockOrder(Map<String, Object> map);

    List<NxDepartmentEntity> queryWeightDepOrders(Integer id);
}
