package com.nongxinle.dto.route;

import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/** Phase 2b-1：可执行性评估结果 */
@Setter
@Getter
@ToString
public class RouteFeasibilityResult {
    private Integer planId;
    private String feasibilityStatus;
    private List<RouteDispatchWarning> warnings = new ArrayList<RouteDispatchWarning>();
}
