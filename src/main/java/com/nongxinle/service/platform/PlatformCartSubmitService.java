package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.customer.PlatformCartAddWithSupplierResponse;
import com.nongxinle.dto.platform.customer.PlatformCartSubmitRequest;

/**
 * 原 submitBySupplier 降级：购物车临时阶段写入带配送商 SKU 的临时行，不创建 bill。
 */
public interface PlatformCartSubmitService {

    PlatformCartAddWithSupplierResponse submitBySupplier(PlatformCartSubmitRequest request);
}
