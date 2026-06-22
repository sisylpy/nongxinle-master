package com.nongxinle.route;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

/**
 * Phase 3D：已出发路线退出沙盘分配，但须保留/恢复派车路线快照（里程、leg、排程标签）。
 * <p>
 * 沙盘优化/改派不参与；坐标 leg 重算与排程标签 enrichment 属于只读展示，不是沙盘分配。
 */
public final class DisRouteExecutionRouteSnapshotHelper {

    private DisRouteExecutionRouteSnapshotHelper() {
    }

    /** 从 DB driver_route 合并路线级快照（总里程、站点数、计划出发/返回等）。 */
    public static void mergeRouteSnapshotFromDb(NxDisDriverRouteEntity target, NxDisDriverRouteEntity dbRoute) {
        if (target == null || dbRoute == null) {
            return;
        }
        DisRouteRouteExecutionHelper.mergeExecutionFieldsFromDb(target, dbRoute);
        if (dbRoute.getNxDdrStopCount() != null) {
            target.setNxDdrStopCount(dbRoute.getNxDdrStopCount());
        }
        if (dbRoute.getNxDdrTotalDistanceM() != null && dbRoute.getNxDdrTotalDistanceM() > 0) {
            target.setNxDdrTotalDistanceM(dbRoute.getNxDdrTotalDistanceM());
        }
        if (dbRoute.getNxDdrTotalDurationS() != null && dbRoute.getNxDdrTotalDurationS() > 0) {
            target.setNxDdrTotalDurationS(dbRoute.getNxDdrTotalDurationS());
        }
        if (dbRoute.getNxDdrPlannedDepartAt() != null) {
            target.setNxDdrPlannedDepartAt(dbRoute.getNxDdrPlannedDepartAt());
        }
        if (dbRoute.getNxDdrPlannedFinishAt() != null) {
            target.setNxDdrPlannedFinishAt(dbRoute.getNxDdrPlannedFinishAt());
            target.setPlannedReturnAt(dbRoute.getNxDdrPlannedFinishAt());
        }
        if (dbRoute.getNxDdrTotalServiceMinutes() != null) {
            target.setNxDdrTotalServiceMinutes(dbRoute.getNxDdrTotalServiceMinutes());
        }
        if (dbRoute.getNxDdrTotalWaitMinutes() != null) {
            target.setNxDdrTotalWaitMinutes(dbRoute.getNxDdrTotalWaitMinutes());
        }
        if (dbRoute.getNxDdrTotalLateMinutes() != null) {
            target.setNxDdrTotalLateMinutes(dbRoute.getNxDdrTotalLateMinutes());
        }
        if (dbRoute.getNxDdrScheduleStatus() != null) {
            target.setNxDdrScheduleStatus(dbRoute.getNxDdrScheduleStatus());
        }
    }

