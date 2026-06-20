package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.LineAmountConfirmResult;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.utils.SalesPriceUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 行级金额可确认判定：不单看 price&gt;0.1，也不因 nxDgGoodsIsWeight=1 一律 PENDING。
 */
@Service
public class PlatformLineAmountConfirmServiceImpl implements PlatformLineAmountConfirmService {

    private static final List<Set<String>> STANDARD_UNIT_SYNONYM_GROUPS = Arrays.asList(
            new HashSet<>(Arrays.asList("件", "箱")),
            new HashSet<>(Arrays.asList("袋", "包", "代"))
    );

    private static final Set<String> WEIGHT_UNITS = new HashSet<>(Arrays.asList("斤", "kg", "KG", "公斤", "g", "G", "克"));
    private static final Set<String> COUNT_UNITS = new HashSet<>(Arrays.asList("个", "只", "根", "条", "块", "瓶", "桶", "听", "罐"));
    private static final Set<String> BUNDLE_UNITS = new HashSet<>(Arrays.asList("把", "捆", "扎"));

    @Override
    public LineAmountConfirmResult isLineAmountConfirmable(String quantity, String orderStandard,
                                                           NxDistributerGoodsEntity goods) {
        if (goods == null) {
            return LineAmountConfirmResult.pending("GOODS_NOT_FOUND");
        }

        BigDecimal qty = parsePositiveQuantity(quantity);
        if (qty == null) {
            return LineAmountConfirmResult.pending("INVALID_QUANTITY");
        }

        String normalizedOrderStandard = normalizeStandard(orderStandard);
        if (StringUtils.isBlank(normalizedOrderStandard)) {
            return LineAmountConfirmResult.pending("MISSING_ORDER_STANDARD");
        }

        ResolvedPrice resolvedPrice = resolveSalesPrice(goods, normalizedOrderStandard);
        if (resolvedPrice != null && SalesPriceUtils.isValidSalesPrice(resolvedPrice.price)) {
            String pricingStandard = normalizeStandard(resolvedPrice.standard);
            BigDecimal subtotal = qty.multiply(new BigDecimal(resolvedPrice.price.trim()))
                    .setScale(2, RoundingMode.HALF_UP);
            return LineAmountConfirmResult.confirmed(
                    new BigDecimal(resolvedPrice.price.trim()).setScale(2, RoundingMode.HALF_UP),
                    pricingStandard,
                    subtotal
            );
        }

        ResolvedPrice primaryPrice = resolvePrimarySalesPrice(goods);
        if (primaryPrice == null || !SalesPriceUtils.isValidSalesPrice(primaryPrice.price)) {
            return LineAmountConfirmResult.pending("INVALID_PRICE");
        }

        String pricingStandard = normalizeStandard(primaryPrice.standard);
        if (StringUtils.isBlank(pricingStandard)) {
            return LineAmountConfirmResult.pending("MISSING_PRICE_STANDARD");
        }

        if (!canOrderQuantityServeAsPricingQuantity(normalizedOrderStandard, pricingStandard, goods)) {
            if (isNonConvertibleCountOrBundleToWeight(normalizedOrderStandard, pricingStandard)) {
                return LineAmountConfirmResult.pending("NEED_WEIGH");
            }
            return LineAmountConfirmResult.pending("UNIT_MISMATCH");
        }

        BigDecimal subtotal = qty.multiply(new BigDecimal(primaryPrice.price.trim()))
                .setScale(2, RoundingMode.HALF_UP);
        return LineAmountConfirmResult.confirmed(
                new BigDecimal(primaryPrice.price.trim()).setScale(2, RoundingMode.HALF_UP),
                pricingStandard,
                subtotal
        );
    }

