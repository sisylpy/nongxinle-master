package com.nongxinle.todaydispatch;

import com.nongxinle.dao.NxDisDriverDutyDao;
import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.DispatchDriverCardDto;
import com.nongxinle.dto.route.DriverDispatchCandidateDto;
import com.nongxinle.dto.route.DriverDispatchListResponse;
import com.nongxinle.dto.route.DriverDispatchListSummaryDto;
import com.nongxinle.entity.NxDisDriverDutyEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.route.DisRouteBatchEligibility;
import com.nongxinle.route.DisRouteDispatchLabels;
import com.nongxinle.route.DisRouteFeasibilityStatus;
import com.nongxinle.route.DriverDutyPrimaryActionMaps;
import com.nongxinle.service.DisRouteDispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisDriverDutyStatus.OFF_DUTY;
import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;
import static com.nongxinle.route.DisRouteDispatchBatch.MORNING;
import static com.nongxinle.route.DisRoutePlanStatus.ASSIGNED;
import static com.nongxinle.route.DisRoutePlanStatus.READY;
import static com.nongxinle.route.DisShipmentTaskStatus.CANCELLED;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

/** 司机可派列表 — 直读 DB，不跑 sandbox compute。 */
@Service("todayDispatchDriverListService")
public class TodayDispatchDriverListService {

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
    private TodayDispatchDutyLockHelper todayDispatchDutyLockHelper;
    @Autowired
    private DriverDispatchStateService driverDispatchStateService;

    public DriverDispatchListResponse listDriversForBatch(Integer disId,
                                                          String routeDate,
                                                          String batchCode) {
        return listDriversForBatch(disId, routeDate, batchCode, true);
    }

