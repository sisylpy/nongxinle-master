package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 09-20 15:11
 */

import com.nongxinle.entity.GbDepartmentBillEntity;
import com.nongxinle.entity.NxDistributerEntity;

import java.util.List;
import java.util.Map;


public interface GbDepartmentBillDao extends BaseDao<GbDepartmentBillEntity> {


    GbDepartmentBillEntity queryGbDepartmentBillDetail(Integer billId);

    List<GbDepartmentBillEntity> queryBillsByParamsGb(Map<String, Object> map);

    int queryTotalByParamsGb(Map<String, Object> map1);

    Integer queryBillsCountByParamsGb(Map<String, Object> map4);

    Double queryGbDepBillsSubTotal(Map<String, Object> map);

    List<GbDepartmentBillEntity> queryDepartmentBillList(Map<String, Object> map);

    Double queryGbDepBillsSellingSubTotal(Map<String, Object> map12);

    int queryDepartmentBillCount(Map<String, Object> map3);

    List<GbDepartmentBillEntity> queryBillGoodsByParams(Map<String, Object> map);

    GbDepartmentBillEntity queryDepartBillByTradeNo(String ordersSn);

    List<GbDepartmentBillEntity> queryDepartBillListByTradeNo(String ordersSn);

    List<GbDepartmentBillEntity> queryBillFromWhichDepartment(Map<String, Object> map4);

    GbDepartmentBillEntity queryDepartBillByTsxTradeNo(String nxDbTradeNo);

    GbDepartmentBillEntity queryByPlatformSubmitToken(String submitToken);

    GbDepartmentBillEntity queryBlockingPlatformCashBillByDep(Integer depId);

    List<NxDistributerEntity> queryNxDistributer(Map<String, Object> map4);
}
