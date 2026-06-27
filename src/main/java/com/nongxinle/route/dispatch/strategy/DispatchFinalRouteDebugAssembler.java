package com.nongxinle.route.dispatch.strategy;

import com.nongxinle.route.DisRouteStopTimeWindowStatus;
import com.nongxinle.route.VisibleDriverRouteSnapshot;
import com.nongxinle.route.VisibleDriverRouteStopSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * PR-2c debug：仅序列化 {@link VisibleDriverRouteSnapshot}，不二次 preview ETA。
 */
public final class DispatchFinalRouteDebugAssembler {

    private DispatchFinalRouteDebugAssembler() {
    }

    public static List<FinalSequencedDriverRouteDebug> buildFromVisibleRouteSnapshots(
            List<VisibleDriverRouteSnapshot> routes) {
        List<FinalSequencedDriverRouteDebug> debugRoutes = new ArrayList<FinalSequencedDriverRouteDebug>();
        if (routes == null || routes.isEmpty()) {
            return debugRoutes;
        }
        for (VisibleDriverRouteSnapshot route : routes) {
            if (route == null || !com.nongxinle.route.VisibleDriverRouteSnapshotBuilder.SECTION_SUGGESTED
                    .equals(route.getSectionKey())) {
                continue;
            }
            FinalSequencedDriverRouteDebug routeDebug = toRouteDebug(route);
            if (routeDebug != null && routeDebug.getStops() != null && !routeDebug.getStops().isEmpty()) {
                debugRoutes.add(routeDebug);
            }
        }
        return debugRoutes;
    }

    private static FinalSequencedDriverRouteDebug toRouteDebug(VisibleDriverRouteSnapshot route) {
        if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
            return null;
        }
        FinalSequencedDriverRouteDebug routeDebug = new FinalSequencedDriverRouteDebug();
        routeDebug.setDriverUserId(route.getDriverUserId());
        routeDebug.setDriverName(route.getDriverName());
        routeDebug.setSuggestedDepartTimeS(route.getSuggestedDepartTimeS());
        routeDebug.setPlannedDepartTimeS(route.getPlannedDepartTimeS());
        routeDebug.setSuggestedDepartReason(route.getSuggestedDepartReason());
        routeDebug.setSuggestedDepartReasonLabel(route.getSuggestedDepartReasonLabel());

        List<FinalSequencedStopDebug> stopDebugs = new ArrayList<FinalSequencedStopDebug>();
        for (VisibleDriverRouteStopSnapshot stop : route.getStops()) {
            if (stop == null) {
                continue;
            }
            stopDebugs.add(toStopDebug(stop));
        }
        routeDebug.setStops(stopDebugs);
        return routeDebug;
    }

    private static FinalSequencedStopDebug toStopDebug(VisibleDriverRouteStopSnapshot stop) {
        FinalSequencedStopDebug debug = new FinalSequencedStopDebug();
        debug.setSeq(stop.getVisibleSeq());
        debug.setDepFatherId(stop.getDepFatherId());
        debug.setCustomerName(stop.getCustomerName());
        debug.setEarliestDeliveryTimeS(stop.getEarliestDeliveryTimeS());
        debug.setLatestDeliveryTimeS(stop.getLatestDeliveryTimeS());
        debug.setProjectedArrivalTimeS(stop.getProjectedArrivalTimeS());
        debug.setTimeWindowStatus(mapPageTimeWindowToDebug(stop.getTimeWindowStatus()));
        debug.setLateMinutes(stop.getLateMinutes());
        debug.setWaitMinutes(stop.getWaitMinutes());
        debug.setWarningLabel(stop.getWarningLabel());
        debug.setWindowSource(stop.getWindowSource());
        debug.setSequenceBucket(stop.getSequenceBucket());
        debug.setSequenceReason(stop.getSequenceReason());
        debug.setSequenceSortKey(stop.getSequenceSortKey());
        return debug;
    }

    private static String mapPageTimeWindowToDebug(String pageStatus) {
        if (pageStatus == null) {
            return null;
        }
        if (DisRouteStopTimeWindowStatus.OK.equals(pageStatus)
                || DisRouteStopTimeWindowStatus.EARLY_WAIT.equals(pageStatus)) {
            return DispatchTimeWindowDebugStatus.ON_TIME.name();
        }
        if (DisRouteStopTimeWindowStatus.LATE.equals(pageStatus)) {
            return DispatchTimeWindowDebugStatus.LATE.name();
        }
        if (DisRouteStopTimeWindowStatus.SUPPLEMENT_AFTER_WINDOW.equals(pageStatus)) {
            return DispatchTimeWindowDebugStatus.WINDOW_MISSED.name();
        }
        if (DisRouteStopTimeWindowStatus.NO_WINDOW.equals(pageStatus)) {
            return DispatchTimeWindowDebugStatus.NO_WINDOW.name();
        }
        return pageStatus;
    }
}
