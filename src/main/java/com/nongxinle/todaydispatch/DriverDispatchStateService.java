package com.nongxinle.todaydispatch;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.route.DisRouteSandboxDispatchEligibilityHelper;
import com.nongxinle.route.DisRouteSandboxDriverDispatchStateHelper;
import com.nongxinle.route.DriverRouteEditPrimaryActionMaps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisRouteSandboxDriverDispatchPhase.ACTIVE_EXECUTION;
import static com.nongxinle.route.DisRouteSandboxDriverDispatchPhase.IDLE;
import static com.nongxinle.route.DisRouteSandboxDriverDispatchPhase.LOADING;
import static com.nongxinle.route.DisRouteSandboxDriverDispatchPhase.REDISPATCH_PRE_DEPART;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;

/**
 * 司机可派性唯一入口（compute / page / availableDrivers 共用）。
 * 参考旧 Helper 重写，不 import 旧 eligibility Helper。
 */
@Service
public class DriverDispatchStateService {

    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;

    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;

    public StateQuery forCompute(Integer disId,
                                               String routeDate,
                                               String batchCode,
                                               Integer primaryPlanId) {
        return StateQuery.of(disId, routeDate, batchCode, primaryPlanId, new Date());
    }

    /** 与 compute 同源：装车中/配送中等不可参与沙盘新单分派的司机。 */
    public Set<Integer> resolveDispatchBlockedDriverIds(TodayDispatchResult result) {
        if (result == null || result.getCompute() == null) {
            return new HashSet<Integer>();
        }
        return resolveDispatchBlockedDriverIds(buildStateQuery(result));
    }

    public Set<Integer> resolveDispatchBlockedDriverIds(StateQuery query) {
        if (query == null || query.getDisId() == null || query.getRouteDate() == null) {
            return new HashSet<Integer>();
        }
        return DisRouteSandboxDispatchEligibilityHelper.resolveSandboxDispatchIneligibleDriverUserIds(
                nxDisDriverRouteDao,
                nxDisRoutePlanDao,
                nxDisShipmentTaskDao,
                query.getDisId(),
                query.getRouteDate(),
                query.getPrimaryPlanId());
    }

    public boolean canReceiveNewStops(Integer driverUserId, StateQuery query) {
        if (driverUserId == null || query == null) {
            return false;
        }
        return DisRouteSandboxDispatchEligibilityHelper.isDriverEligibleForSandboxDispatch(
                driverUserId, resolveDispatchBlockedDriverIds(query));
    }

    public String resolveRoutePhase(NxDisDriverRouteEntity route, StateQuery query) {
        if (route == null) {
            return IDLE;
        }
        return DisRouteSandboxDriverDispatchStateHelper.resolveRoutePhase(
                route, nxDisDriverRouteDao, nxDisShipmentTaskDao);
    }

    public boolean canRouteAcceptEphemeralStops(NxDisDriverRouteEntity route,
                                                StateQuery query) {
        if (route == null || query == null) {
            return false;
        }
        String phase = resolveRoutePhase(route, query);
        return IDLE.equals(phase) || REDISPATCH_PRE_DEPART.equals(phase);
    }

    public boolean blocksAvailableIdleSlot(NxDisDriverRouteEntity route,
                                         StateQuery query) {
        if (route == null || query == null) {
            return false;
        }
        String phase = resolveRoutePhase(route, query);
        return LOADING.equals(phase) || ACTIVE_EXECUTION.equals(phase);
    }

    public boolean blocksAvailableIdleSlotByTaskStop(NxDisRouteStopEntity stop,
                                                     StateQuery query) {
        if (stop == null) {
            return false;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task == null || task.getNxDstStatus() == null) {
            return false;
        }
        String status = task.getNxDstStatus().trim().toUpperCase();
        return IN_DELIVERY.equals(status) || EXCEPTION.equals(status);
    }

