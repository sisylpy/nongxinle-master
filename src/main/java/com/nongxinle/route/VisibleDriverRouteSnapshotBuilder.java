package com.nongxinle.route;

import com.nongxinle.dto.route.DriverDispatchCandidateDto;
import com.nongxinle.dto.route.DriverDispatchListResponse;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.dispatch.strategy.DispatchAssignmentPlan;
import com.nongxinle.route.dispatch.strategy.DispatchSequenceBucket;
import com.nongxinle.route.dispatch.strategy.DispatchStrategyContext;
import com.nongxinle.route.dispatch.strategy.DispatchStrategyMode;
import com.nongxinle.route.dispatch.strategy.DriverRoutePlan;
import com.nongxinle.route.dispatch.strategy.OwnerFixedRouteTimeWindowRouteSequencer;
import com.nongxinle.route.dispatch.strategy.StopAssignment;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.route.proposal.ProposalDriverRoute;
import com.nongxinle.route.proposal.ProposalStop;
import com.nongxinle.route.proposal.SandboxProposalPlan;
import com.nongxinle.route.proposal.SandboxProposalPlanBuilder;
import com.nongxinle.service.impl.DisRouteSandboxLegMetricsHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PR-2c：partition 之后、pageViewModel 之前，生成 sections / map / debug 唯一主权源。
 * execution / frozen 站不参与 visible 序列与 ETA cursor。
 */
public final class VisibleDriverRouteSnapshotBuilder {

    public static final String SECTION_SUGGESTED = "SUGGESTED_DRIVER_ROUTES";
    public static final String SECTION_CONFIRMED = "CONFIRMED_DRIVER_ROUTES";

    private VisibleDriverRouteSnapshotBuilder() {
    }

    public static List<VisibleDriverRouteSnapshot> build(SandboxTodayPageBuildContext ctx,
                                                         DispatchAssignmentPlan assignmentPlan,
                                                         GeoPoint depot,
                                                         DisRouteSandboxLegMetricsHelper legMetricsHelper) {
        if (ctx == null) {
            return Collections.emptyList();
        }
        List<VisibleDriverRouteSnapshot> routes = new ArrayList<VisibleDriverRouteSnapshot>();
        Set<Integer> driversWithConfirmed = indexDriverIds(ctx.getConfirmedStops());
        Set<Integer> confirmedDepIds = indexDepartmentIds(ctx.getConfirmedStops());

        routes.addAll(buildSuggestedSnapshots(ctx, assignmentPlan, depot, legMetricsHelper,
                driversWithConfirmed, confirmedDepIds));
        routes.addAll(buildConfirmedSnapshots(ctx, assignmentPlan, depot, legMetricsHelper));
        return routes;
    }

    /**
     * 分派中专用：只读 {@link SandboxProposalPlan}，不读 mergedPlan.driverRoutes / confirmed / execution。
     */
    public static List<VisibleDriverRouteSnapshot> buildFromProposalPlan(
            SandboxTodayPageBuildContext ctx,
            SandboxProposalPlan proposalPlan,
            DispatchAssignmentPlan assignmentPlan,
            GeoPoint depot,
            DisRouteSandboxLegMetricsHelper legMetricsHelper) {
        if (ctx == null || proposalPlan == null || proposalPlan.getProposalRoutes() == null) {
            return Collections.emptyList();
        }
        syncProposalUnassignedToContext(ctx, proposalPlan);

        List<VisibleDriverRouteSnapshot> routes = new ArrayList<VisibleDriverRouteSnapshot>();
        DispatchStrategyContext strategyContext = buildStrategyContext(ctx);
        boolean ownerFixed = assignmentPlan != null
                && assignmentPlan.getStrategyMode() == DispatchStrategyMode.OWNER_FIXED_ROUTE;

        for (ProposalDriverRoute proposalRoute : proposalPlan.getProposalRoutes()) {
            if (proposalRoute == null || proposalRoute.getDriverUserId() == null) {
                continue;
            }
            Integer driverUserId = proposalRoute.getDriverUserId();
            List<NxDisRouteStopEntity> visible = SandboxProposalPlanBuilder.toStopEntities(proposalRoute.getStops());
            if (visible.isEmpty()) {
                continue;
            }
            if (!DisRouteSandboxDriverDispatchStateHelper.shouldRenderSuggestedDriverRouteCard(
                    driverUserId, DisRouteSandboxTodayRouteKind.SUGGESTED, ctx)) {
                exposeAsUnassigned(ctx, visible);
                traceSnapshotSkip(ctx, driverUserId, null,
                        visible.size(), 0,
                        "DRIVER_IN_LOADING_OR_EXECUTION_ON_PAGE -> exposeAsUnassigned");
                continue;
            }

            NxDisDriverRouteEntity pseudoRoute = toProposalPseudoRoute(proposalRoute);
            VisibleScheduleResult scheduled = prepareVisibleStopsForSnapshot(
                    pseudoRoute, visible, assignmentPlan, strategyContext, depot, legMetricsHelper,
                    ownerFixed && assignmentPlan != null);
            VisibleDriverRouteSnapshot snapshot = assembleRouteSnapshot(
                    driverUserId, pseudoRoute, scheduled.orderedStops, DisRouteSandboxTodayRouteKind.SUGGESTED,
                    SECTION_SUGGESTED, ctx, assignmentPlan, strategyContext, ownerFixed,
                    scheduled.legMetricsFallbackUsed, scheduled.scheduleWarning);
            if (snapshot != null && !snapshot.getStops().isEmpty()) {
                routes.add(snapshot);
            } else {
                traceSnapshotSkip(ctx, driverUserId, pseudoRoute,
                        visible.size(), 0, "SNAPSHOT_ASSEMBLY_EMPTY");
            }
        }

        sortRouteSnapshots(routes);
        return routes;
    }

    private static void syncProposalUnassignedToContext(SandboxTodayPageBuildContext ctx,
                                                      SandboxProposalPlan proposalPlan) {
        if (ctx == null || proposalPlan == null) {
            return;
        }
        List<NxDisRouteStopEntity> unassigned = SandboxProposalPlanBuilder.toStopEntities(
                proposalPlan.getUnassignedStops());
        ctx.setUnassignedStops(unassigned);
    }

