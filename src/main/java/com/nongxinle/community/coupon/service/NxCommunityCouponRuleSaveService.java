package com.nongxinle.community.coupon.service;

import com.nongxinle.entity.NxCommunityCouponEntity;

/**
 * 规则券保存主权：校验、归一、落库均在此服务完成（前台 saveRuleCoupon 与内部服务间调用共用）。
 */
public interface NxCommunityCouponRuleSaveService {

    NxCommunityCouponEntity saveRuleCoupon(NxCommunityCouponEntity coupon);
}
