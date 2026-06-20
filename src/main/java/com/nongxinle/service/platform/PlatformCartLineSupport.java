package com.nongxinle.service.platform;

import com.nongxinle.entity.NxDepartmentOrdersEntity;
import org.apache.commons.lang.StringUtils;

/**
 * 平台购物车/Checkout 行级判定（与 assign、price 维度分离）。
 */
public final class PlatformCartLineSupport {

    private PlatformCartLineSupport() {
    }

    /**
     * 客户是否在购物车临时阶段已选定配送商 SKU（checkout 后决定是否 ASSIGNED）。
     * 必须同时有 nxDistributerId 与 nxDistributerGoodsId；仅有占位 distributerId 不算选商。
     */
    public static boolean hasCustomerSelectedSupplier(NxDepartmentOrdersEntity nxOrder) {
        if (nxOrder == null) {
            return false;
        }
        Integer disId = nxOrder.getNxDoDistributerId();
        Integer disGoodsId = nxOrder.getNxDoDisGoodsId();
        return disId != null && disId > 0
                && disGoodsId != null && disGoodsId > 0;
    }

    public static boolean isCartStatus(Integer nxDoStatus) {
        return nxDoStatus != null && nxDoStatus == -1;
    }

    public static boolean isFormalStatus(Integer nxDoStatus) {
        return nxDoStatus != null && nxDoStatus >= 0;
    }

    public static boolean isBlankOrNonPositive(Integer value) {
        return value == null || value <= 0;
    }

    public static boolean needsDepDisGoods(Integer gbDoDepDisGoodsId) {
        return gbDoDepDisGoodsId == null || gbDoDepDisGoodsId <= 0;
    }

    public static boolean needsNxPurchaseGoods(NxDepartmentOrdersEntity nxOrder) {
        if (nxOrder == null) {
            return false;
        }
        Integer purchaseGoodsId = nxOrder.getNxDoPurchaseGoodsId();
        return purchaseGoodsId == null || purchaseGoodsId <= 0;
    }
}
