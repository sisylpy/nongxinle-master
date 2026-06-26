package com.nongxinle.route.dispatch.strategy;

import com.nongxinle.dto.route.DeliveryHistoryPreferenceBatchResult;
import com.nongxinle.dto.route.DeliveryHistoryPreferenceDto;
import com.nongxinle.entity.NxDisSandboxDayTimeWindowEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.route.DisRouteDeliveryHistoryReason;
import com.nongxinle.route.DisRouteSandboxStopKeyUtils;
import com.nongxinle.route.DisRouteSandboxStopTimeWindowResolver;
import com.nongxinle.route.SandboxStopResolvedTimeWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** PR-2b：OWNER 模式历史司机预绑定（仅司机归属，不排站序/时窗）。 */
final class OwnerFixedRouteHistoryDriverBinder {

    private OwnerFixedRouteHistoryDriverBinder() {
    }

    static void buildPlan(DispatchAssignmentPlan plan,
                          DispatchStrategyContext context,
                          List<NxDisShipmentTaskEntity> optimizable,
                          Set<Integer> eligibleDriverIds,
                          Map<Integer, String> driverNameById) {
        Map<Integer, DeliveryHistoryPreferenceDto> prefByDep = indexPreferences(
                context != null ? context.getDeliveryHistoryPreferences() : null);
        Set<Integer> confirmedDepIds = context != null && context.getConfirmedDepIds() != null
                ? context.getConfirmedDepIds() : new HashSet<Integer>();

        Map<Integer, DriverRoutePlan> routeByDriver = new LinkedHashMap<Integer, DriverRoutePlan>();
        List<FallbackStopAssignment> fallbackStops = new ArrayList<FallbackStopAssignment>();
        Map<Integer, Integer> nextSeqByDriver = new HashMap<Integer, Integer>();

        for (NxDisShipmentTaskEntity task : optimizable) {
            if (task == null || task.getNxDstDepFatherId() == null) {
                continue;
            }
            if (!DispatchStrategyOptimizerEligibility.isPendingOptimizerCandidate(
                    task, confirmedDepIds)) {
                continue;
            }

            DeliveryHistoryPreferenceDto pref = prefByDep.get(task.getNxDstDepFatherId());
            Integer preferredDriverId = pref != null ? pref.getPreferredDriverUserId() : null;
            BindDecision decision = resolveBindDecision(pref, preferredDriverId, eligibleDriverIds);

            if (decision.bind) {
                DriverRoutePlan route = routeByDriver.get(preferredDriverId);
                if (route == null) {
                    route = new DriverRoutePlan();
                    route.setDriverUserId(preferredDriverId);
                    route.setDriverName(driverNameById.get(preferredDriverId));
                    routeByDriver.put(preferredDriverId, route);
                }
                int seq = nextSeq(incrementSeq(nextSeqByDriver, preferredDriverId));
                StopAssignment stop = toStopAssignment(context, task, pref, preferredDriverId, seq,
                        DispatchStopClass.UNTIMED_WITH_HISTORY, DispatchPlanningReason.HISTORY_DRIVER_BIND);
                route.getStops().add(stop);
            } else {
                addFallback(fallbackStops, task, decision.fallbackReason, decision.historyReason);
            }
        }

        plan.getDriverRoutes().addAll(routeByDriver.values());
        plan.setFallbackStops(fallbackStops);
        plan.setHistoryBoundStopCount(countBoundStops(routeByDriver));
        plan.setFallbackStopCount(fallbackStops.size());

        if (plan.getHistoryBoundStopCount() > 0) {
            plan.setPlanningPhase(DispatchPlanningPhase.HISTORY_DRIVER_PREBIND);
            plan.getWarnings().add("history-bound stops="
                    + plan.getHistoryBoundStopCount() + ", fallback stops="
                    + plan.getFallbackStopCount());
        } else {
            plan.setPlanningPhase(DispatchPlanningPhase.LEGACY_OPTIMIZER_DELEGATION);
            plan.getWarnings().add("no history driver bindings; full legacy optimizer");
        }
        if (plan.getFallbackStopCount() > 0) {
            plan.getWarnings().add("fallback deps use legacy BalancedInsertion2OptRouteOptimizer");
        }
    }

