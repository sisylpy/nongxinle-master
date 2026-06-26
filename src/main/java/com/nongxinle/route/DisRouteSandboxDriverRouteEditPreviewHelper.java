package com.nongxinle.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.service.impl.DisRouteSandboxLegMetricsHelper;
import com.nongxinle.service.impl.DisRouteSandboxSchedulePreviewHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate;
import static com.nongxinle.route.RouteCoordinateUtils.toPoint;

/** 司机路线编辑试算（纯内存，不写 DB）。 */
@Component
public class DisRouteSandboxDriverRouteEditPreviewHelper {

    @Autowired
    private DisRouteSandboxLegMetricsHelper disRouteSandboxLegMetricsHelper;
    @Autowired
    private DisRouteSandboxSchedulePreviewHelper disRouteSandboxSchedulePreviewHelper;

    public DriverRoutePreviewResult previewRoute(List<NxDisRouteStopEntity> stops,
                                                 NxDisRoutePlanEntity plan,
                                                 String routeDate) throws IOException {
        DriverRoutePreviewResult result = new DriverRoutePreviewResult();
        if (stops == null || stops.isEmpty()) {
            return result;
        }
        List<NxDisRouteStopEntity> trialStops = cloneStops(stops);
        resequence(trialStops);
        NxDisDriverRouteEntity route = new NxDisDriverRouteEntity();
        route.setStops(trialStops);
        GeoPoint depot = resolveDepot(plan);
        if (allStopsHaveLegMetrics(trialStops)) {
            applyRouteTotalsFromExistingLegs(route, result);
            disRouteSandboxSchedulePreviewHelper.applySchedulePreview(plan, route, routeDate);
        } else {
            disRouteSandboxLegMetricsHelper.applyToDriverRoute(depot, route);
            disRouteSandboxSchedulePreviewHelper.applySchedulePreview(plan, route, routeDate);
        }
        result.totalDistanceM = route.getNxDdrTotalDistanceM();
        result.totalDurationS = route.getNxDdrTotalDurationS();
        result.stops = trialStops;
        Date serverNow = new Date();
        for (NxDisRouteStopEntity stop : trialStops) {
            if (stop == null) {
                continue;
            }
            StopPreview preview = new StopPreview();
            preview.stop = stop;
            preview.departmentId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            preview.plannedArrivalLabel = DisRouteSandboxTodayStopScheduleHelper.resolvePlannedArrivalLabel(
                    stop, serverNow);
            preview.plannedDepartureLabel = DisRouteSandboxTodayStopScheduleHelper.resolvePlannedDepartureLabel(
                    stop, serverNow);
            preview.windowLabel = DisRouteSandboxTodayStopScheduleHelper.resolveCustomerWindowLabel(stop, serverNow);
            preview.lateMinutes = stop.getNxDrsLateMinutes() != null ? stop.getNxDrsLateMinutes() : 0;
            result.stopPreviews.add(preview);
        }
        return result;
    }

    public static List<NxDisRouteStopEntity> cloneStops(List<NxDisRouteStopEntity> source) {
        List<NxDisRouteStopEntity> copy = new ArrayList<NxDisRouteStopEntity>();
        if (source == null) {
            return copy;
        }
        for (NxDisRouteStopEntity stop : source) {
            if (stop != null) {
                copy.add(cloneStop(stop));
            }
        }
        return copy;
    }

    public static NxDisRouteStopEntity cloneStop(NxDisRouteStopEntity source) {
        if (source == null) {
            return null;
        }
        NxDisRouteStopEntity stop = new NxDisRouteStopEntity();
        stop.setNxDrsId(source.getNxDrsId());
        stop.setNxDrsStopSeq(source.getNxDrsStopSeq());
        stop.setNxDrsDepartmentId(source.getNxDrsDepartmentId());
        stop.setNxDrsDepartmentName(source.getNxDrsDepartmentName());
        stop.setNxDrsLat(source.getNxDrsLat());
        stop.setNxDrsLng(source.getNxDrsLng());
        stop.setNxDrsLegDistanceM(source.getNxDrsLegDistanceM());
        stop.setNxDrsLegDurationS(source.getNxDrsLegDurationS());
        stop.setNxDrsPlannedArrivalAt(source.getNxDrsPlannedArrivalAt());
        stop.setNxDrsPlannedDepartureAt(source.getNxDrsPlannedDepartureAt());
        stop.setNxDrsWaitMinutes(source.getNxDrsWaitMinutes());
        stop.setNxDrsLateMinutes(source.getNxDrsLateMinutes());
        stop.setNxDrsTimeWindowStatus(source.getNxDrsTimeWindowStatus());
        stop.setSuggestedDriverUserId(source.getSuggestedDriverUserId());
        if (source.getShipmentTask() != null) {
            stop.setShipmentTask(cloneTask(source.getShipmentTask()));
        }
        return stop;
    }

