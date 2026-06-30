package com.nongxinle.community.cart.service.impl;

import com.nongxinle.dto.coupon.CartLineSnapshot;
import com.nongxinle.dto.coupon.CartPriceResult;
import com.nongxinle.dto.coupon.CartPricingRequest;
import com.nongxinle.dto.coupon.CartSnapshot;
import com.nongxinle.dto.coupon.CouponResult;
import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.community.cart.service.CartPricingService;
import com.nongxinle.community.coupon.service.CouponEngine;
import com.nongxinle.community.catalog.service.NxCommunityGoodsService;
import com.nongxinle.community.cart.service.NxCommunityOrdersSubService;
import com.nongxinle.community.cart.helper.CartPricingSupport;
import com.nongxinle.utils.CouponRuleConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service("cartPricingService")
public class CartPricingServiceImpl implements CartPricingService {

    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;
    @Autowired
    private CouponEngine couponEngine;

    @Override
    public CartPriceResult recalculate(CartPricingRequest request) {
        if (request.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("items 不能为空");
        }

        CartSnapshot snapshot = new CartSnapshot();
        snapshot.setCommunityId(request.getCommunityId());
        snapshot.setUserId(request.getUserId());
        snapshot.setLines(buildPosCartLines(request));
        return recalculateFromSnapshot(snapshot, CouponRuleConstants.CHANNEL_POS);
    }

    @Override
    public CartPriceResult recalculateFromSnapshot(CartSnapshot snapshot, String channel) {
        if (snapshot == null) {
            return CartPricingSupport.emptyPricing();
        }
        List<CartLineSnapshot> lines = snapshot.getLines() == null
                ? new ArrayList<>() : snapshot.getLines();
        BigDecimal goodsSubtotal = sumSubtotal(lines);

        CartPriceResult result = new CartPriceResult();
        result.setCartLines(lines);
        result.setGoodsSubtotal(goodsSubtotal);
        result.setDiscountSubtotal(BigDecimal.ZERO);
        result.setPayableTotal(goodsSubtotal);

        if (snapshot.getUserId() == null || snapshot.getUserId() <= 0 || lines.isEmpty()) {
            return result;
        }

        CouponResult couponResult = couponEngine.evaluate(snapshot, channel);
        applyCouponResult(result, goodsSubtotal, couponResult);
        return result;
    }

    @Override
    public CartPriceResult recalculateMiniappCart(Integer communityId, Integer userId,
                                                  Integer orderType, Integer serviceType, Integer spId) {
        if (communityId == null || userId == null || userId <= 0) {
            return CartPricingSupport.emptyPricing();
        }
        CartSnapshot snapshot = nxCommunityOrdersSubService.buildCartSnapshot(
                communityId, userId, orderType, serviceType, spId);
        return recalculateFromSnapshot(snapshot, CouponRuleConstants.CHANNEL_MINIAPP);
    }

    @Override
    public CartPriceResult recalculateFromDraftSubOrders(Integer communityId, Integer customerUserId, String channel) {
        return recalculateFromDraftSubOrders(communityId, customerUserId, 0, null, -1, channel);
    }

    @Override
    public CartPriceResult recalculateFromDraftSubOrders(Integer communityId, Integer customerUserId,
                                                         Integer orderType, Integer serviceType, Integer spId,
                                                         String channel) {
        if (!CouponRuleConstants.CHANNEL_MINIAPP.equalsIgnoreCase(channel)) {
            CartSnapshot snapshot = nxCommunityOrdersSubService.buildCartSnapshot(
                    communityId, customerUserId, orderType, serviceType, spId);
            return recalculateFromSnapshot(snapshot, channel);
        }
        return recalculateMiniappCart(communityId, customerUserId, orderType, serviceType, spId);
    }

    private void applyCouponResult(CartPriceResult result, BigDecimal goodsSubtotal, CouponResult couponResult) {
        if (couponResult == null) {
            return;
        }
        result.setAvailableCoupons(couponResult.getAvailableCoupons());
        result.setUnavailableCoupons(couponResult.getUnavailableCoupons());
        result.setBestCoupon(couponResult.getBestCoupon());
        if (couponResult.getDiscountSubtotal() != null
                && couponResult.getDiscountSubtotal().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = couponResult.getDiscountSubtotal().setScale(2, RoundingMode.HALF_UP);
            result.setDiscountSubtotal(discount);
            BigDecimal payable = goodsSubtotal.subtract(discount).max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
            result.setPayableTotal(payable);
        }
    }

    private List<CartLineSnapshot> buildPosCartLines(CartPricingRequest request) {
        List<CartLineSnapshot> lines = new ArrayList<>();
        for (CartPricingRequest.CartLineInput item : request.getItems()) {
            if (item.getGoodsId() == null) {
                throw new IllegalArgumentException("goodsId 不能为空");
            }
            NxCommunityGoodsEntity goods = nxCommunityGoodsService.queryObject(item.getGoodsId());
            if (goods == null) {
                throw new IllegalArgumentException("商品不存在: " + item.getGoodsId());
            }
            if (!request.getCommunityId().equals(goods.getNxCgCommunityId())) {
                throw new IllegalArgumentException("商品不属于当前门店: " + item.getGoodsId());
            }
            int qty = item.getQuantity() == null || item.getQuantity() < 1 ? 1 : item.getQuantity();
            BigDecimal price = new BigDecimal(goods.getNxCgGoodsPrice());
            BigDecimal subtotal = price.multiply(new BigDecimal(qty)).setScale(2, RoundingMode.HALF_UP);

            CartLineSnapshot line = new CartLineSnapshot();
            line.setGoodsId(goods.getNxCommunityGoodsId());
            line.setCategoryId(goods.getNxCgCfgGoodsFatherId());
            line.setPrice(price);
            line.setQuantity(qty);
            line.setSubtotal(subtotal);
            lines.add(line);
        }
        return lines;
    }

    private BigDecimal sumSubtotal(List<CartLineSnapshot> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartLineSnapshot line : lines) {
            if (line.getSubtotal() != null) {
                total = total.add(line.getSubtotal());
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
