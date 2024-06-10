package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-26 16:23
 */

import com.nongxinle.entity.NxCommunityAdsenseEntity;

import java.util.List;


public interface NxCommunityAdsenseDao extends BaseDao<NxCommunityAdsenseEntity> {

    List<NxCommunityAdsenseEntity> getListByCommunityId(Integer communityId);

    List<NxCommunityAdsenseEntity> queryAdsenseByNxCommunityId(Integer communityId);


    ;



}
