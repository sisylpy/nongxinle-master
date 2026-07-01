package com.nongxinle.dispatch.core.view;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 站点卡片（未分派客户 / 路线内站点）。 */
@Getter
@Setter
public class DispatchStopCard implements DispatchSectionCard {

    @Override
    public String getCardType() {
        return cardType;
    }

    private String cardType;
    private String cardKey;
    private String badgeLabel;
    private String driverLabel;
    private String goodsSummary;
    private Integer suggestedDriverUserId;
    private String sandboxStopKey;
    private Integer stopId;
    private Integer driverRouteId;
    private Integer driverUserId;
    private Integer addressId;
    private Integer routeSeq;
    private String status;
    private String statusLabel;
    private String statusToneClass;
    private String customerName;
    private String customerPhone;
    private String addressText;
    private Double lat;
    private Double lng;
    private Integer orderCount;
    private List<Integer> orderIds = new ArrayList<Integer>();
    private List<String> goodsSummaries = new ArrayList<String>();
    private String legText;
    private String distanceText;
    private String durationText;
    private String plannedArrivalLabel;
    private String plannedDepartureLabel;
    private String serviceDurationLabel;
    private String arrivalStatusLabel;
    private String arrivalStatusTone;
    private String customerWindowLabel;
    private String windowRequirementLabel;
    private Boolean windowRequirementModified;
    private boolean simulated;
    private DispatchPrimaryAction primaryAction;
}
