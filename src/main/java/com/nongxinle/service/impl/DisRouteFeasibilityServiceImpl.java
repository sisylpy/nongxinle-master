package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverDutyDao;
import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dto.route.RouteDispatchWarning;
import com.nongxinle.dto.route.RouteFeasibilityResult;
import com.nongxinle.entity.NxDisDriverDutyEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.*;
import com.nongxinle.service.DisRouteFeasibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;

@Service
public class DisRouteFeasibilityServiceImpl implements DisRouteFeasibilityService {

    private static final String STOP_CANCELLED = "CANCELLED";
    private static final String SEVERITY_ERROR = "ERROR";
    private static final String SEVERITY_WARN = "WARN";
    private static final String SEVERITY_INFO = "INFO";
    private static final int OVERLOAD_STOP_THRESHOLD = 3;

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisDriverDutyDao nxDisDriverDutyDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private DisRoutePlanBatchHelper disRoutePlanBatchHelper;

    @Override
    @Transactional
    public RouteFeasibilityResult assess(Integer planId) {
        disRoutePlanBatchHelper.ensureBatchPersisted(planId);
        return evaluate(planId, true);
    }

    private RouteFeasibilityResult evaluate(Integer planId, boolean persist) {
        NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryObject(planId);
        if (plan == null) {
            throw new IllegalArgumentException("路线计划不存在");
        }

        String routeDate = resolveRouteDate(plan);

        List<NxDisDriverRouteEntity> driverRoutes = nxDisDriverRouteDao.queryByPlanId(planId);
        Map<Integer, NxDisShipmentTaskEntity> taskById = loadTaskMap(planId);

        List<RouteDispatchWarning> warnings = new ArrayList<RouteDispatchWarning>();
        boolean anyWait = false;
        boolean anyLate = false;
        boolean anyInfeasibleLocked = false;
        int eligibleDriverCount = 0;
        int routesWithStops = 0;

        if (driverRoutes != null) {
            for (NxDisDriverRouteEntity driverRoute : driverRoutes) {
                DisRouteDriverDutyOverlayHelper.sanitizeLegacyFields(driverRoute);
                RouteDriverAssessment assessment = assessDriverRoute(plan, driverRoute, routeDate, taskById);
                warnings.addAll(assessment.warnings);

                if (assessment.batchEligible) {
                    eligibleDriverCount++;
                }
                if (assessment.activeStopCount > 0) {
                    routesWithStops++;
                }
                if (assessment.anyWait) {
                    anyWait = true;
                }
                if (assessment.anyLate) {
                    anyLate = true;
                }
                if (assessment.infeasibleLocked) {
                    anyInfeasibleLocked = true;
                }

                if (persist) {
                    NxDisDriverRouteEntity routeUpdate = new NxDisDriverRouteEntity();
                    routeUpdate.setNxDdrId(driverRoute.getNxDdrId());
                    routeUpdate.setNxDdrDispatchEligible(assessment.batchEligible ? 1 : 0);
                    routeUpdate.setNxDdrIneligibleReason(assessment.ineligibleReason);
                    routeUpdate.setNxDdrFeasibilityStatus(assessment.routeFeasibilityStatus);
                    nxDisDriverRouteDao.updateFeasibility(routeUpdate);
                }
            }
        }

        if (eligibleDriverCount == 0) {
            warnings.add(buildWarning(
                    DisRouteWarningCode.NO_AVAILABLE_DRIVER,
                    SEVERITY_ERROR,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "本批次无可派司机",
                    "请在司机可派状态中开启司机"));
        }

        String planFeasibility = resolvePlanFeasibility(
                anyInfeasibleLocked,
                eligibleDriverCount,
                routesWithStops,
                anyLate,
                anyWait);

        if (persist) {
            NxDisRoutePlanEntity planUpdate = new NxDisRoutePlanEntity();
            planUpdate.setNxDrpId(planId);
            planUpdate.setNxDrpFeasibilityStatus(planFeasibility);
            nxDisRoutePlanDao.updateFeasibility(planUpdate);
        }

        RouteFeasibilityResult result = new RouteFeasibilityResult();
        result.setPlanId(planId);
        result.setFeasibilityStatus(planFeasibility);
        result.setWarnings(warnings);
        return result;
    }

