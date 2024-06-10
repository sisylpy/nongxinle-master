package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import com.nongxinle.entity.GbDepartmentEntity;

import java.util.List;
import java.util.Map;


public interface GbDepartmentDao extends BaseDao<GbDepartmentEntity> {

    List<GbDepartmentEntity> queryGroupDepsByDisId(Map<String, Object> mapgetDisStockOrdersGoods);

    List<GbDepartmentEntity> queryUnLineDepsByDisId(Integer disId);


    List<GbDepartmentEntity> querySubDepartments(Integer depFatherId);

    GbDepartmentEntity queryDepInfoGb(Integer gbDuDepartmentId);

    List<GbDepartmentEntity> queryApplyOutStockGoodsDeps(Map<String, Object> map);

    List<GbDepartmentEntity> queryWasteDepartment(Integer disId);

    List<GbDepartmentEntity> queryDepWithAdminUserByParams(Map<String, Object> map);

    List<GbDepartmentEntity> queryDepByDepType(Map<String, Object> mapDis);

    List<GbDepartmentEntity> queryGroupDepsByDisIdWithUnPayBill(Map<String, Object> map);
}
