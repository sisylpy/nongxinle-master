package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/** Phase 2b-3：司机可派列表单项（日期字段为 yyyy-MM-dd HH:mm:ss 字符串） */
@Setter
@Getter
@ToString
public class DriverDispatchCandidateDto {
    private Integer driverUserId;
    private String driverName;
    private String driverPhone;
    /** 微信头像 URL；无真实头像时不设此字段 */
    private String driverAvatarUrl;
    private String dutyStatus;
    private String dutyStatusLabel;
    private String checkInAt;
    private String checkOutAt;
    private String dispatchBatch;
    private String dispatchBatchLabel;
    private Boolean batchEligible;
    private String batchEligibleLabel;
    private String ineligibleReason;
    private String ineligibleReasonLabel;
    private Integer currentRouteId;
    /** 已确认入库站点数（与 currentStopCount 同口径，字段名更明确） */
    private Integer confirmedStopCount;
    /** 沙盘建议站点数（仅动态沙盘，未入库） */
    private Integer sandboxSuggestedStopCount;
    /** @deprecated 请读 confirmedStopCount；保留兼容 */
    private Integer currentStopCount;
    private Integer currentLateMinutes;
    private Integer currentWaitMinutes;
    private String currentFeasibilityStatus;
    private String currentFeasibilityStatusLabel;
    private String operationHint;

    /** 是否允许关闭可派（仅上岗司机有意义） */
    private Boolean canToggleDutyOff;
    /** 不允许关闭可派时的锁定原因 */
    private String dutyOffLockReason;
    /** 关闭可派动作（enabled/disabled + payload） */
    private Map<String, Object> toggleDutyOffAction;

    /** Phase 3D：确认司机出发（司机卡片） */
    private Integer driverRouteId;
    private String routeStatus;
    private String routeStatusLabel;
    private Integer totalStopCount;
    private Boolean canDepart;
    private String departActionLabel;
    private String departBlockedReason;
    private String departConfirmMessage;
    private String departWarning;
    private Integer unprintedBillCount;
    private String actualDepartAt;
    private String departedAt;
}
