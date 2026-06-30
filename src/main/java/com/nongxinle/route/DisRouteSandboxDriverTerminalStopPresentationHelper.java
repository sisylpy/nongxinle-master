package com.nongxinle.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;

/**
 * 司机终端门店卡展示：按配送进度区分字段文案、时间与色调。
 *
 * <p>配送页状态：
 * <ul>
 *   <li>已完成 — 本段路程 + 出发时间 + 送达时间（灰）</li>
 *   <li>配送中 — 到达/装卸/离开单行（当前站，可操作）</li>
 *   <li>待配送 — 本段路程 + 预计到达/离开（后续站，灰）</li>
 *   <li>配送异常 — 预计到达 + 异常时间（橙）</li>
 * </ul>
 *
 * <p>装车页状态：已分派 — 计划路程 + 预计到达/离开。
 */
public final class DisRouteSandboxDriverTerminalStopPresentationHelper {

    private static final String TONE_DONE = "stop-state-done";
    private static final String TONE_ACTIVE = "stop-state-active";
    private static final String TONE_PENDING = "stop-state-muted";
    private static final String TONE_WARN = "stop-state-warn";
    private static final String TONE_ASSIGNED = "stop-state-assigned";

    private static final String CARD_DONE = "timeline-stop-done";
    private static final String CARD_ACTIVE = "timeline-stop-active";
    private static final String CARD_PENDING = "timeline-stop-pending";
    private static final String CARD_WARN = "timeline-stop-warn";
    private static final String CARD_ASSIGNED = "timeline-stop-assigned";

    private DisRouteSandboxDriverTerminalStopPresentationHelper() {
    }

    public static int resolveCurrentDeliveryStopIndex(List<NxDisRouteStopEntity> orderedStops,
                                                    NxDisDriverRouteEntity route) {
        if (orderedStops == null) {
            return -1;
        }
        for (int i = 0; i < orderedStops.size(); i++) {
            NxDisRouteStopEntity stop = orderedStops.get(i);
            NxDisShipmentTaskEntity task = stop != null ? stop.getShipmentTask() : null;
            if (isHistoricalCompletedTask(task, route)) {
                continue;
            }
            if (task == null || task.getNxDstStatus() == null) {
                continue;
            }
            String status = task.getNxDstStatus().trim().toUpperCase();
            if (EXCEPTION.equals(status)) {
                continue;
            }
            return i;
        }
        return -1;
    }

    public static String resolveDeliveryProgressState(NxDisShipmentTaskEntity task,
                                                      int stopNodeIndex,
                                                      int currentStopIndex,
                                                      NxDisDriverRouteEntity route) {
        if (isHistoricalCompletedTask(task, route)) {
            return "DELIVERED";
        }
        if (task == null || task.getNxDstStatus() == null) {
            return "UPCOMING";
        }
        String status = task.getNxDstStatus().trim().toUpperCase();
        if (DELIVERED.equals(status)) {
            if (route != null && route.getNxDdrActualDepartAt() != null
                    && task.getNxDstDeliveredAt() != null
                    && task.getNxDstDeliveredAt().before(route.getNxDdrActualDepartAt())) {
                return "CURRENT";
            }
            return "DELIVERED";
        }
        if (EXCEPTION.equals(status)) {
            return "EXCEPTION";
        }
        if (stopNodeIndex == currentStopIndex) {
            return "CURRENT";
        }
        return "UPCOMING";
    }

    private static boolean isHistoricalCompletedTask(NxDisShipmentTaskEntity task,
                                                     NxDisDriverRouteEntity route) {
        return DisRouteDriverDepartPolicy.isHistoricalCompletedTask(task, route);
    }