    public DriverDispatchListResponse listDriversForBatch(Integer disId,
                                                          String routeDate,
                                                          String batchCode,
                                                          boolean includeDutyCards) {
        if (disId == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        String queryRouteDate = resolveRouteDate(routeDate);
        String dispatchBatch = normalizeBatch(batchCode);

        Map<Integer, NxDisDriverDutyEntity> dutyByDriver = loadDutyByDriver(disId, queryRouteDate);
        NxDisRoutePlanEntity planHeader = findPlanHeader(disId, queryRouteDate, dispatchBatch);
        Map<Integer, NxDisDriverRouteEntity> routeByDriver = loadRoutesByDriver(planHeader);
        Map<Integer, Integer> stopCountByDriver = countStopsByDriver(planHeader);
        DriverDispatchStateService.StateQuery eligibilityQuery = driverDispatchStateService.forCompute(
                disId,
                queryRouteDate,
                dispatchBatch,
                planHeader != null ? planHeader.getNxDrpId() : null);

        List<NxDistributerUserEntity> driverAccounts = disRouteDispatchService.listDrivers(disId);
        List<DriverDispatchCandidateDto> candidates = new ArrayList<DriverDispatchCandidateDto>();
        for (NxDistributerUserEntity driver : driverAccounts) {
            candidates.add(buildCandidate(
                    driver, dispatchBatch, dutyByDriver, routeByDriver, stopCountByDriver, eligibilityQuery));
        }
        sortCandidates(candidates);

        DriverDispatchListResponse response = new DriverDispatchListResponse();
        response.setRouteDate(queryRouteDate);
        response.setDispatchBatch(dispatchBatch);
        response.setDispatchBatchLabel(DisRouteDispatchLabels.label(dispatchBatch));
        response.setSummary(buildSummary(candidates));
        response.setDrivers(candidates);
        if (includeDutyCards) {
            response.setDriverCards(buildDriverDutyCards(candidates, disId, queryRouteDate));
        }
        return response;
    }

    private List<DispatchDriverCardDto> buildDriverDutyCards(List<DriverDispatchCandidateDto> candidates,
                                                             Integer disId,
                                                             String routeDate) {
        List<DispatchDriverCardDto> cards = new ArrayList<DispatchDriverCardDto>();
        for (DriverDispatchCandidateDto candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            boolean onDuty = ON_DUTY.equals(candidate.getDutyStatus());
            Map<String, Object> toggleAction;
            boolean canToggleDuty;
            String toggleDisabledReason = null;
            if (!onDuty) {
                toggleAction = DriverDutyPrimaryActionMaps.enabledToggleDutyOn(
                        "开启可派",
                        DriverDutyPrimaryActionMaps.buildToggleDutyOffPayload(
                                disId, routeDate, candidate.getDriverUserId(), null));
                canToggleDuty = true;
            } else {
                String lockReason = todayDispatchDutyLockHelper.resolveDutyLockReason(
                        disId, routeDate, candidate.getDriverUserId());
                if (lockReason != null) {
                    canToggleDuty = false;
                    toggleDisabledReason = lockReason;
                    toggleAction = DriverDutyPrimaryActionMaps.disabledToggleDutyOff("不可关闭", lockReason);
                } else {
                    canToggleDuty = true;
                    toggleAction = DriverDutyPrimaryActionMaps.enabledToggleDutyOff(
                            "关闭可派",
                            DriverDutyPrimaryActionMaps.buildToggleDutyOffPayload(
                                    disId, routeDate, candidate.getDriverUserId(), null));
                }
            }
            DispatchDriverCardDto card = new DispatchDriverCardDto();
            card.setDriverUserId(candidate.getDriverUserId());
            card.setDriverName(candidate.getDriverName());
            card.setDutyStatus(candidate.getDutyStatus());
            card.setDutyStatusLabel(candidate.getDutyStatusLabel());
            card.setCurrentStopCount(candidate.getConfirmedStopCount());
            card.setCustomerCount(candidate.getConfirmedStopCount());
            card.setCanToggleDuty(canToggleDuty);
            card.setToggleDisabledReason(toggleDisabledReason);
            card.setPrimaryAction(toggleAction);
            card.setOperationHint(candidate.getOperationHint());
            cards.add(card);
        }
        return cards;
    }

    private DriverDispatchCandidateDto buildCandidate(NxDistributerUserEntity driver,
                                                      String dispatchBatch,
                                                      Map<Integer, NxDisDriverDutyEntity> dutyByDriver,
                                                      Map<Integer, NxDisDriverRouteEntity> routeByDriver,
                                                      Map<Integer, Integer> stopCountByDriver,
                                                      DriverDispatchStateService.StateQuery eligibilityQuery) {
        DriverDispatchCandidateDto dto = new DriverDispatchCandidateDto();
        dto.setDriverUserId(driver.getNxDistributerUserId());
        dto.setDriverName(resolveDriverName(driver));
        dto.setDriverPhone(driver.getNxDiuWxPhone());
        dto.setDispatchBatch(dispatchBatch);
        dto.setDispatchBatchLabel(DisRouteDispatchLabels.label(dispatchBatch));

        NxDisDriverDutyEntity duty = dutyByDriver.get(driver.getNxDistributerUserId());
        boolean onDuty = duty != null && ON_DUTY.equals(duty.getNxDddDutyStatus());
        dto.setDutyStatus(onDuty ? ON_DUTY : OFF_DUTY);
        dto.setDutyStatusLabel(DisRouteDispatchLabels.label(dto.getDutyStatus()));
        DriverDispatchStateService.Eligibility eligibility = driverDispatchStateService.eligibilityFor(
                driver.getNxDistributerUserId(), eligibilityQuery);
        boolean batchEligible = onDuty && eligibility.isCanReceiveNewStops();
        dto.setBatchEligible(batchEligible);
        dto.setBatchEligibleLabel(batchEligible ? "可参与当前批次" : "不参与派车");
        if (!onDuty) {
            dto.setIneligibleReason(DisRouteBatchEligibility.INELIGIBLE_OFF_DUTY);
            dto.setIneligibleReasonLabel("不可派");
            dto.setOperationHint("开启后可参与今日派车");
            dto.setCanToggleDutyOff(Boolean.FALSE);
            return dto;
        }
        if (!batchEligible) {
            dto.setIneligibleReason(eligibility.getIneligibleReason());
            dto.setIneligibleReasonLabel(eligibility.getIneligibleReasonLabel());
            dto.setOperationHint(resolveIneligibleOperationHint(eligibility, dispatchBatch));
            dto.setCanToggleDutyOff(Boolean.FALSE);
            return dto;
        }

        Integer stopCount = stopCountByDriver.get(driver.getNxDistributerUserId());
        if (stopCount == null) {
            stopCount = 0;
        }
        dto.setConfirmedStopCount(stopCount);
        dto.setCurrentStopCount(stopCount);
        NxDisDriverRouteEntity route = routeByDriver.get(driver.getNxDistributerUserId());
        if (route != null) {
            dto.setCurrentRouteId(route.getNxDdrId());
        }
        dto.setCurrentFeasibilityStatus(stopCount > 0
                ? DisRouteFeasibilityStatus.FEASIBLE : DisRouteFeasibilityStatus.IDLE);
        dto.setOperationHint(stopCount > 0
                ? "已确认 " + stopCount + " 站" : resolveBatchDispatchHint(dispatchBatch));
        return dto;
    }

    private Map<Integer, Integer> countStopsByDriver(NxDisRoutePlanEntity planHeader) {
        Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
        if (planHeader == null || planHeader.getNxDrpId() == null) {
            return counts;
        }
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByPlanId(planHeader.getNxDrpId());
        if (tasks == null) {
            return counts;
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task == null || CANCELLED.equals(task.getNxDstStatus())
                    || task.getNxDstAssignedDriverUserId() == null) {
                continue;
            }
            Integer driverId = task.getNxDstAssignedDriverUserId();
            counts.put(driverId, counts.containsKey(driverId) ? counts.get(driverId) + 1 : 1);
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
            if (route != null && route.getNxDdrDriverUserId() != null) {
                routeByDriver.put(route.getNxDdrDriverUserId(), route);
            }
        }
        return routeByDriver;
    }

