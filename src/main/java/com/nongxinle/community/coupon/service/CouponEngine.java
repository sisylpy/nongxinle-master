package com.nongxinle.community.coupon.service;

import com.nongxinle.dto.coupon.CartSnapshot;
import com.nongxinle.dto.coupon.CouponResult;

public interface CouponEngine {

    CouponResult evaluate(CartSnapshot snapshot, String channel);
}
