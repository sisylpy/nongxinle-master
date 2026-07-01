package com.nongxinle.dispatch.core.port;

import com.nongxinle.dispatch.core.domain.DispatchDepot;
import com.nongxinle.dispatch.core.domain.DispatchTenantRef;

/** 仓库/发货点数据源端口。 */
public interface DispatchDepotSource {

    DispatchDepot loadDepot(DispatchTenantRef tenant);
}
