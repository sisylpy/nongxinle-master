package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 11-30 15:31
 */

import com.nongxinle.entity.NxRestrauntEntity;
import com.nongxinle.entity.NxRestrauntUserEntity;

import java.util.List;
import java.util.Map;

public interface NxRestrauntUserService {
	
	NxRestrauntUserEntity queryObject(Integer nxRestrauntUserId);
	
	List<NxRestrauntUserEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxRestrauntUserEntity nxRestrauntUser);
	
	void update(NxRestrauntUserEntity nxRestrauntUser);
	
	void delete(Integer nxRestrauntUserId);
	
	void deleteBatch(Integer[] nxRestrauntUserIds);

    NxRestrauntUserEntity queryResUserByOpenId(String openid);

	NxRestrauntEntity queryAllResUsersByResId(Integer resId);

    NxRestrauntEntity queryAllRestrauntAndDepUsersByResId(Integer resId);

    NxRestrauntEntity queryChainRestrauntAndDepUsersByResId(Integer resId);
}
