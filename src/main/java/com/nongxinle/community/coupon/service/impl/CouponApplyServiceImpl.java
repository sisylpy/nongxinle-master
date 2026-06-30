package com.nongxinle.community.coupon.service.impl;

import com.nongxinle.dao.NxCommunityCouponVerifyLogDao;
import com.nongxinle.dto.MiniappApplyCouponRequest;
import com.nongxinle.dto.coupon.CartPriceResult;
import com.nongxinle.dto.coupon.CouponEligibilityResult;
import com.nongxinle.dto.pos.PosApplyCouponRequest;
import com.nongxinle.entity.NxCommunityCouponEntity;
import com.nongxinle.entity.NxCommunityCouponVerifyLogEntity;
import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.entity.NxCustomerUserCouponEntity;
import com.nongxinle.community.cart.service.CartPricingService;
import com.nongxinle.community.coupon.service.CouponApplyService;
import com.nongxinle.community.coupon.service.NxCommunityCouponService;
import com.nongxinle.community.coupon.service.NxCustomerUserCouponService;
import com.nongxinle.community.order.service.NxCommunityOrdersService;
import com.nongxinle.community.cart.service.NxCommunityOrdersSubService;
import com.nongxinle.community.cart.helper.CartPricingSupport;
import com.nongxinle.utils.CouponConsistencyValidator;
import com.nongxinle.utils.CouponLabelUtils;
import com.nongxinle.utils.CouponRuleConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatYearDayTime;
import static com.nongxinle.community.pos.NxCommunityPosConstants.*;

@Service("couponApplyService")
public class CouponApplyServiceImpl implements CouponApplyService {

    @Autowired
    private NxCommunityOrdersService nxCommunityOrdersService;
    @Autowired
    private NxCustomerUserCouponService nxCustomerUserCouponService;
    @Autowired
    private NxCommunityCouponVerifyLogDao nxCommunityCouponVerifyLogDao;
    @Autowired
    private NxCommunityCouponService nxCommunityCouponService;
    @Autowired
    private CartPricingService cartPricingService;
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;

    @Override
    @Transactional
    public Integer applyToOrder(PosApplyCouponRequest request) {
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        if (request.getUserCouponId() == null) {
            throw new IllegalArgumentException("userCouponId 不能为空");
        }

        NxCommunityOrdersEntity order = nxCommunityOrdersService.queryObject(request.getOrderId());
        assertPosOrderForApply(order);

        NxCustomerUserCouponEntity userCoupon =
                nxCustomerUserCouponService.queryUserCouponDetail(request.getUserCouponId());
        if (userCoupon == null) {
            throw new IllegalArgumentException("用户券不存在");
        }

        CartPriceResult pricing = priceOrderForApply(order, userCoupon.getNxCucCustomerUserId(),
                CouponRuleConstants.CHANNEL_POS);
        CartPricingSupport.requireAvailableCoupon(pricing, request.getUserCouponId());
        CouponEligibilityResult selected = CartPricingSupport.findCoupon(pricing, request.getUserCouponId());

        lockCouponOnOrder(order, request.getUserCouponId(), pricing, selected, request.getOperatorId(), false);
        return order.getNxCommunityOrdersId();
    }

    @Override
    @Transactional
    public Map<String, Object> applyToMiniappOrder(MiniappApplyCouponRequest request) {
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        if (request.getUserCouponId() == null) {
            throw new IllegalArgumentException("userCouponId 不能为空");
        }
        if (request.getCustomerUserId() == null) {
            throw new IllegalArgumentException("customerUserId 不能为空");
        }
        if (request.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }

        NxCommunityOrdersEntity order = nxCommunityOrdersService.queryObject(request.getOrderId());
        assertMiniappOrderForApply(order, request.getCustomerUserId(), request.getCommunityId());

        NxCustomerUserCouponEntity userCoupon = nxCustomerUserCouponService.queryUserCouponDetail(request.getUserCouponId());
        if (userCoupon == null) {
            throw new IllegalArgumentException("用户券不存在");
        }
        if (!request.getCustomerUserId().equals(userCoupon.getNxCucCustomerUserId())) {
            throw new IllegalArgumentException("用户券不属于当前用户");
        }
        if (!Integer.valueOf(COUPON_STATUS_AVAILABLE).equals(userCoupon.getNxCucStatus())) {
            throw new IllegalStateException("优惠券当前不可用");
        }

        CartPriceResult pricing = priceOrderForApply(order, request.getCustomerUserId(),
                CouponRuleConstants.CHANNEL_MINIAPP);
        CartPricingSupport.requireAvailableCoupon(pricing, request.getUserCouponId());
        CouponEligibilityResult selected = CartPricingSupport.findCoupon(pricing, request.getUserCouponId());

        lockCouponOnOrder(order, request.getUserCouponId(), pricing, selected, null, true);

        Map<String, Object> data = buildApplyResponse(order, pricing);
        data.put("orderId", order.getNxCommunityOrdersId());
        return data;
    }

