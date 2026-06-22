package com.nongxinle.service;

import com.nongxinle.dto.route.SandboxStopConfirmRequest;

import java.util.Map;

public interface DisRouteSandboxConfirmService {

    Map<String, Object> confirmStop(SandboxStopConfirmRequest request) throws Exception;
}
