package com.nongxinle.dao;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NxDisDriverRouteDao {

    NxDisDriverRouteEntity queryObject(Integer id);

    List<NxDisDriverRouteEntity> queryByPlanId(Integer planId);

    NxDisDriverRouteEntity queryByPlanAndDriver(@Param("planId") Integer planId,
                                                @Param("driverUserId") Integer driverUserId);

    void save(NxDisDriverRouteEntity entity);

    void update(NxDisDriverRouteEntity entity);

    void updateSchedule(NxDisDriverRouteEntity entity);

    void updateFeasibility(NxDisDriverRouteEntity entity);

    void updateExecution(NxDisDriverRouteEntity entity);

    void updateLoadingGate(NxDisDriverRouteEntity entity);

    void reopenForRedispatch(@Param("driverRouteId") Integer driverRouteId,
                             @Param("routeStatus") String routeStatus);

    void deleteByPlanId(Integer planId);
}