    static boolean shouldDelegateLegacyOptimizer(DispatchAssignmentPlan plan) {
        return plan != null && plan.getFallbackStopCount() > 0;
    }

    private static Map<Integer, DeliveryHistoryPreferenceDto> indexPreferences(
            DeliveryHistoryPreferenceBatchResult batch) {
        Map<Integer, DeliveryHistoryPreferenceDto> map = new LinkedHashMap<Integer, DeliveryHistoryPreferenceDto>();
        if (batch == null || batch.getPreferencesByDepFatherId() == null) {
            return map;
        }
        map.putAll(batch.getPreferencesByDepFatherId());
        return map;
    }

    private static BindDecision resolveBindDecision(DeliveryHistoryPreferenceDto pref,
                                                    Integer preferredDriverId,
                                                    Set<Integer> eligibleDriverIds) {
        if (pref == null || preferredDriverId == null) {
            return BindDecision.fallback(DispatchPlanningReason.DISTANCE_FALLBACK,
                    pref != null ? pref.getReason() : DisRouteDeliveryHistoryReason.NO_HISTORY);
        }
        if (!eligibleDriverIds.contains(preferredDriverId)) {
            return BindDecision.fallback(DispatchPlanningReason.PREFERRED_DRIVER_NOT_ELIGIBLE,
                    pref.getReason() != null ? pref.getReason()
                            : DisRouteDeliveryHistoryReason.PREFERRED_DRIVER_NOT_ELIGIBLE);
        }
        String reason = pref.getReason();
        if (DisRouteDeliveryHistoryReason.HISTORY_DOMINANT_DRIVER.equals(reason)
                || DisRouteDeliveryHistoryReason.HISTORY_TIE_BROKEN_BY_RECENCY.equals(reason)) {
            return BindDecision.bind();
        }
        if (DisRouteDeliveryHistoryReason.NO_HISTORY.equals(reason)) {
            return BindDecision.fallback(DispatchPlanningReason.DISTANCE_FALLBACK, reason);
        }
        if (DisRouteDeliveryHistoryReason.INSUFFICIENT_HISTORY.equals(reason)) {
            return BindDecision.fallback(DispatchPlanningReason.DISTANCE_FALLBACK, reason);
        }
        if (DisRouteDeliveryHistoryReason.MULTIPLE_EQUAL_CANDIDATES.equals(reason)) {
            return BindDecision.fallback(DispatchPlanningReason.DISTANCE_FALLBACK, reason);
        }
        if (DisRouteDeliveryHistoryReason.PREFERRED_DRIVER_NOT_ELIGIBLE.equals(reason)) {
            return BindDecision.fallback(DispatchPlanningReason.PREFERRED_DRIVER_NOT_ELIGIBLE, reason);
        }
        if (DisRouteDeliveryHistoryReason.NO_ELIGIBLE_DRIVER.equals(reason)) {
            return BindDecision.fallback(DispatchPlanningReason.DISTANCE_FALLBACK, reason);
        }
        return BindDecision.fallback(DispatchPlanningReason.DISTANCE_FALLBACK,
                reason != null ? reason : "UNKNOWN_HISTORY_REASON");
    }

