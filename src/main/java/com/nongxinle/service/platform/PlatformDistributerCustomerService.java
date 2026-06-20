package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.distributer.PlatformDistributerTodayCustomersRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerTodayCustomersResponse;

public interface PlatformDistributerCustomerService {

    PlatformDistributerTodayCustomersResponse listTodayCustomers(PlatformDistributerTodayCustomersRequest request);
}
