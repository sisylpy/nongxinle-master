package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.PlatformSupplierRow;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.utils.SalesPriceUtils;

/**
 * 平台化：配送商商品可售/报价校验（与 assign、suppliers 共用）。
 * 仅使用 nx_dg_will_price_one / nx_dg_will_price，不使用进价字段。
 */
public final class PlatformDisGoodsValidator {

    public static final String MSG_INVALID_DIS_GOODS_SALES_PRICE = "该配送商商品未设置有效销售价";

    public static final String MSG_INVALID_ORDER_PRICE_AFTER_PRICING =
            "分配后订单价格无效（为空或≤0.1），请检查配送商销售价或客户历史价";

    private PlatformDisGoodsValidator() {
    }

    public static boolean isAssignableSaleable(NxDistributerGoodsEntity goods) {
        if (goods == null) {
            return false;
        }
        if (goods.getNxDgNxGoodsId() == null || goods.getNxDgNxGoodsId() <= 0) {
            return false;
        }
        if (goods.getNxDgDfgGoodsFatherId() == null) {
            return false;
        }
        if (goods.getNxDgPullOff() != null && goods.getNxDgPullOff() != 0) {
            return false;
        }
        if (goods.getNxDgGoodsIsHidden() != null && goods.getNxDgGoodsIsHidden() != 0) {
            return false;
        }
        return hasValidSalesQuote(goods);
    }

    public static boolean hasValidSalesQuote(NxDistributerGoodsEntity goods) {
        return SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPriceOne())
                || SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPrice());
    }

    public static boolean hasValidSalesQuote(PlatformSupplierRow row) {
        return SalesPriceUtils.isValidSalesPrice(row.getWillPriceOne())
                || SalesPriceUtils.isValidSalesPrice(row.getWillPrice());
    }

    public static boolean isValidOrderPrice(String price) {
        return SalesPriceUtils.isValidSalesPrice(price);
    }

    public static String resolveCurrentQuotePrice(NxDistributerGoodsEntity goods) {
        if (SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPriceOne())) {
            return goods.getNxDgWillPriceOne().trim();
        }
        if (SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPrice())) {
            return goods.getNxDgWillPrice().trim();
        }
        return null;
    }

    public static String resolveCurrentQuotePrice(PlatformSupplierRow row) {
        if (SalesPriceUtils.isValidSalesPrice(row.getWillPriceOne())) {
            return row.getWillPriceOne().trim();
        }
        if (SalesPriceUtils.isValidSalesPrice(row.getWillPrice())) {
            return row.getWillPrice().trim();
        }
        return null;
    }
}
