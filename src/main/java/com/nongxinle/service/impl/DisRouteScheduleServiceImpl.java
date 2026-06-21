package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDepartmentDao;
import com.nongxinle.dao.NxDisDriverDutyDao;
import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisRouteStopDao;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDisDriverDutyEntity;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.route.DisRouteBatchDefaults;
import com.nongxinle.route.DisRouteScheduleStatus;
import com.nongxinle.route.DisRouteStopTimeWindowStatus;
import com.nongxinle.service.DisRouteScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;

@Service
public class DisRouteScheduleServiceImpl implements DisRouteScheduleService {

    private static final int DEFAULT_DEPART_HOUR = 6;
    private static final int DEFAULT_SERVICE_MINUTES = 30;
    private static final String STOP_CANCELLED = "CANCELLED";

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisRouteStopDao nxDisRouteStopDao;
    @Autowired
    private NxDepartmentDao nxDepartmentDao;
    @Autowired
    private NxDisDriverDutyDao nxDisDriverDutyDao;
    @Autowired
    private DisRoutePlanBatchHelper disRoutePlanBatchHelper;

    @Override
    @Transactional
    public void computeSchedule(Integer planId) {
        NxDisRoutePlanEntity plan = disRoutePlanBatchHelper.ensureBatchPersisted(planId);

        String routeDate = resolveRouteDate(plan);
        Date dayStart = parseRouteDateStart(routeDate);
        Date defaultDepartAt = DisRouteBatchDefaults.resolveDefaultDepartAt(plan);
        if (defaultDepartAt == null) {
            defaultDepartAt = addHours(dayStart, DEFAULT_DEPART_HOUR);
        }

        List<NxDisDriverRouteEntity> driverRoutes = nxDisDriverRouteDao.queryByPlanId(planId);

        Date planStartAt = null;
        Date planEndAt = null;
        int planTotalWait = 0;
        int planTotalLate = 0;
        boolean planHasLate = false;
        boolean planAllNoWindow = !driverRoutes.isEmpty();
        boolean hasScheduledRoute = false;

        for (NxDisDriverRouteEntity driverRoute : driverRoutes) {
            RouteScheduleResult routeResult = computeDriverRouteSchedule(
                    plan, driverRoute, routeDate, dayStart, defaultDepartAt);
            if (routeResult == null) {
                continue;
            }
            hasScheduledRoute = true;
            planTotalWait += routeResult.totalWaitMinutes;
            planTotalLate += routeResult.totalLateMinutes;
            if (DisRouteScheduleStatus.HAS_LATE.equals(routeResult.scheduleStatus)) {
                planHasLate = true;
            }
            if (!DisRouteScheduleStatus.NO_WINDOW.equals(routeResult.scheduleStatus)) {
                planAllNoWindow = false;
            }
            if (routeResult.plannedDepartAt != null) {
                planStartAt = minDate(planStartAt, routeResult.plannedDepartAt);
            }
            if (routeResult.plannedFinishAt != null) {
                planEndAt = maxDate(planEndAt, routeResult.plannedFinishAt);
            }
        }

        NxDisRoutePlanEntity planUpdate = new NxDisRoutePlanEntity();
        planUpdate.setNxDrpId(planId);
        planUpdate.setNxDrpPlannedStartAt(planStartAt);
        planUpdate.setNxDrpPlannedEndAt(planEndAt);
        planUpdate.setNxDrpTotalWaitMinutes(planTotalWait);
        planUpdate.setNxDrpTotalLateMinutes(planTotalLate);
        if (!hasScheduledRoute) {
            planUpdate.setNxDrpScheduleStatus(DisRouteScheduleStatus.OK);
        } else if (planHasLate) {
            planUpdate.setNxDrpScheduleStatus(DisRouteScheduleStatus.HAS_LATE);
        } else if (planAllNoWindow) {
            planUpdate.setNxDrpScheduleStatus(DisRouteScheduleStatus.NO_WINDOW);
        } else {
            planUpdate.setNxDrpScheduleStatus(DisRouteScheduleStatus.OK);
        }
        nxDisRoutePlanDao.updateSchedule(planUpdate);
    }

    private RouteScheduleResult computeDriverRouteSchedule(NxDisRoutePlanEntity plan,
                                                           NxDisDriverRouteEntity driverRoute,
                                                           String routeDate,
                                                           Date dayStart,
                                                           Date defaultDepartAt) {
        List<NxDisRouteStopEntity> allStops = nxDisRouteStopDao.queryByDriverRouteId(driverRoute.getNxDdrId());
        List<NxDisRouteStopEntity> activeStops = new ArrayList<NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : allStops) {
            if (isCountableStop(stop)) {
                activeStops.add(stop);
            }
        }

        Date plannedDepartAt = resolvePlannedDepartAt(plan, driverRoute, routeDate, defaultDepartAt);
        Date cursor = plannedDepartAt;
        Date lastDeparture = plannedDepartAt;

