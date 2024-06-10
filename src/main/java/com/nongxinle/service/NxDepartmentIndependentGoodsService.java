package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 07-24 13:00
 */

import com.nongxinle.entity.NxDepartmentIndependentGoodsEntity;

import java.util.List;
import java.util.Map;

public interface NxDepartmentIndependentGoodsService {
	
	NxDepartmentIndependentGoodsEntity queryObject(Integer nxDepartmentIndependentGoodsId);
	
	List<NxDepartmentIndependentGoodsEntity> queryList(Map<String, Object> map);
	

	void save(NxDepartmentIndependentGoodsEntity nxDepartmentIndependentGoods);
	
	void update(NxDepartmentIndependentGoodsEntity nxDepartmentIndependentGoods);
	
	void delete(Integer nxDepartmentIndependentGoodsId);
	
//	void deleteBatch(Integer[] nxDepartmentIndependentGoodsIds);

	//	int queryTotal(Map<String, Object> map);


	List<NxDepartmentIndependentGoodsEntity> querySearchStr(Map<String, Object> map);
}
