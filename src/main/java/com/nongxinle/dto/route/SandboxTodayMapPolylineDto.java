package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SandboxTodayMapPolylineDto {
    private String routeKey;
    private Integer driverUserId;
    private String driverName;
    private String colorKey;
    private String color;
    /** SOLID / DASHED */
    private String lineStyle;
    /** DRIVER_ROUTE / UNASSIGNED */
    private String kind;
    /** ROAD / STRAIGHT / FALLBACK — 路线点来源，仅展示用 */
    private String lineType;
    private List<SandboxTodayMapPointDto> points = new ArrayList<SandboxTodayMapPointDto>();
}
