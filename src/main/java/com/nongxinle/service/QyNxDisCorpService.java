package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 07-15 15:52
 */

import com.nongxinle.entity.QyNxDisCorpEntity;

import java.util.List;
import java.util.Map;

public interface QyNxDisCorpService {
	
	QyNxDisCorpEntity queryObject(Integer qyNxDisCorpId);
	
	List<QyNxDisCorpEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(QyNxDisCorpEntity qyNxDisCorp);
	
	void update(QyNxDisCorpEntity qyNxDisCorp);
	
	void delete(Integer qyNxDisCorpId);
	
	void deleteBatch(Integer[] qyNxDisCorpIds);

    QyNxDisCorpEntity queryQyCropByCropId(String corpId);

	void deleteCropByCropId(String corpid);
}
