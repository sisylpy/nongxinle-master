package com.nongxinle.service;

import com.nongxinle.dto.route.ConfirmLoadingRequest;

import java.util.Map;

public interface DisRouteSandboxConfirmLoadingService {

    Map<String, Object> confirmTaskLoading(Integer taskId, ConfirmLoadingRequest request) throws Exception;

    Map<String, Object> confirmRouteLoadingAll(Integer driverRouteId, ConfirmLoadingRequest request) throws Exception;
}
