package com.nongxinle.route.model;

import com.nongxinle.dto.route.DisRouteOrderSnapshotDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class RouteStopInput {
    private String stopKey;
    private Integer departmentId;
    private String departmentName;
    private GeoPoint location;
    private String address;
    private int orderCount;
    /** Phase 1.5b：关联 shipment_task */
    private Integer shipmentTaskId;
    private List<DisRouteOrderSnapshotDto> orders = new ArrayList<DisRouteOrderSnapshotDto>();
}
