package com.nongxinle.dao;

import com.nongxinle.entity.NxDisShipmentTaskEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface NxDisShipmentTaskDao {

    NxDisShipmentTaskEntity queryObject(Integer id);

    NxDisShipmentTaskEntity queryByOpenKey(@Param("openKey") String openKey);

    List<NxDisShipmentTaskEntity> queryList(Map<String, Object> map);

    List<NxDisShipmentTaskEntity> queryByPlanId(@Param("planId") Integer planId);

    List<NxDisShipmentTaskEntity> queryByDriverRouteId(@Param("driverRouteId") Integer driverRouteId);

    List<NxDisShipmentTaskEntity> queryByDisRouteDateStatus(Map<String, Object> map);

    void save(NxDisShipmentTaskEntity entity);

    void update(NxDisShipmentTaskEntity entity);

    void clearOpenKey(@Param("taskId") Integer taskId);

    void updateSchedule(NxDisShipmentTaskEntity entity);
}