    private static NxDisDriverRouteEntity toProposalPseudoRoute(ProposalDriverRoute proposalRoute) {
        NxDisDriverRouteEntity route = new NxDisDriverRouteEntity();
        if (proposalRoute == null) {
            return route;
        }
        route.setNxDdrDriverUserId(proposalRoute.getDriverUserId());
        route.setDriverName(proposalRoute.getDriverName());
        route.setStops(SandboxProposalPlanBuilder.toStopEntities(proposalRoute.getStops()));
        route.setNxDdrStopCount(route.getStops() != null ? route.getStops().size() : 0);
        route.setNxDdrTotalDistanceM(proposalRoute.getTotalDistanceM());
        route.setNxDdrTotalDurationS(proposalRoute.getTotalDurationS());
        route.setRouteScheduleSummaryLabel(proposalRoute.getScheduleHeadline());
        route.setPlannedDepartLabel(proposalRoute.getPlannedDepartLabel());
        route.setPlannedReturnLabel(proposalRoute.getPlannedReturnLabel());
        return route;
    }

    private static List<VisibleDriverRouteSnapshot> buildSuggestedSnapshots(
            SandboxTodayPageBuildContext ctx,
            DispatchAssignmentPlan assignmentPlan,
            GeoPoint depot,
            DisRouteSandboxLegMetricsHelper legMetricsHelper,
            Set<Integer> driversWithConfirmed,
            Set<Integer> confirmedDepIds) {
        List<VisibleDriverRouteSnapshot> routes = new ArrayList<VisibleDriverRouteSnapshot>();
        NxDisRoutePlanEntity plan = ctx.getMergedPlan();
        if (plan == null || plan.getDriverRoutes() == null) {
            return routes;
        }

        DispatchStrategyContext strategyContext = buildStrategyContext(ctx);
        boolean ownerFixed = assignmentPlan != null
                && assignmentPlan.getStrategyMode() == DispatchStrategyMode.OWNER_FIXED_ROUTE;

        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route == null || route.getNxDdrDriverUserId() == null) {
                continue;
            }
            Integer driverUserId = route.getNxDdrDriverUserId();
            if (driversWithConfirmed.contains(driverUserId)) {
                traceSnapshotSkip(ctx, driverUserId, route, route.getStops() != null ? route.getStops().size() : 0,
                        0, "DRIVER_HAS_CONFIRMED_ROUTE");
                continue;
            }
            List<NxDisRouteStopEntity> visible = collectVisibleSuggestedStops(route);
            visible = excludeConfirmedDepartments(visible, confirmedDepIds);
            if (visible.isEmpty()) {
                traceSnapshotSkip(ctx, driverUserId, route,
                        route.getStops() != null ? route.getStops().size() : 0, 0,
                        "NO_VISIBLE_SUGGESTED_STOPS");
                continue;
            }
            if (!DisRouteSandboxDriverDispatchStateHelper.shouldRenderSuggestedDriverRouteCard(
                    driverUserId, DisRouteSandboxTodayRouteKind.SUGGESTED, ctx)) {
                exposeAsUnassigned(ctx, visible);
                traceSnapshotSkip(ctx, driverUserId, route,
                        route.getStops() != null ? route.getStops().size() : 0, visible.size(),
                        "DRIVER_IN_LOADING_OR_EXECUTION_ON_PAGE -> exposeAsUnassigned");
                continue;
            }

