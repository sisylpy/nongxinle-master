package com.nongxinle.service;

import com.nongxinle.dto.route.SandboxDriverRouteEditBaseRequest;
import com.nongxinle.dto.route.SandboxDriverRouteEditConfirmRequest;
import com.nongxinle.dto.route.SandboxDriverRouteEditPageResponse;
import com.nongxinle.dto.route.SandboxDriverRouteEditPreviewRequest;

import java.util.Map;

/** 司机路线人工编辑（与未分配客户 manual-dispatch 独立）。 */
public interface DisRouteSandboxDriverRouteEditService {

    SandboxDriverRouteEditPageResponse buildEditPage(SandboxDriverRouteEditBaseRequest request) throws Exception;

    SandboxDriverRouteEditPageResponse preview(SandboxDriverRouteEditPreviewRequest request) throws Exception;

    Map<String, Object> confirm(SandboxDriverRouteEditConfirmRequest request) throws Exception;
}
