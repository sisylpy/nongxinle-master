package com.nongxinle.community.pos.service.impl;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.nongxinle.dao.NxCommunityCouponVerifyLogDao;
import com.nongxinle.dao.NxCommunityPosPaymentDao;
import com.nongxinle.dto.coupon.CartPriceResult;
import com.nongxinle.dto.coupon.CouponEligibilityResult;
import com.nongxinle.dto.pos.*;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.community.ecommerce.service.NxECommerceCommunityService;
import com.nongxinle.community.coupon.service.NxCommunityCouponService;
import com.nongxinle.community.coupon.service.NxCustomerUserCouponService;
import com.nongxinle.community.coupon.service.CouponApplyService;
import com.nongxinle.community.pos.service.NxCommunityDeskService;
import com.nongxinle.community.pos.service.NxCommunityPosService;
import com.nongxinle.community.pos.service.PosDeskBindingService;
import com.nongxinle.community.pos.service.DeskOrderReconcileService;
import com.nongxinle.community.cart.service.CartPricingService;
import com.nongxinle.community.cart.service.NxCommunityOrdersSubService;
import com.nongxinle.community.order.service.NxCommunityOrdersService;
import com.nongxinle.community.customer.service.NxCommunityUserService;
import com.nongxinle.community.customer.service.NxCustomerUserService;
import com.nongxinle.community.catalog.service.NxCommunityGoodsService;
import com.nongxinle.community.catalog.service.NxCommunityFatherGoodsService;
import com.nongxinle.community.core.service.NxCommunityService;
import com.nongxinle.community.cart.helper.CartPricingSupport;
import com.nongxinle.utils.CouponLabelUtils;
import com.nongxinle.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.DateUtils.formatWhatYearDayTime;
import static com.nongxinle.community.pos.NxCommunityPosConstants.*;
import static com.nongxinle.utils.NxCommunityTypeUtils.*;

@Service("nxCommunityPosService")
public class NxCommunityPosServiceImpl implements NxCommunityPosService {

    @Autowired
    private NxCommunityService nxCommunityService;
    @Autowired
    private NxCommunityUserService nxCommunityUserService;
    @Autowired
    private NxCommunityDeskService nxCommunityDeskService;
    @Autowired
    private NxCommunityFatherGoodsService nxCommunityFatherGoodsService;
    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;
    @Autowired
    private NxCommunityOrdersService nxCommunityOrdersService;
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;
    @Autowired
    private NxCustomerUserCouponService nxCustomerUserCouponService;
    @Autowired
    private NxCustomerUserService nxCustomerUserService;
    @Autowired
    private NxCommunityCouponService nxCommunityCouponService;
    @Autowired
    private NxECommerceCommunityService nxECommerceCommunityService;
    @Autowired
    private NxCommunityPosPaymentDao nxCommunityPosPaymentDao;
    @Autowired
    private NxCommunityCouponVerifyLogDao nxCommunityCouponVerifyLogDao;
    @Autowired
    private CouponApplyService couponApplyService;
    @Autowired
    private CartPricingService cartPricingService;
    @Autowired
    private PosDeskBindingService posDeskBindingService;
    @Autowired
    private DeskOrderReconcileService deskOrderReconcileService;

    @Value("${pos.wechat.notify-url:" + DEFAULT_POS_WECHAT_NOTIFY_URL + "}")
    private String posWechatNotifyUrl;

    @Override
    public Map<String, Object> bootstrap(PosBootstrapRequest request) {
        if (request.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }
        NxCommunityEntity community = nxCommunityService.queryObject(request.getCommunityId());
        if (community == null) {
            throw new IllegalArgumentException("社区不存在");
        }
        Map<String, Object> operator = new HashMap<>();
        if (request.getOperatorId() != null) {
            NxCommunityUserEntity user = nxCommunityUserService.queryObject(request.getOperatorId());
            if (user != null) {
                operator.put("operatorId", user.getNxCommunityUserId());
                operator.put("name", user.getNxCouWxNickName());
                operator.put("phone", user.getNxCouWxPhone());
            }
        }
        NxECommerceCommunityEntity ecc = nxECommerceCommunityService.queryByCommunityId(request.getCommunityId());
        Map<String, Object> store = new HashMap<>();
        store.put("communityId", community.getNxCommunityId());
        store.put("communityName", community.getNxCommunityName());
        store.put("commerceId", ecc != null ? ecc.getNxEccEId() : null);
        store.put("openTime", community.getNxCommunityOpenTime());
        store.put("closeTime", community.getNxCommunityCloseTime());

        Map<String, Object> deskMap = new HashMap<>();
        deskMap.put("commId", request.getCommunityId());
        deskMap.put("type", 0);
        List<NxCommunityDeskEntity> tables = nxCommunityDeskService.queryComDeskByParams(deskMap);
        deskMap.put("type", 1);
        List<NxCommunityDeskEntity> rooms = nxCommunityDeskService.queryComDeskByParams(deskMap);

        deskOrderReconcileService.reconcileCommunityDesks(request.getCommunityId());

        Map<String, Object> data = new HashMap<>();
        data.put("store", store);
        data.put("operator", operator);
        data.put("tables", toDeskDtoList(tables, request.getCommunityId()));
        data.put("rooms", toDeskDtoList(rooms, request.getCommunityId()));
        return data;
    }

