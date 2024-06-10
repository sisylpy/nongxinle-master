package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-25 08:54
 */

import com.nongxinle.entity.NxCommunityPromoteEntity;

import java.util.List;


public interface NxCommunityPromoteDao extends BaseDao<NxCommunityPromoteEntity> {

    List<NxCommunityPromoteEntity> getListByCommunityId(Integer communityId);

    List<NxCommunityPromoteEntity> queryPromoteByFatherId(Integer nxCommunityFatherGoodsId);
}
