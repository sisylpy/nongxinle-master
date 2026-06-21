package com.nongxinle.dao;

import com.nongxinle.dto.route.DriverAvailableDto;
import com.nongxinle.entity.NxDisDriverDutyEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NxDisDriverDutyDao {

    NxDisDriverDutyEntity queryByDisDriverDate(@Param("disId") Integer disId,
                                               @Param("driverUserId") Integer driverUserId,
                                               @Param("dutyDate") String dutyDate);

    List<DriverAvailableDto> queryOnDutyDrivers(@Param("disId") Integer disId,
                                                  @Param("dutyDate") String dutyDate);

    void save(NxDisDriverDutyEntity entity);

    void update(NxDisDriverDutyEntity entity);
}
