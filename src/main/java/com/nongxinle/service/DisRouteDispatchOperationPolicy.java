package com.nongxinle.service;

import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.dto.route.RouteFeasibilityResult;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.List;

/**
 * Phase 2b-2：统一调度操作保护与读模型操作态 enrichment。
 */
public interface DisRouteDispatchOperationPolicy {

    RouteDispatchOperationDecision evaluateAssign(NxDisShipmentTaskEntity task,
                                                    Integer targetDriverUserId,
                                                    RouteFeasibilityResult feasibility);

    RouteDispatchOperationDecision evaluateMove(NxDisShipmentTaskEntity task,
                                                Integer targetDriverUserId,
                                                RouteFeasibilityResult feasibility);

    RouteDispatchOperationDecision evaluateUnlock(NxDisShipmentTaskEntity task);

    /** 真正装车（非 assign）；推进执行态前校验，INFEASIBLE 会阻断 */
    RouteDispatchOperationDecision evaluateConfirmLoad(NxDisShipmentTaskEntity task,
                                                       Integer targetDriverUserId,
                                                       RouteFeasibilityResult feasibility);

    RouteDispatchOperationDecision evaluateBillReadyPromotion(NxDisShipmentTaskEntity task,
                                                              RouteFeasibilityResult feasibility);

    void requireAssign(NxDisShipmentTaskEntity task, Integer targetDriverUserId);

    void requireMove(NxDisShipmentTaskEntity task, Integer targetDriverUserId);

    void requireUnlock(NxDisShipmentTaskEntity task);

    void requireBillReadyPromotion(NxDisShipmentTaskEntity task);

    /** 只读：为 plan / driverRoute / task / stop 填充 canXXX、label 等字段 */
    void enrichPlanReadModel(NxDisRoutePlanEntity plan, RouteFeasibilityResult feasibility);

    /** 只读：为 plan 下独立 tasks 列表填充操作态（与 stop 嵌套 task 规则一致） */
    void enrichTasksReadModel(List<NxDisShipmentTaskEntity> tasks,
                              NxDisRoutePlanEntity plan,
                              RouteFeasibilityResult feasibility);
}
