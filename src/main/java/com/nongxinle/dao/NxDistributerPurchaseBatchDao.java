package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-25 22:52
 */

import com.nongxinle.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public interface NxDistributerPurchaseBatchDao extends BaseDao<NxDistributerPurchaseBatchEntity> {

    List<NxDistributerPurchaseBatchEntity> queryDisPurchaseBatch(Map<String, Object> map);

    void delateBatchId(Integer purchaseBatchId);

    NxDistributerPurchaseBatchEntity queryBatchWithOrders(Integer batchId);

    List<NxSellUserEntity> queryAllSellersByParams(Map<String, Object> map);

    int queryDisPurchaseBatchCount(Map<String, Object> map);

    Double queryDisPurchaseBatchTotal(Map<String, Object> map);

    Double queryPurchaserCashTotal(Map<String, Object> map1);

    List<NxDistributerEntity> queryNxDistributerBySellerId(String sellId);

    NxDistributerPurchaseBatchEntity queryBatchItemByParams(Map<String, Object> mapB);
}
