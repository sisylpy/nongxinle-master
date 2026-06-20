package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.PlatformCartLineCreateCommand;
import com.nongxinle.dto.platform.PlatformCartLineCreateResult;
import com.nongxinle.entity.GbDepartmentOrdersEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;

public interface PlatformGbNxOrderLineService {

    PlatformCartLineCreateResult createPlatformCartLine(PlatformCartLineCreateCommand command);

    /**
     * checkoutConfirm 正式化：补建购物车临时阶段省略的 dep_dis_goods / purchase_goods / NX purchase（仅已选配送商行）。
     */
    void formalizeCartLineAtCheckout(GbDepartmentOrdersEntity gbOrder, NxDepartmentOrdersEntity nxOrder);
}