    public static void applyDeliveryStopPresentation(Map<String, Object> node,
                                                     NxDisRouteStopEntity stop,
                                                     NxDisShipmentTaskEntity task,
                                                     int stopNodeIndex,
                                                     List<NxDisRouteStopEntity> orderedStops,
                                                     NxDisDriverRouteEntity route,
                                                     int currentStopIndex,
                                                     Date serverNow) {
        if (node == null) {
            return;
        }
        node.put("distanceLabel", "本段路程");

        if (isHistoricalCompletedTask(task, route)) {
            applyDeliveredPresentation(node, task, stopNodeIndex, orderedStops, route);
            return;
        }

        String status = task != null ? task.getNxDstStatus() : null;
        boolean delivered = DELIVERED.equals(status);
        boolean exception = EXCEPTION.equals(status);
        boolean current = currentStopIndex >= 0 && stopNodeIndex == currentStopIndex;
        node.put("deliveryProgressState", resolveDeliveryProgressState(task, stopNodeIndex, currentStopIndex, route));

        if (delivered) {
            applyCurrentDeliveryPresentation(node, stop, task, stopNodeIndex, orderedStops, route, serverNow);
            return;
        }
        if (exception) {
            applyExceptionPresentation(node, task, stop, serverNow);
            return;
        }
        if (current) {
            applyCurrentDeliveryPresentation(node, stop, task, stopNodeIndex, orderedStops, route, serverNow);
            return;
        }
        applyUpcomingDeliveryPresentation(node, stop, task, serverNow);
    }

    public static void applyLoadingStopPresentation(Map<String, Object> node,
                                                    NxDisShipmentTaskEntity task) {
        if (node == null) {
            return;
        }
        node.put("distanceLabel", "计划路程");
        node.put("arrivalFieldLabel", "预计到达");
        node.put("departureFieldLabel", "预计离开");
        node.put("statusLabel", resolveLoadingStatusLabel(task));
        node.put("statusToneClass", TONE_ASSIGNED);
        node.put("cardToneClass", CARD_ASSIGNED);
        if (node.get("nextLegHint") != null) {
            node.put("nextLegHintLabel", "下一站 · ");
        }
    }

    /** 司机装车/配送页统一门店卡：预计到达 · 卸货时长 · 预计离开 + 窗口要求角标。 */
    public static void applyDriverTerminalUnifiedStopCard(Map<String, Object> node,
                                                          NxDisRouteStopEntity stop,
                                                          NxDisShipmentTaskEntity task,
                                                          boolean loadingPhase,
                                                          int stopNodeIndex,
                                                          int currentStopIndex,
                                                          NxDisDriverRouteEntity route,
                                                          List<NxDisRouteStopEntity> orderedStops,
                                                          Date serverNow) {
        if (node == null) {
            return;
        }
        clearLegacyStopPresentationFields(node);
        ensureServiceDurationLabel(node, stop, task);
        node.put("showComplete", Boolean.FALSE);
        node.put("showException", Boolean.FALSE);
        node.put("showNav", Boolean.FALSE);
        node.put("stopDone", Boolean.FALSE);

        if (loadingPhase) {
            node.put("statusLabel", resolveLoadingStatusLabel(task));
            node.put("statusToneClass", TONE_ASSIGNED);
            node.put("cardToneClass", CARD_ASSIGNED);
            return;
        }

        String progress = resolveDeliveryProgressState(task, stopNodeIndex, currentStopIndex, route);
        node.put("deliveryProgressState", progress);
        if ("DELIVERED".equals(progress)) {
            node.put("stopDone", Boolean.TRUE);
            node.put("statusLabel", "已完成");
            node.put("statusToneClass", TONE_DONE);
            node.put("cardToneClass", CARD_DONE);
            applyDeliveredActualTimes(node, task, stopNodeIndex, orderedStops, route);
            return;
        }
        if ("EXCEPTION".equals(progress)) {
            node.put("statusLabel", "配送异常");
            node.put("statusToneClass", TONE_WARN);
            node.put("cardToneClass", CARD_WARN);
            return;
        }
        if ("CURRENT".equals(progress)) {
            node.put("statusLabel", "配送中");
            node.put("statusToneClass", TONE_ACTIVE);
            node.put("cardToneClass", CARD_ACTIVE);
            return;
        }
        node.put("statusLabel", "未到站");
        node.put("statusToneClass", TONE_PENDING);
        node.put("cardToneClass", CARD_PENDING);
    }

    /** 配送页当前站操作按钮（统一模板之上叠加）。 */
    public static void applyDriverTerminalDeliveryActions(Map<String, Object> node, int stopNodeIndex, int currentStopIndex) {
        if (node == null || currentStopIndex < 0 || stopNodeIndex != currentStopIndex) {
            return;
        }
        node.put("showComplete", Boolean.TRUE);
        node.put("showException", Boolean.TRUE);
        node.put("showNav", Boolean.TRUE);
    }