    @Override
    public Map<String, Object> menuList(Integer communityId) {
        Map<String, Object> map = new HashMap<>();
        map.put("commId", communityId);
        List<NxCommunityFatherGoodsEntity> grands = nxCommunityFatherGoodsService.queryGrandGoodsAdmin(map);
        List<Map<String, Object>> categories = new ArrayList<>();
        if (grands != null) {
            for (NxCommunityFatherGoodsEntity grand : grands) {
                List<NxCommunityFatherGoodsEntity> level2 = grand.getFatherGoodsEntities();
                if (level2 != null) {
                    for (NxCommunityFatherGoodsEntity father : level2) {
                        appendCategory(categories, father);
                    }
                }
                appendCategory(categories, grand);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);
        return data;
    }

    @Override
    @Transactional
    public Map<String, Object> saveOrder(PosSaveOrderRequest request) {
        validateSaveOrderRequest(request);
        NxECommerceCommunityEntity ecc = nxECommerceCommunityService.queryByCommunityId(request.getCommunityId());
        if (ecc == null) {
            throw new IllegalArgumentException("社区未绑定商户");
        }

        BigDecimal goodsSubtotal = BigDecimal.ZERO;
        List<NxCommunityOrdersSubEntity> subEntities = new ArrayList<>();
        for (PosOrderItemRequest item : request.getItems()) {
            NxCommunityGoodsEntity goods = nxCommunityGoodsService.queryObject(item.getGoodsId());
            if (goods == null) {
                throw new IllegalArgumentException("商品不存在: " + item.getGoodsId());
            }
            if (!request.getCommunityId().equals(goods.getNxCgCommunityId())) {
                throw new IllegalArgumentException("商品不属于当前门店");
            }
            int qty = item.getQuantity() == null || item.getQuantity() < 1 ? 1 : item.getQuantity();
            BigDecimal price = new BigDecimal(goods.getNxCgGoodsPrice());
            BigDecimal lineSubtotal = price.multiply(new BigDecimal(qty)).setScale(2, RoundingMode.HALF_UP);
            goodsSubtotal = goodsSubtotal.add(lineSubtotal);

            NxCommunityOrdersSubEntity sub = new NxCommunityOrdersSubEntity();
            sub.setNxCosCommunityId(request.getCommunityId());
            sub.setNxCosCommerceId(ecc.getNxEccEId());
            sub.setNxCosCommunityGoodsId(goods.getNxCommunityGoodsId());
            sub.setNxCosCommunityGoodsFatherId(goods.getNxCgCfgGoodsFatherId());
            sub.setNxCosOrderUserId(POS_ORDER_USER_ID);
            sub.setNxCosDeskId(request.getDeskId());
            sub.setNxCosGoodsType(goods.getNxCgGoodsType());
            sub.setNxCosGoodsSellType(goods.getNxCgSellType());
            sub.setNxCosQuantity(String.valueOf(qty));
            sub.setNxCosStandard(goods.getNxCgGoodsStandardname());
            sub.setNxCosPrice(goods.getNxCgGoodsPrice());
            sub.setNxCosSubtotal(lineSubtotal.toPlainString());
            sub.setNxCosRemark(item.getRemark());
            sub.setNxCosStatus(SUB_STATUS_UNPAID);
            sub.setNxCosType(POS_ORDER_TYPE);
            sub.setNxCosServiceType(POS_SERVICE_TYPE);
            sub.setNxCosServiceDate(formatWhatDay(0));
            subEntities.add(sub);
        }

        boolean realDesk = isRealDeskId(request.getDeskId());
        NxCommunityDeskEntity desk = null;
        NxCommunityOrdersEntity existingOrder = null;
        if (realDesk) {
            desk = posDeskBindingService.requireDesk(request.getCommunityId(), request.getDeskId());
            existingOrder = posDeskBindingService.resolveActivePosOrder(desk, request.getCommunityId());
        }

        if (existingOrder != null) {
            for (NxCommunityOrdersSubEntity sub : subEntities) {
                sub.setNxCosOrdersId(existingOrder.getNxCommunityOrdersId());
                nxCommunityOrdersSubService.save(sub);
            }
            refreshPosOrderTotals(existingOrder, request.getUserId());
            return buildOrderDto(existingOrder.getNxCommunityOrdersId(), request.getUserId());
        }

        NxCommunityOrdersEntity order = new NxCommunityOrdersEntity();
        order.setNxCoCommunityId(request.getCommunityId());
        order.setNxCoCommerceId(ecc.getNxEccEId());
        order.setNxCoUserId(POS_ORDER_USER_ID);
        order.setNxCoDeskId(request.getDeskId());
        order.setNxCoOrderChannel(ORDER_CHANNEL_POS);
        order.setNxCoPosOperatorId(request.getOperatorId());
        order.setNxCoType(POS_ORDER_TYPE);
        order.setNxCoServiceType(POS_SERVICE_TYPE);
        order.setNxCoStatus(ORDER_STATUS_UNPAID);
        order.setNxCoPaymentStatus(PAYMENT_STATUS_UNPAID);
        order.setNxCoDate(formatWhatYearDayTime(0));
        order.setNxCoServiceDate(formatWhatDay(0));
        order.setNxCoService(request.getRemark());
        order.setNxCoTotal(goodsSubtotal.setScale(2, RoundingMode.HALF_UP).toPlainString());
        order.setNxCoYouhuiTotal("0");
        order.setNxCoWeighNumber(CommonUtils.generatePickNumber(3));
        nxCommunityOrdersService.justSave(order);

        for (NxCommunityOrdersSubEntity sub : subEntities) {
            sub.setNxCosOrdersId(order.getNxCommunityOrdersId());
            nxCommunityOrdersSubService.save(sub);
        }

        if (realDesk && desk != null) {
            posDeskBindingService.bindDeskToOrder(desk, order.getNxCommunityOrdersId());
        }

        Map<String, Object> data = buildOrderDto(order.getNxCommunityOrdersId(), request.getUserId());
        return data;
    }

    @Override
    public Map<String, Object> orderDetail(Integer orderId) {
        return orderDetail(orderId, null);
    }

    @Override
    public Map<String, Object> orderDetail(Integer orderId, Integer customerUserId) {
        try {
            NxCommunityOrdersEntity order = requirePosOrder(orderId);
            return buildOrderDto(order, customerUserId);
        } catch (IllegalArgumentException ex) {
            deskOrderReconcileService.reconcileByOrderId(orderId);
            throw ex;
        }
    }

    @Override
    public Map<String, Object> orderListToday(Integer communityId, Integer deskId, String status, Integer page, Integer limit) {
        int p = page == null || page < 1 ? 1 : page;
        int l = limit == null || limit < 1 ? 20 : limit;
        Map<String, Object> map = new HashMap<>();
        map.put("commId", communityId);
        map.put("date", formatWhatDay(0));
        map.put("orderChannel", ORDER_CHANNEL_POS);
        map.put("offset", (p - 1) * l);
        map.put("limit", l);
        if (deskId != null) {
            map.put("deskId", deskId);
        }
        if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            map.put("status", posOrderStatusFromLabel(status));
        }
        List<NxCommunityOrdersEntity> orders = nxCommunityOrdersService.queryCustomerOrder(map);
        List<Map<String, Object>> list = new ArrayList<>();
        if (orders != null) {
            for (NxCommunityOrdersEntity order : orders) {
                list.add(buildOrderSummaryDto(order));
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", p);
        data.put("limit", l);
        data.put("total", list.size());
        return data;
    }

    @Override
    public Map<String, Object> getActiveOrderByDesk(Integer communityId, Integer deskId, Integer customerUserId) {
        NxCommunityDeskEntity desk = posDeskBindingService.requireDesk(communityId, deskId);
        NxCommunityOrdersEntity order = posDeskBindingService.resolveActivePosOrder(desk, communityId);
        if (order == null) {
            return null;
        }
        return buildOrderDto(order, customerUserId);
    }

    @Override
    @Transactional
    public Map<String, Object> transferDesk(PosTransferDeskRequest request) {
        if (request.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        if (request.getFromDeskId() == null || request.getToDeskId() == null) {
            throw new IllegalArgumentException("fromDeskId / toDeskId 不能为空");
        }
        if (request.getFromDeskId().equals(request.getToDeskId())) {
            throw new IllegalArgumentException("源桌台与目标桌台不能相同");
        }

        NxCommunityOrdersEntity order = requirePosOrder(request.getOrderId());
        if (!Integer.valueOf(ORDER_STATUS_UNPAID).equals(order.getNxCoStatus())) {
            throw new IllegalArgumentException("仅待支付订单可转台");
        }
        NxCommunityDeskEntity fromDesk = posDeskBindingService.requireDesk(request.getCommunityId(), request.getFromDeskId());
        NxCommunityDeskEntity toDesk = posDeskBindingService.requireDesk(request.getCommunityId(), request.getToDeskId());
        deskOrderReconcileService.reconcileDesk(fromDesk);
        deskOrderReconcileService.reconcileDesk(toDesk);
        if (!request.getOrderId().equals(fromDesk.getNxCdCurrentOrderId())) {
            throw new IllegalArgumentException("订单与源桌台不匹配");
        }
        if (toDesk.getNxCdCurrentOrderId() != null) {
            throw new IllegalArgumentException("目标桌台已有订单");
        }

        posDeskBindingService.releaseDesk(fromDesk);
        order.setNxCoDeskId(request.getToDeskId());
        nxCommunityOrdersService.update(order);
        updateOrderSubsDeskId(order.getNxCommunityOrdersId(), request.getToDeskId());
        posDeskBindingService.bindDeskToOrder(toDesk, order.getNxCommunityOrdersId());
        deskOrderReconcileService.reconcileDesk(fromDesk);
        deskOrderReconcileService.reconcileDesk(toDesk);
        return buildOrderDto(order, null);
    }

    @Override
    @Transactional
    public Map<String, Object> clearDesk(PosClearDeskRequest request) {
        if (request.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }
        if (request.getDeskId() == null) {
            throw new IllegalArgumentException("deskId 不能为空");
        }
        NxCommunityDeskEntity desk = posDeskBindingService.requireDesk(request.getCommunityId(), request.getDeskId());
        deskOrderReconcileService.reconcileDesk(desk);
        Integer orderId = desk.getNxCdCurrentOrderId();
        if (orderId == null) {
            posDeskBindingService.releaseDesk(desk);
            Map<String, Object> data = new HashMap<>();
            data.put("deskId", desk.getNxCommunityDeskId());
            data.put("status", "FREE");
            data.put("currentOrderId", null);
            return data;
        }

        NxCommunityOrdersEntity order = requirePosOrder(orderId);
        order.setNxCoStatus(ORDER_STATUS_CANCELLED);
        nxCommunityOrdersService.update(order);
        cancelOrderSubs(orderId);
        couponApplyService.unlockLockedCouponForOrder(orderId, null);
        posDeskBindingService.releaseDesk(desk);

        Map<String, Object> data = new HashMap<>();
        data.put("deskId", desk.getNxCommunityDeskId());
        data.put("status", "FREE");
        data.put("currentOrderId", null);
        data.put("cancelledOrderId", orderId);
        return data;
    }

    @Override
    public Map<String, Object> couponLookup(PosCouponLookupRequest request) {
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        NxCustomerUserCouponEntity userCoupon = resolveUserCoupon(request.getCode());
        if (userCoupon == null) {
            throw new IllegalArgumentException("优惠券不存在");
        }
        Integer communityId = request.getCommunityId() != null
                ? request.getCommunityId() : userCoupon.getNxCucCommunityId();
        CartPriceResult pricing = PosCartPricingHelper.priceOrder(
                cartPricingService, nxCommunityOrdersSubService, communityId,
                userCoupon.getNxCucCustomerUserId(), request.getOrderId());
        CouponEligibilityResult eligibility = CartPricingSupport.findCoupon(
                pricing, userCoupon.getNxCustomerUserCouponId());
        if (eligibility == null) {
            eligibility = new CouponEligibilityResult();
            eligibility.setUserCouponId(userCoupon.getNxCustomerUserCouponId());
            eligibility.setAvailable(false);
            eligibility.setUnavailableReason("优惠券不可用");
        }
        return buildCouponLookupDto(eligibility, userCoupon);
    }

    @Override
    public Map<String, Object> memberSearch(PosMemberSearchRequest request) {
        if (request.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }
        String keyword = request.getKeyword() == null ? "" : request.getKeyword().trim();
        if (keyword.isEmpty()) {
            throw new IllegalArgumentException("keyword 不能为空");
        }
        if (!keyword.matches("\\d+")) {
            throw new IllegalArgumentException("仅支持手机号或会员编号");
        }

        boolean searchUserId = keyword.length() <= 6;
        boolean searchPhone = keyword.length() >= 4;
        if (!searchUserId && !searchPhone) {
            throw new IllegalArgumentException("请至少输入4位手机号或会员编号");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("communityId", request.getCommunityId());
        map.put("searchUserId", searchUserId);
        map.put("searchPhone", searchPhone);
        if (searchUserId) {
            map.put("userId", Integer.parseInt(keyword));
        }
        if (searchPhone) {
            map.put("phone", keyword);
        }

        List<NxCustomerUserEntity> members = nxCustomerUserService.posSearchMembersByKeyword(map);
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("会员不存在");
        }
        if (members.size() > 1) {
            throw new IllegalStateException("匹配到多个会员，请补全手机号");
        }

        NxCustomerUserEntity member = members.get(0);

        Map<String, Object> data = new HashMap<>();
        data.put("customerUserId", member.getNxCuUserId());
        data.put("name", member.getNxCuWxNickName() != null ? member.getNxCuWxNickName() : "");
        data.put("phone", member.getNxCuWxPhoneNumber() != null ? member.getNxCuWxPhoneNumber() : "");
        return data;
    }

    @Override
    public Map<String, Object> memberCoupons(PosMemberCouponsRequest request) {
        if (request.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }
        if (request.getCustomerUserId() == null) {
            throw new IllegalArgumentException("customerUserId 不能为空");
        }
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("orderId 不能为空");
        }

        NxCustomerUserEntity member = nxCustomerUserService.queryObject(request.getCustomerUserId());
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }
        assertMemberRelatedToCommunity(member, request.getCommunityId());

        CartPriceResult pricing = PosCartPricingHelper.priceOrder(
                cartPricingService, nxCommunityOrdersSubService, request.getCommunityId(),
                member.getNxCuUserId(), request.getOrderId());

        Map<String, Object> data = new HashMap<>();
        data.put("customerUserId", member.getNxCuUserId());
        data.put("orderId", request.getOrderId());
        PosCartPricingHelper.mergeIntoMap(data, pricing);
        return data;
    }

    @Override
    @Transactional
    public Map<String, Object> couponVerify(PosCouponVerifyRequest request) {
        NxCustomerUserCouponEntity userCoupon = nxCustomerUserCouponService.queryUserCouponDetail(request.getUserCouponId());
        if (userCoupon == null) {
            throw new IllegalArgumentException("用户券不存在");
        }
        if (!request.getCommunityId().equals(userCoupon.getNxCucCommunityId())) {
            throw new IllegalArgumentException("优惠券不属于当前门店");
        }
        String reason = couponUnavailableReason(userCoupon, null);
        if (reason != null) {
            throw new IllegalStateException(reason);
        }
        if (!Integer.valueOf(COUPON_STATUS_AVAILABLE).equals(userCoupon.getNxCucStatus())) {
            throw new IllegalStateException("优惠券当前不可核销");
        }

        int before = userCoupon.getNxCucStatus();
        userCoupon.setNxCucStatus(COUPON_STATUS_VERIFIED);
        nxCustomerUserCouponService.update(userCoupon);

        incrementCouponUseCount(userCoupon.getNxCucCouponId());
        Integer logId = saveVerifyLog(userCoupon, VERIFY_STANDALONE, null, null,
                request.getDeskId(), request.getOperatorId(), before, COUPON_STATUS_VERIFIED, request.getRemark());

        Map<String, Object> data = new HashMap<>();
        data.put("verifyLogId", logId);
        data.put("userCouponId", userCoupon.getNxCustomerUserCouponId());
        data.put("status", "VERIFIED");
        data.put("verifiedAt", formatWhatYearDayTime(0));
        return data;
    }

    @Override
    @Transactional
    public Map<String, Object> applyCoupon(PosApplyCouponRequest request) {
        Integer orderId = couponApplyService.applyToOrder(request);
        NxCommunityOrdersEntity order = requirePosOrder(orderId);
        return buildOrderDto(order, null);
    }

    @Override
    @Transactional
    public Map<String, Object> createPayment(PosPaymentCreateRequest request) {
        NxCommunityOrdersEntity order = requirePosOrder(request.getOrderId());
        assertOrderCanCreatePayment(order);
        String channel = request.getPayChannel() == null ? "" : request.getPayChannel().toUpperCase();
        if (!PAY_CHANNEL_WECHAT.equals(channel) && !PAY_CHANNEL_ALIPAY.equals(channel)) {
            throw new IllegalArgumentException("payChannel 仅支持 WECHAT / ALIPAY");
        }

        NxCommunityPosPaymentEntity reusable = resolveReusablePendingPayment(order.getNxCommunityOrdersId(), channel);
        if (reusable != null) {
            return buildPaymentCreateResponse(reusable, true);
        }

        BigDecimal amount = resolvePayableTotal(order);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("订单应付金额为0，无需创建微信支付，请调用 POST /api/nxcommunitypos/payment/settleZero 完成0元结算");
        }
        String outTradeNo = generatePosOutTradeNo();
        String qrCodeUrl;
        try {
            if (PAY_CHANNEL_WECHAT.equals(channel)) {
                qrCodeUrl = createWechatNativeQr(outTradeNo, amount);
                order.setNxCoWxOutTradeNo(outTradeNo);
                nxCommunityOrdersService.update(order);
            } else {
                Map<String, String> ali = PosAlipayNativeUtil.precreate(outTradeNo, amount, "POS堂食订单");
                qrCodeUrl = ali.get("qr_code");
            }
        } catch (Exception e) {
            throw new RuntimeException("创建收款码失败: " + e.getMessage(), e);
        }

        NxCommunityPosPaymentEntity payment = new NxCommunityPosPaymentEntity();
        payment.setNxPpOrderId(order.getNxCommunityOrdersId());
        payment.setNxPpCommunityId(order.getNxCoCommunityId());
        payment.setNxPpPayChannel(channel);
        payment.setNxPpOutTradeNo(outTradeNo);
        payment.setNxPpAmount(amount);
        payment.setNxPpStatus(PAY_FLOW_PENDING);
        payment.setNxPpQrCodeUrl(qrCodeUrl);
        payment.setNxPpOperatorId(order.getNxCoPosOperatorId());
        payment.setNxPpCreateAt(formatWhatYearDayTime(0));
        payment.setNxPpExpireAt(LocalDateTime.now().plusMinutes(PAYMENT_EXPIRE_MINUTES)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        nxCommunityPosPaymentDao.save(payment);

        return buildPaymentCreateResponse(payment, false);
    }

    @Override
    @Transactional
    public Map<String, Object> settleZero(PosSettleZeroRequest request) {
        NxCommunityOrdersEntity order = requirePosOrder(request.getOrderId());
        if (!Integer.valueOf(ORDER_STATUS_UNPAID).equals(order.getNxCoStatus())) {
            throw new IllegalStateException("订单不是待支付状态");
        }
        BigDecimal payable = resolvePayableTotal(order);
        if (payable.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("订单应付金额大于0，请走正常支付流程");
        }
        closePendingPayments(order.getNxCommunityOrdersId());
        Integer operatorId = request.getOperatorId() != null ? request.getOperatorId() : order.getNxCoPosOperatorId();
        completePosOrder(order, operatorId);

        Map<String, Object> data = buildOrderDto(order.getNxCommunityOrdersId());
        data.put("settleType", "ZERO");
        return data;
    }

    @Override
    @Transactional
    public Map<String, Object> paymentStatus(Integer paymentId) {
        NxCommunityPosPaymentEntity payment = nxCommunityPosPaymentDao.queryObject(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("支付单不存在");
        }
        String syncNote = null;
        if (PAY_FLOW_PENDING.equals(payment.getNxPpStatus())
                && PAY_CHANNEL_WECHAT.equals(payment.getNxPpPayChannel())) {
            syncNote = syncWechatPaymentIfPaid(payment);
            payment = nxCommunityPosPaymentDao.queryObject(paymentId);
        }
        Map<String, Object> data = buildPaymentStatusResponse(payment);
        if (syncNote != null) {
            data.put("syncNote", syncNote);
        }
        reconcileDeskIfPaymentEnded(payment);
        return data;
    }

    @Override
    @Transactional
    public Map<String, Object> cancelPayment(PosPaymentCancelRequest request) {
        NxCommunityPosPaymentEntity payment = nxCommunityPosPaymentDao.queryObject(request.getPaymentId());
        if (payment == null) {
            throw new IllegalArgumentException("支付单不存在");
        }
        if (PAY_FLOW_SUCCESS.equals(payment.getNxPpStatus())) {
            throw new IllegalStateException("支付已成功，禁止取消");
        }
        if (!PAY_FLOW_PENDING.equals(payment.getNxPpStatus())) {
            throw new IllegalStateException("仅待支付收款单可取消");
        }
        payment.setNxPpStatus(PAY_FLOW_CLOSED);
        nxCommunityPosPaymentDao.update(payment);
        reconcileDeskIfPaymentEnded(payment);
        Map<String, Object> data = new HashMap<>();
        data.put("paymentId", payment.getNxPosPaymentId());
        data.put("status", PAY_FLOW_CLOSED);
        return data;
    }

    @Override
    @Transactional
    public String handleWechatNotify(String notifyXml) {
        try {
            Map<String, String> notifyData = WXPayUtil.xmlToMap(notifyXml);
            MyWxQingqingxiangPayConfig config = new MyWxQingqingxiangPayConfig();
            if (!WXPayUtil.isSignatureValid(notifyData, config.getKey())) {
                return failXml("签名失败");
            }
            if (!"SUCCESS".equals(notifyData.get("result_code"))) {
                return failXml("支付失败");
            }
            String outTradeNo = notifyData.get("out_trade_no");
            String transactionId = notifyData.get("transaction_id");
            NxCommunityPosPaymentEntity payment = nxCommunityPosPaymentDao.queryByOutTradeNo(outTradeNo);
            if (payment != null) {
                markPaymentSuccess(payment, transactionId, notifyXml);
            }
            Map<String, String> ok = new HashMap<>();
            ok.put("return_code", "SUCCESS");
            ok.put("return_msg", "OK");
            return WXPayUtil.mapToXml(ok);
        } catch (Exception e) {
            return failXml(e.getMessage());
        }
    }

    @Override
    @Transactional
    public String handleAlipayNotify(Map<String, String> params) {
        Map<String, String> copy = new TreeMap<>(params);
        if (!PosAlipayNativeUtil.verifyNotify(copy)) {
            return "failure";
        }
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return "success";
        }
        String outTradeNo = params.get("out_trade_no");
        String transactionId = params.get("trade_no");
        NxCommunityPosPaymentEntity payment = nxCommunityPosPaymentDao.queryByOutTradeNo(outTradeNo);
        if (payment != null && PAY_FLOW_PENDING.equals(payment.getNxPpStatus())) {
            markPaymentSuccess(payment, transactionId, params.toString());
        }
        return "success";
    }