    private Map<Integer, NxDisDriverDutyEntity> loadDutyByDriver(Integer disId, String routeDate) {
        Map<Integer, NxDisDriverDutyEntity> dutyByDriver = new HashMap<Integer, NxDisDriverDutyEntity>();
        List<NxDisDriverDutyEntity> duties = nxDisDriverDutyDao.queryByDisDate(disId, routeDate);
        if (duties == null) {
            return dutyByDriver;
        }
        for (NxDisDriverDutyEntity duty : duties) {
            if (duty != null && duty.getNxDddDriverUserId() != null) {
                dutyByDriver.put(duty.getNxDddDriverUserId(), duty);
            }
        }
        return dutyByDriver;
    }

    private NxDisRoutePlanEntity findPlanHeader(Integer disId, String routeDate, String batchCode) {
        for (String status : new String[]{ASSIGNED, READY}) {
            NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                    disId, routeDate, batchCode, status);
            if (plan != null) {
                return plan;
            }
        }
        return null;
    }

    private static DriverDispatchListSummaryDto buildSummary(List<DriverDispatchCandidateDto> candidates) {
        DriverDispatchListSummaryDto summary = new DriverDispatchListSummaryDto();
        summary.setTotalDriverCount(candidates.size());
        int onDuty = 0;
        int eligible = 0;
        for (DriverDispatchCandidateDto dto : candidates) {
            if (ON_DUTY.equals(dto.getDutyStatus())) {
                onDuty++;
            }
            if (Boolean.TRUE.equals(dto.getBatchEligible())) {
                eligible++;
            }
        }
        summary.setOnDutyCount(onDuty);
        summary.setEligibleCount(eligible);
        summary.setIneligibleCount(candidates.size() - eligible);
        summary.setSummaryLine("共 " + candidates.size() + " 名司机，" + onDuty + " 名已开启可派");
        return summary;
    }

    private static void sortCandidates(List<DriverDispatchCandidateDto> candidates) {
        Collections.sort(candidates, new Comparator<DriverDispatchCandidateDto>() {
            @Override
            public int compare(DriverDispatchCandidateDto a, DriverDispatchCandidateDto b) {
                return Integer.compare(
                        a.getDriverUserId() != null ? a.getDriverUserId() : 0,
                        b.getDriverUserId() != null ? b.getDriverUserId() : 0);
            }
        });
    }

    private static String resolveIneligibleOperationHint(DriverDispatchStateService.Eligibility eligibility,
                                                         String dispatchBatch) {
        if (eligibility != null && eligibility.getIneligibleReasonLabel() != null
                && !eligibility.getIneligibleReasonLabel().trim().isEmpty()) {
            return eligibility.getIneligibleReasonLabel().trim() + "，暂不可参与新派单";
        }
        return "暂不可参与" + resolveBatchLabel(dispatchBatch) + "派车";
    }

    private static String resolveBatchDispatchHint(String dispatchBatch) {
        return "可参与" + resolveBatchLabel(dispatchBatch) + "派车";
    }

    private static String resolveBatchLabel(String dispatchBatch) {
        String label = DisRouteDispatchLabels.label(dispatchBatch);
        return label != null && !label.trim().isEmpty() ? label : "今日";
    }

    private static String resolveDriverName(NxDistributerUserEntity driver) {
        if (driver.getNxDiuWxNickName() != null && !driver.getNxDiuWxNickName().trim().isEmpty()) {
            return driver.getNxDiuWxNickName().trim();
        }
        return String.valueOf(driver.getNxDistributerUserId());
    }

    private static String resolveRouteDate(String routeDate) {
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