    private static void clearLegacyStopPresentationFields(Map<String, Object> node) {
        node.remove("scheduleRowLabel");
        node.remove("windowWaitHint");
        node.remove("distanceLabel");
        node.remove("arrivalFieldLabel");
        node.remove("departureFieldLabel");
        node.remove("arrivalLabel");
        node.remove("departureLabel");
        node.remove("legDistanceLabel");
        node.remove("nextLegHintLabel");
    }

    private static void ensureServiceDurationLabel(Map<String, Object> node,
                                                   NxDisRouteStopEntity stop,
                                                   NxDisShipmentTaskEntity task) {
        if (node.get("serviceDurationLabel") != null) {
            return;
        }
        int minutes = resolveServiceMinutes(stop, task);
        if (minutes > 0) {
            node.put("serviceDurationLabel", minutes + " 分钟");
        }
    }

    private static void applyDeliveredActualTimes(Map<String, Object> node,
                                                  NxDisShipmentTaskEntity task,
                                                  int stopNodeIndex,
                                                  List<NxDisRouteStopEntity> orderedStops,
                                                  NxDisDriverRouteEntity route) {
        node.put("arrivalFieldLabel", "出发时间");
        node.put("departureFieldLabel", "到达时间");
        node.remove("arrivalStatusLabel");
        node.remove("arrivalStatusTone");
        if (task == null) {
            return;
        }
        Date departTime = resolveDepartTimeForDeliveredStop(stopNodeIndex, orderedStops, route);
        if (departTime != null) {
            String departLabel = RouteDispatchDateFormat.format(departTime);
            node.put("plannedArrivalLabel", departLabel);
            node.put("arrivalLabel", departLabel);
        }
        if (task.getNxDstDeliveredAt() != null) {
            String deliveredLabel = RouteDispatchDateFormat.format(task.getNxDstDeliveredAt());
            node.put("plannedDepartureLabel", deliveredLabel);
            node.put("departureLabel", deliveredLabel);
        }
    }

    private static void applyDeliveredPresentation(Map<String, Object> node,
                                                   NxDisShipmentTaskEntity task,
                                                   int stopNodeIndex,
                                                   List<NxDisRouteStopEntity> orderedStops,
                                                   NxDisDriverRouteEntity route) {
        node.put("statusLabel", "已完成");
        node.put("statusToneClass", TONE_DONE);
        node.put("cardToneClass", CARD_DONE);
        node.put("showComplete", false);
        node.put("showException", false);
        node.put("showNav", false);
        node.remove("nextLegHint");
        node.remove("nextLegHintLabel");
        applyDeliveredActualTimes(node, task, stopNodeIndex, orderedStops, route);
    }

    private static void applyCurrentDeliveryPresentation(Map<String, Object> node,
                                                         NxDisRouteStopEntity stop,
                                                         NxDisShipmentTaskEntity task,
                                                         int stopNodeIndex,
                                                         List<NxDisRouteStopEntity> orderedStops,
                                                         NxDisDriverRouteEntity route,
                                                         Date serverNow) {
        node.put("statusLabel", "配送中");
        node.put("statusToneClass", TONE_ACTIVE);
        node.put("cardToneClass", CARD_ACTIVE);
        node.put("showComplete", true);
        node.put("showException", true);
        node.put("showNav", true);
        node.remove("distanceLabel");
        node.remove("distanceText");
        node.remove("legDistanceLabel");
        node.remove("arrivalFieldLabel");
        node.remove("departureFieldLabel");
        node.remove("arrivalLabel");
        node.remove("departureLabel");

        NxDisRouteStopEntity effectiveStop = resolveEffectiveStop(stop, orderedStops, stopNodeIndex);
        CurrentStopSchedule schedule = buildCurrentStopSchedule(
                effectiveStop, task, stopNodeIndex, orderedStops, route, serverNow);
        node.put("scheduleRowLabel", schedule.scheduleRowLabel);
        if (schedule.windowWaitHint != null) {
            node.put("windowWaitHint", schedule.windowWaitHint);
        }
        if (node.get("nextLegHint") != null) {
            node.put("nextLegHintLabel", "完成本站后 · ");
        }
    }

