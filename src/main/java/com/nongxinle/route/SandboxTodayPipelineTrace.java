package com.nongxinle.route;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.DisRouteOrderSnapshotDto;
import com.nongxinle.dto.route.DriverDispatchCandidateDto;
import com.nongxinle.dto.route.DriverDispatchListResponse;
import com.nongxinle.dto.route.SandboxTodayPageViewModel;
import com.nongxinle.dto.route.SandboxTodayMapOverviewDto;
import com.nongxinle.dto.route.SandboxTodaySectionCardDto;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.route.dispatch.strategy.DispatchAssignmentPlan;
import com.nongxinle.route.dispatch.strategy.DispatchSequenceBucket;
import com.nongxinle.route.dispatch.strategy.DispatchStrategyContext;
import com.nongxinle.route.dispatch.strategy.DriverRoutePlan;
import com.nongxinle.route.dispatch.strategy.FallbackStopAssignment;
import com.nongxinle.route.dispatch.strategy.OwnerFixedRouteTimeWindowRouteSequencer;
import com.nongxinle.route.dispatch.strategy.StopAssignment;
import com.nongxinle.route.proposal.SandboxProposalPlanBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;

/**
 * 今日派车分派中链路统一 debugTrace 收集器。仅在 debug 接口开启时实例化并写入响应 {@code debugTrace}。
 */
public final class SandboxTodayPipelineTrace {

    private final LinkedHashMap<String, Object> root = new LinkedHashMap<String, Object>();

    private SandboxTodayPipelineTrace(boolean formalPageContractMode) {
        root.put("enabled", Boolean.TRUE);
        root.put("formalPageContractMode", formalPageContractMode);
        root.put("chainSummary", buildChainSummary());
    }

    public static SandboxTodayPipelineTrace create(boolean formalPageContractMode) {
        return new SandboxTodayPipelineTrace(formalPageContractMode);
    }

    /** trace 未创建时的占位（便于 debug 接口观察接线问题）。 */
    public static Map<String, Object> diagnosticStub(String reason) {
        Map<String, Object> stub = new LinkedHashMap<String, Object>();
        stub.put("enabled", Boolean.FALSE);
        stub.put("diagnosticReason", reason);
        stub.put("chainSummary", buildChainSummary());
        return stub;
    }

    /** 内部可变 root（collector 写入用）。 */
    public Map<String, Object> toMap() {
        return root;
    }

    /** 写入 HTTP 响应：补全必需 phase 占位并返回独立副本，避免后续 collector 改动影响 JSON。 */
    public Map<String, Object> toResponseMap() {
        ensureRequiredPhaseKeys();
        return new LinkedHashMap<String, Object>(root);
    }

    private void ensureRequiredPhaseKeys() {
        ensurePhasePlaceholder("C_assignment", "compute 未调用 recordAssignment");
        ensurePhasePlaceholder("D_sandboxSuggestedStops", "compute 未调用 recordSandboxSuggestedStops（debug 镜像）");
        ensurePhasePlaceholder("P_proposalPlan", "compute 未调用 recordProposalPlan（分派中唯一主权）");
        ensurePhasePlaceholder("R_routeSequence", "compute 未调用 recordRouteSequence（站序 debug）");
        ensurePhasePlaceholder("G_snapshot", "Today 未调用 recordSnapshotOutput（读 PROPOSAL_PLAN）");
        ensurePhasePlaceholder("H_pageViewModel", "Today 未调用 recordPageViewModel");
        ensureLegacyActiveRouteDebugPlaceholder();
    }

    private void ensureLegacyActiveRouteDebugPlaceholder() {
        if (root.containsKey("legacyActiveRouteDebug")) {
            return;
        }
        Map<String, Object> legacy = new LinkedHashMap<String, Object>();
        legacy.put("recorded", Boolean.FALSE);
        legacy.put("traceRole", "LEGACY_ACTIVE_ROUTE_DEBUG");
        legacy.put("usedByDispatchPage", Boolean.FALSE);
        legacy.put("note", "loading/delivery 与 debug plan 字段；分派中 page 不经过 mergedPlan/partition");
        root.put("legacyActiveRouteDebug", legacy);
    }

    private void ensurePhasePlaceholder(String key, String note) {
        if (root.containsKey(key)) {
            return;
        }
        Map<String, Object> phase = new LinkedHashMap<String, Object>();
        phase.put("recorded", Boolean.FALSE);
        phase.put("note", note);
        root.put(key, phase);
    }

    // ── A. 输入订单/任务 ─────────────────────────────────────────────

    public void recordInputStage(int dbTaskCount,
                                 int effectiveOrderCount,
                                 int virtualTaskCount,
                                 int confirmedStopCount,
                                 int sandboxCandidateStopCount,
                                 int loadingStopCount,
                                 int executionStopCount,
                                 List<NxDisRouteStopEntity> confirmedStops,
                                 List<NxDisRouteStopEntity> loadingStops,
                                 List<NxDisRouteStopEntity> executionStops) {
        Map<String, Object> phase = new LinkedHashMap<String, Object>();
        phase.put("dbTaskCount", dbTaskCount);
        phase.put("effectiveOrderCount", effectiveOrderCount);
        phase.put("taskCount", virtualTaskCount);
        phase.put("sandboxCandidateStopCount", sandboxCandidateStopCount);
        phase.put("loadingStopCount", loadingStopCount);
        phase.put("executionStopCount", executionStopCount);
        phase.put("confirmedStopCount", confirmedStopCount);
        phase.put("stopsByDep", mergeStopSummaries(
                confirmedStops, loadingStops, executionStops, Collections.<NxDisRouteStopEntity>emptyList()));
        root.put("A_inputOrdersTasks", phase);
    }

    public void recordConfirmedStopPartition(List<NxDisRouteStopEntity> sandboxStops,
                                             List<NxDisRouteStopEntity> loadingStops,
                                             List<NxDisRouteStopEntity> executionStops) {
        Map<String, Object> phase = new LinkedHashMap<String, Object>();
        phase.put("sandboxConfirmedStopCount", sizeOf(sandboxStops));
        phase.put("loadingConfirmedStopCount", sizeOf(loadingStops));
        phase.put("executionConfirmedStopCount", sizeOf(executionStops));
        phase.put("stopsByDep", mergeStopSummaries(sandboxStops, loadingStops, executionStops,
                Collections.<NxDisRouteStopEntity>emptyList()));
        root.put("A2_confirmedStopPartition", phase);
    }

    // ── B. 司机资格 ─────────────────────────────────────────────────

    public void recordDriverEligibility(List<NxDistributerUserEntity> allDrivers,
                                        List<NxDistributerUserEntity> eligibleDrivers,
                                        Set<Integer> sandboxIneligibleDrivers,
                                        Set<Integer> offDutyDriverIds,
                                        NxDisDriverRouteDao routeDao,
                                        NxDisShipmentTaskDao taskDao,
                                        NxDisRoutePlanEntity planContext) {
        Map<String, Object> phase = new LinkedHashMap<String, Object>();
        Set<Integer> blocked = DisRouteSandboxDispatchEligibilityHelper.unionDriverIds(
                sandboxIneligibleDrivers, offDutyDriverIds);
        phase.put("allDriverCount", sizeOf(allDrivers));
        phase.put("eligibleDriverCount", sizeOf(eligibleDrivers));
        phase.put("ineligibleDriverCount", blocked != null ? blocked.size() : 0);
        phase.put("sandboxIneligibleDriverIds", toList(sandboxIneligibleDrivers));
        phase.put("offDutyDriverIds", toList(offDutyDriverIds));
        phase.put("allDrivers", summarizeDutyDrivers(allDrivers, blocked, routeDao, taskDao, planContext));
        phase.put("eligibleDrivers", summarizeDutyDrivers(eligibleDrivers, null, routeDao, taskDao, planContext));
        phase.put("ineligibleDrivers", summarizeIneligibleDrivers(allDrivers, blocked, routeDao, taskDao, planContext));
        root.put("B_driverEligibility", phase);
    }

