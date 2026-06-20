package com.nongxinle.service;

import com.nongxinle.dto.platform.customer.PlatformCustomerCategoryItem;
import com.nongxinle.dto.platform.customer.PlatformCustomerGoodsCategoriesRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerHomeInitRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerHomeInitResponse;
import com.nongxinle.dto.platform.customer.PlatformCustomerMarketSupplierItem;
import com.nongxinle.dto.platform.customer.PlatformCustomerMarketSuppliersRequest;

import java.util.List;

public interface PlatformCustomerHomeService {

    PlatformCustomerHomeInitResponse homeInit(PlatformCustomerHomeInitRequest request);

    List<PlatformCustomerMarketSupplierItem> listMarketSuppliers(PlatformCustomerMarketSuppliersRequest request);

    List<PlatformCustomerCategoryItem> listGoodsCategories(PlatformCustomerGoodsCategoriesRequest request);
}
