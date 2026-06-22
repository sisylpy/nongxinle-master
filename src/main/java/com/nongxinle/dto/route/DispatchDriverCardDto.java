package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 派单系统统一司机卡模板。
 * 各页面只渲染此结构，不在前端自行拼业务字段。
 */
@Setter
@Getter
@ToString
public class DispatchDriverCardDto {
    private Integer driverUserId;
    private String driverName;
    private String driverAvatarUrl;
    /** ON_DUTY / OFF_DUTY */
    private String dutyStatus;
    private String dutyStatusLabel;
    /** IDLE / SANDBOX / CONFIRMED / LOADING / EXECUTION / COMPLETED */
    private String dispatchStage;
    private String dispatchStageLabel;
    private String plannedDepartAt;
    private String plannedDepartLabel;
    private String plannedReturnAt;
    private String plannedReturnLabel;
    /** 司机可派状态页别名（与 plannedDepart* 同口径） */
    private String plannedDepartureAt;
    private String plannedDepartureLabel;
    /** 司机可派状态页别名（与 plannedReturn* 同口径） */
    private String estimatedReturnAt;
    private String estimatedReturnLabel;
    private Long totalDistanceM;
    private Long totalDurationS;
    private String totalDistanceText;
    private String totalDurationText;
    /** 路线摘要（客户数 + 距离 + 耗时，便于一行展示） */
    private String routeSummary;
    private Integer currentStopCount;
    /** 司机可派状态页别名（与 currentStopCount 同口径） */
    private Integer customerCount;
    private Integer completedStopCount;
    private Integer pendingStopCount;
    /** 司机可派状态页：是否允许切换可派（开启/关闭） */
    private Boolean canToggleDuty;
    private String toggleDisabledReason;
    private Boolean canSimulate;
    private Boolean canConfirm;
    private String confirmMode;
    private String confirmModeLabel;
    private List<String> riskHints = new ArrayList<String>();
    private String blockedReason;
    private String operationHint;
    /** 主操作按钮（primaryAction，如 SIMULATE_MANUAL_DISPATCH） */
    private Map<String, Object> primaryAction;
}
