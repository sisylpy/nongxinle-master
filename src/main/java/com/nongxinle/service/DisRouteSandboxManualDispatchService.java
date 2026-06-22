package com.nongxinle.service;

import com.nongxinle.dto.route.SandboxManualDispatchBaseRequest;
import com.nongxinle.dto.route.SandboxManualDispatchDriverPanoramaResponse;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageRequest;
import com.nongxinle.dto.route.SandboxManualDispatchEditPageResponse;
import com.nongxinle.dto.route.SandboxManualDispatchSimulateRequest;
import com.nongxinle.dto.route.SandboxManualDispatchSimulateResponse;
import com.nongxinle.dto.route.SandboxStopConfirmRequest;

import java.util.Map;

public interface DisRouteSandboxManualDispatchService {

    SandboxManualDispatchDriverPanoramaResponse listDriverPanorama(SandboxManualDispatchBaseRequest request)
            throws Exception;

    SandboxManualDispatchSimulateResponse simulate(SandboxManualDispatchSimulateRequest request) throws Exception;

    SandboxManualDispatchEditPageResponse buildEditPage(SandboxManualDispatchEditPageRequest request) throws Exception;

    Map<String, Object> confirmManualDispatch(SandboxStopConfirmRequest request) throws Exception;
}