    public Eligibility eligibilityFor(Integer driverUserId,
                                                    StateQuery query) {
        Eligibility eligibility = new Eligibility();
        eligibility.setDriverUserId(driverUserId);
        if (driverUserId == null || query == null) {
            eligibility.setPhase("OFF_DUTY");
            eligibility.setCanReceiveNewStops(false);
            eligibility.setBatchEligible(false);
            eligibility.setIneligibleReason("INVALID");
            eligibility.setIneligibleReasonLabel("无效司机");
            return eligibility;
        }
        if (!canReceiveNewStops(driverUserId, query)) {
            Set<Integer> blocked = resolveDispatchBlockedDriverIds(query);
            if (blocked.contains(driverUserId)) {
                NxDisDriverRouteEntity route = findPrimaryRouteForDriver(driverUserId, query);
                if (route != null) {
                    return eligibilityForRoute(route, driverUserId, query);
                }
            }
            return Eligibility.blockedOffDuty(driverUserId);
        }
        NxDisDriverRouteEntity route = findPrimaryRouteForDriver(driverUserId, query);
        if (route != null) {
            return eligibilityForRoute(route, driverUserId, query);
        }
        eligibility.setPhase(IDLE);
        eligibility.setCanReceiveNewStops(true);
        eligibility.setBatchEligible(true);
        eligibility.setBlocksAvailableIdleSlot(false);
        eligibility.setAcceptsEphemeralStopsOnRoute(true);
        return eligibility;
    }

    public Eligibility eligibilityForRoute(NxDisDriverRouteEntity route,
                                                           Integer driverUserId,
                                                           StateQuery query) {
        Eligibility eligibility = new Eligibility();
        eligibility.setDriverUserId(driverUserId);
        String phase = resolveRoutePhase(route, query);
        eligibility.setPhase(phase);
        boolean canReceive = IDLE.equals(phase) || REDISPATCH_PRE_DEPART.equals(phase);
        eligibility.setCanReceiveNewStops(canReceive);
        eligibility.setBatchEligible(canReceive);
        eligibility.setBlocksAvailableIdleSlot(blocksAvailableIdleSlot(route, query));
        eligibility.setAcceptsEphemeralStopsOnRoute(canRouteAcceptEphemeralStops(route, query));
        if (!canReceive) {
            if (LOADING.equals(phase)) {
                eligibility.setIneligibleReason("LOADING");
                eligibility.setIneligibleReasonLabel("装车中");
            } else if (ACTIVE_EXECUTION.equals(phase)) {
                eligibility.setIneligibleReason("ACTIVE_EXECUTION");
                eligibility.setIneligibleReasonLabel("配送中");
            }
        }
        return eligibility;
    }

    public List<Map<String, Object>> buildAvailableDrivers(TodayDispatchResult result,
                                                           Set<Integer> blocked) {
        List<Map<String, Object>> available = new ArrayList<Map<String, Object>>();
        SandboxComputeResult compute = result.getCompute();
        if (compute == null || compute.getOnDutyDrivers() == null) {
            return available;
        }
        Set<Integer> dispatchBlocked = blocked != null ? blocked : resolveDispatchBlockedDriverIds(result);
        Set<Integer> routed = collectRoutedDriverIds(result);
        for (NxDistributerUserEntity driver : compute.getOnDutyDrivers()) {
            if (driver == null || driver.getNxDistributerUserId() == null) {
                continue;
            }
            Integer driverId = driver.getNxDistributerUserId();
            if (routed.contains(driverId)) {
                continue;
            }
            if (!DisRouteSandboxDispatchEligibilityHelper.isDriverEligibleForSandboxDispatch(
                    driverId, dispatchBlocked)) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("driverUserId", driverId);
            item.put("driverName", resolveDriverDisplayName(driver));
            item.put("statusLabel", "空闲可派");
            item.put("routeEditAction", DriverRouteEditPrimaryActionMaps.enabledEditRoute(
                    "编辑路线",
                    DriverRouteEditPrimaryActionMaps.buildPayload(
                            result.getDisId(),
                            result.getRouteDate(),
                            result.getBatchCode(),
                            result.getOperatorUserId(),
                            driverId,
                            "DISPATCH_SANDBOX")));
            available.add(item);
        }
        return available;
    }

