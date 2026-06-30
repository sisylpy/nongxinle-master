package com.nongxinle.route;

import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.Date;

/** 站点排程展示字段 — 与 RouteDispatchReadModelAssembler 口径一致。 */
public final class DisRouteSandboxTodayStopScheduleHelper {

    private DisRouteSandboxTodayStopScheduleHelper() {
    }

    /**
     * 正式 Today sandbox：仅同步窗口/服务展示标签，不复制 seq / leg / planned Date。
     * visible seq / leg / ETA 由 {@link com.nongxinle.todaydispatch.TodayDispatchComputeService} 排程管线写入。
     */
    public static void copySandboxScheduleLabelFields(NxDisRouteStopEntity from, NxDisRouteStopEntity to) {
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
        if (from.getNxDrsServiceMinutes() != null) {
            to.setNxDrsServiceMinutes(from.getNxDrsServiceMinutes());
        }
        if (from.getResolvedEarliestDeliveryTimeS() != null) {
            to.setResolvedEarliestDeliveryTimeS(from.getResolvedEarliestDeliveryTimeS());
        }
        if (from.getResolvedLatestDeliveryTimeS() != null) {
            to.setResolvedLatestDeliveryTimeS(from.getResolvedLatestDeliveryTimeS());
        }
        if (from.getResolvedWindowSource() != null) {
            to.setResolvedWindowSource(from.getResolvedWindowSource());
        }
        if (from.getNxDrsEarliestDeliveryTimeS() != null) {
            to.setNxDrsEarliestDeliveryTimeS(from.getNxDrsEarliestDeliveryTimeS());
        }
        if (from.getNxDrsLatestDeliveryTimeS() != null) {
            to.setNxDrsLatestDeliveryTimeS(from.getNxDrsLatestDeliveryTimeS());
        }
        if (from.getScheduleMode() != null) {
            to.setScheduleMode(from.getScheduleMode());
        }
        if (from.getNxDrsTimeWindowStatus() != null) {
            to.setNxDrsTimeWindowStatus(from.getNxDrsTimeWindowStatus());
        }
        if (from.getNxDrsTimeWindowStatusLabel() != null) {
            to.setNxDrsTimeWindowStatusLabel(from.getNxDrsTimeWindowStatusLabel());
        }
        if (from.getNxDrsLateMinutes() != null) {
            to.setNxDrsLateMinutes(from.getNxDrsLateMinutes());
        }
        if (from.getNxDrsWaitMinutes() != null) {
            to.setNxDrsWaitMinutes(from.getNxDrsWaitMinutes());
        }
    }

    /**
     * 装车 / 配送 / 司机终端：从 plan route stop 完整同步 seq、leg、planned Date 及展示字段。
     * 禁止用于正式 Today suggested route 主链。
     */
    public static void copyFullScheduleFieldsForExecutionView(NxDisRouteStopEntity from, NxDisRouteStopEntity to) {
        if (from == null || to == null) {
            return;
        }
        copySandboxScheduleLabelFields(from, to);

        if (from.getNxDrsStopSeq() != null) {
            to.setNxDrsStopSeq(from.getNxDrsStopSeq());
        }
        copyLegMetrics(from, to);

        if (from.getNxDrsPlannedArrivalAt() != null) {
            to.setNxDrsPlannedArrivalAt(from.getNxDrsPlannedArrivalAt());
        }
        if (from.getNxDrsPlannedDepartureAt() != null) {
            to.setNxDrsPlannedDepartureAt(from.getNxDrsPlannedDepartureAt());
        }
        if (from.getNxDrsPlannedServiceStartAt() != null) {
            to.setNxDrsPlannedServiceStartAt(from.getNxDrsPlannedServiceStartAt());
        }

        mirrorScheduleToTask(from, to);
    }

    private static void copyLegMetrics(NxDisRouteStopEntity from, NxDisRouteStopEntity to) {
        if (from.getNxDrsLegDistanceM() != null) {
            to.setNxDrsLegDistanceM(from.getNxDrsLegDistanceM());
        }
        if (from.getNxDrsLegDurationS() != null) {
            to.setNxDrsLegDurationS(from.getNxDrsLegDurationS());
        }
        if (from.getLegDistanceProvider() != null) {
            to.setLegDistanceProvider(from.getLegDistanceProvider());
        }
        if (from.getLegDistanceType() != null) {
            to.setLegDistanceType(from.getLegDistanceType());
        }
    }

