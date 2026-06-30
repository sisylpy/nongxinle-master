package com.nongxinle.dto.coupon;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CartSnapshot {

    private Integer communityId;
    private Integer userId;
    private Integer orderType;
    private Integer serviceType;
    private Integer spId;
    private List<CartLineSnapshot> lines = new ArrayList<>();
}
