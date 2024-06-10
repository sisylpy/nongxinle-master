package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDistributerEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerService {
	
	NxDistributerEntity queryObject(Integer distributerId);

	
	void save(NxDistributerEntity nxDistributer);

	Integer saveNewNxDistributer(NxDistributerEntity distributerEntity);

	void update(NxDistributerEntity nxDistributerEntity);


    NxDistributerEntity gbDepQueryDistributerInfo(Map<String, Object> map);

    Integer saveNewNxDistributerWrok(NxDistributerEntity distributerEntity, JSONObject jsonObject);

    List<NxDistributerEntity> queryNxDisCustomerBySellerOpenId(String openId);

    NxDistributerEntity queryNxDisInfo(Integer id);

    List<NxDistributerEntity> queryAllTypeOne();


}
