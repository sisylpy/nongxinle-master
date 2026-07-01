package com.nongxinle.dispatch.core.port;

import com.nongxinle.dispatch.core.domain.DispatchOrder;
import com.nongxinle.dispatch.core.domain.DispatchTenantRef;

import java.util.List;

/** 待派单订单数据源端口。 */
public interface DispatchOrderSource {

    List<DispatchOrder> loadEligibleOrders(DispatchTenantRef tenant, String routeDate);
}
