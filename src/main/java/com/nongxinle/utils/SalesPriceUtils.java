package com.nongxinle.utils;

import java.math.BigDecimal;

/**
 * 销售价有效性（原系统 0.1 为未定价占位价，有效价须严格 &gt; 0.1）
 */
public final class SalesPriceUtils {

    public static final BigDecimal MIN_VALID_SALES_PRICE = new BigDecimal("0.1");

    private SalesPriceUtils() {
    }

    public static boolean isValidSalesPrice(String price) {
        if (price == null || price.trim().isEmpty()) {
            return false;
        }
        try {
            return new BigDecimal(price.trim()).compareTo(MIN_VALID_SALES_PRICE) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
