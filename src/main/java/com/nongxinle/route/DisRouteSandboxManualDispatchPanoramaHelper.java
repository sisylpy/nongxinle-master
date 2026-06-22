package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;
import static com.nongxinle.route.DisRouteSandboxStopSource.SANDBOX_SUGGESTED;
import static com.nongxinle.route.DisShipmentTaskStatus.CLOSED;
import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;

/** Phase 2B-1：人工调度司机全景 — 阶段与能力判定（前端不得自行推断）。 */
public final class DisRouteSandboxManualDispatchPanoramaHelper {

    private DisRouteSandboxManualDispatchPanoramaHelper() {
    }

    public static boolean isOnDuty(String dutyStatus) {
        return ON_DUTY.equals(dutyStatus);
    }

    /** 人工调度：在岗司机均可模拟（阶段只影响风险提示，不禁止）。 */
    public static boolean supportsInsertSimulation(String dispatchStage) {
        return dispatchStage != null && !dispatchStage.trim().isEmpty();
    }

    public static ManualDispatchPanoramaCapabilities resolveCapabilities(String dispatchStage) {
        if (dispatchStage == null) {
            dispatchStage = ManualDispatchDispatchStage.IDLE;
        }
        switch (dispatchStage.trim().toUpperCase()) {
            case ManualDispatchDispatchStage.LOADING:
                return ManualDispatchPanoramaCapabilities.riskAck(loadingRiskHints());
            case ManualDispatchDispatchStage.EXECUTION:
                return ManualDispatchPanoramaCapabilities.riskAck(executionRiskHints());
            case ManualDispatchDispatchStage.COMPLETED:
                return ManualDispatchPanoramaCapabilities.riskAck(completedRiskHints());
            case ManualDispatchDispatchStage.IDLE:
            case ManualDispatchDispatchStage.SANDBOX:
            case ManualDispatchDispatchStage.CONFIRMED:
            default:
                return ManualDispatchPanoramaCapabilities.direct();
        }
    }

