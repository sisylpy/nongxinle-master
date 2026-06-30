package com.nongxinle.community.coupon.service.impl;

import com.nongxinle.dto.coupon.CartLineSnapshot;
import com.nongxinle.dto.coupon.CouponEligibilityRequest;
import com.nongxinle.dto.coupon.CouponEligibilityResult;
import com.nongxinle.entity.NxCommunityCouponEntity;
import com.nongxinle.entity.NxCommunityFatherGoodsEntity;
import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.entity.NxCommunityOrdersSubEntity;
import com.nongxinle.entity.NxCustomerUserCouponEntity;
import com.nongxinle.community.coupon.service.CouponEligibilityService;
import com.nongxinle.community.coupon.service.NxCommunityCouponService;
import com.nongxinle.community.catalog.service.NxCommunityFatherGoodsService;
import com.nongxinle.community.order.service.NxCommunityOrdersService;
import com.nongxinle.community.coupon.service.NxCustomerUserCouponService;
import com.nongxinle.utils.CouponLabelUtils;
import com.nongxinle.utils.CouponRuleConstants;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.utils.CouponRuleConstants.*;
import static com.nongxinle.community.pos.NxCommunityPosConstants.COUPON_STATUS_AVAILABLE;
import static com.nongxinle.community.pos.NxCommunityPosConstants.COUPON_STATUS_LOCKED;
import static com.nongxinle.community.pos.NxCommunityPosConstants.COUPON_STATUS_VERIFIED;

@Service("couponEligibilityService")
public class CouponEligibilityServiceImpl implements CouponEligibilityService {

    @Autowired
    private NxCustomerUserCouponService nxCustomerUserCouponService;
    @Autowired
    private NxCommunityCouponService nxCommunityCouponService;
    @Autowired
    private NxCommunityOrdersService nxCommunityOrdersService;
    @Autowired
    private NxCommunityFatherGoodsService nxCommunityFatherGoodsService;

    @Override
    public CouponEligibilityResult evaluate(CouponEligibilityRequest request) {
        validateRequest(request);
        CouponEligibilityResult result = initResult(request.getUserCouponId());

        NxCustomerUserCouponEntity userCoupon = nxCustomerUserCouponService.queryUserCouponDetail(request.getUserCouponId());
        if (userCoupon == null) {
            return markUnavailable(result, "用户券不存在", request.isIncludeUnavailable());
        }

        NxCommunityCouponEntity template = resolveTemplate(userCoupon);
        fillMetadata(result, userCoupon, template);

        String reason = checkUserCouponBasics(userCoupon, request.getCommunityId());
        if (reason != null) {
            return markUnavailable(result, reason, request.isIncludeUnavailable());
        }

        if (template == null) {
            return markUnavailable(result, "优惠券模板不存在", request.isIncludeUnavailable());
        }
        if (template.getNxCpStatus() != null && !Integer.valueOf(TEMPLATE_STATUS_ACTIVE).equals(template.getNxCpStatus())) {
            return markUnavailable(result, "优惠券模板已停用", request.isIncludeUnavailable());
        }

        if (!isCouponInValidPeriod(userCoupon, template)) {
            return markUnavailable(result, "优惠券已过期或未生效", request.isIncludeUnavailable());
        }

        String channel = normalizeChannel(request.getChannel());
        if (!channelMatches(template.getUseChannel(), channel)) {
            return markUnavailable(result, "优惠券不适用于当前渠道", request.isIncludeUnavailable());
        }

        List<CartLineSnapshot> cartLines = resolveCartLines(request);
        if (cartLines.isEmpty()) {
            return markUnavailable(result, "购物车为空", request.isIncludeUnavailable());
        }

        BigDecimal eligibleSubtotal = calcEligibleSubtotal(cartLines, template);
        result.setEligibleSubtotal(eligibleSubtotal);

        return applyDiscountRules(result, template, eligibleSubtotal, request.isIncludeUnavailable());
    }

