package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-04 17:57:31
 */

import com.nongxinle.entity.NxCommunityEntity;

import java.util.List;


public interface NxCommunityDao extends BaseDao<NxCommunityEntity> {

    List<NxCommunityEntity> queryDistributerCommunityList(Integer disId);
}