    // -------------------- internal --------------------

    @Transactional
    protected void markPaymentSuccess(NxCommunityPosPaymentEntity payment, String transactionId, String raw) {
        if (PAY_FLOW_SUCCESS.equals(payment.getNxPpStatus())) {
            return;
        }
        payment.setNxPpStatus(PAY_FLOW_SUCCESS);
        payment.setNxPpTransactionId(transactionId);
        payment.setNxPpPaidAt(formatWhatYearDayTime(0));
        payment.setNxPpNotifyRaw(raw);
        nxCommunityPosPaymentDao.update(payment);

        NxCommunityOrdersEntity order = requirePosOrder(payment.getNxPpOrderId());
        completePosOrder(order, payment.getNxPpOperatorId());
    }

    private void completePosOrder(NxCommunityOrdersEntity order, Integer operatorId) {
        if (Integer.valueOf(ORDER_STATUS_PAID).equals(order.getNxCoStatus())
                && Integer.valueOf(PAYMENT_STATUS_PAID).equals(order.getNxCoPaymentStatus())) {
            return;
        }
        order.setNxCoStatus(ORDER_STATUS_PAID);
        order.setNxCoPaymentStatus(PAYMENT_STATUS_PAID);
        // nx_CO_payment_time 为 varchar(0)，旧系统支付回调也不写入，POS 与之保持一致
        nxCommunityOrdersService.update(order);

        Map<String, Object> subMap = new HashMap<>();
        subMap.put("orderId", order.getNxCommunityOrdersId());
        List<NxCommunityOrdersSubEntity> subs = nxCommunityOrdersSubService.querySubOrdersByParams(subMap);
        if (subs != null) {
            for (NxCommunityOrdersSubEntity sub : subs) {
                sub.setNxCosStatus(SUB_STATUS_PAID);
                nxCommunityOrdersSubService.update(sub);
            }
        }
        if (order.getNxCoUserCouponId() != null) {
            verifyOrderCoupon(order.getNxCoUserCouponId(), order, operatorId);
        }

        if (order.getNxCoDeskId() != null && isRealDeskId(order.getNxCoDeskId())) {
            NxCommunityDeskEntity desk = nxCommunityDeskService.queryObject(order.getNxCoDeskId());
            if (desk != null && order.getNxCommunityOrdersId().equals(desk.getNxCdCurrentOrderId())) {
                posDeskBindingService.releaseDesk(desk);
            }
        }
    }

