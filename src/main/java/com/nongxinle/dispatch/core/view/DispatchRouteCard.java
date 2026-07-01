package com.nongxinle.dispatch.core.view;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 司机路线卡片（沙箱 / 装车 / 配送三页共用结构）。 */
@Getter
@Setter
public class DispatchRouteCard implements DispatchSectionCard {

    @Override
    public String getCardType() {
        return cardType;
    }

    private String cardType;
    private String cardKey;
    private String driverStatusTone;
    private Integer deliveryProgressPercent;
    private String deliveryProgressLine;
    private Integer driverRouteId;
    private Integer driverUserId;
    private String driverName;
    private String driverPhone;
    private String routeStatus;
    private String routeStatusLabel;
    private String routeStatusToneClass;
    private int stopCount;
    private int deliveredCount;
    private int exceptionCount;
    private String loadingEnteredAtLabel;
    private String actualDepartAtLabel;
    private String actualDepartStatusLabel;
    private String actualDepartStatusTone;
    private String routeSummary;
    private String routeStatsLine;
    private String plannedDepartLabel;
    private String plannedReturnLabel;
    private String totalDistanceText;
    private String totalDurationText;
    private String totalRoundTripDistanceText;
    private String totalRoundTripDurationText;
    private String firstStopPlannedArrivalTimeLabel;
    private String firstStopArrivalStatusLabel;
    private String firstStopArrivalStatusTone;
    private String routeHeadlineLine;
    private String routeRoundTripSummaryLine;
    private boolean showDepartAction;
    private boolean simulated;
    private List<DispatchStopCard> stopCards = new ArrayList<DispatchStopCard>();
    private List<DispatchTimelineItem> timeline = new ArrayList<DispatchTimelineItem>();
    private DispatchPrimaryAction primaryAction;
    private DispatchPrimaryAction routeEditAction;
}
