package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 01-17 07:54
 */

import com.nongxinle.entity.NxCommunityPurchaseBatchEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityPurchaseBatchDao extends BaseDao<NxCommunityPurchaseBatchEntity> {

    List<NxCommunityPurchaseBatchEntity> queryComPurchaseBatchByParams(Map<String, Object> map2);

    NxCommunityPurchaseBatchEntity queryBatchDetail(Map<String, Object> map);

    Integer queryCommPurchaseBatchCount(Map<String, Object> map41);

    Double queryCommSupplierUnSettleSubtotal(Map<String, Object> map41);
}
