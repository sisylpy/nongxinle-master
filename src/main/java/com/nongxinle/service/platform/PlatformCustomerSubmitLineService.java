package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.PlatformSubmitLineRequest;
import com.nongxinle.dto.platform.PlatformSubmitLineResponse;

/**
 * 来源 A：购物车临时阶段写入无配送商 SKU 的临时行（status=-1，不建 bill）。
 * checkoutConfirm 才新建 bill 并挂行；再次采购须新 checkout 生成新 bill。
 */
public interface PlatformCustomerSubmitLineService {

    PlatformSubmitLineResponse submitLine(PlatformSubmitLineRequest request);
}
