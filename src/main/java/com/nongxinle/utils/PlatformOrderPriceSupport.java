package com.nongxinle.utils;

import com.nongxinle.entity.NxDepartmentOrdersEntity;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

/**
 * 平台订单销售价语义：期望价 nxDoExpectPrice、实际价 nxDoPrice、差价 nxDoPriceDifferent。
 * 算法与现网 saveToFillWeightAndPrice / giveOrderPrice 一致：差价 = 实际价 - 期望价。
 */
public final class PlatformOrderPriceSupport {

    public static final String PLATFORM_PRICE_LABEL = "期望价/实际价";

    private PlatformOrderPriceSupport() {
    }

    /**
     * assign 成功后：参考价写入期望价，实际价初始相同，差价为 0。
     */
    public static void applyAssignReferencePrice(NxDepartmentOrdersEntity order) {
        if (order == null || !SalesPriceUtils.isValidSalesPrice(order.getNxDoPrice())) {
            return;
        }
        String referencePrice = order.getNxDoPrice().trim();
        order.setNxDoExpectPrice(referencePrice);
        order.setNxDoPrice(referencePrice);
        order.setNxDoPriceDifferent("0");
    }

    /**
     * 修改实际价后重算差价（不覆盖期望价）。
     */
    public static void recalculatePriceDifferent(NxDepartmentOrdersEntity order) {
        if (order == null) {
            return;
        }
        if (StringUtils.isBlank(order.getNxDoExpectPrice()) || StringUtils.isBlank(order.getNxDoPrice())) {
            return;
        }
        try {
            BigDecimal expectPrice = new BigDecimal(order.getNxDoExpectPrice().trim());
            BigDecimal actualPrice = new BigDecimal(order.getNxDoPrice().trim());
            order.setNxDoPriceDifferent(actualPrice.subtract(expectPrice).toPlainString());
        } catch (NumberFormatException ignored) {
            // 沿用现网：非法数字时不写差价
        }
    }

    /**
     * 修改实际价并重算小计、差价（重量优先，否则用订货数量）。
     */
    public static void applyActualPriceChange(NxDepartmentOrdersEntity order, String newPrice) {
        if (order == null || newPrice == null) {
            return;
        }
        order.setNxDoPrice(newPrice.trim());
        recalculateSubtotalFromActualPrice(order);
        recalculatePriceDifferent(order);
    }

    public static void recalculateSubtotalFromActualPrice(NxDepartmentOrdersEntity order) {
        if (order == null || !SalesPriceUtils.isValidSalesPrice(order.getNxDoPrice())) {
            return;
        }
        String multiplier = resolveQuantityMultiplier(order);
        if (StringUtils.isBlank(multiplier)) {
            return;
        }
        try {
            BigDecimal qty = new BigDecimal(multiplier.trim());
            BigDecimal price = new BigDecimal(order.getNxDoPrice().trim());
            order.setNxDoSubtotal(qty.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
        } catch (NumberFormatException ignored) {
        }
    }

    private static String resolveQuantityMultiplier(NxDepartmentOrdersEntity order) {
        if (StringUtils.isNotBlank(order.getNxDoWeight())) {
            return order.getNxDoWeight();
        }
        return order.getNxDoQuantity();
    }
}
