package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BillPrintedOrderRef {
    private Integer liveOrderId;
    private Integer historyOrderId;
    private Integer depFatherId;
    private Integer disId;
}
