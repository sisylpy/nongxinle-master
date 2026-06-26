package com.nongxinle.route;

import com.nongxinle.dao.NxDepartmentDao;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisSandboxDayTimeWindowEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDepartmentEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 批量解析并写入 stop / task 的统一时间窗。 */
public final class DisRouteSandboxStopTimeWindowResolutionSupport {

    private DisRouteSandboxStopTimeWindowResolutionSupport() {
    }

    public static Set<Integer> collectDepIdsFromStops(Collection<NxDisRouteStopEntity> stops) {
        Set<Integer> depIds = new HashSet<Integer>();
        if (stops == null) {
            return depIds;
        }
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

    public static void ensureDepartmentsLoaded(NxDepartmentDao departmentDao,
                                               Collection<Integer> depIds,
                                               Map<Integer, NxDepartmentEntity> cache) {
        if (departmentDao == null || depIds == null || cache == null) {
            return;
        }
        for (Integer depId : new HashSet<Integer>(depIds)) {
            if (depId == null || cache.containsKey(depId)) {
                continue;
            }
            NxDepartmentEntity department = departmentDao.queryObject(depId);
            if (department != null) {
                cache.put(depId, department);
            }
        }
    }

    public static Map<Integer, NxDepartmentEntity> loadDepartmentsByDepId(NxDepartmentDao departmentDao,
                                                                          Collection<Integer> depIds) {
        Map<Integer, NxDepartmentEntity> map = new HashMap<Integer, NxDepartmentEntity>();
        if (departmentDao == null || depIds == null) {
            return map;
        }
        for (Integer depId : new HashSet<Integer>(depIds)) {
            if (depId == null || map.containsKey(depId)) {
                continue;
            }
            NxDepartmentEntity department = departmentDao.queryObject(depId);
            if (department != null) {
                map.put(depId, department);
            }
        }
        return map;
    }

    public static Set<Integer> collectDepIdsFromTasks(Collection<NxDisShipmentTaskEntity> tasks) {
        Set<Integer> depIds = new HashSet<Integer>();
        if (tasks == null) {
            return depIds;
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task != null && task.getNxDstDepFatherId() != null) {
                depIds.add(task.getNxDstDepFatherId());
            }
        }
        return depIds;
    }

    public static void applyToStops(List<NxDisRouteStopEntity> stops,
                                    Map<Integer, NxDepartmentEntity> departmentByDepId,
                                    Map<Integer, NxDisSandboxDayTimeWindowEntity> dayOverrideByDepId) {
        if (stops == null) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            applyToStop(stop, departmentByDepId, dayOverrideByDepId);
        }
    }

    public static void applyToPlan(NxDisRoutePlanEntity plan,
                                   Map<Integer, NxDepartmentEntity> departmentByDepId,
                                   Map<Integer, NxDisSandboxDayTimeWindowEntity> dayOverrideByDepId) {
        if (plan == null) {
            return;
        }
        applyToRoutes(plan.getDriverRoutes(), departmentByDepId, dayOverrideByDepId);
        applyToRoutes(plan.getLoadingDriverRoutes(), departmentByDepId, dayOverrideByDepId);
        applyToRoutes(plan.getExecutionDriverRoutes(), departmentByDepId, dayOverrideByDepId);
    }

    public static void applyToStop(NxDisRouteStopEntity stop,
                                   Map<Integer, NxDepartmentEntity> departmentByDepId,
                                   Map<Integer, NxDisSandboxDayTimeWindowEntity> dayOverrideByDepId) {
        if (stop == null) {
            return;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        Integer depId = stop.getNxDrsDepartmentId();
        if (depId == null && task != null) {
            depId = task.getNxDstDepFatherId();
        }
        NxDepartmentEntity department = depId != null && departmentByDepId != null
                ? departmentByDepId.get(depId) : null;
        NxDisSandboxDayTimeWindowEntity dayOverride = depId != null && dayOverrideByDepId != null
                ? dayOverrideByDepId.get(depId) : null;
        SandboxStopResolvedTimeWindow resolved = DisRouteSandboxStopTimeWindowResolver.resolve(
                stop, task, department, dayOverride);
        DisRouteSandboxStopTimeWindowResolver.applyToStop(stop, resolved);
    }

    private static void applyToRoutes(List<NxDisDriverRouteEntity> routes,
                                      Map<Integer, NxDepartmentEntity> departmentByDepId,
                                      Map<Integer, NxDisSandboxDayTimeWindowEntity> dayOverrideByDepId) {
        if (routes == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null || route.getStops() == null) {
                continue;
            }
            applyToStops(route.getStops(), departmentByDepId, dayOverrideByDepId);
        }
    }
}
