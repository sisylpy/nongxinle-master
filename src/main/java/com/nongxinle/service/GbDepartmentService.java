package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import com.nongxinle.entity.GbDepartmentEntity;

import java.util.List;
import java.util.Map;

public interface GbDepartmentService {
	
	GbDepartmentEntity queryObject(Integer gbDepartmentId);
	
	List<GbDepartmentEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepartmentEntity gbDepartment);
	
	void update(GbDepartmentEntity gbDepartment);
	
	void delete(Integer gbDepartmentId);
	
	void deleteBatch(Integer[] gbDepartmentIds);

    List<GbDepartmentEntity> queryGroupDepsByDisId(Map<String,Object> map);

    List<GbDepartmentEntity> queryUnLineDepsByDisId(Integer disId);

    Integer saveNewChainDepartmentGb(GbDepartmentEntity gbDepartmentEntity);

    List<GbDepartmentEntity> querySubDepartments(Integer depFatherId);

    List<GbDepartmentEntity>  queryMultiGroupInfoGb(String openId);

	Map<String, Object> queryDepAndUserInfoGb(Integer gbDepartmentUserId);

	GbDepartmentEntity queryDepInfoGb(Integer depId);

	void saveNewDepartmentGb(GbDepartmentEntity department);

    List<GbDepartmentEntity> queryApplyOutStockGoodsDeps(Map<String, Object> map);

    List<GbDepartmentEntity> queryWasteDepartment(Integer disId);

    List<GbDepartmentEntity> queryDepWithAdminUserByParams(Map<String, Object> map);

    List<GbDepartmentEntity> queryDepByDepType(Map<String, Object> mapDis);

	void saveNewDepartmentGbWithDepGoods(GbDepartmentEntity department, Integer cankaoDepId);

    List<GbDepartmentEntity> queryGroupDepsByDisIdWithUnPayBill(Map<String, Object> map);
}
