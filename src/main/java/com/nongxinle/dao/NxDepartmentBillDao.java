package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-11 17:01
 */

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.NxDepartmentBillEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;

import java.util.List;
import java.util.Map;


public interface NxDepartmentBillDao extends BaseDao<NxDepartmentBillEntity> {

    List<NxDepartmentBillEntity> queryBillsByParams(Map<String, Object> map);

    NxDepartmentBillEntity querySalesBillApplys(Integer billId);

    int queryTotalByParams(Map<String, Object> map1);

    Integer queryReturnNumberByBillId(Integer billId);

    NxDepartmentBillEntity queryReturnBillOrdersByBillId(Integer billId);

    List<NxDepartmentBillEntity> queryGbDepBillsByParams(Map<String, Object> map);

    Double queryBillSubtotalByParams(Map<String, Object> map);

    Double queryBillCostSubtotalByParams(Map<String, Object> map);

    List<NxDepartmentBillEntity> queryBillsListByParams(Map<String, Object> map);

    NxDepartmentBillEntity queryDepartBillByTradeNo(String ordersSn);
}
