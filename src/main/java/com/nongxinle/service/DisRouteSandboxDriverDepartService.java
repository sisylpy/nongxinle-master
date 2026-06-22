package com.nongxinle.service;

import com.nongxinle.dto.route.DriverRouteDepartRequest;

import java.util.Map;

/** Phase 3D：司机路线确认出发 */
public interface DisRouteSandboxDriverDepartService {

    Map<String, Object> depart(Integer driverRouteId, DriverRouteDepartRequest request) throws Exception;

    /** 装车页按 driverUserId 确认出发，返回装车读模型 */
    Map<String, Object> departByDriverUserId(DriverRouteDepartRequest request) throws Exception;
}
