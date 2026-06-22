package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverDutyDao;
import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.DisRouteBatchContext;
import com.nongxinle.dto.route.DispatchDriverCardDto;
import com.nongxinle.dto.route.DriverDispatchCandidateDto;
import com.nongxinle.dto.route.DriverDispatchListResponse;
import com.nongxinle.dto.route.DriverDispatchListSummaryDto;
import com.nongxinle.dto.route.SandboxComputeRequest;
import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.entity.NxDisDriverDutyEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.route.*;
import com.nongxinle.route.DisRouteSandboxManualDispatchPanoramaHelper.DriverStopCounts;
import com.nongxinle.service.DisRouteDispatchService;
import com.nongxinle.service.DisRouteDriverDispatchListService;
import com.nongxinle.service.DisRouteSandboxComputeService;
import com.nongxinle.service.DisShipmentTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisDriverDutyStatus.OFF_DUTY;
import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;
import static com.nongxinle.route.DisRoutePlanStatus.ASSIGNED;
import static com.nongxinle.route.DisRoutePlanStatus.READY;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service
public class DisRouteDriverDispatchListServiceImpl implements DisRouteDriverDispatchListService {

    @Autowired
    private DisRouteDispatchService disRouteDispatchService;
    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDisDriverDutyDao nxDisDriverDutyDao;
    @Autowired
    private DisShipmentTaskService disShipmentTaskService;
    @Autowired
    private DisRouteDispatchReadIntegrityHelper disRouteDispatchReadIntegrityHelper;
    @Autowired
    private DisRouteSandboxComputeService disRouteSandboxComputeService;

