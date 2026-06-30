package com.nongxinle.todaydispatch;

import com.nongxinle.dao.NxDepartmentDao;
import com.nongxinle.dao.NxDisSandboxDayTimeWindowDao;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisSandboxDayTimeWindowEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteSandboxStopTimeWindowResolver;
import com.nongxinle.route.SandboxStopResolvedTimeWindow;
import com.nongxinle.service.impl.DisRouteDispatchSnapshotHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisShipmentTaskStatus.ASSIGNED;
import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;
import static com.nongxinle.route.DisShipmentTaskStatus.READY_TO_GO;

/** 直读 DB 后补全 stop 送达窗口：今日 override → task/stop → 客户常规（department）。 */
@Component
public class TodayDispatchStopTimeWindowHydrator {

    @Autowired
    private NxDisSandboxDayTimeWindowDao nxDisSandboxDayTimeWindowDao;
    @Autowired
    private NxDepartmentDao nxDepartmentDao;
    @Autowired
    private DisRouteDispatchSnapshotHelper disRouteDispatchSnapshotHelper;

    public void hydrate(Integer disId,
                        String routeDate,
                        List<NxDisRouteStopEntity> stops,
                        List<NxDisShipmentTaskEntity> planTasks) {
        if (stops == null || stops.isEmpty()) {
            return;
        }
        Map<Integer, NxDisSandboxDayTimeWindowEntity> dayOverrides = loadDayOverrides(disId, routeDate);
        Map<Integer, NxDisShipmentTaskEntity> protectedOverrides = loadProtectedTaskOverrides(planTasks);
        Set<Integer> depIds = collectDepIds(stops);
        Map<Integer, NxDepartmentEntity> departments = loadDepartments(depIds);

        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            hydrateStop(stop, routeDate, dayOverrides, protectedOverrides, departments);
        }
    }

    private void hydrateStop(NxDisRouteStopEntity stop,
                           String routeDate,
                           Map<Integer, NxDisSandboxDayTimeWindowEntity> dayOverrides,
                           Map<Integer, NxDisShipmentTaskEntity> protectedOverrides,
                           Map<Integer, NxDepartmentEntity> departments) {
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        Integer depId = stop.getNxDrsDepartmentId();
        if (depId == null && task != null) {
            depId = task.getNxDstDepFatherId();
        }
        NxDepartmentEntity department = depId != null ? departments.get(depId) : null;
        NxDisSandboxDayTimeWindowEntity dayOverride = depId != null ? dayOverrides.get(depId) : null;

        if (task != null) {
            NxDisShipmentTaskEntity taskOverride = depId != null ? protectedOverrides.get(depId) : null;
            applyVirtualTimeWindowOverride(task, taskOverride, dayOverride);
            disRouteDispatchSnapshotHelper.refreshTaskSnapshot(task, true);
            if (task.getNxDstRouteDate() == null || task.getNxDstRouteDate().trim().isEmpty()) {
                task.setNxDstRouteDate(routeDate);
            }
        }

        SandboxStopResolvedTimeWindow resolved = DisRouteSandboxStopTimeWindowResolver.resolve(
                stop, task, department, dayOverride);
        DisRouteSandboxStopTimeWindowResolver.applyToStop(stop, resolved);
    }

    private static void applyVirtualTimeWindowOverride(NxDisShipmentTaskEntity task,
                                                       NxDisShipmentTaskEntity taskOverride,
                                                       NxDisSandboxDayTimeWindowEntity dayOverride) {
        if (taskOverride != null) {
            if (taskOverride != task) {
                task.setNxDstEarliestDeliveryTimeS(taskOverride.getNxDstEarliestDeliveryTimeS());
                task.setNxDstLatestDeliveryTimeS(taskOverride.getNxDstLatestDeliveryTimeS());
                task.setNxDstServiceMinutes(taskOverride.getNxDstServiceMinutes());
                task.setNxDstTimeWindowOverrideFlag(taskOverride.getNxDstTimeWindowOverrideFlag());
                task.setNxDstTimeWindowAdjustReason(taskOverride.getNxDstTimeWindowAdjustReason());
            }
            return;
        }
        if (dayOverride == null) {
            return;
        }
        task.setNxDstEarliestDeliveryTimeS(dayOverride.getNxDsdtwEarliestDeliveryTimeS());
        task.setNxDstLatestDeliveryTimeS(dayOverride.getNxDsdtwLatestDeliveryTimeS());
        task.setNxDstServiceMinutes(dayOverride.getNxDsdtwServiceMinutes());
        task.setNxDstTimeWindowOverrideFlag(1);
        task.setNxDstTimeWindowAdjustReason(dayOverride.getNxDsdtwAdjustReason());
    }

    private Map<Integer, NxDisSandboxDayTimeWindowEntity> loadDayOverrides(Integer disId, String routeDate) {
        Map<Integer, NxDisSandboxDayTimeWindowEntity> map = new HashMap<Integer, NxDisSandboxDayTimeWindowEntity>();
        if (disId == null || routeDate == null || routeDate.trim().isEmpty()) {
            return map;
        }
        List<NxDisSandboxDayTimeWindowEntity> rows = nxDisSandboxDayTimeWindowDao.queryByDisRouteDate(
                disId, routeDate.trim());
        if (rows == null) {
            return map;
        }
        for (NxDisSandboxDayTimeWindowEntity row : rows) {
            if (row != null && row.getNxDsdtwDepFatherId() != null) {
                map.put(row.getNxDsdtwDepFatherId(), row);
            }
        }
        return map;
    }

    private static Map<Integer, NxDisShipmentTaskEntity> loadProtectedTaskOverrides(
            List<NxDisShipmentTaskEntity> planTasks) {
        Map<Integer, NxDisShipmentTaskEntity> map = new HashMap<Integer, NxDisShipmentTaskEntity>();
        if (planTasks == null) {
            return map;
        }
        for (NxDisShipmentTaskEntity task : planTasks) {
            if (task == null || !DisRouteSandboxStopTimeWindowResolver.isActiveOverrideSourceTask(task)) {
                continue;
            }
            if (task.getNxDstDepFatherId() != null) {
                map.put(task.getNxDstDepFatherId(), task);
            }
        }
        return map;
    }

    private static boolean isTaskProtected(NxDisShipmentTaskEntity task) {
        if (task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1) {
            return true;
        }
        String status = task.getNxDstStatus();
        return ASSIGNED.equals(status)
                || READY_TO_GO.equals(status)
                || IN_DELIVERY.equals(status)
                || DELIVERED.equals(status)
                || EXCEPTION.equals(status);
    }

    private static Set<Integer> collectDepIds(List<NxDisRouteStopEntity> stops) {
        Set<Integer> depIds = new HashSet<Integer>();
        for (NxDisRouteStopEntity stop : stops) {
            if (stop == null) {
                continue;
            }
            if (stop.getNxDrsDepartmentId() != null) {
                depIds.add(stop.getNxDrsDepartmentId());
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task != null && task.getNxDstDepFatherId() != null) {
                depIds.add(task.getNxDstDepFatherId());
            }
        }
        return depIds;
    }

    private Map<Integer, NxDepartmentEntity> loadDepartments(Set<Integer> depIds) {
        Map<Integer, NxDepartmentEntity> map = new HashMap<Integer, NxDepartmentEntity>();
        if (depIds == null || depIds.isEmpty()) {
            return map;
        }
        for (Integer depId : depIds) {
            if (depId == null || map.containsKey(depId)) {
                continue;
            }
            NxDepartmentEntity department = nxDepartmentDao.queryObject(depId);
            if (department != null) {
                map.put(depId, department);
            }
        }
        return map;
    }

    public void hydratePlanRouteStops(Integer disId,
                                      String routeDate,
                                      List<NxDisRouteStopEntity> routeStops,
                                      List<NxDisShipmentTaskEntity> planTasks) {
        if (routeStops == null || routeStops.isEmpty()) {
            return;
        }
        hydrate(disId, routeDate, new ArrayList<NxDisRouteStopEntity>(routeStops), planTasks);
    }
}
