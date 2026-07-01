package com.nongxinle.dispatch.core.statemachine;

import com.nongxinle.dispatch.core.domain.DispatchRouteStatus;
import com.nongxinle.dispatch.core.domain.DispatchStopStatus;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 路线 / 站点状态迁移策略（纯规则，无 IO）。
 * 各 tenant adapter 在写库前调用校验。
 */
public final class DispatchStatusTransitionPolicy {

    private static final Map<DispatchStopStatus, Set<DispatchStopStatus>> STOP_TRANSITIONS;

    private static final Map<DispatchRouteStatus, Set<DispatchRouteStatus>> ROUTE_TRANSITIONS;

    static {
        Map<DispatchStopStatus, Set<DispatchStopStatus>> stopMap =
                new HashMap<DispatchStopStatus, Set<DispatchStopStatus>>();
        stopMap.put(DispatchStopStatus.UNASSIGNED, EnumSet.of(
                DispatchStopStatus.ASSIGNED, DispatchStopStatus.CANCELLED));
        stopMap.put(DispatchStopStatus.ASSIGNED, EnumSet.of(
                DispatchStopStatus.LOADING, DispatchStopStatus.CANCELLED, DispatchStopStatus.EXCEPTION));
        stopMap.put(DispatchStopStatus.LOADING, EnumSet.of(
                DispatchStopStatus.IN_DELIVERY, DispatchStopStatus.EXCEPTION, DispatchStopStatus.CANCELLED));
        stopMap.put(DispatchStopStatus.IN_DELIVERY, EnumSet.of(
                DispatchStopStatus.DELIVERED, DispatchStopStatus.EXCEPTION));
        stopMap.put(DispatchStopStatus.DELIVERED, EnumSet.noneOf(DispatchStopStatus.class));
        stopMap.put(DispatchStopStatus.EXCEPTION, EnumSet.of(
                DispatchStopStatus.IN_DELIVERY, DispatchStopStatus.DELIVERED, DispatchStopStatus.CANCELLED));
        stopMap.put(DispatchStopStatus.CANCELLED, EnumSet.of(
                DispatchStopStatus.UNASSIGNED, DispatchStopStatus.ASSIGNED));
        STOP_TRANSITIONS = Collections.unmodifiableMap(stopMap);

        Map<DispatchRouteStatus, Set<DispatchRouteStatus>> routeMap =
                new HashMap<DispatchRouteStatus, Set<DispatchRouteStatus>>();
        routeMap.put(DispatchRouteStatus.DRAFT, EnumSet.of(
                DispatchRouteStatus.LOADING, DispatchRouteStatus.CANCELLED));
        routeMap.put(DispatchRouteStatus.LOADING, EnumSet.of(
                DispatchRouteStatus.IN_DELIVERY, DispatchRouteStatus.CANCELLED));
        routeMap.put(DispatchRouteStatus.IN_DELIVERY, EnumSet.of(
                DispatchRouteStatus.COMPLETED, DispatchRouteStatus.CANCELLED));
        routeMap.put(DispatchRouteStatus.COMPLETED, EnumSet.noneOf(DispatchRouteStatus.class));
        routeMap.put(DispatchRouteStatus.IDLE, EnumSet.of(
                DispatchRouteStatus.DRAFT, DispatchRouteStatus.LOADING));
        routeMap.put(DispatchRouteStatus.CANCELLED, EnumSet.of(
                DispatchRouteStatus.DRAFT, DispatchRouteStatus.IDLE));
        ROUTE_TRANSITIONS = Collections.unmodifiableMap(routeMap);
    }

    private DispatchStatusTransitionPolicy() {
    }

    public static boolean canTransitionStop(DispatchStopStatus from, DispatchStopStatus to) {
        if (from == null || to == null || from == to) {
            return false;
        }
        Set<DispatchStopStatus> allowed = STOP_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public static boolean canTransitionRoute(DispatchRouteStatus from, DispatchRouteStatus to) {
        if (from == null || to == null || from == to) {
            return false;
        }
        Set<DispatchRouteStatus> allowed = ROUTE_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