    private static BigDecimal parsePositiveQuantity(String quantity) {
        if (StringUtils.isBlank(quantity)) {
            return null;
        }
        try {
            BigDecimal qty = new BigDecimal(quantity.trim());
            return qty.compareTo(BigDecimal.ZERO) > 0 ? qty : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String normalizeStandard(String standard) {
        return standard == null ? null : standard.trim();
    }

    /**
     * 下单数量本身能否作为计价数量：单位一致或可可靠换算，且不需要现场称重补斤数。
     */
    private static boolean canOrderQuantityServeAsPricingQuantity(String orderStandard, String pricingStandard,
                                                                  NxDistributerGoodsEntity goods) {
        if (standardsMatch(orderStandard, pricingStandard)) {
            return true;
        }
        if (isStandardMatchCore(orderStandard, goods.getNxDgCartonUnit())
                && standardsMatch(normalizeStandard(goods.getNxDgCartonUnit()), pricingStandard)) {
            return StringUtils.isNotBlank(goods.getNxDgItemsPerCarton());
        }
        return false;
    }

    private static boolean isNonConvertibleCountOrBundleToWeight(String orderStandard, String pricingStandard) {
        if (!WEIGHT_UNITS.contains(pricingStandard)) {
            return false;
        }
        return COUNT_UNITS.contains(orderStandard)
                || BUNDLE_UNITS.contains(orderStandard)
                || isPackUnit(orderStandard);
    }

    private static boolean isPackUnit(String standard) {
        return "件".equals(standard) || "箱".equals(standard)
                || "袋".equals(standard) || "包".equals(standard) || "代".equals(standard);
    }

    private static boolean standardsMatch(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.equals(right)) {
            return true;
        }
        return isStandardMatchCore(left, right);
    }

    private static boolean isStandardMatchCore(String orderStandard, String otherStandard) {
        if (orderStandard == null || otherStandard == null) {
            return false;
        }
        String orderStd = orderStandard.trim();
        String other = otherStandard.trim();
        if (orderStd.isEmpty() || other.isEmpty()) {
            return false;
        }
        if (orderStd.equals(other)) {
            return true;
        }
        for (Set<String> group : STANDARD_UNIT_SYNONYM_GROUPS) {
            if (group.contains(orderStd) && group.contains(other)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 商品主销售价（按商品计价单位），不随下单单位变化；单位是否匹配单独判断。
     */
    private static ResolvedPrice resolvePrimarySalesPrice(NxDistributerGoodsEntity goods) {
        if (SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPrice())) {
            return new ResolvedPrice(goods.getNxDgWillPrice(), goods.getNxDgGoodsStandardname());
        }
        if (SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPriceOne())) {
            String std = StringUtils.isNotBlank(goods.getNxDgWillPriceOneStandard())
                    ? goods.getNxDgWillPriceOneStandard()
                    : goods.getNxDgGoodsStandardname();
            return new ResolvedPrice(goods.getNxDgWillPriceOne(), std);
        }
        if (SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPriceTwo())) {
            return new ResolvedPrice(goods.getNxDgWillPriceTwo(), goods.getNxDgWillPriceTwoStandard());
        }
        if (SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPriceThree())) {
            return new ResolvedPrice(goods.getNxDgWillPriceThree(), goods.getNxDgWillPriceThreeStandard());
        }
        return null;
    }

    private static ResolvedPrice resolveSalesPrice(NxDistributerGoodsEntity goods, String orderStandard) {
        if (orderStandardMatchesTier(orderStandard, goods.getNxDgWillPriceTwoStandard())
                && SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPriceTwo())) {
            return new ResolvedPrice(goods.getNxDgWillPriceTwo(), goods.getNxDgWillPriceTwoStandard());
        }
        if (orderStandardMatchesTier(orderStandard, goods.getNxDgWillPriceThreeStandard())
                && SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPriceThree())) {
            return new ResolvedPrice(goods.getNxDgWillPriceThree(), goods.getNxDgWillPriceThreeStandard());
        }
        if (orderStandardMatchesFirstTier(orderStandard, goods)
                && SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPriceOne())) {
            String std = StringUtils.isNotBlank(goods.getNxDgWillPriceOneStandard())
                    ? goods.getNxDgWillPriceOneStandard()
                    : goods.getNxDgGoodsStandardname();
            return new ResolvedPrice(goods.getNxDgWillPriceOne(), std);
        }
        if (standardsMatch(orderStandard, goods.getNxDgGoodsStandardname())
                && SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPrice())) {
            return new ResolvedPrice(goods.getNxDgWillPrice(), goods.getNxDgGoodsStandardname());
        }
        if (standardsMatch(orderStandard, goods.getNxDgGoodsStandardname())
                && SalesPriceUtils.isValidSalesPrice(goods.getNxDgWillPriceOne())) {
            return new ResolvedPrice(goods.getNxDgWillPriceOne(), goods.getNxDgGoodsStandardname());
        }
        return null;
    }

    private static boolean orderStandardMatchesFirstTier(String orderStandard, NxDistributerGoodsEntity goods) {
        if (orderStandardMatchesTier(orderStandard, goods.getNxDgWillPriceOneStandard())) {
            return true;
        }
        return standardsMatch(orderStandard, goods.getNxDgGoodsStandardname());
    }

    private static boolean orderStandardMatchesTier(String orderStandard, String tierStandard) {
        return StringUtils.isNotBlank(tierStandard) && standardsMatch(orderStandard, tierStandard.trim());
    }

    private static final class ResolvedPrice {
        private final String price;
        private final String standard;

        private ResolvedPrice(String price, String standard) {
            this.price = price;
            this.standard = standard;
        }
    }
}
