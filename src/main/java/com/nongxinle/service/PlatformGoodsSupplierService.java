package com.nongxinle.service;

import com.nongxinle.dto.platform.PlatformSupplierItem;
import com.nongxinle.dto.platform.PlatformSuppliersRequest;

import java.util.List;

public interface PlatformGoodsSupplierService {

    List<PlatformSupplierItem> listSuppliers(PlatformSuppliersRequest request);
}
