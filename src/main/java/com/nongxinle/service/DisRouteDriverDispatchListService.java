package com.nongxinle.service;

import com.nongxinle.dto.route.DriverDispatchListResponse;

/** Phase 2b-3：司机可派列表（读-only） */
public interface DisRouteDriverDispatchListService {

    DriverDispatchListResponse listDriversForBatch(Integer disId, String routeDate, String batchCode);
}
