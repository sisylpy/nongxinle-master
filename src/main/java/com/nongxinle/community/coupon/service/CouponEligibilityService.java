package com.nongxinle.community.coupon.service;

import com.nongxinle.dto.coupon.CouponEligibilityRequest;
import com.nongxinle.dto.coupon.CouponEligibilityResult;
import com.nongxinle.dto.coupon.CartLineSnapshot;

import java.util.List;

public interface CouponEligibilityService {

    CouponEligibilityResult evaluate(CouponEligibilityRequest request);

    List<CouponEligibilityResult> evaluateForMember(Integer customerUserId, Integer communityId,
                                                    Integer orderId, String channel);

    List<CouponEligibilityResult> evaluateForMemberWithCart(Integer customerUserId, Integer communityId,
                                                            List<CartLineSnapshot> cartLines, String channel);
}
