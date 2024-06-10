package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 07-16 12:25
 */

import com.nongxinle.entity.QyNxDisCorpUserEntity;

import java.util.List;
import java.util.Map;

public interface QyNxDisCropUserService {
	
	QyNxDisCorpUserEntity queryObject(Integer qyNxDisCorpUserId);
	
	List<QyNxDisCorpUserEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(QyNxDisCorpUserEntity qyNxDisCropUser);
	
	void update(QyNxDisCorpUserEntity qyNxDisCropUser);
	
	void delete(Integer qyNxDisCorpUserId);
	
	void deleteBatch(Integer[] qyNxDisCorpUserIds);

    QyNxDisCorpUserEntity queryQyUserByUserId(String userId);

	List<QyNxDisCorpUserEntity> queryCorpUserListByCorpId(Integer corpid);
}
