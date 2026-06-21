package com.nongxinle.service;

import com.nongxinle.dto.route.DriverAvailableDto;
import com.nongxinle.dto.route.DriverDutyRequest;
import com.nongxinle.entity.NxDisDriverDutyEntity;
import com.nongxinle.entity.NxDistributerUserEntity;

import java.util.List;

public interface DisDriverDutyService {

    NxDisDriverDutyEntity checkIn(DriverDutyRequest request);

    NxDisDriverDutyEntity checkOut(DriverDutyRequest request);

    List<DriverAvailableDto> listAvailableDrivers(Integer disId, String routeDate);

    List<NxDistributerUserEntity> listOnDutyDriverUsers(Integer disId, String routeDate);

    void requireDriverOnDuty(Integer disId, Integer driverUserId, String routeDate, String driverName);
}
