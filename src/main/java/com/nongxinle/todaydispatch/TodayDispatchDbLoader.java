package com.nongxinle.todaydispatch;

import com.nongxinle.dao.NxDisDriverDutyDao;
import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dao.NxDisShipmentTaskItemDao;
import com.nongxinle.dto.route.DisRouteOrderSnapshotDto;
import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.entity.NxDisDriverDutyEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.route.DisRouteSandboxStopKeyUtils;
import com.nongxinle.service.DisRouteDispatchService;
import com.nongxinle.service.impl.DisRoutePlanPresentationHelper;
import com.nongxinle.service.impl.DisRouteShipmentTaskItemOrderResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;
import static com.nongxinle.route.DisRouteDispatchBatch.MORNING;
import static com.nongxinle.route.DisRoutePlanStatus.READY;
import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.route.DisShipmentTaskStatus.DELIVERED;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;
import static com.nongxinle.route.DisShipmentTaskStatus.READY_TO_GO;
import static com.nongxinle.route.DisShipmentTaskStatus.UNASSIGNED;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

/** 今日派单 — 直读 DB，不经过 sandbox compute 引擎。 */
@Service
public class TodayDispatchDbLoader {

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDisShipmentTaskItemDao nxDisShipmentTaskItemDao;
    @Autowired
    private NxDisDriverDutyDao nxDisDriverDutyDao;
    @Autowired
    private DisRouteDispatchService disRouteDispatchService;
    @Autowired
    private DisRouteShipmentTaskItemOrderResolver disRouteShipmentTaskItemOrderResolver;
    @Autowired
    private TodayDispatchStopTimeWindowHydrator todayDispatchStopTimeWindowHydrator;

    public SandboxComputeResult loadPersisted(Integer disId, String routeDate, String batchCode) {
        return load(disId, routeDate, batchCode, false);
    }

    public SandboxComputeResult loadDispatch(Integer disId, String routeDate, String batchCode) {
        return load(disId, routeDate, batchCode, true);
    }

    private SandboxComputeResult load(Integer disId, String routeDate, String batchCode, boolean includeUnassigned) {
        String effectiveDate = normalizeRouteDate(routeDate);
        String effectiveBatch = normalizeBatch(batchCode);

        SandboxComputeResult result = new SandboxComputeResult();
        result.setRouteDate(effectiveDate);
        result.setDispatchBatch(effectiveBatch);

        NxDisRoutePlanEntity planHeader = findPlanHeader(disId, effectiveDate, effectiveBatch);
        if (planHeader == null) {
            result.setOnDutyDrivers(loadOnDutyDriverUsers(disId, effectiveDate));
            if (includeUnassigned) {
                List<NxDisRouteStopEntity> unassigned = buildUnassignedFromOrders(
                        disId, effectiveDate, Collections.<Integer>emptySet());
                todayDispatchStopTimeWindowHydrator.hydrate(
                        disId, effectiveDate, unassigned, Collections.<NxDisShipmentTaskEntity>emptyList());
                result.setUnassignedStops(unassigned);
            }
            return result;
        }

        NxDisRoutePlanEntity plan = disRouteDispatchService.getPlan(planHeader.getNxDrpId());
        if (plan == null) {
            plan = planHeader;
        }
        result.setPlanContext(planHeader);
        result.setMergedPlan(plan);
        result.setOnDutyDrivers(loadOnDutyDriverUsers(disId, effectiveDate));

        List<NxDisShipmentTaskEntity> allTasks = collectPlanTasks(plan);
        attachTaskItems(allTasks);
        result.setAllDisplayTasks(allTasks);
        hydrateRoutePartitions(plan, disId, effectiveDate, allTasks);

        List<NxDisRouteStopEntity> confirmed = new ArrayList<NxDisRouteStopEntity>();
        List<NxDisRouteStopEntity> loading = new ArrayList<NxDisRouteStopEntity>();
        List<NxDisRouteStopEntity> execution = new ArrayList<NxDisRouteStopEntity>();
        Set<Integer> assignedDepIds = new HashSet<Integer>();

        for (NxDisShipmentTaskEntity task : allTasks) {
            if (task == null || CANCELLED.equals(task.getNxDstStatus())) {
                continue;
            }
            if (task.getNxDstDepFatherId() != null) {
                assignedDepIds.add(task.getNxDstDepFatherId());
            }
            NxDisRouteStopEntity stop = taskToStop(task);
            String status = task.getNxDstStatus();
            if (IN_DELIVERY.equals(status) || DELIVERED.equals(status) || EXCEPTION.equals(status)) {
                execution.add(stop);
            } else if (isLoadingTask(task, plan)) {
                loading.add(stop);
            } else if (com.nongxinle.route.DisShipmentTaskStatus.ASSIGNED.equals(status) || READY_TO_GO.equals(status)) {
                confirmed.add(stop);
            }
        }

        result.setConfirmedStops(confirmed);
        result.setLoadingStops(loading);
        result.setExecutionStops(execution);
        result.setSandboxSuggestedStops(Collections.<NxDisRouteStopEntity>emptyList());

        if (includeUnassigned) {
            result.setUnassignedStops(buildUnassignedFromOrders(disId, effectiveDate, assignedDepIds));
        } else {
            result.setUnassignedStops(Collections.<NxDisRouteStopEntity>emptyList());
        }

        List<NxDisRouteStopEntity> allStops = new ArrayList<NxDisRouteStopEntity>();
        allStops.addAll(confirmed);
        allStops.addAll(loading);
        allStops.addAll(execution);
        allStops.addAll(result.getUnassignedStops());
        todayDispatchStopTimeWindowHydrator.hydrate(disId, effectiveDate, allStops, allTasks);
        return result;
    }