    @Override
    public DriverDispatchListResponse listDriversForBatch(Integer disId, String routeDate, String batchCode) {
        if (disId == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        String queryRouteDate = resolveRouteDate(routeDate);
        String dispatchBatch = normalizeBatchCode(batchCode);

        NxDisRoutePlanEntity planHeader = findPlanHeader(disId, queryRouteDate, dispatchBatch);
        if (planHeader != null) {
            DisRouteTemporalHelper.enrichPlanTemporalFields(planHeader, new Date());
        }

        Map<Integer, NxDisDriverRouteEntity> routeByDriver = loadRoutesByDriver(planHeader);
        Map<Integer, Integer> validStopCountByDriver = resolveValidStopCountByDriver(planHeader, queryRouteDate);

        List<NxDistributerUserEntity> driverAccounts = disRouteDispatchService.listDrivers(disId);
        List<DriverDispatchCandidateDto> candidates = new ArrayList<DriverDispatchCandidateDto>();
        for (NxDistributerUserEntity driver : driverAccounts) {
            candidates.add(buildCandidate(driver, disId, queryRouteDate, dispatchBatch, planHeader, routeByDriver,
                    validStopCountByDriver));
        }
        sortCandidates(candidates);

        DriverDispatchListResponse response = new DriverDispatchListResponse();
        response.setRouteDate(queryRouteDate);
        response.setDispatchBatch(dispatchBatch);
        response.setDispatchBatchLabel(DisRouteDispatchLabels.label(dispatchBatch));
        response.setSummary(buildSummary(candidates));
        response.setDrivers(candidates);
        response.setDriverCards(buildDriverDutyCards(
                candidates, disId, queryRouteDate, dispatchBatch, planHeader, routeByDriver));
        return response;
    }

    private DriverDispatchCandidateDto buildCandidate(NxDistributerUserEntity driver,
                                                      Integer disId,
                                                      String routeDate,
                                                      String dispatchBatch,
                                                      NxDisRoutePlanEntity planHeader,
                                                      Map<Integer, NxDisDriverRouteEntity> routeByDriver,
                                                      Map<Integer, Integer> validStopCountByDriver) {
        DriverDispatchCandidateDto dto = new DriverDispatchCandidateDto();
        dto.setDriverUserId(driver.getNxDistributerUserId());
        dto.setDriverName(resolveDriverName(driver));
        dto.setDriverPhone(driver.getNxDiuWxPhone());
        if (driver.getNxDiuWxAvartraUrl() != null && !driver.getNxDiuWxAvartraUrl().trim().isEmpty()) {
            dto.setDriverAvatarUrl(driver.getNxDiuWxAvartraUrl().trim());
        }
        dto.setDispatchBatch(dispatchBatch);
        dto.setDispatchBatchLabel(DisRouteDispatchLabels.label(dispatchBatch));

        NxDisDriverDutyEntity duty = nxDisDriverDutyDao.queryByDisDriverDate(
                disId, driver.getNxDistributerUserId(), routeDate);
        boolean onDuty = duty != null && ON_DUTY.equals(duty.getNxDddDutyStatus());
        dto.setDutyStatus(onDuty ? ON_DUTY : OFF_DUTY);
        dto.setDutyStatusLabel(DisRouteDispatchLabels.label(dto.getDutyStatus()));
        dto.setCheckInAt(RouteDispatchDateFormat.format(duty != null ? duty.getNxDddCheckInAt() : null));
        dto.setCheckOutAt(RouteDispatchDateFormat.format(duty != null ? duty.getNxDddCheckOutAt() : null));

        String ineligibleReason = onDuty ? null : DisRouteBatchEligibility.INELIGIBLE_OFF_DUTY;
        boolean batchEligible = onDuty;
        dto.setBatchEligible(batchEligible);
        dto.setBatchEligibleLabel(batchEligible ? "可参与当前批次" : "不参与派车");
        dto.setIneligibleReason(ineligibleReason);
        dto.setIneligibleReasonLabel(ineligibleReason != null ? "不可派" : null);

        NxDisDriverRouteEntity route = routeByDriver.get(driver.getNxDistributerUserId());
        Integer validStopCount = validStopCountByDriver.get(driver.getNxDistributerUserId());
        if (validStopCount == null) {
            validStopCount = 0;
        }
        if (route != null && planHeader != null) {
            DisRouteDriverDutyOverlayHelper.overlayOnRoute(route, planHeader, nxDisDriverDutyDao, new Date());
            applyDepartedDriverOverlay(dto, route);
            dto.setCurrentRouteId(route.getNxDdrId());
            dto.setConfirmedStopCount(validStopCount);
            dto.setCurrentStopCount(validStopCount);
            dto.setSandboxSuggestedStopCount(0);
            if (validStopCount == 0) {
                dto.setCurrentLateMinutes(0);
                dto.setCurrentWaitMinutes(0);
                dto.setCurrentFeasibilityStatus(DisRouteFeasibilityStatus.IDLE);
                dto.setCurrentFeasibilityStatusLabel(
                        DisRouteDriverDutyOverlayHelper.labelDriverRouteFeasibility(route, planHeader));
            } else {
                dto.setCurrentLateMinutes(route.getNxDdrTotalLateMinutes());
                dto.setCurrentWaitMinutes(route.getNxDdrTotalWaitMinutes());
                dto.setCurrentFeasibilityStatus(route.getNxDdrFeasibilityStatus());
                dto.setCurrentFeasibilityStatusLabel(
                        DisRouteDriverDutyOverlayHelper.labelDriverRouteFeasibility(route, planHeader));
            }
        } else if (route != null) {
            DisRouteDriverDutyOverlayHelper.sanitizeLegacyFields(route);
            applyDepartedDriverOverlay(dto, route);
            dto.setCurrentRouteId(route.getNxDdrId());
            dto.setConfirmedStopCount(validStopCount);
            dto.setCurrentStopCount(validStopCount);
            dto.setSandboxSuggestedStopCount(0);
            if (validStopCount == 0) {
                dto.setCurrentLateMinutes(0);
                dto.setCurrentWaitMinutes(0);
                dto.setCurrentFeasibilityStatus(DisRouteFeasibilityStatus.IDLE);
            } else {
                dto.setCurrentLateMinutes(route.getNxDdrTotalLateMinutes());
                dto.setCurrentWaitMinutes(route.getNxDdrTotalWaitMinutes());
                dto.setCurrentFeasibilityStatus(route.getNxDdrFeasibilityStatus());
            }
            dto.setCurrentFeasibilityStatusLabel(
                    DisRouteDriverDutyOverlayHelper.labelDriverRouteFeasibility(route, null));
        } else {
            dto.setConfirmedStopCount(0);
            dto.setCurrentStopCount(0);
            dto.setSandboxSuggestedStopCount(0);
            dto.setCurrentLateMinutes(0);
            dto.setCurrentWaitMinutes(0);
        }

        dto.setOperationHint(buildOperationHint(dispatchBatch, dto));
        return dto;
    }

    private List<DispatchDriverCardDto> buildDriverDutyCards(
            List<DriverDispatchCandidateDto> candidates,
            Integer disId,
            String routeDate,
            String dispatchBatch,
            NxDisRoutePlanEntity planHeader,
            Map<Integer, NxDisDriverRouteEntity> routeByDriver) {
        List<DispatchDriverCardDto> cards = new ArrayList<DispatchDriverCardDto>();
        if (candidates == null || candidates.isEmpty()) {
            return cards;
        }
        SandboxComputeResult compute = computeSandboxOptional(disId, routeDate, dispatchBatch);
        Map<Integer, NxDisDriverRouteEntity> mergedRoutes = mergeRoutesByDriver(routeByDriver, compute);
        Date serverNow = new Date();
        for (DriverDispatchCandidateDto candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            NxDisDriverRouteEntity route = mergedRoutes.get(candidate.getDriverUserId());
            if (route != null && planHeader != null) {
                DisRouteDriverDutyOverlayHelper.overlayOnRoute(route, planHeader, nxDisDriverDutyDao, serverNow);
            }
            String dispatchStage = resolveDriverDispatchStage(route, compute, candidate);
            DriverStopCounts stopCounts = resolveDriverStopCounts(compute, route, candidate);
            boolean onDuty = ON_DUTY.equals(candidate.getDutyStatus());
            boolean canToggleDuty;
            String toggleDisabledReason = null;
            Map<String, Object> toggleAction;
            if (onDuty) {
                canToggleDuty = DisRouteDriverDutyToggleHelper.canCloseDuty(dispatchStage);
                if (!canToggleDuty) {
                    toggleDisabledReason = DisRouteDriverDutyToggleHelper.resolveToggleDisabledReason(dispatchStage);
                    toggleAction = DriverDutyPrimaryActionMaps.disabledToggleDutyOff("不可关闭", toggleDisabledReason);
                } else {
                    toggleAction = DriverDutyPrimaryActionMaps.enabledToggleDutyOff(
                            "关闭可派",
                            DriverDutyPrimaryActionMaps.buildToggleDutyOffPayload(
                                    disId, routeDate, candidate.getDriverUserId(), null));
                }
            } else {
                canToggleDuty = true;
                toggleAction = DriverDutyPrimaryActionMaps.enabledToggleDutyOn(
                        "开启可派",
                        DriverDutyPrimaryActionMaps.buildToggleDutyOffPayload(
                                disId, routeDate, candidate.getDriverUserId(), null));
            }
            String operationHint = buildDutyCardOperationHint(dispatchBatch, candidate, dispatchStage, stopCounts);
            cards.add(DisRouteDispatchCardTemplateBuilder.buildDriverDutyCard(
                    candidate, route, dispatchStage, stopCounts,
                    canToggleDuty, toggleDisabledReason, toggleAction, operationHint));
            applyDutyToggleFields(candidate, disId, routeDate, dispatchStage);
        }
        return cards;
    }

    private SandboxComputeResult computeSandboxOptional(Integer disId, String routeDate, String batchCode) {
        try {
            SandboxComputeRequest request = new SandboxComputeRequest();
            request.setDisId(disId);
            request.setRouteDate(routeDate);
            request.setBatchCode(batchCode);
            return disRouteSandboxComputeService.compute(request);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Map<Integer, NxDisDriverRouteEntity> mergeRoutesByDriver(
            Map<Integer, NxDisDriverRouteEntity> routeByDriver,
            SandboxComputeResult compute) {
        Map<Integer, NxDisDriverRouteEntity> merged = new LinkedHashMap<Integer, NxDisDriverRouteEntity>();
        if (routeByDriver != null) {
            merged.putAll(routeByDriver);
        }
        if (compute != null && compute.getMergedPlan() != null) {
            appendPlanRoutes(merged, compute.getMergedPlan().getDriverRoutes());
            appendPlanRoutes(merged, compute.getMergedPlan().getLoadingDriverRoutes());
            appendPlanRoutes(merged, compute.getMergedPlan().getExecutionDriverRoutes());
        }
        return merged;
    }

    private void appendPlanRoutes(Map<Integer, NxDisDriverRouteEntity> index,
                                  List<NxDisDriverRouteEntity> routes) {
        if (index == null || routes == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route != null && route.getNxDdrDriverUserId() != null) {
                index.put(route.getNxDdrDriverUserId(), route);
            }
        }
    }

    private String resolveDriverDispatchStage(NxDisDriverRouteEntity route,
                                              SandboxComputeResult compute,
                                              DriverDispatchCandidateDto candidate) {
        if (compute != null) {
            return DisRouteSandboxManualDispatchPanoramaHelper.resolveDispatchStage(
                    route, compute, candidate.getDriverUserId());
        }
        if (route != null && DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            return ManualDispatchDispatchStage.EXECUTION;
        }
        if (route != null && DisRouteLoadingGateHelper.isRouteEnteredLoading(route)) {
            return ManualDispatchDispatchStage.LOADING;
        }
        if ("IN_DELIVERY".equals(candidate.getIneligibleReason())) {
            return ManualDispatchDispatchStage.EXECUTION;
        }
        if (candidate.getConfirmedStopCount() != null && candidate.getConfirmedStopCount() > 0) {
            return ManualDispatchDispatchStage.CONFIRMED;
        }
        return ManualDispatchDispatchStage.IDLE;
    }

    private DriverStopCounts resolveDriverStopCounts(SandboxComputeResult compute,
                                                     NxDisDriverRouteEntity route,
                                                     DriverDispatchCandidateDto candidate) {
        if (compute != null) {
            return DisRouteSandboxManualDispatchPanoramaHelper.resolveStopCounts(
                    compute, route, candidate.getDriverUserId());
        }
        int total = candidate.getConfirmedStopCount() != null ? candidate.getConfirmedStopCount() : 0;
        return new DriverStopCounts(total, 0, total);
    }

    private String buildDutyCardOperationHint(String dispatchBatch,
                                              DriverDispatchCandidateDto candidate,
                                              String dispatchStage,
                                              DriverStopCounts stopCounts) {
        if (!ON_DUTY.equals(candidate.getDutyStatus())) {
            return "开启后可参与今日派车";
        }
        if (ManualDispatchDispatchStage.COMPLETED.equals(dispatchStage)) {
            return "今日路线已完成，仍在岗可再派；不需要再派可关闭可派";
        }
        if (stopCounts != null && stopCounts.getPendingStopCount() > 0) {
            return "当前路线待送 " + stopCounts.getPendingStopCount() + " 站";
        }
        return candidate.getOperationHint();
    }

    private void applyDutyToggleFields(DriverDispatchCandidateDto dto,
                                         Integer disId,
                                         String routeDate,
                                         String dispatchStage) {
        if (dto == null) {
            return;
        }
        if (!ON_DUTY.equals(dto.getDutyStatus())) {
            dto.setCanToggleDutyOff(Boolean.FALSE);
            return;
        }
        if (dispatchStage != null && !DisRouteDriverDutyToggleHelper.canCloseDuty(dispatchStage)) {
            String reason = DisRouteDriverDutyToggleHelper.resolveToggleDisabledReason(dispatchStage);
            dto.setCanToggleDutyOff(Boolean.FALSE);
            dto.setDutyOffLockReason(reason);
            dto.setToggleDutyOffAction(DriverDutyPrimaryActionMaps.disabledToggleDutyOff("不可关闭", reason));
            return;
        }
        String lockReason = DisRouteDriverDutyLockHelper.resolveDutyLockReason(
                disId,
                routeDate,
                dto.getDriverUserId(),
                nxDisDriverRouteDao,
                nxDisRoutePlanDao,
                nxDisShipmentTaskDao);
        if (lockReason != null && !lockReason.trim().isEmpty()) {
            dto.setCanToggleDutyOff(Boolean.FALSE);
            dto.setDutyOffLockReason(lockReason.trim());
            dto.setToggleDutyOffAction(DriverDutyPrimaryActionMaps.disabledToggleDutyOff(
                    "不可关闭", lockReason.trim()));
            return;
        }
        dto.setCanToggleDutyOff(Boolean.TRUE);
        dto.setDutyOffLockReason(null);
        dto.setToggleDutyOffAction(DriverDutyPrimaryActionMaps.enabledToggleDutyOff(
                "关闭可派",
                DriverDutyPrimaryActionMaps.buildToggleDutyOffPayload(
                        disId, routeDate, dto.getDriverUserId(), null)));
    }

    private String buildOperationHint(String dispatchBatch, DriverDispatchCandidateDto dto) {
        String batchLabel = DisRouteDispatchLabels.label(dispatchBatch);
        if (Boolean.TRUE.equals(dto.getBatchEligible())) {
            if (dto.getConfirmedStopCount() != null && dto.getConfirmedStopCount() > 0) {
                return "可参与" + batchLabel + "派车，已确认 " + dto.getConfirmedStopCount() + " 站";
            }
            if (dto.getSandboxSuggestedStopCount() != null && dto.getSandboxSuggestedStopCount() > 0) {
                return "可参与" + batchLabel + "派车，沙盘建议 " + dto.getSandboxSuggestedStopCount() + " 站";
            }
            if (DisRouteFeasibilityStatus.IDLE.equals(dto.getCurrentFeasibilityStatus())) {
                return "可参与" + batchLabel + "派车（当前路线暂无站）";
            }
            return "可参与" + batchLabel + "派车";
        }
        if (DisRouteBatchEligibility.INELIGIBLE_OFF_DUTY.equals(dto.getIneligibleReason())) {
            return "请在司机可派状态中开启";
        }
        return "不可参与" + batchLabel + "派车";
    }

    private DriverDispatchListSummaryDto buildSummary(List<DriverDispatchCandidateDto> candidates) {
        DriverDispatchListSummaryDto summary = new DriverDispatchListSummaryDto();
        summary.setTotalDriverCount(candidates.size());
        int onDuty = 0;
        int eligible = 0;
        int idle = 0;
        int assignedRoute = 0;
        for (DriverDispatchCandidateDto dto : candidates) {
            if (ON_DUTY.equals(dto.getDutyStatus())) {
                onDuty++;
            }
            if (Boolean.TRUE.equals(dto.getBatchEligible())) {
                eligible++;
            }
            if (DisRouteFeasibilityStatus.IDLE.equals(dto.getCurrentFeasibilityStatus())
                    || (dto.getCurrentRouteId() != null
                    && (dto.getCurrentStopCount() == null || dto.getCurrentStopCount() == 0))) {
                idle++;
            }
            if (dto.getCurrentStopCount() != null && dto.getCurrentStopCount() > 0) {
                assignedRoute++;
            }
        }
        summary.setOnDutyCount(onDuty);
        summary.setEligibleCount(eligible);
        summary.setIneligibleCount(candidates.size() - eligible);
        summary.setIdleCount(idle);
        summary.setAssignedRouteDriverCount(assignedRoute);
        summary.setSummaryLine(buildDutySummaryLine(candidates.size(), onDuty, eligible));
        return summary;
    }

    private static String buildDutySummaryLine(int totalDriverCount, int onDutyCount, int eligibleCount) {
        if (totalDriverCount <= 0) {
            return "暂无司机账号";
        }
        if (onDutyCount <= 0) {
            return "共 " + totalDriverCount + " 名司机，暂无人开启可派";
        }
        if (eligibleCount > 0 && eligibleCount != onDutyCount) {
            return "共 " + totalDriverCount + " 名司机，" + onDutyCount + " 名已开启可派 · "
                    + eligibleCount + " 名可参与当前批次";
        }
        return "共 " + totalDriverCount + " 名司机，" + onDutyCount + " 名已开启可派";
    }

    private void sortCandidates(List<DriverDispatchCandidateDto> candidates) {
        Collections.sort(candidates, new Comparator<DriverDispatchCandidateDto>() {
            @Override
            public int compare(DriverDispatchCandidateDto a, DriverDispatchCandidateDto b) {
                int groupA = sortGroup(a);
                int groupB = sortGroup(b);
                if (groupA != groupB) {
                    return Integer.compare(groupA, groupB);
                }
                String nameA = a.getDriverName() != null ? a.getDriverName() : "";
                String nameB = b.getDriverName() != null ? b.getDriverName() : "";
                int nameCompare = nameA.compareTo(nameB);
                if (nameCompare != 0) {
                    return nameCompare;
                }
                return Integer.compare(
                        a.getDriverUserId() != null ? a.getDriverUserId() : 0,
                        b.getDriverUserId() != null ? b.getDriverUserId() : 0);
            }

            private int sortGroup(DriverDispatchCandidateDto dto) {
                if (Boolean.TRUE.equals(dto.getBatchEligible())) {
                    return 0;
                }
                if (ON_DUTY.equals(dto.getDutyStatus())) {
                    return 1;
                }
                return 2;
            }
        });
    }

    private Map<Integer, Integer> resolveValidStopCountByDriver(NxDisRoutePlanEntity planHeader, String routeDate) {
        Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
        if (planHeader == null || planHeader.getNxDrpId() == null) {
            return counts;
        }
        NxDisRoutePlanEntity plan = disRouteDispatchService.getPlan(planHeader.getNxDrpId());
        if (plan == null) {
            return counts;
        }
        List<com.nongxinle.entity.NxDisShipmentTaskEntity> tasks =
                disShipmentTaskService.queryTasksByPlanId(planHeader.getNxDrpId());
        disRouteDispatchReadIntegrityHelper.apply(plan, tasks, routeDate);
        if (plan.getDriverRoutes() == null) {
            return counts;
        }
        for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
            if (route != null && route.getNxDdrDriverUserId() != null) {
                int stopCount = route.getStops() != null ? route.getStops().size() : 0;
                counts.put(route.getNxDdrDriverUserId(), stopCount);
            }
        }
        return counts;
    }

    private Map<Integer, NxDisDriverRouteEntity> loadRoutesByDriver(NxDisRoutePlanEntity planHeader) {
        Map<Integer, NxDisDriverRouteEntity> routeByDriver = new HashMap<Integer, NxDisDriverRouteEntity>();
        if (planHeader == null || planHeader.getNxDrpId() == null) {
            return routeByDriver;
        }
        List<NxDisDriverRouteEntity> routes = nxDisDriverRouteDao.queryByPlanId(planHeader.getNxDrpId());
        if (routes == null) {
            return routeByDriver;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route.getNxDdrDriverUserId() != null) {
                routeByDriver.put(route.getNxDdrDriverUserId(), route);
            }
        }
        return routeByDriver;
    }

