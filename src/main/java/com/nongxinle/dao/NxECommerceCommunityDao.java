package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 11-28 21:17
 */

import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxECommerceCommunityEntity;

import java.util.List;


public interface NxECommerceCommunityDao extends BaseDao<NxECommerceCommunityEntity> {

    List<NxCommunityEntity> queryCommunityByCommerceId(Integer commerceId);
}
