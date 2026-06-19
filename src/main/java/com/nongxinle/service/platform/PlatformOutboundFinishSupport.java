package com.nongxinle.service.platform;

import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.utils.SalesPriceUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

/**
 * 平台单出库完成时的成本价辅助（不阻断出库）。
 */
public final class PlatformOutboundFinishSupport {

    private PlatformOutboundFinishSupport() {
    }

    public static boolean hasValidOrderCostPrice(NxDepartmentOrdersEntity order) {
        return order != null && SalesPriceUtils.isValidSalesPrice(order.getNxDoCostPrice());
    }

    public static void applyCostToOrder(
            NxDepartmentOrdersEntity order, PlatformDisGoodsCostResolver.CostResolveResult costResult) {
        if (order == null || costResult == null || costResult.isCostMissing()) {
            return;
        }
        order.setNxDoCostPrice(costResult.getCostPrice());
        if (costResult.getCostPriceUpdate() != null) {
            order.setNxDoCostPriceUpdate(costResult.getCostPriceUpdate());
        }
        if (costResult.getCostPriceLevel() != null) {
            order.setNxDoCostPriceLevel(costResult.getCostPriceLevel());
        }
    }

    /**
     * 有效成本价时才写 nxDoCostSubtotal；缺失时不抛异常。
     */
    public static void applyCostSubtotalIfValid(NxDepartmentOrdersEntity order, BigDecimal weightB) {
        if (order == null || weightB == null || !hasValidOrderCostPrice(order)) {
            return;
        }
        try {
            BigDecimal price = new BigDecimal(order.getNxDoCostPrice().trim());
            order.setNxDoCostSubtotal(
                    price.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
        } catch (NumberFormatException ignored) {
            // 成本缺失场景：不阻断出库
        }
    }

    public static BigDecimal resolveOutboundWeight(NxDepartmentOrdersEntity order) {
        if (order == null) {
            return null;
        }
        String multiplier = order.getNxDoWeight();
        if (StringUtils.isBlank(multiplier)) {
            multiplier = order.getNxDoQuantity();
        }
        if (StringUtils.isBlank(multiplier)) {
            return null;
        }
        try {
            return new BigDecimal(multiplier.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
