package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-26 16:23
 */

import com.nongxinle.entity.NxCommunityAdsenseEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityAdsenseDao extends BaseDao<NxCommunityAdsenseEntity> {

    List<NxCommunityAdsenseEntity> getListByCommunityId(Integer communityId);

    List<NxCommunityAdsenseEntity> queryAdsenseByNxCommunityId(Integer communityId);

    List<NxCommunityAdsenseEntity> queryAdsenseByParams(Map<String, Object> map);

    NxCommunityAdsenseEntity queryGoodsAdsenseByParams(Map<String, Object> map);


    ;



}
