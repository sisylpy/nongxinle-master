package com.nongxinle.service;

import com.nongxinle.dto.route.DriverRouteDepartRequest;

import java.util.Map;

/** 司机端：现在出发（无额外业务校验，仅写出发时间与基础状态） */
public interface DisRouteSandboxDriverDepartNowService {

    Map<String, Object> departNow(DriverRouteDepartRequest request) throws Exception;
}
