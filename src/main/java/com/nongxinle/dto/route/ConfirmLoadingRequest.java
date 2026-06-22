package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ConfirmLoadingRequest {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private Integer operatorUserId;
    /** 默认 true：装车确认时将 nx_do_purchase_status 从 4 写入 5 */
    private Boolean updatePurchaseStatus;
}
