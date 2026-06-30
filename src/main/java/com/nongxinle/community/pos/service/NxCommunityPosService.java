package com.nongxinle.community.pos.service;

import com.nongxinle.dto.pos.*;

import java.util.Map;

public interface NxCommunityPosService {

    Map<String, Object> bootstrap(PosBootstrapRequest request);

    Map<String, Object> menuList(Integer communityId);

    Map<String, Object> saveOrder(PosSaveOrderRequest request);

    Map<String, Object> orderDetail(Integer orderId);

    Map<String, Object> orderDetail(Integer orderId, Integer customerUserId);

    Map<String, Object> orderListToday(Integer communityId, Integer deskId, String status, Integer page, Integer limit);

    /** 桌台当前待支付订单（无则返回 null） */
    Map<String, Object> getActiveOrderByDesk(Integer communityId, Integer deskId, Integer customerUserId);

    Map<String, Object> transferDesk(PosTransferDeskRequest request);

    Map<String, Object> clearDesk(PosClearDeskRequest request);

    Map<String, Object> couponLookup(PosCouponLookupRequest request);

    Map<String, Object> memberSearch(PosMemberSearchRequest request);

    Map<String, Object> memberCoupons(PosMemberCouponsRequest request);

    Map<String, Object> couponVerify(PosCouponVerifyRequest request);

    Map<String, Object> applyCoupon(PosApplyCouponRequest request);

    Map<String, Object> createPayment(PosPaymentCreateRequest request);

    Map<String, Object> settleZero(PosSettleZeroRequest request);

    Map<String, Object> paymentStatus(Integer paymentId);

    Map<String, Object> cancelPayment(PosPaymentCancelRequest request);

    String handleWechatNotify(String notifyXml);

    String handleAlipayNotify(Map<String, String> params);
}
