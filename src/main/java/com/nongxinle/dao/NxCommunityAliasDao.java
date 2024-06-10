package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 12-05 18:56
 */

import com.nongxinle.entity.NxCommunityAliasEntity;

import java.util.List;


public interface NxCommunityAliasDao extends BaseDao<NxCommunityAliasEntity> {

    List<NxCommunityAliasEntity> queryComAliasByComGoodsId(Integer comGoodsId);
}