        int totalServiceMinutes = 0;
        int totalWaitMinutes = 0;
        int totalLateMinutes = 0;
        boolean routeHasLate = false;
        boolean routeAllNoWindow = !activeStops.isEmpty();

        for (NxDisRouteStopEntity stop : activeStops) {
            long legSeconds = stop.getNxDrsLegDurationS() != null ? stop.getNxDrsLegDurationS() : 0L;
            Date plannedArrivalAt = addSeconds(cursor, legSeconds);

            StopWindowConfig windowConfig = resolveStopWindowConfig(stop);
            Integer earliestSeconds = windowConfig.earliestSeconds;
            Integer latestSeconds = windowConfig.latestSeconds;
            int serviceMinutes = windowConfig.serviceMinutes;

            Date earliestAt = earliestSeconds != null ? addSeconds(dayStart, earliestSeconds) : null;
            Date latestAt = latestSeconds != null ? addSeconds(dayStart, latestSeconds) : null;

            StopWindowResult windowResult = applyTimeWindow(plannedArrivalAt, earliestAt, latestAt);
            Date plannedServiceStartAt = windowResult.plannedServiceStartAt;
            Date plannedDepartureAt = addMinutes(plannedServiceStartAt, serviceMinutes);

            NxDisRouteStopEntity stopUpdate = new NxDisRouteStopEntity();
            stopUpdate.setNxDrsId(stop.getNxDrsId());
            stopUpdate.setNxDrsEarliestDeliveryTimeS(earliestSeconds);
            stopUpdate.setNxDrsLatestDeliveryTimeS(latestSeconds);
            stopUpdate.setNxDrsServiceMinutes(serviceMinutes);
            stopUpdate.setNxDrsPlannedArrivalAt(plannedArrivalAt);
            stopUpdate.setNxDrsPlannedServiceStartAt(plannedServiceStartAt);
            stopUpdate.setNxDrsPlannedDepartureAt(plannedDepartureAt);
            stopUpdate.setNxDrsWaitMinutes(windowResult.waitMinutes);
            stopUpdate.setNxDrsLateMinutes(windowResult.lateMinutes);
            stopUpdate.setNxDrsTimeWindowStatus(windowResult.timeWindowStatus);
            nxDisRouteStopDao.updateSchedule(stopUpdate);

            totalServiceMinutes += serviceMinutes;
            totalWaitMinutes += windowResult.waitMinutes;
            totalLateMinutes += windowResult.lateMinutes;
            if (DisRouteStopTimeWindowStatus.LATE.equals(windowResult.timeWindowStatus)) {
                routeHasLate = true;
            }
            if (!DisRouteStopTimeWindowStatus.NO_WINDOW.equals(windowResult.timeWindowStatus)) {
                routeAllNoWindow = false;
            }

            cursor = plannedDepartureAt;
            lastDeparture = plannedDepartureAt;
        }

        NxDisDriverRouteEntity routeUpdate = new NxDisDriverRouteEntity();
        routeUpdate.setNxDdrId(driverRoute.getNxDdrId());
        routeUpdate.setNxDdrPlannedDepartAt(plannedDepartAt);
        routeUpdate.setNxDdrPlannedFinishAt(activeStops.isEmpty() ? plannedDepartAt : lastDeparture);
        routeUpdate.setNxDdrTotalServiceMinutes(totalServiceMinutes);
        routeUpdate.setNxDdrTotalWaitMinutes(totalWaitMinutes);
        routeUpdate.setNxDdrTotalLateMinutes(totalLateMinutes);

        String routeScheduleStatus;
        if (activeStops.isEmpty()) {
            routeScheduleStatus = DisRouteScheduleStatus.OK;
        } else if (routeHasLate) {
            routeScheduleStatus = DisRouteScheduleStatus.HAS_LATE;
        } else if (routeAllNoWindow) {
            routeScheduleStatus = DisRouteScheduleStatus.NO_WINDOW;
        } else {
            routeScheduleStatus = DisRouteScheduleStatus.OK;
        }
        routeUpdate.setNxDdrScheduleStatus(routeScheduleStatus);
        nxDisDriverRouteDao.updateSchedule(routeUpdate);