    private boolean isLoadingTask(NxDisShipmentTaskEntity task, NxDisRoutePlanEntity plan) {
        if (task == null || task.getNxDstDriverRouteId() == null || plan == null) {
            return false;
        }
        if (plan.getLoadingDriverRoutes() != null) {
            for (NxDisDriverRouteEntity route : plan.getLoadingDriverRoutes()) {
                if (route != null && task.getNxDstDriverRouteId().equals(route.getNxDdrId())) {
                    return true;
                }
            }
        }
        NxDisDriverRouteEntity route = findRouteById(plan.getDriverRoutes(), task.getNxDstDriverRouteId());
        if (route == null) {
            route = findRouteById(plan.getExecutionDriverRoutes(), task.getNxDstDriverRouteId());
        }
        return route != null && route.getNxDdrLoadingEnteredAt() != null
                && route.getNxDdrActualDepartAt() == null;
    }

    private static NxDisDriverRouteEntity findRouteById(List<NxDisDriverRouteEntity> routes, Integer routeId) {
        if (routes == null || routeId == null) {
            return null;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route != null && routeId.equals(route.getNxDdrId())) {
                return route;
            }
        }
        return null;
    }

    private void hydrateRoutePartitions(NxDisRoutePlanEntity plan,
                                        Integer disId,
                                        String routeDate,
                                        List<NxDisShipmentTaskEntity> planTasks) {
        if (plan == null || plan.getNxDrpId() == null) {
            return;
        }
        List<NxDisDriverRouteEntity> allRoutes = plan.getDriverRoutes();
        if (allRoutes == null || allRoutes.isEmpty()) {
            allRoutes = nxDisDriverRouteDao.queryByPlanId(plan.getNxDrpId());
            plan.setDriverRoutes(allRoutes);
        }
        List<NxDisDriverRouteEntity> loadingRoutes = new ArrayList<NxDisDriverRouteEntity>();
        List<NxDisDriverRouteEntity> executionRoutes = new ArrayList<NxDisDriverRouteEntity>();
        if (allRoutes != null) {
            for (NxDisDriverRouteEntity route : allRoutes) {
                if (route == null) {
                    continue;
                }
                List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDriverRouteId(route.getNxDdrId());
                List<NxDisShipmentTaskEntity> active = filterActiveTasks(tasks);
                attachTaskItems(active);
                List<NxDisRouteStopEntity> routeStops = DisRoutePlanPresentationHelper.tasksToReadModelStops(active);
                todayDispatchStopTimeWindowHydrator.hydratePlanRouteStops(
                        disId, routeDate, routeStops, planTasks);
                route.setStops(routeStops);
                if (route.getNxDdrActualDepartAt() != null) {
                    executionRoutes.add(route);
                } else if (route.getNxDdrLoadingEnteredAt() != null) {
                    loadingRoutes.add(route);
                }
            }
        }
        plan.setLoadingDriverRoutes(loadingRoutes);
        plan.setExecutionDriverRoutes(executionRoutes);
    }

    private List<NxDisShipmentTaskEntity> collectPlanTasks(NxDisRoutePlanEntity plan) {
        List<NxDisShipmentTaskEntity> tasks = new ArrayList<NxDisShipmentTaskEntity>();
        if (plan == null || plan.getNxDrpId() == null) {
            return tasks;
        }
        List<NxDisShipmentTaskEntity> planTasks = nxDisShipmentTaskDao.queryByPlanId(plan.getNxDrpId());
        if (planTasks != null) {
            tasks.addAll(planTasks);
        }
        return tasks;
    }

    private List<NxDisShipmentTaskEntity> filterActiveTasks(List<NxDisShipmentTaskEntity> tasks) {
        List<NxDisShipmentTaskEntity> active = new ArrayList<NxDisShipmentTaskEntity>();
        if (tasks == null) {
            return active;
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task != null && !CANCELLED.equals(task.getNxDstStatus())) {
                active.add(task);
            }
        }
        return active;
    }

    private List<NxDisRouteStopEntity> buildUnassignedFromOrders(Integer disId,
                                                                 String routeDate,
                                                                 Set<Integer> assignedDepIds) {
        List<DisRouteOrderSnapshotDto> orders =
                nxDisRoutePlanDao.queryEligibleLiveOrderSnapshots(disId, null, null, null);
        Map<Integer, NxDisRouteStopEntity> byDep = new LinkedHashMap<Integer, NxDisRouteStopEntity>();
        if (orders == null) {
            return new ArrayList<NxDisRouteStopEntity>();
        }
        for (DisRouteOrderSnapshotDto row : orders) {
            if (row == null || row.getDepartmentId() == null) {
                continue;
            }
            if (assignedDepIds.contains(row.getDepartmentId())) {
                continue;
            }
            if (byDep.containsKey(row.getDepartmentId())) {
                continue;
            }
            NxDisRouteStopEntity stop = orderSnapshotToStop(row);
            byDep.put(row.getDepartmentId(), stop);
        }
        return new ArrayList<NxDisRouteStopEntity>(byDep.values());
    }

    private static NxDisRouteStopEntity orderSnapshotToStop(DisRouteOrderSnapshotDto row) {
        NxDisRouteStopEntity stop = new NxDisRouteStopEntity();
        stop.setNxDrsDepartmentId(row.getDepartmentId());
        stop.setNxDrsDepartmentName(row.getDepartmentName());
        stop.setLiveDepartmentName(row.getDepartmentName());
        stop.setNxDrsLat(row.getLat());
        stop.setNxDrsLng(row.getLng());
        stop.setNxDrsAddress(row.getAddress());
        stop.setSandboxStopKey(DisRouteSandboxStopKeyUtils.build(row.getDepartmentId()));
        stop.setConfirmViaSandbox(true);

        NxDisShipmentTaskEntity task = new NxDisShipmentTaskEntity();
        task.setNxDstDepFatherId(row.getDepartmentId());
        task.setNxDstDepName(row.getDepartmentName());
        task.setLiveDepartmentName(row.getDepartmentName());
        task.setNxDstLat(row.getLat());
        task.setNxDstLng(row.getLng());
        task.setNxDstAddress(row.getAddress());
        task.setNxDstStatus(UNASSIGNED);
        task.setSandboxStopKey(stop.getSandboxStopKey());
        task.setConfirmViaSandbox(true);
        if (row.getOrderId() != null) {
            NxDisShipmentTaskItemEntity item = new NxDisShipmentTaskItemEntity();
            item.setNxDstiLiveOrderId(row.getOrderId());
            List<NxDisShipmentTaskItemEntity> items = new ArrayList<NxDisShipmentTaskItemEntity>();
            items.add(item);
            task.setItems(items);
        }
        stop.setShipmentTask(task);
        return stop;
    }

    private static NxDisRouteStopEntity taskToStop(NxDisShipmentTaskEntity task) {
        List<NxDisRouteStopEntity> stops = DisRoutePlanPresentationHelper.tasksToReadModelStops(
                Collections.singletonList(task));
        if (stops.isEmpty()) {
            NxDisRouteStopEntity stop = new NxDisRouteStopEntity();
            stop.setShipmentTask(task);
            stop.setNxDrsShipmentTaskId(task.getNxDstId());
            stop.setNxDrsDepartmentId(task.getNxDstDepFatherId());
            return stop;
        }
        return stops.get(0);
    }

    private void attachTaskItems(List<NxDisShipmentTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        List<Integer> taskIds = new ArrayList<Integer>();
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task != null && task.getNxDstId() != null) {
                taskIds.add(task.getNxDstId());
            }
        }
        if (taskIds.isEmpty()) {
            return;
        }
        List<NxDisShipmentTaskItemEntity> allItems = nxDisShipmentTaskItemDao.queryByTaskIds(taskIds);
        disRouteShipmentTaskItemOrderResolver.enrichItems(allItems);
        Map<Integer, List<NxDisShipmentTaskItemEntity>> byTask = new HashMap<Integer, List<NxDisShipmentTaskItemEntity>>();
        if (allItems != null) {
            for (NxDisShipmentTaskItemEntity item : allItems) {
                if (item == null || item.getNxDstiTaskId() == null) {
                    continue;
                }
                List<NxDisShipmentTaskItemEntity> bucket = byTask.get(item.getNxDstiTaskId());
                if (bucket == null) {
                    bucket = new ArrayList<NxDisShipmentTaskItemEntity>();
                    byTask.put(item.getNxDstiTaskId(), bucket);
                }
                bucket.add(item);
            }
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null || task.getNxDstId() == null) {
                continue;
            }
            List<NxDisShipmentTaskItemEntity> items = byTask.get(task.getNxDstId());
            task.setItems(items != null ? items : new ArrayList<NxDisShipmentTaskItemEntity>());
        }
    }

    private List<NxDistributerUserEntity> loadOnDutyDriverUsers(Integer disId, String routeDate) {
        List<NxDisDriverDutyEntity> duties = nxDisDriverDutyDao.queryByDisDate(disId, routeDate);
        if (duties == null || duties.isEmpty()) {
            return new ArrayList<NxDistributerUserEntity>();
        }
        Map<Integer, NxDistributerUserEntity> driverAccounts = indexDriverAccounts(disId);
        List<NxDistributerUserEntity> users = new ArrayList<NxDistributerUserEntity>();
        for (NxDisDriverDutyEntity duty : duties) {
            if (duty == null || !ON_DUTY.equals(duty.getNxDddDutyStatus())
                    || duty.getNxDddDriverUserId() == null) {
                continue;
            }
            Integer driverUserId = duty.getNxDddDriverUserId();
            NxDistributerUserEntity account = driverAccounts.get(driverUserId);
            if (account != null) {
                users.add(account);
            } else {
                NxDistributerUserEntity stub = new NxDistributerUserEntity();
                stub.setNxDistributerUserId(driverUserId);
                users.add(stub);
            }
        }
        return users;
    }

    private Map<Integer, NxDistributerUserEntity> indexDriverAccounts(Integer disId) {
        Map<Integer, NxDistributerUserEntity> index = new HashMap<Integer, NxDistributerUserEntity>();
        List<NxDistributerUserEntity> drivers = disRouteDispatchService.listDrivers(disId);
        if (drivers == null) {
            return index;
        }
        for (NxDistributerUserEntity driver : drivers) {
            if (driver != null && driver.getNxDistributerUserId() != null) {
                index.put(driver.getNxDistributerUserId(), driver);
            }
        }
        return index;
    }

    private NxDisRoutePlanEntity findPlanHeader(Integer disId, String routeDate, String batchCode) {
        for (String status : new String[]{
                com.nongxinle.route.DisRoutePlanStatus.ASSIGNED, READY}) {
            NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                    disId, routeDate, batchCode, status);
            if (plan != null) {
                return plan;
            }
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("disId", disId);
        params.put("routeDate", routeDate);
        List<NxDisRoutePlanEntity> plans = nxDisRoutePlanDao.queryList(params);
        if (plans == null) {
            return null;
        }
        for (NxDisRoutePlanEntity plan : plans) {
            if (plan != null && !com.nongxinle.route.DisRoutePlanStatus.CANCELLED.equals(plan.getNxDrpStatus())
                    && batchCode.equals(normalizeBatch(plan.getNxDrpDispatchBatch()))) {
                return plan;
            }
        }
        return null;
    }

    private static String normalizeRouteDate(String routeDate) {
        if (routeDate != null && !routeDate.trim().isEmpty()) {
            return routeDate.trim();
        }
        return formatWhatDay(0);
    }

    private static String normalizeBatch(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return MORNING;
        }
        return batchCode.trim().toUpperCase();
    }
}
