package com.nongxinle.route.dispatch.strategy;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DispatchAssignmentPlan {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private DispatchStrategyMode strategyMode;
    private DispatchPlanningPhase planningPhase;
    private String resolvedAt;
    private List<String> warnings = new ArrayList<String>();
    private List<DriverRoutePlan> driverRoutes = new ArrayList<DriverRoutePlan>();
    /** PR-2b：未历史绑定、可进 legacy optimizer 的 pending 站点。 */
    private List<FallbackStopAssignment> fallbackStops = new ArrayList<FallbackStopAssignment>();
    /** PR-2b：confirmed / 执行中 / manualLocked — 仅 debug，永不进 optimizer。 */
    private List<FrozenStopAssignment> frozenStops = new ArrayList<FrozenStopAssignment>();
    private int historyBoundStopCount;
    private int fallbackStopCount;
    private int frozenStopCount;
}
