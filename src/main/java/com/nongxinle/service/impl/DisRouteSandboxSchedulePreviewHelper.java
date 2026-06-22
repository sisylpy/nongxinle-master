package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDepartmentDao;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteDispatchLabels;
import com.nongxinle.route.DisRouteSandboxScheduleLabelHelper;
import com.nongxinle.route.DisRouteSandboxScheduleMode;
import com.nongxinle.route.DisRouteSandboxScheduleModeResolver;
import com.nongxinle.route.DisRouteSandboxScheduleTimeBasis;
import com.nongxinle.route.DisRouteStopTimeWindowStatus;
import com.nongxinle.route.DisRouteTemporalHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Phase 3a：内存排程预览（不写 DB），支持正式预排与即时补单两种时间锚点。
 */
@Component
public class DisRouteSandboxSchedulePreviewHelper {

    private static final int DEFAULT_DEPART_HOUR = 6;
    private static final int DEFAULT_SERVICE_MINUTES = 30;

    @Autowired
    private NxDepartmentDao nxDepartmentDao;

    public void applySchedulePreview(NxDisRoutePlanEntity plan,
                                     NxDisDriverRouteEntity driverRoute,
                                     String routeDate) {
        if (driverRoute == null || driverRoute.getStops() == null || driverRoute.getStops().isEmpty()) {
            return;
        }
        List<NxDisRouteStopEntity> stops = new java.util.ArrayList<NxDisRouteStopEntity>(driverRoute.getStops());
        Collections.sort(stops, new Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                int seqA = a.getNxDrsStopSeq() != null ? a.getNxDrsStopSeq() : 0;
                int seqB = b.getNxDrsStopSeq() != null ? b.getNxDrsStopSeq() : 0;
                return Integer.compare(seqA, seqB);
            }
        });

        Date serverNow = new Date();
        String deliveryRouteDate = resolveDeliveryRouteDate(stops, routeDate);
        Date dayStart = parseRouteDateStart(deliveryRouteDate);

        String routeScheduleMode = resolveRouteScheduleMode(stops, deliveryRouteDate, serverNow, dayStart);
        boolean adhocRoute = DisRouteSandboxScheduleModeResolver.isAdhocNow(routeScheduleMode);

        Date initialDepartAt = adhocRoute
                ? serverNow
                : resolveScheduledInitialDepartAt(stops, dayStart, deliveryRouteDate, serverNow);
        Date cursor = initialDepartAt;
        int totalWait = 0;
        int totalLate = 0;
        boolean hasLate = false;
        boolean allNoWindow = !stops.isEmpty();
        Date routeFastestArrival = null;

        for (NxDisRouteStopEntity stop : stops) {
            WindowConfig window = resolveWindow(stop);
            String stopMode = DisRouteSandboxScheduleModeResolver.resolveMode(
                    deliveryRouteDate, serverNow, window.earliestSeconds, window.latestSeconds);
            boolean adhocStop = DisRouteSandboxScheduleModeResolver.isAdhocNow(stopMode);

            if (adhocStop && cursor.before(serverNow)) {
                cursor = serverNow;
            }

            long legSeconds = stop.getNxDrsLegDurationS() != null ? stop.getNxDrsLegDurationS() : 0L;
            Date plannedArrivalAt = addSeconds(cursor, legSeconds);

            Date earliestAt = window.earliestSeconds != null
                    ? addSeconds(dayStart, window.earliestSeconds) : null;
            Date latestAt = window.latestSeconds != null
                    ? addSeconds(dayStart, window.latestSeconds) : null;

            WindowResult wr = adhocStop
                    ? applyAdhocWindow(plannedArrivalAt, earliestAt, latestAt)
                    : applyScheduledWindow(plannedArrivalAt, earliestAt, latestAt);
            Date plannedDepartureAt = addMinutes(wr.plannedServiceStartAt, window.serviceMinutes);

            stop.setNxDrsEarliestDeliveryTimeS(window.earliestSeconds);
            stop.setNxDrsLatestDeliveryTimeS(window.latestSeconds);
            stop.setNxDrsServiceMinutes(window.serviceMinutes);
            stop.setNxDrsPlannedArrivalAt(plannedArrivalAt);
            stop.setNxDrsPlannedServiceStartAt(wr.plannedServiceStartAt);
            stop.setNxDrsPlannedDepartureAt(plannedDepartureAt);
            stop.setNxDrsWaitMinutes(wr.waitMinutes);
            stop.setNxDrsLateMinutes(wr.lateMinutes);
            stop.setNxDrsTimeWindowStatus(wr.timeWindowStatus);
            stop.setServiceMinutesSource(window.serviceMinutesSource);
            stop.setServiceDurationLabel(formatServiceDurationLabel(window.serviceMinutes));
            enrichStopScheduleReadModel(stop, stopMode, adhocStop, serverNow, deliveryRouteDate,
                    window, initialDepartAt, plannedArrivalAt, wr.isAfterCustomerWindow);

            if (routeFastestArrival == null || plannedArrivalAt.before(routeFastestArrival)) {
                routeFastestArrival = plannedArrivalAt;
            }

            if (!adhocStop) {
                totalWait += wr.waitMinutes;
                totalLate += wr.lateMinutes;
                if (DisRouteStopTimeWindowStatus.LATE.equals(wr.timeWindowStatus)) {
                    hasLate = true;
                }
            }
            if (!DisRouteStopTimeWindowStatus.NO_WINDOW.equals(wr.timeWindowStatus)) {
                allNoWindow = false;
            }
            cursor = plannedDepartureAt;
        }

        driverRoute.setNxDdrPlannedDepartAt(initialDepartAt);
        NxDisRouteStopEntity lastStop = stops.get(stops.size() - 1);
        Date lastStopDepartureAt = cursor;
        Date lastStopArrivalAt = lastStop.getNxDrsPlannedServiceStartAt() != null
                ? lastStop.getNxDrsPlannedServiceStartAt() : lastStop.getNxDrsPlannedArrivalAt();
        long returnDurationS = driverRoute.getReturnLegDurationS() != null
                ? driverRoute.getReturnLegDurationS() : 0L;
        Date plannedReturnAt = addSeconds(lastStopDepartureAt, returnDurationS);

        driverRoute.setLastStopDepartureAt(lastStopDepartureAt);
        driverRoute.setLastStopArrivalAt(lastStopArrivalAt);
        driverRoute.setPlannedReturnAt(plannedReturnAt);
        driverRoute.setNxDdrPlannedFinishAt(plannedReturnAt);
        driverRoute.setNxDdrTotalWaitMinutes(totalWait);
        driverRoute.setNxDdrTotalLateMinutes(totalLate);
        driverRoute.setNxDdrStopCount(stops.size());
        if (stops.isEmpty()) {
            driverRoute.setNxDdrScheduleStatus(com.nongxinle.route.DisRouteScheduleStatus.OK);
        } else if (hasLate) {
            driverRoute.setNxDdrScheduleStatus(com.nongxinle.route.DisRouteScheduleStatus.HAS_LATE);
        } else if (allNoWindow) {
            driverRoute.setNxDdrScheduleStatus(com.nongxinle.route.DisRouteScheduleStatus.NO_WINDOW);
        } else {
            driverRoute.setNxDdrScheduleStatus(com.nongxinle.route.DisRouteScheduleStatus.OK);
        }

        enrichDriverRouteScheduleReadModel(driverRoute, routeScheduleMode, adhocRoute,
                serverNow, initialDepartAt, lastStopDepartureAt, plannedReturnAt, routeFastestArrival);
        driverRoute.setReturnLegLabel(formatReturnLegLabel(driverRoute));
        applyRouteDurationTotal(driverRoute);
    }

    private static String formatReturnLegLabel(NxDisDriverRouteEntity route) {
        if (route == null) {
            return null;
        }
        Long distM = route.getReturnLegDistanceM();
        Long durS = route.getReturnLegDurationS();
        if (distM == null && durS == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("返回市场");
        if (distM != null) {
            sb.append(' ').append(String.format(java.util.Locale.ROOT, "%.1f", distM / 1000.0)).append(" km");
        }
        if (durS != null) {
            sb.append(" · ").append(Math.max(1, Math.round(durS / 60.0))).append(" 分钟");
        }
        return sb.toString();
    }

    /**
     * 路线总时长 = 各段行驶 + 各站服务 + 各站等待 + 返程行驶（秒）。
     */
    private static void applyRouteDurationTotal(NxDisDriverRouteEntity driverRoute) {
        if (driverRoute == null || driverRoute.getStops() == null || driverRoute.getStops().isEmpty()) {
            return;
        }
        long totalSeconds = 0L;
        for (NxDisRouteStopEntity stop : driverRoute.getStops()) {
            if (stop == null) {
                continue;
            }
            if (stop.getNxDrsLegDurationS() != null) {
                totalSeconds += stop.getNxDrsLegDurationS();
            }
            if (stop.getNxDrsWaitMinutes() != null) {
                totalSeconds += stop.getNxDrsWaitMinutes() * 60L;
            }
            if (stop.getNxDrsServiceMinutes() != null) {
                totalSeconds += stop.getNxDrsServiceMinutes() * 60L;
            }
        }
        if (driverRoute.getReturnLegDurationS() != null) {
            totalSeconds += driverRoute.getReturnLegDurationS();
        }
        driverRoute.setNxDdrTotalDurationS(totalSeconds);
    }

    private static String formatServiceDurationLabel(int serviceMinutes) {
        return "服务 " + serviceMinutes + " 分钟";
    }

    private void enrichStopScheduleReadModel(NxDisRouteStopEntity stop,
                                             String stopMode,
                                             boolean adhocStop,
                                             Date serverNow,
                                             String deliveryRouteDate,
                                             WindowConfig window,
                                             Date routeDepartAt,
                                             Date plannedArrivalAt,
                                             boolean afterCustomerWindow) {
        stop.setScheduleMode(stopMode);
        stop.setScheduleModeLabel(DisRouteSandboxScheduleLabelHelper.modeLabel(stopMode));
        stop.setTimeBasis(adhocStop
                ? DisRouteSandboxScheduleTimeBasis.SERVER_NOW
                : DisRouteSandboxScheduleTimeBasis.CUSTOMER_WINDOW);
        stop.setTimeBasisLabel(DisRouteSandboxScheduleLabelHelper.timeBasisLabel(stop.getTimeBasis()));
        stop.setTimeAnchorAt(adhocStop ? serverNow : null);
        stop.setTimeAnchorLabel(DisRouteSandboxScheduleLabelHelper.timeAnchorLabel(stopMode));
        stop.setIsAfterCustomerWindow(afterCustomerWindow);
        stop.setCustomerWindowLabel(DisRouteSandboxScheduleLabelHelper.formatCustomerWindowLabel(
                deliveryRouteDate, window.earliestSeconds, window.latestSeconds, serverNow));
        if (adhocStop) {
            stop.setFastestArrivalLabel(DisRouteSandboxScheduleLabelHelper.formatAdhocFastestArrivalLabel(
                    plannedArrivalAt, serverNow));
            stop.setPlannedDepartLabel("现在可送");
            stop.setPlannedArrivalLabel(DisRouteSandboxScheduleLabelHelper.formatAdhocPlannedArrivalLabel(
                    plannedArrivalAt, serverNow));
            stop.setNxDrsTimeWindowStatusLabel(DisRouteDispatchLabels.label(stop.getNxDrsTimeWindowStatus()));
            stop.setStopTemporalStatus(com.nongxinle.route.DisRouteStopTemporalStatus.UPCOMING);
            stop.setStopTemporalStatusLabel("未到达");
        } else {
            stop.setFastestArrivalLabel(DisRouteSandboxScheduleLabelHelper.formatFastestArrivalLabel(
                    plannedArrivalAt, serverNow, stopMode));
            stop.setPlannedDepartLabel(DisRouteSandboxScheduleLabelHelper.formatDepartLabel(
                    routeDepartAt, serverNow, stopMode));
            stop.setPlannedArrivalLabel(DisRouteTemporalHelper.formatDateTimeLabel(
                    stop.getNxDrsPlannedServiceStartAt() != null
                            ? stop.getNxDrsPlannedServiceStartAt() : plannedArrivalAt, serverNow));
            stop.setNxDrsTimeWindowStatusLabel(DisRouteDispatchLabels.label(stop.getNxDrsTimeWindowStatus()));
        }
        Date plannedDepartureAt = stop.getNxDrsPlannedDepartureAt();
        if (plannedDepartureAt != null) {
            stop.setPlannedDepartureLabel(DisRouteTemporalHelper.formatDateTimeLabel(plannedDepartureAt, serverNow));
        }
    }

    private void enrichDriverRouteScheduleReadModel(NxDisDriverRouteEntity route,
                                                    String routeScheduleMode,
                                                    boolean adhocRoute,
                                                    Date serverNow,
                                                    Date departAt,
                                                    Date lastStopDepartureAt,
                                                    Date plannedReturnAt,
                                                    Date fastestArrival) {
        route.setScheduleMode(routeScheduleMode);
        route.setScheduleModeLabel(DisRouteSandboxScheduleLabelHelper.modeLabel(routeScheduleMode));
        route.setTimeBasisLabel(DisRouteSandboxScheduleLabelHelper.timeBasisLabel(
                adhocRoute ? DisRouteSandboxScheduleTimeBasis.SERVER_NOW
                        : DisRouteSandboxScheduleTimeBasis.CUSTOMER_WINDOW));
        if (adhocRoute) {
            route.setPlannedDepartLabel("现在可送");
            String returnLabel = DisRouteSandboxScheduleLabelHelper.formatAdhocPlannedArrivalLabel(
                    plannedReturnAt, serverNow);
            route.setPlannedFinishLabel(returnLabel + " 返回");
            route.setPlannedReturnLabel(returnLabel + " 返回");
            route.setRouteScheduleSummaryLabel(DisRouteSandboxScheduleLabelHelper.formatRouteScheduleSummary(
                    routeScheduleMode, fastestArrival, serverNow));
        } else {
            route.setPlannedDepartLabel(DisRouteTemporalHelper.formatDateTimeLabel(departAt, serverNow) + " 出发");
            String returnLabel = DisRouteTemporalHelper.formatDateTimeLabel(plannedReturnAt, serverNow);
            route.setPlannedFinishLabel(returnLabel + " 返回");
            route.setPlannedReturnLabel(returnLabel + " 返回");
            route.setRouteScheduleSummaryLabel(null);
        }
    }

    private String resolveRouteScheduleMode(List<NxDisRouteStopEntity> stops,
                                            String deliveryRouteDate,
                                            Date serverNow,
                                            Date dayStart) {
        for (NxDisRouteStopEntity stop : stops) {
            WindowConfig window = resolveWindow(stop);
            if (DisRouteSandboxScheduleModeResolver.isAdhocNow(
                    DisRouteSandboxScheduleModeResolver.resolveMode(
                            deliveryRouteDate, serverNow, window.earliestSeconds, window.latestSeconds))) {
                return DisRouteSandboxScheduleMode.ADHOC_NOW;
            }
        }
        return DisRouteSandboxScheduleMode.SCHEDULED_BATCH;
    }

    private String resolveDeliveryRouteDate(List<NxDisRouteStopEntity> stops, String fallbackRouteDate) {
        for (NxDisRouteStopEntity stop : stops) {
            if (stop != null && stop.getShipmentTask() != null
                    && stop.getShipmentTask().getNxDstRouteDate() != null
                    && !stop.getShipmentTask().getNxDstRouteDate().trim().isEmpty()) {
                return stop.getShipmentTask().getNxDstRouteDate().trim();
            }
        }
        return fallbackRouteDate;
    }

    private Date resolveScheduledInitialDepartAt(List<NxDisRouteStopEntity> stops,
                                                 Date dayStart,
                                                 String routeDate,
                                                 Date serverNow) {
        NxDisRouteStopEntity firstStop = stops.get(0);
        long firstLegSeconds = firstStop.getNxDrsLegDurationS() != null ? firstStop.getNxDrsLegDurationS() : 0L;
        WindowConfig firstWindow = resolveWindow(firstStop);
        if (firstWindow.earliestSeconds != null) {
            Date earliestAt = addSeconds(dayStart, firstWindow.earliestSeconds);
            Date departAt = addSeconds(earliestAt, -firstLegSeconds);
            if (isRouteDateToday(routeDate, serverNow) && serverNow.after(departAt)) {
                return serverNow;
            }
            return departAt;
        }
        if (isRouteDateToday(routeDate, serverNow)) {
            return serverNow;
        }
        return addHours(dayStart, DEFAULT_DEPART_HOUR);
    }

    private WindowResult applyAdhocWindow(Date plannedArrivalAt, Date earliestAt, Date latestAt) {
        WindowResult result = new WindowResult();
        result.plannedServiceStartAt = plannedArrivalAt;
        result.waitMinutes = 0;
        result.lateMinutes = 0;
        if (latestAt != null && plannedArrivalAt.after(latestAt)) {
            result.timeWindowStatus = DisRouteStopTimeWindowStatus.SUPPLEMENT_AFTER_WINDOW;
            result.isAfterCustomerWindow = true;
        } else {
            result.timeWindowStatus = DisRouteStopTimeWindowStatus.OK;
            result.isAfterCustomerWindow = false;
        }
        return result;
    }

    private WindowResult applyScheduledWindow(Date plannedArrivalAt, Date earliestAt, Date latestAt) {
        WindowResult result = new WindowResult();
        if (earliestAt == null && latestAt == null) {
            result.timeWindowStatus = DisRouteStopTimeWindowStatus.NO_WINDOW;
            result.plannedServiceStartAt = plannedArrivalAt;
            return result;
        }
        if (earliestAt != null && plannedArrivalAt.before(earliestAt)) {
            result.timeWindowStatus = DisRouteStopTimeWindowStatus.EARLY_WAIT;
            result.waitMinutes = minutesCeil(earliestAt.getTime() - plannedArrivalAt.getTime());
            result.plannedServiceStartAt = earliestAt;
            return result;
        }
        if (latestAt != null && plannedArrivalAt.after(latestAt)) {
            result.timeWindowStatus = DisRouteStopTimeWindowStatus.LATE;
            result.lateMinutes = minutesCeil(plannedArrivalAt.getTime() - latestAt.getTime());
            result.plannedServiceStartAt = plannedArrivalAt;
            return result;
        }
        result.timeWindowStatus = DisRouteStopTimeWindowStatus.OK;
        result.plannedServiceStartAt = plannedArrivalAt;
        return result;
    }

    private static boolean isRouteDateToday(String routeDate, Date serverNow) {
        if (routeDate == null || serverNow == null) {
            return false;
        }
        return routeDate.equals(DisRouteTemporalHelper.formatTodayYyyyMmDd(serverNow));
    }

    private WindowConfig resolveWindow(NxDisRouteStopEntity stop) {
        WindowConfig config = new WindowConfig();
        config.serviceMinutes = DEFAULT_SERVICE_MINUTES;
        config.serviceMinutesSource = com.nongxinle.route.DisRouteServiceMinutesSource.DEFAULT;
        config.earliestSeconds = stop.getNxDrsEarliestDeliveryTimeS();
        config.latestSeconds = stop.getNxDrsLatestDeliveryTimeS();
        if (stop.getNxDrsServiceMinutes() != null) {
            config.serviceMinutes = stop.getNxDrsServiceMinutes();
        }
        if (config.earliestSeconds == null && config.latestSeconds == null && stop.getNxDrsDepartmentId() != null) {
            NxDepartmentEntity department = nxDepartmentDao.queryObject(stop.getNxDrsDepartmentId());
            if (department != null) {
                config.earliestSeconds = department.getNxDepartmentEarliestDeliveryTime();
                config.latestSeconds = department.getNxDepartmentLatestDeliveryTime();
                if (department.getNxDepartmentUnloadDuration() != null) {
                    config.serviceMinutes = department.getNxDepartmentUnloadDuration();
                    config.serviceMinutesSource = com.nongxinle.route.DisRouteServiceMinutesSource.DEPARTMENT;
                }
            }
        }
        if (stop.getShipmentTask() != null) {
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (config.earliestSeconds == null) {
                config.earliestSeconds = task.getNxDstEarliestDeliveryTimeS();
            }
            if (config.latestSeconds == null) {
                config.latestSeconds = task.getNxDstLatestDeliveryTimeS();
            }
            if (task.getNxDstServiceMinutes() != null) {
                config.serviceMinutes = task.getNxDstServiceMinutes();
                config.serviceMinutesSource = com.nongxinle.route.DisRouteServiceMinutesSource.TASK;
            }
        }
        return config;
    }

    private static int minutesCeil(long millis) {
        return (int) Math.ceil(millis / 60000.0);
    }

    private static Date parseRouteDateStart(String routeDate) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            return fmt.parse(routeDate);
        } catch (ParseException ex) {
            return new Date();
        }
    }

    private static Date addHours(Date base, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }

    private static Date addSeconds(Date base, long seconds) {
        return new Date(base.getTime() + seconds * 1000L);
    }

    private static Date addMinutes(Date base, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }

    private static class WindowConfig {
        Integer earliestSeconds;
        Integer latestSeconds;
        int serviceMinutes;
        String serviceMinutesSource;
    }

    private static class WindowResult {
        String timeWindowStatus;
        int waitMinutes;
        int lateMinutes;
        Date plannedServiceStartAt;
        boolean isAfterCustomerWindow;
    }
}
