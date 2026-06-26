package com.nongxinle.route.dispatch.strategy;

import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.route.DisRouteDispatchLabels;
import com.nongxinle.route.DisRouteSandboxScheduleLabelHelper;
import com.nongxinle.route.DisRouteSandboxScheduleMode;
import com.nongxinle.route.DisRouteStopTimeWindowStatus;
import com.nongxinle.route.DisRouteTemporalHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PR-2c：OWNER 模式同司机路线内送达时间窗排序、轻量出发时间与过窗预警。
 * 仅对当前沙盘已保留客户重排站序；不判定配送日/intent，不跨司机改派。
 *
 * <p>站序：L0 人工锁定 → L1 有窗可按时 → L2 有窗已过/预计迟到 → L3 无窗。
 */
public final class OwnerFixedRouteTimeWindowRouteSequencer {

    private static final int DEFAULT_SERVICE_MINUTES = 30;
    private static final String WARNING_WINDOW_MISSED = "已过常规窗口，按补单最快送";
    private static final String WARNING_PROJECTED_LATE_PREFIX = "预计迟到";
    private static final int TIGHT_WINDOW_SLACK_SECONDS = 30 * 60;
    private static final String DEPART_REASON_NOW_LATE_OR_TIGHT = "DEPART_NOW_LATE_OR_TIGHT";
    private static final String DEPART_REASON_BACK_CALC_EARLIEST = "DEPART_BACK_CALC_EARLIEST";
    private static final String DEPART_REASON_NOW = "DEPART_NOW";
    private static final String DEPART_LABEL_NOW_LATE_OR_TIGHT = "现在出发（已过窗或时间较紧）";
    private static final String DEPART_LABEL_BACK_CALC = "按首个时间窗倒推建议出发";
    private static final String DEPART_LABEL_NOW = "现在可送";

    private OwnerFixedRouteTimeWindowRouteSequencer() {
    }

