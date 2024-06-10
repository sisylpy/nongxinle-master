package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-12 19:46
 */

import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDistributerSupplierEntity;
import com.nongxinle.entity.NxJrdhUserEntity;
import com.nongxinle.entity.NxSellUserEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerSupplierDao extends BaseDao<NxDistributerSupplierEntity> {

    List<NxDistributerSupplierEntity> queryDisSupplierByParams(Map<String, Object> map);

    List<NxJrdhUserEntity> querySellerDistributerByParams(Map<String, Object> map);
}
