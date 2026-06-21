package com.nongxinle.service.impl;

import com.nongxinle.dao.*;
import com.nongxinle.dto.route.*;
import com.nongxinle.entity.*;
import com.nongxinle.route.*;
import com.nongxinle.route.model.*;
import com.nongxinle.service.DisDriverDutyService;
import com.nongxinle.service.DisRouteDispatchService;
import com.nongxinle.service.DisRouteFeasibilityService;
import com.nongxinle.service.DisRouteScheduleService;
import com.nongxinle.service.DisShipmentTaskService;
import com.nongxinle.service.NxDistributerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;
import static com.nongxinle.route.DisRoutePlanStatus.*;
import static com.nongxinle.route.DisShipmentTaskItemStatus.ACTIVE;
import static com.nongxinle.route.RouteCoordinateUtils.toPoint;
import static com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisUserDriver;

@Service("disRouteDispatchService")
public class DisRouteDispatchServiceImpl implements DisRouteDispatchService {

    private static final String NO_ON_DUTY_DRIVERS_MSG = "当前没有已上岗司机，请先让司机上岗后再生成派车方案。";
    private static final String NO_BATCH_ELIGIBLE_DRIVERS_MSG =
            "当前批次无可派司机（上岗时间晚于批次允许出发时间），请安排司机提前上岗或切换配送批次。";

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisRouteStopDao nxDisRouteStopDao;
    @Autowired
    private NxDistributerUserDao nxDistributerUserDao;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private RouteEngineRegistry routeEngineRegistry;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDisShipmentTaskItemDao nxDisShipmentTaskItemDao;
    @Autowired
    private DisShipmentTaskService disShipmentTaskService;
    @Autowired
    private DisRoutePlanPresentationHelper disRoutePlanPresentationHelper;
    @Autowired
    private DisDriverDutyService disDriverDutyService;
    @Autowired
    private DisRouteScheduleService disRouteScheduleService;
    @Autowired
    private DisRouteFeasibilityService disRouteFeasibilityService;
    @Autowired
    private NxDisDriverDutyDao nxDisDriverDutyDao;
    @Autowired
    private DisRouteDispatchSnapshotHelper disRouteDispatchSnapshotHelper;

    @Override
    @Transactional
    public DisRouteDispatchResult simulate(DisRoutePreviewRequest request) throws Exception {
        validatePreviewRequest(request);

        String routeDate = resolveRouteDate(request);
        DisRouteBatchContext batch = DisRouteBatchDefaults.resolve(request, routeDate);
        GeoPoint depot = resolveDepot(request);
        RouteCostProviderType costType = parseCostProviderType(request.getCostProviderType());
        RouteOptimizerType optimizerType = parseOptimizerType(request.getOptimizerType());

        List<NxDistributerUserEntity> drivers = resolveDrivers(request, routeDate, batch);
        if (drivers.isEmpty()) {
            throw new IllegalArgumentException(NO_BATCH_ELIGIBLE_DRIVERS_MSG);
        }

        List<DisRouteOrderSnapshotDto> orderRows = nxDisRoutePlanDao.queryEligibleLiveOrderSnapshots(
                request.getDisId(), routeDate);
        if (orderRows.isEmpty()) {
            throw new IllegalArgumentException("路线日 " + routeDate + " 没有可进入沙盘的 live 订单");
        }

        Map<Integer, List<DisRouteOrderSnapshotDto>> ordersByDep = groupOrdersByDepartment(orderRows);
        NxDisRoutePlanEntity plan = resolveOrCreateActivePlan(
                request, routeDate, depot, costType, optimizerType, drivers, batch);

        for (List<DisRouteOrderSnapshotDto> depOrders : ordersByDep.values()) {
            upsertOpenTaskFromOrders(request.getDisId(), routeDate, plan.getNxDrpId(), depOrders);
        }

        ensureDriverRoutes(plan, drivers);

        List<NxDisShipmentTaskEntity> optimizableTasks = collectOptimizablePlanTasks(plan.getNxDrpId());
        RouteOptimizeResult optimizeResult = runOptimizationForTasks(
                depot, costType, optimizerType, drivers, optimizableTasks);

        Map<Integer, Set<Integer>> occupiedStopSeqByDriverRoute =
                buildAndValidateLockedOccupiedSeq(plan.getNxDrpId());
        applyOptimizationToUnlockedTasks(plan, optimizeResult, optimizableTasks, occupiedStopSeqByDriverRoute);
        refreshPlanTotalsFromOptimizer(plan.getNxDrpId(), optimizeResult);
        disRoutePlanPresentationHelper.refreshPlanPresentation(plan.getNxDrpId());

        disRouteScheduleService.computeSchedule(plan.getNxDrpId());
        RouteFeasibilityResult feasibility = disRouteFeasibilityService.assess(plan.getNxDrpId());
        disShipmentTaskService.reconcilePlanStatus(plan.getNxDrpId());
        return buildDispatchResult(plan.getNxDrpId(), feasibility);
    }

    @Override
    @Transactional
    public NxDisRoutePlanEntity reoptimize(DisRouteReoptimizeRequest request) throws Exception {
        throw new UnsupportedOperationException("Phase 1.5b: reoptimize 尚未实现（须 respect manualLocked）");
    }

    @Override
    public NxDisRoutePlanEntity getPlan(Integer planId) {
        return loadPlanDetailWithTasks(planId);
    }

    @Override
    public NxDisRoutePlanEntity getPlanByRouteDate(Integer disId, String routeDate, String status, String batchCode) {
        String queryRouteDate = routeDate != null ? routeDate : formatWhatDay(0);
        String dispatchBatch = normalizeDispatchBatch(batchCode);
        NxDisRoutePlanEntity plan = findPlanByDisRouteDateAndBatch(
                disId, queryRouteDate, dispatchBatch, status);
        if (plan == null) {
            return null;
        }
        return loadPlanDetailWithTasks(plan.getNxDrpId());
    }