    @Override
    @Transactional
    public void verifyCouponOnPaymentSuccess(NxCommunityOrdersEntity order) {
        if (order == null || order.getNxCoUserCouponId() == null) {
            return;
        }
        NxCustomerUserCouponEntity userCoupon = nxCustomerUserCouponService.getUserCouponById(order.getNxCoUserCouponId());
        if (userCoupon == null || Integer.valueOf(COUPON_STATUS_VERIFIED).equals(userCoupon.getNxCucStatus())) {
            return;
        }
        if (!Integer.valueOf(COUPON_STATUS_LOCKED).equals(userCoupon.getNxCucStatus())) {
            return;
        }
        int before = userCoupon.getNxCucStatus();
        userCoupon.setNxCucStatus(COUPON_STATUS_VERIFIED);
        nxCustomerUserCouponService.update(userCoupon);
        incrementCouponUseCount(userCoupon.getNxCucCouponId());
        saveVerifyLog(userCoupon, VERIFY_ORDER_PAID, order.getNxCommunityOrdersId(),
                order.getNxCoDeskId(), null, before, COUPON_STATUS_VERIFIED);
    }

    @Override
    @Transactional
    public void unlockLockedCouponForOrder(Integer orderId, Integer customerUserId) {
        if (orderId == null) {
            return;
        }
        NxCommunityOrdersEntity order = nxCommunityOrdersService.queryObject(orderId);
        if (order == null || order.getNxCoUserCouponId() == null) {
            return;
        }
        if (customerUserId != null && !customerUserId.equals(order.getNxCoUserId())) {
            throw new IllegalArgumentException("订单不属于当前用户");
        }
        if (Integer.valueOf(ORDER_STATUS_PAID).equals(order.getNxCoStatus())
                || Integer.valueOf(PAYMENT_STATUS_PAID).equals(order.getNxCoPaymentStatus())) {
            return;
        }

        NxCustomerUserCouponEntity userCoupon =
                nxCustomerUserCouponService.getUserCouponById(order.getNxCoUserCouponId());
        if (userCoupon == null || !Integer.valueOf(COUPON_STATUS_LOCKED).equals(userCoupon.getNxCucStatus())) {
            return;
        }

        int before = userCoupon.getNxCucStatus();
        userCoupon.setNxCucStatus(COUPON_STATUS_AVAILABLE);
        userCoupon.setNxCucOrderId(null);
        nxCustomerUserCouponService.update(userCoupon);

        CartPriceResult pricing = priceOrderForApply(order, resolvePricingUserId(order, customerUserId),
                resolveOrderChannel(order));
        BigDecimal restoredTotal = pricing.getPayableTotal() == null
                ? BigDecimal.ZERO : pricing.getPayableTotal();
        if (!ORDER_CHANNEL_POS.equals(order.getNxCoOrderChannel())) {
            restoredTotal = restoredTotal.add(resolveExtraAmount(order));
        }
        order.setNxCoUserCouponId(null);
        order.setNxCoYouhuiTotal("0");
        order.setNxCoTotal(restoredTotal.setScale(2, RoundingMode.HALF_UP).toPlainString());
        nxCommunityOrdersService.update(order);

        saveVerifyLog(userCoupon, VERIFY_UNLOCK, orderId,
                order.getNxCoDeskId(), null, before, COUPON_STATUS_AVAILABLE);
    }

