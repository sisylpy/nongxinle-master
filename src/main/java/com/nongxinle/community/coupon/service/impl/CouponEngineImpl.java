package com.nongxinle.community.coupon.service.impl;

import com.nongxinle.dto.coupon.CartSnapshot;
import com.nongxinle.dto.coupon.CouponEligibilityResult;
import com.nongxinle.dto.coupon.CouponResult;
import com.nongxinle.community.coupon.service.CouponEligibilityService;
import com.nongxinle.community.coupon.service.CouponEngine;
import com.nongxinle.utils.CartSnapshotHasher;
import com.nongxinle.utils.CouponRuleConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service("couponEngine")
public class CouponEngineImpl implements CouponEngine {

    private final ConcurrentHashMap<String, CouponResult> resultCache = new ConcurrentHashMap<>();

    @Autowired
    private CouponEligibilityService couponEligibilityService;

    @Override
    public CouponResult evaluate(CartSnapshot snapshot, String channel) {
        CouponResult result = CouponResult.empty();
        if (snapshot == null || snapshot.getUserId() == null || snapshot.getUserId() <= 0
                || snapshot.getCommunityId() == null || snapshot.getLines() == null
                || snapshot.getLines().isEmpty()) {
            return result;
        }

        String useChannel = channel == null || channel.trim().isEmpty()
                ? CouponRuleConstants.CHANNEL_MINIAPP : channel.trim().toUpperCase();
        String cacheKey = CartSnapshotHasher.hash(snapshot) + '|' + useChannel;
        CouponResult cached = resultCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<CouponEligibilityResult> all = couponEligibilityService.evaluateForMemberWithCart(
                snapshot.getUserId(), snapshot.getCommunityId(), snapshot.getLines(), useChannel);

        List<CouponEligibilityResult> available = new ArrayList<>();
        List<CouponEligibilityResult> unavailable = new ArrayList<>();
        for (CouponEligibilityResult item : all) {
            if (item.isAvailable()) {
                available.add(item);
            } else {
                unavailable.add(item);
            }
        }
        result.setAvailableCoupons(available);
        result.setUnavailableCoupons(unavailable);

        CouponEligibilityResult best = pickBestCoupon(available);
        result.setBestCoupon(best);
        if (best != null && best.getDiscountAmount() != null) {
            result.setDiscountSubtotal(best.getDiscountAmount().setScale(2, RoundingMode.HALF_UP));
        }

        resultCache.put(cacheKey, result);
        return result;
    }

    private CouponEligibilityResult pickBestCoupon(List<CouponEligibilityResult> available) {
        if (available == null || available.isEmpty()) {
            return null;
        }
        return available.stream()
                .max(Comparator
                        .comparing((CouponEligibilityResult r) ->
                                r.getDiscountAmount() == null ? BigDecimal.ZERO : r.getDiscountAmount())
                        .thenComparing(r -> {
                            BigDecimal threshold = r.getThresholdAmount();
                            return threshold == null ? BigDecimal.ZERO : threshold.negate();
                        })
                        .thenComparing(r -> r.getExpireTime() == null ? "9999-99-99" : r.getExpireTime(),
                                Comparator.reverseOrder()))
                .orElse(null);
    }
}
