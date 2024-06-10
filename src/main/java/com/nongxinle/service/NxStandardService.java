package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-24 15:30:57
 */

import com.nongxinle.entity.NxStandardEntity;

import java.util.List;
import java.util.Map;

public interface NxStandardService {
	
	NxStandardEntity queryObject(Integer nxStandardId);
	
	List<NxStandardEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxStandardEntity nxStandard);
	
	void update(NxStandardEntity nxStandard);
	
	void delete(Integer nxStandardId);
	
	void deleteBatch(Integer[] nxStandardIds);

    List<NxStandardEntity> queryGoodsStandardListByGoodId(Integer nxGoodsId);

    List<NxStandardEntity> queryList(Integer nxGoodsId);
}
