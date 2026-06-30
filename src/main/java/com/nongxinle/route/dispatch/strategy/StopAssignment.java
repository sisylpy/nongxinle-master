package com.nongxinle.route.dispatch.strategy;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StopAssignment {
    private Integer depFatherId;
    private String sandboxStopKey;
    private Integer assignedDriverUserId;
    private int stopSeq;
    private DispatchStopClass stopClass;
    private DispatchFeasibility feasibility;
    private Integer earliestDeliveryTimeS;
    private Integer latestDeliveryTimeS;
    private Integer serviceMinutes;
    private Boolean timeWindowOverrideFlag;
    private Integer preferredDriverUserId;
    private Integer historyAvgStopSeq;
    private DispatchPlanningReason planningReason;
    /** PR-2c：预计到达（routeDate 当天 0 点起秒数）。 */
    private Integer projectedArrivalTimeS;
    /** PR-2c：预计服务结束 cursor（内部排序模拟用，debug 可选）。 */
    private Long projectedServiceEndSeconds;
    /** PR-2c：{@link DispatchTimeWindowDebugStatus} 名称。 */
    private String timeWindowStatus;
    private Integer lateMinutes;
    private Integer waitMinutes;
    private String warningLabel;
    /** PR-2c：统一时间窗来源 {@link com.nongxinle.route.SandboxStopTimeWindowSource}。 */
    private String resolvedWindowSource;
}
