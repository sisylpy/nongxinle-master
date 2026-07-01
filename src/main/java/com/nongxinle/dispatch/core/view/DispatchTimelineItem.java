package com.nongxinle.dispatch.core.view;

import lombok.Getter;
import lombok.Setter;

/** 路线时间轴节点（站点 / 终点 / 仓库）。 */
@Getter
@Setter
public class DispatchTimelineItem {

    /** stop | start | leg | end（与 Nx timeline 字段一致） */
    private String type;
    private Integer routeSeq;
    private String legText;
    private String legRole;
    private String marker;
    private String name;
    private String timeRight;
    private String distanceText;
    private String durationText;
    private String legDistanceLabel;
    private String plannedArrivalLabel;
    private String plannedDepartureLabel;
    private String arrivalFieldLabel;
    private String departureFieldLabel;
    private String serviceDurationLabel;
    private String arrivalStatusLabel;
    private String arrivalStatusTone;
    private String customerWindowLabel;
    private String windowRequirementLabel;
    private Boolean windowRequirementModified;
    private String status;
    private String statusLabel;
    private String statusToneClass;
    private String cardToneClass;
    private String title;
    private String subtitle;
    private Double lat;
    private Double lng;
    private Integer stopId;
    private Integer deliveryStopId;
    /** 路线编辑 stopKey：STOP-{id} 或 ADDR-{addressId} */
    private String stopKey;
    private Integer addressId;
    private Double navLat;
    private Double navLng;
    private String navAddress;
    private Boolean stopDone;
    private Boolean showComplete;
    private Boolean showNav;
    private Boolean showException;
    private DispatchStopCard stopCard;
    private DispatchPrimaryAction primaryAction;
}