    private static StateQuery buildStateQuery(TodayDispatchResult result) {
        SandboxComputeResult compute = result.getCompute();
        Integer planId = null;
        if (compute.getPlanContext() != null) {
            planId = compute.getPlanContext().getNxDrpId();
        } else if (compute.getMergedPlan() != null) {
            planId = compute.getMergedPlan().getNxDrpId();
        }
        return StateQuery.of(result.getDisId(), result.getRouteDate(), result.getBatchCode(), planId, new Date());
    }

    private Set<Integer> collectRoutedDriverIds(TodayDispatchResult result) {
        Set<Integer> ids = new HashSet<Integer>();
        if (result.getSuggestedRoutes() != null) {
            for (DriverRoutePlan route : result.getSuggestedRoutes()) {
                if (route != null && route.getDriverUserId() != null) {
                    ids.add(route.getDriverUserId());
                }
            }
        }
        return ids;
    }

    private NxDisDriverRouteEntity findPrimaryRouteForDriver(Integer driverUserId,
                                                             StateQuery query) {
        NxDisDriverRouteEntity best = null;
        int bestPriority = 0;
        for (NxDisDriverRouteEntity route : loadPlanRoutes(query)) {
            if (route == null || !driverUserId.equals(route.getNxDdrDriverUserId())) {
                continue;
            }
            int priority = routeOverlayPriority(route, query);
            if (best == null || priority > bestPriority) {
                best = route;
                bestPriority = priority;
            }
        }
        return best;
    }

    private int routeOverlayPriority(NxDisDriverRouteEntity route, StateQuery query) {
        String phase = resolveRoutePhase(route, query);
        if (ACTIVE_EXECUTION.equals(phase)) {
            return 3;
        }
        if (LOADING.equals(phase)) {
            return 2;
        }
        return 1;
    }

    private List<NxDisDriverRouteEntity> loadPlanRoutes(StateQuery query) {
        List<NxDisDriverRouteEntity> routes = new ArrayList<NxDisDriverRouteEntity>();
        if (query == null || query.getPrimaryPlanId() == null || nxDisDriverRouteDao == null) {
            return routes;
        }
        List<NxDisDriverRouteEntity> dbRoutes = nxDisDriverRouteDao.queryByPlanId(query.getPrimaryPlanId());
        if (dbRoutes == null) {
            return routes;
        }
        for (NxDisDriverRouteEntity route : dbRoutes) {
            if (route != null) {
                hydrateRouteExecutionContext(route);
                routes.add(route);
            }
        }
        return routes;
    }

    private void hydrateRouteExecutionContext(NxDisDriverRouteEntity route) {
        DisRouteSandboxDriverDispatchStateHelper.hydrateRouteExecutionContext(
                route, nxDisDriverRouteDao, nxDisShipmentTaskDao);
    }

    private static String resolveDriverDisplayName(NxDistributerUserEntity driver) {
        if (driver.getNxDiuWxNickName() != null && !driver.getNxDiuWxNickName().trim().isEmpty()) {
            return driver.getNxDiuWxNickName().trim();
        }
        if (driver.getQyNxDisCorpUserEntity() != null
                && driver.getQyNxDisCorpUserEntity().getQyNxDisCorpUserName() != null
                && !driver.getQyNxDisCorpUserEntity().getQyNxDisCorpUserName().trim().isEmpty()) {
            return driver.getQyNxDisCorpUserEntity().getQyNxDisCorpUserName().trim();
        }
        if (driver.getNxDiuWxPhone() != null && !driver.getNxDiuWxPhone().trim().isEmpty()) {
            return driver.getNxDiuWxPhone().trim();
        }
        if (driver.getNxDiuLoginPhone() != null && !driver.getNxDiuLoginPhone().trim().isEmpty()) {
            return driver.getNxDiuLoginPhone().trim();
        }
        return String.valueOf(driver.getNxDistributerUserId());
    }

