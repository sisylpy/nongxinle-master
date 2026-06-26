package com.nongxinle.service;

import com.nongxinle.dto.route.SandboxStopTimeWindowRequest;

import java.util.Map;

/** 沙箱当日送达时间窗 override（confirm 前按 dep 存储；confirm 后委托 task 级服务）。 */
public interface DisRouteSandboxStopTimeWindowService {

    Map<String, Object> updateStopTimeWindow(SandboxStopTimeWindowRequest request) throws Exception;
}