    private RouteDriverAssessment assessDriverRoute(NxDisRoutePlanEntity plan,
                                                    NxDisDriverRouteEntity driverRoute,
                                                    String routeDate,
                                                    Map<Integer, NxDisShipmentTaskEntity> taskById) {
        RouteDriverAssessment assessment = new RouteDriverAssessment();
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDriverRouteId(driverRoute.getNxDdrId());

        NxDisDriverDutyEntity duty = nxDisDriverDutyDao.queryByDisDriverDate(
                plan.getNxDrpDistributerId(), driverRoute.getNxDdrDriverUserId(), routeDate);
        boolean onDuty = duty != null && ON_DUTY.equals(duty.getNxDddDutyStatus());

        assessment.batchEligible = onDuty;
        if (!onDuty) {
            assessment.ineligibleReason = DisRouteBatchEligibility.INELIGIBLE_OFF_DUTY;
        }

        int activeStopCount = 0;
        int routeLateMinutes = 0;
        if (tasks != null) {
            for (NxDisShipmentTaskEntity task : tasks) {
                if (!isCountableTask(task)) {
                    continue;
                }
                activeStopCount++;
                if (DisRouteStopTimeWindowStatus.EARLY_WAIT.equals(task.getNxDstTimeWindowStatus())) {
                    assessment.anyWait = true;
                }
                if (DisRouteStopTimeWindowStatus.LATE.equals(task.getNxDstTimeWindowStatus())) {
                    assessment.anyLate = true;
                    Integer lateMinutes = task.getNxDstLateMinutes() != null ? task.getNxDstLateMinutes() : 0;
                    routeLateMinutes += lateMinutes;
                    assessment.warnings.add(buildTaskLateWarning(
                            driverRoute, task, lateMinutes, assessment.batchEligible));
                }

                if (isTaskProtected(task) && !assessment.batchEligible) {
                    assessment.infeasibleLocked = true;
                    assessment.warnings.add(buildLockedIneligibleWarning(driverRoute, task));
                }
            }
        }
        assessment.activeStopCount = activeStopCount;

        if (activeStopCount == 0) {
            assessment.routeFeasibilityStatus = DisRouteFeasibilityStatus.IDLE;
            assessment.warnings.add(buildWarning(
                    DisRouteWarningCode.IDLE_DRIVER,
                    SEVERITY_INFO,
                    driverRoute.getNxDdrDriverUserId(),
                    driverRoute.getDriverName(),
                    driverRoute.getNxDdrId(),
                    null,
                    null,
                    null,
                    null,
                    "司机路线暂无配送站",
                    null));
            return assessment;
        }

        if (!assessment.batchEligible) {
            assessment.routeFeasibilityStatus = DisRouteFeasibilityStatus.INFEASIBLE;
            return assessment;
        }

        if (activeStopCount >= OVERLOAD_STOP_THRESHOLD && routeLateMinutes > 0) {
            assessment.warnings.add(buildWarning(
                    DisRouteWarningCode.DRIVER_OVERLOAD,
                    SEVERITY_WARN,
                    driverRoute.getNxDdrDriverUserId(),
                    driverRoute.getDriverName(),
                    driverRoute.getNxDdrId(),
                    null,
                    null,
                    null,
                    routeLateMinutes,
                    "司机路线站数较多且存在迟到，累计迟到 " + routeLateMinutes + " 分钟",
                    "可考虑后续 reoptimize 或人工调整分配"));
        }

        if (assessment.anyLate && routeLateMinutes > 0) {
            assessment.warnings.add(buildSuggestEarlierDepartWarning(driverRoute, routeLateMinutes));
        }

        if (assessment.anyLate) {
            assessment.routeFeasibilityStatus = DisRouteFeasibilityStatus.HAS_LATE;
        } else if (assessment.anyWait) {
            assessment.routeFeasibilityStatus = DisRouteFeasibilityStatus.HAS_WAIT;
        } else {
            assessment.routeFeasibilityStatus = DisRouteFeasibilityStatus.FEASIBLE;
        }
        return assessment;
    }

    private String resolvePlanFeasibility(boolean anyInfeasibleLocked,
                                          int eligibleDriverCount,
                                          int routesWithStops,
                                          boolean anyLate,
                                          boolean anyWait) {
        if (anyInfeasibleLocked) {
            return DisRouteFeasibilityStatus.INFEASIBLE;
        }
        if (eligibleDriverCount == 0 && routesWithStops > 0) {
            return DisRouteFeasibilityStatus.NO_AVAILABLE_DRIVER;
        }
        if (eligibleDriverCount == 0) {
            return DisRouteFeasibilityStatus.NO_AVAILABLE_DRIVER;
        }
        if (anyLate) {
            return DisRouteFeasibilityStatus.HAS_LATE;
        }
        if (anyWait) {
            return DisRouteFeasibilityStatus.HAS_WAIT;
        }
        return DisRouteFeasibilityStatus.FEASIBLE;
    }

    private Map<Integer, NxDisShipmentTaskEntity> loadTaskMap(Integer planId) {
        Map<Integer, NxDisShipmentTaskEntity> map = new HashMap<Integer, NxDisShipmentTaskEntity>();
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByPlanId(planId);
        if (tasks == null) {
            return map;
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            map.put(task.getNxDstId(), task);
        }
        return map;
    }

