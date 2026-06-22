package com.nongxinle.service;

import com.nongxinle.dto.route.SandboxComputeRequest;
import com.nongxinle.dto.route.SandboxComputeResult;

/**
 * Phase 3a：动态沙盘计算（GET 只读，不写未确认客户到 DB）。
 */
public interface DisRouteSandboxComputeService {

    SandboxComputeResult compute(SandboxComputeRequest request) throws Exception;
}
