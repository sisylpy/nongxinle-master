package com.nongxinle.dto.coupon;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CouponEligibilityRequest {

    private Integer userCouponId;
    private Integer communityId;
    private Integer orderId;
    /** 购物车快照行；与 orderId 二选一，优先使用 cartLines */
    private List<CartLineSnapshot> cartLines;
    /** POS | MINIAPP，第一阶段默认 POS */
    private String channel = "POS";
    /** 为 true 时返回 unavailableReason；为 false 时遇不可用直接短路 */
    private boolean includeUnavailable = true;
}
