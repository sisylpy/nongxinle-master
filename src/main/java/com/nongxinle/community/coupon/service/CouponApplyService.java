package com.nongxinle.community.coupon.service;

import com.nongxinle.dto.MiniappApplyCouponRequest;
import com.nongxinle.dto.pos.PosApplyCouponRequest;
import com.nongxinle.entity.NxCommunityOrdersEntity;

import java.util.Map;

public interface CouponApplyService {

    Integer applyToOrder(PosApplyCouponRequest request);

    Map<String, Object> applyToMiniappOrder(MiniappApplyCouponRequest request);

    void verifyCouponOnPaymentSuccess(NxCommunityOrdersEntity order);

    /**
     * 未支付订单放弃支付/取消时，将 status=1 的券解锁回 0，并恢复订单原价。
     * 已支付或已核销的订单/券不做处理（幂等）。
     */
    void unlockLockedCouponForOrder(Integer orderId, Integer customerUserId);
}
