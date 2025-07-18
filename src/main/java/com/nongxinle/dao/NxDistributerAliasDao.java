package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-07 09:12
 */

import com.nongxinle.entity.NxDistributerAliasEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerAliasDao extends BaseDao<NxDistributerAliasEntity> {

    List<NxDistributerAliasEntity> queryAliasByParmas(Map<String, Object> map);
}
