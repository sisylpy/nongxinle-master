package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DisRouteOrderSnapshotDto {
    private Integer orderId;
    private Integer departmentId;
    private String departmentName;
    private String lat;
    private String lng;
    private String address;
    private String goodsName;
    private String quantity;
    private String standard;
    private String remark;
}