    private static void mirrorScheduleToTask(NxDisRouteStopEntity from, NxDisRouteStopEntity to) {
        NxDisShipmentTaskEntity toTask = to.getShipmentTask();
        if (toTask == null) {
            return;
        }
        NxDisShipmentTaskEntity fromTask = from.getShipmentTask();

        Integer routeSeq = from.getNxDrsStopSeq();
        if (routeSeq == null && fromTask != null) {
            routeSeq = fromTask.getNxDstRouteSeq();
        }
        if (routeSeq != null) {
            toTask.setNxDstRouteSeq(routeSeq);
        }
        if (fromTask != null && fromTask.getNxDstManualStopSeq() != null) {
            toTask.setNxDstManualStopSeq(fromTask.getNxDstManualStopSeq());
        }

        Long legDistanceM = from.getNxDrsLegDistanceM();
        Long legDurationS = from.getNxDrsLegDurationS();
        String legProvider = from.getLegDistanceProvider();
        String legType = from.getLegDistanceType();
        if (fromTask != null) {
            if (legDistanceM == null) {
                legDistanceM = fromTask.getNxDstLegDistanceM();
            }
            if (legDurationS == null) {
                legDurationS = fromTask.getNxDstLegDurationS();
            }
            if (legProvider == null) {
                legProvider = fromTask.getLegDistanceProvider();
            }
            if (legType == null) {
                legType = fromTask.getLegDistanceType();
            }
        }
        if (legDistanceM != null) {
            toTask.setNxDstLegDistanceM(legDistanceM);
        }
        if (legDurationS != null) {
            toTask.setNxDstLegDurationS(legDurationS);
        }
        if (legProvider != null) {
            toTask.setLegDistanceProvider(legProvider);
        }
        if (legType != null) {
            toTask.setLegDistanceType(legType);
        }

        Date plannedArrivalAt = from.getNxDrsPlannedArrivalAt();
        if (plannedArrivalAt == null && fromTask != null) {
            plannedArrivalAt = fromTask.getNxDstPlannedArrivalAt();
        }
        if (plannedArrivalAt != null) {
            toTask.setNxDstPlannedArrivalAt(plannedArrivalAt);
        }

        Date plannedDepartureAt = from.getNxDrsPlannedDepartureAt();
        if (plannedDepartureAt == null && fromTask != null) {
            plannedDepartureAt = fromTask.getNxDstPlannedDepartureAt();
        }
        if (plannedDepartureAt != null) {
            toTask.setNxDstPlannedDepartureAt(plannedDepartureAt);
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
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        Integer earliest = DisRouteSandboxStopTimeWindowResolver.readResolvedEarliest(stop);
        Integer latest = DisRouteSandboxStopTimeWindowResolver.readResolvedLatest(stop);
        if (DisRouteSandboxStopTimeWindowResolver.isTodayOverride(stop)) {
            String timeRange = DisRouteSandboxScheduleLabelHelper.formatPlainTimeRange(earliest, latest);
            if (timeRange != null) {
                return applyTodayOverridePrefix(timeRange);
            }
        }
        if (stop.getCustomerWindowLabel() != null && !stop.getCustomerWindowLabel().trim().isEmpty()) {
            return stop.getCustomerWindowLabel().trim();
        }
        if (task == null) {
            return null;
        }
        if (earliest == null && latest == null) {
            return null;
        }
        Date anchor = serverNow != null ? serverNow : new Date();
        return DisRouteSandboxScheduleLabelHelper.formatCustomerWindowLabel(
                task.getNxDstRouteDate(), earliest, latest, anchor);
    }

    public static WindowRequirementView buildWindowRequirementView(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        Integer earliest = DisRouteSandboxStopTimeWindowResolver.readResolvedEarliest(stop);
        Integer latest = DisRouteSandboxStopTimeWindowResolver.readResolvedLatest(stop);
        String label = DisRouteSandboxScheduleLabelHelper.formatPlainTimeRange(earliest, latest);
        if (label == null || label.trim().isEmpty()) {
            return null;
        }
        return new WindowRequirementView(label.trim(),
                DisRouteSandboxStopTimeWindowResolver.isTodayOverride(stop));
    }

    public static final class WindowRequirementView {
        private final String label;
        private final boolean modified;

        public WindowRequirementView(String label, boolean modified) {
            this.label = label;
            this.modified = modified;
        }

        public String getLabel() {
            return label;
        }

        public boolean isModified() {
            return modified;
        }
    }

    public static String applyTodayOverridePrefix(String label) {
        if (label == null || label.trim().isEmpty()) {
            return label;
        }
        String trimmed = label.trim();
        if (trimmed.startsWith("今日调整 ")) {
            return trimmed;
        }
        if (trimmed.startsWith("常规窗口 ")) {
            return "今日调整 " + trimmed.substring("常规窗口 ".length());
        }
        return "今日调整 " + trimmed;
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
