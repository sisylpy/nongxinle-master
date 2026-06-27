package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxTodayCardItemDto;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.route.dispatch.strategy.DispatchSequenceBucket;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * PR-2c：单站可见路线快照 — sections / map / debug 共用，不依赖 stop.nxDrsStopSeq 二次解析。
 */
@Getter
@Setter
public class VisibleDriverRouteStopSnapshot {

    private int visibleSeq;
    private Integer depFatherId;
    private Integer departmentId;
    private String customerName;
    private String cardKey;
    private String sandboxStopKey;

    private Date plannedArrivalAt;
    private Date plannedDepartureAt;
    private String arrivalLabel;
    private String departureLabel;
    private String customerWindowLabel;
    private String serviceDurationLabel;
    private String timeLabel;
    private Integer serviceMinutes;

    private Long legDistanceM;
    private Long legDurationS;
    private String distanceText;
    private String durationText;
    private String legText;

    private Integer earliestDeliveryTimeS;
    private Integer latestDeliveryTimeS;
    private String windowSource;
    private String timeWindowStatus;
    private String timeWindowStatusLabel;
    private Integer lateMinutes;
    private Integer waitMinutes;
    private String warningLabel;

    private DispatchSequenceBucket sequenceBucket;
    private String sequenceReason;
    private String sequenceSortKey;
    private Integer projectedArrivalTimeS;

    private String lat;
    private String lng;

    private String statusLabel;
    private String goodsSummary;
    private List<SandboxTodayCardItemDto> items = new ArrayList<SandboxTodayCardItemDto>();

    /** 站点操作（确认分派、改时间窗等）仍读源实体。 */
    private NxDisRouteStopEntity sourceStop;
    private DisRouteSandboxTodayRouteKind stopKind;
}
