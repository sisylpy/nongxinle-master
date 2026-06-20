package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformCartLineItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxOrderId;
    private Integer gbOrderId;
    private Integer nxGoodsId;
    private Integer nxDistributerId;
    private Integer nxDistributerGoodsId;
    private String goodsName;
    private String quantity;
    private String standard;
    /** 价格是否已知：CONFIRMED / PENDING（与 assignStatus 无关） */
    private String priceConfirmStatus;
    private String lineSubtotal;
}
