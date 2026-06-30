package com.nongxinle.community.coupon.service.impl;

import com.nongxinle.entity.NxCommunityCouponEntity;
import com.nongxinle.community.coupon.service.NxCommunityCouponRuleSaveService;
import com.nongxinle.community.coupon.service.NxCommunityCouponService;
import com.nongxinle.utils.CouponRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("nxCommunityCouponRuleSaveService")
public class NxCommunityCouponRuleSaveServiceImpl implements NxCommunityCouponRuleSaveService {

    @Autowired
    private NxCommunityCouponService nxCommunityCouponService;

    @Override
    public NxCommunityCouponEntity saveRuleCoupon(NxCommunityCouponEntity coupon) {
        CouponRuleValidator.normalizeDefaults(coupon);
        CouponRuleValidator.validateRuleCoupon(coupon);
        if (coupon.getNxCpStatus() == null) {
            coupon.setNxCpStatus(0);
        }
        if (coupon.getNxCpDownCount() == null) {
            coupon.setNxCpDownCount(0);
        }
        if (coupon.getNxCpUseCount() == null) {
            coupon.setNxCpUseCount(0);
        }
        CouponRuleValidator.applyFixedDateTimeZones(coupon);
        nxCommunityCouponService.save(coupon);
        return coupon;
    }
}
