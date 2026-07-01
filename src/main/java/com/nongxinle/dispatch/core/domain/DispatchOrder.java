package com.nongxinle.dispatch.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 待派单业务订单单元（可聚合为同一站点）。 */
@Getter
@Setter
public class DispatchOrder {

    private Integer orderId;
    private Integer addressId;
    private String sandboxStopKey;
    private String customerLabel;
    private String goodsSummary;
    private Integer orderCount;
    private List<Integer> orderIds = new ArrayList<Integer>();
    private Integer suggestedDriverUserId;
    private String serviceDate;
    private String serviceTimeLabel;
}
