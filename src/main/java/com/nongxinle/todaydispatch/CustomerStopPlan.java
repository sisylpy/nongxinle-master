package com.nongxinle.todaydispatch;

import com.nongxinle.entity.NxDisRouteStopEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 分派中一个客户站点（展示 + action payload 源）。 */
@Getter
@Setter
public class CustomerStopPlan {

    private Integer depFatherId;
    private Integer customerId;
    private Integer taskId;
    private Integer deliveryStopId;
    private String customerName;
    private String customerShortName;
    private String cardKey;
    private String sandboxStopKey;
    private int sequence;
    private String goodsSummary;
    private String windowLabel;
    private String customerWindowLabel;
    private String windowRequirementLabel;
    private Boolean windowRequirementModified;
    private String windowStatusLabel;
    private String plannedArrivalLabel;
    private String plannedDepartureLabel;
    private String serviceDurationLabel;
    private String timeLabel;
    private String distanceText;
    private String durationText;
    private Long legDistanceM;
    private Long legDurationS;
    private String arrivalStatusLabel;
    private String arrivalStatusTone;
    private String statusLabel;
    private Double lat;
    private Double lng;
    private Integer earliestDeliveryTimeS;
    private Integer latestDeliveryTimeS;
    private Integer serviceMinutes;
    private Boolean timeWindowOverrideFlag;
    private List<Integer> liveOrderIds = new ArrayList<Integer>();
    private Map<String, Object> primaryAction;
    private String unassignedDriverHint;
    /** 原始 stop（finalize / orphan 后仍有效）。 */
    private NxDisRouteStopEntity sourceStop;
}
