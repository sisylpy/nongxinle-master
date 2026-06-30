package com.nongxinle.route.dispatch.strategy;

import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteSandboxStopKeyUtils;
import com.nongxinle.route.DisShipmentTaskStatus;

import java.util.Set;

import static com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate;

/** PR-2b：判定站点是否可进入 legacy optimizer 子集。 */
public final class DispatchStrategyOptimizerEligibility {

    private DispatchStrategyOptimizerEligibility() {
    }

    /**
     * 仅 pending virtual SIMULATED 且坐标有效、非 confirmed、非 manualLocked 可进 optimizer。
     * confirmed / 执行中 / manualLocked 必须 frozen，不得进入 fallback 子集。
     */
    public static boolean isPendingOptimizerCandidate(NxDisShipmentTaskEntity task,
                                                      Set<Integer> confirmedDepIds) {
        if (task == null || task.getNxDstDepFatherId() == null) {
            return false;
        }
        if (confirmedDepIds != null && confirmedDepIds.contains(task.getNxDstDepFatherId())) {
            return false;
        }
        if (task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1) {
            return false;
        }
        if (!DisShipmentTaskStatus.SIMULATED.equals(task.getNxDstStatus())) {
            return false;
        }
        return isValidCoordinate(task.getNxDstLat(), task.getNxDstLng());
    }

    public static void addFrozenDebugEntry(DispatchAssignmentPlan plan,
                                           Integer depFatherId,
                                           String sandboxStopKey,
                                           String frozenReason) {
        if (plan == null || depFatherId == null) {
            return;
        }
        FrozenStopAssignment frozen = new FrozenStopAssignment();
        frozen.setDepFatherId(depFatherId);
        frozen.setSandboxStopKey(sandboxStopKey != null
                ? sandboxStopKey
                : DisRouteSandboxStopKeyUtils.build(depFatherId));
        frozen.setPlanningReason(DispatchPlanningReason.MANUAL_LOCKED_FROZEN);
        frozen.setFrozenReason(frozenReason);
        plan.getFrozenStops().add(frozen);
    }
}
