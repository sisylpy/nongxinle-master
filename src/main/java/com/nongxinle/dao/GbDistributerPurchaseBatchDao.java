package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 06-25 22:52
 */

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDistributerEntity;
import com.nongxinle.entity.GbDistributerPurchaseBatchEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerPurchaseBatchDao extends BaseDao<GbDistributerPurchaseBatchEntity> {

    List<GbDistributerPurchaseBatchEntity> queryDisPurchaseBatch(Map<String, Object> map);

    void delateBatchId(Integer purchaseBatchId);

    GbDistributerPurchaseBatchEntity queryBatchWithOrders(Integer batchId);

    List<GbDistributerPurchaseBatchEntity> queryDepartmentPurchaseBatch(Map<String, Object> map);

    Integer queryDisPurchaseBatchCount(Map<String, Object> map2);

    Double querySupplierUnSettleSubtotal(Map<String, Object> map4);

    List<GbDepartmentEntity> queryDistributerAccountingData(Map<String, Object> map2);

    Double queryPurchaserCashTotal(Map<String, Object> map1);

    int queryReturnList(Map<String, Object> map333);

    List<GbDistributerEntity> queryGbDistributerBySellerId(String sellId);

    GbDistributerPurchaseBatchEntity queryBatchItemByParams(Map<String, Object> mapB);
}
