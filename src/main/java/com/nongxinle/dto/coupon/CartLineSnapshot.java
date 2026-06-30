package com.nongxinle.dto.coupon;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartLineSnapshot {

    private Integer goodsId;
    private Integer categoryId;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
