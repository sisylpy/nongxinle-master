package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-11 21:54
 */

import com.nongxinle.entity.NxJrdhSupplierEntity;

import java.util.List;
import java.util.Map;


public interface NxJrdhSupplierDao extends BaseDao<NxJrdhSupplierEntity> {

    List<NxJrdhSupplierEntity> queryJrdhSupplerByParams(Map<String, Object> map);

    NxJrdhSupplierEntity querySellUserSupplier(Map<String, Object> mapS);

    List<NxJrdhSupplierEntity> queryJrdhSupplerWithDisByUserId(Map<String, Object> mapS);

    NxJrdhSupplierEntity querySupplierByUserId(Integer sellerId);
}
