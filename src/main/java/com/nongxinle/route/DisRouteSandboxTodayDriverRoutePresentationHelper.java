package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxTodaySectionCardDto;
import com.nongxinle.dto.route.SandboxTodayStopCardDto;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 老板端分派中/装车/配送：司机路线卡展示口径（往返里程、出发-返回时长、站点三列时间）。
 * 不修改 {@link VisibleDriverRouteSnapshot} 主权链，仅在 card/timeline 输出层 enrich。
 */
public final class DisRouteSandboxTodayDriverRoutePresentationHelper {

    private static final Logger log = LoggerFactory.getLogger(
            DisRouteSandboxTodayDriverRoutePresentationHelper.class);

    private DisRouteSandboxTodayDriverRoutePresentationHelper() {
    }

    public static void applyDispatchBossPresentation(SandboxTodaySectionCardDto card,
                                                     VisibleDriverRouteSnapshot routeSnapshot,
                                                     List<SandboxTodayStopCardDto> stopCards,
                                                     SandboxTodayPageBuildContext ctx) {
        if (card == null || routeSnapshot == null || stopCards == null || stopCards.isEmpty() || ctx == null) {
            return;
        }
        DisRouteSandboxTodayRouteKind kind = routeSnapshot.getRouteKind();
        if (kind != DisRouteSandboxTodayRouteKind.SUGGESTED
                && kind != DisRouteSandboxTodayRouteKind.CONFIRMED) {
            return;
        }

        Date serverNow = ctx.getServerNow();
        String routeDate = ctx.getRouteDate();
        NxDisDriverRouteEntity route = routeSnapshot.getSourceRoute();
        String depotName = resolveDepotName(ctx);
        String depotAddress = ctx.getDepotAddress();

        long outboundDistanceM = routeSnapshot.getTotalDistanceM() != null ? routeSnapshot.getTotalDistanceM() : 0L;
        Long returnDistanceM = route != null ? route.getReturnLegDistanceM() : null;
        long totalRoundTripDistanceM = outboundDistanceM + (returnDistanceM != null ? returnDistanceM : 0L);

        VisibleDriverRouteStopSnapshot firstStopSnap = firstVisibleStop(routeSnapshot);
        Date backCalcDepartAt = resolveBackCalcDepartAt(firstStopSnap, routeDate);
        Date plannedDepartAt = resolvePlannedDepartAt(
                routeSnapshot, route, routeDate, backCalcDepartAt, serverNow, firstStopSnap);
        boolean departNow = shouldDepartNow(plannedDepartAt, serverNow, firstStopSnap, routeDate);
        Date routeSuggestedDepartAt = departNow && serverNow != null ? serverNow : plannedDepartAt;
        Date plannedReturnAt = resolvePlannedReturnAt(routeSnapshot, route, routeDate, stopCards);

        String departTimeLabel = departNow
                ? "现在"
                : DisRouteTemporalHelper.formatRouteTimeLabel(routeSuggestedDepartAt, serverNow, routeDate);
        String returnTimeLabel = DisRouteTemporalHelper.formatRouteTimeLabel(plannedReturnAt, serverNow, routeDate);

        card.setDepotName(depotName);
        card.setDepotAddress(depotAddress);
        card.setOutboundDistanceM(outboundDistanceM);
        card.setReturnDistanceM(returnDistanceM);
        card.setTotalRoundTripDistanceM(totalRoundTripDistanceM);
        card.setOutboundDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(outboundDistanceM));
        card.setReturnDistanceText(DisRouteSandboxDisplayFormatHelper.formatDistanceText(returnDistanceM));
        card.setTotalRoundTripDistanceText(
                DisRouteSandboxDisplayFormatHelper.formatDistanceText(totalRoundTripDistanceM));

