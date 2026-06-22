package com.nongxinle.route;

import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.Date;

/** 站点排程展示字段 — 与 RouteDispatchReadModelAssembler 口径一致。 */
public final class DisRouteSandboxTodayStopScheduleHelper {

    private DisRouteSandboxTodayStopScheduleHelper() {
    }

    public static void copyScheduleFields(NxDisRouteStopEntity from, NxDisRouteStopEntity to) {
        if (from == null || to == null) {
            return;
        }
        if (from.getPlannedArrivalLabel() != null) {
            to.setPlannedArrivalLabel(from.getPlannedArrivalLabel());
        }
        if (from.getPlannedDepartureLabel() != null) {
            to.setPlannedDepartureLabel(from.getPlannedDepartureLabel());
        }
        if (from.getCustomerWindowLabel() != null) {
            to.setCustomerWindowLabel(from.getCustomerWindowLabel());
        }
        if (from.getServiceDurationLabel() != null) {
            to.setServiceDurationLabel(from.getServiceDurationLabel());
        }
        if (from.getFastestArrivalLabel() != null) {
            to.setFastestArrivalLabel(from.getFastestArrivalLabel());
        }
        if (from.getNxDrsPlannedArrivalAt() != null) {
            to.setNxDrsPlannedArrivalAt(from.getNxDrsPlannedArrivalAt());
        }
        if (from.getNxDrsPlannedDepartureAt() != null) {
            to.setNxDrsPlannedDepartureAt(from.getNxDrsPlannedDepartureAt());
        }
        if (from.getNxDrsPlannedServiceStartAt() != null) {
            to.setNxDrsPlannedServiceStartAt(from.getNxDrsPlannedServiceStartAt());
        }
        if (from.getNxDrsServiceMinutes() != null) {
            to.setNxDrsServiceMinutes(from.getNxDrsServiceMinutes());
        }
        if (from.getScheduleMode() != null) {
            to.setScheduleMode(from.getScheduleMode());
        }
    }

    public static String resolvePlannedArrivalLabel(NxDisRouteStopEntity stop, Date serverNow) {
        if (stop == null) {
            return null;
        }
        if (stop.getPlannedArrivalLabel() != null && !stop.getPlannedArrivalLabel().trim().isEmpty()) {
            return stop.getPlannedArrivalLabel().trim();
        }
        Date arrivalAt = stop.getNxDrsPlannedArrivalAt();
        if (arrivalAt == null && stop.getShipmentTask() != null) {
            arrivalAt = stop.getShipmentTask().getNxDstPlannedArrivalAt();
        }
        if (arrivalAt != null && serverNow != null) {
            if (DisRouteSandboxScheduleMode.ADHOC_NOW.equals(stop.getScheduleMode())) {
                return DisRouteSandboxScheduleLabelHelper.formatAdhocPlannedArrivalLabel(arrivalAt, serverNow);
            }
            return DisRouteTemporalHelper.formatDateTimeLabel(arrivalAt, serverNow);
        }
        return null;
    }

    public static String resolvePlannedDepartureLabel(NxDisRouteStopEntity stop, Date serverNow) {
        if (stop == null) {
            return null;
        }
        if (stop.getPlannedDepartureLabel() != null && !stop.getPlannedDepartureLabel().trim().isEmpty()) {
            return stop.getPlannedDepartureLabel().trim();
        }
        Date departureAt = stop.getNxDrsPlannedDepartureAt();
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (departureAt == null && task != null) {
            departureAt = task.getNxDstPlannedDepartureAt();
        }
        if (departureAt != null && serverNow != null) {
            return DisRouteTemporalHelper.formatDateTimeLabel(departureAt, serverNow);
        }
        return null;
    }

    public static String resolveCustomerWindowLabel(NxDisRouteStopEntity stop, Date serverNow) {
        if (stop == null) {
            return null;
        }
        if (stop.getCustomerWindowLabel() != null && !stop.getCustomerWindowLabel().trim().isEmpty()) {
            return stop.getCustomerWindowLabel().trim();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task == null) {
            return null;
        }
        Integer earliest = stop.getNxDrsEarliestDeliveryTimeS() != null
                ? stop.getNxDrsEarliestDeliveryTimeS() : task.getNxDstEarliestDeliveryTimeS();
        Integer latest = stop.getNxDrsLatestDeliveryTimeS() != null
                ? stop.getNxDrsLatestDeliveryTimeS() : task.getNxDstLatestDeliveryTimeS();
        if (earliest == null && latest == null) {
            return null;
        }
        Date anchor = serverNow != null ? serverNow : new Date();
        return DisRouteSandboxScheduleLabelHelper.formatCustomerWindowLabel(
                task.getNxDstRouteDate(), earliest, latest, anchor);
    }

    public static String resolveServiceDurationLabel(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getServiceDurationLabel() != null && !stop.getServiceDurationLabel().trim().isEmpty()) {
            return stop.getServiceDurationLabel().trim();
        }
        if (stop.getNxDrsServiceMinutes() != null && stop.getNxDrsServiceMinutes() > 0) {
            return "服务 " + stop.getNxDrsServiceMinutes() + " 分钟";
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getNxDstServiceMinutes() != null && task.getNxDstServiceMinutes() > 0) {
            return "服务 " + task.getNxDstServiceMinutes() + " 分钟";
        }
        return null;
    }
}
