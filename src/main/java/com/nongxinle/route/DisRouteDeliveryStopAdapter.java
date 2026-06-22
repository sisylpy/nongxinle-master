package com.nongxinle.route;

import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

/**
 * Phase 3a.1：从 shipment_task 构建内存 delivery stop 读模型（不写 route_stop）。
 */
public final class DisRouteDeliveryStopAdapter {

    private DisRouteDeliveryStopAdapter() {
    }

    /** 优先 task 字段；legacyStop 仅作 schedule 字段 fallback。 */
    public static NxDisRouteStopEntity fromTask(NxDisShipmentTaskEntity task,
                                                NxDisRouteStopEntity legacyStop) {
        NxDisRouteStopEntity stop = new NxDisRouteStopEntity();
        if (task == null) {
            return stop;
        }
        stop.setShipmentTask(task);
        stop.setNxDrsShipmentTaskId(task.getNxDstId());
        stop.setNxDrsDepartmentId(task.getNxDstDepFatherId());
        stop.setNxDrsDepartmentName(task.getNxDstDepName());
        stop.setNxDrsLat(task.getNxDstLat());
        stop.setNxDrsLng(task.getNxDstLng());
        stop.setNxDrsAddress(task.getNxDstAddress());
        stop.setNxDrsOrderCount(task.getNxDstOrderCount());
        stop.setNxDrsCustomerTier(task.getNxDstCustomerTier());
        stop.setNxDrsPriorityWeight(task.getNxDstPriorityWeight());
        stop.setNxDrsItemCount(task.getNxDstItemCount());
        stop.setNxDrsTotalQuantity(task.getNxDstTotalQuantity());
        stop.setNxDrsEarliestDeliveryTimeS(task.getNxDstEarliestDeliveryTimeS());
        stop.setNxDrsLatestDeliveryTimeS(task.getNxDstLatestDeliveryTimeS());
        stop.setNxDrsServiceMinutes(task.getNxDstServiceMinutes());
        stop.setNxDrsTimeWindowOverrideFlag(task.getNxDstTimeWindowOverrideFlag());
        stop.setNxDrsTimeWindowAdjustReason(task.getNxDstTimeWindowAdjustReason());

        Integer driverRouteId = task.getNxDstDriverRouteId();
        if (driverRouteId == null && legacyStop != null) {
            driverRouteId = legacyStop.getNxDrsDriverRouteId();
        }
        stop.setNxDrsDriverRouteId(driverRouteId);

        Integer stopSeq = task.getNxDstRouteSeq() != null ? task.getNxDstRouteSeq() : task.getNxDstManualStopSeq();
        if (stopSeq == null && legacyStop != null) {
            stopSeq = legacyStop.getNxDrsStopSeq();
        }
        stop.setNxDrsStopSeq(stopSeq);

        Long legDistance = task.getNxDstLegDistanceM();
        Long legDuration = task.getNxDstLegDurationS();
        if (legDistance == null && legacyStop != null) {
            legDistance = legacyStop.getNxDrsLegDistanceM();
        }
        if (legDuration == null && legacyStop != null) {
            legDuration = legacyStop.getNxDrsLegDurationS();
        }
        stop.setNxDrsLegDistanceM(legDistance);
        stop.setNxDrsLegDurationS(legDuration);
        stop.setLegDistanceProvider(task.getLegDistanceProvider());
        stop.setLegDistanceType(task.getLegDistanceType());

        stop.setNxDrsPlannedArrivalAt(firstNonNull(task.getNxDstPlannedArrivalAt(),
                legacyStop != null ? legacyStop.getNxDrsPlannedArrivalAt() : null));
        stop.setNxDrsPlannedServiceStartAt(firstNonNull(task.getNxDstPlannedServiceStartAt(),
                legacyStop != null ? legacyStop.getNxDrsPlannedServiceStartAt() : null));
        stop.setNxDrsPlannedDepartureAt(firstNonNull(task.getNxDstPlannedDepartureAt(),
                legacyStop != null ? legacyStop.getNxDrsPlannedDepartureAt() : null));
        stop.setNxDrsWaitMinutes(firstNonNullInt(task.getNxDstWaitMinutes(),
                legacyStop != null ? legacyStop.getNxDrsWaitMinutes() : null));
        stop.setNxDrsLateMinutes(firstNonNullInt(task.getNxDstLateMinutes(),
                legacyStop != null ? legacyStop.getNxDrsLateMinutes() : null));
        String twStatus = task.getNxDstTimeWindowStatus();
        if (twStatus == null && legacyStop != null) {
            twStatus = legacyStop.getNxDrsTimeWindowStatus();
        }
        stop.setNxDrsTimeWindowStatus(twStatus);
        stop.setStopSource(task.getStopSource());
        stop.setSandboxStopKey(task.getSandboxStopKey());
        stop.setConfirmViaSandbox(task.getConfirmViaSandbox());
        stop.setLiveDepartmentName(task.getLiveDepartmentName());
        return stop;
    }

    private static <T> T firstNonNull(T primary, T fallback) {
        return primary != null ? primary : fallback;
    }

    private static Integer firstNonNullInt(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }
}
