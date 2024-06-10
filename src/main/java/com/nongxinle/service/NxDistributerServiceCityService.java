package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 08-19 13:03
 */

import com.nongxinle.entity.NxDistributerServiceCityEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerServiceCityService {
	
	NxDistributerServiceCityEntity queryObject(Integer nxDistributerServiceCity);
	
	List<NxDistributerServiceCityEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerServiceCityEntity nxDistributerServiceCity);
	
	void update(NxDistributerServiceCityEntity nxDistributerServiceCity);
	
	void delete(Integer nxDistributerServiceCity);
	
	void deleteBatch(Integer[] nxDistributerServiceCitys);
}
