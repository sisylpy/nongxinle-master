package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * 派单系统统一分店 / 客户店铺卡模板。
 * 各页面只渲染此结构，不在前端自行拼业务字段。
 */
@Setter
@Getter
@ToString
public class DispatchStoreCardDto {
    private Integer departmentId;
    private String sandboxStopKey;
    private String customerName;
    private String goodsSummary;
    private String distanceText;
    private String durationText;
    private String plannedArrivalLabel;
    private String plannedDepartureLabel;
    /** 送达窗口 */
    private String customerWindowLabel;
    private String serviceDurationLabel;
    /** 当前派单状态 */
    private String dispatchStatusLabel;
    private SandboxManualDispatchManualTimeConstraintDto manualTimeConstraint;
    private Map<String, Object> primaryAction;
}
