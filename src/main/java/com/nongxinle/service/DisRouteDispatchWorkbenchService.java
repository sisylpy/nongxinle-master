package com.nongxinle.service;

import com.nongxinle.dto.route.DispatchWorkbenchDto;
import com.nongxinle.dto.route.RouteFeasibilityResult;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.List;

/** Phase 2b-4：调度处理台摘要（只读） */
public interface DisRouteDispatchWorkbenchService {

    DispatchWorkbenchDto buildEmpty(String routeDate, String dispatchBatch);

    DispatchWorkbenchDto build(NxDisRoutePlanEntity plan,
                               List<NxDisShipmentTaskEntity> tasks,
                               RouteFeasibilityResult feasibility,
                               String routeDate,
                               String dispatchBatch);
}