    private static NxDisShipmentTaskEntity cloneTask(NxDisShipmentTaskEntity source) {
        NxDisShipmentTaskEntity task = new NxDisShipmentTaskEntity();
        task.setNxDstId(source.getNxDstId());
        task.setNxDstDepFatherId(source.getNxDstDepFatherId());
        task.setNxDstDepName(source.getNxDstDepName());
        task.setNxDstLat(source.getNxDstLat());
        task.setNxDstLng(source.getNxDstLng());
        task.setNxDstAddress(source.getNxDstAddress());
        task.setNxDstAssignedDriverUserId(source.getNxDstAssignedDriverUserId());
        task.setNxDstSuggestedDriverUserId(source.getNxDstSuggestedDriverUserId());
        task.setNxDstRouteSeq(source.getNxDstRouteSeq());
        task.setNxDstManualStopSeq(source.getNxDstManualStopSeq());
        task.setNxDstStatus(source.getNxDstStatus());
        task.setNxDstPlannedArrivalAt(source.getNxDstPlannedArrivalAt());
        task.setNxDstPlannedDepartureAt(source.getNxDstPlannedDepartureAt());
        task.setItems(source.getItems());
        return task;
    }

    public static void resequence(List<NxDisRouteStopEntity> stops) {
        if (stops == null) {
            return;
        }
        int seq = 1;
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            stop.setNxDrsStopSeq(seq);
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task != null) {
                task.setNxDstRouteSeq(seq);
                task.setNxDstManualStopSeq(seq);
            }
            seq++;
        }
    }

    private static boolean allStopsHaveLegMetrics(List<NxDisRouteStopEntity> stops) {
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            if (stop.getNxDrsLegDistanceM() == null || stop.getNxDrsLegDurationS() == null) {
                return false;
            }
        }
        return true;
    }

    private static void applyRouteTotalsFromExistingLegs(NxDisDriverRouteEntity route,
                                                       DriverRoutePreviewResult snapshot) {
        long distance = 0L;
        long duration = 0L;
        if (route.getStops() != null) {
            for (NxDisRouteStopEntity stop : route.getStops()) {
                if (stop == null) {
                    continue;
                }
                if (stop.getNxDrsLegDistanceM() != null) {
                    distance += stop.getNxDrsLegDistanceM();
                }
                if (stop.getNxDrsLegDurationS() != null) {
                    duration += stop.getNxDrsLegDurationS();
                }
            }
        }
        route.setNxDdrTotalDistanceM(distance);
        route.setNxDdrTotalDurationS(duration);
        snapshot.totalDistanceM = distance;
        snapshot.totalDurationS = duration;
    }

    private static GeoPoint resolveDepot(NxDisRoutePlanEntity plan) {
        if (plan != null && isValidCoordinate(plan.getNxDrpDepotLat(), plan.getNxDrpDepotLng())) {
            return toPoint(plan.getNxDrpDepotLat(), plan.getNxDrpDepotLng());
        }
        return null;
    }

    public static final class DriverRoutePreviewResult {
        public long totalDistanceM;
        public long totalDurationS;
        public List<NxDisRouteStopEntity> stops = new ArrayList<NxDisRouteStopEntity>();
        public List<StopPreview> stopPreviews = new ArrayList<StopPreview>();
    }

    public static final class StopPreview {
        public NxDisRouteStopEntity stop;
        public Integer departmentId;
        public String plannedArrivalLabel;
        public String plannedDepartureLabel;
        public String windowLabel;
        public int lateMinutes;
    }
}