    @Override
    public List<CouponEligibilityResult> evaluateForMemberWithCart(Integer customerUserId, Integer communityId,
                                                                   List<CartLineSnapshot> cartLines, String channel) {
        if (cartLines == null || cartLines.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", customerUserId);
        map.put("commId", communityId);
        List<NxCustomerUserCouponEntity> coupons = nxCustomerUserCouponService.queryUserCouponListByParams(map);
        if (coupons == null || coupons.isEmpty()) {
            return Collections.emptyList();
        }

        List<CouponEligibilityResult> results = new ArrayList<>();
        for (NxCustomerUserCouponEntity userCoupon : coupons) {
            if (userCoupon.getNxCucStatus() != null && userCoupon.getNxCucStatus() < 0) {
                continue;
            }
            CouponEligibilityRequest req = new CouponEligibilityRequest();
            req.setUserCouponId(userCoupon.getNxCustomerUserCouponId());
            req.setCommunityId(communityId);
            req.setCartLines(cartLines);
            req.setChannel(channel);
            req.setIncludeUnavailable(true);
            results.add(evaluate(req));
        }
        sortResults(results);
        return results;
    }

    @Override
    public List<CouponEligibilityResult> evaluateForMember(Integer customerUserId, Integer communityId,
                                                           Integer orderId, String channel) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", customerUserId);
        map.put("commId", communityId);
        List<NxCustomerUserCouponEntity> coupons = nxCustomerUserCouponService.queryUserCouponListByParams(map);
        if (coupons == null || coupons.isEmpty()) {
            return Collections.emptyList();
        }

        List<CouponEligibilityResult> results = new ArrayList<>();
        for (NxCustomerUserCouponEntity userCoupon : coupons) {
            if (userCoupon.getNxCucStatus() != null && userCoupon.getNxCucStatus() < 0) {
                continue;
            }
            CouponEligibilityRequest req = new CouponEligibilityRequest();
            req.setUserCouponId(userCoupon.getNxCustomerUserCouponId());
            req.setCommunityId(communityId);
            req.setOrderId(orderId);
            req.setChannel(channel);
            req.setIncludeUnavailable(true);
            results.add(evaluate(req));
        }
        sortResults(results);
        return results;
    }

    private void validateRequest(CouponEligibilityRequest request) {
        if (request.getUserCouponId() == null) {
            throw new IllegalArgumentException("userCouponId 不能为空");
        }
        if (request.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }
        boolean hasCart = request.getCartLines() != null && !request.getCartLines().isEmpty();
        if (!hasCart && request.getOrderId() == null) {
            throw new IllegalArgumentException("orderId 或 cartLines 不能为空");
        }
    }

    private List<CartLineSnapshot> resolveCartLines(CouponEligibilityRequest request) {
        if (request.getCartLines() != null && !request.getCartLines().isEmpty()) {
            return request.getCartLines();
        }
        NxCommunityOrdersEntity order = nxCommunityOrdersService.queryObject(request.getOrderId());
        if (order == null) {
            return Collections.emptyList();
        }
        if (!request.getCommunityId().equals(order.getNxCoCommunityId())) {
            return Collections.emptyList();
        }
        return toCartLines(loadOrderSubs(order));
    }

    private List<CartLineSnapshot> toCartLines(List<NxCommunityOrdersSubEntity> subs) {
        List<CartLineSnapshot> lines = new ArrayList<>();
        if (subs == null) {
            return lines;
        }
        for (NxCommunityOrdersSubEntity sub : subs) {
            CartLineSnapshot line = new CartLineSnapshot();
            line.setGoodsId(sub.getNxCosCommunityGoodsId());
            line.setCategoryId(sub.getNxCosCommunityGoodsFatherId());
            if (sub.getNxCosPrice() != null && !sub.getNxCosPrice().isEmpty()) {
                line.setPrice(new BigDecimal(sub.getNxCosPrice()));
            }
            if (sub.getNxCosQuantity() != null && !sub.getNxCosQuantity().isEmpty()) {
                line.setQuantity(Integer.parseInt(sub.getNxCosQuantity()));
            }
            if (sub.getNxCosSubtotal() != null && !sub.getNxCosSubtotal().isEmpty()) {
                line.setSubtotal(new BigDecimal(sub.getNxCosSubtotal()));
            }
            lines.add(line);
        }
        return lines;
    }

    private CouponEligibilityResult initResult(Integer userCouponId) {
        CouponEligibilityResult result = new CouponEligibilityResult();
        result.setUserCouponId(userCouponId);
        result.setEligibleSubtotal(BigDecimal.ZERO);
        result.setThresholdAmount(BigDecimal.ZERO);
        result.setDistanceToThreshold(BigDecimal.ZERO);
        result.setDiscountAmount(BigDecimal.ZERO);
        return result;
    }

    private void fillMetadata(CouponEligibilityResult result, NxCustomerUserCouponEntity userCoupon,
                              NxCommunityCouponEntity template) {
        if (template != null) {
            result.setCouponName(template.getNxCommunityCouponName());
            result.setCouponType(template.getCouponType());
            result.setCouponTypeLabel(CouponLabelUtils.couponTypeLabel(template.getCouponType()));
            result.setThresholdLabel(CouponLabelUtils.thresholdLabel(template));
            result.setScopeLabel(CouponLabelUtils.scopeLabel(template.getScopeType()));
            result.setThresholdAmount(zeroIfNull(template.getThresholdAmount()));
        }
        result.setValidStart(resolveValidStart(userCoupon, template));
        result.setValidEnd(resolveValidEnd(userCoupon, template));
        result.setExpireTime(result.getValidEnd());
    }

