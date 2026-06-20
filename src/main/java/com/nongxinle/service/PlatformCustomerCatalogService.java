package com.nongxinle.service;

import com.nongxinle.dto.platform.customer.PlatformCustomerGoodsCatalogListRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerGoodsCatalogTreeRequest;
import com.nongxinle.utils.PageUtils;

import java.util.Map;

public interface PlatformCustomerCatalogService {

    Map<String, Object> buildCatalogTree(PlatformCustomerGoodsCatalogTreeRequest request);

    PageUtils listGoodsByGrandCategory(PlatformCustomerGoodsCatalogListRequest request);
}
