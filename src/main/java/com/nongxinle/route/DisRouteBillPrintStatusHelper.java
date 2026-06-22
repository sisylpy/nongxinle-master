package com.nongxinle.route;

import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;

/**
 * Phase 3a.1：配送单打印弱参考 — 只产出提示字段，不作状态硬门槛。
 */
public final class DisRouteBillPrintStatusHelper {

    public static final String PRINT_FULL = "FULL";
    public static final String PRINT_PARTIAL = "PARTIAL";
    public static final String PRINT_NONE = "NONE";

    private DisRouteBillPrintStatusHelper() {
    }

    public static Map<String, Object> buildBillPrintHints(NxDisShipmentTaskEntity task) {
        Map<String, Object> hints = new HashMap<String, Object>();
        BillPrintSummary summary = summarize(task);
        hints.put("billPrintStatus", summary.status);
        hints.put("unprintedBillCount", summary.unprintedCount);
        hints.put("activeItemCount", summary.activeCount);
        hints.put("printedItemCount", summary.printedCount);
        if (summary.unprintedCount > 0) {
            hints.put("billPrintWarning",
                    "还有 " + summary.unprintedCount + " 个订单配送单未打印，可继续装车/出发");
        } else {
            hints.put("billPrintWarning", null);
        }
        return hints;
    }

    public static BillPrintSummary summarize(NxDisShipmentTaskEntity task) {
        BillPrintSummary summary = new BillPrintSummary();
        List<NxDisShipmentTaskItemEntity> items = task != null ? task.getItems() : null;
        if (items == null || items.isEmpty()) {
            summary.status = PRINT_NONE;
            return summary;
        }
        for (NxDisShipmentTaskItemEntity item : items) {
            if (item == null || !ACTIVE.equals(item.getNxDstiItemStatus())) {
                continue;
            }
            summary.activeCount++;
            if (item.getNxDstiBillId() != null || item.getNxDstiHistoryOrderId() != null) {
                summary.printedCount++;
            } else {
                summary.unprintedCount++;
            }
        }
        if (summary.activeCount == 0) {
            summary.status = PRINT_NONE;
        } else if (summary.unprintedCount == 0) {
            summary.status = PRINT_FULL;
        } else if (summary.printedCount == 0) {
            summary.status = PRINT_NONE;
        } else {
            summary.status = PRINT_PARTIAL;
        }
        return summary;
    }

    public static final class BillPrintSummary {
        public String status = PRINT_NONE;
        public int activeCount;
        public int printedCount;
        public int unprintedCount;
    }
}