    private CartPriceResult priceOrderForApply(NxCommunityOrdersEntity order, Integer customerUserId, String channel) {
        if (order == null || order.getNxCoCommunityId() == null || customerUserId == null || customerUserId <= 0) {
            return CartPricingSupport.emptyPricing();
        }
        java.util.function.Supplier<CartPriceResult> supplier = () -> cartPricingService.recalculateFromSnapshot(
                nxCommunityOrdersSubService.buildCartSnapshotFromOrder(
                        order.getNxCoCommunityId(), customerUserId, order.getNxCommunityOrdersId()),
                channel);
        CartPriceResult first = supplier.get();
        CartPriceResult second = supplier.get();
        CouponConsistencyValidator.assertEqual(first, second);
        return first;
    }

    private Integer resolvePricingUserId(NxCommunityOrdersEntity order, Integer customerUserId) {
        if (customerUserId != null && customerUserId > 0) {
            return customerUserId;
        }
        return order.getNxCoUserId();
    }

    private String resolveOrderChannel(NxCommunityOrdersEntity order) {
        if (ORDER_CHANNEL_POS.equals(order.getNxCoOrderChannel())) {
            return CouponRuleConstants.CHANNEL_POS;
        }
        return CouponRuleConstants.CHANNEL_MINIAPP;
    }

