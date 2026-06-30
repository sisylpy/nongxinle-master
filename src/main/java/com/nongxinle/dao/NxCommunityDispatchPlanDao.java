package com.nongxinle.dao;

import com.nongxinle.entity.NxCommunityDispatchPlanEntity;

import java.util.Map;

public interface NxCommunityDispatchPlanDao extends BaseDao<NxCommunityDispatchPlanEntity> {

    NxCommunityDispatchPlanEntity queryByCommunityAndRouteDate(Map<String, Object> map);
}
