package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SandboxTodayMapLegendItemDto {
    /** DRIVER / UNASSIGNED / DEPOT */
    private String kind;
    private String colorKey;
    private String color;
    private String label;
    private Integer driverUserId;
    /** SOLID / DASHED */
    private String lineStyle;
}