        card.setRecommendedDepartAt(routeSuggestedDepartAt);
        card.setRouteSuggestedDepartAt(routeSuggestedDepartAt);
        card.setPlannedReturnAt(plannedReturnAt);
        card.setPlannedDepartLabel(departTimeLabel);
        card.setPlannedReturnLabel(returnTimeLabel);
        card.setRecommendedDepartLabel(buildRouteSuggestedDepartLabel(departTimeLabel));
        card.setRouteSuggestedDepartLabel(card.getRecommendedDepartLabel());
        card.setTotalRoundTripDurationS(calcRoundTripDurationS(routeSuggestedDepartAt, plannedReturnAt));
        card.setTotalRoundTripDurationText(formatCompactDuration(card.getTotalRoundTripDurationS()));

        enrichStopCardsForBossView(stopCards, routeSnapshot, serverNow, routeDate, routeSuggestedDepartAt);

        if (firstStopSnap != null) {
            card.setFirstStopWindowLabel(formatFirstStopWindowLabel(
                    firstStopSnap.getCustomerWindowLabel(),
                    isWindowExpired(firstStopSnap, routeDate, serverNow)));
            card.setFirstStopWindowStartS(firstStopSnap.getEarliestDeliveryTimeS());
            card.setFirstStopWindowEndS(firstStopSnap.getLatestDeliveryTimeS());
            Date firstArrivalAt = resolveFirstStopPlannedArrivalAt(
                    firstStopSnap, routeSuggestedDepartAt, routeDate);
            String firstArrivalLabel = DisRouteTemporalHelper.formatRouteTimeLabel(
                    firstArrivalAt, serverNow, routeDate);
            if (isBlank(firstArrivalLabel) && !stopCards.isEmpty()) {
                firstArrivalLabel = stopCards.get(0).getPlannedArrivalLabel();
            }
            card.setFirstStopPlannedArrivalLabel(buildFirstStopPlannedArrivalLabel(firstArrivalLabel));
            card.setFirstStopPlannedArrivalTimeLabel(firstArrivalLabel);
            card.setFirstStopArrivalStatusLabel(
                    resolveFirstStopArrivalStatusLabel(firstStopSnap, firstArrivalAt, routeDate));
            card.setFirstStopArrivalStatusTone(
                    resolveFirstStopArrivalStatusTone(firstStopSnap, firstArrivalAt, routeDate));
        }

        card.setPlannedReturnLabel(buildPlannedReturnLabel(returnTimeLabel));
        card.setRouteRoundTripSummaryLine(buildRouteRoundTripSummaryLine(
                card.getTotalRoundTripDistanceText(), card.getTotalRoundTripDurationText()));
        card.setRouteHeadlineLine(buildRouteHeadlineLine(
                card.getFirstStopPlannedArrivalLabel(), card.getPlannedReturnLabel()));

        card.setScheduleHeadline(null);
        card.setRouteStatsLine(buildBossRouteStatsLine(
                stopCards.size(),
                card.getTotalRoundTripDistanceText(),
                departTimeLabel,
                returnTimeLabel,
                card.getTotalRoundTripDurationText()));
        card.setRouteSummary(card.getRouteStatsLine());
        card.setTotalDistanceM(totalRoundTripDistanceM);
        card.setTotalDurationS(card.getTotalRoundTripDurationS());
        card.setTotalDistanceText(card.getTotalRoundTripDistanceText());
        card.setTotalDurationText(card.getTotalRoundTripDurationText());

        card.setTimeline(DisRouteSandboxTodayTimelineBuilder.buildDispatchBossTimeline(
                card, stopCards, routeSnapshot.getStops(), depotName, depotAddress, route));

