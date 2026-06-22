package com.nongxinle.route;

import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;

import java.util.List;

import static com.nongxinle.route.DisRouteBillPrintStatusHelper.PRINT_FULL;
import static com.nongxinle.route.DisRouteBillPrintStatusHelper.PRINT_PARTIAL;
import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;

/**
 * Phase 3c：返回沙盘 — 仅撤销派车执行关系，不阻断 bill/配送单。
 * 强阻断只看配送执行态（IN_DELIVERY / DELIVERED / 整车已出发）。
 */
public final class DisRouteReturnToSandboxPolicy {

    public static final String ACTION_LABEL = "返回沙盘";

    private DisRouteReturnToSandboxPolicy() {
    }

    public static RouteDispatchOperationDecision evaluate(NxDisShipmentTaskEntity task,
                                                          List<NxDisShipmentTaskEntity> siblingTasksOnRoute,
                                                          com.nongxinle.entity.NxDisDriverRouteEntity driverRoute) {
        if (task == null || task.getNxDstId() == null) {
            return RouteDispatchOperationDecision.deny("未找到已确认派车记录");
        }
        if (driverRoute != null && DisRouteDriverDepartPolicy.isRouteDeparted(driverRoute)) {
            return RouteDispatchOperationDecision.deny("司机已出发，不能返回沙盘");
        }
        String status = task.getNxDstStatus();
        if (DisShipmentTaskStatus.CANCELLED.equals(status)) {
            return RouteDispatchOperationDecision.deny("该店已返回沙盘或已取消");
        }
        if (DisShipmentTaskStatus.IN_DELIVERY.equals(status)) {
            return RouteDispatchOperationDecision.deny("该店配送中，不能返回沙盘");
        }
        if (DisShipmentTaskStatus.DELIVERED.equals(status)) {
            return RouteDispatchOperationDecision.deny("该店已完成配送，不能返回沙盘");
        }
        if (DisShipmentTaskStatus.EXCEPTION.equals(status)) {
            return RouteDispatchOperationDecision.deny("该店配送异常，不能返回沙盘");
        }
        if (DisShipmentTaskStatus.CLOSED.equals(status)) {
            return RouteDispatchOperationDecision.deny("该店派车记录已关闭，不能返回沙盘");
        }
        if (isDriverRouteDeparted(siblingTasksOnRoute, task.getNxDstId())) {
            return RouteDispatchOperationDecision.deny("司机已确认出发，不能返回沙盘");
        }
        if (!DisShipmentTaskStatus.ASSIGNED.equals(status)
                && !DisShipmentTaskStatus.READY_TO_GO.equals(status)) {
            return RouteDispatchOperationDecision.deny("当前状态不允许返回沙盘");
        }

        RouteDispatchOperationDecision ok = RouteDispatchOperationDecision.allow();
        ok.setOperationHint(buildBillPrintedReminder(task));
        return ok;
    }

    private static boolean isDriverRouteDeparted(List<NxDisShipmentTaskEntity> siblingTasksOnRoute,
                                                 Integer currentTaskId) {
        if (siblingTasksOnRoute == null || siblingTasksOnRoute.isEmpty()) {
            return false;
        }
        for (NxDisShipmentTaskEntity sibling : siblingTasksOnRoute) {
            if (sibling == null || sibling.getNxDstId() == null) {
                continue;
            }
            if (currentTaskId != null && currentTaskId.equals(sibling.getNxDstId())) {
                continue;
            }
            String siblingStatus = sibling.getNxDstStatus();
            if (DisShipmentTaskStatus.IN_DELIVERY.equals(siblingStatus)
                    || DisShipmentTaskStatus.DELIVERED.equals(siblingStatus)
                    || DisShipmentTaskStatus.EXCEPTION.equals(siblingStatus)) {
                return true;
            }
        }
        return false;
    }

    public static String buildBillPrintedReminder(NxDisShipmentTaskEntity task) {
        DisRouteBillPrintStatusHelper.BillPrintSummary summary = DisRouteBillPrintStatusHelper.summarize(task);
        if (PRINT_FULL.equals(summary.status) || PRINT_PARTIAL.equals(summary.status)) {
            return "该店配送单已打印，返回沙盘后请注意是否需要补打或重打";
        }
        if (task != null && task.getItems() != null) {
            for (NxDisShipmentTaskItemEntity item : task.getItems()) {
                if (item != null && ACTIVE.equals(item.getNxDstiItemStatus())
                        && (item.getNxDstiBillId() != null || item.getNxDstiHistoryOrderId() != null)) {
                    return "该店配送单已打印，返回沙盘后请注意是否需要补打或重打";
                }
            }
        }
        return null;
    }

    public static String buildConfirmDialogMessage(NxDisShipmentTaskEntity task) {
        String base = "取消后，该店会回到动态沙盘，系统会重新计算司机和路线。订单和配送单不会删除。";
        String billHint = buildBillPrintedReminder(task);
        if (billHint != null && !billHint.isEmpty()) {
            return base + billHint;
        }
        return base;
    }
}
