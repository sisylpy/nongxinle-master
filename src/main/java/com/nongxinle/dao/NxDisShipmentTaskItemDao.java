package com.nongxinle.dao;

import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NxDisShipmentTaskItemDao {

    NxDisShipmentTaskItemEntity queryObject(Integer id);

    NxDisShipmentTaskItemEntity queryByLiveOrderId(@Param("liveOrderId") Integer liveOrderId);

    List<NxDisShipmentTaskItemEntity> queryByTaskId(@Param("taskId") Integer taskId);

    List<NxDisShipmentTaskItemEntity> queryByTaskIds(@Param("taskIds") List<Integer> taskIds);

    List<NxDisShipmentTaskItemEntity> queryByBillId(@Param("billId") Integer billId);

    void save(NxDisShipmentTaskItemEntity entity);

    void update(NxDisShipmentTaskItemEntity entity);
}
