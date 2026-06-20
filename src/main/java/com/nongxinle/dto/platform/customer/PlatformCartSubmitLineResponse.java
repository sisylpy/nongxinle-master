package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformCartSubmitLineResponse implements Serializable {
    private static final long serialVersionUID = 1L;

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
