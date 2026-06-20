package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformCustomerHomeSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer supplierCount;
    private Integer categoryCount;
    private Integer goodsCount;
}
