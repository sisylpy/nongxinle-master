package com.nongxinle.service.platform;

import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.utils.SalesPriceUtils;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

/**
 * 平台 assign 阶段 buying 成本价解析（Round 1 简化版，不做体重/规格分档）。
 * 有效成本须严格 &gt; 0.1；禁止用销售价或 0.1 占位价冒充成本。
 */
public final class PlatformDisGoodsCostResolver {

    public static final String REASON_NO_VALID_BUYING_PRICE = "NO_VALID_BUYING_PRICE";

    private PlatformDisGoodsCostResolver() {
    }

    @Getter
    public static final class CostResolveResult {
        private final String costPrice;
        private final String costPriceUpdate;
        private final String costPriceLevel;
        private final boolean costMissing;
        private final String costMissingReason;

        private CostResolveResult(String costPrice, String costPriceUpdate, String costPriceLevel,
                                  boolean costMissing, String costMissingReason) {
            this.costPrice = costPrice;
            this.costPriceUpdate = costPriceUpdate;
            this.costPriceLevel = costPriceLevel;
            this.costMissing = costMissing;
            this.costMissingReason = costMissingReason;
        }

        public static CostResolveResult resolved(String costPrice, String costPriceUpdate, String costPriceLevel) {
            return new CostResolveResult(costPrice, costPriceUpdate, costPriceLevel, false, null);
        }

        public static CostResolveResult missing(String reason) {
            return new CostResolveResult(null, null, null, true, reason);
        }
    }

    /**
     * Round 1：按 nxDgBuyingPriceOne → Two → Three → nxDgBuyingPrice 顺序取首个有效价。
     * 复杂规格/体重分档留 Round 1.5。
     */
    public static CostResolveResult resolve(NxDistributerGoodsEntity disGoods, NxDepartmentOrdersEntity order) {
        if (disGoods == null) {
            return CostResolveResult.missing(REASON_NO_VALID_BUYING_PRICE);
        }

        CostCandidate one = candidate("1", disGoods.getNxDgBuyingPriceOne(), disGoods.getNxDgBuyingPriceOneUpdate());
        CostCandidate two = candidate("2", disGoods.getNxDgBuyingPriceTwo(), disGoods.getNxDgBuyingPriceTwoUpdate());
        CostCandidate three = candidate("3", disGoods.getNxDgBuyingPriceThree(), disGoods.getNxDgBuyingPriceThreeUpdate());
        CostCandidate base = candidate("0", disGoods.getNxDgBuyingPrice(), disGoods.getNxDgBuyingPriceUpdate());

        CostCandidate chosen = firstValid(one, two, three, base);
        if (chosen == null) {
            return CostResolveResult.missing(REASON_NO_VALID_BUYING_PRICE);
        }
        return CostResolveResult.resolved(chosen.price, chosen.update, chosen.level);
    }

    private static CostCandidate firstValid(CostCandidate... candidates) {
        for (CostCandidate c : candidates) {
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    private static CostCandidate candidate(String level, String price, String update) {
        if (!SalesPriceUtils.isValidSalesPrice(price)) {
            return null;
        }
        return new CostCandidate(level, price.trim(), StringUtils.isNotBlank(update) ? update.trim() : null);
    }

    private static final class CostCandidate {
        private final String level;
        private final String price;
        private final String update;

        private CostCandidate(String level, String price, String update) {
            this.level = level;
            this.price = price;
            this.update = update;
        }
    }
}
