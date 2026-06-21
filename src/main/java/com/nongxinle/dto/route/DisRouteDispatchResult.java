package com.nongxinle.dto.route;

import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/** Phase 2b-1：simulate / schedule 统一响应 */
@Setter
@Getter
@ToString
public class DisRouteDispatchResult {
    private NxDisRoutePlanEntity plan;
    private List<NxDisShipmentTaskEntity> tasks;
    private String feasibilityStatus;
    private List<RouteDispatchWarning> warnings = new ArrayList<RouteDispatchWarning>();
}
