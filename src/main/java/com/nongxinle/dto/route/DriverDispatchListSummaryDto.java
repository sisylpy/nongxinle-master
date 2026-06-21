package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Phase 2b-3：司机可派列表统计摘要 */
@Setter
@Getter
@ToString
public class DriverDispatchListSummaryDto {
    private Integer totalDriverCount;
    private Integer onDutyCount;
    private Integer eligibleCount;
    private Integer ineligibleCount;
    private Integer idleCount;
    private Integer assignedRouteDriverCount;
}