        if (log.isInfoEnabled()) {
            log.info("sandbox dispatch time: serverNow={}, routeDate={}, suggestedDepartAt={}, firstStopArrival={}",
                    serverNow, routeDate, routeSuggestedDepartAt,
                    firstStopSnap != null ? resolveFirstStopPlannedArrivalAt(
                            firstStopSnap, routeSuggestedDepartAt, routeDate) : null);
        }
    }

    private static void enrichStopCardsForBossView(List<SandboxTodayStopCardDto> stopCards,
                                                   VisibleDriverRouteSnapshot routeSnapshot,
                                                   Date serverNow,
                                                   String routeDate,
                                                   Date routeSuggestedDepartAt) {
        List<VisibleDriverRouteStopSnapshot> snaps = routeSnapshot.getStops();
        for (int i = 0; i < stopCards.size(); i++) {
            SandboxTodayStopCardDto stopCard = stopCards.get(i);
            VisibleDriverRouteStopSnapshot snap = i < snaps.size() ? snaps.get(i) : null;
            if (stopCard == null) {
                continue;
            }
            stopCard.setGoodsSummary(null);
            stopCard.setTimeLabel(null);

            String windowLabel = snap != null ? snap.getCustomerWindowLabel() : stopCard.getCustomerWindowLabel();
            stopCard.setDeliveryWindowLabel(formatDeliveryWindowLabel(
                    windowLabel, isWindowExpired(snap, routeDate, serverNow)));
            stopCard.setCustomerWindowLabel(windowLabel);
            stopCard.setWindowRequirementLabel(
                    formatWindowRequirementLabel(stopCard.getDeliveryWindowLabel()));
            stopCard.setWindowStatusLabel(resolveWindowStatusLabel(snap));

            Date plannedArrivalAt = i == 0
                    ? resolveFirstStopPlannedArrivalAt(snap, routeSuggestedDepartAt, routeDate)
                    : resolvePlannedArrivalAt(snap);
            stopCard.setPlannedArrivalLabel(DisRouteTemporalHelper.formatRouteTimeLabel(
                    plannedArrivalAt, serverNow, routeDate));
            stopCard.setArrivalStatusLabel(
                    resolveFirstStopArrivalStatusLabel(snap, plannedArrivalAt, routeDate));
            stopCard.setArrivalStatusTone(
                    resolveFirstStopArrivalStatusTone(snap, plannedArrivalAt, routeDate));

            stopCard.setServiceDurationLabel(formatCompactServiceDuration(
                    snap != null ? snap.getServiceDurationLabel() : stopCard.getServiceDurationLabel(),
                    snap != null ? snap.getServiceMinutes() : null));

            Date plannedDepartureAt = snap != null ? snap.getPlannedDepartureAt() : null;
            stopCard.setPlannedDepartureAt(plannedDepartureAt);
            stopCard.setPlannedDepartureLabel(DisRouteTemporalHelper.formatRouteTimeLabel(
                    plannedDepartureAt, serverNow, routeDate));

            syncStopCardLegDisplay(stopCard, snap);
        }
    }

    private static void syncStopCardLegDisplay(SandboxTodayStopCardDto stopCard,
                                               VisibleDriverRouteStopSnapshot snap) {
        if (stopCard == null) {
            return;
        }
        String distanceText = stopCard.getDistanceText();
        String durationText = stopCard.getDurationText();
        if (isBlank(distanceText) && snap != null) {
            distanceText = firstNonBlank(
                    snap.getDistanceText(),
                    DisRouteSandboxDisplayFormatHelper.formatDistanceText(snap.getLegDistanceM()));
        }
        if (isBlank(durationText) && snap != null) {
            durationText = firstNonBlank(
                    snap.getDurationText(),
                    DisRouteSandboxDisplayFormatHelper.formatDurationText(snap.getLegDurationS()));
        }
        stopCard.setDistanceText(distanceText);
        stopCard.setDurationText(durationText);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static Date resolvePlannedArrivalAt(VisibleDriverRouteStopSnapshot snap) {
        if (snap == null) {
            return null;
        }
        if (snap.getPlannedArrivalAt() != null) {
            return snap.getPlannedArrivalAt();
        }
        NxDisRouteStopEntity stop = snap.getSourceStop();
        if (stop != null && stop.getNxDrsPlannedArrivalAt() != null) {
            return stop.getNxDrsPlannedArrivalAt();
        }
        return null;
    }

    /**
     * 首站预计到达：优先 schedule 写入的 plannedArrivalAt，否则 projectedArrivalTimeS / 建议出发+首段 leg。
     */
    private static Date resolveFirstStopPlannedArrivalAt(VisibleDriverRouteStopSnapshot firstStopSnap,
                                                         Date routeSuggestedDepartAt,
                                                         String routeDate) {
        Date arrival = resolvePlannedArrivalAt(firstStopSnap);
        if (arrival != null) {
            return arrival;
        }
        if (firstStopSnap == null) {
            return null;
        }
        if (firstStopSnap.getProjectedArrivalTimeS() != null && routeDate != null) {
            Date fromProjected = secondsOnRouteDate(routeDate, firstStopSnap.getProjectedArrivalTimeS());
            if (fromProjected != null) {
                return fromProjected;
            }
        }
        if (routeSuggestedDepartAt != null && firstStopSnap.getLegDurationS() != null
                && firstStopSnap.getLegDurationS() > 0L) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(routeSuggestedDepartAt);
            cal.add(Calendar.SECOND, firstStopSnap.getLegDurationS().intValue());
            return cal.getTime();
        }
        return null;
    }

    private static String resolveFirstStopArrivalStatusLabel(VisibleDriverRouteStopSnapshot snap,
                                                             Date arrivalAt,
                                                             String routeDate) {
        String fromArrivalWindow = resolveArrivalWindowStatusLabel(snap, arrivalAt, routeDate);
        if (fromArrivalWindow != null) {
            return fromArrivalWindow;
        }
        return resolveFirstStopArrivalStatusLabel(snap);
    }

    private static String resolveFirstStopArrivalStatusTone(VisibleDriverRouteStopSnapshot snap,
                                                            Date arrivalAt,
                                                            String routeDate) {
        String toneFromWindow = resolveArrivalWindowStatusTone(snap, arrivalAt, routeDate);
        if (toneFromWindow != null) {
            return toneFromWindow;
        }
        return resolveFirstStopArrivalStatusTone(snap);
    }

    /** 用预计到达时刻与客户窗口比较，得出准时/早到/迟到（与 schedule 状态字段解耦）。 */
    private static String resolveArrivalWindowStatusLabel(VisibleDriverRouteStopSnapshot snap,
                                                          Date arrivalAt,
                                                          String routeDate) {
        if (snap == null || arrivalAt == null || routeDate == null) {
            return null;
        }
        Date latest = snap.getLatestDeliveryTimeS() != null
                ? secondsOnRouteDate(routeDate, snap.getLatestDeliveryTimeS()) : null;
        if (latest != null && arrivalAt.after(latest)) {
            long lateSeconds = (arrivalAt.getTime() - latest.getTime()) / 1000L;
            int lateMinutes = minutesCeil(lateSeconds);
            return lateMinutes > 0 ? "迟到" + lateMinutes + "分钟" : "预计迟到";
        }
        Date earliest = snap.getEarliestDeliveryTimeS() != null
                ? secondsOnRouteDate(routeDate, snap.getEarliestDeliveryTimeS()) : null;
        if (earliest != null && arrivalAt.before(earliest)) {
            long waitSeconds = (earliest.getTime() - arrivalAt.getTime()) / 1000L;
            int waitMinutes = minutesCeil(waitSeconds);
            return waitMinutes > 0 ? "早到 " + waitMinutes + " 分钟" : "早于窗口";
        }
        if (earliest != null || latest != null) {
            return "准时";
        }
        return null;
    }

    private static String resolveArrivalWindowStatusTone(VisibleDriverRouteStopSnapshot snap,
                                                         Date arrivalAt,
                                                         String routeDate) {
        if (snap == null || arrivalAt == null || routeDate == null) {
            return null;
        }
        Date latest = snap.getLatestDeliveryTimeS() != null
                ? secondsOnRouteDate(routeDate, snap.getLatestDeliveryTimeS()) : null;
        if (latest != null && arrivalAt.after(latest)) {
            return "warn";
        }
        Date earliest = snap.getEarliestDeliveryTimeS() != null
                ? secondsOnRouteDate(routeDate, snap.getEarliestDeliveryTimeS()) : null;
        if (earliest != null && arrivalAt.before(earliest)) {
            return "early";
        }
        if (earliest != null || latest != null) {
            return "ok";
        }
        return null;
    }

    private static int minutesCeil(long deltaSeconds) {
        if (deltaSeconds <= 0L) {
            return 0;
        }
        return (int) ((deltaSeconds + 59L) / 60L);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String resolveFirstStopArrivalStatusLabel(VisibleDriverRouteStopSnapshot snap) {
        if (snap == null) {
            return null;
        }
        if (DisRouteStopTimeWindowStatus.LATE.equals(snap.getTimeWindowStatus())) {
            Integer lateMinutes = snap.getLateMinutes();
            return lateMinutes != null && lateMinutes > 0
                    ? "迟到" + lateMinutes + "分钟" : "预计迟到";
        }
        if (DisRouteStopTimeWindowStatus.SUPPLEMENT_AFTER_WINDOW.equals(snap.getTimeWindowStatus())) {
            return "已过窗";
        }
        if (DisRouteStopTimeWindowStatus.EARLY_ARRIVAL.equals(snap.getTimeWindowStatus())) {
            Integer waitMinutes = snap.getWaitMinutes();
            return waitMinutes != null && waitMinutes > 0
                    ? "早到约" + waitMinutes + "分钟" : "早于窗口";
        }
        if (DisRouteStopTimeWindowStatus.EARLY_WAIT.equals(snap.getTimeWindowStatus())) {
            return "需等待";
        }
        return "准时";
    }

    private static String resolveFirstStopArrivalStatusTone(VisibleDriverRouteStopSnapshot snap) {
        if (snap == null) {
            return null;
        }
        if (DisRouteStopTimeWindowStatus.LATE.equals(snap.getTimeWindowStatus())
                || DisRouteStopTimeWindowStatus.SUPPLEMENT_AFTER_WINDOW.equals(snap.getTimeWindowStatus())) {
            return "warn";
        }
        if (DisRouteStopTimeWindowStatus.EARLY_ARRIVAL.equals(snap.getTimeWindowStatus())
                || DisRouteStopTimeWindowStatus.EARLY_WAIT.equals(snap.getTimeWindowStatus())) {
            return "early";
        }
        return "ok";
    }

    private static String resolveWindowStatusLabel(VisibleDriverRouteStopSnapshot snap) {
        if (snap == null) {
            return null;
        }
        if (snap.getTimeWindowStatusLabel() != null && !snap.getTimeWindowStatusLabel().trim().isEmpty()) {
            return snap.getTimeWindowStatusLabel().trim();
        }
        if (DisRouteStopTimeWindowStatus.SUPPLEMENT_AFTER_WINDOW.equals(snap.getTimeWindowStatus())) {
            return "已过窗";
        }
        if (DisRouteStopTimeWindowStatus.EARLY_ARRIVAL.equals(snap.getTimeWindowStatus())) {
            Integer wait = snap.getWaitMinutes();
            return wait != null && wait > 0 ? "早到约" + wait + "分钟" : "早于窗口";
        }
        if (DisRouteStopTimeWindowStatus.EARLY_WAIT.equals(snap.getTimeWindowStatus())) {
            return "需等待";
        }
        if (DisRouteStopTimeWindowStatus.LATE.equals(snap.getTimeWindowStatus())) {
            return "预计迟到";
        }
        if (DisRouteSandboxStopTimeWindowResolver.isTodayOverride(
                snap.getSourceStop() != null ? snap.getSourceStop() : null)) {
            return "今日调整";
        }
        if (DisRouteStopTimeWindowStatus.OK.equals(snap.getTimeWindowStatus())) {
            return "可按时";
        }
        return null;
    }

    private static VisibleDriverRouteStopSnapshot firstVisibleStop(VisibleDriverRouteSnapshot routeSnapshot) {
        if (routeSnapshot.getStops() == null || routeSnapshot.getStops().isEmpty()) {
            return null;
        }
        return routeSnapshot.getStops().get(0);
    }

    private static String resolveDepotName(SandboxTodayPageBuildContext ctx) {
        if (ctx.getDepotName() != null && !ctx.getDepotName().trim().isEmpty()) {
            return ctx.getDepotName().trim();
        }
        return DisRouteSandboxTodayTimelineBuilder.DEPOT_NAME;
    }

    private static Date resolveBackCalcDepartAt(VisibleDriverRouteStopSnapshot firstStop,
                                                String routeDate) {
        if (firstStop == null || firstStop.getEarliestDeliveryTimeS() == null) {
            return null;
        }
        long legS = firstStop.getLegDurationS() != null ? firstStop.getLegDurationS() : 0L;
        Date windowStart = secondsOnRouteDate(routeDate, firstStop.getEarliestDeliveryTimeS());
        if (windowStart == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(windowStart);
        cal.add(Calendar.SECOND, (int) -legS);
        return cal.getTime();
    }

    private static Date resolvePlannedDepartAt(VisibleDriverRouteSnapshot routeSnapshot,
                                               NxDisDriverRouteEntity route,
                                               String routeDate,
                                               Date backCalcDepartAt,
                                               Date serverNow,
                                               VisibleDriverRouteStopSnapshot firstStop) {
        Date fromSuggested = null;
        if (routeSnapshot.getSuggestedDepartTimeS() != null) {
            fromSuggested = secondsOnRouteDate(routeDate, routeSnapshot.getSuggestedDepartTimeS());
        }
        Date fromPlanned = null;
        if (routeSnapshot.getPlannedDepartTimeS() != null) {
            fromPlanned = secondsOnRouteDate(routeDate, routeSnapshot.getPlannedDepartTimeS());
        }
        if (backCalcDepartAt != null && serverNow != null && backCalcDepartAt.after(serverNow)
                && firstStop != null && !isWindowExpired(firstStop, routeDate, serverNow)) {
            Date snapshotDepart = firstNonNullDate(fromSuggested, fromPlanned);
            if (snapshotDepart == null || !snapshotDepart.after(serverNow)) {
                return backCalcDepartAt;
            }
        }
        if (fromSuggested != null) {
            return fromSuggested;
        }
        if (fromPlanned != null) {
            return fromPlanned;
        }
        if (route != null && route.getNxDdrPlannedDepartAt() != null) {
            return route.getNxDdrPlannedDepartAt();
        }
        return backCalcDepartAt;
    }

    private static Date firstNonNullDate(Date... values) {
        if (values == null) {
            return null;
        }
        for (Date value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static boolean shouldDepartNow(Date plannedDepartAt,
                                           Date serverNow,
                                           VisibleDriverRouteStopSnapshot firstStop,
                                           String routeDate) {
        if (serverNow == null) {
            return false;
        }
        if (firstStop != null && isWindowExpired(firstStop, routeDate, serverNow)) {
            return true;
        }
        if (plannedDepartAt == null) {
            return true;
        }
        return !plannedDepartAt.after(serverNow);
    }

    private static Date resolvePlannedReturnAt(VisibleDriverRouteSnapshot routeSnapshot,
                                               NxDisDriverRouteEntity route,
                                               String routeDate,
                                               List<SandboxTodayStopCardDto> stopCards) {
        if (route != null && route.getPlannedReturnAt() != null) {
            return route.getPlannedReturnAt();
        }
        if (route != null && route.getNxDdrPlannedFinishAt() != null) {
            return route.getNxDdrPlannedFinishAt();
        }
        List<VisibleDriverRouteStopSnapshot> snaps = routeSnapshot.getStops();
        if (snaps == null || snaps.isEmpty()) {
            return null;
        }
        VisibleDriverRouteStopSnapshot last = snaps.get(snaps.size() - 1);
        Date lastDeparture = last.getPlannedDepartureAt();
        if (lastDeparture == null) {
            return null;
        }
        long returnDurationS = route != null && route.getReturnLegDurationS() != null
                ? route.getReturnLegDurationS() : 0L;
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastDeparture);
        cal.add(Calendar.SECOND, (int) returnDurationS);
        return cal.getTime();
    }

    private static Date secondsOnRouteDate(String routeDate, int seconds) {
        if (routeDate == null || routeDate.trim().isEmpty()) {
            return null;
        }
        try {
            Date dayStart = DisRouteOrderArriveDateHelper.parseRouteDateStart(routeDate.trim());
            Calendar cal = Calendar.getInstance();
            cal.setTime(dayStart);
            cal.add(Calendar.SECOND, seconds);
            return cal.getTime();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static Long calcRoundTripDurationS(Date departAt, Date returnAt) {
        if (departAt == null || returnAt == null) {
            return null;
        }
        long diffMs = returnAt.getTime() - departAt.getTime();
        if (diffMs <= 0L) {
            return null;
        }
        return diffMs / 1000L;
    }

    private static String buildRouteSuggestedDepartLabel(String departTimeLabel) {
        if (departTimeLabel == null || departTimeLabel.trim().isEmpty()) {
            return null;
        }
        return "建议出发：" + departTimeLabel.trim();
    }

    private static String buildFirstStopPlannedArrivalLabel(String arrivalTimeLabel) {
        if (arrivalTimeLabel == null || arrivalTimeLabel.trim().isEmpty()) {
            return null;
        }
        return "首站预计 " + arrivalTimeLabel.trim() + " 到达";
    }

    private static String buildPlannedReturnLabel(String returnTimeLabel) {
        if (returnTimeLabel == null || returnTimeLabel.trim().isEmpty()) {
            return null;
        }
        return returnTimeLabel.trim();
    }

    private static String buildRouteHeadlineLine(String firstStopArrivalLine, String returnTimeLabel) {
        if (firstStopArrivalLine == null && returnTimeLabel == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (firstStopArrivalLine != null) {
            sb.append(firstStopArrivalLine);
        }
        if (returnTimeLabel != null) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append("预计 ").append(returnTimeLabel.trim()).append(" 返回");
        }
        return sb.toString();
    }

    private static String buildRouteRoundTripSummaryLine(String distanceText, String durationText) {
        StringBuilder sb = new StringBuilder();
        if (distanceText != null && !distanceText.isEmpty()) {
            sb.append("往返里程 ").append(distanceText);
        }
        if (durationText != null && !durationText.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append("全程 ").append(durationText);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private static String formatFirstStopWindowLabel(String customerWindowLabel, boolean expired) {
        if (customerWindowLabel == null || customerWindowLabel.trim().isEmpty()) {
            return null;
        }
        String trimmed = customerWindowLabel.trim();
        String formatted = trimmed.startsWith("首站窗口")
                ? trimmed : "首站窗口：" + stripWindowPrefix(trimmed);
        return appendExpiredSuffix(formatted, expired);
    }

    private static String formatDeliveryWindowLabel(String customerWindowLabel, boolean expired) {
        if (customerWindowLabel == null || customerWindowLabel.trim().isEmpty()) {
            return null;
        }
        String trimmed = customerWindowLabel.trim();
        String formatted = trimmed.startsWith("配送窗口")
                ? trimmed : "配送窗口：" + stripWindowPrefix(trimmed);
        return appendExpiredSuffix(formatted, expired);
    }

    private static String formatWindowRequirementLabel(String deliveryWindowLabel) {
        if (isBlank(deliveryWindowLabel)) {
            return null;
        }
        String text = deliveryWindowLabel.trim();
        if (text.startsWith("配送窗口：")) {
            text = text.substring("配送窗口：".length()).trim();
        } else if (text.startsWith("配送窗口:")) {
            text = text.substring("配送窗口:".length()).trim();
        }
        if (text.startsWith("今日调整 ")) {
            text = text.substring("今日调整 ".length()).trim();
        } else if (text.startsWith("常规窗口 ")) {
            text = text.substring("常规窗口 ".length()).trim();
        }
        text = text.replace('–', '-').replace('—', '-').replace("－", "-");
        if (text.endsWith(" 已过")) {
            text = text.substring(0, text.length() - " 已过".length()).trim();
        }
        if (text.isEmpty()) {
            return null;
        }
        return "要求 " + text;
    }

    private static String stripWindowPrefix(String label) {
        if (label.startsWith("常规窗口 ")) {
            return label.substring("常规窗口 ".length());
        }
        if (label.startsWith("今日调整 ")) {
            return label;
        }
        return label;
    }

    private static String appendExpiredSuffix(String label, boolean expired) {
        if (!expired || label == null || label.endsWith("已过")) {
            return label;
        }
        return label + " 已过";
    }

    private static boolean isWindowExpired(VisibleDriverRouteStopSnapshot stop,
                                           String routeDate,
                                           Date serverNow) {
        if (stop == null || stop.getLatestDeliveryTimeS() == null || serverNow == null
                || routeDate == null
                || !routeDate.equals(DisRouteTemporalHelper.formatTodayYyyyMmDd(serverNow))) {
            return false;
        }
        Calendar now = Calendar.getInstance();
        now.setTime(serverNow);
        int nowSeconds = now.get(Calendar.HOUR_OF_DAY) * 3600
                + now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND);
        return nowSeconds > stop.getLatestDeliveryTimeS();
    }

    private static String formatCompactDuration(Long durationS) {
        if (durationS == null || durationS <= 0L) {
            return null;
        }
        long minutes = Math.max(1L, Math.round(durationS / 60.0));
        if (minutes < 60L) {
            return minutes + "分钟";
        }
        long hours = minutes / 60L;
        long remain = minutes % 60L;
        if (remain <= 0L) {
            return hours + "小时";
        }
        return hours + "小时" + remain + "分";
    }

    private static String formatCompactServiceDuration(String serviceDurationLabel, Integer serviceMinutes) {
        if (serviceMinutes != null && serviceMinutes > 0) {
            return serviceMinutes + " 分钟";
        }
        if (serviceDurationLabel == null || serviceDurationLabel.trim().isEmpty()) {
            return null;
        }
        String trimmed = serviceDurationLabel.trim();
        if (trimmed.startsWith("服务 ")) {
            trimmed = trimmed.substring("服务 ".length());
        }
        trimmed = trimmed.replace("分钟", " 分钟").replace("  分钟", " 分钟");
        return trimmed.trim();
    }

    private static String buildBossRouteStatsLine(int stopCount,
                                                  String totalRoundTripDistanceText,
                                                  String plannedDepartLabel,
                                                  String plannedReturnLabel,
                                                  String totalRoundTripDurationText) {
        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(stopCount).append(" 站");
        if (totalRoundTripDistanceText != null && !totalRoundTripDistanceText.isEmpty()) {
            sb.append(" · 往返里程 ").append(totalRoundTripDistanceText);
        }
        if (plannedDepartLabel != null && plannedReturnLabel != null) {
            sb.append(" · ").append(plannedDepartLabel).append(" 出发，")
                    .append(plannedReturnLabel).append(" 返回");
        }
        if (totalRoundTripDurationText != null && !totalRoundTripDurationText.isEmpty()) {
            sb.append(" · 全程 ").append(totalRoundTripDurationText);
        }
        return sb.toString();
    }
}
