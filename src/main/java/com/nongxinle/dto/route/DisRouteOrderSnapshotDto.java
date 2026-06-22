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
    /** 订单 nx_DO_arrive_date（送达日期，优先作为 routeDate 主权） */
    private String arriveDate;
    /** 订单 nx_DO_arrive_only_date（MM-dd，需补全年份） */
    private String arriveOnlyDate;
    /** 订单 nx_DO_apply_date（申请日，兜底） */
    private String applyDate;
}
