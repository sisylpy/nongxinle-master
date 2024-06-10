package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 07-27 21:41
 */

import com.nongxinle.entity.NxDistributerStandardEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerStandardService {

	List<NxDistributerStandardEntity> queryDisStandardByDisGoodsId(Integer nxDdgDisGoodsId);

	NxDistributerStandardEntity queryObject(Integer nxDistributerStandardId);
	
	List<NxDistributerStandardEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerStandardEntity nxDistributerStandard);
	
	void update(NxDistributerStandardEntity nxDistributerStandard);
	
	void delete(Integer nxDistributerStandardId);
	
	void deleteBatch(Integer[] nxDistributerStandardIds);

    List<NxDistributerStandardEntity> queryDisStandardByParams(Map<String, Object> map);
}
