package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 11-30 15:31
 */

import com.nongxinle.entity.NxRestrauntEntity;

import java.util.List;
import java.util.Map;

public interface NxRestrauntService {
	
	NxRestrauntEntity queryObject(Integer nxRestrauntId);
	
	List<NxRestrauntEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxRestrauntEntity nxRestraunt);
	
	void update(NxRestrauntEntity nxRestraunt);
	
	void delete(Integer nxRestrauntId);
	
	void deleteBatch(Integer[] nxRestrauntIds);

	Integer saveNewRestraunt(NxRestrauntEntity res);

	Map<String, Object> queryResAndUserInfo(Integer resUserId);

    List<NxRestrauntEntity> queryPrepareDeliveryRestraunts(Integer comId);

    List<NxRestrauntEntity> queryDriverRestraunts(Integer userId);

	NxRestrauntEntity queryResInfo(Integer valueOf);

	Integer saveNewChainRestraunt(NxRestrauntEntity res);

    List<NxRestrauntEntity> queryChainRestrauntsByResId(Integer resId);

}