    private static NxDisRouteStopEntity resolveEffectiveStop(NxDisRouteStopEntity stop,
                                                             List<NxDisRouteStopEntity> orderedStops,
                                                             int stopNodeIndex) {
        if (stop != null) {
            return stop;
        }
        if (orderedStops == null || stopNodeIndex < 0 || stopNodeIndex >= orderedStops.size()) {
            return null;
        }
        return orderedStops.get(stopNodeIndex);
    }

    private static String buildDeliveryScheduleRowLabel(Date arrivalAt,
                                                        int serviceMinutes,
                                                        Date departureAt,
                                                        Date serverNow) {
        String arrival = DisRouteTemporalHelper.formatDateTimeLabel(arrivalAt, serverNow);
        String service = serviceMinutes + " 分钟";
        String departure = DisRouteTemporalHelper.formatDateTimeLabel(departureAt, serverNow);
        return "到达：" + arrival + "，装卸：" + service + "，离开：" + departure;
    }

    private static CurrentStopSchedule buildCurrentStopSchedule(NxDisRouteStopEntity stop,
                                                                NxDisShipmentTaskEntity task,
                                                                int stopNodeIndex,
                                                                List<NxDisRouteStopEntity> orderedStops,
                                                                NxDisDriverRouteEntity route,
                                                                Date serverNow) {
        if (stop == null || serverNow == null) {
            throw new IllegalStateException("current delivery stop schedule requires stop and serverNow");
        }
        NxDisShipmentTaskEntity effectiveTask = task != null ? task : stop.getShipmentTask();
        int serviceMinutes = resolveServiceMinutes(stop, effectiveTask);
        if (serviceMinutes <= 0) {
            serviceMinutes = 30;
        }
        Long legSec = DisRouteSandboxDisplayFormatHelper.resolveLegDurationS(stop);
        if ((legSec == null || legSec <= 0L) && effectiveTask != null
                && effectiveTask.getNxDstLegDurationS() != null && effectiveTask.getNxDstLegDurationS() > 0L) {
            legSec = effectiveTask.getNxDstLegDurationS();
        }
        Date serviceStart;
        Date serviceEnd;
        String windowWaitHint = null;
        if (legSec != null && legSec > 0L) {
            Date anchor = resolveDepartTimeForDeliveredStop(stopNodeIndex, orderedStops, route);
            if (anchor == null && route != null && route.getNxDdrActualDepartAt() != null) {
                anchor = route.getNxDdrActualDepartAt();
            }
            if (anchor == null) {
                anchor = serverNow;
            }
            Date driveArrive = new Date(anchor.getTime() + legSec * 1000L);
            if (driveArrive.before(serverNow)) {
                driveArrive = new Date(serverNow.getTime() + legSec * 1000L);
            }
            Date earliestWindow = resolveEarliestDeliveryAt(stop);
            serviceStart = driveArrive;
            if (earliestWindow != null && earliestWindow.after(driveArrive)) {
                serviceStart = earliestWindow;
                int waitMin = (int) Math.ceil((earliestWindow.getTime() - driveArrive.getTime()) / 60000.0);
                if (waitMin > 0) {
                    windowWaitHint = "客户窗口 "
                            + DisRouteSandboxScheduleLabelHelper.formatTimeHm(earliestWindow)
                            + " 起送，路程约 "
                            + Math.max(1L, Math.round(legSec / 60.0))
                            + " 分钟，早到需等约 "
                            + waitMin
                            + " 分钟";
                }
            }
            serviceEnd = new Date(serviceStart.getTime() + serviceMinutes * 60000L);
        } else {
            serviceStart = resolvePlannedArrivalAt(stop);
            serviceEnd = resolvePlannedDepartureAt(stop);
            if (serviceStart == null || serviceEnd == null) {
                Date anchor = resolveDepartTimeForDeliveredStop(stopNodeIndex, orderedStops, route);
                if (anchor == null && route != null && route.getNxDdrActualDepartAt() != null) {
                    anchor = route.getNxDdrActualDepartAt();
                }
                if (anchor == null) {
                    anchor = serverNow;
                }
                serviceStart = serverNow.after(anchor) ? serverNow : anchor;
                serviceEnd = new Date(serviceStart.getTime() + serviceMinutes * 60000L);
            }
        }
        CurrentStopSchedule schedule = new CurrentStopSchedule();
        schedule.scheduleRowLabel = buildDeliveryScheduleRowLabel(
                serviceStart, serviceMinutes, serviceEnd, serverNow);
        schedule.arrivalLabel = DisRouteTemporalHelper.formatDateTimeLabel(serviceStart, serverNow);
        schedule.windowWaitHint = windowWaitHint;
        return schedule;
    }