            VisibleScheduleResult scheduled = prepareVisibleStopsForSnapshot(
                    route, visible, assignmentPlan, strategyContext, depot, legMetricsHelper,
                    ownerFixed && assignmentPlan != null);
            VisibleDriverRouteSnapshot snapshot = assembleRouteSnapshot(
                    driverUserId, route, scheduled.orderedStops, DisRouteSandboxTodayRouteKind.SUGGESTED,
                    SECTION_SUGGESTED, ctx, assignmentPlan, strategyContext, ownerFixed,
                    scheduled.legMetricsFallbackUsed, scheduled.scheduleWarning);
            if (snapshot != null && !snapshot.getStops().isEmpty()) {
                routes.add(snapshot);
            } else if (snapshot == null || snapshot.getStops().isEmpty()) {
                traceSnapshotSkip(ctx, driverUserId, route,
                        route.getStops() != null ? route.getStops().size() : 0,
                        visible.size(), "SNAPSHOT_ASSEMBLY_EMPTY");
            }
        }

        sortRouteSnapshots(routes);
        return routes;
    }

    private static void traceSnapshotSkip(SandboxTodayPageBuildContext ctx,
                                          Integer driverUserId,
                                          NxDisDriverRouteEntity route,
                                          int rawStopCount,
                                          int visibleStopCount,
                                          String skipReason) {
        SandboxTodayPipelineTrace trace = ctx != null ? ctx.getPipelineTrace() : null;
        if (trace == null) {
            return;
        }
        trace.addSnapshotSkip(driverUserId,
                route != null ? route.getDriverName() : null,
                rawStopCount, visibleStopCount, skipReason);
    }

    /**
     * 司机处于装车/配送阶段时不展示其 suggested route，但 sandbox 客户仍必须进入待重新分派区。
     */
    private static void exposeAsUnassigned(SandboxTodayPageBuildContext ctx,
                                           List<NxDisRouteStopEntity> stops) {
        if (ctx == null || stops == null || stops.isEmpty()) {
            return;
        }
        List<NxDisRouteStopEntity> unassigned = ctx.getUnassignedStops();
        if (unassigned == null) {
            unassigned = new ArrayList<NxDisRouteStopEntity>();
            ctx.setUnassignedStops(unassigned);
        }
        Set<Integer> existingDepIds = indexDepartmentIds(unassigned);
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            if (depId == null || existingDepIds.add(depId)) {
                unassigned.add(stop);
            }
        }
    }

    private static List<VisibleDriverRouteSnapshot> buildConfirmedSnapshots(
            SandboxTodayPageBuildContext ctx,
            DispatchAssignmentPlan assignmentPlan,
            GeoPoint depot,
            DisRouteSandboxLegMetricsHelper legMetricsHelper) {
        List<VisibleDriverRouteSnapshot> routes = new ArrayList<VisibleDriverRouteSnapshot>();
        if (ctx.getConfirmedStops() == null || ctx.getConfirmedStops().isEmpty()) {
            return routes;
        }
        DispatchStrategyContext strategyContext = buildStrategyContext(ctx);
        List<NxDisRouteStopEntity> merged = mergePendingSuggestedOntoConfirmed(
                ctx.getConfirmedStops(), ctx.getSuggestedStops());
        Map<Integer, List<NxDisRouteStopEntity>> byDriver = groupStopsByDriver(merged);
        for (Map.Entry<Integer, List<NxDisRouteStopEntity>> entry : byDriver.entrySet()) {
            Integer driverUserId = entry.getKey();
            if (driverUserId == null) {
                continue;
            }
            if (!DisRouteSandboxDriverDispatchStateHelper.shouldRenderSuggestedDriverRouteCard(
                    driverUserId, DisRouteSandboxTodayRouteKind.CONFIRMED, ctx)) {
                continue;
            }
            List<NxDisRouteStopEntity> ordered = sortConfirmedStops(entry.getValue());
            NxDisDriverRouteEntity route = findPlanRoute(ctx.getMergedPlan(), driverUserId);
            VisibleScheduleResult scheduled = prepareVisibleStopsForSnapshot(
                    route, ordered, assignmentPlan, strategyContext, depot, legMetricsHelper, false);
            VisibleDriverRouteSnapshot snapshot = assembleRouteSnapshot(
                    driverUserId, route, scheduled.orderedStops, DisRouteSandboxTodayRouteKind.CONFIRMED,
                    SECTION_CONFIRMED, ctx, assignmentPlan, strategyContext, false,
                    scheduled.legMetricsFallbackUsed, scheduled.scheduleWarning);
            if (snapshot != null && !snapshot.getStops().isEmpty()) {
                routes.add(snapshot);
            }
        }
        sortRouteSnapshots(routes);
        return routes;
    }

    private static VisibleScheduleResult prepareVisibleStopsForSnapshot(
            NxDisDriverRouteEntity route,
            List<NxDisRouteStopEntity> visible,
            DispatchAssignmentPlan assignmentPlan,
            DispatchStrategyContext strategyContext,
            GeoPoint depot,
            DisRouteSandboxLegMetricsHelper legMetricsHelper,
            boolean resequenceOwnerFixed) {
        List<NxDisRouteStopEntity> working = new ArrayList<NxDisRouteStopEntity>(visible);
        if (resequenceOwnerFixed && assignmentPlan != null) {
            OwnerFixedRouteTimeWindowRouteSequencer.resequenceSuggestedStops(
                    working, strategyContext, assignmentPlan);
        } else if (!resequenceOwnerFixed) {
            working = fallbackSortVisible(working);
        }
        OwnerFixedRouteTimeWindowRouteSequencer.invalidateLegMetrics(working);

        boolean legFallback = false;
        if (depot != null) {
            if (legMetricsHelper != null) {
                try {
                    NxDisDriverRouteEntity pseudoRoute = new NxDisDriverRouteEntity();
                    pseudoRoute.setNxDdrDriverUserId(route != null ? route.getNxDdrDriverUserId() : null);
                    pseudoRoute.setStops(new ArrayList<NxDisRouteStopEntity>(working));
                    legMetricsHelper.applyToDriverRoute(depot, pseudoRoute);
                } catch (IOException ex) {
                    applyStraightLineLegFallback(depot, working);
                    legFallback = true;
                }
            } else {
                applyStraightLineLegFallback(depot, working);
                legFallback = true;
            }
            if (ensureLegMetricsPresent(depot, working)) {
                legFallback = true;
            }
        } else {
            applyDefaultLegFallback(working);
            legFallback = true;
        }

        NxDisDriverRouteEntity scheduleRoute = route != null ? route : new NxDisDriverRouteEntity();
        if (scheduleRoute.getNxDdrDriverUserId() == null && !working.isEmpty()) {
            scheduleRoute.setNxDdrDriverUserId(
                    DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(working.get(0)));
        }
        OwnerFixedRouteTimeWindowRouteSequencer.recalculateVisibleScheduleForDriverRoute(
                scheduleRoute, working, assignmentPlan, strategyContext);

        VisibleScheduleResult result = new VisibleScheduleResult();
        result.orderedStops = working;
        result.legMetricsFallbackUsed = legFallback;
        result.scheduleWarning = legFallback
                ? "路线 leg 使用直线估算，ETA 已按最终站序重算"
                : null;
        return result;
    }

    private static List<NxDisRouteStopEntity> collectVisibleSuggestedStops(NxDisDriverRouteEntity route) {
        if (route == null || route.getStops() == null) {
            return Collections.emptyList();
        }
        List<NxDisRouteStopEntity> visible = new ArrayList<NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop != null && DisRouteSandboxTodayDriverRouteCardBuilder.isSuggestedDispatchStop(stop, route)) {
                visible.add(stop);
            }
        }
        return visible;
    }

    private static final long FALLBACK_SPEED_M_PER_S = 8L;
    private static final long DEFAULT_FALLBACK_LEG_DURATION_S = 300L;
    private static final long DEFAULT_FALLBACK_LEG_DISTANCE_M = 2400L;
    private static final double EARTH_RADIUS_M = 6371000.0;

    private static void applyStraightLineLegFallback(GeoPoint depot, List<NxDisRouteStopEntity> orderedStops) {
        if (orderedStops == null || orderedStops.isEmpty()) {
            return;
        }
        GeoPoint cursor = depot;
        for (NxDisRouteStopEntity stop : orderedStops) {
            if (stop == null) {
                continue;
            }
            GeoPoint stopPoint = resolveStopGeoPoint(stop);
            long distanceM = DEFAULT_FALLBACK_LEG_DISTANCE_M;
            if (cursor != null && stopPoint != null) {
                distanceM = Math.max(1L, straightLineMeters(cursor, stopPoint));
            }
            long durationS = Math.max(1L, distanceM / FALLBACK_SPEED_M_PER_S);
            writeLegToStop(stop, distanceM, durationS);
            if (stopPoint != null) {
                cursor = stopPoint;
            }
        }
    }

    /** @return true if any stop still required default leg fill */
    private static boolean ensureLegMetricsPresent(GeoPoint depot, List<NxDisRouteStopEntity> orderedStops) {
        if (orderedStops == null || orderedStops.isEmpty()) {
            return false;
        }
        boolean filled = false;
        GeoPoint cursor = depot;
        for (NxDisRouteStopEntity stop : orderedStops) {
            if (stop == null) {
                continue;
            }
            Long durationS = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
            if (durationS == null || durationS <= 0L) {
                GeoPoint stopPoint = resolveStopGeoPoint(stop);
                long distanceM = DEFAULT_FALLBACK_LEG_DISTANCE_M;
                if (cursor != null && stopPoint != null) {
                    distanceM = Math.max(1L, straightLineMeters(cursor, stopPoint));
                }
                writeLegToStop(stop, distanceM, Math.max(1L, distanceM / FALLBACK_SPEED_M_PER_S));
                filled = true;
            }
            GeoPoint stopPoint = resolveStopGeoPoint(stop);
            if (stopPoint != null) {
                cursor = stopPoint;
            }
        }
        return filled;
    }

    private static void applyDefaultLegFallback(List<NxDisRouteStopEntity> orderedStops) {
        if (orderedStops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : orderedStops) {
            if (stop != null) {
                writeLegToStop(stop, DEFAULT_FALLBACK_LEG_DISTANCE_M, DEFAULT_FALLBACK_LEG_DURATION_S);
            }
        }
    }

    private static void writeLegToStop(NxDisRouteStopEntity stop, long distanceM, long durationS) {
        stop.setNxDrsLegDistanceM(distanceM);
        stop.setNxDrsLegDurationS(durationS);
        stop.setLegDistanceType(com.nongxinle.route.DisRouteDistanceTypes.ESTIMATED_STRAIGHT_DISTANCE);
        stop.setLegDistanceProvider("STRAIGHT_FALLBACK");
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null) {
            task.setNxDstLegDistanceM(distanceM);
            task.setNxDstLegDurationS(durationS);
            task.setLegDistanceType(com.nongxinle.route.DisRouteDistanceTypes.ESTIMATED_STRAIGHT_DISTANCE);
            task.setLegDistanceProvider("STRAIGHT_FALLBACK");
        }
    }

    private static GeoPoint resolveStopGeoPoint(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate(stop.getNxDrsLat(), stop.getNxDrsLng())) {
            return com.nongxinle.route.RouteCoordinateUtils.toPoint(stop.getNxDrsLat(), stop.getNxDrsLng());
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate(
                task.getNxDstLat(), task.getNxDstLng())) {
            return com.nongxinle.route.RouteCoordinateUtils.toPoint(task.getNxDstLat(), task.getNxDstLng());
        }
        return null;
    }

    private static final class VisibleScheduleResult {
        private List<NxDisRouteStopEntity> orderedStops;
        private boolean legMetricsFallbackUsed;
        private String scheduleWarning;
    }

    private static VisibleDriverRouteSnapshot assembleRouteSnapshot(
            Integer driverUserId,
            NxDisDriverRouteEntity route,
            List<NxDisRouteStopEntity> orderedStops,
            DisRouteSandboxTodayRouteKind routeKind,
            String sectionKey,
            SandboxTodayPageBuildContext ctx,
            DispatchAssignmentPlan assignmentPlan,
            DispatchStrategyContext strategyContext,
            boolean includeSequenceDebug,
            boolean legMetricsFallbackUsed,
            String scheduleWarning) {
        if (driverUserId == null || orderedStops == null || orderedStops.isEmpty()) {
            return null;
        }
        Date serverNow = ctx.getServerNow();
        Map<Integer, DriverDispatchCandidateDto> driverIndex = indexDrivers(ctx.getDrivers());
        DriverRoutePlan planRoute = resolvePlanRoute(assignmentPlan, driverUserId);

        long totalDistanceM = 0L;
        long totalDurationS = 0L;
        List<VisibleDriverRouteStopSnapshot> stopSnapshots = new ArrayList<VisibleDriverRouteStopSnapshot>();

        OwnerFixedRouteTimeWindowRouteSequencer.SequencingSnapshot seqCtx = includeSequenceDebug
                ? OwnerFixedRouteTimeWindowRouteSequencer.buildSequencingSnapshot(strategyContext)
                : null;
        Map<Integer, StopAssignment> planStopByDep = includeSequenceDebug
                ? indexPlanStops(assignmentPlan) : Collections.<Integer, StopAssignment>emptyMap();
        List<OwnerFixedRouteTimeWindowRouteSequencer.RouteStopSnapshot> orderedNodes = includeSequenceDebug
                ? OwnerFixedRouteTimeWindowRouteSequencer.toStopSnapshots(orderedStops, planStopByDep)
                : Collections.emptyList();
        OwnerFixedRouteTimeWindowRouteSequencer.RouteDepartSnapshot depart = includeSequenceDebug
                ? OwnerFixedRouteTimeWindowRouteSequencer.resolveSuggestedDepartSnapshot(orderedNodes, seqCtx)
                : null;
        long cursor = depart != null ? depart.departSeconds : 0L;

        int visibleSeq = 0;
        for (int i = 0; i < orderedStops.size(); i++) {
            NxDisRouteStopEntity stop = orderedStops.get(i);
            if (stop == null) {
                continue;
            }
            visibleSeq++;
            Long legDistanceM = DisRouteSandboxDisplayFormatHelper.resolveLegDistanceM(stop);
            Long legDurationS = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
            if (legDistanceM != null) {
                totalDistanceM += legDistanceM;
            }
            if (legDurationS != null) {
                totalDurationS += legDurationS;
            }

            OwnerFixedRouteTimeWindowRouteSequencer.RouteStopSnapshot node =
                    includeSequenceDebug && i < orderedNodes.size() ? orderedNodes.get(i) : null;
            OwnerFixedRouteTimeWindowRouteSequencer.StopPreviewSnapshot preview = null;
            DispatchSequenceBucket bucket = null;
            String sequenceReason = null;
            String sequenceSortKey = null;
            Integer projectedArrivalTimeS = null;
            if (includeSequenceDebug && node != null && seqCtx != null) {
                preview = OwnerFixedRouteTimeWindowRouteSequencer.computeStopPreviewSnapshot(
                        node, seqCtx, cursor);
                bucket = OwnerFixedRouteTimeWindowRouteSequencer.classifySequenceBucket(
                        node, seqCtx, cursor);
                sequenceReason = OwnerFixedRouteTimeWindowRouteSequencer.resolveSequenceReason(
                        bucket, node, preview, seqCtx);
                sequenceSortKey = OwnerFixedRouteTimeWindowRouteSequencer.buildSequenceSortKey(
                        node, seqCtx, bucket);
                projectedArrivalTimeS = resolveProjectedArrivalTimeS(stop, preview, seqCtx);
                cursor = preview.serviceEndSeconds;
            } else {
                projectedArrivalTimeS = resolveProjectedArrivalTimeSFromEntity(stop, strategyContext);
            }

            DisRouteSandboxTodayRouteKind stopKind = resolveStopKind(stop, route, routeKind);
            stopSnapshots.add(buildStopSnapshot(
                    stop, visibleSeq, stopKind, serverNow, ctx, driverUserId,
                    legDistanceM, legDurationS, bucket, sequenceReason, sequenceSortKey,
                    projectedArrivalTimeS, preview));
        }
        VisibleDriverRouteSnapshot snapshot = new VisibleDriverRouteSnapshot();
        snapshot.setDriverUserId(driverUserId);
        snapshot.setDriverName(resolveDriverName(driverUserId, orderedStops, route, driverIndex));
        snapshot.setRouteKind(routeKind);
        snapshot.setSectionKey(sectionKey);
        snapshot.setSourceRoute(route);
        snapshot.setStops(stopSnapshots);
        snapshot.setTotalDistanceM(totalDistanceM);
        snapshot.setTotalDurationS(totalDurationS);
        snapshot.setTotalDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(totalDistanceM));
        snapshot.setTotalDurationText(DisRouteSandboxDisplayFormatHelper.formatDurationText(totalDurationS));
        if (route != null) {
            snapshot.setPlannedDepartLabel(route.getPlannedDepartLabel());
            snapshot.setPlannedReturnLabel(firstNonBlank(route.getPlannedReturnLabel(), route.getPlannedFinishLabel()));
            snapshot.setScheduleHeadline(route.getRouteScheduleSummaryLabel());
        }
        if (planRoute != null) {
            snapshot.setSuggestedDepartTimeS(planRoute.getSuggestedDepartTimeS());
            snapshot.setPlannedDepartTimeS(planRoute.getPlannedDepartTimeS());
            snapshot.setSuggestedDepartReason(planRoute.getSuggestedDepartReason());
            snapshot.setSuggestedDepartReasonLabel(planRoute.getSuggestedDepartReasonLabel());
        } else if (depart != null) {
            snapshot.setSuggestedDepartTimeS((int) depart.departSeconds);
            snapshot.setPlannedDepartTimeS((int) depart.departSeconds);
            snapshot.setSuggestedDepartReason(depart.reasonCode);
            snapshot.setSuggestedDepartReasonLabel(depart.reasonLabel);
        }
        snapshot.setLegMetricsFallbackUsed(legMetricsFallbackUsed);
        snapshot.setScheduleWarning(scheduleWarning);
        return snapshot;
    }

    private static VisibleDriverRouteStopSnapshot buildStopSnapshot(
            NxDisRouteStopEntity stop,
            int visibleSeq,
            DisRouteSandboxTodayRouteKind stopKind,
            Date serverNow,
            SandboxTodayPageBuildContext ctx,
            Integer driverUserId,
            Long legDistanceM,
            Long legDurationS,
            DispatchSequenceBucket sequenceBucket,
            String sequenceReason,
            String sequenceSortKey,
            Integer projectedArrivalTimeS,
            OwnerFixedRouteTimeWindowRouteSequencer.StopPreviewSnapshot preview) {
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        VisibleDriverRouteStopSnapshot snap = new VisibleDriverRouteStopSnapshot();
        snap.setVisibleSeq(visibleSeq);
        snap.setDepFatherId(resolveDepFatherId(stop));
        snap.setDepartmentId(stop.getNxDrsDepartmentId());
        snap.setCustomerName(resolveCustomerName(stop, task));
        snap.setCardKey(resolveCardKey(stop));
        snap.setSandboxStopKey(stop.getSandboxStopKey());
        snap.setPlannedArrivalAt(stop.getNxDrsPlannedArrivalAt());
        snap.setPlannedDepartureAt(stop.getNxDrsPlannedDepartureAt());
        snap.setArrivalLabel(DisRouteSandboxTodayStopScheduleHelper.resolvePlannedArrivalLabel(stop, serverNow));
        snap.setDepartureLabel(DisRouteSandboxTodayStopScheduleHelper.resolvePlannedDepartureLabel(stop, serverNow));
        snap.setCustomerWindowLabel(DisRouteSandboxTodayStopScheduleHelper.resolveCustomerWindowLabel(stop, serverNow));
        snap.setServiceDurationLabel(DisRouteSandboxTodayStopScheduleHelper.resolveServiceDurationLabel(stop));
        snap.setTimeLabel(resolveTimeLabel(stop, serverNow));
        snap.setServiceMinutes(stop.getNxDrsServiceMinutes());
        snap.setLegDistanceM(legDistanceM);
        snap.setLegDurationS(legDurationS);
        snap.setDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(legDistanceM));
        snap.setDurationText(DisRouteSandboxDisplayFormatHelper.formatDurationText(legDurationS));
        snap.setLegText(DisRouteSandboxTodayTimelineBuilder.joinLegText(
                snap.getDistanceText(), snap.getDurationText()));
        snap.setEarliestDeliveryTimeS(firstNonNull(
                stop.getResolvedEarliestDeliveryTimeS(), stop.getNxDrsEarliestDeliveryTimeS()));
        snap.setLatestDeliveryTimeS(firstNonNull(
                stop.getResolvedLatestDeliveryTimeS(), stop.getNxDrsLatestDeliveryTimeS()));
        snap.setWindowSource(stop.getResolvedWindowSource());
        snap.setTimeWindowStatus(stop.getNxDrsTimeWindowStatus());
        snap.setTimeWindowStatusLabel(stop.getNxDrsTimeWindowStatusLabel());
        snap.setLateMinutes(firstNonNull(stop.getNxDrsLateMinutes(),
                preview != null ? preview.lateMinutes : null));
        snap.setWaitMinutes(firstNonNull(stop.getNxDrsWaitMinutes(),
                preview != null ? preview.waitMinutes : null));
        snap.setWarningLabel(resolveWarningLabel(stop, preview));
        snap.setSequenceBucket(sequenceBucket);
        snap.setSequenceReason(sequenceReason);
        snap.setSequenceSortKey(sequenceSortKey);
        snap.setProjectedArrivalTimeS(projectedArrivalTimeS);
        snap.setLat(stop.getNxDrsLat());
        snap.setLng(stop.getNxDrsLng());
        snap.setStatusLabel(resolveStopStatusLabel(stop, task, stopKind, driverUserId, ctx));
        snap.setGoodsSummary(DisRouteSandboxDisplayFormatHelper.buildGoodsSummary(task));
        snap.setItems(DisRouteSandboxTodayDriverRouteCardBuilder.buildCardItemsForView(task));
        snap.setSourceStop(stop);
        snap.setStopKind(stopKind);
        return snap;
    }

    private static Integer resolveProjectedArrivalTimeS(
            NxDisRouteStopEntity stop,
            OwnerFixedRouteTimeWindowRouteSequencer.StopPreviewSnapshot preview,
            OwnerFixedRouteTimeWindowRouteSequencer.SequencingSnapshot seqCtx) {
        if (stop != null && stop.getNxDrsPlannedArrivalAt() != null
                && seqCtx != null && seqCtx.dayStartMs > 0L) {
            long seconds = (stop.getNxDrsPlannedArrivalAt().getTime() - seqCtx.dayStartMs) / 1000L;
            if (seconds >= 0L) {
                return (int) seconds;
            }
        }
        return preview != null ? preview.projectedArrivalTimeS : null;
    }

    private static Integer resolveProjectedArrivalTimeSFromEntity(
            NxDisRouteStopEntity stop,
            DispatchStrategyContext strategyContext) {
        if (stop == null || stop.getNxDrsPlannedArrivalAt() == null) {
            return null;
        }
        OwnerFixedRouteTimeWindowRouteSequencer.SequencingSnapshot seqCtx =
                OwnerFixedRouteTimeWindowRouteSequencer.buildSequencingSnapshot(strategyContext);
        if (seqCtx.dayStartMs <= 0L) {
            return null;
        }
        long seconds = (stop.getNxDrsPlannedArrivalAt().getTime() - seqCtx.dayStartMs) / 1000L;
        return seconds >= 0L ? (int) seconds : null;
    }

    private static String resolveWarningLabel(
            NxDisRouteStopEntity stop,
            OwnerFixedRouteTimeWindowRouteSequencer.StopPreviewSnapshot preview) {
        if (stop != null && stop.getNxDrsTimeWindowStatusLabel() != null
                && !stop.getNxDrsTimeWindowStatusLabel().trim().isEmpty()
                && !DisRouteStopTimeWindowStatus.OK.equals(stop.getNxDrsTimeWindowStatus())
                && !DisRouteStopTimeWindowStatus.NO_WINDOW.equals(stop.getNxDrsTimeWindowStatus())
                && !DisRouteStopTimeWindowStatus.EARLY_WAIT.equals(stop.getNxDrsTimeWindowStatus())) {
            return stop.getNxDrsTimeWindowStatusLabel().trim();
        }
        return preview != null ? preview.warningLabel : null;
    }

    private static List<NxDisRouteStopEntity> fallbackSortVisible(List<NxDisRouteStopEntity> visible) {
        if (visible == null || visible.isEmpty()) {
            return Collections.emptyList();
        }
        List<NxDisRouteStopEntity> ordered = new ArrayList<NxDisRouteStopEntity>(visible);
        Collections.sort(ordered, new Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                return Integer.compare(resolveManualSortSeq(a), resolveManualSortSeq(b));
            }
        });
        return ordered;
    }

    private static List<NxDisRouteStopEntity> sortConfirmedStops(List<NxDisRouteStopEntity> stops) {
        return fallbackSortVisible(stops);
    }

    private static int resolveManualSortSeq(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return 999;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getNxDstManualStopSeq() != null && task.getNxDstManualStopSeq() > 0) {
            return task.getNxDstManualStopSeq();
        }
        if (stop.getNxDrsStopSeq() != null && stop.getNxDrsStopSeq() > 0) {
            return stop.getNxDrsStopSeq();
        }
        return 999;
    }

    public static List<Map<String, Object>> collectScheduleWarnings(List<VisibleDriverRouteSnapshot> routes) {
        List<Map<String, Object>> warnings = new ArrayList<Map<String, Object>>();
        if (routes == null) {
            return warnings;
        }
        for (VisibleDriverRouteSnapshot route : routes) {
            if (route == null || route.getScheduleWarning() == null
                    || route.getScheduleWarning().trim().isEmpty()) {
                continue;
            }
            Map<String, Object> warning = new LinkedHashMap<String, Object>();
            warning.put("driverUserId", route.getDriverUserId());
            warning.put("driverName", route.getDriverName());
            warning.put("sectionKey", route.getSectionKey());
            warning.put("message", route.getScheduleWarning().trim());
            warning.put("legMetricsFallbackUsed", route.isLegMetricsFallbackUsed());
            warnings.add(warning);
        }
        return warnings;
    }

    private static List<NxDisRouteStopEntity> excludeConfirmedDepartments(
            List<NxDisRouteStopEntity> stops,
            Set<Integer> confirmedDepIds) {
        if (stops == null || stops.isEmpty() || confirmedDepIds == null || confirmedDepIds.isEmpty()) {
            return stops != null ? stops : Collections.<NxDisRouteStopEntity>emptyList();
        }
        List<NxDisRouteStopEntity> filtered = new ArrayList<NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            if (depId != null && confirmedDepIds.contains(depId)) {
                continue;
            }
            filtered.add(stop);
        }
        return filtered;
    }

    private static List<NxDisRouteStopEntity> mergePendingSuggestedOntoConfirmed(
            List<NxDisRouteStopEntity> confirmedStops,
            List<NxDisRouteStopEntity> suggestedStops) {
        if (confirmedStops == null || confirmedStops.isEmpty()) {
            return confirmedStops != null ? confirmedStops : Collections.<NxDisRouteStopEntity>emptyList();
        }
        List<NxDisRouteStopEntity> merged = new ArrayList<NxDisRouteStopEntity>(confirmedStops);
        if (suggestedStops == null || suggestedStops.isEmpty()) {
            return merged;
        }
        Set<Integer> driversWithConfirmed = indexDriverIds(confirmedStops);
        Set<Integer> existingDepIds = indexDepartmentIds(confirmedStops);
        for (NxDisRouteStopEntity stop : suggestedStops) {
            if (stop == null) {
                continue;
            }
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            if (depId != null && existingDepIds.contains(depId)) {
                continue;
            }
            Integer driverId = DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(stop);
            if (driverId == null || !driversWithConfirmed.contains(driverId)) {
                continue;
            }
            merged.add(stop);
            if (depId != null) {
                existingDepIds.add(depId);
            }
        }
        return merged;
    }

    private static Map<Integer, StopAssignment> indexPlanStops(DispatchAssignmentPlan plan) {
        Map<Integer, StopAssignment> index = new LinkedHashMap<Integer, StopAssignment>();
        if (plan == null || plan.getDriverRoutes() == null) {
            return index;
        }
        for (DriverRoutePlan route : plan.getDriverRoutes()) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            for (StopAssignment stop : route.getStops()) {
                if (stop != null && stop.getDepFatherId() != null) {
                    index.put(stop.getDepFatherId(), stop);
                }
            }
        }
        return index;
    }

    private static DriverRoutePlan resolvePlanRoute(DispatchAssignmentPlan plan, Integer driverUserId) {
        if (plan == null || driverUserId == null || plan.getDriverRoutes() == null) {
            return null;
        }
        for (DriverRoutePlan route : plan.getDriverRoutes()) {
            if (route != null && driverUserId.equals(route.getDriverUserId())) {
                return route;
            }
        }
        return null;
    }

    private static NxDisDriverRouteEntity findPlanRoute(NxDisRoutePlanEntity plan, Integer driverUserId) {
        if (plan == null || driverUserId == null || plan.getDriverRoutes() == null) {
            return null;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route != null && driverUserId.equals(route.getNxDdrDriverUserId())) {
                return route;
            }
        }
        return null;
    }

    private static DispatchStrategyContext buildStrategyContext(SandboxTodayPageBuildContext ctx) {
        return DispatchStrategyContext.builder()
                .disId(ctx.getDisId())
                .routeDate(ctx.getRouteDate())
                .batchCode(ctx.getBatchCode())
                .serverNow(ctx.getServerNow() != null ? ctx.getServerNow() : new Date())
                .build();
    }

    private static Map<Integer, List<NxDisRouteStopEntity>> groupStopsByDriver(List<NxDisRouteStopEntity> stops) {
        Map<Integer, List<NxDisRouteStopEntity>> byDriver = new LinkedHashMap<Integer, List<NxDisRouteStopEntity>>();
        if (stops == null) {
            return byDriver;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Integer driverId = DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(stop);
            if (driverId == null) {
                continue;
            }
            List<NxDisRouteStopEntity> bucket = byDriver.get(driverId);
            if (bucket == null) {
                bucket = new ArrayList<NxDisRouteStopEntity>();
                byDriver.put(driverId, bucket);
            }
            bucket.add(stop);
        }
        return byDriver;
    }

    private static Set<Integer> indexDriverIds(List<NxDisRouteStopEntity> stops) {
        Set<Integer> ids = new HashSet<Integer>();
        if (stops == null) {
            return ids;
        }
        for (NxDisRouteStopEntity stop : stops) {
            Integer driverId = DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(stop);
            if (driverId != null) {
                ids.add(driverId);
            }
        }
        return ids;
    }

    private static Set<Integer> indexDepartmentIds(List<NxDisRouteStopEntity> stops) {
        Set<Integer> depIds = new HashSet<Integer>();
        if (stops == null) {
            return depIds;
        }
        for (NxDisRouteStopEntity stop : stops) {
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            if (depId != null) {
                depIds.add(depId);
            }
        }
        return depIds;
    }

    private static void sortRouteSnapshots(List<VisibleDriverRouteSnapshot> routes) {
        Collections.sort(routes, new Comparator<VisibleDriverRouteSnapshot>() {
            @Override
            public int compare(VisibleDriverRouteSnapshot a, VisibleDriverRouteSnapshot b) {
                String nameA = a.getDriverName() != null ? a.getDriverName() : "";
                String nameB = b.getDriverName() != null ? b.getDriverName() : "";
                return nameA.compareTo(nameB);
            }
        });
    }

    private static Map<Integer, DriverDispatchCandidateDto> indexDrivers(DriverDispatchListResponse drivers) {
        Map<Integer, DriverDispatchCandidateDto> index = new LinkedHashMap<Integer, DriverDispatchCandidateDto>();
        if (drivers == null || drivers.getDrivers() == null) {
            return index;
        }
        for (DriverDispatchCandidateDto driver : drivers.getDrivers()) {
            if (driver != null && driver.getDriverUserId() != null) {
                index.put(driver.getDriverUserId(), driver);
            }
        }
        return index;
    }

    private static String resolveDriverName(Integer driverUserId,
                                            List<NxDisRouteStopEntity> stops,
                                            NxDisDriverRouteEntity route,
                                            Map<Integer, DriverDispatchCandidateDto> driverIndex) {
        if (route != null && route.getDriverName() != null && !route.getDriverName().trim().isEmpty()) {
            return route.getDriverName().trim();
        }
        DriverDispatchCandidateDto meta = driverIndex.get(driverUserId);
        if (meta != null && meta.getDriverName() != null && !meta.getDriverName().trim().isEmpty()) {
            return meta.getDriverName().trim();
        }
        if (stops != null) {
            for (NxDisRouteStopEntity stop : stops) {
                if (stop != null && stop.getSuggestedDriverName() != null
                        && !stop.getSuggestedDriverName().trim().isEmpty()) {
                    return stop.getSuggestedDriverName().trim();
                }
            }
        }
        return "司机 " + driverUserId;
    }

    private static DisRouteSandboxTodayRouteKind resolveStopKind(NxDisRouteStopEntity stop,
                                                                 NxDisDriverRouteEntity route,
                                                                 DisRouteSandboxTodayRouteKind cardKind) {
        if (cardKind == DisRouteSandboxTodayRouteKind.CONFIRMED) {
            return DisRouteSandboxTodayRouteKind.CONFIRMED;
        }
        if (DisRouteSandboxTodayDriverRouteCardBuilder.isConfirmedDispatchStop(stop, route)) {
            return DisRouteSandboxTodayRouteKind.CONFIRMED;
        }
        return DisRouteSandboxTodayRouteKind.SUGGESTED;
    }

    private static String resolveCardKey(NxDisRouteStopEntity stop) {
        if (stop.getSandboxStopKey() != null && !stop.getSandboxStopKey().trim().isEmpty()) {
            return stop.getSandboxStopKey().trim();
        }
        if (stop.getNxDrsDepartmentId() != null) {
            return DisRouteSandboxStopKeyUtils.build(stop.getNxDrsDepartmentId());
        }
        return "stop";
    }

    private static Integer resolveDepFatherId(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsDepartmentId() != null) {
            return stop.getNxDrsDepartmentId();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        return task != null ? task.getNxDstDepFatherId() : null;
    }

    private static String resolveCustomerName(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (stop.getNxDrsDepartmentName() != null && !stop.getNxDrsDepartmentName().trim().isEmpty()) {
            return stop.getNxDrsDepartmentName().trim();
        }
        if (stop.getLiveDepartmentName() != null && !stop.getLiveDepartmentName().trim().isEmpty()) {
            return stop.getLiveDepartmentName().trim();
        }
        if (task != null && task.getNxDstDepName() != null && !task.getNxDstDepName().trim().isEmpty()) {
            return task.getNxDstDepName().trim();
        }
        if (stop.getNxDrsDepartmentId() != null) {
            return "客户 " + stop.getNxDrsDepartmentId();
        }
        return "未知客户";
    }

    private static String resolveTimeLabel(NxDisRouteStopEntity stop, Date serverNow) {
        if (stop.getFastestArrivalLabel() != null && !stop.getFastestArrivalLabel().trim().isEmpty()) {
            return stop.getFastestArrivalLabel().trim();
        }
        if (stop.getNxDrsPlannedArrivalAt() != null && serverNow != null) {
            String label = DisRouteSandboxScheduleLabelHelper.formatFastestArrivalLabel(
                    stop.getNxDrsPlannedArrivalAt(), serverNow, stop.getScheduleMode());
            if (label != null) {
                return label;
            }
        }
        Long legDurationS = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
        if (legDurationS != null && legDurationS > 0L) {
            long minutes = Math.max(1L, Math.round(legDurationS / 60.0));
            return "预计最快 约 " + minutes + " 分钟后到";
        }
        return null;
    }

    private static String resolveStopStatusLabel(NxDisRouteStopEntity stop,
                                                 NxDisShipmentTaskEntity task,
                                                 DisRouteSandboxTodayRouteKind kind,
                                                 Integer driverUserId,
                                                 SandboxTodayPageBuildContext ctx) {
        if (kind == DisRouteSandboxTodayRouteKind.CONFIRMED) {
            return "已分派 / 可装车";
        }
        if (kind == DisRouteSandboxTodayRouteKind.SUGGESTED) {
            String windowStatus = stop != null ? stop.getNxDrsTimeWindowStatus() : null;
            if (DisRouteStopTimeWindowStatus.LATE.equals(windowStatus)
                    || DisRouteStopTimeWindowStatus.SUPPLEMENT_AFTER_WINDOW.equals(windowStatus)
                    || DisRouteStopTimeWindowStatus.EARLY_WAIT.equals(windowStatus)) {
                if (stop.getNxDrsTimeWindowStatusLabel() != null
                        && !stop.getNxDrsTimeWindowStatusLabel().trim().isEmpty()) {
                    return stop.getNxDrsTimeWindowStatusLabel().trim();
                }
            }
            return null;
        }
        return null;
    }

    private static Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.trim().isEmpty()) {
            return primary.trim();
        }
        return fallback;
    }

    private static long straightLineMeters(GeoPoint a, GeoPoint b) {
        if (a == null || b == null || a.getLat() == null || a.getLng() == null
                || b.getLat() == null || b.getLng() == null) {
            return 0L;
        }
        double lat1 = Math.toRadians(Double.parseDouble(a.getLat().trim()));
        double lat2 = Math.toRadians(Double.parseDouble(b.getLat().trim()));
        double dLat = lat2 - lat1;
        double dLng = Math.toRadians(Double.parseDouble(b.getLng().trim())
                - Double.parseDouble(a.getLng().trim()));
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return Math.round(2 * EARTH_RADIUS_M * Math.asin(Math.min(1.0, Math.sqrt(h))));
    }
}
