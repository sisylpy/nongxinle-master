package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-22 20:41
 */

import com.nongxinle.entity.NxRetailerPurchaseBatchEntity;

import java.util.List;
import java.util.Map;


public interface NxRetailerPurchaseBatchDao extends BaseDao<NxRetailerPurchaseBatchEntity> {

    List<NxRetailerPurchaseBatchEntity> queryRetPurBatchByParams(Map<String, Object> map3);

    List<NxRetailerPurchaseBatchEntity> queryRetPurBatchSizeByParams(Map<String, Object> map3);

    NxRetailerPurchaseBatchEntity queryRetPurBatchDetail(Map<String, Object> map);
}
