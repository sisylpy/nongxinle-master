package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SandboxTodayMapDepotDto {
    private String markerType = "DEPOT";
    private String name;
    private Double lat;
    private Double lng;
    private String colorKey = "DEPOT";
    private String color = "#333333";
}
