package com.nongxinle.service;

import com.nongxinle.dto.route.DispatchWorkbenchDto;
import com.nongxinle.dto.route.RouteFeasibilityResult;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.Date;
import java.util.List;

/** Phase 2b-4：调度处理台摘要（只读） */
public interface DisRouteDispatchWorkbenchService {

    DispatchWorkbenchDto buildEmpty(String routeDate, String dispatchBatch);

    /** Phase 3a：沙盘无客户时不展示批次时段，标题仅「今天暂无订货客户」等 */
    DispatchWorkbenchDto buildSandboxEmpty(String routeDate, Date serverNow);

    /** Phase 3a：沙盘有客户时的处理台（不用「早班有风险」模板，时段结束后不做风险判断） */
    DispatchWorkbenchDto buildSandbox(NxDisRoutePlanEntity plan,
                                      List<NxDisShipmentTaskEntity> tasks,
                                      RouteFeasibilityResult feasibility,
                                      String routeDate,
                                      String dispatchBatch,
                                      int customerStopCount,
                                      int confirmedStopCount,
                                      Date serverNow);

    DispatchWorkbenchDto build(NxDisRoutePlanEntity plan,
                               List<NxDisShipmentTaskEntity> tasks,
                               RouteFeasibilityResult feasibility,
                               String routeDate,
                               String dispatchBatch);
}
