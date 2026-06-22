package com.nongxinle.service;

import com.nongxinle.dto.route.DriverRouteLoadingGateRequest;

import java.util.Map;

public interface DisRouteSandboxRouteLoadingGateService {

    Map<String, Object> enterLoading(Integer driverRouteId, DriverRouteLoadingGateRequest request) throws Exception;

    Map<String, Object> returnToDispatch(Integer driverRouteId, DriverRouteLoadingGateRequest request) throws Exception;
}
