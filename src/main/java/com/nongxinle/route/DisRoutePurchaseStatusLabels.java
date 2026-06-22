package com.nongxinle.route;

import com.nongxinle.utils.NxDistributerTypeUtils;

/** 订单采购/出库/装车状态 → 中文 label（装车读模型 orders[]） */
public final class DisRoutePurchaseStatusLabels {

    private DisRoutePurchaseStatusLabels() {
    }

    public static String label(Integer purchaseStatus) {
        if (purchaseStatus == null) {
            return null;
        }
        if (purchaseStatus.equals(NxDistributerTypeUtils.getNxDepOrderBuyStatusFinishLoad())) {
            return "已装车";
        }
        if (purchaseStatus.equals(NxDistributerTypeUtils.getNxDepOrderBuyStatusFinishOut())) {
            return "出库完成";
        }
        if (purchaseStatus.equals(NxDistributerTypeUtils.getNxDepOrderBuyStatusFinishPurchase())) {
            return "采购完成";
        }
        if (purchaseStatus.equals(NxDistributerTypeUtils.getNxDepOrderBuyStatusIsPurchase())) {
            return "已采购";
        }
        if (purchaseStatus.equals(NxDistributerTypeUtils.getNxDepOrderBuyStatusWithPurchase())) {
            return "备货中";
        }
        if (purchaseStatus.equals(NxDistributerTypeUtils.getNxDepOrderBuyStatusUnPurchase())) {
            return "未采购";
        }
        return "状态" + purchaseStatus;
    }
}
