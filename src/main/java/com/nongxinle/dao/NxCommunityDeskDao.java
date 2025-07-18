package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-07 09:33
 */

import com.nongxinle.entity.NxCommunityDeskEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityDeskDao extends BaseDao<NxCommunityDeskEntity> {

    List<NxCommunityDeskEntity> queryComDeskByParams(Map<String, Object> map);

    NxCommunityDeskEntity queryDeskWithOrders(Map<String, Object> map);
}