    private boolean isTaskProtected(NxDisShipmentTaskEntity task) {
        if (task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1) {
            return true;
        }
        String status = task.getNxDstStatus();
        return DisShipmentTaskStatus.ASSIGNED.equals(status)
                || DisShipmentTaskStatus.READY_TO_GO.equals(status)
                || DisShipmentTaskStatus.IN_DELIVERY.equals(status)
                || DisShipmentTaskStatus.DELIVERED.equals(status);
    }

    private boolean isCountableTask(NxDisShipmentTaskEntity task) {
        if (task == null) {
            return false;
        }
        String status = task.getNxDstStopStatus();
        return status == null || !STOP_CANCELLED.equalsIgnoreCase(status);
    }

    private RouteDispatchWarning buildTaskLateWarning(NxDisDriverRouteEntity driverRoute,
                                                      NxDisShipmentTaskEntity task,
                                                      int lateMinutes,
                                                      boolean batchEligible) {
        String lateReason = inferStopLateReason(driverRoute, batchEligible);
        return buildWarning(
                DisRouteWarningCode.STOP_LATE,
                SEVERITY_WARN,
                driverRoute.getNxDdrDriverUserId(),
                driverRoute.getDriverName(),
                driverRoute.getNxDdrId(),
                null,
                task.getNxDstId(),
                task.getNxDstDepName(),
                lateMinutes,
                task.getNxDstDepName() + " 预计迟到 " + lateMinutes + " 分钟",
                lateReason);
    }

    private RouteDispatchWarning buildLockedIneligibleWarning(NxDisDriverRouteEntity driverRoute,
                                                              NxDisShipmentTaskEntity task) {
        return buildWarning(
                DisRouteWarningCode.LOCKED_ON_INELIGIBLE_DRIVER,
                SEVERITY_ERROR,
                driverRoute.getNxDdrDriverUserId(),
                driverRoute.getDriverName(),
                driverRoute.getNxDdrId(),
                null,
                task.getNxDstId(),
                task.getNxDstDepName(),
                null,
                "已锁定站点「" + task.getNxDstDepName() + "」分给了不可派司机",
                "请改派司机或调整 duty");
    }

    private String resolveRouteDate(NxDisRoutePlanEntity plan) {
        if (plan.getNxDrpRouteDate() != null && !plan.getNxDrpRouteDate().trim().isEmpty()) {
            return plan.getNxDrpRouteDate().trim();
        }
        if (plan.getNxDrpPlanDate() != null && !plan.getNxDrpPlanDate().trim().isEmpty()) {
            return plan.getNxDrpPlanDate().trim();
        }
        throw new IllegalArgumentException("路线计划缺少 routeDate");
    }

    private String inferStopLateReason(NxDisDriverRouteEntity driverRoute, boolean batchEligible) {
        if (!batchEligible) {
            return "司机当前不可派";
        }
        if (driverRoute.getNxDdrPlannedDepartAt() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(driverRoute.getNxDdrPlannedDepartAt());
            if (cal.get(Calendar.HOUR_OF_DAY) >= 12) {
                return "司机出发过晚";
            }
        }
        return "路线时长超出客户最晚送达时间";
    }

    private RouteDispatchWarning buildSuggestEarlierDepartWarning(NxDisDriverRouteEntity driverRoute,
                                                                  int routeLateMinutes) {
        return buildWarning(
                DisRouteWarningCode.SUGGEST_EARLIER_DEPART,
                SEVERITY_INFO,
                driverRoute.getNxDdrDriverUserId(),
                driverRoute.getDriverName(),
                driverRoute.getNxDdrId(),
                null,
                null,
                null,
                routeLateMinutes,
                "若将出发时间提前约 " + routeLateMinutes + " 分钟，可能消除本路线迟到",
                "当前仅提示，不会自动调整出发时间");
    }

    private RouteDispatchWarning buildWarning(String code,
                                              String severity,
                                              Integer driverUserId,
                                              String driverName,
                                              Integer routeId,
                                              Integer stopId,
                                              Integer taskId,
                                              String departmentName,
                                              Integer lateMinutes,
                                              String message,
                                              String suggestion) {
        RouteDispatchWarning warning = new RouteDispatchWarning();
        warning.setCode(code != null ? code : DisRouteWarningCode.UNKNOWN);
        warning.setSeverity(severity);
        warning.setDriverUserId(driverUserId);
        warning.setDriverName(driverName);
        warning.setRouteId(routeId);
        warning.setStopId(stopId);
        warning.setTaskId(taskId);
        warning.setDepartmentName(departmentName);
        warning.setLateMinutes(lateMinutes);
        warning.setMessage(message);
        warning.setSuggestion(suggestion);
        return warning;
    }

    private static final class RouteDriverAssessment {
        private boolean batchEligible;
        private String ineligibleReason;
        private int activeStopCount;
        private boolean anyWait;
        private boolean anyLate;
        private boolean infeasibleLocked;
        private String routeFeasibilityStatus;
        private final List<RouteDispatchWarning> warnings = new ArrayList<RouteDispatchWarning>();
    }
}
