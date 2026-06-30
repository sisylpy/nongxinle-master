package com.nongxinle.route;

import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisSandboxDayTimeWindowEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.route.dispatch.strategy.StopAssignment;

import static com.nongxinle.route.DisShipmentTaskStatus.ASSIGNED;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;
import static com.nongxinle.route.DisShipmentTaskStatus.READY_TO_GO;

/**
 * 沙盘 stop 时间窗唯一解析口径：今日 override → 客户常规 → 无窗。
 * sequencer / schedule preview / sections / primaryAction / debug 均消费本解析结果。
 */
public final class DisRouteSandboxStopTimeWindowResolver {

    private static final int DEFAULT_SERVICE_MINUTES = 30;

    private DisRouteSandboxStopTimeWindowResolver() {
    }

    public static SandboxStopResolvedTimeWindow resolve(NxDisRouteStopEntity stop,
                                                        NxDisShipmentTaskEntity task,
                                                        NxDepartmentEntity department,
                                                        NxDisSandboxDayTimeWindowEntity dayOverride) {
        if (task == null && stop != null) {
            task = stop.getShipmentTask();
        }

        // 已派 task 上的今日调整优先于 day 表（避免 task 已更新但 day 表/快照仍留旧值）
        if (task != null && isOverrideFlag(task.getNxDstTimeWindowOverrideFlag())) {
            SandboxStopResolvedTimeWindow fromTask = buildFromTask(task, true);
            if (fromTask.hasWindow()) {
                return fromTask;
            }
        }

        if (dayOverride != null && dayOverride.getNxDsdtwLatestDeliveryTimeS() != null) {
            return buildWindow(
                    dayOverride.getNxDsdtwEarliestDeliveryTimeS(),
                    dayOverride.getNxDsdtwLatestDeliveryTimeS(),
                    firstPositive(dayOverride.getNxDsdtwServiceMinutes(), DEFAULT_SERVICE_MINUTES),
                    SandboxStopTimeWindowSource.TODAY_OVERRIDE);
        }

        if (stop != null && isOverrideFlag(stop.getNxDrsTimeWindowOverrideFlag())) {
            SandboxStopResolvedTimeWindow fromStop = buildFromStopOrTask(stop, task, true);
            if (fromStop.hasWindow()) {
                return fromStop;
            }
        }

        if (stop != null) {
            Integer earliest = stop.getNxDrsEarliestDeliveryTimeS();
            Integer latest = stop.getNxDrsLatestDeliveryTimeS();
            if (earliest != null || latest != null) {
                return buildWindow(
                        earliest,
                        latest,
                        resolveServiceMinutes(stop, task, department),
                        SandboxStopTimeWindowSource.CUSTOMER_REGULAR);
            }
        }

        if (task != null) {
            Integer earliest = task.getNxDstEarliestDeliveryTimeS();
            Integer latest = task.getNxDstLatestDeliveryTimeS();
            if (earliest != null || latest != null) {
                return buildWindow(
                        earliest,
                        latest,
                        resolveServiceMinutes(stop, task, department),
                        SandboxStopTimeWindowSource.CUSTOMER_REGULAR);
            }
        }

        if (department != null) {
            Integer earliest = department.getNxDepartmentEarliestDeliveryTime();
            Integer latest = department.getNxDepartmentLatestDeliveryTime();
            if (earliest != null || latest != null) {
                return buildWindow(
                        earliest,
                        latest,
                        resolveServiceMinutes(stop, task, department),
                        SandboxStopTimeWindowSource.CUSTOMER_REGULAR);
            }
        }

        return SandboxStopResolvedTimeWindow.none();
    }

    public static void applyToStop(NxDisRouteStopEntity stop, SandboxStopResolvedTimeWindow resolved) {
        if (stop == null || resolved == null) {
            return;
        }
        stop.setResolvedEarliestDeliveryTimeS(resolved.getResolvedEarliestDeliveryTimeS());
        stop.setResolvedLatestDeliveryTimeS(resolved.getResolvedLatestDeliveryTimeS());
        stop.setResolvedWindowSource(resolved.getWindowSource() != null
                ? resolved.getWindowSource().name() : SandboxStopTimeWindowSource.NONE.name());
        stop.setNxDrsEarliestDeliveryTimeS(resolved.getResolvedEarliestDeliveryTimeS());
        stop.setNxDrsLatestDeliveryTimeS(resolved.getResolvedLatestDeliveryTimeS());
        if (resolved.getResolvedServiceMinutes() != null) {
            stop.setNxDrsServiceMinutes(resolved.getResolvedServiceMinutes());
        }
        if (resolved.getWindowSource() == SandboxStopTimeWindowSource.TODAY_OVERRIDE) {
            stop.setNxDrsTimeWindowOverrideFlag(1);
        }
    }

    public static void applyToStopAssignment(StopAssignment assignment,
                                             SandboxStopResolvedTimeWindow resolved) {
        if (assignment == null || resolved == null) {
            return;
        }
        assignment.setEarliestDeliveryTimeS(resolved.getResolvedEarliestDeliveryTimeS());
        assignment.setLatestDeliveryTimeS(resolved.getResolvedLatestDeliveryTimeS());
        if (resolved.getResolvedServiceMinutes() != null) {
            assignment.setServiceMinutes(resolved.getResolvedServiceMinutes());
        }
        assignment.setTimeWindowOverrideFlag(resolved.getWindowSource()
                == SandboxStopTimeWindowSource.TODAY_OVERRIDE);
        assignment.setResolvedWindowSource(resolved.getWindowSource() != null
                ? resolved.getWindowSource().name() : SandboxStopTimeWindowSource.NONE.name());
    }

