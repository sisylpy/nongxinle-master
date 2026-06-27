package com.nongxinle.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** PR-2c：司机可见路线快照 — 建议派车 / 已确认待装车区唯一主权源。 */
@Getter
@Setter
public class VisibleDriverRouteSnapshot {

    private Integer driverUserId;
    private String driverName;
    private DisRouteSandboxTodayRouteKind routeKind;
    /** {@code SUGGESTED_DRIVER_ROUTES} 或 {@code CONFIRMED_DRIVER_ROUTES} */
    private String sectionKey;

    private NxDisDriverRouteEntity sourceRoute;
    private List<VisibleDriverRouteStopSnapshot> stops = new ArrayList<VisibleDriverRouteStopSnapshot>();

    private Long totalDistanceM;
    private Long totalDurationS;
    private String totalDistanceText;
    private String totalDurationText;
    private String scheduleHeadline;
    private String plannedDepartLabel;
    private String plannedReturnLabel;

    private Integer suggestedDepartTimeS;
    private Integer plannedDepartTimeS;
    private String suggestedDepartReason;
    private String suggestedDepartReasonLabel;

    /** leg 矩阵失败时使用直线估算，route 级可观察标记。 */
    private boolean legMetricsFallbackUsed;
    private String scheduleWarning;
}
