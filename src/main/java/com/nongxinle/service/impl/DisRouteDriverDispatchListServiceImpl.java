package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverDutyDao;
import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dto.route.DisRouteBatchContext;
import com.nongxinle.dto.route.DriverDispatchCandidateDto;
import com.nongxinle.dto.route.DriverDispatchListResponse;
import com.nongxinle.dto.route.DriverDispatchListSummaryDto;
import com.nongxinle.entity.NxDisDriverDutyEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.route.*;
import com.nongxinle.service.DisRouteDispatchService;
import com.nongxinle.service.DisRouteDriverDispatchListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisDriverDutyStatus.OFF_DUTY;
import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;
import static com.nongxinle.route.DisRoutePlanStatus.ASSIGNED;
import static com.nongxinle.route.DisRoutePlanStatus.READY;
import static com.nongxinle.route.DisRoutePlanStatus.SIMULATED;
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
    private NxDisDriverDutyDao nxDisDriverDutyDao;

    @Override
    public DriverDispatchListResponse listDriversForBatch(Integer disId, String routeDate, String batchCode) {
        if (disId == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        String queryRouteDate = resolveRouteDate(routeDate);
        String dispatchBatch = normalizeBatchCode(batchCode);

        NxDisRoutePlanEntity planHeader = findPlanHeader(disId, queryRouteDate, dispatchBatch);
        DisRouteBatchContext batchContext = resolveBatchContext(queryRouteDate, dispatchBatch, planHeader);
        Date latestAllowedCheckInAt = DisRouteBatchDefaults.latestAllowedCheckInAt(batchContext.getDefaultDepartAt());
        String latestAllowedCheckInAtStr = RouteDispatchDateFormat.format(latestAllowedCheckInAt);

        Map<Integer, NxDisDriverRouteEntity> routeByDriver = loadRoutesByDriver(planHeader);

        List<NxDistributerUserEntity> driverAccounts = disRouteDispatchService.listDrivers(disId);
        List<DriverDispatchCandidateDto> candidates = new ArrayList<DriverDispatchCandidateDto>();
        for (NxDistributerUserEntity driver : driverAccounts) {
            candidates.add(buildCandidate(driver, disId, queryRouteDate, dispatchBatch,
                    latestAllowedCheckInAt, latestAllowedCheckInAtStr, routeByDriver));
        }
        sortCandidates(candidates);

        DriverDispatchListResponse response = new DriverDispatchListResponse();
        response.setRouteDate(queryRouteDate);
        response.setDispatchBatch(dispatchBatch);
        response.setDispatchBatchLabel(DisRouteDispatchLabels.label(dispatchBatch));
        response.setLatestAllowedCheckInAt(latestAllowedCheckInAtStr);
        response.setSummary(buildSummary(candidates));
        response.setDrivers(candidates);
        return response;
    }

    private DriverDispatchCandidateDto buildCandidate(NxDistributerUserEntity driver,
                                                      Integer disId,
                                                      String routeDate,
                                                      String dispatchBatch,
                                                      Date latestAllowedCheckInAt,
                                                      String latestAllowedCheckInAtStr,
                                                      Map<Integer, NxDisDriverRouteEntity> routeByDriver) {
        DriverDispatchCandidateDto dto = new DriverDispatchCandidateDto();
        dto.setDriverUserId(driver.getNxDistributerUserId());
        dto.setDriverName(resolveDriverName(driver));
        dto.setDriverPhone(driver.getNxDiuWxPhone());
        dto.setDispatchBatch(dispatchBatch);
        dto.setDispatchBatchLabel(DisRouteDispatchLabels.label(dispatchBatch));
        dto.setLatestAllowedCheckInAt(latestAllowedCheckInAtStr);

        NxDisDriverDutyEntity duty = nxDisDriverDutyDao.queryByDisDriverDate(
                disId, driver.getNxDistributerUserId(), routeDate);
        boolean onDuty = duty != null && ON_DUTY.equals(duty.getNxDddDutyStatus());
        dto.setDutyStatus(onDuty ? ON_DUTY : OFF_DUTY);
        dto.setDutyStatusLabel(DisRouteDispatchLabels.label(dto.getDutyStatus()));
        dto.setCheckInAt(RouteDispatchDateFormat.format(duty != null ? duty.getNxDddCheckInAt() : null));
        dto.setCheckOutAt(RouteDispatchDateFormat.format(duty != null ? duty.getNxDddCheckOutAt() : null));

        String ineligibleReason = resolveIneligibleReason(onDuty, duty, latestAllowedCheckInAt);
        boolean batchEligible = ineligibleReason == null;
        dto.setBatchEligible(batchEligible);
        dto.setBatchEligibleLabel(batchEligible ? "可派" : "不可派");
        dto.setIneligibleReason(ineligibleReason);
        dto.setIneligibleReasonLabel(ineligibleReason != null
                ? DisRouteDispatchLabels.label(ineligibleReason) : null);

        NxDisDriverRouteEntity route = routeByDriver.get(driver.getNxDistributerUserId());
        if (route != null) {
            dto.setCurrentRouteId(route.getNxDdrId());
            dto.setCurrentStopCount(route.getNxDdrStopCount());
            dto.setCurrentLateMinutes(route.getNxDdrTotalLateMinutes());
            dto.setCurrentWaitMinutes(route.getNxDdrTotalWaitMinutes());
            dto.setCurrentFeasibilityStatus(route.getNxDdrFeasibilityStatus());
            dto.setCurrentFeasibilityStatusLabel(DisRouteDispatchLabels.label(route.getNxDdrFeasibilityStatus()));
        } else {
            dto.setCurrentStopCount(0);
            dto.setCurrentLateMinutes(0);
            dto.setCurrentWaitMinutes(0);
        }

        dto.setOperationHint(buildOperationHint(dispatchBatch, dto));
        return dto;
    }

    private String resolveIneligibleReason(boolean onDuty,
                                           NxDisDriverDutyEntity duty,
                                           Date latestAllowedCheckInAt) {
        if (!onDuty) {
            return DisRouteBatchEligibility.INELIGIBLE_OFF_DUTY;
        }
        Date checkInAt = duty != null ? duty.getNxDddCheckInAt() : null;
        if (latestAllowedCheckInAt != null && checkInAt != null && checkInAt.after(latestAllowedCheckInAt)) {
            return DisRouteBatchEligibility.INELIGIBLE_CHECKIN_TOO_LATE;
        }
        return null;
    }

    private String buildOperationHint(String dispatchBatch, DriverDispatchCandidateDto dto) {
        String batchLabel = DisRouteDispatchLabels.label(dispatchBatch);
        if (Boolean.TRUE.equals(dto.getBatchEligible())) {
            if (dto.getCurrentStopCount() != null && dto.getCurrentStopCount() > 0) {
                return "可参与" + batchLabel + "派车，当前已有 " + dto.getCurrentStopCount() + " 站";
            }
            if (DisRouteFeasibilityStatus.IDLE.equals(dto.getCurrentFeasibilityStatus())) {
                return "可参与" + batchLabel + "派车（当前路线暂无站）";
            }
            return "可参与" + batchLabel + "派车";
        }
        if (DisRouteBatchEligibility.INELIGIBLE_OFF_DUTY.equals(dto.getIneligibleReason())) {
            return "请先上岗";
        }
        if (DisRouteBatchEligibility.INELIGIBLE_CHECKIN_TOO_LATE.equals(dto.getIneligibleReason())) {
            if (DisRouteDispatchBatch.MORNING.equals(dispatchBatch)) {
                return "上岗过晚，建议切换下午班";
            }
            if (DisRouteDispatchBatch.AFTERNOON.equals(dispatchBatch)) {
                return "上岗过晚，不适合当前批次";
            }
            return "上岗过晚，不适合当前批次";
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
        return summary;
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
        for (String status : new String[]{ASSIGNED, SIMULATED, READY}) {
            NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                    disId, routeDate, dispatchBatch, status);
            if (plan != null) {
                return plan;
            }
        }
        return null;
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
