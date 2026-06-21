package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Phase 2b-5：当日送达窗口 override 请求 */
@Setter
@Getter
@ToString
public class TaskTimeWindowRequest {
    private Integer earliestDeliveryTimeS;
    private Integer latestDeliveryTimeS;
    private Integer serviceMinutes;
    private String reason;
    private Integer operatorUserId;
}
