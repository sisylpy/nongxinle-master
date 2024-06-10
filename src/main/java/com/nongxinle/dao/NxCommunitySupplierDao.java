package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-15 18:45
 */

import com.nongxinle.entity.NxCommunitySupplierEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunitySupplierDao extends BaseDao<NxCommunitySupplierEntity> {

    List<NxCommunitySupplierEntity> queryCommunitySupplierByParams(Map<String, Object> map);
}