    public static Integer readResolvedEarliest(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && isOverrideFlag(task.getNxDstTimeWindowOverrideFlag())
                && task.getNxDstEarliestDeliveryTimeS() != null) {
            return task.getNxDstEarliestDeliveryTimeS();
        }
        if (stop.getResolvedEarliestDeliveryTimeS() != null) {
            return stop.getResolvedEarliestDeliveryTimeS();
        }
        if (stop.getNxDrsEarliestDeliveryTimeS() != null) {
            return stop.getNxDrsEarliestDeliveryTimeS();
        }
        return task != null ? task.getNxDstEarliestDeliveryTimeS() : null;
    }

    public static Integer readResolvedLatest(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && isOverrideFlag(task.getNxDstTimeWindowOverrideFlag())
                && task.getNxDstLatestDeliveryTimeS() != null) {
            return task.getNxDstLatestDeliveryTimeS();
        }
        if (stop.getResolvedLatestDeliveryTimeS() != null) {
            return stop.getResolvedLatestDeliveryTimeS();
        }
        if (stop.getNxDrsLatestDeliveryTimeS() != null) {
            return stop.getNxDrsLatestDeliveryTimeS();
        }
        return task != null ? task.getNxDstLatestDeliveryTimeS() : null;
    }

    public static boolean isTodayOverride(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        if (SandboxStopTimeWindowSource.TODAY_OVERRIDE.name().equals(stop.getResolvedWindowSource())) {
            return true;
        }
        return isOverrideFlag(stop.getNxDrsTimeWindowOverrideFlag())
                || (stop.getShipmentTask() != null
                && isActiveOverrideSourceTask(stop.getShipmentTask()));
    }

    /** 仅未送达的在途 task 可作为同店新单的 override 来源。 */
    public static boolean isActiveOverrideSourceTask(NxDisShipmentTaskEntity task) {
        if (task == null || !isOverrideFlag(task.getNxDstTimeWindowOverrideFlag())) {
            return false;
        }
        String status = task.getNxDstStatus();
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return ASSIGNED.equals(normalized)
                || READY_TO_GO.equals(normalized)
                || IN_DELIVERY.equals(normalized)
                || EXCEPTION.equals(normalized);
    }

    private static SandboxStopResolvedTimeWindow buildFromStopOrTask(NxDisRouteStopEntity stop,
                                                                     NxDisShipmentTaskEntity task,
                                                                     boolean todayOverride) {
        Integer earliest = stop.getNxDrsEarliestDeliveryTimeS();
        Integer latest = stop.getNxDrsLatestDeliveryTimeS();
        if (earliest == null && task != null) {
            earliest = task.getNxDstEarliestDeliveryTimeS();
        }
        if (latest == null && task != null) {
            latest = task.getNxDstLatestDeliveryTimeS();
        }
        return buildWindow(
                earliest,
                latest,
                resolveServiceMinutes(stop, task, null),
                todayOverride ? SandboxStopTimeWindowSource.TODAY_OVERRIDE
                        : SandboxStopTimeWindowSource.CUSTOMER_REGULAR);
    }

    private static SandboxStopResolvedTimeWindow buildFromTask(NxDisShipmentTaskEntity task,
                                                               boolean todayOverride) {
        return buildWindow(
                task.getNxDstEarliestDeliveryTimeS(),
                task.getNxDstLatestDeliveryTimeS(),
                firstPositive(task.getNxDstServiceMinutes(), DEFAULT_SERVICE_MINUTES),
                todayOverride ? SandboxStopTimeWindowSource.TODAY_OVERRIDE
                        : SandboxStopTimeWindowSource.CUSTOMER_REGULAR);
    }

    private static SandboxStopResolvedTimeWindow buildWindow(Integer earliest,
                                                             Integer latest,
                                                             int serviceMinutes,
                                                             SandboxStopTimeWindowSource source) {
        SandboxStopResolvedTimeWindow window = new SandboxStopResolvedTimeWindow();
        window.setResolvedEarliestDeliveryTimeS(earliest);
        window.setResolvedLatestDeliveryTimeS(latest);
        window.setResolvedServiceMinutes(serviceMinutes);
        window.setWindowSource(source != null ? source : SandboxStopTimeWindowSource.NONE);
        return window;
    }

    private static int resolveServiceMinutes(NxDisRouteStopEntity stop,
                                             NxDisShipmentTaskEntity task,
                                             NxDepartmentEntity department) {
        if (stop != null && stop.getNxDrsServiceMinutes() != null && stop.getNxDrsServiceMinutes() > 0) {
            return stop.getNxDrsServiceMinutes();
        }
        if (task != null && task.getNxDstServiceMinutes() != null && task.getNxDstServiceMinutes() > 0) {
            return task.getNxDstServiceMinutes();
        }
        if (department != null && department.getNxDepartmentUnloadDuration() != null
                && department.getNxDepartmentUnloadDuration() > 0) {
            return department.getNxDepartmentUnloadDuration();
        }
        return DEFAULT_SERVICE_MINUTES;
    }

    private static int firstPositive(Integer value, int fallback) {
        return value != null && value > 0 ? value : fallback;
    }

    private static boolean isOverrideFlag(Integer flag) {
        return flag != null && flag == 1;
    }
}
