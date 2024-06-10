package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-02 17:14
 */

import com.nongxinle.entity.QyGbDisCorpUserEntity;

import java.util.List;
import java.util.Map;

public interface QyGbDisCorpUserService {
	
	QyGbDisCorpUserEntity queryObject(Integer qyGbDisCorpUserId);
	
	List<QyGbDisCorpUserEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(QyGbDisCorpUserEntity qyGbDisCorpUser);
	
	void update(QyGbDisCorpUserEntity qyGbDisCorpUser);
	
	void delete(Integer qyGbDisCorpUserId);
	
	void deleteBatch(Integer[] qyGbDisCorpUserIds);

    List<QyGbDisCorpUserEntity> queryCorpUserListByCorpId(Integer qyNxDisCorpId);
}
