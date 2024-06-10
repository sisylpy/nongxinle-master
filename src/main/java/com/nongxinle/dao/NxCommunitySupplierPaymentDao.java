package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-15 18:45
 */

import com.nongxinle.entity.NxCommunityPurchaseBatchEntity;
import com.nongxinle.entity.NxCommunitySupplierPaymentEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunitySupplierPaymentDao extends BaseDao<NxCommunitySupplierPaymentEntity> {

    List<NxCommunityPurchaseBatchEntity> queryCommPurchaseBatch(Map<String, Object> map);
}
