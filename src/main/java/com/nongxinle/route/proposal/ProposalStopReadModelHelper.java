package com.nongxinle.route.proposal;

import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.route.DisRouteSandboxStopTimeWindowResolver;
import com.nongxinle.route.dispatch.strategy.DispatchAssignmentPlan;
import com.nongxinle.route.dispatch.strategy.DriverRoutePlan;
import com.nongxinle.route.dispatch.strategy.StopAssignment;

import java.util.HashMap;
import java.util.Map;

/** proposalPlan debug 读模型：时间窗字段与 R_routeSequence / pageViewModel 同源。 */
final class ProposalStopReadModelHelper {

    private ProposalStopReadModelHelper() {
    }

    static Map<Integer, StopAssignment> indexPlanStops(DispatchAssignmentPlan assignmentPlan) {
        Map<Integer, StopAssignment> byDep = new HashMap<Integer, StopAssignment>();
        if (assignmentPlan == null || assignmentPlan.getDriverRoutes() == null) {
            return byDep;
        }
        for (DriverRoutePlan route : assignmentPlan.getDriverRoutes()) {
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

    static Integer resolveEarliestDeliveryTimeS(NxDisRouteStopEntity entity, StopAssignment planStop) {
        Integer resolved = DisRouteSandboxStopTimeWindowResolver.readResolvedEarliest(entity);
        if (resolved != null) {
            return resolved;
        }
        return planStop != null ? planStop.getEarliestDeliveryTimeS() : null;
    }

    static Integer resolveLatestDeliveryTimeS(NxDisRouteStopEntity entity, StopAssignment planStop) {
        Integer resolved = DisRouteSandboxStopTimeWindowResolver.readResolvedLatest(entity);
        if (resolved != null) {
            return resolved;
        }
        return planStop != null ? planStop.getLatestDeliveryTimeS() : null;
    }

    static Boolean resolveTimeWindowOverrideFlag(NxDisRouteStopEntity entity, StopAssignment planStop) {
        if (entity != null && DisRouteSandboxStopTimeWindowResolver.isTodayOverride(entity)) {
            return Boolean.TRUE;
        }
        if (entity != null && entity.getNxDrsTimeWindowOverrideFlag() != null) {
            return entity.getNxDrsTimeWindowOverrideFlag() == 1;
        }
        return planStop != null ? planStop.getTimeWindowOverrideFlag() : null;
    }
}