    /** stop leg / 排程：优先 task DB，其次 legacy route_stop；不把 null 强行写成 0。 */
    public static void hydrateStopSnapshotFromPersistence(NxDisRouteStopEntity stop,
                                                        NxDisShipmentTaskEntity task,
                                                        NxDisRouteStopEntity legacyStop) {
        if (stop == null) {
            return;
        }
        Long legDistance = firstPositive(task != null ? task.getNxDstLegDistanceM() : null,
                legacyStop != null ? legacyStop.getNxDrsLegDistanceM() : null,
                stop.getNxDrsLegDistanceM());
        Long legDuration = firstPositive(task != null ? task.getNxDstLegDurationS() : null,
                legacyStop != null ? legacyStop.getNxDrsLegDurationS() : null,
                stop.getNxDrsLegDurationS());
        if (legDistance != null) {
            stop.setNxDrsLegDistanceM(legDistance);
            if (task != null) {
                task.setNxDstLegDistanceM(legDistance);
            }
        }
        if (legDuration != null) {
            stop.setNxDrsLegDurationS(legDuration);
            if (task != null) {
                task.setNxDstLegDurationS(legDuration);
            }
        }
        String provider = firstNonBlank(task != null ? task.getLegDistanceProvider() : null,
                stop.getLegDistanceProvider());
        String distanceType = firstNonBlank(task != null ? task.getLegDistanceType() : null,
                stop.getLegDistanceType());
        if (provider != null) {
            stop.setLegDistanceProvider(provider);
            if (task != null) {
                task.setLegDistanceProvider(provider);
            }
        }
        if (distanceType != null) {
            stop.setLegDistanceType(distanceType);
            if (task != null) {
                task.setLegDistanceType(distanceType);
            }
        }
        if (task != null) {
            if (stop.getNxDrsPlannedArrivalAt() == null && task.getNxDstPlannedArrivalAt() != null) {
                stop.setNxDrsPlannedArrivalAt(task.getNxDstPlannedArrivalAt());
            }
            if (stop.getNxDrsPlannedServiceStartAt() == null && task.getNxDstPlannedServiceStartAt() != null) {
                stop.setNxDrsPlannedServiceStartAt(task.getNxDstPlannedServiceStartAt());
            }
            if (stop.getNxDrsPlannedDepartureAt() == null && task.getNxDstPlannedDepartureAt() != null) {
                stop.setNxDrsPlannedDepartureAt(task.getNxDstPlannedDepartureAt());
            }
            if (stop.getNxDrsWaitMinutes() == null && task.getNxDstWaitMinutes() != null) {
                stop.setNxDrsWaitMinutes(task.getNxDstWaitMinutes());
            }
            if (stop.getNxDrsLateMinutes() == null && task.getNxDstLateMinutes() != null) {
                stop.setNxDrsLateMinutes(task.getNxDstLateMinutes());
            }
            if (stop.getNxDrsTimeWindowStatus() == null && task.getNxDstTimeWindowStatus() != null) {
                stop.setNxDrsTimeWindowStatus(task.getNxDstTimeWindowStatus());
            }
        }
        if (legacyStop != null) {
            if (stop.getNxDrsPlannedArrivalAt() == null && legacyStop.getNxDrsPlannedArrivalAt() != null) {
                stop.setNxDrsPlannedArrivalAt(legacyStop.getNxDrsPlannedArrivalAt());
            }
            if (stop.getNxDrsPlannedServiceStartAt() == null && legacyStop.getNxDrsPlannedServiceStartAt() != null) {
                stop.setNxDrsPlannedServiceStartAt(legacyStop.getNxDrsPlannedServiceStartAt());
            }
            if (stop.getNxDrsPlannedDepartureAt() == null && legacyStop.getNxDrsPlannedDepartureAt() != null) {
                stop.setNxDrsPlannedDepartureAt(legacyStop.getNxDrsPlannedDepartureAt());
            }
            if (legDistance == null && legacyStop.getNxDrsLegDistanceM() != null
                    && legacyStop.getNxDrsLegDistanceM() > 0) {
                stop.setNxDrsLegDistanceM(legacyStop.getNxDrsLegDistanceM());
            }
            if (legDuration == null && legacyStop.getNxDrsLegDurationS() != null
                    && legacyStop.getNxDrsLegDurationS() > 0) {
                stop.setNxDrsLegDurationS(legacyStop.getNxDrsLegDurationS());
            }
        }
    }

