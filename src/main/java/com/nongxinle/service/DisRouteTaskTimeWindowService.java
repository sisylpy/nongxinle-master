package com.nongxinle.service;

import com.nongxinle.dto.route.TaskTimeWindowRequest;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

/** Phase 2b-5：当日送达窗口 override */
public interface DisRouteTaskTimeWindowService {

    NxDisShipmentTaskEntity updateTimeWindow(Integer taskId, TaskTimeWindowRequest request);
}
