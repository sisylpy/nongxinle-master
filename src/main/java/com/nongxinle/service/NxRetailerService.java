package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-22 15:25
 */

import com.nongxinle.entity.NxRetailerEntity;

import java.util.List;
import java.util.Map;

public interface NxRetailerService {
	
	NxRetailerEntity queryObject(Integer nxRetailerId);


    Integer saveNewRestailer(NxRetailerEntity retailerEntity);

	Map<String, Object> queryRetailerAndUserInfo(Integer restailerUserId);
}
