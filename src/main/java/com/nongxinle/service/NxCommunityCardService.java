package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-23 14:26
 */

import com.nongxinle.entity.NxCommunityCardEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityCardService {
	
	NxCommunityCardEntity queryObject(Integer nxCommunityCardId);
	
	List<NxCommunityCardEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityCardEntity nxCommunityCard);
	
	void update(NxCommunityCardEntity nxCommunityCard);
	
	void delete(Integer nxCommunityCardId);
	
	void deleteBatch(Integer[] nxCommunityCardIds);

    List<NxCommunityCardEntity> queryCardListByParams(Map<String, Object> map);

}