    // ── C. assignment ───────────────────────────────────────────────

    public void recordAssignment(DispatchAssignmentPlan plan,
                                 Set<Integer> historyBoundDepIds,
                                 boolean delegateLegacyOptimizer,
                                 int fallbackOptimizableCount) {
        Map<String, Object> phase = new LinkedHashMap<String, Object>();
        if (plan == null) {
            phase.put("assignmentPlanPresent", false);
            phase.put("note", "dispatchStrategyOrchestrator returned null plan");
            root.put("C_assignment", phase);
            return;
        }
        phase.put("assignmentPlanPresent", true);
        phase.put("strategyMode", plan.getStrategyMode() != null ? plan.getStrategyMode().name() : null);
        phase.put("planningPhase", plan.getPlanningPhase() != null ? plan.getPlanningPhase().name() : null);
        phase.put("delegateLegacyOptimizer", delegateLegacyOptimizer);
        phase.put("historyBoundDepCount", historyBoundDepIds != null ? historyBoundDepIds.size() : 0);
        phase.put("historyBoundDepIds", toList(historyBoundDepIds));
        phase.put("historyBoundStops", summarizeHistoryBoundStops(plan));
        phase.put("fallbackStops", summarizeFallbackStops(plan.getFallbackStops()));
        phase.put("fallbackOptimizableCount", fallbackOptimizableCount);
        phase.put("assignmentPlanDriverRoutes", summarizeAssignmentRoutes(plan.getDriverRoutes()));
        phase.put("fallbackNotMergedReason",
                explainFallbackNotMerged(plan, delegateLegacyOptimizer, fallbackOptimizableCount));
        root.put("C_assignment", phase);
    }

    // ── D. sandboxSuggestedStops ─────────────────────────────────────

    public void recordSandboxSuggestedStops(List<NxDisRouteStopEntity> suggestedStops,
                                            Set<Integer> historyBoundDepIds,
                                            DispatchAssignmentPlan assignmentPlan) {
        Map<String, Object> phase = new LinkedHashMap<String, Object>();
        phase.put("sandboxSuggestedStopCount", sizeOf(suggestedStops));
        phase.put("stops", summarizeSuggestedStops(suggestedStops, historyBoundDepIds, assignmentPlan));
        root.put("D_sandboxSuggestedStops", phase);
    }

    // ── P. proposal plan（分派中主权）──────────────────────────────────

    public void recordProposalPlan(com.nongxinle.route.proposal.SandboxProposalPlan plan) {
        Map<String, Object> phase = new LinkedHashMap<String, Object>();
        if (plan == null) {
            phase.put("proposalPlanPresent", false);
            root.put("P_proposalPlan", phase);
            return;
        }
        phase.put("proposalPlanPresent", true);
        int routeCount = plan.getProposalRoutes() != null ? plan.getProposalRoutes().size() : 0;
        int unassignedCount = plan.getUnassignedStops() != null ? plan.getUnassignedStops().size() : 0;
        int assignedStopCount = 0;
        boolean hasRouteId = false;
        boolean hasForbiddenStop = false;
        List<Map<String, Object>> routeRows = new ArrayList<Map<String, Object>>();
        if (plan.getProposalRoutes() != null) {
            for (com.nongxinle.route.proposal.ProposalDriverRoute route : plan.getProposalRoutes()) {
                if (route == null) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                row.put("driverUserId", route.getDriverUserId());
                row.put("driverName", route.getDriverName());
                List<Integer> depIds = new ArrayList<Integer>();
                List<String> sources = new ArrayList<String>();
                int stopCount = route.getStops() != null ? route.getStops().size() : 0;
                assignedStopCount += stopCount;
                if (route.getStops() != null) {
                    for (com.nongxinle.route.proposal.ProposalStop proposalStop : route.getStops()) {
                        if (proposalStop == null) {
                            continue;
                        }
                        if (proposalStop.getDepFatherId() != null) {
                            depIds.add(proposalStop.getDepFatherId());
                        }
                        if (proposalStop.getProposalSource() != null) {
                            sources.add(proposalStop.getProposalSource().name());
                        }
                        if (proposalStop.getStop() != null
                                && !SandboxProposalPlanBuilder.isEligibleProposalStop(proposalStop.getStop())) {
                            hasForbiddenStop = true;
                        }
                    }
                }
                row.put("depFatherIds", depIds);
                row.put("stopCount", stopCount);
                row.put("proposalSources", sources);
                row.put("hasRouteId", Boolean.FALSE);
                routeRows.add(row);
            }
        }
        if (plan.getUnassignedStops() != null) {
            for (com.nongxinle.route.proposal.ProposalStop proposalStop : plan.getUnassignedStops()) {
                if (proposalStop != null && proposalStop.getStop() != null
                        && !SandboxProposalPlanBuilder.isEligibleProposalStop(proposalStop.getStop())) {
                    hasForbiddenStop = true;
                }
            }
        }
        phase.put("proposalRouteCount", routeCount);
        phase.put("assignedStopCount", assignedStopCount);
        phase.put("unassignedStopCount", unassignedCount);
        phase.put("customerStopCount", assignedStopCount + unassignedCount);
        phase.put("proposalRoutes", routeRows);
        phase.put("containsRouteId", hasRouteId);
        phase.put("containsDeliveredLoadingInDeliveryStop", hasForbiddenStop);
        phase.put("sovereignty", "DISPATCH_PAGE_READS_PROPOSAL_PLAN_ONLY");
        root.put("P_proposalPlan", phase);
    }

    // ── R. route sequence（站序 debug）──────────────────────────────────