    private static StopAssignment toStopAssignment(DispatchStrategyContext context,
                                                   NxDisShipmentTaskEntity task,
                                                   DeliveryHistoryPreferenceDto pref,
                                                   Integer driverUserId,
                                                   int stopSeq,
                                                   DispatchStopClass stopClass,
                                                   DispatchPlanningReason planningReason) {
        StopAssignment stop = new StopAssignment();
        stop.setDepFatherId(task.getNxDstDepFatherId());
        stop.setSandboxStopKey(task.getSandboxStopKey() != null
                ? task.getSandboxStopKey()
                : DisRouteSandboxStopKeyUtils.build(task.getNxDstDepFatherId()));
        stop.setAssignedDriverUserId(driverUserId);
        stop.setStopSeq(stopSeq);
        stop.setStopClass(stopClass);
        stop.setFeasibility(DispatchFeasibility.OK);
        NxDepartmentEntity department = null;
        NxDisSandboxDayTimeWindowEntity dayOverride = null;
        if (context != null && task.getNxDstDepFatherId() != null) {
            if (context.getDepartmentByDepId() != null) {
                department = context.getDepartmentByDepId().get(task.getNxDstDepFatherId());
            }
            if (context.getSandboxDayOverrideByDepId() != null) {
                dayOverride = context.getSandboxDayOverrideByDepId().get(task.getNxDstDepFatherId());
            }
        }
        SandboxStopResolvedTimeWindow resolved = DisRouteSandboxStopTimeWindowResolver.resolve(
                null, task, department, dayOverride);
        DisRouteSandboxStopTimeWindowResolver.applyToStopAssignment(stop, resolved);
        stop.setPreferredDriverUserId(driverUserId);
        if (pref != null && pref.getAvgStopSeq() != null) {
            stop.setHistoryAvgStopSeq(pref.getAvgStopSeq().intValue());
        }
        stop.setPlanningReason(planningReason);
        return stop;
    }

    private static void addFallback(List<FallbackStopAssignment> fallbackStops,
                                    NxDisShipmentTaskEntity task,
                                    DispatchPlanningReason planningReason,
                                    String historyReason) {
        FallbackStopAssignment fallback = new FallbackStopAssignment();
        fallback.setDepFatherId(task.getNxDstDepFatherId());
        fallback.setSandboxStopKey(task.getSandboxStopKey() != null
                ? task.getSandboxStopKey()
                : DisRouteSandboxStopKeyUtils.build(task.getNxDstDepFatherId()));
        fallback.setPlanningReason(planningReason);
        fallback.setHistoryReason(historyReason);
        fallbackStops.add(fallback);
    }

    private static int incrementSeq(Map<Integer, Integer> nextSeqByDriver, Integer driverUserId) {
        Integer current = nextSeqByDriver.get(driverUserId);
        int next = current != null ? current + 1 : 1;
        nextSeqByDriver.put(driverUserId, next);
        return next;
    }

    private static int nextSeq(int seq) {
        return seq;
    }

    private static int countBoundStops(Map<Integer, DriverRoutePlan> routeByDriver) {
        int count = 0;
        for (DriverRoutePlan route : routeByDriver.values()) {
            if (route.getStops() != null) {
                count += route.getStops().size();
            }
        }
        return count;
    }

    static Map<Integer, String> buildDriverNameIndex(List<NxDistributerUserEntity> drivers) {
        Map<Integer, String> map = new HashMap<Integer, String>();
        if (drivers == null) {
            return map;
        }
        for (NxDistributerUserEntity driver : drivers) {
            if (driver == null || driver.getNxDistributerUserId() == null) {
                continue;
            }
            map.put(driver.getNxDistributerUserId(), driver.getNxDiuWxNickName());
        }
        return map;
    }

    static Set<Integer> eligibleDriverIds(List<NxDistributerUserEntity> drivers) {
        Set<Integer> ids = new HashSet<Integer>();
        if (drivers == null) {
            return ids;
        }
        for (NxDistributerUserEntity driver : drivers) {
            if (driver != null && driver.getNxDistributerUserId() != null) {
                ids.add(driver.getNxDistributerUserId());
            }
        }
        return ids;
    }

    private static final class BindDecision {
        private final boolean bind;
        private final DispatchPlanningReason fallbackReason;
        private final String historyReason;

        private BindDecision(boolean bind,
                             DispatchPlanningReason fallbackReason,
                             String historyReason) {
            this.bind = bind;
            this.fallbackReason = fallbackReason;
            this.historyReason = historyReason;
        }

        static BindDecision bind() {
            return new BindDecision(true, null, null);
        }

        static BindDecision fallback(DispatchPlanningReason reason, String historyReason) {
            return new BindDecision(false, reason, historyReason);
        }
    }
}
