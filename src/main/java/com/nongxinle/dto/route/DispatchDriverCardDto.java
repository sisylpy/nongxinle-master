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
    /** 今日派车 duty card：IDLE / LOADING / EXECUTION。delivery 页 execution 卡仍可能为 COMPLETED（DB 记录）。 */
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
    /** 主描述一行：准备出发 / 现在可送 / 预计返回 */
    private String headline;
    /** 路线指标一行：15.2 公里 · 49 分钟 */
    private String metricsLine;
    /** 当前任务一行：1 个客户 · 待送 1 */
    private String currentTaskLine;
    /** ok / warn / muted — 前端 badge 色调 */
    private String dutyBadgeTone;
    private String stageBadgeTone;
    /** ok / warn — operationHint 色调 */
    private String hintTone;
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