    private static Date resolvePlannedArrivalAt(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsPlannedArrivalAt() != null) {
            return stop.getNxDrsPlannedArrivalAt();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        return task != null ? task.getNxDstPlannedArrivalAt() : null;
    }

    private static Date resolvePlannedDepartureAt(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsPlannedDepartureAt() != null) {
            return stop.getNxDrsPlannedDepartureAt();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        return task != null ? task.getNxDstPlannedDepartureAt() : null;
    }

    private static Date resolveEarliestDeliveryAt(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        Integer earliestSeconds = stop.getNxDrsEarliestDeliveryTimeS();
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (earliestSeconds == null && task != null) {
            earliestSeconds = task.getNxDstEarliestDeliveryTimeS();
        }
        if (earliestSeconds == null) {
            return null;
        }
        String routeDate = task != null ? task.getNxDstRouteDate() : null;
        if (routeDate == null || routeDate.trim().isEmpty()) {
            return null;
        }
        Date dayStart = DisRouteOrderArriveDateHelper.parseRouteDateStart(routeDate.trim());
        if (dayStart == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(dayStart);
        cal.add(Calendar.SECOND, earliestSeconds);
        return cal.getTime();
    }

    private static int resolveServiceMinutes(NxDisRouteStopEntity stop, NxDisShipmentTaskEntity task) {
        if (stop != null && stop.getNxDrsServiceMinutes() != null && stop.getNxDrsServiceMinutes() > 0) {
            return stop.getNxDrsServiceMinutes();
        }
        if (task != null && task.getNxDstServiceMinutes() != null && task.getNxDstServiceMinutes() > 0) {
            return task.getNxDstServiceMinutes();
        }
        return 0;
    }

    private static final class CurrentStopSchedule {
        private String scheduleRowLabel;
        private String arrivalLabel;
        private String windowWaitHint;
    }

    private static void applyUpcomingDeliveryPresentation(Map<String, Object> node,
                                                          NxDisRouteStopEntity stop,
                                                          NxDisShipmentTaskEntity task,
                                                          Date serverNow) {
        node.put("arrivalFieldLabel", "计划到达");
        node.put("departureFieldLabel", "计划离开");
        node.put("statusLabel", "未到站");
        node.put("statusToneClass", TONE_PENDING);
        node.put("cardToneClass", CARD_PENDING);
        node.put("showComplete", false);
        node.put("showException", false);
        node.put("showNav", false);
        if (stop != null && serverNow != null) {
            node.put("arrivalLabel", resolveAbsolutePlannedArrivalLabel(stop, serverNow));
            node.put("departureLabel", resolveAbsolutePlannedDepartureLabel(stop, serverNow));
        }
        node.remove("nextLegHint");
        node.remove("nextLegHintLabel");
    }

    private static void applyExceptionPresentation(Map<String, Object> node,
                                                   NxDisShipmentTaskEntity task,
                                                   NxDisRouteStopEntity stop,
                                                   Date serverNow) {
        node.put("arrivalFieldLabel", "预计到达");
        node.put("departureFieldLabel", "异常时间");
        node.put("statusLabel", "配送异常");
        node.put("statusToneClass", TONE_WARN);
        node.put("cardToneClass", CARD_WARN);
        node.put("showComplete", false);
        node.put("showException", false);
        node.put("showNav", false);
        node.remove("nextLegHint");
        node.remove("nextLegHintLabel");
        if (stop != null && serverNow != null) {
            node.put("arrivalLabel", resolveAbsolutePlannedArrivalLabel(stop, serverNow));
        }
        if (task != null && task.getNxDstExceptionAt() != null) {
            node.put("departureLabel", RouteDispatchDateFormat.format(task.getNxDstExceptionAt()));
        }
    }

    static String resolveAbsolutePlannedArrivalLabel(NxDisRouteStopEntity stop, Date serverNow) {
        if (stop == null) {
            return null;
        }
        Date arrivalAt = stop.getNxDrsPlannedArrivalAt();
        if (arrivalAt == null && stop.getShipmentTask() != null) {
            arrivalAt = stop.getShipmentTask().getNxDstPlannedArrivalAt();
        }
        if (arrivalAt == null) {
            return null;
        }
        return DisRouteTemporalHelper.formatDateTimeLabel(arrivalAt, serverNow);
    }

    static String resolveAbsolutePlannedDepartureLabel(NxDisRouteStopEntity stop, Date serverNow) {
        if (stop == null) {
            return null;
        }
        Date departureAt = stop.getNxDrsPlannedDepartureAt();
        if (departureAt == null && stop.getShipmentTask() != null) {
            departureAt = stop.getShipmentTask().getNxDstPlannedDepartureAt();
        }
        if (departureAt == null) {
            return null;
        }
        return DisRouteTemporalHelper.formatDateTimeLabel(departureAt, serverNow);
    }

    public static void applyDeliveryMapMarkerPresentation(com.nongxinle.dto.route.SandboxTodayMapMarkerDto marker,
                                                          NxDisRouteStopEntity stop,
                                                          int stopIndex,
                                                          int currentStopIndex,
                                                          Date serverNow,
                                                          NxDisDriverRouteEntity route,
                                                          List<NxDisRouteStopEntity> orderedStops) {
        if (marker == null || stop == null) {
            return;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        String progress = resolveDeliveryProgressState(task, stopIndex, currentStopIndex, route);
        if ("DELIVERED".equals(progress)) {
            marker.setColor("#BFBFBF");
            marker.setDisplaySubtitle("已送达");
            marker.setArrivalLabel(null);
            return;
        }
        if ("EXCEPTION".equals(progress)) {
            marker.setColor("#FAAD14");
            marker.setDisplaySubtitle("配送异常");
            marker.setArrivalLabel(null);
            return;
        }
        if ("CURRENT".equals(progress)) {
            marker.setColor("#34B768");
            CurrentStopSchedule schedule = buildCurrentStopSchedule(
                    stop, task, stopIndex, orderedStops, route, serverNow);
            marker.setDisplaySubtitle(schedule.arrivalLabel);
            marker.setArrivalLabel(schedule.arrivalLabel);
            return;
        }
        marker.setColor("#D9D9D9");
        marker.setDisplaySubtitle("未到站");
        marker.setArrivalLabel(null);
    }

    private static String resolveLoadingStatusLabel(NxDisShipmentTaskEntity task) {
        if (task != null && task.getNxDstStatusLabel() != null && !task.getNxDstStatusLabel().trim().isEmpty()) {
            String label = task.getNxDstStatusLabel().trim();
            if (!"配送中".equals(label)) {
                return label;
            }
        }
        return "已分派";
    }

    private static Date resolveDepartTimeForDeliveredStop(int stopNodeIndex,
                                                          List<NxDisRouteStopEntity> orderedStops,
                                                          NxDisDriverRouteEntity route) {
        if (stopNodeIndex > 0 && orderedStops != null && stopNodeIndex <= orderedStops.size()) {
            NxDisRouteStopEntity previous = orderedStops.get(stopNodeIndex - 1);
            NxDisShipmentTaskEntity previousTask = previous != null ? previous.getShipmentTask() : null;
            if (previousTask != null && previousTask.getNxDstDeliveredAt() != null) {
                return previousTask.getNxDstDeliveredAt();
            }
        }
        if (route == null) {
            return null;
        }
        if (route.getNxDdrActualDepartAt() != null) {
            return route.getNxDdrActualDepartAt();
        }
        if (route.getDepartedAt() != null) {
            return route.getDepartedAt();
        }
        return route.getNxDdrPlannedDepartAt();
    }
}
