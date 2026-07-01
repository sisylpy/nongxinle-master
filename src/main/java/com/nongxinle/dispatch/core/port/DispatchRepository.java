package com.nongxinle.dispatch.core.port;

import com.nongxinle.dispatch.core.domain.DispatchPlan;
import com.nongxinle.dispatch.core.domain.DispatchRoute;
import com.nongxinle.dispatch.core.domain.DispatchStop;
import com.nongxinle.dispatch.core.domain.DispatchTenantRef;

import java.util.List;
import java.util.Optional;

/** 派单计划 / 路线 / 站点持久化端口（adapter 实现，core 仅定义契约）。 */
public interface DispatchRepository {

    Optional<DispatchPlan> findPlan(DispatchTenantRef tenant, String routeDate);

    DispatchPlan savePlan(DispatchPlan plan);

    List<DispatchRoute> findRoutesByPlanId(Integer planId);

    DispatchRoute saveRoute(DispatchRoute route);

    List<DispatchStop> findStopsByRouteId(Integer driverRouteId);

    DispatchStop saveStop(DispatchStop stop);
}
