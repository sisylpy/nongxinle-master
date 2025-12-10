package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 08-14 22:10
 */

import com.nongxinle.entity.NxDistributerGoodsLinshiEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerGoodsLinshiService {
	
	NxDistributerGoodsLinshiEntity queryObject(Integer nxDistributerGoodsLsId);
	
	List<NxDistributerGoodsLinshiEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerGoodsLinshiEntity nxDistributerGoodsLinshi);
	
	void update(NxDistributerGoodsLinshiEntity nxDistributerGoodsLinshi);
	
	void delete(Integer nxDistributerGoodsLsId);
	
	void deleteBatch(Integer[] nxDistributerGoodsLsIds);

    List<NxDistributerGoodsLinshiEntity> disGetLinshiGoodsList(Map<String, Object> map);

	int disGetLinshiGoodsTotal(Map<String, Object> map);
}
