package com.nongxinle.dao;

import com.nongxinle.dto.route.DisRouteOrderSnapshotDto;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface NxDisRoutePlanDao {

    NxDisRoutePlanEntity queryObject(Integer id);

    NxDisRoutePlanEntity queryByDisRouteDateStatus(@Param("disId") Integer disId,
                                                   @Param("routeDate") String routeDate,
                                                   @Param("status") String status);

    NxDisRoutePlanEntity queryByDisRouteDateBatchStatus(@Param("disId") Integer disId,
                                                       @Param("routeDate") String routeDate,
                                                       @Param("dispatchBatch") String dispatchBatch,
                                                       @Param("status") String status);

    List<NxDisRoutePlanEntity> queryList(Map<String, Object> map);

    void save(NxDisRoutePlanEntity entity);

    void update(NxDisRoutePlanEntity entity);

    void updateSchedule(NxDisRoutePlanEntity entity);

    void updateFeasibility(NxDisRoutePlanEntity entity);

    void updateBatch(NxDisRoutePlanEntity entity);

    void cancelDraftByDisRouteDate(@Param("disId") Integer disId, @Param("routeDate") String routeDate);

    void cancelConfirmedByDisRouteDate(@Param("disId") Integer disId, @Param("routeDate") String routeDate);

    List<DisRouteOrderSnapshotDto> queryEligibleLiveOrderSnapshots(@Param("disId") Integer disId,
                                                                 @Param("routeDate") String routeDate,
                                                                 @Param("routeDateOnly") String routeDateOnly,
                                                                 @Param("excludeDepartmentIds") List<Integer> excludeDepartmentIds);

    int countEligibleLiveOrders(@Param("disId") Integer disId,
                                @Param("excludeDepartmentIds") List<Integer> excludeDepartmentIds);
}
