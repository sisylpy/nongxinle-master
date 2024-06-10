package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-02 22:59
 */

import com.nongxinle.entity.QyGbDisCorpEntity;

import java.util.List;
import java.util.Map;

public interface QyGbDisCorpService {
	
	QyGbDisCorpEntity queryObject(Integer qyGbDisCorpId);
	
	List<QyGbDisCorpEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(QyGbDisCorpEntity qyGbDisCorp);
	
	void update(QyGbDisCorpEntity qyGbDisCorp);
	
	void delete(Integer qyGbDisCorpId);
	
	void deleteBatch(Integer[] qyGbDisCorpIds);

    QyGbDisCorpEntity queryQyCropByCropId(String corpid);

	void deleteCropByCropId(String corpid);

}
