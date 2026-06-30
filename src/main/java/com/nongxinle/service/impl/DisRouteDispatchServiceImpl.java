package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisRouteStopDao;
import com.nongxinle.dao.NxDistributerUserDao;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.service.DisRouteDispatchService;
import com.nongxinle.service.DisShipmentTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisUserDriver;

/** 派单 plan 读模型与司机账号列表；全量 simulate 写链已删除。 */
@Service("disRouteDispatchService")
public class DisRouteDispatchServiceImpl implements DisRouteDispatchService {

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisRouteStopDao nxDisRouteStopDao;
    @Autowired
    private NxDistributerUserDao nxDistributerUserDao;
    @Autowired
    private DisShipmentTaskService disShipmentTaskService;

    @Override
    public NxDisRoutePlanEntity getPlan(Integer planId) {
        return loadPlanDetailWithTasks(planId);
    }

    @Override
    public List<NxDistributerUserEntity> listDrivers(Integer disId) {
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