    /**
     * 先按请求 status 精确查；未命中则同日、同批次级联 ASSIGNED / SIMULATED / READY。
     * 仅匹配 nx_drp_dispatch_batch = dispatchBatch；null/空 batch 的老 plan 不会命中 MORNING。
     */
    private NxDisRoutePlanEntity findPlanByDisRouteDateAndBatch(Integer disId,
                                                                String routeDate,
                                                                String dispatchBatch,
                                                                String requestedStatus) {
        if (requestedStatus != null && !requestedStatus.trim().isEmpty()) {
            NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                    disId, routeDate, dispatchBatch, requestedStatus.trim());
            if (plan != null) {
                return plan;
            }
        }
        for (String fallbackStatus : new String[]{ASSIGNED, SIMULATED, READY}) {
            if (requestedStatus != null && fallbackStatus.equalsIgnoreCase(requestedStatus.trim())) {
                continue;
            }
            NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                    disId, routeDate, dispatchBatch, fallbackStatus);
            if (plan != null) {
                return plan;
            }
        }
        return null;
    }

    private String normalizeDispatchBatch(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return DisRouteDispatchBatch.MORNING;
        }
        return batchCode.trim().toUpperCase();
    }

    @Override
    public NxDisRoutePlanEntity getTodayPlan(Integer disId, String status, String batchCode) {
        return getPlanByRouteDate(disId, formatWhatDay(0), status, batchCode);
    }

    @Override
    public DriverRouteTasksResponse getDriverLoadingToday(Integer driverUserId) {
        return buildDriverTasksResponse(driverUserId, DisShipmentTaskStatus.ASSIGNED);
    }

    @Override
    public DriverRouteTasksResponse getDriverDeliveryToday(Integer driverUserId) {
        return buildDriverTasksResponse(driverUserId, DisShipmentTaskStatus.READY_TO_GO);
    }

    private DriverRouteTasksResponse buildDriverTasksResponse(Integer driverUserId, String taskStatus) {
        DriverRouteTasksResponse response = new DriverRouteTasksResponse();
        NxDistributerUserEntity driver = nxDistributerUserDao.queryObject(driverUserId);
        if (driver == null) {
            response.setTasks(new ArrayList<NxDisShipmentTaskEntity>());
            return response;
        }
        String routeDate = formatWhatDay(0);
        response.setRouteDate(routeDate);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("disId", driver.getNxDiuDistributerId());
        map.put("routeDate", routeDate);
        map.put("status", taskStatus);
        map.put("assignedDriverUserId", driverUserId);
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDisRouteDateStatus(map);
        if (tasks == null) {
            tasks = new ArrayList<NxDisShipmentTaskEntity>();
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            NxDisShipmentTaskEntity detail = disShipmentTaskService.queryTaskDetail(task.getNxDstId());
            if (detail != null) {
                task.setItems(detail.getItems());
            }
        }
        response.setTasks(tasks);

        Integer planId = resolvePlanIdForDriver(driver, routeDate, taskStatus, tasks);
        if (planId != null) {
            NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryObject(planId);
            if (plan != null) {
                response.setPlanId(plan.getNxDrpId());
                response.setPlanStatus(plan.getNxDrpStatus());
            }
            NxDisDriverRouteEntity driverRoute = loadDriverRouteWithStops(planId, driverUserId, tasks);
            response.setDriverRoute(driverRoute);
        }
        return response;
    }

    /** 优先从 task.planId 解析；否则按 routeDate + 状态级联查 plan */
    private Integer resolvePlanIdForDriver(NxDistributerUserEntity driver,
                                           String routeDate,
                                           String taskStatus,
                                           List<NxDisShipmentTaskEntity> tasks) {
        if (tasks != null) {
            for (NxDisShipmentTaskEntity task : tasks) {
                if (task.getNxDstPlanId() != null) {
                    return task.getNxDstPlanId();
                }
            }
        }
        Integer disId = driver.getNxDiuDistributerId();
        if (DisShipmentTaskStatus.READY_TO_GO.equals(taskStatus)) {
            NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateStatus(disId, routeDate, READY);
            if (plan == null) {
                plan = nxDisRoutePlanDao.queryByDisRouteDateStatus(disId, routeDate, ASSIGNED);
            }
            if (plan == null) {
                plan = nxDisRoutePlanDao.queryByDisRouteDateStatus(disId, routeDate, SIMULATED);
            }
            return plan != null ? plan.getNxDrpId() : null;
        }
        NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateStatus(disId, routeDate, ASSIGNED);
        if (plan == null) {
            plan = nxDisRoutePlanDao.queryByDisRouteDateStatus(disId, routeDate, SIMULATED);
        }
        return plan != null ? plan.getNxDrpId() : null;
    }

    /** 单司机读模型：driver_route + stops（stopSeq 升序）+ stop.shipmentTask.items */
    private NxDisDriverRouteEntity loadDriverRouteWithStops(Integer planId,
                                                            Integer driverUserId,
                                                            List<NxDisShipmentTaskEntity> tasks) {
        NxDisDriverRouteEntity driverRoute = findDriverRouteInPlan(planId, driverUserId);
        if (driverRoute == null) {
            driverRoute = findDriverRouteFromTaskStops(tasks);
        }
        if (driverRoute == null) {
            throw new IllegalStateException(buildMissingDriverRouteMessage(planId, driverUserId));
        }
        return driverRoute;
    }

    private NxDisDriverRouteEntity findDriverRouteInPlan(Integer planId, Integer driverUserId) {
        NxDisRoutePlanEntity planDetail = loadPlanDetailWithTasks(planId);
        if (planDetail == null || planDetail.getDriverRoutes() == null) {
            return null;
        }
        for (NxDisDriverRouteEntity route : planDetail.getDriverRoutes()) {
            if (Objects.equals(driverUserId, route.getNxDdrDriverUserId())) {
                return route;
            }
        }
        return null;
    }

    private NxDisDriverRouteEntity findDriverRouteFromTaskStops(List<NxDisShipmentTaskEntity> tasks) {
        if (tasks == null) {
            return null;
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null || task.getNxDstId() == null) {
                continue;
            }
            NxDisRouteStopEntity stop = nxDisRouteStopDao.queryByShipmentTaskId(task.getNxDstId());
            if (stop == null || stop.getNxDrsDriverRouteId() == null) {
                continue;
            }
            NxDisDriverRouteEntity route = nxDisDriverRouteDao.queryObject(stop.getNxDrsDriverRouteId());
            if (route == null) {
                continue;
            }
            attachStopsWithTasks(route);
            return route;
        }
        return null;
    }

    private void attachStopsWithTasks(NxDisDriverRouteEntity driverRoute) {
        List<NxDisRouteStopEntity> stops = nxDisRouteStopDao.queryByDriverRouteId(driverRoute.getNxDdrId());
        List<NxDisRouteStopEntity> routeStops = stops != null
                ? stops : new ArrayList<NxDisRouteStopEntity>();
        DisRoutePlanPresentationHelper.prepareStopsForReadModel(routeStops);
        for (NxDisRouteStopEntity stop : routeStops) {
            stop.setOrders(null);
            stop.setOrderIds(null);
            if (stop.getNxDrsShipmentTaskId() != null) {
                stop.setShipmentTask(disShipmentTaskService.queryTaskDetail(stop.getNxDrsShipmentTaskId()));
            }
        }
        driverRoute.setStops(routeStops);
    }

    private String buildMissingDriverRouteMessage(Integer planId, Integer driverUserId) {
        StringBuilder message = new StringBuilder();
        message.append("driver_route 未找到：planId=").append(planId)
                .append(", driverUserId=").append(driverUserId);
        List<NxDisDriverRouteEntity> routes = nxDisDriverRouteDao.queryByPlanId(planId);
        if (routes == null || routes.isEmpty()) {
            message.append("；plan 下无 driver_route 记录");
        } else {
            message.append("；plan 下已有 driver_route driverUserId=[");
            for (int i = 0; i < routes.size(); i++) {
                if (i > 0) {
                    message.append(',');
                }
                NxDisDriverRouteEntity route = routes.get(i);
                message.append(route.getNxDdrDriverUserId())
                        .append("(routeId=").append(route.getNxDdrId()).append(')');
            }
            message.append(']');
        }
        return message.toString();
    }

    @Override
    public List<NxDistributerUserEntity> listDrivers(Integer disId) {
        return queryDriverAccountsByDisId(disId);
    }

    /**
     * simulate 司机来源（须 ON_DUTY 且适合当前批次）：
     * 1. request.driverUserIds 非空 → 校验归属/角色/上岗/批次可派后使用；
     * 2. 为空或缺失 → disId + routeDate 下 ON_DUTY 且 batchEligible 司机。
     */
    private List<NxDistributerUserEntity> resolveDrivers(DisRoutePreviewRequest request,
                                                         String routeDate,
                                                         DisRouteBatchContext batch) {
        Date latestCheckInAt = DisRouteBatchDefaults.latestAllowedCheckInAt(batch.getDefaultDepartAt());
        if (hasExplicitDriverUserIds(request)) {
            return resolveExplicitDrivers(request, routeDate, batch, latestCheckInAt);
        }
        return resolveAutoBatchEligibleDrivers(request.getDisId(), routeDate, latestCheckInAt);
    }

    private List<NxDistributerUserEntity> resolveAutoBatchEligibleDrivers(Integer disId,
                                                                          String routeDate,
                                                                          Date latestCheckInAt) {
        List<NxDistributerUserEntity> onDutyDrivers = disDriverDutyService.listOnDutyDriverUsers(disId, routeDate);
        if (onDutyDrivers.isEmpty()) {
            throw new IllegalArgumentException(NO_ON_DUTY_DRIVERS_MSG);
        }
        List<NxDistributerUserEntity> eligible = new ArrayList<NxDistributerUserEntity>();
        for (NxDistributerUserEntity driver : onDutyDrivers) {
            if (isDriverBatchEligible(disId, driver.getNxDistributerUserId(), routeDate, latestCheckInAt)) {
                eligible.add(driver);
            }
        }
        return eligible;
    }

    private boolean hasExplicitDriverUserIds(DisRoutePreviewRequest request) {
        return request.getDriverUserIds() != null && !request.getDriverUserIds().isEmpty();
    }

    private List<NxDistributerUserEntity> resolveExplicitDrivers(DisRoutePreviewRequest request,
                                                                 String routeDate,
                                                                 DisRouteBatchContext batch,
                                                                 Date latestCheckInAt) {
        List<NxDistributerUserEntity> selected = new ArrayList<NxDistributerUserEntity>();
        for (Integer driverUserId : request.getDriverUserIds()) {
            if (driverUserId == null) {
                continue;
            }
            NxDistributerUserEntity driver = nxDistributerUserDao.queryObject(driverUserId);
            if (driver == null) {
                throw new IllegalArgumentException("司机不存在: " + driverUserId);
            }
            if (!request.getDisId().equals(driver.getNxDiuDistributerId())) {
                throw new IllegalArgumentException("司机不属于该配送商: " + driverUserId);
            }
            if (!getNxDisUserDriver().equals(driver.getNxDiuAdmin())) {
                throw new IllegalArgumentException("用户不是司机角色: " + driverUserId);
            }
            disDriverDutyService.requireDriverOnDuty(
                    request.getDisId(), driverUserId, routeDate, driver.getNxDiuWxNickName());
            if (!isDriverBatchEligible(request.getDisId(), driverUserId, routeDate, latestCheckInAt)) {
                String label = driver.getNxDiuWxNickName() != null && !driver.getNxDiuWxNickName().trim().isEmpty()
                        ? driver.getNxDiuWxNickName().trim() : String.valueOf(driverUserId);
                throw new IllegalArgumentException(
                        "司机不适合当前批次 " + batch.getBatchCode() + "：" + label
                                + " 上岗时间晚于 " + formatDateTime(latestCheckInAt));
            }
            selected.add(driver);
        }
        if (selected.isEmpty()) {
            throw new IllegalArgumentException(NO_ON_DUTY_DRIVERS_MSG);
        }
        return selected;
    }

    private boolean isDriverBatchEligible(Integer disId,
                                          Integer driverUserId,
                                          String routeDate,
                                          Date latestCheckInAt) {
        NxDisDriverDutyEntity duty = nxDisDriverDutyDao.queryByDisDriverDate(disId, driverUserId, routeDate);
        if (duty == null || !ON_DUTY.equals(duty.getNxDddDutyStatus()) || duty.getNxDddCheckInAt() == null) {
            return false;
        }
        if (latestCheckInAt == null) {
            return true;
        }
        return !duty.getNxDddCheckInAt().after(latestCheckInAt);
    }

    private String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private DisRouteDispatchResult buildDispatchResult(Integer planId, RouteFeasibilityResult feasibility) {
        DisRouteDispatchResult result = new DisRouteDispatchResult();
        result.setPlan(loadPlanDetailWithTasks(planId));
        result.setTasks(disShipmentTaskService.queryTasksByPlanId(planId));
        if (feasibility != null) {
            result.setFeasibilityStatus(feasibility.getFeasibilityStatus());
            result.setWarnings(feasibility.getWarnings());
        }
        return result;
    }

    /** 配送商下全部司机账号（nx_DIU_admin=5），不含上岗过滤；供配置/列表用 */
    private List<NxDistributerUserEntity> queryDriverAccountsByDisId(Integer disId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("disId", disId);
        map.put("admin", getNxDisUserDriver());
        List<NxDistributerUserEntity> drivers = nxDistributerUserDao.getAdminUserByParams(map);
        if (drivers == null || drivers.isEmpty()) {
            return new ArrayList<NxDistributerUserEntity>();
        }
        Collections.sort(drivers, new Comparator<NxDistributerUserEntity>() {
            @Override
            public int compare(NxDistributerUserEntity a, NxDistributerUserEntity b) {
                return Integer.compare(a.getNxDistributerUserId(), b.getNxDistributerUserId());
            }
        });
        return drivers;
    }

    private List<DriverInput> toDriverInputs(List<NxDistributerUserEntity> drivers) {
        List<DriverInput> inputs = new ArrayList<DriverInput>();
        for (NxDistributerUserEntity driver : drivers) {
            DriverInput input = new DriverInput();
            input.setDriverUserId(driver.getNxDistributerUserId());
            input.setDriverName(driver.getNxDiuWxNickName());
            inputs.add(input);
        }
        return inputs;
    }

    private void validatePreviewRequest(DisRoutePreviewRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
    }

    private String resolveRouteDate(DisRoutePreviewRequest request) {
        if (request.getRouteDate() != null && !request.getRouteDate().trim().isEmpty()) {
            return request.getRouteDate().trim();
        }
        if (request.getPlanDate() != null && !request.getPlanDate().trim().isEmpty()) {
            return request.getPlanDate().trim();
        }
        return formatWhatDay(0);
    }

    private GeoPoint resolveDepot(DisRoutePreviewRequest request) {
        if (isValidCoordinate(request.getDepotLat(), request.getDepotLng())) {
            return toPoint(request.getDepotLat(), request.getDepotLng());
        }
        NxDistributerEntity dis = nxDistributerService.queryObject(request.getDisId());
        if (dis != null && isValidCoordinate(dis.getNxDistributerLan(), dis.getNxDistributerLun())) {
            return toPoint(dis.getNxDistributerLan(), dis.getNxDistributerLun());
        }
        throw new IllegalArgumentException("配送商出发点坐标无效，请传入 depotLat/depotLng");
    }

    private RouteCostProviderType parseCostProviderType(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return RouteCostProviderType.TENCENT_MATRIX;
        }
        return RouteCostProviderType.valueOf(raw.trim());
    }

    private RouteOptimizerType parseOptimizerType(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return RouteOptimizerType.BALANCED_INSERTION_2OPT;
        }
        return RouteOptimizerType.valueOf(raw.trim());
    }

    private Map<Integer, List<DisRouteOrderSnapshotDto>> groupOrdersByDepartment(List<DisRouteOrderSnapshotDto> orderRows) {
        Map<Integer, List<DisRouteOrderSnapshotDto>> grouped = new LinkedHashMap<Integer, List<DisRouteOrderSnapshotDto>>();
        for (DisRouteOrderSnapshotDto row : orderRows) {
            if (!grouped.containsKey(row.getDepartmentId())) {
                grouped.put(row.getDepartmentId(), new ArrayList<DisRouteOrderSnapshotDto>());
            }
            grouped.get(row.getDepartmentId()).add(row);
        }
        return grouped;
    }

    private NxDisRoutePlanEntity resolveOrCreateActivePlan(DisRoutePreviewRequest request,
                                                           String routeDate,
                                                           GeoPoint depot,
                                                           RouteCostProviderType costType,
                                                           RouteOptimizerType optimizerType,
                                                           List<NxDistributerUserEntity> drivers,
                                                           DisRouteBatchContext batch) {
        Integer disId = request.getDisId();
        String batchCode = batch.getBatchCode();
        NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                disId, routeDate, batchCode, SIMULATED);
        if (plan == null) {
            plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                    disId, routeDate, batchCode, DisRoutePlanStatus.ASSIGNED);
        }
        if (plan == null) {
            plan = new NxDisRoutePlanEntity();
            plan.setNxDrpDistributerId(disId);
            plan.setNxDrpPlanDate(routeDate);
            plan.setNxDrpRouteDate(routeDate);
            plan.setNxDrpStatus(SIMULATED);
            plan.setNxDrpDepotLat(depot.getLat());
            plan.setNxDrpDepotLng(depot.getLng());
            plan.setNxDrpOptimizerType(optimizerType.name());
            plan.setNxDrpCostProviderType(costType.name());
            plan.setNxDrpDriverCount(drivers.size());
            plan.setNxDrpCreatedBy(request.getOperatorUserId());
            plan.setNxDrpCreatedAt(new Date());
            plan.setNxDrpTotalDistanceM(0L);
            plan.setNxDrpTotalDurationS(0L);
            nxDisRoutePlanDao.save(plan);
        } else {
            NxDisRoutePlanEntity update = new NxDisRoutePlanEntity();
            update.setNxDrpId(plan.getNxDrpId());
            update.setNxDrpDepotLat(depot.getLat());
            update.setNxDrpDepotLng(depot.getLng());
            update.setNxDrpOptimizerType(optimizerType.name());
            update.setNxDrpCostProviderType(costType.name());
            update.setNxDrpDriverCount(drivers.size());
            nxDisRoutePlanDao.update(update);
            plan = nxDisRoutePlanDao.queryObject(plan.getNxDrpId());
        }
        persistBatchFields(plan.getNxDrpId(), batch);
        return nxDisRoutePlanDao.queryObject(plan.getNxDrpId());
    }

    private void persistBatchFields(Integer planId, DisRouteBatchContext batch) {
        NxDisRoutePlanEntity update = new NxDisRoutePlanEntity();
        update.setNxDrpId(planId);
        update.setNxDrpDispatchBatch(batch.getBatchCode());
        update.setNxDrpBatchStartAt(batch.getBatchStartAt());
        update.setNxDrpBatchEndAt(batch.getBatchEndAt());
        update.setNxDrpDefaultDepartAt(batch.getDefaultDepartAt());
        nxDisRoutePlanDao.updateBatch(update);
    }

    private NxDisShipmentTaskEntity upsertOpenTaskFromOrders(Integer disId,
                                                             String routeDate,
                                                             Integer planId,
                                                             List<DisRouteOrderSnapshotDto> orders) {
        DisRouteOrderSnapshotDto first = orders.get(0);
        Integer depFatherId = first.getDepartmentId();
        String openKey = DisShipmentTaskOpenKeyUtils.buildOpenKey(disId, routeDate, depFatherId);
        boolean hasCoords = isValidCoordinate(first.getLat(), first.getLng());

        NxDisShipmentTaskEntity existing = nxDisShipmentTaskDao.queryByOpenKey(openKey);
        boolean isNew = existing == null;
        NxDisShipmentTaskEntity task = isNew ? new NxDisShipmentTaskEntity() : existing;
        boolean protectedTask = !isNew && isTaskSimulateProtected(existing);

        if (isNew) {
            task.setNxDstDistributerId(disId);
            task.setNxDstRouteDate(routeDate);
            task.setNxDstDepFatherId(depFatherId);
            task.setNxDstOpenKey(openKey);
            task.setNxDstManualLocked(0);
            task.setNxDstPriorityLevel(0);
            task.setNxDstPlanId(planId);
            task.setNxDstStatus(hasCoords ? DisShipmentTaskStatus.SIMULATED : DisShipmentTaskStatus.UNASSIGNED);
        }

        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(isNew ? null : existing.getNxDstId());
        update.setNxDstDepName(first.getDepartmentName());
        update.setNxDstLat(first.getLat());
        update.setNxDstLng(first.getLng());
        update.setNxDstAddress(first.getAddress());
        update.setNxDstPlanId(planId);

        if (!protectedTask) {
            if (hasCoords) {
                if (isNew || DisShipmentTaskStatus.UNASSIGNED.equals(existing.getNxDstStatus())) {
                    update.setNxDstStatus(DisShipmentTaskStatus.SIMULATED);
                }
            } else {
                update.setNxDstStatus(DisShipmentTaskStatus.UNASSIGNED);
            }
        }

        if (isNew) {
            task.setNxDstDepName(first.getDepartmentName());
            task.setNxDstLat(first.getLat());
            task.setNxDstLng(first.getLng());
            task.setNxDstAddress(first.getAddress());
            nxDisShipmentTaskDao.save(task);
            update.setNxDstId(task.getNxDstId());
        } else {
            update.setNxDstId(existing.getNxDstId());
            nxDisShipmentTaskDao.update(update);
            task = nxDisShipmentTaskDao.queryObject(existing.getNxDstId());
        }

        for (DisRouteOrderSnapshotDto order : orders) {
            upsertTaskItem(task.getNxDstId(), order);
        }

        persistTaskDispatchSnapshot(task.getNxDstId());

        if (!protectedTask && !hasCoords) {
            removeStopForUnassignedTask(task);
        }

        return nxDisShipmentTaskDao.queryObject(task.getNxDstId());
    }

    private void upsertTaskItem(Integer taskId, DisRouteOrderSnapshotDto order) {
        NxDisShipmentTaskItemEntity existing = nxDisShipmentTaskItemDao.queryByLiveOrderId(order.getOrderId());
        if (existing != null) {
            NxDisShipmentTaskItemEntity update = new NxDisShipmentTaskItemEntity();
            update.setNxDstiId(existing.getNxDstiId());
            update.setNxDstiGoodsName(order.getGoodsName());
            update.setNxDstiQuantity(order.getQuantity());
            update.setNxDstiStandard(order.getStandard());
            update.setNxDstiRemark(order.getRemark());
            if (!taskId.equals(existing.getNxDstiTaskId())) {
                update.setNxDstiTaskId(taskId);
            }
            update.setNxDstiItemStatus(ACTIVE);
            nxDisShipmentTaskItemDao.update(update);
        } else {
            NxDisShipmentTaskItemEntity item = new NxDisShipmentTaskItemEntity();
            item.setNxDstiTaskId(taskId);
            item.setNxDstiLiveOrderId(order.getOrderId());
            item.setNxDstiGoodsName(order.getGoodsName());
            item.setNxDstiQuantity(order.getQuantity());
            item.setNxDstiStandard(order.getStandard());
            item.setNxDstiRemark(order.getRemark());
            item.setNxDstiItemStatus(ACTIVE);
            nxDisShipmentTaskItemDao.save(item);
        }
    }

    private boolean isTaskSimulateProtected(NxDisShipmentTaskEntity task) {
        if (task == null) {
            return false;
        }
        if (task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1) {
            return true;
        }
        String status = task.getNxDstStatus();
        return DisShipmentTaskStatus.ASSIGNED.equals(status)
                || DisShipmentTaskStatus.READY_TO_GO.equals(status)
                || DisShipmentTaskStatus.IN_DELIVERY.equals(status)
                || DisShipmentTaskStatus.DELIVERED.equals(status);
    }

    private void removeStopForUnassignedTask(NxDisShipmentTaskEntity task) {
        if (task == null || isTaskSimulateProtected(task)) {
            return;
        }
        nxDisRouteStopDao.deleteByShipmentTaskId(task.getNxDstId());
    }

    private void ensureDriverRoutes(NxDisRoutePlanEntity plan, List<NxDistributerUserEntity> drivers) {
        int seq = 1;
        for (NxDistributerUserEntity driver : drivers) {
            NxDisDriverRouteEntity existing = nxDisDriverRouteDao.queryByPlanAndDriver(
                    plan.getNxDrpId(), driver.getNxDistributerUserId());
            if (existing == null) {
                NxDisDriverRouteEntity driverRoute = new NxDisDriverRouteEntity();
                driverRoute.setNxDdrPlanId(plan.getNxDrpId());
                driverRoute.setNxDdrDriverUserId(driver.getNxDistributerUserId());
                driverRoute.setNxDdrRouteSeq(seq++);
                driverRoute.setNxDdrTotalDistanceM(0L);
                driverRoute.setNxDdrTotalDurationS(0L);
                driverRoute.setNxDdrStopCount(0);
                nxDisDriverRouteDao.save(driverRoute);
            } else {
                seq++;
            }
        }
    }

    private List<NxDisShipmentTaskEntity> collectOptimizablePlanTasks(Integer planId) {
        List<NxDisShipmentTaskEntity> planTasks = nxDisShipmentTaskDao.queryByPlanId(planId);
        if (planTasks == null || planTasks.isEmpty()) {
            return new ArrayList<NxDisShipmentTaskEntity>();
        }
        List<NxDisShipmentTaskEntity> optimizable = new ArrayList<NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : planTasks) {
            if (isTaskSimulateProtected(task)) {
                continue;
            }
            if (!isValidCoordinate(task.getNxDstLat(), task.getNxDstLng())) {
                continue;
            }
            if (DisShipmentTaskStatus.UNASSIGNED.equals(task.getNxDstStatus())) {
                continue;
            }
            optimizable.add(task);
        }
        return optimizable;
    }

    private RouteOptimizeResult runOptimizationForTasks(GeoPoint depot,
                                                        RouteCostProviderType costType,
                                                        RouteOptimizerType optimizerType,
                                                        List<NxDistributerUserEntity> drivers,
                                                        List<NxDisShipmentTaskEntity> optimizableTasks) throws Exception {
        if (optimizableTasks.isEmpty()) {
            return null;
        }
        List<RouteStopInput> stopInputs = new ArrayList<RouteStopInput>();
        for (NxDisShipmentTaskEntity task : optimizableTasks) {
            stopInputs.add(toStopInputFromTask(task));
        }

        RouteCostProvider costProvider = routeEngineRegistry.costProvider(costType);
        CostMatrix matrix = costProvider.buildMatrix(depot, stopInputs);

        RouteOptimizeRequest optimizeRequest = new RouteOptimizeRequest();
        optimizeRequest.setDepot(depot);
        optimizeRequest.setStops(stopInputs);
        optimizeRequest.setCostMatrix(matrix);
        optimizeRequest.setOptimizerType(optimizerType);
        optimizeRequest.setDrivers(toDriverInputs(drivers));

        RouteOptimizer optimizer = routeEngineRegistry.optimizer(optimizerType);
        return optimizer.optimize(optimizeRequest);
    }

    private RouteStopInput toStopInputFromTask(NxDisShipmentTaskEntity task) {
        RouteStopInput stop = new RouteStopInput();
        stop.setShipmentTaskId(task.getNxDstId());
        stop.setStopKey(String.valueOf(task.getNxDstDepFatherId()));
        stop.setDepartmentId(task.getNxDstDepFatherId());
        stop.setDepartmentName(task.getNxDstDepName());
        stop.setLocation(toPoint(task.getNxDstLat(), task.getNxDstLng()));
        stop.setAddress(task.getNxDstAddress());
        stop.setOrderCount(countActiveItems(task.getNxDstId()));
        return stop;
    }

    private int countActiveItems(Integer taskId) {
        return disRouteDispatchSnapshotHelper.countActiveItems(taskId);
    }

    private void applyOptimizationToUnlockedTasks(NxDisRoutePlanEntity plan,
                                                  RouteOptimizeResult optimizeResult,
                                                  List<NxDisShipmentTaskEntity> optimizableTasks,
                                                  Map<Integer, Set<Integer>> occupiedStopSeqByDriverRoute) {
        if (optimizeResult == null || optimizableTasks.isEmpty()) {
            return;
        }
        Map<Integer, NxDisShipmentTaskEntity> taskByDepId = new HashMap<Integer, NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : optimizableTasks) {
            taskByDepId.put(task.getNxDstDepFatherId(), task);
        }

        for (OptimizedDriverRouteResult driverRouteResult : optimizeResult.getDriverRoutes()) {
            NxDisDriverRouteEntity driverRoute = nxDisDriverRouteDao.queryByPlanAndDriver(
                    plan.getNxDrpId(), driverRouteResult.getDriverUserId());
            if (driverRoute == null) {
                continue;
            }
            Set<Integer> occupied = occupiedStopSeqByDriverRoute.get(driverRoute.getNxDdrId());
            if (occupied == null) {
                occupied = new HashSet<Integer>();
                occupiedStopSeqByDriverRoute.put(driverRoute.getNxDdrId(), occupied);
            }

            for (OptimizedStopResult stopResult : driverRouteResult.getStops()) {
                NxDisShipmentTaskEntity task = taskByDepId.get(stopResult.getDepartmentId());
                if (task == null || isTaskSimulateProtected(task)) {
                    continue;
                }
                int remappedStopSeq = remapOptimizerStopSeq(stopResult.getStopSeq(), occupied);
                if (occupied.contains(remappedStopSeq)) {
                    throw new IllegalStateException(
                            "司机路线 stopSeq 冲突：driverRouteId=" + driverRoute.getNxDdrId()
                                    + " 无法为 unlocked task=" + task.getNxDstId()
                                    + " 分配 stopSeq=" + remappedStopSeq);
                }
                occupied.add(remappedStopSeq);

                NxDisShipmentTaskEntity taskUpdate = new NxDisShipmentTaskEntity();
                taskUpdate.setNxDstId(task.getNxDstId());
                taskUpdate.setNxDstSuggestedDriverUserId(driverRouteResult.getDriverUserId());
                nxDisShipmentTaskDao.update(taskUpdate);

                upsertRouteStopForTask(task, driverRoute, stopResult, remappedStopSeq);
            }
        }
    }

    /**
     * 收集每条 driver_route 上 locked stop 已占用的 stopSeq，并校验 locked 之间不重复。
     * manualStopSeq 优先；否则用现有 route_stop.stopSeq。
     */
    private Map<Integer, Set<Integer>> buildAndValidateLockedOccupiedSeq(Integer planId) {
        Map<Integer, Set<Integer>> occupiedByRoute = new HashMap<Integer, Set<Integer>>();
        List<NxDisDriverRouteEntity> driverRoutes = nxDisDriverRouteDao.queryByPlanId(planId);
        if (driverRoutes != null) {
            for (NxDisDriverRouteEntity driverRoute : driverRoutes) {
                occupiedByRoute.put(driverRoute.getNxDdrId(), new HashSet<Integer>());
            }
        }

        List<NxDisShipmentTaskEntity> planTasks = nxDisShipmentTaskDao.queryByPlanId(planId);
        if (planTasks == null) {
            return occupiedByRoute;
        }

        for (NxDisShipmentTaskEntity task : planTasks) {
            if (!isTaskSimulateProtected(task)) {
                continue;
            }
            NxDisRouteStopEntity stop = nxDisRouteStopDao.queryByShipmentTaskId(task.getNxDstId());
            if (stop == null || stop.getNxDrsDriverRouteId() == null) {
                continue;
            }
            Integer effectiveSeq = resolveLockedStopSeq(task, stop);
            if (effectiveSeq == null) {
                continue;
            }

            Set<Integer> occupied = occupiedByRoute.get(stop.getNxDrsDriverRouteId());
            if (occupied == null) {
                occupied = new HashSet<Integer>();
                occupiedByRoute.put(stop.getNxDrsDriverRouteId(), occupied);
            }
            if (occupied.contains(effectiveSeq)) {
                throw new IllegalStateException(
                        "司机路线 stopSeq 冲突：driverRouteId=" + stop.getNxDrsDriverRouteId()
                                + " 存在多个锁定停靠点占用 stopSeq=" + effectiveSeq
                                + "（请先人工调整 manualStopSeq）");
            }
            occupied.add(effectiveSeq);
        }
        return occupiedByRoute;
    }

    private Integer resolveLockedStopSeq(NxDisShipmentTaskEntity task, NxDisRouteStopEntity stop) {
        if (task.getNxDstManualStopSeq() != null) {
            return task.getNxDstManualStopSeq();
        }
        if (stop.getNxDrsStopSeq() != null) {
            return stop.getNxDrsStopSeq();
        }
        return null;
    }

    /** 将 optimizer 相对序号映射到跳过 locked 占用后的实际 stopSeq。 */
    private int remapOptimizerStopSeq(int optimizerSeq, Set<Integer> occupied) {
        if (optimizerSeq <= 0) {
            throw new IllegalArgumentException("optimizer stopSeq 必须 >= 1");
        }
        int actual = 0;
        int remaining = optimizerSeq;
        while (remaining > 0) {
            actual++;
            if (!occupied.contains(actual)) {
                remaining--;
            }
        }
        return actual;
    }

    private void upsertRouteStopForTask(NxDisShipmentTaskEntity task,
                                        NxDisDriverRouteEntity driverRoute,
                                        OptimizedStopResult stopResult,
                                        int stopSeq) {
        int orderCount = countActiveItems(task.getNxDstId());
        NxDisRouteStopEntity stop = nxDisRouteStopDao.queryByShipmentTaskId(task.getNxDstId());
        if (stop == null) {
            stop = new NxDisRouteStopEntity();
            stop.setNxDrsDriverRouteId(driverRoute.getNxDdrId());
            stop.setNxDrsShipmentTaskId(task.getNxDstId());
            stop.setNxDrsStopSeq(stopSeq);
            stop.setNxDrsDepartmentId(task.getNxDstDepFatherId());
            stop.setNxDrsDepartmentName(task.getNxDstDepName());
            stop.setNxDrsLat(task.getNxDstLat());
            stop.setNxDrsLng(task.getNxDstLng());
            stop.setNxDrsAddress(task.getNxDstAddress());
            stop.setNxDrsOrderCount(orderCount);
            stop.setNxDrsLegDistanceM(stopResult.getLegDistanceM());
            stop.setNxDrsLegDurationS(stopResult.getLegDurationS());
            stop.setNxDrsStopStatus("PENDING");
            nxDisRouteStopDao.save(stop);
        } else {
            NxDisRouteStopEntity update = new NxDisRouteStopEntity();
            update.setNxDrsId(stop.getNxDrsId());
            update.setNxDrsDriverRouteId(driverRoute.getNxDdrId());
            update.setNxDrsStopSeq(stopSeq);
            update.setNxDrsOrderCount(orderCount);
            update.setNxDrsLegDistanceM(stopResult.getLegDistanceM());
            update.setNxDrsLegDurationS(stopResult.getLegDurationS());
            nxDisRouteStopDao.update(update);
        }
        syncStopDispatchSnapshot(task.getNxDstId());
    }

    private void persistTaskDispatchSnapshot(Integer taskId) {
        NxDisShipmentTaskEntity task = disShipmentTaskService.queryTaskDetail(taskId);
        if (task == null) {
            return;
        }
        boolean preserveOverride = task.getNxDstTimeWindowOverrideFlag() != null
                && task.getNxDstTimeWindowOverrideFlag() == 1;
        NxDisShipmentTaskEntity snapshotUpdate = disRouteDispatchSnapshotHelper.buildTaskUpdateFromSnapshot(
                task, preserveOverride);
        nxDisShipmentTaskDao.update(snapshotUpdate);
    }

    private void syncStopDispatchSnapshot(Integer taskId) {
        NxDisShipmentTaskEntity task = disShipmentTaskService.queryTaskDetail(taskId);
        NxDisRouteStopEntity stop = nxDisRouteStopDao.queryByShipmentTaskId(taskId);
        if (task == null || stop == null) {
            return;
        }
        boolean preserveOverride = task.getNxDstTimeWindowOverrideFlag() != null
                && task.getNxDstTimeWindowOverrideFlag() == 1;
        NxDisShipmentTaskEntity taskUpdate = disRouteDispatchSnapshotHelper.buildTaskUpdateFromSnapshot(
                task, preserveOverride);
        nxDisShipmentTaskDao.update(taskUpdate);
        NxDisRouteStopEntity stopUpdate = disRouteDispatchSnapshotHelper.buildStopUpdateFromTaskSnapshot(task);
        stopUpdate.setNxDrsId(stop.getNxDrsId());
        nxDisRouteStopDao.updateDispatchSnapshot(stopUpdate);
    }

    private void refreshPlanTotalsFromOptimizer(Integer planId, RouteOptimizeResult optimizeResult) {
        NxDisRoutePlanEntity update = new NxDisRoutePlanEntity();
        update.setNxDrpId(planId);
        if (optimizeResult != null) {
            update.setNxDrpTotalDistanceM(optimizeResult.getTotalDistanceM());
            update.setNxDrpTotalDurationS(optimizeResult.getTotalDurationS());
        }
        nxDisRoutePlanDao.update(update);
    }

    private NxDisRoutePlanEntity loadPlanDetailWithTasks(Integer planId) {
        NxDisRoutePlanEntity plan = loadPlanDetail(planId);
        if (plan == null) {
            return null;
        }
        attachShipmentTasksToStops(plan);
        plan.setShipmentTasks(disShipmentTaskService.queryTasksByPlanId(planId));
        return plan;
    }

    private void attachShipmentTasksToStops(NxDisRoutePlanEntity plan) {
        if (plan.getDriverRoutes() == null) {
            return;
        }
        for (NxDisDriverRouteEntity driverRoute : plan.getDriverRoutes()) {
            if (driverRoute.getStops() == null) {
                continue;
            }
            for (NxDisRouteStopEntity stop : driverRoute.getStops()) {
                stop.setOrders(null);
                stop.setOrderIds(null);
                if (stop.getNxDrsShipmentTaskId() != null) {
                    stop.setShipmentTask(disShipmentTaskService.queryTaskDetail(stop.getNxDrsShipmentTaskId()));
                }
            }
        }
    }

    private NxDisRoutePlanEntity loadPlanDetail(Integer planId) {
        NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryObject(planId);
        if (plan == null) {
            return null;
        }
        List<NxDisDriverRouteEntity> driverRoutes = nxDisDriverRouteDao.queryByPlanId(planId);
        if (!driverRoutes.isEmpty()) {
            List<Integer> routeIds = new ArrayList<Integer>();
            for (NxDisDriverRouteEntity route : driverRoutes) {
                routeIds.add(route.getNxDdrId());
            }
            List<NxDisRouteStopEntity> allStops = nxDisRouteStopDao.queryByDriverRouteIds(routeIds);
            Map<Integer, List<NxDisRouteStopEntity>> stopMap = new HashMap<Integer, List<NxDisRouteStopEntity>>();
            for (NxDisRouteStopEntity stop : allStops) {
                if (!stopMap.containsKey(stop.getNxDrsDriverRouteId())) {
                    stopMap.put(stop.getNxDrsDriverRouteId(), new ArrayList<NxDisRouteStopEntity>());
                }
                stopMap.get(stop.getNxDrsDriverRouteId()).add(stop);
            }
            for (NxDisDriverRouteEntity route : driverRoutes) {
                List<NxDisRouteStopEntity> stops = stopMap.get(route.getNxDdrId());
                List<NxDisRouteStopEntity> routeStops = stops != null
                        ? stops : new ArrayList<NxDisRouteStopEntity>();
                DisRoutePlanPresentationHelper.prepareStopsForReadModel(routeStops);
                route.setStops(routeStops);
            }
        }
        plan.setDriverRoutes(driverRoutes);
        return plan;
    }
}