    private NxDisRoutePlanEntity findPlanHeader(Integer disId, String routeDate, String dispatchBatch) {
        for (String status : new String[]{ASSIGNED, READY}) {
            NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                    disId, routeDate, dispatchBatch, status);
            if (plan != null) {
                return plan;
            }
        }
        return findExecutionPlanHeader(disId, routeDate, dispatchBatch);
    }

    /** 与 sandbox compute 一致：出发后 plan 可能非 ASSIGNED/READY 头查，按 execution route 找回。 */
    private NxDisRoutePlanEntity findExecutionPlanHeader(Integer disId, String routeDate, String dispatchBatch) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("disId", disId);
        params.put("routeDate", routeDate);
        List<NxDisRoutePlanEntity> plans = nxDisRoutePlanDao.queryList(params);
        if (plans == null || plans.isEmpty()) {
            return null;
        }
        NxDisRoutePlanEntity batchMatch = null;
        NxDisRoutePlanEntity anyActive = null;
        for (NxDisRoutePlanEntity plan : plans) {
            if (plan == null || DisRoutePlanStatus.CANCELLED.equals(plan.getNxDrpStatus())) {
                continue;
            }
            if (plan.getNxDrpId() == null) {
                continue;
            }
            List<NxDisDriverRouteEntity> routes = nxDisDriverRouteDao.queryByPlanId(plan.getNxDrpId());
            if (!DisRouteExecutionPlanHelper.hasExecutionDriverRoute(routes)
                    && !hasConfirmedTasksOnPlan(plan.getNxDrpId())) {
                continue;
            }
            if (dispatchBatch.equals(normalizeBatchCode(plan.getNxDrpDispatchBatch()))) {
                batchMatch = plan;
                break;
            }
            if (anyActive == null) {
                anyActive = plan;
            }
        }
        return batchMatch != null ? batchMatch : anyActive;
    }

    private boolean hasConfirmedTasksOnPlan(Integer planId) {
        if (planId == null) {
            return false;
        }
        List<com.nongxinle.entity.NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByPlanId(planId);
        return DisRouteExecutionPlanHelper.hasConfirmedOrExecutionTasks(tasks);
    }

    private DisRouteBatchContext resolveBatchContext(String routeDate,
                                                     String dispatchBatch,
                                                     NxDisRoutePlanEntity planHeader) {
        if (planHeader != null) {
            if (DisRouteBatchDefaults.needsBatchPersist(planHeader)) {
                return DisRouteBatchDefaults.resolveForRouteDate(routeDate, dispatchBatch);
            }
            return DisRouteBatchDefaults.fromPlan(planHeader);
        }
        if (DisRouteDispatchBatch.ADHOC.equals(dispatchBatch)) {
            throw new IllegalArgumentException("ADHOC 批次需先有当日路线计划后再查询司机可派列表");
        }
        return DisRouteBatchDefaults.resolveForRouteDate(routeDate, dispatchBatch);
    }

    private void applyDepartedDriverOverlay(DriverDispatchCandidateDto dto, NxDisDriverRouteEntity route) {
        if (dto == null || route == null || !DisRouteRouteExecutionHelper.isExecutionRoute(route)) {
            return;
        }
        dto.setBatchEligible(false);
        dto.setBatchEligibleLabel("配送中");
        dto.setIneligibleReason("IN_DELIVERY");
        dto.setIneligibleReasonLabel("配送中");
        String routeStatus = DisRouteRouteExecutionHelper.resolveRouteStatus(route);
        dto.setRouteStatus(routeStatus);
        dto.setRouteStatusLabel(DisRouteDispatchLabels.label(routeStatus));
        dto.setDriverRouteId(route.getNxDdrId());
        dto.setCanDepart(false);
        dto.setDepartBlockedReason("该司机路线已在配送中或已完成");
        if (route.getNxDdrActualDepartAt() != null) {
            String formatted = RouteDispatchDateFormat.format(route.getNxDdrActualDepartAt());
            dto.setActualDepartAt(formatted);
            dto.setDepartedAt(formatted);
        }
    }

    private String resolveDriverName(NxDistributerUserEntity driver) {
        if (driver.getNxDiuWxNickName() != null && !driver.getNxDiuWxNickName().trim().isEmpty()) {
            return driver.getNxDiuWxNickName().trim();
        }
        return String.valueOf(driver.getNxDistributerUserId());
    }

    private String resolveRouteDate(String routeDate) {
        if (routeDate != null && !routeDate.trim().isEmpty()) {
            return routeDate.trim();
        }
        return formatWhatDay(0);
    }

    private String normalizeBatchCode(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return DisRouteDispatchBatch.MORNING;
        }
        return batchCode.trim().toUpperCase();
    }
}
