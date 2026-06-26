package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 今日派车 section 卡片：{@code DRIVER_ROUTE} 或 {@code CUSTOMER}。
 */
@Getter
@Setter
public class SandboxTodaySectionCardDto {
    private String cardType;

    /** DRIVER_ROUTE */
    private Integer driverUserId;
    private String driverName;
    private String driverAvatarUrl;
    private String driverStatusLabel;
    private String driverStatusTone;
    private String badgeLabel;
    private String scheduleHeadline;
    private String routeSummary;
    private String routeStatsLine;
    private Integer customerStopCount;
    private Long totalDistanceM;
    private Long totalDurationS;
    private String totalDistanceText;
    private String totalDurationText;

    /** 老板端分派/装车/配送：往返里程与出发-返回时长展示 */
    private String recommendedDepartLabel;
    private String routeSuggestedDepartLabel;
    private String firstStopPlannedArrivalLabel;
    /** 司机卡三列布局：首站到达时刻（如 06:00） */
    private String firstStopPlannedArrivalTimeLabel;
    /** 司机卡首站到达状态（如 准时 / 预计迟到） */
    private String firstStopArrivalStatusLabel;
    /** ok | early | warn */
    private String firstStopArrivalStatusTone;
    private String routeHeadlineLine;
    private String routeRoundTripSummaryLine;
    private String firstStopWindowLabel;
    private Integer firstStopWindowStartS;
    private Integer firstStopWindowEndS;
    private String depotName;
    private String depotAddress;
    private Long outboundDistanceM;
    private Long returnDistanceM;
    private Long totalRoundTripDistanceM;
    private String outboundDistanceText;
    private String returnDistanceText;
    private String totalRoundTripDistanceText;
    private Date recommendedDepartAt;
    private Date routeSuggestedDepartAt;
    private Date plannedReturnAt;
    private String plannedDepartLabel;
    private String plannedReturnLabel;
    private Long totalRoundTripDurationS;
    private String totalRoundTripDurationText;

    private List<Map<String, Object>> timeline = new ArrayList<Map<String, Object>>();
    private List<SandboxTodayStopCardDto> stopCards = new ArrayList<SandboxTodayStopCardDto>();

    /** 配送页 EXECUTION 司机卡：统一模板与扩展字段 */
    private DispatchDriverCardDto driverCard;
    private String dispatchStage;
    private String dispatchStageLabel;
    private String estimatedReturnLabel;
    private Integer customerCount;
    private Integer completedStopCount;
    private Integer pendingStopCount;
    private String driverNameResolveStatus;
    private String driverNameMissingReason;

    /** CUSTOMER（待分配 / 异常 / 已确认等扁平客户卡） */
    private String cardKey;
    private String customerName;
    private Integer departmentId;
    private String goodsSummary;
    private List<SandboxTodayCardItemDto> items = new ArrayList<SandboxTodayCardItemDto>();
    private String plannedArrivalLabel;
    private String plannedDepartureLabel;
    private String customerWindowLabel;
    private String serviceDurationLabel;
    private String driverLabel;
    private Integer suggestedDriverUserId;
    private String suggestedDriverName;
    private String distanceText;
    private String durationText;
    private String timeLabel;
    private String statusLabel;
    private Map<String, Object> primaryAction;
    /** 司机路线人工编辑入口（与 primaryAction 并列，不替代装车等主操作）。 */
    private Map<String, Object> routeEditAction;
}