    private String checkUserCouponBasics(NxCustomerUserCouponEntity userCoupon, Integer communityId) {
        if (!Integer.valueOf(COUPON_STATUS_AVAILABLE).equals(userCoupon.getNxCucStatus())) {
            if (Integer.valueOf(COUPON_STATUS_LOCKED).equals(userCoupon.getNxCucStatus())) {
                return "优惠券已锁定";
            }
            if (Integer.valueOf(COUPON_STATUS_VERIFIED).equals(userCoupon.getNxCucStatus())) {
                return "优惠券已核销";
            }
            return "优惠券不可用";
        }
        if (!communityId.equals(userCoupon.getNxCucCommunityId())) {
            return "优惠券不属于当前门店";
        }
        if (userCoupon.getNxCucStatus() != null && userCoupon.getNxCucStatus() < 0) {
            return "优惠券已失效";
        }
        return null;
    }

    private CouponEligibilityResult applyDiscountRules(CouponEligibilityResult result, NxCommunityCouponEntity template,
                                                       BigDecimal eligibleSubtotal, boolean includeUnavailable) {
        BigDecimal templateDiscount = zeroIfNull(template.getDiscountAmount()).setScale(2, RoundingMode.HALF_UP);
        String couponType = template.getCouponType() == null ? TYPE_CASH : template.getCouponType();

        if (eligibleSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return markUnavailable(result, "无适用商品", includeUnavailable);
        }

        if (TYPE_FULL_REDUCTION.equals(couponType)) {
            BigDecimal threshold = zeroIfNull(template.getThresholdAmount()).setScale(2, RoundingMode.HALF_UP);
            result.setThresholdAmount(threshold);
            if (eligibleSubtotal.compareTo(threshold) < 0) {
                BigDecimal distance = threshold.subtract(eligibleSubtotal).setScale(2, RoundingMode.HALF_UP);
                result.setDistanceToThreshold(distance);
                return markUnavailable(result, "未满" + threshold.toPlainString() + "元，还差" + distance.toPlainString() + "元",
                        includeUnavailable);
            }
            result.setDistanceToThreshold(BigDecimal.ZERO);
        } else {
            result.setDistanceToThreshold(BigDecimal.ZERO);
        }

        BigDecimal discount = templateDiscount.min(eligibleSubtotal).setScale(2, RoundingMode.HALF_UP);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            return markUnavailable(result, "优惠金额为0", includeUnavailable);
        }

