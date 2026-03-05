package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 02-19 20:22
 */

import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDistributerNxDistributerEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerNxDistributerService {
	
	NxDistributerNxDistributerEntity queryObject(Integer nxDistributerNxDistributerId);
	
	List<NxDistributerNxDistributerEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerNxDistributerEntity nxDistributerNxDistributer);
	
	void update(NxDistributerNxDistributerEntity nxDistributerNxDistributer);
	
	void delete(Integer nxDistributerNxDistributerId);
	
	void deleteBatch(Integer[] nxDistributerNxDistributerIds);

	List<NxDistributerEntity> queryOfferNxDisByParams(Map<String, Object> map3);

	/**
	 * 根据两个配送商 id 查询协作关系
	 */
	NxDistributerNxDistributerEntity queryByPartnerIds(Integer disId1, Integer disId2);
}
