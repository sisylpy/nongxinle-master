package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-21 20:42
 */

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.GbDistributerEntity;
import com.nongxinle.entity.GbDistributerUserEntity;

import java.util.List;
import java.util.Map;

public interface GbDistributerService {
	
	GbDistributerEntity queryObject(Integer gbDistributerId);
	
	List<GbDistributerEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDistributerEntity gbDistributer);
	
	void update(GbDistributerEntity gbDistributer);
	
	void delete(Integer gbDistributerId);
	
	void deleteBatch(Integer[] gbDistributerIds);

	Integer saveNewDistributerGb(GbDistributerEntity gbDistributerEntity);

    GbDistributerEntity queryDistributerInfo(Integer gbDepartmentDisId);

    List<GbDistributerEntity> queryListAll();

    void kaitongGbDis(Integer id);

	Integer saveNewDistributerGbWork(GbDistributerEntity gbDistributerEntity, JSONObject jsonObject);

    List<GbDistributerEntity> queryGbDisCustomerBySellerOpenId(String openId);

	Integer saveNewDistributerGbForPeisong(GbDistributerUserEntity distributerUserEntity, Integer disId);
}