        result.setDiscountAmount(discount);
        result.setAvailable(true);
        result.setUnavailableReason(null);
        return result;
    }

    private CouponEligibilityResult markUnavailable(CouponEligibilityResult result, String reason,
                                                    boolean includeUnavailable) {
        result.setAvailable(false);
        result.setDiscountAmount(BigDecimal.ZERO);
        if (includeUnavailable) {
            result.setUnavailableReason(reason);
            return result;
        }
        throw new IllegalStateException(reason);
    }

    private List<NxCommunityOrdersSubEntity> loadOrderSubs(NxCommunityOrdersEntity order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getNxCommunityOrdersId());
        NxCommunityOrdersEntity detail = nxCommunityOrdersService.queryOrdersItemDetail(map);
        if (detail == null || detail.getNxOrdersSubEntities() == null) {
            return Collections.emptyList();
        }
        return detail.getNxOrdersSubEntities();
    }

    private BigDecimal calcEligibleSubtotal(List<CartLineSnapshot> lines, NxCommunityCouponEntity template) {
        String scopeType = template.getScopeType() == null ? SCOPE_ALL : template.getScopeType();
        Set<Integer> refIds = parseScopeRefIds(template.getScopeRefIds());
        BigDecimal total = BigDecimal.ZERO;
        for (CartLineSnapshot line : lines) {
            if (!matchesScope(line, scopeType, refIds)) {
                continue;
            }
            if (line.getSubtotal() != null) {
                total = total.add(line.getSubtotal());
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean matchesScope(CartLineSnapshot line, String scopeType, Set<Integer> refIds) {
        if (SCOPE_ALL.equals(scopeType)) {
            return true;
        }
        if (SCOPE_GOODS.equals(scopeType)) {
            return line.getGoodsId() != null && refIds.contains(line.getGoodsId());
        }
        if (SCOPE_CATEGORY.equals(scopeType)) {
            return categoryMatches(line.getCategoryId(), refIds);
        }
        return false;
    }

    private boolean categoryMatches(Integer categoryId, Set<Integer> refIds) {
        if (categoryId != null && refIds.contains(categoryId)) {
            return true;
        }
        if (categoryId != null) {
            Set<Integer> ancestors = resolveCategoryAncestors(categoryId);
            for (Integer ancestor : ancestors) {
                if (refIds.contains(ancestor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<Integer> resolveCategoryAncestors(Integer categoryId) {
        Set<Integer> ancestors = new HashSet<>();
        Integer current = categoryId;
        int guard = 0;
        while (current != null && guard++ < 10) {
            NxCommunityFatherGoodsEntity father = nxCommunityFatherGoodsService.queryObject(current);
            if (father == null || father.getNxCfgFathersFatherId() == null
                    || father.getNxCfgFathersFatherId() <= 0) {
                break;
            }
            current = father.getNxCfgFathersFatherId();
            ancestors.add(current);
        }
        return ancestors;
    }

    private Set<Integer> parseScopeRefIds(String scopeRefIds) {
        Set<Integer> ids = new HashSet<>();
        if (scopeRefIds == null || scopeRefIds.trim().isEmpty()) {
            return ids;
        }
        try {
            JSONArray array = JSONArray.fromObject(scopeRefIds);
            for (int i = 0; i < array.size(); i++) {
                ids.add(array.getInt(i));
            }
        } catch (Exception ignored) {
            // 非法 JSON 视为无命中
        }
        return ids;
    }

    private NxCommunityCouponEntity resolveTemplate(NxCustomerUserCouponEntity userCoupon) {
        NxCommunityCouponEntity template = userCoupon.getNxCommunityCouponEntity();
        if (needsTemplateReload(template) && userCoupon.getNxCucCouponId() != null) {
            template = nxCommunityCouponService.queryObject(userCoupon.getNxCucCouponId());
        }
        return template;
    }

    private boolean needsTemplateReload(NxCommunityCouponEntity template) {
        if (template == null) {
            return true;
        }
        return template.getDiscountAmount() == null
                || template.getCouponType() == null
                || template.getScopeType() == null;
    }

    private boolean isCouponInValidPeriod(NxCustomerUserCouponEntity userCoupon, NxCommunityCouponEntity template) {
        String stop = userCoupon.getNxCucStopDate();
        if ((stop == null || stop.isEmpty()) && template != null) {
            stop = template.getNxCpStopDate();
        }
        if (stop != null && !stop.isEmpty()) {
            LocalDate end = LocalDate.parse(stop);
            if (LocalDate.now().isAfter(end)) {
                return false;
            }
        }
        String start = userCoupon.getNxCucStartDate();
        if ((start == null || start.isEmpty()) && template != null) {
            start = template.getNxCpStartDate();
        }
        if (start != null && !start.isEmpty()) {
            LocalDate begin = LocalDate.parse(start);
            if (LocalDate.now().isBefore(begin)) {
                return false;
            }
        }
        return true;
    }

    private boolean channelMatches(String templateChannel, String requestChannel) {
        String useChannel = templateChannel == null || templateChannel.isEmpty() ? CHANNEL_ALL : templateChannel;
        return CHANNEL_ALL.equals(useChannel) || useChannel.equals(requestChannel);
    }

    private String normalizeChannel(String channel) {
        if (channel == null || channel.trim().isEmpty()) {
            return CHANNEL_POS;
        }
        return channel.trim().toUpperCase();
    }

    private String resolveValidStart(NxCustomerUserCouponEntity userCoupon, NxCommunityCouponEntity template) {
        if (userCoupon.getNxCucStartDate() != null && !userCoupon.getNxCucStartDate().isEmpty()) {
            return userCoupon.getNxCucStartDate();
        }
        return template != null ? template.getNxCpStartDate() : "";
    }

    private String resolveValidEnd(NxCustomerUserCouponEntity userCoupon, NxCommunityCouponEntity template) {
        if (userCoupon.getNxCucStopDate() != null && !userCoupon.getNxCucStopDate().isEmpty()) {
            return userCoupon.getNxCucStopDate();
        }
        return template != null ? template.getNxCpStopDate() : "";
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void sortResults(List<CouponEligibilityResult> results) {
        results.sort((a, b) -> {
            if (a.isAvailable() != b.isAvailable()) {
                return a.isAvailable() ? -1 : 1;
            }
            int expireCompare = compareNullableString(a.getExpireTime(), b.getExpireTime());
            if (expireCompare != 0) {
                return expireCompare;
            }
            BigDecimal discountA = a.getDiscountAmount() == null ? BigDecimal.ZERO : a.getDiscountAmount();
            BigDecimal discountB = b.getDiscountAmount() == null ? BigDecimal.ZERO : b.getDiscountAmount();
            return discountB.compareTo(discountA);
        });
    }

    private int compareNullableString(String a, String b) {
        if (a == null || a.isEmpty()) {
            return (b == null || b.isEmpty()) ? 0 : 1;
        }
        if (b == null || b.isEmpty()) {
            return -1;
        }
        return a.compareTo(b);
    }
}
