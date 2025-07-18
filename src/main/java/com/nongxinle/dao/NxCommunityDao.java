package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-04 17:57:31
 */

import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxECommerceCommunityEntity;
import com.nongxinle.entity.NxECommerceEntity;

import java.util.List;


public interface NxCommunityDao extends BaseDao<NxCommunityEntity> {

    NxCommunityEntity saveOne(NxCommunityEntity nxCommunity);

    NxECommerceEntity queryCommunityByECommerceId(Integer id);

    List<NxCommunityEntity> queryCommunityListByUserPoint(String nxCuaLocation);
}
