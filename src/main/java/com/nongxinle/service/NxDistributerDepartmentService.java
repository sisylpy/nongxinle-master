package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-09 21:11
 */

import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDistributerDepartmentEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerDepartmentService {

	List<NxDepartmentEntity> queryDisDepartmentsBySettleType(Map<String, Object> map);

	void save(NxDistributerDepartmentEntity nxDistributerDepartment);

}