    private void assertPosOrderForApply(NxCommunityOrdersEntity order) {
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!ORDER_CHANNEL_POS.equals(order.getNxCoOrderChannel())) {
            throw new IllegalArgumentException("非 POS 订单");
        }
        assertUnpaidOrderWithoutCoupon(order);
    }

    private void assertMiniappOrderForApply(NxCommunityOrdersEntity order, Integer customerUserId, Integer communityId) {
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (ORDER_CHANNEL_POS.equals(order.getNxCoOrderChannel())) {
            throw new IllegalArgumentException("POS 订单请使用 POS 用券接口");
        }
        if (!customerUserId.equals(order.getNxCoUserId())) {
            throw new IllegalArgumentException("订单不属于当前用户");
        }
        if (!communityId.equals(order.getNxCoCommunityId())) {
            throw new IllegalArgumentException("订单门店不匹配");
        }
        assertUnpaidOrderWithoutCoupon(order);
    }

    private void assertUnpaidOrderWithoutCoupon(NxCommunityOrdersEntity order) {
        if (order.getNxCoStatus() != null && order.getNxCoStatus() >= ORDER_STATUS_PAID) {
            throw new IllegalStateException("仅未支付订单可使用优惠券");
        }
        if (order.getNxCoPaymentStatus() != null && Integer.valueOf(PAYMENT_STATUS_PAID).equals(order.getNxCoPaymentStatus())) {
            throw new IllegalStateException("订单已支付，不可使用优惠券");
        }
        if (order.getNxCoUserCouponId() != null) {
            throw new IllegalStateException("订单已应用优惠券");
        }
    }

    private void lockCouponOnOrder(NxCommunityOrdersEntity order, Integer userCouponId,
                                   CartPriceResult pricing, CouponEligibilityResult selected,
                                   Integer operatorId, boolean includeExtras) {
        NxCustomerUserCouponEntity userCoupon = nxCustomerUserCouponService.queryUserCouponDetail(userCouponId);
        int before = userCoupon.getNxCucStatus();
        userCoupon.setNxCucStatus(COUPON_STATUS_LOCKED);
        userCoupon.setNxCucOrderId(order.getNxCommunityOrdersId());
        nxCustomerUserCouponService.update(userCoupon);

        BigDecimal goodsSubtotal = pricing.getGoodsSubtotal() == null
                ? BigDecimal.ZERO : pricing.getGoodsSubtotal();
        BigDecimal discount = selected.getDiscountAmount() == null
                ? BigDecimal.ZERO : selected.getDiscountAmount();
        discount = discount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal payable = goodsSubtotal.subtract(discount).max(BigDecimal.ZERO);
        if (includeExtras) {
            payable = payable.add(resolveExtraAmount(order));
        }
        payable = payable.setScale(2, RoundingMode.HALF_UP);

        order.setNxCoUserCouponId(userCoupon.getNxCustomerUserCouponId());
        order.setNxCoYouhuiTotal(discount.toPlainString());
        order.setNxCoTotal(payable.toPlainString());
        nxCommunityOrdersService.update(order);

        saveVerifyLog(userCoupon, VERIFY_ORDER_LOCK, order.getNxCommunityOrdersId(),
                order.getNxCoDeskId(), operatorId, before, COUPON_STATUS_LOCKED);
    }

    private BigDecimal resolveExtraAmount(NxCommunityOrdersEntity order) {
        BigDecimal extras = BigDecimal.ZERO;
        if (order.getNxCoDeliveryFee() != null && !order.getNxCoDeliveryFee().isEmpty()) {
            extras = extras.add(new BigDecimal(order.getNxCoDeliveryFee()));
        }
        if (order.getNxCoBuyMemberCardSubtotal() != null && !order.getNxCoBuyMemberCardSubtotal().isEmpty()) {
            extras = extras.add(new BigDecimal(order.getNxCoBuyMemberCardSubtotal()));
        }
        return extras.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> buildApplyResponse(NxCommunityOrdersEntity order, CartPriceResult pricing) {
        Map<String, Object> data = new HashMap<>();
        if (pricing != null) {
            data.putAll(pricing.toPricingFields());
        }
        data.put("nxCoTotal", order.getNxCoTotal());
        data.put("appliedCoupon", buildAppliedCouponDto(order));
        return data;
    }

    private Map<String, Object> buildAppliedCouponDto(NxCommunityOrdersEntity order) {
        if (order.getNxCoUserCouponId() == null) {
            return null;
        }
        NxCustomerUserCouponEntity userCoupon = nxCustomerUserCouponService.getUserCouponById(order.getNxCoUserCouponId());
        if (userCoupon == null) {
            return null;
        }
        NxCommunityCouponEntity template = userCoupon.getNxCommunityCouponEntity();
        if (template == null && userCoupon.getNxCucCouponId() != null) {
            template = nxCommunityCouponService.queryObject(userCoupon.getNxCucCouponId());
        }
        Map<String, Object> applied = new HashMap<>();
        applied.put("userCouponId", order.getNxCoUserCouponId());
        applied.put("couponName", template != null ? template.getNxCommunityCouponName() : "");
        if (template != null) {
            applied.put("couponTypeLabel", CouponLabelUtils.couponTypeLabel(template.getCouponType()));
            applied.put("thresholdLabel", CouponLabelUtils.thresholdLabel(template));
            applied.put("scopeLabel", CouponLabelUtils.scopeLabel(template.getScopeType()));
        }
        applied.put("discountAmount", order.getNxCoYouhuiTotal());
        return applied;
    }

    private void incrementCouponUseCount(Integer couponId) {
        NxCommunityCouponEntity template = nxCommunityCouponService.queryObject(couponId);
        if (template != null) {
            int count = template.getNxCpUseCount() == null ? 0 : template.getNxCpUseCount();
            template.setNxCpUseCount(count + 1);
            nxCommunityCouponService.update(template);
        }
    }

    private void saveVerifyLog(NxCustomerUserCouponEntity userCoupon, String type, Integer orderId,
                               Integer deskId, Integer operatorId, int before, int after) {
        NxCommunityCouponVerifyLogEntity log = new NxCommunityCouponVerifyLogEntity();
        log.setNxCvlUserCouponId(userCoupon.getNxCustomerUserCouponId());
        log.setNxCvlCouponId(userCoupon.getNxCucCouponId());
        log.setNxCvlCommunityId(userCoupon.getNxCucCommunityId());
        log.setNxCvlVerifyType(type);
        log.setNxCvlOrderId(orderId);
        log.setNxCvlDeskId(deskId);
        log.setNxCvlOperatorId(operatorId);
        log.setNxCvlBeforeStatus(before);
        log.setNxCvlAfterStatus(after);
        log.setNxCvlCreateAt(formatWhatYearDayTime(0));
        nxCommunityCouponVerifyLogDao.save(log);
    }
}