    private void verifyOrderCoupon(Integer userCouponId, NxCommunityOrdersEntity order, Integer operatorId) {
        NxCustomerUserCouponEntity userCoupon = nxCustomerUserCouponService.getUserCouponById(userCouponId);
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
                null, order.getNxCoDeskId(), operatorId,
                before, COUPON_STATUS_VERIFIED, null);
    }

    private void unlockOrderCoupons(NxCommunityOrdersEntity order, Integer operatorId) {
        if (order.getNxCoUserCouponId() == null) {
            return;
        }
        NxCustomerUserCouponEntity userCoupon = nxCustomerUserCouponService.getUserCouponById(order.getNxCoUserCouponId());
        if (userCoupon == null || !Integer.valueOf(COUPON_STATUS_LOCKED).equals(userCoupon.getNxCucStatus())) {
            return;
        }
        int before = userCoupon.getNxCucStatus();
        userCoupon.setNxCucStatus(COUPON_STATUS_AVAILABLE);
        userCoupon.setNxCucOrderId(null);
        nxCustomerUserCouponService.update(userCoupon);
        saveVerifyLog(userCoupon, VERIFY_UNLOCK, order.getNxCommunityOrdersId(), null,
                order.getNxCoDeskId(), operatorId, before, COUPON_STATUS_AVAILABLE, null);
        order.setNxCoUserCouponId(null);
        order.setNxCoYouhuiTotal("0");
        nxCommunityOrdersService.update(order);
    }

    private void closePendingPayments(Integer orderId) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderId);
        map.put("status", PAY_FLOW_PENDING);
        List<NxCommunityPosPaymentEntity> pending = nxCommunityPosPaymentDao.queryList(map);
        if (pending == null) {
            return;
        }
        for (NxCommunityPosPaymentEntity p : pending) {
            p.setNxPpStatus(PAY_FLOW_CLOSED);
            nxCommunityPosPaymentDao.update(p);
        }
    }

    private void assertOrderCanCreatePayment(NxCommunityOrdersEntity order) {
        if (Integer.valueOf(ORDER_STATUS_PAID).equals(order.getNxCoStatus())
                || Integer.valueOf(PAYMENT_STATUS_PAID).equals(order.getNxCoPaymentStatus())) {
            throw new IllegalStateException("订单已支付，禁止重复创建支付");
        }
        if (!Integer.valueOf(ORDER_STATUS_UNPAID).equals(order.getNxCoStatus())) {
            throw new IllegalStateException("订单不是待支付状态");
        }
    }

    /**
     * 复用未过期 PENDING；过期/异渠道/多余 PENDING 一律关闭，保证同单最多一个有效 PENDING。
     */
    private NxCommunityPosPaymentEntity resolveReusablePendingPayment(Integer orderId, String channel) {
        List<NxCommunityPosPaymentEntity> pending = listPendingPayments(orderId);
        if (pending.isEmpty()) {
            return null;
        }
        NxCommunityPosPaymentEntity reusable = null;
        for (NxCommunityPosPaymentEntity payment : pending) {
            boolean canReuse = reusable == null
                    && !isPaymentExpired(payment)
                    && channel.equals(payment.getNxPpPayChannel());
            if (canReuse) {
                reusable = payment;
            } else {
                payment.setNxPpStatus(PAY_FLOW_CLOSED);
                nxCommunityPosPaymentDao.update(payment);
            }
        }
        return reusable;
    }

    private List<NxCommunityPosPaymentEntity> listPendingPayments(Integer orderId) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderId);
        map.put("status", PAY_FLOW_PENDING);
        List<NxCommunityPosPaymentEntity> pending = nxCommunityPosPaymentDao.queryList(map);
        return pending == null ? Collections.emptyList() : pending;
    }

    private boolean isPaymentExpired(NxCommunityPosPaymentEntity payment) {
        String expireAt = payment.getNxPpExpireAt();
        if (expireAt == null || expireAt.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDateTime expire = LocalDateTime.parse(expireAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return LocalDateTime.now().isAfter(expire);
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> buildPaymentCreateResponse(NxCommunityPosPaymentEntity payment, boolean reused) {
        Map<String, Object> data = new HashMap<>();
        data.put("paymentId", payment.getNxPosPaymentId());
        data.put("orderId", payment.getNxPpOrderId());
        data.put("payChannel", payment.getNxPpPayChannel());
        data.put("qrCodeUrl", payment.getNxPpQrCodeUrl());
        data.put("outTradeNo", payment.getNxPpOutTradeNo());
        data.put("amount", payment.getNxPpAmount() == null ? null : payment.getNxPpAmount().toPlainString());
        data.put("expireAt", payment.getNxPpExpireAt());
        data.put("status", payment.getNxPpStatus());
        if (reused) {
            data.put("reused", true);
        }
        return data;
    }

    private String resolveWechatNotifyUrl() {
        if (posWechatNotifyUrl == null || posWechatNotifyUrl.trim().isEmpty()) {
            throw new IllegalStateException("未配置 pos.wechat.notify-url，无法创建微信支付");
        }
        return posWechatNotifyUrl.trim();
    }

    private String createWechatNativeQr(String outTradeNo, BigDecimal amountYuan) throws Exception {
        MyWxQingqingxiangPayConfig config = new MyWxQingqingxiangPayConfig();
        validateWechatPayConfig(config);
        int totalFee = amountYuan.multiply(new BigDecimal(100)).intValue();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "POS堂食订单");
        params.put("out_trade_no", outTradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", String.valueOf(totalFee));
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("notify_url", resolveWechatNotifyUrl());
        params.put("trade_type", "NATIVE");
        params.put("product_id", outTradeNo);
        WXPay wxpay = new WXPay(config);
        Map<String, String> resp = wxpay.unifiedOrder(params);
        if ("SUCCESS".equals(resp.get("return_code")) && "SUCCESS".equals(resp.get("result_code"))) {
            return resp.get("code_url");
        }
        throw new RuntimeException("微信下单失败: " + resp.get("return_msg"));
    }

    /**
     * PENDING 微信单主动查单补偿。本地 localhost 收不到 notify，轮询 status 时向微信确认是否已支付。
     *
     * @return 补偿说明；未支付或已 SUCCESS 前已处理则返回 null
     */
    private String syncWechatPaymentIfPaid(NxCommunityPosPaymentEntity payment) {
        if (payment.getNxPpOutTradeNo() == null || payment.getNxPpOutTradeNo().trim().isEmpty()) {
            throw new IllegalStateException("支付流水缺少 outTradeNo，无法向微信查单");
        }
        try {
            Map<String, String> resp = queryWechatOrder(payment.getNxPpOutTradeNo());
            if (!"SUCCESS".equals(resp.get("return_code"))) {
                throw new RuntimeException("微信查单通信失败: " + resp.get("return_msg"));
            }
            if (!"SUCCESS".equals(resp.get("result_code"))) {
                String errCode = resp.get("err_code");
                String errDesc = resp.get("err_code_des");
                if ("ORDERNOTEXIST".equals(errCode)) {
                    return null;
                }
                throw new RuntimeException("微信查单失败: " + (errDesc != null ? errDesc : errCode));
            }
            String tradeState = resp.get("trade_state");
            if ("SUCCESS".equals(tradeState)) {
                String transactionId = resp.get("transaction_id");
                markPaymentSuccess(payment, transactionId, WXPayUtil.mapToXml(resp));
                return "已从微信查单补偿为 SUCCESS";
            }
            return null;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("微信查单异常: " + e.getMessage(), e);
        }
    }

    private Map<String, String> queryWechatOrder(String outTradeNo) throws Exception {
        MyWxQingqingxiangPayConfig config = new MyWxQingqingxiangPayConfig();
        validateWechatPayConfig(config);
        WXPay wxpay = new WXPay(config);
        Map<String, String> data = new HashMap<>();
        data.put("out_trade_no", outTradeNo);
        return wxpay.orderQuery(data);
    }

    private void validateWechatPayConfig(MyWxQingqingxiangPayConfig config) {
        if (config.getAppID() == null || config.getAppID().trim().isEmpty()) {
            throw new IllegalStateException("微信支付未配置 appId（MyWxQingqingxiangPayConfig）");
        }
        if (config.getMchID() == null || config.getMchID().trim().isEmpty()) {
            throw new IllegalStateException("微信支付未配置 mchId（MyWxQingqingxiangPayConfig）");
        }
        if (config.getKey() == null || config.getKey().trim().isEmpty()) {
            throw new IllegalStateException("微信支付未配置 API_KEY（MyWxQingqingxiangPayConfig）");
        }
    }

    private Map<String, Object> buildPaymentStatusResponse(NxCommunityPosPaymentEntity payment) {
        Map<String, Object> data = new HashMap<>();
        data.put("paymentId", payment.getNxPosPaymentId());
        data.put("orderId", payment.getNxPpOrderId());
        data.put("payChannel", payment.getNxPpPayChannel());
        data.put("status", payment.getNxPpStatus());
        data.put("paidAt", payment.getNxPpPaidAt());
        data.put("transactionId", payment.getNxPpTransactionId());
        data.put("outTradeNo", payment.getNxPpOutTradeNo());
        return data;
    }

    /**
     * 微信商户订单号最长 32 字节。格式：POS + yyyyMMddHHmmssSSS(17) + 12位随机 = 32。
     */
    private String generatePosOutTradeNo() {
        String timePart = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String outTradeNo = "POS" + timePart + randomPart;
        if (outTradeNo.getBytes().length > 32) {
            throw new IllegalStateException("POS outTradeNo 超长: " + outTradeNo);
        }
        return outTradeNo;
    }

    private NxCommunityOrdersEntity requirePosOrder(Integer orderId) {
        NxCommunityOrdersEntity order = nxCommunityOrdersService.queryObject(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!ORDER_CHANNEL_POS.equals(order.getNxCoOrderChannel())) {
            throw new IllegalArgumentException("非 POS 订单");
        }
        if (Integer.valueOf(ORDER_STATUS_CANCELLED).equals(order.getNxCoStatus())) {
            throw new IllegalArgumentException("订单已作废");
        }
        return order;
    }

    private Map<String, Object> buildOrderDto(Integer orderId, Integer customerUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", orderId);
        NxCommunityOrdersEntity order = nxCommunityOrdersService.queryOrdersItemDetail(map);
        return buildOrderDto(order, customerUserId);
    }

    private Map<String, Object> buildOrderDto(Integer orderId) {
        return buildOrderDto(orderId, null);
    }

    private Map<String, Object> buildOrderDto(NxCommunityOrdersEntity order, Integer customerUserId) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("orderId", order.getNxCommunityOrdersId());
        dto.put("deskId", order.getNxCoDeskId());
        if (order.getNxCoDeskId() != null && order.getNxCoDeskId() > 0 && order.getNxCoDeskId() != 99) {
            NxCommunityDeskEntity desk = nxCommunityDeskService.queryObject(order.getNxCoDeskId());
            dto.put("deskName", desk != null ? desk.getNxCommunityDeskName() : null);
        }
        dto.put("status", posOrderStatusLabel(order.getNxCoStatus()));
        dto.put("paymentStatus", order.getNxCoPaymentStatus() != null && order.getNxCoPaymentStatus() == 1 ? "PAID" : "UNPAID");
        mergeOrderPricingFields(dto, order, customerUserId);
        dto.put("remark", order.getNxCoService());
        dto.put("items", buildItemDtoList(order.getNxOrdersSubEntities()));
        dto.put("appliedCoupon", buildAppliedCouponDto(order));
        return dto;
    }

    private void mergeOrderPricingFields(Map<String, Object> dto, NxCommunityOrdersEntity order,
                                         Integer customerUserId) {
        if (order.getNxCoUserCouponId() != null) {
            Map<String, String> amounts = resolveAppliedOrderAmounts(order);
            dto.put("goodsSubtotal", amounts.get("goodsSubtotal"));
            dto.put("discountSubtotal", amounts.get("discountSubtotal"));
            dto.put("payableTotal", amounts.get("payableTotal"));
            dto.put("couponInfo", buildAppliedCouponInfo(order));
            return;
        }
        if (customerUserId != null && customerUserId > 0) {
            CartPriceResult pricing = PosCartPricingHelper.priceOrder(
                    cartPricingService, nxCommunityOrdersSubService,
                    order.getNxCoCommunityId(), customerUserId, order.getNxCommunityOrdersId());
            PosCartPricingHelper.mergeIntoMap(dto, pricing);
            return;
        }
        String goodsSubtotal = calcGoodsSubtotal(order);
        dto.put("goodsSubtotal", goodsSubtotal);
        dto.put("discountSubtotal", "0.00");
        dto.put("payableTotal", goodsSubtotal);
        dto.put("couponInfo", CartPricingSupport.emptyPricing().toCouponInfoMap());
    }

    private Map<String, Object> buildAppliedCouponInfo(NxCommunityOrdersEntity order) {
        Map<String, Object> info = CartPricingSupport.emptyPricing().toCouponInfoMap();
        if (order.getNxCoUserCouponId() == null) {
            return info;
        }
        NxCustomerUserCouponEntity userCoupon = nxCustomerUserCouponService.getUserCouponById(order.getNxCoUserCouponId());
        if (userCoupon == null) {
            return info;
        }
        CouponEligibilityResult applied = new CouponEligibilityResult();
        applied.setUserCouponId(order.getNxCoUserCouponId());
        applied.setAvailable(true);
        NxCommunityCouponEntity template = resolveCouponTemplate(userCoupon);
        if (template != null) {
            applied.setCouponName(template.getNxCommunityCouponName());
            applied.setCouponTypeLabel(CouponLabelUtils.couponTypeLabel(template.getCouponType()));
            applied.setThresholdLabel(CouponLabelUtils.thresholdLabel(template));
            applied.setScopeLabel(CouponLabelUtils.scopeLabel(template.getScopeType()));
        }
        if (order.getNxCoYouhuiTotal() != null && !order.getNxCoYouhuiTotal().isEmpty()) {
            applied.setDiscountAmount(new BigDecimal(order.getNxCoYouhuiTotal()));
        }
        info.put("bestCoupon", applied.toMap());
        return info;
    }

    private Map<String, String> resolveAppliedOrderAmounts(NxCommunityOrdersEntity order) {
        String goodsSubtotal = calcGoodsSubtotal(order);
        BigDecimal discount = BigDecimal.ZERO;
        if (order.getNxCoYouhuiTotal() != null && !order.getNxCoYouhuiTotal().isEmpty()) {
            discount = new BigDecimal(order.getNxCoYouhuiTotal());
        }
        discount = discount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal payable;
        if (order.getNxCoTotal() != null && !order.getNxCoTotal().isEmpty()) {
            payable = new BigDecimal(order.getNxCoTotal());
        } else {
            payable = new BigDecimal(goodsSubtotal).subtract(discount).max(BigDecimal.ZERO);
        }
        Map<String, String> amounts = new HashMap<>();
        amounts.put("goodsSubtotal", goodsSubtotal);
        amounts.put("discountSubtotal", discount.toPlainString());
        amounts.put("payableTotal", payable.setScale(2, RoundingMode.HALF_UP).toPlainString());
        return amounts;
    }

    private Map<String, Object> buildOrderSummaryDto(NxCommunityOrdersEntity order) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("orderId", order.getNxCommunityOrdersId());
        dto.put("deskId", order.getNxCoDeskId());
        dto.put("status", posOrderStatusLabel(order.getNxCoStatus()));
        dto.put("paymentStatus", order.getNxCoPaymentStatus() != null && order.getNxCoPaymentStatus() == 1 ? "PAID" : "UNPAID");
        dto.put("payableTotal", order.getNxCoTotal());
        dto.put("goodsSubtotal", calcGoodsSubtotal(order));
        dto.put("paidAt", order.getNxCoPaymentTime());
        int itemCount = order.getNxOrdersSubEntities() == null ? 0 : order.getNxOrdersSubEntities().size();
        dto.put("itemCount", itemCount);
        return dto;
    }

    private String calcGoodsSubtotal(NxCommunityOrdersEntity order) {
        BigDecimal total = BigDecimal.ZERO;
        if (order.getNxOrdersSubEntities() != null) {
            for (NxCommunityOrdersSubEntity sub : order.getNxOrdersSubEntities()) {
                if (sub.getNxCosSubtotal() != null) {
                    total = total.add(new BigDecimal(sub.getNxCosSubtotal()));
                }
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private List<Map<String, Object>> buildItemDtoList(List<NxCommunityOrdersSubEntity> subs) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (subs == null) {
            return items;
        }
        for (NxCommunityOrdersSubEntity sub : subs) {
            Map<String, Object> item = new HashMap<>();
            item.put("lineId", sub.getNxCommunityOrdersSubId());
            item.put("goodsId", sub.getNxCosCommunityGoodsId());
            if (sub.getNxCommunityGoodsEntity() != null) {
                item.put("goodsName", sub.getNxCommunityGoodsEntity().getNxCgGoodsName());
            }
            item.put("quantity", sub.getNxCosQuantity());
            item.put("price", sub.getNxCosPrice());
            item.put("subtotal", sub.getNxCosSubtotal());
            item.put("remark", sub.getNxCosRemark());
            items.add(item);
        }
        return items;
    }

    private Map<String, Object> buildAppliedCouponDto(NxCommunityOrdersEntity order) {
        if (order.getNxCoUserCouponId() == null) {
            return null;
        }
        NxCustomerUserCouponEntity userCoupon = nxCustomerUserCouponService.getUserCouponById(order.getNxCoUserCouponId());
        if (userCoupon == null) {
            return null;
        }
        NxCommunityCouponEntity template = resolveCouponTemplate(userCoupon);
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

    private Map<String, Object> buildCouponLookupDto(CouponEligibilityResult eligibility,
                                                     NxCustomerUserCouponEntity userCoupon) {
        Map<String, Object> data = eligibility.toMap();
        data.put("status", couponStatusLabel(userCoupon.getNxCucStatus()));
        data.put("canUseInOrder", eligibility.isAvailable());
        String standaloneReason = standaloneVerifyUnavailableReason(userCoupon, eligibility.getUnavailableReason());
        data.put("canStandaloneVerify", standaloneReason == null
                && Integer.valueOf(COUPON_STATUS_AVAILABLE).equals(userCoupon.getNxCucStatus()));
        if (!eligibility.isAvailable() && eligibility.getUnavailableReason() == null) {
            data.put("unavailableReason", standaloneReason);
        }
        return data;
    }

    private NxCommunityCouponEntity resolveCouponTemplate(NxCustomerUserCouponEntity userCoupon) {
        if (userCoupon == null) {
            return null;
        }
        NxCommunityCouponEntity template = userCoupon.getNxCommunityCouponEntity();
        if (template == null && userCoupon.getNxCucCouponId() != null) {
            template = nxCommunityCouponService.queryObject(userCoupon.getNxCucCouponId());
        }
        return template;
    }

    private BigDecimal resolvePayableTotal(NxCommunityOrdersEntity order) {
        if (order.getNxCoTotal() != null && !order.getNxCoTotal().isEmpty()) {
            return new BigDecimal(order.getNxCoTotal()).setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(calcGoodsSubtotal(order)).setScale(2, RoundingMode.HALF_UP);
    }

    private String standaloneVerifyUnavailableReason(NxCustomerUserCouponEntity userCoupon, String orderReason) {
        if (!Integer.valueOf(COUPON_STATUS_AVAILABLE).equals(userCoupon.getNxCucStatus())) {
            if (Integer.valueOf(COUPON_STATUS_LOCKED).equals(userCoupon.getNxCucStatus())) {
                return "优惠券已锁定";
            }
            if (Integer.valueOf(COUPON_STATUS_VERIFIED).equals(userCoupon.getNxCucStatus())) {
                return "优惠券已核销";
            }
            return "优惠券不可用";
        }
        if (userCoupon.getNxCucStatus() != null && userCoupon.getNxCucStatus() < 0) {
            return "优惠券已失效";
        }
        if (!isCouponInValidPeriod(userCoupon)) {
            return "优惠券已过期或未生效";
        }
        if (orderReason != null && (orderReason.contains("订单") || orderReason.contains("适用商品")
                || orderReason.contains("未满") || orderReason.contains("渠道"))) {
            return null;
        }
        return orderReason;
    }

    private NxCustomerUserCouponEntity resolveUserCoupon(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        String trimmed = code.trim();
        if (trimmed.matches("\\d+")) {
            return nxCustomerUserCouponService.queryUserCouponDetail(Integer.parseInt(trimmed));
        }
        return null;
    }

    private String couponUnavailableReason(NxCustomerUserCouponEntity userCoupon, Integer communityId) {
        if (communityId != null && !communityId.equals(userCoupon.getNxCucCommunityId())) {
            return "优惠券不属于当前门店";
        }
        if (userCoupon.getNxCucStatus() != null && userCoupon.getNxCucStatus() < 0) {
            return "优惠券已失效";
        }
        if (!isCouponInValidPeriod(userCoupon)) {
            return "优惠券已过期或未生效";
        }
        return null;
    }

    private boolean isCouponInValidPeriod(NxCustomerUserCouponEntity userCoupon) {
        String stop = userCoupon.getNxCucStopDate();
        if (stop == null && userCoupon.getNxCommunityCouponEntity() != null) {
            stop = userCoupon.getNxCommunityCouponEntity().getNxCpStopDate();
        }
        if (stop != null && !stop.isEmpty()) {
            LocalDate end = LocalDate.parse(stop);
            if (LocalDate.now().isAfter(end)) {
                return false;
            }
        }
        String start = userCoupon.getNxCucStartDate();
        if (start == null && userCoupon.getNxCommunityCouponEntity() != null) {
            start = userCoupon.getNxCommunityCouponEntity().getNxCpStartDate();
        }
        if (start != null && !start.isEmpty()) {
            LocalDate begin = LocalDate.parse(start);
            if (LocalDate.now().isBefore(begin)) {
                return false;
            }
        }
        return true;
    }

    private void assertMemberRelatedToCommunity(NxCustomerUserEntity member, Integer communityId) {
        if (communityId.equals(member.getNxCuCommunityId())) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", member.getNxCuUserId());
        map.put("commId", communityId);
        List<NxCustomerUserCouponEntity> any = nxCustomerUserCouponService.queryUserCouponListByParams(map);
        if (any == null || any.isEmpty()) {
            throw new IllegalArgumentException("会员与当前门店无关");
        }
    }

    private void incrementCouponUseCount(Integer couponId) {
        NxCommunityCouponEntity template = nxCommunityCouponService.queryObject(couponId);
        if (template != null) {
            int count = template.getNxCpUseCount() == null ? 0 : template.getNxCpUseCount();
            template.setNxCpUseCount(count + 1);
            nxCommunityCouponService.update(template);
        }
    }

    private Integer saveVerifyLog(NxCustomerUserCouponEntity userCoupon, String type, Integer orderId,
                                  Integer subId, Integer deskId, Integer operatorId,
                                  int before, int after, String remark) {
        NxCommunityCouponVerifyLogEntity log = new NxCommunityCouponVerifyLogEntity();
        log.setNxCvlUserCouponId(userCoupon.getNxCustomerUserCouponId());
        log.setNxCvlCouponId(userCoupon.getNxCucCouponId());
        log.setNxCvlCommunityId(userCoupon.getNxCucCommunityId());
        log.setNxCvlVerifyType(type);
        log.setNxCvlOrderId(orderId);
        log.setNxCvlOrderSubId(subId);
        log.setNxCvlDeskId(deskId);
        log.setNxCvlOperatorId(operatorId);
        log.setNxCvlBeforeStatus(before);
        log.setNxCvlAfterStatus(after);
        log.setNxCvlRemark(remark);
        log.setNxCvlCreateAt(formatWhatYearDayTime(0));
        nxCommunityCouponVerifyLogDao.save(log);
        return log.getNxCouponVerifyLogId();
    }

    private List<Map<String, Object>> toDeskDtoList(List<NxCommunityDeskEntity> desks, Integer communityId) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (desks == null) {
            return list;
        }
        for (NxCommunityDeskEntity desk : desks) {
            deskOrderReconcileService.reconcileDesk(desk);
            Map<String, Object> d = new HashMap<>();
            d.put("deskId", desk.getNxCommunityDeskId());
            d.put("deskName", desk.getNxCommunityDeskName());
            d.put("chairNum", desk.getNxCdChairNum());
            d.put("status", posDeskBindingService.deskStatusLabel(desk));
            d.put("currentOrderId", desk.getNxCdCurrentOrderId());
            if (desk.getNxCdCurrentOrderId() != null) {
                NxCommunityOrdersEntity order = nxCommunityOrdersService.queryObject(desk.getNxCdCurrentOrderId());
                if (order != null) {
                    d.put("payableTotal", order.getNxCoTotal());
                }
            }
            list.add(d);
        }
        return list;
    }

    private void reconcileDeskIfPaymentEnded(NxCommunityPosPaymentEntity payment) {
        if (payment == null || payment.getNxPpOrderId() == null) {
            return;
        }
        String status = payment.getNxPpStatus();
        if (PAY_FLOW_CLOSED.equals(status) || PAY_FLOW_FAILED.equals(status)) {
            deskOrderReconcileService.reconcileByOrderId(payment.getNxPpOrderId());
        }
    }

    private boolean isRealDeskId(Integer deskId) {
        return deskId != null && deskId != -1 && deskId != 99;
    }

    private void refreshPosOrderTotals(NxCommunityOrdersEntity order, Integer customerUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getNxCommunityOrdersId());
        NxCommunityOrdersEntity detail = nxCommunityOrdersService.queryOrdersItemDetail(map);
        if (detail == null) {
            return;
        }
        if (order.getNxCoUserCouponId() != null) {
            Map<String, String> amounts = resolveAppliedOrderAmounts(detail);
            order.setNxCoTotal(amounts.get("payableTotal"));
            nxCommunityOrdersService.update(order);
            return;
        }
        if (customerUserId != null && customerUserId > 0) {
            CartPriceResult pricing = PosCartPricingHelper.priceOrder(
                    cartPricingService, nxCommunityOrdersSubService,
                    order.getNxCoCommunityId(), customerUserId, order.getNxCommunityOrdersId());
            order.setNxCoTotal(pricing.getPayableTotal().toPlainString());
            nxCommunityOrdersService.update(order);
            return;
        }
        String goodsSubtotal = calcGoodsSubtotal(detail);
        order.setNxCoTotal(goodsSubtotal);
        nxCommunityOrdersService.update(order);
    }

    private void updateOrderSubsDeskId(Integer orderId, Integer deskId) {
        Map<String, Object> subMap = new HashMap<>();
        subMap.put("orderId", orderId);
        List<NxCommunityOrdersSubEntity> subs = nxCommunityOrdersSubService.querySubOrdersByParams(subMap);
        if (subs == null) {
            return;
        }
        for (NxCommunityOrdersSubEntity sub : subs) {
            sub.setNxCosDeskId(deskId);
            nxCommunityOrdersSubService.update(sub);
        }
    }

    private void cancelOrderSubs(Integer orderId) {
        Map<String, Object> subMap = new HashMap<>();
        subMap.put("orderId", orderId);
        List<NxCommunityOrdersSubEntity> subs = nxCommunityOrdersSubService.querySubOrdersByParams(subMap);
        if (subs == null) {
            return;
        }
        for (NxCommunityOrdersSubEntity sub : subs) {
            sub.setNxCosStatus(SUB_STATUS_DELETED);
            nxCommunityOrdersSubService.update(sub);
        }
    }

    private void validateSaveOrderRequest(PosSaveOrderRequest request) {
        if (request.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }
        if (request.getDeskId() == null) {
            throw new IllegalArgumentException("deskId 不能为空");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("items 不能为空");
        }
    }

    private void appendCategory(List<Map<String, Object>> categories, NxCommunityFatherGoodsEntity father) {
        List<NxCommunityGoodsEntity> goodsList = father.getNxCommunityGoodsEntities();
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }
        List<Map<String, Object>> goodsDto = new ArrayList<>();
        for (NxCommunityGoodsEntity goods : goodsList) {
            if (goods == null) {
                continue;
            }
            if (goods.getNxCgPullOff() != null
                    && goods.getNxCgPullOff().equals(NX_COMMUNITY_GOODS_PULL_OFF_YES)) {
                continue;
            }
            Map<String, Object> g = new HashMap<>();
            g.put("goodsId", goods.getNxCommunityGoodsId());
            g.put("goodsName", goods.getNxCgGoodsName());
            g.put("standardName", goods.getNxCgGoodsStandardname());
            g.put("price", goods.getNxCgGoodsPrice());
            g.put("goodsType", goodsTypeLabel(goods.getNxCgGoodsType()));
            g.put("sellStatus", "ON_SALE");
            g.put("soldOut", false);
            g.put("imageUrl", goods.getNxCgNxGoodsFilePath());
            goodsDto.add(g);
        }
        if (!goodsDto.isEmpty()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("categoryId", father.getNxCommunityFatherGoodsId());
            cat.put("categoryName", father.getNxCfgFatherGoodsName());
            cat.put("sort", father.getNxCfgFatherGoodsSort());
            cat.put("goods", goodsDto);
            categories.add(cat);
        }
    }

    private String goodsTypeLabel(Integer type) {
        if (type == null) {
            return "COMMON";
        }
        if (type.equals(getNxCommunityGoodsTypeDuopin())) {
            return "DUOPIN";
        }
        if (type.equals(getNxCommunityGoodsTypeTaocan())) {
            return "TAOCAN";
        }
        return "COMMON";
    }

    private String posOrderStatusLabel(Integer status) {
        if (status == null) {
            return "UNPAID";
        }
        if (status == ORDER_STATUS_PAID) {
            return "PAID";
        }
        if (status == ORDER_STATUS_CANCELLED) {
            return "CANCELLED";
        }
        return "UNPAID";
    }

    private Integer posOrderStatusFromLabel(String label) {
        if ("PAID".equalsIgnoreCase(label)) {
            return ORDER_STATUS_PAID;
        }
        if ("CANCELLED".equalsIgnoreCase(label)) {
            return ORDER_STATUS_CANCELLED;
        }
        return ORDER_STATUS_UNPAID;
    }

    private String couponStatusLabel(Integer status) {
        if (status == null) {
            return "UNKNOWN";
        }
        if (status == COUPON_STATUS_AVAILABLE) {
            return "AVAILABLE";
        }
        if (status == COUPON_STATUS_LOCKED) {
            return "LOCKED";
        }
        if (status == COUPON_STATUS_VERIFIED) {
            return "VERIFIED";
        }
        return "UNAVAILABLE";
    }

    private String failXml(String msg) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("return_code", "FAIL");
            data.put("return_msg", msg);
            return WXPayUtil.mapToXml(data);
        } catch (Exception e) {
            return "<xml><return_code><![CDATA[FAIL]]></return_code></xml>";
        }
    }
}
