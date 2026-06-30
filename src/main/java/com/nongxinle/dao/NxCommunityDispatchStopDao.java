package com.nongxinle.dao;

import com.nongxinle.entity.NxCommunityDispatchStopEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityDispatchStopDao extends BaseDao<NxCommunityDispatchStopEntity> {

    List<NxCommunityDispatchStopEntity> queryByPlanId(Integer planId);

    List<NxCommunityDispatchStopEntity> queryByDriverRouteId(Integer driverRouteId);

    List<NxCommunityDispatchStopEntity> queryActiveByPlanId(Map<String, Object> map);

    int countActiveByDriverRouteId(Integer driverRouteId);
}