    /** 新链内部查询上下文。 */
    public static final class StateQuery {

        private Integer disId;
        private String routeDate;
        private String batchCode;
        private Integer primaryPlanId;
        private Date asOf;

        public static StateQuery of(Integer disId,
                                    String routeDate,
                                    String batchCode,
                                    Integer primaryPlanId,
                                    Date asOf) {
            StateQuery query = new StateQuery();
            query.disId = disId;
            query.routeDate = routeDate;
            query.batchCode = batchCode;
            query.primaryPlanId = primaryPlanId;
            query.asOf = asOf != null ? asOf : new Date();
            return query;
        }

        public Integer getDisId() {
            return disId;
        }

        public String getRouteDate() {
            return routeDate;
        }

        public String getBatchCode() {
            return batchCode;
        }

        public Integer getPrimaryPlanId() {
            return primaryPlanId;
        }

        public Date getAsOf() {
            return asOf;
        }
    }

    /** 新链内部 eligibility 结果。 */
    public static final class Eligibility {

        private Integer driverUserId;
        private String phase;
        private boolean canReceiveNewStops;
        private boolean batchEligible;
        private String ineligibleReason;
        private String ineligibleReasonLabel;
        private boolean blocksAvailableIdleSlot;
        private boolean acceptsEphemeralStopsOnRoute;

        public static Eligibility blockedOffDuty(Integer driverUserId) {
            Eligibility eligibility = new Eligibility();
            eligibility.driverUserId = driverUserId;
            eligibility.phase = "OFF_DUTY";
            eligibility.canReceiveNewStops = false;
            eligibility.batchEligible = false;
            eligibility.ineligibleReason = "OFF_DUTY";
            eligibility.ineligibleReasonLabel = "未上岗";
            eligibility.blocksAvailableIdleSlot = false;
            eligibility.acceptsEphemeralStopsOnRoute = true;
            return eligibility;
        }

        public Integer getDriverUserId() {
            return driverUserId;
        }

        public String getPhase() {
            return phase;
        }

        public boolean isCanReceiveNewStops() {
            return canReceiveNewStops;
        }

        public boolean isBatchEligible() {
            return batchEligible;
        }

        public String getIneligibleReason() {
            return ineligibleReason;
        }

        public String getIneligibleReasonLabel() {
            return ineligibleReasonLabel;
        }

        public boolean isBlocksAvailableIdleSlot() {
            return blocksAvailableIdleSlot;
        }

        public boolean isAcceptsEphemeralStopsOnRoute() {
            return acceptsEphemeralStopsOnRoute;
        }

        private void setDriverUserId(Integer driverUserId) {
            this.driverUserId = driverUserId;
        }

        private void setPhase(String phase) {
            this.phase = phase;
        }

        private void setCanReceiveNewStops(boolean canReceiveNewStops) {
            this.canReceiveNewStops = canReceiveNewStops;
        }

        private void setBatchEligible(boolean batchEligible) {
            this.batchEligible = batchEligible;
        }

        private void setIneligibleReason(String ineligibleReason) {
            this.ineligibleReason = ineligibleReason;
        }

        private void setIneligibleReasonLabel(String ineligibleReasonLabel) {
            this.ineligibleReasonLabel = ineligibleReasonLabel;
        }

        private void setBlocksAvailableIdleSlot(boolean blocksAvailableIdleSlot) {
            this.blocksAvailableIdleSlot = blocksAvailableIdleSlot;
        }

        private void setAcceptsEphemeralStopsOnRoute(boolean acceptsEphemeralStopsOnRoute) {
            this.acceptsEphemeralStopsOnRoute = acceptsEphemeralStopsOnRoute;
        }
    }
}