    public static boolean routeSnapshotIncomplete(NxDisDriverRouteEntity route) {
        if (route == null) {
            return true;
        }
        if (route.getNxDdrTotalDistanceM() != null && route.getNxDdrTotalDistanceM() > 0) {
            return false;
        }
        if (route.getStops() == null || route.getStops().isEmpty()) {
            return true;
        }
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop == null) {
                continue;
            }
            Long leg = stop.getNxDrsLegDistanceM();
            if (leg != null && leg > 0) {
                return false;
            }
            NxDisShipmentTaskEntity task = stop.getShipmentTask();
            if (task != null && task.getNxDstLegDistanceM() != null && task.getNxDstLegDistanceM() > 0) {
                return false;
            }
        }
        return true;
    }

    public static int resolveEffectiveStopCount(NxDisDriverRouteEntity route) {
        if (route == null) {
            return 0;
        }
        if (route.getNxDdrStopCount() != null && route.getNxDdrStopCount() > 0) {
            return route.getNxDdrStopCount();
        }
        if (route.getTotalStopCount() != null && route.getTotalStopCount() > 0) {
            return route.getTotalStopCount();
        }
        if (route.getStops() == null) {
            return 0;
        }
        int count = 0;
        for (NxDisRouteStopEntity stop : route.getStops()) {
            if (stop != null) {
                count++;
            }
        }
        return count;
    }

    public static NxDisDriverRouteEntity buildRouteSnapshotUpdate(NxDisDriverRouteEntity route) {
        if (route == null || route.getNxDdrId() == null) {
            return null;
        }
        NxDisDriverRouteEntity update = new NxDisDriverRouteEntity();
        update.setNxDdrId(route.getNxDdrId());
        if (route.getNxDdrStopCount() != null) {
            update.setNxDdrStopCount(route.getNxDdrStopCount());
        } else if (route.getStops() != null) {
            update.setNxDdrStopCount(route.getStops().size());
        }
        if (route.getNxDdrTotalDistanceM() != null) {
            update.setNxDdrTotalDistanceM(route.getNxDdrTotalDistanceM());
        }
        if (route.getNxDdrTotalDurationS() != null) {
            update.setNxDdrTotalDurationS(route.getNxDdrTotalDurationS());
        }
        return update;
    }

    public static NxDisDriverRouteEntity buildRouteScheduleSnapshotUpdate(NxDisDriverRouteEntity route) {
        if (route == null || route.getNxDdrId() == null) {
            return null;
        }
        NxDisDriverRouteEntity update = new NxDisDriverRouteEntity();
        update.setNxDdrId(route.getNxDdrId());
        update.setNxDdrPlannedDepartAt(route.getNxDdrPlannedDepartAt());
        update.setNxDdrPlannedFinishAt(route.getNxDdrPlannedFinishAt() != null
                ? route.getNxDdrPlannedFinishAt() : route.getPlannedReturnAt());
        update.setNxDdrTotalServiceMinutes(route.getNxDdrTotalServiceMinutes());
        update.setNxDdrTotalWaitMinutes(route.getNxDdrTotalWaitMinutes());
        update.setNxDdrTotalLateMinutes(route.getNxDdrTotalLateMinutes());
        update.setNxDdrScheduleStatus(route.getNxDdrScheduleStatus());
        return update;
    }

    public static NxDisShipmentTaskEntity buildTaskLegSnapshotUpdate(NxDisRouteStopEntity stop) {
        if (stop == null || stop.getShipmentTask() == null || stop.getShipmentTask().getNxDstId() == null) {
            return null;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
        update.setNxDstId(task.getNxDstId());
        if (stop.getNxDrsLegDistanceM() != null) {
            update.setNxDstLegDistanceM(stop.getNxDrsLegDistanceM());
        } else if (task.getNxDstLegDistanceM() != null) {
            update.setNxDstLegDistanceM(task.getNxDstLegDistanceM());
        }
        if (stop.getNxDrsLegDurationS() != null) {
            update.setNxDstLegDurationS(stop.getNxDrsLegDurationS());
        } else if (task.getNxDstLegDurationS() != null) {
            update.setNxDstLegDurationS(task.getNxDstLegDurationS());
        }
        update.setNxDstPlannedArrivalAt(stop.getNxDrsPlannedArrivalAt() != null
                ? stop.getNxDrsPlannedArrivalAt() : task.getNxDstPlannedArrivalAt());
        update.setNxDstPlannedServiceStartAt(stop.getNxDrsPlannedServiceStartAt() != null
                ? stop.getNxDrsPlannedServiceStartAt() : task.getNxDstPlannedServiceStartAt());
        update.setNxDstPlannedDepartureAt(stop.getNxDrsPlannedDepartureAt() != null
                ? stop.getNxDrsPlannedDepartureAt() : task.getNxDstPlannedDepartureAt());
        update.setNxDstWaitMinutes(stop.getNxDrsWaitMinutes() != null
                ? stop.getNxDrsWaitMinutes() : task.getNxDstWaitMinutes());
        update.setNxDstLateMinutes(stop.getNxDrsLateMinutes() != null
                ? stop.getNxDrsLateMinutes() : task.getNxDstLateMinutes());
        update.setNxDstTimeWindowStatus(stop.getNxDrsTimeWindowStatus() != null
                ? stop.getNxDrsTimeWindowStatus() : task.getNxDstTimeWindowStatus());
        return update;
    }

    private static Long firstPositive(Long... values) {
        if (values == null) {
            return null;
        }
        for (Long value : values) {
            if (value != null && value > 0) {
                return value;
            }
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }
}
