package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-30 06:45
 */

import com.nongxinle.entity.NxCommunityStandardEntity;

import java.util.List;


public interface NxCommunityStandardDao extends BaseDao<NxCommunityStandardEntity> {

    List<NxCommunityStandardEntity> queryComGoodsStandards(Integer comGoodsId);
}