    public void recordRouteSequence(com.nongxinle.route.proposal.SandboxProposalPlan plan,
                                    DispatchAssignmentPlan assignmentPlan,
                                    DispatchStrategyContext sequencingContext) {
        Map<String, Object> phase = new LinkedHashMap<String, Object>();
        if (plan == null || plan.getProposalRoutes() == null || plan.getProposalRoutes().isEmpty()) {
            phase.put("recorded", Boolean.FALSE);
            phase.put("note", "proposalPlan 无路线，跳过站序 debug");
            root.put("R_routeSequence", phase);
            return;
        }
        OwnerFixedRouteTimeWindowRouteSequencer.SequencingSnapshot seqCtx =
                sequencingContext != null
                        ? OwnerFixedRouteTimeWindowRouteSequencer.buildSequencingSnapshot(sequencingContext)
                        : null;
        Map<Integer, StopAssignment> planStopByDep = indexPlanStops(assignmentPlan);
        List<Map<String, Object>> routeRows = new ArrayList<Map<String, Object>>();
        for (com.nongxinle.route.proposal.ProposalDriverRoute route : plan.getProposalRoutes()) {
            if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
                continue;
            }
            List<NxDisRouteStopEntity> entities =
                    com.nongxinle.route.proposal.SandboxProposalPlanBuilder.toStopEntities(route.getStops());
            List<OwnerFixedRouteTimeWindowRouteSequencer.RouteStopSnapshot> orderedNodes =
                    OwnerFixedRouteTimeWindowRouteSequencer.toStopSnapshots(entities, planStopByDep);
            OwnerFixedRouteTimeWindowRouteSequencer.RouteDepartSnapshot depart = seqCtx != null
                    ? OwnerFixedRouteTimeWindowRouteSequencer.resolveSuggestedDepartSnapshot(orderedNodes, seqCtx)
                    : null;
            long cursor = depart != null ? depart.departSeconds : 0L;

            Map<String, Object> routeRow = new LinkedHashMap<String, Object>();
            routeRow.put("driverUserId", route.getDriverUserId());
            routeRow.put("driverName", route.getDriverName());
            List<Map<String, Object>> stopRows = new ArrayList<Map<String, Object>>();
            int finalSeq = 0;
            for (int i = 0; i < route.getStops().size(); i++) {
                com.nongxinle.route.proposal.ProposalStop proposalStop = route.getStops().get(i);
                if (proposalStop == null) {
                    continue;
                }
                finalSeq++;
                OwnerFixedRouteTimeWindowRouteSequencer.RouteStopSnapshot node =
                        i < orderedNodes.size() ? orderedNodes.get(i) : null;
                OwnerFixedRouteTimeWindowRouteSequencer.StopPreviewSnapshot preview = null;
                DispatchSequenceBucket bucket = null;
                String sequenceReason = null;
                String distanceTieBreakReason = null;
                if (node != null && seqCtx != null) {
                    preview = OwnerFixedRouteTimeWindowRouteSequencer.computeStopPreviewSnapshot(
                            node, seqCtx, cursor);
                    bucket = OwnerFixedRouteTimeWindowRouteSequencer.classifySequenceBucket(
                            node, seqCtx, cursor);
                    sequenceReason = OwnerFixedRouteTimeWindowRouteSequencer.resolveSequenceReason(
                            bucket, node, preview, seqCtx);
                    distanceTieBreakReason = OwnerFixedRouteTimeWindowRouteSequencer.resolveDistanceTieBreakReason(
                            node, bucket);
                    cursor = preview.serviceEndSeconds;
                }
                NxDisRouteStopEntity entity = proposalStop.getStop();
                StopAssignment planStop = proposalStop.getDepFatherId() != null
                        ? planStopByDep.get(proposalStop.getDepFatherId()) : null;
                Map<String, Object> stopRow = new LinkedHashMap<String, Object>();
                stopRow.put("finalSeq", finalSeq);
                stopRow.put("depFatherId", proposalStop.getDepFatherId());
                stopRow.put("customerName", proposalStop.getCustomerName());
                stopRow.put("proposalSource", proposalStop.getProposalSource() != null
                        ? proposalStop.getProposalSource().name() : null);
                stopRow.put("earliestDeliveryTimeS", resolveEarliestDeliveryTimeS(entity, planStop));
                stopRow.put("latestDeliveryTimeS", resolveLatestDeliveryTimeS(entity, planStop));
                stopRow.put("timeWindowOverrideFlag", resolveTimeWindowOverrideFlag(entity, planStop));
                if (bucket != null) {
                    stopRow.put("sequenceBucket", bucket.name());
                }
                stopRow.put("sequenceReason", sequenceReason);
                stopRow.put("distanceTieBreakReason", distanceTieBreakReason);
                stopRows.add(stopRow);
            }
            routeRow.put("stops", stopRows);
            routeRows.add(routeRow);
        }
        phase.put("recorded", Boolean.TRUE);
        phase.put("sequencer", "OwnerFixedRouteTimeWindowRouteSequencer");
        phase.put("sortPolicy", "L0 manual → L1 on-time by latest → L2 late by overdue → L3 no window; "
                + "history preference driver-only");
        phase.put("proposalRoutes", routeRows);
        root.put("R_routeSequence", phase);
    }

    private static Map<Integer, StopAssignment> indexPlanStops(DispatchAssignmentPlan plan) {
        Map<Integer, StopAssignment> byDep = new HashMap<Integer, StopAssignment>();
        if (plan == null || plan.getDriverRoutes() == null) {
            return byDep;
        }
        for (DriverRoutePlan route : plan.getDriverRoutes()) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            for (StopAssignment stop : route.getStops()) {
                if (stop != null && stop.getDepFatherId() != null) {
                    byDep.put(stop.getDepFatherId(), stop);
                }
            }
        }
        return byDep;
    }

    private static Integer resolveEarliestDeliveryTimeS(NxDisRouteStopEntity entity, StopAssignment planStop) {
        if (entity != null && entity.getNxDrsEarliestDeliveryTimeS() != null) {
            return entity.getNxDrsEarliestDeliveryTimeS();
        }
        return planStop != null ? planStop.getEarliestDeliveryTimeS() : null;
    }

    private static Integer resolveLatestDeliveryTimeS(NxDisRouteStopEntity entity, StopAssignment planStop) {
        if (entity != null && entity.getNxDrsLatestDeliveryTimeS() != null) {
            return entity.getNxDrsLatestDeliveryTimeS();
        }
        return planStop != null ? planStop.getLatestDeliveryTimeS() : null;
    }

    private static Boolean resolveTimeWindowOverrideFlag(NxDisRouteStopEntity entity, StopAssignment planStop) {
        if (entity != null && entity.getNxDrsTimeWindowOverrideFlag() != null) {
            return entity.getNxDrsTimeWindowOverrideFlag() == 1;
        }
        return planStop != null ? planStop.getTimeWindowOverrideFlag() : null;
    }

    // ── legacy active route debug（loading/delivery；非分派中主链）────────

    public void recordMergedPlanBeforeDbMerge(Map<Integer, NxDisDriverRouteEntity> routeByDriver,
                                              List<NxDisRouteStopEntity> suggestedStops,
                                              List<NxDisRouteStopEntity> confirmedStops,
                                              Set<Integer> sandboxIneligibleDrivers,
                                              NxDisDriverRouteDao routeDao,
                                              NxDisShipmentTaskDao taskDao) {
        Map<String, Object> phase = getOrCreateLegacyMergedPlanPhase();
        phase.put("beforeDbMergeRoutes", summarizeMergedRoutes(routeByDriver));
        phase.put("appendSkippedStops", analyzeAppendSkipped(
                suggestedStops, confirmedStops, routeByDriver, sandboxIneligibleDrivers, routeDao, taskDao));
    }

    public void recordMergedPlanAfterDbMerge(NxDisRoutePlanEntity mergedPlan) {
        Map<String, Object> phase = getOrCreateLegacyMergedPlanPhase();
        phase.put("afterDbMergeRoutes", summarizePlanRoutes(mergedPlan != null ? mergedPlan.getDriverRoutes() : null));
        phase.put("mergedPlanDriverRouteCount",
                mergedPlan != null && mergedPlan.getDriverRoutes() != null
                        ? mergedPlan.getDriverRoutes().size() : 0);
        if (mergedPlan != null && mergedPlan.getDriverRoutes() != null) {
            phase.put("mergedPlanRoutes", summarizePlanRoutesDetailed(mergedPlan.getDriverRoutes()));
        }
        finalizeLegacyMergedPlanPhase(phase);
    }

    public void recordPartitionBefore(List<NxDisDriverRouteEntity> routesBefore) {
        Map<String, Object> phase = getOrCreateLegacyPartitionPhase();
        phase.put("inputRouteCount", sizeOf(routesBefore));
        phase.put("inputRoutes", summarizePlanRoutesDetailed(routesBefore));
    }

    public void recordPartitionAfter(NxDisRoutePlanEntity plan,
                                     NxDisDriverRouteDao routeDao,
                                     NxDisShipmentTaskDao taskDao) {
        Map<String, Object> phase = getOrCreateLegacyPartitionPhase();
        int sandboxCount = plan != null && plan.getDriverRoutes() != null ? plan.getDriverRoutes().size() : 0;
        int loadingCount = plan != null && plan.getLoadingDriverRoutes() != null
                ? plan.getLoadingDriverRoutes().size() : 0;
        int executionCount = plan != null && plan.getExecutionDriverRoutes() != null
                ? plan.getExecutionDriverRoutes().size() : 0;
        phase.put("sandboxRoutesCount", sandboxCount);
        phase.put("loadingRoutesCount", loadingCount);
        phase.put("executionRoutesCount", executionCount);
        phase.put("sandboxRoutes", summarizePlanRoutesDetailed(plan != null ? plan.getDriverRoutes() : null));
        phase.put("loadingRoutes", summarizePlanRoutesDetailed(plan != null ? plan.getLoadingDriverRoutes() : null));
        phase.put("executionRoutes", summarizePlanRoutesDetailed(
                plan != null ? plan.getExecutionDriverRoutes() : null));
        phase.put("filteredRoutes", analyzePartitionFilteredRoutes(
                (List<Map<String, Object>>) phase.get("inputRoutes"),
                plan, routeDao, taskDao));
        if (sandboxCount == 0) {
            phase.put("sandboxRoutesEmptyReason", explainSandboxRoutesEmpty(plan, routeDao, taskDao));
        }
        finalizeLegacyPartitionPhase(phase);
    }

    // ── G. snapshot（分派中：读 PROPOSAL_PLAN）────────────────────────

    public void recordSnapshotInput(int inputRouteCount) {
        recordSnapshotInput(inputRouteCount, "MERGED_PLAN_DRIVER_ROUTES");
    }

    public void recordSnapshotInput(int inputRouteCount, String inputSource) {
        Map<String, Object> phase = getOrCreatePhaseG();
        phase.put("snapshotInputRouteCount", inputRouteCount);
        phase.put("snapshotInputSource", inputSource);
    }

    private final List<Map<String, Object>> snapshotSkips = new ArrayList<Map<String, Object>>();

    public void addSnapshotSkip(Integer driverUserId,
                                String driverName,
                                int rawStopCount,
                                int visibleStopCount,
                                String skipReason) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("driverUserId", driverUserId);
        row.put("driverName", driverName);
        row.put("rawStopCount", rawStopCount);
        row.put("visibleStopCount", visibleStopCount);
        row.put("skipReason", skipReason);
        snapshotSkips.add(row);
    }

    public void recordSnapshotOutput(List<VisibleDriverRouteSnapshot> snapshots) {
        Map<String, Object> phase = getOrCreatePhaseG();
        phase.put("snapshotOutputCount", snapshots != null ? snapshots.size() : 0);
        phase.put("snapshots", summarizeSnapshots(snapshots));
        phase.put("skippedRoutes", new ArrayList<Map<String, Object>>(snapshotSkips));
        if (snapshots == null || snapshots.isEmpty()) {
            phase.put("outputEmptyReason", explainSnapshotEmpty(phase, snapshotSkips));
        }
        root.put("G_snapshot", phase);
    }

    // ── H. pageViewModel ─────────────────────────────────────────────

    public void recordPageViewModel(SandboxTodayPageViewModel viewModel,
                                    List<VisibleDriverRouteSnapshot> snapshots,
                                    int sandboxSuggestedStopCount,
                                    NxDisRoutePlanEntity mergedPlan) {
        recordPageViewModel(viewModel, snapshots, sandboxSuggestedStopCount, mergedPlan, null);
    }

    public void recordPageViewModel(SandboxTodayPageViewModel viewModel,
                                    List<VisibleDriverRouteSnapshot> snapshots,
                                    int proposalCustomerStopCount,
                                    NxDisRoutePlanEntity mergedPlan,
                                    com.nongxinle.route.proposal.SandboxProposalPlan proposalPlan) {
        Map<String, Object> phase = new LinkedHashMap<String, Object>();
        int sectionsCount = viewModel != null && viewModel.getSections() != null
                ? viewModel.getSections().size() : 0;
        int driverRouteCardCount = countDriverRouteCards(viewModel);
        int mapMarkerCount = countMapMarkers(viewModel);
        int topMetricsCustomerStopCount = extractTopMetricsCustomerStopCount(viewModel);
        phase.put("sectionsCount", sectionsCount);
        phase.put("driverRouteCardCount", driverRouteCardCount);
        phase.put("mapMarkerCount", mapMarkerCount);
        phase.put("topMetricsCustomerStopCount", topMetricsCustomerStopCount);
        phase.put("sandboxSuggestedStopCount", proposalCustomerStopCount);
        phase.put("proposalRouteCount", proposalPlan != null && proposalPlan.getSummary() != null
                ? proposalPlan.getSummary().getProposalRouteCount() : 0);
        phase.put("proposalCustomerStopCount", proposalPlan != null && proposalPlan.getSummary() != null
                ? proposalPlan.getSummary().getCustomerStopCount() : proposalCustomerStopCount);
        phase.put("legacyMergedPlanDriverRouteCount", mergedPlan != null && mergedPlan.getDriverRoutes() != null
                ? mergedPlan.getDriverRoutes().size() : 0);
        phase.put("legacyMergedPlanNote", "见 legacyActiveRouteDebug；不参与分派中 pageViewModel");
        phase.put("visibleSnapshotCount", snapshots != null ? snapshots.size() : 0);
        phase.put("finalDriverRoutesSource", "proposalPlan -> visibleDriverRoutes");
        if (sectionsCount == 0) {
            phase.put("sectionsEmptyReason", explainSectionsEmpty(
                    mergedPlan, snapshots, proposalCustomerStopCount));
        }
        root.put("H_pageViewModel", phase);
    }

    public void recordDriverCardsOverlay(DriverDispatchListResponse drivers) {
        Map<String, Object> overlay = new LinkedHashMap<String, Object>();
        if (drivers == null || drivers.getDrivers() == null) {
            overlay.put("driverCards", Collections.emptyList());
        } else {
            List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
            for (DriverDispatchCandidateDto dto : drivers.getDrivers()) {
                if (dto == null) {
                    continue;
                }
                Map<String, Object> card = new LinkedHashMap<String, Object>();
                card.put("driverUserId", dto.getDriverUserId());
                card.put("driverName", dto.getDriverName());
                card.put("dispatchStage", deriveDispatchStageFromDto(dto));
                card.put("currentStopCount", dto.getCurrentStopCount());
                card.put("sandboxSuggestedStopCount", dto.getSandboxSuggestedStopCount());
                card.put("confirmedStopCount", dto.getConfirmedStopCount());
                card.put("batchEligible", dto.getBatchEligible());
                card.put("ineligibleReason", dto.getIneligibleReason());
                card.put("availabilityStatus", deriveAvailabilityStatusFromDto(dto));
                card.put("note", explainDriverCardStopCountSource(dto));
                cards.add(card);
            }
            overlay.put("driverCards", cards);
        }
        root.put("driverCardsOverlay", overlay);
    }

    // ── chain summary (15 steps) ─────────────────────────────────────

    private static List<Map<String, Object>> buildChainSummary() {
        List<Map<String, Object>> steps = new ArrayList<Map<String, Object>>();
        addStep(steps, 1, "当前未完成订单 / tasks",
                "DisRouteSandboxComputeServiceImpl.compute",
                "queryEligibleOrders → buildVirtualTasks；pending 进 optimizable，不进 Proposal 的 DELIVERED 历史");
        addStep(steps, 2, "策略分派 C_assignment",
                "DispatchStrategyOrchestrator.plan",
                "history binding + fallbackStops；策略 debug，非 page 主权");
        addStep(steps, 3, "suggestedStops / unassignedStops",
                "applyHistoryDriverBindingsFromPlan + applyOptimizationInMemory",
                "ephemeral 内存列表；sandboxSuggestedStops 仅 debug 镜像");
        addStep(steps, 4, "SandboxProposalPlan（分派中唯一主权）",
                "SandboxProposalPlanBuilder.build",
                "history + optimizer 结果写入 proposalRoutes / unassignedStops；无 routeId / 无 DELIVERED");
        addStep(steps, 5, "司机可派判断",
                "DisRouteSandboxDriverDispatchStateHelper + duty",
                "ON_DUTY 且无 LOADING/IN_DELIVERY active route → IDLE/AVAILABLE；不用 COMPLETED 作当前状态");
        addStep(steps, 6, "VisibleDriverRouteSnapshot",
                "VisibleDriverRouteSnapshotBuilder.buildFromProposalPlan",
                "只读 proposalPlan.proposalRoutes；不读 mergedPlan.driverRoutes / partition");
        addStep(steps, 7, "pageViewModel sections / map / topMetrics",
                "DisRouteSandboxTodayPageViewModelBuilder.build",
                "唯一主权 = visibleDriverRoutes ← SandboxProposalPlan");
        addStep(steps, 8, "legacy buildMergedPlan（非分派中主链）",
                "DisRouteSandboxComputeServiceImpl.buildMergedPlan",
                "loading/delivery/debug plan 字段；可与 Proposal 混读误导，已降级至 legacyActiveRouteDebug");
        addStep(steps, 9, "legacy partitionPlanRoutes（非分派中主链）",
                "DisRouteSandboxReadModelPartitionHelper.partitionPlanRoutes",
                "ActiveRoute 切分 sandbox/loading/execution；不服务分派中 pageViewModel");
        return steps;
    }

    private static void addStep(List<Map<String, Object>> steps, int order, String name,
                                String location, String note) {
        Map<String, Object> step = new LinkedHashMap<String, Object>();
        step.put("step", order);
        step.put("name", name);
        step.put("location", location);
        step.put("note", note);
        steps.add(step);
    }

    // ── helpers ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> getLegacyActiveRouteDebugRoot() {
        Object existing = root.get("legacyActiveRouteDebug");
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }
        Map<String, Object> legacy = new LinkedHashMap<String, Object>();
        legacy.put("traceRole", "LEGACY_ACTIVE_ROUTE_DEBUG");
        legacy.put("usedByDispatchPage", Boolean.FALSE);
        legacy.put("note", "loading/delivery 与 debug plan；分派中 page 只读 SandboxProposalPlan");
        root.put("legacyActiveRouteDebug", legacy);
        root.remove("E_mergedPlan");
        root.remove("F_partition");
        return legacy;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrCreateLegacyMergedPlanPhase() {
        Map<String, Object> legacy = getLegacyActiveRouteDebugRoot();
        Object existing = legacy.get("mergedPlan");
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }
        Map<String, Object> phase = new LinkedHashMap<String, Object>();
        legacy.put("mergedPlan", phase);
        return phase;
    }

    private void finalizeLegacyMergedPlanPhase(Map<String, Object> phase) {
        if (phase == null) {
            return;
        }
        phase.put("traceRole", "LEGACY_ACTIVE_ROUTE_DEBUG");
        phase.put("usedByDispatchPage", Boolean.FALSE);
        phase.put("legacyKey", "mergedPlan");
        phase.put("note", "buildMergedPlan 混合 Active/历史；不进入分派中 snapshot/pageViewModel");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrCreateLegacyPartitionPhase() {
        Map<String, Object> legacy = getLegacyActiveRouteDebugRoot();
        Object existing = legacy.get("partition");
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }
        Map<String, Object> phase = new LinkedHashMap<String, Object>();
        legacy.put("partition", phase);
        return phase;
    }

    private void finalizeLegacyPartitionPhase(Map<String, Object> phase) {
        if (phase == null) {
            return;
        }
        phase.put("traceRole", "LEGACY_ACTIVE_ROUTE_DEBUG");
        phase.put("usedByDispatchPage", Boolean.FALSE);
        phase.put("legacyKey", "partition");
        phase.put("note", "partitionPlanRoutes 仅服务 loading/delivery；分派中不读 sandboxRoutesCount");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrCreatePhaseG() {
        Object existing = root.get("G_snapshot");
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }
        Map<String, Object> phase = new LinkedHashMap<String, Object>();
        root.put("G_snapshot", phase);
        return phase;
    }

    private static int sizeOf(List<?> list) {
        return list != null ? list.size() : 0;
    }

    private static List<Integer> toList(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<Integer>(ids);
    }

    private static List<Map<String, Object>> mergeStopSummaries(
            List<NxDisRouteStopEntity> confirmed,
            List<NxDisRouteStopEntity> loading,
            List<NxDisRouteStopEntity> execution,
            List<NxDisRouteStopEntity> extra) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        appendStopSummaries(out, confirmed, "CONFIRMED");
        appendStopSummaries(out, loading, "LOADING");
        appendStopSummaries(out, execution, "EXECUTION");
        appendStopSummaries(out, extra, "OTHER");
        return out;
    }

    private static void appendStopSummaries(List<Map<String, Object>> out,
                                            List<NxDisRouteStopEntity> stops,
                                            String bucket) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            Map<String, Object> row = summarizeStop(stop);
            row.put("bucket", bucket);
            out.add(row);
        }
    }

    private static Map<String, Object> summarizeStop(NxDisRouteStopEntity stop) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        row.put("depFatherId", task != null ? task.getNxDstDepFatherId() : null);
        row.put("customerName", resolveCustomerName(stop, task));
        row.put("stopSource", stop.getStopSource());
        row.put("stopScope", stop.getStopScope());
        row.put("status", task != null ? task.getNxDstStatus() : null);
        row.put("assignedDriverUserId", task != null ? task.getNxDstAssignedDriverUserId() : null);
        row.put("suggestedDriverUserId", resolveSuggestedDriverUserId(stop, task));
        return row;
    }

    private static Integer resolveSuggestedDriverUserId(NxDisRouteStopEntity stop,
                                                        NxDisShipmentTaskEntity task) {
        if (stop != null && stop.getSuggestedDriverUserId() != null) {
            return stop.getSuggestedDriverUserId();
        }
        if (task != null && task.getNxDstSuggestedDriverUserId() != null) {
            return task.getNxDstSuggestedDriverUserId();
        }
        return task != null ? task.getSuggestedDriverUserId() : null;
    }

    private static String resolveCustomerName(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (stop != null && stop.getLiveDepartmentName() != null && !stop.getLiveDepartmentName().trim().isEmpty()) {
            return stop.getLiveDepartmentName();
        }
        if (stop != null && stop.getNxDrsDepartmentName() != null && !stop.getNxDrsDepartmentName().trim().isEmpty()) {
            return stop.getNxDrsDepartmentName();
        }
        if (task != null && task.getLiveDepartmentName() != null && !task.getLiveDepartmentName().trim().isEmpty()) {
            return task.getLiveDepartmentName();
        }
        if (task != null && task.getNxDstDepName() != null) {
            return task.getNxDstDepName();
        }
        return null;
    }

    private static List<Map<String, Object>> summarizeDutyDrivers(
            List<NxDistributerUserEntity> drivers,
            Set<Integer> highlightBlocked,
            NxDisDriverRouteDao routeDao,
            NxDisShipmentTaskDao taskDao,
            NxDisRoutePlanEntity planContext) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        if (drivers == null) {
            return out;
        }
        for (NxDistributerUserEntity driver : drivers) {
            if (driver == null) {
                continue;
            }
            Integer driverId = driver.getNxDistributerUserId();
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("driverUserId", driverId);
            row.put("driverName", driver.getNxDiuWxNickName());
            row.put("dutyStatus", "ON_DUTY");
            NxDisDriverRouteEntity route = findDriverRouteOnPlan(planContext, driverId);
            String phase = route != null
                    ? DisRouteSandboxDriverDispatchStateHelper.resolveRoutePhase(route, routeDao, taskDao)
                    : DisRouteSandboxDriverDispatchPhase.IDLE;
            row.put("dispatchStage", mapPhaseToDispatchStage(phase));
            row.put("routePhase", phase);
            row.put("routeStatus", route != null ? route.getNxDdrRouteStatus() : null);
            boolean blocked = highlightBlocked != null && driverId != null && highlightBlocked.contains(driverId);
            row.put("eligibleForSandboxDispatch", !blocked);
            if (blocked) {
                row.put("ineligibleReason", explainDriverIneligible(driverId, route, routeDao, taskDao, phase));
            }
            out.add(row);
        }
        return out;
    }

    private static List<Map<String, Object>> summarizeIneligibleDrivers(
            List<NxDistributerUserEntity> allDrivers,
            Set<Integer> blocked,
            NxDisDriverRouteDao routeDao,
            NxDisShipmentTaskDao taskDao,
            NxDisRoutePlanEntity planContext) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        if (allDrivers == null || blocked == null || blocked.isEmpty()) {
            return out;
        }
        for (NxDistributerUserEntity driver : allDrivers) {
            if (driver == null || driver.getNxDistributerUserId() == null) {
                continue;
            }
            Integer driverId = driver.getNxDistributerUserId();
            if (!blocked.contains(driverId)) {
                continue;
            }
            NxDisDriverRouteEntity route = findDriverRouteOnPlan(planContext, driverId);
            String phase = route != null
                    ? DisRouteSandboxDriverDispatchStateHelper.resolveRoutePhase(route, routeDao, taskDao)
                    : DisRouteSandboxDriverDispatchPhase.IDLE;
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("driverUserId", driverId);
            row.put("driverName", driver.getNxDiuWxNickName());
            row.put("routePhase", phase);
            row.put("dispatchStage", mapPhaseToDispatchStage(phase));
            row.put("ineligibleReason", explainDriverIneligible(driverId, route, routeDao, taskDao, phase));
            out.add(row);
        }
        return out;
    }

    private static String explainDriverIneligible(Integer driverId,
                                                  NxDisDriverRouteEntity route,
                                                  NxDisDriverRouteDao routeDao,
                                                  NxDisShipmentTaskDao taskDao,
                                                  String phase) {
        if (DisRouteSandboxDriverDispatchPhase.LOADING.equals(phase)) {
            return "routePhase=LOADING：装车中，blocksSandboxComputeDispatch";
        }
        if (DisRouteSandboxDriverDispatchPhase.ACTIVE_EXECUTION.equals(phase)) {
            return "routePhase=ACTIVE_EXECUTION：配送中，blocksSandboxComputeDispatch";
        }
        if (route != null && DisRouteSandboxDriverDispatchStateHelper.blocksSandboxComputeDispatch(
                route, routeDao, taskDao)) {
            return "blocksSandboxComputeDispatch(routePhase=" + phase + ")";
        }
        return "offDuty 或 sandboxIneligibleDriverIds 集合内(driverUserId=" + driverId + ")";
    }

    private static String mapPhaseToDispatchStage(String phase) {
        if (DisRouteSandboxDriverDispatchPhase.LOADING.equals(phase)) {
            return "LOADING";
        }
        if (DisRouteSandboxDriverDispatchPhase.ACTIVE_EXECUTION.equals(phase)) {
            return "IN_DELIVERY";
        }
        return "IDLE";
    }

    private static NxDisDriverRouteEntity findDriverRouteOnPlan(NxDisRoutePlanEntity plan, Integer driverId) {
        if (plan == null || driverId == null || plan.getDriverRoutes() == null) {
            return null;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route != null && driverId.equals(route.getNxDdrDriverUserId())) {
                return route;
            }
        }
        return null;
    }

    private static List<Map<String, Object>> summarizeHistoryBoundStops(DispatchAssignmentPlan plan) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        if (plan == null || plan.getDriverRoutes() == null) {
            return out;
        }
        for (DriverRoutePlan route : plan.getDriverRoutes()) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            for (StopAssignment assignment : route.getStops()) {
                if (assignment == null) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                row.put("depFatherId", assignment.getDepFatherId());
                row.put("driverUserId", route.getDriverUserId());
                row.put("driverName", route.getDriverName());
                row.put("source", "HISTORY_BINDING");
                out.add(row);
            }
        }
        return out;
    }

    private static List<Map<String, Object>> summarizeFallbackStops(List<FallbackStopAssignment> fallbackStops) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        if (fallbackStops == null) {
            return out;
        }
        for (FallbackStopAssignment fallback : fallbackStops) {
            if (fallback == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("depFatherId", fallback.getDepFatherId());
            row.put("planningReason", fallback.getPlanningReason() != null
                    ? fallback.getPlanningReason().name() : null);
            row.put("historyReason", fallback.getHistoryReason());
            row.put("source", "FALLBACK");
            out.add(row);
        }
        return out;
    }

    private static List<Map<String, Object>> summarizeAssignmentRoutes(List<DriverRoutePlan> routes) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        if (routes == null) {
            return out;
        }
        for (DriverRoutePlan route : routes) {
            if (route == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("driverUserId", route.getDriverUserId());
            row.put("driverName", route.getDriverName());
            List<Integer> depIds = new ArrayList<Integer>();
            if (route.getStops() != null) {
                for (StopAssignment stop : route.getStops()) {
                    if (stop != null && stop.getDepFatherId() != null) {
                        depIds.add(stop.getDepFatherId());
                    }
                }
            }
            row.put("depFatherIds", depIds);
            row.put("stopCount", depIds.size());
            out.add(row);
        }
        return out;
    }

    private static String explainFallbackNotMerged(DispatchAssignmentPlan plan,
                                                   boolean delegateLegacyOptimizer,
                                                   int fallbackOptimizableCount) {
        if (plan == null) {
            return "无 assignmentPlan";
        }
        int fallbackCount = plan.getFallbackStops() != null ? plan.getFallbackStops().size() : 0;
        if (fallbackCount == 0) {
            return "无 fallbackStops";
        }
        if (!delegateLegacyOptimizer) {
            return "fallbackStops(" + fallbackCount + ") 保留在 assignmentPlan.fallbackStops；"
                    + "未 delegate legacy optimizer，不自动写入 suggestedStops/driverRoutes";
        }
        if (fallbackOptimizableCount == 0) {
            return "fallbackStops(" + fallbackCount + ") 存在但 fallbackOptimizable=0，legacy optimizer 未运行";
        }
        return "fallbackStops(" + fallbackCount + ") 应由 legacy optimizer 写入 suggestedStops；"
                + "不直接 merge 进 assignmentPlan.driverRoutes（设计分离）";
    }

    private static List<Map<String, Object>> summarizeSuggestedStops(
            List<NxDisRouteStopEntity> suggestedStops,
            Set<Integer> historyBoundDepIds,
            DispatchAssignmentPlan assignmentPlan) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        if (suggestedStops == null) {
            return out;
        }
        Set<Integer> historyDeps = historyBoundDepIds != null ? historyBoundDepIds : Collections.<Integer>emptySet();
        for (NxDisRouteStopEntity stop : suggestedStops) {
            if (stop == null) {
                continue;
            }
            Map<String, Object> row = summarizeStop(stop);
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            Integer depId = task != null ? task.getNxDstDepFatherId() : null;
            row.put("suggestedDriverUserId", resolveSuggestedDriverUserId(stop, task));
            row.put("origin", depId != null && historyDeps.contains(depId) ? "HISTORY" : "FALLBACK_OR_OPTIMIZER");
            out.add(row);
        }
        return out;
    }

    private static List<Map<String, Object>> summarizeMergedRoutes(
            Map<Integer, NxDisDriverRouteEntity> routeByDriver) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        if (routeByDriver == null) {
            return out;
        }
        for (NxDisDriverRouteEntity route : routeByDriver.values()) {
            out.add(summarizeRouteBrief(route));
        }
        return out;
    }

    private static List<Map<String, Object>> summarizePlanRoutes(List<NxDisDriverRouteEntity> routes) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        if (routes == null) {
            return out;
        }
        for (NxDisDriverRouteEntity route : routes) {
            out.add(summarizeRouteBrief(route));
        }
        return out;
    }

    private static List<Map<String, Object>> summarizePlanRoutesDetailed(List<NxDisDriverRouteEntity> routes) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        if (routes == null) {
            return out;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null) {
                continue;
            }
            Map<String, Object> row = summarizeRouteBrief(route);
            row.put("routeScope", route.getRouteScope());
            row.put("routeStatus", route.getNxDdrRouteStatus());
            row.put("sandboxEligible", route.getSandboxEligible());
            row.put("stopSourceDistribution", countStopSources(route.getStops()));
            out.add(row);
        }
        return out;
    }

    private static Map<String, Object> summarizeRouteBrief(NxDisDriverRouteEntity route) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        if (route == null) {
            return row;
        }
        row.put("routeId", route.getNxDdrId());
        row.put("driverUserId", route.getNxDdrDriverUserId());
        row.put("driverName", route.getDriverName());
        row.put("stopCount", route.getStops() != null ? route.getStops().size() : 0);
        List<Integer> depIds = new ArrayList<Integer>();
        if (route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop == null || stop.getShipmentTask() == null) {
                    continue;
                }
                Integer depId = stop.getShipmentTask().getNxDstDepFatherId();
                if (depId != null) {
                    depIds.add(depId);
                }
            }
        }
        row.put("depFatherIds", depIds);
        return row;
    }

    private static Map<String, Integer> countStopSources(List<NxDisRouteStopEntity> stops) {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        if (stops == null) {
            return counts;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            String source = stop.getStopSource() != null ? stop.getStopSource() : "UNKNOWN";
            counts.put(source, counts.containsKey(source) ? counts.get(source) + 1 : 1);
        }
        return counts;
    }

    private static List<Map<String, Object>> analyzeAppendSkipped(
            List<NxDisRouteStopEntity> suggestedStops,
            List<NxDisRouteStopEntity> confirmedStops,
            Map<Integer, NxDisDriverRouteEntity> routeByDriver,
            Set<Integer> sandboxIneligibleDrivers,
            NxDisDriverRouteDao routeDao,
            NxDisShipmentTaskDao taskDao) {
        List<Map<String, Object>> skipped = new ArrayList<Map<String, Object>>();
        analyzeAppendList(suggestedStops, routeByDriver, sandboxIneligibleDrivers, routeDao, taskDao, skipped);
        analyzeAppendList(confirmedStops, routeByDriver, sandboxIneligibleDrivers, routeDao, taskDao, skipped);
        return skipped;
    }

    private static void analyzeAppendList(List<NxDisRouteStopEntity> stops,
                                          Map<Integer, NxDisDriverRouteEntity> routeByDriver,
                                          Set<Integer> sandboxIneligibleDrivers,
                                          NxDisDriverRouteDao routeDao,
                                          NxDisShipmentTaskDao taskDao,
                                          List<Map<String, Object>> skipped) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null || stop.getShipmentTask() == null) {
                continue;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            Integer driverId = task.getNxDstAssignedDriverUserId() != null
                    ? task.getNxDstAssignedDriverUserId()
                    : task.getNxDstSuggestedDriverUserId();
            if (driverId == null) {
                Map<String, Object> row = summarizeStop(stop);
                row.put("skipReason", "NO_DRIVER_ID");
                skipped.add(row);
                continue;
            }
            boolean ephemeral = DisRouteSandboxDispatchEligibilityHelper.isSandboxEphemeralStop(stop);
            if (ephemeral && !DisRouteSandboxDispatchEligibilityHelper.isDriverEligibleForSandboxDispatch(
                    driverId, sandboxIneligibleDrivers)) {
                Map<String, Object> row = summarizeStop(stop);
                row.put("skipReason", "DRIVER_SANDBOX_INELIGIBLE(driverUserId=" + driverId + ")");
                skipped.add(row);
                continue;
            }
            NxDisDriverRouteEntity route = routeByDriver.get(driverId);
            if (ephemeral && route != null
                    && !DisRouteSandboxDispatchEligibilityHelper.acceptsSandboxEphemeralStops(
                            route, routeDao, taskDao)) {
                Map<String, Object> row = summarizeStop(stop);
                String phase = DisRouteSandboxDriverDispatchStateHelper.resolveRoutePhase(route, routeDao, taskDao);
                row.put("skipReason", "ROUTE_REJECTS_EPHEMERAL(routePhase=" + phase + ")");
                skipped.add(row);
            }
        }
    }

    private static List<Map<String, Object>> analyzePartitionFilteredRoutes(
            List<Map<String, Object>> inputRoutes,
            NxDisRoutePlanEntity plan,
            NxDisDriverRouteDao routeDao,
            NxDisShipmentTaskDao taskDao) {
        List<Map<String, Object>> filtered = new ArrayList<Map<String, Object>>();
        if (inputRoutes == null || inputRoutes.isEmpty()) {
            return filtered;
        }
        Set<String> sandboxKeys = new HashSet<String>();
        if (plan != null && plan.getDriverRoutes() != null) {
            for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
                if (route != null && route.getNxDdrDriverUserId() != null) {
                    sandboxKeys.add(String.valueOf(route.getNxDdrDriverUserId()));
                }
            }
        }
        for (Map<String, Object> input : inputRoutes) {
            if (input == null) {
                continue;
            }
            Object driverUserId = input.get("driverUserId");
            Object stopCount = input.get("stopCount");
            int stops = stopCount instanceof Number ? ((Number) stopCount).intValue() : 0;
            if (stops <= 0) {
                continue;
            }
            String key = driverUserId != null ? String.valueOf(driverUserId) : null;
            if (key != null && sandboxKeys.contains(key)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<String, Object>(input);
            row.put("filterReason", inferPartitionFilterReason(input, plan, routeDao, taskDao));
            filtered.add(row);
        }
        return filtered;
    }

    private static String inferPartitionFilterReason(Map<String, Object> inputRoute,
                                                     NxDisRoutePlanEntity plan,
                                                     NxDisDriverRouteDao routeDao,
                                                     NxDisShipmentTaskDao taskDao) {
        Object driverUserIdObj = inputRoute.get("driverUserId");
        if (driverUserIdObj == null || plan == null) {
            return "UNKNOWN";
        }
        Integer driverUserId = driverUserIdObj instanceof Number
                ? ((Number) driverUserIdObj).intValue() : null;
        NxDisDriverRouteEntity source = findRouteInList(plan.getDriverRoutes(), driverUserId);
        if (source == null && plan.getLoadingDriverRoutes() != null) {
            source = findRouteInList(plan.getLoadingDriverRoutes(), driverUserId);
            if (source != null) {
                return "ROUTE_IN_LOADING_PHASE：sandbox stops 被拆到 loadingDriverRoutes 或 ephemeral 被丢弃";
            }
        }
        if (source == null && plan.getExecutionDriverRoutes() != null) {
            source = findRouteInList(plan.getExecutionDriverRoutes(), driverUserId);
            if (source != null) {
                return "ROUTE_IN_EXECUTION：sandbox ephemeral stops 被剥离";
            }
        }
        String phase = source != null
                ? DisRouteSandboxDriverDispatchStateHelper.resolveRoutePhase(source, routeDao, taskDao)
                : DisRouteSandboxDriverDispatchPhase.IDLE;
        if (DisRouteSandboxDriverDispatchPhase.LOADING.equals(phase)) {
            return "routeInLoadingPhase=true：partition 不写入 plan.driverRoutes（sandbox 区）";
        }
        return "sandboxStops 为空或全部为 execution/loading scope";
    }

    private static NxDisDriverRouteEntity findRouteInList(List<NxDisDriverRouteEntity> routes,
                                                          Integer driverUserId) {
        if (routes == null || driverUserId == null) {
            return null;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route != null && driverUserId.equals(route.getNxDdrDriverUserId())) {
                return route;
            }
        }
        return null;
    }

    private static String explainSandboxRoutesEmpty(NxDisRoutePlanEntity plan,
                                                    NxDisDriverRouteDao routeDao,
                                                    NxDisShipmentTaskDao taskDao) {
        if (plan == null || plan.getLoadingDriverRoutes() == null) {
            return "legacy partition：merge/partition 后无 sandbox route；见 legacyActiveRouteDebug.mergedPlan";
        }
        for (NxDisDriverRouteEntity loadingRoute : plan.getLoadingDriverRoutes()) {
            if (loadingRoute == null) {
                continue;
            }
            String phase = DisRouteSandboxDriverDispatchStateHelper.resolveRoutePhase(
                    loadingRoute, routeDao, taskDao);
            if (DisRouteSandboxDriverDispatchPhase.LOADING.equals(phase)) {
                return "所有含 suggested stops 的司机 routePhase=LOADING；"
                        + "partition 规则 !routeInLoadingPhase 阻止写入 sandboxRoutes";
            }
        }
        return "buildMergedPlan 未写入 stops，或 mergeDbDriverRoutes 覆盖了 ephemeral routes";
    }

    private static List<Map<String, Object>> summarizeSnapshots(List<VisibleDriverRouteSnapshot> snapshots) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        if (snapshots == null) {
            return out;
        }
        for (VisibleDriverRouteSnapshot snapshot : snapshots) {
            if (snapshot == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("driverUserId", snapshot.getDriverUserId());
            row.put("driverName", snapshot.getDriverName());
            row.put("routeKind", snapshot.getRouteKind() != null ? snapshot.getRouteKind().name() : null);
            row.put("stopCount", snapshot.getStops() != null ? snapshot.getStops().size() : 0);
            out.add(row);
        }
        return out;
    }

    private static String explainSnapshotEmpty(Map<String, Object> phase,
                                               List<Map<String, Object>> skippedRoutes) {
        Object inputCount = phase.get("snapshotInputRouteCount");
        if (inputCount instanceof Number && ((Number) inputCount).intValue() == 0) {
            return "partition 后 plan.driverRoutes=0 → snapshot builder 无输入";
        }
        if (skippedRoutes != null && !skippedRoutes.isEmpty()) {
            return "plan.driverRoutes 有输入但全部被 skip（见 skippedRoutes）";
        }
        return "collectVisibleSuggestedStops 过滤后 visible stops 为空";
    }

    private static int countDriverRouteCards(SandboxTodayPageViewModel viewModel) {
        if (viewModel == null || viewModel.getSections() == null) {
            return 0;
        }
        int count = 0;
        for (com.nongxinle.dto.route.SandboxTodaySectionDto section : viewModel.getSections()) {
            if (section == null || section.getCards() == null) {
                continue;
            }
            for (SandboxTodaySectionCardDto card : section.getCards()) {
                if (card != null && "DRIVER_ROUTE".equals(card.getCardType())) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int countMapMarkers(SandboxTodayPageViewModel viewModel) {
        if (viewModel == null || viewModel.getMapOverview() == null) {
            return 0;
        }
        SandboxTodayMapOverviewDto map = viewModel.getMapOverview();
        return map.getMarkers() != null ? map.getMarkers().size() : 0;
    }

    private static int extractTopMetricsCustomerStopCount(SandboxTodayPageViewModel viewModel) {
        if (viewModel == null || viewModel.getTopMetrics() == null) {
            return 0;
        }
        Integer count = viewModel.getTopMetrics().getCustomerStopCount();
        return count != null ? count : 0;
    }

    private static String explainSectionsEmpty(NxDisRoutePlanEntity mergedPlan,
                                               List<VisibleDriverRouteSnapshot> snapshots,
                                               int proposalCustomerStopCount) {
        int snapshotCount = snapshots != null ? snapshots.size() : 0;
        if (proposalCustomerStopCount > 0 && snapshotCount == 0) {
            return "主权断裂：proposalPlan.customerStopCount=" + proposalCustomerStopCount
                    + " 但 VisibleDriverRouteSnapshot=0（见 G_snapshot.skippedRoutes）";
        }
        if (snapshotCount == 0) {
            return "proposalPlan 为空或 snapshot 被过滤 → sections/map 为空";
        }
        return "PageViewModelBuilder 过滤了全部 snapshot";
    }

    /** 分派中运行时：OFF_DUTY / IDLE / LOADING / IN_DELIVERY（不含 COMPLETED）。 */
    private static String deriveAvailabilityStatusFromDto(DriverDispatchCandidateDto dto) {
        if (dto == null) {
            return null;
        }
        if (dto.getDutyStatus() != null && !ON_DUTY.equals(dto.getDutyStatus())) {
            return "OFF_DUTY";
        }
        String phase = DisRouteSandboxDriverDispatchStateHelper.resolveDriverMetaPhase(dto);
        if (DisRouteSandboxDriverDispatchPhase.LOADING.equals(phase)) {
            return "LOADING";
        }
        if (DisRouteSandboxDriverDispatchPhase.ACTIVE_EXECUTION.equals(phase)) {
            return "IN_DELIVERY";
        }
        return "IDLE";
    }

    private static String deriveDispatchStageFromDto(DriverDispatchCandidateDto dto) {
        return deriveAvailabilityStatusFromDto(dto);
    }

    private static String explainDriverCardStopCountSource(DriverDispatchCandidateDto dto) {
        if (dto == null) {
            return null;
        }
        Integer suggested = dto.getSandboxSuggestedStopCount();
        Integer confirmed = dto.getConfirmedStopCount();
        if (suggested != null && suggested > 0) {
            return "sandboxSuggestedStopCount 来自 proposalPlan / listDrivers overlay（非 legacy mergedPlan）";
        }
        if (confirmed != null && confirmed > 0) {
            return "currentStopCount 来自 confirmedStops 或 listDriversForBatch 初始值";
        }
        return "listDriversForBatch 初始 currentStopCount";
    }
}
