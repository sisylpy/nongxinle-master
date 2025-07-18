package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 07-30 18:51
 */

import com.nongxinle.entity.NxAliasEntity;
import com.nongxinle.entity.NxGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxAliasDao extends BaseDao<NxAliasEntity> {

    List<NxAliasEntity> queryNxAliasList(Map<String, Object> map);

    List<NxGoodsEntity> queryNxGoodsByName(Map<String, Object> map);
}
