package com.nongxinle.dao;

import com.nongxinle.entity.NxDisRouteUnassignedStopOrderEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NxDisRouteUnassignedStopOrderDao {

    List<NxDisRouteUnassignedStopOrderEntity> queryByUnassignedStopId(Integer unassignedStopId);

    List<NxDisRouteUnassignedStopOrderEntity> queryByUnassignedStopIds(@Param("unassignedStopIds") List<Integer> unassignedStopIds);

    void save(NxDisRouteUnassignedStopOrderEntity entity);

    void deleteByUnassignedStopIds(@Param("unassignedStopIds") List<Integer> unassignedStopIds);

    void deleteByPlanId(@Param("planId") Integer planId);
}
