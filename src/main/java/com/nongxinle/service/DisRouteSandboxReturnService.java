package com.nongxinle.service;

import com.nongxinle.dto.route.SandboxStopReturnToSandboxRequest;

import java.util.Map;

/** Phase 3c：已确认店返回动态沙盘 */
public interface DisRouteSandboxReturnService {

    Map<String, Object> returnToSandbox(Integer deliveryStopId,
                                        SandboxStopReturnToSandboxRequest request) throws Exception;
}
