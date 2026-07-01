package com.nongxinle.dispatch.core.port;

import com.nongxinle.dispatch.core.domain.DispatchDriver;
import com.nongxinle.dispatch.core.domain.DispatchTenantRef;

import java.util.List;

/** 司机数据源端口。 */
public interface DispatchDriverSource {

    List<DispatchDriver> loadAvailableDrivers(DispatchTenantRef tenant, String routeDate);
}
