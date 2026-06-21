package com.nongxinle.service.impl;

import com.nongxinle.dto.route.RouteDispatchPriorityPreview;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteCustomerTier;
import com.nongxinle.route.DisRouteDispatchLabels;

/**
 * Phase 2b-5：大客户优先 preview（不重排路线）。
 */
public final class DisRoutePriorityPreviewHelper {

    private DisRoutePriorityPreviewHelper() {
    }

    public static RouteDispatchPriorityPreview preview(NxDisShipmentTaskEntity task) {
        RouteDispatchPriorityPreview preview = new RouteDispatchPriorityPreview();
        if (task == null) {
            preview.setPriorityScorePreview(0);
            preview.setPriorityReason("无任务数据");
            return preview;
        }
        String tier = DisRouteCustomerTier.normalize(task.getNxDstCustomerTier());
        int tierScore = tierBaseScore(tier);
        int weight = task.getNxDstPriorityWeight() != null ? task.getNxDstPriorityWeight() : 0;
        int orderCount = task.getNxDstOrderCount() != null ? task.getNxDstOrderCount() : 0;
        int itemCount = task.getNxDstItemCount() != null ? task.getNxDstItemCount() : orderCount;
        int qtyBonus = parseQuantityBonus(task.getNxDstTotalQuantity());

        int score = tierScore + weight + orderCount * 10 + itemCount * 5 + qtyBonus;
        preview.setPriorityScorePreview(score);
        preview.setPriorityReason(buildReason(tier, weight, orderCount, itemCount, qtyBonus));
        return preview;
    }

    public static void enrichTask(NxDisShipmentTaskEntity task) {
        if (task == null) {
            return;
        }
        task.setCustomerTierLabel(DisRouteDispatchLabels.label(
                DisRouteCustomerTier.normalize(task.getNxDstCustomerTier())));
        RouteDispatchPriorityPreview preview = preview(task);
        task.setPriorityScorePreview(preview.getPriorityScorePreview());
        task.setPriorityReason(preview.getPriorityReason());
    }

    public static void enrichStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return;
        }
        stop.setCustomerTierLabel(DisRouteDispatchLabels.label(
                DisRouteCustomerTier.normalize(stop.getNxDrsCustomerTier())));
        NxDisShipmentTaskEntity proxy = new NxDisShipmentTaskEntity();
        proxy.setNxDstCustomerTier(stop.getNxDrsCustomerTier());
        proxy.setNxDstPriorityWeight(stop.getNxDrsPriorityWeight());
        proxy.setNxDstOrderCount(stop.getNxDrsOrderCount());
        proxy.setNxDstItemCount(stop.getNxDrsItemCount());
        proxy.setNxDstTotalQuantity(stop.getNxDrsTotalQuantity());
        RouteDispatchPriorityPreview preview = preview(proxy);
        stop.setPriorityScorePreview(preview.getPriorityScorePreview());
        stop.setPriorityReason(preview.getPriorityReason());
    }

    private static int tierBaseScore(String tier) {
        if (DisRouteCustomerTier.VIP.equals(tier)) {
            return 100;
        }
        if (DisRouteCustomerTier.NEW.equals(tier)) {
            return 30;
        }
        if (DisRouteCustomerTier.SMALL.equals(tier)) {
            return 20;
        }
        return 50;
    }

    private static int parseQuantityBonus(String totalQuantity) {
        if (totalQuantity == null || totalQuantity.trim().isEmpty()) {
            return 0;
        }
        try {
            return new java.math.BigDecimal(totalQuantity.trim()).intValue();
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static String buildReason(String tier, int weight, int orderCount, int itemCount, int qtyBonus) {
        String tierLabel = DisRouteDispatchLabels.label(tier);
        StringBuilder reason = new StringBuilder();
        reason.append(tierLabel != null ? tierLabel : tier);
        if (weight > 0) {
            reason.append("，权重+").append(weight);
        }
        if (orderCount > 0) {
            reason.append("，").append(orderCount).append("单");
        }
        if (itemCount > orderCount) {
            reason.append("，").append(itemCount).append("行");
        }
        if (qtyBonus > 0) {
            reason.append("，数量+").append(qtyBonus);
        }
        return reason.toString();
    }
}
