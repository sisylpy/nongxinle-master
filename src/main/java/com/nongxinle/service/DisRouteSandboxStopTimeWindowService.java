package com.nongxinle.service;

import com.nongxinle.dto.route.SandboxStopTimeWindowRequest;

import java.util.Map;

/** 沙箱当日送达时间窗 override（confirm 前按 dep 存储；confirm 后委托 task 级服务）。 */
public interface DisRouteSandboxStopTimeWindowService {

    Map<String, Object> updateStopTimeWindow(SandboxStopTimeWindowRequest request) throws Exception;

    /** 送达或回沙盘后清除当日门店时间窗调整（同店新单走常规窗）。 */
    void clearTodayOverride(Integer disId, String routeDate, Integer depFatherId);
}
