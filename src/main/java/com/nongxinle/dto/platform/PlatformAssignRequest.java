package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@ToString
public class PlatformAssignRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer marketId;
    private Integer orderId;
    private Integer disGoodsId;
    /** ORDER_ONLY | ORDER_AND_DEFAULT */
    private String switchScope;
    private String reasonCode;
    private String reasonNote;
    private Integer operatorId;
}