        RouteScheduleResult result = new RouteScheduleResult();
        result.plannedDepartAt = plannedDepartAt;
        result.plannedFinishAt = routeUpdate.getNxDdrPlannedFinishAt();
        result.totalWaitMinutes = totalWaitMinutes;
        result.totalLateMinutes = totalLateMinutes;
        result.scheduleStatus = routeScheduleStatus;
        return result;
    }

    private Date resolvePlannedDepartAt(NxDisRoutePlanEntity plan,
                                      NxDisDriverRouteEntity driverRoute,
                                      String routeDate,
                                      Date defaultDepartAt) {
        Date plannedDepartAt = defaultDepartAt;
        NxDisDriverDutyEntity duty = nxDisDriverDutyDao.queryByDisDriverDate(
                plan.getNxDrpDistributerId(), driverRoute.getNxDdrDriverUserId(), routeDate);
        if (duty != null && ON_DUTY.equals(duty.getNxDddDutyStatus()) && duty.getNxDddCheckInAt() != null) {
            if (duty.getNxDddCheckInAt().after(plannedDepartAt)) {
                plannedDepartAt = duty.getNxDddCheckInAt();
            }
        }
        return plannedDepartAt;
    }

    /** Phase 2b-5：优先使用 stop/task 当日快照，无快照时才回退 department 默认值。 */
    private StopWindowConfig resolveStopWindowConfig(NxDisRouteStopEntity stop) {
        StopWindowConfig config = new StopWindowConfig();
        config.earliestSeconds = stop.getNxDrsEarliestDeliveryTimeS();
        config.latestSeconds = stop.getNxDrsLatestDeliveryTimeS();
        config.serviceMinutes = stop.getNxDrsServiceMinutes() != null
                ? stop.getNxDrsServiceMinutes() : DEFAULT_SERVICE_MINUTES;

        if (config.earliestSeconds == null && config.latestSeconds == null && stop.getNxDrsDepartmentId() != null) {
            NxDepartmentEntity department = loadDepartment(stop.getNxDrsDepartmentId());
            config.earliestSeconds = department != null ? department.getNxDepartmentEarliestDeliveryTime() : null;
            config.latestSeconds = department != null ? department.getNxDepartmentLatestDeliveryTime() : null;
            if (stop.getNxDrsServiceMinutes() == null) {
                config.serviceMinutes = resolveServiceMinutes(department);
            }
        }
        return config;
    }

    private StopWindowResult applyTimeWindow(Date plannedArrivalAt, Date earliestAt, Date latestAt) {
        StopWindowResult result = new StopWindowResult();
        if (earliestAt == null && latestAt == null) {
            result.timeWindowStatus = DisRouteStopTimeWindowStatus.NO_WINDOW;
            result.waitMinutes = 0;
            result.lateMinutes = 0;
            result.plannedServiceStartAt = plannedArrivalAt;
            return result;
        }
        if (earliestAt != null && plannedArrivalAt.before(earliestAt)) {
            result.timeWindowStatus = DisRouteStopTimeWindowStatus.EARLY_WAIT;
            result.waitMinutes = minutesCeil(earliestAt.getTime() - plannedArrivalAt.getTime());
            result.lateMinutes = 0;
            result.plannedServiceStartAt = earliestAt;
            return result;
        }
        if (latestAt != null && plannedArrivalAt.after(latestAt)) {
            result.timeWindowStatus = DisRouteStopTimeWindowStatus.LATE;
            result.waitMinutes = 0;
            result.lateMinutes = minutesCeil(plannedArrivalAt.getTime() - latestAt.getTime());
            result.plannedServiceStartAt = plannedArrivalAt;
            return result;
        }
        result.timeWindowStatus = DisRouteStopTimeWindowStatus.OK;
        result.waitMinutes = 0;
        result.lateMinutes = 0;
        result.plannedServiceStartAt = plannedArrivalAt;
        return result;
    }

    private NxDepartmentEntity loadDepartment(Integer departmentId) {
        if (departmentId == null) {
            return null;
        }
        return nxDepartmentDao.queryObject(departmentId);
    }

    private int resolveServiceMinutes(NxDepartmentEntity department) {
        if (department != null && department.getNxDepartmentUnloadDuration() != null
                && department.getNxDepartmentUnloadDuration() > 0) {
            return department.getNxDepartmentUnloadDuration();
        }
        return DEFAULT_SERVICE_MINUTES;
    }

    private boolean isCountableStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        String status = stop.getNxDrsStopStatus();
        return status == null || !STOP_CANCELLED.equalsIgnoreCase(status);
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

    private Date parseRouteDateStart(String routeDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setLenient(false);
            return format.parse(routeDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("路线日格式无效: " + routeDate);
        }
    }

    private Date addHours(Date base, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(base);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    private Date addSeconds(Date base, long seconds) {
        return new Date(base.getTime() + seconds * 1000L);
    }

    private Date addMinutes(Date base, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(base);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    private int minutesCeil(long diffMs) {
        if (diffMs <= 0L) {
            return 0;
        }
        return (int) Math.ceil(diffMs / 60000.0);
    }

    private Date minDate(Date current, Date candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null || candidate.before(current)) {
            return candidate;
        }
        return current;
    }

    private Date maxDate(Date current, Date candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null || candidate.after(current)) {
            return candidate;
        }
        return current;
    }

    private static final class StopWindowResult {
        private String timeWindowStatus;
        private int waitMinutes;
        private int lateMinutes;
        private Date plannedServiceStartAt;
    }

    private static final class StopWindowConfig {
        private Integer earliestSeconds;
        private Integer latestSeconds;
        private int serviceMinutes;
    }

    private static final class RouteScheduleResult {
        private Date plannedDepartAt;
        private Date plannedFinishAt;
        private int totalWaitMinutes;
        private int totalLateMinutes;
        private String scheduleStatus;
    }
}
