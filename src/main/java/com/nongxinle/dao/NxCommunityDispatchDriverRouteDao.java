package com.nongxinle.dao;

import com.nongxinle.entity.NxCommunityDispatchDriverRouteEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityDispatchDriverRouteDao extends BaseDao<NxCommunityDispatchDriverRouteEntity> {

    NxCommunityDispatchDriverRouteEntity queryByPlanAndDriver(Map<String, Object> map);

    List<NxCommunityDispatchDriverRouteEntity> queryByPlanId(Integer planId);

    int clearLoadingGate(Map<String, Object> map);
}
