package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlatformCartLineCreateResult {

    private Integer gbOrderId;
    private Integer nxOrderId;
    private Integer platformAssignId;
    private Integer fulfillmentId;
    private String goodsName;
    private String quantity;
    private String standard;
    private String priceConfirmStatus;
    private String lineSubtotal;
}