    public static void sequencePlan(DispatchAssignmentPlan plan, DispatchStrategyContext context) {
        if (plan == null || plan.getDriverRoutes() == null || plan.getDriverRoutes().isEmpty()) {
            return;
        }
        SequencingContext seqCtx = SequencingContext.from(context);
        Map<Integer, NxDisShipmentTaskEntity> taskByDep = indexTasks(
                context != null ? context.getOptimizableTasks() : null);
        for (DriverRoutePlan route : plan.getDriverRoutes()) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            RouteDepartPlan departPlan = sequenceStopAssignments(route.getStops(), seqCtx, taskByDep);
            applyDepartPlanToRoute(route, departPlan);
            route.setRouteScheduleMode(DisRouteSandboxScheduleMode.ADHOC_NOW);
        }
    }

    public static void resequenceSuggestedStops(List<NxDisRouteStopEntity> suggestedStops,
                                                  DispatchStrategyContext context,
                                                  DispatchAssignmentPlan plan) {
        if (suggestedStops == null || suggestedStops.isEmpty()) {
            return;
        }
        SequencingContext seqCtx = SequencingContext.from(context);
        Map<Integer, List<NxDisRouteStopEntity>> byDriver = groupSuggestedByDriver(suggestedStops);
        Map<Integer, StopAssignment> planStopByDep = indexPlanStops(plan);

        for (Map.Entry<Integer, List<NxDisRouteStopEntity>> entry : byDriver.entrySet()) {
            List<NxDisRouteStopEntity> driverStops = entry.getValue();
            if (driverStops == null || driverStops.size() <= 1) {
                enrichSuggestedSingleton(driverStops, seqCtx, planStopByDep);
                if (driverStops != null && !driverStops.isEmpty()) {
                    List<RouteStopNode> single = toNodesFromEntities(driverStops, planStopByDep);
                    syncDepartPlanToDriverRoute(plan, entry.getKey(), resolveSuggestedDepart(single, seqCtx));
                }
                continue;
            }
            List<RouteStopNode> nodes = toNodesFromEntities(driverStops, planStopByDep);
            List<RouteStopNode> ordered = orderNodesRespectingManualPins(nodes, seqCtx);
            RouteDepartPlan departPlan = resolveSuggestedDepart(ordered, seqCtx);
            SequencingContext routeCtx = seqCtx.withDepart(departPlan);
            applyOrderToEntities(driverStops, ordered, routeCtx, planStopByDep);
            syncDepartPlanToDriverRoute(plan, entry.getKey(), departPlan);
        }
        rewriteParentStopListOrder(suggestedStops, byDriver);
    }

    /** 将 per-driver 排完序的子列表顺序写回 parent（Proposal/Snapshot 读 list 顺序与 nxDrsStopSeq 一致）。 */
    private static void rewriteParentStopListOrder(List<NxDisRouteStopEntity> parent,
                                                   Map<Integer, List<NxDisRouteStopEntity>> byDriver) {
        if (parent == null || byDriver == null || byDriver.isEmpty()) {
            return;
        }
        java.util.IdentityHashMap<NxDisRouteStopEntity, Boolean> placed
                = new java.util.IdentityHashMap<NxDisRouteStopEntity, Boolean>();
        List<NxDisRouteStopEntity> reordered = new ArrayList<NxDisRouteStopEntity>();
        for (List<NxDisRouteStopEntity> driverStops : byDriver.values()) {
            if (driverStops == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : driverStops) {
                if (stop != null && !placed.containsKey(stop)) {
                    reordered.add(stop);
                    placed.put(stop, Boolean.TRUE);
                }
            }
        }
        for (NxDisRouteStopEntity stop : parent) {
            if (stop != null && !placed.containsKey(stop)) {
                reordered.add(stop);
            }
        }
        parent.clear();
        parent.addAll(reordered);
    }

    /**
     * PR-2c：重排且 leg 已按路线顺序刷新后，按最终站序重算 ETA 并写回 stop / task 读模型。
     */
    public static void recalculateScheduleForMergedPlan(NxDisRoutePlanEntity mergedPlan,
                                                        DispatchAssignmentPlan assignmentPlan,
                                                        DispatchStrategyContext context) {
        if (mergedPlan == null || mergedPlan.getDriverRoutes() == null) {
            return;
        }
        SequencingContext seqCtx = SequencingContext.from(context);
        Map<Integer, StopAssignment> planStopByDep = indexPlanStops(assignmentPlan);
        Map<Integer, DriverRoutePlan> planRouteByDriver = indexDriverRoutePlans(assignmentPlan);
        for (NxDisDriverRouteEntity route : mergedPlan.getDriverRoutes()) {
            if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
                continue;
            }
            Integer driverUserId = route.getNxDdrDriverUserId();
            DriverRoutePlan planRoute = driverUserId != null ? planRouteByDriver.get(driverUserId) : null;
            RouteDepartPlan departPlan = resolveDepartPlanForDriver(route, planRoute, seqCtx, planStopByDep);
            recalculateScheduleForDriverRoute(route, departPlan, seqCtx, planStopByDep);
        }
    }

    /**
     * 仅对当前可见建议站重算 schedule cursor（不含 execution/frozen 混入站）。
     */
    public static void recalculateVisibleScheduleForDriverRoute(NxDisDriverRouteEntity route,
                                                                List<NxDisRouteStopEntity> orderedVisible,
                                                                DispatchAssignmentPlan assignmentPlan,
                                                                DispatchStrategyContext context) {
        if (orderedVisible == null || orderedVisible.isEmpty()) {
            return;
        }
        SequencingContext seqCtx = SequencingContext.from(context);
        Map<Integer, StopAssignment> planStopByDep = indexPlanStops(assignmentPlan);
        Map<Integer, DriverRoutePlan> planRouteByDriver = indexDriverRoutePlans(assignmentPlan);
        Integer driverUserId = route != null ? route.getNxDdrDriverUserId() : null;
        DriverRoutePlan planRoute = driverUserId != null ? planRouteByDriver.get(driverUserId) : null;
        RouteDepartPlan departPlan = resolveDepartPlanForVisible(route, planRoute, orderedVisible, seqCtx, planStopByDep);
        NxDisDriverRouteEntity pseudoRoute = new NxDisDriverRouteEntity();
        pseudoRoute.setNxDdrDriverUserId(driverUserId);
        pseudoRoute.setStops(new ArrayList<NxDisRouteStopEntity>(orderedVisible));
        recalculateScheduleForDriverRoute(pseudoRoute, departPlan, seqCtx, planStopByDep);
        if (route != null) {
            route.setNxDdrPlannedDepartAt(pseudoRoute.getNxDdrPlannedDepartAt());
        }
    }

    /**
     * 分派中可见站 schedule：与老板端展示层一致，首窗未到则按 earliest-leg 倒推出发，
     * 不用 assignmentPlan 里可能滞后的 DEPART_NOW。
     */
    private static RouteDepartPlan resolveDepartPlanForVisible(NxDisDriverRouteEntity route,
                                                               DriverRoutePlan planRoute,
                                                               List<NxDisRouteStopEntity> orderedVisible,
                                                               SequencingContext seqCtx,
                                                               Map<Integer, StopAssignment> planStopByDep) {
        if (orderedVisible == null || orderedVisible.isEmpty()) {
            return RouteDepartPlan.now(seqCtx);
        }
        List<RouteStopNode> nodes = toNodesFromEntities(orderedVisible, planStopByDep);
        RouteStopNode firstWindowed = firstWindowedStop(nodes);
        if (firstWindowed != null && firstWindowed.earliestSeconds != null
                && firstWindowed.legDurationS > 0L
                && !isWindowAlreadyMissed(firstWindowed, seqCtx)) {
            long backCalcDepart = firstWindowed.earliestSeconds - firstWindowed.legDurationS;
            if (backCalcDepart > seqCtx.serverNowSeconds) {
                return RouteDepartPlan.at(backCalcDepart,
                        DEPART_REASON_BACK_CALC_EARLIEST, DEPART_LABEL_BACK_CALC);
            }
        }
        return resolveSuggestedDepart(nodes, seqCtx);
    }

    /** 重排后丢弃 depot→站 leg，迫使后续 leg metrics 按路线顺序重算 segment leg。 */
    public static void invalidateLegMetrics(List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            stop.setNxDrsLegDurationS(null);
            stop.setNxDrsLegDistanceM(null);
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task != null) {
                task.setNxDstLegDurationS(null);
                task.setNxDstLegDistanceM(null);
            }
        }
    }

    public static SequencingSnapshot buildSequencingSnapshot(DispatchStrategyContext context) {
        return SequencingSnapshot.from(SequencingContext.from(context));
    }

    public static List<RouteStopSnapshot> toStopSnapshots(List<NxDisRouteStopEntity> stops,
                                                          Map<Integer, StopAssignment> planStopByDep) {
        List<RouteStopSnapshot> snapshots = new ArrayList<RouteStopSnapshot>();
        if (stops == null) {
            return snapshots;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop != null) {
                snapshots.add(toSnapshot(toNodeFromEntity(stop, planStopByDep)));
            }
        }
        return snapshots;
    }

    public static RouteDepartSnapshot resolveSuggestedDepartSnapshot(List<RouteStopSnapshot> ordered,
                                                                    SequencingSnapshot seqCtx) {
        RouteDepartPlan departPlan = resolveSuggestedDepart(toNodes(ordered), toContext(seqCtx));
        RouteDepartSnapshot snapshot = new RouteDepartSnapshot();
        snapshot.departSeconds = departPlan.departSeconds;
        snapshot.reasonCode = departPlan.reasonCode;
        snapshot.reasonLabel = departPlan.reasonLabel;
        return snapshot;
    }

    public static StopPreviewSnapshot computeStopPreviewSnapshot(RouteStopSnapshot node,
                                                                 SequencingSnapshot seqCtx,
                                                                 long cursorSeconds) {
        return computeStopPreviewSnapshot(node, seqCtx, cursorSeconds, true);
    }

    public static StopPreviewSnapshot computeStopPreviewSnapshot(RouteStopSnapshot node,
                                                                 SequencingSnapshot seqCtx,
                                                                 long cursorSeconds,
                                                                 boolean allowDepotWindowWait) {
        PreviewFields preview = computePreviewFields(
                toNode(node), toContext(seqCtx), cursorSeconds, allowDepotWindowWait);
        return StopPreviewSnapshot.from(preview);
    }

    public static DispatchSequenceBucket classifySequenceBucket(RouteStopSnapshot node,
                                                                SequencingSnapshot seqCtx,
                                                                long cursorBeforeStop) {
        if (node == null) {
            return DispatchSequenceBucket.L3_NO_WINDOW;
        }
        if (node.isManualSeqPinned()) {
            return DispatchSequenceBucket.L0_MANUAL_LOCKED;
        }
        if (node.hasNoWindow()) {
            return DispatchSequenceBucket.L3_NO_WINDOW;
        }
        SequencingContext ctx = toContext(seqCtx);
        RouteStopNode routeNode = toNode(node);
        if (isWindowAlreadyMissed(routeNode, ctx)) {
            return DispatchSequenceBucket.L2_TIMED_LATE;
        }
        long arrival = cursorBeforeStop + node.legDurationS;
        long serviceStart = arrival;
        if (node.earliestSeconds != null && arrival < node.earliestSeconds) {
            serviceStart = node.earliestSeconds;
        }
        if (node.latestSeconds != null && serviceStart > node.latestSeconds) {
            return DispatchSequenceBucket.L2_TIMED_LATE;
        }
        return DispatchSequenceBucket.L1_TIMED_ON_TIME;
    }

    public static String resolveSequenceReason(DispatchSequenceBucket bucket,
                                               RouteStopSnapshot node,
                                               StopPreviewSnapshot preview,
                                               SequencingSnapshot seqCtx) {
        if (bucket == null) {
            return null;
        }
        switch (bucket) {
            case L0_MANUAL_LOCKED:
                return "人工锁定顺序，固定第" + (node != null ? node.manualStopSeq : "?") + "位";
            case L1_TIMED_ON_TIME:
                return "有窗且可按时/接近窗口，按 latest 优先";
            case L2_TIMED_LATE:
                if (preview != null && preview.status == DispatchTimeWindowDebugStatus.WINDOW_MISSED) {
                    return "L2 过窗段：已过 latest，"
                            + buildSequenceSortKey(node, seqCtx, bucket)
                            + "；保留在路线并提示补单最快送";
                }
                return "L2 过窗段：预计超过 latest，"
                        + buildSequenceSortKey(node, seqCtx, bucket)
                        + "；保留在路线";
            case L3_NO_WINDOW:
                return "无时间窗，排在有窗客户之后；"
                        + buildSequenceSortKey(node, seqCtx, bucket);
            default:
                return null;
        }
    }

    public static String buildSequenceSortKey(RouteStopSnapshot node,
                                              SequencingSnapshot seqCtx,
                                              DispatchSequenceBucket bucket) {
        if (node == null || bucket == null) {
            return "";
        }
        StringBuilder key = new StringBuilder();
        if (bucket == DispatchSequenceBucket.L2_TIMED_LATE && seqCtx != null) {
            key.append("overdueSec=").append(overdueSeveritySeconds(toNode(node), toContext(seqCtx)));
            key.append(", legSec=").append(node.legDurationS);
            if (node.latestSeconds != null) {
                key.append(", latestS=").append(node.latestSeconds);
            }
            return key.toString();
        }
        if (bucket == DispatchSequenceBucket.L1_TIMED_ON_TIME) {
            key.append("latestS=").append(node.latestSeconds != null ? node.latestSeconds : "null");
            key.append(", legSec=").append(node.legDurationS);
            return key.toString();
        }
        if (bucket == DispatchSequenceBucket.L3_NO_WINDOW) {
            key.append("legSec=").append(node.legDurationS);
            return key.toString();
        }
        return "";
    }

    /** debug：同紧急级别内距离/leg 破同说明（历史偏好不参与站序）。 */
    public static String resolveDistanceTieBreakReason(RouteStopSnapshot node,
                                                       DispatchSequenceBucket bucket) {
        if (node == null || bucket == null || bucket == DispatchSequenceBucket.L0_MANUAL_LOCKED) {
            return null;
        }
        if (node.legDurationS <= 0L) {
            return null;
        }
        switch (bucket) {
            case L1_TIMED_ON_TIME:
                return "同 latest 时按 legDurationS=" + node.legDurationS + "s 更近优先";
            case L2_TIMED_LATE:
                return "同过窗严重度时按 legDurationS=" + node.legDurationS + "s 更近优先";
            case L3_NO_WINDOW:
                return "无窗段按 legDurationS=" + node.legDurationS + "s 更近优先";
            default:
                return null;
        }
    }

    public static final class SequencingSnapshot {
        public final String routeDate;
        public final Date serverNow;
        public final long serverNowSeconds;
        public final long dayStartMs;

        private SequencingSnapshot(String routeDate, Date serverNow, long serverNowSeconds, long dayStartMs) {
            this.routeDate = routeDate;
            this.serverNow = serverNow;
            this.serverNowSeconds = serverNowSeconds;
            this.dayStartMs = dayStartMs;
        }

        static SequencingSnapshot from(SequencingContext ctx) {
            return new SequencingSnapshot(ctx.routeDate, ctx.serverNow, ctx.serverNowSeconds, ctx.dayStartMs);
        }
    }

    public static final class RouteStopSnapshot {
        public Integer depFatherId;
        public Integer earliestSeconds;
        public Integer latestSeconds;
        public Integer serviceMinutes;
        public Integer historyAvgStopSeq;
        public long legDurationS;
        public boolean manualLocked;
        public Integer manualStopSeq;

        public boolean hasNoWindow() {
            return earliestSeconds == null && latestSeconds == null;
        }

        public boolean isManualSeqPinned() {
            return manualLocked && manualStopSeq != null && manualStopSeq > 0;
        }
    }

    public static final class RouteDepartSnapshot {
        public long departSeconds;
        public String reasonCode;
        public String reasonLabel;
    }

    public static final class StopPreviewSnapshot {
        public Integer projectedArrivalTimeS;
        public long serviceEndSeconds;
        public DispatchTimeWindowDebugStatus status;
        public Integer lateMinutes;
        public Integer waitMinutes;
        public String warningLabel;

        static StopPreviewSnapshot from(PreviewFields preview) {
            StopPreviewSnapshot snapshot = new StopPreviewSnapshot();
            snapshot.projectedArrivalTimeS = preview.projectedArrivalTimeS;
            snapshot.serviceEndSeconds = preview.serviceEndSeconds;
            snapshot.status = preview.status;
            snapshot.lateMinutes = preview.lateMinutes;
            snapshot.waitMinutes = preview.waitMinutes;
            snapshot.warningLabel = preview.warningLabel;
            return snapshot;
        }
    }

    private static RouteStopSnapshot toSnapshot(RouteStopNode node) {
        RouteStopSnapshot snapshot = new RouteStopSnapshot();
        snapshot.depFatherId = node.depFatherId;
        snapshot.earliestSeconds = node.earliestSeconds;
        snapshot.latestSeconds = node.latestSeconds;
        snapshot.serviceMinutes = node.serviceMinutes;
        snapshot.historyAvgStopSeq = node.historyAvgStopSeq;
        snapshot.legDurationS = node.legDurationS;
        snapshot.manualLocked = node.manualLocked;
        snapshot.manualStopSeq = node.manualStopSeq;
        return snapshot;
    }

    private static RouteStopNode toNode(RouteStopSnapshot snapshot) {
        RouteStopNode node = new RouteStopNode();
        if (snapshot == null) {
            return node;
        }
        node.depFatherId = snapshot.depFatherId;
        node.earliestSeconds = snapshot.earliestSeconds;
        node.latestSeconds = snapshot.latestSeconds;
        node.serviceMinutes = snapshot.serviceMinutes;
        node.historyAvgStopSeq = snapshot.historyAvgStopSeq;
        node.legDurationS = snapshot.legDurationS;
        node.manualLocked = snapshot.manualLocked;
        node.manualStopSeq = snapshot.manualStopSeq;
        return node;
    }

    private static List<RouteStopNode> toNodes(List<RouteStopSnapshot> snapshots) {
        List<RouteStopNode> nodes = new ArrayList<RouteStopNode>();
        if (snapshots == null) {
            return nodes;
        }
        for (RouteStopSnapshot snapshot : snapshots) {
            nodes.add(toNode(snapshot));
        }
        return nodes;
    }

    private static SequencingContext toContext(SequencingSnapshot snapshot) {
        if (snapshot == null) {
            return SequencingContext.from(DispatchStrategyContext.builder().build());
        }
        return new SequencingContext(snapshot.routeDate, snapshot.serverNow, snapshot.serverNowSeconds,
                snapshot.serverNowSeconds, DEPART_REASON_NOW, DEPART_LABEL_NOW, snapshot.dayStartMs);
    }

    private static RouteDepartPlan sequenceStopAssignments(List<StopAssignment> stops,
                                                SequencingContext seqCtx,
                                                Map<Integer, NxDisShipmentTaskEntity> taskByDep) {
        if (stops == null || stops.isEmpty()) {
            return RouteDepartPlan.now(seqCtx);
        }
        if (stops.size() == 1) {
            List<RouteStopNode> single = toNodesFromAssignments(stops, taskByDep);
            RouteDepartPlan departPlan = resolveSuggestedDepart(single, seqCtx);
            SequencingContext routeCtx = seqCtx.withDepart(departPlan);
            enrichAssignmentPreview(stops.get(0), routeCtx, taskByDep, routeCtx.initialDepartSeconds, 1);
            return departPlan;
        }
        List<RouteStopNode> nodes = toNodesFromAssignments(stops, taskByDep);
        List<RouteStopNode> ordered = orderNodesRespectingManualPins(nodes, seqCtx);
        RouteDepartPlan departPlan = resolveSuggestedDepart(ordered, seqCtx);
        SequencingContext routeCtx = seqCtx.withDepart(departPlan);
        applyOrderToAssignments(stops, ordered, routeCtx, taskByDep);
        return departPlan;
    }

    private static List<RouteStopNode> orderNodesRespectingManualPins(List<RouteStopNode> nodes,
                                                                      SequencingContext seqCtx) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<RouteStopNode> pinned = new ArrayList<RouteStopNode>();
        List<RouteStopNode> flexible = new ArrayList<RouteStopNode>();
        for (RouteStopNode node : nodes) {
            if (node.isManualSeqPinned()) {
                pinned.add(node);
            } else {
                flexible.add(node);
            }
        }
        if (pinned.isEmpty()) {
            return orderNodes(flexible, seqCtx);
        }
        return mergePinnedAndFlexible(pinned, orderNodes(flexible, seqCtx));
    }

    private static List<RouteStopNode> mergePinnedAndFlexible(List<RouteStopNode> pinned,
                                                              List<RouteStopNode> flexOrdered) {
        Map<Integer, RouteStopNode> pinBySeq = new HashMap<Integer, RouteStopNode>();
        int maxPinSeq = 0;
        for (RouteStopNode node : pinned) {
            if (node.manualStopSeq != null && node.manualStopSeq > 0) {
                pinBySeq.put(node.manualStopSeq, node);
                maxPinSeq = Math.max(maxPinSeq, node.manualStopSeq);
            }
        }
        int totalSlots = Math.max(flexOrdered.size() + pinned.size(), maxPinSeq);
        List<RouteStopNode> merged = new ArrayList<RouteStopNode>(totalSlots);
        int flexIdx = 0;
        java.util.Set<Integer> placedDepIds = new java.util.HashSet<Integer>();
        for (int slot = 1; slot <= totalSlots; slot++) {
            RouteStopNode pinnedNode = pinBySeq.get(slot);
            if (pinnedNode != null) {
                merged.add(pinnedNode);
                placedDepIds.add(pinnedNode.depFatherId);
            } else if (flexIdx < flexOrdered.size()) {
                merged.add(flexOrdered.get(flexIdx++));
            }
        }
        while (flexIdx < flexOrdered.size()) {
            merged.add(flexOrdered.get(flexIdx++));
        }
        for (RouteStopNode node : pinned) {
            if (node.depFatherId != null && !placedDepIds.contains(node.depFatherId)) {
                merged.add(node);
            }
        }
        return merged;
    }

    private static List<RouteStopNode> orderNodes(List<RouteStopNode> nodes, SequencingContext seqCtx) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<RouteStopNode> timedFeasible = new ArrayList<RouteStopNode>();
        List<RouteStopNode> noWindow = new ArrayList<RouteStopNode>();
        List<RouteStopNode> lateBucket = new ArrayList<RouteStopNode>();

        for (RouteStopNode node : nodes) {
            if (node.hasNoWindow()) {
                noWindow.add(node);
            } else if (isWindowAlreadyMissed(node, seqCtx)) {
                lateBucket.add(node);
            } else {
                timedFeasible.add(node);
            }
        }

        sortTimedFeasible(timedFeasible);
        promoteProjectedLate(timedFeasible, lateBucket, seqCtx);
        sortNoWindow(noWindow);
        sortLateBucket(lateBucket, seqCtx, nodes.size() == lateBucket.size());

        List<RouteStopNode> ordered = new ArrayList<RouteStopNode>(
                timedFeasible.size() + lateBucket.size() + noWindow.size());
        ordered.addAll(timedFeasible);
        ordered.addAll(lateBucket);
        ordered.addAll(noWindow);
        return ordered;
    }

    private static RouteDepartPlan resolveSuggestedDepart(List<RouteStopNode> ordered, SequencingContext seqCtx) {
        if (ordered == null || ordered.isEmpty()) {
            return RouteDepartPlan.now(seqCtx);
        }
        if (simulateLeaveNowLateOrTight(ordered, seqCtx)) {
            return RouteDepartPlan.at(seqCtx.serverNowSeconds,
                    DEPART_REASON_NOW_LATE_OR_TIGHT, DEPART_LABEL_NOW_LATE_OR_TIGHT);
        }
        if (allWindowedStopsAreFuture(ordered, seqCtx) && wouldArriveTooEarlyIfLeaveNow(ordered, seqCtx)) {
            RouteStopNode firstWindowed = firstWindowedStop(ordered);
            if (firstWindowed != null && firstWindowed.earliestSeconds != null) {
                long suggested = firstWindowed.earliestSeconds - firstWindowed.legDurationS;
                if (suggested > seqCtx.serverNowSeconds) {
                    return RouteDepartPlan.at(suggested,
                            DEPART_REASON_BACK_CALC_EARLIEST, DEPART_LABEL_BACK_CALC);
                }
            }
        }
        return RouteDepartPlan.at(seqCtx.serverNowSeconds, DEPART_REASON_NOW, DEPART_LABEL_NOW);
    }

    private static boolean simulateLeaveNowLateOrTight(List<RouteStopNode> ordered, SequencingContext seqCtx) {
        long cursor = seqCtx.serverNowSeconds;
        for (RouteStopNode node : ordered) {
            if (node.hasNoWindow()) {
                cursor += node.legDurationS + serviceSeconds(node);
                continue;
            }
            if (isWindowAlreadyMissed(node, seqCtx)) {
                return true;
            }
            long arrival = cursor + node.legDurationS;
            long serviceStart = arrival;
            if (node.earliestSeconds != null && arrival < node.earliestSeconds) {
                serviceStart = node.earliestSeconds;
            }
            if (node.latestSeconds != null && serviceStart > node.latestSeconds) {
                return true;
            }
            if (node.latestSeconds != null
                    && (node.latestSeconds - serviceStart) <= TIGHT_WINDOW_SLACK_SECONDS) {
                return true;
            }
            cursor = serviceStart + serviceSeconds(node);
        }
        return false;
    }

    private static boolean allWindowedStopsAreFuture(List<RouteStopNode> ordered, SequencingContext seqCtx) {
        boolean hasWindowed = false;
        for (RouteStopNode node : ordered) {
            if (node == null || node.hasNoWindow()) {
                continue;
            }
            hasWindowed = true;
            if (isWindowAlreadyMissed(node, seqCtx)) {
                return false;
            }
        }
        return hasWindowed;
    }

    private static boolean wouldArriveTooEarlyIfLeaveNow(List<RouteStopNode> ordered, SequencingContext seqCtx) {
        long cursor = seqCtx.serverNowSeconds;
        for (RouteStopNode node : ordered) {
            if (node == null || node.hasNoWindow()) {
                if (node != null) {
                    cursor += node.legDurationS + serviceSeconds(node);
                }
                continue;
            }
            if (isWindowAlreadyMissed(node, seqCtx)) {
                return false;
            }
            long arrival = cursor + node.legDurationS;
            if (node.earliestSeconds != null && arrival < node.earliestSeconds) {
                return true;
            }
            long serviceStart = arrival;
            if (node.earliestSeconds != null && arrival < node.earliestSeconds) {
                serviceStart = node.earliestSeconds;
            }
            cursor = serviceStart + serviceSeconds(node);
        }
        return false;
    }

    private static RouteStopNode firstWindowedStop(List<RouteStopNode> ordered) {
        if (ordered == null) {
            return null;
        }
        for (RouteStopNode node : ordered) {
            if (node != null && !node.hasNoWindow()) {
                return node;
            }
        }
        return null;
    }

    private static void applyDepartPlanToRoute(DriverRoutePlan route, RouteDepartPlan departPlan) {
        if (route == null || departPlan == null) {
            return;
        }
        route.setSuggestedDepartTimeS((int) departPlan.departSeconds);
        route.setPlannedDepartTimeS((int) departPlan.departSeconds);
        route.setSuggestedDepartReason(departPlan.reasonCode);
        route.setSuggestedDepartReasonLabel(departPlan.reasonLabel);
    }

    private static void syncDepartPlanToDriverRoute(DispatchAssignmentPlan plan,
                                                    Integer driverUserId,
                                                    RouteDepartPlan departPlan) {
        if (plan == null || plan.getDriverRoutes() == null || driverUserId == null || departPlan == null) {
            return;
        }
        for (DriverRoutePlan route : plan.getDriverRoutes()) {
            if (route != null && driverUserId.equals(route.getDriverUserId())) {
                applyDepartPlanToRoute(route, departPlan);
                return;
            }
        }
    }

    private static void sortTimedFeasible(List<RouteStopNode> timedFeasible) {
        Collections.sort(timedFeasible, new Comparator<RouteStopNode>() {
            @Override
            public int compare(RouteStopNode a, RouteStopNode b) {
                int latestCmp = compareNullableInt(a.latestSeconds, b.latestSeconds);
                if (latestCmp != 0) {
                    return latestCmp;
                }
                int legCmp = Long.compare(a.legDurationS, b.legDurationS);
                if (legCmp != 0) {
                    return legCmp;
                }
                return compareNullableInt(a.depFatherId, b.depFatherId);
            }
        });
    }

    private static void promoteProjectedLate(List<RouteStopNode> timedFeasible,
                                             List<RouteStopNode> lateBucket,
                                             SequencingContext seqCtx) {
        if (timedFeasible.isEmpty()) {
            return;
        }
        long cursor = seqCtx.initialDepartSeconds;
        List<RouteStopNode> stillFeasible = new ArrayList<RouteStopNode>();
        for (RouteStopNode node : timedFeasible) {
            long arrival = cursor + node.legDurationS;
            if (node.latestSeconds != null && arrival > node.latestSeconds) {
                lateBucket.add(node);
            } else {
                stillFeasible.add(node);
            }
            long serviceSeconds = node.serviceMinutes != null
                    ? node.serviceMinutes.longValue() * 60L : DEFAULT_SERVICE_MINUTES * 60L;
            long serviceStart = arrival;
            if (node.earliestSeconds != null && arrival < node.earliestSeconds) {
                serviceStart = node.earliestSeconds;
            }
            cursor = serviceStart + serviceSeconds;
        }
        timedFeasible.clear();
        timedFeasible.addAll(stillFeasible);
    }

    private static void sortNoWindow(List<RouteStopNode> noWindow) {
        Collections.sort(noWindow, new Comparator<RouteStopNode>() {
            @Override
            public int compare(RouteStopNode a, RouteStopNode b) {
                int legCmp = Long.compare(a.legDurationS, b.legDurationS);
                if (legCmp != 0) {
                    return legCmp;
                }
                return compareNullableInt(a.depFatherId, b.depFatherId);
            }
        });
    }

    private static void sortLateBucket(List<RouteStopNode> lateBucket,
                                       SequencingContext seqCtx,
                                       boolean allLate) {
        Collections.sort(lateBucket, new Comparator<RouteStopNode>() {
            @Override
            public int compare(RouteStopNode a, RouteStopNode b) {
                int severityCmp = Integer.compare(
                        overdueSeveritySeconds(b, seqCtx), overdueSeveritySeconds(a, seqCtx));
                if (severityCmp != 0) {
                    return severityCmp;
                }
                int legCmp = Long.compare(a.legDurationS, b.legDurationS);
                if (legCmp != 0) {
                    return legCmp;
                }
                int serviceCmp = compareNullableInt(a.serviceMinutes, b.serviceMinutes);
                if (serviceCmp != 0) {
                    return serviceCmp;
                }
                return compareNullableInt(a.depFatherId, b.depFatherId);
            }
        });
    }

    private static int overdueSeveritySeconds(RouteStopNode node, SequencingContext seqCtx) {
        if (node.latestSeconds != null) {
            return (int) Math.max(0L, seqCtx.serverNowSeconds - node.latestSeconds);
        }
        if (node.earliestSeconds != null) {
            return (int) Math.max(0L, seqCtx.serverNowSeconds - node.earliestSeconds);
        }
        return 0;
    }

    private static boolean isWindowAlreadyMissed(RouteStopNode node, SequencingContext seqCtx) {
        if (node.latestSeconds == null) {
            return false;
        }
        return seqCtx.serverNowSeconds > node.latestSeconds;
    }

    private static void applyOrderToAssignments(List<StopAssignment> stops,
                                                List<RouteStopNode> ordered,
                                                SequencingContext seqCtx,
                                                Map<Integer, NxDisShipmentTaskEntity> taskByDep) {
        Map<Integer, StopAssignment> byDep = new HashMap<Integer, StopAssignment>();
        for (StopAssignment stop : stops) {
            if (stop != null && stop.getDepFatherId() != null) {
                byDep.put(stop.getDepFatherId(), stop);
            }
        }
        stops.clear();
        long cursor = seqCtx.initialDepartSeconds;
        int seq = 1;
        for (RouteStopNode node : ordered) {
            StopAssignment assignment = byDep.get(node.depFatherId);
            if (assignment == null) {
                continue;
            }
            enrichAssignmentPreview(assignment, seqCtx, taskByDep, cursor, seq++);
            cursor = assignment.getProjectedServiceEndSeconds() != null
                    ? assignment.getProjectedServiceEndSeconds() : cursor;
            stops.add(assignment);
        }
    }

    private static void applyOrderToEntities(List<NxDisRouteStopEntity> driverStops,
                                               List<RouteStopNode> ordered,
                                               SequencingContext seqCtx,
                                               Map<Integer, StopAssignment> planStopByDep) {
        Map<Integer, NxDisRouteStopEntity> byDep = new HashMap<Integer, NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : driverStops) {
            Integer depId = resolveDepFatherId(stop);
            if (depId != null) {
                byDep.put(depId, stop);
            }
        }
        driverStops.clear();
        long cursor = seqCtx.initialDepartSeconds;
        int seq = 1;
        for (RouteStopNode node : ordered) {
            NxDisRouteStopEntity stop = byDep.get(node.depFatherId);
            if (stop == null) {
                continue;
            }
            stop.setNxDrsStopSeq(seq);
            StopAssignment planStop = planStopByDep.get(node.depFatherId);
            PreviewFields preview = computePreviewFields(node, seqCtx, cursor, seq == 1);
            if (planStop != null) {
                applyPreviewToAssignment(planStop, preview, seq);
            }
            applyPreviewToStopEntity(stop, node, preview, seqCtx, seq == 1);
            cursor = preview.serviceEndSeconds;
            driverStops.add(stop);
            seq++;
        }
    }

    private static void enrichSuggestedSingleton(List<NxDisRouteStopEntity> driverStops,
                                                   SequencingContext seqCtx,
                                                   Map<Integer, StopAssignment> planStopByDep) {
        if (driverStops == null || driverStops.isEmpty()) {
            return;
        }
        NxDisRouteStopEntity stop = driverStops.get(0);
        if (stop == null) {
            return;
        }
        stop.setNxDrsStopSeq(1);
        Integer depId = resolveDepFatherId(stop);
        RouteStopNode node = toNodeFromEntity(stop, planStopByDep);
        StopAssignment planStop = depId != null ? planStopByDep.get(depId) : null;
        RouteDepartPlan departPlan = resolveSuggestedDepart(Collections.singletonList(node), seqCtx);
        SequencingContext routeCtx = seqCtx.withDepart(departPlan);
        PreviewFields preview = computePreviewFields(node, routeCtx, routeCtx.initialDepartSeconds, true);
        if (planStop != null) {
            applyPreviewToAssignment(planStop, preview, 1);
        }
        applyPreviewToStopEntity(stop, node, preview, routeCtx, true);
    }

    private static void recalculateScheduleForDriverRoute(NxDisDriverRouteEntity route,
                                                          RouteDepartPlan departPlan,
                                                          SequencingContext seqCtx,
                                                          Map<Integer, StopAssignment> planStopByDep) {
        if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
            return;
        }
        List<NxDisRouteStopEntity> ordered = new ArrayList<NxDisRouteStopEntity>(route.getStops());
        sortStopsBySeq(ordered);
        SequencingContext routeCtx = seqCtx.withDepart(departPlan);
        long cursor = routeCtx.initialDepartSeconds;
        int seq = 1;
        for (NxDisRouteStopEntity stop : ordered) {
            if (stop == null) {
                continue;
            }
            RouteStopNode node = toNodeFromEntity(stop, planStopByDep);
            PreviewFields preview = computePreviewFields(node, routeCtx, cursor, seq == 1);
            stop.setNxDrsStopSeq(seq);
            StopAssignment planStop = node.depFatherId != null ? planStopByDep.get(node.depFatherId) : null;
            if (planStop != null) {
                applyPreviewToAssignment(planStop, preview, seq);
            }
            applyPreviewToStopEntity(stop, node, preview, routeCtx, seq == 1);
            cursor = preview.serviceEndSeconds;
            seq++;
        }
        route.getStops().clear();
        route.getStops().addAll(ordered);
        if (routeCtx.dayStartMs > 0L) {
            route.setNxDdrPlannedDepartAt(new Date(routeCtx.dayStartMs + routeCtx.initialDepartSeconds * 1000L));
        }
    }

    private static RouteDepartPlan resolveDepartPlanForDriver(NxDisDriverRouteEntity route,
                                                              DriverRoutePlan planRoute,
                                                              SequencingContext seqCtx,
                                                              Map<Integer, StopAssignment> planStopByDep) {
        if (planRoute != null && planRoute.getPlannedDepartTimeS() != null) {
            return RouteDepartPlan.at(
                    planRoute.getPlannedDepartTimeS().longValue(),
                    planRoute.getSuggestedDepartReason(),
                    planRoute.getSuggestedDepartReasonLabel());
        }
        if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
            return RouteDepartPlan.now(seqCtx);
        }
        List<RouteStopNode> nodes = toNodesFromEntities(route.getStops(), planStopByDep);
        return resolveSuggestedDepart(nodes, seqCtx);
    }

    private static void sortStopsBySeq(List<NxDisRouteStopEntity> stops) {
        Collections.sort(stops, new Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                int seqA = a != null && a.getNxDrsStopSeq() != null ? a.getNxDrsStopSeq() : 999;
                int seqB = b != null && b.getNxDrsStopSeq() != null ? b.getNxDrsStopSeq() : 999;
                return Integer.compare(seqA, seqB);
            }
        });
    }

    private static Map<Integer, DriverRoutePlan> indexDriverRoutePlans(DispatchAssignmentPlan plan) {
        Map<Integer, DriverRoutePlan> map = new LinkedHashMap<Integer, DriverRoutePlan>();
        if (plan == null || plan.getDriverRoutes() == null) {
            return map;
        }
        for (DriverRoutePlan route : plan.getDriverRoutes()) {
            if (route != null && route.getDriverUserId() != null) {
                map.put(route.getDriverUserId(), route);
            }
        }
        return map;
    }

    private static void applyPreviewToStopEntity(NxDisRouteStopEntity stop,
                                               RouteStopNode node,
                                               PreviewFields preview,
                                               SequencingContext seqCtx) {
        applyPreviewToStopEntity(stop, node, preview, seqCtx, true);
    }

    private static void applyPreviewToStopEntity(NxDisRouteStopEntity stop,
                                               RouteStopNode node,
                                               PreviewFields preview,
                                               SequencingContext seqCtx,
                                               boolean allowDepotWindowWait) {
        if (stop == null || preview == null || seqCtx == null) {
            return;
        }
        Date dayStart = new Date(seqCtx.dayStartMs);
        Date serverNow = seqCtx.serverNow;
        Date plannedArrivalAt = addSeconds(dayStart, preview.projectedArrivalTimeS);
        long serviceStartSeconds = preview.projectedArrivalTimeS;
        if (allowDepotWindowWait && node != null && node.earliestSeconds != null
                && preview.projectedArrivalTimeS < node.earliestSeconds
                && preview.status != DispatchTimeWindowDebugStatus.WINDOW_MISSED
                && preview.status != DispatchTimeWindowDebugStatus.EARLY_ARRIVAL) {
            serviceStartSeconds = node.earliestSeconds;
        }
        Date plannedServiceStartAt = addSeconds(dayStart, (int) serviceStartSeconds);
        Date plannedDepartureAt = addSeconds(dayStart, (int) preview.serviceEndSeconds);

        stop.setNxDrsPlannedArrivalAt(plannedArrivalAt);
        stop.setNxDrsPlannedServiceStartAt(plannedServiceStartAt);
        stop.setNxDrsPlannedDepartureAt(plannedDepartureAt);
        stop.setNxDrsWaitMinutes(preview.waitMinutes);
        stop.setNxDrsLateMinutes(preview.lateMinutes);
        stop.setNxDrsTimeWindowStatus(mapPreviewToPageStatus(preview));
        if (preview.warningLabel != null && !preview.warningLabel.trim().isEmpty()) {
            stop.setNxDrsTimeWindowStatusLabel(preview.warningLabel.trim());
        } else {
            stop.setNxDrsTimeWindowStatusLabel(resolvePreviewStatusLabel(preview));
        }

        if (preview.status == DispatchTimeWindowDebugStatus.WINDOW_MISSED
                || (node != null && isWindowAlreadyMissed(node, seqCtx))) {
            stop.setPlannedArrivalLabel(DisRouteSandboxScheduleLabelHelper.formatAdhocPlannedArrivalLabel(
                    plannedArrivalAt, serverNow));
        } else if (DisRouteSandboxScheduleMode.ADHOC_NOW.equals(stop.getScheduleMode())) {
            stop.setPlannedArrivalLabel(DisRouteSandboxScheduleLabelHelper.formatAdhocPlannedArrivalLabel(
                    plannedArrivalAt, serverNow));
        } else {
            stop.setPlannedArrivalLabel(
                    DisRouteTemporalHelper.formatDateTimeLabel(plannedArrivalAt, serverNow));
        }
        stop.setPlannedDepartureLabel(DisRouteTemporalHelper.formatDateTimeLabel(plannedDepartureAt, serverNow));
        syncTaskScheduleMirror(stop, plannedArrivalAt, plannedServiceStartAt, plannedDepartureAt, preview);
    }

    private static String resolvePreviewStatusLabel(PreviewFields preview) {
        if (preview == null || preview.status == null) {
            return DisRouteDispatchLabels.label(DisRouteStopTimeWindowStatus.NO_WINDOW);
        }
        if (preview.status == DispatchTimeWindowDebugStatus.EARLY_ARRIVAL) {
            if (preview.waitMinutes != null && preview.waitMinutes > 0) {
                return "早到约" + preview.waitMinutes + "分钟";
            }
            return DisRouteDispatchLabels.label(DisRouteStopTimeWindowStatus.EARLY_ARRIVAL);
        }
        return DisRouteDispatchLabels.label(mapPreviewToPageStatus(preview));
    }

    private static String mapPreviewToPageStatus(PreviewFields preview) {
        if (preview == null || preview.status == null) {
            return DisRouteStopTimeWindowStatus.NO_WINDOW;
        }
        switch (preview.status) {
            case WINDOW_MISSED:
                return DisRouteStopTimeWindowStatus.SUPPLEMENT_AFTER_WINDOW;
            case LATE:
                return DisRouteStopTimeWindowStatus.LATE;
            case EARLY_ARRIVAL:
                return DisRouteStopTimeWindowStatus.EARLY_ARRIVAL;
            case ON_TIME:
                return preview.waitMinutes != null && preview.waitMinutes > 0
                        ? DisRouteStopTimeWindowStatus.EARLY_WAIT
                        : DisRouteStopTimeWindowStatus.OK;
            case NO_WINDOW:
            default:
                return DisRouteStopTimeWindowStatus.NO_WINDOW;
        }
    }

    private static void syncTaskScheduleMirror(NxDisRouteStopEntity stop,
                                               Date plannedArrivalAt,
                                               Date plannedServiceStartAt,
                                               Date plannedDepartureAt,
                                               PreviewFields preview) {
        if (stop == null || stop.getShipmentTask() == null) {
            return;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        task.setNxDstPlannedArrivalAt(plannedArrivalAt);
        task.setNxDstPlannedServiceStartAt(plannedServiceStartAt);
        task.setNxDstPlannedDepartureAt(plannedDepartureAt);
        if (preview != null) {
            task.setNxDstWaitMinutes(preview.waitMinutes);
            task.setNxDstLateMinutes(preview.lateMinutes);
            task.setNxDstTimeWindowStatus(mapPreviewToPageStatus(preview));
        }
    }

    private static Date addSeconds(Date base, int seconds) {
        if (base == null) {
            return null;
        }
        return new Date(base.getTime() + seconds * 1000L);
    }

    private static void enrichAssignmentPreview(StopAssignment assignment,
                                                SequencingContext seqCtx,
                                                Map<Integer, NxDisShipmentTaskEntity> taskByDep,
                                                long cursorSeconds,
                                                int stopSeq) {
        if (assignment == null) {
            return;
        }
        RouteStopNode node = toNodeFromAssignment(assignment, taskByDep);
        PreviewFields preview = computePreviewFields(node, seqCtx, cursorSeconds, stopSeq == 1);
        applyPreviewToAssignment(assignment, preview, stopSeq);
    }

    private static void applyPreviewToAssignment(StopAssignment assignment,
                                                 PreviewFields preview,
                                                 int stopSeq) {
        assignment.setStopSeq(stopSeq);
        assignment.setProjectedArrivalTimeS(preview.projectedArrivalTimeS);
        assignment.setProjectedServiceEndSeconds(preview.serviceEndSeconds);
        assignment.setTimeWindowStatus(preview.status.name());
        assignment.setLateMinutes(preview.lateMinutes);
        assignment.setWaitMinutes(preview.waitMinutes);
        assignment.setWarningLabel(preview.warningLabel);
        assignment.setFeasibility(preview.feasibility);
        assignment.setStopClass(preview.stopClass);
    }

    private static PreviewFields computePreviewFields(RouteStopNode node,
                                                      SequencingContext seqCtx,
                                                      long cursorSeconds) {
        return computePreviewFields(node, seqCtx, cursorSeconds, true);
    }

    private static PreviewFields computePreviewFields(RouteStopNode node,
                                                      SequencingContext seqCtx,
                                                      long cursorSeconds,
                                                      boolean allowDepotWindowWait) {
        PreviewFields preview = new PreviewFields();
        if (node.hasNoWindow()) {
            long arrival = cursorSeconds + node.legDurationS;
            preview.projectedArrivalTimeS = (int) arrival;
            preview.serviceEndSeconds = arrival + serviceSeconds(node);
            preview.status = DispatchTimeWindowDebugStatus.NO_WINDOW;
            preview.feasibility = DispatchFeasibility.NO_WINDOW;
            preview.stopClass = node.historyAvgStopSeq != null
                    ? DispatchStopClass.UNTIMED_WITH_HISTORY : DispatchStopClass.DISTANCE_ONLY;
            applyManualLockStopClass(node, preview);
            return preview;
        }

        long arrival = cursorSeconds + node.legDurationS;
        preview.projectedArrivalTimeS = (int) arrival;

        if (isWindowAlreadyMissed(node, seqCtx)) {
            preview.status = DispatchTimeWindowDebugStatus.WINDOW_MISSED;
            preview.feasibility = DispatchFeasibility.INFEASIBLE;
            preview.stopClass = DispatchStopClass.INFEASIBLE_LATE;
            preview.lateMinutes = minutesCeil(seqCtx.serverNowSeconds - node.latestSeconds);
            preview.warningLabel = WARNING_WINDOW_MISSED;
            preview.serviceEndSeconds = arrival + serviceSeconds(node);
            applyManualLockStopClass(node, preview);
            return preview;
        }

        long serviceStart = arrival;
        if (allowDepotWindowWait && node.earliestSeconds != null && arrival < node.earliestSeconds) {
            preview.waitMinutes = minutesCeil(node.earliestSeconds - arrival);
            serviceStart = node.earliestSeconds;
        } else if (!allowDepotWindowWait && node.earliestSeconds != null && arrival < node.earliestSeconds) {
            preview.waitMinutes = minutesCeil(node.earliestSeconds - arrival);
            preview.status = DispatchTimeWindowDebugStatus.EARLY_ARRIVAL;
            preview.feasibility = DispatchFeasibility.OK;
            preview.stopClass = node.historyAvgStopSeq != null
                    ? DispatchStopClass.TIMED_WITH_HISTORY : DispatchStopClass.TIMED_NO_HISTORY;
            preview.serviceEndSeconds = arrival + serviceSeconds(node);
            applyManualLockStopClass(node, preview);
            return preview;
        }

        long serviceEnd = serviceStart + serviceSeconds(node);
        if (node.latestSeconds != null && arrival > node.latestSeconds) {
            preview.status = DispatchTimeWindowDebugStatus.LATE;
            preview.feasibility = DispatchFeasibility.LATE;
            preview.stopClass = DispatchStopClass.INFEASIBLE_LATE;
            preview.lateMinutes = minutesCeil(arrival - node.latestSeconds);
            preview.warningLabel = WARNING_PROJECTED_LATE_PREFIX + preview.lateMinutes + "分钟";
        } else {
            preview.status = DispatchTimeWindowDebugStatus.ON_TIME;
            preview.feasibility = DispatchFeasibility.OK;
            preview.stopClass = node.historyAvgStopSeq != null
                    ? DispatchStopClass.TIMED_WITH_HISTORY : DispatchStopClass.TIMED_NO_HISTORY;
        }
        applyManualLockStopClass(node, preview);
        preview.serviceEndSeconds = serviceEnd;
        return preview;
    }

    private static void applyManualLockStopClass(RouteStopNode node, PreviewFields preview) {
        if (node != null && node.isManualSeqPinned()) {
            preview.stopClass = DispatchStopClass.MANUAL_LOCKED;
        }
    }

    private static long serviceSeconds(RouteStopNode node) {
        int minutes = node.serviceMinutes != null ? node.serviceMinutes : DEFAULT_SERVICE_MINUTES;
        return minutes * 60L;
    }

    private static int minutesCeil(long deltaSeconds) {
        if (deltaSeconds <= 0) {
            return 0;
        }
        return (int) ((deltaSeconds + 59) / 60);
    }

    private static Map<Integer, NxDisShipmentTaskEntity> indexTasks(List<NxDisShipmentTaskEntity> tasks) {
        Map<Integer, NxDisShipmentTaskEntity> map = new HashMap<Integer, NxDisShipmentTaskEntity>();
        if (tasks == null) {
            return map;
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task != null && task.getNxDstDepFatherId() != null) {
                map.put(task.getNxDstDepFatherId(), task);
            }
        }
        return map;
    }

    private static Map<Integer, StopAssignment> indexPlanStops(DispatchAssignmentPlan plan) {
        Map<Integer, StopAssignment> map = new HashMap<Integer, StopAssignment>();
        if (plan == null || plan.getDriverRoutes() == null) {
            return map;
        }
        for (DriverRoutePlan route : plan.getDriverRoutes()) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            for (StopAssignment stop : route.getStops()) {
                if (stop != null && stop.getDepFatherId() != null) {
                    map.put(stop.getDepFatherId(), stop);
                }
            }
        }
        return map;
    }

    private static Map<Integer, List<NxDisRouteStopEntity>> groupSuggestedByDriver(
            List<NxDisRouteStopEntity> suggestedStops) {
        Map<Integer, List<NxDisRouteStopEntity>> byDriver = new LinkedHashMap<Integer, List<NxDisRouteStopEntity>>();
        for (NxDisRouteStopEntity stop : suggestedStops) {
            if (stop == null) {
                continue;
            }
            Integer driverId = stop.getSuggestedDriverUserId();
            if (driverId == null && stop.getShipmentTask() != null) {
                driverId = stop.getShipmentTask().getNxDstSuggestedDriverUserId();
            }
            if (driverId == null) {
                continue;
            }
            List<NxDisRouteStopEntity> list = byDriver.get(driverId);
            if (list == null) {
                list = new ArrayList<NxDisRouteStopEntity>();
                byDriver.put(driverId, list);
            }
            list.add(stop);
        }
        return byDriver;
    }

    private static List<RouteStopNode> toNodesFromAssignments(List<StopAssignment> stops,
                                                              Map<Integer, NxDisShipmentTaskEntity> taskByDep) {
        List<RouteStopNode> nodes = new ArrayList<RouteStopNode>();
        for (StopAssignment stop : stops) {
            if (stop != null) {
                nodes.add(toNodeFromAssignment(stop, taskByDep));
            }
        }
        return nodes;
    }

    private static List<RouteStopNode> toNodesFromEntities(List<NxDisRouteStopEntity> stops,
                                                           Map<Integer, StopAssignment> planStopByDep) {
        List<RouteStopNode> nodes = new ArrayList<RouteStopNode>();
        for (NxDisRouteStopEntity stop : stops) {
            if (stop != null) {
                nodes.add(toNodeFromEntity(stop, planStopByDep));
            }
        }
        return nodes;
    }

    private static RouteStopNode toNodeFromAssignment(StopAssignment stop,
                                                      Map<Integer, NxDisShipmentTaskEntity> taskByDep) {
        RouteStopNode node = new RouteStopNode();
        node.depFatherId = stop.getDepFatherId();
        node.earliestSeconds = stop.getEarliestDeliveryTimeS();
        node.latestSeconds = stop.getLatestDeliveryTimeS();
        node.serviceMinutes = stop.getServiceMinutes();
        node.historyAvgStopSeq = stop.getHistoryAvgStopSeq();
        NxDisShipmentTaskEntity task = taskByDep != null ? taskByDep.get(stop.getDepFatherId()) : null;
        node.legDurationS = resolveLegDurationS(task, null);
        applyManualLockFields(node, task);
        return node;
    }

    private static RouteStopNode toNodeFromEntity(NxDisRouteStopEntity stop,
                                                  Map<Integer, StopAssignment> planStopByDep) {
        RouteStopNode node = new RouteStopNode();
        node.depFatherId = resolveDepFatherId(stop);
        node.earliestSeconds = stop.getResolvedEarliestDeliveryTimeS();
        node.latestSeconds = stop.getResolvedLatestDeliveryTimeS();
        node.serviceMinutes = stop.getNxDrsServiceMinutes();
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (node.earliestSeconds == null) {
            node.earliestSeconds = stop.getNxDrsEarliestDeliveryTimeS();
        }
        if (node.latestSeconds == null) {
            node.latestSeconds = stop.getNxDrsLatestDeliveryTimeS();
        }
        if (node.earliestSeconds == null && task != null) {
            node.earliestSeconds = task.getNxDstEarliestDeliveryTimeS();
        }
        if (node.latestSeconds == null && task != null) {
            node.latestSeconds = task.getNxDstLatestDeliveryTimeS();
        }
        if (node.serviceMinutes == null && task != null) {
            node.serviceMinutes = task.getNxDstServiceMinutes();
        }
        StopAssignment planStop = planStopByDep != null && node.depFatherId != null
                ? planStopByDep.get(node.depFatherId) : null;
        if (planStop != null) {
            if (node.earliestSeconds == null) {
                node.earliestSeconds = planStop.getEarliestDeliveryTimeS();
            }
            if (node.latestSeconds == null) {
                node.latestSeconds = planStop.getLatestDeliveryTimeS();
            }
            if (node.serviceMinutes == null) {
                node.serviceMinutes = planStop.getServiceMinutes();
            }
            if (planStop.getHistoryAvgStopSeq() != null) {
                node.historyAvgStopSeq = planStop.getHistoryAvgStopSeq();
            }
        }
        node.legDurationS = resolveLegDurationS(task, stop);
        applyManualLockFields(node, task);
        return node;
    }

    private static void applyManualLockFields(RouteStopNode node, NxDisShipmentTaskEntity task) {
        if (task == null || node == null) {
            return;
        }
        node.manualLocked = task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1;
        node.manualStopSeq = task.getNxDstManualStopSeq();
    }

    private static long resolveLegDurationS(NxDisShipmentTaskEntity task, NxDisRouteStopEntity stop) {
        if (stop != null && stop.getNxDrsLegDurationS() != null) {
            return stop.getNxDrsLegDurationS();
        }
        if (task != null && task.getNxDstLegDurationS() != null) {
            return task.getNxDstLegDurationS();
        }
        return 0L;
    }

    private static Integer resolveDepFatherId(NxDisRouteStopEntity stop) {
        if (stop.getNxDrsDepartmentId() != null) {
            return stop.getNxDrsDepartmentId();
        }
        if (stop.getShipmentTask() != null) {
            return stop.getShipmentTask().getNxDstDepFatherId();
        }
        return null;
    }

    private static int compareNullableInt(Integer a, Integer b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        return Integer.compare(a, b);
    }

    private static final class RouteStopNode {
        private Integer depFatherId;
        private Integer earliestSeconds;
        private Integer latestSeconds;
        private Integer serviceMinutes;
        private Integer historyAvgStopSeq;
        private long legDurationS;
        private boolean manualLocked;
        private Integer manualStopSeq;

        private boolean hasNoWindow() {
            return earliestSeconds == null && latestSeconds == null;
        }

        private boolean isManualSeqPinned() {
            return manualLocked && manualStopSeq != null && manualStopSeq > 0;
        }
    }

    private static final class PreviewFields {
        private Integer projectedArrivalTimeS;
        private long serviceEndSeconds;
        private DispatchTimeWindowDebugStatus status;
        private DispatchFeasibility feasibility;
        private DispatchStopClass stopClass;
        private Integer lateMinutes;
        private Integer waitMinutes;
        private String warningLabel;
    }

    private static final class RouteDepartPlan {
        private final long departSeconds;
        private final String reasonCode;
        private final String reasonLabel;

        private RouteDepartPlan(long departSeconds, String reasonCode, String reasonLabel) {
            this.departSeconds = departSeconds;
            this.reasonCode = reasonCode;
            this.reasonLabel = reasonLabel;
        }

        static RouteDepartPlan now(SequencingContext seqCtx) {
            long now = seqCtx != null ? seqCtx.serverNowSeconds : 0L;
            return new RouteDepartPlan(now, DEPART_REASON_NOW, DEPART_LABEL_NOW);
        }

        static RouteDepartPlan at(long departSeconds, String reasonCode, String reasonLabel) {
            return new RouteDepartPlan(Math.max(0L, departSeconds), reasonCode, reasonLabel);
        }
    }

    private static final class SequencingContext {
        private final String routeDate;
        private final Date serverNow;
        private final long serverNowSeconds;
        private final long initialDepartSeconds;
        private final long dayStartMs;
        private final String suggestedDepartReason;
        private final String suggestedDepartReasonLabel;

        private SequencingContext(String routeDate,
                                  Date serverNow,
                                  long serverNowSeconds,
                                  long initialDepartSeconds,
                                  String suggestedDepartReason,
                                  String suggestedDepartReasonLabel,
                                  long dayStartMs) {
            this.routeDate = routeDate;
            this.serverNow = serverNow;
            this.serverNowSeconds = serverNowSeconds;
            this.initialDepartSeconds = initialDepartSeconds;
            this.suggestedDepartReason = suggestedDepartReason;
            this.suggestedDepartReasonLabel = suggestedDepartReasonLabel;
            this.dayStartMs = dayStartMs;
        }

        SequencingContext withDepart(RouteDepartPlan departPlan) {
            if (departPlan == null) {
                return this;
            }
            return new SequencingContext(routeDate, serverNow, serverNowSeconds,
                    departPlan.departSeconds, departPlan.reasonCode, departPlan.reasonLabel, dayStartMs);
        }

        static SequencingContext from(DispatchStrategyContext context) {
            String routeDate = context != null ? context.getRouteDate() : null;
            Date serverNow = context != null && context.getServerNow() != null
                    ? context.getServerNow() : new Date();
            if (routeDate == null || routeDate.trim().isEmpty()) {
                routeDate = DisRouteTemporalHelper.formatTodayYyyyMmDd(serverNow);
            }
            Date dayStart = parseRouteDateStart(routeDate.trim());
            long dayStartMs = dayStart.getTime();
            long serverNowSeconds = Math.max(0L, (serverNow.getTime() - dayStartMs) / 1000L);
            return new SequencingContext(routeDate.trim(), serverNow, serverNowSeconds,
                    serverNowSeconds, DEPART_REASON_NOW, DEPART_LABEL_NOW, dayStartMs);
        }

        private static Date parseRouteDateStart(String routeDate) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(routeDate);
            } catch (ParseException ex) {
                return new Date();
            }
        }
    }
}
