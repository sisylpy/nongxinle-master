package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Phase 2b-3：司机可派列表单项（日期字段为 yyyy-MM-dd HH:mm:ss 字符串） */
@Setter
@Getter
@ToString
public class DriverDispatchCandidateDto {
    private Integer driverUserId;
    private String driverName;
    private String driverPhone;
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
    private String latestAllowedCheckInAt;
    private Integer currentRouteId;
    private Integer currentStopCount;
    private Integer currentLateMinutes;
    private Integer currentWaitMinutes;
    private String currentFeasibilityStatus;
    private String currentFeasibilityStatusLabel;
    private String operationHint;
}
