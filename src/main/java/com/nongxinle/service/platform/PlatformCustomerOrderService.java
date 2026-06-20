package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.customer.PlatformCustomerOrderDeleteRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerOrderLineItem;
import com.nongxinle.dto.platform.customer.PlatformCustomerOrderListRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerOrderUpdateRequest;

import java.util.List;

public interface PlatformCustomerOrderService {

    List<PlatformCustomerOrderLineItem> listTodayLines(PlatformCustomerOrderListRequest request);

    PlatformCustomerOrderLineItem updateLine(PlatformCustomerOrderUpdateRequest request);

    void deleteLine(PlatformCustomerOrderDeleteRequest request);
}
