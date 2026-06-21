package com.nongxinle.dao;

import com.nongxinle.entity.NxDisRouteStopEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NxDisRouteStopDao {

    List<NxDisRouteStopEntity> queryByDriverRouteId(Integer driverRouteId);

    List<NxDisRouteStopEntity> queryByDriverRouteIds(@Param("driverRouteIds") List<Integer> driverRouteIds);

    NxDisRouteStopEntity queryByShipmentTaskId(@Param("shipmentTaskId") Integer shipmentTaskId);

    void save(NxDisRouteStopEntity entity);

    void update(NxDisRouteStopEntity entity);

    void updateSchedule(NxDisRouteStopEntity entity);

    void updateDispatchSnapshot(NxDisRouteStopEntity entity);

    void deleteByShipmentTaskId(@Param("shipmentTaskId") Integer shipmentTaskId);

    void deleteByDriverRouteIds(@Param("driverRouteIds") List<Integer> driverRouteIds);
}
