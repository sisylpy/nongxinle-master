package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

/** 地图卡片顶栏摘要：客户数 / 路线数 / 未分配数。 */
@Getter
@Setter
public class SandboxTodayMapSummaryDto {
    private Integer customerStopCount;
    private Integer routeCount;
    private Integer unassignedCount;
}