    /** simulate / confirm 用的司机当前路线基线站点。 */
    public static List<NxDisRouteStopEntity> collectDriverBaselineStops(SandboxComputeResult compute,
                                                                        Integer driverUserId,
                                                                        String dispatchStage) {
        List<NxDisRouteStopEntity> stops = new ArrayList<NxDisRouteStopEntity>();
        if (compute == null || driverUserId == null) {
            return stops;
        }
        String stage = dispatchStage != null ? dispatchStage.trim().toUpperCase() : ManualDispatchDispatchStage.IDLE;
        switch (stage) {
            case ManualDispatchDispatchStage.LOADING:
                appendDriverStops(stops, compute.getLoadingStops(), driverUserId);
                break;
            case ManualDispatchDispatchStage.EXECUTION:
                appendDriverStops(stops, compute.getExecutionStops(), driverUserId);
                break;
            case ManualDispatchDispatchStage.COMPLETED:
                break;
            default:
                appendDriverStops(stops, compute.getConfirmedStops(), driverUserId);
                appendDriverStops(stops, compute.getSandboxSuggestedStops(), driverUserId);
                break;
        }
        Collections.sort(stops, new java.util.Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                int seqA = a != null && a.getNxDrsStopSeq() != null ? a.getNxDrsStopSeq() : 0;
                int seqB = b != null && b.getNxDrsStopSeq() != null ? b.getNxDrsStopSeq() : 0;
                return Integer.compare(seqA, seqB);
            }
        });
        return stops;
    }

    public static String resolveDispatchStage(NxDisDriverRouteEntity route,
                                              SandboxComputeResult compute,
                                              Integer driverUserId) {
        if (route != null && isCompletedRoute(route)) {
            return ManualDispatchDispatchStage.COMPLETED;
        }
        if (route != null && DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            return ManualDispatchDispatchStage.EXECUTION;
        }
        if (route != null && DisRouteLoadingGateHelper.isRouteEnteredLoading(route)) {
            return ManualDispatchDispatchStage.LOADING;
        }
        if (countSandboxSuggestedStops(compute, driverUserId) > 0) {
            return ManualDispatchDispatchStage.SANDBOX;
        }
        if (countConfirmedDispatchStops(compute, driverUserId) > 0) {
            return ManualDispatchDispatchStage.CONFIRMED;
        }
        return ManualDispatchDispatchStage.IDLE;
    }

    public static DriverStopCounts resolveStopCounts(SandboxComputeResult compute,
                                                     NxDisDriverRouteEntity route,
                                                     Integer driverUserId) {
        List<NxDisRouteStopEntity> allStops = collectAllDriverStops(compute, driverUserId);
        int completed = 0;
        for (NxDisRouteStopEntity stop : allStops) {
            if (isDeliveredStop(stop)) {
                completed++;
            }
        }
        int total = allStops.size();
        if (total <= 0 && route != null && route.getTotalStopCount() != null && route.getTotalStopCount() > 0) {
            total = route.getTotalStopCount();
        }
        int pending = Math.max(0, total - completed);
        return new DriverStopCounts(total, completed, pending);
    }

    public static String buildRouteSummary(NxDisDriverRouteEntity route,
                                           DriverStopCounts counts,
                                           String dispatchStage) {
        int stopCount = counts != null ? counts.getCurrentStopCount() : 0;
        if (stopCount <= 0) {
            if (ManualDispatchDispatchStage.COMPLETED.equals(dispatchStage)) {
                return "已完成 · 可再派";
            }
            if (ManualDispatchDispatchStage.LOADING.equals(dispatchStage)) {
                return "装车中";
            }
            if (ManualDispatchDispatchStage.EXECUTION.equals(dispatchStage)) {
                return "配送中";
            }
            return null;
        }
        String distanceText = null;
        String durationText = null;
        if (route != null) {
            distanceText = DisRouteSandboxDisplayFormatHelper.formatDistanceText(route.getNxDdrTotalDistanceM());
            durationText = DisRouteSandboxDisplayFormatHelper.formatDurationText(route.getNxDdrTotalDurationS());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(stopCount).append(" 个客户");
        if (ManualDispatchDispatchStage.LOADING.equals(dispatchStage)) {
            sb.append(" · 装车中");
        } else if (ManualDispatchDispatchStage.EXECUTION.equals(dispatchStage)) {
            sb.append(" · 配送中");
        }
        if (distanceText != null && !distanceText.isEmpty()) {
            sb.append(" · ").append(distanceText);
        }
        if (durationText != null && !durationText.isEmpty()) {
            sb.append(" · ").append(durationText);
        }
        return sb.toString();
    }

    public static String buildOperationHint(String dispatchStage, int pendingStopCount) {
        if (ManualDispatchDispatchStage.LOADING.equals(dispatchStage)) {
            return "可插入装车路线，请关注装车顺序影响";
        }
        if (ManualDispatchDispatchStage.EXECUTION.equals(dispatchStage)) {
            return "可插入剩余配送路线，请评估在途影响";
        }
        if (ManualDispatchDispatchStage.COMPLETED.equals(dispatchStage)) {
            return "上一条路线已完成，可将该司机作为空闲司机分派";
        }
        if (pendingStopCount <= 0) {
            return "当前路线暂无站，可直接插入";
        }
        return "当前路线已有 " + pendingStopCount + " 站待送，可选择插入位置";
    }

    private static boolean isCompletedRoute(NxDisDriverRouteEntity route) {
        String status = DisRouteRouteExecutionHelper.resolveRouteStatus(route);
        return DisRouteDriverRouteStatus.COMPLETED.equals(status)
                || DisRouteDriverRouteStatus.DELIVERED.equals(status)
                || DisRouteDriverRouteStatus.CLOSED.equals(status);
    }

    private static List<String> loadingRiskHints() {
        List<String> hints = new ArrayList<String>();
        hints.add("司机正在装车，追加可能影响装车顺序");
        return hints;
    }

    private static List<String> executionRiskHints() {
        List<String> hints = new ArrayList<String>();
        hints.add("司机正在配送中，可能需要司机回市场取货");
        return hints;
    }

    private static List<String> completedRiskHints() {
        List<String> hints = new ArrayList<String>();
        hints.add("司机今日路线已完成，可作为空闲司机再派");
        hints.add("司机位置需要老板自行确认");
        return hints;
    }

    private static int countSandboxSuggestedStops(SandboxComputeResult compute, Integer driverUserId) {
        return countStopsForDriver(compute != null ? compute.getSandboxSuggestedStops() : null, driverUserId);
    }

    private static int countConfirmedDispatchStops(SandboxComputeResult compute, Integer driverUserId) {
        int count = countStopsForDriver(compute != null ? compute.getConfirmedStops() : null, driverUserId);
        if (compute != null && compute.getLoadingStops() != null) {
            for (NxDisRouteStopEntity stop : compute.getLoadingStops()) {
                if (stop == null) {
                    continue;
                }
                Integer stopDriverId = resolveStopDriverUserId(stop);
                if (driverUserId.equals(stopDriverId)
                        && !SANDBOX_SUGGESTED.equals(stop.getStopSource())) {
                    count++;
                }
            }
        }
        return count;
    }

    private static List<NxDisRouteStopEntity> collectAllDriverStops(SandboxComputeResult compute,
                                                                    Integer driverUserId) {
        if (compute == null || driverUserId == null) {
            return Collections.emptyList();
        }
        List<NxDisRouteStopEntity> all = new ArrayList<NxDisRouteStopEntity>();
        appendDriverStops(all, compute.getConfirmedStops(), driverUserId);
        appendDriverStops(all, compute.getSandboxSuggestedStops(), driverUserId);
        appendDriverStops(all, compute.getLoadingStops(), driverUserId);
        appendDriverStops(all, compute.getExecutionStops(), driverUserId);
        return all;
    }

    private static int countStopsForDriver(List<NxDisRouteStopEntity> stops, Integer driverUserId) {
        if (stops == null || driverUserId == null) {
            return 0;
        }
        int count = 0;
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            if (driverUserId.equals(resolveStopDriverUserId(stop))) {
                count++;
            }
        }
        return count;
    }

    private static void appendDriverStops(List<NxDisRouteStopEntity> target,
                                        List<NxDisRouteStopEntity> source,
                                        Integer driverUserId) {
        if (target == null || source == null || driverUserId == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : source) {
            if (stop == null) {
                continue;
            }
            if (driverUserId.equals(resolveStopDriverUserId(stop))) {
                target.add(stop);
            }
        }
    }

    private static Integer resolveStopDriverUserId(NxDisRouteStopEntity stop) {
        if (stop.getSuggestedDriverUserId() != null) {
            return stop.getSuggestedDriverUserId();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task == null) {
            return null;
        }
        if (task.getNxDstAssignedDriverUserId() != null) {
            return task.getNxDstAssignedDriverUserId();
        }
        return task.getNxDstSuggestedDriverUserId();
    }

    private static boolean isDeliveredStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task == null || task.getNxDstStatus() == null) {
            return false;
        }
        String status = task.getNxDstStatus().trim().toUpperCase();
        return DELIVERED.equals(status) || CLOSED.equals(status);
    }

    public static final class ManualDispatchPanoramaCapabilities {
        private final boolean canSimulate;
        private final boolean canConfirm;
        private final String confirmMode;
        private final String blockedReason;
        private final List<String> riskHints;

        private ManualDispatchPanoramaCapabilities(boolean canSimulate,
                                                   boolean canConfirm,
                                                   String confirmMode,
                                                   String blockedReason,
                                                   List<String> riskHints) {
            this.canSimulate = canSimulate;
            this.canConfirm = canConfirm;
            this.confirmMode = confirmMode;
            this.blockedReason = blockedReason;
            this.riskHints = riskHints != null ? riskHints : Collections.<String>emptyList();
        }

        public static ManualDispatchPanoramaCapabilities direct() {
            return new ManualDispatchPanoramaCapabilities(
                    true, true, ManualDispatchConfirmMode.DIRECT, null, Collections.<String>emptyList());
        }

        public static ManualDispatchPanoramaCapabilities riskAck(List<String> riskHints) {
            return new ManualDispatchPanoramaCapabilities(
                    true, true, ManualDispatchConfirmMode.RISK_ACK, null, riskHints);
        }

        public static ManualDispatchPanoramaCapabilities forbidden(String blockedReason) {
            return new ManualDispatchPanoramaCapabilities(
                    false, false, ManualDispatchConfirmMode.FORBIDDEN, blockedReason, Collections.<String>emptyList());
        }

        public boolean isCanSimulate() {
            return canSimulate;
        }

        public boolean isCanConfirm() {
            return canConfirm;
        }

        public String getConfirmMode() {
            return confirmMode;
        }

        public String getBlockedReason() {
            return blockedReason;
        }

        public List<String> getRiskHints() {
            return riskHints;
        }
    }

    public static final class DriverStopCounts {
        private final int currentStopCount;
        private final int completedStopCount;
        private final int pendingStopCount;

        public DriverStopCounts(int currentStopCount, int completedStopCount, int pendingStopCount) {
            this.currentStopCount = currentStopCount;
            this.completedStopCount = completedStopCount;
            this.pendingStopCount = pendingStopCount;
        }

        public int getCurrentStopCount() {
            return currentStopCount;
        }

        public int getCompletedStopCount() {
            return completedStopCount;
        }

        public int getPendingStopCount() {
            return pendingStopCount;
        }
    }
}
